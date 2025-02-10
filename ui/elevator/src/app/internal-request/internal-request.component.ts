import {ChangeDetectorRef, Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {Floor} from "../model/floor";
import {RequestService} from "../service/request.service";
import {Car} from "../model/car";
import {MatSnackBar} from "@angular/material/snack-bar";
import {CarRequest} from "../model/car-request";
import {take} from "rxjs";
import {ElevatorService} from "../service/elevator.service";

@Component({
  selector: 'app-internal-request',
  templateUrl: './internal-request.component.html',
  styleUrl: './internal-request.component.scss'
})
export class InternalRequestComponent implements OnInit {

  @Input()
  car: Car;

  @Input()
  floorKeyToRequest: any;

  floors: Floor[];

  constructor(private requestService: RequestService,
              private elevatorService: ElevatorService,
              private changeDetectorRef: ChangeDetectorRef,
              private snackbar: MatSnackBar) {
  }

  ngOnInit(): void {
    // Get the floors so we know which buttons to define in the internal panel.
    this.elevatorService.getFloors().pipe(take(1)).subscribe({
      next: floors => {
        console.log("Got floors internal.");
        this.floors = floors;
        this.changeDetectorRef.markForCheck();
      },
      error: err => {
        this.snackbar.open("Error sending request: " + JSON.stringify(err), undefined, {
          duration: 10000
        });
      }
    });
  }

  onRequest(floor: Floor) {
    console.log("request: " + floor.id);
    const req: CarRequest = {
      floor: floor.id,
      assignedCarId: this.car.id
    };
    this.requestService.saveRequest(req).pipe(take(1)).subscribe({
      next: r => {
        this.snackbar.open("Sent request.");
        const key = this.requestService.buildRequestKey(req);
        this.floorKeyToRequest[key] = req;
        this.changeDetectorRef.markForCheck();
      },
      error: err => {
        this.snackbar.open("Error sending request: " + JSON.stringify(err), undefined, {
          duration: 10000
        });
      }
    });
  }

  getButtonStyle(floor: number): any {
    console.log(JSON.stringify(this.floorKeyToRequest));
    const requestKey = floor + "_" + this.car.id;
    return {lit: this.floorKeyToRequest[requestKey]};
  }

}
