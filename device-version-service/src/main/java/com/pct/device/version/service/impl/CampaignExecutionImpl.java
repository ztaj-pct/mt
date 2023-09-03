package com.pct.device.version.service.impl;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.pct.common.dto.MsDeviceRestResponse;
import com.pct.common.model.User;
import com.pct.device.service.device.DeviceReport;
import com.pct.device.version.constant.CampaignStatus;
import com.pct.device.version.constant.CampaignStepDeviceStatus;
import com.pct.device.version.constant.DeviceStatusForCampaign;
import com.pct.device.version.constant.SensorStatus;
import com.pct.device.version.dto.DeviceCampaignStatusDTO;
import com.pct.device.version.exception.DeviceInMultipleCampaignsException;
import com.pct.device.version.exception.DeviceVersionException;
import com.pct.device.version.model.Campaign;
import com.pct.device.version.model.CampaignConfigProblem;
import com.pct.device.version.model.CampaignStep;
import com.pct.device.version.model.CampaignStepDeviceDetail;
import com.pct.device.version.model.DeviceStatus;
import com.pct.device.version.model.Package;
import com.pct.device.version.payload.ExecuteCampaignRequest;
import com.pct.device.version.repository.ICampaignConfigProblemRepository;
import com.pct.device.version.repository.ICampaignRepository;
import com.pct.device.version.repository.ICampaignStepDeviceDetailRepository;
import com.pct.device.version.repository.ICampaignStepRepository;
import com.pct.device.version.repository.IDeviceStatusRepository;
import com.pct.device.version.repository.IDeviceVersionRepository;
import com.pct.device.version.repository.IPackageRepository;
import com.pct.device.version.repository.projections.CampaignPauseView;
import com.pct.device.version.repository.projections.DeviceCampaignStepStatus;
import com.pct.device.version.repository.projections.StepStatusForDeviceView;
import com.pct.device.version.service.ICampaignExecutionService;
import com.pct.device.version.service.ILatestDeviceMaintenanceReportService;
import com.pct.device.version.service.IPackageService;
import com.pct.device.version.util.BeanConverter;
import com.pct.device.version.util.CampaignDeviceHelper;
import com.pct.device.version.util.Constants;
import com.pct.device.version.util.DeviceDetailProcessor;
import com.pct.device.version.util.DeviceReportNotAvailableInMS;
import com.pct.device.version.util.RestUtils;
import com.pct.device.version.validation.CampaignUtils;

@Service
public class CampaignExecutionImpl implements ICampaignExecutionService {

	Logger logger = LoggerFactory.getLogger(CampaignExecutionImpl.class);
	Logger analysisLog = LoggerFactory.getLogger("analytics");

	Long minAnyCount = 0l;
	@Autowired
	private BeanConverter beanConverter;
	@Autowired
	private RestUtils restUtils;
	@Autowired
	private ICampaignRepository campaignRepo;
	@Autowired
	private ICampaignStepRepository campaignStepRepository;
	@Autowired
	private ICampaignStepDeviceDetailRepository stepDeviceDetailRepository;
	@Autowired
	private IPackageRepository packageRepository;
	@Autowired
	private CampaignUtils campaignUtils;
	@Autowired
	private ICampaignConfigProblemRepository configProblemRepository;
	@Autowired
	private CampaignDeviceHelper campaignDeviceHelper;
	@Autowired
	private IPackageService packageService;
	@Autowired
	private ILatestDeviceMaintenanceReportService iLatestDeviceMaintenanceReportService;

	@Autowired
	private DeviceDetailProcessor deviceDetailProcessor;

	@Autowired
	private IDeviceStatusRepository deviceStatuseRepository;
	@Autowired
	IDeviceVersionRepository deviceVersionRepository;

	public boolean checkOnlineAndNullStatus(String fieldStatus) {

		return ((fieldStatus != null && fieldStatus.equalsIgnoreCase(SensorStatus.ONLINE.getValue()))
				|| fieldStatus == null);

	}

	@Override
	public String processCampaign(ExecuteCampaignRequest executeRequest, String msgUuid)
			throws DeviceInMultipleCampaignsException {
		DeviceCampaignStatusDTO deviceCampaignStatusDTO = new DeviceCampaignStatusDTO();
	
		
		LocalDateTime ldt = LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC);
		Instant now = ldt.toInstant(ZoneOffset.UTC);
		
		logger.info("NOW 11111********"+now);
		logger.info("executeRequest.getTimestampReceivedPST() ********"+executeRequest.getTimestampReceivedPST());
		
