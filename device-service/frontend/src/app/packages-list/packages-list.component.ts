import { Component, OnInit, Inject, ViewChild } from '@angular/core';
import { Router } from "@angular/router";
import { FormBuilder, FormGroup, Validators, FormControl } from '@angular/forms';
import { PagerService } from '../util/pagerService';
import { User } from "../models/user.model";
import { UserService } from '../user/user.component.service';
import { ToastrService } from 'ngx-toastr';
import { ForGotPasswordservice } from '../forgotpassword/forgotpassword.component.service';
import { CampaignService } from '../campaign/campaign.component.service';
import { ServerSideRowModelModule } from '@ag-grid-enterprise/server-side-row-model';
import { AgGridAngular } from '@ag-grid-community/angular';
import { GridOptions, IDatasource, IGetRowsParams } from '@ag-grid-community/all-modules';
import { HttpClient } from '@angular/common/http';
import { ClientSideRowModelModule } from '@ag-grid-community/client-side-row-model';
import { MenuModule } from '@ag-grid-enterprise/menu';
import { ColumnsToolPanelModule } from '@ag-grid-enterprise/column-tool-panel';
import '@ag-grid-community/all-modules/dist/styles/ag-grid.css';
import '@ag-grid-community/all-modules/dist/styles/ag-theme-alpine.css';
import { Module } from '@ag-grid-community/core';
import { MultiFilterModule } from '@ag-grid-enterprise/multi-filter';
import { SetFilterModule } from '@ag-grid-enterprise/set-filter';
import { BtnCellRenderer } from './btn-cell-renderer.component';
import * as moment from 'moment-timezone';
import { CustomSuccessToastrComponent } from '../custom-success-toastr/custom-success-toastr.component';
import { CustomDateComponent } from '../ui-elements/custom-date-component.component';
import { CustomHeader } from '../ui-elements/custom-header.component';
import { PaginationConstants } from '../util/paginationConstants';
import { AssetsService } from '../assets.service';
declare var $: any;

@Component({
  selector: 'app-packages-list',
  templateUrl: './packages-list.component.html',
  styleUrls: ['./packages-list.component.css']
})
export class PackagesListComponent implements OnInit {

  selected:number;
  currentPage: any;
  pager: any = {};
  pagedItems: any[];
  private allItems: any;
  totalpage;
  showingpage;
  page = 0;
  loading = false;
  total_number_of_records = 0;
  //parameters for sorting
  sortByPackageName = false;
  sortByBinVersion = false;
  sortByAppVersion = false;
  sortByMcuVersion = false;
  sortByBleVersion = false;
  sortByConfig1 = false;
  sortByConfig2 = false;
  sortByConfig3 = false;
  sortByConfig4 = false;
  sortFlag;
  column = "packageName";
  order = "asc";

  //parameters for counting records
  pageLimit = 25;
  totalRecords = 0;
  currentEndRecord = 0;
  currentStartRecord = 0;
  packageUuid;
  packages: any[]

  package_name = new FormControl();
  bin_version = new FormControl();
  app_version = new FormControl();
  mcu_version = new FormControl();
  ble_version = new FormControl();
  config1 = new FormControl();
  config4 = new FormControl();
  filterValues = new Map();
  filterValuesJsonObj: any = {};
  isClearButtonVisible = false;
  frameworkComponents;

  pageSizeOptions: PaginationConstants = new PaginationConstants();
  pageSizeObjArr=this.pageSizeOptions.pageSizeObjArr;
  pageSizeOptionsArr: number[];
  //role for access
  userCompanyType;
  isSuperAdmin;

