import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpHeaders } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { environment } from 'src/environments/environment';
import { Asset } from './models/asset';
import { catchError, map } from 'rxjs/operators';

const httpOptions = {
  headers: new HttpHeaders({
    'responseType': 'ResponseContentType.Blob'
  })
};
@Injectable({
  providedIn: 'root'
})
export class AssetsService {
  asset: Asset;
  routeflag = false;
  navigateFromSettings: any;
  editFlag = false;
  userCompanyType: any;
  assetCompanyId: any;
  isSuperAdmin: any;
  selectedIdsToDelete: string[] = [];
  organisationId: any;

  constructor(private http: HttpClient) { }
  private baseUrl = environment.baseUrl;

  addAssetList(assetList: any): Observable<any> {
    const url = this.baseUrl + '/asset/bulk';
    return this.http.post(url, assetList);
  }


  getAssetList(): Observable<any> {
    const url = this.baseUrl + '/asset';
    return this.http.get(url);
  }
  getSuperAdminList(): Observable<any> {
    const url = this.baseUrl + '/asset/getSuperAdmin';
    return this.http.get(url);
  }

  addAsset(asset: Asset): Observable<any> {
    const url = this.baseUrl + '/asset';
    return this.http.post(url, asset).pipe(catchError(this.handleError));
  }

  handleError(error: HttpErrorResponse) {
    return throwError(error);
  }
  updateAsset(asset: Asset): Observable<any> {
    const url = this.baseUrl + '/asset';
    return this.http.put(url, asset);
  }

  getUserById(id: number): Observable<any> {
    const url = this.baseUrl + '/asset/' + id;
    return this.http.get(url);
  }
  deleteassetById(ids: string[]): Observable<any> {
    const url = this.baseUrl + '/asset/delete';
    return this.http.post(url, ids);
  }

  downloadAsset(): Observable<Blob> {
    const url = this.baseUrl + '/asset/data-import-template';
    return this.http.get<Blob>(url, httpOptions);
  }

  // getDataImportTemplate(): Observable<Blob> {
  //   const options = new RequestOptions({responseType: ResponseContentType.Blob });
  //   return this.http.get(this.resourceUrl + '/data-import-template', options)
  //       .map((res) => res.blob());
  // }
  getAllAssets(currentPage, companyId) {
    this.isSuperAdmin = sessionStorage.getItem('IS_ROLE_SUPERADMIN');
    this.userCompanyType = sessionStorage.getItem('type');
    let url = this.baseUrl + '/asset/page?_type=active&_limit=25&_page=' + currentPage;
    if ((this.isSuperAdmin === 'true' || this.userCompanyType === 'Manufacturer') && companyId != null) {
      url = url + '&companyId= ' + companyId;
    }
    return this.http.get<any>(url);
  }



  getActiveAssetsWithSort(currentPage, column, order, companyId, filterValues, pageLimit, yearFilterType) {
    let url = this.baseUrl + '/asset/page?_type=active&_limit=' + pageLimit + '&_page=' + currentPage + '&_sort=' + column + '&_order=' + order + '&_yearFilter=' + yearFilterType;
    this.isSuperAdmin = sessionStorage.getItem('IS_ROLE_SUPERADMIN');
    const roleCallCenterUser = JSON.parse(sessionStorage.getItem('IS_ROLE_CALL_CENTER_USER'));
    const rolePCTUser = JSON.parse(sessionStorage.getItem('IS_ROLE_PCT_USER'));
    this.userCompanyType = sessionStorage.getItem('type');
    if ((this.isSuperAdmin === 'true' || roleCallCenterUser || rolePCTUser || this.userCompanyType === 'MANUFACTURER' || this.userCompanyType === 'INSTALLER') && companyId != null) {
      url = url + '&companyId=' + companyId;
    }
    return this.http.post<any>(url, filterValues);
  }

  getCustomerAssetsWithSort(currentPage, column, order, pageLimit) {
    const url = this.baseUrl + '/asset/summary?_type=active&_limit=' + pageLimit + '&_page=' + currentPage + '&_sort=' + column + '&_order=' + order;
    return this.http.get<any>(url);
  }

