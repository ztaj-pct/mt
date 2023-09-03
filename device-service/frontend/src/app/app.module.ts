import {MatRadioModule} from '@angular/material/radio';
import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { APP_BASE_HREF, LocationStrategy, HashLocationStrategy, CommonModule, DatePipe } from '@angular/common';
import { HeaderModule } from './layout/header/header.component.module';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { ToastrModule } from 'ngx-toastr';
import { RouterModule } from '@angular/router';
import { LoginModule } from './login/login.module';
import { HttpClientModule, HTTP_INTERCEPTORS} from '@angular/common/http';
import { UsersListModule } from './users-list/users-list.module';
import { ResetPasswordModule } from './reset-password/reset-password.module';
import { JwtInterceptor } from './_helpers/jwt.interceptor';
import { ErrorInterceptor } from './_helpers/error.interceptor';
import { PasswordStrengthModule } from './password-strength/password-strength.module';
import { DeviceSummaryReportComponent } from './device-summary-report/device-summary-report.component';
import { AssetsComponent } from './assets/assets.component';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { AssetsListComponent } from './assets-list/assets-list.component';
import { DataTableModule } from 'angular-6-datatable';
import { TentativeComponent } from './tentative/tentative.component';
import { AddAssetsComponent } from './add-assets/add-assets.component';
import { DndDirective } from './assets/dnd.directive';
import {ModuleRegistry, AllModules} from '@ag-grid-enterprise/all-modules';
import { NgMultiSelectDropDownModule } from 'ng-multiselect-dropdown';
import { CustomerAssetModule } from './customer-assets-list/customer-assets-module';
import { CompanyModule } from './company/company-module';
import { UserTimeModuleModule } from './util/userdate.module';
import {  DateAdapter, MatAutocompleteModule, MatCheckboxModule, MatDatepickerModule, MatFormFieldModule, MatInputModule, MatNativeDateModule, MatSelectModule, MatTabsModule, MAT_DATE_FORMATS, MAT_DATE_LOCALE } from '@angular/material';
// import { NgxMatDatetimePickerModule, NgxMatTimepickerModule, NgxMatNativeDateModule } from '@angular-material-components/datetime-picker';
import { ResetInstallationStatusComponent } from './reset-installation-status/reset-installation-status.component';
import { ShippedDevicesComponent } from './shipped-devices/shipped-devices.component';
import { CustomSuccessToastrComponent } from './custom-success-toastr/custom-success-toastr.component';
import { CustomErrorToastrComponent } from './custom-error-toastr/custom-error-toastr.component';
import { CustomBatchErrorToastrComponent } from './custom-batch-error-toastr/custom-batch-error-toastr.component';

import { BtnCellRendererForCompany } from './company-list/btn-cell-renderer.component';
import { BtnCellRendererForUser } from './users-list/btn-cell-renderer.component';
import { BtnCellRendererForCustomAsset } from './customer-assets-list/btn-cell-renderer.component';
import { BtnCellRendererForAssets } from './assets-list/btn-cell-renderer.component';
import { BtnCellRendererForCheckbox } from './assets-list/btn-cell-render.component';
import { CustomHeader } from './ui-elements/custom-header.component';
import { CustomDateComponent } from './ui-elements/custom-date-component.component';


