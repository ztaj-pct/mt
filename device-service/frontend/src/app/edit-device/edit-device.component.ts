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
import { CustomErrorToastrComponent } from '../custom-error-toastr/custom-error-toastr.component';
@Component({
  selector: 'app-edit-device',
  templateUrl: './edit-device.component.html',
  styleUrls: ['./edit-device.component.css']
})
export class EditDeviceComponent implements OnInit {

  loading = false;
  deviceId: any;
  submitted = false;
  companies: any[] = [];
  approvedProducts: any[] = [];
  deviceEditForm: FormGroup;
  usage: any[] = CommonData.USAGE_DATA;
  serviceNetwork: any[] = CommonData.TELECOM_PROVIDER_DATA;
  hardwareType: any[] = CommonData.HARDWARE_TYPE;
  countryCodeList: any[] = CountryCode.COUNTRY_CODE_LIST;
  assetType: any[] = CommonData.ASSET_CATEGORY;
  productCode: any[] = CommonData.PRODUCT_CODE;
  validatorUtil: ValidatorUtils = new ValidatorUtils();
  rolePCTUser: any;
  cellularDisablel = false;
  isRolePctUser: any;
  constructor(
    private readonly formBuilder: FormBuilder,
    private readonly companiesService: CompaniesService,
    private readonly assetsService: AssetsService,
    private readonly gatewayListService: GatewayListService,
    private toastr: ToastrService,
    private readonly router: Router,
    private location: Location,
    private readonly activateRoute: ActivatedRoute) {

    this.isRolePctUser = JSON.parse(sessionStorage.getItem('IS_ROLE_PCT_USER'));

    if (this.isRolePctUser) {
      this.location.back();
    }
    this.activateRoute.queryParams.subscribe(params => {
      this.deviceId = params.id;
    });
    this.getAllcompanies();
    this.getAssetTypeORApprovedProduct();
    if (this.deviceId) {
      this.getAssetDetails(this.deviceId);
    }
  }

  ngOnInit() {
    this.initializeFormControl();
    this.rolePCTUser = JSON.parse(sessionStorage.getItem('IS_ROLE_PCT_USER'));
  }

  initializeFormControl(data?: any) {
    if (data && data.uuid && data.uuid !== '') {
      this.cellularDisablel = true;
    }
    this.deviceEditForm = this.formBuilder.group({
      asset_payload: this.formBuilder.group({
        assetId: [data && data.assets_detail_payload && data.assets_detail_payload.asset_id ? data.assets_detail_payload.asset_id : ''],
        asset_name: [data && data.assets_detail_payload && data.assets_detail_payload.asset_name ? data.assets_detail_payload.asset_name : ''],
        asset_type: [data && data.assets_detail_payload && data.assets_detail_payload.asset_type ? data.assets_detail_payload.asset_type : ''],
        manufacturer_name: [data && data.assets_detail_payload && data.assets_detail_payload.manufacturer_name ? data.assets_detail_payload.manufacturer_name : ''],
        year: [data && data.assets_detail_payload && data.assets_detail_payload.year ? data.assets_detail_payload.year : ''],
        id: [data && data.assets_detail_payload && data.assets_detail_payload.asset_uniq_id ? data.assets_detail_payload.asset_uniq_id : ''],
        vin: [data && data.assets_detail_payload && data.assets_detail_payload.vin ? data.assets_detail_payload.vin : '', Validators.compose([Validators.pattern(this.validatorUtil.vinValidator), Validators.minLength(17), Validators.maxLength(17)])]
      }),

      imei: [data && data.imei ? data.imei : ''],
      can: [data && data.can ? data.can : '', Validators.compose([Validators.required])],
      product_name: [data && data.product_name ? data.product_name : ''],
      product_code: [data && data.product_code ? data.product_code : ''],
      usage_status: [data && data.usage ? data.usage.toLowerCase() : '', Validators.compose([Validators.required])],
      uuid: [data && data.uuid ? data.uuid : null],
      mac_address: [data && data.mac_address ? data.mac_address : ''],
      purchase_by: [data && data.purchase_by ? data.purchase_by : ''],
      installed_by: [data && data.installed_by ? data.installed_by : ''],
      hardware_id: [data && data.hardware_id ? data.hardware_id : ''],
      hardware_type: [data && data.hardware_type ? data.hardware_type : ''],
      is_active: [data && data.is_active ? data.is_active : false],

      cellular_payload: this.formBuilder.group({
        service_network: [data && data.cellular_payload.service_network ? data.cellular_payload.service_network : '', Validators.compose([Validators.required])],
        cellular: [data && data.cellular_payload && data.cellular_payload.cellular ? data.cellular_payload.cellular : '', Validators.compose([Validators.required])],
        imsi: [data && data.cellular_payload && data.cellular_payload.imsi ? data.cellular_payload.imsi : ''],
        country_code: [data && data.cellular_payload && data.cellular_payload.country_code ? data.cellular_payload.country_code : ''],
        phone: [data && data.cellular_payload && data.cellular_payload.phone ? data.cellular_payload.phone : '', Validators.compose([Validators.pattern(this.validatorUtil.phoneNumberValidator), Validators.minLength(10), Validators.maxLength(10)])],
        uuid: [data && data.cellular_payload && data.cellular_payload.uuid ? data.cellular_payload.uuid : ''],
      })
    });
  }

