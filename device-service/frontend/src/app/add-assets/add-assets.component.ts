import { Component, OnInit, ViewChild } from '@angular/core';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';
import { Asset } from '../models/asset';
import { AssetsService } from '../assets.service';
import { ToastrService } from 'ngx-toastr';
import { ActivatedRoute, Router } from '@angular/router';
import { CompaniesService } from '../companies.service';
import { IDropdownSettings } from 'ng-multiselect-dropdown/multiselect.model';
import { setDropDownSetting } from '../util/constants';
import { ValidatorUtils } from '../util/validatorUtils';
import { CustomErrorToastrComponent } from '../custom-error-toastr/custom-error-toastr.component';
import { CustomSuccessToastrComponent } from '../custom-success-toastr/custom-success-toastr.component';
import { MatSelect } from '@angular/material';
import { CustomBatchErrorToastrComponent } from '../custom-batch-error-toastr/custom-batch-error-toastr.component';
import { CommonData } from '../constant/common-data';
declare var $: any;
@Component({
  selector: 'app-add-assets',
  templateUrl: './add-assets.component.html',
  styleUrls: ['./add-assets.component.css']
})
export class AddAssetsComponent implements OnInit {


  get f() { return this.registerForm.controls; }
  @ViewChild('select') select: MatSelect;
  loading = false;
  registerForm: FormGroup;
  assets: Asset = new Asset();
  numberValidator = '^[0-9]*$';
  spaceValidator = '^[-a-zA-Z0-9-()]+([-a-zA-Z0-9-() ]+)*';
  updateFlag = false;
  companies: any = [];
  isSuperAdmin: any;
  isRoleCustomerManagerInstaller: any;
  companiesDetails: any;
  approvedProductDropdown: IDropdownSettings;
  assetType1Dropdown: IDropdownSettings;
  validatorUtil: ValidatorUtils = new ValidatorUtils();
  isContainerType1 = true;
  buttonDisabled = false;
  cancalButton = false;
  assetType1;
  vinValidation: boolean;
  approvedProducts;
  vinRequiredMsg;
  isDelete = false;
  ids;
  assetsUuid: any;
  assetsData: any;
  organisationId: any;
  rolePCTUser: any;
  organisationData: any;
  roleCallCenterUser: any;
  doorType = CommonData.DOOR_TYPE;
  constructor(
    private readonly formBuldier: FormBuilder,
    public assetsService: AssetsService,
    private readonly toastr: ToastrService,
    private readonly router: Router,
    public companiesService: CompaniesService,
    private readonly activateRoute: ActivatedRoute) {
    this.roleCallCenterUser = JSON.parse(sessionStorage.getItem('IS_ROLE_CALL_CENTER_USER'));
    this.activateRoute.queryParams.subscribe(params => {
      this.assetsUuid = params.id;
      this.organisationId = params.organisationId;
    });
  }
  ngOnInit() {
    this.approvedProductDropdown = setDropDownSetting('id', 'dataType', true, false);
    this.isSuperAdmin = sessionStorage.getItem('IS_ROLE_SUPERADMIN');
    this.companiesDetails = sessionStorage.getItem('companies');
    this.isRoleCustomerManagerInstaller = sessionStorage.getItem('IS_ROLE_CUSTOMER_MANAGER_INSTALLER');
    this.rolePCTUser = JSON.parse(sessionStorage.getItem('IS_ROLE_PCT_USER'));


    if (this.assetsUuid) {
      this.updateFlag = true;
    }
    this.initializeFormControl();
    this.getAssetTypeORApprovedProduct();
  }
  initializeFormControl(data?: any) {
    let productApproved: any;
    if (this.assetsData && this.assetsData.eligible_gateway) {
      productApproved = this.approvedProducts[this.approvedProducts.findIndex(product => product.value === this.assetsData.eligible_gateway)];
    }
    let assetsType: any;
    if (this.assetsData && this.assetsData.eligible_gateway) {
      assetsType = this.assetType1[this.assetType1.findIndex(type => type.display_label === this.assetsData.category)];
    }
    if (data) {
      this.vinValidation = data.is_vin_validated;
    }

    this.registerForm = this.formBuldier.group({
      assetId: [{ value: data && data.assigned_name ? data.assigned_name : '', disabled: data && data.assigned_name ? true : false }, Validators.compose([Validators.required])],
      eligible_gateway: [productApproved ? productApproved : '', Validators.compose([Validators.required])],
      vin: [data && data.vin ? data.vin : '', Validators.compose([Validators.minLength(17), Validators.maxLength(17)])],
      category: [assetsType ? assetsType : '', Validators.compose([Validators.required])],
      year: [data && data.manufacturer_details && data.manufacturer_details.year ? data.manufacturer_details.year : '',
      Validators.compose([Validators.minLength(4), Validators.maxLength(4), Validators.pattern(this.validatorUtil.numberValidator)])],
      manufacturer: [data && data.manufacturer_details && data.manufacturer_details.make ? data.manufacturer_details.make : ''],
      comment: [data && data.comment ? data.comment : ''], // disabled: true
      company: [data && data.can ? data.can : '', Validators.compose([Validators.required])],
      assetNickName: [data && data.asset_nick_name ? data.asset_nick_name : ''],
      noOfTires: [data && data.no_of_tires ? data.no_of_tires : ''],
      noOfAxel: [data && data.no_of_axel ? data.no_of_axel : ''],
      externalLength: [data && data.external_length ? data.external_length : ''],
      doorType: [data && data.door_type ? data.door_type : ''],
      tag: [data && data.tag ? data.tag : ''],
      isActive: [1],
    });
  }

