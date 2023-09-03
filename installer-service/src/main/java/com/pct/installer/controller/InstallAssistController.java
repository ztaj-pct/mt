package com.pct.installer.controller;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pct.common.dto.ResponseBodyDTO;
import com.pct.common.dto.ResponseDTO;
import com.pct.common.util.JwtUser;
import com.pct.installer.dto.FinishWorkOrderRequest;
import com.pct.installer.dto.WorkOrderDTO;
import com.pct.installer.service.IInstallAssistService;

@RestController
@RequestMapping("/install-assist")
@CrossOrigin
public class InstallAssistController {
	
	Logger logger = LoggerFactory.getLogger(InstallAssistController.class);
	
	@Autowired
	private IInstallAssistService installAssistService;
	    
	    
	    @PostMapping("/work-order/start")
	    public ResponseEntity<ResponseBodyDTO> saveWorkOrderData(@RequestBody WorkOrderDTO workOrderDTO) {
	        logger.info("Inside saveWorkOrderData Method From InstallAssistController");
	        JwtUser jwtUser = (JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			logger.info("Username : " + jwtUser.getUsername());
			Map<String, Object> resp = new HashMap<>();
	        try {
				
	             String workorder = installAssistService.saveWorkOrder(workOrderDTO,jwtUser.getUsername());
				 resp.put("work_order_uuid", workorder);

				 return new ResponseEntity<>(new ResponseBodyDTO<>(true, "Work Order Saved Successfully",resp), HttpStatus.CREATED);
	        } catch (Exception exception) {
	            logger.error("Exception occurred while saving workOrder", exception);
				return new ResponseEntity<>(new ResponseBodyDTO<>(false, exception.getMessage(), resp),
	                    HttpStatus.INTERNAL_SERVER_ERROR);
	        }
	    }
	   
	    @PostMapping("/work-order/complete")
	    public ResponseEntity<ResponseDTO> finishWorkOrderData(@RequestBody FinishWorkOrderRequest workOrderDTO) {
	        logger.info("Inside finishWorkOrderData Method From InstallAssistController");
	       
	        JwtUser jwtUser = (JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			logger.info("Username : " + jwtUser.getUsername());
	        try {
	            installAssistService.finishWorkOrder(workOrderDTO, jwtUser.getUsername());
	            return new ResponseEntity<ResponseDTO>(new ResponseDTO(true, "Work Order Saved Successfully"), HttpStatus.CREATED);
	        } catch (Exception exception) {
	            logger.error("Exception occurred while saving workOrder", exception);
	            return new ResponseEntity<ResponseDTO>(
	                    new ResponseDTO(false, exception.getMessage()),
	                    HttpStatus.INTERNAL_SERVER_ERROR);
	        }
	    }
}
