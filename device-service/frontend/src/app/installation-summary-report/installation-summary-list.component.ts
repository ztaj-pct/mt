import { Component, OnInit, ViewChild } from '@angular/core';
import { AssetsService } from '../assets.service';
import { Asset } from '../models/asset';
import { Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { PagerService } from '../util/pagerService';
import { AgGridAngular } from '@ag-grid-community/angular';
import { GridOptions, IDatasource, IGetRowsParams } from '@ag-grid-community/all-modules';
import { HttpClient } from '@angular/common/http';
import { ClientSideRowModelModule } from '@ag-grid-community/client-side-row-model';
import { MenuModule } from '@ag-grid-enterprise/menu';
import { ColumnsToolPanelModule } from '@ag-grid-enterprise/column-tool-panel';
import '@ag-grid-community/core/dist/styles/ag-grid.css';
import '@ag-grid-community/core/dist/styles/ag-theme-alpine.css';
import { Module } from '@ag-grid-community/core';
import { MultiFilterModule } from '@ag-grid-enterprise/multi-filter';
import { SetFilterModule } from '@ag-grid-enterprise/set-filter';
import { FormBuilder, FormControl, FormGroup } from '@angular/forms';
import * as moment from 'moment';

import { RichSelectModule } from '@ag-grid-enterprise/rich-select';
import { CustomDateComponent } from '../ui-elements/custom-date-component.component';
import { CustomHeader } from '../ui-elements/custom-header.component';
import { PaginationConstants } from '../util/paginationConstants';
import { CompaniesService } from '../companies.service';
import { InstallationService } from './installation.service';
import { UsersListService } from '../users-list/users-list.component.service';
import { UserService } from '../user/user.component.service';
import { BtnCellRendererForAssets } from '../assets-list/btn-cell-renderer.component';
import { User } from '../models/user.model';
declare var $: any;


@Component({
  selector: 'app-installation-summary-list',
  templateUrl: './installation-summary-list.component.html',
  styleUrls: ['./installtion-summary-list.component.css']

})
export class InstallationSummaryComponent implements OnInit {

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
  column = 'dateStarted';
  order = 'asc';
  assetToDelete = Asset;
  userCompanyType;
  pageSizeOptions: PaginationConstants = new PaginationConstants();
  pageSizeObjArr = this.pageSizeOptions.pageSizeObjArrForInstallationSummaryView;
  pageSizeOptionsArr: number[];
  filterValuesJsonObj1: any = { 'status': 'Active' };
  selected: number;
  isSuperAdmin;
  isRoleCustomerManager;

  companies: any;
  company: any;
  selectedCompany = new FormControl();
  selectDays = new FormControl(0);
  companyUuid = '1';
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
  totalCount = 0;







  constructor(private installService: InstallationService, public userListService: UsersListService, public userService: UserService,
    public router: Router, private toastr: ToastrService, private pagerService: PagerService, private companiesService: CompaniesService, public _formBuldier: FormBuilder) {
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
        headerName: 'Asset ID', field: 'asset_id', sortable: true, filter: 'agTextColumnFilter', filterParams: containsFilterParams,
        cellRenderer: function (params) {
          if (params.data != undefined) {
            return '<a href="javascript:void(0)">' + params.value + '</a>';
          }
        }
      },
      {
        headerName: 'Product', field: 'product_name',
        sortable: true, filter: 'agSetColumnFilter', filterParams: filterParamsProductType,
        minWidth: 200,
        cellRenderer: function (params) {
          if (params.data != undefined) {
            return params.data.product_name + '&nbsp(' + params.data.product_code + ')';
          }
        }
      },
      {
        headerName: 'Device ID', field: 'device_id', sortable: true, filter: 'agTextColumnFilter', filterParams: containsFilterParams, minWidth: 220,

      },
      {
        headerName: 'Status', field: 'status', sortable: true, filter: 'agSetColumnFilter', floatingFilter: true, flex: 1,
        filterParams: filterParamsAssetStatus,

      },

      {
        headerName: 'User Name', field: 'user_name', sortable: true, filter: 'agTextColumnFilter', filterParams: containsFilterParams, minWidth: 200,
        cellRenderer: function (params) {
          if (params.data != undefined && params.data.user_name != null) {
            return '<a href="javascript:void(0)">' + params.data.user_name + '</a>';
          }
        }
      },
      {
        headerName: 'User Company', field: 'organisation_name', sortable: true, filter: 'agTextColumnFilter', filterParams: containsFilterParams, minWidth: 200,
        cellRenderer: function (params) {
          if (params.data != undefined && params.data.organisation_name != null) {
            return '<a href="javascript:void(0)">' + params.data.organisation_name + '</a>';
          }
        }
      },

      {
        headerName: 'Installed', field: 'installed', sortable: true, filter: 'agDateColumnFilter', filterParams: containsFilterParams, minWidth: 200,
        cellRenderer: (data) => {
          if (data.value != null) {
            return moment(data.value).format('YYYY-MM-DD HH:mm');
          }
        }
      }, {
        headerName: 'Last Updated On', field: 'updated_at', sortable: true, filter: 'agDateColumnFilter', filterParams: containsFilterParams, minWidth: 200, hide: false,
        cellRenderer: (data) => {
          if (data != null && data != undefined && data.value != null && data.value != undefined) {
            return moment(data.value).format('YYYY-MM-DD HH:mm');
          } else {
            return '';
          }
        }
      },

      {
        headerName: 'Battery', field: 'battery_status', sortable: true, filter: 'agSetColumnFilter', filterParams: filterParamsStatus, minWidth: 180,

        cellRenderer: function (params) {
          if (params.data != undefined && params.data.battery_status != null) {
            return params.data.battery_status;
          } else {

            return 'N/A';

          }
        }
      },
      {
        headerName: 'Battery V', field: 'battery_voltage', sortable: true, filter: 'agSetColumnFilter', filterParams: filterParamsStatus, minWidth: 180,

        cellRenderer: function (params) {
          if (params.data != undefined && params.data.battery_voltage != null) {
            return params.data.battery_voltage;
          } else {

            return 'N/A';

          }
        }
      }, {
        headerName: 'Blue/ABS', field: 'primary_status', sortable: true, filter: 'agSetColumnFilter', filterParams: filterParamsStatus, minWidth: 180,

        cellRenderer: function (params) {
          if (params.data != undefined && params.data.primary_status != null) {
            return params.data.primary_status;
          } else {

            return 'N/A';

          }
        }
      },
      {
        headerName: 'Blue/ABS V', field: 'primary_voltage', sortable: true, filter: 'agSetColumnFilter', filterParams: filterParamsStatus, minWidth: 180,

        cellRenderer: function (params) {
          if (params.data != undefined && params.data.primary_voltage != null) {
            return params.data.primary_voltage;
          } else {

            return 'N/A';

          }
        }
      },
      {
        headerName: 'Marker/Brown', field: 'secondary_status', sortable: true, filter: 'agSetColumnFilter', filterParams: filterParamsStatus, minWidth: 180,

        cellRenderer: function (params) {
          if (params.data != undefined && params.data.secondary_status != null) {
            return params.data.secondary_status;
          } else {

            return 'N/A';

          }
        }
      },
      {
        headerName: 'Marker/Brown V', field: 'secondary_voltage', sortable: true, filter: 'agSetColumnFilter', filterParams: filterParamsStatus, minWidth: 180,

        cellRenderer: function (params) {
          if (params.data != undefined && params.data.secondary_voltage != null) {
            return params.data.secondary_voltage;
          } else {

            return 'N/A';

          }
        }
      },

      {
        headerName: 'Cargo Sensor', field: 'cargo_sensor', sortable: true, filter: 'agSetColumnFilter', filterParams: filterParamsStatus, minWidth: 180,

        cellRenderer: function (params) {
          if (params.data != undefined && params.data.cargo_sensor != null) {
            return params.data.cargo_sensor;
          } else {

            return 'N/A';

          }
        }
      },
      {
        headerName: 'Cargo Camera Sensor', field: 'cargo_camera_sensor', sortable: true, filter: 'agSetColumnFilter', filterParams: filterParamsStatus, minWidth: 180,

        cellRenderer: function (params) {
          if (params.data != undefined && params.data.cargo_camera_sensor != null) {
            return params.data.cargo_camera_sensor;
          } else {

            return 'N/A';

          }
        }
      },
      {
        headerName: 'Door Sensor', field: 'door_sensor', sortable: true, filter: 'agSetColumnFilter', filterParams: filterParamsStatus, minWidth: 180,
        cellRenderer: function (params) {
          if (params.data != undefined && params.data.door_sensor != null) {
            return params.data.door_sensor;
          } else {
            return 'N/A';
          }
        }
      },
      {
        headerName: 'Door MAC', field: 'door_mac_address', sortable: true, filter: 'agSetColumnFilter', filterParams: filterParamsStatus, minWidth: 180,
        cellRenderer: function (params) {
          if (params.data != undefined && params.data.door_mac_address != null) {
            return params.data.door_mac_address;
          } else {
            return 'N/A';
          }
        }
      },
      {
        headerName: 'Door Type', field: 'door_type', sortable: true, filter: 'agSetColumnFilter', filterParams: filterParamsStatus, minWidth: 180,
        cellRenderer: function (params) {
          if (params.data != undefined && params.data.door_type != null) {
            return params.data.door_type;
          } else {
            return 'N/A';
          }
        }
      },
      {
        headerName: 'LampCheck ATIS', field: 'lamp_check_atis', sortable: true, filter: 'agSetColumnFilter', filterParams: filterParamsStatus, minWidth: 180,
        cellRenderer: function (params) {
          if (params.data != undefined && params.data.lamp_check_atis != null) {
            return params.data.lamp_check_atis;
          } else {
            return 'N/A';
          }
        }
      },
      {
        headerName: 'LampCheck ATIS MAC', field: 'lamp_check_atis_mac', sortable: true, filter: 'agSetColumnFilter', filterParams: filterParamsStatus, minWidth: 220,
        cellRenderer: function (params) {
          if (params.data != undefined && params.data.lamp_check_atis_mac != null) {
            return params.data.lamp_check_atis_mac;
          } else {
            return 'N/A';
          }
        }
      },


      {
        headerName: 'ABS', field: 'abssensor', sortable: true, filter: 'agSetColumnFilter', filterParams: filterParamsStatus,
        cellRenderer: function (params) {
          if (params.data != undefined && params.data.abssensor != null) {
            return params.data.abssensor;
          } else {

            return 'N/A';

          }
        }
      },

      {
        headerName: 'IA Version', field: 'app_version', sortable: true, filter: 'agSetColumnFilter', filterParams: filterParamsStatus,
        cellRenderer: function (params) {
          if (params.data != undefined && params.data.app_version != null) {
            return params.data.app_version;
          } else {
            return 'N/A';

          }
        }
      },

      {
        headerName: 'ATIS', field: 'atis_sensor', sortable: true, filter: 'agSetColumnFilter', filterParams: filterParamsStatus,
        cellRenderer: function (params) {
          if (params.data != undefined && params.data.atis_sensor != null) {
            return params.data.atis_sensor;
          } else {
            return 'N/A';
          }
        }
      },
      {
        headerName: 'LiteSentry', field: 'light_sentry', sortable: true, filter: 'agSetColumnFilter', filterParams: filterParamsStatus, minWidth: 180,
        cellRenderer: function (params) {
          if (params.data != undefined && params.data.light_sentry != null) {
            return params.data.light_sentry;
          } else {
            return 'N/A';
          }
        }
      },
      {
        headerName: 'Receiver', field: 'reciever', sortable: true, filter: 'agSetColumnFilter', filterParams: filterParamsStatus,
        cellRenderer: function (params) {
          if (params.data != undefined && params.data.reciever != null) {
            return params.data.reciever;
          } else {
            return 'N/A';
          }
        }
      },
      // { headerName: 'TPMS', field: 'tpms', sortable: true, filter: 'agSetColumnFilter', filterParams: filterParamsStatus,
      // cellRenderer: function (params) {
      //   if(params.data != undefined && params.data.tpms!=null){
      //     return  params.data.tpms;
      //   }else{
      //     return 'N/A';
      //   }
      // }      
      // },
      {
        headerName: 'TPMS LOF-1 ID', field: 'tpms_lof', sortable: true, filter: 'agSetColumnFilter', filterParams: filterParamsStatus, minWidth: 200,
        cellRenderer: function (params) {
          if (params.data != undefined && params.data.tpms_lof != null) {
            return params.data.tpms_lof;
          } else {
            return 'N/A';
          }
        }
      }, {
        headerName: 'TPMS LIF-2 ID', field: 'tpms_lif', sortable: true, filter: 'agSetColumnFilter', filterParams: filterParamsStatus, minWidth: 200,
        cellRenderer: function (params) {
          if (params.data != undefined && params.data.tpms_lif != null) {
            return params.data.tpms_lif;
          } else {
            return 'N/A';
          }
        }
      }, {
        headerName: 'TPMS RIF-3 ID', field: 'tpms_rif', sortable: true, filter: 'agSetColumnFilter', filterParams: filterParamsStatus, minWidth: 200,
        cellRenderer: function (params) {
          if (params.data != undefined && params.data.tpms_rif != null) {
            return params.data.tpms_rif;
          } else {
            return 'N/A';
          }
        }
      }, {
        headerName: 'TPMS ROF-4 ID', field: 'tpms_rof', sortable: true, filter: 'agSetColumnFilter', filterParams: filterParamsStatus, minWidth: 200,
        cellRenderer: function (params) {
          if (params.data != undefined && params.data.tpms_rof != null) {
            return params.data.tpms_rof;
          } else {
            return 'N/A';
          }
        }
      }, {
        headerName: 'TPMS LOR-5 ID', field: 'tpms_lor', sortable: true, filter: 'agSetColumnFilter', filterParams: filterParamsStatus, minWidth: 200,
        cellRenderer: function (params) {
          if (params.data != undefined && params.data.tpms_lor != null) {
            return params.data.tpms_lor;
          } else {
            return 'N/A';
          }
        }
      }, {
        headerName: 'TPMS LIR-6 ID', field: 'tpms_lir', sortable: true, filter: 'agSetColumnFilter', filterParams: filterParamsStatus, minWidth: 200,
        cellRenderer: function (params) {
          if (params.data != undefined && params.data.tpms_lir != null) {
            return params.data.tpms_lir;
          } else {
            return 'N/A';
          }
        }
      }, {
        headerName: 'TPMS RIR-7 ID', field: 'tpms_rir', sortable: true, filter: 'agSetColumnFilter', filterParams: filterParamsStatus, minWidth: 200,
        cellRenderer: function (params) {
          if (params.data != undefined && params.data.tpms_rir != null) {
            return params.data.tpms_rir;
          } else {
            return 'N/A';
          }
        }
      }, {
        headerName: 'TPMS ROR-8 ID', field: 'tpms_ror', sortable: true, filter: 'agSetColumnFilter', filterParams: filterParamsStatus, minWidth: 200,
        cellRenderer: function (params) {
          if (params.data != undefined && params.data.tpms_ror != null) {
            return params.data.tpms_ror;
          } else {
            return 'N/A';
          }
        }
      }, {
        headerName: 'Air Tank', field: 'air_tank', sortable: true, filter: 'agSetColumnFilter', filterParams: filterParamsStatus,
        cellRenderer: function (params) {
          if (params.data != undefined && params.data.air_tank != null) {
            return params.data.air_tank;
          } else {
            return 'N/A';
          }
        }
      },
      {
        headerName: 'Air Tank ID', field: 'air_tank_id', sortable: true, filter: 'agSetColumnFilter', filterParams: filterParamsStatus,
        cellRenderer: function (params) {
          if (params.data != undefined && params.data.air_tank_id != null) {
            return params.data.air_tank_id;
          } else {
            return 'N/A';
          }
        }
      },

      {
        headerName: 'Regulator', field: 'regulator', sortable: true, filter: 'agSetColumnFilter', filterParams: filterParamsStatus,
        cellRenderer: function (params) {
          if (params.data != undefined && params.data.regulator != null) {
            return params.data.regulator;
          } else {
            return 'N/A';
          }
        }
      },
      {
        headerName: 'Regulator ID', field: 'regulator_id', sortable: true, filter: 'agSetColumnFilter', filterParams: filterParamsStatus, minWidth: 180,
        cellRenderer: function (params) {
          if (params.data != undefined && params.data.regulator_id != null) {
            return params.data.regulator_id;
          } else {
            return 'N/A';
          }
        }
      },
      {
        headerName: 'Wheel End', field: 'wheel_end', sortable: true, filter: 'agSetColumnFilter', filterParams: filterParamsStatus,
        cellRenderer: function (params) {
          if (params.data != undefined && params.data.wheel_end != null) {
            return params.data.wheel_end;
          } else {
            return 'N/A';
          }
        }
      },
      {
        headerName: 'Action', field: 'action', sortable: false, filter: 'agNumberColumnFilter', floatingFilter: true, floatingFilterComponent: 'customClearFloatingFilter', floatingFilterComponentParams: {
          suppressFilterButton: true
        }, cellRenderer: 'btnCellRenderer', width: 100, minWidth: 120, pinned: 'right'
      }

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
      btnCellRenderer: BtnCellRendererForAssets,
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
    if (sessionStorage.getItem('selectedCompany') != undefined && sessionStorage.getItem('selectedCompany') != null
      && sessionStorage.getItem('selectedCompany') != 'undefined') {
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
    var type: string[];
    type = ['CUSTOMER'];
    this.companiesService.getAllCompany(type, true).subscribe(
      data => {
        this.companies = [];
        if (data != undefined && data != null
          && data.body != undefined && data.body != null
          && data.body.length > 0) {
          this.companies = data.body;
        }
        // this.companyForm.controls['company'].patchValue(new Array(this.companies[this.companies.findIndex(com => com.companyName === this.company)]));
      },
      error => {
        console.log(error);
      }

    );
  }

  getClearFilterFloatingComponent() {
    function ClearFloatingFilter() { }
    ClearFloatingFilter.prototype.init = function (params) {
      this.eGui = document.createElement('div');
      this.eGui.innerHTML =
        '<button class="btnClear primary_buttonClear" (click)="clearFilter()">Clear Filters</button>';
      this.currentValue = null;
      this.clearFilterButton = this.eGui.querySelector('button');
      const that = this;
      function onClearFilterButtonClicked() {
        this.column = 'assignedName';
        this.order = 'asc';
        params.api.setFilterModel(null);
        // params.column.columnApi.applyColumnState({ defaultState: { sort: null } });
        // params.column.columnApi.resetColumnState();
        params.api.onFilterChanged();
        // sessionStorage.setItem("asset_list_grid_column_state_"+this.assetsService.assetCompanyId, null);
        sessionStorage.setItem('asset_list_grid_filter_state_' + this.organisationId, null);
      }
      this.clearFilterButton.addEventListener('click', onClearFilterButtonClicked);
    };
    ClearFloatingFilter.prototype.onParentModelChanged = function (parentModel) {
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

  onCompanySelect(event) {
    this.company = event.value;
    sessionStorage.setItem('selectedCompany', JSON.stringify(this.company));
    this.companyUuid = event.value;
    this.days = 7;
    this.onGridReady(this.paramList);
  }

  onDateRangeSelect(event) {
    this.days = event.value;
    sessionStorage.setItem('selectedDays', JSON.stringify(this.days));
    this.onGridReady(this.paramList);
  }
  onGridReady(params) {
    this.gridOptions.api.setDomLayout('autoHeight');
    this.paramList = params;
    this.gridApi = params.api;
    this.gridApi.setRowData([]);
    this.gridColumnApi = params.columnApi;
    let gridSavedState = sessionStorage.getItem('install_list_grid_column_state_' + this.companyUuid);
    if (gridSavedState) {
      this.gridColumnApi.setColumnState(JSON.parse(gridSavedState));
    }
    let filterSavedState = sessionStorage.getItem('install_list_grid_filter_state_' + this.companyUuid);
    if (filterSavedState) {
      this.gridApi.setFilterModel(JSON.parse(filterSavedState));
    }

    var datasource = {
      getRows: (params: IGetRowsParams) => {

        this.sortForAgGrid(params.sortModel);
        this.filterForAgGrid(params.filterModel);
        this.loading = true;
        $('#temp1').addClass('loadingAsset');
        // if(this.companyUuid != null && this.companyUuid != undefined){
        this.installService.getInstallationWithSort(this.gridApi.paginationGetCurrentPage() + 1, this.column, this.order, this.companyUuid, this.filterValuesJsonObj, this.pageLimit, this.days).subscribe(data => {

          this.rowData = data.body.content;
          $('#temp1').removeClass('loadingAsset');
          if (this.rowData != null && this.rowData.length > 0) {
            for (var i = 0; i < this.rowData.length; i++) {
              if (this.rowData[i].reciever == null) {
                this.rowData[i].reciever = 'N/A';
              }
              if (this.rowData[i].cargo_sensor == null) {
                this.rowData[i].cargo_sensor = 'N/A';
              }
              if (this.rowData[i].pct_cargo_sensor == null) {
                this.rowData[i].pct_cargo_sensor = 'N/A';
              }
              if (this.rowData[i].cargo_camera_sensor == null) {
                this.rowData[i].cargo_camera_sensor = 'N/A';
              }
              if (this.rowData[i].abssensor == null) {
                this.rowData[i].abssensor = 'N/A';
              }
              if (this.rowData[i].door_sensor == null) {
                this.rowData[i].door_sensor = 'N/A';
              }
              if (this.rowData[i].atis_sensor == null) {
                this.rowData[i].atis_sensor = 'N/A';
              }
              if (this.rowData[i].light_sentry == null) {
                this.rowData[i].light_sentry = 'N/A';
              }
              if (this.rowData[i].tpms == null) {
                this.rowData[i].tpms = 'N/A';
              }
              if (this.rowData[i].wheel_end == null) {
                this.rowData[i].wheel_end = 'N/A';
              }
              if (this.rowData[i].regulator == null) {
                this.rowData[i].regulator = 'N/A';
              }
              if (this.rowData[i].air_tank == null) {
                this.rowData[i].air_tank = 'N/A';
              }
              if (this.rowData[i].air_tank_id == null) {
                this.rowData[i].air_tank_id = 'N/A';
              }
              if (this.rowData[i].regulator_id == null) {
                this.rowData[i].regulator_id = 'N/A';
              }
              if (this.rowData[i].tpms_rir == null) {
                this.rowData[i].tpms_rir = 'N/A';
              }
              if (this.rowData[i].tpms_ror == null) {
                this.rowData[i].tpms_ror = 'N/A';
              }
              if (this.rowData[i].tpms_lir == null) {
                this.rowData[i].tpms_lir = 'N/A';
              }
              if (this.rowData[i].tpms_lor == null) {
                this.rowData[i].tpms_lor = 'N/A';
              }
              if (this.rowData[i].tpms_rif == null) {
                this.rowData[i].tpms_rif = 'N/A';
              }
              if (this.rowData[i].tpms_lif == null) {
                this.rowData[i].tpms_lif = 'N/A';
              }
              if (this.rowData[i].tpms_lof == null) {
                this.rowData[i].tpms_lof = 'N/A';
              }
              if (this.rowData[i].lamp_check_atis_mac == null) {
                this.rowData[i].lamp_check_atis_mac = 'N/A';
              }
              if (this.rowData[i].lamp_check_atis == null) {
                this.rowData[i].lamp_check_atis = 'N/A';
              }
              if (this.rowData[i].door_type == null) {
                this.rowData[i].door_type = 'N/A';
              }
              if (this.rowData[i].door_mac_address == null) {
                this.rowData[i].door_mac_address = 'N/A';
              }
              if (this.rowData[i].tpms_rof == null) {
                this.rowData[i].tpms_rof = 'N/A';
              }
              if (this.rowData[i].tpms_rof == null) {
                this.rowData[i].tpms_rof = 'N/A';
              }
              if (this.rowData[i].secondary_voltage == null) {
                this.rowData[i].secondary_voltage = 'N/A';
              }
              if (this.rowData[i].secondary_status == null) {
                this.rowData[i].secondary_status = 'N/A';
              }
              if (this.rowData[i].primary_voltage == null) {
                this.rowData[i].primary_voltage = 'N/A';
              }
              if (this.rowData[i].primary_status == null) {
                this.rowData[i].primary_status = 'N/A';
              }
              if (this.rowData[i].battery_voltage == null) {
                this.rowData[i].battery_voltage = 'N/A';
              }
              if (this.rowData[i].battery_status == null) {
                this.rowData[i].battery_status = 'N/A';
              }
            }

          }
          this.loading = true;
          this.totalCount = data.total_key;
          params.successCallback(this.rowData, data.total_key);
          if (this.isFirstTimeLoad == true) {
            let paginationSavedPage = sessionStorage.getItem('install_list_grid_saved_page_' + this.companyUuid);
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
      fileName:
        'installation-summary-report'
    };
    this.gridApi.exportDataAsCsv(params);
  }

  onDragStopped(params) {
    let columnState = this.gridColumnApi.getColumnState();
    this.saveGridStateInSession(columnState);
  }

  onSortChanged(params) {
    let columnState = this.gridColumnApi.getColumnState();
    this.saveGridStateInSession(columnState);
  }

  onFilterChanged(params) {
    let filterModel = params.api.getFilterModel();
    sessionStorage.setItem('install_list_grid_filter_state_' + this.companyUuid, JSON.stringify(filterModel));
  }

  onColumnVisible(params) {
    let columnState = this.gridColumnApi.getColumnState();
    this.saveGridStateInSession(columnState);
  }

  onColumnPinned(params) {
    let columnState = this.gridColumnApi.getColumnState();
    this.saveGridStateInSession(columnState);
  }


  onColumnResized(params) {
    let columnState = this.gridColumnApi.getColumnState();
    this.saveGridStateInSession(columnState);
  }

  onPaginationChanged(params) {

    this.gridApi.forEachNode(function (node) {
      node.setSelected(false);
    });
    if (this.isPaginationStateRecovered || params.api.paginationGetCurrentPage() > 0) {
      sessionStorage.setItem('install_list_grid_saved_page_' + this.companyUuid, params.api.paginationGetCurrentPage());
    }
  }
  onFirstDataRendered(params) {
    this.isFirstTimeLoad = true;
  }


  saveGridStateInSession(columnState) {
    sessionStorage.setItem('install_list_grid_column_state_' + this.companyUuid, JSON.stringify(columnState));
  }


  onCellClicked(params) {

    if (params.column.colId == 'asset_id') {
      this.loading = false;
      this.installService.deviceUUid = params.data.device_uuid;
      sessionStorage.setItem('device_id', params.data.device_id);
      sessionStorage.setItem('device_uuid', this.installService.deviceUUid);
      // this.router.navigate(['gateway-profile']);
      this.router.navigate(['gateway-profile'], { queryParams: { tab: 'History' } });
    }
    if (params.column.colId == 'user_name') {
      this.loading = true;
      this.userListService.getUserById(params.data.user_id).subscribe(data => {
        this.userService.editFlag = true;
        this.userService.user = data.body;
        this.userService.user.role = this.userService.user.role;
        this.userService.user.notify = this.userService.user.notify;
        this.router.navigate(['add-user'], { queryParams: { id: this.userService.user.id } });

      }, error => {
        console.log(error);
      });

    }

    if (params.column.colId == 'organisation_name') {
      this.loading = true;
      this.router.navigate(['add-company'], { queryParams: { id: params.data.org_id } });

    }
  }

  getSelectedRowData() {
    let selectedNodes = this.gridApi.getSelectedNodes();
    let selectedData = selectedNodes.map(node => node.data);
    return selectedData;
  }

  editUser(user: User): void {
    this.router.navigate(['add-user'], { queryParams: { id: user.id } });
  }
  addAssets() {
    this.router.navigate(['add-assets']);
  }
  uploadAssets() {
    this.router.navigate(['upload-assets']);
  }

  onPageSizeChange() {
    // var value = (<HTMLInputElement>document.getElementById('page-size')).value;
    sessionStorage.setItem('assetlistsize', String(this.selected));
    var value = this.selected;
    this.pageLimit = (Number(value));
    this.gridApi.gridOptionsWrapper.setProperty('cacheBlockSize', value);
    const api: any = this.gridApi;
    api.rowModel.resetCache();
    this.gridApi.paginationSetPageSize(Number(value));
  }

  editAsset(asset: Asset): void {
    if (this.userCompanyType === 'Manufacturer' || this.isSuperAdmin === 'true' || this.isRoleCustomerManager === 'true') {
      // this.assetsService.asset = asset
      // this.assetsService.editFlag = true;
      this.router.navigate(['add-assets']);
    }

    // this.assetsService.getUserById(asset.id).subscribe(data => {
    //   this.assetsService.editFlag = true;
    //   this.assetsService.asset = data.body;
    //   this.router.navigate(['add-assets']);

    // }, error => {
    //   console.log(error)
    // });
  }







  filterForAgGrid(filterModel) {
    this.gridApi.paginationCurrentPage = 0;
    if (filterModel != undefined) {

      this.filterValuesJsonObj = {};
      this.filterValues = new Map();
      if (filterModel.asset_id != undefined && filterModel.asset_id.filter != null && filterModel.asset_id.filter != undefined && filterModel.asset_id.filter != '') {
        this.filterValues.set('assetId', filterModel.asset_id.filter);
      }
      if (filterModel.product_name != undefined && filterModel.product_name.filter != null && filterModel.product_name.filter != undefined && filterModel.product_name.filter != '') {
        this.filterValues.set('productName', filterModel.product_name.filter);
      }


      if (filterModel.product_name != undefined && filterModel.product_name.values != null && filterModel.product_name.values != undefined) {
        if (filterModel.product_name.values.length == 0) {
          this.filterValues.set('productName', 'null');
        } else {
          this.filterValues.set('productName', filterModel.product_name.values.toString());
        }
      }


      if (filterModel.device_id != undefined && filterModel.device_id.filter != null && filterModel.device_id.filter != undefined && filterModel.device_id.filter != '') {
        this.filterValues.set('deviceId', filterModel.device_id.filter);
      }
      if (filterModel.category != undefined && filterModel.category.values != null && filterModel.category.values != undefined) {
        if (filterModel.category.values.length == 0) {
          this.filterValues.set('category', 'null');
        } else {
          // for(var i=0; i<filterModel.category.values.length;i++){
          // this.filterValues.set("category", filterModel.category.values[i]);
          this.filterValues.set('category', filterModel.category.values.toString());
        }
      }
      if (filterModel.status != undefined && filterModel.status.values != null && filterModel.status.values != undefined) {
        if (filterModel.status.values.length == 0) {
          this.filterValues.set('status', 'null');
        } else {
          this.filterValues.set('status', filterModel.status.values.toString());
        }
      }
      if (filterModel.installed != undefined && filterModel.installed.dateFrom != null && filterModel.installed.dateFrom != undefined && filterModel.installed.dateFrom != '') {

        this.filterValues.set('installed', filterModel.installed.dateFrom);
      }

      if (filterModel.updated_at != undefined && filterModel.updated_at.dateFrom != null && filterModel.updated_at.dateFrom != undefined && filterModel.updated_at.dateFrom != '') {
        this.filterValues.set('updatedAt', filterModel.updated_at.dateFrom);
      }
      if (filterModel.user_name != undefined && filterModel.user_name.filter != null && filterModel.user_name.filter != undefined && filterModel.user_name.filter != '') {
        let removedSpacesText =
          filterModel.user_name.filter.split(' ').join('');
        this.filterValues.set('installer_name', removedSpacesText);
      }
      if (filterModel.organisation_name != undefined && filterModel.organisation_name.filter != null && filterModel.organisation_name.filter != undefined && filterModel.organisation_name.filter != '') {
        this.filterValues.set('installer_company', filterModel.organisation_name.filter);
      }


      if (filterModel.status != undefined && filterModel.status.values != null && filterModel.status.values != undefined) {
        if (filterModel.status.values.length == 0) {
          this.filterValues.set('status', 'null');
        } else {
          this.filterValues.set('status', filterModel.status.values.toString());
        }
      }



      if (filterModel.cargo_sensor != undefined && filterModel.cargo_sensor.values != null && filterModel.cargo_sensor.values != undefined) {
        if (filterModel.cargo_sensor.values.length == 0) {
          this.filterValues.set('cargo_sensor', 'null');
        } else {
          this.filterValues.set('cargo_sensor', filterModel.cargo_sensor.values.toString());
        }
      }

      if (filterModel.pct_cargo_sensor != undefined && filterModel.pct_cargo_sensor.values != null && filterModel.pct_cargo_sensor.values != undefined) {
        if (filterModel.pct_cargo_sensor.values.length == 0) {
          this.filterValues.set('pct_cargo_sensor', 'null');
        } else {
          this.filterValues.set('pct_cargo_sensor', filterModel.pct_cargo_sensor.values.toString());
        }
      }
      if (filterModel.cargo_camera_sensor != undefined && filterModel.cargo_camera_sensor.values != null && filterModel.cargo_camera_sensor.values != undefined) {
        if (filterModel.cargo_camera_sensor.values.length == 0) {
          this.filterValues.set('cargo_camera_sensor', 'null');
        } else {
          this.filterValues.set('cargo_camera_sensor', filterModel.cargo_camera_sensor.values.toString());
        }
      }
      if (filterModel.abssensor != undefined && filterModel.abssensor.values != null && filterModel.abssensor.values != undefined) {
        if (filterModel.abssensor.values.length == 0) {
          this.filterValues.set('abssensor', 'null');
        } else {
          this.filterValues.set('abssensor', filterModel.abssensor.values.toString());
        }
      }

      if (filterModel.tpms != undefined && filterModel.tpms.values != null && filterModel.tpms.values != undefined) {
        if (filterModel.tpms.values.length == 0) {
          this.filterValues.set('tpms', 'null');
        } else {

          this.filterValues.set('tpms', filterModel.tpms.values.toString());
        }
      }
      if (filterModel.light_sentry != undefined && filterModel.light_sentry.values != null && filterModel.light_sentry.values != undefined) {
        if (filterModel.light_sentry.values.length == 0) {
          this.filterValues.set('light_sentry', 'null');
        } else {

          this.filterValues.set('light_sentry', filterModel.light_sentry.values.toString());
        }
      }
      if (filterModel.atis_sensor != undefined && filterModel.atis_sensor.values != null && filterModel.atis_sensor.values != undefined) {
        if (filterModel.atis_sensor.values.length == 0) {
          this.filterValues.set('atis_sensor', 'null');
        } else {

          this.filterValues.set('atis_sensor', filterModel.atis_sensor.values.toString());
        }
      }
      if (filterModel.door_sensor != undefined && filterModel.door_sensor.values != null && filterModel.door_sensor.values != undefined) {
        if (filterModel.door_sensor.values.length == 0) {
          this.filterValues.set('door_sensor', 'null');
        } else {

          this.filterValues.set('door_sensor', filterModel.door_sensor.values.toString());
        }
      }
      if (filterModel.wheel_end != undefined && filterModel.wheel_end.values != null && filterModel.wheel_end.values != undefined) {
        if (filterModel.wheel_end.values.length == 0) {
          this.filterValues.set('wheel_end', 'null');
        } else {
          this.filterValues.set('wheel_end', filterModel.wheel_end.values.toString());
        }
      }
      if (filterModel.air_tank != undefined && filterModel.air_tank.values != null && filterModel.air_tank.values != undefined) {
        if (filterModel.air_tank.values.length == 0) {
          this.filterValues.set('air_tank', 'null');
        } else {

          this.filterValues.set('air_tank', filterModel.air_tank.values.toString());
        }
      }
      if (filterModel.regulator != undefined && filterModel.regulator.values != null && filterModel.regulator.values != undefined) {
        if (filterModel.regulator.values.length == 0) {
          this.filterValues.set('regulator', 'null');
        } else {
          this.filterValues.set('regulator', filterModel.regulator.values.toString());
        }
      }
      if (filterModel.reciever != undefined && filterModel.reciever.values != null && filterModel.reciever.values != undefined) {
        if (filterModel.reciever.values.length == 0) {
          this.filterValues.set('reciever', 'null');
        } else {
          this.filterValues.set('reciever', filterModel.reciever.values.toString());
        }
      }


      let jsonObject = {};
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
      case 'asset_id': {
        this.column = 'asset.assignedName';
        this.order = sortModel[0].sort;
        break;
      }
      case 'product_name': {
        this.column = 'gateway.productName';
        this.order = sortModel[0].sort;
        break;
      }
      case 'status': {
        this.column = 'asset.status';
        this.order = sortModel[0].sort;
        break;
      }
      case 'device_id': {
        this.column = 'gateway.imei';
        this.order = sortModel[0].sort;
        break;
      }
      case 'user_name': {
        this.column = 'createdBy.lastName';
        this.order = sortModel[0].sort;
        break;
      }
      case 'organisation_name': {
        this.column = 'createdBy.company.companyName';
        this.order = sortModel[0].sort;
        break;
      }

      case 'installed': {
        this.column = 'dateEnded';
        this.order = sortModel[0].sort;
        break;
      }

      case 'updated_at': {
        this.column = 'updatedOn';
        this.order = sortModel[0].sort;
        break;
      }

      case 'cargo_sensor': {
        this.column = 'status';
        this.order = sortModel[0].sort;
        break;
      }

      case 'pct_cargo_sensor': {
        this.column = 'status';
        this.order = sortModel[0].sort;
        break;
      }
      case 'cargo_camera_sensor': {
        this.column = 'status';
        this.order = sortModel[0].sort;
        break;
      }
      case 'abssensor': {
        this.column = 'status';
        this.order = sortModel[0].sort;
        break;
      }
      case 'regulator': {
        this.column = 'status';
        this.order = sortModel[0].sort;
        break;
      }

      case 'wheel_end': {
        this.column = 'status';
        this.order = sortModel[0].sort;
        break;
      }

      case 'tpms': {
        this.column = 'status';
        this.order = sortModel[0].sort;
        break;
      }

      case 'light_sentry': {
        this.column = 'status';
        this.order = sortModel[0].sort;

        break;
      }

      case 'atis_sensor': {
        this.column = 'status';
        this.order = sortModel[0].sort;
        break;
      }
      case 'door_sensor': {
        this.column = 'status';
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



  // checkbox(item: any) {
  //   if (this.selectedIdsToDelete.find(x => x == item.asset_uuid)) {
  //     this.selectedIdsToDelete.splice(this.selectedIdsToDelete.indexOf(item.asset_uuid), 1)
  //   }
  //   else {
  //     this.selectedIdsToDelete.push(item.asset_uuid);
  //   }
  //   if( this.assetList.length >0 &&  this.assetList.length === this.selectedIdsToDelete.length){
  //     this.isChecked =true;
  //   }else
  //   this.isChecked =false;
  // }


  reloadComponent() {
    let currentUrl = this.router.url;
    this.router.navigateByUrl('/', { skipLocationChange: true }).then(() =>
      this.router.navigate([currentUrl]));
  }

  clearFilter() {
    this.column = 'dateStarted';
    this.order = 'asc';
    this.paramList.api.setFilterModel(null);
    // params.column.columnApi.applyColumnState({ defaultState: { sort: null } });
    // params.column.columnApi.resetColumnState();
    this.paramList.api.onFilterChanged();
    // sessionStorage.setItem("asset_list_grid_column_state_"+this.assetsService.assetCompanyId, null);
    sessionStorage.setItem('asset_list_grid_filter_state_' + this.companyUuid, null);
  }

}




var filterParamsStatus = {
  values: function (params) {
    setTimeout(function () {
      params.success(['Pass', 'Fail', 'N/A', 'Pending']);
    }, 2000);
  },
};

var filterParamsAssetStatus = {
  values: function (params) {
    setTimeout(function () {
      params.success(['Partial', 'Pending', 'Started', 'Active', 'Inactive', 'Deleted', 'Error']);
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
        'StealthNet',
        'freight-l',
        'Smart9',
        'PSI TireView - 8 Tires Excluding CP Hoses',
        'Cargo Sensor (Wireless)',
        'Smart ABS Harness (Includes ABS Fault Reader)',
        'Lite Sentry',
        'Door Sensor (Wireless)',
        'ATIS + Hardware Sensor',
        'Air Tank Sensor',
        'Wheel End Sensor',
        'Regulator Sensor',
        'LampCheck ABS',
        'LampCheck ATIS',
        'PCT Cargo Sensor',
        'MicroSP Wireless Receiver',
        'MicroSP Air Tank',
        'MicroSP ATIS Regulator',
        'MicroSP Wired Receiver',
        'MicroSP TPMS Sensor',
        'MicroSP Flowthrough TPMS (Outer)',
        'MicroSP Flowthrough TPMS (Inner)',
        'PCT Cargo Camera',
        'PCT Cargo Camera G2',
        'PCT Cargo Camera G3',
        'Maxon MaxLink',
        'SmartPair',
        'arrow-l',
        'sabre-l',
        'katana-h',
        'dagger-lg',
        'cutlass-l',
        'arrow-h',
        'arrow-g',
        'arrow-lgc',
        'arrow-lg',
        'arrow-c',
        'dagger-h',
        'dagger-l',
        'talon-lte',
        'arrow-ma',
        'Smart7700',
        'arrow-laq',
        'Smart7',
        'Sabre']);
    }, 2000);
  },
};
