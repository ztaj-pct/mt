import { User } from "./user.model";

export class AddLocalUser {
    user: User = new User();
    role: Role = new Role();
}

export class Role {
    id: number;
    roleName: String;
    description: String
}