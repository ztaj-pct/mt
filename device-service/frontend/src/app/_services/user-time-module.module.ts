import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { UserDatePipe } from './user-time-zone.pipe';
@NgModule({
  declarations: [UserDatePipe],
  imports: [
  ],
  exports: [UserDatePipe]
})
export class UserTimeModuleModule { }
