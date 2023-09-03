package com.pct.device.controller;

import com.pct.common.constant.GatewayStatus;
import com.pct.common.constant.GatewayType;
import com.pct.common.dto.ResponseBodyDTO;
import com.pct.common.dto.ResponseDTO;
import com.pct.common.util.JwtUtil;
import com.pct.device.bean.GatewayBean;
import com.pct.device.dto.GatewayListDTO;
import com.pct.device.exception.DeviceException;
import com.pct.device.payload.ShipmentDetailsRequest;
import com.pct.device.service.IGatewayService;
import com.pct.device.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/gateway")
public class GatewayController {

    Logger logger = LoggerFactory.getLogger(GatewayController.class);

    @Autowired
    private IGatewayService gatewayService;
    @Autowired
    private JwtUtil jwtUtil;

//    @GetMapping()
//    public ResponseEntity<ResponseBodyDTO<FetchGatewayImeiResponse>> getJobSummary(HttpServletRequest httpServletRequest,
//                                                                                   @RequestParam(value = "imei", required = false) Long imei) {
//        try {
//            FetchGatewayImeiResponse gatewayImeiResponse = gatewayService.getGatewayImeiResponse(imei);
//            return new ResponseEntity<ResponseBodyDTO<FetchGatewayImeiResponse>>(
//                    new ResponseBodyDTO<FetchGatewayImeiResponse>(true, "Fetched Job Summary Successfully", gatewayImeiResponse),
//                    HttpStatus.OK);
//        } catch (Exception exception) {
//            logger.error("Exception occurred while getting gateway list", exception);
//            return new ResponseEntity<ResponseBodyDTO<FetchGatewayImeiResponse>>(
//                    new ResponseBodyDTO<FetchGatewayImeiResponse>(false, exception.getMessage(), null), HttpStatus.INTERNAL_SERVER_ERROR);
//
//        }
//    }

    @GetMapping()
    public ResponseEntity<ResponseBodyDTO<GatewayListDTO>> getGateway(HttpServletRequest httpServletRequest,
                                                                      @RequestParam(value = "can", required = false) String accountNumber,
                                                                      @RequestParam(value = "imei", required = false) String imei,
                                                                      @RequestParam(value = "gateway-uuid", required = false) String gatewayUuid,
                                                                      @RequestParam(value = "gateway-status", required = false) GatewayStatus gatewayStatus,
                                                                      @RequestParam(value="type", required = false) GatewayType type,
                                                                      @RequestParam(value="macAddress", required = false) String macAddress
                                                                      ) {
        try {

            List<GatewayBean> gatewayBeanList = gatewayService.getGateway(accountNumber, imei, gatewayUuid, gatewayStatus,type,macAddress);
            GatewayListDTO gatewayListDto = new GatewayListDTO();
            gatewayListDto.setGatewayList(gatewayBeanList);

            return new ResponseEntity<ResponseBodyDTO<GatewayListDTO>>(
                    new ResponseBodyDTO<GatewayListDTO>(true, "Fetched Gateway(s) Successfully", gatewayListDto),
                    HttpStatus.OK);
        } catch (Exception exception) {
            logger.error("Exception occurred while getting gateway(s)", exception);
            return new ResponseEntity<ResponseBodyDTO<GatewayListDTO>>(
                    new ResponseBodyDTO<GatewayListDTO>(false, exception.getMessage(), null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/shipping-detail")
    public ResponseEntity<ResponseBodyDTO> shippingDetails(@RequestBody ShipmentDetailsRequest shipmentDetailsRequest) {
        try {
            logger.info("Request received for ShipmentDetails {}", shipmentDetailsRequest.toString());
            Map<String, List<String>> imeiResponseMap = gatewayService.saveShipmentDetails(shipmentDetailsRequest);
            String message = "";
            if (imeiResponseMap.get(Constants.REJECTED_MAP_KEY) == null ||
                    imeiResponseMap.get(Constants.REJECTED_MAP_KEY).isEmpty()) {
                message = "Successfully saved Gateways and Sensors for shipment details order number " + shipmentDetailsRequest.getSalesforceOrderNumber();
            } else if (imeiResponseMap.get(Constants.SAVED_MAP_KEY) == null ||
                    imeiResponseMap.get(Constants.SAVED_MAP_KEY).isEmpty()) {
                message = "All Gateways and Sensors for shipment details order number " + shipmentDetailsRequest.getSalesforceOrderNumber() + " were rejected";
            } else if ((imeiResponseMap.get(Constants.SAVED_MAP_KEY) != null &&
                    !imeiResponseMap.get(Constants.SAVED_MAP_KEY).isEmpty()) &&
                    (imeiResponseMap.get(Constants.REJECTED_MAP_KEY) != null &&
                            !imeiResponseMap.get(Constants.REJECTED_MAP_KEY).isEmpty())) {
                message = "Shipment details were partially saved";
            }
            return new ResponseEntity(new ResponseBodyDTO(true, message, imeiResponseMap),
                    HttpStatus.OK);
        } catch (DeviceException e) {
            logger.error("Exception while saving shipment details", e);
            return new ResponseEntity(new ResponseBodyDTO(false, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            logger.error("Exception while saving shipment details", e);
            return new ResponseEntity(new ResponseBodyDTO<List<String>>(false, "Exception occurred while saving shipment details"),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/reset")
    public ResponseEntity<ResponseDTO> resetGateway(@RequestParam(name = "can", required = true) String can,
                                                    @RequestParam(name = "imei", required = false) String imei) {
        try {
            logger.info("Request received resetting gateway");
            boolean status = gatewayService.resetGateway(can, imei);
            return new ResponseEntity(new ResponseDTO(true, "Gateway deleted successfully"),
                    HttpStatus.OK);
        } catch (DeviceException e) {
            logger.error("Exception while saving shipment details", e);
            return new ResponseEntity(new ResponseDTO(false, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            logger.error("Exception while saving shipment details", e);
            return new ResponseEntity(new ResponseDTO(false, "Exception occurred while resetting gateway"),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @DeleteMapping("/resetGateway")
    public ResponseEntity<ResponseDTO> resetGatewayWithMac(@RequestParam(name = "can", required = true) String can,
                                                    @RequestParam(name = "mac", required = false) String mac) {
        try {
            logger.info("Request received resetting gateway.");
            boolean status = gatewayService.resetGatewayWithMac(can, mac);
            return new ResponseEntity(new ResponseDTO(true, "Gateway deleted successfully"),
                    HttpStatus.OK);
        } catch (DeviceException e) {
            logger.error("Exception while saving shipment details.", e);
            return new ResponseEntity(new ResponseDTO(false, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            logger.error("Exception while saving shipment details.", e);
            return new ResponseEntity(new ResponseDTO(false, "Exception occurred while resetting gateway"),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