  // ag-grid code 
  @ViewChild('myGrid') myGrid: AgGridAngular;
  public gridOptions: Partial<GridOptions>;
  public gridApi;
  public paramList;
  public gridColumnApi;
  public columnDefs;
  public cacheOverflowSize;
  public maxConcurrentDatasourceRequests;
  public infiniteInitialRowCount;
  public components;
  rowData: any;
  public modules: Module[] = [
    ClientSideRowModelModule,
    MenuModule,
    ColumnsToolPanelModule,
    MultiFilterModule,
    SetFilterModule,
  ];
  public sideBar;
  public defaultColDef;
  //Pagination Save State
  isFirstTimeLoad = false;
  isPaginationStateRecovered = false;
  rowModelType = 'infinite';
  // end ag-grid code
  constructor(private campaignService: CampaignService, private userService: UserService, private pagerService: PagerService,
    private router: Router, private _formBuldier: FormBuilder, private toastr: ToastrService, private forGotPasswordservice: ForGotPasswordservice, private http: HttpClient ) {
    var containsFilterParams = {
      filterOptions: [
        'contains',
      ],
      suppressAndOrCondition: true
    };
    this.columnDefs = [
      {
        headerName: 'Name', field: 'package_name', sortable: true, filter: 'agTextColumnFilter', filterParams: containsFilterParams,
        minWidth: 250,
        cellRenderer: function (params) {
          if (params != undefined && params.value != null && params.value != undefined)
            return '<a href="javascript:void(0)">' + params.value + '</a>'
        }
      },
      {
        headerName: 'Created Time UTC',
        field: 'created_at',
        filter: 'agDateColumnFilter',
        filterParams: containsFilterParams,
        minWidth: 200,
        cellRenderer: (data) => {
          if (data != undefined && data.value != undefined)
            return moment(data.value).format('YYYY-MM-DD HH:mm')
        }
      },
      { headerName: 'BIN', field: 'bin_version', sortable: true, filter: 'agTextColumnFilter', filterParams: containsFilterParams },
      { headerName: 'App', field: 'app_version', sortable: true, filter: 'agTextColumnFilter', filterParams: containsFilterParams },
      { headerName: 'MCU', field: 'mcu_version', sortable: true, filter: 'agTextColumnFilter', filterParams: containsFilterParams },
      { headerName: 'BLE', field: 'ble_version', sortable: true, filter: 'agTextColumnFilter', filterParams: containsFilterParams },
      { headerName: 'Config1', field: 'config1', sortable: true, filter: 'agTextColumnFilter', filterParams: containsFilterParams },
      { headerName: 'Config2', field: 'config2', hide: true, sortable: true, filter: 'agTextColumnFilter', filterParams: containsFilterParams },
      { headerName: 'Config3', field: 'config3', hide: true, sortable: true, filter: 'agTextColumnFilter', filterParams: containsFilterParams },
      { headerName: 'Config4', field: 'config4', sortable: true, filter: 'agTextColumnFilter', filterParams: containsFilterParams },
      {
        headerName: 'In Use', field: 'is_used_in_campaign', sortable: true,
        filter: 'agSetColumnFilter', filterParams: filterParams, floatingFilter: true,
        minWidth: 130,
        cellRenderer: params => {
          if (params != undefined && params.data != undefined && params.data.is_used_in_campaign) {
            return `<i class="fa fa-check" aria-hidden="true"></i>`;
          } else {
            return '';
          }

        }
      },
      // { headerName: 'Action', field: 'action', sortable: false, filter: false, cellRenderer: 'btnCellRenderer', width: 100, minWidth: 100, pinned: 'right' }
      {
        headerName: 'Action', field: 'action', sortable: false, filter: 'agNumberColumnFilter', floatingFilter: true, floatingFilterComponent: 'customClearFloatingFilter', floatingFilterComponentParams: {
          suppressFilterButton: true
        }, cellRenderer: 'btnCellRenderer', width: 100, minWidth: 120, pinned: 'right'
      }
    ];
    this.components = {
      customClearFloatingFilter: this.getClearFilterFloatingComponent(),
    };
    this.cacheOverflowSize = 2;
    this.maxConcurrentDatasourceRequests = 2;
    this.infiniteInitialRowCount = 2;
    if (sessionStorage.getItem('package_list_page_size')!=undefined){
      this.pageLimit = Number(sessionStorage.getItem('package_list_page_size'));
    } else {
      this.pageLimit = 25;
    }
    this.gridOptions = {
      cacheBlockSize: this.pageLimit,
      paginationPageSize: this.pageLimit,
      rowModelType: 'infinite',
    }
    this.frameworkComponents = {
      btnCellRenderer: BtnCellRenderer,
      agDateInput: CustomDateComponent,
      agColumnHeader: CustomHeader
    }

    this.defaultColDef = {
      flex: 1,
      minWidth: 150,
      sortable: true,
      floatingFilter: true,
      enablePivot: true,
      resizable: true,
      headerComponentParams: { menuIcon: 'fa-bars' },
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
            suppressColumnExpandAll: true,
          },
        },
      ],
      defaultToolPanel: '',
    };

    

  }

  ngOnInit() {
    if (sessionStorage.getItem('package_list_page_size')!=undefined){
      this.pageLimit = Number(sessionStorage.getItem('package_list_page_size'));
    }
   else{
      this.pageLimit = 25;
   }

    this.userCompanyType = sessionStorage.getItem('type');
    this.isSuperAdmin = sessionStorage.getItem('IS_ROLE_SUPERADMIN');
    if(this.isSuperAdmin === 'true'){
      this.router.navigate(['packages']);
    }else{
      this.router.navigate(['customerAssets']);
    }
    this.package_name.valueChanges.subscribe((package_name) => {
      this.valueChangeEvent();
    });

    this.bin_version.valueChanges.subscribe((bin_version) => {
      this.valueChangeEvent();
    });

    this.app_version.valueChanges.subscribe((app_version) => {
      this.valueChangeEvent();
    });

    this.mcu_version.valueChanges.subscribe((mcu_version) => {
      this.valueChangeEvent();
    });

    this.ble_version.valueChanges.subscribe((ble_version) => {
      this.valueChangeEvent();
    });

    this.config1.valueChanges.subscribe((config1) => {
      this.valueChangeEvent();
    });

    this.config4.valueChanges.subscribe((config4) => {
      this.valueChangeEvent();
    });


    this.sortFlag = false;
    if (this.currentPage === 0 || this.currentPage === undefined) {
      this.currentPage = 1;
      this.getPackages(this.currentPage);
    }
    else {
      this.getPackages(this.showingpage);
    }
  }

  onCellClicked(params) {
    if (params.column.colId == "package_name") {
      this.loading = true;
      var uuid = params.data.uuid;
      this.campaignService.findByPackageId(uuid).subscribe(data => {
        this.campaignService.editFlag = true;
        this.campaignService.packageDetail = data.body;
        this.loading = false;
        this.router.navigate(['package']);
      }, error => {
        console.log(error)
      });
    }
  }

  clickOnParticularFilter(id) {
    $("#" + id).addClass("hidden");
    if (id == "package_name") {
      this.package_name.patchValue("");
    } else if (id == "bin_version") {
      this.bin_version.patchValue("");
    } else if (id == "app_version") {
      this.app_version.patchValue("");
    } else if (id == "mcu_version") {
      this.mcu_version.patchValue("");
    } else if (id == "ble_version") {
      this.ble_version.patchValue("");
    } else if (id == "config1") {
      this.config1.patchValue("");
    } else if (id == "config4") {
      this.config4.patchValue("");
    }
  }

  clearFilter() {
    // this.filterValuesJsonObj = {};
    // this.filterValues = new Map();
    // this.getPackages(1);
    // this.clearFormControls();
    // this.isClearButtonVisible = false;
    this.gridApi.setFilterModel(null);
    this.paramList.api.paginationCurrentPage = 0;
    this.onGridReady(this.paramList);
  }

  clearFormControls() {
    this.package_name.patchValue("");
    this.bin_version.patchValue("");
    this.app_version.patchValue("");
    this.mcu_version.patchValue("");
    this.ble_version.patchValue("");
    this.config1.patchValue("");
    this.config4.patchValue("");
  }

  valueChangeEvent() {
    this.filterValuesJsonObj = {};
    this.filterValues = new Map();
    if (this.package_name.value != null && this.package_name.value != undefined && this.package_name.value != "") {
      this.filterValues.set("packageName", this.package_name.value);
      this.isClearButtonVisible = true;
      $("#package_name").removeClass("hidden");
    } else {
      $("#package_name").addClass("hidden");
    }

    if (this.bin_version.value != null && this.bin_version.value != undefined && this.bin_version.value != "") {
      this.filterValues.set("binVersion", this.bin_version.value);
      this.isClearButtonVisible = true;
      $("#bin_version").removeClass("hidden");
    } else {
      $("#bin_version").addClass("hidden");
    }

    if (this.app_version.value != null && this.app_version.value != undefined && this.app_version.value != "") {
      this.filterValues.set("appVersion", this.app_version.value);
      this.isClearButtonVisible = true;
      $("#app_version").removeClass("hidden");
    } else {
      $("#app_version").addClass("hidden");
    }

    if (this.mcu_version.value != null && this.mcu_version.value != undefined && this.mcu_version.value != "") {
      this.filterValues.set("mcuVersion", this.mcu_version.value);
      this.isClearButtonVisible = true;
      $("#mcu_version").removeClass("hidden");
    } else {
      $("#mcu_version").addClass("hidden");
    }

    if (this.ble_version.value != null && this.ble_version.value != undefined && this.ble_version.value != "") {
      this.filterValues.set("bleVersion", this.ble_version.value);
      this.isClearButtonVisible = true;
      $("#ble_version").removeClass("hidden");
    } else {
      $("#ble_version").addClass("hidden");
    }

    if (this.config1.value != null && this.config1.value != undefined && this.config1.value != "") {
      this.filterValues.set("config1", this.config1.value);
      this.isClearButtonVisible = true;
      $("#config1").removeClass("hidden");
    } else {
      $("#config1").addClass("hidden");
    }

    if (this.config4.value != null && this.config4.value != undefined && this.config4.value != "") {
      this.filterValues.set("config4", this.config4.value);
      this.isClearButtonVisible = true;
      $("#config4").removeClass("hidden");
    } else {
      $("#config4").addClass("hidden");
    }

    let jsonObject = {};
    this.filterValues.forEach((value, key) => {
      jsonObject[key] = value
    });
    this.filterValuesJsonObj = jsonObject;
    this.getPackages(1);
  }

  countRecords(data) {
    if (data.body === null || data.body.length === 0) {
      this.totalRecords = 0;
      this.currentStartRecord = 0;
      this.currentEndRecord = 0;
    } else {
      this.totalRecords = data.total_key;
      this.currentStartRecord = (this.pageLimit * (data.current_page - 1)) + 1;
      if (this.pageLimit == data.body.length) {
        this.currentEndRecord = this.pageLimit * (data.current_page);
      }
      else {
        this.currentEndRecord = this.currentStartRecord + data.body.length - 1;
      }
    }
  }

  getPackages(currentPage) {
    this.campaignService.getPackages(currentPage, this.column, this.order, this.filterValuesJsonObj, this.pageLimit).subscribe(data => {
      this.packages = data.body;
      this.totalpage = data.total_pages;
      this.showingpage = data.current_page;
      this.allItems = data.total_key;
      this.countRecords(data);
      this.setPage(1, data, data.total_pages, data.body.length);
      if (data.body == null) {
        this.showingpage = 0;
      }
    }, error => {
      console.log(error);
    })

  }

  deletePackage(id) {
    this.packageUuid = id;
  }

  reloadComponent() {
    let currentUrl = this.router.url;
    this.router.navigateByUrl('/', { skipLocationChange: true }).then(() =>
      this.router.navigate([currentUrl]));
  }

  deletePackageDb() {
    this.campaignService.deletePackage(this.campaignService.packageId).subscribe(data => {
      //this.toastr.success(data.message);
      this.toastr.success(data.message, null, { toastComponent: CustomSuccessToastrComponent });
      this.campaignService.packageDetail = undefined;
      this.campaignService.packageId = null;
      this.paramList.api.paginationCurrentPage = 0;
      // this.onGridReady(this.paramList);
      this.reloadComponent();
    }, error => {
      console.log(error);
      this.reloadComponent();
    });
  }

  setPage(page: number, body, total_pages, pageItems) {
    // get pager object from service
    this.pager = this.pagerService.getPager(this.allItems, page, total_pages, pageItems);

    // get current page of items
    this.pagedItems = body;
  }

  sortBy(columnName) {
    this.currentPage = 1;
    this.sortFlag = true;
    switch (columnName) {
      case 'package_name': {
        if (this.sortByPackageName) {
          this.column = "packageName";
          this.order = "asc";
          this.sortByPackageName = false;
        } else {
          this.column = "packageName";
          this.order = "desc";
          this.sortByPackageName = true;
        }

        this.getPackages(this.currentPage);

        break;
      }
      case 'bin_version': {
        if (this.sortByBinVersion) {
          this.column = "binVersion";
          this.order = "asc";
          this.sortByBinVersion = false;
        } else {
          this.column = "binVersion";
          this.order = "desc";
          this.sortByBinVersion = true;
        }

        this.getPackages(this.currentPage);
        break;
      }
      case 'app_version': {
        if (this.sortByAppVersion) {
          this.column = "appVersion";
          this.order = "asc";
          this.sortByAppVersion = false;
        } else {
          this.column = "appVersion";
          this.order = "desc";
          this.sortByAppVersion = true;
        }

        this.getPackages(this.currentPage);
        break;
      }
      case 'mcu_version': {
        if (this.sortByMcuVersion) {
          this.column = "mcuVersion";
          this.order = "asc";
          this.sortByMcuVersion = false;
        } else {
          this.column = "mcuVersion";
          this.order = "desc";
          this.sortByMcuVersion = true;
        }

        this.getPackages(this.currentPage);
        break;
      }

      case 'ble_version': {
        if (this.sortByBleVersion) {
          this.column = "bleVersion";
          this.order = "asc";
          this.sortByBleVersion = false;
        } else {
          this.column = "bleVersion";
          this.order = "desc";
          this.sortByBleVersion = true;
        }

        this.getPackages(this.currentPage);
        break;
      }

      case 'config1': {
        if (this.sortByConfig1) {
          this.column = "config1";
          this.order = "asc";
          this.sortByConfig1 = false;
        } else {
          this.column = "config1";
          this.order = "desc";
          this.sortByConfig1 = true;
        }

        this.getPackages(this.currentPage);
        break;
      }

      case 'config2': {
        if (this.sortByConfig2) {
          this.column = "config2";
          this.order = "asc";
          this.sortByConfig2 = false;
        } else {
          this.column = "config2";
          this.order = "desc";
          this.sortByConfig2 = true;
        }

        this.getPackages(this.currentPage);
        break;
      }

      case 'config3': {
        if (this.sortByConfig3) {
          this.column = "config3";
          this.order = "asc";
          this.sortByConfig3 = false;
        } else {
          this.column = "config3";
          this.order = "desc";
          this.sortByConfig3 = true;
        }

        this.getPackages(this.currentPage);
        break;
      }


      case 'config4': {
        if (this.sortByConfig4) {
          this.column = "config4";
          this.order = "asc";
          this.sortByConfig4 = false;
        } else {
          this.column = "config4";
          this.order = "desc";
          this.sortByConfig4 = true;
        }

        this.getPackages(this.currentPage);
        break;
      }

      default: {
        console.log("default")
      }
    }
  }

  _getdata(page: number) {
    if (page === 0) {
      page = 1;
    }
    if (this.sortFlag) {
      this.getPackages(page);
    }
    else {
      this.campaignService.getPackages(page, this.column, this.order, {}, this.pageLimit).subscribe(data => {
        this.packages = data.body;
        this.totalpage = data.total_pages;
        this.showingpage = data.current_page;
        this.allItems = data.total_key;
        this.countRecords(data);
        this.setPage(page, data, data.total_pages, data.body.length);
        if (data.body == null) {
          this.showingpage = 0;
        }
      }, error => {
        console.log(error);
      })
    }
  }

  editPackage(uuid): void {
    this.loading = true;
    this.campaignService.findByPackageId(uuid).subscribe(data => {
      this.campaignService.editFlag = true;
      this.campaignService.packageDetail = data.body;
      this.loading = false;
      this.router.navigate(['package']);
    }, error => {
      console.log(error)
    });
  };

  createPackage() {
    this.router.navigate(['package']);
  }

  uploadPackages() {
    this.router.navigate(['upload-packages']);
  }


  onGridReady(params) {
    // this.gridOptions.api.setDomLayout('autoHeight');
    this.paramList = params;
    this.gridApi = params.api;
    this.gridColumnApi = params.columnApi;
    let gridSavedState = sessionStorage.getItem("package_list_grid_column_state");
    if (gridSavedState) {
      console.log(gridSavedState);
      console.log("column state found in session storage");
      this.gridColumnApi.setColumnState(JSON.parse(gridSavedState));
      console.log("priting column state after setting");
      console.log(this.gridColumnApi.getColumnState());
    }
    let filterSavedState = sessionStorage.getItem("package_list_grid_filter_state");
    if (filterSavedState) {
      console.log(filterSavedState);
      console.log("filter state found in session storage");
      this.gridApi.setFilterModel(JSON.parse(filterSavedState));
      console.log("priting filter state after setting");
      console.log(this.gridApi.getFilterModel());
    } else {
      console.log("Filter state not found in session storage");
    }

    var datasource = {
      getRows: (params: IGetRowsParams) => {
        this.loading = true;
        this.sortForAgGrid(params.sortModel);
        this.filterForAgGrid(params.filterModel);
        this.campaignService.getPackages(this.gridApi.paginationGetCurrentPage() + 1, this.column, this.order, this.filterValuesJsonObj, this.pageLimit).subscribe(data => {
          this.rowData = data.body;
          this.loading = false;
          params.successCallback(data.body, data.total_key);
          this.total_number_of_records = data.total_key;
          if (this.isFirstTimeLoad == true) {
            let paginationSavedPage = sessionStorage.getItem("package_list_grid_saved_page");
            if (paginationSavedPage && this.isFirstTimeLoad) {
              this.gridOptions.api.paginationGoToPage(Number.parseInt(paginationSavedPage));
              this.isFirstTimeLoad = false;
              this.isPaginationStateRecovered = true;
            }
          }
        }, error => {
          console.log(error);
        })
      }
    }
    this.gridApi.setDatasource(datasource);
  }


  onPaginationChanged(params) {
    if (this.isPaginationStateRecovered || params.api.paginationGetCurrentPage() > 0) {
      sessionStorage.setItem("package_list_grid_saved_page", params.api.paginationGetCurrentPage());
    }
  }

  onDragStopped(params) {
    console.log("Column resize working");
    console.log(params);
    let columnState = this.gridColumnApi.getColumnState();
    this.saveGridStateInSession(columnState);
  }


  onSortChanged(params) {
    console.log("Column Sort working");
    console.log(params);
    let columnState = this.gridColumnApi.getColumnState();
    this.saveGridStateInSession(columnState);
  }

  onFilterChanged(params) {
    console.log("Filter change working");
    let filterModel = params.api.getFilterModel();
    console.log(filterModel);
    sessionStorage.setItem("package_list_grid_filter_state", JSON.stringify(filterModel));
  }

  onColumnVisible(params) {
    console.log("Column Visible working");
    console.log(params);
    let columnState = this.gridColumnApi.getColumnState();
    this.saveGridStateInSession(columnState);
  }

  onColumnPinned(params) {
    console.log("Column Pinned working");
    console.log(params);
    let columnState = this.gridColumnApi.getColumnState();
    this.saveGridStateInSession(columnState);
  }

  onColumnResized(params){
    console.log(params);
    let columnState = this.gridColumnApi.getColumnState();
    this.saveGridStateInSession(columnState);
  }



  onFirstDataRendered(params) {
    console.log("First time data rendered");
    this.isFirstTimeLoad = true;
  }



  saveGridStateInSession(columnState) {
    sessionStorage.setItem("package_list_grid_column_state", JSON.stringify(columnState));
  }

  filterForAgGrid(filterModel) {
    this.gridApi.paginationCurrentPage = 0;
    if (filterModel != undefined) {
      this.filterValuesJsonObj = {};
      this.filterValues = new Map();
      if (filterModel.package_name != undefined && filterModel.package_name.filter != null && filterModel.package_name.filter != undefined && filterModel.package_name.filter != "") {
        this.filterValues.set("packageName", filterModel.package_name.filter);
      }

      if (filterModel.bin_version != undefined && filterModel.bin_version.filter != null && filterModel.bin_version.filter != undefined && filterModel.bin_version.filter != "") {
        this.filterValues.set("binVersion", filterModel.bin_version.filter);
      }

      if (filterModel.app_version != undefined && filterModel.app_version.filter != null && filterModel.app_version.filter != undefined && filterModel.app_version.filter != "") {
        this.filterValues.set("appVersion", filterModel.app_version.filter);
      }

      if (filterModel.mcu_version != undefined && filterModel.mcu_version.filter != null && filterModel.mcu_version.filter != undefined && filterModel.mcu_version.filter != "") {
        this.filterValues.set("mcuVersion", filterModel.mcu_version.filter);
      }

      if (filterModel.ble_version != undefined && filterModel.ble_version.filter != null && filterModel.ble_version.filter != undefined && filterModel.ble_version.filter != "") {
        this.filterValues.set("bleVersion", filterModel.ble_version.filter);
      }

      if (filterModel.config1 != undefined && filterModel.config1.filter != null && filterModel.config1.filter != undefined && filterModel.config1.filter != "") {
        this.filterValues.set("config1", filterModel.config1.filter);
      }

      if (filterModel.config2 != undefined && filterModel.config2.filter != null && filterModel.config2.filter != undefined && filterModel.config2.filter != "") {
        this.filterValues.set("config2", filterModel.config2.filter);
      }

      if (filterModel.config3 != undefined && filterModel.config3.filter != null && filterModel.config3.filter != undefined && filterModel.config3.filter != "") {
        this.filterValues.set("config3", filterModel.config3.filter);
      }

      if (filterModel.config4 != undefined && filterModel.config4.filter != null && filterModel.config4.filter != undefined && filterModel.config4.filter != "") {
        this.filterValues.set("config4", filterModel.config4.filter);
      }

      if (filterModel.created_at != undefined && filterModel.created_at.dateFrom != null && filterModel.created_at.dateFrom != undefined && filterModel.created_at.dateFrom != "") {
        this.filterValues.set("createdAt", filterModel.created_at.dateFrom);
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
    switch (sortModel[0].colId) {
      case 'package_name': {
        this.column = "packageName";
        this.order = sortModel[0].sort;
        break;
      }
      case 'bin_version': {
        this.column = "binVersion";
        this.order = sortModel[0].sort;
        break;
      }
      case 'app_version': {
        this.column = "appVersion";
        this.order = sortModel[0].sort;
        break;
      }
      case 'mcu_version': {
        this.column = "mcuVersion";
        this.order = sortModel[0].sort;
        break;
      }

      case 'ble_version': {
        this.column = "bleVersion";
        this.order = sortModel[0].sort;
        break;
      }

      case 'config1': {
        this.column = "config1";
        this.order = sortModel[0].sort;
        break;
      }

      case 'config2': {
        this.column = "config2";
        this.order = sortModel[0].sort;
        break;
      }

      case 'config3': {
        this.column = "config3";
        this.order = sortModel[0].sort;
        break;
      }
      case 'config4': {
        this.column = "config4";
        this.order = sortModel[0].sort;
        break;
      }
      case 'created_at': {
        this.column = "createdAt";
        this.order = sortModel[0].sort;
        break;
      }

      case 'is_used_in_campaign': {
        this.column = "isUsedInCampaign";
        this.order = sortModel[0].sort;
        break;
      }

      default: {
        console.log("default")
      }
    }
  }

  onBtExport() {
    console.log("Object.entries(this.filterValuesJsonObj).length");
    console.log(Object.entries(this.filterValuesJsonObj).length);
    if((this.filterValuesJsonObj != {} && Object.entries(this.filterValuesJsonObj).length !== 0) || this.column != "packageName" || this.order != "asc") {
      this.campaignService.exportFilterPackageList(this.total_number_of_records, 1,this.column, this.order, this.filterValuesJsonObj).subscribe(data => {
        console.log(data);
      }, error => {
        console.log(error);
      })
    } else {
      this.campaignService.exportPackageList().subscribe(data => {
        console.log(data);
      }, error => {
        console.log(error);
      });
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
        this.column = 'packageName';
        this.order = 'asc';
        params.api.setFilterModel(null);
        params.column.columnApi.applyColumnState({ defaultState: { sort: null } });
        //params.column.columnApi.resetColumnState();
        params.api.onFilterChanged();
        sessionStorage.setItem("package_list_grid_saved_page", null);
        sessionStorage.setItem("package_list_grid_filter_state", null);
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
  onPageSizeChange() {
   sessionStorage.setItem('package_list_page_size',String(this.selected));
   console.log(sessionStorage.getItem('package_list_page_size') +"from onPageSizeChange");
    var value=this.selected;
    this.pageLimit = (Number(value));
    this.gridApi.gridOptionsWrapper.setProperty('cacheBlockSize', value);
    const api: any = this.gridApi;
    api.rowModel.resetCache();
    this.gridApi.paginationSetPageSize(Number(value));
  }
}


var filterParams = {
  values: function (params) {
    setTimeout(function () {
      params.success(['Yes', 'No']);
    }, 1000);
  },
};