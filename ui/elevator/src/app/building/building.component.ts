import {Component, OnDestroy, OnInit} from '@angular/core';
import {ElevatorService} from "../service/elevator.service";
import {Floor} from "../model/floor";
import {Car} from "../model/car";
import {forkJoin, ReplaySubject, take, takeUntil} from "rxjs";
import {MatSnackBar} from "@angular/material/snack-bar";
import {RequestService} from "../service/request.service";
import {CarRequest} from "../model/car-request";

@Component({
  selector: 'app-building',
  templateUrl: './building.component.html',
  styleUrl: './building.component.scss'
})
export class BuildingComponent implements OnInit, OnDestroy {

  floors: Floor[] = [];
  cars: Car[] = [];
  floorKeyToRequest: any = {};
  destroyed = new ReplaySubject<boolean>(1);

  constructor(private elevatorService: ElevatorService,
              private requestService: RequestService,
              private snackbar: MatSnackBar) {

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
            // Using timeouts so the css transitions have time to work.
            setTimeout(() => {
              // Open the door
              car.isDoorOpen = true;
              setTimeout(() => {
                // Close the door
                car.isDoorOpen = false;
                // Logic for request updates so that we can light up the buttons
                this.requestService.getRequests().pipe(take(1)).subscribe({
                  next: requests => {
                    const floorKeyToRequest: any = {};
                    requests.forEach(r => {
                      const floorKey = this.buildFloorKey(r.floor, r.direction);
                      if (!floorKeyToRequest[floorKey]) {
                        floorKeyToRequest[floorKey] = r;
                      }
                    });
                    console.log("Got floor requests");
                    this.floorKeyToRequest = floorKeyToRequest;
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

  private buildFloorKey(floor: number, direction: "UP" | "DOWN" | undefined): string {
    return floor + "_" + direction;
  }

  getCarClass(car: Car) {
    return {
      car: true,
      ["car-floor-" + car.currentFloor]: true,
      isOpen: car.isDoorOpen
    };
  }

  onCarRequest(floor: Floor, direction: "UP" | "DOWN") {
    const req: CarRequest = {
      floor: floor.id,
      direction: direction,
      assignedCarId: -1
    };
    this.requestService.saveRequest(req).pipe().subscribe({
      next: r => {
        this.snackbar.open("Sent request.");
        const floorKey = this.buildFloorKey(req.floor, req.direction);
        this.floorKeyToRequest[floorKey] = req;
        // Update to detect changes
        this.floorKeyToRequest = Object.assign({}, this.floorKeyToRequest);
      },
      error: err => {
        this.snackbar.open("Error sending request: " + JSON.stringify(err), undefined, {
          duration: 10000
        });
      }
    });
  }

  getFloorPanelButtonClass(floor: Floor, direction: "UP" | "DOWN" | undefined) {
    const floorKey = this.buildFloorKey(floor.id, direction)
    return {lit: this.floorKeyToRequest[floorKey]};
  }

  ngOnDestroy(): void {
    this.destroyed.next(true);
    this.destroyed.complete();
  }

}
