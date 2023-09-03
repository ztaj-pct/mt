import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { LoginComponent } from './login/login.component';
import { UsersListComponent } from './users-list/users-list.component';
import { ResetPasswordComponent } from './reset-password/reset-password.component';
import { ForGotPasswordComponent } from './forgotpassword/forgotpassword.component';
import { ForGotPasswordModule } from './forgotpassword/forgotpassword.commponent.module';
import { DeviceSummaryReportComponent } from './device-summary-report/device-summary-report.component';
import { AssetsComponent } from './assets/assets.component';
import { AssetsListComponent } from './assets-list/assets-list.component';
import { TentativeComponent } from './tentative/tentative.component';
import { AddAssetsComponent } from './add-assets/add-assets.component';
import { CompanyListComponent } from './company-list/company-list.component';
import { CustomerAssetsListComponent } from './customer-assets-list/customer-assets-list.component';
import { CompanyNewComponent } from './company/company.newcomponent';
import { ResetInstallationStatusComponent } from './reset-installation-status/reset-installation-status.component';
import { ShippedDevicesComponent } from './shipped-devices/shipped-devices.component';
import { CampaignComponent } from './campaign/campaign.component';
import { PackagesListComponent } from './packages-list/packages-list.component';
import { UploadPackagesComponent } from './upload-packages/upload-packages.component';
import { AddCampaignComponent } from './add-campaign/add-campaign.component';
import { CampaignsListComponent } from './campaigns-list/campaigns-list.component';
import { Error404Component } from './error404/error404.component';
import { DirtyCheckGuard } from './dirty-gaurd-check/dirty-check.gaurd';
import { InstallationSummaryComponent } from './installation-summary-report/installation-summary-list.component';
import { InstallationDetailsComponent } from './installation-details/installation-details.component';
import { AddGatewayComponent } from './add-gateway/add-gateway.component';
import { GatewayListComponent } from './gateway-list/gateway-list.component';
import { UpdateGatewayComponent } from './update-gateway/update-gateway.component';
import { UploadGatewayComponent } from './upload-gateway/upload-gateway.component';
import { GatewaySummaryListComponent } from './gateway-summary-list/gateway-summary-list.component';
import { DeviceProfieComponent } from './device-profile/device-profile.component';
import { ATCommandListComponent } from './atcommand-list/atcommand-list.component';
import { AddATCommandComponent } from './add-atcommand/add-atcommand.component';
import { DeviceReportComponent } from './device-reports/device-reports.component';
import { TlvColumndefsComponent } from './tlv-columndefs/tlv-columndefs.component';
import { AllDeviceReportsComponent } from './all-device-reports/all-device-reports.component';
import { DeviceProvisioningComponent } from './device-provisioning/device-provisioning.component';
import { AddUserComponent } from './add-user/add-user.component';
import { EditDeviceComponent } from './edit-device/edit-device.component';
import { DeviceReportCountComponentComponent } from './device-report-count-component/device-report-count-component.component';
import { ReportCountComponentComponent } from './report-count-component/report-count-component.component';
import { DeviceForwardingRedisComponent } from './device-forwarding-redis/device-forwarding-redis.component';
import { UploadAssetAssociationsComponent } from './upload-asset-associations/upload-asset-associations.component';
import { GatewayDataForwardRuleComponent } from './gateway-list-data-forwarding-rule/gateway-data-forward-rule.component';
import { BatchDeviceEditComponent } from './batch-device-edit/batch-device-edit.component';
import { MaintenanceSummaryReportComponent } from './maintenance-summary-report/maintenance-summary-report.component';



const routes: Routes = [{
  path: '',

  children: [
    {
      path: 'login',
      component: LoginComponent
    },
    {
      path: 'users',
      component: UsersListComponent
    },

    {
      path: 'reset-password',
      component: ResetPasswordComponent
    },
    {
      path: 'device-summary',
      component: DeviceSummaryReportComponent
    },
    {
      path: 'forgot-password',
      component: ForGotPasswordComponent
    },
    {
      path: 'add-assets',
      component: AddAssetsComponent
    },
    {
      path: 'upload-assets',
      component: AssetsComponent
    },
    {
      path: 'upload-devices',
      component: UploadGatewayComponent
    },
    {
      path: 'add-user',
      component: AddUserComponent
    },

    {
      path: 'assets',
      component: AssetsListComponent
    },
    {
      path: 'gatewaysummary',
      component: GatewaySummaryListComponent
    },
    {
      path: 'installation-summary',
      component: InstallationSummaryComponent
    },
    {
      path: 'maintenance-summary',
      component: MaintenanceSummaryReportComponent
    },
    {
      path: 'installation-details',
      component: InstallationDetailsComponent
    },
    {
      path: 'add-gateway',
      component: AddGatewayComponent,
    },
    {
      path: 'update-gateways',
      component: UpdateGatewayComponent,
    },
    {
      path: 'gateway-list',
      component: GatewayListComponent,
    },
    {
      path: 'gateway-profile',
      component: DeviceProfieComponent,
    },
    {
      path: 'atcommand-list',
      component: ATCommandListComponent,
    },
    {
      path: 'add-atcommand',
      component: AddATCommandComponent,
    },
    {
      path: 'gateway-reports',
      component: DeviceReportComponent,
    },
    {
      path: 'tlv-columndefs',
      component: TlvColumndefsComponent,
    },
    {
      path: 'tentative',
      component: TentativeComponent
    },
    {
      path: 'company',
      component: CompanyListComponent
    },
    {
      path: 'add-company',
      component: CompanyNewComponent
    },
    {
      path: 'customerAssets',
      component: CustomerAssetsListComponent
    },
    {
      path: 'shippedDevices',
      component: ShippedDevicesComponent
    },
    {
      path: 'reset-installation-status',
      component: ResetInstallationStatusComponent
    },
    {
      path: 'packages',
      component: PackagesListComponent
    },
    {
      path: 'campaigns',
      component: CampaignsListComponent
    },
    {
      path: 'package',
      component: CampaignComponent,
      canDeactivate: [DirtyCheckGuard]
    },
    {
      path: 'add-campaign',
      component: AddCampaignComponent,
      canDeactivate: [DirtyCheckGuard]
    },
    {
      path: 'upload-packages',
      component: UploadPackagesComponent
    },
    {
      path: 'all-report-list',
      component: AllDeviceReportsComponent,
    },
    {
      path: 'device-provisioning',
      component: DeviceProvisioningComponent,
    },
    {
      path: 'edit-device',
      component: EditDeviceComponent,
    },
    {
      path: 'latest-report-count',
      component: DeviceReportCountComponentComponent,
    },
    {
      path: 'report-count',
      component: ReportCountComponentComponent,
    },
    {
      path: 'device-forwarding-redis',
      component: DeviceForwardingRedisComponent
    },
    {
      path: 'load-asset-association',
      component: UploadAssetAssociationsComponent
    },
    {
      path: 'gateway-data-forward-rule',
      component: GatewayDataForwardRuleComponent
    },
    {
      path: 'batch-device-edit',
      component: BatchDeviceEditComponent
    },
    {
      path: '',
      redirectTo: 'login',
      pathMatch: 'full',
    },
    {
      path: 'error',
      component: Error404Component
    },
    {
      path: '**',
      component: Error404Component
    },
  ],


}, {

  path: '**',
  component: Error404Component

}
];

@NgModule({
  imports: [RouterModule.forRoot(routes), ForGotPasswordModule],
  exports: [RouterModule]
})
export class AppRoutingModule { }
