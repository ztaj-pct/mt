import { AgGridAngular } from '@ag-grid-community/angular';
import { ClientSideRowModelModule, ColumnsToolPanelModule, GridOptions, IGetRowsParams, MenuModule, Module, MultiFilterModule, RowGroupingModule } from '@ag-grid-enterprise/all-modules';
import { Component, OnInit, ViewChild } from '@angular/core';
import { FormControl } from '@angular/forms';
import { Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { ATCommandService } from '../atcommand.service';
import { CustomErrorToastrComponent } from '../custom-error-toastr/custom-error-toastr.component';
import { CustomSuccessToastrComponent } from '../custom-success-toastr/custom-success-toastr.component';
import { CustomDateComponent } from '../ui-elements/custom-date-component.component';
import { CustomHeader } from '../ui-elements/custom-header.component';
import { PagerService } from '../util/pagerService';
import { PaginationConstants } from '../util/paginationConstants';
import { BtnCellRendererForATCommand } from './btn-cell-renderer.component';
import * as _ from 'underscore';
import moment from "moment-timezone";
import { interval } from 'rxjs';

@Component({
  selector: 'app-atcommand-list',
  templateUrl: './atcommand-list.component.html',
  styleUrls: ['./atcommand-list.component.css']
})
export class ATCommandListComponent implements OnInit {
  sortFlag;
  currentPage: any;
  showingpage;
  totalpage;
  gatewayList: any[] = [];
  totalRecords = 0;
  currentEndRecord = 0;
  currentStartRecord = 0;
  pagedItems: any[];
  private allItems: any;
  pageLimit = 20;
  total_number_of_records = 0;
  pager: any = {};
  sortCreatedOn = false;
  sortByCreatedBy = false;
  sortByLastUpdatedBy = false;
  sortByLastUpdateOn = false;
  pageSizeOptions: PaginationConstants = new PaginationConstants();
  pageSizeObjArr = this.pageSizeOptions.pageSizeObjArr;
  column = 'created_epoch';
  order = 'desc';
  userCompanyType;
  isSuperAdmin;
  isRoleCustomerManager;
  // parameters for counting records
  filterValues = new Map();
  filterValuesJsonObj: any = {};
  isClearButtonVisible = false;
  loading = false;
  frameworkComponents;
  updateSubscription: any;
  public sideBar;

  // aggrid code
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
  userNameList: String[];
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
  atcRequest: any;
  rolePCTUser: any;


  constructor(public atcommandService: ATCommandService, public router: Router, private toastr: ToastrService, private pagerService: PagerService) {
    this.columnDefs = createNormalColDefs();
    this.rolePCTUser = JSON.parse(sessionStorage.getItem('IS_ROLE_PCT_USER'));

    const filterParamsUsers = {
      values: params => params.success(this.getFilterValuesData())
    };

    this.cacheOverflowSize = 2;
    this.maxConcurrentDatasourceRequests = 2;
    this.infiniteInitialRowCount = 2;

    this.gridOptions = {
      cacheBlockSize: this.pageLimit,
      paginationPageSize: this.pageLimit,
      rowModelType: 'infinite',
    };
    this.frameworkComponents = {
      btnCellRenderer: BtnCellRendererForATCommand,
      agDateInput: CustomDateComponent,
      agColumnHeader: CustomHeader
    };

    this.defaultColDef = {
      flex: 1,
      minWidth: 100,
      sortable: true,
      floatingFilter: true,
      enablePivot: true,
      resizable: true,
      filter: true,
      wrapText: true,
      // autoHeight: true,
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
    this.columnDefs = createNormalColDefs();
    this.gridApi.setColumnDefs(this.columnDefs);
    this.onColumnVisible(this.gridApi);

  }

  ngOnInit() {
    this.atcommandService.isAddATCommand = false;
    this.atcommandService.isATCommandsummary = false;
    this.loading = true;
    // this.updateSubscription = interval(10000).subscribe(
    //   (val) => { this.refresh(); }
    // );
  }

  onPageSizeChange() {
    sessionStorage.setItem('gatewayListsize', String(this.selected));
    const value = this.selected;
    this.pageLimit = (Number(value));
    this.gridApi.gridOptionsWrapper.setProperty('cacheBlockSize', value);
    const api: any = this.gridApi;
    api.rowModel.resetCache();
    this.gridApi.paginationSetPageSize(Number(value));
  }
  getSelectedRowData() {
    const selectedNodes = this.gridApi.getSelectedNodes();
    const selectedData = selectedNodes.map(node => node.data);
    return selectedData;
  }

  reloadComponent() {
    const currentUrl = this.router.url;
    this.router.navigateByUrl('/', { skipLocationChange: true }).then(() =>
      this.router.navigate([currentUrl]));
  }


  onFirstDataRendered(params) {
    this.isFirstTimeLoad = true;
  }

  headerHeightSetter(event) {
    const padding = 20;
    const height = headerHeightGetter() + padding;
    this.gridApi.setHeaderHeight(height);
    this.gridApi.resetRowHeights();
  }

  refresh() {
    if (this.gridApi != null) {
      this.gridApi.onFilterChanged();
    }
  }
  onGridReady(params) {
    this.gridOptions.api.setDomLayout('autoHeight');
    this.paramList = params;
    this.gridApi = params.api;
    this.gridApi.setRowData([]);
    this.gridColumnApi = params.columnApi;
    const gridSavedState = sessionStorage.getItem('gateway_list_grid_column_state');
    if (gridSavedState) {
      this.gridColumnApi.setColumnState(JSON.parse(gridSavedState));
    }
    const filterSavedState = sessionStorage.getItem('gateway_list_grid_filter_state');
    if (filterSavedState) {
      this.gridApi.setFilterModel(JSON.parse(filterSavedState));
    } else {
    }
    const datasource = {
      getRows: (params: IGetRowsParams) => {
        this.loading = true;
        // this.companyname=this.selectedCompany;
        this.sortForAgGrid(params.sortModel);
        this.filterForAgGrid(params.filterModel);
        const id = sessionStorage.getItem('device_id');
        this.atcommandService.getATCQueueRequest(id, this.gridApi.paginationGetCurrentPage() + 1, this.column, this.order, this.pageLimit).subscribe(data => {
          this.loading = false;
          const datas = this.filterInLocal(params.filterModel, data.body.content);
          this.rowData = this.sortingInLocal(datas, params.sortModel);
          this.total_number_of_records = this.rowData.length;
          params.successCallback(this.rowData, this.rowData.length);
          this.autoSizeAll(false);
          if (this.isFirstTimeLoad == true) {
            const paginationSavedPage = sessionStorage.getItem('gateway_list_grid_saved_page');
            if (paginationSavedPage && this.isFirstTimeLoad) {
              this.gridOptions.api.paginationGoToPage(Number.parseInt(paginationSavedPage));
              this.isFirstTimeLoad = false;
              this.isPaginationStateRecovered = true;
            }
          }
        },
          error => {
            console.log(error);
          });
      }
    };
    this.gridApi.setDatasource(datasource);
  }

  isEmptyObject(obj) {
    return (obj && (Object.keys(obj).length === 0));
  }

  sortingInLocal(data: any, sortModel: any): any {
    if (sortModel && sortModel.length > 0) {
      const sortedArray = _.sortBy(data, sortModel[0].colId);
      if (sortModel[0].sort == 'desc') {
        sortedArray.reverse();
      }
      return sortedArray;
    } else {
      return data;
    }
  }
  filterInLocal(filterModel: any, data: any): any {
    if (filterModel != undefined && filterModel != null && !this.isEmptyObject(filterModel)) {
      const elasticModelFilters = JSON.parse(JSON.stringify(filterModel));
      filterModel = elasticModelFilters;
      const Okeys = Object.keys(filterModel);
      const keys: any[] = [];
      const values: any[] = [];
      for (let i = 0; i < Okeys.length; i++) {
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
  autoSizeAll(skipHeader: boolean) {
    const allColumnIds: string[] = [];
    this.gridColumnApi.getAllColumns()!.forEach((column) => {
      allColumnIds.push(column.getId());
    });
    this.gridColumnApi.autoSizeColumns(allColumnIds, skipHeader);
  }

  getFilterValuesData() {
    return this.userNameList;
  }
  filterForAgGrid(filterModel) {
    this.gridApi.paginationCurrentPage = 0;
    if (filterModel != undefined) {


      this.filterValuesJsonObj = {};
      this.filterValues = new Map();
      if (filterModel.created_epoch != undefined && filterModel.created_epoch.filter != null && filterModel.created_epoch.filter != undefined && filterModel.created_epoch.filter != '') {
        this.filterValues.set('created_epoch', filterModel.created_epoch.filter);

      }
      if (filterModel.at_command != undefined && filterModel.at_command.filter != null && filterModel.at_command.filter != undefined && filterModel.at_command.filter != '') {
        this.filterValues.set('at_command', filterModel.at_command.filter);
      }
      if (filterModel.priority != undefined && filterModel.priority.filter != null && filterModel.priority.filter != undefined && filterModel.priority.filter != '') {
        this.filterValues.set('priority', filterModel.priority.filter);
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
    this.sortFlag = true;
    switch (sortModel[0].colId) {
      case 'created_epoch': {

        this.column = 'createdEpoch';
        this.order = sortModel[0].sort;
        break;
      }
      case 'at_command': {

        this.column = 'at_command';
        this.order = sortModel[0].sort;
        break;
      }
      case 'priority': {

        this.column = 'priority';
        this.order = sortModel[0].sort;
        break;
      }
      default: {
      }
    }
  }

  onDragStopped(params) {
    const columnState = this.gridColumnApi.getColumnState();
    this.saveGridStateInSession(columnState);
  }
  onColumnResized(params) {
    const columnState = this.gridColumnApi.getColumnState();
    this.saveGridStateInSession(columnState);
  }
  onSortChanged(params) {
    const columnState = this.gridColumnApi.getColumnState();
    this.saveGridStateInSession(columnState);
  }

  onFilterChanged(params) {
    const filterModel = params.api.getFilterModel();
    sessionStorage.setItem('gateway_list_grid_filter_state', JSON.stringify(filterModel));
  }

  onColumnVisible(params) {
    const columnState = this.gridColumnApi.getColumnState();
    this.saveGridStateInSession(columnState);
  }

  onColumnPinned(params) {
    const columnState = this.gridColumnApi.getColumnState();
    this.saveGridStateInSession(columnState);
  }
  saveGridStateInSession(columnState: any) {
    // throw new Error('Method not implemented.');
  }


  onPaginationChanged(params) {
    this.isChecked = false;
    if (this.gridApi) {
      this.gridApi.forEachNode(function (node) {
        node.setSelected(false);
      });
      this.selectedIdsToDelete = [];
      if (this.isPaginationStateRecovered || params.api.paginationGetCurrentPage() > 0) {
        sessionStorage.setItem('gateway_list_grid_saved_page', params.api.paginationGetCurrentPage());
      }
    }
  }

  addATCommand() {
    this.atcommandService.isAddATCommand = true;
  }

  onCellClicked(params) {
    if (params.column.colId == 'imei') {
      // this.gatewayListService.deviceId = params.data.imei;
      // if(this.gatewayListService.deviceId != null){
      this.router.navigate(['gateway-profile']);
      // }
    }
  }
  deleteATcommand() {

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
        // params.column.columnApi.applyColumnState({ defaultState: { sort: null } });
        // params.column.columnApi.resetColumnState();
        params.api.onFilterChanged();
        sessionStorage.setItem('custom_gateway_list_grid_filter_state', null);
        // sessionStorage.setItem("custom_asset_list_grid_column_state", null);
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


function createNormalColDefs() {
  return [
    {
      headerName: 'Time Sent', field: 'created_epoch', sortable: true, filter: 'agDateColumnFilter', floatingFilter: true, filterParams: containsFilterParams, minWidth: 180,
      cellRenderer(params) {
        if (params.data != undefined) { 
          return '<div>' + moment.tz(new Date(Number(params.data.created_epoch)), 'America/Los_Angeles').format('YYYY-MM-DD HH:MM:SS') + '</div>';
          // return '<div>' + moment(new Date(Number(params.data.created_epoch))).format('YYYY-MM-DD HH:MM:SS','pst') + '</div>';

        }
      }
    },
    {
      headerName: 'Command', field: 'at_command', sortable: true, filter: 'agTextColumnFilter', floatingFilter: true, filterParams: containsFilterParams,
      cellRenderer(params) {
        if (params.data != undefined) {
          return '<div>' + params.data.at_command + '</div>';
        }
      }
    },
    {
      headerName: 'Source', field: 'source', hide: false, sortable: true, filter: 'agTextColumnFilter', filterParams: containsFilterParams,
      cellRenderer(params: any) {
        if (params.data != undefined && params.data.source != null) {
          return '<div>' + params.data.source + '</div>';
        }
      }
    },
    {
      headerName: 'Created By', field: 'created_by', sortable: true, filter: 'agTextColumnFilter', floatingFilter: true, filterParams: containsFilterParams,
      cellRenderer(params) {
        if (params.data != undefined && params.data.created_by != null) {
          return '<div>' + params.data.created_by + '</div>';
        }
      }
    },
    // {
    //   headerName: 'Priority', field: 'priority', sortable: true, filter: 'agTextColumnFilter', filterParams: containsFilterParams,
    //   cellRenderer: function (params) {
    //     if(params.data != undefined ){
    //    return '<div>'+params.data.priority+'</div>'
    //   }}
    // },
    {
      headerName: 'Status', field: 'status', sortable: true, filter: 'agTextColumnFilter', width: 240, floatingFilter: true, filterParams: containsFilterParams,
      cellRenderer(params: any) {
        if (params.data != undefined) {
          return 'Command is in queue and will be sent upon receiving next report';
        }
      }
    },
    {
      headerName: 'Server', field: 'server', sortable: true, filter: 'agTextColumnFilter', width: 240, floatingFilter: true, filterParams: containsFilterParams,
      cellRenderer(params: any) {
        if (params.data != undefined && params.data.server != null) {
          return '<div>' + params.data.server + '</div>';
        }
      }
    },
    {
      headerName: 'Device', field: 'device', sortable: true, filter: 'agTextColumnFilter', width: 240, floatingFilter: true, filterParams: containsFilterParams,
      cellRenderer(params: any) {
        if (params.data != undefined && params.data.device != null) {
          return '<div>' + params.data.device + '</div>';
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
}

const containsFilterParams = {
  filterOptions: [
    'contains',
    'equal'
  ],
  suppressAndOrCondition: true
};

function atcommandRequest(atcommandRequest: any) {
  throw new Error('Function not implemented.');
}



