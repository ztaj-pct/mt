import { AgGridAngular } from '@ag-grid-community/angular';
import { GridOptions, Module, ClientSideRowModelModule, MenuModule, MultiFilterModule, RowGroupingModule, IGetRowsParams } from '@ag-grid-enterprise/all-modules';
import { ColumnsToolPanelModule } from '@ag-grid-enterprise/column-tool-panel';
import { Component, OnInit, ViewChild } from '@angular/core';

@Component({
  selector: 'app-tlv-columndefs',
  templateUrl: './tlv-columndefs.component.html',
  styleUrls: ['./tlv-columndefs.component.css']
})
export class TlvColumndefsComponent implements OnInit {
  columnDefinationData = {
    "message": "Fetched Device report(s) Successfully",
    "status": true,
    "title": "null",
    "body": {
      "content": [
        {
          "json_object": {
            "general": {
              "server_ip": "10.192.10.52",
              "device_ip": "185.77.143.71",
              "device_port": 17006,
              "server_port": 15020,
              "rawreport": "s",
              "uuid": "d"
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
  // ag-grid code 
  @ViewChild('myGrid') myGrid: AgGridAngular;
  public gridOptions: Partial<GridOptions>;
  public gridApi;
  public columnDefs;
  public gridColumnApi;
  public cacheOverflowSize;
  public maxConcurrentDatasourceRequests;
  public infiniteInitialRowCount;
  public defaultColDef;
  public paramList;
  public components;
  rowData: any;
  public modules: Module[] = [
    ClientSideRowModelModule,
    MenuModule,
    ColumnsToolPanelModule,
    MultiFilterModule,
    RowGroupingModule,
  ];
  rowModelType = 'infinite';
  constructor() {
    var containsFilterParams = {
      filterOptions: [
        'contains',
      ],
      suppressAndOrCondition: true
    };
    this.gridOptions = {
      rowModelType: 'infinite',
    }
  }

  ngOnInit() {
  }

  onGridReady(params:any) {
    this.gridOptions.api.setDomLayout('autoHeight');
  //  const api: any = this.gridApi;
   this.paramList = params;
    console.log(this.paramList)
    this.gridApi = params.api;
    this.gridApi.setRowData([]);
    console.log(this.gridApi);
    this.gridColumnApi = params.columnApi;
    //const colDefs = this.gridOptions.api.getColumnDefs();
    const generalDefs = this.gridOptions.api.getColumnDefs();

    // const keys = Object.keys(this.columnDefinationData.body.content[0])
    // keys.forEach(key => {
    //   console.log("key  =====================> " + key);
    //   colDefs.push({ field: key })
    // });
   // console.log(colDefs);
    //console.log(colDefs);
    console.log(this.columnDefinationData.body.content.length);

    if (this.columnDefinationData.body.content != null && this.columnDefinationData.body.content.length > 0) {
      for (var i = 0; i < this.columnDefinationData.body.content.length; i++) {
        const stringObject = JSON.parse(JSON.stringify(this.columnDefinationData.body.content[i].json_object));
        this.columnDefinationData.body.content[i].parsed_object = stringObject;
        const JStringValues = Object.values(stringObject)
        console.log(stringObject)

        const generalKeys = Object.keys(stringObject.general)
        //  const voltageKeys = Object.keys(stringObject.voltage)
        //  const report_header = Object.keys(stringObject.report_header)
        //  const rtcdate_time_info =  Object.keys(stringObject.report_header.rtcdate_time_info)
        //  console.log(rtcdate_time_info)
        //  const temperature = Object.keys(stringObject.temperature)
        //  const abs = Object.keys(stringObject.abs)
        //  const alpha_atis = Object.keys(stringObject.alpha_atis)
        //  const lite_sentry = Object.keys(stringObject.lite_sentry)
        //  const abs_odometer = Object.keys(stringObject.abs_odometer)
        //  const abs_beta = Object.keys(stringObject.abs_beta)
        //  const chassis = Object.keys(stringObject.chassis)
        //  const tpms_alpha = Object.keys(stringObject.tpms_alpha)
        //  const network_field = Object.keys(stringObject.network_field)
        //  const ble_door_sensor = Object.keys(stringObject.ble_door_sensor)
        //  const door_sensor = Object.keys(stringObject.door_sensor)
        //  const gpio = Object.keys(stringObject.gpio)
        //  const orientation_fields = Object.keys(stringObject.orientation_fields)
        //  const waterfall = Object.keys(stringObject.waterfall)
        //  const software_version = Object.keys(stringObject.software_version)
        //  const config_version = Object.keys(stringObject.config_version)
        //  const psi_air_supply = Object.keys(stringObject.psi_air_supply)
        //  const tpms_beta = Object.keys(stringObject.tpms_beta)
        //  const ble_temperature = Object.keys(stringObject.ble_temperature)
        //  const ble_temperature_sensormeasure =  Array.from(Object.keys(stringObject.ble_temperature.ble_temperature_sensormeasure));
        //  console.log(ble_temperature_sensormeasure)
        //  const fv_ecu = Object.keys(stringObject.field_vehicle_ecu);
        //  const fv_dtc = Object.keys(stringObject.field_vehicle_dtc)
        //  const fv_vin = Object.keys(stringObject.field_vehicle_vin)
        //  const beta_abs = Object.keys(stringObject.beta_abs_id)
        //  const peripheral_version = Object.keys(stringObject.peripheral_version)
        //  const skf_wheel_end = Object.keys(stringObject.skf_wheel_end)
        //  const peripheral = Object.keys(stringObject.peripheral)
        //  const tftp_status = Object.keys(stringObject.tftp_status)
        //  const cargo_camera_sensor = Object.keys(stringObject.cargo_camera_sensor)
        //  const psi_wheel_end = Object.keys(stringObject.psi_wheel_end)


        console.log("=================== generalKeys ===================");
        console.log(generalKeys);

        //  console.log("=================== voltageKeys ===================");
        //  console.log(voltageKeys);
        generalKeys.forEach(generalKey => generalDefs.push({ field: "general." + generalKey, "headerName": headerDisplay(generalKey),sortable: true, filter: 'agTextColumnFilter', floatingFilter: true, filterParams: containsFilterParams  }));
        //  voltageKeys.forEach(voltageKey => voltageDefs.push({ field: "parsed_object.voltage." + voltageKey, "headerName": voltageKey }));
        //  report_header.forEach(report_hea => report_headerDefs.push({ field: "parsed_object.report_header." + report_hea, "headerName": report_hea }));
        //  rtcdate_time_info.forEach(generalKey => rtcColDefs.push({ field: "parsed_object.report_header.rtcdate_time_info[0]." + generalKey, "headerName": generalKey }));
        //  temperature.forEach(voltageKey => temperatureDefs.push({ field: "parsed_object.temperature." + voltageKey, "headerName": voltageKey }));
        //  abs.forEach(generalKey => absDefs.push({ field: "parsed_object.abs." + generalKey, "headerName": generalKey }));
        //  alpha_atis.forEach(voltageKey => alpha_atisDefs.push({ field: "parsed_object.alpha_atis." + voltageKey, "headerName": voltageKey }));
        //  lite_sentry.forEach(generalKey => lite_sentryDefs.push({ field: "parsed_object.lite_sentry." + generalKey, "headerName": generalKey }));
        //  abs_odometer.forEach(voltageKey => abs_odometerDefs.push({ field: "parsed_object.abs_odometer." + voltageKey, "headerName": voltageKey }));
        //  abs_beta.forEach(generalKey => abs_betaDefs.push({ field: "parsed_object.abs_beta." + generalKey, "headerName": generalKey }));
        //  chassis.forEach(voltageKey => chassisDefs.push({ field: "parsed_object.chassis." + voltageKey, "headerName": voltageKey }));
        //  tpms_alpha.forEach(generalKey => tpms_alphaDefs.push({ field: "parsed_object.tpms_alpha." + generalKey, "headerName": generalKey }));
        //  network_field.forEach(voltageKey => network_fieldDefs.push({ field: "parsed_object.network_field." + voltageKey, "headerName": voltageKey }));
        //  ble_door_sensor.forEach(generalKey => ble_door_sensorDefs.push({ field: "parsed_object.ble_door_sensor." + generalKey, "headerName": generalKey }));
        //  door_sensor.forEach(voltageKey => door_sensorDefs.push({ field: "parsed_object.door_sensor." + voltageKey, "headerName": voltageKey }));
        //  gpio.forEach(generalKey => gpioDefs.push({ field: "parsed_object.gpio." + generalKey, "headerName": generalKey }));
        //  orientation_fields.forEach(voltageKey => orientation_fieDefs.push({ field: "parsed_object.orientation_fields." + voltageKey, "headerName": voltageKey }));
        //  waterfall.forEach(generalKey => waterfallDefs.push({ field: "parsed_object.waterfall." + generalKey, "headerName": generalKey }));
        //  software_version.forEach(voltageKey => software_versioDefs.push({ field: "parsed_object.software_version." + voltageKey, "headerName": voltageKey }));
        //  config_version.forEach(generalKey => config_versionDefs.push({ field: "parsed_object.config_version." + generalKey, "headerName": generalKey }));
        //  psi_air_supply.forEach(voltageKey => psi_air_supplyDefs.push({ field: "parsed_object.psi_air_supply." + voltageKey, "headerName": voltageKey }));
        //  tpms_beta.forEach(generalKey => tpms_betaDefs.push({ field: "parsed_object.tpms_beta." + generalKey, "headerName": generalKey }));
        //  ble_temperature.forEach(voltageKey => ble_temperatureDefs.push({ field: "parsed_object.ble_temperature." + voltageKey, "headerName": voltageKey }));
        //  fv_ecu.forEach(generalKey => fv_ecuDefs.push({ field: "parsed_object.field_vehicle_ecu." + generalKey, "headerName": generalKey }));
        //  fv_dtc.forEach(voltageKey => fv_dtcDefs.push({ field: "parsed_object.field_vehicle_dtc." + voltageKey, "headerName": voltageKey }));
        //  fv_vin.forEach(generalKey => fv_vinDefs.push({ field: "parsed_object.field_vehicle_vin." + generalKey, "headerName": generalKey }));
        //  beta_abs.forEach(voltageKey => beta_absDefs.push({ field: "parsed_object.beta_abs_id." + voltageKey, "headerName": voltageKey }));
        //  peripheral_version.forEach(generalKey => peripheral_versionDefs.push({ field: "parsed_object.peripheral_version." + generalKey, "headerName": generalKey }));
        //  skf_wheel_end.forEach(voltageKey => skf_wheel_endDefs.push({ field: "parsed_object.skf_wheel_end." + voltageKey, "headerName": voltageKey }));
        //  peripheral.forEach(generalKey => peripheralDefs.push({ field: "parsed_object.peripheral." + generalKey, "headerName": generalKey }));
        //  tftp_status.forEach(voltageKey => tftp_statusDefs.push({ field: "parsed_object.tftp_status." + voltageKey, "headerName": voltageKey }));
        //  cargo_camera_sensor.forEach(generalKey => cargo_camera_sensorDefs.push({ field: "parsed_object.cargo_camera_sensor." + generalKey, "headerName": generalKey }));
        //  psi_wheel_end.forEach(voltageKey => psi_wheel_endDefs.push({ field: "parsed_object.psi_wheel_end." + voltageKey, "headerName": voltageKey }));
        //  ble_temperature_sensormeasure.forEach(generalKey => ble_temperature_sensormeasureDefs.push({ field: "parsed_object.ble_temperature.ble_temperature_sensormeasure[0]." + generalKey, "headerName": generalKey }));


        this.columnDefs =
          [
            { headerName: "General", children: generalDefs, },
            //  { headerName: "Voltage", children: voltageDefs },
            //  { headerName: "report_header", children: report_headerDefs,rtcColDefs },
            //  { headerName: "temperature", children: temperatureDefs },
            //  { headerName: "abs", children: absDefs },
            //  { headerName: "alpha_atis", children: alpha_atisDefs },
            //  { headerName: "lite_sentry", children: lite_sentryDefs },
            //  { headerName: "abs_odometer", children: abs_odometerDefs },
            //  { headerName: "abs_beta", children: abs_betaDefs },
            //  { headerName: "chassis", children: chassisDefs },
            //  { headerName: "tpms_alpha", children: tpms_alphaDefs },
            //  { headerName: "network_field", children: network_fieldDefs },
            //  { headerName: "ble_door_sensor", children: ble_door_sensorDefs },
            //  { headerName: "door_sensor", children: door_sensorDefs },
            //  { headerName: "gpio", children: gpioDefs },
            //  { headerName: "orientation_fields", children: orientation_fieDefs },
            //  { headerName: "waterfall", children: waterfallDefs },
            //  { headerName: "software_version", children: software_versioDefs },
            //  { headerName: "config_version", children: config_versionDefs },
            //  { headerName: "psi_air_supply", children: psi_air_supplyDefs },
            //  { headerName: "tpms_beta", children: tpms_betaDefs },
            //  { headerName: "ble_temperature", children: ble_temperatureDefs,ble_temperature_sensormeasureDefs },
            //  { headerName: "field_vehicle_ecu", children: fv_ecuDefs },
            //  { headerName: "field_vehicle_dtc", children: fv_dtcDefs },
            //  { headerName: "field_vehicle_vin", children: fv_vinDefs },
            //  { headerName: "beta_abs_id", children: beta_absDefs },
            //  { headerName: "peripheral_version", children: peripheral_versionDefs },
            //  { headerName: "skf_wheel_end", children: skf_wheel_endDefs },
            //  //{ headerName: "ble_temperature_sensormeasure", children: ble_temperature_sensormeasureDefs },
            //  { headerName: "peripheral", children: peripheralDefs },
            //  { headerName: "tftp_status", children: tftp_statusDefs },
            //  { headerName: "cargo_camera_sensor", children: cargo_camera_sensorDefs },
            //  { headerName: "psi_wheel_end", children: psi_wheel_endDefs },
            {
              headerName: 'Action', field: 'action', sortable: false, filter: 'agNumberColumnFilter', floatingFilter: true, floatingFilterComponent: 'customClearFloatingFilter',
              floatingFilterComponentParams: {
                suppressFilterButton: false
              },
              cellRenderer: 'btnCellRenderer', width: 140, minWidth: 140, pinned: 'right'
            }
          ];
      }
    }

    console.log("====================  this.columnDefs =================");
    console.log(this.columnDefs);
    console.log(JSON.stringify(this.columnDefs));
    console.log("===================== ROW DATA : ===================");
    // this.gridOptions.api.setRowData(data.body.content);
    //  this.gridApi.setRowData(this.elasticRowData);
    // this.gridApi.setRowData([])
    //   this.gridApi.setRowData(this.rowData);
    console.log("CALL ends here ")
    //params.successCallback(this.rowData, data.totalHits.value);
  }
}

var containsFilterParams = {
  filterOptions: [
    'contains',
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
