import { Component, OnInit, ViewChild } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { MatOption, MatSelect } from '@angular/material';
import { Location } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { CompaniesService } from '../companies.service';
import { CustomSuccessToastrComponent } from '../custom-success-toastr/custom-success-toastr.component';
import { Organisation, User } from '../models/user.model';
import { MustMatch, ValidatorUtils } from '../util/validatorUtils';
import { AddUserService } from './add-user.service';
import { CountryCode } from '../constant/country.code';
declare var $: any;
@Component({
  selector: 'app-add-user',
  templateUrl: './add-user.component.html',
  styleUrls: ['./add-user.component.css']
})
export class AddUserComponent implements OnInit {

  @ViewChild('select') select: MatSelect;

  allSelected = false;
  countryCode = 'us';
  ids = [];
  orgName = new Map();
  companies = [];
  orignalCompanies = [];

  organisation: Organisation;
  user: User;
  registerForm: FormGroup;
  // Roles=[];
  userId: any;
  notify;
  submitted = false;

  updateFlag = false;
  loggedInUserCompanyName: String;
  pageTitle;
  roleMaintenance: any;
  rolePCTUser: any;
  isSuperAdmin: any;
  companiesDetails: any;
  notification: any[] = [{ value: 'email', name: 'Email' }, { value: 'text', name: 'Text' }];
  userCompanyType;
  validatorUtil: ValidatorUtils = new ValidatorUtils();
  loggedInUser: any;
  Roles = [];
  isHomeLocationSeleted = false;
  dropDownLocationsList: any = [];
  dropDownAdditionalLocationsList: any = [];
  dropDownHomeLocationsList: any = [];
  selectedHomeLocation: any;
  selectedAdditionalLocations: any = [];
  orgLocationsList: any = [];
  roleCallCenterUser: any;
  isRoleOrgUser: any;
  constructor(
    private addUserService: AddUserService,
    public router: Router,
    private formBuldier: FormBuilder,
    private toastr: ToastrService,
    public companiesService: CompaniesService,
    private location: Location,
    private readonly activateRoute: ActivatedRoute) {
    this.roleCallCenterUser = JSON.parse(sessionStorage.getItem('IS_ROLE_CALL_CENTER_USER'));
    this.isRoleOrgUser = JSON.parse(sessionStorage.getItem('IS_ROLE_ORG_USER'));
    this.rolePCTUser = JSON.parse(sessionStorage.getItem('IS_ROLE_PCT_USER'));
    this.roleMaintenance = JSON.parse(sessionStorage.getItem('IS_ROLE_MAINTENANCE'));
    this.loggedInUser = sessionStorage.getItem('loginId');
    this.activateRoute.queryParams.subscribe(params => {
      this.userId = params.id;
    });
    if (this.rolePCTUser || (this.isRoleOrgUser && this.loggedInUser !== this.userId)) {
      this.location.back();
    }
    if (this.rolePCTUser) {
      this.location.back();
    }
    this.initializeForm();
    this.getAllCompanies();
  }
  ngOnInit() {
    if (this.addUserService.user != undefined && this.addUserService.editFlag == true) {
      this.updateFlag = true;
      if (this.addUserService.navigateFromSettings === true) {
        this.pageTitle = 'Account Settings';
      }
    }
    this.isSuperAdmin = sessionStorage.getItem('IS_ROLE_SUPERADMIN');
    this.companiesDetails = sessionStorage.getItem('companies');
    this.createRoles();
    this.pageTitle = 'Create or Edit User';

    this.userCompanyType = sessionStorage.
      getItem('type');

  }

  getUserById() {
    this.addUserService.getUserByUserId(this.userId)
      .subscribe(data => {
        console.log(data.body);
        this.countryCode = data.body.country_code;
        this.organisation = data.body.organisation;
        this.initializeForm(data.body);
        this.selectedHomeLocation = data.body.home_location;
        this.selectedAdditionalLocations = data.body.additional_locations;
        if (this.selectedHomeLocation !== undefined && this.selectedHomeLocation !== null) {
          this.isHomeLocationSeleted = true;
        }
        this.getLocationListByOrgId();
      }, error => {
        // this.cancel();
      });
  }


  cancel() {
    this.router.navigate(['users']);
  }

  onCountryChange(event) {
    this.countryCode = event.iso2;
  }

  toggleAllSelection() {
    if (this.allSelected) {
      this.select.options.forEach((item: MatOption) => item.select());
    } else {
      this.select.options.forEach((item: MatOption) => item.deselect());
    }
  }




