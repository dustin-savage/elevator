import {CarRequest} from "./car-request";

export class Car {
  id: number;
  currentFloor: number;
  direction: "UP" | "DOWN";
  isDoorOpen: boolean;
  enabled: boolean;
  request?: CarRequest;

}
