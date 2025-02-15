import {Component, Input} from '@angular/core';
import {Car} from "../model/car";
import {CommonModule} from "@angular/common";
import {Floor} from "../model/floor";

@Component({
  selector: 'app-car',
  standalone: true,
  imports: [
    CommonModule
  ],
  templateUrl: './car.component.html',
  styleUrl: './car.component.scss'
})
export class CarComponent {

  @Input()
  car: Car;

  @Input()
  floors: Floor[];

  getCarClass() {
    return {
      car: true,
      ["car-floor-" + this.car.currentFloor]: true,
      isOpen: this.car.isDoorOpen
    };
  }

  getCarStyle() {
    // total shaft height is 80vh
    const totalViewHeight = 80;
    const floorHeight = totalViewHeight / this.floors.length;
    const marginTop = totalViewHeight - ((this.car.currentFloor + 1) * floorHeight);
    return {
      marginTop: "calc(" + marginTop + "vh - 2px)",
      height: "calc(" + floorHeight + "vh - 4px)"
    };
  }

}
