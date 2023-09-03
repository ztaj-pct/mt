package com.pct.device.version.controller;

import com.pct.common.controller.IApplicationController;
import com.pct.common.dto.ResponseBodyDTO;
import com.pct.common.util.Context;
import com.pct.common.util.JwtUser;
import com.pct.common.util.JwtUtil;
import com.pct.device.service.device.CampaignInstalledDevice;
import com.pct.device.version.dto.MessageDTO;
import com.pct.device.version.dto.StepDTO;
import com.pct.device.version.exception.BadRequestException;
import com.pct.device.version.exception.BaseMessageException;
import com.pct.device.version.exception.DeviceVersionBatchNotificationException;
import com.pct.device.version.exception.DeviceVersionException;
import com.pct.device.version.model.CampaignGatewayDetails;
import com.pct.device.version.model.CampaignListDisplay;
import com.pct.device.version.model.CampaignStatsPayloadList;
import com.pct.device.version.model.CampaignStep;
import com.pct.device.version.payload.*;
import com.pct.device.version.payload.CampaignPayload;
import com.pct.device.version.payload.CampaignStatsPayload;
import com.pct.device.version.payload.DeviceCampaignHistory;
import com.pct.device.version.payload.DeviceWithEligibility;
import com.pct.device.version.payload.SaveCampaignRequest;
import com.pct.device.version.payload.SelectedDevice;
import com.pct.device.version.payload.UpdateCampaignPayload;
import com.pct.device.version.repository.projections.CampaignIdAndNameView;
import com.pct.device.version.service.ICampaignService;
import com.pct.device.version.service.IDeviceCampaignStatusService;
import com.pct.device.version.util.CampaignExcelExporter;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/campaign")
public class CampaignController implements IApplicationController<CampaignStep> {

	Logger logger = LoggerFactory.getLogger(CampaignController.class);

	private static Integer DEFAULT_PAGE_SIZE = 10000;
	@Autowired
	private ICampaignService campaignService;
	@Autowired
	private JwtUtil jwtUtil;
	
	@Autowired
	private IDeviceCampaignStatusService deviceCampaignStatusService;

