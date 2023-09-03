import { Component, OnInit, ViewChild } from '@angular/core';
import { Router } from '@angular/router';
import { PagerService } from 'src/app/util/pagerService';
import { ToastrService } from 'ngx-toastr';
import { PaginationConstants } from 'src/app/util/paginationConstants';
import { AgGridAngular } from '@ag-grid-community/angular';
import { ClientSideRowModelModule, ColumnsToolPanelModule, GridApi, GridOptions, IGetRowsParams, MenuModule, Module, MultiFilterModule, RowGroupingModule } from '@ag-grid-enterprise/all-modules';
import { FormControl } from '@angular/forms';
import { CustomDateComponent } from 'src/app/ui-elements/custom-date-component.component';
import { CustomSuccessToastrComponent } from 'src/app/custom-success-toastr/custom-success-toastr.component';
import { CustomErrorToastrComponent } from 'src/app/custom-error-toastr/custom-error-toastr.component';
import * as moment from 'moment';
import { CustomHeader } from 'src/app/ui-elements/custom-header.component';
import '@ag-grid-community/core/dist/styles/ag-grid.css';
import '@ag-grid-community/core/dist/styles/ag-theme-alpine.css';
declare var $: any;
import { SetFilterModule } from '@ag-grid-enterprise/set-filter';
import { DeviceReportsService } from './device-reports.service';
import { DevicesListService } from '../devices-list/devices-list.service';
import { BtnCellRendererForGatewayDetails } from '../devices-list/btn-cell-renderer.component';
import { any, filter } from 'underscore';
import { Filter } from 'src/app/models/Filter.model';

@Component({
  selector: 'app-device-reports',
  templateUrl: './device-reports.component.html',
  styleUrls: ['./device-reports.component.css']
})
export class DeviceReportComponent implements OnInit {
  sortFlag: boolean = false;
  currentPage: any;
  showingpage: number = 0;
  totalpage: string = "";
  gatewayList: any[] = [];
  totalRecords = 0;
  currentEndRecord = 0;
  currentStartRecord = 0;
  pagedItems!: any[];
  private allItems: any;
  total_number_of_records = 0;
  pager: any = {};
  sortByOfAssets = false;
  sortByCompany = false;
  sortCreatedOn = false;
  sortByCreatedBy = false;
  sortByLastUpdatedBy = false;
  sortByLastUpdateOn = false;
  pageSizeOptions: PaginationConstants = new PaginationConstants();
  pageSizeObjArr = this.pageSizeOptions.pageSizeObjArr;
  column = 'seq';
  order = 'asc';
  userCompanyType!: string | null;
  isSuperAdmin!: string | null;
  isRoleCustomerManager!: string | null;

  //parameters for counting records
  filterValues = new Map();
  filterEValues = new Map();
  filterValuesJsonObj: any = {};
  isClearButtonVisible = false;
  loading = false;
  frameworkComponents: any;
  public sideBar: any;
  elasticFilter: Filter = new Filter();


