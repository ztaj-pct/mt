package com.pct.device.version.validation;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.pct.common.dto.MsDeviceRestResponse;
import com.pct.device.service.device.DeviceReport;
import com.pct.device.version.constant.CampaignStatus;
import com.pct.device.version.constant.CampaignStepDeviceStatus;
import com.pct.device.version.constant.DeviceStatusForCampaign;
import com.pct.device.version.constant.GroupingConstants;
import com.pct.device.version.model.Campaign;
import com.pct.device.version.model.CampaignStep;
import com.pct.device.version.model.CampaignStepDeviceDetail;
import com.pct.device.version.model.Package;
import com.pct.device.version.repository.ICampaignRepository;
import com.pct.device.version.repository.ICampaignStepDeviceDetailRepository;
import com.pct.device.version.repository.ICampaignStepRepository;
import com.pct.device.version.repository.IDeviceVersionRepository;
//import com.pct.device.version.repository.msdevice.ICampaignInstalledDeviceMsRepository;
import com.pct.device.version.util.CampaignDeviceHelper;
import com.pct.device.version.util.Constants;
import com.pct.device.version.util.RestUtils;

@Component
public class CampaignUtils {

	Logger logger = LoggerFactory.getLogger(CampaignUtils.class);
	Logger analysisLog = LoggerFactory.getLogger("analytics");

	@Autowired
	private ICampaignRepository campaignRepo;
	@Autowired
	private ICampaignStepRepository campaignStepRepository;
	@Autowired
	private ICampaignStepDeviceDetailRepository campaignStepDeviceDetailRepository;
	@Autowired
	private RestUtils restUtils;
	@Autowired
	private CampaignDeviceHelper campaignDeviceHelper;
	
/*	@Autowired
	private ICampaignInstalledDeviceMsRepository iCampaignInstalledDeviceMsRepository;
*/	
	@Autowired
	private IDeviceVersionRepository deviceVersionRepository;

	private CampaignUtils() {

	}

	public Campaign checkForCompletedCampaign(Campaign campaign) {
		LocalDateTime ldt = LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC);
		Instant now = ldt.toInstant(ZoneOffset.UTC);
	
