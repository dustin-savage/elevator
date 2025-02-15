import {ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit} from '@angular/core';
import {ElevatorService} from "../service/elevator.service";
import {Floor} from "../model/floor";
import {Car} from "../model/car";
import {forkJoin, ReplaySubject, take, takeUntil} from "rxjs";
import {MatSnackBar} from "@angular/material/snack-bar";
import {RequestService} from "../service/request.service";
import {CarRequest} from "../model/car-request";
import {CommonModule, NgForOf} from "@angular/common";
import {FloorComponent} from "../floor/floor.component";
import {InternalButtonsComponent} from "../internal-buttons/internal-buttons.component";

@Component({
  selector: 'app-building',
  standalone: true,
  imports: [
    NgForOf,
    FloorComponent,
    CommonModule,
    InternalButtonsComponent
  ],
  templateUrl: './building.component.html',
  styleUrl: './building.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class BuildingComponent implements OnInit, OnDestroy {

  floors: Floor[] = [];
  cars: Car[] = [];
  floorKeyToRequest: any = {};
  destroyed = new ReplaySubject<boolean>(1);

  constructor(private elevatorService: ElevatorService,
              private requestService: RequestService,
              private snackbar: MatSnackBar,
              private changeDetectorRef: ChangeDetectorRef) {
  }

  ngOnInit(): void {
    const obsArr = [];
    obsArr.push(this.elevatorService.getFloors());
    obsArr.push(this.elevatorService.getCars());

    forkJoin(obsArr).pipe(take(1)).subscribe({
      next: r => {
        this.snackbar.open("Got elevator info!");
        this.floors = (<Floor[]>r[0]).reverse();
        this.cars = <Car[]>r[1];
        this.changeDetectorRef.markForCheck();
      },
      error: err => {
        this.snackbar.open("Error fetching elevator info: " + JSON.stringify(err), undefined, {
          duration: 10000
        });
      }
    });

    // Car logic for request updates so that we can move the cars
    this.elevatorService.getCarUpdates().pipe(takeUntil(this.destroyed)).subscribe({
      next: cars => {
        cars.filter(c => c.request).forEach(c => {
          console.log("Got a car with a request");
          const car: Car | undefined = this.cars.find(car => car.id === c.id);
          if (car && c.request?.floor !== undefined) {
            console.log("Got car with request");
            car.request = c.request;
            console.log("Moving car");
            // Move the car to the desired floor
            car.currentFloor = car.request.floor;
            this.changeDetectorRef.markForCheck();
            // Using timeouts so the css transitions have time to work.
            setTimeout(() => {
              // Open the door
              car.isDoorOpen = true;
              this.changeDetectorRef.markForCheck();
              setTimeout(() => {
                // Close the door
                car.isDoorOpen = false;
                this.changeDetectorRef.markForCheck();
                // Logic for request updates so that we can light up the buttons
                this.requestService.getRequests().pipe(take(1)).subscribe({
                  next: requests => {
                    // Map requests to keys that can be used to apply CSS.
                    // Ex: "2_UP" in the map would cause the up arrow to light up on the 2nd floor.
                    const floorKeyToRequest: any = {};
                    requests.forEach(r => {
                      const floorKey = this.requestService.buildRequestKey(r);
                      if (!floorKeyToRequest[floorKey]) {
                        floorKeyToRequest[floorKey] = r;
                      }
                    });
                    console.log("Got requests for button panels");
                    this.floorKeyToRequest = floorKeyToRequest;
                    this.changeDetectorRef.markForCheck();
                  },
                  error: err => {
                    this.snackbar.open("Error fetching requests: " + JSON.stringify(err));
                  }
                });
              }, 2000);
            }, 1000);

            // Complete requests on that floor and direction
            this.elevatorService.completeRequests(car).pipe(take(1)).subscribe({
              next: r => {
                console.log("Completed requests!");
              },
              error: err => {
                this.snackbar.open("Error fetching cars: " + JSON.stringify(err));
              }
            });
          }
        });
      },
      error: err => {
        this.snackbar.open("Error fetching cars: " + JSON.stringify(err));
      }
    });

  }

  getCarClass(car: Car) {
    return {
      car: true,
      ["car-floor-" + car.currentFloor]: true,
      isOpen: car.isDoorOpen
    };
  }

  ngOnDestroy(): void {
    this.destroyed.next(true);
    this.destroyed.complete();
  }

}
