import { Component, OnInit, ViewChild } from '@angular/core';
import { Router } from '@angular/router';
import { DevicesListService } from './devices-list.service';
import { PagerService } from 'src/app/util/pagerService';
import { ToastrService } from 'ngx-toastr';
import { PaginationConstants } from 'src/app/util/paginationConstants';
import { AgGridAngular } from '@ag-grid-community/angular';
import { ClientSideRowModelModule, ColumnsToolPanelModule, GridApi, GridOptions, IGetRowsParams, MenuModule, Module, MultiFilterModule, RowGroupingModule } from '@ag-grid-enterprise/all-modules';
import { FormControl } from '@angular/forms';
import { BtnCellRendererForGatewayDetails } from './btn-cell-renderer.component';

import * as moment from 'moment';
import '@ag-grid-community/core/dist/styles/ag-grid.css';
import '@ag-grid-community/core/dist/styles/ag-theme-alpine.css';
declare var $: any;
import { SetFilterModule } from '@ag-grid-enterprise/set-filter';
import { CustomDateComponent } from 'src/app/ui-elements/custom-date-component.component';
import { CustomHeader } from 'src/app/ui-elements/custom-header.component';
import { CustomErrorToastrComponent } from 'src/app/custom-error-toastr/custom-error-toastr.component';
import { CustomSuccessToastrComponent } from 'src/app/custom-success-toastr/custom-success-toastr.component';

@Component({
  selector: 'app-devices-list',
  templateUrl: './devices-list.component.html',
  styleUrls: ['./devices-list.component.css']
})
export class DevicesListComponent implements OnInit {
  sortFlag: boolean = false;
  currentPage: any;
  showingpage : number =0;
  totalpage : string ="";
  gatewayList: any[] = [];
  totalRecords = 0;
  currentEndRecord = 0;
  currentStartRecord = 0;
  pagedItems!: any[];
  private allItems: any;
  pageLimit = 25;
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
  userCompanyType!: string | null;
  isSuperAdmin!: string | null;
  isRoleCustomerManager!: string | null;

  //parameters for counting records
  filterValues = new Map();
  filterValuesJsonObj: any = {};
  isClearButtonVisible = false;
  loading = false;
  frameworkComponents: any;
  public sideBar : any;


   // ag-grid code
  @ViewChild('myGrid')
  myGrid!: AgGridAngular;
   public gridOptions:any;
   public gridApi:any;
   public columnDefs;
   public gridColumnApi!: { setColumnState: (arg0: any) => void; getColumnState: () => any; };
   public cacheOverflowSize;
   public maxConcurrentDatasourceRequests;
   public infiniteInitialRowCount;
   public defaultColDef : any;
   public paramList: any;
   public components : any;
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
  selectedtoDateRange:any[] = [];
  selected: number =0;
  selectedIdsToDelete: string[] = [];
  isChecked: boolean = false;
  deviceList: any;

