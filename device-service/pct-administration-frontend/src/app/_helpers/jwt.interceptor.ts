import { Injectable } from '@angular/core';
import { HttpRequest, HttpHandler, HttpEvent, HttpInterceptor } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';


@Injectable()
export class JwtInterceptor implements HttpInterceptor {

    intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
      //  let token = "eyJhbGciOiJIUzUxMiJ9.eyJzY29wZXMiOlsiU3VwZXJhZG1pbiJdLCJzdWIiOjE5LCJjb21wYW55SWQiOjEsImlhdCI6MTYzOTM4ODQ3NiwiZXhwIjoxNjM5NDc0ODc2fQ.5iJxQXyLReNiZK3fwm74tiT3KKa18vsS6fBAyEyvBe_bLJ29DCvKoffJeux36Yw3rcONP3oTX_G0BUK1JwswwA";
        let token = sessionStorage.getItem('token');
        if(request != undefined && request != null && request.url != undefined && request.url != null) {
            if(request.url.includes(environment.baseUrl)) {
                if (token) {
                    request = request.clone({
                        setHeaders: {
                            Authorization: `Bearer ${token}`,
                             'Content-Type': 'application/json'
                        }
                    });
                }
            }
        }
        return next.handle(request);
    }
}