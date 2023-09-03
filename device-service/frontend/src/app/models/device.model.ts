import { Company } from './Company.model';

export class Device {
        id: String;
        model: string;
        make: String;
        type: String;
        name: String;
        registeredOn: String;
        currentState: String;
        manufacturer: String;
        lastCommunication: Date;
        battery: number; 
        fleet: Company = new Company();
        resellerId: Company = new Company();
}