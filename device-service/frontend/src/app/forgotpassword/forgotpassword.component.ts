import { Component, OnInit } from '@angular/core';
import { ForGotPasswordservice } from './forgotpassword.component.service';
import { FormBuilder, Validators, FormGroup } from '@angular/forms';
import { ToastrService } from 'ngx-toastr';
import { ValidatorUtils, MustMatch } from '../util/validatorUtils';
import { CustomSuccessToastrComponent } from '../custom-success-toastr/custom-success-toastr.component';
import { CustomErrorToastrComponent } from '../custom-error-toastr/custom-error-toastr.component';
import { Router } from '@angular/router';

@Component({
    selector: 'forgot-password',
    templateUrl: './forgotpassword.component.html',
    styleUrls: ['./forgotpassword.component.css']
})
export class ForGotPasswordComponent implements OnInit {
    forgotpasswordForm: FormGroup;
    invalidEmail = false;
    submitted = false;
    mailsent = false;
    loading = false;
    passwordSuccess = false;
    validatorUtil: ValidatorUtils = new ValidatorUtils();
    emailValidator = '[a-zA-Z0-9._%+-]+@[a-z0-9.-]+\.[a-z]{2,3}$';
    constructor(private forgotpasswoedService: ForGotPasswordservice,    private readonly router: Router,
        private _formBuldier: FormBuilder, private toastr: ToastrService) { }
    ngOnInit() {
        this.forgotpasswordForm = this._formBuldier.group({
            email: ['', Validators.compose([Validators.required, Validators.email, Validators.minLength(4), Validators.pattern(this.validatorUtil.emailValidator)])],
        },
        );
    }
    get getForm() { return this.forgotpasswordForm.controls; }

    onSubmit(form) {
        console.log(form);
        this.loading = true;

        this.submitted = true;
        console.log(form.invalid);
        if (!form.invalid) {
            this.loading = true;
            this.forgotpasswoedService.restPasswordByAdmin(form.value.email).subscribe(data => {
                this.loading = false;
                if (data.status) {
                    this.toastr.success('New Password sent to the registered Email', null, { toastComponent: CustomSuccessToastrComponent });
                    this.passwordSuccess = true;
                } else {
                    this.toastr.error(data.message, null, { toastComponent: CustomErrorToastrComponent });
                }
                this.mailsent = true;
                this.forgotpasswordForm.reset();
                this.submitted = false;
                this.invalidEmail = false;

            }, error => {
                this.loading = false;
                console.log(error);
                this.invalidEmail = true;
                if (error && error.message) {
                    this.toastr.error(error.message, null, { toastComponent: CustomErrorToastrComponent });
                }
            });

        }
    }

    gotoLoginPage(){
      this.router.navigate(['login']);
    }
}
