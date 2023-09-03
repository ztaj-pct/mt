import { AgGridAngular } from '@ag-grid-community/angular';
import { GridOptions, Module, ClientSideRowModelModule, MenuModule, RichSelectModule, MultiFilterModule, IGetRowsParams } from '@ag-grid-enterprise/all-modules';
import { ColumnsToolPanelModule } from '@ag-grid-enterprise/column-tool-panel';
import { Component, OnInit, ViewChild } from '@angular/core';
import { FormControl, FormBuilder } from '@angular/forms';
import { Router } from '@angular/router';
import * as moment from 'moment';
import { ToastrService } from 'ngx-toastr';
import { ATCommandService } from '../atcommand.service';
import { CompaniesService } from '../companies.service';
import { GatewayListService } from '../gateway-list.service';
import { Asset } from '../models/asset';
import { CustomDateComponent } from '../ui-elements/custom-date-component.component';
import { CustomHeader } from '../ui-elements/custom-header.component';
import { UserService } from '../user/user.component.service';
import { UsersListService } from '../users-list/users-list.component.service';
import { PagerService } from '../util/pagerService';
import { PaginationConstants } from '../util/paginationConstants';
import { MaintenanceService } from './maintenance.service';
declare var $: any;

@Component({
  selector: 'app-maintenance-summary-report',
  templateUrl: './maintenance-summary-report.component.html',
  styleUrls: ['./maintenance-summary-report.component.css']
})
export class MaintenanceSummaryReportComponent implements OnInit {


