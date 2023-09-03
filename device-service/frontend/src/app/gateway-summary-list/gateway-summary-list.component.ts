import { AgGridAngular } from '@ag-grid-community/angular';
import { ClientSideRowModelModule, ColumnsToolPanelModule, GridOptions, IGetRowsParams, MenuModule, Module, MultiFilterModule } from '@ag-grid-enterprise/all-modules';
import { Component, OnInit, ViewChild } from '@angular/core';
import { Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { AssetsService } from '../assets.service';
import { GatewayListService } from '../gateway-list.service';
import { CustomDateComponent } from '../ui-elements/custom-date-component.component';
import { CustomHeader } from '../ui-elements/custom-header.component';
import { PagerService } from '../util/pagerService';
import { GatewayBtnCellRendererComponent } from './gateway-btn-cell-renderer.component';
declare var $: any;
@Component({
  selector: 'app-gateway-summary-list',
  templateUrl: './gateway-summary-list.component.html',
  styleUrls: ['./gateway-summary-list.component.css']
})
export class GatewaySummaryListComponent implements OnInit {

  sortFlag;
  currentPage = 1;
  isFirst = true;
  showingpage;
  totalpage;
  assetList: any[] = [];
  totalRecords = 0;
  currentEndRecord = 0;
  currentStartRecord = 0;
  pagedItems: any[];
  private allItems: any;
  pageLimit = 50;
  pager: any = {};
  sortByOfAssets = false;
  sortByCompany = false;
  sortCreatedOn = false;
  sortByCreatedBy = false;
  sortByLastUpdatedBy = false;
  sortByLastUpdateOn = false;
  column = 'organisationName';
  order = 'asc';
  userCompanyType;
  isSuperAdmin;
  isRoleCustomerManager;
  // parameters for counting records
  filterValues = new Map();
  filterValuesJsonObj: any = {};
  isClearButtonVisible = false;
  loading = false;
  frameworkComponents;
  range = 500;
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
    ColumnsToolPanelModule,
    MultiFilterModule,
  ];
  rowModelType = 'infinite';

  // Pagination Save State
  isPaginationStateRecovered = false;
  filterModelCountFilter: any;
  userNameList: string[];

  constructor(
    public assetsService: AssetsService, public router: Router,
    private toastr: ToastrService, private pagerService: PagerService, public gatewayListService: GatewayListService) {

    const containsFilterParams = {
      filterOptions: [
        'contains',
      ],
      suppressAndOrCondition: true
    };

    this.gatewayListService.organisationName = null;
    this.gatewayListService.filterNameForGatewaySummary = null;

    const filterParamsUsers = {
      values: params => params.success(this.getFilterValuesData())
    };

    const filterParams1 = {
      filterOptions: [
        'equals'
      ],
      suppressAndOrCondition: true,
    };

    this.columnDefs = [
      {
        headerName: 'Company', field: 'organisation_name', sortable: true, filter: 'agTextColumnFilter', minWidth: 200, filterParams: containsFilterParams,
        cellRenderer: function (params: any) {
          if (params.data) {
            return '<div class="pointer"><a style="color:#4ba9d7"><b>' + params.data.organisation_name + '</b></a></div>';
          }
        }
      },
      {
        headerName: 'Smart7 77-6800',
        field: 'smart_seven', sortable: true, filter: 'agNumberColumnFilter', filterParams: filterParams1,
        minWidth: 180,
        cellRenderer: function (params: any) {
          if (params.data) {
            if (params.data.smart_seven > 0) {
              //return '<div class="pointer"><a style="color:#4ba9d7"><b>' + params.data.smart_seven + '</b></a></div>';
              return params.data.smart_seven;
            } else {
              return 0;
            }
          }
        }
      },
      {
        headerName: 'TraillerNet 77-6740',
        field: 'trailler_net', sortable: true, filter: 'agNumberColumnFilter', filterParams: filterParams1,
        minWidth: 180,
        cellRenderer: function (params: any) {
          if (params.data) {
            if (params.data.trailler_net > 0) {
              //return '<div class="pointer"><a style="color:#4ba9d7"><b>' + params.data.trailler_net + '</b></a></div>';
              return params.data.trailler_net;
            } else {
              return 0;
            }
          }
        }
      },
      {
        headerName: 'SmartPair 77-S106',
        field: 'smart_pair', sortable: true, filter: 'agNumberColumnFilter', filterParams: filterParams1,
        minWidth: 180,
        cellRenderer: function (params: any) {
          if (params.data) {
            if (params.data.smart_pair > 0) {
              //return '<div class="pointer"><a style="color:#4ba9d7"><b>' + params.data.smart_pair + '</b></a></div>';
              return params.data.smart_pair;
            } else {
              return 0;
            }
          }
        }
      },
      {
        headerName: 'StealthNet 77-6740',
        field: 'stealth_net', sortable: true, filter: 'agNumberColumnFilter', filterParams: filterParams1,
        minWidth: 180,
        cellRenderer: function (params: any) {
          if (params.data) {
            if (params.data.stealth_net > 0) {
              //return '<div class="pointer"><a style="color:#4ba9d7"><b>' + params.data.stealth_net + '</b></a></div>';
              return params.data.stealth_net;
            } else {
              return 0;
            }
          }
        }
      },
      {
        headerName: 'Sabre 77-6230',
        field: 'sabre', sortable: true, filter: 'agNumberColumnFilter', filterParams: filterParams1,
        minWidth: 180,
        cellRenderer: function (params: any) {
          if (params.data) {
            if (params.data.sabre > 0) {
              //return '<div class="pointer"><a style="color:#4ba9d7"><b>' + params.data.sabre + '</b></a></div>';
              return params.data.sabre;
            } else {
              return 0;
            }
          }
        }
      },
      {
        headerName: 'FreightLa 77-6800',
        field: 'freight_la', sortable: true, filter: 'agNumberColumnFilter', filterParams: filterParams1,
        minWidth: 180,
        cellRenderer: function (params: any) {
          if (params.data) {
            if (params.data.freight_la > 0) {
              //eturn '<div class="pointer"><a style="color:#4ba9d7"><b>' + params.data.freight_la + '</b></a></div>';
              return params.data.freight_la;
            } else {
              return 0;
            }
          }
        }
      },
      {
        headerName: 'arrowl 77-6280',
        field: 'arrow_l', sortable: true, filter: 'agNumberColumnFilter', filterParams: filterParams1,
        minWidth: 180,
        cellRenderer: function (params: any) {
          if (params.data) {
            if (params.data.arrow_l > 0) {
              //return '<div class="pointer"><a style="color:#4ba9d7"><b>' + params.data.arrow_l + '</b></a></div>';
              return params.data.arrow_l;
            } else {
              return 0;
            }
          }
        }
      },
      {
        headerName: 'FreightL 77-6700',
        field: 'freight_l', sortable: true, filter: 'agNumberColumnFilter', filterParams: filterParams1,
        minWidth: 180,
        cellRenderer: function (params: any) {
          if (params.data) {
            if (params.data.freight_l > 0) {
              //return '<div class="pointer"><a style="color:#4ba9d7"><b>' + params.data.freight_l + '</b></a></div>';
              return params.data.freight_l;
            } else {
              return 0;
            }
          }
        }
      },
      {
        headerName: 'cutlassL 77-6290',
        field: 'cutlass_l', sortable: true, filter: 'agNumberColumnFilter', filterParams: filterParams1,
        minWidth: 180,
        cellRenderer: function (params: any) {
          if (params.data) {
            if (params.data.cutlass_l > 0) {
              //return '<div class="pointer"><a style="color:#4ba9d7"><b>' + params.data.cutlass_l + '</b></a></div>';
              return params.data.cutlass_l;
            } else {
              return 0;
            }
          }
        }
      },
      {
        headerName: 'dagger67Lg 77-6400',
        field: 'dagger67_lg', sortable: true, filter: 'agNumberColumnFilter', filterParams: filterParams1,
        minWidth: 180,
        cellRenderer: function (params: any) {
          if (params.data) {
            if (params.data.dagger67_lg > 0) {
              //return '<div class="pointer"><a style="color:#4ba9d7"><b>' + params.data.dagger67_lg + '</b></a></div>';
              return params.data.dagger67_lg;
            } else {
              return 0;
            }
          }
        }
      },
      {
        headerName: 'Smart7 77-6800',
        field: 'smart7', sortable: true, filter: 'agNumberColumnFilter', filterParams: filterParams1,
        minWidth: 180,
        cellRenderer: function (params: any) {
          if (params.data) {
            if (params.data.smart7 > 0) {
              //return '<div class="pointer"><a style="color:#4ba9d7"><b>' + params.data.smart7 + '</b></a></div>';
              return params.data.smart7;
            } else {
              return 0;
            }
          }
        }
      },
      {
        headerName: 'katanaH 77-6600',
        field: 'katana_h', sortable: true, filter: 'agNumberColumnFilter', filterParams: filterParams1,
        minWidth: 180,
        cellRenderer: function (params: any) {
          if (params.data) {
            if (params.data.katana_h > 0) {
              //return '<div class="pointer"><a style="color:#4ba9d7"><b>' + params.data.katana_h + '</b></a></div>';
              return params.data.katana_h;
            } else {
              return 0;
            }
          }
        }
      },
      // { headerName: 'Action', field: 'action', sortable: true, filter: 'agTextColumnFilter', cellRenderer: 'btnCellRenderer', width: 100, minWidth: 120, pinned: 'right' }
      // ,
      {
        headerName: 'Action', field: 'action', sortable: false, filter: 'agNumberColumnFilter', floatingFilter: true, floatingFilterComponent: 'customClearFloatingFilter',
        floatingFilterComponentParams: {
          suppressFilterButton: true
        },
        cellRenderer: 'btnCellRenderer', width: 140, minWidth: 140, pinned: 'right'
      }
    ];
    this.cacheOverflowSize = 2;
    this.maxConcurrentDatasourceRequests = 1;
    this.infiniteInitialRowCount = 1;
    this.gridOptions = {
      cacheBlockSize: this.pageLimit,
      paginationPageSize: 25,
      rowModelType: 'infinite',
      animateRows: true,
      rowData: null,
      unSortIcon: true,
      sortingOrder: ['desc', 'asc', null],
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
    this.frameworkComponents = {
      btnCellRenderer: GatewayBtnCellRendererComponent,
      agDateInput: CustomDateComponent,
      // agColumnHeader: CustomHeader
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

    this.sortFlag = false;
    this.userCompanyType = sessionStorage.getItem('type');
    this.isSuperAdmin = sessionStorage.getItem('IS_ROLE_SUPERADMIN');
    this.isRoleCustomerManager = sessionStorage.getItem('IS_ROLE_CUSTOMER_MANAGER');
  }

  openExportPopup() {
    $('#exportCSVModal').modal('show');
  }


  exportDeviceList() {
    this.loading = true;
    this.gatewayListService.exportGatewayDetails(1, this.column, this.order, this.range, this.filterValuesJsonObj, this.filterModelCountFilter).subscribe(data => {
      this.loading = false;
    }, error => {
      this.loading = false;
    });

    // this.gridOptions.api.exportDataAsCsv();

  }

  onGridReady(params: any) {
    // this.gridOptions.api.setDomLayout('autoHeight');
    this.paramList = params;
    this.gridApi = params.api;
    this.gridApi.setRowData([]);
    this.gridColumnApi = params.columnApi;
    const gridSavedState = sessionStorage.getItem('gateway_summery_list_grid_column_state');
    if (gridSavedState) {
      this.gridColumnApi.setColumnState(JSON.parse(gridSavedState));
    }
    const filterSavedState = sessionStorage.getItem('gateway_summery_list_grid_filter_state');
    if (filterSavedState) {
      this.gridApi.setFilterModel(JSON.parse(filterSavedState));
    }

    const datasource = {
      getRows: (params: IGetRowsParams) => {
        this.loading = true;
        this.sortForAgGrid(params.sortModel);
        this.filterForAgGrid(params.filterModel);
        if (this.isFirst) {
          this.gatewayListService.getAllGatewayDetails(this.currentPage, this.column, this.order, this.pageLimit, this.filterValuesJsonObj, this.filterModelCountFilter).subscribe(data => {
            this.rowData = data.body.content;
            this.loading = false;
            this.totalRecords = data.total_key;
            if (this.totalRecords <= (this.currentPage * this.pageLimit)) {
              this.isFirst = false;
            }
            this.currentPage++;
            params.successCallback(this.rowData, this.totalRecords);
          }, error => {
            console.log(error);
          });
        }
      }
    };
    this.gridApi.setDatasource(datasource);
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

  onColumnPinned(params: any) {
    this.saveGridStateInSession(this.gridColumnApi.getColumnState());
  }

  onFilterChanged(params: any) {
    this.currentPage = 1;
    this.isFirst = true;
    sessionStorage.setItem('gateway_summery_list_grid_filter_state', JSON.stringify(params.api.getFilterModel()));
  }

  onColumnVisible(params: any) {
    this.saveGridStateInSession(this.gridColumnApi.getColumnState());
  }


  saveGridStateInSession(columnState: any) {
    sessionStorage.setItem('gateway_summery_list_grid_column_state', JSON.stringify(columnState));
  }

  addGateway() {
    this.assetsService.routeflag = false;
  }
  uploadGateway() {
    this.router.navigate(['upload-devices']);
  }

  viewComapnyAssets(asset) {
    // this.router.navigate(['device-provisioning'], { queryParams: { id: asset.organisation_id } });
    this.gatewayListService.organisationName = asset.organisation_name;
    this.router.navigate(['gateway-list']);
  }

  filterForAgGrid(filterModel) {
    this.gridApi.paginationCurrentPage = 0;
    if (filterModel) {

      this.filterValuesJsonObj = {};
      this.filterValues = new Map();
      if (filterModel.organisation_name && filterModel.organisation_name.filter && filterModel.organisation_name.filter !== '') {
        this.filterValues.set('organisationName', filterModel.organisation_name.filter);
      }

      if (filterModel.smart_seven !== undefined && filterModel.smart_seven.filter != null && filterModel.smart_seven.filter !== undefined && filterModel.smart_seven.filter !== '') {
        this.filterValues.set('smartSeven', filterModel.smart_seven.filter);
      }

      if (filterModel.trailler_net !== undefined && filterModel.trailler_net.filter != null && filterModel.trailler_net.filter !== undefined && filterModel.trailler_net.filter !== '') {
        this.filterValues.set('traillerNet', filterModel.trailler_net.filter);
      }

      if (filterModel.smart_pair !== undefined && filterModel.smart_pair.filter != null && filterModel.smart_pair.filter !== undefined && filterModel.smart_pair.filter !== '') {
        this.filterValues.set('smartPair', filterModel.smart_pair.filter);
      }

      if (filterModel.stealth_net !== undefined && filterModel.stealth_net.filter != null && filterModel.stealth_net.filter !== undefined && filterModel.stealth_net.filter !== '') {
        this.filterValues.set('stealthNet', filterModel.stealth_net.filter);
      }

      if (filterModel.sabre !== undefined && filterModel.sabre.filter != null && filterModel.sabre.filter !== undefined && filterModel.sabre.filter !== '') {
        this.filterValues.set('sabre', filterModel.sabre.filter);
      }

      if (filterModel.freight_la !== undefined && filterModel.freight_la.filter != null && filterModel.freight_la.filter !== undefined && filterModel.freight_la.filter !== '') {
        this.filterValues.set('freightLa', filterModel.freight_la.filter);
      }

      if (filterModel.arrow_l !== undefined && filterModel.arrow_l.filter != null && filterModel.arrow_l.filter !== undefined && filterModel.arrow_l.filter !== '') {
        this.filterValues.set('arrowL', filterModel.arrow_l.filter);
      }

      if (filterModel.freight_l !== undefined && filterModel.freight_l.filter != null && filterModel.freight_l.filter !== undefined && filterModel.freight_l.filter !== '') {
        this.filterValues.set('freightL', filterModel.freight_l.filter);
      }

      if (filterModel.cutlass_l !== undefined && filterModel.cutlass_l.filter != null && filterModel.cutlass_l.filter !== undefined && filterModel.cutlass_l.filter !== '') {
        this.filterValues.set('cutlassL', filterModel.cutlass_l.filter);
      }

      if (filterModel.dagger67_lg !== undefined && filterModel.dagger67_lg.filter != null && filterModel.dagger67_lg.filter !== undefined && filterModel.dagger67_lg.filter !== '') {
        this.filterValues.set('dagger67Lg', filterModel.dagger67_lg.filter);
      }

      if (filterModel.smart7 !== undefined && filterModel.smart7.filter != null && filterModel.smart7.filter !== undefined && filterModel.smart7.filter !== '') {
        this.filterValues.set('smart7', filterModel.smart7.filter);
      }

      if (filterModel.katana_h !== undefined && filterModel.katana_h.filter != null && filterModel.katana_h.filter !== undefined && filterModel.katana_h.filter !== '') {
        this.filterValues.set('katanaH', filterModel.katana_h.filter);
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
    if (sortModel[0] === undefined || sortModel[0] === null || sortModel[0].colId === undefined || sortModel[0].colId === null) {
      return;
    }
    this.sortFlag = true;
    switch (sortModel[0].colId) {
      case 'organisation_name': {

        this.column = 'organisationName';
        this.order = sortModel[0].sort;
        break;
      }
      case 'count': {

        this.column = 'count';
        this.order = sortModel[0].sort;
        break;
      }
      case 'smart_seven': {

        this.column = 'smartSeven';
        this.order = sortModel[0].sort;
        break;
      }
      case 'trailler_net': {

        this.column = 'traillerNet';
        this.order = sortModel[0].sort;
        break;
      }
      case 'smart_pair': {

        this.column = 'smartPair';
        this.order = sortModel[0].sort;
        break;
      }
      case 'stealth_net': {

        this.column = 'stealthNet';
        this.order = sortModel[0].sort;
        break;
      }
      case 'sabre': {

        this.column = 'sabre';
        this.order = sortModel[0].sort;
        break;
      }
      case 'freight_la': {

        this.column = 'freightLa';
        this.order = sortModel[0].sort;
        break;
      }
      case 'arrow_l': {

        this.column = 'arrowL';
        this.order = sortModel[0].sort;
        break;
      }
      case 'freight_l': {

        this.column = 'freightL';
        this.order = sortModel[0].sort;
        break;
      }
      case 'cutlass_l': {

        this.column = 'cutlassL';
        this.order = sortModel[0].sort;
        break;
      }
      case 'dagger67_lg': {

        this.column = 'dagger67Lg';
        this.order = sortModel[0].sort;
        break;
      }
      case 'smart7': {

        this.column = 'smart7';
        this.order = sortModel[0].sort;
        break;
      }
      case 'katana_h': {

        this.column = 'katanaH';
        this.order = sortModel[0].sort;
        break;
      }
      default: {
        console.log('default');
      }
    }
  }

  onCellClicked(params: any) {
    if (params.column.colId === 'organisation_name') {
      this.assetsService.assetCompanyId = params.data.company_id;
      this.gatewayListService.filterNameForGatewaySummary = 'none';
      this.viewComapnyAssets(params.data);
    }
    // else if (params.column.colId === 'smart_seven' && params.data.smart_seven > 0) {
    //   this.gatewayListService.filterNameForGatewaySummary = 'Smart7';
    //   this.assetsService.assetCompanyId = params.data.company_id;
    //   this.viewComapnyAssets(params.data);
    // } else if (params.column.colId === 'trailler_net' && params.data.trailler_net > 0) {
    //   this.gatewayListService.filterNameForGatewaySummary = 'TraillerNet';
    //   this.assetsService.assetCompanyId = params.data.company_id;
    //   this.viewComapnyAssets(params.data);
    // } else if (params.column.colId === 'cutlass_l' && params.data.cutlass_l > 0) {
    //   this.gatewayListService.filterNameForGatewaySummary = 'Cutlass-l';
    //   this.assetsService.assetCompanyId = params.data.company_id;
    //   this.viewComapnyAssets(params.data);
    // } else if (params.column.colId === 'smart_pair' && params.data.smart_pair > 0) {
    //   this.gatewayListService.filterNameForGatewaySummary = 'SmartPair';
    //   this.assetsService.assetCompanyId = params.data.company_id;
    //   this.viewComapnyAssets(params.data);
    // } else if (params.column.colId === 'stealth_net' && params.data.stealth_net > 0) {
    //   this.gatewayListService.filterNameForGatewaySummary = 'StealthNet';
    //   this.assetsService.assetCompanyId = params.data.company_id;
    //   this.viewComapnyAssets(params.data);
    // } else if (params.column.colId === 'sabre' && params.data.sabre > 0) {
    //   this.gatewayListService.filterNameForGatewaySummary = 'Sabre';
    //   this.assetsService.assetCompanyId = params.data.company_id;
    //   this.viewComapnyAssets(params.data);
    // } else if (params.column.colId === 'arrow_l' && params.data.arrow_l > 0) {
    //   this.gatewayListService.filterNameForGatewaySummary = 'Arrow-l';
    //   this.assetsService.assetCompanyId = params.data.company_id;
    //   this.viewComapnyAssets(params.data);
    // } else if (params.column.colId === 'freight_la' && params.data.freight_la > 0) {
    //   this.gatewayListService.filterNameForGatewaySummary = 'Freight-la';
    //   this.assetsService.assetCompanyId = params.data.company_id;
    //   this.viewComapnyAssets(params.data);
    // } else if (params.column.colId === 'freight_l' && params.data.freight_l > 0) {
    //   this.gatewayListService.filterNameForGatewaySummary = 'Freight-I';
    //   this.assetsService.assetCompanyId = params.data.company_id;
    //   this.viewComapnyAssets(params.data);
    // } else if (params.column.colId === 'cutlass_l' && params.data.cutlass_l > 0) {
    //   this.gatewayListService.filterNameForGatewaySummary = 'Cutlass-l';
    //   this.assetsService.assetCompanyId = params.data.company_id;
    //   this.viewComapnyAssets(params.data);
    // } else if (params.column.colId === 'dagger67_lg' && params.data.dagger67_lg > 0) {
    //   this.gatewayListService.filterNameForGatewaySummary = 'Dagger67-lg';
    //   this.assetsService.assetCompanyId = params.data.company_id;
    //   this.viewComapnyAssets(params.data);
    // } else if (params.column.colId === 'smart7' && params.data.smart7 > 0) {
    //   this.gatewayListService.filterNameForGatewaySummary = 'Smart7';
    //   this.assetsService.assetCompanyId = params.data.company_id;
    //   this.viewComapnyAssets(params.data);
    // } else if (params.column.colId === 'katana_h' && params.data.katana_h > 0) {
    //   this.gatewayListService.filterNameForGatewaySummary = 'katana-h';
    //   this.assetsService.assetCompanyId = params.data.company_id;
    //   this.viewComapnyAssets(params.data);
    // }
  }

  getClearFilterFloatingComponent() {
    function ClearFloatingFilter() { }
    ClearFloatingFilter.prototype.init = function (params: any) {
      this.eGui = document.createElement('div');
      this.eGui.innerHTML =
        '<button class="btnClear primary_buttonClear" (click)="clearFilter()">Clear Filters</button>';
      this.currentValue = null;
      this.clearFilterButton = this.eGui.querySelector('button');
      const that = this;
      function onClearFilterButtonClicked() {
        this.column = 'organisationName';
        this.order = 'asc';
        params.api.setFilterModel(null);
        params.api.onFilterChanged();
        sessionStorage.setItem('gateway_summery_list_grid_filter_state', null);
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
    return this.userNameList;
  }

  onBtNormalCols() {
    this.gridApi.onFilterChanged();
  }

}
