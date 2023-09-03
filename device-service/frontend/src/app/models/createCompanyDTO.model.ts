
export class CreateCompanyDTO {
    id: number;
    customer: any;
    company_view_list: any = [];
    type: String;
    status: boolean;
    is_asset_list_required: boolean;
    is_approval_req_for_device_update: boolean;
    is_test_req_before_device_update: boolean;
    is_monthly_release_notes_req: boolean;
    is_digital_sign_req_for_firmware: boolean;
    no_of_device: any;


}
export class AddCompanyDTO {
    ext_id: String;
    company_name: String;
    is_asset_list_required: boolean;
    status: boolean;
    is_approval_req_for_device_update: boolean;
    is_test_req_before_device_update: boolean;
    is_monthly_release_notes_req: boolean;
    is_digital_sign_req_for_firmware: boolean;
    is_auto_reset_installation: boolean;
    no_of_device: any;

}