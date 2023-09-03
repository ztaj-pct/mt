package com.pct.installer.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pct.common.controller.IApplicationController;
import com.pct.common.dto.InProgressInstall;
import com.pct.common.dto.ResponseBodyDTO;
import com.pct.common.dto.ResponseDTO;
import com.pct.common.dto.TpmsSensorCountDTO;
import com.pct.common.exception.InterServiceRestException;
import com.pct.common.model.InstallHistory;
import com.pct.common.model.User;
import com.pct.common.payload.GatewayDetailsBean;
import com.pct.common.payload.GetInstallHistoryByAssetUuids;
import com.pct.common.payload.InstallationStatusGatewayRequest;
import com.pct.common.payload.InstalledHistroyResponse;
import com.pct.common.util.Context;
import com.pct.common.util.JwtUser;
import com.pct.common.util.JwtUtil;
import com.pct.common.util.Logutils;
import com.pct.installer.dto.InstallationDetailResponseDTO;
import com.pct.installer.dto.InstallationSummaryResponseDTO;
import com.pct.installer.dto.MessageDTO;
import com.pct.installer.dto.SensorInstallInstructionDto;
import com.pct.installer.dto.SensorReasonCodeDto;
import com.pct.installer.entity.LogIssue;
import com.pct.installer.exception.InstallerException;
import com.pct.installer.payload.CreateGatewaySensorAssociation;
import com.pct.installer.payload.DeviceDetailsResponse;
import com.pct.installer.payload.FinishInstallRequest;
import com.pct.installer.payload.GatewayDetailsResponse;
import com.pct.installer.payload.InstallationStatusResponse;
import com.pct.installer.payload.LogIssueBean;
import com.pct.installer.payload.LogIssueGatewayRequest;
import com.pct.installer.payload.LogIssueRequest;
import com.pct.installer.payload.LogIssueStatusRequest;
import com.pct.installer.payload.SensorDetailsResponse;
import com.pct.installer.payload.StartInstallRequest;
import com.pct.installer.payload.UpdateSensorStatusRequest;
import com.pct.installer.payload.UpdateSensorStatusWithInstanceRequest;
import com.pct.installer.service.IInstallationService;
import com.pct.installer.util.RestUtils;
import com.pct.installer.util.Utilities;

/**
 * @author Abhishek on 01/05/20
 */

@RestController
@RequestMapping("/installation")
@CrossOrigin
public class InstallationController implements IApplicationController<InstallHistory> {
	Logger logger = LoggerFactory.getLogger(InstallationController.class);

	boolean status = false;
	String msg = "";
	HttpStatus httpStatus;

	public static final String className = "InstallationController";

	@Autowired
	private IInstallationService installationService;

	@Autowired
	private JwtUtil jwtutil;

	@Autowired
	private RestUtils restUtils;