  getOrganisationById(accoutnNumber: any) {
    this.loading = true;
    this.companiesService.findCompanyByAccount(accoutnNumber).subscribe(data => {
      this.loading = false;
      if (data && data.type && data.type === 'CUSTOMER') {
        data.type = 'Customer';
      } else if (data && data.type && data.type === 'INSTALLER') {
        data.type = 'Installer';
      }
      this.organisationData = data;
    }, error => {
      this.loading = false;
      console.log(error);
    });
  }

  getAssetsByUuid() {
    this.loading = true;
    this.assetsService.getAssetById(this.assetsUuid).subscribe(data => {
      this.loading = false;
      this.assetsData = data;
      this.getOrganisationById(data.can);
      this.initializeFormControl(data);
    },
      error => {
        this.loading = false;
        console.log(error);
      });
  }
  getAssetTypeORApprovedProduct() {
    this.loading = true;
    this.assetsService.getAssetTypeAndApprovedProduct().subscribe(data => {
      this.assetType1 = data.body.ASSET_TYPE_1;
      this.approvedProducts = data.body.APPROVED_PRODUCT;
      this.getAllcompanies();
    },
      error => {
        this.loading = false;
        console.log(error);
      });
  }
  getAllcompanies() {
    this.loading = true;
    let type: string;
    if (this.isSuperAdmin) {
      type = 'CUSTOMER';
    }
    this.companiesService.getAllCompanyList().subscribe(
      data => {
        this.loading = false;
        if (this.roleCallCenterUser) {
          const obj = data.find(item => item.accountNumber === 'A-00243');
          this.companies.push(obj);
        } else {
          this.companies = data;
        }
        if (this.assetsUuid) {
          this.getAssetsByUuid();
        }
      },
      error => {
        this.loading = false;
        console.log(error);
      }

    );
  }
  cancel() {
    this.cancalButton = true;
    if (this.updateFlag && this.assetsService.routeflag) {
      this.router.navigate(['installation-details']);
      return;
    }
    if (this.updateFlag) {
      this.router.navigate(['assets'], { queryParams: { id: this.organisationId } });
      return;
    }
    if (sessionStorage.getItem('beforeAssetCancelUrl').includes('/assets')) {
      this.router.navigate(['assets']);
      return;
    }
    this.router.navigate(['customerAssets']);
  }
  delete() {
    this.isDelete = true;
  }
  onCancel() {
    this.isDelete = false;
  }
  onSubmit(registerForm) {
    if (this.isDelete) {
      return;
    }
    if (this.cancalButton) {
      return;
    }
    this.assets.eligible_gateway = registerForm.controls.eligible_gateway.value.value;
    this.assets.category = registerForm.controls.category.value.value;
    this.assets.assigned_name = registerForm.controls.assetId.value;
    this.assets.year = registerForm.controls.year.value;
    this.assets.vin = registerForm.controls.vin.value;
    this.assets.manufacturer = registerForm.controls.manufacturer.value;
    this.assets.comment = registerForm.controls.comment.value;
    this.assets.no_of_tires = registerForm.controls.noOfTires.value;
    this.assets.no_of_axel = registerForm.controls.noOfAxel.value;
    this.assets.external_length = registerForm.controls.externalLength.value;
    this.assets.asset_nick_name = registerForm.controls.assetNickName.value;
    this.assets.tag = registerForm.controls.tag.value;
    this.assets.door_type = registerForm.controls.doorType.value;
    if (this.organisationData) {
      this.assets.company = {
        account_number: registerForm.controls.company.value,
        type: this.organisationData.type,
        id: this.organisationData.id
      };
    } else {
      this.assets.company = {
        account_number: registerForm.controls.company.value,
      };
    }

    if (this.assetsData) {
      this.assets.id = this.assetsData.id;
      this.assets.uuid = this.assetsData.asset_uuid;
      this.buttonDisabled = true;
      this.assetsService.updateAsset(this.assets).subscribe(
        data => {
          this.buttonDisabled = false;
          if (!data.body.assetPayload.vin_validated) {
            const errorTitleArray = Object.keys(data.body.errors);
            let totalErrorDevices = 0;
            let finalErrorString = '';
            for (const iterator of errorTitleArray) {
              finalErrorString += '<br><b>' + iterator + '</b>';
              finalErrorString += '<br><ul>';
              totalErrorDevices++;
            }
            const finalErrorTitle = totalErrorDevices + ' Errors';
            if (data.body.errors !== undefined && data.body.errors !== null && data.body.errors.length > 0) {
              this.toastr.error(finalErrorString, finalErrorTitle, { toastComponent: CustomBatchErrorToastrComponent, tapToDismiss: true, disableTimeOut: true, enableHtml: true }); this.assetsService.assetCompanyId = this.assets.company.id;
            } else {
              this.toastr.success('Asset Updated successfully', null, { toastComponent: CustomSuccessToastrComponent });
            }
            this.vinValidation = false;
            this.updateFlag = true;
            this.registerForm.patchValue({ comment: data.body.assetPayload.comment });
            this.router.navigate(['assets']);
          } else {
            this.vinValidation = data.is_vin_validated;
            this.toastr.success('Asset Updated successfully', null, { toastComponent: CustomSuccessToastrComponent });
            this.router.navigate(['assets'], { queryParams: { id: this.organisationId } });
          }
        }, error => {
          this.buttonDisabled = false;
          console.log(error);
        }

      );
    } else {
      this.buttonDisabled = true;
      this.assetsService.addAsset(this.assets).subscribe(
        data => {
          this.buttonDisabled = false;
          if (!data.body.assetPayload.vin_validated) {
            const errorTitleArray = Object.keys(data.body.errors);
            let totalErrorDevices = 0;
            let finalErrorString = '';
            for (const iterator of errorTitleArray) {
              finalErrorString += '<br><b>' + iterator + '</b>';
              finalErrorString += '<br><ul>';
              totalErrorDevices++;
            }
            const finalErrorTitle = totalErrorDevices + ' Errors';
            if (errorTitleArray && errorTitleArray.length > 0) {
              this.toastr.error(finalErrorString, finalErrorTitle, { toastComponent: CustomBatchErrorToastrComponent, tapToDismiss: true, disableTimeOut: true, enableHtml: true });
            } else {
              this.toastr.success(data.message ? data.message : 'Asset added successfully', null, { toastComponent: CustomSuccessToastrComponent });
            }
            this.vinValidation = false;
            this.updateFlag = true;
            this.registerForm.patchValue({ comment: data.body.assetPayload.comment });
            this.router.navigate(['assets']);

          } else {
            this.toastr.success(data.message, null, { toastComponent: CustomSuccessToastrComponent });
            this.router.navigate(['assets'], { queryParams: { id: this.organisationId } });
          }
        }, error => {
          console.log(error);
          this.buttonDisabled = false;

        }

      );

    }
  }

