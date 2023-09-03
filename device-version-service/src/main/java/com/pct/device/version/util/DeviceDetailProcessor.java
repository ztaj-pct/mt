package com.pct.device.version.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pct.common.dto.MsDeviceRestResponse;
import com.pct.device.service.device.DeviceReport;
import com.pct.device.version.constant.CampaignStepDeviceStatus;
import com.pct.device.version.constant.DeviceStatusForCampaign;
import com.pct.device.version.constant.StatsStatus;
import com.pct.device.version.dto.DeviceCampaignStatusDTO;
import com.pct.device.version.dto.VersionMigrationDetailDTO;
import com.pct.device.version.exception.DeviceVersionException;
import com.pct.device.version.model.Campaign;
import com.pct.device.version.model.CampaignConfigProblem;
import com.pct.device.version.model.CampaignStep;
import com.pct.device.version.model.CampaignStepDeviceDetail;
import com.pct.device.version.model.DeviceCampaignStatus;
import com.pct.device.version.model.DeviceStatus;
import com.pct.device.version.model.MultipleCampaignDevice;
import com.pct.device.version.model.Package;
import com.pct.device.version.payload.CampaignDeviceDetail;
import com.pct.device.version.payload.CampaignHyperLink;
import com.pct.device.version.payload.CampaignStatsPayload;
import com.pct.device.version.payload.DeviceStepStatus;
import com.pct.device.version.payload.PackagePayload;
import com.pct.device.version.repository.ICampaignConfigProblemRepository;
import com.pct.device.version.repository.ICampaignRepository;
import com.pct.device.version.repository.ICampaignStepDeviceDetailRepository;
import com.pct.device.version.repository.ICampaignStepRepository;
import com.pct.device.version.repository.IDeviceCampaignStatusRepository;
import com.pct.device.version.repository.IDeviceStatusRepository;
import com.pct.device.version.repository.IDeviceVersionRepository;
import com.pct.device.version.repository.ILatestDeviceMaintenanceReportRepository;
import com.pct.device.version.repository.IMultipleCampaignDeviceRepository;
import com.pct.device.version.repository.IOrganisationVersionRepository;
import com.pct.device.version.repository.IPackageRepository;
//import com.pct.device.version.repository.msdevice.ICampaignInstalledDeviceMsRepository;
import com.pct.device.version.repository.projections.StepStatusForDeviceView;
import com.pct.device.version.validation.CampaignUtils;

import lombok.Data;

@Component
@Data
public class DeviceDetailProcessor {

	Logger logger = LoggerFactory.getLogger(DeviceDetailProcessor.class);
	Logger analysisLog = LoggerFactory.getLogger("analytics");

	@Autowired
	private IPackageRepository packageRepository;
	@Autowired
	private IMultipleCampaignDeviceRepository multiCampaignDeviceRepository;
	@Autowired
	private ICampaignStepRepository campaignStepRepository;
	@Autowired
	private CampaignUtils campaignUtils;
	@Autowired
	private ICampaignStepDeviceDetailRepository deviceDetailRepository;
	@Autowired
	private CampaignDeviceHelper campaignDeviceHelper;
	@Autowired
	private ICampaignStepDeviceDetailRepository stepDeviceDetailRepository;
	@Autowired
	private ICampaignConfigProblemRepository configProblemRepository;
	@Autowired
	private ILatestDeviceMaintenanceReportRepository iLatestDeviceMaintenanceReportRepository;
	/*@Autowired
	private ICampaignInstalledDeviceMsRepository iCampaignInstalledDeviceMsRepository;
*/	@Autowired
	IDeviceCampaignStatusRepository deviceCampaignStatusRepository;
	@Autowired
	private ICampaignRepository campaignRepo;

	@Autowired
	IDeviceStatusRepository deviceStatusRepository;
	@Autowired
	AmazonSESUtil amazonSESUtil;
	@Autowired
	ICampaignRepository campaignRepository;
	@Autowired
	IDeviceVersionRepository deviceVersionRepository;
	@Autowired
	IOrganisationVersionRepository organisationVersionRepository;

	public static String mapToJson(Object object) throws JsonProcessingException {
		ObjectMapper objectMapper = new ObjectMapper();
		return objectMapper.writeValueAsString(object);

	}

	private void checkFailedStepsForDevice(Campaign campaignData, String deviceId,
			CampaignDeviceDetail campaignDeviceDetail) {

		List<String> failedStepUuid = deviceDetailRepository.getFailedExecutedStep(campaignData.getUuid(), deviceId);
		if (failedStepUuid != null && failedStepUuid.size() > 0) {
			String stepStatus = deviceDetailRepository.getStatusOfStep(failedStepUuid.get(0), deviceId);
			if (stepStatus.equalsIgnoreCase(CampaignStepDeviceStatus.PENDING.getValue())) {
				CampaignStep step = campaignStepRepository.findByUuid(failedStepUuid.get(0));
				campaignDeviceDetail.setComments(step.getFromPackage().getPackageName() + " to "
						+ step.getToPackage().getPackageName() + " " + Constants.GATEWAY_PROBLEM_STATUS);
				campaignDeviceDetail.setDeviceStatusForCampaign(DeviceStatusForCampaign.PROBLEM.getValue());
			}

			Long reAttempt = stepDeviceDetailRepository.findByCampaignUuidAndStepUuidAndFailedStatus(deviceId,
					failedStepUuid.get(0));
			if (reAttempt > Constants.MAX_FAILED_ATTEMPT_FOR_UPDATE) {
				CampaignStep step = campaignStepRepository.findByUuid(failedStepUuid.get(0));
				campaignDeviceDetail.setComments(
						"Step-" + step.getStepOrderNumber() + " could not be executed after multiple attempts.");
				campaignDeviceDetail.setDeviceStatusForCampaign(DeviceStatusForCampaign.PROBLEM.getValue());
				// amazonSESUtil.sendEmail(campaignDeviceDetail.getImei(),campaignData.getCampaignName());
				if (processEmailForProblem(campaignDeviceDetail.getImei(), campaignData, step.getUuid())) {
					logger.info("Device " + campaignDeviceDetail.getImei() + "set to problem status for campaign "
							+ campaignData.getCampaignName() + "on step " + step.getStepOrderNumber());
				}

			}
		}
	}

	private void checkFailedStepsForDevice(Campaign campaignData, String deviceId,
			DeviceCampaignStatus deviceCampaignStatus) {
		logger.info("checkFailedStepsForDevice for device :" + deviceId);
		List<String> failedStepUuid = deviceDetailRepository.getFailedExecutedStep(campaignData.getUuid(), deviceId);
		if (failedStepUuid != null && failedStepUuid.size() > 0) {
			String stepStatus = deviceDetailRepository.getStatusOfStep(failedStepUuid.get(0), deviceId);
			if (stepStatus != null && stepStatus.equalsIgnoreCase(CampaignStepDeviceStatus.PENDING.getValue())) {
				CampaignStep step = campaignStepRepository.findByUuid(failedStepUuid.get(0));
				deviceCampaignStatus.setComment(step.getFromPackage().getPackageName() + " to "
						+ step.getToPackage().getPackageName() + " " + Constants.GATEWAY_PROBLEM_STATUS);
				deviceCampaignStatus.setRunningStatus(DeviceStatusForCampaign.PROBLEM.getValue());
			}

			Long reAttempt = stepDeviceDetailRepository.findByCampaignUuidAndStepUuidAndFailedStatus(deviceId,
					failedStepUuid.get(0));
			if (reAttempt > Constants.MAX_FAILED_ATTEMPT_FOR_UPDATE) {
				CampaignStep step = campaignStepRepository.findByUuid(failedStepUuid.get(0));
				deviceCampaignStatus.setComment(
						"Step-" + step.getStepOrderNumber() + " could not be executed after multiple attempts.");
				deviceCampaignStatus.setRunningStatus(DeviceStatusForCampaign.PROBLEM.getValue());
				// amazonSESUtil.sendEmail(campaignDeviceDetail.getImei(),campaignData.getCampaignName());
				if (processEmailForProblem(deviceCampaignStatus.getDeviceId(), campaignData, step.getUuid())) {
					logger.info("Device " + deviceCampaignStatus.getDeviceId() + "set to problem status for campaign "
							+ campaignData.getCampaignName() + "on step " + step.getStepOrderNumber());
				}

			}
		}
	}
	
	
	private void checkFailedStepsForDeviceForUpdated(Campaign campaignData, String deviceId,
			DeviceCampaignStatus deviceCampaignStatus) {
		logger.info("checkFailedStepsForDevice for device :" + deviceId);
		List<String> failedStepUuid = deviceDetailRepository.getFailedExecutedStep(campaignData.getUuid(), deviceId);
		if (failedStepUuid != null && failedStepUuid.size() > 0) {
			String stepStatus = deviceDetailRepository.getStatusOfStep(failedStepUuid.get(0), deviceId);
			if (stepStatus != null && stepStatus.equalsIgnoreCase(CampaignStepDeviceStatus.PENDING.getValue())) {
				CampaignStep step = campaignStepRepository.findByUuid(failedStepUuid.get(0));
				deviceCampaignStatus.setComment(step.getFromPackage().getPackageName() + " to "
						+ step.getToPackage().getPackageName() + " " + Constants.GATEWAY_PROBLEM_STATUS);
				deviceCampaignStatus.setRunningStatus(DeviceStatusForCampaign.PROBLEM.getValue());
			}

			Long reAttempt = stepDeviceDetailRepository.findByCampaignUuidAndStepUuidAndFailedStatus(deviceId,
					failedStepUuid.get(0));
			if (reAttempt > Constants.MAX_FAILED_ATTEMPT_FOR_UPDATE) {
				CampaignStep step = campaignStepRepository.findByUuid(failedStepUuid.get(0));
				deviceCampaignStatus.setComment(
						"Step-" + step.getStepOrderNumber() + " could not be executed after multiple attempts.");
				deviceCampaignStatus.setRunningStatus(DeviceStatusForCampaign.PROBLEM.getValue());
				// amazonSESUtil.sendEmail(campaignDeviceDetail.getImei(),campaignData.getCampaignName());
				/*if (processEmailForProblem(deviceCampaignStatus.getDeviceId(), campaignData, step.getUuid())) {
					logger.info("Device " + deviceCampaignStatus.getDeviceId() + "set to problem status for campaign "
							+ campaignData.getCampaignName() + "on step " + step.getStepOrderNumber());
				}*/

			}
		}
	}
	
	
	private void checkFailedStepsForDevice(Campaign campaignData, String deviceId,
			DeviceCampaignStatus deviceCampaignStatus,Map<String,List<String>> failedStepMap) {
		logger.info("checkFailedStepsForDevice for device :" + deviceId);
		//List<String> failedStepUuid = deviceDetailRepository.getFailedExecutedStep(campaignData.getUuid(), deviceId);
		List<String> failedStepUuid = failedStepMap.get(deviceId);
		if (failedStepUuid != null && failedStepUuid.size() > 0) {
			String stepStatus = deviceDetailRepository.getStatusOfStep(failedStepUuid.get(0), deviceId);
			if (stepStatus != null && stepStatus.equalsIgnoreCase(CampaignStepDeviceStatus.PENDING.getValue())) {
				CampaignStep step = campaignStepRepository.findByUuid(failedStepUuid.get(0));
				deviceCampaignStatus.setComment(step.getFromPackage().getPackageName() + " to "
						+ step.getToPackage().getPackageName() + " " + Constants.GATEWAY_PROBLEM_STATUS);
				deviceCampaignStatus.setRunningStatus(DeviceStatusForCampaign.PROBLEM.getValue());
			}

			Long reAttempt = stepDeviceDetailRepository.findByCampaignUuidAndStepUuidAndFailedStatus(deviceId,
					failedStepUuid.get(0));
			if (reAttempt > Constants.MAX_FAILED_ATTEMPT_FOR_UPDATE) {
				CampaignStep step = campaignStepRepository.findByUuid(failedStepUuid.get(0));
				deviceCampaignStatus.setComment(
						"Step-" + step.getStepOrderNumber() + " could not be executed after multiple attempts.");
				deviceCampaignStatus.setRunningStatus(DeviceStatusForCampaign.PROBLEM.getValue());
				// amazonSESUtil.sendEmail(campaignDeviceDetail.getImei(),campaignData.getCampaignName());
				if (processEmailForProblem(deviceCampaignStatus.getDeviceId(), campaignData, step.getUuid())) {
					logger.info("Device " + deviceCampaignStatus.getDeviceId() + "set to problem status for campaign "
							+ campaignData.getCampaignName() + "on step " + step.getStepOrderNumber());
				}

			}
		}
	}

