import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { CampaignComponent} from './campaign.component';
import { UserTimeModuleModule } from '../_services/user-time-module.module';
@NgModule({
  imports: [
   CommonModule,
   ReactiveFormsModule,
   RouterModule,
   UserTimeModuleModule
  ],
  declarations: [
    CampaignComponent,
  ],
  exports:[CampaignComponent]
})
export class CampaignModule { }
