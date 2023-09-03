import { Component, OnInit, ViewChild } from '@angular/core';
import { Router } from '@angular/router';
import { FormBuilder, FormGroup, Validators, FormArray } from '@angular/forms';
import { ToastrService } from 'ngx-toastr';
import { ValidatorUtils, MustMatch } from '../util/validatorUtils';
import { CustomErrorToastrComponent } from '../custom-error-toastr/custom-error-toastr.component';
import { CustomSuccessToastrComponent } from '../custom-success-toastr/custom-success-toastr.component';
import { MatSelect } from '@angular/material';
import { UpdateGatewayService } from './update-gateway.service';
import { assetdetails, datalist, Gateway, sensor } from '../models/gateway';
import { CompaniesService } from '../companies.service';
import { GatewayListService } from '../gateway-list.service';
import { CommonData } from '../constant/common-data';
declare var $: any;
@Component({
    selector: 'update-gateway',
    templateUrl: './update-gateway.component.html',
    styleUrls: ['./update-gateway.component.css']
})
export class UpdateGatewayComponent implements OnInit {
    @ViewChild('select') select: MatSelect;
    devicesToBeSaved: any[];

    allSelected = false;
    update_gateway_form: FormGroup;
    notify;
    submitted = false;
    updateFlag = false;
    pageTitle;
    time_zone: String[] = ['Pacific', 'Eastern', 'Central', 'Mountain'];
    validatorUtil: ValidatorUtils = new ValidatorUtils();
    addgateway: Gateway;
    isSuperAdmin: any;
    userService: any;
    companies: any[];
    count: any;
    loading = false;
    rolePCTUser: any;
    buttonDisabled = false;

    // imei_list:[];
    sensor: sensor;
    companiesDetails: string;
    asset_details: assetdetails;
    datalist: any;
    sensorList: any;// = CommonData.SENSOR_LIST;
    sensorSectionList: any;// = CommonData.SENSOR_SECTION_LIST;
    update_sensors_configuration_obj: any = {
        is_replace_existing_configuration: null,
        sensor_list: [],
        imei_list: [],
        is_standard_configuration: false,
        section_id: '30300e47-b305-441d-9577-18436479a241'
    };




    constructor(
        private readonly updateGatewayService: UpdateGatewayService,
        private readonly gatewayListService: GatewayListService,
        private readonly router: Router,
        private readonly formBuldier: FormBuilder,
        private readonly toastr: ToastrService,
        private readonly companiesService: CompaniesService) {
        this.rolePCTUser = JSON.parse(sessionStorage.getItem('IS_ROLE_PCT_USER'));
    }



    ngOnInit() {
        this.pageTitle = 'Add Gateway Details';
        this.getSensorSectionList();
        this.initializeForm();
        this.getSensorList();
        console.log(this.addgateway);
    }

    getSensorSectionList() {
        this.loading = true;
        this.updateGatewayService.getSensorSectionList().subscribe(data => {
            this.sensorSectionList = data.body;
            this.loading = false;
        }, error => {
            console.log(error);
        });
    }
    initializeForm() {
        this.updateGatewayService.editFlag = false;
        if (this.updateFlag) {
            this.update_gateway_form = this.formBuldier.group({
                is_replace_existing_configuration: [null, Validators.compose([Validators.required])],
                sensors: this.formBuldier.array([]),
                is_standard_configuration: [true, Validators.compose([Validators.required])],
                section_id: ['30300e47-b305-441d-9577-18436479a241'],
            });
        } else {
            this.update_gateway_form = this.formBuldier.group({
                is_replace_existing_configuration: [null, Validators.compose([Validators.required])],
                sensors: this.formBuldier.array([]),
                is_standard_configuration: [true, Validators.compose([Validators.required])],
                section_id: ['30300e47-b305-441d-9577-18436479a241'],
            });
            this.addNewSensor(1);
        }
    }


