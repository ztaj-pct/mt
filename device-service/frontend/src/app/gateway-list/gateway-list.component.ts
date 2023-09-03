import { Component, OnInit, ViewChild } from '@angular/core';
import { Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { PagerService } from '../util/pagerService';
import { AgGridAngular } from '@ag-grid-community/angular';
import { GridOptions, IGetRowsParams } from '@ag-grid-community/all-modules';
import { ClientSideRowModelModule } from '@ag-grid-community/client-side-row-model';
import { MenuModule } from '@ag-grid-enterprise/menu';
import { ColumnsToolPanelModule } from '@ag-grid-enterprise/column-tool-panel';
import '@ag-grid-community/core/dist/styles/ag-grid.css';
import '@ag-grid-community/core/dist/styles/ag-theme-alpine.css';
import { Module } from '@ag-grid-community/core';
import { MultiFilterModule } from '@ag-grid-enterprise/multi-filter';
import { FormBuilder, FormControl, FormGroup } from '@angular/forms';
import { BtnCellRendererForGatewayDetails } from './btn-cell-renderer.component';
import { CustomDateComponent } from '../ui-elements/custom-date-component.component';
import { GatewayListService } from '../gateway-list.service';
import { ColumnApi, ColumnGroupOpenedEvent, GridApi, RowGroupingModule } from '@ag-grid-enterprise/all-modules';
import { PaginationConstants } from '../util/paginationConstants';
import { CustomSuccessToastrComponent } from '../custom-success-toastr/custom-success-toastr.component';
import { CustomErrorToastrComponent } from '../custom-error-toastr/custom-error-toastr.component';
import { ATCommandService } from '../atcommand.service';
import { CommonData } from '../constant/common-data';
import { Filter } from '../models/Filter.model';
import { ThemePalette } from '@angular/material/core';
import { CustomDatePipe } from '../pipes/custom-date.pipe';
import { CompaniesService } from '../companies.service';
import { CdkTreeNodeOutlet } from '@angular/cdk/tree';
declare var $: any;

interface BodyScrollEvent {
  direction: ScrollDirection;
  left: number;
  top: number;
  api: GridApi;
  columnApi: ColumnApi;
  // Event identifier
  type: string;
}

type ScrollDirection = 'horizontal' | 'vertical';
@Component({
  selector: 'app-gateway-list',
  templateUrl: './gateway-list.component.html',
  styleUrls: ['./gateway-list.component.css']
})
export class GatewayListComponent implements OnInit {

  @ViewChild('picker') picker: any;
  range = 10000;
  dateRangeType: any[] = CommonData.DATE_RANGE_DATA;
  deviceTypeList: any[] = CommonData.CAMPAIGN_DEVICE_TYPE_DATA;
  pageSize: any[] = CommonData.PAGE_SIZE_DATA;
  exportDeviceColumnName: any[] = CommonData.DEVICE_LIST_COLUMN_FOR_EXPORT;
  qaDateType = 'Any';
  customerName = 'Any';
  qaStatusList = [{
    key: 'Passed', value: 'Passed',
  },
  {
    key: 'Failed', value: 'Failed',
  },
  {
    key: 'Pending', value: 'Pending',
  }];
  imeiFilter = false;
  assetFilter = false;
  customerFilter = false;
  reportFilter = false;
  eventFilter = false;
  groupFilter = false;
  qaDateFilter = false;
  sensorFilter = false;
  qaStatusFilter = false;
  sourceFilter = false;
  sortFlag;
  currentPage = 1;
  showingpage;
  totalpage;
  gatewayList: any[] = [];
  totalRecords = 0;
  currentEndRecord = 0;
  currentStartRecord = 0;
  pagedItems: any[];
  private allItems: any;
  pageLimit = 50;
  totalNumberOfRecords = 0;
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
  // parameters for counting records
  filterValues = new Map();
  filterValuesJsonObj: any = {};
  elasticFilter: Filter = new Filter();
  isClearButtonVisible = false;
  loading = false;
  frameworkComponents;
  public sideBar;
  sensorSectionList: any = [{ "id": "1", "name": "Action1" }];
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
  public multiSortKey = 'ctrl';
  public dataForwardAction = "Choose Action";
  rowData: any;
  public modules: Module[] = [
    ClientSideRowModelModule,
    MenuModule,
    ColumnsToolPanelModule,
    MultiFilterModule,
    RowGroupingModule,
  ];
  rowModelType = 'infinite';


  // Pagination Save State
  isFirstTimeLoad = false;
  isPaginationStateRecovered = false;
  filterModelCountFilter: any;
  userNameList: string[];
  companies: any[] = [];
  recentCustomerList: any = [];
  company: any;
  selectedCompany = new FormControl();
  selectDays = new FormControl(0);
  companyUuid = '1';
  companyname: any;
  days = 0;
  selectedtoDateRange = [];
  selected: number;
  selectedIdsToDelete: string[] = [];
  isChecked = false;
  isFirst = true;
  dateIsReadOnly = false;
  filterForm: FormGroup;
  customerList: any[] = [];
  customerListForExport: any;
  // isFilterClick = false;

  public showSpinners = true;
  public showSeconds = false;
  public touchUi = false;
  public enableMeridian = false;
  public stepHour = 1;
  public stepMinute = 1;
  public stepSecond = 1;
  selectAll = false;
  public color: ThemePalette = 'primary';
  roleCallCenterUser: any;
  rolePCTUser: any;
  eventTypeList = [];
  eventList = [];
  today = new Date();
  forwardingGroups: any[];
  resellerCompanies: any[] = [];
  showRecords: boolean = false;
  // organisationName: String;
  // public domLayout: 'normal' | 'autoHeight' | 'print' = 'autoHeight';
  // organisationName: String;
  constructor(
    private readonly atcommandService: ATCommandService,
    private readonly gatewayListService: GatewayListService,
    private readonly companyService: CompaniesService,
    private readonly router: Router,
    private readonly toastr: ToastrService,
    private readonly pagerService: PagerService,
    private readonly formBuldier: FormBuilder) {
    this.getAllEvent();
    this.getAllForwardingGroups();
    this.roleCallCenterUser = JSON.parse(sessionStorage.getItem('IS_ROLE_CALL_CENTER_USER'));
    this.rolePCTUser = JSON.parse(sessionStorage.getItem('IS_ROLE_PCT_USER'));
    const datas = JSON.parse(localStorage.getItem('gatewayRecentSearchList'));
    if (datas && datas.length > 0) {
      this.recentCustomerList = datas;
    }
    if (this.gatewayListService.organisationName != undefined && this.gatewayListService.organisationName != null && this.gatewayListService.organisationName != '') {
      // this.organisationName = this.gatewayListService.organisationName;
      sessionStorage.setItem('organisationNameForGatewayList', this.gatewayListService.organisationName.toString());
      sessionStorage.setItem('filterNameForGatewaySummary', this.gatewayListService.filterNameForGatewaySummary.toString());
      this.gatewayListService.organisationName = null;
      this.gatewayListService.filterNameForGatewaySummary = null;
    } else {
      sessionStorage.setItem('organisationNameForGatewayList', null);
      sessionStorage.setItem('filterNameForGatewaySummary', null);
    }
    this.getAllCustomer();
    const containsFilterParams = {
      filterOptions: [
        'contains',
      ],
      suppressAndOrCondition: true,
      buttons: ['reset'],
      clearButton: true
    };
    const equalFilterParams = {
      filterOptions: [
        'equals',
      ],
      suppressAndOrCondition: true,
      buttons: ['reset'],
      clearButton: true
    };
    const containsEqualFilterParams = {
      filterOptions: [
        'contains',
        'equals'
      ],
      suppressAndOrCondition: true,
      buttons: ['reset'],
      clearButton: true
    };

    // const filterParamsCustomers = {
    //   values: params => params.success(this.getFilterValuesData())
    // };

    let self = this;
    const filterParamsCustomers = {
      values(params: any) {
        setTimeout(function () {
          params.success(self.getFilterValuesData());
        }, 3000);
      },
    };

    const filterParamsForEvent = {
      values(params: any) {
        setTimeout(function () {
          params.success(self.getEventFilterValuesData());
        }, 3000);
      },
    };


    const filterParamsCompany = {
      values: params => params.success(this.getFilterValuesData())
    };

    const purchaseByFilterParamsCompany = {
      values: params => params.success(this.getPurchaseByFilterValuesData())
    };

    const filterParamsForwardingGroups = {
      values: params => params.success(this.getForwardingGroupsFilterValues())
    };

    const cutomeDatepipe = new CustomDatePipe();

    const filterParams2 = {
      suppressAndOrCondition: true,
    };
    this.columnDefs = [
      {
        headerName: '#', minWidth: 20, width: 20, headerCheckboxSelection: true,
        headerCheckboxSelectionFilteredOnly: true, filter: false,
        checkboxSelection: true, lockPosition: 'right'
      },
      {
        headerName: 'Customer',
        field: 'created_by',
        filter: 'agSetColumnFilter',
        floatingFilter: true, flex: 1,
        unSortIcon: true,
        filterParams: filterParamsCustomers, minWidth: 180,
        sortingOrder: ['asc', 'desc', null],
        lockPosition: 'right',
        cellClass: 'locked-col',
        suppressColumnsToolPanel: true,
      },
      {
        headerName: 'Forwarding Group',
        field: 'forwarding_group',
        sortable: true,
        filter: 'agSetColumnFilter',
        filterParams: filterParamsForwardingGroups
      },
      {
        headerName: 'Parent Group',
        field: 'purchase_by_name',
        sortable: true,
        filter: 'agSetColumnFilter',
        filterParams: purchaseByFilterParamsCompany
      },
      {
        headerName: 'Device ID', field: 'imei',
        sortingOrder: ['asc', 'desc', null], unSortIcon: true,
        filter: 'agTextColumnFilter', floatingFilter: true,
        filterParams: containsFilterParams,
        width: 200,
        suppressMovable: true,
        cellClass: 'suppress-movable-col',
        suppressColumnsToolPanel: true,
        cellRenderer: function (params) {
          if (params.data !== undefined && params.data.imei != null) {
            return '<div class="pointer" style="color: #0000EE;text-decoration: underline;"><a>' + params.data.imei + '</a></div>';
          }
        }
      },
      {
        headerName: 'Asset Details',
        children: [
          {
            headerName: 'Asset Name', field: 'asset_name', sortable: true, unSortIcon: true, filter: 'agTextColumnFilter', width: 240, floatingFilter: true, filterParams: containsFilterParams,
            cellRenderer: function (params) {
              if (params.data !== undefined && params.data.asset_name != null) {
                return '<div>' + params.data.asset_name + '</div>';
              }
            }
          },
          {
            headerName: 'Asset Type', field: 'asset_type', filter: 'agSetColumnFilter', sortable: true, filterParams: filterParamsForAssetStatus, unSortIcon: true, columnGroupShow: 'open', width: 240, floatingFilter: true,
            cellRenderer: function (params) {
              if (params.data !== undefined && params.data.asset_type != null) {
                return '<div>' + params.data.asset_type + '</div>';
              }
            }
          },
          // {
          //   headerName: 'Installed Date', field: 'installedDateTimestamp', columnGroupShow: 'open', sortable: true, unSortIcon: true, filter: 'agDateColumnFilter', minWidth: 200, floatingFilter: true,
          //   cellRenderer: function (params) {
          //     if (params.data !== undefined && params.data.installed_date != null) {
          //       return '<div>' + params.data.installed_date + '</div>';
          //     }
          //   }
          // },
          {
            headerName: 'VIN', field: 'vin', columnGroupShow: 'open', sortable: true, unSortIcon: true, filter: 'agTextColumnFilter', width: 240, floatingFilter: true, filterParams: containsFilterParams,
            cellRenderer: function (params) {
              if (params.data !== undefined && params.data.vin != null) {
                return '<div>' + params.data.vin + '</div>';
              }
            }
          },
          {
            headerName: 'Manufacturer', field: 'manufacturer', columnGroupShow: 'open', sortable: true, unSortIcon: true, filter: 'agTextColumnFilter', width: 240, floatingFilter: true, filterParams: containsFilterParams,
            cellRenderer: function (params) {
              if (params.data !== undefined && params.data.manufacturer != null) {
                return '<div>' + params.data.manufacturer + '</div>';
              }
            }
          }
        ]
      },
      {
        headerName: 'Device Details',
        children: [
          {
            headerName: 'Status', field: 'status', unSortIcon: true, filter: 'agSetColumnFilter', width: 180, floatingFilter: true, filterParams: filterParams,
            cellRenderer: function (params) {
              if (params.data !== undefined && params.data.status != null) {
                return '<div>' + params.data.status + '</div>';
              }
            }
          },
          {
            headerName: 'Usage', field: 'usage_status', unSortIcon: true, filter: 'agSetColumnFilter', width: 180, floatingFilter: true, filterParams: filterParamsUsageStatus,
            cellRenderer: function (params) {
              if (params.data !== undefined && params.data.usage_status != null) {
                return '<div>' + params.data.usage_status + '</div>';
              }
            }
          },
          {
            headerName: 'Order #', field: 'son', unSortIcon: true, filter: 'agTextColumnFilter', width: 180, floatingFilter: true, filterParams: containsFilterParams,
            cellRenderer: function (params) {
              if (params.data !== undefined && params.data.son != null) {
                return '<div>' + params.data.son + '</div>';
              }
            }
          },
          {
            headerName: 'Model', field: 'device_type', unSortIcon: true, filter: 'agSetColumnFilter', filterParams: filterParamsModel,
            cellRenderer: function (params) {
              if (params.data !== undefined && params.data.device_type != null) {
                return '<div>' + params.data.device_type + '</div>';
              }
            }
          },
          {
            headerName: 'Product Type', field: 'product_name', unSortIcon: true, filter: 'agSetColumnFilter', filterParams: filterParamsProductType,
            cellRenderer: function (params) {
              if (params.data !== undefined && params.data.product_name != null) {
                return '<div>' + params.data.product_name + '</div>';
              }
            }
          },
          {
            headerName: 'Battery', field: 'battery', sortable: true, suppressSorting: true, filter: 'agTextColumnFilter', filterParams: containsFilterParams, minWidth: 200,
            cellRenderer: function (params) {
              if (params.data !== undefined && params.data.battery != null) {
                return '<div>' + params.data.battery + '</div>';
              }
            }
          },
          {
            headerName: 'Installation Date', field: 'installation_date', sortable: true, suppressSorting: true, filter: 'agDateColumnFilter', filterParams: filterParamsInstalledStatusFlag, minWidth: 200,
            cellRenderer: function (params) {
              if (params.data !== undefined && params.data.installation_date != null) {
                if (params.data.status === 'ACTIVE') {
                  return '<div>' + params.data.installation_date + '</div>';
                } else {
                  return '<div>  </div>';
                }
              }
            }
          }
        ]
      },
      {
        headerName: 'Report Details',
        children: [
          {
            headerName: 'Last Report', field: 'last_report_date_time', sortable: true, suppressSorting: true, filter: 'agDateColumnFilter', minWidth: 200,
            cellRenderer: function (params) {
              if (params.data !== undefined && params.data.last_report_date_time != null) {
                return '<div>' + cutomeDatepipe.transform(params.data.last_report_date_time, 'latest_report') + '</div>';
              }
            }
          },
          {
            headerName: 'Event ID', field: 'event_id', sortable: true, suppressSorting: true, filter: 'agTextColumnFilter', filterParams: equalFilterParams, minWidth: 200,
            cellRenderer: function (params) {
              if (params.data !== undefined && params.data.event_id != null && params.data.event_id != 0) {
                return '<div>' + params.data.event_id + '</div>';
              }
            }
          },
          {
            headerName: 'Event Type', field: 'event_type', sortable: true, suppressSorting: true, filter: 'agSetColumnFilter', filterParams: filterParamsForEvent, minWidth: 200,
            cellRenderer: function (params) {
              if (params.data !== undefined && params.data.event_type != null) {
                return '<div>' + params.data.event_type + '</div>';
              }
            }
          },
          {
            headerName: 'Lat', field: 'lat', sortable: true, suppressSorting: true, columnGroupShow: 'open', filter: 'agTextColumnFilter', filterParams: containsEqualFilterParams, minWidth: 200,
            cellRenderer: function (params) {
              if (params.data !== undefined && params.data.lat != null) {
                return '<div>' + params.data.lat + '</div>';
              }
            }
          },
          {
            headerName: 'Long', field: 'longitude', sortable: true, suppressSorting: true, columnGroupShow: 'open', filter: 'agTextColumnFilter', filterParams: containsEqualFilterParams, minWidth: 200,
            cellRenderer: function (params) {
              if (params.data !== undefined && params.data.longitude != null) {
                return '<div>' + params.data.longitude + '</div>';
              }
            }
          }
        ]
      },
      {
        headerName: 'QA',
        children: [
          {
            headerName: 'QA Status', field: 'qa_status', filter: 'agSetColumnFilter', filterParams: filterParamsQaStatus, width: 180, floatingFilter: true,
            cellRenderer: function (params) {
              if (params.data !== undefined && params.data.qa_status != null) {
                return '<div>' + params.data.qa_status + '</div>';
              }
            }
          },
          {
            headerName: 'QA Date', field: 'qa_date', sortable: true, filter: 'agDateColumnFilter', floatingFilter: true, minWidth: 200,
            cellRenderer: function (params) {
              if (params.data !== undefined && params.data.qa_dates != null) {
                return '<div>' + params.data.qa_dates + '</div>';
              }
            }
          },
          {
            headerName: 'configName', field: 'config_name', sortable: true, suppressSorting: true, filter: 'agTextColumnFilter', filterParams: equalFilterParams, minWidth: 200,
            cellRenderer: function (params) {
              if (params.data !== undefined && params.data.config_name != null) {
                return '<div>' + params.data.config_name + '</div>';
              }
            }
          }]
      },
      {
        headerName: 'Security',
        children: [
          {
            headerName: 'Sec Status', field: 'imei_hashed', sortable: true, filter: "agSetColumnFilter", filterParams: filterParamsSecStatus, width: 180, floatingFilter: true,
            cellRenderer: function (params) {
              if (params.data !== undefined && params.data.sec_status != null) {
                return '<div>' + params.data.sec_status + '</div>';
              }
            }
          },
          {
            headerName: 'Sec Date', field: 'revoked_time', sortable: false, filter: "agDateColumnFilter", floatingFilter: true, minWidth: 200,
            cellRenderer: function (params) {
              if (params.data !== undefined && params.data.sec_date != null) {
                return '<div>' + cutomeDatepipe.transform(params.data.sec_date, 'latest_report') + '</div>';
              }
            }
          }]
      },
      {
        headerName: 'Firmware Load',
        children: [
          {
            headerName: 'BIN', field: 'bin_version', sortable: true, width: 240, floatingFilter: true, filter: 'agTextColumnFilter', filterParams: containsFilterParams, hide: true,
            cellRenderer: function (params) {
              if (params.data !== undefined && params.data.bin_version) {
                return '<div>' + params.data.bin_version + '</div>';
              }
            }
          },
          {
            headerName: 'APP', field: 'app_version', columnGroupShow: 'open', sortable: true, width: 240, filter: 'agTextColumnFilter', filterParams: containsFilterParams, floatingFilter: true, hide: true,
            cellRenderer: function (params) {
              if (params.data !== undefined && params.data.app_version) {
                return '<div>' + params.data.app_version + '</div>';
              }
            }
          },
          {
            headerName: 'MCU', field: 'mcu_version', columnGroupShow: 'open', sortable: true, width: 240, floatingFilter: true, filter: 'agTextColumnFilter', filterParams: containsFilterParams, hide: true,
            cellRenderer: function (params) {
              if (params.data !== undefined && params.data.mcu_version) {
                return '<div>' + params.data.mcu_version + '</div>';
              }
            }
          },
          {
            headerName: 'BLE', field: 'ble_version', columnGroupShow: 'open', sortable: true, filter: 'agTextColumnFilter', filterParams: containsFilterParams, width: 240, floatingFilter: true, hide: true,
            cellRenderer: function (params) {
              if (params.data !== undefined && params.data.ble_version) {
                return '<div>' + params.data.ble_version + '</div>';
              }
            }
          }
        ]
      },
      {
        headerName: 'Configuration',
        children: [
          {
            headerName: 'Config1 Name', field: 'config1_name', sortable: true, width: 240, floatingFilter: true, filter: 'agTextColumnFilter', filterParams: containsFilterParams, hide: true,
            cellRenderer: function (params) {
              if (params.data !== undefined && params.data.config1_name) {
                return '<div>' + params.data.config1_name + '</div>';
              }
            }
          },
          {
            headerName: 'Config1 CRC', field: 'config1_crc', columnGroupShow: 'open', filter: 'agTextColumnFilter', filterParams: containsFilterParams, sortable: true, width: 240, floatingFilter: true, hide: true,
            cellRenderer: function (params) {
              if (params.data !== undefined && params.data.config1_crc) {
                return '<div>' + params.data.config1_crc + '</div>';
              }
            }
          },
          {
            headerName: 'Config2 Name', field: 'config2_name', columnGroupShow: 'open', sortable: true, width: 240, floatingFilter: true, filter: 'agTextColumnFilter', filterParams: containsFilterParams, hide: true,
            cellRenderer: function (params) {
              if (params.data !== undefined && params.data.config2_name) {
                return '<div>' + params.data.config2_name + '</div>';
              }
            }
          },
          {
            headerName: 'Config2 CRC', field: 'config2_crc', columnGroupShow: 'open', sortable: true, filter: 'agTextColumnFilter', filterParams: containsFilterParams, width: 240, floatingFilter: true, hide: true,
            cellRenderer: function (params) {
              if (params.data !== undefined && params.data.config2_crc) {
                return '<div>' + params.data.config2_crc + '</div>';
              }
            }
          },
          {
            headerName: 'Config3 Name', field: 'config3_name', columnGroupShow: 'open', sortable: true, width: 240, floatingFilter: true, filter: 'agTextColumnFilter', filterParams: containsFilterParams, hide: true,
            cellRenderer: function (params) {
              if (params.data !== undefined && params.data.config3_name) {
                return '<div>' + params.data.config3_name + '</div>';
              }
            }
          },
          {
            headerName: 'Config3 CRC', field: 'config3_crc', columnGroupShow: 'open', sortable: true, width: 240, filter: 'agTextColumnFilter', filterParams: containsFilterParams, floatingFilter: true, hide: true,
            cellRenderer: function (params) {
              if (params.data !== undefined && params.data.config3_crc) {
                return '<div>' + params.data.config3_crc + '</div>';
              }
            }
          },
          {
            headerName: 'Config4 Name', field: 'config4_name', columnGroupShow: 'open', sortable: true, width: 240, floatingFilter: true, filter: 'agTextColumnFilter', filterParams: containsFilterParams, hide: true,
            cellRenderer: function (params) {
              if (params.data !== undefined && params.data.config4_name) {
                return '<div>' + params.data.config4_name + '</div>';
              }
            }
          },
          {
            headerName: 'Config4 CRC', field: 'config4_crc', columnGroupShow: 'open', sortable: true, filter: 'agTextColumnFilter', filterParams: containsFilterParams, width: 240, floatingFilter: true, hide: true,
            cellRenderer: function (params) {
              if (params.data !== undefined && params.data.config4_crc) {
                return '<div>' + params.data.config4_crc + '</div>';
              }
            }
          },
          {
            headerName: 'Devuser.cfg Name', field: 'devuser_cfg_name', columnGroupShow: 'open', sortable: true, width: 240, floatingFilter: true, filter: 'agTextColumnFilter', filterParams: containsFilterParams, hide: true,
            cellRenderer: function (params) {
              if (params.data !== undefined && params.data.devuser_cfg_name) {
                return '<div>' + params.data.devuser_cfg_name + '</div>';
              }
            }
          },
          {
            headerName: 'Devuser.cfg Value', field: 'devuser_cfg_value', columnGroupShow: 'open', sortable: true, filter: 'agTextColumnFilter', filterParams: containsFilterParams, width: 240, floatingFilter: true, hide: true,
            cellRenderer: function (params) {
              if (params.data !== undefined && params.data.devuser_cfg_value) {
                return '<div>' + params.data.devuser_cfg_value + '</div>';
              }
            }
          }
        ]
      },
      {
        headerName: 'Device Forwarding',
        children: [
          // {
          //   headerName: 'Fwd 1 Type', field: 'type1', sortable: false, width: 240, floatingFilter: true, filter: 'agSetColumnFilter', filterParams: filterParamsForwardingType, hide: true,
          //   cellRenderer: function (params) {
          //     if (params.data !== undefined && params.data.device_forwarding_payload.length > 0) {
          //       return '<div>' + params.data.device_forwarding_payload[0].type + '</div>';
          //     }
          //   }
          // },
          // {
          //   headerName: 'Fwd 1 Destination', field: 'url1', columnGroupShow: 'open', filter: 'agTextColumnFilter', sortable: false, width: 240, floatingFilter: true, filterParams: containsFilterParams, hide: true,
          //   cellRenderer: function (params) {
          //     if (params.data !== undefined && params.data.device_forwarding_payload.length > 0) {
          //       return '<div>' + params.data.device_forwarding_payload[0].url + '</div>';
          //     }
          //   }
          // },
          // {
          //   headerName: 'Fwd 2 Type', field: 'type2', columnGroupShow: 'open', sortable: false, width: 240, floatingFilter: true, filter: 'agSetColumnFilter', filterParams: filterParamsForwardingType, hide: true,
          //   cellRenderer: function (params) {
          //     if (params.data !== undefined && params.data.device_forwarding_payload.length > 0) {
          //       return '<div>' + params.data.device_forwarding_payload[1].type + '</div>';
          //     }
          //   }
          // },
          // {
          //   headerName: 'Fwd 2 Destination', field: 'url2', columnGroupShow: 'open', sortable: false, filter: 'agTextColumnFilter', width: 240, floatingFilter: true, filterParams: containsFilterParams, hide: true,
          //   cellRenderer: function (params) {
          //     if (params.data !== undefined && params.data.device_forwarding_payload.length > 0) {
          //       return '<div>' + params.data.device_forwarding_payload[1].url + '</div>';
          //     }
          //   }
          // },
          // {
          //   headerName: 'Fwd 3 Type', field: 'type3', columnGroupShow: 'open', sortable: false, filter: 'agSetColumnFilter', filterParams: filterParamsForwardingType, width: 240, floatingFilter: true, hide: true,
          //   cellRenderer: function (params) {
          //     if (params.data !== undefined && params.data.device_forwarding_payload.length > 0) {
          //       return '<div>' + params.data.device_forwarding_payload[2].type + '</div>';
          //     }
          //   }
          // },
          // {
          //   headerName: 'Fwd 3 Destination', field: 'url3', columnGroupShow: 'open', sortable: false, filter: 'agTextColumnFilter', width: 240, floatingFilter: true, filterParams: containsFilterParams, hide: true,
          //   cellRenderer: function (params) {
          //     if (params.data !== undefined && params.data.device_forwarding_payload.length > 0) {
          //       return '<div>' + params.data.device_forwarding_payload[2].url + '</div>';
          //     }
          //   }
          // },
          // {
          //   headerName: 'Fwd 4 Type', field: 'type4', columnGroupShow: 'open', sortable: false, filter: 'agSetColumnFilter', filterParams: filterParamsForwardingType, width: 240, floatingFilter: true, hide: true,
          //   cellRenderer: function (params) {
          //     if (params.data !== undefined && params.data.device_forwarding_payload.length > 0) {
          //       return '<div>' + params.data.device_forwarding_payload[3].type + '</div>';
          //     }
          //   }
          // },
          // {
          //   headerName: 'Fwd 4 Destination', field: 'url4', columnGroupShow: 'open', sortable: false, filter: 'agTextColumnFilter', width: 240, floatingFilter: true, filterParams: containsFilterParams, hide: true,
          //   cellRenderer: function (params) {
          //     if (params.data !== undefined && params.data.device_forwarding_payload.length > 0) {
          //       return '<div>' + params.data.device_forwarding_payload[3].url + '</div>';
          //     }
          //   }
          // }
          {
            headerName: 'Fwd Rule 1', field: 'rule1', sortable: false, width: 240, floatingFilter: true, filter: 'agSetColumnFilter', filterParams: filterParamsForwardingType, hide: true,
            cellRenderer: function (params) {
              if (params.data !== undefined && params.data.rule1) {
                return '<div>' + params.data.rule1 + '</div>';
              }
            }
          },
          {
            headerName: 'Fwd Rule 1 Source', field: 'rule1_source', columnGroupShow: 'open', filter: 'agTextColumnFilter', sortable: false, width: 240, floatingFilter: true, filterParams: containsFilterParams, hide: true,
            cellRenderer: function (params) {
              if (params.data !== undefined && params.data.rule1_source) {
                return '<div>' + params.data.rule1_source + '</div>';
              }
            }
          },
          {
            headerName: 'Fwd Rule 2', field: 'rule2', columnGroupShow: 'open', sortable: false, width: 240, floatingFilter: CdkTreeNodeOutlet, filter: 'agSetColumnFilter', filterParams: filterParamsForwardingType, hide: true,
            cellRenderer: function (params) {
              if (params.data !== undefined && params.data.rule2) {
                return '<div>' + params.data.rule2 + '</div>';
              }
            }
          },
          {
            headerName: 'Fwd Rule 2 Source', field: 'rule2_source', columnGroupShow: 'open', sortable: false, filter: 'agTextColumnFilter', width: 240, floatingFilter: true, filterParams: containsFilterParams, hide: true,
            cellRenderer: function (params) {
              if (params.data !== undefined && params.data.rule2_source) {
                return '<div>' + params.data.rule2_source + '</div>';
              }
            }
          },
          {
            headerName: 'Fwd Rule 3', field: 'rule3', columnGroupShow: 'open', sortable: false, filter: 'agSetColumnFilter', filterParams: filterParamsForwardingType, width: 240, floatingFilter: true, hide: true,
            cellRenderer: function (params) {
              if (params.data !== undefined && params.data.rule3) {
                return '<div>' + params.data.rule3 + '</div>';
              }
            }
          },
          {
            headerName: 'Fwd Rule 3 Source', field: 'rule3_source', columnGroupShow: 'open', sortable: false, filter: 'agTextColumnFilter', width: 240, floatingFilter: true, filterParams: containsFilterParams, hide: true,
            cellRenderer: function (params) {
              if (params.data !== undefined && params.data.rule3_source) {
                return '<div>' + params.data.rule3_source + '</div>';
              }
            }
          },
          {
            headerName: 'Fwd Rule 4', field: 'rule4', columnGroupShow: 'open', sortable: false, filter: 'agSetColumnFilter', filterParams: filterParamsForwardingType, width: 240, floatingFilter: true, hide: true,
            cellRenderer: function (params) {
              if (params.data !== undefined && params.data.rule4) {
                return '<div>' + params.data.rule4 + '</div>';
              }
            }
          },
          {
            headerName: 'Fwd Rule 4 Source', field: 'rule4_source', columnGroupShow: 'open', sortable: false, filter: 'agTextColumnFilter', width: 240, floatingFilter: true, filterParams: containsFilterParams, hide: true,
            cellRenderer: function (params) {
              if (params.data !== undefined && params.data.rule4_source) {
                return '<div>' + params.data.rule4_source + '</div>';
              }
            }
          },
          {
            headerName: 'Fwd Rule 5', field: 'rule5', columnGroupShow: 'open', sortable: false, filter: 'agSetColumnFilter', filterParams: filterParamsForwardingType, width: 240, floatingFilter: true, hide: true,
            cellRenderer: function (params) {
              if (params.data !== undefined && params.data.rule5) {
                return '<div>' + params.data.rule5 + '</div>';
              }
            }
          },
          {
            headerName: 'Fwd Rule 5 Source', field: 'rule5_source', columnGroupShow: 'open', sortable: false, filter: 'agTextColumnFilter', width: 240, floatingFilter: true, filterParams: containsFilterParams, hide: true,
            cellRenderer: function (params) {
              if (params.data !== undefined && params.data.rule5_source) {
                return '<div>' + params.data.rule5_source + '</div>';
              }
            }
          }
        ]
      },
      {
        headerName: 'SIM #',
        children: [
          {
            headerName: 'Service Network', field: 'service_network', unSortIcon: true, width: 260, filter: 'agTextColumnFilter', floatingFilter: true, filterParams: containsFilterParams,
            cellRenderer: function (params) {
              if (params.data !== undefined && params.data.cellular_payload.service_network != null) {
                return '<div>' + params.data.cellular_payload.service_network + '</div>';
              }
            }
          },
          {
            headerName: 'SIM #', field: 'cellular', unSortIcon: true, columnGroupShow: 'open', filter: 'agTextColumnFilter', width: 240, floatingFilter: true, filterParams: containsFilterParams,
            cellRenderer: function (params) {
              if (params.data !== undefined && params.data.cellular_payload.cellular != null) {
                return '<div>' + params.data.cellular_payload.cellular + '</div>';
              }
            }
          },
          {
            headerName: 'Service Country', field: 'service_country', unSortIcon: true, columnGroupShow: 'open', filter: 'agTextColumnFilter', width: 240, floatingFilter: true, filterParams: containsFilterParams,
            cellRenderer: function (params) {
              if (params.data !== undefined && params.data.cellular_payload.service_country != null) {
                return '<div>' + params.data.cellular_payload.service_country + '</div>';
              }
            }
          },
          {
            headerName: 'Phone #', field: 'phone', columnGroupShow: 'open', unSortIcon: true, filter: 'agTextColumnFilter', width: 240, floatingFilter: true, filterParams: containsFilterParams,
            cellRenderer: function (params) {
              if (params.data !== undefined && params.data.cellular_payload.phone != null) {
                return '<div>' + params.data.cellular_payload.phone + '</div>';
              }
            }
          }
        ]
      },
      {
        headerName: 'Action', field: 'action', sortable: false, filter: 'agNumberColumnFilter', floatingFilter: true, floatingFilterComponent: 'customClearFloatingFilter',
        floatingFilterComponentParams: {
          suppressFilterButton: true
        },
        cellRenderer: 'btnCellRenderer', width: 140, minWidth: 140, pinned: 'right'
      }
    ];
    this.cacheOverflowSize = 5;
    this.maxConcurrentDatasourceRequests = 1;
    this.infiniteInitialRowCount = 1;
    this.gridOptions = {
      cacheBlockSize: this.pageLimit,
      paginationPageSize: this.pageLimit,
      rowModelType: 'infinite',
      animateRows: true,
      rowData: null,
      sortingOrder: ['desc', 'asc', null],
      onBodyScroll: (event: BodyScrollEvent) => {
        if (event.direction === 'horizontal') {
          this.autoSizeAll(false);
        }
      },
      onColumnGroupOpened: (event: ColumnGroupOpenedEvent) => {
        this.autoSizeAll(false);
      }
    };

    this.frameworkComponents = {
      btnCellRenderer: BtnCellRendererForGatewayDetails,
      agDateInput: CustomDateComponent,
      // agColumnHeader: CustomHeader
    };

    this.defaultColDef = {
      flex: 1,
      minWidth: 100,
      sortable: true,
      floatingFilter: true,
      unSortIcon: true,
      enablePivot: true,
      resizable: true,
      filter: true,
      wrapText: true,
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


  getEventFilterValuesData() {
    return this.eventTypeList;
  }
  getAllEvent() {
    this.gatewayListService.getEventList().subscribe(
      data => {
        this.eventTypeList = [];
        this.eventList = data.body;
        for (const iterator of data.body) {
          this.eventTypeList.push(iterator.eventType);
        }
        return this.eventTypeList;
      }
    );
  }
  onBtNormalCols() {
    // this.gridApi.setColumnDefs([]);
    // this.gridApi.setColumnDefs(this.columnDefs);
    // this.onColumnVisible(this.gridApi);
    this.gridApi.onFilterChanged();
  }


  ngOnInit() {
    this.userCompanyType = sessionStorage.getItem('type');
    this.isSuperAdmin = sessionStorage.getItem('IS_ROLE_SUPERADMIN');
    this.userCompanyType = sessionStorage.getItem('type');
    const data = JSON.parse(sessionStorage.getItem('gateway_advance_save_filter'));
    this.initilizeFilterForm(data);
    this.getAllcompanies();
    this.getAllResellerCompanies();
    if (this.isSuperAdmin === 'true') {
      this.router.navigate(['gateway-list']);
    } else {
      this.router.navigate(['gateway-list']);
    }
    this.isRoleCustomerManager = sessionStorage.getItem('IS_ROLE_CUSTOMER_MANAGER');
    this.sortFlag = false;
    const columState = JSON.parse(sessionStorage.getItem('gateway_list_grid_column_state'));
    if (columState) {
      setTimeout(() => {
        this.gridColumnApi.applyColumnState({ state: columState, applyOrder: true, });
      }, 300);
    }
  }


  getSelectedRowData() {
    return this.gridApi.getSelectedNodes().map(node => node.data);
  }

  reloadComponent() {
    this.router.navigateByUrl('/', { skipLocationChange: true }).then(() =>
      this.router.navigate([this.router.url]));
  }

  deleteGateway(): void {
    if (this.gatewayListService.gateway != null) {
      const gatewayList = [];
      gatewayList.push(this.gatewayListService.gateway.imei);
      this.gatewayListService.deleteGatewayByIds(gatewayList).subscribe(data => {
        if (data.body.length === 0) {
          this.toastr.success('The selected gateway(s) have been deleted successfully.', null, { toastComponent: CustomSuccessToastrComponent });
        }
      }, error => {
        console.log(error);
      });
      this.reloadComponent();
    }

  }

  getSelctedRow(action) {

    this.gatewayList = [];
    this.selectedIdsToDelete = [];
    this.gatewayList = this.getSelectedRowData();
    this.gatewayListService.gatewayList = [];

    if (action === 'delete') {
      for (let i = 0; i < this.gatewayList.length; i++) {
        this.gatewayList[i].is_selected = true;
        if (this.gatewayList[i].status !== 'Pending') {
          this.toastr.error('The following records are part of an installation, and so they cannot be deleted.', 'Problem with Deletion', { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
          return;
        } else {
          this.gatewayListService.gatewayList.push(this.gatewayList[i]);
        }
      }
      if (this.gatewayListService.gatewayList.length > 1) {
        $('#deleteGatewaysModal').modal('show');

      } else if (this.gatewayListService.gatewayList.length === 1) {
        $('#deleteGatewayModal').modal('show');
      }
    } else if (action === 'update') {
      for (let i = 0; i < this.gatewayList.length; i++) {
        this.gatewayList[i].is_selected = true;
        if (this.gatewayList[i].gateway_type !== 'Gateway') {
          this.toastr.error('The selected device(s) do not support sensors.', 'Device Does Not Support Sensors ', { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
          return;
        } else {
          if (this.gatewayList[i].status !== 'Pending') {
            this.toastr.error('The selected device(s) are part of an in-progress or active installation, and cannot be modified.', 'Device Configuration Can’t Be Modified',
              { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
            return;
          } else {
            this.gatewayListService.gatewayList.push(this.gatewayList[i]);
          }
        }
      }
      this.router.navigate(['update-gateways']);
    }

  }


  deleteAll(event) {
    if (event.checked) {
      this.selectAll = true;
    } else {
      this.selectAll = false;
    }
    this.gridApi.forEachNode(function (node) {
      if (event.checked) {
        node.setSelected(true);
      } else {
        node.setSelected(false);
      }
    });

    if (event.checked) {
      this.gatewayList = this.getSelectedRowData();
      for (let i = 0; i < this.gatewayList.length; i++) {
        this.gatewayList[i].is_selected = true;
      }
    } else if (!event.checked) {
      this.selectedIdsToDelete = [];
      this.gatewayList = this.getSelectedRowData();
      for (let i = 0; i < this.gatewayList.length; i++) {
        this.gatewayList[i].is_selected = false;
      }
    }
  }

  onPageSizeChanged(event: any) {
    this.pageLimit = Number(event.target.value);
    this.gridOptions.cacheBlockSize = this.pageLimit;
    this.gridOptions.paginationPageSize = this.pageLimit;
    this.gridApi.paginationSetPageSize(Number(event.target.value));
    this.gridApi.onFilterChanged();

  }
  onGridReady(params) {
    this.paramList = params;
    this.gridApi = params.api;
    this.gridApi.setRowData([]);
    this.gridColumnApi = params.columnApi;
    let filterSavedState = sessionStorage.getItem('gateway_list_grid_filter_state');
    let gridSavedState = sessionStorage.getItem('gateway_list_grid_column_state');
    let gridColumnHeaderState = sessionStorage.getItem('gateway_list_grid_column_group_state');
    if (this.gatewayListService.organisationId) {
      filterSavedState = sessionStorage.getItem('gateway_list_grid_filter_state' + this.gatewayListService.organisationId);
      gridSavedState = sessionStorage.getItem('gateway_list_grid_column_state' + this.gatewayListService.organisationId);
      gridColumnHeaderState = sessionStorage.getItem('gateway_list_grid_column_group_state' + this.gatewayListService.organisationId);
    }

    if (gridSavedState) {
      this.gridColumnApi.setColumnState(JSON.parse(gridSavedState));
      this.gridColumnApi.setColumnGroupState(JSON.parse(gridColumnHeaderState))
    }
    if (filterSavedState) {
      this.gridApi.setFilterModel(JSON.parse(filterSavedState));
    }
    const datasource = {
      getRows: (params: IGetRowsParams) => {
        this.sortForAgGrid(params.sortModel);
        if (sessionStorage.getItem('organisationNameForGatewayList') != undefined && sessionStorage.getItem('organisationNameForGatewayList') != null &&
          sessionStorage.getItem('organisationNameForGatewayList') != '' && sessionStorage.getItem('organisationNameForGatewayList') != 'null'
          && sessionStorage.getItem('filterNameForGatewaySummary') != undefined && sessionStorage.getItem('filterNameForGatewaySummary') != null &&
          sessionStorage.getItem('filterNameForGatewaySummary') != '' && sessionStorage.getItem('filterNameForGatewaySummary') != 'null'
        ) {
          const Okeys = Object.keys(params.filterModel);
          if (Okeys.length > 0) {
            let isNotCompanyField = true;
            let isNotProductField = true;
            for (let i = 0; i < Okeys.length; i++) {
              if (Okeys[i] === 'created_by') {
                if (isNotCompanyField) {
                  isNotCompanyField = false;
                }

              } else if (Okeys[i] === 'product_name') {
                if (isNotProductField) {
                  isNotProductField = false;
                }
              }
            }

            if (isNotCompanyField && isNotProductField) {
              params.filterModel.created_by = {
                filterType: 'set',
                values: [sessionStorage.getItem('organisationNameForGatewayList')]
              };

              if (sessionStorage.getItem('filterNameForGatewaySummary') !== 'null' && sessionStorage.getItem('filterNameForGatewaySummary') !== 'none') {
                params.filterModel.product_name = {
                  filterType: 'set',
                  values: [sessionStorage.getItem('filterNameForGatewaySummary')]
                };
              }

            } else if (isNotCompanyField && !isNotProductField) {
              params.filterModel.created_by = {
                filterType: 'set',
                values: [sessionStorage.getItem('organisationNameForGatewayList')]
              };
            } else if (!isNotCompanyField && isNotProductField) {
              if (sessionStorage.getItem('filterNameForGatewaySummary') !== 'null' && sessionStorage.getItem('filterNameForGatewaySummary') !== 'none') {
                params.filterModel.product_name = {
                  filterType: 'set',
                  values: [sessionStorage.getItem('filterNameForGatewaySummary')]
                };
              }
            } else {
              sessionStorage.setItem('organisationNameForGatewayList', null);
              sessionStorage.setItem('filterNameForGatewaySummary', null);
            }

          } else {
            if (sessionStorage.getItem('filterNameForGatewaySummary') !== 'null' && sessionStorage.getItem('filterNameForGatewaySummary') !== 'none') {
              params.filterModel = {
                created_by: {
                  filterType: 'set',
                  values: [sessionStorage.getItem('organisationNameForGatewayList')]
                },
                product_name: {
                  filterType: 'set',
                  values: [sessionStorage.getItem('filterNameForGatewaySummary')]
                }
              };
            } else {
              params.filterModel = {
                created_by: {
                  filterType: 'set',
                  values: [sessionStorage.getItem('organisationNameForGatewayList')]
                }
              };
            }

          }

          // this.organisationName = null;
        }


        this.filterForAgGrid(params.filterModel);
        if (this.isFirst) {
          this.loading = true;
          this.gatewayListService.getGatewayListSortwithCompanyPage(this.currentPage, this.column, this.order, this.pageLimit,
            this.filterValuesJsonObj).subscribe(data => {
              this.loading = false;
              this.rowData = data.body.content;
              this.totalNumberOfRecords = data.total_key;
              if (this.totalNumberOfRecords <= (this.currentPage * this.pageLimit)) {
                this.isFirst = false;
              }
              this.currentPage++;
              params.successCallback(this.rowData, data.total_key);
              this.autoSizeAll(false);
              if (this.selectAll) {
                let self = this;
                setTimeout(function () {
                  self.deleteAll({ checked: true });
                }, 300);
              }
            },
              error => {
                this.loading = false;
                console.log(error);
              });
        } else {

        }
      }
    };
    this.gridApi.setDatasource(datasource);
  }

  private rowSelection;
  onSelectionChanged(event) {
  }
  onRowSelected(event) {
    const gatewayList = this.getSelectedRowData();
    if (gatewayList.length !== 0 && (gatewayList.length % this.pageLimit === 0 || gatewayList.length == this.totalNumberOfRecords)) {
      this.isChecked = true;
      this.selectAll = true;
    } else {
      this.isChecked = false;
      this.selectAll = false;
    }
  }


  autoSizeAll(skipHeader: boolean) {

    const columState = JSON.parse(sessionStorage.getItem('gateway_list_grid_column_state'));
    const filterSavedState = sessionStorage.getItem('gateway_list_grid_filter_state');
    const gridColumnGroup = sessionStorage.getItem('gateway_list_grid_column_group_state');

    // if (!columState && !filterSavedState) {
    const allColumnIds: string[] = [];
    this.gridColumnApi.getAllColumns().forEach((column) => {
      allColumnIds.push(column.getId());
    });
    this.gridColumnApi.autoSizeColumns(allColumnIds, skipHeader);
    // }
  }

  onDragStopped(params: any) {
    this.saveGridStateInSession(this.gridColumnApi.getColumnState());
  }

  onColumnGroupOpened(params: any){
    console.log("my column group open event called")
    sessionStorage.setItem('gateway_list_grid_column_group_state', JSON.stringify(this.gridColumnApi.getColumnGroupState()))
  }

  onColumnGroupClosed(params: any){
    console.log("my column group close event called")
  }

  onColumnResized(params: any) {
    this.saveGridStateInSession(this.gridColumnApi.getColumnState());
  }
  onSortChanged(params: any) {
    this.currentPage = 1;
    this.isFirst = true;
    this.saveGridStateInSession(this.gridColumnApi.getColumnState());
  }

  onFilterChanged(params: any) {
    this.currentPage = 1;
    this.isFirst = true;
    sessionStorage.setItem('gateway_list_grid_filter_state', JSON.stringify(params.api.getFilterModel()));
  }

  onColumnVisible(params: any) {
    this.saveGridStateInSession(this.gridColumnApi.getColumnState());
  }

  onColumnPinned(params: any) {
    this.saveGridStateInSession(this.gridColumnApi.getColumnState());
  }

  openExportPopup() {
    $('#exportCSVModal').modal('show');
  }

  exportDeviceList() {
    const exportColumnList = [];
    for (const iterator of this.gridColumnApi.getColumnState()) {
      if (!iterator.hide) {
        const obj = this.exportDeviceColumnName.find(item => item.dbName === iterator.colId);
        if (obj) {
          exportColumnList.push(obj.headerName);
        }
      }
    }
    let filterObj = this.filterValuesJsonObj;
    let accountNumberKey = false;
    if (!(filterObj && (Object.keys(filterObj).length === 0))) {
      Object.entries(filterObj).forEach(item => {
        if (item.length > 0) {
          const fil: any = item[1];
          if (fil.key === 'accountNumber') {
            accountNumberKey = true;
            fil.value = this.customerListForExport.toString();
            item[1] = fil;
          }
        }
      });
    }
    if (!accountNumberKey) {
      this.filterValues = new Map();
      this.elasticFilter.key = 'accountNumber';
      this.elasticFilter.operator = 'eq';
      this.elasticFilter.value = this.customerListForExport.toString();
      this.filterValues.set('filterValues1', this.elasticFilter);
      const jsonObject: any = {};
      this.filterValues.forEach((filter, key) => {
        jsonObject[key] = filter;
      });
      filterObj = jsonObject;
    }
    const exportObj = {
      filter_value: filterObj,
      header_name: exportColumnList
    };
    this.loading = true;
    this.gatewayListService.getDeivceCountByOrganization(this.customerListForExport).subscribe(data => {
      if (data > 10000) {
        this.loading = false;
        this.gatewayListService.exportDataIntoCSV(1, this.column, this.order, 20000,
          exportObj).subscribe(data => {
            this.toastr.success('Data Exported File sent to mail', null, { toastComponent: CustomSuccessToastrComponent });
            // this.loading = false;
          },
            error => {
              // this.loading = false;
              console.log(error);
            });
      } else {
        this.gatewayListService.exportDataIntoCSVInLocal(1, this.column, this.order, 10000,
          exportObj).subscribe(data => {
            this.toastr.success('Data Exported', null, { toastComponent: CustomSuccessToastrComponent });
            this.loading = false;
          },
            error => {
              this.loading = false;
              console.log(error);
            });
      }
    },
      error => {
        this.loading = false;
        console.log(error);
      });

  }

  deleteAllDevices() {
    this.selectedIdsToDelete = [];
    if (this.gatewayListService.gatewayList.length < 1) {
      this.toastr.error('Please select at least one device', 'No Device Selected', { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
      return;
    }

    for (const iterator of this.gatewayListService.gatewayList) {
      this.selectedIdsToDelete.push(iterator.uuid);
    }

    this.gatewayListService.bulkDeleteForGateways(this.selectedIdsToDelete).subscribe(data => {
      if (data.status) {
        this.selectedIdsToDelete = [];
        this.gatewayListService.gatewayList = [];
        this.toastr.success(data.message, null, { toastComponent: CustomSuccessToastrComponent });
        this.reloadComponent();
      } else {
        this.toastr.error('Something went wrong during deleting device', 'Device Problem Notification', { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
        this.reloadComponent();
      }
      this.isChecked = false;
    }, error => {
      console.log(error);
      this.reloadComponent();
    });

  }


  resetGateway() {
    if (this.gatewayListService.deviceId != null) {
      this.router.navigate(['gateway-profile']);
    }
    // if (this.gatewayListService.gatewayList.length < 1) {
    //   this.toastr.error('Please select at least one device', 'No Device Selected', { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
    //   return;
    // }
    // this.loading = true;
    // this.resetInstalationStatusservice.resetInstalationStatus(null, this.gatewayListService.gatewayList[0].imei, this.gatewayListService.gatewayList[0].account_number).subscribe(data => {
    //   if (data.status) {
    //     this.gatewayListService.gatewayList = [];
    //     this.toastr.success(data.message, null, { toastComponent: CustomSuccessToastrComponent });
    //     this.reloadComponent();
    //   } else {
    //     this.toastr.error(data.message, null, { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
    //   }
    //   this.loading = false;
    // }, error => {
    //   console.log(error);
    //   this.loading = false;
    // }
    // );
  }

  onFirstDataRendered(params) {
    this.isFirstTimeLoad = true;
  }

  saveGridStateInSession(columnState) {
    sessionStorage.setItem('gateway_list_grid_column_state', JSON.stringify(columnState));
  }


  getGateway(currentPage) {
    this.gatewayListService.getGatewayDetails(currentPage).subscribe(
      data => {
        this.gatewayList = data.body.content;
        this.totalpage = data.total_pages;
        this.showingpage = data.currentPage + 1;
        this.allItems = data.totalKey;
        this.countRecords(data);
        this.setPage(1, data.body, data.total_pages, data.body.length);
        if (data.body.content == null) {
          this.showingpage = 0;
        }
      },
      error => { throw error; },
      () => console.log('finished')
    );
  }


  countRecords(data) {
    if (data.body.content == null || data.body.content.length === 0) {
      this.totalRecords = 0;
      this.currentStartRecord = 0;
      this.currentEndRecord = 0;
    } else {
      this.totalRecords = data.total_key;
      this.currentStartRecord = (this.pageLimit * (data.current_page)) + 1;
      if (this.pageLimit === data.body.content.length) {
        this.currentEndRecord = this.pageLimit * (data.current_page + 1);
      } else {
        this.currentEndRecord = this.currentStartRecord + data.body.content.length - 1;
      }
    }
  }

  setPage(page: number, body, totalPages, pageItems) {
    this.pager = this.pagerService.getPager(this.allItems, page, totalPages, pageItems);
    this.pagedItems = body;
  }

  uploadgateways() {
    this.router.navigate(['upload-devices']);
  }

  filterForAgGrid(filterModel) {
    this.gridApi.paginationCurrentPage = 0;
    this.filterValuesJsonObj = {};
    this.filterValues = new Map();
    if (filterModel !== undefined && filterModel != null) {
      const elasticModelFilters = JSON.parse(JSON.stringify(filterModel));
      filterModel = elasticModelFilters;
      const Okeys = Object.keys(filterModel);
      let index = 0;
      for (let i = 0; i < Okeys.length; i++) {
        this.elasticFilter.key = Okeys[i];
        if (filterModel[Okeys[i]].type === 'contains') {
          this.elasticFilter.operator = 'like';
          this.elasticFilter.value = filterModel[Okeys[i]].filter;
          this.filterValues.set('filterValues ' + i, this.elasticFilter);
        } else if (filterModel[Okeys[i]].type === 'equals') {
          if (filterModel[Okeys[i]].filterType === 'date') {
            this.elasticFilter.operator = 'date';
            this.elasticFilter.value = filterModel[Okeys[i]].dateFrom;
          } else {
            this.elasticFilter.operator = 'eq';
            this.elasticFilter.value = filterModel[Okeys[i]].filter;
          }
          this.filterValues.set('filterValues ' + i, this.elasticFilter);
        } else if (filterModel[Okeys[i]].type === 'notEqual') {
          this.elasticFilter.operator = 'notequal';
          if (filterModel[Okeys[i]].filterType === 'date') {
            this.elasticFilter.value = filterModel[Okeys[i]].dateFrom;
          } else {
            this.elasticFilter.value = filterModel[Okeys[i]].filter;
          }
          this.filterValues.set('filterValues ' + i, this.elasticFilter);
        } else if (filterModel[Okeys[i]].type === 'inRange') {
          this.elasticFilter.operator = 'gt';
          this.elasticFilter.value = filterModel[Okeys[i]].dateFrom;
          this.filterValues.set('filterValues ' + i + 'a', this.elasticFilter);
          this.elasticFilter = new Filter();
          this.elasticFilter.key = Okeys[i];
          this.elasticFilter.operator = 'lt';
          this.elasticFilter.value = filterModel[Okeys[i]].dateTo;
          this.filterValues.set('filterValues ' + i + 'b', this.elasticFilter);
        } else if (filterModel[Okeys[i]].type === 'greaterThan') {
          this.elasticFilter.operator = 'gt';
          this.elasticFilter.value = filterModel[Okeys[i]].dateFrom;
          this.filterValues.set('filterValues ' + i + 'a', this.elasticFilter);
        } else if (filterModel[Okeys[i]].type === 'lessThan') {
          this.elasticFilter.operator = 'lt';
          this.elasticFilter.value = filterModel[Okeys[i]].dateFrom;
          this.filterValues.set('filterValues ' + i + 'a', this.elasticFilter);
        } else if (filterModel[Okeys[i]].filterType === 'set') {
          this.elasticFilter.operator = 'eq';
          if (this.elasticFilter.key === 'event_id' && filterModel[Okeys[i]].values && filterModel[Okeys[i]].values.length > 0) {
            const values = [];
            if (filterModel[Okeys[i]].values) {
              for (const iterator of filterModel[Okeys[i]].values) {
                const obj = this.eventList.find(item => item.eventType === iterator);
                if (obj) {
                  values.push(obj.eventId);
                }
              }
              this.elasticFilter.value = values.toString();
            }
          } else if (this.elasticFilter.key === 'installed_status_flag') {
            const values = [];
            if (filterModel[Okeys[i]].values) {
              for (const iterator of filterModel[Okeys[i]].values) {
                if (iterator === 'YES') {
                  values.push('Y');
                } else if (iterator === 'NO') {
                  values.push('N');
                } else {
                  values.push('N/A');
                }
              }
              this.elasticFilter.value = values.toString();
            }
          } else {
            this.elasticFilter.value = filterModel[Okeys[i]].values.toString();
          }
          this.filterValues.set('filterValues ' + i + 'a', this.elasticFilter);
        }
        // this.filterValues.set('filterValues ' + i, this.elasticFilter)
        this.elasticFilter = new Filter();
        index = i + 1;
      }
      this.prepareFilterObj(index);
      const jsonObject: any = {};
      this.filterValues.forEach((filter, key) => {
        jsonObject[key] = filter;
      });
      this.filterValuesJsonObj = jsonObject;

    }
  }

  // filterForAgGrid(filterModel) {
  //   console.log("filter values");
  //   console.log(filterModel);

  //   this.gridApi.paginationCurrentPage = 0;
  //   if (filterModel !== undefined) {


  //     this.filterValuesJsonObj = {};
  //     this.filterValues = new Map();

  //     if (filterModel.imei !== undefined && filterModel.imei.filter != null && filterModel.imei.filter !== undefined && filterModel.imei.filter !== '') {
  //       this.filterValues.set('imei', filterModel.imei.filter);
  //     }
  //     if (filterModel.product_name !== undefined && filterModel.product_name.values != null && filterModel.product_name.values !== undefined) {
  //       if (filterModel.product_name.values.length === 0) {
  //         this.filterValues.set('productName', 'null');
  //       } else {
  //         this.filterValues.set('productName', filterModel.product_name.values.toString());
  //       }
  //     }
  //     if (filterModel.created_by !== undefined && filterModel.created_by.values != null && filterModel.created_by.values !== undefined) {
  //       if (filterModel.created_by.values.length === 0) {
  //         this.filterValues.set('createdBy', 'null');
  //       } else {
  //         this.filterValues.set('createdBy', filterModel.created_by.values.toString());
  //       }
  //     }

  //     if (filterModel.qa_status !== undefined && filterModel.qa_status.values != null && filterModel.qa_status.values !== undefined) {
  //       if (filterModel.qa_status.values.length === 0) {
  //         this.filterValues.set('qaStatus', 'null');
  //       } else {
  //         this.filterValues.set('qaStatus', filterModel.qa_status.values.toString());
  //       }
  //     }
  //     if (filterModel.usage_status !== undefined && filterModel.usage_status.values != null && filterModel.usage_status.values !== undefined) {
  //       if (filterModel.usage_status.values.length === 0) {
  //         this.filterValues.set('usageStatus', 'null');
  //       } else {
  //         this.filterValues.set('usageStatus', filterModel.usage_status.values.toString());
  //       }
  //     }
  //     if (filterModel.service_country !== undefined && filterModel.service_country.filter != null && filterModel.service_country.filter !== undefined && filterModel.service_country.filter !== '') {
  //       this.filterValues.set('serviceCountry', filterModel.service_country.filter);
  //     }
  //     if (filterModel.service_network !== undefined && filterModel.service_network.filter != null && filterModel.service_network.filter !== undefined && filterModel.service_network.filter !== '') {
  //       this.filterValues.set('serviceNetwork', filterModel.service_network.filter);
  //     }
  //     if (filterModel.son !== undefined && filterModel.son.filter != null && filterModel.son.filter !== undefined && filterModel.son.filter !== '') {
  //       this.filterValues.set('son', filterModel.son.filter);
  //     }
  //     if (filterModel.imsi !== undefined && filterModel.imsi.filter != null && filterModel.imsi.filter !== undefined && filterModel.imsi.filter !== '') {
  //       this.filterValues.set('imsi', filterModel.imsi.filter);
  //     }
  //     if (filterModel.phone !== undefined && filterModel.phone.filter != null && filterModel.phone.filter !== undefined && filterModel.phone.filter !== '') {
  //       this.filterValues.set('phone', filterModel.phone.filter);
  //     }

  //     if (filterModel.last_report !== undefined && filterModel.last_report.dateFrom != null && filterModel.last_report.dateFrom !== undefined && filterModel.last_report.dateFrom !== '') {
  //       this.filterValues.set('lastReportDate', {
  //         operator: 'eq',
  //         value: filterModel.last_report.dateFrom
  //       });
  //     }
  //     if (filterModel.status !== undefined && filterModel.status.values != null && filterModel.status.values !== undefined) {
  //       if (filterModel.status.values.length === 0) {
  //         this.filterValues.set('status', 'null');
  //       } else {
  //         this.filterValues.set('status', filterModel.status.values.toString());
  //       }
  //     }
  //     if (filterModel.type1 !== undefined && filterModel.type1.filter != null && filterModel.type1.filter !== undefined && filterModel.type1.filter !== '') {
  //       this.filterValues.set('type1', filterModel.type1.filter);
  //     }
  //     if (filterModel.url1 !== undefined && filterModel.url1.filter != null && filterModel.url1.filter !== undefined && filterModel.url1.filter !== '') {
  //       this.filterValues.set('url1', filterModel.url1.filter);
  //     }
  //     if (filterModel.type2 !== undefined && filterModel.type2.filter != null && filterModel.type2.filter !== undefined && filterModel.type2.filter !== '') {
  //       this.filterValues.set('type2', filterModel.type2.filter);
  //     }
  //     if (filterModel.url2 !== undefined && filterModel.url2.filter != null && filterModel.url2.filter !== undefined && filterModel.url2.filter !== '') {
  //       this.filterValues.set('url2', filterModel.url2.filter);
  //     }
  //     if (filterModel.type3 !== undefined && filterModel.type3.filter != null && filterModel.type3.filter !== undefined && filterModel.type3.filter !== '') {
  //       this.filterValues.set('type3', filterModel.type3.filter);
  //     }
  //     if (filterModel.url3 !== undefined && filterModel.url3.filter != null && filterModel.url3.filter !== undefined && filterModel.url3.filter !== '') {
  //       this.filterValues.set('url3', filterModel.url3.filter);
  //     }
  //     if (filterModel.type4 !== undefined && filterModel.type4.filter != null && filterModel.type4.filter !== undefined && filterModel.type4.filter !== '') {
  //       this.filterValues.set('type4', filterModel.type4.filter);
  //     }
  //     if (filterModel.url4 !== undefined && filterModel.url4.filter != null && filterModel.url4.filter !== undefined && filterModel.url4.filter !== '') {
  //       this.filterValues.set('url4', filterModel.url4.filter);
  //     }
  //     const jsonObject = {};
  //     this.filterValues.forEach((filter, key) => {
  //       jsonObject[key] = filter;
  //     });
  //     this.filterValuesJsonObj = jsonObject;
  //     this.prepareFilterObj();
  //   }
  // }
  sortForAgGrid(sortModel) {
    this.gridApi.paginationCurrentPage = 0;
    if (sortModel[0] === undefined || sortModel[0] == null || sortModel[0].colId === undefined || sortModel[0].colId == null) {
      return;
    }
    this.sortFlag = true;
    switch (sortModel[0].colId) {
      case 'imei': {
        this.column = 'imei';
        this.order = sortModel[0].sort;
        break;
      }
      case 'product_name': {
        this.column = 'productName';
        this.order = sortModel[0].sort;
        break;
      }
      case 'device_type': {
        this.column = 'deviceType';
        this.order = sortModel[0].sort;
        break;
      }
      case 'created_by': {
        this.column = 'organisation.organisationName';
        this.order = sortModel[0].sort;
        break;
      }
      case 'last_report': {
        this.column = 'deviceDetails.latestReport';
        this.order = sortModel[0].sort;
        break;
      }

      case 'qa_status': {
        this.column = 'qaStatus';
        this.order = sortModel[0].sort;
        break;
      }
      case 'usage_status': {
        this.column = 'usageStatus';
        this.order = sortModel[0].sort;
        break;
      }

      case 'service_country': {
        this.column = 'cellular.serviceCountry';
        this.order = sortModel[0].sort;
        break;
      }

      case 'service_network': {
        this.column = 'cellular.serviceNetwork';
        this.order = sortModel[0].sort;
        break;
      }
      case 'son': {
        this.column = 'son';
        this.order = sortModel[0].sort;
        break;
      }
      case 'status': {
        this.column = 'status';
        this.order = sortModel[0].sort;
        break;
      }
      case 'cellular': {
        this.column = 'cellular.cellular';
        this.order = sortModel[0].sort;
        break;
      }
      case 'phone': {
        this.column = 'cellular.phone';
        this.order = sortModel[0].sort;
        break;
      }
      case 'type1': {
        this.column = 'device_forwarding_payload[0].type';
        this.order = sortModel[0].sort;
        break;
      }
      case 'url1': {
        this.column = 'device_forwarding_payload[0].url';
        this.order = sortModel[0].sort;
        break;
      }
      case 'qa_date': {
        this.column = 'qaDate';
        this.order = sortModel[0].sort;
        break;
      }
      case 'asset_type': {
        this.column = 'assetDeviceXref.asset.category';
        this.order = sortModel[0].sort;
        break;
      }
      case 'asset_name': {
        this.column = 'assetDeviceXref.asset.assignedName';
        this.order = sortModel[0].sort;
        break;
      }
      case 'vin': {
        this.column = 'assetDeviceXref.asset.vin';
        this.order = sortModel[0].sort;
        break;
      }
      case 'manufacturer': {
        this.column = 'assetDeviceXref.asset.manufacturer.name';
        this.order = sortModel[0].sort;
        break;
      }
      case 'last_report_date_time': {
        this.column = 'deviceDetails.latestReport';
        this.order = sortModel[0].sort;
        break;
      }
      case 'event_id': {
        this.column = 'deviceDetails.eventId';
        this.order = sortModel[0].sort;
        break;
      }
      case 'event_type': {
        this.column = 'deviceDetails.eventType';
        this.order = sortModel[0].sort;
        break;
      }
      case 'battery': {
        this.column = 'deviceDetails.battery';
        this.order = sortModel[0].sort;
        break;
      }
      case 'lat': {
        this.column = 'deviceDetails.lat';
        this.order = sortModel[0].sort;
        break;
      }
      case 'longitude': {
        this.column = 'deviceDetails.longitude';
        this.order = sortModel[0].sort;
        break;
      }
      case 'bin_version': {
        this.column = 'deviceDetails.binVersion';
        this.order = sortModel[0].sort;
        break;
      }
      case 'app_version': {
        this.column = 'deviceDetails.appVersion';
        this.order = sortModel[0].sort;
        break;
      }
      case 'mcu_version': {
        this.column = 'deviceDetails.mcuVersion';
        this.order = sortModel[0].sort;
        break;
      }
      case 'ble_version': {
        this.column = 'deviceDetails.bleVersion';
        this.order = sortModel[0].sort;
        break;
      }
      case 'config1_name': {
        this.column = 'deviceDetails.config1Name';
        this.order = sortModel[0].sort;
        break;
      }
      case 'config1_crc': {
        this.column = 'deviceDetails.config1CRC';
        this.order = sortModel[0].sort;
        break;
      }
      case 'config2_name': {
        this.column = 'deviceDetails.config2Name';
        this.order = sortModel[0].sort;
        break;
      }
      case 'config2_crc': {
        this.column = 'deviceDetails.config2CRC';
        this.order = sortModel[0].sort;
        break;
      }
      case 'config3_name': {
        this.column = 'deviceDetails.config3Name';
        this.order = sortModel[0].sort;
        break;
      }
      case 'config3_crc': {
        this.column = 'deviceDetails.config3CRC';
        this.order = sortModel[0].sort;
        break;
      }
      case 'config4_name': {
        this.column = 'deviceDetails.config4Name';
        this.order = sortModel[0].sort;
        break;
      }
      case 'config4_crc': {
        this.column = 'deviceDetails.config4CRC';
        this.order = sortModel[0].sort;
        break;
      }
      case 'devuser_cfg_name': {
        this.column = 'deviceDetails.devuserCfgName';
        this.order = sortModel[0].sort;
        break;
      }
      case 'devuser_cfg_value': {
        this.column = 'deviceDetails.devuserCfgValue';
        this.order = sortModel[0].sort;
        break;
      }
      case 'installedDateTimestamp': {
        this.column = 'installedDateTimestamp';
        this.order = sortModel[0].sort;
        break;
      }
      case 'imei_hashed': {
        this.column = 'deviceSignature.imeiHashed';
        this.order = sortModel[0].sort;
        break;
      }
      case 'forwarding_group': {
        this.column = 'organisation.forwardingGroupMappers.customerForwardingGroup.name';
        this.order = sortModel[0].sort;
        break;
      }
      case 'purchase_by_name': {
        this.column = 'purchaseBy.organisationName';
        this.order = sortModel[0].sort;
        break;
      }
      case 'config_name': {
        this.column = 'configName';
        this.order = sortModel[0].sort;
        break;
      }
      case 'installed_status_flag': {
        this.column = 'installedStatusFlag';
        this.order = sortModel[0].sort;
        break;
      }
      default: {
      }
    }
  }

  onCellClicked(params) {
    if (params.column.colId === 'imei') {
      sessionStorage.setItem('device_id', params.data.imei);
      this.gatewayListService.deviceId = params.data.imei;
      sessionStorage.setItem('device_uuid', params.data.uuid);
      this.atcommandService.deviceId = this.gatewayListService.deviceId;
      if (this.gatewayListService.deviceId != null) {
        this.router.navigate(['gateway-profile']);
      }
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
      const that = this;
      function onClearFilterButtonClicked() {
        sessionStorage.setItem('organisationNameForGatewayList', null);
        sessionStorage.setItem('filterNameForGatewaySummary', null);
        this.column = 'imei';
        this.order = 'asc';
        params.api.setFilterModel(null);
        params.api.onFilterChanged();
        sessionStorage.setItem('custom_gateway_list_grid_filter_state', null);
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

  getPurchaseByFilterValuesData() {
    return this.resellerCompanies;
  }

  getAllcompanies() {
    this.gatewayListService.getAllCustomer(['End_Customer']).subscribe(
      data => {
        this.companies = data && data.length > 0 ? data : [];
      },
      error => {
        console.log(error);
      }
    );
  }

  getAllResellerCompanies() {
    this.gatewayListService.getAllCustomer(['RESELLER']).subscribe(
      data => {
        this.resellerCompanies = data && data.length > 0 ? data : [];
      },
      error => {
        console.log(error);
      }
    );
  }

  onCompanySelect(event) {
    this.company = event.value;
    sessionStorage.setItem('selectedCompany', JSON.stringify(this.company));
    this.companyname = event.value;
    this.days = 7;
    this.onGridReady(this.paramList);
  }

  onDateRangeSelect(event) {
    this.days = event.value;
    sessionStorage.setItem('selectedDays', JSON.stringify(this.days));
    this.onGridReady(this.paramList);
  }

  headerHeightSetter(event) {
    this.gridApi.setHeaderHeight(headerHeightGetter() + 20);
    this.gridApi.resetRowHeights();
  }

  getAllCustomer(name?: any) {
    if (!name) {
      name = '';
    }
    this.gatewayListService.getAllCustomerObject(['End_Customer'], name).subscribe(
      data => {
        if (!this.roleCallCenterUser) {
          const recentCustomer = JSON.parse(localStorage.getItem('gatewayRecentSearch'));
          const recentCustomer2 = JSON.parse(localStorage.getItem('gatewayRecentSearch2'));
          if (data !== undefined && data != null && data.length > 0) {
            if (recentCustomer) {
              let i = 0;
              for (const iterator of recentCustomer) {
                if (recentCustomer2 && recentCustomer2.length === 1) {
                  const index = data.map(function (item) { return item.accountNumber; }).indexOf(recentCustomer2[0]);
                  if (index !== -1) {
                    this.customerName = data[index].organisationName;
                  }
                }
                if (i < 3) {
                  const index = data.map(function (item) { return item.accountNumber; }).indexOf(iterator);
                  const index2 = this.recentCustomerList.map(function (item) { return item.accountNumber; }).indexOf(iterator);
                  if (index !== -1 && index2 === -1) {
                    this.recentCustomerList.push(data[index]);
                    if (this.recentCustomerList.length > 3) {
                      this.recentCustomerList.shift();
                    }
                    data.splice(index, 1);
                    i++;
                  }
                } else {
                  break;
                }
              }
              localStorage.setItem('gatewayRecentSearchList', JSON.stringify(this.recentCustomerList));
            }
            this.customerList = data;
          }
        } else {
          const obj = data.find(item => item.accountNumber === 'A-00243');
          if (obj) {
            this.customerList.push(obj);
          }
        }
      },
      error => {
        console.log(error);
      }
    );
  }


  prepareFilterObj(i: number) {
    const filteValues = this.filterForm.value;
    if (filteValues.imei_selection === '2' && filteValues.imei_ids && filteValues.imei_ids.length > 0) {
      // this.filterValuesJsonObj.imeiList = this.filterForm.value.imei_ids;
      this.elasticFilter = new Filter();
      i++;
      this.elasticFilter.key = 'imeiList';
      this.elasticFilter.operator = 'eq';
      const values = this.filterForm.value.imei_ids.split(/[ ,\n]+/);
      this.elasticFilter.value = values.toString();
      this.filterValues.set('filterValues ' + i + 'a', this.elasticFilter);
    }

    if (filteValues.asset_selection === '2' && filteValues.asset_ids && filteValues.asset_ids.length > 0) {
      // this.filterValuesJsonObj.assetIdList = this.filterForm.value.asset_ids;
      this.elasticFilter = new Filter();
      i++;
      this.elasticFilter.key = 'assetIdList';
      this.elasticFilter.operator = 'eq';
      const values = this.filterForm.value.asset_ids.split(/[ ,\n]+/);
      this.elasticFilter.value = values.toString();
      this.filterValues.set('filterValues ' + i + 'a', this.elasticFilter);
    }

    if (filteValues.customer_list && filteValues.customer_list !== '' && filteValues.customer_list.length > 0) {
      // this.filterValuesJsonObj.accountNumber = this.filterForm.value.customer_list.toString();
      this.elasticFilter = new Filter();
      i++;
      this.elasticFilter.key = 'accountNumber';
      this.elasticFilter.operator = 'eq';
      const recentSearch = [];
      this.filterForm.value.customer_list.forEach(element => {
        recentSearch.push(element);
      });
      localStorage.setItem('gatewayRecentSearch2', JSON.stringify(recentSearch));
      if (recentSearch.length <= 2) {
        for (const iterator of this.recentCustomerList) {
          const index = recentSearch.map(function (item) { return item; }).indexOf(iterator.accountNumber);
          if (index === -1) {
            recentSearch.push(iterator.accountNumber);
          }
        }
      }
      localStorage.setItem('gatewayRecentSearch', JSON.stringify(recentSearch));
      this.elasticFilter.value = this.filterForm.value.customer_list.toString();
      this.filterValues.set('filterValues ' + i + 'a', this.elasticFilter);
    }

    if (filteValues.qa_status && filteValues.qa_status !== '') {
      this.elasticFilter = new Filter();
      i++;
      this.elasticFilter.key = 'qa_status';
      this.elasticFilter.operator = 'eq';
      this.elasticFilter.value = this.filterForm.value.qa_status.toString();
      this.filterValues.set('filterValues ' + i + 'a', this.elasticFilter);
    }
    if (filteValues.date_rang && filteValues.date_rang !== '' && filteValues.from && filteValues.from !== '' && filteValues.to && filteValues.to !== '') {
      this.elasticFilter = new Filter();
      i++;
      this.elasticFilter.key = 'qa_date_advance';
      this.elasticFilter.operator = 'gt';
      this.elasticFilter.value = this.filterForm.value.from;
      this.filterValues.set('filterValues ' + i + 'a', this.elasticFilter);
      this.elasticFilter = new Filter();
      this.elasticFilter.key = 'qa_date_advance';
      this.elasticFilter.operator = 'lt';
      this.elasticFilter.value = this.filterForm.value.to;
      this.filterValues.set('filterValues ' + i + 'b', this.elasticFilter);
    }
  }

  initilizeFilterForm(data?: any) {
    this.filterForm = this.formBuldier.group({
      imei_selection: [data && data.imei_selection ? data.imei_selection : '1'],
      imei_ids: [data && data.imei_ids ? data.imei_ids : ''],
      asset_selection: [data && data.asset_selection ? data.asset_selection : '1'],
      asset_ids: [data && data.asset_ids ? data.asset_ids : ''],
      customer_list: [data && data.customer_list ? data.customer_list : ''],
      date_rang: [data && data.date_rang ? data.date_rang : ''],
      from: [data && data.from ? data.from : ''],
      to: [data && data.to ? data.to : ''],
      customer_name: [data && data.customer_name ? data.customer_name : ''],
      qa_status: [data && data.qa_status ? data.qa_status : ''],
      source: [data && data.source ? data.source : '1']
    });
    if (data && data.date_rang) {
      this.selectChangeHandler({ value: data.date_rang }, false);
    }
  }

  SearchData(data) {
    this.filterForm.controls.customer_list.patchValue([]);
    this.getAllCustomer(data);
  }
  toggleAllSelection(event) {
    if (event._selected) {
      let value1 = this.customerList.map(item => item.accountNumber);
      const value2 = this.recentCustomerList.map(item => item.accountNumber);
      // let values=[this.customerList.map(item => item.accountNumber)]
      value1 = value1.concat(value2);
      this.filterForm.controls.customer_list
        .patchValue(value1);
      event._selected = true;
      if (this.filterForm.value.customer_list && this.filterForm.value.customer_list.length === 1 && this.filterForm.value.customer_list[0] !== 0) {
        const obj = this.customerList.find(item => item.accountNumber === this.filterForm.value.customer_list[0]);
        const obj2 = this.recentCustomerList.find(item => item.accountNumber === this.filterForm.value.customer_list[0]);
        if (obj) {
          this.customerName = obj.organisationName;
        }
        if (obj2) {
          this.customerName = obj2.organisationName;
        }
      }
      this.filterForm.updateValueAndValidity();

    } else {
      event._selected = false;
      this.filterForm.controls.customer_list.patchValue([]);
    }
  }

  toggleAllSelection2(event) {
    if (event._selected) {
      let value1 = this.customerList.map(item => item.accountNumber);
      value1.push(0);
      this.customerListForExport = value1;
      event._selected = true;
    } else {
      event._selected = false;
      this.customerListForExport = [];
    }
  }
  selectCustomerChangeHandler(event: any) {
    if (event.value.length === 1 && event.value[0] !== 0) {
      const obj = this.customerList.find(item => item.accountNumber === event.value[0]);
      if (obj) {
        this.customerName = obj.organisationName;
      }
      const obj2 = this.recentCustomerList.find(item => item.accountNumber === event.value[0]);
      if (obj2) {
        this.customerName = obj2.organisationName;
      }
    }
    this.advanceFilte();
  }

  selectCustomerChangeHandler2(event: any) {
    // if (event.value.length === 1 && event.value[0] !== 0) {
    //   const obj = this.customerList.find(item => item.accountNumber === event.value[0]);
    //   if (obj) {
    //     this.customerName = obj.organisationName;
    //   }
    // }
  }

  toggleAllSelectionStatus(event) {
    if (event._selected) {
      this.filterForm.controls.qa_status
        .patchValue(this.qaStatusList.map(item => item.key));
      event._selected = true;
      this.filterForm.updateValueAndValidity();

    } else {
      event._selected = false;
      this.filterForm.controls.qa_status.patchValue([]);
    }
  }

  clearAdvanceFilter(data) {
    switch (data) {
      case 'ImeiFilter':
        this.filterForm.controls.imei_selection.setValue('1');
        this.filterForm.controls.imei_ids.setValue('');
        this.filterForm.updateValueAndValidity();
        this.imeiFilter = true;
        this.assetFilter = this.customerFilter = this.reportFilter = this.sensorFilter
          = this.eventFilter = this.groupFilter = this.qaStatusFilter = this.sourceFilter = false;
        break;
      case 'AssetFilter':
        this.filterForm.controls.asset_selection.setValue('1');
        this.filterForm.controls.asset_ids.setValue('');
        this.filterForm.updateValueAndValidity();
        this.assetFilter = true;
        this.imeiFilter = this.customerFilter = this.reportFilter = this.sensorFilter
          = this.eventFilter = this.groupFilter = this.qaStatusFilter = this.sourceFilter = false;
        break;
      case 'CustomerFilter':
        this.getAllCustomer();
        this.filterForm.controls.customer_list.setValue('');
        this.filterForm.updateValueAndValidity();
        this.customerFilter = true;
        this.assetFilter = this.imeiFilter = this.reportFilter = this.sensorFilter
          = this.eventFilter = this.groupFilter = this.qaStatusFilter = this.sourceFilter = false;
        break;
      case 'ReportFilter':
        this.filterForm.controls.date_rang.setValue('');
        this.filterForm.controls.from.setValue('');
        this.filterForm.controls.to.setValue('');
        this.filterForm.updateValueAndValidity();
        this.reportFilter = true;
        this.assetFilter = this.customerFilter = this.imeiFilter = this.sensorFilter
          = this.eventFilter = this.groupFilter = this.qaStatusFilter = this.sourceFilter = false;
        break;
      case 'QaStatusFilter':
        this.filterForm.controls.qa_status.setValue('');
        this.filterForm.updateValueAndValidity();
        this.qaStatusFilter = true;
        this.assetFilter = this.customerFilter = this.reportFilter = this.sensorFilter
          = this.eventFilter = this.groupFilter = this.imeiFilter = this.sourceFilter = false;
        break;
      case 'SourceFilter':
        this.sourceFilter = this.sourceFilter ? false : true;
        this.assetFilter = this.customerFilter = this.reportFilter = this.sensorFilter
          = this.eventFilter = this.groupFilter = this.imeiFilter = this.qaStatusFilter
          = false;
        break;
      default:
        break;
    }
    this.advanceFilte();
  }

  correctEvent = true;
  onDateChange(event: any, which: any) {
    console.log(event);
    if (this.correctEvent) {
      this.filterForm.controls.date_rang.setValue('time_rang');
      this.qaDateType = 'Range';
      this.dateIsReadOnly = false;
    } else {
      if (which === 'TO') {
        this.correctEvent = true;
        this.advanceFilte();
      }
    }
  }

  selectChangeHandler(event: any, correct?: boolean) {
    // this.dateIsReadOnly = true;
    this.correctEvent = false;
    switch (event.value) {
      case 'last_hour':
        this.filterForm.controls.from.setValue(new Date(new Date().getTime() - (1000 * 60 * 60)));
        this.filterForm.controls.to.setValue(new Date());
        this.qaDateType = 'Last Hour';
        break;
      case 'last_24_hour':
        this.filterForm.controls.from.setValue(new Date(new Date().getTime() - (1000 * 60 * 60 * 24)));
        this.filterForm.controls.to.setValue(new Date());
        this.qaDateType = 'Last24Hour';
        break;
      case 'last_week':
        this.filterForm.controls.from.setValue(new Date(new Date().getTime() - (1000 * 60 * 60 * 24 * 7)));
        this.filterForm.controls.to.setValue(new Date());
        this.qaDateType = 'Last Week';
        break;
      case 'last_month':
        this.filterForm.controls.from.setValue(new Date(new Date().getTime() - (1000 * 60 * 60 * 24 * 30)));
        this.filterForm.controls.to.setValue(new Date());
        this.qaDateType = 'Last Month';
        break;
      case 'last_year':
        this.filterForm.controls.from.setValue(new Date(new Date().getTime() - (1000 * 60 * 60 * 24 * 365)));
        this.filterForm.controls.to.setValue(new Date());
        this.qaDateType = 'Last Year';
        break;
      case 'time_rang':
        this.qaDateType = 'Range';
        this.dateIsReadOnly = false;
        break;
      case '':
        this.filterForm.controls.from.setValue('');
        this.filterForm.controls.to.setValue('');
        break;
      default:
        break;
    }
    this.advanceFilte();
  }


  showHideFilter(data: any) {
    // this.isFilterClick = true;
    switch (data) {
      case 'ImeiFilter':
        this.imeiFilter = this.imeiFilter ? false : true;
        this.assetFilter = this.customerFilter = this.reportFilter = this.sensorFilter
          = this.eventFilter = this.groupFilter = this.qaStatusFilter = this.sourceFilter = false;
        break;
      case 'AssetFilter':
        this.assetFilter = this.assetFilter ? false : true;
        this.imeiFilter = this.customerFilter = this.reportFilter = this.sensorFilter
          = this.eventFilter = this.groupFilter = this.qaStatusFilter = this.sourceFilter = false;
        break;
      case 'CustomerFilter':
        this.customerFilter = this.customerFilter ? false : true;
        this.assetFilter = this.imeiFilter = this.reportFilter = this.sensorFilter
          = this.eventFilter = this.groupFilter = this.qaStatusFilter = this.sourceFilter = false;
        break;
      case 'ReportFilter':
        this.reportFilter = this.reportFilter ? false : true;
        this.assetFilter = this.customerFilter = this.imeiFilter = this.sensorFilter
          = this.eventFilter = this.groupFilter = this.qaStatusFilter = this.sourceFilter = false;
        break;
      case 'SensorFilter':
        this.sensorFilter = this.sensorFilter ? false : true;
        this.assetFilter = this.customerFilter = this.reportFilter = this.imeiFilter
          = this.eventFilter = this.groupFilter = this.qaStatusFilter = this.sourceFilter = false;
        break;
      case 'EventFilter':
        this.eventFilter = this.eventFilter ? false : true;
        this.assetFilter = this.customerFilter = this.reportFilter = this.sensorFilter
          = this.imeiFilter = this.groupFilter = this.qaStatusFilter = this.sourceFilter = false;
        break;
      case 'GroupFilter':
        this.groupFilter = this.groupFilter ? false : true;
        this.assetFilter = this.customerFilter = this.reportFilter = this.sensorFilter
          = this.eventFilter = this.imeiFilter = this.qaStatusFilter = this.sourceFilter = false;
        break;
      case 'QaStatusFilter':
        this.qaStatusFilter = this.qaStatusFilter ? false : true;
        this.assetFilter = this.customerFilter = this.reportFilter = this.sensorFilter
          = this.eventFilter = this.groupFilter = this.imeiFilter = this.sourceFilter = false;
        break;
      case 'SourceFilter':
        this.sourceFilter = this.sourceFilter ? false : true;
        this.assetFilter = this.customerFilter = this.reportFilter = this.sensorFilter
          = this.eventFilter = this.groupFilter = this.imeiFilter = this.qaStatusFilter
          = false;
        break;
      default:
        break;
    }
  }

  hide() {
    // !this.isFilterClick && 
    if ((this.imeiFilter || this.assetFilter || this.customerFilter
      || this.reportFilter || this.sensorFilter
      || this.eventFilter || this.groupFilter
      || this.qaStatusFilter || this.sourceFilter)) {
      this.imeiFilter = this.assetFilter = this.customerFilter = this.reportFilter = this.sensorFilter
        = this.eventFilter = this.groupFilter = this.qaStatusFilter = this.sourceFilter = false;
    }
    // this.isFilterClick = false;
  }

  advanceFilte() {
    this.hide();
    if (this.filterForm.value.imei_ids.length > 0 || this.filterForm.value.customer_list.length > 0 || this.filterForm.value.asset_ids.length > 0
      || this.filterForm.value.qa_status.length > 0 || this.filterForm.value.from !== "" || this.filterForm.value.to !== "" || this.filterForm.value.date_rang !== "") {
      this.showRecords = true;
    } else
      this.showRecords = false;
    sessionStorage.setItem('gateway_advance_save_filter', JSON.stringify(this.filterForm.value));
    this.gridApi.onFilterChanged();
    this.getAllCustomer();
    this.hide();
  }


  gotoGatewayDataForwardRule() {
    this.gatewayList = [];
    this.selectedIdsToDelete = [];
    this.gatewayList = this.getSelectedRowData();
    this.gatewayListService.gatewayList = [];
    this.gatewayListService.gatewayList = this.gatewayList;

    if (this.gatewayListService.gatewayList != null && this.gatewayListService.gatewayList.length <= 0) {
      this.toastr.error('Please select atleast one gateway to apply the action', 'Null Value Validation', { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
    } else if (this.dataForwardAction === 'Choose Action') {
      this.toastr.error('Please select the action first', 'Dropdown Value Validation', { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
    } else if (this.dataForwardAction === 'BatchEdit') {

      sessionStorage.setItem('batch_edit_device_list', JSON.stringify(this.gatewayList));
      this.router.navigate(['batch-device-edit']);
    } else {
      this.router.navigate(['gateway-data-forward-rule']);
    }


  }

  getForwardingGroupsFilterValues() {
    return this.forwardingGroups;
  }

  getAllForwardingGroups() {
    this.companyService.getAllCustomerForwardingGroupName().subscribe(data => {
      this.forwardingGroups = data;
    }, error => {
      console.log(error);
    });
  }

  selectSpecificImei() {
    this.filterForm.controls.imei_selection.setValue('2');
  }

  selectSpecificAssets() {
    this.filterForm.controls.asset_selection.setValue('2');
  }

  selectSpecificImei2()
  {
    this.filterForm.controls.imei_selection.setValue('2');
    this.advanceFilte();
  }

  selectSpecificAssets2()
  {
    this.filterForm.controls.asset_selection.setValue('2');
    this.advanceFilte();
  }

  selectAnyForSpecificImei()
  {
    this.filterForm.controls.imei_selection.setValue('1');
    this.advanceFilte();
  }

  selectAnyForSpecificAssets()
  {
    this.filterForm.controls.asset_selection.setValue('1');
    this.advanceFilte();
  }
}

function headerHeightGetter() {
  const columnHeaderTexts = document.querySelectorAll('.ag-header-cell-text');
  const columnHeaderTextsArray = [];
  columnHeaderTexts.forEach(node => columnHeaderTextsArray.push(node));
  const clientHeights = columnHeaderTextsArray.map(
    headerText => headerText.clientHeight
  );
  const tallestHeaderTextHeight = Math.max(...clientHeights);
  return tallestHeaderTextHeight;
}



const filterParamsProductType = {
  values: function (params) {
    setTimeout(function () {
      params.success([
        'ChassisNet',
        'TrailerNet',
        'Smart7',
        'Stealthnet',
        'SmartPair'
      ]);
    }, 2000);
  },
};


const filterParamsModel = {
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
        'Sabre',
        'StealthNet',
        'SmartPair',
        'Smart7 lid',
        'Freight-I',
        'Freight-la',
        'Arrow-l',
        'Freight-l',
        'Cutlass-l',
        'Dagger67-lg',
        'katana-h',
        'talon-lte']);
    }, 2000);
  },
};

const filterParams = {
  values: function (params) {
    setTimeout(function () {
      params.success(['Pending', 'Install in progress', 'Active', 'Installed', 'Inactive']);
    }, 2000);
  },
};

const filterParamsUsageStatus = {
  values: function (params) {
    setTimeout(function () {
      params.success(['RMA', 'Production', 'Engineering', 'Pilot', 'EOL', 'unknown', 'installed']);
    }, 2000);
  },
};

const filterParamsQaStatus = {
  values: function (params) {
    setTimeout(function () {
      params.success(['Passed', 'Failed', 'Pending']);
    }, 2000);
  },
};

const filterParamsSecStatus = {
  values: function (params) {
    setTimeout(function () {
      params.success(['Secured', 'Unsecured']);
    }, 2000);
  },
};

const filterParamsForwardingType = {
  values: function (params) {
    setTimeout(function () {
      params.success(['elastic', 'json', 'ups', 'eroad']);
    }, 2000);
  },
};

const filterParamsForAssetStatus = {
  values: function (params) {
    setTimeout(function () {
      params.success(['TRAILER', 'CHASSIS', 'CONTAINER ', 'VEHICLE', 'TRACTOR']);
    }, 2000);
  },
};

const filterParamsInstalledStatusFlag = {
  values: function (params) {
    setTimeout(function () {
      params.success(['YES', 'NO', 'NULL']);
    }, 2000);
  },
};


