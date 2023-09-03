
export class CompanyAccess {
    id: number;
    companyId: number;
    accessToCompanyId :number;
    
    constructor(companyId,accessToCompanyId){
        this.companyId=companyId;
        this.accessToCompanyId=accessToCompanyId;
    }
}
