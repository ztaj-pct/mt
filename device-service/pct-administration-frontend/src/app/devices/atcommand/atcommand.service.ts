import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from 'src/environments/environment';

@Injectable({
  providedIn: 'root'
})
export class AtcommandService {
  deviceId:string ="";
  isAddATCommand :Boolean = false;
  constructor(private http: HttpClient) { }
  private baseUrl = environment.baseUrl;

  sendATCRequest(atcRequest: any)
  {
    console.log("Inside sendATCRequest in service class")
    var url = this.baseUrl + "/atcommand/send-atc-request";
    return this.http.post<any>(url,atcRequest);
  }

  getATCQueueRequest(deviceId: string,currentPage: string, sort: string, order: string, limit: any)
  {
    console.log("Inside getATCQueueRequest in service class")
    var url = this.baseUrl + "/atcommand/queued-atc-request?device_id="+deviceId+"&_page="+currentPage+"&_limit="+limit+"&_sort="+sort+"&_order="+order;
    return this.http.get<any>(url);
  }
}
