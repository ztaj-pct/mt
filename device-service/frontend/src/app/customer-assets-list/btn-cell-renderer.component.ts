import { Component, OnDestroy } from '@angular/core';
import { ICellRendererAngularComp } from '@ag-grid-community/angular';

import { Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { AssetsService } from '../assets.service';
declare var $: any;



@Component({
  selector: 'btn-cell-renderer',
  template: `
  <div class="action_col">
  <span class="iconBtn icoBtn_edit" (click)="viewComapnyAssets()">
    <i class="fa fa-list" aria-hidden="true"></i></span>
  <span class="iconBtn icoBtn_delete" (click)="exportAssetList()"><i class="fa fa-download"
      aria-hidden="true"></i></span>
</div>
  `,
})
export class BtnCellRendererForCustomAsset implements ICellRendererAngularComp, OnDestroy {
  private params: any;
  packageUuid;
  loading = false;
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
  }

  constructor(private assetsService: AssetsService, private router: Router, private toastr: ToastrService) {

  }
  viewComapnyAssets(): void {
    console.log(this.params.data);
    this.assetsService.assetCompanyId = this.params.data.organisation_id;
    this.router.navigate(['assets'], { queryParams: { id: this.params.data.organisation_id } });
  }

  ngOnDestroy() {
    // no need to remove the button click handler
    // https://stackoverflow.com/questions/49083993/does-angular-automatically-remove-template-event-listeners
  }

  exportAssetList() {
    this.params.context.componentParent.loading = true;
    this.assetsService.getActiveAssetsWithSortForDownload(0,
      'assignedName', 'asc', this.params.data.organisation_id, {}, 20000000, undefined).subscribe(data => {
        this.params.context.componentParent.loading = false;
      }, error => {
        this.params.context.componentParent.loading = false;
        console.log(error);
      });
  }
}