	public boolean processEmailForProblem(String deviceId, Campaign camp, String stepUuid) {
		try {
			if (stepDeviceDetailRepository.getProblemEmailCountForStep(deviceId, camp.getUuid(), stepUuid) == 0) {
				amazonSESUtil.sendEmail(deviceId, camp.getCampaignName());
				CampaignStepDeviceDetail mostRecentStep = stepDeviceDetailRepository
						.findByDeviceIdAndCampaignUuidAndCampaignStepUuid(deviceId, camp.getUuid(), stepUuid).get(0);
				mostRecentStep.setProblemEmail(true);
				stepDeviceDetailRepository.save(mostRecentStep);
				return true;
			}
		} catch (Exception e) {
			logger.info("Exception occurred while processing email");
			e.printStackTrace();
		}
		return false;
	}

	private CampaignStatsPayload setCampaignData(CampaignStatsPayload campaignStatsResponse, Campaign campaignData) {

		campaignStatsResponse.setCampaignName(campaignData.getCampaignName());
		campaignStatsResponse.setCampaignStatus(campaignData.getCampaignStatus().getValue());

		campaignStatsResponse.setDescription(campaignData.getDescription());

		campaignStatsResponse.setExcludeLowBattery(campaignData.getExcludeLowBattery());
		campaignStatsResponse.setExcludeNotInstalled(campaignData.getExcludeNotInstalled());

		campaignStatsResponse.setExcludeEngineering(campaignData.getExcludeEngineering());
		if (campaignData.getExcludeRma() != null) {
			campaignStatsResponse.setExcludeRma(campaignData.getExcludeRma());
		}
		if (campaignData.getExcludeEol() != null) {
			campaignStatsResponse.setExcludeEol(campaignData.getExcludeEol());

		}
		campaignStatsResponse.setCampaignType(campaignData.getCampaignType());
		return campaignStatsResponse;
	}


	private List<MsDeviceRestResponse> fetchIMIEForCampaign(Campaign campaignData, String msgUuid, String allImei) {

		List<MsDeviceRestResponse> allDevices = new ArrayList<MsDeviceRestResponse>();
		String groupingType = campaignData.getGroup().getGroupingType();
		String groupingName = campaignData.getGroup().getGroupingName();
		logger.info(msgUuid + " Feting device for groupingType" + groupingType);
		if (groupingType.equalsIgnoreCase("Customer")) {

			logger.info(msgUuid + " Feting device for groupingType Customer : "
					+ campaignData.getGroup().getGroupingName());

			// allDevices = restUtils.getDevicesFromMSByCustomerName(groupingName);
			// We are getting device list dto from device table using same
			// device-version-version no rest-call
			// We are getting only 3 required fields imei, customer name (organisation name
			// ) , customer id ( customer id)
			allDevices = deviceVersionRepository.findImeisByCustomerName(campaignData.getGroup().getGroupingName());

			/*
			 * if (allDevices != null) { imeiCount = Long.valueOf(allDevices.size()); }
			 */
		} else if (groupingType.equalsIgnoreCase("ALL")) {

			logger.info(msgUuid + " Feting device for groupingType ALL : ");
			// getting customers name from organisation service where
			// isApprovalReqForDeviceUpdate is false
			// getting all devices from same device service no rest-call
			//allDevices = deviceVersionRepository.findAllDevices();
			// getting customers name from organisation service where isApprovalReqForDeviceUpdate is false
			List<String> customersName = organisationVersionRepository.findOrganisationsByFirmwareFlag();
			logger.info(msgUuid + " customersName for UN-RESTRICTED flow 10001: >>>  "+ customersName);

			// getting all devices from same device service no rest-call
			allDevices = deviceVersionRepository.findAllDeviceByCustomersName(customersName);
			logger.info(msgUuid + " device count ##########  "+ allDevices.size());

			// Page<MsDeviceRestResponse> devicesFromMs =
			// restUtils.getDeviceDataFromMS(groupingName, 0, Integer.MAX_VALUE,
			// "", "");
			// allDevices = devicesFromMs.getContent();

		}

		else if (groupingType.equalsIgnoreCase("IMEI")) {
			// campaignStatsResponse.setImeiList(allImei);
			logger.info(msgUuid + " Feting device for groupingType IMEI : ");
			allImei = campaignData.getGroup().getTargetValue();
			if (!StringUtils.isEmpty(allImei)) {
				logger.info(msgUuid + " pre restUtils.getSelectedDevicesFromMSByImeis 619");
				allDevices = deviceVersionRepository.findAllDeviceByIMEIList(Arrays.asList(allImei.split(",")));
				// allDevices =
				// restUtils.getSelectedDevicesFromMSByImeis(Arrays.asList(allImei.split(",")));
				logger.info(msgUuid + " post restUtils.getSelectedDevicesFromMSByImeis 621");

			}
		}
		logger.info(msgUuid + " device count ##########  " + allDevices.size());
		return allDevices;
	}

	public PackagePayload packageToPackageResponse(Package packageData) {
		PackagePayload packageResponse = null;

		packageResponse = new PackagePayload();
		packageResponse.setPackageName(packageData.getPackageName());
		packageResponse.setAppVersion(packageData.getAppVersion());
		packageResponse.setBleVersion(packageData.getBleVersion());
		packageResponse.setMcuVersion(packageData.getMcuVersion());
		packageResponse.setBinVersion(packageData.getBinVersion());
		packageResponse.setConfig1(packageData.getConfig1());
		packageResponse.setConfig2(packageData.getConfig2());
		packageResponse.setConfig3(packageData.getConfig3());
		packageResponse.setConfig4(packageData.getConfig4());
		packageResponse.setConfig1Crc(packageData.getConfig1Crc());
		packageResponse.setConfig2Crc(packageData.getConfig2Crc());
		packageResponse.setConfig3Crc(packageData.getConfig3Crc());
		packageResponse.setConfig4Crc(packageData.getConfig4Crc());

		packageResponse.setIsDeleted(packageData.getIsDeleted());
		packageResponse.setIsUsedInCampaign(
				campaignStepRepository.findPackageUsedInCampaign(packageData.getUuid()) > 0 ? true : false);
		packageResponse.setUuid(packageData.getUuid());
		packageResponse.setCreatedAt(packageData.getCreatedAt());
		packageResponse.setUpdatedAt(packageData.getUpdatedAt());
		if (packageData.getCreatedBy() != null) {
			packageResponse.setCreatedBy(
					packageData.getCreatedBy().getFirstName() + " " + packageData.getCreatedBy().getLastName());
		}
		if (packageData.getUpdatedBy() != null) {
			packageResponse.setUpdatedBy(
					packageData.getUpdatedBy().getFirstName() + " " + packageData.getUpdatedBy().getLastName());
		}

		packageResponse.setDeviceType(packageData.getDeviceType());
		packageResponse.setLiteSentryApp(packageData.getLiteSentryApp());
		packageResponse.setLiteSentryBoot(packageData.getLiteSentryBoot());
		packageResponse.setLiteSentryHardware(packageData.getLiteSentryHardware());

		packageResponse.setMicrospMcu(packageData.getMicrospMcu());
		packageResponse.setMicrospApp(packageData.getMicrospApp());
		packageResponse.setCargoMaxbotixHardware(packageData.getCargoMaxbotixHardware());
		packageResponse.setCargoMaxbotixFirmware(packageData.getCargoMaxbotixFirmware());
		packageResponse.setCargoRiotHardware(packageData.getCargoRiotHardware());
		packageResponse.setCargoRiotFirmware(packageData.getCargoRiotFirmware());

		return packageResponse;
	}

