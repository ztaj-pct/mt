export class Gateway {
    epicor_order_number:string;
    salesforce_account_id:string;
    salesforce_account_name:string;
    salesforce_order_number:string;
    asset_details:any=[];
    notify: any;
    imei:string;
}


export class sensor {
    product_code:string;
    product_name:string;
}



export class  datalist {
    product_code : string;
    product_name: string;

}

export class assetdetails {
    product_short_name :string;
    product_code :string;
    quantity_shipped:string;
    imei_list :any = [];
    data_list:any =[];
    sensor_list:any=[];
}

export class UploadGateway {
    company: any;
    is_all_imei: Boolean;
    is_all_mac_address: Boolean;
    eligible_gateway: String;
    salesforce_order_id: String;
    gateway_list : any[];
    usage_status: string;
    product_master_response: any;
    telecom_provider: string;
    product_code: string;
    forwarding_list: any[];
    ignore_forwarding_rules: any[];
    other_relationships: any[];
    purchased_by: any;
}

export class uploadGatewayForChecking {
    device_list : any[];
    is_product_is_approved_for_asset : String;
    device_details: any[];
}