import { Component, OnInit } from '@angular/core';
import { FormGroup, FormControl, FormBuilder, Validators } from '@angular/forms';
import * as XLSX from 'xlsx';
import { ToastrService } from 'ngx-toastr';
import { Router } from '@angular/router';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Package } from '../models/package.model';
import { CampaignService } from '../campaign/campaign.component.service';
import { CustomSuccessToastrComponent } from '../custom-success-toastr/custom-success-toastr.component';
import { CustomErrorToastrComponent } from '../custom-error-toastr/custom-error-toastr.component';
declare var $: any;

const httpOptions = {
  headers: new HttpHeaders({
    'Content-Type': 'application/json'
  })
}
@Component({
  selector: 'app-upload-packages',
  templateUrl: './upload-packages.component.html',
  styleUrls: ['./upload-packages.component.css']
})
export class UploadPackagesComponent {

  uploadPackageForm: FormGroup;
  data: Array<any> = [];
  selectedFiles: FileList;
  selectedFileName;
  loading = false;
  packageList: Package[] = [];
  totalProductsOrdered: number = 0;
  fieldErrorMessage: string;
  requiredFieldList: String = "";
  emptyString: String = "";
  isValidationsFailForFile = false;
  isQuantityCheckedForShippedDevice = false;
  userCompanyType;
  isSuperAdmin;
  constructor(public _formBuldier: FormBuilder, private toastr: ToastrService, public router: Router, public campaignService: CampaignService, private http: HttpClient) { }

  ngOnInit() {
    this.userCompanyType = sessionStorage.getItem('type');
    this.isSuperAdmin = sessionStorage.getItem('IS_ROLE_SUPERADMIN');
    if(this.isSuperAdmin === 'true'){
      this.router.navigate(['upload-packages']);
    }else{
      this.router.navigate(['customerAssets']);
    }
    this.uploadPackageForm = this._formBuldier.group({
      file: ['', Validators.compose([Validators.required])]
    });
  }

  /**
   * on file drop handler
   */
  onFileDropped($event) {
    this.onFileChange($event);
  }

