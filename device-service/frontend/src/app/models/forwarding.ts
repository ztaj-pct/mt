export class Forwarding{
  device_id:string;
   forwarding_list:  Array<DeviceForwarding> = new Array<DeviceForwarding>();
}

export class DeviceForwarding{
    type: string;
    url:string;
}
