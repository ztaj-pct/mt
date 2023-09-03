import { Component, OnInit } from "@angular/core";
import { FormArray, FormBuilder, FormGroup, Validators } from "@angular/forms";
import { Router } from "@angular/router";
import { ToastrService } from "ngx-toastr";
import { forkJoin, Observable } from "rxjs";
import { CompaniesService } from "../companies.service";
import { CommonData } from "../constant/common-data";
import { CustomErrorToastrComponent } from "../custom-error-toastr/custom-error-toastr.component";
import { CustomSuccessToastrComponent } from "../custom-success-toastr/custom-success-toastr.component";
import { GatewayListService } from "../gateway-list.service";
import { UpdateGatewayService } from "../update-gateway/update-gateway.service";

declare var $: any;

@Component({
    selector: 'app-device-data-forwarding',
    templateUrl: './device-data-forwarding.component.html',
    styleUrls: ['./device-data-forwarding.component.css']
})
export class DeviceDataForwardingComponent implements OnInit {

    loading: boolean = false;
    deviceId: any;
    deviceDetailsObject: any;
    deviceDataForwardingRule: any[] = [];
    customerDataForwardingRule: any[] = [];
    ignoreDeviceDataForwardingRule: any[] = [];
    devicesDataForm: FormGroup;
    customerForwardingRuleUrls: any[] = [];
    deleteForwardingRuleIndex: number = -1;
    customers: any[] = [];
    endCustomerOrg: any;
    purchasedByOrg: any;
    isAddAnotherRule: boolean = true;
    url:any;

    constructor(
        private readonly router: Router,
        private readonly formBuldier: FormBuilder,
        private readonly companiesService: CompaniesService,
        private readonly gatewayListService: GatewayListService,
        private readonly gatewaysService: UpdateGatewayService,
        private readonly toastr: ToastrService) {
        this.deviceId = sessionStorage.getItem('device_id');
    }

    ngOnInit() {
        this.init(); 
    }

    init() {
        this.initilizeForm();
        this.initData();
    }

    initilizeForm() {
        this.devicesDataForm = this.formBuldier.group({
            imei_id: [''],
            forwarding_list: this.formBuldier.array([]),
            ignore_forwarding_rules: this.formBuldier.array([])
        });
    }

    initData() {
        this.loading = true;
        this.getData()
            .subscribe(res => {
                this.deviceDetailsObject = res[0].body[0];
                this.customerForwardingRuleUrls = res[1];
                this.customers = res[2].body;
                this.deviceDataForwardingRule = res[3].body;

                let orgUuid = [];

                let temp = this.customers.filter(x => x.accountNumber == this.deviceDetailsObject.can);
                if (temp.length > 0) {
                    orgUuid.push(temp[0].uuid);
                }
                if (this.deviceDetailsObject.purchased_by != null) {
                    temp = this.customers.filter(x => x.accountNumber == this.deviceDetailsObject.purchased_by);
                    if (temp.length > 0) {
                        orgUuid.push(temp[0].uuid);
                    }
                }

                if (orgUuid.length > 0) {
                    this.initCustomerData(orgUuid);
                } else {
                    this.loading = false;
                }

                this.setDeviceForwardingRules();
            }, err => {
                console.log(err);
                this.loading = false;
            });
    }

    setDeviceForwardingRules() {
        for (let i = 0; i < this.deviceDataForwardingRule.length; i++) {
            const element = this.deviceDataForwardingRule[i];
            const temp = this.customerForwardingRuleUrls.filter(x => x.uuid == element.forwarding_rule_url_uuid);
            if (temp.length > 0) {
                element.description = temp[0].description;
            }
            this.addExiztingRule(element);
        }
    }

    getData(): Observable<any> {
        const response1 = this.gatewayListService.getDeviceDetailsBydeviceId(this.deviceId);
        const response2 = this.companiesService.getAllCustomerForwardingRuleUrl();
        const response3 = this.companiesService.getAllActiveByOrganisationRoles(['RESELLER', 'CUSTOMER']);
        const response4 = this.gatewayListService.getDeviceForwardingList(this.deviceId);
        return forkJoin([response1, response2, response3, response4]);
    }

    initCustomerData(uuids: any[]) {
        this.getCustomerData(uuids)
            .subscribe(res => {
                console.log("Result");
                console.log(res[0].url);
                this.customerDataForwardingRule = res[0];
                this.url =  this.customerDataForwardingRule[0].url;
                this.ignoreDeviceDataForwardingRule = res[1].body;
                this.setCustomerForwardingRules();
                this.loading = false;
            }, err => {
                console.log(err);
                this.loading = false;
            });
    }

    setCustomerForwardingRules() {
        for (let index = 0; index < this.customerDataForwardingRule.length; index++) {
            const element = this.customerDataForwardingRule[index];
            element.ignore = this.ignoreDeviceDataForwardingRule.filter(x => x.customer_forwarding_rule_uuid == element.uuid).length > 0 ? true : false;
            this.addCustomerForwardingRule(element);
        }
    }

    getCustomerData(uuids: any[]): Observable<any> {
        const response1 = this.companiesService.getCustomerForwardingRulesByOrganizationUuids(uuids);
        const response2 = this.companiesService.getIgnoreRulesUsingDeviceImei(this.deviceDetailsObject.imei);
        return forkJoin([response1, response2]);
    }

