import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { UserTimeModuleModule } from '../_services/user-time-module.module';
import { AddCampaignComponent } from './add-campaign.component';
import { AgGridModule } from '@ag-grid-community/angular';
import { MatRadioModule, MatTableModule, MatTabsModule, MatCheckboxModule, MatAutocomplete, MatAutocompleteModule, MatFormFieldModule, MatInputModule, MatSelectModule } from '@angular/material';
import { CampaignHyperlinkRenderer } from './campaign-hyperlink.component';
import { MatDialogModule, MatDialog, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material';
import {  DeviceHistoryDetailsDialogComponent } from './device-history-details-dialog.component';
@NgModule({
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterModule,
    UserTimeModuleModule,
    MatRadioModule,
    MatCheckboxModule,
    AgGridModule.withComponents([CampaignHyperlinkRenderer]),
    MatTabsModule,
    MatDialogModule,
    FormsModule,
    MatAutocompleteModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule
    
  ],
  declarations: [
    AddCampaignComponent,
    DeviceHistoryDetailsDialogComponent    
  ],
  exports: [AddCampaignComponent],
  entryComponents: [DeviceHistoryDetailsDialogComponent],

})
export class AddCampaignModule { }
