import { Component, OnInit, Inject, ViewChild } from '@angular/core';
import { Router } from '@angular/router';
import { UsersListService } from './users-list.component.service';
import { FormBuilder, FormGroup, Validators, FormControl } from '@angular/forms';
import { PagerService } from '../util/pagerService';
import { User } from '../models/user.model';
import { UserService } from '../user/user.component.service';
import { ToastrService } from 'ngx-toastr';
import { ForGotPasswordservice } from '../forgotpassword/forgotpassword.component.service';
import { CustomSuccessToastrComponent } from '../custom-success-toastr/custom-success-toastr.component';
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
import { BtnCellRendererForUser } from './btn-cell-renderer.component';
import { CustomHeader } from '../ui-elements/custom-header.component';
import { PaginationConstants } from '../util/paginationConstants';
import { AssetsService } from '../assets.service';
import { CountryCode } from '../constant/country.code';

@Component({
  selector: 'app-users-list',
  templateUrl: './users-list.component.html',
  styleUrls: ['./users-list.component.css']
})
export class UsersListComponent implements OnInit {
  selected = 25;
  userToInactive;
  currentPage: any;
  pager: any = {};
  pagedItems: any[];
  private allItems: any;
  totalpage;
  showingpage;
  page = 0;
  loading = false;
  pageSizeOptions: PaginationConstants = new PaginationConstants();
  pageSizeObjArr = [
    { id: '10', idValue: 10 },
    { id: '25', idValue: 25 }
  ];
  pageSizeOptionsArr: number[];
  // parameters for sorting
  sortByEmail = false;
  sortByFirstName = false;
  sortByLastName = false;
  sortByPhone = false;
  sortByActive = false;
  sortByFleet = false;
  sortByRole = false;
  sortFlag;
  column = 'lastName';
  order = 'asc';

