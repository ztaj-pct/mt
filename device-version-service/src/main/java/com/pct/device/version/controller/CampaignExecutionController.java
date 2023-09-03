package com.pct.device.version.controller;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import com.pct.device.version.exception.DeviceInMultipleCampaignsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pct.common.controller.IApplicationController;
import com.pct.common.dto.ResponseBodyDTO;
import com.pct.common.util.JwtUser;
import com.pct.common.util.JwtUtil;
import com.pct.device.version.constant.CampaignStatus;
import com.pct.device.version.dto.MessageDTO;
import com.pct.device.version.exception.BadRequestException;
import com.pct.device.version.exception.BaseMessageException;
import com.pct.device.version.exception.DeviceVersionException;
import com.pct.device.version.model.Campaign;
import com.pct.device.version.payload.CampaignPayload;
import com.pct.device.version.payload.PackagePayload;
import com.pct.device.version.payload.SaveCampaignRequest;
import com.pct.device.version.payload.UpdateCampaignPayload;
import com.pct.device.version.service.ICampaignExecutionService;
import com.pct.device.version.service.ICampaignService;
import com.pct.device.version.util.RestUtils;
import com.pct.device.version.payload.ExecuteCampaignRequest;

@RestController
@RequestMapping("/campaign/execution")
public class CampaignExecutionController implements IApplicationController<Campaign> {

	Logger logger = LoggerFactory.getLogger(CampaignExecutionController.class);
	Logger analysisLog = LoggerFactory.getLogger("analytics");

	private static Integer DEFAULT_PAGE_SIZE = 10000;
	@Autowired
	private ICampaignExecutionService campaignExecution ;
	@Autowired
	private JwtUtil jwtUtil;
	@Autowired
	private RestUtils restutils;
	
