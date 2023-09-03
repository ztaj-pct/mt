package com.pct.device.controller;

import com.pct.common.constant.SensorStatus;
import com.pct.common.dto.ResponseBodyDTO;
import com.pct.common.dto.ResponseDTO;
import com.pct.common.util.JwtUtil;
import com.pct.device.bean.SensorBean;
import com.pct.device.payload.UpdateMacAddressRequest;
import com.pct.device.service.ISensorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/sensor")
public class SensorController {

    Logger logger = LoggerFactory.getLogger(SensorController.class);

    @Autowired
    private ISensorService sensorService;
    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping()
    public ResponseEntity<ResponseBodyDTO<List<SensorBean>>> getSensor(HttpServletRequest httpServletRequest,
                                                                       @RequestParam(value = "can", required = false) String accountNumber,
                                                                       @RequestParam(value = "sensor-uuid", required = false) String sensorUuid,
                                                                       @RequestParam(value = "sensor-status", required = false) SensorStatus sensorStatus) {
        try {
            List<SensorBean> sensorList = sensorService.getSensor(accountNumber, sensorUuid, sensorStatus);

            return new ResponseEntity<ResponseBodyDTO<List<SensorBean>>>(
                    new ResponseBodyDTO<List<SensorBean>>(true, "Fetched Sensor(s) Successfully", sensorList),
                    HttpStatus.OK);
        } catch (Exception exception) {
            logger.error("Exception occurred while getting sensor(s)", exception);
            return new ResponseEntity<ResponseBodyDTO<List<SensorBean>>>(
                    new ResponseBodyDTO<List<SensorBean>>(false, exception.getMessage(), null), HttpStatus.INTERNAL_SERVER_ERROR);

        }
    }

    @PutMapping("/mac-address")
    public ResponseEntity<ResponseDTO> updateMacAddressForSensor(HttpServletRequest httpServletRequest,
                                                                  @RequestBody UpdateMacAddressRequest updateMacAddressRequest) {
        logger.info("Request received for updating sensor uuid {} with mac address {}", updateMacAddressRequest.getUuid(), updateMacAddressRequest.getMacAddress());
        try {
            Long userId = jwtUtil.getUserIdFromRequest(httpServletRequest);
            Boolean status = sensorService.updateSensorMacAddress(updateMacAddressRequest, userId);
            return new ResponseEntity<>(new ResponseDTO(status, "Successfully updated MAC Address for Sensor"),
                    HttpStatus.OK);
        } catch (Exception exception) {
            logger.error("Exception occurred while updating sensor mac address", exception);
            return new ResponseEntity<ResponseDTO>(
                    new ResponseDTO(false, exception.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
