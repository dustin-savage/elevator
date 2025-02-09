import {HttpClient} from "@angular/common/http";
import {Injectable} from "@angular/core";
import {Observable, ReplaySubject, take} from "rxjs";
import {Floor} from "../model/floor";
import {Car} from "../model/car";
import {MatSnackBar} from "@angular/material/snack-bar";

@Injectable({
  providedIn: 'root'
})
export class ElevatorService {
  private carUpdateEmitter = new ReplaySubject<Car[]>();
  private intervalId: number;

  constructor(private http: HttpClient,
              private snackbar: MatSnackBar) {

  }

  getFloors(): Observable<Floor[]> {
    return this.http.get<Floor[]>("/svc/elevator/floors");
  }

  getCars(): Observable<Car[]> {
    return this.http.get<Car[]>("/svc/elevator/cars");
  }

  completeRequests(car: any): Observable<void> {
    return this.http.post<void>("/svc/elevator/cars/" + car.id + "/completeRequest", car);
  }

  public getCarUpdates(): Observable<Car[]> {
    if (!this.intervalId) {
      // TODO: Use websockets for push notification so UI does not have to poll
      this.intervalId = setInterval(() => {
        this.getCars().pipe(take(1)).subscribe({
          next: cars => {
            // Notify car observers
            this.carUpdateEmitter.next(cars);
          },
          error: err => {
            this.snackbar.open("Error fetching requests: " + JSON.stringify(err), undefined, {
              duration: 10000
            });
          }
        });
      }, 4000);
    }
    return this.carUpdateEmitter.asObservable();
  }

}