  duplicateEmail(controlName: string) {
    if (!this.userId) {
      return (formGroup: FormGroup) => {
        const control = formGroup.controls[controlName];
        if (control.value && control.value.length > 5) {
          if (this.updateFlag && control.value === this.addUserService.user.email) {
            return;
          }

          this.addUserService.findByUsername(control.value)
            .subscribe(data => {
              if (data.body) {
                control.setErrors({ duplicate: true });
              }
            });
        }
      };
    }
  }


  initializeForm(data?: any) {
    const roleId = [];
    if (data && data.role && data.role.length > 0) {
      for (const iterator of data.role) {
        roleId.push(iterator.role_id);
        iterator.roleId = iterator.role_id;
      }
    }
    if (this.userId) {
      // this.countryCode = data && data.country_code ? data.country_code.substring(1, data.country_code.length) : '';
      this.registerForm = this.formBuldier.group({
        id: [data && data.id ? data.id : null],
        first_name: [data && data.first_name ? data.first_name : '', Validators.compose([Validators.required, Validators.pattern(this.validatorUtil.spaceValidator)])],
        last_name: [data && data.last_name ? data.last_name : '', Validators.compose([Validators.required, Validators.pattern(this.validatorUtil.spaceValidator)])],
        email: [data && data.email ? data.email : '', Validators.compose([Validators.required,
        Validators.email, Validators.minLength(4),
        Validators.pattern(this.validatorUtil.emailValidator)])],
        roleId: [roleId ? roleId : [], Validators.compose([Validators.required])],
        role: [data && data.role ? data.role : [], Validators.compose([Validators.required])],
        is_active: [data && data.is_active ? 1 : 0],
        phone: [data && data.phone ? data.phone : '', Validators.compose([Validators.pattern(this.validatorUtil.phoneNumberValidator), Validators.minLength(10), Validators.maxLength(10), Validators.required])],
        time_zone: [data && data.time_zone ? data.time_zone : ''],
        organisationsId: [{ value: data && data.organisation ? data.organisation.id : '', disabled: this.isSuperAdmin === 'false' }, Validators.compose([Validators.required])],
        locationId: [data && data.home_location ? data.home_location.id : ''],
        homeLocation: [data && data.home_location ? data.home_location : ''],
        organisations: [data && data.organisation ? data.organisation : ''],
        notify: [data && data.notify ? data.notify : ''],
        countryCode: [data && data.country_code ? data.country_code.substring(1, data.country_code.length) : '']
      });
      this.registerForm.updateValueAndValidity();
    } else {
      this.registerForm = this.formBuldier.group({
        id: [data && data.id ? data.id : null],
        first_name: [data && data.first_name ? data.first_name : '', Validators.compose([Validators.required, Validators.pattern(this.validatorUtil.spaceValidator)])],
        last_name: [data && data.last_name ? data.last_name : '', Validators.compose([Validators.required, Validators.pattern(this.validatorUtil.spaceValidator)])],
        email: [data && data.email ? data.email : '', Validators.compose([Validators.required,
        Validators.email, Validators.minLength(4),
        Validators.pattern(this.validatorUtil.emailValidator)])],
        roleId: [roleId ? roleId : [], Validators.compose([Validators.required])],
        role: [data && data.role ? data.role : [], Validators.compose([Validators.required])],
        is_active: [data && data.is_active ? 1 : 0],
        phone: [data && data.phone ? data.phone : '', Validators.compose([Validators.pattern(this.validatorUtil.phoneNumberValidator), Validators.minLength(10), Validators.maxLength(10), Validators.required])],
        time_zone: [data && data.time_zone ? data.time_zone : ''],
        organisationsId: [{ value: data && data.organisation ? data.organisation.id : '', disabled: this.isSuperAdmin === 'false' }, Validators.compose([Validators.required])],
        locationId: [''],
        homeLocation: [data && data.home_location ? data.home_location : ''],
        organisations: [data && data.organisation ? data.organisation : ''],
        notify: [data && data.notify ? data.notify : ''],
        countryCode: [data && data.country_code ? data.country_code : ''],
        password: ['', Validators.compose([Validators.required, Validators.minLength(5), Validators.pattern(this.validatorUtil.passwordValidator)])],
        confirmPassword: ['', Validators.required]
      }, {
        validator: [MustMatch('password', 'confirmPassword'), this.duplicateEmail('email')]
      });
    }
  }

  telInputObject(obj) {
    let self = this;
    setTimeout(function () {
      if (obj && self.countryCode) {
        obj.setCountry(self.countryCode);
      }
      return obj;
    }, 1500);
  }

  message = "";
  onSelectChange(event: any) {
    this.getLocationListByOrgId();
  }

