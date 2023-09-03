import { AgGridAngular } from '@ag-grid-community/angular';
import { ClientSideRowModelModule } from '@ag-grid-community/client-side-row-model';
import { BodyScrollEvent, GridOptions, IGetRowsParams, Module } from '@ag-grid-community/core';
import { ColumnsToolPanelModule } from '@ag-grid-enterprise/column-tool-panel';
import { MenuModule } from '@ag-grid-enterprise/menu';
import { MultiFilterModule } from '@ag-grid-enterprise/multi-filter';
import { RowGroupingModule } from '@ag-grid-enterprise/row-grouping';
import { Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { ATCommandService } from '../atcommand.service';
import { CompaniesService } from '../companies.service';
import { CommonData } from '../constant/common-data';
import { CustomErrorToastrComponent } from '../custom-error-toastr/custom-error-toastr.component';
import { CustomSuccessToastrComponent } from '../custom-success-toastr/custom-success-toastr.component';
import { GatewayListService } from '../gateway-list.service';
import { BtnCellRendererForGatewayDetails } from '../gateway-list/btn-cell-renderer.component';
import { Filter } from '../models/Filter.model';
import { CustomDateComponent } from '../ui-elements/custom-date-component.component';
import { PagerService } from '../util/pagerService';
import { PaginationConstants } from '../util/paginationConstants';
import { ValidatorUtils } from '../util/validatorUtils';
import { BtnCellRendererForRawReport } from './btn-cell-renderer-rr.component';
import { BtnCellRendererForDeviceReport } from './btn-cell-renderer.component';
declare var $: any;
import { BtnCellRendererForRTC } from './btn-cell-renderer-rtc.component';
import { ATCommandListComponent } from '../atcommand-list/atcommand-list.component';
import { ColumnGroupOpenedEvent } from '@ag-grid-enterprise/all-modules';
import { map } from 'rxjs/operators';
import { CustomDatePipe } from '../pipes/custom-date.pipe';
import { DeviceSecurityService } from '../device-security.service';
import { MatRadioButton, MatRadioChange } from '@angular/material';
import { contains } from 'underscore';
import { DeviceKeyExist } from '../models/deviceKeyExist';
import { generateSignedCommand } from '../models/generateSignedCommand';
import * as moment from "moment";
import { DatePipe } from '@angular/common';
import { DeviceDataForwardingComponent } from '../device-data-forwarding/device-data-forwarding.component';
declare var google: any;
@Component({
  selector: 'app-device-profile',
  templateUrl: './device-profile.component.html',
  styleUrls: ['./device-profile.component.css']
})
export class DeviceProfieComponent implements OnInit {
  @ViewChild(ATCommandListComponent) child: ATCommandListComponent;
  @ViewChild('closeAddExpenseModal') closeAddExpenseModal: ElementRef;
  @ViewChild('closeExpenseModalForRetrieveSensor') closeExpenseModalForRetrieveSensor: ElementRef;
  retrieveSensorHeading = 'Request in Progress';
  retrieveSensorMessage = ' Currently retrieving data from the device. It may take up to a few minutes for the sensor data to update in the screen.';
  retrieveSensorErrorMessage = '';
  atCommandSentfailed = false;
  atCommandErrorMessage = 'The update command could not be sent to the device.';
  hideButton = false;
  columnData: any;
  eventNameList: any;
  sortFlag;
  currentPage: any = 0;
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
  pageSizeOptions: PaginationConstants = new PaginationConstants();
  pageSizeObjArr = this.pageSizeOptions.pageSizeObjArr;
  column = 'general_mask_fields.received_time_stamp';
  order = 'desc';
  userCompanyType;
  isSuperAdmin;
  isRoleCustomerManager;
  isGroupColumn = false;
  customer = new Map();
  // parameters for counting records
  filterValues = new Map();
  filterValuesJsonObj: any = {};
  isClearButtonVisible = false;
  loading = false;
  mapViewHideShow = false;
  frameworkComponents;
  public sideBar;
  // ag-grid code
  @ViewChild('myGrid') myGrid: AgGridAngular;
  public gridOptions: Partial<GridOptions>;
  public gridApi;
  public columnDefs;
  public columnDefs2;
  public gridColumnApi;
  public cacheOverflowSize = 2;
  public maxConcurrentDatasourceRequests = 2;
  public infiniteInitialRowCount = 2;
  public maxBlocksInCache = 2;
  public paginationPageSize = 50;
  public defaultColDef;
  public paramList;
  public components;
  rowData: any;
  mapRowData: any;
  receivedTimeStamp: any;
  lastSelectedRow: any
  latestRawReport: string;
  public modules: Module[] = [
    ClientSideRowModelModule,
    MenuModule,
    ColumnsToolPanelModule,
    MultiFilterModule,
    RowGroupingModule,
  ];
  rowModelType = 'infinite';
  // deviceKeyExist : DeviceKeyExist;
  deviceKeyExist: DeviceKeyExist = new DeviceKeyExist();
  generateCommandModel: generateSignedCommand;
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
  selected: number;
  selectedIdsToDelete: string[] = [];
  isChecked = false;
  elasticFilter: Filter = new Filter();
  organisationList: any;
  sensorList: any;
  configuration: any;
  dprovision = [];
  configurationList = [];
  configurationDetails: any;
  redisData: any;
  deviceprovision: any;
  cellularpayload: any;
  spayload = [];
  deviceReports;
  jsonObject: any;
  mainPower: any;
  rawreportform: any;
  count: number;
  validatorUtil: ValidatorUtils = new ValidatorUtils();
  updateFlag = false;
  submitted: boolean;
  rawReport: any;
  format: any;
  type: any;
  isSubmitted = false;
  parsedReport: any;
  elasticRowData: any[];
  coluDefs: any;
  sensorDetailList: any[] = [];
  deviceInstallationTab = false;
  sensorTypeMap: any = CommonData.SENSOR_TYPE_MAP;
  sonsorStatusMap: any = CommonData.SENSOR_STATUS_KEY_VALUE_MAP;
  exportAllDeviceReportColumnName = CommonData.ALL_DEVICE_REPORT_COLUMN_FOR_EXPORT;
  peripheralVersions: any[] = [];
  rowCount: any;
  isGetData = false;
  top = 160;
  isFirst = true;
  reportTab = false;
  campaignTab = false;
  historyTab = false;
  atCommandTab = false;
  deviceForwardingTab = false;
  deviceId: any;
  isFirstTime = true;
  range = 10000;
  eventList: any;
  last_report_date: any;
  counts = {};
  roleCallCenterUser: any;
  sensorId: any;
  sensorDetailUuid: any;
  sensorName = 'TPMS Sensor';
  location: any;
  showLocation: any;
  postionArray = CommonData.SENSOR_POSITION;
  message = 'Valid IDs include the characters A-F and 0-9 only.';
  isInvalid = false;
  isSecureKeyPresent = false;
  isRequireReportSigning = new FormControl(false);
  isRequireVerfication = new FormControl(false);
  expiration_time = new FormControl(0, Validators.pattern('^[0-9]*$'));
  atcommand_request = new FormControl('AT+');
  isAlertPresent = false;
  alertMessage: any;
  radioButtonEvent: any;
  existingKeyDetail: any;
  alertMessageDetail: any;
  signedCommandResponse: any;
  retrySensorButton = false;
  primaryPower: any = 0;
  secondaryPower: any = 0;
  isRolePctUser: any;
  flag: boolean;
  imageUrl: any;
  deviceGroupDetail: any;
  selectedTab = new FormControl(0);

  selectAll = false;
  lat = 47.751076;
  lng = -120.740135;
  zoom = 4;
  groupHeaderHeight = 40;
  headerHeight = 40;
  url = { url: 'assets/image/placeholder.png', scaledSize: { height: 20, width: 20 } };
  get f() { return this.rawreportform.controls; }
  config1: any;
  config2: any;
  config3: any;
  config4: any;
  config5: any;
  constructor(
    private readonly atCommandService: ATCommandService,
    private readonly formBuldier: FormBuilder,
    private readonly gatewayListService: GatewayListService,
    private readonly companiesService: CompaniesService,
    private readonly router: Router,
    private readonly toastr: ToastrService,
    private readonly pagerService: PagerService,
    private readonly deviceSecurityService: DeviceSecurityService,
    private readonly datepipe: DatePipe,
    private readonly activateRoute: ActivatedRoute) {
    this.activateRoute.queryParams.subscribe(params => {
      if (params.tab === 'History') {
        this.historyTab = true;
        this.selectedTab.setValue(6);
      }
    });
    this.roleCallCenterUser = JSON.parse(sessionStorage.getItem('IS_ROLE_CALL_CENTER_USER'));
    this.isRolePctUser = JSON.parse(sessionStorage.getItem('IS_ROLE_PCT_USER'));
    this.deviceId = sessionStorage.getItem('device_id');
    this.rowModelType = 'infinite';
    this.paginationPageSize = 100;

    // this.gridOptions = {
    //   cacheBlockSize: this.pageLimit,
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
      },

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
            suppressMenu: true,
            suppressPivots: true,
            suppressPivotMode: true,
            suppressSideButtons: true,
            suppressColumnFilter: false,
            suppressColumnSelectAll: false,
            suppressColumnExpandAll: false
          },
        },
      ],
      defaultToolPanel: '',
    };

  }

  onBtNormalCols() {
    // this.gridApi.setColumnDefs([]);
    // this.gridApi.setColumnDefs(this.columnDefs);
    // this.onColumnVisible(this.gridApi);
    this.gridApi.onFilterChanged();
  }


  isOpen = false;
  onRowSelected(event) {
    this.isOpen = false;
    this.mapRowData = [];
    this.mapRowData = this.getSelectedRowData();
    this.lastSelectedRow = [];
    this.lastSelectedRow = this.mapRowData[this.mapRowData.length - 1];
    if (this.mapRowData.length > 1) {
      this.zoom = 4;
    } else {
      this.zoom = 7;
    }
    if (this.lastSelectedRow.general_mask_fields) {
      this.isOpen = true;
      this.lat = this.lastSelectedRow.general_mask_fields.latitude;
      this.lng = this.lastSelectedRow.general_mask_fields.longitude;
      // this.zoom = 7;
      this.convertDMS(this.lastSelectedRow.general_mask_fields.latitude, this.lastSelectedRow.general_mask_fields.longitude)
      this.getGeoLocation(this.lastSelectedRow.general_mask_fields.latitude, this.lastSelectedRow.general_mask_fields.longitude);
    } else {
      // this.zoom = 4;
    }


    // console.log("mapdatarow is", this.mapRowData);
    // console.log("last data is ", last);
    // const datas =[]
    // this.mapRowDatas = [];
    // for (const iterator of this.mapRowData) {
    //   if (!this.mapRowDatas[iterator.report_header.device_id]) {
    //     if (this.mapRowDatas[iterator.report_header.device_id] === undefined) {
    //       this.mapRowDatas[iterator.report_header.device_id] = [];
    //     }
    //     this.mapRowDatas[iterator.report_header.device_id].push(iterator);
    //   } else {
    //     this.mapRowDatas[iterator.report_header.device_id].push(iterator);
    //   }
    //   // this.mainMapRowData.push(this.mapRowDatas);
    // }
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

  hideMapView() {
    this.mapViewHideShow = false;
    document.getElementById('btn1').classList.remove('btnDeactivated');
    document.getElementById('btn2').classList.add('btnDeactivated');
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
    const filterParamsForEvent = {
      values: params => params.success(this.eventNameList)
    };
    this.columnDefs2 = [
      {
        headerName: '#', minWidth: 20, width: 20, headerCheckboxSelection: true,
        headerCheckboxSelectionFilteredOnly: true, filter: false,
        checkboxSelection: true, lockPosition: 'right'
      },
      {
        field: 'report_header.event_name',
        filter: 'agSetColumnFilter',
        sortable: false,
        filterParams: filterParamsForEvent,
        headerName: 'Event',
        lockPosition: 'right',
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

    this.lat = this.rowData[0].general_mask_fields.latitude;
    this.lng = this.rowData[0].general_mask_fields.longitude;
  }

  latInDms: any;
  lngInDms: any

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

  marker: any;
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
              if (data2.value) {
                const newDate = new Date(data2.value);
                const newDate2 = new Date('2000-01-01 00:00:00.00000');
                if (newDate < newDate2) {
                  return 'N/A';
                } else {
                  return cutomeDatepipe.transform(data2.value, 'report');
                }
              } else {
                return 'N/A';
              }
            };
          }
          if (element.headerName === 'Event') {
            element.filterParams = filterParamsForEvent;
          }
          if (element.headerName === 'Thumbnail') {
            element.cellRenderer = (data2) => {
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

          if (element.headerName === 'Seq #') {
            element.cellRenderer = (params) => {
              if (params.data && params.data.report_header && (params.data.report_header.sequence || params.data.report_header.sequence === 0)) {
                if (this.counts[params.data.report_header.sequence] > 1) {
                  return '<div style="background:#EDD3D3">' + params.data.report_header.sequence + '</div>';
                } else {
                  return params.data.report_header.sequence;
                }
              } else {
                return '<div style="background:#FEF5BD">  </div>';
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
          if (element.headerName === 'Raw Report') {
            element.cellRenderer = (params) => {
              if (params.data && params.data.general && params.data.general.rawreport) {
                return '<div style="font-family: monospace;"> ' + params.data.general.rawreport + ' </div>';
              }
            };
          }
        }
      });
      this.isGroupColumn = true;
      this.columnDefs = reportColDefs;
      setTimeout(() => {
        this.autoSizeAll(false);
      }, 800);
    } else {
      const reportColDefs = JSON.parse(this.columnData[2].reportColumnDefs);
      reportColDefs.forEach(element => {
        if (element.children) {
          element.children.forEach(elements => {
            if (elements.headerName === 'Received Time' || elements.headerName === 'GPS Time' || elements.headerName === 'RTC Time') {
              elements.cellRenderer = (data2) => {
                if (data2.value) {
                  const newDate = new Date(data2.value);
                  const newDate2 = new Date('2000-01-01 00:00:00.00000');
                  if (newDate < newDate2) {
                    return 'N/A';
                  } else {
                    return cutomeDatepipe.transform(data2.value, 'report');
                  }
                } else {
                  return 'N/A';
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

            if (elements.headerName === 'Seq #') {
              elements.cellRenderer = (params) => {
                if (params.data && params.data.report_header && (params.data.report_header.sequence || params.data.report_header.sequence === 0)) {
                  if (this.counts[params.data.report_header.sequence] > 1) {
                    return '<div style="background:#EDD3D3">' + params.data.report_header.sequence + '</div>';
                  } else {
                    return params.data.report_header.sequence;
                  }
                } else {
                  return '<div style="background:#FEF5BD">  </div>';
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
            if (elements.headerName === 'Raw Report') {
              elements.cellRenderer = (params) => {
                if (params.data && params.data.general && params.data.general.rawreport) {
                  return '<div style="font-family: monospace;"> ' + params.data.general.rawreport + ' </div>';
                }
              };
            }
          });
        }
      });
      this.isGroupColumn = false;
      this.columnDefs = reportColDefs;
      setTimeout(() => {
        this.autoSizeAll(false);
      }, 800);
    }
    // },
    //   (error: any) => {
    //     console.log(error);
    //   });
  }


  ngOnInit() {
    this.loading = true;
    this.atCommandService.isAddATCommand = true;
    this.getAssetDetails(sessionStorage.getItem('device_id'));
    this.userCompanyType = sessionStorage.getItem('type');
    this.isSuperAdmin = sessionStorage.getItem('IS_ROLE_SUPERADMIN');
    this.userCompanyType = sessionStorage.getItem('type');
    this.isSuperAdmin = sessionStorage.getItem('IS_ROLE_SUPERADMIN');
    if (this.isSuperAdmin === 'true') {
      this.router.navigate(['gateway-profile']);
    } else {
      this.router.navigate(['gateway-profile']);
    }
    this.isRoleCustomerManager = sessionStorage.getItem('IS_ROLE_CUSTOMER_MANAGER');
    this.sortFlag = false;

    this.initializeForm();
    this.getDeviceSecuritySettings(sessionStorage.getItem('device_id'));
   // this.getAssetDeviceDetail();
    this.getGroupDetails();
    this.isGroupColumn = false;
    this.refresh();
    // this.getAllEvent();
    if (!this.roleCallCenterUser) {
      this.getAllOrganisation();
    }
  }
  // getAssetDeviceDetail() {
  //   const id = sessionStorage.getItem('device_id');
  //   this.gatewayListService.getAssetDeviceDetail(id).subscribe(
  //     data => {
  //       this.isSubmitted = true;
  //       this.parsedReport = data ? data.body : '';
  //     },
  //     error => {
  //       console.log(error);
  //     }
  //   );
  // }

  getGroupDetails() {
    const id = sessionStorage.getItem('device_id');
    this.gatewayListService.getGroupDetailsBydeviceId(id).subscribe(
      data => {
        this.deviceGroupDetail = data.body;
      },
      error => {
        console.log(error);
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
  getAllEvent() {
    this.gatewayListService.getEventList().subscribe(
      data => {

        this.eventList = data.body;
      }
    );
  }



  getAssetDetails(id: any) {
    this.loading = true;
    this.gatewayListService.getAssetStatus(id).subscribe(data => {
      if (data != null) {
        this.loading = false;
        this.dprovision = data.body;
        sessionStorage.setItem('DEVICE_ASSET_DETAILS', JSON.stringify(this.dprovision[0]));
        this.deviceprovision = this.dprovision[0];
        const cutomeDatepipe = new CustomDatePipe();
        if (this.deviceprovision && this.deviceprovision.last_maintenance_report_date) {
          this.deviceprovision.last_maintenance_report_date = cutomeDatepipe.transform(this.deviceprovision.last_maintenance_report_date, 'report');
        }
        this.last_report_date = cutomeDatepipe.transform(this.deviceprovision.last_report_date, 'report');
        if (this.deviceprovision.redis_data) {
          this.redisData = JSON.parse(JSON.parse(JSON.stringify(this.deviceprovision.redis_data)));
        }

        if (this.redisData && this.redisData.waterfall && this.redisData.waterfall.waterfallInfo && this.redisData.waterfall.waterfallInfo.length > 0) {
          for (const iterator of this.redisData.waterfall.waterfallInfo) {
            switch (iterator.configId) {
              case 1:
                this.config1 = iterator;
                break;
              case 2:
                this.config2 = iterator;
                break;
              case 3:
                this.config3 = iterator;
                break;
              case 4:
                this.config4 = iterator;
                break;
              case 5:
                this.config5 = iterator;
                break;
              default:
                break;
            }

          }
        }

        let eps = 1e-9;
        if (this.redisData != undefined && this.redisData.voltage != undefined && this.redisData.voltage.mainPower != undefined) {
          this.primaryPower = this.redisData.voltage.mainPower;
          if (this.primaryPower > 0) {
            this.primaryPower = Number((this.primaryPower + eps).toFixed(2));
          }

        } else if (this.redisData != undefined && this.redisData.general_mask_fields != undefined && this.redisData.general_mask_fields.externalPower_Volts != undefined) {
          this.primaryPower = this.redisData.general_mask_fields.externalPower_Volts;
          if (this.primaryPower > 0) {
            this.primaryPower = Number((this.primaryPower + eps).toFixed(2));
          }
        }

        if (this.redisData != undefined && this.redisData.voltage != undefined && this.redisData.voltage.auxPower != undefined && this.redisData.voltage.auxPower != 1000000000) {
          this.secondaryPower = this.redisData.voltage.auxPower;
          if (this.secondaryPower > 0) {
            this.secondaryPower = Number((this.secondaryPower + eps).toFixed(2));
          }
        }
        if (this.primaryPower === null || this.primaryPower === undefined || this.primaryPower === 0) {
          if (this.deviceprovision && this.deviceprovision.battery_data && this.deviceprovision.battery_data.length > 0) {
            const obj = this.deviceprovision.battery_data.find(item => item.power_source.toLowerCase() === 'primary');
            if (obj && obj.voltage) {
              if (obj.voltage.includes('v')) {
                const values = obj.voltage.split('v');
                this.primaryPower = values && values.length > 0 ? Number(values[0]) : 0;
              } else if (obj.voltage.includes('V')) {
                const values = obj.voltage.split('V');
                this.primaryPower = values && values.length > 0 ? Number(values[0]) : 0;
              } else {
                this.primaryPower = Number(obj.voltage);
              }

            }
          }
        }

        if (this.secondaryPower === null || this.secondaryPower === undefined || this.secondaryPower === 0) {
          if (this.deviceprovision && this.deviceprovision.battery_data && this.deviceprovision.battery_data.length > 0) {
            const obj = this.deviceprovision.battery_data.find(item => item.power_source.toLowerCase() === 'secondary');
            if (obj && obj.voltage) {
              if (obj.voltage.includes('v')) {
                const values = obj.voltage.split('v');
                this.secondaryPower = values && values.length > 0 ? Number(values[0]) : 0;
              } else if (obj.voltage.includes('V')) {
                const values = obj.voltage.split('V');
                this.secondaryPower = values && values.length > 0 ? Number(values[0]) : 0;
              } else {
                this.secondaryPower = obj.voltage;
              }

            }
          }
        }

        if (this.primaryPower < 10 && this.secondaryPower < 10) {
          this.flag = true;
        }
        this.latestRawReport = this.deviceprovision.last_raw_report;
        this.sensorList = this.prepareSensorList(this.deviceprovision.sensor_payload);
        this.cellularpayload = this.deviceprovision.cellular_payload;
      }
    }, error => {
      console.log(error);
    });
  }

  retrieveSensorDetails() {
    this.retrieveSensorHeading = 'Request in Progress';
    this.retrieveSensorMessage = ' Currently retrieving data from the device. It may take up to a few minutes for the sensor data to update in the screen.';
    this.retrieveSensorErrorMessage = '';
    $('#RetriveSensorModal').modal('show');
    let self = this;
    setTimeout(function () {
      self.retrieveSensorDetails1();
    }, 500);

  }

  retryRetrieveSensorDetails() {
    this.closeExpenseModalForRetrieveSensor.nativeElement.click();
    this.retrieveSensorHeading = 'Request in Progress';
    this.retrieveSensorMessage = ' Currently retrieving data from the device. It may take up to a few minutes for the sensor data to update in the screen.';
    this.retrieveSensorErrorMessage = '';
    let self = this;
    setTimeout(function () {
      $('#RetriveSensorModal').modal('show');
      setTimeout(function () {
        self.retrieveSensorDetails1();
      }, 500);
    }, 500);

  }
  errors = false;
  retryRetrive = false;
  retrieveSensorDetails1() {
    // this.loading = true;
    const payload = {
      device_id: this.deviceprovision.imei,
      device_uuid: this.deviceprovision.uuid,
      can: this.deviceprovision.can,
      at_command: 'AT+XINTPMS',
      priority: -1
    };
    this.errors = false;
    this.retryRetrive = false;
    this.gatewayListService.retrieveSensorDetails(payload).subscribe(data => {
      this.loading = false;
      this.getAssetDetails(sessionStorage.getItem('device_id'));
      if (data && data.status) {
        this.errors = true;
        let self = this;
        setTimeout(function () {
          self.closeExpenseModalForRetrieveSensor.nativeElement.click();
        }, 500);
        this.toastr.success(data.message, null, { toastComponent: CustomSuccessToastrComponent });
      } else {
        if (data && data.message === 'DataNotUpdated') {
          this.retrieveSensorHeading = 'Problem Retrieving Sensor Data';
          this.retrieveSensorMessage = 'The AT Command has not saved the data into DB for getting a response, Please retry';
          this.errors = true;
        } else if (data && data.message === 'SensorNotOnline') {
          this.retrieveSensorHeading = 'Sensor Not Online';
          this.retrieveSensorMessage = 'The device did not return sensor data. This could be because the sensor is not online.';
          this.errors = true;
        } else if (data && data.message === 'SensorOnlineButNoData') {
          this.retrieveSensorHeading = 'Sensor Online But 0 Sensor';
          this.retrieveSensorMessage = 'The device did not return sensor data. This could be due to a Sensor online but 0 sensor ';
          this.errors = true;
        } else if (data && data.message === 'NOTINSTALLED') {
          this.retrieveSensorHeading = 'Sensor Not Installed';
          this.retrieveSensorMessage = 'The device did not return sensor data. This could be because the sensor is not installed.';
          this.errors = true;
        } else if (data && data.message === 'StatusDeleted') {
          this.retrieveSensorHeading = 'Could Not Retrieve Data';
          this.retrieveSensorMessage = 'The device did not accept this request to retrieve data, and may not currently be in listening mode. ';
          this.errors = true;
          this.retryRetrive = true;
        } else {
          this.retrieveSensorHeading = 'Device Response Pending';
          this.retrieveSensorMessage = 'The device has not yet responded. You can click the Check Status button if the sensor list has not appeared after 1-2 minutes.';
          this.errors = true;
        }
        //  this.toastr.error(data.message, null, { toastComponent: CustomErrorToastrComponent });
      }
      this.loading = false;
    }, error => {
      this.loading = false;
      this.errors = true;
      this.retrieveSensorHeading = 'Problem Retrieving Sensor Data';
      this.retrieveSensorMessage = '';
      if (error.includes('already queued')) {
        this.retrieveSensorErrorMessage = 'AT Command is already in queued';
      } else if (error.includes('403') || error.includes('no body') || error.includes('Read timed out') || error.includes('Unexpected end of stream.')) {
        this.retryRetrive = true;
        this.retrieveSensorErrorMessage = 'Problem Retrieving Sensor Data Due to Connection Reset, Please try again.';
      } else {
        this.retrieveSensorErrorMessage = error;
      }
      console.log(error);
    });
  }

  refreshSensor() {
    this.errors = false;
    this.retrieveSensorHeading = 'Request in Progress';
    this.retrieveSensorMessage = ' Currently retrieving data from the device. It may take up to a few minutes for the sensor data to update in the screen.';
    this.retrieveSensorErrorMessage = '';
    $('#RetriveSensorModal').modal('show');
    let self = this;
    setTimeout(function () {
      self.refreshSensor1();
    }, 500);

  }

  refreshSensor1() {
    this.loading = true;
    const payload = {
      device_id: this.deviceprovision.imei,
      device_uuid: this.deviceprovision.uuid,
      can: this.deviceprovision.can,
      at_command: 'AT+XINTPMS',
      priority: -1
    };
    this.gatewayListService.retrieveLatestSensorDetails(payload).subscribe(data => {
      this.loading = false;
      this.getAssetDetails(sessionStorage.getItem('device_id'));
      this.errors = true;
      if (data && data.status) {
        let self = this;
        setTimeout(function () {
          self.closeExpenseModalForRetrieveSensor.nativeElement.click();
        }, 500);
        this.toastr.success(data.message, null, { toastComponent: CustomSuccessToastrComponent });
      } else if (data && data.message === 'DataNotUpdated') {
        this.retrieveSensorHeading = 'Problem Retrieving Sensor Data';
        this.retrieveSensorMessage = 'The AT Command has not saved the data into DB for getting a response, Please retry';
        this.errors = true;
      } else if (data && data.message === 'SensorNotOnline') {
        this.retrieveSensorHeading = 'Sensor Not Online';
        this.retrieveSensorMessage = 'The device did not return sensor data. This could be because the sensor is not online.';
        this.errors = true;
      } else if (data && data.message === 'SensorOnlineButNoData') {
        this.retrieveSensorHeading = 'Sensor Online But 0 Sensor';
        this.retrieveSensorMessage = 'The device did not return sensor data. This could be due to a Sensor online but 0 sensor ';
        this.errors = true;
      } else if (data && data.message === 'NOTINSTALLED') {
        this.retrieveSensorHeading = 'Sensor Not Installed';
        this.retrieveSensorMessage = 'The device did not return sensor data. This could be because the sensor is not installed.';
        this.errors = true;
      } else if (data && data.message === 'StatusDeleted') {
        this.retrieveSensorHeading = 'Could Not Retrieve Data';
        this.retrieveSensorMessage = 'The device did not accept this request to retrieve data, and may not currently be in listening mode. ';
        this.errors = true;
        this.retryRetrive = true;
      } else {
        this.retrieveSensorHeading = 'Device Response Pending';
        this.retrieveSensorMessage = 'The device has not yet responded. You can click the Check Status button if the sensor list has not appeared after 1-2 minutes.';
        this.errors = true;
      }
      this.loading = false;
    }, error => {
      this.loading = false;
      this.errors = true;
      this.retrieveSensorHeading = 'Problem Retrieving Sensor Data';
      this.retrieveSensorMessage = '';
      if (error.includes('already queued')) {
        this.retrieveSensorErrorMessage = 'AT Command is already in queued';
      } else if (error.includes('403') || error.includes('no body') || error.includes('Read timed out') || error.includes('Unexpected end of stream.')) {
        this.retryRetrive = true;
        this.retrieveSensorErrorMessage = 'Problem Retrieving Sensor Data Due to Connection Reset, Please try again.';
      } else {
        this.retrieveSensorErrorMessage = error;
      }
      console.log(error);
    });
  }
  getConfigurationDetails(id: any) {
    this.peripheralVersions = [];
    this.gatewayListService.getConfigurationDetails(id).subscribe(data => {
      if (data != null) {
        this.loading = false;
        this.configurationList = data.body;
        this.configurationDetails = this.configurationList[0];
        if (this.configurationDetails.redis_data) {
          this.redisData = JSON.parse(JSON.parse(JSON.stringify(this.configurationDetails.redis_data)));
          if (this.redisData && this.redisData.peripheral_version) {
            for (const key in this.redisData.peripheral_version) {
              if (Object.prototype.hasOwnProperty.call(this.redisData.peripheral_version, key)) {
                const value = this.redisData.peripheral_version[key];
                if (value instanceof Object) {
                  const obj = {
                    category: key,
                    type: value.type ? value.type : '',
                    sensorStatus: value.sensorStatus ? value.sensorStatus : '',
                    sensorData: value.dataElements ? value.dataElements : ''
                  };
                  if (value.dataElements) {
                    let sensorDataElement = '';
                    value.dataElements.forEach(element => {
                      sensorDataElement = (sensorDataElement + 'H/W Version: ' + (element.hardwareVersion ? element.hardwareVersion : '') + ', F/W Version:' + (element.firmwareVersion ? element.firmwareVersion : '') + '  ');
                    });
                    obj.sensorData = sensorDataElement;
                  }
                  this.peripheralVersions.push(obj);
                }
              }
            }
          }
        }
      }
    }, error => {
      console.log(error);
    });
    // this.getDeviceSecuritySettings(id);
  }

  getDeviceSecuritySettings(id: any) {
    if (id != undefined && id != null) {
      this.deviceSecurityService.login().subscribe(
        data => {
          sessionStorage.setItem('jwt_token', data.jwttoken);
          this.getSecurityKeyDetails(id);
        },
        error => { }
      );
      console.log(id);
      // API call
    }
  }
  getSecurityKeyDetails(id: any) {
    this.deviceSecurityService.getDeviceKeyDetail(id).subscribe(
      data => {
        if (data != undefined && data != null) {
          console.log(data)
          this.existingKeyDetail = data;
          if (data.keyExist === true) {
            this.isSecureKeyPresent = true;
          }
          if (data.reportSigningEnabled === true) {
            this.isRequireReportSigning.setValue(true);
          }
          if (data.responseVerificationEnabled === true) {
            this.isRequireVerfication.setValue(true);
          }
        }
      },
      error => {
        console.log(error);
      }
    );
    this.getAlertDetails(id);
  }
  getAlertDetails(id: any) {
    this.deviceSecurityService.getAlertDetail(id).subscribe(
      data => {
        if (data != undefined && data != null) {
          if (data.alert) {
            this.isAlertPresent = true;
            this.alertMessage = data.message;
          }
        }
      },
      error => {
        console.log(error)
      }
    );
  }
  onChange(event: MatRadioChange) {
    this.radioButtonEvent = event;
    $('#deviceKeyRequest').modal('show');
  }
  isNumber(str: string): boolean {
    if (typeof str !== 'string') {
      return false;
    }

    if (str.trim() === '') {
      return false;
    }

    return !Number.isNaN(Number(str));
  }
  generateSignedCommand() {
    this.changeRequestCommand();
    this.refreshEvent();
    $('#generateSignedCommandRequest').modal('show');
  }
  isDisableCommandRequest = true;
  changeRequestCommand() {
    if (this.expiration_time.value != undefined && this.expiration_time.value != null && this.expiration_time.value != ''
      && this.atcommand_request.value != undefined && this.atcommand_request.value != null && this.atcommand_request.value != '') {
      if (!this.isNumber(this.expiration_time.value)) {
        this.toastr.error('Expiration Time should be numeric.', null, { toastComponent: CustomErrorToastrComponent });
        this.isDisableCommandRequest = true;
        return;
      }
      this.isDisableCommandRequest = false;
    } else {
      this.isDisableCommandRequest = true;
    }

  }
  generateSignedRequest() {

    this.generateCommandModel = new generateSignedCommand();
    this.generateCommandModel.deviceId = sessionStorage.getItem('device_id');
    this.generateCommandModel.expireTime = this.expiration_time.value;
    this.generateCommandModel.atCommand = this.atcommand_request.value;
    this.deviceSecurityService.sendSignedCommandRequest(this.generateCommandModel).subscribe(
      data => {
        console.log(data);
        this.signedCommandResponse = data.responseMessage;
        $('#signedCommandResponse').modal('show');
      },
      error => {
        this.toastr.error('Key does not exist in database for given device id', null, { toastComponent: CustomErrorToastrComponent });
      }
    );
  }
  refreshEvent() {
    this.expiration_time = new FormControl(0, Validators.pattern('^[0-9]*$'));
    this.atcommand_request = new FormControl('AT+');
    this.isDisableCommandRequest = true;
  }
  responseUpdate() {
    this.toastr.success('Signed command generated successfully.', null, { toastComponent: CustomSuccessToastrComponent });
    this.expiration_time = new FormControl(0, Validators.pattern('^[0-9]*$'));
    this.atcommand_request = new FormControl('AT+');
  }
  onCancelRequest() {
    this.isRequireReportSigning.setValue(this.existingKeyDetail.reportSigningEnabled);
    this.isRequireVerfication.setValue(this.existingKeyDetail.responseVerificationEnabled);
  }
  changeDeviceKeyRequest() {
    var deviceKeyExist = {
    }
    if (this.radioButtonEvent.source.name === 'report_signing') {
      deviceKeyExist['reportSigningEnabled'] = this.radioButtonEvent.value
      // this.deviceKeyExist.reportSigningEnabled = this.radioButtonEvent.value;
    }
    if (this.radioButtonEvent.source.name === 'response_verfication') {
      deviceKeyExist['responseVerificationEnabled'] = this.radioButtonEvent.value
      //  this.deviceKeyExist.responseVerificationEnabled =  this.radioButtonEvent.value;
    }

    this.deviceSecurityService.updateDeviceKeyRequest(this.deviceId, deviceKeyExist)
      .subscribe(response => {
        console.log(response);
        if (response == 200) {
          this.getDeviceSecuritySettings(this.deviceId);
          this.toastr.success('Security settings updated successfully', null, { toastComponent: CustomSuccessToastrComponent });
        }
        else {
          this.toastr.error('Unable to update the settings', null, { toastComponent: CustomErrorToastrComponent });
        }
      });

  }
  initializeForm() {
    this.count = 0;
    this.rawreportform = this.formBuldier.group({
      raw_report: ['', Validators.compose([Validators.required])],
      format: ['', Validators.compose([Validators.required])],
      type: ['', Validators.compose([Validators.required])]
    });
  }
  getBuildNumber(data: string): string {
    return data.slice(data.length - 2, data.length);
  }

  prepareBetaTmpsReading(data: any, location: any, sensorDetails: any) {
    if (location === 'FLO' && data.length > 0) {
      sensorDetails.reading = data[0].primaryCurbsidePressurePSI + ' PSI/' + data[0].primaryCurbsideTemperatureF + 'F';
      sensorDetails.status = data[0].primaryCurbStatus;
    }
    if (location === 'FLI' && data.length > 0) {
      sensorDetails.reading = data[0].innerCurbisdePressurePSI + ' PSI/' + data[0].innerCurbsideTemperatureF + 'F';
      sensorDetails.status = data[0].innerCurbStatus;
    }
    if (location === 'FRI' && data.length > 0) {
      sensorDetails.reading = data[0].primaryRoadsidePressurePSI + ' PSI/' + data[0].primaryRoadsideTemperatureF + 'F';
      sensorDetails.status = data[0].innerRoadsideStatus;
    }
    if (location === 'FRO' && data.length > 0) {
      sensorDetails.reading = data[0].primaryRoadsidePressurePSI + ' PSI/' + data[0].primaryRoadsideTemperatureF + 'F';
      sensorDetails.status = data[0].primaryRoadsideStatus;
    }

    if (location === 'RLO' && data.length > 1) {
      sensorDetails.reading = data[1].primaryCurbsidePressurePSI + ' PSI/' + data[1].primaryCurbsideTemperatureF + 'F';
      sensorDetails.status = data[1].primaryCurbStatus;
    }
    if (location === 'RLI' && data.length > 1) {
      sensorDetails.reading = data[1].innerCurbisdePressurePSI + ' PSI/' + data[1].innerCurbsideTemperatureF + 'F';
      sensorDetails.status = data[1].innerCurbStatus;
    }
    if (location === 'RRI' && data.length > 1) {
      sensorDetails.reading = data[1].primaryRoadsidePressurePSI + ' PSI/' + data[1].primaryRoadsideTemperatureF + 'F';
      sensorDetails.status = data[1].innerRoadsideStatus;
    }
    if (location === 'RRO' && data.length > 1) {
      sensorDetails.reading = data[1].primaryRoadsidePressurePSI + ' PSI/' + data[1].primaryRoadsideTemperatureF + 'F';
      sensorDetails.status = data[1].primaryRoadsideStatus;
    }

    return sensorDetails;
  }
  recieverStatus: string;
  // sensorDetailList: any[] = [];
  prepareSensorList(data: any): any {
    const cutomeDatepipe = new CustomDatePipe();
    this.sensorDetailList = [];
    data.forEach(element => {
      if (element.sensor_sub_detail !== undefined && element.sensor_sub_detail != null && element.sensor_sub_detail.length > 0) {
        element.sensor_sub_detail.forEach(details => {
          let sensorDetails: any = {};
          sensorDetails.product_name = element.product_name;
          sensorDetails.product_code = element.product_code;
          if (element.product_name.toLowerCase() === 'microsp tpms sensor') {
            if (details.sensor_id) {
              const value = details.sensor_id.slice(0, 2);
              let value2 = details.sensor_id;
              if (value === '0x') {
                value2 = details.sensor_id.slice(2, 10);
              }
              sensorDetails.mac_address = value2;
            }
          } else if (element.product_name === 'Door Sensor (Wireless)' || element.product_name === 'Door Sensor') {
            sensorDetails.mac_address = element.mac_address;
          } else {
            sensorDetails.mac_address = details.sensor_id ? details.sensor_id : element.mac_address;
          }
          sensorDetails.altPositionName = '--';
          sensorDetails.position = details.position;
          const obj = this.postionArray.find(item => item.value === details.position);
          if (obj) {
            sensorDetails.positionName = obj.data;
            sensorDetails.altPositionName = obj.altName;
          } else {
            sensorDetails.positionName = details.position;
          }
          sensorDetails.type = details.type;
          sensorDetails.uuid = details.uuid;
          sensorDetails.vendor = details.vendor;
          sensorDetails.status = details.status;
          sensorDetails.at_command_uuid = details.at_command_uuid;
          sensorDetails.at_command_status = details.at_command_status;
          sensorDetails.new_sensor_id = details.new_sensor_id;
          sensorDetails.inPending = false;
          //  sensorDetails.reading = (details.sensor_pressure ? details.sensor_pressure : '') + ' / ' + (details.sensor_temperature ? details.sensor_temperature : '');
          const sensorData = this.redisData ? this.redisData[this.sensorTypeMap.get(element.product_name)] : null;
          if (sensorData) {
            sensorDetails.receivedTimeStamp = cutomeDatepipe.transform(sensorData.receivedTimeStamp, 'report');
            if (sensorData.receivedTimeStamp && details.at_command_status && details.at_command_status === 'COMPLETED') {
              const receivedTimeStampDate = new Date(sensorData.receivedTimeStamp);
              if (details.updated_on) {
                const updatedOnDate = new Date(cutomeDatepipe.transform(details.updated_on, 'latest_report'));
                const futDate = new Date(updatedOnDate.getTime() - (7 * 60 * 60 * 1000));
                if (futDate.getTime() > receivedTimeStampDate.getTime()) {
                  sensorDetails.inPending = true;
                }
              }
            }

            if (sensorData && element.product_name.toLowerCase() === 'microsp tpms sensor') {
              if (this.redisData.tpms_beta && this.redisData.tpms_beta.tpmsBetaMeasure && this.redisData.tpms_beta.tpmsBetaMeasure.length > 0) {
                this.recieverStatus = this.redisData.tpms_beta.status;
                sensorDetails = this.prepareBetaTmpsReading(this.redisData.tpms_beta.tpmsBetaMeasure, sensorDetails.positionName, sensorDetails);
              } else {
                sensorData[this.sonsorStatusMap.get(this.sensorTypeMap.get(element.product_name))].forEach((element: any) => {
                  if (sensorDetails.positionName === element.tireLocationStr) {
                    sensorDetails.reading = element.tirePressurePSI + ' PSI/' + element.tireTemperatureF + 'F';
                    return;
                  }
                });
              }
            } else {
              switch (element.product_name) {
                case 'BLE PCT Door Sensor':
                  sensorDetails.reading = sensorData.doorState;
                  sensorDetails.type = sensorData.doorType;
                  break;
                case 'Lamp Check ATIS':
                  sensorDetails.reading = sensorData.condition;
                  sensorDetails.type = sensorData.type;
                  break;
                case 'Chassis':
                  sensorDetails.reading = sensorData.cargoStateTMC;
                  break;
                case 'Alpha ABS':
                  if (sensorData.activeFaultsWithoutStoredFaults && sensorData.noActiveFaultsWithStoredFaults && sensorData.activeAndStoredFaults && sensorData.notCommunicating
                    && sensorData.notPoweredUp) {
                    sensorDetails.status = 'UnHealthy';
                  } else {
                    sensorDetails.status = 'Healthy';
                  }
                  break;
                case 'Door Sensor (Wireless)':
                  sensorDetails.status = sensorData.doorState;
                  sensorDetails.type = sensorData.doorType;
                  sensorDetails.mac_address = element.mac_address;
                  break;
                case 'Door Sensor':
                  sensorDetails.status = sensorData.doorState;
                  sensorDetails.type = sensorData.doorType;
                  sensorDetails.mac_address = element.mac_address;
                  break;
                case 'MicroSP Air Tank':
                  // sensorDetails.status = sensorData.commStatus;
                  sensorDetails.status =  details.status;;
                  break;
                case 'Lite Sentry':
                  if (sensorData.genericFault && sensorData.markerFault && sensorData.deadRightMarkerBulb && sensorData.deadStopBulb
                    && sensorData.deadLeftMarkerBulb && sensorData.deadLicenseBulb) {
                    sensorDetails.status = 'UnHealthy';
                  } else {
                    sensorDetails.status = 'Healthy';
                  }
                  break;

                default:
                  break;
              }
            }
          }
          this.sensorDetailList.push(sensorDetails);
        });
      } else {
        if (element.product_name !== 'MICROSP TPMS SENSOR') {
          this.sensorDetails(element);
        }
      }
    });
    return this.sensorDetailList;
  }

  sensorDetails(element: any) {
    const cutomeDatepipe = new CustomDatePipe();
    const sensorDetails: any = {};
    sensorDetails.product_name = element.product_name;
    sensorDetails.product_code = element.product_code;
    sensorDetails.mac_address = element.mac_address;
    sensorDetails.altPositionName = '--';
    sensorDetails.status = element.status;
    // sensorDetails.type = element.type;
    if (this.redisData) {
      const sensorData = this.redisData[this.sensorTypeMap.get(element.product_name)];
      if (sensorData) {
        sensorDetails.receivedTimeStamp = cutomeDatepipe.transform(sensorData.receivedTimeStamp, 'report');
        switch (element.product_name) {
          case 'BLE PCT Door Sensor':
            sensorDetails.status = sensorData.doorState;
            sensorDetails.type = sensorData.doorType;
            break;
          case 'Lamp Check ATIS':
            sensorDetails.reading = sensorData.condition;
            sensorDetails.type = sensorData.type;
            break;
          case 'Chassis':
            sensorDetails.reading = sensorData.cargoStateTMC;
            break;
          case 'Alpha ABS':
            if (sensorData.activeFaultsWithoutStoredFaults && sensorData.noActiveFaultsWithStoredFaults && sensorData.activeAndStoredFaults && sensorData.notCommunicating
              && sensorData.notPoweredUp) {
              sensorDetails.status = 'UnHealthy';
            } else {
              sensorDetails.status = 'Healthy';
            }
            break;
          case 'Door Sensor (Wireless)':
            sensorDetails.status = sensorData.doorState;
            sensorDetails.type = sensorData.doorType;
            break;
          case 'Door Sensor':
            sensorDetails.status = sensorData.doorState;
            sensorDetails.type = sensorData.doorType;
            break;
          case 'Lite Sentry':
            if (sensorData.genericFault && sensorData.markerFault && sensorData.deadRightMarkerBulb && sensorData.deadStopBulb
              && sensorData.deadLeftMarkerBulb && sensorData.deadLicenseBulb) {
              sensorDetails.status = 'UnHealthy';
            } else {
              sensorDetails.status = 'Healthy';
            }
            break;
          case 'MicroSP Air Tank':
            // sensorDetails.status = sensorData.doorState;
            sensorDetails.status = element.status;
            sensorDetails.type = sensorData.doorType;
            break;
          case 'MicroSP ATIS Regulator':
            sensorDetails.status = sensorData.doorState;
            sensorDetails.type = sensorData.doorType;
            break;
          default:
            sensorDetails.reading = sensorData[this.sonsorStatusMap.get(this.sensorTypeMap.get(element.product_name))];
            break;
        }
      }
    }
    if (element.product_name !== 'MicroSP TPMS Sensor') {
      this.sensorDetailList.push(sensorDetails);
    }
  }

  onSubmit(rawreportform) {
    this.submitted = true;
    this.initializeForm();
    this.getFormControlValues(rawreportform);
    // this.getParseRawReport();
  }
  getFormControlValues(rawreportform) {
    this.rawReport = rawreportform.controls.raw_report.value;
    this.format = rawreportform.controls.format.value;
    this.type = rawreportform.controls.type.value;
  }

  getParseRawReport() {
    this.gatewayListService.getParsedReport(this.rawReport, this.format, this.type).subscribe(
      data => {
        this.isSubmitted = true;
        this.parsedReport = data.body;
      },
      error => {
        console.log(error);
      }
    );
  }

  getSelectedRowData() {
    return this.gridApi.getSelectedNodes().map(node => node.data);
  }

  reloadComponent() {
    this.router.navigateByUrl('/', { skipLocationChange: true }).then(() =>
      this.router.navigate([this.router.url]));
  }

  deleteGateway(): void {
    if (this.gatewayListService.gateway != null) {
      const gatewayList = [];
      gatewayList.push(this.gatewayListService.gateway.imei);
      this.gatewayListService.deleteGatewayByIds(gatewayList).subscribe(data => {
        if (data.body.length === 0) {
          this.toastr.success('The selected gateway(s) have been deleted successfully.', null, { toastComponent: CustomSuccessToastrComponent });
        }
      }, error => {
        console.log(error);
      });
      this.reloadComponent();
    }
  }

  getSelctedRow(action: any) {
    this.gatewayList = [];
    this.selectedIdsToDelete = [];
    this.gatewayList = this.getSelectedRowData();
    this.gatewayListService.gatewayList = [];

    if (action === 'delete') {
      for (let i = 0; i < this.gatewayList.length; i++) {
        this.gatewayList[i].is_selected = true;
        if (this.gatewayList[i].status !== 'Pending') {
          this.toastr.error('The following records are part of an installation, and so they cannot be deleted.', 'Problem with Deletion', { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
          return;
        } else {
          this.gatewayListService.gatewayList.push(this.gatewayList[i]);
        }
      }
    } else if (action === 'update') {
      for (let i = 0; i < this.gatewayList.length; i++) {
        this.gatewayList[i].is_selected = true;
        if (this.gatewayList[i].gateway_type !== 'Gateway') {
          this.toastr.error('The selected device(s) do not support sensors.', 'Device Does Not Support Sensors ', { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
          return;
        } else {
          if (this.gatewayList[i].status !== 'Pending') {
            this.toastr.error('The selected device(s) are part of an in-progress or active installation, and cannot be modified.', 'Device Configuration Can’t Be Modified',
              { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
            return;
          } else {
            this.gatewayListService.gatewayList.push(this.gatewayList[i]);
          }
        }
      }
      this.router.navigate(['update-gateways']);
    }

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

  deleteAll(event) {
    this.gridApi.forEachNode(function (node) {
      if (event.checked) {
        node.setSelected(true);
      } else {
        node.setSelected(false);
      }
    });

    if (event.checked) {
      this.gatewayList = this.getSelectedRowData();
      for (let i = 0; i < this.gatewayList.length; i++) {
        this.gatewayList[i].is_selected = true;
      }
    } else if (!event.checked) {
      this.selectedIdsToDelete = [];
      this.gatewayList = this.getSelectedRowData();
      for (let i = 0; i < this.gatewayList.length; i++) {
        this.gatewayList[i].is_selected = false;
      }
    }
  }

  clearFilter() {
    this.column = 'general_mask_fields.received_time_stamp';
    this.order = 'desc';
    this.paramList.api.setFilterModel(null);
    this.paramList.api.onFilterChanged();
    sessionStorage.setItem('custom_gateway_list_grid_filter_state', null);
  }

  openExportPopup() {
    $('#exportCSVModal').modal('show');
  }

  openGatewayPowerLow(sensor: any) {
    if (this.primaryPower < 10 && this.secondaryPower < 10) {
      $('#gatewayPowerLow').modal('show');
    } else if (this.recieverStatus == 'OFFLINE' || sensor.status == 'OFFLINE' || sensor.status == 'Offline' || this.recieverStatus == 'Offline') {
      $('#recieverOfline').modal('show');
    }
  }

  openSensorEditPopup(sensorDetails: any) {
    this.atCommandSentfailed = false;
    this.isInvalid = false;
    this.hideButton = false;
    this.sensorId = sensorDetails.mac_address;
    this.sensorDetailUuid = sensorDetails.uuid;
    this.location = sensorDetails.position;
    const obj = this.postionArray.find(item => item.value === sensorDetails.position);
    if (obj) {
      this.showLocation = obj.name;
    } else {
      this.showLocation = this.location;
    }
    if (sensorDetails.position === '0x49') {
      this.sensorName = 'MicroSP Air Tank';
    } else if (sensorDetails.position === '0x4A') {
      this.sensorName = 'MicroSP ATIS Regulator';
    } else {
      this.sensorName = 'TPMS Sensor';
    }

    $('#SensorEditModal').modal('show');
  }

  editSensorRetry() {
    this.atCommandSentfailed = false;
  }
  erroHeading = 'Update Not Successful';
  saveSensorId() {
    this.retrySensorButton = false;
    const loc = this.location.split('0x');
    if (loc.length > 1) {
      this.location = loc[1];
    }
    const locValue = this.location.slice(0, 2);
    let locValue2 = this.location;
    if (locValue === '0x') {
      locValue2 = this.location.slice(2, 10);
    }
    const value = this.sensorId.slice(0, 2);
    let value2 = this.sensorId;
    if (value === '0x') {
      value2 = this.sensorId.slice(2, 10);
    }
    const payload = {
      uuid: this.sensorDetailUuid,
      macAddress: value2,
      deviceId: this.deviceId,
      position: locValue2
    };
    if (this.sensorId && this.sensorDetailUuid) {
      this.hideButton = true;
      this.erroHeading = 'Request in Progress';
      this.atCommandErrorMessage = 'Currently updating the sensor details with the device. It may take up to a few minutes for the new sensor’s data to update in the screen.';
      this.atCommandSentfailed = true;
      this.gatewayListService.updateSensorMacAdderess(payload).subscribe(data => {
        this.hideButton = false;
        this.getAssetDetails(sessionStorage.getItem('device_id'));
        console.log(data);
        if (data.status) {
          this.hideButton = false;
          this.atCommandSentfailed = false;
          // It may take up to 3 minutes for the new sensor to report status.
          this.toastr.success('The new ID was sent to the gateway successfully.', null, { toastComponent: CustomSuccessToastrComponent });
          let self = this;
          setTimeout(function () {
            self.closeAddExpenseModal.nativeElement.click();
          }, 100);
        } else {
          if (data.message === 'STATUSPENDING') {
            this.atCommandSentfailed = true;
            this.erroHeading = 'Device Response Pending';
            this.atCommandErrorMessage = ' The device has not yet responded. You can click the Refresh  button for the sensor if the updated reading has not appeared after 1-2 minutes.';
            // this.toastr.error(data.message, null, { toastComponent: CustomErrorToastrComponent });
          } else if (data.message === 'STATUSERROR') {
            this.retrySensorButton = true;
            this.atCommandSentfailed = true;
            this.erroHeading = 'Problem Retrieving Sensor Data';
            this.atCommandErrorMessage = 'The device did not return sensor data. This could be due to a problem with the hardware. ';
          } else if (data.message === 'STATUSDELETED') {
            this.retrySensorButton = true;
            this.atCommandSentfailed = true;
            this.erroHeading = 'Could Not Retrieve Data';
            this.atCommandErrorMessage = 'The device did not accept this request to retrieve data, and may not currently be in listening mode.';
          } else {
            this.erroHeading = 'Problem Retrieving Sensor Data';
            this.atCommandErrorMessage = 'The device did not return sensor data. This could be due to a problem with the hardware. ';
          }
        }

      }, error => {
        console.log(error);
        this.getAssetDetails(sessionStorage.getItem('device_id'));
        this.atCommandSentfailed = true;
        this.hideButton = false;
        this.erroHeading = 'Problem Retrieving Sensor Data';
        if (error.includes('already queued')) {
          this.atCommandErrorMessage = 'AT Command is already in queued';
        } else {
          this.atCommandErrorMessage = 'The update command could not be sent to the device.';
        }
        // this.toastr.error('The ID could not be sent to the gateway successfully. Please try again', null, { toastComponent: CustomErrorToastrComponent });
      });
    } else {
      this.toastr.error('Please select valid sensor that have a macAddress', null, { toastComponent: CustomErrorToastrComponent });
    }
  }

  refreshUpdatedSesnor(sensorData: any) {
    this.sensorId = sensorData.mac_address;
    this.sensorDetailUuid = sensorData.uuid;
    this.location = sensorData.position;
    const obj = this.postionArray.find(item => item.value === sensorData.position);
    if (obj) {
      this.showLocation = obj.name;
    } else {
      this.showLocation = this.location;
    }
    if (sensorData.position === '0x49') {
      this.sensorName = 'MicroSP Air Tank';
    } else if (sensorData.position === '0x4A') {
      this.sensorName = 'MicroSP ATIS Regulator';
    } else {
      this.sensorName = 'TPMS Sensor';
    }
    $('#SensorEditModal').modal('show');
    let self = this;
    setTimeout(function () {
      self.refreshUpdatedSesnor2(sensorData);
    }, 500);
  }
  refreshUpdatedSesnor2(sensorData: any) {
    this.retrySensorButton = false;
    this.hideButton = true;
    this.erroHeading = 'Request in Progress';
    this.atCommandErrorMessage = 'Currently updating the sensor details with the device. It may take up to a few minutes for the new sensor’s data to update in the screen.';
    const payload = {
      sensor_uuid: sensorData.uuid,
      device_id: this.deviceId,
      new_sensor_id: sensorData.new_sensor_id,
      at_command_uuid: sensorData.at_command_uuid
    };
    this.gatewayListService.refreshUpdatedSensorMacAdderess(payload).subscribe(data => {
      this.hideButton = false;
      this.getAssetDetails(sessionStorage.getItem('device_id'));
      // this.toastr.success(data.message, null, { toastComponent: CustomSuccessToastrComponent });
      if (data.status) {
        this.hideButton = false;
        let self = this;
        setTimeout(function () {
          self.closeAddExpenseModal.nativeElement.click();
        }, 200);
        this.toastr.success('Sensor Id Updated', null, { toastComponent: CustomSuccessToastrComponent });
      } else {
        if (data.message === 'STATUSPENDING') {
          this.atCommandSentfailed = true;
          this.erroHeading = 'Device Response Pending';
          this.atCommandErrorMessage = ' The device has not yet responded. You can click the Refresh  button for the sensor if the updated reading has not appeared after 1-2 minutes.';
          // this.toastr.error(data.message, null, { toastComponent: CustomErrorToastrComponent });
        } else if (data.message === 'STATUSERROR') {
          this.retrySensorButton = true;
          this.atCommandSentfailed = true;
          this.erroHeading = 'Problem Retrieving Sensor Data';
          this.atCommandErrorMessage = 'The device did not return sensor data. This could be due to a problem with the hardware. ';
        } else if (data.message === 'STATUSCOMPLETEDWITHERROR') {
          this.retrySensorButton = true;
          this.atCommandSentfailed = true;
          this.erroHeading = 'Problem Retrieving Sensor Data';
          this.atCommandErrorMessage = 'The device did not return sensor data. This could be due to a problem with the hardware. ';
        } else if (data.message === 'STATUSDELETED') {
          this.retrySensorButton = true;
          this.atCommandSentfailed = true;
          this.erroHeading = 'Could Not Retrieve Data';
          this.atCommandErrorMessage = 'The device did not accept this request to retrieve data, and may not currently be in listening mode.';
        } else {
          this.erroHeading = 'Problem Retrieving Sensor Data';
          this.atCommandErrorMessage = 'The device did not return sensor data. This could be due to a problem with the hardware. ';
        }
      }
    }, error => {
      console.log(error);
    });
  }


  validate(event: any) {
    const regEx = '^[a-fA-F0-9]+$';
    const isHex = event.target.value.match(regEx);
    if (!isHex) {
      this.isInvalid = true;
    } else {
      this.isInvalid = false;
    }

  }


  exportDeviceList() {
    // this.loading = true;

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

    const id = sessionStorage.getItem('device_id');

    // this.loading = true;
    if (this.range > 10000) {
      this.gatewayListService.exportData(0, this.column, this.order, this.range, exportObj, id).subscribe(data => {
        // this.loading = false;
        this.toastr.success('Data Exported File sent to mail', null, { toastComponent: CustomSuccessToastrComponent });
      }, error => {
        // this.loading = false;
      });
    } else {
      this.loading = true;
      this.gatewayListService.exportDataInLocal(0, this.column, this.order, this.range, exportObj, id).subscribe(data => {
        this.loading = false;
        this.toastr.success('Data Exported ', null, { toastComponent: CustomSuccessToastrComponent });
      }, error => {
        this.loading = false;
      });
    }

  }
  onGridReady(params: any) {
    this.paramList = params;
    this.gridApi = params.api;
    this.gridApi.setRowData([]);
    // console.log("=============== ONGRID READY===============");
    const columState = JSON.parse(sessionStorage.getItem('device_report_grid_column_state'));
    const columStateHeader = JSON.parse(sessionStorage.getItem('device_report_grid_Header_column_state'));
    const filterSavedState = sessionStorage.getItem('device_report_grid_filter_state');
    if (filterSavedState) {
      this.gridApi.setFilterModel(JSON.parse(filterSavedState));
    }
    this.getReportColumnDefs();
    this.gridColumnApi = params.columnApi;
    if (columState) {
      this.gridColumnApi.applyColumnState({ state: columState, applyOrder: true, });
    }
    if (columStateHeader) {
     // this.gridColumnApi.setColumnGroupState(columStateHeader)
     this.gridColumnApi.setColumnGroupState(columStateHeader);

      //  this.gridColumnApi.applyColumnGroupState({ state: columStateHeader, applyOrder: true, });
    }
    const datasource = {
      getRows: (params: IGetRowsParams) => {
        // this.counts = {};
        this.isFirstTime = false;
        this.rowCount = null;
        this.loading = true;
        this.isGroupColumn = false;
        const elem = document.getElementsByClassName('ag-body-viewport') as HTMLCollection;
        this.sortForAgGrid(params.sortModel);
        // sessionStorage.setItem('device_report_grid_filter_state', JSON.stringify(params.filterModel));
        this.filterForAgGrid(params.filterModel);
        const id = sessionStorage.getItem('device_id');
        if (this.isFirst) {
          if (elem && elem.length > 0) {
            const ele = elem[0] as HTMLElement;
            ele.style.overflow = 'hidden';
          }
          this.loading = true;
          this.counts = {};
          this.gatewayListService.getDeviceReportsElastic(this.currentPage, this.column, this.order, this.pageLimit, this.filterValuesJsonObj, id)
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
            )
            .subscribe(data => {
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
              this.elasticRowData = [];
              this.rowData = [];
              if (data != null && data.totalHits > 0) {
                for (const iterator of data.tlvData) {
                  this.elasticRowData.push(iterator);
                  if (iterator.report_header) {
                    if (iterator.report_header.sequence || iterator.report_header.sequence === 0) {
                      this.counts[iterator.report_header.sequence] = (this.counts[iterator.report_header.sequence] || 0) + 1;
                    }
                    if (iterator.report_header.event_id) {
                      this.eventList.forEach(event => {
                        if (event.eventId === iterator.report_header.event_id) {
                          iterator.report_header.event_name = event.eventType;
                        }
                      });
                    }
                  }
                  if (iterator.general_mask_fields) {
                    if (iterator.general_mask_fields.gpsstatus_index === 0) {
                      this.receivedTimeStamp = iterator.general_mask_fields.received_time_stamp;
                      iterator.general_mask_fields.gpsstatus_index = 'Unlocked';
                    } else {
                      iterator.general_mask_fields.gpsstatus_index = 'Locked';
                    }
                  }
                  if (!iterator.voltage) {
                    iterator.general_mask_fields.internal_power_volts = iterator.general_mask_fields.internal_power_volts;
                    iterator.general_mask_fields.external_power_volts = iterator.general_mask_fields.external_power_volts;
                  } else {
                    const eps = 1e-9;
                    iterator.general_mask_fields.internal_power_volts = (iterator.voltage.battery_power + eps).toFixed(2);
                    iterator.general_mask_fields.external_power_volts = (iterator.voltage.main_power + eps).toFixed(2);
                  }
                  if (iterator.network_field) {
                    iterator.general_mask_fields.rssi = iterator.network_field.rssi;
                  } else {
                    iterator.general_mask_fields.rssi = iterator.general_mask_fields.rssi;
                  }
                  if (iterator.door_sensor) {
                    iterator.door_sensor.status = iterator.door_sensor.status + ',  ' + iterator.door_sensor.last_valid_check + '; RSSI ' + iterator.door_sensor.ble_rssi + ';';
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
                  if (iterator.reefer) {
                    iterator.reefer.concatValues = iterator.reefer.status + '; ' + iterator.reefer.state + ' ;' + iterator.reefer.fan_state;
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
                  if (iterator.general) {
                    if (iterator.general.company_id) {
                      this.organisationList.forEach(organisation => {
                        if (organisation.accountNumber === iterator.general.company_id) {
                          iterator.general.company_id = organisation.organisationName;
                        }
                      });
                    }
                  }
                  // if(iterator.field_vehicle_vin)
                  // {
                  //   iterator.general_mask_fields.vin == iterator.field_vehicle_vin.vin;
                  // }
                  // else
                  // {
                  //   iterator.general_mask_fields.vin == 'NA';
                  // }
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
                  if (iterator.voltage) {
                    const eps = 1e-9;
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
                  if (iterator.abs_odometer) {
                    iterator.abs_odometer.odometer = (iterator.abs_odometer.odometer * 0.0621371).toFixed(2);
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
                  if (iterator.ble_door_sensor) {
                    iterator.ble_door_sensor.comm_status = iterator.ble_door_sensor.comm_status + '; sStatus : ' + iterator.ble_door_sensor.sensor_status + ': ' + iterator.ble_door_sensor.door_state + '; ' + iterator.ble_door_sensor.door_type
                      + '; ' + iterator.ble_door_sensor.door_sequence + '; ' + iterator.ble_door_sensor.battery + '; ' + iterator.ble_door_sensor.temperature_c;
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
                          '; Size :' + iterator.peripheral_version.door_sensor.data_size + '; + Version: ' + iterator.peripheral_version.door_sensor.version + ';';
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
                    // }   // Old get Info
                    if (iterator.chassis.condition === "Healthy") {
                      iterator.chassis = iterator.chassis.condition + ', Codes: ' + iterator.chassis.code1 + ', ' + iterator.chassis.code2 + ', ' + iterator.chassis.cargo_state + ', ' + iterator.chassis.dist_mm + 'mm';
                    } else {
                      iterator.chassis = iterator.chassis.condition + ', Codes: ' + iterator.chassis.code1 + ', ' + iterator.chassis.code2;
                    }
                  }
                  if (iterator.field_vehicle_ecu) {
                    iterator.field_vehicle_ecu.fuel_level_percentage = iterator.field_vehicle_ecu.fuel_level_percentage.toFixed(2);
                  }
                  let psiAirSupply = '';
                  if (iterator.psi_air_supply != null && iterator.psi_air_supply != undefined && iterator.psi_air_supply.psi_air_supply_measure != null && iterator.psi_air_supply.psi_air_supply_measure != undefined) {
                    for (let i = 0; i < iterator.psi_air_supply.psi_air_supply_measure.length; i++) {
                      const j = i + 1;
                      psiAirSupply = 'ASMS ' + j + ':  T Status_ ' + j + ': ' + iterator.psi_air_supply.psi_air_supply_measure[i].tank_status + '; T Bat_' + j + ': ' + iterator.psi_air_supply.psi_air_supply_measure[i].tank_battery + '; S Status_' + j + ': ' + iterator.psi_air_supply.psi_air_supply_measure[i].supply_status + '; S Bat_' + j + ': ' + iterator.psi_air_supply.psi_air_supply_measure[i].supply_battery + '; Tank Pressure_' + j + ': ' + iterator.psi_air_supply.psi_air_supply_measure[i].tank_pressure + '; Supply Pressure_' + j + ': ' + iterator.psi_air_supply.psi_air_supply_measure[i].supply_pressure;
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
                        psiWheelEnd = iterator.psi_wheel_end.comm_status + '; L Status_1:' + iterator.psi_wheel_end.psi_wheel_end_measure[0].left_status + '; L Bat_1:' + iterator.psi_wheel_end.psi_wheel_end_measure[0].left_battery + ';  R Status_1:' + iterator.psi_wheel_end.psi_wheel_end_measure[0].right_status + ' R Bat_1:' + iterator.psi_wheel_end.psi_wheel_end_measure[0].right_battery + '; L Temp_1: ' + iterator.psi_wheel_end.psi_wheel_end_measure[0].left_temperature +
                          '; R Temp_1: ' + iterator.psi_wheel_end.psi_wheel_end_measure[0].right_temperature;
                        + '; L Status_2:' + iterator.psi_wheel_end.psi_wheel_end_measure[1].left_status + '; L Bat_2:' + iterator.psi_wheel_end.psi_wheel_end_measure[1].left_battery + ';  R Status_2:' + iterator.psi_wheel_end.psi_wheel_end_measure[1].right_status + ' R Bat_2:' + iterator.psi_wheel_end.psi_wheel_end_measure[1].right_battery + '; L Temp_2: ' +
                          iterator.psi_wheel_end.psi_wheel_end_measure[1].left_temperature + '; R Temp_2: ' + iterator.psi_wheel_end.psi_wheel_end_measure[1].right_temperature;
                        iterator.psi_wheel_end.psi_wheel_end_measure = psiWheelEnd;
                      } else {
                        iterator.psi_wheel_end.psi_wheel_end_measure = iterator.psi_wheel_end.comm_status;
                      }

                    } else {
                      iterator.psi_wheel_end.psi_wheel_end_measure = iterator.psi_wheel_end.comm_status;
                    }
                  }
                  // let tpmsAlpha = '';
                  // if (iterator.tpms_alpha != null && iterator.tpms_alpha != undefined && iterator.tpms_alpha.tpms_measures != null && iterator.tpms_alpha.tpms_measures != undefined) {
                  //   if (iterator.tpms_alpha.num_sensors > 0) {
                  //     iterator.tpms_alpha.tpms_measures.forEach((element: any) => {
                  //       tpmsAlpha = tpmsAlpha + element.tire_location_str + ' : ';
                  //       if (element.tire_pressure_psi <= 0) {
                  //         tpmsAlpha = tpmsAlpha + '-- , ';
                  //       }
                  //       else {
                  //         tpmsAlpha = tpmsAlpha + element.tire_pressure_psi + 'psi, ';
                  //       }
                  //       if (element.tire_temperature_c <= -50) {
                  //         tpmsAlpha = tpmsAlpha + ' -- ;';
                  //       } else {
                  //         tpmsAlpha = tpmsAlpha + element.tire_temperature_f + 'F; '
                  //       }
                  //     });
                  //   }
                  //   if (iterator.tpms_alpha.num_sensors > 0) {
                  //     iterator.tpms_alpha.tpms_measures = iterator.tpms_alpha.status + '; ' + tpmsAlpha;
                  //   } else {
                  //     iterator.tpms_alpha.tpms_measures = iterator.tpms_alpha.status;
                  //   }
                  // }
                  let abs_info = '';
                  if (iterator.beta_abs_id) {
                    if (iterator.beta_abs_id.manufacturer_info == null) {
                      abs_info = iterator.beta_abs_id.status + ', Manufacturer: NA ,  Serial Number: '
                        + iterator.beta_abs_id.serial_num + ', Firmware version: ' + iterator.beta_abs_id.abs_fw_version + ', Model: ' + iterator.beta_abs_id.model;
                    } else {
                      abs_info = iterator.beta_abs_id.status + ', Manufacturer: ' +
                        iterator.beta_abs_id.manufacturer_info + ', Serial Number: ' + iterator.beta_abs_id.serial_num + ', Firmware version: ' + iterator.beta_abs_id.abs_fw_version + ', Model: ' + iterator.beta_abs_id.model;
                    }
                    iterator.beta_abs_id.status = abs_info;
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
                      lite_entry_debug = 'Scenario_ID ' + j + iterator.lite_entry_debug.lite_entry_debug_measure[i].scenario_id + ':  Minimum_Voltage_ ' + j + ': ' +
                        iterator.lite_entry_debug.lite_entry_debug_measure[i].minimum_voltage +
                        ' mV; Maximum_Voltage__' + j + ': ' + iterator.lite_entry_debug.lite_entry_debug_measure[i].maximum_voltage + ' mV; Fault_Voltage_' + j +
                        iterator.lite_entry_debug.lite_entry_debug_measure[i].fault_voltage + ' mV; Minimum_Current_' + j +
                        ': ' + iterator.lite_entry_debug.lite_entry_debug_measure[i].minimum_current + ' mA; Maximum_Current_' +
                        j + ': ' + iterator.lite_entry_debug.lite_entry_debug_measure[i].maximum_current +
                        ' mA; Fault_Current' + j + ': ' + iterator.lite_entry_debug.lite_entry_debug_measure[i].fault_current +
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
                    if (iterator.cargo_camera_sensor.state === 'EMPTY') {
                      iterator.cargo_camera_sensor.prediction_value = 'NA';
                    }
                    else {
                      iterator.cargo_camera_sensor.prediction_value = iterator.cargo_camera_sensor.prediction_value;
                    }
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
              if (this.currentPage <= 50 && this.rowData.length > 0) {
                this.receivedTimeStamp = this.rowData[0].general_mask_fields.received_time_stamp;
              }
              // params.successCallback(this.rowData, data.totalHits.value);
              params.successCallback(this.rowData, data.totalHits);
              this.currentPage += 50;
              this.autoSizeAll(false);
              setTimeout(() => {
                this.autoSizeAll(false);
              }, 4000);

            },
              error => {
                this.loading = false;
                console.log(error);
              });
        } else {
          this.loading = false;
          this.autoSizeAll(false);
        }
      }
    };
    this.gridApi.setDatasource(datasource);
  }

  public GetCurrentUserInformation(): Promise<any> {
    const id = sessionStorage.getItem('device_id');
    return this.gatewayListService.getDeviceReportsElastic(this.currentPage, this.column, this.order, this.pageLimit, this.filterValuesJsonObj, id).toPromise();
  }

  getDataFromServer(params: IGetRowsParams): Promise<void> {
    return new Promise<void>(resolve => {
      if (this.totalNumberOfRecords < this.currentPage && !this.isGetData) {
        this.GetCurrentUserInformation().then((data) => {
          this.loading = false;
          this.totalNumberOfRecords = data.totalHits.value;
          this.elasticRowData = [];
          this.rowData = [];
          if (data.hits != null && data.hits.length > 0) {
            for (const iterator of data.hits) {
              this.elasticRowData.push(iterator.sourceAsMap);
            }
          }
          this.rowData = this.elasticRowData;
          params.successCallback(this.rowData, data.totalHits.value);
          this.autoSizeAll(false);
          this.currentPage += 50;
          this.isGetData = false;
        }).catch((error) => {
          console.log('Promise rejected with ' + JSON.stringify(error));
        });
      } else {
      }
      resolve();
    });
  }

  autoSizeAll(skipHeader: boolean) {
    // console.log("================= autoSizeAll ===================");
    const columState = JSON.parse(sessionStorage.getItem('device_report_grid_column_state'));
    const columStateHeader = JSON.parse(sessionStorage.getItem('device_report_grid_Header_column_state'));
    const filterSavedState = sessionStorage.getItem('device_report_grid_filter_state');

    // if (!columState && !filterSavedState) {
    const allColumnIds: string[] = [];
    this.gridColumnApi.getAllColumns().forEach((column) => {
      allColumnIds.push(column.getId());
    });
    this.gridColumnApi.autoSizeColumns(allColumnIds, skipHeader);
    // }
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
    this.currentPage = 0;
    this.isFirst = true;
    // sessionStorage.setItem('gateway_list_grid_filter_state', JSON.stringify(params.api.getFilterModel()));
    sessionStorage.setItem('device_report_grid_filter_state', JSON.stringify(params.api.getFilterModel()));
  }

  onColumnVisible(params: any) {
    this.saveGridStateInSession(this.gridColumnApi.getColumnState());
  }

  onExpandedChanged(param: any) {
    this.saveGridStateInSession(this.gridColumnApi.getColumnState());
  }

  onColumnPinned(params: any) {
    this.saveGridStateInSession(this.gridColumnApi.getColumnState());
  }


  onFirstDataRendered(params: any) {
    this.isFirstTimeLoad = true;
  }

  saveGridStateInSession(columnState: any) {
    // console.log("============= COLOUM CHANGE ===============");
    // console.log(columnState);
    sessionStorage.setItem('device_report_grid_column_state', JSON.stringify(columnState));
  }
  onColumnGroupOpened(params: any){
    console.log("my column group open event called")
    //const columnGroupState = this.gridColumnApi.getColumnGroupState();
    sessionStorage.setItem('device_report_grid_Header_column_state', JSON.stringify(this.gridColumnApi.getColumnGroupState()));
  }

  getGateway(currentPage: any) {
    this.gatewayListService.getGatewayDetails(currentPage).subscribe(
      data => {
        this.gatewayList = data.body.content;
        this.totalpage = data.total_pages;
        this.showingpage = data.currentPage + 1;
        this.allItems = data.totalKey;
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


  countRecords(data: any) {
    if (data.body.content == null || data.body.content.length === 0) {
      this.totalRecords = 0;
      this.currentStartRecord = 0;
      this.currentEndRecord = 0;
    } else {
      this.totalRecords = data.total_key;
      this.currentStartRecord = (this.pageLimit * (data.current_page)) + 1;
      if (this.pageLimit === data.body.content.length) {
        this.currentEndRecord = this.pageLimit * (data.current_page + 1);
      } else {
        this.currentEndRecord = this.currentStartRecord + data.body.content.length - 1;
      }
    }
  }

  setPage(page: number, body: any, totalPages: any, pageItems: any) {
    this.pager = this.pagerService.getPager(this.allItems, page, totalPages, pageItems);
    this.pagedItems = body;
  }

  uploadgateways() {
    this.router.navigate(['upload-devices']);
  }

  editDevice() {
    this.router.navigate(['edit-device'], { queryParams: { id: this.deviceId } });
  }

  filterForAgGrid(filterModel: any) {
    this.gridApi.paginationCurrentPage = 0;
    this.filterValuesJsonObj = {};
    this.filterEValues = new Map();
    if (filterModel !== undefined && filterModel != null) {
      const elasticModelFilters = JSON.parse(JSON.stringify(filterModel));
      filterModel = elasticModelFilters;
      const Okeys = Object.keys(filterModel);
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
        } else if (filterModel[Okeys[i]].type === 'not equal') {
          this.elasticFilter.operator = 'notequal';
          this.elasticFilter.value = filterModel[Okeys[i]].filter;
          this.filterEValues.set('filterValues ' + i, this.elasticFilter);
        } else if (filterModel[Okeys[i]].type === 'inRange') {
          this.elasticFilter.operator = 'gt';
          this.elasticFilter.value = filterModel[Okeys[i]].dateFrom;
          this.filterEValues.set('filterValues ' + i + 'a', this.elasticFilter);
          this.elasticFilter = new Filter();
          this.elasticFilter.key = Okeys[i];
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
        this.elasticFilter = new Filter();
      }
      const jsonObject: any = {};
      this.filterEValues.forEach((filter, key) => {
        jsonObject[key] = filter;
      });
      this.filterValuesJsonObj = jsonObject;
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
      } else if (sortModel[0].colId === 'general.device_type') {
        this.column = 'general.device_type.keyword';
        this.order = sortModel[0].sort;
      } else {

        this.column = sortModel[0].colId;
        this.order = sortModel[0].sort;
      }
    }
    // this.gridApi.paginationCurrentPage = 0;
    // if (sortModel[0] === undefined || sortModel[0] == null || sortModel[0].colId === undefined || sortModel[0].colId == null) {
    //   return;
    // }
    // this.sortFlag = true;
    // this.column = sortModel[0].colId;
    // this.order = sortModel[0].sort;
  }

  onCellClicked(params: any) {
    if (params.column.colId === 'imei') {
      this.gatewayListService.deviceId = params.data.imei;
      this.atCommandService.deviceId = this.gatewayListService.deviceId;
      if (this.gatewayListService.deviceId != null) {
        this.router.navigate(['gateway-profile']);
      }
    }
    if (params.column.colId === 'cargo_camera_sensor.uri') {
      if (params.data.cargo_camera_sensor && params.data.cargo_camera_sensor.uri && params.data.cargo_camera_sensor.uri.length > 38) {
        if (params.data.cargo_camera_sensor.uri.includes('http')) {
          // window.open(params.data.cargo_camera_sensor.uri, '_blank');
          this.imageUrl = params.data.cargo_camera_sensor.uri;
          $('#expandImageModal').modal('show');
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
        this.column = 'imei';
        this.order = 'asc';
        params.api.setFilterModel(null);
        params.api.onFilterChanged();
        sessionStorage.setItem('custom_gateway_list_grid_filter_state', null);
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


  getAllcompanies() {
    this.companiesService.getAllCompany(['EndCustomer'], true).subscribe(
      data => {
        this.companies = [];
        if (data !== undefined && data != null
          && data.body !== undefined && data.body != null
          && data.body.content !== undefined && data.body.content !== null
          && data.body.content.length > 0) {
          for (const iterator of data.body.content) {
            this.companies.push(iterator.customer);
          }
        }
      },
      error => {
        console.log(error);
      }

    );
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

        // const columState = JSON.parse(sessionStorage.getItem('device_report_grid_column_state'));
        this.gatewayListService.getColumnDefs().subscribe(data => {
          this.columnData = data;
          const reportColDefs = JSON.parse(data[2].reportColumnDefs);
          reportColDefs.forEach(element => {
            if (element.children) {
              element.children.forEach(elements => {
                if (elements.headerName === 'Received Time' || elements.headerName === 'GPS Time' || elements.headerName === 'RTC Time') {
                  elements.cellRenderer = (data2) => {
                    if (data2.value) {
                      const newDate = new Date(data2.value);
                      const newDate2 = new Date('2000-01-01 00:00:00.00000');
                      if (newDate < newDate2) {
                        return 'N/A';
                      } else {
                        return cutomeDatepipe.transform(data2.value, 'report');
                      }
                    } else {
                      return 'N/A';
                    }
                  };
                }
                if (elements.headerName === 'Event') {
                  elements.filterParams = filterParamsForEvent;
                }
                if (elements.headerName === 'Thumbnail') {
                  elements.cellRenderer = (params) => {
                    // if (params.data && params.data.cargo_camera_sensor && params.data.cargo_camera_sensor.uri && params.data.cargo_camera_sensor.uri.length > 38
                    //   && params.data.general.company_id !== 'Amazon' && params.data.general.company_id !== 'amazon') {
                    //   return '<img width="100%" height="100px" src="' + params.data.cargo_camera_sensor.uri + '">';
                    // } else if (params.data && params.data.cargo_camera_sensor && params.data.cargo_camera_sensor.uri && params.data.general.company_id !== 'Amazon' && params.data.general.company_id !== 'amazon') {
                    //   return params.data.cargo_camera_sensor.uri;
                    // } else {
                    //   return '';
                    // }
                    if (params.data && params.data.cargo_camera_sensor && params.data.cargo_camera_sensor.uri && params.data.cargo_camera_sensor.uri.length > 0) {
                      if (params.data.cargo_camera_sensor.uri.includes('http')) {
                        return '<img width="100%" height="100px" src="' + params.data.cargo_camera_sensor.uri + '">';
                      } else if (params.data.general.company_id === 'Amazon' || params.data.general.company_id === 'amazon') {
                        return '<img width="100%" height="100px" src="http://images.phillips-connect.net/' + params.data.cargo_camera_sensor.uri + '">';
                      }
                    }
                  };
                }
                if (elements.headerName === 'Seq #') {
                  elements.cellRenderer = (params) => {
                    if (params.data && params.data.report_header && (params.data.report_header.sequence || params.data.report_header.sequence === 0)) {
                      if (this.counts[params.data.report_header.sequence] > 1) {
                        return '<div style="background:#EDD3D3">' + params.data.report_header.sequence + '</div>';
                      } else {
                        return params.data.report_header.sequence;
                      }
                    } else {
                      return '<div style="background:#FEF5BD">  </div>';
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
                if (elements.headerName === 'Raw Report') {
                  elements.cellRenderer = (params) => {
                    if (params.data && params.data.general && params.data.general.rawreport) {
                      return '<div style="font-family: monospace;"> ' + params.data.general.rawreport + ' </div>';
                    }
                  };
                }
              });
            }
          });
          this.columnDefs = reportColDefs;
          const columState = JSON.parse(sessionStorage.getItem('device_report_grid_column_state'));
          const columStateHeader = JSON.parse(sessionStorage.getItem('device_report_grid_Header_column_state'));
          const filterSavedState = sessionStorage.getItem('device_report_grid_filter_state');
          if (columState) {
            setTimeout(() => {
              this.gridColumnApi.applyColumnState({ state: columState, applyOrder: true, });
            }, 800);
          }
          if (columStateHeader) {
            setTimeout(() => {
              this.gridColumnApi.setColumnGroupState(columStateHeader);
            }, 800);
          }
          if (filterSavedState) {
            setTimeout(() => {
              this.gridApi.setFilterModel(JSON.parse(filterSavedState));
            }, 1000);
          }
        },
          (error: any) => {
            console.log(error);
          });
      }
    );
  }

  refresh() {
    // setInterval(() => {
    //   if (this.reportTab) {
    //     this.gridApi.onFilterChanged();
    //   }
    // }, 15 * 1000);
  }

  onCompanySelect(event: any) {
    this.company = event.value;
    sessionStorage.setItem('selectedCompany', JSON.stringify(this.company));
    this.companyname = event.value;
    this.days = 7;
    this.onGridReady(this.paramList);
  }

  onDateRangeSelect(event: any) {
    this.days = event.value;
    sessionStorage.setItem('selectedDays', JSON.stringify(this.days));
    this.onGridReady(this.paramList);
  }

  yourFn(event: any) {
    this.reportTab = false;
    switch (event.tab.textLabel) {
      case 'AT Commands':
        this.atCommandTab = true;
        this.atCommandService.isAddATCommand = false;
        setTimeout(() => {
          this.child.autoSizeAll(false);
        }, 3000);
        break;
      case 'Reports & Location':
        this.reportTab = true;
        this.atCommandTab = false;
        this.atCommandService.isAddATCommand = true;
        this.currentPage = 0;
        if (!this.isFirstTime) {
          this.onGridReady(this.paramList);
        }
        setTimeout(() => {
          this.autoSizeAll(false);
        }, 3000);
        break;
      case 'Configuration':
        this.atCommandTab = false;
        this.getConfigurationDetails(sessionStorage.getItem('device_id'));
        break;
      case 'Campaigns':
        this.atCommandTab = false;
        this.campaignTab = true;
        break;
      case 'Data Forwarding':
        this.atCommandTab = false;
        this.deviceForwardingTab = true;
        break;
      case 'History':
        this.historyTab = true;
        break;
      case 'Assets Status':
        this.atCommandTab = false;
        this.getAssetDetails(sessionStorage.getItem('device_id'));
        break;
      default:
        break;
    }
  }

  refreshAsset() {
    this.getAssetDetails(sessionStorage.getItem('device_id'));
  }

  headerHeightSetter(event: any) {
    this.gridApi.setHeaderHeight(headerHeightGetter());
    this.gridApi.resetRowHeights();
  }

  showLatestRawReport() {
    $('#latestReportModel').modal('show');
  }

  copyToClipBoard(inputElement: any) {
    inputElement.select();
    document.execCommand('copy');
    inputElement.setSelectionRange(0, 0);
  }

  @ViewChild("deviceForwardingReportTab") deviceDataForwardingComponent: DeviceDataForwardingComponent;

  confirmDeleteForwardingRule() {
    this.deviceDataForwardingComponent.confirmDeleteForwardingRule();
  }

  cancelDeleteForwardingRule() {
    this.deviceDataForwardingComponent.cancelDeleteForwardingRule();
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