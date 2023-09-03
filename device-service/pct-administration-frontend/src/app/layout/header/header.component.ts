import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';

@Component({
  selector: 'header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.css']
})
export class HeaderComponent implements OnInit {
  isRoleCustomerManager:any;
  title = 'VisaTamplate';
  myVar = false;
  public sessionStorage = sessionStorage;
  isRoleCustomerManagerInstaller: any;
  userCompanyType :any;
  isSuperAdmin :any;
  constructor(private route: Router,private toastr: ToastrService) { }

  ngOnInit(): void {
    this.userCompanyType = sessionStorage.getItem('type');
    this.isSuperAdmin = sessionStorage.getItem('IS_ROLE_SUPERADMIN');
  
    this.isRoleCustomerManagerInstaller = sessionStorage.getItem('IS_ROLE_CUSTOMER_MANAGER_INSTALLER');
    console.log("isRoleCustomerManagerInstaller "+this.isRoleCustomerManagerInstaller);
  }

  gotoDesForRefresh() {
    alert("On click")
    this.isSuperAdmin = sessionStorage.getItem('IS_ROLE_SUPERADMIN');
    if(this.isSuperAdmin === 'true'){
      this.redirectTo('devices-list');
   }
  }
  redirectTo(uri: string) {
    this.route.navigateByUrl('/', { skipLocationChange: true }).then(() =>
      this.route.navigate([uri]));
  }

}
  