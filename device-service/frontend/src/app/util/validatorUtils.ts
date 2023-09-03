import { FormGroup } from '@angular/forms';

export class ValidatorUtils {
    emailValidator = "^[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,4}$";
    spaceValidator = "^[-a-zA-Z0-9-()]+([-a-zA-Z0-9-() ]+)*";
    passwordValidator = "^[a-zA-Z0-9.!@#$%^&*()?_+=<>,.~{}|\\-\\[\\]\\\\]+$";
    numberValidator = "^[0-9]*$";
    phoneNumberValidator = "^[0-9.+]+$";
    vinValidator = "^[0-9A-Z]*$";

}

export function MustMatch(controlName: string, matchingControlName: string) {
    return (formGroup: FormGroup) => {
        const control = formGroup.controls[controlName];
        const matchingControl = formGroup.controls[matchingControlName];

        if (matchingControl.errors && !matchingControl.errors.mustMatch) {
            // return if another validator has already found an error on the matchingControl
            return;
        }
        // set error on matchingControl if validation fails
        if (control.value !== matchingControl.value) {
            matchingControl.setErrors({ mustMatch: true });
        } else {
            matchingControl.setErrors(null);
        }
    }
}
