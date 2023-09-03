import { Component, OnDestroy } from '@angular/core';
import { ICellRendererAngularComp } from '@ag-grid-community/angular';
import { Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { DateOption } from 'flatpickr/dist/types/options';
declare var $: any;



@Component({
  selector: 'measure-cell-renderer',
  template: `<span>{{ this.displayValue }}</span>`,
})
export class BtnCellRendererForDeviceReport implements ICellRendererAngularComp, OnDestroy {
  displayValue: string;

  refresh(params: any): boolean {
    throw new Error("Method not implemented.");
  }
  afterGuiAttached?(params?: import("@ag-grid-community/all-modules").IAfterGuiAttachedParams): void {
    throw new Error("Method not implemented.");
  }
  public params: any;
  packageUuid;
  loading = false;
  agInit(params: any): void {
    this.params = params;
    this.displayValue = this.showMeasureObject();
  }


  constructor(private router: Router, private toastr: ToastrService) {
  }
  ngOnDestroy() {
    // no need to remove the button click handler 
    // https://stackoverflow.com/questions/49083993/does-angular-automatically-remove-template-event-listeners
  }

  showMeasureObject(): string {
    if (this.params.value != null) {
      return JSON.stringify(this.params.value);
    }
  }
}



