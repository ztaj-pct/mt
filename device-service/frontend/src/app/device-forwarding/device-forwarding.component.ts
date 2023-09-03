import { Component, OnInit } from '@angular/core';
import { FormArray, FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { ToastrService } from 'ngx-toastr';
import { map } from 'rxjs/operators';
import { CommonData } from '../constant/common-data';
import { CustomErrorToastrComponent } from '../custom-error-toastr/custom-error-toastr.component';
import { CustomSuccessToastrComponent } from '../custom-success-toastr/custom-success-toastr.component';
import { GatewayListService } from '../gateway-list.service';
import { DeviceForwarding, Forwarding } from '../models/forwarding';

@Component({
  selector: 'app-device-forwarding',
  templateUrl: './device-forwarding.component.html',
  styleUrls: ['./device-forwarding.component.css']
})
export class DeviceForwardingComponent implements OnInit {
  deviceforwardingform: any;
  forwarding = new Forwarding();
  deviceForwarding = new DeviceForwarding();
  public deviceForwordingRuleData: any[] = CommonData.DEVICE_FORWORDING_RULE_DATA;
  url1: any;
  submitted: boolean;
  forwardingDetails: any;
  deviceForwardingDetails: any;
  updateFlag = false;
  notify: any;
  deviceForwardingRuleList: any[] = [];
  isReadonly = false;
  isEnd = false;
  rolePCTUser: any;
  constructor(
    private readonly _formBuldier: FormBuilder,
    private readonly gatewayListService: GatewayListService,
    private readonly toastr: ToastrService) { }

  get f() { return this.deviceforwardingform.controls; }

  ngOnInit() {
    this.initializeForm();
    this.getDeviceForwardingList();
    this.rolePCTUser = JSON.parse(sessionStorage.getItem('IS_ROLE_PCT_USER'));

  }
  getDeviceForwardingList() {
    this.gatewayListService.getDeviceForwardingList(sessionStorage.getItem('device_id')).subscribe(
      data => {
        console.log(data);
        data.body ? data.body.forEach(element => {
          element.value = '';
          switch (element.type) {
            case 'elastic':
              element.value = 'Elastic (RAW)';
              break;
            case 'json':
              element.value = 'Standard JSON';
              break;
            case 'ups':
              element.value = 'Custom (UPS)';
              break;
            case 'eroad':
              element.value = 'Kinesis(EROAD)';
              break;

            default:
              break;
          }
        }) : [];
        this.deviceForwardingRuleList = data.body ? data.body : [];
        if (this.deviceForwardingRuleList.length <= 3) {
          this.isReadonly = true;
        }
        if (this.deviceForwardingRuleList.length <= 0) {
          this.addAnotherRule('', '', null, 0);
        } else {
          this.deviceForwardingRuleList.forEach(element => {
            this.addAnotherRule(element.type, element.url, element.uuid, element.id);
          });
        }

      },
      error => {
        console.log(error);
      }
    );
  }

  onSubmit(deviceforwardingform: FormGroup) {
    if (!deviceforwardingform.value.device_id) {
      this.toastr.error('Device Id not present', 'Error', { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: false });
      return;
    }

    if (deviceforwardingform.valid) {
      // if(this.deviceForwardingRuleList.length<4){
      this.addDeviceForwardingRule();
      // }else{
      //   this.toastr.error("All the rule are added", "Error",{toastComponent: CustomErrorToastrComponent, tapToDismiss:true, disableTimeOut:false});

      // }
    }

  }


  addDeviceForwardingRule() {
    // let ruleList:any[] = [];
    // for (let index = 0; index < this.deviceforwardingform.value.forwarding_list.length; index++) {
    //   const element = this.deviceforwardingform.value.forwarding_list[index];
    //     if(element.uuId==null){
    //       ruleList.push(element);
    //     }
    // }
    // this.deviceforwardingform.value.forwarding_list = ruleList;
    this.gatewayListService.addDeviceForwardingRule(this.deviceforwardingform.value).subscribe(
      data => {
        this.toastr.success('Rule successfully added', null, { toastComponent: CustomSuccessToastrComponent });
        this.initializeForm();
        this.getDeviceForwardingList();
      },
      error => {
        console.log(error);
        this.toastr.error(error.message, 'Error', { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: false });
      }
    );
  }

  updateDeviceForwarding() {
    this.forwarding.device_id = sessionStorage.getItem('device_id');
    console.log('Device Forwarding Details');
    console.log(this.forwarding);
    console.log('Inside getParseRawReport');
    this.gatewayListService.updateDeviceForwarding(this.forwarding).subscribe(
      data => {
        this.forwardingDetails = data.body;
      },
      error => {
        console.log(error);
      }
    );
  }
  initializeForm() {
    this.deviceforwardingform = this._formBuldier.group({
      device_id: sessionStorage.getItem('device_id'),
      forwarding_list: this._formBuldier.array([]),
    });
    // [
    //   this._formBuldier.group({
    //     type: ['', Validators.compose([Validators.required])],
    //     url: ['', Validators.compose([Validators.required])],
    //   })
    // ]
  }

  get forwarding_list(): FormArray {
    return this.deviceforwardingform.get('forwarding_list') as FormArray;
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
    // if (this.deviceForwardingRuleList.length > 0) {
    //   for (let index = 0; index < this.deviceForwardingRuleList.length; index++) {
    //       if (this.deviceForwardingRuleList[index].type === event.value) {
    //         this.forwarding_list.controls[i].patchValue({
    //           type: "",
    //         });
    //         this.toastr.error("Already added this rule, Please select another one", "Error",{toastComponent: CustomErrorToastrComponent, tapToDismiss:true, disableTimeOut:false});
    //       }
    //   }
    // }
  }


  addAnotherRule(type: any, url: any, uuId: any, id: any) {

    if (this.forwarding_list.controls.length <= 3) {
      this.forwarding_list.push(this.newRuleFormGroup(type, url, uuId, id));
    }
    if (this.forwarding_list.controls.length >= (4 - this.deviceForwardingRuleList.length)) {
      this.isEnd = true;
    }
  }

  newRuleFormGroup(type: any, url: any, uuId: any, id: any): FormGroup {
    return this._formBuldier.group({
      type: [type, Validators.compose([Validators.required])],
      url: [url, Validators.compose([Validators.required])],
      uuid: [uuId ? uuId : null],
      id: [id ? id : 0],
    });
  }

  removeRule(index: number) {
    this.forwarding_list.removeAt(index);
  }

  modifyCustomerForwarding() {

  }

  // setFormControlValues() {
  //   this.deviceforwardingform.patchValue({ 'type1': this.gatewayListService.forwarding[0].type });
  //   this.deviceforwardingform.patchValue({ 'url1': this.gatewayListService.forwarding[0].url });
  //   this.deviceforwardingform.patchValue({ 'type2': this.gatewayListService.forwarding[1].type });
  //   this.deviceforwardingform.patchValue({ 'url2': this.gatewayListService.forwarding[1].url });
  //   this.deviceforwardingform.patchValue({ 'type3': this.gatewayListService.forwarding[2].type });
  //   this.deviceforwardingform.patchValue({ 'url3': this.gatewayListService.forwarding[2].url });
  //   this.deviceforwardingform.patchValue({ 'type4': this.gatewayListService.forwarding[3].type });
  //   this.deviceforwardingform.patchValue({ 'url4': this.gatewayListService.forwarding[3].url });
  // }
}
