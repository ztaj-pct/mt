import { Component, OnInit } from '@angular/core';
import { FormGroup, FormControl, FormBuilder, Validators } from '@angular/forms';
import * as XLSX from 'xlsx';
import { ToastrService } from 'ngx-toastr';
import { Router } from '@angular/router';
import { ShippedDevice } from '../models/ShippedDevice';
import { ShippedDevices } from '../models/ShippedDevices';
import { ShippedDeviceService } from '../shipped-devices.service';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { CustomErrorToastrComponent } from '../custom-error-toastr/custom-error-toastr.component';
import { CustomSuccessToastrComponent } from '../custom-success-toastr/custom-success-toastr.component';
declare var $: any;

const httpOptions = {
  headers: new HttpHeaders({
    'Content-Type': 'application/json'
  })
}
@Component({
  selector: 'app-shipped-devices',
  templateUrl: './shipped-devices.component.html',
  styleUrls: ['./shipped-devices.component.css']
})
export class ShippedDevicesComponent {

  shippedDeviceForm: FormGroup;
  data: Array<any> = [];
  salesForceOrderResponse: any;
  selectedFiles: FileList;
  selectedFileName;
  shippedDevices: ShippedDevices = new ShippedDevices();
  loading = false;
  devicesToBeShipped: ShippedDevice[] = [];
  totalProductsOrdered: number = 0;
  fieldErrorMessage: string;
  requiredFieldList: String = "";
  isValidationsFailForFile = false;
  isQuantityCheckedForShippedDevice = false;
  constructor(public _formBuldier: FormBuilder, private toastr: ToastrService, public router: Router, public shippedDeviceService: ShippedDeviceService, private http: HttpClient) { }

