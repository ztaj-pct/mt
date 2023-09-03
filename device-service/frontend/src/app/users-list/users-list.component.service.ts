import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from 'src/environments/environment';
import { AddLocalUser } from '../models/addlocaluser.model';

@Injectable({
  providedIn: 'root'
})
export class UsersListService {

  addlocaluser: AddLocalUser = new AddLocalUser();
  constructor(private http: HttpClient) { }
  private baseUrl = environment.baseUrl;
  private UserUrl = this.baseUrl + '/user';

  getInfo = function () {
    this.any.navigateByUrl('/getInfo');
  };

  deleteUser(id: number) {
    return this.http.delete(this.UserUrl + '/' + id);
  }

  getActiveUsers(currentPage, pageLimit) {
    const url = this.baseUrl + '/user?_type=active&_limit=' + pageLimit + '&_page=' + currentPage;
    return this.http.get<any>(url);
  }

  getActiveUsersWithSort(currentPage, column, order, pageLimit) {
    const url = this.baseUrl + '/user?_type=active&_limit=' + pageLimit + '&_page=' + currentPage + '&_sort=' + column + '&_order=' + order;
    return this.http.get<any>(url);
  }

  getUsersWithSort(currentPage, sort, order, filterValues, limit) {
    const url = this.baseUrl + '/user/page?_type=active&_page=' + currentPage + '&_limit=' + limit + '&_sort=' + sort + '&_order=' + order;
    return this.http.post<any>(url, filterValues);
  }
  updateUser(user) {
    return this.http.put(this.UserUrl, user);
  }
  getUserById(id: number) {
    return this.http.get<any>(this.UserUrl + '/get/' + id);
  }

  inActiveUserById(id: any) {
    return this.http.delete<any>(this.UserUrl + '/' + id);
  }

  getAllUsers() {
    const url = this.baseUrl + '/user/all';
    return this.http.get<any>(url);
  }
}
