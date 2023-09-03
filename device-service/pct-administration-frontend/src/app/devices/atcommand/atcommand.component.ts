import { AgGridAngular } from '@ag-grid-community/angular';
import { ClientSideRowModelModule, ColumnsToolPanelModule, GridOptions, IGetRowsParams, MenuModule, Module, MultiFilterModule, RowGroupingModule } from '@ag-grid-enterprise/all-modules';
import { Component, OnInit, ViewChild } from '@angular/core';
import { FormBuilder, FormControl, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { CustomSuccessToastrComponent } from 'src/app/custom-success-toastr/custom-success-toastr.component';
import { ATCRequest } from 'src/app/models/ATCRequest';
import { CustomDateComponent } from 'src/app/ui-elements/custom-date-component.component';
import { CustomHeader } from 'src/app/ui-elements/custom-header.component';
import { PagerService } from 'src/app/util/pagerService';
import { PaginationConstants } from 'src/app/util/paginationConstants';
import { DevicesListService } from '../devices-list/devices-list.service';
import { AtcommandService } from './atcommand.service';
import { BtnCellRendererForATCommand } from './btn-cell-renderer.component';

@Component({
  selector: 'app-atcommand',
  templateUrl: './atcommand.component.html',
  styleUrls: ['./atcommand.component.css']
})
export class AtcommandComponent implements OnInit {

  sortFlag: boolean = false;
  currentPage: any;
  showingpage: any;
  totalpage: any;
  gatewayList: any[] = [];
  totalRecords = 0;
  currentEndRecord = 0;
  currentStartRecord = 0;
  pagedItems: any[] =[];
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
  column = 'created_on';
  order = 'asc';
  userCompanyType: any;
  isSuperAdmin: any;
  isRoleCustomerManager: any;
  //parameters for counting records
  filterValues = new Map();
  filterValuesJsonObj: any = {};
  isClearButtonVisible = false;
  loading = false;
  frameworkComponents: any;
  public sideBar : any;
  
  //aggrid code
  //aggrid code

  @ViewChild('myGrid')
  myGrid!: AgGridAngular;
   public gridOptions:any;
   public gridApi:any;
  public columnDefs;
  public gridColumnApi!: { setColumnState: (arg0: any) => void; getColumnState: () => any; };
  public cacheOverflowSize;
  public maxConcurrentDatasourceRequests;
  public infiniteInitialRowCount;
  public defaultColDef :any;
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
    selectedtoDateRange  : any[] = [];
    selected!: number;
    selectedIdsToDelete: string[] = [];
    isChecked: boolean = false;
    
    // formcontrol def
  atcommadform: any;
  count: number = 0 ;
  gateway_id: any;
  at_command: any;
  priority: any;
  submitted: boolean= false;
  gatewayListService: any;
  atcResponse: any;
  atcRequest: ATCRequest = new ATCRequest;

  
    get f() { return this.atcommadform.controls; }
  constructor(private deviceListService: DevicesListService,private _formBuldier: FormBuilder,public atcommandService :AtcommandService, public router: Router, private toastr: ToastrService, private pagerService: PagerService) { this.columnDefs = createNormalColDefs();
  
    var filterParamsUsers = {
      values: (params: { success: (arg0: String[]) => any; }) => params.success(this.getFilterValuesData())
    };

    this.cacheOverflowSize = 2;
    this.maxConcurrentDatasourceRequests = 2;
    this.infiniteInitialRowCount = 2;
    
    this.gridOptions = {
      cacheBlockSize: this.pageLimit,
      paginationPageSize: this.pageLimit,
      rowModelType: 'infinite',
    }
    this.frameworkComponents = {
      btnCellRenderer: BtnCellRendererForATCommand,
      agDateInput: CustomDateComponent,
      agColumnHeader: CustomHeader
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

  ngOnInit() {
    this.atcommandService.isAddATCommand =false;
    this.loading = true;
    this.initializeForm();
  }

  initializeForm() {
    console.log("Initialise Form")
          console.log(this.atcommadform)
          this.count=0;
              this.atcommadform = this._formBuldier.group({
                  gateway_id:  ['', Validators.compose([Validators.required])],
                  at_command:  ['', Validators.compose([Validators.required])],
                  priority:  ['', Validators.compose([Validators.required])],
              });
  }
  onSubmit(atcommadform:any) {
    console.log("On Form Submit")
    console.log(this.atcommadform)
    this.submitted = true; 
    this.initializeForm(); 
    this.getFormControlValues(atcommadform); 
    this.sendATCRequest();    

    }
    sendATCRequest() {
    this.atcommandService.sendATCRequest(this.atcRequest).subscribe(
        data => {
          this.toastr.success("ATC Request sent successfully", "", {toastComponent:CustomSuccessToastrComponent});
          this.atcResponse= data.body;
          this.atcommandService.isAddATCommand = false;
        },
        error => {
            console.log(error)
        }
    );
      }
  
  getFormControlValues(atcommadform :any) {
    this.atcRequest= new ATCRequest();
    this.atcRequest.gateway_id = this.deviceListService.deviceId;
    this.atcRequest.at_command = atcommadform.controls.at_command.value;   
    this.atcRequest.priority = atcommadform.controls.priority.value;   
    }
  onPageSizeChange() {
    sessionStorage.setItem('gatewayListsize', String(this.selected));
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


  onFirstDataRendered(params: any) {
    console.log("First time data rendered");
    this.isFirstTimeLoad = true;
  }
  
  headerHeightSetter(event: any) {
    var padding = 20;
    var height = headerHeightGetter() + padding;
    this.gridApi.setHeaderHeight(height);
    this.gridApi.resetRowHeights();
  }

  onGridReady(params: { api: any; columnApi: any; }) {
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
        console.log("ATCommand DeviceId")
        console.log(this.deviceListService.deviceId)
        this.atcommandService.getATCQueueRequest(this.deviceListService.deviceId,this.gridApi.paginationGetCurrentPage() + 1, this.column, this.order, this.pageLimit,).subscribe((data: { body: { content: any; }; total_key: number; }) => {
            this.loading = false;
            this.rowData = data.body.content;
            console.log(this.rowData);
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
          (            error: any) => {
              console.log(error);
            })
      }
    }
    this.gridApi.setDatasource(datasource);
  }

  getFilterValuesData() {
    return this.userNameList;
  }
  filterForAgGrid(filterModel: { created_epoch: { filter: string | null | undefined; } | undefined; at_command: { filter: string | null; } | undefined; cellular: { filter: undefined; }; priority: { filter: string | null | undefined; } | undefined; } | undefined) {
    this.gridApi.paginationCurrentPage = 0;
    if (filterModel != undefined) {


      this.filterValuesJsonObj = {};
      this.filterValues = new Map();
      if (filterModel.created_epoch != undefined && filterModel.created_epoch.filter != null && filterModel.created_epoch.filter != undefined && filterModel.created_epoch.filter != "") {
        this.filterValues.set("created_epoch", filterModel.created_epoch.filter);
        
      }
      if (filterModel.at_command != undefined && filterModel.at_command.filter != null && filterModel.cellular.filter != undefined && filterModel.at_command.filter != "") {
        this.filterValues.set("cellular", filterModel.at_command.filter);
      }
      if (filterModel.priority != undefined && filterModel.priority.filter != null && filterModel.priority.filter != undefined && filterModel.priority.filter != "") {
        this.filterValues.set("priority", filterModel.priority.filter);
      }
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
      case 'created_epoch': {

        this.column = "createdEpoch";
        this.order = sortModel[0].sort;
        break;
      }
      case 'at_command': {

        this.column = "at_command";
        this.order = sortModel[0].sort;
        break;
      }
      case 'priority': {

        this.column = "priority";
        this.order = sortModel[0].sort;
        break;
      }
      default: {
        console.log("default")
      }
    }
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
  saveGridStateInSession(columnState: any) {
    throw new Error('Method not implemented.');
  }
  
  
  
  onPaginationChanged(params: { api: { paginationGetCurrentPage:any}}) {
    this.isChecked = false;
    this.gridApi.forEachNode(function (node: { setSelected: (arg0: boolean) => void; }) {
      node.setSelected(false);
    });
    this.selectedIdsToDelete = [];
    if (this.isPaginationStateRecovered || params.api.paginationGetCurrentPage() > 0) {
      sessionStorage.setItem("gateway_list_grid_saved_page", params.api.paginationGetCurrentPage());
    }
}

addATCommand(event: any) {
  alert("button")
  this.atcommandService.isAddATCommand= true;
}

onCellClicked(params: any) {
  console.log("Inside OnCellClicked")
  if (params.column.colId == "imei") {
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


 function createNormalColDefs() {
    return [
      {
        headerName: 'Created On', field: 'created_epoch', sortable: true, filter: 'agTextColumnFilter', floatingFilter: true, filterParams: containsFilterParams,
        cellRenderer: function (params: { data: { created_epoch: string; } | undefined; }) {
          if(params.data != undefined ){
          
         return '<div class="pointer" style="color:#4ba9d7;"><a><b>'+params.data.created_epoch+'<b></a></div>'
        
        }}
      },
      {
        headerName: 'ATCommand', field: 'at_command', sortable: true, filter: 'agTextColumnFilter', floatingFilter: true, filterParams: containsFilterParams,
        cellRenderer: function (params: { data: { at_command: string; } | undefined; }) {
          if(params.data != undefined ){
         return '<div>'+params.data.at_command +'</div>'
        }}
      },
      {
        headerName: 'Priority', field: 'priority', sortable: true, filter: 'agTextColumnFilter', filterParams: containsFilterParams,
        cellRenderer: function (params: { data: { priority: string; } | undefined; }) {
          if(params.data != undefined ){
         return '<div>'+params.data.priority+'</div>'
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

function atcommandRequest(atcommandRequest: any) {
  throw new Error('Function not implemented.');
}