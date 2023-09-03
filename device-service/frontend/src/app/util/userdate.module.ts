import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { UserDatePipe } from './userdate.pipe';

@NgModule({
  declarations: [UserDatePipe],
  imports: [
  ],
  exports: [UserDatePipe]
})
export class UserTimeModuleModule { }
