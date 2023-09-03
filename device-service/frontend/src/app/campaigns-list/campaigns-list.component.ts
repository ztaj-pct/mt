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
import '@ag-grid-community/core/dist/styles/ag-grid.css';
import '@ag-grid-community/core/dist/styles/ag-theme-alpine.css';
import { Module } from '@ag-grid-community/core';
import { MultiFilterModule } from '@ag-grid-enterprise/multi-filter';
import { SetFilterModule } from '@ag-grid-enterprise/set-filter';
import { BtnCellRendererForCampaign } from './btn-cell-renderer.component';
import * as moment from 'moment-timezone';
import { CustomSuccessToastrComponent } from '../custom-success-toastr/custom-success-toastr.component';
import { CustomCampaignsHeader } from '../ui-elements/custom-campaigns-header.component';
import { AssetsService } from '../assets.service';
import { PaginationConstants } from '../util/paginationConstants';
import { DeviceHistoryDetailsDialogComponent } from '../add-campaign/device-history-details-dialog.component';
import { MatDialog, MatDialogRef } from '@angular/material';
import { CustomErrorToastrComponent } from '../custom-error-toastr/custom-error-toastr.component';
import { CustomTooltip } from '../ui-elements/custom-tooltip.component';
import { CustomDateComponent } from '../ui-elements/custom-date-component.component';

declare var $: any;

@Component({
  selector: 'app-campaigns-list',
  templateUrl: './campaigns-list.component.html',
  styleUrls: ['./campaigns-list.component.css']
})
export class CampaignsListComponent implements OnInit {

  currentPage: any;
  pager: any = {};
  pagedItems: any[];
  public allItems: any;
  totalpage;
  showingpage;
  page = 0;
  loading = false;
  userCompanyType;
  isSuperAdmin;
  selected:number;
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
  column = "campaignName";
  order = "asc";

