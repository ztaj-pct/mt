import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from 'src/environments/environment';

import { ResetPassword } from '../models/resetPassword.model';

@Injectable({
    providedIn: 'root'
})
export class ResetPasswordservice {
    forgotpassword: ResetPassword = new ResetPassword();
    constructor(private http: HttpClient) { }
    private baseUrl = environment.baseUrl;
    private UserUrl = this.baseUrl + '/user';
    public resetPassword(forgotpassword) {
        return this.http.put<any>(this.UserUrl + "/update-password/admin", forgotpassword);
    }

}