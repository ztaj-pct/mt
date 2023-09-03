import { Component, OnInit } from '@angular/core';
import { Device } from '../models/device.model'
import { FormBuilder, FormGroup, Validators, FormControl } from '@angular/forms';
import { CampaignService } from '../campaign/campaign.component.service';
import { NavigationStart, Router, Event as NavigationEvent } from "@angular/router";
import { ToastrService } from 'ngx-toastr';
import { Package } from '../models/package.model';
import { AssetsService } from '../assets.service';
import { CustomSuccessToastrComponent } from '../custom-success-toastr/custom-success-toastr.component';
import { filter } from 'rxjs/operators';
import { DirtyComponent } from '../util/dirty-component';



declare var $: any;

const HEADER_ROW = 3;
const DATA_START_ROW = 1;
const NUMBER_OF_COLUMNS = 4;
const SESSION_ID = "0993b670-084a-4bdf-bbe7-af5895e19939";
const API_KEY = "1bde5f76-05ee-434a-b7e8-da05a9f71a3a";
const PRODUCT_ID = 10048;

@Component({
	selector: 'app-device',
	templateUrl: './campaign.component.html',
	styleUrls: ['./campaign.component.css']
})
export class CampaignComponent implements OnInit, DirtyComponent {

	addPackage: FormGroup;
	fileToUpload: File = null;
	data: Array<any> = [];
	updateFlag = false;
	submitted = false;
	deletePackageID = false;
	popupMessage;
	popupTitle;
	loading = false;
	addPackageValue;
	packageList = [];
	products = ['TrailerNet', 'ChassisNet', 'ContainerNet', 'AssetTrac', 'EZTrac', 'EZTrac Solar', 'EZTrac Plus'];
	lookupValues = ['BIN_VERSION', 'APP_VERSION', 'MCU_VERSION', 'BLE_VERSION','CONFIG1', 'CONFIG2', 'CONFIG3', 'CONFIG4', 'LITE_SENTRY_HARDWARE'
      , 'LITE_SENTRY_APP', 'LITE_SENTRY_BOOT', 'MICROSP_APP', 'MICROSP_MCU', 'CARGO_MAXBOTIX_HARDWARE', 'CARGO_MAXBOTIX_FIRMWARE', 'CARGO_RIOT_HARDWARE', 'CARGO_RIOT_FIRMWARE','DEVICE_TYPE'];

	binVersionList = [];
	appVersionList = [];
	mcuVersionList = [];
	bleVersionList = [];
	config1List = [];
	config2List = [];
	config3List = [];
	config4List = [];

	liteSentryHardwareList = [];
	liteSentryAppList = [];
	liteSentryBootList = [];
	microspAppList = [];
	microspMcuList = [];
	cargoMaxbotixHardwareList = [];
	cargoMaxbotixFirmwareList = [];
	cargoRiotHardwareList = [];
	cargoRiotFirmwareList = [];
	deviceTypeList = [];

	packageObj;
	lookupName = "";
	lookupValue = "";
	lookupValueModel = new FormControl('');
	originalFormValue: any;

	isDirty = false;
	validCRC2 = false;
	validCRC3 = false;
	validCRC4 = false;

	userCompanyType;
	isSuperAdmin;

	constructor(public _formBuldiers: FormBuilder, public router: Router, public campaignService: CampaignService, public toastr: ToastrService) {
		//   this.router.events
		//   .pipe(
		//     filter( event =>event instanceof NavigationStart)
		//   )
		//   .subscribe(
		//     (event: NavigationEvent) => {
		//       if(this.isDirty){



		//         alert('hi');
		//       }

		//       console.log(event);
		//   }
		// )
	}

	canDeactivate() {
		return this.isDirty;
	}


	ngOnInit() {
		this.userCompanyType = sessionStorage.getItem('type');
		this.isSuperAdmin = sessionStorage.getItem('IS_ROLE_SUPERADMIN');
		if (this.isSuperAdmin === 'true') {
			this.router.navigate(['package']);
		} else {
			this.router.navigate(['customerAssets']);
		}
		if (this.campaignService.packageDetail != undefined && this.campaignService.editFlag == true) {
			this.updateFlag = true;
		}
		this.campaignService.editFlag = false;
		this.initializeForm();
		this.getLookupValues();

		//this.addPackage.valueChanges.subscribe( e => this.isDirty = true );
	}
	print() {
		console.log(JSON.stringify(this.addPackage.value));
		this.isDirty = true;
	}
	get f() { return this.addPackage.controls; }

