import { Component, OnInit } from '@angular/core';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { AssetsService } from '../assets.service';
import { CompaniesService } from '../companies.service';
import { CustomErrorToastrComponent } from '../custom-error-toastr/custom-error-toastr.component';
import { CustomSuccessToastrComponent } from '../custom-success-toastr/custom-success-toastr.component';
import { GatewayListService } from '../gateway-list.service';
import { Asset } from '../models/asset';
import { AssetCompany } from '../models/assetCompany';
declare var $: any;
import { saveAs } from 'file-saver';
import { CommonData } from '../constant/common-data';

@Component({
  selector: 'app-upload-asset-associations',
  templateUrl: './upload-asset-associations.component.html',
  styleUrls: ['./upload-asset-associations.component.css']
})
export class UploadAssetAssociationsComponent implements OnInit {
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
  assetCategory = CommonData.ASSET_CATEGORY;
  assetAssociationCustomer = CommonData.ASSOCIATION_CUSTOMER_LIST;
  assetInfoWSname: string;
  isSuperAdmin: any;
  companiesDetails: any;
  modified = false;
  assetCompany: AssetCompany = new AssetCompany();
  selectedFileName;
  fileSelected: boolean;
  isRoleCustomerManagerInstaller: any;
  roleCallCenterUser: any;
  heading = []; // for headings
  fileRow = []; // for rows
  errorMessage = 'The following records contain invalid IMEIs. Please check the file and try again.';
  errorHeading = 'File contains invalid IMEI values';
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
      companies: ['']
    });
  }

  getAllCompanyList() {
    this.loading = true;
    this.gatewayListService.getAllCustomerObject(['EndCustomer']).subscribe(
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
    this.router.navigate(['customerAssets']);
  }

  onFileChange(event) {
    this.assetPayload = {};
    // if (this.assetCompany.company === null || this.assetCompany.company === undefined) {
    //   this.toastr.error('Please select a company', null, { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
    //   this.clearUploadData();
    //   return;
    // }
    console.log('file event selected file');
    console.log('Inside OnfileChange Method');
    const target: DataTransfer = <DataTransfer>(event);
    if (event.length !== 1) {
      throw new Error('Cannot use multiple files');
    }
    this.fileSelected = true;
    this.selectedFileName = event[0].name;
    if (this.selectedFileName.split(/\.(?=[^\.]+$)/)[1] !== 'csv') {
      this.toastr.error('The Asset List data file must be in .csv format. Please check your file and try again.', 'Incorrect File Format', { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
      return;
    }
    const reader: FileReader = new FileReader();
    reader.readAsText(event[0]);
    reader.onload = (e) => {
      const csv: any = reader.result;
      let allTextLines = [];
      allTextLines = csv.split(/\r|\n|\r/);
      // Table Headings
      const headers = allTextLines[0].split(',');
      if (headers && headers.length > 1) {
        for (const iterator of headers) {
          this.heading.push(iterator);
        }
      } else {
        this.toastr.error('Header not Available and try again.', 'Incorrect File Format', { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
        this.clearUploadData();
        return;
      }
      let dataList = [];
      // Table Rows
      for (let i = 1; i < allTextLines.length; i++) {
        const val = allTextLines[i].split(',');
        if (val.length > 1) {
          if (val[1] && val[1].length > 1 && val[2]) {
            val[2] = val[2].trim();
            if (val[2].length === 14) {
              val[2] = '0' + val[1];
            }
            if (val[2].length >= 15) {
              const obj = {
                customer: val[0] ? val[0].trim() : val[0],
                asset_id: val[1] ? val[1].trim() : val[1],
                device_id: val[2],
                asset_type: val[3] ? val[3].trim().replace(/(\r\n|\n|\r)/gm, '') : val[3],
                installed_date: val[4] ? val[4].trim() : val[4],
                vin: val[5] ? val[5].trim() : val[5],
                make: val[6] ? val[6].trim() : val[6],
                gateway_eligibility: null,
                company_payload: null
              };
              if (obj.asset_type) {
                const obj1 = this.assetCategory.find(item => item.value.toLowerCase() === obj.asset_type.toLowerCase());
                if (obj1) {
                  obj.gateway_eligibility = obj1.gateway;
                  obj.asset_type = obj1.value;
                } else {
                  obj.asset_type = undefined;
                  this.errorHeading = 'Upload does not proceed in this scenario.';
                  this.errorMessage = 'The following records contain invalid Asset Type. Please check the file and try again.';
                  $('#AssociationMessageModel').modal('show');
                  this.clearUploadData();
                  break;
                }
              }
              if (obj.customer && this.assetAssociationCustomer.indexOf(obj.customer) !== -1) {
                if (obj.customer === 'Werner Enterprises') {
                  obj.customer = 'Werner Enterprises, Inc.';
                }
                const obj1 = this.companies.find(item => item.organisationName.toLowerCase() === obj.customer.toLowerCase());
                if (obj1) {
                  const obj2 = {
                    id: obj1.id,
                    company_name: obj1.organisationName,
                    short_name: obj1.shortName,
                    status: obj1.isActive,
                    account_number: obj1.accountNumber,
                    isAsset_list_required: obj1.isAssetListRequired,
                    uuid: obj1.uuid
                  };
                  obj.company_payload = obj2;
                }
              }
              if (!(obj.company_payload || this.assetCompany.company)) {
                this.toastr.error('Please select a company for imei' + obj.device_id, null, { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
                this.clearUploadData();
                return;
              }
              dataList.push(obj);
              this.fileRow.push(val);
            } else {
              console.log(val);
              this.errorHeading = 'Upload does not proceed in this scenario.';
              this.errorMessage = 'The following records contain invalid IMEIs. Please check the file and try again.';
              $('#AssociationMessageModel').modal('show');
              this.clearUploadData();
              break;
            }
          } else {
            console.log(val);
            this.errorHeading = 'Upload does not proceed in this scenario.';
            this.errorMessage = 'The following records contain invalid IMEIs. Please check the file and try again.'
            $('#AssociationMessageModel').modal('show');
            this.clearUploadData();
            break;
          }
        }
      }
      this.assetPayload = {
        account_number: this.assetCompany && this.assetCompany.company ? this.assetCompany.company.accountNumber : '',
        asset_payload: dataList
      }
      console.log(this.assetPayload);
    };
  }
  downloadTemplate() {
    this.assetsService.downloadAsset().subscribe((blob) => saveAs(blob, 'Data_Collection_Template.xlsx'));
  }


  rejectModifiedData() {
    this.loading = false;
    this.registerCompany.controls.file.reset();
  }

  clearUploadData() {
    this.registerCompany.patchValue({ file: null });
    this.selectedFileName = null;
    this.assetPayload = {};
  }
  assetPayload: any;
  onFileChangeBrower(event) {
    this.assetPayload = {};
    /* wire up file reader */
    // if (this.assetCompany.company === null || this.assetCompany.company === undefined) {
    //   this.toastr.error('Please select a company', null, { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
    //   this.clearUploadData();
    //   return;
    // }
    console.log('Inside OnfileChange Method');
    const target: DataTransfer = <DataTransfer>(event);
    if (event.target.files.length !== 1) {
      throw new Error('Cannot use multiple files');
    }
    this.fileSelected = true;
    this.selectedFileName = event.target.files[0].name;
    if (this.selectedFileName.split(/\.(?=[^\.]+$)/)[1] !== 'csv') {
      this.toastr.error('The Asset List data file must be in .csv format. Please check your file and try again.', 'Incorrect File Format', { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
      return;
    }
    const reader: FileReader = new FileReader();
    reader.readAsText(event.target.files[0]);
    reader.onload = (e) => {
      const csv: any = reader.result;
      let allTextLines = [];
      allTextLines = csv.split(/\r|\n|\r/);
      // Table Headings
      const headers = allTextLines[0].split(',');
      if (headers && headers.length > 1) {
        for (const iterator of headers) {
          this.heading.push(iterator);
        }
      } else {
        this.toastr.error('Header not Available and try again.', 'Incorrect File Format', { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
        this.clearUploadData();
        return;
      }
      let dataList = [];
      // Table Rows
      for (let i = 1; i < allTextLines.length; i++) {
        const val = allTextLines[i].split(',');
        if (val.length > 1) {
          if (val[1] && val[1].length > 1 && val[2]) {
            val[2] = val[2].trim();
            if (val[2].length === 14) {
              val[2] = '0' + val[1];
            }
            if (val[2].length >= 15) {
              const obj = {
                customer: val[0] ? val[0].trim() : val[0],
                asset_id: val[1] ? val[1].trim() : val[1],
                device_id: val[2],
                asset_type: val[3] ? val[3].trim().replace(/(\r\n|\n|\r)/gm, '') : val[3],
                installed_date: val[4] ? val[4].trim() : val[4],
                vin: val[5] ? val[5].trim() : val[5],
                make: val[6] ? val[6].trim() : val[6],
                gateway_eligibility: null,
                company_payload: null
              };
              if (obj.asset_type) {
                const obj1 = this.assetCategory.find(item => item.value.toLowerCase() === obj.asset_type.toLowerCase());
                if (obj1) {
                  obj.gateway_eligibility = obj1.gateway;
                  obj.asset_type = obj1.value;
                } else {
                  obj.asset_type = undefined;
                  this.errorHeading = 'Upload does not proceed in this scenario.';
                  this.errorMessage = 'The following records contain invalid Asset Type. Please check the file and try again.';
                  $('#AssociationMessageModel').modal('show');
                  this.clearUploadData();
                  break;
                }
              }
              if (obj.customer && this.assetAssociationCustomer.indexOf(obj.customer) !== -1) {
                if (obj.customer === 'Werner Enterprises') {
                  obj.customer = 'Werner Enterprises, Inc.';
                }
                const obj1 = this.companies.find(item => item.organisationName.toLowerCase() === obj.customer.toLowerCase());
                if (obj1) {
                  const obj2 = {
                    id: obj1.id,
                    company_name: obj1.organisationName,
                    short_name: obj1.shortName,
                    status: obj1.isActive,
                    account_number: obj1.accountNumber,
                    isAsset_list_required: obj1.isAssetListRequired,
                    uuid: obj1.uuid
                  };
                  obj.company_payload = obj2;
                }
              }
              if (!(obj.company_payload || this.assetCompany.company)) {
                this.toastr.error('Please select a company for imei' + obj.device_id, null, { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
                this.clearUploadData();
                return;
              }
              dataList.push(obj);
              this.fileRow.push(val);
            } else {
              console.log(val);
              this.errorHeading = 'Upload does not proceed in this scenario.';
              this.errorMessage = 'The following records contain invalid IMEIs. Please check the file and try again.';
              $('#AssociationMessageModel').modal('show');
              this.clearUploadData();
              break;
            }
          } else {
            console.log(val);
            this.errorHeading = 'Upload does not proceed in this scenario.';
            this.errorMessage = 'The following records contain invalid IMEIs. Please check the file and try again.'
            $('#AssociationMessageModel').modal('show');
            this.clearUploadData();
            break;
          }
        }
      }
      this.assetPayload = {
        account_number: this.assetCompany && this.assetCompany.company ? this.assetCompany.company.accountNumber : '',
        asset_payload: dataList
      }
      console.log(this.assetPayload);
    };
  }


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
    // if (this.assetCompany.company === null || this.assetCompany.company === undefined) {
    //   this.toastr.error('Please select a company', null, { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
    //   return;
    // }
    // if (this.fileSelected && this.assetInfoWSname == undefined) {
    //   console.log(this.data.length + ' is data length');
    //   this.toastr.error('The uploaded file does not follow the expected data format. Please use the template provided to format the data.', 'Invalid file format', { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
    //   return;

    // } else if (this.fileSelected && this.assetInfoWSname != undefined) {
    //   if (!this.assetInfoWSname.includes('Asset Information')) {
    //     console.log(this.data.length + ' is data length when wrong file name');
    //     this.toastr.error('The uploaded file does not follow the expected data format. Please use the template provided to format the data.', 'Invalid file format', { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
    //     return;
    //   }

    // }
    if (!this.assetPayload || !this.assetPayload.asset_payload || this.assetPayload.asset_payload.length < 1) {
      console.log(this.data.length + ' is data length when no file selected');
      this.toastr.error('Please select a file.', '', { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
      return;

    }
    this.loading = true;
    this.assetsService.uploadAssetAssociation(this.assetPayload).subscribe(data => {
      this.loading = false;
      console.log(data);
      if (data.status) {
        this.toastr.success('Asset Successfully associated', null, { toastComponent: CustomSuccessToastrComponent, tapToDismiss: true, disableTimeOut: true });
        this.clearUploadData();
        this.router.navigate(['customerAssets']);
      } else {
        if (data.message === 'DeviceNotFound') {
          this.errorHeading = 'Upload does not proceed in this scenario.';
          this.errorMessage = 'The following records contain device IDs that do not exist in the device database. Please check the file and try again.';
          $('#AssociationMessageModel').modal('show');
          this.clearUploadData();
        } else if (data.message === 'Company Not Found') {
          this.errorHeading = 'Upload does not proceed in this scenario.';
          this.errorMessage = 'End customer not found in database. please check and try again';
          $('#AssociationMessageModel').modal('show');
          this.clearUploadData();
        } else if (data.message === 'AssociationDataNull') {
          this.errorHeading = 'Upload does not proceed in this scenario.';
          this.errorMessage = 'The file does not contain any record. please check and try again';
          $('#AssociationMessageModel').modal('show');
          this.clearUploadData();
        } else if (data.message === 'DiffrentEndCustomer') {
          this.errorHeading = 'Upload does not proceed in this scenario.';
          this.errorMessage = 'The following records contain device IDs(' + data.body + ') that are assigned to a different customer than the one selected.  Please check the file and try again.';
          $('#AssociationMessageModel').modal('show');
          this.clearUploadData();
        } else if (data.message === 'WrongVIN') {
          this.errorHeading = 'Upload proceed in this scenario.';
          this.errorMessage = 'The following records contain invalid IMEIs. These VIN values will be ignored.';
          $('#AssociationMessageModel').modal('show');
          this.clearUploadData();
        }
      }
    },
      error => {
        this.assetPayload = {};
        this.loading = false;
        console.log(error);
        this.clearUploadData();
      }
    );
  }

}