		if (campaign != null) {
			if (campaign.getGroup().getGroupingName().equalsIgnoreCase(GroupingConstants.GROUPING_NAME_CUSTOM)) {
				CampaignStep lastStepByCampaignUuid = campaignStepRepository
						.findLastStepByCampaignUuid(campaign.getUuid());
				List<CampaignStepDeviceDetail> lastStepRuns = campaignStepDeviceDetailRepository
						.findByCampaignStepAndStatus(lastStepByCampaignUuid.getUuid(),
								CampaignStepDeviceStatus.SUCCESS);
				if (lastStepRuns != null) {
					List<String> targetIMEIs = Arrays.asList(campaign.getGroup().getTargetValue().split(","));
					if (lastStepRuns.size() == targetIMEIs.size()) {
						campaign.setCampaignStatus(CampaignStatus.FINISHED);
						campaign.setIsActive(false);
						campaign.setUpdatedAt(now);
						campaign = campaignRepo.save(campaign);
						logger.info("Campaign " + campaign.getCampaignName() + " , Campaign UUID : "
								+ campaign.getUuid() + " ==> finished ");
						analysisLog.debug("Campaign " + campaign.getCampaignName() + " , Campaign UUID : "
								+ campaign.getUuid() + " ==> finished ");
					}
				}
			}
		}
		return campaign;
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
		
		
	}
		public boolean waterfallFieldCheckScheduler(String packageField, String executeCampaignRequestField, String msgUuid) {

			if (packageField.equalsIgnoreCase(Constants.ANY) || packageField.equalsIgnoreCase(executeCampaignRequestField)
					|| (packageField != null && executeCampaignRequestField != null
							&& packageField.trim().equalsIgnoreCase(executeCampaignRequestField.trim()))
					|| packageField.equalsIgnoreCase(Constants.EMPTY_STRING) || executeCampaignRequestField == null) {

						analysisLog.debug("Message UUID (13.C) : " + msgUuid + " , PackageField : " + packageField
						+ " , ExecuteCampaignRequestField : " + executeCampaignRequestField
						+ " , Condition matched : true");

				return true;

			} else {

					analysisLog.debug("Message UUID (13.D) : " + msgUuid + " , PackageField : " + packageField
						+ " , ExecuteCampaignRequestField : " + executeCampaignRequestField
						+ " , Condition matched : false");

				return false;

			}
//		return (packageField.equalsIgnoreCase(Constants.ANY)
//				|| packageField.equalsIgnoreCase(executeCampaignRequestField)
//				|| packageField.equalsIgnoreCase(Constants.EMPTY_STRING) || executeCampaignRequestField == null) ;

	}

	public boolean configFieldCheck(String packageField, String executeCampaignRequestField, String msgUuid) {

		if (packageField.equalsIgnoreCase(Constants.ANY)
				|| packageField.equalsIgnoreCase(executeCampaignRequestField)
				|| (packageField != null && executeCampaignRequestField != null
				&& packageField.trim().equalsIgnoreCase(executeCampaignRequestField.trim()))) {

			logger.info("Message UUID (13.A) : " + msgUuid + " , PackageField : " + packageField
					+ " , ExecuteCampaignRequestField : " + executeCampaignRequestField
					+ " , Condition matched : true");
			analysisLog.debug("Message UUID (13.A) : " + msgUuid + " , PackageField : " + packageField
					+ " , ExecuteCampaignRequestField : " + executeCampaignRequestField
					+ " , Condition matched : true");

			return true;

		} else {

			logger.info("Message UUID (13.B) : " + msgUuid + " , PackageField : " + packageField
					+ " , ExecuteCampaignRequestField : " + executeCampaignRequestField
					+ " , Condition matched : false");
			analysisLog.debug("Message UUID (13.B) : " + msgUuid + " , PackageField : " + packageField
					+ " , ExecuteCampaignRequestField : " + executeCampaignRequestField
					+ " , Condition matched : false");
			return false;

		}
//		return (packageField.equalsIgnoreCase(Constants.ANY)
//				|| packageField.equalsIgnoreCase(executeCampaignRequestField));

	}
	
	
	public boolean configFieldCheckScheduler(String packageField, String executeCampaignRequestField, String msgUuid) {

		if (packageField.equalsIgnoreCase(Constants.ANY)
				|| packageField.equalsIgnoreCase(executeCampaignRequestField)
				|| (packageField != null && executeCampaignRequestField != null
				&& packageField.trim().equalsIgnoreCase(executeCampaignRequestField.trim()))) {

				analysisLog.debug("Message UUID (13.A) : " + msgUuid + " , PackageField : " + packageField
					+ " , ExecuteCampaignRequestField : " + executeCampaignRequestField
					+ " , Condition matched : true");

			return true;

		} else {

			analysisLog.debug("Message UUID (13.B) : " + msgUuid + " , PackageField : " + packageField
					+ " , ExecuteCampaignRequestField : " + executeCampaignRequestField
					+ " , Condition matched : false");
			return false;

		}
//		return (packageField.equalsIgnoreCase(Constants.ANY)
//				|| packageField.equalsIgnoreCase(executeCampaignRequestField));

	}

	public Campaign checkForPausedCampaign(Campaign campaign) {
		LocalDateTime ldt = LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC);
		Instant now = ldt.toInstant(ZoneOffset.UTC);
	
		if (campaign != null) {
			if (campaign.getPauseExecution()) {
				CampaignStep lastStepByCampaignUuid = campaignStepRepository
						.findLastStepByCampaignUuid(campaign.getUuid());
				List<CampaignStepDeviceDetail> lastStepRuns = campaignStepDeviceDetailRepository
						.findByCampaignStepAndStatus(lastStepByCampaignUuid.getUuid(),
								CampaignStepDeviceStatus.SUCCESS);
				if (lastStepRuns != null) {
					if (campaign.getPauseExecution()) {
						Long pauseLimit = campaign.getPauseLimit();
						if (lastStepRuns != null && lastStepRuns.size() == pauseLimit) {
							campaign.setCampaignStatus(CampaignStatus.PAUSED);
							campaign.setUpdatedAt(now);
							campaign = campaignRepo.save(campaign);
							logger.info("Campaign " + campaign.getCampaignName() + " , Campaign UUID : "
									+ campaign.getUuid() + " paused ");
							analysisLog.debug("Campaign " + campaign.getCampaignName() + " , Campaign UUID : "
									+ campaign.getUuid() + " paused ");
						}
					}
				}
			}
		}
		return campaign;
	}

	public Campaign checkForIdleCampaign(Campaign campaign) {
		LocalDateTime ldt = LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC);
		Instant now = ldt.toInstant(ZoneOffset.UTC);
	
		if (campaign != null) {
			if (campaign.getGroup().getGroupingName().equalsIgnoreCase(GroupingConstants.GROUPING_NAME_CUSTOM)) {
				CampaignStep lastStepByCampaignUuid = campaignStepRepository
						.findLastStepByCampaignUuid(campaign.getUuid());
				List<CampaignStepDeviceDetail> lastStepRuns = campaignStepDeviceDetailRepository
						.findByCampaignStepAndStatus(lastStepByCampaignUuid.getUuid(),
								CampaignStepDeviceStatus.SUCCESS);
				if (lastStepRuns != null) {
					List<String> targetIMEIs = Arrays.asList(campaign.getGroup().getTargetValue().split(","));
					if (lastStepRuns.size() == targetIMEIs.size()) {
						// campaign.setCampaignStatus(CampaignStatus.FINISHED);
						// campaign.setIsActive(false);
						campaign.setUpdatedAt(now);
						campaign = campaignRepo.save(campaign);
						logger.info("Campaign " + campaign.getCampaignName() + " finished ");
						analysisLog.debug("Campaign " + campaign.getCampaignName() + " finished ");
					}
				}
			}
		}
		return campaign;
	}

	public Integer calculateCampaignCompletion(Campaign campaign) {
		Integer percent = null;
		if (campaign != null) {
			CampaignStep lastStepByCampaignUuid = campaignStepRepository.findLastStepByCampaignUuid(campaign.getUuid());
			List<CampaignStepDeviceDetail> lastStepRuns = campaignStepDeviceDetailRepository
					.findByCampaignAndStatus(campaign.getUuid(), CampaignStepDeviceStatus.SUCCESS);
			if (lastStepRuns != null) {

				// lastStepRuns = 3 device out of 30 are done with final step.
				Long imeiCount = 0l;

				if (campaign.getGroup().getGroupingType().equals("Customer")) {
					//List<MsDeviceRestResponse> allDevices = restUtils.getDevicesFromMSByCustomerName(campaign.getGroup().getGroupingName());
					List<MsDeviceRestResponse> allDevices = deviceVersionRepository.findImeisByCustomerName(campaign.getGroup().getGroupingName());

					if (allDevices != null) {
						imeiCount = Long.valueOf(allDevices.size());
					}
				} else {
					String allImei = campaign.getGroup().getTargetValue();
					String[] arr = null;
					if (!StringUtils.isEmpty(allImei)) {
						arr = allImei.split(",");
						imeiCount = Long.valueOf(arr.length);
					}
				}
				if (imeiCount == 0) {
					return 0;
				}
				percent = (int) ((lastStepRuns.size() * 100)
						/ (lastStepByCampaignUuid.getStepOrderNumber() * imeiCount));
			}
		}
		return percent;
	}

	public List<Campaign> checkCampaignForDeviceId(String deviceId, String customer) {
		List<Campaign> activeCampaignsForImei = new ArrayList<>();
		List<Campaign> dynamicCustomerCampaigns = campaignRepo.findActiveCampaignByDynamicCustomer(customer);
		List<Campaign> staticGroupingCampaigns = campaignRepo.findByIMEIForStaticGrouping(deviceId);
		activeCampaignsForImei.addAll(dynamicCustomerCampaigns);
		activeCampaignsForImei.addAll(staticGroupingCampaigns);
		return activeCampaignsForImei;
	}

	public String checkDeviceMatchesBaseline(String deviceId, Map<String, List<DeviceReport>> deviceMaintReportMap,
			Package baseLine, String msgUuid) {

		DeviceReport latestMaint = null;
		if (deviceMaintReportMap != null && deviceMaintReportMap.get(deviceId) != null) {
			latestMaint = deviceMaintReportMap.get(deviceId).get(0);
		}
		return checkDeviceMatchesBaseline( deviceId, latestMaint,
				 baseLine,  msgUuid);
	}
	
	
	
	public String checkDeviceMatchesBaselineScheduler(String deviceId, Map<String, List<DeviceReport>> deviceMaintReportMap,
			Package baseLine, String msgUuid) {

		DeviceReport latestMaint = null;
		if (deviceMaintReportMap != null && deviceMaintReportMap.get(deviceId) != null) {
			latestMaint = deviceMaintReportMap.get(deviceId).get(0);
		}
		if (latestMaint != null && latestMaint.getEXTENDER_VERSION() != null
				&& latestMaint.getBASEBAND_SW_VERSION() != null && latestMaint.getBLE_VERSION() != null
				&& latestMaint.getAPP_SW_VERSION() != null) {
			latestMaint = removePrefixInMaintReport(latestMaint);

			

			StringBuilder sb = new StringBuilder();
			sb.append("Message UUID (24) : " + msgUuid).append("   .   ");
			sb.append(
					"Comparing baseLine fields with executeCampaignRequest fields in checkDeviceMatchesBaseline method. ")
					.append("   .   ");
			sb.append("baseLine AppVer : " + baseLine.getAppVersion()).append(" | ")
					.append("ExecuteCampaignRequest AppVer : " + latestMaint.getAPP_SW_VERSION()).append("   .   ");
			sb.append("baseLine BinVer: " + baseLine.getBinVersion()).append(" | ")
					.append("ExecuteCampaignRequest BinVer: " + latestMaint.getBASEBAND_SW_VERSION()).append("   .   ");
			sb.append("baseLine BleVer: " + baseLine.getBleVersion()).append(" | ")
					.append("ExecuteCampaignRequest BleVer: " + latestMaint.getBLE_VERSION()).append("   .   ");
			sb.append("baseLine McuVer: " + baseLine.getMcuVersion()).append(" | ")
					.append("ExecuteCampaignRequest McuVer: " + latestMaint.getEXTENDER_VERSION()).append("   .   ");
			sb.append("baseLine Config 1: " + baseLine.getConfig1() + ", " + baseLine.getConfig1Crc()).append(" | ")
					.append("ExecuteCampaignRequest Config 1: " + latestMaint.getConfig1CIV() + ", "
							+ latestMaint.getConfig1CRC())
					.append("   .   ");
			sb.append("baseLine Config 2: " + baseLine.getConfig2() + ", " + baseLine.getConfig2Crc()).append(" | ")
					.append("ExecuteCampaignRequest Config 2: " + latestMaint.getConfig2CIV() + ", "
							+ latestMaint.getConfig2CRC())
					.append("   .   ");
			sb.append("baseLine Config 3: " + baseLine.getConfig3() + ", " + baseLine.getConfig3Crc()).append(" | ")
					.append("ExecuteCampaignRequest Config 3: " + latestMaint.getConfig3CIV() + ", "
							+ latestMaint.getConfig3CRC())
					.append("   .   ");
			sb.append("baseLine Config 4: " + baseLine.getConfig4() + ", " + baseLine.getConfig4Crc()).append(" | ")
					.append("ExecuteCampaignRequest Config 4: " + latestMaint.getConfig4CIV() + ", "
							+ latestMaint.getConfig4CRC())
					.append("   .   ");
			sb.append("baseLine Maxbotix Firmware Ver : " + baseLine.getCargoMaxbotixFirmware()).append(" | ")
					.append("ExecuteCampaignRequest Maxbotix Firmware Ver : " + latestMaint.getMaxbotixFirmware())
					.append("   .   ");
			sb.append("baseLine Maxbotix Hardware Ver : " + baseLine.getCargoMaxbotixHardware()).append(" | ")
					.append("ExecuteCampaignRequest Maxbotix Firmware Ver : " + latestMaint.getMaxbotixhardware())
					.append("   .   ");
			sb.append("baseLine Riot Firmware Ver : " + baseLine.getCargoRiotFirmware()).append(" | ")
					.append("ExecuteCampaignRequest Riot Firmware Ver : " + latestMaint.getRiotFirmware()).append("   .   ");
			sb.append("baseLine Riot Hardware Ver : " + baseLine.getCargoRiotHardware()).append(" | ")
					.append("ExecuteCampaignRequest Riot Hardware Ver : " + latestMaint.getRiothardware()).append("   .   ");
			sb.append("baseLine Lite-Sentry App Ver : " + baseLine.getLiteSentryApp()).append(" | ")
					.append("ExecuteCampaignRequest Lite-Sentry App Ver : " + latestMaint.getLiteSentryApp())
					.append("   .   ");
			sb.append("baseLine Lite-Sentry Boot Ver : " + baseLine.getLiteSentryBoot()).append(" | ")
					.append("ExecuteCampaignRequest Lite-Sentry Boot Ver : " + latestMaint.getLiteSentryBoot())
					.append("   .   ");
			sb.append("baseLine Lite-Sentry Hardware Ver : " + baseLine.getLiteSentryHardware()).append(" | ")
					.append("ExecuteCampaignRequest Lite-Sentry Hardware Ver : " + latestMaint.getLiteSentryHw())
					.append("   .   ");
			sb.append("baseLine Micro SP MCU Ver : " + baseLine.getMicrospMcu()).append(" | ")
					.append("ExecuteCampaignRequest Micro SP MCU Ver : " + latestMaint.getSteMCU()).append("   .   ");
			sb.append("baseLine Micro SP App Ver : " + baseLine.getMicrospApp()).append(" | ")
					.append("ExecuteCampaignRequest Micro SP App Ver : " + latestMaint.getSteApp()).append("   .   ");
			sb.append("baseLine Device Type : " + baseLine.getDeviceType()).append(" | ")
					.append("ExecuteCampaignRequest  Device Type : " + latestMaint.getDevice_type()).append("   .   ");

			//logger.info(sb.toString());
			analysisLog.debug(sb.toString());

			if (configFieldCheckScheduler(baseLine.getAppVersion(), latestMaint.getAPP_SW_VERSION(), msgUuid)
					&& configFieldCheckScheduler(baseLine.getBinVersion(), latestMaint.getBASEBAND_SW_VERSION() , msgUuid)
					&& configFieldCheckScheduler(baseLine.getBleVersion(), latestMaint.getBLE_VERSION() ,msgUuid)
					&& configFieldCheckScheduler(baseLine.getMcuVersion(), latestMaint.getEXTENDER_VERSION() ,msgUuid)
					&& configFieldCheckScheduler(baseLine.getDeviceType(), latestMaint.getDevice_type(),msgUuid)
					&& configFieldCheckScheduler(baseLine.getCargoMaxbotixFirmware(), latestMaint.getMaxbotixFirmware(),msgUuid)
					&& configFieldCheckScheduler(baseLine.getCargoMaxbotixHardware(), latestMaint.getMaxbotixhardware(),msgUuid)
					&& configFieldCheckScheduler(baseLine.getCargoRiotFirmware(), latestMaint.getRiotFirmware(),msgUuid)
					&& configFieldCheckScheduler(baseLine.getCargoRiotHardware(), latestMaint.getRiothardware(),msgUuid)
					&& configFieldCheckScheduler(baseLine.getMicrospApp(), latestMaint.getSteApp(),msgUuid)
					&& configFieldCheckScheduler(baseLine.getMicrospMcu(), latestMaint.getSteMCU(),msgUuid)
					&& configFieldCheckScheduler(baseLine.getLiteSentryApp(), latestMaint.getLiteSentryApp(),msgUuid)
					&& configFieldCheckScheduler(baseLine.getLiteSentryBoot(), latestMaint.getLiteSentryBoot(),msgUuid)
					&& configFieldCheckScheduler(baseLine.getLiteSentryHardware(), latestMaint.getLiteSentryHw(),msgUuid)
					&& waterfallFieldCheckScheduler(baseLine.getConfig1Crc(), latestMaint.getConfig1CRC(),msgUuid)
					&& waterfallFieldCheckScheduler(baseLine.getConfig2Crc(), latestMaint.getConfig2CRC(),msgUuid)
					&& waterfallFieldCheckScheduler(baseLine.getConfig3Crc(), latestMaint.getConfig3CRC(),msgUuid)
					&& waterfallFieldCheckScheduler(baseLine.getConfig4Crc(), latestMaint.getConfig4CRC(),msgUuid)
					&& waterfallFieldCheckScheduler(baseLine.getConfig2(), latestMaint.getConfig2CIV(),msgUuid)
					&& waterfallFieldCheckScheduler(baseLine.getConfig3(), latestMaint.getConfig3CIV(),msgUuid)
					&& waterfallFieldCheckScheduler(baseLine.getConfig4(), latestMaint.getConfig4CIV(),msgUuid)) {
				return DeviceStatusForCampaign.ELIGIBLE.getValue();
			}

		} else {
			
			analysisLog.info("Message UUID (24) : " + msgUuid + ", latestMaint is null");
		}
		return "";
	}
	public String checkDeviceMatchesBaseline(String deviceId,  DeviceReport latestMaint,
			Package baseLine, String msgUuid) {

		if (latestMaint != null && latestMaint.getEXTENDER_VERSION() != null
				&& latestMaint.getBASEBAND_SW_VERSION() != null && latestMaint.getBLE_VERSION() != null
				&& latestMaint.getAPP_SW_VERSION() != null) {
			latestMaint = removePrefixInMaintReport(latestMaint);

			

			StringBuilder sb = new StringBuilder();
			sb.append("Message UUID (24) : " + msgUuid).append("   .   ");
			sb.append(
					"Comparing baseLine fields with executeCampaignRequest fields in checkDeviceMatchesBaseline method. ")
					.append("   .   ");
			sb.append("baseLine AppVer : " + baseLine.getAppVersion()).append(" | ")
					.append("ExecuteCampaignRequest AppVer : " + latestMaint.getAPP_SW_VERSION()).append("   .   ");
			sb.append("baseLine BinVer: " + baseLine.getBinVersion()).append(" | ")
					.append("ExecuteCampaignRequest BinVer: " + latestMaint.getBASEBAND_SW_VERSION()).append("   .   ");
			sb.append("baseLine BleVer: " + baseLine.getBleVersion()).append(" | ")
					.append("ExecuteCampaignRequest BleVer: " + latestMaint.getBLE_VERSION()).append("   .   ");
			sb.append("baseLine McuVer: " + baseLine.getMcuVersion()).append(" | ")
					.append("ExecuteCampaignRequest McuVer: " + latestMaint.getEXTENDER_VERSION()).append("   .   ");
			sb.append("baseLine Config 1: " + baseLine.getConfig1() + ", " + baseLine.getConfig1Crc()).append(" | ")
					.append("ExecuteCampaignRequest Config 1: " + latestMaint.getConfig1CIV() + ", "
							+ latestMaint.getConfig1CRC())
					.append("   .   ");
			sb.append("baseLine Config 2: " + baseLine.getConfig2() + ", " + baseLine.getConfig2Crc()).append(" | ")
					.append("ExecuteCampaignRequest Config 2: " + latestMaint.getConfig2CIV() + ", "
							+ latestMaint.getConfig2CRC())
					.append("   .   ");
			sb.append("baseLine Config 3: " + baseLine.getConfig3() + ", " + baseLine.getConfig3Crc()).append(" | ")
					.append("ExecuteCampaignRequest Config 3: " + latestMaint.getConfig3CIV() + ", "
							+ latestMaint.getConfig3CRC())
					.append("   .   ");
			sb.append("baseLine Config 4: " + baseLine.getConfig4() + ", " + baseLine.getConfig4Crc()).append(" | ")
					.append("ExecuteCampaignRequest Config 4: " + latestMaint.getConfig4CIV() + ", "
							+ latestMaint.getConfig4CRC())
					.append("   .   ");
			sb.append("baseLine Maxbotix Firmware Ver : " + baseLine.getCargoMaxbotixFirmware()).append(" | ")
					.append("ExecuteCampaignRequest Maxbotix Firmware Ver : " + latestMaint.getMaxbotixFirmware())
					.append("   .   ");
			sb.append("baseLine Maxbotix Hardware Ver : " + baseLine.getCargoMaxbotixHardware()).append(" | ")
					.append("ExecuteCampaignRequest Maxbotix Firmware Ver : " + latestMaint.getMaxbotixhardware())
					.append("   .   ");
			sb.append("baseLine Riot Firmware Ver : " + baseLine.getCargoRiotFirmware()).append(" | ")
					.append("ExecuteCampaignRequest Riot Firmware Ver : " + latestMaint.getRiotFirmware()).append("   .   ");
			sb.append("baseLine Riot Hardware Ver : " + baseLine.getCargoRiotHardware()).append(" | ")
					.append("ExecuteCampaignRequest Riot Hardware Ver : " + latestMaint.getRiothardware()).append("   .   ");
			sb.append("baseLine Lite-Sentry App Ver : " + baseLine.getLiteSentryApp()).append(" | ")
					.append("ExecuteCampaignRequest Lite-Sentry App Ver : " + latestMaint.getLiteSentryApp())
					.append("   .   ");
			sb.append("baseLine Lite-Sentry Boot Ver : " + baseLine.getLiteSentryBoot()).append(" | ")
					.append("ExecuteCampaignRequest Lite-Sentry Boot Ver : " + latestMaint.getLiteSentryBoot())
					.append("   .   ");
			sb.append("baseLine Lite-Sentry Hardware Ver : " + baseLine.getLiteSentryHardware()).append(" | ")
					.append("ExecuteCampaignRequest Lite-Sentry Hardware Ver : " + latestMaint.getLiteSentryHw())
					.append("   .   ");
			sb.append("baseLine Micro SP MCU Ver : " + baseLine.getMicrospMcu()).append(" | ")
					.append("ExecuteCampaignRequest Micro SP MCU Ver : " + latestMaint.getSteMCU()).append("   .   ");
			sb.append("baseLine Micro SP App Ver : " + baseLine.getMicrospApp()).append(" | ")
					.append("ExecuteCampaignRequest Micro SP App Ver : " + latestMaint.getSteApp()).append("   .   ");
			sb.append("baseLine Device Type : " + baseLine.getDeviceType()).append(" | ")
					.append("ExecuteCampaignRequest  Device Type : " + latestMaint.getDevice_type()).append("   .   ");

			logger.info(sb.toString());
			analysisLog.debug(sb.toString());

			if (configFieldCheck(baseLine.getAppVersion(), latestMaint.getAPP_SW_VERSION(), msgUuid)
					&& configFieldCheck(baseLine.getBinVersion(), latestMaint.getBASEBAND_SW_VERSION() , msgUuid)
					&& configFieldCheck(baseLine.getBleVersion(), latestMaint.getBLE_VERSION() ,msgUuid)
					&& configFieldCheck(baseLine.getMcuVersion(), latestMaint.getEXTENDER_VERSION() ,msgUuid)
					&& configFieldCheck(baseLine.getDeviceType(), latestMaint.getDevice_type(),msgUuid)
					&& configFieldCheck(baseLine.getCargoMaxbotixFirmware(), latestMaint.getMaxbotixFirmware(),msgUuid)
					&& configFieldCheck(baseLine.getCargoMaxbotixHardware(), latestMaint.getMaxbotixhardware(),msgUuid)
					&& configFieldCheck(baseLine.getCargoRiotFirmware(), latestMaint.getRiotFirmware(),msgUuid)
					&& configFieldCheck(baseLine.getCargoRiotHardware(), latestMaint.getRiothardware(),msgUuid)
					&& configFieldCheck(baseLine.getMicrospApp(), latestMaint.getSteApp(),msgUuid)
					&& configFieldCheck(baseLine.getMicrospMcu(), latestMaint.getSteMCU(),msgUuid)
					&& configFieldCheck(baseLine.getLiteSentryApp(), latestMaint.getLiteSentryApp(),msgUuid)
					&& configFieldCheck(baseLine.getLiteSentryBoot(), latestMaint.getLiteSentryBoot(),msgUuid)
					&& configFieldCheck(baseLine.getLiteSentryHardware(), latestMaint.getLiteSentryHw(),msgUuid)
					&& waterfallFieldCheck(baseLine.getConfig1Crc(), latestMaint.getConfig1CRC(),msgUuid)
					&& waterfallFieldCheck(baseLine.getConfig2Crc(), latestMaint.getConfig2CRC(),msgUuid)
					&& waterfallFieldCheck(baseLine.getConfig3Crc(), latestMaint.getConfig3CRC(),msgUuid)
					&& waterfallFieldCheck(baseLine.getConfig4Crc(), latestMaint.getConfig4CRC(),msgUuid)
					&& waterfallFieldCheck(baseLine.getConfig2(), latestMaint.getConfig2CIV(),msgUuid)
					&& waterfallFieldCheck(baseLine.getConfig3(), latestMaint.getConfig3CIV(),msgUuid)
					&& waterfallFieldCheck(baseLine.getConfig4(), latestMaint.getConfig4CIV(),msgUuid)) {
				return DeviceStatusForCampaign.ELIGIBLE.getValue();
			}

		} else {
			logger.info("Message UUID (24) : " + msgUuid + ", latestMaint is null");
		}
		return "";
	}

	private DeviceReport removePrefixInMaintReport(DeviceReport latestMaint) {
		latestMaint.setBASEBAND_SW_VERSION(latestMaint.getBASEBAND_SW_VERSION().replaceAll("V", ""));
		latestMaint.setEXTENDER_VERSION(latestMaint.getEXTENDER_VERSION().replaceAll("V", ""));
		latestMaint.setBLE_VERSION(latestMaint.getBLE_VERSION().replaceAll("V", ""));
		return latestMaint;
	}

	public boolean validateDeviceUpgradeEligibleForCampaign(DeviceReport deviceReport) {
		SimpleDateFormat outSDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS");
		Date currentDate = new Date();
		if (deviceReport != null) {
			if (null != deviceReport.getIsDeviceUpgradeEligibleForCampaign()
					&& deviceReport.getIsDeviceUpgradeEligibleForCampaign().equalsIgnoreCase("true")) {
				return true;
			} else {
				if (null != deviceReport.getIsDeviceInstalledForCampaign()
						&& deviceReport.getIsDeviceInstalledForCampaign().equalsIgnoreCase("true")
						&& null != deviceReport.getDeviceInstalledForCampaignDT()) {
					java.util.Date deviceInstalledForCampaignDT = null;
					try {
						deviceInstalledForCampaignDT = outSDF.parse(deviceReport.getDeviceInstalledForCampaignDT());
					} catch (ParseException e) {
						logger.error(" deviceInstalledForCampaignDT  in invalid Format");
						return false;
					}
					if (deviceInstalledForCampaignDT.before(Date.from(
							currentDate.toInstant().minus(Constants.UPGRADE_ELIGIBLE_TIME_BUFFER, ChronoUnit.HOURS)))) {
						Map<String, String> map = new HashMap<>();
						map.put("isDeviceUpgradeEligibleForCampaign", "true");
						map.put("deviceUpgradeEligibleForCampaignDT", outSDF.format(currentDate));
						campaignDeviceHelper.updateDeviceReports(deviceReport, map);
						return true;
					}
				} else {
					return false;
				}
			}
		}
		return false;
	}

	public boolean validateDeviceUpgradeEligibleForCampaign(String deviceId) {
		List<String> deviceIds = new ArrayList<String>();
		deviceIds.add(deviceId);
		List<DeviceReport> msDeviceReports = campaignDeviceHelper.getDeviceReports(deviceIds);
		if (null != msDeviceReports && msDeviceReports.size() > 0) {
			return validateDeviceUpgradeEligibleForCampaign(msDeviceReports.get(0));
		} else {
			return false;
		}
	}
	
	public boolean isInstalledFlagTrue(String msgUuid, String imei) {

		logger.info("before calling restUtils class  msgUuid : " + msgUuid +". Device ID : "+ imei);

		//String installedFalg = iCampaignInstalledDeviceMsRepository.getInstalledFlagByDeviceImei(imei);
		
		MsDeviceRestResponse msDeviceRestResponse  = deviceVersionRepository.findDeviceByIMEI(imei);
		
		logger.info(" <<<<<<<<<<>>>>>>>>>>>>>>>>>  msgUuid : " + msgUuid +". installedFalg : "+ msDeviceRestResponse.getInstalledStatusFlag());

		
		//String installedFalg= restUtils.getCampaignInstalledFlag(msgUuid,imei);
		if(msDeviceRestResponse.getInstalledStatusFlag() != null && msDeviceRestResponse.getInstalledStatusFlag().equalsIgnoreCase("Y")) {
			logger.info("Message UUID (35.B) : " + msgUuid
					+ " , Campaign Validation succeded as Installed flag is Y . Device ID : "
					+ imei);

			analysisLog.debug("Message UUID (35.B) : " + msgUuid
					+ " , Campaign Validation succeded as Installed flag is Y . Device ID : "
					+ imei);
			return true;
		} 
		return false;
	}
	
	
	
	

	public List<Campaign> checkCampaignForDeviceId(String deviceId) {
		List<Campaign> activeCampaignsForImei = new ArrayList<>();
		List<Campaign> staticGroupingCampaigns = campaignRepo.findByIMEIForStaticGrouping(deviceId);
		activeCampaignsForImei.addAll(staticGroupingCampaigns);
		return activeCampaignsForImei;
	}

	
}