	// get isDirty(): boolean {

	//   console.log(JSON.stringify(this.originalFormValue));
	//   console.log(JSON.stringify(this.addPackage.value));
	//   return JSON.stringify(this.originalFormValue) !== JSON.stringify(this.addPackage.value);
	// }

	initializeForm() {
		this.addPackage = this._formBuldiers.group({
			package_name: ['', Validators.compose([Validators.required])],
			bin_version: ['', Validators.compose([Validators.required])],
			app_version: ['', Validators.compose([Validators.required])],
			mcu_version: ['', Validators.compose([Validators.required])],
			ble_version: ['', Validators.compose([Validators.required])],
			config1: ['', Validators.compose([Validators.required])],
			config2: ['', Validators.compose([Validators.required])],
			config3: ['', Validators.compose([Validators.required])],
			config4: ['', Validators.compose([Validators.required])],
			config1_crc: ['', Validators.compose([Validators.required, Validators.pattern(/^(\s+\S+\s*)*(?!\s).*$/)])],
			config2_crc: ['', Validators.compose([Validators.required, Validators.pattern(/^(\s+\S+\s*)*(?!\s).*$/)])],
			config3_crc: ['', Validators.compose([Validators.required, Validators.pattern(/^(\s+\S+\s*)*(?!\s).*$/)])],
			config4_crc: ['', Validators.compose([Validators.required, Validators.pattern(/^(\s+\S+\s*)*(?!\s).*$/)])],
			isIgnoreForMCU: [false],
			isIgnoreForBLE: [false],
			isIgnoreForConfig2: [false],
			isIgnoreForConfig3: [false],
			isIgnoreForConfig4: [false],
			is_used_in_campaign: [true],

			device_type: ['ANY', Validators.compose([Validators.required])],
			lite_sentry_hardware: ['ANY', Validators.compose([Validators.required])],
			lite_sentry_app: ['ANY', Validators.compose([Validators.required])],
			lite_sentry_boot: ['ANY', Validators.compose([Validators.required])],
			microsp_app: ['ANY', Validators.compose([Validators.required])],
			microsp_mcu: ['ANY', Validators.compose([Validators.required])],
			cargo_maxbotix_hardware: ['ANY', Validators.compose([Validators.required])],
			cargo_maxbotix_firmware: ['ANY', Validators.compose([Validators.required])],
			cargo_riot_hardware: ['ANY', Validators.compose([Validators.required])],
			cargo_riot_firmware: ['ANY', Validators.compose([Validators.required])]


		}, {
			validator: [this.duplicatePackageName('package_name')]
		});

		this.originalFormValue = this.addPackage.value;

		if (this.updateFlag) {
			this.setFormControlValues();
		}
		const ble_version_control = this.addPackage.get('ble_version');
		const mcu_version_control = this.addPackage.get('mcu_version');
		const config_2 = this.addPackage.get('config2');
		const config_3 = this.addPackage.get('config3');
		const config_4 = this.addPackage.get('config4');

		this.addPackage.get('isIgnoreForBLE').valueChanges
			.subscribe(isIgnoreForBLE => {
				if (isIgnoreForBLE) {
					ble_version_control.setValidators(null);
					this.addPackage.patchValue({ 'ble_version': "" });
				} else {
					ble_version_control.setValidators([Validators.required])
					this.addPackage.patchValue({ 'ble_version': "" });
				}
				ble_version_control.updateValueAndValidity();
			});

		this.lookupValueModel.valueChanges.subscribe(selectedValue => {
			if (selectedValue != undefined && selectedValue != null && selectedValue != "") {
				$('#validateRequired').addClass("displayNameNode");
			} else {
				$('#validateRequired').removeClass("displayNameNode");
			}
		})
		this.addPackage.get('isIgnoreForMCU').valueChanges
			.subscribe(isIgnoreForMCU => {
				if (isIgnoreForMCU) {
					mcu_version_control.setValidators(null);
					this.addPackage.patchValue({ 'mcu_version': "" });
				} else {
					mcu_version_control.setValidators([Validators.required]);
					this.addPackage.patchValue({ 'mcu_version': "" });
				}
				mcu_version_control.updateValueAndValidity();
			});



		this.addPackage.get('isIgnoreForConfig2').valueChanges
			.subscribe(isIgnoreForConfig2 => {

				if (isIgnoreForConfig2) {
					config_2.setValidators(null);
					this.addPackage.patchValue({ 'config2': "" });
				} else {
					config_2.setValidators([Validators.required]);
					this.addPackage.patchValue({ 'config2': "" });
				}
				config_2.updateValueAndValidity();
			});

		this.addPackage.get('isIgnoreForConfig3').valueChanges
			.subscribe(isIgnoreForConfig3 => {
				if (isIgnoreForConfig3) {
					config_3.setValidators(null);
					this.addPackage.patchValue({ 'config3': "" });
				} else {
					config_3.setValidators([Validators.required]);
					this.addPackage.patchValue({ 'config3': "" });
				}
				config_3.updateValueAndValidity();
			});

		this.addPackage.get('isIgnoreForConfig4').valueChanges
			.subscribe(isIgnoreForConfig4 => {
				if (isIgnoreForConfig4) {
					config_4.setValidators(null);
					this.addPackage.patchValue({ 'config4': "" });
				} else {
					config_4.setValidators([Validators.required]);
					this.addPackage.patchValue({ 'config4': "" });
				}
				config_4.updateValueAndValidity();
			});
	}
	toggleCRC2() {
		if (this.addPackage.get('config2').value.toLowerCase() == "missing") {
			this.addPackage.controls['config2_crc'].disable();
			this.addPackage.patchValue({ 'config2_crc': "" });
			this.validCRC2 = true;
		}
		else if (this.addPackage.get('isIgnoreForConfig2').value) {
			this.addPackage.controls['config2_crc'].disable();
			this.addPackage.patchValue({ 'config2_crc': "" });
			this.validCRC2 = true;
		}
		else if ((!this.addPackage.get('isIgnoreForConfig2').value) &&
			(this.addPackage.get('config2').value.toLowerCase() != "missing")) {
			this.addPackage.controls['config2_crc'].enable();
			this.validCRC2 = false;
		}
		else {
			this.addPackage.controls['config2_crc'].enable();
			this.validCRC2 = false;
		}
	}
	toggleCRC3() {
		if (this.addPackage.get('config3').value.toLowerCase() == "missing") {
			this.addPackage.controls['config3_crc'].disable();
			this.addPackage.patchValue({ 'config3_crc': "" });
			this.validCRC3 = true;
		}
		else if (this.addPackage.get('isIgnoreForConfig3').value) {
			this.addPackage.controls['config3_crc'].disable();
			this.addPackage.patchValue({ 'config3_crc': "" });
			this.validCRC3 = true;
		}
		else if ((!this.addPackage.get('isIgnoreForConfig3').value) &&
			(this.addPackage.get('config3').value.toLowerCase() != "missing")) {
			this.addPackage.controls['config3_crc'].enable();
			this.validCRC3 = false;
		}
		else {
			this.addPackage.controls['config3_crc'].enable();
			this.validCRC3 = false;
		}
	}
	toggleCRC4() {
		if (this.addPackage.get('config4').value.toLowerCase() == "missing") {
			this.addPackage.controls['config4_crc'].disable();
			this.addPackage.patchValue({ 'config4_crc': "" });
			this.validCRC4 = true;
		}
		else if (this.addPackage.get('isIgnoreForConfig4').value) {
			this.addPackage.controls['config4_crc'].disable();
			this.addPackage.patchValue({ 'config4_crc': "" });
			this.validCRC4 = true;
		}
		else if ((!this.addPackage.get('isIgnoreForConfig4').value) &&
			(this.addPackage.get('config4').value.toLowerCase() != "missing")) {
			this.addPackage.controls['config4_crc'].enable();
			this.validCRC4 = false;
		}
		else {
			this.addPackage.controls['config4_crc'].enable();
			this.validCRC4 = false;
		}
	}

