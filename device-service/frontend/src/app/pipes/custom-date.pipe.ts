import { Pipe, PipeTransform } from '@angular/core';
@Pipe({ name: 'customDatePipe' })
export class CustomDatePipe implements PipeTransform {

    transform(input: any, key: string) {
        if (!input) { return input; }
        if (key === 'report') {
            const dateArray = input.split('.', 2);
            return dateArray[0];
        } else if (key === 'latest_report') {
            const dateArray = input.split('.', 2);
            const newStr = dateArray[0].replace('T', ' ');
            return newStr.replace('Z', '');
        } else {
            const newStr = input.replace('T', ' ');
            return newStr.replace('Z', '');
        }
    }
}