    onSubmit(event) {
        this.loading = true;
        let uploadGateway = {
            forwarding_rules: [],
            ignore_forwarding_rules: [],
            imeis: [this.deviceId],
            end_customer_account_number: this.deviceDetailsObject.can,
            purchase_by_account_number: this.deviceDetailsObject.purchased_by
        };
        uploadGateway.forwarding_rules = this.devicesDataForm.value.forwarding_list;
        let ignore_forwarding_rules: any[] = [];
        for (let index = 0; index < this.devicesDataForm.value.ignore_forwarding_rules.length; index++) {
            const element = this.devicesDataForm.value.ignore_forwarding_rules[index];
            if (element.ignore) {
                ignore_forwarding_rules.push(element);
            }
        }
        uploadGateway.ignore_forwarding_rules = ignore_forwarding_rules;

        this.gatewaysService.bulkUpdateDeviceDataForwoarding(uploadGateway).subscribe(data => {
            if (data.status) {
                this.toastr.success('Device data forwarding is updated successfully', null, { toastComponent: CustomSuccessToastrComponent });
                this.init();
            } else {
                this.toastr.error('Something went wrong while updating the device data forwarding.', 'Problem with updating', { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
                this.loading = false;
            }
        }, error => {
            console.log(error);
            this.loading = false;
        });
    }

    cancel() {
        this.loading = true;
        this.initilizeForm();
        this.setDeviceForwardingRules();
        this.setCustomerForwardingRules();
        this.loading = false;
    }

    get forwarding_list(): FormArray {
        return this.devicesDataForm.get('forwarding_list') as FormArray;
    }

    get g() {
        return this.devicesDataForm.controls;
    }

    get ignore_forwarding_rules(): FormArray {
        return this.devicesDataForm.get('ignore_forwarding_rules') as FormArray;
    }

    selectChangeHandler(event, i) {
        let isValid = true;
        if (this.forwarding_list.value.length > 1) {
            for (let index = 0; index < this.forwarding_list.value.length; index++) {
                if (index !== i) {
                    if (this.forwarding_list.value[index].forwarding_rule_url_uuid === event.value) {
                        this.forwarding_list.controls[i].patchValue({
                            type: '',
                            forwarding_rule_url_uuid: ''
                        });
                        this.toastr.error('Already selected endpoint, Please select another one', 'Error', { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: false });
                        isValid = false;
                        break;
                    }
                }
            }
        }
        if (isValid) {
            this.isAddAnotherRule = true;
            const items = this.customerForwardingRuleUrls.filter(r => r.uuid == event.value);
            if (items.length > 0) {
                this.forwarding_list.value[i].url = items[0].endpointDestination;
                this.forwarding_list.value[i].description = items[0].description;
                this.forwarding_list.value[i].type = CommonData.DATA_FORWORDING_RULE_FORMAT_MAPPING[items[0].format];
                this.forwarding_list.value[i].format = items[0].format;
            }
        } else {
            this.forwarding_list.value[i].url = '';
            this.forwarding_list.value[i].description = '';
            this.forwarding_list.value[i].type = '';
            this.forwarding_list.value[i].format = '';
        }
    }

    confirmDeleteForwardingRule() {
        if (this.deleteForwardingRuleIndex != -1) {
            this.deleteForwardingRule(this.deleteForwardingRuleIndex);
            this.deleteForwardingRuleIndex = -1;
        }
    }

    cancelDeleteForwardingRule() {
        this.deleteForwardingRuleIndex = -1;
    }

    deleteForwardingRule(index: number) {
        const add = this.devicesDataForm.get('forwarding_list') as FormArray;
        add.removeAt(index);
    }

    onDeleteForwardingRule(index: number) {
        if (this.forwarding_list.value[index].forwarding_rule_url_uuid != null &&
            this.forwarding_list.value[index].forwarding_rule_url_uuid != '') {
            this.deleteForwardingRuleIndex = index;
            $('#deleteForwardingRuleModal').modal('show');
        } else {
            this.isAddAnotherRule = true;
            this.deleteForwardingRule(index);
        }
    }

    addAnotherRule() {
        this.isAddAnotherRule = false;
        this.forwarding_list.push(this.formBuldier.group({
            type: [null],
            forwarding_rule_url_uuid: ['', Validators.compose([Validators.required])],
            url: [null],
            uuid: [null],
            id: [0],
            description: [null]
        }));
    }

    addExiztingRule(data: any) {
        this.forwarding_list.push(this.formBuldier.group({
            type: [data.type],
            forwarding_rule_url_uuid: [data.forwarding_rule_url_uuid, Validators.compose([Validators.required])],
            url: [data.url],
            uuid: [data.uuid],
            id: [data.id],
            description: [data.description]
        }));
    }

    addCustomerForwardingRule(forwaringRule: any) {
        this.ignore_forwarding_rules.push(this.formBuldier.group({
            organisationName: [forwaringRule.organisation.organisationName, null],
            organisationId: [forwaringRule.organisation.id, null],
            type: [forwaringRule.type, null],
            url: [forwaringRule.url, null],
            description: [forwaringRule.forwardingRuleUrl.description ? forwaringRule.forwardingRuleUrl.description : null],
            ruleName: [forwaringRule.forwardingRuleUrl.ruleName ? forwaringRule.forwardingRuleUrl.ruleName : null],
            uuid: [forwaringRule.uuid ? forwaringRule.uuid : null],
            id: [forwaringRule.id ? forwaringRule.id : 0],
            ignore: [forwaringRule.ignore]
        }));
    }

    viewOrgDetails(id: any) {
        this.router.navigate(['add-company'], { queryParams: { id: id } });
    }
}