  ngOnInit() {

    this.shippedDeviceForm = this._formBuldier.group({
      file: ['', Validators.compose([Validators.required])],
      companies: ['', Validators.compose([Validators.required])],
      epicorOrderNo: ['', Validators.compose([Validators.required])]
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

  onFileChangeBrower(event) {
    /* wire up file reader */

    console.log("Inside OnfileChange Method");
    const target: DataTransfer = <DataTransfer>(event);
    if (event.target.files.length !== 1) {
      throw new Error('Cannot use multiple files');
    }
    this.selectedFileName = event.target.files[0].name;
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
    reader.readAsBinaryString(event.target.files[0]);

  }

  changeConfirmQuantity(id) {



    let containputiner = $("#id_" + id).val();
    if (this.salesForceOrderResponse != null) {
      let remainingQty = (parseInt(this.salesForceOrderResponse.message.shipment_details[id].quantity_ordered) - parseInt(this.salesForceOrderResponse.message.shipment_details[id].quantity_shipped));
      if (remainingQty >= parseInt(containputiner + "")) {
        this.salesForceOrderResponse.message.shipment_details[id].quantity_confirmed = containputiner;
        this.isQuantityCheckedForShippedDevice = false;
      } else {
        this.fieldErrorMessage = "The quantity shipped cannot be greater than the total ordered quantity for this line item. Please check and try again.";
        //this.toastr.error("The quantity shipped cannot be greater than the total ordered quantity for this line item. Please check and try again.",null,{toastComponent: CustomErrorToastrComponent, tapToDismiss:true, disableTimeOut:true});
        $("#fieldErrorMessageModal").modal('show');
        this.isQuantityCheckedForShippedDevice = true;
      }

    }
  }

  checkUndefinedOrNullOrBlankCheck(field): boolean {
    let isCheck = false;
    if (field == undefined || field == null || field == "") {
      isCheck = true;
    }
    return isCheck;
  }

  checkUndefinedAndNullString(value) {
    let isValidString = false;
    if (value != undefined && value != null && value != "") {
      isValidString = true;
    }
    return isValidString;
  }

  save() {

    if (this.data.length < 1) {
      this.toastr.error("Please select a file", null, { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
      return;
    }

    if (this.salesForceOrderResponse == null) {
      this.toastr.error("Epicor number is required field.", null, { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
      return;
    }


    if (this.isQuantityCheckedForShippedDevice) {
      this.fieldErrorMessage = "The quantity shipped cannot be greater than the total ordered quantity for this line item. Please check and try again.";
      //this.toastr.error("The quantity shipped cannot be greater than the total ordered quantity for this line item. Please check and try again.",null,{toastComponent: CustomErrorToastrComponent, tapToDismiss:true, disableTimeOut:true});
      $("#fieldErrorMessageModal").modal('show');
      return;
    }

    this.loading = true;
    let map = new Map();
    this.devicesToBeShipped = [];
    let imei_list = [];
    let deviceToShipped = null;
    if (this.data.length > 1) {
      let date = new Date();
      let dateUTC = date.toISOString();
      for (let i = 1; i < this.data.length; i++) {

        if (this.checkUndefinedAndNullString(this.data[i].H)) {
          if (i == 1) {
            map.set(this.data[i].H, 1);
          } else {
            if (map.has(this.data[i].H)) {
              let value = map.get(this.data[i].H);
              map.set(this.data[i].H, ++value);
            } else {
              map.set(this.data[i].H, 1);
            }
          }
        }


        if (this.checkUndefinedAndNullString(this.data[i].B)) {
          /*if (i > 1) {
            this.devicesToBeShipped[this.devicesToBeShipped.length - 1].imei.push(imei_list);
            imei_list = [];
          }*/

          deviceToShipped = new ShippedDevice();
          //deviceToShipped.epicor_order_id = this.data[i].A;
          deviceToShipped.epicor_order_id = this.data[i].B;
          deviceToShipped.salesforce_order_number = this.data[i].C;
          deviceToShipped.salesforce_account_id = this.data[i].D;
          deviceToShipped.packing_slip_number = this.data[i].E;
          deviceToShipped.shipment_details = this.data[i].F;
          deviceToShipped.product_code = this.data[i].G;
          deviceToShipped.quantity_shipped = this.data[i].H;
          deviceToShipped.order_item_number = this.data[i].I;
          if (this.checkUndefinedAndNullString(this.data[i].J)) {
            deviceToShipped.imei.push(this.data[i].J);
          }
          if (this.checkUndefinedOrNullOrBlankCheck(deviceToShipped.epicor_order_id)) {
            if (this.requiredFieldList == "") {
              this.requiredFieldList = "epicor_order_id";
            } else {
              this.requiredFieldList = this.requiredFieldList + " , epicor_order_id";
            }
          }
          if (this.checkUndefinedOrNullOrBlankCheck(deviceToShipped.salesforce_order_number)) {
            if (this.requiredFieldList == "") {
              this.requiredFieldList = "salesforce_order_number";
            } else {
              this.requiredFieldList = this.requiredFieldList + " , salesforce_order_number";
            }
          }

          if (this.checkUndefinedOrNullOrBlankCheck(deviceToShipped.salesforce_account_id)) {
            if (this.requiredFieldList == "") {
              this.requiredFieldList = "salesforce_account_id";
            } else {
              this.requiredFieldList = this.requiredFieldList + " , salesforce_account_id";
            }
          }

          if (this.checkUndefinedOrNullOrBlankCheck(deviceToShipped.packing_slip_number)) {
            if (this.requiredFieldList == "") {
              this.requiredFieldList = "packing_slip_number";
            } else {
              this.requiredFieldList = this.requiredFieldList + " , packing_slip_number";
            }
          }

          /*if (this.checkUndefinedOrNullOrBlankCheck(deviceToShipped.shipment_details)) {
            if (this.requiredFieldList == "") {
              this.requiredFieldList = "shipment_details";
            } else {
              this.requiredFieldList = this.requiredFieldList + " , shipment_details";
            }
          }*/

          if (this.checkUndefinedOrNullOrBlankCheck(deviceToShipped.product_code)) {
            if (this.requiredFieldList == "") {
              this.requiredFieldList = "product_code";
            } else {
              this.requiredFieldList = this.requiredFieldList + " , product_code";
            }
          }

          if (this.checkUndefinedOrNullOrBlankCheck(deviceToShipped.quantity_shipped)) {
            if (this.requiredFieldList == "") {
              this.requiredFieldList = "quantity_shipped";
            } else {
              this.requiredFieldList = this.requiredFieldList + " , quantity_shipped";
            }
          }

          if (this.checkUndefinedOrNullOrBlankCheck(deviceToShipped.order_item_number)) {
            if (this.requiredFieldList == "") {
              this.requiredFieldList = "order_item_number";
            } else {
              this.requiredFieldList = this.requiredFieldList + " , order_item_number";
            }
          }


          if (this.requiredFieldList != "") {
            this.fieldErrorMessage = "Record [" + i + "] is missing these required fields: [ " + this.requiredFieldList + " ]. Please check your file and try again.";
            this.toastr.error(this.fieldErrorMessage, null, { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
            this.fieldErrorMessage = "";
            this.requiredFieldList = "";
            this.isValidationsFailForFile = true;
          }

          if (!this.isValidationsFailForFile) {
            if ("" + deviceToShipped.epicor_order_id !== this.salesForceOrderResponse.message.epicor_order_number) {
              this.fieldErrorMessage = "Record [" + i + "] contains a different order ID. Please check and try again."
              $("#fieldErrorMessageModal").modal('show');
              this.loading = false;
              return;
            }
            this.devicesToBeShipped.push(deviceToShipped);
          }
        } else if (this.checkUndefinedAndNullString(this.data[i].J)) {
          deviceToShipped.imei.push(this.data[i].J)
        }

      }

      // console.log("==================== this.devicesToBeShipped ==================");
      // console.log(this.devicesToBeShipped);
      this.loading = false;
      if (this.isValidationsFailForFile) {
        this.isValidationsFailForFile = false;
        this.loading = false;
        return;
      }

      /*if (this.salesForceOrderResponse.message.shipment_details != null && this.salesForceOrderResponse.message.shipment_details.length > 0) {
        for (var k = 0; k < this.salesForceOrderResponse.message.shipment_details.length; k++) {
          if (this.salesForceOrderResponse.message.shipment_details[k] != undefined && this.salesForceOrderResponse.message.shipment_details[k].quantity_confirmed != undefined && this.salesForceOrderResponse.message.shipment_details[k].quantity_confirmed != null) {
            let key = k + 1;
            let value = map.get(key);
            if (value != undefined && value != null && this.salesForceOrderResponse.message.shipment_details[k].quantity_confirmed != value) {
              this.fieldErrorMessage = "The shipped quantity that was entered for the line item " + key + " does not match the number of records in the data file. Please check and try again.";
              $("#fieldErrorMessageModal").modal('show');
              return;
            }
          }
        }
      }*/


      this.shippedDevices.shipped_devices.shipped_device_list = this.devicesToBeShipped
      this.shippedDevices.epicor_order_detail = this.salesForceOrderResponse;
      this.loading = false;
      this.shippedDeviceService.addShippedDevice(this.shippedDevices).subscribe(data => {
        if (data != undefined && data != null) {
          this.toastr.success(data.message, null, {toastComponent:CustomSuccessToastrComponent});
        }
        this.loading = false;
        this.router.navigate(['customerAssets']);

      }, error => {
        console.log(error);
        this.loading = false;
      })

    }

  }

  onBlurEventForEpicor(event: any) {
    if (this.shippedDeviceForm.controls.epicorOrderNo != undefined && this.shippedDeviceForm.controls.epicorOrderNo.value != undefined && this.shippedDeviceForm.controls.epicorOrderNo.value != null && this.shippedDeviceForm.controls.epicorOrderNo.value != "") {
      this.loading = true;
      this.shippedDeviceService.getSalesForceOrderDetailByEpicorNumber(this.shippedDeviceForm.controls.epicorOrderNo.value).subscribe(data => {
        this.totalProductsOrdered = 0;
        if (data != undefined && data != null && data.message != undefined && data.message != null && data.code != undefined && data.code != null && data.code == "100") {
          this.salesForceOrderResponse = data;
          this.loading = false;
          if (this.salesForceOrderResponse.message.shipment_details != undefined && this.salesForceOrderResponse.message.shipment_details != null && this.salesForceOrderResponse.message.shipment_details.length > 0) {
            if (this.salesForceOrderResponse.message.shipment_details.length > 1) {
              const sorted = this.salesForceOrderResponse.message.shipment_details.sort((a, b) => a.product_short_name > b.product_short_name ? 1 : -1);
              this.salesForceOrderResponse.message.shipment_details = sorted;
            }
            for (var i = 0; i < this.salesForceOrderResponse.message.shipment_details.length; i++) {
              this.totalProductsOrdered = this.totalProductsOrdered + this.salesForceOrderResponse.message.shipment_details[i].quantity_ordered;
            }
          }
        } else {
          this.fieldErrorMessage = "This order number could not be found. Please try again.";
          $("#fieldErrorMessageModal").modal('show');
          this.loading = false;
          //this.toastr.error("This order number could not be found. Please try again.",null,{toastComponent: CustomErrorToastrComponent, tapToDismiss:true, disableTimeOut:true});
          this.salesForceOrderResponse = null;
          this.totalProductsOrdered = 0;
        }
        this.loading = false;

      }, error => {
        console.log(error);
        this.loading = false;
      })
    }
  }

  cancel() {
    this.shippedDeviceForm.reset();
    this.router.navigate(['customerAssets']);
  }

}
