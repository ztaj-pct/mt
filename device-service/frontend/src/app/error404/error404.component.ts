import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { settings } from 'cluster';
import { url } from 'inspector';
import { ToastrService } from 'ngx-toastr';
import { ShippedDeviceIcon } from '../layout/header/header.component';
import { UserService } from '../user/user.component.service';

@Component({
  selector: 'app-error404',
  templateUrl: './error404.component.html',
  styleUrls: ['./error404.component.css']
})
export class Error404Component  {

  isRoleCustomerManager;
  title = 'VisaTamplate';
  myVar = false;
  shippedDeviceIcon = ShippedDeviceIcon.icon;
  constructor(private route: Router, public editUserService: UserService, private toastr: ToastrService) {}


  settings() {
    let userId = sessionStorage.getItem('loggedInUser');
    this.editUserService.getUserById(parseInt(userId)).subscribe(data => {
      console.log(data);
      this.editUserService.editFlag = true;
      this.editUserService.navigateFromSettings = true;
      this.editUserService.user = data.body;
      this.route.navigate(['user']);
    }, error => {
      console.log(error)
    });
  }

  gotoAssetList() {
    this.isRoleCustomerManager = sessionStorage.getItem('IS_ROLE_CUSTOMER_MANAGER');
    if (this.isRoleCustomerManager === 'true')
      this.route.navigate(['assets']);
    else
      this.route.navigate(['customerAssets']);
  }

  redirectTo(uri: string) {
    this.route.navigateByUrl('/', { skipLocationChange: true }).then(() =>
      this.route.navigate([uri]));
  }

  gotoDesForRefresh(url) {
    this.redirectTo(url);
  }

  shippedDevices() {
    this.route.navigate(['shippedDevices']);
  }
}
 