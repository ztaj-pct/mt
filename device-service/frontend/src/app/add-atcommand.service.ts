import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from 'src/environments/environment';
import { ATCRequest } from './models/ATCRequest';

@Injectable({
  providedIn: 'root'
})
export class AddATCommandService {
 atcRequest : ATCRequest;

  constructor(private http: HttpClient) { }
  private baseUrl = environment.baseUrl;

  sendATCRequest(atcRequest)
  {
    var url = this.baseUrl + "/atcommand/send-atc-request";
    return this.http.post<any>(url,atcRequest);
  }
}
