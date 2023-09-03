import { Component, OnInit, ViewChild } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { PagerService } from '../util/pagerService';
import { AgGridAngular } from '@ag-grid-community/angular';
import { GridOptions, IGetRowsParams } from '@ag-grid-community/all-modules';
import { ClientSideRowModelModule } from '@ag-grid-community/client-side-row-model';
import { MenuModule } from '@ag-grid-enterprise/menu';
import { ColumnsToolPanelModule } from '@ag-grid-enterprise/column-tool-panel';
import '@ag-grid-community/core/dist/styles/ag-grid.css';
import '@ag-grid-community/core/dist/styles/ag-theme-alpine.css';
import { ColumnApi, GridApi, Module } from '@ag-grid-community/core';
import { MultiFilterModule } from '@ag-grid-enterprise/multi-filter';
import { FormControl } from '@angular/forms';
import * as moment from 'moment';
import { CustomDateComponent } from '../ui-elements/custom-date-component.component';
import { GatewayListService } from '../gateway-list.service';
import { CustomHeader } from '../ui-elements/custom-header.component';
import { RowGroupingModule } from '@ag-grid-enterprise/all-modules';
import { PaginationConstants } from '../util/paginationConstants';
import { CustomSuccessToastrComponent } from '../custom-success-toastr/custom-success-toastr.component';
import { CustomErrorToastrComponent } from '../custom-error-toastr/custom-error-toastr.component';
import { ResetInstalationStatusService } from '../reset-installation-status/reset-installation-status.service';
import { ATCommandService } from '../atcommand.service';
import { BtnCellRendererForDeviceProvisioningComponent } from './btn-cell-renderer.component';
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
  selector: 'app-device-provisioning',
  templateUrl: './device-provisioning.component.html',
  styleUrls: ['./device-provisioning.component.css']
})
export class DeviceProvisioningComponent implements OnInit {


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
  rolePCTUser;
  isRoleCustomerManager;
  // parameters for counting records
  filterValues = new Map();
  filterValuesJsonObj: any = {};
  isClearButtonVisible = false;
  loading = false;
  frameworkComponents;
  public sideBar;
  selectAll = false;
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


