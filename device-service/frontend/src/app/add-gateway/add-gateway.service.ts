import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from 'src/environments/environment';
import { assetdetails, Gateway, sensor } from '../models/gateway';


@Injectable({
    providedIn: 'root'
})
export class AddGatewayService {

    gateway: Gateway;
    sensor : sensor;
    navigateFromSettings;
    editFlag: boolean = false;
    asset_details: assetdetails;

    constructor(private http: HttpClient) { }
    private baseUrl = environment.baseUrl;
    private UserUrl = this.baseUrl + '/gateway';

   
    public createGateway(gateway) {
        return this.http.post<any>(this.UserUrl+ "/shipping-detail", gateway);
    }

   

}