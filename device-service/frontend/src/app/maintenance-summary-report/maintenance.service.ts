import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from 'src/environments/environment';

@Injectable({
  providedIn: 'root'
})
export class MaintenanceService {
  assetId: string;
  private baseUrl = environment.baseUrl;

  constructor(private http: HttpClient) { }

  getMaintenanceWithSort(currentPage, column, order, companyUuid, filterValues, pageLimit, days: number, isUuid: boolean) {
    const url = this.baseUrl + '/maintenance-report/page?_type=active&_limit=' + pageLimit + '&_page=' + currentPage + '&_sort=' + column + '&_order=' + order + '&_companyUuid=' + companyUuid + '&_days=' + days + '&_isUuid=' + isUuid;
    return this.http.post<any>(url, filterValues);
  }

}