  // parameters for counting records
  pageLimit = 25;
  totalRecords = 0;
  currentEndRecord = 0;
  currentStartRecord = 0;
  isSuperAdmin;
  // parameters for counting records
  companies: any[];

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
  public components;
  public paramList;
  countryCodeList = CountryCode.COUNTRY_CODE_LIST;
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
  roleCallCenter: any;
  isRoleOrgUser: any;
  users: any[];
  isRolePctUser: any;
  constructor(
    public userListService: UsersListService,
    public userService: UserService,
    private pagerService: PagerService,
    private router: Router,
    private toastr: ToastrService,
    private forGotPasswordservice: ForGotPasswordservice) {
    this.roleCallCenter = JSON.parse(sessionStorage.getItem('IS_ROLE_CALL_CENTER_USER'));
    this.isRoleOrgUser = JSON.parse(sessionStorage.getItem('IS_ROLE_ORG_USER'));
    this.isRolePctUser = JSON.parse(sessionStorage.getItem('IS_ROLE_PCT_USER'));
    let filterCallCenter: any;
    let sort = true;
    if (this.roleCallCenter) {
      filterCallCenter = false;
      sort = false;
    } else {
      filterCallCenter = 'agTextColumnFilter';
    }
    const containsFilterParams = {
      filterOptions: [
        'contains',
      ],
      suppressAndOrCondition: true
    };
    let countryCodeObjList = this.countryCodeList;
    this.columnDefs = [
      {
        headerName: 'Email', field: 'email', sortable: true, filter: 'agTextColumnFilter', filterParams: containsFilterParams,

        cellRenderer(params) {
          if (params.data && params.data.email) {
            return '<a href="javascript:void(0)">' + params.data.email + '</a>';
          }
        }
      },
      {
        headerName: 'First Name', field: 'first_name', sortable: true, filter: 'agTextColumnFilter', filterParams: containsFilterParams,
      },
      { headerName: 'Last Name', field: 'last_name', sortable: true, filter: 'agTextColumnFilter', filterParams: containsFilterParams },
      {
        headerName: 'Mobile #', field: 'phone', sortable: true, filter: 'agTextColumnFilter', filterParams: containsFilterParams,
        cellRenderer(params) {
          if (params.data && params.data.country_code) {
            const code = params.data.country_code.toUpperCase();
            const obj = countryCodeObjList.find(item => item.code === code);
            if (obj) {
              return obj.dial_code + ' ' + params.data.phone;
            }
            return params.data.phone;
          } else if (params.data && params.data.phone) {
            return params.data.phone;
          }
        }
      },
      {
        headerName: 'Role', field: 'roleName', sortable: true, filter: 'agSetColumnFilter', floatingFilter: true, flex: 1,
        filterParams,
        cellRenderer(params) {
          if (params.data && params.data.role) {
            return params.data.role.name;
          }
        }
      },
      {
        headerName: 'Organization', field: 'organisation_name', sortable: sort, filter: filterCallCenter, filterParams: containsFilterParams, minWidth: 180,
      },
      {
        headerName: 'Status', field: 'is_active', sortable: false, filter: 'agSetColumnFilter', floatingFilter: true, flex: 1,
        filterParams: filterParamsStatus,
        cellRenderer(params) {
          if (params.data) {
            if (params.data.is_active) {
              return '<div class="status"><button class="btn-xs active"><span>Active</span></button></div>';
            } else {
              return ' <div class="status"><button class="btn-xs inactive"><span>Inactive</span></button></div>';
            }
          }
        }
      },
      {
        headerName: 'Reset Password', field: 'resetPassword', sortable: false, filter: false, minWidth: 200,
        cellRenderer(params) {
          if (params.data && params.data.email) {
            return ' <button class="btn-xs primary_button" (click)="restPassword(user.email)"><span>Reset Password</span></button>';
          }
        }
      },
      {
        headerName: 'Action', field: 'action', sortable: false, filter: 'agNumberColumnFilter', floatingFilter: true, floatingFilterComponent: 'customClearFloatingFilter', floatingFilterComponentParams: {
          suppressFilterButton: true
        }, cellRenderer: 'btnCellRenderer', width: 100, minWidth: 120, pinned: 'right',
      }

    ];
    this.cacheOverflowSize = 2;
    this.maxConcurrentDatasourceRequests = 2;
    this.infiniteInitialRowCount = 2;
    if (sessionStorage.getItem('user_list_page_size')) {
      this.pageLimit = Number(sessionStorage.getItem('user_list_page_size'));
    } else {
      this.pageLimit = 25;
    }
    this.gridOptions = {
      cacheBlockSize: this.pageLimit,
      paginationPageSize: this.pageLimit,
      rowModelType: 'infinite',
    };
    this.frameworkComponents = {
      btnCellRenderer: BtnCellRendererForUser,
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
    if (sessionStorage.getItem('user_list_page_size')) {
      this.pageLimit = Number(sessionStorage.getItem('user_list_page_size'));
    } else {
      this.pageLimit = 25;
    }

    this.sortFlag = true;
    this.isSuperAdmin = sessionStorage.getItem('IS_ROLE_SUPERADMIN');
  }


  onGridReady(params) {
    // this.gridOptions.api.setDomLayout('autoHeight');
    this.paramList = params;
    this.gridApi = params.api;
    this.gridApi.setRowData([]);
    this.gridColumnApi = params.columnApi;
    const gridSavedState = sessionStorage.getItem('user_list_grid_column_state');
    if (gridSavedState) {
      this.gridColumnApi.setColumnState(JSON.parse(gridSavedState));
    }
    const filterSavedState = sessionStorage.getItem('user_list_grid_filter_state');
    if (filterSavedState) {
      this.gridApi.setFilterModel(JSON.parse(filterSavedState));
    }

    const datasource = {
      getRows: (params: IGetRowsParams) => {
        this.pageSizeObjArr = [
          { id: '10', idValue: 10 },
          { id: '25', idValue: 25 }
        ];
        this.loading = true;
        this.sortForAgGrid(params.sortModel);
        this.filterForAgGrid(params.filterModel);
        this.userListService.getUsersWithSort(this.gridApi.paginationGetCurrentPage() + 1, this.column, this.order, this.filterValuesJsonObj, this.pageLimit).subscribe(data => {
          this.loading = false;
          this.rowData = data.body.content;
          if (this.rowData != null && this.rowData.length > 0) {
            for (let i = 0; i < this.rowData.length; i++) {
              this.rowData[i].organisationName = this.rowData[i].organisationName;
              if (this.rowData[i].role != undefined && this.rowData[i].role.length > 0) {
                let roleNam = '';
                if (this.rowData[i].role.length > 1) {
                  for (let j = 0; j < this.rowData[i].role.length; j++) {
                    roleNam = roleNam + this.rowData[i].role[j].roleName + ',   ';
                    this.rowData[i].roleName = roleNam;
                  }
                } else {
                  roleNam = this.rowData[i].role[0].roleName;
                  this.rowData[i].roleName = roleNam;
                }

              }
            }
          }
          if (data.body.totalElements > 0 && data.body.totalElements < 25) {
            this.gridOptions.paginationPageSize = data.body.totalElements;
          } else {
            this.gridOptions.paginationPageSize = this.pageLimit;
          }

          if (data.body.totalElements > 50) {
            this.pageSizeObjArr.push({ id: '50', idValue: 50 });
          }

          if (data.body.totalElements > 100) {
            this.pageSizeObjArr.push({ id: '100', idValue: 100 });
          }
          if (data.body.totalElements > 200) {
            this.pageSizeObjArr.push({ id: '200', idValue: 200 });
          }
          this.pageSizeObjArr.push({ id: 'All', idValue: data.body.totalElements });

          params.successCallback(this.rowData, data.body.total_key);
          if (this.isFirstTimeLoad) {
            const paginationSavedPage = sessionStorage.getItem('user_list_grid_saved_page');
            if (paginationSavedPage && this.isFirstTimeLoad) {
              this.gridOptions.api.paginationGoToPage(Number.parseInt(paginationSavedPage));
              this.isFirstTimeLoad = false;
              this.isPaginationStateRecovered = true;
            }
          }
        }, error => {
          console.log(error);
        });
      }
    };
    this.gridApi.setDatasource(datasource);
  }

  onDragStopped(params) {
    this.saveGridStateInSession(this.gridColumnApi.getColumnState());
  }


  onSortChanged(params) {
    this.saveGridStateInSession(this.gridColumnApi.getColumnState());
  }

  onFilterChanged(params) {
    sessionStorage.setItem('user_list_grid_filter_state', JSON.stringify(params.api.getFilterModel()));
  }

  onColumnVisible(params) {
    this.saveGridStateInSession(this.gridColumnApi.getColumnState());
  }
  onColumnResized(params) {
    this.saveGridStateInSession(this.gridColumnApi.getColumnState());
  }

  onColumnPinned(params) {
    this.saveGridStateInSession(this.gridColumnApi.getColumnState());
  }

  onPaginationChanged(params) {
    if (this.isPaginationStateRecovered || params.api.paginationGetCurrentPage() > 0) {
      sessionStorage.setItem('user_list_grid_saved_page', params.api.paginationGetCurrentPage());
    }
  }

  onFirstDataRendered(params) {
    this.isFirstTimeLoad = true;
  }



  saveGridStateInSession(columnState) {
    sessionStorage.setItem('user_list_grid_column_state', JSON.stringify(columnState));
  }


  countRecords(data) {
    if (data.body === null || data.body.length === 0) {
      this.totalRecords = 0;
      this.currentStartRecord = 0;
      this.currentEndRecord = 0;

    } else {
      this.totalRecords = data.totalKey;
      this.currentStartRecord = (this.pageLimit * (data.currentPage)) + 1;
      this.currentPage = data.currentPage + 1;
      if (this.pageLimit === data.body.content.length) {
        this.currentEndRecord = this.pageLimit * (data.currentPage + 1);
      } else {
        this.currentEndRecord = this.currentStartRecord + data.body.content.length - 1;
      }
    }
  }


  getUsers(currentPage, pageLimit) {
    this.userListService.getActiveUsers(currentPage, pageLimit).subscribe(data => {
      this.users = data.body.content;
      this.totalpage = data.total_pages;
      this.showingpage = data.currentPage + 1;
      this.allItems = data.totalKey;
      this.countRecords(data);
      this.setPage(this.showingpage, data.body, data.total_pages, data.body.length);
      if (data.body.content == null) {
        this.showingpage = 0;
      }
    }, error => {
      console.log(error);
    });

  }



  onCellClicked(params) {
    if (params.data) {
      if (params.column.colId === 'resetPassword') {
        this.restPassword(params.data.email);
      }

      if (params.column.colId === 'email') {
        if (!this.isRolePctUser) {
          this.loading = true;
          this.editUser(params.data);
        }
      }
      this.userToInactive = params.data;
    }
  }

  clearFilter() {
    this.column = 'lastName';
    this.order = 'asc';
    this.gridColumnApi.applyColumnState({ defaultState: { sort: null } });
    this.gridApi.setFilterModel(null);
    this.gridColumnApi.resetColumnState();
    sessionStorage.setItem('user_list_grid_column_state', null);
    sessionStorage.setItem('user_list_grid_filter_state', null);
    this.paramList.api.paginationCurrentPage = 0;
    this.onGridReady(this.paramList);
  }

  setPage(page: number, body, totalPages, pageItems) {
    this.pager = this.pagerService.getPager(this.allItems, page, totalPages, pageItems);
    this.pagedItems = body;
  }
  reloadComponent() {
    const currentUrl = this.router.url;
    this.router.navigateByUrl('/', { skipLocationChange: true }).then(() =>
      this.router.navigate([currentUrl]));
  }

  onBtNormalCols() {
    this.gridApi.onFilterChanged();
  }

  inactiveUser(user: User): void {
    this.userListService.inActiveUserById(user.id)
      .subscribe(data => {
        this.toastr.success('User Deactivated Successfully', null, { toastComponent: CustomSuccessToastrComponent });
        this.reloadComponent();
      }, error => {
        console.log(error);
      });
  }

  editUser(user: User): void {
    this.router.navigate(['add-user'], { queryParams: { id: user.id } });
  }

  createUser() {
    this.router.navigate(['add-user']);
  }


  filterForAgGrid(filterModel) {
    this.gridApi.paginationCurrentPage = 0;
    if (filterModel) {
      this.filterValuesJsonObj = {};
      this.filterValues = new Map();
      if (filterModel.email && filterModel.email.filter && filterModel.email.filter !== '') {
        this.filterValues.set('email', filterModel.email.filter);
      }

      if (filterModel.first_name && filterModel.first_name.filter && filterModel.first_name.filter !== '') {
        this.filterValues.set('firstName', filterModel.first_name.filter);
      }

      if (filterModel.last_name && filterModel.last_name.filter && filterModel.last_name.filter !== '') {
        this.filterValues.set('lastName', filterModel.last_name.filter);
      }

      if (filterModel.phone && filterModel.phone.filter && filterModel.phone.filter !== '') {
        this.filterValues.set('phone', filterModel.phone.filter);
      }


      if (filterModel.organisation_name && filterModel.organisation_name.filter && filterModel.organisation_name.filter !== '') {
        this.filterValues.set('organisationName', filterModel.organisation_name.filter);
      }


      if (filterModel.roleName && filterModel.roleName.values && filterModel.roleName.values != null) {
        if (filterModel.roleName.values.length === 0) {
          this.filterValues.set('roleName', 'null');
        } else {
          // for (const iterator of filterModel.roleName.values) {
          this.filterValues.set('roleName', filterModel.roleName.values.toString());
          // }
        }
      }
      if (filterModel.is_active && filterModel.is_active.values) {
        if (filterModel.is_active.values.length === 0) {
          this.filterValues.set('isActive', 'null');
        } else {
          for (const iterator of filterModel.is_active.values) {
            const value = iterator === 'Active' ? true : false;
            this.filterValues.set('isActive', value);
          }
        }
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
    if (sortModel[0] === undefined || sortModel[0] == null || sortModel[0].colId === undefined || sortModel[0].colId == null) {
      return;
    }
    this.sortFlag = true;
    switch (sortModel[0].colId) {
      case 'email': {
        this.column = 'email';
        this.order = sortModel[0].sort;
        break;
      }
      case 'first_name': {

        this.column = 'firstName';
        this.order = sortModel[0].sort;
        break;
      }
      case 'last_name': {
        this.column = 'lastName';
        this.order = sortModel[0].sort;
        break;
      }
      case 'phone': {

        this.column = 'phone';
        this.order = sortModel[0].sort;
        break;
      }

      case 'is_active': {

        this.column = 'isActive';
        this.order = sortModel[0].sort;
        break;
      }

      case 'organisation_name': {

        this.column = 'organisation.organisationName';
        this.order = sortModel[0].sort;
        break;
      }
      case 'roleName': {
        this.column = 'role.name';
        this.order = sortModel[0].sort;
        break;
      }
      default: {
        console.log('default');
      }
    }
  }
  restPassword(email) {
    this.loading = true;
    const role = sessionStorage.getItem('role');
    if (!this.isSuperAdmin) {
      this.toastr.success('Not authorized', null, { toastComponent: CustomSuccessToastrComponent });
      this.loading = false;
      return;
    }
    this.forGotPasswordservice.restPasswordByAdmin(email).subscribe(data => {
      this.toastr.success('Password Reset successful. The new password is sent to the user on his Email', null, { toastComponent: CustomSuccessToastrComponent });
      this.loading = false;
    }, error => {
      this.loading = false;
      console.log(error);
    });

  }

  superAdmin(user) {
    if (user.role.some((r) => r.roleName === 'ROLE_SUPERADMIN')) {
      return true;
    }
    return false;
  }

  getClearFilterFloatingComponent() {
    function ClearFloatingFilter() { }
    ClearFloatingFilter.prototype.init = function (params) {
      this.eGui = document.createElement('div');
      this.eGui.innerHTML =
        '<button class="btnClear primary_buttonClear">Clear Filters</button>';
      this.currentValue = null;
      this.clearFilterButton = this.eGui.querySelector('button');
      const that = this;
      function onClearFilterButtonClicked() {
        this.column = 'lastName';
        this.order = 'asc';
        params.api.setFilterModel(null);
        sessionStorage.setItem('user_list_grid_filter_state', null);
        params.api.paginationCurrentPage = 0;

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
    sessionStorage.setItem('user_list_page_size', String(this.selected));
    const value = this.selected;
    this.pageLimit = (Number(value));
    this.gridApi.gridOptionsWrapper.setProperty('cacheBlockSize', value);
    const api: any = this.gridApi;
    api.rowModel.resetCache();
    this.gridApi.paginationSetPageSize(Number(value));
  }

}


const filterParams = {
  values(params) {
    setTimeout(function () {
      let filterValue = ['Phillips Connect Admin', 'Org Admin', 'Phillips Connect User', 'Call Center User', 'Org User', 'Maintenance Role'];
      const roleCallCenter = JSON.parse(sessionStorage.getItem('IS_ROLE_CALL_CENTER_USER'));
      if (roleCallCenter) {
        filterValue = ['Call Center User'];
      }
      params.success(filterValue);
    }, 3000);
  },
};
const filterParamsStatus = {
  values(params) {
    setTimeout(function () {
      params.success(['Active', 'Inactive']);
    }, 1000);
  },
};
