import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { AssetsService } from '../assets.service';
import { CompaniesService } from '../companies.service';
import { CommonData } from '../constant/common-data';
import { CountryCode } from '../constant/country.code';
import { CustomSuccessToastrComponent } from '../custom-success-toastr/custom-success-toastr.component';
import { GatewayListService } from '../gateway-list.service';
import { Location } from '@angular/common';
import { ValidatorUtils } from '../util/validatorUtils';

@Component({
  selector: 'app-batch-device-edit',
  templateUrl: './batch-device-edit.component.html',
  styleUrls: ['./batch-device-edit.component.css']
})
export class BatchDeviceEditComponent implements OnInit {

  loading = false;
  companies: any[] = [];
  approvedProducts: any[] = [];
  deviceEditForm: FormGroup;
  usage: any[] = CommonData.USAGE_DATA;
  serviceNetwork: any[] = CommonData.TELECOM_PROVIDER_DATA;
  hardwareType: any[] = CommonData.HARDWARE_TYPE;
  countryCodeList: any[] = CountryCode.COUNTRY_CODE_LIST;
  validatorUtil: ValidatorUtils = new ValidatorUtils();
  rolePCTUser: any;
  isRolePctUser: any;
  deviceList: any;
  endCustomer: any = [];
  installedBy: any = [];
  purchasedBy: any = [];
  imeiList: any = [];
  constructor(
    private readonly formBuilder: FormBuilder,
    private readonly companiesService: CompaniesService,
    private readonly assetsService: AssetsService,
    private readonly gatewayListService: GatewayListService,
    private toastr: ToastrService,
    private readonly router: Router,
    private location: Location) {

    this.deviceList = JSON.parse(sessionStorage.getItem('batch_edit_device_list'));
    this.isRolePctUser = JSON.parse(sessionStorage.getItem('IS_ROLE_PCT_USER'));
    if (this.isRolePctUser || !this.deviceList || this.deviceList.length <= 0) {
      this.location.back();
    }
    this.imeiList = this.deviceList.map(item => item.imei);
    this.getAllcompanies();
    this.getAssetTypeORApprovedProduct();
  }

  ngOnInit() {
    this.initializeFormControl();
    this.rolePCTUser = JSON.parse(sessionStorage.getItem('IS_ROLE_PCT_USER'));
  }

  initializeFormControl() {
    this.deviceEditForm = this.formBuilder.group({
      asset_type: [''],
      manufacturer_name: [''],
      year: [''],
      imei: [this.imeiList],
      can: ['', Validators.compose([Validators.required])],
      product_name: [''],
      product_code: [''],
      usage_status: ['', Validators.compose([Validators.required])],
      purchase_by: [''],
      installed_by: [''],
      service_network: ['', Validators.compose([Validators.required])],
      country_code: [''],
      is_active: [1],
    });
  }

  getAllcompanies() {
    this.companiesService.getAllCompanyList().subscribe(
      data => {
        this.companies = data;
        for (const iterator of data) {
          if (iterator.organisationRole) {
            const obj = iterator.organisationRole.find(item => (item === 'END_CUSTOMER' || item === 'ENDCUSTOMER' || item === 'EndCustomer'));
            if (obj) {
              this.endCustomer.push(iterator);
            }
            const obj2 = iterator.organisationRole.find(item => (item === 'INSTALLER' || item === 'Installer'));
            if (obj2) {
              this.installedBy.push(iterator);
            }
            const obj3 = iterator.organisationRole.find(item => (item === 'Reseller' || item === 'RESELLER' || item === 'END_CUSTOMER' || item === 'ENDCUSTOMER' || item === 'EndCustomer'));
            if (obj3) {
              this.purchasedBy.push(iterator);
            }
          }
        }
      },
      error => {
        console.log(error);
      });
  }


  getAssetTypeORApprovedProduct() {
    this.approvedProducts = [];
    this.assetsService.getProductList().subscribe(data => {
      this.approvedProducts = data;
    });
  }

  cancel() {
    this.router.navigate(['gateway-list']);
  }

  onSubmit(data) {
    console.log('Uppdate Form Values');
    console.log(this.deviceEditForm.value);
    if (!this.deviceEditForm.valid) {
      this.validateAllFormFields(this.deviceEditForm);
    } else {
      this.loading = true;
      this.gatewayListService.batchUpdateDevice(this.deviceEditForm.value).subscribe(data => {
        this.loading = false;
        this.toastr.success('Device updated successfully', 'Success', { toastComponent: CustomSuccessToastrComponent });
        this.router.navigate(['gateway-list']);
      }, error => {
        this.loading = false;
        console.log(error);
      });
    }

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

  get f() { return this.deviceEditForm.controls; }

  onChangePurchaseBy(accountNumber) {
    // const orgs =  this.companies.filter(x => x.accountNumber === accountNumber);
    // if(orgs.length > 0 ) {
    //   const obj = orgs[0].organisationRole.find(item => (item === 'END_CUSTOMER' || item === 'ENDCUSTOMER' || item === 'EndCustomer'));
    //     if (obj) {
    //       this.deviceEditForm.get('can').setValue(accountNumber);
    //     }
    // }
  }

}
