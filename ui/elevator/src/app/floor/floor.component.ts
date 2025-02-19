import {ChangeDetectionStrategy, ChangeDetectorRef, Component, Input} from '@angular/core';
import {Floor} from "../model/floor";
import {CarRequest} from "../model/car-request";
import {RequestService} from "../service/request.service";
import {MatSnackBar} from "@angular/material/snack-bar";
import {MatIconModule} from "@angular/material/icon";
import {MatButtonModule} from "@angular/material/button";
import {CommonModule} from "@angular/common";

@Component({
  selector: 'app-floor',
  standalone: true,
  imports: [
    MatIconModule,
    MatButtonModule,
    CommonModule
  ],
  templateUrl: './floor.component.html',
  styleUrl: './floor.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class FloorComponent {
  @Input()
  floorCount: number;

  @Input()
  floor: Floor;

  @Input()
  isTop: boolean;

  @Input()
  isBottom: boolean;

  @Input()
  floorKeyToRequest: any = {};

  constructor(private requestService: RequestService,
              private snackbar: MatSnackBar,
              private changeDetectorRef: ChangeDetectorRef) {
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
        const floorKey = this.requestService.buildRequestKey(req);
        this.floorKeyToRequest[floorKey] = req;
        this.changeDetectorRef.markForCheck();
      },
      error: err => {
        this.snackbar.open("Error sending request: " + JSON.stringify(err), undefined, {
          duration: 10000
        });
      }
    });
  }

  getFloorPanelButtonClass(floor: Floor, direction: "UP" | "DOWN" | undefined) {
    const floorKey = this.requestService.buildRequestKey({
      floor: floor.id,
      direction,
      assignedCarId: -1
    });
    return {lit: this.floorKeyToRequest[floorKey]};
  }

  getFloorStyle() {
    // total shaft height is 80vh
    const totalViewHeight = 80;
    const floorHeight = totalViewHeight / this.floorCount;
    return {
      height: "calc(" + floorHeight + "vh - 2px)"
    };
  }

}
