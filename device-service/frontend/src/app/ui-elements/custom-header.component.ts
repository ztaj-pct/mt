import { Component, ViewChild, ElementRef } from '@angular/core';

@Component({
  selector: 'app-loading-overlay',
  template: `
  <div  *ngIf="params.displayName != '#'">
    <div style="cursor: pointer;">
      <div
        *ngIf="params.enableMenu"
        #menuButton 
        class="customHeaderMenuButton"
        (click)="onMenuClicked($event)"
      >
        <i class="fa {{ params.menuIcon }}"></i>
      </div>
      <div class="customHeaderLabel ag-header-cell-text" role="columnheader" style="white-space: normal;"   (click)="onSortRequestedName($event)">{{ params.displayName }}</div>&nbsp;
      <div
        *ngIf="params.enableSorting"
        
        [ngClass]="ascSort"
        class="customSortDownLabel"
      >
        <i class="fa fa-long-arrow-up"></i>
      </div>
      <div
        *ngIf="params.enableSorting"
        [ngClass]="descSort"
        class="customSortUpLabel"
      >
        <i class="fa fa-long-arrow-down" style="
        margin-left: -6px;
    "></i>
      </div>
      <div
      *ngIf="params.column.userProvidedColDef.unSortIcon && !params.column.isSortAscending() && !params.column.isSortDescending()"
      
      [ngClass]="noSort"
      class="customSortDownLabel"
    >
      <i class="fa fa-arrows-v" style="
      margin-left: -13px;"></i>
    </div>
      <!--<div  *ngIf="params.displayName != 'Action'"
       
        (click)="clearFilterForColumn('', $event)"
        
        class="clearFilter"
      >
        <i class="fa fa-times" 
       
    ></i>
      </div> -->
    </div>
    </div>
  `,
  styles: [
    `
      .customHeaderMenuButton,
      .customHeaderLabel,
      .customSortDownLabel,
      .customSortUpLabel,
      .customSortRemoveLabel {
        float: left;
        cursor: pointer;
        margin: 0 0 0 6px;
      }

      .customSortUpLabel {
        margin: 0;
      }

      .customHeaderMenuButton i{
        margin-left: -6px;
        margin-right: 6px;
      }
      .customSortRemoveLabel {
        font-size: 11px;
      }

      .active {
        color: black;
        opacity:1;
      }
      .inactives {
        opacity:0;
      }
      .clearFilter {
        color: black;
      }
      .clearFilter i{
        position: absolute;
        right: 19px;
        top: 20px;
      }
      .ag-header-cell {
        position:relative;
      }
    `,
  ],
})
export class CustomHeader {
  public params: any;
  private ascSort: string;
  private descSort: string;
  private noSort: string;
  private sort: string;
  @ViewChild('menuButton', { read: ElementRef }) public menuButton;

  agInit(params): void {
    this.params = params;

    params.column.addEventListener(
      'sortChanged',
      this.onSortChanged.bind(this)
    );
    this.onSortChanged();
  }

  onSortRequested(order, event) {

    this.params.setSort(order, event.shiftKey);
  }
  onSortRequestedName(event) {
    if (this.params.enableSorting) {
      if (this.params.column.sort == undefined) {
        this.sort = 'asc';
        this.params.setSort(this.sort, event.shiftKey);
      } else if (this.params.column.isSortAscending() == true) {
        this.sort = 'desc';

        this.params.setSort(this.sort, event.shiftKey);
      } else if (this.params.column.isSortDescending() == true) {

        this.sort = 'asc';
        this.params.setSort(this.sort, event.shiftKey);
      }
    }
  }

  onMenuClicked() {

    this.params.showColumnMenu(this.menuButton.nativeElement);
  }

  onSortChanged() {

    this.ascSort = this.descSort = this.noSort = 'inactive';
    this.ascSort = this.descSort = this.noSort = 'inactives';
    if (this.params.column.isSortAscending()) {
      this.ascSort = 'active';
      this.descSort = 'inactives';
    } else if (this.params.column.isSortDescending()) {
      this.descSort = 'active';
      this.ascSort = 'inactives';

    } else {
      this.noSort = 'active';
    }
  }




  clearFilterForColumn(order, event) {
    this.params.setSort('asc', event.shiftKey);
    this.ascSort = 'inactives';
    this.clearCountryFilter();

  }

  clearCountryFilter() {
    var FilterComponent = this.params.api.getFilterInstance(this.params.column.colId);
    FilterComponent.setModel(null);
    this.params.api.onFilterChanged();
  }

}

