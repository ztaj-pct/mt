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
  <div class='action_col'>
  <span class='iconBtn icoBtn_edit' *ngIf="!params?.data?.campaign_status.includes('% Complete') && (params?.data?.campaign_status !== 'Paused' && params?.data?.campaign_status !== 'Stopped')" style="margin-left: 17px;"></span>
  <span class='iconBtn icoBtn_edit' *ngIf="params?.data?.campaign_status.includes('% Complete')"  (click)='pauseCampaign()'><i class='fa fa-pause' aria-hidden='true'></i></span>
  <span class='iconBtn icoBtn_edit' *ngIf="params?.data?.campaign_status == 'Paused' || params?.data?.campaign_status == 'Stopped'"  (click)='playCampaign()'><i class='fa fa-play' aria-hidden='true'></i></span>
  <span class='iconBtn icoBtn_edit' (click)='editPackage()'><i class='fa fa-pencil' aria-hidden='true'></i></span></div>
  `,
})
//<span class='iconBtn icoBtn_delete' (click)='deleteCampaign()'><i  class='fa fa-trash' aria-hidden='true'></i></span>
export class BtnCellRendererForCampaign implements ICellRendererAngularComp, OnDestroy {
    refresh(params: any): boolean {
        throw new Error("Method not implemented.");
    }
    afterGuiAttached?(params?: import("@ag-grid-community/all-modules").IAfterGuiAttachedParams): void {
        throw new Error("Method not implemented.");
    }
  public params: any;
  public packageUuid;
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
      this.campaignService.findByCampaignId(uuid).subscribe(data => {
        this.campaignService.editFlag = true;
        this.campaignService.campaignDetail = data.body;
        this.loading = false;
        this.router.navigate(['add-campaign']);
      }, error => {
        console.log(error)
        this.loading = false;
      });
  }

  ngOnDestroy() {
    // no need to remove the button click handler 
    // https://stackoverflow.com/questions/49083993/does-angular-automatically-remove-template-event-listeners
  }

  deleteCampaign() {
	if(this.params.data.campaign_status == "Not Started") {
      $('#deleteCampaignModal').modal('show');
      this.campaignService.campaignId = this.params.data.uuid;
    } else {
      //this.toastr.error("You can not delete the In Progress Campaign");
      this.toastr.error("This campaign has already been started and cannot be deleted.","Campaign is In Use",{toastComponent: CustomErrorToastrComponent, tapToDismiss:true, disableTimeOut:true});
    }
    
  }

  pauseCampaign() {
    $('#pauseCampaignModal').modal('show');
    this.campaignService.campaignId = this.params.data.uuid;
  }

  playCampaign() {
    $('#playCampaignModal').modal('show');
    this.campaignService.campaignId = this.params.data.uuid;
  }

}