package com.pct.device.controller;

import com.pct.common.dto.ResponseDTO;
import com.pct.device.payload.ShippedDevicesRequest;
import com.pct.device.service.IShipmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Abhishek on 01/07/20
 */

@RestController
@RequestMapping("/shipment")
public class ShipmentController {

    Logger logger = LoggerFactory.getLogger(ShipmentController.class);

    @Autowired
    private IShipmentService shipmentService;

    @PostMapping
    public ResponseEntity<ResponseDTO> shipmentDetail(@RequestBody ShippedDevicesRequest shippedDevicesRequest) {
        logger.info("Inside shipmentDetail method");
        try {
            Boolean status = shipmentService.addShipmentDetail(shippedDevicesRequest);
            return new ResponseEntity<ResponseDTO>(new ResponseDTO(status, "Successfully posted shipment detail to HUB"), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Exception occurred while processing shipment detail", e);
            return new ResponseEntity<ResponseDTO>(new ResponseDTO(false, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
