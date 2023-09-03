import { Component, OnDestroy } from "@angular/core";
import { CampaignService } from '../campaign/campaign.component.service';
import { Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { ICellRendererAngularComp } from "@ag-grid-community/angular";

@Component({
    selector: "campaign-hyperlink-renderer",
    template: `{{params.value}}`
})
export class CampaignHyperlinkRenderer implements ICellRendererAngularComp, OnDestroy {
    refresh(params: any): boolean {
        throw new Error("Method not implemented.");
    }
    afterGuiAttached?(params?: import("@ag-grid-community/all-modules").IAfterGuiAttachedParams): void {
        throw new Error("Method not implemented.");
    }

    constructor(private campaignService: CampaignService, private router: Router, private toastr: ToastrService) {

    }
    public params: any;
    loading = false;

    agInit(params: any): void {
        this.params = params;
    }

    btnClickedHandler(link) {
        this.gotoOnCampaign(link);
    }

    gotoOnCampaign(uuid) {
        this.loading = true;
        var uuid = uuid;
        this.campaignService.findByCampaignId(uuid).subscribe(data => {
            this.campaignService.editFlag = true;
            this.campaignService.campaignDetail = data.body;
            this.loading = false;
            this.router.navigate(['add-campaign']);
        }, error => {
            console.log(error)
        });
    }

    ngOnDestroy() {
        // no need to remove the button click handler
        // https://stackoverflow.com/questions/49083993/does-angular-automatically-remove-template-event-listeners
    }
}
