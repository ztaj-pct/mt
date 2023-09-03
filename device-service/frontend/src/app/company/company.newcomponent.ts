import { Component, OnInit, ViewChild } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CompaniesService } from '../companies.service';
import { FormGroup, FormBuilder, Validators, FormArray, FormControl } from '@angular/forms';
import { ToastrService } from 'ngx-toastr';
import { CreateCompanyDTO, AddCompanyDTO } from '../models/createCompanyDTO.model';
import { IDropdownSettings } from 'ng-multiselect-dropdown/multiselect.model';
import { ValidatorUtils } from '../util/validatorUtils';
import { CustomSuccessToastrComponent } from '../custom-success-toastr/custom-success-toastr.component';
import { CustomErrorToastrComponent } from '../custom-error-toastr/custom-error-toastr.component';
import { MatOption, MatSelect } from '@angular/material';
import { Reseller } from '../models/reseller.model';
import { sensor } from '../models/gateway';
import { CommonData } from '../constant/common-data';
import { Location } from '@angular/common';
import { MatTableDataSource } from '@angular/material/table';
import { CustomBatchErrorToastrComponent } from '../custom-batch-error-toastr/custom-batch-error-toastr.component';
declare var $: any;
@Component({
    selector: 'app-company',
    templateUrl: './company.component.html',
    styleUrls: ['./company.component.css']
})

export class CompanyNewComponent implements OnInit {
    @ViewChild('select') select: MatSelect;
    displayedColumns: string[] = ['position', 'name', 'weight', 'symbol'];
    dataSource = new MatTableDataSource<PeriodicElement>(ELEMENT_DATA);
    update_oraganisation_form: FormGroup;
    resellerList = [];
    reseller: Reseller;
    allSelected = false;
    companyForm: FormGroup;
    locationUpdateForm: FormGroup;
    heading = ''
    message = '';
    isError = false;

    Types = ['Installer', 'Fleet'];
    //resellerList = ['Reseller-1', 'Reseller-2']
    installerList = [];
    maintanenceList = [];
    AssetListRequired: any[] = [{ value: true, name: 'Yes' }, { value: false, name: 'No' }];
    isAssetListRequired;
    isApprovedAssetList = false;
    companiesList: any = [];
    submitted = false;
    loading = false;
    sensor: sensor;
    newCompany: CreateCompanyDTO;
    company: AddCompanyDTO;
    companyType = true;
    companydropdown = false;
    validatorUtil: ValidatorUtils = new ValidatorUtils();
    errorMessage;
    organisationId: any;
    organisationData: any;
    disabled = false;
    rolePCTUser: any;
    deviceForwardingRuleList: any[] = [];
    isEnd = false;
    locationHideShow = false;
    public customerForwordingRuleData: any[] = CommonData.DEVICE_FORWORDING_RULE_DATA;
    customerForwardingGroups: any[] = [];
    customerForwardingRules: any[] = [];
    customerForwardingGroupUuid: any = '';
    customerForwardingRuleUrls: any[] = [];
    customerForwardingGroupDesc: any = '';
    locationsList: any = [];
    deleteId: any;
    locationObj: any;
    locationButton = true;
    locationButton2 = false;
    redioButtonDisable = false;

    dropdownSettings: IDropdownSettings = {
        singleSelection: false,
        idField: 'id',
        textField: 'company_name',
        selectAllText: 'Select All',
        unSelectAllText: 'Deselect All',
        itemsShowLimit: 3,
        allowSearchFilter: false
    };
    constructor(
        public companyService: CompaniesService,
        public formBuldier: FormBuilder,
        private toastr: ToastrService,
        private _formBuldier: FormBuilder,
        private router: Router,
        private location: Location,
        private readonly activateRoute: ActivatedRoute) {
        this.rolePCTUser = JSON.parse(sessionStorage.getItem('IS_ROLE_PCT_USER'));
        if (this.rolePCTUser) {
            this.location.back();
        }
        this.activateRoute.queryParams.subscribe(params => {
            this.organisationId = params.id;
        });

    }


    ngOnInit() {
        this.rolePCTUser = JSON.parse(sessionStorage.getItem('IS_ROLE_PCT_USER'));
        if (this.rolePCTUser) {
            this.location.back();
            return;
        }
        this.getFormControl();
        if (this.organisationId) {
            this.getOrganisationById();
        }
        this.getAllCustomerForwardingGroup();
        this.getAllCustomerForwardingRuleUrl();
        this.getAllActiveResellerOrg();
        this.getAllActiveInstallerOrg();
        this.getAllActiveMaintenanceOrg();
        this.getLocationFormGroup();
    }

