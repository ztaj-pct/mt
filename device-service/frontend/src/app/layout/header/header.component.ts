import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { UserService } from '../../user/user.component.service';
import { ToastrService } from 'ngx-toastr';
import { AssetsService } from 'src/app/assets.service';
import { CampaignService } from 'src/app/campaign/campaign.component.service';
import { environment } from 'src/environments/environment';

@Component({
  selector: 'header-component',
  templateUrl: './header.component.html',

})
export class HeaderComponent implements OnInit {
  isRoleCustomerManager;
  title = 'VisaTamplate';
  myVar = false;
  shippedDeviceIcon = ShippedDeviceIcon.icon;
  public sessionStorage = sessionStorage;
  isRoleCustomerManagerInstaller: any;
  userCompanyType;
  isSuperAdmin;
  rolePCTUser: any;
  roleCallCenterUser: any;
  private baseUrl = environment.baseUrl;
  IaUiUrl = environment.IaUiUrl;

  constructor(private assetsService: AssetsService, private route: Router, public editUserService: UserService, private toastr: ToastrService, public campaignService: CampaignService) {
    this.userCompanyType = sessionStorage.getItem('type');
    this.isSuperAdmin = sessionStorage.getItem('IS_ROLE_SUPERADMIN');
    this.rolePCTUser = sessionStorage.getItem('IS_ROLE_PCT_USER');
    this.roleCallCenterUser = sessionStorage.getItem('IS_ROLE_CALL_CENTER_USER');
    this.isRoleCustomerManagerInstaller = sessionStorage.getItem('IS_ROLE_CUSTOMER_MANAGER_INSTALLER');

  }
  ngOnInit() {

  }


  logout() {
    const token = sessionStorage.getItem('token');
    sessionStorage.clear();
    // const url = this.baseUrl + '/app/logout?Authorization=Bearer ' + token;
    // const link = document.createElement('a');
    // link.href = url;
    // document.body.appendChild(link);
    // link.click();
    // // sessionStorage.clear();
    this.route.navigate(['login']);
  }
  settings() {
    const userId = sessionStorage.getItem('loggedInUser');
    this.editUserService.getUserById(parseInt(userId)).subscribe(data => {
      console.log(data);
      this.editUserService.editFlag = true;
      this.editUserService.navigateFromSettings = true;
      this.editUserService.user = data.body;
      this.route.navigate(['user']);
    }, error => {
      console.log(error);
    });
  }

  gotoAddAsst() {
    this.assetsService.editFlag = false;
    this.assetsService.asset = null;
    this.route.navigateByUrl('/', { skipLocationChange: true }).then(() =>
      this.route.navigate(['add-assets']));
  }

  gotoAssetList() {
    this.isRoleCustomerManager = sessionStorage.getItem('IS_ROLE_CUSTOMER_MANAGER');
    if (this.isRoleCustomerManager === 'true') {
      this.route.navigate(['gateway-list']);
    } else {
      this.route.navigate(['gateway-list']);
    }
  }

  landingPage() {
    const landingPageObj = JSON.parse(sessionStorage.getItem('landingPageValue'));
    if (landingPageObj) {
        this.route.navigate([landingPageObj.url]);
    }
  }

  redirectTo(uri: string) {
    this.route.navigateByUrl('/', { skipLocationChange: true }).then(() =>
      this.route.navigate([uri]));
  }

  gotoDesForRefresh(url) {
    this.isSuperAdmin = sessionStorage.getItem('IS_ROLE_SUPERADMIN');
    this.rolePCTUser = sessionStorage.getItem('IS_ROLE_PCT_USER');
    if (this.isSuperAdmin === 'true' || this.rolePCTUser === 'true') {
      this.campaignService.campaignDetail = null;
      this.redirectTo(url);
    }
  }

  gotoNavigation(url) {
    this.isSuperAdmin = sessionStorage.getItem('IS_ROLE_SUPERADMIN');
    if (this.isSuperAdmin === 'true') {
      this.redirectTo(url);
    }
  }
  shippedDevices() {
    this.route.navigate(['shippedDevices']);
  }

  openIaPage(key: any) {
    const token = localStorage.getItem('user2_token');
    const role = localStorage.getItem('role');
    const id = localStorage.getItem('id');
    switch (key) {
      case 'allPackage':
        window.open(this.IaUiUrl + '/#/packages?ms2=true&ms2uvid=' + token + '&role=' + role + '&id=' + id, '');
        break;
      case 'createPackage':
        window.open(this.IaUiUrl + '/#/package?ms2=true&ms2uvid=' + token + '&role=' + role + '&id=' + id, '');
        break;
      case 'uploadPackage':
        window.open(this.IaUiUrl + '/#/upload-packages?ms2=true&ms2uvid=' + token + '&role=' + role + '&id=' + id, '');
        break;
      case 'allCampaigns':
        window.open(this.IaUiUrl + '/#/campaigns?ms2=true&ms2uvid=' + token + '&role=' + role + '&id=' + id, '');
        break;
      case 'createCampaign':
        window.open(this.IaUiUrl + '/#/add-campaign?ms2=true&ms2uvid=' + token + '&role=' + role + '&id=' + id, '');
        break;
      case 'campaignsAnalytics':
        // window.open(this.IaUiUrl + '/#/customerAssets', '');
        break;
      case 'installationSummary':
        window.open(this.IaUiUrl + '/#/installation-summary?ms2=true&ms2uvid=' + token + '&role=' + role + '&id=' + id, '');
        break;
      case 'allAsset':
        window.open(this.IaUiUrl + '/#/all-assets?ms2=true&ms2uvid=' + token + '&role=' + role + '&id=' + id, '');
        break;
      case 'companyWithAsset':
        window.open(this.IaUiUrl + '/#/customerAssets?ms2=true&ms2uvid=' + token + '&role=' + role + '&id=' + id, '');
        break;
      case 'createAsset':
        window.open(this.IaUiUrl + '/#/add-assets?ms2=true&ms2uvid=' + token + '&role=' + role + '&id=' + id, '');
        break;
      case 'uploadAsset':
        window.open(this.IaUiUrl + '/#/upload-assets?ms2=true&ms2uvid=' + token + '&role=' + role + '&id=' + id, '');
        break;
      default:
        break;
    }
  }
}
export const ShippedDeviceIcon = {
  icon: 'iVBORw0KGgoAAAANSUhEUgAAABIAAAASCAYAAABWzo5XAAAAcklEQVR4AWMgAggAMQ8DhaAZiH8D8XcgTidWkwoQTwfi/VB8GIj/Q+nDUAP3I+F+IJbAZtBysEZMvBqEccj1YzNoP0iSRLyfkEH5QOyAA+fTzSDKvTbqtflUN4huXltPhkGzsRlkAjVsP5F4OTijD1oAAI/Ul3F7z+HsAAAAAElFTkSuQmCC'
};

