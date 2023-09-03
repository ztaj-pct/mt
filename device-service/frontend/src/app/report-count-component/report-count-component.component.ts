import { AgGridAngular } from '@ag-grid-community/angular';
import { GridOptions, Module, ClientSideRowModelModule, MenuModule, MultiFilterModule, RowGroupingModule, IGetRowsParams } from '@ag-grid-enterprise/all-modules';
import { ColumnsToolPanelModule } from '@ag-grid-enterprise/column-tool-panel';
import { Component, OnInit, ViewChild } from '@angular/core';
import { Router } from '@angular/router';
import * as moment from 'moment';
import { ToastrService } from 'ngx-toastr';
import { ATCommandService } from '../atcommand.service';
import { GatewayListService } from '../gateway-list.service';
import { Filter } from '../models/Filter.model';
import { PagerService } from '../util/pagerService';
import { PaginationConstants } from '../util/paginationConstants';

@Component({
  selector: 'app-report-count-component',
  templateUrl: './report-count-component.component.html',
  styleUrls: ['./report-count-component.component.css']
})
export class ReportCountComponentComponent implements OnInit {



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
  column = 'ms1recordcount';
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
  isPaginationStateRecovered = false;
  filterModelCountFilter: any;
  userNameList: string[];
  companies: any[];
  company: any;
  companyUuid = '1';
  companyname: any;
  days = 0;
  selectedtoDateRange = [];
  selected: number;
  selectedIdsToDelete: string[] = [];
  isChecked = false;
  isFirst = true;
  dateIsReadOnly = true;
  customerList: any[] = [];
  isFilterClick = false;
  constructor(
    private readonly atcommandService: ATCommandService,
    private readonly gatewayListService: GatewayListService,
    private readonly router: Router,
    private readonly toastr: ToastrService,
    private readonly pagerService: PagerService) {

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
        headerName: 'Ms2 Organisation Name', field: 'ms2OrganisationName',
        sortingOrder: ['asc', 'desc', null], unSortIcon: true,
        filter: 'agTextColumnFilter', floatingFilter: true,
        filterParams: containsFilterParams,
        width: 200,
        cellRenderer: function (params) {
          if (params.data !== undefined && params.data.ms2OrganisationName != null) {
            return '<div class="pointer" style="color:#4ba9d7;"><a>' + params.data.ms2OrganisationName + '</a></div>';
          }
        }
      },
      {
        headerName: 'Ms1 Report Count', field: 'ms1recordcount', unSortIcon: true, filter: 'agTextColumnFilter', width: 180, floatingFilter: true, filterParams: containsFilterParams,
        cellRenderer: function (params) {
          if (params.data !== undefined && params.data.ms1recordcount != null) {
            return '<div>' + params.data.ms1recordcount + '</div>';
          }
        }
      },
      // {
      //   headerName: 'Ms1 Last Record Id', field: 'ms1lastRecordId', sortable: true, suppressSorting: true, filter: 'agTextColumnFilter', minWidth: 200,
      //   cellRenderer: function (params) {
      //     if (params.data !== undefined && params.data.ms1lastRecordId != null) {
      //       return '<div>' + params.data.ms1lastRecordId + '</div>';
      //     }
      //   }
      // },
      // {
      //   headerName: 'Ms1 Date Time', field: 'ms1dateTime', unSortIcon: true, filter: 'agDateColumnFilter',
      //   cellRenderer: function (params) {
      //     if (params.data !== undefined && params.data.ms1dateTime != null) {
      //       return '<div>' + moment(params.data.ms1dateTime).format('YYYY-MM-DD hh:mm:ss') + '</div>';
      //     }
      //   }
      // },
      // {
      //   headerName: 'Ms2 Record Count', field: 'ms2recordcount', unSortIcon: true, filter: 'agTextColumnFilter', width: 180, floatingFilter: true, filterParams: containsFilterParams,
      //   cellRenderer: function (params) {
      //     if (params.data !== undefined && params.data.ms2recordcount != null) {
      //       return '<div>' + params.data.ms2recordcount + '</div>';
      //     }
      //   }
      // },

      // {
      //   headerName: 'Ms2 Last Record Id', field: 'ms2lastRecordId', unSortIcon: true, filter: 'agTextColumnFilter', width: 180, floatingFilter: true,
      //   cellRenderer: function (params) {
      //     if (params.data !== undefined && params.data.ms2lastRecordId != null) {
      //       return '<div>' + params.data.ms2lastRecordId + '</div>';
      //     }
      //   }
      // },
      // {
      //   headerName: 'Ms2 Date', field: 'ms2dateTime',

      //   filter: 'agDateColumnFilter',
      //   floatingFilter: true, flex: 1,
      //   unSortIcon: true, minWidth: 180,
      //   sortingOrder: ['asc', 'desc', null],
      //   cellRenderer: function (params) {
      //     if (params.data !== undefined && params.data.ms2dateTime != null) {
      //       return '<div>' + moment(params.data.ms2dateTime).format('YYYY-MM-DD hh:mm:ss') + '</div>';
      //     }
      //   }
      // },

      {
        headerName: 'Ms2 Report Count', field: 'ms2Count', sortable: true, suppressSorting: true, filter: 'agTextColumnFilter', minWidth: 200,
        cellRenderer: function (params) {
          if (params.data !== undefined && params.data.ms1recordcount != null) {
            return '<div>' + params.data.ms1recordcount + '</div>';
          }
        }
      },


