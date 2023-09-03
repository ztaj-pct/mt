import { Pipe, Injectable } from "@angular/core";
import { DatePipe } from "@angular/common";
import * as moment from 'moment-timezone';


@Pipe({
    name: "userDate"
})
export class UserDatePipe extends DatePipe {

    map = {
        "Pacific": {
            zone: "America/Los_Angeles",
        }, "Mountain": {
            zone: "America/Denver",
        }, "Central": {
            zone: "America/Chicago",
        }, "Eastern": {
            zone: "America/New_York",
        }
    };

    currentTimeZone: string = 'Pacific';
    format: string = 'YYYY-MM-DD HH:mm z';

    defaultTimeZone = this.map['Pacific'];


    transform(value: any, pattern: string = this.format): string | null {
        if (!value) return '';

        // Try and parse the passed value.
        var momentDate = moment(value);

        // If moment didn't understand the value, return it unformatted.
        if (!momentDate.isValid()) return value;

        let currentTimeZone = this.map[sessionStorage.getItem('timeZone')];
        // if(!this.userTimeZone) 

        if (!currentTimeZone) currentTimeZone = this.defaultTimeZone;
        // Otherwise, return the date formatted as requested.
        return momentDate.tz(currentTimeZone.zone).format(this.format);


        if (!value) return '';
        var momentDate = moment(value);
        console.log(momentDate);
        console.log(pattern); //M/d/yy, h:mm a
        let result = momentDate.tz('America/Los_Angeles').format(this.format);
        console.log(result);
        return result;
    }


}