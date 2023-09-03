import { AgGridAngular } from '@ag-grid-community/angular';
import { Module, ClientSideRowModelModule, MenuModule, MultiFilterModule, RowGroupingModule, IGetRowsParams } from '@ag-grid-enterprise/all-modules';
import { ColumnsToolPanelModule } from '@ag-grid-enterprise/column-tool-panel';
import { Component, OnInit, ViewChild } from '@angular/core';
import { FormBuilder, FormControl } from '@angular/forms';
import { Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { interval } from 'rxjs/internal/observable/interval';
import { CustomDateComponent } from 'src/app/ui-elements/custom-date-component.component';
import { CustomHeader } from 'src/app/ui-elements/custom-header.component';
import { PagerService } from 'src/app/util/pagerService';
import { PaginationConstants } from 'src/app/util/paginationConstants';
import { BtnCellRendererForATCommand } from '../atcommand-list/btn-cell-renderer.component';
import { ATCommandService } from '../atcommand.service';
import * as _ from 'underscore';
import moment from 'moment-timezone';
@Component({
  selector: 'app-atcommand-summary',
  templateUrl: './atcommand-summary.component.html',
  styleUrls: ['./atcommand-summary.component.css']
})
export class AtcommandSummaryComponent implements OnInit {
  sortFlag: boolean = false;
  currentPage: any;
  showingpage: any;
  totalpage: any;
  gatewayList: any[] = [];
  totalRecords = 0;
  currentEndRecord = 0;
  currentStartRecord = 0;
  pagedItems: any[] = [];
  private allItems: any;
  pageLimit = 25;
  total_number_of_records = 0;
  pager: any = {};
  sortCreatedOn = false;
  sortByCreatedBy = false;
  sortByLastUpdatedBy = false;
  sortByLastUpdateOn = false;
  pageSizeOptions: PaginationConstants = new PaginationConstants();
  pageSizeObjArr = this.pageSizeOptions.pageSizeObjArr;
  column = 'created_date';
  order = 'desc';
  userCompanyType: any;
  isSuperAdmin: any;
  isRoleCustomerManager: any;
  //parameters for counting records
  filterValues = new Map();
  filterValuesJsonObj: any = {};
  isClearButtonVisible = false;
  loading = false;
  frameworkComponents: any;
  public sideBar: any;

  @ViewChild('myGrid')
  myGrid!: AgGridAngular;
  public gridOptions: any;
  public gridApi: any;
  public columnDefs: any;
  public gridColumnApi!: any;
  public cacheOverflowSize: any;
  public maxConcurrentDatasourceRequests: any;
  public infiniteInitialRowCount: any;
  public defaultColDef: any;
  public paramList: any;
  public components: any;
  rowData: any;
  public modules: Module[] = [
    ClientSideRowModelModule,
    MenuModule,
    ColumnsToolPanelModule,
    MultiFilterModule,
    RowGroupingModule,
  ];
  rowModelType = 'infinite';

  //Pagination Save State
  isFirstTimeLoad = false;
  isPaginationStateRecovered = false;
  filterModelCountFilter: any;
  userNameList!: String[];
  companies!: any[];
  company: any;
  selectedCompany = new FormControl();
  selectDays = new FormControl(0);
  companyUuid = '1';
  companyname: any;
  days = 0;
  selectedtoDateRange: any[] = [];
  selected!: number;
  selectedIdsToDelete: string[] = [];
  isChecked: boolean = false;
  updateSubscription: any;


  constructor(private _formBuldier: FormBuilder, public atcommandService: ATCommandService, public router: Router, private toastr: ToastrService, private pagerService: PagerService) {
    this.columnDefs = [
      {
        headerName: 'Created Date', field: 'created_date', sortable: true, filter: 'agDateColumnFilter', floatingFilter: true, filterParams: containsFilterParams, minWidth: 180,
        cellRenderer: function (params: any) {
          if (params.data != undefined && params.data.created_date != null) {
            return '<div>' + moment.tz(params.data.created_date, 'America/Los_Angeles').format('YYYY-MM-DD HH:MM:SS') + '</div>';
          }
        }
      },
      {
        headerName: 'AT Command', field: 'at_command', sortable: true, filter: 'agTextColumnFilter', filterParams: containsFilterParams, floatingFilter: true,
        cellRenderer: function (params: any) {
          if (params.data != undefined && params.data.at_command != null) {
            return '<div>' + params.data.at_command + '</div>';
          }
        }
      },
      {
        headerName: 'Source', field: 'source', hide: false, sortable: true, filter: 'agTextColumnFilter', filterParams: containsFilterParams,
        cellRenderer: function (params: any) {
          if (params.data != undefined && params.data.source != null) {
            return '<div>' + params.data.source + '</div>';
          }
        }
      },
      {
        headerName: 'Created By', field: 'created_by', sortable: true, filter: 'agTextColumnFilter', floatingFilter: true, filterParams: containsFilterParams,
        cellRenderer: function (params) {
          if (params.data != undefined && params.data.created_by != null) {
            return '<div>' + params.data.created_by + '</div>';
          }
        }
      },
      {
        headerName: 'Status', field: 'status', sortable: true, filter: 'agTextColumnFilter', floatingFilter: true, filterParams: containsFilterParams,
        cellRenderer: function (params: any) {
          if (params.data != undefined && params.data.status != null) {
            return '<div>' + params.data.status + '</div>';
          }
        }
      },
      {
        headerName: 'Time Executed', field: 'last_executed', sortable: true, filter: 'agTextColumnFilter', floatingFilter: true, filterParams: containsFilterParams,
        cellRenderer: function (params: any) {
          if (params.data != undefined && params.last_executed != null) {
            return '<div>' + params.data.last_executed + '</div>';
          }
        }
      },
      {
        headerName: 'Device Response', field: 'device_response', sortable: true, filter: 'agTextColumnFilter', floatingFilter: true, filterParams: containsFilterParams,
        cellRenderer: function (params: any) {
          if (params.data != undefined && params.data.device_response != null) {
            return '<div>' + params.data.device_response + '</div>';
          }
        }
      },
      // {
      //   headerName: 'Packet', field: 'packet', sortable: true, filter: 'agTextColumnFilter', width: 240, floatingFilter: true, filterParams: containsFilterParams,
      //   cellRenderer: function (params: any) {
      //     if (params.data != undefined && params.data.packet != null) {
      //       return '<div>' + params.data.packet + '</div>'
      //     }
      //   }
      // },
      {
        headerName: 'Retries', field: 'retry_count', sortable: true, filter: 'agTextColumnFilter', floatingFilter: true, filterParams: containsFilterParams,
        cellRenderer: function (params: any) {
          if (params.data != undefined && params.data.retry_count != null) {
            return '<div>' + params.data.retry_count + '</div>';
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
      minWidth: 200,
      sortable: true,
      floatingFilter: true,
      enablePivot: true,
      resizable: true,
      filter: true,
      wrapText: true,
      autoHeight: true,
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

  ngOnInit(): void {
    this.atcommandService.isAddATCommand = false;
    this.loading = true;
    // this.updateSubscription = interval(10000).subscribe(
    //   (val) => { this.refresh(); }
    // );
  }

  autoSizeAll(skipHeader: boolean) {
    const allColumnIds: string[] = [];
    this.gridColumnApi.getAllColumns().forEach((column) => {
      allColumnIds.push(column.getId());
    });
    this.gridColumnApi.autoSizeColumns(allColumnIds, skipHeader);
  }
  refresh() {
    if (this.gridApi != null) {
      this.gridApi.onFilterChanged();
    }
  }

  onGridReady(params: { api: any; columnApi: any; }) {
    this.gridOptions.api.setDomLayout('autoHeight');
    this.paramList = params;
    this.gridApi = params.api;
    this.gridApi.setRowData([]);
    this.gridColumnApi = params.columnApi;
    const gridSavedState = sessionStorage.getItem('atcommand_summary_grid_column_state');
    if (gridSavedState) {
      this.gridColumnApi.setColumnState(JSON.parse(gridSavedState));
    }
    const filterSavedState = sessionStorage.getItem('atcommand_summary_grid_filter_state');
    if (filterSavedState) {
      this.gridApi.setFilterModel(JSON.parse(filterSavedState));
    } else {
    }
    const datasource = {
      getRows: (params: IGetRowsParams) => {
        this.loading = true;
        this.sortForAgGrid(params.sortModel);
        this.filterForAgGrid(params.filterModel);
        const id = sessionStorage.getItem('device_id');
        this.atcommandService.getGatewayAtRequest(id, this.gridApi.paginationGetCurrentPage() + 1, this.column, this.order, this.pageLimit).subscribe((data) => {
          this.loading = false;
          // this.rowData = data.body.content;
          const datas = this.filterInLocal(params.filterModel, data.body.content);
          this.rowData = this.sortingInLocal(datas, params.sortModel);
          this.total_number_of_records = this.rowData.length;

          // this.total_number_of_records = data.total_key;

          params.successCallback(this.rowData, this.rowData.length);
          this.autoSizeAll(false);
          if (this.isFirstTimeLoad == true) {
            const paginationSavedPage = sessionStorage.getItem('atcommand_summary_grid_saved_page');
            if (paginationSavedPage && this.isFirstTimeLoad) {
              this.gridOptions.api.paginationGoToPage(Number.parseInt(paginationSavedPage));
              this.isFirstTimeLoad = false;
              this.isPaginationStateRecovered = true;
            }
          }
        },
          (error: any) => {
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
  clearFilter() {
    if (this.gridApi != null) {
      this.gridApi.setFilterModel(null);
      this.gridApi.onFilterChanged();
    }
  }

  onPageSizeChange() {
    sessionStorage.setItem('atcommandSummarysize', String(this.selected));
    const value = this.selected;
    this.pageLimit = (Number(value));
    this.gridApi.gridOptionsWrapper.setProperty('cacheBlockSize', value);
    const api: any = this.gridApi;
    api.rowModel.resetCache();
    this.gridApi.paginationSetPageSize(Number(value));
  }
  getSelectedRowData() {
    const selectedNodes = this.gridApi.getSelectedNodes();
    const selectedData = selectedNodes.map((node: { data: any; }) => node.data);
    return selectedData;
  }

  reloadComponent() {
    const currentUrl = this.router.url;
    this.router.navigateByUrl('/', { skipLocationChange: true }).then(() =>
      this.router.navigate([currentUrl]));
  }


  onFirstDataRendered(params: any) {
    this.isFirstTimeLoad = true;
  }

  headerHeightSetter(event: any) {
    const padding = 20;
    const height = headerHeightGetter() + padding;
    this.gridApi.setHeaderHeight(height);
    this.gridApi.resetRowHeights();
  }
  filterForAgGrid(filterModel: any) {
    this.gridApi.paginationCurrentPage = 0;
    if (filterModel != undefined) {


      this.filterValuesJsonObj = {};
      this.filterValues = new Map();
      if (filterModel.at_command != undefined && filterModel.at_command.filter != null && filterModel.at_command.filter != undefined && filterModel.at_command.filter != '') {
        this.filterValues.set('at_command', filterModel.at_command.filter);
      }
      if (filterModel.priority != undefined && filterModel.priority.filter != null && filterModel.priority.filter != undefined && filterModel.priority.filter != '') {
        this.filterValues.set('priority', filterModel.priority.filter);
      }
      if (filterModel.source != undefined && filterModel.source.filter != null && filterModel.source.filter != undefined && filterModel.source.filter != '') {
        this.filterValues.set('source', filterModel.source.filter);
      }
      if (filterModel.last_executed != undefined && filterModel.last_executed.filter != null && filterModel.last_executed.filter != undefined && filterModel.last_executed.filter != '') {
        this.filterValues.set('last_executed', filterModel.last_executed.filter);
      }
      if (filterModel.success != undefined && filterModel.success.filter != null && filterModel.success.filter != undefined && filterModel.success.filter != '') {
        this.filterValues.set('success', filterModel.success.filter);
      }
      if (filterModel.packet != undefined && filterModel.packet.filter != null && filterModel.packet.filter != undefined && filterModel.packet.filter != '') {
        this.filterValues.set('packet', filterModel.packet.filter);
      }
      if (filterModel.created_date != undefined && filterModel.created_date.filter != null && filterModel.created_date.filter != undefined && filterModel.created_date.filter != '') {
        this.filterValues.set('created_date', filterModel.created_date.filter);
      }
      if (filterModel.retry_count != undefined && filterModel.retry_count.filter != null && filterModel.retry_count.filter != undefined && filterModel.retry_count.filter != '') {
        this.filterValues.set('retry_count', filterModel.retry_count.filter);
      }
      const jsonObject: any = {};
      this.filterValues.forEach((filter, key) => {
        jsonObject[key] = filter;
      });
      this.filterValuesJsonObj = jsonObject;
    }
  }

  sortForAgGrid(sortModel: any) {
    this.gridApi.paginationCurrentPage = 0;
    if (sortModel[0] == undefined || sortModel[0] == null || sortModel[0].colId == undefined || sortModel[0].colId == null) {
      return;
    }
    this.sortFlag = true;
    switch (sortModel[0].colId) {
      case 'at_command': {

        this.column = 'at_command';
        this.order = sortModel[0].sort;
        break;
      }
      case 'source': {

        this.column = 'source';
        this.order = sortModel[0].sort;
        break;
      }
      case 'priority': {

        this.column = 'priority';
        this.order = sortModel[0].sort;
        break;
      }
      case 'last_executed': {

        this.column = 'last_executed';
        this.order = sortModel[0].sort;
        break;
      }
      case 'success': {

        this.column = 'success';
        this.order = sortModel[0].sort;
        break;
      }
      case 'packet': {

        this.column = 'packet';
        this.order = sortModel[0].sort;
        break;
      }
      case 'created_date': {

        this.column = 'created_date';
        this.order = sortModel[0].sort;
        break;
      }
      case 'retry_count': {

        this.column = 'retry_count';
        this.order = sortModel[0].sort;
        break;
      }
      default: {
      }
    }
  }

  onDragStopped(params: any) {
    const columnState = this.gridColumnApi.getColumnState();
    this.saveGridStateInSession(columnState);
  }
  onColumnResized(params: any) {
    const columnState = this.gridColumnApi.getColumnState();
    this.saveGridStateInSession(columnState);
  }
  onSortChanged(params: any) {
    const columnState = this.gridColumnApi.getColumnState();
    this.saveGridStateInSession(columnState);
  }

  onFilterChanged(params: { api: { getFilterModel: () => any; }; }) {
    const filterModel = params.api.getFilterModel();
    sessionStorage.setItem('atcommand_summary_grid_filter_state', JSON.stringify(filterModel));
  }

  onColumnVisible(params: any) {
    const columnState = this.gridColumnApi.getColumnState();
    this.saveGridStateInSession(columnState);
  }

  onColumnPinned(params: any) {
    const columnState = this.gridColumnApi.getColumnState();
    this.saveGridStateInSession(columnState);
  }
  saveGridStateInSession(columnState: any) {
    //   throw new Error('Method not implemented.');
  }

  exportData(){
    this.gridOptions.api.exportDataAsCsv();
  }


  onPaginationChanged(params: { api: { paginationGetCurrentPage: any } }) {
    this.isChecked = false;
    if (this.gridApi !== undefined && this.gridApi !== null) {
      this.gridApi.forEachNode(function (node: { setSelected: (arg0: boolean) => void; }) {
        node.setSelected(false);
      });
    }
    this.selectedIdsToDelete = [];
    if (this.isPaginationStateRecovered || params.api.paginationGetCurrentPage() > 0) {
      sessionStorage.setItem('atcommand_summary_grid_saved_page', params.api.paginationGetCurrentPage());
    }
  }

  public addATCommand() {
    this.atcommandService.isAddATCommand = true;
  }

  onCellClicked(params: any) {
    if (params.column.colId == 'imei') {
      // this.gatewayListService.deviceId = params.data.imei;
      // if(this.gatewayListService.deviceId != null){
      this.router.navigate(['device-profile']);
      // }
    }
  }

  getClearFilterFloatingComponent() {
    function ClearFloatingFilter() { }
    ClearFloatingFilter.prototype.init = function (params: { api: { setFilterModel: (arg0: null) => void; onFilterChanged: () => void; }; }) {
      this.eGui = document.createElement('div');
      this.eGui.innerHTML =
        '<button class="btnClear primary_buttonClear" (click)="clearFilter()">Clear Filters</button>';
      this.currentValue = null;
      this.clearFilterButton = this.eGui.querySelector('button');
      const that = this;
      function onClearFilterButtonClicked(this: any) {
        this.column = 'imei';
        this.order = 'asc';
        params.api.setFilterModel(null);
        // params.column.columnApi.applyColumnState({ defaultState: { sort: null } });
        // params.column.columnApi.resetColumnState();
        params.api.onFilterChanged();
        sessionStorage.setItem('custom_atcommand_summary_grid_filter_state', '');
        // sessionStorage.setItem("custom_asset_list_grid_column_state", null);
      }
      this.clearFilterButton.addEventListener('click', onClearFilterButtonClicked);
    };
    ClearFloatingFilter.prototype.onParentModelChanged = function (parentModel: { filter: string; }) {
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

  const columnHeaderTextsArray: Element[] = [];

  columnHeaderTexts.forEach(node => columnHeaderTextsArray.push(node));

  const clientHeights = columnHeaderTextsArray.map(
    headerText => headerText.clientHeight
  );
  const tallestHeaderTextHeight = Math.max(...clientHeights);
  return tallestHeaderTextHeight;
}

const containsFilterParams = {
  filterOptions: [
    'contains',
  ],
  suppressAndOrCondition: true
};