  // ag-grid code
  @ViewChild('myGrid')
  myGrid!: AgGridAngular;
  public gridOptions: any;
  public gridApi: any;
  public columnDefs: any;
  public gridColumnApi!: { setColumnState: (arg0: any) => void; getColumnState: () => any; };
  public cacheOverflowSize;
  public maxConcurrentDatasourceRequests;
  public infiniteInitialRowCount;
  public defaultColDef: any;
  public paramList: any;
  public components: any;
  rowData: any;
  elasticRowData: any[] = []
  public modules: Module[] = [
    ClientSideRowModelModule,
    MenuModule,
    ColumnsToolPanelModule,
    MultiFilterModule,
    RowGroupingModule,
  ];
  rowModelType = 'infinite';
  columnDefinationData = {
    "message": "Fetched Device report(s) Successfully",
    "status": true,
    "title": "null",
    "body": {
      "content": [
        {
          "json_object": {
            "report_header": {
              "device_id": "015115004226907",
              "sequence": 46511,
              "acknowledge": true,
              "event_id": 25,
              "rtcdate_time_info": {
                "year": 2021,
                "month": 12,
                "day": 20,
                "hour": 4,
                "minutes": 34,
                "seconds": 47
              }
            },
            "temperature": {
              "id": 4097,
              "status": "null",
              "received_time_stamp": "2021-12-20 04:34:47.000000000",
              "selector": "null",
              "name": "Internal Gateway Temperature Sensors",
              "display_name": "Temperature",
              "ambient_temperature": 22,
              "internal_temperature": 22
            },
            "general_mask_fields": {
              "id": 1,
              "status": "null",
              "received_time_stamp": "2021-12-20 04:34:47.000000000",
              "selector": "null",
              "name": "General Report Data ",
              "display_name": "General TLV",
              "gpsstatus_index": 0,
              "num_satellites": 1000000000,
              "hdop": 1000000000,
              "external_power_volts": 12.2,
              "internal_power_volts": 4.15,
              "odometer_kms": 0,
              "latitude": 33.637383,
              "longitude": -117.852942,
              "altitude_feet": 1000000000,
              "speed_kms": 0,
              "heading": 0,
              "rssi": 59,
              "gps_time": "2021-12-10 16:30:39.00000",
              "power": "external",
              "trip": "Off",
              "ignition": "Off",
              "motion": "Off",
              "shipping_mode": "Off",
              "vib_trip": "Off",
              "charger": "Off",
              "tamper": "Off",
              "odometer_field": "enabled",
              "status_field": "enabled",
              "ext_int_power": "enabled",
              "gps_field": "disabled",
              "altitude_field": "disabled",
              "speedheading_field": "enabled"
            },
            "voltage": {
              "id": 528,
              "status": "null",
              "received_time_stamp": "2021-12-20 04:34:47.000000000",
              "selector": 1,
              "name": "voltage",
              "display_name": "Voltage",
              "main_power": 12.24,
              "aux_power": 0.008,
              "charge_power": 0,
              "battery_power": 4.188
            },
            "abs": {
              "id": 1553,
              "status": "null",
              "received_time_stamp": "2021-09-03 10:51:44.000000000",
              "selector": "null",
              "name": "Alpha ABS",
              "display_name": "A ABS",
              "no_faults": true,
              "mis_wired": false,
              "active_faults_without_stored_faults": false,
              "no_active_faults_with_stored_faults": false,
              "active_and_stored_faults": false,
              "not_communicating": false,
              "not_powered_up": false,
              "num_active_faults": 0,
              "active_faults_codes": "null",
              "num_stored_faults": 0,
              "stored_faults_codes": ""
            },
            "alpha_atis": {
              "id": 1555,
              "status": "null",
              "received_time_stamp": "2021-09-03 10:51:44.000000000",
              "selector": 0,
              "name": "Alpha ATIS",
              "display_name": "A ATIS",
              "condition": "Off",
              "light_activated_num": 0,
              "light_activated_seconds": 0,
              "type": "PSI Indicator light"
            },
            "lite_sentry": {
              "id": 1552,
              "status": "null",
              "received_time_stamp": "2021-09-03 10:51:44.000000000",
              "selector": -128,
              "name": "Lite-Sentry Alpha",
              "display_name": "A LODC",
              "communicating": false,
              "mis_wired": false,
              "generic_fault": false,
              "marker_fault": false,
              "dead_right_marker_bulb": false,
              "dead_stop_bulb": false,
              "dead_left_marker_bulb": false,
              "dead_license_bulb": false,
              "light_sentry_error": "null"
            },
            "abs_odometer": {
              "id": 1554,
              "status": "null",
              "received_time_stamp": "2021-09-03 10:51:44.000000000",
              "selector": "null",
              "name": "A ABS Odometer",
              "display_name": "ABS Odometer",
              "odometer": 0
            },
            "abs_beta": {
              "id": 1569,
              "status": "Initializing",
              "received_time_stamp": "2021-09-03 10:51:44.000000000",
              "selector": 0,
              "name": "ABS DTC Beta",
              "display_name": "ABS Beta",
              "num_sensors": 0,
              "abs_voltage": 0,
              "reserved": "null",
              "abs_warning_lamp": 0,
              "abs_warning_lamp_status": "null",
              "num_of_faults": 0,
              "is_any_fault_active": false,
              "absbeta_measure": "null"
            },
            "chassis": {
              "id": 1088,
              "status": "",
              "received_time_stamp": "2021-09-03 10:51:44.000000000",
              "selector": "",
              "name": "Chassis",
              "display_name": "Chassis/Cargo Sensor",
              "condition": "Healthy",
              "cargo_state": "Unloaded",
              "dist_mm": -1,
              "age_seconds": 1
            },
            "tpms_alpha": {
              "id": 1556,
              "status": "Unresponsive",
              "received_time_stamp": "2021-09-03 10:51:44.000000000",
              "selector": -128,
              "name": "Alpha TPMS",
              "display_name": "A TPMS",
              "num_sensors": 1,
              "tpms_measures": [
                {
                  "axle_tireire_location_code": 40,
                  "tire_location_str": "RRO",
                  "axle_location": 2,
                  "location_code_hex_str": "28",
                  "tire_pressure_mbar": 0,
                  "tire_location": 2,
                  "tire_pressure_psi": 0,
                  "tire_temperature_c": -50
                }
              ]
            },
            "network_field": {
              "id": 17,
              "status": "null",
              "received_time_stamp": "2021-08-31 07:18:39.000000000",
              "selector": 127,
              "name": "Network",
              "display_name": "Network",
              "rssi": 70,
              "mask": 127,
              "roaming_index": 1,
              "service_type_index": 4,
              "mobile_country_code": 310,
              "mobile_network_code": 410,
              "tower_id": "0159000000f1",
              "tower_centroid_latitude": 0,
              "tower_centroid_longitude": 0,
              "cellular_band": "0203520000",
              "rx_tx_ec": "ffba00015047"
            },
            "ble_door_sensor": {
              "id": 1576,
              "status": "null",
              "received_time_stamp": "2021-08-31 07:18:39.000000000",
              "selector": 0,
              "name": "bleDoorSensor",
              "display_name": " BLE Door Sensor",
              "comm_status": "Online",
              "sensor_status": "Online",
              "door_state": "Open",
              "door_type": "Roll",
              "door_sequence": "Detected",
              "battery": "OK",
              "temperature_c": 23
            },
            "door_sensor": {
              "id": 1544,
              "status": "Healthy,",
              "received_time_stamp": "2021-08-31 07:18:39.000000000",
              "selector": "null",
              "name": "Alpha Door Sensor",
              "display_name": "A BLE_Door",
              "ble_rssi": 0,
              "open_count": 0,
              "hw_ver": "00",
              "sw_ver": "03",
              "last_valid_check": "Open"
            },
            "gpio": {
              "id": 80,
              "status": "null",
              "received_time_stamp": "2021-08-31 07:19:03.000000000",
              "selector": "null",
              "name": "GPIO status",
              "display_name": "GPIO status",
              "gpio_status": "0002",
              "gpio": [
                "output low",
                "output low",
                "output low",
                "output low",
                "output low",
                "output low",
                "output low",
                "input low"
              ]
            },
            "orientation_fields": {
              "id": 1090,
              "status": "null",
              "received_time_stamp": "2021-08-31 11:08:37.000000000",
              "selector": "null",
              "name": "Internal Gateway Accelerometer",
              "display_name": "Orientation Fields Sensor",
              "orientation_status_index": 1,
              "orientation_xaxis_milli_gs": 31,
              "orientation_yaxis_milli_gs": 953,
              "orientation_zaxis_milli_gs": -47
            },
            "waterfall": {
              "id": 259,
              "status": "null",
              "received_time_stamp": "2021-08-31 11:08:37.000000000",
              "selector": 1,
              "name": "waterfallInfo",
              "display_name": "Waterfall",
              "number_of_files": 4,
              "waterfall_info": [
                {
                  "config_id": 1,
                  "config_crc": "7132",
                  "config_identification_version": "TrailerNet_CutL_16"
                },
                {
                  "config_id": 2,
                  "config_crc": "66a9",
                  "config_identification_version": "Empty_01"
                },
                {
                  "config_id": 3,
                  "config_crc": "66a9",
                  "config_identification_version": "Empty_01"
                },
                {
                  "config_id": 4,
                  "config_crc": "3fcc",
                  "config_identification_version": "Jersey_Telecom_03"
                }
              ]
            },
            "software_version": {
              "id": 256,
              "status": "null",
              "received_time_stamp": "2021-08-31 11:08:37.000000000",
              "selector": 1,
              "name": "Software Version",
              "display_name": "Software Version",
              "hw_id_version": 16405,
              "os_version": "4.3.18_B0",
              "device_name": "cutlass-l",
              "app_version": "2.8.211_B1",
              "extender_version": "1.2.8_b0",
              "ble_version": "1.8.1_b1",
              "hw_version_revision": 3
            },
            "config_version": {
              "id": 258,
              "status": "null",
              "received_time_stamp": "2021-08-31 11:08:37.000000000",
              "selector": "null",
              "name": "Config Version",
              "display_name": "Config Version",
              "device_config": 32767,
              "configuration_desc": "TrailerNet_CutL_16"
            },
            "psi_air_supply": {
              "id": 1578,
              "status": "null",
              "received_time_stamp": "2021-08-09 07:53:38.000000000",
              "selector": 0,
              "name": "Beta PSI Air Supply Monitoring System",
              "display_name": "PSI Air Supply Monitor",
              "comm_status": "Online",
              "psi_air_supply_measure": [
                {
                  "tank_status": "Online",
                  "tank_battery": "Low",
                  "supply_status": "Online",
                  "supply_battery": "Low",
                  "tank_pressure": "8800 millibars",
                  "supply_pressure": "8000 millibars"
                }
              ]
            },
            "tpms_beta": {
              "id": 1568,
              "status": "Online",
              "received_time_stamp": "2021-08-09 07:53:38.000000000",
              "selector": 0,
              "name": "Beta TPMS",
              "display_name": "Beta TPMS",
              "num_sensors": 2,
              "tpms_beta_measure": [
                {
                  "primary_curb_status": "Online",
                  "inner_curb_status": "Online",
                  "inner_roadside_status": "Online",
                  "primary_roadside_status": "Online",
                  "primary_curbside_pressure": 8200,
                  "primary_curbside_pressure_psi": 118.93095,
                  "primary_curbside_temperature": 38,
                  "primary_roadside_pressure": 8900,
                  "primary_roadside_pressure_psi": 129.08359,
                  "primary_roadside_temperature": 36,
                  "inner_roadside_pressure": 8900,
                  "inner_roadside_pressure_psi": 129.08359,
                  "inner_roadside_temperature": 38,
                  "inner_curbside_pressure": 9100,
                  "inner_curbisde_pressure_psi": 131.98434,
                  "inner_curbside_temperature": 38
                },
                {
                  "primary_curb_status": "Online",
                  "inner_curb_status": "Online",
                  "inner_roadside_status": "Offline",
                  "primary_roadside_status": "Online",
                  "primary_curbside_pressure": 8900,
                  "primary_curbside_pressure_psi": 129.08359,
                  "primary_curbside_temperature": 39,
                  "primary_roadside_pressure": 9100,
                  "primary_roadside_pressure_psi": 131.98434,
                  "primary_roadside_temperature": 39,
                  "inner_roadside_pressure": 9999,
                  "inner_roadside_pressure_psi": 9999,
                  "inner_roadside_temperature": 9999,
                  "inner_curbside_pressure": 9000,
                  "inner_curbisde_pressure_psi": 130.53397,
                  "inner_curbside_temperature": 39
                }
              ]
            },
            "ble_temperature": {
              "id": 1579,
              "status": "null",
              "received_time_stamp": "2021-10-13 10:28:34.000000000",
              "selector": 0,
              "name": "Beta BLE Temperature Sensor",
              "display_name": " BLE Temperature Sensor",
              "comm_status": "Online",
              "temperature_elements": 1,
              "ble_temperature_sensormeasure": [
                {
                  "sensor_id": "0",
                  "size": 3,
                  "sensor_status": "Online",
                  "type": "Reading",
                  "battery": "OK",
                  "data_start": [
                    {
                      "first_age": "NA",
                      "period": "NA"
                    }
                  ],
                  "data_reading": [
                    {
                      "reading_quantity": "4",
                      "temperature_readings": [
                        "3C",
                        "3C",
                        "3C",
                        "4C"
                      ]
                    }
                  ]
                }
              ]
            },
            "field_vehicle_ecu": {
              "id": 1027,
              "status": "null",
              "received_time_stamp": "2021-08-31 07:18:25.000000000",
              "selector": 125,
              "name": "Field Vehicle ECU",
              "display_name": "Vehicle ECM",
              "fuel_level_percentage": 0,
              "odtsince_milkms": 0,
              "odtsince_milmiles": 0,
              "odtsince_dtcclear_kms": 2150,
              "odtsince_dtcclear_miles": 1335.9476
            },
            "field_vehicle_dtc": {
              "id": 1026,
              "status": "null",
              "received_time_stamp": "2021-08-31 07:18:25.000000000",
              "selector": 0,
              "name": "Vehicle DTC",
              "display_name": "Vehicle DTC",
              "num_diagnostics_codes": 0,
              "diagnostics_codes": "null",
              "diagnostics_light": 0
            },
            "field_vehicle_vin": {
              "id": 1025,
              "status": "null",
              "received_time_stamp": "2021-08-31 07:18:25.000000000",
              "selector": "null",
              "name": "Field Vehicle VIN",
              "display_name": "Vehicle VIN",
              "vin": "19XFC2F5XGE207173"
            },
            "beta_abs_id": {
              "id": 1572,
              "status": "Online",
              "received_time_stamp": "2021-10-12 07:49:22.000000000",
              "selector": 0,
              "name": "Beta ABS Identification",
              "display_name": "Beta ABS ID",
              "manufacturer_info": "Wabco",
              "serial_num": 0,
              "abs_fw_version": 0,
              "model": 0
            },
            "peripheral_version": {
              "id": 261,
              "status": "null",
              "received_time_stamp": "2021-06-12 00:15:04.000000000",
              "selector": 0,
              "name": "Peripheral Version",
              "display_name": "Peripheral Versions",
              "lite_sentry": {
                "type": "Lite Sentry",
                "sensor_status": "Online",
                "data_size": 11,
                "hardware_id": "0",
                "hardware_version": "0",
                "app_version": "1.4.0_b2",
                "boot_version": "0.0.0_B0"
              },
              "maxbotix_cargo_sensor": {
                "type": "Maxbotix Cargo Sensor",
                "sensor_status": "Online",
                "data_size": 3,
                "hardware_version": "A",
                "firmware_version": "2.5"
              },
              "ste_receiver": {
                "type": "STE TPMS Receiver",
                "sensor_status": "Online",
                "data_size": 6,
                "mcu_app": "2.0.1_b0",
                "zeeki_app": "0.0"
              },
              "riot_cargo_or_chassis_sensor": "null",
              "hegemon_absreader": "null",
              "abs_reader": "",
              "temperature_sensor": {
                "type": " BLE PCT Temp Sensor",
                "sensor_status": "Online",
                "data_size": 2,
                "data_elements": [
                  {
                    "location": 254,
                    "hardware_version": 1,
                    "firmware_version": 5
                  }
                ]
              }
            },
            "lamp_check_atis": {
              "type": "Lamp Check ATIS",
              "sensor_status": "Online",
              "data_size": 2,
              "data_elements": [
                {
                  "location": 0,
                  "hardware_version": 1,
                  "firmware_version": 1
                }
              ]
            },
            "skf_wheel_end": {
              "id": 1577,
              "status": "",
              "received_time_stamp": "2021-08-31 07:18:46.000000000",
              "selector": 0,
              "name": "Beta SKF Wheel End Monitoring System",
              "display_name": "SKF Wheel End Sensor",
              "comm_status": "Online",
              "sfk_wheel_end_measure": [
                {
                  "axle_location": "Axle 1",
                  "side": "Left Turn Signal",
                  "sensor_status": "Online",
                  "vibration": "OK",
                  "battery": "OK",
                  "low_temp": -25,
                  "high_temp": 58,
                  "current_temp": 23
                }
              ]
            },
            "peripheral": {
              "id": 257,
              "status": "",
              "received_time_stamp": "2021-09-01 10:52:14.000000000",
              "selector": 1,
              "name": "Peripheral",
              "display_name": "Periph Versions",
              "port_num": 0,
              "driver": 1,
              "port_type": 0,
              "descript": "SN:90fd9ffffe7005c0\u0000",
              "model": "HW:103-01 v2\u0000",
              "rev": "FW:1005 1.19.4 R\u0000"
            },
            "tftp_status": {
              "id": 512,
              "status": "",
              "received_time_stamp": "2021-09-01 10:52:14.000000000",
              "selector": 0,
              "name": "TFTP status",
              "display_name": "TFTP status",
              "tftp_status": " Successful"
            },
            "cargo_camera_sensor": {
              "id": 1582,
              "status": "",
              "received_time_stamp": "2021-10-18 21:36:22.000000000",
              "selector": 0,
              "name": "Camera Photo Status",
              "display_name": "Camera Photo Status",
              "upload_status": "Upload Success",
              "uri": "http://images.phillips-connect.net/or4t4qVI3R.jpg"
            },
            "psi_wheel_end": {
              "id": 1575,
              "status": "",
              "received_time_stamp": "2021-09-01 10:52:59.000000000",
              "selector": 0,
              "name": "Beta PSI Wheel End Monitoring System",
              "display_name": "PSI Wheel End Sensor",
              "comm_status": "Online",
              "axle_elements": 2,
              "psi_wheel_end_measure": [
                {
                  "left_status": "Online",
                  "left_battery": "Low",
                  "right_status": "Online",
                  "right_battery": "Low",
                  "left_temperature": "75",
                  "right_temperature": "74"
                },
                {
                  "left_status": "Online",
                  "left_battery": "Low",
                  "right_status": "Offline",
                  "right_battery": "Low",
                  "left_temperature": "78",
                  "right_temperature": "NA"
                }
              ]
            }
          },
          "parsed_object": {},
          "device_id": "015115006460447",
          "event_id": "Stopped",
          "gps_time": "null",
          "sequence_number": "40762",
          "raw_report": "7d0100151150042269079f3a29fa405c192c01174129e4726d075e8dd503b4495d3900003d538000000000e21009012fcb000800001056f0010400ec00ec"
        }
      ],
      "pageable": {
        "sort": {
          "sorted": true,
          "unsorted": false,
          "empty": false
        },
        "pageNumber": 0,
        "pageSize": 25,
        "offset": 0,
        "unpaged": false,
        "paged": true
      },
      "last": true,
      "totalPages": 1,
      "totalElements": 12,
      "first": true,
      "size": 25,
      "number": 0,
      "sort": {
        "sorted": true,
        "unsorted": false,
        "empty": false
      },
      "numberOfElements": 12,
      "empty": false
    },
    "total_key": 12,
    "current_page": 0,
    "total_pages": 0
  };


