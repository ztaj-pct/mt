import { Component, OnInit, ViewChild } from '@angular/core';
import { Router } from '@angular/router';
import { FormBuilder, FormGroup, Validators, FormArray } from '@angular/forms';
import { ToastrService } from 'ngx-toastr';
import { ValidatorUtils, MustMatch } from '../util/validatorUtils';
import { CustomErrorToastrComponent } from '../custom-error-toastr/custom-error-toastr.component';
import { CustomSuccessToastrComponent } from '../custom-success-toastr/custom-success-toastr.component';
import { MatSelect } from '@angular/material';
import { assetdetails, datalist, Gateway, sensor } from '../models/gateway';
import { CompaniesService } from '../companies.service';
import { GatewayListService } from '../gateway-list.service';
import { Location } from '@angular/common';
import { CommonData } from '../constant/common-data';
import { UpdateGatewayService } from '../update-gateway/update-gateway.service';
import { GatewayDataForwardService } from './gateway-data-forward-rule.service';
import { constant } from 'underscore';
declare var $: any;
@Component({
    selector: 'gateway-data-forward-rule',
    templateUrl: './gateway-data-forward-rule.component.html',
    styleUrls: ['./gateway-data-forward-rule.component.css']
})
export class GatewayDataForwardRuleComponent implements OnInit {
    @ViewChild('select') select: MatSelect;
    devicesToBeSaved: any[];

    allSelected = false;
    update_gateway_form: FormGroup;
    submitted = false;
    pageTitle;
    time_zone: String[] = ['Pacific', 'Eastern', 'Central', 'Mountain'];
    validatorUtil: ValidatorUtils = new ValidatorUtils();
    isSuperAdmin: any;
    userService: any;
    companies: any[];
    count: any;
    loading = false;
    rolePCTUser: any;
    buttonDisabled = false;
    maxRules = true;

    // imei_list:[];
    sensorList: any;// = CommonData.SENSOR_LIST;
    sensorSectionList: any;// = CommonData.SENSOR_SECTION_LIST;
    gateway_data_forward_obj: any = {
        action: null,
        forwarding_rules: [],
        imeis: [],
    };




    constructor(
        private readonly updateGatewayService: UpdateGatewayService,
        private readonly gatewayListService: GatewayListService,
        private readonly router: Router,
        private readonly formBuldier: FormBuilder,
        private readonly toastr: ToastrService,
        private location: Location,
        private readonly gatewayDataForwardService: GatewayDataForwardService) {
        this.rolePCTUser = JSON.parse(sessionStorage.getItem('IS_ROLE_PCT_USER'));
        if(this.rolePCTUser){
            this.location.back();
        }
    }



    ngOnInit() {
        this.pageTitle = 'Add Gateway Details';
        this.getAllCustomerForwardingRuleUrl();
        this.initializeForm();
    }

    getAllCustomerForwardingRuleUrl() {
        this.gatewayDataForwardService.getAllCustomerForwardingRuleUrl().subscribe(data => {
            this.sensorList = data;
        }, error => {
            console.log(error);
            this.loading = false;
        });
    }
    

    initializeForm() {
            this.update_gateway_form = this.formBuldier.group({
                secondaryAction: ["add", [Validators.required]],
                rules: this.formBuldier.array([]),
                primaryAction: ["specify", [Validators.required]]
            });
       
            this.addNewSensor(1);
    }

    radioChange(){
        if(this.update_gateway_form.value.primaryAction != 'specify'){
            this.update_gateway_form.controls['secondaryAction'].setValue('');
        }
        else{
            if (this.update_gateway_form.value.secondaryAction != 'replace')
            this.update_gateway_form.controls['secondaryAction'].setValue('add');
        }
    }

    radioChange1(){

        if(this.update_gateway_form.value.secondaryAction == 'add' || this.update_gateway_form.value.secondaryAction == 'replace'){
            this.update_gateway_form.controls['primaryAction'].setValue('specify');
            this.update_gateway_form.controls['secondaryAction'].setValue(this.update_gateway_form.value.secondaryAction);
        }
    }

