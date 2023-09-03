import { AfterViewInit, Component, OnInit, ViewChild } from '@angular/core';
import { AssetsService } from '../assets.service';
import { Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { PagerService } from '../util/pagerService';
import { CompaniesService } from '../companies.service';
import { InstallationService } from '../installation-summary-report/installation.service';
import { CustomSuccessToastrComponent } from '../custom-success-toastr/custom-success-toastr.component';
import { CustomErrorToastrComponent } from '../custom-error-toastr/custom-error-toastr.component';
import { ResetInstalationStatusService } from '../reset-installation-status/reset-installation-status.service';
import { AgGridAngular } from '@ag-grid-community/angular';
import { GridOptions } from '@ag-grid-community/core';
import { DatePipe } from '@angular/common';
import { MaintenanceService } from '../maintenance-summary-report/maintenance.service';

declare var $: any;


@Component({
  selector: 'installation-details',
  templateUrl: './installation-details.component.html',
  styleUrls: ['./installtion-details.component.css']

})
export class InstallationDetailsComponent implements OnInit {
  loading = false;
  assetData: any;
  installationdetails;
  voltage: string;
  isSuperAdmin;
  batteryList;
  loogedIssueList;
  sensorList;
  maintenaceList;
  userCompanyType;
  isRoleCustomerManager;
  // ag-grid code
  @ViewChild('myGrid') myGrid: AgGridAngular;
  public gridOptions: Partial<GridOptions>;
  public gridApi;
  public columnDefs;
  public gridColumnApi;
  rowModelType = 'infinite';
  rowData: any[];
  defaultColDef: any;
  rowSelection: string;
  isMaintenanceDetails = false;
  isDeviceDetails = true;
  maintenanceEventDetails: any;
  deviceHistoryData = [];
  paramList: any;
  uuidList: any;
  column = 'createdDate';
  order = 'asc';
  filterValues = new Map();
  filterValuesJsonObj: any = {};
  isFirstTimeLoad = false;
  isPaginationStateRecovered = false;
  pageLimit = 25;
  constructor(private maintenanceService: MaintenanceService, private installService: InstallationService, private assetsService: AssetsService,
    public router: Router, private toastr: ToastrService, private pagerService: PagerService, private companiesService: CompaniesService,
    private resetInstalationStatusservice: ResetInstalationStatusService, private datePipe: DatePipe) {
    this.userCompanyType = sessionStorage.getItem('type');
    this.isSuperAdmin = sessionStorage.getItem('IS_ROLE_SUPERADMIN');
    this.isRoleCustomerManager = sessionStorage.getItem('IS_ROLE_CUSTOMER_MANAGER');
    this.getDeviceHistory();
    const containsFilterParams = {
      filterOptions: [
        'contains',
      ],
      suppressAndOrCondition: true,
      buttons: ['reset'],
      clearButton: true
    };
    this.columnDefs = [
      {
        headerName: 'Sensor', field: 'sensorName', sortable: true, filter: 'agTextColumnFilter', floatingFilter: true,
        filterParams: containsFilterParams
      },
      {
        headerName: 'Old Sensor ID/MAC', field: 'oldSensorId', sortable: true, filter: 'agTextColumnFilter', floatingFilter: true,
        filterParams: containsFilterParams,
        cellRenderer: function (params) {
          if (params.data !== undefined && params.data.oldSensorId != null) {
            return '<div>' + params.data.oldSensorId + '</div>';
          } else if (params.data !== undefined && params.data.oldMacAddress != null) {
            return '<div>' + params.data.oldMacAddress + '</div>';
          }
        }
      },
      {
        headerName: 'New Sensor ID/MAC', field: 'newSensorId', sortable: true, filter: 'agTextColumnFilter', floatingFilter: true,
        filterParams: containsFilterParams,
        cellRenderer: function (params) {
          if (params.data !== undefined && params.data.newSensorId != null) {
            return '<div>' + params.data.newSensorId + '</div>';
          } else if (params.data !== undefined && params.data.macAddress != null) {
            return '<div>' + params.data.macAddress + '</div>';
          }
        }
      },
      {
        headerName: 'Position', field: 'position', sortable: true, filter: 'agTextColumnFilter', floatingFilter: true,
        filterParams: containsFilterParams
      },
      {
        headerName: 'Status', field: 'status', sortable: false, filter: false, floatingFilter: true,
        filterParams: containsFilterParams
      },
      {
        headerName: 'Reading', field: 'reading', sortable: false, filter: false, floatingFilter: true,
        filterParams: containsFilterParams
      },
      {
        headerName: 'Notes', field: 'notes', sortable: false, filter: false, floatingFilter: true,
        filterParams: containsFilterParams
      }

    ];
    this.defaultColDef = {
      flex: 1,
      minWidth: 100,
      sortable: true,
      floatingFilter: false,
      unSortIcon: true,
      enablePivot: true,
      resizable: false,
      filter: false,
      headerComponentParams: { menuIcon: 'fa-bars' },
    };
    this.rowSelection = 'single';
    this.gridOptions = {
      cacheBlockSize: this.pageLimit,
      paginationPageSize: this.pageLimit,
      rowModelType: 'infinite',
    };
  }

  showCustomUI = false;


  ngOnInit() {
    this.isSuperAdmin = sessionStorage.getItem('IS_ROLE_SUPERADMIN');
    this.isRoleCustomerManager = sessionStorage.getItem('IS_ROLE_CUSTOMER_MANAGER');
    if (this.isSuperAdmin) {
      // let deviceUUid =sessionStorage.getItem("install_code");
      // let deviceUUid =sessionStorage.getItem("device_uuid");
      // this.installService.getInstallationDetails(deviceUUid).subscribe(data => {
      //   if(data!=null){
      //     this.loading=false;
      //     this.installationdetails=data;
      //     this.loogedIssueList = data.logged_issues;

      //      if(data.sensor_details != undefined && data.sensor_details != null &&
      //       data.sensor_details.length > 2) {
      //         data.sensor_details =  data.sensor_details.sort((a, b) => a.sensor_name > b.sensor_name ? 1 : -1);
      //      }
      //      this.batteryList = data.battery_data;
      //      this.sensorList = data.sensor_details;
      //     this.assetData = data.asset_details_resposne
      //   }

      // }, error => {
      //   console.log(error)

      // });

    }

  }


  onDeviceHistoryClick(data) {
    switch (data.event_name) {
      case 'Maintenance':
        this.isDeviceDetails = false;
        this.isMaintenanceDetails = true;
        if (data.maintenanceUuid) {
          let uuidList: any[] = data.maintenanceUuid.split(',');
          if (uuidList != null && uuidList.length > 0) {
            if (uuidList[0].includes('[')) {
              let uuid = uuidList[0].split('[');
              if (uuid && uuid.length > 0) {
                uuidList[0] = uuid[1];
              }
            }
            if (uuidList[uuidList.length - 1].includes(']')) {
              let uuid = uuidList[uuidList.length - 1].split(']');
              if (uuid && uuid.length > 0) {
                uuidList[uuidList.length - 1] = uuid[0];
              }
            }
            // this.getMaintenanceDetail(uuidList);
            this.uuidList = uuidList;
            this.onGridReady(this.paramList);
          } else {
            this.uuidList = [];
            this.onGridReady(this.paramList);
          }
        } else {
          this.uuidList = [];
          this.onGridReady(this.paramList);
        }
        break;
      case 'Device Installed':
        this.isDeviceDetails = true;
        this.isMaintenanceDetails = false;
        this.showCustomUI = false;
        break;
      case 'QA':
        this.isDeviceDetails = true;
        this.isMaintenanceDetails = false;
        this.showCustomUI = true;
        break;
      default:
        break;
    }
  }


  onCellClicked(params) {
  }

  getMaintenanceDetail(uuidList: any) {
    if (!this.maintenanceEventDetails) {
      this.loading = true;
      this.installService.getMaintenanceDetailByUuidList(uuidList).subscribe(data => {
        this.loading = false;
        if (data && data.body && data.body.length > 0) {
          this.maintenanceEventDetails = data.body[0];
        }
        this.maintenaceList = data.body;
      }, error => {
      });
    }
  }

  getDeviceHistory() {
    this.loading = true;
    const deviceUUid = sessionStorage.getItem('device_uuid');
    this.installService.getInstallationDetails(deviceUUid).subscribe(data => {
      this.loading = false;
      if (data != null) {
        this.installationdetails = data;
        this.loogedIssueList = data.logged_issues;
        if (data.last_maintenance_report_date != null)
          data.last_maintenance_report_date = (this.datePipe.transform(data.last_maintenance_report_date, "yyyy-MM-dd hh:mm:ss") + " PST"); //output : 2018-02-13
        this.deviceHistoryData =
          [
            { event_name: 'Device Installed', event_date: data.installed_date },
            { event_name: 'QA', event_date: data.qa_date_and_time }
          ];
        if (data && data.finish_work_order && data.finish_work_order.length > 0) {
          for (const iterator of data.finish_work_order) {
            if (iterator.maintenance_uuid) {
              let obj = { event_name: 'Maintenance', event_date: iterator.end_date, maintenanceUuid: iterator.maintenance_uuid };
              this.deviceHistoryData.push(obj);
            }
          }
        }
        if (data.sensor_details != undefined && data.sensor_details != null &&
          data.sensor_details.length > 2) {
          data.sensor_details = data.sensor_details.sort((a, b) => a.sensor_name > b.sensor_name ? 1 : -1);
        }
        if (data.battery_data) {
          for (var i = 0; i < data.battery_data.length; i++) {
            this.voltage = data.battery_data[i].voltage;
            const voltageData = this.voltage.split('V');
            data.battery_data[i].voltage = (Number(voltageData[0]).toFixed(1)) + ' V';
          }
        }
        if (data.sensor_details && data.sensor_details.length > 2) {
          data.sensor_details = data.sensor_details.sort((a, b) => a.sensor_name > b.sensor_name ? 1 : -1);
        }
        if (data.battery_data) {
          for (const iterator of data.battery_data) {
            this.voltage = iterator.voltage;
            const voltageData = this.voltage.split('V');
            iterator.voltage = (Number(voltageData[0]).toFixed(1)) + ' V';
          }
        }
        this.batteryList = data.battery_data;
        this.sensorList = data.sensor_details;
        this.assetData = data.asset_details_resposne;
      }


    }, error => {
      this.loading = false;
      console.log(error);
    });
  }


  onGridReady(params) {
    this.paramList = params;
    this.gridApi = params.api;
    this.gridApi.setRowData([]);
    this.gridColumnApi = params.columnApi;
    this.gridApi.sizeColumnsToFit();
    const deviceUUid = sessionStorage.getItem('device_uuid');
    const datasource = {
      getRows: (params: any) => {
        this.sortForAgGrid(params.sortModel);
        this.filterForAgGrid(params.filterModel);
        this.loading = true;
        if (this.uuidList && this.uuidList.length > 0) {

          this.maintenanceService.getMaintenanceWithSort(this.gridApi.paginationGetCurrentPage() + 1, this.column, this.order, '', this.filterValuesJsonObj, this.pageLimit, 0, true).subscribe(data => {
            this.loading = false;
            this.rowData = data.body.content;
            if (data && data.body && data.body.content && data.body.content.length > 0) {
              this.maintenanceEventDetails = data.body.content[0];
            }

            params.successCallback(this.rowData, data.total_key);
            if (this.isFirstTimeLoad == true) {
              const paginationSavedPage = sessionStorage.getItem('maintenance_event_list_grid_saved_page');
              if (paginationSavedPage && this.isFirstTimeLoad) {
                this.gridOptions.api.paginationGoToPage(Number.parseInt(paginationSavedPage));
                this.isFirstTimeLoad = false;
                this.isPaginationStateRecovered = true;
              }
            }
          }, error => {
            this.loading = false;
            console.log(error);
          });
        } else {
          this.loading = false;
          this.rowData = [];
          params.successCallback(this.rowData, 0);
        }
        // this.loading = true;
        // this.installService.getMaintenanceDetailByUuidList(this.uuidList).subscribe(data => {
        //   this.loading = false;
        //   if (data && data.body && data.body.length > 0) {
        //     this.maintenanceEventDetails = data.body[0];
        //   }
        //   this.maintenaceList = data.body;
        //   this.rowData = data.body;
        //   params.successCallback(this.rowData, this.rowData.length);
        // }, error => {
        // });

      }
    };
    this.gridApi.setDatasource(datasource);
    this.autoSizeAll(false);
    setTimeout(() => {
      this.autoSizeAll(false);
    }, 4000);
  }

  filterForAgGrid(filterModel) {
    this.gridApi.paginationCurrentPage = 0;
    if (filterModel != undefined) {

      this.filterValuesJsonObj = {};
      this.filterValues = new Map();
      if (this.uuidList) {
        this.filterValues.set('uuidList', this.uuidList.toString());
      }
      if (filterModel.createdDate && filterModel.createdDate.dateFrom && filterModel.createdDate.dateFrom !== '') {
        this.filterValues.set('createdDate', filterModel.createdDate.dateFrom);
      }

      if (filterModel.serviceTime && filterModel.serviceTime.dateFrom && filterModel.serviceTime.dateFrom !== '') {
        this.filterValues.set('serviceTime', filterModel.serviceTime.dateFrom);
      }

      if (filterModel.assetId && filterModel.assetId.filter && filterModel.assetId.filter !== '') {
        this.filterValues.set('assetId', filterModel.assetId.filter);
      }

      if (filterModel.deviceId && filterModel.deviceId.filter && filterModel.deviceId.filter !== '') {
        this.filterValues.set('deviceId', filterModel.deviceId.filter);
      }

      if (filterModel.oldSensorId && filterModel.oldSensorId.filter && filterModel.oldSensorId.filter !== '') {
        this.filterValues.set('oldSensorId', filterModel.oldSensorId.filter);
      }

      if (filterModel.newSensorId != undefined && filterModel.newSensorId.filter != null && filterModel.newSensorId.filter != undefined && filterModel.newSensorId.filter != '') {
        this.filterValues.set('newSensorId', filterModel.newSensorId.filter);
      }

      if (filterModel.macAddress != undefined && filterModel.macAddress.filter != null && filterModel.macAddress.filter != undefined && filterModel.macAddress.filter != '') {
        this.filterValues.set('macAddress', filterModel.macAddress.filter);
      }

      if (filterModel.technician != undefined && filterModel.technician.filter != null && filterModel.technician.filter != undefined && filterModel.technician.filter != '') {
        this.filterValues.set('technician', filterModel.technician.filter);
      }

      if (filterModel.resolutionType != undefined && filterModel.resolutionType.filter != null && filterModel.resolutionType.filter != undefined && filterModel.resolutionType.filter != '') {
        this.filterValues.set('resolutionType', filterModel.resolutionType.filter);
      }

      if (filterModel.workOrder != undefined && filterModel.workOrder.filter != null && filterModel.workOrder.filter !== '') {
        this.filterValues.set('workOrder', filterModel.workOrder.filter);
      }

      if (filterModel.serviceVendorName != undefined && filterModel.serviceVendorName.filter && filterModel.serviceVendorName.filter !== '') {
        this.filterValues.set('serviceVendorName', filterModel.serviceVendorName.filter);
      }

      if (filterModel.validationTime != undefined && filterModel.validationTime.filter && filterModel.validationTime.filter !== '') {
        this.filterValues.set('validationTime', filterModel.validationTime.filter);
      }

      if (filterModel.sensorType != undefined && filterModel.sensorType.filter && filterModel.sensorType.filter !== '') {
        this.filterValues.set('sensorType', filterModel.sensorType.filter);
      }

      if (filterModel.maintenanceLocation && filterModel.maintenanceLocation.filter && filterModel.maintenanceLocation.filter !== '') {
        this.filterValues.set('maintenanceLocation', filterModel.maintenanceLocation.filter);
      }
      const jsonObject = {};
      this.filterValues.forEach((filter, key) => {
        jsonObject[key] = filter;
      });
      this.filterValuesJsonObj = jsonObject;
    }
  }


  sortForAgGrid(sortModel) {
    this.gridApi.paginationCurrentPage = 0;
    if (sortModel[0] == undefined || sortModel[0] == null || sortModel[0].colId == undefined || sortModel[0].colId == null) {
      return;
    }
    switch (sortModel[0].colId) {
      case 'createdDate': {
        this.column = 'createdDate';
        this.order = sortModel[0].sort;
        break;
      }
      case 'serviceTime': {
        this.column = 'serviceDateTime';
        this.order = sortModel[0].sort;
        break;
      }
      case 'assetId': {
        this.column = 'asset.assignedName';
        this.order = sortModel[0].sort;
        break;
      }
      case 'deviceId': {
        this.column = 'device.imei';
        this.order = sortModel[0].sort;
        break;
      }
      case 'sensorType': {
        this.column = 'sensorType';
        this.order = sortModel[0].sort;
        break;
      }
      case 'oldSensorId': {
        this.column = 'oldSensorId';
        this.order = sortModel[0].sort;
        break;
      }

      case 'newSensorId': {
        this.column = 'newSensorId';
        this.order = sortModel[0].sort;
        break;
      }

      case 'macAddress': {
        this.column = 'macAddress';
        this.order = sortModel[0].sort;
        break;
      }

      case 'technician': {
        this.column = 'user.firstName';
        this.order = sortModel[0].sort;
        break;
      }

      case 'serviceVendorName': {
        this.column = 'organisation.organisationName';
        this.order = sortModel[0].sort;
        break;
      }

      case 'validationTime': {
        this.column = 'validationTime';
        this.order = sortModel[0].sort;
        break;
      }
      case 'resolutionType': {
        this.column = 'resolutionType';
        this.order = sortModel[0].sort;
        break;
      }
      case 'workOrder': {
        this.column = 'workOrder';
        this.order = sortModel[0].sort;
        break;
      }
      case 'maintenanceLocation': {
        this.column = 'maintenanceLocation';
        this.order = sortModel[0].sort;
        break;
      }
      default: {
        console.log('default');
      }
    }
  }


  autoSizeAll(skipHeader: boolean) {
    // console.log("================= autoSizeAll ===================");
    // if (!columState && !filterSavedState) {
    const allColumnIds: string[] = [];
    this.gridColumnApi.getAllColumns().forEach((column) => {
      allColumnIds.push(column.getId());
    });
    this.gridColumnApi.autoSizeColumns(allColumnIds, skipHeader);
    // }
  }

  nDragStopped(params) {
    this.saveGridStateInSession(this.gridColumnApi.getColumnState());
  }

  onSortChanged(params) {
    this.saveGridStateInSession(this.gridColumnApi.getColumnState());
  }

  onFilterChanged(params) {
    sessionStorage.setItem('maintenance_event_list_grid_filter_state', JSON.stringify(params.api.getFilterModel()));
  }

  onColumnVisible(params) {
    this.saveGridStateInSession(this.gridColumnApi.getColumnState());
  }

  onColumnPinned(params) {
    this.saveGridStateInSession(this.gridColumnApi.getColumnState());
  }


  onColumnResized(params) {
    this.saveGridStateInSession(this.gridColumnApi.getColumnState());
  }

  onPaginationChanged(params) {

    // this.gridApi.forEachNode(function (node) {
    //   node.setSelected(false);
    // });
    if (this.isPaginationStateRecovered || params.api.paginationGetCurrentPage() > 0) {
      sessionStorage.setItem('maintenance_event_list_grid_saved_page', params.api.paginationGetCurrentPage());
    }
  }
  onFirstDataRendered(params) {
    this.isFirstTimeLoad = true;
  }


  saveGridStateInSession(columnState) {
    sessionStorage.setItem('maintenance_event_list_grid_column_state', JSON.stringify(columnState));
  }



  getAssetDetails() {
    if (this.userCompanyType === 'Manufacturer' || this.isSuperAdmin === 'true' || this.isRoleCustomerManager === 'true') {
      this.assetsService.editFlag = true;
      this.assetsService.routeflag = true;
      this.assetsService.asset = this.assetData;
      this.router.navigate(['add-assets']);
    }
  }


  resetGateway() {

    this.loading = true;
    this.resetInstalationStatusservice.resetInstalationStatus(null, this.installationdetails.device_id, this.installationdetails.asset_details_resposne.can).subscribe(data => {
      if (data.status) {
        this.toastr.success(data.message, null, { toastComponent: CustomSuccessToastrComponent });
        this.router.navigate(['installation-summary']);
      } else {
        this.toastr.error(data.message, null, { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
      }
      this.loading = false;
    }, error => {
      console.log(error);
      this.loading = false;
    }
    );
  }


  markInstallationForRework() {

    this.loading = true;
    this.resetInstalationStatusservice.markInstallationForRework(this.installationdetails.install_code).subscribe(data => {
      if (data.status) {
        this.toastr.success(data.message, null, { toastComponent: CustomSuccessToastrComponent });
        this.router.navigate(['installation-summary']);
      } else {
        this.toastr.error(data.message, null, { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
      }
      this.loading = false;
    }, error => {
      console.log(error);
      this.loading = false;
    }
    );
  }

}

const filterHistoryEventName = {
  values(params) {
    setTimeout(function () {
      params.success(['Device Installed', 'Maintenance']);
    }, 2000);
  },
};

function filterModel(rowData: any, params: any) {
  throw new Error('Function not implemented.');
}


function dateComparator(date1, date2) {
  const date1Number = monthToComparableNumber(date1);
  const date2Number = monthToComparableNumber(date2);

  if (date1Number === null && date2Number === null) {
    return 0;
  }
  if (date1Number === null) {
    return -1;
  }
  if (date2Number === null) {
    return 1;
  }

  return date1Number - date2Number;
}

function monthToComparableNumber(date) {
  if (date === undefined || date === null || date.length !== 10) {
    return null;
  }

  const yearNumber = Number.parseInt(date.substring(6, 10));
  const monthNumber = Number.parseInt(date.substring(3, 5));
  const dayNumber = Number.parseInt(date.substring(0, 2));

  return yearNumber * 10000 + monthNumber * 100 + dayNumber;
}
