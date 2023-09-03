import { Injectable } from '@angular/core';
import * as Highcharts from 'highcharts';
  
@Injectable()
export class HighchartsService {

  

  constructor() {
  }
  
  createPieChart(container, options?: Object) {
    let opts = options;
    let e = document.createElement("div");
    container.appendChild(e);
    Highcharts.chart(container, opts);
  }


  createStackedbarChart(el, cfg) {
    Highcharts.chart(el, cfg);
  }
}