    // resetSensorValue(index) {
    //     const add = this.update_gateway_form.get('rules') as FormArray;
    //     add.at(index).patchValue({ rule: '' });
    // }
description = ["","","","",""];
    onChangeForSensor(event, index) {
         console.log(event.value);
         console.log(index);
         console.log(this.sensorList);
        const obj = this.sensorList.find(item=>item.uuid === event.value);
        if(obj)
        {
          this.description[index] = obj.description;
        }
         let count = 0;
        this.update_gateway_form.controls.rules.value.forEach(element => {
            if(element.rule === event.value)
        {
            count = count + 1;
        }
        });
        if(count > 1)
        {
            this.toastr.error('Same rule already selected please select another rule or remove it', 'Duplicate Value Validation', { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
            return;  
        }
        
    }

    addNewSensor(repeatationValue) {
        const add = this.update_gateway_form.get('rules') as FormArray;
        for (let i = 0; i < repeatationValue; i++) {
            add.push(this.formBuldier.group({
                rule: ['']
            }));
        }
        if(this.update_gateway_form.get('rules').value.length >=5 )
        {
            this.maxRules = false;
        }
        else{
            this.maxRules = true;
        }

    }

    deleteSensor(index: number) {
        
        const add = this.update_gateway_form.get('rules') as FormArray;
        for (let i = 0; i < this.description.length; i++) {
            if(add.controls.length >=2  && i >= index + 1)
            {
                this.description[i-1] = this.description[i]; 
            }
            
        }
        if (add.controls.length >= 2) {
            add.removeAt(index);
        }
       
        
        
        if(this.update_gateway_form.get('rules').value.length >=5 )
        {
            this.maxRules = false;
        }
        else{
            this.maxRules = true;
        }
    }

    get f() { return this.update_gateway_form.controls; }

    onSubmit(updateGatewayForm: any) {
        this.submitted = true;
        this.getFormControlValues(updateGatewayForm);
        // this.updateGatewayDetails();
    }


    getFormControlValues(updateGatewayForm: any) {

        this.gateway_data_forward_obj.forwarding_rules = [];
        this.gateway_data_forward_obj.imeis = [];

        if (this.update_gateway_form.controls  
            && this.update_gateway_form.controls.primaryAction.value 
            && this.update_gateway_form.controls.primaryAction.value == "specify"
            && this.update_gateway_form.controls.rules &&
            this.update_gateway_form.controls.rules.value &&
            this.update_gateway_form.controls.rules.value.length > 0) {
            for (let i = 0; i < this.update_gateway_form.controls.rules.value.length; i++) {
                if (this.update_gateway_form.controls.rules.value[i].rule === '' ||
                    this.update_gateway_form.controls.rules.value[i].rule === undefined ||
                    this.update_gateway_form.controls.rules.value[i].rule == null) {
                    this.toastr.error('Please select the value for empty rule select option or remove it', 'Null Value Validation', { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
                    return;
                }
            }
        }

        if (this.update_gateway_form.controls.primaryAction.value && this.update_gateway_form.controls.primaryAction.value == "specify") {
            if (this.update_gateway_form.controls.secondaryAction != null && this.update_gateway_form.controls.secondaryAction.value != null) {
                this.gateway_data_forward_obj.action = this.update_gateway_form.controls.secondaryAction.value;
            } 
        }
        else 
        {
            this.gateway_data_forward_obj.action = this.update_gateway_form.controls.primaryAction.value;
        }
        if (!this.gatewayListService.gatewayList) {
            this.gatewayListService.gatewayList = JSON.parse(sessionStorage.getItem('GATEWAY_LIST_DATA'));
        }
        if (this.gatewayListService.gatewayList && this.gatewayListService.gatewayList.length > 0) {
            for (let i = 0; i < this.gatewayListService.gatewayList.length; i++) {
                this.gateway_data_forward_obj.imeis.push(this.gatewayListService.gatewayList[i].imei);
            }
        } else {
            this.toastr.error('Something wrong with device seletion please try again.', 'Device(s) Selection Error', { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
            return;
        }

        if (this.update_gateway_form.controls && this.update_gateway_form.controls.rules &&
            this.update_gateway_form.controls.rules.value &&
            this.update_gateway_form.controls.rules.value.length > 0 
            && this.update_gateway_form.controls.primaryAction.value 
            && this.update_gateway_form.controls.primaryAction.value == "specify") {
            for (let i = 0; i < this.update_gateway_form.controls.rules.value.length; i++) {
                for (let j = 0; j < this.sensorList.length; j++) {
                    if (this.update_gateway_form.controls.rules.value[i].rule === this.sensorList[j].uuid) {
                        const rule = {
                            type:"json",
                            url: this.sensorList[j].endpointDestination,
                            forwarding_rule_url_uuid: this.sensorList[j].uuid
                        };
                        this.gateway_data_forward_obj.forwarding_rules.push(rule);
                        break;
                    }
                }
            }
        }
        if(this.gateway_data_forward_obj.action == "add") {
            $('#confirmAddDeviceSpecificRules').modal('show');  
        } else if(this.gateway_data_forward_obj.action == "replace") {
            $('#confirmReplaceDeviceSpecificRules').modal('show');  
        } else if(this.gateway_data_forward_obj.action == "restore") {
            $('#confirmRestoreDefaultRules').modal('show');  
        } else {
            $('#confirmRemoveAllDataForwardingRules').modal('show');  
        }
        //$('#confirmConfigurationChange').modal('show');
        console.log(this.gateway_data_forward_obj);


    }


    updateDataForwardRule() {
        this.buttonDisabled = true;
        this.gatewayDataForwardService.updateDataForwardRule(this.gateway_data_forward_obj)
            .subscribe(data => {
                this.buttonDisabled = false;
                this.toastr.success('The selected gateway(s) have been updated with the new rules.', null, { toastComponent: CustomSuccessToastrComponent });
                this.submitted = false;
                $('#confirmConfigurationChange').modal('hide');
                this.router.navigate(['gateway-list']);
                this.gateway_data_forward_obj = {
                    secondaryAction: null,
                    sensor_list: [],
                    imei_list: []
                };
                this.update_gateway_form.reset();
            }, error => {
                this.buttonDisabled = false;
                this.cancel();
                console.log('something went wrong');
                console.log(error);

            });
    }

    cancel() {
        this.update_gateway_form.reset();
        this.router.navigate(['gateway-list']);
    }

}

