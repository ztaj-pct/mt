import { Component, OnInit, ViewChild, ElementRef, ChangeDetectorRef } from '@angular/core';
import { Device } from '../models/device.model'
import { FormBuilder, FormGroup, Validators, FormControl } from '@angular/forms';
import { CampaignService } from '../campaign/campaign.component.service';
import { Router, NavigationEnd } from "@angular/router";
import { ToastrService } from 'ngx-toastr';
import { AssetsService } from '../assets.service';
import { Campaign } from '../models/campaign.model';
import { GridOptions, IGetRowsParams, Module, RowNode } from '@ag-grid-community/core';
import { CompaniesService } from '../companies.service';

import { RichSelectModule } from '@ag-grid-enterprise/rich-select';
import { MenuModule } from '@ag-grid-enterprise/menu';
import { ColumnsToolPanelModule } from '@ag-grid-enterprise/column-tool-panel';
import '@ag-grid-community/core/dist/styles/ag-grid.css';
import '@ag-grid-community/core/dist/styles/ag-theme-alpine.css';
import { ClientSideRowModelModule } from '@ag-grid-community/client-side-row-model';
import { MultiFilterModule } from '@ag-grid-enterprise/multi-filter';
import { SetFilterModule } from '@ag-grid-enterprise/set-filter';
import * as XLSX from 'xlsx';
import { HighchartsService } from './highcharts.service';
import * as Highcharts from 'highcharts';
import { CampaignHyperlinkRenderer } from './campaign-hyperlink.component';
import { CustomSuccessToastrComponent } from '../custom-success-toastr/custom-success-toastr.component';
import { CustomErrorToastrComponent } from '../custom-error-toastr/custom-error-toastr.component';
import { Observable, interval, Subscription } from 'rxjs';
import { CustomBatchErrorToastrComponent } from '../custom-batch-error-toastr/custom-batch-error-toastr.component';
import { DirtyComponent } from '../util/dirty-component';
import { MatDialog, MatDialogRef } from '@angular/material';
import { DeviceHistoryDetailsDialogComponent } from './device-history-details-dialog.component';
import { PaginationConstants } from '../util/paginationConstants';
import * as moment from "moment";
import { any } from 'underscore';
import { map, startWith } from 'rxjs/operators';
import { BtnCellRendererForCampaign } from '../campaigns-list/btn-cell-renderer.component';
import { CustomDateComponent } from '../ui-elements/custom-date-component.component';
import { CustomCampaignsHeader } from '../ui-elements/custom-campaigns-header.component';
import { CustomTooltip } from '../ui-elements/custom-tooltip.component';
import { GatewayListService } from '../gateway-list.service';
import { Filter } from '../models/Filter.model';
declare var $: any;

const HEADER_ROW = 3;
const DATA_START_ROW = 1;
const NUMBER_OF_COLUMNS = 4;
const SESSION_ID = "0993b670-084a-4bdf-bbe7-af5895e19939";
const API_KEY = "1bde5f76-05ee-434a-b7e8-da05a9f71a3a";
const PRODUCT_ID = 10048;

@Component({
	selector: 'add-campaign',
	templateUrl: './add-campaign.component.html',
	styleUrls: ['./add-campaign.component.css']
})
export class AddCampaignComponent implements OnInit, DirtyComponent {
	@ViewChild('charts') public chartEl: ElementRef;
	@ViewChild('stackbarcharts') public stackbarChartEl: ElementRef;
	addCampaign: FormGroup;
	filteredOptions: Observable<string[]>;
	campaigntype: String;
	excludedImeis = [];
	customername: String;
	fileToUpload: File = null;
	type: String = '';
	oneDay: number = 24 * 60 * 60 * 1000;
	threeDay: number = 72 * 60 * 60 * 1000;
	currentTime: any = Date.now();
	currentDate: Date = new Date();
	frameworkComponents;
	data: Array<any> = [];
	updateFlag = false;
	isUploadList = false;
	isRestrictedScope = false
	definedListClick = true;
	rowClassRules;
	submitted = false;
	deleteCampaignID = false;
	popupMessage;
	popupTitle;
	addCampaignValue;
	CampaignList = [];
	packageList = [];
	packageListName = [];
	packageListForDisplay = [];
	rowParam: any;
	campaignObj;
	deviceCampaignHistoryList;
	campaignInstalledDevice;
	campaignObjCopy;
	groupList = ["Group A", "Group B"];
	types = ["UNRESTRICTED", "RESTRICTED", "DEFINED_LIST"];
	public gridApi;
	selectedFileName;
	public gridColumnApi;
	title = "Create a Campaign";
	loading = false;
	public gridApiSecond;
	public gridColumnApiSecond;
	mySubscription;
	paramList;
	companies = [];
	companies2 = [];
	companiesName = [];
	imei_list = [];
	deviceList = [];
	selectedTab = new FormControl(0);
	commonTitle = "";
	commonMessage = "";
	commonModelOption = "";
	actionName = "";
	stateOfCustomerOrUpdate;
	selectedStep = null;
	selectedGateway = null;
	selectedDataForBulkDelete = null;
	selectedDataForBulkExclude = null;
	radioStatus = true;
	updateBaseline = false;
	exclude_gateways_types = false;
	isReplace = true;
	imeis_to_be_removed = [];
	device_status_for_campaign;
	isEmergencyStopFlag = false;
	myOptions = {
		chart: {
			type: 'bar'
		},
		title: {
			text: 'Breakdown by Steps'
		},
		xAxis: {
			categories: ['Step 1', 'Step 2', 'Step 3']
		},
		yAxis: {
			min: 0,
			title: {
				text: ''
			}
		},
		legend: {
			reversed: true
		},
		plotOptions: {
			series: {
				stacking: 'normal'
			}
		},
		series: [{
			name: 'On Hold',
			data: [],
			color: '#EF4923'
		}, {
			name: 'Completed',
			data: [],
			color: '#518BCA'
		}, {
			name: 'In Progress',
			data: [],
			color: '#A2CD48'
		}, {
			name: 'Not Started',
			data: [],
			color: '#EFD31A'
		}]
	};

	campaign_summary = {
		"total_gateways": 0,
		"not_started": 0,
		"in_progress": 0,
		"completed": 0,
		"on_hold": 0,
		"not_eligible": 0,
		"off_path": 0,
		"on_path": 0
	}

	data_view_for_steps = [];

	pieChartOptions = {
		chart: {
			plotBackgroundColor: null,
			plotBorderWidth: null,
			plotShadow: false,
			type: 'pie'
		},
		title: {
			text: 'Overall Progress'
		},
		tooltip: {
			pointFormat: '{point.name}: <b>{point.y}</b>'
		},
		plotOptions: {
			pie: {
				allowPointSelect: true,
				cursor: 'pointer',
				dataLabels: {
					enabled: true,
					//format: '<b>{point.name}</b>: {point.percentage:.1f} %',
					format: '{point.y}',
					style: {
						//color: (Highcharts.theme && Highcharts.theme.contrastTextColor) || 'black'
						color: 'gray'
					}
				},
				showInLegend: true
			}
		},
		series: [{
			name: 'Brands',
			colorByPoint: true,
			type: undefined,
			data: [{
				name: 'Not Started',
				y: 0,
				color: '#EFD31A'
			}, {
				name: 'In Progress',
				y: 0,
				color: '#A2CD48'
			}, {
				name: 'Completed',
				y: 0,
				color: '#518BCA'
			}, {
				name: 'On Hold',
				y: 0,
				color: '#EF4923'
			}, {
				name: 'Off Path',
				y: 0,
				color: '#BA4A00'
			}, {
				name: 'On Path',
				y: 0,
				color: '#bf80ff'
			}]
		}]
	}
	pageSizeOptions: PaginationConstants = new PaginationConstants();
	pageSizeObjArr = this.pageSizeOptions.pageSizeObjArr;
	pageSizeOptionsArr: number[];
	selected: number;
	pageLimit = 25;
	public modules: Module[] = [ClientSideRowModelModule, RichSelectModule,
		MenuModule,
		ColumnsToolPanelModule, MultiFilterModule,
		SetFilterModule];

	public modulesSecond: Module[] = [
		ClientSideRowModelModule,
		MenuModule,
		ColumnsToolPanelModule,
		MultiFilterModule,
		SetFilterModule,
	];
	public columnDefs;
	public defaultColDef;
	public columnDefsSecond;
	public defaultColDefSecond;
	public rowData: [];
	public rowDataSecond: any;

	public campaignStatus;

	sequenceData = [];
	selectedCustomerId = null;

	originalFormValue: any;

	isDirty = false;
	tempFlag = false;
	changeDetection = false;
	isCustomerSelected = false;

	userCompanyType;
	isSuperAdmin;

	selectedDeviceIdForResetStatus = [];
	selectedDataForResetStatus = [];
	// ### added new fields 
	public infiniteInitialRowCount;
	public cacheOverflowSize;
	public maxConcurrentDatasourceRequests;
	isPaginationStateRecovered = false;
	public components;
	frameworkComponentsSecond;
	public gridOptions: Partial<GridOptions>;
	total_number_of_records = 0;
	isFirstTimeLoad = false;
	//parameters for sorting
	sortByOrganisationName = false;
	sortByDeviceId = false;
	filterValuesJsonObj: any = {};
	filterValues = new Map();

	column = "uuid";
	order = "asc";

