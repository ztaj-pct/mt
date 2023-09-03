import { Component, OnDestroy } from '@angular/core';
import { ICellRendererAngularComp } from '@ag-grid-community/angular';

import { Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { CompaniesService } from '../companies.service';
declare var $: any;



@Component({
  selector: 'btn-cell-renderer',
  template: `
  <div class='action_col' *ngIf="!rolePCTUser">
  <span class='iconBtn icoBtn_edit'  (click)='editCompany()'>
  <i class='fa fa-pencil' aria-hidden='true'></i>
  </span>
  <span class='iconBtn icoBtn_delete' (click)='deleteCompany()'>
  <i  class='fa fa-trash' aria-hidden='true'></i>
  </span>
  </div>
  `,
})
export class BtnCellRendererForCompany implements ICellRendererAngularComp, OnDestroy {

  private params: any;
  packageUuid;
  rolePCTUser: any;
  loading = false;
  agInit(params: any): void {
    this.params = params;
  }

  btnClickedHandler() {
    this.params.clicked(this.params.value);
  }

  constructor(private companiesListService: CompaniesService, private router: Router, private toastr: ToastrService) {
    this.rolePCTUser = JSON.parse(sessionStorage.getItem('IS_ROLE_PCT_USER'));
  }

  refresh(params: any): boolean {
    throw new Error('Method not implemented.');
  }
  afterGuiAttached?(params?: import('@ag-grid-community/all-modules').IAfterGuiAttachedParams): void {
    throw new Error('Method not implemented.');
  }
  editCompany(): void {
    this.router.navigate(['add-company'], { queryParams: { id: this.params.data.id } });
  }

  ngOnDestroy() {
    // no need to remove the button click handler
    // https://stackoverflow.com/questions/49083993/does-angular-automatically-remove-template-event-listeners
  }

  deleteCompany() {
    this.companiesListService.company = this.params.data;
    if (this.params.data.status) {
      $('#inactiveCompanyModal').modal('show');
    } else {
      $('#inactiveCompanyModal').modal('hide');
    }
  }
}