	@PostMapping()
	public ResponseEntity<ResponseBodyDTO> createCampaign(@RequestBody SaveCampaignRequest saveCampaignRequest,
			HttpServletRequest httpServletRequest) {
		try {
			logger.info("Request received for saveCampaignRequest {}", saveCampaignRequest.toString());
			//Long userId = jwtUtil.getUserIdFromRequest(httpServletRequest);
			JwtUser jwtUser = (JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

			String uuid = campaignService.saveCampaign(saveCampaignRequest, jwtUser.getUserId());
			String message = "Successfully saved Campaign ";
			return new ResponseEntity(new ResponseBodyDTO(true, message, uuid), HttpStatus.OK);
		} catch (BaseMessageException e) {
			logger.error("Exception while saving Campaign ", e);
			return new ResponseEntity(new ResponseBodyDTO(false, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (DeviceVersionException e) {
			logger.error("Exception while saving Campaign ", e);
			return new ResponseEntity(new ResponseBodyDTO(false, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			logger.error("Exception while saving Campaign ", e);
			return new ResponseEntity(new ResponseBodyDTO(false, "Exception occurred while saving Campaign"),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@PostMapping("/getAll")
	public ResponseEntity<Object> getCampaign(@RequestParam(value = "_page", required = false) Integer page,
			@RequestParam(value = "_limit", required = false) Integer pageSize,
			@RequestParam(value = "_sort", required = false) String sort,
			@RequestParam(value = "_order", required = false) String order,
			@RequestBody Map<String, String> filterValues) {
		logger.info("Inside getCampaign");
		try {
			if (page == null) {
				page = 1;
			} else if (page == -1 || page == 0) {
				throw new BadRequestException("invalid.page_index.in.input");
			}
			if (pageSize == null)
				pageSize = 25;
			if (pageSize > DEFAULT_PAGE_SIZE)
				pageSize = DEFAULT_PAGE_SIZE;
			if (sort.equalsIgnoreCase("campaignName")) {
				sort = "campaign."+sort;
			}if (sort.equalsIgnoreCase("createdAt")) {
				sort = "campaign."+sort;
			}
			if (sort.equalsIgnoreCase("createdBy")) {
				sort = "campaign.createdBy.firstName";
			}
			if (sort.equalsIgnoreCase("firstStep")) {
				sort = "fromPackage.packageName";
			}
			if (sort.equalsIgnoreCase("lastStep")) {
				sort = "toPackage.packageName";
			}
			if (sort.equalsIgnoreCase("imeiGroup")) {
				sort = "campaign.group.groupingName";
			}
			if (sort.equalsIgnoreCase("lastCount")) {
				sort = "campaign.group.imeiCount";
			}
			if (sort.equalsIgnoreCase("campaignStatus")) {
				sort = "campaign.campaignStatus";
			}
			if (sort.equalsIgnoreCase("campaignStatus")) {
				sort = "campaign.campaignStatus";
			}

			pageSize = 200;
			MessageDTO<Object> messageDto = new MessageDTO<>("Campaign Fetched Successfully", true);
			Page<CampaignPayload> packageResponse = campaignService.getAllCampaign(
					filterValues,getPageable(page - 1, pageSize, sort, order)); 

			messageDto.setBody(packageResponse.getContent());
			messageDto.setTotalKey(packageResponse.getTotalElements());
			messageDto.setCurrentPage(packageResponse.getNumber() + 1);
			messageDto.setTotal_pages(packageResponse.getTotalPages());

			return new ResponseEntity(messageDto, HttpStatus.OK);
			
			
		} catch (BaseMessageException e) {
			logger.error("Exception while fetching Campaign ", e);
			return new ResponseEntity(new ResponseBodyDTO(false, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			logger.error("Exception while fetching Campaign ", e);
			return new ResponseEntity(new ResponseBodyDTO(false, "Exception occurred while fetching Campaign"),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	
	@GetMapping(value ="/v1/exportGatewayList/{uuid}" )
	public void exportGatewayList(@Validated @PathVariable("uuid") String campaignUuid,
								  HttpServletResponse response){
  		
		
		String msgUuid = UUID.randomUUID().toString();
		logger.info("UUID For Fetching Export GatewayList Is : "+campaignUuid + " msgUuid "+msgUuid);
		ModelMapper modelMapper=new ModelMapper();
		CampaignStatsPayload campaignStatsPayload=campaignService.getByUuid(campaignUuid, msgUuid,0);
		 
		
		Map<String, String> filterValues = new HashMap<>() ;
		 
		 
		 
		
		Page<CampaignDeviceDetail> campaignDeviceDetailList = deviceCampaignStatusService
				.getCampaignDeviceDetails(campaignUuid, filterValues, getPageable(0, 20000, "deviceId", "asc"));
		
		
		response.setContentType("application/octet-stream");

		String headerKey = "Content-Disposition";
		String headerValue = "attachment; filename=" + campaignStatsPayload.getCampaignName() + "_gateways.xlsx";
		response.setHeader(headerKey, headerValue);
		logger.info("The Total Gateway records For This Campaign Are ##: "+campaignDeviceDetailList.getNumberOfElements());

 		try {
			CampaignExcelExporter exporter = new CampaignExcelExporter(campaignDeviceDetailList.getContent(),1);
 
			//exporter.export(response);
			exporter.exportGateways(response,msgUuid);

			logger.info("Gateway Lists Excel Named  : "+campaignStatsPayload.getCampaignName() + "_gateways.xlsx Has Been Generated.");
		}catch(Exception ee){
			ee.printStackTrace();
		}
	}


	@GetMapping(value = "/{uuid}/{method}")
	public ResponseEntity<ResponseBodyDTO<CampaignStatsPayload>> getCampaignByUuid(
			@Validated @PathVariable("uuid") String campaignUuid ,@PathVariable("method") Integer method) {
		String msgUuid1 = UUID.randomUUID().toString();
		String campaignMsgUUID = "Campaign uuid: "+campaignUuid;
		String msgUuid = campaignMsgUUID + " , msg uuid : "+msgUuid1;
		logger.info("Inside getCampaignByUuid");
		try {

			CampaignStatsPayload campaignResponse = campaignService.getByUuid(campaignUuid, msgUuid,method);

			return new ResponseEntity<ResponseBodyDTO<CampaignStatsPayload>>(
					new ResponseBodyDTO<CampaignStatsPayload>("Fetched campaign by uuid", campaignResponse), HttpStatus.OK);

		} catch (DeviceVersionException e) {
			logger.error("Exception while Fetching Campaign ", e);
			return new ResponseEntity(new ResponseBodyDTO(false, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			logger.error("Exception while Fetching Campaign ", e);
			return new ResponseEntity(new ResponseBodyDTO(false, "Error In Fetching Campaign"),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@DeleteMapping(value = "/{uuid}")
	public ResponseEntity<ResponseBodyDTO> softDeleteCampaign(@PathVariable("uuid") String uuid) {
		logger.info("Inside Delete Campaign");
		try {
			campaignService.deleteById(uuid);
			return new ResponseEntity<ResponseBodyDTO>(new ResponseBodyDTO(true, "Campaign Deleted successfully"),
					HttpStatus.OK);
		} catch (BaseMessageException e) {
			logger.error("Exception in delete Campaign ", e);
			return new ResponseEntity(new ResponseBodyDTO(false, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			logger.error("Exception in delete Campaign ", e);
			return new ResponseEntity(new ResponseBodyDTO(false, "Exception occurred in Campaign delete"),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}
	
	
	@PutMapping()
	public ResponseEntity<MessageDTO<String>> updateCampaign(@RequestBody UpdateCampaignPayload campaignToUpdate,
			HttpServletRequest httpServletRequest) {
		logger.info("Inside UpdatePackage");
		try {
			//Long userId = jwtUtil.getUserIdFromRequest(httpServletRequest);
			JwtUser jwtUser = (JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

			String uuid = campaignService.update(campaignToUpdate, jwtUser.getUsername());
			return new ResponseEntity<MessageDTO<String>>(
					new MessageDTO<String>("Campaign Updated  successfully", uuid, true), HttpStatus.CREATED);
		} catch (BaseMessageException e) {
			logger.error("Exception while updating the Campaign ", e);
			return new ResponseEntity(new ResponseBodyDTO(false, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (DeviceVersionException e) {
			logger.error("Exception while updating the Campaign ", e);
			return new ResponseEntity(new ResponseBodyDTO(false, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			logger.error("Exception while updating the Campaign ", e);
			return new ResponseEntity(new ResponseBodyDTO(false, "Error In updating the Campaign"),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}
	
	@GetMapping(value = "/ms-device/{customerName}")
	public ResponseEntity<ResponseBodyDTO<DeviceWithEligibility>> getDevicesByCustomerName(
			@Validated @PathVariable("customerName") String customerName , @RequestParam(value = "_page", required = false) Integer page,
			@RequestParam(value = "_limit", required = false) Integer pageSize,
			@RequestParam(value = "_sort", required = false) String sort,
			@RequestParam(value = "_order", required = false) String order,
			@RequestParam(value = "basePackageUuid", required = false) String basePackageUuid,
			@RequestParam(value = "campaignUuid", required = false)String campaignUuid) {
		String msgUuid = UUID.randomUUID().toString();
		logger.info("Message UUid: "+msgUuid+", Inside getDevicesByCustomerName");
		try {
			
			if (page == null) {
				page = 1;
			} else if (page == -1 || page == 0) {
				throw new BadRequestException("invalid.page_index.in.input");
			}
			if (pageSize == null)
				pageSize = 2500;
			//commit for backup
			if(customerName.equalsIgnoreCase("ALL")){
			    pageSize = 5000;
			}
			if (pageSize > DEFAULT_PAGE_SIZE)
				pageSize = DEFAULT_PAGE_SIZE;
			pageSize = 25;

			MessageDTO<Object> messageDto = new MessageDTO<>("Fetched devices from MS database by customerName", true);
			List<DeviceWithEligibility> deviceResponse = campaignService.getAllDeviceFromMS(
					customerName,page - 1, pageSize, sort, order,basePackageUuid, campaignUuid, msgUuid); 
			logger.info("Message UUid: "+msgUuid+", exiting getDevicesByCustomerName");
			return new ResponseEntity(deviceResponse, HttpStatus.OK);

		} catch (DeviceVersionException e) {
			logger.error("Exception while Fetching Campaign ", e);
			return new ResponseEntity(new ResponseBodyDTO(false, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			logger.error("Exception while getting devices from MS database {}", e);
			return new ResponseEntity(new ResponseBodyDTO(false, "Exception while getting devices from MS database"),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}


	
	@PostMapping("/ms-device/getSelectedDevices")
	public ResponseEntity<ResponseBodyDTO<SelectedDevice>> getSelectedDevices(
			 @RequestParam(value = "_page", required = false) Integer page,
			@RequestParam(value = "_limit", required = false) Integer pageSize,
			@RequestParam(value = "_sort", required = false) String sort,
			@RequestParam(value = "_order", required = false) String order ,
			@RequestParam(value = "basePackageUuid", required = false) String basePackageUuid ,
			@RequestParam(value = "campaignUuid", required = false)String campaignUuid, 
			@RequestBody List<String> imeiList) {
		String msgUuid = UUID.randomUUID().toString() +" ";
		logger.info("Inside getDevicesByCustomerName");
		try {
			
			if (page == null) {
				page = 1;
			} else if (page == -1 || page == 0) {
				throw new BadRequestException("invalid.page_index.in.input");
			}
			if (pageSize == null)
				pageSize = 25;
			if (pageSize > DEFAULT_PAGE_SIZE)
				pageSize = DEFAULT_PAGE_SIZE;
			logger.info(msgUuid + "getSelectedDevices basePackageUuid" + basePackageUuid);
			MessageDTO<Object> messageDto = new MessageDTO<>("Fetched devices from MS database by customerName", true);
			SelectedDevice deviceResponse = campaignService.getSelectedDevices(
					imeiList,page - 1, pageSize, sort, order,basePackageUuid, campaignUuid, msgUuid);
			return new ResponseEntity(deviceResponse, HttpStatus.OK);

		} catch (DeviceVersionException e) {
			logger.error("Exception while Fetching Campaign ", e);
			return new ResponseEntity(new ResponseBodyDTO(false, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (DeviceVersionBatchNotificationException e) {
			logger.error("Exception while saving Campaign ", e);
			return new ResponseEntity(new ResponseBodyDTO(false, e.getMessage(),e.getObject()), HttpStatus.INTERNAL_SERVER_ERROR);
		}catch (Exception e) {
			logger.error("Exception while getting devices from MS database {}", e);
			return new ResponseEntity(new ResponseBodyDTO(false, "Exception while getting devices from MS database"),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@GetMapping("/ms-device/getCampaignInstalledDevice/{imei}")
    public   ResponseEntity<CampaignInstalledDevice> getCampaignInstalledDeviceFromMS(@PathVariable("imei") String imei) {
        try {
        	CampaignInstalledDevice allDevices = campaignService.getCampaignInstalledDeviceFromMS(imei);
            return new ResponseEntity<>(allDevices, HttpStatus.OK);
        } catch (Exception e) {
        	e.printStackTrace();
            logger.error("Exception while getting devices from MS database {}",  e);
            return new ResponseEntity(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
	
	
	@GetMapping("/check-baselinePackage-exist/{packageUuid}")
	public ResponseEntity<MessageDTO<String>> checkBaselinePackageExistInCampaign(@PathVariable("packageUuid") String packageUuid) {
		logger.info("Inside checkBaselinePackageExistInCampaign");
		try {
			String message = "";
			Boolean result = false;
			List<CampaignIdAndNameView> isExist = campaignService.getBaselinePackageFromExistingCampaign(packageUuid);
			if (isExist != null && isExist.size() > 0) {
				message = "Package as baseline already exist";
			} else {
				message = "Package as baseline is unique";
			}
			logger.info(message);
			return new ResponseEntity(isExist, HttpStatus.OK);
		} catch (Exception e) {
			logger.error("Exception while checking package already exist as baseline ", e);
			return new ResponseEntity(new MessageDTO("Exception while checking package already exist as baseline ", false),
					HttpStatus.INTERNAL_SERVER_ERROR);

		}

	}


	@GetMapping("/check-campaignName-exist/{campaignName}")
	public ResponseEntity<MessageDTO<String>> checkCampaignNameExist(@PathVariable("campaignName") String campaignName) {
		logger.info("Inside checkUniqueCampaignName");
		try {
			String message = "";
			if (campaignName == null) {
				logger.error("Invalid campaign name");
				return new ResponseEntity(new MessageDTO("Invalid campaign name", false),
						HttpStatus.INTERNAL_SERVER_ERROR);
			}

			Boolean isExist = campaignService.getCampaignByName(campaignName);
			if (isExist) {
				message = "Campaign name " + campaignName + " already exist";
			} else {
				message = "Campaign name " + campaignName + " is unique";
			}
			return new ResponseEntity<MessageDTO<String>>(new MessageDTO<String>(message, isExist), HttpStatus.OK);
		} catch (Exception e) {
			logger.error("Exception while checking unique Campaign name ", e);
			return new ResponseEntity(new MessageDTO("Exception occurred while checking unique Campaign name", false),
					HttpStatus.INTERNAL_SERVER_ERROR);

		}

	}

	@GetMapping("/getDeviceCampaignHistoryByImei/{imei}")
    public   ResponseEntity<List<DeviceCampaignHistory>> getDeviceCampaignHistoryByImei(@PathVariable("imei") String imei) {
        try {
        	List<DeviceCampaignHistory> listDeviceCampaignHistory= campaignService.getDeviceCampaignHistoryByImei(imei);
            return new ResponseEntity<>(listDeviceCampaignHistory, HttpStatus.OK);
        } catch (Exception e) {
        	e.printStackTrace();
            logger.error("Exception while getting devices from MS database {}",  e);
            return new ResponseEntity(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
	@GetMapping("/campaign-history/{imei}")
	public ResponseEntity<CampaignHistory> fetchCampaignHistory(@PathVariable("imei") String imei) {
		try {
			Context context = new Context();
			CampaignHistory campaignHistory = campaignService.fetchCampaignHistory(context.getLogUUId(), imei);
			return new ResponseEntity<>(campaignHistory, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception while getting campaign history. ", e);
			return new ResponseEntity(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping(value = "/package-sequence/{campaignName}")
	public ResponseEntity<ResponseBodyDTO<PackageSequence>> getPackageSequenceByCampaignName(
			@Validated @PathVariable("campaignName") String campaignName) {
		String msgUuid = UUID.randomUUID().toString();
		logger.info("Inside getPackageSequenceByCampaignName");
		try {

			PackageSequence packageSequence = campaignService.getPackageSequenceByCampaignName(campaignName, msgUuid);

			return new ResponseEntity<ResponseBodyDTO<PackageSequence>>(
					new ResponseBodyDTO<PackageSequence>("Fetched Package Sequence by Campaign name", packageSequence),
					HttpStatus.OK);

		} catch (DeviceVersionException e) {
			logger.error("Exception while fetching package sequence ", e);
			return new ResponseEntity(new ResponseBodyDTO(false, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			logger.error("Exception while fetching package sequence", e);
			return new ResponseEntity(new ResponseBodyDTO(false, "Exception while fetching package sequence"),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping(value = "/gatewayListByStatusAndCampaignName/{name}/{status}")
	public ResponseEntity<ResponseBodyDTO<List<String>>> getGatewayListByStatusAndCampaignName(
			@Validated @PathVariable("name") String name, @Validated @PathVariable("status") String status) {
		String msgUuid = UUID.randomUUID().toString();
		logger.info("Inside getGatewayListByStatusAndCampaignName");
		try {
			String uuid = campaignService.getCampaignUUidByName(name.trim());
			CampaignStatsPayload campaignResponse = campaignService.getByUuid(uuid,msgUuid,1);
			List<String> list = campaignResponse.getCampaignDeviceDetail().stream()
					.filter(m -> m.getDeviceStatusForCampaign().equalsIgnoreCase(status)).map(m -> m.getImei())
					.collect(Collectors.toList());
			return new ResponseEntity<ResponseBodyDTO<List<String>>>(
					new ResponseBodyDTO<List<String>>("Fetched gateway list for given campaign and status", list), HttpStatus.OK);
		} catch (DeviceVersionException e) {
			logger.error("Exception while fetching gateway list", e);
			return new ResponseEntity(new ResponseBodyDTO(false, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			logger.error("Exception while fetching gateway list", e);
			return new ResponseEntity(new ResponseBodyDTO(false, "Exception while fetching gateway list"),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@GetMapping(value = "/current-campaign/{imei}")
	public ResponseEntity<ResponseBodyDTO<CurrentCampaignResponse>> getCurrentCampaignByImei(
			@Validated @PathVariable("imei") String imei) {
		String msgUuid = UUID.randomUUID().toString();
		logger.info("imei:   "+imei +" msgUuid :"+msgUuid);
		try {
			CurrentCampaignResponse currentCampaignPayload = campaignService.currentCampaignByImei(imei, msgUuid);

			return new ResponseEntity<ResponseBodyDTO<CurrentCampaignResponse>>(
					new ResponseBodyDTO<CurrentCampaignResponse>("Fetched Current Campaign by imei", currentCampaignPayload), HttpStatus.OK);

		} catch (DeviceVersionException e) {
			logger.error("Exception while Fetching Current Campaign  ", e);
			return new ResponseEntity(new ResponseBodyDTO(false, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			logger.error("Exception while Fetching Current Campaign ", e);
			return new ResponseEntity(new ResponseBodyDTO(false, "Error In Fetching Current Campaign "),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@PostMapping(value = "/resetStatusOfProblemDevice2")
	public ResponseEntity<ResponseBodyDTO> resetStatusOfProblemDevice(@RequestBody List<String> selectedDeviceIdForResetStatus) {
		logger.info("Inside Reset Status Of Problem Device");
		try {
			campaignService.resetStatusOfProblemDevice(selectedDeviceIdForResetStatus);
			return new ResponseEntity<ResponseBodyDTO>(new ResponseBodyDTO(true, "Device Status Reset successfully"),
					HttpStatus.OK);
		} catch (BaseMessageException e) {
			logger.error("Exception in reset problem device status ", e);
			return new ResponseEntity(new ResponseBodyDTO(false, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			logger.error("Exception in reset problem device status ", e);
			return new ResponseEntity(new ResponseBodyDTO(false, "Exception occurred Reset Status Of Problem Device"),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}
	
	@GetMapping("/check-IdenticalBaselinePackage-exist/{packageUuid}")
	public ResponseEntity<MessageDTO<String>> checkIdenticalBaselinePackageExistInCampaign(@PathVariable("packageUuid") String packageUuid) {
		logger.info("Inside checkIdenticalBaselinePackageExistInCampaign");
		try {
			String message = "";
			Boolean result = false;
			List<CampaignIdAndNameView> isExist = campaignService.getIdenticalBaselinePackageFromExistingCampaign(packageUuid);
			if (isExist != null && isExist.size() > 0) {
				message = "Identical package as baseline already exist";
			} else {
				message = "Package as baseline is unique";
			}
			logger.info(message);
			return new ResponseEntity(isExist, HttpStatus.OK);
		} catch (Exception e) {
			logger.error("Exception while checking identical package already exist as baseline ", e);
			return new ResponseEntity(new MessageDTO("Exception while checking identical package already exist as baseline ", false),
					HttpStatus.INTERNAL_SERVER_ERROR);

		}

	}
	
	
	@GetMapping(value = "/ms-device/v2/{customerName}")
	public Page<DeviceWithEligibility> getDevicesByCustomerNamePage(
			@Validated @PathVariable("customerName") String customerName , @RequestParam(value = "_page", required = false) Integer page,
			@RequestParam(value = "_limit", required = false) Integer pageSize,
			@RequestParam(value = "_sort", required = false) String sort,
			@RequestParam(value = "_order", required = false) String order,
			@RequestParam(value = "basePackageUuid", required = false) String basePackageUuid,
			@RequestParam(value = "campaignUuid", required = false)String campaignUuid) {
		String msgUuid = UUID.randomUUID().toString();
		logger.info("Message UUid: "+msgUuid+", Inside getDevicesByCustomerNamePage");
		try {
			
			if (page == null) {
				page = 1;
			} else if (page == -1 || page == 0) {
				throw new BadRequestException("invalid.page_index.in.input");
			}
			if (pageSize == null)
				pageSize = 10;
			if (pageSize > DEFAULT_PAGE_SIZE)
				pageSize = DEFAULT_PAGE_SIZE;			

			
			MessageDTO<Object> messageDto = new MessageDTO<>("Fetched devices from MS database by customerName", true);
			Page<DeviceWithEligibility> deviceResponse = campaignService.getAllDeviceFromMSPage(
					customerName,page - 1, pageSize, sort, order,basePackageUuid, campaignUuid, msgUuid); 
			 return deviceResponse;
 
		} catch (DeviceVersionException e) {
			logger.error("Exception while Fetching Campaign ", e);
			return null; 
			} catch (Exception e) {
			logger.error("Exception while getting devices from MS database {}", e);
			return null;
			}
	}
	@PostMapping(value = "/processDevicesForUpdateCampaign/list")
	public ResponseEntity<ResponseBodyDTO> processDevicesForUpdateCampaignList(@RequestBody List<String> devices) {
		String msgUuid = UUID.randomUUID().toString();
		logger.info("Message UUid: "+msgUuid+", Inside processDevicesForUpdateCampaignList");
 		try {
			campaignService.processDevicesForUpdateCampaign(devices ,msgUuid);
			return new ResponseEntity<ResponseBodyDTO>(new ResponseBodyDTO(true, "Devices process successfully"),
					HttpStatus.OK);
		} catch (BaseMessageException e) {
			logger.error("Exception in  processDevicesForUpdateCampaign ", e);
			return new ResponseEntity(new ResponseBodyDTO(false, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			logger.error("Exception inprocessDevicesForUpdateCampaign ", e);
			return new ResponseEntity(new ResponseBodyDTO(false, "Exception occurred processDevicesForUpdateCampaign"),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}
	@PostMapping(value = "/processDevicesForUpdateCampaign/{status}")
	public ResponseEntity<ResponseBodyDTO> processDevicesForUpdateCampaign(@Validated @PathVariable("status") String status) {
		String msgUuid = UUID.randomUUID().toString(); 
		logger.info("Message UUid: "+msgUuid+", Inside processDevicesForUpdateCampaign STATUS : "+ status);
 		try {
			campaignService.processDevicesForUpdateCampaignSchedular(msgUuid, status);
			return new ResponseEntity<ResponseBodyDTO>(new ResponseBodyDTO(true, "Devices processed successfully"),
					HttpStatus.OK);
		} catch (BaseMessageException e) {
			logger.error("Exception in  processDevicesForUpdateCampaign ###", e);
			return new ResponseEntity(new ResponseBodyDTO(false, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			logger.error("Exception inprocessDevicesForUpdateCampaign ###", e);
			return new ResponseEntity(new ResponseBodyDTO(false, "Exception occurred processDevicesForUpdateCampaign###"),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}
}
