import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from 'src/environments/environment';
import { AddLocalUser } from '../models/addlocaluser.model';
import { User } from '../models/user.model';

@Injectable({
  providedIn: 'root'
})
export class UserService {

  user: User = new User();
    navigateFromSettings:any;
    editFlag: boolean = false;

    addlocaluser: AddLocalUser = new AddLocalUser();
    constructor(private http: HttpClient) { }
    private baseUrl = environment.baseUrl;
    private UserUrl = this.baseUrl + '/user';

    public findByUsername(username:any) {
        return this.http.get<any>(this.UserUrl + "/username-availability/" + username);
    }

    public createUser(AddLocalUser:any) {
        return this.http.post<any>(this.UserUrl, AddLocalUser);
    }

    public updateUser(AddLocalUser:any) {
        return this.http.put<any>(this.UserUrl, AddLocalUser);
    }

    getUserById(user_uuid: number) {
        return this.http.get<any>(this.UserUrl + "/" + user_uuid);
    }


}
