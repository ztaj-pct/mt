import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from 'src/environments/environment';
import { AddLocalUser } from '../models/addlocaluser.model';
import { User } from '../models/user.model';

@Injectable({
  providedIn: 'root'
})
export class AddUserService {

  user: User;
  navigateFromSettings;
  editFlag = false;
  type = 'All';

  addlocaluser: AddLocalUser = new AddLocalUser();
  constructor(private http: HttpClient) { }

  private baseUrl = environment.baseUrl;
  private UserUrl = this.baseUrl + '/user';
  private UserUrl2 = this.UserUrl + '/username-availability?username=';

  private UserUrl1 = this.baseUrl + '/api/getAllRoles';



  public addUser(addlocaluser) {
    return this.http.post<any>(this.UserUrl, addlocaluser);
  }

  public findByUsername(username) {
    let url = this.UserUrl + '/username-availability?username=' + username;
    return this.http.post<any>(url,  username);
}

public createUser(AddLocalUser) {
  return this.http.post<any>(this.UserUrl, AddLocalUser);
}

public updateUser(AddLocalUser) {
  return this.http.put<any>(this.UserUrl, AddLocalUser);
}

getUserById(user_uuid: number) {
  return this.http.get<any>(this.UserUrl + '/' + user_uuid);
}

getUserByUserName(user_name: string) {
  return this.http.get<any>(this.UserUrl + '/username?user_name=' + user_name);
}

getAllCompanies() {
this.type = 'All';
return this.http.get<any>(this.baseUrl + '/customer/type?type=' + this.type);
}

getAllRoleName() {
  return this.http.get<any>(this.baseUrl + '/api/getAllRoleName');
}

getLocationsByOrgId(orgId: number) {
  return this.http.get<any>(this.baseUrl + '/location/get-locations?organisationId=' + orgId);
}

getUserByUserId(userId: number) {
  return this.http.get<any>(this.UserUrl + '/get/' + userId);
}


}