	@PutMapping("/manage-status")
	public ResponseEntity<MessageDTO<String>> manageCampaign(
			@RequestParam(value = "campaignUuid", required = false) String campaignUuid,
			@RequestParam(value = "campaignStatus", required = false) String campaignStatus,
			@RequestParam(value = "pauseLimit", required = false) Long pauseLimit,
			HttpServletRequest httpServletRequest) {
		    logger.info("Inside manageCampaign campaignUuid: "+campaignUuid+" campaignStatus: "+campaignStatus +" pauseLimit:"+pauseLimit);		try {
			//Long userId = jwtUtil.getUserIdFromRequest(httpServletRequest);
			JwtUser jwtUser = (JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			String uuid = campaignExecution.manageCampaignStatus(campaignUuid, jwtUser.getUsername(), campaignStatus,pauseLimit);
			return new ResponseEntity<MessageDTO<String>>(
					new MessageDTO<String>("Campaign status changed successfully", uuid, true), HttpStatus.OK);
		} catch (DeviceVersionException e) {
			logger.error("Exception while changing the Campaign status ", e);
			return new ResponseEntity(new ResponseBodyDTO(false, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			logger.error("Exception while changing the Campaign status ", e);
			return new ResponseEntity(new ResponseBodyDTO(false, "Error while changing the Campaign status "),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	
	@PostMapping()
	public ResponseEntity<ResponseBodyDTO> executionCampaign(@RequestBody ExecuteCampaignRequest executeCampaignRequest,
			HttpServletRequest httpServletRequest) {
      long startTIme = System.currentTimeMillis();
		//String uid = UUID.randomUUID().toString();
		String uid = executeCampaignRequest.getUuid();
		if(uid==null || uid.isEmpty()) {
			uid = UUID.randomUUID().toString();
		}
		String deviceID=executeCampaignRequest.getDeviceId()!=null?executeCampaignRequest.getDeviceId():" null ";
		String msgUuid = uid + ", Device ID : "+deviceID;
		try {
			logger.info("Message UUID : " +msgUuid+" Request received for executeCampaignRequest {}", executeCampaignRequest.toString());
			analysisLog.debug("Message UUID : " +msgUuid+" Request received for executeCampaignRequest {}", executeCampaignRequest.toString());
			String atCommand = campaignExecution.processCampaign(executeCampaignRequest, msgUuid);
			if(atCommand != null)
			{
				logger.info("Message UUID : " +msgUuid+ " Found Execute at command MS2  "+ atCommand+" for device "+ executeCampaignRequest.getDeviceId());
			}
			else
			{
				logger.info("Message UUID : " +msgUuid+ " Null Execute at command MS2   "+ atCommand+" for device "+ executeCampaignRequest.getDeviceId());
			}
			analysisLog.debug("Message UUID : " +msgUuid+ " Execute at command "+ atCommand+" for device "+ executeCampaignRequest.getDeviceId());
			restutils.callIA(executeCampaignRequest,msgUuid);
			
			String message = "Campaign processed Successfully ";
			long timeDiff = System.currentTimeMillis() - startTIme;
			logger.info("Message UUID : " +msgUuid+ " campaign request processing Time(ms) MS2  "+ timeDiff);
			return new ResponseEntity(atCommand, HttpStatus.OK);
		} catch (BaseMessageException e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			String exceptionDetails = sw.toString();
			logger.error("Message UUID (1): " +msgUuid+ " Exception while processing Campaign {} ", e.getMessage()+" BaseMessageException exceptionDetails "+exceptionDetails);
			analysisLog.debug("Message UUID (1): " +msgUuid+ " Exception while processing Campaign {} ", e.getMessage()+" BaseMessageException exceptionDetails"+ exceptionDetails);
			return new ResponseEntity(new ResponseBodyDTO(false, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (DeviceVersionException e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			String exceptionDetails = sw.toString();
			logger.error("Message UUID (2): " +msgUuid+ " Exception while processing Campaign {} ", e.getMessage()+" DeviceVersionException exceptionDetails "+exceptionDetails);
			analysisLog.debug("Message UUID (2): " +msgUuid+ " Exception while processing Campaign {} ", e.getMessage()+" DeviceVersionException exceptionDetails "+exceptionDetails);
			return new ResponseEntity(new ResponseBodyDTO(false, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (DeviceInMultipleCampaignsException e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			String exceptionDetails = sw.toString();
			logger.error("Message UUID (3): " +msgUuid+ " Exception while processing Campaign "+ e.getMessage()+ " DeviceInMultipleCampaignsException exceptionDetails "+ exceptionDetails);
			analysisLog.debug("Message UUID (3): " +msgUuid+ " Exception while processing Campaign "+ e.getMessage()+ " DeviceInMultipleCampaignsException exceptionDetails "+ exceptionDetails);
			return new ResponseEntity(new ResponseBodyDTO(false, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			String exceptionDetails = sw.toString();
			logger.error("Message UUID (4): " +msgUuid+ " Exception while processing Campaign "+ e.getMessage()+ " Exception exceptionDetails ");
			analysisLog.debug("Message UUID (4): " +msgUuid+ " Exception while processing Campaign "+ e.getMessage()+ " Exception exceptionDetails ");
			return new ResponseEntity(new ResponseBodyDTO(false, "Exception occurred while processing Campaign"),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	@PutMapping("/manage-campaign-status")
	public ResponseEntity<MessageDTO<String>> manageCampaignStatus(
			@RequestParam(value = "campaignUuid", required = false) String campaignUuid,
			@RequestParam(value = "campaignStatus", required = false) String campaignStatus,
			@RequestParam(value = "pauseLimit", required = false) Long pauseLimit,
			HttpServletRequest httpServletRequest) {
		    logger.info("Inside manageCampaign campaignUuid: "+campaignUuid+" campaignStatus: "+campaignStatus +" pauseLimit:"+pauseLimit);		try {
			//Long userId = jwtUtil.getUserIdFromRequest(httpServletRequest);
			JwtUser jwtUser = (JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			String uuid = campaignExecution.manageCampaignStatus(campaignUuid, jwtUser.getUsername(), campaignStatus,pauseLimit);
			return new ResponseEntity<MessageDTO<String>>(
					new MessageDTO<String>("Campaign status changed successfully", uuid, true), HttpStatus.OK);
		} catch (DeviceVersionException e) {
			logger.error("Exception while changing the Campaign status ", e);
			return new ResponseEntity(new ResponseBodyDTO(false, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			logger.error("Exception while changing the Campaign status ", e);
			return new ResponseEntity(new ResponseBodyDTO(false, "Error while changing the Campaign status "),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
}
