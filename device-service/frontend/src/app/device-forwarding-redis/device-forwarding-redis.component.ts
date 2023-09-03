import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormControl, FormGroup } from '@angular/forms';
import { ToastrService } from 'ngx-toastr';
import { GatewayListService } from '../gateway-list.service';

@Component({
  selector: 'app-device-forwarding-redis',
  templateUrl: './device-forwarding-redis.component.html'
})
export class DeviceForwardingRedisComponent implements OnInit {

    deviceForwardingform: any;
    deviceForwarding: any = {
      deviceForwardingRules: [],
      customerForwardingRules: [],
      customerIgnoreForwardingRules: [],
      deviceForwardingApplingRules: [],
      purchasedByForwardingRules: []
    };
    loading: boolean = false;

constructor(
    private readonly _formBuldier: FormBuilder,
    private readonly gatewayListService: GatewayListService,
    private readonly toastr: ToastrService) { }

  ngOnInit() {
    this.initializeForm();
  }

  onSubmit() {
    this.validateAllFormFields(this.deviceForwardingform);
    if (this.deviceForwardingform.valid) {
      this.loading = true;
      this.gatewayListService.getDeviceForwardingFromRedis(this.deviceForwardingform.value.imei).subscribe(
        data => {
          this.deviceForwarding = data.body;
          this.loading = false;
        },
        error => {
          console.log(error);
          this.loading = false;
        }
      );
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

  initializeForm() {
    this.deviceForwardingform = this._formBuldier.group({
      imei: ['']
    });
  }

}
