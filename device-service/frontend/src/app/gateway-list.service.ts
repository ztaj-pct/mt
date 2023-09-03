import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpHeaders } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { environment } from 'src/environments/environment';
import { Gateway } from './models/gateway';
import { map } from 'rxjs/operators';
import { DeviceForwarding, Forwarding } from './models/forwarding';

@Injectable({
  providedIn: 'root'
})
export class GatewayListService {

  gateway: Gateway;
  gatewayList: any[];
  navigateFromSettings: any;
  editFlag = false;
  userCompanyType: any;
  productname: any;
  isSuperAdmin: any;
  selectedIdsToDelete: string[] = [];
  deviceImeiList: string[] = [];
  deviceId: string;
  rawReport: string;
  organisationId: string;
  parseFlag = false;
  isDeviceReport = false;
  forwarding: any;
  deviceForwarding = new DeviceForwarding();
  constructor(private http: HttpClient) { }
  private baseUrl = environment.baseUrl;
  organisationName: String;
  filterNameForGatewaySummary: String;




  // getGatewayWithSort(currentPage, column, order,pageLimit) {
  //   var url = this.baseUrl + '/gateway/summary?_type=active&_limit='+pageLimit+'&_page=' + currentPage + '&_sort=' + column + '&_order=' + order;
  //   return this.http.get<any>(url);
  // }

  exportDataIntoCSV(currentPage: number, sort: string, order: string, limit: string | number, filterValues: any) {
    const url = this.baseUrl + '/device/exportToCsvWithFilterForDevice?_type=active&_page=' + currentPage + '&_limit=' + limit + '&_sort=' + sort + '&_order=' + order;
    return this.http.post(url, filterValues, { responseType: 'blob' as 'blob' });
    // .pipe(
    //   map((data: any) => {
    //     const blob = new Blob([data], {
    //       type: 'text/csv' // for excel
    //     });
    //     const link = document.createElement('a');
    //     link.href = window.URL.createObjectURL(blob);
    //     link.download = 'DevicesFiltered.csv';
    //     link.click();
    //     window.URL.revokeObjectURL(link.href);
    //   })
    // );
  }
  exportDataIntoCSVInLocal(currentPage: number, sort: string, order: string, limit: string | number, filterValues: any) {
    const url = this.baseUrl + '/device/exportToCsvWithFilterForDevice?_type=active&_page=' + currentPage + '&_limit=' + limit + '&_sort=' + sort + '&_order=' + order;
    return this.http.post(url, filterValues, { responseType: 'blob' as 'blob' }).pipe(
      map((data: any) => {
        const blob = new Blob([data], {
          type: 'text/csv' // for excel
        });
        const link = document.createElement('a');
        link.href = window.URL.createObjectURL(blob);
        link.download = 'DevicesFiltered.csv';
        link.click();
        window.URL.revokeObjectURL(link.href);
      })
    );
  }

  getDeivceCountByOrganization(data:any) {
    const url = this.baseUrl + '/device/count_by_org_id';
    return this.http.post<any>(url, data);
  }
  getGatewayListSortwithCompanyPage(currentPage: number, sort: string, order: string, limit: string | number, filterValues: any) {
    const url = this.baseUrl + '/device/page?_type=active&_page=' + currentPage + '&_limit=' + limit + '&_sort=' + sort + '&_order=' + order;
    return this.http.post<any>(url, filterValues);
  }

  gatLatestDeviceReportCountWithPage(currentPage: number, sort: string, order: string, limit: string | number, filterValues: any) {
    const url = this.baseUrl + '/device/report/page?_type=active&_page=' + currentPage + '&_limit=' + limit + '&_sort=' + sort + '&_order=' + order;
    return this.http.post<any>(url, filterValues);
  }

  gatReportCountWithPage(currentPage: number, sort: string, order: string, limit: string | number, filterValues: any) {
    const url = this.baseUrl + '/device/report/count?_type=active&_page=' + currentPage + '&_limit=' + limit + '&_sort=' + sort + '&_order=' + order;
    return this.http.post<any>(url, filterValues);
  }


  getGatewayAndSensorListSortwithCompanyPage(currentPage: number, sort: string, order: string, limit: string | number, can: any, filterValues: any) {
    const url = this.baseUrl + '/device/device-list?_type=active&_page=' + currentPage + '&_limit=' + limit + '&_sort=' + sort + '&_order=' + order + '&can=' + can;
    return this.http.post<any>(url, filterValues);
  }


  // getUserList() {
  //   var url = this.baseUrl + '/gateway/getSuperAdmin';
  //   return this.http.get<any>(url);
  // }


  getUserList() {
    const url = this.baseUrl + '/asset/getSuperAdmin';
    return this.http.get<any>(url);
  }
  getGatewayDetails(currentPage: string) {
    const url = this.baseUrl + '/gateway/summary?_type=active&_limit=25&_page=' + currentPage;
    return this.http.get<any>(url);
  }

