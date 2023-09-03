import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { FormBuilder, FormGroup, Validators, FormControl } from '@angular/forms';
import { ResetPasswordservice } from './reset-password.component.service';
import { ToastrService } from 'ngx-toastr';
import { CustomSuccessToastrComponent } from '../custom-success-toastr/custom-success-toastr.component';
import { LoginService } from '../login/login.service';
declare var $: any;

@Component({
  selector: 'ResetPassword',
  templateUrl: './reset-password.component.html',
  styleUrls: ['./reset-password.component.css']
})
export class ResetPasswordComponent implements OnInit {
  resetForm: FormGroup;
  passwordReset = false;
  invalidpassword = false;
  submitted = false;
  emailValidator = "[a-zA-Z0-9._%+-]+@[a-z0-9.-]+\.[a-z]{2,3}$";
  passwordValidator = "^[a-zA-Z0-9.!@#$%^&*()?_+=<>,.~{}|\\-\\[\\]\\\\]+$";
  spaceValidator = "^[-a-zA-Z0-9-()]+([-a-zA-Z0-9-() ]+)*";
  naviagetUrlObj: any;
  constructor(private ResertPasswordservice: ResetPasswordservice, private router: Router, private _formBuldier: FormBuilder, private toastr: ToastrService, private loginService: LoginService) {
    this.naviagetUrlObj = JSON.parse(sessionStorage.getItem('landingPageValue'));

    this.router.events.subscribe(path => {
      if (sessionStorage.getItem('Password_Change') != null && sessionStorage.getItem('Password_Change') == 'true') {
        this.router.navigate(['reset-password']);
      }
    });
  }


  ngOnInit() {

    this.resetForm = this._formBuldier.group({
      oldPassword: ['', Validators.compose([Validators.required, Validators.minLength(5)])],
      newPassword: ['', Validators.compose([Validators.required, Validators.minLength(5), Validators.pattern(this.passwordValidator)])],
      confirmPassword: ['', Validators.required],
    },
      {
        validator: [MustMatch('newPassword', 'confirmPassword')]

      },
    );
  }




  cancel() {
    this.resetForm.reset();
    $(".point").css('background-color', '#DDD');
    this.router.navigate([this.loginService.beforeResetPasswordUrl]);
  }

  get getForm() { return this.resetForm.controls; }

  onSubmit(resetForm) {
    this.submitted = true;
    if (!resetForm.invalid) {
      console.log(resetForm);
      this.ResertPasswordservice.forgotpassword.oldpassword = resetForm.value.oldPassword;
      this.ResertPasswordservice.forgotpassword.newpassword = resetForm.value.newPassword;
      this.ResertPasswordservice.resetPassword(this.ResertPasswordservice.forgotpassword).subscribe(data => {
        console.log(data);
        sessionStorage.setItem('Password_Change', 'false');
        this.toastr.success("Password Reset Successful", null, { toastComponent: CustomSuccessToastrComponent });
        this.passwordReset = true;
        this.invalidpassword = false;
        this.submitted = false;
        this.resetForm.reset();
        if (this.naviagetUrlObj && this.naviagetUrlObj.url) {
          this.router.navigate([this.naviagetUrlObj.url]);
        } else {
          this.router.navigate(['gateway-list']);
        }
      }, error => {
        this.invalidpassword = true;
        this.passwordReset = false;
        this.resetForm.reset();
        this.submitted = false;
        console.log(error);
      });
    }
  }

}

export function MustMatch(controlName: string, matchingControlName: string) {
  return (formGroup: FormGroup) => {
    const control = formGroup.controls[controlName];
    const matchingControl = formGroup.controls[matchingControlName];

    if (matchingControl.errors && !matchingControl.errors.mustMatch) {
      // return if another validator has already found an error on the matchingControl
      return;
    }
    // set error on matchingControl if validation fails
    if (control.value !== matchingControl.value) {
      matchingControl.setErrors({ mustMatch: true });
    } else {
      matchingControl.setErrors(null);
    }
  }



}

