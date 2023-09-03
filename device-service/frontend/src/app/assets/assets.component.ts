import { Component, OnInit } from '@angular/core';

import { AssetsService } from '../assets.service';
import { FormGroup, FormControl, FormBuilder, Validators } from '@angular/forms';
import * as XLSX from 'xlsx';
import { Asset } from '../models/asset';
import { ToastrService } from 'ngx-toastr';
import { Router } from '@angular/router';
import { saveAs } from 'file-saver';
import { CompaniesService } from '../companies.service';
import { AssetCompany } from '../models/assetCompany';
import { CustomErrorToastrComponent } from '../custom-error-toastr/custom-error-toastr.component';
import { CustomSuccessToastrComponent } from '../custom-success-toastr/custom-success-toastr.component';
import { CustomBatchErrorToastrComponent } from '../custom-batch-error-toastr/custom-batch-error-toastr.component';
import { GatewayListService } from '../gateway-list.service';
import { CommonData } from '../constant/common-data';
declare var $: any;
@Component({
  selector: '[app-assets]',
  templateUrl: './assets.component.html',
  styleUrls: ['./assets.component.css']
})


export class AssetsComponent implements OnInit {
  devicesToBeSaved: Asset[] = [];
  registerCompany: FormGroup;
  selectedFiles: FileList;
  currentFileUpload: File;
  data: Array<any> = [];
  resError = false;
  resSuccess = false;
  updateFlag = false;
  submitted = false;
  loading = false;
  companies: any = [];
  assetInfoWSname: String;
  isSuperAdmin: any;
  companiesDetails: any;
  assetCategory = CommonData.ASSET_CATEGORY;
  eligibility_gateway = [
    { name: 'Smart7' },
    { name: 'TrailerNet' },
    { name: 'ContainerNet' },
    { name: 'Sabre' },
    { name: 'ChassisNet' },
    { name: 'ContainerNet' },
    { name: 'EZTrac' },
    { name: 'AssetTrac' },
    { name: 'DriveTrac' },
    { name: 'EZTracSolar' },
    { name: 'EZTracPlus' }
  ];
  category = [{ name: 'Trailer' }, { name: 'Chassis' }, { name: 'Container' }, { name: 'Vehicle' }, { name: 'Tractor' }];

  modified = false;
  assetCompany: AssetCompany = new AssetCompany();
  selectedFileName;
  fileSelected: boolean;
  isRoleCustomerManagerInstaller: any;
  roleCallCenterUser: any;
  constructor(
    private assetsService: AssetsService,
    private readonly gatewayListService: GatewayListService,
    private toastr: ToastrService,
    public formBuldier: FormBuilder, private companiesService: CompaniesService,
    public router: Router) {
    this.roleCallCenterUser = JSON.parse(sessionStorage.getItem('IS_ROLE_CALL_CENTER_USER'));
    this.getAllCompanyList();
  }
  ngOnInit() {
    this.isSuperAdmin = sessionStorage.getItem('IS_ROLE_SUPERADMIN');
    this.isRoleCustomerManagerInstaller = sessionStorage.getItem('IS_ROLE_CUSTOMER_MANAGER_INSTALLER');
    this.companiesDetails = sessionStorage.getItem('companies');
    this.registerCompany = this.formBuldier.group({
      file: ['', Validators.compose([Validators.required])],
      companies: ['', Validators.compose([Validators.required])]
    });
  }

  getAllCompanyList() {
    this.loading = true;
    this.gatewayListService.getAllCustomerObject(['End_Customer']).subscribe(
      data => {
        this.loading = false;
        if (data !== undefined && data != null && data.length > 0) {
          if (this.roleCallCenterUser) {
            const obj = data.find(item => item.accountNumber === 'A-00243');
            this.companies.push(obj);
          } else {
            this.companies = data;
          }
        }
      },
      error => {
        this.loading = false;
        console.log(error);
      }
    );
  }

