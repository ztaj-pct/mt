import { Component, OnDestroy } from '@angular/core';
import { ICellRendererAngularComp } from '@ag-grid-community/angular';

import { Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { GatewayListService } from '../gateway-list.service';
import { CustomErrorToastrComponent } from '../custom-error-toastr/custom-error-toastr.component';
declare var $: any;



@Component({
  selector: 'btn-cell-renderer',
  template: `
  <div class='action_col'>
  <span *ngIf="!params?.data?.gateway_type ||params?.data?.gateway_type == 'SENSOR'" style="margin-left: 31px;"></span>
  <span class='iconBtn icoBtn_edit' *ngIf="params?.data?.gateway_type == 'Gateway'||params?.data?.gateway_type == 'GATEWAY'"  (click)='editGateway()'>
  <i class='fa fa-pencil' aria-hidden='true'>
  </i></span>
  <span class='iconBtn icoBtn_delete' (click)='deleteGateway()'>
  <i  class='fa fa-trash' aria-hidden='true'>
  </i></span>
  <span class='iconBtn icoBtn_delete' *ngIf="params?.data?.status == 'Installed' || params?.data?.status == 'Problem' || params?.data?.status == 'Install in progress' || params?.data?.status == 'Active'"  (click)='resetGateway()'>
  <i  class='fa fa-repeat' aria-hidden='true'>
  </i></span>

  <span class='iconBtn icoBtn_delete' *ngIf="params?.data?.status == 'Active not IA'"  (click)='resetIndividualDevice()'>
  <i  class='fa fa-repeat' aria-hidden='true'>
  </i></span>
  </div>
  `,
})
export class BtnCellRendererForDeviceProvisioningComponent implements ICellRendererAngularComp, OnDestroy {

  public params: any;
  packageUuid;
  loading = false;
  agInit(params: any): void {
    this.params = params;
  }

  btnClickedHandler() {
    this.params.clicked(this.params.value);
  }

  constructor(private gatewayListService: GatewayListService, private router: Router, private toastr: ToastrService) {

  }

  refresh(params: any): boolean {
    throw new Error('Method not implemented.');
  }
  afterGuiAttached?(params?: import ('@ag-grid-community/all-modules').IAfterGuiAttachedParams): void {
    throw new Error('Method not implemented.');
  }

  ngOnDestroy() {
    // no need to remove the button click handler
    // https://stackoverflow.com/questions/49083993/does-angular-automatically-remove-template-event-listeners
  }

  deleteGateway() {
    this.gatewayListService.gatewayList = [];
    if (this.params.data.status.toLowerCase() !== 'pending') {
      this.toastr.error('The device with the ID ' + this.params.data.imei + ' is part of an installation and so it cannot be deleted.',
        'Problem with Deletion', { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
    } else {
      this.gatewayListService.gatewayList.push(this.params.data);
      $('#deleteGatewayModal').modal('show');
    }
  }

  editGateway(): void {
    this.gatewayListService.gatewayList = [];
    if (!this.params.data.status || this.params.data.status.toLowerCase() !== 'pending') {
      this.toastr.error('The selected device(s) are part of an in-progress or active installation, and cannot be modified.', 'Device Configuration Can’t Be Modified',
        { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
    } else {
      this.gatewayListService.gatewayList.push(this.params.data);
      this.router.navigate(['update-gateways']);
    }
  }

  resetGateway(): void {
    this.gatewayListService.gatewayList = [];
    if (this.params.data.status.toLowerCase() === 'installed' || this.params.data.status.toLowerCase() === 'problem' || this.params.data.status.toLowerCase() === 'install in progress' || this.params.data.status.toLowerCase() === 'active') {
      this.gatewayListService.gatewayList.push(this.params.data);
      $('#resetGatewayModal').modal('show');
    } else {
      this.toastr.error('The device with the ID ' + this.params.data.imei + ' is not part of an installation and so it cannot be reset.', 'Problem with Reset',
        { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
    }
  }

  resetIndividualDevice(): void {
    this.gatewayListService.gatewayList = [];
    if (this.params.data.status === 'Active not IA') {
      this.gatewayListService.gatewayList.push(this.params.data);
      $('#resetIndividualDevice').modal('show');
    } else {
      this.toastr.error('The device with the ID ' + this.params.data.imei + ' is not part of an installation and so it cannot be reset.',
        'Problem with Reset', { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
    }
  }
}
