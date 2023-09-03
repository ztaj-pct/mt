package com.pct.device.service.impl;

import com.pct.device.bean.ShippedDevicesHubRequest;
import com.pct.device.payload.ShippedDevicesRequest;
import com.pct.device.service.IShipmentService;
import com.pct.device.util.BeanConverter;
import com.pct.device.util.RestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Abhishek on 01/07/20
 */

@Service
public class ShipmentService implements IShipmentService {

    Logger logger = LoggerFactory.getLogger(ShipmentService.class);

    @Autowired
    private RestUtils restUtils;
    @Autowired
    private BeanConverter beanConverter;

    @Override
    public Boolean addShipmentDetail(ShippedDevicesRequest shippedDevicesRequest) {
        ShippedDevicesHubRequest shippedDevicesHubRequest = beanConverter.convertShippedDevicesToShippedDevicesHubRequest(shippedDevicesRequest);
        try {
            Boolean status = restUtils.sendShippedDevicesToHub(shippedDevicesHubRequest);
            return status;
        } catch (Exception e) {
            logger.error("Exception while posting shipped devices", e);
            return false;
        }
    }
}