  onFileChange(event) {
    /* wire up file reader */
    this.selectedFileName = event[0].name;

    if (this.selectedFileName.substr(this.selectedFileName.lastIndexOf('.') + 1) != "xlsx") {
      this.toastr.error("The Package List data file must be in .XLSX format. Please check your file and try again.", "Incorrect File Format", { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
      this.clearUploadData();
      return;
    }
    console.log("Inside OnfileChange Method");
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
      const wsname: string = wb.SheetNames[0];
      const ws: XLSX.WorkSheet = wb.Sheets[wsname];

      /* save data */
      this.data = (XLSX.utils.sheet_to_json(ws, { header: 'A' }));
      const header = this.data[1];

      console.log("Excel data captured");
      // console.log(this.data);
      const headerKeys = Object.keys(header);

    };

    reader.readAsBinaryString(event[0]);

  }

  selectFile(event) {
    const file = event.target.files.item(0);
    this.selectedFiles = event.target.files;

  }

  clearUploadData() {
    this.uploadPackageForm.patchValue({ 'file': null });
    this.selectedFileName = null;
  }

  onFileChangeBrower(event) {
    /* wire up file reader */

    console.log("Inside OnfileChange Method");
    const target: DataTransfer = <DataTransfer>(event);
    if (event.target.files.length !== 1) {
      throw new Error('Cannot use multiple files');
    }
    this.selectedFileName = event.target.files[0].name;

    if (this.selectedFileName.substr(this.selectedFileName.lastIndexOf('.') + 1) != "xlsx") {
      this.toastr.error("The Package List data file must be in .XLSX format. Please check your file and try again.", "Incorrect File Format", { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
      this.clearUploadData();
      return;
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
      this.data = (XLSX.utils.sheet_to_json(ws, { header: 'A' }));
      const header = this.data[1];

      console.log("Excel data captured");
      const headerKeys = Object.keys(header);

    };
    reader.readAsBinaryString(event.target.files[0]);

  }

  checkUndefinedOrNullOrBlankCheck(field): boolean {
    let isCheck = false;
    if (field == undefined || field == null || field == "") {
      isCheck = true;
    }
    return isCheck;
  }
  nonEmptyMandatoryCheck(field): boolean {
    let isCheck = false;
    if (field != "") {
      isCheck = true;
    }
    return isCheck;
  }

  save() {

    if (this.data.length < 1) {
      this.toastr.error("Please select a file", "", { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
      return;
    }

    this.loading = true;
    let map = new Map();
    if (this.data.length > 1) {
      let date = new Date();
      let dateUTC = date.toISOString();
      this.packageList = [];
      for (let i = 1; i < this.data.length; i++) {
        const packageObj = new Package();
        packageObj.package_name = this.data[i].A;
        packageObj.bin_version = this.data[i].B;
        packageObj.app_version = this.data[i].C;
        packageObj.mcu_version = this.data[i].D;
        packageObj.ble_version = this.data[i].E;
        packageObj.config1 = this.data[i].F;
        packageObj.config1_crc = this.data[i].G;
        packageObj.config2 = this.data[i].H;
        packageObj.config2_crc = this.data[i].I;
        packageObj.config3 = this.data[i].J;
        packageObj.config3_crc = this.data[i].K;
        packageObj.config4 = this.data[i].L;
        packageObj.config4_crc = this.data[i].M;
        packageObj.device_type = this.data[i].N
        packageObj.lite_sentry_hardware = this.data[i].O
        packageObj.lite_sentry_app = this.data[i].P
        packageObj.lite_sentry_boot = this.data[i].Q
        packageObj.microsp_app = this.data[i].R
        packageObj.microsp_mcu = this.data[i].S
        packageObj.cargo_maxbotix_hardware = this.data[i].T
        packageObj.cargo_maxbotix_firmware = this.data[i].U
        packageObj.cargo_riot_hardware = this.data[i].V
        packageObj.cargo_riot_firmware = this.data[i].W

        if (this.checkUndefinedOrNullOrBlankCheck(packageObj.package_name)) {
          if (this.requiredFieldList == "") {
            this.requiredFieldList = "package_name";
          } else {
            this.requiredFieldList = this.requiredFieldList + " , package_name";
          }
        }
        if (this.checkUndefinedOrNullOrBlankCheck(packageObj.bin_version)) {
          if (this.requiredFieldList == "") {
            this.requiredFieldList = "bin_version";
          } else {
            this.requiredFieldList = this.requiredFieldList + " , bin_version";
          }
        }

        if (this.checkUndefinedOrNullOrBlankCheck(packageObj.app_version)) {
          if (this.requiredFieldList == "") {
            this.requiredFieldList = "app_version";
          } else {
            this.requiredFieldList = this.requiredFieldList + " , app_version";
          }
        }

        if (this.checkUndefinedOrNullOrBlankCheck(packageObj.mcu_version)) {
          if (this.requiredFieldList == "") {
            this.requiredFieldList = "mcu_version";
          } else {
            this.requiredFieldList = this.requiredFieldList + " , mcu_version";
          }
        }

        if (this.checkUndefinedOrNullOrBlankCheck(packageObj.ble_version)) {
          if (this.requiredFieldList == "") {
            this.requiredFieldList = "ble_version";
          } else {
            this.requiredFieldList = this.requiredFieldList + " , ble_version";
          }
        }

        if (this.checkUndefinedOrNullOrBlankCheck(packageObj.config1)) {
          if (this.requiredFieldList == "") {
            this.requiredFieldList = "config1";
          } else {
            this.requiredFieldList = this.requiredFieldList + " , config1";
          }
        }

        if (this.checkUndefinedOrNullOrBlankCheck(packageObj.config2)) {
          if (this.requiredFieldList == "") {
            this.requiredFieldList = "config2";
          } else {
            this.requiredFieldList = this.requiredFieldList + " , config2";
          }
        }

        if (this.checkUndefinedOrNullOrBlankCheck(packageObj.config3)) {
          if (this.requiredFieldList == "") {
            this.requiredFieldList = "config3";
          } else {
            this.requiredFieldList = this.requiredFieldList + " , config3";
          }
        }

        if (this.checkUndefinedOrNullOrBlankCheck(packageObj.config4)) {
          if (this.requiredFieldList == "") {
            this.requiredFieldList = "config4";
          } else {
            this.requiredFieldList = this.requiredFieldList + " , config4";
          }
        }
        
        if (this.checkUndefinedOrNullOrBlankCheck(packageObj.config1_crc)) {
          if (this.requiredFieldList == "") {
            this.requiredFieldList = "config1_crc";
          } else {
            this.requiredFieldList = this.requiredFieldList + " , config1_crc";
          }
        }


        if (this.checkUndefinedOrNullOrBlankCheck(packageObj.device_type)) {
          if (this.requiredFieldList == "") {
            this.requiredFieldList = "device_type";
          } else {
            this.requiredFieldList = this.requiredFieldList + " , device_type";
          }
        }


        if (this.checkUndefinedOrNullOrBlankCheck(packageObj.lite_sentry_hardware)) {
          if (this.requiredFieldList == "") {
            this.requiredFieldList = "lite_sentry_hardware";
          } else {
            this.requiredFieldList = this.requiredFieldList + " , lite_sentry_hardware";
          }
        }

        if (this.checkUndefinedOrNullOrBlankCheck(packageObj.lite_sentry_boot)) {
          if (this.requiredFieldList == "") {
            this.requiredFieldList = "lite_sentry_boot";
          } else {
            this.requiredFieldList = this.requiredFieldList + " , lite_sentry_boot";
          }
        }

        if (this.checkUndefinedOrNullOrBlankCheck(packageObj.microsp_app)) {
          if (this.requiredFieldList == "") {
            this.requiredFieldList = "microsp_app";
          } else {
            this.requiredFieldList = this.requiredFieldList + " , microsp_app";
          }
        }

        if (this.checkUndefinedOrNullOrBlankCheck(packageObj.microsp_mcu)) {
          if (this.requiredFieldList == "") {
            this.requiredFieldList = "microsp_mcu";
          } else {
            this.requiredFieldList = this.requiredFieldList + " , microsp_mcu";
          }
        }

        if (this.checkUndefinedOrNullOrBlankCheck(packageObj.cargo_maxbotix_hardware)) {
          if (this.requiredFieldList == "") {
            this.requiredFieldList = "cargo_maxbotix_hardware";
          } else {
            this.requiredFieldList = this.requiredFieldList + " , cargo_maxbotix_hardware";
          }
        }

        if (this.checkUndefinedOrNullOrBlankCheck(packageObj.cargo_maxbotix_firmware)) {
          if (this.requiredFieldList == "") {
            this.requiredFieldList = "cargo_maxbotix_firmware";
          } else {
            this.requiredFieldList = this.requiredFieldList + " , cargo_maxbotix_firmware";
          }
        }

        if (this.checkUndefinedOrNullOrBlankCheck(packageObj.cargo_riot_hardware)) {
          if (this.requiredFieldList == "") {
            this.requiredFieldList = "cargo_riot_hardware";
          } else {
            this.requiredFieldList = this.requiredFieldList + " , cargo_riot_hardware";
          }
        }

        if (this.checkUndefinedOrNullOrBlankCheck(packageObj.cargo_riot_firmware)) {
          if (this.requiredFieldList == "") {
            this.requiredFieldList = "cargo_riot_firmware";
          } else {
            this.requiredFieldList = this.requiredFieldList + " , cargo_riot_firmware";
          }
        }

        if (packageObj.config2!=null && packageObj.config2 != undefined &&( packageObj.config2.toLowerCase() == "any" || packageObj.config2.toLowerCase() == "missing")) {
          console.log("config2 is either missing or any");
          if (this.checkUndefinedOrNullOrBlankCheck(packageObj.config2_crc)) {
            packageObj.config2_crc=this.emptyString;
          }
          else{
            //show error
            this.isValidationsFailForFile = true;
            this.fieldErrorMessage = "Record [" + i + "] : config2_crc has to be blank as Config2 is either Missing or Any . Please check your file and try again.";
            this.toastr.error(this.fieldErrorMessage, "", { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
          }
            
        }
        else{
          if (this.checkUndefinedOrNullOrBlankCheck(packageObj.config2_crc)) {
            if (this.requiredFieldList == "") {
              this.requiredFieldList = "config2_crc";
            } else {
              this.requiredFieldList = this.requiredFieldList + " , config2_crc";
            }
          }
        }


        if (packageObj.config3!=null && packageObj.config3 != undefined && (packageObj.config3.toLowerCase() == "any" || packageObj.config3.toLowerCase() == "missing")) {
          console.log("config3 is either missing or any");
          if (this.checkUndefinedOrNullOrBlankCheck(packageObj.config3_crc)) {
            packageObj.config3_crc=this.emptyString;
          }
          else{
            //show error
            this.isValidationsFailForFile = true;
            this.fieldErrorMessage = "Record [" + i + "] : config3_crc has to be blank as Config3 is either Missing or Any . Please check your file and try again.";
            this.toastr.error(this.fieldErrorMessage, "", { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
          }
        }
        else{
          if (this.checkUndefinedOrNullOrBlankCheck(packageObj.config3_crc)) {
            if (this.requiredFieldList == "") {
              this.requiredFieldList = "config3_crc";
            } else {
              this.requiredFieldList = this.requiredFieldList + " , config3_crc";
            }
          }
          
        }


        if (packageObj.config4!=null && packageObj.config4 != undefined && ( packageObj.config4.toLowerCase() == "any" || packageObj.config4.toLowerCase() == "missing")) {
          console.log("config4 is either missing or any");
          if (this.checkUndefinedOrNullOrBlankCheck(packageObj.config4_crc)) {
            packageObj.config4_crc=this.emptyString;
          }
          else{
            //show error
            this.isValidationsFailForFile = true;
            this.fieldErrorMessage = "Record [" + i + "] : config4_crc has to be blank as Config4 is either Missing or Any . Please check your file and try again.";
            this.toastr.error(this.fieldErrorMessage, "", { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
          }
        }
        else{
          if (this.checkUndefinedOrNullOrBlankCheck(packageObj.config4_crc)) {
            if (this.requiredFieldList == "") {
              this.requiredFieldList = "config4_crc";
            } else {
              this.requiredFieldList = this.requiredFieldList + " , config4_crc";
            }
          }
        }

        this.packageList.push(packageObj);
        console.log("=================== this.packageList ======================");
        console.log(this.packageList);

        if (this.requiredFieldList != "") {
          this.fieldErrorMessage = "Record [" + i + "] is missing these required fields: [ " + this.requiredFieldList + " ]. Please check your file and try again.";
          //this.toastr.error(this.fieldErrorMessage);
          this.toastr.error(this.fieldErrorMessage, "", { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
          this.fieldErrorMessage = "";
          this.requiredFieldList = "";
          this.isValidationsFailForFile = true;
        }

      }
      this.loading = false;
      if (this.isValidationsFailForFile) {
        this.isValidationsFailForFile = false;
        this.loading = false;
        return;
      }

      this.campaignService.addPackage(this.packageList).subscribe(data => {
        //this.toastr.success(data.message);
        this.toastr.success(data.message, null, { toastComponent: CustomSuccessToastrComponent });
        this.uploadPackageForm.reset();
        this.router.navigate(['packages']);
  
      }, error => {
        this.cancel();
        console.log(error);
      });
      

    }

  }

  cancel() {
    this.uploadPackageForm.reset();
    this.router.navigate(['packages']);
  }

}