	private dialogRef: MatDialogRef<DeviceHistoryDetailsDialogComponent>
	constructor(public _formBuldiers: FormBuilder, public router: Router, public dialog: MatDialog, public campaignService: CampaignService, public companiesService: CompaniesService,
		public toastr: ToastrService, private hcs: HighchartsService, private changeDetector: ChangeDetectorRef, public companyService: CompaniesService, private readonly gatewayListService: GatewayListService) {

		this.cacheOverflowSize = 2;
		this.maxConcurrentDatasourceRequests = 2;
		this.infiniteInitialRowCount = 2;
		this.components = {
			customClearFloatingFilter: this.getClearFilterFloatingComponent(),
		};
		var containsFilterParams = {
			filterOptions: [
				'contains',
			],
			suppressAndOrCondition: true
		};
		const filterParamsCustomers = {
			values: params => params.success(this.getFilterValuesData())
		};

		this.gridOptions = {
			cacheBlockSize: this.pageLimit,
			paginationPageSize: this.pageLimit,
			rowModelType: 'infinite',
		}
		this.columnDefs = [
			{
				headerName: 'Step #', field: 'step_order_number', rowDrag: true, editable: false,
				minWidth: 200,
				tooltipField: 'tooltip',
				cellRenderer: params => {
					if (params != undefined && params.value != undefined && params.value == "Baseline") {
						return `Baseline  <span class="tooltips"><i class="fa fa-info-circle tool"
            data-tip="The Package You set as a baseline will limit the campaign to only those gateways in the selected list that currently have this configuration."
            tabindex="1"></i>
        </span>`;
					} else if (params != undefined && params.value != undefined && params.value == "End State") {
						return `End State  <span class="tooltips"><i class="fa fa-info-circle tool"
            data-tip="The end state defines the expected configuration of devices after this campaign has been completed."
            tabindex="2"></i>
        </span>`;
					} else {
						return params.value;
					}

				}
			},
			{
				headerName: 'Package Name',
				field: 'package.package_name',
				minWidth: 180,
				// cellEditor: 'agRichSelectCellEditor',
				// cellEditorParams: cellCellEditorParams,
				filter: 'agTextColumnFilter',
				filterParams: containsFilterParams,
				onCellClicked: params => {
					console.log(params);
					this.rowParam = null;
					$('#clickModel').modal('show');
					this.rowParam = params;
				},
				cellRenderer: params => {
					if (params.data.package != null && params.data.package != undefined && params.data.package.package_name != null && params.data.package.package_name != "") {
						return params.data.package.package_name;
					} else {
						return 'Click to Choose';
					}
				}
			}, {
				headerName: 'Command',
				field: 'at_command',
				minWidth: 220,
				filter: 'agTextColumnFilter',
				filterParams: containsFilterParams,
				cellRenderer: params => {
					if (params.data.at_command != null && params.data.at_command != "") {
						return params.data.at_command;
					} else if (params.data.step_order_number == "End State") {
						return '';
					} else {
						return 'Click to Add Command';
					}
				},
				editable: (params) => {
					return params.data.step_order_number !== "End State";
				}
			}, {
				headerName: 'Created By',
				field: 'package.created_by',
				minWidth: 150,
				editable: false,
				hide: true,
				filter: 'agTextColumnFilter',
				filterParams: containsFilterParams
			},
			{
				headerName: 'Created',
				field: 'package.created_at',
				minWidth: 150,
				editable: false,
				hide: true,
				filter: 'agTextColumnFilter',
				filterParams: containsFilterParams
			},
			{
				headerName: 'BIN', field: 'package.bin_version', editable: false, filter: 'agTextColumnFilter',
				filterParams: containsFilterParams,
				width: 150,
				cellRenderer: params => {
					if (params.data.package != undefined && params.data.package != null
						&& params.data.package.bin_version != undefined && params.data.package.bin_version != null) {
						if (this.checkValueIsEqual(params, 'package.bin_version')) {
							return '<b>' + params.data.package.bin_version + '</b>';
						} else {
							return params.data.package.bin_version;
						}

					} else {
						return '';
					}
				}
			},
			{
				headerName: 'App', field: 'package.app_version', editable: false, filter: 'agTextColumnFilter',
				filterParams: containsFilterParams,
				width: 150,
				cellRenderer: params => {
					if (params.data.package != undefined && params.data.package != null
						&& params.data.package.app_version != undefined && params.data.package.app_version != null) {
						if (this.checkValueIsEqual(params, 'package.app_version')) {
							return '<b>' + params.data.package.app_version + '</b>';
						} else {
							return params.data.package.app_version;
						}

					} else {
						return '';
					}
				}
			},
			{
				headerName: 'MCU', field: 'package.mcu_version', editable: false, filter: 'agTextColumnFilter',
				filterParams: containsFilterParams,
				width: 150,
				cellRenderer: params => {
					if (params.data.package != undefined && params.data.package != null
						&& params.data.package.mcu_version != undefined && params.data.package.mcu_version != null) {
						if (this.checkValueIsEqual(params, 'package.mcu_version')) {
							return '<b>' + params.data.package.mcu_version + '</b>';
						} else {
							return params.data.package.mcu_version;
						}

					} else {
						return '';
					}
				}
			},
			{
				headerName: 'BLE', field: 'package.ble_version', editable: false, filter: 'agTextColumnFilter',
				filterParams: containsFilterParams,
				width: 150,
				cellRenderer: params => {
					if (params.data.package != undefined && params.data.package != null
						&& params.data.package.ble_version != undefined && params.data.package.ble_version != null) {
						if (this.checkValueIsEqual(params, 'package.ble_version')) {
							return '<b>' + params.data.package.ble_version + '</b>';
						} else {
							return params.data.package.ble_version;
						}

					} else {
						return '';
					}
				}
			},
			{
				headerName: 'Config1', field: 'package.config1', editable: false, filter: 'agTextColumnFilter', filterParams: containsFilterParams,
				width: 180,
				cellRenderer: params => {
					if (params.data.package != undefined && params.data.package != null
						&& params.data.package.config1 != undefined && params.data.package.config1 != null) {
						if (this.checkValueIsEqual(params, 'package.config1')) {
							return '<b>' + params.data.package.config1 + '</b>';
						} else {
							return params.data.package.config1;
						}

					} else {
						return '';
					}
				}
			},
			{
				headerName: 'Config1 CRC', field: 'package.config1_crc', editable: false, filter: 'agTextColumnFilter', filterParams: containsFilterParams,
				width: 180,
				cellRenderer: params => {
					if (params.data.package != undefined && params.data.package != null
						&& params.data.package.config1_crc != undefined && params.data.package.config1_crc != null) {
						if (this.checkValueIsEqual(params, 'package.config1_crc')) {
							return '<b>' + params.data.package.config1_crc + '</b>';
						} else {
							return params.data.package.config1_crc;
						}

					} else {
						return '';
					}
				}
			},
			{
				headerName: 'Config2', field: 'package.config2', editable: false, filter: 'agTextColumnFilter', filterParams: containsFilterParams,
				width: 180,
				cellRenderer: params => {
					if (params.data.package != undefined && params.data.package != null
						&& params.data.package.config2 != undefined && params.data.package.config2 != null) {
						if (this.checkValueIsEqual(params, 'package.config2')) {
							return '<b>' + params.data.package.config2 + '</b>';
						} else {
							return params.data.package.config2;
						}

					} else {
						return '';
					}
				}
			},
			{
				headerName: 'Config2 CRC', field: 'package.config2_crc', editable: false, filter: 'agTextColumnFilter', filterParams: containsFilterParams,
				width: 180,
				cellRenderer: params => {
					if (params.data.package != undefined && params.data.package != null
						&& params.data.package.config2_crc != undefined && params.data.package.config2_crc != null) {
						if (this.checkValueIsEqual(params, 'package.config2_crc')) {
							return '<b>' + params.data.package.config2_crc + '</b>';
						} else {
							return params.data.package.config2_crc;
						}

					} else {
						return '';
					}
				}
			},
			{
				headerName: 'Config3', field: 'package.config3', editable: false, filter: 'agTextColumnFilter', filterParams: containsFilterParams,
				width: 180,
				cellRenderer: params => {
					if (params.data.package != undefined && params.data.package != null
						&& params.data.package.config3 != undefined && params.data.package.config3 != null) {
						if (this.checkValueIsEqual(params, 'package.config3')) {
							return '<b>' + params.data.package.config3 + '</b>';
						} else {
							return params.data.package.config3;
						}

					} else {
						return '';
					}
				}
			},
			{
				headerName: 'Config3 CRC', field: 'package.config3_crc', editable: false, filter: 'agTextColumnFilter', filterParams: containsFilterParams,
				width: 180,
				cellRenderer: params => {
					if (params.data.package != undefined && params.data.package != null
						&& params.data.package.config3_crc != undefined && params.data.package.config3_crc != null) {
						if (this.checkValueIsEqual(params, 'package.config3_crc')) {
							return '<b>' + params.data.package.config3_crc + '</b>';
						} else {
							return params.data.package.config3_crc;
						}

					} else {
						return '';
					}
				}
			},
			{
				headerName: 'Config4', field: 'package.config4', editable: false, filter: 'agTextColumnFilter', filterParams: containsFilterParams,
				width: 180,
				cellRenderer: params => {
					if (params.data.package != undefined && params.data.package != null
						&& params.data.package.config4 != undefined && params.data.package.config4 != null) {
						if (this.checkValueIsEqual(params, 'package.config4')) {
							return '<b>' + params.data.package.config4 + '</b>';
						} else {
							return params.data.package.config4;
						}

					} else {
						return '';
					}
				}
			},
			{
				headerName: 'Config4 CRC', field: 'package.config4_crc', editable: false, filter: 'agTextColumnFilter', filterParams: containsFilterParams,
				width: 180,
				cellRenderer: params => {
					if (params.data.package != undefined && params.data.package != null
						&& params.data.package.config4_crc != undefined && params.data.package.config4_crc != null) {
						if (this.checkValueIsEqual(params, 'package.config4_crc')) {
							return '<b>' + params.data.package.config4_crc + '</b>';
						} else {
							return params.data.package.config4_crc;
						}

					} else {
						return '';
					}
				}
			},
			{
				headerName: 'Device Type', field: 'package.device_type', sortable: true, filter: 'agTextColumnFilter', filterParams: containsFilterParams, editable: false,
				width: 180,
				cellRenderer: params => {
					if (params.data.package != undefined && params.data.package != null
						&& params.data.package.device_type != undefined && params.data.package.device_type != null) {
						if (this.checkValueIsEqual(params, 'package.device_type')) {
							return '<b>' + params.data.package.device_type + '</b>';
						} else {
							return params.data.package.device_type;
						}

					} else {
						return '';
					}
				}
			},
			{
				headerName: 'LiteSentry HW', field: 'package.lite_sentry_hardware', sortable: true, filter: 'agTextColumnFilter', filterParams: containsFilterParams, editable: false,
				width: 180,
				cellRenderer: params => {
					if (params.data.package != undefined && params.data.package != null
						&& params.data.package.lite_sentry_hardware != undefined && params.data.package.lite_sentry_hardware != null) {
						if (this.checkValueIsEqual(params, 'package.lite_sentry_hardware')) {
							return '<b>' + params.data.package.lite_sentry_hardware + '</b>';
						} else {
							return params.data.package.lite_sentry_hardware;
						}

					} else {
						return '';
					}
				}
			},
			{
				headerName: 'LiteSentry App', field: 'package.lite_sentry_app', sortable: true, filter: 'agTextColumnFilter', filterParams: containsFilterParams, editable: false,
				width: 180,
				cellRenderer: params => {
					if (params.data.package != undefined && params.data.package != null
						&& params.data.package.lite_sentry_app != undefined && params.data.package.lite_sentry_app != null) {
						if (this.checkValueIsEqual(params, 'package.lite_sentry_app')) {
							return '<b>' + params.data.package.lite_sentry_app + '</b>';
						} else {
							return params.data.package.lite_sentry_app;
						}

					} else {
						return '';
					}
				}
			},
			{
				headerName: 'LiteSentry Boot', field: 'package.lite_sentry_boot', sortable: true, filter: 'agTextColumnFilter', filterParams: containsFilterParams, editable: false,
				width: 180,
				cellRenderer: params => {
					if (params.data.package != undefined && params.data.package != null
						&& params.data.package.lite_sentry_boot != undefined && params.data.package.lite_sentry_boot != null) {
						if (this.checkValueIsEqual(params, 'package.lite_sentry_boot')) {
							return '<b>' + params.data.package.lite_sentry_boot + '</b>';
						} else {
							return params.data.package.lite_sentry_boot;
						}

					} else {
						return '';
					}
				}
			},
			{
				headerName: 'MicroSP MCU', field: 'package.microsp_mcu', sortable: true, filter: 'agTextColumnFilter', filterParams: containsFilterParams, editable: false,
				width: 180,
				cellRenderer: params => {
					if (params.data.package != undefined && params.data.package != null
						&& params.data.package.microsp_mcu != undefined && params.data.package.microsp_mcu != null) {
						if (this.checkValueIsEqual(params, 'package.microsp_mcu')) {
							return '<b>' + params.data.package.microsp_mcu + '</b>';
						} else {
							return params.data.package.microsp_mcu;
						}

					} else {
						return '';
					}
				}
			},
			{
				headerName: 'MicroSP App', field: 'package.microsp_app', sortable: true, filter: 'agTextColumnFilter', filterParams: containsFilterParams, editable: false,
				width: 180,
				cellRenderer: params => {
					if (params.data.package != undefined && params.data.package != null
						&& params.data.package.microsp_app != undefined && params.data.package.microsp_app != null) {
						if (this.checkValueIsEqual(params, 'package.microsp_app')) {
							return '<b>' + params.data.package.microsp_app + '</b>';
						} else {
							return params.data.package.microsp_app;
						}

					} else {
						return '';
					}
				}
			},

			{
				headerName: 'Cargo (Max) HW', field: 'package.cargo_maxbotix_hardware', sortable: true, filter: 'agTextColumnFilter', filterParams: containsFilterParams, editable: false,
				width: 180,
				cellRenderer: params => {
					if (params.data.package != undefined && params.data.package != null
						&& params.data.package.cargo_maxbotix_hardware != undefined && params.data.package.cargo_maxbotix_hardware != null) {
						if (this.checkValueIsEqual(params, 'package.cargo_maxbotix_hardware')) {
							return '<b>' + params.data.package.cargo_maxbotix_hardware + '</b>';
						} else {
							return params.data.package.cargo_maxbotix_hardware;
						}

					} else {
						return '';
					}
				}
			},
			{
				headerName: 'Cargo (Max) FW', field: 'package.cargo_maxbotix_firmware', sortable: true, filter: 'agTextColumnFilter', filterParams: containsFilterParams, editable: false,
				width: 180,
				cellRenderer: params => {
					if (params.data.package != undefined && params.data.package != null
						&& params.data.package.cargo_maxbotix_firmware != undefined && params.data.package.cargo_maxbotix_firmware != null) {
						if (this.checkValueIsEqual(params, 'package.cargo_maxbotix_firmware')) {
							return '<b>' + params.data.package.cargo_maxbotix_firmware + '</b>';
						} else {
							return params.data.package.cargo_maxbotix_firmware;
						}

					} else {
						return '';
					}
				}
			},
			{
				headerName: 'Cargo (RIOT) HW', field: 'package.cargo_riot_hardware', sortable: true, filter: 'agTextColumnFilter', filterParams: containsFilterParams, editable: false,
				width: 180,
				cellRenderer: params => {
					if (params.data.package != undefined && params.data.package != null
						&& params.data.package.cargo_riot_hardware != undefined && params.data.package.cargo_riot_hardware != null) {
						if (this.checkValueIsEqual(params, 'package.cargo_riot_hardware')) {
							return '<b>' + params.data.package.cargo_riot_hardware + '</b>';
						} else {
							return params.data.package.cargo_riot_hardware;
						}

					} else {
						return '';
					}
				}
			},
			{
				headerName: 'Cargo (RIOT) FW', field: 'package.cargo_riot_firmware', sortable: true, filter: 'agTextColumnFilter', filterParams: containsFilterParams, editable: false,
				width: 180,
				cellRenderer: params => {
					if (params.data.package != undefined && params.data.package != null
						&& params.data.package.cargo_riot_firmware != undefined && params.data.package.cargo_riot_firmware != null) {
						if (this.checkValueIsEqual(params, 'package.cargo_riot_firmware')) {
							return '<b>' + params.data.package.cargo_riot_firmware + '</b>';
						} else {
							return params.data.package.cargo_riot_firmware;
						}

					} else {
						return '';
					}
				}
			},

			//{ headerName: 'Config5', field: 'package.config5', editable: false, filter: 'agTextColumnFilter', filterParams: containsFilterParams, width: 180 },
			{
				headerName: '', sortable: false,
				width: 50,
				pinned: 'right',
				filter: false,
				editable: false,
				cellRenderer: params => {
					if (params.data.step_order_number != "End State" && params.data.step_order_number != "Baseline") {
						return `<i class="fa fa-times" aria-hidden="true"></i>`;
					}

				}
			},
		];
		this.defaultColDef = {
			minWidth: 100,
			width: 90,
			sortable: true,
			filter: true,
			editable: true,
			resizable: true
		};

		// =================================== CONFIGURATION FOR SECOND GRID ==========================
		this.frameworkComponents = {
			btnCellRenderer: BtnCellRendererForCampaign,
			agDateInput: CustomDateComponent,
			agColumnHeader: CustomCampaignsHeader,
			customTooltip: CustomTooltip
		}
		this.columnDefsSecond = [
			{
				headerName: '#', field: 'step_order_number', minWidth: 150, filter: 'agTextColumnFilter', headerCheckboxSelection: true,
				headerCheckboxSelectionFilteredOnly: true,
				checkboxSelection: true,
			},

			{
				headerName: 'IMEI', field: 'imei', cellClass: 'imeiHyperLink', minWidth: 200, filter: 'agTextColumnFilter', cellRenderer: params => {
					if (params.value != null && params.value != undefined) {
						let newLink = '<a data-toggle="modal" data-target="#deviceHistoyModal">' + params.value + '</a>';
						return newLink;
					}

				}, onCellClicked: params => {

					this.loading = true;
					setTimeout(() => {
						this.campaignService.getDeviceCampaignHistoryByImei(params.node.data.imei).subscribe(data => {
							this.deviceCampaignHistoryList = data;
							this.loading = false;
							params.node.data.device_campaign_history = this.deviceCampaignHistoryList;
							this.deviceHistoryPopupValue(params);
						}, error => {
							console.log(error)
							this.loading = false;
						});

						this.campaignService.findCampaignInstalledDevice(params.node.data.imei).subscribe(data => {

							this.campaignInstalledDevice = data;
							this.loading = false;
							params.node.data.campaign_installed_device = this.campaignInstalledDevice;
							if (params.node.data.campaign_installed_device) {
								params.node.data.campaign_installed_device.qa_TIMESTAMP = this.campaignInstalledDevice.qa_TIMESTAMP ?
									moment(this.campaignInstalledDevice.qa_TIMESTAMP).format("MM-DD-YYYY HH:mm:ss") : '';
							}
							if (params.node.data.campaign_installed_device) {
								params.node.data.campaign_installed_device.latest_MAINTENANCE_QA_TIMESTAMP = this.campaignInstalledDevice.latest_MAINTENANCE_QA_TIMESTAMP ?
									moment(this.campaignInstalledDevice.latest_MAINTENANCE_QA_TIMESTAMP).format("MM-DD-YYYY HH:mm:ss") : '';
							}
							if (params.node.data.campaign_installed_device) {
								params.node.data.campaign_installed_device.first_POWER_UP_TIMESTAMP_AFTER_QA = this.campaignInstalledDevice.first_POWER_UP_TIMESTAMP_AFTER_QA ?
									moment(this.campaignInstalledDevice.first_POWER_UP_TIMESTAMP_AFTER_QA).format("MM-DD-YYYY HH:mm:ss") : '';
							}
							if (params.node.data.campaign_installed_device) {
								params.node.data.campaign_installed_device.is_INSTALLED_FOR_CAMPAIGN_TIMESTAMP = this.campaignInstalledDevice.is_INSTALLED_FOR_CAMPAIGN_TIMESTAMP ?
									moment(this.campaignInstalledDevice.is_INSTALLED_FOR_CAMPAIGN_TIMESTAMP).format("MM-DD-YYYY HH:mm:ss") : '';
							}
							if (params.node.data.campaign_installed_device) {
								params.node.data.campaign_installed_device.last_UPDATED_AT = this.campaignInstalledDevice.last_UPDATED_AT ?
									moment(this.campaignInstalledDevice.last_UPDATED_AT).format("MM-DD-YYYY HH:mm:ss") : '';
							}
						}, error => {
							console.log(error)
							this.loading = false;
						});

					}, 300);
				}
			},

			//  Manish review changes End
			{
				headerName: 'Customer', field: 'organisation_name', minWidth: 180, filter: 'agSetColumnFilter',
				floatingFilter: true, filterParams: filterParamsCustomers,
			},

			// {
			//   headerName: '', sortable: false,
			//   width: 50,
			//   filter: false,
			//   editable: false,
			//   cellRenderer: params => {
			//     return `<i class="fa fa-times" aria-hidden="true"></i>`;
			//   }
			// },
			// {
			// 	headerName: 'Status', field: 'device_status_for_campaign', minWidth: 200, filter: 'agTextColumnFilter',
			// 	cellRenderer: params => {
			// 		if (params.data.device_status_for_campaign === "Not Started") {
			// 			return `Waiting`;
			// 		} else {
			// 			return params.data.device_status_for_campaign;
			// 		}
			// 	}
			// },
			// {
			// 	headerName: 'Status', field: 'device_status_for_campaign', minWidth: 200, filter: 'agTextColumnFilter',
			// 	cellRenderer: params => {
			// 		if (params.data.device_status_for_campaign === "Not Started") {
			// 			return `Waiting`;
			// 		} else {
			// 			return params.data.device_status_for_campaign;
			// 		}
			// 	}
			// },
			{
				headerName: 'Status', field: 'device_status_for_campaign', minWidth: 180, filter: 'agSetColumnFilter',
				floatingFilter: true, filterParams: filterParamsStatus,
			},
			{ headerName: 'Installed', field: 'installed_flag', minWidth: 200, filter: 'agTextColumnFilter' },
			{
				headerName: 'Last Maintenance Report', field: 'last_report', minWidth: 200, filter: 'agTextColumnFilter',
				cellRenderer: params => {
					console.log(this.currentDate.getUTCDate());

					console.log("params.data>>>>>>>>");
					console.log(params.data);
					var date2 = null;
					if (params.data != undefined && params.data.last_report != undefined) {
						date2 = new Date(params.data.last_report);
						console.log(date2.getTime() + " diff " + this.currentDate.getTime());
						console.log(params.data.last_report);

					}

					if (params.data != undefined && params.data.last_report != undefined && params.data.last_report != null) {
						if ((this.currentDate.getTime() - date2.getTime()) < this.oneDay) {
							console.log("normal");
							return '<div style="text-align:center;" class="status">' + params.data.last_report + '</div>'

						}
						else if ((this.currentDate.getTime() - date2.getTime()) > this.threeDay) {
							// alert("condition match");
							console.log("red");
							return '<div class="status" style="background-color:red; text-align:center;">' + params.data.last_report + '</div>'
						}
						else if ((this.currentDate.getTime() - date2.getTime()) > this.oneDay && (this.currentDate.getTime() - date2.getTime()) < this.threeDay) {
							console.log("yellow");
							return '<div class="status" style="background-color:yellow; text-align:center;">' + params.data.last_report + '</div>'
						}
						else {
							return '<div class="status" style="text-align:center;">' + params.data.last_report + '</div>'

						}
					}
					else {
						console.log("else");
						if (params.data != undefined && params.data.last_report != undefined && params.data.last_report != null) {
							return '<div style="text-align:center;" class="status>' + params.data.last_report + '</div>'
						} else {
							return '<div style="text-align:center;" class="status>' + "" + '</div>'

						}
					}
				}
			},
			{ headerName: 'Comments', field: 'comments', minWidth: 400, filter: 'agTextColumnFilter', cellRenderer: "btnCellRenderer" },
			{
				headerName: '', sortable: false,
				width: 50,
				pinned: 'right',
				filter: false,
				editable: false,
				cellRenderer: params => {
					return `<i class="fa fa-times" aria-hidden="true"></i>`;

				}
			}
		];
		this.defaultColDefSecond = {
			flex: 1,
			sortable: true,
			filter: true,
			enablePivot: true,
			resizable: true,
			floatingFilter: true,
		};

		var sequenceObj1 = {
			package: {
				"package_name": null
			},
			step_order_number: "Baseline",
			tooltip: "The Package You set as a baseline will limit the campaign to only those gateways in the selected list that currently have this configuration."
		}
		var sequenceObj2 = {
			package: {
				"package_name": null
			},

		}

		var sequenceObj3 = {
			package: {
				"package_name": null
			},

		}

		var sequenceObj4 = {
			package: {
				"package_name": null
			},
			step_order_number: "End State",
			tooltip: "The end state defines the expected configuration of devices after this campaign has been completed."
		}
		this.sequenceData.push(sequenceObj1);
		this.sequenceData.push(sequenceObj2);
		this.sequenceData.push(sequenceObj3);
		this.sequenceData.push(sequenceObj4);

		this.rowClassRules = {
			'high-light-row-of-sequence': function (params) {
				var stepOrderNumber = params.data.step_order_number;
				return stepOrderNumber == 'Baseline' || stepOrderNumber == 'End State';
			}
		};
		campaignService.getPackages(1, "packageName", "asc", {}, 100000).subscribe(data => {
			this.packageList = data.body;
			this.packageListName = [];
			this.packageListForDisplay = [];
			for (var i = 0; i < this.packageList.length; i++) {
				this.packageListName.push(this.packageList[i].package_name);
				this.packageListForDisplay.push(this.packageList[i].package_name);
			}
			packageListNameGloble = this.packageListName;

		}, error => {
			console.log(error);
		})

		// this.router.routeReuseStrategy.shouldReuseRoute = function () {
		//   return false;
		// };

		// this.mySubscription = this.router.events.subscribe((event) => {
		//   if (event instanceof NavigationEnd) {
		//     // Trick the Router into believing it's last link wasn't previously loaded
		//     this.router.navigated = false;
		//     //alert(event.url);
		//   }
		// });

		if (sessionStorage.getItem('add_campaign_list_page_size') != undefined) {
			console.log(sessionStorage.getItem('add_campaign_list_page_size') + " from Cons");
			this.pageLimit = Number(sessionStorage.getItem('add_campaign_list_page_size'));
		}

	}
	getFilterValuesData() {
		return this.companiesName;
	}
	ngOnDestroy() {
		// if (this.mySubscription) {
		//   this.mySubscription.unsubscribe();
		// }
	}

	getEmergencyStopFlagValue() {
		this.campaignService.getEmergencyStopFlagValue()
			.subscribe(data => {
				this.isEmergencyStopFlag = data.status;
			});
	}

	checkValueIsEqual(params, fieldName) {
		let flag = false;
		if (params.data.step_order_number != "Baseline") {
			for (var i = 0; i < this.sequenceData.length; i++) {
				if (this.sequenceData[i].step_order_number == params.data.step_order_number) {
					if (fieldName == 'package.bin_version') {
						if (this.sequenceData[i].package.bin_version != this.sequenceData[i - 1].package.bin_version) {
							flag = true;
							break;
						}
					} else if (fieldName == 'package.app_version') {
						if (this.sequenceData[i].package.app_version != this.sequenceData[i - 1].package.app_version) {
							flag = true;
							break;
						}
					} else if (fieldName == 'package.mcu_version') {
						if (this.sequenceData[i].package.mcu_version != this.sequenceData[i - 1].package.mcu_version) {
							flag = true;
							break;
						}
					} else if (fieldName == 'package.ble_version') {
						if (this.sequenceData[i].package.ble_version != this.sequenceData[i - 1].package.ble_version) {
							flag = true;
							break;
						}
					} else if (fieldName == 'package.config1') {
						if (this.sequenceData[i].package.config1 != this.sequenceData[i - 1].package.config1) {
							flag = true;
							break;
						}
					} else if (fieldName == 'package.config2') {
						if (this.sequenceData[i].package.config2 != this.sequenceData[i - 1].package.config2) {
							flag = true;
							break;
						}
					} else if (fieldName == 'package.config3') {
						if (this.sequenceData[i].package.config3 != this.sequenceData[i - 1].package.config3) {
							flag = true;
							break;
						}
					} else if (fieldName == 'package.config4') {
						if (this.sequenceData[i].package.config4 != this.sequenceData[i - 1].package.config4) {
							flag = true;
							break;
						}
					} else if (fieldName == 'package.config1_crc') {
						if (this.sequenceData[i].package.config1_crc != this.sequenceData[i - 1].package.config1_crc) {
							flag = true;
							break;
						}
					} else if (fieldName == 'package.config2_crc') {
						if (this.sequenceData[i].package.config2_crc != this.sequenceData[i - 1].package.config2_crc) {
							flag = true;
							break;
						}
					} else if (fieldName == 'package.config3_crc') {
						if (this.sequenceData[i].package.config3_crc != this.sequenceData[i - 1].package.config3_crc) {
							flag = true;
							break;
						}
					} else if (fieldName == 'package.config4_crc') {
						if (this.sequenceData[i].package.config4_crc != this.sequenceData[i - 1].package.config4_crc) {
							flag = true;
							break;
						}
					} else if (fieldName == 'package.device_type') {
						if (this.sequenceData[i].package.device_type != this.sequenceData[i - 1].package.device_type) {
							flag = true;
							break;
						}
					} else if (fieldName == 'package.lite_sentry_hardware') {
						if (this.sequenceData[i].package.lite_sentry_hardware != this.sequenceData[i - 1].package.lite_sentry_hardware) {
							flag = true;
							break;
						}
					} else if (fieldName == 'package.lite_sentry_app') {
						if (this.sequenceData[i].package.lite_sentry_app != this.sequenceData[i - 1].package.lite_sentry_app) {
							flag = true;
							break;
						}
					} else if (fieldName == 'package.lite_sentry_boot') {
						if (this.sequenceData[i].package.lite_sentry_boot != this.sequenceData[i - 1].package.lite_sentry_boot) {
							flag = true;
							break;
						}
					} else if (fieldName == 'package.microsp_mcu') {
						if (this.sequenceData[i].package.microsp_mcu != this.sequenceData[i - 1].package.microsp_mcu) {
							flag = true;
							break;
						}
					} else if (fieldName == 'package.microsp_app') {
						if (this.sequenceData[i].package.microsp_app != this.sequenceData[i - 1].package.microsp_app) {
							flag = true;
							break;
						}
					} else if (fieldName == 'package.cargo_maxbotix_hardware') {
						if (this.sequenceData[i].package.cargo_maxbotix_hardware != this.sequenceData[i - 1].package.cargo_maxbotix_hardware) {
							flag = true;
							break;
						}
					} else if (fieldName == 'package.cargo_maxbotix_firmware') {
						if (this.sequenceData[i].package.cargo_maxbotix_firmware != this.sequenceData[i - 1].package.cargo_maxbotix_firmware) {
							flag = true;
							break;
						}
					} else if (fieldName == 'package.cargo_riot_hardware') {
						if (this.sequenceData[i].package.cargo_riot_hardware != this.sequenceData[i - 1].package.cargo_riot_hardware) {
							flag = true;
							break;
						}
					} else if (fieldName == 'package.cargo_riot_firmware') {
						if (this.sequenceData[i].package.cargo_riot_firmware != this.sequenceData[i - 1].package.cargo_riot_firmware) {
							flag = true;
							break;
						}
					}
				}
			}
		}
		return flag;
	}

	canDeactivate() {
		console.log("guard check");
		return this.isDirty;
	}

	changeValueOfExclude() {
		this.changeDetection = true;
		this.isDirty = true;
	}

	showExcludeGatewaysTypes() {
		this.exclude_gateways_types = !this.exclude_gateways_types;
	}


	ngOnInit() {
		this.getAllcompanies();
		this.getActiveCompanies();
		if (sessionStorage.getItem('add_campaign_list_page_size') != undefined) {
			console.log(sessionStorage.getItem('add_campaign_list_page_size') + "from ngOnIt");
			this.pageLimit = Number(sessionStorage.getItem('add_campaign_list_page_size'));
		}
		else {
			this.pageLimit = 25;
		}

		this.userCompanyType = sessionStorage.getItem('type');
		this.isSuperAdmin = sessionStorage.getItem('IS_ROLE_SUPERADMIN');
		if (this.isSuperAdmin === 'true') {
			this.router.navigate(['add-campaign']);
		} else {
			this.router.navigate(['customerAssets']);
		}
		this.tempFlag = false;
		this.changeDetection = false;
		this.updateBaseline = false;
		if (this.campaignService.campaignDetail != undefined && this.campaignService.editFlag == true) {
			this.updateFlag = true;
		}
		this.campaignService.editFlag = false;
		this.initializeForm();
		//this.getCustomerCompaniesList();
		$(document).ready(function () {
			$('[data-toggle="tooltip"]').tooltip({
				placement: 'top'
			});
		});

		this.getEmergencyStopFlagValue();
	}

	clickOnStatisticsDetail(param, count) {

		if (count <= 0) {
			return false;
		}
		this.clearFilterForSecond();
		if (param == "Not Started") {
			setTimeout(() => {
				var filterComponent = this.gridApiSecond.getFilterInstance("step_1");
				filterComponent.setModel({
					type: "Contains",
					filter: "Pending"
				});
				filterComponent.onFilterChanged();
			}, 150);
		} else if (param == "In Progress") {
			setTimeout(() => {
				var splitted = this.columnDefsSecond[this.columnDefsSecond.length - 1].headerName.split(" ", 2);
				if (splitted[1] != "1") {
					var filterComponent = this.gridApiSecond.getFilterInstance("step_1");
					filterComponent.setModel({
						type: "notContains",
						filter: "Pending"
					});
					var filterComponent1 = this.gridApiSecond.getFilterInstance("step_" + splitted[1]);
					filterComponent1.setModel({
						condition1:
						{
							filter: "In progress",
							type: "Contains"
						},
						condition2:
						{
							type: "Contains",
							filter: "Pending"
						},
						operator: "OR"
					});
					filterComponent1.onFilterChanged();
				} else {
					var filterComponent = this.gridApiSecond.getFilterInstance("step_1");
					filterComponent.setModel({
						type: "Contains",
						filter: "In progress"
					});
				}

				filterComponent.onFilterChanged();
			}, 150);
		} else if (param == "Completed") {
			setTimeout(() => {
				var filterComponent = this.gridApiSecond.getFilterInstance("device_status_for_campaign");
				filterComponent.setModel({
					type: "Contains",
					filter: "Completed"
				});
				filterComponent.onFilterChanged();
			}, 150);
		} else if (param == "On Hold") {
			setTimeout(() => {
				var filterComponent = this.gridApiSecond.getFilterInstance("device_status_for_campaign");
				filterComponent.setModel({
					type: "Contains",
					filter: "Problem"
				});
				filterComponent.onFilterChanged();
			}, 150);
		} else if (param == "Not Eligible") {
			setTimeout(() => {
				var filterComponent = this.gridApiSecond.getFilterInstance("device_status_for_campaign");
				filterComponent.setModel({
					condition1:
					{
						filter: "Not Eligible",
						type: "Contains"
					},
					condition2:
					{
						type: "Contains",
						filter: "Unknown"
					},
					operator: "OR"
				});
				filterComponent.onFilterChanged();
			}, 150);
		} else if (param == "Total Gateways") {
			this.clearFilterForSecond();
		}

		this.gridApiSecond.setColumnDefs(this.columnDefsSecond);
		this.gridApiSecond.setRowData([]);
		this.gridApiSecond.setRowData(this.deviceList);
		this.imei_list = [];
		for (let i = 0; i < this.deviceList.length; i++) {
			this.imei_list.push(this.deviceList[i].imei);
		}
		this.selectedTab.setValue(2);
	}

