import { Component, OnDestroy } from '@angular/core';
import { ICellRendererAngularComp } from '@ag-grid-community/angular';
import { CampaignService } from '../campaign/campaign.component.service';
import { Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { CustomErrorToastrComponent } from '../custom-error-toastr/custom-error-toastr.component';
declare var $: any;


@Component({
  selector: 'btn-cell-renderer',
  template: `
  <div class='action_col'><span class='iconBtn icoBtn_edit'  (click)='editPackage()'><i class='fa fa-pencil' aria-hidden='true'></i></span><span class='iconBtn icoBtn_delete' (click)='deletePackage()'><i  class='fa fa-trash' aria-hidden='true'></i></span></div>
  `,
})
export class BtnCellRenderer implements ICellRendererAngularComp, OnDestroy {
    refresh(params: any): boolean {
        throw new Error("Method not implemented.");
    }
    afterGuiAttached?(params?: import("@ag-grid-community/all-modules").IAfterGuiAttachedParams): void {
        throw new Error("Method not implemented.");
    }
  private params: any;
  packageUuid;
  loading = false;
  agInit(params: any): void {
    this.params = params;
  }

  btnClickedHandler() {
    this.params.clicked(this.params.value);
  }

  constructor(private campaignService: CampaignService,  private router: Router, private toastr: ToastrService){
      
  }

  editPackage() {
      var uuid = this.params.data.uuid;
      this.loading = true;
      this.campaignService.findByPackageId(uuid).subscribe(data => {
        this.campaignService.editFlag = true;
        this.campaignService.packageDetail = data.body;
        this.loading = false;
        this.router.navigate(['package']);
      }, error => {
        console.log(error)
      });
  }

  ngOnDestroy() {
    // no need to remove the button click handler 
    // https://stackoverflow.com/questions/49083993/does-angular-automatically-remove-template-event-listeners
  }

  deletePackage() {
    if(!this.params.data.is_used_in_campaign) {
      $('#deletePackageModal').modal('show');
      this.campaignService.packageId = this.params.data.uuid;
    } else {
      this.toastr.error("User can not delete the In Use Package", "Delete Notification", { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
    }
  }

}