    getOrganisationById() {
        this.loading = true;
        this.companyService.findCompanyById(this.organisationId).subscribe(data => {
            this.loading = false;
            if (data && data.type && data.type === 'CUSTOMER') {
                this.companyType = false;
                this.companydropdown = true;
                data.type = 'Customer';
            } else if (data && data.type && data.type === 'INSTALLER') {
                this.companyType = true;
                data.type = 'Installer';
            } else if (data && data.type && data.type === 'RESELLER') {
                this.companyType = true;
                data.type = 'Reseller';
            } else if (data && data.type && data.type === 'MANUFACTURER') {
                this.companyType = true;
                data.type = 'Manufacturer';
            } else if (data && data.type && data.type === 'PCT') {
                this.companyType = true;
                data.type = 'Pct';
            }

            if (data.organisationRole) {
                const organisationRole = data.organisationRole;
                data.endCustomer = organisationRole.filter(r => (r === 'END_CUSTOMER')).length == 1 ? true : false;
                data.reseller = organisationRole.filter(r => (r === 'RESELLER')).length == 1 ? true : false;
                data.installer = organisationRole.filter(r => (r === 'INSTALLER')).length == 1 ? true : false;
                data.maintenance_mode = organisationRole.filter(r => (r === 'MAINTENANCE_MODE')).length == 1 ? true : false;
            }

            this.getCustomerForwardingGroupByOrganizationUuid(data.uuid);
            this.getCustomerForwardingRulesByOrganizationUuid(data.uuid);
            this.getIgnoreRulesImeiByAccountNumber(data.accountNumber);
            // console.log("id is", data.id)
            this.getLocationsByOrgId(data.id);

            this.organisationData = data;
            this.getFormControl(data);
        }, error => {
            console.log(error);
            this.loading = false;
        });
    }


    getFormControl(data?: any) {
        this.disabled = data !== undefined && data != null ? true : false;
        this.companyForm = this.formBuldier.group({
            id: [data && data.id ? data.id : null],
            organisation_name: [data && data.organisationName ? data.organisationName : ''],
            salesforce_account_number: [data && data.accountNumber ? data.accountNumber : ''],
            epicor_account_number: [data && data.epicorAccountNumber ? data.epicorAccountNumber : ''],
            status: [(!this.organisationId) || (data && data.isActive) ? 1 : 0],
            type: [data && data.type ? data.type : ''],
            access_id: [data && data.accessId ? data.accessId : []],
            is_asset_list_required: [data && data.isAssetListRequired ? data.isAssetListRequired : false],
            short_name: [data && data.shortName ? data.shortName : ''],
            // location_name: [''],
            // location_street_address: [''],
            // location_city: [''],
            // location_state: [''],
            // location_ziip_code: [''],
            //account_number: [data && data.accountNumber ? data.accountNumber : null],
            //record_id: [data && data.previousRecordId ? data.previousRecordId : ''],
            //epicor_id: [data && data.epicorId ? data.epicorId : ''],
            is_approval_req_for_device_update: [data && data.isApprovalReqForDeviceUpdate ? data.isApprovalReqForDeviceUpdate : false],
            is_test_req_before_device_update: [data && data.isTestReqBeforeDeviceUpdate ? data.isTestReqBeforeDeviceUpdate : false],
            is_monthly_release_notes_req: [data && data.isMonthlyReleaseNotesReq ? data.isMonthlyReleaseNotesReq : false],
            is_digital_sign_req_for_firmware: [data && data.isDigitalSignReqForFirmware ? data.isDigitalSignReqForFirmware : false],
            //is_auto_reset_installation: [data && data.isAutoResetInstallation ? data.isAutoResetInstallation : false],
            no_of_device: [data && data.noOfDevice ? data.noOfDevice : '', Validators.min(0)],
            end_customer: [data && data.endCustomer ? data.endCustomer : ''],
            reseller: [data && data.reseller ? data.reseller : ''],
            installer: [data && data.installer ? data.installer : ''],
            maintenance_mode: [data && data.maintenance_mode ? data.maintenance_mode : ''],
            authorized_installers: [data && data.authorizedInstallers ? data.authorizedInstallers : ''],
            buys_from: this._formBuldier.array([]),
            AI: this._formBuldier.array([]),
            MAINTENANCE: this._formBuldier.array([]),
            forwarding_list: this._formBuldier.array([]),
            customer_forwarding_group_uuid: [''],
            forwarding_rule_url_uuid: [''],
            ORG_LOCATION: this._formBuldier.array([]),
        });

        this.getCustomerCompaniesList();
        this.addExistingReseller(data && data.resellerList ? data.resellerList : []);
        this.addExistingInstaller(data && data.installerList ? data.installerList : []);
        this.addExistingMaintenance(data && data.maintenanceList ? data.maintenanceList : []);
    }