	clickOnBreakdownByStepsDetail(param, count, index) {
		if (count <= 0) {
			return false;
		}
		this.clearFilterForSecond();
		if (param == "Not Started") {
			setTimeout(() => {
				var filterComponent = this.gridApiSecond.getFilterInstance("step_" + index);
				filterComponent.setModel({
					type: "Contains",
					filter: "Pending"
				});
				filterComponent.onFilterChanged();
			}, 150);
		} else if (param == "In Progress") {
			setTimeout(() => {
				var filterComponent = this.gridApiSecond.getFilterInstance("step_" + index);
				filterComponent.setModel({
					type: "Contains",
					filter: "In progress"
				});
				filterComponent.onFilterChanged();
			}, 150);
		} else if (param == "Completed") {
			setTimeout(() => {
				var filterComponent = this.gridApiSecond.getFilterInstance("step_" + index);
				filterComponent.setModel({
					condition1:
					{
						filter: "In progress",
						type: "notContains"
					},
					condition2:
					{
						type: "notContains",
						filter: "Pending"
					},
					operator: "AND"
				});
				filterComponent.onFilterChanged();
			}, 150);
		} else if (param == "On Hold") {
			setTimeout(() => {
				var filterComponent = this.gridApiSecond.getFilterInstance("step_" + index);
				filterComponent.setModel({
					type: "Contains",
					filter: "On hold"
				});
				filterComponent.onFilterChanged();
			}, 150);
		}
		this.gridApiSecond.setColumnDefs(this.columnDefsSecond);
		this.gridApiSecond.setRowData([]);
		this.gridApiSecond.setRowData(this.deviceList);
		this.imei_list = [];
		for (let i = 0; i < this.deviceList.length; i++) {
			this.imei_list.push(this.deviceList[i].imei);
		}
		this.selectedTab.setValue(2);
	}


