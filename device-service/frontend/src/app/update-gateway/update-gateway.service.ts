import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from 'src/environments/environment';
import { assetdetails, Gateway, sensor } from '../models/gateway';
import { Observable } from 'rxjs';


@Injectable({
    providedIn: 'root'
})
export class UpdateGatewayService {

    gateway: Gateway;
    sensor: sensor;
    navigateFromSettings;
    editFlag = false;
    asset_details: assetdetails;

    constructor(private http: HttpClient) { }
    private baseUrl = environment.baseUrl;
    private UserUrl = this.baseUrl + '/gateway';
    private sensorUrl = this.baseUrl + '/sensor';


    public createGateway(gateway) {
        return this.http.post<any>(this.UserUrl + '/shipping-detail', gateway);
    }

    public updateGateway(gateway) {
        return this.http.put<any>(this.UserUrl + '/bulkUpdate', gateway);
    }

    getSensorSectionList() {
        const url = this.UserUrl + '/sensor-section-list';
        return this.http.get<any>(url);
    }

    getSensorList() {
        const url = this.sensorUrl + '/sensor-list';
        return this.http.get<any>(url);
    }

    uploadGatewayList(gatewayList: any): Observable<any> {
        const url = this.baseUrl + '/gateway/bulk';
        return this.http.post(url, gatewayList);
    }


    checkListOfDeviceHaveImeiOrMacAddress(gatewayList: any): Observable<any> {
        const url = this.baseUrl + '/gateway/checkGatewayListData';
        return this.http.post(url, gatewayList);
    }

    bulkUpdate(gatewayList: any): Observable<any> {
        const url = this.baseUrl + '/gateway/bulk';
        return this.http.put(url, gatewayList);
    }

    bulkUpdateDeviceDataForwoarding(dfRequest: any): Observable<any> {
        const url = this.baseUrl + '/df/bulk/update';
        return this.http.put(url, dfRequest);
    }
}