	duplicatePackageName(controlName: string) {
		return (formGroup: FormGroup) => {
			const control = formGroup.controls[controlName];
			if (control.value && control.value.length > 2) {
				if (this.updateFlag && control.value === this.campaignService.packageDetail.package_name) {
					return;
				}
				this.campaignService.findByPackageName(control.value)
					.subscribe(data => {
						if (data.status) {
							control.setErrors({ duplicate: true });
						}
					});
			}
		}
	}

	setFormControlValues() {
		if (this.updateFlag) {

			this.packageObj = this.campaignService.packageDetail;
			this.addPackage.patchValue({ 'package_name': this.campaignService.packageDetail.package_name });
			this.addPackage.patchValue({ 'bin_version': this.campaignService.packageDetail.bin_version });
			this.addPackage.patchValue({ 'app_version': this.campaignService.packageDetail.app_version });

			this.addPackage.patchValue({ 'config1': this.campaignService.packageDetail.config1 });
			// this.addPackage.patchValue({ 'config2': this.campaignService.packageDetail.config2 });
			//this.addPackage.patchValue({ 'config3': this.campaignService.packageDetail.config3 });
			// this.addPackage.patchValue({ 'config4': this.campaignService.packageDetail.config4 });
			this.addPackage.patchValue({ 'config1_crc': this.campaignService.packageDetail.config1_crc });
			this.addPackage.patchValue({ 'config2_crc': this.campaignService.packageDetail.config2_crc });
			this.addPackage.patchValue({ 'config3_crc': this.campaignService.packageDetail.config3_crc });
			this.addPackage.patchValue({ 'config4_crc': this.campaignService.packageDetail.config4_crc });
			this.addPackage.patchValue({ 'is_used_in_campaign': this.campaignService.packageDetail.is_used_in_campaign });

			this.addPackage.patchValue({ 'device_type': this.campaignService.packageDetail.device_type });
			this.addPackage.patchValue({ 'lite_sentry_hardware': this.campaignService.packageDetail.lite_sentry_hardware });
			this.addPackage.patchValue({ 'lite_sentry_app': this.campaignService.packageDetail.lite_sentry_app });
			this.addPackage.patchValue({ 'lite_sentry_boot': this.campaignService.packageDetail.lite_sentry_boot });
			this.addPackage.patchValue({ 'microsp_app': this.campaignService.packageDetail.microsp_app });
			this.addPackage.patchValue({ 'microsp_mcu': this.campaignService.packageDetail.microsp_mcu });
			this.addPackage.patchValue({ 'cargo_maxbotix_hardware': this.campaignService.packageDetail.cargo_maxbotix_hardware });
			this.addPackage.patchValue({ 'cargo_maxbotix_firmware': this.campaignService.packageDetail.cargo_maxbotix_firmware });
			this.addPackage.patchValue({ 'cargo_riot_hardware': this.campaignService.packageDetail.cargo_riot_hardware });
			this.addPackage.patchValue({ 'cargo_riot_firmware': this.campaignService.packageDetail.cargo_riot_firmware });


			const ble_version_control = this.addPackage.get('ble_version');
			const mcu_version_control = this.addPackage.get('mcu_version');
			const config_2 = this.addPackage.get('config2');
			const config_3 = this.addPackage.get('config3');
			const config_4 = this.addPackage.get('config4');
			
			if (this.campaignService.packageDetail.ble_version == "ANY") {
				this.addPackage.patchValue({ 'ble_version': "" });
				this.addPackage.patchValue({ 'isIgnoreForBLE': true });
				ble_version_control.setValidators(null);
				ble_version_control.updateValueAndValidity();
			} else {
				this.addPackage.patchValue({ 'ble_version': this.campaignService.packageDetail.ble_version });
				this.addPackage.patchValue({ 'isIgnoreForBLE': false });
			}

			if (this.campaignService.packageDetail.mcu_version == "ANY") {
				this.addPackage.patchValue({ 'mcu_version': "" });
				this.addPackage.patchValue({ 'isIgnoreForMCU': true });
				mcu_version_control.setValidators(null);
				mcu_version_control.updateValueAndValidity();
			} else {
				this.addPackage.patchValue({ 'mcu_version': this.campaignService.packageDetail.mcu_version });
				this.addPackage.patchValue({ 'isIgnoreForMCU': false });
			}
			if (this.campaignService.packageDetail.config2 == "ANY") {
				this.addPackage.patchValue({ 'config2': "" });
				this.addPackage.patchValue({ 'isIgnoreForConfig2': true });
				config_2.setValidators(null);
				config_2.updateValueAndValidity();
			} else {
				this.addPackage.patchValue({ 'config2': this.campaignService.packageDetail.config2 });
				this.addPackage.patchValue({ 'isIgnoreForConfig2': false });
			}

			if (this.campaignService.packageDetail.config3 == "ANY") {
				this.addPackage.patchValue({ 'config3': "" });
				this.addPackage.patchValue({ 'isIgnoreForConfig3': true });
				config_3.setValidators(null);
				config_3.updateValueAndValidity();
			} else {
				this.addPackage.patchValue({ 'config3': this.campaignService.packageDetail.config3 });
				this.addPackage.patchValue({ 'isIgnoreForConfig3': false });
			}

			if (this.campaignService.packageDetail.config4 == "ANY") {
				this.addPackage.patchValue({ 'config4': "" });
				this.addPackage.patchValue({ 'isIgnoreForConfig4': true });
				config_4.setValidators(null);
				config_4.updateValueAndValidity();
			} else {
				this.addPackage.patchValue({ 'config4': this.campaignService.packageDetail.config4 });
				this.addPackage.patchValue({ 'isIgnoreForConfig4': false });
			}
			this.toggleCRC2();
			this.toggleCRC3();
			this.toggleCRC4();

		}
	}


