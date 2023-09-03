import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AggridTestComponent } from './aggrid-test/aggrid-test.component';
import { AppComponent } from './app.component';
import { AtcommandComponent } from './devices/atcommand/atcommand.component';
import { DeviceForwardingComponent } from './devices/device-forwarding/device-forwarding.component';
import { DeviceProfileComponent } from './devices/device-profile/device-profile.component';
import { DeviceReportComponent } from './devices/device-reports/device-reports.component';
import { DevicesListComponent } from './devices/devices-list/devices-list.component';
import { LoginComponent } from './login/login.component';



const routes: Routes = [{
   path: '', redirectTo: 'login', pathMatch: 'full' },
   {
    path: 'login',
    component: LoginComponent,
  },
  {
      path: 'devices-list',
      component: DevicesListComponent,
    },
    {
      path: 'device-profile',
      component: DeviceProfileComponent,
    },
    {
      path: 'device-reports',
      component: DeviceReportComponent,
    },
    {
      path: 'aggrid-test',
      component: AggridTestComponent,
    },
    {
      path: 'atcommand',
      component: AtcommandComponent,
    },
    {
      path: 'device-forwarding',
      component: DeviceForwardingComponent,
    }

];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {
 
 }
