import { Company } from './company.model';


export class User {
  id: number=0;
  user_name: String='';
  password: String='';
  first_name: string='';
  last_name: string='';
  email: string='';
  phone: string='';
  country_code: string='';
  role:  Array<Role> = new Array<Role>();
  is_active: Boolean=false;
  fleet: Company = new Company();
  company: string='';
  notify:any
  companies:any
}
export class Role {
  id: number;
  roleName: String;
  description: String

  constructor(id:any,roleName:any,description:any){
    this.id=id;
    this.roleName = roleName;
    this.description =description
  }
  }
