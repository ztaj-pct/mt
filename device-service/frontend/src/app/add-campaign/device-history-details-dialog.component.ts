import { Component, Inject, OnInit } from '@angular/core';
import { MatDialog, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material';
import { Subject, BehaviorSubject } from 'rxjs';
import { CampaignHyperlinkRenderer } from './campaign-hyperlink.component';
import { CustomSuccessToastrComponent } from '../custom-success-toastr/custom-success-toastr.component';
import { CustomErrorToastrComponent } from '../custom-error-toastr/custom-error-toastr.component';
import { Observable, interval, Subscription } from 'rxjs';
import { CustomBatchErrorToastrComponent } from '../custom-batch-error-toastr/custom-batch-error-toastr.component';
import { DirtyComponent } from '../util/dirty-component';
import { Device } from '../models/device.model'
import { FormBuilder, FormGroup, Validators, FormControl } from '@angular/forms';
import { CampaignService } from '../campaign/campaign.component.service';
import { Router, NavigationEnd } from "@angular/router";
import { ToastrService } from 'ngx-toastr';
import { AssetsService } from '../assets.service';
import { Campaign } from '../models/campaign.model';
import { Module, RowNode } from '@ag-grid-community/core';
import { GridOptions, IDatasource, IGetRowsParams } from '@ag-grid-community/all-modules';
import { RichSelectModule } from '@ag-grid-enterprise/rich-select';
import { MenuModule } from '@ag-grid-enterprise/menu';
import { ColumnsToolPanelModule } from '@ag-grid-enterprise/column-tool-panel';
import '@ag-grid-community/core/dist/styles/ag-grid.css';
import '@ag-grid-community/core/dist/styles/ag-theme-alpine.css';
import { ClientSideRowModelModule } from '@ag-grid-community/client-side-row-model';
import { MultiFilterModule } from '@ag-grid-enterprise/multi-filter';
import { SetFilterModule } from '@ag-grid-enterprise/set-filter';
import * as XLSX from 'xlsx';
import { HighchartsService } from './highcharts.service';
import * as moment from "moment";
@Component({
  selector: 'app-device-history-details-dialog',
  templateUrl: './device-history-details-dialog.component.html',
  styleUrls: ['./device-history-details-dialog.component.css']
})
export class DeviceHistoryDetailsDialogComponent {

  constructor(public dialogRef: MatDialogRef<DeviceHistoryDetailsDialogComponent>, @Inject(MAT_DIALOG_DATA) public data: any, public campaignService: CampaignService
    , @Inject(MAT_DIALOG_DATA) public deviceCampaignHistoryList: any, @Inject(MAT_DIALOG_DATA) public campaignInstalledDevice: any, @Inject(MAT_DIALOG_DATA) public latestMaintenanceReport: any, @Inject(MAT_DIALOG_DATA) public currentCampaignBaseline: any) {

      if (data.latestMaintenanceReport !== undefined && data.latestMaintenanceReport !== null) {
      this.data.data.device_report = data.latestMaintenanceReport;
      this.data.data.imei = data.latestMaintenanceReport.imei
    }

    if (data.campaginHistory !== undefined && data.campaginHistory !== null) {
      data.data.device_campaign_history = data.campaginHistory;
    }

    this.campaginHistoryData = data.data.device_campaign_history;

    if (data.currentCampaignBaseline !== undefined && data.currentCampaignBaseline !== null) {
      this.baselineData = data.currentCampaignBaseline;
    } else {
      if (campaignService.campaignDetail !== null && campaignService.campaignDetail !== undefined) {
        this.baselineData = campaignService.campaignDetail.version_migration_detail[0].from_package;
      }
    }
    if (data.campaginInstalledDevice !== undefined && data.campaginInstalledDevice !== null) {
      data.data.campaign_installed_device = data.campaginInstalledDevice;
    }
    this.columnDefs = [
      {
        headerName: 'Campaign', field: 'campaing_name'
        , suppressSizeToFit: false

      },
      {
        headerName: 'Campaign Status', field: 'campaing_status'
        , suppressSizeToFit: false
      },
      {
        headerName: 'Last Step', field: 'last_step',
        width: 110, suppressSizeToFit: false
      },
      {
        headerName: 'Last Step Status', field: 'last_step_status'
        , suppressSizeToFit: false
      },
      {
        headerName: 'Last Step time', field: 'last_step_time',
        width: 110, suppressSizeToFit: false,
        cellRenderer: (data) => {
          return data.value ? moment(data.value).format("MM-DD-YYYY HH:mm:ss"):'';
     }      
      },
      {
        headerName: 'BIN', field: 'baseband_sw_version'
      },
      {
        headerName: 'App', field: 'app_sw_version'
      },
      {
        headerName: 'BLE', field: 'ble_version'

      },
      {
        headerName: 'Config 1', field: 'config1_civ'
      },
      {
        headerName: 'Config 1 CRC', field: 'config1_crc'
      },
      {
        headerName: 'Config 2', field: 'config2_civ'
      },
      {
        headerName: 'Config 2 CRC', field: 'config2_crc'
      },
      {
        headerName: 'Config 3', field: 'config3_civ'
      },
      {
        headerName: 'Config 3 CRC', field: 'config3_crc'
      },
      {
        headerName: 'Config 4', field: 'config4_civ'
      },
      {
        headerName: 'Config 4 CRC', field: 'config4_crc'
      }
    ];
    
    this.defaultColDef = {
      width: 90,
      sortable: true,
      filter: true,
      resizable: true
    };
  }

  public modules: Module[] = [ClientSideRowModelModule, RichSelectModule,
    MenuModule,
    ColumnsToolPanelModule, MultiFilterModule,
    SetFilterModule];
  public columnDefs;
  public columnFirstDefs;
  public defaultColDef;
  public gridApi;
  public gridColumnApi;
  public loading;
  public rowData;
  public campaginHistoryData;
  public domLayout;
  public gridApiFirst;
  public gridColumnApiFirst;
  public deviceReport;
  public baselineData;

  onGridReady(params) {
    this.gridApi = params.api;
    this.gridColumnApi = params.columnApi;
    this.rowData = this.campaginHistoryData;

    // Following line to make the currently visible columns fit the screen  
    params.api.sizeColumnsToFit();

    // Following line dymanic set height to row on content
    params.api.resetRowHeights();
    this.domLayout = "autoHeight";
    setTimeout(() => this.autoSizeAll(false), 200);
    var datasource = {
    }
    this.gridApi.setRowData(this.rowData);
    //this.gridApi.setDatasource(datasource);
  }

  
  onNoClick(): void {
    this.dialogRef.close();
  }
  sizeToFit() {
    this.gridApi.sizeColumnsToFit();
  }

  autoSizeAll(skipHeader) {
    var allColumnIds = [];
    this.gridColumnApi.getAllColumns().forEach(function (column) {
      allColumnIds.push(column.colId);
    });
    this.gridColumnApi.autoSizeColumns(allColumnIds, false);
  }


}