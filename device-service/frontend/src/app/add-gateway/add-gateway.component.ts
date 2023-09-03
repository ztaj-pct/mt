import { Component, OnInit, ViewChild } from '@angular/core';
import { Router } from '@angular/router';
import { FormBuilder, FormGroup, Validators, FormControl } from '@angular/forms';
import { ToastrService } from 'ngx-toastr';
import { ValidatorUtils, MustMatch } from '../util/validatorUtils';
import { CustomErrorToastrComponent } from '../custom-error-toastr/custom-error-toastr.component';
import { CustomSuccessToastrComponent } from '../custom-success-toastr/custom-success-toastr.component';
import { MatOption, MatSelect } from '@angular/material';
import { object } from 'underscore';
import { AddGatewayService } from './add-gateway.service';
import { assetdetails, datalist, Gateway, sensor } from '../models/gateway';
import { get } from 'http';
import { CompaniesService } from '../companies.service';
declare var $: any;
@Component({
    selector: 'app-gateway',
    templateUrl: './add-gateway.component.html',
    styleUrls: ['./add-gateway.component.css']
})
export class AddGatewayComponent implements OnInit {
    @ViewChild('select') select: MatSelect;
    devicesToBeSaved: any[];
    
    allSelected=false;
    shipmentform: FormGroup;
    notify;
    submitted = false;
    updateFlag = false;
    pageTitle;
    time_zone: String[] = ['Pacific', 'Eastern', 'Central', 'Mountain'];
    validatorUtil: ValidatorUtils = new ValidatorUtils();
    addgateway : Gateway;
    isSuperAdmin: any;
    userService: any;
    companies: any[];
    count :any;
   // imei_list:[];
    sensor: sensor;
    companiesDetails: string;
    asset_details: assetdetails;
    datalist: any;



    
    constructor(public addgatewayservice :AddGatewayService,
        public router: Router, private _formBuldier: FormBuilder, private toastr: ToastrService,public companiesService: CompaniesService) { }
        

        
    ngOnInit() {
        this.pageTitle = "Add Gateway Details";
        this.companiesDetails = sessionStorage.getItem('companies');
        this.getAllCompanies();
        this.initializeForm();
        console.log(this.addgateway);
    }
    initializeForm() {
        this.count=0;
        this.addgatewayservice.editFlag = false;
        if (this.updateFlag) {
            this.shipmentform = this._formBuldier.group({
                product_short_name: ['', Validators.compose([Validators.required, Validators.pattern(this.validatorUtil.spaceValidator)])],
                product_code: ['', Validators.compose([Validators.required, Validators.pattern(this.validatorUtil.spaceValidator)])],
                salesforce_order_number: ['', Validators.compose([Validators.required, Validators.pattern(this.validatorUtil.spaceValidator)])],
                epicor_order_number: ['', Validators.compose([Validators.required,  Validators.pattern(this.validatorUtil.spaceValidator)])],
                imei_list: ['', Validators.compose([Validators.required])],
                company: ['', Validators.compose([Validators.required])],
                sensor_product_name:  [' ', Validators.compose([Validators.required])],
                sensor_product_code :['', Validators.compose([Validators.required])],
                time_zone: [this.time_zone[0]]   

        });
    }
        else
        {
            this.shipmentform = this._formBuldier.group({
                product_short_name: ['', Validators.compose([Validators.required, Validators.pattern(this.validatorUtil.spaceValidator)])],
                product_code: ['', Validators.compose([Validators.required, Validators.pattern(this.validatorUtil.spaceValidator)])],
                salesforce_order_number: ['', Validators.compose([Validators.required, Validators.pattern(this.validatorUtil.spaceValidator)])],
                epicor_order_number: ['', Validators.compose([Validators.required,  Validators.pattern(this.validatorUtil.spaceValidator)])],
                imei_list: ['', Validators.compose([Validators.required])],
                company: ['' ,Validators.compose([Validators.required])],
                sensor_product_name:  ['', Validators.compose([Validators.required])],
                sensor_product_code :['' ,Validators.compose([Validators.required])],
                time_zone: [this.time_zone[0]]   
            });
        }

            if (this.updateFlag) {
                this.setFormControlValues();
            }

    }
    setFormControlValues() {
        if (this.updateFlag) {
            
            this.addgateway = this.addgatewayservice.gateway;
            this.asset_details = this.addgatewayservice.asset_details;
            this.shipmentform.patchValue({ 'product_short_name': this.addgatewayservice.asset_details.product_short_name});
            this.shipmentform.patchValue({ 'product_code': this.addgatewayservice.asset_details.product_code});
            this.shipmentform.patchValue({ 'salesforce_order_number': this.addgatewayservice.gateway.salesforce_order_number });
            this.shipmentform.patchValue({ 'epicor_order_number': this.addgatewayservice.gateway.epicor_order_number});
            this.shipmentform.patchValue({  'imei_list' : this.addgatewayservice.asset_details.imei_list});
            this.shipmentform.patchValue({  'sensor_product_name' : this.addgatewayservice.sensor.product_name});
            this.shipmentform.patchValue({  'sensor_product_code' : this.addgatewayservice.sensor.product_code});
            //this.shipmentform.patchValue({  'company' : this.addgatewayservice.gateway.salesforce_account_name});


            
       // if (this.addgatewayservice.gateway.imei_list != null && this.addgatewayservice.gateway.imei_list != undefined 
    //  && this.addgatewayservice.gateway.imei_list != "") {
      //  this.imei_list = this.addgatewayservice.gateway.imei_list.split(',');
    //   console.log(this.imei_list);
     
          // this.devicesToBeSaved.push(this.addgatewayservice.sensor.product_code,this.addgatewayservice.sensor.product_name);
         //  this.shipmentform.patchValue({  'imei_list' : this.addgatewayservice.gateway.imei_list});
         
         this.shipmentform.patchValue({ 'notify': this.addgatewayservice.gateway.notify });
            this.notify =this.addgatewayservice.gateway.notify;
        }
    
    }    
        
