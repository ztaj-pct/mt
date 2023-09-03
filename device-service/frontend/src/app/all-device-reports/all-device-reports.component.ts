import { AgGridAngular } from '@ag-grid-community/angular';
import { ClientSideRowModelModule, ColumnApi, ColumnGroupOpenedEvent, ColumnsToolPanelModule, GridApi, GridOptions, IGetRowsParams, MenuModule, Module, MultiFilterModule, RowGroupingModule } from '@ag-grid-enterprise/all-modules';
import { DatePipe, DecimalPipe, formatNumber } from '@angular/common';
import { Component, Inject, LOCALE_ID, OnInit, ViewChild } from '@angular/core';
import { FormBuilder, FormControl, FormGroup } from '@angular/forms';
import { Router } from '@angular/router';
import { ATCommandService } from '../atcommand.service';
import { CommonData } from '../constant/common-data';
import { BtnCellRendererForRawReport } from '../device-profile/btn-cell-renderer-rr.component';
import { BtnCellRendererForDeviceReport } from '../device-profile/btn-cell-renderer.component';
import { GatewayListService } from '../gateway-list.service';
import { BtnCellRendererForGatewayDetails } from '../gateway-list/btn-cell-renderer.component';
import { Filter } from '../models/Filter.model';
import { CustomDateComponent } from '../ui-elements/custom-date-component.component';
import { CustomHeader } from '../ui-elements/custom-header.component';
import { PaginationConstants } from '../util/paginationConstants';
declare var $: any;
import * as moment from 'moment';
import { BtnCellRendererForRTC } from '../device-profile/btn-cell-renderer-rtc.component';
import { filter, map } from 'rxjs/operators';
import { element } from 'protractor';
import { ConditionalExpr } from '@angular/compiler';
import { iteratorToArray } from '@angular/animations/browser/src/util';
import { MatOption } from '@angular/material';
import * as _ from 'underscore';
import { CustomDatePipe } from '../pipes/custom-date.pipe';
import { ToastrService } from 'ngx-toastr';
import { CustomSuccessToastrComponent } from '../custom-success-toastr/custom-success-toastr.component';
import { any, last } from 'underscore';
import { interval } from 'rxjs';
import { CustomErrorToastrComponent } from '../custom-error-toastr/custom-error-toastr.component';
declare var google: any;
interface BodyScrollEvent {
  direction: ScrollDirection;
  left: number;
  top: number;
  api: GridApi;
  columnApi: ColumnApi;
  // Event identifier
  type: string;
}

type ScrollDirection = 'horizontal' | 'vertical';
@Component({
  selector: 'app-all-device-reports',
  templateUrl: './all-device-reports.component.html',
  styleUrls: ['./all-device-reports.component.css']
})
export class AllDeviceReportsComponent implements OnInit {
  // isFilterClick = false;



  @ViewChild('picker') picker: any;
  qaDateType = 'Any';
  customerName = 'Any';
  imeiFilter = false;
  assetFilter = false;
  customerFilter = false;
  reportFilter = false;
  eventFilter = false;
  groupFilter = false;
  qaDateFilter = false;
  sensorFilter = false;
  deviceTypeFilter = false;
  sourceFilter = false;
  filterForm: FormGroup;
  imageUrl: any;
  today = new Date();
  from:any;
  to:any;
  // allDeviceReportFilterForm: FormGroup;
  deviceTypeList: any[] = CommonData.CAMPAIGN_DEVICE_TYPE_DATA;
  colorCode: any[] = CommonData.COLOR_CODE;
  dateRangeType: any[] = CommonData.DATE_RANGE_DATA;
  exportAllDeviceReportColumnName = CommonData.ALL_DEVICE_REPORT_COLUMN_FOR_EXPORT;
  customerList: any[] = [];
  recentCustomerList: any[] = [];
  advanceOption = false;
  range = 10000;
  // table data
  frameworkComponents;
  userCompanyType;
  isSuperAdmin;
  isRoleCustomerManager;
  isGroupColumn = true;
  filterValues = new Map();
  filterValuesJsonObj: any = {};
  loading = false;
  sortFlag;
  currentPage = 0;
  showingpage;
  totalpage;
  gatewayList: any[] = [];
  totalRecords = 0;
  currentEndRecord = 0;
  currentStartRecord = 0;
  pagedItems: any[];
  private allItems: any;
  pageLimit = 50;
  totalNumberOfRecords = 0;
  pager: any = {};
  sortByOfAssets = false;
  sortByCompany = false;
  sortCreatedOn = false;
  sortByCreatedBy = false;
  sortByLastUpdatedBy = false;
  sortByLastUpdateOn = false;
  mapViewHideShow = false;
  pageSizeOptions: PaginationConstants = new PaginationConstants();
  pageSizeObjArr = this.pageSizeOptions.pageSizeObjArr;
  column = 'general_mask_fields.received_time_stamp';
  order = 'desc';
  public sideBar;
  columnData: any;
  eventNameList: any;
  customer = new Map();
  // ag-grid code
  @ViewChild('myGrid') myGrid: AgGridAngular;
  public gridOptions: Partial<GridOptions>;
  public gridApi;
  public columnDefs;
  public columnDefs2;
  public gridColumnApi;
  public defaultColDef;
  public paramList;
  public components;
  groupHeaderHeight = 40;
  headerHeight = 40;
  floatingFiltersHeight = 40;
  rowData: any;
  mapRowData: any;
  mapRowDatas: any = {};
  mainMapRowData: any = [];
  latestRawReport: string;
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
  filterEValues = new Map();
  userNameList: string[];
  companies: any[];
  company: any;
  selectedCompany = new FormControl();
  selectDays = new FormControl(0);
  companyUuid = '1';
  companyname: any;
  days = 0;
  selectedtoDateRange = [];
  eventList: any;
  organisationList: any;
  selected: number;
  selectedIdsToDelete: string[] = [];
  isChecked = false;
  elasticFilter: Filter = new Filter();
  elasticRowData: any[];
  dateIsReadOnly = false;
  isFirst = true;
  counts = {};
  selectAll = false;
  lat = 47.751076;
  lng = -120.740135;
  zoom = 4;
  latInDms: any;
  lngInDms: any;
  marker: any;
  url = { url: 'assets/image/placeholder.png', scaledSize: { height: 20, width: 20 } };
  public maxConcurrentDatasourceRequests = 2;
  public cacheOverflowSize = 2;
  public infiniteInitialRowCount = 1;
  public overlayNoRowsTemplate =
    '<span style="padding: 10px; border: 2px solid #444; background: lightgoldenrodyellow;">No data in the table</span>';

  @ViewChild('allSelected') private allSelected: MatOption;

