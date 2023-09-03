package com.pct.device.version.util;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.persistence.Tuple;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pct.common.dto.DeviceDetailsDto;
import com.pct.common.dto.MsDeviceRestResponse;
import com.pct.common.dto.OrganisationsDTO;
import com.pct.common.model.Device;
import com.pct.common.model.DeviceDetails;
import com.pct.common.model.Organisation;
import com.pct.common.model.User;
import com.pct.device.service.device.DeviceReport;
import com.pct.device.version.constant.CampaignStatus;
import com.pct.device.version.constant.CampaignStepDeviceStatus;
import com.pct.device.version.constant.DeviceStatusForCampaign;
import com.pct.device.version.constant.StatsStatus;
import com.pct.device.version.dto.CampaignUpdateVersionDetailDTO;
import com.pct.device.version.dto.StepDTO;
import com.pct.device.version.dto.VersionMigrationDetailDTO;
import com.pct.device.version.exception.DeviceVersionException;
import com.pct.device.version.model.Campaign;
import com.pct.device.version.model.CampaignConfigProblem;
import com.pct.device.version.model.CampaignStatsPayloadList;
import com.pct.device.version.model.CampaignStep;
import com.pct.device.version.model.CampaignStepDeviceDetail;
import com.pct.device.version.model.DeviceCampaignStatus;
import com.pct.device.version.model.DeviceStatus;
import com.pct.device.version.model.Grouping;
import com.pct.device.version.model.LatestDeviceMaintenanceReport;
import com.pct.device.version.model.MultipleCampaignDevice;
import com.pct.device.version.model.Package;
import com.pct.device.version.payload.CampaignDeviceDetail;
import com.pct.device.version.payload.CampaignHyperLink;
import com.pct.device.version.payload.CampaignPayload;
import com.pct.device.version.payload.CampaignStatsPayload;
import com.pct.device.version.payload.CampaignSummary;
import com.pct.device.version.payload.DeviceCampaignHistory;
import com.pct.device.version.payload.DeviceStepStatus;
import com.pct.device.version.payload.ExecuteCampaignRequest;
import com.pct.device.version.payload.PackagePayload;
import com.pct.device.version.payload.SaveCampaignRequest;
import com.pct.device.version.payload.SavePackageRequest;
import com.pct.device.version.payload.UpdateCampaignPayload;
import com.pct.device.version.payload.VersionMigrationDetail;
import com.pct.device.version.payload.WaterfallInfo;
import com.pct.device.version.repository.ICampaignConfigProblemRepository;
import com.pct.device.version.repository.ICampaignStepDeviceDetailRepository;
import com.pct.device.version.repository.ICampaignStepRepository;
import com.pct.device.version.repository.IDeviceCampaignStatusRepository;
import com.pct.device.version.repository.IDeviceStatusRepository;
import com.pct.device.version.repository.IDeviceVersionRepository;
import com.pct.device.version.repository.ILatestDeviceMaintenanceReportRepository;
import com.pct.device.version.repository.IMultipleCampaignDeviceRepository;
import com.pct.device.version.repository.IPackageRepository;
//import com.pct.device.version.repository.msdevice.ICampaignInstalledDeviceMsRepository;
import com.pct.device.version.repository.projections.StepStatusForDeviceView;
import com.pct.device.version.service.impl.DeviceCampaignStatusServiceImpl;
import com.pct.device.version.validation.CampaignUtils;

import lombok.Data;

@Component
@Data
public class BeanConverter {

	Logger logger = LoggerFactory.getLogger(BeanConverter.class);
	Logger analysisLog = LoggerFactory.getLogger("analytics");

	@Autowired
	private IPackageRepository packageRepository;
	@Autowired
	private IMultipleCampaignDeviceRepository multiCampaignDeviceRepository;
	@Autowired
	private ICampaignStepRepository campaignStepRepository;
	@Autowired
	private RestUtils restUtils;
	@Autowired
	private CampaignUtils campaignUtils;
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
	private IDeviceCampaignStatusRepository deviceCampaignStatusRepository;
	@Autowired
	IDeviceStatusRepository elegibleCampaignStepDeviceDetailRepository;
	@Autowired
	AmazonSESUtil amazonSESUtil;
	@Autowired
	DeviceCampaignStatusServiceImpl deviceCampaignStatusServiceImpl;
	@Autowired
	private ICampaignStepDeviceDetailRepository deviceDetailRepository;
	@Autowired
	private IDeviceVersionRepository deviceVersionRepository;

	public static String mapToJson(Object object) throws JsonProcessingException {
		ObjectMapper objectMapper = new ObjectMapper();
		return objectMapper.writeValueAsString(object);

	}

	public Package packageRequestToPackage(SavePackageRequest savePackageRequest, Boolean isCreated, User user) {
		logger.info("Inside Bean Convertor packageRequestToPackage Method");
		Package inputPackage = null;
		
		
		LocalDateTime ldt = LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC);
		Instant now = ldt.toInstant(ZoneOffset.UTC);
	

		inputPackage = new Package();
		inputPackage.setPackageName(savePackageRequest.getPackageName());
		inputPackage.setAppVersion(savePackageRequest.getAppVersion());
		inputPackage.setBleVersion(savePackageRequest.getBleVersion());
		inputPackage.setMcuVersion(savePackageRequest.getMcuVersion());
		inputPackage.setBinVersion(savePackageRequest.getBinVersion());
		inputPackage.setConfig1(savePackageRequest.getConfig1());
		inputPackage.setConfig2(savePackageRequest.getConfig2());
		inputPackage.setConfig3(savePackageRequest.getConfig3());
		inputPackage.setConfig4(savePackageRequest.getConfig4());
		inputPackage.setConfig1Crc(savePackageRequest.getConfig1Crc());
		inputPackage.setConfig2Crc(savePackageRequest.getConfig2Crc());
		inputPackage.setConfig3Crc(savePackageRequest.getConfig3Crc());
		inputPackage.setConfig4Crc(savePackageRequest.getConfig4Crc());

		inputPackage.setIsDeleted(false);
		String packageUuid = "";
		boolean isPackageUuidUnique = false;
		while (!isPackageUuidUnique) {
			packageUuid = UUID.randomUUID().toString();
			Package byUuid = packageRepository.findByUuid(packageUuid);
			if (byUuid == null) {
				isPackageUuidUnique = true;
			}
		}
		if (isCreated) {
			inputPackage.setCreatedBy(user);
			inputPackage.setCreatedAt(now);
		}
		inputPackage.setUpdatedBy(user);
		inputPackage.setUuid(packageUuid);

		inputPackage.setDeviceType(savePackageRequest.getDeviceType());
		inputPackage.setLiteSentryApp(savePackageRequest.getLiteSentryApp());
		inputPackage.setLiteSentryBoot(savePackageRequest.getLiteSentryBoot());
		inputPackage.setLiteSentryHardware(savePackageRequest.getLiteSentryHardware());

		inputPackage.setMicrospMcu(savePackageRequest.getMicrospMcu());
		inputPackage.setMicrospApp(savePackageRequest.getMicrospApp());
		inputPackage.setCargoMaxbotixHardware(savePackageRequest.getCargoMaxbotixHardware());
		inputPackage.setCargoMaxbotixFirmware(savePackageRequest.getCargoMaxbotixFirmware());
		inputPackage.setCargoRiotHardware(savePackageRequest.getCargoRiotHardware());
		inputPackage.setCargoRiotFirmware(savePackageRequest.getCargoRiotFirmware());

