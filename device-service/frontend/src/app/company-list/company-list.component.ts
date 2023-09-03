import { Component, OnInit, ViewChild } from '@angular/core';
import { CompaniesService } from '../companies.service';
import { PagerService } from '../util/pagerService';
import { ToastrService } from 'ngx-toastr';
import { Router, NavigationExtras } from '@angular/router';
import { CreateCompanyDTO } from '../models/createCompanyDTO.model';
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
import { SetFilterModule } from '@ag-grid-enterprise/set-filter';
import { BtnCellRendererForCompany } from './btn-cell-renderer.component';
import { CustomHeader } from '../ui-elements/custom-header.component';
import { PaginationConstants } from '../util/paginationConstants';

@Component({
  selector: 'app-company-list',
  templateUrl: './company-list.component.html',
  styleUrls: ['./company-list.component.css']
})
export class CompanyListComponent implements OnInit {
  currentPage = 1;
  isFirst = true;
  pager: any = {};
  pagedItems: any[];
  private allItems: any;
  batteryIconclass: any = [];
  totalpage;
  showingpage;
  page = 0;
  sortFlag;
  isSuperAdmin;
  newCompany: CreateCompanyDTO;
  companyToInactive;
  loading = false;
  selected: number;
  // pageSizeOptions: PaginationConstants = new PaginationConstants();
  // pageSizeObjArr = this.pageSizeOptions.pageSizeObjArr;
  // pageSizeOptionsArr: number[];
  // parameters for sorting column
  // sortByFullName = false;
  // sortByShortName = false;
  // sortByType = false;
  // sortByStatus = false;
  // sortByCanView = false;
  column = 'organisationName';
  order = 'asc';

  roleSort: any;

  // parameters for counting records
  pageLimit = 50;
  // totalRecords = 0;
  // currentEndRecord = 0;
  // currentStartRecord = 0;
  totalNumberOfRecords = 0;

  companies: any[];
  public components;
  filterValues = new Map();
  filterValuesJsonObj: any = {};
  isClearButtonVisible = false;
  frameworkComponents;
  forwardingGroups: any[];

