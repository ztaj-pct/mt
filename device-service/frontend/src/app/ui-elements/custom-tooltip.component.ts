import { Component } from '@angular/core';
import { ITooltipAngularComp } from '@ag-grid-community/angular';

@Component({
  selector: 'tooltip-component',
  template: ` 
    <div class="custom-tooltip" *ngIf="!isHeader">
     {{ valueToDisplay }} 
    </div>`,
  styles: [
    `
      :host {
        position: absolute;
         overflow: hidden;
        pointer-events: none;
        transition: opacity 1s;
      }

      :host.ag-tooltip-hiding {
        opacity: 0;
      }
 
    `,
  ],
})
export class CustomTooltip implements ITooltipAngularComp {
  public params: any;
  public valueToDisplay: string;
  public isHeader: boolean;
  public isGroupedHeader: boolean;

  agInit(params): void {
    this.params = params;
    this.isHeader = params.rowIndex === undefined;
    this.isGroupedHeader = !!params.colDef.children;
    this.valueToDisplay = params.value;
  }
}