	cancelModelOfAddValue() {
		$('#addValue1').modal('hide');
		this.lookupValueModel.patchValue("");
		$('#validateRequired').addClass("displayNameNode");
	}


	addValueOfLookup(valueName, lookupName) {
		this.lookupName = valueName;
		this.lookupValue = lookupName;
		$('#addValue1').modal('show');
	}

	addLookupValue() {
		var spaceValidate = this.lookupValueModel.value.trim();
		this.loading = true;
		if (this.lookupValueModel.value != undefined && this.lookupValueModel.value != null && this.lookupValueModel.value != "" && spaceValidate) {
			if (this.lookupValue != undefined && this.lookupValue != null) {
				let configLookupObj = {
					"field": this.lookupValue,
					"display_label": this.lookupValueModel.value,
					"value": this.lookupValueModel.value,
				}

				if (this.lookupValue == "BIN_VERSION" || this.lookupValue == "APP_VERSION" || this.lookupValue == "MCU_VERSION" || this.lookupValue == "BLE_VERSION") {
					configLookupObj.display_label = configLookupObj.display_label.toUpperCase();
					configLookupObj.value = configLookupObj.value.toUpperCase();
				}
				this.campaignService.addLookupValue(configLookupObj).subscribe(data => {
					//this.toastr.success(data.message);
					this.toastr.success(data.message, null, { toastComponent: CustomSuccessToastrComponent });
					this.loading = false;
					$('#addValue1').modal('hide');
					this.lookupValueModel.patchValue("");
					this.getLookupValuesByValue();
				}, error => {
					this.loading = false;
					$('#addValue1').modal('hide');
					this.lookupValueModel.patchValue("");
					console.log(error);
				});
			}
		} else {
			$('#validateRequired').removeClass("displayNameNode");
			this.loading = false;
		}
	}