  getAllGatewayDetails(currentPage, _sort, _order, _limit, filterValues, _filterModelCountFilter) {
    const url = this.baseUrl + '/gateway/gatewaysummary?_type=active&_page=' + currentPage + '&_limit=' + _limit + '&_sort=' + _sort + '&_order=' + _order;
    return this.http.post<any>(url, filterValues);
  }

  deleteGatewayByIds(ids: string[]): Observable<any> {
    const url = this.baseUrl + '/gateways/' + ids;
    return this.http.delete(url);
  }

  bulkDeleteForGateways(uuidList: string[]) {
    return this.http.post<any>(this.baseUrl + '/gateway/bulkDelete', uuidList);
  }

  exportFilterDeviceList(limit: string | number, currentPage: string | number, sort: string, order: string, filterValues: any): Observable<any> {
    const url = this.baseUrl + '/gateway/exportToCsvWithFilterForGateway?_page=' + currentPage + '&_limit=' + limit + '&_sort=' + sort + '&_order=' + order;
    return this.http.post(url, filterValues, { responseType: 'blob' as 'blob' })
      .pipe(
        map((data: any) => {
          const blob = new Blob([data], {
            type: 'text/csv' // for excel
          });
          const link = document.createElement('a');
          link.href = window.URL.createObjectURL(blob);
          link.download = 'DevicesFiltered.csv';
          link.click();
          window.URL.revokeObjectURL(link.href);
        })
      );
  }

  getConfigurationDetails(imei: string) {
    const url = this.baseUrl + '/gateway/getConfiguration?imei=' + imei;
    return this.http.get<any>(url);
  }

  getAssetStatus(imei: string) {
    const url = this.baseUrl + '/gateway/assetsDetails?imei=' + imei;
    return this.http.get<any>(url);
  }


  getDeviceReports(deviceId: string, currentPage: string, sort: string, order: string, limit: string, filterValues: any, mainVoltagefilter: string) {
    const url = this.baseUrl + '/device/device-reports?_deviceid=' + deviceId + '&_page=' + currentPage + '&_limit=' + limit + '&_sort=' + sort + '&_order=' + order + '&_mvoltagefilter=' + mainVoltagefilter;
    return this.http.post<any>(url, filterValues);
  }

  getParsedReport(rawReport: string, format: string, type: string) {
    const url = this.baseUrl + '/device/parse-report?raw-report=' + rawReport + '&format=' + format + '&type=' + type;
    return this.http.get<any>(url);
  }

  getColumnDefs() {
    const url = this.baseUrl + '/device/report-columndefs';
    return this.http.get<any>(url);
  }

  getDeviceReportsElastic(currentPage: string, sort: string, order: string, limit: number, filter: any, deviceId: string) {
    const url = this.baseUrl + '/device/getDeviceFromElastic?from=' + currentPage + '&size=' + limit + '&sort=' + sort + '&order=' + order + '&deviceId=' + deviceId;
    // let url = 'http://100.24.214.108:7081/report/getDeviceFromElastic?from='+currentPage+'&size=' + limit + '&deviceId='+deviceId;
    // let url = 'http://100.24.214.108:7081/report/getDeviceFromElastic?from='+currentPage+'&size=' + limit + '&deviceId=015115004226907';
    return this.http.post<any>(url, filter);
  }

  getAllDeviceReportsElastic(currentPage: number, sort: string, order: string, limit: number, filter: any) {
    const url = this.baseUrl + '/device/getDeviceReportFromElastic?from=' + currentPage + '&size=' + limit + '&sort=' + sort + '&order=' + order;
    // let url = 'http://100.24.214.108:7081/report/getDeviceFromElastic?from='+currentPage+'&size=' + limit + '&deviceId='+deviceId;
    // let url = 'http://100.24.214.108:7081/report/getDeviceFromElastic?from='+currentPage+'&size=' + limit + '&deviceId=015115004226907';
    return this.http.post<any>(url, filter);
  }

  updateDeviceForwarding(forwarding: Forwarding) {
    const url = this.baseUrl + '/df/device-forwarding';
    return this.http.post<any>(url, forwarding);
  }
  // New FrontEnd getCustomer
  getAllCustomer(type: string | string[]): Observable<any> {

    const url = this.baseUrl + '/device/type/' + type;
    return this.http.get<any>(url);
  }

  getAllCustomerObject(type: string | string[], name?: any): Observable<any> {
    if (!name) {
      name = '';
    }
    const url = this.baseUrl + '/device/company/type/' + type + '?name=' + name;
    return this.http.get<any>(url);
  }

  getEventList() {
    const url = this.baseUrl + '/event';
    return this.http.get<any>(url);
  }

  getOrganisationList() {
    const url = this.baseUrl + '/organisation/getAll';
    return this.http.get<any>(url);
  }

  getDeviceForwardingList(deviceId: any) {
    const url = this.baseUrl + '/df/device_forwarding_imei/' + deviceId;
    return this.http.get<any>(url);
  }

