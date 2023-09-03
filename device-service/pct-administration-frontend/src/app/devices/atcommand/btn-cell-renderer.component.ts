import { Component, OnDestroy } from '@angular/core';
import { ICellRendererAngularComp } from '@ag-grid-community/angular';

import { Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { AtcommandService } from './atcommand.service';
declare var $: any;



@Component({
  selector: 'btn-cell-renderer',
  template: `
  <div class='action_col'>
  <span class='iconBtn icoBtn_delete' (click)='deleteATCommand()'>
  <i  class='fa fa-trash' aria-hidden='true'></i>
  </span>
  </div>
  `,
})
export class BtnCellRendererForATCommand implements ICellRendererAngularComp, OnDestroy {
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

  constructor(private atcommandService: AtcommandService, private router: Router, private toastr: ToastrService) {

  }
  ngOnDestroy() {
    // no need to remove the button click handler 
    // https://stackoverflow.com/questions/49083993/does-angular-automatically-remove-template-event-listeners
  }

  deleteATCommand() {
  //   this.gatewayListService.gatewayList = [];
  //   if (this.params.data.status != 'Pending') {
  //     this.toastr.error("The device with the ID "+this.params.data.imei+" is part of an installation and so it cannot be deleted.","Problem with Deletion",{toastComponent: CustomErrorToastrComponent, tapToDismiss:true, disableTimeOut:true});
  //   } else {
  //     this.gatewayListService.gatewayList.push(this.params.data);
  //     $('#deleteGatewayModal').modal('show');
  //   }
   }
}