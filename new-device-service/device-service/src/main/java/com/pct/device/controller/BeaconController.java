package com.pct.device.controller;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pct.common.dto.ResponseBodyDTO;
import com.pct.common.dto.ResponseDTO;
import com.pct.common.util.JwtUtil;
import com.pct.device.exception.DeviceException;
import com.pct.device.payload.BeaconDetailsRequest;
import com.pct.device.service.IGatewayService;

@RestController
@RequestMapping("/beacon")
public class BeaconController {
	
	 @Autowired
	  private IGatewayService gatewayService;
	 
	 @Autowired
	 private JwtUtil jwtutil;
	
	 Logger logger = LoggerFactory.getLogger(BeaconController.class);


	    @PostMapping("/shipping-detail")
	    public ResponseEntity<ResponseDTO> beaconDetails(@RequestBody BeaconDetailsRequest beaconDetailsRequest, HttpServletRequest httpServletRequest) {
	        try {
	            logger.info("Request received for beaconDetails {}", beaconDetailsRequest.toString());
	            Long userId = jwtutil.getUserIdFromRequest(httpServletRequest);
	            Map<String, List<String>> macResponseMap = gatewayService.saveBeaconDetails(beaconDetailsRequest,userId);
	            return new ResponseEntity(new ResponseDTO(true, "Beacon Saved Successfully"),
	                    HttpStatus.OK);
	        } catch (DeviceException e) {
	            logger.error("Exception while saving beacon details", e);
	            return new ResponseEntity(new ResponseDTO(false, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
	        } catch (Exception e) {
	            logger.error("Exception while saving beacon details", e);
	            return new ResponseEntity(new ResponseDTO(false, "Exception occurred while saving beacon details"),
	                    HttpStatus.INTERNAL_SERVER_ERROR);
	        }
	    }
}