  correctEvent = true;
  lastSelectedRow: any;
  updateSubscription: any;
  constructor(
    @Inject(LOCALE_ID) private locale: string,
    private readonly gatewayListService: GatewayListService,
    private readonly formBuldier: FormBuilder,
    private readonly router: Router,
    private readonly atCommandService: ATCommandService,
    private toastr: ToastrService,
    private readonly datepipe: DatePipe) {
    const datas = JSON.parse(localStorage.getItem('recentSearchList'));
    const hideColumn = JSON.parse(sessionStorage.getItem('HideColumn'));
    if (hideColumn !== null && hideColumn !== undefined) {
      this.isGroupColumn = hideColumn;
    }
    if (datas && datas.length > 0) {
      this.recentCustomerList = datas;
    }
    this.getReportColumnDefs();
    // if (sessionStorage.getItem('gatewayListsize') !== undefined) {
    //   this.pageLimit = Number(sessionStorage.getItem('gatewayListsize'));
    // } else {
    this.pageLimit = 50;
    // }
    // this.gridOptions = {
    //   cacheBlockSize: this.pageLimit,
    //   paginationPageSize: this.pageLimit,
    //   rowModelType: 'infinite',
    //   onBodyScroll: (event: BodyScrollEvent) => {
    //     if (event.direction === 'horizontal') {
    //       this.autoSizeAll(false);
    //     }
    //   },
    // };

    this.gridOptions = {
      cacheBlockSize: this.pageLimit,
      paginationPageSize: this.pageLimit,
      rowModelType: 'infinite',
      onBodyScroll: (event: BodyScrollEvent) => {
        if (event.direction === 'horizontal') {
          this.autoSizeAll(false);
        }
      },
      onColumnGroupOpened: (event: ColumnGroupOpenedEvent) => {
        this.autoSizeAll(false);
      }
    };

    this.frameworkComponents = {
      btnCellRenderer: BtnCellRendererForGatewayDetails,
      measureCellRenderer: BtnCellRendererForDeviceReport,
      rawReportCellRenderer: BtnCellRendererForRawReport,
      rtcCellRenderer: BtnCellRendererForRTC,
      agDateInput: CustomDateComponent,
      // agColumnHeader: CustomHeader
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
            suppressMenu: true,
            suppressColumnExpandAll: false
          },
        },
      ],
      defaultToolPanel: '',
    };
  }

  ngOnInit() {
    // this.initializeForm();
    const data: any = JSON.parse(sessionStorage.getItem('all_report_advance_save_filter'));
    this.initilizeFilterForm(data);
    this.getAllCustomer();
    this.getAllOrganisation();
    this.updateSubscription = interval(120000).subscribe(
      (val) => { this.refresh(); }
    );
  }

  getAllEvent() {
    this.gatewayListService.getEventList().subscribe(
      data => {
        this.eventList = data.body;
        const eventNameList = [];
        for (const iterator of this.eventList) {
          eventNameList.push(iterator.eventType);
        }
        return eventNameList;
      }
    );
  }

  getAllOrganisation() {
    this.gatewayListService.getOrganisationList().subscribe(
      data => {
        this.organisationList = data;
      }
    );
  }

  getAllCustomer(name?: any) {
    if (!name) {
      name = '';
    }
    this.gatewayListService.getAllCustomerObject(['EndCustomer'], name).subscribe(
      data => {
        const recentCustomer = JSON.parse(localStorage.getItem('recentSearch'));
        const recentCustomer2 = JSON.parse(localStorage.getItem('recentSearch2'));
        if (data !== undefined && data != null && data.length > 0) {
          if (recentCustomer) {
            if (recentCustomer2 && recentCustomer2.length === 1) {
              const index = data.map(function (item) { return item.accountNumber; }).indexOf(recentCustomer2[0]);
              if (index !== -1) {
                this.customerName = data[index].organisationName;
              }
            }
            let i = 0;
            for (const iterator of recentCustomer) {
              if (i < 3) {
                const index = data.map(function (item) { return item.accountNumber; }).indexOf(iterator);
                const index2 = this.recentCustomerList.map(function (item) { return item.accountNumber; }).indexOf(iterator);
                if (index !== -1 && index2 === -1) {
                  this.recentCustomerList.push(data[index]);
                  if (this.recentCustomerList.length > 3) {
                    this.recentCustomerList.shift();
                  }
                  data.splice(index, 1);
                  i++;

                }
              } else {
                break;
              }
            }
            localStorage.setItem('recentSearchList', JSON.stringify(this.recentCustomerList));
          }

          this.customerList = data;
        }
      },
      error => {
        console.log(error);
      }
    );
  }
  onDateChange(event: any, which: any) {
    this.from = this.filterForm.controls.from.value;
    this.to = this.filterForm.controls.to.value ;
    console.log(event);
    if (this.correctEvent) {
      this.filterForm.controls.date_rang.setValue('time_rang');
      this.qaDateType = 'Range';
      this.dateIsReadOnly = false;
    } else {
      if (which === 'TO') {
        this.correctEvent = true;
       
      }
    }
    this.advanceFilter();
  }

  hideMapView() {
    this.mapViewHideShow = false;
    document.getElementById('btn1').classList.remove('btnDeactivated');
    document.getElementById('btn2').classList.add('btnDeactivated');
  }

  selectChangeHandler(event: any, correct?: boolean) {
    // this.dateIsReadOnly = true;
    this.correctEvent = false;
    switch (event.value) {
      case 'last_hour':
        this.filterForm.controls.from.setValue(new Date(new Date().getTime() - (1000 * 60 * 60)));
        this.filterForm.controls.to.setValue(new Date());
        this.qaDateType = 'Last Hour';
        break;
      case 'last_24_hour':
        this.filterForm.controls.from.setValue(new Date(new Date().getTime() - (1000 * 60 * 60 * 24)));
        this.filterForm.controls.to.setValue(new Date());
        this.qaDateType = 'Last24Hour';
        break;
      case 'last_week':
        this.filterForm.controls.from.setValue(new Date(new Date().getTime() - (1000 * 60 * 60 * 24 * 7)));
        this.filterForm.controls.to.setValue(new Date());
        this.qaDateType = 'Last Week';
        break;
      case 'last_month':
        this.filterForm.controls.from.setValue(new Date(new Date().getTime() - (1000 * 60 * 60 * 24 * 30)));
        this.filterForm.controls.to.setValue(new Date());
        this.qaDateType = 'Last Month';
        break;
      case 'last_year':
        this.filterForm.controls.from.setValue(new Date(new Date().getTime() - (1000 * 60 * 60 * 24 * 365)));
        this.filterForm.controls.to.setValue(new Date());
        this.qaDateType = 'Last Year';
        break;
      case 'time_rang':
        this.qaDateType = 'Range';
        this.dateIsReadOnly = false;
        break;
      case '':
        this.filterForm.controls.from.setValue('');
        this.filterForm.controls.to.setValue('');
        break;
      default:
        break;
    }
    this.advanceFilter();
  }

  selectCustomerChangeHandler(event: any) {
    console.log(event.value);
    if (event.value.length === 1 && event.value[0] !== 0) {
      const obj = this.customerList.find(item => item.accountNumber === event.value[0]);
      if (obj) {
        this.customerName = obj.organisationName;
      }
      const obj2 = this.recentCustomerList.find(item => item.accountNumber === event.value[0]);
      if (obj2) {
        this.customerName = obj2.organisationName;
      }
    }
    this.advanceFilter();
  }


  // table methods
  uploadgateways() {
    this.router.navigate(['upload-devices']);
  }

  openExportPopup() {
    $('#exportCSVModal').modal('show');
  }



  exportDeviceList() {
    const exportColumnList = [];
    for (const iterator of this.gridColumnApi.getColumnState()) {
      if (!iterator.hide) {
        const obj = this.exportAllDeviceReportColumnName.find(item => item.dbName === iterator.colId);
        if (obj) {
          exportColumnList.push(obj.headerName);
        }
      }
    }
    const exportObj = {
      filter_value: this.filterValuesJsonObj,
      header_name: exportColumnList
    };
    if (this.range > 10000) {
      this.gatewayListService.exportData(0, this.column, this.order, this.range, exportObj).subscribe(data => {
        this.toastr.success('Data Exported File sent to mail', null, { toastComponent: CustomSuccessToastrComponent });
      }, error => {
      });
    } else {
      this.loading = true;
      this.gatewayListService.exportDataInLocal(0, this.column, this.order, this.range, exportObj).subscribe(data => {
        this.loading = false;
        this.toastr.success('Data Exported', null, { toastComponent: CustomSuccessToastrComponent });
      }, error => {
        this.loading = false;
      });
    }
    // this.gridOptions.api.exportDataAsCsv();

  }


  onBtNormalCols() {
    this.gridApi.onFilterChanged();
  }


  saveGridStateInSession(columnState) {
    console.log("============= SAVED STATE =============");
    // console.log(columnState);
    sessionStorage.setItem('all_device_report_grid_column_state', JSON.stringify(columnState));
  }

  onColumnGroupOpened(params: any) {
    console.log("my column group open event called")
    sessionStorage.setItem('all_device_report_grid_column_group_state', JSON.stringify(this.gridColumnApi.getColumnGroupState()));
  }



  onRowSelected(event) {
    this.mapRowData = this.getSelectedRowData();
    this.mapRowDatas = [];
    if (this.mapRowData.length > 1) {
      this.zoom = 4;
    } else {
      this.zoom = 7;
    }
    for (const iterator of this.mapRowData) {
      if (!this.mapRowDatas[iterator.report_header.device_id]) {
        if (this.mapRowDatas[iterator.report_header.device_id] === undefined) {
          this.mapRowDatas[iterator.report_header.device_id] = [];
        }
        this.mapRowDatas[iterator.report_header.device_id].push(iterator);
      } else {
        this.mapRowDatas[iterator.report_header.device_id].push(iterator);
      }
      // this.mainMapRowData.push(this.mapRowDatas);
    }

    if (Object.keys(this.mapRowDatas).length == 1) {
      Object.keys(this.mapRowDatas).forEach(element => {
        this.lastSelectedRow = [];
        let dataArray = this.mapRowDatas[element];
        this.lastSelectedRow = dataArray[dataArray.length - 1];
      });
      if (this.lastSelectedRow && this.lastSelectedRow.general_mask_fields) {
        this.lat = this.lastSelectedRow.general_mask_fields.latitude;
        this.lng = this.lastSelectedRow.general_mask_fields.longitude; this.convertDMS(this.lastSelectedRow.general_mask_fields.latitude, this.lastSelectedRow.general_mask_fields.longitude)
        this.getGeoLocation(this.lastSelectedRow.general_mask_fields.latitude, this.lastSelectedRow.general_mask_fields.longitude);
      }
    }

    //   let i = 0;

    //   Object.keys(this.mapRowDatas).forEach(element => {
    //     this.mapRowDatas[element].forEach(element1 => {
    //       console.log(element1);
    //   });
    // });
    // for (iterator of ) {
    //   console.log("outer loop is " + i, iterator);
    //   i++;
    //   // 
    //   iterator.forEach((value: string, key: string) => {
    //     console.log(key, value);
    // });
    // }
  }



  convertDMS(lat, lng) {
    let latitude = this.toDegreesMinutesAndSeconds(lat);
    let latitudeCardinal = lat >= 0 ? 'N' : 'S';
    let longitude = this.toDegreesMinutesAndSeconds(lng);
    let longitudeCardinal = lng >= 0 ? 'E' : 'W';
    console.log("DMS frmate is", latitude + " " + latitudeCardinal, "\n" + longitude + " " + longitudeCardinal)
    // return latitude + " " + latitudeCardinal + "\n" + longitude + " " + longitudeCardinal;
    this.latInDms = latitude + ' ' + latitudeCardinal;
    this.lngInDms = longitude + ' ' + longitudeCardinal;
  }

  toDegreesMinutesAndSeconds(coordinate) {
    let absolute = Math.abs(coordinate);
    let degrees = Math.floor(absolute);
    let minutesNotTruncated = (absolute - degrees) * 60;
    let minutes = Math.floor(minutesNotTruncated);
    let seconds = ((minutesNotTruncated - minutes) * 60);
    return degrees + '° ' + minutes + '\' ' + seconds.toFixed(1) + '\'\'';
  }


  getGeoLocation(lat: number, lng: number) {
    if (navigator.geolocation) {
      let geocoder = new google.maps.Geocoder();
      let latlng = new google.maps.LatLng(lat, lng);
      let request = {
        latLng: latlng
      };
      geocoder.geocode(request, (results, status) => {
        if (status === google.maps.GeocoderStatus.OK) {
          this.marker = results[0].formatted_address;
          // let result = results[0].formatted_address;
          // let rsltAdrComponent = result.address_components;
          // if (result != null) {
          //   this.marker.buildingNum = rsltAdrComponent.find(x => x.types === 'street_number').long_name;
          //   this.marker.streetName = rsltAdrComponent.find(x => x.types === 'route').long_name;
          //   console.log(this.marker);
          // } else {
          //   alert("No address available!");
          // }
        }
      });
    }
  }

  onRangeSelectionChanged(params: any) {
    const cellRanges = this.gridApi.getCellRanges();
    let startIndex;
    let endIndex;
    cellRanges.forEach(element => {
      startIndex = element.startRow.rowIndex;
      endIndex = element.endRow.rowIndex;
    });
    this.gridApi.forEachNode(function (node) {
      if (node.rowIndex >= startIndex && node.rowIndex <= endIndex) {
        node.setSelected(true);
      } else {
        node.setSelected(false);
      }
    });
  }

  showMapView() {
    this.mapViewHideShow = true;
    document.getElementById('btn2').classList.remove('btnDeactivated');
    document.getElementById('btn1').classList.add('btnDeactivated');
    this.currentPage = 0;
    const filterParamsForEvent = {
      values: params => params.success(this.eventNameList)
    };
    this.columnDefs2 = [
      {
        headerName: '#', minWidth: 20, width: 20, headerCheckboxSelection: true, headerCheckboxSelectionFilteredOnly: true, filter: false, sortable: false,
        checkboxSelection: true, lockPosition: 'right'
      },
      {
        field: 'general.company_id',
        filter: false,
        sortable: false,
        filterParams: {
          filterOptions: [
            'equals',
            'not equal'
          ],
          suppressAndOrCondition: true
        },
        headerName: 'Customer',
        lockPosition: 'right',
        cellClass: 'locked-col',
        suppressColumnsToolPanel: true
      },
      {
        field: 'report_header.device_id',
        filter: 'agTextColumnFilter',
        sortable: true,
        unSortIcon: true,
        suppressMovable: true,
        cellClass: 'suppress-movable-col',
        suppressColumnsToolPanel: true,
        filterParams: {
          filterOptions: [
            'contains',
            'equals',
            'not equal'
          ],
          suppressAndOrCondition: true
        },
        headerName: 'Device ID'
      },
      {
        field: 'report_header.event_name',
        filter: 'agSetColumnFilter',
        sortable: false,
        filterParams: filterParamsForEvent,
        headerName: 'Event',
        cellClass: 'locked-col',
        suppressColumnsToolPanel: true,
        maxWidth: 140,
      },
      {
        field: 'general_mask_fields.received_time_stamp',
        filter: 'agDateColumnFilter',
        sortabl: true,
        unSortIcon: true,
        suppressMovable: true,
        cellClass: 'suppress-movable-col',
        suppressColumnsToolPanel: true,
        filterParams: {
          suppressAndOrCondition: true
        },
        headerName: 'Received Time'
      }
    ];
  }

  columnGroups() {
    const cutomeDatepipe = new CustomDatePipe();
    // this.gatewayListService.getColumnDefs().subscribe(data => {
    const filterParamsForEvent = {
      values: params => params.success(this.eventNameList)
    };
    if (this.isGroupColumn === false) {
      const reportColDefs = JSON.parse(this.columnData[3].reportColumnDefs);
      reportColDefs.forEach(element => {
        if (element) {
          if (element.headerName === 'Received Time' || element.headerName === 'GPS Time' || element.headerName === 'RTC Time') {
            element.cellRenderer = (data2) => {
              if (data2) {
                if (data2.value) {
                  const newDate = new Date(data2.value);
                  const newDate2 = new Date('2000-01-01 00:00:00.00000');
                  if (newDate < newDate2) {
                    return 'N/A';
                  } else {
                    return cutomeDatepipe.transform(data2.value, 'report');
                  }
                } else {
                  // console.log(data2);
                  // return 'N/A';
                }
              }
            };
          }
          if (element.headerName === 'Event') {
            element.filterParams = filterParamsForEvent;
          }
          if (element.headerName === 'Thumbnail') {
            element.cellRenderer = (data2) => {
              if (data2 && data2.value && data2.value.length > 0) {
                if (data2.value.includes('http')) {
                  return '<img width="100%" height="100px" style="pointer-events: none;" src="' + data2.value + '">';
                } else if (data2.data.general.company_id === 'Amazon' || data2.data.general.company_id === 'amazon') {
                  return '<img width="100%" height="100px" style="pointer-events: none;" src="http://images.phillips-connect.net/' + data2.value + '">';
                }
              }
            };
          }
          if (element.headerName === 'GPS') {
            element.cellRenderer = (params) => {
              if (params.data && params.data.general_mask_fields && params.data.general_mask_fields.gpsstatus_index) {
                if (params.data.general_mask_fields.gpsstatus_index !== 'Locked') {
                  return '<div style="font-style: italic;">' + params.data.general_mask_fields.gpsstatus_index + '</div>';
                } else {
                  return params.data.general_mask_fields.gpsstatus_index;
                }
              } else {
                return '';
              }
            };
          }
          if (element.headerName === 'Seq #') {
            element.cellRenderer = (params) => {
              if (params.data && params.data.report_header && (params.data.report_header.sequence || params.data.report_header.sequence === 0)) {
                if (this.counts[params.data.report_header.device_id][params.data.report_header.sequence] > 1) {
                  return '<div style="background:#EDD3D3">' + params.data.report_header.sequence + '</div>';
                } else {
                  return params.data.report_header.sequence;
                }
              } else {
                return '<div style="background:#FEF5BD"> &nbsp; </div>';
              }
            };
          }

          if (element.headerName === 'Raw Report') {
            element.cellRenderer = (params) => {
              if (params.data && params.data.general && params.data.general.rawreport) {
                return '<div style="font-family: monospace;"> ' + params.data.general.rawreport + ' </div>';
              }
            };
          }
          if (element.headerName === 'Device ID') {
            element.cellRenderer = (params) => {
              if (params.data && params.data.report_header && params.data.report_header.device_id) {
                return '<div class="pointer" style="color:skyblue;"><a> ' + params.data.report_header.device_id + ' </a></div>';
              }
            };
          }
        }
      });
      sessionStorage.setItem('HideColumn', JSON.stringify(this.isGroupColumn));
      this.isGroupColumn = true;
      this.columnDefs = reportColDefs;
      const columState = JSON.parse(sessionStorage.getItem('all_device_report_grid_column_state'));
      const columGroupState = JSON.parse(sessionStorage.getItem('all_device_report_grid_column_group_state'));
      const filterSavedState = sessionStorage.getItem('all_device_report_grid_filter_state');

      if (columState) {
        setTimeout(() => {
          this.gridColumnApi.applyColumnState({ state: columState, applyOrder: true, });
        }, 300);
      }

      if (columGroupState) {
        setTimeout(() => {
          this.gridColumnApi.setColumnGroupState(columGroupState);
        }, 300);
      }
      if (filterSavedState) {
        setTimeout(() => {
          this.gridApi.setFilterModel(JSON.parse(filterSavedState));
        }, 1000);
      }
      setTimeout(() => {
        this.autoSizeAll(false);
      }, 1000);
    } else {
      const reportColDefs = JSON.parse(this.columnData[4].reportColumnDefs);
      reportColDefs.forEach(element => {
        if (element.children) {
          element.children.forEach(elements => {
            if (elements.headerName === 'Received Time' || elements.headerName === 'GPS Time' || elements.headerName === 'RTC Time') {
              elements.cellRenderer = (data2) => {
                if (data2) {
                  if (data2.value) {
                    const newDate = new Date(data2.value);
                    const newDate2 = new Date('2000-01-01 00:00:00.00000');
                    if (newDate < newDate2) {
                      return 'N/A';
                    } else {
                      return cutomeDatepipe.transform(data2.value, 'report');
                    }
                  } else {
                    // console.log(data2);
                    // return 'N/A';
                  }
                }
              };
            }
            if (elements.headerName === 'Event') {
              elements.filterParams = filterParamsForEvent;
            }
            if (elements.headerName === 'Thumbnail') {
              elements.cellRenderer = (data2) => {
                // if (data2 && data2.value && data2.value.length > 38 && data2.data.general.company_id !== 'Amazon' && data2.data.general.company_id !== 'amazon') {
                //   return '<img width="100%" height="100px" src="' + data2.value + '">';
                // } else if (data2.value && data2.data.general.company_id !== 'Amazon' && data2.data.general.company_id !== 'amazon') {
                //   return data2.value;
                // } else {
                //   return '';
                // }
                if (data2 && data2.value && data2.value.length > 0) {
                  if (data2.value.includes('http')) {
                    return '<img width="100%" height="100px" src="' + data2.value + '">';
                  } else if (data2.data.general.company_id === 'Amazon' || data2.data.general.company_id === 'amazon') {
                    return '<img width="100%" height="100px" src="http://images.phillips-connect.net/' + data2.value + '">';
                  }
                }
              };
            }
            if (elements.headerName === 'GPS') {
              elements.cellRenderer = (params) => {
                if (params.data && params.data.general_mask_fields && params.data.general_mask_fields.gpsstatus_index) {
                  if (params.data.general_mask_fields.gpsstatus_index !== 'Locked') {
                    return '<div style="font-style: italic;">' + params.data.general_mask_fields.gpsstatus_index + '</div>';
                  } else {
                    return params.data.general_mask_fields.gpsstatus_index;
                  }
                } else {
                  return '';
                }
              };
            }
            if (elements.headerName === 'Seq #') {
              elements.cellRenderer = (params) => {
                if (params.data && params.data.report_header && (params.data.report_header.sequence || params.data.report_header.sequence === 0)) {
                  if (this.counts[params.data.report_header.device_id] && this.counts[params.data.report_header.device_id][params.data.report_header.sequence] > 1) {
                    return '<div style="background:#EDD3D3">' + params.data.report_header.sequence + '</div>';
                  } else {
                    return params.data.report_header.sequence;
                  }
                } else {
                  return '<div style="background:#FEF5BD"> &nbsp; </div>';
                }
              };
            }
            if (element.headerName === 'Device ID') {
              element.cellRenderer = (params) => {
                if (params.data && params.data.report_header && params.data.report_header.device_id) {
                  return '<div class="pointer" style="color:skyblue;"><a>' + params.data.report_header.device_id + ' </a></div>';
                }
              };
            }
          });
        }
      });
      sessionStorage.setItem('HideColumn', JSON.stringify(this.isGroupColumn));
      this.isGroupColumn = false;
      this.columnDefs = reportColDefs;
      const columState = JSON.parse(sessionStorage.getItem('all_device_report_grid_column_state'));
      const columGroupState = JSON.parse(sessionStorage.getItem('all_device_report_grid_column_group_state'));
      const filterSavedState = sessionStorage.getItem('all_device_report_grid_filter_state');

      if (columState) {
        setTimeout(() => {
          this.gridColumnApi.applyColumnState({ state: columState, applyOrder: true, });
        }, 300);
      }
      if (columGroupState) {
        setTimeout(() => {
          this.gridColumnApi.setColumnGroupState(columGroupState);
        }, 300);
      }
      if (filterSavedState) {
        setTimeout(() => {
          this.gridApi.setFilterModel(JSON.parse(filterSavedState));
        }, 1000);
      }
      setTimeout(() => {
        this.autoSizeAll(false);
      }, 1000);
    }
    // },
    //   (error: any) => {
    //     console.log(error);
    //   });
  }

  selectAllRow(event) {
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
  }

  getSelectedRowData() {
    return this.gridApi.getSelectedNodes().map(node => node.data);
  }


  filterForAgGrid(filterModel) {
    this.gridApi.paginationCurrentPage = 0;
    this.filterValuesJsonObj = {};
    this.filterEValues = new Map();
    if (filterModel !== undefined && filterModel != null) {
      const elasticModelFilters = JSON.parse(JSON.stringify(filterModel));
      filterModel = elasticModelFilters;
      const Okeys = Object.keys(filterModel);
      let index = 0;
      for (let i = 0; i < Okeys.length; i++) {
        this.elasticFilter.key = Okeys[i];

        if (Okeys[i] === 'general_mask_fields.gps_time') {
          this.elasticFilter.key = 'general_mask_fields.gps_time.keyword';
        }
        if (this.elasticFilter.key === 'report_header.event_name' && filterModel[Okeys[i]].filterType === 'set') {
          this.elasticFilter.operator = 'term';
          const values = [];
          if (filterModel[Okeys[i]].values && filterModel[Okeys[i]].values.length > 0) {
            if (filterModel[Okeys[i]].values) {
              for (const iterator of filterModel[Okeys[i]].values) {
                const obj = this.eventList.find(item => item.eventType === iterator);
                if (obj) {
                  values.push(obj.eventId);
                }
              }

            }
          } else {
            values.push(0);
          }
          this.elasticFilter.key = 'report_header.event_id';
          this.elasticFilter.value = values.toString();
          this.filterEValues.set('filterValues ' + i, this.elasticFilter);
        }
        if (this.elasticFilter.key === 'general_mask_fields.gpsstatus_index' && filterModel[Okeys[i]].filterType === 'set' && filterModel[Okeys[i]].values) {
          this.elasticFilter.operator = 'term';
          this.elasticFilter.key = 'general_mask_fields.gpsstatus_index';
          const values = [];
          for (const iterator of filterModel[Okeys[i]].values) {
            if (iterator === 'Locked') {
              values.push('1');
            }
            if (iterator === 'Unlocked') {
              values.push('0');
            }
          }

          this.elasticFilter.value = values.toString();
          this.filterEValues.set('filterValues ' + i, this.elasticFilter);
        }
        if (filterModel[Okeys[i]].type === 'contains') {
          this.elasticFilter.operator = 'like';
          this.elasticFilter.value = filterModel[Okeys[i]].filter;
          this.filterEValues.set('filterValues ' + i, this.elasticFilter);
        } else if (filterModel[Okeys[i]].type === 'equals') {
          if (filterModel[Okeys[i]].filterType === 'date') {
            this.elasticFilter.operator = 'date';
            this.elasticFilter.value = filterModel[Okeys[i]].dateFrom;
          } else {
            this.elasticFilter.operator = 'eq';
            this.elasticFilter.value = filterModel[Okeys[i]].filter;
          }
          this.filterEValues.set('filterValues ' + i, this.elasticFilter);
        } else if (filterModel[Okeys[i]].type === 'not equal' || filterModel[Okeys[i]].type === 'notEqual') {
          if (filterModel[Okeys[i]].filterType === 'date') {
            this.elasticFilter.operator = 'notequal';
            this.elasticFilter.value = filterModel[Okeys[i]].dateFrom;
          } else {
            this.elasticFilter.operator = 'notequal';
            this.elasticFilter.value = filterModel[Okeys[i]].filter;
          }
          this.filterEValues.set('filterValues ' + i, this.elasticFilter);
        } else if (filterModel[Okeys[i]].type === 'inRange') {
          this.elasticFilter.operator = 'gt';
          this.elasticFilter.value = filterModel[Okeys[i]].dateFrom;
          this.filterEValues.set('filterValues ' + i + 'a', this.elasticFilter);
          this.elasticFilter = new Filter();
          this.elasticFilter.key = Okeys[i];
          if (Okeys[i] === 'general_mask_fields.gps_time') {
            this.elasticFilter.key = 'general_mask_fields.gps_time.keyword';
          }
          this.elasticFilter.operator = 'lt';
          this.elasticFilter.value = filterModel[Okeys[i]].dateTo;
          this.filterEValues.set('filterValues ' + i + 'b', this.elasticFilter);
        } else if (filterModel[Okeys[i]].type === 'greaterThan') {
          this.elasticFilter.operator = 'gt';
          this.elasticFilter.value = filterModel[Okeys[i]].dateFrom;
          this.filterEValues.set('filterValues ' + i + 'a', this.elasticFilter);
        } else if (filterModel[Okeys[i]].type === 'lessThan') {
          this.elasticFilter.operator = 'lt';
          this.elasticFilter.value = filterModel[Okeys[i]].dateFrom;
          this.filterEValues.set('filterValues ' + i + 'a', this.elasticFilter);
        }
        // this.filterEValues.set('filterValues ' + i, this.elasticFilter)
        this.elasticFilter = new Filter();
        index = i + 1;
      }
      this.prepareFilterObj(index);
      const jsonObject: any = {};
      this.filterEValues.forEach((filter, key) => {
        jsonObject[key] = filter;
      });
      this.filterValuesJsonObj = jsonObject;
    }
  }


  prepareFilterObj(i: number) {
    const filteValues = this.filterForm.value;
    if (filteValues.imei_selection === '2' && filteValues.imei_ids && filteValues.imei_ids.length > 0) {
      this.elasticFilter = new Filter();
      i++;
      this.elasticFilter.key = 'report_header.device_id';
      this.elasticFilter.operator = 'term';
      const values = this.filterForm.value.imei_ids.split(/[ ,\n]+/);
      this.elasticFilter.value = values.toString();
      this.filterEValues.set('filterValues ' + i + 'a', this.elasticFilter);
    }

    if (filteValues.asset_selection === '2' && filteValues.asset_ids && filteValues.asset_ids.length > 0) {
      this.elasticFilter = new Filter();
      i++;
      this.elasticFilter.key = 'report_header.assets_id';
      this.elasticFilter.operator = 'term';
      const values = this.filterForm.value.asset_ids.split(/[ ,\n]+/);
      this.elasticFilter.value = values.toString();
      this.filterEValues.set('filterValues ' + i + 'a', this.elasticFilter);
    }

    if (filteValues.customer_list && filteValues.customer_list !== '' && filteValues.customer_list.length > 0) {
      this.elasticFilter = new Filter();
      i++;
      this.elasticFilter.key = 'general.company_id';
      this.elasticFilter.operator = 'eq';
      const cutomerName: any = [];
      const recentSearch = [];
      this.filterForm.value.customer_list.forEach(element => {
        recentSearch.push(element);
        if (element === 'MAC-230' || element === 'UPS-001') {
          cutomerName.push(element.slice(4));
        } else {
          cutomerName.push(element.slice(2));
        }
      });
      localStorage.setItem('recentSearch2', JSON.stringify(recentSearch));
      if (recentSearch.length <= 2) {
        for (const iterator of this.recentCustomerList) {
          const index = recentSearch.map(function (item) { return item; }).indexOf(iterator.accountNumber);
          if (index === -1) {
            recentSearch.push(iterator.accountNumber);
          }
        }
      }
      localStorage.setItem('recentSearch', JSON.stringify(recentSearch));
      this.elasticFilter.value = cutomerName.toString();
      this.filterEValues.set('filterValues ' + i + 'a', this.elasticFilter);
    }

    if (filteValues.device_type && filteValues.device_type !== '') {
      this.elasticFilter = new Filter();
      i++;
      this.elasticFilter.key = 'general.device_type';
      this.elasticFilter.operator = 'eq';
      this.elasticFilter.value = this.filterForm.value.device_type;
      this.filterEValues.set('filterValues ' + i + 'a', this.elasticFilter);
    }

    if (filteValues.from && filteValues.from !== '' && filteValues.to && filteValues.to !== '') {
      this.elasticFilter = new Filter();
      i++;
      this.elasticFilter.key = 'general_mask_fields.received_time_stamp';
      this.elasticFilter.operator = 'gt';
      this.elasticFilter.value = this.datepipe.transform(this.filterForm.value.from, 'yyyy-MM-dd HH:mm:ss');
      this.filterEValues.set('filterValues ' + i + 'a', this.elasticFilter);

      this.elasticFilter = new Filter();
      this.elasticFilter.key = 'general_mask_fields.received_time_stamp';
      this.elasticFilter.operator = 'lt';
      if (this.filterForm.value.date_rang === 'last_hour' || this.filterForm.value.date_rang === 'last_24_hour' || this.filterForm.value.date_rang === 'last_week' || this.filterForm.value.date_rang === 'last_month'
        || this.filterForm.value.date_rang === 'last_year') {
        this.elasticFilter.value = this.datepipe.transform(new Date(), 'yyyy-MM-dd HH:mm:ss');
      } else {
        this.elasticFilter.value = this.datepipe.transform(this.filterForm.value.to, 'yyyy-MM-dd HH:mm:ss');
      }
      // this.elasticFilter.value = this.datepipe.transform(this.filterForm.value.to, 'yyyy-MM-dd HH:mm:ss');
      this.filterEValues.set('filterValues ' + i + 'b', this.elasticFilter);
    }

    if (filteValues.source && filteValues.source !== '' && filteValues.source !== '1') {
      if (filteValues.source === '34') {
        this.elasticFilter = new Filter();
        i++;
        this.elasticFilter.key = 'report_header.event_id';
        this.elasticFilter.operator = 'term';
        this.elasticFilter.value = '34,35';
        this.filterEValues.set('filterValues ' + i + 'a', this.elasticFilter);
      } else {
        this.elasticFilter = new Filter();
        i++;
        this.elasticFilter.key = 'report_header.event_id';
        this.elasticFilter.operator = 'notEqualTerm';
        this.elasticFilter.value = '34,35';
        this.filterEValues.set('filterValues ' + i + 'a', this.elasticFilter);
      }
    }
  }

  sortForAgGrid(sortModel: any) {

    this.gridApi.paginationCurrentPage = 0;
    if (sortModel.length === 0) {
      this.sortFlag = true;
      this.column = 'general_mask_fields.received_time_stamp';
      this.order = 'desc';
    } else {
      this.sortFlag = true;
      if (sortModel[0].colId === 'general_mask_fields.gps_time') {
        this.column = 'general_mask_fields.gps_time.keyword';
        this.order = sortModel[0].sort;
      } else if (sortModel[0].colId === 'config_version.configuration_desc') {
        this.column = 'config_version.configuration_desc.keyword';
        this.order = sortModel[0].sort;
      } else if (sortModel[0].colId === 'reefer.status') {
        this.column = 'reefer.status.keyword';
        this.order = sortModel[0].sort;
      } else if (sortModel[0].colId === 'reefer.state') {
        this.column = 'reefer.state.keyword';
        this.order = sortModel[0].sort;
      } else if (sortModel[0].colId === 'reefer.fan_state') {
        this.column = 'reefer.fan_state.keyword';
        this.order = sortModel[0].sort;
      } else if (sortModel[0].colId === 'config_version.device_config_changed') {
        this.column = 'config_version.device_config_changed.keyword';
        this.order = sortModel[0].sort;
      } else if (sortModel[0].colId === 'cargo_camera_sensor.uri') {
        this.column = 'cargo_camera_sensor.uri.keyword';
        this.order = sortModel[0].sort;
      } else if (sortModel[0].colId === 'cargo_camera_sensor.prediction_value') {
        this.column = 'cargo_camera_sensor.prediction_value.keyword';
        this.order = sortModel[0].sort;
      } else if (sortModel[0].colId === 'cargo_camera_sensor.confidence_rating') {
        this.column = 'cargo_camera_sensor.confidence_rating.keyword';
        this.order = sortModel[0].sort;
      } else if (sortModel[0].colId === 'cargo_camera_sensor.state') {
        this.column = 'cargo_camera_sensor.state.keyword';
        this.order = sortModel[0].sort;
      } else if (sortModel[0].colId === 'report_header.device_id') {
        this.column = 'report_header.device_id.keyword';
        this.order = sortModel[0].sort;
      } else if (sortModel[0].colId === 'report_header.event_name') {
        this.column = 'report_header.event_id';
        this.order = sortModel[0].sort;
      } else if (sortModel[0].colId === 'general_mask_fields.status') {
        this.column = 'general_mask_fields.status.keyword';
        this.order = sortModel[0].sort;
      } else if (sortModel[0].colId === 'general_mask_fields.trip') {
        this.column = 'general_mask_fields.trip.keyword';
        this.order = sortModel[0].sort;
      } else if (sortModel[0].colId === 'general_mask_fields.ignition') {
        this.column = 'general_mask_fields.ignition.keyword';
        this.order = sortModel[0].sort;
      } else if (sortModel[0].colId === 'general_mask_fields.motion') {
        this.column = 'general_mask_fields.motion.keyword';
        this.order = sortModel[0].sort;
      } else if (sortModel[0].colId === 'general_mask_fields.tamper') {
        this.column = 'general_mask_fields.tamper.keyword';
        this.order = sortModel[0].sort;
      } else if (sortModel[0].colId === 'orientation_fields.orientation_status') {
        this.column = 'orientation_fields.orientation_status.keyword';
        this.order = sortModel[0].sort;
      } else if (sortModel[0].colId === 'network_field.service_type_index') {
        this.column = 'network_field.service_type_index.keyword';
        this.order = sortModel[0].sort;
      } else if (sortModel[0].colId === 'network_field.cellular_band') {
        this.column = 'network_field.cellular_band.keyword';
        this.order = sortModel[0].sort;
      } else if (sortModel[0].colId === 'network_field.rx_tx_ec') {
        this.column = 'network_field.rx_tx_ec.keyword';
        this.order = sortModel[0].sort;
      } else if (sortModel[0].colId === 'software_version.app_version') {
        this.column = 'software_version.app_version.keyword';
        this.order = sortModel[0].sort;
      } else if (sortModel[0].colId === 'software_version.os_version') {
        this.column = 'software_version.os_version.keyword';
        this.order = sortModel[0].sort;
      } else if (sortModel[0].colId === 'oftware_version.os_version') {
        this.column = 'oftware_version.os_version.keyword';
        this.order = sortModel[0].sort;
      } else if (sortModel[0].colId === 'software_version.hw_version_revision') {
        this.column = 'software_version.hw_version_revision.keyword';
        this.order = sortModel[0].sort;
      } else if (sortModel[0].colId === 'software_version.extender_version') {
        this.column = 'software_version.extender_version.keyword';
        this.order = sortModel[0].sort;
      } else if (sortModel[0].colId === 'software_version.ble_version') {
        this.column = 'software_version.ble_version.keyword';
        this.order = sortModel[0].sort;
      } else if (sortModel[0].colId === 'config_version.device_config') {
        this.column = 'config_version.device_config.keyword';
        this.order = sortModel[0].sort;
      } else if (sortModel[0].colId === 'tftp_status.tftp_status') {
        this.column = 'tftp_status.tftp_status.keyword';
        this.order = sortModel[0].sort;
      } else if (sortModel[0].colId === 'gpio.gpio_status') {
        this.column = 'gpio.gpio_status.keyword';
        this.order = sortModel[0].sort;
      } else if (sortModel[0].colId === 'beta_abs_id.status') {
        this.column = 'beta_abs_id.status.keyword';
        this.order = sortModel[0].sort;
      } else if (sortModel[0].colId === 'psi_air_supply.psi_air_supply_measure') {
        this.column = 'psi_air_supply.psi_air_supply_measure.keyword';
        this.order = sortModel[0].sort;
      } else if (sortModel[0].colId === 'alpha_atis.condition') {
        this.column = 'alpha_atis.condition.keyword';
        this.order = sortModel[0].sort;
      } else if (sortModel[0].colId === 'psi_wheel_end.psi_wheel_end_measure') {
        this.column = 'psi_wheel_end.psi_wheel_end_measure.keyword';
        this.order = sortModel[0].sort;
      } else if (sortModel[0].colId === 'skf_wheel_end.comm_status') {
        this.column = 'skf_wheel_end.comm_status.keyword';
        this.order = sortModel[0].sort;
      } else if (sortModel[0].colId === 'tpms_alpha.status') {
        this.column = 'tpms_alpha.status.keyword';
        this.order = sortModel[0].sort;
      } else if (sortModel[0].colId === 'tpms_beta.status') {
        this.column = 'tpms_beta.status.keyword';
        this.order = sortModel[0].sort;
      } else if (sortModel[0].colId === 'door_sensor.status') {
        this.column = 'door_sensor.status.keyword';
        this.order = sortModel[0].sort;
      } else if (sortModel[0].colId === 'ble_door_sensor.comm_status') {
        this.column = 'ble_door_sensor.comm_status.keyword';
        this.order = sortModel[0].sort;
      } else if (sortModel[0].colId === 'pepsi_temperature.comm_status') {
        this.column = 'pepsi_temperature.comm_status.keyword';
        this.order = sortModel[0].sort;
      } else if (sortModel[0].colId === 'ble_temperature.comm_status') {
        this.column = 'ble_temperature.comm_status.keyword';
        this.order = sortModel[0].sort;
      } else if (sortModel[0].colId === 'ble_temperature.ble_temperature_sensormeasure') {
        this.column = 'ble_temperature.ble_temperature_sensormeasure.keyword';
        this.order = sortModel[0].sort;
      } else if (sortModel[0].colId === 'general.device_ip') {
        this.column = 'general.device_ip.keyword';
        this.order = sortModel[0].sort;
      } else if (sortModel[0].colId === 'general.device_type') {
        this.column = 'general.device_type.keyword';
        this.order = sortModel[0].sort;
      } else {
        this.column = sortModel[0].colId;
        this.order = sortModel[0].sort;
      }
    }
  }

  onCellClicked(params) {
    if (params.column.colId === 'report_header.device_id') {
      sessionStorage.setItem('device_id', params.data.report_header.device_id);
      if (params.data.report_header.device_id != null) {
        this.router.navigate(['gateway-profile']);
      }
    }
    if (params.column.colId === 'cargo_camera_sensor.uri') {
      if (params.data.cargo_camera_sensor && params.data.cargo_camera_sensor.uri && params.data.cargo_camera_sensor.uri.length > 38) {
        if (params.data.cargo_camera_sensor.uri.includes('http')) {
          this.imageUrl = params.data.cargo_camera_sensor.uri;
          $('#expandImageModal').modal('show');
          // window.open(params.data.cargo_camera_sensor.uri, '_blank');
        } else if (params.data.general.company_id === 'Amazon' || params.data.general.company_id === 'amazon') {
          const url = 'http://images.phillips-connect.net/' + params.data.cargo_camera_sensor.uri;
          this.imageUrl = url;
          $('#expandImageModal').modal('show');
          // window.open(url, '_blank');
        }
      }
    }
    if (params.column.colId === 'report_header.sequence') {
      this.latestRawReport = params.data.general.rawreport;
      $('#latestReportModel').modal('show');
    }
  }

  copyToClipBoard(inputElement: any) {
    inputElement.select();
    document.execCommand('copy');
    inputElement.setSelectionRange(0, 0);
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
        this.column = 'general_mask_fields.received_time_stamp';
        this.order = 'desc';
        params.api.setFilterModel(null);
        params.column.columnApi.applyColumnState({ defaultState: { sort: null } });
        //params.column.columnApi.resetColumnState();
        params.api.onFilterChanged();
        sessionStorage.setItem('all_device_report_grid_filter_state', null);
        // sessionStorage.setItem('custom_asset_list_grid_column_state', null);
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

  clearFilter() {
    this.column = 'general_mask_fields.received_time_stamp';
    this.order = 'desc';
    this.paramList.api.setFilterModel(null);
    this.paramList.api.onFilterChanged();
    sessionStorage.setItem('all_device_report_grid_filter_state', null);
    sessionStorage.setItem('all_device_report_grid_column_state', null);
    sessionStorage.setItem('all_device_report_grid_column_group_state', null);
  }

  getFilterValuesData() {
    return this.userNameList;
  }

  refresh() {
    if (this.gridApi != null && this.currentPage === 50) {
      this.currentPage = 0;
      this.gridApi.onFilterChanged();
    }
  }

  onGridReady(params: any) {
    params.api.sizeColumnsToFit();
    // this.gridOptions.api.setDomLayout('autoHeight');
    this.paramList = params;
    this.gridApi = params.api;
    this.gridApi.setRowData([]);
    this.gridColumnApi = params.columnApi;

    const columState = JSON.parse(sessionStorage.getItem('all_device_report_grid_column_state'));
    const columGroupState = JSON.parse(sessionStorage.getItem('all_device_report_grid_column_group_state'));
    const filterSavedState = sessionStorage.getItem('all_device_report_grid_filter_state');
    if (columState) {
      this.gridColumnApi.applyColumnState({ state: columState, applyOrder: true, });
    }
    if (columGroupState) {
      //this.gridColumnApi.applyColumnState({ state: columState, applyOrder: true, });
      this.gridColumnApi.setColumnGroupState(columGroupState);
    }

    if (filterSavedState) {
      this.gridApi.setFilterModel(JSON.parse(filterSavedState));
    }


    const datasource = {
      getRows: (params: IGetRowsParams) => {
        const elem = document.getElementsByClassName('ag-body-viewport') as HTMLCollection;
        //sessionStorage.setItem('all_device_report_grid_filter_state', JSON.stringify(params.filterModel));
        this.sortForAgGrid(params.sortModel);
        this.filterForAgGrid(params.filterModel);
        if (!this.isEmptyObject(this.filterValuesJsonObj)) {
          // this.isGroupColumn = false;
          if (this.isFirst) {
            if (elem && elem.length > 0) {
              const ele = elem[0] as HTMLElement;
              ele.style.overflow = 'hidden';
            }
            this.loading = true;
            this.counts = {};
            this.gatewayListService.getAllDeviceReportsElastic(this.currentPage, this.column, this.order, this.pageLimit, this.filterValuesJsonObj)
              .pipe(
                map((reports: any) => {
                  return {
                    totalHits: reports.totalHits.value,
                    tlvData: reports.hits.map(
                      report => ({
                        lat_long: report.sourceAsMap.general_mask_fields.latitude + '/' + report.sourceAsMap.general_mask_fields.longitude,
                        general: report.sourceAsMap.general,
                        general_mask_fields: report.sourceAsMap.general_mask_fields,
                        report_header: report.sourceAsMap.report_header,
                        temperature: report.sourceAsMap.temperature,
                        voltage: report.sourceAsMap.voltage,
                        abs: report.sourceAsMap.abs,
                        lite_sentry: report.sourceAsMap.lite_sentry,
                        odometer: report.sourceAsMap.odometer,
                        abs_odometer: report.sourceAsMap.abs_odometer,
                        abs_beta: report.sourceAsMap.abs_beta,
                        chassis: report.sourceAsMap.chassis,
                        tpms_alpha: report.sourceAsMap.tpms_alpha,
                        network_field: report.sourceAsMap.network_field,
                        ble_door_sensor: report.sourceAsMap.ble_door_sensor,
                        door_sensor: report.sourceAsMap.door_sensor,
                        gpio: report.sourceAsMap.gpio,
                        orientation_fields: report.sourceAsMap.orientation_fields,
                        waterfall: report.sourceAsMap.waterfall,
                        software_version: report.sourceAsMap.software_version,
                        config_version: report.sourceAsMap.config_version,
                        psi_air_supply: report.sourceAsMap.psi_air_supply,
                        tpms_beta: report.sourceAsMap.tpms_beta,
                        ble_temperature: report.sourceAsMap.ble_temperature,
                        field_vehicle_ecu: report.sourceAsMap.field_vehicle_ecu,
                        field_vehicle_dtc: report.sourceAsMap.field_vehicle_dtc,
                        field_vehicle_vin: report.sourceAsMap.field_vehicle_vin,
                        beta_abs_id: report.sourceAsMap.beta_abs_id,
                        peripheral_version: report.sourceAsMap.peripheral_version,
                        skf_wheel_end: report.sourceAsMap.skf_wheel_end,
                        peripheral: report.sourceAsMap.peripheral,
                        tftp_status: report.sourceAsMap.tftp_status,
                        cargo_camera_sensor: report.sourceAsMap.cargo_camera_sensor,
                        psi_wheel_end: report.sourceAsMap.psi_wheel_end,
                        alpha_atis: report.sourceAsMap.alpha_atis,
                        reefer: report.sourceAsMap.reefer,
                        accelerometer_fields: report.sourceAsMap.accelerometer_fields,
                        pepsi_temperature: report.sourceAsMap.pepsi_temperature,
                        beacon: report.sourceAsMap.beacon,
                        gateway_advertisement: report.sourceAsMap.gateway_advertisement,
                        tank_saver: report.sourceAsMap.tank_saver_beta,
                        advertisement_maxlink: report.sourceAsMap.advertisement_maxlink,
                        connectable_maxlink: report.sourceAsMap.connectable_maxlink,
                        lite_sentry_debug: report.sourceAsMap.lite_sentry_debug,
                      })
                    ),
                  };
                })
              ).subscribe(data => {
                let self = this;
                setTimeout(() => {
                  self.loading = false;
                  if (elem && elem.length > 0) {
                    const ele = elem[0] as HTMLElement;
                    if (ele) {
                      ele.style.overflow = 'auto';
                    }
                  }
                }, 500);

                this.totalNumberOfRecords = data.totalHits;
                if (this.totalNumberOfRecords <= this.currentPage) {
                  this.isFirst = false;
                }
                this.gridApi.hideOverlay();
                this.elasticRowData = [];
                this.rowData = [];
                this.currentPage += 50;
                if (data != null && data.totalHits > 0) {
                  for (const iterator of data.tlvData) {
                    this.elasticRowData.push(iterator);
                    if (iterator.report_header) {
                      if (iterator.report_header.sequence || iterator.report_header.sequence === 0) {
                        if (!this.counts[iterator.report_header.device_id]) {
                          this.counts[iterator.report_header.device_id] = {};
                        }
                        this.counts[iterator.report_header.device_id][iterator.report_header.sequence] = (this.counts[iterator.report_header.device_id][iterator.report_header.sequence] || 0) + 1;
                      }
                      if (iterator.report_header.event_id) {
                        if (this.eventList && this.eventList.length > 0) {
                          this.eventList.forEach(event => {
                            if (event.eventId === iterator.report_header.event_id) {
                              iterator.report_header.event_name = event.eventType;
                            }
                          });
                        }
                      }
                    }
                    if (!iterator.voltage) {
                      if (iterator.general_mask_fields.internal_power_volts === 1000000000) {
                        iterator.general_mask_fields.internal_power_volts = 'NA';
                      } else {
                        iterator.general_mask_fields.internal_power_volts = iterator.general_mask_fields.internal_power_volts;
                      }
                      if (iterator.general_mask_fields.external_power_volts === 1000000000) {
                        iterator.general_mask_fields.external_power_volts = 'NA';
                      } else {
                        iterator.general_mask_fields.external_power_volts = iterator.general_mask_fields.external_power_volts;
                      }
                    } else {
                      let eps = 1e-9;
                      iterator.general_mask_fields.internal_power_volts = (iterator.voltage.battery_power + eps).toFixed(2);
                      iterator.general_mask_fields.external_power_volts = (iterator.voltage.main_power + eps).toFixed(2);
                    }
                    if (iterator.general_mask_fields) {
                      if (iterator.general_mask_fields.gpsstatus_index === 0) {
                        iterator.general_mask_fields.gpsstatus_index = 'Unlocked';
                      } else {
                        iterator.general_mask_fields.gpsstatus_index = 'Locked';
                      }
                    }
                    let deviceStatus = '';
                    let deviceStatusAll = '';
                    if (iterator.voltage) {
                      if (iterator.voltage.selector === 1) {
                        if ((iterator.voltage.main_power < 1000000000) && (iterator.voltage.main_power > 7)
                          && (iterator.voltage.aux_power < 1000000000)
                          && ((iterator.voltage.main_power + 0.3) > iterator.voltage.aux_power)) {
                          deviceStatus = 'Primary External';
                          deviceStatusAll = 'Primary External, ';
                        } else if ((iterator.voltage.aux_power < 1000000000) && (iterator.voltage.aux_power > 7)) {
                          deviceStatus = 'Secondary External';
                          deviceStatusAll = 'Secondary External, ';
                        } else {
                          deviceStatus = 'Battery';
                          deviceStatusAll = 'Battery, ';
                        }
                      } else {
                        if ((iterator.voltage.main_power > 7) && (iterator.voltage.main_power < 1000000000)) {
                          deviceStatus = 'Primary External';
                          deviceStatusAll = 'Primary External, ';
                        } else {
                          deviceStatus = 'Battery';
                          deviceStatusAll = 'Battery, ';
                        }
                      }
                    } else {
                      if ((iterator.general_mask_fields.external_power_volts < 1000000000) && (iterator.general_mask_fields.external_power_volts > 7)) {
                        deviceStatus = 'External';
                        deviceStatusAll = 'External, ';
                      } else {
                        deviceStatus = 'Battery';
                        deviceStatusAll = 'Battery, ';
                      }
                    }
                    iterator.general_mask_fields.status = deviceStatus;
                    if (iterator.voltage) {
                      let eps = 1e-9
                      if (iterator.voltage.aux_power === 1000000000) {
                        iterator.voltage.aux_power = 'NA';
                      } else {
                        iterator.voltage.aux_power = (iterator.voltage.aux_power + eps).toFixed(2);
                      }
                      if (iterator.voltage.charge_power === 1000000000) {
                        iterator.voltage.charge_power = 'NA';
                      } else {
                        iterator.voltage.charge_power = (iterator.voltage.charge_power + eps).toFixed(2);
                      }
                    }
                    if (iterator.general_mask_fields) {
                      if (iterator.general_mask_fields.trip === 'On') {
                        iterator.general_mask_fields.trip = 'In Trip';
                        deviceStatusAll = deviceStatusAll + 'In Trip, ';
                      } else {
                        iterator.general_mask_fields.trip = 'Not In Trip';
                        deviceStatusAll = deviceStatusAll + 'Not In Trip, ';
                      }
                      if (iterator.general_mask_fields.ignition === 'On') {
                        iterator.general_mask_fields.ignition = 'Ignition On';
                        deviceStatusAll = deviceStatusAll + 'Ignition On, ';
                      } else {
                        iterator.general_mask_fields.ignition = 'Ignition Off';
                        deviceStatusAll = deviceStatusAll + 'Ignition Off, ';
                      }
                      if (iterator.general_mask_fields.motion === 'On') {
                        iterator.general_mask_fields.motion = 'In Motion';
                        deviceStatusAll = deviceStatusAll + 'In Motion, ';
                      } else {
                        iterator.general_mask_fields.motion = 'Not In Motion';
                        deviceStatusAll = deviceStatusAll + 'Not In Motion, ';
                      }
                      if (iterator.general_mask_fields.tamper === 'On') {
                        iterator.general_mask_fields.tamper = 'Tampered';
                        deviceStatusAll = deviceStatusAll + 'Tampered';
                      } else {
                        iterator.general_mask_fields.tamper = 'Not Tampered';
                        deviceStatusAll = deviceStatusAll + 'Not Tampered';
                      }
                    }
                    iterator.general_mask_fields.device_status = deviceStatusAll;
                    if (iterator.general_mask_fields.altitude_feet === 1000000000) {
                      iterator.general_mask_fields.altitude_feet = 'NA';
                    } else {
                      iterator.general_mask_fields.altitude_feet = (iterator.general_mask_fields.altitude_feet * 3.28084).toFixed(2);
                    }
                    if (iterator.general_mask_fields.hdop === 1000000000) {
                      iterator.general_mask_fields.hdop = 'NA';
                    } else {
                      iterator.general_mask_fields.hdop = iterator.general_mask_fields.hdop;
                    }
                    if (iterator.general_mask_fields.num_satellites === 1000000000) {
                      iterator.general_mask_fields.num_satellites = 'NA';
                    } else {
                      iterator.general_mask_fields.num_satellites = iterator.general_mask_fields.num_satellites;
                    }
                    if (iterator.odometer) {
                      iterator.general_mask_fields.odometer_kms = iterator.odometer.odometer_miles.toFixed(2);

                    } else {
                      if (iterator.general_mask_fields.odometer_kms === 1000000000) {
                        iterator.general_mask_fields.odometer_kms = 'NA';

                      } else {
                        iterator.general_mask_fields.odometer_kms = (iterator.general_mask_fields.odometer_kms * 0.621371).toFixed(2);
                      }
                    }
                    if (iterator.abs_odometer) {
                      iterator.abs_odometer.odometer = (iterator.abs_odometer.odometer * 0.0621371).toFixed(2);
                    }
                    if (iterator.general) {
                      if (iterator.general.company_id) {
                        if (this.organisationList && this.organisationList.length > 0) {
                          this.organisationList.forEach(organisation => {
                            if (organisation.accountNumber === iterator.general.company_id) {
                              iterator.general.company_id = organisation.organisationName;
                            }
                          });
                        }
                      }
                    }
                    if (iterator.beacon) {
                      iterator.beacon.comm_status = iterator.beacon.c_status + '; Overflow ' + iterator.beacon.overflag + '; Collecting Beacon ' + iterator.beacon.collecting_beacon_flag + '; Quantity: ' + iterator.beacon.num_beacons;
                    }
                    if (iterator.ble_door_sensor) {
                      iterator.ble_door_sensor.comm_status = iterator.ble_door_sensor.comm_status +
                        '; sStatus : ' + iterator.ble_door_sensor.sensor_status + ': ' +
                        iterator.ble_door_sensor.door_state + '; ' + iterator.ble_door_sensor.door_type
                        + '; ' + iterator.ble_door_sensor.door_sequence + '; ' + iterator.ble_door_sensor.battery + '; ' + iterator.ble_door_sensor.temperature_c;
                    }
                    // if(iterator.field_vehicle_vin)
                    // {
                    //   iterator.general_mask_fields.vin == iterator.field_vehicle_vin.vin;
                    // }
                    // else
                    // {
                    //   iterator.general_mask_fields.vin == 'NA';
                    // }
                    if (iterator.field_vehicle_ecu) {
                      iterator.field_vehicle_ecu.fuel_level_percentage = iterator.field_vehicle_ecu.fuel_level_percentage.toFixed(2);
                    }
                    let absStatus = '';
                    if (iterator.abs) {
                      if (iterator.abs.no_faults === true) {
                        iterator.abs.status = 'Healthy;';
                      } else {
                        if (iterator.abs.numActiveFaults > 0) {
                          if ((iterator.abs.activeFaultsCodes != null) && (iterator.abs.activeFaultsCodes.length > 0)) {
                            absStatus = 'A-Codes: ';
                            absStatus = absStatus + iterator.abs.activeFaultsCodes;
                          }
                        }
                        if (iterator.abs.numStoredFaults > 0) {
                          if ((iterator.abs.storedFaultsCodes != null) && (iterator.abs.storedFaultsCodes.length > 0)) {
                            absStatus = 'S-Codes: ';
                            absStatus = absStatus + iterator.abs.storedFaultsCodes;
                          }
                        }
                        iterator.abs.status = absStatus;
                      }
                    }
                    if (iterator.accelerometer_fields == null && iterator.accelerometer_fields === undefined) {
                      iterator.general_mask_fields.accelerometer_xdefault = '-13824.00';
                      iterator.general_mask_fields.accelerometer_ydefault = '-13824.00';
                      iterator.general_mask_fields.accelerometer_zdefault = '-13824.00';
                    } else {
                      iterator.general_mask_fields.accelerometer_xdefault = iterator.accelerometer_fields.accelerometer_xcalib;
                      iterator.general_mask_fields.accelerometer_ydefault = iterator.accelerometer_fields.accelerometer_ycalib;
                      iterator.general_mask_fields.accelerometer_zdefault = iterator.accelerometer_fields.accelerometer_zcalib;
                    }
                    if (iterator.network_field) {
                      iterator.general_mask_fields.rssi = iterator.network_field.rssi;
                    } else {
                      iterator.general_mask_fields.rssi = iterator.general_mask_fields.rssi;
                    }
                    if (iterator.reefer) {
                      iterator.reefer.concatValues = iterator.reefer.status + '; ' + iterator.reefer.state + ' ;' + iterator.reefer.fan_state;
                    }
                    if (iterator.waterfall != null && iterator.waterfall !== undefined && iterator.waterfall.waterfall_info != null && iterator.waterfall.waterfall_info !== undefined) {
                      for (let i = 0; i < iterator.waterfall.waterfall_info.length; i++) {
                        if (iterator.waterfall.waterfall_info[i].config_id === 1) {
                          iterator.waterfall.config1_id = iterator.waterfall.waterfall_info[i].config_identification_version;
                          iterator.waterfall.config1_crc = iterator.waterfall.waterfall_info[i].config_crc;
                        }
                        if (iterator.waterfall.waterfall_info[i].config_id === 2) {
                          iterator.waterfall.config2_id = iterator.waterfall.waterfall_info[i].config_identification_version;
                          iterator.waterfall.config2_crc = iterator.waterfall.waterfall_info[i].config_crc;
                        }
                        if (iterator.waterfall.waterfall_info[i].config_id === 3) {
                          iterator.waterfall.config3_id = iterator.waterfall.waterfall_info[i].config_identification_version;
                          iterator.waterfall.config3_crc = iterator.waterfall.waterfall_info[i].config_crc;
                        }
                        if (iterator.waterfall.waterfall_info[i].config_id === 4) {
                          iterator.waterfall.config4_id = iterator.waterfall.waterfall_info[i].config_identification_version;
                          iterator.waterfall.config4_crc = iterator.waterfall.waterfall_info[i].config_crc;
                        }
                        if (iterator.waterfall.waterfall_info[i].config_id === 5) {
                          iterator.waterfall.config5_id = iterator.waterfall.waterfall_info[i].config_identification_version;
                          iterator.waterfall.config5_crc = iterator.waterfall.waterfall_info[i].config_crc;
                        }
                      }
                    }
                    let tankSaver = '';
                    if (iterator.tank_saver) {
                      tankSaver = iterator.tank_saver.c_status + ', ';
                      if (iterator.tank_saver.tank_saver_beta_measure != null && iterator.tank_saver.tank_saver_beta_measure.length > 0) {
                        iterator.tank_saver.tank_saver_beta_measure.forEach(tank_saver_beta => {
                          tankSaver = tankSaver + 'S Status: ' + tank_saver_beta.s_status + ', ';
                          if (tank_saver_beta.session_time != null) {
                            tankSaver = tankSaver + 'Session Time : ' + tank_saver_beta.session_time + ', ';
                          }
                          if (tank_saver_beta.advertisement_age != null) {
                            tankSaver = tankSaver + 'Advertisemnet Age : ' + tank_saver_beta.advertisement_age + ', ';
                          }
                          if (tank_saver_beta.session_age_code != null) {
                            tankSaver = tankSaver + 'Session Age Code : ' + tank_saver_beta.session_age_code + ', ';
                          }
                          if (tank_saver_beta.advertisement_age_code != null) {
                            tankSaver = tankSaver + 'Advertisment Age Code : ' + tank_saver_beta.advertisement_age_code + ', ';
                          }
                        });
                      }
                      iterator.tank_saver.concatValues = tankSaver;
                    }
                    if (iterator.software_version) {
                      iterator.software_version.app_version = iterator.software_version.device_name + ' ' + iterator.software_version.app_version;
                    }
                    if (iterator.door_sensor) {
                      iterator.door_sensor.status = iterator.door_sensor.status + ',  ' + iterator.door_sensor.last_valid_check + '; RSSI ' + iterator.door_sensor.ble_rssi + ';';
                    }
                    let tpmsBeta = '';
                    if (iterator.tpms_beta && iterator.tpms_beta.tpms_beta_measure) {
                      tpmsBeta = iterator.tpms_beta.status;
                      if (iterator.tpms_beta.tpms_beta_measure != null && iterator.tpms_beta.tpms_beta_measure.length > 0) {
                        if (iterator.tpms_beta.tpms_beta_measure[0].primary_curb_status === 'Online') {
                          iterator.tpms_beta.tpms_beta_measure.lof = iterator.tpms_beta.tpms_beta_measure[0].primary_curb_status;
                          iterator.tpms_beta.tpms_beta_measure.lof_temp = iterator.tpms_beta.tpms_beta_measure[0].primary_curbside_temperature_f + 'F';
                          iterator.tpms_beta.tpms_beta_measure.lof_psi = iterator.tpms_beta.tpms_beta_measure[0].primary_curbside_pressure_psi + 'psi';
                          tpmsBeta = tpmsBeta + '; FLOS: ' + iterator.tpms_beta.tpms_beta_measure[0].primary_curb_status + '; FLO: ' +
                            iterator.tpms_beta.tpms_beta_measure[0].primary_curbside_pressure_psi + 'psi, ' + iterator.tpms_beta.tpms_beta_measure[0].primary_curbside_temperature_f + 'F ';
                        } else {
                          iterator.tpms_beta.tpms_beta_measure.lof = iterator.tpms_beta.tpms_beta_measure[0].primary_curb_status;
                          tpmsBeta = tpmsBeta + '; FLOS: ' + iterator.tpms_beta.tpms_beta_measure[0].primary_curb_status;
                        }
                        if (iterator.tpms_beta.tpms_beta_measure[0].inner_curb_status === 'Online') {
                          iterator.tpms_beta.tpms_beta_measure.lif = iterator.tpms_beta.tpms_beta_measure[0].inner_curb_status;
                          iterator.tpms_beta.tpms_beta_measure.lif_temp = iterator.tpms_beta.tpms_beta_measure[0].inner_curbside_temperature_f + 'F';
                          iterator.tpms_beta.tpms_beta_measure.lif_psi = iterator.tpms_beta.tpms_beta_measure[0].inner_curbisde_pressure_psi + 'psi';
                          tpmsBeta = tpmsBeta + '; FLIS: ' + iterator.tpms_beta.tpms_beta_measure[0].inner_curb_status + '; FLI: ' +
                            iterator.tpms_beta.tpms_beta_measure[0].inner_curbisde_pressure_psi + 'psi, ' + iterator.tpms_beta.tpms_beta_measure[0].inner_curbside_temperature_f + 'F ';
                        } else {
                          iterator.tpms_beta.tpms_beta_measure.lif = iterator.tpms_beta.tpms_beta_measure[0].inner_curb_status;
                          tpmsBeta = tpmsBeta + '; FLIS: ' + iterator.tpms_beta.tpms_beta_measure[0].inner_curb_status;
                        }
                        if (iterator.tpms_beta.tpms_beta_measure[0].inner_roadside_status === 'Online') {
                          iterator.tpms_beta.tpms_beta_measure.rif = iterator.tpms_beta.tpms_beta_measure[0].inner_roadside_status;
                          iterator.tpms_beta.tpms_beta_measure.rif_temp = iterator.tpms_beta.tpms_beta_measure[0].inner_roadside_temperature_f + 'F';
                          iterator.tpms_beta.tpms_beta_measure.rif_psi = iterator.tpms_beta.tpms_beta_measure[0].inner_roadside_pressure_psi + 'psi';
                          tpmsBeta = tpmsBeta + '; FRIS: ' + iterator.tpms_beta.tpms_beta_measure[0].inner_roadside_status + '; FRI: ' +
                            iterator.tpms_beta.tpms_beta_measure[0].inner_roadside_pressure_psi + 'psi, ' + iterator.tpms_beta.tpms_beta_measure[0].inner_roadside_temperature_f + 'F ';
                        } else {
                          iterator.tpms_beta.tpms_beta_measure.rif = iterator.tpms_beta.tpms_beta_measure[0].inner_roadside_status;
                          tpmsBeta = tpmsBeta + '; FRIS:' + iterator.tpms_beta.tpms_beta_measure[0].inner_roadside_status;
                        }
                        if (iterator.tpms_beta.tpms_beta_measure[0].primary_roadside_status === 'Online') {
                          iterator.tpms_beta.tpms_beta_measure.rof = iterator.tpms_beta.tpms_beta_measure[0].primary_roadside_status;
                          iterator.tpms_beta.tpms_beta_measure.rof_temp = iterator.tpms_beta.tpms_beta_measure[0].primary_roadside_temperature_f + 'F';
                          iterator.tpms_beta.tpms_beta_measure.rof_psi = iterator.tpms_beta.tpms_beta_measure[0].primary_roadside_pressure_psi + 'psi';
                          tpmsBeta = tpmsBeta + '; FROS: ' + iterator.tpms_beta.tpms_beta_measure[0].primary_roadside_status + '; FRO: ' +
                            iterator.tpms_beta.tpms_beta_measure[0].primary_roadside_pressure_psi + 'psi, ' + iterator.tpms_beta.tpms_beta_measure[0].primary_roadside_temperature_f + 'F ';
                        } else {
                          tpmsBeta = tpmsBeta + '; FROS: ' + iterator.tpms_beta.tpms_beta_measure[0].primary_roadside_status;
                          iterator.tpms_beta.tpms_beta_measure.rof = iterator.tpms_beta.tpms_beta_measure[0].primary_roadside_status;
                        }
                        if (iterator.tpms_beta.tpms_beta_measure.length >= 2 && iterator.tpms_beta.tpms_beta_measure[1].primary_curb_status === 'Online') {
                          iterator.tpms_beta.tpms_beta_measure.lor = iterator.tpms_beta.tpms_beta_measure[1].primary_curb_status;
                          iterator.tpms_beta.tpms_beta_measure.lor_temp = iterator.tpms_beta.tpms_beta_measure[1].primary_curbside_temperature_f + 'F';
                          iterator.tpms_beta.tpms_beta_measure.lor_psi = iterator.tpms_beta.tpms_beta_measure[1].primary_curbside_pressure_psi + 'psi';
                          tpmsBeta = tpmsBeta + '; RLOS: ' + iterator.tpms_beta.tpms_beta_measure[1].primary_curb_status + '; RLO: ' +
                            iterator.tpms_beta.tpms_beta_measure[1].primary_curbside_pressure_psi + 'psi, ' + iterator.tpms_beta.tpms_beta_measure[1].primary_curbside_temperature_f + 'F ';
                        } else if (iterator.tpms_beta.tpms_beta_measure.length >= 2) {
                          tpmsBeta = tpmsBeta + '; RLOS:' + iterator.tpms_beta.tpms_beta_measure[1].primary_curb_status;
                          iterator.tpms_beta.tpms_beta_measure.lor = iterator.tpms_beta.tpms_beta_measure[1].primary_curb_status;
                        }
                        if (iterator.tpms_beta.tpms_beta_measure.length >= 2 && iterator.tpms_beta.tpms_beta_measure[1].inner_curb_status === 'Online') {
                          iterator.tpms_beta.tpms_beta_measure.lir = iterator.tpms_beta.tpms_beta_measure[1].inner_curb_status;
                          iterator.tpms_beta.tpms_beta_measure.lir_temp = iterator.tpms_beta.tpms_beta_measure[1].inner_curbside_temperature_f + 'F';
                          iterator.tpms_beta.tpms_beta_measure.lir_psi = iterator.tpms_beta.tpms_beta_measure[1].inner_curbisde_pressure_psi + 'psi';
                          tpmsBeta = tpmsBeta + '; RLIS: ' + iterator.tpms_beta.tpms_beta_measure[1].inner_curb_status + '; RLI: ' +
                            iterator.tpms_beta.tpms_beta_measure[1].inner_curbisde_pressure_psi + 'psi, ' + iterator.tpms_beta.tpms_beta_measure[1].inner_curbside_temperature_f + 'F ';
                        } else if (iterator.tpms_beta.tpms_beta_measure.length >= 2) {
                          iterator.tpms_beta.tpms_beta_measure.lir = iterator.tpms_beta.tpms_beta_measure[1].inner_curb_status;
                          tpmsBeta = tpmsBeta + '; RLIS: ' + iterator.tpms_beta.tpms_beta_measure[1].inner_curb_status;
                        }
                        if (iterator.tpms_beta.tpms_beta_measure.length >= 2 && iterator.tpms_beta.tpms_beta_measure[1].inner_roadside_status === 'Online') {
                          iterator.tpms_beta.tpms_beta_measure.rir = iterator.tpms_beta.tpms_beta_measure[1].inner_roadside_status;
                          iterator.tpms_beta.tpms_beta_measure.rir_temp = iterator.tpms_beta.tpms_beta_measure[1].inner_roadside_temperature_f + 'F';
                          iterator.tpms_beta.tpms_beta_measure.rir_psi = iterator.tpms_beta.tpms_beta_measure[1].inner_roadside_pressure_psi + 'psi';
                          tpmsBeta = tpmsBeta + '; RRIS: ' + iterator.tpms_beta.tpms_beta_measure[1].inner_roadside_status + '; RRI: ' +
                            iterator.tpms_beta.tpms_beta_measure[1].inner_roadside_pressure_psi + 'psi, ' + iterator.tpms_beta.tpms_beta_measure[1].inner_roadside_temperature_f + 'F ';
                        } else if (iterator.tpms_beta.tpms_beta_measure.length >= 2) {
                          iterator.tpms_beta.tpms_beta_measure.rir = iterator.tpms_beta.tpms_beta_measure[1].inner_roadside_status;
                          tpmsBeta = tpmsBeta + '; RRIS: ' + iterator.tpms_beta.tpms_beta_measure[1].inner_roadside_status;
                        }
                        if (iterator.tpms_beta.tpms_beta_measure.length >= 2 && iterator.tpms_beta.tpms_beta_measure[1].primary_roadside_status === 'Online') {
                          iterator.tpms_beta.tpms_beta_measure.ror = iterator.tpms_beta.tpms_beta_measure[1].primary_roadside_status;
                          iterator.tpms_beta.tpms_beta_measure.ror_temp = iterator.tpms_beta.tpms_beta_measure[1].primary_roadside_temperature_f + 'F';
                          iterator.tpms_beta.tpms_beta_measure.ror_psi = iterator.tpms_beta.tpms_beta_measure[1].primary_roadside_pressure_psi + 'psi';
                          tpmsBeta = tpmsBeta + '; RROS: ' + iterator.tpms_beta.tpms_beta_measure[1].primary_roadside_status + '; RRO: ' +
                            iterator.tpms_beta.tpms_beta_measure[1].primary_roadside_pressure_psi + 'psi, ' + iterator.tpms_beta.tpms_beta_measure[1].primary_roadside_temperature_f + 'F ';
                        } else if (iterator.tpms_beta.tpms_beta_measure.length >= 2) {
                          iterator.tpms_beta.tpms_beta_measure.ror = iterator.tpms_beta.tpms_beta_measure[1].primary_roadside_status;
                          tpmsBeta = tpmsBeta + '; RROS: ' + iterator.tpms_beta.tpms_beta_measure[1].primary_roadside_status;
                        }
                      }
                      iterator.tpms_beta.allValues = tpmsBeta;
                    }
                    let peripheralVersion = '';
                    if (iterator.peripheral_version) {
                      if (iterator.peripheral_version.lite_sentry) {
                        if (iterator.peripheral_version.lite_sentry.sensor_status === 'Online') {
                          peripheralVersion = 'Sensor Type : Lite Sentry; Sensor status : ' + iterator.peripheral_version.lite_sentry.sensor_status + '; Size :' + iterator.peripheral_version.lite_sentry.data_size +
                            '; Hardware id :' + iterator.peripheral_version.lite_sentry.hardware_id +
                            '; Hardware Version :' + iterator.peripheral_version.lite_sentry.hardware_version +
                            '; App Version :' + iterator.peripheral_version.lite_sentry.app_version +
                            '; Boot Version :' + iterator.peripheral_version.lite_sentry.boot_version + '; ';
                        } else {
                          peripheralVersion = 'Sensor Type : Lite Sentry; Sensor status : ' + iterator.peripheral_version.lite_sentry.sensor_status + '; ';
                        }
                      }
                      if (iterator.peripheral_version.maxbotix_cargo_sensor) {
                        if (iterator.peripheral_version.maxbotix_cargo_sensor.sensor_status === 'Online') {
                          peripheralVersion = peripheralVersion + 'Sensor Type : Maxbotix Cargo Sensor; Sensor status: ' + iterator.peripheral_version.maxbotix_cargo_sensor.sensor_status +
                            '; Size :' + iterator.peripheral_version.maxbotix_cargo_sensor.data_size +
                            '; Hardware Version :' + iterator.peripheral_version.maxbotix_cargo_sensor.hardware_version +
                            '; Firmware Version :' + iterator.peripheral_version.maxbotix_cargo_sensor.firmware_version + ';';
                        } else {
                          peripheralVersion = peripheralVersion + 'Sensor Type : Maxbotix Cargo Sensor; Sensor status: ' + iterator.peripheral_version.maxbotix_cargo_sensor.sensor_status + '; ';
                        }
                      }
                      if (iterator.peripheral_version.ste_receiver) {
                        if (iterator.peripheral_version.ste_receiver.sensor_status === 'Online') {
                          peripheralVersion = peripheralVersion + 'Sensor Type : STE TPMS Receiver; Sensor status: ' + iterator.peripheral_version.ste_receiver.sensor_status +
                            '; Size :' + iterator.peripheral_version.ste_receiver.data_size +
                            '; MCU App :' + iterator.peripheral_version.ste_receiver.mcu_app +
                            '; Zeeki App :' + iterator.peripheral_version.ste_receiver.zeeki_app + ';';
                        } else {
                          peripheralVersion = peripheralVersion + 'Sensor Type : STE TPMS Receiver; Sensor status: ' + iterator.peripheral_version.ste_receiver.sensor_status + '; ';
                        }
                      }
                      if (iterator.peripheral_version.hamaton_tpmsreceiver) {
                        if (iterator.peripheral_version.hamaton_tpmsreceiver.sensor_status === 'Online') {
                          peripheralVersion = peripheralVersion + 'Sensor Type : Hamaton TPMS Receiver; Sensor status: ' + iterator.peripheral_version.hamaton_tpmsreceiver.sensor_status +
                            '; Size :' + iterator.peripheral_version.hamaton_tpmsreceiver.data_size +
                            '; MCU App :' + iterator.peripheral_version.hamaton_tpmsreceiver.mcu_app +
                            '; Zeeki App :' + iterator.peripheral_version.hamaton_tpmsreceiver.zeeki_app + ';';
                        } else {
                          peripheralVersion = peripheralVersion + 'Sensor Type : Hamaton TPMS Receiver; Sensor status: ' + iterator.peripheral_version.hamaton_tpmsreceiver.sensor_status + '; ';
                        }
                      }
                      if (iterator.peripheral_version.cargo_vision) {
                        if (iterator.peripheral_version.cargo_vision.sensor_status === 'Online') {
                          peripheralVersion = peripheralVersion + 'Sensor Type : BLE PCT Cargo Vision; Sensor status: ' + iterator.peripheral_version.cargo_vision.sensor_status +
                            '; Size :' + iterator.peripheral_version.cargo_vision.data_size +
                            '; MCU App :' + iterator.peripheral_version.cargo_vision.mcu_app +
                            '; BLE App :' + iterator.peripheral_version.cargo_vision.ble_app +
                            '; Cam Version Length :' + iterator.peripheral_version.cargo_vision.cam_version_length +
                            '; Cam Version :' + iterator.peripheral_version.cargo_vision.cam_version + ';';
                        } else {
                          peripheralVersion = peripheralVersion + 'Sensor Type : BLE PCT Cargo Vision; Sensor status: ' + iterator.peripheral_version.cargo_vision.sensor_status + '; ';
                        }
                      }
                      if (iterator.peripheral_version.riot_cargo_or_chassis_sensor) {
                        if (iterator.peripheral_version.riot_cargo_or_chassis_sensor.sensor_status === 'Online') {
                          peripheralVersion = peripheralVersion + 'Sensor Type : Riot Cargo/Chassis Sensor; Sensor status : ' + iterator.peripheral_version.riot_cargo_or_chassis_sensor.sensor_status +
                            '; Size :' + iterator.peripheral_version.riot_cargo_or_chassis_sensor.data_size +
                            '; Hardware Version :' + iterator.peripheral_version.riot_cargo_or_chassis_sensor.hardware_version +
                            '; Firmware Version :' + iterator.peripheral_version.riot_cargo_or_chassis_sensor.firmware_version + '; ';
                        } else {
                          peripheralVersion = peripheralVersion + 'Sensor Type : Riot Cargo/Chassis Sensor; Sensor status: ' + iterator.peripheral_version.riot_cargo_or_chassis_sensor.sensor_status + '; ';
                        }
                      }
                      if (iterator.peripheral_version.hegemon_absreader) {
                        if (iterator.peripheral_version.hegemon_absreader.sensor_status === 'Online') {
                          peripheralVersion = peripheralVersion + 'Sensor Type : Hegemon PLC Reader; Sensor status : ' + iterator.peripheral_version.hegemon_absreader.sensor_status +
                            '; Size :' + iterator.peripheral_version.hegemon_absreader.data_size +
                            '; Version :' + iterator.peripheral_version.hegemon_absreader.version + '; ';
                        } else {
                          peripheralVersion = peripheralVersion + 'Sensor Type : Hegemon PLC Reader; Sensor status : ' + iterator.peripheral_version.hegemon_absreader.sensor_status + '; ';
                        }
                      }
                      if (iterator.peripheral_version.abs_reader) {
                        if (iterator.peripheral_version.abs_reader.sensor_status === 'Online') {
                          peripheralVersion = peripheralVersion + 'Sensor Type : Semitech ABS Reader; Sensor status : ' + iterator.peripheral_version.abs_reader.sensor_status +
                            '; Size :' + iterator.peripheral_version.abs_reader.data_size + '; Version : ' + iterator.peripheral_version.abs_reader.version + '; ';
                        } else {
                          peripheralVersion = peripheralVersion + 'Sensor Type : Semitech ABS Reader; Sensor status : ' + iterator.peripheral_version.abs_reader.sensor_status + '; ';
                        }
                      }
                      if (iterator.peripheral_version.temperature_sensor) {
                        if (iterator.peripheral_version.temperature_sensor.sensor_status === 'Online') {
                          peripheralVersion = peripheralVersion + 'Sensor Type : BLE PCT Temp Sensor; Sensor status : ' + iterator.peripheral_version.temperature_sensor.sensor_status +
                            '; Size :' + iterator.peripheral_version.temperature_sensor.data_size + ';';
                        } else {
                          peripheralVersion = peripheralVersion + 'Sensor Type : BLE PCT Temp Sensor; Sensor status : ' + iterator.peripheral_version.temperature_sensor.sensor_status + '; ';
                        }
                      }
                      if (iterator.peripheral_version.lamp_check_atis) {
                        if (iterator.peripheral_version.lamp_check_atis.sensor_status === 'Online') {
                          peripheralVersion = peripheralVersion + 'Sensor Type : Lamp Check ATIS; Sensor status : ' + iterator.peripheral_version.lamp_check_atis.sensor_status +
                            '; Size :' + iterator.peripheral_version.lamp_check_atis.data_size + ';  Version : ' + iterator.peripheral_version.lamp_check_atis.version + '; ';
                        } else {
                          peripheralVersion = peripheralVersion + 'Sensor Type : Lamp Check ATIS; Sensor status : ' + iterator.peripheral_version.lamp_check_atis.sensor_status + '; ';
                        }
                      }
                      if (iterator.peripheral_version.door_sensor) {
                        if (iterator.peripheral_version.door_sensor.sensor_status === 'Online') {
                          peripheralVersion = peripheralVersion + 'Sensor Type : BLE PCT Door Sensor; Sensor status : ' + iterator.peripheral_version.door_sensor.sensor_status +
                            '; Size :' + iterator.peripheral_version.door_sensor.data_size + '; Version : ' + iterator.peripheral_version.door_sensor.version + ';';
                        } else {
                          peripheralVersion = peripheralVersion + 'Sensor Type : BLE PCT Door Sensor; Sensor status : ' + iterator.peripheral_version.door_sensor.sensor_status + ';';
                        }
                      }
                      iterator.peripheral_version = peripheralVersion;
                    }
                    if (iterator.chassis) {
                      // if (iterator.chassis.condition === 'Unresponsive') {
                      //   iterator.chassis = iterator.chassis.condition;
                      // } else if (iterator.chassis.condition === 'Sensor Fault' || iterator.chassis.condition === 'Sensor Calibration Error') {
                      //   iterator.chassis = iterator.chassis.condition + ': codes ' + iterator.chassis.code1 + ', ' + iterator.chassis.code2;
                      // } else {
                      //   iterator.chassis = iterator.chassis.condition + ': codes ' + iterator.chassis.code1 + ', ' + iterator.chassis.code2 + ', ' + iterator.chassis.cargo_state + ', ' + iterator.chassis.dist_mm + 'mm';
                      // }
                      if (iterator.chassis.condition === "Healthy") {
                        iterator.chassis = iterator.chassis.condition + ', Codes: ' + iterator.chassis.code1 + ', ' + iterator.chassis.code2 + ', ' + iterator.chassis.cargo_state + ', ' + iterator.chassis.dist_mm + 'mm';
                      } else {
                        iterator.chassis = iterator.chassis.condition + ', Codes: ' + iterator.chassis.code1 + ', ' + iterator.chassis.code2;
                      }
                    }
                    let psiAirSupply = '';
                    if (iterator.psi_air_supply != null && iterator.psi_air_supply != undefined && iterator.psi_air_supply.psi_air_supply_measure != null && iterator.psi_air_supply.psi_air_supply_measure !== undefined) {
                      for (let i = 0; i < iterator.psi_air_supply.psi_air_supply_measure.length; i++) {
                        const j = i + 1;
                        psiAirSupply = psiAirSupply + 'ASMS ' + j + ':  T Status_ ' + j + ': ' +
                          iterator.psi_air_supply.psi_air_supply_measure[i].tank_status +
                          '; T Bat_' + j + ': ' + iterator.psi_air_supply.psi_air_supply_measure[i].tank_battery + '; S Status_' + j +
                          ': ' + iterator.psi_air_supply.psi_air_supply_measure[i].supply_status + '; S Bat_' +
                          j + ': ' + iterator.psi_air_supply.psi_air_supply_measure[i].supply_battery +
                          '; Tank Pressure_' + j + ': ' + iterator.psi_air_supply.psi_air_supply_measure[i].tank_pressure +
                          '; Supply Pressure_' + j + ': ' + iterator.psi_air_supply.psi_air_supply_measure[i].supply_pressure;
                      }
                      if (iterator.psi_air_supply.psi_air_supply_measure.length > 0) {
                        iterator.psi_air_supply.psi_air_supply_measure = iterator.psi_air_supply.comm_status + '; ' + psiAirSupply;
                      } else {
                        iterator.psi_air_supply.psi_air_supply_measure = iterator.psi_air_supply.comm_status;
                      }
                    }
                    let psiWheelEnd = '';
                    if (iterator.psi_wheel_end && iterator.psi_wheel_end.psi_wheel_end_measure) {
                      if (iterator.psi_wheel_end.comm_status === 'Online') {
                        if (iterator.psi_wheel_end.axle_elements > 0) {
                          psiWheelEnd = iterator.psi_wheel_end.comm_status + '; L Status_1:' + iterator.psi_wheel_end.psi_wheel_end_measure[0].left_status + '; L Bat_1:' +
                            iterator.psi_wheel_end.psi_wheel_end_measure[0].left_battery + ';  R Status_1:' +
                            iterator.psi_wheel_end.psi_wheel_end_measure[0].right_status + ' R Bat_1:' +
                            iterator.psi_wheel_end.psi_wheel_end_measure[0].right_battery + '; L Temp_1: ' + iterator.psi_wheel_end.psi_wheel_end_measure[0].left_temperature +
                            '; R Temp_1: ' + iterator.psi_wheel_end.psi_wheel_end_measure[0].right_temperature
                            + '; L Status_2:' + iterator.psi_wheel_end.psi_wheel_end_measure[1].left_status +
                            '; L Bat_2:' + iterator.psi_wheel_end.psi_wheel_end_measure[1].left_battery
                            + ';  R Status_2:' + iterator.psi_wheel_end.psi_wheel_end_measure[1].right_status + ' R Bat_2:' + iterator.psi_wheel_end.psi_wheel_end_measure[1].right_battery + '; L Temp_2: ' +
                            iterator.psi_wheel_end.psi_wheel_end_measure[1].left_temperature + '; R Temp_2: ' + iterator.psi_wheel_end.psi_wheel_end_measure[1].right_temperature;
                          iterator.psi_wheel_end.psi_wheel_end_measure = psiWheelEnd;
                        } else {
                          iterator.psi_wheel_end.psi_wheel_end_measure = iterator.psi_wheel_end.comm_status;
                        }

                      } else {
                        iterator.psi_wheel_end.psi_wheel_end_measure = iterator.psi_wheel_end.comm_status;
                      }
                    }
                    let sensorId = '';
                    let ageOfReading = '';
                    let sStatus = '';
                    let humidity = '';
                    let temperature = '';
                    let batteryLevel = '';

                    let pepsiMinew = '';
                    if (iterator.pepsi_temperature) {
                      pepsiMinew = iterator.pepsi_temperature.comm_status + '; ';
                      if (iterator.pepsi_temperature.temperature_elements > 0) {
                        iterator.pepsi_temperature.pepsi_temperaturemeasure.forEach(pepsiTemp => {
                          pepsiMinew = pepsiMinew + 'Mac Address : ' + pepsiTemp.mac_address + '; Battery Level : ' + pepsiTemp.battery_level + '%; Temperature : ' + pepsiTemp.temperature + 'C ; ';
                          sensorId = sensorId + pepsiTemp.sensor_id + ((iterator.pepsi_temperature.temperature_elements > 1) ? ',' : '');
                          ageOfReading = ageOfReading + pepsiTemp.age_of_reading + ((iterator.pepsi_temperature.temperature_elements > 1) ? ',' : '');
                          sStatus = sStatus + pepsiTemp.s_status + ((iterator.pepsi_temperature.temperature_elements > 1) ? ',' : '');
                          humidity = humidity + pepsiTemp.humidity + ((iterator.pepsi_temperature.temperature_elements > 1) ? ',' : '');
                          temperature = temperature + pepsiTemp.temperature + ((iterator.pepsi_temperature.temperature_elements > 1) ? 'C,' : 'C');
                          batteryLevel = batteryLevel + pepsiTemp.battery_level + ((iterator.pepsi_temperature.temperature_elements > 1) ? '%,' : '%');
                        });
                      }
                      iterator.pepsi_temperature.sensor_id = sensorId;
                      iterator.pepsi_temperature.age_of_reading = ageOfReading;
                      iterator.pepsi_temperature.s_status = sStatus;
                      iterator.pepsi_temperature.humidity = humidity;
                      iterator.pepsi_temperature.temperature = temperature;
                      iterator.pepsi_temperature.battery_level = batteryLevel;
                      iterator.pepsi_temperature.all_status = pepsiMinew + ' Total Sensors : ' + iterator.pepsi_temperature.temperature_elements + '; ';
                    }
                    let tpmsAlpha = '';
                    if (iterator.tpms_alpha != null && iterator.tpms_alpha !== undefined && iterator.tpms_alpha.tpms_measures != null && iterator.tpms_alpha.tpms_measures !== undefined) {
                      if (iterator.tpms_alpha.num_sensors > 0) {
                        if (iterator.tpms_alpha.tpms_measures && iterator.tpms_alpha.tpms_measures.length > 0) {
                          iterator.tpms_alpha.tpms_measures.forEach((alphaTpms: any) => {
                            let temperature = '';
                            let pressure = '';
                            if (alphaTpms.tire_pressure_psi <= 0) {
                              pressure = '--';
                              tpmsAlpha = tpmsAlpha + '-- , ';
                            } else {
                              pressure = alphaTpms.tire_pressure_psi + 'psi';
                              tpmsAlpha = tpmsAlpha + alphaTpms.tire_pressure_psi + 'psi, ';
                            }
                            if (alphaTpms.tire_temperature_c <= -50) {
                              temperature = '--';
                              tpmsAlpha = tpmsAlpha + ' -- ;';
                            } else {
                              temperature = alphaTpms.tire_temperature_f + 'F';
                              tpmsAlpha = tpmsAlpha + alphaTpms.tire_temperature_f + 'F; ';
                            }
                            if (alphaTpms.tire_location_str === 'FLO' || alphaTpms.tire_location_str === 'LFO' || alphaTpms.tire_location_str === 'LOF' || alphaTpms.tire_location_str === 'FOL' || alphaTpms.tire_location_str === 'OFL' || alphaTpms.tire_location_str === 'OLF') {
                              iterator.tpms_alpha.lof = alphaTpms.tire_location_str;
                              iterator.tpms_alpha.lofTemp = temperature;
                              iterator.tpms_alpha.lofPsi = pressure;
                            }
                            if (alphaTpms.tire_location_str === 'LIF' || alphaTpms.tire_location_str === 'LFI' || alphaTpms.tire_location_str === 'ILF' || alphaTpms.tire_location_str === 'IFL' || alphaTpms.tire_location_str === 'FLI' || alphaTpms.tire_location_str === 'FIL') {
                              iterator.tpms_alpha.lif = alphaTpms.tire_location_str;
                              iterator.tpms_alpha.lifTemp = temperature;
                              iterator.tpms_alpha.lifPsi = pressure;
                            }
                            if (alphaTpms.tire_location_str === 'RIF' || alphaTpms.tire_location_str === 'RFI' || alphaTpms.tire_location_str === 'IRF' || alphaTpms.tire_location_str === 'IFR' || alphaTpms.tire_location_str === 'FRI' || alphaTpms.tire_location_str === 'FIR') {
                              iterator.tpms_alpha.rif = alphaTpms.tire_location_str;
                              iterator.tpms_alpha.rifTemp = temperature;
                              iterator.tpms_alpha.rifPsi = pressure;
                            }
                            if (alphaTpms.tire_location_str === 'ROF' || alphaTpms.tire_location_str === 'RFO' || alphaTpms.tire_location_str === 'ORF' || alphaTpms.tire_location_str === 'OFR' || alphaTpms.tire_location_str === 'FRO' || alphaTpms.tire_location_str === 'FOR') {
                              iterator.tpms_alpha.rof = alphaTpms.tire_location_str;
                              iterator.tpms_alpha.rofTemp = temperature;
                              iterator.tpms_alpha.rofPsi = pressure;
                            }
                            if (alphaTpms.tire_location_str === 'LOR' || alphaTpms.tire_location_str === 'LRO' || alphaTpms.tire_location_str === 'OLR' || alphaTpms.tire_location_str === 'ORL' || alphaTpms.tire_location_str === 'RLO' || alphaTpms.tire_location_str === 'ROL') {
                              iterator.tpms_alpha.lor = alphaTpms.tire_location_str;
                              iterator.tpms_alpha.lorTemp = temperature;
                              iterator.tpms_alpha.lorPsi = pressure;
                            }
                            if (alphaTpms.tire_location_str === 'LIR' || alphaTpms.tire_location_str === 'LRI' || alphaTpms.tire_location_str === 'ILR' || alphaTpms.tire_location_str === 'IRL' || alphaTpms.tire_location_str === 'RLI' || alphaTpms.tire_location_str === 'RIL') {
                              iterator.tpms_alpha.lir = alphaTpms.tire_location_str;
                              iterator.tpms_alpha.lirTemp = temperature;
                              iterator.tpms_alpha.lirPsi = pressure;
                            }
                            if (alphaTpms.tire_location_str === 'RIR' || alphaTpms.tire_location_str === 'IRR' || alphaTpms.tire_location_str === 'RRI') {
                              iterator.tpms_alpha.rir = alphaTpms.tire_location_str;
                              iterator.tpms_alpha.rirTemp = temperature;
                              iterator.tpms_alpha.rirPsi = pressure;
                            }
                            if (alphaTpms.tire_location_str === 'ROR' || alphaTpms.tire_location_str === 'ORR' || alphaTpms.tire_location_str === 'RRO') {
                              iterator.tpms_alpha.ror = alphaTpms.tire_location_str;
                              iterator.tpms_alpha.rorTemp = temperature;
                              iterator.tpms_alpha.rorPsi = pressure;
                            }
                          });
                        }
                      }
                      if (iterator.tpms_alpha.num_sensors > 0) {
                        iterator.tpms_alpha.tpms_measures_all = iterator.tpms_alpha.status + '; ' + tpmsAlpha;
                      } else {
                        iterator.tpms_alpha.tpms_measures_all = iterator.tpms_alpha.status;
                      }
                    }
                    let lite_entry_debug = '';
                    if (iterator.lite_sentry_debug !== null && iterator.lite_entry_debug !== undefined && iterator.lite_entry_debug.lite_entry_debug_measure != null && iterator.lite_entry_debug.lite_entry_debug_measure !== undefined) {
                      for (let i = 0; i < iterator.lite_entry_debug.lite_entry_debug_measure.length; i++) {
                        const j = i + 1;
                        lite_entry_debug = 'Scenario_ID_' + j + iterator.lite_entry_debug.lite_entry_debug_measure[i].scenario_id + ':  Minimum_Voltage_' + j + ': ' +
                          iterator.lite_entry_debug.lite_entry_debug_measure[i].minimum_voltage +
                          ' mV; Maximum_Voltage__' + j + ': ' + iterator.lite_entry_debug.lite_entry_debug_measure[i].maximum_voltage + ' mV; Fault_Voltage_' + j +
                          iterator.lite_entry_debug.lite_entry_debug_measure[i].fault_voltage + ' mV; Minimum_Current_' + j +
                          ': ' + iterator.lite_entry_debug.lite_entry_debug_measure[i].minimum_current + ' mA; Maximum_Current_' +
                          j + ': ' + iterator.lite_entry_debug.lite_entry_debug_measure[i].maximum_current +
                          ' mA; Fault_Current_' + j + ': ' + iterator.lite_entry_debug.lite_entry_debug_measure[i].fault_current +
                          ' mA; Amount_' + j + ': ' + iterator.lite_entry_debug.lite_entry_debug_measure[i].amount + '; Count_' + j + ': ' + iterator.lite_entry_debug.lite_entry_debug_measure[i].count;
                      }
                      iterator.lite_sentry_debug.lite_entry_debug_measure = lite_entry_debug;
                    }
                    let abs_beta_info = '';
                    if (iterator.abs_beta !== null && iterator.abs_beta !== undefined && iterator.abs_beta.absbeta_measure !== null && iterator.abs_beta.absbeta_measure !== undefined) {
                      for (let i = 0, j = 0; i < iterator.abs_beta.absbeta_measure.length; i++) {
                        if (iterator.abs_beta.absbeta_measure[i].active_status === "Active") {
                          iterator.abs_beta.active_status = "Active";
                          j = j + 1;
                          abs_beta_info = abs_beta_info + 'FMI_code_' + j + ': ' + iterator.abs_beta.absbeta_measure[i].fmi + '; ' + iterator.abs_beta.absbeta_measure[i].sid_pid_status + '_' + j + ': ' + iterator.abs_beta.absbeta_measure[i].sid_pid +
                            '; Count_' + j + ': ' + iterator.abs_beta.absbeta_measure[i].occurrence_count + '; ';
                        }
                      }
                      if (abs_beta_info === '' && iterator.abs_beta.active_status !== "Active") {
                        abs_beta_info = "NA";
                        iterator.abs_beta.active_status = "NA";
                      }
                      iterator.abs_beta.absbeta_measure = abs_beta_info;
                    }
                    let abs_info = '';
                    if (iterator.beta_abs_id != null && iterator.beta_abs_id !== undefined) {
                      if (iterator.beta_abs_id.manufacturer_info == null) {
                        abs_info = iterator.beta_abs_id.status + ', Manufacturer: NA ,  Serial Number: ' +
                          iterator.beta_abs_id.serial_num + ', Firmware version: ' + iterator.beta_abs_id.abs_fw_version +
                          ', Model: ' + iterator.beta_abs_id.model;
                      } else {
                        abs_info = iterator.beta_abs_id.status + ', Manufacturer: ' +
                          iterator.beta_abs_id.manufacturer_info + ', Serial Number: ' +
                          iterator.beta_abs_id.serial_num + ', Firmware version: ' + iterator.beta_abs_id.abs_fw_version + ', Model: ' + iterator.beta_abs_id.model;
                      }
                      iterator.beta_abs_id.status = abs_info;
                    }
                    if (iterator.cargo_camera_sensor) {
                      if (iterator.cargo_camera_sensor.upload_status === 'Success Uploaded to image server' || iterator.cargo_camera_sensor.upload_status === 'Partial Upload due to Camera connection unstable'
                        || iterator.cargo_camera_sensor.upload_status === 'Partial Upload due to Camera connection unstable') {
                        iterator.cargo_camera_sensor.uri = iterator.cargo_camera_sensor.uri;
                      }
                      if (iterator.cargo_camera_sensor.camera_voltage === null) {
                        iterator.cargo_camera_sensor.camera_voltage = 'NA';
                      } else {
                        iterator.cargo_camera_sensor.camera_voltage = iterator.cargo_camera_sensor.camera_voltage;
                      }
                      if (iterator.cargo_camera_sensor.prediction_value === null || iterator.cargo_camera_sensor.prediction_value === undefined) {
                        iterator.cargo_camera_sensor.prediction_value = 'NA';
                      }
                      else {
                        iterator.cargo_camera_sensor.prediction_value = iterator.cargo_camera_sensor.prediction_value;
                      }
                    }
                    let bleTemp = '';
                    if (iterator.ble_temperature && iterator.ble_temperature.ble_temperature_sensormeasure) {
                      if (iterator.ble_temperature.ble_temperature_sensormeasure && iterator.ble_temperature.ble_temperature_sensormeasure.length > 0) {
                        iterator.ble_temperature.ble_temperature_sensormeasure.forEach((el: any) => {
                          if (el.data_reading && el.data_reading.length > 0) {
                            for (const dataReading of el.data_reading) {
                              bleTemp = bleTemp + dataReading.temperature_readings.toString();
                            }
                          }
                        });
                      }
                      iterator.ble_temperature.ble_temperature_sensormeasure = bleTemp;
                    }
                    if (iterator.advertisement_maxlink && iterator.advertisement_maxlink.advertisement_maxlink_measure) {
                      if (iterator.advertisement_maxlink.advertisement_maxlink_measure != null && iterator.advertisement_maxlink.advertisement_maxlink_measure.length > 0) {
                        if (iterator.advertisement_maxlink.advertisement_maxlink_measure[0].s_status === "Online") {
                          iterator.advertisement_maxlink.lift_gate_voltage = iterator.advertisement_maxlink.advertisement_maxlink_measure[0].lift_gate_voltage;
                          iterator.advertisement_maxlink.state_of_charge = iterator.advertisement_maxlink.advertisement_maxlink_measure[0].state_of_charge;
                        }
                      }
                    }
                    if (iterator.connectable_maxlink && iterator.connectable_maxlink.connectable_maxlink_measure) {
                      if (iterator.connectable_maxlink.connectable_maxlink_measure != null && iterator.connectable_maxlink.connectable_maxlink_measure.length > 0) {
                        if (iterator.connectable_maxlink.connectable_maxlink_measure[0].char_sta === "Online") {
                          iterator.connectable_maxlink.lift_gate_cycles = iterator.connectable_maxlink.connectable_maxlink_measure[0].lift_gate_cycles;
                          if (iterator.connectable_maxlink.connectable_maxlink_measure[0].motor1_runtime !== null) {
                            iterator.connectable_maxlink.motor1_runtime = iterator.connectable_maxlink.connectable_maxlink_measure[0].motor1_runtime;
                          } else {
                            iterator.connectable_maxlink.motor1_age_code = iterator.connectable_maxlink.connectable_maxlink_measure[0].motor1_age_code;
                          }
                          if (iterator.connectable_maxlink.connectable_maxlink_measure[0].motor2_runtime !== null) {
                            iterator.connectable_maxlink.motor2_runtime = iterator.connectable_maxlink.connectable_maxlink_measure[0].motor2_runtime;
                          } else {
                            iterator.connectable_maxlink.motor2_age_code = iterator.connectable_maxlink.connectable_maxlink_measure[0].motor2_age_code;
                          }
                          iterator.connectable_maxlink.status_flags = iterator.connectable_maxlink.connectable_maxlink_measure[0].status_flags;
                        }
                      }
                    }
                  }
                }
                // this.rowData = this.elasticRowData;
                this.rowData = data.tlvData;
                console.log(this.counts);
                // params.successCallback(this.rowData, data.totalHits.value);
                params.successCallback(this.rowData, data.totalHits);
                if (this.rowData.length <= 0) {
                  this.gridApi.showNoRowsOverlay();
                }
                this.autoSizeAll(true);
                setTimeout(() => {
                  this.autoSizeAll(true);
                }, 5000);

              },
                error => {
                  this.loading = false;
                  console.log(error);
                });
          } else {
            this.loading = false;
          }
        } else {
          this.rowData = [];
          params.successCallback(this.rowData, 0);
          this.autoSizeAll(false);
          this.gridApi.showNoRowsOverlay();
          setTimeout(() => {
            this.gridApi.showNoRowsOverlay();
          }, 3000);
        }
      }
    };
    this.gridApi.setDatasource(datasource);
  }

  isEmptyObject(object): boolean {
    for (const key in object) {
      if (object.hasOwnProperty(key)) {
        return false;
      }
    }
    return true;
  }
  onDragStopped(params: any) {
    this.saveGridStateInSession(this.gridColumnApi.getColumnState());
  }
  onColumnResized(params: any) {
    this.saveGridStateInSession(this.gridColumnApi.getColumnState());
  }
  onSortChanged(params: any) {
    this.currentPage = 0;
    this.isFirst = true;
    this.saveGridStateInSession(this.gridColumnApi.getColumnState());
  }

  onFilterChanged(params: any) {
    this.isFirst = true;
    this.currentPage = 0;
    sessionStorage.setItem('all_device_report_grid_filter_state', JSON.stringify(params.api.getFilterModel()));
  }

  onColumnVisible(params: any) {
    this.saveGridStateInSession(this.gridColumnApi.getColumnState());
  }

  onColumnPinned(params: any) {
    this.saveGridStateInSession(this.gridColumnApi.getColumnState());
  }


  onFirstDataRendered(params: any) {
    this.isFirstTimeLoad = true;
  }

  autoSizeAll(skipHeader: boolean) {
    const columState = JSON.parse(sessionStorage.getItem('all_device_report_grid_column_state'));
    const columGroupState = JSON.parse(sessionStorage.getItem('all_device_report_grid_column_group_state'));
    const filterSavedState = JSON.parse(sessionStorage.getItem('all_device_report_grid_filter_state'));

    // if (!columState && !filterSavedState) {
    const allColumnIds: string[] = [];
    this.gridColumnApi.getAllColumns().forEach((column) => {
      allColumnIds.push(column.getId());
    });
    this.gridColumnApi.autoSizeColumns(allColumnIds, skipHeader);
    // }
  }

  getReportColumnDefs() {
    const cutomeDatepipe = new CustomDatePipe();
    this.gatewayListService.getEventList().subscribe(
      data => {
        this.eventList = data.body;
        this.eventNameList = [];
        for (const iterator of this.eventList) {
          this.eventNameList.push(iterator.eventType);
        }

        const filterParamsForEvent = {
          values: params => params.success(this.eventNameList)
        };
        this.gatewayListService.getColumnDefs().subscribe(data => {
          this.columnData = data;
          this.columnGroups();
          // const reportColDefs = JSON.parse(data[4].reportColumnDefs);
          // const values = [];
          // reportColDefs.forEach(element => {
          //   if (element.children) {
          //     element.children.forEach(elements => {
          //       const val = 'private String ' + (elements.headerName.replace(/\s/g, '')) + ';';
          //       values.push(val);
          //       if (elements.headerName === 'Received Time' || elements.headerName === 'GPS Time' || elements.headerName === 'RTC Time') {
          //         elements.cellRenderer = (data2) => {
          //           if (data2) {
          //             if (data2.value) {
          //               const newDate = new Date(data2.value);
          //               const newDate2 = new Date('2000-01-01 00:00:00.00000');
          //               if (newDate < newDate2) {
          //                 return 'N/A';
          //               } else {
          //                 return cutomeDatepipe.transform(data2.value, 'report');
          //               }
          //             } else {
          //               // console.log(data2);
          //               // return 'N/A';
          //             }
          //           }
          //         };
          //       }
          //       if (elements.headerName === 'Device ID') {
          //         elements.cellRenderer = (params) => {
          //           if (params.data !== undefined && params.data.report_header !== undefined && params.data.report_header !== null && params.data.report_header.device_id !== undefined) {
          //             return '<div class="pointer" style="color:#4ba9d7;"><a>' + params.data.report_header.device_id + '</a></div>';
          //           }
          //         };
          //       }
          //       if (elements.headerName === 'Event') {
          //         elements.filterParams = filterParamsForEvent;
          //       }
          //       if (elements.headerName === 'Thumbnail') {
          //         elements.cellRenderer = (params) => {
          //           // if (params.data && params.data.cargo_camera_sensor && params.data.cargo_camera_sensor.uri && params.data.cargo_camera_sensor.uri.length > 38
          //           //   && params.data.general.company_id !== 'Amazon' && params.data.general.company_id !== 'amazon') {
          //           //   return '<img width="100%" height="100px" src="' + params.data.cargo_camera_sensor.uri + '">';
          //           // } else if (params.data && params.data.cargo_camera_sensor && params.data.cargo_camera_sensor.uri && params.data.general.company_id !== 'Amazon' && params.data.general.company_id !== 'amazon') {
          //           //   return params.data.cargo_camera_sensor.uri;
          //           // } else {
          //           //   return '';
          //           // }
          //           if (params.data && params.data.cargo_camera_sensor && params.data.cargo_camera_sensor.uri && params.data.cargo_camera_sensor.uri.length > 0) {
          //             if (params.data.cargo_camera_sensor.uri.includes('http')) {
          //               return '<img width="100%" height="100px" src="' + params.data.cargo_camera_sensor.uri + '">';
          //             } else if (params.data.general.company_id === 'Amazon' || params.data.general.company_id === 'amazon') {
          //               return '<img width="100%" height="100px" src="http://images.phillips-connect.net/' + params.data.cargo_camera_sensor.uri + '">';
          //             }
          //           }
          //         };
          //       }
          //       if (elements.headerName === 'GPS') {
          //         elements.cellRenderer = (params) => {
          //           if (params.data && params.data.general_mask_fields && params.data.general_mask_fields.gpsstatus_index) {
          //             if (params.data.general_mask_fields.gpsstatus_index !== 'Locked') {
          //               return '<div style="font-style: italic;">' + params.data.general_mask_fields.gpsstatus_index + '</div>';
          //             } else {
          //               return params.data.general_mask_fields.gpsstatus_index;
          //             }
          //           } else {
          //             return '';
          //           }
          //         };
          //       }

          //       if (elements.headerName === 'Seq #') {
          //         elements.cellRenderer = (params) => {
          //           if (params.data && params.data.report_header && (params.data.report_header.sequence || params.data.report_header.sequence === 0)) {
          //             if (this.counts[params.data.report_header.device_id][params.data.report_header.sequence] > 1) {
          //               return '<div style="background:#EDD3D3">' + params.data.report_header.sequence + '</div>';
          //             } else {
          //               return params.data.report_header.sequence;
          //             }
          //           } else {
          //             return '<div style="background:#FEF5BD"> &nbsp; </div>';
          //           }
          //         };
          //       }

          //       if (elements.headerName === 'Raw Report') {
          //         elements.cellRenderer = (params) => {
          //           if (params.data && params.data.general && params.data.general.rawreport) {
          //             return '<div style="font-family: monospace;"> ' + params.data.general.rawreport + ' </div>';
          //           }
          //         };
          //       }
          //     });
          //   }
          // });
          // this.columnDefs = reportColDefs;


          // const columState = JSON.parse(sessionStorage.getItem('all_device_report_grid_column_state'));
          // const filterSavedState = sessionStorage.getItem('all_device_report_grid_filter_state');

          // if (columState) {
          //   setTimeout(() => {
          //     this.gridColumnApi.applyColumnState({ state: columState, applyOrder: true, });
          //   }, 300);
          // }
          // if (filterSavedState) {
          //   setTimeout(() => {
          //     this.gridApi.setFilterModel(JSON.parse(filterSavedState));
          //   }, 1000);
          // }
        },
          (error: any) => {
            console.log(error);
          });
      }
    );
  }


  // headerHeightSetter(event: any) {
  //   this.gridApi.setHeaderHeight(headerHeightGetter() + 20);
  //   this.gridApi.resetRowHeights();
  // }

  onPageSizeChange() {
    sessionStorage.setItem('gatewayListsize', String(this.selected));
    this.pageLimit = (Number(this.selected));
    this.gridApi.gridOptionsWrapper.setProperty('cacheBlockSize', this.selected);
    const api: any = this.gridApi;
    api.rowModel.resetCache();
    this.gridApi.paginationSetPageSize(Number(this.selected));
  }

  show() {
    this.advanceOption = !this.advanceOption;
  }




  initilizeFilterForm(data?: any) {
    this.filterForm = this.formBuldier.group({
      imei_selection: [data && data.imei_selection ? data.imei_selection : '1'],
      imei_ids: [data && data.imei_ids ? data.imei_ids : ''],
      asset_selection: [data && data.asset_selection ? data.asset_selection : '1'],
      asset_ids: [data && data.asset_ids ? data.asset_ids : ''],
      customer_list: [data && data.customer_list ? data.customer_list : ''],
      date_rang: [data && data.date_rang ? data.date_rang : 'last_24_hour'],
      from: [data && data.from ? data.from : ''],
      to: [data && data.to ? data.to : ''],
      customer_name: [data && data.customer_name ? data.customer_name : ''],
      device_type: [data && data.device_type ? data.device_type : ''],
      source: [data && data.source ? data.source : '1']
    });
    if (data && data.date_rang) {
      this.selectChangeHandler({ value: data.date_rang }, false);
    } else {
      this.selectChangeHandler({ value: 'last_24_hour' }, false);
    }
    this.filterForm.updateValueAndValidity();
  }

  SearchData(data) {
    this.filterForm.controls.customer_list.patchValue([]);
    this.getAllCustomer(data);
  }
  toggleAllSelection(event) {
    if (event._selected) {
      let value1 = this.customerList.map(item => item.accountNumber);
      const value2 = this.recentCustomerList.map(item => item.accountNumber);
      // let values=[this.customerList.map(item => item.accountNumber)]
      value1 = value1.concat(value2);
      this.filterForm.controls.customer_list
        .patchValue(value1);
      event._selected = true;
      if (this.filterForm.value.customer_list && this.filterForm.value.customer_list.length === 1 && this.filterForm.value.customer_list[0] !== 0) {
        const obj = this.customerList.find(item => item.accountNumber === this.filterForm.value.customer_list[0]);
        const obj2 = this.recentCustomerList.find(item => item.accountNumber === this.filterForm.value.customer_list[0]);
        if (obj) {
          this.customerName = obj.organisationName;
        }
        if (obj2) {
          this.customerName = obj2.organisationName;
        }
      }
      this.filterForm.updateValueAndValidity();

    } else {
      event._selected = false;
      this.filterForm.controls.customer_list.patchValue([]);
    }
  }

  clearAdvanceFilter(data) {
    switch (data) {
      case 'ImeiFilter':
        this.filterForm.controls.imei_selection.setValue('1');
        this.filterForm.controls.imei_ids.setValue('');
        this.filterForm.updateValueAndValidity();
        this.imeiFilter = true;
        this.assetFilter = this.customerFilter = this.reportFilter = this.sensorFilter
          = this.eventFilter = this.groupFilter = this.deviceTypeFilter = this.sourceFilter = false;
        break;
      case 'AssetFilter':
        this.filterForm.controls.asset_selection.setValue('1');
        this.filterForm.controls.asset_ids.setValue('');
        this.filterForm.updateValueAndValidity();
        this.assetFilter = true;
        this.imeiFilter = this.customerFilter = this.reportFilter = this.sensorFilter
          = this.eventFilter = this.groupFilter = this.deviceTypeFilter = this.sourceFilter = false;
        break;
      case 'CustomerFilter':
        this.getAllCustomer();
        this.filterForm.controls.customer_list.setValue('');
        this.filterForm.updateValueAndValidity();
        this.customerFilter = true;
        this.assetFilter = this.imeiFilter = this.reportFilter = this.sensorFilter
          = this.eventFilter = this.groupFilter = this.deviceTypeFilter = this.sourceFilter = false;
        break;
      case 'ReportFilter':
        this.filterForm.controls.date_rang.setValue('');
        this.filterForm.controls.from.setValue('');
        this.filterForm.controls.to.setValue('');
        this.filterForm.updateValueAndValidity();
        this.reportFilter = true;
        this.assetFilter = this.customerFilter = this.imeiFilter = this.sensorFilter
          = this.eventFilter = this.groupFilter = this.deviceTypeFilter = this.sourceFilter = false;
        break;
      case 'SensorFilter':
        this.sensorFilter = this.sensorFilter ? false : true;
        this.assetFilter = this.customerFilter = this.reportFilter = this.imeiFilter
          = this.eventFilter = this.groupFilter = this.deviceTypeFilter = this.sourceFilter = false;
        break;
      case 'EventFilter':
        this.eventFilter = this.eventFilter ? false : true;
        this.assetFilter = this.customerFilter = this.reportFilter = this.sensorFilter
          = this.imeiFilter = this.groupFilter = this.deviceTypeFilter = this.sourceFilter = false;
        break;
      case 'GroupFilter':
        this.groupFilter = this.groupFilter ? false : true;
        this.assetFilter = this.customerFilter = this.reportFilter = this.sensorFilter
          = this.eventFilter = this.imeiFilter = this.deviceTypeFilter = this.sourceFilter = false;
        break;
      case 'DeviceTypeFilter':
        this.filterForm.controls.device_type.setValue('');
        this.filterForm.updateValueAndValidity();
        this.deviceTypeFilter = true;
        this.assetFilter = this.customerFilter = this.reportFilter = this.sensorFilter
          = this.eventFilter = this.groupFilter = this.imeiFilter = this.sourceFilter = false;
        break;
      case 'SourceFilter':
        this.filterForm.controls.source.setValue('1');
        this.filterForm.updateValueAndValidity();
        this.sourceFilter = true;
        this.assetFilter = this.customerFilter = this.reportFilter = this.sensorFilter
          = this.eventFilter = this.groupFilter = this.imeiFilter = this.deviceTypeFilter
          = false;
        break;
      default:
        break;
    }
    this.advanceFilter();
  }



  showHideFilter(data: any) {
    // this.isFilterClick = true;
    switch (data) {
      case 'ImeiFilter':
        this.imeiFilter = this.imeiFilter ? false : true;
        this.assetFilter = this.customerFilter = this.reportFilter = this.sensorFilter
          = this.eventFilter = this.groupFilter = this.deviceTypeFilter = this.sourceFilter = false;
        break;
      case 'AssetFilter':
        this.assetFilter = this.assetFilter ? false : true;
        this.imeiFilter = this.customerFilter = this.reportFilter = this.sensorFilter
          = this.eventFilter = this.groupFilter = this.deviceTypeFilter = this.sourceFilter = false;
        break;
      case 'CustomerFilter':
        this.customerFilter = this.customerFilter ? false : true;
        this.assetFilter = this.imeiFilter = this.reportFilter = this.sensorFilter
          = this.eventFilter = this.groupFilter = this.deviceTypeFilter = this.sourceFilter = false;
        break;
      case 'ReportFilter':
        this.reportFilter = this.reportFilter ? false : true;
        this.assetFilter = this.customerFilter = this.imeiFilter = this.sensorFilter
          = this.eventFilter = this.groupFilter = this.deviceTypeFilter = this.sourceFilter = false;
        break;
      case 'SensorFilter':
        this.sensorFilter = this.sensorFilter ? false : true;
        this.assetFilter = this.customerFilter = this.reportFilter = this.imeiFilter
          = this.eventFilter = this.groupFilter = this.deviceTypeFilter = this.sourceFilter = false;
        break;
      case 'EventFilter':
        this.eventFilter = this.eventFilter ? false : true;
        this.assetFilter = this.customerFilter = this.reportFilter = this.sensorFilter
          = this.imeiFilter = this.groupFilter = this.deviceTypeFilter = this.sourceFilter = false;
        break;
      case 'GroupFilter':
        this.groupFilter = this.groupFilter ? false : true;
        this.assetFilter = this.customerFilter = this.reportFilter = this.sensorFilter
          = this.eventFilter = this.imeiFilter = this.deviceTypeFilter = this.sourceFilter = false;
        break;
      case 'DeviceTypeFilter':
        this.deviceTypeFilter = this.deviceTypeFilter ? false : true;
        this.assetFilter = this.customerFilter = this.reportFilter = this.sensorFilter
          = this.eventFilter = this.groupFilter = this.imeiFilter = this.sourceFilter = false;
        break;
      case 'SourceFilter':
        this.sourceFilter = this.sourceFilter ? false : true;
        this.assetFilter = this.customerFilter = this.reportFilter = this.sensorFilter
          = this.eventFilter = this.groupFilter = this.imeiFilter = this.deviceTypeFilter
          = false;
        break;
      default:
        break;
    }
  }

  hide() {
    //!this.isFilterClick &&
    if ((this.imeiFilter || this.assetFilter || this.customerFilter
      || this.reportFilter || this.sensorFilter
      || this.eventFilter || this.groupFilter
      || this.deviceTypeFilter || this.sourceFilter)) {
      this.imeiFilter = this.assetFilter = this.customerFilter = this.reportFilter = this.sensorFilter
        = this.eventFilter = this.groupFilter = this.deviceTypeFilter = this.sourceFilter = false;
    }
    // this.isFilterClick = false;
  }

  advanceFilter() {
    sessionStorage.setItem('all_report_advance_save_filter', JSON.stringify(this.filterForm.value));
    this.hide();
    if (this.gridApi) {
      this.gridApi.onFilterChanged();
    }
    this.getAllCustomer();
  }

  selectSpecificImei() {
    this.filterForm.controls.imei_selection.setValue('2');
  }

  selectSpecificAssets() {
    this.filterForm.controls.asset_selection.setValue('2');
  }

  selectSpecificImei2() {
    this.filterForm.controls.imei_selection.setValue('2');
    this.advanceFilter();
  }

  selectSpecificAssets2() {
    this.filterForm.controls.asset_selection.setValue('2');
    this.advanceFilter();
  }

  selectAnyForSpecificImei() {
    this.filterForm.controls.imei_selection.setValue('1');
    this.advanceFilter();
  }

  selectAnyForSpecificAssets() {
    this.filterForm.controls.asset_selection.setValue('1');
    this.advanceFilter();
  }

}


function headerHeightGetter() {
  const columnHeaderTexts = document.querySelectorAll('.ag-header-cell-text');

  const columnHeaderTextsArray = [];

  columnHeaderTexts.forEach(node => columnHeaderTextsArray.push(node));

  const clientHeights = columnHeaderTextsArray.map(
    headerText => headerText.clientHeight
  );
  const tallestHeaderTextHeight = Math.max(50);
  return tallestHeaderTextHeight;
}