  pageSizeOptions: PaginationConstants = new PaginationConstants();
  pageSizeObjArr=this.pageSizeOptions.pageSizeObjArr;
  pageSizeOptionsArr: number[];
  //parameters for counting records
  pageLimit = 10;
  //pageLimit = 25;
  totalRecords = 0;
  currentEndRecord = 0;
  currentStartRecord = 0;
  packageUuid;
  Campaigns: any[]
 //  private frameworkComponents;

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
  tooltipShowDelay;
  //Pagination Save State
  isFirstTimeLoad = false;
  isPaginationStateRecovered = false;
  isEmergencyStopFlag = false;

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
  public deviceCampaignHistoryList;
  public campaignInstalledDevice;
  public latestMaintenanceReport;
  public currentCampaignBaseline;
  public dialogRef: MatDialogRef<DeviceHistoryDetailsDialogComponent>
  rowData: any;
  public components;
  public modules: Module[] = [
    ClientSideRowModelModule,
    MenuModule,
    ColumnsToolPanelModule,
    MultiFilterModule,
    SetFilterModule,
  ];
  public sideBar;
  public defaultColDef;
  rowModelType = 'infinite';
  // end ag-grid code
  constructor(private campaignService: CampaignService, private userService: UserService, private pagerService: PagerService,
    private router: Router, private _formBuldier: FormBuilder, private toastr: ToastrService, private forGotPasswordservice: ForGotPasswordservice, private http: HttpClient,private dialog: MatDialog ) {
    var containsFilterParams = {
      filterOptions: [
        'contains',
      ],
      suppressAndOrCondition: true
    };
    
    this.columnDefs = [
      {
        headerName: 'Name', field: 'campaign_name', sortable: true, filter: 'agTextColumnFilter', filterParams: containsFilterParams,
        cellRenderer: function (params) {
          if(params!=undefined && params.value!=null && params.value!=undefined)
          return '<a href="javascript:void(0)">' + params.value + '</a>'
        }
      },
      { headerName: 'Description', field: 'description',minWidth: 180,tooltipField: 'description', sortable: true, filter: 'agTextColumnFilter', filterParams: containsFilterParams 
    ,tooltipComponentParams: { color: '#ececec' },},
      
      {
        headerName: 'Device Type', field: 'device_type',
        sortable: true, filter: 'agSetColumnFilter', filterParams: filterParamsForDeviceType,
        minWidth: 140,
      },
      { headerName: 'Status', field: 'campaign_status', sortable: false, filter: 'agSetColumnFilter', filterParams: filterParams, floatingFilter: true },
      { headerName: 'Last Command', field: 'last_command_days', sortable: false, minWidth: 180 , filter: 'agTextColumnFilter', filterParams: containsFilterParams },
      { headerName: 'Eligible', field: 'eligible', sortable: true, filter: 'agTextColumnFilter', filterParams: containsFilterParams },
      { headerName: 'Completed', field: 'completed', sortable: true, filter: 'agTextColumnFilter', filterParams: containsFilterParams },
      { headerName: 'Problem', field: 'problem_count', sortable: true, filter: 'agTextColumnFilter', filterParams: containsFilterParams },
      { headerName: 'Started', field: 'days_running', sortable: false, filter: 'agTextColumnFilter', filterParams: containsFilterParams, minWidth: 180 },
 //     { headerName: 'Not Started', field: 'not_started_count', minWidth: 160, sortable: true, filter: false, floatingFilter: false },
 //     { headerName: 'In Progress', field: 'in_progress_count', minWidth: 160, sortable: true, filter: false, floatingFilter: false },
      { headerName: 'Created By', field: 'created_by', sortable: true, filter: 'agTextColumnFilter', filterParams: containsFilterParams, hide: true },
      { headerName: 'Waiting', field: 'not_started', sortable: true, filter: 'agTextColumnFilter', filterParams: containsFilterParams, hide: true },
      { headerName: 'In Progress', field: 'in_progress', sortable: true, filter: 'agTextColumnFilter', filterParams: containsFilterParams, hide: true },
	  {
		   headerName: 'Not Installed', field: 'not_installed', sortable: false, hide: true,
			cellRenderer: params => params.value === "" || params.value == null ? "0" : params.value
		},
		{
		headerName: 'Off Path', field: 'off_path', sortable: false, hide: true,
		cellRenderer: params => params.value === "" || params.value == null ? "0" : params.value
		},
		{
		headerName: 'On Path', field: 'on_path', sortable: false, hide: true,
		cellRenderer: params => params.value === "" || params.value == null ? "0" : params.value
		},
      {
        headerName: 'Started Date',
        field: 'campaign_start_date',
        hide: true,
        filter: 'agDateColumnFilter',
        filterParams: containsFilterParams,
        minWidth: 200,
        cellRenderer: (data) => {
          if(data!=undefined && data!=null && data.value!=null && data.value!=undefined)
          return moment(data.value).format('YYYY-MM-DD HH:mm')
        }
      },
      {
        headerName: 'Created Time UTC',
        field: 'created_at',
        filter: 'agDateColumnFilter',
        filterParams: containsFilterParams,
        minWidth: 200,
        cellRenderer: (data) => {
          if(data!=undefined && data.value!=null && data.value!=undefined)
          return moment(data.value).format('YYYY-MM-DD HH:mm')
        }
      },
      //{ headerName: 'Baseline', field: 'firstStep', sortable: true, filter: 'agTextColumnFilter', filterParams: containsFilterParams },
      //{ headerName: 'End State', field: 'lastStep', sortable: true, filter: 'agTextColumnFilter', filterParams: containsFilterParams },

      { headerName: 'Customer', field: 'imei_group', sortable: true, filter: 'agTextColumnFilter', filterParams: containsFilterParams, hide: true },
      { headerName: 'Action', field: 'action', sortable: false, filter: 'agNumberColumnFilter', floatingFilter: true, floatingFilterComponent: 'customClearFloatingFilter', floatingFilterComponentParams: {
        suppressFilterButton: true
      }, cellRenderer: 'btnCellRenderer', width: 100, minWidth: 120, pinned: 'right' }
      //{ headerName: 'IMEI Count', field: 'imei_count', sortable: true, filter: 'agTextColumnFilter', filterParams: containsFilterParams },
      // { headerName: 'Action', field: 'action', sortable: false, filter: false, cellRenderer: 'btnCellRenderer', width: 120, minWidth: 120, pinned: 'right' }
    ]; 
/*  
    this.columnDefs = [
      {
        headerName: 'Name', field: 'campaign_name', sortable: true, filter: 'agTextColumnFilter', filterParams: containsFilterParams,
        cellRenderer: function (params) {
          if(params!=undefined && params.value!=null && params.value!=undefined)
          return '<a href="javascript:void(0)">' + params.value + '</a>'
        }
      },
      { headerName: 'Status', field: 'campaign_status', sortable: true, filter: 'agSetColumnFilter', filterParams: filterParams, floatingFilter: true },
 //     { headerName: 'Not Started', field: 'not_started_count', minWidth: 160, sortable: true, filter: false, floatingFilter: false },
 //     { headerName: 'In Progress', field: 'in_progress_count', minWidth: 160, sortable: true, filter: false, floatingFilter: false },
      { headerName: 'Created By', field: 'created_by', sortable: true, filter: 'agTextColumnFilter', filterParams: containsFilterParams, hide: true },
      {
        headerName: 'Created Time UTC',
        field: 'created_at',
        filter: 'agDateColumnFilter',
        filterParams: containsFilterParams,
        minWidth: 200,
        cellRenderer: (data) => {
          if(data!=undefined && data.value!=null && data.value!=undefined)
          return moment(data.value).format('YYYY-MM-DD HH:mm')
        }
      },
      { headerName: 'Baseline', field: 'firstStep', sortable: true, filter: 'agTextColumnFilter', filterParams: containsFilterParams },
      { headerName: 'End State', field: 'lastStep', sortable: true, filter: 'agTextColumnFilter', filterParams: containsFilterParams },

      { headerName: 'IMEI Group', field: 'imei_group', sortable: true, filter: 'agTextColumnFilter', filterParams: containsFilterParams, hide: true },
      { headerName: 'Action', field: 'action', sortable: false, filter: 'agNumberColumnFilter', floatingFilter: true, floatingFilterComponent: 'customClearFloatingFilter', floatingFilterComponentParams: {
        suppressFilterButton: true
      }, cellRenderer: 'btnCellRenderer', width: 100, minWidth: 120, pinned: 'right' }
      //{ headerName: 'IMEI Count', field: 'imei_count', sortable: true, filter: 'agTextColumnFilter', filterParams: containsFilterParams },
      // { headerName: 'Action', field: 'action', sortable: false, filter: false, cellRenderer: 'btnCellRenderer', width: 120, minWidth: 120, pinned: 'right' }
    ];*/
    this.components = {
      customClearFloatingFilter: this.getClearFilterFloatingComponent(),
    };
    this.cacheOverflowSize = 2;
    this.maxConcurrentDatasourceRequests = 2;
    this.infiniteInitialRowCount = 2;

    if (sessionStorage.getItem('campaign_list_page_size')!=undefined){
      this.pageLimit = Number(sessionStorage.getItem('campaign_list_page_size'));
    } else {
      this.pageLimit = 25;
    }
    this.gridOptions = {
      cacheBlockSize: this.pageLimit,
      paginationPageSize: this.pageLimit,
      rowModelType: 'infinite',
    }
    this.frameworkComponents = {
      btnCellRenderer: BtnCellRendererForCampaign,
      agDateInput: CustomDateComponent,
        agColumnHeader: CustomCampaignsHeader,
         customTooltip: CustomTooltip
    }

    this.defaultColDef = {
      flex: 1,
      minWidth: 150,
      sortable: true,
      floatingFilter: true,
      enablePivot: true,
      resizable: true,
      headerComponentParams: { menuIcon: 'fa-bars' },
      tooltipComponent: 'customTooltip',

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

    this.tooltipShowDelay = 0;

   }

   getEmergencyStopFlagValue() {
    this.campaignService.getEmergencyStopFlagValue()
    .subscribe(data => {
        this.isEmergencyStopFlag = data.status;
    });
   }
   
  ngOnInit() {
    if (sessionStorage.getItem('campaign_list_page_size')!=undefined){
      this.pageLimit = Number(sessionStorage.getItem('campaign_list_page_size'));
    }
   else{
      this.pageLimit = 25;
   }

   this.userCompanyType = sessionStorage.getItem('type');
    this.isSuperAdmin = sessionStorage.getItem('IS_ROLE_SUPERADMIN');
    if(this.isSuperAdmin === 'true'){
      this.router.navigate(['campaigns']);
    }else{
      this.router.navigate(['customerAssets']);
    }

    this.getEmergencyStopFlagValue();
  }

  callEmergencyStop() {
    this.updateEmergencyStop(true, "All campaigns stopped successfully");
    $('#emergencyStopModal').modal('show');
  }

  callResumeCampaignActivity() {
    this.updateEmergencyStop(false, "All campaigns resumed successfully");
    $('#resumeCampaignActivityModal').modal('show');
  }

  updateEmergencyStop(isEmergencyStopFlag, message) {
    this.loading = true;
    this.campaignService.updateEmergencyStopFlagValue(isEmergencyStopFlag).subscribe(data => {
      this.toastr.success(message, null, { toastComponent: CustomSuccessToastrComponent });
      this.campaignService.campaignDetail = undefined;
      this.campaignService.campaignId = null;
      this.paramList.api.paginationCurrentPage = 0;
      this.onGridReady(this.paramList);
      this.getEmergencyStopFlagValue();
      this.loading = false;
    }, error => {
      this.loading = false;
    });
  }

  pauseCampaignDb() {

    if(this.isEmergencyStopFlag) {
      this.toastr.error("User can not update the \"Emergency Stopped\" campaigns", "Update Alert", { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
      return false;
    }
    this.loading = true;
      this.campaignService.changeCampaignStatus(this.campaignService.campaignId, "Stopped",null).subscribe(data => {
        //this.toastr.success(data.message);
        this.toastr.success(data.message, null, { toastComponent: CustomSuccessToastrComponent });
        this.campaignService.campaignDetail = undefined;
        this.campaignService.campaignId = null;
        this.paramList.api.paginationCurrentPage = 0;
        this.onGridReady(this.paramList);
        this.loading = false;
      }, error => {
        this.loading = false;
      });
  }

  playCampaignDb() {

    if(this.isEmergencyStopFlag) {
      this.toastr.error("User can not update the \"Emergency Stopped\" campaigns", "Update Alert", { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
      return false;
    }
    this.loading = true;
      this.campaignService.changeCampaignStatus(this.campaignService.campaignId, "In progress",null).subscribe(data => {
        //this.toastr.success(data.message);
        this.toastr.success(data.message, null, { toastComponent: CustomSuccessToastrComponent });
        this.campaignService.campaignDetail = undefined;
        this.campaignService.campaignId = null;
        this.paramList.api.paginationCurrentPage = 0;
        this.onGridReady(this.paramList);
        this.loading = false;
      }, error => {
        this.loading = false;
      });
  }

  onCellClicked(params) {
    if (params.column.colId == "campaign_name") {
      this.loading = true;
      var uuid = params.data.uuid;
      this.campaignService.findByCampaignId(uuid).subscribe(data => {
        this.campaignService.editFlag = true;
        this.campaignService.campaignDetail = data.body;
        this.loading = false;
        this.router.navigate(['add-campaign']);
      }, error => {
        console.log(error)
      });
    }
  }

  clearFilter() {
    this.gridApi.setFilterModel(null);
    this.paramList.api.paginationCurrentPage = 0;
    this.onGridReady(this.paramList);
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

  getCampaigns(currentPage) {
    this.campaignService.getCampaigns(currentPage, this.column, this.order, this.filterValuesJsonObj, this.pageLimit).subscribe(data => {
      this.Campaigns = data.body;
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

  reloadComponent() {
    let currentUrl = this.router.url;
      this.router.navigateByUrl('/', { skipLocationChange: true }).then(() =>
      this.router.navigate([currentUrl]));
    }

  deleteCampaignDb() {
    this.campaignService.deleteCampaign(this.campaignService.campaignId).subscribe(data => {
      this.toastr.success(data.message, null, { toastComponent: CustomSuccessToastrComponent });
      this.campaignService.campaignDetail = undefined;
      this.campaignService.campaignId = null;
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


  _getdata(page: number) {
    if (page === 0) {
      page = 1;
    }
    if (this.sortFlag) {
      this.getCampaigns(page);
    }
    else {
      this.campaignService.getCampaigns(page, this.column, this.order, {}, this.pageLimit).subscribe(data => {
        this.Campaigns = data.body;
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

  createCampaign() {
    this.router.navigate(['add-campaign']);
  }


  onGridReady(params) {
    this.gridOptions.api.setDomLayout('autoHeight');
    this.paramList = params;
    this.gridApi = params.api;
    this.gridApi.setRowData([]);
    this.gridColumnApi = params.columnApi;
    let gridSavedState = sessionStorage.getItem("campaign_list_grid_column_state");
    if(gridSavedState) {
      this.gridColumnApi.setColumnState(JSON.parse(gridSavedState));
    }
    let filterSavedState = sessionStorage.getItem("campaign_list_grid_filter_state");
    if(filterSavedState) {
      this.gridApi.setFilterModel(JSON.parse(filterSavedState));
    } else {
    }
   
    var datasource = {
      getRows: (params: IGetRowsParams) => {
        this.loading = true; 
        this.sortForAgGrid(params.sortModel);
        this.filterForAgGrid(params.filterModel);
        this.campaignService.getCampaigns(this.gridApi.paginationGetCurrentPage() + 1, this.column, this.order, this.filterValuesJsonObj, this.pageLimit).subscribe(data => {
          this.rowData = data.content;
          this.loading = false;
          if (this.rowData != null && this.rowData.length > 0) {
            for (var i = 0; i < this.rowData.length; i++) {

              if(this.rowData[i].eligible==-100){
                this.rowData[i].eligible="Pending"
              }
              if(this.rowData[i].completed==-100){
                this.rowData[i].completed="Pending"
              }
              if(this.rowData[i].not_started==-100){
                this.rowData[i].not_started="Pending"
              }
              if(this.rowData[i].in_progress==-100){
                this.rowData[i].in_progress="Pending"
              }
              if(this.rowData[i].problem_count==-100){
                this.rowData[i].problem_count="Pending"
              }


              if (this.rowData[i].version_migration_detail != null && this.rowData[i].version_migration_detail.length > 0) {
                const max = this.rowData[i].version_migration_detail.reduce(function (prev, current) {
                  return (prev.step_order_number > current.step_order_number) ? prev : current
                })
                this.rowData[i].lastStep = max.to_package.package_name;

                const min = this.rowData[i].version_migration_detail.reduce(function (prev, current) {
                  return (prev.step_order_number < current.step_order_number) ? prev : current
                })
                this.rowData[i].firstStep = min.from_package.package_name
                // for (var j = 0; j < this.rowData[i].version_migration_detail.length; j++) {

                // }
              }
            }
          }
          params.successCallback(this.rowData, data.totalElements);
          if(this.isFirstTimeLoad == true) {
            let paginationSavedPage = sessionStorage.getItem("campaign_list_grid_saved_page");
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
    if(this.isPaginationStateRecovered || params.api.paginationGetCurrentPage() > 0) {
      sessionStorage.setItem("campaign_list_grid_saved_page", params.api.paginationGetCurrentPage());
    }
  }

  onDragStopped(params) {
    let columnState = this.gridColumnApi.getColumnState();
    this.saveGridStateInSession(columnState);
  }

 
  onSortChanged(params) {
    let columnState = this.gridColumnApi.getColumnState();
    this.saveGridStateInSession(columnState);
  }

  onFilterChanged(params) {
    console.log("Filter change working");
    let filterModel = params.api.getFilterModel();
    console.log(filterModel);
    sessionStorage.setItem("campaign_list_grid_filter_state", JSON.stringify(filterModel));
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
    sessionStorage.setItem("campaign_list_grid_column_state", JSON.stringify(columnState));
  }



  filterForAgGrid(filterModel) {
    this.gridApi.paginationCurrentPage = 0;
    if (filterModel != undefined) {
      this.filterValuesJsonObj = {};
      this.filterValues = new Map();
      if (filterModel.campaign_name != undefined && filterModel.campaign_name.filter != null && filterModel.campaign_name.filter != undefined && filterModel.campaign_name.filter != "") {
        this.filterValues.set("campaignName", filterModel.campaign_name.filter);
      }
      if (filterModel.description != undefined && filterModel.description.filter != null && filterModel.description.filter != undefined && filterModel.description.filter != "") {
        this.filterValues.set("description", filterModel.description.filter);
      }

      if (filterModel.device_type != undefined && filterModel.device_type.values != null && filterModel.device_type.values != undefined) {
        if (filterModel.device_type.values.length == 0) {
          this.filterValues.set("deviceType", "null");
        } else if (filterModel.device_type.values.length == 41) {
          
        } else {
          this.filterValues.set("deviceType", filterModel.device_type.values.toString());
        }
      }

      if (filterModel.created_by != undefined && filterModel.created_by.filter != null && filterModel.created_by.filter != undefined && filterModel.created_by.filter != "") {
        this.filterValues.set("createdBy", filterModel.created_by.filter);
      }

      if (filterModel.firstStep != undefined && filterModel.firstStep.filter != null && filterModel.firstStep.filter != undefined && filterModel.firstStep.filter != "") {
        this.filterValues.set("firstStep", filterModel.firstStep.filter);
      }

      if (filterModel.lastStep != undefined && filterModel.lastStep.filter != null && filterModel.lastStep.filter != undefined && filterModel.lastStep.filter != "") {
        this.filterValues.set("lastStep", filterModel.lastStep.filter);
      }

      // if (filterModel.campaign_status != undefined && filterModel.campaign_status.filter != null && filterModel.campaign_status.filter != undefined && filterModel.campaign_status.filter != "") {
      //   this.filterValues.set("campaignStatus", filterModel.campaign_status.filter);
      // }

      if (filterModel.campaign_status != undefined && filterModel.campaign_status.values != undefined && filterModel.campaign_status.values != null) {
        if (filterModel.campaign_status.values.length == 0) {
          this.filterValues.set("campaignStatus", "null");
        } else {
          // for (var i = 0; i < filterModel.campaign_status.values.length; i++) {
          //   this.filterValues.set("campaignStatus", filterModel.campaign_status.values[i]);
          // }
          this.filterValues.set("campaignStatus", filterModel.campaign_status.values.toString());
        }
      }

      if (filterModel.imei_group != undefined && filterModel.imei_group.filter != null && filterModel.imei_group.filter != undefined && filterModel.imei_group.filter != "") {
        this.filterValues.set("imeiGroup", filterModel.imei_group.filter);
      }

      if (filterModel.imei_count != undefined && filterModel.imei_count.filter != null && filterModel.imei_count.filter != undefined && filterModel.imei_count.filter != "") {
        this.filterValues.set("imeiCount", filterModel.imei_count.filter);
      }

      if (filterModel.created_at != undefined && filterModel.created_at.dateFrom != null && filterModel.created_at.dateFrom != undefined && filterModel.created_at.dateFrom != "") {
        this.filterValues.set("createdAt", filterModel.created_at.dateFrom);
      }

      if (filterModel.campaign_start_date != undefined && filterModel.campaign_start_date.dateFrom != null && filterModel.campaign_start_date.dateFrom != undefined && filterModel.campaign_start_date.dateFrom != "") {
        this.filterValues.set("campaignStartDate", filterModel.campaign_start_date.dateFrom);
      }

      if (filterModel.last_command_days != undefined && filterModel.last_command_days.filter != null && filterModel.last_command_days.filter != undefined && filterModel.last_command_days.filter != "") {
        this.filterValues.set("lastCommandDays", filterModel.last_command_days.filter);
      }

      if (filterModel.days_running != undefined && filterModel.days_running.filter != null && filterModel.days_running.filter != undefined && filterModel.days_running.filter != "") {
        this.filterValues.set("daysRunning", filterModel.days_running.filter);
      }

      if (filterModel.problem_count != undefined && filterModel.problem_count.filter != null && filterModel.problem_count.filter != undefined && filterModel.problem_count.filter != "") {
        this.filterValues.set("problemCount", filterModel.problem_count.filter);
      }
      
       if (filterModel.eligible != undefined && filterModel.eligible.filter != null && filterModel.eligible.filter != undefined && filterModel.eligible.filter != "") {
        this.filterValues.set("eligible", filterModel.eligible.filter);
      }
      
       if (filterModel.completed != undefined && filterModel.completed.filter != null && filterModel.completed.filter != undefined && filterModel.completed.filter != "") {
        this.filterValues.set("completed", filterModel.completed.filter);
      }
      
      if (filterModel.not_started != undefined && filterModel.not_started.filter != null && filterModel.not_started.filter != undefined && filterModel.not_started.filter != "") {
        this.filterValues.set("notStarted", filterModel.not_started.filter);
      }
      if (filterModel.in_progress != undefined && filterModel.in_progress.filter != null && filterModel.in_progress.filter != undefined && filterModel.in_progress.filter != "") {
        this.filterValues.set("inProgress", filterModel.in_progress.filter);
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
      case 'campaign_name': {
        this.column = "campaignName";
        this.order = sortModel[0].sort;
        break;
      }
      case 'description': {
        this.column = "description";
        this.order = sortModel[0].sort;
        break;
      }
      case 'device_type': {
        this.column = "deviceType";
        this.order = sortModel[0].sort;
        break;
      }
      case 'created_by': {
        this.column = "createdBy";
        this.order = sortModel[0].sort;
        break;
      }
      case 'not_started_count': {
        this.column = "notStartedCount";
        this.order = sortModel[0].sort;
        break;
      }
      case 'in_progress_count': {
        this.column = "inProgressCount";
        this.order = sortModel[0].sort;
        break;
      }
      case 'firstStep': {
        this.column = "firstStep";
        this.order = sortModel[0].sort;
        break;
      }
      case 'lastStep': {
        this.column = "lastStep";
        this.order = sortModel[0].sort;
        break;
      }

      case 'campaign_status': {
        this.column = "campaignStatus";
        this.order = sortModel[0].sort;
        break;
      }


      case 'imei_group': {
        this.column = "imeiGroup";
        this.order = sortModel[0].sort;
        break;
      }


      case 'imei_count': {
        this.column = "imeiCount";
        this.order = sortModel[0].sort;
        break;
      }

      case 'created_at': {
        this.column = "createdAt";
        this.order = sortModel[0].sort;
        break;
      }

      case 'last_command_days': {
        this.column = "lastCommandDays";
        this.order = sortModel[0].sort;
        break;
      }

      case 'days_running': {
        this.column = "daysRunning";
        this.order = sortModel[0].sort;
        break;
      }

      case 'problem_count': {
        this.column = "problemCount";
        this.order = sortModel[0].sort;
        break;
      }

      case 'campaign_start_date': {
        this.column = "campaignStartDate";
        this.order = sortModel[0].sort;
        break;
      }
      
     
      
         case 'completed': {
        this.column = "completed";
        this.order = sortModel[0].sort;
        break;
      }
      
      
         case 'eligible': {
        this.column = "eligible";
        this.order = sortModel[0].sort;
        break;
      }

      case 'not_started': {
        this.column = "notStarted";
        this.order = sortModel[0].sort;
        break;
      }

      case 'in_progress': {
        this.column = "inProgress";
        this.order = sortModel[0].sort;
        break;
      }
      
      

      default: {
        console.log("default")
      }
    }
  }

  getClearFilterFloatingComponent() {
    function ClearFloatingFilter() {}
    ClearFloatingFilter.prototype.init = function (params) {
      this.eGui = document.createElement('div');
      this.eGui.innerHTML =
        '<button class="btnClear primary_buttonClear" (click)="clearFilter()">Clear Filters</button>';
      this.currentValue = null;
      this.clearFilterButton = this.eGui.querySelector('button');
      var that = this;
      function onClearFilterButtonClicked() {
        this.column ='campaignName';
        this.order ='asc';
        params.api.setFilterModel(null);
        params.column.columnApi.applyColumnState({ defaultState: { sort: null } });
        //params.column.columnApi.resetColumnState();
        params.api.onFilterChanged();
        sessionStorage.setItem("campaign_list_grid_saved_page", null);
        sessionStorage.setItem("campaign_list_grid_filter_state", null);
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
   sessionStorage.setItem('campaign_list_page_size',String(this.selected));
   console.log(sessionStorage.getItem('campaign_list_page_size') +"from onPageSizeChange");
    var value=this.selected;
    this.pageLimit = (Number(value));
    this.gridApi.gridOptionsWrapper.setProperty('cacheBlockSize', value);
    const api: any = this.gridApi;
    api.rowModel.resetCache();
    this.gridApi.paginationSetPageSize(Number(value));
  }

   
  deviceSearch(event) {
    console.log(event.target.value);

    if (event.target.value.length === 15) {
      this.loading = true;

      setTimeout(() => {
        this.campaignService.getDeviceCampaignHistoryByImei(event.target.value).subscribe(data => {
          this.deviceCampaignHistoryList = data;
          this.loading = false;

        }, error => {
          console.log(error)
          this.loading = false;
        });

        this.campaignService.findCampaignInstalledDevice(event.target.value).subscribe(data => {

          this.campaignInstalledDevice = data;
          this.loading = false;

          if (this.campaignInstalledDevice) {
            this.campaignInstalledDevice.qa_TIMESTAMP = this.campaignInstalledDevice.qa_TIMESTAMP ?
              moment(this.campaignInstalledDevice.qa_TIMESTAMP).format("MM-DD-YYYY HH:mm:ss") : '';
          }
          if (this.campaignInstalledDevice) {
            this.campaignInstalledDevice.latest_MAINTENANCE_QA_TIMESTAMP = this.campaignInstalledDevice.latest_MAINTENANCE_QA_TIMESTAMP ?
              moment(this.campaignInstalledDevice.latest_MAINTENANCE_QA_TIMESTAMP).format("MM-DD-YYYY HH:mm:ss") : '';
          }
          if (this.campaignInstalledDevice) {
            this.campaignInstalledDevice.first_POWER_UP_TIMESTAMP_AFTER_QA = this.campaignInstalledDevice.first_POWER_UP_TIMESTAMP_AFTER_QA ?
              moment(this.campaignInstalledDevice.first_POWER_UP_TIMESTAMP_AFTER_QA).format("MM-DD-YYYY HH:mm:ss") : '';
          }
          if (this.campaignInstalledDevice) {
            this.campaignInstalledDevice.is_INSTALLED_FOR_CAMPAIGN_TIMESTAMP = this.campaignInstalledDevice.is_INSTALLED_FOR_CAMPAIGN_TIMESTAMP ?
              moment(this.campaignInstalledDevice.is_INSTALLED_FOR_CAMPAIGN_TIMESTAMP).format("MM-DD-YYYY HH:mm:ss") : '';
          }
          if (this.campaignInstalledDevice) {
            this.campaignInstalledDevice.last_UPDATED_AT = this.campaignInstalledDevice.last_UPDATED_AT ?
              moment(this.campaignInstalledDevice.last_UPDATED_AT).format("MM-DD-YYYY HH:mm:ss") : '';
          }
        }, error => {
          console.log(error)
          this.loading = false;
        });

        this.campaignService.fetchCampaignHistory(event.target.value).subscribe(data => {
          this.latestMaintenanceReport = data.device_report;
          this.currentCampaignBaseline = data.package_payload;
          this.loading = false;

        }, error => {
          console.log(error)
          this.loading = false;
        });

      }, 100);
      setTimeout(() => {
        if(this.latestMaintenanceReport !== null && this.latestMaintenanceReport !== undefined){
          this.deviceHistoryPopupValue();
        }else{
          this.toastr.info("Device history not found.", "", { toastComponent: CustomErrorToastrComponent });
        }
      }, 800);

    }else{
      this.toastr.error("Please enter 15 digit imei.", "", { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });

    }
  }
  deviceHistoryPopupValue(): void {
    this.dialogRef = this.dialog.open(DeviceHistoryDetailsDialogComponent, {
      width: '1200px',
      height: '750px',
      data: { data: this.deviceCampaignHistoryList, campaginHistory: this.deviceCampaignHistoryList, campaginInstalledDevice: this.campaignInstalledDevice, latestMaintenanceReport: this.latestMaintenanceReport, currentCampaignBaseline: this.currentCampaignBaseline }

    });
  }
}

var filterParams = {
  values: function (params) {
    setTimeout(function () {
      //params.success(['Not Started', 'In Progress', 'Paused', 'Stopped', 'Completed']);
      params.success(['Not Started', 'In Progress', 'Paused', 'Stopped', 'Finished']);
    }, 3000);
  },
};

var filterParamsForDeviceType = {
  values: function (params) {
    setTimeout(function () {
      params.success([
        "ANY",
        "arrow-g",
        "katana-g",
        "dagger-g",
        "cutlass-g",
        "arrow-c",
        "katana-c",
        "dagger-c",
        "cutlass-c",
        "arrow-h",
        "katana-h",
        "dagger-h",
        "cutlass-h",
        "arrow-l",
        "katana-l",
        "dagger-l",
        "cutlass-l",
        "arrow-ma",
        "katana-ma",
        "dagger-ma",
        "cutlass-ma",
        "dagger67-ma",
        "arrow-mg",
        "katana-mg",
        "dagger-mg",
        "cutlass-mg",
        "arrow-lg",
        "katana-lg",
        "dagger-lg",
        "cutlass-lg",
        "dagger67-lg",
        "arrow-lgc",
        "arrow-la",
        "katana-la",
        "dagger-la",
        "cutlass-la",
        "dagger67-la",
        "arrow-lac",
        "cutlass-la",
        "dagger67-qa",
        "arrow-qa",
        "arrow-qg"]);
    }, 2000);
  },
};