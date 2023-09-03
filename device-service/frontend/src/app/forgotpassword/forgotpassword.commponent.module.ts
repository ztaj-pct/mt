import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { ForGotPasswordComponent } from './forgotpassword.component';
import { ReactiveFormsModule } from '@angular/forms';


@NgModule({
    imports: [
        CommonModule,
        RouterModule,
        ReactiveFormsModule
    ],
    declarations: [
        ForGotPasswordComponent,
    ],
    exports: [ForGotPasswordComponent]
})
export class ForGotPasswordModule { }