	void gatewayEligibilityCalculationForAllDevices(String deviceId, CampaignDeviceDetail campaignDeviceDetail,
			Campaign campaignData, List<CampaignStep> allCampaignSteps, List<DeviceReport> msDeviceReports,
			String isEligible, String firstStepStatusInThisCamp, String lastStepStatusInThisCamp,
			DeviceReport deviceReport) {

		List<CampaignHyperLink> link = new ArrayList<CampaignHyperLink>();
		boolean startedInAnyOtherCamp = false;
		String multiCamp = "";
		if (allCampaignSteps == null) {
			logger.error("error in getting campaign step for {}", campaignData.getUuid());
			throw new DeviceVersionException("error in getting campaign step ");
		}
		try {
			List<Campaign> activeCampaignsForImei = campaignUtils.checkCampaignForDeviceId(deviceId, "");

			// just check with base line Eli or NE
			if (msDeviceReports == null || msDeviceReports.size() == 0) {
				logger.info("Gateway has not reported recently ", campaignData.getUuid());
				campaignDeviceDetail.setDeviceStatusForCampaign(DeviceStatusForCampaign.UNKNOWN.getValue());
				campaignDeviceDetail.setComments(Constants.NOT_REPORTED_RECENTLY);
			} else {
				Map<String, List<DeviceReport>> deviceMaintReportMap = msDeviceReports.stream()
						.collect(Collectors.groupingBy(w -> w.getDEVICE_ID()));

				if (activeCampaignsForImei.size() == 1) {
					if (lastStepStatusInThisCamp != null
							&& lastStepStatusInThisCamp.equalsIgnoreCase(CampaignStepDeviceStatus.SUCCESS.getValue())) {
						campaignDeviceDetail.setDeviceStatusForCampaign(DeviceStatusForCampaign.SUCCESS.getValue());
					} else {
						if (isEligible.equalsIgnoreCase(DeviceStatusForCampaign.ELIGIBLE.getValue())
								|| !StringUtils.isEmpty(firstStepStatusInThisCamp)
										&& (lastStepStatusInThisCamp == null || !lastStepStatusInThisCamp
												.equalsIgnoreCase(CampaignStepDeviceStatus.SUCCESS.getValue()))) {

							campaignDeviceDetail
									.setDeviceStatusForCampaign(DeviceStatusForCampaign.ELIGIBLE.getValue());
							List<String> failedStepUuid = deviceDetailRepository
									.getFailedExecutedStep(campaignData.getUuid(), deviceId);
							checkFailedStepsForDevice(campaignData, deviceId, campaignDeviceDetail);
						} else {
							campaignDeviceDetail
									.setDeviceStatusForCampaign(DeviceStatusForCampaign.NOT_ELIGIBLE.getValue());
							if (null != deviceReport && null != deviceReport.getIsDeviceInstalledForCampaign()
									&& deviceReport.getIsDeviceInstalledForCampaign().equalsIgnoreCase("true"))
								campaignDeviceDetail.setComments(Constants.NOT_ELIGIBLE_FOR_BASELINE);
							else {
								campaignDeviceDetail.setComments(Constants.GATEWAY_NOT_INSTALLED);
							}
						}
					}
				} else {
					if (lastStepStatusInThisCamp != null
							&& lastStepStatusInThisCamp.equalsIgnoreCase(CampaignStepDeviceStatus.SUCCESS.getValue())) {
						campaignDeviceDetail.setDeviceStatusForCampaign(DeviceStatusForCampaign.COMPLETED.getValue());
					} else {
						campaignDeviceDetail.setDeviceStatusForCampaign(DeviceStatusForCampaign.ELIGIBLE.getValue());

						if (isEligible.equalsIgnoreCase(DeviceStatusForCampaign.ELIGIBLE.getValue())
								|| (!StringUtils.isEmpty(firstStepStatusInThisCamp)
										&& (lastStepStatusInThisCamp == null || !lastStepStatusInThisCamp
												.equalsIgnoreCase(CampaignStepDeviceStatus.SUCCESS.getValue())))) {

							// change the logic to fetch the current campaign from Database
							/*
							 * for (Campaign campaign : activeCampaignsForImei) { if
							 * (!campaign.getUuid().equals(campaignData.getUuid())) { link.add(new
							 * CampaignHyperLink(campaign.getCampaignName(), campaign.getUuid())); multiCamp
							 * += campaign.getCampaignName(); if
							 * (!StringUtils.isEmpty(firstStepStatusInThisCamp)) { startedInAnyOtherCamp =
							 * true; }
							 * 
							 * } }
							 */
							List<DeviceStatus> elegibleCampaignStepDeviceDetail = deviceStatusRepository
									.getByDeviceAndStatus(deviceId, "PENDING");
							if (elegibleCampaignStepDeviceDetail.size() > 0) {
								if (!(elegibleCampaignStepDeviceDetail.get(0).getCampaign().getUuid()
										.equals(campaignData.getUuid()))) {
									startedInAnyOtherCamp = true;
								}
							}
							if (!startedInAnyOtherCamp) {
								campaignDeviceDetail
										.setDeviceStatusForCampaign(DeviceStatusForCampaign.ELIGIBLE.getValue());
								campaignDeviceDetail.setComments(Constants.CONFLICT_ELIGIBLE + " " + multiCamp);
								campaignDeviceDetail.setCampaignHyperLink(link);
							}

							List<String> failedStepUuid = deviceDetailRepository
									.getFailedExecutedStep(campaignData.getUuid(), deviceId);
							checkFailedStepsForDevice(campaignData, deviceId, campaignDeviceDetail);
						}

						// if device is not eligible for campaign
						else {
							campaignDeviceDetail
									.setDeviceStatusForCampaign(DeviceStatusForCampaign.NOT_ELIGIBLE.getValue());
							if (null != deviceReport && null != deviceReport.getIsDeviceInstalledForCampaign()
									&& deviceReport.getIsDeviceInstalledForCampaign().equalsIgnoreCase("true"))
								campaignDeviceDetail.setComments(Constants.NOT_ELIGIBLE_FOR_BASELINE);
							else {
								campaignDeviceDetail.setComments(Constants.GATEWAY_NOT_INSTALLED);
							}

							for (Campaign campaign : activeCampaignsForImei) {
								if (!campaign.getUuid().equals(campaignData.getUuid())) {
									/*
									 * String firstStepStatus = deviceDetailRepository
									 * .findStatusByCampaignUuidAndStepOrderNumber(campaign.getUuid(), deviceId,
									 * 1l); Long maxStep =
									 * campaignStepRepository.findLastStepByCampaignUuid(campaign.getUuid())
									 * .getStepOrderNumber(); String lastStepStatus = deviceDetailRepository
									 * .findStatusByCampaignUuidAndStepOrderNumber(campaign.getUuid(), deviceId,
									 * maxStep);
									 */
									// List<DeviceStatus> elegibleCampaignStepDeviceDetail = deviceStatusRepository
									// .getByDeviceAndStatus(deviceId, "PENDING");

									/*
									 * if (!StringUtils.isEmpty(firstStepStatus) && (lastStepStatus == null ||
									 * !lastStepStatus
									 * .equalsIgnoreCase(CampaignStepDeviceStatus.SUCCESS.getValue()))) {
									 */
									/*
									 * if (elegibleCampaignStepDeviceDetail.size() > 0) {
									 * campaignDeviceDetail.setComments( Constants.CONFLICT_NOT_ELIGIBLE + " " +
									 * campaign.getCampaignName()); link.add(new
									 * CampaignHyperLink(campaign.getCampaignName(), campaign.getUuid()));
									 * campaignDeviceDetail.setCampaignHyperLink(link); break; }
									 */

								}
							}

						}
					}
				}

			}
		} catch (Exception e) {
			logger.error("error in calculating eligiblity for gateway {}", deviceId);
			e.printStackTrace();
		}
	}