    getSensorList() {
        this.loading = true;
        this.updateGatewayService.getSensorList().subscribe(data => {
            this.sensorList = data.body;
            this.loading = false;
        }, error => {
            console.log(error);
        });
    }
    resetSensorValue(index) {
        const add = this.update_gateway_form.get('sensors') as FormArray;
        add.at(index).patchValue({ sensor: '' });
    }

    onChangeForSensor(event, index) {
        console.log(event.target.value);
        console.log(index);
        if (event && event.target && event.target.value && event.target.value !== '' &&
            this.update_gateway_form.controls &&
            this.update_gateway_form.controls.sensors) {
            if (this.update_gateway_form.controls.sensors.value &&
                this.update_gateway_form.controls.sensors.value.length > 0) {
                for (let i = 0; i < this.update_gateway_form.controls.sensors.value.length; i++) {
                    if(this.update_gateway_form.controls.sensors.value[index].sensor == '84feq5095-ee52-4axa6-b6af-79810edacc80') {
                        if(this.update_gateway_form.controls.sensors.value[i].sensor == 'ec4aa04c-183e-4dc3-bfd2-5a5a6e7d50ec' || 
                        this.update_gateway_form.controls.sensors.value[i].sensor == '349a4a70-03c4-402e-999e-7037cc6404a5') {
                            this.toastr.error("One of the sensors has been entered twice. Please check and try again.", "Duplicate Sensor Selection", { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
                            this.resetSensorValue(index);
                            return;
                        }
                    } else if(this.update_gateway_form.controls.sensors.value[index].sensor == 'ec4aa04c-183e-4dc3-bfd2-5a5a6e7d50ec') {
                        if(this.update_gateway_form.controls.sensors.value[i].sensor == '84feq5095-ee52-4axa6-b6af-79810edacc80' || 
                        this.update_gateway_form.controls.sensors.value[i].sensor == '349a4a70-03c4-402e-999e-7037cc6404a5') {
                            this.toastr.error("One of the sensors has been entered twice. Please check and try again.", "Duplicate Sensor Selection", { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
                            this.resetSensorValue(index);
                            return;
                        }
                    } else if(this.update_gateway_form.controls.sensors.value[index].sensor == '349a4a70-03c4-402e-999e-7037cc6404a5') {
                        if(this.update_gateway_form.controls.sensors.value[i].sensor == '84feq5095-ee52-4axa6-b6af-79810edacc80' || 
                        this.update_gateway_form.controls.sensors.value[i].sensor == 'ec4aa04c-183e-4dc3-bfd2-5a5a6e7d50ec') {
                            this.toastr.error("One of the sensors has been entered twice. Please check and try again.", "Duplicate Sensor Selection", { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
                            this.resetSensorValue(index);
                            return;
                        }
                    } else if(i != index && this.update_gateway_form.controls.sensors.value[i].sensor == event.target.value) {
                        this.toastr.error('One of the sensors has been entered twice. Please check and try again.', 'Duplicate Sensor Selection', { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
                        this.resetSensorValue(index);
                        return;
                    }
                }
            }
        }
    }

    addNewSensor(repeatationValue) {
        const add = this.update_gateway_form.get('sensors') as FormArray;
        for (let i = 0; i < repeatationValue; i++) {
            add.push(this.formBuldier.group({
                sensor: ['']
            }));
        }

    }

    deleteSensor(index: number) {
        const add = this.update_gateway_form.get('sensors') as FormArray;
        if (add.controls.length >= 2) {
            add.removeAt(index);
        }
    }

    get f() { return this.update_gateway_form.controls; }

    onSubmit(updateGatewayForm: any) {
        this.submitted = true;
        this.getFormControlValues(updateGatewayForm);
        // this.updateGatewayDetails();
    }


    getFormControlValues(updateGatewayForm: any) {

        this.update_sensors_configuration_obj.sensor_list = [];
        this.update_sensors_configuration_obj.imei_list = [];
        this.update_sensors_configuration_obj.is_standard_configuration = this.update_gateway_form.controls.is_standard_configuration.value;
        if (this.update_gateway_form.controls.is_standard_configuration.value) {
            if (this.update_gateway_form.controls &&
                this.update_gateway_form.controls.section_id
                && this.update_gateway_form.controls.section_id.value &&
                this.update_gateway_form.controls.section_id.value !== '') {
                this.update_sensors_configuration_obj.section_id = this.update_gateway_form.controls.section_id.value;
            } else {
                this.toastr.error('Please select the sensor package', 'Null Value Validation', { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
                return;
            }
        }
        if (this.update_gateway_form.controls && !this.update_gateway_form.controls.is_standard_configuration.value
            && this.update_gateway_form.controls.sensors &&
            this.update_gateway_form.controls.sensors.value &&
            this.update_gateway_form.controls.sensors.value.length > 0) {
            for (let i = 0; i < this.update_gateway_form.controls.sensors.value.length; i++) {
                if (this.update_gateway_form.controls.sensors.value[i].sensor === '' ||
                    this.update_gateway_form.controls.sensors.value[i].sensor === undefined ||
                    this.update_gateway_form.controls.sensors.value[i].sensor == null) {
                    this.toastr.error('Please select the value for empty sensor select option or remove it', 'Null Value Validation', { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
                    return;
                }
            }
        }

        if (!this.update_gateway_form.controls.is_standard_configuration.value) {
            if (this.update_gateway_form.controls.is_replace_existing_configuration != null && this.update_gateway_form.controls.is_replace_existing_configuration.value != null) {
                this.update_sensors_configuration_obj.is_replace_existing_configuration = this.update_gateway_form.controls.is_replace_existing_configuration.value;
            } else {
                this.toastr.error('Please select the configuration option in the "Choose individual sensors" section ', 'Select Option for Sensor', { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
                return;
            }
        }
        if (!this.gatewayListService.gatewayList) {
            this.gatewayListService.gatewayList = JSON.parse(sessionStorage.getItem('GATEWAY_LIST_DATA'));
        }
        if (this.gatewayListService.gatewayList && this.gatewayListService.gatewayList.length > 0) {
            for (let i = 0; i < this.gatewayListService.gatewayList.length; i++) {
                this.update_sensors_configuration_obj.imei_list.push(this.gatewayListService.gatewayList[i].imei);
            }
        } else {
            this.toastr.error('Something wrong with device seletion please try again.', 'Device(s) Selection Error', { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
            return;
        }

        if (this.update_gateway_form.controls && this.update_gateway_form.controls.sensors &&
            this.update_gateway_form.controls.sensors.value &&
            this.update_gateway_form.controls.sensors.value.length > 0) {
            for (let i = 0; i < this.update_gateway_form.controls.sensors.value.length; i++) {
                for (let j = 0; j < this.sensorList.length; j++) {
                    if (this.update_gateway_form.controls.sensors.value[i].sensor === this.sensorList[j].sensor_uuid) {
                        const sensor = {
                            product_code: this.sensorList[j].product_code,
                            product_name: this.sensorList[j].product_name
                        };
                        this.update_sensors_configuration_obj.sensor_list.push(sensor);
                        break;
                    }
                }
            }
        }

        $('#confirmConfigurationChange').modal('show');
        console.log(this.update_sensors_configuration_obj);


    }


    updateGatewayDetails() {
        this.buttonDisabled = true;
        this.updateGatewayService.updateGateway(this.update_sensors_configuration_obj)
            .subscribe(data => {
                this.buttonDisabled = false;
                this.toastr.success('The selected gateway(s) have been updated with the new configuration.', null, { toastComponent: CustomSuccessToastrComponent });
                this.submitted = false;
                $('#confirmConfigurationChange').modal('hide');
                this.router.navigate(['device-provisioning']);
                this.update_sensors_configuration_obj = {
                    is_replace_existing_configuration: null,
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
        this.router.navigate(['device-provisioning']);
    }

}

