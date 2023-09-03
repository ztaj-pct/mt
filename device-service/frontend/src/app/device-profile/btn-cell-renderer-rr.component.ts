import { Component, OnDestroy } from '@angular/core';
import { ICellRendererAngularComp } from '@ag-grid-community/angular';
import { Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
declare var $: any;




@Component({
  selector: 'rawreport-cell-renderer',
  template: `<div style="color:#4ba9d7;"><a>{{ this.displayRawReport }}</a></div>`
})
export class BtnCellRendererForRawReport implements ICellRendererAngularComp, OnDestroy {
  displayValue: string;
  displayRawReport: string;

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
    this.displayRawReport = this.showRawReport();
  }


  constructor(private router: Router, private toastr: ToastrService) {
  }
  ngOnDestroy() {
    // no need to remove the button click handler 
    // https://stackoverflow.com/questions/49083993/does-angular-automatically-remove-template-event-listeners
  }

  showRawReport(): string{
    if (this.params.value != null) {
      return this.params.value;
    }
  }

  // btnClickedHandler() {
  //   alert(this.params.value)
  //   $('#rawReportModals').modal('show');
  // }

  
  showLatestRawReport(){
    $('#latestReportModel').modal('show');
  }
}