	void gatewayEligibilityCalculation(String deviceId, DeviceCampaignStatus deviceCampaignStatus,
			Campaign campaignData, List<CampaignStep> allCampaignSteps, List<DeviceReport> msDeviceReports,
			String isEligible, String firstStepStatusInThisCamp, String lastStepStatusInThisCamp,
			DeviceReport deviceReport) {

		List<CampaignHyperLink> link = new ArrayList<CampaignHyperLink>();
		boolean startedInAnyOtherCamp = false;
		String multiCamp = "";
		if (allCampaignSteps == null) {
			logger.error("error in getting campaign step for {}", campaignData.getUuid());
			throw new DeviceVersionException("error in getting campaign step ");
		}
		try {
			List<Campaign> activeCampaignsForImei = campaignUtils.checkCampaignForDeviceId(deviceId, "");

			// just check with base line Eli or NE
			if (msDeviceReports == null || msDeviceReports.size() == 0) {
				logger.info("Gateway has not reported recently ", campaignData.getUuid());
				deviceCampaignStatus.setRunningStatus(DeviceStatusForCampaign.UNKNOWN.getValue());
				deviceCampaignStatus.setComment(Constants.NOT_REPORTED_RECENTLY);
				// campaignDeviceDetail.setDeviceStatusForCampaign(DeviceStatusForCampaign.UNKNOWN.getValue());
				// campaignDeviceDetail.setComments(Constants.NOT_REPORTED_RECENTLY);
			} else {
				Map<String, List<DeviceReport>> deviceMaintReportMap = msDeviceReports.stream()
						.collect(Collectors.groupingBy(w -> w.getDEVICE_ID()));

				if (activeCampaignsForImei.size() == 1) {
					if (lastStepStatusInThisCamp != null
							&& lastStepStatusInThisCamp.equalsIgnoreCase(CampaignStepDeviceStatus.SUCCESS.getValue())) {
						// campaignDeviceDetail.setDeviceStatusForCampaign(DeviceStatusForCampaign.COMPLETED.getValue());
						deviceCampaignStatus.setRunningStatus(DeviceStatusForCampaign.SUCCESS.getValue());
						deviceCampaignStatus.setComment(DeviceStatusForCampaign.COMPLETED.getValue());

					} else {
						if (isEligible.equalsIgnoreCase(DeviceStatusForCampaign.ELIGIBLE.getValue())
								|| !StringUtils.isEmpty(firstStepStatusInThisCamp)
										&& (lastStepStatusInThisCamp == null || !lastStepStatusInThisCamp
												.equalsIgnoreCase(CampaignStepDeviceStatus.SUCCESS.getValue()))) {

							// campaignDeviceDetail
							// .setDeviceStatusForCampaign(DeviceStatusForCampaign.ELIGIBLE.getValue());
							deviceCampaignStatus.setRunningStatus(DeviceStatusForCampaign.PENDNG.getValue());
							List<String> failedStepUuid = deviceDetailRepository
									.getFailedExecutedStep(campaignData.getUuid(), deviceId);
							// checkFailedStepsForDevice(campaignData, deviceId, campaignDeviceDetail);
						} else {
							// campaignDeviceDetail
							// .setDeviceStatusForCampaign(DeviceStatusForCampaign.NOT_ELIGIBLE.getValue());
							deviceCampaignStatus.setRunningStatus(DeviceStatusForCampaign.NOT_ELIGIBLE.getValue());

							if (null != deviceReport && null != deviceReport.getIsDeviceInstalledForCampaign()
									&& deviceReport.getIsDeviceInstalledForCampaign().equalsIgnoreCase("true"))
								deviceCampaignStatus.setComment(Constants.NOT_ELIGIBLE_FOR_BASELINE);
							// campaignDeviceDetail.setComments(Constants.NOT_ELIGIBLE_FOR_BASELINE);
							else {
								deviceCampaignStatus.setComment(Constants.GATEWAY_NOT_INSTALLED);
								// campaignDeviceDetail.setComments(Constants.GATEWAY_NOT_INSTALLED);
							}
						}
					}
				} else {
					if (lastStepStatusInThisCamp != null
							&& lastStepStatusInThisCamp.equalsIgnoreCase(CampaignStepDeviceStatus.SUCCESS.getValue())) {
						deviceCampaignStatus.setRunningStatus(DeviceStatusForCampaign.COMPLETED.getValue());
						// campaignDeviceDetail.setDeviceStatusForCampaign(DeviceStatusForCampaign.COMPLETED.getValue());
					} else {
						// campaignDeviceDetail.setDeviceStatusForCampaign(DeviceStatusForCampaign.ELIGIBLE.getValue());
						deviceCampaignStatus.setEligibility(DeviceStatusForCampaign.ELIGIBLE.getValue());
						if (isEligible.equalsIgnoreCase(DeviceStatusForCampaign.ELIGIBLE.getValue())
								|| (!StringUtils.isEmpty(firstStepStatusInThisCamp)
										&& (lastStepStatusInThisCamp == null || !lastStepStatusInThisCamp
												.equalsIgnoreCase(CampaignStepDeviceStatus.SUCCESS.getValue())))) {

							// change the logic to fetch the current campaign from Database
							/*
							 * for (Campaign campaign : activeCampaignsForImei) { if
							 * (!campaign.getUuid().equals(campaignData.getUuid())) { link.add(new
							 * CampaignHyperLink(campaign.getCampaignName(), campaign.getUuid())); multiCamp
							 * += campaign.getCampaignName(); if
							 * (!StringUtils.isEmpty(firstStepStatusInThisCamp)) { startedInAnyOtherCamp =
							 * true; }
							 * 
							 * } }
							 */
							List<DeviceStatus> elegibleCampaignStepDeviceDetail = deviceStatusRepository
									.getByDeviceAndStatus(deviceId, "PENDING");
							if (elegibleCampaignStepDeviceDetail.size() > 0) {
								if (!(elegibleCampaignStepDeviceDetail.get(0).getCampaign().getUuid()
										.equals(campaignData.getUuid()))) {
									startedInAnyOtherCamp = true;
								}
							}
							if (!startedInAnyOtherCamp) {
								// campaignDeviceDetail
								// .setDeviceStatusForCampaign(DeviceStatusForCampaign.ELIGIBLE.getValue());
								// campaignDeviceDetail.setComments(Constants.CONFLICT_ELIGIBLE + " " +
								// multiCamp);
								deviceCampaignStatus.setEligibility(DeviceStatusForCampaign.ELIGIBLE.getValue());
								deviceCampaignStatus.setComment(Constants.CONFLICT_ELIGIBLE + " " + multiCamp);

								// campaignDeviceDetail.setCampaignHyperLink(link);
							}

							// List<String> failedStepUuid = deviceDetailRepository
							// .getFailedExecutedStep(campaignData.getUuid(), deviceId);
							// checkFailedStepsForDevice(campaignData, deviceId, campaignDeviceDetail);
						}

						// if device is not eligible for campaign
						else {
							deviceCampaignStatus.setEligibility(DeviceStatusForCampaign.NOT_ELIGIBLE.getValue());
							// campaignDeviceDetail
							// .setDeviceStatusForCampaign(DeviceStatusForCampaign.NOT_ELIGIBLE.getValue());
							if (null != deviceReport && null != deviceReport.getIsDeviceInstalledForCampaign()
									&& deviceReport.getIsDeviceInstalledForCampaign().equalsIgnoreCase("true"))
								deviceCampaignStatus.setComment(Constants.NOT_ELIGIBLE_FOR_BASELINE);
							else {
								deviceCampaignStatus.setComment(Constants.GATEWAY_NOT_INSTALLED);
							}

							for (Campaign campaign : activeCampaignsForImei) {
								if (!campaign.getUuid().equals(campaignData.getUuid())) {
									/*
									 * String firstStepStatus = deviceDetailRepository
									 * .findStatusByCampaignUuidAndStepOrderNumber(campaign.getUuid(), deviceId,
									 * 1l); Long maxStep =
									 * campaignStepRepository.findLastStepByCampaignUuid(campaign.getUuid())
									 * .getStepOrderNumber(); String lastStepStatus = deviceDetailRepository
									 * .findStatusByCampaignUuidAndStepOrderNumber(campaign.getUuid(), deviceId,
									 * maxStep);
									 */
									List<DeviceStatus> elegibleCampaignStepDeviceDetail = deviceStatusRepository
											.getByDeviceAndStatus(deviceId, "PENDING");

									/*
									 * if (!StringUtils.isEmpty(firstStepStatus) && (lastStepStatus == null ||
									 * !lastStepStatus
									 * .equalsIgnoreCase(CampaignStepDeviceStatus.SUCCESS.getValue()))) {
									 */
									if (elegibleCampaignStepDeviceDetail.size() > 0) {
										deviceCampaignStatus.setComment(
												Constants.CONFLICT_NOT_ELIGIBLE + " " + campaign.getCampaignName());
										// campaignDeviceDetail.setComments(
										// Constants.CONFLICT_NOT_ELIGIBLE + " " + campaign.getCampaignName());
										link.add(new CampaignHyperLink(campaign.getCampaignName(), campaign.getUuid()));
										// campaignDeviceDetail.setCampaignHyperLink(link);
										break;
									}

								}
							}

						}
					}
				}

			}
		} catch (Exception e) {
			logger.error("error in calculating eligiblity for gateway {}", deviceId);
			e.printStackTrace();
		}
	}

	private Map<String, List<StepStatusForDeviceView>> getDeviceDetailsMap(List<String> allImeis, Campaign campaignData,
			long stepNumber, String msgUuid) {
		List<StepStatusForDeviceView> stepStatusInThisCampForAllDevices = new ArrayList<StepStatusForDeviceView>();

		logger.info(msgUuid + "  -- 643");
		if (allImeis != null && !allImeis.isEmpty()) {
			stepStatusInThisCampForAllDevices = deviceDetailRepository
					.findAllStatusByCampaignUuidAndStepOrderNumber(campaignData.getUuid(), allImeis, stepNumber);

		}
		logger.info(msgUuid + "  -- 651");
		Map<String, List<StepStatusForDeviceView>> stepStatusInThisCampMap = stepStatusInThisCampForAllDevices.stream()
				.collect(Collectors.groupingBy(w -> w.getDevice_id()));

		return stepStatusInThisCampMap;
	}

	private Map<String, List<CampaignStepDeviceDetail>> getDeviceDetailsMapLastStatus(List<String> allImeis,
			Campaign campaignData, String msgUuid) {
		List<CampaignStepDeviceDetail> stepStatusInThisCampForAllDevices = new ArrayList<CampaignStepDeviceDetail>();
		stepStatusInThisCampForAllDevices = deviceDetailRepository
				.getDeviceCampaignLastExecuteStep(campaignData.getUuid());
		logger.info(msgUuid + "  -- 651");
		Map<String, List<CampaignStepDeviceDetail>> stepStatusInThisCampMap = stepStatusInThisCampForAllDevices.stream()
				.collect(Collectors.groupingBy(w -> w.getDeviceId()));

		return stepStatusInThisCampMap;
	}

	private Map<String, List<CampaignConfigProblem>> getCampaignConfigProblem(Campaign campaignData, String msgUuid) {

		List<CampaignConfigProblem> campaignConfigProblemList = configProblemRepository
				.findByCampaignId(campaignData.getCampaignId());
		// List<CampaignStepDeviceDetail> stepStatusInThisCampForAllDevices = new
		// ArrayList<CampaignStepDeviceDetail>();

		logger.info(msgUuid + "  -- 651");
		Map<String, List<CampaignConfigProblem>> stepStatusInThisCampMap = campaignConfigProblemList.stream()
				.collect(Collectors.groupingBy(w -> w.getImei()));

		return stepStatusInThisCampMap;
	}

	public List<String> processDevicesForAddedCampaignDevices(Campaign campaignData, List<CampaignStep> allCampaignSteps,
			List<MsDeviceRestResponse> allDevices, Package baseLine,
			Map<String, List<DeviceReport>> deviceMaintReportMap, String msgUuid) {
		List<DeviceCampaignStatus> deviceCampaignStatusList = new ArrayList<DeviceCampaignStatus>();
		int deviceCounter = 0;
		String excludedImei = campaignData.getGroup().getExcludedImei();
		if(excludedImei == null)
		{
			excludedImei = "AAA";	
			
		}
		for (MsDeviceRestResponse device : allDevices) {
			deviceCounter++;
			logger.info(msgUuid + " Processing imei for campaing######## " + deviceCounter);
			LocalDateTime ldt = LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC);
			Instant now = ldt.toInstant(ZoneOffset.UTC);
		
			DeviceCampaignStatus deviceCampaignStatus = new DeviceCampaignStatus();
			deviceCampaignStatus.setCreatedAt(now);
			deviceCampaignStatus.setStatus(CampaignStepDeviceStatus.NOTSTARTED);
			deviceCampaignStatus.setDeviceId(device.getImei());
			deviceCampaignStatus.setCampaign(campaignData);
			deviceCampaignStatus.setCustomerName(device.getOrganisationName());
			deviceCampaignStatus.setCustomerId(device.getOrganisationId() + "");

			deviceCampaignStatus.setRunningStatus(DeviceStatusForCampaign.ELIGIBLE.getValue());
			String isEligible = campaignUtils.checkDeviceMatchesBaseline(device.getImei(), deviceMaintReportMap,
					baseLine, msgUuid);
			
			if (deviceMaintReportMap != null && deviceMaintReportMap.get(device.getImei()) != null) {
				DeviceReport latestMaint = deviceMaintReportMap.get(device.getImei()).get(0);
				deviceCampaignStatus.setLastReportedAt(latestMaint.getUpdated_date());
			}

			if (isEligible == null || !(isEligible.equals(DeviceStatusForCampaign.ELIGIBLE.getValue()))) {
				isEligible = DeviceStatusForCampaign.NOT_ELIGIBLE.getValue();
				deviceCampaignStatus.setRunningStatus(DeviceStatusForCampaign.NOT_ELIGIBLE.getValue());

			}
			deviceCampaignStatus.setEligibility(isEligible);
			//CampaignInstalledDevice msDevices = iCampaignInstalledDeviceMsRepository
			//		.findCampaignInstalledDeviceByDeviceImei(device.getImei());

			// CampaignInstalledDevice msDevices =
			// restUtils.CampaignInstalledDeviceFromMS(device.getImei());
			if (device.getInstalledStatusFlag() != null) {
				// campaignDeviceDetail.setInstalledFlag(msDevices.getInstalled_Flag());
				deviceCampaignStatus.setInstalledFlag(device.getInstalledStatusFlag());
			}

			if (!isEligible.equalsIgnoreCase(DeviceStatusForCampaign.ELIGIBLE.getValue())) {

				List<Campaign> campaignList = campaignUtils.checkCampaignForDeviceId(device.getImei(), null);

				if (campaignList != null && campaignList.size() <= 1) {

					deviceCampaignStatus.setOffOnPath(Constants.OFF_PATH);

				} else {
					deviceCampaignStatus.setOffOnPath(Constants.ON_PATH);

				}
			}

			try {
				List<CampaignConfigProblem> ccp = new ArrayList<CampaignConfigProblem>();
				// ccp=
				// configProblemRepository.findByDeviceIdAndCampaignName(device.getImei(),campaignData.getCampaignName());
				ccp = configProblemRepository.findByDeviceIdAndCampaignId(device.getImei(),
						campaignData.getCampaignId());
				if (ccp != null && !ccp.isEmpty() && ccp.size() > 0) {
					// campaignDeviceDetail.setDeviceStatusForCampaign(ccp.get(0).getDeviceStatusForCampaign());
					// campaignDeviceDetail.setComments(ccp.get(0).getComments());
					deviceCampaignStatus.setRunningStatus(ccp.get(0).getDeviceStatusForCampaign());
					deviceCampaignStatus.setComment(ccp.get(0).getComments());
				}

			} catch (Exception e) {
				logger.info("Error in setting values for device with id: " + device.getImei() + " and campaign:  "
						+ campaignData.getCampaignName());
			}
			if (deviceCampaignStatus.getProblem() == null) {
				deviceCampaignStatus.setProblem(Constants.NOPPROBLEM);
			}
			if (deviceCampaignStatus.getOnHold() == null) {
				deviceCampaignStatus.setOnHold(Constants.OFFHOLD);
			}
			if (deviceCampaignStatus.getOffOnPath() == null) {
				deviceCampaignStatus.setOffOnPath(Constants.ON_PATH);
			}
			if (deviceCampaignStatus.getLastCompletedStepUUID() == null) {
				deviceCampaignStatus.setLastCompletedStepUUID(DeviceStatusForCampaign.NOTSTARTED.getValue());
			}
			if(campaignData.getGroup().getRemovedImei() != null )
			{
				if(campaignData.getGroup().getRemovedImei().contains(device.getImei()))
				{
					deviceCampaignStatus.setRunningStatus(DeviceStatusForCampaign.REMOVED.getValue());
				}
			}
			
			if(campaignData.getGroup().getExcludedImei() != null )
			{
				if(campaignData.getGroup().getExcludedImei().contains(device.getImei()))
				{
					deviceCampaignStatus.setRunningStatus(DeviceStatusForCampaign.EXCLUDED.getValue());
				}
			}
			deviceCampaignStatusList.add(deviceCampaignStatus);
		}