  selectFile(event) {
    const file = event.target.files.item(0);
    this.selectedFiles = event.target.files;

  }

  // create the form object.

  get g() {
    return this.registerCompany.controls;
  }

  cancel() {
    this.registerCompany.reset();
    if (sessionStorage.getItem('beforeUploadAssetUrl').includes('/assets')) {
      this.router.navigate(['assets']);
      return;
    }
    this.router.navigate(['customerAssets']);
  }


  insertIntoDb() {
    if (this.data.length > 2) {
      const date = new Date();
      const dateUTC = date.toISOString();

      if (this.data[1].A != 'Asset ID' || this.data[1].B != 'Which product is approved for asset' ||
        this.data[1].D != 'Asset Type 1') {
        this.loading = false;
        this.toastr.error('The uploaded file does not follow the expected data format. Please use the template provided to format the data.', 'Invalid file format', { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
      } else {
        for (let i = 2; i < this.data.length; i++) {
          const deviceToAdd = new Asset();
          deviceToAdd.assigned_name = this.data[i].A;
          deviceToAdd.eligible_gateway = this.data[i].B;
          if (deviceToAdd.eligible_gateway == undefined || deviceToAdd.eligible_gateway == undefined) {
            this.toastr.error('Please enter correct values in \'Product approved for Asset\'.', 'Invalid file format', { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
            this.loading = false;
            return;
          }
          deviceToAdd.vin = this.data[i].C;
          if (deviceToAdd.vin == null || deviceToAdd.vin == undefined) {
            $('#nullVin').modal('show');
            return;
          }
          var target = this.category.find(temp => temp.name == deviceToAdd.eligible_gateway);
          deviceToAdd.category = this.data[i].D;
          if (deviceToAdd.category == undefined || deviceToAdd.category == undefined) {
            this.toastr.error('Please enter correct values in \'Asset Type\'.', 'Invalid file format', { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
            this.loading = false;
            return;
          }
          var target1 = this.eligibility_gateway.find(temp => temp.name == deviceToAdd.category);

          if (target1 || target) {
            this.toastr.error('Please enter correct values in  ‘Asset type’ and ‘Product approved for Asset.', 'Invalid file format', { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
            this.loading = false;
            return;
          }
          deviceToAdd.year = this.data[i].H;
          deviceToAdd.manufacturer = this.data[i].I;
          deviceToAdd.door_type = this.data[i].G && this.data[i].G != '' ? this.data[i].G.toUpperCase() : '';
          deviceToAdd.no_of_tires = this.data[i].P;
          deviceToAdd.external_length = this.data[i].J;
          this.devicesToBeSaved.push(deviceToAdd);
        }
        this.assetCompany.asset_list = this.devicesToBeSaved;
        if (this.assetCompany.company) {
          const company = {
            id: this.assetCompany.company.id ? this.assetCompany.company.id : null,
            company_name: this.assetCompany.company.companyName ? this.assetCompany.company.companyName : null,
            short_name: this.assetCompany.company.shortName ? this.assetCompany.company.shortName : null,
            type: this.assetCompany.company.type ? this.assetCompany.company.type : null,
            status: this.assetCompany.company.status ? this.assetCompany.company.status : null,
            account_number: this.assetCompany.company.accountNumber ? this.assetCompany.company.accountNumber : null,
            is_asset_list_required: this.assetCompany.company.isAssetListRequired ? this.assetCompany.company.isAssetListRequired : null,
            uuid: this.assetCompany.company.uuid ? this.assetCompany.company.uuid : null,
          };
          this.assetCompany.company = company;
        }
        this.assetsService.addAssetList(this.assetCompany).subscribe(data => {
          this.clearUploadData();
          if (Object.keys(data.body).length === 0) {
            this.toastr.success('Assets saved successfully', null, { toastComponent: CustomSuccessToastrComponent });
          } else {
            var totalErrorDevices = 0;
            var finalErrorString = '';
            var errorTitleArray = Object.keys(data.body);
            for (var i = 0; i < errorTitleArray.length; i++) {
              finalErrorString += '<br><b>' + errorTitleArray[i] + '</b>';
              var errorDevicesArray = data.body[errorTitleArray[i]];
              finalErrorString += '<br><ul>';
              for (var j = 0; j < errorDevicesArray.length; j++) {
                finalErrorString += '<li>' + errorDevicesArray[j] + '</li>';
                totalErrorDevices++;
              }
              finalErrorString += '</ul>';
            }
            var finalErrorTitle = totalErrorDevices + ' Errors';
            this.toastr.error(finalErrorString, finalErrorTitle, { toastComponent: CustomBatchErrorToastrComponent, tapToDismiss: true, disableTimeOut: true, enableHtml: true });
          }

          this.loading = false;
          this.router.navigate(['customerAssets']);

        }, error => {
          this.clearUploadData();
          console.log(error);
          this.loading = false;
        });
      }


      // if ends


    } else {
      this.toastr.error('File is empty. Please select another file', null, { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
      this.loading = false;
    }


  }

  rejectData() {
    this.devicesToBeSaved = [];
    this.loading = false;
    this.registerCompany.controls['file'].reset();
    this.registerCompany.reset();
    return;
  }

  onFileChange(event) {
    if (this.assetCompany.company === null || this.assetCompany.company === undefined) {
      this.toastr.error('Please select a company', null, { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
      this.clearUploadData();
      return;
    }
    this.fileSelected = true;
    this.selectedFileName = event[0].name;
    const target: DataTransfer = <DataTransfer>(event);
    // if (event.length !== 1) {
    //     throw new Error('Cannot use multiple files');
    // }
    const reader: FileReader = new FileReader();
    reader.onload = (e: any) => {
      /* read workbook */
      const bstr: string = e.target.result;
      const wb: XLSX.WorkBook = XLSX.read(bstr, { type: 'binary' });

      /* grab first sheet */
      const wsname: string = wb.SheetNames[1];
      const ws: XLSX.WorkSheet = wb.Sheets[wsname];

      /* save data */
      this.data = (XLSX.utils.sheet_to_json(ws, { header: 'A' }));
      const header = this.data[2];

      const headerKeys = Object.keys(header);

    };

    reader.readAsBinaryString(event[0]);
    this.assetsService.checkForOverwrite(this.assetCompany.company.id).subscribe(res => {
      if (res.body === true) {
        $('#replaceAssetModal').modal('show');
      }
    });

  }
  downloadTemplate() {
    this.assetsService.downloadAsset().subscribe((blob) => saveAs(blob, 'Data_Collection_Template.xlsx'));

  }
  insertToDb() {
    this.devicesToBeSaved = [];
    if (this.data.length > 2) {
      const date = new Date();
      if (this.data[1].A != 'Asset ID' || this.data[1].B != 'Which product is approved for asset' ||
        this.data[1].D != 'Asset Type 1') {
        this.loading = false;
        this.toastr.error('The uploaded file does not follow the expected data format. Please use the template provided to format the data.', 'Invalid file format', { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
      } else {
        for (let i = 2; i < this.data.length; i++) {
          const deviceToAdd = new Asset();
          deviceToAdd.assigned_name = this.data[i].A;
          deviceToAdd.eligible_gateway = this.data[i].B;
          var target = this.category.find(temp => temp.name == deviceToAdd.eligible_gateway);
          deviceToAdd.vin = this.data[i].C;
          deviceToAdd.category = this.data[i].D;
          if (deviceToAdd.eligible_gateway == undefined || deviceToAdd.eligible_gateway == undefined) {
            this.toastr.error('Please enter correct values in \'Product approved for Asset\'.', 'Invalid file format', { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
            this.loading = false;
            return;
          }
          deviceToAdd.vin = this.data[i].C;
          if (deviceToAdd.vin == null || deviceToAdd.vin == undefined) {
            $('#nullVin').modal('show');
            return;
          }
          var target1 = this.eligibility_gateway.find(temp => temp.name == deviceToAdd.category);
          var target = this.category.find(temp => temp.name == deviceToAdd.eligible_gateway);
          if (deviceToAdd.category == undefined || deviceToAdd.category == undefined) {
            this.toastr.error('Please enter correct values in \'Asset Type\'.', 'Invalid file format', { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
            this.loading = false;
            return;
          }
          if (target1 || target) {
            this.toastr.error('Please enter correct values in  ‘Asset type’ and ‘Product approved for Asset.', 'Invalid file format', { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
            this.loading = false;
            return;
          }
          deviceToAdd.year = this.data[i].H;
          deviceToAdd.manufacturer = this.data[i].I;
          this.devicesToBeSaved.push(deviceToAdd);
        }
        this.assetCompany.asset_list = this.devicesToBeSaved;
        if (this.assetCompany.company) {
          const company = {
            id: this.assetCompany.company.id ? this.assetCompany.company.id : null,
            company_name: this.assetCompany.company.organisationName ? this.assetCompany.company.organisationName : null,
            short_name: this.assetCompany.company.shortName ? this.assetCompany.company.shortName : null,
            type: this.assetCompany.company.type ? this.assetCompany.company.type : null,
            status: this.assetCompany.company.status ? this.assetCompany.company.status : null,
            account_number: this.assetCompany.company.accountNumber ? this.assetCompany.company.accountNumber : null,
            is_asset_list_required: this.assetCompany.company.isAssetListRequired ? this.assetCompany.company.isAssetListRequired : null,
            uuid: this.assetCompany.company.uuid ? this.assetCompany.company.uuid : null,
          };
          this.assetCompany.company = company;
        }
        this.assetsService.addAssetList(this.assetCompany).subscribe(data => {
          this.clearUploadData();
          if (Object.keys(data.body).length === 0) {
            this.toastr.success('Assets saved successfully', null, { toastComponent: CustomSuccessToastrComponent });
          } else {
            var totalErrorDevices = 0;
            var finalErrorString = '';
            var errorTitleArray = Object.keys(data.body);
            for (var i = 0; i < errorTitleArray.length; i++) {
              finalErrorString += '<br><b>' + errorTitleArray[i] + '</b>';
              var errorDevicesArray = data.body[errorTitleArray[i]];
              finalErrorString += '<br><ul>';
              for (var j = 0; j < errorDevicesArray.length; j++) {
                finalErrorString += '<li>' + errorDevicesArray[j] + '</li>';
                totalErrorDevices++;
              }
              finalErrorString += '</ul>';
            }
            var finalErrorTitle = totalErrorDevices + ' Errors';
            this.toastr.error(finalErrorString, finalErrorTitle, { toastComponent: CustomBatchErrorToastrComponent, tapToDismiss: true, disableTimeOut: true, enableHtml: true });
          }

          this.loading = false;
          this.router.navigate(['customerAssets']);

        }, error => {
          this.clearUploadData();
          console.log(error);
          this.loading = false;
        });
      }
    } else {
      this.toastr.error('File is empty. Please select another file', null, { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
      this.loading = false;
    }


  }


  UpdateToDb() {

    if (this.data.length > 2) {
      const date = new Date();
      const dateUTC = date.toISOString();

      if (this.data[1].A != 'Asset ID' || this.data[1].B != 'Which product is approved for asset' ||
        this.data[1].D != 'Asset Type 1') {
        this.loading = false;
        this.toastr.error('The uploaded file does not follow the expected data format. Please use the template provided to format the data.', 'Invalid file format', { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
      } else {

        for (let i = 2; i < this.data.length; i++) {
          const deviceToAdd = new Asset();
          deviceToAdd.assigned_name = this.data[i].A;
          deviceToAdd.eligible_gateway = this.data[i].B;
          var target = this.category.find(temp => temp.name == deviceToAdd.eligible_gateway);
          deviceToAdd.vin = this.data[i].C;
          deviceToAdd.category = this.data[i].D;
          var target1 = this.eligibility_gateway.find(temp => temp.name == deviceToAdd.category);
          if (deviceToAdd.eligible_gateway == undefined || deviceToAdd.eligible_gateway == undefined) {
            this.toastr.error('Please enter correct values in \'Product approved for Asset\'.', 'Invalid file format', { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
            this.loading = false;
            return;
          }
          deviceToAdd.vin = this.data[i].C;
          if (deviceToAdd.vin == null || deviceToAdd.vin == undefined) {
            $('#nullVin').modal('show');
            return;
          }

          if (deviceToAdd.category == undefined || deviceToAdd.category == undefined) {
            this.toastr.error('Please enter correct values in \'Asset Type\'.', 'Invalid file format', { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
            this.loading = false;
            return;
          }
          if (target1 || target) {
            this.toastr.error('Please enter correct values in  ‘Asset type’ and ‘Product approved for Asset.', 'Invalid file format', { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
            this.loading = false;
            return;
          }
          deviceToAdd.year = this.data[i].H;
          deviceToAdd.manufacturer = this.data[i].I;
          this.devicesToBeSaved.push(deviceToAdd);

        }
        this.assetCompany.asset_list = this.devicesToBeSaved;
        if (this.assetCompany.company) {
          const company = {
            id: this.assetCompany.company.id ? this.assetCompany.company.id : null,
            company_name: this.assetCompany.company.companyName ? this.assetCompany.company.companyName : null,
            short_name: this.assetCompany.company.shortName ? this.assetCompany.company.shortName : null,
            type: this.assetCompany.company.type ? this.assetCompany.company.type : null,
            status: this.assetCompany.company.status ? this.assetCompany.company.status : null,
            account_number: this.assetCompany.company.accountNumber ? this.assetCompany.company.accountNumber : null,
            is_asset_list_required: this.assetCompany.company.isAssetListRequired ? this.assetCompany.company.isAssetListRequired : null,
            uuid: this.assetCompany.company.uuid ? this.assetCompany.company.uuid : null,
          };
          this.assetCompany.company = company;
        }
        this.assetsService.reuploadModifiedAssets(this.assetCompany).subscribe(data => {
          if (Object.keys(data.body).length === 0) {
            this.toastr.success('Assets saved successfully', null, { toastComponent: CustomSuccessToastrComponent });
          } else {
            var totalErrorDevices = 0;
            var finalErrorString = '';
            var errorTitleArray = Object.keys(data.body);
            for (var i = 0; i < errorTitleArray.length; i++) {
              finalErrorString += '<br><b>' + errorTitleArray[i] + '</b>';
              var errorDevicesArray = data.body[errorTitleArray[i]];
              finalErrorString += '<br><ul>';
              for (var j = 0; j < errorDevicesArray.length; j++) {
                finalErrorString += '<li>' + errorDevicesArray[j] + '</li>';
                totalErrorDevices++;
              }
              finalErrorString += '</ul>';
            }
            var finalErrorTitle = totalErrorDevices + ' Errors';
            this.toastr.error(finalErrorString, finalErrorTitle, { toastComponent: CustomBatchErrorToastrComponent, tapToDismiss: true, disableTimeOut: true, enableHtml: true });
          }

          this.loading = false;
          this.router.navigate(['customerAssets']);
        }, error => {
          console.log(error);
          this.loading = false;
        });
      }
    } else {
      this.toastr.error('File is empty. Please select another file', null, { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
      this.loading = false;
    }
  }


  insertModifiedDataIntoDb() {

    if (this.data.length > 2) {
      const date = new Date();
      const dateUTC = date.toISOString();

      if (this.data[1].A != 'Asset ID' || this.data[1].B != 'Which product is approved for asset' ||
        this.data[1].D != 'Asset Type 1') {
        this.loading = false;
        this.toastr.error('The uploaded file does not follow the expected data format. Please use the template provided to format the data.', 'Invalid file format', { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
      } else {

        for (let i = 2; i < this.data.length; i++) {
          const deviceToAdd = new Asset();
          deviceToAdd.assigned_name = this.data[i].A;
          deviceToAdd.vin = this.data[i].C;
          if (deviceToAdd.vin == null || deviceToAdd.vin == undefined) {
            $('#nullVinUpdate').modal('show');
            return;
          }
          deviceToAdd.eligible_gateway = this.data[i].B;
          var target = this.category.find(temp => temp.name == deviceToAdd.eligible_gateway);
          if (this.data[i].D) {
            deviceToAdd.category = this.data[i].D.trim().replace(/(\r\n|\n|\r)/gm, '');
            if (deviceToAdd.category) {
              const obj = this.assetCategory.find(item => item.value.toLowerCase() === deviceToAdd.category.toLowerCase());
              if (obj) {
                deviceToAdd.category = obj.value;
              } else {
                deviceToAdd.category = undefined;
              }
            }
          }
          if (deviceToAdd.eligible_gateway == undefined || deviceToAdd.eligible_gateway == undefined) {
            this.toastr.error('Please enter correct values in \'Product approved for Asset\'.', 'Invalid file format', { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
            this.loading = false;
            return;
          }
          if (deviceToAdd.category === undefined) {
            this.toastr.error('Please enter correct values in \'Asset Type\'.', 'Invalid file format', { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
            this.loading = false;
            return;
          }
          var target1 = this.eligibility_gateway.find(temp => temp.name == deviceToAdd.category);
          if (target || target1) {
            this.toastr.error('Please enter correct values in  ‘Asset type’ and ‘Product approved for Asset.', 'Invalid file format', { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
            this.loading = false;
            return;
          }
          deviceToAdd.year = this.data[i].H;
          deviceToAdd.manufacturer = this.data[i].I;
          deviceToAdd.door_type = this.data[i].G && this.data[i].G != '' ? this.data[i].G.toUpperCase() : '';
          deviceToAdd.no_of_tires = this.data[i].P;
          deviceToAdd.external_length = this.data[i].J;
          this.devicesToBeSaved.push(deviceToAdd);

        }
        this.assetCompany.asset_list = this.devicesToBeSaved;
        if (this.assetCompany.company) {
          const company = {
            id: this.assetCompany.company.id ? this.assetCompany.company.id : null,
            company_name: this.assetCompany.company.companyName ? this.assetCompany.company.companyName : null,
            short_name: this.assetCompany.company.shortName ? this.assetCompany.company.shortName : null,
            type: this.assetCompany.company.type ? this.assetCompany.company.type : null,
            status: this.assetCompany.company.status ? this.assetCompany.company.status : null,
            account_number: this.assetCompany.company.accountNumber ? this.assetCompany.company.accountNumber : null,
            is_asset_list_required: this.assetCompany.company.isAssetListRequired ? this.assetCompany.company.isAssetListRequired : null,
            uuid: this.assetCompany.company.uuid ? this.assetCompany.company.uuid : null,
          };
          this.assetCompany.company = company;
        }
        this.assetsService.reuploadModifiedAssets(this.assetCompany).subscribe(data => {
          this.clearUploadData();
          if (Object.keys(data.body).length === 0) {
            this.toastr.success('Assets saved successfully', null, { toastComponent: CustomSuccessToastrComponent });
          } else {
            var totalErrorDevices = 0;
            var finalErrorString = '';
            var errorTitleArray = Object.keys(data.body);
            for (var i = 0; i < errorTitleArray.length; i++) {
              finalErrorString += '<br><b>' + errorTitleArray[i] + '</b>';
              var errorDevicesArray = data.body[errorTitleArray[i]];
              finalErrorString += '<br><ul>';
              for (var j = 0; j < errorDevicesArray.length; j++) {
                finalErrorString += '<li>' + errorDevicesArray[j] + '</li>';
                totalErrorDevices++;
              }
              finalErrorString += '</ul>';
            }
            var finalErrorTitle = totalErrorDevices + ' Errors';
            this.toastr.error(finalErrorString, finalErrorTitle, { toastComponent: CustomBatchErrorToastrComponent, tapToDismiss: true, disableTimeOut: true, enableHtml: true });
          }

          this.loading = false;
          this.router.navigate(['customerAssets']);
        }, error => {
          this.clearUploadData();
          console.log(error);
          this.loading = false;
        });
      }
    } else {
      this.toastr.error('File is empty. Please select another file', null, { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
      this.loading = false;
    }
  }
  rejectModifiedData() {
    this.loading = false;
    this.registerCompany.controls.file.reset();
  }

  clearUploadData() {
    this.registerCompany.patchValue({ file: null });
    this.selectedFileName = null;
  }
  onFileChangeBrower(event) {
    /* wire up file reader */
    if (this.assetCompany.company === null || this.assetCompany.company === undefined) {
      this.toastr.error('Please select a company', null, { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
      this.clearUploadData();
      return;
    }
    const target: DataTransfer = <DataTransfer>(event);
    if (event.target.files.length !== 1) {
      throw new Error('Cannot use multiple files');
    }
    this.fileSelected = true;
    this.selectedFileName = event.target.files[0].name;
    if (this.selectedFileName.split(/\.(?=[^\.]+$)/)[1] !== 'xlsx') {
      this.toastr.error('The Asset List data file must be in .xlsx format. Please check your file and try again.', 'Incorrect File Format', { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
      return;
    }
    const reader: FileReader = new FileReader();
    reader.onload = (e: any) => {
      /* read workbook */
      const bstr: string = e.target.result;
      const wb: XLSX.WorkBook = XLSX.read(bstr, { type: 'binary' });

      /* grab first sheet */
      const wsname: string = wb.SheetNames[1];
      this.assetInfoWSname = wb.SheetNames[1];
      const ws: XLSX.WorkSheet = wb.Sheets[wsname];

      /* save data */
      this.data = (XLSX.utils.sheet_to_json(ws, { header: 'A' }));
      const header = this.data[2];

      const headerKeys = Object.keys(header);

    };
    reader.readAsBinaryString(event.target.files[0]);
    this.assetsService.checkForOverwrite(this.assetCompany.company.id).subscribe(res => {
      if (res.body === true) {
        $('#replaceAssetModal').modal('show');
      }
    });

  }
  /// ==================================================================================================


  files: any[] = [];

  /**
   * on file drop handler
   */
  onFileDropped($event) {
    this.onFileChange($event);
  }




  insertModified() {
    this.modified = true;
  }

  save() {
    if (this.assetCompany.company === null || this.assetCompany.company === undefined) {
      this.toastr.error('Please select a company', null, { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
      return;
    }
    if (this.fileSelected && this.assetInfoWSname == undefined) {
      this.toastr.error('The uploaded file does not follow the expected data format. Please use the template provided to format the data.', 'Invalid file format', { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
      return;

    } else if (this.fileSelected && this.assetInfoWSname != undefined) {
      if (!this.assetInfoWSname.includes('Asset Information')) {
        this.toastr.error('The uploaded file does not follow the expected data format. Please use the template provided to format the data.', 'Invalid file format', { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
        return;
      }

    }
    if (this.data.length < 1) {
      this.toastr.error('Please select a file.', '', { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
      return;

    }
    this.loading = true;
    if (this.modified) {
      this.insertModifiedDataIntoDb();

    } else {
      this.insertIntoDb();
    }
  }

}
