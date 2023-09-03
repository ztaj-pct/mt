import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';
import { environment } from 'src/environments/environment';
import { Login } from '../models/login.model';


@Injectable({
  providedIn: 'root'
})
export class LoginService {

  loginUser: Login = new Login();


  beforeLoginUrl:any;
  beforeResetPasswordUrl:any;
  beforeAssetCancelUrl: string="";
  constructor(private http: HttpClient) { }

  private baseUrl = environment.baseUrl;
  private userLogin = this.baseUrl + '/user/token/login';

  public login(User:any) {
      return this.http.post<any>(this.userLogin, User)
  }
}
