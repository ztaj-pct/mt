import { Component, OnDestroy } from '@angular/core';
import { ICellRendererAngularComp } from '@ag-grid-community/angular';

import { Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { CustomErrorToastrComponent } from 'src/app/custom-error-toastr/custom-error-toastr.component';
import { DevicesListService } from '../devices/devices-list/devices-list.service';
declare var $: any;



@Component({
  selector: 'btn-cell-renderer',
  template: `
  <div class='action_col'>
  <span *ngIf="params?.data?.imei != null" class='iconBtn icoBtn_delete'>
  <i  class='fa fa-pencil' aria-hidden='true'>
  </i></span>
  </div>
  `,
})
export class BtnCellRendererForGatewayDetails implements ICellRendererAngularComp, OnDestroy {
  refresh(params: any): boolean {
    throw new Error("Method not implemented.");
  }
  afterGuiAttached?(params?: import("@ag-grid-community/all-modules").IAfterGuiAttachedParams): void {
    throw new Error("Method not implemented.");
  }
  public params: any;
  packageUuid: any;
  loading = false;
  agInit(params: any): void {
    this.params = params;
  }

  btnClickedHandler() {
    this.params.clicked(this.params.value);
  }

  constructor(public devicesListService: DevicesListService, private router: Router, private toastr: ToastrService) {

  }
  ngOnDestroy() {
    // no need to remove the button click handler 
    // https://stackoverflow.com/questions/49083993/does-angular-automatically-remove-template-event-listeners
  }

  deleteGateway() {
    this.devicesListService.deviceList = [];
    if (this.params.data.status != 'Pending') {
      this.toastr.error("The device with the ID "+this.params.data.imei+" is part of an installation and so it cannot be deleted.","Problem with Deletion",{toastComponent: CustomErrorToastrComponent, tapToDismiss:true, disableTimeOut:true});
    } else {
      this.devicesListService.deviceList.push(this.params.data);
      $('#deleteGatewayModal').modal('show');
    }
  }

  editGateway(): void {
    this.devicesListService.deviceList = [];
    if (this.params.data.status != 'Pending') {
      this.toastr.error("The selected device(s) are part of an in-progress or active installation, and cannot be modified.","Device Configuration Can’t Be Modified",{toastComponent: CustomErrorToastrComponent, tapToDismiss:true, disableTimeOut:true});
    } else {
      this.devicesListService.deviceList.push(this.params.data);
      this.router.navigate(['update-gateways']);
    }
  };

  resetGateway(): void {
    this.devicesListService.deviceList = [];
    if (this.params.data.status == 'Installed' || this.params.data.status == 'Problem' || this.params.data.status == 'Install in progress' || this.params.data.status == 'Active') {
      this.devicesListService.deviceList.push(this.params.data);
      $('#resetGatewayModal').modal('show');
      } else {
        this.toastr.error("The device with the ID "+this.params.data.imei+" is not part of an installation and so it cannot be reset.","Problem with Reset",{toastComponent: CustomErrorToastrComponent, tapToDismiss:true, disableTimeOut:true});
    }
  }

}