		deviceCampaignStatusRepository.saveAll(deviceCampaignStatusList);

		String abc = "Completed";
		List<String> list = new ArrayList<>();
		list.add(abc);
		logger.info(msgUuid + " Processing imei for campaing######## " + deviceCounter);
		return list;
	}
	
	
	
	public List<String> processDevicesForUpdateCampaign(Campaign campaignData, List<CampaignStep> allCampaignSteps,
			List<MsDeviceRestResponse> allDevices, Package baseLine,
			Map<String, List<DeviceReport>> deviceMaintReportMap, String msgUuid) {
		List<DeviceCampaignStatus> deviceCampaignStatusList = new ArrayList<DeviceCampaignStatus>();
		int deviceCounter = 0;
		String excludedImei = campaignData.getGroup().getExcludedImei();
		if(excludedImei == null)
		{
			excludedImei = "AAA";	
			
		}
		for (MsDeviceRestResponse device : allDevices) {
			deviceCounter++;
			logger.info(msgUuid + " Processing imei for campaing######## " + deviceCounter);
			LocalDateTime ldt = LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC);
			Instant now = ldt.toInstant(ZoneOffset.UTC);
		
			List<DeviceCampaignStatus> deviceCampaignStatusList1 = deviceCampaignStatusRepository
					.findByDeviceAndCampaignUUID(device.getImei(), campaignData.getUuid());
			DeviceCampaignStatus deviceCampaignStatus = null;
			if (deviceCampaignStatusList1.size() > 0) {
				deviceCampaignStatus = deviceCampaignStatusList1.get(0);
				if(campaignData.getGroup().getRemovedImei() != null )
				{
					if(campaignData.getGroup().getRemovedImei().contains(device.getImei()))
					{
						deviceCampaignStatus.setRunningStatus(DeviceStatusForCampaign.REMOVED.getValue());
					}
				}
				
				if(campaignData.getGroup().getExcludedImei() != null )
				{
					if(campaignData.getGroup().getExcludedImei().contains(device.getImei()))
					{
						deviceCampaignStatus.setRunningStatus(DeviceStatusForCampaign.EXCLUDED.getValue());
					}
				}
				deviceCampaignStatusList.add(deviceCampaignStatus);
				continue;
			} else {

				deviceCampaignStatus = new DeviceCampaignStatus();
			}
		
			deviceCampaignStatus.setCreatedAt(now);
			deviceCampaignStatus.setStatus(CampaignStepDeviceStatus.NOTSTARTED);
			deviceCampaignStatus.setDeviceId(device.getImei());
			deviceCampaignStatus.setCampaign(campaignData);
			deviceCampaignStatus.setCustomerName(device.getOrganisationName());
			deviceCampaignStatus.setCustomerId(device.getOrganisationId() + "");

			deviceCampaignStatus.setRunningStatus(DeviceStatusForCampaign.ELIGIBLE.getValue());
			String isEligible = campaignUtils.checkDeviceMatchesBaseline(device.getImei(), deviceMaintReportMap,
					baseLine, msgUuid);
			
			if (deviceMaintReportMap != null && deviceMaintReportMap.get(device.getImei()) != null) {
				DeviceReport latestMaint = deviceMaintReportMap.get(device.getImei()).get(0);
				deviceCampaignStatus.setLastReportedAt(latestMaint.getUpdated_date());
			}

			if (isEligible == null || !(isEligible.equals(DeviceStatusForCampaign.ELIGIBLE.getValue()))) {
				isEligible = DeviceStatusForCampaign.NOT_ELIGIBLE.getValue();
				deviceCampaignStatus.setRunningStatus(DeviceStatusForCampaign.NOT_ELIGIBLE.getValue());

			}
			deviceCampaignStatus.setEligibility(isEligible);
			//CampaignInstalledDevice msDevices = iCampaignInstalledDeviceMsRepository
			//		.findCampaignInstalledDeviceByDeviceImei(device.getImei());

			// CampaignInstalledDevice msDevices =
			// restUtils.CampaignInstalledDeviceFromMS(device.getImei());
			if (device.getInstalledStatusFlag() != null) {
				// campaignDeviceDetail.setInstalledFlag(msDevices.getInstalled_Flag());
				deviceCampaignStatus.setInstalledFlag(device.getInstalledStatusFlag());
			}

			if (!isEligible.equalsIgnoreCase(DeviceStatusForCampaign.ELIGIBLE.getValue())) {

				List<Campaign> campaignList = campaignUtils.checkCampaignForDeviceId(device.getImei(), null);

				if (campaignList != null && campaignList.size() <= 1) {

					deviceCampaignStatus.setOffOnPath(Constants.OFF_PATH);

				} else {
					deviceCampaignStatus.setOffOnPath(Constants.ON_PATH);

				}
			}

			try {
				List<CampaignConfigProblem> ccp = new ArrayList<CampaignConfigProblem>();
				// ccp=
				// configProblemRepository.findByDeviceIdAndCampaignName(device.getImei(),campaignData.getCampaignName());
				ccp = configProblemRepository.findByDeviceIdAndCampaignId(device.getImei(),
						campaignData.getCampaignId());
				if (ccp != null && !ccp.isEmpty() && ccp.size() > 0) {
					// campaignDeviceDetail.setDeviceStatusForCampaign(ccp.get(0).getDeviceStatusForCampaign());
					// campaignDeviceDetail.setComments(ccp.get(0).getComments());
					deviceCampaignStatus.setRunningStatus(ccp.get(0).getDeviceStatusForCampaign());
					deviceCampaignStatus.setComment(ccp.get(0).getComments());
				}

			} catch (Exception e) {
				logger.info("Error in setting values for device with id: " + device.getImei() + " and campaign:  "
						+ campaignData.getCampaignName());
			}
			if (deviceCampaignStatus.getProblem() == null) {
				deviceCampaignStatus.setProblem(Constants.NOPPROBLEM);
			}
			if (deviceCampaignStatus.getOnHold() == null) {
				deviceCampaignStatus.setOnHold(Constants.OFFHOLD);
			}
			if (deviceCampaignStatus.getOffOnPath() == null) {
				deviceCampaignStatus.setOffOnPath(Constants.ON_PATH);
			}
			if (deviceCampaignStatus.getLastCompletedStepUUID() == null) {
				deviceCampaignStatus.setLastCompletedStepUUID(DeviceStatusForCampaign.NOTSTARTED.getValue());
			}
			if(campaignData.getGroup().getRemovedImei() != null )
			{
				if(campaignData.getGroup().getRemovedImei().contains(device.getImei()))
				{
					deviceCampaignStatus.setRunningStatus(DeviceStatusForCampaign.REMOVED.getValue());
				}
			}
			
			if(campaignData.getGroup().getExcludedImei() != null )
			{
				if(campaignData.getGroup().getExcludedImei().contains(device.getImei()))
				{
					deviceCampaignStatus.setRunningStatus(DeviceStatusForCampaign.EXCLUDED.getValue());
				}
			}
			deviceCampaignStatusList.add(deviceCampaignStatus);
		}

		deviceCampaignStatusRepository.saveAll(deviceCampaignStatusList);

		String abc = "Completed";
		List<String> list = new ArrayList<>();
		list.add(abc);
		logger.info(msgUuid + " Processing imei for campaing######## " + deviceCounter);
		return list;
	}

	@Async("asyncTaskExecutor")
	public void processDevicesForUpdate(Campaign campaignData, List<CampaignStep> allCampaignSteps,
			String msgUuid , List<String> newImeis) {
		logger.info(msgUuid + " Processing imei for campaing" + campaignData.getCampaignName() + campaignData.getUuid());
		String allImei = null;
		List<MsDeviceRestResponse> allDevices = fetchIMIEForCampaign(campaignData, msgUuid, allImei);
		//List<MsDeviceRestResponse> allDevices =deviceVersionRepository.findAllDeviceByIMEIList(newImeis);
		logger.info(msgUuid + " Fetched device size " + allDevices.size());
		List<DeviceCampaignStatus> deviceCampaignStatusList = new ArrayList<DeviceCampaignStatus>();
		// CampaignStatsPayload campaignStatsResponse = new CampaignStatsPayload();
		List<String> allImeis = allDevices.stream().map(MsDeviceRestResponse::getImei).collect(Collectors.toList());
	//	List<DeviceReport> msDeviceReports = campaignDeviceHelper.getDeviceReports(allImeis);
		Map<String, List<DeviceReport>> deviceMaintReportMap = campaignDeviceHelper.getDeviceReports1(allImeis);
		logger.info(msgUuid + " deviceMaintReportMap " + deviceMaintReportMap.size());
		String basePackageUuid = allCampaignSteps.get(0).getFromPackage().getUuid();
		Package baseLine = packageRepository.findByUuid(basePackageUuid);
		List<List<MsDeviceRestResponse>> items = new ArrayList<List<MsDeviceRestResponse>>();

		int workerCount = 0;

		int fromIndex = 0;
		int toIndex = 0;

		while (fromIndex < allDevices.size()) {

			if ((allDevices.size() - fromIndex) > 1000) {
				toIndex = fromIndex + 1000;

			} else {
				toIndex = allDevices.size();
			}

			items.add(allDevices.subList(fromIndex, toIndex));
			fromIndex = toIndex;
		}
		List<CompletableFuture<List<String>>> futures = items.stream()
				.map(item -> CompletableFuture.supplyAsync(() -> processDevicesForUpdateCampaign(campaignData,
						allCampaignSteps, item, baseLine, deviceMaintReportMap, msgUuid)))
				.collect(Collectors.toList());

		// Wait for ALL CompletableFutures to finish then return
		List<String> s = futures.stream().map(CompletableFuture::join).flatMap(List::stream)
				.collect(Collectors.toList());
		logger.info(msgUuid + " Completed processDevicesForUpdate " + s);
		
		campaignData.setInitStatus("Complete");
		Campaign savedCamp = campaignRepo.save(campaignData);
		logger.info(msgUuid + " Finished " + s);
		
	
	}
	
	
	@Async("asyncTaskExecutor")
	public void processDevicesForAddedCampaign(Campaign campaignData, List<CampaignStep> allCampaignSteps,
			String msgUuidIn) {
		String msgUuid = msgUuidIn + msgUuidIn + " processDevicesForAddedCampaign   "+ campaignData.getUuid();
		logger.info(msgUuid);
		String allImei = null;
		List<MsDeviceRestResponse> allDevices = fetchIMIEForCampaign(campaignData, msgUuid, allImei);
		logger.info(msgUuid + " Fetched device size " + allDevices.size());
		List<DeviceCampaignStatus> deviceCampaignStatusList = new ArrayList<DeviceCampaignStatus>();
		// CampaignStatsPayload campaignStatsResponse = new CampaignStatsPayload();
		List<String> allImeis = allDevices.stream().map(MsDeviceRestResponse::getImei).collect(Collectors.toList());
	//	List<DeviceReport> msDeviceReports = campaignDeviceHelper.getDeviceReports(allImeis);
		Map<String, List<DeviceReport>> deviceMaintReportMap = campaignDeviceHelper.getDeviceReports1(allImeis);
		logger.info(msgUuid + " deviceMaintReportMap " + deviceMaintReportMap.size());
		String basePackageUuid = allCampaignSteps.get(0).getFromPackage().getUuid();
		Package baseLine = packageRepository.findByUuid(basePackageUuid);
		List<List<MsDeviceRestResponse>> items = new ArrayList<List<MsDeviceRestResponse>>();

		int workerCount = 0;

		int fromIndex = 0;
		int toIndex = 0;

		while (fromIndex < allDevices.size()) {

			if ((allDevices.size() - fromIndex) > 1000) {
				toIndex = fromIndex + 1000;

			} else {
				toIndex = allDevices.size();
			}

			items.add(allDevices.subList(fromIndex, toIndex));
			fromIndex = toIndex;
		}
		List<CompletableFuture<List<String>>> futures = items.stream()
				.map(item -> CompletableFuture.supplyAsync(() -> processDevicesForAddedCampaignDevices(campaignData,
						allCampaignSteps, item, baseLine, deviceMaintReportMap, msgUuid)))
				.collect(Collectors.toList());

		// Wait for ALL CompletableFutures to finish then return
		List<String> concatedString = futures.stream().map(CompletableFuture::join).flatMap(List::stream)
				.collect(Collectors.toList());
		logger.info(msgUuid + " Completed all " + concatedString);
		
		campaignData.setInitStatus("Complete");
		Campaign savedCamp = campaignRepo.save(campaignData);
		logger.info(msgUuid + " Finished " + concatedString);
		
	
	}

	

	public void processDevicesForUpdateCampaign(Campaign campaignData, List<CampaignStep> allCampaignSteps,
			String msgUuidIn) {
		String msgUuid = msgUuidIn + msgUuidIn + " processDevicesForUpdateCampaign   "+ campaignData.getUuid();
		logger.info(msgUuid + " processDevicesForUpdateCampaign");
		
		
		VersionMigrationDetailDTO versionMigrationDetailDTO = null;
		List<VersionMigrationDetailDTO> versionMigrationDetailList = new ArrayList<>();
		List<CampaignDeviceDetail> campaignDeviceDetailList = new ArrayList<>();
		
		Long maxStepInCamp = campaignStepRepository.findLastStepByCampaignUuid(campaignData.getUuid())
				.getStepOrderNumber();
				String allImei = null;
		DeviceStepStatus deviceStepStatus = null;
		Map<String, List<CampaignStepDeviceDetail>> imeiStepData = new HashMap<String, List<CampaignStepDeviceDetail>>();
		Map<String, List<MultipleCampaignDevice>> OnHoldForCampaignData = new HashMap<String, List<MultipleCampaignDevice>>();
		List<MsDeviceRestResponse> allDevices = fetchIMIEForCampaign(campaignData, msgUuid, allImei);

		logger.info(msgUuid + " allDevice.size" +allDevices.size());
		List<MultipleCampaignDevice> onHoldOfCampaign = multiCampaignDeviceRepository
				.findByCampaignUuid(campaignData.getUuid());
		OnHoldForCampaignData = onHoldOfCampaign.stream().collect(Collectors.groupingBy(w -> w.getDeviceId()));
		
		long notEligibleCount = 0;
		List<String> allImeis = allDevices.stream().map(MsDeviceRestResponse::getImei).collect(Collectors.toList());
		
	//	List<String> failedStepUuidMap  = deviceDetailRepository.getFailedExecutedStep(campaignData.getUuid(), allImeis);
		
		
		// List<DeviceReport> msDeviceReports =
		logger.info(msgUuid + "  -- 1633");
		
	//	List<DeviceReport> msDeviceReports = campaignDeviceHelper.getDeviceReports(allImeis);
		
	//	Map<String, String> msDeviceLastReportDate = campaignDeviceHelper.getDeviceLastReportDate(allImeis);
		
		String basePackageUuid = allCampaignSteps.get(0).getFromPackage().getUuid();
		Package baseLine = packageRepository.findByUuid(basePackageUuid);

		
		DeviceCampaignStatusDTO deviceCampaignStatusDTO = new DeviceCampaignStatusDTO();

		Map<String, List<StepStatusForDeviceView>> firstStepStatusInThisCampMap = getDeviceDetailsMap(allImeis,
				campaignData, 1l, msgUuid);
	
		Map<String, List<StepStatusForDeviceView>> lastStepStatusInThisCampMap = getDeviceDetailsMap(allImeis,
				campaignData, maxStepInCamp, msgUuid);

		Map<String, List<CampaignStepDeviceDetail>> currentStepStatusInThisCampMap = getDeviceDetailsMapLastStatus(
				allImeis, campaignData, msgUuid);

		Map<String, List<CampaignConfigProblem>> problemIMEIMap = getCampaignConfigProblem(campaignData, msgUuid);
		
		Map<String, List<DeviceReport>> deviceMaintReportMap = campaignDeviceHelper.getDeviceReports1(allImeis);
		logger.info(msgUuid + " allDevice.size" +allDevices.size());
/*		if (msDeviceReports != null && msDeviceReports.size() > 0) {
			for (DeviceReport dev : msDeviceReports) {
				msDeviceReportsMap.put(dev.getDEVICE_ID(), dev);
			}
		}*/
//////  NEW PERFORMSNCE CHANGES
		List<DeviceCampaignStatus> deviceCampaignStatusList = new ArrayList<DeviceCampaignStatus>();
		
		int count = 0;
		int iterator = 0;
		int workerCount = 0;

		int fromIndex = 0;
		int toIndex = 0;
		List<List<MsDeviceRestResponse>> items = new ArrayList<List<MsDeviceRestResponse>>();

		while (fromIndex < allDevices.size()) {

			if ((allDevices.size() - fromIndex) > 1000) {
				toIndex = fromIndex + 1000;

			} else {
				toIndex = allDevices.size();
			}

			items.add(allDevices.subList(fromIndex, toIndex));
			fromIndex = toIndex;
		}
		List<CompletableFuture<List<String>>> futures = items.stream()
				.map(item -> CompletableFuture.supplyAsync(() -> updateSomeDevices(campaignData,
						 item, msgUuid , firstStepStatusInThisCampMap,lastStepStatusInThisCampMap,currentStepStatusInThisCampMap, deviceMaintReportMap,baseLine,problemIMEIMap)))
				.collect(Collectors.toList());
		List<String> s = futures.stream().map(CompletableFuture::join).flatMap(List::stream)
				.collect(Collectors.toList());
		logger.info(msgUuid + " Completed 12 for  uuid " + campaignData.getUuid() + s);
	}
	
	public  List<String> updateSomeDevices(Campaign campaignData,List<MsDeviceRestResponse> allDevices,String msgUuid,	Map<String, List<StepStatusForDeviceView>> firstStepStatusInThisCampMap,Map<String, List<StepStatusForDeviceView>> lastStepStatusInThisCampMap,
			Map<String, List<CampaignStepDeviceDetail>> currentStepStatusInThisCampMap,Map<String, List<DeviceReport>> deviceMaintReportMap,
			Package baseLine ,Map<String, List<CampaignConfigProblem>> problemIMEIMap)
	{
		
		List<String> returnList =  new ArrayList<String>();
		int count =0;
		int iteration = 0;
	
		List<DeviceCampaignStatus> deviceCampaignStatusList = new ArrayList<DeviceCampaignStatus>();
		for (MsDeviceRestResponse device : allDevices) {
			logger.info(msgUuid + " Processing device ID " +device.getImei());
			LocalDateTime ldt = LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC);
			Instant now = ldt.toInstant(ZoneOffset.UTC);
		
			DeviceCampaignStatusDTO deviceCampaignStatusDTO = new DeviceCampaignStatusDTO();
			List<DeviceCampaignStatus> deviceCampaignStatusList1 = deviceCampaignStatusRepository
					.findByDeviceAndCampaignUUID(device.getImei(), campaignData.getUuid());
			DeviceCampaignStatus deviceCampaignStatus = null;
			List<String> allImeis = new ArrayList<String>();
			allImeis.add(device.getImei());
			if (deviceCampaignStatusList1.size() > 0) {
				deviceCampaignStatus = deviceCampaignStatusList1.get(0);
			} else {

				deviceCampaignStatus = new DeviceCampaignStatus();
			}
			String deviceStatusInCampaign = getDeviceStatusInCampaign(allImeis, campaignData, msgUuid);
			deviceCampaignStatus.setCreatedAt(now);

			deviceCampaignStatus.setDeviceId(device.getImei());
			deviceCampaignStatus.setCustomerId(device.getOrganisationId()+"");
			deviceCampaignStatus.setCustomerName(device.getOrganisationName());
			deviceCampaignStatus.setCampaign(campaignData);
			String firstStepStatusInThisCamp = null;
			String lastStepStatusInThisCamp = null;
			//Boolean installedFlag = campaignUtils.isInstalledFlagTrue(msgUuid, device.getImei());
			//deviceCampaignStatus.setInstalledFlag(installedFlag.toString());
			if(device.getInstalledStatusFlag() != null && device.getInstalledStatusFlag().equalsIgnoreCase("Y")) {
				deviceCampaignStatus.setInstalledFlag(true+"");
			}
			else
				deviceCampaignStatus.setInstalledFlag(false+"");
			
			List<StepStatusForDeviceView> firstStepStatusInThisCampList = firstStepStatusInThisCampMap
					.get(device.getImei());
			logger.info(msgUuid + " Processing device ID Count " +count);
		 
			
			String isEligible = DeviceStatusForCampaign.ELIGIBLE.getValue();
			if (firstStepStatusInThisCampList == null || !(firstStepStatusInThisCampList.size() > 0)) {
				isEligible = campaignUtils.checkDeviceMatchesBaseline(device.getImei(), deviceMaintReportMap, baseLine,
						msgUuid);

				if (isEligible == null || !(isEligible.length() > 0)) {
					isEligible = DeviceStatusForCampaign.NOT_ELIGIBLE.getValue();
					deviceCampaignStatus.setRunningStatus(isEligible);
				}

			}

			String status = getDeviceStatusInCampaign(firstStepStatusInThisCampMap, lastStepStatusInThisCampMap,
					currentStepStatusInThisCampMap, problemIMEIMap, device.getImei(), deviceCampaignStatusDTO,
					isEligible, msgUuid);

			if (isEligible.equals(DeviceStatusForCampaign.ELIGIBLE.getValue())) {
				deviceCampaignStatus.setRunningStatus(status);
			}
			deviceCampaignStatus.setProblem(deviceCampaignStatusDTO.getProblemStatus());
			deviceCampaignStatus.setProblemComment(deviceCampaignStatusDTO.getProblemComment());
			deviceCampaignStatus.setLastCompletedStepUUID(deviceCampaignStatusDTO.getLastSuccessStepUUID());

			deviceCampaignStatus.setEligibility(isEligible);
			deviceCampaignStatus.setLastStepOrderNumber(deviceCampaignStatusDTO.getLastStepOrderNumber());
			deviceCampaignStatus.setLastStepExecutionDate(deviceCampaignStatusDTO.getLastStepExecutionDate());

			checkFailedStepsForDeviceForUpdated(campaignData, device.getImei(), deviceCampaignStatus);
			
//			List<String> allImeis = new ArrayList<String>(Arrays.asList(device.getImei()));

			/*
			 * gatewayEligibilityCalculation(device.getImei(), deviceCampaignStatus,
			 * campaignData, allCampaignSteps, msDeviceReports, isEligible,
			 * firstStepStatusInThisCamp, lastStepStatusInThisCamp,
			 * msDeviceReportsMap.get(device.getImei()));
			 */
		
			if (!isEligible.equalsIgnoreCase(DeviceStatusForCampaign.ELIGIBLE.getValue())
					&& (firstStepStatusInThisCamp == null)) {
			
				//// NEEDED changes
			//	notEligibleCount++;
			/*	List<Campaign> campaignList = campaignUtils.checkCampaignForDeviceId(device.getImei(), null);
				logger.info(msgUuid + "  -- 1737");
				// logger.info(" campaignList campaignList "+campaignList);
				if (campaignList != null && campaignList.size() <= 1) {

					deviceCampaignStatus.setOffOnPath(Constants.OFF_PATH);

				} else {
					deviceCampaignStatus.setOffOnPath(Constants.ON_PATH);

				}
				*/
			}

			if (deviceCampaignStatus.getOnHold() == null) {
				deviceCampaignStatus.setOnHold(Constants.OFFHOLD);
			}
			// campaignDeviceDetail.setDeviceStepStatus(deviceStepStatusList);
			// campaignDeviceDetail.setDeviceReport(msDeviceReportsMap.get(device.getImei()));

			

			//CampaignInstalledDevice msDevices = null;
			/*
			 * iCampaignInstalledDeviceMsRepository
			 * .findCampaignInstalledDeviceByDeviceImei(device.getImei());
			 */
			// CampaignInstalledDevice msDevices =
			// restUtils.CampaignInstalledDeviceFromMS(device.getImei());
			if (device.getInstalledStatusFlag() != null) {
				// campaignDeviceDetail.setInstalledFlag(msDevices.getInstalled_Flag());
				deviceCampaignStatus.setInstalledFlag(device.getInstalledStatusFlag());
			}
			// campaignDeviceDetailList.add(campaignDeviceDetail);
			/*
			 * baseLineMatch += campaignUtils
			 * .validateDeviceUpgradeEligibleForCampaign(msDeviceReportsMap.get(device.
			 * getImei())) == true ? 1 : 0;
			 */
			if (deviceCampaignStatus.getProblem() == null) {
				deviceCampaignStatus.setProblem(Constants.NOPPROBLEM);
			}
			if (deviceCampaignStatus.getOffOnPath() == null) {
				deviceCampaignStatus.setOffOnPath(Constants.ON_PATH);
			}
			if (deviceCampaignStatus.getLastCompletedStepUUID() == null) {
				deviceCampaignStatus.setLastCompletedStepUUID(DeviceStatusForCampaign.NOTSTARTED.getValue());
			}
			if(campaignData.getGroup().getRemovedImei() != null )
			{
				if(campaignData.getGroup().getRemovedImei().contains(device.getImei()))
				{
					deviceCampaignStatus.setRunningStatus(DeviceStatusForCampaign.REMOVED.getValue());
				}
			}
			
			if(campaignData.getGroup().getExcludedImei() != null )
			{
				if(campaignData.getGroup().getExcludedImei().contains(device.getImei()))
				{
					deviceCampaignStatus.setRunningStatus(DeviceStatusForCampaign.EXCLUDED.getValue());
				}
			}
			deviceCampaignStatusList.add(deviceCampaignStatus);
			count++;
			if(count > 1000)
			{
				iteration++;
				logger.info(msgUuid + " Saving Processed " +iteration*count);
				deviceCampaignStatusRepository.saveAll(deviceCampaignStatusList);
				deviceCampaignStatusList =new ArrayList<DeviceCampaignStatus>();
				count =0;
				
			//	logger.info(msgUuid + "Saving 1000 "+ iterator++);
			}
			
			
		}

		/*
		 * List<Object> startedDevices =
		 * deviceDetailRepository.findCountOfExecutedGateways(campaignData.getUuid());
		 * long startedDeviceCount = startedDevices == null?0 :startedDevices.size();
		 * inProgress = startedDeviceCount - completed;
		 */
		deviceCampaignStatusRepository.saveAll(deviceCampaignStatusList);
		logger.info(msgUuid + " Complete Processing of ");
		return returnList;
		
	}