		String atCommand = null;
		try {

			

			Campaign eligibleCampaign = null;
			List<Campaign> activeCampaignForImei;
			List<Campaign> excludeCampaignList = new ArrayList<Campaign>();
			List<String> campaignList = new ArrayList<String>();

			Set<String> noReportsIdsRef = DeviceReportNotAvailableInMS.getNoReportsIds();
			noReportsIdsRef.remove(executeRequest.getDeviceId());
			MsDeviceRestResponse msDeviceRestResponse = deviceVersionRepository.findDeviceByIMEI(executeRequest.getDeviceId());
			msgUuid = msgUuid + " deviceId : " + executeRequest.getDeviceId();
			logger.info("Message UUID (1) : " + msgUuid + ", Inside processCampaign method");
			analysisLog.debug("Message UUID (1) : " + msgUuid + ", Inside processCampaign method");
			ExecuteCampaignRequest executeCampaignRequest = beanConverter.parseWaterfallData(executeRequest, msgUuid);
			DeviceReport dr = new DeviceReport();
			dr.setAPP_SW_VERSION(executeCampaignRequest.getSwVersionApplication());
			dr.setBASEBAND_SW_VERSION(executeCampaignRequest.getSwVersionBaseband());
			dr.setDEVICE_ID(executeCampaignRequest.getDeviceId());
			dr.setEXTENDER_VERSION(executeCampaignRequest.getExtenderVersion());
			// CLD-898
			dr.setBLE_VERSION(executeCampaignRequest.getBleVersion());

			dr.setConfig1CIV(executeCampaignRequest.getConfig1CIV());
			dr.setConfig2CIV(executeCampaignRequest.getConfig2CIV());
			dr.setConfig3CIV(executeCampaignRequest.getConfig3CIV());
			dr.setConfig4CIV(executeCampaignRequest.getConfig4CIV());
			dr.setConfig1CRC(executeCampaignRequest.getConfig1CRC());
			dr.setConfig2CRC(executeCampaignRequest.getConfig2CRC());
			dr.setConfig3CRC(executeCampaignRequest.getConfig3CRC());
			dr.setConfig4CRC(executeCampaignRequest.getConfig4CRC());

			dr.setDevice_type(executeCampaignRequest.getDevice_type());

			dr.setLiteSentryApp(executeCampaignRequest.getLiteSentryApp());
			dr.setLiteSentryBoot(executeCampaignRequest.getLiteSentryBoot());
			dr.setLiteSentryHw(executeCampaignRequest.getLiteSentryHw());

			dr.setMaxbotixFirmware(executeCampaignRequest.getMaxbotixFirmware());
			dr.setMaxbotixhardware(executeCampaignRequest.getMaxbotixHardware());

			dr.setRiotFirmware(executeCampaignRequest.getRiotFirmware());
			dr.setRiothardware(executeCampaignRequest.getRiotHardware());

			dr.setSteApp(executeCampaignRequest.getSteApp());
			dr.setSteMCU(executeCampaignRequest.getSteMcu());

			dr.setConfigurationDesc(executeCampaignRequest.getConfigurationDesc());
			deviceCampaignStatusDTO.setDeviceReport(dr);
			Boolean emergencyStopFlag = packageService.getEmergencyStopFlagValue();

			if (emergencyStopFlag) {
				logger.info("Message UUID (1.1) : " + msgUuid
						+ ", Emergency Stop triggered. Returning AT command as null.");
				
				return null;

			}

			logger.info("Message UUID (2) : " + msgUuid + " , ExecuteCampaignRequest STE status: "
					+ executeRequest.getSteStatus() + " , " + "ExecuteCampaignRequest Lite Sentry status: "
					+ executeRequest.getLiteSentryStatus() + " , " + "ExecuteCampaignRequest Maxbotix Cargo status: "
					+ executeRequest.getMaxbotixStatus() + " , " + "ExecuteCampaignRequest Riot Cargo status: "
					+ executeRequest.getRiotStatus());
		
			// AT command should be triggered in below scenario

			if (!(checkOnlineAndNullStatus(executeRequest.getLiteSentryStatus())
					&& checkOnlineAndNullStatus(executeRequest.getRiotStatus())
					&& checkOnlineAndNullStatus(executeRequest.getMaxbotixStatus())
					&& checkOnlineAndNullStatus(executeRequest.getSteStatus()))) {

				logger.info("Message UUID (3.a) : " + msgUuid
						+ ", . Sensor status is not Online for any of peripheral sensors");
		
			}

			// list of active campaigns for particular device
		/*	activeCampaignForImei = campaignUtils.checkCampaignForDeviceId(executeCampaignRequest.getDeviceId(),
					executeCampaignRequest.getOwnerLevel2());
    */
			activeCampaignForImei = campaignUtils.checkCampaignForDeviceId(executeCampaignRequest.getDeviceId(),
					msDeviceRestResponse.getOrganisationName());
			deviceCampaignStatusDTO.setCustomerNam(msDeviceRestResponse.getOrganisationName());
			deviceCampaignStatusDTO.setCustomerId(msDeviceRestResponse.getOrganisationId()+"");
			String message;
			for (Campaign campaign : activeCampaignForImei) {
				//String customer = executeCampaignRequest.getOwnerLevel2();
				String customer = msDeviceRestResponse.getOrganisationName();
				String campaignType = campaign.getCampaignType();
				/*if (campaignType != null && campaignType.equalsIgnoreCase(CampaignScope.UNRESTRICTED.name())) {
					message = restUtils.isApprovalReqForDeviceUpdate(customer);
					if (message.equalsIgnoreCase("yes")) {
						excludeCampaignList.add(campaign);
					}
				}*/
			}

			for (Campaign campaign : activeCampaignForImei) {
				String excludedImei = campaign.getGroup().getExcludedImei();
				String removedImei = campaign.getGroup().getRemovedImei();
				if(excludedImei == null)
				{
					excludedImei = "";	
					
				}
				logger.info("Message UUID (4) : " + msgUuid
						+ " iterating over activeCampaignForImei list, currently processing campaign (uuid) : "
						+ campaign.getUuid() + ", campaign name : " + campaign.getCampaignName());
				
				// find last step-details of campaign (repo call)
				//CampaignStep a = campaignStepRepository.findLastStepByCampaignUuid(campaign.getUuid());
				
				
				
				
				Long maxStepInCamp =campaignStepRepository.findLastStepInCampaign(campaign.getUuid());
				
				// find status of last step : completed/pending (should not be failed)

				String lastStepStatusInThisCamp = stepDeviceDetailRepository.findStatusByCampaignUuidAndStepOrderNumber(
						campaign.getUuid(), executeCampaignRequest.getDeviceId(), maxStepInCamp);

				logger.info("Message UUID (5) : " + msgUuid + ", campaign (uuid) : " + campaign.getUuid()
						+ ", campaign name : " + campaign.getCampaignName() + ", lastStepStatusInThisCamp : "
						+ lastStepStatusInThisCamp);
			
				if (lastStepStatusInThisCamp != null
						&& (lastStepStatusInThisCamp.equalsIgnoreCase(CampaignStepDeviceStatus.SUCCESS.getValue())
								|| lastStepStatusInThisCamp.equals((CampaignStepDeviceStatus.REMOVED.getValue())))) {
					logger.info("Message UUID (5.1) : " + msgUuid + ", Excluding campaign (uuid) : " + campaign.getUuid()
					+ ", campaign name : " + campaign.getCampaignName() + ", lastStepStatusInThisCamp : "
					+ lastStepStatusInThisCamp);
					excludeCampaignList.add(campaign);
				}
				if(excludedImei != null && excludedImei.contains(executeRequest.getDeviceId()))
				{
					excludeCampaignList.add(campaign);
					logger.info("Message UUID (5.2) : " + msgUuid + ", Excluding campaign (uuid) : " + campaign.getUuid()
					+ ", campaign name : " + campaign.getCampaignName() + "device  excluded ");
				}
				if(removedImei != null && removedImei.contains(executeRequest.getDeviceId()))
				{
					excludeCampaignList.add(campaign);
					logger.info("Message UUID (5.2) : " + msgUuid + ", Excluding campaign (uuid) : " + campaign.getUuid()
					+ ", campaign name : " + campaign.getCampaignName() + "device  removed ");
		
				}
			
				// find status of last step : completed/pending (should not be failed)
				//List<CampaignStepDeviceDetail> removedStepStatusInThisCamp = stepDeviceDetailRepository
					//	.getRemovedIMEI(campaign.getUuid(), executeCampaignRequest.getDeviceId());

				
			
				//if (removedStepStatusInThisCamp != null && removedStepStatusInThisCamp.size() > 0) {
				//	excludeCampaignList.add(campaign);
				//	logger.info("Message UUID (5.3) : " + msgUuid + ", Excluding campaign (uuid) : " + campaign.getUuid()
				//	+ ", campaign name : " + campaign.getCampaignName() + "device  removed ");
		
					
				//}
			}

			activeCampaignForImei.removeAll(excludeCampaignList);

			if (activeCampaignForImei == null || activeCampaignForImei.size() == 0) {
				logger.info("Message UUID (6) : " + msgUuid + ". Device doesn't belong to any campaign");
				
				throw new DeviceVersionException(
						"Device  " + executeCampaignRequest.getDeviceId() + " doesn't belong to any campaign");
			} else {

				if (activeCampaignForImei.size() == 1) {
					eligibleCampaign = activeCampaignForImei.get(0);
					logger.info("Message UUID (7) : " + msgUuid
							+ ", only one campaign in list of activeCampaignForImei, Campaign (uuid): "
							+ eligibleCampaign.getUuid() + ", Campaign name : " + eligibleCampaign.getCampaignName());
				
				} else {
					boolean campaignMatchFound = false;
					logger.info(
							"Message UUID (8) : " + msgUuid + " more than one active campaign found for given imei."+activeCampaignForImei.size());
					for (Campaign activeCampaign : activeCampaignForImei) {
						eligibleCampaign = activeCampaign;
						campaignList.add(eligibleCampaign.getUuid());
						logger.info(
						 		"Message UUID (8.01)  eligibleCampaign.getUuid() " + msgUuid + " activeCampaign.getUuid() " + activeCampaign.getUuid()+ " eligibleCampaign.getUuid() " + eligibleCampaign.getUuid()+  " "+executeCampaignRequest.getDeviceId());
						List<DeviceCampaignStepStatus> deviceCampaignStepStatus = stepDeviceDetailRepository.getStepsByDeviceCampaign(activeCampaign.getUuid(),
													executeCampaignRequest.getDeviceId());
						for(DeviceCampaignStepStatus deviceCampaignStepStatus1 : deviceCampaignStepStatus)
						{
							logger.info(
							 		"Message UUID (8.02) : " + msgUuid  + " "+ "  "+activeCampaign.getUuid() +" "+ deviceCampaignStepStatus1.getDevice_id() + " "+deviceCampaignStepStatus1.getStatus() +" "+ deviceCampaignStepStatus1.getStep_order_number());
						}
						logger.info(
						 		"Message UUID (8.1) : " + msgUuid  + " "+deviceCampaignStepStatus.size());
						
						Map<Long, List<DeviceCampaignStepStatus>> deviceCampaignStepStatusMap = deviceCampaignStepStatus.stream()
								.collect(Collectors.groupingBy(w -> w.getStep_order_number()));
						logger.info(
								"Message UUID (8.2) : " + msgUuid  + " "+deviceCampaignStepStatusMap);
						
						
						// find status of first step for given device id for selected active campaign
						/*String campInProgressForGateway = stepDeviceDetailRepository
								.findStatusByCampaignUuidAndStepOrderNumber(activeCampaign.getUuid(),
										executeCampaignRequest.getDeviceId(), 1l);*/
						String campInProgressForGateway = null;
						if(deviceCampaignStepStatusMap.get(1l) != null && deviceCampaignStepStatusMap.get(1l).size() >0)
						{
							campInProgressForGateway = deviceCampaignStepStatusMap.get(1l).get(0).getStatus();
							logger.info(
									"Message UUID (8.3) : " + msgUuid + " campInProgressForGateway "+campInProgressForGateway + "  "+deviceCampaignStepStatusMap.get(1l).get(0).getStatus());
						
							
						}
						else
						{
							logger.info(
									"Message UUID (8.4) : " + msgUuid + " campInProgressForGateway "+campInProgressForGateway + "  "+ "null");
						}
						

						logger.info("Message UUID (9) : " + msgUuid + " Status of campaign in progress : "
								+ activeCampaign.getUuid() + " " +activeCampaign.getCampaignName() + "  " + campInProgressForGateway);
				
						if (!StringUtils.isEmpty(campInProgressForGateway)) {
							campaignMatchFound = true;

							// fetch all steps data for selected campaign
							List<CampaignStep> allSteps = campaignStepRepository
									.getAllStepsOfCampaign(activeCampaign.getUuid());

							// fetch status of second last step where status in not failed
							String campFinishedForGateway = stepDeviceDetailRepository
									.findStatusByCampaignUuidAndStepOrderNumber(activeCampaign.getUuid(),
											executeCampaignRequest.getDeviceId(),
											allSteps.get(allSteps.size() - 1).getStepOrderNumber());
					/*	String campFinishedForGateway  = null;
						long secondLast = allSteps.get(allSteps.size() - 1).getStepOrderNumber();
								logger.info(
											"Message UUID (8.5) : campFinishedForGateway secondLast " + secondLast); 
							 
							if(deviceCampaignStepStatusMap.get(secondLast) != null && deviceCampaignStepStatusMap.get(secondLast).size() >0)
							{
								
								campFinishedForGateway =deviceCampaignStepStatusMap.get(secondLast).get(0).getStatus();
								logger.info(
										"Message UUID (8.51) : campFinishedForGateway " + msgUuid + " "+campFinishedForGateway + "  "+deviceCampaignStepStatusMap.get(secondLast).get(0).getStatus());
							
								
							}
							else
							{
								logger.info(
										"Message UUID (8.61) : campFinishedForGateway " + msgUuid + " "+campFinishedForGateway + "  "+ "null");
							}
							
							logger.info("Message UUID (10) :" + msgUuid
									+ " status of second last step where status in not failed (campFinishedForGateway) "
									+ campFinishedForGateway);
						*/
							if (campFinishedForGateway != null) {
								if (!CampaignStepDeviceStatus.SUCCESS.getValue()
										.equalsIgnoreCase(campFinishedForGateway)
										&& !CampaignStepDeviceStatus.REMOVED.getValue()
												.equalsIgnoreCase(campFinishedForGateway)) {

									// execution when second last step is not executed
									updateToPackageConfigInCampaign(executeCampaignRequest, allSteps,
											deviceCampaignStatusDTO, msgUuid);

									// fetch status of second last step where status in not failed
									campFinishedForGateway = stepDeviceDetailRepository
											.findStatusByCampaignUuidAndStepOrderNumber(activeCampaign.getUuid(),
													executeCampaignRequest.getDeviceId(),
													allSteps.get(allSteps.size() - 1).getStepOrderNumber());
									
                          
							//	secondLast = allSteps.get(allSteps.size() - 1).getStepOrderNumber();
         
							//	logger.info("Message UUID (8.50) :" + msgUuid +" secondLast "+secondLast);
							 
							//if(deviceCampaignStepStatusMap.get(secondLast) != null && deviceCampaignStepStatusMap.get(secondLast).size() >0)
							//{
								
								//campFinishedForGateway = deviceCampaignStepStatusMap.get(secondLast).get(0).getStatus();
							//	logger.info(
								//		"Message UUID (8.52) : campFinishedForGateway " + msgUuid + " "+campFinishedForGateway + "  "+deviceCampaignStepStatusMap.get(secondLast).get(0).getStatus());
							
								
						//	}
							//else
						//	{
							//	logger.info(
								//		"Message UUID (8.62) : campFinishedForGateway " + msgUuid + " "+campFinishedForGateway + "  "+ "null");
						//	}
							
									logger.info("Message UUID (18) :" + msgUuid
											+ " status of second last step where status in not failed (campFinishedForGateway) "
											+ " campaign  " + activeCampaign.getCampaignName() + ":" +activeCampaign.getCampaignId() + "  "+
											campFinishedForGateway);
									if (!CampaignStepDeviceStatus.SUCCESS.getValue()
											.equalsIgnoreCase(campFinishedForGateway)) {
										break;
									} else {
										campaignMatchFound = false;
									}

								} else {
									campaignMatchFound = false;
								}
							} else {
								break;
							}
						}
					}
					
					if (!campaignMatchFound) {
						logger.info("Message UUID (20) : " + msgUuid + " Looking for eligible campaign via baseline ");
						logger.info("Message UUID (21) : " + msgUuid + " Num of camp " + activeCampaignForImei.size()
								+ " , campaign list: " + campaignList + " for device "
								+ executeCampaignRequest.getDeviceId());
					
						Map<String, Long> countMap = new HashMap<>();
						minAnyCount = 0l;
						for (Campaign activeCampaign : activeCampaignForImei) {
							logger.info("Message UUID (22) : " + msgUuid + " campaign  " + activeCampaign.getCampaignName() + ":" +activeCampaign.getCampaignId());
							analysisLog.debug(
									"Message UUID (22) : " + msgUuid + " camp " + activeCampaign.getCampaignName());

							Map<String, List<DeviceReport>> deviceMaintReportMap = new HashMap<String, List<DeviceReport>>();

							List<DeviceReport> drList = new ArrayList<DeviceReport>();
							drList.add(dr);
							deviceMaintReportMap.put(executeCampaignRequest.getDeviceId(), drList);

							List<CampaignStep> allSteps = campaignStepRepository
									.getAllStepsOfCampaign(activeCampaign.getUuid());

							String basePackageUuid = allSteps.get(0).getFromPackage().getUuid();

							Package baseLine = packageRepository.findByUuid(basePackageUuid);
					//		Package baseLine =  allSteps.get(0).getFromPackage();
							logger.info("Message UUID (23) : " + msgUuid + " campaign  " + activeCampaign.getCampaignName() + ":" +activeCampaign.getCampaignId() +" baseline " + baseLine.getPackageName() + "allSteps.get(0).getFromPackage() packageName "+allSteps.get(0).getFromPackage().getPackageName() );

							String isEligible = campaignUtils.checkDeviceMatchesBaseline(
									executeCampaignRequest.getDeviceId(), deviceMaintReportMap, baseLine, msgUuid);

							if (isEligible != null
									&& isEligible.equalsIgnoreCase(DeviceStatusForCampaign.ELIGIBLE.getValue())) {
								// baseline prioritization start
								
								ObjectMapper oMapper = new ObjectMapper();
								oMapper.registerModule(new JavaTimeModule());
								
								Map<String, Object> baseLineMap = oMapper.convertValue(baseLine, Map.class);
								Long anyCount = baseLineMap.entrySet().stream().filter(x -> x.getValue() != null
										&& x.getValue().toString().trim().equalsIgnoreCase("ANY")).count();
								if (minAnyCount == 0l || minAnyCount >= anyCount) {
									minAnyCount = anyCount;
								}
								countMap.put(activeCampaign.getUuid(), anyCount);
								logger.info("Message UUID (25) : " + msgUuid
										+ ". Base line match found for eligible campaigns " + countMap + " for device "
										+ executeCampaignRequest.getDeviceId());
										// baseline prioritization end
							}
						}
						// baseline prioritization rule start
						List<String> listOfCompaignUuid = countMap.entrySet().stream()
								.filter(x -> x.getValue() == minAnyCount).map(x -> x.getKey())
								.collect(Collectors.toList());
						if (listOfCompaignUuid != null && listOfCompaignUuid.size() == 1) {
						//eligibleCampaign = campaignRepo.findByUuid(listOfCompaignUuid.get(0));
							eligibleCampaign=activeCampaignForImei.stream().filter(x->x.getUuid()==listOfCompaignUuid.get(0))
									.collect(Collectors.toList()).get(0);
							logger.info("Message UUID (25-1) : " + msgUuid + ". Base line match found for campaign "
									+ eligibleCampaign.getCampaignName() + " for device "
									+ executeCampaignRequest.getDeviceId());
							} else if (listOfCompaignUuid != null && listOfCompaignUuid.size() > 1) {
							logger.info("Message UUID (25-2) : " + msgUuid
									+ ". Base line match found for list of campaigns " + listOfCompaignUuid
									+ " for device " + executeCampaignRequest.getDeviceId());
						
							eligibleCampaign = campaignRepo.getEligibleCompaignByUuids(listOfCompaignUuid);

							logger.info("Message UUID (25-3) : " + msgUuid + ". Base line match found for campaign "
									+ eligibleCampaign.getCampaignName() + " :" +eligibleCampaign.getUuid()+" for device "
									+ executeCampaignRequest.getDeviceId());
										}
						// baseline prioritization rule end
					}
					else
					{
						logger.info("Message UUID (19.1) : " + msgUuid + " Device is in progress for campaign  " + eligibleCampaign.getCampaignName() + " :" +  eligibleCampaign.getUuid());
					}
					logger.info("Message UUID (26) : " + msgUuid + " . Device ID "
							+ executeCampaignRequest.getDeviceId() + " found in multiple campaigns : ");
				}
				logger.info("Message UUID (27) : " + msgUuid + " . Device " + executeCampaignRequest.getDeviceId()
						+ " is processed with Campaign " + eligibleCampaign.getUuid() +" "+eligibleCampaign.getCampaignName());
			}
			logger.info("Message UUID (28) : " + msgUuid);
			
			deviceCampaignStatusDTO.setCurrentCampaignUUID(eligibleCampaign.getUuid());
			List<CampaignStep> allSteps = campaignStepRepository.getAllStepsOfCampaign(eligibleCampaign.getUuid());

			// same call as above
			// deviceCampaignStatusDTO.setIsEligible(isEligible);
			updateToPackageConfigInCampaign(executeCampaignRequest, allSteps, deviceCampaignStatusDTO, msgUuid);
			if (eligibleCampaign.getCampaignStatus().equals(CampaignStatus.IN_PROGRESS)) {

				boolean pauseCampaign = checkPauseInCampaign(eligibleCampaign, executeCampaignRequest.getDeviceId(),
						msgUuid);
				logger.info("Message UUID (31) : " + msgUuid + " pauseCampaign " + pauseCampaign);
				

				if (!pauseCampaign) {
					// below condition checks for eol,rma, is installed and other campaign
					// validations for gateway
					if (campaignValidation(executeCampaignRequest, eligibleCampaign, msgUuid)) {
						atCommand = checkFromPackageConfigInCampaign(executeCampaignRequest, allSteps,
								eligibleCampaign.getCampaignName(), deviceCampaignStatusDTO, msgUuid);
						logger.info(
								"Message UUID (62) : " + msgUuid + " atCommand to be send to gateway : " + atCommand);
					}
				} else {
					logger.info("Message UUID (63) : " + msgUuid + " Campaign " + eligibleCampaign.getCampaignName()
							+ " is in paused state ");
					// state pause check(check state for )
					campaignUtils.checkForPausedCampaign(eligibleCampaign);
					campaignUtils.checkForCompletedCampaign(eligibleCampaign);
				}
			} else {
				logger.info("Message UUID (64) : " + msgUuid + " Campaign " + eligibleCampaign.getCampaignName()
						+ " is not in-progress state ");
				}
			logger.info("Message UUID (65) : " + msgUuid + " , returning atCommand********* : " + atCommand);

			User user = eligibleCampaign.getCreatedBy();

			if (atCommand != null || (deviceCampaignStatusDTO.getCampaignRunningStatus() != null
					&& deviceCampaignStatusDTO.getCampaignRunningStatus().equals(CampaignStepDeviceStatus.SUCCESS))) {
				deviceCampaignStatusDTO.setAtCommandFound(true);
				deviceCampaignStatusDTO.setDeviceId(executeCampaignRequest.getDeviceId());
				// deviceCampaignStatusDTO.setAtCommand(atCommand);
				List<DeviceStatus> deviceStatusList = deviceStatuseRepository
						.getByDevice(executeCampaignRequest.getDeviceId());
				DeviceStatus deviceStatus = null;

				if (deviceStatusList == null || deviceStatusList.size() < 1) {
					deviceStatus = new DeviceStatus();
					deviceStatus.setCreatedAt(now);
					deviceStatus.setDeviceId(executeCampaignRequest.getDeviceId());
				} else {
					deviceStatus = deviceStatusList.get(0);
				}

				deviceStatus.setCampaign(eligibleCampaign);
				deviceStatus.setRunningStatus(deviceCampaignStatusDTO.getCampaignRunningStatus());
		//	deviceStatuseRepository.save(deviceStatus);
				deviceDetailProcessor.processDeviceForSingleCampaign(deviceCampaignStatusDTO, msgUuid);
			}
			if (atCommand != null) {
				String firstName = user.getFirstName();
				String lastName = user.getLastName();
				String email = user.getEmail();

				logger.info("Message UUID : " + msgUuid + ", AT command triggered: " + atCommand

						+ " from Campaign Manager (new), by: " + (firstName != null ? firstName : "") + " "
						+ (lastName != null ? lastName : "") + " email: " + (email != null ? email : ""));
		
			}
      
		} finally {
			//deviceDetailProcessor.processSingleDeviceForAllCampaignPostExecution(deviceCampaignStatusDTO,msgUuid);
			iLatestDeviceMaintenanceReportService.updateLatestDeviceMaintenanceReport(executeRequest,
					executeRequest.getDeviceId(), msgUuid);

		}

