import { Component, OnInit } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';
import { LoginService } from '../login/login.service';
import { ToastrService } from 'ngx-toastr';
import { UserService } from '../user/user.component.service';
import { CommonData } from '../constant/common-data';
import { CustomSuccessToastrComponent } from '../custom-success-toastr/custom-success-toastr.component';
import { CustomErrorToastrComponent } from '../custom-error-toastr/custom-error-toastr.component';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {

  loginForm: FormGroup;

  invaliduser = false;
  loading = false;
  loginError = false;
  errorMessage: any;
  isSuperAdmin: any;
  rolePCTUser: any;
  roleCallCenterUser: any;
  isRoleCustomerManager: any;
  isCustomerManagerInstaller: any;
  landingPageList = CommonData.LANDING_PAGE_LIST;

  userRole: any;
  companies: any;
  // login_token;
  constructor(
    private router: Router, private _formBuldier: FormBuilder, private route: ActivatedRoute,
    private loginService: LoginService, private toastr: ToastrService, private userService: UserService
  ) { }
  ngOnInit() {
    const data = JSON.parse(localStorage.getItem('rememberMe'));
    this.loginForm = this._formBuldier.group({
      username: [data && data.userName ? data.userName : '', [Validators.required, Validators.email]],
      password: [data && data.password ? data.password : '', [Validators.required, Validators.minLength(5)]],
      rememberme: [data && data.rememberme ? data.rememberme : false],
      landing: [data && data.landing ? data.landing : 'DEVICE_LIST'],
    });
    const token = sessionStorage.getItem('token');
    if (token != null && token !== undefined && token !== '') {
      const data = {
        user_id: sessionStorage.getItem('userId'),
        token: sessionStorage.getItem('token'),
        time_zone: sessionStorage.getItem('timeZone'),
        userName: sessionStorage.getItem('userName')
      };
      this.AzureLogin(data);
    }
    //  else {
    //     window.location.href = "http://localhost:8011/azure/login";
    //  }
  }

  get getLoginForm() { return this.loginForm.controls; }
  // deploye
  login(loginForm: any) {
    if (loginForm.value.rememberme) {
      const obj = {
        userName: loginForm.value.username,
        password: loginForm.value.password,
        rememberme: loginForm.value.rememberme,
        landing: loginForm.value.landing
      };
      localStorage.setItem('rememberMe', JSON.stringify(obj));
    } else {
      localStorage.removeItem('rememberMe');
    }
    this.loginError = false;
    this.errorMessage = '';
    this.loginService.loginUser.username = loginForm.value.username;
    this.loginService.loginUser.password = loginForm.value.password;
    this.loginService.loginUser.landing = loginForm.value.landing;
    this.login2(loginForm);
    this.loginService.login(this.loginService.loginUser).subscribe(
      data => {
        if (data.status === 'FAILURE') {
          this.loginError = true;
          this.errorMessage = data.message;
          return;
        }
        sessionStorage.setItem('loggedInUser', data.user_id);
        sessionStorage.setItem('token', data.token);
        sessionStorage.setItem('timeZone', data.time_zone);
        this.userService.getUserByUserName(data.userName).subscribe(
          data => {
            this.isSuperAdmin = false;
            this.isRoleCustomerManager = false;
            this.userRole = data.role;
            sessionStorage.setItem('userName', data.user_name);
            sessionStorage.setItem('email', data.user_name);
            sessionStorage.setItem('loginId', data.id);
            if (data.organisation) {
              sessionStorage.setItem('ORGANISATION_ID', data.organisation.id);
            }
            let roleOrgUser = false;
            let roleMaintenance = false;
            let roleInstaller = false;
            let roleOrgAdmin = false;
            for (const iterator of this.userRole) {
              if (iterator.name === 'Phillips Connect Admin' || iterator.description === 'ROLE_SUPERADMIN') {
                this.isSuperAdmin = true;
                break;
              }
              if (iterator.name === 'Phillips Connect User' || iterator.description === 'ROLE_PCT_USER') {
                this.rolePCTUser = true;
              }
              if (iterator.name === 'Call Center User' || iterator.description === 'ROLE_CALL_CENTER_USER') {
                this.roleCallCenterUser = true;
              }
              if (iterator.name === 'Installer' || iterator.description === 'ROLE_INSTALLER') {
                roleInstaller = true;
              }
              if (iterator.name === 'Org User' || iterator.description === 'ROLE_ORG_USER') {
                roleOrgUser = true;
              }
              if (iterator.name === 'Org Admin' || iterator.description === 'ROLE_COMPANY_ADMIN') {
                roleOrgAdmin = true;
              }
              if (iterator.name === 'Maintenance Role' || iterator.description === 'ROLE_MAINTENANCE') {
                roleMaintenance = true;
              }
            }

            sessionStorage.setItem('IS_ROLE_SUPERADMIN', this.isSuperAdmin ? this.isSuperAdmin : false);
            sessionStorage.setItem('IS_ROLE_CUSTOMER_MANAGER', this.isRoleCustomerManager);
            sessionStorage.setItem('IS_ROLE_CUSTOMER_MANAGER_INSTALLER', this.isCustomerManagerInstaller);
            sessionStorage.setItem('IS_ROLE_PCT_USER', this.rolePCTUser ? this.rolePCTUser : false);
            sessionStorage.setItem('IS_ROLE_CALL_CENTER_USER', this.roleCallCenterUser ? this.roleCallCenterUser : false);
            sessionStorage.setItem('IS_ROLE_INSTALLER', JSON.stringify(roleInstaller));
            sessionStorage.setItem('IS_ROLE_ORG_USER', JSON.stringify(roleOrgUser));
            sessionStorage.setItem('IS_ROLE_ORG_ADMIN', JSON.stringify(roleOrgAdmin));
            sessionStorage.setItem('IS_ROLE_MAINTENANCE', JSON.stringify(roleMaintenance));


            if (data.landing_page && !this.roleCallCenterUser && !roleOrgAdmin && !roleOrgUser && !roleMaintenance) {
              const obj = this.landingPageList.find(item => item.value === data.landing_page);
              if (obj) {
                sessionStorage.setItem('landingPage', obj.name);
                sessionStorage.setItem('landingPageValue', JSON.stringify(obj));
                this.navigateTopage(data, obj.url);
                return;
              }
            }

            if (roleOrgAdmin || roleOrgUser) {
              this.router.navigate(['customerAssets']);
              return;
            }
            if (roleMaintenance) {
              this.router.navigate(['users']);
              return;
            }
            if (this.isRoleCustomerManager) {
              this.router.navigate(['gateway-list']);
            } else
              if (this.loginService.beforeLoginUrl !== null && this.loginService.beforeLoginUrl !== undefined && this.loginService.beforeLoginUrl !== '/') {
                this.router.navigate([this.loginService.beforeLoginUrl]);
                this.loginService.beforeLoginUrl = null;
                return;
              }
            this.router.navigate(['gateway-list']);

          }
        );
      },
      error => {
        console.log(error);
        this.errorMessage = 'The username & password entered does not match a valid user account. Please check and try again.';
        this.loginError = true;
        this.invaliduser = true;
      }
    );
  }

  navigateTopage(data: any, pageUrl: any) {
    if (data.is_password_change === true) {
      this.router.navigate(['reset-password']);
      sessionStorage.setItem('Password_Change', 'true');
      return;
    } else {
      this.router.navigate([pageUrl]);
    }
  }

  login2(loginForm) {
    this.loginError = false;
    this.loginService.loginUser.username = loginForm.value.username;
    this.loginService.loginUser.password = loginForm.value.password;
    const obj: any = {};
    this.loginService.login2(this.loginService.loginUser).subscribe(
      data => {
        obj.loggedInUser = data.user_id;
        obj.token = data.access_token;
        obj.timeZone = data.time_zone;
        localStorage.setItem('user2_token', data.access_token);
        localStorage.setItem('id', data.user_id);
        this.userService.getUserById2(data.user_id).subscribe(
          data => {
            this.isSuperAdmin = false;
            this.isRoleCustomerManager = false;
            this.userRole = data.body.role;
            this.companies = data.body.companies.companyName;
            for (let i = 0; i < this.userRole.length; i++) {
              // if (this.userRole[i].displayName === 'ROLE_INSTALLER') {
              // sessionStorage.removeItem('loggedInUser');
              // sessionStorage.removeItem('token');
              // sessionStorage.removeItem('timeZone');
              // return;
              // }
              if (this.userRole[i].displayName === 'ROLE_SUPERADMIN') {
                this.isSuperAdmin = true;
              } else {
                this.isSuperAdmin = false;
              }
              if ((data.body.companies != null && data.body.companies.type != null &&
                data.body.companies.type === 'INSTALLER' && this.userRole[i].displayName === 'ROLE_CUSTOMER_MANAGER') || (this.userRole[i].displayName === 'ROLE_INSTALLER')) {
                this.isCustomerManagerInstaller = true;
              }
              if (this.userRole[i].displayName === 'ROLE_CUSTOMER_MANAGER' && data.body.companies != null && data.body.companies.type != null && data.body.companies.type === 'CUSTOMER') {
                this.isRoleCustomerManager = true;
              }

            }
            localStorage.setItem('role', this.isSuperAdmin);

            obj.IS_ROLE_SUPERADMIN = this.isSuperAdmin;
            obj.IS_ROLE_CUSTOMER_MANAGER = this.isRoleCustomerManager;
            obj.IS_ROLE_CUSTOMER_MANAGER_INSTALLER = this.isCustomerManagerInstaller;
            obj.companies = this.companies;
            obj.type = data.body.companies.type;

            localStorage.setItem('sessionStorage', JSON.stringify(obj));
          }
        );


      },
      error => {
        console.log(error);
      }
    );
  }


  refresh() {
    location.reload();
  }

  forget() {
    this.router.navigate(['forgotPassword']);
  }

  AzureLogin(data) {
    sessionStorage.setItem('loggedInUser', data.user_id);
    sessionStorage.setItem('token', data.token);
    sessionStorage.setItem('timeZone', data.time_zone);
    this.iaLoginUsingAzureToken(data);
    this.userService.getUserByUserName(data.userName).subscribe(
      data => {
        console.log('data user');
        console.log(data);
        this.isSuperAdmin = false;
        this.isRoleCustomerManager = false;
        this.userRole = data.role;
        console.log(this.userRole.name);
        sessionStorage.setItem('userName', data.userName);
        let roleOrgUser = false;
        let roleMaintenance = false;
        let roleInstaller = false;
        let roleOrgAdmin = false;
        for (const iterator of this.userRole) {
          if (iterator.name === 'Phillips Connect Admin' || iterator.description === 'ROLE_SUPERADMIN') {
            this.isSuperAdmin = true;
            break;
          }
          if (iterator.name === 'Phillips Connect User' || iterator.description === 'ROLE_PCT_USER') {
            this.rolePCTUser = true;
          }

          if (iterator.name === 'Call Center User' || iterator.description === 'ROLE_CALL_CENTER_USER') {
            this.roleCallCenterUser = true;
          }
          if (iterator.name === 'Installer' || iterator.description === 'ROLE_INSTALLER') {
            roleInstaller = true;
          }
          if (iterator.name === 'Org User' || iterator.description === 'ROLE_ORG_USER') {
            roleOrgUser = true;
          }
          if (iterator.name === 'Org Admin' || iterator.description === 'ROLE_COMPANY_ADMIN') {
            roleOrgAdmin = true;
          }
          if (iterator.name === 'Maintenance Role' || iterator.description === 'ROLE_MAINTENANCE') {
            roleMaintenance = true;
          }
        }
        sessionStorage.setItem('IS_ROLE_SUPERADMIN', this.isSuperAdmin ? this.isSuperAdmin : false);
        sessionStorage.setItem('IS_ROLE_CUSTOMER_MANAGER', this.isRoleCustomerManager);
        sessionStorage.setItem('IS_ROLE_CUSTOMER_MANAGER_INSTALLER', this.isCustomerManagerInstaller);
        sessionStorage.setItem('IS_ROLE_PCT_USER', this.rolePCTUser ? this.rolePCTUser : false);
        sessionStorage.setItem('IS_ROLE_CALL_CENTER_USER', this.roleCallCenterUser ? this.roleCallCenterUser : false);
        sessionStorage.setItem('IS_ROLE_INSTALLER', JSON.stringify(roleInstaller));
        sessionStorage.setItem('IS_ROLE_ORG_USER', JSON.stringify(roleOrgUser));
        sessionStorage.setItem('IS_ROLE_ORG_ADMIN', JSON.stringify(roleOrgAdmin));
        sessionStorage.setItem('IS_ROLE_MAINTENANCE', JSON.stringify(roleMaintenance));

        // if (data.is_password_change === true) {
        //   this.router.navigate(['reset-password']);
        //   sessionStorage.setItem('Password_Change', 'true');
        //   return;
        // }
        if (this.isRoleCustomerManager) {
          this.router.navigate(['gateway-list']);
        } else
          if (this.loginService.beforeLoginUrl !== null && this.loginService.beforeLoginUrl !== undefined && this.loginService.beforeLoginUrl !== '/') {
            this.router.navigate([this.loginService.beforeLoginUrl]);
            this.loginService.beforeLoginUrl = null;
            return;
          }
        this.router.navigate(['gateway-list']);
      });
  }

  iaLoginUsingAzureToken(input) {
    this.loginError = false;
    const obj: any = {};
    this.loginService.ialoginUsingAzure({ "accessToken": input.token }).subscribe(
      data => {
        obj.loggedInUser = data.user_id;
        obj.token = data.access_token;
        obj.timeZone = data.time_zone;
        localStorage.setItem('user2_token', data.access_token);
        localStorage.setItem('id', data.user_id);
        this.userService.getUserById2(data.user_id).subscribe(
          data => {
            this.isSuperAdmin = false;
            this.isRoleCustomerManager = false;
            this.userRole = data.body.role;
            this.companies = data.body.companies.companyName;
            for (let i = 0; i < this.userRole.length; i++) {
              // if (this.userRole[i].displayName === 'ROLE_INSTALLER') {
              // sessionStorage.removeItem('loggedInUser');
              // sessionStorage.removeItem('token');
              // sessionStorage.removeItem('timeZone');
              // return;
              // }
              if (this.userRole[i].displayName === 'ROLE_SUPERADMIN') {
                this.isSuperAdmin = true;
              } else {
                this.isSuperAdmin = false;
              }
              if ((data.body.companies != null && data.body.companies.type != null &&
                data.body.companies.type === 'INSTALLER' && this.userRole[i].displayName === 'ROLE_CUSTOMER_MANAGER') || (this.userRole[i].displayName === 'ROLE_INSTALLER')) {
                this.isCustomerManagerInstaller = true;
              }
              if (this.userRole[i].displayName === 'ROLE_CUSTOMER_MANAGER' && data.body.companies != null && data.body.companies.type != null && data.body.companies.type === 'CUSTOMER') {
                this.isRoleCustomerManager = true;
              }

            }
            localStorage.setItem('role', this.isSuperAdmin);

            obj.IS_ROLE_SUPERADMIN = this.isSuperAdmin;
            obj.IS_ROLE_CUSTOMER_MANAGER = this.isRoleCustomerManager;
            obj.IS_ROLE_CUSTOMER_MANAGER_INSTALLER = this.isCustomerManagerInstaller;
            obj.companies = this.companies;
            obj.type = data.body.companies.type;

            localStorage.setItem('sessionStorage', JSON.stringify(obj));
          }
        );


      },
      error => {
        console.log(error);
      }
    );
  }

}
