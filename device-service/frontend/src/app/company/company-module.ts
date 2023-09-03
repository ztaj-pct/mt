import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import {MatRadioModule} from '@angular/material/radio';
import { NgMultiSelectDropDownModule } from 'ng-multiselect-dropdown';
import { CompanyNewComponent } from './company.newcomponent';
import { CompanyListComponent } from '../company-list/company-list.component';
import { DataTableModule } from 'angular-6-datatable';
import { AgGridModule } from '@ag-grid-community/angular';
import { MatCheckboxModule, MatFormFieldModule, MatInputModule, MatSelectModule, MatTabsModule } from '@angular/material';


@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    RouterModule,
    MatRadioModule,
    DataTableModule,
    AgGridModule.withComponents([]),
    NgMultiSelectDropDownModule,
    DataTableModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatCheckboxModule,
    MatTabsModule
  ],
  declarations: [
    CompanyNewComponent,
    CompanyListComponent
  ],
  exports: [CompanyNewComponent,CompanyListComponent]
})
export class CompanyModule { }
