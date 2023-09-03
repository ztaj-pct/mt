import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpHeaders, HttpResponse } from '@angular/common/http';
import { environment } from 'src/environments/environment';
import { Login } from './models/login.model';
import { DeviceKeyExist } from './models/deviceKeyExist';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { generateSignedCommand } from './models/generateSignedCommand';

@Injectable({
  providedIn: 'root'
})

export class DeviceSecurityService {
  loginUser: Login = new Login();
  devicekeyRequestModel :  DeviceKeyExist =  new DeviceKeyExist();
  reportSigningEnabled: Boolean;
  responseVerificationEnabled : Boolean;
  isSuperAdmin: any;
  deviceId: string;
  constructor(private http: HttpClient) { }
  private baseUrl = environment.deviceSecuritySettingsUrl;


  public login() {
    this.loginUser.username = 'pctinuse';
    this.loginUser.password = 'password';
    return this.http.post<any>(this.baseUrl+'/authenticate',this.loginUser);
}

public getDeviceKeyDetail(device_id: String)
{
  const token = sessionStorage.getItem('jwt_token');
  console.log(token)
  const httpOptions = {
      headers: new HttpHeaders({
          Authorization: 'Bearer ' + token,
      })
  };
  // return this.http.get<any>(this.baseUrl + '/device/' + device_id,httpOptions);
  return this.http.get<any>(this.baseUrl + '/device/' + device_id,httpOptions); // testing purpose
}

public updateDeviceKeyRequest(device_id: String,enabledKey:any)
{
  console.log("enabled Key")
  console.log(enabledKey)
  const token = sessionStorage.getItem('jwt_token');
  console.log(token)
  type bodyType = 'body';

  const httpOptions = {
      headers: new HttpHeaders({
          Authorization: 'Bearer ' + token,
      }),
      observe: <bodyType>'response'
  };
  // return this.http.get<any>(this.baseUrl + '/device/' + device_id,httpOptions);
  return this.http.put<any>(this.baseUrl + '/device/' + device_id + '/',enabledKey,httpOptions)
  .pipe(map(data => {
    return data.status
      }));
  // .subscribe((response: HttpResponse<any>) => {
  //   console.log(response.status); 
  //   });
}

public getAlertDetail(device_id: String)
{
  const token = sessionStorage.getItem('jwt_token');
  console.log(token)
  const httpOptions = {
      headers: new HttpHeaders({
          Authorization: 'Bearer ' + token,
      })
  };
  // return this.http.get<any>(this.baseUrl + '/device/' + device_id,httpOptions);
  return this.http.get<any>(this.baseUrl + '/device/'+device_id+'/alert',httpOptions)
  ; // testing purpose
}

public sendSignedCommandRequest(generateCommandModel: generateSignedCommand)
{
  const token = sessionStorage.getItem('jwt_token');
  console.log(token)
  const httpOptions = {
      headers: new HttpHeaders({
          Authorization: 'Bearer ' + token,
      }),
  };
  return this.http.post<any>(this.baseUrl + '/device/',generateCommandModel,httpOptions);// testing purpose
}

}
