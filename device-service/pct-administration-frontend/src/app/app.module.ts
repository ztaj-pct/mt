import { NgModule } from '@angular/core';
import { MatTabsModule } from '@angular/material/tabs';
import { MatSelectModule } from '@angular/material/select';
import { BrowserModule} from '@angular/platform-browser';
import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { DevicesListComponent } from './devices/devices-list/devices-list.component';
import { HeaderComponent } from './layout/header/header.component';
import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';
import { JwtInterceptor } from './_helpers/jwt.interceptor';
import { DeviceProfileComponent } from './devices/device-profile/device-profile.component';
import { CustomHeader } from './ui-elements/custom-header.component';
import { CustomErrorToastrComponent } from './custom-error-toastr/custom-error-toastr.component';
import { BtnCellRendererForGatewayDetails } from './devices/devices-list/btn-cell-renderer.component';
import { AgGridModule } from '@ag-grid-community/angular';
import { CustomDateComponent } from './ui-elements/custom-date-component.component';
import { AllModules, ModuleRegistry } from '@ag-grid-enterprise/all-modules';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { ToastrModule } from 'ngx-toastr';
import { CustomSuccessToastrComponent } from './custom-success-toastr/custom-success-toastr.component';
import { DeviceReportComponent } from './devices/device-reports/device-reports.component';
import { AtcommandComponent } from './devices/atcommand/atcommand.component';
import { AggridTestComponent } from './aggrid-test/aggrid-test.component';
import { BtnCellRendererForATCommand } from './devices/atcommand/btn-cell-renderer.component';
import { LoginComponent } from './login/login.component';
import { UserComponent } from './user/user.component';
import { DeviceForwardingComponent } from './devices/device-forwarding/device-forwarding.component';


@NgModule({
  declarations: [
    AppComponent,
    DevicesListComponent,
    HeaderComponent,
    DeviceProfileComponent,
    CustomHeader,   
    CustomSuccessToastrComponent,
    CustomErrorToastrComponent,
    BtnCellRendererForGatewayDetails,
    DeviceReportComponent,
    AtcommandComponent,
    AggridTestComponent,
    LoginComponent,
    UserComponent,
    DeviceForwardingComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    MatTabsModule,
    MatSelectModule,
    BrowserAnimationsModule,
    HttpClientModule,
    FormsModule,
    ReactiveFormsModule,
    FormsModule,
    ToastrModule.forRoot(),
    AgGridModule.withComponents([
      CustomHeader,
      CustomDateComponent,
      BtnCellRendererForGatewayDetails,
      BtnCellRendererForATCommand])
  ],
  providers: [    
    { provide: HTTP_INTERCEPTORS, useClass: JwtInterceptor, multi: true }],
  bootstrap: [AppComponent]
})
export class AppModule {
  constructor() {
    ModuleRegistry.registerModules(AllModules);
  }
 }