	savePackage(addPackage) {
		this.isDirty = false;


		if (this.updateFlag) {

			this.submitted = true;
			this.packageObj.package_name = addPackage.controls.package_name.value;
			this.packageObj.bin_version = addPackage.controls.bin_version.value;


			if (addPackage.controls.isIgnoreForBLE.value) {
				this.packageObj.ble_version = "ANY";
			} else {
				this.packageObj.ble_version = addPackage.controls.ble_version.value;
			}

			if (addPackage.controls.isIgnoreForMCU.value) {
				this.packageObj.mcu_version = "ANY";
			} else {
				this.packageObj.mcu_version = addPackage.controls.mcu_version.value;
			}
			if (addPackage.controls.isIgnoreForConfig2.value) {
				this.packageObj.config2 = "ANY";
			} else {
				this.packageObj.config2 = addPackage.controls.config2.value;
			}
			if (addPackage.controls.isIgnoreForConfig3.value) {
				this.packageObj.config3 = "ANY";
			} else {
				this.packageObj.config3 = addPackage.controls.config3.value;
			}
			if (addPackage.controls.isIgnoreForConfig4.value) {
				this.packageObj.config4 = "ANY";
			} else {
				this.packageObj.config4 = addPackage.controls.config4.value;
			}

			this.packageObj.app_version = addPackage.controls.app_version.value;
			this.packageObj.config1 = addPackage.controls.config1.value;
			//this.packageObj.config2 = addPackage.controls.config2.value;
			//this.packageObj.config3 = addPackage.controls.config3.value;
			//this.packageObj.config4 = addPackage.controls.config4.value;
			this.packageObj.config1_crc = addPackage.controls.config1_crc.value;
			this.packageObj.config2_crc = addPackage.controls.config2_crc.value;
			this.packageObj.config3_crc = addPackage.controls.config3_crc.value;
			this.packageObj.config4_crc = addPackage.controls.config4_crc.value;


			this.packageObj.device_type = addPackage.controls.device_type.value;
			this.packageObj.lite_sentry_hardware = addPackage.controls.lite_sentry_hardware.value;
			this.packageObj.lite_sentry_app = addPackage.controls.lite_sentry_app.value;
			this.packageObj.lite_sentry_boot = addPackage.controls.lite_sentry_boot.value;
			this.packageObj.microsp_app = addPackage.controls.microsp_app.value;
			this.packageObj.microsp_mcu = addPackage.controls.microsp_mcu.value;
			this.packageObj.cargo_maxbotix_hardware = addPackage.controls.cargo_maxbotix_hardware.value;
			this.packageObj.cargo_maxbotix_firmware = addPackage.controls.cargo_maxbotix_firmware.value;
			this.packageObj.cargo_riot_hardware = addPackage.controls.cargo_riot_hardware.value;
			this.packageObj.cargo_riot_firmware = addPackage.controls.cargo_riot_firmware.value;

			// this.toggleCRC2();
			// this.toggleCRC3();
			// this.toggleCRC4();
			this.loading = true;
			console.log(this.packageObj);
			this.campaignService.updatePackage(this.packageObj).subscribe(data => {
				//this.toastr.success(data.message);
				this.toastr.success(data.message, null, { toastComponent: CustomSuccessToastrComponent });
				this.loading = false;
				this.campaignService.packageDetail = undefined;
				this.addPackage.reset();
				this.router.navigate(['packages']);

			}, error => {
				this.cancel();
				console.log(error);
			});
		} else {
			this.packageList = [];
			this.submitted = true;
			const packageToSave = new Package();
			packageToSave.package_name = addPackage.controls.package_name.value;
			packageToSave.bin_version = addPackage.controls.bin_version.value;


			if (addPackage.controls.isIgnoreForBLE.value) {
				packageToSave.ble_version = "ANY";
			} else {
				packageToSave.ble_version = addPackage.controls.ble_version.value;
			}

			if (addPackage.controls.isIgnoreForMCU.value) {
				packageToSave.mcu_version = "ANY";
			} else {
				packageToSave.mcu_version = addPackage.controls.mcu_version.value;
			}

			if (addPackage.controls.isIgnoreForConfig2.value) {
				packageToSave.config2 = "ANY";
			} else {
				packageToSave.config2 = addPackage.controls.config2.value;
			}
			if (addPackage.controls.isIgnoreForConfig3.value) {
				packageToSave.config3 = "ANY";
			} else {
				packageToSave.config3 = addPackage.controls.config3.value;
			}
			if (addPackage.controls.isIgnoreForConfig4.value) {
				packageToSave.config4 = "ANY";
			} else {
				packageToSave.config4 = addPackage.controls.config4.value;
			}

			packageToSave.app_version = addPackage.controls.app_version.value;
			packageToSave.config1 = addPackage.controls.config1.value;
			//  packageToSave.config2 = addPackage.controls.config2.value;
			//  packageToSave.config3 = addPackage.controls.config3.value;
			// packageToSave.config4 = addPackage.controls.config4.value;
			packageToSave.config1_crc = addPackage.controls.config1_crc.value;
			packageToSave.config2_crc = addPackage.controls.config2_crc.value;
			packageToSave.config3_crc = addPackage.controls.config3_crc.value;
			packageToSave.config4_crc = addPackage.controls.config4_crc.value;

			packageToSave.device_type = addPackage.controls.device_type.value;
			packageToSave.lite_sentry_hardware = addPackage.controls.lite_sentry_hardware.value;
			packageToSave.lite_sentry_app = addPackage.controls.lite_sentry_app.value;
			packageToSave.lite_sentry_boot = addPackage.controls.lite_sentry_boot.value;
			packageToSave.microsp_app = addPackage.controls.microsp_app.value;
			packageToSave.microsp_mcu = addPackage.controls.microsp_mcu.value;
			packageToSave.cargo_maxbotix_hardware = addPackage.controls.cargo_maxbotix_hardware.value;
			packageToSave.cargo_maxbotix_firmware = addPackage.controls.cargo_maxbotix_firmware.value;
			packageToSave.cargo_riot_hardware = addPackage.controls.cargo_riot_hardware.value;
			packageToSave.cargo_riot_firmware = addPackage.controls.cargo_riot_firmware.value;
			this.packageList.push(packageToSave);
			this.campaignService.addPackage(this.packageList).subscribe(data => {
				//this.toastr.success(data.message);
				this.toastr.success(data.message, null, { toastComponent: CustomSuccessToastrComponent });
				this.campaignService.packageDetail = undefined;
				this.addPackage.reset();
				this.router.navigate(['packages']);

			}, error => {
				this.cancel();
				console.log(error);
			});
		}

	}

