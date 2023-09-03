import { Injectable } from '@angular/core';
import { HttpRequest, HttpHandler, HttpEvent, HttpInterceptor } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { CustomErrorToastrComponent } from '../custom-error-toastr/custom-error-toastr.component';
import { CustomBatchErrorToastrComponent } from '../custom-batch-error-toastr/custom-batch-error-toastr.component';
import { environment } from 'src/environments/environment';

@Injectable()
export class ErrorInterceptor implements HttpInterceptor {
    errorTitle;
    private baseUrl = environment.baseUrl;
    constructor(private route: Router, private toastr: ToastrService) {

    }

    intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
        return next.handle(request).pipe(catchError(err => {

            const token = sessionStorage.getItem('token');
            if (!token) {
                this.route.navigate(['login']);
                return throwError(err.error);
            }

            if (err.status === 400 && err.error.body != null && err.error.body.errors != null) {
                const errorTitleArray = Object.keys(err.error.body.errors);
                console.log(errorTitleArray);
                let totalErrorDevices = 0;
                let finalErrorString = '';
                for (const iterator of errorTitleArray) {
                    finalErrorString += '<br><b>' + iterator + '</b>';
                    finalErrorString += '<br><ul>';
                    totalErrorDevices++;
                }

                const finalErrorTitle = totalErrorDevices + ' Errors';
                this.toastr.error(finalErrorString, finalErrorTitle, { toastComponent: CustomBatchErrorToastrComponent, tapToDismiss: true, disableTimeOut: true, enableHtml: true });
                return throwError(err.error);
            }
            if (err.url.includes('/token/login') && err.status === 401) {
                console.log('inside error path');
                if (err.url === 'https://api.phillips-connect.com/user/token/login') {
                    return throwError(err.error);
                } else {
                    this.logout();
                    return throwError(err.error);
                }
            }
            if (err.status === 401) {
                if (err.error.path === '/user') {
                    sessionStorage.clear();
                    // this.route.navigate(['login']);
                    this.toastr.error('Your session has expired. Please login again.', null, { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
                    this.logout();
                } else {
                    if (err.error instanceof Blob) {
                        this.toastr.error("you don't have permission to access the requested resource", null, { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
                    } else {
                        this.toastr.error(err.error instanceof Object ? err.error.error : err.error, null, { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
                    }
                }
                return throwError(err.error);
            }
            if (err.status === 500) {
                if (err.error.code === 501) {
                    return throwError(err.error);
                }
            }

            if (err.error.message === undefined) {
                if (err.error.isAtCommandSigned === false) {
                    return throwError(err.error);
                }
                if (err.error.alert === false) {
                    return throwError(err.error);
                }
                if (err.error.size !== undefined) {
                    return throwError(err.error);
                }

                if (err.error.message === undefined) {
                    if (err.url.includes('https://device-signature.phillips-connect.com') && err.url.includes('/alert') && err.status == 0) {
                        return throwError(err.error);
                    }
                    this.toastr.error('Internal Server Error Please try again', 'Internal Server Error', { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
                    return throwError(err.error);
                }

                let errors = 'Internal server error';
                if (err.error instanceof Object) {
                    errors = err.error.message ? err.error.message : 'Internal server error';
                }
                this.toastr.error(errors, null, { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
                return throwError(err.error);
            }
            if (err.error.title === undefined) {
                this.errorTitle = 'Error';
            } else {
                this.errorTitle = err.error.title;
            }

            if (err != null && err.error != null && err.error.body != null &&
                err.error.body.length > 0 && err.error.message != null &&
                (err.error.message === 'The following IMEI values are invalid. IMEIs must be numeric and 15 characters long' ||
                    err.error.message === 'The following IMEI values were not found on the maintenance server and will not be added to the list for this campaign.')) {
                let finalErrorStringFirst = err.error.message;
                for (const iterator of err.error.body) {
                    finalErrorStringFirst += '<br><b>' + iterator + '</b>';
                    // finalErrorString += "<br><ul>";
                }

                const finalErrorTitleFirst = err.error.body.length + ' Errors';
                this.toastr.error(finalErrorStringFirst, finalErrorTitleFirst, { toastComponent: CustomBatchErrorToastrComponent, tapToDismiss: true, disableTimeOut: true, enableHtml: true });
                return throwError(err.error);
            }
            const error = err.error.message;
            if (err.error.body != null) {
                const msg = err.error.body;
                this.toastr.error(err.error.message, msg, { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
                return throwError(error);
            }
            if (err.error.message.includes('already queued')) {
                err.error.message = 'AT Command is already in queued';
            } else if (err.error.message.includes('Unexpected end of stream.')) {
                err.error.message = 'Unexpected end of stream.';
                return throwError(error);
            } else if (err.error.message.includes('Exception:-403')) {
                return throwError(error);
            } else if (err.error.message.includes('atc-request') && err.error.message.includes('Read timed out')) {
                return throwError(error);
            }
            this.toastr.error(err.error.message, this.errorTitle, { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
            return throwError(error);
        }));
    }

    logout() {
        const token = sessionStorage.getItem('token');
        sessionStorage.clear();
        let url = this.baseUrl + "/app/logout?Authorization=Bearer " + token;
        var link = document.createElement('a');
        link.href = url;
        document.body.appendChild(link);
        link.click();
    }
}
