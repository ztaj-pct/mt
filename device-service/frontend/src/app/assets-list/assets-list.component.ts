import { Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { AssetsService } from '../assets.service';
import { Asset } from '../models/asset';
import { ActivatedRoute, Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { PagerService } from '../util/pagerService';
import { CustomErrorToastrComponent } from '../custom-error-toastr/custom-error-toastr.component';
import { CustomSuccessToastrComponent } from '../custom-success-toastr/custom-success-toastr.component';
import { AgGridAngular } from '@ag-grid-community/angular';
import { GridOptions, IGetRowsParams } from '@ag-grid-community/all-modules';
import { ClientSideRowModelModule } from '@ag-grid-community/client-side-row-model';
import { MenuModule } from '@ag-grid-enterprise/menu';
import { ColumnsToolPanelModule } from '@ag-grid-enterprise/column-tool-panel';
import '@ag-grid-community/core/dist/styles/ag-grid.css';
import '@ag-grid-community/core/dist/styles/ag-theme-alpine.css';
import { Module } from '@ag-grid-community/core';
import { MultiFilterModule } from '@ag-grid-enterprise/multi-filter';
import { BtnCellRendererForAssets } from './btn-cell-renderer.component';
import * as moment from 'moment';
import { BtnCellRendererForCheckbox } from './btn-cell-render.component';
import { RichSelectModule } from '@ag-grid-enterprise/rich-select';
import { CustomDateComponent } from '../ui-elements/custom-date-component.component';
import { PaginationConstants } from '../util/paginationConstants';
import { CustomDatePipe } from '../pipes/custom-date.pipe';
declare var $: any;


@Component({
  selector: 'app-assets-list',
  templateUrl: './assets-list.component.html',
  styleUrls: ['./asset-list.component.css']

})
export class AssetsListComponent implements OnInit {
  currentPage = 1;
  isFirst = true;
  assetList: Asset[] = Array(new Asset());
  totalpage;
  showingpage;
  deviceId;
  deleteDeviceID;
  searchFlag;
  range = 10000;
  sortFlag;
  totalNumberOfRecords = 0;
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
  column = 'assignedName';
  order = 'asc';
  assetToDelete = Asset;
  userCompanyType;
  pageSizeOptions: PaginationConstants = new PaginationConstants();
  pageSizeObjArr = this.pageSizeOptions.pageSizeObjArr;
  pageSizeOptionsArr: number[];
  selected: number;
  isSuperAdmin;
  rolePCTUser;
  isRoleCustomerManager;
  isRoleCustomerManagerInstaller;
  isRoleOrgAdmin: any;
  isRoleOrgUser: any;
  selectedIdsToDelete: string[] = [];
  isChecked = false;
  filterModelYearType;
  selectAll = false;

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
  organisationId: any;

  roleCallCenterUser: any;
  constructor(
    private assetsService: AssetsService,
    public router: Router,
    private toastr: ToastrService,
    private pagerService: PagerService,
    private readonly activateRoute: ActivatedRoute) {
    this.roleCallCenterUser = JSON.parse(sessionStorage.getItem('IS_ROLE_CALL_CENTER_USER'));
    this.activateRoute.queryParams.subscribe(params => {
      this.organisationId = params.id;
    });
    if (!(this.organisationId) && this.roleCallCenterUser) {
      const id = JSON.parse(sessionStorage.getItem('ORGANISATION_ID'));
      if (id) {
        this.organisationId = id;
      }
    }
    this.assetsService.organisationId = this.organisationId;
    const containsFilterParams = {
      filterOptions: [
        'contains',
      ],
      suppressAndOrCondition: true
    };
    const cutomeDatepipe = new CustomDatePipe();

    const filterParams1 = {

      filterOptions: [
        'equals'
      ],
      defaultOption: 'equals',
      suppressAndOrCondition: true,
    };
    this.columnDefs = [
      {
        headerName: '#', minWidth: 60, headerCheckboxSelection: true,
        headerCheckboxSelectionFilteredOnly: true, filter: false,
        checkboxSelection: true,
      },
      {
        headerName: 'Asset ID', field: 'assigned_name', sortable: true, filter: 'agTextColumnFilter', filterParams: containsFilterParams,
        cellRenderer(params) {
          if (params.data != undefined) {
            return '<a href="javascript:void(0)">' + params.value + '</a>';
          }
        }
      },
      {
        headerName: 'Asset Nickname', field: 'asset_nick_name', sortable: true, filter: 'agTextColumnFilter', filterParams: containsFilterParams,
      },
      {
        headerName: 'Product', field: 'eligible_gateway',
        sortable: true, filter: 'agSetColumnFilter', filterParams: filterParamsProductType,
        minWidth: 200,
      },
      {
        headerName: 'VIN', field: 'vin', sortable: true, filter: 'agTextColumnFilter', filterParams: containsFilterParams, minWidth: 220,
        cellRenderer(params) {
          if (params.data && params.data.vin) {
            if (params.data.is_vin_validated == false) {
              return '<i class="fa fa-exclamation-triangle" aria-hidden="true" style="color:darkred"></i>&nbsp;&nbsp;' + params.data.vin;
            } else if (params.data.is_vin_validated == true) {
              return '<i class="fa fa-check-circle" aria-hidden="true" style="color:green"></i>&nbsp;&nbsp;' + params.data.vin;
            } else {
              return params.data.vin;
            }
          }
        }
      },
      {
        headerName: 'Category', field: 'category', sortable: true, filter: 'agSetColumnFilter', floatingFilter: true, flex: 1, minWidth: 200,
        filterParams,

      },
      {
        headerName: '# Tires', field: 'no_of_tires', sortable: true, filter: 'agTextColumnFilter', filterParams: containsFilterParams,
      },
      {
        headerName: '# Axles', field: 'no_of_axel', sortable: true, filter: 'agTextColumnFilter', filterParams: containsFilterParams,
      },
      {
        headerName: 'Length', field: 'external_length', sortable: true, filter: 'agTextColumnFilter', filterParams: containsFilterParams,
      },
      {
        headerName: 'Door', field: 'door_type', sortable: true, filter: 'agTextColumnFilter', filterParams: containsFilterParams,
      },
      {
        headerName: 'Year',
        field: 'year',
        sortable: true,
        filter: 'agNumberColumnFilter',
        filterParams: filterParams1,

      },
      {
        headerName: 'Make', field: 'make', sortable: true,
        filter: 'agSetColumnFilter', filterParams: filterParamsMake,

      },

      {
        headerName: 'Status', field: 'status', sortable: true, filter: 'agSetColumnFilter', floatingFilter: true, flex: 1,
        filterParams: filterParamsStatus,

      },
      // {
      //   headerName: 'IMEI', field: 'imei', filter: 'agTextColumnFilter', filterParams: containsFilterParams, minWidth: 200,

      // },
      // {
      //   headerName: 'Install Date ', field: 'installed',sortable: true, filter: 'agSetColumnFilter', floatingFilter: true, flex: 1, minWidth: 200,
      //   cellRenderer: (data) => {
      //     if (data.value) {
      //       return moment(data.value).format('YYYY-MM-DD HH:mm');
      //     }
      //   }

      // },

      // {
      //   headerName: 'Installation Date', field: 'installation_date', sortable: true, suppressSorting: true, filter: 'agSetColumnFilter', filterParams: filterParamsInstalledStatusFlag, minWidth: 200,
      //   cellRenderer: function (params) {
      //     console.log("params?.data?.installation_date");
      //    // console.log(params.data.installation_date);
      //     if (params.data !== undefined && params.data.installation_date != null) {
      //       if (params.data.status != null && params.data.status.toLowerCase() === 'active') {
      //         return '<div>'  + params.data.installation_date +   '</div>';
      //       } else {
      //         return '<div>  </div>';
      //       }
      //     }
      //   }
      // },

      {
        headerName: 'Added', field: 'datetime_created', sortable: true, filter: 'agDateColumnFilter', filterParams: containsFilterParams, minWidth: 200,
        cellRenderer: (data) => {
          if (data.value) {
            return cutomeDatepipe.transform(data.value, 'latest_report');
          }
        }
      },

      {
        headerName: 'Updated', field: 'datetime_updated', sortable: true, filter: 'agDateColumnFilter', filterParams: containsFilterParams, minWidth: 200,
        cellRenderer: (data) => {
          if (data.value) {
            return cutomeDatepipe.transform(data.value, 'latest_report');
          }
        }
      },
      {
        headerName: 'Comment', field: 'comment', sortable: true, filter: 'agTextColumnFilter', filterParams: containsFilterParams,

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

    // if (sessionStorage.getItem('assetlistsize') !== undefined) {
    //   this.pageLimit = Number(sessionStorage.getItem('assetlistsize'));
    // }


    this.gridOptions = {
      cacheBlockSize: this.pageLimit,
      paginationPageSize: this.pageLimit,
      rowModelType: 'infinite',
    };
    this.frameworkComponents = {
      btnCellRenderer: BtnCellRendererForAssets,
      btnCellRender: BtnCellRendererForCheckbox,
      agDateInput: CustomDateComponent,
      // agColumnHeader: CustomHeader
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
    this.sortFlag = true;
    this.userCompanyType = sessionStorage.getItem('type');
    this.isSuperAdmin = sessionStorage.getItem('IS_ROLE_SUPERADMIN');
    this.isRoleCustomerManager = sessionStorage.getItem('IS_ROLE_CUSTOMER_MANAGER');
    this.isRoleOrgAdmin = JSON.parse(sessionStorage.getItem('IS_ROLE_ORG_ADMIN'));
    this.isRoleOrgUser = JSON.parse(sessionStorage.getItem('IS_ROLE_ORG_USER'));
    this.rolePCTUser = JSON.parse(sessionStorage.getItem('IS_ROLE_PCT_USER'));
    this.isRoleCustomerManagerInstaller = sessionStorage.getItem('IS_ROLE_CUSTOMER_MANAGER_INSTALLER');
  }


  onGridReady(params) {
    this.paramList = params;
    this.gridApi = params.api;
    this.gridApi.setRowData([]);
    this.gridColumnApi = params.columnApi;
    const gridSavedState = sessionStorage.getItem('asset_list_grid_column_state_' + this.organisationId);
    if (gridSavedState) {
      this.gridColumnApi.setColumnState(JSON.parse(gridSavedState));
    }
    const filterSavedState = sessionStorage.getItem('asset_list_grid_filter_state_' + this.organisationId);
    if (filterSavedState) {
      this.gridApi.setFilterModel(JSON.parse(filterSavedState));
    }
    const datasource = {
      getRows: (params: IGetRowsParams) => {

        this.sortForAgGrid(params.sortModel);
        this.filterForAgGrid(params.filterModel);
        if (this.isFirst) {
          // this.loading = true;
          $('#temp1').addClass('loadingAsset');
          this.assetsService.getActiveAssetsWithSort(this.currentPage,
            this.column, this.order, this.organisationId, this.filterValuesJsonObj, this.pageLimit, this.filterModelYearType).subscribe(data => {
              this.rowData = data.body.content;
              $('#temp1').removeClass('loadingAsset');
              if (this.rowData != null && this.rowData.length > 0) {
                if (this.organisationId) {
                  this.selectedCompanyName = this.rowData[0].company_name;
                } else {
                  this.selectedCompanyName = 'All Organization';
                }
                for (const iterator of this.rowData) {
                  iterator.year = iterator.manufacturer_details.year;
                  iterator.make = iterator.manufacturer_details.make;
                }
              }
              this.totalNumberOfRecords = data.total_key;
              if (this.totalNumberOfRecords <= (this.currentPage * this.pageLimit)) {
                this.isFirst = false;
              }
              this.currentPage++;
              params.successCallback(this.rowData, data.total_key);
              if (this.selectAll) {
                let self = this;
                setTimeout(function () {
                  self.deleteAll({ checked: true });
                }, 300);
              }
            }, error => {
              $('#temp1').removeClass('loadingAsset');
              this.loading = false;
              console.log(error);
            });
        }
      }
    };
    this.gridApi.setDatasource(datasource);
  }


  onDragStopped(params) {
    this.saveGridStateInSession(this.gridColumnApi.getColumnState());
  }

  onSortChanged(params) {
    this.currentPage = 1;
    this.isFirst = true;
    this.saveGridStateInSession(this.gridColumnApi.getColumnState());
  }

  onFilterChanged(params) {
    this.currentPage = 1;
    this.isFirst = true;
    sessionStorage.setItem('asset_list_grid_filter_state_' + this.organisationId, JSON.stringify(params.api.getFilterModel()));
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
    this.isChecked = false;
    this.gridApi.forEachNode(function (node) {
      node.setSelected(false);
    });
    this.selectedIdsToDelete = [];
    if (this.isPaginationStateRecovered || params.api.paginationGetCurrentPage() > 0) {
      sessionStorage.setItem('asset_list_grid_saved_page_' + this.organisationId, params.api.paginationGetCurrentPage());
    }
  }
  onFirstDataRendered(params) {
    this.isFirstTimeLoad = true;
  }


  saveGridStateInSession(columnState) {
    sessionStorage.setItem('asset_list_grid_column_state_' + this.organisationId, JSON.stringify(columnState));
  }


  onCellClicked(params) {

    this.assetToDelete = params.data;
    if (params.column.colId === 'assigned_name') {
      this.loading = true;
      if (this.userCompanyType === 'Manufacturer' || this.isSuperAdmin === 'true' || this.isRoleCustomerManager === 'true' || this.roleCallCenterUser || this.isRoleOrgAdmin || this.isRoleOrgUser || this.rolePCTUser) {
        this.router.navigate(['add-assets'], { queryParams: { id: params.data.asset_uuid, organisationId: this.organisationId } });
      }
    }

  }


  getSelectedRowData() {
    const selectedNodes = this.gridApi.getSelectedNodes();
    const selectedData = selectedNodes.map(node => node.data);
    return selectedData;
  }

  getAsset(currentPage) {
    const comapnyId = this.organisationId;
    this.assetsService.getAllAssets(currentPage, comapnyId).subscribe(
      data => {
        this.totalpage = data.total_pages;
        this.showingpage = data.currentPage + 1;
        this.allItems = data.totalKey;
        this.selectedCompanyName = this.assetList[0].company_name;
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
  addAssets() {
    this.router.navigate(['add-assets']);
  }
  uploadAssets() {
    this.router.navigate(['upload-assets']);
  }

  // onPageSizeChange() {
  //   sessionStorage.setItem('assetlistsize', String(this.selected));
  //   const value = this.selected;
  //   this.pageLimit = (Number(value));
  //   this.gridApi.gridOptionsWrapper.setProperty('cacheBlockSize', value);
  //   const api: any = this.gridApi;
  //   api.rowModel.resetCache();
  //   this.gridApi.paginationSetPageSize(Number(value));
  // }

  editAsset(asset: Asset): void {
    if (this.userCompanyType === 'Manufacturer' || this.isSuperAdmin === 'true' || this.isRoleCustomerManager === 'true' || this.roleCallCenterUser || this.rolePCTUser) {
      this.router.navigate(['add-assets']);
    }
  }







  filterForAgGrid(filterModel) {
    this.gridApi.paginationCurrentPage = 0;
    if (filterModel) {

      this.filterValuesJsonObj = {};
      this.filterValues = new Map();
      if (filterModel.assigned_name && filterModel.assigned_name.filter && filterModel.assigned_name.filter !== '') {
        this.filterValues.set('assignedName', filterModel.assigned_name.filter);
      }

      if (filterModel.eligible_gateway != undefined && filterModel.eligible_gateway.values != null && filterModel.eligible_gateway.values != undefined) {
        if (filterModel.eligible_gateway.values.length == 0) {
          this.filterValues.set('eligible_gateway', 'null');
        } else {
          this.filterValues.set('eligible_gateway', filterModel.eligible_gateway.values.toString());
        }
      }


      if (filterModel.vin !== undefined && filterModel.vin.filter != null && filterModel.vin.filter !== undefined && filterModel.vin.filter !== '') {
        this.filterValues.set('vin', filterModel.vin.filter);
      }

      if (filterModel.imei !== undefined && filterModel.imei.filter != null && filterModel.imei.filter !== undefined && filterModel.imei.filter !== '') {
        this.filterValues.set('imei', filterModel.imei.filter);
      }
      if (filterModel.category !== undefined && filterModel.category.values != null && filterModel.category.values !== undefined) {
        if (filterModel.category.values.length === 0) {
          this.filterValues.set('category', 'null');
        } else {
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

      if (filterModel.datetime_created != undefined && filterModel.datetime_created.dateFrom != null && filterModel.datetime_created.dateFrom != undefined && filterModel.datetime_created.dateFrom != '') {
        this.filterValues.set('datetimeCreated', filterModel.datetime_created.dateFrom);
      }

      if (filterModel.datetime_updated != undefined && filterModel.datetime_updated.dateFrom != null && filterModel.datetime_updated.dateFrom != undefined && filterModel.datetime_updated.dateFrom != '') {
        this.filterValues.set('datetimeUpdated', filterModel.datetime_updated.dateFrom);
      }

      if (filterModel.year != undefined && filterModel.year.filter != null && filterModel.year.filter != undefined && filterModel.year.filter != '') {
        this.filterValues.set('year', filterModel.year.filter);
        this.filterModelYearType = filterModel.year.type;
      }

      if (filterModel.make && filterModel.make.values) {
        if (filterModel.make.values.length === 0) {
          this.filterValues.set('make', 'null');
        } else {
          this.filterValues.set('make', filterModel.make.values.toString());
        }
      }
      if (filterModel.comment && filterModel.comment.filter && filterModel.comment.filter !== '') {
        this.filterValues.set('comment', filterModel.comment.filter);
      }
      if (filterModel.asset_nick_name && filterModel.asset_nick_name.filter && filterModel.asset_nick_name.filter !== '') {
        this.filterValues.set('assetNickName', filterModel.asset_nick_name.filter);
      }
      if (filterModel.no_of_tires && filterModel.no_of_tires.filter && filterModel.no_of_tires.filter !== '') {
        this.filterValues.set('noOfTires', filterModel.no_of_tires.filter);
      }
      if (filterModel.no_of_axel && filterModel.no_of_axel.filter && filterModel.no_of_axel.filter !== '') {
        this.filterValues.set('noOfAxles', filterModel.no_of_axel.filter);
      }
      if (filterModel.external_length && filterModel.external_length.filter && filterModel.external_length.filter !== '') {
        this.filterValues.set('externalLength', filterModel.external_length.filter);
      }
      if (filterModel.door_type && filterModel.door_type.filter && filterModel.door_type.filter !== '') {
        this.filterValues.set('doorType', filterModel.door_type.filter);
      }



      const jsonObject = {};
      this.filterValues.forEach((filter, key) => {
        jsonObject[key] = filter;
      });
      this.filterValuesJsonObj = jsonObject;
    }
  }

  openExportPopup() {
    $('#exportCSVModal').modal('show');
  }

  testExportAssetList() {
    this.loading = true;
    this.assetsService.getActiveAssetsWithSortForDownload(this.gridApi.paginationGetCurrentPage(),
      this.column, this.order, this.organisationId, this.filterValuesJsonObj, this.range, this.filterModelYearType).subscribe(data => {
        this.loading = false;
      }, error => {
        this.loading = false;
        console.log(error);
      });
  }

  sortForAgGrid(sortModel) {
    this.gridApi.paginationCurrentPage = 0;
    if (sortModel[0] == undefined || sortModel[0] == null || sortModel[0].colId == undefined || sortModel[0].colId == null) {
      return;
    }
    this.sortFlag = true;
    switch (sortModel[0].colId) {
      case 'assigned_name': {
        this.column = 'assignedName';
        this.order = sortModel[0].sort;
        break;
      }
      case 'eligible_gateway': {
        this.column = 'gatewayEligibility';
        this.order = sortModel[0].sort;
        break;
      }
      case 'vin': {
        this.column = 'vin';
        this.order = sortModel[0].sort;
        break;
      }
      case 'category': {
        this.column = 'category';
        this.order = sortModel[0].sort;
        break;
      }

      case 'year': {
        this.column = 'year';
        this.order = sortModel[0].sort;
        break;
      }

      case 'make': {
        this.column = 'manufacturer';
        this.order = sortModel[0].sort;
        break;
      }

      case 'status': {
        this.column = 'status';
        this.order = sortModel[0].sort;
        break;
      }

      case 'datetime_created': {
        this.column = 'createdAt';
        this.order = sortModel[0].sort;

        break;
      }

      case 'datetime_updated': {
        this.column = 'updatedAt';
        this.order = sortModel[0].sort;
        break;
      }
      case 'comment': {
        this.column = 'comment';
        this.order = sortModel[0].sort;
        break;
      }
      case 'asset_nick_name': {
        this.column = 'assetNickName';
        this.order = sortModel[0].sort;
        break;
      }
      case 'no_of_tires': {
        this.column = 'noOfTires';
        this.order = sortModel[0].sort;
        break;
      }
      case 'no_of_axel': {
        this.column = 'noOfAxles';
        this.order = sortModel[0].sort;
        break;
      }
      case 'external_length': {
        this.column = 'externalLength';
        this.order = sortModel[0].sort;
        break;
      }
      case 'door_type': {
        this.column = 'doorType';
        this.order = sortModel[0].sort;
        break;
      }
      default: {
        console.log('default');
      }
    }
  }

  countRecords(data) {
    if (data.body.content === null || data.body.content.length === 0) {
      this.totalRecords = 0;
      this.currentStartRecord = 0;
      this.currentEndRecord = 0;
    } else {
      this.totalRecords = data.total_key;
      this.currentStartRecord = (this.pageLimit * (data.current_page)) + 1;
      if (this.pageLimit == data.body.content.length) {
        this.currentEndRecord = this.pageLimit * (data.current_page + 1);
      } else {
        this.currentEndRecord = this.currentStartRecord + data.body.content.length - 1;
      }
    }
  }
  _getdata(page: number) {
    if (page === 0) {
      page = 1;
    } else {
      const comapnyId = this.organisationId;
      this.assetsService.getAllAssets(page, comapnyId).subscribe(data => {

        this.assetList = data.body.content;
        this.totalpage = data.total_pages;
        this.showingpage = data.currentPage + 1;
        this.countRecords(data);
        // initialize to page 1
        this.setPage(page, data.body, data.total_pages, data.body.length);
        if (data.body.content == null) {
          this.showingpage = 0;
        }
      }, error => {
        console.log(error);
      });
    }
  }



  setPage(page: number, body, totalPages, pageItems) {
    this.pager = this.pagerService.getPager(this.allItems, page, totalPages, pageItems);
    this.pagedItems = body;
  }

  backToList() {
    this.router.navigate(['customerAssets']);
  }



  exportAssetList() {
    this.assetsService.exportAssetList(this.assetsService.assetCompanyId).subscribe(data => {
    }, error => {
      console.log(error);
    });
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
    // const currentUrl = this.router.url;
    // this.router.navigateByUrl('/', { skipLocationChange: true }).then(() =>
    //   this.router.navigate([currentUrl]));
    this.gridApi.onFilterChanged();
  }

  deleteAsset(asset: any): void {

    this.assetsService.deleteassetById([asset.asset_uuid]).subscribe(data => {
      if (data.body.length === 0) {
        this.rowData = this.assetList.filter(u => u !== asset);
        this.toastr.success('Asset Deleted Successfully', null, { toastComponent: CustomSuccessToastrComponent });
      } else {
        this.toastr.error('The record with the Asset ID ' + data.body +
          ' already has hardware installed, and so it cannot be deleted.', 'Problem with Deletion', { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
      }
    }, error => {
      console.log(error);
    });
    this.reloadComponent();
  }

  getSelctedRow() {
    this.assetList = this.getSelectedRowData();
    for (let i = 0; i < this.assetList.length; i++) {
      this.assetList[i].is_selected = true;
      if (!this.selectedIdsToDelete.find(x => x === this.assetList[i].asset_uuid)) {
        this.selectedIdsToDelete.push(this.assetList[i].asset_uuid);
      }
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
      this.assetList = this.getSelectedRowData();
      for (let i = 0; i < this.assetList.length; i++) {
        this.assetList[i].is_selected = true;
        if (!this.selectedIdsToDelete.find(x => x == this.assetList[i].asset_uuid)) {
          this.selectedIdsToDelete.push(this.assetList[i].asset_uuid);
        }
      }
    } else if (!event.checked) {
      this.selectedIdsToDelete = [];
      this.assetList = this.getSelectedRowData();
      for (let i = 0; i < this.assetList.length; i++) {
        this.assetList[i].is_selected = false;
        this.selectedIdsToDelete.splice(this.selectedIdsToDelete.indexOf(this.assetList[i].asset_uuid), 1);

      }
    }
  }

  deleteAllAssets() {
    if (this.selectedIdsToDelete.length < 1) {
      this.toastr.error('Please select at least one asset', 'No Asset Selected', { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
      return;
    }
    this.assetsService.deleteassetById(this.selectedIdsToDelete).subscribe(data => {
      if (data.body.length === 0) {
        // this.getAsset(this.currentPage);
        this.gridApi.onFilterChanged();

        this.selectedIdsToDelete = [];
        this.toastr.success('Asset Deleted successfully', null, { toastComponent: CustomSuccessToastrComponent });
      } else {
        this.toastr.error('The record with the Asset ID ' + data.body + ' already has hardware installed, and so it cannot be deleted.', 'Problem with Deletion', { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
        // this.getAsset(this.currentPage);
        this.gridApi.onFilterChanged();
        this.selectedIdsToDelete = [];
      }
      this.isChecked = false;
    }, error => {
      console.log(error);
    });
    this.reloadComponent();
  }

  onBtNormalCols() {
    this.gridApi.onFilterChanged();
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

}

const filterParams = {
  values(params) {
    setTimeout(function () {
      params.success(['Trailer', 'Chassis', 'Container', 'Vehicle', 'Tractor']);
    }, 2000);
  },
};


const filterParamsStatus = {
  values(params) {
    setTimeout(function () {
      params.success(['Partial', 'Pending', 'Install in progress', 'Active', 'Inactive', 'Deleted', 'Error']);
    }, 2000);
  },
};

const filterParamsProductType = {
  values(params) {
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

const filterParamsMake = {
  values(params) {
    setTimeout(function () {
      params.success([
        'BENZ',
        'WABASH',
        'HYUNDAI',
        'CIMC',
        'STOUGHTON',
        'FORD',
        'HONDA',
        'WABASH NATIONAL CORPORATION',
        'NISSAN',
        'CHEVROLET',
        'UTILITY TRAILER MANUFACTURER',
        'MAZDA',
        'KENTUCKY',
        'CADILLAC',
        'GMC',
        'DATSUN',
        'MERCEDES-BENZ',
        'KIA',
        'SATURN',
        'TOYOTA',
        'JEEP',
        'INFINITI',
        'VOLKSWAGEN',
        'HYUNDAI TRANSLEAD',
        'BMW',
        'STOUGHTON TRAILERS',
        'GREAT DANE',
        'VALOR',
        'KENTUCKY TRAILER',
        'STERLING TRUCK',
        'DODGE',
        'LINCOLN',
        'SUBARU',
        'LEXUS',
        'HYTRA',
        'HUMMER',
        'WINNEBAGO',
        'CHRYSLER',
        'ACURA',
        'PETERBILT',
        'HYUNDAI TRANSLEAD TRAILERS',
        'BUICK',
        'KENWORTH',
        'KENTUCKY MANUFACTURING CO./ KENTUCKY TRAILER/ KENTUCKY TRAILER TECH',
        'FREIGHTLINER',
        'AUDI',
        'LAND ROVER',
        'RAM',
        'WHITEGMC',
        'PORSCHE',
        'OLDSMOBILE',
        '678432',
        'MAKE',
        'NULL',
        'cheetah',
        'mon1',
        'che1',
        'mec1',
        'cim1',
        'transcraft',
        'Not Available',
        'MONON',
        'CHEETAH CHASSIS',
        'FONTAINE TRAILER CO.',
        'GREAT DANE TRAILERS',
        'VANGUARD NATIONAL TRAILER CORPORATION (VNTC)',
        'STRICK COMMERCIAL TRAILER',
        'BULK TANK INTERNATIONAL',
        'SILVER EAGLE MANUFACTURING COMPANY',
        'PINES',
        'POLAR TANK TRAILER',
        'MICKEY TRUCK BODIES INC.',
        'HACKNEY AND SONS, INC.',
        'HESSE CORPORATION',
        'PINE RIDGE',
        'TRAILMOBILE',
        'VOLVO TRUCK',
        'MACK',
        'SAAB',
        'WORKHORSE',
        'Not Available'
      ]);
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