		return atCommand;
	}

	private boolean campaignValidation(ExecuteCampaignRequest executeCampaignRequest, Campaign campaign,
			String msgUuid) {
		try {
			// TODO Auto-generated method stub
			logger.info("Message UUID (32) : " + msgUuid + " campaignValidation -executeCampaignRequest "
					+ executeCampaignRequest);
			analysisLog.debug("Message UUID (32) : " + msgUuid + " campaignValidation -executeCampaignRequest "
					+ executeCampaignRequest);
			if (!(executeCampaignRequest.getConfig5CRC() == null
					|| executeCampaignRequest.getConfig5CRC().equals(Constants.EMPTY_STRING)
					|| executeCampaignRequest.getConfig5CRC().matches("[" + Constants.ZERO + "]+"))) {

				logger.info("Message UUID (33) : " + msgUuid + " , Campaign Validation failed. Device ID "
						+ executeCampaignRequest.getDeviceId() + " Report contains value for Config 5 : ");
				analysisLog.debug("Message UUID (33) : " + msgUuid + " , Campaign Validation failed. Device ID "
						+ executeCampaignRequest.getDeviceId() + " Report contains value for Config 5 : ");
				return false;
			}
			if (campaign.getExcludeLowBattery() != null && campaign.getExcludeLowBattery()
					&& executeCampaignRequest.getBatteryPowerV() <= Constants.LOW_BATTERY_POWER) {

				logger.info("Message UUID (34) : " + msgUuid + " ,Campaign Validation failed. Device ID "
						+ executeCampaignRequest.getDeviceId() + " Reports Low Battery "
						+ executeCampaignRequest.getBatteryPowerV());
				analysisLog.debug("Message UUID (34) : " + msgUuid + " ,Campaign Validation failed. Device ID "
						+ executeCampaignRequest.getDeviceId() + " Reports Low Battery "
						+ executeCampaignRequest.getBatteryPowerV());
				return false;

			}

//			if (campaign.getExcludeNotInstalled() != null && campaign.getExcludeNotInstalled()
//					&& (!campaignUtils.validateDeviceUpgradeEligibleForCampaign(executeCampaignRequest.getDeviceId()))) {
//
//				logger.info("Message UUID (35) : " + msgUuid + " , Campaign Validation failed for Power up event,"
//						+ ". Device ID : " + executeCampaignRequest.getDeviceId() + " checking for Installed flag ");
//
//				analysisLog.debug("Message UUID (35) : " + msgUuid + " , Campaign Validation failed for Power up event,"
//						+ ". Device ID : " + executeCampaignRequest.getDeviceId() + " checking for Installed flag ");

			logger.info("Message UUID (35.1001CC) : " + msgUuid + " getExcludeNotInstalled>>  "
					+ campaign.getExcludeNotInstalled());
			logger.info("Message UUID (35.1002CC) : " + msgUuid + " getExcludeNotInstalled>>  "
					+ (campaign.getExcludeNotInstalled() != null && campaign.getExcludeNotInstalled()));

			if (campaign.getExcludeNotInstalled() != null && campaign.getExcludeNotInstalled()) {
				if (!campaignUtils.isInstalledFlagTrue(msgUuid, executeCampaignRequest.getDeviceId())) {
					logger.info("Message UUID (35.C) : " + msgUuid
							+ " , Campaign Validation failed as Installed flag is N . Device ID : "
							+ executeCampaignRequest.getDeviceId() + " is Not Installed device ");

					analysisLog.debug("Message UUID (35.C) : " + msgUuid
							+ " , Campaign Validation failed as Installed flag is N . Device ID : "
							+ executeCampaignRequest.getDeviceId() + " is Not Installed device ");
					return false;
				}
			}

//			}

			if (campaign.getExcludeEngineering() != null && campaign.getExcludeEngineering()
					&& (Constants.CUSTOMER_ENGINEERING.equalsIgnoreCase(executeCampaignRequest.getOwnerLevel2())
							|| executeCampaignRequest.getDeviceUsage().equalsIgnoreCase(Constants.USAGE_ENGINEERING))) {

				logger.info("Message UUID (36) : " + msgUuid + " , Campaign Validation failed. Device ID "
						+ executeCampaignRequest.getDeviceId() + " is with ENGINEERING usage");
				analysisLog.debug("Message UUID (36) : " + msgUuid + " , Campaign Validation failed. Device ID "
						+ executeCampaignRequest.getDeviceId() + " is with ENGINEERING usage");
				return false;

			}

			if (campaign.getExcludeRma() != null && campaign.getExcludeRma()
					&& executeCampaignRequest.getDeviceUsage().equalsIgnoreCase(Constants.USAGE_RMA)) {

				logger.info("Message UUID (37) : " + msgUuid + " ,Campaign Validation failed. Device ID "
						+ executeCampaignRequest.getDeviceId() + " is with RMA usage");
				analysisLog.debug("Message UUID (37) : " + msgUuid + " ,Campaign Validation failed. Device ID "
						+ executeCampaignRequest.getDeviceId() + " is with RMA usage");
				return false;
			}

			if (campaign.getExcludeEol() != null && campaign.getExcludeEol()
					&& executeCampaignRequest.getDeviceUsage().equalsIgnoreCase(Constants.USAGE_EOL)) {

				logger.info("Message UUID (38) : " + msgUuid + " , Campaign Validation failed. Device ID "
						+ executeCampaignRequest.getDeviceId() + " is with EOL usage");
				analysisLog.debug("Message UUID (38) : " + msgUuid + " , Campaign Validation failed. Device ID "
						+ executeCampaignRequest.getDeviceId() + " is with EOL usage");
				return false;
			}
			logger.info("Message UUID (39) : " + msgUuid + " return true ");
			analysisLog.debug("Message UUID (39) : " + msgUuid + " return true ");
			return true;
		} catch (Exception e) {
			logger.info("Message UUID (39.B) : " + msgUuid + " return false as Exception occured " + e.getMessage());
			analysisLog
					.debug("Message UUID (39.B) : " + msgUuid + " return false as Exception occured " + e.getMessage());
			logger.error("Message UUID (39.B) : " + msgUuid + " return false as Exception occured ", e);
			return false;
		}

	}

	private int getStepToMarkOnHold(String campUuid, String deviceId, Integer totalSteps) {
		// TODO Auto-generated method stub
		logger.info("Message UUID : 307 " + " getStepToMarkOnHold  campUuid " + campUuid);
		analysisLog.debug("Message UUID  307 " + " getStepToMarkOnHold campUuid " + campUuid);
		Integer lastExecStep = stepDeviceDetailRepository.getLastExecutedStep(campUuid, deviceId);
		if (lastExecStep != null) {
			if (lastExecStep.equals(totalSteps)) {
				return 0;
			} else {
				if (lastExecStep < totalSteps) {
					lastExecStep++;
					return lastExecStep;
				}
			}

		} else {
			return 1;
		}
		return 0;
	}

	public boolean waterfallFieldCheck(String packageField, String executeCampaignRequestField, String msgUuid) {
		if (packageField.equalsIgnoreCase(Constants.ANY) || packageField.equalsIgnoreCase(executeCampaignRequestField)
				|| (packageField != null && executeCampaignRequestField != null
						&& packageField.trim().equalsIgnoreCase(executeCampaignRequestField.trim()))
				|| packageField.equalsIgnoreCase(Constants.EMPTY_STRING) || executeCampaignRequestField == null) {
			logger.info("Message UUID (13.C) : " + msgUuid + " , PackageField : " + packageField
					+ " , ExecuteCampaignRequestField : " + executeCampaignRequestField
					+ " , Condition matched : true");
			analysisLog.debug("Message UUID (13.C) : " + msgUuid + " , PackageField : " + packageField
					+ " , ExecuteCampaignRequestField : " + executeCampaignRequestField
					+ " , Condition matched : true");

			return true;

		} else {
			logger.info("Message UUID (13.D) : " + msgUuid + " , PackageField : " + packageField
					+ " , ExecuteCampaignRequestField : " + executeCampaignRequestField
					+ " , Condition matched : false");
			analysisLog.debug("Message UUID (13.D) : " + msgUuid + " , PackageField : " + packageField
					+ " , ExecuteCampaignRequestField : " + executeCampaignRequestField
					+ " , Condition matched : false");

			return false;

		}

//		return (packageField.equalsIgnoreCase(Constants.ANY)
//				|| packageField.equalsIgnoreCase(executeCampaignRequestField)
//				|| packageField.equalsIgnoreCase(Constants.EMPTY_STRING) || executeCampaignRequestField == null);

	}

	public boolean configFieldCheck(String packageField, String executeCampaignRequestField, String msgUuid) {

		if (packageField.equalsIgnoreCase(Constants.ANY)
				|| (packageField != null && executeCampaignRequestField != null
						&& packageField.trim().equalsIgnoreCase(executeCampaignRequestField.trim()))
				|| packageField.equalsIgnoreCase(executeCampaignRequestField) || packageField == null) {

			logger.info("Message UUID (13.A) : " + msgUuid + " , PackageField : " + packageField
					+ " , ExecuteCampaignRequestField : " + executeCampaignRequestField
					+ " , Condition matched : true");


			return true;

		} else {
			logger.info("Message UUID (13.B) : " + msgUuid + " , PackageField : " + packageField
					+ " , ExecuteCampaignRequestField : " + executeCampaignRequestField
					+ " , Condition matched : false");
					return false;
		}

//		return (packageField.equalsIgnoreCase(Constants.ANY)
//				|| packageField.equalsIgnoreCase(executeCampaignRequestField) || packageField == null);
	}

	private void updateToPackageConfigInCampaign(ExecuteCampaignRequest executeCampaignRequest,
			List<CampaignStep> allSteps, DeviceCampaignStatusDTO deviceCampaignStatusDTO, String msgUuid) {
		LocalDateTime ldt = LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC);
		Instant now = ldt.toInstant(ZoneOffset.UTC);
	
		CampaignStepDeviceDetail currentStepInfo = null;
		CampaignStep campStep = null;