  getCustomerAssetsListWithSort(currentPage, _sort, _order, _limit, filterValues, _filterModelCountFilter) {
    const url = this.baseUrl + '/asset/summary?_type=active&_page=' + currentPage + '&_limit=' + _limit + '&_sort=' + _sort + '&_order=' + _order + '&_filterModelCountFilter=' + _filterModelCountFilter;
    return this.http.post<any>(url, filterValues);
  }

  // ---------------------------------------------------------------------------------------

  uploadGatewayList(companyGateway: any): Observable<any> {
    const url = this.baseUrl + '#';
    return this.http.post(url, companyGateway);
  }

  getCustomerGatewaySummaryWithSort(currentPage, _sort, _order, _limit, filterValues, _filterModelCountFilter) {
    const url = this.baseUrl + '/gateway/summary?_type=active&_page=' + currentPage + '&_limit=' + _limit + '&_sort=' + _sort + '&_order=' + _order;
    return this.http.get<any>(url);
  }

  // ---------------------------------------------------------------------------------------

  checkForOverwrite(companyId: number): Observable<any> {
    const url = this.baseUrl + '/asset/overwrite/' + companyId;
    return this.http.get(url);
  }

  reuploadModifiedAssets(assetList: any): Observable<any> {
    const url = this.baseUrl + '/asset/bulk';
    return this.http.put(url, assetList);
  }

  exportAssetList(companyId: number): Observable<any> {
    const url = this.baseUrl + '/asset/download/' + companyId;
    return this.http.get(url, { responseType: 'blob' as 'blob' })
      .pipe(
        map((data: any) => {
          const blob = new Blob([data], {
            type: 'text/csv' // for excel
          });
          const link = document.createElement('a');
          link.href = window.URL.createObjectURL(blob);
          link.download = 'AssetDetails.csv';
          link.click();
          window.URL.revokeObjectURL(link.href);
        })
      );
  }


  getActiveAssetsWithSortForDownload(currentPage, column, order, companyId, filterValues, pageLimit, yearFilterType) {
    let url = this.baseUrl + '/asset/downloadCSV?_type=active&_limit=' + pageLimit + '&_page=' + currentPage + '&_sort=' + column + '&_order=' + order + '&_yearFilter=' + yearFilterType;
    this.isSuperAdmin = sessionStorage.getItem('IS_ROLE_SUPERADMIN');
    this.userCompanyType = sessionStorage.getItem('type');
    if ((this.isSuperAdmin === 'true' || this.userCompanyType === 'MANUFACTURER') && companyId != null) {
      url = url + '&companyId= ' + companyId;
    }
    return this.http.post(url, filterValues, { responseType: 'blob' as 'blob' }).pipe(
      map((data: any) => {
        const blob = new Blob([data], {
          type: 'text/csv' // for excel
        });
        const link = document.createElement('a');
        link.href = window.URL.createObjectURL(blob);
        link.download = 'AssetDetails.csv';
        link.click();
        window.URL.revokeObjectURL(link.href);
      })
    );
  }

  getAssetTypeAndApprovedProduct(): Observable<any> {
    const type = ['APPROVED_PRODUCT', 'ASSET_TYPE_1'];
    const url = this.baseUrl + '/config-lookup?type=' + type;
    return this.http.get(url);
  }

  getUserList() {
    const url = this.baseUrl + '/asset/getSuperAdmin';
    return this.http.get<any>(url);
  }

  getProductList(): Observable<any> {
    const url = this.baseUrl + '/product/product-list';
    return this.http.get(url);
  }

  getAssetById(uuId: any): Observable<any> {
    const url = this.baseUrl + '/asset/getById?id=' + uuId;
    return this.http.get(url);
  }

  uploadAssetAssociation(payload: any): Observable<any> {
    const url = this.baseUrl + '/asset/asset_association';
    return this.http.post<any>(url, payload);
  }
}
