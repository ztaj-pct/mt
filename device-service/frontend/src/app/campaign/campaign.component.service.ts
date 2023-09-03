import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { Device } from '../models/device.model';
import { Package } from '../models/package.model';
import { Campaign } from '../models/campaign.model';
import { Observable } from 'rxjs';
import { catchError, map } from 'rxjs/operators';

@Injectable({
    providedIn: 'root'
})
export class CampaignService {

    packageDetail: Package;
    campaignDetail: Campaign;
    packageId: any;
    editFlag = false;
    campaignId;
    filterValuesJsonObj: any = {};

    constructor(private http: HttpClient) { }
    private baseUrl = environment.baseUrl;
    private packageUrl = this.baseUrl + '/package';
    private campaignUrl = this.baseUrl + '/campaign';
    private msDevice = this.baseUrl + '/campaign/ms-device';
    private msDeviceV2 = this.baseUrl + '/campaign/ms-device/v2';

    private campaignExecutionUrl = this.baseUrl + '/campaign/execution';
    private configLookup = this.baseUrl + '/config-lookup';
    private msDesiveServices = this.baseUrl + '/device/core/ms-device/';
    private campaignUrlDeviceCampaignStatus = this.baseUrl + '/campaign/v2/device-campaign-status';

    updatePackage(packageObj) {
        return this.http.put<any>(this.packageUrl, packageObj);
    }

    exportGatewayList(campaignUuid, campaignName): Observable<any> {
        let url = this.campaignUrl + "/v1/exportGatewayList/" + campaignUuid;
        return this.http.get(url , {responseType: "blob" as "blob"})
        .pipe(
          map((data: any) => {
            let blob = new Blob([data], {
              type: "text/csv" // for excel
            });
            var link = document.createElement("a");
            link.href = window.URL.createObjectURL(blob);
            link.download = campaignName + "_gateways.xlsx";
            link.click();
            window.URL.revokeObjectURL(link.href);
          })
        );
      }

      exportPackageList(): Observable<any> {
        let url = this.packageUrl + "/v1/exportToCsv";
        return this.http.get(url , {responseType: "blob" as "blob"})
        .pipe(
          map((data: any) => {
            let blob = new Blob([data], {
              type: "text/csv" // for excel
            });
            var link = document.createElement("a");
            link.href = window.URL.createObjectURL(blob);
            link.download = "packages.csv";
            link.click();
            window.URL.revokeObjectURL(link.href);
          })
        );
      }

    // activateCampaign(campaignUuid) {
    //     return this.http.put<any>(this.campaignExecutionUrl + "/activate", campaignUuid);
    // }

    addPackage(packageObj) {
        return this.http.post<any>(this.packageUrl, packageObj);
    }

    addLookupValue(lookupObj) {
        return this.http.post<any>(this.configLookup, lookupObj);
    }

    // pauseCampaign(campaignUuid) {
    //     return this.http.put<any>(this.campaignExecutionUrl + "/pause", campaignUuid);
    // }


    changeCampaignStatus(campaignUuid, status, pauseLimit) {
      if (pauseLimit != null && pauseLimit != "" && pauseLimit != undefined) {
        return this.http.put<any>(this.packageUrl + "/manage-status?campaignUuid=" + campaignUuid + "&campaignStatus=" + status + "&pauseLimit=" + pauseLimit, null);
      }else{
        return this.http.put<any>(this.packageUrl + "/manage-status?campaignUuid=" + campaignUuid + "&campaignStatus=" + status, null);
      }
    }

    updateCampaign(campaignObj) {
        return this.http.put<any>(this.campaignUrl, campaignObj);
    }

    addCampaign(campaignObj) {
        return this.http.post<any>(this.campaignUrl, campaignObj);
    }

    public findByPackageId(uuid) {
        return this.http.get<any>(this.packageUrl + "/" + uuid);
    }

    public findByCampaignId(uuid) {
        console.log()
        return this.http.get<any>(this.campaignUrl + "/" + uuid +"/1");
    }

    public findDataByCustomer(customerName, basePackageUuid, campaignUuid) {
        if(campaignUuid != null) {
          return this.http.get<any>(this.msDevice + "/" + customerName + "?basePackageUuid=" + basePackageUuid + "&campaignUuid=" + campaignUuid);
        } else {
          return this.http.get<any>(this.msDevice + "/" + customerName + "?basePackageUuid=" + basePackageUuid);
        }
    }
    public findDataByCustomerPage(currentPage, _sort, _order, filterValues, _limit,customerName, basePackageUuid, campaignUuid) {
      if(campaignUuid != null) {
        return this.http.get<any>(this.msDeviceV2 + "/" + customerName + "?basePackageUuid=" + basePackageUuid + "&campaignUuid=" + campaignUuid+"_page=" + currentPage + "&_limit=" + _limit + "&_order=" + _order);
      } else {
        return this.http.get<any>(this.msDeviceV2 + "/" + customerName + "?basePackageUuid=" + basePackageUuid + "_page=" + currentPage + "&_limit=" + _limit +  "&_order=" + _order);
      }
  }
    findDataByImeiList(imeiList, basePackageUuid) {
        return this.http.post<any>(this.msDevice + "/getSelectedDevices" + "?basePackageUuid=" + basePackageUuid, imeiList);
    }
    findCampaignInstalledDevice(imei) {             
      let result =  this.http.get<any>(this.msDevice + "/getCampaignInstalledDevice/" +imei);
    return result;
     }