	getLookupValues() {
		this.loading = true;
		this.campaignService.getLookupValues(this.lookupValues).subscribe(data => {
			this.binVersionList = data.body.BIN_VERSION;
			this.appVersionList = data.body.APP_VERSION;
			this.mcuVersionList = data.body.MCU_VERSION;
			this.bleVersionList = data.body.BLE_VERSION;
			this.config1List = data.body.CONFIG1;
			this.config2List = data.body.CONFIG2;
			this.config3List = data.body.CONFIG3;
			this.config4List = data.body.CONFIG4;

			this.liteSentryHardwareList = data.body.LITE_SENTRY_HARDWARE;
			this.liteSentryAppList = data.body.LITE_SENTRY_APP;
			this.liteSentryBootList = data.body.LITE_SENTRY_BOOT;
			this.microspAppList = data.body.MICROSP_APP;
			this.microspMcuList = data.body.MICROSP_MCU;
			this.cargoMaxbotixHardwareList = data.body.CARGO_MAXBOTIX_HARDWARE;
			this.cargoMaxbotixFirmwareList = data.body.CARGO_MAXBOTIX_FIRMWARE;
			this.cargoRiotHardwareList = data.body.CARGO_RIOT_HARDWARE;
			this.cargoRiotFirmwareList = data.body.CARGO_RIOT_FIRMWARE;
			this.deviceTypeList = data.body.DEVICE_TYPE;

			if (this.isAnyFoundInList(this.mcuVersionList)) {
				let obj = { "id": 999, "field": "MCU_VERSION", "value": "ANY", "display_label": "ANY" }
				this.mcuVersionList.unshift(obj);
			}
			this.bleVersionList = data.body.BLE_VERSION;
			if (this.isAnyFoundInList(this.bleVersionList)) {
				let obj = { "id": 999, "field": "BLE_VERSION", "value": "ANY", "display_label": "ANY" }
				this.bleVersionList.unshift(obj);
			}
			if (this.isAnyFoundInList(this.config2List) && this.config2List != undefined) {
				let obj = { "id": 999, "field": "CONFIG2", "value": "ANY", "display_label": "ANY" }
				this.config2List.unshift(obj);
			}
			if (this.isAnyFoundInList(this.config3List) && this.config3List != undefined) {
				let obj = { "id": 999, "field": "CONFIG3", "value": "ANY", "display_label": "ANY" }
				this.config3List.unshift(obj);
			}
			if (this.isAnyFoundInList(this.config4List) && this.config4List != undefined) {
				let obj = { "id": 999, "field": "CONFIG4", "value": "ANY", "display_label": "ANY" }
				this.config4List.unshift(obj);
			}
			this.loading = false;
		}, error => {
			console.log(error);
		});
	}

