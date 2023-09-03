import { Component, OnInit } from '@angular/core';
import {MatTabsModule} from '@angular/material/tabs';
import { AppService } from './app.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit
  {
    loading = false;
  sensorList: any;
  configuration: any;
  dprovision : any[] = [];
  deviceprovision:any;
  cellularpayload: any;
  spayload : any[] = [];
    constructor(private appService: AppService) { }

  ngOnInit(): void {
    this.loading = true;
    this.appService.getConfigurationDetails().subscribe(data => {
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
  }
}