  onSelectRoleChange(event: any) {
    let filterCompanies = false;
    this.companies = this.orignalCompanies;
    let roleList = [];
    let company = [];
    for (const iterator1 of this.companies) {
      let toAdd = false;
      let callCenterRole = false;
      let maintenanceRole = false;
      let installerRole = false;
        for (const iterator of event) {
          let obj = this.Roles.find(element => element.roleId === iterator);
          obj.role_id = obj.roleId;
         
          if(obj.description == "ROLE_CALL_CENTER_USER" || obj.description == "ROLE_INSTALLER" || obj.description == "ROLE_MAINTENANCE")
          {
          if(obj.description == "ROLE_CALL_CENTER_USER")
          {
           if(iterator1.accountNumber === "A-00243")
           {
            toAdd = true;
           }
          }
          else
          {
          if(obj.description == "ROLE_MAINTENANCE")
          {
            maintenanceRole = true;
            if(iterator1.organisationRole && iterator1.organisationRole.includes('MAINTENANCE_MODE'))
            {
              if(installerRole)
              {
                if(toAdd)
                {
                  toAdd = true;
                }
                else
                {
                  toAdd = false
                }
              }
              else{
                toAdd = true;
              }
              
            }
            else
            {
              if(installerRole)
              {
                toAdd = false;
              }
            }
          }
          if(obj.description == "ROLE_INSTALLER")
          {
            installerRole = true;
            if(iterator1.organisationRole && iterator1.organisationRole.includes('INSTALLER'))
            {
              if(maintenanceRole)
              {
                if(toAdd)
                {
                  toAdd = true;
                }
                else
                {
                  toAdd = false
                }
              }
              else{
                toAdd = true;
              }
              
            }
            else
            {
              if(maintenanceRole)
              {
                toAdd = false;
              }
            }
          }
        }
          filterCompanies = true;
        }
        roleList.push(obj);
      }
      if(toAdd)
      {
      company.push(iterator1);
      }
      
    }
    if(filterCompanies)
    {
    this.companies = [];
    this.companies = company;
    }
    this.registerForm.controls.role.setValue(roleList);
  }



  getAllCompanies() {
    if (this.userId) {
      this.getUserById();
    }
    this.companiesService.getAllOrgList().subscribe(
      data => {
        if (this.roleCallCenterUser) {
          this.companies.push(data.find(item => item.accountNumber === 'A-00243'));
        } else if (this.roleMaintenance) {
          let user;
          this.addUserService.getUserByUserId(this.loggedInUser)
            .subscribe(userData => {
              user = userData.body;
              this.companies.push(data.find(item => item.accountNumber === user.organisation.accountNumber));
              if (this.organisation) {
                this.companies.push(data.find(item => item.accountNumber === this.organisation.accountNumber));
              }
            }, error => {
            });
        } else {
          this.companies = data;
        }
        this.orignalCompanies = this.companies;
        this.getAllRoles();
      },
      error => {
        console.log(error);
      }
    );
  }


  getLocationListByOrgId() {
    const organisation = this.registerForm.controls.organisations.value;
    this.addUserService.getLocationsByOrgId(organisation.id).subscribe(
      data => {
        this.orgLocationsList = data;
        this.updateList();
        if (this.selectedHomeLocation && this.selectedHomeLocation.id !== null) {
          this.registerForm.controls.locationId.setValue(this.selectedHomeLocation.id);
        }
        this.registerForm.updateValueAndValidity();
      },
      error => {
        console.log(error);
      }
    );
  }

  updateList() {
    this.dropDownLocationsList = [];
    this.dropDownAdditionalLocationsList = [];
    this.dropDownHomeLocationsList = [];
    for (const itertor of this.orgLocationsList) {
      const obj = this.selectedAdditionalLocations.find(item => item.id === itertor.id);
      if (!obj) {
        this.dropDownHomeLocationsList.push(itertor);
        if (this.selectedHomeLocation !== undefined && this.selectedHomeLocation !== null) {
          if (this.selectedHomeLocation.id !== itertor.id) {
            this.dropDownLocationsList.push(itertor);
            this.dropDownAdditionalLocationsList.push(itertor);
          }
        } else {
          this.dropDownLocationsList.push(itertor);
          this.dropDownAdditionalLocationsList.push(itertor);
        }
      }
    }
  }

