import { Component, OnInit } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { DeviceReportsService } from '../device-reports/device-reports.service';
import { DevicesListService } from '../devices-list/devices-list.service';

@Component({
  selector: 'app-device-profile',
  templateUrl: './device-profile.component.html',
  styleUrls: ['./device-profile.component.css']
})

export class DeviceProfileComponent implements OnInit {
  loading = false;
  sensorList: any;
  configuration: any;
  dprovision: any[] = [];
  deviceprovision:any;
  cellularpayload: any;
  spayload : any[] = [];
  rawreportform: any;
  isSubmitted: Boolean = false;
  parsedReport: any;
  raw_report: any;
  format: any;
  count: number =0;
  submitted: boolean = false;;
  get f() { return this.rawreportform.controls; }

  constructor(private _formBuldier: FormBuilder,public deviceReportsService: DeviceReportsService,private deviceListService: DevicesListService) { }

  ngOnInit(): void {
    this.loading = true;
    this.deviceListService.getConfigurationDetails(this.deviceListService.deviceId).subscribe(data => {
      if(data!=null){
        console.log(data)
        this.loading = false;
        this.dprovision= data.body;
        this.deviceprovision = this.dprovision[0];
        console.log("Device provision");
        console.log(this.deviceprovision);
        console.log("Sensor List");
        this.sensorList=  this.deviceprovision.sensor_payload;
        this.cellularpayload = this.deviceprovision.cellular_payload;
      }
    }, error => {
      console.log(error)

    });
    this.initializeForm();
  }
  initializeForm() {
    console.log("Initialise Form")
          console.log(this.rawreportform)
          this.count=0;
              this.rawreportform = this._formBuldier.group({
                  raw_report:  ['', Validators.compose([Validators.required])],
                  format:  ['', Validators.compose([Validators.required])],
              });
  }
  onSubmit(rawreportform: { controls: { raw_report: { value: any; }; format: { value: any; }; }; }) {

    console.log("On Form Submit")
    console.log(this.rawreportform)
    this.submitted = true; 
    this.initializeForm(); 
    this.getFormControlValues(rawreportform);     
    this.getParseRawReport();            
    }

    getFormControlValues(rawreportform: { controls: { raw_report: { value: any; }; format: { value: any; }; }; }) {
      this.raw_report = rawreportform.controls.raw_report.value;
     this.format = rawreportform.controls.format.value;   
      }

      getParseRawReport() {
        console.log("Inside getParseRawReport")
        this.deviceListService.getParsedReport(this.raw_report,this.format).subscribe(
            data => {
              this.isSubmitted = true;
              this.parsedReport= data.body;
            },
            error => {
                console.log(error)
            }
        );
    }

    yourFn(event: { tab: { textLabel: string; }; }){
      if(event.tab.textLabel == 'AT Commands'){
        //this.atCommandService.isAddATCommand = false;
      }
    }

}
