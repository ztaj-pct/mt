import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { environment } from '../../environments/environment';

const httpOptions = {
    headers: new HttpHeaders({
        'Content-Type': 'application/json'
    })

};

@Injectable({
    providedIn: 'root'
})
export class ForGotPasswordservice {

    constructor(private http: HttpClient) { }
    private baseUrl = environment.baseUrl;
    private userLogin = this.baseUrl + '/user';

    public forGotPassword(email) {
        return this.http.post<any>(this.userLogin + "/forgot-password/" + email, httpOptions)
    }

    public restPasswordByAdmin(userEmail){
        return this.http.post<any>(this.userLogin + "/reset-password/admin/" + userEmail, httpOptions)
 }
}