  // Pagination Save State
  isFirstTimeLoad = false;
  isPaginationStateRecovered = false;
  filterModelCountFilter: any;
  userNameList: string[];
  companies: any[];
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
  organisationId: any;
  constructor(
    private readonly atcommandService: ATCommandService,
    private readonly gatewayListService: GatewayListService,
    private readonly router: Router,
    private readonly toastr: ToastrService,
    private readonly pagerService: PagerService,
    private readonly resetInstalationStatusservice: ResetInstalationStatusService,
    private readonly activateRoute: ActivatedRoute) {
    this.activateRoute.queryParams.subscribe(params => {
      this.organisationId = params.id;
    });
    const containsFilterParams = {
      filterOptions: [
        'contains',
      ],
      suppressAndOrCondition: true
    };

    const filterParamsCustomers = {
      values: params => params.success(this.getFilterValuesData())
    };


    const filterParams2 = {
      suppressAndOrCondition: true,
    };
    this.columnDefs = [
      {
        headerName: '#', minWidth: 20, width: 20, headerCheckboxSelection: true,
        headerCheckboxSelectionFilteredOnly: true, filter: false,
        checkboxSelection: true,
      },
      {
        headerName: 'Order #', field: 'order_no', sortable: true, filter: 'agTextColumnFilter', floatingFilter: true, filterParams: containsFilterParams,
      },
      {
        headerName: 'Customer', field: 'company_name', width: 80, sortable: true, filter: 'agTextColumnFilter', floatingFilter: true, filterParams: containsFilterParams,
      },
      {
        headerName: 'Device ID', field: 'imei',
        suppressMovable: true,
        cellClass: 'suppress-movable-col',
        suppressColumnsToolPanel: true, minWidth: 180, sortable: true, filter: 'agTextColumnFilter', filterParams: containsFilterParams,
        cellRenderer: function (params) {
          if (params && params.value) {
            return '<a style="color:#4ba9d7">' + params.value + '</a>';
          }
        }
      },
      {
        headerName: 'Product', field: 'product_name', width: 60, sortable: true, filter: 'agSetColumnFilter', filterParams: filterParamsProductType
      },
      {
        headerName: 'Status', field: 'status', sortable: true, filter: 'agSetColumnFilter', filterParams: filterParams, floatingFilter: true,
      }, {
        headerName: 'Type', field: 'gateway_type', sortable: true, filter: false, filterParams: filterParamsForType, floatingFilter: true, hide: true
      },
      {
        headerName: 'PCT Cargo (77-S180)', field: 'pct_cargo_sensor', sortable: false, filter: false, width: 180, hide: true,
        cellRenderer: function (params) {
          if (params && params.value) {
            return params.value;
          } else {
            return '--';
          }
        }, cellStyle: params => {
          if (params && params.value && params.value !== 'N/A' && params.value !== '0') {
            // mark police cells as red
            return { backgroundColor: 'lightyellow' };
          }
          return null;
        }
      },
      {
        headerName: 'MicroSP Transceiver (77-S196)', field: 'micro_sp_transceiver', sortable: false, filter: false, width: 180, hide: true,
        cellRenderer: function (params) {
          if (params && params.value) {
            return params.value;
          } else {
            return '--';
          }
        }, cellStyle: params => {
          if (params && params.value && params.value !== 'N/A' && params.value !== '0') {
            // mark police cells as red
            return { backgroundColor: 'lightyellow' };
          }
          return null;
        }
      },
      // {
      //   headerName: 'MicroSP TPMS (77-S177)', field: 'micro_sp_tpms', sortable: false, filter: false, width: 180,
      //   cellRenderer: function (params) {
      //     if (params && params.value) {
      //       return params.value;
      //     } else {
      //       return '--';
      //     }
      //   }, cellStyle: params => {
      //     if (params && params.value && params.value !== 'N/A' && params.value !== '0') {
      //       // mark police cells as red
      //       return { backgroundColor: 'lightyellow' };
      //     }
      //     return null;
      //   }
      // },
      {
        headerName: '4x2 - MicroSP TPMS Sensors', field: 'micro_sp_tpms4_x2', sortable: false, filter: false, width: 180,
        cellRenderer: function (params) {
          if (params && params.value) {
            return params.value;
          } else {
            return '--';
          }
        }, cellStyle: params => {
          if (params && params.value && params.value !== 'N/A' && params.value !== '0') {
            // mark police cells as red
            return { backgroundColor: 'lightyellow' };
          }
          return null;
        }
      },
      {
        headerName: '4x1 - MicroSP TPMS Sensors', field: 'micro_sp_tpms4_x1', sortable: false, filter: false, width: 180,
        cellRenderer: function (params) {
          if (params && params.value) {
            return params.value;
          } else {
            return '--';
          }
        }, cellStyle: params => {
          if (params && params.value && params.value !== 'N/A' && params.value !== '0') {
            // mark police cells as red
            return { backgroundColor: 'lightyellow' };
          }
          return null;
        }
      },
      {
        headerName: '2x2 - MicroSP TPMS Sensors', field: 'micro_sp_tpms2_x2', sortable: false, filter: false, width: 180,
        cellRenderer: function (params) {
          if (params && params.value) {
            return params.value;
          } else {
            return '--';
          }
        }, cellStyle: params => {
          if (params && params.value && params.value !== 'N/A' && params.value !== '0') {
            // mark police cells as red
            return { backgroundColor: 'lightyellow' };
          }
          return null;
        }
      },
      {
        headerName: 'MicroSP TPMS Inner (77-S198)', field: 'micro_sp_tpmsinner', sortable: false, filter: false, width: 180, hide: true,
        cellRenderer: function (params) {
          if (params && params.value) {
            return params.value;
          } else {
            return '--';
          }
        }, cellStyle: params => {
          if (params && params.value && params.value !== 'N/A' && params.value !== '0') {
            // mark police cells as red
            return { backgroundColor: 'lightyellow' };
          }
          return null;
        }
      }, {
        headerName: 'MicroSP TPMS Outer (77-S199)', field: 'micro_sp_tpmsouter', sortable: false, filter: false, width: 180, hide: true,
        cellRenderer: function (params) {
          if (params && params.value) {
            return params.value;
          } else {
            return '--';
          }
        }, cellStyle: params => {
          if (params && params.value && params.value !== 'N/A' && params.value !== '0') {
            // mark police cells as red
            return { backgroundColor: 'lightyellow' };
          }
          return null;
        }
      }, {
        headerName: 'MicroSP Air Tank (77-S202)', field: 'micro_sp_air_tank', sortable: false, filter: false, width: 100,
        cellRenderer: function (params) {
          if (params && params.value) {
            return params.value;
          } else {
            return '--';
          }
        }, cellStyle: params => {
          if (params && params.value && params.value !== 'N/A' && params.value !== '0') {
            // mark police cells as red
            return { backgroundColor: 'lightyellow' };
          }
          return null;
        }
      }, {
        headerName: 'MicroSP Wired Receiver (77-S206)', field: 'micro_sp_wired_receiver', sortable: false, filter: false, width: 100,
        cellRenderer: function (params) {
          if (params && params.value) {
            return params.value;
          } else {
            return '--';
          }
        }, cellStyle: params => {
          if (params && params.value && params.value !== 'N/A' && params.value !== '0') {
            // mark police cells as red
            return { backgroundColor: 'lightyellow' };
          }
          return null;
        }
      },
      {
        headerName: 'MicroSP ATIS Regulator (77-S203)', field: 'micro_sp_atisregulator', sortable: false, filter: false, width: 100,
        cellRenderer: function (params) {
          if (params && params.value) {
            return params.value;
          } else {
            return '--';
          }
        }, cellStyle: params => {
          if (params && params.value && params.value !== 'N/A' && params.value !== '0') {
            // mark police cells as red
            return { backgroundColor: 'lightyellow' };
          }
          return null;
        }
      }, {
        headerName: 'Air Tank (77-S137)', field: 'air_tank', sortable: false, filter: false, width: 180, hide: true,
        cellRenderer: function (params) {
          if (params && params.value) {
            return params.value;
          } else {
            return '--';
          }
        }, cellStyle: params => {
          if (params && params.value && params.value !== 'N/A' && params.value !== '0') {
            // mark police cells as red
            return { backgroundColor: 'lightyellow' };
          }
          return null;
        }
      }, {
        headerName: 'ABS Sensor (77-H101)', field: 'abs_sensor', sortable: false, filter: false, width: 180,
        cellRenderer: function (params) {
          if (params && params.value) {
            return params.value;
          } else {
            return '--';
          }
        }, cellStyle: params => {
          if (params && params.value && params.value !== 'N/A' && params.value !== '0') {
            // mark police cells as red
            return { backgroundColor: 'lightyellow' };
          }
          return null;
        }
      }, {
        headerName: 'ATIS Sensor (77-S120)', field: 'atis_sensor', sortable: false, filter: false, width: 180, hide: true,
        cellRenderer: function (params) {
          if (params && params.value) {
            return params.value;
          } else {
            return '--';
          }
        }, cellStyle: params => {
          if (params && params.value && params.value !== 'N/A' && params.value !== '0') {
            // mark police cells as red
            return { backgroundColor: 'lightyellow' };
          }
          return null;
        }
      },
      {
        headerName: 'Cargo Sensor (77-S102)', field: 'cargo_sensor', sortable: false, filter: false, width: 180, hide: false,
        cellRenderer: function (params) {
          if (params && params.value) {
            return params.value;
          } else {
            return '--';
          }
        }, cellStyle: params => {
          if (params && params.value && params.value !== 'N/A' && params.value !== '0') {
            // mark police cells as red
            return { backgroundColor: 'lightyellow' };
          }
          return null;
        }
      },
      {
        headerName: 'Door Sensor (77-S108)', field: 'door_sensor', sortable: false, filter: false, width: 180,
        cellRenderer: function (params) {
          if (params && params.value) {
            return params.value;
          } else {
            return '--';
          }
        }, cellStyle: params => {
          if (params && params.value && params.value !== 'N/A' && params.value !== '0') {
            // mark police cells as red
            return { backgroundColor: 'lightyellow' };
          }
          return null;
        }
      },
      {
        headerName: 'Lamp Check ABS (77-S191)', field: 'lamp_check_abs', sortable: false, filter: false, width: 180, hide: true,
        cellRenderer: function (params) {
          if (params && params.value) {
            return params.value;
          } else {
            return '--';
          }
        }, cellStyle: params => {
          if (params && params.value && params.value !== 'N/A' && params.value !== '0') {
            // mark police cells as red
            return { backgroundColor: 'lightyellow' };
          }
          return null;
        }
      }, {
        headerName: 'Lamp Check ATIS (77-S188)', field: 'lamp_check_atis', sortable: false, filter: false, width: 180,
        cellRenderer: function (params) {
          if (params && params.value) {
            return params.value;
          } else {
            return '--';
          }
        }, cellStyle: params => {
          if (params && params.value && params.value !== 'N/A' && params.value !== '0') {
            // mark police cells as red
            return { backgroundColor: 'lightyellow' };
          }
          return null;
        }
      }, {
        headerName: 'LiteSentry (77-S107)', field: 'light_sentry', sortable: false, filter: false, width: 180,
        cellRenderer: function (params) {
          if (params && params.value) {
            return params.value;
          } else {
            return '--';
          }
        }, cellStyle: params => {
          if (params && params.value && params.value !== 'N/A' && params.value !== '0') {
            // mark police cells as red
            return { backgroundColor: 'lightyellow' };
          }
          return null;
        }
      },
      {
        headerName: 'Regulator (77-S164)', field: 'regulator', sortable: false, filter: false, width: 180, hide: true,
        cellRenderer: function (params) {
          if (params && params.value) {
            return params.value;
          } else {
            return '--';
          }
        }, cellStyle: params => {
          if (params && params.value && params.value !== 'N/A' && params.value !== '0') {
            // mark police cells as red
            return { backgroundColor: 'lightyellow' };
          }
          return null;
        }
      },
      {
        headerName: 'TPMS (77-S500)', field: 'tpms', sortable: false, filter: false, width: 180, hide: true,
        cellRenderer: function (params) {
          if (params && params.value) {
            return params.value;
          } else {
            return '--';
          }
        }, cellStyle: params => {
          if (params && params.value && params.value !== 'N/A' && params.value !== '0') {
            // mark police cells as red
            return { backgroundColor: 'lightyellow' };
          }
          return null;
        }
      },
      {
        headerName: 'Wheel End (77-S119)', field: 'wheel_end', sortable: false, filter: false, width: 180, hide: true,
        cellRenderer: function (params) {
          if (params && params.value) {
            return params.value;
          } else {
            return '--';
          }
        }, cellStyle: params => {
          if (params && params.value && params.value !== 'N/A' && params.value !== '0') {
            // mark police cells as red
            return { backgroundColor: 'lightyellow' };
          }
          return null;
        }
      },
      {
        headerName: 'PCT Cargo Camera (77-S111)', field: 'pct_cargo_camera_g1', sortable: false, filter: false, width: 180,
        cellRenderer: function (params) {
          if (params && params.value) {
            return params.value;
          } else {
            return '--';
          }
        }, cellStyle: params => {
          if (params && params.value && params.value !== 'N/A' && params.value !== '0') {
            // mark police cells as red
            return { backgroundColor: 'lightyellow' };
          }
          return null;
        }
      },
      // {
      //   headerName: 'PCT Cargo Camera G2 (77-S225)', field: 'pct_cargo_camera_g2', sortable: false, filter: false,width: 180, hide: true,
      //   cellRenderer: function (params) {
      //     if (params && params.value)
      //       return params.value
      //     else
      //       return '--'
      //   }, cellStyle: params => {
      //     if (params && params.value && params.value !== 'N/A' && params.value !== '0') {
      //       //mark police cells as red
      //       return { backgroundColor: 'lightyellow' };
      //     }
      //     return null;
      //   }
      // },
      // {
      //   headerName: 'PCT Cargo Camera G3 (77-S226)', field: 'pct_cargo_camera_g3', sortable: false, filter: false,width: 180, hide: true,
      //   cellRenderer: function (params) {
      //     if (params && params.value)
      //       return params.value
      //     else
      //       return '--'
      //   }, cellStyle: params => {
      //     if (params && params.value && params.value !== 'N/A' && params.value !== '0') {
      //       //mark police cells as red
      //       return { backgroundColor: 'lightyellow' };
      //     }
      //     return null;
      //   }
      // },
      {
        headerName: 'Created By', field: 'created_by', hide: true, sortable: true, filter: 'agTextColumnFilter', floatingFilter: true, filterParams: containsFilterParams,

      },
      {
        headerName: 'Updated by', field: 'updated_by', hide: true, sortable: true, filter: 'agTextColumnFilter', floatingFilter: true, filterParams: containsFilterParams,

      }, {
        headerName: 'Created On', field: 'created_at', sortable: true, filter: 'agDateColumnFilter', filterParams: containsFilterParams, hide: false,
        minWidth: 200,
        cellRenderer: (data) => {
          if (data && data.value) {
            return moment(data.value).format('YYYY-MM-DD HH:mm');
          } else {
            return '';
          }
        }
      }, {
        headerName: 'Last Updated On', field: 'updated_at', sortable: true, filter: 'agDateColumnFilter', filterParams: containsFilterParams, minWidth: 200, hide: false,
        cellRenderer: (data) => {
          if (data && data.value) {
            return moment(data.value).format('YYYY-MM-DD HH:mm');
          } else {
            return '';
          }
        }
      },
      {
        headerName: 'Action', field: 'action', sortable: false, filter: 'agNumberColumnFilter', floatingFilter: true, floatingFilterComponent: 'customClearFloatingFilter',
        floatingFilterComponentParams: {
          suppressFilterButton: true
        },
        cellRenderer: 'btnCellRenderer', width: 140, minWidth: 140, pinned: 'right'
      }
    ];
    this.cacheOverflowSize = 1;
    this.maxConcurrentDatasourceRequests = 1;
    this.infiniteInitialRowCount = 1;
    this.gridOptions = {
      cacheBlockSize: this.pageLimit,
      paginationPageSize: this.pageLimit,
      rowModelType: 'infinite',
      onBodyScroll: (event: BodyScrollEvent) => {
        if (event.direction === 'horizontal') {
          this.autoSizeAll(false);
        }
      },
    };

    this.frameworkComponents = {
      btnCellRenderer: BtnCellRendererForDeviceProvisioningComponent,
      agDateInput: CustomDateComponent,
      agColumnHeader: CustomHeader
    };

    this.defaultColDef = {
      flex: 1,
      minWidth: 100,
      sortable: false,
      floatingFilter: true,
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

  onBtNormalCols() {
    this.gridApi.setColumnDefs([]);
    this.gridApi.setColumnDefs(this.columnDefs);
    this.onColumnVisible(this.gridApi);
  }


  ngOnInit() {
    this.userCompanyType = sessionStorage.getItem('type');
    this.isSuperAdmin = sessionStorage.getItem('IS_ROLE_SUPERADMIN');
    this.userCompanyType = sessionStorage.getItem('type');
    this.rolePCTUser = sessionStorage.getItem('IS_ROLE_PCT_USER');
    this.getAllcompanies();
    this.isRoleCustomerManager = sessionStorage.getItem('IS_ROLE_CUSTOMER_MANAGER');
    this.sortFlag = false;
  }


  getSelectedRowData() {
    return this.gridApi.getSelectedNodes().map(node => node.data);
  }

  reloadComponent() {
    this.gridApi.onFilterChanged();
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
      for (const iterator of this.gatewayList) {
        iterator.is_selected = true;
        if (!iterator.status || iterator.status.toLowerCase() !== 'pending') {
          this.toastr.error('The following records are part of an installation, and so they cannot be deleted.', 'Problem with Deletion', { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
          return;
        } else {
          this.gatewayListService.gatewayList.push(iterator);
        }
      }
      if (this.gatewayListService.gatewayList.length > 1) {
        $('#deleteGatewaysModal').modal('show');

      } else if (this.gatewayListService.gatewayList.length === 1) {
        $('#deleteGatewayModal').modal('show');
      }
    } else if (action === 'update') {
      for (const iterator of this.gatewayList) {
        iterator.is_selected = true;
        if (!iterator.gateway_type || iterator.gateway_type.toLowerCase() !== 'gateway') {
          this.toastr.error('The selected device(s) do not support sensors.', 'Device Does Not Support Sensors ', { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
          return;
        } else {
          if (iterator.status.toLowerCase() !== 'pending') {
            this.toastr.error('The selected device(s) are part of an in-progress or active installation, and cannot be modified.', 'Device Configuration Can’t Be Modified',
              { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
            return;
          } else {
            this.gatewayListService.gatewayList.push(iterator);
          }
        }
      }
      this.router.navigate(['update-gateways']);
    }

  }

  private rowSelection;
  onSelectionChanged(event) {
  }
  onRowSelected(event) {
    const gatewayList = this.getSelectedRowData();
    if (gatewayList.length !== 0 && gatewayList.length % this.pageLimit === 0) {
      this.isChecked = true;
      this.selectAll = true;
    } else {
      this.isChecked = false;
      this.selectAll = false;
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
      for (const iterator of this.gatewayList) {
        iterator.is_selected = true;
      }
    } else if (!event.checked) {
      this.selectedIdsToDelete = [];
      this.gatewayList = this.getSelectedRowData();
      for (const iterator of this.gatewayList) {
        iterator.is_selected = false;
      }
    }
  }

  refresh() {
    this.gridApi.onFilterChanged();
  }
  onGridReady(params) {
    this.paramList = params;
    this.gridApi = params.api;
    this.gridApi.setRowData([]);
    this.gridColumnApi = params.columnApi;
    const columState = JSON.parse(sessionStorage.getItem('device_provisioning_list_grid_column_state'));
    if (columState) {
      // setTimeout(() => {
      this.gridColumnApi.applyColumnState({ state: columState, applyOrder: true, });
      // }, 300);
    }
    const filterSavedState = sessionStorage.getItem('device_provisioning_list_grid_filter_state' + this.gatewayListService.organisationId);
    if (filterSavedState) {
      this.gridApi.setFilterModel(JSON.parse(filterSavedState));
    }

    const datasource = {
      getRows: (params: IGetRowsParams) => {
        // this.isChecked = false;
        this.sortForAgGrid(params.sortModel);
        this.filterForAgGrid(params.filterModel);
        if (this.isFirst) {
          this.loading = true;
          this.gatewayListService.getGatewayAndSensorListSortwithCompanyPage(this.currentPage, this.column, this.order, this.pageLimit, this.organisationId ? this.organisationId : '',
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


  autoSizeAll(skipHeader: boolean) {
    const allColumnIds: string[] = [];
    this.gridColumnApi.getAllColumns().forEach((column) => {
      allColumnIds.push(column.getId());
    });
    this.gridColumnApi.autoSizeColumns(allColumnIds, skipHeader);
  }

  onDragStopped(params: any) {
    this.saveGridStateInSession(this.gridColumnApi.getColumnState());
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
    sessionStorage.setItem('device_provisioning_list_grid_filter_state' + this.gatewayListService.organisationId, JSON.stringify(params.api.getFilterModel()));
  }

  onColumnVisible(params: any) {
    this.saveGridStateInSession(this.gridColumnApi.getColumnState());
  }

  onColumnPinned(params: any) {
    this.saveGridStateInSession(this.gridColumnApi.getColumnState());
  }

  exportDeviceList() {
    // this.loading = true;
    // this.gatewayListService.exportFilterDeviceList(this.totalNumberOfRecords, 1, this.column, this.order, this.filterValuesJsonObj).subscribe(data => {
    //   this.loading = false;
    // }, error => {
    //   this.loading = false;
    // })

    this.gridOptions.api.exportDataAsCsv();

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
    if (this.gatewayListService.gatewayList.length < 1) {
      this.toastr.error('Please select at least one device', 'No Device Selected', { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
      return;
    }
    this.loading = true;
    this.resetInstalationStatusservice.resetInstalationStatus(null, this.gatewayListService.gatewayList[0].imei, this.gatewayListService.gatewayList[0].account_number).subscribe(data => {
      if (data.status) {
        this.gatewayListService.gatewayList = [];
        this.toastr.success(data.message, null, { toastComponent: CustomSuccessToastrComponent });
        this.reloadComponent();
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

  onFirstDataRendered(params) {
    this.isFirstTimeLoad = true;
  }

  saveGridStateInSession(columnState) {
    sessionStorage.setItem('device_provisioning_list_grid_column_state', JSON.stringify(columnState));
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
    if (filterModel !== undefined) {
      this.filterValuesJsonObj = {};
      this.filterValues = new Map();
      if (filterModel.order_no && filterModel.order_no.filter && filterModel.order_no.filter !== '') {
        this.filterValues.set('son', filterModel.order_no.filter);
      }
      if (filterModel.company_name && filterModel.company_name.filter && filterModel.company_name.filter !== '') {
        this.filterValues.set('companyName', filterModel.company_name.filter);
      }

      if (filterModel.imei && filterModel.imei.filter && filterModel.imei.filter !== '') {
        this.filterValues.set('imei', filterModel.imei.filter);
      }

      if (filterModel.created_by && filterModel.created_by.filter && filterModel.created_by.filter !== '') {
        this.filterValues.set('createdBy', filterModel.created_by.filter);
      }

      if (filterModel.updated_by && filterModel.updated_by.filter && filterModel.updated_by.filter !== '') {
        this.filterValues.set('updatedBy', filterModel.updated_by.filter);
      }

      if (filterModel.product_name && filterModel.product_name.values) {
        if (filterModel.product_name.values.length === 0) {
          this.filterValues.set('productName', 'null');
        } else {
          this.filterValues.set('productName', filterModel.product_name.values.toString());
        }
      }
      if (filterModel.status && filterModel.status.values) {
        if (filterModel.status.values.length === 0) {
          this.filterValues.set('status', 'null');
        } else {
          this.filterValues.set('status', filterModel.status.values.toString());
        }
      }

      if (filterModel.gateway_type && filterModel.gateway_type.values) {
        if (filterModel.gateway_type.values.length === 0) {
          this.filterValues.set('gateway_type', 'null');
        } else {
          this.filterValues.set('gateway_type', filterModel.status.values.toString());
        }
      }

      if (filterModel.created_at && filterModel.created_at.dateFrom) {
        if (filterModel.created_at.dateFrom.length === 0) {
          this.filterValues.set('created_at', 'null');
        } else {
          this.filterValues.set('created_at', filterModel.created_at.dateFrom.toString());
        }
      }

      if (filterModel.updated_at && filterModel.updated_at.dateFrom) {
        if (filterModel.updated_at.dateFrom.length === 0) {
          this.filterValues.set('updated_at', 'null');
        } else {
          this.filterValues.set('updated_at', filterModel.updated_at.dateFrom.toString());
        }
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
      case 'order_no': {
        this.column = 'son';
        this.order = sortModel[0].sort;
        break;
      }
      case 'company_name': {
        this.column = 'organisation.organisationName';
        this.order = sortModel[0].sort;
        break;
      }

      case 'status': {
        this.column = 'status';
        this.order = sortModel[0].sort;
        break;
      }

      case 'created_at': {
        this.column = 'createdAt';
        this.order = sortModel[0].sort;
        break;
      }
      case 'updated_at': {
        this.column = 'updatedAt';
        this.order = sortModel[0].sort;
        break;
      }
      default: {
      }
    }
  }

  onCellClicked(params) {
    if (params.column.colId === 'imei') {
      if (!params.data.gateway_type || params.data.gateway_type.toLowerCase() !== 'gateway') {
        // this.toastr.error('The selected device(s) do not support sensors.', 'Device Does Not Support Sensors ', { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
        return;
      } else {
        if (params.data.status.toLowerCase() !== 'pending') {
          this.toastr.error('The selected device(s) are part of an in-progress or active installation, and cannot be modified.', 'Device Configuration Can’t Be Modified',
            { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
          return;
        } else {
          this.gatewayListService.gatewayList = [];
          this.gatewayListService.gatewayList.push(params.data);
          sessionStorage.setItem('GATEWAY_LIST_DATA', JSON.stringify(this.gatewayListService.gatewayList));
        }
      }
      this.router.navigate(['update-gateways']);
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
        this.column = 'imei';
        this.order = 'asc';
        params.api.setFilterModel(null);
        params.api.onFilterChanged();
        sessionStorage.setItem('custom_device_provisioning_list_grid_filter_state', null);
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


  getAllcompanies() {
    this.gatewayListService.getAllCustomer(['EndCustomer']).subscribe(
      data => {
        this.companies = [];
        if (data !== undefined && data != null && data.length > 0) {
          for (const iterator of data) {
            this.companies.push(iterator);
          }
        }
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
    // this.onGridReady(this.paramList);
  }

  onDateRangeSelect(event) {
    this.days = event.value;
    sessionStorage.setItem('selectedDays', JSON.stringify(this.days));
    // this.onGridReady(this.paramList);
  }

  headerHeightSetter(event) {
    this.gridApi.setHeaderHeight(headerHeightGetter() + 20);
    this.gridApi.resetRowHeights();
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
        'Arrow-c',
        'Freight-l',
        'Cutlass-l',
        'Dagger67-lg',
        'Katana-h',
      'Talon-lte']);
    }, 2000);
  },
};

const filterParams = {
  values(params) {
    setTimeout(function () {
      params.success(['Pending', 'Install in progress', 'Installed', 'Active', 'Inactive', 'Deleted', 'Error', 'Problem']);
    }, 2000);
  },
};

const filterParamsForType = {
  values(params) {
    setTimeout(function () {
      params.success(['Gateway']);
    }, 2000);
  },
};

const filterParamsUsageStatus = {
  values(params) {
    setTimeout(function () {
      params.success(['RMA', 'Production', 'Engineering', 'Pilot']);
    }, 2000);
  },
};

const filterParamsQaStatus = {
  values(params) {
    setTimeout(function () {
      params.success(['Passed', 'Fail', 'Success']);
    }, 2000);
  },
};
