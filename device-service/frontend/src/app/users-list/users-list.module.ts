import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { UsersListComponent } from './users-list.component';
import { DataTableModule } from 'angular-6-datatable';
import { AgGridModule } from '@ag-grid-community/angular';
import { FormsModule } from '@angular/forms';

@NgModule({
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterModule,
    DataTableModule,
    FormsModule,
    AgGridModule.withComponents([])
  ],
  declarations: [
    UsersListComponent,
  ],
  exports: [UsersListComponent]
})
export class UsersListModule { }
