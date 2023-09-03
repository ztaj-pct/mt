import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { LoginService } from './login.service';
import { ToastrService } from 'ngx-toastr';
import { UserService } from '../user/user.service';


@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {
 
  loginForm: FormGroup = new FormGroup({
  
})
  invaliduser = false;
  loading = false;
  loginError = false;
  isSuperAdmin:any;
  isRoleCustomerManager:any;
  isCustomerManagerInstaller : any;

  userRole:any;
  companies:any;

  constructor(private router: Router, private _formBuldier: FormBuilder, private route: ActivatedRoute, 
    private loginService: LoginService, private toastr: ToastrService,private userService:UserService) { 
      
    }

    ngOnInit() {
      this.loginForm = this._formBuldier.group({
        username: ['', [Validators.required, Validators.email]],
  
        password: ['', [Validators.required, Validators.minLength(5)]],
  
      });
  
    }
  
  get getLoginForm() { return this.loginForm.controls; }
  login(loginForm:any) {
    this.loginError = false;
    this.loginService.loginUser.username = loginForm.value.username;
    this.loginService.loginUser.password = loginForm.value.password;
    this.loginService.login(this.loginService.loginUser).subscribe(
      data => {
        console.log(data);
        sessionStorage.setItem('loggedInUser', data.user_id);
        sessionStorage.setItem('token', data.access_token);
        sessionStorage.setItem('timeZone', data.time_zone);
        this.userService.getUserById(data.user_id).subscribe(
          (data:any) => {
            this.isSuperAdmin = false;
            this.isRoleCustomerManager = false;
            this.userRole = data.body.role;
            // this.companies = data.body.companies.companyName;
            
            
            console.log("this.user");
            console.log(data.body);
            for (let i = 0; i < this.userRole.length; i++) {
              let userdata=false;
              if (this.userRole[i].displayName === "ROLE_SUPERADMIN") {
                console.log("IS_ROLE_SUPERADMIN");
                this.isSuperAdmin = true;
              }else{
                this.isSuperAdmin = false; 
              }
              // if((data.body.companies != null && data.body.companies.type != null && data.body.companies.type=="INSTALLER" && this.userRole[i].displayName === "ROLE_CUSTOMER_MANAGER") || (this.userRole[i].displayName === "ROLE_INSTALLER")){
              //  this.isCustomerManagerInstaller=true;
              // }
              // if (this.userRole[i].displayName === "ROLE_CUSTOMER_MANAGER" && data.body.companies != null && data.body.companies.type != null && data.body.companies.type=="CUSTOMER"){
              //   this.isRoleCustomerManager = true;
              // }
              
            }
            sessionStorage.setItem('IS_ROLE_SUPERADMIN', this.isSuperAdmin);
            sessionStorage.setItem('IS_ROLE_CUSTOMER_MANAGER', this.isRoleCustomerManager);
            sessionStorage.setItem('IS_ROLE_CUSTOMER_MANAGER_INSTALLER', this.isCustomerManagerInstaller);

            // sessionStorage.setItem('companies',  this.companies);
            // sessionStorage.setItem('type', data.body.companies.type);
            if(data.body.is_password_change==true){
              this.router.navigate(['reset-password']);
              sessionStorage.setItem('Password_Change', 'true');
              return  
            }
            if (this.isRoleCustomerManager)
              this.router.navigate(['devices-list']);
            else
            if(this.loginService.beforeLoginUrl != null){
              this.router.navigate([this.loginService.beforeLoginUrl]);
              this.loginService.beforeLoginUrl = null;
              return
            }
              this.router.navigate(['devices-list']);
         
          }
        )
        
       
      },
      error => {
        console.log(error);
        console.log(error.status == 401);
        if (error.status == 401) {
          this.loginError = true;
        }
        this.invaliduser = true;
      }
    );
  }
  refresh() {
    location.reload();
  }

  forget() {
    this.router.navigate(['forgotPassword']);
  }
}