	onCellClickedForSecond(params) {
		if (params.column.getColId() === '0') {
			this.selectedGateway = params;
			if (this.updateFlag) {

				this.actionName = "ConfirmDeleteGateway";
					this.commonTitle = "Confirm Delete";
					this.commonMessage = "Are you sure you would like to delete this campaign?";
					this.commonModelOption = "Yes";
					$('#commonModal').modal('show');
					return;

				// if (this.campaignStatus == 'Not Started') {
				// 	this.actionName = "ConfirmDeleteGateway";
				// 	this.commonTitle = "Confirm Delete";
				// 	this.commonMessage = "Are you sure you would like to delete this campaign?";
				// 	this.commonModelOption = "Yes";
				// 	$('#commonModal').modal('show');
				// 	return;
				// } else if (params.data.step_1 == undefined || params.data.step_1 == "Pending" || params.data.step_1 == "Problem") {
				// 	this.actionName = "ConfirmDeleteGateway";
				// 	this.commonTitle = "Confirm Delete";
				// 	this.commonMessage = "Are you sure you would like to delete this campaign?";
				// 	this.commonModelOption = "Yes";
				// 	$('#commonModal').modal('show');
				// 	return;
				// } else if (params.data.device_status_for_campaign == 'Eligible') {
				// 	this.imeis_to_be_removed.push(params.data.imei);
				// 	this.device_status_for_campaign = params.data.device_status_for_campaign;
				// 	this.actionName = "ConfirmDeleteGateway";
				// 	this.commonTitle = "Confirm Delete";
				// 	this.commonMessage = "Are you sure you want to remove this device from an active campaign?";
				// 	this.commonModelOption = "Yes";
				// 	$('#commonModal').modal('show');
				// 	return;
				// }

				// else if (this.campaignStatus == 'In progress' && params.data.step_1 !== "Pending" && params.data.step_1 != "Problem") {
				// 	this.toastr.error("This gateway has either completed this campaign, or is currently in progress, and cannot be removed.", "Gateway Cannot Be Removed", { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
				// 	return;
				// }
			} else {
				this.actionName = "ConfirmDeleteGateway";
				this.commonTitle = "Confirm Delete";
				this.commonMessage = "Are you sure you would like to delete this campaign?";
				this.commonModelOption = "Yes";
				$('#commonModal').modal('show');
				return;
			}
			if (this.updateFlag) {

				if (this.campaignService.campaignDetail.campaign_status == "In progress") {
					if (params.data.step_1 == undefined || params.data.step_1 != "Pending") {
						this.toastr.error("This gateway has either completed this campaign, or is currently in progress, and cannot be removed.", "Gateway Cannot Be Removed", { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
						return;
					}
				}
				//if (this.campaignService.campaignDetail.campaign_status !== "In progress") {
				if (params.data.step_1 == undefined || params.data.step_1 == "Pending") {
					for (let device of this.deviceList) {
						if (device != null && device != undefined && device.imei != null && device.imei == params.data.imei) {
							this.deviceList.splice(this.deviceList.indexOf(device), 1);
							break;
						}
					}
					this.gridApiSecond.setRowData([]);
					this.deviceList.forEach(function (data, index) {
						data.step_order_number = index + 1;
					});
					this.gridApiSecond.setRowData(this.deviceList);
				} else {
					//this.toastr.error("Can not remove the In progress or Completed gateway");
					this.toastr.error("This gateway has either completed this campaign, or is currently in progress, and cannot be removed.", "Gateway Cannot Be Removed", { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
				}
				// } else {
				//   this.toastr.error("You can not remove the gateway of In progress campaign");
				// }

			} else {


				for (let device of this.deviceList) {
					if (device != null && device != undefined && device.imei != null && device.imei == params.data.imei) {
						this.deviceList.splice(this.deviceList.indexOf(device), 1);
						break;
					}
				}
				this.gridApiSecond.setRowData([]);
				this.deviceList.forEach(function (data, index) {
					data.step_order_number = index + 1;
				});
				this.gridApiSecond.setRowData(this.deviceList);
			}
		}
	}

	clearFilter() {
		// this.filterValuesJsonObj = {};
		// this.filterValues = new Map();
		// this.getCampaigns(1);
		// this.clearFormControls();
		// this.isClearButtonVisible = false;
		this.gridApi.setFilterModel(null);


	}

	getSelectedRowData() {
		let selectedNodes = this.gridApiSecond.getSelectedNodes();
		let selectedData = selectedNodes.map(node => node.data);
		return selectedData;
	}

	clearFilterForSecond() {
		//this.getSelectedRowData();
		// var filterComponent = this.gridApiSecond.getFilterInstance("step_1");
		// console.log("========================> filterComponent");
		// console.log(filterComponent.getModel());
		this.gridApiSecond.setFilterModel(null);
	}

	archiveCampaign() {
		if (this.campaignStatus == "Finished") {
			this.actionName = "Archive";
			this.commonTitle = "Confirm Archiving";
			this.commonMessage = "Are you sure you would like to archive this campaign?";
			this.commonModelOption = "Yes";
			$('#commonModal').modal('show');
		} else {
			this.toastr.error("This campaign must be completed before it can be archived.", "Campaign Cannot be Archived", { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true })
		}
	}

	pauseCampaign() {
		if (this.isEmergencyStopFlag) {
			this.toastr.error("User can not update the \"Emergency Stopped\" campaigns", "Update Alert", { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
			return false;
		}
		this.actionName = "Stopped";
		this.commonTitle = "Stop Campaign Execution";
		this.commonMessage = "Are you sure you want to stop execution of this campaign? ";
		this.commonModelOption = "Yes";
		$('#commonModal').modal('show');
	}

	playCampaign() {
		if (this.isEmergencyStopFlag) {
			this.toastr.error("User can not update the \"Emergency Stopped\" campaigns", "Update Alert", { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
			return false;
		}
		this.actionName = "Resume";
		this.commonTitle = "Resume Campaign Execution";
		this.commonMessage = "Are you sure you want to resume execution of this campaign?";
		this.commonModelOption = "Yes";
		$('#commonModal').modal('show');
	}

	retireCampaign() {
		if (this.campaignStatus == "In progress") {
			this.actionName = "Retire";
			this.commonTitle = "Confirm Campaign Retirement";
			this.commonMessage = "Are you sure you want to retire this campaign?";
			this.commonModelOption = "Yes";
			$('#commonModal').modal('show');
		} else {
			this.toastr.error("Only campaigns that are in progress can be retired.", "Campaign Cannot be Retired", { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true })
		}
	}

	deleteCampaign() {
		if (this.campaignStatus == "Not Started") {
			this.actionName = "Delete";
			this.commonTitle = "Confirm Deactivation";
			this.commonMessage = "Are you sure you want to delete this campaign?";
			this.commonModelOption = "Yes";
			$('#commonModal').modal('show');
		} else {
			this.toastr.error("This campaign has already been started and cannot be deleted.", "Campaign is In Use", { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true })
		}
	}

	commonModalCancel() {
		$('#commonModal').modal('hide');
		if (this.actionName == "changeUploadToCustomer" || this.actionName == "changeCustomerToUpload") {
			this.addCampaign.patchValue({ 'is_imei_for_customer': this.stateOfCustomerOrUpdate });
		}
	}

	commonAction() {
		if (this.actionName == "Archive") {
			this.changeCampaignStatus("Archived", false);
			$('#commonModal').modal('hide');
		} else if (this.actionName == "Retire") {
			this.changeCampaignStatus("Retired", false);
			$('#commonModal').modal('hide');
		} else if (this.actionName == "Delete") {
			this.deleteCampaignDb();
			$('#commonModal').modal('hide');
		} else if (this.actionName == "Stopped") {
			this.pauseCampaignDB();
			$('#commonModal').modal('hide');
		} else if (this.actionName == "Resume") {
			//this.resumeCampaign();
			this.startCampaignDb();
			$('#commonModal').modal('hide');
		} else if (this.actionName == "changeUploadToCustomer") {
			this.changeDetection = true;
			this.deviceList = [];
			this.gridApiSecond.setRowData([]);
			$('#commonModal').modal('hide');
		} else if (this.actionName == "changeCustomerToUpload") {
			this.changeDetection = true;
			this.deviceList = [];
			this.gridApiSecond.setRowData([]);
			$('#commonModal').modal('hide');
		} else if (this.actionName == "AgainUploadFile") {
			this.changeDetection = true;
			this.deviceList = [];
			this.gridApiSecond.setRowData([]);
			this.addCampaign.patchValue({ 'file': null });
			this.selectedFileName = null;
			$('#commonModal').modal('hide');
			$('#uploadFileModal').modal('show');
		} else if (this.actionName == "ConfirmDeletionForStep") {
			this.changeDetection = true;
			this.removePackage(this.selectedStep);
			this.gridApi.setRowData([]);
			this.callChangeSequence();
			this.selectedStep = null
			$('#commonModal').modal('hide');
		} else if (this.actionName == "ConfirmDeleteGateway") {

 			let obj = this.rowDataSecond.find(item => item.imei === this.selectedGateway.data.imei);
			if (obj) {
				this.rowDataSecond.splice(this.rowDataSecond.indexOf(obj), 1);
				this.imeis_to_be_removed.push(obj.imei);
			}
			 
			this.rowDataSecond.forEach(function (data, index) {
				data.step_order_number = index + 1;
			});
 
			console.log(this.imeis_to_be_removed);
			this.refreshDelete = true;
			this.gridApiSecond.onFilterChanged();
			this.changeDetection = true;
			$('#commonModal').modal('hide');

			// this.changeDetection = true;
			// for (let device of this.deviceList) {
			// 	if (device != null && device != undefined && device.imei != null && device.imei == this.selectedGateway.data.imei) {
			// 		this.deviceList.splice(this.deviceList.indexOf(device), 1);
			// 		this.imei_list.splice(this.imei_list.indexOf(this.selectedGateway.data.imei), 1);
			// 		this.imeis_to_be_removed.push(this.selectedGateway.data.imei);
			// 		this.isDirty = true;
			// 		break;
			// 	}
			// }
			// this.gridApiSecond.setRowData([]);
			// this.deviceList.forEach(function (data, index) {
			// 	data.step_order_number = index + 1;
			// });
			// this.gridApiSecond.setRowData(this.deviceList);
			// this.selectedGateway = null;
			// $('#commonModal').modal('hide');
		} else if (this.actionName == "ConfirmDeleteGatewayBulk") {
			this.saveCampaign(this.addCampaign);

			// console.log(this.rowDataSecond);
			// for (let selectedDevice of this.selectedDataForBulkDelete) {
			// 	let obj = this.rowDataSecond.find(item => item.imei === selectedDevice.imei);
			// 	if (obj) {
			// 		this.rowDataSecond.splice(this.rowDataSecond.indexOf(obj), 1);
			// 		this.imeis_to_be_removed.push(obj.imei);
			// 	}
			// }
			// this.rowDataSecond.forEach(function (data, index) {
			// 	data.step_order_number = index + 1;
			// });
			// this.refreshDelete = true;
			// this.gridApiSecond.onFilterChanged();
			// this.changeDetection = true;
			// $('#commonModal').modal('hide');
			// this.selectedDataForBulkDelete = null;
		} else if (this.actionName == "ConfirmExcludeGatewayBulk") {
			this.excludedImeis = [];
			console.log(this.rowDataSecond);
			for (let selectedDevice of this.selectedDataForBulkExclude) {
				let obj = this.rowDataSecond.find(item => item.imei === selectedDevice.imei);
				if (obj) {
					this.rowDataSecond.splice(this.rowDataSecond.indexOf(obj), 1);
					this.excludedImeis.push(obj.imei);
				}
			}
			this.rowDataSecond.forEach(function (data, index) {
				data.step_order_number = index + 1;
			});
			this.refreshDelete = true;
			this.gridApiSecond.onFilterChanged();
			this.changeDetection = true;
			this.addCampaign.patchValue({ 'excluded_imeis': this.excludedImeis });
			$('#commonModal').modal('hide');
			this.selectedDataForBulkDelete = null;
			this.saveCampaign(this.addCampaign);
		} else if (this.actionName == "ResetStatusGatewayBulk") {
			this.loading = true;
			this.campaignService.resetStatusOfProblemDevice(this.selectedDeviceIdForResetStatus).subscribe(data => {
				this.toastr.success(data.message, null, { toastComponent: CustomSuccessToastrComponent });
				this.loading = false;
				this.router.navigate(['campaigns']);
			}, error => {
				console.log(error);
				this.loading = false;
			});
		}
	}

	onSortChanged(event) {
		var sortModel = this.gridApi.getSortModel();
		sortActive = sortModel && sortModel.length > 0;
		var suppressRowDrag = sortActive || filterActive;
		this.gridApi.setSuppressRowDrag(suppressRowDrag);
	}

	onFilterChanged(event) {
		filterActive = this.gridApi.isAnyFilterPresent();
		var suppressRowDrag = sortActive || filterActive;
		this.gridApi.setSuppressRowDrag(suppressRowDrag);
	}

	deleteCampaignForUpdate() {
		this.campaignService.campaignId = this.campaignService.campaignDetail.uuid;
	}

	startCampaign() {
		if (this.isEmergencyStopFlag) {
			this.toastr.error("User can not update the \"Emergency Stopped\" campaigns", "Update Alert", { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
			return false;
		}
		this.campaignService.campaignDetail.is_active = true;
	}

	startCampaignDb() {
		//this.saveCampaign(this.addCampaign);
		this.activateCampaign();
	}

	cancleStartCampaign() {
		this.campaignService.campaignDetail.is_active = false;
	}

	print() {
		console.log("change method call");
		console.log(JSON.stringify(this.addCampaign.value));
		let campaign_name = this.addCampaign.controls.campaign_name.value;
		var campaignNameTrimed = null;
		if (campaign_name != undefined && campaign_name != null && campaign_name != "") {
			//campaignNameTrimed = campaign_name.trim();
			campaignNameTrimed = campaign_name.replace(/ {2,}/g, ' ').trim();
		} else {
			//this.loading = false;
		}

		if (campaignNameTrimed == undefined || campaignNameTrimed == null || campaignNameTrimed == "") {
			this.addCampaign.patchValue({ 'campaign_name': "" });
			this.addCampaign.get('campaign_name').setValidators([Validators.required]);
			this.addCampaign.get('campaign_name').updateValueAndValidity();
		}

		if (this.changeDetection) {
			console.log("========================= CHANGE DET =========" + this.changeDetection);
			this.isDirty = true;
		}
		console.log("========================= CHANGE DIRTY =========" + this.isDirty);
	}

	refreshDataForStepsTable() {
		var version_migration_detail_list = [];
		let campaignObjectValue = JSON.parse(JSON.stringify(this.campaignObjCopy));
		if (campaignObjectValue.version_migration_detail != null && campaignObjectValue.version_migration_detail.length > 0) {
			for (var i = 0; i < campaignObjectValue.version_migration_detail.length; i++) {
				var version_migration_detail_obj = {
					"at_command": campaignObjectValue.version_migration_detail[i].at_command,
					"step_order_number": campaignObjectValue.version_migration_detail[i].step_order_number,
					"step_id": campaignObjectValue.version_migration_detail[i].step_id,
					"package": campaignObjectValue.version_migration_detail[i].from_package,
					"tooltip": ""
				}

				if (i == 0) {
					version_migration_detail_obj.step_order_number = "Baseline";
					version_migration_detail_obj.tooltip = "The Package You set as a baseline will limit the campaign to only those gateways in the selected list that currently have this configuration."
				}

				version_migration_detail_list.push(version_migration_detail_obj);

				if (i == campaignObjectValue.version_migration_detail.length - 1) {
					var version_migration_detail_obj_new = {
						"at_command": "",
						"step_order_number": "End State",
						"package": campaignObjectValue.version_migration_detail[i].to_package,
						"tooltip": "The end state defines the expected configuration of devices after this campaign has been completed."
					}

					version_migration_detail_list.push(version_migration_detail_obj_new);
				}
			}

			this.sequenceData = version_migration_detail_list;
			this.callChangeSequence();
			this.gridApi.setRowData([]);
			this.setDataAndRefreshInGrid();
		}
	}

	onCellValueChanged(params) {
		var colId = params.column.getId();
		if (colId === 'package.package_name') {

			if (this.updateFlag && this.campaignService.campaignDetail.campaign_status == "In progress") {
				this.toastr.error("The package sequence can no longer be modified because this campaign is in progress.", "Campaign in Progress", { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true })
				this.refreshDataForStepsTable();
				return;
			}

			if (this.packageList != null && this.packageList.length > 0) {
				this.isDirty = true;
				for (var i = 0; i < this.packageList.length; i++) {
					for (var j = 0; j < this.sequenceData.length; j++) {
						if (this.packageList[i].package_name == this.sequenceData[j].package.package_name) {
							this.sequenceData[j].package = JSON.parse(JSON.stringify(this.packageList[i]));
							//break;
							//this break caused issues when two step had same package selected
						}
					}
				}

				if (params.data.step_order_number == "Baseline") {
					this.campaignService.findBaseIsExistOrNot(params.data.package.uuid)
						.subscribe(data => {
							//if (data.message == "Package as baseline already exist") {
							var message = "";
							if (data != undefined && data != null && data.length > 0) {
								for (var i = 0; i < data.length; i++) {
									if ((data.length - 1) == i) {
										message = message + data[i].campaign_name
									} else {
										message = message + data[i].campaign_name + ",";
									}
								}
								this.toastr.error("The baseline device configuration selected for this campaign is already used in " + message + ". Please select a different baseline.", "Baseline Configuration Used by Another Campaign", { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true })
								this.sequenceData[0].package = { "package_name": null };
								this.setDataAndRefreshInGrid();
							}
							else {
								//API call to fetch details when baseline changes
								this.updateBaseline = true;
								this.checkFileData();
							}
							//}
						});
				}
				this.setDataAndRefreshInGrid();
			}
		} else if (colId === 'at_command') {

			if (this.updateFlag && this.campaignService.campaignDetail.campaign_status == "In progress") {
				this.toastr.error("The package sequence can no longer be modified because this campaign is in progress.", "Campaign in Progress", { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true })
				this.refreshDataForStepsTable();
				return;
			}

			var atCommandValue = "";
			if (params.data.at_command != undefined && params.data.at_command != null && params.data.at_command != "") {
				atCommandValue = params.data.at_command.trim();
			}
			if (!atCommandValue.startsWith("AT+X")) {
				this.toastr.error("Valid commands for the campaign must begin with ‘AT+X’. Please check your entries and try again.", "Please Enter a Valid Command", { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true })
				for (var j = 0; j < this.sequenceData.length; j++) {
					if (this.sequenceData[j].step_order_number == params.data.step_order_number) {
						this.sequenceData[j].at_command = null;
					}
				}
				this.setDataAndRefreshInGrid();
			} else {
				for (var j = 0; j < this.sequenceData.length; j++) {
					if (this.sequenceData[j].step_order_number == params.data.step_order_number) {
						this.sequenceData[j].at_command = params.data.at_command.trim();
					}
				}
				this.setDataAndRefreshInGrid();
			}
		}
	}

	setDataAndRefreshInGrid() {
		this.gridApi.setRowData(this.sequenceData);
		var paramsRe = {
			force: true,
			suppressFlash: false,
		};
		this.gridApi.refreshCells(paramsRe);
	}
	onCellClicked(params) {
		if (params.column.getColId() === '0' && this.sequenceData.length > 2) {
			//this.sequenceData = this.sequenceData.filter(({ step_order_number }) => step_order_number !== params.data.step_order_number);
			if ((this.updateFlag && this.campaignService.campaignDetail.campaign_status == "Not Started") || !this.updateFlag) {
				this.changeDetection = true;
				this.isDirty = true;
				this.actionName = "ConfirmDeletionForStep";
				this.commonTitle = "Confirm Deletion";
				this.commonMessage = "Are you sure you want to remove this step from the campaign? ";
				this.commonModelOption = "Yes";
				this.selectedStep = params;
				$('#commonModal').modal('show');
				return;
			}

			if (this.updateFlag) {
				if (this.campaignService.campaignDetail.campaign_status == "In progress") {
					//this.toastr.error("You can not delete the any step of In progress campaign");
					this.toastr.error("The package sequence can no longer be modified because this campaign is in progress.", "Campaign in Progress", { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true })
					return;
				}
				this.removePackage(params);
				this.gridApi.setRowData([]);
				this.callChangeSequence();
			} else {
				this.changeDetection = true;
				this.isDirty = true;
				this.removePackage(params);
				this.gridApi.setRowData([]);
				this.callChangeSequence();
			}
		}
	}

	removePackage(packageObj: any): void {
		for (let seq of this.sequenceData) {
			if (seq.step_order_number != "Baseline" && seq.step_order_number != "End State" && seq.step_order_number == packageObj.data.step_order_number) {
				this.sequenceData.splice(this.sequenceData.indexOf(seq), 1);
				break;
			}
		}
	};

	deleteSelectedDevice(): void {
		let selectedNodes = this.gridApiSecond.getSelectedNodes();
		let selectedData = selectedNodes.map(node => node.data);
		this.selectedDataForBulkDelete = selectedData;
		this.actionName = "ConfirmDeleteGatewayBulk";
		this.commonTitle = "Confirm Delete";
		this.commonMessage = "Are you sure you want to delete these IMEIs from the list?";
		this.commonModelOption = "Yes";
		$('#commonModal').modal('show');
		// if (this.updateFlag) {
		// 	if (this.campaignStatus == 'Not Started') {
		// 		this.actionName = "ConfirmDeleteGatewayBulk";
		// 		this.commonTitle = "Confirm Delete";
		// 		this.commonMessage = "Are you sure you want to delete these IMEIs from the list?";
		// 		this.commonModelOption = "Yes";
		// 		$('#commonModal').modal('show');
		// 		return;
		// 	} else if (this.campaignStatus == 'In progress') {
		// 		// && params.data.step_1 !== "Pending" && params.data.step_1 != "Problem"
		// 		let finalErrorString = [];
		// 		for (let selectedDevice of selectedData) {
		// 			if (selectedDevice.step_1 !== "Pending" && selectedDevice.step_1 != "Problem") {
		// 				// commented to allow delete without condition 
		// 				//finalErrorString.push(selectedDevice.imei);
		// 			}
		// 		}
		// 		if (finalErrorString.length > 0) {
		// 			this.toastr.error(finalErrorString.toString(), "The following gateways have either completed this campaign, or are currently in progress, and cannot be removed. ", { toastComponent: CustomBatchErrorToastrComponent, tapToDismiss: true, disableTimeOut: true, enableHtml: true });
		// 			this.actionName = "ConfirmDeleteGatewayBulk";
		// 			this.commonTitle = "Confirm Delete";
		// 			this.commonMessage = "Are you sure you want to delete these IMEIs from the list?";
		// 			this.commonModelOption = "Yes";
		// 			$('#commonModal').modal('show');
		// 			return;
		// 		} else {
		// 			this.actionName = "ConfirmDeleteGatewayBulk";
		// 			this.commonTitle = "Confirm Delete";
		// 			this.commonMessage = "Are you sure you want to delete these IMEIs from the list?";
		// 			this.commonModelOption = "Yes";
		// 			$('#commonModal').modal('show');
		// 			return;
		// 		}
		// 	}
		// } else {
		// 	this.actionName = "ConfirmDeleteGatewayBulk";
		// 	this.commonTitle = "Confirm Delete";
		// 	this.commonMessage = "Are you sure you want to delete these IMEIs from the list?";
		// 	this.commonModelOption = "Yes";
		// 	$('#commonModal').modal('show');
		// 	return;
		// }
		// if (this.updateFlag) {
		// 	console.log("updating");
		// 	//if (this.campaignService.campaignDetail.campaign_status !== "In progress") {
		// 	for (let device of this.deviceList) {
		// 		for (let selectedDevice of selectedData) {
		// 			if (device != null && device != undefined && device.imei != null && device.imei == selectedDevice.imei) {
		// 				console.log(device);
		// 				this.removeDevice(device);
		// 				break;
		// 				/*	if (selectedDevice.step_1 == undefined || selectedDevice.step_1 == "Pending") 
		// 					{
		// 						//this.deviceList.splice(this.deviceList.indexOf(device), 1);
		// 						this.removeDevice(device);
		// 						break;
		// 					} else {
		// 						//this.toastr.error("Can not remove the In progress or Completed gateway");
		// 						//this.toastr.error("Can not remove the In progress or Completed gateway", "", { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
		// 					}
		// 				*/
		// 			}
		// 		}
		// 	}
		// 	this.gridApiSecond.setRowData([]);
		// 	this.deviceList.forEach(function (data, index) {
		// 		data.step_order_number = index + 1;
		// 	});
		// 	this.gridApiSecond.setRowData(this.deviceList);
		// 	// } else {
		// 	//   this.toastr.error("You can not remove the gateway of In progress campaign");
		// 	// }
		// } else {
		// 	for (let device of this.deviceList) {
		// 		for (let selectedDevice of selectedData) {
		// 			if (device != null && device != undefined && device.imei != null && device.imei == selectedDevice.imei) {
		// 				//this.deviceList.splice(this.deviceList.indexOf(device), 1);
		// 				this.removeDevice(device);
		// 				break;
		// 			}
		// 		}
		// 	}
		// 	this.gridApiSecond.setRowData([]);
		// 	this.deviceList.forEach(function (data, index) {
		// 		data.step_order_number = index + 1;
		// 	});
		// 	this.gridApiSecond.setRowData(this.deviceList);
		// }
	};

	selectAll(){
		this.gridApiSecond.forEachNode(function (node) {
			// if (event.checked) {
			  node.setSelected(true);
			// } else {
			//   node.setSelected(false);
			// }
		  });
	}

	excludeSelectedDevice(): void {
		let selectedNodes = this.gridApiSecond.getSelectedNodes();
		let selectedData = selectedNodes.map(node => node.data);
		this.selectedDataForBulkExclude = selectedData;
		this.actionName = "ConfirmExcludeGatewayBulk";
		this.commonTitle = "Confirm Exclude";
		this.commonMessage = "Are you sure you want to exculde these IMEIs from the list?";
		this.commonModelOption = "Yes";
		$('#commonModal').modal('show');
	};

	removeDevice(device: any): void {
		this.deviceList = this.deviceList.filter(({ imei }) => imei !== device.imei);
	}

	onRowDragMove(event) {

	}

	onRowDragEnd(event) {
		if (event.node != undefined && event.node.id == "Baseline" || event.node.id == "End State") {
			return;
		}

		if (event.overNode != undefined && event.overNode.id == "Baseline" || event.overNode.id == "End State") {
			return;
		}
		this.changeDetection = true;
		var movingNode = event.node;
		var overNode = event.overNode;
		var rowNeedsToMove = movingNode !== overNode;
		if (rowNeedsToMove) {
			var movingData = movingNode.data;
			var overData = overNode.data;
			var fromIndex = this.sequenceData.indexOf(movingData);
			var toIndex = this.sequenceData.indexOf(overData);
			var newStore = this.sequenceData.slice();
			moveInArray(newStore, fromIndex, toIndex);
			this.sequenceData = newStore;
			this.gridApi.setRowData(newStore);
			this.gridApi.clearFocusedCell();
		}
		function moveInArray(arr, fromIndex, toIndex) {
			var element = arr[fromIndex];
			arr.splice(fromIndex, 1);
			arr.splice(toIndex, 0, element);
		}
		this.callChangeSequence();
	}

	callChangeSequence() {
		var initVar = 1;
		for (var i = 0; i < this.sequenceData.length; i++) {
			if (this.sequenceData[i].step_order_number != null && this.sequenceData[i].step_order_number != undefined && this.sequenceData[i].step_order_number == "Baseline" || this.sequenceData[i].step_order_number == "End State") {
				continue;
			} else {
				this.sequenceData[i].step_order_number = initVar;
				initVar++;
			}
		}
		// this.sequenceData.forEach(function (data, index) {
		//   data.step_order_number = index + 1;
		// });
		this.gridApi.setRowData(this.sequenceData);
	}

	refreshStatistics() {
		if (this.campaignStatus == 'In progress') {
			$("#refreshButton").addClass("fa-spin");
			$("#refreshButtonId").addClass("disabledRefresh");
			$("#refreshButtonId").prop('disabled', true);
			var uuid = this.campaignObj.uuid;
			//this.loading = true;
			this.campaignService.findByCampaignId(uuid).subscribe(data => {
				this.campaignObj = data.body;
				$("#refreshButton").removeClass("fa-spin");
				$("#refreshButtonId").removeClass("disabledRefresh");
				$("#refreshButtonId").prop('disabled', false);
				if (this.campaignObj.campaign_status != null && this.campaignObj.campaign_status !== "Not Started") {
					if (this.campaignObj.campaign_summary != undefined && this.campaignObj.campaign_summary != null) {
						this.campaign_summary = this.campaignObj.campaign_summary;
						// let not_started_percent = (this.campaign_summary.not_started * 100) / this.campaign_summary.total_gateways;
						// let in_progress_percent = (this.campaign_summary.in_progress * 100) / this.campaign_summary.total_gateways;
						// let completed_percent = (this.campaign_summary.completed * 100) / this.campaign_summary.total_gateways;
						// let on_hold_percent = (this.campaign_summary.on_hold * 100) / this.campaign_summary.total_gateways;
						//this.campaign_summary.not_eligible = 0;
						this.pieChartOptions.series[0].data[0].y = this.campaign_summary.not_started;
						this.pieChartOptions.series[0].data[1].y = this.campaign_summary.in_progress;
						this.pieChartOptions.series[0].data[2].y = this.campaign_summary.completed;
						this.pieChartOptions.series[0].data[3].y = this.campaign_summary.on_hold;
						this.pieChartOptions.series[0].data[4].y = this.campaign_summary.off_path;
						this.pieChartOptions.series[0].data[5].y = this.campaign_summary.on_path;
						this.hcs.createPieChart(this.chartEl.nativeElement, this.pieChartOptions);

					}
				}

				if (this.campaignObj.campaign_status != null && this.campaignObj.campaign_status !== "Not Started") {
					this.title = "Execute a Campaign";

					if (this.campaignObj.campaign_summary != undefined && this.campaignObj.campaign_summary != null) {
						this.campaign_summary = this.campaignObj.campaign_summary;
						// let not_started_percent = (this.campaign_summary.not_started * 100) / this.campaign_summary.total_gateways;
						// let in_progress_percent = (this.campaign_summary.in_progress * 100) / this.campaign_summary.total_gateways;
						// let completed_percent = (this.campaign_summary.completed * 100) / this.campaign_summary.total_gateways;
						// let on_hold_percent = (this.campaign_summary.on_hold * 100) / this.campaign_summary.total_gateways;
						//this.campaign_summary.not_eligible = 0;
						this.pieChartOptions.series[0].data[0].y = this.campaign_summary.not_started;
						this.pieChartOptions.series[0].data[1].y = this.campaign_summary.in_progress;
						this.pieChartOptions.series[0].data[2].y = this.campaign_summary.completed;
						this.pieChartOptions.series[0].data[3].y = this.campaign_summary.on_hold;
						this.pieChartOptions.series[0].data[4].y = this.campaign_summary.off_path;
						this.pieChartOptions.series[0].data[5].y = this.campaign_summary.on_path;
						this.hcs.createPieChart(this.chartEl.nativeElement, this.pieChartOptions);
					}

					this.data_view_for_steps = [];
					if (this.campaignObj.version_migration_detail && this.campaignObj.version_migration_detail.length > 0) {
						for (const iterator of this.campaignObj.version_migration_detail) {
							var step_data = {
								not_started: 0,
								in_progress: 0,
								completed: 0,
								on_hold: 0,
								step_name_for_display: iterator.from_package.package_name + ' to ' + iterator.to_package.package_name
							}
							if (this.campaignObj.step_dto && this.campaignObj.step_dto.length > 0) {
								for (const steps of this.campaignObj.step_dto) {
									if (iterator.from_package.package_name === steps.package_name) {
										switch (steps.status) {
											case 'SUCCESS':
												step_data.completed = steps.count;
												break;
											case 'PENDING':
												step_data.in_progress = steps.count;
												break;
											case 'FAILED':
												break;
											case 'HOLD':
												step_data.on_hold = steps.count;
												break;
											case 'Not Started':
												step_data.not_started = steps.count;
												break;
										}
									}
								}
							}
							this.data_view_for_steps.push(step_data);
						}
					}
					if (this.campaignObj.campaign_device_detail != undefined && this.campaignObj.campaign_device_detail != null && this.campaignObj.campaign_device_detail.length > 0) {
						// for (var i = 0; i < this.campaignObj.campaign_device_detail.length; i++) {
						// 	if (this.campaignObj.campaign_device_detail[i].device_step_status != undefined && this.campaignObj.campaign_device_detail[i].device_step_status != null && this.campaignObj.campaign_device_detail[i].device_step_status.length > 0) {
						// 		for (var j = 0; j < this.campaignObj.campaign_device_detail[i].device_step_status.length; j++) {

						// 			if (i === 0) {
						// 				if (this.campaignObj.campaign_device_detail[i].device_step_status[j].step_status == "In progress") {
						// 					var step_data = {
						// 						"step_name": "Step " + this.campaignObj.campaign_device_detail[i].device_step_status[j].step_order_number,
						// 						"step_order_number": this.campaignObj.campaign_device_detail[i].device_step_status[j].step_order_number,
						// 						"not_started": 0,
						// 						"in_progress": 1,
						// 						"completed": 0,
						// 						"on_hold": 0,
						// 						"step_name_for_display": ""
						// 					}
						// 					this.data_view_for_steps.push(JSON.parse(JSON.stringify(step_data)));
						// 				} else if (this.campaignObj.campaign_device_detail[i].device_step_status[j].step_status == "Pending") {
						// 					var step_data = {
						// 						"step_name": "Step " + this.campaignObj.campaign_device_detail[i].device_step_status[j].step_order_number,
						// 						"step_order_number": this.campaignObj.campaign_device_detail[i].device_step_status[j].step_order_number,
						// 						"not_started": 1,
						// 						"in_progress": 0,
						// 						"completed": 0,
						// 						"on_hold": 0,
						// 						"step_name_for_display": ""
						// 					}
						// 					this.data_view_for_steps.push(JSON.parse(JSON.stringify(step_data)));
						// 				} else if (this.campaignObj.campaign_device_detail[i].device_step_status[j].step_status == "On hold") {
						// 					var step_data = {
						// 						"step_name": "Step " + this.campaignObj.campaign_device_detail[i].device_step_status[j].step_order_number,
						// 						"step_order_number": this.campaignObj.campaign_device_detail[i].device_step_status[j].step_order_number,
						// 						"not_started": 0,
						// 						"in_progress": 0,
						// 						"completed": 0,
						// 						"on_hold": 1,
						// 						"step_name_for_display": ""
						// 					}
						// 					this.data_view_for_steps.push(JSON.parse(JSON.stringify(step_data)));
						// 				} else {
						// 					var step_data = {
						// 						"step_name": "Step " + this.campaignObj.campaign_device_detail[i].device_step_status[j].step_order_number,
						// 						"step_order_number": this.campaignObj.campaign_device_detail[i].device_step_status[j].step_order_number,
						// 						"not_started": 0,
						// 						"in_progress": 0,
						// 						"completed": 1,
						// 						"on_hold": 0,
						// 						"step_name_for_display": ""
						// 					}
						// 					this.data_view_for_steps.push(JSON.parse(JSON.stringify(step_data)));
						// 				}

						// 			} else {
						// 				if (this.campaignObj.campaign_device_detail[i].device_step_status[j].step_status == "In progress") {
						// 					this.data_view_for_steps[j].in_progress = this.data_view_for_steps[j].in_progress + 1;

						// 				} else if (this.campaignObj.campaign_device_detail[i].device_step_status[j].step_status == "Pending") {
						// 					this.data_view_for_steps[j].not_started = this.data_view_for_steps[j].not_started + 1;

						// 				} else if (this.campaignObj.campaign_device_detail[i].device_step_status[j].step_status == "On hold") {
						// 					this.data_view_for_steps[j].on_hold = this.data_view_for_steps[j].on_hold + 1;

						// 				} else {
						// 					this.data_view_for_steps[j].completed = this.data_view_for_steps[j].completed + 1;

						// 				}
						// 			}
						// 		}
						// 	}

						// }

						this.myOptions = {
							chart: {
								type: 'bar'
							},
							title: {
								text: 'Breakdown by Steps'
							},
							xAxis: {
								categories: ['Step 1', 'Step 2', 'Step 3']
							},
							yAxis: {
								min: 0,
								title: {
									text: ''
								}
							},
							legend: {
								reversed: true
							},
							plotOptions: {
								series: {
									stacking: 'normal'
								}
							},
							series: [{
								name: 'On Hold',
								data: [],
								color: '#EF4923'
							}, {
								name: 'Completed',
								data: [],
								color: '#518BCA'
							}, {
								name: 'In Progress',
								data: [],
								color: '#A2CD48'
							}, {
								name: 'Not Started',
								data: [],
								color: '#EFD31A'
							}]
						};
						if (this.data_view_for_steps != undefined && this.data_view_for_steps != null && this.data_view_for_steps.length > 0) {
							this.myOptions.xAxis.categories = [];
							for (var i = 0; i < this.data_view_for_steps.length; i++) {

								//this.data_view_for_steps[i].not_started = this.data_view_for_steps[i].not_started - this.campaign_summary.not_eligible;
								this.myOptions.xAxis.categories.push(this.data_view_for_steps[i].step_name);
								this.myOptions.series[0].data.push(this.data_view_for_steps[i].on_hold);
								this.myOptions.series[1].data.push(this.data_view_for_steps[i].completed);
								this.myOptions.series[2].data.push(this.data_view_for_steps[i].in_progress);
								this.myOptions.series[3].data.push(this.data_view_for_steps[i].not_started);

								// if (this.data_view_for_steps[i].step_order_number != null && this.data_view_for_steps[i].step_order_number != undefined) {
								// 	if (this.campaignObj.version_migration_detail != null && this.campaignObj.version_migration_detail != undefined && this.campaignObj.version_migration_detail.length > 0) {
								// 		for (var ivf = 0; ivf < this.campaignObj.version_migration_detail.length; ivf++) {
								// 			if (this.data_view_for_steps[i].step_order_number == this.campaignObj.version_migration_detail[ivf].step_order_number) {
								// 				this.data_view_for_steps[i].step_name_for_display = this.campaignObj.version_migration_detail[ivf].from_package.package_name + " to " + this.campaignObj.version_migration_detail[ivf].to_package.package_name;
								// 			}
								// 		}
								// 	}
								// }
							}
							//this.hcs.destroy(this.stackbarChartEl.nativeElement, this.myOptions);
							this.hcs.createStackedbarChart(this.stackbarChartEl.nativeElement, this.myOptions);
						}

					}
				}
				//this.loading = false;
			}, error => {
				$("#refreshButton").removeClass("fa-spin");
				$("#refreshButtonId").removeClass("disabledRefresh");
				$("#refreshButtonId").prop('disabled', false);
				//this.loading = false;
			});

		}
	}

	addPackage() {
		var sequenceObj = {
			package: {
				"package_name": null
			},

		}
		//this.sequenceData.insert(this.sequenceData.length -2 , sequenceObj);
		this.sequenceData.splice((this.sequenceData.length - 1), 0, sequenceObj);
		//this.sequenceData.push(sequenceObj);
		this.callChangeSequence();
		this.changeDetection = true;
		this.isDirty = true;
	}
	tabSection: string = '';

	selectTabEvent(event) {
		if (event != undefined && event.tab.textLabel == "Gateways") {
			this.tabSection = event.tab.textLabel;
			//  if (this.updateFlag) {
			console.log("devicelist in gatway tab ", this.deviceList);
			this.gridApiSecond.setColumnDefs(this.columnDefsSecond);
			this.gridApiSecond.setRowData([]);
			this.gridApiSecond.setRowData(this.deviceList);
			this.imei_list = [];
			for (let i = 0; i < this.deviceList.length; i++) {
				this.imei_list.push(this.deviceList[i].imei);
				//   }
			}
			if (this.updateFlag) {
				this.onGridReadySecond(this.paramList);
			}
		}
	}

	onGridReady(params) {
		this.gridApi = params.api;
		this.gridColumnApi = params.columnApi;

		this.callChangeSequence();
	}

	// onGridReadySecond(params) {
	// 	this.gridApiSecond = params.api;
	// 	this.gridColumnApiSecond = params.columnApi;
	// 	this.gridApiSecond.setRowData([]);

	// }
	refreshDelete = false;
	onGridReadySecond(params) {
		this.gridApiSecond = params.api;
		this.gridColumnApiSecond = params.columnApi;
		this.gridOptions.api.setDomLayout('autoHeight');
		this.paramList = params;
		// let gridSavedState = sessionStorage.getItem("gateway_list_for_add_campaign_grid_column_state");
		// if (gridSavedState) {
		//   console.log(gridSavedState);
		//   console.log("column state found in session storage");
		//   this.gridColumnApiSecond.setColumnState(JSON.parse(gridSavedState));
		//   console.log("priting column state after setting");
		//   console.log(this.gridColumnApiSecond.getColumnState());
		// }
		let filterSavedState = sessionStorage.getItem("gateway_list_for_add_campaign_grid_filter_state");
		if (filterSavedState) {
			console.log(filterSavedState);
			console.log("filter state found in session storage");
			this.gridApiSecond.setFilterModel(JSON.parse(filterSavedState));
			console.log("priting filter state after setting");
			console.log(this.gridApiSecond.getFilterModel());
		} else {
			console.log("Filter state not found in session storage");
		}

		var datasource = {
			getRows: (params: IGetRowsParams) => {
				this.sortForAgGrid(params.sortModel);
				this.filterForAgGrid(params.filterModel);
				if (this.updateFlag && this.tabSection == "Gateways") {
					this.loading = true;
					if (this.refreshDelete) {
						this.refreshDelete = false;
						this.loading = false;
						params.successCallback(this.rowDataSecond, this.rowDataSecond.length);
					} else {
						this.campaignService.findDeviceCampaignStatusPageByCampaignId(this.gridApiSecond.paginationGetCurrentPage() + 1, this.column, this.order, this.filterValuesJsonObj, this.pageLimit, this.campaignService.campaignDetail.uuid).subscribe(data => {
							data.body.forEach(function (data1, index) {
								data1.step_order_number = index + 1;
								if (data1.device_step_status != undefined && data1.device_step_status != null && data1.device_step_status.length > 0) {
									for (var i = 0; i < data1.device_step_status.length; i++) {
										data1["device_step_status" + data1.device_step_status[i].step_order_number] = data1.device_step_status[i].step_status;
									}
								}
							});

							this.rowDataSecond = data.body;
							this.loading = false;
							params.successCallback(data.body, data.total_key);
							this.total_number_of_records = data.total_key;
							if (this.isFirstTimeLoad == true) {
								let paginationSavedPage = sessionStorage.getItem("gateway_list_for_add_campaign_grid_saved_page");
								if (paginationSavedPage && this.isFirstTimeLoad) {
									this.gridOptions.api.paginationGoToPage(Number.parseInt(paginationSavedPage));
									this.isFirstTimeLoad = false;
									this.isPaginationStateRecovered = true;
								}
							}
						}, error => {
							console.log(error)
							this.loading = false;

						});
					}
				}
			}
		}
		this.gridApiSecond.setDatasource(datasource);
	}

	getRowNodeId(data) {
		return data.step_order_number;
	}

	get f() { return this.addCampaign.controls; }

	initializeForm() {
		this.addCampaign = this._formBuldiers.group({
			campaign_name: ['', Validators.compose([Validators.required])],
			// grouping_uuid: ['', Validators.compose([Validators.required])],
			execution_status: [''],
			isCustomer: [true],
			description: [''],
			is_pause_execution: [true],
			is_imei_for_customer: [true],
			customer_name: [{ value: '', disabled: false }],
			no_of_imeis: [{ value: 5, disabled: false }],
			file: [''],
			exclude_not_installed: [true],
			exclude_low_battery: [true],
			exclude_rma: [true],
			exclude_eol: [true],
			excluded_imeis: [],
			exclude_engineering: [true],
			type: [],
			customer_name2: [{ value: '', disabled: false }],
			init_status: [''],


		});

		this.originalFormValue = this.addCampaign.value;

		this.frameworkComponents = {
			btnCellRenderer: CampaignHyperlinkRenderer
		};

		this.addCampaign.get('is_pause_execution').valueChanges
			.subscribe(is_pause_execution => {
				if (!is_pause_execution) {
					this.addCampaign.get('no_of_imeis').reset();
					this.addCampaign.get('no_of_imeis').disable();
				}
				else {
					this.addCampaign.get('no_of_imeis').enable();
				}
			});

		this.addCampaign.get('campaign_name').valueChanges
			.subscribe(campaign_name => {
				var campaignNameTrimed = null;
				if (campaign_name != undefined && campaign_name != null && campaign_name != "") {
					//campaignNameTrimed = campaign_name.trim();
					campaignNameTrimed = campaign_name.replace(/ {2,}/g, ' ').trim();
				} else {
					//this.loading = false;
				}

				if (campaignNameTrimed == undefined || campaignNameTrimed == null || campaignNameTrimed == "") {
					this.addCampaign.patchValue({ 'campaign_name': "" });
					this.addCampaign.get('campaign_name').setValidators([Validators.required]);
					this.addCampaign.get('campaign_name').updateValueAndValidity();
				}
				if (campaignNameTrimed != undefined && campaignNameTrimed != null && campaignNameTrimed != "" && campaignNameTrimed.length > 2) {
					if (this.updateFlag && campaignNameTrimed === this.campaignService.campaignDetail.campaign_name) {
						//this.loading = false;
						return;
					}
					this.campaignService.findByCampaignName(campaignNameTrimed)
						.subscribe(data => {
							if (data.status) {
								this.toastr.error("The current campaign name is already used by another campaign You must choose a unique name for this campaign.", "Campaign Name Must Be Unique", { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
								this.addCampaign.get('campaign_name').setErrors({ duplicate: true });
							}
							//this.loading = false;
						}, error => {
							//this.loading = false;
						});
				} else {
					//this.loading = false;
				}
			});

		this.addCampaign.get('is_imei_for_customer').valueChanges
			.subscribe(is_imei_for_customer => {
				if (!is_imei_for_customer) {
					this.addCampaign.get('customer_name').reset();
					this.addCampaign.get('customer_name').disable();
					// this.deviceList = [];
					// this.gridApiSecond.setRowData([]);
				} else {
					this.addCampaign.get('customer_name').enable();
					// this.deviceList = [];
					// this.gridApiSecond.setRowData([]);
				}
			});


		if (this.updateFlag) {
			this.setFormControlValues();
		}
	}

	duplicateCampaignName(controlName: string) {
		return (formGroup: FormGroup) => {
			console.log("============= controlName =================" + controlName);
			//this.loading = true;
			const control = formGroup.controls[controlName];
			var campaignNameTrimed = null;
			if (control != undefined && control != null && control.value != undefined && control.value != null && control.value != "") {
				//campaignNameTrimed = control.value.trim();
				campaignNameTrimed = control.value.replace(/ {2,}/g, ' ').trim();
			} else {
				//this.loading = false;
			}
			if (campaignNameTrimed != undefined && campaignNameTrimed != null && campaignNameTrimed != "" && campaignNameTrimed.length > 2) {
				if (this.updateFlag && campaignNameTrimed === this.campaignService.campaignDetail.campaign_name) {
					//this.loading = false;
					return;
				}
				this.campaignService.findByCampaignName(campaignNameTrimed)
					.subscribe(data => {
						if (data.status) {
							control.setErrors({ duplicate: true });
						}
						//this.loading = false;
					}, error => {
						//this.loading = false;
					});
			} else {
				//this.loading = false;
			}
		}
	}



	checkForDeviceList() {
		if (this.deviceList != undefined && this.deviceList != null && this.deviceList.length > 0) {
			return true;
		} else {
			return false;
		}
	}

	replaceImeiList() {
		this.isReplace = true;
		if (this.actionName == 'AgainUploadFile') {
			this.deviceList = [];
			this.gridApiSecond.setRowData([]);
			this.addCampaign.patchValue({ 'file': null });
			this.selectedFileName = null;
			$('#modalForAppendOrReplace').modal('hide');
			$('#uploadFileModal').modal('show');
		} else if (this.actionName == 'changeUploadToCustomer') {
			this.deviceList = [];
			this.gridApiSecond.setRowData([]);
			$('#modalForAppendOrReplace').modal('hide');
		}
	}

	appendImeiList() {
		this.isReplace = false;
		if (this.actionName == 'AgainUploadFile') {
			this.deviceList = [];
			this.gridApiSecond.setRowData([]);
			this.addCampaign.patchValue({ 'file': null });
			this.selectedFileName = null;
			$('#modalForAppendOrReplace').modal('hide');
			$('#uploadFileModal').modal('show');
		} else if (this.actionName == 'changeUploadToCustomer') {
			this.deviceList = [];
			this.gridApiSecond.setRowData([]);
			$('#modalForAppendOrReplace').modal('hide');
		}

	}


	radioChange(event, type) {

		this.deviceList = [];
		this.gridApiSecond.setRowData([]);
		this.addCampaign.patchValue({ 'customer_name': null });

		if (type === 'DEFINED_LIST') {
			this.isUploadList = true;
			this.campaigntype = type;
			this.customername = 'IMEI';
			this.type = type;
			this.isRestrictedScope = false;

		} else if (type === 'UNRESTRICTED') {

			if (this.sequenceData[0].package == undefined || this.sequenceData[0].package == null || this.sequenceData[0].package.uuid == undefined || this.sequenceData[0].package.uuid == null) {
				this.toastr.error("Please select a package to establish the baseline device configuration required for this campaign.", "Baseline Configuration Must Be Specified", { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
				this.addCampaign.patchValue({ 'customer_name': null });
				this.loading = false;
				return;
			}


			this.type = type;
			this.isUploadList = false;
			this.isRestrictedScope = false;

			this.campaigntype = type;
			this.customername = 'ALL';
			if (this.updateFlag == false) {
				// console.log("added value");
				// this.loading = true;
				// this.campaignService.findDataByCustomerPage(this.gridApiSecond.paginationGetCurrentPage() + 1, this.column, this.order, this.filterValuesJsonObj, this.pageLimit,"ALL", this.sequenceData[0].package.uuid, null).subscribe(data => {
				//   this.loading = false;
				//   console.log("Data is pri");
				//   console.log(data);
				//   this.deviceList = [];
				//   this.deviceList = data.content;
				//   if (this.deviceList != null && this.deviceList.length > 0) {
				// 	this.imei_list = [];
				// 	for (let i = 0; i < this.deviceList.length; i++) {
				// 		this.imei_list.push(this.deviceList[i].imei);
				// 	}
				// }

				//   this.gridApiSecond.setRowData([]);
				//   this.gridApiSecond.setRowData(this.deviceList);
				// });
				this.selectedCustomerId = this.customername;
				this.onGridReadySecond(this.paramList);
			}


		} else if (type === 'RESTRICTED') {
			this.isUploadList = false;
			this.campaigntype = type;
			//this.customername = "";
			this.isRestrictedScope = true;

			this.type = type;
			if (!this.updateFlag) {
				this.addCampaign.get('customer_name').enable();
			}

		}

		console.log(" radioChange this.customername>> " + this.customername);


		if (event) {
			if (this.campaignStatus == "In progress") {
				this.isCustomerSelected = true;
				this.tempFlag = true;
				this.toastr.error("The customer cannot be changed because this campaign is currently in progress.", "Customer Cannot be Changed", { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
				//this.addCampaign.patchValue({ 'is_imei_for_customer': false });
				return;
			} else if ((this.updateFlag && this.campaignStatus == "Not Started") || !this.updateFlag) {
				if (this.checkForDeviceList()) {
					this.actionName = "changeUploadToCustomer";
					$('#modalForAppendOrReplace').modal('show');
				}
			}
		}

		this.changeDetection = true;
		this.isDirty = true;
		// else if(!event){
		//       this.isCustomerSelected=false;
		//       this.addCampaign.get('customer_name').reset();
		//       this.addCampaign.get('customer_name').patchValue("");
		//       this.addCampaign.get('customer_name').disable();
		//   }

		//  else if ( this.addCampaign.controls.is_imei_for_customer.value && !event) {
		//   console.log("hello")
		//   this.stateOfCustomerOrUpdate = this.addCampaign.controls.is_imei_for_customer.value;
		//   if (this.campaignStatus == "In progress") {
		//     this.toastr.error("The customer cannot be changed because this campaign is currently in progress.", "Customer Cannot be Changed", { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
		//     this.addCampaign.patchValue({ 'is_imei_for_customer': this.stateOfCustomerOrUpdate });
		//     return;
		//   } else if ((this.updateFlag && this.campaignStatus == "Not Started") || !this.updateFlag) {
		//     if (this.checkForDeviceList()) {
		//       this.actionName = "changeCustomerToUpload";
		//       this.commonTitle = "Confirm Change";
		//       this.commonMessage = "Are you sure you want to remove the current gateways from this campaign?";
		//       this.commonModelOption = "Yes";
		//       $('#commonModal').modal('show');
		//     }
		//   }
		// }

		if (event.value != undefined && event.value) {
			console.log("inside this");
			this.addCampaign.get('customer_name').enable();
		} else if (event.value != undefined) {
			this.addCampaign.get('customer_name').reset();
			this.addCampaign.get('customer_name').disable();
		}
	}
	getAllcompanies() {
		this.gatewayListService.getAllCustomer(['CUSTOMER']).subscribe(
			data => {
				this.companiesName = data && data.length > 0 ? data : [];
			},
			error => {
				console.log(error);
			}
		);
	}
	// getActiveCompanies() {
	// 	this.loading = true;
	// 	this.companiesService.getAllCompanyList().subscribe(data => {
	// 		this.companies = data.body.content;
	// 		this.loading = false;
	// 	}, error => {
	// 		console.log(error);
	// 	})
	// }
	getActiveCompanies() {
		this.loading = true;
		this.campaignService.getActiveCompanies(1, 1000, "Customer").subscribe(data => {
			console.log(data.body);
			this.companies = data.body;
			this.companies2 = data.body;
			console.log(this.companies);

			this.loading = false;
		}, error => {
			console.log(error);
		})
	}

	getCustomerCompaniesList() {
		this.companyService.getAllCompany(['CUSTOMER'], true).subscribe(
			data => {
				this.companies = [];
				this.companies = data;


				this.loading = false;
			},
			error => {
				this.toastr.error(error, null, { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
				this.loading = false;
			});
	}

	clickOnCampaign(uuid) {
		alert(uuid);
	}
	setFormControlValues() {
		if (this.updateFlag) {
			console.log("setFormControlValues > " + this.campaignService.campaignDetail.customer_name);
			/*this.columnDefsSecond.push({
			  headerName: 'Comments', field: 'comments', minWidth: 400, filter: 'agTextColumnFilter', cellRenderer: "btnCellRenderer"
			});*/
			this.title = "Execute a Campaign";

			this.campaignObj = this.campaignService.campaignDetail;


			this.campaignObjCopy = JSON.parse(JSON.stringify(this.campaignService.campaignDetail));
			this.addCampaign.patchValue({ 'campaign_name': this.campaignService.campaignDetail.campaign_name });
			//this.addCampaign.patchValue({ 'grouping_uuid': this.campaignService.campaignDetail.grouping_uuid });
			this.addCampaign.patchValue({ 'description': this.campaignService.campaignDetail.description });
			this.addCampaign.patchValue({ 'customer_name': this.campaignService.campaignDetail.customer_name });
			this.addCampaign.patchValue({ 'is_imei_for_customer': this.campaignService.campaignDetail.is_imei_for_customer });
			this.addCampaign.patchValue({ 'is_pause_execution': this.campaignService.campaignDetail.is_pause_execution });
			this.addCampaign.patchValue({ 'no_of_imeis': this.campaignService.campaignDetail.no_of_imeis });
			this.addCampaign.patchValue({ 'exclude_not_installed': this.campaignService.campaignDetail.exclude_not_installed });
			this.addCampaign.patchValue({ 'exclude_low_battery': this.campaignService.campaignDetail.exclude_low_battery });

			this.addCampaign.patchValue({ 'exclude_rma': this.campaignService.campaignDetail.exclude_rma });
			this.addCampaign.patchValue({ 'exclude_eol': this.campaignService.campaignDetail.exclude_eol });
			this.addCampaign.patchValue({ 'exclude_engineering': this.campaignService.campaignDetail.exclude_engineering });
			this.addCampaign.patchValue({ 'init_status': this.campaignService.campaignDetail.init_status });


			console.log("campaign type " + this.campaignService.campaignDetail.campaign_type);
			if (this.campaignService.campaignDetail.campaign_type == 'UNRESTRICTED') {
				this.type = 'UNRESTRICTED';
			} else if (this.campaignService.campaignDetail.is_imei_for_customer === true || this.campaignService.campaignDetail.campaign_type == 'RESTRICTED') {
				this.type = 'RESTRICTED';
			} else if (this.campaignService.campaignDetail.campaign_type == 'DEFINED_LIST' || this.campaignService.campaignDetail.is_imei_for_customer === false || this.campaignService.campaignDetail.campaign_type == null) {
				this.isUploadList = true;
				this.type = 'DEFINED_LIST';
			}

			if (this.updateFlag && (this.type == 'UNRESTRICTED' || this.type == 'RESTRICTED')) {
				this.definedListClick = false;
			}
			console.log("type<<<<<<<<<<<>>> " + this.type);


			if (!this.isCustomerSelected) {
				console.log("isCustomerSelected : " + this.isCustomerSelected);
				//this.addCampaign.get('customer_name').reset();
				//this.addCampaign.get('customer_name').patchValue("");
				this.addCampaign.get('customer_name').disable();
			}

			this.campaignStatus = this.campaignObj.campaign_status;
			if (this.changeDetector != undefined) {
				this.changeDetector.detectChanges();
			}

			if (this.campaignObj.campaign_status != null && this.campaignObj.campaign_status !== "Not Started") {
				if (this.campaignObj.campaign_summary != undefined && this.campaignObj.campaign_summary != null) {
					this.campaign_summary = this.campaignObj.campaign_summary;
					// let not_started_percent = (this.campaign_summary.not_started * 100) / this.campaign_summary.total_gateways;
					// let in_progress_percent = (this.campaign_summary.in_progress * 100) / this.campaign_summary.total_gateways;
					// let completed_percent = (this.campaign_summary.completed * 100) / this.campaign_summary.total_gateways;
					// let on_hold_percent = (this.campaign_summary.on_hold * 100) / this.campaign_summary.total_gateways;
					//this.campaign_summary.not_eligible = 0;
					this.pieChartOptions.series[0].data[0].y = this.campaign_summary.not_started;
					this.pieChartOptions.series[0].data[1].y = this.campaign_summary.in_progress;
					this.pieChartOptions.series[0].data[2].y = this.campaign_summary.completed;
					this.pieChartOptions.series[0].data[3].y = this.campaign_summary.on_hold;
					this.pieChartOptions.series[0].data[4].y = this.campaign_summary.off_path;
					this.pieChartOptions.series[0].data[5].y = this.campaign_summary.on_path;
					this.hcs.createPieChart(this.chartEl.nativeElement, this.pieChartOptions);

				}

			}
			this.deviceList = [];
			this.data_view_for_steps = [];
			// if (this.campaignObj.campaign_device_detail != undefined && this.campaignObj.campaign_device_detail != null && this.campaignObj.campaign_device_detail.length > 0) {
			// 	for (var i = 0; i < this.campaignObj.campaign_device_detail.length; i++) {
			// 		var device_data = {
			// 			"step_order_number": (i + 1),
			// 			"imei": this.campaignObj.campaign_device_detail[i].imei,
			// 			"device_MODEL": this.campaignObj.campaign_device_detail[i].product_name,
			// 			"organisation_name": this.campaignObj.campaign_device_detail[i].customer_name,
			// 			"device_status_for_campaign": this.campaignObj.campaign_device_detail[i].device_status_for_campaign,
			// 			"last_report": this.campaignObj.campaign_device_detail[i].last_report,
			// 			"comments": this.campaignObj.campaign_device_detail[i].comments,
			// 			"installed_flag": this.campaignObj.campaign_device_detail[i].installed_flag,
			// 			"campaign_hyper_link": this.campaignObj.campaign_device_detail[i].campaign_hyper_link,
			// 			"device_report": this.campaignObj.campaign_device_detail[i].device_report,
			// 			"device_campaign_history": this.campaignObj.campaign_device_detail[i].device_campaign_history,
			// 			"campaign_installed_device": ""

			// 		}
			// 		if (this.gatewaysList[i].device_step_status != undefined && this.gatewaysList[i].device_step_status != null && this.gatewaysList[i].device_step_status.length > 0) {
			// 			for (var j = 0; j < this.gatewaysList[i].device_step_status.length; j++) {
			// 				device_data["step_" + this.gatewaysList[i].device_step_status[j].step_order_number] = this.gatewaysList[i].device_step_status[j].step_status;
			// 				if (i == 0) {
			// 					this.columnDefsSecond.push({ field: "step_" + this.gatewaysList[i].device_step_status[j].step_order_number, headerName: "Step " + this.gatewaysList[i].device_step_status[j].step_order_number + " (Time in UTC)", filter: 'agTextColumnFilter', minWidth: 200 });

			// 				}
			// 			}
			// 		}
			// 		this.deviceList.push(JSON.parse(JSON.stringify(device_data)));



			// 	}
			// }

			if (this.campaignObj.version_migration_detail != undefined && this.campaignObj.version_migration_detail != null && this.campaignObj.version_migration_detail.length > 0) {
				for (var j = 0; j < this.campaignObj.version_migration_detail.length; j++) {
					this.columnDefsSecond.push({ field: "device_step_status" + this.campaignObj.version_migration_detail[j].step_order_number, headerName: "Step " + this.campaignObj.version_migration_detail[j].step_order_number + " (Time in UTC)", filter: false, minWidth: 200 });

				}
			}

			if (this.campaignObj.campaign_status != null && this.campaignObj.campaign_status !== "Not Started") {
				if (this.campaignObj.campaign_status == "In progress") {
					this.title = "Execute a Campaign";
					var tabEvent = {
						"tab": {
							"textLabel": "Stats"
						},
					}
					this.selectTabEvent(tabEvent);
				}
				if (this.campaignObj.campaign_summary != undefined && this.campaignObj.campaign_summary != null) {
					this.campaign_summary = this.campaignObj.campaign_summary;
					// let not_started_percent = (this.campaign_summary.not_started * 100) / this.campaign_summary.total_gateways;
					// let in_progress_percent = (this.campaign_summary.in_progress * 100) / this.campaign_summary.total_gateways;
					// let completed_percent = (this.campaign_summary.completed * 100) / this.campaign_summary.total_gateways;
					// let on_hold_percent = (this.campaign_summary.on_hold * 100) / this.campaign_summary.total_gateways;
					//this.campaign_summary.not_eligible = 0;
					this.pieChartOptions.series[0].data[0].y = this.campaign_summary.not_started;
					this.pieChartOptions.series[0].data[1].y = this.campaign_summary.in_progress;
					this.pieChartOptions.series[0].data[2].y = this.campaign_summary.completed;
					this.pieChartOptions.series[0].data[3].y = this.campaign_summary.on_hold;
					this.pieChartOptions.series[0].data[4].y = this.campaign_summary.off_path;
					this.pieChartOptions.series[0].data[5].y = this.campaign_summary.on_path;
					this.hcs.createPieChart(this.chartEl.nativeElement, this.pieChartOptions);

				}

				if (this.campaignObj.version_migration_detail && this.campaignObj.version_migration_detail.length > 0) {
					for (const iterator of this.campaignObj.version_migration_detail) {
						var step_data = {
							not_started: 0,
							in_progress: 0,
							completed: 0,
							on_hold: 0,
							step_name_for_display: iterator.from_package.package_name + ' to ' + iterator.to_package.package_name
						}
						if (this.campaignObj.step_dto && this.campaignObj.step_dto.length > 0) {
							for (const steps of this.campaignObj.step_dto) {
								if (iterator.from_package.package_name === steps.package_name) {
									switch (steps.status) {
										case 'SUCCESS':
											step_data.completed = steps.count;
											break;
										case 'PENDING':
											step_data.in_progress = steps.count;
											break;
										case 'FAILED':
											break;
										case 'HOLD':
											step_data.on_hold = steps.count;
											break;
										case 'Not Started':
											step_data.not_started = steps.count;
											break;
									}
								}
							}
						}
						this.data_view_for_steps.push(step_data);
					}
				}
				if (this.campaignObj.campaign_device_detail != undefined && this.campaignObj.campaign_device_detail != null && this.campaignObj.campaign_device_detail.length > 0) {
					// for (var i = 0; i < this.campaignObj.campaign_device_detail.length; i++) {
					// 	if (this.campaignObj.campaign_device_detail[i].device_step_status != undefined && this.campaignObj.campaign_device_detail[i].device_step_status != null && this.campaignObj.campaign_device_detail[i].device_step_status.length > 0) {
					// 		for (var j = 0; j < this.campaignObj.campaign_device_detail[i].device_step_status.length; j++) {

					// 			if (i === 0) {
					// 				if (this.campaignObj.campaign_device_detail[i].device_step_status[j].step_status == "In progress") {
					// 					var step_data = {
					// 						"step_name": "Step " + this.campaignObj.campaign_device_detail[i].device_step_status[j].step_order_number,
					// 						"step_order_number": this.campaignObj.campaign_device_detail[i].device_step_status[j].step_order_number,
					// 						"not_started": 0,
					// 						"in_progress": 1,
					// 						"completed": 0,
					// 						"on_hold": 0,
					// 						"step_name_for_display": ""
					// 					}
					// 					this.data_view_for_steps.push(JSON.parse(JSON.stringify(step_data)));
					// 				} else if (this.campaignObj.campaign_device_detail[i].device_step_status[j].step_status == "Pending") {
					// 					var step_data = {
					// 						"step_name": "Step " + this.campaignObj.campaign_device_detail[i].device_step_status[j].step_order_number,
					// 						"step_order_number": this.campaignObj.campaign_device_detail[i].device_step_status[j].step_order_number,
					// 						"not_started": 1,
					// 						"in_progress": 0,
					// 						"completed": 0,
					// 						"on_hold": 0,
					// 						"step_name_for_display": ""
					// 					}
					// 					this.data_view_for_steps.push(JSON.parse(JSON.stringify(step_data)));
					// 				} else if (this.campaignObj.campaign_device_detail[i].device_step_status[j].step_status == "On hold") {
					// 					var step_data = {
					// 						"step_name": "Step " + this.campaignObj.campaign_device_detail[i].device_step_status[j].step_order_number,
					// 						"step_order_number": this.campaignObj.campaign_device_detail[i].device_step_status[j].step_order_number,
					// 						"not_started": 0,
					// 						"in_progress": 0,
					// 						"completed": 0,
					// 						"on_hold": 1,
					// 						"step_name_for_display": ""
					// 					}
					// 					this.data_view_for_steps.push(JSON.parse(JSON.stringify(step_data)));
					// 				} else {
					// 					var step_data = {
					// 						"step_name": "Step " + this.campaignObj.campaign_device_detail[i].device_step_status[j].step_order_number,
					// 						"step_order_number": this.campaignObj.campaign_device_detail[i].device_step_status[j].step_order_number,
					// 						"not_started": 0,
					// 						"in_progress": 0,
					// 						"completed": 1,
					// 						"on_hold": 0,
					// 						"step_name_for_display": ""
					// 					}
					// 					this.data_view_for_steps.push(JSON.parse(JSON.stringify(step_data)));
					// 				}

					// 			} else {
					// 				if (this.campaignObj.campaign_device_detail[i].device_step_status[j].step_status == "In progress") {
					// 					this.data_view_for_steps[j].in_progress = this.data_view_for_steps[j].in_progress + 1;

					// 				} else if (this.campaignObj.campaign_device_detail[i].device_step_status[j].step_status == "Pending") {
					// 					this.data_view_for_steps[j].not_started = this.data_view_for_steps[j].not_started + 1;

					// 				} else if (this.campaignObj.campaign_device_detail[i].device_step_status[j].step_status == "On hold") {
					// 					this.data_view_for_steps[j].on_hold = this.data_view_for_steps[j].on_hold + 1;

					// 				} else {
					// 					this.data_view_for_steps[j].completed = this.data_view_for_steps[j].completed + 1;

					// 				}
					// 			}
					// 		}
					// 	}

					// }
					if (this.data_view_for_steps != undefined && this.data_view_for_steps != null && this.data_view_for_steps.length > 0) {
						this.myOptions.xAxis.categories = [];
						for (var i = 0; i < this.data_view_for_steps.length; i++) {

							// this.data_view_for_steps[i].not_started = this.data_view_for_steps[i].not_started - this.campaign_summary.not_eligible;
							this.myOptions.xAxis.categories.push(this.data_view_for_steps[i].step_name);
							this.myOptions.series[0].data.push(this.data_view_for_steps[i].on_hold);
							this.myOptions.series[1].data.push(this.data_view_for_steps[i].completed);
							this.myOptions.series[2].data.push(this.data_view_for_steps[i].in_progress);
							this.myOptions.series[3].data.push(this.data_view_for_steps[i].not_started);

							// if (this.data_view_for_steps[i].step_order_number != null && this.data_view_for_steps[i].step_order_number != undefined) {
							// 	if (this.campaignObj.version_migration_detail != null && this.campaignObj.version_migration_detail != undefined && this.campaignObj.version_migration_detail.length > 0) {
							// 		for (var ivf = 0; ivf < this.campaignObj.version_migration_detail.length; ivf++) {
							// 			if (this.data_view_for_steps[i].step_order_number == this.campaignObj.version_migration_detail[ivf].step_order_number) {
							// 				this.data_view_for_steps[i].step_name_for_display = this.campaignObj.version_migration_detail[ivf].from_package.package_name + " to " + this.campaignObj.version_migration_detail[ivf].to_package.package_name;
							// 			}
							// 		}
							// 	}
							// }
						}
						this.hcs.createStackedbarChart(this.stackbarChartEl.nativeElement, this.myOptions);
					}

				}


			}

			if (this.campaignService.campaignDetail.is_imei_for_customer) {
				this.selectedCustomerId = this.campaignService.campaignDetail.customer_name;
				//this.getDataByImeiListOrCustomerId("Customer");
			} else {

				if (this.campaignService.campaignDetail.imei_list != null && this.campaignService.campaignDetail.imei_list != undefined && this.campaignService.campaignDetail.imei_list != "") {
					this.imei_list = this.campaignService.campaignDetail.imei_list.split(',');
					//this.getDataByImeiListOrCustomerId("ImeiList");
				}

			}
			var version_migration_detail_list = [];
			if (this.campaignObj.version_migration_detail != null && this.campaignObj.version_migration_detail.length > 0) {
				for (var i = 0; i < this.campaignObj.version_migration_detail.length; i++) {
					var version_migration_detail_obj = {
						"at_command": this.campaignObj.version_migration_detail[i].at_command,
						"step_order_number": this.campaignObj.version_migration_detail[i].step_order_number,
						"step_id": this.campaignObj.version_migration_detail[i].step_id,
						"package": this.campaignObj.version_migration_detail[i].from_package,
						"tooltip": ""
					}

					if (i == 0) {
						version_migration_detail_obj.step_order_number = "Baseline";
						version_migration_detail_obj.tooltip = "The Package You set as a baseline will limit the campaign to only those gateways in the selected list that currently have this configuration."
					}

					version_migration_detail_list.push(version_migration_detail_obj);

					if (i == this.campaignObj.version_migration_detail.length - 1) {
						var version_migration_detail_obj_new = {
							"at_command": "",
							"step_order_number": "End State",
							"package": this.campaignObj.version_migration_detail[i].to_package,
							"tooltip": "The end state defines the expected configuration of devices after this campaign has been completed."
						}

						version_migration_detail_list.push(version_migration_detail_obj_new);
					}
				}

				this.sequenceData = version_migration_detail_list;
			}
			console.log("type>>> " + this.type);
			this.addCampaign.patchValue({ 'type': this.type });
			this.isRestrictedScope = true;

		}
		if (this.type !== 'RESTRICTED') {
			this.addCampaign.get('customer_name').disable();
		}
	}



	pauseCampaignDB() {
		if (!this.addCampaign.valid) {
			this.markFormGroupTouched(this.addCampaign);
			return;
		}

		if (this.addCampaign.controls.is_imei_for_customer.value == null || this.addCampaign.controls.is_imei_for_customer.value == undefined) {
			//this.toastr.error("Please select one of the option in the IMEIs tab section");
			this.toastr.error("Please select one of the option in the IMEIs tab section", "", { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
			return;
		}
		if (!this.updateFlag && !this.addCampaign.controls.is_imei_for_customer.value) {
			if (this.data.length < 1) {
				//this.toastr.error("Please select a file");
				this.toastr.error("Please select a file", "", { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
				return;
			}
		}

		if (this.addCampaign.controls.is_imei_for_customer.value) {
			if (this.addCampaign.controls.customer_name.value == undefined || this.addCampaign.controls.customer_name.value == null || this.addCampaign.controls.customer_name.value == "") {
				//this.toastr.error("Please select a customer");
				this.toastr.error("Please select a customer", "", { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
				return;
			}
		}

		if (this.addCampaign.controls.is_pause_execution.value) {
			if (this.addCampaign.controls.no_of_imeis.value == null || this.addCampaign.controls.no_of_imeis.value == undefined || this.addCampaign.controls.no_of_imeis.value == "" || this.addCampaign.controls.no_of_imeis.value <= 0) {
				//this.toastr.error("Please specify a number of records to execute before pausing");
				this.toastr.error("Please specify a number of records to execute before pausing", "", { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
				return;
			}
		}

		if (this.updateFlag) {
			this.loading = true;
			this.campaignService.changeCampaignStatus(this.campaignService.campaignDetail.uuid, "Stopped", null).subscribe(data => {
				//this.toastr.success(data.message);
				this.toastr.success(data.message, null, { toastComponent: CustomSuccessToastrComponent });
				this.loading = false;
				this.campaignService.campaignDetail = undefined;
				this.addCampaign.reset();
				this.loading = true;
				this.campaignService.findByCampaignId(data.body).subscribe(data => {
					this.campaignService.editFlag = true;
					this.campaignService.campaignDetail = data.body;
					this.loading = false;
					if (this.campaignService.campaignDetail != undefined && this.campaignService.editFlag == true) {
						this.updateFlag = true;
					}
					this.campaignService.editFlag = false;
					this.campaignStatus = null;
					this.selectedTab.setValue(0);
					this.initializeForm();
					let is_pause_execution = this.addCampaign.get('is_pause_execution').value;
					if (is_pause_execution) {
						this.addCampaign.get('no_of_imeis').enable();
					} else {
						this.addCampaign.get('no_of_imeis').disable();
					}
					this.getCustomerCompaniesList();
				}, error => {
					console.log(error)
					this.loading = false;
				});
			}, error => {
				this.cancel();
				console.log(error);
				this.loading = false;
			});
		}
	}

	deleteCampaignDb() {
		this.loading = true;
		this.campaignService.deleteCampaign(this.campaignService.campaignDetail.uuid).subscribe(data => {
			//this.toastr.success(data.message);
			this.toastr.success(data.message, null, { toastComponent: CustomSuccessToastrComponent });
			this.loading = false;
			this.campaignService.campaignDetail = undefined;
			this.campaignService.campaignId = null;
			this.router.navigate(['campaigns']);
		}, error => {
			console.log(error);
			this.loading = false;
		});
	}


	changeCustomerValue() {
		this.customername = this.addCampaign.controls.customer_name.value;
		if (this.tempFlag) {
			this.addCampaign.get('customer_name').reset();
			//this.addCampaign.get('customer_name').patchValue("");
			this.addCampaign.get('customer_name').disable();
			this.toastr.error("The customer cannot be changed because this campaign is currently in progress.", "Customer Cannot be Changed", { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
		}
		else {
			this.selectedCustomerId = this.addCampaign.controls.customer_name.value;
			if (this.sequenceData[0].package != null && this.sequenceData[0].package != undefined && this.sequenceData[0].package.uuid != null && this.sequenceData[0].package.uuid != undefined) {

			} else {
				//this.toastr.error("Please Select BaseLine first");
				this.toastr.error("Please select a package to establish the baseline device configuration required for this campaign.", "Baseline Configuration Must Be Specified", { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
				this.addCampaign.patchValue({ 'file': null });
				this.addCampaign.patchValue({ 'customer_name': null });
				this.loading = false;
				return;
			}
			this.onGridReadySecond(this.paramList);
		}

	}

	/**
	 * Marks all controls in a form group as touched
	 * @param formGroup - The form group to touch
	 */
	private markFormGroupTouched(formGroup: FormGroup) {
		(<any>Object).values(formGroup.controls).forEach(control => {
			control.markAsTouched();

			if (control.controls) {
				this.markFormGroupTouched(control);
			}
		});
	}
	resumeCampaign() {
		if (this.updateFlag) {
			this.loading = true;
			this.campaignService.changeCampaignStatus(this.campaignService.campaignDetail.uuid, "In progress", null).subscribe(data => {
				//this.toastr.success(data.message);
				this.toastr.success(data.message, null, { toastComponent: CustomSuccessToastrComponent });
				this.campaignService.campaignDetail = undefined;
				this.loading = false;
				this.addCampaign.reset();
				this.loading = true;
				this.campaignService.findByCampaignId(data.body).subscribe(data => {
					this.campaignService.editFlag = true;
					this.campaignService.campaignDetail = data.body;
					this.loading = false;
					if (this.campaignService.campaignDetail != undefined && this.campaignService.editFlag == true) {
						this.updateFlag = true;
					}
					this.campaignService.editFlag = false;
					this.campaignStatus = null;
					this.selectedTab.setValue(0);
					this.initializeForm();
					let is_pause_execution = this.addCampaign.get('is_pause_execution').value;
					if (is_pause_execution) {
						this.addCampaign.get('no_of_imeis').enable();
					} else {
						this.addCampaign.get('no_of_imeis').disable();
					}
					this.getCustomerCompaniesList();
				}, error => {
					console.log(error)
					this.loading = false;
				});
				//this.router.navigate(['campaigns']);

			}, error => {
				this.cancel();
				this.loading = false;
			});
		}
	}

	activateCampaign() {
		if (!this.addCampaign.valid) {
			this.markFormGroupTouched(this.addCampaign);
			return;
		}

		// if (this.addCampaign.controls.is_imei_for_customer.value == null || this.addCampaign.controls.is_imei_for_customer.value == undefined) {
		// 	//this.toastr.error("Please select one of the option in the IMEIs tab section");
		// 	this.toastr.error("Please select one of the option in the IMEIs tab section", "", { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
		// 	return;
		// }
		if (!this.updateFlag && !this.addCampaign.controls.is_imei_for_customer.value) {
			if (this.data.length < 1) {
				//this.toastr.error("Please select a file");
				this.toastr.error("Please select a file", "", { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
				return;
			}
		}

		// if (this.addCampaign.controls.is_imei_for_customer.value) {
		// 	if (this.addCampaign.controls.customer_name.value == undefined || this.addCampaign.controls.customer_name.value == null || this.addCampaign.controls.customer_name.value == "") {
		// 		//this.toastr.error("Please select a customer");
		// 		this.toastr.error("Please select a customer", "", { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
		// 		return;
		// 	}
		// }

		if (this.addCampaign.controls.is_pause_execution.value) {
			if (this.addCampaign.controls.no_of_imeis.value == null || this.addCampaign.controls.no_of_imeis.value == undefined || this.addCampaign.controls.no_of_imeis.value == "" || this.addCampaign.controls.no_of_imeis.value <= 0) {
				//this.toastr.error("Please specify a number of records to execute before pausing");
				this.toastr.error("Please specify a number of records to execute before pausing", "", { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
				return;
			}
		}

		if (this.updateFlag) {
			this.loading = true;
			this.campaignService.changeCampaignStatus(this.campaignService.campaignDetail.uuid, "In progress", this.addCampaign.controls.no_of_imeis.value).subscribe(data => {
				//this.toastr.success(data.message);
				this.toastr.success(data.message, null, { toastComponent: CustomSuccessToastrComponent });
				this.campaignService.campaignDetail = undefined;
				this.loading = false;
				this.addCampaign.reset();
				this.loading = true;
				this.campaignService.findByCampaignId(data.body).subscribe(data => {
					this.campaignService.editFlag = true;
					this.campaignService.campaignDetail = data.body;
					this.loading = false;
					if (this.campaignService.campaignDetail != undefined && this.campaignService.editFlag == true) {
						this.updateFlag = true;
					}
					this.campaignService.editFlag = false;
					this.campaignStatus = null;
					this.selectedTab.setValue(0);
					this.initializeForm();
					let is_pause_execution = this.addCampaign.get('is_pause_execution').value;
					if (is_pause_execution) {
						this.addCampaign.get('no_of_imeis').enable();
					} else {
						this.addCampaign.get('no_of_imeis').disable();
					}
					this.getCustomerCompaniesList();
				}, error => {
					console.log(error)
					this.loading = false;
				});
				//this.router.navigate(['campaigns']);

			}, error => {
				this.cancel();
				this.loading = false;
			});
		}
	}

	changeCampaignStatus(status, isStay) {
		if (!this.addCampaign.valid) {
			this.markFormGroupTouched(this.addCampaign);
			return;
		}

		if (this.addCampaign.controls.is_imei_for_customer.value == null || this.addCampaign.controls.is_imei_for_customer.value == undefined) {
			//this.toastr.error("Please select one of the option in the IMEIs tab section");
			this.toastr.error("Please select one of the option in the IMEIs tab section", "", { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
			return;
		}
		if (!this.updateFlag && !this.addCampaign.controls.is_imei_for_customer.value) {
			if (this.data.length < 1) {
				//this.toastr.error("Please select a file");
				this.toastr.error("Please select a file", "", { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
				return;
			}
		}

		if (this.addCampaign.controls.is_imei_for_customer.value) {
			if (this.addCampaign.controls.customer_name.value == undefined || this.addCampaign.controls.customer_name.value == null || this.addCampaign.controls.customer_name.value == "") {
				//this.toastr.error("Please select a customer");
				this.toastr.error("Please select a customer", "", { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
				return;
			}
		}

		if (this.addCampaign.controls.is_pause_execution.value) {
			if (this.addCampaign.controls.no_of_imeis.value == null || this.addCampaign.controls.no_of_imeis.value == undefined || this.addCampaign.controls.no_of_imeis.value == "" || this.addCampaign.controls.no_of_imeis.value <= 0) {
				//this.toastr.error("Please specify a number of records to execute before pausing");
				this.toastr.error("Please specify a number of records to execute before pausing", "", { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
				return;
			}
		}

		if (this.updateFlag) {
			this.loading = true;
			this.campaignService.changeCampaignStatus(this.campaignService.campaignDetail.uuid, status, null).subscribe(data => {
				//this.toastr.success(data.message);
				this.toastr.success(data.message, null, { toastComponent: CustomSuccessToastrComponent });
				this.campaignService.campaignDetail = undefined;
				this.loading = false;
				this.addCampaign.reset();
				if (isStay) {
					this.loading = true;
					this.campaignService.findByCampaignId(data.body).subscribe(data => {
						this.campaignService.editFlag = true;
						this.campaignService.campaignDetail = data.body;
						this.loading = false;
						if (this.campaignService.campaignDetail != undefined && this.campaignService.editFlag == true) {
							this.updateFlag = true;
						}
						this.campaignService.editFlag = false;
						this.campaignStatus = null;
						this.selectedTab.setValue(0);
						this.initializeForm();
						this.getCustomerCompaniesList();
					}, error => {
						console.log(error)
						this.loading = false;
					});
				} else {
					this.router.navigate(['campaigns']);
				}
			}, error => {
				this.cancel();
				this.loading = false;
			});
		}
	}

	saveCampaign(addCampaign) {
		console.log("saveCampaign>  start" + this.customername);
		if (this.customername == null) {
			this.customername = addCampaign.controls.customer_name.value;
		}
		console.log("saveCampaign start letter " + this.customername);
		if (!this.addCampaign.valid) {
			this.markFormGroupTouched(this.addCampaign);
			return;
		}

		//if (addCampaign.controls.is_imei_for_customer.value == null || addCampaign.controls.is_imei_for_customer.value == undefined) {
		//	//this.toastr.error("Please select one of the option in the IMEIs tab section");
		//	this.toastr.error("Please select one of the option in the IMEIs tab section", "", { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
		//	return;
		// }
		if (!this.updateFlag && !addCampaign.controls.is_imei_for_customer.value && this.campaigntype == "IMEI") {
			if (this.data.length < 1) {
				//this.toastr.error("Please select a file");
				this.toastr.error("Please select a file", "", { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
				return;
			}
		}

		if (addCampaign.controls.is_imei_for_customer.value && addCampaign.controls.is_imei_for_customer.value == "RESTRICTED") {
			if (addCampaign.controls.customer_name.value == undefined || addCampaign.controls.customer_name.value == null || addCampaign.controls.customer_name.value == "") {
				//this.toastr.error("Please select a customer");
				this.toastr.error("Please select a customer", "", { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
				return;
			}
		}

		if (addCampaign.controls.is_pause_execution.value) {
			if (addCampaign.controls.no_of_imeis.value == null || addCampaign.controls.no_of_imeis.value == undefined || addCampaign.controls.no_of_imeis.value == "" || addCampaign.controls.no_of_imeis.value <= 0) {
				//this.toastr.error("Please specify a number of records to execute before pausing");
				this.toastr.error("Please specify a number of records to execute before pausing", "", { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
				return;
			}
		}
		this.isDirty = false;
		if (this.updateFlag) {
			this.submitted = true;
			//this.campaignObj.campaign_name = addCampaign.controls.campaign_name.value;
			this.campaignObj.campaign_name = addCampaign.controls.campaign_name.value.replace(/ {2,}/g, ' ').trim();
			this.campaignObj.description = addCampaign.controls.description.value;
			this.campaignObj.customer_name = addCampaign.controls.customer_name.value;
			this.campaignObj.is_imei_for_customer = addCampaign.controls.is_imei_for_customer.value;
			this.campaignObj.is_pause_execution = addCampaign.controls.is_pause_execution.value;
			this.campaignObj.no_of_imeis = addCampaign.controls.no_of_imeis.value;
			this.campaignObj.is_active = this.campaignService.campaignDetail.is_active;
			this.campaignObj.exclude_not_installed = addCampaign.controls.exclude_not_installed.value;
			this.campaignObj.exclude_low_battery = addCampaign.controls.exclude_low_battery.value;
			this.campaignObj.exclude_rma = addCampaign.controls.exclude_rma.value;
			this.campaignObj.exclude_eol = addCampaign.controls.exclude_eol.value;
			this.campaignObj.exclude_engineering = addCampaign.controls.exclude_engineering.value;
			this.campaignObj.excluded_imeis = this.excludedImeis.toString();
			this.campaignObj.is_replace = this.isReplace;
			this.campaignObj.customer_name = this.customername;
			this.campaignObj.init_status = addCampaign.controls.init_status.value;


			if (this.campaigntype != null) {
				this.campaignObj.campaign_type = this.campaigntype;
			}
			if (!addCampaign.controls.is_imei_for_customer.value) {
				this.imei_list = [];
				for (let i = 0; i < this.deviceList.length; i++) {
					this.imei_list.push(this.deviceList[i].imei);
				}
			}
			let not_eligible_devices = [];
			for (let i = 0; i < this.deviceList.length; i++) {
				if (this.deviceList[i].device_status_for_campaign != null && this.deviceList[i].device_status_for_campaign != undefined && this.deviceList[i].device_status_for_campaign == "Not Eligible") {
					not_eligible_devices.push(this.deviceList[i].imei);
				}
			}
			this.campaignObj.imei_list = this.imei_list.toString();
			var version_migration_list = [];
			var is_device_type_same = true;
			var previous_device_type = "";
			if (this.sequenceData != null && this.sequenceData.length > 0) {
				for (var j = 0; j < this.sequenceData.length; j++) {

					if (j == 0 && (this.sequenceData[j].package.uuid == undefined || this.sequenceData[j].package.uuid == null || this.sequenceData[j].package.package_name == undefined || this.sequenceData[j].package.package_name == null)) {
						this.toastr.error("Please select a package to establish the baseline device configuration required for this campaign.", "Baseline Configuration Must Be Specified", { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
						return;
					}

					if (j == (this.sequenceData.length - 1) && (this.sequenceData[j].package.uuid == undefined || this.sequenceData[j].package.uuid == null || this.sequenceData[j].package.package_name == undefined || this.sequenceData[j].package.package_name == null)) {
						this.toastr.error("Please select a package to establish the expected end state device configuration for this campaign.", "End State Configuration Must Be Specified", { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
						return;
					}
					if (this.sequenceData[j].package.uuid == undefined || this.sequenceData[j].package.uuid == null || this.sequenceData[j].package.package_name == undefined || this.sequenceData[j].package.package_name == null) {
						//this.toastr.error("Please select package for step : " + this.sequenceData[j].step_order_number);
						this.toastr.error("You must specify the starting and ending configuration for the campaign, as well as the update steps.", "Package Sequence Must Be Specified", { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
						return;
					}

					if ((this.sequenceData[j].at_command == undefined || this.sequenceData[j].at_command == null) && (j < (this.sequenceData.length - 1))) {
						//this.toastr.error("Please fill the value of command for step : " + this.sequenceData[j].step_order_number);
						this.toastr.error("Please fill the value of command for step : " + this.sequenceData[j].step_order_number, "", { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
						return;
					}

					if (this.sequenceData[j].package.device_type != undefined || this.sequenceData[j].package.device_type != null) {
						if (previous_device_type != "") {
							if (previous_device_type != this.sequenceData[j].package.device_type) {
								if (is_device_type_same) {
									is_device_type_same = false;
								}
							}
						} else {
							previous_device_type = this.sequenceData[j].package.device_type;
						}
					}


					var stepObj = {
						"package_uuid": this.sequenceData[j].package.uuid,
						"at_command": this.sequenceData[j].at_command,
						"step_order_number": j + 1,
						"step_id": null
					}

					if (this.sequenceData[j].step_id != undefined && this.sequenceData[j].step_id != null && this.sequenceData[j].step_id != "") {
						stepObj.step_id = this.sequenceData[j].step_id;
					}
					version_migration_list.push(stepObj);
				}
			}

			if (!is_device_type_same) {
				this.toastr.error("All Packages in the Campaign must have the same Device Type filter specified. Please check and try again.", "Mismatched Device Types", { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
				return;
			}
			this.campaignObj.device_type = previous_device_type;
			this.campaignObj.version_migration_detail = version_migration_list;
			this.campaignObj.not_eligible_devices = not_eligible_devices.toString();
			this.loading = true;
			this.campaignObj.device_status_for_campaign = this.device_status_for_campaign;
			this.campaignObj.imeis_to_be_removed = this.imeis_to_be_removed;
			this.campaignService.updateCampaign(this.campaignObj).subscribe(data => {
				this.toastr.success(data.message, null, { toastComponent: CustomSuccessToastrComponent });
				this.campaignService.campaignDetail = undefined;
				this.loading = false;
				this.addCampaign.reset();
				this.router.navigate(['campaigns']);

			}, error => {
				this.cancel();
				console.log(error);
				this.loading = false;
			});
		} else {
			this.CampaignList = [];
			this.submitted = true;
			const CampaignToSave = new Campaign();
			var version_migration_list = [];
			var is_device_type_same = true;
			var previous_device_type = "";
			if (this.sequenceData != null && this.sequenceData.length > 0) {
				for (var j = 0; j < this.sequenceData.length; j++) {

					if (j == 0 && (this.sequenceData[j].package.uuid == undefined || this.sequenceData[j].package.uuid == null || this.sequenceData[j].package.package_name == undefined || this.sequenceData[j].package.package_name == null)) {
						this.toastr.error("Please select a package to establish the baseline device configuration required for this campaign.", "Baseline Configuration Must Be Specified", { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
						return;
					}

					if (j == (this.sequenceData.length - 1) && (this.sequenceData[j].package.uuid == undefined || this.sequenceData[j].package.uuid == null || this.sequenceData[j].package.package_name == undefined || this.sequenceData[j].package.package_name == null)) {
						this.toastr.error("Please select a package to establish the expected end state device configuration for this campaign.", "End State Configuration Must Be Specified", { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
						return;
					}

					if (this.sequenceData[j].package.uuid == undefined || this.sequenceData[j].package.uuid == null || this.sequenceData[j].package.package_name == undefined || this.sequenceData[j].package.package_name == null) {
						//this.toastr.error("Please select package for step : " + this.sequenceData[j].step_order_number);
						this.toastr.error("You must specify the starting and ending configuration for the campaign, as well as the update steps.", "Package Sequence Must Be Specified", { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
						return;
					}

					if ((this.sequenceData[j].at_command == undefined || this.sequenceData[j].at_command == null) && (j < (this.sequenceData.length - 1))) {
						this.toastr.error("Please fill the value of command for step : " + this.sequenceData[j].step_order_number, "", { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
						//this.toastr.error("Please fill the value of command for step : " + this.sequenceData[j].step_order_number);
						return;
					}

					if (this.sequenceData[j].package.device_type != undefined || this.sequenceData[j].package.device_type != null) {
						if (previous_device_type != "") {
							if (previous_device_type != this.sequenceData[j].package.device_type) {
								if (is_device_type_same) {
									is_device_type_same = false;
								}
							}
						} else {
							previous_device_type = this.sequenceData[j].package.device_type;
						}
					}

					var stepObj = {
						"package_uuid": this.sequenceData[j].package.uuid,
						"at_command": this.sequenceData[j].at_command,
						"step_order_number": j + 1,
						"step_id": null
					}
					version_migration_list.push(stepObj);
				}
			}

			if (!is_device_type_same) {
				this.toastr.error("All Packages in the Campaign must have the same Device Type filter specified. Please check and try again.", "Mismatched Device Types", { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
				return;
			}
			CampaignToSave.version_migration_detail = version_migration_list;
			CampaignToSave.device_type = previous_device_type;
			//CampaignToSave.campaign_name = addCampaign.controls.campaign_name.value.trim();
			CampaignToSave.campaign_name = addCampaign.controls.campaign_name.value.replace(/ {2,}/g, ' ').trim();
			//CampaignToSave.grouping_uuid = addCampaign.controls.grouping_uuid.value;
			CampaignToSave.description = addCampaign.controls.description.value;
			//CampaignToSave.customer_name = addCampaign.controls.customer_name.value;
			CampaignToSave.is_imei_for_customer = addCampaign.controls.is_imei_for_customer.value;
			CampaignToSave.is_pause_execution = addCampaign.controls.is_pause_execution.value;
			CampaignToSave.no_of_imeis = addCampaign.controls.no_of_imeis.value;
			CampaignToSave.exclude_not_installed = addCampaign.controls.exclude_not_installed.value;
			CampaignToSave.exclude_low_battery = addCampaign.controls.exclude_low_battery.value;
			CampaignToSave.exclude_rma = addCampaign.controls.exclude_rma.value;
			CampaignToSave.exclude_eol = addCampaign.controls.exclude_eol.value;
			CampaignToSave.exclude_engineering = addCampaign.controls.exclude_engineering.value;
			console.log("saveCampaign customername" + this.customername);
			CampaignToSave.customer_name = this.customername.toString();
			CampaignToSave.campaign_type = this.campaigntype;
			console.log(" saveCampaign this.customername >> " + this.customername.toString() + "this.campaigntype " + this.campaigntype);
			//console.log(" CampaignToSave.customer_name this.customername >> "+CampaignToSave.customer_name +"this.campaigntype>  "+CampaignToSave.campaign_type );


			let not_eligible_devices = [];
			for (let i = 0; i < this.deviceList.length; i++) {
				if (this.deviceList[i].device_status_for_campaign != null && this.deviceList[i].device_status_for_campaign != undefined && this.deviceList[i].device_status_for_campaign == "Not Eligible") {
					not_eligible_devices.push(this.deviceList[i].imei);
				}
			}
			CampaignToSave.imei_list = this.imei_list.toString();
			console.log(this.imei_list.toString());

			CampaignToSave.not_eligible_devices = not_eligible_devices.toString();
			this.CampaignList.push(CampaignToSave);
			this.loading = true;
			this.campaignService.addCampaign(CampaignToSave).subscribe(data => {
				this.toastr.success(data.message, null, { toastComponent: CustomSuccessToastrComponent });
				this.campaignService.campaignDetail = undefined;
				this.loading = false;
				this.addCampaign.reset();
				//this.router.navigate(['campaigns']);
				this.loading = true;
				this.campaignService.findByCampaignId(data.body).subscribe(data => {
					if (!this.isCustomerSelected) {
						console.log("isCustomerSelected : " + this.isCustomerSelected);
						this.addCampaign.get('customer_name').reset();
						//this.addCampaign.get('customer_name').patchValue("");
						this.addCampaign.get('customer_name').disable();
					}
					this.campaignService.editFlag = true;
					this.campaignService.campaignDetail = data.body;
					this.loading = false;
					this.selectedTab.setValue(0);
					if (this.campaignService.campaignDetail != undefined && this.campaignService.editFlag == true) {
						this.updateFlag = true;
					}
					this.campaignService.editFlag = false;
					this.campaignStatus = null;
					this.initializeForm();
					this.getCustomerCompaniesList();;
				}, error => {
					console.log(error)
					this.loading = false;
				});

			}, error => {
				this.cancel();
				console.log(error);
				this.loading = false;
			});
		}

	}

	saveCampaignForPopup(addCampaign) {

		if (!this.addCampaign.valid) {
			this.markFormGroupTouched(this.addCampaign);
			return;
		}

		if (addCampaign.controls.is_imei_for_customer.value == null || addCampaign.controls.is_imei_for_customer.value == undefined) {
			//this.toastr.error("Please select one of the option in the IMEIs tab section");
			this.toastr.error("Please select one of the option in the IMEIs tab section", "", { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
			return;
		}
		if (!this.updateFlag && !addCampaign.controls.is_imei_for_customer.value) {
			if (this.data.length < 1) {
				//this.toastr.error("Please select a file");
				this.toastr.error("Please select a file", "", { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
				return;
			}
		}

		if (addCampaign.controls.is_imei_for_customer.value) {
			if (addCampaign.controls.customer_name.value == undefined || addCampaign.controls.customer_name.value == null || addCampaign.controls.customer_name.value == "") {
				//this.toastr.error("Please select a customer");
				this.toastr.error("Please select a customer", "", { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
				return;
			}
		}

		if (addCampaign.controls.is_pause_execution.value) {
			if (addCampaign.controls.no_of_imeis.value == null || addCampaign.controls.no_of_imeis.value == undefined || addCampaign.controls.no_of_imeis.value == "" || addCampaign.controls.no_of_imeis.value <= 0) {
				//this.toastr.error("Please specify a number of records to execute before pausing");
				this.toastr.error("Please specify a number of records to execute before pausing", "", { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
				return;
			}
		}

		if (this.updateFlag) {
			this.submitted = true;
			//this.campaignObj.campaign_name = addCampaign.controls.campaign_name.value.trim();
			this.campaignObj.campaign_name = addCampaign.controls.campaign_name.value.replace(/ {2,}/g, ' ').trim();
			this.campaignObj.description = addCampaign.controls.description.value;
			this.campaignObj.customer_name = addCampaign.controls.customer_name.value;
			this.campaignObj.is_imei_for_customer = addCampaign.controls.is_imei_for_customer.value;
			this.campaignObj.is_pause_execution = addCampaign.controls.is_pause_execution.value;
			this.campaignObj.no_of_imeis = addCampaign.controls.no_of_imeis.value;
			this.campaignObj.is_active = this.campaignService.campaignDetail.is_active;
			this.campaignObj.exclude_not_installed = addCampaign.controls.exclude_not_installed.value;
			this.campaignObj.exclude_low_battery = addCampaign.controls.exclude_low_battery.value;
			this.campaignObj.exclude_rma = addCampaign.controls.exclude_rma.value;
			this.campaignObj.exclude_eol = addCampaign.controls.exclude_eol.value;
			this.campaignObj.exclude_engineering = addCampaign.controls.exclude_engineering.value;

			if (!addCampaign.controls.is_imei_for_customer.value) {
				this.imei_list = [];
				for (let i = 0; i < this.deviceList.length; i++) {
					this.imei_list.push(this.deviceList[i].imei);
				}
			}
			let not_eligible_devices = [];
			for (let i = 0; i < this.deviceList.length; i++) {
				if (this.deviceList[i].device_status_for_campaign != null && this.deviceList[i].device_status_for_campaign != undefined && this.deviceList[i].device_status_for_campaign == "Not Eligible") {
					not_eligible_devices.push(this.deviceList[i].imei);
				}
			}
			this.campaignObj.imei_list = this.imei_list.toString();
			var version_migration_list = [];
			if (this.sequenceData != null && this.sequenceData.length > 0) {
				for (var j = 0; j < this.sequenceData.length; j++) {

					if (j == 0 && (this.sequenceData[j].package.uuid == undefined || this.sequenceData[j].package.uuid == null || this.sequenceData[j].package.package_name == undefined || this.sequenceData[j].package.package_name == null)) {
						this.toastr.error("Please select a package to establish the baseline device configuration required for this campaign.", "Baseline Configuration Must Be Specified", { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
						return;
					}

					if (j == (this.sequenceData.length - 1) && (this.sequenceData[j].package.uuid == undefined || this.sequenceData[j].package.uuid == null || this.sequenceData[j].package.package_name == undefined || this.sequenceData[j].package.package_name == null)) {
						this.toastr.error("Please select a package to establish the expected end state device configuration for this campaign.", "End State Configuration Must Be Specified", { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
						return;
					}

					if (this.sequenceData[j].package.uuid == undefined || this.sequenceData[j].package.uuid == null || this.sequenceData[j].package.package_name == undefined || this.sequenceData[j].package.package_name == null) {
						//this.toastr.error("Please select package for step : " + this.sequenceData[j].step_order_number);
						this.toastr.error("You must specify the starting and ending configuration for the campaign, as well as the update steps.", "Package Sequence Must Be Specified", { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
						return;
					}

					if ((this.sequenceData[j].at_command == undefined || this.sequenceData[j].at_command == null) && (j < (this.sequenceData.length - 1))) {
						//this.toastr.error("Please fill the value of command for step : " + this.sequenceData[j].step_order_number);
						this.toastr.error("Please fill the value of command for step : " + this.sequenceData[j].step_order_number, "", { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
						return;
					}
					var stepObj = {
						"package_uuid": this.sequenceData[j].package.uuid,
						"at_command": this.sequenceData[j].at_command,
						"step_order_number": j + 1,
						"step_id": null
					}

					if (this.sequenceData[j].step_id != undefined && this.sequenceData[j].step_id != null && this.sequenceData[j].step_id != "") {
						stepObj.step_id = this.sequenceData[j].step_id;
					}
					version_migration_list.push(stepObj);
				}
			}
			this.campaignObj.version_migration_detail = version_migration_list;
			this.campaignObj.not_eligible_devices = not_eligible_devices.toString();
			this.loading = true;
			this.campaignObj.imeis_to_be_removed = this.imeis_to_be_removed;
			this.campaignObj.device_status_for_campaign = this.device_status_for_campaign;

			this.campaignService.updateCampaign(this.campaignObj).subscribe(data => {
				this.toastr.success(data.message, null, { toastComponent: CustomSuccessToastrComponent });
				this.campaignService.campaignDetail = undefined;
				this.loading = false;
				this.addCampaign.reset();
				//this.router.navigate(['campaigns']);
				this.loading = true;
				this.campaignService.findByCampaignId(data.body).subscribe(data => {
					this.campaignService.editFlag = true;
					this.campaignService.campaignDetail = data.body;
					$('#editModalForCampaignDetail').modal('hide');
					this.changeDetection = false;
					this.loading = false;
					this.selectedTab.setValue(0);
					if (this.campaignService.campaignDetail != undefined && this.campaignService.editFlag == true) {
						this.updateFlag = true;
					}
					this.campaignService.editFlag = false;
					this.campaignStatus = null;
					this.initializeForm();
					this.getCustomerCompaniesList();
				}, error => {
					console.log(error)
					this.loading = false;
				});

			}, error => {
				$('#editModalForCampaignDetail').modal('hide');
				this.loading = false;
				this.cancel();
			});
		}

	}

	cancelForEditModel() {
		this.addCampaign.patchValue({ 'campaign_name': this.campaignService.campaignDetail.campaign_name });
		this.addCampaign.patchValue({ 'description': this.campaignService.campaignDetail.description });
	}

	uploadClick() {

		if (this.sequenceData[0].package == undefined || this.sequenceData[0].package == null || this.sequenceData[0].package.uuid == undefined || this.sequenceData[0].package.uuid == null) {
			//this.toastr.error("Please Select BaseLine first");
			this.toastr.error("Please select a package to establish the baseline device configuration required for this campaign.", "Baseline Configuration Must Be Specified", { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
			this.addCampaign.patchValue({ 'file': null });
			this.addCampaign.patchValue({ 'customer_name': null });
			this.loading = false;
			return;
		}

		if (this.deviceList != undefined && this.deviceList != null && this.deviceList.length > 0) {
			if (this.campaignStatus == "In progress") {
				this.actionName = "AgainUploadFile";
				this.commonTitle = "Confirm Change";
				this.commonMessage = "Uploading a new list will add the new IMEIs to the existing list of gateways. Would you like to proceed?";
				this.commonModelOption = "Yes";
				$('#commonModal').modal('show');
				return;
			} else if (this.campaignStatus == "Not Started") {
				this.actionName = "AgainUploadFile";
				this.changeDetection = true;
				$('#modalForAppendOrReplace').modal('show');
				return;
			} else {
				this.changeDetection = true;
				this.actionName = "AgainUploadFile";
				this.commonTitle = "Confirm Change";
				this.commonMessage = "Uploading a new list will replace the existing list of gateways. Would you like to proceed?";
				this.commonModelOption = "Yes";
				$('#commonModal').modal('show');
				return;
			}
		} else {
			this.addCampaign.patchValue({ 'is_imei_for_customer': false });
			this.changeDetection = true;
			$('#uploadFileModal').modal('show');
		}

		// if (this.deviceList != undefined && this.deviceList != null && this.deviceList.length > 0) {
		//   this.actionName = "AgainUploadFile";
		//   this.commonTitle = "Confirm Change";
		//   this.commonMessage = "Uploading a new list will replace the existing list of gateways. Would you like to proceed?";
		//   this.commonModelOption = "Yes";
		//   $('#commonModal').modal('show');
		//   return;
		// } else {
		//   this.addCampaign.patchValue({ 'is_imei_for_customer': false });
		//   $('#uploadFileModal').modal('show');
		// }

	}

	callChangeSequenceForSecond() {
		this.deviceList.forEach(function (data, index) {
			data.step_order_number = index + 1;
			data.status = "Pending";
		});
		this.gridApiSecond.setRowData(this.deviceList);
	}

	getDataByImeiListOrCustomerId(action) {
		this.deviceList = [];

		if (this.sequenceData[0].package != null && this.sequenceData[0].package != undefined && this.sequenceData[0].package.uuid != null && this.sequenceData[0].package.uuid != undefined) {

		} else {
			//this.toastr.error("Please Select BaseLine first");
			this.toastr.error("Please select a package to establish the baseline device configuration required for this campaign.", "Baseline Configuration Must Be Specified", { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
			this.addCampaign.patchValue({ 'file': null });
			this.addCampaign.patchValue({ 'customer_name': null });
			this.loading = false;
			return;
		}
		this.loading = true;
		//this.imei_list = [];
		this.gridApiSecond.setRowData([]);
		let campaignUuid = null;
		if (this.updateFlag) {
			campaignUuid = this.campaignService.campaignDetail.uuid;
		}
		if (action == "Customer") {

			this.loading = false;

			// this.campaignService.findDataByCustomer(this.gridApiSecond.paginationGetCurrentPage() + 1, this.column, this.order, this.filterValuesJsonObj, this.pageLimit,this.selectedCustomerId, this.sequenceData[0].package.uuid, campaignUuid).subscribe(data => {
			// 	this.deviceList = data.content;
			// 	this.loading = false;
			// 	if (this.deviceList != null && this.deviceList.length > 0) {
			// 		this.imei_list = [];
			// 		for (let i = 0; i < this.deviceList.length; i++) {
			// 			this.imei_list.push(this.deviceList[i].imei);
			// 		}
			// 	}
			// 	this.gridApiSecond.setRowData([]);
			// 	this.callChangeSequenceForSecond();
			// 	this.loading = false;
			// }, error => {
			// 	console.log(error)
			// 	this.loading = false;
			// });
		} else {
			$('#uploadFileModal').modal('hide');
			this.campaignService.findDataByImeiList(this.imei_list, this.sequenceData[0].package.uuid).subscribe(data => {
				if (data.invalidImeiList != null && data.invalidImeiList.length > 0) {

					var finalErrorStringFirst = "The following IMEI values are part of one or more other campaigns. For this, we can consider campaigns that are <b> In Progress </b> or <b>Not Started;</b> we do not need to consider <b>Finished</b> campaigns.<br>";

					for (var i = 0; i < data.invalidImeiList.length; i++) {
						finalErrorStringFirst += "<br><b>" + data.invalidImeiList[i] + "</b>";
					}
					var finalErrorTitleFirst = data.invalidImeiList.length + " Errors";
					this.toastr.error(finalErrorStringFirst, finalErrorTitleFirst, { toastComponent: CustomBatchErrorToastrComponent, tapToDismiss: true, disableTimeOut: true, enableHtml: true });

				}
				if (data.deviceWithEligibilityList != null && data.deviceWithEligibilityList.length > 0) {
					this.deviceList = data.deviceWithEligibilityList;

					console.log(this.rowDataSecond);
					console.log("device list with rowdata ", this.deviceList);
					//this.onGridReadySecondDefined(this.paramList,data);


					this.rowDataSecond = data.deviceWithEligibilityList;
					this.gridApiSecond = this.paramList.api;
					this.gridColumnApiSecond = this.paramList.columnApi;
					this.gridOptions.api.setDomLayout('autoHeight');
					let gridSavedState = sessionStorage.getItem("gateway_list_for_add_campaign_grid_column_state");
					if (gridSavedState) {
						console.log(gridSavedState);
						console.log("column state found in session storage");
						this.gridColumnApiSecond.setColumnState(JSON.parse(gridSavedState));
						console.log("priting column state after setting");
						console.log(this.gridColumnApiSecond.getColumnState());
					}
					let filterSavedState = sessionStorage.getItem("gateway_list_for_add_campaign_grid_filter_state");
					if (filterSavedState) {
						console.log(filterSavedState);
						console.log("filter state found in session storage");
						this.gridApiSecond.setFilterModel(JSON.parse(filterSavedState));
						console.log("priting filter state after setting");
						console.log(this.gridApiSecond.getFilterModel());
					} else {
						console.log("Filter state not found in session storage");
					}

					var datasource = {
						getRows: (params: IGetRowsParams) => {
							this.loading = true;
							this.sortForAgGrid(params.sortModel);
							this.filterForAgGrid(params.filterModel);
							this.loading = false;
							params.successCallback(data.deviceWithEligibilityList, data.deviceWithEligibilityList.length);
							this.total_number_of_records = data.length;
							if (this.isFirstTimeLoad == true) {
								let paginationSavedPage = sessionStorage.getItem("gateway_list_for_add_campaign_grid_saved_page");
								if (paginationSavedPage && this.isFirstTimeLoad) {
									this.gridOptions.api.paginationGoToPage(Number.parseInt(paginationSavedPage));
									this.isFirstTimeLoad = false;
									this.isPaginationStateRecovered = true;
								}
							}

						}
					}
					this.gridApiSecond.setDatasource(datasource);





				}

				console.log(data);
				this.loading = false;
				$('#uploadFileModal').modal('hide');
				if (this.deviceList != null && this.deviceList.length > 0) {
					if (!this.updateFlag) {
						this.imei_list = [];
					}
					this.imei_list = [];
					for (let i = 0; i < this.deviceList.length; i++) {
						this.imei_list.push(this.deviceList[i].imei);
					}
				}
				this.gridApiSecond.setRowData([]);
				this.callChangeSequenceForSecond();
				this.loading = false;

			}, error => {
				console.log(error)
				this.loading = false;
				$('#uploadFileModal').modal('hide');
			});
		}

	}

	cancelUploadClick() {
		$('#uploadFileModal').modal('hide');
		this.addCampaign.patchValue({ 'file': null });
		this.selectedFileName = null;
		this.loading = false;
	}

	clearUploadData() {
		this.addCampaign.patchValue({ 'file': null });
		this.selectedFileName = null;
		if (!this.updateFlag) {
			this.deviceList = [];
			this.gridApiSecond.setRowData([]);
			this.data = [];
		}
	}


	@ViewChild('searchInput') searchInput: ElementRef;

	onSelectionChange(event: any) {
		console.log("event call");
		$('#clickModel').modal('hide');
		console.log(event.option.value);
		this.sequenceData[this.rowParam.rowIndex].package.package_name = event.option.value;
		this.onCellValueChanged(this.rowParam);
		this.searchInput.nativeElement.value = '';
		this.packageListForDisplay = this.packageListName;
	}
	onKeypressEvent(event: any) {
		var value = event.target.value;
		const filterValue = value.toLowerCase();
		this.packageListForDisplay = this.packageListName.filter(option => option != null && option.toLowerCase().includes(filterValue));
		return this.packageListForDisplay;

	}

	private _filter(value: string): string[] {
		const filterValue = value.toLowerCase();
		return this.packageListName.filter(option => option.toLowerCase().includes(filterValue));
	}

	/**
	 * on file drop handler
	 */
	onFileDropped($event) {
		this.onFileChange($event);
	}

	onFileChange(event) {
		this.loading = true;
		this.selectedFileName = event[0].name;

		if (this.selectedFileName.substr(this.selectedFileName.lastIndexOf('.') + 1) != "xlsx") {
			this.toastr.error("The IMEI List data file must be in .XLSX format. Please check your file and try again.", "Incorrect File Format", { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
			this.clearUploadData();
			return;
		}
		console.log("Inside OnfileChange Method");
		const target: DataTransfer = <DataTransfer>(event);
		// if (event.length !== 1) {
		//     throw new Error('Cannot use multiple files');
		// }
		const reader: FileReader = new FileReader();
		reader.onload = (e: any) => {
			/* read workbook */
			const bstr: string = e.target.result;
			const wb: XLSX.WorkBook = XLSX.read(bstr, { type: 'binary' });

			/* grab first sheet */
			const wsname: string = wb.SheetNames[0];
			const ws: XLSX.WorkSheet = wb.Sheets[wsname];

			/* save data */
			this.data = (XLSX.utils.sheet_to_json(ws, { header: 'A' }));
			const header = this.data[0];

			console.log("Excel data captured");
			// console.log(this.data);
			const headerKeys = Object.keys(header);
			this.loading = false;
			//this.checkFileData();

		};

		reader.readAsBinaryString(event[0]);


	}

	checkFileData() {
		console.log(this.sequenceData[0].package);
		if (this.data.length < 1) {
			//this.toastr.error("Please select a file");
			if (!this.updateBaseline) {
				this.toastr.error("Please select a file", "", { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
			}
			return;

		}
		this.imei_list = [];
		if (!this.updateFlag) {
			this.imei_list = [];
		}
		if (this.data.length > 1) {
			this.loading = true;
			if (this.data[0].A != "imei" && this.data[0].A != "IMEI" && this.data[0].A != "imeis" && this.data[0].A != "IMEIS") {
				this.toastr.error("The uploaded file does not follow the expected data format. Please use the template provided to format the data.", "Incorrect File Format", { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
				this.clearUploadData();
				return;
			}
			for (let i = 1; i < this.data.length; i++) {
				this.imei_list.push(this.data[i].A);
			}
			this.getDataByImeiListOrCustomerId("ImeiList");

		} else {
			//this.toastr.error("File is empty. Please select another file");
			this.toastr.error("File is empty. Please select another file", "", { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
			//this.loading = false;
		}
	}

	deletePopupCancel() { }

	deactivateDevice() { }

	onFileChangeBrower(event) {
		/* wire up file reader */
		this.loading = true;
		console.log("Inside OnfileChange Method");
		const target: DataTransfer = <DataTransfer>(event);
		if (event.target.files.length !== 1) {
			throw new Error('Cannot use multiple files');
		}
		this.selectedFileName = event.target.files[0].name;

		if (this.selectedFileName.substr(this.selectedFileName.lastIndexOf('.') + 1) != "xlsx") {
			this.toastr.error("The IMEI List data file must be in .XLSX format. Please check your file and try again.", "Incorrect File Format", { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
			this.clearUploadData();
			return;
		}
		const reader: FileReader = new FileReader();
		this.loading = false;
		reader.onload = (e: any) => {
			/* read workbook */
			const bstr: string = e.target.result;
			const wb: XLSX.WorkBook = XLSX.read(bstr, { type: 'binary' });

			/* grab first sheet */
			const wsname: string = wb.SheetNames[0];
			const ws: XLSX.WorkSheet = wb.Sheets[wsname];

			/* save data */
			this.data = (XLSX.utils.sheet_to_json(ws, { header: 'A' }));
			const header = this.data[0];

			console.log("Excel data captured");
			// console.log(this.data);
			const headerKeys = Object.keys(header);
			this.loading = false;
			//this.checkFileData();
		};
		reader.readAsBinaryString(event.target.files[0]);
		console.log("Calling to insertIntoDb Method");

	}

	cancel() {
		this.loading = false;
		this.campaignService.campaignDetail = undefined;
		this.addCampaign.reset();
		this.router.navigate(['campaigns']);
	}

	cancelForAddCampaign() {
		this.router.navigate(['campaigns']);
	}

	onBtExport() {
		// var columnWidth = getBooleanValue('#columnWidth')
		//   ? getTextValue('#columnWidthValue')
		//   : undefined;


		this.campaignService.exportGatewayList(this.campaignService.campaignDetail.uuid, this.campaignService.campaignDetail.campaign_name).subscribe(data => {
			console.log(data);
		}, error => {
			console.log(error);
		});

		// var params = {
		//   sheetName:
		//     "Gateway List",
		//   exportMode: undefined,
		//   rowHeight: 20,
		//   columnWidth: 150
		// };
		// this.gridApiSecond.exportDataAsExcel(params);
	}

	changeValueOfPauseAfter() {
		// debugger
		// if (this.addCampaign.controls.no_of_imeis.value > this.deviceList.length) {
		// 	this.toastr.error("You have chosen a pause interval that is greater than the current number of gateways in the campaign.", "Pause Interval", { toastComponent: CustomErrorToastrComponent, tapToDismiss: true, disableTimeOut: true });
		// 	this.addCampaign.patchValue({ 'no_of_imeis': this.deviceList.length });
		// }
		this.changeDetection = true;
		this.isDirty = true;
	}

	radioChangeForPauseAfter(event) {
		this.changeDetection = true;
		this.isDirty = true;
	}
	deviceHistoryValue: any;
	/*   deviceHistoryPopupValue(params) {
	  
		this.deviceHistoryValue = params.node.data;
    
		console.log(" params.node.data "+JSON.stringify(this.deviceHistoryValue));
	  } */

	deviceHistoryPopupValue(params): void {

		console.log(" params.node.data " + JSON.stringify(params.node.data));
		this.dialogRef = this.dialog.open(DeviceHistoryDetailsDialogComponent, {
			width: '1200px',
			height: '750px',
			data: { data: params.node.data }
		});
	}


	onPageSizeChange() {
		sessionStorage.setItem('add_campaign_list_page_size', String(this.selected));
		console.log(sessionStorage.getItem('add_campaign_list_page_size') + "from onPageSizeChange");
		var value = this.selected;
		this.pageLimit = (Number(value));
		this.gridApiSecond.gridOptionsWrapper.setProperty('cacheBlockSize', value);
		const api: any = this.gridApiSecond;
		api.rowModel.resetCache();
		this.gridApiSecond.paginationSetPageSize(Number(value));
	}
	resetStatusOfSelectedDevice(): void {
		let selectedNodes = this.gridApiSecond.getSelectedNodes();
		let selectedData = selectedNodes.map(node => node.data);
		let otherStatusFlag = false;
		this.selectedDeviceIdForResetStatus = [];
		this.selectedDataForResetStatus = [];
		selectedData.forEach(element => {
			if (element.device_status_for_campaign !== "Problem") {
				otherStatusFlag = true;
			}
			else {
				this.selectedDeviceIdForResetStatus.push(element.imei);
			}

		});
		if (this.selectedDeviceIdForResetStatus == null || this.selectedDeviceIdForResetStatus.length <= 0) {
			this.actionName = "NoAction";
			this.commonTitle = "Reset Status";
			this.commonMessage = "No selected devices are in the Problem status please select Problem status device.";
			this.commonModelOption = "OK";
			$('#commonModal').modal('show');
			selectedData = null;
			return;
		}

		if (otherStatusFlag) {
			this.actionName = "ResetStatusGatewayBulk";
			this.commonTitle = "Reset Status";
			this.commonMessage = "One or more of the selected devices is not currently in the Problem status and will not be reset.";
			this.commonModelOption = "OK";
			$('#commonModal').modal('show');
			return;
		}
		else {
			this.actionName = "ResetStatusGatewayBulk";
			this.commonTitle = "Reset Status";
			this.commonMessage = "All selected devices are in the Problem status and will be reset.";
			this.commonModelOption = "OK";
			$('#commonModal').modal('show');
			return;
		}


	};


	SearchData(data) {
		this.addCampaign.controls.customer_name.patchValue([]);
		this.getAllCustomer(data);
	}

	getAllCustomer(name?: any) {
		if (!name) {
			name = '';
		}
		this.gatewayListService.getAllCustomerObject(['CUSTOMER'], name).subscribe(
			data => {
				for (const iterator of data) {
					iterator.organisation_name = iterator.organisationName;
				}
				this.companies = data;
			},
			error => {
				console.log(error);
			}
		);
	}
	getAllCustomer2(name?: any) {
		if (!name) {
			name = '';
		}
		this.gatewayListService.getAllCustomerObject(['CUSTOMER'], name).subscribe(
			data => {
				for (const iterator of data) {
					iterator.organisation_name = iterator.organisationName;
				}
				this.companies2 = data;
			},
			error => {
				console.log(error);
			}
		);
	}
	SearchData2(data) {
		this.addCampaign.controls.customer_name2.patchValue([]);
		this.getAllCustomer2(data);
	}

	openedChange(event) {
		console.log(event);
		if (!event) {
			console.log(this.addCampaign.controls.customer_name.value);
		}
	}
	customers: string = '';

	openedChange2(event) {
		console.log(event);
		if (!event) {
			console.log(this.addCampaign.controls.customer_name2.value);
			this.customers = this.addCampaign.controls.customer_name2.value.toString();;
		}
	}
	showClick() {
		this.onGridReadySecond(this.paramList);
	}
	selectCustomerChangeHandler(event: any) {
		// console.log(event);
		if (event.value.length === 1 && event.value[0] !== 0) {
			// console.log(event);
		}
	}
	// ##### pagination new component


	onColumnVisible(params) {
		console.log("Column Visible working");
		console.log(params);
		let columnState = this.gridColumnApiSecond.getColumnState();
		this.saveGridStateInSession(columnState);
	}
	saveGridStateInSession(columnState) {
		sessionStorage.setItem("gateway_list_for_add_campaign_grid_column_state", JSON.stringify(columnState));
	}
	onColumnPinned(params) {
		console.log("Column Pinned working");
		console.log(params);
		let columnState = this.gridColumnApiSecond.getColumnState();
		this.saveGridStateInSession(columnState);
	}
	onPaginationChanged(params) {
		if (this.isPaginationStateRecovered || params.api.paginationGetCurrentPage() > 0) {
			sessionStorage.setItem("gateway_list_for_add_campaign_grid_saved_page", params.api.paginationGetCurrentPage());
		}
	}
	onColumnResized(params) {
		console.log(params);
		let columnState = this.gridColumnApiSecond.getColumnState();
		this.saveGridStateInSession(columnState);
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
				this.column = 'organisation_name';
				this.order = 'asc';
				params.api.setFilterModel(null);
				params.column.gridColumnApiSecond.applyColumnState({ defaultState: { sort: null } });
				//params.column.columnApi.resetColumnState();
				params.api.onFilterChangedSecond();
				sessionStorage.setItem("gateway_list_for_add_campaign_grid_saved_page", null);
				sessionStorage.setItem("gateway_list_for_add_campaign_grid_filter_state", null);
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
	onSortChangedSecond(params) {
		console.log("Column Sort second working");
		console.log(params);
		let columnState = this.gridColumnApiSecond.getColumnState();
		this.saveGridStateInSession(columnState);
	}
	onFilterChangedSecond(params) {
		console.log("Filter change second working");
		let filterModel = params.api.getFilterModel();
		console.log(filterModel);
		sessionStorage.setItem("gateway_list_for_add_campaign_grid_filter_state", JSON.stringify(filterModel));
	}
	sortForAgGrid(sortModel) {
		this.gridApiSecond.paginationCurrentPage = 0;
		if (sortModel[0] == undefined || sortModel[0] == null || sortModel[0].colId == undefined || sortModel[0].colId == null) {
			return;
		}
		switch (sortModel[0].colId) {

			case 'imei': {
				this.column = "deviceId";
				this.order = sortModel[0].sort;
				break;
			}
			case 'organisation_name': {
				this.column = "customerName";
				this.order = sortModel[0].sort;
				break;
			}
			//   case 'device_status_for_campaign': {
			//     this.column = "mcuVersion";
			//     this.order = sortModel[0].sort;
			//     break;
			//   }

			//   case 'installed_flag': {
			//     this.column = "installedFlag";
			//     this.order = sortModel[0].sort;
			//     break;
			//   }

			case 'last_report': {
				this.column = "lastReport";
				this.order = sortModel[0].sort;
				break;
			}


		}
	}
	elasticFilter: Filter = new Filter();
	status: string = '';

	filterForAgGrid(filterModel) {
		this.gridApi.paginationCurrentPage = 0;
		if (filterModel != undefined) {
			this.filterValuesJsonObj = {};
			this.filterValues = new Map();


			var filterModel1;
			const elasticModelFilters = JSON.parse(JSON.stringify(filterModel));
			filterModel1 = elasticModelFilters;
			const Okeys = Object.keys(filterModel1);
			let index = 0;
			for (let i = 0; i < Okeys.length; i++) {
				this.elasticFilter.key = Okeys[i];

				if (filterModel1[Okeys[i]].filterType === 'set') {
					this.elasticFilter.operator = 'eq';
					if (this.elasticFilter.key === 'event_id' && filterModel1[Okeys[i]].values && filterModel1[Okeys[i]].values.length > 0) {
						const values = [];
						if (filterModel1[Okeys[i]].values) {
							for (const iterator of filterModel1[Okeys[i]].values) {
								const obj = this.companiesName.find(item => item.eventType === iterator);
								if (obj) {
									values.push(obj.eventId);
								}
							}
							this.elasticFilter.value = values.toString();
						}
					} else {
						if (Okeys[i] === 'device_status_for_campaign') {
							this.status = filterModel1[Okeys[i]].values.toString();
						}
						if (Okeys[i] === 'organisation_name') {
							this.customers = filterModel1[Okeys[i]].values.toString();
						}
					}
					//this.filterValues.set('filterValues ' + i + 'a', this.elasticFilter);
				}
				// this.filterValues.set('filterValues ' + i, this.elasticFilter)
				this.elasticFilter = new Filter();
				index = i + 1;
			}





			if (this.customers != null && this.customers != "" && this.customers != undefined) {
				this.filterValues.set("customerName", this.customers);
			} else {
				if (filterModel.organisation_name != undefined && filterModel.organisation_name.filter != null && filterModel.organisation_name.filter != undefined && filterModel.organisation_name.filter != "") {
					this.filterValues.set("customerName", filterModel.organisation_name.filter);
				}
			}

			if (filterModel.imei != undefined && filterModel.imei.filter != null && filterModel.imei.filter != undefined && filterModel.imei.filter != "") {
				this.filterValues.set("imei", filterModel.imei.filter);
			}

			if (this.status != null && this.status != undefined && this.status != "") {
				this.filterValues.set("deviceStatusForCampaign", this.status);
			}

			if (filterModel.last_report != undefined && filterModel.last_report.filter != null && filterModel.last_report.filter != undefined && filterModel.last_report.filter != "") {
				this.filterValues.set("lastReport", filterModel.last_report.filter);
			}

			let jsonObject = {};
			this.filterValues.forEach((filter, key) => {
				jsonObject[key] = filter
			});

			this.filterValuesJsonObj = jsonObject;
		}
	}
	onFirstDataRendered(params) {
		console.log("First time data rendered");
		this.isFirstTimeLoad = true;
	}

	customerNameFilter: string = '';

	onCustomerSelection(event: any) {
		this.customerNameFilter = event.target.value;
		console.log(this.customerNameFilter);
	}
	onStatusSelection(event: any) {
		this.customerNameFilter = event.target.value;
	}
	refreshGateways() {
 			//  if (this.updateFlag) {
			console.log("devicelist in gatway tab ", this.deviceList);
			this.gridApiSecond.setColumnDefs(this.columnDefsSecond);
			this.gridApiSecond.setRowData([]);
			this.gridApiSecond.setRowData(this.deviceList);
			this.imei_list = [];
			for (let i = 0; i < this.deviceList.length; i++) {
				this.imei_list.push(this.deviceList[i].imei);
				//   }
			}
			if (this.updateFlag) {
				this.campaignService.findByCampaignUUID(this.campaignService.campaignDetail.uuid)
					.subscribe(data => {
 			  
					  }, error => {
						console.log(error)
 					  });
				this.onGridReadySecond(this.paramList);
			}
	}
}


function cellCellEditorParams(params) {
	var packageNameList = packageListNameGloble;
	return {
		values: packageNameList,
		formatValue: function (value) {
			return value;
		},
	};
}
function getBooleanValue(cssSelector) {
	return document.querySelector(cssSelector).checked === true;
}
function getTextValue(cssSelector) {
	return document.querySelector(cssSelector).value;
}
function getNumericValue(cssSelector) {
	var value = parseFloat(getTextValue(cssSelector));
	if (isNaN(value)) {
		var message = 'Invalid number entered in ' + cssSelector + ' field';
		alert(message);
		throw new Error(message);
	}
	return value;
}
function myColumnWidthCallback(params) {
	var originalWidth = params.column.getActualWidth();
	if (params.index < 7) {
		return originalWidth;
	}
	return 30;
}
var sortActive = false;
var filterActive = false;


var packageListNameGloble = [];
const filterParamsStatus = {
	values: function (params) {
		setTimeout(function () {
			params.success([
 				'Eligible',
				'Not Eligible',
				'Off Path',
				'On Path',
				'Unknown',
				'Completed',
				'Problem',
				'Pending',
				'Not Started',
				'Success',
				'Excluded',
				'Not Started',
				'Removed']);
		}, 2000);
	},
};