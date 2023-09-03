import { Company } from './Company.model';


export class User {
  id: number;
  user_name: String;
  password: String;
  first_name: string;
  last_name: string;
  email: string;
  phone: string;
  country_code: string;
  //role:  Array<Role> = new Array<Role>();
  is_active: Boolean;
  fleet: Company = new Company();
  company: string;
  role:any
  
  
  notify:any
  companies:any
  roles:any
  organisations:any

}
export class Role {
  id: number;
  name: String;
  description: String

  constructor(id,name, description){
    this.id=id;
    this.name = name;
    this.description = description;
    
  }
  }


  export class Organisation{
    id:number;
    organisationName:String;
    type:String;
    isActive: boolean;
    accountNumber:number
    constructor(id, organisationName, type, isActive, accountNumber){     
       this.id=id;
       this.organisationName=organisationName;
       this.type=type;
       this.isActive=isActive;
       this.accountNumber=accountNumber;

    }

  }
  