    getDeviceCampaignHistoryByImei(imei) {             
      let result =  this.http.get<any>(this.campaignUrl + "/getDeviceCampaignHistoryByImei/" +imei);
      return result;
  }

    getPackages(currentPage, _sort, _order, filterValues, _limit) {
        var url = this.packageUrl + "/getAll?_page=" + currentPage + "&_limit=" + _limit + "&_sort=" + _sort + "&_order=" + _order;
        return this.http.post<any>(url, filterValues);
    }

    exportFilterPackageList(_limit, currentPage,_sort, _order, filterValues): Observable<any> {
        let url = this.packageUrl + "/v1/exportToCsvFilter?_page=" + currentPage + "&_limit=" + _limit + "&_sort=" + _sort + "&_order=" + _order;
        return this.http.post(url , filterValues, {responseType: "blob" as "blob"})
        .pipe(
          map((data: any) => {
            let blob = new Blob([data], {
              type: "text/csv" // for excel
            });
            var link = document.createElement("a");
            link.href = window.URL.createObjectURL(blob);
            link.download = "packagesFiltered.csv";
            link.click();
            window.URL.revokeObjectURL(link.href);
          })
        );
      }

    /*getCampaigns(currentPage, _sort, _order, filterValues, _limit) {
        var url = this.campaignUrl + "/getAll?_page=" + currentPage + "&_limit=" + _limit + "&_sort=" + _sort + "&_order=" + _order;
        return this.http.post<any>(url, filterValues);
    }*/
    //http://localhost:8016/campaign/v1/fetchListCampaign
    getCampaigns(currentPage, _sort, _order, filterValues, _limit) {
      var url = this.campaignUrl + "/v1/fetchListCampaign?_page=" + currentPage + "&_limit=" + _limit + "&_sort=" + _sort + "&_order=" + _order;
      return this.http.post<any>(url, filterValues);
    }

    getLookupValues(type): Observable<any> {
        let url = this.baseUrl + "/config-lookup?type=" + type;
        return this.http.get(url);
    }

    deletePackage(uuid): Observable<any> {
        return this.http.delete(this.packageUrl + "/" + uuid);
    }

    deleteCampaign(uuid): Observable<any> {
        return this.http.delete(this.campaignUrl + "/" + uuid);
    }

    public findByPackageName(packageName) {
        return this.http.get<any>(this.packageUrl + "/check-packageName-exist/" + packageName);
    }

    public findByCampaignName(campaignName) {
        return this.http.get<any>(this.campaignUrl + "/check-campaignName-exist/" + campaignName);
    }

    public findBaseIsExistOrNot(basePackageUuid) {
        return this.http.get<any>(this.campaignUrl + "/check-baselinePackage-exist/" + basePackageUuid);
    }

    // getActiveCompanies(currentPage, pageLimit, type) {
    //     var url = this.baseUrl + "/company?_limit=" + pageLimit + "&_page=" + currentPage + "&_type=" + type;
    //     return this.http.get<any>(url);
    // }

    getActiveCompanies(currentPage, pageLimit, type) {

      var url = this.baseUrl + "/organisation/page?_limit=" + pageLimit + "&_page=" + currentPage + "&_type=" + type;
      return this.http.post<any>(url,this.filterValuesJsonObj);
  }
    



    
    fetchCampaignHistory(imei) {
      let result = this.http.get<any>(this.campaignUrl + "/campaign-history/" + imei);
      return result;
    }

    resetStatusOfProblemDevice(selectedDeviceIdForResetStatus) {
      let result = this.http.post<any>(this.campaignUrl + "/resetStatusOfProblemDevice",selectedDeviceIdForResetStatus);
      return result;
    }
    public getEmergencyStopFlagValue() {
      return this.http.get<any>(this.packageUrl + "/emergency-stop");
    }

    updateEmergencyStopFlagValue(isEmergencyStop) {
      return this.http.put<any>(this.packageUrl + "/emergency-stop?isEmergencyStop=" + isEmergencyStop, null);
  }
 
findDeviceCampaignStatusPageByCampaignId(currentPage, _sort, _order, filterValues, _limit,uuid) {
  var url = this.campaignUrlDeviceCampaignStatus + "/fetchAll?_page=" + currentPage + "&_limit=" + _limit + "&_sort=" + _sort + "&_order=" + _order + "&uuid=" +uuid;
  return this.http.post<any>(url, filterValues);
}
public findByCampaignUUID(uuid) {
   return this.http.get<any>(this.campaignUrl + "/" + uuid +"/2");
}
}
