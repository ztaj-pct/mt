
export interface type{
    id:string;
    idValue:number;
}
export class PaginationConstants{
    //pageSizeOptionsArr: number[] = [10, 25, 50, 100, 1000];
     public pageSizeObjArr :type[] = [
        {id: '10', idValue: 10},
        {id: '25', idValue: 25},
        {id: '50', idValue: 50},
        {id: '100', idValue: 100},
        {id: 'All', idValue: 5000}
    ];

    public pageSizeObjArrForInstallationSummaryView :type[] = [
        {id: '10', idValue: 10},
        {id: '25', idValue: 25},
        {id: '50', idValue: 50},
        {id: '100', idValue: 100},
        // {id: 'All', idValue: 5000}
    ];
    
}