  constructor(public devicesListService: DevicesListService, public router: Router, private toastr: ToastrService, private pagerService: PagerService) { 
  var filterParamsUsers = {
      values: (params: { success: (arg0: String[]) => any; }) => params.success(this.getFilterValuesData())
    };

    var filterParams1 = {

      filterOptions: [
        'equals', 'lessThan', 'greaterThan'
      ],
      defaultOption: 'lessThan',
      suppressAndOrCondition: true,
    };

    this.columnDefs = createNormalColDefs();
    this.cacheOverflowSize = 2;
    this.maxConcurrentDatasourceRequests = 2;
    this.infiniteInitialRowCount = 2;
    if (sessionStorage.getItem('deviceListsize') != undefined) {
      this.pageLimit = Number(sessionStorage.getItem('deviceListsize'));
    } else {
      this.pageLimit = 25;
    }
    this.gridOptions = {
      cacheBlockSize: this.pageLimit,
      paginationPageSize: this.pageLimit,
      rowModelType: 'infinite',
    }
    this.frameworkComponents = {
      btnCellRenderer: BtnCellRendererForGatewayDetails,
      agDateInput: CustomDateComponent,
      agColumnHeader: CustomHeader,
    }

    
    this.defaultColDef = {
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
  ngOnInit(){
    this.userCompanyType = sessionStorage.getItem('type');
    this.isSuperAdmin = sessionStorage.getItem('IS_ROLE_SUPERADMIN');
    this.userCompanyType = sessionStorage.getItem('type');
    this.isSuperAdmin = sessionStorage.getItem('IS_ROLE_SUPERADMIN');
    if(this.isSuperAdmin === 'true'){
      this.router.navigate(['device-list']);
    }else{
      this.router.navigate(['customerAssets']);
    }
    console.log("isSuperAdmin " + this.isSuperAdmin);
    this.isRoleCustomerManager = sessionStorage.getItem('IS_ROLE_CUSTOMER_MANAGER');
    console.log("isRoleCustomerManager " + this.isRoleCustomerManager);

    if (sessionStorage.getItem('deviceListsize') != undefined) {
      this.pageLimit = Number(sessionStorage.getItem('deviceListsize'));
    }
    else {
      this.pageLimit = 25;
    }
    this.sortFlag = false;
  }

  onPageSizeChange() {
  
    sessionStorage.setItem('deviceListsize', String(this.selected));
    var value = this.selected;
    this.pageLimit = (Number(value));
    this.gridApi.gridOptionsWrapper.setProperty('cacheBlockSize', value);
    const api: any = this.gridApi;
    api.rowModel.resetCache();
    this.gridApi.paginationSetPageSize(Number(value));
  }

  getSelectedRowData() {
    let selectedNodes = this.gridApi.getSelectedNodes();
    let selectedData = selectedNodes.map((node: { data: any; }) => node.data);
    return selectedData;
  }

  reloadComponent() {
    let currentUrl = this.router.url;
    this.router.navigateByUrl('/', { skipLocationChange: true }).then(() =>
      this.router.navigate([currentUrl]));
  }

  getSelctedRow(action: string) {

    this.gatewayList = [];
    this.selectedIdsToDelete = [];
    this.gatewayList = this.getSelectedRowData();
    this.devicesListService.deviceList = [];

    if (action == 'delete') {
      for (let i = 0; i < this.gatewayList.length; i++) {
        this.gatewayList[i].is_selected = true;
        if (this.gatewayList[i].status != "Pending") {
          this.toastr.error("The following records are part of an installation, and so they cannot be deleted.", "Problem with Deletion", { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
          return;
        } else {
          this.devicesListService.deviceList.push(this.deviceList[i]);
        }
      }
      // if (this.devicesListService.deviceList.length > 1) {
      //   $('#deleteGatewaysModal').modal('show');

      // } else if (this.devicesListService.deviceList.length == 1) {
      //   $('#deleteGatewayModal').modal('show');
      // }

      console.log(this.devicesListService.deviceList);
    } else if (action == 'update') {
      for (let i = 0; i < this.deviceList.length; i++) {
        this.deviceList[i].is_selected = true;
        if (this.deviceList[i].gateway_type != "Gateway") {
          this.toastr.error("The selected device(s) do not support sensors.", "Device Does Not Support Sensors ", { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
          return;
        } else {
          if (this.deviceList[i].status != "Pending") {
            this.toastr.error("The selected device(s) are part of an in-progress or active installation, and cannot be modified.", "Device Configuration Can’t Be Modified", { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
            return;
          } else {
            this.devicesListService.deviceList.push(this.deviceList[i]);
          }
        }
      }
      console.log(this.devicesListService.deviceList);
      this.router.navigate(['update-gateways']);
    }

  }
  onGridReady(params: any) {
    this.gridOptions.api.setDomLayout('autoHeight');
    this.paramList = params;
    console.log(this.paramList)
    this.gridApi = params.api;
    this.gridApi.setRowData([]);
    console.log(this.gridApi);
    this.gridColumnApi = params.columnApi;
    let gridSavedState = sessionStorage.getItem("gateway_list_grid_column_state");
    if (gridSavedState) {
      console.log(gridSavedState);
      console.log("column state found in session storage");
      this.gridColumnApi.setColumnState(JSON.parse(gridSavedState));
      console.log("printing column state after setting");
      console.log(this.gridColumnApi.getColumnState());
    }
    let filterSavedState = sessionStorage.getItem("gateway_list_grid_filter_state");
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
        //this.companyname=this.selectedCompany;
        this.sortForAgGrid(params.sortModel);
        this.filterForAgGrid(params.filterModel);
        console.log(this.filterModelCountFilter);
        console.log(this.gridApi.paginationGetCurrentPage());
        //  this.devicesListService.getGatewayListWithSort(this.gridApi.paginationGetCurrentPage() + 1, this.column, this.order, this.pageLimit
        //  ,this.filterValuesJsonObj,this.filterModelCountFilter).subscribe(data => {
        this.devicesListService.getGatewayListSortwithCompanyPage(this.gridApi.paginationGetCurrentPage() + 1, this.column, this.order, this.pageLimit,
          this.filterValuesJsonObj).subscribe(data => {
            this.loading = false;
            this.rowData = data.body.content;
            this.total_number_of_records = data.total_key;
            params.successCallback(this.rowData, data.total_key);
            if (this.isFirstTimeLoad == true) {
              let paginationSavedPage = sessionStorage.getItem("gateway_list_grid_saved_page");
              if (paginationSavedPage && this.isFirstTimeLoad) {
                this.gridOptions.api.paginationGoToPage(Number.parseInt(paginationSavedPage));
                this.isFirstTimeLoad = false;
                this.isPaginationStateRecovered = true;
              }
            }
          },
            error => {
              console.log(error);
            })
      }
    }
    this.gridApi.setDatasource(datasource);
  }

  onDragStopped(params: any) {
    console.log("Column resize working");
    console.log(params);
    let columnState = this.gridColumnApi.getColumnState();
    this.saveGridStateInSession(columnState);
  }
  onColumnResized(params: any) {
    console.log(params);
    let columnState = this.gridColumnApi.getColumnState();
    this.saveGridStateInSession(columnState);
  }
  onSortChanged(params: any) {
    console.log("Column Sort working");
    console.log(params);
    let columnState = this.gridColumnApi.getColumnState();
    this.saveGridStateInSession(columnState);
  }

  onFilterChanged(params: { api: { getFilterModel: () => any; }; }) {
    console.log("Filter change working");
    let filterModel = params.api.getFilterModel();
    console.log(filterModel);
    sessionStorage.setItem("gateway_list_grid_filter_state", JSON.stringify(filterModel));
  }

  onColumnVisible(params: any) {
    console.log("Column Visible working");
    console.log(params);
    let columnState = this.gridColumnApi.getColumnState();
    this.saveGridStateInSession(columnState);
  }

  onColumnPinned(params: any) {
    console.log("Column Pinned working");
    console.log(params);
    let columnState = this.gridColumnApi.getColumnState();
    this.saveGridStateInSession(columnState);
  }

  onPaginationChanged(params: { api: { paginationGetCurrentPage: () => any; }; }) {
    this.isChecked = false;
    this.gridApi.forEachNode(function (node: { setSelected: (arg0: boolean) => void; }) {
      node.setSelected(false);
    });
    this.selectedIdsToDelete = [];
    if (this.isPaginationStateRecovered || params.api.paginationGetCurrentPage() > 0) {
      sessionStorage.setItem("gateway_list_grid_saved_page", params.api.paginationGetCurrentPage());
    }
  }

  exportDeviceList() {
    // this.loading = true;
    // this.devicesListService.exportFilterDeviceList(this.total_number_of_records, 1, this.column, this.order, this.filterValuesJsonObj).subscribe((data: any) => {
    //   this.loading = false;
    // }, (error: any) => {
    //   this.loading = false;
    // })

  }

  deleteAllDevices() {
    this.selectedIdsToDelete = [];
    if (this.devicesListService.deviceList.length < 1) {
      this.toastr.error("Please select at least one device", "No Device Selected", { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
      return;
    }

    for (var i = 0; i < this.devicesListService.deviceList.length; i++) {
      this.selectedIdsToDelete.push(this.devicesListService.deviceList[i].uuid);
    }
    this.devicesListService.bulkDeleteForGateways(this.selectedIdsToDelete).subscribe((data: { status: any; message: string | undefined; }) => {

      if (data.status) {
        this.selectedIdsToDelete = [];
        this.devicesListService.deviceList = [];
        this.toastr.success(data.message, "", { toastComponent: CustomSuccessToastrComponent });
        this.reloadComponent();
      } else {
        this.toastr.error("Something went wrong during deleting device", "Device Problem Notification", { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
        this.reloadComponent();
      }
      this.isChecked = false;
    }, (error: any) => {
      console.log(error);
      this.reloadComponent();
    });

  }


  resetGateway() {
    if (this.devicesListService.deviceList.length < 1) {
      this.toastr.error("Please select at least one device", "No Device Selected", { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
      return;
    }
    this.loading=true;
    // this.resetInstalationStatusservice.resetInstalationStatus(null, this.devicesListService.deviceList[0].imei ,this.devicesListService.deviceList[0].account_number).subscribe((data: { status: any; message: string | undefined; }) => {
    //   console.log(data);
    //   if (data.status) {
    //     this.devicesListService.deviceList = [];
    //     this.toastr.success(data.message, null, { toastComponent: CustomSuccessToastrComponent });
    //     this.reloadComponent();
    //   } else {
    //     this.toastr.error(data.message,null,{toastComponent: CustomErrorToastrComponent, tapToDismiss:true, disableTimeOut:true});
    //   }
    //     this.loading=false;
    // }, (error: any) => {
    //   console.log(error);
    //   this.loading=false;
    // }
    // );
  }

  onFirstDataRendered(params: any) {
    console.log("First time data rendered");
    this.isFirstTimeLoad = true;
  }

  saveGridStateInSession(columnState: any) {
    sessionStorage.setItem("gateway_list_grid_column_state", JSON.stringify(columnState));
  }
  getGateway(currentPage: any) {
    this.devicesListService.getGatewayDetails(currentPage).subscribe(
      (      data) => {
        console.log(data);
        this.deviceList = data.body.content;
        this.totalpage = data.total_pages;
        console.log("Total no. of records" + this.totalpage) //total no.of records
        this.showingpage = data.currentPage + 1;
        this.allItems = data.totalKey;
        this.countRecords(data);
        console.log("The count Record" + this.countRecords(data));
        this.setPage(1, data.body, data.total_pages, data.body.length);
        if (data.body.content == null) {
          this.showingpage = 0;
        }
        console.log(this.gatewayList)
      },
      (      error: any) => { throw error },
      () => console.log("finished")
    );
  }

  countRecords(data: { body: { content: string | any[] | null; }; total_key: number; current_page: number; }) {
    //if (data.body.content === null || data.body.content.length === 0) 
    if (data.body.content == null || data.body.content.length === 0) {
      this.totalRecords = 0;
      this.currentStartRecord = 0;
      this.currentEndRecord = 0;
    } else {
      this.totalRecords = data.total_key;
      this.currentStartRecord = (this.pageLimit * (data.current_page)) + 1;
      if (this.pageLimit == data.body.content.length) {
        this.currentEndRecord = this.pageLimit * (data.current_page + 1);
      }
      else {
        this.currentEndRecord = this.currentStartRecord + data.body.content.length - 1;
      }
    }
  }

  setPage(page: number, body: any[], total_pages: any, pageItems: number | undefined) {
    // get pager object from service
    this.pager = this.pagerService.getPager(this.allItems, page, total_pages, pageItems);

    // get current page of items
    this.pagedItems = body;
  }

  uploadgateways() {
    this.router.navigate(['upload-devices']);
  }

  filterForAgGrid(filterModel: { imei: { filter: string | null | undefined; } | undefined; cellular: { filter: string | null | undefined; } | undefined; organisation_name: { filter: string | null | undefined; } | undefined; created_on: { filter: string | null | undefined; } | undefined; qa_status: { filter: string | null | undefined; } | undefined; usage_status: { filter: string | null | undefined; } | undefined; service_country: { filter: string | null | undefined; } | undefined; service_network: { filter: string | null | undefined; } | undefined; } | undefined) {
    this.gridApi.paginationCurrentPage = 0;
    if (filterModel != undefined) {


      this.filterValuesJsonObj = {};
      this.filterValues = new Map();

      // if (filterModel.product_name != undefined && filterModel.product_name.filter != null && filterModel.product_name.filter != undefined && filterModel.product_name.filter != "") {
      //   this.filterValues.set("productName", filterModel.product_name.filter);
      // }      
      if (filterModel.imei != undefined && filterModel.imei.filter != null && filterModel.imei.filter != undefined && filterModel.imei.filter != "") {
        this.filterValues.set("imei", filterModel.imei.filter);
      }
      if (filterModel.cellular != undefined && filterModel.cellular.filter != null && filterModel.cellular.filter != undefined && filterModel.cellular.filter != "") {
        this.filterValues.set("cellular", filterModel.cellular.filter);
      }
      if (filterModel.organisation_name != undefined && filterModel.organisation_name.filter != null && filterModel.organisation_name.filter != undefined && filterModel.organisation_name.filter != "") {
        this.filterValues.set("organisationName", filterModel.organisation_name.filter);
      }
      if (filterModel.created_on != undefined && filterModel.created_on.filter != null && filterModel.created_on.filter != undefined && filterModel.created_on.filter != "") {
        this.filterValues.set("created_on", filterModel.created_on.filter);
      }

      if (filterModel.qa_status != undefined && filterModel.qa_status.filter != null && filterModel.qa_status.filter != undefined && filterModel.qa_status.filter != "") {
        this.filterValues.set("qaStatus", filterModel.qa_status.filter);
      }
      if (filterModel.usage_status != undefined && filterModel.usage_status.filter != null && filterModel.usage_status.filter != undefined && filterModel.usage_status.filter != "") {
        this.filterValues.set("usageStatus", filterModel.usage_status.filter);
      }

      if (filterModel.service_country != undefined && filterModel.service_country.filter != null && filterModel.service_country.filter != undefined && filterModel.service_country.filter != "") {
        this.filterValues.set("serviceCountry", filterModel.service_country.filter);
      }
      if (filterModel.service_network != undefined && filterModel.service_network.filter != null && filterModel.service_network.filter != undefined && filterModel.service_network.filter != "") {
        this.filterValues.set("serviceNetwork", filterModel.service_network.filter);
      }

      // if (filterModel.status != undefined && filterModel.status.values != undefined && filterModel.status.values != null) {
      //   if (filterModel.status.values.length == 0) {
      //     this.filterValues.set("status", "null");
      //   } else {
      //     this.filterValues.set("status", filterModel.status.values.toString());
      //   }
      // }


      let jsonObject :any  = {};
      this.filterValues.forEach((filter, key) => {
        jsonObject[key] = filter
      });
      this.filterValuesJsonObj = jsonObject;
    }
  }
  sortForAgGrid(sortModel:any) {

    this.gridApi.paginationCurrentPage = 0;
    if (sortModel[0] == undefined || sortModel[0] == null || sortModel[0].colId == undefined || sortModel[0].colId == null) {
      return;
    }
    this.sortFlag = true;
    switch (sortModel[0].colId) {
      case 'imei': {

        this.column = "imei";
        this.order = sortModel[0].sort;
        break;
      }
      case 'cellular': {

        this.column = "cellular.cellular";
        this.order = sortModel[0].sort;
        break;
      }
      case 'organisation_name': {

        this.column = "organisation.organisationName";
        this.order = sortModel[0].sort;
        break;
      }
      case 'created_on': {

        this.column = "createdOn";
        this.order = sortModel[0].sort;
        break;
      }

      case 'qa_status': {
        this.column = "qaStatus";
        this.order = sortModel[0].sort;
        break;
      }
      case 'usage_status': {

        this.column = "usageStatus";
        this.order = sortModel[0].sort;
        break;
      }

      case 'service_country': {
        this.column = "cellular.serviceCountry";
        this.order = sortModel[0].sort;
      break;
      }

     case 'service_network': {
        this.column = "cellular.serviceNetwork";
        this.order = sortModel[0].sort;
        break;
      }
      default: {
        console.log("default")
      }
    }
  }

  onCellClicked(params:any) {
    console.log("Inside Oncell clicked");
    if (params.column.colId == "imei") {
      this.devicesListService.deviceId = params.data.imei;
      if(this.devicesListService.deviceId != null){
        console.log("Inside IMEI");
        this.router.navigate(['device-profile']);
      }
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
      var that = this;
      function onClearFilterButtonClicked(this: any) {
        this.column = 'imei';
        this.order = 'asc';
        params.api.setFilterModel(null);
        // params.column.columnApi.applyColumnState({ defaultState: { sort: null } });
        // params.column.columnApi.resetColumnState();
        params.api.onFilterChanged();
        sessionStorage.setItem("custom_gateway_list_grid_filter_state", "");
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

  getFilterValuesData() {
    return this.userNameList;
  }

  // getAllcompanies() {
  //   var type: string[];
  //   type = ["CUSTOMER"]
  //   this.companiesService.getAllCompany(type, true).subscribe(
  //     data => {
  //       this.companies = [];
  //       if (data != undefined && data != null
  //         && data.body != undefined && data.body != null
  //         && data.body.content != undefined && data.body.content != null
  //         && data.body.content.length > 0) {
  //         for (var i = 0; i < data.body.content.length; i++) {
  //           console.log(data.body.content[i].customer);
  //           this.companies.push(data.body.content[i].customer)
  //         }
  //       }
  //       // this.companyForm.controls['company'].patchValue(new Array(this.companies[this.companies.findIndex(com => com.companyName === this.company)]));

  //     },
  //     error => {
  //       console.log(error)
  //     }

  //   );
  // }

  // onCompanySelect(event) {
  //   console.log(event.value);
  //   this.company = event.value;
  //   sessionStorage.setItem("selectedCompany", JSON.stringify(this.company));
  //   this.companyname = event.value;
  //   this.days = 7;
  //   this.onGridReady(this.paramList);
  // }

  onDateRangeSelect(event: { value: number; }) {
    this.days = event.value;
    sessionStorage.setItem("selectedDays", JSON.stringify(this.days));
    this.onGridReady(this.paramList);
  }

  headerHeightSetter(event: any) {
    var padding = 20;
    var height = headerHeightGetter() + padding;
    this.gridApi.setHeaderHeight(height);
    this.gridApi.resetRowHeights();
  }



}

function headerHeightGetter() {
  var columnHeaderTexts = document.querySelectorAll('.ag-header-cell-text');

  var columnHeaderTextsArray: Element[] = [];

  columnHeaderTexts.forEach(node => columnHeaderTextsArray.push(node));

  var clientHeights = columnHeaderTextsArray.map(
    headerText => headerText.clientHeight
  );
  var tallestHeaderTextHeight = Math.max(...clientHeights);
  return tallestHeaderTextHeight;
}

var filterParamsProductType = {
  values: function (params: { success: (arg0: string[]) => void; }) {
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
        'Smart7 lid']);
    }, 2000);
  },
};

var filterParams = {
  values: function (params: any) {
    setTimeout(function () {
      //params.success(['Not Started', 'In Progress', 'Paused', 'Stopped', 'Completed']);
      params.success(['Pending', 'Install in progress', 'Installed', 'Active', 'Inactive', 'Deleted', 'Error', 'Problem']);
    }, 2000);
  },
};

var filterParamsForType = {
  values: function (params:any) {
    setTimeout(function () {
      //params.success(['Not Started', 'In Progress', 'Paused', 'Stopped', 'Completed']);
      params.success(['Gateway', 'Beacon']);
    }, 2000);
  },
};

function createNormalColDefs() {
  return [
    {
      

      headerName: 'IMEI', field: 'imei', sortable: true, filter: 'agTextColumnFilter', floatingFilter: true, filterParams: containsFilterParams,
      cellRenderer: function (params: any) {
        if(params.data != undefined && params.data.imei!=null){
     return '<div class="pointer" style="color:#4ba9d7;"><a><b>'+params.data.imei+'<b></a></div>'
      }}
    },
    {
      headerName: 'Model', field: 'cellular', sortable: true, filter: 'agTextColumnFilter', floatingFilter: true, filterParams: containsFilterParams,
      cellRenderer: function (params:any) {
        if(params.data != undefined && params.data.cellular_payload.cellular!=null){
       return '<div>'+params.data.cellular_payload.cellular+'</div>'
      }}
    },
    {
      headerName: 'Last Report', field: 'created_on', hide:false, sortable: true, filter: 'agTextColumnFilter', filterParams: containsFilterParams,
      cellRenderer: function (params:any) {
        if(params.data != undefined && params.data.last_report_date!=null){
       return '<div>'+params.data.last_report_date+'</div>'
      }}
    },
    {
      headerName: 'Customer', field: 'created_by', sortable: true, filter: 'agTextColumnFilter', filterParams: containsFilterParams,
      cellRenderer: function (params:any) {
        if(params.data != undefined && params.data.created_by!=null){
       return '<div>'+params.data.created_by+'</div>'
      }}
    },
    {
      headerName: 'QA Status', field: 'qa_status', sortable: true, filter: 'agTextColumnFilter', width: 180,filterParams: containsFilterParams, floatingFilter: true,
      cellRenderer: function (params:any) {
        if(params.data != undefined  && params.data.qa_status!=null){
       return '<div>'+params.data.qa_status+'</div>'
      }}
    },
    {
      headerName: 'Usage', field: 'usage_status',  sortable: true, filter: 'agTextColumnFilter',width: 180, floatingFilter: true, filterParams: containsFilterParams,
      cellRenderer: function (params:any) {
        if(params.data != undefined  && params.data.usage_status!=null ){
       return '<div>'+params.data.usage_status+'</div>'
      }}
    },
    {
      headerName: 'Service Country', field: 'service_country', sortable: true, filter: 'agTextColumnFilter', width: 240,floatingFilter: true, filterParams: containsFilterParams,     
      cellRenderer: function (params:any) {
        if(params.data != undefined  && params.data.cellular_payload.service_country!=null ){
       return '<div>'+params.data.cellular_payload.service_country+'</div>'
      }}
    },
    {
      headerName: 'Service Network', field: 'service_network',  sortable: true, width: 260,filter: 'agTextColumnFilter', floatingFilter: true, filterParams: containsFilterParams,
      cellRenderer: function (params:any) {
        if(params.data != undefined  && params.data.cellular_payload.service_network!=null ){
       return '<div>'+params.data.cellular_payload.service_network+'</div>'
      }}
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

var containsFilterParams = {
  filterOptions: [
    'contains',
  ],
  suppressAndOrCondition: true
};