  getAllRoles() {
    this.addUserService.getAllRoleName().subscribe(
      data => {
        if (this.roleCallCenterUser) {
          this.Roles.push(data.data.find(item => item.name === 'Call Center User'));
        } else if (this.roleMaintenance) {
          const roleInstaller = data.data.find((item: { name: string; }) => item.name === 'Installer');
          if (roleInstaller !== undefined) {
            this.Roles.push(roleInstaller);
          }
          this.Roles.push(data.data.find((item: { name: string; }) => item.name === 'Org User'));
          this.Roles.push(data.data.find((item: { name: string; }) => item.name === 'Maintenance Role'));
        } else {
          this.Roles = data.data;
        }
        // if (this.userId) {
        //   this.getUserById();
        // }
      },
      error => {
        console.log(error);
      }
    );
  }



  createRoles() {
    if ((this.updateFlag && this.addUserService.user.companies.type === 'MANUFACTURER')) {
      this.Roles = this.Roles.filter(role => role.id === 1);
      return;
    }
    this.Roles = this.Roles.filter(role => role.id !== 1);
  }

  get f() { return this.registerForm.controls; }

  superAdmin(user) {
    if (user.role.some((r) => r.roleName === 'ROLE_SUPERADMIN')) {
      return true;
    }
    return false;
  }

  onSubmit(registerForm) {

    if (!this.registerForm.valid) {
      this.validateAllFormFields(this.registerForm);
      return;
    }

    const reData = this.registerForm.value;

    const local = {
      id: reData.id,
      first_name: reData.first_name,
      last_name: reData.last_name,
      organisation: {
        accountNumber: reData.organisations.accountNumber
      },
      password: reData.password,
      phone: reData.phone,
      country_code: this.countryCode,
      is_active: reData.is_active,
      user_name: reData.email,
      notify: reData.notify,
      email: reData.email,
      role: reData.role,
      home_location: reData.homeLocation && reData.homeLocation !== '' ? reData.homeLocation : {},
      additional_locations: this.selectedAdditionalLocations
    };
    this.addUserService.addUser(local)
      .subscribe(
        data => {
          if (this.userId) {
            this.toastr.success('User successfully updated', local.email, { toastComponent: CustomSuccessToastrComponent });
          } else {
            this.toastr.success('User successfully added', local.email, { toastComponent: CustomSuccessToastrComponent });
          }
          this.router.navigate(['users']);
          this.submitted = false;
          this.registerForm.reset();

        }, error => {
          this.cancel();
          console.log(error);

        }
      );
  }

  validateAllFormFields(formGroup: FormGroup) {
    Object.keys(formGroup.controls).forEach(field => {
      const control = formGroup.get(field);
      if (control instanceof FormControl) {
        control.markAsTouched({ onlySelf: true });
      } else if (control instanceof FormGroup) {
        this.validateAllFormFields(control);
      }
    });
  }

  onSelectChangeAdditionalLocations(event) {
    const obj = this.dropDownLocationsList.find(element => element.id === event.value);
    if (obj) {
      const list = this.dropDownLocationsList.filter(data => {
        if (data.id !== obj.id) {
          return true;
        }
        return false;
      }
      );
      this.dropDownLocationsList = list;
    }
    this.selectedAdditionalLocations.push(obj);
    this.showAnotherLocation = false;
    this.updateList();
  }

  onSelectChangeHomeLocation(event) {
    const obj = this.dropDownLocationsList.find(element => element.id === event.value);
    if (obj) {
      this.registerForm.controls.homeLocation.setValue(obj);
      this.selectedHomeLocation = obj;
      this.isHomeLocationSeleted = true;
    }
    this.updateList();
  }

  deleteSelectedAdditionalLocation(index: number) {
    this.selectedAdditionalLocations.splice(index, 1);
    this.updateList();
    // const add = this.companyForm.get('forwarding_list') as FormArray;
    // add.removeAt(index)
  }

  showAnotherLocation: boolean = false;

  showAddAnotherLocation() {
    this.showAnotherLocation = true;
  }

  oncompanySelection(event) {
    const companyType = this.registerForm.controls.company.value.type;
    this.registerForm.controls.role.reset();

    if (companyType.toUpperCase() === 'MANUFACTURER') {
      this.Roles = [
        {
          id: 1,
          roleName: 'Phillips Connect Admin',
          description: 'String',
          displayName: 'ROLE_SUPERADMIN'
        }];
    } else if (companyType.toUpperCase() === 'INSTALLER' || companyType.toUpperCase() == 'CUSTOMER') {
      this.registerForm.controls.role.reset;
      this.Roles = [{
        id: 2,
        roleName: 'Installer',
        description: 'String',
        displayName: 'ROLE_INSTALLER'
      },
      {
        id: 3,
        roleName: 'Org Admin',
        description: 'String',
        displayName: 'ROLE_CUSTOMER_MANAGER'
      }];
    } else {
      this.Roles = undefined;
    }
    if (this.updateFlag) {
      this.toastr.info('Please Select the Role');
    }
  }



}
