import { IDropdownSettings } from 'ng-multiselect-dropdown/multiselect.model';

export class Constants {
    static readonly DATE_FMT = 'dd/MMM/yyyy';
    static readonly DATE_TIME_FMT = `${Constants.DATE_FMT} hh:mm:ss`;
}

export function setDropDownSetting(idFeild: string, textField: string, singleSelection: boolean, allowSearchFilter: boolean): IDropdownSettings {
    let dropdownSettings: IDropdownSettings = {
        singleSelection: singleSelection,
        idField: idFeild,
        textField: textField,
        selectAllText: 'Select All',
        unSelectAllText: 'UnSelect All',
        itemsShowLimit: 3,
        allowSearchFilter: allowSearchFilter
    }
    return dropdownSettings
}