  endCustomer: any = [];
  installedBy: any = [];
  purchasedBy: any = [];

  getAllcompanies() {
    this.companiesService.getAllOrgList().subscribe(
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
            const obj3 = iterator.organisationRole.find(item => (item === 'Reseller' || item === 'RESELLER'));
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
      // const productList = ['ChassisNet', 'TrailerNet', 'Smart7', 'Sabre', 'StealthNet', 'SmartPair', 'Smart7 Lid'];
      // for (const iterator of productList) {
      //   const obj = data.find(item => item.product_name.toLowerCase() === iterator.toLocaleLowerCase());
      //   if (obj) {
      //     this.approvedProducts.push(obj);
      //   }
      // }
      this.approvedProducts = data;
    });
  }

  getAssetDetails(id: any) {
    this.gatewayListService.getAssetStatus(id).subscribe(data => {
      if (data && data.body && data.body.length > 0) {
        this.initializeFormControl(data.body[0]);
      }
    }, error => {
      console.log(error);
    });
  }

  cancel() {
    this.router.navigate(['gateway-profile']);
  }

  onSubmit(data) {
    console.log('Uppdate Form Values');
    console.log(this.deviceEditForm.value);
    if (!this.deviceEditForm.valid) {
      this.validateAllFormFields(this.deviceEditForm);
    } else {
      if (this.deviceEditForm.value.asset_payload.asset_name || this.deviceEditForm.value.asset_payload.asset_type || this.deviceEditForm.value.asset_payload.manufacturer_name
        || this.deviceEditForm.value.asset_payload.year || this.deviceEditForm.value.asset_payload.vin) {
        if (!(this.deviceEditForm.value.asset_payload.asset_name && this.deviceEditForm.value.asset_payload.asset_type )) {
          this.toastr.success('Please Fill All Asset Details', 'Error', { toastComponent: CustomErrorToastrComponent });
          return;
        }
      }
      this.loading = true;
      if (this.deviceEditForm.value.uuid == null || this.deviceEditForm.value.uuid === '') {
        this.gatewayListService.addDevice(this.deviceEditForm.value).subscribe(data => {
          this.loading = false;
          this.toastr.success('Device updated successfully', 'Success', { toastComponent: CustomSuccessToastrComponent });
          this.router.navigate(['gateway-profile']);
        }, error => {
          this.loading = false;
          console.log(error);
        });
      } else {
        this.gatewayListService.updateDevice(this.deviceEditForm.value).subscribe(data => {
          this.loading = false;
          this.toastr.success('Device updated successfully', 'Success', { toastComponent: CustomSuccessToastrComponent });
          this.router.navigate(['gateway-profile']);
        }, error => {
          this.loading = false;
          console.log(error);
        });
      }
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

  get f1() {
    const assets: any = this.deviceEditForm.controls.asset_payload;
    return assets.controls;
  }

  get f2() {
    const cellular_payload: any = this.deviceEditForm.controls.cellular_payload;
    return cellular_payload.controls;
  }

}