      // {
      //   headerName: 'Can', field: 'can', sortable: true, suppressSorting: true, filter: 'agTextColumnFilter', minWidth: 200,
      //   cellRenderer: function (params) {
      //     if (params.data !== undefined && params.data.can != null) {
      //       return '<div>' + params.data.can + '</div>';
      //     }
      //   }
      // },

      // {
      //   headerName: 'Diffrence', field: 'diff', sortable: true, suppressSorting: true, filter: 'agTextColumnFilter', minWidth: 200,
      //   cellRenderer: function (params) {
      //     if (params.data !== undefined && params.data.diff != null) {
      //       if (params.data.diff >= 0) {
      //         return '<div>' + params.data.diff + '</div>';
      //       } else {
      //         return '<div>' + 0 + '</div>';
      //       }
      //     }
      //   }
      // }
    ];
    this.cacheOverflowSize = 2;
    this.maxConcurrentDatasourceRequests = 2;
    this.infiniteInitialRowCount = 2;
    this.gridOptions = {
      cacheBlockSize: this.pageLimit,
      paginationPageSize: this.pageLimit,
      rowModelType: 'infinite',
      animateRows: true,
      rowData: null,
      sortingOrder: ['desc', 'asc', null],
    };

    // this.frameworkComponents = {
    //   btnCellRenderer: BtnCellRendererForGatewayDetails,
    //   agDateInput: CustomDateComponent,
    //   agColumnHeader: CustomHeader
    // };

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
    this.isSuperAdmin = sessionStorage.getItem('IS_ROLE_SUPERADMIN');
    this.isRoleCustomerManager = sessionStorage.getItem('IS_ROLE_CUSTOMER_MANAGER');
    this.sortFlag = false;
    const columState = JSON.parse(sessionStorage.getItem('latest_report_list_grid_column_state'));
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

  onGridReady(params) {
    this.paramList = params;
    this.gridApi = params.api;
    this.gridApi.setRowData([]);
    this.gridColumnApi = params.columnApi;
    const gridSavedState = sessionStorage.getItem('latest_report_list_grid_column_state' + this.gatewayListService.organisationId);
    if (gridSavedState) {
      this.gridColumnApi.setColumnState(JSON.parse(gridSavedState));
    }
    const filterSavedState = sessionStorage.getItem('latest_report_list_grid_filter_state' + this.gatewayListService.organisationId);
    if (filterSavedState) {
      this.gridApi.setFilterModel(JSON.parse(filterSavedState));
    }

    const datasource = {
      getRows: (params: IGetRowsParams) => {
        this.sortForAgGrid(params.sortModel);
        this.filterForAgGrid(params.filterModel);
        if (this.isFirst) {
          this.loading = true;
          this.gatewayListService.gatReportCountWithPage(this.currentPage, this.column, this.order, this.pageLimit,
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
    sessionStorage.setItem('latest_report_list_grid_filter_state', JSON.stringify(params.api.getFilterModel()));
  }

  onColumnVisible(params: any) {
    this.saveGridStateInSession(this.gridColumnApi.getColumnState());
  }

  onColumnPinned(params: any) {
    this.saveGridStateInSession(this.gridColumnApi.getColumnState());
  }


  saveGridStateInSession(columnState) {
    sessionStorage.setItem('latest_report_list_grid_column_state', JSON.stringify(columnState));
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
          this.elasticFilter.value = filterModel[Okeys[i]].values.toString();
          this.filterValues.set('filterValues ' + i + 'a', this.elasticFilter);
        }
        // this.filterValues.set('filterValues ' + i, this.elasticFilter)
        this.elasticFilter = new Filter();
        index = i + 1;
      }
      const jsonObject: any = {};
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
      case 'ms2OrganisationName': {
        this.column = 'ms2OrganisationName';
        this.order = sortModel[0].sort;
        break;
      }
      // case 'ms1dateTime': {
      //   this.column = 'ms1dateTime';
      //   this.order = sortModel[0].sort;
      //   break;
      // }
      // case 'ms1lastRecordId': {
      //   this.column = 'ms1lastRecordId';
      //   this.order = sortModel[0].sort;
      //   break;
      // }
      case 'ms1recordcount': {
        this.column = 'ms1recordcount';
        this.order = sortModel[0].sort;
        break;
      }

      // case 'ms2dateTime': {
      //   this.column = 'ms2dateTime';
      //   this.order = sortModel[0].sort;
      //   break;
      // }
      // case 'ms2lastRecordId': {
      //   this.column = 'ms2lastRecordId';
      //   this.order = sortModel[0].sort;
      //   break;
      // }

      case 'ms2Count': {
        this.column = 'ms2Count';
        this.order = sortModel[0].sort;
        break;
      }

      // case 'can': {
      //   this.column = 'can';
      //   this.order = sortModel[0].sort;
      //   break;
      // }
      // case 'diff': {
      //   this.column = 'diff';
      //   this.order = sortModel[0].sort;
      //   break;
      // }
      default: {
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
        this.column = 'ms2OrganisationName';
        this.order = 'asc';
        params.api.setFilterModel(null);
        params.api.onFilterChanged();
        sessionStorage.setItem('custom_latest_report_list_grid_filter_state', null);
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