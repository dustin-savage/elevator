import {ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit} from '@angular/core';
import {ElevatorService} from "../service/elevator.service";
import {Floor} from "../model/floor";
import {Car} from "../model/car";
import {forkJoin, Observable, ReplaySubject, take, takeUntil} from "rxjs";
import {MatSnackBar} from "@angular/material/snack-bar";
import {RequestService} from "../service/request.service";
import {CommonModule} from "@angular/common";
import {FloorComponent} from "../floor/floor.component";
import {InternalButtonsComponent} from "../internal-buttons/internal-buttons.component";
import {CarComponent} from "../car/car.component";
import {CarRequest} from "../model/car-request";
import {MatIconModule} from "@angular/material/icon";

@Component({
  selector: 'app-building',
  standalone: true,
  imports: [
    FloorComponent,
    CommonModule,
    InternalButtonsComponent,
    CarComponent,
    MatIconModule
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

  ngOnDestroy(): void {
    this.destroyed.next(true);
    this.destroyed.complete();
  }

  getShaftsStyle() {
    // total shaft height is 80vh
    const totalViewHeight = 80;
    const floorHeight = totalViewHeight / this.floors.length;
    return {
      gap: "calc((100vw - " + this.cars.length * floorHeight + "vh + " + this.cars.length * 4 + "px) / " + this.cars.length + ")",
    };
  }

  getShaftStyle() {
    // total shaft height is 80vh
    const totalViewHeight = 80;
    const floorHeight = totalViewHeight / this.floors.length;
    return {
      width: "calc(" + floorHeight + "vh + " + this.cars.length + "px)"
    };
  }

  onSelectAll() {
    const obsArray: Observable<CarRequest>[] = [];
    this.floors.forEach(floor => {
      if (this.floors.length - 1 != floor.id) {
        obsArray.push(this.sendRequest(floor, "UP"));
      }
      if (0 != floor.id) {
        obsArray.push(this.sendRequest(floor, "DOWN"));
      }
    });
    this.floorKeyToRequest = Object.assign({}, this.floorKeyToRequest);
    this.changeDetectorRef.markForCheck();

    forkJoin(obsArray).pipe().subscribe({
      next: r => {
        this.snackbar.open("Sent all requests.");
      },
      error: err => {
        this.snackbar.open("Error sending requests: " + JSON.stringify(err), undefined, {
          duration: 10000
        });
      }
    });
  }

  private sendRequest(floor: Floor, direction: "UP" | "DOWN"): Observable<CarRequest> {
    let req: CarRequest = {
      floor: floor.id,
      direction: direction,
      assignedCarId: -1
    };
    const floorKey = this.requestService.buildRequestKey(req);
    this.floorKeyToRequest[floorKey] = req;
    return this.requestService.saveRequest(req);
  }

}
