import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import { CreateCompanyDTO, AddCompanyDTO } from './models/createCompanyDTO.model';

@Injectable({
  providedIn: 'root'
})
export class CompaniesService {
  private baseUrl = environment.baseUrl;

  company: any;
  editFlag = false;
  constructor(private http: HttpClient) { }

  getAllCompanies(): Observable<any> {
    const url = this.baseUrl + '/company/customer/hub';
    return this.http.get<any>(url);
  }
  createCompany(company: CreateCompanyDTO) {
    return this.http.post(this.baseUrl + '/company', company);
  }
  getActiveCompanies(currentPage, pageLimit) {
    const url = this.baseUrl + '/company?_limit=' + pageLimit + '&_page=' + currentPage + '&_type=ALL';
    return this.http.get<any>(url);
  }
  getActiveInstaller() {
    const url = this.baseUrl + '/company/installer';
    return this.http.get<any>(url);
  }
  getCompanyList() {
    const url = this.baseUrl + '/company/companylist';
    return this.http.get<String[]>(url);
  }

  updateCompany(company: CreateCompanyDTO) {
    return this.http.put(this.baseUrl + '/company', company);
  }

  softDeleteCompanyByUuid(uuid: string) {
    return this.http.delete(this.baseUrl + '/organisation?uuid=' + uuid);
  }
  findCompanyById(id: number) {
    return this.http.get<any>(this.baseUrl + '/organisation/id?id=' + id);
  }

  findCompanyByAccount(id: any) {
    return this.http.get<any>(this.baseUrl + '/organisation/byAccount?id=' + id);
  }

  getCompanies(type: String): Observable<any> {
    return this.http.get(this.baseUrl + '/company?_type=' + type);
  }

  getCompaniesWithSort(currentPage, _sort, _order, filterValues, _limit) {
    const url = this.baseUrl + '/organisation/page?_type=ALL&_page=' + currentPage + '&_limit=' + _limit + '&_sort=' + _sort + '&_order=' + _order;
    return this.http.post<any>(url, filterValues);
  }

  getAllCompaniesWithSort(currentPage, column, order, pageLimit) {
    const url = this.baseUrl + '/company?_type=ALL&_limit=' + pageLimit + '&_page=' + currentPage + '&_sort=' + column + '&_order=' + order;
    return this.http.get<any>(url);
  }
  getAllCompany(type,isActive): Observable<any> {
    
    let url = this.baseUrl + "/organisation/getAllOrganisationFilter?_type="+type+"&_status="+isActive;
    return this.http.get<any>(url);
  }
  getAllCompanyListObjByType(type: any, isActive?: boolean): Observable<any> {
    const url = this.baseUrl + '/device/company/type/' + type;
    return this.http.get<any>(url);
  }

  getAllCompanyList(): Observable<any> {
    const url = this.baseUrl + '/organisation/getAll';
    return this.http.get<any>(url);
  }
  getAllOrgList(): Observable<any> {
    const url = this.baseUrl + '/organisation/getAllOrg';
    return this.http.get<any>(url);
  }

  addCompanyFleet(company: any) {
    return this.http.post(this.baseUrl + '/organisation', company);
  }

  getAllCompanyName(): Observable<any> {
    const url = this.baseUrl + '/organisation/getAllCompanyName';
    return this.http.get<any>(url);
  }

  createOrganisation(local): Observable<any> {
    return this.http.post(this.baseUrl + '/organisation', local);
  }

  getAllActiveByOrganisationRoles(organisationRole: string[]) {
    return this.http.get<any>(this.baseUrl + '/organisation/getAllActiveByOrganisationRoles?organisationRoles=' + organisationRole);
  }

  getAllCustomerForwardingGroup() {
    return this.http.get<any>(this.baseUrl + '/customer/forwarding/groups/all' );
  }

  getAllCustomerForwardingGroupName() {
    return this.http.get<any>(this.baseUrl + '/customer/forwarding/groups/name/all' );
  }

  getCustomerForwardingGroupByOrganizationUuid(uuid: string) {
    return this.http.get<any>(this.baseUrl + '/customer/forwarding/groups?organizationUuid=' + uuid);
  }

  getCustomerForwardingRulesByOrganizationUuids(uuid: string[]) {
    return this.http.get<any>(this.baseUrl + '/customer/forwarding/rules?organizationUuids=' + uuid);
  }

  getAllCustomerForwardingRuleUrl() {
    return this.http.get<any>(this.baseUrl + '/customer/forwarding/rule/urls' );
  }

  getIgnoreRuleImeisByCustomerAccountNumber(accountNumber: string) {
    return this.http.get<any>(this.baseUrl + '/df/ignore/rules/imei?customerAccountNumber=' + accountNumber);
  }

  getLocationsByOrgId(orgId: number) {
    return this.http.get<any>(this.baseUrl + '/location/get-locations?organisationId=' + orgId);
  }

  getIgnoreRulesUsingDeviceImei(imei: string) {
    return this.http.get<any>(this.baseUrl + '/df/ignore/rules?imei=' + imei);
  }

  createOrganisationLocation(local) {
    return this.http.post(this.baseUrl + '/location', local);
  }

  deleteOrganisationLocation(locationId) {
    return this.http.delete(this.baseUrl + '/location?locationId='+locationId);
  }

  updateOrganisationLocation(locationObj:any) {
    return this.http.put(this.baseUrl + '/location',locationObj);
  }

}