	isAnyFoundInList(list: any) {
		let isAnyFound = list != undefined ? list.find(o => (o.value === 'Any' || o.value === 'ANY')) : undefined;
		if (isAnyFound) {
			return false
		} return true
	}
	getLookupValuesByValue() {
		this.loading = true;
		let lookupList = [];
		lookupList.push(this.lookupValue);
		this.campaignService.getLookupValues(lookupList).subscribe(data => {
			if (this.lookupValue == "BIN_VERSION") {
				this.binVersionList = data.body.BIN_VERSION;
			} else if (this.lookupValue == "APP_VERSION") {
				this.appVersionList = data.body.APP_VERSION;
			} else if (this.lookupValue == "MCU_VERSION") {
				this.mcuVersionList = data.body.MCU_VERSION;
			} else if (this.lookupValue == "BLE_VERSION") {
				this.bleVersionList = data.body.BLE_VERSION;
			} else if (this.lookupValue == "CONFIG1") {
				this.config1List = data.body.CONFIG1;
			} else if (this.lookupValue == "CONFIG2") {
				this.config2List = data.body.CONFIG2;
			} else if (this.lookupValue == "CONFIG3") {
				this.config3List = data.body.CONFIG3;
			} else if (this.lookupValue == "CONFIG4") {
				this.config4List = data.body.CONFIG4;
			} else if (this.lookupValue == "LITE_SENTRY_HARDWARE") {
				this.liteSentryHardwareList = data.body.LITE_SENTRY_HARDWARE;
			} else if (this.lookupValue == "LITE_SENTRY_APP") {
				this.liteSentryAppList = data.body.LITE_SENTRY_APP;
			} else if (this.lookupValue == "LITE_SENTRY_BOOT") {
				this.liteSentryBootList = data.body.LITE_SENTRY_BOOT;
			} else if (this.lookupValue == "MICROSP_APP") {
				this.microspAppList = data.body.MICROSP_APP;
			} else if (this.lookupValue == "MICROSP_MCU") {
				this.microspMcuList = data.body.MICROSP_MCU;
			} else if (this.lookupValue == "CARGO_MAXBOTIX_HARDWARE") {
				this.cargoMaxbotixHardwareList = data.body.CARGO_MAXBOTIX_HARDWARE;
			} else if (this.lookupValue == "CARGO_MAXBOTIX_FIRMWARE") {
				this.cargoMaxbotixFirmwareList = data.body.CARGO_MAXBOTIX_FIRMWARE;
			} else if (this.lookupValue == "CARGO_RIOT_HARDWARE") {
				this.cargoRiotHardwareList = data.body.CARGO_RIOT_HARDWARE;
			} else if (this.lookupValue == "CARGO_RIOT_FIRMWARE") {
				this.cargoRiotFirmwareList = data.body.CARGO_RIOT_FIRMWARE;
			}
			else if (this.lookupValue == "DEVICE_TYPE") {
				this.deviceTypeList = data.body.DEVICE_TYPE;;
			}
			this.lookupValue = "";
			this.loading = false;
		}, error => {
			console.log(error);
			this.loading = false;
		});
	}

	cancel() {
		this.campaignService.packageDetail = undefined;
		this.addPackage.reset();
		this.router.navigate(['packages']);
	}

	deleteModal() { }

	deactivateDevice() { }

	deleteDevice() { }

	deletePopupCancel() { }

}