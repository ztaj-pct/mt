import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from 'src/environments/environment';
import { assetdetails, Gateway, sensor } from '../models/gateway';
import { Observable } from 'rxjs';


@Injectable({
    providedIn: 'root'
})
export class GatewayDataForwardService {

    gateway: Gateway;
    sensor: sensor;
    navigateFromSettings;
    editFlag = false;
    asset_details: assetdetails;

    constructor(private http: HttpClient) { }
    private baseUrl = environment.baseUrl;
    
    public getAllCustomerForwardingRuleUrl() {
        return this.http.get<any>(this.baseUrl + '/customer/forwarding/rule/urls' );
      }
      
      public updateDataForwardRule(obj) {
        return this.http.put<any>(this.baseUrl + '/df/batch/rules', obj);
    }
}
