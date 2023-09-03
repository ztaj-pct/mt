import { Component, EventEmitter, OnDestroy, Output } from '@angular/core';
import { ICellRendererAngularComp } from '@ag-grid-community/angular';

import { Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { CustomErrorToastrComponent } from '../custom-error-toastr/custom-error-toastr.component';
import { ATCommandService } from '../atcommand.service';
import { GatewayListService } from '../gateway-list.service';
import { ATCRequests } from '../models/atrequest.model';
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
  atcRequest : ATCRequests;
  public params: any;
  packageUuid;
  loading = false;
  agInit(params: any): void {
    this.params = params;
  }

  btnClickedHandler() {
    this.params.clicked(this.params.value);
  }
 @Output() refreshEvent = new EventEmitter<string>();
  constructor(private atcommandService: ATCommandService, private router: Router, private toastr: ToastrService,public gatewayListService: GatewayListService) {

  }
 
  ngOnDestroy() {
    // no need to remove the button click handler 
    // https://stackoverflow.com/questions/49083993/does-angular-automatically-remove-template-event-listeners
  }

  deleteATCommand() {
    this.gatewayListService.gatewayList = [];
    if (this.params.data.status != 'Pending') {
      this.atcRequest = new ATCRequests();
      this.atcRequest.gateway_id = sessionStorage.getItem('device_id');
      this.atcRequest.at_command = this.params.data.at_command;
      this.atcRequest.priority = this.params.data.priority;
      this.atcRequest.uuid = this.params.data.uuid;
      this.atcommandService.deleteAtCommandById(this.atcRequest).subscribe(data => {
        this.params.api.onFilterChanged();
      }, error => {
        console.log(error)
      });
    }
  }


   
}