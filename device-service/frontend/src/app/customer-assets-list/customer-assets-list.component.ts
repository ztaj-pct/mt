import { Component, OnInit, ViewChild } from '@angular/core';
import { AssetsService } from '../assets.service';
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
import { BtnCellRendererForCustomAsset } from './btn-cell-renderer.component';
import * as moment from 'moment';
import { CustomDateComponent } from '../ui-elements/custom-date-component.component';
import { CustomHeader } from '../ui-elements/custom-header.component';
import { PaginationConstants } from '../util/paginationConstants';
import { UsersListService } from '../users-list/users-list.component.service';
import { CompaniesService } from '../companies.service';



@Component({
  selector: 'app-customer-assets-list',
  templateUrl: './customer-assets-list.component.html',
  styleUrls: ['./customer-assets-list.component.css']
})
export class CustomerAssetsListComponent implements OnInit {
  sortFlag;
  currentPage: any;
  showingpage;
  totalpage;
  assetList: any[] = [];
  totalRecords = 0;
  currentEndRecord = 0;
  currentStartRecord = 0;
  pagedItems: any[];
  private allItems: any;
  pageLimit = 25;
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

  selected = 25;
  pageSizeOptions: PaginationConstants = new PaginationConstants();
  pageSizeObjArr = this.pageSizeOptions.pageSizeObjArr;
  pageSizeOptionsArr: number[];

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
    ColumnsToolPanelModule,
    MultiFilterModule,
    MultiFilterModule,
  ];
  rowModelType = 'infinite';
  // Pagination Save State
  isFirstTimeLoad = false;
  isPaginationStateRecovered = false;
  filterModelCountFilter: any;
  userNameList: String[];

  companies: any[];
  forwardingGroups: any[];
  totalNumberOfElements: any;

  constructor(
    private readonly companyService: CompaniesService,
    private readonly assetsService: AssetsService,
    public router: Router,
    private toastr: ToastrService,
    private pagerService: PagerService,
    public userListService: UsersListService) {
    this.getAllUser();
    this.getAllForwardingGroups();
    this.getAllcompanies();
    const containsFilterParams = {
      filterOptions: [
        'contains',
      ],
      suppressAndOrCondition: true
    };

    // const filterParamsUsers = {
    //   values: params => params.success(this.getFilterValuesData())
    // };

    // const filterParamsCompany = {
    //   values: params => params.success(this.getCompanyFilterValues())
    // };
    let self = this;
    const filterParamsCompany = {
      values(params: any) {
        setTimeout(function () {
          params.success(self.getCompanyFilterValues());
        }, 3000);
      },
    };

    const filterParamsUsers = {
      values(params: any) {
        setTimeout(function () {
          params.success(self.getFilterValuesData());
        }, 3000);
      },
    };

    const filterParamsForwardingGroups = {
      values: params => params.success(this.getForwardingGroupsFilterValues())
    };

    const filterParams1 = {

      filterOptions: [
        'equals', 'lessThan', 'greaterThan'
      ],
      defaultOption: 'equals',
      suppressAndOrCondition: true,
    };
    this.columnDefs = [
      {
        headerName: 'Organization', field: 'organisation_name', sortable: true, filter: 'agTextColumnFilter', minWidth: 200, filterParams: containsFilterParams,
        cellRenderer: function (params) {
          if (params.data) {
            return '<div class="pointer" style="color:#4ba9d7;"><a>' + params.data.organisation_name + '</a></div>';

          }
        }
      },
      {
        headerName: 'Forwarding Group',
        field: 'forwarding_group',
        sortable: true,
        filter: 'agSetColumnFilter',
        filterParams: filterParamsForwardingGroups,
        minWidth: 200
      },
      {
        headerName: 'Parent Group',
        field: 'parent_group',
        sortable: true,
        filter: 'agSetColumnFilter',
        filterParams: filterParamsCompany,
        minWidth: 200
      },
      {
        headerName: '# of Assets',
        field: 'count', sortable: true,
        filter: 'agNumberColumnFilter',
        filterParams: filterParams1,
        minWidth: 180,
      },
      {
        headerName: 'Created On', field: 'created_at', sortable: true, filter: 'agDateColumnFilter', filterParams: containsFilterParams,
        minWidth: 200,
        cellRenderer: (data) => {
          if (data.value) {
            return moment(data.value).format('YYYY-MM-DD HH:mm:ss');
          }
        }
      },
      {
        headerName: 'Created By', field: 'created_by',
        sortable: true, filter: 'agTextColumnFilter',
        floatingFilter: true, flex: 1,
        filterParams: containsFilterParams, minWidth: 180,

      },

      {
        headerName: 'Last Updated On', field: 'updated_at', sortable: true, filter: 'agDateColumnFilter', filterParams: containsFilterParams, minWidth: 200,
        cellRenderer: (data) => {
          if (data.value) {
            return moment(data.value).format('YYYY-MM-DD HH:mm:ss');
          }
        }
      },
      {
        headerName: 'Last Updated by', field: 'updated_by',
        sortable: true, filter: 'agSetColumnFilter',
        floatingFilter: true, flex: 1,
        filterParams: filterParamsUsers, minWidth: 200,
        cellRenderer: (data) => {
          if (data.value) {
            return data.value;
          }
        }

      },

      {
        headerName: 'Action', field: 'action', sortable: false, filter: 'agNumberColumnFilter', floatingFilter: true, floatingFilterComponent: 'customClearFloatingFilter', floatingFilterComponentParams: {
          suppressFilterButton: true
        }, cellRenderer: 'btnCellRenderer', width: 100, minWidth: 120, pinned: 'right'
      }


    ];
    this.cacheOverflowSize = 1;
    this.maxConcurrentDatasourceRequests = 1;
    this.infiniteInitialRowCount = 1;
    if (sessionStorage.getItem('customer_assets_list_page_size') != undefined) {
      this.pageLimit = Number(sessionStorage.getItem('customer_assets_list_page_size'));
    } else {
      this.pageLimit = 25;
    }
    this.gridOptions = {
      cacheBlockSize: this.pageLimit,
      paginationPageSize: this.pageLimit,
      rowModelType: 'infinite',
      context: {
        componentParent: this
      }
    };
    this.frameworkComponents = {
      btnCellRenderer: BtnCellRendererForCustomAsset,
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
    if (sessionStorage.getItem('customer_assets_list_page_size') != undefined) {
      this.pageLimit = Number(sessionStorage.getItem('customer_assets_list_page_size'));
    } else {
      this.pageLimit = 25;
    }
    this.sortFlag = false;
    this.userCompanyType = sessionStorage.getItem('type');
    this.isSuperAdmin = sessionStorage.getItem('IS_ROLE_SUPERADMIN');
    this.isRoleCustomerManager = sessionStorage.getItem('IS_ROLE_CUSTOMER_MANAGER');

    // if (this.isSuperAdmin) {

    // }

  }

  getAllUser() {
    this.userListService.getAllUsers().subscribe(data => {
      if (data != null) {
        const name = [];
        for (const iterator of data.body) {
          name.push(iterator.first_name + ' ' + iterator.last_name);
        }
        this.userNameList = name;
      }

    }, error => {
      console.log(error);
      this.loading = false;
    });
  }

  onGridReady(params) {
    // this.gridOptions.api.setDomLayout('autoHeight');
    this.paramList = params;
    this.gridApi = params.api;
    this.gridApi.setRowData([]);
    this.gridColumnApi = params.columnApi;
    const gridSavedState = sessionStorage.getItem('custom_asset_list_grid_column_state');
    if (gridSavedState) {
      this.gridColumnApi.setColumnState(JSON.parse(gridSavedState));
    }
    const filterSavedState = sessionStorage.getItem('custom_asset_list_grid_filter_state');
    if (filterSavedState) {
      this.gridApi.setFilterModel(JSON.parse(filterSavedState));
    }
    const datasource = {
      getRows: (params: IGetRowsParams) => {
        this.loading = true;
        this.sortForAgGrid(params.sortModel);
        this.filterForAgGrid(params.filterModel);
        this.assetsService.getCustomerAssetsListWithSort(this.gridApi.paginationGetCurrentPage() + 1, this.column, this.order, this.pageLimit, this.filterValuesJsonObj, this.filterModelCountFilter).subscribe(data => {
          this.loading = false;
          this.rowData = data.body.content;
          if (this.rowData != null && this.rowData.length > 0) {
            for (const iterator of this.rowData) {
              iterator.created_by = iterator.created_first_name ? iterator.created_first_name : '' + ' ' + iterator.created_last_name ? iterator.created_last_name : '';
              iterator.updated_by = iterator.updated_first_name ? iterator.updated_first_name : '' + ' ' + iterator.updated_last_name ? iterator.updated_last_name : '';
            }
          }

          this.totalNumberOfElements =  data.total_key;
          params.successCallback(this.rowData, data.total_key);
          if (this.isFirstTimeLoad === true) {
            const paginationSavedPage = sessionStorage.getItem('custom_asset_list_grid_saved_page');
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
      }
    };
    this.gridApi.setDatasource(datasource);
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
    // this.removefilterValues();
    sessionStorage.setItem('custom_asset_list_grid_filter_state', JSON.stringify(filterModel));
  }

  // async removefilterValues(){
  //   for(let i = 0;i<this.columnDefs.length;i++){
  //     document.querySelectorAll('[aria-colindex="' + (i+1) + '"]')[1].querySelector('input').value = '';
  //   }
  // }

  onColumnVisible(params) {
    const columnState = this.gridColumnApi.getColumnState();
    this.saveGridStateInSession(columnState);
  }

  onColumnPinned(params) {
    const columnState = this.gridColumnApi.getColumnState();
    this.saveGridStateInSession(columnState);
  }



  onPaginationChanged(params) {
    if (this.isPaginationStateRecovered || params.api.paginationGetCurrentPage() > 0) {
      sessionStorage.setItem('custom_asset_list_grid_saved_page', params.api.paginationGetCurrentPage());
    }
  }

  onFirstDataRendered(params) {
    this.isFirstTimeLoad = true;
  }

  saveGridStateInSession(columnState) {
    sessionStorage.setItem('custom_asset_list_grid_column_state', JSON.stringify(columnState));
  }




  addAssets() {
    this.assetsService.routeflag = false;
    this.router.navigate(['add-assets']);
  }
  uploadAssets() {
    this.router.navigate(['upload-assets']);
  }

  viewComapnyAssets(asset) {
    this.router.navigate(['assets'], { queryParams: { id: asset.organisation_id } });
  }




  filterForAgGrid(filterModel) {
    this.gridApi.paginationCurrentPage = 0;
    if (filterModel != undefined) {

      this.filterValuesJsonObj = {};
      this.filterValues = new Map();
      if (filterModel.organisation_name !== undefined && filterModel.organisation_name.filter != null && filterModel.organisation_name.filter !== undefined && filterModel.organisation_name.filter !== '') {
        this.filterValues.set('organisationName', filterModel.organisation_name.filter);
      }

      if (filterModel.forwarding_group && filterModel.forwarding_group.values) {
        const fgList = [];
        for (const iterator of filterModel.forwarding_group.values) {
          fgList.push(iterator);
        }
        const v = fgList.join(",");
        this.filterValues.set('forwardingGroup', v.length == 0 ? 'null' : v);
      }

      if (filterModel.parent_group && filterModel.parent_group.values) {
        const rList = [];
        for (const iterator of filterModel.parent_group.values) {
          rList.push(iterator);
        }
        const v = rList.join(",");
        this.filterValues.set('parentGroup', v.length == 0 ? 'null' : v);
      }

      // if (filterModel.created_by != undefined && filterModel.created_by.filter != null && filterModel.created_by.filter != undefined && filterModel.created_by.filter != "") {
      //   this.filterValues.set("createdBy", filterModel.created_by.filter);
      // }
      if (filterModel.created_by && filterModel.created_by.filter ) {
        if (filterModel.created_by.filter.length == 0) {
          this.filterValues.set('createdBy', 'null');
        } else {
          this.filterValues.set('createdBy', filterModel.created_by.filter.toString());
        }
      }
      // if (filterModel.updated_by != undefined && filterModel.updated_by.filter != null && filterModel.updated_by.filter != undefined && filterModel.updated_by.filter != "") {
      //   this.filterValues.set("updatedBy", filterModel.updated_by.filter);
      // }
      if (filterModel.updated_by != undefined && filterModel.updated_by.values != null && filterModel.updated_by.values != undefined) {
        if (filterModel.updated_by.values.length == 0) {
          this.filterValues.set('updatedBy', 'null');
        } else {
          this.filterValues.set('updatedBy', filterModel.updated_by.values.toString());
        }
      }
      if (filterModel.created_at != undefined && filterModel.created_at.dateFrom != null && filterModel.created_at.dateFrom != undefined && filterModel.created_at.dateFrom != '') {
        this.filterValues.set('createdAt', filterModel.created_at.dateFrom);
      }

      if (filterModel.updated_at != undefined && filterModel.updated_at.dateFrom != null && filterModel.updated_at.dateFrom != undefined && filterModel.updated_at.dateFrom != '') {
        this.filterValues.set('updatedAt', filterModel.updated_at.dateFrom);
      }

      // if (filterModel.count != undefined && filterModel.count.filter != null && filterModel.count.filter != undefined && filterModel.count.filter != "") {
      //   this.filterValues.set("count", filterModel.count.filter);
      // }
      if (filterModel.count !== undefined && filterModel.count.filter != null && filterModel.count.filter !== undefined && filterModel.count.filter !== '') {
        this.filterValues.set('count', filterModel.count.filter);
        this.filterModelCountFilter = filterModel.count.type;
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
      case 'organisation_name': {
        this.column = 'organisationName';
        this.order = sortModel[0].sort;
        break;
      }
      case 'forwarding_group': {
        this.column = 'forwardingGroup';
        this.order = sortModel[0].sort;
        break;
      }
      case 'parent_group': {
        this.column = 'parentGroup';
        this.order = sortModel[0].sort;
        break;
      }
      case 'count': {

        this.column = 'count';
        this.order = sortModel[0].sort;
        break;
      }

      case 'created_at': {
        this.column = 'createdAt';
        this.order = sortModel[0].sort;
        break;
      }

      case 'created_by': {
        this.column = 'createdFirstName';
        this.order = sortModel[0].sort;
        break;
      }

      case 'updated_at': {
        this.column = 'updatedAt';
        this.order = sortModel[0].sort;
        break;
      }
      case 'updated_by': {

        this.column = 'updatedFirstName';
        this.order = sortModel[0].sort;
        break;
      }
      default: {
      }
    }
  }
  onCellClicked(params) {
    if (params.column.colId == 'organisation_name') {
      this.assetsService.assetCompanyId = params.data.organisation_id;
      this.viewComapnyAssets(params.data);
    }
  }




  exportAssetList(asset: any) {
    this.assetsService.exportAssetList(asset.company_id).subscribe(data => {
    }, error => {
      console.log(error);
    });
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
        this.column = 'organisationName';
        this.order = 'asc';
        params.api.setFilterModel(null);
        // params.column.columnApi.applyColumnState({ defaultState: { sort: null } });
        // params.column.columnApi.resetColumnState();
        params.api.onFilterChanged();
        sessionStorage.setItem('custom_asset_list_grid_filter_state', null);
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

  getFilterValuesData() {
    return this.userNameList;
  }

  onPageSizeChange() {
    sessionStorage.setItem('customer_assets_list_page_size', String(this.selected));
    const value = this.selected;
    this.pageLimit = (Number(value));
    this.gridApi.gridOptionsWrapper.setProperty('cacheBlockSize', value);
    const api: any = this.gridApi;
    api.rowModel.resetCache();
    this.gridApi.paginationSetPageSize(Number(value));
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

  getCompanyFilterValues() {
    return this.companies;
  }

  getAllcompanies() {
    this.companyService.getAllCompanyName().subscribe(
      data => {
        this.companies = data && data.length > 0 ? data : [];
      },
      error => {
        console.log(error);
      }
    );
  }

  onBtNormalCols() {
    this.gridApi.onFilterChanged();
  }
}
