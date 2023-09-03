import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { ToastrService } from 'ngx-toastr';
import { ATCommandListComponent } from '../atcommand-list/atcommand-list.component';
import { ATCommandService } from '../atcommand.service';
import { CustomSuccessToastrComponent } from '../custom-success-toastr/custom-success-toastr.component';
import { ATCRequest } from '../models/ATCRequest';

@Component({
  selector: 'app-add-atcommand',
  templateUrl: './add-atcommand.component.html',
  styleUrls: ['./add-atcommand.component.css']
})
export class AddATCommandComponent implements OnInit {
  atcommadform: any;
  count: number;
  gateway_id: any;
  at_command: any;
  priority: any;
  submitted: boolean;
  gatewayListService: any;
  atcResponse: any;
  atcRequest: ATCRequest;
  rolePCTUser:any;
 

  get f() { return this.atcommadform.controls; }
  constructor( private toastr: ToastrService,private  atcommandService: ATCommandService, private _formBuldier: FormBuilder) { }

 
  @Output() someEvent = new EventEmitter<string>();
  @Output() refreshEvent = new EventEmitter<string>();
  ngOnInit() {
    this.initializeForm();

    this.rolePCTUser = JSON.parse(sessionStorage.getItem('IS_ROLE_PCT_USER'));
   }
  initializeForm() {
          this.count=0;
              this.atcommadform = this._formBuldier.group({
                  gateway_id:  ['', Validators.compose([Validators.required])],
                  at_command:  ['', Validators.compose([Validators.required])],
                  priority:  ['', Validators.compose([Validators.required])],
                  wait_time:  [''],
              });
  }
  onSubmit(atcommadform) {
    this.submitted = true; 
    this.initializeForm(); 
    this.getFormControlValues(atcommadform); 
    this.sendATCRequest();    

    }
    sendATCRequest() {
    this.atcommandService.sendATCRequest(this.atcRequest).subscribe(
        data => {
          this.toastr.success("ATC Request sent successfully", null, {toastComponent:CustomSuccessToastrComponent});
          this.atcResponse= data.body;
          this.atcommandService.isAddATCommand = false;
        //  sessionStorage.setItem("atcommand_request",JSON.stringify(this.atcRequest))
        // this.parentApi.refresh()
        this.someEvent.next();
        this.refreshEvent.next();
 
        },
        error => {
            console.log(error)
        }
    );
      }
  
  getFormControlValues(atcommadform) {
    this.atcRequest= new ATCRequest();
    this.atcRequest.gateway_id = sessionStorage.getItem('device_id');
    this.atcRequest.at_command = "at+" + atcommadform.controls.at_command.value;   
    this.atcRequest.priority = "-1";   
    }

}

