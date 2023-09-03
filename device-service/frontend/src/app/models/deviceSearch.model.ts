export class DeviceSearch {
  currentpage: Number;
  startDate: String;
  endDate: String;
  fleet: String;
  communicationstatus: String;
  geotabStatus: String;
  error: String;
  searchbyNameorId: String;
  short: String;
  order: String;
  limit: Number = 30;


  get getUrl(): string {
    var url = '_limit=' + this.limit + '&_page=' + this.currentpage;

    if (this.startDate) {
      url += "&_fromDate=" + this.startDate;
    }
    if (this.endDate) {
      url += "&_toDate=" + this.endDate;
    }
    if (this.searchbyNameorId) {
      url += "&_nameOrId=" + this.searchbyNameorId;
    }
    if (this.fleet) {
      url += "&_fleet=" + this.fleet;
    }
    if (this.short) {
      url += "&sort=" + this.short;
    }
    if (this.order) {
      url += "&_order=" + this.order;
    }
    return url;
  }
}