	@Autowired
	Utilities utilities;

//-------------------------------------Aamir Start----------------------------//
	@PutMapping("/start-install")
	public ResponseEntity<ResponseDTO> startInstall(@Valid @RequestBody StartInstallRequest startInstallRequest,
			HttpServletRequest httpServletRequest) {
		Context context = new Context();
		String logUuId = context.getLogUUId();
		String methodName = "startInstall";

		Logutils.log(className, methodName, context.getLogUUId(), " starting point of start-Installation" + " startInstallRequest : " + startInstallRequest, logger);
		try {

			Logutils.log(className, methodName, context.getLogUUId(),
					" Before calling startInstall method from installation Service method "+ startInstallRequest , logger);
			JwtUser jwtUser = (JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

			User user = restUtils.getUserFromAuthServiceByName(jwtUser.getUsername());

			if (user != null) {
				Logutils.log(className, methodName, logUuId, " username : " + user.getUserName(), logger);
			}

			boolean resStatus = installationService.startInstall(startInstallRequest, user.getId(), logUuId);

			if (resStatus) {
				Logutils.log(className, methodName, context.getLogUUId(), " resStatus " + resStatus, logger);
				status = true;
				msg = "InstallHistory Created Successfully";
				httpStatus = HttpStatus.OK;
			}
			Logutils.log(className, methodName, context.getLogUUId(),
					" Exiting from startInstall Method of InstallController.", logger);
		} catch (Exception e) {
			logger.error("Exception occurred while creating install history", e);
			status = false;
			msg = e.getMessage();
			if (e instanceof Exception)
				if (e instanceof InterServiceRestException)
					httpStatus = ((InterServiceRestException) e).getHttpStatus();
				else if (e instanceof InstallerException)
					httpStatus = HttpStatus.BAD_REQUEST;
				else
					httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
		} finally {
			Logutils.log(className, methodName, context.getLogUUId(), " completed startInstallation", logger);
			return new ResponseEntity<ResponseDTO>(new ResponseDTO(status, msg), httpStatus);
		}
	}

	@GetMapping("/offline-data")
	public ResponseEntity<ResponseBodyDTO<SensorDetailsResponse>> getOfflineData() {
	    	Context context = new Context();
	    	String methodName = "getOfflineData Controller";
	        try {
	            logger.info("Request received to getoffline data");
	            Logutils.log(className,methodName,context.getLogUUId()," before calling getOfflineData method from installation Service method in controller",logger);
	            SensorDetailsResponse sensorDetailsResponse = installationService.getOfflineData(context);
	            Logutils.log(className,methodName,context.getLogUUId()," after calling getOfflineData method from installation Service method in controller",logger);
	            return new ResponseEntity<ResponseBodyDTO<SensorDetailsResponse>>(
	                    new ResponseBodyDTO(true, "Successfully fetched offline data", sensorDetailsResponse),
	                    HttpStatus.OK);
	        } catch (InstallerException exception) {
	            logger.error("Exception occurred while fetching offline data", exception);
	            return new ResponseEntity<ResponseBodyDTO<SensorDetailsResponse>>(
	                    new ResponseBodyDTO(false, exception.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);

		} catch (Exception exception) {
			logger.error("Exception occurred while fetching offline data", exception);
			return new ResponseEntity<ResponseBodyDTO<SensorDetailsResponse>>(
					new ResponseBodyDTO(false, exception.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/core/sensor-count")
	public ResponseEntity<TpmsSensorCountDTO> getSensorCount(@RequestParam("sensor_uuid") String sensorUuid) {
	        logger.info(" Inside getSensorCount Method From InstallerController " + " sensorUuid : " +  sensorUuid);
	        try {
	            TpmsSensorCountDTO count = installationService.getSensorCount(sensorUuid);
	            return new ResponseEntity<>(count, HttpStatus.OK);
	        }catch (Exception exception) {
	            logger.error("Exception occurred while fetching sensor count", exception);
	            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
	        }
	    }

	@GetMapping("/gateway-details-with-pagination")
	public ResponseEntity<ResponseBodyDTO<GatewayDetailsResponse>> getGatewayDetailsWithPagination(

			@RequestParam(name = "install_code", required = false) String installCode,
			@RequestParam(name = "can", required = false) String can,
			@RequestParam(value = "_page", required = false) Integer page,
			@RequestParam(value = "_limit", required = false) Integer pageSize,
			@RequestParam(value = "_sort", required = false) String sort,
			@RequestParam(value = "_order", required = false) String order, HttpServletRequest httpServletRequest) {
//		logger.info(" started getGateway Details With Pagination");
		Context context = new Context();
		String methodName = "getGatewayDetailsWithPagination Controller";
		Logutils.log(className, methodName, context.getLogUUId(), " started getGatewayDetailsWithPagination : " + " installCode : " + installCode,
				logger);
		try {
			MessageDTO<Page<GatewayDetailsBean>> messageDto = new MessageDTO<>("Successfully fetched gateway details",
					true);
			String logUUid = context.getLogUUId();
			logger.info("Request received to get gateway details");
			JwtUser jwtUser = (JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

			User user = restUtils.getUserFromAuthServiceByName(jwtUser.getUsername());
			if (user != null) {
				Logutils.log(className, methodName, logUUid, " username : " + user.getUserName(), logger);

			}
			Logutils.log(className, methodName, context.getLogUUId(),
					" after calling getGatewayDetails method from installation Service method in controller", logger);
			Page<GatewayDetailsBean> gatewayDetailsResponse = installationService.getGatewayDetailsWithPagination(
					logUUid, installCode, can, user.getId(), context, page, pageSize, sort, order);
			messageDto.setBody(gatewayDetailsResponse);
			messageDto.setTotalKey(gatewayDetailsResponse.getTotalElements());
			messageDto.setCurrentPage(gatewayDetailsResponse.getNumber());
			messageDto.setTotal_pages(gatewayDetailsResponse.getTotalPages());
//			logger.info(" completed getGateway Details With Pagination");
			Logutils.log(className, methodName, context.getLogUUId(), " completed getGateway Details With Pagination",
					logger);
			return new ResponseEntity(messageDto, HttpStatus.OK);

		} catch (InstallerException exception) {
			logger.error("Exception occurred while fetching gateway details", exception);
			return new ResponseEntity<ResponseBodyDTO<GatewayDetailsResponse>>(
					new ResponseBodyDTO(false, exception.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);

		} catch (Exception exception) {
			logger.error("Exception occurred while fetching reason gateway details", exception);
			return new ResponseEntity<ResponseBodyDTO<GatewayDetailsResponse>>(
					new ResponseBodyDTO(false, exception.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/log-issue")
	public ResponseEntity<ResponseBodyDTO<List<LogIssueBean>>> getLoggedIssues(@RequestParam(name = "install_code", required = true) String installCode) {
		    	Context context = new Context();
		    	String methodName = "getLoggedIssues Controller";
		    	 logger.info("Inside getloggedissues method : " + " installCode : " + installCode);
		    	try {
		           
		            Logutils.log(className,methodName,context.getLogUUId()," before calling getLoggedIssues method from installation Service method in controller",logger);
		            List<LogIssueBean> logIssueBeanList = installationService.getLoggedIssues(installCode,context);
		            Logutils.log(className,methodName,context.getLogUUId()," after calling getLoggedIssues method from installation Service method in controller",logger);
		            return new ResponseEntity<ResponseBodyDTO<List<LogIssueBean>>>(
		                    new ResponseBodyDTO(true, "Successfully fetched logged issue", logIssueBeanList),
		                    HttpStatus.OK);
		        } catch (InstallerException exception) {
		            logger.error("Exception occurred while fetching logged issue", exception);
		            return new ResponseEntity<ResponseBodyDTO<List<LogIssueBean>>>(
		                    new ResponseBodyDTO(false, exception.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);

		} catch (Exception exception) {
			logger.error("Exception occurred while fetching logged issue", exception);
			return new ResponseEntity<ResponseBodyDTO<List<LogIssueBean>>>(
					new ResponseBodyDTO(false, exception.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PutMapping("/reset")
 	public ResponseEntity<ResponseDTO> resetInstallation(@RequestParam(name = "vin", required = false) String vin,
		                                                         @RequestParam(name = "device_id", required = false) String device_id,
		                                                         @RequestParam(name = "can", required = true) String can) {
		        

		    	 Context context = new Context();
		    	 String logUUid=context.getLogUUId();
		    	 String methodName = "resetInstallation Controller";
		    	 String msg = "";
		    	    logger.info("Inside resetInstallation Method From InstallerController : " + " can : " + can);
		    	 
		        try {
		        	Logutils.log(className,methodName,logUUid," Before calling resetInstallation method from installation Service method in controller",logger);
		        	Boolean status = installationService.resetInstallation(logUUid, vin, device_id, can);
		        	Logutils.log(className,methodName,logUUid," after calling resetInstallation method from installation Service method in controller",logger);
		        	
		        	if(device_id != null) {
		        		msg = "The installation for IMEI " + device_id + " has been reset successfully.";
		        	 }
		        	 else if(vin != null) {
		        		msg = "The installation for VIN " + vin + " has been reset successfully.";
		        	 }
		        	 return new ResponseEntity<ResponseDTO>(new ResponseDTO(status, msg), HttpStatus.OK);
		           
		        } catch (InterServiceRestException e) {
		            logger.error("Exception occurred while resetting installation", e);
		            return new ResponseEntity<ResponseDTO>(
		                    new ResponseDTO(false, e.getMessage()), e.getHttpStatus());
		        } catch (InstallerException installHistoryException) {
		            logger.error("Exception occurred while resetting installation", installHistoryException);
		            return new ResponseEntity<ResponseDTO>(
		                    new ResponseDTO(false, installHistoryException.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		        } catch (Exception exception) {
		            logger.error("Exception occurred while resetting installation", exception);
		            return new ResponseEntity<ResponseDTO>(new ResponseDTO(false, exception.getMessage()),
		                    HttpStatus.INTERNAL_SERVER_ERROR);
		        }
		    }
	 @PutMapping("/status/sensor-instance")
	    public ResponseEntity<ResponseDTO> updateSensorStatusWithInstance(@Valid @RequestBody UpdateSensorStatusWithInstanceRequest updateSensorStatusRequest) {
	        logger.info("Inside updateSensorStatusWithInstance Method From InstallerController : " + " updateSensorStatusRequest : " + updateSensorStatusRequest);
	        Context context = new Context();
	   	    String methodName = "updateSensorStatusWithInstance";
	        try {
	        	String logUUid = context.getLogUUId();
	        	Logutils.log(className,methodName,logUUid,"RequestBody : " + updateSensorStatusRequest,logger);
	        	Logutils.log(className,methodName,logUUid," Before calling updateSensorStatusWithPositioning method from installation Service method in controller",logger);
	            Boolean status = installationService.updateSensorStatusWithPositioning(updateSensorStatusRequest,logUUid);
	            Logutils.log(className,methodName,logUUid," after calling updateSensorStatusWithPositioning method from installation Service method in controller",logger);
	            return new ResponseEntity(new ResponseDTO(status, "Successfully updated Sensor status"), HttpStatus.OK);
	        } catch (InterServiceRestException e) {
	            logger.error("Exception occurred while updating sensor status", e);
	            return new ResponseEntity<ResponseDTO>(
	                    new ResponseDTO(false, e.getMessage()), e.getHttpStatus());
	        } catch (InstallerException installHistoryException) {
	            logger.error("Exception occurred while updating sensor status", installHistoryException);
	            return new ResponseEntity<ResponseDTO>(
	                    new ResponseDTO(false, installHistoryException.getMessage()), HttpStatus.BAD_REQUEST);
	        } catch (Exception exception) {
	            logger.error("Exception occurred while updating sensor status", exception);
	            return new ResponseEntity<ResponseDTO>(new ResponseDTO(false, exception.getMessage()),
	                    HttpStatus.INTERNAL_SERVER_ERROR);
	        }
	    }
	@PutMapping("/finish-install")
    public ResponseEntity<ResponseDTO> finishInstall(@Valid @RequestBody FinishInstallRequest finishInstallRequest,HttpServletRequest httpServletRequest) {
        logger.info(" Inside finishInstall Method From InstallerController : " + "finishInstallRequest : " + finishInstallRequest);
        JwtUser jwtUser = (JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    	User user = restUtils.getUserFromAuthServiceByName(jwtUser.getUsername()); 
        Context context = new Context();
   	    String methodName = "finishInstall";
        try {
        	 Logutils.log(className,methodName,context.getLogUUId()," Before calling finish install method from installation Service method in controller",logger);
            boolean status = installationService.finishInstall(finishInstallRequest,user.getId());
            
            if(status) {
//				try {
//				String deviceId = installationService.checkIsAutoResetInstallationIsApplicableForCurrentInstallation(finishInstallRequest, context);
//					try { 
//						logger.info("deviceID found for reset"+deviceId);
//					installationService.resetGatewayStatus(deviceId);
//					 Logutils.log(className,methodName,context.getLogUUId()," gateway status updated successfully from installer service",logger);
//					} catch (Exception e) {
//						 logger.error("Exception occurred while updating gateway status", e);
//					}
//					} catch (Exception e) {
//					 logger.error("Exception occurred while checkIsAutoResetInstallationIsApplicableForCurrentInstallation", e);
//				}
        	return new ResponseEntity(new ResponseDTO(status, "Installation successfully marked as finished"), HttpStatus.OK);
        } else {
                return new ResponseEntity(new ResponseDTO(status, "Installation  marked as rejected"), HttpStatus.OK);
            }
        } catch (InterServiceRestException e) {
            logger.error("Exception occurred while updating install history", e);
            return new ResponseEntity<ResponseDTO>(
                    new ResponseDTO(false, e.getMessage()), e.getHttpStatus());
        } catch (InstallerException installHistoryException) {
            logger.error("Exception occurred while updating install history", installHistoryException);
            return new ResponseEntity<ResponseDTO>(
                    new ResponseDTO(false, installHistoryException.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (Exception exception) {
            logger.error("Exception occurred while updating install history", exception);
            return new ResponseEntity<ResponseDTO>(new ResponseDTO(false, exception.getMessage()),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
	//-------------------------------------Aamir End----------------------------//
//	 @PutMapping("/finish-install")
//	    public ResponseEntity<ResponseDTO> finishInstall(@Valid @RequestBody FinishInstallRequest finishInstallRequest,HttpServletRequest httpServletRequest) {
//	        logger.info("Inside finishInstall Method From InstallerController");
//	        Long userId = jwtutil.getUserIdFromRequest(httpServletRequest);
//	        Context context = new Context();
//	   	    String methodName = "finishInstall";
//	        try {
//	        	 Logutils.log(className,methodName,context.getLogUUId()," Before calling finish install method from installation Service method in controller",logger);
//	            boolean status = installationService.finishInstall(finishInstallRequest,context,userId);
//	            
//	            if(status) {
//					/*try {
//					String deviceId = installationService.checkIsAutoResetInstallationIsApplicableForCurrentInstallation(finishInstallRequest, context);
//						try { 
//							logger.info("deviceID found for reset"+deviceId);
//						installationService.resetGatewayStatus(deviceId);
//						 Logutils.log(className,methodName,context.getLogUUId()," gateway status updated successfully from installer service",logger);
//						} catch (Exception e) {
//							 logger.error("Exception occurred while updating gateway status", e);
//						}
//						} catch (Exception e) {
//						 logger.error("Exception occurred while checkIsAutoResetInstallationIsApplicableForCurrentInstallation", e);
//					}*/
//	            	return new ResponseEntity(new ResponseDTO(status, "Installation successfully marked as finished"), HttpStatus.OK);
//	            } else {
//	                return new ResponseEntity(new ResponseDTO(status, "Installation  marked as rejected"), HttpStatus.OK);
//	            }
//	        } catch (InterServiceRestException e) {
//	            logger.error("Exception occurred while updating install history", e);
//	            return new ResponseEntity<ResponseDTO>(
//	                    new ResponseDTO(false, e.getMessage()), e.getHttpStatus());
//	        } catch (InstallerException installHistoryException) {
//	            logger.error("Exception occurred while updating install history", installHistoryException);
//	            return new ResponseEntity<ResponseDTO>(
//	                    new ResponseDTO(false, installHistoryException.getMessage()), HttpStatus.BAD_REQUEST);
//	        } catch (Exception exception) {
//	            logger.error("Exception occurred while updating install history", exception);
//	            return new ResponseEntity<ResponseDTO>(new ResponseDTO(false, exception.getMessage()),
//	                    HttpStatus.INTERNAL_SERVER_ERROR);
//	        }
//	    }
//
	@PutMapping("/status/sensor")
	public ResponseEntity<ResponseDTO> updateSensorStatus(
			@Valid @RequestBody UpdateSensorStatusRequest updateSensorStatusRequest) {
//		logger.info(" started update Sensor Status");
		Logutils.log(className, " started updateSensorStatus " + " updateSensorStatusRequest : " + updateSensorStatusRequest , logger);
		try {
			Logutils.log(className,
					" Before calling updateSensorStatus method from installation Service method in controller", logger);
			Boolean status = installationService.updateSensorStatus(updateSensorStatusRequest);
			return new ResponseEntity(new ResponseDTO(status, "Successfully updated Sensor status"), HttpStatus.OK);
		} catch (InterServiceRestException e) {
			logger.error("Exception occurred while updating sensor status", e);
			return new ResponseEntity<ResponseDTO>(new ResponseDTO(false, e.getMessage()), e.getHttpStatus());
		} catch (InstallerException installHistoryException) {
			logger.error("Exception occurred while updating sensor status", installHistoryException);
			return new ResponseEntity<ResponseDTO>(new ResponseDTO(false, installHistoryException.getMessage()),
					HttpStatus.BAD_REQUEST);
		} catch (Exception exception) {
			logger.error("Exception occurred while updating sensor status", exception);
			return new ResponseEntity<ResponseDTO>(new ResponseDTO(false, exception.getMessage()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PutMapping("/status/gateway")
	public ResponseEntity<ResponseDTO> updateGatewayInstallStatus(
			@Valid @RequestBody InstallationStatusGatewayRequest installationStatusGatewayRequest) {
//		logger.info(" started update Gateway InstallStatus");

		Context context = new Context();
		String methodName = " updateGatewayInstallStatus Controller ";
		String logUUid = context.getLogUUId();
		Logutils.log(className, methodName, logUUid, " started updateGatewayInstallStatus : " + " installationStatusGatewayRequest : " + installationStatusGatewayRequest, logger);

		try {
			Logutils.log(className, methodName, logUUid,
					" Before calling updateGatewayInstallStatus method from installation Service method in controller",
					logger);
//        	Logutils.log(className,methodName, context.getLogUUId(), "Before calling updateGatewayInstallStatus method from deviceService", context.getLogUUId(), InstallationStatusGatewayRequest.class);
			Boolean status = installationService.updateGatewayInstallStatus(installationStatusGatewayRequest);
//            Logutils.log(className,methodName, context.getLogUUId(), "After calling updateGatewayInstallStatus method from deviceService", context.getLogUUId(), InstallationStatusGatewayRequest.class);
			return new ResponseEntity<ResponseDTO>(new ResponseDTO(status, "Updated gateway installation successfully"),
					HttpStatus.OK);
		} catch (InterServiceRestException e) {
			logger.error("Exception occurred while updating gateway install status", e);
			return new ResponseEntity<ResponseDTO>(new ResponseDTO(false, e.getMessage()), e.getHttpStatus());
		} catch (InstallerException installHistoryException) {
			logger.error("Exception occurred while updating gateway install status", installHistoryException);
			return new ResponseEntity<ResponseDTO>(new ResponseDTO(false, installHistoryException.getMessage()),
					HttpStatus.BAD_REQUEST);
		} catch (Exception exception) {
			logger.error("Exception occurred while updating gateway install status", exception);
			return new ResponseEntity<ResponseDTO>(new ResponseDTO(false, exception.getMessage()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

//
	@GetMapping("/status")
	public ResponseEntity<ResponseBodyDTO<InstallationStatusResponse>> installationStatus(
			@RequestParam("install_uuid") String installUuid) {
//		logger.info(" started installation Status");

		Logutils.log(className, " started installationStatus : " + " installUuid : " + installUuid, logger);
		try {
			Logutils.log(className,
					" Before calling getinstallationstatus method from installation Service method in controller",
					logger);
			InstallationStatusResponse installationStatusResponse = installationService
					.getInstallationStatus(installUuid);
			return new ResponseEntity<ResponseBodyDTO<InstallationStatusResponse>>(
					new ResponseBodyDTO(true, "Successfully fetched installation status", installationStatusResponse),
					HttpStatus.OK);
		} catch (InterServiceRestException e) {
			logger.error("Exception occurred while fetching installation status", e);
			return new ResponseEntity<ResponseBodyDTO<InstallationStatusResponse>>(
					new ResponseBodyDTO(false, e.getMessage()), e.getHttpStatus());
		} catch (InstallerException installHistoryException) {
			logger.error("Exception occurred while fetching installation status", installHistoryException);
			return new ResponseEntity<ResponseBodyDTO<InstallationStatusResponse>>(
					new ResponseBodyDTO(false, installHistoryException.getMessage()), HttpStatus.BAD_REQUEST);
		} catch (Exception exception) {
			logger.error("Exception occurred while fetching installation status", exception);
			return new ResponseEntity<ResponseBodyDTO<InstallationStatusResponse>>(
					new ResponseBodyDTO(false, exception.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PutMapping("/mark-installation-for-rework")
	public ResponseEntity<ResponseDTO> markInstallationInProgress(
			@RequestParam(name = "install_code", required = true) String installCode,
			HttpServletRequest httpServletRequest) {
//		logger.info(" started mark installation inProgress");
		Context context = new Context();
		String methodName = "markInstallationInProgress Controller";
		Logutils.log(className, methodName, context.getLogUUId(), " started markinstallationinProgress : " + " installCode : " + installCode, logger);

		JwtUser jwtUser = (JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		User user = restUtils.getUserFromAuthServiceByName(jwtUser.getUsername());
		if (user != null) {
			Logutils.log(className, methodName, context.getLogUUId(), " username : " + user.getUserName(), logger);
		}
		try {
			Logutils.log(className, methodName, context.getLogUUId(),
					" Before calling markInstallationInProgress method from installation Service method in controller and install code : "
							+ installCode,
					logger);
			// Long userId = jwtutil.getUserIdFromRequest(httpServletRequest);
			Boolean status = installationService.markInstallationToInProgress(installCode, user.getId(), context);
			Logutils.log(className, methodName, context.getLogUUId(),
					" after calling markInstallationInProgress method from installation Service method in controller",
					logger);

			return new ResponseEntity<ResponseDTO>(new ResponseDTO(status,
					"Installation for install code " + installCode + " has been marked In progress successfully."),
					HttpStatus.OK);
		} catch (InterServiceRestException e) {
			logger.error("Exception occurred while updateInstallation installation", e);
			return new ResponseEntity<ResponseDTO>(new ResponseDTO(false, e.getMessage()), e.getHttpStatus());
		} catch (InstallerException installHistoryException) {
			logger.error("Exception occurred while updateInstallation installation", installHistoryException);
			return new ResponseEntity<ResponseDTO>(new ResponseDTO(false, installHistoryException.getMessage()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Exception exception) {
			logger.error("Exception occurred while updateInstallation installation", exception);
			return new ResponseEntity<ResponseDTO>(new ResponseDTO(false, exception.getMessage()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/in-progress")
	public ResponseEntity<ResponseBodyDTO<List<InProgressInstall>>> getInProgressInstall(
			HttpServletRequest httpServletRequest, @RequestParam(value = "can", required = true) String accountNumber) {
//		logger.info(" started getting InProgress Install");
		Context context = new Context();
		String logUUid = context.getLogUUId();
		String methodName = "getInProgressInstall Controller";
		Logutils.log(className, methodName, context.getLogUUId(), " started  getInProgressInstall : " + " accountNumber : " + accountNumber, logger);

		try {
			logger.info("Request received for can : " + accountNumber);
			Logutils.log(className, methodName, logUUid,
					" Before calling getInProgressInstall method from installation Service method in controller",
					logger);
			List<InProgressInstall> inProgressInstallList = installationService.getInProgressInstall(accountNumber,
					logUUid);
			Logutils.log(className, methodName, logUUid,
					" after calling getInProgressInstall method from installation Service method in controller",
					logger);
			return new ResponseEntity<ResponseBodyDTO<List<InProgressInstall>>>(
					new ResponseBodyDTO<List<InProgressInstall>>(true, "Fetched in progress installs successfully",
							inProgressInstallList),
					HttpStatus.OK);
		} catch (Exception exception) {
			logger.error("Exception occurred while getting gateway(s)", exception);
			return new ResponseEntity<ResponseBodyDTO<List<InProgressInstall>>>(
					new ResponseBodyDTO<List<InProgressInstall>>(false, exception.getMessage(), null),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/in-progress-installation-v2")
	public ResponseEntity<ResponseBodyDTO<Page<InProgressInstall>>> getInProgressInstallV2(
			HttpServletRequest httpServletRequest, @RequestParam(value = "can", required = true) String accountNumber,
			@RequestParam(value = "_page", required = false) Integer page,
			@RequestParam(value = "_limit", required = false) Integer size,
			@RequestParam(value = "_sort", required = false) String sort,
			@RequestParam(value = "_order", required = false) String order) {
//		logger.info(" started getting getting InProgress Install V2");
		Context context = new Context();
		String logUUid = context.getLogUUId();
		String methodName = "getInProgressInstallV2 Controller";
		Logutils.log(className, methodName, logUUid, " started getInProgressInstallV2 : " + " accountNumber : " + accountNumber, logger);

		try {
			logger.info("Request received for can : " + accountNumber);
			Logutils.log(className, methodName, logUUid,
					" Before calling getInProgressInstall method from installation Service method in controller",
					logger);
			Page<InProgressInstall> inProgressInstallList = installationService.getInProgressInstallV2(accountNumber,
					logUUid, getPageable(page - 1, size, sort, order));
			Logutils.log(className, methodName, logUUid,
					" after calling getInProgressInstall method from installation Service method in controller",
					logger);
			return new ResponseEntity<ResponseBodyDTO<Page<InProgressInstall>>>(
					new ResponseBodyDTO<Page<InProgressInstall>>(true, "Fetched in progress installs successfully",
							inProgressInstallList),
					HttpStatus.OK);
		} catch (Exception exception) {
			logger.error("Exception occurred while getting gateway(s)", exception);
			return new ResponseEntity<ResponseBodyDTO<Page<InProgressInstall>>>(
					new ResponseBodyDTO<Page<InProgressInstall>>(false, exception.getMessage(), null),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PostMapping("/log-issue")
	public ResponseEntity<ResponseDTO> logIssue(@Valid @RequestBody LogIssueRequest logIssueRequest,
			HttpServletRequest httpServletRequest) {
//		logger.info(" started logIssue installation");
		Context context = new Context();
		String logUUid = context.getLogUUId();
		String methodName = "logIssue Controller";
		Logutils.log(className, methodName, logUUid, " started logIssue installation : " + " logIssueRequest : " + logIssueRequest, logger);
		try {
			logger.info("Request received for log issue");
			JwtUser jwtUser = (JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			User user = restUtils.getUserFromAuthServiceByName(jwtUser.getUsername());
			if (user != null) {
				Logutils.log(className, methodName, logUUid, " username : " + user.getUserName(), logger);
			}

			Logutils.log(className, methodName, logUUid,
					" Before calling logIssue method from installation Service method in controller", logger);
			LogIssue logIssue = installationService.logIssue(logIssueRequest, user.getId(), logUUid);
			Logutils.log(className, methodName, logUUid,
					" after calling logIssue method from installation Service method in controller", logger);
			return new ResponseEntity<ResponseDTO>(new ResponseDTO(true, "The issue was submitted successfully."),
					HttpStatus.OK);
		} catch (InstallerException exception) {
			logger.error("Exception occurred while logging issue", exception);
			return new ResponseEntity<ResponseDTO>(new ResponseDTO(false, exception.getMessage()),
					HttpStatus.INTERNAL_SERVER_ERROR);

		} catch (Exception exception) {
			logger.error("Exception occurred while logging issue", exception);
			return new ResponseEntity<ResponseDTO>(new ResponseDTO(false, exception.getMessage()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PutMapping("/status/log-issue")
	public ResponseEntity<ResponseDTO> updateLogIssueStatus(
			@Valid @RequestBody LogIssueStatusRequest logIssueStatusRequest, HttpServletRequest httpServletRequest) {
//		logger.info(" started update LogIssue Status");
		Logutils.log(className, " started updateLogIssueStatus : " + " logIssueStatusRequest : " + logIssueStatusRequest, logger);
		try {
			JwtUser jwtUser = (JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			User user = restUtils.getUserFromAuthServiceByName(jwtUser.getUsername());
			if (user != null) {
				Logutils.log(className, " username : " + user.getUserName(), logger);
			}

			Boolean responseStatus = installationService.updateLogIssueStatus(logIssueStatusRequest, user.getId());
			return new ResponseEntity<ResponseDTO>(new ResponseDTO(responseStatus, "Updated Log issue successfully"),
					HttpStatus.OK);
		} catch (InterServiceRestException e) {
			logger.error("Exception occurred while updating Log issue  status", e);
			return new ResponseEntity<ResponseDTO>(new ResponseDTO(false, e.getMessage()), e.getHttpStatus());
		} catch (InstallerException installerException) {
			logger.error("Exception occurred while updating Log issue status", installerException);
			return new ResponseEntity<ResponseDTO>(new ResponseDTO(false, installerException.getMessage()),
					HttpStatus.BAD_REQUEST);
		} catch (Exception exception) {
			logger.error("Exception occurred while updating Log issue status", exception);
			return new ResponseEntity<ResponseDTO>(new ResponseDTO(false, exception.getMessage()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PostMapping("/page")
	// @Secured(AuthoritiesConstants.SUPER_ADMIN)
	public ResponseEntity<Object> getAllInstallationSummary(
			@RequestParam(value = "_page", required = false) Integer page,
			@RequestParam(value = "_type", required = false) String type,
			@RequestParam(value = "_limit", required = false) Integer size,
			@RequestParam(value = "_sort", required = false) String sort,
			@RequestParam(value = "_order", required = false) String order,
			@RequestParam(value = "_companyUuid", required = false) String companyUuid,
			@RequestParam(value = "_days", required = false) Integer days,
			@RequestBody Map<String, String> filterValues, HttpServletRequest httpServletRequest) {
//		logger.info(" started getAll Installation Summary");
		Logutils.log(className, " started getAllInstallationSummary : " + " companyUuid : " + companyUuid, logger);
		MessageDTO<Page<InstallationSummaryResponseDTO>> messageDto = new MessageDTO<>(
				"Installation Summary Fetched Successfully", true);
		// Long userId = jwtutil.getUserIdFromRequest(httpServletRequest);
		JwtUser jwtUser = (JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		User user = restUtils.getUserFromAuthServiceByName(jwtUser.getUsername());
		if (user != null) {
			Logutils.log(className, " username : " + user.getUserName(), logger);
		}
		Page<InstallationSummaryResponseDTO> users = installationService.getAllInstallationSummary(
				getPageable(page - 1, size, sort, order), companyUuid, user.getId(), filterValues, Long.valueOf(days));
		messageDto.setBody(users);
		messageDto.setTotalKey(users.getTotalElements());
		messageDto.setCurrentPage(users.getNumber());
		messageDto.setTotal_pages(users.getTotalPages());
		Logutils.log(className, " completed getall Installation Summary", logger);
		return new ResponseEntity(messageDto, HttpStatus.OK);

	}

	@PostMapping("/exportExcelForInstallationSummary")
	// @Secured(AuthoritiesConstants.SUPER_ADMIN)
	public void exportExcelForInstallationSummary(@RequestParam(value = "_page", required = false) Integer page,
			@RequestParam(value = "_type", required = false) String type,
			@RequestParam(value = "_limit", required = false) Integer size,
			@RequestParam(value = "_sort", required = false) String sort,
			@RequestParam(value = "_order", required = false) String order,
			@RequestParam(value = "_companyUuid", required = false) String companyUuid,
			@RequestParam(value = "_days", required = false) Integer days,
			@RequestBody Map<String, String> filterValues, HttpServletRequest httpServletRequest,
			HttpServletResponse response) {
//		logger.info(" started export Excel ForInstallation Summary");
		Logutils.log(className, " started exportExcelForInstallationSummary : " + " companyUuid : " + companyUuid, logger);
		JwtUser jwtUser = (JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		User user = restUtils.getUserFromAuthServiceByName(jwtUser.getUsername());
		if (user != null) {
			Logutils.log(className, " username : " + user.getUserName(), logger);
		}
		Workbook workbook = null;
		try {

			workbook = installationService.exportExcelForInstallationSummary(getPageable(page - 1, size, sort, order),
					companyUuid, user.getId(), filterValues, Long.valueOf(days));
			workbook.write(response.getOutputStream());

		} catch (Exception e) {
			response.setHeader("errorMessage", e.getMessage());
			logger.error(
					"Exception in exportToExcel Of Contact Person Controller and Exception is : " + e.getMessage());
		} finally {
			try {
				if (workbook != null) {
					workbook.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		logger.info("Exiting from exportExcelForInstallationSummary() method of Installation Controller");

		response.setContentType("application/pdf");
		response.setHeader("Content-Disposition", "attachment; filename=" + "Installation Summary Report" + ".xlsx");
		response.setHeader("filename", "Installation Summary Report" + ".xlsx");

	}

	@GetMapping("/insallation-details")
	public ResponseEntity<InstallationDetailResponseDTO> getAllInstallationDetails(
			@RequestParam(value = "deviceUUid", required = true) String deviceUUid,
			HttpServletRequest httpServletRequest) {
		Context context = new Context();
		String logUUid = context.getLogUUId();
		Logutils.log(className, logUUid, " started  getAllInstallationDetails : " + " deviceUUid : " + deviceUUid, logger);
		try {

			InstallationDetailResponseDTO details = installationService.getInstallHistoryByDeviceUUid(logUUid,
					deviceUUid);
			return new ResponseEntity<InstallationDetailResponseDTO>(details, HttpStatus.OK);
		} catch (Exception exception) {
			logger.error("Exception occurred while resetting installation", exception);
			return new ResponseEntity<InstallationDetailResponseDTO>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	// -------------------- Aamir 1 Start
	// -----------------------------------------------//

	@PostMapping("/reset-company")
	public ResponseEntity<ResponseDTO> resetCompanyData(@RequestParam("company_uuid") String companyUuid) {
//		logger.info("Inside resetCompanyData Method From InstallerController for company UUID {}", companyUuid);
		Context context = new Context();
		String methodName = "resetCompanyData Controller";
		Logutils.log(className, methodName, " started resetCompanyData : " + " companyUuid : " + companyUuid, logger);
		System.out.println("reset company " + companyUuid);
		try {
			Logutils.log(className, methodName, context.getLogUUId(),
					" Before calling resetCompanyData method from installation Service method in controller", logger);
			Boolean status = installationService.resetCompanyData(companyUuid, context);
			Logutils.log(className, methodName, context.getLogUUId(),
					" after calling resetCompanyData method from installation Service method in controller", logger);
			return new ResponseEntity<ResponseDTO>(
					new ResponseDTO(status, "Successfully deleted InstallHistory and InstallLog for company uuid"),
					HttpStatus.OK);
		} catch (Exception exception) {
			logger.error("Exception occurred while resetting installation", exception);
			return new ResponseEntity<ResponseDTO>(new ResponseDTO(false, exception.getMessage()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PostMapping("/core/reset-gateway")
	public ResponseEntity<ResponseDTO> resetGatewayData(@RequestParam("device_uuid") String deviceUuid) {
//		logger.info("Inside resetGatewayData Method From InstallerController for gateway UUID {}", deviceUuid);
		Logutils.log(className, " started resetGatewayData" + " deviceUuid " + deviceUuid, logger);
		try {
			Boolean status = installationService.resetGatewayData(deviceUuid);
			return new ResponseEntity<ResponseDTO>(
					new ResponseDTO(status, "Successfully deleted InstallHistory and InstallLog for gateway uuid"),
					HttpStatus.OK);
		} catch (Exception exception) {
			logger.error("Exception occurred while resetting installation", exception);
			return new ResponseEntity<ResponseDTO>(new ResponseDTO(false, exception.getMessage()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PostMapping("/log-issue-gateway")
	public ResponseEntity<ResponseDTO> logIssueForGateway(@Valid @RequestBody LogIssueGatewayRequest logIssueRequest,
			HttpServletRequest httpServletRequest) {
		Context context = new Context();
		String methodName = "logIssueForGateway Controller";
		Logutils.log(className, methodName, " started logIssueForGateway : " + " logIssueRequest : " + logIssueRequest, logger);
		try {
			logger.info("Request received for log issue");
			JwtUser jwtUser = (JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			User user = restUtils.getUserFromAuthServiceByName(jwtUser.getUsername());
			if (user != null) {
				Logutils.log(className, methodName, context.getLogUUId(), " username : " + user.getUserName(), logger);
			}
			Logutils.log(className, methodName, context.getLogUUId(),
					" Before calling logIssueForGateway method from installation Service method in controller", logger);
			LogIssue logIssue = installationService.logIssueForGateway(logIssueRequest, user.getId(), context);
			Logutils.log(className, methodName, context.getLogUUId(),
					" after calling logIssueForGateway method from installation Service method in controller", logger);
			return new ResponseEntity<ResponseDTO>(new ResponseDTO(true, "The issue was submitted successfully."),
					HttpStatus.OK);
		} catch (InstallerException exception) {
			logger.error("Exception occurred while logging issue", exception);
			return new ResponseEntity<ResponseDTO>(new ResponseDTO(false, exception.getMessage()),
					HttpStatus.INTERNAL_SERVER_ERROR);

        } catch (Exception exception) {
            logger.error("Exception occurred while logging issue", exception);
            return new ResponseEntity<ResponseDTO>(
                    new ResponseDTO(false, exception.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    
    @GetMapping("/gateway-details")
    public ResponseEntity<ResponseBodyDTO<DeviceDetailsResponse>> getGatewayDetails(@RequestParam(name = "install_code", required = false) String installCode,@RequestParam(name = "can", required = false) String can,HttpServletRequest httpServletRequest) {
    	 Context context = new Context();
    	String logUUid = context.getLogUUId();
    	DeviceDetailsResponse gatewayDetailsResponse = null; ;
    	 String methodName = "getGatewayDetails Controller";
    	try {
            logger.info("Request received to getatewaydetails");
           // Long userId = jwtutil.getUserIdFromRequest(httpServletRequest);
            JwtUser jwtUser = (JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        	User user = restUtils.getUserFromAuthServiceByName(jwtUser.getUsername()); 	
            Logutils.log(className,methodName,context.getLogUUId()," after calling getGatewayDetails method from installation Service method in controller",logger);
            gatewayDetailsResponse = installationService.getGatewayDetails(installCode,can,user.getId(),logUUid);
            if (gatewayDetailsResponse != null) {
				status = true;
				msg = "InstallHistory Created Successfully";
				httpStatus = HttpStatus.OK;
			}
			Logutils.log(className, methodName, context.getLogUUId(),
					" after calling getGatewayDetails method from installation Service method in controller", logger);

		} catch (Exception e) {
			logger.error("Exception occurred while creating install history", e);
			status = false;
			msg = e.getMessage();
			if (e instanceof Exception)
				if (e instanceof InterServiceRestException)
					httpStatus = ((InterServiceRestException) e).getHttpStatus();
				else if (e instanceof InstallerException)
					httpStatus = HttpStatus.BAD_REQUEST;
				else
					httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
		} finally {
			return new ResponseEntity<ResponseBodyDTO<DeviceDetailsResponse>>(
					new ResponseBodyDTO(status, msg, gatewayDetailsResponse), HttpStatus.OK);
			// return new ResponseEntity<ResponseDTO>(new ResponseDTO(status, msg),
			// httpStatus);
		}

	}

	@PostMapping("/core/asset")
	public ResponseEntity<List<InstallHistory>> getInstallHistoryByAsset(
			@RequestBody GetInstallHistoryByAssetUuids getInstallHistoryByAssetUuids) {
		logger.info("Inside getInstallHistoryByAsset Method From InstallerController : " + getInstallHistoryByAssetUuids);
		try {
			List<InstallHistory> installHistoryByAssetUuids = installationService
					.getInstallHistoryByAssetUuids(getInstallHistoryByAssetUuids);
			Logutils.log(className, " completed getInstall History ByAsset", logger);
			return new ResponseEntity<List<InstallHistory>>(installHistoryByAssetUuids, HttpStatus.OK);
		} catch (Exception exception) {
			logger.error("Exception occurred while resetting installation", exception);
			return new ResponseEntity<List<InstallHistory>>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/sensor-details")
	public ResponseEntity<ResponseBodyDTO<SensorDetailsResponse>> getSensorDetails(
			@RequestParam(name = "install_code", required = true) String installCode) {
		Context context = new Context();
		String methodName = "getSensorDetails Controller";
		Logutils.log(className, methodName, context.getLogUUId(), " started getSensorDetails : " + " installCode : " + installCode, logger);
		try {
			logger.info("Request received for getReasonCodes");
			Logutils.log(className, methodName, context.getLogUUId(),
					" before calling getSensorDetails method from installation Service method in controller", logger);
			SensorDetailsResponse sensorDetailsResponse = installationService.getSensorDetails(installCode, context);
			Logutils.log(className, methodName, context.getLogUUId(),
					" after calling getSensorDetails method from installation Service method in controller", logger);
			return new ResponseEntity<ResponseBodyDTO<SensorDetailsResponse>>(
					new ResponseBodyDTO<SensorDetailsResponse>(true, "Successfully fetched reason codes",
							sensorDetailsResponse),
					HttpStatus.OK);
		} catch (InstallerException exception) {
			logger.error("Exception occurred while getting reason codes", exception);
			return new ResponseEntity<ResponseBodyDTO<SensorDetailsResponse>>(
					new ResponseBodyDTO<SensorDetailsResponse>(false, exception.getMessage()),
					HttpStatus.INTERNAL_SERVER_ERROR);

		} catch (Exception exception) {
			logger.error("Exception occurred while getting reason codes", exception);
			return new ResponseEntity<ResponseBodyDTO<SensorDetailsResponse>>(
					new ResponseBodyDTO<SensorDetailsResponse>(false, exception.getMessage()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/page/finished-installations")
	public ResponseEntity<ResponseBodyDTO<InProgressInstall>> getFinishedInstallationsWithPagination(
			@RequestParam(value = "_page", required = true) Integer page,
			@RequestParam(value = "_limit", required = true) Integer size,
			@RequestParam(value = "_sort", required = false) String sort,
			@RequestParam(value = "_order", required = false) String order,
			@RequestParam(value = "can", required = false) String accountNumber,
			HttpServletRequest httpServletRequest) {
//		logger.info("Inside getFinishedInstallationsWithPagination Method From InstallerController");
		Logutils.log(className, " started getFinishedInstallationsWithPagination : " + " accountNumber : " + accountNumber, logger);

		if (sort == null || sort == "") {
			sort = "createdOn";
			order = "DESC";
		}
		Page<InProgressInstall> finishedInstallations = null;
		String message = "Finished Installations Fetched Successfully";
		boolean flag = false;

		try {
			finishedInstallations = installationService
					.getFinishedInstallationsWithPagination(getPageable(page - 1, size, sort, order), accountNumber);
			flag = true;
		} catch (Exception e) {
			message = "No Data Found";
			logger.error("Exception occurred metthod getFinishedInstallationsWithPagination", e);
		}

		MessageDTO<Page<InProgressInstall>> messageDto = new MessageDTO<>(message, flag);
		if (finishedInstallations != null) {
			messageDto.setBody(finishedInstallations);
			messageDto.setTotalKey(finishedInstallations.getTotalElements());
			messageDto.setCurrentPage(finishedInstallations.getNumber());
			messageDto.setTotal_pages(finishedInstallations.getTotalPages());
		}
		logger.info("Exiting from getFinishedInstallationsWithPagination Method From InstallerController");
		return new ResponseEntity(messageDto, HttpStatus.OK);

		}
	  
	  
	  
    // -------------------- Aamir 1 End -----------------------------------------------//  
    
	  @GetMapping("/gateway-details-with-pagination-new")
		public ResponseEntity<ResponseBodyDTO<GatewayDetailsResponse>> getGatewayDetailsWithPaginationNew(
				@RequestParam(name = "install_code", required = false) String installCode,
				@RequestParam(name = "can", required = false) String can,
				@RequestParam(value = "_page", required = false) Integer page,
				@RequestParam(value = "_limit", required = false) Integer pageSize,
				@RequestParam(value = "_sort", required = false) String sort,
				@RequestParam(value = "_order", required = false) String order,
				@RequestParam(value = "time_of_last_download", required = false) String timeOfLastDownload,
				HttpServletRequest httpServletRequest) {
			Context context = new Context();
			String methodName = "getGatewayDetailsWithPaginationNew Controller";
			Logutils.log(className, methodName, context.getLogUUId(), " started getGatewayDetailsWithPaginationNew : " + "install code : "+ installCode + "time of last download : " + timeOfLastDownload ,
					logger);
			try {
				MessageDTO<Page<GatewayDetailsBean>> messageDto = new MessageDTO<>("Successfully fetched gateway details",
						true);

			logger.info("Request received to get gateway details");
//				Long userId = jwtutil.getUserIdFromRequest(httpServletRequest);
				
				 JwtUser jwtUser = (JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		        	User user = restUtils.getUserFromAuthServiceByName(jwtUser.getUsername());
				Logutils.log(className, methodName, context.getLogUUId(),
						" after calling getGatewayDetails method from installation Service method in controller", logger);
				Page<GatewayDetailsBean> gatewayDetailsResponse = installationService.getGatewayDetailsWithPaginationNew(
						installCode, can, user.getId(), context, page, pageSize, sort, order, timeOfLastDownload);
				messageDto.setBody(gatewayDetailsResponse);
				messageDto.setTotalKey(gatewayDetailsResponse.getTotalElements());
				messageDto.setCurrentPage(gatewayDetailsResponse.getNumber());
				messageDto.setTotal_pages(gatewayDetailsResponse.getTotalPages());
				return new ResponseEntity(messageDto, HttpStatus.OK);

		} catch (InstallerException exception) {
			logger.error("Exception occurred while fetching gateway details", exception);
			return new ResponseEntity<ResponseBodyDTO<GatewayDetailsResponse>>(
					new ResponseBodyDTO(false, exception.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);

		} catch (Exception exception) {
			logger.error("Exception occurred while fetching reason gateway details", exception);
			return new ResponseEntity<ResponseBodyDTO<GatewayDetailsResponse>>(
					new ResponseBodyDTO(false, exception.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/gateway-details-with-pagination-v2")
	public ResponseEntity<ResponseBodyDTO<GatewayDetailsResponse>> getGatewayDetailsWithPaginationV2(
			@RequestParam(name = "install_code", required = false) String installCode,
			@RequestParam(name = "can", required = false) String can,
			@RequestParam(value = "_page", required = false) Integer page,
			@RequestParam(value = "_limit", required = false) Integer pageSize,
			@RequestParam(value = "_sort", required = false) String sort,
			@RequestParam(value = "_order", required = false) String order,
			@RequestParam(value = "time_of_last_download", required = false) String timeOfLastDownload,
			HttpServletRequest httpServletRequest) {
		Context context = new Context();
		
		String methodName = "getGatewayDetailsWithPaginationV2 Controller";
		Logutils.log(className, methodName, context.getLogUUId(), " started getGatewayDetailsWithPaginationV2 : " + "install code : "+ installCode + "time of last download : " + timeOfLastDownload ,
				logger);
		try {
			MessageDTO<Page<GatewayDetailsBean>> messageDto = new MessageDTO<>("Successfully fetched gateway details",
					true);

			logger.info("Request received to get gateway details");
//				Long userId = jwtutil.getUserIdFromRequest(httpServletRequest);

			JwtUser jwtUser = (JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			User user = restUtils.getUserFromAuthServiceByName(jwtUser.getUsername());
			if (user != null) {
				Logutils.log(className, methodName, " username : " + user.getUserName(), logger);
			}
			Logutils.log(className, methodName, context.getLogUUId(),
					" after calling getGatewayDetailsWithPaginationV2 method from installation Service method in controller", logger);
			Page<GatewayDetailsBean> gatewayDetailsResponse = installationService
					.getGatewayDetailsWithPaginationNewTesting(installCode, can, user.getId(), context, page, pageSize,
							sort, order, timeOfLastDownload);
			messageDto.setBody(gatewayDetailsResponse);
			messageDto.setTotalKey(gatewayDetailsResponse.getTotalElements());
			messageDto.setCurrentPage(gatewayDetailsResponse.getNumber());
			messageDto.setTotal_pages(gatewayDetailsResponse.getTotalPages());
			return new ResponseEntity(messageDto, HttpStatus.OK);

		} catch (InstallerException exception) {
			logger.error("Exception occurred while fetching gateway details", exception);
			return new ResponseEntity<ResponseBodyDTO<GatewayDetailsResponse>>(
					new ResponseBodyDTO(false, exception.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);

		} catch (Exception exception) {
			logger.error("Exception occurred while fetching reason gateway details", exception);
			return new ResponseEntity<ResponseBodyDTO<GatewayDetailsResponse>>(
					new ResponseBodyDTO(false, exception.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PutMapping("/gateway-sensor-association")
	public ResponseEntity<ResponseDTO> createGatewaySensorAssociation(
			@Valid @RequestBody CreateGatewaySensorAssociation createGatewaySensorAssociation,
			HttpServletRequest httpServletRequest) {
//		logger.info("Inside createGatewaySensorAssociation Method From InstallerController");
		Logutils.log(className, " started createGatewaySensorAssociation : " + " createGatewaySensorAssociation : " + createGatewaySensorAssociation, logger);
		try {
			Context context = new Context();
			String logUuid = context.getLogUUId();
			String methodName = "createGatewaySensorAssociation";
			logger.debug("Inside try block of createGatewaySensorAssociation Method From InstallerController");
			Logutils.log(className, methodName, context.getLogUUId(),
					" Before calling creating gateway senson association method from installation Service method ",
					logger);
//	            Long userId = jwtutil.getUserIdFromRequest(httpServletRequest);
			JwtUser jwtUser = (JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			User user = restUtils.getUserFromAuthServiceByName(jwtUser.getUsername());
			if (user != null) {
				Logutils.log(className, methodName, logUuid, " username : " + user.getUserName(), logger);
			}
			boolean status = installationService.createGatewaySensorAssociation(createGatewaySensorAssociation,
					user.getId(), logUuid);
			Logutils.log(className, methodName, context.getLogUUId(),
					" after calling creating gateway senson association method from installation Service method ",
					logger);
			logger.info("Exiting from startInstall Method of InstallController");
			return new ResponseEntity<ResponseDTO>(new ResponseDTO(status, "Sensor Attached With Gateway Successfully"),
					HttpStatus.OK);
		} catch (InterServiceRestException e) {
			logger.error("Exception occurred while creating gateway senson association", e);
			return new ResponseEntity<ResponseDTO>(new ResponseDTO(false, e.getMessage()), e.getHttpStatus());
		} catch (InstallerException installHistoryException) {
			logger.error("Exception occurred while creating gateway senson association", installHistoryException);
			return new ResponseEntity<ResponseDTO>(new ResponseDTO(false, installHistoryException.getMessage()),
					HttpStatus.BAD_REQUEST);
		} catch (Exception exception) {
			logger.error("Exception occurred while creating gateway senson association", exception);
			return new ResponseEntity<ResponseDTO>(new ResponseDTO(false, exception.getMessage()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/installation-by-can-and-imei")
	public ResponseEntity<ResponseBodyDTO<InProgressInstall>> getInstallationByCanAndImei(
			HttpServletRequest httpServletRequest, @RequestParam(value = "can", required = true) String accountNumber,
			@RequestParam(value = "imei", required = true) String imei) {
		Context context = new Context();
		String methodName = "getInstallationByCanAndImei Controller";
		Logutils.log(className, methodName, context.getLogUUId(), " started getInstallationByCanAndImei : " + "accountNumber : " + accountNumber, logger);
		try {
			logger.info("Request received for can : " + accountNumber + " And IMEI : " + imei);
			Logutils.log(className, methodName, context.getLogUUId(),
					" Before calling getInstallationByCanAndImei method from installation Service method in controller",
					logger);
			InProgressInstall finsihedInstallation = installationService.getInstallationByCanAndImei(accountNumber,
					imei);
			Logutils.log(className, methodName, context.getLogUUId(),
					" after calling getInstallationByCanAndImei method from installation Service method in controller",
					logger);
			return new ResponseEntity<ResponseBodyDTO<InProgressInstall>>(new ResponseBodyDTO<InProgressInstall>(true,
					"Fetched finsihed installation successfully", finsihedInstallation), HttpStatus.OK);
		} catch (Exception exception) {
			logger.error("Exception occurred while getting Installation By Can And Imei", exception);
			return new ResponseEntity<ResponseBodyDTO<InProgressInstall>>(
					new ResponseBodyDTO<InProgressInstall>(false, exception.getMessage(), null),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PostMapping("/get-installed-history")
	public ResponseEntity<ResponseBodyDTO<List<InstalledHistroyResponse>>> getInstalledHistory(
			@Valid @RequestBody List<String> deviceImei, HttpServletRequest httpServletRequest) {
		Context context = new Context();
		String methodName = "getInstalledHistory Controller";
		Logutils.log(className, methodName, context.getLogUUId(), " started getInstalledHistory : " + "deviceImei : " + deviceImei, logger);
		try {
			logger.info("Request received for geting installed History List by device imei");
			Logutils.log(className, methodName, context.getLogUUId(),
					" Before calling getInstalledHistory method from installation Service method in controller",
					logger);
			List<InstalledHistroyResponse> installedHistroyResponse = installationService
					.getInstaledHistoryDeviceImei(deviceImei, context);
			Logutils.log(className, methodName, context.getLogUUId(),
					" after calling getInstalledHistory method from installation Service method in controller", logger);
			return new ResponseEntity<ResponseBodyDTO<List<InstalledHistroyResponse>>>(
					new ResponseBodyDTO<List<InstalledHistroyResponse>>(true, "Fetched installed history successfully",
							installedHistroyResponse),
					HttpStatus.OK);
		} catch (InstallerException exception) {
			logger.error("Exception occurred while geting installed History List by device uuid issue", exception);
			return new ResponseEntity<ResponseBodyDTO<List<InstalledHistroyResponse>>>(
					new ResponseBodyDTO<List<InstalledHistroyResponse>>(true, exception.getMessage(), null),
					HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Exception exception) {
			logger.error("Exception occurred while geting installed History List by device uuid issue", exception);
			return new ResponseEntity<ResponseBodyDTO<List<InstalledHistroyResponse>>>(
					new ResponseBodyDTO<List<InstalledHistroyResponse>>(true, exception.getMessage(), null),
					HttpStatus.INTERNAL_SERVER_ERROR);

		}
	}

	@GetMapping("/get-sensor-install-instruction")
	public ResponseEntity<ResponseBodyDTO<List<SensorInstallInstructionDto>>> getSensorInstallInstruction(
			@RequestParam(value = "product-name", required = true) String productName) {
		Logutils.log(className, " started getSensorInstallInstruction : " + " productName : " + productName, logger);
		try {

			List<SensorInstallInstructionDto> sensorInstallInstruction = installationService
					.getSensorInstallInstruction(productName);

			return new ResponseEntity<ResponseBodyDTO<List<SensorInstallInstructionDto>>>(
					new ResponseBodyDTO<List<SensorInstallInstructionDto>>(true,
							"Fetched SensorInstallInstruction successfully", sensorInstallInstruction),
					HttpStatus.OK);
		} catch (Exception exception) {
			logger.error("Exception occurred while resetting installation", exception);
			return new ResponseEntity<ResponseBodyDTO<List<SensorInstallInstructionDto>>>(
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/get-sensor-reason-code")
	public ResponseEntity<ResponseBodyDTO<List<SensorReasonCodeDto>>> getSensorReasonCode(
			@RequestParam(value = "product-name", required = true) String productName) {
		Logutils.log(className, " started getSensorReasonCode : " + " productName : " + productName, logger);
		try {

			List<SensorReasonCodeDto> sensorReasonCode = installationService.getSensorReasonCodeByDto(productName);

			return new ResponseEntity<ResponseBodyDTO<List<SensorReasonCodeDto>>>(
					new ResponseBodyDTO<List<SensorReasonCodeDto>>(true, "Fetched Sensor Reason code successfully",
							sensorReasonCode),
					HttpStatus.OK);
		} catch (Exception exception) {
			logger.error("Exception occurred while resetting installation", exception);
			return new ResponseEntity<ResponseBodyDTO<List<SensorReasonCodeDto>>>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/get-installation-date")
	public ResponseEntity<ResponseBodyDTO<String>> getInstallationDate(
			@RequestParam(value = "asset_uuid", required = true) String assetUuid) {
		Logutils.log(className, " started getInstallationDate : " + " assetUuid : " + assetUuid, logger);
		try {

			String installationDate = installationService.getInstallationDate(assetUuid);

			return new ResponseEntity<ResponseBodyDTO<String>>(
					new ResponseBodyDTO<String>(true, "Fetched Sensor Reason code successfully", installationDate),
					HttpStatus.OK);
		} catch (Exception exception) {
			logger.error("Exception occurred while resetting installation", exception);
			return new ResponseEntity<ResponseBodyDTO<String>>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

}