		// packages.add(inputPackage);
		logger.info(inputPackage.toString());
		return inputPackage;
	}

	private void setDeviceCampaignStepDetails(Map<String, List<CampaignStepDeviceDetail>> imeiStepData,
			MsDeviceRestResponse device, int maxStepInCamp, List<DeviceStepStatus> deviceStepStatusList) {

		setDeviceCampaignStepDetails(imeiStepData, device.getImei(), maxStepInCamp, deviceStepStatusList);

	}

	private void setDeviceCampaignStepDetails(Map<String, List<CampaignStepDeviceDetail>> imeiStepData, String deviceId,
			int maxStepInCamp, List<DeviceStepStatus> deviceStepStatusList) {
		if (imeiStepData.containsKey(deviceId)) {
			List<CampaignStepDeviceDetail> data = imeiStepData.get(deviceId);
			for (int i = 0; i < maxStepInCamp; i++) {
				DeviceStepStatus deviceStepStatus = new DeviceStepStatus();
				deviceStepStatus.setStepOrderNumber(Long.valueOf(i) + 1);
				if (i < data.size()) {
					if (data.get(i).getStatus().equals(CampaignStepDeviceStatus.PENDING)) {
						deviceStepStatus.setStepStatus(StatsStatus.IN_PROGRESS.getValue());
					} else if (data.get(i).getStatus().equals(CampaignStepDeviceStatus.REMOVED)) {
						deviceStepStatus.setStepStatus(StatsStatus.REMOVED.getValue());
					} else {
						if (data.get(i).getStopExecutionTime() != null)
							deviceStepStatus.setStepStatus(data.get(i).getStopExecutionTime().toString());
					}
				} else {
					deviceStepStatus.setStepStatus(StatsStatus.PENDING.getValue());
				}
				deviceStepStatusList.add(deviceStepStatus);
			}
		} else {
			for (int i = 0; i < maxStepInCamp; i++) {
				DeviceStepStatus deviceStepStatus = new DeviceStepStatus();
				deviceStepStatus.setStepOrderNumber(Long.valueOf(i) + 1);
				deviceStepStatus.setStepStatus(StatsStatus.PENDING.getValue());
				deviceStepStatusList.add(deviceStepStatus);
			}
		}
	}

	public CampaignStatsPayloadList campaignToCampaignStatsResponse(Campaign campaignData,
			List<CampaignStep> allCampaignSteps, CampaignStatsPayloadList campaignStatsPayloadList, String msgUuid) {
		logger.info("Inside BeanConvertor Layer With  campaignToCampaignStatsResponse");
		Long maxStepInCamp = campaignStepRepository.findLastStepByCampaignUuid(campaignData.getUuid())
				.getStepOrderNumber();
		String allImei = null;
		CampaignDeviceDetail campaignDeviceDetail = null;
		DeviceStepStatus deviceStepStatus = null;
		Map<String, List<CampaignStepDeviceDetail>> imeiStepData = new HashMap<String, List<CampaignStepDeviceDetail>>();
		Map<String, List<MultipleCampaignDevice>> OnHoldForCampaignData = new HashMap<String, List<MultipleCampaignDevice>>();
		List<CampaignStepDeviceDetail> allStepDetail = deviceDetailRepository.findByCampaignUuid(campaignData.getUuid(),
				CampaignStepDeviceStatus.FAILED);

		if (allStepDetail != null) {
			imeiStepData = allStepDetail.stream().collect(Collectors.groupingBy(w -> w.getDeviceId()));
		}
		List<MsDeviceRestResponse> allDevices = new ArrayList<MsDeviceRestResponse>();
		if (campaignData.getGroup().getGroupingType().equals("Customer")) {
			//allDevices = restUtils.getDevicesFromMSByCustomerName(campaignData.getGroup().getGroupingName());
			 allDevices = deviceVersionRepository.findImeisByCustomerName(campaignData.getGroup().getGroupingName());

			/*
			 * if (allDevices != null) { imeiCount = Long.valueOf(allDevices.size()); }
			 */
		} else {
			allImei = campaignData.getGroup().getTargetValue();
			if (!StringUtils.isEmpty(allImei)) {
				
				//allDevices = restUtils.getSelectedDevicesFromMSByImeis(Arrays.asList(allImei.split(",")));
				allDevices = deviceVersionRepository.findAllDeviceByIMEIList(Arrays.asList(allImei.split(",")));

			}
		}

		List<MultipleCampaignDevice> onHoldOfCampaign = multiCampaignDeviceRepository
				.findByCampaignUuid(campaignData.getUuid());
		OnHoldForCampaignData = onHoldOfCampaign.stream().collect(Collectors.groupingBy(w -> w.getDeviceId()));

		long notEligibleCount = 0;
		long eligibleCount = 0;
		long problemCount = 0;
		List<String> allImeis = allDevices.stream().map(MsDeviceRestResponse::getImei).collect(Collectors.toList());
		// List<DeviceReport> msDeviceReports =
		// restUtils.getLastMaintReportFromMS(allImeis);
		List<DeviceReport> msDeviceReports = campaignDeviceHelper.getDeviceReports(allImeis);
		Map<String, DeviceReport> msDeviceReportsMap = new HashMap<>();

		if (msDeviceReports != null && msDeviceReports.size() > 0) {
			for (DeviceReport dev : msDeviceReports) {
				msDeviceReportsMap.put(dev.getDEVICE_ID(), dev);
			}
		}
		for (MsDeviceRestResponse device : allDevices) {
			List<DeviceStepStatus> deviceStepStatusList = new ArrayList<DeviceStepStatus>();
			campaignDeviceDetail = new CampaignDeviceDetail();
			campaignDeviceDetail.setImei(device.getImei());
			campaignDeviceDetail.setProductName(device.getProductName());
			// campaignDeviceDetail.setCustomerName(device.getOwnerLevel2());
			campaignDeviceDetail.setCustomerName(device.getOrganisationsDto().getOrganisationName());
			setDeviceCampaignStepDetails(imeiStepData, device, maxStepInCamp.intValue(), deviceStepStatusList);
//			List<String> allImeis = new ArrayList<String>(Arrays.asList(device.getImei()));

			gatewayEligibilityCalculation(device.getImei(), campaignDeviceDetail, campaignData, maxStepInCamp,
					allCampaignSteps, msDeviceReports, msgUuid);

			Map<String, List<DeviceReport>> deviceMaintReportMap = msDeviceReports.stream()
					.collect(Collectors.groupingBy(w -> w.getDEVICE_ID()));
			String basePackageUuid = allCampaignSteps.get(0).getFromPackage().getUuid();
			Package baseLine = packageRepository.findByUuid(basePackageUuid);
			String isEligible = campaignUtils.checkDeviceMatchesBaseline(device.getImei(), deviceMaintReportMap,
					baseLine, msgUuid);
			if (DeviceStatusForCampaign.ELIGIBLE.getValue().equalsIgnoreCase(isEligible)) {
				eligibleCount++;
			}
			if (DeviceStatusForCampaign.PROBLEM.getValue().equals(campaignDeviceDetail.getDeviceStatusForCampaign())) {
				problemCount++;

			}

			String firstStepStatusInThisCamp = deviceDetailRepository
					.findStatusByCampaignUuidAndStepOrderNumber(campaignData.getUuid(), device.getImei(), 1l);
			if (!isEligible.equalsIgnoreCase(DeviceStatusForCampaign.ELIGIBLE.getValue())
					&& (firstStepStatusInThisCamp == null)) {
				notEligibleCount++;
			}

			if (OnHoldForCampaignData.containsKey(device.getImei())) {
				deviceStepStatusList.get(
						(int) (OnHoldForCampaignData.get(device.getImei()).get(0).getCampaignStep().getStepOrderNumber()
								- 1))
						.setStepStatus(StatsStatus.ON_HOLD.getValue());
				// deviceStepStatus.setStepOrderNumber(OnHoldForCampaignData.get(device.getImei()).get(0).getCampaignStep().getStepOrderNumber());
			}

			campaignDeviceDetail.setDeviceStepStatus(deviceStepStatusList);
			campaignDeviceDetail.setDeviceReport(msDeviceReportsMap.get(device.getImei()));
		}

		long totalGateways = 0;
		long onHold = 0;
		long notStarted = 0;
		long inProgress = 0;
		long completed = 0;

//		notEligibleCount = StringUtils.isEmpty(campaignData.getGroup().getNotEligibleValue()) ? 0
//				: Arrays.asList(campaignData.getGroup().getNotEligibleValue().split(",")).size();
		totalGateways = allDevices == null ? 0 : allDevices.size();
		completed = deviceDetailRepository.getCountOfLastCampaignStepStatusAsSuccess(campaignData.getUuid(),
				maxStepInCamp, CampaignStepDeviceStatus.SUCCESS);

		onHold = multiCampaignDeviceRepository.findByCampaignUuid(campaignData.getUuid()).size();

		inProgress = deviceDetailRepository.getInProgressButNotHoldGatewayCount(campaignData.getUuid());// ,
		// CampaignStepDeviceStatus.PENDING);

		notStarted = totalGateways - (onHold + inProgress + completed + notEligibleCount);

		// campaignStatsResponse.setImeiCount(imeiCount);
		campaignStatsPayloadList.setEligible(eligibleCount);
		campaignStatsPayloadList.setOnHold(onHold);
		campaignStatsPayloadList.setInProgress(inProgress);
		campaignStatsPayloadList.setTotalGateways(totalGateways);
		campaignStatsPayloadList.setCompleted(completed);
		campaignStatsPayloadList.setNotEligible(notEligibleCount);
		campaignStatsPayloadList.setProblemCount(problemCount);

		logger.info(campaignStatsPayloadList.toString());
		return campaignStatsPayloadList;
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

	public Package updatePackageToPackage(PackagePayload packagePayload, Package packageData, User user) {
		LocalDateTime ldt = LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC);
		Instant now = ldt.toInstant(ZoneOffset.UTC);
	
		packageData.setPackageName(packagePayload.getPackageName());
		packageData.setAppVersion(packagePayload.getAppVersion());
		packageData.setBleVersion(packagePayload.getBleVersion());
		packageData.setMcuVersion(packagePayload.getMcuVersion());
		packageData.setBinVersion(packagePayload.getBinVersion());
		packageData.setConfig1(packagePayload.getConfig1());
		packageData.setConfig2(packagePayload.getConfig2());
		packageData.setConfig3(packagePayload.getConfig3());
		packageData.setConfig4(packagePayload.getConfig4());
		packageData.setConfig1Crc(packagePayload.getConfig1Crc());
		packageData.setConfig2Crc(packagePayload.getConfig2Crc());
		packageData.setConfig3Crc(packagePayload.getConfig3Crc());
		packageData.setConfig4Crc(packagePayload.getConfig4Crc());
		packageData.setIsDeleted(false);
		packageData.setUpdatedBy(user);
		packageData.setUpdatedAt(now);

		packageData.setDeviceType(packagePayload.getDeviceType());
		packageData.setLiteSentryApp(packagePayload.getLiteSentryApp());
		packageData.setLiteSentryBoot(packagePayload.getLiteSentryBoot());
		packageData.setLiteSentryHardware(packagePayload.getLiteSentryHardware());

		packageData.setMicrospMcu(packagePayload.getMicrospMcu());
		packageData.setMicrospApp(packagePayload.getMicrospApp());
		packageData.setCargoMaxbotixHardware(packagePayload.getCargoMaxbotixHardware());
		packageData.setCargoMaxbotixFirmware(packagePayload.getCargoMaxbotixFirmware());
		packageData.setCargoRiotHardware(packagePayload.getCargoRiotHardware());
		packageData.setCargoRiotFirmware(packagePayload.getCargoRiotFirmware());

		return packageData;
	}

	public Campaign campaignRequestToCampaign(Grouping grouping, SaveCampaignRequest saveCampaignRequest,
			Boolean isCreated, User user) {
		Campaign campaign = null;
		LocalDateTime ldt = LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC);
		Instant now = ldt.toInstant(ZoneOffset.UTC);
	
		campaign = new Campaign();
		String campaignUuid = "";
		boolean isCampaignUuidUnique = false;
		while (!isCampaignUuidUnique) {
			campaignUuid = UUID.randomUUID().toString();
			Package byUuid = packageRepository.findByUuid(campaignUuid);
			if (byUuid == null) {
				isCampaignUuidUnique = true;
			}
		}

		if (isCreated) {
			campaign.setCreatedBy(user);
			campaign.setCreatedAt(now);
		}
		campaign.setUpdatedBy(user);
		campaign.setIsDeleted(false);
		campaign.setIsActive(false);
		campaign.setUuid(campaignUuid);
		campaign.setGroup(grouping);
		campaign.setDescription(saveCampaignRequest.getDescription());
		campaign.setExcludeLowBattery(saveCampaignRequest.getExcludeLowBattery());
		campaign.setExcludeNotInstalled(saveCampaignRequest.getExcludeNotInstalled());
		campaign.setExcludeEngineering(saveCampaignRequest.getExcludeEngineering());
		campaign.setExcludeRma(saveCampaignRequest.getExcludeRma());
		campaign.setExcludeEol(saveCampaignRequest.getExcludeEol());
		campaign.setDeviceType(saveCampaignRequest.getDeviceType());
		campaign.setCampaignStatus(CampaignStatus.NOT_STARTED);
		campaign.setCustomerName(grouping.getGroupingName());
		campaign.setInitStatus("Inistialising");
		
		if (saveCampaignRequest.getCampaignName() == null
				|| saveCampaignRequest.getCampaignName().trim().equals(Constants.EMPTY_STRING)) {
			logger.error("Invalid Campaign Name");
			throw new DeviceVersionException("Invalid Campaign Name");
		}
		campaign.setCampaignName(saveCampaignRequest.getCampaignName());
		campaign.setPauseExecution(saveCampaignRequest.getIsPauseExecution());
		campaign.setPauseLimit(saveCampaignRequest.getNoOfImeis());
		campaign.setCampaignType(saveCampaignRequest.getCampaignType());
		return campaign;
	}

	public List<CampaignStep> campaignRequestToCampaignStep(SaveCampaignRequest saveCampaignRequest, Campaign savedCamp,
			Boolean isCreated, User user) {
		LocalDateTime ldt = LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC);
		Instant now = ldt.toInstant(ZoneOffset.UTC);
	
		CampaignStep campaignStep = null;
		Package from = null;
		Package to = null;
		List<CampaignStep> campaignStepList = new ArrayList<>();

		List<VersionMigrationDetail> vmList = saveCampaignRequest.getVersionMigrationDetail();

		for (int i = 0; i < vmList.size() - 1; i++) {
			VersionMigrationDetail versionMigrationDetail = vmList.get(i);
			if (versionMigrationDetail != null) {
				campaignStep = new CampaignStep();
				from = packageRepository.findByUuid(versionMigrationDetail.getPackageUuid());
				if (vmList.get(i + 1) != null) {
					to = packageRepository.findByUuid(vmList.get(i + 1).getPackageUuid());
				}
				if (from == null || to == null || StringUtils.isEmpty(versionMigrationDetail.getAtCommand())) {
					logger.error("Exception while saving Campaign step , insufficient data to create Campaign Step ");
					throw new DeviceVersionException("insufficient data to create Campaign Step");
				}
				campaignStep.setFromPackage(from);
				campaignStep.setToPackage(to);
				campaignStep.setAtCommand(versionMigrationDetail.getAtCommand());
				campaignStep.setStepOrderNumber(versionMigrationDetail.getStepOrderNumber());

				String campaignStepUuid = "";
				boolean isPackageUuidUnique = false;
				while (!isPackageUuidUnique) {
					campaignStepUuid = UUID.randomUUID().toString();
					CampaignStep byUuid = campaignStepRepository.findByUuid(campaignStepUuid);
					if (byUuid == null) {
						isPackageUuidUnique = true;
					}
				}

				if (isCreated) {
					campaignStep.setCreatedBy(user);
					campaignStep.setCreatedAt(now);
				}
				campaignStep.setUpdatedBy(user);
				campaignStep.setUuid(campaignStepUuid);
				campaignStep.setCampaign(savedCamp);
				campaignStepList.add(campaignStep);
			}
		}
		return campaignStepList;
	}

	public CampaignPayload campaignToCampaignResponse(Campaign campaignData, List<CampaignStep> allCampaignSteps) {
		CampaignPayload campaignResponse = null;
		VersionMigrationDetailDTO versionMigrationDetailDTO = null;
		// Long imeiCount = 0l;
		List<VersionMigrationDetailDTO> versionMigrationDetailList = new ArrayList<>();
		campaignResponse = new CampaignPayload();
		campaignResponse.setCampaignName(campaignData.getCampaignName());

		if (campaignData.getCampaignStatus().equals(CampaignStatus.IN_PROGRESS)) {
			campaignResponse.setCampaignStatus(
					campaignUtils.calculateCampaignCompletion(campaignData).toString() + Constants.COMPLETION_PATTERN);
		} else {
			campaignResponse.setCampaignStatus(campaignData.getCampaignStatus().getValue());
		}

		// campaignResponse.setGroup(campaignData.getGroup());

		/*
		 * 
		 * set imei group and count
		 */
		campaignResponse.setDescription(campaignData.getDescription());
		campaignResponse.setExcludeLowBattery(campaignData.getExcludeLowBattery());
		campaignResponse.setExcludeNotInstalled(campaignData.getExcludeNotInstalled());
		campaignResponse.setExcludeEngineering(campaignData.getExcludeEngineering());
		campaignResponse.setExcludeRma(campaignData.getExcludeRma());
		campaignResponse.setExcludeEol(campaignData.getExcludeEol());

		campaignResponse.setImeiGroup(campaignData.getGroup().getGroupingName());
		String allImei = null;

		if (campaignData.getGroup().getGroupingType().equals("Customer")) {
			campaignResponse.setIsImeiForCustomer(true);
			campaignResponse.setCustomerName(campaignData.getGroup().getGroupingName());
			/*
			 * List<Device> allDevices =
			 * restUtils.getDeviceDataFromMS(campaignData.getGroup().getGroupingName()); if
			 * (allDevices != null) { imeiCount = Long.valueOf(allDevices.size()); }
			 */
		} else {
			campaignResponse.setIsImeiForCustomer(false);
			campaignResponse.setImeiList(allImei);
			/*
			 * allImei = campaignData.getGroup().getTargetValue(); String[] arr = null; if
			 * (!StringUtils.isEmpty(allImei)) { arr = allImei.split(","); imeiCount =
			 * Long.valueOf(arr.length); }
			 */
		}
		// campaignResponse.setImeiCount(imeiCount);

		campaignResponse.setIsActive(campaignData.getIsActive());
		campaignResponse.setIsDeleted(campaignData.getIsDeleted());
		campaignResponse.setUuid(campaignData.getUuid());
		campaignResponse.setCreatedAt(campaignData.getCreatedAt());
		campaignResponse.setUpdatedAt(campaignData.getUpdatedAt());

		campaignResponse.setIsPauseExecution(campaignData.getPauseExecution());
		campaignResponse.setNoOfImeis(campaignData.getPauseLimit());
		if (campaignData.getCreatedBy() != null) {
			campaignResponse.setCreatedBy(
					campaignData.getCreatedBy().getFirstName() + " " + campaignData.getCreatedBy().getLastName());
		}
		if (campaignData.getUpdatedBy() != null) {
			campaignResponse.setUpdatedBy(
					campaignData.getUpdatedBy().getFirstName() + " " + campaignData.getUpdatedBy().getLastName());
		}
		if (allCampaignSteps != null) {
			for (CampaignStep campaignStep : allCampaignSteps) {
				versionMigrationDetailDTO = new VersionMigrationDetailDTO();
				versionMigrationDetailDTO.setFromPackage(packageToPackageResponse(campaignStep.getFromPackage()));
				versionMigrationDetailDTO.setToPackage(packageToPackageResponse(campaignStep.getToPackage()));
				versionMigrationDetailDTO.setStepOrderNumber(campaignStep.getStepOrderNumber());
				versionMigrationDetailDTO.setAtCommand(campaignStep.getAtCommand());
				versionMigrationDetailDTO.setStepId(campaignStep.getStepId());
				versionMigrationDetailList.add(versionMigrationDetailDTO);
			}
		}
		campaignResponse.setVersionMigrationDetail(versionMigrationDetailList);

		return campaignResponse;
	}

	private void checkFailedStepsForDevice(Campaign campaignData, String deviceId,
			CampaignDeviceDetail campaignDeviceDetail) {

		List<String> failedStepUuid = deviceDetailRepository.getFailedExecutedStep(campaignData.getUuid(), deviceId);
		if (failedStepUuid != null && failedStepUuid.size() > 0) {
			String stepStatus = deviceDetailRepository.getStatusOfStep(failedStepUuid.get(0), deviceId);
			if (stepStatus != null && stepStatus.equalsIgnoreCase(CampaignStepDeviceStatus.PENDING.getValue())) {
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

	private CampaignStatsPayload setCampaignData(CampaignStatsPayload campaignStatsResponse, Campaign campaignData) {

		campaignStatsResponse.setCampaignName(campaignData.getCampaignName());
		campaignStatsResponse.setCampaignStatus(campaignData.getCampaignStatus().getValue());
		//campaignStatsResponse.setCustomerName(campaignData.getCustomerName());
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

	private List<MsDeviceRestResponse> fetchIMIEForCampaign(CampaignStatsPayload campaignStatsResponse,
			Campaign campaignData, String msgUuid, String allImei) {

		List<MsDeviceRestResponse> allDevices = new ArrayList<MsDeviceRestResponse>();
		String groupingType = campaignData.getGroup().getGroupingType();
		String groupingName = campaignData.getGroup().getGroupingName();
		if (groupingType.equalsIgnoreCase("Customer")) {
			campaignStatsResponse.setIsImeiForCustomer(groupingType);
			campaignStatsResponse.setCustomerName(groupingName);

			logger.info(msgUuid + " pre restUtils.getDevicesFromMSByCustomerName 607");

			//allDevices = restUtils.getDevicesFromMSByCustomerName(groupingName);
			allDevices =  deviceVersionRepository.findImeisByCustomerName(campaignData.getGroup().getGroupingName());

			logger.info(msgUuid + " post restUtils.getDeviceDataFromMS 610");
			/*
			 * if (allDevices != null) { imeiCount = Long.valueOf(allDevices.size()); }
			 */
		} else if (groupingType.equalsIgnoreCase("ALL")) {
			campaignStatsResponse.setIsImeiForCustomer(groupingType);
			campaignStatsResponse.setCustomerName(groupingName);
			Page<MsDeviceRestResponse> devicesFromMs = restUtils.getDeviceDataFromMS(groupingName, 0, Integer.MAX_VALUE,
					"", "");
			allDevices = devicesFromMs.getContent();

		}

		else if (groupingType.equalsIgnoreCase("IMEI")) {
			campaignStatsResponse.setIsImeiForCustomer(groupingType);
			campaignStatsResponse.setCustomerName(groupingName);
			// campaignStatsResponse.setImeiList(allImei);
			allImei = campaignData.getGroup().getTargetValue();
			if (!StringUtils.isEmpty(allImei)) {
				logger.info(msgUuid + " pre restUtils.getSelectedDevicesFromMSByImeis 619");

				//allDevices = restUtils.getSelectedDevicesFromMSByImeis(Arrays.asList(allImei.split(",")));
				allDevices = deviceVersionRepository.findAllDeviceByIMEIList(Arrays.asList(allImei.split(",")));

				logger.info(msgUuid + " post restUtils.getSelectedDevicesFromMSByImeis 621");

			}
		}
		return allDevices;
	}

	public CampaignStatsPayload campaignToCampaignStatsResponse(Campaign campaignData,
			List<CampaignStep> allCampaignSteps, String msgUuid) {
		CampaignStatsPayload campaignStatsResponse = null;
		VersionMigrationDetailDTO versionMigrationDetailDTO = null;
		List<VersionMigrationDetailDTO> versionMigrationDetailList = new ArrayList<>();
		List<CampaignDeviceDetail> campaignDeviceDetailList = new ArrayList<>();

		logger.info(msgUuid + " . calling campaignStepRepository.findLastStepByCampaignUuid");
		Long maxStepInCamp = campaignStepRepository.findLastStepByCampaignUuid(campaignData.getUuid())
				.getStepOrderNumber();
		logger.info(msgUuid + " . fetched maxStepInCamp 571");
		campaignStatsResponse = new CampaignStatsPayload();

		campaignStatsResponse = setCampaignData(campaignStatsResponse, campaignData);

		String allImei = null;
		CampaignDeviceDetail campaignDeviceDetail = null;
		DeviceStepStatus deviceStepStatus = null;
		Map<String, List<CampaignStepDeviceDetail>> imeiStepData = new HashMap<String, List<CampaignStepDeviceDetail>>();
		Map<String, List<MultipleCampaignDevice>> OnHoldForCampaignData = new HashMap<String, List<MultipleCampaignDevice>>();

		logger.info(msgUuid + " pre deviceDetailRepository.findByCampaignUuid 591");

		List<CampaignStepDeviceDetail> allStepDetail = deviceDetailRepository.findByCampaignUuid(campaignData.getUuid(),
				CampaignStepDeviceStatus.FAILED);

		logger.info(msgUuid + "  post deviceDetailRepository.findByCampaignUuid 591");
		if (allStepDetail != null) {
			imeiStepData = allStepDetail.stream().collect(Collectors.groupingBy(w -> w.getDeviceId()));
		}
		logger.info(msgUuid + "  get imeiStepData 600");

		List<MsDeviceRestResponse> allDevices = fetchIMIEForCampaign(campaignStatsResponse, campaignData, msgUuid,
				allImei);

		logger.info(msgUuid + "  -- 624");
		List<MultipleCampaignDevice> onHoldOfCampaign = multiCampaignDeviceRepository
				.findByCampaignUuid(campaignData.getUuid());
		OnHoldForCampaignData = onHoldOfCampaign.stream().collect(Collectors.groupingBy(w -> w.getDeviceId()));
		logger.info(msgUuid + "  -- 628");
		long notEligibleCount = 0;
		List<String> allImeis = allDevices.stream().map(MsDeviceRestResponse::getImei).collect(Collectors.toList());
		// List<DeviceReport> msDeviceReports =
		// restUtils.getLastMaintReportFromMS(allImeis);
		logger.info(msgUuid + "  -- 633");
		List<DeviceReport> msDeviceReports = campaignDeviceHelper.getDeviceReports(allImeis);
		logger.info(msgUuid + "  -- 635");
		Map<String, String> msDeviceLastReportDate = campaignDeviceHelper.getDeviceLastReportDate(allImeis);
		Map<String, DeviceReport> msDeviceReportsMap = new HashMap<>();

		if (msDeviceReports != null && msDeviceReports.size() > 0) {
			for (DeviceReport dev : msDeviceReports) {
				msDeviceReportsMap.put(dev.getDEVICE_ID(), dev);
			}
		}
		logger.info(msgUuid + "  -- 637");
		String basePackageUuid = allCampaignSteps.get(0).getFromPackage().getUuid();
		Package baseLine = packageRepository.findByUuid(basePackageUuid);

		List<StepStatusForDeviceView> firstStepStatusInThisCampForAllDevices = new ArrayList<StepStatusForDeviceView>();
		List<StepStatusForDeviceView> lastStepStatusInThisCampForAllDevices = new ArrayList<StepStatusForDeviceView>();
		logger.info(msgUuid + "  -- 643");
		if (allImeis != null && !allImeis.isEmpty()) {
			firstStepStatusInThisCampForAllDevices = deviceDetailRepository
					.findAllStatusByCampaignUuidAndStepOrderNumber(campaignData.getUuid(), allImeis, 1l);
			lastStepStatusInThisCampForAllDevices = deviceDetailRepository
					.findAllStatusByCampaignUuidAndStepOrderNumber(campaignData.getUuid(), allImeis, maxStepInCamp);

		}
		logger.info(msgUuid + "  -- 651");
		Map<String, List<StepStatusForDeviceView>> firstStepStatusInThisCampMap = firstStepStatusInThisCampForAllDevices
				.stream().collect(Collectors.groupingBy(w -> w.getDevice_id()));
		logger.info(msgUuid + "  -- 654");
		Map<String, List<StepStatusForDeviceView>> lastStepStatusInThisCampMap = lastStepStatusInThisCampForAllDevices
				.stream().collect(Collectors.groupingBy(w -> w.getDevice_id()));
		logger.info(msgUuid + "  -- 657");

//////  NEW PERFORMSNCE CHANGES
		long baseLineMatch = 0;
		long offPath = 0;
		long onPath = 0;
		logger.info(msgUuid + "  -- 669");
		for (MsDeviceRestResponse device : allDevices) {
			String firstStepStatusInThisCamp = null;
			String lastStepStatusInThisCamp = null;
			List<StepStatusForDeviceView> firstStepStatusInThisCampList = firstStepStatusInThisCampMap
					.get(device.getImei());
			logger.info(msgUuid + "  -- 675");
			if (firstStepStatusInThisCampList != null && !firstStepStatusInThisCampList.isEmpty()) {
				firstStepStatusInThisCamp = firstStepStatusInThisCampList.get(0).getStatus();
			}
			logger.info(msgUuid + "  -- 679");
			List<StepStatusForDeviceView> lastStepStatusInThisCampList = lastStepStatusInThisCampMap
					.get(device.getImei());
			logger.info(msgUuid + "  -- 682");
			if (lastStepStatusInThisCampList != null && !lastStepStatusInThisCampList.isEmpty()) {
				lastStepStatusInThisCamp = lastStepStatusInThisCampList.get(0).getStatus();
			}
			logger.info(msgUuid + "  -- 686");
			List<DeviceStepStatus> deviceStepStatusList = new ArrayList<DeviceStepStatus>();
			campaignDeviceDetail = new CampaignDeviceDetail();
			if (device != null && device.getImei() != null) {
				campaignDeviceDetail.setImei(device.getImei());
			}
			campaignDeviceDetail.setProductName(device.getProductName());
			campaignDeviceDetail.setCustomerName(device.getOrganisationName());
			campaignDeviceDetail.setLastReport(msDeviceLastReportDate.get(device.getImei()));
			campaignDeviceDetail.setOrganisationName(device.getOrganisationName());

			checkFailedStepsForDevice(campaignData, device.getImei(), campaignDeviceDetail);
			logger.info(msgUuid + "  -- 719");
//			List<String> allImeis = new ArrayList<String>(Arrays.asList(device.getImei()));
			Map<String, List<DeviceReport>> deviceMaintReportMap = msDeviceReports.stream()
					.collect(Collectors.groupingBy(w -> w.getDEVICE_ID()));
			logger.info(msgUuid + "  -- 723");
			String isEligible = campaignUtils.checkDeviceMatchesBaseline(device.getImei(), deviceMaintReportMap,
					baseLine, msgUuid);
			logger.info(msgUuid + "  -- 726");
			gatewayEligibilityCalculationForAllDevices(device.getImei(), campaignDeviceDetail, campaignData,
					allCampaignSteps, msDeviceReports, isEligible, firstStepStatusInThisCamp, lastStepStatusInThisCamp,
					msDeviceReportsMap.get(device.getImei()));
			logger.info(msgUuid + "  -- 730");
			if (!isEligible.equalsIgnoreCase(DeviceStatusForCampaign.ELIGIBLE.getValue())
					&& (firstStepStatusInThisCamp == null)) {
				logger.info(msgUuid + "  -- 733");
				//// NEEDED changes
				notEligibleCount++;
				List<Campaign> campaignList = campaignUtils.checkCampaignForDeviceId(device.getImei(), null);
				logger.info(msgUuid + "  -- 737");
				// logger.info(" campaignList campaignList "+campaignList);
				if (campaignList != null && campaignList.size() <= 1) {

					offPath++;
					if (campaignDeviceDetail != null && campaignDeviceDetail.getDeviceStatusForCampaign()
							.equalsIgnoreCase(DeviceStatusForCampaign.NOT_ELIGIBLE.getValue())) {
						campaignDeviceDetail.setDeviceStatusForCampaign(DeviceStatusForCampaign.OFF_PATH.getValue());
					}
				} else {
					/*
					 * if (isDeviceStatusOnPath(campaignData, campaignList)) { onPath++;
					 * campaignDeviceDetail.setDeviceStatusForCampaign(DeviceStatusForCampaign.
					 * ON_PATH.getValue()); updateCommentsForOffPath(campaignData, campaignList,
					 * campaignDeviceDetail, device.getImei());
					 * 
					 * }
					 */

				}
			}
			logger.info(msgUuid + "  -- 758");
			if (OnHoldForCampaignData.containsKey(device.getImei())) {
				deviceStepStatusList.get(
						(int) (OnHoldForCampaignData.get(device.getImei()).get(0).getCampaignStep().getStepOrderNumber()
								- 1))
						.setStepStatus(StatsStatus.ON_HOLD.getValue());
				// deviceStepStatus.setStepOrderNumber(OnHoldForCampaignData.get(device.getImei()).get(0).getCampaignStep().getStepOrderNumber());
			}

			campaignDeviceDetail.setDeviceStepStatus(deviceStepStatusList);
			campaignDeviceDetail.setDeviceReport(msDeviceReportsMap.get(device.getImei()));
			// campaignDevice history data details
			// Removed due to performace issue
			// getDeviceCampaignHistory(device, campaignDeviceDetail, campaignData);
			// query to another table (CampaignConfigProblem)
			// update status and comments
			logger.info(msgUuid + "  -- 772");
			try {
				List<CampaignConfigProblem> ccp = new ArrayList<CampaignConfigProblem>();
				// ccp=
				// configProblemRepository.findByDeviceIdAndCampaignName(device.getImei(),campaignData.getCampaignName());
				ccp = configProblemRepository.findByDeviceIdAndCampaignId(device.getImei(),
						campaignData.getCampaignId());
				if (ccp != null && !ccp.isEmpty() && ccp.size() > 0) {
					campaignDeviceDetail.setDeviceStatusForCampaign(ccp.get(0).getDeviceStatusForCampaign());
					campaignDeviceDetail.setComments(ccp.get(0).getComments());
				}
			} catch (Exception e) {
				logger.info("Error in setting values for device with id: " + device.getImei() + " and campaign:  "
						+ campaignData.getCampaignName());
			}
			logger.info(msgUuid + "  -- 787");

			//CampaignInstalledDevice msDevices = iCampaignInstalledDeviceMsRepository
			//		.findCampaignInstalledDeviceByDeviceImei(device.getImei());

			// CampaignInstalledDevice msDevices =
			// restUtils.CampaignInstalledDeviceFromMS(device.getImei());
			if (device != null && device.getInstalledStatusFlag() !=null) {
				campaignDeviceDetail.setInstalledFlag(device.getInstalledStatusFlag());
			}
			campaignDeviceDetailList.add(campaignDeviceDetail);
			baseLineMatch += campaignUtils
					.validateDeviceUpgradeEligibleForCampaign(msDeviceReportsMap.get(device.getImei())) == true ? 1 : 0;
		}
		campaignStatsResponse.setCampaignDeviceDetail(campaignDeviceDetailList);

		long totalGateways = 0;
		long onHold = 0;
		long notStarted = 0;
		long inProgress = 0;
		long completed = 0;

		logger.info(msgUuid + "  -- 805");
//		notEligibleCount = StringUtils.isEmpty(campaignData.getGroup().getNotEligibleValue()) ? 0
//				: Arrays.asList(campaignData.getGroup().getNotEligibleValue().split(",")).size();
		totalGateways = allDevices == null ? 0 : allDevices.size();
		completed = deviceDetailRepository.getCountOfLastCampaignStepStatusAsSuccess(campaignData.getUuid(),
				maxStepInCamp, CampaignStepDeviceStatus.SUCCESS);
		logger.info(msgUuid + "  -- 811");
		onHold = multiCampaignDeviceRepository.findByCampaignUuid(campaignData.getUuid()).size();

		inProgress = deviceDetailRepository.getInProgressButNotHoldGatewayCount(campaignData.getUuid());// ,
																										// CampaignStepDeviceStatus.PENDING);
		logger.info(msgUuid + "  -- 816");
		/*
		 * List<Object> startedDevices =
		 * deviceDetailRepository.findCountOfExecutedGateways(campaignData.getUuid());
		 * long startedDeviceCount = startedDevices == null?0 :startedDevices.size();
		 * inProgress = startedDeviceCount - completed;
		 */

		notStarted = totalGateways - (onHold + inProgress + completed + notEligibleCount);

		campaignStatsResponse.setCampaignSummary(new CampaignSummary(totalGateways, notStarted, inProgress, completed,
				onHold, notEligibleCount, baseLineMatch, offPath, onPath));

		// campaignStatsResponse.setImeiCount(imeiCount);

		campaignStatsResponse.setIsActive(campaignData.getIsActive());
		campaignStatsResponse.setUuid(campaignData.getUuid());
		campaignStatsResponse.setCreatedAt(campaignData.getCreatedAt());
		campaignStatsResponse.setUpdatedAt(campaignData.getUpdatedAt());

		campaignStatsResponse.setIsPauseExecution(campaignData.getPauseExecution());
		campaignStatsResponse.setNoOfImeis(campaignData.getPauseLimit());
		if (campaignData.getCreatedBy() != null) {
			campaignStatsResponse.setCreatedBy(
					campaignData.getCreatedBy().getFirstName() + " " + campaignData.getCreatedBy().getLastName());
		}
		if (campaignData.getUpdatedBy() != null) {
			campaignStatsResponse.setUpdatedBy(
					campaignData.getUpdatedBy().getFirstName() + " " + campaignData.getUpdatedBy().getLastName());
		}
		if (allCampaignSteps != null) {
			for (CampaignStep campaignStep : allCampaignSteps) {
				versionMigrationDetailDTO = new VersionMigrationDetailDTO();
				versionMigrationDetailDTO.setFromPackage(packageToPackageResponse(campaignStep.getFromPackage()));
				versionMigrationDetailDTO.setToPackage(packageToPackageResponse(campaignStep.getToPackage()));
				versionMigrationDetailDTO.setStepOrderNumber(campaignStep.getStepOrderNumber());
				versionMigrationDetailDTO.setAtCommand(campaignStep.getAtCommand());
				versionMigrationDetailDTO.setStepId(campaignStep.getStepId());
				versionMigrationDetailList.add(versionMigrationDetailDTO);
			}
		}
		campaignStatsResponse.setVersionMigrationDetail(versionMigrationDetailList);
		logger.info(msgUuid + "  -- 859");
		return campaignStatsResponse;
	}

	public CampaignStatsPayload campaignToCampaignStatsResponseNew(Campaign campaignData,
			List<CampaignStep> allCampaignSteps, String msgUuid) {
		
		logger.info(msgUuid + " Inside campaignToCampaignStatsResponseNew ");
		CampaignStatsPayload campaignStatsResponse = new CampaignStatsPayload();
		List<VersionMigrationDetailDTO> versionMigrationDetailList = new ArrayList<>();	
		List<DeviceCampaignStatus> deviceCampaignStatusList = deviceCampaignStatusRepository
				.findByCampaignUUID(campaignData.getUuid());
		
		logger.info(msgUuid + " Inside campaignToCampaignStatsResponseNew device list "+ deviceCampaignStatusList.size());
		List<CampaignDeviceDetail> campaignDeviceDetailList = new ArrayList<>();
		Map<String, List<DeviceCampaignStatus>> deviceCampaignStatusListByRunningStatus = deviceCampaignStatusList
				.stream().collect(Collectors.groupingBy(w -> w.getRunningStatus()));
		logger.info(msgUuid + " Inside campaignToCampaignStatsResponseNew device list map created "+ deviceCampaignStatusList.size());
	
		List<String> allImeis = new ArrayList(deviceCampaignStatusListByRunningStatus.keySet());
		Map<String, List<CampaignStepDeviceDetail>> imeiStepData = new HashMap<String, List<CampaignStepDeviceDetail>>();
		Map<String, List<MultipleCampaignDevice>> OnHoldForCampaignData = new HashMap<String, List<MultipleCampaignDevice>>();
		List<CampaignStepDeviceDetail> allStepDetail = deviceDetailRepository.findByCampaignUuid(campaignData.getUuid(),
				CampaignStepDeviceStatus.FAILED);
		logger.info(msgUuid + " Inside campaignToCampaignStatsResponseNew got allStepDetail" +allStepDetail.size());
		if (allStepDetail != null) {
			imeiStepData = allStepDetail.stream().collect(Collectors.groupingBy(w -> w.getDeviceId()));
		}
		
	/*	campaignDeviceDetailList = deviceCampaignStatusListToCampaignDeviceDetailList(deviceCampaignStatusList,
				imeiStepData, allCampaignSteps.size());*/
		campaignStatsResponse.setCampaignDeviceDetail(campaignDeviceDetailList);
		
		String groupingType = campaignData.getGroup().getGroupingType();
		String groupingName = campaignData.getGroup().getGroupingName();
		campaignStatsResponse.setCustomerName(campaignData.getGroup().getGroupingName());
		campaignStatsResponse.setIsImeiForCustomer(groupingType);

		long totalGateways = 0;
		long onHold = 0;
		long notStarted = 0;
		long inProgress = 0;
		long completed = 0;
		long notEligibleCount = 0;
		long baseLineMatch = 0;
		long offPath = 0;
		long onPath = 0;


		totalGateways = deviceCampaignStatusList.size();

		if (deviceCampaignStatusListByRunningStatus != null) {
			if (deviceCampaignStatusListByRunningStatus.get(CampaignStepDeviceStatus.PENDING.getValue()) != null) {
				inProgress = deviceCampaignStatusListByRunningStatus.get(CampaignStepDeviceStatus.PENDING.getValue())
						.size();
			}
			if (deviceCampaignStatusListByRunningStatus.get(CampaignStepDeviceStatus.SUCCESS.getValue()) != null) {
				completed = deviceCampaignStatusListByRunningStatus.get(CampaignStepDeviceStatus.SUCCESS.getValue())
						.size();
			}

			if (deviceCampaignStatusListByRunningStatus.get(CampaignStepDeviceStatus.NOTSTARTED.getValue()) != null) {
				notStarted = deviceCampaignStatusListByRunningStatus.get(CampaignStepDeviceStatus.NOTSTARTED.getValue())
						.size();
			}
			if (deviceCampaignStatusListByRunningStatus.get(Constants.ON_PATH) != null) {
				offPath = deviceCampaignStatusListByRunningStatus.get(Constants.ON_PATH).size();
			}

			if (deviceCampaignStatusListByRunningStatus.get(DeviceStatusForCampaign.NOT_ELIGIBLE.getValue()) != null) {
				notEligibleCount = deviceCampaignStatusListByRunningStatus
						.get(DeviceStatusForCampaign.NOT_ELIGIBLE.getValue()).size();
			}

			if (deviceCampaignStatusListByRunningStatus.get(DeviceStatusForCampaign.ELIGIBLE.getValue()) != null) {
				 baseLineMatch =
				 deviceCampaignStatusListByRunningStatus.get(DeviceStatusForCampaign.ELIGIBLE.getValue()).size();
			}

		}
	
		int notEligibleCountInt = (int)notEligibleCount;
		List<StepDTO> stepDTO = getStepUuidDTO(campaignData.getUuid(), allCampaignSteps,
				deviceCampaignStatusList.size() - notEligibleCountInt);
		campaignStatsResponse.setStepDTO(stepDTO);
		logger.info(msgUuid + " Inside campaignToCampaignStatsResponseNew got allStepDetail" +stepDTO.size());
	
		campaignStatsResponse.setCampaignSummary(new CampaignSummary(totalGateways, notStarted, inProgress, completed,
				onHold, notEligibleCount, baseLineMatch, offPath, onPath));
		campaignStatsResponse = setCampaignData(campaignStatsResponse, campaignData);

		campaignStatsResponse.setIsActive(campaignData.getIsActive());
		campaignStatsResponse.setUuid(campaignData.getUuid());
		campaignStatsResponse.setCreatedAt(campaignData.getCreatedAt());
		campaignStatsResponse.setUpdatedAt(campaignData.getUpdatedAt());
		campaignStatsResponse.setExcludedImeis(campaignData.getGroup().getExcludedImei());
		campaignStatsResponse.setInitStatus(campaignData.getInitStatus());

		campaignStatsResponse.setIsPauseExecution(campaignData.getPauseExecution());
		campaignStatsResponse.setNoOfImeis(campaignData.getPauseLimit());
		if (campaignData.getCreatedBy() != null) {
			campaignStatsResponse.setCreatedBy(
					campaignData.getCreatedBy().getFirstName() + " " + campaignData.getCreatedBy().getLastName());
		}
		if (campaignData.getUpdatedBy() != null) {
			campaignStatsResponse.setUpdatedBy(
					campaignData.getUpdatedBy().getFirstName() + " " + campaignData.getUpdatedBy().getLastName());
		}
		if (allCampaignSteps != null) {
			for (CampaignStep campaignStep : allCampaignSteps) {
				VersionMigrationDetailDTO versionMigrationDetailDTO = new VersionMigrationDetailDTO();
				versionMigrationDetailDTO.setFromPackage(packageToPackageResponse(campaignStep.getFromPackage()));
				versionMigrationDetailDTO.setToPackage(packageToPackageResponse(campaignStep.getToPackage()));
				versionMigrationDetailDTO.setStepOrderNumber(campaignStep.getStepOrderNumber());
				versionMigrationDetailDTO.setAtCommand(campaignStep.getAtCommand());
				versionMigrationDetailDTO.setStepId(campaignStep.getStepId());
				versionMigrationDetailList.add(versionMigrationDetailDTO);
			}
		}
		campaignStatsResponse.setVersionMigrationDetail(versionMigrationDetailList);
		logger.info(msgUuid + "  Response Returned");
		return campaignStatsResponse;

	}

	public Campaign updateCampaignPayloadToCampaign(Grouping grouping, UpdateCampaignPayload campaignToUpdate,
			Campaign campaignData, User user) {
		LocalDateTime ldt = LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC);
		Instant now = ldt.toInstant(ZoneOffset.UTC);
	
		if (campaignToUpdate.getCampaignName() == null
				|| campaignToUpdate.getCampaignName().trim().equals(Constants.EMPTY_STRING)) {
			logger.error("Invalid Campaign Name");
			throw new DeviceVersionException("Invalid Campaign Name");
		}
		campaignData.setCampaignName(campaignToUpdate.getCampaignName());
		campaignData.setDescription(campaignToUpdate.getDescription());

		campaignData.setExcludeLowBattery(campaignToUpdate.getExcludeLowBattery());
		campaignData.setExcludeNotInstalled(campaignToUpdate.getExcludeNotInstalled());

		campaignData.setExcludeEngineering(campaignToUpdate.getExcludeEngineering());
		campaignData.setExcludeRma(campaignToUpdate.getExcludeRma());
		campaignData.setExcludeEol(campaignToUpdate.getExcludeEol());

		campaignData.setIsActive(campaignToUpdate.getIsActive());
		campaignData.setCampaignStatus(CampaignStatus.getCampaignStatus(campaignToUpdate.getCampaignStatus()));
		campaignData.setDeviceType(campaignToUpdate.getDeviceType());
		campaignData.setUpdatedAt(now);
		campaignData.setUpdatedBy(user);
		campaignData.setGroup(grouping);
		campaignData.setPauseExecution(campaignToUpdate.getIsPauseExecution());
		campaignData.setPauseLimit(campaignToUpdate.getNoOfImeis());
		campaignData.setInitStatus("Inistialising");
		return campaignData;
	}

	public List<List<CampaignStep>> updateCampaignPayloadToCampaignStep(UpdateCampaignPayload campaignToUpdate,
			Campaign updatedCampaign, User user) {
		LocalDateTime ldt = LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC);
		Instant now = ldt.toInstant(ZoneOffset.UTC);
	
		// TODO Auto-generated method stub
		List<CampaignStep> campaignStepList;
		List<CampaignStep> stepListToUpdate = new ArrayList<>();
		List<List<CampaignStep>> finalListReturn = new ArrayList<List<CampaignStep>>(2);
		CampaignStep campaignStep = null;

		campaignStepList = campaignStepRepository.findByCampaignUuid(updatedCampaign.getUuid());

		List<CampaignUpdateVersionDetailDTO> vmList = campaignToUpdate.getVersionMigrationDetail();

		if (vmList.size() - 1 > campaignStepList.size()) {
			// vmList
		}

		int dbStepSize = campaignStepList.size();
		int i = 0;
		for (; i < vmList.size() - 1; i++) {

			if (dbStepSize >= i + 1) {

				campaignStep = campaignStepList.get(0);
				campaignStep.setCampaign(updatedCampaign);
				// campaignStep.setFromPackage();
				Package from = null;
				Package to = null;
				from = packageRepository.findByUuid(vmList.get(i).getPackageUuid());
				if (vmList.get(i + 1) != null) {
					to = packageRepository.findByUuid(vmList.get(i + 1).getPackageUuid());
				}
				if (from == null || to == null || StringUtils.isEmpty(vmList.get(i).getAtCommand())) {
					logger.error("Exception while saving Campaign step , insufficient data to create Campaign Step ");
					throw new DeviceVersionException("insufficient data to create Campaign Step");
				}
				campaignStep.setFromPackage(from);
				campaignStep.setToPackage(to);
				campaignStep.setAtCommand(vmList.get(i).getAtCommand());
				campaignStep.setStepOrderNumber(vmList.get(i).getStepOrderNumber());
				campaignStep.setUpdatedAt(now);
				campaignStep.setUpdatedBy(user);
				stepListToUpdate.add(campaignStep);
				campaignStepList.remove(0);
			} else {

				UpdatePayloadCSToCampStep(vmList.get(i), vmList.get(i + 1), stepListToUpdate, updatedCampaign, user);
				// ad new steps
			}

		}
		finalListReturn.add(campaignStepList);
		finalListReturn.add(stepListToUpdate);
		return finalListReturn;
	}

	private void UpdatePayloadCSToCampStep(CampaignUpdateVersionDetailDTO versionMigrationDetail,
			CampaignUpdateVersionDetailDTO next, List<CampaignStep> finalList, Campaign updatedCampaign, User user) {
		Package from = null;
		Package to = null;
		if (versionMigrationDetail != null) {
			CampaignStep campaignStep = new CampaignStep();
			from = packageRepository.findByUuid(versionMigrationDetail.getPackageUuid());
			if (next != null) {
				to = packageRepository.findByUuid(next.getPackageUuid());
			}
			if (from == null || to == null || StringUtils.isEmpty(versionMigrationDetail.getAtCommand())) {
				logger.error("Exception while saving Campaign step , insufficient data to create Campaign Step ");
				throw new DeviceVersionException("insufficient data to create Campaign Step");
			}
			campaignStep.setFromPackage(from);
			campaignStep.setToPackage(to);
			campaignStep.setAtCommand(versionMigrationDetail.getAtCommand());
			campaignStep.setStepOrderNumber(versionMigrationDetail.getStepOrderNumber());

			String campaignStepUuid = "";
			boolean isPackageUuidUnique = false;
			while (!isPackageUuidUnique) {
				campaignStepUuid = UUID.randomUUID().toString();
				CampaignStep byUuid = campaignStepRepository.findByUuid(campaignStepUuid);
				if (byUuid == null) {
					isPackageUuidUnique = true;
				}
			}
			campaignStep.setUpdatedBy(user);
			campaignStep.setUuid(campaignStepUuid);
			campaignStep.setCampaign(updatedCampaign);
			finalList.add(campaignStep);

		}
	}

	public ExecuteCampaignRequest parseWaterfallData(ExecuteCampaignRequest executeCampaignRequest, String msgUuid) {
		// TODO Auto-generated method stub
		try {
			WaterfallInfo data = executeCampaignRequest.getWaterfallData();
			if (data == null) {
				return executeCampaignRequest;
			}
			for (int i = 0; i < data.getNumberOfFiles(); i++) {
				int index = data.getConfigId().get(i);
				switch (index) {
				case 1:
					executeCampaignRequest.setConfig1CIV(data.getConfigIdentificationVersion().get(i));
					executeCampaignRequest.setConfig1CRC(data.getConfigCRC().get(i));
					break;
				case 2:
					executeCampaignRequest.setConfig2CIV(data.getConfigIdentificationVersion().get(i));
					executeCampaignRequest.setConfig2CRC(data.getConfigCRC().get(i));

					break;
				case 3:
					executeCampaignRequest.setConfig3CIV(data.getConfigIdentificationVersion().get(i));
					executeCampaignRequest.setConfig3CRC(data.getConfigCRC().get(i));

					break;
				case 4:
					executeCampaignRequest.setConfig4CIV(data.getConfigIdentificationVersion().get(i));
					executeCampaignRequest.setConfig4CRC(data.getConfigCRC().get(i));

					break;
				case 5:
					executeCampaignRequest.setConfig5CIV(data.getConfigIdentificationVersion().get(i));
					executeCampaignRequest.setConfig5CRC(data.getConfigCRC().get(i));

					break;
				default:
					break;
				}
			}
		} catch (Exception e) {
			logger.error("Message UUID : " + msgUuid + " error in parsing Waterfall tlv for {}",
					executeCampaignRequest.getDeviceId());
			analysisLog.debug("Message UUID : " + msgUuid + " error in parsing Waterfall tlv for {}",
					executeCampaignRequest.getDeviceId());
			throw new DeviceVersionException("error in parsing Waterfall tlv");
		}

		return executeCampaignRequest;
	}

	void gatewayEligibilityCalculation(String deviceId, CampaignDeviceDetail campaignDeviceDetail,
			Campaign campaignData, Long maxStepInCamp, List<CampaignStep> allCampaignSteps,
			List<DeviceReport> msDeviceReports, String msgUuid) {

		List<CampaignHyperLink> link = new ArrayList<CampaignHyperLink>();
		boolean startedInAnyOtherCamp = false;
		String multiCamp = "";
		if (allCampaignSteps == null) {
			logger.error("error in getting campaign step for {}", campaignData.getUuid());
			throw new DeviceVersionException("error in getting campaign step ");
		}
		try {
			String basePackageUuid = allCampaignSteps.get(0).getFromPackage().getUuid();
			List<Campaign> activeCampaignsForImei = campaignUtils.checkCampaignForDeviceId(deviceId, "");

			// just check with base line Eli or NE
			if (msDeviceReports == null || msDeviceReports.size() == 0) {
				logger.info("Gateway has not reported recently ", campaignData.getUuid());
				campaignDeviceDetail.setDeviceStatusForCampaign(DeviceStatusForCampaign.UNKNOWN.getValue());
				campaignDeviceDetail.setComments(Constants.NOT_REPORTED_RECENTLY);
			} else {
				Map<String, List<DeviceReport>> deviceMaintReportMap = msDeviceReports.stream()
						.collect(Collectors.groupingBy(w -> w.getDEVICE_ID()));
				Package baseLine = packageRepository.findByUuid(basePackageUuid);
				String isEligible = campaignUtils.checkDeviceMatchesBaseline(deviceId, deviceMaintReportMap, baseLine,
						msgUuid);

				String firstStepStatusInThisCamp = deviceDetailRepository
						.findStatusByCampaignUuidAndStepOrderNumber(campaignData.getUuid(), deviceId, 1l);
				String lastStepStatusInThisCamp = deviceDetailRepository
						.findStatusByCampaignUuidAndStepOrderNumber(campaignData.getUuid(), deviceId, maxStepInCamp);

				if (activeCampaignsForImei.size() == 1) {
					if (lastStepStatusInThisCamp != null
							&& lastStepStatusInThisCamp.equalsIgnoreCase(CampaignStepDeviceStatus.SUCCESS.getValue())) {
						campaignDeviceDetail.setDeviceStatusForCampaign(DeviceStatusForCampaign.COMPLETED.getValue());
					} else {
						if (isEligible.equalsIgnoreCase(DeviceStatusForCampaign.ELIGIBLE.getValue())
								|| !StringUtils.isEmpty(firstStepStatusInThisCamp)
										&& (lastStepStatusInThisCamp == null || !lastStepStatusInThisCamp
												.equalsIgnoreCase(CampaignStepDeviceStatus.SUCCESS.getValue()))) {

							campaignDeviceDetail
									.setDeviceStatusForCampaign(DeviceStatusForCampaign.ELIGIBLE.getValue());
							List<String> failedStepUuid = deviceDetailRepository
									.getFailedExecutedStep(campaignData.getUuid(), deviceId);
							if (failedStepUuid != null && failedStepUuid.size() > 0) {
								String stepStatus = deviceDetailRepository.getStatusOfStep(failedStepUuid.get(0),
										deviceId);
								if (stepStatus.equalsIgnoreCase(CampaignStepDeviceStatus.PENDING.getValue())) {
									CampaignStep step = campaignStepRepository.findByUuid(failedStepUuid.get(0));
									campaignDeviceDetail.setComments(step.getFromPackage().getPackageName() + " to "
											+ step.getToPackage().getPackageName() + " "
											+ Constants.GATEWAY_PROBLEM_STATUS);
									campaignDeviceDetail
											.setDeviceStatusForCampaign(DeviceStatusForCampaign.PROBLEM.getValue());
								}

								Long reAttempt = stepDeviceDetailRepository
										.findByCampaignUuidAndStepUuidAndFailedStatus(deviceId, failedStepUuid.get(0));
								if (reAttempt > Constants.MAX_FAILED_ATTEMPT_FOR_UPDATE) {
									CampaignStep step = campaignStepRepository.findByUuid(failedStepUuid.get(0));
									campaignDeviceDetail.setComments("Step-" + step.getStepOrderNumber()
											+ " could not be executed after multiple attempts.");
									campaignDeviceDetail
											.setDeviceStatusForCampaign(DeviceStatusForCampaign.PROBLEM.getValue());

								}
							}
						} else {
							campaignDeviceDetail
									.setDeviceStatusForCampaign(DeviceStatusForCampaign.NOT_ELIGIBLE.getValue());
							campaignDeviceDetail.setComments(Constants.NOT_ELIGIBLE_FOR_BASELINE);
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

							for (Campaign campaign : activeCampaignsForImei) {
								if (!campaign.getUuid().equals(campaignData.getUuid())) {
									link.add(new CampaignHyperLink(campaign.getCampaignName(), campaign.getUuid()));
									multiCamp += campaign.getCampaignName();
									String firstStepStatus = deviceDetailRepository
											.findStatusByCampaignUuidAndStepOrderNumber(campaign.getUuid(), deviceId,
													1l);
									if (!StringUtils.isEmpty(firstStepStatus)) {
										startedInAnyOtherCamp = true;
									}

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
							if (failedStepUuid != null && failedStepUuid.size() > 0) {
								String stepStatus = deviceDetailRepository.getStatusOfStep(failedStepUuid.get(0),
										deviceId);
								if (stepStatus.equalsIgnoreCase(CampaignStepDeviceStatus.PENDING.getValue())) {
									CampaignStep step = campaignStepRepository.findByUuid(failedStepUuid.get(0));
									campaignDeviceDetail.setComments(step.getFromPackage().getPackageName() + " to "
											+ step.getToPackage().getPackageName() + " "
											+ Constants.GATEWAY_PROBLEM_STATUS);
									campaignDeviceDetail
											.setDeviceStatusForCampaign(DeviceStatusForCampaign.PROBLEM.getValue());
								}
								Long reAttempt = stepDeviceDetailRepository
										.findByCampaignUuidAndStepUuidAndFailedStatus(deviceId, failedStepUuid.get(0));
								if (reAttempt > Constants.MAX_FAILED_ATTEMPT_FOR_UPDATE) {
									CampaignStep step = campaignStepRepository.findByUuid(failedStepUuid.get(0));
									campaignDeviceDetail.setComments("Step-" + step.getStepOrderNumber()
											+ " could not be executed after multiple attempts.");
									campaignDeviceDetail
											.setDeviceStatusForCampaign(DeviceStatusForCampaign.PROBLEM.getValue());

								}
							}
						} else {
							campaignDeviceDetail
									.setDeviceStatusForCampaign(DeviceStatusForCampaign.NOT_ELIGIBLE.getValue());
							campaignDeviceDetail.setComments(Constants.NOT_ELIGIBLE_FOR_BASELINE);

							for (Campaign campaign : activeCampaignsForImei) {
								if (!campaign.getUuid().equals(campaignData.getUuid())) {
									String firstStepStatus = deviceDetailRepository
											.findStatusByCampaignUuidAndStepOrderNumber(campaign.getUuid(), deviceId,
													1l);
									Long maxStep = campaignStepRepository.findLastStepByCampaignUuid(campaign.getUuid())
											.getStepOrderNumber();
									String lastStepStatus = deviceDetailRepository
											.findStatusByCampaignUuidAndStepOrderNumber(campaign.getUuid(), deviceId,
													maxStep);

									if (!StringUtils.isEmpty(firstStepStatus)
											&& (lastStepStatus == null || !lastStepStatus
													.equalsIgnoreCase(CampaignStepDeviceStatus.SUCCESS.getValue()))) {
										campaignDeviceDetail.setComments(
												Constants.CONFLICT_NOT_ELIGIBLE + " " + campaign.getCampaignName());
										link.add(new CampaignHyperLink(campaign.getCampaignName(), campaign.getUuid()));
										campaignDeviceDetail.setCampaignHyperLink(link);
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

	public CampaignStatsPayloadList campaignToCampaignStatsResponseScheduler(Campaign campaignData,
			List<CampaignStep> allCampaignSteps, CampaignStatsPayloadList campaignStatsPayloadList, String msgUuid) {
		Long maxStepInCamp = campaignStepRepository.findLastStepByCampaignUuid(campaignData.getUuid())
				.getStepOrderNumber();
		String allImei = null;
		Map<String, List<CampaignStepDeviceDetail>> imeiStepData = new HashMap<String, List<CampaignStepDeviceDetail>>();
		List<CampaignStepDeviceDetail> allStepDetail = deviceDetailRepository.findByCampaignUuid(campaignData.getUuid(),
				CampaignStepDeviceStatus.FAILED);
		if (allStepDetail != null) {
			imeiStepData = allStepDetail.stream().collect(Collectors.groupingBy(w -> w.getDeviceId()));
		}
		List<String> allDevices = new ArrayList<String>();
		try {
			if (campaignData.getGroup().getGroupingType().equals("Customer")) {
				//allDevices = restUtils.getDevicesFromMSByCustomerName(campaignData.getGroup().getGroupingName())
				//		.stream().map(MsDeviceRestResponse::getImei).collect(Collectors.toList());
				
				allDevices =  deviceVersionRepository.findImeisByCustomerName(campaignData.getGroup().getGroupingName())
						  .stream().map(MsDeviceRestResponse::getImei).collect(Collectors.toList());

				;
			} else {
				allImei = campaignData.getGroup().getTargetValue();
				if (!StringUtils.isEmpty(allImei)) {
					// allDevices =
					// restUtils.getSelectedDeviceDataFromMS(Arrays.asList(allImei.split(",")));
					allDevices = Arrays.asList(allImei.split(","));
				}
			}
		} catch (Exception e) {
			// logger.info("problllllll camp "+campaignData.getCampaignName() + " campin
			// gropu "+campaignData.getGroup());
			// TODO: handle exception
		}

		long notEligibleCount = 0;
		long eligibleCount = 0;
		long problemCount = 0;
		long offPath = 0;
		long onPath = 0;
		long notInstalled = 0;
		List<DeviceReport> msDeviceReports = campaignDeviceHelper.getDeviceReportsForScheduler(allDevices);
		Map<String, List<DeviceReport>> deviceMaintReportMap = msDeviceReports.stream()
				.collect(Collectors.groupingBy(w -> w.getDEVICE_ID()));
		for (String device : allDevices) {
			String basePackageUuid = allCampaignSteps.get(0).getFromPackage().getUuid();
			Package baseLine = packageRepository.findByUuid(basePackageUuid);
			String isEligible = campaignUtils.checkDeviceMatchesBaselineScheduler(device, deviceMaintReportMap, baseLine,
					msgUuid);
			if (DeviceStatusForCampaign.ELIGIBLE.getValue().equalsIgnoreCase(isEligible)) {
				eligibleCount++;
			}

			String firstStepStatusInThisCamp = deviceDetailRepository
					.findStatusByCampaignUuidAndStepOrderNumber(campaignData.getUuid(), device, 1l);
			if (!isEligible.equalsIgnoreCase(DeviceStatusForCampaign.ELIGIBLE.getValue())
					&& (firstStepStatusInThisCamp == null)) {
				notEligibleCount++;
				List<Campaign> campaignList = campaignUtils.checkCampaignForDeviceId(device, null);

				if (campaignList != null && campaignList.size() <= 1) {
					offPath++;
				} else {
					/*
					 * if (isDeviceStatusOnPath(campaignData, campaignList)) { onPath++; }
					 */

				}
			}

			List<String> failedStepUuid = deviceDetailRepository.getFailedExecutedStep(campaignData.getUuid(), device);
			if (failedStepUuid != null && failedStepUuid.size() > 0) {
				String stepStatus = deviceDetailRepository.getStatusOfStep(failedStepUuid.get(0), device);
				if (stepStatus != null && stepStatus.equalsIgnoreCase(CampaignStepDeviceStatus.PENDING.getValue())) {
//					CampaignStep step = campaignStepRepository.findByUuid(failedStepUuid.get(0));
//					problemCount ++;

					Long reAttempt = stepDeviceDetailRepository.findByCampaignUuidAndStepUuidAndFailedStatus(device,
							failedStepUuid.get(0));
					if (reAttempt > Constants.MAX_FAILED_ATTEMPT_FOR_UPDATE) {
						problemCount++;
					}
				}
			}

		}

		long totalGateways = 0;
		long onHold = 0;
		long notStarted = 0;
		long inProgress = 0;
		long completed = 0;

		// notEligibleCount =
		// StringUtils.isEmpty(campaignData.getGroup().getNotEligibleValue()) ? 0
		// :
		// Arrays.asList(campaignData.getGroup().getNotEligibleValue().split(",")).size();
		totalGateways = allDevices == null ? 0 : allDevices.size();
		completed = deviceDetailRepository.getCountOfLastCampaignStepStatusAsSuccess(campaignData.getUuid(),
				maxStepInCamp, CampaignStepDeviceStatus.SUCCESS);

		inProgress = deviceDetailRepository.getInProgressButNotHoldGatewayCount(campaignData.getUuid());// ,
		// CampaignStepDeviceStatus.PENDING);

		notStarted = totalGateways - (onHold + inProgress + completed + notEligibleCount);

		campaignStatsPayloadList.setEligible(eligibleCount);
		campaignStatsPayloadList.setInProgress(inProgress);
		campaignStatsPayloadList.setTotalGateways(totalGateways);
		campaignStatsPayloadList.setCompleted(completed);
		campaignStatsPayloadList.setNotEligible(notEligibleCount);
		campaignStatsPayloadList.setProblemCount(problemCount);
		campaignStatsPayloadList.setNotStarted(notStarted);
		campaignStatsPayloadList.setOffPath(offPath);
		campaignStatsPayloadList.setOnPath(onPath);
		return campaignStatsPayloadList;
	}

	// get by uuid performance changes

	void getDeviceCampaignHistory(Device device, CampaignDeviceDetail campaignDeviceDetail, Campaign campaignData) {
		List<DeviceCampaignHistory> link = new ArrayList<DeviceCampaignHistory>();
		DeviceCampaignHistory deviceCampaignHistory = null;
		String campaignStepDeviceDetailStatus = "";
		try {
//			List<Campaign> activeCampaignsForImei = campaignUtils.checkCampaignForDeviceId(device.getImei(),
//					device.getOwnerLevel2());
			List<Campaign> activeCampaignsForImei = campaignUtils.checkCampaignForDeviceId(device.getImei(),
					device.getOrganisation().getOrganisationName());
			for (Campaign campaign : activeCampaignsForImei) {
				try {
					deviceCampaignHistory = new DeviceCampaignHistory();
					deviceCampaignHistory.setCampaingName(campaign.getCampaignName());
					deviceCampaignHistory.setCampaingStatus(campaign.getCampaignStatus().getValue());
					CampaignStepDeviceDetail campaignStepDeviceDetail = deviceDetailRepository
							.getDeviceCampaignLastExecuteStep(campaign.getUuid(), device.getImei());
					if (campaignStepDeviceDetail != null) {
						campaignStepDeviceDetailStatus = campaignStepDeviceDetail.getStatus().getValue();
						if (campaignStepDeviceDetailStatus
								.equalsIgnoreCase(CampaignStepDeviceStatus.PENDING.getValue())  && campaignStepDeviceDetail.getStartExecutionTime() != null) {
							deviceCampaignHistory
									.setLastStepTime(campaignStepDeviceDetail.getStartExecutionTime().toString());
						} else {
							if(campaignStepDeviceDetail.getStopExecutionTime() != null)
							{
								deviceCampaignHistory.setLastStepTime(campaignStepDeviceDetail.getStopExecutionTime().toString());
							}
						}
						CampaignStep byUuid = campaignStepRepository
								.findByUuid(campaignStepDeviceDetail.getCampaignStep().getUuid());
						Package packageTo = packageRepository.findByUuid(byUuid.getToPackage().getUuid());
						Package packageFrom = packageRepository.findByUuid(byUuid.getFromPackage().getUuid());
						deviceCampaignHistory
								.setLastStep(packageFrom.getPackageName() + " To " + packageTo.getPackageName());
						deviceCampaignHistory.setLastStepStatus(campaignStepDeviceDetail.getStatus().getValue());
						deviceCampaignHistory.setAPP_SW_VERSION(packageTo.getAppVersion());
						deviceCampaignHistory.setBASEBAND_SW_VERSION(packageTo.getBinVersion());
						deviceCampaignHistory.setBLE_Version(packageTo.getBleVersion());
						deviceCampaignHistory.setConfig1CIV(packageTo.getConfig1());
						deviceCampaignHistory.setConfig1CRC(packageTo.getConfig1Crc());
						deviceCampaignHistory.setConfig2CIV(packageTo.getConfig2());
						deviceCampaignHistory.setConfig2CRC(packageTo.getConfig2Crc());
						deviceCampaignHistory.setConfig3CIV(packageTo.getConfig3());
						deviceCampaignHistory.setConfig3CRC(packageTo.getConfig3Crc());
						deviceCampaignHistory.setConfig4CIV(packageTo.getConfig4());
						deviceCampaignHistory.setConfig4CRC(packageTo.getConfig4Crc());
					}
					link.add(deviceCampaignHistory);
				} catch (Exception e) {
					logger.error("error in getting campaign history details for gateway {}", device.getImei());
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			logger.error("error in getDeviceCampaignHistory  for gateway {}", device.getImei());
			e.printStackTrace();
		}
		campaignDeviceDetail.setDeviceCampaignHistory(link);
	}

	public List<DeviceCampaignHistory> getDeviceCampaignHistoryByDeviceId(String imei) {
		List<DeviceCampaignHistory> link = new ArrayList<DeviceCampaignHistory>();
		List<MsDeviceRestResponse> allDevices = new ArrayList<MsDeviceRestResponse>();
		if (!StringUtils.isEmpty(imei)) {
			//allDevices = restUtils.getSelectedDevicesFromMSByImeis(Arrays.asList(imei.split(",")));
			allDevices = deviceVersionRepository.findAllDeviceByIMEIList(Arrays.asList(imei.split(",")));

		}
		MsDeviceRestResponse device = null;
		if (allDevices == null || allDevices.size() < 1) {
			return link;
		}
		device = allDevices.get(0);
		DeviceCampaignHistory deviceCampaignHistory = null;
		String campaignStepDeviceDetailStatus = "";
		try {
			List<Campaign> activeCampaignsForImei = campaignUtils.checkCampaignForDeviceId(device.getImei(),
					device.getOrganisationName());
			for (Campaign campaign : activeCampaignsForImei) {
				try {
					deviceCampaignHistory = new DeviceCampaignHistory();
					deviceCampaignHistory.setCampaingName(campaign.getCampaignName());
					deviceCampaignHistory.setCampaingStatus(campaign.getCampaignStatus().getValue());
					CampaignStepDeviceDetail campaignStepDeviceDetail = deviceDetailRepository
							.getDeviceCampaignLastExecuteStep(campaign.getUuid(), device.getImei());
					if (campaignStepDeviceDetail != null) {
						campaignStepDeviceDetailStatus = campaignStepDeviceDetail.getStatus().getValue();
						if (campaignStepDeviceDetailStatus
								.equalsIgnoreCase(CampaignStepDeviceStatus.PENDING.getValue())) {
							deviceCampaignHistory
									.setLastStepTime(campaignStepDeviceDetail.getStartExecutionTime().toString());
						} else {
							deviceCampaignHistory
									.setLastStepTime(campaignStepDeviceDetail.getStopExecutionTime().toString());
						}
						CampaignStep byUuid = campaignStepRepository
								.findByUuid(campaignStepDeviceDetail.getCampaignStep().getUuid());
						Package packageTo = packageRepository.findByUuid(byUuid.getToPackage().getUuid());
						Package packageFrom = packageRepository.findByUuid(byUuid.getFromPackage().getUuid());
						deviceCampaignHistory
								.setLastStep(packageFrom.getPackageName() + " To " + packageTo.getPackageName());
						deviceCampaignHistory.setLastStepStatus(campaignStepDeviceDetail.getStatus().getValue());
						deviceCampaignHistory.setAPP_SW_VERSION(packageTo.getAppVersion());
						deviceCampaignHistory.setBASEBAND_SW_VERSION(packageTo.getBinVersion());
						deviceCampaignHistory.setBLE_Version(packageTo.getBleVersion());
						deviceCampaignHistory.setConfig1CIV(packageTo.getConfig1());
						deviceCampaignHistory.setConfig1CRC(packageTo.getConfig1Crc());
						deviceCampaignHistory.setConfig2CIV(packageTo.getConfig2());
						deviceCampaignHistory.setConfig2CRC(packageTo.getConfig2Crc());
						deviceCampaignHistory.setConfig3CIV(packageTo.getConfig3());
						deviceCampaignHistory.setConfig3CRC(packageTo.getConfig3Crc());
						deviceCampaignHistory.setConfig4CIV(packageTo.getConfig4());
						deviceCampaignHistory.setConfig4CRC(packageTo.getConfig4Crc());
					}
					link.add(deviceCampaignHistory);
				} catch (Exception e) {
					logger.error("error in getting campaign history details for gateway {}", device.getImei());
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			logger.error("error in getDeviceCampaignHistory  for gateway {}", device.getImei());
			e.printStackTrace();
		}
		return link;
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
						campaignDeviceDetail.setDeviceStatusForCampaign(DeviceStatusForCampaign.COMPLETED.getValue());
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
							List<DeviceStatus> elegibleCampaignStepDeviceDetail = elegibleCampaignStepDeviceDetailRepository
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
									List<DeviceStatus> elegibleCampaignStepDeviceDetail = elegibleCampaignStepDeviceDetailRepository
											.getByDeviceAndStatus(deviceId, "PENDING");

									/*
									 * if (!StringUtils.isEmpty(firstStepStatus) && (lastStepStatus == null ||
									 * !lastStepStatus
									 * .equalsIgnoreCase(CampaignStepDeviceStatus.SUCCESS.getValue()))) {
									 */
									if (elegibleCampaignStepDeviceDetail.size() > 0) {
										campaignDeviceDetail.setComments(
												Constants.CONFLICT_NOT_ELIGIBLE + " " + campaign.getCampaignName());
										link.add(new CampaignHyperLink(campaign.getCampaignName(), campaign.getUuid()));
										campaignDeviceDetail.setCampaignHyperLink(link);
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

	public LatestDeviceMaintenanceReport getLatestDeviceMaintenanceReport(ExecuteCampaignRequest executeCampaignRequest,
			String deviceId, String msgUuid) {
		LatestDeviceMaintenanceReport latestReport = iLatestDeviceMaintenanceReportRepository.findByDeviceId(deviceId);

		if (latestReport == null) {
			logger.debug("No latest device maintenance report found with device id " + deviceId);
			logger.debug("Message UUID : " + msgUuid);
			latestReport = new LatestDeviceMaintenanceReport();
			latestReport.setDeviceId(deviceId);
		}

		latestReport.setExtenderVersion(executeCampaignRequest.getExtenderVersion());
		latestReport.setSwVersionApplication(executeCampaignRequest.getSwVersionApplication());
		latestReport.setTimestampReceivedPST(executeCampaignRequest.getTimestampReceivedPST());
		logger.debug("executeCampaignRequest.getTimestampReceivedPST() ****** " + executeCampaignRequest.getTimestampReceivedPST());
		latestReport.setSwVersionBaseband(executeCampaignRequest.getSwVersionBaseband());
		latestReport.setBleVersion(executeCampaignRequest.getBleVersion());
		latestReport.setConfig1(executeCampaignRequest.getConfig1CIV());
		latestReport.setConfig2(executeCampaignRequest.getConfig2CIV());
		latestReport.setConfig3(executeCampaignRequest.getConfig3CIV());
		latestReport.setConfig4(executeCampaignRequest.getConfig4CIV());
		latestReport.setConfig5(executeCampaignRequest.getConfig5CIV());
		latestReport.setConfig1Crc(executeCampaignRequest.getConfig1CRC());
		latestReport.setConfig2Crc(executeCampaignRequest.getConfig2CRC());
		latestReport.setConfig3Crc(executeCampaignRequest.getConfig3CRC());
		latestReport.setConfig4Crc(executeCampaignRequest.getConfig4CRC());
		latestReport.setConfig5Crc(executeCampaignRequest.getConfig5CRC());
		LocalDateTime ldt = LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC);
		Instant now = ldt.toInstant(ZoneOffset.UTC);
	
		logger.debug("Timestamp.from(now)  " +Timestamp.from(now) );
		latestReport.setTimestampUpdatedPST(Timestamp.from(now));

		latestReport.setDeviceType(executeCampaignRequest.getDevice_type());

		latestReport.setLiteSentryApp(executeCampaignRequest.getLiteSentryApp());
		latestReport.setLiteSentryBoot(executeCampaignRequest.getLiteSentryBoot());
		latestReport.setLiteSentryHardware(executeCampaignRequest.getLiteSentryHw());

		latestReport.setMicrospMcu(executeCampaignRequest.getSteMcu());
		latestReport.setMicrospApp(executeCampaignRequest.getSteApp());

		latestReport.setCargoMaxbotixHardware(executeCampaignRequest.getMaxbotixHardware());
		latestReport.setCargoMaxbotixFirmware(executeCampaignRequest.getMaxbotixFirmware());

		latestReport.setCargoRiotHardware(executeCampaignRequest.getRiotHardware());
		latestReport.setCargoRiotFirmware(executeCampaignRequest.getRiotFirmware());
		try {
			latestReport.setRawReport(executeCampaignRequest.getRawReport());
		} catch (Exception e) {
			logger.error("error in setting raw report for gateway {}", deviceId);
		}

		return latestReport;
	}

	public boolean isDeviceStatusOnPath(Campaign currentCampain, List<Campaign> otherCampainList) {
		// logger.info("otherCampainList.size() "+otherCampainList.size());
		if (currentCampain != null && otherCampainList != null && otherCampainList.size() > 1) {
			for (Campaign otCampain : otherCampainList) {
				if (currentCampain.getUuid() != otCampain.getUuid()) {
					if (otCampain.getCampaignStatus().equals(CampaignStatus.IN_PROGRESS)
							|| otCampain.getCampaignStatus().equals(CampaignStatus.NOT_STARTED)
							|| otCampain.getCampaignStatus().equals(CampaignStatus.PAUSED)) {
						boolean onPathMatch = compareBaseLinePackage(currentCampain, otCampain);

						if (onPathMatch == true) {
							return true;
						}
					}
				}
			}
		}

		return false;
	}

	public void updateCommentsForOffPath(Campaign currentCampain, List<Campaign> otherCampainList,
			CampaignDeviceDetail campaignDeviceDetail, String deviceId) {

		if (currentCampain != null && otherCampainList != null && otherCampainList.size() > 1) {
			for (Campaign otCampain : otherCampainList) {
				if (currentCampain.getUuid() != otCampain.getUuid()) {
					String deviceStatus = deviceDetailRepository
							.findStatusByCampaignUuidAndStepOrderNumber(otCampain.getUuid(), deviceId, 1L);
					if (deviceStatus != null & !deviceStatus.isEmpty()
							&& (CampaignStepDeviceStatus.SUCCESS.getValue().equalsIgnoreCase(deviceStatus)
									|| CampaignStepDeviceStatus.PENDING.getValue().equalsIgnoreCase(deviceStatus)))
						campaignDeviceDetail.setComments("Currently in progress in " + otCampain.getCampaignName());
				} else {
					campaignDeviceDetail.setComments("Currently waiting to update in " + otCampain.getCampaignName());
				}
			}

		}

	}

	private boolean compareBaseLinePackage(Campaign currentCampain, Campaign otCampain) {
		Package currentCampaignPackage = campaignStepRepository
				.findBaseLinePackageByCampaignUuid(currentCampain.getUuid());
		CampaignStep otherCampaignStep = campaignStepRepository.findLastStepByCampaignUuid(otCampain.getUuid());

		if (currentCampaignPackage != null && otherCampaignStep != null) {
			// logger.info("currentCampain.getUuid() . "+currentCampain.getUuid()+ "
			// otCampain.getUuid() "+otCampain.getUuid());

			Package toPackage = otherCampaignStep.getToPackage();
			// logger.info("currentCampaignPackage . "+currentCampaignPackage.toString());
			// logger.info("toPackage . "+toPackage.toString());
			if ((currentCampaignPackage.getBinVersion().equalsIgnoreCase(toPackage.getBinVersion())
					|| currentCampaignPackage.getBinVersion().equalsIgnoreCase(Constants.ANY)
					|| toPackage.getBinVersion().equalsIgnoreCase(Constants.ANY))
					&& (currentCampaignPackage.getAppVersion().equalsIgnoreCase(toPackage.getAppVersion())
							|| currentCampaignPackage.getAppVersion().equalsIgnoreCase(Constants.ANY)
							|| toPackage.getAppVersion().equalsIgnoreCase(Constants.ANY))
					&& (currentCampaignPackage.getMcuVersion().equalsIgnoreCase(toPackage.getMcuVersion())
							|| currentCampaignPackage.getMcuVersion().equalsIgnoreCase(Constants.ANY)
							|| toPackage.getMcuVersion().equalsIgnoreCase(Constants.ANY))
					&& (currentCampaignPackage.getBleVersion().equalsIgnoreCase(toPackage.getBleVersion())
							|| currentCampaignPackage.getBleVersion().equalsIgnoreCase(Constants.ANY)
							|| toPackage.getBleVersion().equalsIgnoreCase(Constants.ANY))
					&& (currentCampaignPackage.getConfig1().equalsIgnoreCase(toPackage.getConfig1())
							|| currentCampaignPackage.getConfig1().equalsIgnoreCase(Constants.ANY)
							|| toPackage.getConfig1().equalsIgnoreCase(Constants.ANY))
					&& (currentCampaignPackage.getConfig2().equalsIgnoreCase(toPackage.getConfig2())
							|| currentCampaignPackage.getConfig2().equalsIgnoreCase(Constants.ANY)
							|| toPackage.getConfig2().equalsIgnoreCase(Constants.ANY))
					&& (currentCampaignPackage.getConfig3().equalsIgnoreCase(toPackage.getConfig3())
							|| currentCampaignPackage.getConfig3().equalsIgnoreCase(Constants.ANY)
							|| toPackage.getConfig3().equalsIgnoreCase(Constants.ANY))
					&& (currentCampaignPackage.getConfig4().equalsIgnoreCase(toPackage.getConfig4())
							|| currentCampaignPackage.getConfig4().equalsIgnoreCase(Constants.ANY)
							|| toPackage.getConfig4().equalsIgnoreCase(Constants.ANY))
					&& (currentCampaignPackage.getConfig1Crc().equalsIgnoreCase(toPackage.getConfig1Crc())
							|| currentCampaignPackage.getConfig1Crc().equalsIgnoreCase(Constants.ANY)
							|| toPackage.getConfig1Crc().equalsIgnoreCase(Constants.ANY))
					&& (currentCampaignPackage.getConfig2Crc().equalsIgnoreCase(toPackage.getConfig2Crc())
							|| currentCampaignPackage.getConfig2Crc().equalsIgnoreCase(Constants.EMPTY_STRING)
							|| toPackage.getConfig2Crc().equalsIgnoreCase(Constants.EMPTY_STRING))
					&& (currentCampaignPackage.getConfig3Crc().equalsIgnoreCase(toPackage.getConfig3Crc())
							|| currentCampaignPackage.getConfig3Crc().equalsIgnoreCase(Constants.EMPTY_STRING)
							|| toPackage.getConfig3Crc().equalsIgnoreCase(Constants.EMPTY_STRING))
					&& (currentCampaignPackage.getConfig4Crc().equalsIgnoreCase(toPackage.getConfig4Crc())
							|| currentCampaignPackage.getConfig4Crc().equalsIgnoreCase(Constants.EMPTY_STRING)
							|| toPackage.getConfig4Crc().equalsIgnoreCase(Constants.EMPTY_STRING))) {
				return true;

			}
		}

		return false;
	}

	public ExecuteCampaignRequest deviceReportRedisToExecuteCampaignRequest(DeviceReport deviceReport) {
		ExecuteCampaignRequest executeCampaignRequest = new ExecuteCampaignRequest();

		executeCampaignRequest.setConfig1CRC(deviceReport.getConfig1CRC());
		executeCampaignRequest.setConfig2CRC(deviceReport.getConfig2CRC());
		executeCampaignRequest.setConfig3CRC(deviceReport.getConfig3CRC());
		executeCampaignRequest.setConfig4CRC(deviceReport.getConfig4CRC());
		executeCampaignRequest.setConfig5CRC(deviceReport.getConfig5CRC());

		executeCampaignRequest.setSwVersionBaseband(deviceReport.getBASEBAND_SW_VERSION());
		executeCampaignRequest.setSwVersionApplication(deviceReport.getAPP_SW_VERSION());
		executeCampaignRequest.setExtenderVersion(deviceReport.getEXTENDER_VERSION());

		executeCampaignRequest.setConfig1CIV(deviceReport.getConfig1CIV());
		executeCampaignRequest.setConfig2CIV(deviceReport.getConfig2CIV());
		executeCampaignRequest.setConfig3CIV(deviceReport.getConfig3CIV());
		executeCampaignRequest.setConfig4CIV(deviceReport.getConfig4CIV());
		executeCampaignRequest.setConfig5CIV(deviceReport.getConfig5CIV());

		return executeCampaignRequest;
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

	public List<MsDeviceRestResponse> convertDeviceListToMsDeviceRestResponse(List<Device> deviceList) {
		List<MsDeviceRestResponse> msDeviceRestResponseList = new ArrayList<>();
		for (Device device : deviceList) {
			msDeviceRestResponseList.add(convertDeviceToMsDeviceRestResponse(device));

		}
		return msDeviceRestResponseList;
	}

	private MsDeviceRestResponse convertDeviceToMsDeviceRestResponse(Device device) {
		MsDeviceRestResponse msDeviceRestResponse = new MsDeviceRestResponse();

		msDeviceRestResponse.setAppVersion(device.getDeviceDetails().getAppVersion());
		msDeviceRestResponse.setBinVersion(device.getDeviceDetails().getBinVersion());
		msDeviceRestResponse.setBleVersion(device.getDeviceDetails().getBleVersion());
		msDeviceRestResponse.setConfig1(device.getDeviceDetails().getConfig1Name());
		msDeviceRestResponse.setConfig2(device.getDeviceDetails().getConfig2Name());
		msDeviceRestResponse.setConfig3(device.getDeviceDetails().getConfig3Name());
		msDeviceRestResponse.setConfig4(device.getDeviceDetails().getConfig4Name());
		msDeviceRestResponse.setCreatedAt(device.getCreatedAt());
		msDeviceRestResponse.setCreatedBy(null);
		msDeviceRestResponse.setDeviceDetailsDto(convertDeviceDetailsToDeviceDetailsDto(device.getDeviceDetails()));
		msDeviceRestResponse.setDeviceType(device.getDeviceType());
		msDeviceRestResponse.setEpicorOrderNumber(device.getEpicorOrderNumber());
		msDeviceRestResponse.setId(device.getId());
		msDeviceRestResponse.setImei(device.getImei());
		//msDeviceRestResponse.setInstalledDate(device.getInstalledDate());
		msDeviceRestResponse.setIotType(device.getIotType());
		// msDeviceRestResponse.setLatestReport(device.getLatestReport());
		msDeviceRestResponse.setMacAddress(device.getMacAddress());
		msDeviceRestResponse.setMcuVersion(device.getDeviceDetails().getMcuVersion());
		msDeviceRestResponse.setOrganisationsDto(convertOrganisationToOrganisationsDto(device.getOrganisation()));
		return msDeviceRestResponse;
	}

	private OrganisationsDTO convertOrganisationToOrganisationsDto(Organisation organisation) {
		OrganisationsDTO organisationsDto = new OrganisationsDTO();
		organisationsDto.setId(organisation.getId());
		organisationsDto.setOrganisationName(organisation.getOrganisationName());
		organisationsDto.setShortName(organisation.getShortName());
		return organisationsDto;
	}

	private DeviceDetailsDto convertDeviceDetailsToDeviceDetailsDto(DeviceDetails deviceDetails) {
		DeviceDetailsDto deviceDetailsDto = new DeviceDetailsDto();
		deviceDetailsDto.setAppVersion(deviceDetails.getAppVersion());
		deviceDetailsDto.setBattery(deviceDetails.getBattery());
		deviceDetailsDto.setBinVersion(deviceDetails.getBinVersion());
		deviceDetailsDto.setBleVersion(deviceDetails.getBinVersion());
		deviceDetailsDto.setConfig1CRC(deviceDetails.getConfig1CRC());
		deviceDetailsDto.setConfig1Name(deviceDetails.getConfig1Name());
		deviceDetailsDto.setConfig2CRC(deviceDetails.getConfig2CRC());
		deviceDetailsDto.setConfig2Name(deviceDetails.getConfig2Name());
		deviceDetailsDto.setConfig3CRC(deviceDetails.getConfig3CRC());
		deviceDetailsDto.setConfig3Name(deviceDetails.getConfig3Name());
		deviceDetailsDto.setConfig4CRC(deviceDetails.getConfig4CRC());
		deviceDetailsDto.setConfig4Name(deviceDetails.getConfig4Name());
		deviceDetailsDto.setDevuserCfgName(deviceDetails.getDevuserCfgName());
		deviceDetailsDto.setDevuserCfgValue(deviceDetails.getDevuserCfgValue());
		deviceDetailsDto.setEventId(deviceDetails.getEventId());
		deviceDetailsDto.setEventType(deviceDetails.getEventType());
		//deviceDetailsDto.setId(deviceDetails.getId());
		deviceDetailsDto.setImei(deviceDetails.getImei());
		deviceDetailsDto.setLat(deviceDetails.getLat());
		deviceDetailsDto.setLatestReport(deviceDetails.getLatestReport());
		deviceDetailsDto.setLongitude(deviceDetails.getLongitude());
		deviceDetailsDto.setMcuVersion(deviceDetails.getMcuVersion());
		return deviceDetailsDto;
	}

	public List<CampaignDeviceDetail> deviceCampaignStatusListToCampaignDeviceDetailList(
			List<DeviceCampaignStatus> deviceCampaignStatusList,
			Map<String, List<CampaignStepDeviceDetail>> imeiStepData, int maxStepInCamp) {
		
		Map<String , List<DeviceCampaignStatus>> deviceCampaignStatusMap = deviceCampaignStatusList.stream().collect(Collectors.groupingBy(w -> w.getDeviceId()));
		List<String> imeiList = new ArrayList(deviceCampaignStatusMap.keySet());
	
		List<LatestDeviceMaintenanceReport> latestMaintenanceReportList = iLatestDeviceMaintenanceReportRepository.findLatestDeviceMaintenanceReportByDeviceId(imeiList);
		Map<String, List<LatestDeviceMaintenanceReport>> latestMaintenanceReportMap = null;
		if(latestMaintenanceReportList != null )
		{
			latestMaintenanceReportMap = latestMaintenanceReportList.stream().collect(Collectors.groupingBy(w -> w.getDeviceId()));
		}
		
		List<CampaignDeviceDetail> campaignDeviceDetailList = new ArrayList<>();

		for (DeviceCampaignStatus deviceCampaignStatus : deviceCampaignStatusList) {
			CampaignDeviceDetail campaignDeviceDetail = new CampaignDeviceDetail();
			campaignDeviceDetail.setInstalledFlag(deviceCampaignStatus.getInstalledFlag());
			if (deviceCampaignStatus.getLastReportedAt() != null) {
				campaignDeviceDetail.setLastReport(deviceCampaignStatus.getLastReportedAt().toString());
			}

			campaignDeviceDetail.setDeviceStatusForCampaign(deviceCampaignStatus.getRunningStatus());
			campaignDeviceDetail.setComments(deviceCampaignStatus.getComment());
			campaignDeviceDetail.setImei(deviceCampaignStatus.getDeviceId());
			campaignDeviceDetail.setCustomerName(deviceCampaignStatus.getCustomerName());
			campaignDeviceDetail.setOrganisationName(deviceCampaignStatus.getCustomerName());
			campaignDeviceDetail.setCustomerId(deviceCampaignStatus.getCustomerId());
			List<DeviceStepStatus> deviceStepStatusList = new ArrayList<DeviceStepStatus>();
			setDeviceCampaignStepDetails(imeiStepData, deviceCampaignStatus.getDeviceId(), maxStepInCamp,
					deviceStepStatusList);
			campaignDeviceDetail.setDeviceStepStatus(deviceStepStatusList);
			campaignDeviceDetailList.add(campaignDeviceDetail);
			List<DeviceCampaignStatus> deviceCampaignPendingStatusList = deviceCampaignStatusRepository
					.findByDeviceIdAndStatus(deviceCampaignStatus.getDeviceId(),
							CampaignStepDeviceStatus.PENDING.getValue());
			List<CampaignHyperLink> link = new ArrayList<CampaignHyperLink>();
			if (deviceCampaignPendingStatusList != null && deviceCampaignPendingStatusList.size() > 0) {
				for (DeviceCampaignStatus deviceCampaignStatusPending : deviceCampaignPendingStatusList) {
					if (!deviceCampaignStatusPending.getCampaign().getUuid()
							.equals(deviceCampaignStatus.getCampaign().getUuid())) {
						if(deviceCampaignStatus.getEligibility().equals(DeviceStatusForCampaign.ELIGIBLE.getValue()))
						{
							campaignDeviceDetail.setComments(Constants.CONFLICT_ELIGIBLE + " "
									+ deviceCampaignStatusPending.getCampaign().getCampaignName());
					
						}
						else
						{
							campaignDeviceDetail.setComments(Constants.CONFLICT_NOT_ELIGIBLE + " "
									+ deviceCampaignStatusPending.getCampaign().getCampaignName());
					
						}
						link.add(new CampaignHyperLink(deviceCampaignStatusPending.getCampaign().getCampaignName(),
								deviceCampaignStatusPending.getCampaign().getUuid()));
						campaignDeviceDetail.setCampaignHyperLink(link);
					}
				}
			}
			if(latestMaintenanceReportMap != null && latestMaintenanceReportMap.get(deviceCampaignStatus.getDeviceId()) != null && latestMaintenanceReportMap.get(deviceCampaignStatus.getDeviceId()).get(0) != null)
			{
				LatestDeviceMaintenanceReport  latestDeviceMaintenanceReport = latestMaintenanceReportMap.get(deviceCampaignStatus.getDeviceId()).get(0);
				if(latestDeviceMaintenanceReport != null)
				{
					campaignDeviceDetail.setLastReport(latestDeviceMaintenanceReport.getTimestampReceivedPST()+"");
				}
				campaignDeviceDetail.setDeviceReport(populateDeviceReport(latestDeviceMaintenanceReport));
						
			}
			List<String> list = new ArrayList<String>();
			list.add(deviceCampaignStatus.getDeviceId());
			List<DeviceReport> deviceReports = campaignDeviceHelper.getDeviceReports(list);
			if(deviceReports != null && deviceReports.size() >0)
			{
			//	campaignDeviceDetail.setDeviceReport(deviceReports.get(0));
			}
		}

		return campaignDeviceDetailList;
	}
	
	private DeviceReport populateDeviceReport(LatestDeviceMaintenanceReport 
latestDeviceMaintenanceReport) {
		
		DeviceReport deviceReports = new DeviceReport();
		deviceReports.setAPP_SW_VERSION(latestDeviceMaintenanceReport.getSwVersionApplication());
		deviceReports.setBASEBAND_SW_VERSION(latestDeviceMaintenanceReport.getSwVersionBaseband());
		deviceReports.setBLE_VERSION(latestDeviceMaintenanceReport.getBleVersion());
		deviceReports.setDEVICE_ID(latestDeviceMaintenanceReport.getDeviceId());
		deviceReports.setEXTENDER_VERSION(latestDeviceMaintenanceReport.getExtenderVersion());
		
		deviceReports.setLiteSentryApp(latestDeviceMaintenanceReport.getLiteSentryApp());
		deviceReports.setLiteSentryHw(latestDeviceMaintenanceReport.getLiteSentryHardware());
		deviceReports.setLiteSentryBoot(latestDeviceMaintenanceReport.getLiteSentryBoot());
		deviceReports.setSteMCU(latestDeviceMaintenanceReport.getMicrospMcu());
		deviceReports.setSteApp(latestDeviceMaintenanceReport.getMicrospApp());
		deviceReports.setMaxbotixFirmware(latestDeviceMaintenanceReport.getCargoMaxbotixFirmware());
		deviceReports.setMaxbotixhardware(latestDeviceMaintenanceReport.getCargoMaxbotixHardware());
		//deviceReport.setSteMCU(latestDeviceMaintenanceReport.get);
	//	deviceReport.setSteApp(latestDeviceMaintenanceReport.ge`);
		deviceReports.setRiotFirmware(latestDeviceMaintenanceReport.getCargoRiotFirmware());
		deviceReports.setRiothardware(latestDeviceMaintenanceReport.getCargoRiotHardware());
		deviceReports.setBLE_VERSION(latestDeviceMaintenanceReport.getBleVersion());

		deviceReports.setConfig1CIV(latestDeviceMaintenanceReport.getConfig1());
		deviceReports.setConfig2CIV(latestDeviceMaintenanceReport.getConfig2());
		deviceReports.setConfig3CIV(latestDeviceMaintenanceReport.getConfig3());
		deviceReports.setConfig4CIV(latestDeviceMaintenanceReport.getConfig4());
		deviceReports.setConfig5CIV(latestDeviceMaintenanceReport.getConfig5());
		
		deviceReports.setConfig1CRC(latestDeviceMaintenanceReport.getConfig1Crc());
		deviceReports.setConfig2CRC(latestDeviceMaintenanceReport.getConfig2Crc());
		deviceReports.setConfig3CRC(latestDeviceMaintenanceReport.getConfig3Crc());
		deviceReports.setConfig4CRC(latestDeviceMaintenanceReport.getConfig4Crc());
		deviceReports.setConfig5CRC(latestDeviceMaintenanceReport.getConfig5Crc());
		if(latestDeviceMaintenanceReport.getTimestampReceivedPST() != null)
		deviceReports.setUpdated_date(latestDeviceMaintenanceReport.getTimestampReceivedPST().toInstant());
		
		return deviceReports;
		
		
	}

	public List<StepDTO> getStepUuidDTO(String campaignUuid, List<CampaignStep> allCampaignSteps,
			int totalImeisInCampaigan) {
		
		
		BigInteger totalCount = BigInteger.valueOf(totalImeisInCampaigan);
		Map<String, List<CampaignStep>> campaignStepDataMap = new HashMap<String, List<CampaignStep>>();
		if (allCampaignSteps != null) {
			campaignStepDataMap = allCampaignSteps.stream().collect(Collectors.groupingBy(w -> w.getUuid()));
		}
		List<Tuple> findByStepDtoByUuid = deviceDetailRepository.findByStepDtoByUuid(campaignUuid);
		Map<String, List<StepDTO>> stepDTOMap = new HashMap<String, List<StepDTO>>();
		List<StepDTO> stepDTOList = null;
		if (findByStepDtoByUuid != null) {
			stepDTOList = findByStepDtoByUuid.stream().map(t -> new StepDTO(t.get(0, String.class),
					t.get(1, BigInteger.class), t.get(2, String.class), t.get(3, String.class)))
					.collect(Collectors.toList());

			stepDTOMap = stepDTOList.stream().collect(Collectors.groupingBy(w -> w.getStepUuid()));
		}
		List<StepDTO> returnList = new ArrayList<StepDTO>();
		for (String stepUuid : campaignStepDataMap.keySet()) {
			String packageName = campaignStepDataMap.get(stepUuid).get(0).getFromPackage().getPackageName();
			List<StepDTO> stepDtoListForStep = stepDTOMap.get(stepUuid);
			
			if (stepDtoListForStep == null) {
				stepDtoListForStep = new ArrayList<StepDTO>();
				StepDTO stepDTO = new StepDTO(packageName, BigInteger.valueOf(totalImeisInCampaigan), stepUuid,
						CampaignStepDeviceStatus.NOTSTARTED.getValue());
				returnList.add(stepDTO);
			} else {
				String packageName1 = stepDtoListForStep.get(0).getPackageName();
				BigInteger notStarted = BigInteger.valueOf(totalImeisInCampaigan);
				logger.info("notStarted ****"+notStarted + " stepDto1  " +stepDtoListForStep.get(0).getPackageName());
				
				for (StepDTO stepDto1 : stepDtoListForStep) {
				 logger.info("notStarted ****"+notStarted + " stepDto1  " +stepDtoListForStep.get(0).getPackageName()  + " stepDto1.getCount()  "+ stepDto1.getCount()  + "stepDto1.getStatus() "+stepDto1.getStatus() ); 
					notStarted = notStarted.subtract(stepDto1.getCount());
					returnList.add(stepDto1);
				}
				StepDTO stepDTO = new StepDTO(packageName1, notStarted, stepUuid, CampaignStepDeviceStatus.NOTSTARTED.getValue());
				returnList.add(stepDTO);
				// stepDtoListForStep.add(stepDTO);
			}

		}

		return returnList;
	}

}