  getAssetDeviceDetail(deviceId: any) {
    const url = this.baseUrl + '/asset-device/getAssetDeviceAssociation?device_id=' + deviceId;
    return this.http.get<any>(url);
  }

  addDeviceForwardingRule(deviceForwardingPayload: any) {
    const url = this.baseUrl + '/df/device-forwarding';
    return this.http.post<any>(url, deviceForwardingPayload);
  }

  // Campaign Call
  getDeviceCampaignConfiguration(deviceId: any) {
    const url = this.baseUrl + '/campaign/campaign-history/' + deviceId;
    return this.http.get<any>(url);
  }
  getDeviceCampaignByIMEI(deviceId: any) {
    const url = this.baseUrl + '/campaign/getDeviceCampaignHistoryByImei/' + deviceId;
 
    return this.http.get<any>(url);
  }
  getCampaignInstalledDevice(deviceId: any) {
    const url = this.baseUrl + '/campaign/ms-device/getCampaignInstalledDevice/' + deviceId;
    return this.http.get<any>(url);
  }

  getCurrentCampaign(deviceId: any) {
    const url = this.baseUrl + '/campaign/current-campaign/' + deviceId;
    return this.http.get<any>(url);
  }

  updateDevice(payload: any) {
    const url = this.baseUrl + '/device/update-asset-device';
    return this.http.put<any>(url, payload);
  }

  batchUpdateDevice(payload: any) {
    const url = this.baseUrl + '/device/batch-update-device';
    return this.http.put<any>(url, payload);
  }

  updateSensorMacAdderess(payLoad: any) {
    const url = this.baseUrl + '/sensor/update-sensor-details';
    return this.http.put<any>(url, payLoad);
  }

  refreshUpdatedSensorMacAdderess(payLoad: any) {
    const url = this.baseUrl + '/sensor/refresh-sensor-details';
    return this.http.post<any>(url, payLoad);
  }


  retrieveSensorDetails(payLoad: any) {
    const url = this.baseUrl + '/sensor/get-sensor-details';
    return this.http.post<any>(url, payLoad);
  }

  retrieveLatestSensorDetails(payLoad: any) {
    const url = this.baseUrl + '/sensor/get-latest-sensor-details';
    return this.http.post<any>(url, payLoad);
  }

  addDevice(payload: any) {
    const url = this.baseUrl + '/device';
    return this.http.post<any>(url, payload);
  }

  exportData(currentPage: number, sort: string, order: string, limit: number, filter: any, deviceId?: any) {
    if (!deviceId) {
      deviceId = '';
    }
    const url = this.baseUrl + '/device/exportDataFromCsv?from=' + currentPage + '&size=' + limit + '&sort=' + sort + '&order=' + order + '&deviceId=' + deviceId;
    // const url = this.baseUrl + '/device/csv';
    return this.http.post(url, filter, { responseType: 'blob' as 'blob' });

  }

  exportDataInLocal(currentPage: number, sort: string, order: string, limit: number, filter: any, deviceId?: any) {
    if (!deviceId) {
      deviceId = '';
    }
    const url = this.baseUrl + '/device/exportDataFromCsv?from=' + currentPage + '&size=' + limit + '&sort=' + sort + '&order=' + order + '&deviceId=' + deviceId;
    // const url = this.baseUrl + '/device/csv';
    return this.http.post(url, filter, { responseType: 'blob' as 'blob' })
      .pipe(
        map((data: any) => {
          const blob = new Blob([data], {
            type: 'text/csv' // for excel
          });
          const link = document.createElement('a');
          link.href = window.URL.createObjectURL(blob);
          link.download = 'DevicesFiltered.csv';
          link.click();
          window.URL.revokeObjectURL(link.href);
        })
      );
  }

  exportGatewayDetails(currentPage, _sort, _order, _limit, filterValues, _filterModelCountFilter) {
    const url = this.baseUrl + '/gateway/exportGatewaySummary?_type=active&_page=' + currentPage + '&_limit=' + _limit + '&_sort=' + _sort + '&_order=' + _order;

    return this.http.post(url, filterValues, { responseType: 'blob' as 'blob' })
      .pipe(
        map((data: any) => {
          const blob = new Blob([data], {
            type: 'text/csv' // for excel
          });
          const link = document.createElement('a');
          link.href = window.URL.createObjectURL(blob);
          link.download = 'DevicesSummaryFiltered.csv';
          link.click();
          window.URL.revokeObjectURL(link.href);
        })
      );
  }

  getDeviceForwardingFromRedis(imei: any) {
    const url = this.baseUrl + '/df/from/redis?imei=' + imei;
    return this.http.get<any>(url);
  }

  getDeviceDetailsBydeviceId(deviceId: any) {
    const url = this.baseUrl + '/device?device-id=' + deviceId;
    return this.http.get<any>(url);
  }

  getGroupDetailsBydeviceId(deviceId: any) {
    const url = this.baseUrl + '/device/group-details?imei=' + deviceId;
    return this.http.get<any>(url);
  }

}
