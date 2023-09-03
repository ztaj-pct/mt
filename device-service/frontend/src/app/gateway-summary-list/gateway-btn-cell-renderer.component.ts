import { Component, OnDestroy } from '@angular/core';
import { ICellRendererAngularComp } from '@ag-grid-community/angular';

import { Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { AssetsService } from '../assets.service';
declare var $: any;



@Component({
    selector: 'app-gateway-btn-cell-renderer',
    template: `
  <div class="action_col">
  <span class="iconBtn icoBtn_edit" (click)="viewDeviceProvisioning()">
    <i class="fa fa-list" aria-hidden="true"></i>
    </span>
</div>
  `,
})
export class GatewayBtnCellRendererComponent implements ICellRendererAngularComp, OnDestroy {

    private params: any;
    packageUuid;
    loading = false;
    constructor(private assetsService: AssetsService, private router: Router, private toastr: ToastrService) {

    }

    agInit(params: any): void {
        this.params = params;
    }

    btnClickedHandler() {
        this.params.clicked(this.params.value);
    }

    refresh(params: any): boolean {
        throw new Error('Method not implemented.');
    }

    afterGuiAttached?(params?: import ('@ag-grid-community/all-modules').IAfterGuiAttachedParams): void {
        throw new Error('Method not implemented.');
    }
    viewDeviceProvisioning(): void {
        this.router.navigate(['device-provisioning'], { queryParams: { id: this.params.data.organisation_id } });
    }

    ngOnDestroy() {
        // no need to remove the button click handler
        // https://stackoverflow.com/questions/49083993/does-angular-automatically-remove-template-event-listeners
    }

    exportAssetList() {
        // console.log(this.params.data);
        // this.assetsService.exportAssetList(this.params.data.company_id).subscribe(data => {
        //   console.log(data);
        // }, error => {
        //   console.log(error);
        // });
    }
}
