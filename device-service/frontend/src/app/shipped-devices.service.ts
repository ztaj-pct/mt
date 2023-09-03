import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import { map } from 'rxjs/operators';
import { ShippedDevices } from './models/ShippedDevices';

const httpOptions = {
  headers: new HttpHeaders({
    'responseType': 'ResponseContentType.Blob'
  })
};
@Injectable({
  providedIn: 'root'
})
export class ShippedDeviceService {
  navigateFromSettings;
  editFlag: boolean = false;
  isSuperAdmin;
  

  constructor(private http: HttpClient) { }
  private baseUrl = environment.baseUrl;

  addShippedDevice(shippedDevices: ShippedDevices): Observable<any> {
    let url = this.baseUrl + "/shipment";
    return this.http.post(url, shippedDevices);
  }

  getSalesForceOrderDetailByEpicorNumber(epicorNumber: String): Observable<any> {
    let url = "https://se5un0pzn7.execute-api.us-east-2.amazonaws.com/Prod/order?epicor-order-number=" + epicorNumber;
    return this.http.get(url);
  }

}
