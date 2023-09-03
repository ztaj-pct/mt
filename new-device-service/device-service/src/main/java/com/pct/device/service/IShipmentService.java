package com.pct.device.service;

import com.pct.device.payload.ShippedDevicesRequest;

/**
 * @author Abhishek on 01/07/20
 */

public interface IShipmentService {

    Boolean addShipmentDetail(ShippedDevicesRequest shippedDevicesRequest);
}