    get f() { return this.shipmentform.controls; }
       
    onSubmit(shipmentform) {
        this.submitted = true;  
        this.getFormControlValues(shipmentform);                  
        this.createUser();
        }


    getFormControlValues(shipmentform) {
        this.addgateway = new Gateway();
        this.sensor = new sensor();
        this.datalist = new datalist();
        this.asset_details= new assetdetails();

            console.log("shipmentform" + shipmentform.controls.product_short_name.value);
            console.log(shipmentform.controls);
        
            this.asset_details.product_short_name = shipmentform.controls.product_short_name.value;
            this.asset_details.product_code = shipmentform.controls.product_code.value;
            this.addgateway.salesforce_order_number = shipmentform.controls.salesforce_order_number.value;
            this.addgateway.epicor_order_number = shipmentform.controls.epicor_order_number.value;
            this.asset_details.imei_list = shipmentform.controls.imei_list;
            this.sensor.product_code = shipmentform.controls.sensor_product_code.value;
            this.sensor.product_name= shipmentform.controls.sensor_product_name.value;
            this.addgateway.notify =this.notify;
            this.addgateway.salesforce_account_name = shipmentform.controls.company.value.customer.company_name;
            this.addgateway.salesforce_account_id= shipmentform.controls.company.value.customer.account_number;
            this.datalist.product_name= shipmentform.controls.product_short_name.value;
            this.datalist.product_code= shipmentform.controls.product_code.value;
           // this.user.companies = registerForm.controls.company.value.customer;


           // this.addgateway.sensor_list.push(new sensor(this.sensor.product_code,this.sensor.product_name));
            this.asset_details.sensor_list.push(this.sensor);
            
           var str = shipmentform.controls.imei_list.value;
           var str_array = str.split(',');
            for(let i=0;i<str_array.length;i++)
            {
               console.log(str_array);
               this.asset_details.imei_list=str_array;
            }
            
           this.asset_details.data_list.push(this.datalist);
           this.asset_details.quantity_shipped = str_array.length;  
           this.addgateway.asset_details.push(this.asset_details);
            
           // console.log(this.addgateway.asset_details);
               
         // let imei_list = shipmentform.controls.imei_list.value;
       //   for(let imei of imei_list)
        //  {
        //    console.log(imei);
         //   this.addgateway.imei_list.push(imei);
       //   }

        }
        

    createUser() {
     console.log("Gateway  created");
        console.log(this.addgateway);
        this.addgatewayservice.createGateway(this.addgateway)
            .subscribe(data => {
                this.toastr.success("Gateway successfully added", null, {toastComponent:CustomSuccessToastrComponent});
                this.submitted = false;

                this.router.navigate(['add-gateway']);
                this.addgatewayservice.gateway = undefined;
                this.addgatewayservice.navigateFromSettings = undefined;
                this.shipmentform.reset();
            }, error => {
                this.cancel();
                console.log('something went wrong');
                console.log(error);

            });
        }

        getAllCompanies() {
            this.companiesService.getCompanies("ALL").subscribe(
                data => {
                    this.companies = data.body.content;
                    console.log("company List")
                    console.log(JSON.stringify(this.companies))
                    if (this.updateFlag) {
                       // this.shipmentform.controls['company'].patchValue(this.companies[this.companies.findIndex(com => customer.company_name === this.userService.user.companies.companyName)]);
                       this.shipmentform.controls['salesforce_account_name'].patchValue(this.companies[this.companies.findIndex(com => com.customer.company_name === this.addgatewayservice.gateway.salesforce_account_name)]);
                      this.shipmentform.controls['salesforce_account_id'].patchValue(this.companies[this.companies.findIndex(com => com.customer.id  === this.addgatewayservice.gateway.salesforce_account_id)]);
                        console.log(this.companies.findIndex(com => com.customer.company_name === this.userService.user.companies.companyName));
                    }
                },
                error => {
                    console.log(error)
                }
            );
        }


        cancel() {
            this.addgatewayservice.gateway = undefined;
            this.addgatewayservice.navigateFromSettings = undefined;
            this.shipmentform.reset();
            this.router.navigate(['add-gateway']);
        }

}

