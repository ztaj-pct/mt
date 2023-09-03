import { Component, OnDestroy } from '@angular/core';
import { ICellRendererAngularComp } from '@ag-grid-community/angular';

import { Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { GatewayListService } from '../gateway-list.service';
import { CustomErrorToastrComponent } from '../custom-error-toastr/custom-error-toastr.component';
import { ATCommandService } from '../atcommand.service';
declare var $: any;



@Component({
  selector: 'btn-cell-renderer',
  template: `
  <div class='action_col'>
  <span *ngIf="params?.data?.imei != null"  (click)='resetGateway()'>
  <i  class='fa fa-eye' aria-hidden='true'>
  </i></span>
  &nbsp;&nbsp;
  <span *ngIf="!rolePCTUser" class='iconBtn icoBtn_edit' (click)='updateDataForwardRule()'>
  <i class='fa fa-pencil' aria-hidden='true'>
  </i></span>
  </div>
  `,
})
export class BtnCellRendererForGatewayDetails implements ICellRendererAngularComp, OnDestroy {

  public params: any;
  packageUuid;
  loading = false;
  rolePCTUser: any;

  refresh(params: any): boolean {
    throw new Error('Method not implemented.');
  }
  afterGuiAttached?(params?: import('@ag-grid-community/all-modules').IAfterGuiAttachedParams): void {
    throw new Error('Method not implemented.');
  }
  agInit(params: any): void {
    this.params = params;
  }

  btnClickedHandler() {
    this.params.clicked(this.params.value);
    this.resetGateway();
  }

  constructor(private readonly atcommandService: ATCommandService, private gatewayListService: GatewayListService, private router: Router, private toastr: ToastrService) {
    this.rolePCTUser = JSON.parse(sessionStorage.getItem('IS_ROLE_PCT_USER'));
  }
  ngOnDestroy() {
    // no need to remove the button click handler
    // https://stackoverflow.com/questions/49083993/does-angular-automatically-remove-template-event-listeners
  }

  deleteGateway() {
    this.gatewayListService.gatewayList = [];
    if (this.params.data.status != 'Pending') {
      this.toastr.error('The device with the ID ' + this.params.data.imei + ' is part of an installation and so it cannot be deleted.', 'Problem with Deletion', { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
    } else {
      this.gatewayListService.gatewayList.push(this.params.data);
      $('#deleteGatewayModal').modal('show');
    }
  }

  editGateway(): void {
    this.gatewayListService.gatewayList = [];
    if (this.params.data.status !== 'Pending') {
      this.toastr.error('The selected device(s) are part of an in-progress or active installation, and cannot be modified.', 'Device Configuration Can’t Be Modified', { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
    } else {
      this.gatewayListService.gatewayList.push(this.params.data);
      this.router.navigate(['update-gateways']);
    }
  };

  resetGateway(): void {
    if (this.params.data.imei != null) {
      sessionStorage.setItem('device_id', this.params.data.imei);
      console.log(sessionStorage.getItem('device_id'));
      sessionStorage.setItem('device_uuid', this.params.data.uuid);
      this.atcommandService.deviceId = this.gatewayListService.deviceId;

      this.router.navigate(['gateway-profile']);
    }
    // this.gatewayListService.gatewayList = [];
    // if (this.params.data.status == 'Installed' || this.params.data.status == 'Problem' || this.params.data.status == 'Install in progress' || this.params.data.status == 'Active') {
    //   this.gatewayListService.gatewayList.push(this.params.data);
    //   $('#resetGatewayModal').modal('show');
    //   } else {
    //     this.toastr.error("The device with the ID "+this.params.data.imei+" is not part of an installation and so it cannot be reset.","Problem with Reset",{toastComponent: CustomErrorToastrComponent, tapToDismiss:true, disableTimeOut:true});
    // }
  }

  updateDataForwardRule(): void {
    this.gatewayListService.gatewayList = [];
    this.gatewayListService.gatewayList.push(this.params.data);
    this.router.navigate(['gateway-data-forward-rule']);
  }
}
