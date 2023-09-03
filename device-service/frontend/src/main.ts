import { LicenseManager } from '@ag-grid-enterprise/all-modules';
import { enableProdMode } from '@angular/core';
import { platformBrowserDynamic } from '@angular/platform-browser-dynamic';

import { AppModule } from './app/app.module';
import { environment } from './environments/environment';

LicenseManager.setLicenseKey('CompanyName=Phillips Connect Technologies Inc,LicensedApplication=Maintenance Server,LicenseType=SingleApplication,LicensedConcurrentDeveloperCount=3,LicensedProductionInstancesCount=1,AssetReference=AG-035456,SupportServicesEnd=5_December_2023_[v2]_MTcwMTczNDQwMDAwMA==d374a892838cb35053934f951e64befb');
if (environment.production) {
  enableProdMode();
}

platformBrowserDynamic().bootstrapModule(AppModule)
  .catch(err => console.error(err));
