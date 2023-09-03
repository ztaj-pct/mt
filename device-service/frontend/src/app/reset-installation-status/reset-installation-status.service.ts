import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from 'src/environments/environment';

@Injectable({
    providedIn: 'root'
})
export class ResetInstalationStatusService {
    constructor(private http: HttpClient) { }
    private baseUrl = environment.baseUrl;
    private Url = this.baseUrl + '/installation';
    private CutomerUrl = this.baseUrl + '/customer';

    public resetInstalationStatus(vin, deviceID,can) {
        console.log(deviceID);
        
         if(vin === null || vin === undefined){
            vin="";
        }
        
        if(deviceID === null || deviceID === undefined){
            deviceID="";
        }
        return this.http.put<any>(this.Url + "/reset?vin=" + vin + "&device_id=" +deviceID + "&can=" + can, null);
    }

    public resetCustomerTestData(uuid) {
        return this.http.post<any>(this.CutomerUrl + "/reset?customer_uuid=" + uuid, null);
    }


    public markInstallationForRework(install_code) {
        return this.http.put<any>(this.Url + "/mark-installation-for-rework?install_code=" + install_code, null);
    }

}