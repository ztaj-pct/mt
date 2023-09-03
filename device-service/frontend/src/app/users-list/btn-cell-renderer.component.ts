import { Component, OnDestroy } from '@angular/core';
import { ICellRendererAngularComp } from '@ag-grid-community/angular';
import { Router } from '@angular/router';
import { UserService } from '../user/user.component.service';
declare var $: any;
@Component({
  selector: 'btn-cell-renderer',
  template: `
  <div class='action_col' *ngIf="params.data&&!isRolePctUser&&(!isRoleOrgUser||params.data.email===userName)"><span class='iconBtn icoBtn_edit'  (click)='editUser()'>
  <i class='fa fa-pencil' aria-hidden='true'></i></span><span class='iconBtn icoBtn_delete' (click)='deleteUser()'><i  class='fa fa-trash' aria-hidden='true'></i></span></div>
  `,
})
export class BtnCellRendererForUser implements ICellRendererAngularComp, OnDestroy {
  isRolePctUser: any;
  isRoleOrgUser: any;
  userName: any;
  constructor(
    private usersService: UserService,
    private router: Router) {

    this.isRolePctUser = JSON.parse(sessionStorage.getItem('IS_ROLE_PCT_USER'));
    this.isRoleOrgUser = JSON.parse(sessionStorage.getItem('IS_ROLE_ORG_USER'));
    this.userName = sessionStorage.getItem('email');
  }
  public params: any;
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
  editUser(): void {
    // console.log(this.params.data);
    // let uuid = this.params.data.id;
    // this.loading = true;
    // this.usersService.editFlag = true;
    // this.usersService.getUserById(uuid).subscribe(data => {
    //   this.usersService.user = data.body;
    //   this.usersService.user.role = this.usersService.user.role;
    //   this.usersService.user.notify = this.usersService.user.notify;
    this.router.navigate(['add-user'], { queryParams: { id: this.params.data.id } });
    // }, error => {
    //   console.log(error);
    //   this.loading = false;
    // });
  }

  ngOnDestroy() {
    // no need to remove the button click handler
    // https://stackoverflow.com/questions/49083993/does-angular-automatically-remove-template-event-listeners
  }

  deleteUser() {
    this.usersService.user = this.params.data;
    if (this.params.data.is_active) {
      $('#inactiveUserModal').modal('show');
    } else {
      $('#inactiveUserModal').modal('hide');
    }
  }
}
