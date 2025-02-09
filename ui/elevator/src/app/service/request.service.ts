import {HttpClient} from "@angular/common/http";
import {Injectable} from "@angular/core";
import {Observable} from "rxjs";
import {CarRequest} from "../model/car-request";

@Injectable({
  providedIn: 'root'
})
export class RequestService {

  constructor(private http: HttpClient) {

  }

  public getRequests(): Observable<CarRequest[]> {
    return this.http.get<CarRequest[]>("/svc/elevator/requests");
  }

  public saveRequest(request: CarRequest): Observable<CarRequest> {
    console.log(JSON.stringify(request));
    return this.http.post<CarRequest>("/svc/elevator/requests", request);
  }

}
