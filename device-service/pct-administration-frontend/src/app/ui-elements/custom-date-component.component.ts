import { Component, ElementRef, ViewChild } from '@angular/core';
import flatpickr from 'flatpickr';

@Component({
  selector: 'app-loading-overlay',
  template: `
    <div
      #flatpickrEl
      class="ag-input-wrapper custom-date-filter"
      role="presentation"
    >
      <input type="text" #eInput data-input style="width: 100%;" placeholder="yyyy-mm-dd" />
      <a class="input-button" title="clear" data-clear>
        <i class="fa fa-times"></i>
        
      </a>
     
    </div>
  `,
  styles: [
    `
      .custom-date-filter a {
        position: absolute;
        right: 25px;
        color: rgba(0, 0, 0, 0.54);
        cursor: pointer;
      }

    /*   .custom-date-filter:after {
        content: "\f073";
        background:url('../../assets/calendericon.png');
        font-family: FontAwesome !important;
        opacity: 1;
      } */
      
    `,
  ],
})
export class CustomDateComponent {
  @ViewChild('flatpickrEl', { read: ElementRef })
  flatpickrEl!: ElementRef;
  @ViewChild('eInput', { read: ElementRef })
  eInput!: ElementRef;
  private date: Date = new Date();
  private params: any;
  private picker: any;

  agInit(params: any): void {
    this.params = params;
  }

  ngAfterViewInit(): void {
    // outputs `I am span`
    this.picker = flatpickr(this.flatpickrEl.nativeElement, {
      onChange: this.onDateChanged.bind(this),
      wrap: true,
    });

    this.picker.calendarContainer.classList.add('ag-custom-component-popup');
  }

  ngOnDestroy() {
    console.log(`Destroying DateComponent`);
  }

  onDateChanged(selectedDates: Date[]) {
    this.date = selectedDates[0] || null;
    this.params.onDateChanged();
  }

  getDate(): Date {
    return this.date;
  }

  setDate(date: Date): void {
    this.date = date || null;
    this.picker.setDate(date);
  }

  setInputPlaceholder(placeholder: string): void {
    this.eInput.nativeElement.setAttribute('placeholder', placeholder);
  }

  setInputAriaLabel(label: string): void {
    this.eInput.nativeElement.setAttribute('aria-label', label);
  }
}
