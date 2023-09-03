
import { AgGridAngular } from '@ag-grid-community/angular';
import { ClientSideRowModelModule, ColumnsToolPanelModule, GridOptions, IGetRowsParams, MenuModule, Module, MultiFilterModule, RowGroupingModule } from '@ag-grid-enterprise/all-modules';
import { Component, OnInit, ViewChild } from '@angular/core';
import { FormControl } from '@angular/forms';
import { Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { AssetsService } from '../assets.service';
import { ATCommandService } from '../atcommand.service';
import { CompaniesService } from '../companies.service';
import { GatewayListService } from '../gateway-list.service';
import { BtnCellRendererForGatewayDetails } from '../gateway-list/btn-cell-renderer.component';
import { ResetInstalationStatusService } from '../reset-installation-status/reset-installation-status.service';
import { CustomDateComponent } from '../ui-elements/custom-date-component.component';
import { CustomHeader } from '../ui-elements/custom-header.component';
import { PagerService } from '../util/pagerService';
import { PaginationConstants } from '../util/paginationConstants';
declare var $: any;
import * as _ from 'underscore';
@Component({
  selector: 'app-campaign-tab',
  templateUrl: './campaign-tab.component.html',
  styleUrls: ['./campaign-tab.component.css']
})
export class CampaignTabComponent implements OnInit {

  sortFlag;
  currentPage: any;
  showingpage;
  totalpage;
  gatewayList: any[] = [];
  version_migration_detail_list = [];
  totalRecords = 0;
  currentEndRecord = 0;
  currentStartRecord = 0;
  pagedItems: any[];
  private allItems: any;
  pageLimit = 5;
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
  column = 'imei';
  order = 'asc';
  userCompanyType;
  isSuperAdmin;
  isRoleCustomerManager;
  //parameters for counting records
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
  public gridApiSecond;
  public gridColumnApi;
  public cacheOverflowSize = 2;
  public maxConcurrentDatasourceRequests = 2;
  public infiniteInitialRowCount = 2;
  public defaultColDef = {
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
  public paramList;
  public components;
  rowData: any;
  public columnDefsSecond;
  public defaultColDefSecond;
  public rowData1: [];
  public modules: Module[] = [
    ClientSideRowModelModule,
    MenuModule,
    ColumnsToolPanelModule,
    MultiFilterModule,
    RowGroupingModule,
  ];
  rowModelType = 'infinite';
  rowClassRules;

  //Pagination Save State
  isFirstTimeLoad = false;
  isPaginationStateRecovered = false;
  filterModelCountFilter: any;
  userNameList: String[];
  companies: any[];
  sequenceData = [];
  company: any;
  selectedCompany = new FormControl();
  selectDays = new FormControl(0);
  companyUuid = '1';
  companyname: any;
  days = 0;
  selectedtoDateRange = [];
  selected: number;
  selectedIdsToDelete: string[] = [];
  isChecked: boolean = false;
  filterParamsCustomers = {
    values: params => params.success(this.getFilterValuesData())
  }
  containsFilterParams = {
    filterOptions: [
      'contains',
    ],
    suppressAndOrCondition: true
  };



  columnDefs = [
    {
      headerName: 'Campaign', field: 'campaing_name', sortable: true, filter: 'agTextColumnFilter', floatingFilter: true, filterParams: this.containsFilterParams, width: 200,
      cellRenderer: function (params) {
        if (params.data != undefined && params.data.campaing_name != null) {
          return '<div class="pointer" style="color:#4ba9d7;"><a><b>' + params.data.campaing_name + '<b></a></div>'

        }
      }
    },
    {
      headerName: 'Status', field: 'campaing_status', sortable: true, filter: 'agTextColumnFilter', floatingFilter: true, filterParams: this.containsFilterParams,
      cellRenderer: function (params) {
        if (params.data != undefined && params.data.campaing_status != null) {
          return '<div>' + params.data.campaing_status + '</div>'
        }
      }
    },
    {
      headerName: 'Last Step', field: 'last_step', hide: true, sortable: true, filter: 'agTextColumnFilter', filterParams: this.containsFilterParams,
      cellRenderer: function (params) {
        if (params.data != undefined && params.data.last_step != null) {
          return '<div>' + params.data.last_step + '</div>'
        }
      }

    },
    {
      headerName: 'Last Step Time', field: 'last_step_time', sortable: true, filter: 'agTextColumnFilter', filterParams: this.containsFilterParams, width: 180, floatingFilter: true,
      cellRenderer: function (params) {
        if (params.data != undefined && params.data.last_step_time != null) {
          return '<div>' + params.data.last_step_time + '</div>'
        }
      }
    },
    {
      headerName: 'Bin', field: 'bin', sortable: true, filter: 'agTextColumnFilter', width: 180, floatingFilter: true, filterParams: this.containsFilterParams,
      cellRenderer: function (params) {
        if (params.data != undefined && params.data.bin_version != null) {
          return '<div>' + params.data.bin_version + '</div>'
        }
      }
    },
    {
      headerName: 'App', field: 'app_version', sortable: true, filter: 'agTextColumnFilter', width: 240, floatingFilter: true, filterParams: this.containsFilterParams,
      cellRenderer: function (params) {
        if (params.data != undefined && params.data.app_version != null) {
          return '<div>' + params.data.app_version + '</div>'
        }
      }
    },
    {
      headerName: 'MCU', field: 'mcu', sortable: true, width: 260, filter: 'agTextColumnFilter', floatingFilter: true, filterParams: this.containsFilterParams,
      cellRenderer: function (params) {
        if (params.data != undefined && params.data.mcu != null) {
          return '<div>' + params.data.mcu + '</div>'
        }
      }
    },
    {
      headerName: 'BLE', field: 'ble_version', sortable: true, width: 260, filter: 'agTextColumnFilter', floatingFilter: true, filterParams: this.containsFilterParams,
      cellRenderer: function (params) {
        if (params.data != undefined && params.data.ble_version != null) {
          return '<div>' + params.data.ble_version + '</div>'
        }
      }
    },
    {
      headerName: 'Config1', field: 'config1', sortable: true, width: 260, filter: 'agTextColumnFilter', floatingFilter: true, filterParams: this.containsFilterParams,
      cellRenderer: function (params) {
        if (params.data != undefined && params.data.config1_civ != null) {
          return '<div>' + params.data.config1_civ + '</div>'
        }
      }
    },
    // {
    //   headerName: 'Action', field: 'action', sortable: false, filter: 'agNumberColumnFilter', floatingFilter: true, floatingFilterComponent: 'customClearFloatingFilter',
    //   floatingFilterComponentParams: {
    //     suppressFilterButton: true
    //   },
    //   cellRenderer: 'btnCellRenderer', width: 140, minWidth: 140, pinned: 'right'
    // }

  ];



  campaign_report: any;
  campaignList: any[];
  currentCampaign: any;
  rowParam: any;
  deviceAssetDetails:any;
  version_migration_detail = [];
  gridColumnApiSecond: any;
  constructor(public assetsService: AssetsService, public atcommandService: ATCommandService, public gatewayListService: GatewayListService, public companiesService: CompaniesService,
    public router: Router) {

      this.deviceAssetDetails=JSON.parse(sessionStorage.getItem('DEVICE_ASSET_DETAILS'));
    // if (sessionStorage.getItem('gatewayListsize') != undefined) {
    //   this.pageLimit = Number(sessionStorage.getItem('gatewayListsize'));
    // }

    var containsFilterParams = {
      filterOptions: [
        'contains',
      ],
      suppressAndOrCondition: true
    };

    this.rowClassRules = {
      'high-light-row-of-sequence': function (params) {
        var stepOrderNumber = params.data.step_order_number;
        return stepOrderNumber == 'Baseline' || stepOrderNumber == 'End State';
      }
    };
    this.columnDefsSecond = [
      {
        headerName: 'Step #', field: 'step_order_number', rowDrag: true, editable: false,
        minWidth: 200,
        tooltipField: 'tooltip',
        cellRenderer: params => {
          if (params != undefined && params.value != undefined && params.value == "Baseline") {
            return `Baseline  <span class="tooltips"><i class="fa fa-info-circle tool"
            data-tip="The Package You set as a baseline will limit the campaign to only those gateways in the selected list that currently have this configuration."
            tabindex="1"></i>
        </span>`;
          } else if (params != undefined && params.value != undefined && params.value == "End State") {
            return `End State  <span class="tooltips"><i class="fa fa-info-circle tool"
            data-tip="The end state defines the expected configuration of devices after this campaign has been completed."
            tabindex="2"></i>
        </span>`;
          } else {
            return params.value;
          }

        }
      },
      {
        headerName: 'Package Name',
        field: 'package.package_name',
        minWidth: 180,
        // cellEditor: 'agRichSelectCellEditor',
        // cellEditorParams: cellCellEditorParams,
        filter: 'agTextColumnFilter',
        filterParams: containsFilterParams,
        onCellClicked: params => {
          console.log(params);
          this.rowParam = null;
          $('#clickModel').modal('show');
          this.rowParam = params;
        },
        cellRenderer: params => {
          if (params.data.package != null && params.data.package != undefined && params.data.package.package_name != null && params.data.package.package_name != "") {
            return params.data.package.package_name;
          } else {
            return 'Click to Choose';
          }
        }
      }, {
        headerName: 'Command',
        field: 'at_command',
        minWidth: 220,
        filter: 'agTextColumnFilter',
        filterParams: containsFilterParams,
        cellRenderer: params => {
          if (params.data.at_command != null && params.data.at_command != "") {
            return params.data.at_command;
          } else if (params.data.step_order_number == "End State") {
            return '';
          } else {
            return 'Click to Add Command';
          }
        },
        editable: (params) => {
          return params.data.step_order_number !== "End State";
        }
      }, {
        headerName: 'Created By',
        field: 'package.created_by',
        minWidth: 150,
        editable: false,
        hide: true,
        filter: 'agTextColumnFilter',
        filterParams: containsFilterParams
      },
      {
        headerName: 'Created',
        field: 'package.created_at',
        minWidth: 150,
        editable: false,
        hide: true,
        filter: 'agTextColumnFilter',
        filterParams: containsFilterParams
      },
      {
        headerName: 'BIN', field: 'package.bin_version', editable: false, filter: 'agTextColumnFilter',
        filterParams: containsFilterParams,
        width: 150,
        cellRenderer: params => {
          if (params.data.package != undefined && params.data.package != null
            && params.data.package.bin_version != undefined && params.data.package.bin_version != null) {
            if (this.checkValueIsEqual(params, 'package.bin_version')) {
              return '<b>' + params.data.package.bin_version + '</b>';
            } else {
              return params.data.package.bin_version;
            }

          } else {
            return '';
          }
        }
      },
      {
        headerName: 'App', field: 'package.app_version', editable: false, filter: 'agTextColumnFilter',
        filterParams: containsFilterParams,
        width: 150,
        cellRenderer: params => {
          if (params.data.package != undefined && params.data.package != null
            && params.data.package.app_version != undefined && params.data.package.app_version != null) {
            if (this.checkValueIsEqual(params, 'package.app_version')) {
              return '<b>' + params.data.package.app_version + '</b>';
            } else {
              return params.data.package.app_version;
            }

          } else {
            return '';
          }
        }
      },
      {
        headerName: 'MCU', field: 'package.mcu_version', editable: false, filter: 'agTextColumnFilter',
        filterParams: containsFilterParams,
        width: 150,
        cellRenderer: params => {
          if (params.data.package != undefined && params.data.package != null
            && params.data.package.mcu_version != undefined && params.data.package.mcu_version != null) {
            if (this.checkValueIsEqual(params, 'package.mcu_version')) {
              return '<b>' + params.data.package.mcu_version + '</b>';
            } else {
              return params.data.package.mcu_version;
            }

          } else {
            return '';
          }
        }
      },
      {
        headerName: 'BLE', field: 'package.ble_version', editable: false, filter: 'agTextColumnFilter',
        filterParams: containsFilterParams,
        width: 150,
        cellRenderer: params => {
          if (params.data.package != undefined && params.data.package != null
            && params.data.package.ble_version != undefined && params.data.package.ble_version != null) {
            if (this.checkValueIsEqual(params, 'package.ble_version')) {
              return '<b>' + params.data.package.ble_version + '</b>';
            } else {
              return params.data.package.ble_version;
            }

          } else {
            return '';
          }
        }
      },
      {
        headerName: 'Config1', field: 'package.config1', editable: false, filter: 'agTextColumnFilter', filterParams: containsFilterParams,
        width: 180,
        cellRenderer: params => {
          if (params.data.package != undefined && params.data.package != null
            && params.data.package.config1 != undefined && params.data.package.config1 != null) {
            if (this.checkValueIsEqual(params, 'package.config1')) {
              return '<b>' + params.data.package.config1 + '</b>';
            } else {
              return params.data.package.config1;
            }

          } else {
            return '';
          }
        }
      },
      {
        headerName: 'Config1 CRC', field: 'package.config1_crc', editable: false, filter: 'agTextColumnFilter', filterParams: containsFilterParams,
        width: 180,
        cellRenderer: params => {
          if (params.data.package != undefined && params.data.package != null
            && params.data.package.config1_crc != undefined && params.data.package.config1_crc != null) {
            if (this.checkValueIsEqual(params, 'package.config1_crc')) {
              return '<b>' + params.data.package.config1_crc + '</b>';
            } else {
              return params.data.package.config1_crc;
            }

          } else {
            return '';
          }
        }
      },
      {
        headerName: 'Config2', field: 'package.config2', editable: false, filter: 'agTextColumnFilter', filterParams: containsFilterParams,
        width: 180,
        cellRenderer: params => {
          if (params.data.package != undefined && params.data.package != null
            && params.data.package.config2 != undefined && params.data.package.config2 != null) {
            if (this.checkValueIsEqual(params, 'package.config2')) {
              return '<b>' + params.data.package.config2 + '</b>';
            } else {
              return params.data.package.config2;
            }

          } else {
            return '';
          }
        }
      },
      {
        headerName: 'Config2 CRC', field: 'package.config2_crc', editable: false, filter: 'agTextColumnFilter', filterParams: containsFilterParams,
        width: 180,
        cellRenderer: params => {
          if (params.data.package != undefined && params.data.package != null
            && params.data.package.config2_crc != undefined && params.data.package.config2_crc != null) {
            if (this.checkValueIsEqual(params, 'package.config2_crc')) {
              return '<b>' + params.data.package.config2_crc + '</b>';
            } else {
              return params.data.package.config2_crc;
            }

          } else {
            return '';
          }
        }
      },
      {
        headerName: 'Config3', field: 'package.config3', editable: false, filter: 'agTextColumnFilter', filterParams: containsFilterParams,
        width: 180,
        cellRenderer: params => {
          if (params.data.package != undefined && params.data.package != null
            && params.data.package.config3 != undefined && params.data.package.config3 != null) {
            if (this.checkValueIsEqual(params, 'package.config3')) {
              return '<b>' + params.data.package.config3 + '</b>';
            } else {
              return params.data.package.config3;
            }

          } else {
            return '';
          }
        }
      },
      {
        headerName: 'Config3 CRC', field: 'package.config3_crc', editable: false, filter: 'agTextColumnFilter', filterParams: containsFilterParams,
        width: 180,
        cellRenderer: params => {
          if (params.data.package != undefined && params.data.package != null
            && params.data.package.config3_crc != undefined && params.data.package.config3_crc != null) {
            if (this.checkValueIsEqual(params, 'package.config3_crc')) {
              return '<b>' + params.data.package.config3_crc + '</b>';
            } else {
              return params.data.package.config3_crc;
            }

          } else {
            return '';
          }
        }
      },
      {
        headerName: 'Config4', field: 'package.config4', editable: false, filter: 'agTextColumnFilter', filterParams: containsFilterParams,
        width: 180,
        cellRenderer: params => {
          if (params.data.package != undefined && params.data.package != null
            && params.data.package.config4 != undefined && params.data.package.config4 != null) {
            if (this.checkValueIsEqual(params, 'package.config4')) {
              return '<b>' + params.data.package.config4 + '</b>';
            } else {
              return params.data.package.config4;
            }

          } else {
            return '';
          }
        }
      },
      {
        headerName: 'Config4 CRC', field: 'package.config4_crc', editable: false, filter: 'agTextColumnFilter', filterParams: containsFilterParams,
        width: 180,
        cellRenderer: params => {
          if (params.data.package != undefined && params.data.package != null
            && params.data.package.config4_crc != undefined && params.data.package.config4_crc != null) {
            if (this.checkValueIsEqual(params, 'package.config4_crc')) {
              return '<b>' + params.data.package.config4_crc + '</b>';
            } else {
              return params.data.package.config4_crc;
            }

          } else {
            return '';
          }
        }
      },
      {
        headerName: 'Device Type', field: 'package.device_type', sortable: true, filter: 'agTextColumnFilter', filterParams: containsFilterParams, editable: false,
        width: 180,
        cellRenderer: params => {
          if (params.data.package != undefined && params.data.package != null
            && params.data.package.device_type != undefined && params.data.package.device_type != null) {
            if (this.checkValueIsEqual(params, 'package.device_type')) {
              return '<b>' + params.data.package.device_type + '</b>';
            } else {
              return params.data.package.device_type;
            }

          } else {
            return '';
          }
        }
      },
      {
        headerName: 'LiteSentry HW', field: 'package.lite_sentry_hardware', sortable: true, filter: 'agTextColumnFilter', filterParams: containsFilterParams, editable: false,
        width: 180,
        cellRenderer: params => {
          if (params.data.package != undefined && params.data.package != null
            && params.data.package.lite_sentry_hardware != undefined && params.data.package.lite_sentry_hardware != null) {
            if (this.checkValueIsEqual(params, 'package.lite_sentry_hardware')) {
              return '<b>' + params.data.package.lite_sentry_hardware + '</b>';
            } else {
              return params.data.package.lite_sentry_hardware;
            }

          } else {
            return '';
          }
        }
      },
      {
        headerName: 'LiteSentry App', field: 'package.lite_sentry_app', sortable: true, filter: 'agTextColumnFilter', filterParams: containsFilterParams, editable: false,
        width: 180,
        cellRenderer: params => {
          if (params.data.package != undefined && params.data.package != null
            && params.data.package.lite_sentry_app != undefined && params.data.package.lite_sentry_app != null) {
            if (this.checkValueIsEqual(params, 'package.lite_sentry_app')) {
              return '<b>' + params.data.package.lite_sentry_app + '</b>';
            } else {
              return params.data.package.lite_sentry_app;
            }

          } else {
            return '';
          }
        }
      },
      {
        headerName: 'LiteSentry Boot', field: 'package.lite_sentry_boot', sortable: true, filter: 'agTextColumnFilter', filterParams: containsFilterParams, editable: false,
        width: 180,
        cellRenderer: params => {
          if (params.data.package != undefined && params.data.package != null
            && params.data.package.lite_sentry_boot != undefined && params.data.package.lite_sentry_boot != null) {
            if (this.checkValueIsEqual(params, 'package.lite_sentry_boot')) {
              return '<b>' + params.data.package.lite_sentry_boot + '</b>';
            } else {
              return params.data.package.lite_sentry_boot;
            }

          } else {
            return '';
          }
        }
      },
      {
        headerName: 'MicroSP MCU', field: 'package.microsp_mcu', sortable: true, filter: 'agTextColumnFilter', filterParams: containsFilterParams, editable: false,
        width: 180,
        cellRenderer: params => {
          if (params.data.package != undefined && params.data.package != null
            && params.data.package.microsp_mcu != undefined && params.data.package.microsp_mcu != null) {
            if (this.checkValueIsEqual(params, 'package.microsp_mcu')) {
              return '<b>' + params.data.package.microsp_mcu + '</b>';
            } else {
              return params.data.package.microsp_mcu;
            }

          } else {
            return '';
          }
        }
      },
      {
        headerName: 'MicroSP App', field: 'package.microsp_app', sortable: true, filter: 'agTextColumnFilter', filterParams: containsFilterParams, editable: false,
        width: 180,
        cellRenderer: params => {
          if (params.data.package != undefined && params.data.package != null
            && params.data.package.microsp_app != undefined && params.data.package.microsp_app != null) {
            if (this.checkValueIsEqual(params, 'package.microsp_app')) {
              return '<b>' + params.data.package.microsp_app + '</b>';
            } else {
              return params.data.package.microsp_app;
            }

          } else {
            return '';
          }
        }
      },

      {
        headerName: 'Cargo (Max) HW', field: 'package.cargo_maxbotix_hardware', sortable: true, filter: 'agTextColumnFilter', filterParams: containsFilterParams, editable: false,
        width: 180,
        cellRenderer: params => {
          if (params.data.package != undefined && params.data.package != null
            && params.data.package.cargo_maxbotix_hardware != undefined && params.data.package.cargo_maxbotix_hardware != null) {
            if (this.checkValueIsEqual(params, 'package.cargo_maxbotix_hardware')) {
              return '<b>' + params.data.package.cargo_maxbotix_hardware + '</b>';
            } else {
              return params.data.package.cargo_maxbotix_hardware;
            }

          } else {
            return '';
          }
        }
      },
      {
        headerName: 'Cargo (Max) FW', field: 'package.cargo_maxbotix_firmware', sortable: true, filter: 'agTextColumnFilter', filterParams: containsFilterParams, editable: false,
        width: 180,
        cellRenderer: params => {
          if (params.data.package != undefined && params.data.package != null
            && params.data.package.cargo_maxbotix_firmware != undefined && params.data.package.cargo_maxbotix_firmware != null) {
            if (this.checkValueIsEqual(params, 'package.cargo_maxbotix_firmware')) {
              return '<b>' + params.data.package.cargo_maxbotix_firmware + '</b>';
            } else {
              return params.data.package.cargo_maxbotix_firmware;
            }

          } else {
            return '';
          }
        }
      },
      {
        headerName: 'Cargo (RIOT) HW', field: 'package.cargo_riot_hardware', sortable: true, filter: 'agTextColumnFilter', filterParams: containsFilterParams, editable: false,
        width: 180,
        cellRenderer: params => {
          if (params.data.package != undefined && params.data.package != null
            && params.data.package.cargo_riot_hardware != undefined && params.data.package.cargo_riot_hardware != null) {
            if (this.checkValueIsEqual(params, 'package.cargo_riot_hardware')) {
              return '<b>' + params.data.package.cargo_riot_hardware + '</b>';
            } else {
              return params.data.package.cargo_riot_hardware;
            }

          } else {
            return '';
          }
        }
      },
      {
        headerName: 'Cargo (RIOT) FW', field: 'package.cargo_riot_firmware', sortable: true, filter: 'agTextColumnFilter', filterParams: containsFilterParams, editable: false,
        width: 180,
        cellRenderer: params => {
          if (params.data.package != undefined && params.data.package != null
            && params.data.package.cargo_riot_firmware != undefined && params.data.package.cargo_riot_firmware != null) {
            if (this.checkValueIsEqual(params, 'package.cargo_riot_firmware')) {
              return '<b>' + params.data.package.cargo_riot_firmware + '</b>';
            } else {
              return params.data.package.cargo_riot_firmware;
            }

          } else {
            return '';
          }
        }
      }
    ];
    this.defaultColDefSecond = {
      width: 90,
      sortable: true,
      filter: true,
      editable: true,
      resizable: true
    };

    this.gridOptions = {
      cacheBlockSize: this.pageLimit,
      paginationPageSize: this.pageLimit,
      rowModelType: 'infinite',
    }
    this.frameworkComponents = {
      btnCellRenderer: BtnCellRendererForGatewayDetails,
      agDateInput: CustomDateComponent,
      agColumnHeader: CustomHeader
    }
    this.components = {
      customClearFloatingFilter: this.getClearFilterFloatingComponent(),
    };



  }

  ngOnInit() {
    this.currentDeviceConfiguration();
    this.getCampaignInstalledDevice();
    console.log("data is ", this.sequenceData)
  }


  checkValueIsEqual(params, fieldName) {
    let flag = false;
    if (params.data.step_order_number != "Baseline") {
      for (var i = 0; i < this.sequenceData.length; i++) {
        if (this.sequenceData[i].step_order_number == params.data.step_order_number) {
          if (fieldName == 'package.bin_version') {
            if (this.sequenceData[i].package.bin_version != this.sequenceData[i - 1].package.bin_version) {
              flag = true;
              break;
            }
          } else if (fieldName == 'package.app_version') {
            if (this.sequenceData[i].package.app_version != this.sequenceData[i - 1].package.app_version) {
              flag = true;
              break;
            }
          } else if (fieldName == 'package.mcu_version') {
            if (this.sequenceData[i].package.mcu_version != this.sequenceData[i - 1].package.mcu_version) {
              flag = true;
              break;
            }
          } else if (fieldName == 'package.ble_version') {
            if (this.sequenceData[i].package.ble_version != this.sequenceData[i - 1].package.ble_version) {
              flag = true;
              break;
            }
          } else if (fieldName == 'package.config1') {
            if (this.sequenceData[i].package.config1 != this.sequenceData[i - 1].package.config1) {
              flag = true;
              break;
            }
          } else if (fieldName == 'package.config2') {
            if (this.sequenceData[i].package.config2 != this.sequenceData[i - 1].package.config2) {
              flag = true;
              break;
            }
          } else if (fieldName == 'package.config3') {
            if (this.sequenceData[i].package.config3 != this.sequenceData[i - 1].package.config3) {
              flag = true;
              break;
            }
          } else if (fieldName == 'package.config4') {
            if (this.sequenceData[i].package.config4 != this.sequenceData[i - 1].package.config4) {
              flag = true;
              break;
            }
          }
        }
      }
    }
    return flag;
  }

  getCampaignInstalledDevice() {
    var deviceID = sessionStorage.getItem('device_id')
    this.gatewayListService.getCampaignInstalledDevice(deviceID).subscribe(
      data => {
        console.log(data)
      },
      error => {
        console.log(error)
      }
    );
  }
  currentDeviceConfiguration() {
    var deviceID = sessionStorage.getItem('device_id')
    this.gatewayListService.getDeviceCampaignConfiguration(deviceID).subscribe(
      data => {
        if (data) {
          this.campaign_report = data.device_report;

        }
      },
      error => {
        console.log(error);
      }

    );
  }


  onGridReadySecond(params) {
    this.gridApiSecond = params.api;
    this.gridColumnApiSecond = params.columnApi;
    var deviceId = sessionStorage.getItem('device_id');
    this.gatewayListService.getCurrentCampaign(deviceId).subscribe(
      data => {
        console.log(data);
        this.currentCampaign = data.body.campaign_name;
        this.version_migration_detail = [];
        this.version_migration_detail = data.body.version_migration_detail_dto_list;
        if (this.version_migration_detail != null && this.version_migration_detail.length > 0) {
          for (var i = 0; i < this.version_migration_detail.length; i++) {
            var version_migration_detail_obj = {
              "at_command": this.version_migration_detail[i].at_command,
              "step_order_number": this.version_migration_detail[i].step_order_number,
              "step_id": this.version_migration_detail[i].step_id,
              "package": this.version_migration_detail[i].from_package,
              "tooltip": ""
            }

            if (i == 0) {
              version_migration_detail_obj.step_order_number = "Baseline";
              version_migration_detail_obj.tooltip = "The Package You set as a baseline will limit the campaign to only those gateways in the selected list that currently have this configuration."
            }

            this.version_migration_detail_list.push(version_migration_detail_obj);

            if (i == this.version_migration_detail.length - 1) {
              var version_migration_detail_obj_new = {
                "at_command": "",
                "step_order_number": "End State",
                "package": this.version_migration_detail[i].to_package,
                "tooltip": "The end state defines the expected configuration of devices after this campaign has been completed."
              }

              this.version_migration_detail_list.push(version_migration_detail_obj_new);
            }
          }

          this.sequenceData = this.version_migration_detail_list;
          console.log("sequence Dtaa");
          console.log(this.sequenceData);
          var initVar = 1;
          for (var i = 0; i < this.sequenceData.length; i++) {
            if (this.sequenceData[i].step_order_number != null && this.sequenceData[i].step_order_number != undefined && this.sequenceData[i].step_order_number == "Baseline" || this.sequenceData[i].step_order_number == "End State") {
              continue;
            } else {
              this.sequenceData[i].step_order_number = initVar;
              initVar++;
            }
          }
          this.gridApiSecond.setRowData([]);
          this.gridApiSecond.setRowData(this.sequenceData);
        }
      },
      error => {
        console.log(error)
      });

  }

  callChangeSequence() {
    var initVar = 1;
    for (var i = 0; i < this.sequenceData.length; i++) {
      if (this.sequenceData[i].step_order_number != null && this.sequenceData[i].step_order_number != undefined && this.sequenceData[i].step_order_number == "Baseline" || this.sequenceData[i].step_order_number == "End State") {
        continue;
      } else {
        this.sequenceData[i].step_order_number = initVar;
        initVar++;
      }
    }
    this.gridApiSecond.setRowData([]);
    this.gridApiSecond.setRowData(this.sequenceData);
  }

  onGridReady(params) {
    this.gridOptions.api.setDomLayout('autoHeight');
    this.paramList = params;
    this.gridApi = params.api;
    this.gridApi.setRowData([]);
    this.gridColumnApi = params.columnApi;
    // let gridSavedState = sessionStorage.getItem("gateway_list_grid_column_state" + this.gatewayListService.organisationId);
    // if (gridSavedState) {
    //   this.gridColumnApi.setColumnState(JSON.parse(gridSavedState));
    // }
    // let filterSavedState = sessionStorage.getItem("gateway_list_grid_filter_state" + this.gatewayListService.organisationId);
    // if (filterSavedState) {
    //   this.gridApi.setFilterModel(JSON.parse(filterSavedState));
    // } else {
    // }

    var datasource = {
      getRows: (params: IGetRowsParams) => {
        this.loading = true;
        //this.companyname=this.selectedCompany;
        this.sortForAgGrid(params.sortModel);
        this.filterForAgGrid(params.filterModel);
        var deviceID = sessionStorage.getItem('device_id')
        this.gatewayListService.getDeviceCampaignByIMEI(deviceID).subscribe(
          data => {
            console.log('Data is ');
            console.log(data)
            // this.currentCampaign = data[0];
            var datas = this.filterInLocal(params.filterModel, data);
            this.rowData = this.sortingInLocal(datas, params.sortModel);
            //   this.rowData = data;
            this.total_number_of_records = data.total_key;
            params.successCallback(this.rowData, data.total_key);
          },
          error => {
            console.log(error)
          }

        );
      }
    }
    this.gridApi.setDatasource(datasource);
  }

  isEmptyObject(obj) {
    return (obj && (Object.keys(obj).length === 0));
  }

  sortingInLocal(data: any, sortModel: any): any {
    if (sortModel && sortModel.length > 0) {
      let sortedArray = _.sortBy(data, sortModel[0].colId);
      if (sortModel[0].sort == "desc") {
        sortedArray.reverse();
      }
      return sortedArray;
    } else {
      return data;
    }
  }
  filterInLocal(filterModel: any, data: any): any {
    if (filterModel != undefined && filterModel != null && !this.isEmptyObject(filterModel)) {
      const elasticModelFilters = JSON.parse(JSON.stringify(filterModel))
      filterModel = elasticModelFilters
      const Okeys = Object.keys(filterModel);
      var keys: any[] = [];
      var values: any[] = [];
      for (var i = 0; i < Okeys.length; i++) {
        keys.push(Okeys[i]);
        values.push(filterModel[Okeys[i]].filter);
      }
      return data.filter((item) =>
        keys.some((key) =>
          values.some((val) => item[key].toLowerCase().includes(val.toLowerCase()))));

    } else {
      return data;
    }
  }
  filterForAgGrid(filterModel) {
    this.gridApi.paginationCurrentPage = 0;
    if (filterModel != undefined) {
      this.filterValuesJsonObj = {};
      this.filterValues = new Map();
      if (filterModel.imei != undefined && filterModel.imei.filter != null && filterModel.imei.filter != undefined && filterModel.imei.filter != "") {
        this.filterValues.set("imei", filterModel.imei.filter);
      }
      if (filterModel.campaing_name != undefined && filterModel.campaing_name.values != null && filterModel.campaing_name.values != undefined) {
        if (filterModel.campaing_name.values.length == 0) {
          this.filterValues.set("campaingName", "null");
        } else {
          this.filterValues.set("productName", filterModel.campaing_name.values.toString());
        }
      }
      if (filterModel.campaing_status != undefined && filterModel.campaing_status.values != null && filterModel.campaing_status.values != undefined) {
        if (filterModel.campaing_status.values.length == 0) {
          this.filterValues.set("campaing_status", "null");
        } else {
          this.filterValues.set("campaing_status", filterModel.campaing_status.values.toString());
        }
      }
      if (filterModel.bin_version != undefined && filterModel.bin_version.filter != null && filterModel.bin_version.filter != undefined && filterModel.bin_version.filter != "") {
        this.filterValues.set("bin_version", filterModel.bin_version.filter);
      }
      if (filterModel.ble_version != undefined && filterModel.ble_version.values != null && filterModel.ble_version.values != undefined) {
        if (filterModel.ble_version.values.length == 0) {
          this.filterValues.set("ble_version", "null");
        } else {
          this.filterValues.set("ble_version", filterModel.ble_version.values.toString());
        }
      }
      if (filterModel.app_version != undefined && filterModel.app_version.values != null && filterModel.app_version.values != undefined) {
        if (filterModel.app_version.values.length == 0) {
          this.filterValues.set("app_version", "null");
        } else {
          this.filterValues.set("app_version", filterModel.app_version.values.toString());
        }
      }
      if (filterModel.config1_civ != undefined && filterModel.config1_civ.filter != null && filterModel.config1_civ.filter != undefined && filterModel.config1_civ.filter != "") {
        this.filterValues.set("config1_civ", filterModel.config1_civ.filter);
      }
      let jsonObject = {};
      this.filterValues.forEach((filter, key) => {
        jsonObject[key] = filter
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
      case 'campaing_name': {
        this.column = "campaing_name";
        this.order = sortModel[0].sort;
        break;
      }
      case 'campaing_status': {
        this.column = "campaing_status";
        this.order = sortModel[0].sort;
        break;
      }
      case 'bin_version': {
        this.column = "bin_version";
        this.order = sortModel[0].sort;
        break;
      }
      case 'ble_version': {
        this.column = "ble_version";
        this.order = sortModel[0].sort;
        break;
      }
      case 'app_version': {
        this.column = "app_version";
        this.order = sortModel[0].sort;
        break;
      }
      case 'config1_civ': {
        this.column = "config1_civ";
        this.order = sortModel[0].sort;
        break;
      }
      default: {
      }
    }
  }

  onDragStopped(params) {
    let columnState = this.gridColumnApi.getColumnState();
    this.saveGridStateInSession(columnState);
  }
  onColumnResized(params) {
    let columnState = this.gridColumnApi.getColumnState();
    this.saveGridStateInSession(columnState);
  }
  onSortChanged(params) {
    let columnState = this.gridColumnApi.getColumnState();
    this.saveGridStateInSession(columnState);
  }

  saveGridStateInSession(columnState) {
    sessionStorage.setItem("gateway_list_grid_column_state", JSON.stringify(columnState));
  }



  headerHeightSetter(event) {
    var padding = 20;
    var height = headerHeightGetter() + padding;
    this.gridApi.setHeaderHeight(height);
    this.gridApi.resetRowHeights();
  }


  onPaginationChanged(params) {
    this.isChecked = false;
    this.gridApi ? this.gridApi.forEachNode(function (node) {
      node.setSelected(false);
    }) : "";
    this.selectedIdsToDelete = [];
    if (this.isPaginationStateRecovered || params.api.paginationGetCurrentPage() > 0) {
      sessionStorage.setItem("gateway_list_grid_saved_page", params.api.paginationGetCurrentPage());
    }
  }

  onFilterChanged(params) {
    let filterModel = params.api.getFilterModel();
    sessionStorage.setItem("gateway_list_grid_filter_state", JSON.stringify(filterModel));
  }

  clearFilter() {
    if (this.gridApi != null) {
      this.gridApi.setFilterModel(null);
      this.gridApi.onFilterChanged();
    }
  }


  getClearFilterFloatingComponent() {
    function ClearFloatingFilter() { }
    ClearFloatingFilter.prototype.init = function (params) {
      this.eGui = document.createElement('div');
      this.eGui.innerHTML =
        '<button class="btnClear primary_buttonClear" (click)="clearFilter()">Clear Filters</button>';
      this.currentValue = null;
      this.clearFilterButton = this.eGui.querySelector('button');
      var that = this;
      function onClearFilterButtonClicked() {
        this.column = 'imei';
        this.order = 'asc';
        params.api.setFilterModel(null);
        // params.column.columnApi.applyColumnState({ defaultState: { sort: null } });
        // params.column.columnApi.resetColumnState();
        params.api.onFilterChanged();
        sessionStorage.setItem("custom_gateway_list_grid_filter_state", null);
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

  getFilterValuesData() {
    return this.companies;
  }



}


function headerHeightGetter() {
  var columnHeaderTexts = document.querySelectorAll('.ag-header-cell-text');
  var columnHeaderTextsArray = [];
  columnHeaderTexts.forEach(node => columnHeaderTextsArray.push(node));
  var clientHeights = columnHeaderTextsArray.map(
    headerText => headerText.clientHeight
  );
  var tallestHeaderTextHeight = Math.max(...clientHeights);
  return tallestHeaderTextHeight;
}