import { MatRadioModule } from '@angular/material/radio';
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { CustomerAssetsListComponent } from './customer-assets-list.component';
import { DataTableModule } from 'angular-6-datatable';
import { UserTimeModuleModule } from '../util/userdate.module';
import { AgGridModule } from '@ag-grid-community/angular';

@NgModule({
    imports: [
        CommonModule,
        FormsModule,
        ReactiveFormsModule,
        RouterModule,
        MatRadioModule,
        DataTableModule,
        UserTimeModuleModule,
        AgGridModule.withComponents([])
    ],
    declarations: [
        CustomerAssetsListComponent,
    ],
    exports: [CustomerAssetsListComponent]
})
export class CustomerAssetModule { }
