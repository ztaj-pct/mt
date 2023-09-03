import { Injectable } from '@angular/core';
import { environment } from 'src/environments/environment';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class CompanyFormServiceService {

  constructor(private http: HttpClient) { }
  private baseUrl = environment.baseUrl;
  private UserUrl = this.baseUrl + '/api';

  getResellerByCompanyId(companyID:number) :Observable<any>{
      return this.http.get<any>(this.UserUrl + "/companies/" + companyID );
  }
}
