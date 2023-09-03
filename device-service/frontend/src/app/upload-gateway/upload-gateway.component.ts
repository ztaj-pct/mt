import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, Validators, FormArray } from '@angular/forms';
import { Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import * as XLSX from 'xlsx';
import { AssetsService } from '../assets.service';
import { CompaniesService } from '../companies.service';
import { CustomBatchErrorToastrComponent } from '../custom-batch-error-toastr/custom-batch-error-toastr.component';
import { CustomErrorToastrComponent } from '../custom-error-toastr/custom-error-toastr.component';
import { CustomSuccessToastrComponent } from '../custom-success-toastr/custom-success-toastr.component';
import { UploadGateway, uploadGatewayForChecking } from '../models/gateway';
import { CompanyGateway } from '../models/companyGateway';
import { UpdateGatewayService } from '../update-gateway/update-gateway.service';
import { GatewayListService } from '../gateway-list.service';
import { CommonData } from '../constant/common-data';
import { ValidatorUtils } from '../util/validatorUtils';
declare var $: any;

@Component({
  selector: 'app-upload-gateway',
  templateUrl: './upload-gateway.component.html',
  styleUrls: ['./upload-gateway.component.css']
})
export class UploadGatewayComponent implements OnInit {
  validatorsUtils:ValidatorUtils;
  isSuperAdmin: any;
  devicesToBeValid: Array<any> = [];
  devicesToBeSaved: Array<any> = [];
  companiesDetails: any;
  companies: any;
  data: Array<any> = [];
  loading = false;
  selectedFiles: FileList;
  uploadDevicesForm: FormGroup;
  currentFileUpload: File;
  resError = false;
  resSuccess = false;
  updateFlag = false;
  submitted = false;
  gatewayInfoWSname: string;
  approvedProducts;
  companyGateway: CompanyGateway = new CompanyGateway();
  selectedFileName;
  modified = false;
  fileSelected: boolean;
  isRoleCustomerManagerInstaller: any;
  uploadGatewayForChecking: uploadGatewayForChecking;
  uploadGateway: UploadGateway;
  files: any[] = [];
  usage: any[] = CommonData.USAGE_DATA;
  serviceNetwork: any[] = CommonData.TELECOM_PROVIDER_DATA;
  is_all_imei = false;
  is_all_mac_address = false;
  rolePCTUser: any;
  buttonDisabled = false;
  heading = []; // for headings
  errorMessage = 'Invalid data';
  errorHeading = 'Error';
   pattern = /^\d+$/;
  deviceIdList: any[] = [];
  deviceForwardingRuleList: any[] = [];
  isEnd = false;
  public deviceForwordingRuleData: any[] = CommonData.DEVICE_FORWORDING_RULE_DATA;
  isReadonly = false;

  customerForwardingRules: any[] = [];
  customerForwardingRuleUrls: any[] = [];
  perchaseByList: any[];
  endCustomerUuid: any = '';
  purchasedByUuid: any = '';

  otherRelationshipTypes: any[] = [{
    id: 1,
    name: 'type 1'
  }, {
    id: 2,
    name: 'type 2'
  }, {
    id: 3,
    name: 'type 3'
  }];
  otherRelationshipValues: any[] = [{
    id: 1,
    name: 'value 1'
  }, {
    id: 2,
    name: 'value 2'
  }, {
    id: 3,
    name: 'value 3'
  }];

  constructor(
    private readonly gatewayListService: GatewayListService,
    private readonly gatewaysService: UpdateGatewayService,
    private readonly router: Router,
    private readonly companiesService: CompaniesService,
    private readonly formBuldier: FormBuilder,
    private readonly toastr: ToastrService, public assetsService: AssetsService) {
    this.isSuperAdmin = sessionStorage.getItem('IS_ROLE_SUPERADMIN');
    this.isRoleCustomerManagerInstaller = sessionStorage.getItem('IS_ROLE_CUSTOMER_MANAGER_INSTALLER');
    this.companiesDetails = sessionStorage.getItem('companies');
    this.rolePCTUser = JSON.parse(sessionStorage.getItem('IS_ROLE_PCT_USER'));
    this.initilizeForm();
    this.getAllCustomer();
    this.getAllCustomerAndResaller();
    this.validatorsUtils= new ValidatorUtils();
  }

  ngOnInit() {
    this.getAssetTypeORApprovedProduct();
    this.getAllCustomerForwardingRuleUrl();
    // this.errorHeading='Invalid File Formate';
    // this.errorMessage='The Device List data file must be in .csv format. Please check your file and try again.';
    // setTimeout(()=>{
    //   $('#AssociationMessageModel').modal('show');
    // },1000);
  }

  initilizeForm() {
    this.uploadDevicesForm = this.formBuldier.group({
      file: [''],
      company: ['', Validators.compose([Validators.required])],
      isProductIsApprovedForAsset: ['', Validators.compose([Validators.required])],
      salesforceOrderId: ['', Validators.compose([Validators.required])],
      telecomProvider: [''],
      product_code: ['', Validators.compose([Validators.required])],
      usage: ['production', Validators.compose([Validators.required])],
      forwarding_list: this.formBuldier.array([]),
      imei_ids: [''],
      purchased_by: ['', Validators.compose([Validators.required])],
      //other_relationships: this.formBuldier.array([]),
      ignore_forwarding_rules: this.formBuldier.array([])
    });

    //this.addAnotherRelationships();

    if (this.deviceForwardingRuleList.length <= 0) {
      //this.addAnotherRule('', '', null, 0);
      this.isReadonly = true;
    }
  }
  getAllCustomer() {
    this.companiesService.getAllActiveByOrganisationRoles(['CUSTOMER']).subscribe(
      data => {
        // if (data !== undefined && data != null && data.length > 0) {
        this.companies = data.body;
        //}
      },
      error => {
        console.log(error);
      }
    );
  }
  selectFile(event) {
    const file = event.target.files.item(0);
    this.selectedFiles = event.target.files;
  }

  selectChangeHandler(event: any, i: number) {
    let isValid = true;
    if (this.forwarding_list.value.length > 1) {
      for (let index = 0; index < this.forwarding_list.value.length; index++) {
        if (index !== i) {
          if (this.forwarding_list.value[index].forwarding_rule_url_uuid === event.value) {
            this.forwarding_list.controls[i].patchValue({
              type: '',
              forwarding_rule_url_uuid: ''
            });
            this.toastr.error('Already selected endpoint, Please select another one', 'Error', { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: false });
            isValid = false;
            break;
          }
        }
      }
    }
    if (isValid) {
      const items = this.customerForwardingRuleUrls.filter(r => r.uuid == event.value);
      if (items.length > 0) {
        this.forwarding_list.value[i].url = items[0].endpointDestination;
        this.forwarding_list.value[i].description = items[0].description;
        this.forwarding_list.value[i].type = CommonData.DATA_FORWORDING_RULE_FORMAT_MAPPING[items[0].format];
        this.forwarding_list.value[i].format = items[0].format;
      }
    }
  }

  get forwarding_list(): FormArray {
    return this.uploadDevicesForm.get('forwarding_list') as FormArray;
  }

  addAnotherRule(type: any, url: any, uuId: any, id: any) {

    if (this.forwarding_list.controls.length <= 4) {
      this.forwarding_list.push(this.newRuleFormGroup(type, url, uuId, id));
    }
    if (this.forwarding_list.controls.length >= (5 - this.deviceForwardingRuleList.length)) {
      this.isEnd = true;
    }
  }

  newRuleFormGroup(type: any, url: any, uuId: any, id: any): FormGroup {
    return this.formBuldier.group({
      type: [type],
      // , Validators.compose([Validators.required])
      forwarding_rule_url_uuid: ['', Validators.compose([Validators.required])],
      url: [url],
      //, Validators.compose([Validators.required])
      uuid: [uuId ? uuId : null],
      id: [id ? id : 0],
      description: [null]
    });
  }

  get g() {
    return this.uploadDevicesForm.controls;
  }

  cancel() {
    this.uploadDevicesForm.reset();
    this.router.navigate(['gateway-list']);
  }

  getAssetTypeORApprovedProduct() {
    this.approvedProducts = [];
    this.assetsService.getProductList().subscribe(data => {
      const productList = ['ChassisNet', 'TrailerNet', 'Smart7', 'Sabre', 'StealthNet', 'SmartPair', 'Smart7 Lid'];
      for (const iterator of productList) {
        const obj = data.find(item => item.product_name.toLowerCase() === iterator.toLocaleLowerCase());
        if (obj) {
          obj.name = iterator;
          this.approvedProducts.push(obj);
        }
      }
      // this.approvedProducts = data;
    });
  }

  rejectData() {
    this.devicesToBeSaved = [];
    this.devicesToBeValid = [];
    this.loading = false;
    this.uploadDevicesForm.controls.file.reset();
    this.uploadDevicesForm.reset();
    return;
  }

  onFileChange($event) {
    if (!this.uploadDevicesForm.controls.isProductIsApprovedForAsset.value) {
      this.toastr.error('Please select Product Type', null, { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
      this.clearUploadData();
      return;
    }

    this.fileSelected = true;
    this.selectedFileName = $event[0].name;

    const target: DataTransfer = ($event) as DataTransfer;
    if ($event.length !== 1) {
      throw new Error('Cannot use multiple files');
    }
    this.fileSelected = true;
    // this.selectedFileName = $event.files[0].name;
    if (this.selectedFileName.split(/\.(?=[^\.]+$)/)[1] !== 'csv') {
      // this.toastr.error('The Device List data file must be in .csv format. Please check your file and try again.', 'Incorrect File Format', { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
      // return;
         this.errorHeading='Invalid File Formate';
         this.errorMessage='The Device List data file must be in .csv format. Please check your file and try again.';
         $('#AssociationMessageModel').modal('show');
         this.clearUploadData();
         
    }
    const reader: FileReader = new FileReader();
    reader.onload = (e: any) => {
      /* read workbook */
      const bstr: string = e.target.result;
      const wb: XLSX.WorkBook = XLSX.read(bstr, { type: 'binary' });

      /* grab first sheet */
      const wsname: string = wb.SheetNames[0];
      const ws: XLSX.WorkSheet = wb.Sheets[wsname];

      /* save data */
      this.data = (XLSX.utils.sheet_to_json(ws, { header: 'A' , raw : false}));
      const header = this.data[1];
      this.gatewayInfoWSname = wb.SheetNames[0];
      this.checkDataIsImeiOrMacAddress();
      const headerKeys = Object.keys(header);

    };

    reader.readAsBinaryString($event[0]);

  }

  checkDataIsImeiOrMacAddress() {
    this.devicesToBeSaved = [];
    this.devicesToBeValid = [];
    if (this.data && this.data.length > 1) {
      if ((this.data[0].A == undefined || this.data[0].B == undefined || this.data[0].C == undefined) || this.data[0].A.toLowerCase() !== 'device id' && this.data[0].B.toLowerCase() !== 'serial' && this.data[0].C.toLowerCase() !== 'sim') {
        this.errorHeading='Invalid File Format';
        this.errorMessage='The Device List data file must be in .csv format. Please check your file and try again.';
        $('#AssociationMessageModel').modal('show');
        this.clearUploadData();
        
        // this.toastr.error('The uploaded file does not follow the expected data format. Please upload a CSV file with three cloumn Device ID, Serial, SIM',
        //   'Incorrect File Format', { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
        //this.clearUploadData();
        //return;
      }
      for (let i = 1; i < this.data.length; i++) {
        this.devicesToBeValid.push(this.data[i].A);
        if(this.data[i].B == undefined || this.data[i].B === null){
         // return this.toastr.error('<b>Missing serial List:</b> The following records are missing serial numbers. Please check the file and try again.', 'Error', { toastComponent: CustomBatchErrorToastrComponent, tapToDismiss: true, disableTimeOut: true, enableHtml: true });
         this.errorHeading='Missing Serial Number';
         this.errorMessage='The following records are missing serial numbers. Please check the file and try again';
         $('#AssociationMessageModel').modal('show');
         this.clearUploadData();
         break;
        }
        else if(this.data[i].C == undefined ||this.data[i].C=== null ){
         // return this.toastr.error('<b>Missing sim List:</b> The following records are missing SIM numbers. Please check the file and try again.', 'Error', { toastComponent: CustomBatchErrorToastrComponent, tapToDismiss: true, disableTimeOut: true, enableHtml: true });
         this.errorHeading='Missing SIM Number';
         this.errorMessage='The following records are missing SIM numbers. Please check the file and try again.';
         $('#AssociationMessageModel').modal('show');
         this.clearUploadData();
         break;
        }

        const alreadyPresent = this.devicesToBeSaved.find((device: { sim: string; }) => device.sim === this.data[i].C );
        if(alreadyPresent!==undefined){
        // return  this.toastr.error('<b>already Exist:</b> The following records have existing serial numbers. They will be updated with the details in the file. ', 'Error', { toastComponent: CustomBatchErrorToastrComponent, tapToDismiss: true, disableTimeOut: true, enableHtml: true });
        this.errorHeading='SIM Number Already Exist';
        this.errorMessage='The following records have existing SIM numbers. They will be updated with the details in the file. ';
        $('#AssociationMessageModel').modal('show');
       // this.clearUploadData();
        }

        else if(this.data[i].C.length < 18 || this.data[i].C.length > 22  || isNaN(this.data[i].C)){
         //return this.toastr.error('<b>Invalid Format:</b> The following records have invalid SIM numbers. Please check that these values are numeric and between xxxxxx', 'Error', { toastComponent: CustomBatchErrorToastrComponent, tapToDismiss: true, disableTimeOut: true, enableHtml: true });
          this.errorHeading='Invalid Format';
          this.errorMessage='The following records have invalid SIM numbers. Please check that these values are numeric and between 18 and 22 digits long and try again. ';
          $('#AssociationMessageModel').modal('show');
         this.clearUploadData();
          break;
        }
        const alreadyExist = this.devicesToBeSaved.find((device: { serial: string; }) => device.serial === this.data[i].B );
        if(alreadyExist!==undefined){
        // return  this.toastr.error('<b>already Exist:</b> The following records have existing serial numbers. They will be updated with the details in the file. ', 'Error', { toastComponent: CustomBatchErrorToastrComponent, tapToDismiss: true, disableTimeOut: true, enableHtml: true });
        this.errorHeading='Serial Number Already Exist';
        this.errorMessage='The following records have existing serial numbers. They will be updated with the details in the file. ';
        $('#AssociationMessageModel').modal('show');
       // this.clearUploadData();
        
        }
        
          this.devicesToBeSaved.push(
            {
              imei: this.data[i].A,
              serial: this.data[i].B,
              sim: this.data[i].C
            });
      }

      if (this.uploadDevicesForm.controls.isProductIsApprovedForAsset.value) {
        this.uploadGatewayForChecking = new uploadGatewayForChecking();
        this.uploadGatewayForChecking.device_list = this.devicesToBeValid;
        this.uploadGatewayForChecking.is_product_is_approved_for_asset = this.uploadDevicesForm.controls.isProductIsApprovedForAsset.value.product_name;
        //this.checkListOfDeviceHaveImeiOrMacAddress();
      }

    } else {
      // this.toastr.error('File is empty. Please select another file', null, { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
    }
  }

  focusOutFunction() {
    // if (this.uploadDevicesForm.value.imei_ids != undefined 
    //   && this.uploadDevicesForm.value.imei_ids != null 
    //   && this.uploadDevicesForm.value.imei_ids != "" 
    //   && !this.uploadDevicesForm.controls.isProductIsApprovedForAsset.value) {
    //   this.toastr.error('Please select Product Type', null, { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
    //   this.clearUploadData();
    //   return;
    // }

    let imei_list = [];

    if (this.uploadDevicesForm.value.imei_ids != undefined && this.uploadDevicesForm.value.imei_ids != null && this.uploadDevicesForm.value.imei_ids != "") {
      imei_list = this.uploadDevicesForm.value.imei_ids.split(',')
      //}
      this.uploadGatewayForChecking.device_list = imei_list.filter(x => x !== "");
      this.uploadGatewayForChecking.is_product_is_approved_for_asset = this.uploadDevicesForm.controls.isProductIsApprovedForAsset.value.product_name;
      //if(imei_list.length > 0) {
      // this.checkListOfDeviceHaveImeiOrMacAddress();
      //}
    } else if (this.selectedFileName == null) {
      this.uploadGatewayForChecking.device_list = [];
    }

  }

  rejectModifiedData() {
    this.loading = false;
    this.uploadDevicesForm.controls.file.reset();
  }

  clearUploadData() {
    this.uploadDevicesForm.patchValue({ file: null });
    this.selectedFileName = null;
    this.data = [];
    this.devicesToBeSaved = [];
    this.devicesToBeValid = [];
  }

  checkListOfDeviceHaveImeiOrMacAddress() {
    $('#AssociationMessageModel2').modal('show');
    if (!this.uploadDevicesForm.valid) {
      this.clearUploadData();
      this.validateAllFormFields(this.uploadDevicesForm);
      this.validateForwardingList();
      return;
    }
    this.focusOutFunction();

    if (this.uploadGatewayForChecking.device_list == null ||
      this.uploadGatewayForChecking.device_list == undefined ||
      this.uploadGatewayForChecking.device_list.length < 1) {
      this.clearUploadData();
      this.loading = false;
      this.toastr.error('<b>Missing IMEI List:</b> The IMEI List is missing. Please add it and try again.', 'Error', { toastComponent: CustomBatchErrorToastrComponent, tapToDismiss: true, disableTimeOut: true, enableHtml: true });
      return;
    }

    this.loading = true;
    this.uploadGatewayForChecking.device_details = this.devicesToBeSaved;
    this.gatewaysService.checkListOfDeviceHaveImeiOrMacAddress(this.uploadGatewayForChecking).subscribe(data => {
      if (!data.body.status) {

        if (data.body.is_invalid_ids) {
          let finalErrorStringFirst = 'The following device IDs are incorrectly formatted. Please check the file and try again.';
          for (const iterator of data.body.invalid_ids_list) {
            finalErrorStringFirst += '<br>' + iterator;
          }
          const finalErrorTitleFirst = data.body.invalid_ids_list.length + ' Errors';
          this.toastr.error(finalErrorStringFirst, finalErrorTitleFirst, { toastComponent: CustomBatchErrorToastrComponent, tapToDismiss: true, disableTimeOut: true, enableHtml: true });
          this.clearUploadData();
          this.loading = false;
          return;
        }

        if (data.body.gateway_list != null && data.body.gateway_list.length > 0) {
          let finalErrorString = 'The following device IDs do not match the selected product. Please check the file and try again.';
          for (const iterator of data.body.gateway_list) {
            finalErrorString += '<br>' + iterator;
          }

          const finalErrorTitle = data.body.gateway_list.length + ' Errors';
          this.toastr.error(finalErrorString, finalErrorTitle, { toastComponent: CustomBatchErrorToastrComponent, tapToDismiss: true, disableTimeOut: true, enableHtml: true });
          this.loading = false;
          return;
        }
        this.clearUploadData();
        //  this.loading = false;
      } else {
        this.is_all_imei = data.body.is_all_imei;
        this.is_all_mac_address = data.body.is_all_mac_address;
        if (data.body.gateway_list != null && data.body.gateway_list.length > 0) {
          this.devicesToBeValid = data.body.gateway_list;
          let isIntialLengthNotZero = false;
          if (this.devicesToBeSaved.length > 0) {
            isIntialLengthNotZero = true;
          }
          for (let i = 0; i < data.body.gateway_list.length; i++) {
            if (isIntialLengthNotZero) {
              this.devicesToBeSaved[i].imei = data.body.gateway_list[i];
            } else {
              this.devicesToBeSaved.push(
                {
                  imei: data.body.gateway_list[i],
                  sim: null,
                  phone: null
                });
            }


          }
        }
        // if (data.body.gateway_list_which_present_in_db && data.body.gateway_list_which_present_in_db.length > 0) {
        //   let finalErrorString = '<b>Duplicate Device IDs:</b> The following device IDs already exist for this customer. They will be ignored in the import. ';

        //   for (const iterator of data.body.gateway_list_which_present_in_db) {
        //     this.devicesToBeSaved = this.devicesToBeSaved.filter(device => device.imei !== Number(iterator));
        //     finalErrorString += '<br>' + iterator;
        //   }
        //   const finalErrorTitle = data.body.gateway_list_which_present_in_db.length + ' Errors';
        //   this.toastr.error(finalErrorString, finalErrorTitle, { toastComponent: CustomBatchErrorToastrComponent, tapToDismiss: true, disableTimeOut: true, enableHtml: true });
        // }
      }
      //this.loading = false;
      //return;
      if(data.body.sim_no_already_exist != null && data.body.sim_no_already_exist.length > 0) {
        this.loading = false;
        this.deviceIdList = data.body.sim_no_already_exist;
        this.errorHeading='SIM Number Already Exist';
         this.errorMessage='The following records have existing SIM numbers. They will be updated with the details in the file.';
         $('#alreadyExistSimSerialNo').modal('show');
      } else if(data.body.serial_no_already_exist != null && data.body.serial_no_already_exist.length > 0) {
        this.loading = false;
        this.deviceIdList = data.body.serial_no_already_exist;
        this.errorHeading='Serial Number Already Exist';
         this.errorMessage='The following records have existing serial numbers. They will be updated with the details in the file.';
         $('#alreadyExistSimSerialNo').modal('show');
      } else {
        this.save();
      }
    }, error => {
      console.log(error);
      this.loading = false;
    });
  }

  processTosave() {
    this.save();
  }

  onChangeEventForProductApproved(event) {
    this.checkDataIsImeiOrMacAddress();

    if (this.uploadDevicesForm.controls.isProductIsApprovedForAsset.value) {
      this.uploadGatewayForChecking = new uploadGatewayForChecking();

      this.uploadGatewayForChecking.is_product_is_approved_for_asset = this.uploadDevicesForm.controls.isProductIsApprovedForAsset.value.product_name;
      if (this.devicesToBeValid.length > 0) {
        this.uploadGatewayForChecking.device_list = this.devicesToBeValid;
      } else if (this.uploadDevicesForm.value.imei_ids != undefined && this.uploadDevicesForm.value.imei_ids != null && this.uploadDevicesForm.value.imei_ids != "") {
        let imei_list = [];
        imei_list = this.uploadDevicesForm.value.imei_ids.split(',')
        this.uploadGatewayForChecking.device_list = imei_list;
      }
      // if(this.uploadGatewayForChecking != null 
      //   && this.uploadGatewayForChecking != undefined
      //   && this.uploadGatewayForChecking.device_list != null
      //   && this.uploadGatewayForChecking.device_list != undefined
      //   && this.uploadGatewayForChecking.device_list.length > 0) {
      //     this.checkListOfDeviceHaveImeiOrMacAddress();
      //   }
    }
  }

  onFileChangeBrower($event) {

    if (!this.uploadDevicesForm.controls.isProductIsApprovedForAsset.value) {
      this.toastr.error('Please select Product Type', null, { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
      this.clearUploadData();
      return;
    }
    const target: DataTransfer = ($event) as DataTransfer;
    if ($event.target.files.length !== 1) {
      throw new Error('Cannot use multiple files');
    }
    this.fileSelected = true;
    this.selectedFileName = $event.target.files[0].name;
    if (this.selectedFileName.split(/\.(?=[^\.]+$)/)[1] !== 'csv') {
        this.toastr.error('The Device List data file must be in .csv format. Please check your file and try again.', 'Incorrect File Format', { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
       return;
        //  this.errorHeading='Invalid File Formate';
        //  this.errorMessage='The Device List data file must be in .csv format. Please check your file and try again.';
        //  $('#AssociationMessageModel').modal('show');
        //  this.clearUploadData();
         
    }
    const reader: FileReader = new FileReader();
    reader.onload = (e: any) => {
      /* read workbook */
      const bstr: string = e.target.result;
      const wb: XLSX.WorkBook = XLSX.read(bstr, { type: 'binary' });

      /* grab first sheet */
      const wsname: string = wb.SheetNames[0];
      this.gatewayInfoWSname = wb.SheetNames[0];
      const ws: XLSX.WorkSheet = wb.Sheets[wsname];

      /* save data */
      this.data = (XLSX.utils.sheet_to_json(ws, { header: 'A' ,raw:false}));
      const header = this.data[1];
      this.checkDataIsImeiOrMacAddress();
      const headerKeys = Object.keys(header);

    };
    reader.readAsBinaryString($event.target.files[0]);
  }

  /**
   * on file drop handler
   */
  onFileDropped($event) {
    this.onFileChange($event);
  }

  insertModified() {
    this.modified = true;
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

  validateForwardingList() {
    this.forwarding_list.controls.forEach(field => {
      if (field.invalid) {
        field.get("forwarding_rule_url_uuid").markAsTouched({ onlySelf: true });
        //field.get("type").markAsTouched({ onlySelf: true });
      }
    });
  }

  save() {

    if (this.devicesToBeSaved.length < 1) {
      this.clearUploadData();
      this.loading = false;
      this.toastr.error('<b>Missing IMEI List:</b> The IMEI List is missing. Please add it and try again.', 'Error', { toastComponent: CustomBatchErrorToastrComponent, tapToDismiss: true, disableTimeOut: true, enableHtml: true });
      return;
    }

    this.loading = true;
    this.uploadGateway = new UploadGateway();
    this.uploadGateway.gateway_list = this.devicesToBeSaved;
    this.uploadGateway.company = {
      id: this.uploadDevicesForm.controls.company.value.id,
      company_name: this.uploadDevicesForm.controls.company.value.companyName,
      short_name: this.uploadDevicesForm.controls.company.value.shortName,
      type: this.uploadDevicesForm.controls.company.value.type,
      status: this.uploadDevicesForm.controls.company.value.status,
      account_number: this.uploadDevicesForm.controls.company.value.accountNumber,
      is_asset_list_required: this.uploadDevicesForm.controls.company.value.isAssetListRequired,
      uuid: this.uploadDevicesForm.controls.company.value.uuid
    };
    this.uploadGateway.telecom_provider = this.uploadDevicesForm.controls.telecomProvider.value;
    this.uploadGateway.usage_status = this.uploadDevicesForm.controls.usage.value;
    this.uploadGateway.product_master_response = this.uploadDevicesForm.controls.isProductIsApprovedForAsset.value;
    this.uploadGateway.salesforce_order_id = this.uploadDevicesForm.controls.salesforceOrderId.value;
    this.uploadGateway.is_all_imei = this.is_all_imei;
    this.uploadGateway.is_all_mac_address = this.is_all_mac_address;
    this.uploadGateway.product_code = this.uploadDevicesForm.controls.product_code.value;
    this.uploadGateway.forwarding_list = this.uploadDevicesForm.value.forwarding_list;
    let ignore_forwarding_rules: any[] = [];
    for (let index = 0; index < this.uploadDevicesForm.value.ignore_forwarding_rules.length; index++) {
      const element = this.uploadDevicesForm.value.ignore_forwarding_rules[index];
      if (element.ignore) {
        ignore_forwarding_rules.push(element);
      }
    }
    this.uploadGateway.ignore_forwarding_rules = ignore_forwarding_rules;
    //this.uploadGateway.other_relationships = this.uploadDevicesForm.value.other_relationships;
    this.uploadGateway.purchased_by = this.uploadDevicesForm.value.purchased_by;
    this.buttonDisabled = true;
    console.log(" ============================ this.uploadGateway ==========================");
    console.log(this.uploadGateway);
    this.gatewaysService.bulkUpdate(this.uploadGateway).subscribe(data => {
      this.buttonDisabled = false;
      if (data.status) {
        const result = data.body;
        let finalErrorString = '<b>IMEIs not found:</b> The following IMEIs were not found in the database. The most likely reason for this is that they have not yet gone through the QA process.';
        let isNotFoundError = false;
        let finalSuccssString = '<b>IMEIs Successfully update:</b> The following IMEIs Succssfully updated.';
        let successCount = 0;
        let isSuccess = false;
        let notFoundErrorCount = 0;
        let errorStringFromBackend = '';
        for (let index = 0; index < result.length; index++) {
          const element = result[index];
          if (element.status) {
            isSuccess = true;
            finalSuccssString += '<br>' + element.id;
            successCount = successCount + 1;
          } else {
            isNotFoundError = true;
            errorStringFromBackend += '<br>' + element.message + '<br>' + element.id;
            finalErrorString = errorStringFromBackend;
            notFoundErrorCount = notFoundErrorCount + 1;
          }
        }
        if (isNotFoundError) {
          let title = notFoundErrorCount + ' Errors';
          if (isSuccess) {
            title = successCount + ' Successes ' + title;
            finalErrorString = finalSuccssString + '<br>' + finalErrorString;
          }
          this.toastr.error(finalErrorString, title, { toastComponent: CustomBatchErrorToastrComponent, tapToDismiss: true, disableTimeOut: true, enableHtml: true });
          this.clearUploadData();
        } else {
          this.toastr.success('Device(s) uploaded successfully', null, { toastComponent: CustomSuccessToastrComponent });
          this.router.navigate(['gateway-list']);
        }
      } else {
        this.toastr.error('Something went wrong while uploading the gateway.', 'Problem with uploading', { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
      }
      this.loading = false;

    }, error => {
      console.log(error);
      this.buttonDisabled = false;
      this.loading = false;
    });
  }

  onSelectionOrg(event) {
    if (event != null && event != undefined && event.value != null && event.value != undefined) {
      this.endCustomerUuid = event.value.uuid;
      this.getCustomerForwardingRulesByOrganizationUuid(this.endCustomerUuid, this.getSecondUuid(this.endCustomerUuid, this.purchasedByUuid));
    }
  }

  onSelectionPurchasedBy(event) {
    if (event != null && event != undefined && event.value != null && event.value != undefined) {
      this.purchasedByUuid = event.value.uuid;
      this.getCustomerForwardingRulesByOrganizationUuid(this.purchasedByUuid, this.getSecondUuid(this.purchasedByUuid, this.endCustomerUuid));
    }
  }

  onSelectionOtherRelationshipType(event) {

  }

  onSelectionOtherRelationshipValue(event) {

  }

  getSecondUuid(fUuid: string, sUuid: string) {
    let rUuid = null;
    if (sUuid != null && sUuid != undefined && sUuid != '' && fUuid != sUuid) {
      rUuid = sUuid;
    }
    return rUuid;
  }

  getCustomerForwardingRulesByOrganizationUuid(uuid1: string, uuid2: string) {
    let uuids = [];
    uuids.push(uuid1);
    if (uuid2 != null) {
      uuids.push(uuid2);
    }
    this.companiesService.getCustomerForwardingRulesByOrganizationUuids(uuids).subscribe(data => {
      this.customerForwardingRules = data;
      this.emptyCustomerForwardingRule();
      for (let index = 0; index < this.customerForwardingRules.length; index++) {
        const element = this.customerForwardingRules[index];
        this.addCustomerForwardingRule(element);
      }
    }, error => {
      console.log(error);
      this.loading = false;
    });
  }

  emptyCustomerForwardingRule() {
    const items = this.uploadDevicesForm.get('ignore_forwarding_rules') as FormArray;
    const l = items.length;
    for (let i = 0; i < l; i++) {
      items.removeAt(0);
    }
  }

  addCustomerForwardingRule(forwaringRule: any) {
    this.ignore_forwarding_rules.push(this.formBuldier.group({
      organisationName: [forwaringRule.organisation.organisationName, null],
      type: [forwaringRule.type, null],
      url: [forwaringRule.url, null],
      description: [forwaringRule.forwardingRuleUrl.description ? forwaringRule.forwardingRuleUrl.description : null],
      ruleName: [forwaringRule.forwardingRuleUrl.ruleName ? forwaringRule.forwardingRuleUrl.ruleName : null],
      uuid: [forwaringRule.uuid ? forwaringRule.uuid : null],
      id: [forwaringRule.id ? forwaringRule.id : 0],
      ignore: [false]
    }));
  }

  getAllCustomerForwardingRuleUrl() {
    this.companiesService.getAllCustomerForwardingRuleUrl().subscribe(data => {
      this.customerForwardingRuleUrls = data;
    }, error => {
      console.log(error);
      this.loading = false;
    });
  }

  deleteForwardingRule(index: number) {
    const add = this.uploadDevicesForm.get('forwarding_list') as FormArray;
    add.removeAt(index)
  }

  // addAnotherRelationships() {
  //   this.other_relationships.push( this.formBuldier.group({
  //     other_relationship_type: ['', Validators.compose([Validators.required])],
  //     other_relationship_value: ['', Validators.compose([Validators.required])]
  //   }));
  // }

  // get other_relationships(): FormArray {
  //   return this.uploadDevicesForm.get('other_relationships') as FormArray;
  // }

  get ignore_forwarding_rules(): FormArray {
    return this.uploadDevicesForm.get('ignore_forwarding_rules') as FormArray;
  }

  // deleteRelationship(index: number) {
  //   const add = this.uploadDevicesForm.get('other_relationships') as FormArray;
  //   add.removeAt(index)
  // }

  getAllCustomerAndResaller() {
    this.companiesService.getAllActiveByOrganisationRoles(['RESELLER', 'CUSTOMER']).subscribe(
      data => {
        this.perchaseByList = data.body;
      },
      error => {
        console.log(error);
      }
    );
  }

  clearFile() {
    this.selectedFileName = null;
  }
}