//		String campaignName = campStep.getCampaign().getCampaignName();
		
		logger.info("Message UUID (11) : " + msgUuid + ", inside updateToPackageConfigInCampaign "+ ". campaignName "  );
		analysisLog.debug("Message UUID (11) : " + msgUuid + ", inside updateToPackageConfigInCampaign ");

		if (allSteps != null && allSteps.size() > 0) {

			logger.info("Message UUID (12) : " + msgUuid + ". campaignName " + ", allSteps size " + allSteps.size());
			analysisLog.debug("Message UUID (12) : " + msgUuid + ", allSteps size " + allSteps.size());

			for (CampaignStep campaignStep : allSteps) {
				Package toPackage = campaignStep.getToPackage();

				StringBuilder sb = new StringBuilder();
				sb.append("Message UUID (13): " + msgUuid).append("   .   ");
				sb.append(
						"Comparing toPackage fields with executeCampaignRequest fields in updateToPackageConfigInCampaign method. ")
						.append("   .   ");
				sb.append("ToPackage AppVer : " + toPackage.getAppVersion()).append(" | ")
						.append("ExecuteCampaignRequest AppVer : " + executeCampaignRequest.getSwVersionApplication())
						.append("   .   ");
				sb.append("ToPackage BinVer: " + toPackage.getBinVersion()).append(" | ")
						.append("ExecuteCampaignRequest BinVer: " + executeCampaignRequest.getSwVersionBaseband())
						.append("   .   ");
				sb.append("ToPackage BleVer: " + toPackage.getBleVersion()).append(" | ")
						.append("ExecuteCampaignRequest BleVer: " + executeCampaignRequest.getBleVersion())
						.append("   .   ");
				sb.append("ToPackage McuVer: " + toPackage.getMcuVersion()).append(" | ")
						.append("ExecuteCampaignRequest McuVer: " + executeCampaignRequest.getExtenderVersion())
						.append("   .   ");
				sb.append("ToPackage Config 1: " + toPackage.getConfig1() + ", " + toPackage.getConfig1Crc())
						.append(" | ")
						.append("ExecuteCampaignRequest Config 1: " + executeCampaignRequest.getConfig1CIV() + ", "
								+ executeCampaignRequest.getConfig1CRC())
						.append("   .   ");
				sb.append("ToPackage Config 2: " + toPackage.getConfig2() + ", " + toPackage.getConfig2Crc())
						.append(" | ")
						.append("ExecuteCampaignRequest Config 2: " + executeCampaignRequest.getConfig2CIV() + ", "
								+ executeCampaignRequest.getConfig2CRC())
						.append("   .   ");
				sb.append("ToPackage Config 3: " + toPackage.getConfig3() + ", " + toPackage.getConfig3Crc())
						.append(" | ")
						.append("ExecuteCampaignRequest Config 3: " + executeCampaignRequest.getConfig3CIV() + ", "
								+ executeCampaignRequest.getConfig3CRC())
						.append("   .   ");
				sb.append("ToPackage Config 4: " + toPackage.getConfig4() + ", " + toPackage.getConfig4Crc())
						.append(" | ")
						.append("ExecuteCampaignRequest Config 4: " + executeCampaignRequest.getConfig4CIV() + ", "
								+ executeCampaignRequest.getConfig4CRC())
						.append("   .   ");
				sb.append("ToPackage Maxbotix Firmware Ver : " + toPackage.getCargoMaxbotixFirmware()).append(" | ")
						.append("ExecuteCampaignRequest Maxbotix Firmware Ver : "
								+ executeCampaignRequest.getMaxbotixFirmware())
						.append("   .   ");
				sb.append("ToPackage Maxbotix Hardware Ver : " + toPackage.getCargoMaxbotixHardware()).append(" | ")
						.append("ExecuteCampaignRequest Maxbotix Firmware Ver : "
								+ executeCampaignRequest.getMaxbotixHardware())
						.append("   .   ");
				sb.append("ToPackage Riot Firmware Ver : " + toPackage.getCargoRiotFirmware()).append(" | ").append(
						"ExecuteCampaignRequest Riot Firmware Ver : " + executeCampaignRequest.getRiotFirmware())
						.append("   .   ");
				sb.append("ToPackage Riot Hardware Ver : " + toPackage.getCargoRiotHardware()).append(" | ").append(
						"ExecuteCampaignRequest Riot Hardware Ver : " + executeCampaignRequest.getRiotHardware())
						.append("   .   ");
				sb.append("ToPackage Lite-Senty App Ver : " + toPackage.getLiteSentryApp()).append(" | ").append(
						"ExecuteCampaignRequest Lite-Sentry App Ver : " + executeCampaignRequest.getLiteSentryApp())
						.append("   .   ");
				sb.append("ToPackage Lite-Senty Boot Ver : " + toPackage.getLiteSentryBoot()).append(" | ").append(
						"ExecuteCampaignRequest Lite-Sentry Boot Ver : " + executeCampaignRequest.getLiteSentryBoot())
						.append("   .   ");
				sb.append("ToPackage Lite-Sentry Hardware Ver : " + toPackage.getLiteSentryHardware()).append(" | ")
						.append("ExecuteCampaignRequest Lite-Sentry Hardware Ver : "
								+ executeCampaignRequest.getLiteSentryHw())
						.append("   .   ");
				sb.append("ToPackage Micro SP MCU Ver : " + toPackage.getMicrospMcu()).append(" | ")
						.append("ExecuteCampaignRequest Micro SP MCU Ver : " + executeCampaignRequest.getSteMcu())
						.append("   .   ");
				sb.append("ToPackage Micro SP App Ver : " + toPackage.getMicrospApp()).append(" | ")
						.append("ExecuteCampaignRequest Micro SP App Ver : " + executeCampaignRequest.getSteApp())
						.append("   .   ");
				sb.append("ToPackage Device Type : " + toPackage.getDeviceType()).append(" | ")
						.append("ExecuteCampaignRequest  Device Type : " + executeCampaignRequest.getDevice_type())
						.append("   .   ");

				logger.info(sb.toString());
				analysisLog.debug(sb.toString());

				if (configFieldCheck(toPackage.getAppVersion(), executeCampaignRequest.getSwVersionApplication(),
						msgUuid)
						&& configFieldCheck(toPackage.getBinVersion(), executeCampaignRequest.getSwVersionBaseband(),
								msgUuid)
						&& configFieldCheck(toPackage.getBleVersion(), executeCampaignRequest.getBleVersion(), msgUuid)
						&& configFieldCheck(toPackage.getMcuVersion(), executeCampaignRequest.getExtenderVersion(),
								msgUuid)
						&& configFieldCheck(toPackage.getDeviceType(), executeCampaignRequest.getDevice_type(), msgUuid)
						&& configFieldCheck(toPackage.getCargoMaxbotixFirmware(),
								executeCampaignRequest.getMaxbotixFirmware(), msgUuid)
						&& configFieldCheck(toPackage.getCargoMaxbotixHardware(),
								executeCampaignRequest.getMaxbotixHardware(), msgUuid)
						&& configFieldCheck(toPackage.getCargoRiotFirmware(), executeCampaignRequest.getRiotFirmware(),
								msgUuid)
						&& configFieldCheck(toPackage.getCargoRiotHardware(), executeCampaignRequest.getRiotHardware(),
								msgUuid)
						&& configFieldCheck(toPackage.getMicrospApp(), executeCampaignRequest.getSteApp(), msgUuid)
						&& configFieldCheck(toPackage.getMicrospMcu(), executeCampaignRequest.getSteMcu(), msgUuid)
						&& configFieldCheck(toPackage.getLiteSentryApp(), executeCampaignRequest.getLiteSentryApp(),
								msgUuid)
						&& configFieldCheck(toPackage.getLiteSentryBoot(), executeCampaignRequest.getLiteSentryBoot(),
								msgUuid)
						&& configFieldCheck(toPackage.getLiteSentryHardware(), executeCampaignRequest.getLiteSentryHw(),
								msgUuid)
						&& waterfallFieldCheck(toPackage.getConfig1(), executeCampaignRequest.getConfig1CIV(), msgUuid)
						&& waterfallFieldCheck(toPackage.getConfig2(), executeCampaignRequest.getConfig2CIV(), msgUuid)
						&& waterfallFieldCheck(toPackage.getConfig3(), executeCampaignRequest.getConfig3CIV(), msgUuid)
						&& waterfallFieldCheck(toPackage.getConfig4(), executeCampaignRequest.getConfig4CIV(), msgUuid)
						&& waterfallFieldCheck(toPackage.getConfig1Crc(), executeCampaignRequest.getConfig1CRC(),
								msgUuid)
						&& waterfallFieldCheck(toPackage.getConfig2Crc(), executeCampaignRequest.getConfig2CRC(),
								msgUuid)
						&& waterfallFieldCheck(toPackage.getConfig3Crc(), executeCampaignRequest.getConfig3CRC(),
								msgUuid)
						&& waterfallFieldCheck(toPackage.getConfig4Crc(), executeCampaignRequest.getConfig4CRC(),
								msgUuid)) {
					logger.info("Message UUID (14) : " + msgUuid + ", Conditions fulfilled for Campaign: "
							+ ". campaignName " );
					analysisLog.debug("Message UUID (14) : " + msgUuid + ", Conditions fulfilled for Campaign: "
							+ campaignStep.getCampaign().getUuid());
					campStep = campaignStep;
					break;
				}

			}
		} else
			logger.info("Message UUID (15) : " + msgUuid +  " campStep " + campStep);
		analysisLog.debug("Message UUID (15) : " + msgUuid +  " campStep " + campStep);
		if (campStep != null) {

			// fetch device detail object where status is not failed
			currentStepInfo = stepDeviceDetailRepository
					.findStatusByCampaignUuidAndStepUuid(executeCampaignRequest.getDeviceId(), campStep.getUuid());
			try {
				logger.info("Message UUID (16) : " + msgUuid +  " currentStepInfo " + currentStepInfo
						+ " , currentStepUUID: " + currentStepInfo.getUuid());
				analysisLog.debug("Message UUID (16) : " + msgUuid + " currentStepInfo " + currentStepInfo
						+ " , currentStepUUID: " + currentStepInfo.getUuid());
			} catch (Exception e) {
				logger.info("Message UUID (16.b) : current step is null" );
				analysisLog.debug("Message UUID (16.b) : current step is null");
			}
			if (currentStepInfo != null && currentStepInfo.getStatus() != null
					&& (currentStepInfo.getStatus().equals(CampaignStepDeviceStatus.PENDING))) {

				// update status from pending to success for found campaign step
				deviceCampaignStatusDTO.setLastSuccessStepUUID(currentStepInfo.getUuid());
				deviceCampaignStatusDTO.setCampaignRunningStatus(CampaignStepDeviceStatus.SUCCESS);
				currentStepInfo.setStatus(CampaignStepDeviceStatus.SUCCESS);
				deviceCampaignStatusDTO.setLastSuccessStepUUID(campStep.getUuid());
				currentStepInfo.setStopExecutionTime(now);
				stepDeviceDetailRepository.save(currentStepInfo);
				logger.info("Message UUID (17) : " + msgUuid + ". Device ID " + executeCampaignRequest.getDeviceId()
						+ " successfully upgraded to package  : " + campStep.getToPackage().getPackageName());
					}

		}
	}

	private String checkFromPackageConfigInCampaign(ExecuteCampaignRequest executeCampaignRequest,
			List<CampaignStep> allSteps, String campaignName, DeviceCampaignStatusDTO deviceCampaignStatusDTO,
			String msgUuid) {
		// TODO Auto-generated method stub
		LocalDateTime ldt = LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC);
		Instant now = ldt.toInstant(ZoneOffset.UTC);
		CampaignStepDeviceStatus currentStepStatus = null;
		CampaignStepDeviceDetail currentStepInfo = null;
		String previousStepStatus = null;
		boolean currentStepDecision = false;
		boolean previousStepDecision = true;
		logger.info("Message UUID (40) : " + msgUuid + ". campaignName " + campaignName);
	
		logger.info("Message UUID (41) : " + msgUuid + " Device " + executeCampaignRequest.getDeviceId()
				+ " belong to campaign " + campaignName);
	
		CampaignStep campStep = null;

		if (allSteps != null && allSteps.size() > 0) {
			logger.info("Message UUID (42) :" + msgUuid + " allSteps " + allSteps.size());
			
			for (CampaignStep campaignStep : allSteps) {
				Package fromPackage = campaignStep.getFromPackage();

				// delete object of CampaignConfigProblem if above conditions are bypassed
				try {
					configProblemRepository.deleteByDeviceIdAndCampaignId(executeCampaignRequest.getDeviceId(),
							campaignStep.getCampaign().getCampaignId());
					deviceCampaignStatusDTO.setProblemComment(null);
					deviceCampaignStatusDTO.setProblemStatus(null);

					logger.info("Message UUID (43) : " + msgUuid
							+ " Object of CampaignConfigProblem is deleted with device with id: "
							+ executeCampaignRequest.getDeviceId() + " and campaign:  " + campaignName);

				} catch (Exception e) {
					logger.error("Message UUID (44) : " + msgUuid + " Error in deleting device with id: "
							+ executeCampaignRequest.getDeviceId() + " and campaign:  " + campaignName);
				}

				if (executeCampaignRequest.getConfig2CIV() == "" || executeCampaignRequest.getConfig3CIV() == ""
						|| executeCampaignRequest.getConfig4CIV() == "") {
					logger.info("Message UUID (45) : " + msgUuid + " executeCampaignRequest " + executeCampaignRequest);

		// db entry device level
					CampaignConfigProblem ccp = new CampaignConfigProblem();
					ccp.setImei(executeCampaignRequest.getDeviceId());
					ccp.setCampaignName(campaignName);
					ccp.setComments("One or more of the device configs has an invalid filename.");
					ccp.setDeviceStatusForCampaign(DeviceStatusForCampaign.PROBLEM.getValue());
					// ccp.setCampaign(campaignStep.getCampaign());
					ccp.setCampaignId(campaignStep.getCampaign().getCampaignId());
					configProblemRepository.save(ccp);
					deviceCampaignStatusDTO.setProblemStatus(DeviceStatusForCampaign.PROBLEM.getValue());
					deviceCampaignStatusDTO
							.setProblemComment("One or more of the device configs has an invalid filename.");
					logger.info("Message UUID (46): " + msgUuid
							+ " ,Object of CampaignConfigProblem is saved as 'One or more of the device configs has an invalid filename.'  with device with id: "
							+ executeCampaignRequest.getDeviceId() + " and campaignId:  "
							+ campaignStep.getCampaign().getCampaignId());

					return null;
				}

				logger.info("Message UUID (47) : " + msgUuid + " executeCampaignRequest " + executeCampaignRequest);
			
				if ((executeCampaignRequest.getConfig2CIV() != null
						&& fromPackage.getConfig2().trim().equalsIgnoreCase(Constants.MISSING)
						&& !fromPackage.getConfig2().equalsIgnoreCase(executeCampaignRequest.getConfig2CIV()))
						|| (executeCampaignRequest.getConfig3CIV() != null
								&& fromPackage.getConfig3().trim().equalsIgnoreCase(Constants.MISSING)
								&& !fromPackage.getConfig3().equalsIgnoreCase(executeCampaignRequest.getConfig3CIV()))
						|| (executeCampaignRequest.getConfig4CIV() != null
								&& fromPackage.getConfig4().trim().equalsIgnoreCase(Constants.MISSING) && !fromPackage
										.getConfig4().equalsIgnoreCase(executeCampaignRequest.getConfig4CIV()))) {

					String exeStatus = stepDeviceDetailRepository.getStepSatusByStepUuid(campaignStep.getUuid(),
							executeCampaignRequest.getDeviceId());

					logger.info("Message UUID (48) : " + msgUuid + " exeStatus " + exeStatus
							+ " , checking Missing conditions");
					// db entry device level
					if (exeStatus == null
							|| (!CampaignStepDeviceStatus.SUCCESS.getValue().equalsIgnoreCase(exeStatus))) {

						CampaignConfigProblem ccp = new CampaignConfigProblem();
						ccp.setImei(executeCampaignRequest.getDeviceId());
						ccp.setCampaignName(campaignName);
						ccp.setComments(
								"Missing was selected for one or more config field whereas Device report has config file present for the same.");
						ccp.setDeviceStatusForCampaign(DeviceStatusForCampaign.PROBLEM.getValue());
						deviceCampaignStatusDTO.setProblemStatus(DeviceStatusForCampaign.PROBLEM.getValue());
						deviceCampaignStatusDTO.setProblemComment(
								"Missing was selected for one or more config field whereas Device report has config file present for the same.");

						// ccp.setCampaign(campaignStep.getCampaign());
						ccp.setCampaignId(campaignStep.getCampaign().getCampaignId());
						configProblemRepository.save(ccp);
						logger.info("Message UUID (49) : " + msgUuid
								+ ", Object of CampaignConfigProblem is saved as 'Missing was selected for one or more config field whereas Device report has config file present for the same.'  with device with id: "
								+ executeCampaignRequest.getDeviceId() + " and campaignId:  "
								+ campaignStep.getCampaign().getCampaignId());
						return null;
					}
				}

				StringBuilder sb = new StringBuilder();
				sb.append("Message UUID (50.A) : " + msgUuid).append("   .   " + campaignName);
				sb.append(
						"Comparing FromPackage fields with executeCampaignRequest fields in checkFromPackageConfigInCampaign method. ")
						.append("   .   ");
				sb.append("fromPackage AppVer : " + fromPackage.getAppVersion()).append(" | ")
						.append("ExecuteCampaignRequest AppVer : " + executeCampaignRequest.getSwVersionApplication())
						.append("   .   ");
				sb.append("fromPackage BinVer: " + fromPackage.getBinVersion()).append(" | ")
						.append("ExecuteCampaignRequest BinVer: " + executeCampaignRequest.getSwVersionBaseband())
						.append("   .   ");
				sb.append("fromPackage BleVer: " + fromPackage.getBleVersion()).append(" | ")
						.append("ExecuteCampaignRequest BleVer: " + executeCampaignRequest.getBleVersion())
						.append("   .   ");
				sb.append("fromPackage McuVer: " + fromPackage.getMcuVersion()).append(" | ")
						.append("ExecuteCampaignRequest McuVer: " + executeCampaignRequest.getExtenderVersion())
						.append("   .   ");
				sb.append("fromPackage Config 1: " + fromPackage.getConfig1() + ", " + fromPackage.getConfig1Crc())
						.append(" | ")
						.append("ExecuteCampaignRequest Config 1: " + executeCampaignRequest.getConfig1CIV() + ", "
								+ executeCampaignRequest.getConfig1CRC())
						.append("   .   ");
				sb.append("fromPackage Config 2: " + fromPackage.getConfig2() + ", " + fromPackage.getConfig2Crc())
						.append(" | ")
						.append("ExecuteCampaignRequest Config 2: " + executeCampaignRequest.getConfig2CIV() + ", "
								+ executeCampaignRequest.getConfig2CRC())
						.append("   .   ");
				sb.append("fromPackage Config 3: " + fromPackage.getConfig3() + ", " + fromPackage.getConfig3Crc())
						.append(" | ")
						.append("ExecuteCampaignRequest Config 3: " + executeCampaignRequest.getConfig3CIV() + ", "
								+ executeCampaignRequest.getConfig3CRC())
						.append("   .   ");
				sb.append("fromPackage Config 4: " + fromPackage.getConfig4() + ", " + fromPackage.getConfig4Crc())
						.append(" | ")
						.append("ExecuteCampaignRequest Config 4: " + executeCampaignRequest.getConfig4CIV() + ", "
								+ executeCampaignRequest.getConfig4CRC())
						.append("   .   ");
				sb.append("fromPackage Maxbotix Firmware Ver : " + fromPackage.getCargoMaxbotixFirmware()).append(" | ")
						.append("ExecuteCampaignRequest Maxbotix Firmware Ver : "
								+ executeCampaignRequest.getMaxbotixFirmware())
						.append("   .   ");
				sb.append("fromPackage Maxbotix Hardware Ver : " + fromPackage.getCargoMaxbotixHardware()).append(" | ")
						.append("ExecuteCampaignRequest Maxbotix Firmware Ver : "
								+ executeCampaignRequest.getMaxbotixHardware())
						.append("   .   ");
				sb.append("fromPackage Riot Firmware Ver : " + fromPackage.getCargoRiotFirmware()).append(" | ").append(
						"ExecuteCampaignRequest Riot Firmware Ver : " + executeCampaignRequest.getRiotFirmware())
						.append("   .   ");
				sb.append("fromPackage Riot Hardware Ver : " + fromPackage.getCargoRiotHardware()).append(" | ").append(
						"ExecuteCampaignRequest Riot Hardware Ver : " + executeCampaignRequest.getRiotHardware())
						.append("   .   ");
				sb.append("fromPackage Lite-Sentry App Ver : " + fromPackage.getLiteSentryApp()).append(" | ").append(
						"ExecuteCampaignRequest Lite-Sentry App Ver : " + executeCampaignRequest.getLiteSentryApp())
						.append("   .   ");
				sb.append("fromPackage Lite-Sentry Boot Ver : " + fromPackage.getLiteSentryBoot()).append(" | ").append(
						"ExecuteCampaignRequest Lite-Sentry Boot Ver : " + executeCampaignRequest.getLiteSentryBoot())
						.append("   .   ");
				sb.append("fromPackage Lite-Sentry Hardware Ver : " + fromPackage.getLiteSentryHardware()).append(" | ")
						.append("ExecuteCampaignRequest Lite-Sentry Hardware Ver : "
								+ executeCampaignRequest.getLiteSentryHw())
						.append("   .   ");
				sb.append("fromPackage Micro SP MCU Ver : " + fromPackage.getMicrospMcu()).append(" | ")
						.append("ExecuteCampaignRequest Micro SP MCU Ver : " + executeCampaignRequest.getSteMcu())
						.append("   .   ");
				sb.append("fromPackage Micro SP App Ver : " + fromPackage.getMicrospApp()).append(" | ")
						.append("ExecuteCampaignRequest Micro SP App Ver : " + executeCampaignRequest.getSteApp())
						.append("   .   ");
				sb.append("fromPackage Device Type : " + fromPackage.getDeviceType()).append(" | ")
						.append("ExecuteCampaignRequest  Device Type : " + executeCampaignRequest.getDevice_type())
						.append("   .   ");

				logger.info(sb.toString());
				
				if (configFieldCheck(fromPackage.getAppVersion(), executeCampaignRequest.getSwVersionApplication(),
						msgUuid)
						&& configFieldCheck(fromPackage.getBinVersion(), executeCampaignRequest.getSwVersionBaseband(),
								msgUuid)
						&& configFieldCheck(fromPackage.getBleVersion(), executeCampaignRequest.getBleVersion(),
								msgUuid)
						&& configFieldCheck(fromPackage.getMcuVersion(), executeCampaignRequest.getExtenderVersion(),
								msgUuid)
						&& configFieldCheck(fromPackage.getDeviceType(), executeCampaignRequest.getDevice_type(),
								msgUuid)
						&& configFieldCheck(fromPackage.getCargoMaxbotixFirmware(),
								executeCampaignRequest.getMaxbotixFirmware(), msgUuid)
						&& configFieldCheck(fromPackage.getCargoMaxbotixHardware(),
								executeCampaignRequest.getMaxbotixHardware(), msgUuid)
						&& configFieldCheck(fromPackage.getCargoRiotFirmware(),
								executeCampaignRequest.getRiotFirmware(), msgUuid)
						&& configFieldCheck(fromPackage.getCargoRiotHardware(),
								executeCampaignRequest.getRiotHardware(), msgUuid)
						&& configFieldCheck(fromPackage.getMicrospApp(), executeCampaignRequest.getSteApp(), msgUuid)
						&& configFieldCheck(fromPackage.getMicrospMcu(), executeCampaignRequest.getSteMcu(), msgUuid)
						&& configFieldCheck(fromPackage.getLiteSentryApp(), executeCampaignRequest.getLiteSentryApp(),
								msgUuid)
						&& configFieldCheck(fromPackage.getLiteSentryBoot(), executeCampaignRequest.getLiteSentryBoot(),
								msgUuid)
						&& configFieldCheck(fromPackage.getLiteSentryHardware(),
								executeCampaignRequest.getLiteSentryHw(), msgUuid)
						&& waterfallFieldCheck(fromPackage.getConfig1Crc(), executeCampaignRequest.getConfig1CRC(),
								msgUuid)
						&& waterfallFieldCheck(fromPackage.getConfig2Crc(), executeCampaignRequest.getConfig2CRC(),
								msgUuid)
						&& waterfallFieldCheck(fromPackage.getConfig3Crc(), executeCampaignRequest.getConfig3CRC(),
								msgUuid)
						&& waterfallFieldCheck(fromPackage.getConfig4Crc(), executeCampaignRequest.getConfig4CRC(),
								msgUuid)
						&& waterfallFieldCheck(fromPackage.getConfig1(), executeCampaignRequest.getConfig1CIV(),
								msgUuid)
						&& waterfallFieldCheck(fromPackage.getConfig2(), executeCampaignRequest.getConfig2CIV(),
								msgUuid)
						&& waterfallFieldCheck(fromPackage.getConfig3(), executeCampaignRequest.getConfig3CIV(),
								msgUuid)
						&& waterfallFieldCheck(fromPackage.getConfig4(), executeCampaignRequest.getConfig4CIV(),
								msgUuid)) {
					logger.info("Message UUID (50.B) : " + msgUuid
							+ " ,campaignStep determined in checkFromPackageConfigInCampaign. "+ campaignName + " campaignStep "
							+ campaignStep);
					analysisLog.debug("Message UUID (50.B) : " + msgUuid
							+ " ,campaignStep determined in checkFromPackageConfigInCampaign. " + " campaignStep "
							+ campaignStep);
					campStep = campaignStep;
					break;
				}

			}
		}
		logger.info("Message UUID (51) : " + msgUuid + " canpaign " +campaignName +  " campStep " + campStep);
		
		
		analysisLog.debug("Message UUID (51) : " + msgUuid + " campStep " + campStep);
		if (campStep == null) {
			logger.info("Message UUID (52) : " + msgUuid + " No match found for device "
					+ executeCampaignRequest.getDeviceId() + " config in from_package for campaign " + campaignName);
			analysisLog.debug("Message UUID (52) : " + msgUuid + " No match found for device "
					+ executeCampaignRequest.getDeviceId() + " config in from_package for campaign " + campaignName);
			return null;

		}
		if (campStep.getStepOrderNumber() > 1) {

			previousStepStatus = stepDeviceDetailRepository.findStatusByCampaignUuidAndStepOrderNumber(
					campStep.getCampaign().getUuid(), executeCampaignRequest.getDeviceId(),
					campStep.getStepOrderNumber() - 1);
			logger.info("Message UUID (53) : " + msgUuid +  " canpaign " + campaignName + " , campStep.getUuid() : " + campStep.getUuid()
					+ " , campStep.getStepOrderNumber() : " + campStep.getStepOrderNumber() + " , previousStepStatus : "
					+ previousStepStatus);
			analysisLog.debug("Message UUID (53) : " + msgUuid + " , campStep.getUuid() : " + campStep.getUuid()
					+ " , campStep.getStepOrderNumber() : " + campStep.getStepOrderNumber() + " , previousStepStatus : "
					+ previousStepStatus);
			if (!previousStepStatus.equalsIgnoreCase(CampaignStepDeviceStatus.SUCCESS.getValue())) {
				previousStepDecision = false;
			}

		}

		currentStepInfo = stepDeviceDetailRepository
				.findStatusByCampaignUuidAndStepUuid(executeCampaignRequest.getDeviceId(), campStep.getUuid());
		logger.info("Message UUID (54) : " + msgUuid + " canpaign " + campaignName  + " currentStepInfo " + currentStepInfo + " previousStepStatus "
				+ previousStepStatus);
		analysisLog.debug("Message UUID (54) : " + msgUuid + " currentStepInfo " + currentStepInfo
				+ " previousStepStatus " + previousStepStatus);
		if (previousStepDecision) {
			// create an entry for current step in CampaignStepDeviceDetail if not found
			if (currentStepInfo == null) {
				currentStepDecision = true;
				currentStepInfo = new CampaignStepDeviceDetail();
				currentStepInfo.setCampaign(campStep.getCampaign());
				currentStepInfo.setCampaignStep(campStep);
				currentStepInfo.setDeviceId(executeCampaignRequest.getDeviceId());
				currentStepInfo.setStartExecutionTime(now);
				currentStepInfo.setStatus(CampaignStepDeviceStatus.PENDING);
				currentStepInfo.setUuid(generateStepRunUuid());
				deviceCampaignStatusDTO.setLastPendingStepUUID(campStep.getUuid());
				logger.info("Message UUID (55) : " + msgUuid + " , Device " + executeCampaignRequest.getDeviceId()
						+ " upgrade match found for campaign " + campaignName + " step " + campStep.getStepOrderNumber()
						+ " Step UUID : " + campStep.getUuid() + "NOW "+now);
				analysisLog.debug("Message UUID (55) : " + msgUuid + " , Device " + executeCampaignRequest.getDeviceId()
						+ " upgrade match found for campaign " + campaignName + " step " + campStep.getStepOrderNumber()
						+ " Step UUID : " + campStep.getUuid());
			}
			// update an entry for current step in CampaignStepDeviceDetail also check for
			// number of retries before executing command
			else {
				currentStepStatus = currentStepInfo.getStatus();
				logger.info("Message UUID (56) : " + msgUuid +  " canpaign " + campaignName  +" currentStepInfo " + currentStepInfo
						+ " , currentStep Uuid : " + currentStepInfo.getUuid());
				analysisLog.debug("Message UUID (56) : " + msgUuid + " currentStepInfo " + currentStepInfo
						+ " , currentStep Uuid : " + currentStepInfo.getUuid());
				if (currentStepStatus != null && (currentStepStatus.equals(CampaignStepDeviceStatus.PENDING))
						&& ((Duration.between(currentStepInfo.getStartExecutionTime(), Instant.now()).abs().toMinutes() > 15))) {
					Long reAttempt = stepDeviceDetailRepository.findByCampaignUuidAndStepUuidAndFailedStatus(
							executeCampaignRequest.getDeviceId(), campStep.getUuid());
					logger.info("Message UUID (57) : " + msgUuid +  " canpaign " + campaignName  + " reAttempt# " + reAttempt +"  Now "+now);
					analysisLog.debug("Message UUID (57) : " + msgUuid + " reAttempt# " + reAttempt);
					//
					if (reAttempt < Constants.MAX_FAILED_ATTEMPT_FOR_UPDATE) {
						currentStepDecision = true;
						currentStepInfo.setStopExecutionTime(now);
						currentStepInfo.setStatus(CampaignStepDeviceStatus.FAILED);

						CampaignStepDeviceDetail reAttemptStep = new CampaignStepDeviceDetail();

						reAttemptStep.setCampaign(campStep.getCampaign());
						reAttemptStep.setCampaignStep(campStep);
						reAttemptStep.setDeviceId(executeCampaignRequest.getDeviceId());
						reAttemptStep.setStartExecutionTime(now);
						reAttemptStep.setStatus(CampaignStepDeviceStatus.PENDING);
						reAttemptStep.setUuid(generateStepRunUuid());
						stepDeviceDetailRepository.save(reAttemptStep);

						logger.info("Message UUID (58) : " + msgUuid + " canpaign " + campaignName  +" . Device "
								+ executeCampaignRequest.getDeviceId() + " upgrade re-attempt");
						analysisLog.debug("Message UUID (58) : " + msgUuid + " . Device "
								+ executeCampaignRequest.getDeviceId() + " upgrade re-attempt");
					} else {
						deviceCampaignStatusDTO.setCampaignRunningStatus(CampaignStepDeviceStatus.PROBLEM);
						logger.info("Message UUID (59) : " + msgUuid + " canpaign " + campaignName  +" Device " + executeCampaignRequest.getDeviceId()
								+ " upgrade maximum attempt reached");
						analysisLog.debug("Message UUID (59) : " + msgUuid + " Device "
								+ executeCampaignRequest.getDeviceId() + " upgrade maximum attempt reached");
					}
				} else {
					logger.info("Message UUID (60) : " + msgUuid + " canpaign " + campaignName  + " . Device " + executeCampaignRequest.getDeviceId()
							+ " upgrade already requested");
					analysisLog.debug("Message UUID (60) : " + msgUuid + " . Device "
							+ executeCampaignRequest.getDeviceId() + " upgrade already requested");
				}
			}

			if (currentStepDecision) {
				deviceCampaignStatusDTO.setLastPendingStepUUID(currentStepInfo.getUuid());
				deviceCampaignStatusDTO.setCampaignRunningStatus(CampaignStepDeviceStatus.PENDING);
				deviceCampaignStatusDTO.setLastPendingStepUUID(campStep.getUuid());
				logger.info("Message UUID (61) : " + " canpaign " + campaignName  + msgUuid + " currentStepDecision " + currentStepDecision
						+ ", saving currentStep object." + " CurrentStepInfo UUID: " + currentStepInfo.getUuid());
				analysisLog.debug("Message UUID (61) : " + msgUuid + " currentStepDecision " + currentStepDecision
						+ ", saving currentStep object." + " CurrentStepInfo UUID: " + currentStepInfo.getUuid());
				stepDeviceDetailRepository.save(currentStepInfo);
				return campStep.getAtCommand();
			}
		}
		return null;
	}

	private boolean checkPauseInCampaign(Campaign campaign, String deiceId, String msgUuid) {
		String campaignName = campaign.getCampaignName(); 
		logger.info("Message UUID (29) : " + msgUuid + "checkPauseInCampaign for  canpaign " + campaignName  + " deiceId " + deiceId);
		analysisLog.debug("Message UUID  (29) : " + msgUuid + " deiceId " + deiceId);
		CampaignPauseView pauseDetails = campaignRepo.findCampaignStepLimit(campaign.getUuid());
		if (!pauseDetails.getPause_execution()) {
			return false;
		}
		Long stepLimit = pauseDetails.getPause_limit();
		List<Object> executedGatewayList = stepDeviceDetailRepository.findCountOfExecutedGateways(campaign.getUuid());
		if (executedGatewayList == null) {
			return false;
		}
		int executedGatewayCount = executedGatewayList.size();
		if (executedGatewayCount < stepLimit
				|| (executedGatewayCount == stepLimit && executedGatewayList.contains(deiceId))) {
			return false;
		}
		logger.info("Message UUID (30) : " + msgUuid + " Device ID " + deiceId + " can not proceed, campaign "
				+ campaign.getCampaignName() + " is in paused state ");
		analysisLog.debug("Message UUID (30) : " + msgUuid + " Device ID " + deiceId + " can not proceed, campaign "
				+ campaign.getCampaignName() + " is in paused state ");
		return true;

	}

	@Override
	public String manageCampaignStatus(String campaignUuid, String userName, String status, Long pauseLimit) {
		// TODO Auto-generated method stub
		
		LocalDateTime ldt = LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC);
		Instant now = ldt.toInstant(ZoneOffset.UTC);
		if (status == null) {
			throw new DeviceVersionException("Desired campaign status is null");
		}
		CampaignStatus campaignStatus = CampaignStatus.getCampaignStatus(status);
		if (campaignStatus == null) {
			throw new DeviceVersionException("Desired campaign status is not valid");
		}
		User user = restUtils.getUserFromAuthService(userName);
		Campaign campaignData = campaignRepo.findByUuid(campaignUuid);
		if (campaignData == null) {
			throw new DeviceVersionException("Campaign not found for id");
		}
		campaignData.setUpdatedAt(now);
		campaignData.setUpdatedBy(user);
		campaignData.setIsActive(true);
		if (campaignStatus.equals(CampaignStatus.RETIRED)) {
			campaignData.setIsActive(false);
		}
		if (campaignStatus.equals(CampaignStatus.IN_PROGRESS)
				&& CampaignStatus.NOT_STARTED.equals(campaignData.getCampaignStatus())) {
			campaignData.setCampaignStartDate(now);
		}
		campaignData.setCampaignStatus(campaignStatus);
		if (pauseLimit != null) {
			campaignData.setPauseLimit(pauseLimit);
		}
		campaignData = campaignRepo.save(campaignData);
		logger.info("Campaign " + campaignData.getCampaignName() + " " + campaignStatus.getValue());
		analysisLog.debug("Campaign " + campaignData.getCampaignName() + " " + campaignStatus.getValue());
		return campaignData.getUuid();
	}

	private String generateStepRunUuid() {
		boolean isStepDevDetailUuidUnique = false;
		String StepDDUuid = "";
		while (!isStepDevDetailUuidUnique) {
			StepDDUuid = UUID.randomUUID().toString();
			CampaignStepDeviceDetail byUuid = stepDeviceDetailRepository.findByUuid(StepDDUuid);
			if (byUuid == null) {
				isStepDevDetailUuidUnique = true;
			}
		}
		return StepDDUuid;
		// temp
	}

//	public boolean crcNull(String executeCampaignRequestField) {
//		return executeCampaignRequestField == null ? true : false;
//	};
//
//	public boolean fieldMatched(String packageField, String executeCampaignRequestField) {
//		return packageField.equalsIgnoreCase(executeCampaignRequestField) ? true : false;
//	};
//
//	public boolean packageFieldAny(String packageField) {
//		return packageField.equalsIgnoreCase(Constants.ANY) ? true : false;
//	};
//
//	public boolean packageFieldEmpty(String packageField) {
//		return packageField.equalsIgnoreCase(Constants.EMPTY_STRING) ? true : false;
//	};
}