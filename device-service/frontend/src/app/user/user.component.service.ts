import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { environment } from 'src/environments/environment';
import { User } from '../models/user.model';
import { AddLocalUser } from '../models/addlocaluser.model';

@Injectable({
    providedIn: 'root'
})
export class UserService {

    user: User;
    navigateFromSettings;
    editFlag = false;

    addlocaluser: AddLocalUser = new AddLocalUser();
    constructor(private http: HttpClient) { }
    private baseUrl = environment.baseUrl;
    private UserUrl = this.baseUrl + '/user';
    private UserUrl2 = environment.IabaseUrl + '/user';

    public findByUsername(username) {
        return this.http.get<any>(this.UserUrl + '/username-availability/' + username);
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

    getUserById2(user_uuid: number) {
        const token = localStorage.getItem('user2_token');
        const httpOptions = {
            headers: new HttpHeaders({
                Authorization: 'Bearer ' + token,
            })
        };
        return this.http.get<any>(this.UserUrl2 + '/' + user_uuid, httpOptions);
    }

    getUserByUserName(user_name: string) {
        return this.http.get<any>(this.UserUrl + '/username?user_name=' + user_name);
    }


}
