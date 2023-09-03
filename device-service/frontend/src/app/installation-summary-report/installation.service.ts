import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from 'src/environments/environment';
import { catchError, map } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class InstallationService {
  deviceUUid: string;
  private baseUrl = environment.baseUrl;

  constructor(private http: HttpClient) { }


  getInstallationWithSort(currentPage, column, order, companyUuid, filterValues, pageLimit, days) {
    const url = this.baseUrl + '/installation/page?_type=active&_limit=' + pageLimit + '&_page=' + currentPage + '&_sort=' + column + '&_order=' + order + '&_companyUuid=' + companyUuid + '&_days=' + days;
    return this.http.post<any>(url, filterValues);
  }

  getInstallationDetails(deviceUUid) {
    const url = this.baseUrl + '/installation/insallation-details?deviceUUid=' + deviceUUid;
    return this.http.get<any>(url);
  }

  exportExcelForInstallationSummary(currentPage, column, order, companyUuid, filterValues, pageLimit, days) {
    const url = this.baseUrl + '/installation/exportExcelForInstallationSummary?_type=active&_limit=' + pageLimit + '&_page=' + currentPage + '&_sort=' + column + '&_order=' + order + '&_companyUuid=' + companyUuid + '&_days=' + days;
    return this.http.post(url, filterValues, { responseType: 'blob' as 'blob' })
      .pipe(
        map((data: any) => {
          const blob = new Blob([data], {
            type: 'text/csv' // for excel
          });
          const link = document.createElement('a');
          link.href = window.URL.createObjectURL(blob);
          link.download = 'Installation Summary Report.xlsx';
          link.click();
          window.URL.revokeObjectURL(link.href);
        })
      );
  }

  getMaintenanceDetailByUuidList(uuidList: any) {
    const url = this.baseUrl + '/maintenance-report/getDetailByUuid';
    return this.http.post<any>(url, uuidList);
  }


}
