import { Component, OnDestroy } from '@angular/core';
import { ICellRendererAngularComp } from '@ag-grid-community/angular';

import { Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { AssetsService } from '../assets.service';
import { Asset } from '../models/asset';
declare var $: any;



@Component({
  selector: 'btn-cell-render',
  template: `
  <input type="checkbox"  [(ngModel)]="isChecked" (change)="checkbox()">
  `,
})
export class BtnCellRendererForCheckbox implements ICellRendererAngularComp, OnDestroy {
  userCompanyType: string;
  isSuperAdmin: string;
  isRoleCustomerManager: string;
  assetList: Asset[] = Array(new Asset());
  asset: any;
  isChecked = false;
  private params: any;
  packageUuid;
  loading = false;
  isRoleOrgAdmin: any;
  isRoleOrgUser: any;
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

  ngOnDestroy() {
    this.userCompanyType = sessionStorage.getItem('type');
    this.isSuperAdmin = sessionStorage.getItem('IS_ROLE_SUPERADMIN');
    this.isRoleCustomerManager = sessionStorage.getItem('IS_ROLE_CUSTOMER_MANAGER');
    this.isRoleOrgAdmin = JSON.parse(sessionStorage.getItem('IS_ROLE_ORG_ADMIN'));
    this.isRoleOrgUser = JSON.parse(sessionStorage.getItem('IS_ROLE_ORG_USER'));
  }


  checkbox() {
    if (this.isChecked) {
      this.asset = this.params.data;

      console.log(this.asset);
      if (this.assetsService.selectedIdsToDelete.find(x => x == this.asset.asset_uuid)) {
        this.assetsService.selectedIdsToDelete.splice(this.assetsService.selectedIdsToDelete.indexOf(this.asset.asset_uuid), 1)
      } else {
        this.assetsService.selectedIdsToDelete.push(this.asset.asset_uuid);
      }
      console.log(this.assetsService.selectedIdsToDelete);
      if (this.assetList.length > 0 && this.assetList.length === this.assetsService.selectedIdsToDelete.length) {
        this.isChecked = true;
      } else {
        this.isChecked = false;
      }
    }
  }
  editAsset(): void {
    console.log(this.params.data);
    var uuid = this.params.data;
    this.loading = true;
    if (this.userCompanyType === 'Manufacturer' || this.isSuperAdmin === 'true' || this.isRoleCustomerManager === 'true' || this.isRoleOrgAdmin || this.isRoleOrgUser) {
      this.assetsService.asset = uuid;
      this.assetsService.editFlag = true;
      this.router.navigate(['add-assets']);
    }
  }


  deleteAsset() {
    console.log(this.params.data);
    $('#deleteAssetModal').modal('show');

  }
}