  onChangeEvent(event: any) {
    let assetIdTrimed = null;
    const assetId = event.target.value;
    if (assetId && assetId !== '') {
      assetIdTrimed = assetId.replace(/ {2,}/g, ' ').trim();
    }

    if (!assetIdTrimed || assetIdTrimed === '') {
      this.registerForm.patchValue({ assetId: null });
      this.registerForm.get('assetId').setValidators([Validators.required]);
      this.registerForm.get('assetId').updateValueAndValidity();
    }
  }
  onTypeSelect(event) {
    // if (this.registerForm.controls.assetType1.value.display_label === "Container") {
    //   const vin = this.registerForm.get('vin');
    //   vin.clearValidators();
    //   vin.updateValueAndValidity();
    //   this.isContainerType1 = true;
    // } else {
    //   const vin = this.registerForm.get('vin');
    //   vin.setValidators(Validators.required);
    //   vin.updateValueAndValidity();
    //   this.isContainerType1 = false;

    //   this.vinRequiredMsg = "VIN is required for Asset Type " + this.registerForm.controls.assetType1.value.display_label;
    // }

  }
  deleteAssetById() {
    if (this.assetsData && this.assetsData.asset_uuid) {
      this.assetsService.deleteassetById([this.assetsData.asset_uuid]).subscribe(data => {
        if (data.body.length === 0) {
          this.toastr.success('Asset Deleted successfully', null, { toastComponent: CustomSuccessToastrComponent });
          // this.router.navigate(['assets'], { queryParams: { id: this.assetsUuid } });
          this.router.navigate(['assets']);
        } else {
          this.toastr.error('This Asset already has hardware installed, and so it cannot be deleted.', 'Problem with Deletion', { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
        }
      }, error => {
        console.log(error);
      });
    } else {
      this.toastr.success('Asset can not be Deleted.', null, { toastComponent: CustomSuccessToastrComponent });
    }
  }

}
