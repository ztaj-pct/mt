export class Campaign {
    campaign_name: String;
    // grouping_uuid: String;
    // execution_status: String;
    description: String;
    version_migration_detail:any[];
    imei_list: String;
    is_imei_for_customer: boolean;
    customer_name : String;
    is_pause_execution: boolean;
    no_of_imeis: number;
    uuid: String;
    is_active: Boolean = false;
    exclude_not_installed: Boolean;
    exclude_low_battery: Boolean;
    exclude_rma: Boolean;
    exclude_eol: Boolean;
    exclude_engineering: Boolean;
    campaign_status: String;
    not_eligible_devices: String;
    is_replace: Boolean = true;
    device_type: String;
    campaign_type: String;
    type:String;
    init_status :String;
}