@Async
	public void processDeviceForSingleCampaign(DeviceCampaignStatusDTO deviceCampaignStatusDTO, String msgUUID) {
		logger.info(msgUUID + "inside process for processDeviceForSingleCampaign");
		LocalDateTime ldt = LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC);
		Instant now = ldt.toInstant(ZoneOffset.UTC);
	
		String deviceId = deviceCampaignStatusDTO.getDeviceId();
		String campaignUUID = deviceCampaignStatusDTO.getCurrentCampaignUUID();
		Campaign campaign = campaignRepository.findByUuid(campaignUUID);
		logger.info(msgUUID + "inside process for processDeviceForSingleCampaign 1");
		DeviceReport deviceReport = deviceCampaignStatusDTO.getDeviceReport();

		List<DeviceCampaignStatus> deviceCampaignStatusList = deviceCampaignStatusRepository
				.findByDeviceAndCampaignUUID(deviceId, campaignUUID);
		logger.info(msgUUID + "inside process for processDeviceForSingleCampaign 2");
		DeviceCampaignStatus deviceCampaignStatus = null;
		List<String> allImeis = new ArrayList<String>();
		allImeis.add(deviceId);
		if (deviceCampaignStatusList.size() > 0) {
			if (deviceCampaignStatusList.get(0).getRunningStatus() != null
					&& deviceCampaignStatusList.get(0).getRunningStatus().equals(DeviceStatusForCampaign.COMPLETED.getValue()))
				return;
			deviceCampaignStatus = deviceCampaignStatusList.get(0);
		} else {
			String deviceStatusInCampaign = getDeviceStatusInCampaign(allImeis, campaign, msgUUID);

			deviceCampaignStatus = new DeviceCampaignStatus();

			deviceCampaignStatus.setRunningStatus(deviceStatusInCampaign);
			deviceCampaignStatus.setCampaign(campaign);
			deviceCampaignStatus.setCreatedAt(now);
			deviceCampaignStatus.setDeviceId(deviceCampaignStatusDTO.getDeviceId());
			deviceCampaignStatus.setLastReportedAt(now);
			deviceCampaignStatus.setOnHold(StatsStatus.ON_HOLD.getValue());
			deviceCampaignStatus.setOffOnPath(Constants.ON_PATH);
			Boolean installedFlag = campaignUtils.isInstalledFlagTrue(msgUUID, deviceId);
			logger.info(msgUUID + "inside process for processDeviceForSingleCampaign 2.5");
			deviceCampaignStatus.setInstalledFlag(installedFlag.toString());
			List<CampaignStep> allCampaignSteps = campaignStepRepository.findByCampaignUuid(campaign.getUuid());
			
			logger.info(msgUUID + "inside process for processDeviceForSingleCampaign 2.6");

			Package baseLine = allCampaignSteps.get(0).getFromPackage();
			String isEligible = campaignUtils.checkDeviceMatchesBaseline(deviceId, deviceReport, baseLine, msgUUID);
			deviceCampaignStatus.setEligibility(isEligible);

		}
		deviceCampaignStatus.setLastCompletedStepUUID(deviceCampaignStatusDTO.getLastSuccessStepUUID());
		if (deviceCampaignStatus.getLastCompletedStepUUID() == null) {
			deviceCampaignStatus.setLastCompletedStepUUID(DeviceStatusForCampaign.NOTSTARTED.getValue());
		}

		deviceCampaignStatus.setLastStepUUID(deviceCampaignStatusDTO.getLastPendingStepUUID());
		// Package baseLine = packageRepository.findByUuid(basePackageUuid);
		if (deviceCampaignStatusDTO.getProblemStatus() != null) {
			deviceCampaignStatus.setProblem(Constants.PROBLEM);
			deviceCampaignStatus.setProblemComment(deviceCampaignStatusDTO.getProblemComment());
		} else {
			deviceCampaignStatus.setProblem(Constants.NOPPROBLEM);
			deviceCampaignStatus.setProblemComment(null);

		}

		deviceCampaignStatus.setRunningStatus(deviceCampaignStatusDTO.getCampaignRunningStatus().getValue());
		deviceCampaignStatusRepository.save(deviceCampaignStatus);
		logger.info(msgUUID + "exiting process for processDeviceForSingleCampaign 3");
	}


	private String getDeviceStatusInCampaign(List<String> allImeis, Campaign campaign, String msgUUID) {
		String status = null;
		String firstStepStatusInThisCamp = null;
		String lastStepStatusInThisCamp = null;
		List<StepStatusForDeviceView> firstStepThisCamp = new ArrayList<StepStatusForDeviceView>();
		List<StepStatusForDeviceView> lastStepInThisCamp = new ArrayList<StepStatusForDeviceView>();
		Long maxStepInCamp = campaignStepRepository.findLastStepByCampaignUuid(campaign.getUuid()).getStepOrderNumber();
		if (allImeis != null && !allImeis.isEmpty()) {
			firstStepThisCamp = deviceDetailRepository.findAllStatusByCampaignUuidAndStepOrderNumber(campaign.getUuid(),
					allImeis, 1l);
			lastStepInThisCamp = deviceDetailRepository
					.findAllStatusByCampaignUuidAndStepOrderNumber(campaign.getUuid(), allImeis, maxStepInCamp);

		}
		if (firstStepThisCamp != null && !firstStepThisCamp.isEmpty()) {
			firstStepStatusInThisCamp = firstStepThisCamp.get(0).getStatus();
		}
		if (lastStepInThisCamp != null && !lastStepInThisCamp.isEmpty()) {
			lastStepStatusInThisCamp = lastStepInThisCamp.get(0).getStatus();

		}
		if (!StringUtils.isEmpty(firstStepStatusInThisCamp)) {
			if (lastStepStatusInThisCamp != null
					&& lastStepStatusInThisCamp.equalsIgnoreCase(CampaignStepDeviceStatus.SUCCESS.getValue())) {
				// set status as complete
				status = DeviceStatusForCampaign.SUCCESS.getValue();

			} else {
				status = (DeviceStatusForCampaign.PENDNG.getValue());
			}
		} else {
			status = (DeviceStatusForCampaign.NOTSTARTED.getValue());
			// set status as not started
		}

		return status;
	}

	private String getDeviceStatusInCampaign(Map<String, List<StepStatusForDeviceView>> firstStepStatusInThisCampMap,
			Map<String, List<StepStatusForDeviceView>> lastStepStatusInThisCampMap,
			Map<String, List<CampaignStepDeviceDetail>> currentStepStatusInThisCampMap,
			Map<String, List<CampaignConfigProblem>> problemIMEIMap, String deviceId,
			DeviceCampaignStatusDTO deviceCampaignStatusDTO, String msgUUID, String eligible) {

		logger.info("------8999");
		String status = null;
		String firstStepStatusInThisCamp = null;
		String lastStepStatusInThisCamp = null;
		String currentStatusInThisCamp = null;
		String currentStatusInThisCampUUID = null;
		long lastStepNumber = 0;
		String lastStepExecutionDate = null;

		List<CampaignStepDeviceDetail> currentStepStatusInThisCampList = currentStepStatusInThisCampMap.get(deviceId);
		List<CampaignConfigProblem> problemIMEIList = problemIMEIMap.get(deviceId);

		List<StepStatusForDeviceView> firstStepStatusInThisCampList = firstStepStatusInThisCampMap.get(deviceId);

		if (firstStepStatusInThisCampList != null && !firstStepStatusInThisCampList.isEmpty()) {
			firstStepStatusInThisCamp = firstStepStatusInThisCampList.get(0).getStatus();
		}

		List<StepStatusForDeviceView> lastStepStatusInThisCampList = lastStepStatusInThisCampMap.get(deviceId);

		if (lastStepStatusInThisCampList != null && !lastStepStatusInThisCampList.isEmpty()) {
			lastStepStatusInThisCamp = lastStepStatusInThisCampList.get(0).getStatus();
		}
		if (problemIMEIList != null && !problemIMEIList.isEmpty()) {
			deviceCampaignStatusDTO.setProblemStatus(DeviceStatusForCampaign.PROBLEM.getValue());
			deviceCampaignStatusDTO.setProblemComment(problemIMEIList.get(0).getComments());
		}

		if (currentStepStatusInThisCampList != null && !currentStepStatusInThisCampList.isEmpty()) {
			currentStatusInThisCamp = currentStepStatusInThisCampList.get(0).getStatus().getValue();
			currentStatusInThisCampUUID = currentStepStatusInThisCampList.get(0).getCampaignStep().getUuid();
			if (currentStepStatusInThisCampList.get(0).getCampaignStep().getStepOrderNumber() != null)
				lastStepNumber = currentStepStatusInThisCampList.get(0).getCampaignStep().getStepOrderNumber();
			if (currentStepStatusInThisCampList.get(0).getStopExecutionTime() != null)

				lastStepExecutionDate = currentStepStatusInThisCampList.get(0).getStopExecutionTime().toString();
		}
		if (!StringUtils.isEmpty(firstStepStatusInThisCamp)) {
			if (lastStepStatusInThisCamp != null
					&& lastStepStatusInThisCamp.equalsIgnoreCase(CampaignStepDeviceStatus.SUCCESS.getValue())) {
				// set status as complete
				logger.info(msgUUID + "  --11 772");
				status = DeviceStatusForCampaign.SUCCESS.getValue();
				deviceCampaignStatusDTO.setCurrentStepUUID(currentStatusInThisCampUUID);
				deviceCampaignStatusDTO.setLastSuccessStepUUID(currentStatusInThisCampUUID);
				deviceCampaignStatusDTO.setLastStepOrderNumber(lastStepNumber);

			} else {
				logger.info(msgUUID + "  -- 12772");
				deviceCampaignStatusDTO.setCurrentStepUUID(currentStatusInThisCampUUID);
				deviceCampaignStatusDTO.setLastSuccessStepUUID(currentStatusInThisCampUUID);
				status = (DeviceStatusForCampaign.PENDNG.getValue());
				deviceCampaignStatusDTO.setLastSuccessStepUUID(currentStatusInThisCampUUID);
				deviceCampaignStatusDTO.setLastStepOrderNumber(lastStepNumber);

			}
		} else {
			logger.info(msgUUID + "  -- 13772");
			
			if (!(DeviceStatusForCampaign.NOT_ELIGIBLE.getValue().contentEquals(eligible)))
				status = (DeviceStatusForCampaign.ELIGIBLE.getValue());
			else
				status = DeviceStatusForCampaign.NOT_ELIGIBLE.getValue();
			
		}

		return status;
	}

	

	@Async
	public void processSingleDeviceForAllCampaignPostExecution(DeviceCampaignStatusDTO deviceCampaignStatusDTO, String msgUUID) {
		try {
			logger.info(msgUUID + "  processSingleDeviceForAllCampaignPostExecution --773");
			LocalDateTime ldt = LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC);
			Instant now = ldt.toInstant(ZoneOffset.UTC);
		
			List<Campaign> activeCampaignForImei;
			activeCampaignForImei = campaignUtils.checkCampaignForDeviceId(deviceCampaignStatusDTO.getDeviceId(),
					deviceCampaignStatusDTO.getCustomerNam());
			DeviceCampaignStatus deviceCampaignStatus = null;
			List<String> allImeis = new ArrayList<String>();
			allImeis.add(deviceCampaignStatusDTO.getDeviceId());
			for (Campaign campaign : activeCampaignForImei) {
				logger.info(msgUUID + "  processSingleDeviceForAllCampaignPostExecution --774 " + campaign.getCampaignName());
				if(campaign.getGroup().getExcludedImei() != null && campaign.getGroup().getExcludedImei().contains(deviceCampaignStatusDTO.getDeviceId()))
				{
					continue;
				}
				if(campaign.getGroup().getRemovedImei() != null && campaign.getGroup().getRemovedImei().contains(deviceCampaignStatusDTO.getDeviceId()))
				{
					continue;
				}
				List<DeviceCampaignStatus> deviceCampaignStatusList = deviceCampaignStatusRepository
						.findStatusByCampaignUuidAndCampaignUUID(deviceCampaignStatusDTO.getDeviceId(),
								campaign.getUuid());
				if (deviceCampaignStatusList.size() > 0) {
					if (deviceCampaignStatusList.get(0).getRunningStatus() != null && !(deviceCampaignStatusList.get(0)
							.getRunningStatus().equals(DeviceStatusForCampaign.ELIGIBLE.getValue())))
						continue;
					deviceCampaignStatus = deviceCampaignStatusList.get(0);
				} else {

					String deviceStatusInCampaign = getDeviceStatusInCampaign(allImeis, campaign, msgUUID);

					deviceCampaignStatus = new DeviceCampaignStatus();

					deviceCampaignStatus.setRunningStatus(deviceStatusInCampaign);
					deviceCampaignStatus.setCampaign(campaign);
					deviceCampaignStatus.setCreatedAt(now);
					deviceCampaignStatus.setDeviceId(deviceCampaignStatusDTO.getDeviceId());
					deviceCampaignStatus.setLastReportedAt(now);
					deviceCampaignStatus.setOnHold(StatsStatus.ON_HOLD.getValue());
					deviceCampaignStatus.setOffOnPath(Constants.ON_PATH);
					Boolean installedFlag = campaignUtils.isInstalledFlagTrue(msgUUID,
							deviceCampaignStatusDTO.getDeviceId());
					deviceCampaignStatus.setInstalledFlag(installedFlag.toString());

				}

				List<CampaignStep> allCampaignSteps = campaignStepRepository.findByCampaignUuid(campaign.getUuid());
				Package baseLine = allCampaignSteps.get(0).getFromPackage();
				String isEligible = campaignUtils.checkDeviceMatchesBaseline(deviceCampaignStatusDTO.getDeviceId(),
						deviceCampaignStatusDTO.getDeviceReport(), baseLine, msgUUID);
				deviceCampaignStatus.setEligibility(isEligible);
				deviceCampaignStatusRepository.save(deviceCampaignStatus);
				logger.info(msgUUID + "  processSingleDeviceForAllCampaignPostExecution --775 " + campaign.getCampaignName());
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}

}