  //Pagination Save State
  isFirstTimeLoad = false;
  isPaginationStateRecovered = false;
  filterModelCountFilter: any;
  userNameList!: String[];
  companies!: any[];
  company: any;
  selectedCompany = new FormControl();
  selectDays = new FormControl(0);
  companyUuid = '1';
  companyname: any;
  days = 0;
  selectedtoDateRange: any[] = [];
  selected: number = 0;
  selectedIdsToDelete: string[] = [];
  isChecked: boolean = false;
  deviceList: any;
  pageLimit: any = 5;
  elasticModelFilter: any[] = [];



  constructor(public devicesListService: DevicesListService, public deviceReportsService: DeviceReportsService, public router: Router, private toastr: ToastrService, private pagerService: PagerService) {
    this.columnDefs = [
      {
        "headerName": "General Mask Fields",
        "children": [
          {
            "field": "general_mask_fields.id",
            "headerName": "Id",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "general_mask_fields.status",
            "headerName": "Status",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "general_mask_fields.received_time_stamp",
            "headerName": "Received Time Stamp",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "general_mask_fields.selector",
            "headerName": "Selector",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "general_mask_fields.name",
            "headerName": "Name",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "general_mask_fields.display_name",
            "headerName": "Display Name",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "general_mask_fields.gpsstatus_index",
            "headerName": "Gpsstatus Index",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "general_mask_fields.num_satellites",
            "headerName": "Num Satellites",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "general_mask_fields.hdop",
            "headerName": "Hdop",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "general_mask_fields.external_power_volts",
            "headerName": "External Power Volts",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "general_mask_fields.internal_power_volts",
            "headerName": "Internal Power Volts",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "general_mask_fields.odometer_kms",
            "headerName": "Odometer Kms",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "general_mask_fields.latitude",
            "headerName": "Latitude",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "general_mask_fields.longitude",
            "headerName": "Longitude",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "general_mask_fields.altitude_feet",
            "headerName": "Altitude Feet",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "general_mask_fields.speed_kms",
            "headerName": "Speed Kms",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "general_mask_fields.heading",
            "headerName": "Heading",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "general_mask_fields.rssi",
            "headerName": "Rssi",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "general_mask_fields.gps_time",
            "headerName": "Gps Time",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "general_mask_fields.power",
            "headerName": "Power",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "general_mask_fields.trip",
            "headerName": "Trip",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "general_mask_fields.ignition",
            "headerName": "Ignition",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "general_mask_fields.motion",
            "headerName": "Motion",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "general_mask_fields.shipping_mode",
            "headerName": "Shipping Mode",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "general_mask_fields.vib_trip",
            "headerName": "Vib Trip",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "general_mask_fields.charger",
            "headerName": "Charger",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "general_mask_fields.tamper",
            "headerName": "Tamper",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "general_mask_fields.odometer_field",
            "headerName": "Odometer Field",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "general_mask_fields.status_field",
            "headerName": "Status Field",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "general_mask_fields.ext_int_power",
            "headerName": "Ext Int Power",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "general_mask_fields.gps_field",
            "headerName": "Gps Field",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "general_mask_fields.altitude_field",
            "headerName": "Altitude Field",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "general_mask_fields.speedheading_field",
            "headerName": "Speedheading Field",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          }
        ]
      },
      {
        "headerName": "Voltage",
        "children": [
          {
            "field": "voltage.id",
            "headerName": "Id",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "voltage.status",
            "headerName": "Status",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "voltage.received_time_stamp",
            "headerName": "Received Time Stamp",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "voltage.selector",
            "headerName": "Selector",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "voltage.name",
            "headerName": "Name",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "voltage.display_name",
            "headerName": "Display Name",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "voltage.main_power",
            "headerName": "Main Power",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "voltage.aux_power",
            "headerName": "Aux Power",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "voltage.charge_power",
            "headerName": "Charge Power",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "voltage.battery_power",
            "headerName": "Battery Power",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          }
        ]
      },
      {
        "headerName": "Report Header",
        "children": [
          {
            "field": "report_header.device_id",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            },
            "headerName": "Device Id"
          },
          {
            "field": "report_header.sequence",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            },
            "headerName": "Sequence"
          },
          {
            "field": "report_header.acknowledge",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            },
            "headerName": "Acknowledge"
          },
          {
            "field": "report_header.event_id",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            },
            "headerName": "Event Id"
          },
          {
            "field": "report_header.rtcdate_time_info",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            },
            "headerName": "Rtcdate Time Info"
          },
          {
            "field": "report_header.rtcdate_time_info",
            "headerName": "RTC Date Time",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          }
        ]
      },
      {
        "headerName": "Temperature",
        "children": [
          {
            "field": "temperature.id",
            "headerName": "Id",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "temperature.status",
            "headerName": "Status",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "temperature.received_time_stamp",
            "headerName": "Received Time Stamp",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "temperature.selector",
            "headerName": "Selector",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "temperature.name",
            "headerName": "Name",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "temperature.display_name",
            "headerName": "Display Name",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "temperature.ambient_temperature",
            "headerName": "Ambient Temperature",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "temperature.internal_temperature",
            "headerName": "Internal Temperature",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          }
        ]
      },
      {
        "headerName": "ABS",
        "children": [
          {
            "field": "abs.id",
            "headerName": "Id",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "abs.status",
            "headerName": "Status",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "abs.received_time_stamp",
            "headerName": "Received Time Stamp",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "abs.selector",
            "headerName": "Selector",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "abs.name",
            "headerName": "Name",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "abs.display_name",
            "headerName": "Display Name",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "abs.no_faults",
            "headerName": "No Faults",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "abs.mis_wired",
            "headerName": "Mis Wired",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "abs.active_faults_without_stored_faults",
            "headerName": "Active Faults Without Stored Faults",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "abs.no_active_faults_with_stored_faults",
            "headerName": "No Active Faults With Stored Faults",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "abs.active_and_stored_faults",
            "headerName": "Active And Stored Faults",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "abs.not_communicating",
            "headerName": "Not Communicating",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "abs.not_powered_up",
            "headerName": "Not Powered Up",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "abs.num_active_faults",
            "headerName": "Num Active Faults",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "abs.active_faults_codes",
            "headerName": "Active Faults Codes",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "abs.num_stored_faults",
            "headerName": "Num Stored Faults",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "abs.stored_faults_codes",
            "headerName": "Stored Faults Codes",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          }
        ]
      },
      {
        "headerName": "Alpha Atis",
        "children": [
          {
            "field": "alpha_atis.id",
            "headerName": "Id",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "alpha_atis.status",
            "headerName": "Status",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "alpha_atis.received_time_stamp",
            "headerName": "Received Time Stamp",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "alpha_atis.selector",
            "headerName": "Selector",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "alpha_atis.name",
            "headerName": "Name",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "alpha_atis.display_name",
            "headerName": "Display Name",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "alpha_atis.condition",
            "headerName": "Condition",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "alpha_atis.light_activated_num",
            "headerName": "Light Activated Num",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "alpha_atis.light_activated_seconds",
            "headerName": "Light Activated Seconds",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "alpha_atis.type",
            "headerName": "Type",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          }
        ]
      },
      {
        "headerName": "Lite Sentry",
        "children": [
          {
            "field": "lite_sentry.id",
            "headerName": "Id",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "lite_sentry.status",
            "headerName": "Status",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "lite_sentry.received_time_stamp",
            "headerName": "Received Time Stamp",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "lite_sentry.selector",
            "headerName": "Selector",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "lite_sentry.name",
            "headerName": "Name",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "lite_sentry.display_name",
            "headerName": "Display Name",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "lite_sentry.communicating",
            "headerName": "Communicating",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "lite_sentry.mis_wired",
            "headerName": "Mis Wired",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "lite_sentry.generic_fault",
            "headerName": "Generic Fault",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "lite_sentry.marker_fault",
            "headerName": "Marker Fault",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "lite_sentry.dead_right_marker_bulb",
            "headerName": "Dead Right Marker Bulb",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "lite_sentry.dead_stop_bulb",
            "headerName": "Dead Stop Bulb",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "lite_sentry.dead_left_marker_bulb",
            "headerName": "Dead Left Marker Bulb",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "lite_sentry.dead_license_bulb",
            "headerName": "Dead License Bulb",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "lite_sentry.light_sentry_error",
            "headerName": "Light Sentry Error",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          }
        ]
      },
      {
        "headerName": "ABS Odometer",
        "children": [
          {
            "field": "abs_odometer.id",
            "headerName": "Id",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "abs_odometer.status",
            "headerName": "Status",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "abs_odometer.received_time_stamp",
            "headerName": "Received Time Stamp",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "abs_odometer.selector",
            "headerName": "Selector",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "abs_odometer.name",
            "headerName": "Name",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "abs_odometer.display_name",
            "headerName": "Display Name",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "abs_odometer.odometer",
            "headerName": "Odometer",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          }
        ]
      },
      {
        "headerName": "ABS Beta",
        "children": [
          {
            "field": "abs_beta.id",
            "headerName": "Id",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "abs_beta.status",
            "headerName": "Status",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "abs_beta.received_time_stamp",
            "headerName": "Received Time Stamp",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "abs_beta.selector",
            "headerName": "Selector",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "abs_beta.name",
            "headerName": "Name",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "abs_beta.display_name",
            "headerName": "Display Name",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "abs_beta.num_sensors",
            "headerName": "Num Sensors",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "abs_beta.abs_voltage",
            "headerName": "Abs Voltage",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "abs_beta.reserved",
            "headerName": "Reserved",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "abs_beta.abs_warning_lamp",
            "headerName": "Abs Warning Lamp",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "abs_beta.abs_warning_lamp_status",
            "headerName": "Abs Warning Lamp Status",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "abs_beta.num_of_faults",
            "headerName": "Num Of Faults",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "abs_beta.is_any_fault_active",
            "headerName": "Is Any Fault Active",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "abs_beta.absbeta_measure",
            "headerName": "Absbeta Measure",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          }
        ]
      },
      {
        "headerName": "Chassis",
        "children": [
          {
            "field": "chassis.id",
            "headerName": "Id",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "chassis.status",
            "headerName": "Status",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "chassis.received_time_stamp",
            "headerName": "Received Time Stamp",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "chassis.selector",
            "headerName": "Selector",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "chassis.name",
            "headerName": "Name",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "chassis.display_name",
            "headerName": "Display Name",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "chassis.condition",
            "headerName": "Condition",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "chassis.cargo_state",
            "headerName": "Cargo State",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "chassis.dist_mm",
            "headerName": "Dist Mm",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "chassis.age_seconds",
            "headerName": "Age Seconds",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          }
        ]
      },
      {
        "headerName": "TPMS Alpha",
        "children": [
          {
            "field": "tpms_alpha.id",
            "headerName": "Id",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "tpms_alpha.status",
            "headerName": "Status",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "tpms_alpha.received_time_stamp",
            "headerName": "Received Time Stamp",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "tpms_alpha.selector",
            "headerName": "Selector",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "tpms_alpha.name",
            "headerName": "Name",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "tpms_alpha.display_name",
            "headerName": "Display Name",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "tpms_alpha.num_sensors",
            "headerName": "Num Sensors",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "tpms_alpha.tpms_measures",
            "headerName": "Tpms Measures",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "tpms_alpha.tpms_measures",
            "headerName": "TPMS Measure",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          }
        ]
      },
      {
        "headerName": "Network Field",
        "children": [
          {
            "field": "network_field.id",
            "headerName": "Id",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "network_field.status",
            "headerName": "Status",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "network_field.received_time_stamp",
            "headerName": "Received Time Stamp",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "network_field.selector",
            "headerName": "Selector",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "network_field.name",
            "headerName": "Name",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "network_field.display_name",
            "headerName": "Display Name",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "network_field.rssi",
            "headerName": "Rssi",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "network_field.mask",
            "headerName": "Mask",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "network_field.roaming_index",
            "headerName": "Roaming Index",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "network_field.service_type_index",
            "headerName": "Service Type Index",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "network_field.mobile_country_code",
            "headerName": "Mobile Country Code",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "network_field.mobile_network_code",
            "headerName": "Mobile Network Code",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "network_field.tower_id",
            "headerName": "Tower Id",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "network_field.tower_centroid_latitude",
            "headerName": "Tower Centroid Latitude",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "network_field.tower_centroid_longitude",
            "headerName": "Tower Centroid Longitude",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "network_field.cellular_band",
            "headerName": "Cellular Band",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "network_field.rx_tx_ec",
            "headerName": "Rx Tx Ec",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          }
        ]
      },
      {
        "headerName": "BLE Door Sensor",
        "children": [
          {
            "field": "ble_door_sensor.id",
            "headerName": "Id",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "ble_door_sensor.status",
            "headerName": "Status",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "ble_door_sensor.received_time_stamp",
            "headerName": "Received Time Stamp",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "ble_door_sensor.selector",
            "headerName": "Selector",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "ble_door_sensor.name",
            "headerName": "Name",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "ble_door_sensor.display_name",
            "headerName": "Display Name",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "ble_door_sensor.comm_status",
            "headerName": "Comm Status",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "ble_door_sensor.sensor_status",
            "headerName": "Sensor Status",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "ble_door_sensor.door_state",
            "headerName": "Door State",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "ble_door_sensor.door_type",
            "headerName": "Door Type",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "ble_door_sensor.door_sequence",
            "headerName": "Door Sequence",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "ble_door_sensor.battery",
            "headerName": "Battery",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "ble_door_sensor.temperature_c",
            "headerName": "Temperature C",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          }
        ]
      },
      {
        "headerName": "Door Sensor",
        "children": [
          {
            "field": "door_sensor.id",
            "headerName": "Id",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "door_sensor.status",
            "headerName": "Status",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "door_sensor.received_time_stamp",
            "headerName": "Received Time Stamp",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "door_sensor.selector",
            "headerName": "Selector",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "door_sensor.name",
            "headerName": "Name",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "door_sensor.display_name",
            "headerName": "Display Name",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "door_sensor.ble_rssi",
            "headerName": "Ble Rssi",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "door_sensor.open_count",
            "headerName": "Open Count",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "door_sensor.hw_ver",
            "headerName": "Hw Ver",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "door_sensor.sw_ver",
            "headerName": "Sw Ver",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "door_sensor.last_valid_check",
            "headerName": "Last Valid Check",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          }
        ]
      },
      {
        "headerName": "GPIO",
        "children": [
          {
            "field": "gpio.id",
            "headerName": "Id",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "gpio.status",
            "headerName": "Status",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "gpio.received_time_stamp",
            "headerName": "Received Time Stamp",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "gpio.selector",
            "headerName": "Selector",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "gpio.name",
            "headerName": "Name",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "gpio.display_name",
            "headerName": "Display Name",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "gpio.gpio_status",
            "headerName": "Gpio Status",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "gpio.gpio",
            "headerName": "Gpio",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          }
        ]
      },
      {
        "headerName": "Orientation Fields",
        "children": [
          {
            "field": "orientation_fields.id",
            "headerName": "Id",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "orientation_fields.status",
            "headerName": "Status",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "orientation_fields.received_time_stamp",
            "headerName": "Received Time Stamp",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "orientation_fields.selector",
            "headerName": "Selector",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "orientation_fields.name",
            "headerName": "Name",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "orientation_fields.display_name",
            "headerName": "Display Name",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "orientation_fields.orientation_status_index",
            "headerName": "Orientation Status Index",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "orientation_fields.orientation_xaxis_milli_gs",
            "headerName": "Orientation Xaxis Milli Gs",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "orientation_fields.orientation_yaxis_milli_gs",
            "headerName": "Orientation Yaxis Milli Gs",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "orientation_fields.orientation_zaxis_milli_gs",
            "headerName": "Orientation Zaxis Milli Gs",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          }
        ]
      },
      {
        "headerName": "Waterfall",
        "children": [
          {
            "field": "waterfall.id",
            "headerName": "Id",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "waterfall.status",
            "headerName": "Status",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "waterfall.received_time_stamp",
            "headerName": "Received Time Stamp",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "waterfall.selector",
            "headerName": "Selector",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "waterfall.name",
            "headerName": "Name",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "waterfall.display_name",
            "headerName": "Display Name",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "waterfall.number_of_files",
            "headerName": "Number Of Files",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "waterfall.waterfall_info",
            "headerName": "Waterfall Info",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "waterfall.waterfall_info",
            "headerName": "Waterfall Info",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          }
        ]
      },
      {
        "headerName": "Software Version",
        "children": [
          {
            "field": "software_version.id",
            "headerName": "Id",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "software_version.status",
            "headerName": "Status",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "software_version.received_time_stamp",
            "headerName": "Received Time Stamp",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "software_version.selector",
            "headerName": "Selector",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "software_version.name",
            "headerName": "Name",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "software_version.display_name",
            "headerName": "Display Name",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "software_version.hw_id_version",
            "headerName": "Hw Id Version",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "software_version.os_version",
            "headerName": "Os Version",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "software_version.device_name",
            "headerName": "Device Name",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "software_version.app_version",
            "headerName": "App Version",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "software_version.extender_version",
            "headerName": "Extender Version",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "software_version.ble_version",
            "headerName": "Ble Version",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "software_version.hw_version_revision",
            "headerName": "Hw Version Revision",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          }
        ]
      },
      {
        "headerName": "Config  Version",
        "children": [
          {
            "field": "config_version.id",
            "headerName": "Id",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "config_version.status",
            "headerName": "Status",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "config_version.received_time_stamp",
            "headerName": "Received Time Stamp",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "config_version.selector",
            "headerName": "Selector",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "config_version.name",
            "headerName": "Name",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "config_version.display_name",
            "headerName": "Display Name",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "config_version.device_config",
            "headerName": "Device Config",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "config_version.configuration_desc",
            "headerName": "Configuration Desc",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          }
        ]
      },
      {
        "headerName": "PSI Air Supply",
        "children": [
          {
            "field": "psi_air_supply.id",
            "headerName": "Id",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "psi_air_supply.status",
            "headerName": "Status",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "psi_air_supply.received_time_stamp",
            "headerName": "Received Time Stamp",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "psi_air_supply.selector",
            "headerName": "Selector",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "psi_air_supply.name",
            "headerName": "Name",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "psi_air_supply.display_name",
            "headerName": "Display Name",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "psi_air_supply.comm_status",
            "headerName": "Comm Status",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "psi_air_supply.psi_air_supply_measure",
            "headerName": "Psi Air Supply Measure",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "psi_air_supply.psi_air_supply_measure",
            "headerName": "PSI Air Supply measure",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          }
        ]
      },
      {
        "headerName": "TPMS Beta",
        "children": [
          {
            "field": "tpms_beta.id",
            "headerName": "Id",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "tpms_beta.status",
            "headerName": "Status",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "tpms_beta.received_time_stamp",
            "headerName": "Received Time Stamp",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "tpms_beta.selector",
            "headerName": "Selector",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "tpms_beta.name",
            "headerName": "Name",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "tpms_beta.display_name",
            "headerName": "Display Name",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "tpms_beta.num_sensors",
            "headerName": "Num Sensors",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "tpms_beta.tpms_beta_measure",
            "headerName": "Tpms Beta Measure",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "tpms_beta.tpms_beta_measure",
            "headerName": "TPMS Beta Measure",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          }
        ]
      },
      {
        "headerName": "BLE Temperature",
        "children": [
          {
            "field": "ble_temperature.id",
            "headerName": "Id",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "ble_temperature.status",
            "headerName": "Status",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "ble_temperature.received_time_stamp",
            "headerName": "Received Time Stamp",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "ble_temperature.selector",
            "headerName": "Selector",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "ble_temperature.name",
            "headerName": "Name",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "ble_temperature.display_name",
            "headerName": "Display Name",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "ble_temperature.comm_status",
            "headerName": "Comm Status",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "ble_temperature.temperature_elements",
            "headerName": "Temperature Elements",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "ble_temperature.ble_temperature_sensormeasure",
            "headerName": "Ble Temperature Sensormeasure",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "ble_temperature.ble_temperature_sensormeasure",
            "headerName": "BLE Temp. Sensor Measure",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          }
        ]
      },
      {
        "headerName": "Field Vehicle ECU",
        "children": [
          {
            "field": "field_vehicle_ecu.id",
            "headerName": "Id",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "field_vehicle_ecu.status",
            "headerName": "Status",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "field_vehicle_ecu.received_time_stamp",
            "headerName": "Received Time Stamp",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "field_vehicle_ecu.selector",
            "headerName": "Selector",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "field_vehicle_ecu.name",
            "headerName": "Name",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "field_vehicle_ecu.display_name",
            "headerName": "Display Name",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "field_vehicle_ecu.fuel_level_percentage",
            "headerName": "Fuel Level Percentage",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "field_vehicle_ecu.odtsince_milkms",
            "headerName": "Odtsince Milkms",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "field_vehicle_ecu.odtsince_milmiles",
            "headerName": "Odtsince Milmiles",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "field_vehicle_ecu.odtsince_dtcclear_kms",
            "headerName": "Odtsince Dtcclear Kms",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "field_vehicle_ecu.odtsince_dtcclear_miles",
            "headerName": "Odtsince Dtcclear Miles",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          }
        ]
      },
      {
        "headerName": "Field Vehicle DTC",
        "children": [
          {
            "field": "field_vehicle_dtc.id",
            "headerName": "Id",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "field_vehicle_dtc.status",
            "headerName": "Status",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "field_vehicle_dtc.received_time_stamp",
            "headerName": "Received Time Stamp",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "field_vehicle_dtc.selector",
            "headerName": "Selector",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "field_vehicle_dtc.name",
            "headerName": "Name",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "field_vehicle_dtc.display_name",
            "headerName": "Display Name",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "field_vehicle_dtc.num_diagnostics_codes",
            "headerName": "Num Diagnostics Codes",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "field_vehicle_dtc.diagnostics_codes",
            "headerName": "Diagnostics Codes",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "field_vehicle_dtc.diagnostics_light",
            "headerName": "Diagnostics Light",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          }
        ]
      },
      {
        "headerName": "Field Vehicle VIN",
        "children": [
          {
            "field": "field_vehicle_vin.id",
            "headerName": "Id",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "field_vehicle_vin.status",
            "headerName": "Status",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "field_vehicle_vin.received_time_stamp",
            "headerName": "Received Time Stamp",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "field_vehicle_vin.selector",
            "headerName": "Selector",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "field_vehicle_vin.name",
            "headerName": "Name",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "field_vehicle_vin.display_name",
            "headerName": "Display Name",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "field_vehicle_vin.vin",
            "headerName": "Vin",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          }
        ]
      },
      {
        "headerName": "Beta ABS ID",
        "children": [
          {
            "field": "beta_abs_id.id",
            "headerName": "Id",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "beta_abs_id.status",
            "headerName": "Status",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "beta_abs_id.received_time_stamp",
            "headerName": "Received Time Stamp",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "beta_abs_id.selector",
            "headerName": "Selector",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "beta_abs_id.name",
            "headerName": "Name",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "beta_abs_id.display_name",
            "headerName": "Display Name",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "beta_abs_id.manufacturer_info",
            "headerName": "Manufacturer Info",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "beta_abs_id.serial_num",
            "headerName": "Serial Num",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "beta_abs_id.abs_fw_version",
            "headerName": "Abs Fw Version",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "beta_abs_id.model",
            "headerName": "Model",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          }
        ]
      },
      {
        "headerName": "Peripheral Version",
        "children": [
          {
            "field": "peripheral_version.Id",
            "headerName": "id",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "peripheral_version.Status",
            "headerName": "status",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "peripheral_version.Received Time Stamp",
            "headerName": "received_time_stamp",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "peripheral_version.Selector",
            "headerName": "selector",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "peripheral_version.Name",
            "headerName": "name",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "peripheral_version.Display Name",
            "headerName": "display_name",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "peripheral_version.Lite Sentry",
            "headerName": "lite_sentry",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "peripheral_version.Maxbotix Cargo Sensor",
            "headerName": "maxbotix_cargo_sensor",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "peripheral_version.Ste Receiver",
            "headerName": "ste_receiver",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "peripheral_version.Riot Cargo Or Chassis Sensor",
            "headerName": "riot_cargo_or_chassis_sensor",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "peripheral_version.Hegemon Absreader",
            "headerName": "hegemon_absreader",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "peripheral_version.Abs Reader",
            "headerName": "abs_reader",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "peripheral_version.Temperature Sensor",
            "headerName": "temperature_sensor",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          }
        ]
      },
      {
        "headerName": "SKF Wheel End",
        "children": [
          {
            "field": "skf_wheel_end.id",
            "headerName": "Id",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "skf_wheel_end.status",
            "headerName": "Status",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "skf_wheel_end.received_time_stamp",
            "headerName": "Received Time Stamp",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "skf_wheel_end.selector",
            "headerName": "Selector",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "skf_wheel_end.name",
            "headerName": "Name",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "skf_wheel_end.display_name",
            "headerName": "Display Name",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "skf_wheel_end.comm_status",
            "headerName": "Comm Status",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "skf_wheel_end.sfk_wheel_end_measure",
            "headerName": "Sfk Wheel End Measure",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "skf_wheel_end.sfk_wheel_end_measure",
            "headerName": "SFK Wheel End Measure",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          }
        ]
      },
      {
        "headerName": "Peripheral",
        "children": [
          {
            "field": "peripheral.id",
            "headerName": "Id",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "peripheral.status",
            "headerName": "Status",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "peripheral.received_time_stamp",
            "headerName": "Received Time Stamp",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "peripheral.selector",
            "headerName": "Selector",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "peripheral.name",
            "headerName": "Name",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "peripheral.display_name",
            "headerName": "Display Name",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "peripheral.port_num",
            "headerName": "Port Num",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "peripheral.driver",
            "headerName": "Driver",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "peripheral.port_type",
            "headerName": "Port Type",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "peripheral.descript",
            "headerName": "Descript",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "peripheral.model",
            "headerName": "Model",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "peripheral.rev",
            "headerName": "Rev",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          }
        ]
      },
      {
        "headerName": "TFTP Status",
        "children": [
          {
            "field": "tftp_status.id",
            "headerName": "Id",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "tftp_status.status",
            "headerName": "Status",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "tftp_status.received_time_stamp",
            "headerName": "Received Time Stamp",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "tftp_status.selector",
            "headerName": "Selector",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "tftp_status.name",
            "headerName": "Name",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "tftp_status.display_name",
            "headerName": "Display Name",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "tftp_status.tftp_status",
            "headerName": "Tftp Status",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          }
        ]
      },
      {
        "headerName": "Cargo Camera Sensor",
        "children": [
          {
            "field": "cargo_camera_sensor.id",
            "headerName": "Id",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "cargo_camera_sensor.status",
            "headerName": "Status",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "cargo_camera_sensor.received_time_stamp",
            "headerName": "Received Time Stamp",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "cargo_camera_sensor.selector",
            "headerName": "Selector",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "cargo_camera_sensor.name",
            "headerName": "Name",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "cargo_camera_sensor.display_name",
            "headerName": "Display Name",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "cargo_camera_sensor.upload_status",
            "headerName": "Upload Status",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "cargo_camera_sensor.uri",
            "headerName": "Uri",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          }
        ]
      },
      {
        "headerName": "PSI Wheel End",
        "children": [
          {
            "field": "psi_wheel_end.id",
            "headerName": "Id",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "psi_wheel_end.status",
            "headerName": "Status",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "psi_wheel_end.received_time_stamp",
            "headerName": "Received Time Stamp",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "psi_wheel_end.selector",
            "headerName": "Selector",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "psi_wheel_end.name",
            "headerName": "Name",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "psi_wheel_end.display_name",
            "headerName": "Display Name",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "psi_wheel_end.comm_status",
            "headerName": "Comm Status",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "psi_wheel_end.axle_elements",
            "headerName": "Axle Elements",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "psi_wheel_end.psi_wheel_end_measure",
            "headerName": "Psi Wheel End Measure",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          },
          {
            "field": "psi_wheel_end.psi_wheel_end_measure",
            "headerName": "PSI Wheel End Measure",
            "filter": "agTextColumnFilter",
            "sortable": true,
            "filterParams": {
              "filterOptions": [
                "contains",
                "equals"
              ],
              "suppressAndOrCondition": true
            }
          }
        ]
      },
      {
        "headerName": "Action",
        "field": "action",
        "sortable": false,
        "filter": "agNumberColumnFilter",
        "floatingFilter": true,
        "floatingFilterComponent": "customClearFloatingFilter",
        "floatingFilterComponentParams": {
          "suppressFilterButton": false
        },
        "cellRenderer": "btnCellRenderer",
        "width": 140,
        "minWidth": 140,
        "pinned": "right"
      }
    ];

    var filterParams1 = {
      filterOptions: [
        'equals', 'lessThan', 'greaterThan'
      ],
      defaultOption: 'lessThan',
      suppressAndOrCondition: true,
    };

    //this.columnDefs = createNormalColDefs();
    this.cacheOverflowSize = 2;
    this.maxConcurrentDatasourceRequests = 2;
    this.infiniteInitialRowCount = 2;
    if (sessionStorage.getItem('deviceReportsize') != undefined) {
      this.pageLimit = Number(sessionStorage.getItem('deviceReportsize'));
    } else {
      this.pageLimit = 5;
    }
    this.gridOptions = {
      cacheBlockSize: this.pageLimit,
      paginationPageSize: this.pageLimit,
      rowModelType: 'infinite',
    }
    this.frameworkComponents = {
      btnCellRenderer: BtnCellRendererForGatewayDetails,
      agDateInput: CustomDateComponent,
      agColumnHeader: CustomHeader,
    }


    this.defaultColDef = {
      flex: 1,
      minWidth: 100,
      sortable: true,
      floatingFilter: true,
      enablePivot: true,
      resizable: true,
      filter: true,
      wrapText: true,
      autoHeight: true,
      headerComponentParams: { menuIcon: 'fa-bars' },
    };
    this.components = {
      customClearFloatingFilter: this.getClearFilterFloatingComponent(),
    };
    this.sideBar = {
      toolPanels: [
        {
          id: 'columns',
          labelDefault: 'Columns',
          labelKey: 'columns',
          iconKey: 'columns',
          toolPanel: 'agColumnsToolPanel',
          toolPanelParams: {
            suppressRowGroups: true,
            suppressValues: true,
            suppressPivots: true,
            suppressPivotMode: true,
            suppressSideButtons: true,
            suppressColumnFilter: false,
            suppressColumnSelectAll: false,
            suppressColumnExpandAll: true
          },
        },
      ],
      defaultToolPanel: '',
    };

  }

  onBtNormalCols() {
    this.gridApi.setColumnDefs([]);
    // this.columnDefs = createNormalColDefs();
    this.gridApi.setColumnDefs(this.columnDefs);
    this.onColumnVisible(this.gridApi);
  }
  ngOnInit() {
    this.userCompanyType = sessionStorage.getItem('type');
    this.isSuperAdmin = sessionStorage.getItem('IS_ROLE_SUPERADMIN');
    this.userCompanyType = sessionStorage.getItem('type');
    this.isSuperAdmin = sessionStorage.getItem('IS_ROLE_SUPERADMIN');
    if (this.isSuperAdmin === 'true') {
      this.router.navigate(['device-list']);
    } else {
      this.router.navigate(['customerAssets']);
    }
    console.log("isSuperAdmin " + this.isSuperAdmin);
    this.isRoleCustomerManager = sessionStorage.getItem('IS_ROLE_CUSTOMER_MANAGER');
    console.log("isRoleCustomerManager " + this.isRoleCustomerManager);

    if (sessionStorage.getItem('deviceReportsize') != undefined) {
      this.pageLimit = Number(sessionStorage.getItem('deviceReportsize'));
    }
    else {
      this.pageLimit = 5;
    }
    this.sortFlag = false;
    console.log("deviceID")
    console.log(sessionStorage.getItem("deviceId"))
    this.deviceReportsService.deviceImeiList[0] = this.devicesListService.deviceId

  }

  onPageSizeChange() {
    sessionStorage.setItem('deviceReportsize', String(this.selected));
    var value = this.selected;
    this.pageLimit = (Number(value));
    this.gridApi.gridOptionsWrapper.setProperty('cacheBlockSize', value);
    const api: any = this.gridApi;
    api.rowModel.resetCache();
    this.gridApi.paginationSetPageSize(Number(value));
  }

  getSelectedRowData() {
    let selectedNodes = this.gridApi.getSelectedNodes();
    let selectedData = selectedNodes.map((node: { data: any; }) => node.data);
    return selectedData;
  }

  reloadComponent() {
    let currentUrl = this.router.url;
    this.router.navigateByUrl('/', { skipLocationChange: true }).then(() =>
      this.router.navigate([currentUrl]));
  }

  getSelctedRow(action: string) {

    this.gatewayList = [];
    this.selectedIdsToDelete = [];
    this.gatewayList = this.getSelectedRowData();
    this.devicesListService.deviceList = [];

    if (action == 'delete') {
      for (let i = 0; i < this.gatewayList.length; i++) {
        this.gatewayList[i].is_selected = true;
        if (this.gatewayList[i].status != "Pending") {
          this.toastr.error("The following records are part of an installation, and so they cannot be deleted.", "Problem with Deletion", { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
          return;
        } else {
          this.devicesListService.deviceList.push(this.deviceList[i]);
        }
      }
      // if (this.devicesListService.deviceList.length > 1) {
      //   $('#deleteGatewaysModal').modal('show');

      // } else if (this.devicesListService.deviceList.length == 1) {
      //   $('#deleteGatewayModal').modal('show');
      // }

      console.log(this.devicesListService.deviceList);
    } else if (action == 'update') {
      for (let i = 0; i < this.deviceList.length; i++) {
        this.deviceList[i].is_selected = true;
        if (this.deviceList[i].gateway_type != "Gateway") {
          this.toastr.error("The selected device(s) do not support sensors.", "Device Does Not Support Sensors ", { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
          return;
        } else {
          if (this.deviceList[i].status != "Pending") {
            this.toastr.error("The selected device(s) are part of an in-progress or active installation, and cannot be modified.", "Device Configuration Can’t Be Modified", { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
            return;
          } else {
            this.devicesListService.deviceList.push(this.deviceList[i]);
          }
        }
      }
      console.log(this.devicesListService.deviceList);
      this.router.navigate(['update-gateways']);
    }

  }
  onGridReady(params: any) {
    this.gridOptions.api.setDomLayout('autoHeight');
    this.paramList = params;
    console.log(this.paramList)
    this.gridApi = params.api;
    this.gridApi.setRowData([]);
    console.log(this.gridApi);
    this.gridColumnApi = params.columnApi;
    let gridSavedState = sessionStorage.getItem("device_report_grid_column_state");
    if (gridSavedState) {
      console.log(gridSavedState);
      console.log("column state found in session storage");
      //this.gridColumnApi.setColumnState(JSON.parse(gridSavedState));
      console.log("printing column state after setting");
      console.log(this.gridColumnApi.getColumnState());
    }
    let filterSavedState = sessionStorage.getItem("device_report_grid_filter_state");
    if (filterSavedState) {
      console.log(filterSavedState);
      console.log("filter state found in session storage");
      this.gridApi.setFilterModel(JSON.parse(filterSavedState));
      console.log("priting filter state after setting");
      console.log(this.gridApi.getFilterModel());
    } else {
      console.log("Filter state not found in session storage");
    }

    var datasource = {
      getRows: (params: IGetRowsParams) => {
        this.loading = true;
        //this.sortForAgGrid(params.sortModel);
        this.filterForAgGrid(params.filterModel);
        console.log(params.sortModel)
        console.log(params.filterModel)
        console.log(this.gridApi.paginationGetCurrentPage());
        // this.devicesListService.getDeviceReportsTest(this.gridApi.paginationGetCurrentPage() + 1, this.column, this.order, this.pageLimit,params.filterModel).subscribe(data => {
        this.deviceReportsService.getDeviceReportsElastic(this.gridApi.paginationGetCurrentPage() + 1, this.column, this.order, this.pageLimit, this.filterValuesJsonObj).subscribe(data => {
          this.loading = false;
          console.log("Call Starts API ")
          console.log("Data is ")
          console.log(data);
          this.total_number_of_records = data.totalHits.value;
          this.elasticRowData = []
          this.rowData = []
          if (data.hits != null && data.hits.length > 0) {
            for (var i = 0; i < data.hits.length; i++) {
              //const stringObject = JSON.parse(this.rowData[i].json_object);
              this.elasticRowData.push(data.hits[i].sourceAsMap)
            }
          }

          this.rowData = this.elasticRowData;
          console.log("Elastic Data")
          console.log(this.elasticRowData)

          console.log("====================  this.columnDefs =================");
          // /console.log(this.columnDefs);
          console.log(JSON.stringify(this.columnDefs));
          console.log("===================== ROW DATA : ===================");
          // this.gridOptions.api.setRowData(data.body.content);
          //  this.gridApi.setRowData(this.elasticRowData);
          // this.gridApi.setRowData([])
          //   this.gridApi.setRowData(this.rowData);
          console.log("CALL ends here ")
          params.successCallback(this.rowData, data.totalHits.value);
        },
          error => {
            console.log(error);
          })
      }
    }
    this.gridApi.setDatasource(datasource);
  }

  onDragStopped(params: any) {
    console.log("Column resize working");
    console.log(params);
    let columnState = this.gridColumnApi.getColumnState();
    this.saveGridStateInSession(columnState);
  }
  onColumnResized(params: any) {
    console.log(params);
    let columnState = this.gridColumnApi.getColumnState();
    this.saveGridStateInSession(columnState);
  }
  onSortChanged(params: any) {
    console.log("Column Sort working");
    console.log(params);
    let columnState = this.gridColumnApi.getColumnState();
    this.saveGridStateInSession(columnState);
  }

  onFilterChanged(params: any) {
    console.log("Filter change working");
    let filterModel = params.api.getFilterModel();
    console.log(filterModel);
    sessionStorage.setItem("device_report_grid_filter_state", JSON.stringify(filterModel));
  }

  onColumnVisible(params: any) {
    console.log("Column Visible working");
    console.log(params);
    let columnState = this.gridColumnApi.getColumnState();
    this.saveGridStateInSession(columnState);
  }

  onColumnPinned(params: any) {
    console.log("Column Pinned working");
    console.log(params);
    let columnState = this.gridColumnApi.getColumnState();
    this.saveGridStateInSession(columnState);
  }

  onPaginationChanged(params: { api: { paginationGetCurrentPage: () => any; }; }) {
    this.isChecked = false;
    this.gridApi.forEachNode(function (node: { setSelected: (arg0: boolean) => void; }) {
      node.setSelected(false);
    });
    this.selectedIdsToDelete = [];
    if (this.isPaginationStateRecovered || params.api.paginationGetCurrentPage() > 0) {
      sessionStorage.setItem("device_report_grid_saved_page", params.api.paginationGetCurrentPage());
    }
  }

  onFirstDataRendered(_params: any) {
    console.log("First time data rendered");
    this.isFirstTimeLoad = true;
  }

  saveGridStateInSession(columnState: any) {
    sessionStorage.setItem("device_report_grid_column_state", JSON.stringify(columnState));
  }

  countRecords(data: any) {
    if (data.body.content == null || data.body.content.length === 0) {
      this.totalRecords = 0;
      this.currentStartRecord = 0;
      this.currentEndRecord = 0;
    } else {
      this.totalRecords = data.total_key;
      this.currentStartRecord = (this.pageLimit * (data.current_page)) + 1;
      if (this.pageLimit == data.body.content.length) {
        this.currentEndRecord = this.pageLimit * (data.current_page + 1);
      }
      else {
        this.currentEndRecord = this.currentStartRecord + data.body.content.length - 1;
      }
    }
  }

  setPage(page: number, body: any[], total_pages: any, pageItems: number | undefined) {
    // get pager object from service
    this.pager = this.pagerService.getPager(this.allItems, page, total_pages, pageItems);

    // get current page of items
    this.pagedItems = body;
  }

  filterForAgGrid(filterModel: any) {
    this.gridApi.paginationCurrentPage = 0;
    if (filterModel != undefined && filterModel != null) {
      this.filterValuesJsonObj = {};
      this.filterValues = new Map();
      const elasticModelFilters = JSON.parse(JSON.stringify(filterModel))
      filterModel = elasticModelFilters
      const Okeys = Object.keys(filterModel)
      for (var i = 0; i < Okeys.length; i++) {
        this.elasticFilter.key = Okeys[i];
        this.elasticFilter.value = filterModel[Okeys[i]].filter;
        this.elasticFilter.operator = "like";
        this.filterEValues.set("filterValues " + i, this.elasticFilter)
        this.elasticFilter = new Filter();
        console.log(this.filterEValues)
      }
      let jsonObject: any = {};
      this.filterEValues.forEach((filter, key) => {
        console.log("Filter is ")
        console.log(filter)
        console.log(key)
        jsonObject[key] = filter
      });
      this.filterValuesJsonObj = jsonObject;
    }
  }
  sortForAgGrid(sortModel: any) {

    this.gridApi.paginationCurrentPage = 0;
    if (sortModel[0] == undefined || sortModel[0] == null || sortModel[0].colId == undefined || sortModel[0].colId == null) {
      return;
    }
    this.sortFlag = true;
    this.column = sortModel[0].colId;
    this.order = sortModel[0].sort;
  }

  onCellClicked(params: any) {
    console.log("Inside Oncell clicked");
    if (params.column.colId == "imei") {
      this.devicesListService.deviceId = params.data.imei;
      if (this.devicesListService.deviceId != null) {
        console.log("Inside IMEI");
        this.router.navigate(['device-profile']);
      }
    }
  }

  getClearFilterFloatingComponent() {
    function ClearFloatingFilter() { }
    ClearFloatingFilter.prototype.init = function (params: any) {
      this.eGui = document.createElement('div');
      this.eGui.innerHTML =
        '<button class="btnClear primary_buttonClear" (click)="clearFilter()">Clear Filters</button>';
      this.currentValue = null;
      this.clearFilterButton = this.eGui.querySelector('button');
      var that = this;
      function onClearFilterButtonClicked(this: any) {
        this.column = 'imei';
        this.order = 'asc';
        params.api.setFilterModel(null);
        params.column.columnApi.applyColumnState({ defaultState: { sort: null } });
        params.column.columnApi.resetColumnState();
        params.api.onFilterChanged();
        sessionStorage.setItem("custom_device_report_grid_filter_state", "");

      }
      this.clearFilterButton.addEventListener('click', onClearFilterButtonClicked);
    };
    ClearFloatingFilter.prototype.onParentModelChanged = function (parentModel: { filter: string; }) {
      if (!parentModel) {
        this.eFilterInput.value = '';
        this.currentValue = null;
      } else {
        this.eFilterInput.value = parentModel.filter + '';
        this.currentValue = parentModel.filter;
      }
    };
    ClearFloatingFilter.prototype.getGui = function () {
      return this.eGui;
    };
    return ClearFloatingFilter;
  }

  onDateRangeSelect(event: { value: number; }) {
    this.days = event.value;
    sessionStorage.setItem("selectedDays", JSON.stringify(this.days));
    this.onGridReady(this.paramList);
  }

  headerHeightSetter(_event: any) {
    var padding = 20;
    var height = headerHeightGetter() + padding;
    this.gridApi.setHeaderHeight(height);
    this.gridApi.resetRowHeights();
  }
}

function headerHeightGetter() {
  var columnHeaderTexts = document.querySelectorAll('.ag-header-cell-text');
  var columnHeaderTextsArray: Element[] = [];
  columnHeaderTexts.forEach(node => columnHeaderTextsArray.push(node));
  var clientHeights = columnHeaderTextsArray.map(
    headerText => headerText.clientHeight
  );
  var tallestHeaderTextHeight = Math.max(...clientHeights);
  return tallestHeaderTextHeight;
}

var containsFilterParams = {
  filterOptions: [
    'contains', 'equals'
  ],
  suppressAndOrCondition: true
};

function headerDisplay(generalKey: string) {
  const snakeToCamelCase = (sentence: string) => sentence
    .split('_')
    .map((word) => {
      return firstUppercase(word);
    })
    .join(' ');

  const firstUppercase = (word: string) => word &&
    word.charAt(0).toUpperCase() + word.slice(1);
  const headerNames = snakeToCamelCase(generalKey);
  return headerNames;
}

