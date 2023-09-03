import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { PasswordStrengthBarComponent } from './password-strength-bar.component'

@NgModule({
  imports: [
    CommonModule,
    RouterModule,

  ],
  declarations: [PasswordStrengthBarComponent],
  exports: [PasswordStrengthBarComponent]
})
export class PasswordStrengthModule { }
