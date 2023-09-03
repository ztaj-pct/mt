import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Filter } from 'src/app/models/Filter.model';
import { environment } from 'src/environments/environment';

@Injectable({
  providedIn: 'root'
})
export class DeviceReportsService {
  deviceId:any;
  deviceList: any[] = [];
  navigateFromSettings: any;
  editFlag: boolean = false;
  userCompanyType: any;
  productname: any;
  isSuperAdmin: any;
  deviceImeiList: string[] =[];
  selectedIdsToDelete: string[] = [];
  elasticFilter: Filter = new Filter();

  constructor(private http: HttpClient) { }
  private baseUrl = environment.baseUrl;


  getDeviceReports(currentPage: string, sort: string, order: string, limit: string) {
    let url = this.baseUrl + "/device/device-reports?_page=" + currentPage + "&_limit=" + limit + "&_sort=" + sort + "&_order=" + order;
    return this.http.post<any>(url, this.deviceImeiList);
  } 

  getDeviceReportsElastic(currentPage: string, sort: string, order: string, limit: string, filter: any) {
    //let url = this.baseUrl + "/device/elastic-report?_deviceid=015115006460447&from=" + currentPage + "&size=" + limit + "&deviceId=015115006460447";
    //let url = "http://localhost:7082/report/getDeviceFromElastic?from=0&size="+limit+"&deviceId=015115004226907";
    let url = "http://100.24.214.108:7081/report/getDeviceFromElastic?from=0&size=5&deviceId=015115004226907";
    return this.http.post<any>(url,filter);
  }
}