  // ag-grid code
  @ViewChild('myGrid') myGrid: AgGridAngular;
  public gridOptions: Partial<GridOptions>;
  public gridApi;
  public paramList;
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
  rowModelType = 'infinite';
  public columnDefs;
  public gridColumnApi;
  public cacheOverflowSize;
  public maxConcurrentDatasourceRequests;
  public infiniteInitialRowCount;
  rolePCTUser: any;
  // end ag-grid code
  // Pagination Save State
  isFirstTimeLoad = false;
  isPaginationStateRecovered = false;
  organisationNameList: String[] = [];
  filterParamsCompany: any;
  constructor(
    public companiesListService: CompaniesService,
    private pagerService: PagerService,
    private readonly router: Router,
    private toastr: ToastrService) {
    this.getAllcompanies();
    this.getAllForwardingGroups();
    this.rolePCTUser = JSON.parse(sessionStorage.getItem('IS_ROLE_PCT_USER'));
    this.isSuperAdmin = JSON.parse(sessionStorage.getItem('IS_ROLE_SUPERADMIN'));
    const containsFilterParams = {
      filterOptions: [
        'contains'
      ],
      suppressAndOrCondition: true
    };

    // const filterParamsCompany = {
    //   values: params => params.success(this.companies)
    // };

    let self = this;
    const filterParamsCompany = {
      values(params: any) {
        setTimeout(function () {
          params.success(self.getCompanyFilterValues());
        }, 3000);
      },
    };

    const filterParamsForwardingGroups = {
      values(params: any) {
        setTimeout(function () {
          params.success(self.getForwardingGroupsFilterValues());
        }, 3000);
      },
    };

    this.columnDefs = [
      {
        headerName: 'Name', field: 'organisation_name', sortable: true, filter: 'agTextColumnFilter', filterParams: containsFilterParams,
        cellRenderer(params: any) {
          if (params.data != undefined) {
            return '<a class="pointer" style="color:#4BA9D7;" href="javascript:void(0)">' + params.value + '</a>';
          }
        }
      },
      {
        headerName: 'Forwarding Group',
        field: 'forwarding_group',
        sortable: true,
        filter: 'agSetColumnFilter',
        filterParams: filterParamsForwardingGroups
      },
      {
        headerName: 'Parent Group',
        field: 'resellers',
        sortable: true,
        filter: 'agSetColumnFilter',
        filterParams: filterParamsCompany,
        cellRenderer(params: any) {
          if (params.data) {
            let valString = "";
            params.data.resellers.forEach(x => {
              if (valString == "") {
                valString = x.organisation_name;
              } else {
                valString = valString + ", " + x.organisation_name;
              }
            });
            return valString;
          }
        }
      },
      {
        headerName: 'End User',
        field: 'end_customer',
        sortable: true,
        filter: 'agSetColumnFilter',
        filterParams: yOrNFilterParams,
        cellRenderer(params: any) {
          if (params.data) {
            if (params.data.organisation_roles != null
              && params.data.organisation_roles.filter(r => r === 'End_Customer').length > 0) {
              return 'Yes';
            } else {
              return 'No';
            }
          }
        }
      },
      {
        headerName: 'Reseller',
        field: 'reseller',
        sortable: true,
        filter: 'agSetColumnFilter',
        filterParams: yOrNFilterParams,
        cellRenderer(params: any) {
          if (params.data) {
            if (params.data.organisation_roles != null
              && params.data.organisation_roles.filter(r => r === 'Reseller').length > 0) {
              return 'Yes';
            } else {
              return 'No';
            }
          }
        }
      },
      {
        headerName: 'Installer',
        field: 'installer',
        sortable: true,
        filter: 'agSetColumnFilter',
        filterParams: yOrNFilterParams,
        cellRenderer(params: any) {
          if (params.data) {
            if (params.data.organisation_roles != null
              && params.data.organisation_roles.filter(r => r === 'Installer').length > 0) {
              return 'Yes';
            } else {
              return 'No';
            }
          }
        }
      },
      {
        headerName: 'Maintenance_Mode',
        field: 'maintenance_mode',
        sortable: true,
        filter: 'agSetColumnFilter',
        filterParams: yOrNFilterParams,
        cellRenderer(params: any) {
          if (params.data) {
            if (params.data.organisation_roles != null
              && params.data.organisation_roles.filter(r => r === 'Maintenance_Mode').length > 0) {
              return 'Yes';
            } else {
              return 'No';
            }
          }
        }
      },
      {
        headerName: 'Asset List', field: 'is_asset_list_required', sortable: false, filter: 'agSetColumnFilter', filterParams: filterParams1,
        cellRenderer(params: any) {
          if (params.data) {
            if (params.data.is_asset_list_required) {
              return 'Yes';
            } else
              if (params.data.is_asset_list_required === false) {
                return 'No';
              } else {
                if (!params.data.is_asset_list_required) {
                  return 'N/A';
                }
              }
          }
        },
      },
      {
        headerName: 'Status', field: 'status', sortable: false, filter: 'agSetColumnFilter', floatingFilter: true, flex: 1,
        filterParams: filterParamsStatus,
        cellRenderer(params: any) {
          if (params.data) {
            if (params.data.status) {
              return '<div class="status"><button class="btn-xs active"><span>Active</span></button></div>';
            } else {
              return ' <div class="status"><button class="btn-xs inactive"><span>Inactive</span></button></div>';
            }
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
    this.maxConcurrentDatasourceRequests =1;
    this.infiniteInitialRowCount = 1;
    this.gridOptions = {
      cacheBlockSize: this.pageLimit,
      paginationPageSize: this.pageLimit,
      rowModelType: 'infinite',
    };
    this.frameworkComponents = {
      btnCellRenderer: BtnCellRendererForCompany,
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

  }

  onBtNormalCols() {
    this.gridApi.onFilterChanged();
  }

  getAllcompanies() {
    this.companiesListService.getAllCompanyName().subscribe(
      data => {
        this.companies = data && data.length > 0 ? data : [];
        return this.companies;
      },
      error => {
        console.log(error);
      }
    );
  }

  onCellClicked(params: any) {
    if (params.column.colId === 'organisation_name') {
      if (!this.rolePCTUser) {
        this.router.navigate(['add-company'], { queryParams: { id: params.data.id } });
      }
    }
  }


  onGridReady(params: any) {
    this.paramList = params;
    this.gridApi = params.api;
    this.gridApi.setRowData([]);
    this.gridColumnApi = params.columnApi;
    const gridSavedState = sessionStorage.getItem('company_list_grid_column_state');
    if (gridSavedState) {
      this.gridColumnApi.setColumnState(JSON.parse(gridSavedState));
    }
    const filterSavedState = sessionStorage.getItem('company_list_grid_filter_state');
    if (filterSavedState) {
      this.gridApi.setFilterModel(JSON.parse(filterSavedState));
    }
    const datasource = {
      getRows: (params: IGetRowsParams) => {
        this.sortForAgGrid(params.sortModel);
        this.filterForAgGrid(params.filterModel);
        if (this.isFirst) {
          if (this.column == 'organisationRole') {
            this.filterValuesJsonObj.roleSort = this.roleSort;
          } else {
            this.filterValuesJsonObj.roleSort = null;
          }
          this.loading = true;
          this.companiesListService.getCompaniesWithSort(this.currentPage, this.column, this.order, this.filterValuesJsonObj, this.pageLimit).subscribe(data => {
            // for (const iterator of data.body) {
            //   if (iterator.type.toLowerCase() === 'installer' && iterator.access_list && iterator.access_list.length > 0) {
            //     let shortName = '';
            //     for (const iterator2 of iterator.access_list) {
            //       if (iterator2.short_name) {
            //         shortName = shortName + iterator2.short_name + ',';
            //       }
            //     }
            //     iterator.access_name = shortName;
            //   }
            //}


            this.rowData = data.body;
            this.loading = false;
            this.totalNumberOfRecords = data.total_key;
            if (this.totalNumberOfRecords <= (this.currentPage * this.pageLimit)) {
              this.isFirst = false;
            }
            this.currentPage++;
            params.successCallback(this.rowData, data.total_key);
          }, error => {
            console.log(error);
          });
        }
      }
    };
    this.gridApi.setDatasource(datasource);
  }

  onDragStopped(params: any) {
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
    // this.removefilterValue();
    sessionStorage.setItem('company_list_grid_filter_state', JSON.stringify(params.api.getFilterModel()));
  }

  async removefilterValue(){
    for(let i = 0;i<this.columnDefs.length;i++){
      document.querySelectorAll('[aria-colindex="' + (i+1) + '"]')[1].querySelector('input').value = '';
    }
  }

  onColumnVisible(params: any) {
    this.saveGridStateInSession(this.gridColumnApi.getColumnState());
  }

  onColumnPinned(params: any) {
    this.saveGridStateInSession(this.gridColumnApi.getColumnState());
  }

  onColumnResized(params: any) {
    this.saveGridStateInSession(this.gridColumnApi.getColumnState());
  }

  saveGridStateInSession(columnState) {
    sessionStorage.setItem('company_list_grid_column_state', JSON.stringify(columnState));
  }

  headerHeightSetter(event) {
    this.gridApi.setHeaderHeight(headerHeightGetter() + 20);
    this.gridApi.resetRowHeights();
  }


  filterForAgGrid(filterModel) {
    if (filterModel !== undefined) {
      this.filterValuesJsonObj = {};
      this.filterValues = new Map();
      if (filterModel.organisation_name && filterModel.organisation_name.filter && filterModel.organisation_name.filter !== '') {
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

      if (filterModel.resellers && filterModel.resellers.values) {
        const rList = [];
        for (const iterator of filterModel.resellers.values) {
          rList.push(iterator);
        }
        const v = rList.join(",");
        this.filterValues.set('resellers', v.length == 0 ? 'null' : v);
      }

      if (filterModel.end_customer && filterModel.end_customer.values) {
        if (filterModel.end_customer.values.length === 0) {
          this.filterValues.set('endCustomer', 'null');
        } else {
          for (const iterator of filterModel.end_customer.values) {
            this.filterValues.set('endCustomer', iterator);
          }
        }
      }

      if (filterModel.reseller && filterModel.reseller.values) {
        if (filterModel.reseller.values.length === 0) {
          this.filterValues.set('reseller', 'null');
        } else {
          for (const iterator of filterModel.reseller.values) {
            this.filterValues.set('reseller', iterator);
          }
        }
      }

      if (filterModel.installer && filterModel.installer.values) {
        if (filterModel.installer.values.length === 0) {
          this.filterValues.set('installer', 'null');
        } else {
          for (const iterator of filterModel.installer.values) {
            this.filterValues.set('installer', iterator);
          }
        }
      }

      if (filterModel.maintenance_mode && filterModel.maintenance_mode.values) {
        if (filterModel.maintenance_mode.values.length === 0) {
          this.filterValues.set('maintenance_mode', 'null');
        } else {
          for (const iterator of filterModel.maintenance_mode.values) {
            this.filterValues.set('maintenance_mode', iterator);
          }
        }
      }

      if (filterModel.status && filterModel.status.values) {
        if (filterModel.status.values.length === 0) {
          this.filterValues.set('status', 'null');
        } else {
          for (const iterator of filterModel.status.values) {
            this.filterValues.set('status', iterator);
          }
        }
      }
      if (filterModel.access_name && filterModel.access_name.values) {
        // this.filterValues.set('canView', filterModel.campname.filter);
        if (filterModel.access_name.values.length === 0) {
          this.filterValues.set('canView', 'null');
        } else {
          this.filterValues.set('canView', filterModel.access_name.values.toString());
        }
      }

      if (filterModel.is_asset_list_required && filterModel.is_asset_list_required.values) {
        if (filterModel.is_asset_list_required.values.length === 0) {
          this.filterValues.set('isAssetListRequired', 'null');
        } else {
          this.filterValues.set('isAssetListRequired', filterModel.is_asset_list_required.values.toString());
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
    console.log('sorting data');
    console.log(sortModel);
    this.gridApi.paginationCurrentPage = 0;
    if (sortModel[0] === undefined || sortModel[0] == null || sortModel[0].colId === undefined || sortModel[0].colId == null) {
      return;
    }
    switch (sortModel[0].colId) {
      case 'organisation_name': {
        this.column = 'organisationName';
        this.order = sortModel[0].sort;
        break;
      }
      case 'forwarding_group': {
        this.column = 'forwardingGroupMappers.customerForwardingGroup.name';
        this.order = sortModel[0].sort;
        break;
      }
      case 'resellers': {
        this.column = 'resellerList.organisationName';
        this.order = sortModel[0].sort;
        break;
      }
      case 'end_customer': {
        this.roleSort = 'END_CUSTOMER,' + sortModel[0].sort;
        this.column = 'organisationRole';
        this.order = sortModel[0].sort;
        break;
      }
      case 'reseller': {
        this.roleSort = 'RESELLER,' + sortModel[0].sort;
        this.column = 'organisationRole';
        this.order = sortModel[0].sort;
        break;
      }
      case 'installer': {
        this.roleSort = 'INSTALLER,' + sortModel[0].sort;
        this.column = 'organisationRole';
        this.order = sortModel[0].sort;
        break;
      }
      case 'maintenance_mode': {
        this.roleSort = 'MAINTENANCE_MODE,' + sortModel[0].sort;
        this.column = 'organisationRole';
        this.order = sortModel[0].sort;
        break;
      }
      case 'short_name': {
        this.column = 'shortName';
        this.order = sortModel[0].sort;
        break;
      }
      case 'type': {
        this.column = 'type';
        this.order = sortModel[0].sort;
        break;
      }
      case 'status': {
        this.column = 'isActive';
        this.order = sortModel[0].sort;
        break;
      }
      case 'is_asset_list_required': {
        this.column = 'isAssetListRequired';
        this.order = sortModel[0].sort;
        break;
      }
      default: {
        console.log('default');
      }
    }
  }

  getClearFilterFloatingComponent() {
    function ClearFloatingFilter() { }
    ClearFloatingFilter.prototype.init = function (params: any) {
      this.eGui = document.createElement('div');
      this.eGui.innerHTML =
        '<button class="btnClear primary_buttonClear" (click)="clearFilter()">Clear Filters</button>';
      this.currentValue = null;
      this.clearFilterButton = this.eGui.querySelector('button');
      function onClearFilterButtonClicked() {
        this.column = 'organisationName';
        this.order = 'asc';
        params.api.setFilterModel(null);
        params.api.onFilterChanged();
        sessionStorage.setItem('company_list_grid_filter_state', null);
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

  createCompany(): void {
    this.router.navigate(['add-company']);
  }

  reloadComponent() {
    const currentUrl = this.router.url;
    this.router.navigateByUrl('/', { skipLocationChange: true }).then(() =>
      this.router.navigate([currentUrl]));
  }

  inactiveCompany(company): void {
    this.companiesListService.softDeleteCompanyByUuid(company.uuid).subscribe(data => {
      this.toastr.success('Company Inactive Successfully', null, { toastComponent: CustomSuccessToastrComponent });
      this.reloadComponent();
    }, error => {
      console.log(error);
      // this.reloadComponent();
    });
  }

  getForwardingGroupsFilterValues() {
    return this.forwardingGroups;
  }

  getCompanyFilterValues() {
    return this.companies;
  }

  getAllForwardingGroups() {
    this.companiesListService.getAllCustomerForwardingGroupName().subscribe(data => {
      this.forwardingGroups = data;
    }, error => {
      console.log(error);
    });
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


const filterParams = {
  values(params: any) {
    setTimeout(function () {
      params.success(['END_CUSTOMER', 'MANUFACTURER', 'INSTALLER', 'RESELLER', 'PCT']);
    }, 3000);
  },
};

const filterParams1 = {
  values(params: any) {
    setTimeout(function () {
      params.success(['Yes', 'No']);
    }, 2000);
  },
};

const yOrNFilterParams = {
  values(params: any) {
    setTimeout(function () {
      params.success(['Yes', 'No']);
    }, 2000);
  },
};


const filterParamsStatus = {
  values(params: any) {
    setTimeout(function () {
      params.success(['Active', 'Inactive']);
    }, 3000);
  },
};
