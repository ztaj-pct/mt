import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from 'src/environments/environment';
import { ATCRequest } from './models/ATCRequest';
import { ATCRequests } from './models/atrequest.model';

@Injectable({
  providedIn: 'root'
})
export class ATCommandService {

  deviceId: string;
  isAddATCommand: Boolean = false;
  constructor(private http: HttpClient) { }
  private baseUrl = environment.baseUrl;
  isATCommandsummary: Boolean = false;


  sendATCRequest(atcRequest) {
    const url = this.baseUrl + '/gateway-command/atc-request';
    return this.http.post<any>(url, atcRequest);
  }

  getATCQueueRequest(deviceId, currentPage, sort, order, limit) {
    const url = this.baseUrl + '/gateway-command/queued-atc-request?device_id=' + deviceId + '&_page=' + currentPage + '&_limit=' + limit + '&_sort=' + sort + '&_order=' + order;
    return this.http.get<any>(url);
  }

  // getGatewayAtRequest(deviceId: any, currentPage: string, sort: string, order: string, limit: any,filtervalues: any) {
  //   var url = this.baseUrl + "/atcommand/gateway-atc-request?gateway_Id="+deviceId+"&page="+currentPage+"&limit="+limit+"&sort="+sort+"&order="+order;
  //   return this.http.post<any>(url,filtervalues);
  // }


  getGatewayAtRequest(deviceId: any, currentPage: string, sort: string, order: string, limit: any) {
    const url = this.baseUrl + '/gateway-command/gateway-atc-request?gateway_Id=' + deviceId + '&page=' + currentPage + '&limit=' + limit + '&sort=' + sort + '&order=' + order;
    return this.http.get<any>(url);
  }

  getCombineGatewayAtRequest(deviceId: any, currentPage: string, sort: string, order: string, limit: any) {
    const url = this.baseUrl + '/gateway-command/gateway-combine-atc-request?gateway_Id=' + deviceId + '&page=' + currentPage + '&limit=' + limit + '&sort=' + sort + '&order=' + order;
    return this.http.get<any>(url);
  }

  deleteAtCommandById(atcRequest: ATCRequests) {
    const url = this.baseUrl + '/gateway-command/atc-delete';
    return this.http.post<any>(url, atcRequest);
  }
}
