import {HttpClient} from "@angular/common/http";
import {Injectable} from "@angular/core";
import {Observable, ReplaySubject, take} from "rxjs";
import {CarRequest} from "../model/car-request";
import {MatSnackBar} from "@angular/material/snack-bar";

@Injectable({
  providedIn: 'root'
})
export class RequestService {

  // private floorRequestUpdateEmitter = new ReplaySubject<any>();
  // private intervalId: number;

  constructor(private http: HttpClient,
              private snackbar: MatSnackBar) {

  }

  public getRequests(): Observable<CarRequest[]> {
    return this.http.get<CarRequest[]>("/svc/elevator/requests");
  }

  public saveRequest(request: CarRequest): Observable<CarRequest> {
    console.log(JSON.stringify(request));
    return this.http.post<CarRequest>("/svc/elevator/requests", request);
  }

  // public getFloorRequestUpdates(): Observable<any> {
  //   if (false && !this.intervalId) {
  //     // TODO: Use websockets for push notification so UI does not have to poll
  //     this.intervalId = setInterval(() => {
  //         this.getRequests().pipe(take(1)).subscribe({
  //           next: requests => {
  //             const floorRequests: any = {};
  //             requests.forEach(r => {
  //               const floorKey = this.buildFloorKey(r.floor, r.direction);
  //               if (!floorRequests[floorKey]) {
  //                 floorRequests[floorKey] = r;
  //               }
  //             });
  //             // Notify floor request observers
  //             this.floorRequestUpdateEmitter.next(floorRequests);
  //           },
  //           error: err => {
  //             this.snackbar.open("Error fetching requests: " + JSON.stringify(err), undefined, {
  //               duration: 10000
  //             });
  //           }
  //         });
  //       }, 3000);
  //   }
  //   return this.floorRequestUpdateEmitter.asObservable();
  // }

  // public buildFloorKey(floor: number, direction: "UP" | "DOWN" | undefined): string {
  //   return floor + "_" + direction;
  // }

}
