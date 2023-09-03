import { Component, OnDestroy } from '@angular/core';
import { ICellRendererAngularComp } from '@ag-grid-community/angular';

import { Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { AssetsService } from '../assets.service';
declare var $: any;



@Component({
  selector: 'btn-cell-renderer',
  template: `
  <div class='action_col'><span class='iconBtn icoBtn_edit'  (click)='editAsset()'><i class='fa fa-pencil' aria-hidden='true'></i></span>
  <span class='iconBtn icoBtn_delete' (click)='deleteAsset()'><i  class='fa fa-trash' aria-hidden='true'></i></span></div>
  `,
})
export class BtnCellRendererForAssets implements ICellRendererAngularComp, OnDestroy {
  userCompanyType: string;
  isSuperAdmin: string;
  isRoleCustomerManager: string;
  isRoleOrgAdmin: any;
  isRoleOrgUser: any;
  private params: any;
  packageUuid;
  loading = false;
  roleCallCenterUser: any;
  rolePCTUser: any;

  agInit(params: any): void {
    this.params = params;
    this.userCompanyType = sessionStorage.getItem('type');
    this.isSuperAdmin = sessionStorage.getItem('IS_ROLE_SUPERADMIN');
    this.isRoleCustomerManager = sessionStorage.getItem('IS_ROLE_CUSTOMER_MANAGER');
    this.roleCallCenterUser = JSON.parse(sessionStorage.getItem('IS_ROLE_CALL_CENTER_USER'));
    this.isRoleOrgAdmin = JSON.parse(sessionStorage.getItem('IS_ROLE_ORG_ADMIN'));
    this.isRoleOrgUser = JSON.parse(sessionStorage.getItem('IS_ROLE_ORG_USER'));
    this.rolePCTUser = JSON.parse(sessionStorage.getItem('IS_ROLE_PCT_USER'));
  }

  refresh(params: any): boolean {
    throw new Error('Method not implemented.');
  }
  afterGuiAttached?(params?: import('@ag-grid-community/all-modules').IAfterGuiAttachedParams): void {
    throw new Error('Method not implemented.');
  }

  btnClickedHandler() {
    this.params.clicked(this.params.value);
  }

  constructor(private assetsService: AssetsService, private router: Router, private toastr: ToastrService) {
  }
  viewComapnyAssets(): void {
    console.log(this.params.data);
    this.assetsService.assetCompanyId = this.params.data.company_id;
    this.router.navigate(['assets']);
  }

  ngOnDestroy() {
    this.userCompanyType = sessionStorage.getItem('type');
    this.isSuperAdmin = sessionStorage.getItem('IS_ROLE_SUPERADMIN');
    this.isRoleCustomerManager = sessionStorage.getItem('IS_ROLE_CUSTOMER_MANAGER');
    this.isRoleOrgAdmin = JSON.parse(sessionStorage.getItem('IS_ROLE_ORG_ADMIN'));
    this.isRoleOrgUser = JSON.parse(sessionStorage.getItem('IS_ROLE_ORG_USER'));
    this.rolePCTUser = JSON.parse(sessionStorage.getItem('IS_ROLE_PCT_USER'));
  }

  editAsset(): void {
    this.loading = true;
    if (this.userCompanyType === 'Manufacturer' || this.isSuperAdmin === 'true' || this.isRoleCustomerManager === 'true' || this.roleCallCenterUser || this.isRoleOrgAdmin || this.isRoleOrgUser || this.rolePCTUser) {
      this.router.navigate(['add-assets'], { queryParams: { id: this.params.data.asset_uuid, organisationId: this.assetsService.organisationId } });
    }
  }


  deleteAsset() {
    if (this.userCompanyType === 'Manufacturer' || this.isSuperAdmin === 'true' || this.isRoleCustomerManager === 'true' || this.roleCallCenterUser || this.isRoleOrgAdmin || this.isRoleOrgUser || this.rolePCTUser) {
      console.log(this.params.data);
      $('#deleteAssetModal').modal('show');
    }
  }
}
