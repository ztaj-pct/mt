import { Component, OnInit, ViewChild } from '@angular/core';
import { FormGroup, FormBuilder } from '@angular/forms';
import { ResetInstalationStatusService } from './reset-installation-status.service';
import { ToastrService } from 'ngx-toastr';
import { CompaniesService } from '../companies.service';
import { CustomErrorToastrComponent } from '../custom-error-toastr/custom-error-toastr.component';
import { CustomSuccessToastrComponent } from '../custom-success-toastr/custom-success-toastr.component';
import { MatOption, MatSelect } from '@angular/material';
@Component({
  selector: 'app-reset-installation-status',
  templateUrl: './reset-installation-status.component.html',
  styleUrls: ['./reset-installation-status.component.css']
})
export class ResetInstallationStatusComponent implements OnInit {
  @ViewChild('select') select: MatSelect;
  resetInstallationForm: FormGroup;
  resetCustomerForm : FormGroup;
  companiesList: any = [];
  loading=false;
  constructor(public _formBuldier: FormBuilder, private resetInstalationStatusservice: ResetInstalationStatusService, private toastr: ToastrService,public companyService: CompaniesService) { }

  ngOnInit() {
    this.getCustomerCompaniesList();
    this.resetInstallationForm = this._formBuldier.group({
      deviceID: [''],
      vin: [''],
      accountNumber: [''],
    },
    );
    this.resetCustomerForm = this._formBuldier.group({
      uuid: [''],
     },
    );
  }
  onSubmit(resetInstallationForm) {
  
    if (!resetInstallationForm.value.vin && !resetInstallationForm.value.deviceID ) {
      this.toastr.error("Please enter either a Device ID or a VIN for the installation you want to reset.",null,{toastComponent: CustomErrorToastrComponent, tapToDismiss:true, disableTimeOut:true});
      return;
    }
    if (!resetInstallationForm.value.accountNumber) {
      this.toastr.error("Please select Customer.",null,{toastComponent: CustomErrorToastrComponent, tapToDismiss:true, disableTimeOut:true});
      return;
    }
    this.loading=true;
    this.resetInstalationStatusservice.resetInstalationStatus(resetInstallationForm.value.vin, resetInstallationForm.value.deviceID,resetInstallationForm.value.accountNumber).subscribe(data => {
      console.log(data);
      if (data.status)
        this.toastr.success(data.message, null, {toastComponent:CustomSuccessToastrComponent});
        
      else
        this.toastr.error(data.message,null,{toastComponent: CustomErrorToastrComponent, tapToDismiss:true, disableTimeOut:true});
        this.cancel();
        this.loading=false;
    }, error => {
      console.log(error);
      this.loading=false;
    }
    );
  }
  cancel() {
    this.resetInstallationForm.reset();
  }
  onResetCustomer(resetCustomerForm) {
    if (!resetCustomerForm.value.uuid) {
      this.toastr.error("Please choose the customer whose data you want to reset.",null,{toastComponent: CustomErrorToastrComponent, tapToDismiss:true, disableTimeOut:true});
      return;
    }
    this.loading=true;
    this.resetInstalationStatusservice.resetCustomerTestData(resetCustomerForm.value.uuid).subscribe(data => {
      console.log(data);
      if (data.status)
        this.toastr.success(data.message, null, {toastComponent:CustomSuccessToastrComponent});
        this.resetForm();
        this.loading=false;
    }, error => {
      console.log(error);
      this.loading=false;
    });
  }
  getCustomerCompaniesList() {
    var type:string[]; 
    type = ["Customer"] 
    this.companyService.getAllCompany(type,true).subscribe(
        data => {
            this.companiesList = data.body.content;
        }
    );
}
resetForm() {
  this.resetCustomerForm.reset();
}

}