import { UploadPackagesComponent } from './upload-packages/upload-packages.component';
import { CampaignModule } from './campaign/campaign.module';
import { PackagesListModule } from './packages-list/packages-listmodule';
import { AddCampaignModule } from './add-campaign/add-campaign.module';
import { AgGridModule } from '@ag-grid-community/angular';
import { BtnCellRenderer } from './packages-list/btn-cell-renderer.component';
import { CampaignsListModule } from './campaigns-list/campaigns-list-module';
import { BtnCellRendererForCampaign } from './campaigns-list/btn-cell-renderer.component';
import { HighchartsService } from './add-campaign/highcharts.service';
import { CampaignHyperlinkRenderer } from './add-campaign/campaign-hyperlink.component';
import { Error404Component } from './error404/error404.component';
import { InstallationSummaryComponent } from './installation-summary-report/installation-summary-list.component';
import { InstallationDetailsComponent } from './installation-details/installation-details.component';
import { CustomTooltip } from './ui-elements/custom-tooltip.component';
import { CustomCampaignsHeader } from './ui-elements/custom-campaigns-header.component';
import { AddGatewayComponent } from './add-gateway/add-gateway.component';
import { UpdateGatewayComponent } from './update-gateway/update-gateway.component';
import { GatewayListComponent } from './gateway-list/gateway-list.component';
import { BtnCellRendererForGatewayDetails } from './gateway-list/btn-cell-renderer.component';
import { UploadGatewayComponent } from './upload-gateway/upload-gateway.component';
import { GatewaySummaryListComponent } from './gateway-summary-list/gateway-summary-list.component';
import { DeviceProfieComponent } from './device-profile/device-profile.component';
import { ATCommandListComponent } from './atcommand-list/atcommand-list.component';
import { AddATCommandComponent } from './add-atcommand/add-atcommand.component';
import { BtnCellRendererForATCommand } from './atcommand-list/btn-cell-renderer.component';
import { DeviceReportComponent } from './device-reports/device-reports.component';
import { AtcommandSummaryComponent } from './atcommand-summary/atcommand-summary.component';
import { DeviceForwardingComponent } from './device-forwarding/device-forwarding.component';
import { DeviceForwardingRedisComponent } from './device-forwarding-redis/device-forwarding-redis.component';
import { DeviceDataForwardingComponent } from './device-data-forwarding/device-data-forwarding.component';
import { TlvColumndefsComponent } from './tlv-columndefs/tlv-columndefs.component';
import { BtnCellRendererForDeviceReport } from './device-profile/btn-cell-renderer.component';
import { BtnCellRendererForRawReport } from './device-profile/btn-cell-renderer-rr.component';
import { CampaignTabComponent } from './campaign-tab/campaign-tab.component';
import { AllDeviceReportsComponent } from './all-device-reports/all-device-reports.component';
import { OrderByPipe } from './pipes/order-by.pipe';
import {MomentDateAdapter, MAT_MOMENT_DATE_ADAPTER_OPTIONS, MatMomentDateModule} from '@angular/material-moment-adapter';
import * as _moment from 'moment';
import { BtnCellRendererForRTC } from './device-profile/btn-cell-renderer-rtc.component';
import { DeviceProvisioningComponent } from './device-provisioning/device-provisioning.component';
import { BtnCellRendererForDeviceProvisioningComponent } from './device-provisioning/btn-cell-renderer.component';
import { GatewayBtnCellRendererComponent } from './gateway-summary-list/gateway-btn-cell-renderer.component';
import { AddUserComponent } from './add-user/add-user.component';
import { EditDeviceComponent } from './edit-device/edit-device.component';
import { Ng2TelInputModule } from 'ng2-tel-input';
import { OwlDateTimeModule, OwlNativeDateTimeModule } from 'ng-pick-datetime';
import { DeviceReportCountComponentComponent } from './device-report-count-component/device-report-count-component.component';
import { ReportCountComponentComponent } from './report-count-component/report-count-component.component';
import { CustomDatePipe } from './pipes/custom-date.pipe';
import { UploadAssetAssociationsComponent } from './upload-asset-associations/upload-asset-associations.component';
import { GatewayDataForwardRuleComponent } from './gateway-list-data-forwarding-rule/gateway-data-forward-rule.component';
import { BatchDeviceEditComponent } from './batch-device-edit/batch-device-edit.component';
import { AgmCoreModule } from '@agm/core';
import {AgmSnazzyInfoWindowModule} from '@agm/snazzy-info-window';
import { MaintenanceSummaryReportComponent } from './maintenance-summary-report/maintenance-summary-report.component'
export const MY_FORMATS = {
  parse: {
      dateInput: 'LL'
  },
  display: {
      dateInput: 'MM/DD/YYYY',
      monthYearLabel: 'YYYY',
      dateA11yLabel: 'LL',
      monthYearA11yLabel: 'YYYY'
  }
};
@NgModule({
  declarations: [
    AppComponent,
    DeviceSummaryReportComponent,
    AssetsComponent,
    AssetsListComponent,
    InstallationSummaryComponent,
    InstallationDetailsComponent,
    TentativeComponent,
    AddAssetsComponent,
    AddGatewayComponent,
    UpdateGatewayComponent,
    GatewayListComponent,
    DeviceProfieComponent,
    DndDirective,
    ResetInstallationStatusComponent,
    ShippedDevicesComponent,
    CustomSuccessToastrComponent,
    CustomErrorToastrComponent,
    CustomBatchErrorToastrComponent,
    UploadPackagesComponent,
    BtnCellRenderer,
    BtnCellRendererForCampaign,
    CampaignHyperlinkRenderer,
    BtnCellRendererForCompany,
    CustomHeader,
    BtnCellRendererForUser,
    BtnCellRendererForCustomAsset,
    CustomDateComponent,
    BtnCellRendererForAssets,
    BtnCellRendererForCheckbox,
    Error404Component,
    CustomTooltip,
    CustomCampaignsHeader,
    BtnCellRendererForGatewayDetails,
    BtnCellRendererForDeviceProvisioningComponent,
    UploadGatewayComponent,
    GatewaySummaryListComponent,
    DeviceReportComponent,
    ATCommandListComponent,
    AddATCommandComponent,
    BtnCellRendererForATCommand,
    AtcommandSummaryComponent,
    DeviceForwardingComponent,
    DeviceForwardingRedisComponent,
    DeviceDataForwardingComponent,
    TlvColumndefsComponent,
    BtnCellRendererForDeviceReport,
    BtnCellRendererForRawReport,
    CampaignTabComponent,
    AllDeviceReportsComponent,
    OrderByPipe,
    BtnCellRendererForRTC,
    DeviceProvisioningComponent,
    GatewayBtnCellRendererComponent,
    AddUserComponent,
    EditDeviceComponent,
    DeviceReportCountComponentComponent,
    ReportCountComponentComponent,
    CustomDatePipe,
    UploadAssetAssociationsComponent,
    GatewayDataForwardRuleComponent,
    BatchDeviceEditComponent,
    MaintenanceSummaryReportComponent
  ],
  entryComponents: [
    CustomSuccessToastrComponent, 
    CustomErrorToastrComponent,
    CustomBatchErrorToastrComponent],
  imports: [
    ToastrModule.forRoot({
      timeOut: 3000,
      progressBar: true,
      positionClass: 'toast-top-right',
      preventDuplicates: true
    }),
    CommonModule,
    FormsModule,
    RouterModule,
    BrowserModule,
    AppRoutingModule,
    ReactiveFormsModule,
    HeaderModule,
    BrowserAnimationsModule,
    LoginModule,
    MatTabsModule,
    HttpClientModule,
    UsersListModule,
    DataTableModule,
    ResetPasswordModule,
    PasswordStrengthModule,
    MatRadioModule,
    NgMultiSelectDropDownModule,
    CustomerAssetModule,
    CompanyModule,
    UserTimeModuleModule,
    MatCheckboxModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatDatepickerModule,
    MatNativeDateModule,
    CampaignModule,
    PackagesListModule,
    AddCampaignModule,
    MatMomentDateModule,
    CampaignsListModule,
    OwlDateTimeModule,
    OwlNativeDateTimeModule,
    Ng2TelInputModule,
    MatAutocompleteModule,
    // NgxMatDatetimePickerModule,
    // NgxMatTimepickerModule,
    // NgxMatNativeDateModule,
    AgmCoreModule.forRoot({
      apiKey: 'AIzaSyDZGmZnYLc5PBLJ_r1KbcaEeGqSUEtI58o'
    }),
    AgmSnazzyInfoWindowModule,
    AgGridModule.withComponents([BtnCellRenderer,
                                BtnCellRendererForCampaign,
                                CampaignHyperlinkRenderer,
                                BtnCellRendererForCompany,
                                BtnCellRendererForUser,
                                BtnCellRendererForCustomAsset,
                                BtnCellRendererForAssets,
                                BtnCellRendererForCheckbox,
                                CustomHeader,
                                CustomDateComponent,
                                CustomTooltip,
                                CustomCampaignsHeader,
                                BtnCellRendererForGatewayDetails,
                                BtnCellRendererForDeviceProvisioningComponent,
                                BtnCellRendererForATCommand,
                                BtnCellRendererForDeviceReport,
                                BtnCellRendererForRawReport,
                                BtnCellRendererForRTC,
                                GatewayBtnCellRendererComponent])

  ],
  providers: [
    { provide: LocationStrategy, useClass: HashLocationStrategy },
    { provide: APP_BASE_HREF, useValue: '/' },
    { provide: HTTP_INTERCEPTORS, useClass: JwtInterceptor, multi: true },
    { provide: HTTP_INTERCEPTORS, useClass: ErrorInterceptor, multi: true },
    {provide: MAT_DATE_LOCALE, useValue: 'en-GB'},
    {
      provide: DateAdapter,
      useClass: MomentDateAdapter,
      deps: [MAT_DATE_LOCALE, MAT_MOMENT_DATE_ADAPTER_OPTIONS],
    },

    {provide: MAT_DATE_FORMATS, useValue: MY_FORMATS},
    HighchartsService,
    MatDatepickerModule,
    MatNativeDateModule,
    DatePipe,
    CustomDatePipe,
    // NgxMatDatetimePickerModule,
    // NgxMatTimepickerModule,
    // NgxMatNativeDateModule
  ],
  bootstrap: [AppComponent]
})
export class AppModule {
  constructor() {
    ModuleRegistry.registerModules(AllModules);
  }
 }
