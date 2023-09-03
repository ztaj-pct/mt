import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { DataTableModule } from 'angular-6-datatable';
import { AgGridModule } from '@ag-grid-community/angular';
import { HttpClientModule } from '@angular/common/http';
import { CampaignsListComponent } from './campaigns-list.component';

@NgModule({
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterModule,
    DataTableModule,
    AgGridModule.withComponents([]),
    HttpClientModule,
    FormsModule
  ],
  declarations: [
    CampaignsListComponent,
  ],
  exports: [CampaignsListComponent]
})
export class CampaignsListModule { }