    getLocationFormGroup(data?: any) {
        this.locationUpdateForm = this.formBuldier.group({
            id: [data && data.id ? data.id : null],
            location_name: [data && data.location_name ? data.location_name : ''],
            street_address: [data && data.street_address ? data.street_address : ''],
            state: [data && data.state ? data.state : ''],
            city: [data && data.city ? data.city : ''],
            zip_code: [data && data.zip_code ? data.zip_code : ''],
        });
    }
    getCustomerCompaniesList() {
        this.companyService.getAllCompany(['EndCustomer'], true).subscribe(
            data => {
                this.companiesList = [];
                this.companiesList = data.body;
                console.log(this.companiesList);
                setTimeout(() => {
                    if (this.organisationData && this.organisationData.accessId && this.organisationData.accessId.length > 0) {
                        this.defaultSelet();
                    }
                }, 100);
                this.loading = false;
            },
            error => {
                this.toastr.error(error, null, { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
                this.loading = false;
            });
    }

    selectChangeHandler(event: any, i: number) {
        if (this.forwarding_list.value.length > 1) {
            for (let index = 0; index < this.forwarding_list.value.length; index++) {
                if (index !== i) {
                    if (this.forwarding_list.value[index].type === event.value) {
                        this.forwarding_list.controls[i].patchValue({
                            type: '',
                        });
                        this.toastr.error('Already selected type, Please select another one', 'Error', { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: false });
                        return;
                    }
                }
            }
        }

    }

    get forwarding_list(): FormArray {
        return this.companyForm.get('forwarding_list') as FormArray;
    }

    addAnotherRule(element: any) {

        if (this.forwarding_list.controls.length <= 4) {
            if (element != null) {
                this.forwarding_list.push(this.existingRuleFormGroup(element));
            } else {
                this.forwarding_list.push(this.newRuleFormGroup());
            }
        }

    }

    existingRuleFormGroup(element: any): FormGroup {
        return this.formBuldier.group({
            type: [element.type],
            //, Validators.compose([Validators.required])
            forwarding_rule_url_uuid: [element.forwardingRuleUrlUuid, Validators.compose([Validators.required])],
            url: [element.url, null],
            uuid: [element.uuid, null],
            id: [element.id],
            ruleName: [element.forwardingRuleUrl.ruleName],
            description: [element.forwardingRuleUrl.description],
            format: [element.forwardingRuleUrl.format]
        });
    }

    newRuleFormGroup(): FormGroup {
        return this.formBuldier.group({
            type: [''],
            // Validators.compose([Validators.required])
            forwarding_rule_url_uuid: ['', Validators.compose([Validators.required])],
            url: [null],
            uuid: [null],
            id: [0],
            ruleName: [null],
            description: [null],
            format: [null]
        });
    }

    deleteForwardingRule(index: number) {
        const add = this.companyForm.get('forwarding_list') as FormArray;
        add.removeAt(index)
    }

    get f() { return this.companyForm.controls; }

    get locationForm() { return this.locationUpdateForm.controls; }

    cancel() {
        this.router.navigate(['company']);
    }

    toggleAllSelection() {
        if (this.allSelected) {
            this.select.options.forEach((item: MatOption) => item.select());
        } else {
            this.select.options.forEach((item: MatOption) => item.deselect());
        }
    }

    defaultSelet() {
        let newStatus = true;
        this.organisationData.accessId.forEach(element => {
            this.select.options.forEach((item: MatOption) => {
                if (item.value === element) {
                    item.select();
                }
                if (!item.selected) {
                    newStatus = false;
                }
            });
        });
        this.allSelected = newStatus;
    }
    optionClick() {
        let newStatus = true;
        this.select.options.forEach((item: MatOption) => {
            console.log(item);
            if (!item.selected) {
                newStatus = false;
            }
        });
        this.allSelected = newStatus;
    }
    attributeDisplay(attribute1, attribute2) {
        if (attribute1 != null && attribute2 != null && attribute1 != undefined && attribute2 != undefined && attribute1.id === attribute2.id) {
            return attribute1.company_name;
        } else {
            return '';
        }

    }
    getResellerCheck() {
        // this.loading = true;
        // this.companyService.getAllCompanies().subscribe(
        //     data => {
        //         this.loading = false;
        //         this.companiesList = data.body;
        //     },
        //     error => {
        //         console.log('something went wrong');
        //         console.log(error);
        //         this.loading = false;
        //     });
    }

    showErrorMsg() {
        let isError = false;
        let msgBody = '';
        let errorCount = 0;
        if (this.companyForm.get('organisation_name').invalid) {
            errorCount++;
            isError = true;
            msgBody = msgBody + '<b> Missing Organization Name:</b> The Organization Name is missing. Please add it and try again.';
        }

        if (this.companyForm.get('salesforce_account_number').invalid) {
            msgBody = msgBody + (isError ? '<br>' : '') + '<b> Missing Salesforce Account Number:</b> The Salesforce Account number is missing. Please add it and try again.';
            isError = true;
            errorCount++;
        }

        if (this.companyForm.get('epicor_account_number').invalid) {
            msgBody = msgBody + (isError ? '<br>' : '') + '<b> Missing Epicor Account Number:</b>  The Epicor Account Number is missing. Please add it and try again.';
            isError = true;
            errorCount++;
        }

        const reData = this.companyForm.value
        if (!(reData.end_customer || reData.installer || reData.reseller || reData.maintenance_mode)) {
            msgBody = msgBody + (isError ? '<br>' : '') + '<b> Missing Organization Role:</b>  The Organization Role is missing. Atleast one organization role selection is required. Please select it and try again.';
            isError = true;
            errorCount++;
        }

        let forwardingError = false;
        this.forwarding_list.controls.forEach(c => {
            if (c.invalid) {
                forwardingError = true;
            }
        });

        if (forwardingError) {
            msgBody = msgBody + (isError ? '<br>' : '') + '<b> Missing Forwording Rule:</b>  The Forwording Rule is missing. Either select rule or remove it.';
            isError = true;
            errorCount++;
        }

        if (this.companyForm.get('no_of_device').invalid) {
            msgBody = msgBody + (isError ? '<br>' : '') + '<b> Invalid No Of Device:</b>  The No Of Device is allow only positive value. Please change it and try again.';
            isError = true;
            errorCount++;
        }

        if (isError) {
            this.toastr.error(msgBody, errorCount + ' Errors', { toastComponent: CustomBatchErrorToastrComponent, tapToDismiss: true, disableTimeOut: true, enableHtml: true });
        }

        return isError;
    }

    onSubmit(companyForm) {
        this.loading = true;
        this.submitted = true;
        // console.log("companyForm");
        // console.log(companyForm);       
        // console.log(companyForm.value);
        //if (this.companyForm.invalid) {
        //this.toastr.error('Please fill the required fields', null, { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
        //  this.loading = false;
        //return;
        //}

        this.createOrganisation(companyForm);

    }

    createOrganisation(companyForm, locationList?: any) {

        if (!this.companyForm.valid) {
            this.validateAllFormFields(this.companyForm);
            this.showErrorMsg();
            this.loading = false;
            return;
        } else {
            if (this.showErrorMsg()) {
                this.loading = false;
                return;
            }
        }

        var organisationRoleList = [];


        const reData = this.companyForm.value;
        if (reData.end_customer) {
            organisationRoleList.push('END_CUSTOMER');
        }
        if (reData.installer) {
            organisationRoleList.push('INSTALLER');
        }
        if (reData.reseller) {
            organisationRoleList.push('RESELLER');
        }
        if (reData.maintenance_mode) {
            organisationRoleList.push('MAINTENANCE_MODE');
        }

        if (organisationRoleList.length < 1) {
            this.loading = false;
            this.toastr.error('Atleast one organization role selection is required', '', { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
            return;
        }

        let customerForwardingGroupsList = [];
        if (this.customerForwardingGroupUuid != null && this.customerForwardingGroupUuid != '') {
            customerForwardingGroupsList.push({
                customer_forwarding_group_uuid: this.customerForwardingGroupUuid
            });
        }

        if (companyForm.value.id != null) {
            const local = {
                id: reData.id,
                organisation_name: reData.organisation_name,
                salesforce_account_number: reData.salesforce_account_number,
                epicor_account_number: reData.epicor_account_number,
                reseller_list: reData.buys_from,
                installer_list: reData.AI,
                maintenance_list: reData.MAINTENANCE,
                // org_location_list: reData.ORG_LOCATION,
                location_name: reData.location_name,
                location_street_address: reData.location_street_address,
                location_city: reData.location_city,
                location_state: reData.location_state,
                location_ziip_code: reData.location_ziip_code,
                organisation_role: organisationRoleList,
                status: reData.status,
                access_id: reData.access_id,
                short_name: reData.short_name,
                is_approval_req_for_device_update: reData.is_approval_req_for_device_update,
                is_asset_list_required: reData.is_asset_list_required,
                is_digital_sign_req_for_firmware: reData.is_digital_sign_req_for_firmware,
                is_monthly_release_notes_req: reData.is_monthly_release_notes_req,
                is_test_req_before_device_update: reData.is_test_req_before_device_update,
                no_of_device: reData.no_of_device,
                customer_forwarding_groups: customerForwardingGroupsList,
                customer_forwarding_rules: reData.forwarding_list
                //type: reData.type,
                //type:reData.type

            };
            this.companyService.createOrganisation(local).subscribe(
                data => {

                    this.toastr.success('Organization successfully updated', null, { toastComponent: CustomSuccessToastrComponent });
                    this.router.navigate(['company']);
                    this.loading = false;
                    this.submitted = false;
                    this.companyForm.reset();
                }, error => {
                    console.log(error);
                    this.loading = false;
                });
        }

        if (companyForm.value.id == null) {
            const local = {

                organisation_name: reData.organisation_name,
                salesforce_account_number: reData.salesforce_account_number,
                epicor_account_number: reData.epicor_account_number,
                reseller_list: reData.buys_from,
                installer_list: reData.AI,
                maintenance_list: reData.MAINTENANCE,
                // org_location_list: reData.ORG_LOCATION,
                location_name: reData.location_name,
                location_street_address: reData.location_street_address,
                location_city: reData.location_city,
                location_state: reData.location_state,
                location_ziip_code: reData.location_ziip_code,
                organisation_role: organisationRoleList,
                status: reData.status,
                access_id: reData.access_id,
                short_name: reData.short_name,
                is_approval_req_for_device_update: reData.is_approval_req_for_device_update,
                is_asset_list_required: reData.is_asset_list_required,
                is_digital_sign_req_for_firmware: reData.is_digital_sign_req_for_firmware,
                is_monthly_release_notes_req: reData.is_monthly_release_notes_req,
                is_test_req_before_device_update: reData.is_test_req_before_device_update,
                no_of_device: reData.no_of_device,
                customer_forwarding_groups: customerForwardingGroupsList,
                customer_forwarding_rules: reData.forwarding_list
                //type: reData.type,
                //type:reData.type

            };

            this.companyService.createOrganisation(local).subscribe(
                data => {
                    this.toastr.success('Organisation successfully added', null, { toastComponent: CustomSuccessToastrComponent });
                    if (locationList && locationList.length > 0) {
                        const requestBody = {
                            locationList: locationList,
                            organisationId: data.body
                        };
                        this.companyService.createOrganisationLocation(requestBody)
                            .subscribe(
                                res => {
                                    // this.toastr.success('Successfully Added Organization\'s Locations', null, { toastComponent: CustomSuccessToastrComponent, tapToDismiss: true, disableTimeOut: true });
                                    // this.companyForm.setControl('ORG_LOCATION', this._formBuldier.array([]));
                                    // this.getLocationsByOrgId(this.organisationId);
                                    this.router.navigate(['company']);
                                    this.loading = false;
                                }, error => {
                                    this.loading = false;
                                    console.error(error);
                                }
                            );
                    } else {
                        this.loading = false;
                        this.submitted = false;
                        this.companyForm.reset();
                        this.router.navigate(['company']);
                    }
                }, error => {
                    console.log(error);
                    this.loading = false;
                });
        }
    }

    validateAllFormFields(formGroup: FormGroup) {
        Object.keys(formGroup.controls).forEach(field => {
            const control = formGroup.get(field);
            if (control instanceof FormControl) {
                control.markAsTouched({ onlySelf: true });
            } else if (control instanceof FormGroup) {
                this.validateAllFormFields(control);
            }
        });
    }






    setFromControlValue(formControlName, value) {
        this.companyForm.controls[formControlName].setValue(value);
    }
    getFromControlValue(formControlName) {
        return this.companyForm.controls[formControlName].value;
    }

    addNewCompany() {
        const value = this.companyForm.value;
        // if (value && value.status && value.status === 1) {
        //     value.is_asset_list_required = true;
        // } else if (value && value.status && value.status === 0) {
        //     value.is_asset_list_required = false;
        // }
        this.companyService.addCompanyFleet(value).subscribe(data => {
            this.toastr.success('Company Successfully Added', null, { toastComponent: CustomSuccessToastrComponent });
            this.router.navigate(['company']);
        }, error => {
            // this.cancel();
            console.log(error);
        });
    }

    onTypeSelect(event: any) {
        this.setFromControlValue('organisation_name', null);
        this.errorMessage = this.companyForm.controls.type.value;
        if (this.companyForm.controls.type.value === 'Customer') {
            this.companyType = false;
            this.companydropdown = true;
            this.isApprovedAssetList = true;
            this.getResellerCheck();
            this.companyForm.get('access_id').setValidators([]);
            this.companyForm.get('organisation_name').setValidators([]);
            this.companyForm.updateValueAndValidity();
            return;
        }
        this.companyForm.get('organisation_name').setValidators(Validators.required);
        this.companyForm.get('access_id').setValidators(Validators.required);
        this.companyForm.updateValueAndValidity();
        this.getCustomerCompaniesList();
        this.isApprovedAssetList = false;
        this.companydropdown = false;
        this.companyType = true;
    }

    confirmDeactivation() {
    }

    addExistingReseller(resellers) {
        if (resellers != null && resellers != undefined && resellers.length > 0) {
            const add = this.companyForm.get('buys_from') as FormArray;
            for (var i = 0; i < resellers.length; i++) {
                add.push(this._formBuldier.group({
                    company_id: [resellers[i].company_id]
                }))
            }
        }
    }

    addNewReseller(repeatationValue) {
        const add = this.companyForm.get('buys_from') as FormArray;
        for (var i = 0; i < repeatationValue; i++) {
            add.push(this._formBuldier.group({
                company_id: [""]
            }))
        }
    }

    deleteReseller(index: number) {
        const add = this.companyForm.get('buys_from') as FormArray;
        // if (add.controls.length >= 2) {
        add.removeAt(index)
        // }
    }

    addExistingInstaller(installers) {
        if (installers != null && installers != undefined && installers.length > 0) {
            const add = this.companyForm.get('AI') as FormArray;
            for (var i = 0; i < installers.length; i++) {
                add.push(this._formBuldier.group({
                    company_id: [installers[i].company_id]
                }))
            }
        }
    }

    addNewInstaller(repeatationValue) {
        const add = this.companyForm.get('AI') as FormArray;
        for (var i = 0; i < repeatationValue; i++) {
            add.push(this._formBuldier.group({
                company_id: [""]
            }))
        }
    }

    addExistingMaintenance(maintanence) {
        if (maintanence != null && maintanence != undefined && maintanence.length > 0) {
            const add = this.companyForm.get('MAINTENANCE') as FormArray;
            for (var i = 0; i < maintanence.length; i++) {
                add.push(this._formBuldier.group({
                    company_id: [maintanence[i].company_id]
                }))
            }
        }
    }

    addNewMaintenance(repeatationValue) {
        const add = this.companyForm.get('MAINTENANCE') as FormArray;
        for (var i = 0; i < repeatationValue; i++) {
            add.push(this._formBuldier.group({
                company_id: [""]
            }))
        }
    }

    deleteMaintenance(index: number) {
        const add = this.companyForm.get('MAINTENANCE') as FormArray;
        // if (add.controls.length >= 2) {
        add.removeAt(index)
        // }
    }

    deleteInstaller(index: number) {
        const add = this.companyForm.get('AI') as FormArray;
        // if (add.controls.length >= 2) {
        add.removeAt(index)
        // }
    }

    resetResellerValue(index) {
        const add = this.companyForm.get('buys_from') as FormArray;
        add.removeAt(index)
        add.at(index).patchValue({ "company_id ": "" });
    }

    handleChangeloc(event) {
        this.locationHideShow = event;
    }

    addNewLocation(repeatationValue) {
        const add = this.companyForm.get('ORG_LOCATION') as FormArray;
        // for (var i = 0; i < repeatationValue; i++) {
        add.push(this._formBuldier.group({
            location_name: new FormControl('', Validators.required),
            street_address: new FormControl(''),
            city: new FormControl(''),
            state: new FormControl(''),
            zip_code: new FormControl(''),
        }));

        // }
    }

    saveNewLocation() {
      
        const locations = (this.companyForm.get('ORG_LOCATION') as FormArray).controls;
        const locationList = [];

        for (let locationGroup of locations) {
            let loc = locationGroup as FormGroup;
            if (!loc.valid) {
                this.validateAllFormFields(loc);
                return;
            }
            locationList.push(locationGroup.value);
        }
        if (!this.organisationId) {
            this.createOrganisation(this.companyForm, locationList);
        } else {

            const requestBody = {
                locationList: locationList,
                organisationId: this.organisationId
            };

            this.loading = true;

            this.companyService.createOrganisationLocation(requestBody)
                .subscribe(
                    res => {
                        this.loading = false;
                        this.toastr.success('Successfully Added Organization\'s Locations', null, { toastComponent: CustomSuccessToastrComponent, tapToDismiss: true, disableTimeOut: true });
                        this.companyForm.setControl('ORG_LOCATION', this._formBuldier.array([]));
                        this.getLocationsByOrgId(this.organisationId);
                        this.loading = false;
                    }, error => {
                        this.loading = false;
                        console.error(error);
                    }
                );
        }
    }


    deleteLocation(index: number) {
        const add = this.companyForm.get('ORG_LOCATION') as FormArray;
        add.removeAt(index);
    }



    onChangeForReseller(event, index) {
        // console.log(event);
        // console.log(index);
        if (event != undefined && event != null && event.value != undefined && event.value != null && event.value != "" && this.companyForm.controls != undefined && this.companyForm.controls != null && this.companyForm.controls.buys_from != undefined && this.companyForm.controls.buys_from != null) {
            if (this.companyForm.controls.buys_from.value != undefined && this.companyForm.controls.buys_from.value != null && this.companyForm.controls.buys_from.value.length > 0) {
                for (var i = 0; i < this.companyForm.controls.buys_from.value.length; i++) {
                    if (i != index && this.companyForm.controls.buys_from.value[i].company_id == event.value) {
                        this.toastr.error("One of the Reseller has been entered twice. Please check and try again.", "Duplicate Reseller Selection", { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
                        this.resetResellerValue(index);
                        return;
                    }
                }
            }
        }
    }

    resetInstallerValue(index) {
        const add = this.companyForm.get('AI') as FormArray;
        add.removeAt(index)
        add.at(index).patchValue({ "company_id": "" });
    }


    onChangeForInstaller(event, index) {
        if (event != undefined && event != null && event.value != undefined && event.value != null && event.value != "" && this.companyForm.controls != undefined && this.companyForm.controls != null && this.companyForm.controls.AI != undefined && this.companyForm.controls.AI != null) {
            if (this.companyForm.controls.AI.value != undefined && this.companyForm.controls.AI.value != null && this.companyForm.controls.AI.value.length > 0) {
                for (var i = 0; i < this.companyForm.controls.AI.value.length; i++) {
                    if (i != index && this.companyForm.controls.AI.value[i].company_id == event.value) {
                        this.toastr.error("One of the Installer has been entered twice. Please check and try again.", "Duplicate Reseller Selection", { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
                        this.resetInstallerValue(index);
                        return;
                    }
                }
            }
        }
    }

    getAllActiveInstallerOrg() {
        this.companyService.getAllActiveByOrganisationRoles(['Installer']).subscribe(data => {
            this.installerList = data.body;
        }, error => {
            console.log(error);
            this.loading = false;
        });
    }

    getAllActiveMaintenanceOrg() {
        this.companyService.getAllActiveByOrganisationRoles(['Maintenance_Mode']).subscribe(data => {
            this.maintanenceList = data.body;
        }, error => {
            console.log(error);
            this.loading = false;
        });
    }


    getAllActiveResellerOrg() {
        this.companyService.getAllActiveByOrganisationRoles(['Reseller']).subscribe(data => {
            this.resellerList = data.body;
        }, error => {
            console.log(error);
            this.loading = false;
        });
    }

    getAllCustomerForwardingGroup() {
        this.companyService.getAllCustomerForwardingGroup().subscribe(data => {
            this.customerForwardingGroups = data;
        }, error => {
            console.log(error);
            this.loading = false;
        });
    }

    getCustomerForwardingGroupByOrganizationUuid(uuid: string) {
        this.companyService.getCustomerForwardingGroupByOrganizationUuid(uuid).subscribe(data => {
            let forwardingGroups = data;
            if (forwardingGroups != undefined && forwardingGroups != null && forwardingGroups.length > 0) {
                this.customerForwardingGroupUuid = forwardingGroups[0].uuid;
                this.customerForwardingGroupDesc = forwardingGroups[0].description;
            }
        }, error => {
            console.log(error);
            this.loading = false;
        });
    }

    getCustomerForwardingRulesByOrganizationUuid(uuid: string) {
        this.companyService.getCustomerForwardingRulesByOrganizationUuids([uuid]).subscribe(data => {
            this.customerForwardingRules = data;
            for (let index = 0; index < this.customerForwardingRules.length; index++) {
                const element = this.customerForwardingRules[index];
                this.addAnotherRule(element);
            }
        }, error => {
            console.log(error);
            this.loading = false;
        });
    }

    ignoreRulesImeiData: any = {};

    getIgnoreRulesImeiByAccountNumber(accountNumber: string) {
        this.companyService.getIgnoreRuleImeisByCustomerAccountNumber(accountNumber).subscribe(data => {
            this.ignoreRulesImeiData = data.body;
        }, error => {
            console.log(error);
            this.loading = false;
        });
    }

    getLocationsByOrgId(orgId: number) {
        this.companyService.getLocationsByOrgId(orgId).subscribe(data => {
            this.locationsList = data;
            if (this.locationsList && this.locationsList.length > 0) {
                this.locationButton = false;
                this.locationButton2 = true;
                this.handleChangeloc(true);
                this.redioButtonDisable = true;
            }
        }, error => {
            console.log(error);
            this.loading = false;
        });
    }


    openDeletePopup(id) {
        this.heading = 'Confirm Location Deletion';
        this.message = 'Are you sure you want to remove this location?';
        this.isError = false;
        $('#deleteLocationModal').modal('show');
        this.deleteId = id;
    }

    deleteLocations() {
        console.log(this.deleteId);
        this.companyService.deleteOrganisationLocation(this.deleteId).subscribe((data: any) => {
            if (data.status) {
                this.toastr.success('Successfully Deleted', null, { toastComponent: CustomSuccessToastrComponent, tapToDismiss: true, disableTimeOut: true });
                this.getLocationsByOrgId(this.organisationId);
                this.loading = false;
            } else {
                this.heading = 'Location Cannot be Deleted';
                this.message = data.message;
                this.isError = true;
                $('#deleteLocationModal').modal('show');
            }
        }, error => {
            console.log(error);
            this.loading = false;
        });
    }


    editLocation(location: any) {
        this.getLocationFormGroup(location);
        setTimeout(() => {
            $('#editLocationModal').modal('show');
        }, 300);
    }

    updateLocations() {
        console.log(this.locationObj);
        if (this.locationUpdateForm.value && this.locationUpdateForm.value.id) {
            this.companyService.updateOrganisationLocation(this.locationUpdateForm.value).subscribe(data => {
                this.toastr.success('Successfully Updated', null, { toastComponent: CustomSuccessToastrComponent, tapToDismiss: true, disableTimeOut: true });
                this.getLocationsByOrgId(this.organisationId);
                this.loading = false;
            }, error => {
                console.log(error);
                this.loading = false;
            });
        } else {
            this.toastr.error('Please fill the correct data', null, { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
        }
    }
    onSelectChangeEndpointHandler(event, i) {
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
            const items = this.customerForwardingRuleUrls.filter(r => r.uuid == event.value);
            if (items.length > 0) {
                this.forwarding_list.value[i].ruleName = items[0].ruleName;
                this.forwarding_list.value[i].description = items[0].description;
                this.forwarding_list.value[i].type = CommonData.DATA_FORWORDING_RULE_FORMAT_MAPPING[items[0].format];
                this.forwarding_list.value[i].format = items[0].format;
            }
        }
    }

    getAllCustomerForwardingRuleUrl() {
        this.companyService.getAllCustomerForwardingRuleUrl().subscribe(data => {
            this.customerForwardingRuleUrls = data;
        }, error => {
            console.log(error);
            this.loading = false;
        });
    }

    onChangeForwordingGroup(event) {
        if (event != undefined && event != null && event.value != undefined && event.value != null && event.value != "") {
            for (let index = 0; index < this.customerForwardingGroups.length; index++) {
                const element = this.customerForwardingGroups[index];
                if (element.uuid == event.value) {
                    this.customerForwardingGroupDesc = element.description;
                    break;
                }
            }
        }
    }

    ignoreRulesImeis: any[] = [];

    confirmDeleteForwardingRule() {
        if (this.deleteForwardingRuleIndex != -1) {
            let uuid = this.forwarding_list.value[this.deleteForwardingRuleIndex].uuid
            let imeis = this.ignoreRulesImeiData[uuid];
            if (imeis != null && imeis != undefined && imeis.length > 0) {
                this.ignoreRulesImeis = imeis;
                $('#ignoreRulesImeiWarningModal').modal('show');
            }
            this.deleteForwardingRule(this.deleteForwardingRuleIndex);
        }
    }

    deleteForwardingRuleIndex: number = -1;

    onDeleteForwardingRule(index: number) {
        if (this.organisationId
            && this.forwarding_list.value[index].id != 0
            && this.forwarding_list.value[index].uuid != null) {
            this.deleteForwardingRuleIndex = index;
            $('#deleteForwardingRuleModal').modal('show');
        } else if (this.forwarding_list.value[index].forwarding_rule_url_uuid != null
            && this.forwarding_list.value[index].forwarding_rule_url_uuid != undefined
            && this.forwarding_list.value[index].forwarding_rule_url_uuid != '') {
            this.deleteForwardingRuleIndex = index;
            $('#deleteForwardingRuleModal').modal('show');
        }
        else {
            this.deleteForwardingRule(index);
        }
    }

    showAnotherRuleForm: boolean = false;

    showAddAnotherRuleForm() {
        this.showAnotherRuleForm = true;
    }

    newRuleTemp: any = {};

    onSelectChangeEndpointHandlerUpdate(event) {
        let isValid = true;
        if (this.forwarding_list.value.length > 0) {
            for (let index = 0; index < this.forwarding_list.value.length; index++) {
                if (this.forwarding_list.value[index].forwarding_rule_url_uuid === event.value.uuid) {
                    this.companyForm.get('forwarding_rule_url_uuid').patchValue('');
                    this.toastr.error('Already selected endpoint, Please select another one', 'Error', { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: false });
                    isValid = false;
                    break;
                }
            }
        }
        if (isValid) {
            const ele = {
                type: event.value.type != null ? event.value.type : CommonData.DATA_FORWORDING_RULE_FORMAT_MAPPING[event.value.format],
                forwardingRuleUrlUuid: event.value.uuid,
                url: event.value.endpointDestination,
                // uuid: null,
                // id: [0],
                forwardingRuleUrl: {
                    ruleName: event.value.ruleName,
                    description: event.value.description,
                    format: event.value.format
                }
            }
            this.newRuleTemp = ele;
            $('#newOrganizationLevelRuleModal').modal('show');
        }
    }

    addNewRule() {
        this.addAnotherRule(this.newRuleTemp);
        this.companyForm.get('forwarding_rule_url_uuid').patchValue('');
        this.showAnotherRuleForm = false;
    }

    cancelAddNewRule() {
        this.newRuleTemp = {};
        this.companyForm.get('forwarding_rule_url_uuid').patchValue('');
    }
}

export interface PeriodicElement {
    name: string;
    position: number;
    weight: number;
    symbol: string;
}

const ELEMENT_DATA: PeriodicElement[] = [
    { position: 1, name: 'Hydrogen', weight: 1.0079, symbol: 'H' },
    { position: 2, name: 'Helium', weight: 4.0026, symbol: 'He' },
    { position: 3, name: 'Lithium', weight: 6.941, symbol: 'Li' },
    { position: 4, name: 'Beryllium', weight: 9.0122, symbol: 'Be' },
    { position: 5, name: 'Boron', weight: 10.811, symbol: 'B' },
    { position: 6, name: 'Carbon', weight: 12.0107, symbol: 'C' },
    { position: 7, name: 'Nitrogen', weight: 14.0067, symbol: 'N' },
    { position: 8, name: 'Oxygen', weight: 15.9994, symbol: 'O' },
    { position: 9, name: 'Fluorine', weight: 18.9984, symbol: 'F' },
    { position: 10, name: 'Neon', weight: 20.1797, symbol: 'Ne' },
    { position: 11, name: 'Sodium', weight: 22.9897, symbol: 'Na' },
    { position: 12, name: 'Magnesium', weight: 24.305, symbol: 'Mg' },
    { position: 13, name: 'Aluminum', weight: 26.9815, symbol: 'Al' },
    { position: 14, name: 'Silicon', weight: 28.0855, symbol: 'Si' },
    { position: 15, name: 'Phosphorus', weight: 30.9738, symbol: 'P' },
    { position: 16, name: 'Sulfur', weight: 32.065, symbol: 'S' },
    { position: 17, name: 'Chlorine', weight: 35.453, symbol: 'Cl' },
    { position: 18, name: 'Argon', weight: 39.948, symbol: 'Ar' },
    { position: 19, name: 'Potassium', weight: 39.0983, symbol: 'K' },
    { position: 20, name: 'Calcium', weight: 40.078, symbol: 'Ca' },
];
