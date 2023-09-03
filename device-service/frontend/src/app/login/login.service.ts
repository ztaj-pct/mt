import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { FormGroup } from '@angular/forms';
import { Login } from '../models/login.model';
import { environment } from 'src/environments/environment';

@Injectable({
    providedIn: 'root'
})


export class LoginService {
    loginUser: Login = new Login();
    loginForm: FormGroup;
    beforeLoginUrl;
    beforeResetPasswordUrl;
    beforeAssetCancelUrl: string;
    constructor(private http: HttpClient) { }

    private baseUrl = environment.baseUrl;
    private userLogin = this.baseUrl + '/user/token/login';
    private userLogin2 = environment.IabaseUrl + '/user/token/login';
    private iaAzureLoginUrl = environment.IabaseUrl + '/azure/login';

    public login(user) {
        return this.http.post<any>(this.userLogin, user);
    }

    public login2(user) {
        return this.http.post<any>(this.userLogin2, user);
    }

    public ialoginUsingAzure(data) {
        return this.http.post<any>(this.iaAzureLoginUrl, data);
    }
}