  totalpage;
  showingpage;
  deviceId;
  deleteDeviceID;
  searchFlag;
  sortFlag;
  currentPage: any;
  pager: any = {};
  pagedItems: any[];
  private allItems: any;
  pageLimit = 25;
  totalRecords = 0;
  currentEndRecord = 0;
  currentStartRecord = 0;
  page = 0;
  selectedCompanyName;
  // parameters for sorting
  sortByAssetId = false;
  sortByType = false;
  sortByIsProductIsApprovedForAsset = false;
  sortByVin = false;
  sortByAssetType1 = false;
  sortByModelYear = false;
  sortByManufacturer = false;
  sortByStatus = false;
  sortByCreatedAt = false;
  sortByUpdatedAt = false;
  column = 'createdDate';
  order = 'asc';
  assetToDelete = Asset;
  userCompanyType;
  pageSizeOptions: PaginationConstants = new PaginationConstants();
  pageSizeObjArr = this.pageSizeOptions.pageSizeObjArr;
  pageSizeOptionsArr: number[];
  selected: number;
  isSuperAdmin;
  isRoleCustomerManager;
  companies: any;
  company: any;
  selectedCompany = new FormControl();
  selectDays = new FormControl(0);
  companyUuid = '';
  days = 0;
  selectedtoDateRange = [];
  // parameters for counting records
  filterValues = new Map();
  filterValuesJsonObj: any = {};
  isClearButtonVisible = false;
  loading = false;
  frameworkComponents;
  public sideBar;
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
    RichSelectModule,
    ColumnsToolPanelModule,
    MultiFilterModule,
    MultiFilterModule,
  ];
  rowModelType = 'infinite';
  // Pagination Save State
  isFirstTimeLoad = false;
  isPaginationStateRecovered = false;






  constructor(private maintenanceService: MaintenanceService, public userListService: UsersListService, public userService: UserService,
    public router: Router, private toastr: ToastrService, private pagerService: PagerService, private companiesService: CompaniesService,
    private readonly atcommandService: ATCommandService,
    private readonly gatewayListService: GatewayListService) {

    var containsFilterParams = {
      filterOptions: [
        'contains',
      ],
      suppressAndOrCondition: true
    };


    var filterParams1 = {

      filterOptions: [
        'equals', 'lessThan', 'lessThanOrEqual', 'greaterThan', 'greaterThanOrEqual'
      ],
      defaultOption: 'equals',
      suppressAndOrCondition: true,
    };
    this.columnDefs = [

      {
        headerName: 'Service Date', field: 'createdDate', sortable: true, filter: 'agDateColumnFilter', filterParams: containsFilterParams, minWidth: 200, hide: false,
        cellRenderer: (data) => {
          if (data != null && data != undefined && data.value != null && data.value != undefined) {
            return moment(data.value).format('YYYY-MM-DD HH:mm');
          } else {
            return '';
          }
        }
      },
      {
        headerName: 'Service Time', field: 'serviceTime', sortable: true, filter: 'agDateColumnFilter', filterParams: containsFilterParams, minWidth: 200, hide: false,
        cellRenderer: (data) => {
          if (data != null && data != undefined && data.value != null && data.value != undefined) {
            return moment(data.value).format('YYYY-MM-DD HH:mm');
          } else {
            return '';
          }
        }
      },
      {
        headerName: 'Asset ID', field: 'assetId', sortable: true, filter: 'agTextColumnFilter', filterParams: containsFilterParams, minWidth: 220,
        cellRenderer: function (params) {
          if (params.value) {
            return '<a href="javascript: void (0)">' + params.value + '</a>';
          }
        }

      },
      {
        headerName: 'Device ID', field: 'deviceId', sortable: true, filter: 'agTextColumnFilter', filterParams: containsFilterParams, minWidth: 220,
        cellRenderer: function (params) {
          if (params.value) {
            return '<a href="javascript: void (0)">' + params.value + '</a>';
          }
        }
      },

      {
        headerName: 'Sensor Type', field: 'sensorType', sortable: true, filter: 'agTextColumnFilter', filterParams: containsFilterParams, minWidth: 200,
      },
      {
        headerName: 'Old Sensor ID', field: 'oldSensorId', sortable: true, filter: 'agTextColumnFilter', filterParams: containsFilterParams, minWidth: 200,

      },
      {
        headerName: 'New Sensor ID', field: 'newSensorId', sortable: true, filter: 'agTextColumnFilter', filterParams: containsFilterParams, minWidth: 200
      },
      {
        headerName: 'Technician', field: 'technician', sortable: true, filter: 'agTextColumnFilter', filterParams: containsFilterParams, minWidth: 180,
      },
      {
        headerName: 'Service Vendor', field: 'serviceVendorName', sortable: true, filter: 'agTextColumnFilter', filterParams: containsFilterParams, minWidth: 200,
      },
      {
        headerName: 'Validation Time', field: 'validationTime', sortable: true, filter: 'agTextColumnFilter', filterParams: containsFilterParams, minWidth: 200,
        // cellRenderer: (data) => {
        //   if (data != null && data != undefined && data.value != null && data.value != undefined) {
        //     return moment(data.value).format('HH:mm');
        //   } else {
        //     return '';
        //   }
        // }
      },
      {
        headerName: 'Resolution Type', field: 'resolutionType', sortable: true, filter: 'agTextColumnFilter', filterParams: containsFilterParams, minWidth: 180,
      },
      {
        headerName: 'Work Order', field: 'workOrder', sortable: true, filter: 'agTextColumnFilter', filterParams: containsFilterParams, minWidth: 220,
      },
      {
        headerName: 'Yard Location', field: 'maintenanceLocation', sortable: true, filter: 'agTextColumnFilter', filterParams: containsFilterParams, minWidth: 220,
      },
    ];
    this.cacheOverflowSize = 2;
    this.maxConcurrentDatasourceRequests = 2;
    this.infiniteInitialRowCount = 2;

    if (sessionStorage.getItem('assetlistsize') != undefined) {
      this.pageLimit = Number(sessionStorage.getItem('assetlistsize'));
    }
    this.gridOptions = {
      cacheBlockSize: this.pageLimit,
      paginationPageSize: this.pageLimit,
      rowModelType: 'infinite',
    };
    this.frameworkComponents = {
      agDateInput: CustomDateComponent,
      agColumnHeader: CustomHeader
    };
    this.defaultColDef = {
      flex: 1,
      minWidth: 150,
      sortable: true,
      floatingFilter: true,
      enablePivot: true,
      resizable: true,
      filter: true,
      headerComponentParams: { menuIcon: 'fa-bars' },
    };
    this.components = {

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
            suppressColumnFilter: true,
            suppressColumnSelectAll: true,
            suppressColumnExpandAll: true
          },
        },
      ],
      defaultToolPanel: '',
    };

  }
  ngOnInit() {
    if (sessionStorage.getItem('selectedCompany') != null) {
      this.company = JSON.parse(sessionStorage.getItem('selectedCompany'));
      this.companyUuid = this.company;
      this.selectedCompany.setValue(this.company);
    }
    if (sessionStorage.getItem('selectedDays') != null) {
      this.days = JSON.parse(sessionStorage.getItem('selectedDays'));
      this.selectDays.setValue(this.days);
    }
    this.getAllcompanies();
    if (sessionStorage.getItem('assetlistsize') != undefined) {
      this.pageLimit = Number(sessionStorage.getItem('assetlistsize'));
    } else {
      this.pageLimit = 25;
    }

    this.sortFlag = true;

    this.userCompanyType = sessionStorage.getItem('type');
    this.isSuperAdmin = sessionStorage.getItem('IS_ROLE_SUPERADMIN');
    this.isRoleCustomerManager = sessionStorage.getItem('IS_ROLE_CUSTOMER_MANAGER');

  }


  getAllcompanies() {
    const type = ['CUSTOMER'];
    this.companiesService.getAllCompany(type, true).subscribe(
      data => {
        this.companies = data.body;
      },
      error => {
        console.log(error);
      }

    );
  }

  onCompanySelect(event) {
    this.company = event.value;
    sessionStorage.setItem('selectedCompany', JSON.stringify(this.company));
    if (event.value === '0') {
      this.companyUuid = '0';
    } else {
      this.companyUuid = event.value.uuid;
    }
    this.days = 7;
    this.onGridReady(this.paramList);
  }

  onDateRangeSelect(event) {
    this.days = event.value;
    sessionStorage.setItem('selectedDays', JSON.stringify(this.days));
    this.onGridReady(this.paramList);
  }
  onGridReady(params) {
    // this.gridOptions.api.setDomLayout('autoHeight');
    this.paramList = params;
    this.gridApi = params.api;
    this.gridApi.setRowData([]);
    this.gridColumnApi = params.columnApi;
    const gridSavedState = sessionStorage.getItem('maintenance_list_grid_column_state_' + this.companyUuid);
    if (gridSavedState) {
      this.gridColumnApi.setColumnState(JSON.parse(gridSavedState));
    }
    const filterSavedState = sessionStorage.getItem('maintenance_list_grid_filter_state_' + this.companyUuid);
    if (filterSavedState) {
      this.gridApi.setFilterModel(JSON.parse(filterSavedState));
    }
    var datasource = {
      getRows: (params: IGetRowsParams) => {
        this.sortForAgGrid(params.sortModel);
        this.filterForAgGrid(params.filterModel);
        this.loading = true;
        this.maintenanceService.getMaintenanceWithSort(this.gridApi.paginationGetCurrentPage() + 1, this.column, this.order, this.companyUuid, this.filterValuesJsonObj, this.pageLimit, this.days, false).subscribe(data => {
          this.rowData = data.body.content;
          this.loading = false;

          params.successCallback(this.rowData, data.total_key);
          if (this.isFirstTimeLoad == true) {
            const paginationSavedPage = sessionStorage.getItem('maintenance_list_grid_saved_page_' + this.companyUuid);
            if (paginationSavedPage && this.isFirstTimeLoad) {
              this.gridOptions.api.paginationGoToPage(Number.parseInt(paginationSavedPage));
              this.isFirstTimeLoad = false;
              this.isPaginationStateRecovered = true;
            }
          }
        }, error => {
          console.log(error);
        });
      }
      // }
    };
    this.gridApi.setDatasource(datasource);
  }

  onBtExport() {
    var params = {
      sheetName:
        'maintenance-summary-report',
      exportMode: undefined,
      rowHeight: 20,
      columnWidth: 150
    };
    this.gridApi.exportDataAsExcel(params);
  }

  onDragStopped(params) {
    this.saveGridStateInSession(this.gridColumnApi.getColumnState());
  }

  onSortChanged(params) {
    this.saveGridStateInSession(this.gridColumnApi.getColumnState());
  }

  onFilterChanged(params) {
    sessionStorage.setItem('maintenance_list_grid_filter_state_' + this.companyUuid, JSON.stringify(params.api.getFilterModel()));
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
      sessionStorage.setItem('maintenance_list_grid_saved_page_' + this.companyUuid, params.api.paginationGetCurrentPage());
    }
  }
  onFirstDataRendered(params) {
    this.isFirstTimeLoad = true;
  }


  saveGridStateInSession(columnState) {
    sessionStorage.setItem('maintenance_list_grid_column_state_' + this.companyUuid, JSON.stringify(columnState));
  }


  onCellClicked(params) {
    if (params.column.colId === 'deviceId') {
      sessionStorage.setItem('device_id', params.data.deviceId);
      this.gatewayListService.deviceId = params.data.deviceId;
      this.atcommandService.deviceId = this.gatewayListService.deviceId;
      if (this.gatewayListService.deviceId != null) {
        this.router.navigate(['gateway-profile']);
      }
    }

    if (params.column.colId === 'assetId') {
      this.loading = true;
      this.router.navigate(['add-assets'], { queryParams: { id: params.data.assetUuId } });
    }
  }

  getSelectedRowData() {
    return this.gridApi.getSelectedNodes().map(node => node.data);
  }


  addAssets() {
    this.router.navigate(['add-assets']);
  }
  uploadAssets() {
    this.router.navigate(['upload-assets']);
  }

  onPageSizeChange() {
    sessionStorage.setItem('assetlistsize', String(this.selected));
    this.pageLimit = (Number(this.selected));
    this.gridApi.gridOptionsWrapper.setProperty('cacheBlockSize', this.selected);
    const api: any = this.gridApi;
    api.rowModel.resetCache();
    this.gridApi.paginationSetPageSize(Number(this.selected));
  }

  editAsset(asset: Asset): void {
    if (this.userCompanyType === 'Manufacturer' || this.isSuperAdmin === 'true' || this.isRoleCustomerManager === 'true') {
      this.router.navigate(['add-assets']);
    }
  }

  filterForAgGrid(filterModel) {
    this.gridApi.paginationCurrentPage = 0;
    if (filterModel != undefined) {

      this.filterValuesJsonObj = {};
      this.filterValues = new Map();

      if (filterModel.createdDate && filterModel.createdDate.dateFrom && filterModel.createdDate.dateFrom !== '') {
        this.filterValues.set('createdDate', filterModel.createdDate.dateFrom);
      }

      if (filterModel.serviceTime && filterModel.serviceTime.dateFrom && filterModel.serviceTime.dateFrom !== '') {
        this.filterValues.set('serviceTime', filterModel.serviceTime.dateFrom);
      }

      if (filterModel.assetId != undefined && filterModel.assetId.filter != null && filterModel.assetId.filter != undefined && filterModel.assetId.filter != '') {
        this.filterValues.set('assetId', filterModel.assetId.filter);
      }

      if (filterModel.deviceId != undefined && filterModel.deviceId.filter != null && filterModel.deviceId.filter != undefined && filterModel.deviceId.filter != '') {
        this.filterValues.set('deviceId', filterModel.deviceId.filter);
      }

      if (filterModel.oldSensorId != undefined && filterModel.oldSensorId.filter != null && filterModel.oldSensorId.filter != undefined && filterModel.oldSensorId.filter != '') {
        this.filterValues.set('oldSensorId', filterModel.oldSensorId.filter);
      }

      if (filterModel.newSensorId != undefined && filterModel.newSensorId.filter != null && filterModel.newSensorId.filter != undefined && filterModel.newSensorId.filter != '') {
        this.filterValues.set('newSensorId', filterModel.newSensorId.filter);
      }

      if (filterModel.technician != undefined && filterModel.technician.filter != null && filterModel.technician.filter != undefined && filterModel.technician.filter != '') {
        this.filterValues.set('technician', filterModel.technician.filter);
      }

      if (filterModel.resolutionType != undefined && filterModel.resolutionType.filter != null && filterModel.resolutionType.filter != undefined && filterModel.resolutionType.filter != '') {
        this.filterValues.set('resolutionType', filterModel.resolutionType.filter);
      }

      if (filterModel.workOrder != undefined && filterModel.workOrder.filter != null && filterModel.workOrder.filter != undefined && filterModel.workOrder.filter != '') {
        this.filterValues.set('workOrder', filterModel.workOrder.filter);
      }

      if (filterModel.serviceVendorName != undefined && filterModel.serviceVendorName.filter != null && filterModel.serviceVendorName.filter != undefined && filterModel.serviceVendorName.filter != '') {
        this.filterValues.set('serviceVendorName', filterModel.serviceVendorName.filter);
      }

      if (filterModel.validationTime != undefined && filterModel.validationTime.filter != null && filterModel.validationTime.filter != undefined && filterModel.validationTime.filter != '') {
        this.filterValues.set('validationTime', filterModel.validationTime.filter);
      }

      if (filterModel.sensorType != undefined && filterModel.sensorType.filter != null && filterModel.sensorType.filter != undefined && filterModel.sensorType.filter != '') {
        this.filterValues.set('sensorType', filterModel.sensorType.filter);
      }

      if (filterModel.maintenanceLocation != undefined && filterModel.maintenanceLocation.filter != null && filterModel.maintenanceLocation.filter != undefined && filterModel.maintenanceLocation.filter != '') {
        this.filterValues.set('maintenanceLocation', filterModel.maintenanceLocation.filter);
      }

      // if (filterModel.serviceTime != undefined && filterModel.serviceTime.filter != null && filterModel.serviceTime.filter != undefined && filterModel.serviceTime.filter != '') {
      //   this.filterValues.set('serviceTime', filterModel.serviceTime.filter);
      // }

      // if (filterModel.serviceVendorName != undefined && filterModel.serviceVendorName.values != null && filterModel.serviceVendorName.values != undefined) {
      //   if (filterModel.serviceVendorName.values.length == 0) {
      //     this.filterValues.set('serviceVendorName', 'null');
      //   } else {
      //     this.filterValues.set('serviceVendorName', filterModel.serviceVendorName.values.toString());
      //   }
      // }

      // if (filterModel.validationTime != undefined && filterModel.validationTime.values != null && filterModel.validationTime.values != undefined) {
      //   if (filterModel.validationTime.values.length == 0) {
      //     this.filterValues.set('validationTime', 'null');
      //   } else {
      //     this.filterValues.set('validationTime', filterModel.validationTime.values.toString());
      //   }
      // }

      // if (filterModel.sensorType != undefined && filterModel.sensorType.values != null && filterModel.sensorType.values != undefined) {
      //   if (filterModel.sensorType.values.length == 0) {
      //     this.filterValues.set('sensorType', 'null');
      //   } else {
      //     this.filterValues.set('sensorType', filterModel.sensorType.values.toString());
      //   }
      // }

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
    this.sortFlag = true;
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



  backToList() {
    this.router.navigate(['customerAssets']);
  }




  reloadComponent() {
    const currentUrl = this.router.url;
    this.router.navigateByUrl('/', { skipLocationChange: true }).then(() =>
      this.router.navigate([currentUrl]));
  }

  clearFilter() {
    this.column = 'createdDate';
    this.order = 'asc';
    this.paramList.api.setFilterModel(null);
    // params.column.columnApi.applyColumnState({ defaultState: { sort: null } });
    // params.column.columnApi.resetColumnState();
    this.paramList.api.onFilterChanged();
    // sessionStorage.setItem('asset_list_grid_column_state_'+this.assetsService.assetCompanyId, null);
    sessionStorage.setItem('asset_list_grid_filter_state_' + this.companyUuid, null);
  }

}




var filterParamsStatus = {
  values: function (params) {
    setTimeout(function () {
      params.success(['Pass', 'Fail', 'Pending']);
    }, 2000);
  },
};

var filterParamsAssetStatus = {
  values: function (params) {
    setTimeout(function () {
      params.success(['Partial', 'Pending', 'Install in progress', 'Active', 'Inactive', 'Deleted', 'Error']);
    }, 2000);
  },
};

var filterParamsProductType = {
  values: function (params) {
    setTimeout(function () {
      params.success([
        'EZTrac',
        'EZTracPlus',
        'EZTracSolar',
        'AssetTrac',
        'DriveTrac',
        'ContainerNet',
        'ChassisNet',
        'TrailerNet',
        'Smart7',
        'Sabre']);
    }, 2000);
  },
};
