import { AgGridAngular } from '@ag-grid-community/angular';
import { ColDef } from '@ag-grid-enterprise/all-modules';
import { LEADING_TRIVIA_CHARS } from '@angular/compiler/src/render3/view/template';
import { Component, OnInit, ViewChild } from '@angular/core';
import { each, values } from 'underscore';
import { BtnCellRendererForGatewayDetails } from '../devices/devices-list/btn-cell-renderer.component';
import { DevicesListService } from '../devices/devices-list/devices-list.service';
import { CustomDateComponent } from '../ui-elements/custom-date-component.component';
import { CustomHeader } from '../ui-elements/custom-header.component';
import '@ag-grid-community/core/dist/styles/ag-grid.css';
import '@ag-grid-community/core/dist/styles/ag-theme-alpine.css';
import { of } from 'rxjs';
import { connectableObservableDescriptor } from 'rxjs/internal/observable/ConnectableObservable';

@Component({
  selector: 'app-aggrid-test',
  templateUrl: './aggrid-test.component.html',
  styleUrls: ['./aggrid-test.component.css']
})
export class AggridTestComponent implements OnInit {

  @ViewChild('myGrid')
  myGrid!: AgGridAngular;

  public gridOptions: any;
  public gridApi: any;
  columnDefs: any;
  rowData: any;
  public gridColumnApi!: { setColumnState: (arg0: any) => void; getColumnState: () => any; };
  total_number_of_records: any;
  jsonObject: any;
  frameworkComponents: any;
  public sideBar: any;
  selectedValues = new Map();
  headers: any[] = [];

  constructor(private devicesListService: DevicesListService) {

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
    this.frameworkComponents = {
      btnCellRenderer: BtnCellRendererForGatewayDetails,
      agDateInput: CustomDateComponent,
      agColumnHeader: CustomHeader,
    }
    this.gridOptions = {
      defaultColDef: {
        sortable: true,
        filter: 'agTextColumnFilter',
        resizable: true
      },

      columnDefs: this.columnDefs,
      enableSorting: true,
      enableFilter: true,
      pagination: true
    };

  }

  onBtNormalCols() {
    this.gridApi.setColumnDefs([]);
    //this.columnDefs = createNormalColDefs();
    this.gridApi.setColumnDefs(this.columnDefs);
    //this.onColumnVisible(this.gridApi);
  }


  ngOnInit(): void {
    this.devicesListService.deviceImeiList[0] = "015115006460447";
   // console.log(this.gridApi.getFilterModel());
    // console.log("inside get Headers")
    //  this.devicesListService.getHeaders().subscribe(data => {
    //    console.log("H Data is     ")
    //    this.headers = data.body;
    //    console.log(this.headers)
    //  });

    this.devicesListService.getDeviceReports().subscribe(data => {
      this.rowData = data.body.content;
      this.total_number_of_records = data.total_key;
      console.log("Data is ")
      
      const colDefs = this.gridOptions.api.getColumnDefs();
      const generalDefs = this.gridOptions.api.getColumnDefs();
      const voltageDefs = this.gridOptions.api.getColumnDefs();
      colDefs.length = 0;


      const keys = Object.keys(data.body.content[0])
      keys.forEach(key => {
       colDefs.push({ field: key })
      });
       if (this.rowData != null && this.rowData.length > 0) {
         for (var i = 0; i < this.rowData.length; i++) {
          const stringObject = JSON.parse(this.rowData[i].json_object);
          this.rowData[i].parsed_object = stringObject;
          generalDefs.length = 0;
          voltageDefs.length = 0;
        //  if(this.headers !=null && this.headers.length > 0) {
        //    for(var j=0 ;j<this.headers.length;j++){
        //      console.log("Header is printed here")
        //     console.log(this.headers[j])
        //    }
        //  }
     
         // const JStringObjects = Object.keys(stringObject)
          const JStringValues = Object.values(stringObject)
          console.log(stringObject)
         // this.rowData[i].general_mask_fields =  this.rowData[i].parsed_object.general_mask_fields;
          //this.rowData[i].voltage =this.rowData[i].parsed_object.voltage
          
         //  JStringObjects.forEach(JStringObject => {
        //     JStringValues.forEach(JStringValue =>{
        //       this.selectedValues.set(JStringObject,JStringValue);
        //       console.log("printing map values");
        //     console.log(this.selectedValues);
        //       let jsonObject = {};  
        //       this.selectedValues.forEach((value, key) => {  
        //        this.jsonObject[key] = value;
       //   });  
          // console.log(JSON.stringify(jsonObject));

        //   });
          const generalKeys = Object.keys(stringObject.general_mask_fields)
          const voltageKeys = Object.keys(stringObject.voltage)

         generalKeys.forEach(generalKey => generalDefs.push({ field: generalKey }));
          voltageKeys.forEach(voltageKey => voltageDefs.push({ field: voltageKey }));
       
          this.columnDefs = [{ headerName: "", children: colDefs },
           { headerName: "general_mask_fields",columnGroupShow: 'closed', children: generalDefs },
          { headerName: "Voltage", children: voltageDefs }, {
            headerName: 'Action', field: 'action', sortable: false, filter: 'agNumberColumnFilter', floatingFilter: true, floatingFilterComponent: 'customClearFloatingFilter',
            floatingFilterComponentParams: {
              suppressFilterButton: true
            },
            cellRenderer: 'btnCellRenderer', width: 140, minWidth: 140, pinned: 'right'
          }
          ];
         }
       }
      // console.log("ROW DATA : "+ this.rowData)
     this.gridOptions.api.setRowData(this.rowData);
    },
      error => {
        console.log(error);
      })
  }
  onSortChanged(params: any) {
    console.log("Column Sort working");
    console.log(params);
    let columnState = this.gridColumnApi.getColumnState();
    this.saveGridStateInSession(columnState);
  }
  saveGridStateInSession(columnState: any) {
    sessionStorage.setItem("gateway_list_grid_column_state", JSON.stringify(columnState));
  }

  // getHeaders()
  // {
  //   console.log("inside get Headers")
  //   this.devicesListService.getHeaders().subscribe(data => {
  //     console.log("H Data is     ")
  //     console.log(data.body)
  //     this.headers = data.body;
  //     console.log(this.headers)
  //   });
  // }
}
