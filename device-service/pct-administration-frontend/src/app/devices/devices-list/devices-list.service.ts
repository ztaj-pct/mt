import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpHeaders } from '@angular/common/http';
import { environment } from 'src/environments/environment';
import { map } from 'underscore';

@Injectable({
  providedIn: 'root'
})

export class DevicesListService {
  deviceId:any;
  deviceList: any[] = [];
  navigateFromSettings: any;
  editFlag: boolean = false;
  userCompanyType: any;
  productname: any;
  isSuperAdmin: any;
  selectedIdsToDelete: string[] = [];
  filterValues: any = '{ "product_name" : "SMART7"}';
  deviceImeiList: string[] =[];



  constructor(private http: HttpClient) { }
  private baseUrl = environment.baseUrl;


  getGatewayListSortwithCompanyPage(currentPage: string, sort: string, order: string, limit: string | number, filterValues: any) {
    var url = this.baseUrl + "/device/page?_type=active&_page=" + currentPage + "&_limit=" + limit + "&_sort=" + sort + "&_order=" + order;
    return this.http.post<any>(url, filterValues);
  }

  //Testing of new AGGrid Starts
  getDeviceListSortwithCompanyPage() {
    var url = this.baseUrl + "/device/page?_type=active&_page=1&_limit=25&_sort=imei&_order=asc";
    return this.http.post<any>(url, this.filterValues);
  }


  getDeviceTestReports(currentPage: string, sort: string, order: string, limit: string, filter: any) {
    console.log("device Report")
  //  console.log(filterModel)
    let url = this.baseUrl + "/device/device-reports?_deviceid=015115006460447&_page=" + currentPage + "&_limit=" + limit + "&_sort=" + sort + "&_order=" + order;
    return this.http.post<any>(url,filter);
  } 

  
  getDeviceReportsElastic(currentPage: string, sort: string, order: string, limit: string, filter: any) {
    //let url = this.baseUrl + "/device/elastic-report?_deviceid=015115006460447&from=" + currentPage + "&size=" + limit + "&deviceId=015115006460447";
    let url = "http://localhost:7082/report/getDeviceFromElastic?from=0&size=5&deviceId=015115006460447";
    return this.http.post<any>(url,filter);
  } 

  getDeviceReportsTest(currentPage: string, sort: string, order: string, limit: string, filter: any) {
   this.deviceImeiList[0]="015115006460447";
    this.deviceImeiList[1] =filter;
    let url = this.baseUrl + "/device/device-reports-test?_page=" + currentPage + "&_limit=" + limit + "&_sort=" + sort + "&_order=" + order;
    return this.http.post<any>(url,this.deviceImeiList);
  } 

  // getHeaders() {
  //   let url = this.baseUrl + "/device/report-header";
  //   return this.http.get<any>(url);
  // } 

  //Testing of new AGGrid Ends

  getGatewayDetails(currentPage: string) {
    var url = this.baseUrl + "/gateway/summary?_type=active&_limit=25&_page=" + currentPage;
    return this.http.get<any>(url);
  }

  getConfigurationDetails(imei:string) {
    var url = this.baseUrl + "/gateway/getConfiguration?imei="+imei;
    return this.http.get<any>(url);
  }

  bulkDeleteForGateways(uuid_list: string[]) {
    return this.http.post<any>(this.baseUrl + "/gateway/bulkDelete", uuid_list);
  }

  getParsedReport(rawReport: string,format: string)
  {
    console.log("Inside getParseRawReport in service class")
    var url = this.baseUrl + "/device/parse-report?raw-report="+rawReport+"&format="+format;
    return this.http.get<any>(url);
  }
}
