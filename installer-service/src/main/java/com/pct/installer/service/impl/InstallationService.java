package com.pct.installer.service.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.pct.common.constant.AssetStatus;
import com.pct.common.constant.Constants;
import com.pct.common.constant.DeviceStatus;
import com.pct.common.constant.EventType;
import com.pct.common.constant.GatewayStatus;
import com.pct.common.constant.IOTType;
import com.pct.common.constant.InstallHistoryStatus;
import com.pct.common.constant.InstallLogStatus;
import com.pct.common.constant.OrganisationRole;
import com.pct.common.constant.ProdCodeName;
import com.pct.common.constant.SensorPosition;
import com.pct.common.constant.SensorStatus;
import com.pct.common.dto.AttributeValueResposneDTO;
import com.pct.common.dto.DeviceDto;
import com.pct.common.dto.InProgressInstall;
import com.pct.common.dto.ProductMasterDto;
import com.pct.common.dto.TpmsSensorCountDTO;
import com.pct.common.exception.InterServiceRestException;
import com.pct.common.model.Asset;
import com.pct.common.model.Attribute;
import com.pct.common.model.Device;
import com.pct.common.model.Device_Device_xref;
import com.pct.common.model.InstallHistory;
import com.pct.common.model.InstallLog;
import com.pct.common.model.Organisation;
import com.pct.common.model.ProductMaster;
import com.pct.common.model.ReasonCode;
import com.pct.common.model.SensorDetail;
import com.pct.common.model.SensorHistoryForInstallation;
import com.pct.common.model.SensorInstallInstruction;
import com.pct.common.model.SensorReasonCode;
import com.pct.common.model.User;
import com.pct.common.payload.DeviceSensorxrefPayload;
import com.pct.common.payload.GatewayDetailsBean;
import com.pct.common.payload.GetInstallHistoryByAssetUuids;
import com.pct.common.payload.InstallInstructionBean;
import com.pct.common.payload.InstallationStatusGatewayRequest;
import com.pct.common.payload.InstalledHistroyResponse;
import com.pct.common.payload.ReasonCodeBean;
import com.pct.common.payload.SensorUpdateRequest;
import com.pct.common.payload.UpdateGatewayAssetStatusRequest;
import com.pct.common.util.Context;
import com.pct.common.util.JwtUser;
import com.pct.common.util.Logutils;
//import com.pct.installer.constant.EventType;
import com.pct.installer.constant.LogIssueStatus;
import com.pct.installer.dto.InstallationDetailResponseDTO;
import com.pct.installer.dto.InstallationSummaryResponseDTO;
import com.pct.installer.dto.SensorInstallInstructionDto;
import com.pct.installer.dto.SensorReasonCodeDto;
//import com.pct.installer.entity.InstallLog;
import com.pct.installer.entity.LogIssue;
//import com.pct.installer.entity.LogIssue;
import com.pct.installer.exception.InstallerException;
import com.pct.installer.payload.CreateGatewaySensorAssociation;
import com.pct.installer.payload.DeviceDetailsBean;
import com.pct.installer.payload.DeviceDetailsResponse;
import com.pct.installer.payload.DeviceResponsePayload;
import com.pct.installer.payload.FinishInstallRequest;
import com.pct.installer.payload.InstallationStatusResponse;
import com.pct.installer.payload.LogIssueBean;
import com.pct.installer.payload.LogIssueGatewayRequest;
import com.pct.installer.payload.LogIssueRequest;
import com.pct.installer.payload.LogIssueStatusRequest;
import com.pct.installer.payload.SensorDetailsBean;
import com.pct.installer.payload.SensorDetailsResponse;
import com.pct.installer.payload.StartInstallRequest;
import com.pct.installer.payload.UpdateSensorStatusRequest;
import com.pct.installer.payload.UpdateSensorStatusWithInstanceRequest;
import com.pct.installer.repository.IAttributeRepository;
import com.pct.installer.repository.IDeviceDeviceXrefRepository;
import com.pct.installer.repository.IDeviceRepository;
import com.pct.installer.repository.IInstallHistoryRepository;
import com.pct.installer.repository.IInstallLogRepository;
import com.pct.installer.repository.ILogIssueRepository;
import com.pct.installer.repository.IProductMasterRepository;
//import com.pct.installer.repository.ILogIssueRepository;
import com.pct.installer.repository.IReasonCodeRepository;
import com.pct.installer.repository.ISensorHistoryForInstallationRepository;
import com.pct.installer.repository.ISensorInstallInstructionRepository;
import com.pct.installer.repository.ISensorReasonCodeRepository;
import com.pct.installer.repository.SensorDetailRepository;
import com.pct.installer.service.IInstallationService;
import com.pct.installer.specification.InstallerHistorySpecification;
import com.pct.installer.util.BeanConverter;
import com.pct.installer.util.RestUtils;
import com.pct.installer.util.Utilities;
//import ch.qos.logback.core.joran.util.beans.BeanUtil;

/**
 * @author Abhishek on 01/05/20
 */

@Service
public class InstallationService implements IInstallationService {

	Logger logger = LoggerFactory.getLogger(InstallationService.class);
	public static final String className = "InstallationService";

	@Autowired
	private RestUtils restUtils;

	@Autowired
	private IDeviceRepository deviceRepository;

	@Autowired
	private IProductMasterRepository productMasterRepository;

	@Autowired
	private IAttributeRepository atttributeRespo;

	@Autowired
	private IInstallHistoryRepository installHistoryRepository;
	@Autowired
	private IInstallLogRepository installLogRepository;
	@Autowired
	private SensorDetailRepository sensorDetailRepository;

	@Autowired
	private IDeviceRepository deviceRepo;

	@Autowired
	IDeviceDeviceXrefRepository deviceDeviceXrefRepository;

	@Autowired
	private IReasonCodeRepository reasonCodeRepository;
	@Autowired
	private ILogIssueRepository logIssueRepository;
	@Autowired
	private ISensorReasonCodeRepository sensorReasonCodeRepository;
	@Autowired
	private ISensorInstallInstructionRepository sensorInstallInstructionRepository;
	@Autowired
	private BeanConverter beanConverter;

	@Autowired
	Utilities utilities;

	@Autowired
	private ISensorHistoryForInstallationRepository sensorHistoryForInstallationRepository;

	boolean returnStatus = false;

	static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public static final TimeZone timeZoneUTC = TimeZone.getTimeZone("PST");
	static {
		dateFormat.setTimeZone(timeZoneUTC);
	}
	static DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
			.withZone(ZoneId.of("America/Los_Angeles"));

//---------------------------------Aamir Start-------------------------------------//
	@Override
	@Transactional
	public Boolean startInstall(StartInstallRequest startInstallRequest, Long userId, String logUUid) throws Exception {

		String methodName = "startInstall";
		Logutils.log(className, methodName, logUUid,
				" Inside startInstall Method From InstallerService " + "StartInstallRequest " + startInstallRequest,
				logger);
		if (utilities.checkFieldsNullablity(logUUid, startInstallRequest)) {
			InstallHistory installHistory = null;

			Asset asset = restUtils.getAssetByAssetUUID(logUUid, startInstallRequest.getAssetUuid());
			if (asset != null) {

				Logutils.log(className, methodName, logUUid, "asset  Uuid : " + asset.getUuid(), logger);
			}
			User user = restUtils.getUserFromAuthService(logUUid, userId);
			if (user != null) {
				Logutils.log(className, methodName, logUUid, "  User : " + user.getUserName(), logger);
			}

			Device device = utilities.getDevice(logUUid, startInstallRequest);
			if (device != null) {
				Logutils.log(className, methodName, logUUid, "  Uuid : " + asset.getUuid(), logger);
			}
			installHistory = utilities.createInstallHistory(logUUid, asset, device, userId, startInstallRequest);
			if (installHistory != null) {
				Logutils.log(className, methodName, logUUid, "  Uuid : " + installHistory.getUuid(), logger);
			}
			boolean adXrefSuccessful = utilities.createAssetDeviceyXrefRequest(logUUid, startInstallRequest);
			if (adXrefSuccessful) {
				Logutils.log(className, methodName, logUUid, "  Uuid : " + adXrefSuccessful, logger);
			}

			// Add code for pre-pair product
			if (asset.getIsApplicableForPrePair() && restUtils.isAssetApplicableForPrePair(logUUid, asset.getUuid())) {
				try {
					Boolean prePairingStatus = utilities.prePairingInstallationAtTheTimeOfStartInstall(logUUid, asset,
							device, userId, installHistory);
					Logutils.log(logUUid,
							" prePairingInstallationAtTheTimeOfStartInstall method Pre Pairing is successfully completed.",
							logger, "asset :" + asset.getUuid() + " with status of " + prePairingStatus);
//    					logger.info("Pre Pairing is successfully completed for the asset :" + asset.getUuid() + " with status of " + prePairingStatus);
				} catch (Exception e) {
					e.printStackTrace();
					logger.error("Exception occurred while pre pairing", e.getMessage());
				}
			}
			returnStatus = true;
		} else {
			logger.error("Exception occurred while creating install history", "Device ID and vim should not be null");
			throw new InstallerException("Device ID and vim should not be null");
		}

		Logutils.log(className, methodName, logUUid, " Exiting From InstallerService ", logger);
		return returnStatus;
	}

	@Override
	@Transactional
	public Boolean resetInstallation(String logUUid, String vin, String deviceID, String can) {
		String methodName = "resetInstallation";
		Logutils.log(className, methodName, logUUid, " Inside resetInstallation Method From InstallerService "
				+ "deviceID " + deviceID + "can " + can + "vin " + vin, logger);
		String imei = null;
		String mac = null;
		if (utilities.checkInputValue(deviceID)) {
			String macReg = "^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$";
			String imeiReg = "^([0-9]{15})$";
			Pattern macPattern = Pattern.compile(macReg);
			Pattern imeiPattern = Pattern.compile(imeiReg);
			Matcher macMatcher = macPattern.matcher(deviceID.trim());
			Matcher imeiMatcher = imeiPattern.matcher(deviceID.trim());

			if (macMatcher.matches()) {
				mac = deviceID;
			} else if (imeiMatcher.matches()) {
				imei = deviceID;
			} else {
				throw new InstallerException("Installation Reset for " + deviceID
						+ " has been unsuccessful due to incorrect format of Device ID.");

			}
		}
		logger.info(" Inside resetInstallation Method From InstallerService having value of device Id " + deviceID);
		boolean status = false;
		if (utilities.checkInputValue(can)) {
			if (utilities.checkInputValue(vin) && utilities.checkInputValue(imei) || utilities.checkInputValue(mac)) {
				String vinLog = "Vin: " + vin;
				String canLog = "Can: " + can;

				Logutils.log(logUUid, " Before calling restUtils.getAssetByVinAndCan method ", logger, vinLog, canLog);
				Asset asset = restUtils.getAssetByVinAndCan(logUUid, vin, can);
				String assetIdLog = "AssetId: ";
				if (asset != null) {
					Logutils.log(className, methodName, logUUid, "asset  Uuid : " + asset.getUuid(), logger);
					assetIdLog = assetIdLog + asset.getId();
				}

				Logutils.log(logUUid, " After calling restUtils.getAssetByVinAndCan method ", logger, assetIdLog);

				if (asset == null) {
					throw new InstallerException("This VIN could not be found. Please check and try again.");
				}
				Device device = utilities.getDeviceForRest(logUUid, imei, mac, can);
				if (device != null) {
					Logutils.log(className, methodName, logUUid, "Device  Uuid : " + device.getUuid(), logger);
				}
				utilities.createInstallationHisteory(logUUid, asset, device);
				status = true;

			} else if ((utilities.checkInputValue(vin)) && (imei == null || imei.isEmpty())
					&& (mac == null || mac.isEmpty())) {
				utilities.deleteRepoForResetInstallation1(logUUid, vin);
				status = true;

			} else if ((vin == null || vin.isEmpty()) && utilities.checkInputValue(imei)
					|| (utilities.checkInputValue(mac))) {
				utilities.deleteRepoForResetInstallation2(logUUid, imei, mac);
				status = true;

			} else {
				throw new InstallerException(
						"Please enter either a Valid Device-ID or a VIN for the installation you want to reset.");
			}
		} else {
			throw new InstallerException("Company account number can not be null");
		}
		Logutils.log(className, methodName, logUUid, " Exiting From InstallerService ", logger);
		return status;
	}

	@Override
	public List<LogIssueBean> getLoggedIssues(String installCode, Context context) {
		String methodName = "getLoggedIssues";
		String logUUid = context.getLogUUId();
		Logutils.log(className, methodName, logUUid,
				" Inside getLoggedIssues Method From InstallerService " + " installCode " + installCode, logger);
		if (utilities.checkInputValue(installCode)) {
			String installCodeLog = "InstallCode: " + installCode;
			Logutils.log(className, methodName, context.getLogUUId(),
					" Before calling installHistoryRepository.findByInstallCode method ", logger, installCodeLog);

			InstallHistory byInstallCode = installHistoryRepository.findByInstallCode(installCode);
			if (byInstallCode != null) {
				Logutils.log(className, methodName, context.getLogUUId(),
						" Before calling logIssueRepository.findByInstallCode method ", logger, installCodeLog);

				List<LogIssue> logIssueList = logIssueRepository.findByInstallCode(installCode);
				if (logIssueList != null && logIssueList.size() > 0) {
					Logutils.log(className, methodName, logUUid, " list of logIssueList : " + logIssueList, logger);
				}
				if (utilities.checkInputValue(logIssueList)) {
					List<LogIssueBean> logIssueBeanList = beanConverter.convertLogIssueToLogIssueBean(logIssueList);
					if (logIssueBeanList != null && logIssueBeanList.size() > 0) {
						Logutils.log(className, methodName, logUUid, " list of logIssueBeanList : " + logIssueBeanList,
								logger);
					}
					Logutils.log(className, methodName, logUUid, " Exiting From InstallerService ", logger);
					return logIssueBeanList;
				} else {
					return new ArrayList<LogIssueBean>();
				}
			} else {
				throw new InstallerException("No install history found for given install code");
			}
		} else {
			throw new InstallerException("Please provide install code and try again");
		}
	}

	@Override
	public Page<GatewayDetailsBean> getGatewayDetailsWithPagination(String logUUid, String installCode, String can,
			Long userId, Context context, Integer page, Integer pageSize, String sort, String order) {
		String methodName = "getGatewayDetailsWithPagination";
		Logutils.log(className, methodName, logUUid,
				" Inside getGatewayDetailsWithPagination Method From InstallerService " + "installCode " + installCode,
				logger);
		long totalElements = 0;
		Pageable pageable = null;
		List<GatewayDetailsBean> gatewatDetailList = new ArrayList<>();
		List<Device> gatewayList = new ArrayList<>();
		String userIdLog = "UserId: " + userId;
		Logutils.log(className, methodName, context.getLogUUId(),
				" Before calling restUtils.getUserFromAuthService method ", logger, userIdLog);
		User user = restUtils.getUserFromAuthService(logUUid, userId);
		if (user != null) {
			Logutils.log(className, methodName, logUUid, "  User : " + user.getUserName(), logger);
		}
		if (installCode == null && can == null) {
			Logutils.log(className, methodName, context.getLogUUId(),
					" value of install code and can is null and role of user is ", logger, "");
			if (user.getOrganisation().getOrganisationRole().contains(OrganisationRole.INSTALLER)) {
				Organisation company = restUtils
						.getOrganisationByUuidFromCompanyService(user.getOrganisation().getUuid());
				List<Organisation> companies = company.getAccessList();
				if (companies.size() > 0) {
					List<String> canList = new ArrayList<String>();

					for (Organisation com : companies) {
						canList.add(com.getAccountNumber());

					}
					ResponseEntity<Page<Device>> result = restUtils
							.getGatewaysByCANAndStatusFromDeviceServiceWithPagination(logUUid, installCode, can, null,
									page, sort, order, canList);

					List<Device> list = result.getBody().getContent();
					totalElements = result.getBody().getTotalElements();
					pageable = result.getBody().getPageable();
					gatewayList.addAll(list);
				}

			}
			if (user.getOrganisation().getOrganisationRole().contains(OrganisationRole.END_CUSTOMER)) {
				Organisation companies = user.getOrganisation();
				if (companies != null) {
					ResponseEntity<Page<Device>> result = restUtils
							.getGatewaysByCANAndStatusFromDeviceServiceWithPagination(logUUid,
									companies.getAccountNumber(), null, page, pageSize, sort, order, null);

					List<Device> list = result.getBody().getContent();
					totalElements = result.getBody().getTotalElements();
					pageable = result.getBody().getPageable();

					gatewayList.addAll(list);
				}
			}
			if (user.getOrganisation().getOrganisationRole().contains(OrganisationRole.MANUFACTURER)) {

				List<Organisation> list = restUtils.getCompanyByCustomer(logUUid);
				if (list.size() > 0) {
					List<String> canList = new ArrayList<String>();

					for (Organisation com : list) {
						canList.add(com.getAccountNumber());
					}
					ResponseEntity<Page<Device>> result = restUtils
							.getGatewaysByCANAndStatusFromDeviceServiceWithPagination(logUUid, null, null, null, page,
									sort, order, canList);
					List<Device> list1 = result.getBody().getContent();
					totalElements = result.getBody().getTotalElements();
					pageable = result.getBody().getPageable();

					gatewayList.addAll(list1);
				}

			}
		}
		String installCodeLog = "InstallCode: " + installCode;

		if (utilities.checkInputValue(installCode) && can == null) {
			Logutils.log(className, methodName, context.getLogUUId(),
					" Before calling installHistoryRepository.findByInstallCode method ", logger, installCodeLog);

			InstallHistory installHistory1 = installHistoryRepository.findByInstallCode(installCode);
			if (installHistory1 != null) {
				Logutils.log(className, methodName, logUUid, "  Uuid : " + installHistory1.getUuid(), logger);
			}
			gatewayList.add(installHistory1.getDevice());
		}
		if (installCode == null && utilities.checkInputValue(can)) {
			String canLog = "Can: " + can;
			Logutils.log(className, methodName, context.getLogUUId(),
					" Before calling restUtils.getGatewaysByCANAndStatusFromDeviceService method ", logger, canLog);

			ResponseEntity<Page<Device>> result = restUtils.getGatewaysByCANAndStatusFromDeviceServiceWithPagination(
					logUUid, can, null, page, pageSize, sort, order, null);

			if (result != null) {
				Logutils.log(className, methodName, logUUid, "  result : " + result, logger);
			}

			gatewayList = result.getBody().getContent();
			totalElements = result.getBody().getTotalElements();
			pageable = result.getBody().getPageable();

		}

		if (utilities.checkInputValue(installCodeLog) && utilities.checkInputValue(can)) {
			Logutils.log(className, methodName, context.getLogUUId(),
					" Before calling installHistoryRepository.findByInstallCode method ", logger, installCodeLog);

			InstallHistory installHistory = installHistoryRepository.findByInstallCode(installCode);
			if (installHistory.getOrganisation().getAccountNumber().equalsIgnoreCase(can)) {
				gatewayList.add(installHistory.getDevice());
			} else {
				throw new InstallerException("provided Install Code and Account Number is not matching");
			}
		}
		if (gatewayList.size() > 0) {
			for (Device gat : gatewayList) {
				String productNameLog = "ProductName: " + gat.getProductName();
				Logutils.log(className, methodName, context.getLogUUId(),
						" Inside iterating Gateway LIst to gate product Name ", logger, productNameLog);
				String uuidLog = "Uuid: " + gat.getUuid();
				List<InstallInstructionBean> sensorInstallInstructions = new ArrayList<>();
				Map<String, List<ReasonCodeBean>> reasonCodeBeanMap = new HashMap<>();
				List<AttributeValueResposneDTO> attributeList = new ArrayList<>();
				Logutils.log(className, methodName, context.getLogUUId(),
						" Before calling restUtils.getAttributeListByProductName method ", logger, productNameLog,
						uuidLog);

				List<Attribute> attributes = restUtils.getAttributeListByProductName(logUUid, gat.getProductName(),
						gat.getUuid());

				Logutils.log(className, methodName, context.getLogUUId(),
						" after calling restUtils.getAttributeListByProductName method ", logger, productNameLog,
						uuidLog);
				GatewayDetailsBean gatewatDetails = new GatewayDetailsBean();
				gatewatDetails.setGatewayUuid(gat.getUuid());
				gatewatDetails.setGatewayProductName(gat.getProductName());
				gatewatDetails.setGatewayProductCode(gat.getProductCode());
				// gatewatDetails.setEligibleAssetType(ins.getAsset().getGatewayEligibility());
				if (attributes.size() > 0) {
					Logutils.log(className, methodName, logUUid, "  list of attribute size : " + attributes.size(),
							logger);
					gatewatDetails.set_blocker(attributes.get(0).getProductMaster().isBlocker());
					for (Attribute att : attributes) {
						AttributeValueResposneDTO attRes = new AttributeValueResposneDTO();
						attRes.setApplicable(att.isApplicable());
						attRes.setAttribute_uuid(att.getUuid());
						attRes.setAttributeName(att.getAttributeName());
						attRes.setThresholdValue(att.getAttributeValue());
						attributeList.add(attRes);
					}
				}
				gatewatDetails.setGatewayAttribute(attributeList);
				Logutils.log(className, methodName, context.getLogUUId(),
						" Before calling sensorInstallInstructionRepository.findBySensorProductName method ", logger,
						productNameLog);

				List<SensorInstallInstruction> sensorInstallInstructionList = sensorInstallInstructionRepository
						.findBySensorProductName(gat.getProductName());
				sensorInstallInstructionList.forEach(sensorInstallInstruction -> {
					InstallInstructionBean installInstructionBean = new InstallInstructionBean();
					installInstructionBean
							.setInstruction(sensorInstallInstruction.getInstallInstruction().getInstruction());
					installInstructionBean
							.setSequence(sensorInstallInstruction.getInstallInstruction().getStepSequence());
					sensorInstallInstructions.add(installInstructionBean);
				});
				InstallInstructionComparator comparator = new InstallInstructionComparator();
				Collections.sort(sensorInstallInstructions, comparator);
				gatewatDetails.setGatewayInstallInstructions(sensorInstallInstructions);
				Logutils.log(className, methodName, context.getLogUUId(),
						" Before calling sensorReasonCodeRepository.findBySensorProductName method ", logger,
						productNameLog);

				List<SensorReasonCode> sensorReasonCodeList = sensorReasonCodeRepository
						.findBySensorProductName(gat.getProductName());
				sensorReasonCodeList.forEach(sensorReasonCode -> {
					ReasonCodeBean reasonCodeBean = new ReasonCodeBean();
					reasonCodeBean.setReasonCode(sensorReasonCode.getReasonCode().getCode());
					reasonCodeBean.setDisplayName(sensorReasonCode.getReasonCode().getValue());
					if (reasonCodeBeanMap.get(sensorReasonCode.getReasonCode().getIssueType()) != null) {
						reasonCodeBeanMap.get(sensorReasonCode.getReasonCode().getIssueType()).add(reasonCodeBean);
					} else {
						List<ReasonCodeBean> reasonCodeBeanList = new ArrayList<>();
						reasonCodeBeanList.add(reasonCodeBean);
						reasonCodeBeanMap.put(sensorReasonCode.getReasonCode().getIssueType(), reasonCodeBeanList);
					}
				});
				gatewatDetails.setGatewayReasonCodes(reasonCodeBeanMap);
				gatewatDetailList.add(gatewatDetails);
				InstallInstructionComparator comparator1 = new InstallInstructionComparator();
			}
			if (pageable == null)

				pageable = PageRequest.of(0, 2);
			Page<GatewayDetailsBean> pageOfGatewayDetailsBean = new PageImpl<>(gatewatDetailList, pageable,
					totalElements);

			Logutils.log(className, methodName, logUUid, " Exiting From InstallerService ", logger);
			return pageOfGatewayDetailsBean;
		} else {
			throw new InstallerException("No Gateway for Given Input");
		}
	}

	@Override
	public TpmsSensorCountDTO getSensorCount(String sensorUuid) {
		String methodName = "getSensorCount";
		Context context = new Context();
		String logUUid = context.getLogUUId();
		Logutils.log(className, methodName, logUUid,
				" Inside getSensorCount Method From InstallerService " + "sensorUuid " + sensorUuid, logger);
		TpmsSensorCountDTO tpms = new TpmsSensorCountDTO();
//		List<String> ins = installLogRepository.findDistinctByInstanceType(sensorUuid);
		Integer ins = sensorDetailRepository.getSensorCountBySensorUuid(sensorUuid);
		if (ins != null) {
			Logutils.log(className, methodName, logUUid, "  Uuid : " + ins, logger);
		}
//		count = ins.size();
		tpms.setCount(ins);
		tpms.setUuid(sensorUuid);
		Logutils.log(className, methodName, logUUid, " Exiting From InstallerService ", logger);
		return tpms;
	}

	@Override
	public SensorDetailsResponse getOfflineData(Context context) {
		String methodName = "getOfflineData";
		String logUUid = context.getLogUUId();
		Logutils.log(className, methodName, logUUid, " Inside getOfflineData Method From InstallerService ", logger);
//		List<SensorDetailsBean> sensorDetailsBeanList = new ArrayList<>();
		SensorDetailsResponse sensorDetailsResponse = new SensorDetailsResponse();
		Logutils.log(className, methodName, context.getLogUUId(),
				" Before calling restUtils.getUserFromAuthService method ", logger);
		List<String> productNameList = sensorInstallInstructionRepository.findAllUniqueSensorInstallInstruction();
		List<SensorDetailsBean> sensorDetailsBeanList = null;
		if (productNameList != null && !productNameList.isEmpty()) {
			Logutils.log(className, methodName, logUUid, "  Product Name List are : " + productNameList.size(), logger);
			sensorDetailsBeanList = utilities.getSensorDetailsBeanList(productNameList);
			sensorDetailsResponse.setSensorList(sensorDetailsBeanList);
		}
		Logutils.log(className, methodName, logUUid, " Exiting From InstallerService ", logger);
		return sensorDetailsResponse;
	}

	@Override
	public Boolean updateSensorStatusWithPositioning(UpdateSensorStatusWithInstanceRequest updateSensorStatusRequest,
			String logUUid) {
		String methodName = "updateSensorStatusWithPositioning";
		Logutils.log(className, methodName, logUUid,
				" Inside updateSensorStatusWithPositioning Method From InstallerService "
						+ " UpdateSensorStatusWithInstanceRequest " + updateSensorStatusRequest,
				logger);
		if (updateSensorStatusRequest != null && utilities.checkInputValue(updateSensorStatusRequest.getStatus())
				&& utilities.checkInputValue(updateSensorStatusRequest.getInstallUuid())
				&& utilities.checkInputValue(updateSensorStatusRequest.getDatetimeRT())
				&& utilities.checkInputValue(updateSensorStatusRequest.getSensorUuid())) {

			String installUuidLog = updateSensorStatusRequest.getInstallUuid();
			Logutils.log(logUUid, " Before calling installHistoryRepository.findByInstallCode method  ", logger,
					installUuidLog);
			InstallHistory installHistory = installHistoryRepository
					.findByInstallCode(updateSensorStatusRequest.getInstallUuid());
			String installHistoryIdLog = "InstallHistoryId: ";
			if (installHistory != null) {
				Logutils.log(className, methodName, logUUid,
						"Install History  Uuid : " + installHistory.getInstallCode(), logger);
				installHistoryIdLog = installHistoryIdLog + installHistory.getId();
			}
			Logutils.log(logUUid, " After calling installHistoryRepository.findByInstallCode method  ", logger,
					installHistoryIdLog);

			if (installHistory != null) {
				SensorUpdateRequest sensorUpdateRequest = new SensorUpdateRequest();
				sensorUpdateRequest.setSensorUuid(updateSensorStatusRequest.getSensorUuid());
				sensorUpdateRequest.setUpdatedOn(updateSensorStatusRequest.getDatetimeRT());
				sensorUpdateRequest.setStatus(
						DeviceStatus.getGatewayStatusInSearch(updateSensorStatusRequest.getStatus()).getValue());
				sensorUpdateRequest.setLogUUId(logUUid);
				Device sensor = new Device();
				String sensorIdLog = "SensorId: ";

				if (sensorUpdateRequest.getStatus().equalsIgnoreCase(DeviceStatus.INSTALLED.getValue())) {
					if (updateSensorStatusRequest.getPosition() == null || updateSensorStatusRequest.getPosition()
							.equalsIgnoreCase(SensorPosition.Default.getValue())) {
						Logutils.log(logUUid,
								" Before calling restUtils.updateSensor method status INSTALLED case and position is null ",
								logger);
						sensor = restUtils.updateSensor(sensorUpdateRequest);
						if (sensor != null) {
							Logutils.log(className, methodName, logUUid, "sensor" + sensor.getUuid(), logger);
							sensorIdLog = sensorIdLog + sensor.getId();
						}
						logger.info("sensorIdLog " + sensorIdLog);

						Logutils.log(logUUid, " After calling restUtils.updateSensor method status INSTALLED case",
								logger, sensorIdLog);

					} else {

						sensorUpdateRequest.setStatus(DeviceStatus.PENDING.getValue());
						Logutils.log(logUUid, " Before calling restUtils.updateSensor method  In  Pending status case",
								logger);

						sensor = restUtils.updateSensor(sensorUpdateRequest);
						if (sensor != null) {
							Logutils.log(className, methodName, logUUid, "sensor" + sensor.getUuid(), logger);
							sensorIdLog = sensorIdLog + sensor.getId();
						}
						logger.info("sensorIdLog" + sensor.getId());
						Logutils.log(logUUid, " After calling restUtils.updateSensor method  In  Pending status case",
								logger, sensorIdLog);

					}
				} else {
					Logutils.log(logUUid, " sensor status is not INSTALLED restUtils.updateSensor method  ", logger);
					sensor = restUtils.updateSensor(sensorUpdateRequest);
					if (sensor != null) {
						sensorIdLog = sensorIdLog + sensor.getId();
					}
					logger.debug("sensorIdLog" + sensor.getId());

					Logutils.log(logUUid, " After calling restUtils.updateSensor method  ", logger, sensorIdLog);

				}

				InstallLog installLog = new InstallLog();
				SensorDetail sensorDetail = null;

				if (updateSensorStatusRequest.getPosition() != null) {
					Logutils.log(logUUid, " Before calling sensorDetailRepository.findByIdAndPosition method  ",
							logger);
					sensorDetail = sensorDetailRepository.findByIdAndPosition(updateSensorStatusRequest.getSensorUuid(),
							updateSensorStatusRequest.getPosition());
					Logutils.log(logUUid, " After calling sensorDetailRepository.findByIdAndPosition method  ", logger);
				}

				if (sensorDetail == null) {
					sensorDetail = new SensorDetail();
					String uuid = UUID.randomUUID().toString();
					sensorDetail.setUuid(uuid);

				}

				String sensorId = String.valueOf(sensor.getId());
				sensorDetail.setSensorId(sensorId);
				sensorDetail.setSensorUUID(sensor);
				sensorDetail.setProductCode(sensor.getProductCode());

				Logutils.log(logUUid, " Before calling deviceDeviceXrefRepository.findBySensorUuid method  ", logger);
				Device_Device_xref deviceSensorxref = deviceDeviceXrefRepository.findBySensorUuid(sensor).get(0);
				if (deviceSensorxref != null) {
					Logutils.log(className, methodName, logUUid, "deviceSensorxref  id : " + deviceSensorxref.getId(),
							logger);
				}
				sensorDetail.setDeviceId(deviceSensorxref.getDeviceUuid().getImei());
				installLog.setSensor(sensor);
				if (updateSensorStatusRequest.getStatus().equalsIgnoreCase(DeviceStatus.INSTALLED.getValue())) {
					installLog.setEventType(EventType.SENSOR_INSTALLATION_COMPLETE);
				} else {
					installLog.setEventType(EventType.INSTALLATION);
				}

				installLog.setStatus(InstallLogStatus.getInstallLogStatus(updateSensorStatusRequest.getStatus()));
				sensorDetail.setStatus(updateSensorStatusRequest.getStatus());
				installLog.setInstallHistory(installHistory);
				if (utilities.checkInputValue(updateSensorStatusRequest.getPosition()) && !updateSensorStatusRequest
						.getPosition().equalsIgnoreCase(SensorPosition.Default.getValue())) {
					installLog.setInstanceType(updateSensorStatusRequest.getPosition());
					sensorDetail.setPosition(updateSensorStatusRequest.getPosition());
				} else {
					installLog.setInstanceType(null);
					sensorDetail.setPosition(null);
				}
				if (updateSensorStatusRequest.getSensorId() != null) {
					installLog.setSensorId(updateSensorStatusRequest.getSensorId());
					sensorDetail.setSensorId(updateSensorStatusRequest.getSensorId());
				}

				installLog
						.setTimestamp(Instant.ofEpochMilli(Long.parseLong(updateSensorStatusRequest.getDatetimeRT())));
				installLog.setSensor(sensor);

				if (utilities.checkInputValue(updateSensorStatusRequest.getSensorPressure())) {
					installLog.setSensorPressure(updateSensorStatusRequest.getSensorPressure());
					sensorDetail.setSensorPressure(updateSensorStatusRequest.getSensorPressure());
				}

				if (utilities.checkInputValue(updateSensorStatusRequest.getSensorTemperature())) {
					installLog.setSensorTemperature(updateSensorStatusRequest.getSensorTemperature());
					sensorDetail.setSensorTemperature(updateSensorStatusRequest.getSensorTemperature());
				}
				installLog.setSensorDetail(sensorDetail);

				JwtUser jwtUser = (JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
				User user = restUtils.getUserFromAuthServiceByName(jwtUser.getUsername());
				if (user != null) {
					Logutils.log(className, methodName, logUUid, " User " + user.getUserName(), logger);
				}
				// List<SensorDetail> sensorDetail1=
				// sensorDetailRepository.findListBySensorUuid(sensor.getUuid());
				// if(sensorDetail1!=null && sensorDetail1.size()>0) {
				// Logutils.log(logUUid, " Size of sensor detail : " + sensorDetail1.size(),
				// logger);
				// Instant createTime = sensorDetail1.get(0).getCreatedOn();
				// sensorDetail.setCreatedOn(createTime);
				// sensorDetailRepository.delete(sensorDetail1.get(0));
				// sensorDetail.setUpdatedBy(user);
				// sensorDetail.setUpdatedOn(Instant.now());
				// } else {
				// sensorDetail.setCreatedBy(user);
				// sensorDetail.setCreatedOn(Instant.now());
				// }

				if (sensor.getProductCode().equalsIgnoreCase(ProdCodeName.Door_Sensor.getValue())) {
					Logutils.log(logUUid, " setting value for door sensor type", logger);
					if (updateSensorStatusRequest.getType() != null) {
						installLog.setType(updateSensorStatusRequest.getType());
						sensorDetail.setType(updateSensorStatusRequest.getType());
						Logutils.log(logUUid, " Before calling sensorDetailRepository.save method", logger);
						sensorDetail = sensorDetailRepository.save(sensorDetail);
						installLog.setSensorDetail(sensorDetail);
						Logutils.log(logUUid, " Before calling installLogRepository.save method", logger);
						installLog = installLogRepository.save(installLog);
					}
				} else {
					Logutils.log(logUUid, " Before calling sensorDetailRepository.save method", logger);
					sensorDetail = sensorDetailRepository.save(sensorDetail);
					installLog.setSensorDetail(sensorDetail);
					Logutils.log(logUUid, " Before calling installLogRepository.save method", logger);
					installLog = installLogRepository.save(installLog);
				}

				String installLogIdLog = "InstallLogId: ";
				if (installLog != null) {
					Logutils.log(className, methodName, logUUid, "installLog" + installLog, logger);
					installLogIdLog = installLogIdLog + installLog.getId();
				}
				Logutils.log(logUUid, " After calling installLogRepository.save method", logger, installLogIdLog);

				return true;
			} else {
				logger.error("Exception occurred while updating status for sensor",
						"No InstallHistory found for Install UUID");
				throw new InstallerException("No InstallHistory found for Install UUID");
			}
		} else {
			logger.error("Exception occurred while updating status for Sensor",
					"Install UUID, sensor uuid, status and date can't be null");
			throw new InstallerException("Install UUID, sensor uuid, status and date can't be null");
		}
	}

	@Override
	@Transactional
	public Boolean finishInstall(FinishInstallRequest finishInstallRequest, Long userId) {
		String methodName = "finishInstall";
		Context context = new Context();
		String logUUid = context.getLogUUId();
		Logutils.log(className, methodName, logUUid,
				" Inside finishInstall Method From InstallerService " + " FinishInstallRequest " + finishInstallRequest,
				logger);
		if (finishInstallRequest != null && utilities.checkInputValue(finishInstallRequest.getStatus())
				&& utilities.checkInputValue(finishInstallRequest.getInstallUuid())
				&& utilities.checkInputValue(finishInstallRequest.getDatetimeRT())
				&& utilities.checkInputValue(finishInstallRequest.getReasonCode())) {
			String installUuidLog = "InstallUuid: " + finishInstallRequest.getInstallUuid();
			String reasonCodeLog = "ReasonCode: " + finishInstallRequest.getReasonCode();
			Logutils.log(className, methodName, context.getLogUUId(),
					" finish install method install code value from finish install request = " + installUuidLog, logger,
					installUuidLog);
			Logutils.log(className, methodName, context.getLogUUId(),
					" Before calling findByInstallCode method reason code value ", logger, reasonCodeLog);
			Logutils.log(className, methodName, context.getLogUUId(),
					" Before calling findByInstallCode method status value ", logger, finishInstallRequest.getStatus());
			User user = restUtils.getUserFromAuthService("", userId);
			InstallHistory installHistory = installHistoryRepository
					.findByInstallCode(finishInstallRequest.getInstallUuid());
			String installHistoryIdLog = "";
			if (installHistory != null) {
				Logutils.log(className, methodName, logUUid, "installHistory  Uuid : " + installHistory.getUuid(),
						logger);
				installHistoryIdLog = "InstallHistoryId: " + installHistory.getId();
			}
			Logutils.log(className, methodName, context.getLogUUId(), " After calling findByInstallCode", logger,
					installHistoryIdLog);

			if (installHistory != null) {
				Logutils.log(className, methodName, context.getLogUUId(),
						" Before calling findByInstallUuidOrderByTimeStampDesc method ", logger, installUuidLog);

				List<InstallLog> installLogList = installLogRepository
						.findByInstallUuidOrderByTimeStampDesc(finishInstallRequest.getInstallUuid());
				Logutils.log(className, methodName, context.getLogUUId(),
						" After calling findByInstallUuidOrderByTimeStampDesc method ", logger);
				String installLogIdLog = "InstallLogId:";

				InstallLog updateInstallLog = null;
				if (installLogList.size() > 0) {
					Logutils.log(className, methodName, logUUid, "size from installLogList  : " + installLogList.size(),
							logger);
					updateInstallLog = installLogList.get(0);
				}

				if (installHistory.getStatus().equals(InstallHistoryStatus.FINISHED)
						|| installHistory.getStatus().equals(InstallHistoryStatus.PROBLEM)) {
					InstallLog installLog = new InstallLog();
					installLog.setEventType(EventType.REJECTED_UPDATE);
					installLog.setStatus(InstallLogStatus.REJECTED);
					installLog.setInstallHistory(installHistory);
					Logutils.log(className, methodName, context.getLogUUId(),
							" Before calling installLogRepository.save method InstallHistory Finished OR Problem case ",
							logger);

					installLog = installLogRepository.save(installLog);
					if (installLog != null) {
						installLogIdLog = installLogIdLog + installLog.getId();
					}
					Logutils.log(className, methodName, context.getLogUUId(),
							" After calling installLogRepository.save method InstallHistory Finished OR Problem case",
							logger, installLogIdLog);

					installLog.setTimestamp(Instant.ofEpochMilli(Long.parseLong(finishInstallRequest.getDatetimeRT())));
					return false;

				} else if (updateInstallLog != null && installHistory.getStatus().equals(InstallHistoryStatus.STARTED)
						&& updateInstallLog.getTimestamp().toEpochMilli() > Long
								.parseLong(finishInstallRequest.getDatetimeRT())) {
					InstallLog installLog = new InstallLog();
					installLog.setEventType(EventType.REJECTED_UPDATE);
					installLog.setStatus(InstallLogStatus.REJECTED);
					installLog.setInstallHistory(installHistory);
					installLog.setTimestamp(Instant.ofEpochMilli(Long.parseLong(finishInstallRequest.getDatetimeRT())));
					Logutils.log(className, methodName, context.getLogUUId(),
							" Before calling installLogRepository.save method update case", logger);

					installLog = installLogRepository.save(installLog);
					if (installLog != null) {
						installLogIdLog = installLogIdLog + installLog.getId();
					}
					Logutils.log(className, methodName, context.getLogUUId(),
							" After calling installLogRepository.save method update case", logger, installLogIdLog);

					return false;
				}
				installHistory.setDateEnded(Instant.ofEpochMilli(Long.parseLong(finishInstallRequest.getDatetimeRT())));
				installHistory.setUpdatedOn(Instant.now());
				installHistory
						.setStatus(InstallHistoryStatus.getInstallHistoryStatus(finishInstallRequest.getStatus()));
				Logutils.log(className, methodName, context.getLogUUId(),
						" Before calling installHistoryRepository.save method ", logger);
				installHistory.setUpdatedBy(user);
				installHistory = installHistoryRepository.save(installHistory);
				installHistoryRepository.flush();
				if (installHistory != null) {
					installHistoryIdLog = "InstallHistoryId: " + installHistory.getId();
				} else {
					installHistoryIdLog = "";
				}
				Logutils.log(className, methodName, context.getLogUUId(),
						" After calling installHistoryRepository.save method ", logger, installHistoryIdLog);

				InstallLog installLog = new InstallLog();
				installLog.setEventType(EventType.INSTALLATION_COMPLETE);
				installLog.setStatus(InstallLogStatus.getInstallLogStatus(finishInstallRequest.getStatus()));
				installLog.setInstallHistory(installHistory);
				String issueTypeLog = "IssueType: INSTALLATION";
				Logutils.log(className, methodName, context.getLogUUId(),
						" Before calling reasonCodeRepository.findByCodeAndIssueType method  ", logger, reasonCodeLog,
						issueTypeLog);

				ReasonCode reasonCode = reasonCodeRepository
						.findByCodeAndIssueType(finishInstallRequest.getReasonCode(), "INSTALLATION");
				String reasonCodeIdLog = "ReasonCodeId: ";
				if (reasonCode != null) {
					Logutils.log(className, methodName, logUUid, "reasonCode  Uuid : " + reasonCode.getUuid(), logger);
					reasonCodeIdLog = reasonCodeIdLog + reasonCode.getId();
					Logutils.log(className, methodName, context.getLogUUId(),
							" Reason code found for the given reason code in finish install method  ", logger,
							reasonCodeLog, reasonCodeIdLog);

				}
				Logutils.log(className, methodName, context.getLogUUId(),
						" After calling reasonCodeRepository.findByCodeAndIssueType method  ", logger, reasonCodeIdLog);

				installLog.setReasonCode(reasonCode);
				installLog.setTimestamp(Instant.ofEpochMilli(Long.parseLong(finishInstallRequest.getDatetimeRT())));
				Logutils.log(className, methodName, context.getLogUUId(),
						" Before calling installLogRepository.save method  ", logger);

				installLog = installLogRepository.save(installLog);
				installLogRepository.flush();
				Logutils.log(className, methodName, context.getLogUUId(),
						" After calling installLogRepository.save method  ", logger);

				// Gateway gateway = installHistory.getGateway();
				Device device = installHistory.getDevice();
				if (installHistory.getStatus().equals(InstallHistoryStatus.FINISHED)
						|| installHistory.getStatus().equals(InstallHistoryStatus.ACTIVE_WITH_MINOR_ISSUE)) {
					device.setStatus(DeviceStatus.ACTIVE);
					device.setInstallationDate(
							Instant.ofEpochMilli(Long.parseLong(finishInstallRequest.getDatetimeRT())));

				} else if (installHistory.getStatus().equals(InstallHistoryStatus.PROBLEM)) {
					device.setStatus(DeviceStatus.PROBLEM);
				}
				device.setUpdatedOn(Instant.ofEpochMilli(Long.parseLong(finishInstallRequest.getDatetimeRT())));
				if (installHistory.getAsset() != null && installHistory.getAsset().getId() != null) {
					installHistory.getAsset().setStatus(AssetStatus.ACTIVE);
					installHistory.getAsset().setInstallationDate(
							Instant.ofEpochMilli(Long.parseLong(finishInstallRequest.getDatetimeRT())));
					installHistory.getAsset().setInstallationDate(
							Instant.ofEpochMilli(Long.parseLong(finishInstallRequest.getDatetimeRT())));
					installHistory.getAsset()
							.setUpdatedOn(Instant.ofEpochMilli(Long.parseLong(finishInstallRequest.getDatetimeRT())));

					UpdateGatewayAssetStatusRequest updateGatewayAssetStatusRequest = new UpdateGatewayAssetStatusRequest(
							device.getUuid(), installHistory.getAsset().getUuid(), DeviceStatus.INSTALLED,
							AssetStatus.ACTIVE, logUUid);
					Logutils.log(className, methodName, context.getLogUUId(),
							" Before calling restUtils.updateGatewayAndAssetStatus method  ", logger);
					updateGatewayAssetStatusRequest.setLogUUId(logUUid);
					device = restUtils.updateGatewayAndAssetStatus(updateGatewayAssetStatusRequest);
				}
				String gatewayIdLog = "GatewayId: ";
				if (device != null) {
					gatewayIdLog = gatewayIdLog + device.getId();
					Logutils.log(className, methodName, context.getLogUUId(),
							" gateway found after restutil call to update gateway status method  ", logger,
							gatewayIdLog);

				}
				Logutils.log(className, methodName, context.getLogUUId(),
						" After calling restUtils.updateGatewayAndAssetStatus method  ", logger, gatewayIdLog);

				try {
					Logutils.log(className, methodName, context.getLogUUId(),
							" Before calling restUtils.updateAssetToDeviceInMS method  ", logger);

//					Long recordId = restUtils.updateAssetToDeviceInMS(logUUid, installHistory);
					Long recordId = 1L;
					String recordIdLog = "RecordId: ";
					if (recordId != null) {
						recordIdLog = recordIdLog + recordId;
					}
					Logutils.log(className, methodName, context.getLogUUId(),
							" After calling restUtils.updateAssetToDeviceInMS method  ", logger, recordIdLog);

					logger.info(
							"Status updated in MS for AssetToDevice for assetId = {} and imei = {} for record id = {}",
							installHistory.getAsset().getAssignedName(), installHistory.getDevice().getImei(),
							recordId);
					restUtils.sendFinishInstallToElastic(logUUid, installHistory.getDevice().getImei(),
							installHistory.getAsset().getAssignedName(), installHistory.getAsset().getVin());
					Logutils.log(className, methodName, context.getLogUUId(),
							" After calling  sendFinishInstallToElastic METHOD ", logger, recordIdLog);
					Logutils.log(className, methodName, context.getLogUUId(), " Finish installation completed ",
							logger);
				} catch (InterServiceRestException e) {
					logger.error("Error occurred while updating AssetToDevice in MS - ", e);
				}

				/*
				 * Logutils.log(className,methodName,context.getLogUUId()
				 * ," Call method : callSendUDPCommandAPI of restUtils ",logger); Boolean flag =
				 * restUtils.callSendUDPCommandAPI(gateway.getImei());
				 * Logutils.log(className,methodName,context.getLogUUId()
				 * ,"After Calling method : callSendUDPCommandAPI of restUtils and flag is : " +
				 * flag,logger);
				 */
				Logutils.log(className, methodName, logUUid, " Exiting From InstallerService ", logger);
				return true;
			} else {
				logger.error("Exception occurred while marking install as finished",
						"No InstallHistory found for Install UUID");
				throw new InstallerException("No InstallHistory found for Install UUID");
			}
		} else if (finishInstallRequest.getReasonCode() == null) {
			logger.error("Exception occurred while marking install as finished", "Reason code can't be null");
			throw new InstallerException("No Reason Code Selected/Please select a reason code and try again.");
		} else {
			logger.error("Exception occurred while marking install as finished",
					"Install UUID, status and date can't be null");
			throw new InstallerException("Install UUID, status and date can't be null");
		}
	}
	// ---------------------------------Aamir
	// End-------------------------------------//
//	@Override
//	@Transactional
//    public Boolean finishInstall(FinishInstallRequest finishInstallRequest, Context context, Long userId) {
////        logger.info("Inside finishInstall Method From InstallerService");
////        String methodName ="finishInstall";
////        if (finishInstallRequest != null &&
////                finishInstallRequest.getStatus() != null && !finishInstallRequest.getStatus().isEmpty() &&
////                finishInstallRequest.getInstallUuid() != null && !finishInstallRequest.getInstallUuid().isEmpty() &&
////                finishInstallRequest.getDatetimeRT() != null && !finishInstallRequest.getDatetimeRT().isEmpty() &&
////                finishInstallRequest.getReasonCode() != null) {
////            String installUuidLog="InstallUuid: "+finishInstallRequest.getInstallUuid();
////            String reasonCodeLog="ReasonCode: " + finishInstallRequest.getReasonCode();
////            Logutils.log(className,methodName,context.getLogUUId()," finish install method install code value from finish install request = "+installUuidLog,logger,installUuidLog);
////            Logutils.log(className,methodName,context.getLogUUId()," Before calling findByInstallCode method reason code value ",logger,reasonCodeLog);
////            Logutils.log(className,methodName,context.getLogUUId()," Before calling findByInstallCode method status value ",logger,finishInstallRequest.getStatus());
////            
////            User user = restUtils.getUserFromAuthService(userId);
////            
////            InstallHistory installHistory = installHistoryRepository.findByInstallCode(finishInstallRequest.getInstallUuid());
////            
////            String installHistoryIdLog="";
////            if(installHistory !=null){
////                
////            	installHistoryIdLog="InstallHistoryId: "+installHistory.getId();
////           
////            }
////            Logutils.log(className,methodName,context.getLogUUId()," After calling findByInstallCode",logger,installHistoryIdLog);
////
////            if (installHistory != null) {
////                Logutils.log(className,methodName,context.getLogUUId()," Before calling findByInstallUuidOrderByTimeStampDesc method ",logger,installUuidLog);
////
////                List<InstallLog> installLogList = installLogRepository.findByInstallUuidOrderByTimeStampDesc(finishInstallRequest.getInstallUuid());
////               
////                Logutils.log(className,methodName,context.getLogUUId()," After calling findByInstallUuidOrderByTimeStampDesc method ",logger);
////                
////                String installLogIdLog = "InstallLogId:";
////
////                InstallLog updateInstallLog = null;
////               
////                if(installLogList.size() > 0)
////                    updateInstallLog=installLogList.get(0);
////                if (installHistory
////                        .getStatus()
////                        .equals(InstallHistoryStatus.FINISHED) || installHistory
////                        .getStatus()
////                        .equals(InstallHistoryStatus.PROBLEM)) {
////                    InstallLog installLog = new InstallLog();
////                    installLog.setEventType(EventType.REJECTED_UPDATE);
////                     installLog.setStatus(InstallLogStatus.REJECTED);
////                    installLog.setInstallHistory(installHistory);
////                    Logutils.log(className,methodName,context.getLogUUId()," Before calling installLogRepository.save method InstallHistory Finished OR Problem case ",logger);
////
////                    installLog = installLogRepository.save(installLog);
////                    if(installLog != null) {
////                        installLogIdLog =installLogIdLog + installLog.getId();
////                    }
////                    Logutils.log(className,methodName,context.getLogUUId()," After calling installLogRepository.save method InstallHistory Finished OR Problem case",logger,installLogIdLog);
////
////                    installLog.setTimestamp(Instant.ofEpochMilli(Long.parseLong(finishInstallRequest.getDatetimeRT())));
////                    return false;
////
////                } else if (updateInstallLog !=null && installHistory
////                        .getStatus()
////                        .equals(InstallHistoryStatus.STARTED) && updateInstallLog.getTimestamp().toEpochMilli() > Long.parseLong(finishInstallRequest.getDatetimeRT()) ){
////                    InstallLog installLog = new InstallLog();
////                    installLog.setEventType(EventType.REJECTED_UPDATE);
////                    installLog.setStatus(InstallLogStatus.REJECTED);
////                    installLog.setInstallHistory(installHistory);
////                    installLog.setTimestamp(Instant.ofEpochMilli(Long.parseLong(finishInstallRequest.getDatetimeRT())));
////                    Logutils.log(className,methodName,context.getLogUUId()," Before calling installLogRepository.save method update case",logger);
////
////                    installLog = installLogRepository.save(installLog);
////                    if(installLog != null) {
////                        installLogIdLog =installLogIdLog + installLog.getId();
////                    }
////                    Logutils.log(className,methodName,context.getLogUUId()," After calling installLogRepository.save method update case",logger,installLogIdLog);
////
////                    return false;
////                }
////                installHistory.setDateEnded(Instant.ofEpochMilli(Long.parseLong(finishInstallRequest.getDatetimeRT())));
////                installHistory.setUpdatedOn(Instant.now());
////                installHistory.setStatus(InstallHistoryStatus.getInstallHistoryStatus(finishInstallRequest.getStatus()));
////                Logutils.log(className,methodName,context.getLogUUId()," Before calling installHistoryRepository.save method ",logger);
////                installHistory.setUpdatedBy(user);
////                installHistory = installHistoryRepository.save(installHistory);
////                installHistoryRepository.flush();
////                if(installHistory !=null){
////                    installHistoryIdLog="InstallHistoryId: "+installHistory.getId();
////                }else{
////                    installHistoryIdLog="";
////                }
////                Logutils.log(className,methodName,context.getLogUUId()," After calling installHistoryRepository.save method ",logger,installHistoryIdLog);
////
////                InstallLog installLog = new InstallLog();
////                installLog.setEventType(EventType.INSTALLATION_COMPLETE);
////                installLog.setStatus(InstallLogStatus.getInstallLogStatus(finishInstallRequest.getStatus()));
////                installLog.setInstallHistory(installHistory);
////                String issueTypeLog="IssueType: INSTALLATION";
////                Logutils.log(className,methodName,context.getLogUUId()," Before calling reasonCodeRepository.findByCodeAndIssueType method  ",logger,reasonCodeLog,issueTypeLog);
////
////                ReasonCode reasonCode = reasonCodeRepository.findByCodeAndIssueType(finishInstallRequest.getReasonCode(), "INSTALLATION");
////                String reasonCodeIdLog="ReasonCodeId: ";
////                if(reasonCode != null){
////                    reasonCodeIdLog= reasonCodeIdLog + reasonCode.getId();
////                	Logutils.log(className,methodName,context.getLogUUId()," Reason code found for the given reason code in finish install method  ",logger,reasonCodeLog,reasonCodeIdLog);
////
////                }
////                Logutils.log(className,methodName,context.getLogUUId()," After calling reasonCodeRepository.findByCodeAndIssueType method  ",logger,reasonCodeIdLog);
////
////                installLog.setReasonCode(reasonCode);
////                installLog.setTimestamp(Instant.ofEpochMilli(Long.parseLong(finishInstallRequest.getDatetimeRT())));
////                Logutils.log(className,methodName,context.getLogUUId()," Before calling installLogRepository.save method  ",logger);
////
////                installLog = installLogRepository.save(installLog);
////                installLogRepository.flush();
////                Logutils.log(className,methodName,context.getLogUUId()," After calling installLogRepository.save method  ",logger);
////
////                Device device = installHistory.getDevice();
////                if (installHistory.getStatus().equals(InstallHistoryStatus.FINISHED) || installHistory.getStatus().equals(InstallHistoryStatus.ACTIVE_WITH_MINOR_ISSUE)) {
////                	device.setStatus(DeviceStatus.ACTIVE);
////                } else if (installHistory.getStatus().equals(InstallHistoryStatus.PROBLEM)) {
////                    device.setStatus(DeviceStatus.PROBLEM);
////                }
////                device.setUpdatedOn(Instant.ofEpochMilli(Long.parseLong(finishInstallRequest.getDatetimeRT())));
////                //sonu
//////                device.getAsset().setStatus(AssetStatus.ACTIVE);
//////                device.getAsset().setUpdatedOn(Instant.ofEpochMilli(Long.parseLong(finishInstallRequest.getDatetimeRT())));
//////                UpdateDeviceAssetStatusRequest updateDeviceAssetStatusRequest = new UpdateDeviceAssetStatusRequest(device.getUuid(), device.getAsset().getUuid(), DeviceStatus.INSTALLED, AssetStatus.ACTIVE);
////                Logutils.log(className,methodName,context.getLogUUId()," Before calling restUtils.updateGatewayAndAssetStatus method  ",logger);
////
////                device = restUtils.updateDeviceAndAssetStatus(updateDeviceAssetStatusRequest);
////                String gatewayIdLog="GatewayId: ";
////                if(device !=null) {
////                    gatewayIdLog = gatewayIdLog + device.getId();
////                    Logutils.log(className,methodName,context.getLogUUId()," gateway found after restutil call to update gateway status method  ",logger,gatewayIdLog);
////
////                }
////                Logutils.log(className,methodName,context.getLogUUId()," After calling restUtils.updateGatewayAndAssetStatus method  ",logger, gatewayIdLog);
////
////                try {
////                    Logutils.log(className,methodName,context.getLogUUId()," Before calling restUtils.updateAssetToDeviceInMS method  ",logger);
////
////                    Long recordId = restUtils.updateAssetToDeviceInMS(installHistory);
////                    String recordIdLog="RecordId: ";
////                    if(recordId != null){
////                        recordIdLog =recordIdLog + recordId;
////                    }
////                    Logutils.log(className,methodName,context.getLogUUId()," After calling restUtils.updateAssetToDeviceInMS method  ",logger,recordIdLog);
////
////                    logger.info("Status updated in MS for AssetToDevice for assetId = {} and imei = {} for record id = {}",
////                            installHistory.getAsset().getAssignedName(), installHistory.getDevice().getImei(), recordId);
////                    restUtils.sendFinishInstallToElastic(installHistory.getDevice().getImei(),
////                            installHistory.getAsset().getAssignedName(),
////                            installHistory.getAsset().getVin());
////                    Logutils.log(className,methodName,context.getLogUUId()," After calling  sendFinishInstallToElastic METHOD ",logger,recordIdLog);
////                    Logutils.log(className,methodName,context.getLogUUId()," Finish installation completed ",logger);
////                } catch (InterServiceRestException e) {
////                    logger.error("Error occurred while updating AssetToDevice in MS - ", e);
////                }
////                
////				/*Logutils.log(className,methodName,context.getLogUUId()," Call method : callSendUDPCommandAPI of restUtils ",logger);
////				Boolean flag = restUtils.callSendUDPCommandAPI(gateway.getImei());
////				Logutils.log(className,methodName,context.getLogUUId(),"After Calling method : callSendUDPCommandAPI of restUtils and flag is : " + flag,logger);*/
////
////                return true;
////            } else {
////                logger.error("Exception occurred while marking install as finished", "No InstallHistory found for Install UUID");
////                throw new InstallerException("No InstallHistory found for Install UUID");
////            }
////        } else if (finishInstallRequest.getReasonCode() == null) {
////            logger.error("Exception occurred while marking install as finished", "Reason code can't be null");
////            throw new InstallerException("No Reason Code Selected/Please select a reason code and try again.");
////        } else {
////            logger.error("Exception occurred while marking install as finished", "Install UUID, status and date can't be null");
////            throw new InstallerException("Install UUID, status and date can't be null");
////        }
//		
//		return true;
//    }

//    @Transactional
//    private AutoresetResponse checkIsAutoResetInstallationIsApplicableForCurrentInstallation(InstallHistory installHistory,Context context) {
//    	AutoresetResponse autoresetResponse = new AutoresetResponse();
//    	Boolean isAutoResetInstallation = false;
//    	Boolean isResetInstallationDone = false;
//		logger.info("checking auto reset value for company id : " + installHistory.getCompany().getId());
//		isAutoResetInstallation = restUtils.checkAutoResetValueForCompany(installHistory.getCompany().getId());
//		logger.info("value of isAutoResetInstallation : " + isAutoResetInstallation);
//		if(isAutoResetInstallation) {
//			logger.info("value of isAutoResetInstallation : " + isAutoResetInstallation);
//			String deviceId = null;
//			if(installHistory.getGateway().getType().equals(GatewayType.BEACON)) {
//				deviceId = installHistory.getGateway().getMacAddress();
//			} else if(installHistory.getGateway().getType().equals(GatewayType.GATEWAY)) {
//				deviceId = installHistory.getGateway().getImei();
//			}
//			logger.info("value of deviceId : " + deviceId);
//			logger.info("Calling resetInstallation method");
//			isResetInstallationDone = resetInstallation(installHistory.getAsset().getVin(), deviceId, installHistory.getCompany().getAccountNumber(), context);
//			logger.info("value of isResetInstallationDone : " + isResetInstallationDone);
//			if(isResetInstallationDone) {
//				logger.info("Reset Installation done successfully for compnay : " + installHistory.getCompany().getAccountNumber() + " installation id : " + installHistory.getUuid());
//			}
//			autoresetResponse.setStatus(isResetInstallationDone);
//			autoresetResponse.setDeviceId(deviceId);
//		}
//	
//		return autoresetResponse;
//    }
//
	@Override
	public Boolean updateSensorStatus(UpdateSensorStatusRequest updateSensorStatusRequest) {
		Context context = new Context();
		String logUUId = context.getLogUUId();
		String methodName = "updateSensorStatus";
		String installUuidLog = "InstallUuid: " + updateSensorStatusRequest.getInstallUuid();
		Logutils.log(className, methodName, logUUId, " Inside updateSensorStatus Method From InstallerService "
				+ " UpdateSensorStatusRequest " + updateSensorStatusRequest, logger);
		if (updateSensorStatusRequest != null && updateSensorStatusRequest.getStatus() != null
				&& !updateSensorStatusRequest.getStatus().isEmpty()
				&& updateSensorStatusRequest.getInstallUuid() != null
				&& !updateSensorStatusRequest.getInstallUuid().isEmpty()
				&& updateSensorStatusRequest.getDatetimeRT() != null
				&& !updateSensorStatusRequest.getDatetimeRT().isEmpty()
				&& updateSensorStatusRequest.getSensorUuid() != null
				&& !updateSensorStatusRequest.getSensorUuid().isEmpty()) {

			Logutils.log(className, methodName, context.getLogUUId(),
					" Before calling installHistoryRepository.findByInstallCode method  ", logger, installUuidLog);
			InstallHistory installHistory = installHistoryRepository
					.findByInstallCode(updateSensorStatusRequest.getInstallUuid());
			String installHistoryIdLog = "InstallHistoryId: ";
			if (installHistory != null) {
				installHistoryIdLog = installHistoryIdLog + installHistory.getId();
			}
			Logutils.log(className, methodName, context.getLogUUId(),
					" After calling installHistoryRepository.findByInstallCode method  ", logger, installHistoryIdLog);

			if (installHistory != null) {
				SensorUpdateRequest sensorUpdateRequest = new SensorUpdateRequest();
				sensorUpdateRequest.setSensorUuid(updateSensorStatusRequest.getSensorUuid());
				sensorUpdateRequest.setUpdatedOn(updateSensorStatusRequest.getDatetimeRT());
				sensorUpdateRequest
						.setStatus(SensorStatus.getSensorStatus(updateSensorStatusRequest.getStatus()).getValue());
//                Device sensor = restUtils.updateSensor(sensorUpdateRequest);
				Device sensor = updateSensor(sensorUpdateRequest);
				InstallLog installLog = new InstallLog();
				String sensorStatus = sensor.getStatus().getValue();
				if (sensorStatus.equalsIgnoreCase(SensorStatus.INSTALLED.getValue())) {
					installLog.setEventType(EventType.SENSOR_INSTALLATION_COMPLETE);
				} else {
					installLog.setEventType(EventType.INSTALLATION);
				}
				installLog.setStatus(InstallLogStatus.getInstallLogStatus(updateSensorStatusRequest.getStatus()));
				installLog.setInstallHistory(installHistory);
				installLog
						.setTimestamp(Instant.ofEpochMilli(Long.parseLong(updateSensorStatusRequest.getDatetimeRT())));
				installLog.setSensor(sensor);
				Logutils.log(className, methodName, context.getLogUUId(),
						" Before calling installLogRepository.save method  ", logger);

				installLog = installLogRepository.save(installLog);
				String installLogIdLog = "InstallLogId: ";
				if (installLog != null) {
					installLogIdLog = installHistoryIdLog + installLog.getId();
				}

				Logutils.log(className, methodName, context.getLogUUId(),
						" After calling installLogRepository.save method  ", logger, installLogIdLog);

				return true;
			} else {
				logger.error("Exception occurred while updating status for sensor",
						"No InstallHistory found for Install UUID");
				throw new InstallerException("No InstallHistory found for Install UUID");
			}
		} else {
			logger.error("Exception occurred while updating status for Sensor",
					"Install UUID, sensor uuid, status and date can't be null");
			throw new InstallerException("Install UUID, sensor uuid, status and date can't be null");
		}
	}

	private Device updateSensor(SensorUpdateRequest sensorUpdateRequest) {
		String methodName = "updateSensor";
		Logutils.log(className, methodName,
				" Inside updateSensor Method From InstallerService " + " SensorUpdateRequest " + sensorUpdateRequest,
				logger);

		Logutils.log(className, methodName, " Before calling deviceRepository.findByUuid method  ", logger);
		Device sensor = deviceRepository.findByUuid(sensorUpdateRequest.getSensorUuid());
		if (sensor != null && sensor.getStatus() != null) {
			sensor.setUpdatedOn(Instant.ofEpochMilli(Long.parseLong(sensorUpdateRequest.getUpdatedOn())));
			sensor.setStatus(DeviceStatus.getGatewayStatusInSearch(sensorUpdateRequest.getStatus()));
			Logutils.log(className, methodName, " Before calling deviceRepository.save method  ", logger);
			sensor = deviceRepository.save(sensor);
		}
		Logutils.log(className, methodName,
				" Before calling deviceDeviceXrefRepository.findSensorBySensorUuid method  ", logger);
		Device_Device_xref deviceSensorxref = deviceDeviceXrefRepository.findSensorBySensorUuid(sensor);

		if (deviceSensorxref != null && deviceSensorxref.getDeviceUuid() != null) {
			Device gateway = deviceSensorxref.getDeviceUuid();
			gateway.setLastPerformAction(Constants.GATEWAYS_ACTION_UPDATED);
			gateway.setTimeOfLastDownload(Instant.now());
			deviceRepository.save(gateway);
		}
		Logutils.log(className, methodName, " Exiting From InstallerService ", logger);
		return sensor;

	}

//

	@Override
	public Boolean updateGatewayInstallStatus(InstallationStatusGatewayRequest installationStatusGatewayRequest) {
		Context ctx = new Context();
		String logUUid = ctx.getLogUUId();
		String methodName = "updateGatewayInstallStatus";
//		String logUUid = installationStatusGatewayRequest.getLogUUId();
		Logutils.log(className, methodName, logUUid, " Inside updateGatewayInstallStatus Method From InstallerService "
				+ " InstallationStatusGatewayRequest " + installationStatusGatewayRequest, logger);
		if (installationStatusGatewayRequest != null
				&& utilities.checkInputValue(installationStatusGatewayRequest.getStatus())
				&& utilities.checkInputValue(installationStatusGatewayRequest.getInstallUuid())
				&& utilities.checkInputValue(installationStatusGatewayRequest.getDatetimeRt())
				&& utilities.checkInputValue(installationStatusGatewayRequest.getGatewayUuid())) {

			String installUuidLog = "InstallUuid: " + installationStatusGatewayRequest.getInstallUuid();
			String gatewayUuidLog = "GatewayUuid: " + installationStatusGatewayRequest.getGatewayUuid();
			String statusLog = installationStatusGatewayRequest.getStatus();
			Logutils.log(logUUid, " Before calling restUtils.updateGatewayInstallStatus method ", logger,
					installUuidLog, gatewayUuidLog, statusLog);

			// Device device =
			// restUtils.updateGatewayStatusAndDate(installationStatusGatewayRequest);
			Device device = updateGatewayStatusAndDate(installationStatusGatewayRequest);
			String gatewayIdLog = "GatewayId: ";
			if (device != null) {
				gatewayIdLog = gatewayIdLog + device.getId();
			}
			Logutils.log(className, methodName, logUUid, " After calling restUtils.updateGatewayInstallStatus method ",
					logger, gatewayIdLog);

			InstallLog installLog = new InstallLog();
			installLog.setStatus(InstallLogStatus.getInstallLogStatus(installationStatusGatewayRequest.getStatus()));
			installLog.setTimestamp(
					Instant.ofEpochMilli(Long.parseLong(installationStatusGatewayRequest.getDatetimeRt())));

			Logutils.log(className, methodName, logUUid,
					" Before calling installHistoryRepository.findByInstallCode method ", logger, installUuidLog);
			InstallHistory installHistory = installHistoryRepository
					.findByInstallCode(installationStatusGatewayRequest.getInstallUuid());
			String installHistoryIdLog = "InstallHistoryId: ";
			if (installHistory != null) {
				installHistoryIdLog = installHistoryIdLog + installHistory.getId();
			}

			Logutils.log(className, methodName, logUUid,
					" After calling installHistoryRepository.findByInstallCode method ", logger, installHistoryIdLog);

			installLog.setInstallHistory(installHistory);
			String deviceStatus = device.getStatus().getValue();
			if (deviceStatus.equalsIgnoreCase(GatewayStatus.INSTALLED.getValue())) {
				installLog.setEventType(EventType.GATEWAY_INSTALLATION_COMPLETE);
			} else {
				installLog.setEventType(EventType.INSTALLATION);
			}
			installLog.setData(installationStatusGatewayRequest.getData());
			Logutils.log(className, methodName, logUUid, " Before calling installLogRepository.save method ", logger);
			installLog = installLogRepository.save(installLog);
			String installLogIdLog = "InstallLogId: ";
			if (installLog != null) {
				installLogIdLog = installLogIdLog + installLog.getId();
			}
			Logutils.log(className, methodName, logUUid, " After calling installLogRepository.save method ", logger,
					installLogIdLog);
			Logutils.log(className, methodName, logUUid, " Exiting From InstallerService ", logger);
			return true;
		} else {
			logger.error("Exception occurred while updating status for Sensor",
					"Install UUID, gateway uuid, status and date can't be null");
			throw new InstallerException("Install UUID, gateway uuid, status and date can't be null");
		}
	}

	private Device updateGatewayStatusAndDate(InstallationStatusGatewayRequest installationStatusGatewayRequest) {
		String methodName = "updateGatewayStatusAndDate";
		Logutils.log(className, methodName, " Inside updateGatewayStatusAndDate Method From InstallerService "
				+ " InstallationStatusGatewayRequest " + installationStatusGatewayRequest, logger);
		Logutils.log(className, methodName, " Before calling deviceRepository.findByUuid method ",
				" GatewayUuid value : " + installationStatusGatewayRequest.getGatewayUuid(), logger);
		Device device = deviceRepository.findByUuid(installationStatusGatewayRequest.getGatewayUuid());

		if (installationStatusGatewayRequest.getDatetimeRt() != null) {
			logger.info("DatetimeRt value :" + installationStatusGatewayRequest.getDatetimeRt());
			device.setUpdatedAt(Instant.ofEpochMilli(Long.parseLong(installationStatusGatewayRequest.getDatetimeRt())));
		} else {
			device.setUpdatedAt(Instant.now());
		}
		device.setLastPerformAction(Constants.GATEWAYS_ACTION_UPDATED);
		device.setTimeOfLastDownload(Instant.now());
		Logutils.log(className, methodName, " Before calling deviceRepository.save method ", logger);

		device = deviceRepository.save(device);
		return device;

	}

	@Override
	public InstallationStatusResponse getInstallationStatus(String installUuid) {
		Context context = new Context();
		String methodName = "getInstallationStatus";
		String logUUid = context.getLogUUId();
		Logutils.log(className, methodName, context.getLogUUId(),
				" Inside getInstallationStatus Method From InstallerService " + "installUuid " + installUuid, logger);
		if (utilities.checkInputValue(installUuid)) {
			String installUuidLog = "InstallUuid: " + installUuid;

			Logutils.log(className, methodName, context.getLogUUId(),
					" Before calling installHistoryRepository.findByInstallCode method ", logger, installUuidLog);
			InstallHistory installHistory = installHistoryRepository.findByInstallCode(installUuid);
			String installHistoryIdLog = "InstallHistoryId: ";
			if (installHistory != null) {

				Logutils.log(className, methodName, logUUid, "  installHistory : " + installHistory, logger);
				installHistoryIdLog = installHistoryIdLog + installHistory.getId();
			}
			Logutils.log(className, methodName, context.getLogUUId(),
					" After calling installHistoryRepository.findByInstallCode method ", logger, installHistoryIdLog);

			Logutils.log(className, methodName, context.getLogUUId(),
					" Before calling beanConverter.createInstallationStatusResponseFromInstallHistory method ", logger);

			InstallationStatusResponse installationStatusResponse = beanConverter
					.createInstallationStatusResponseFromInstallHistory(installHistory);
			Logutils.log(className, methodName, context.getLogUUId(), " Exiting From InstallerService ", logger);
			return installationStatusResponse;
		} else {
			logger.error("Exception occurred while updating status for Sensor", "Install UUID can't be null");
			throw new InstallerException("Install UUID can't be null");
		}
	}

	private Boolean deleteAssetByUUID(String logUUid, Asset asset) {

		String methodName = "deleteAssetByUUID";
		Logutils.log(className, methodName, logUUid,
				" Inside deleteAssetByUUID Method From InstallerService " + "Asset " + asset, logger);
		Boolean isDeleted = false;
		try {
			if (asset != null && asset.getOrganisation() != null
					&& asset.getOrganisation().getIsAssetListRequired() != null
					&& !asset.getOrganisation().getIsAssetListRequired()) {
				Logutils.log(className, methodName, logUUid, " Before calling restUtils.deleteAssetByAssetUUID method ",
						logger);

				isDeleted = restUtils.deleteAssetByAssetUUID(logUUid, asset.getUuid());
				Logutils.log(logUUid,
						" After successfull deletion asset ID :" + asset.getUuid() + " isDeleted : " + isDeleted,
						logger);
			}
		} catch (Exception e) {
			Logutils.log(logUUid, " Exception is : " + e.getMessage(), logger);
		}
		Logutils.log(logUUid, " Exiting from the method ", logger);
		return isDeleted;
	}

//
//    @Override
//    public InstallHistory inProgressInstallForGateway(String gatewayUuid) {
//        Context context =new Context();
//        final String methodName = "inProgressInstallForGateway";
//        String gatewayUuidLog = "GatewayUuid: "+gatewayUuid;
//        String installHistoryStatusLog = "InstallHistoryStatus: "+InstallHistoryStatus.STARTED;
//        Logutils.log(className,methodName,context.getLogUUId()," Before calling installHistoryRepository.findByGatewayUuidAndStatus method ",logger,gatewayUuidLog,installHistoryStatusLog);
//
//        List<InstallHistory> installHistoryList = installHistoryRepository.findByGatewayUuidAndStatus(gatewayUuid, InstallHistoryStatus.STARTED);
//        Logutils.log(className,methodName,context.getLogUUId()," After calling installHistoryRepository.findByGatewayUuidAndStatus method ",logger);
//
//        return installHistoryList.get(0);
//    }
//
//    @Override
//    public Boolean resetCompanyData(String companyUuid,Context context) {
//        final String methodName="resetCompanyData";
//        String companyUuidLog = "CompanyUuid: "+companyUuid;
//        Logutils.log(className,methodName,context.getLogUUId()," Before calling restUtils.getCompanyByUuidFromCompanyService method ",logger,companyUuidLog);
//
//        Company company = restUtils.getCompanyByUuidFromCompanyService(companyUuid);
//        String companyIdLog ="CompanyId: ";
//        String companyAccountNumLog ="AccountNumber: ";
//
//        if(company != null ){
//            companyIdLog =companyIdLog + company.getId();
//            companyAccountNumLog = companyAccountNumLog + company.getAccountNumber();
//        }
//        Logutils.log(className,methodName,context.getLogUUId()," After calling restUtils.getCompanyByUuidFromCompanyService method ",logger,companyIdLog);
//
//        Logutils.log(className,methodName,context.getLogUUId()," Before calling installHistoryRepository.findByCompanyUuid method ",logger,companyUuidLog);
//
//        List<InstallHistory> installHistoryList = installHistoryRepository.findByCompanyUuid(companyUuid);
//        Logutils.log(className,methodName,context.getLogUUId()," After calling installHistoryRepository.findByCompanyUuid method ",logger);
//
//        installHistoryList.forEach(installHistory -> {
//            List<InstallLog> installLogList = installLogRepository.findByInstallHistory(installHistory.getId());
//            installLogList.forEach(installLog -> {
//                installLogRepository.delete(installLog);
//            });
//            List<LogIssue> logIssueList = logIssueRepository.findByInstallCode(installHistory.getInstallCode());
//            logIssueList.forEach(logIssue -> {
//                logIssueRepository.delete(logIssue);
//            });
//            installHistoryRepository.delete(installHistory);
//        });
//        Logutils.log(className,methodName,context.getLogUUId()," Before calling restUtils.getSensorByCan method ",logger,companyAccountNumLog);
//
//        List<Sensor> sensorList = restUtils.getSensorByCan(company.getAccountNumber());
//        Logutils.log(className,methodName,context.getLogUUId()," AFTER calling restUtils.getSensorByCan method ",logger,companyAccountNumLog);
//        sensorList.forEach(sensor -> {
//            List<InstallLog> installLogList = installLogRepository.findBySensorUuid(sensor.getUuid());
//            Logutils.log(className,methodName,context.getLogUUId()," finding  install log findBySensorUuid method ",logger);
//            installLogList.forEach(installLog -> {
//                installLogRepository.delete(installLog);
//                Logutils.log(className,methodName,context.getLogUUId()," finding  deleting install log findBySensorUuid method ",logger);
//            });
//            List<LogIssue> logIssueList = logIssueRepository.findBySensorUuid(sensor.getUuid());
//            logIssueList.forEach(logIssue -> {
//                logIssueRepository.delete(logIssue);
//            });
//        });
//        return true;
//    }
//
	@Override
	public List<InProgressInstall> getInProgressInstall(String accountNumber, String logUUid) {
		String methodName = "getInProgressInstall";

		Logutils.log(className, methodName, logUUid,
				" Inside getInProgressInstall Method From InstallerService " + "accountNumber " + accountNumber,
				logger);
		List<InProgressInstall> inProgressInstallList = new ArrayList<InProgressInstall>();
		String accountNumberLog = "AccountNumber: " + accountNumber;

		Logutils.log(className, methodName, logUUid, " Before calling restUtils.getCompanyFromCompanyService method ",
				logger, accountNumberLog);
		Organisation organisation = restUtils.getCompanyFromCompanyService(logUUid, accountNumber);
		String companyIdLog = "CompanyId: ";
		String companyUuidLog = "CompanyUuid: ";
		if (organisation != null) {
			companyIdLog = companyIdLog + organisation.getId();
			companyUuidLog = companyUuidLog + organisation.getUuid();
		}
		Logutils.log(className, methodName, logUUid, " After calling restUtils.getCompanyFromCompanyService method ",
				logger, accountNumberLog);

		String statusLog = "InstallHistoryStatus:  " + InstallHistoryStatus.STARTED;
		Logutils.log(logUUid, " Before calling installHistoryRepository.findByCompanyUuidAndStatus method ", logger,
				companyUuidLog, statusLog);

		List<InProgressInstall> installHistoryList = installHistoryRepository
				.findByCompanyUuidAndStatusByDto(organisation.getUuid(), InstallHistoryStatus.STARTED);

		Logutils.log(logUUid, " After calling installHistoryRepository.findByCompanyUuidAndStatus method ", logger);

		// inProgressInstallList =
		// beanConverter.createInProgressInstallHistoryList(installHistoryList);
		Logutils.log(className, methodName, logUUid, " Exiting From InstallerService ", logger);
		return installHistoryList;
	}

	@Override
	public Page<InProgressInstall> getInProgressInstallV2(String accountNumber, String logUUid, Pageable page) {
		String methodName = "getInProgressInstall";
		Logutils.log(className, methodName, logUUid,
				" Inside getInProgressInstallV2 Method From InstallerService " + "accountNumber " + accountNumber,
				logger);
		List<InProgressInstall> inProgressInstallList = new ArrayList<InProgressInstall>();
		String accountNumberLog = "AccountNumber: " + accountNumber;

		Logutils.log(className, methodName, logUUid, " Before calling restUtils.getCompanyFromCompanyService method ",
				logger, accountNumberLog);
		Organisation organisation = restUtils.getCompanyFromCompanyService(logUUid, accountNumber);
		Logutils.log(className, methodName, logUUid, " After calling restUtils.getCompanyFromCompanyService method ",
				logger, accountNumberLog);
		String companyIdLog = "CompanyId: ";
		String companyUuidLog = "CompanyUuid: ";
		if (organisation != null) {
			companyIdLog = companyIdLog + organisation.getId();
			companyUuidLog = companyUuidLog + organisation.getUuid();

		}

		String statusLog = "InstallHistoryStatus:  " + InstallHistoryStatus.STARTED;
		Logutils.log(logUUid, " Before calling installHistoryRepository.findByCompanyUuidAndStatus method ", logger,
				companyUuidLog, statusLog);

		Page<InProgressInstall> installHistoryList = installHistoryRepository
				.findByCompanyUuidAndStatusByDtoV2(organisation.getUuid(), InstallHistoryStatus.STARTED, page);

		Logutils.log(logUUid, " After calling installHistoryRepository.findByCompanyUuidAndStatus method ", logger);

		// inProgressInstallList =
		// beanConverter.createInProgressInstallHistoryList(installHistoryList);
		Logutils.log(className, methodName, logUUid, " Exiting From InstallerService ", logger);
		return installHistoryList;
	}

//    @Override
//    public LogIssue logIssue(LogIssueRequest logIssueRequest,Long userId,Context context) {
//        if (logIssueRequest.getInstallCode() != null && !logIssueRequest.getInstallCode().isEmpty() &&
//                logIssueRequest.getReasonCode() != null &&
//                logIssueRequest.getDatetimeRT() != null && !logIssueRequest.getDatetimeRT().isEmpty() &&
//                logIssueRequest.getIssueType() != null && !logIssueRequest.getIssueType().isEmpty() &&
//                logIssueRequest.getSensorUuid() != null && !logIssueRequest.getSensorUuid().isEmpty()) {
//            String methodName = "logIssue";
//            String installCodeLog = "InstallCode: "+ logIssueRequest.getInstallCode();
//            String userIdLog="UserId: "+userId;
//            String sensorUuidLog = "SensorUuid: "+ logIssueRequest.getSensorUuid();
//            String reasonCodeLog = "ReasonCode: "+ logIssueRequest.getReasonCode();
//            String issueTypeLog = "IssueType: "+logIssueRequest.getIssueType();
//            Logutils.log(className,methodName,context.getLogUUId()," Before calling installHistoryRepository.findByInstallCode method install code value ",logger,installCodeLog);
//            Logutils.log(className,methodName,context.getLogUUId()," Before calling installHistoryRepository.findByInstallCode method sensour uuid value ",logger,sensorUuidLog);
//            Logutils.log(className,methodName,context.getLogUUId()," Before calling installHistoryRepository.findByInstallCode method reason code value ",logger,reasonCodeLog);
//
//            InstallHistory installHistory = installHistoryRepository.findByInstallCode(logIssueRequest.getInstallCode());
//            String InstallHistoryIdLog ="InstallHistoryId: "+ installHistory.getId();
//            Logutils.log(className,methodName,context.getLogUUId()," After calling installHistoryRepository.findByInstallCode method ",logger,InstallHistoryIdLog);
//
//            if (installHistory == null) {
//                throw new InstallerException("No install history found for Install Code");
//            }
//            Logutils.log(className,methodName,context.getLogUUId()," Before calling restUtils.getUserFromAuthService method ",logger,userIdLog);
//            User user = restUtils.getUserFromAuthService(userId);
//
//            Logutils.log(className,methodName,context.getLogUUId()," Before calling restUtils.getSensorBySensorUuid method ",logger,sensorUuidLog);
//
//            Sensor sensor = restUtils.getSensorBySensorUuid(logIssueRequest.getSensorUuid());
//            String sensorIdLog = "SensorId: ";
//            String productNameLog = "ProductName: ";
//            if(sensor != null){
//                sensorIdLog = sensorIdLog + sensor.getId();
//                productNameLog = productNameLog + sensor.getProductName();
//            }
//            Logutils.log(className,methodName,context.getLogUUId()," After calling restUtils.getSensorBySensorUuid method ",logger,sensorIdLog);
//
//
//            ReasonCode reasonCode = null;
//            if (sensor != null) {
//                if (!logIssueRequest.getReasonCode().isEmpty()) {
//                    Logutils.log(className,methodName,context.getLogUUId()," Before calling reasonCodeRepository.findByCodeAndIssueType method ",logger, reasonCodeLog, issueTypeLog);
//
//                    reasonCode = reasonCodeRepository.findByCodeAndIssueType(logIssueRequest.getReasonCode(), logIssueRequest.getIssueType());
//                    if (reasonCode == null && logIssueRequest.getReasonCode() != null && "".equalsIgnoreCase(logIssueRequest.getReasonCode())) {
//                        throw new InstallerException("No Reason Code found for given code");
//                    }
//                } else {
//                    Logutils.log(className,methodName,context.getLogUUId()," Before calling sensorReasonCodeRepository.findBySensorProductNameAndIssueType method ",logger , productNameLog, issueTypeLog);
//
//                    List<SensorReasonCode> sensorReasonCodeList = sensorReasonCodeRepository.findBySensorProductNameAndIssueType(sensor.getProductName(), logIssueRequest.getIssueType());
//                    if (sensorReasonCodeList != null && !sensorReasonCodeList.isEmpty()) {
//                        //throw new InstallerException("No Reason Code found for given code");
//                    }
//                }
//                List<String> sensorUuidList = installHistory.getGateway().getSensors().stream().map(sensor1 -> sensor1.getUuid()).collect(Collectors.toList());
//                if (!sensorUuidList.contains(logIssueRequest.getSensorUuid())) {
//                    throw new InstallerException("Sensor does not belong to given install code");
//                } else {
//                    Logutils.log(className,methodName,context.getLogUUId()," Before calling sensorReasonCodeRepository.findBySensorProductNameAndIssueType method ",logger , productNameLog, issueTypeLog);
//
//                    List<SensorReasonCode> sensorReasonCodeList = sensorReasonCodeRepository.findBySensorProductNameAndIssueType(sensor.getProductName(), logIssueRequest.getIssueType());
//                    List<String> reasonCodeForSensor = sensorReasonCodeList.stream().map(reasonCode1 -> reasonCode1.getReasonCode().getCode()).collect(Collectors.toList());
//                    if (reasonCodeForSensor != null && !reasonCodeForSensor.isEmpty() && logIssueRequest.getReasonCode() != null && "".equalsIgnoreCase(logIssueRequest.getReasonCode()) && !logIssueRequest.getReasonCode().isEmpty() && !reasonCodeForSensor.contains(reasonCode.getCode())) {
//                        throw new InstallerException("Reason code is not applicable for given sensor");
//                    }
//                }
//                
//            } else {
//                throw new InstallerException("No sensor found for given Sensor UUID");
//            }
//            
//            LogIssue logIssue = null;
//            if(logIssueRequest.getIssueUuid() != null && !logIssueRequest.getIssueUuid().isEmpty()) {
//            	logIssue = logIssueRepository.findByLogIssueUuid(logIssueRequest.getIssueUuid());
//            	if(logIssue == null) {
//            		logIssue = new LogIssue();
//            		logIssue.setUuid(UUID.randomUUID().toString());
//            		logIssue.setCreatedOn(Instant.ofEpochMilli(Long.parseLong(logIssueRequest.getDatetimeRT())));
//            		logIssue.setCreatedBy(user);
//            	} else {
//            		logIssue.setUpdatedBy(user);
//            		logIssue.setCreatedOn(Instant.ofEpochMilli(Long.parseLong(logIssueRequest.getDatetimeRT())));
//            	}
//            	logIssue.setUuid(logIssueRequest.getIssueUuid());
//            } else {
//            	logIssue = new LogIssue();
//            	logIssue.setUuid(UUID.randomUUID().toString());
//            	logIssue.setCreatedOn(Instant.ofEpochMilli(Long.parseLong(logIssueRequest.getDatetimeRT())));
//            	logIssue.setCreatedBy(user);
//            }
//            
//            logIssue.setInstallHistory(installHistory);
//            logIssue.setIssueType(logIssueRequest.getIssueType());
//            if(logIssueRequest.getSensorId()!=null) {
//            	logIssue.setSensorId(logIssueRequest.getSensorId());;
//            }
//            logIssue.setReasonCode(reasonCode);
//            logIssue.setSensor(sensor);
//            logIssue.setType("SENSOR");
//            logIssue.setRelatedUuid(sensor.getUuid());
//            logIssue.setComment(logIssueRequest.getComment());
//            logIssue.setData(logIssueRequest.getData());
//            logIssue.setStatus(LogIssueStatus.getLogIssueStatus(logIssueRequest.getStatus()));
//            Logutils.log(className,methodName,context.getLogUUId()," Before calling logIssueRepository.save method ",logger );
//            logIssue = logIssueRepository.save(logIssue);
//            String logIssueIdLog ="LogIssueId: ";
//            if(logIssue != null){
//                logIssueIdLog = logIssueIdLog + logIssue.getId();
//            }
//            Logutils.log(className,methodName,context.getLogUUId()," After calling logIssueRepository.save method ",logger,logIssueIdLog );
//
//            return logIssue;
//        } else {
//            String exceptionMessage = "";
//            if (logIssueRequest.getInstallCode() == null || logIssueRequest.getInstallCode().isEmpty()) {
//                exceptionMessage = "Please provide install code";
//            } else if (logIssueRequest.getReasonCode() == null || logIssueRequest.getReasonCode().isEmpty()) {
//                exceptionMessage = "No Reason Code Selected/Please select a reason code and try again.";
//            } else if (logIssueRequest.getIssueType() == null || logIssueRequest.getIssueType().isEmpty()) {
//                exceptionMessage = "Please provide issue type";
//            } else if (logIssueRequest.getDatetimeRT() == null || logIssueRequest.getDatetimeRT().isEmpty()) {
//                exceptionMessage = "Please provide timestamp";
//            } else if (logIssueRequest.getSensorUuid() == null || logIssueRequest.getSensorUuid().isEmpty()) {
//                exceptionMessage = "No Sensor Selected/Please select a sensor and try again.";
//            }
//            throw new InstallerException(exceptionMessage);
//        }
//    }
//
//    @Override
//    public SensorDetailsResponse getSensorDetails(String installCode,Context context) {
//        String methodName = "getSensorDetails";
//
//        if (installCode != null && !installCode.isEmpty()) {
//            String installCodeLog ="InstallCode: "+installCode;
//            Logutils.log(className,methodName,context.getLogUUId()," Before calling installHistoryRepository.findByInstallCode method ",logger, installCodeLog );
//
//            InstallHistory installHistory = installHistoryRepository.findByInstallCode(installCode);
//            if (installHistory != null) {
//                SensorDetailsResponse sensorDetailsResponse = new SensorDetailsResponse();
//                sensorDetailsResponse.setGatewayUuid(installHistory.getGateway().getUuid());
//                List<SensorDetailsBean> sensorDetailsBeanList = new ArrayList<>();
//                installHistory.getGateway().getSensors().forEach(sensor -> {
//                    String productCodeLog = "ProductCode: "+sensor.getProductCode();
//                    SensorDetailsBean sensorDetailsBean = new SensorDetailsBean();
//                    sensorDetailsBean.setSensorUuid(sensor.getUuid());
//                    sensorDetailsBean.setSensorProductCode(sensor.getProductCode());
//                    Logutils.log(className,methodName,context.getLogUUId()," Before calling restUtils.getLookupValueFromDeviceService method ",logger, productCodeLog );
//
//                    String sensorDisplayName = restUtils.getLookupValueFromDeviceService(sensor.getProductCode());
//                    if (sensorDisplayName != null && !sensorDisplayName.isEmpty()) {
//                        sensorDetailsBean.setSensorProductName(sensorDisplayName);
//                    } else {
//                        sensorDetailsBean.setSensorProductName(sensor.getProductName());
//                    }
//                    Map<String, List<ReasonCodeBean>> reasonCodeBeanMap = new HashMap<>();
//                    List<InstallInstructionBean> sensorInstallInstructions = new ArrayList<>();
//                    String productNameLog ="ProductName: "+sensor.getProductName();
//
//                    Logutils.log(className,methodName,context.getLogUUId()," Before calling sensorReasonCodeRepository.findBySensorProductName method ",logger, productNameLog );
//                    List<SensorReasonCode> sensorReasonCodeList = sensorReasonCodeRepository.findBySensorProductName(sensor.getProductName());
//                    sensorReasonCodeList.forEach(sensorReasonCode -> {
//                        ReasonCodeBean reasonCodeBean = new ReasonCodeBean();
//                        reasonCodeBean.setReasonCode(sensorReasonCode.getReasonCode().getCode());
//                        reasonCodeBean.setDisplayName(sensorReasonCode.getReasonCode().getValue());
//                        if (reasonCodeBeanMap.get(sensorReasonCode.getReasonCode().getIssueType()) != null) {
//                            reasonCodeBeanMap.get(sensorReasonCode.getReasonCode().getIssueType()).add(reasonCodeBean);
//                        } else {
//                            List<ReasonCodeBean> reasonCodeBeanList = new ArrayList<>();
//                            reasonCodeBeanList.add(reasonCodeBean);
//                            reasonCodeBeanMap.put(sensorReasonCode.getReasonCode().getIssueType(), reasonCodeBeanList);
//                        }
//                    });
//                    sensorDetailsBean.setSensorReasonCodes(reasonCodeBeanMap);
//                    Logutils.log(className,methodName,context.getLogUUId()," Before calling sensorInstallInstructionRepository.findBySensorProductName method ",logger, productNameLog );
//                    List<SensorInstallInstruction> sensorInstallInstructionList = sensorInstallInstructionRepository.findBySensorProductName(sensor.getProductName());
//                    sensorInstallInstructionList.forEach(sensorInstallInstruction -> {
//                        InstallInstructionBean installInstructionBean = new InstallInstructionBean();
//                        installInstructionBean.setInstruction(sensorInstallInstruction.getInstallInstruction().getInstruction());
//                        installInstructionBean.setSequence(sensorInstallInstruction.getInstallInstruction().getStepSequence());
//                        sensorInstallInstructions.add(installInstructionBean);
//                    });
//                    sensorDetailsBean.setSensorInstallInstructions(sensorInstallInstructions);
//                    InstallInstructionComparator comparator = new InstallInstructionComparator();
//                    Collections.sort(sensorDetailsBean.getSensorInstallInstructions(), comparator);
//                    sensorDetailsBeanList.add(sensorDetailsBean);
//                });
//                sensorDetailsResponse.setSensorList(sensorDetailsBeanList);
//                return sensorDetailsResponse;
//            } else {
//                throw new InstallerException("No install history found for install code");
//            }
//        } else {
//            throw new InstallerException("Please provide install code and try again");
//        }
//    }
//
//    @Override
//    public List<LogIssueBean> getLoggedIssues(String installCode,Context context) {
//        String methodName = "getLoggedIssues";
//
//        if (installCode != null && !installCode.isEmpty()) {
//            String installCodeLog = "InstallCode: "+installCode;
//            Logutils.log(className,methodName,context.getLogUUId()," Before calling installHistoryRepository.findByInstallCode method ",logger, installCodeLog );
//
//            InstallHistory byInstallCode = installHistoryRepository.findByInstallCode(installCode);
//            if(byInstallCode != null) {
//                Logutils.log(className,methodName,context.getLogUUId()," Before calling logIssueRepository.findByInstallCode method ",logger, installCodeLog );
//
//                List<LogIssue> logIssueList = logIssueRepository.findByInstallCode(installCode);
//                if (logIssueList != null && !logIssueList.isEmpty()) {
//                    List<LogIssueBean> logIssueBeanList = beanConverter.convertLogIssueToLogIssueBean(logIssueList);
//                    return logIssueBeanList;
//                } else {
//                    return new ArrayList<LogIssueBean>();
//                }
//            } else {
//                throw new InstallerException("No install history found for given install code");
//            }
//        } else {
//            throw new InstallerException("Please provide install code and try again");
//        }
//    }
//
//    @Override
//    public Boolean resetGatewayData(String gatewayUuid) {
//        Context context = new Context();
//        String methodName = "resetGatewayData";
//        String gatewayUuidLog = "GatewayUuid: "+gatewayUuid;
//        Logutils.log(className,methodName,context.getLogUUId()," Before calling installHistoryRepository.findByGatewayUuid method ",logger, gatewayUuidLog );
//
//        List<InstallHistory> installHistoryList = installHistoryRepository.findByGatewayUuid(gatewayUuid);
//        installHistoryList.forEach(installHistory -> {
//            Logutils.log(className,methodName,context.getLogUUId()," Before calling logIssueRepository.findByInstallCode method ",logger, gatewayUuidLog );
//
//            List<LogIssue> logIssues = logIssueRepository.findByInstallCode(installHistory.getInstallCode());
//            logIssues.forEach(logIssue -> {
//                logIssueRepository.delete(logIssue);
//            });
//            installHistory.getGateway().getSensors().forEach(sensor -> {
//                List<LogIssue> logIssueList = logIssueRepository.findBySensorUuid(sensor.getUuid());
//                logIssueList.forEach(logIssue -> {
//                    logIssueRepository.delete(logIssue);
//                });
//            });
//            String installHistoryIdLog="InstallHistoryId :"+installHistory.getId();
//            Logutils.log(className,methodName,context.getLogUUId()," Before calling installLogRepository.findByInstallHistory method ",logger, installHistoryIdLog );
//
//            List<InstallLog> installLogList = installLogRepository.findByInstallHistory(installHistory.getId());
//            installLogList.forEach(installLog -> {
//                installLogRepository.delete(installLog);
//            });
//            installHistory.getGateway().getSensors().forEach(sensor -> {
//                List<InstallLog> installLogs = installLogRepository.findBySensorUuid(sensor.getUuid());
//                installLogs.forEach(installLog -> {
//                    installLogRepository.delete(installLog);
//                });
//            });
//            Logutils.log(className,methodName,context.getLogUUId()," Before calling installHistoryRepository.delete method ",logger, installHistoryIdLog );
//
//            installHistoryRepository.delete(installHistory);
//        });
//        return true;
//    }
//    
//
	@Override
	public List<InstallHistory> getInstallHistoryByAssetUuids(
			GetInstallHistoryByAssetUuids getInstallHistoryByAssetUuids) {
		Context context = new Context();
		String methodName = "getInstallHistoryByAssetUuids";
		Logutils.log(className, methodName, context.getLogUUId(),
				" Inside getInstallHistoryByAssetUuids Method From InstallerService "
						+ " GetInstallHistoryByAssetUuids " + getInstallHistoryByAssetUuids,
				logger);
		if (getInstallHistoryByAssetUuids.getFilterValues() != null
				&& (getInstallHistoryByAssetUuids.getFilterValues().containsKey("imei")
						|| getInstallHistoryByAssetUuids.getFilterValues().containsKey("installed"))) {

			if (getInstallHistoryByAssetUuids.getFilterValues().containsKey("imei")
					&& getInstallHistoryByAssetUuids.getFilterValues().get("imei") != null
					&& getInstallHistoryByAssetUuids.getFilterValues().containsKey("installed")
					&& getInstallHistoryByAssetUuids.getFilterValues().get("installed") != null) {
				String date = getInstallHistoryByAssetUuids.getFilterValues().get("installed");
				Date d1 = null;
				try {
					d1 = dateFormat.parse(date);
				} catch (ParseException e) {
					e.printStackTrace();
				}
				Instant instant = Instant.parse(DATE_TIME_FORMATTER.format(d1.toInstant()));
				Logutils.log(className, methodName, context.getLogUUId(),
						" Before calling installHistoryRepository.findByAssetUuidswithImeiAndInstalledDate method imei key",
						logger);

				List<InstallHistory> installHistoryList = installHistoryRepository
						.findByAssetUuidswithImeiAndInstalledDate(getInstallHistoryByAssetUuids.getAssetUuids(),
								getInstallHistoryByAssetUuids.getFilterValues().get("imei"), instant,
								instant.plus(1, ChronoUnit.DAYS));
				Logutils.log(className, methodName, context.getLogUUId(),
						" Before calling installHistoryRepository.findByAssetUuidswithImeiAndInstalledDate method imei key",
						logger);
				return installHistoryList;
			}
			if (getInstallHistoryByAssetUuids.getFilterValues().containsKey("installed")
					&& getInstallHistoryByAssetUuids.getFilterValues().get("installed") != null) {
				String date = getInstallHistoryByAssetUuids.getFilterValues().get("installed");
				Date d1 = null;
				try {
					d1 = dateFormat.parse(date);
				} catch (ParseException e) {
					e.printStackTrace();
				}
				Instant instant = Instant.parse(DATE_TIME_FORMATTER.format(d1.toInstant()));

				Logutils.log(className, methodName, context.getLogUUId(),
						" Before calling installHistoryRepository.findByAssetUuidswithImeiAndInstalledDate method  installed key",
						logger);

				List<InstallHistory> installHistoryList = installHistoryRepository.findByAssetUuidswithInstalledDate(
						getInstallHistoryByAssetUuids.getAssetUuids(), instant, instant.plus(1, ChronoUnit.DAYS));
				Logutils.log(className, methodName, context.getLogUUId(),
						" After calling installHistoryRepository.findByAssetUuidswithInstalledDate method  installed key",
						logger);
				return installHistoryList;
			}
			if (getInstallHistoryByAssetUuids.getFilterValues().containsKey("imei")
					&& getInstallHistoryByAssetUuids.getFilterValues().get("imei") != null) {
				Logutils.log(className, methodName, context.getLogUUId(),
						" Before calling installHistoryRepository.findByAssetUuidswithImeiAndInstalledDate method  imei key",
						logger);
				List<InstallHistory> installHistoryList = installHistoryRepository.findByAssetUuidswithImei(
						getInstallHistoryByAssetUuids.getAssetUuids(),
						getInstallHistoryByAssetUuids.getFilterValues().get("imei"));
				return installHistoryList;
			}

		} else {
			Logutils.log(className, methodName, context.getLogUUId(),
					" Before calling installHistoryRepository.findByAssetUuids method ", logger);

			List<InstallHistory> installHistoryList = installHistoryRepository
					.findByAssetUuids(getInstallHistoryByAssetUuids.getAssetUuids());
			return installHistoryList;
		}
		return null;
	}

//	@Override
//	public Page<InstallationSummaryResponseDTO> getAllInstallationSummary(Pageable pageable, String comapnyUuid,
//			Long userId, Map<String, String> filterValues, Long days) {
//        Context context =new Context();
//        String methodName = "getAllInstallationSummary";
//        String userIdLog = "UserId: "+userId;
//        Logutils.log(className,methodName,context.getLogUUId()," Before calling restUtils.getUserFromAuthService method ",logger, userIdLog );
//
//        User user = restUtils.getUserFromAuthService(userId);
//		Page<InstallHistory> installHistory = null;
//		int size=0;
//		Specification<InstallHistory> spc = InstallerHistorySpecification.getaInstallerSpecification(filterValues, user, comapnyUuid, days);
//        Logutils.log(className,methodName,context.getLogUUId()," Before calling installHistoryRepository.findAll method ",logger );
//
//        installHistory = installHistoryRepository.findAll(spc, pageable);
//		List<InstallationSummaryResponseDTO> assetResponseDTOList = new ArrayList<>();
//		
//		for(InstallHistory ins :installHistory) {
//			try {
//				if((filterValues.containsKey("cargo_sensor") && filterValues.get("cargo_sensor")!=null) || (filterValues.containsKey("abssensor") && filterValues.get("abssensor")!=null)||(filterValues.containsKey("door_sensor") && filterValues.get("door_sensor")!=null) || (filterValues.containsKey("atis_sensor") && filterValues.get("atis_sensor")!=null) || (filterValues.containsKey("light_sentry") && filterValues.get("light_sentry")!=null) || (filterValues.containsKey("tpms") && filterValues.get("tpms")!=null) || (filterValues.containsKey("wheel_end") && filterValues.get("wheel_end")!=null)||(filterValues.containsKey("air_tank") && filterValues.get("air_tank")!=null)||(filterValues.containsKey("regulator") && filterValues.get("regulator")!=null)) {
//				InstallationSummaryResponseDTO vf =  beanConverter.convertInstallHistoryToInstallResponseDTO(ins,filterValues);
//				if(vf.isFilter()) {
//				assetResponseDTOList.add(vf);
//				size = assetResponseDTOList.size();
//				}
//				}else {
//					InstallationSummaryResponseDTO vf =  beanConverter.convertInstallHistoryToInstallResponseDTO(ins,filterValues);
//					assetResponseDTOList.add(vf);
//					size = (int) installHistory.getTotalElements();
//				}
//			} catch (JsonProcessingException e) {
//				logger.error("Exception while converting installHistory to InstallationSummaryResponseDTO", e);
//				e.printStackTrace();
//			}	
//		}
//		Page<InstallationSummaryResponseDTO> page = new PageImpl<>(assetResponseDTOList, pageable,size);
//		return page;
//	}
//	
//	@Override
//	public Workbook exportExcelForInstallationSummary(Pageable pageable, String comapnyUuid,
//			Long userId, Map<String, String> filterValues, Long days) {
//		Workbook workbook = null;
//        Context context =new Context();
//        String methodName = "getAllInstallationSummary";
//        String userIdLog = "UserId: "+userId;
//        Logutils.log(className,methodName,context.getLogUUId()," Before calling restUtils.getUserFromAuthService method ",logger, userIdLog );
//
//        User user = restUtils.getUserFromAuthService(userId);
//		Page<InstallHistory> installHistory = null;
//		int size=0;
//		Specification<InstallHistory> spc = InstallerHistorySpecification.getaInstallerSpecification(filterValues, user, comapnyUuid, days);
//        Logutils.log(className,methodName,context.getLogUUId()," Before calling installHistoryRepository.findAll method ",logger );
//
//        installHistory = installHistoryRepository.findAll(spc, pageable);
//		List<InstallationSummaryResponseDTO> assetResponseDTOList = new ArrayList<>();
//		
//		for(InstallHistory ins :installHistory) {
//			try {
//				if((filterValues.containsKey("cargo_sensor") && filterValues.get("cargo_sensor")!=null) || (filterValues.containsKey("abssensor") && filterValues.get("abssensor")!=null)||(filterValues.containsKey("door_sensor") && filterValues.get("door_sensor")!=null) || (filterValues.containsKey("atis_sensor") && filterValues.get("atis_sensor")!=null) || (filterValues.containsKey("light_sentry") && filterValues.get("light_sentry")!=null) || (filterValues.containsKey("tpms") && filterValues.get("tpms")!=null) || (filterValues.containsKey("wheel_end") && filterValues.get("wheel_end")!=null)||(filterValues.containsKey("air_tank") && filterValues.get("air_tank")!=null)||(filterValues.containsKey("regulator") && filterValues.get("regulator")!=null)) {
//				InstallationSummaryResponseDTO vf =  beanConverter.convertInstallHistoryToInstallResponseDTO(ins,filterValues);
//				if(vf.isFilter()) {
//				assetResponseDTOList.add(vf);
//				size = assetResponseDTOList.size();
//				}
//				}else {
//					InstallationSummaryResponseDTO vf =  beanConverter.convertInstallHistoryToInstallResponseDTO(ins,filterValues);
//					assetResponseDTOList.add(vf);
//					size = (int) installHistory.getTotalElements();
//				}
//			} catch (JsonProcessingException e) {
//				logger.error("Exception while converting installHistory to InstallationSummaryResponseDTO", e);
//				e.printStackTrace();
//			}	
//		}
//		
//		String[] headerColumns = { "Asset ID", "Product", "Device ID", "Status", "User Name", "User Company", "Started" , "Finished",
//				"Last Updated", "InstallAssist Version", "Battery", "Battery V", "Blue/ABS", "Blue/ABS V", "Marker/Brown", "Marker/Brown V", "Cargo Sensor",
//				"Cargo Camera Sensor", "Door Sensor", "Door MAC", "Door Type", "LampCheck ATIS", "LampCheck ATIS MAC", "ABS", "ATIS",
//				"LiteSentry", "Receiver", "TPMS LOF-1", "TPMS LOF-1 ID", "TPMS LIF-2", "TPMS LIF-2 ID", "TPMS RIF-3", "TPMS RIF-3 ID",
//				"TPMS ROF-4", "TPMS ROF-4 ID", "TPMS LOR-5", "TPMS LOR-5 ID", "TPMS LIR-6", "TPMS LIR-6 ID", "TPMS RIR-7", "TPMS RIR-7 ID",
//				"TPMS ROR-8", "TPMS ROR-8 ID", "Air Tank", "Air Tank ID", "Regulator", "Regulator ID", "Wheel End" };
//		workbook = createExcelForInstallationSummary("Installation Summary Report", headerColumns, assetResponseDTOList);
//		return workbook;
//	}
//	
//	public Workbook createExcelForInstallationSummary(String sheetName, String[] headerColumns,
//			List<InstallationSummaryResponseDTO> assetResponseDTOList) {
//		// Create a Workbook
//		Workbook workbook = new XSSFWorkbook(); // new HSSFWorkbook() for generating `.xls` file
//
//		// Create a Sheet
//		Sheet sheet = workbook.createSheet(sheetName);
//
//		// Create a Font for styling header cells
//		Font headerFontForClientDetail = workbook.createFont();
//		headerFontForClientDetail.setBold(true);
//		headerFontForClientDetail.setFontName("Calibri");
//		headerFontForClientDetail.setFontHeightInPoints((short) 11);
//		headerFontForClientDetail.setColor(IndexedColors.BLACK.getIndex());
//
//		// Create a CellStyle with the font
//		CellStyle headerCellStyleForClientDetails = workbook.createCellStyle();
//		headerCellStyleForClientDetails.setFont(headerFontForClientDetail);
//
//		// Create a Font for styling header cells
//		Font headerFont = workbook.createFont();
//		headerFont.setBold(true);
//		headerFont.setFontName("Calibri");
//		headerFont.setFontHeightInPoints((short) 10);
//		headerFont.setColor(IndexedColors.BLACK.getIndex());
//
//		// Create a CellStyle with the font
//		CellStyle headerCellStyle = workbook.createCellStyle();
//		headerCellStyle.setFont(headerFont);
//
//		/* create Header for Asset Details */
//		Row headerForContactPerson = sheet.createRow(0);
//		// Create cells
//		for (int i = 0; i < headerColumns.length; i++) {
//			Cell cell = headerForContactPerson.createCell(i);
//			cell.setCellValue(headerColumns[i]);
//			cell.setCellStyle(headerCellStyle);
//		}
//		int rowNum = 1;
//
//		if (assetResponseDTOList != null && assetResponseDTOList.size() > 0) {
//			for (InstallationSummaryResponseDTO installationSummaryDTO : assetResponseDTOList) {
//				Row row = sheet.createRow(rowNum++);
//				int i = 0;
//				createCellAndSetValue(row, i++, installationSummaryDTO.getAssetId());
//				createCellAndSetValue(row, i++, installationSummaryDTO.getProductName() != null ? installationSummaryDTO.getProductName() + " (" + installationSummaryDTO.getProductCode() + ")" : "");
//				createCellAndSetValue(row, i++, installationSummaryDTO.getDeviceId());
//				createCellAndSetValue(row, i++, installationSummaryDTO.getStatus());
//				createCellAndSetValue(row, i++, installationSummaryDTO.getInstallerName());
//				createCellAndSetValue(row, i++, installationSummaryDTO.getInstallerCompany());
//				createCellAndSetValue(row, i++, installationSummaryDTO.getDateStarted() != null ? installationSummaryDTO.getDateStarted().toString() : "");
//				createCellAndSetValue(row, i++, installationSummaryDTO.getInstalled());
//				createCellAndSetValue(row, i++, installationSummaryDTO.getUpdatedAt() != null ? installationSummaryDTO.getUpdatedAt().toString() : "");
//				createCellAndSetValue(row, i++, installationSummaryDTO.getAppVersion() != null ? installationSummaryDTO.getAppVersion() : "");
//				createCellAndSetValue(row, i++, installationSummaryDTO.getBatteryStatus());
//				createCellAndSetValue(row, i++, installationSummaryDTO.getBatteryVoltage()); //10
//				
//				createCellAndSetValue(row, i++, installationSummaryDTO.getPrimaryStatus());
//				createCellAndSetValue(row, i++, installationSummaryDTO.getPrimaryVoltage());
//				createCellAndSetValue(row, i++, installationSummaryDTO.getSecondaryStatus());
//				createCellAndSetValue(row, i++, installationSummaryDTO.getSecondaryVoltage());
//				createCellAndSetValue(row, i++, installationSummaryDTO.getCargoSensor());
//				createCellAndSetValue(row, i++, installationSummaryDTO.getCargoCameraSensor());
//				createCellAndSetValue(row, i++, installationSummaryDTO.getDoorSensor());
//				String doorMacAddress = null;
//				if(installationSummaryDTO.getDoorMacAddress() != null) {
//					doorMacAddress = installationSummaryDTO.getDoorMacAddress();
//					doorMacAddress = doorMacAddress.replaceAll(":", "");
//				}
//				createCellAndSetValue(row, i++, doorMacAddress);
//				createCellAndSetValue(row, i++, installationSummaryDTO.getDoorType());
//				createCellAndSetValue(row, i++, installationSummaryDTO.getLampCheckAtis()); // 20
//				
//				String atisMacAddress = null;
//				if(installationSummaryDTO.getLampCheckAtisMac() != null) {
//					atisMacAddress = installationSummaryDTO.getLampCheckAtisMac();
//					atisMacAddress = atisMacAddress.replaceAll(":", "");
//				}
//				createCellAndSetValue(row, i++, atisMacAddress);
//				createCellAndSetValue(row, i++, installationSummaryDTO.getABSSensor());
//				createCellAndSetValue(row, i++, installationSummaryDTO.getAtisSensor());
//				createCellAndSetValue(row, i++, installationSummaryDTO.getLightSentry());
//				createCellAndSetValue(row, i++, installationSummaryDTO.getReciever());
//				
//				createCellAndSetValue(row, i++, installationSummaryDTO.getTpmsLofStatus());
//				createCellAndSetValue(row, i++, installationSummaryDTO.getTpmsLof());
//				
//				createCellAndSetValue(row, i++, installationSummaryDTO.getTpmsLifStatus());
//				createCellAndSetValue(row, i++, installationSummaryDTO.getTpmsLif());
//				
//				createCellAndSetValue(row, i++, installationSummaryDTO.getTpmsRifStatus());
//				createCellAndSetValue(row, i++, installationSummaryDTO.getTpmsRif());
//				
//				createCellAndSetValue(row, i++, installationSummaryDTO.getTpmsRofStatus());
//				createCellAndSetValue(row, i++, installationSummaryDTO.getTpmsRof());
//				
//				createCellAndSetValue(row, i++, installationSummaryDTO.getTpmsLorStatus()); //30
//				createCellAndSetValue(row, i++, installationSummaryDTO.getTpmsLor());
//				
//				createCellAndSetValue(row, i++, installationSummaryDTO.getTpmsLirStatus());
//				createCellAndSetValue(row, i++, installationSummaryDTO.getTpmsLir());
//				
//				createCellAndSetValue(row, i++, installationSummaryDTO.getTpmsRirStatus());
//				createCellAndSetValue(row, i++, installationSummaryDTO.getTpmsRir());
//				
//				createCellAndSetValue(row, i++, installationSummaryDTO.getTpmsRorStatus());
//				createCellAndSetValue(row, i++, installationSummaryDTO.getTpmsRor());
//				
//				createCellAndSetValue(row, i++, installationSummaryDTO.getAirTank());
//				createCellAndSetValue(row, i++, installationSummaryDTO.getAirTankId());
//				createCellAndSetValue(row, i++, installationSummaryDTO.getRegulator());
//				createCellAndSetValue(row, i++, installationSummaryDTO.getRegulatorId());
//				createCellAndSetValue(row, i++, installationSummaryDTO.getWheelEnd()); //38
//
//			}
//		}
//
//		getAutoSizeColumeForAssigneeReport(sheet, 47);
//
//		return workbook;
//
//	}
//	
//	public void createCellAndSetValue(Row row , int i, String value) {
//		row.createCell(i).setCellValue(value != null ? value : "N/A");
//	}
//	
//	private void getAutoSizeColumeForAssigneeReport(Sheet sheet, int count) {
//		for (int i = 0; i < count; i++) {
//			sheet.autoSizeColumn(i);
//		}
//	}
//
//    @Override
//    public SensorDetailsResponse getOfflineData(Context context) {
//        String methodName = "getOfflineData";
//        List<SensorDetailsBean> sensorDetailsBeanList = new ArrayList<>();
//        SensorDetailsResponse sensorDetailsResponse = new SensorDetailsResponse();
//        Logutils.log(className,methodName,context.getLogUUId()," Before calling restUtils.getUserFromAuthService method ",logger );
//        List<String> productNameList = sensorInstallInstructionRepository.findAllUniqueSensorInstallInstruction();
//        productNameList.forEach(productName -> {
//            SensorDetailsBean sensorDetailsBean = new SensorDetailsBean();
//            String productNameLog ="ProductName: "+productName;
//            Logutils.log(className,methodName,context.getLogUUId()," Before calling restUtils.getUserFromAuthService method ",logger ,productNameLog );
//
//            List<SensorInstallInstruction> sensorInstallInstructionList = sensorInstallInstructionRepository.findBySensorProductName(productName);
//
//            sensorDetailsBean.setSensorProductName(productName);
//            List<InstallInstructionBean> sensorInstallInstructions = new ArrayList<>();
//            sensorInstallInstructionList.forEach(sensorInstallInstruction -> {
//                sensorDetailsBean.setSensorProductCode(sensorInstallInstruction.getSensorProductCode());
//                InstallInstructionBean installInstructionBean = new InstallInstructionBean();
//                installInstructionBean.setInstruction(sensorInstallInstruction
//                        .getInstallInstruction()
//                        .getInstruction());
//                installInstructionBean.setSequence(sensorInstallInstruction
//                        .getInstallInstruction()
//                        .getStepSequence());
//                sensorInstallInstructions.add(installInstructionBean);
//            });
//            sensorDetailsBean.setSensorInstallInstructions(sensorInstallInstructions);
//
//            Map<String, List<ReasonCodeBean>> reasonCodeBeanMap = new HashMap<>();
//            Logutils.log(className,methodName,context.getLogUUId()," Before calling restUtils.getUserFromAuthService method ",logger ,productNameLog );
//
//            List<SensorReasonCode> sensorReasonCodeList = sensorReasonCodeRepository.findBySensorProductName(productName);
//            sensorReasonCodeList.forEach(sensorReasonCode -> {
//                ReasonCodeBean reasonCodeBean = new ReasonCodeBean();
//                reasonCodeBean.setReasonCode(sensorReasonCode
//                        .getReasonCode()
//                        .getCode());
//                reasonCodeBean.setDisplayName(sensorReasonCode
//                        .getReasonCode()
//                        .getValue());
//                if (reasonCodeBeanMap.get(sensorReasonCode
//                        .getReasonCode()
//                        .getIssueType()) != null) {
//                    reasonCodeBeanMap
//                            .get(sensorReasonCode
//                                    .getReasonCode()
//                                    .getIssueType())
//                            .add(reasonCodeBean);
//                } else {
//                    List<ReasonCodeBean> reasonCodeBeanList = new ArrayList<>();
//                    reasonCodeBeanList.add(reasonCodeBean);
//                    reasonCodeBeanMap.put(sensorReasonCode
//                            .getReasonCode()
//                            .getIssueType(), reasonCodeBeanList);
//                }
//            });
//            sensorDetailsBean.setSensorReasonCodes(reasonCodeBeanMap);
//            sensorDetailsBeanList.add(sensorDetailsBean);
//        });
//
//        sensorDetailsResponse.setSensorList(sensorDetailsBeanList);
//        return sensorDetailsResponse;
//    }
	@Override
	public InstallationDetailResponseDTO getInstallHistoryByDeviceUUid(String logUUid, String deviceUUid) {
		Context context = new Context();
		String methodName = "getInstallHistoryByDeviceUUid";
		Logutils.log(className, methodName, logUUid,
				" Inside getInstallHistoryByDeviceUUid Method From InstallerService " + " deviceUUid " + deviceUUid,
				logger);

		InstallationDetailResponseDTO installResponse = new InstallationDetailResponseDTO();
		try {
			if (deviceUUid != null) {
				String assetNameLog = "DeviceUUid: " + deviceUUid;
				Logutils.log(className, methodName, context.getLogUUId(),
						" Before calling installHistoryRepository.findByInstallCode method ", logger, assetNameLog);
				InstallHistory installhistories = installHistoryRepository.findByDeviceUuid(deviceUUid);
				Logutils.log(className, methodName, context.getLogUUId(),
						" after calling installHistoryRepository.findByInstallCode method ", logger, assetNameLog);

				Logutils.log(className, methodName, logUUid,
						" Before calling beanConverter.convertInstallationDetail method ", logger);

				if (installhistories != null) {
					installResponse = beanConverter.convertInstallationDetail(logUUid, installhistories);
				}
				Logutils.log(className, methodName, context.getLogUUId(),
						" after converting into installhistory into installdetailResponse method ", logger,
						assetNameLog);
				return installResponse;
			}
		} catch (JsonProcessingException e) {
			logger.error("Exception while converting installHistory to InstallationSummaryResponseDTO", e);
		}
		Logutils.log(className, methodName, logUUid, " Exiting From InstallerService ", logger);
		return installResponse;

	}
//
//	@Override
//	public GatewayDetailsResponse getGatewayDetails(String installCode, String can, Long userId,Context context) {
//        String methodName = "getGatewayDetails";
//        GatewayDetailsResponse gatewayReponse = new GatewayDetailsResponse();
//		List<GatewayDetailsBean> gatewatDetailList = new ArrayList<>();
//		List<Gateway> gatewayList = new ArrayList<>();
//		String userIdLog = "UserId: "+userId;
//        Logutils.log(className,methodName,context.getLogUUId()," Before calling restUtils.getUserFromAuthService method ",logger ,userIdLog );
//        User user = restUtils.getUserFromAuthService(userId);
//		if (installCode == null && can == null) {
//			Logutils.log(className,methodName,context.getLogUUId(),"value of install code and can is null and role of user is ",logger ,user.getCompany().getType().getValue() );
//			if (user.getCompany().getType().equals(CompanyType.INSTALLER)) {
//				List<Company> companies = user.getCompany().getAccessList();
//				if (companies.size() > 0) {
//					for (Company com : companies) {
//						List<Gateway> list = restUtils
//								.getGatewaysByCANAndStatusFromDeviceService(com.getAccountNumber(), null);
//						gatewayList.addAll(list);
//					}
//				}
//			}
//			if (user.getCompany().getType().equals(CompanyType.CUSTOMER)) {
//				Company companies = user.getCompany();
//				if (companies != null) {
//					List<Gateway> list = restUtils
//							.getGatewaysByCANAndStatusFromDeviceService(companies.getAccountNumber(), null);
//					gatewayList.addAll(list);
//				}
//			}
//			if (user.getCompany().getType().equals(CompanyType.MANUFACTURER)) {
//				List<Company> list = restUtils.getCompanyByCustomer();
//					if (list.size() > 0) {
//						for (Company com : list) {
//							List<Gateway> list1 = restUtils
//									.getGatewaysByCANAndStatusFromDeviceService(com.getAccountNumber(), null);
//							gatewayList.addAll(list1);
//						}
//					}
//				
//			}
//		}
//        String installCodeLog = "InstallCode: "+installCode;
//
//        if (installCode != null && !installCode.isEmpty() && can == null) {
//            Logutils.log(className,methodName,context.getLogUUId()," Before calling installHistoryRepository.findByInstallCode method ",logger ,installCodeLog );
//
//            InstallHistory installHistory1 = installHistoryRepository.findByInstallCode(installCode);
//			gatewayList.add(installHistory1.getGateway());
//		}
//		if (installCode == null && can != null && !can.isEmpty()) {
//		    String canLog ="Can: "+can;
//            Logutils.log(className,methodName,context.getLogUUId()," Before calling restUtils.getGatewaysByCANAndStatusFromDeviceService method ",logger ,canLog );
//
//            gatewayList = restUtils.getGatewaysByCANAndStatusFromDeviceService(can, null);
//		}
//
//		if (installCode != null && !installCode.isEmpty() && can != null && !can.isEmpty()) {
//            Logutils.log(className,methodName,context.getLogUUId()," Before calling installHistoryRepository.findByInstallCode method ",logger , installCodeLog);
//
//            InstallHistory installHistory = installHistoryRepository.findByInstallCode(installCode);
//			if (installHistory.getCompany().getAccountNumber().equalsIgnoreCase(can)) {
//				gatewayList.add(installHistory.getGateway());
//			} else {
//				throw new InstallerException("provided Install Code and Account Number is not matching");
//			}
//		}
//			if (gatewayList.size() > 0) {
//				for (Gateway gat : gatewayList) {
//				    String productNameLog ="ProductName: "+gat.getProductName();
//				    Logutils.log(className,methodName,context.getLogUUId()," Inside iterating Gateway LIst to gate product Name ",logger ,productNameLog );
//                    String uuidLog ="Uuid: "+ gat.getUuid();
//					List<InstallInstructionBean> sensorInstallInstructions = new ArrayList<>();
//					Map<String, List<ReasonCodeBean>> reasonCodeBeanMap = new HashMap<>();
//					List<AttributeValueResposneDTO> attributeList = new ArrayList<>();
//                    Logutils.log(className,methodName,context.getLogUUId()," Before calling restUtils.getAttributeListByProductName method ",logger , productNameLog , uuidLog);
//
//                    List<Attribute> attributes = restUtils.getAttributeListByProductName(gat.getProductName(),
//							gat.getUuid());
//                    Logutils.log(className,methodName,context.getLogUUId()," after calling restUtils.getAttributeListByProductName method ",logger , productNameLog , uuidLog);
//					GatewayDetailsBean gatewatDetails = new GatewayDetailsBean();
//					gatewatDetails.setGatewayUuid(gat.getUuid());
//					gatewatDetails.setGatewayProductName(gat.getProductName());
//					gatewatDetails.setGatewayProductCode(gat.getProductCode());
//					// gatewatDetails.setEligibleAssetType(ins.getAsset().getGatewayEligibility());
//					if (attributes.size() > 0) {
//						gatewatDetails.set_blocker(attributes.get(0).getProductMaster().isBlocker());
//						for (Attribute att : attributes) {
//							AttributeValueResposneDTO attRes = new AttributeValueResposneDTO();
//							attRes.setApplicable(att.isApplicable());
//							attRes.setAttribute_uuid(att.getUuid());
//							attRes.setAttributeName(att.getAttributeName());
//							attRes.setThresholdValue(att.getAttributeValue());
//							attributeList.add(attRes);
//						}
//					}
//					gatewatDetails.setGatewayAttribute(attributeList);
//                    Logutils.log(className,methodName,context.getLogUUId()," Before calling sensorInstallInstructionRepository.findBySensorProductName method ",logger , productNameLog );
//
//                    List<SensorInstallInstruction> sensorInstallInstructionList = sensorInstallInstructionRepository
//							.findBySensorProductName(gat.getProductName());
//					sensorInstallInstructionList.forEach(sensorInstallInstruction -> {
//						InstallInstructionBean installInstructionBean = new InstallInstructionBean();
//						installInstructionBean
//								.setInstruction(sensorInstallInstruction.getInstallInstruction().getInstruction());
//						installInstructionBean
//								.setSequence(sensorInstallInstruction.getInstallInstruction().getStepSequence());
//						sensorInstallInstructions.add(installInstructionBean);
//					});
//					InstallInstructionComparator comparator = new InstallInstructionComparator();
//					Collections.sort(sensorInstallInstructions, comparator);
//					gatewatDetails.setGatewayInstallInstructions(sensorInstallInstructions);
//                    Logutils.log(className,methodName,context.getLogUUId()," Before calling sensorReasonCodeRepository.findBySensorProductName method ",logger , productNameLog );
//
//                    List<SensorReasonCode> sensorReasonCodeList = sensorReasonCodeRepository
//							.findBySensorProductName(gat.getProductName());
//					sensorReasonCodeList.forEach(sensorReasonCode -> {
//						ReasonCodeBean reasonCodeBean = new ReasonCodeBean();
//						reasonCodeBean.setReasonCode(sensorReasonCode.getReasonCode().getCode());
//						reasonCodeBean.setDisplayName(sensorReasonCode.getReasonCode().getValue());
//						if (reasonCodeBeanMap.get(sensorReasonCode.getReasonCode().getIssueType()) != null) {
//							reasonCodeBeanMap.get(sensorReasonCode.getReasonCode().getIssueType()).add(reasonCodeBean);
//						} else {
//							List<ReasonCodeBean> reasonCodeBeanList = new ArrayList<>();
//							reasonCodeBeanList.add(reasonCodeBean);
//							reasonCodeBeanMap.put(sensorReasonCode.getReasonCode().getIssueType(), reasonCodeBeanList);
//						}
//					});
//					gatewatDetails.setGatewayReasonCodes(reasonCodeBeanMap);
//					gatewatDetailList.add(gatewatDetails);
//				}
//				gatewayReponse.setGatewayDetailsBean(gatewatDetailList);
//				return gatewayReponse;
//			} else {
//				throw new InstallerException("No Gateway for Given Input");
//			}
//	}
//
//	@Override
//	public LogIssue logIssueForGateway(LogIssueGatewayRequest logIssueRequest, Long userId,Context context) {
//
//        if (logIssueRequest.getInstallCode() != null && !logIssueRequest.getInstallCode().isEmpty() &&
//                logIssueRequest.getReasonCode() != null &&
//                logIssueRequest.getDatetimeRT() != null && !logIssueRequest.getDatetimeRT().isEmpty() &&
//                logIssueRequest.getIssueType() != null && !logIssueRequest.getIssueType().isEmpty() &&
//                logIssueRequest.getGatewayUuid() != null && !logIssueRequest.getGatewayUuid().isEmpty()) {
//            String methodName = "logIssueForGateway";
//            String userIdLog = "UserId: "+userId;
//            String gatewayUuidLog= "GatewayUuid: "+logIssueRequest.getGatewayUuid();
//            String installCodeLog ="InstallCode: "+ logIssueRequest.getInstallCode();
//            String issueTypeLog = "IssueType: "+logIssueRequest.getIssueType();
//            String reasonCodeLog = "ReasonCode: " + logIssueRequest.getReasonCode();
//
//
//            Logutils.log(className,methodName,context.getLogUUId()," Before calling installHistoryRepository.findByInstallCode method ",logger , installCodeLog);
//
//            InstallHistory installHistory = installHistoryRepository.findByInstallCode(logIssueRequest.getInstallCode());
//            if (installHistory == null) {
//                throw new InstallerException("No install history found for Install Code");
//            }
//            Logutils.log(className,methodName,context.getLogUUId()," Before calling restUtils.getUserFromAuthService method ",logger , userIdLog);
//            User user = restUtils.getUserFromAuthService(userId);
//
//            Logutils.log(className,methodName,context.getLogUUId()," Before calling restUtils.getGatewayByUuid method ",logger , gatewayUuidLog);
//            Gateway gateway = restUtils.getGatewayByUuid(logIssueRequest.getGatewayUuid());
//            ReasonCode reasonCode = null;
//            if (gateway != null) {
//                String productNameLog="ProductName: "+ gateway.getProductName();
//                if (!logIssueRequest.getReasonCode().isEmpty()) {
//                    Logutils.log(className,methodName,context.getLogUUId()," Before calling reasonCodeRepository.findByCodeAndIssueType method ",logger , reasonCodeLog, issueTypeLog);
//
//                    reasonCode = reasonCodeRepository.findByCodeAndIssueType(logIssueRequest.getReasonCode(), logIssueRequest.getIssueType());
//                    if (reasonCode == null && logIssueRequest.getReasonCode() != null && "".equalsIgnoreCase(logIssueRequest.getReasonCode())) {
//                        throw new InstallerException("No Reason Code found for given code");
//                    }
//                } else {
//                    Logutils.log(className,methodName,context.getLogUUId()," Before calling sensorReasonCodeRepository.findBySensorProductNameAndIssueType method ",logger , reasonCodeLog, issueTypeLog);
//
//                    List<SensorReasonCode> sensorReasonCodeList = sensorReasonCodeRepository.findBySensorProductNameAndIssueType(gateway.getProductName(), logIssueRequest.getIssueType());
//                    if (sensorReasonCodeList != null && !sensorReasonCodeList.isEmpty()) {
//                        //throw new InstallerException("No Reason Code found for given code");
//                    }
//                }
//                String sensorUuidList = installHistory.getGateway().getUuid();
//                if (!sensorUuidList.equalsIgnoreCase(logIssueRequest.getGatewayUuid())) {
//                    throw new InstallerException("Gateway does not belong to given install code");
//                } else {
//                    Logutils.log(className,methodName,context.getLogUUId()," Before calling sensorReasonCodeRepository.findBySensorProductNameAndIssueType method ",logger , productNameLog, issueTypeLog);
//
//                    List<SensorReasonCode> sensorReasonCodeList = sensorReasonCodeRepository.findBySensorProductNameAndIssueType(gateway.getProductName(), logIssueRequest.getIssueType());
//                    List<String> reasonCodeForSensor = sensorReasonCodeList.stream().map(reasonCode1 -> reasonCode1.getReasonCode().getCode()).collect(Collectors.toList());
//                    if (reasonCodeForSensor != null && !reasonCodeForSensor.isEmpty() && logIssueRequest.getReasonCode() != null && "".equalsIgnoreCase(logIssueRequest.getReasonCode()) && !logIssueRequest.getReasonCode().isEmpty() &&  !reasonCodeForSensor.contains(reasonCode.getCode())) {
//                        throw new InstallerException("Reason code is not applicable for given gateway");
//                    }
//                }
//                
//            } else {
//                throw new InstallerException("No gateway found for given Gateway Uuid");
//            }
//            
//            LogIssue logIssue = new LogIssue();
//            logIssue.setInstallHistory(installHistory);
//            logIssue.setIssueType(logIssueRequest.getIssueType());
//            logIssue.setReasonCode(reasonCode);
//            logIssue.setType("GATEWAY");
//            logIssue.setGateway(gateway);
//            logIssue.setRelatedUuid(gateway.getUuid());
//            logIssue.setCreatedOn(Instant.ofEpochMilli(Long.parseLong(logIssueRequest.getDatetimeRT())));
//            logIssue.setComment(logIssueRequest.getComment());
//            logIssue.setData(logIssueRequest.getData());
//            logIssue.setStatus(LogIssueStatus.getLogIssueStatus(logIssueRequest.getStatus()));
//            logIssue.setUuid(UUID.randomUUID().toString());
//            logIssue.setCreatedBy(user);
//            Logutils.log(className,methodName,context.getLogUUId()," Before calling logIssueRepository.save method ",logger );
//            logIssue = logIssueRepository.save(logIssue);
//            return logIssue;
//        } else {
//            String exceptionMessage = "";
//            if (logIssueRequest.getInstallCode() == null || logIssueRequest.getInstallCode().isEmpty()) {
//                exceptionMessage = "Please provide install code";
//            } else if (logIssueRequest.getReasonCode() == null || logIssueRequest.getReasonCode().isEmpty()) {
//                exceptionMessage = "No Reason Code Selected/Please select a reason code and try again.";
//            } else if (logIssueRequest.getIssueType() == null || logIssueRequest.getIssueType().isEmpty()) {
//                exceptionMessage = "Please provide issue type";
//            } else if (logIssueRequest.getDatetimeRT() == null || logIssueRequest.getDatetimeRT().isEmpty()) {
//                exceptionMessage = "Please provide timestamp";
//            } else if (logIssueRequest.getGatewayUuid() == null || logIssueRequest.getGatewayUuid().isEmpty()) {
//                exceptionMessage = "No gateway Selected/Please select a gateway and try again.";
//            }
//            throw new InstallerException(exceptionMessage);
//        }
//    
//	}
//	

//
//	@Override
//	public TpmsSensorCountDTO getSensorCount(String sensorUuid) {
//	TpmsSensorCountDTO tpms = new TpmsSensorCountDTO();
//	Integer count = 0;	
//	List<String> ins = installLogRepository.findDistinctByInstanceType(sensorUuid);
//		count = ins.size();
//		tpms.setCount(count);
//		tpms.setUuid(sensorUuid);
//		return tpms;
//	}
//
//	@Override
//	public String checkIsAutoResetInstallationIsApplicableForCurrentInstallation(FinishInstallRequest finishInstallRequest,Context context) {
//		InstallHistory installHistory = installHistoryRepository.findByInstallCode(finishInstallRequest.getInstallUuid());
//		 AutoresetResponse autoresetResponse = new  AutoresetResponse();
//		try {
//			  autoresetResponse = checkIsAutoResetInstallationIsApplicableForCurrentInstallation(installHistory, context);
//         	if(autoresetResponse.isStatus()) {
// 				logger.info("Reset Installation done successfully for compnay : " + installHistory.getCompany().getAccountNumber() + " installation id : " + installHistory.getUuid());
// 			}
//			} catch (Exception e) {
//				logger.error("Exception getting while checking isAutoResetInstallation" + e.getMessage());
//			}
//		return autoresetResponse.getDeviceId();
//	}
//
//	@Override
//	public Boolean resetGatewayStatus(String deviceId) {
//		Gateway gateway = restUtils.getGatewayByImei(deviceId);
//		try {
//			InstallationStatusGatewayRequest installationStatusGatewayRequest = new InstallationStatusGatewayRequest();
//			installationStatusGatewayRequest.setGatewayUuid(gateway.getUuid());
//			installationStatusGatewayRequest.setStatus("PENDING");
//			restUtils.resetGateway(installationStatusGatewayRequest);
//		} catch (Exception e) {
//			logger.error("Exception getting while checking isAutoResetInstallation" + e.getMessage());
//		}
//		return null;
//	}
//
	@Override
	public Boolean markInstallationToInProgress(String installCode, Long userId, Context context) {

		String methodName = "markInstallationToInProgress";
		String logUUid = context.getLogUUId();
		Logutils.log(className, methodName, logUUid,
				" Inside markInstallationToInProgress Method From InstallerService " + "installCode " + installCode,
				logger);
		Boolean status = false;

		if (utilities.checkInputValue(installCode)) {
			Logutils.log(className, methodName, context.getLogUUId(), "  Install Code : ", logger, installCode);
			Logutils.log(className, methodName, " Before calling restUtils.getUserFromAuthService method ", logger);
			User user = restUtils.getUserFromAuthService(logUUid, userId);

			Logutils.log(className, methodName, " Before calling installHistoryRepository.findByInstallCode method ",
					logger);

			InstallHistory installHistory = installHistoryRepository.findByInstallCode(installCode);

			Logutils.log(className, methodName, " After calling installHistoryRepository.findByInstallCode method ",
					logger);

			if (installHistory != null && !installHistory.getStatus().equals(InstallHistoryStatus.STARTED)) {

				installHistory.setUpdatedBy(user);
				installHistory.setUpdatedOn(Instant.now());
				installHistory.setStatus(InstallHistoryStatus.STARTED);
				Logutils.log(className, methodName, context.getLogUUId(), "  Install History After Finding : ", logger,
						installHistory.getStatus().getValue());
				Device device = installHistory.getDevice();
				Asset asset = installHistory.getAsset();
				if (device != null && asset != null) {
					device.setStatus(DeviceStatus.INSTALL_IN_PROGRESS);
					asset.setStatus(AssetStatus.INSTALL_IN_PROGRESS);
					UpdateGatewayAssetStatusRequest updateGatewayAssetStatusRequest = new UpdateGatewayAssetStatusRequest(
							device.getUuid(), asset.getUuid(), DeviceStatus.INSTALL_IN_PROGRESS,
							AssetStatus.INSTALL_IN_PROGRESS, logUUid);
					Logutils.log(className, methodName, context.getLogUUId(),
							" Before calling restUtils.updateGatewayAndAssetStatus method  ", logger);

					device = restUtils.updateGatewayAndAssetStatus(updateGatewayAssetStatusRequest);
					String gatewayIdLog = "GatewayId: ";
					if (device != null) {
						gatewayIdLog = gatewayIdLog + device.getId();
						Logutils.log(className, methodName, context.getLogUUId(),
								" gateway found after restutil call to update gateway status method  ", logger,
								gatewayIdLog);

					}
				}
				Logutils.log(logUUid, " Before calling installHistoryRepository.save method", logger);
				installHistory = installHistoryRepository.save(installHistory);
				Logutils.log(className, methodName, context.getLogUUId(),
						"  Install History After Updating Install History Status : "
								+ installHistory.getStatus().getValue() + " Gateway Status : "
								+ installHistory.getDevice().getStatus().getValue() + " Asset Status : "
								+ installHistory.getAsset().getStatus().getValue(),
						logger, installHistory.getStatus().getValue());
				Logutils.log(className, methodName, context.getLogUUId(),
						" After calling installLogRepository.save method  ", logger);
				status = true;
				logger.info(" Exiting from markInstallationToInProgress Method From InstallerService");
			} else {
				logger.error("Exception occurred while marking install as in progress",
						"No InstallHistory found for Install UUID");
				throw new InstallerException("No InstallHistory found for Install UUID");
			}
		}
		Logutils.log(className, methodName, logUUid, " Exiting From InstallerService ", logger);
		return status;
	}
//	@Override
//	public Page<GatewayDetailsBean> getGatewayDetailsWithPagination(String installCode, String can, Long userId,Context context,Integer page, Integer pageSize, String sort, String order) {
//        long totalElements = 0;
//        Pageable pageable = null;
//		String methodName = "getGatewayDetails";
//		List<GatewayDetailsBean> gatewatDetailList = new ArrayList<>();
//		List<Gateway> gatewayList = new ArrayList<>();
//		String userIdLog = "UserId: "+userId;
//        Logutils.log(className,methodName,context.getLogUUId()," Before calling restUtils.getUserFromAuthService method ",logger ,userIdLog );
//        User user = restUtils.getUserFromAuthService(userId);
//		if (installCode == null && can == null) {
//			Logutils.log(className,methodName,context.getLogUUId(),"value of install code and can is null and role of user is ",logger ,user.getCompany().getType().getValue() );
//			if (user.getCompany().getType().equals(CompanyType.INSTALLER)) {
//				List<Company> companies = user.getCompany().getAccessList();
//				if (companies.size() > 0) {
//					List<String> canList = new ArrayList<String>();
//					
//					
//					
//					for (Company com : companies) {
//						canList.add(com.getAccountNumber());
//
//					}
//						ResponseEntity<RestResponsePage<Gateway>> result  = restUtils
//								.getGatewaysByCANAndStatusFromDeviceServiceWithPagination(null,page,pageSize,sort,order,canList.toString());
//						List<Gateway> list = result.getBody().getContent();
//						 totalElements = result.getBody().getTotalElements();
//						pageable = result.getBody().getPageable();
//						gatewayList.addAll(list);					}
//				
//			}
//			if (user.getCompany().getType().equals(CompanyType.CUSTOMER)) {
//				Company companies = user.getCompany();
//				if (companies != null) {
//					ResponseEntity<RestResponsePage<Gateway>> result  = restUtils
//							.getGatewaysByCANAndStatusFromDeviceServiceWithPagination(companies.getAccountNumber(),page,pageSize,sort,order,null);
//					
//					List<Gateway> list = result.getBody().getContent();
//					 totalElements = result.getBody().getTotalElements();
//					 pageable = result.getBody().getPageable();
//
//					gatewayList.addAll(list);
//				}
//			}
//			if (user.getCompany().getType().equals(CompanyType.MANUFACTURER)) {
//				
//				List<Company> list = restUtils.getCompanyByCustomer();
//					if (list.size() > 0) {
//						List<String> canList = new ArrayList<String>();
// 						
//						for (Company com : list) {
//							canList.add(com.getAccountNumber());
// 						}
//						ResponseEntity<RestResponsePage<Gateway>> result  = restUtils
//									.getGatewaysByCANAndStatusFromDeviceServiceWithPagination( null,page,pageSize,sort,order,canList.toString());
//						List<Gateway> list1 = result.getBody().getContent();
//						 totalElements = result.getBody().getTotalElements();
//						pageable = result.getBody().getPageable();
//
//						gatewayList.addAll(list1);						}
//				
//			}
//		}
//        String installCodeLog = "InstallCode: "+installCode;
//
//        if (installCode != null && !installCode.isEmpty() && can == null) {
//            Logutils.log(className,methodName,context.getLogUUId()," Before calling installHistoryRepository.findByInstallCode method ",logger ,installCodeLog );
//
//            InstallHistory installHistory1 = installHistoryRepository.findByInstallCode(installCode);
//			gatewayList.add(installHistory1.getGateway());
//		}
//		if (installCode == null && can != null && !can.isEmpty()) {
//		    String canLog ="Can: "+can;
//            Logutils.log(className,methodName,context.getLogUUId()," Before calling restUtils.getGatewaysByCANAndStatusFromDeviceService method ",logger ,canLog );
//
//            ResponseEntity<RestResponsePage<Gateway>> result  = restUtils.getGatewaysByCANAndStatusFromDeviceServiceWithPagination(can,page,pageSize,sort,order,null);
//            
//            gatewayList = result.getBody().getContent();
//			totalElements = result.getBody().getTotalElements();
//			pageable = result.getBody().getPageable();
//
//
//		}
//
//		if (installCode != null && !installCode.isEmpty() && can != null && !can.isEmpty()) {
//            Logutils.log(className,methodName,context.getLogUUId()," Before calling installHistoryRepository.findByInstallCode method ",logger , installCodeLog);
//
//            InstallHistory installHistory = installHistoryRepository.findByInstallCode(installCode);
//			if (installHistory.getCompany().getAccountNumber().equalsIgnoreCase(can)) {
//				gatewayList.add(installHistory.getGateway());
//			} else {
//				throw new InstallerException("provided Install Code and Account Number is not matching");
//			}
//		}
//			if (gatewayList.size() > 0) {
//				for (Gateway gat : gatewayList) {
//				    String productNameLog ="ProductName: "+gat.getProductName();
//				    Logutils.log(className,methodName,context.getLogUUId()," Inside iterating Gateway LIst to gate product Name ",logger ,productNameLog );
//                    String uuidLog ="Uuid: "+ gat.getUuid();
//					List<InstallInstructionBean> sensorInstallInstructions = new ArrayList<>();
//					Map<String, List<ReasonCodeBean>> reasonCodeBeanMap = new HashMap<>();
//					List<AttributeValueResposneDTO> attributeList = new ArrayList<>();
//                    Logutils.log(className,methodName,context.getLogUUId()," Before calling restUtils.getAttributeListByProductName method ",logger , productNameLog , uuidLog);
//
//                    List<Attribute> attributes = restUtils.getAttributeListByProductName(gat.getProductName(),
//							gat.getUuid());
//                    Logutils.log(className,methodName,context.getLogUUId()," after calling restUtils.getAttributeListByProductName method ",logger , productNameLog , uuidLog);
//					GatewayDetailsBean gatewatDetails = new GatewayDetailsBean();
//					gatewatDetails.setGatewayUuid(gat.getUuid());
//					gatewatDetails.setGatewayProductName(gat.getProductName());
//					gatewatDetails.setGatewayProductCode(gat.getProductCode());
//					// gatewatDetails.setEligibleAssetType(ins.getAsset().getGatewayEligibility());
//					if (attributes.size() > 0) {
//						gatewatDetails.set_blocker(attributes.get(0).getProductMaster().isBlocker());
//						for (Attribute att : attributes) {
//							AttributeValueResposneDTO attRes = new AttributeValueResposneDTO();
//							attRes.setApplicable(att.isApplicable());
//							attRes.setAttribute_uuid(att.getUuid());
//							attRes.setAttributeName(att.getAttributeName());
//							attRes.setThresholdValue(att.getAttributeValue());
//							attributeList.add(attRes);
//						}
//					}
//					gatewatDetails.setGatewayAttribute(attributeList);
//                    Logutils.log(className,methodName,context.getLogUUId()," Before calling sensorInstallInstructionRepository.findBySensorProductName method ",logger , productNameLog );
//
//                    List<SensorInstallInstruction> sensorInstallInstructionList = sensorInstallInstructionRepository
//							.findBySensorProductName(gat.getProductName());
//					sensorInstallInstructionList.forEach(sensorInstallInstruction -> {
//						InstallInstructionBean installInstructionBean = new InstallInstructionBean();
//						installInstructionBean
//								.setInstruction(sensorInstallInstruction.getInstallInstruction().getInstruction());
//						installInstructionBean
//								.setSequence(sensorInstallInstruction.getInstallInstruction().getStepSequence());
//						sensorInstallInstructions.add(installInstructionBean);
//					});
//					InstallInstructionComparator comparator = new InstallInstructionComparator();
//					Collections.sort(sensorInstallInstructions, comparator);
//					gatewatDetails.setGatewayInstallInstructions(sensorInstallInstructions);
//                    Logutils.log(className,methodName,context.getLogUUId()," Before calling sensorReasonCodeRepository.findBySensorProductName method ",logger , productNameLog );
//
//                    List<SensorReasonCode> sensorReasonCodeList = sensorReasonCodeRepository
//							.findBySensorProductName(gat.getProductName());
//					sensorReasonCodeList.forEach(sensorReasonCode -> {
//						ReasonCodeBean reasonCodeBean = new ReasonCodeBean();
//						reasonCodeBean.setReasonCode(sensorReasonCode.getReasonCode().getCode());
//						reasonCodeBean.setDisplayName(sensorReasonCode.getReasonCode().getValue());
//						if (reasonCodeBeanMap.get(sensorReasonCode.getReasonCode().getIssueType()) != null) {
//							reasonCodeBeanMap.get(sensorReasonCode.getReasonCode().getIssueType()).add(reasonCodeBean);
//						} else {
//							List<ReasonCodeBean> reasonCodeBeanList = new ArrayList<>();
//							reasonCodeBeanList.add(reasonCodeBean);
//							reasonCodeBeanMap.put(sensorReasonCode.getReasonCode().getIssueType(), reasonCodeBeanList);
//						}
//					});
//					gatewatDetails.setGatewayReasonCodes(reasonCodeBeanMap);
//					gatewatDetailList.add(gatewatDetails);
//				}
//				Page<GatewayDetailsBean> pageOfGatewayDetailsBean = new PageImpl<>(gatewatDetailList, pageable, totalElements);
//
//				
//				return pageOfGatewayDetailsBean;
//			} else {
//				throw new InstallerException("No Gateway for Given Input");
//			}
//	}
//	
//	@Override
//	public Boolean updateLogIssueStatus(LogIssueStatusRequest logIssueStatusRequest,Long userId) {
//        Context context =new Context();
//        String methodName = "updateLogIssueStatus";
//
//        if(logIssueStatusRequest.getLogIssueUuid() !=null && !logIssueStatusRequest.getLogIssueUuid().isEmpty() && logIssueStatusRequest.getStatus() !=null && !logIssueStatusRequest.getStatus().isEmpty()){
//            String logIssueUuidLog = "LogIssueUuid: "+logIssueStatusRequest.getLogIssueUuid();
//            String userIdLog = "UserId: "+userId;
//
//            Logutils.log(className,methodName,context.getLogUUId()," Before calling logIssueRepository.findByLogIssueUuid method ",logger, logIssueUuidLog );
//            LogIssue logIssue = logIssueRepository.findByLogIssueUuid(logIssueStatusRequest.getLogIssueUuid());
//            Logutils.log(className,methodName,context.getLogUUId()," Before calling restUtils.getUserFromAuthService method ",logger, userIdLog );
//            User user = restUtils.getUserFromAuthService(userId);
//			if(logIssue !=null){
//				logIssue.setUpdatedBy(user);
//				String logIssueIdLog = "LogIssue: "+logIssue.getId();
//				logIssue.setStatus(LogIssueStatus.getLogIssueStatus(logIssueStatusRequest.getStatus()));
//                Logutils.log(className,methodName,context.getLogUUId()," Before calling logIssueRepository.save method ",logger, logIssueIdLog );
//                logIssueRepository.save(logIssue);
//			}
//			else {
//                throw new InstallerException("No log issue found for uuid");
//            }
//		}
//		else {
//            throw new InstallerException("Please provide uuid and status.");
//        }
//		return true;
//	}
//
//	
//}
//class InstallInstructionComparator implements Comparator<InstallInstructionBean> {
//    @Override
//    public int compare(InstallInstructionBean x, InstallInstructionBean y) {
//        int seqComparison = compare(x.getSequence(), y.getSequence());
//        return seqComparison;
//    }
//    private static int compare(int a, int b) {
//        return a < b ? -1
//                : a > b ? 1
//                : 0;
//    } 

	@Override
	public LogIssue logIssue(LogIssueRequest logIssueRequest, Long userId, String logUUid) {
		String methodName = "logIssue";
		Logutils.log(className, methodName, logUUid,
				" Inside logIssue Method From InstallerService " + " LogIssueRequest " + logIssueRequest, logger);
		if (utilities.checkInputValue(logIssueRequest.getInstallCode()) && logIssueRequest.getReasonCode() != null
				&& utilities.checkInputValue(logIssueRequest.getDatetimeRT())
				&& utilities.checkInputValue(logIssueRequest.getIssueType())
				&& utilities.checkInputValue(logIssueRequest.getSensorUuid())) {

			String installCodeLog = "InstallCode: " + logIssueRequest.getInstallCode();
			String userIdLog = "UserId: " + userId;
			String sensorUuidLog = "SensorUuid: " + logIssueRequest.getSensorUuid();
			String reasonCodeLog = "ReasonCode: " + logIssueRequest.getReasonCode();
			String issueTypeLog = "IssueType: " + logIssueRequest.getIssueType();

			Logutils.log(logUUid, " Before calling installHistoryRepository.findByInstallCode method", logger,
					installCodeLog, sensorUuidLog, reasonCodeLog);

			InstallHistory installHistory = installHistoryRepository
					.findByInstallCode(logIssueRequest.getInstallCode());

			String InstallHistoryIdLog = "InstallHistoryId: " + installHistory.getId();
			Logutils.log(logUUid, " After calling installHistoryRepository.findByInstallCode method ", logger,
					InstallHistoryIdLog);

			if (installHistory == null) {
				throw new InstallerException("No install history found for Install Code");
			}

			Logutils.log(className, methodName, logUUid, " Before calling restUtils.getUserFromAuthService method",
					logger);
			User user = restUtils.getUserFromAuthService(logUUid, userId);

			Logutils.log(className, methodName, logUUid, " Before calling restUtils.getSensorBySensorUuid method",
					logger);
			Device device = restUtils.getSensorBySensorUuid(logUUid, logIssueRequest.getSensorUuid()).get(0);

			String sensorIdLog = "SensorId: ";
			String productNameLog = "ProductName: ";
			if (device != null) {
				sensorIdLog = sensorIdLog + device.getId();
				productNameLog = productNameLog + device.getProductName();
				Logutils.log(className, methodName, logUUid, "  Uuid : " + device.getUuid(), logger, sensorIdLog,
						productNameLog);
			}

			ReasonCode reasonCode = null;
			if (device != null) {
				if (!logIssueRequest.getReasonCode().isEmpty()) {

					Logutils.log(logUUid, " Before calling reasonCodeRepository.findByCodeAndIssueType method", logger,
							reasonCodeLog);
					reasonCode = reasonCodeRepository.findByCodeAndIssueType(logIssueRequest.getReasonCode(),
							logIssueRequest.getIssueType());

					Logutils.log(logUUid, " After calling reasonCodeRepository.findByCodeAndIssueType method", logger,
							reasonCodeLog);

					if (reasonCode == null) {
						throw new InstallerException("No Reason Code found for given code");
					}
				} else {

					Logutils.log(logUUid,
							" Before calling sensorReasonCodeRepository.findBySensorProductNameAndIssueType method",
							logger, productNameLog, issueTypeLog);

					List<SensorReasonCode> sensorReasonCodeList = sensorReasonCodeRepository
							.findBySensorProductNameAndIssueType(device.getProductName(),
									logIssueRequest.getIssueType());
					if (sensorReasonCodeList != null && !sensorReasonCodeList.isEmpty()) {
						Logutils.log(logUUid, " Size of sensorReasonCodeList : " + sensorReasonCodeList.size(), logger);
						throw new InstallerException("No Reason Code found for given code");
					}
				}

				// List<String> sensorUuidList =
				// installHistory.getDevice().getSensors().stream().map(sensor1 ->
				// sensor1.getUuid()).collect(Collectors.toList());

				Logutils.log(logUUid, " Before calling deviceDeviceXrefRepository.findByDeviceUUID method", logger,
						sensorUuidLog);
				List<Device_Device_xref> deviceUuidList = deviceDeviceXrefRepository
						.findByDeviceUUID(logIssueRequest.getSensorUuid());
				if (deviceUuidList != null && deviceUuidList.size() > 0) {

					Logutils.log(className, methodName, logUUid, " list of deviceUuid : " + deviceUuidList, logger);
				}

				List<Device> sensorUuidList = new ArrayList<>();
				for (Device_Device_xref sensorUuidList1 : deviceUuidList) {

					Device device1 = sensorUuidList1.getSensorUuid();
					sensorUuidList.add(device1);

				}

				for (Device device2 : sensorUuidList) {

					if (!device2.getUuid().equalsIgnoreCase(logIssueRequest.getSensorUuid())) {
						throw new InstallerException("Sensor does not belong to given install code");
					} else {
						Logutils.log(logUUid,
								" Before calling sensorReasonCodeRepository.findBySensorProductNameAndIssueType method ",
								logger, productNameLog, issueTypeLog);

						List<SensorReasonCode> sensorReasonCodeList = sensorReasonCodeRepository
								.findBySensorProductNameAndIssueType(device2.getProductName(),
										logIssueRequest.getIssueType());
						List<String> reasonCodeForSensor = sensorReasonCodeList.stream()
								.map(reasonCode1 -> reasonCode1.getReasonCode().getCode()).collect(Collectors.toList());
						if (utilities.checkInputValue(reasonCodeForSensor)
								&& !reasonCodeForSensor.contains(reasonCode.getCode())) {
							throw new InstallerException("Reason code is not applicable for given sensor");
						}
					}

				}
			} else {
				throw new InstallerException("No sensor found for given Sensor UUID");
			}

			LogIssue logIssue = null;
			if (utilities.checkInputValue(logIssueRequest.getIssueUuid())) {

				Logutils.log(logUUid, " Before calling logIssueRepository.findByLogIssueUuid method ",
						"IssueUuid :" + logIssueRequest.getIssueUuid(), logger);
				logIssue = logIssueRepository.findByLogIssueUuid(logIssueRequest.getIssueUuid());
				if (logIssue != null) {
					throw new InstallerException("Record alredy Exist");
				}
				if (logIssue == null) {
					logIssue = new LogIssue();
					logIssue.setUuid(UUID.randomUUID().toString());
					logIssue.setCreatedOn(Instant.ofEpochMilli(Long.parseLong(logIssueRequest.getDatetimeRT())));
					logIssue.setCreatedBy(user);
				} else {
					logIssue.setUpdatedBy(user);
					logIssue.setCreatedOn(Instant.ofEpochMilli(Long.parseLong(logIssueRequest.getDatetimeRT())));
				}
				logIssue.setUuid(logIssueRequest.getIssueUuid());
			} else {
				logIssue = new LogIssue();
				logIssue.setUuid(UUID.randomUUID().toString());
				logIssue.setCreatedOn(Instant.ofEpochMilli(Long.parseLong(logIssueRequest.getDatetimeRT())));
				logIssue.setCreatedBy(user);
			}

			logIssue.setInstallHistory(installHistory);
			logIssue.setIssueType(logIssueRequest.getIssueType());
			if (logIssueRequest.getSensorId() != null) {
				logIssue.setSensorId(logIssueRequest.getSensorId());
				;
			}
			logIssue.setReasonCode(reasonCode);
			logIssue.setDevice(device);
			logIssue.setType("SENSOR");
			logIssue.setRelatedUuid(device.getUuid());
			logIssue.setComment(logIssueRequest.getComment());
			logIssue.setData(logIssueRequest.getData());
			logIssue.setStatus(LogIssueStatus.getLogIssueStatus(logIssueRequest.getStatus()));
			Logutils.log(logUUid, " Before calling logIssueRepository.save method ", logger);
			logIssue = logIssueRepository.save(logIssue);
			String logIssueIdLog = "LogIssueId: ";
			if (logIssue != null) {
				logIssueIdLog = logIssueIdLog + logIssue.getId();
			}
			Logutils.log(logUUid, " After calling logIssueRepository.save method ", logger, logIssueIdLog);

			return logIssue;
		} else {
			String exceptionMessage = "";
			if (logIssueRequest.getInstallCode() == null || logIssueRequest.getInstallCode().isEmpty()) {
				exceptionMessage = "Please provide install code";
			} else if (logIssueRequest.getReasonCode() == null || logIssueRequest.getReasonCode().isEmpty()) {
				exceptionMessage = "No Reason Code Selected/Please select a reason code and try again.";
			} else if (logIssueRequest.getIssueType() == null || logIssueRequest.getIssueType().isEmpty()) {
				exceptionMessage = "Please provide issue type";
			} else if (logIssueRequest.getDatetimeRT() == null || logIssueRequest.getDatetimeRT().isEmpty()) {
				exceptionMessage = "Please provide timestamp";
			} else if (logIssueRequest.getSensorUuid() == null || logIssueRequest.getSensorUuid().isEmpty()) {
				exceptionMessage = "No Sensor Selected/Please select a sensor and try again.";
			}
			throw new InstallerException(exceptionMessage);
		}
	}

//---------------------Aamir 1 Start-------------------------------------------------// 

	@Override
	public Boolean resetCompanyData(String companyUuid, Context context) {
		final String methodName = "resetCompanyData";
		Logutils.log(className, methodName, context.getLogUUId(),
				" Inside resetCompanyData Method From InstallerService " + "companyUuid " + companyUuid, logger);
		String companyUuidLog = "CompanyUuid: " + companyUuid;
		Logutils.log(className, methodName, context.getLogUUId(),
				" Before calling restUtils.getCompanyByUuidFromCompanyService method ", logger, companyUuidLog);

		Organisation organisation = restUtils.getOrganisationByUuidFromCompanyService(companyUuid);
		String companyIdLog = "CompanyId: ";
		String companyAccountNumLog = "AccountNumber: ";

		if (organisation != null) {
			companyIdLog = companyIdLog + organisation.getId();
			companyAccountNumLog = companyAccountNumLog + organisation.getAccountNumber();
		}
		Logutils.log(className, methodName, context.getLogUUId(),
				" After calling restUtils.getCompanyByUuidFromCompanyService method ", logger, companyIdLog);

		Logutils.log(className, methodName, context.getLogUUId(),
				" Before calling installHistoryRepository.findByCompanyUuid method ", logger, companyUuidLog);

		List<InstallHistory> installHistoryList = installHistoryRepository.findByCompanyUuid(companyUuid);
		Logutils.log(className, methodName, context.getLogUUId(),
				" After calling installHistoryRepository.findByCompanyUuid method ", logger);

		installHistoryList.forEach(installHistory -> {
			Logutils.log(className, methodName, context.getLogUUId(),
					" Before calling installLogRepository.findByInstallHistory method ", logger);

			List<InstallLog> installLogList = installLogRepository.findByInstallHistory(installHistory.getId());
			installLogList.forEach(installLog -> {
				Logutils.log(className, methodName, context.getLogUUId(),
						" Before calling installLogRepository.delete method ", logger);
				installLogRepository.delete(installLog);
			});
			Logutils.log(className, methodName, context.getLogUUId(),
					" Before calling logIssueRepository.findByInstallCode method ", logger);
			List<LogIssue> logIssueList = logIssueRepository.findByInstallCode(installHistory.getInstallCode());
			logIssueList.forEach(logIssue -> {
				Logutils.log(className, methodName, context.getLogUUId(),
						" Before calling logIssueRepository.delete method ", logger);
				logIssueRepository.delete(logIssue);
			});
			Logutils.log(className, methodName, context.getLogUUId(),
					" Before calling installHistoryRepository.delete method ", logger);
			installHistoryRepository.delete(installHistory);
		});
		Logutils.log(className, methodName, context.getLogUUId(), " Before calling restUtils.getSensorByCan method ",
				logger, companyAccountNumLog);

		List<DeviceResponsePayload> deviceList = restUtils.getSensorByCan(organisation.getAccountNumber());
		Logutils.log(className, methodName, context.getLogUUId(), " AFTER calling restUtils.getSensorByCan method ",
				logger, companyAccountNumLog);
		deviceList.forEach(device -> {
			List<InstallLog> installLogList = installLogRepository.findBySensorUuid(device.getUuid());
			Logutils.log(className, methodName, context.getLogUUId(), " finding  install log findBySensorUuid method ",
					logger);
			installLogList.forEach(installLog -> {
				installLogRepository.delete(installLog);
				Logutils.log(className, methodName, context.getLogUUId(),
						" finding  deleting install log findBySensorUuid method ", logger);
			});
			List<LogIssue> logIssueList = logIssueRepository.findBySensorUuid(device.getUuid());
			logIssueList.forEach(logIssue -> {
				logIssueRepository.delete(logIssue);
			});
		});
		Logutils.log(className, methodName, context.getLogUUId(), " Exiting From InstallerService ", logger);
		return true;
	}

	@Override
	public Boolean resetGatewayData(String deviceUuid) {
		String methodName = "resetGatewayData";
		Context context = new Context();
		Logutils.log(className, methodName, context.getLogUUId(),
				" Inside resetGatewayData Method From InstallerService " + "deviceUuid " + deviceUuid, logger);
		String gatewayUuidLog = "GatewayUuid: " + deviceUuid;
		Logutils.log(className, methodName, context.getLogUUId(),
				" Before calling installHistoryRepository.findByGatewayUuid method ", logger, gatewayUuidLog);

		List<InstallHistory> installHistoryList = installHistoryRepository.findByGatewayUuid(deviceUuid);
		installHistoryList.forEach(installHistory -> {
			Logutils.log(className, methodName, context.getLogUUId(),
					" Before calling logIssueRepository.findByInstallCode method ", logger, gatewayUuidLog);

			List<LogIssue> logIssues = logIssueRepository.findByInstallCode(installHistory.getInstallCode());
			logIssues.forEach(logIssue -> {
				logIssueRepository.delete(logIssue);
			});
			Logutils.log(className, methodName, context.getLogUUId(),
					" Before calling deviceDeviceXrefRepository.findByGateway method ", logger, gatewayUuidLog);

			List<Device_Device_xref> deviceSensorxreffList = deviceDeviceXrefRepository.findByGateway(deviceUuid);
			deviceSensorxreffList.forEach(sensor -> {
				Logutils.log(className, methodName, context.getLogUUId(),
						" Before calling logIssueRepository.findBySensorUuid method ", logger, gatewayUuidLog);

				List<LogIssue> logIssueList = logIssueRepository.findBySensorUuid(sensor.getDeviceUuid().getUuid());
				logIssueList.forEach(logIssue -> {
					Logutils.log(className, methodName, context.getLogUUId(),
							" Before calling logIssueRepository.delete method ", logger);
					logIssueRepository.delete(logIssue);
				});
			});

			String installHistoryIdLog = "InstallHistoryId :" + installHistory.getId();
			Logutils.log(className, methodName, context.getLogUUId(),
					" Before calling installLogRepository.findByInstallHistory method ", logger, installHistoryIdLog);

			List<InstallLog> installLogList = installLogRepository.findByInstallHistory(installHistory.getId());
			installLogList.forEach(installLog -> {
				Logutils.log(className, methodName, context.getLogUUId(),
						" Before calling logIssueRepository.delete method ", logger);
				installLogRepository.delete(installLog);
			});

			List<Device_Device_xref> deviceSensorxreffList1 = deviceDeviceXrefRepository.findByGateway(deviceUuid);
			deviceSensorxreffList1.forEach(sensor -> {
				List<InstallLog> installLogs = installLogRepository.findBySensorUuid(sensor.getDeviceUuid().getUuid());
				installLogs.forEach(installLog -> {
					installLogRepository.delete(installLog);
				});
			});

			Logutils.log(className, methodName, context.getLogUUId(),
					" Before calling installHistoryRepository.delete method ", logger, installHistoryIdLog);

			installHistoryRepository.delete(installHistory);
		});

		Logutils.log(className, methodName, context.getLogUUId(), " Exiting From InstallerService ", logger);
		return true;
	}

	@Override
	public LogIssue logIssueForGateway(LogIssueGatewayRequest logIssueRequest, Long userId, Context context) {
		String methodName = "logIssueForGateway";

		String logUUid = context.getLogUUId();
		Logutils.log(className, methodName, logUUid, " Inside logIssueForGateway Method From InstallerService "
				+ " LogIssueGatewayRequest " + logIssueRequest, logger);

		if (utilities.checkInputValue(logIssueRequest.getInstallCode())
				&& utilities.checkInputValue(logIssueRequest.getReasonCode())
				&& utilities.checkInputValue(logIssueRequest.getDatetimeRT())
				&& utilities.checkInputValue(logIssueRequest.getIssueType())
				&& utilities.checkInputValue(logIssueRequest.getDeviceUuid())) {
			String userIdLog = "UserId: " + userId;
			String gatewayUuidLog = "GatewayUuid: " + logIssueRequest.getDeviceUuid();
			String installCodeLog = "InstallCode: " + logIssueRequest.getInstallCode();
			String issueTypeLog = "IssueType: " + logIssueRequest.getIssueType();
			String reasonCodeLog = "ReasonCode: " + logIssueRequest.getReasonCode();

			Logutils.log(className, methodName, context.getLogUUId(),
					" Before calling installHistoryRepository.findByInstallCode method ", logger, installCodeLog);

			InstallHistory installHistory = installHistoryRepository
					.findByInstallCode(logIssueRequest.getInstallCode());
			if (installHistory == null) {
				throw new InstallerException("No install history found for Install Code");
			}
			Logutils.log(className, methodName, context.getLogUUId(),
					" Before calling restUtils.getUserFromAuthService method ", logger, userIdLog);
			User user = restUtils.getUserFromAuthService(logUUid, userId);

			Logutils.log(className, methodName, context.getLogUUId(),
					" Before calling restUtils.getGatewayByUuid method ", logger, gatewayUuidLog);

			Device device1 = restUtils.getGatewayByUuid(logIssueRequest.getDeviceUuid());
			ReasonCode reasonCode = null;
			if (device1 != null) {
				String productNameLog = "ProductName: " + device1.getProductName();
				if (!logIssueRequest.getReasonCode().isEmpty()) {
					Logutils.log(className, methodName, context.getLogUUId(),
							" Before calling reasonCodeRepository.findByCodeAndIssueType method ", logger,
							reasonCodeLog, issueTypeLog);

					reasonCode = reasonCodeRepository.findByCodeAndIssueType(logIssueRequest.getReasonCode(),
							logIssueRequest.getIssueType());
					if (reasonCode == null && logIssueRequest.getReasonCode() != null
							&& "".equalsIgnoreCase(logIssueRequest.getReasonCode())) {
						throw new InstallerException("No Reason Code found for given code");
					}
				} else {
					Logutils.log(className, methodName, context.getLogUUId(),
							" Before calling sensorReasonCodeRepository.findBySensorProductNameAndIssueType method ",
							logger, reasonCodeLog, issueTypeLog);

					List<SensorReasonCode> sensorReasonCodeList = sensorReasonCodeRepository
							.findBySensorProductNameAndIssueType(device1.getProductName(),
									logIssueRequest.getIssueType());
					if (sensorReasonCodeList != null && !sensorReasonCodeList.isEmpty()) {
						// throw new InstallerException("No Reason Code found for given code");
						Logutils.log(logUUid, " Size of sensor Reason Code : " + sensorReasonCodeList.size(), logger);
					}
				}
				String sensorUuidList = installHistory.getDevice().getUuid();
				if (!sensorUuidList.equalsIgnoreCase(logIssueRequest.getDeviceUuid())) {
					throw new InstallerException("Gateway does not belong to given install code");
				} else {
					Logutils.log(className, methodName, context.getLogUUId(),
							" Before calling sensorReasonCodeRepository.findBySensorProductNameAndIssueType method ",
							logger, productNameLog, issueTypeLog);

					List<SensorReasonCode> sensorReasonCodeList = sensorReasonCodeRepository
							.findBySensorProductNameAndIssueType(device1.getProductName(),
									logIssueRequest.getIssueType());
					List<String> reasonCodeForSensor = sensorReasonCodeList.stream()
							.map(reasonCode1 -> reasonCode1.getReasonCode().getCode()).collect(Collectors.toList());
					if (utilities.checkInputValue(reasonCodeForSensor)
							&& utilities.checkInputValue(logIssueRequest.getReasonCode())

							&& !reasonCodeForSensor.contains(reasonCode.getCode())) {
						throw new InstallerException("Reason code is not applicable for given gateway");
					}
				}

			} else {
				throw new InstallerException("No gateway found for given Gateway Uuid");
			}

			LogIssue logIssue = new LogIssue();
			logIssue.setInstallHistory(installHistory);
			logIssue.setIssueType(logIssueRequest.getIssueType());
			logIssue.setReasonCode(reasonCode);
			logIssue.setType("GATEWAY");
			logIssue.setDevice(device1);
			logIssue.setRelatedUuid(device1.getUuid());
			logIssue.setCreatedOn(Instant.ofEpochMilli(Long.parseLong(logIssueRequest.getDatetimeRT())));
			logIssue.setComment(logIssueRequest.getComment());
			logIssue.setData(logIssueRequest.getData());
			logIssue.setStatus(LogIssueStatus.getLogIssueStatus(logIssueRequest.getStatus()));
			logIssue.setUuid(UUID.randomUUID().toString());
			logIssue.setCreatedBy(user);
			Logutils.log(className, methodName, context.getLogUUId(), " Before calling logIssueRepository.save method ",
					logger);
			logIssue = logIssueRepository.save(logIssue);
			Logutils.log(className, methodName, logUUid, " Exiting From InstallerService ", logger);
			return logIssue;
		} else {
			String exceptionMessage = "";
			if (logIssueRequest.getInstallCode() == null || logIssueRequest.getInstallCode().isEmpty()) {
				exceptionMessage = "Please provide install code";
			} else if (logIssueRequest.getReasonCode() == null || logIssueRequest.getReasonCode().isEmpty()) {
				exceptionMessage = "No Reason Code Selected/Please select a reason code and try again.";
			} else if (logIssueRequest.getIssueType() == null || logIssueRequest.getIssueType().isEmpty()) {
				exceptionMessage = "Please provide issue type";
			} else if (logIssueRequest.getDatetimeRT() == null || logIssueRequest.getDatetimeRT().isEmpty()) {
				exceptionMessage = "Please provide timestamp";
			} else if (logIssueRequest.getDeviceUuid() == null || logIssueRequest.getDeviceUuid().isEmpty()) {
				exceptionMessage = "No gateway Selected/Please select a gateway and try again.";
			}
			throw new InstallerException(exceptionMessage);
		}

	}

	@Override
	public DeviceDetailsResponse getGatewayDetails(String installCode, String can, Long userId, String logUUid) {

		String methodName = "getGatewayDetails";
		Logutils.log(className, methodName, logUUid,
				" Inside getGatewayDetails Method From InstallerService " + "installCode " + installCode, logger);
		DeviceDetailsResponse deviceDetailsResponse = new DeviceDetailsResponse();
		List<DeviceDetailsBean> deviceDetailsBeanList = new ArrayList<>();
		List<Device> deviceList = new ArrayList<>();
		String userIdLog = "UserId: " + userId;
		Logutils.log(className, methodName, logUUid, " Before calling restUtils.getUserFromAuthService method ", logger,
				userIdLog);

		User user = restUtils.getUserFromAuthService(logUUid, userId);
		if (installCode == null && can == null) {
			/*
			 * Logutils.log(logUUid,
			 * "value of install code and can is null and role of user is ", logger,
			 * user.getOrganisation().getType().getValue());
			 */
			if (user.getOrganisation().getOrganisationRole().contains(OrganisationRole.INSTALLER)) {
				List<Organisation> organisation = user.getOrganisation().getAccessList();
				if (organisation.size() > 0) {
					for (Organisation com : organisation) {

						Logutils.log(className, methodName, logUUid,
								" Before calling restUtils.getGatewaysByCANAndStatusFromDeviceService method ", logger);
						List<Device> list = restUtils.getGatewaysByCANAndStatusFromDeviceService(logUUid,
								com.getAccountNumber(), null);
						deviceList.addAll(list);
					}
				}
			}
			if (user.getOrganisation().getOrganisationRole().contains(OrganisationRole.END_CUSTOMER)) {
				Organisation organisation = user.getOrganisation();
				if (organisation != null) {
					Logutils.log(className, methodName, logUUid,
							" Before calling restUtils.getGatewaysByCANAndStatusFromDeviceService method ", logger);
					List<Device> list = restUtils
							.getGatewaysByCANAndStatusFromDeviceService(organisation.getAccountNumber(), null, logUUid);
					deviceList.addAll(list);
				}
			}
			if (user.getOrganisation().getOrganisationRole().contains(OrganisationRole.MANUFACTURER)) {
				Logutils.log(className, methodName, logUUid, " Before calling restUtils.getCompanyByCustomer method ",
						logger);
				List<Organisation> list = restUtils.getCompanyByCustomer();
				if (list.size() > 0) {
					for (Organisation com : list) {
						Logutils.log(className, methodName, logUUid,
								" Before calling restUtils.getGatewaysByCANAndStatusFromDeviceService method ", logger);
						List<Device> list1 = restUtils
								.getGatewaysByCANAndStatusFromDeviceService(com.getAccountNumber(), null, logUUid);
						deviceList.addAll(list1);
					}
				}

			}
		}
		String installCodeLog = "InstallCode: " + installCode;

		if (utilities.checkInputValue(installCode) && can == null) {
			Logutils.log(logUUid, " Before calling installHistoryRepository.findByInstallCode method ", logger,
					installCodeLog);
			InstallHistory installHistory1 = installHistoryRepository.findByInstallCode(installCode);
			if (installHistory1 != null) {
				deviceList.add(installHistory1.getDevice());
			}

		}
		if (installCode == null && utilities.checkInputValue(can)) {
			String canLog = "Can: " + can;
			Logutils.log(logUUid, " Before calling restUtils.getGatewaysByCANAndStatusFromDeviceService method ",
					logger, canLog);

			deviceList = restUtils.getGatewaysByCANAndStatusFromDeviceService(can, null, logUUid);
		}

		if (utilities.checkInputValue(installCode) && utilities.checkInputValue(can)) {
			Logutils.log(logUUid, " Before calling installHistoryRepository.findByInstallCode method ", logger,
					installCodeLog);

			InstallHistory installHistory = installHistoryRepository.findByInstallCode(installCode);
			if (installHistory.getOrganisation().getAccountNumber().equalsIgnoreCase(can)) {
				deviceList.add(installHistory.getDevice());
			} else {
				throw new InstallerException("provided Install Code and Account Number is not matching");
			}
		}
		if (deviceList.size() > 0) {
			for (Device device : deviceList) {
				String productNameLog = "ProductName: " + device.getProductName();
				Logutils.log(logUUid, " Inside iterating Gateway LIst to gate product Name ", logger, productNameLog);
				String uuidLog = "Uuid: " + device.getUuid();
				List<InstallInstructionBean> sensorInstallInstructions = new ArrayList<>();
				Map<String, List<ReasonCodeBean>> reasonCodeBeanMap = new HashMap<>();
				List<AttributeValueResposneDTO> attributeList = new ArrayList<>();

				Logutils.log(className, methodName, logUUid,
						" Before calling restUtils.getAttributeListByProductName method ", logger, productNameLog);
				List<Attribute> attributes = restUtils.getAttributeListByProductName(logUUid, device.getProductName(),
						device.getUuid());

				DeviceDetailsBean deviceDetails = new DeviceDetailsBean();
				deviceDetails.setDeviceUuid(device.getUuid());
				deviceDetails.setDeviceProductName(device.getProductName());
				deviceDetails.setDeviceProductCode(device.getProductCode());
				// gatewatDetails.setEligibleAssetType(ins.getAsset().getGatewayEligibility());
				if (attributes.size() > 0) {
					deviceDetails.set_blocker(attributes.get(0).getProductMaster().isBlocker());
					for (Attribute att : attributes) {
						AttributeValueResposneDTO attRes = new AttributeValueResposneDTO();
						attRes.setApplicable(att.isApplicable());
						attRes.setAttribute_uuid(att.getUuid());
						attRes.setAttributeName(att.getAttributeName());
						attRes.setThresholdValue(att.getAttributeValue());
						attributeList.add(attRes);
					}
				}
				deviceDetails.setDeviceAttribute(attributeList);
				Logutils.log(logUUid,
						" Before calling sensorInstallInstructionRepository.findBySensorProductName method ", logger,
						productNameLog);

				List<SensorInstallInstruction> sensorInstallInstructionList = sensorInstallInstructionRepository
						.findBySensorProductName(device.getProductName());
				sensorInstallInstructionList.forEach(sensorInstallInstruction -> {
					InstallInstructionBean installInstructionBean = new InstallInstructionBean();
					installInstructionBean
							.setInstruction(sensorInstallInstruction.getInstallInstruction().getInstruction());
					installInstructionBean
							.setSequence(sensorInstallInstruction.getInstallInstruction().getStepSequence());
					sensorInstallInstructions.add(installInstructionBean);
				});
				InstallInstructionComparator comparator = new InstallInstructionComparator();
				Collections.sort(sensorInstallInstructions, comparator);
				deviceDetails.setDeviceInstallInstructions(sensorInstallInstructions);
				Logutils.log(logUUid, " Before calling sensorReasonCodeRepository.findBySensorProductName method ",
						logger, productNameLog);

				List<SensorReasonCode> sensorReasonCodeList = sensorReasonCodeRepository
						.findBySensorProductName(device.getProductName());
				sensorReasonCodeList.forEach(sensorReasonCode -> {
					ReasonCodeBean reasonCodeBean = new ReasonCodeBean();
					reasonCodeBean.setReasonCode(sensorReasonCode.getReasonCode().getCode());
					reasonCodeBean.setDisplayName(sensorReasonCode.getReasonCode().getValue());
					if (reasonCodeBeanMap.get(sensorReasonCode.getReasonCode().getIssueType()) != null) {
						reasonCodeBeanMap.get(sensorReasonCode.getReasonCode().getIssueType()).add(reasonCodeBean);
					} else {
						List<ReasonCodeBean> reasonCodeBeanList = new ArrayList<>();
						reasonCodeBeanList.add(reasonCodeBean);
						reasonCodeBeanMap.put(sensorReasonCode.getReasonCode().getIssueType(), reasonCodeBeanList);
					}
				});
				deviceDetails.setDeviceReasonCodes(reasonCodeBeanMap);
				deviceDetailsBeanList.add(deviceDetails);
			}
			deviceDetailsResponse.setDeviceDetailsBean(deviceDetailsBeanList);
			Logutils.log(className, methodName, logUUid, " Exiting From InstallerService ", logger);
			return deviceDetailsResponse;
		} else {
			throw new InstallerException("No Device for Given Input");
		}
	}

	@Override
	public Boolean updateLogIssueStatus(LogIssueStatusRequest logIssueStatusRequest, Long userId) {
		Context context = new Context();
		String methodName = "updateLogIssueStatus";
		Logutils.log(className, methodName, context.getLogUUId(),
				" Inside updateLogIssueStatus Method From InstallerService ", logger);

		if (utilities.checkInputValue(logIssueStatusRequest.getLogIssueUuid())
				&& utilities.checkInputValue(logIssueStatusRequest.getStatus())) {
			String logIssueUuidLog = "LogIssueUuid: " + logIssueStatusRequest.getLogIssueUuid();
			String userIdLog = "UserId: " + userId;

			Logutils.log(className, methodName, context.getLogUUId(),
					" Before calling logIssueRepository.findByLogIssueUuid method ", logger, logIssueUuidLog);
			LogIssue logIssue = logIssueRepository.findByLogIssueUuid(logIssueStatusRequest.getLogIssueUuid());
			Logutils.log(className, methodName, context.getLogUUId(),
					" Before calling restUtils.getUserFromAuthService method ", logger, userIdLog);
			User user = restUtils.getUserFromAuthService("", userId);
			if (logIssue != null) {
				logIssue.setUpdatedBy(user);
				String logIssueIdLog = "LogIssue: " + logIssue.getId();
				logIssue.setStatus(LogIssueStatus.getLogIssueStatus(logIssueStatusRequest.getStatus()));
				Logutils.log(className, methodName, context.getLogUUId(),
						" Before calling logIssueRepository.save method ", logger, logIssueIdLog);
				logIssueRepository.save(logIssue);
			} else {
				throw new InstallerException("No log issue found for uuid");
			}
		} else {
			throw new InstallerException("Please provide uuid and status.");
		}
		Logutils.log(className, methodName, context.getLogUUId(), " Exiting From InstallerService ", logger);
		return true;
	}

	@Override
	public SensorDetailsResponse getSensorDetails(String installCode, Context context) {
		String methodName = "getSensorDetails";

		Logutils.log(className, methodName, context.getLogUUId(),
				" Inside getSensorDetails Method From InstallerService ", logger);
		if (utilities.checkInputValue(installCode)) {
			String installCodeLog = "InstallCode: " + installCode;
			Logutils.log(className, methodName, context.getLogUUId(),
					" Before calling installHistoryRepository.findByInstallCode method ", logger, installCodeLog);

			InstallHistory installHistory = installHistoryRepository.findByInstallCode(installCode);
			if (installHistory != null) {
				SensorDetailsResponse sensorDetailsResponse = new SensorDetailsResponse();
				sensorDetailsResponse.setGatewayUuid(installHistory.getDevice().getUuid());
				List<SensorDetailsBean> sensorDetailsBeanList = new ArrayList<>();
				Logutils.log(className, methodName, context.getLogUUId(),
						" Before calling deviceDeviceXrefRepository.findByGateway method ", logger);

				List<Device_Device_xref> deviceSensorxreffList = deviceDeviceXrefRepository
						.findByGateway(String.valueOf(installHistory.getDevice().getUuid()));
				deviceSensorxreffList.forEach(sensor -> {
					String productCodeLog = "ProductCode: " + sensor.getSensorUuid().getProductCode();
					SensorDetailsBean sensorDetailsBean = new SensorDetailsBean();
					sensorDetailsBean.setSensorUuid(sensor.getSensorUuid().getUuid());
					sensorDetailsBean.setSensorProductCode(sensor.getSensorUuid().getProductCode());
					Logutils.log(className, methodName, context.getLogUUId(),
							" Before calling restUtils.getLookupValueFromDeviceService method ", logger,
							productCodeLog);

					String sensorDisplayName = restUtils
							.getLookupValueFromDeviceService(sensor.getSensorUuid().getProductCode());
					if (utilities.checkInputValue(sensorDisplayName)) {
						sensorDetailsBean.setSensorProductName(sensorDisplayName);
					} else {
						sensorDetailsBean.setSensorProductName(sensor.getSensorUuid().getProductName());
					}
					Map<String, List<ReasonCodeBean>> reasonCodeBeanMap = new HashMap<>();
					List<InstallInstructionBean> sensorInstallInstructions = new ArrayList<>();
					String productNameLog = "ProductName: " + sensor.getSensorUuid().getProductName();

					Logutils.log(className, methodName, context.getLogUUId(),
							" Before calling sensorReasonCodeRepository.findBySensorProductName method ", logger,
							productNameLog);
					List<SensorReasonCode> sensorReasonCodeList = sensorReasonCodeRepository
							.findBySensorProductName(sensor.getSensorUuid().getProductName());
					sensorReasonCodeList.forEach(sensorReasonCode -> {
						ReasonCodeBean reasonCodeBean = new ReasonCodeBean();
						reasonCodeBean.setReasonCode(sensorReasonCode.getReasonCode().getCode());
						reasonCodeBean.setDisplayName(sensorReasonCode.getReasonCode().getValue());
						if (reasonCodeBeanMap.get(sensorReasonCode.getReasonCode().getIssueType()) != null) {
							reasonCodeBeanMap.get(sensorReasonCode.getReasonCode().getIssueType()).add(reasonCodeBean);
						} else {
							List<ReasonCodeBean> reasonCodeBeanList = new ArrayList<>();
							reasonCodeBeanList.add(reasonCodeBean);
							reasonCodeBeanMap.put(sensorReasonCode.getReasonCode().getIssueType(), reasonCodeBeanList);
						}
					});
					sensorDetailsBean.setSensorReasonCodes(reasonCodeBeanMap);
					Logutils.log(className, methodName, context.getLogUUId(),
							" Before calling sensorInstallInstructionRepository.findBySensorProductName method ",
							logger, productNameLog);
					List<SensorInstallInstruction> sensorInstallInstructionList = sensorInstallInstructionRepository
							.findBySensorProductName(sensor.getSensorUuid().getProductName());
					Logutils.log(className, methodName, context.getLogUUId(),
							" After calling sensorInstallInstructionRepository.findBySensorProductName method ", logger,
							productNameLog);
					sensorInstallInstructionList.forEach(sensorInstallInstruction -> {
						InstallInstructionBean installInstructionBean = new InstallInstructionBean();
						installInstructionBean
								.setInstruction(sensorInstallInstruction.getInstallInstruction().getInstruction());
						installInstructionBean
								.setSequence(sensorInstallInstruction.getInstallInstruction().getStepSequence());
						sensorInstallInstructions.add(installInstructionBean);
					});
					sensorDetailsBean.setSensorInstallInstructions(sensorInstallInstructions);
					InstallInstructionComparator comparator = new InstallInstructionComparator();
					Collections.sort(sensorDetailsBean.getSensorInstallInstructions(), comparator);
					sensorDetailsBeanList.add(sensorDetailsBean);
				});
				sensorDetailsResponse.setSensorList(sensorDetailsBeanList);
				Logutils.log(className, methodName, context.getLogUUId(), " Exiting From InstallerService ", logger);

				return sensorDetailsResponse;
			} else {
				throw new InstallerException("No install history found for install code");
			}
		} else {
			throw new InstallerException("Please provide install code and try again");
		}
	}

	// -----------------------------------aamir 1 end
	// ---------------------------------------//

	@Override
	public Page<InstallationSummaryResponseDTO> getAllInstallationSummary(Pageable pageable, String comapnyUuid,
			Long userId, Map<String, String> filterValues, Long days) {
		Context context = new Context();
		String methodName = "getAllInstallationSummary";
		Logutils.log(className, methodName, context.getLogUUId(),
				" Inside getAllInstallationSummary Method From InstallerService ", logger);
		String userIdLog = "UserId: " + userId;
		Logutils.log(className, methodName, context.getLogUUId(),
				" Before calling restUtils.getUserFromAuthService method ", logger, userIdLog);

		User user = restUtils.getUserFromAuthService("", userId);
		Page<InstallHistory> installHistory = null;
		int size = 0;
		Specification<InstallHistory> spc = InstallerHistorySpecification.getaInstallerSpecification(filterValues, user,
				comapnyUuid, days);
		Logutils.log(className, methodName, context.getLogUUId(),
				" Before calling installHistoryRepository.findAll method ", logger);

		installHistory = installHistoryRepository.findAll(spc, pageable);
		List<InstallationSummaryResponseDTO> assetResponseDTOList = new ArrayList<>();

		for (InstallHistory ins : installHistory) {
			try {
				if ((filterValues.containsKey("cargo_sensor") && filterValues.get("cargo_sensor") != null)
						|| (filterValues.containsKey("abssensor") && filterValues.get("abssensor") != null)
						|| (filterValues.containsKey("door_sensor") && filterValues.get("door_sensor") != null)
						|| (filterValues.containsKey("atis_sensor") && filterValues.get("atis_sensor") != null)
						|| (filterValues.containsKey("light_sentry") && filterValues.get("light_sentry") != null)
						|| (filterValues.containsKey("tpms") && filterValues.get("tpms") != null)
						|| (filterValues.containsKey("wheel_end") && filterValues.get("wheel_end") != null)
						|| (filterValues.containsKey("air_tank") && filterValues.get("air_tank") != null)
						|| (filterValues.containsKey("regulator") && filterValues.get("regulator") != null)) {
					Logutils.log(className, methodName, context.getLogUUId(),
							" Before calling beanConverter.convertInstallHistoryToInstallResponseDTO method ", logger);
					InstallationSummaryResponseDTO vf = beanConverter.convertInstallHistoryToInstallResponseDTO(ins,
							filterValues, userId);
					if (vf.isFilter()) {
						assetResponseDTOList.add(vf);
						size = assetResponseDTOList.size();
					}
				} else {
					Logutils.log(className, methodName, context.getLogUUId(),
							" Before calling beanConverter.convertInstallHistoryToInstallResponseDTO method ", logger);
					InstallationSummaryResponseDTO vf = beanConverter.convertInstallHistoryToInstallResponseDTO(ins,
							filterValues, userId);
					assetResponseDTOList.add(vf);
					size = (int) installHistory.getTotalElements();
				}
			} catch (JsonProcessingException e) {
				logger.error("Exception while converting installHistory to InstallationSummaryResponseDTO", e);
				e.printStackTrace();
			}
		}
		Page<InstallationSummaryResponseDTO> page = new PageImpl<>(assetResponseDTOList, pageable, size);
		Logutils.log(className, methodName, context.getLogUUId(), " Exiting From InstallerService ", logger);
		return page;
	}

	class InstallInstructionComparator implements Comparator<InstallInstructionBean> {
		@Override
		public int compare(InstallInstructionBean x, InstallInstructionBean y) {
			int seqComparison = compare(x.getSequence(), y.getSequence());
			return seqComparison;
		}

		private int compare(int a, int b) {
			return a < b ? -1 : a > b ? 1 : 0;
		}
	}

	@Override
	public Workbook exportExcelForInstallationSummary(Pageable pageable, String comapnyUuid, Long userId,
			Map<String, String> filterValues, Long days) {
		Workbook workbook = null;
		Context context = new Context();
		String methodName = "exportExcelForInstallationSummary";
		Logutils.log(className, methodName, context.getLogUUId(),
				" " + " Inside exportExcelForInstallationSummary Method From InstallerService ", logger);
		String userIdLog = "UserId: " + userId;
		Logutils.log(className, methodName, context.getLogUUId(),
				" Before calling restUtils.getUserFromAuthService method ", logger, userIdLog);

		User user = restUtils.getUserFromAuthService("", userId);
		Page<InstallHistory> installHistory = null;
		int size = 0;
		Specification<InstallHistory> spc = InstallerHistorySpecification.getaInstallerSpecification(filterValues, user,
				comapnyUuid, days);
		Logutils.log(className, methodName, context.getLogUUId(),
				" Before calling installHistoryRepository.findAll method ", logger);

		installHistory = installHistoryRepository.findAll(spc, pageable);
		List<InstallationSummaryResponseDTO> assetResponseDTOList = new ArrayList<>();

		for (InstallHistory ins : installHistory) {
			try {
				if ((filterValues.containsKey("cargo_sensor") && filterValues.get("cargo_sensor") != null)
						|| (filterValues.containsKey("abssensor") && filterValues.get("abssensor") != null)
						|| (filterValues.containsKey("door_sensor") && filterValues.get("door_sensor") != null)
						|| (filterValues.containsKey("atis_sensor") && filterValues.get("atis_sensor") != null)
						|| (filterValues.containsKey("light_sentry") && filterValues.get("light_sentry") != null)
						|| (filterValues.containsKey("tpms") && filterValues.get("tpms") != null)
						|| (filterValues.containsKey("wheel_end") && filterValues.get("wheel_end") != null)
						|| (filterValues.containsKey("air_tank") && filterValues.get("air_tank") != null)
						|| (filterValues.containsKey("regulator") && filterValues.get("regulator") != null)) {
					Logutils.log(className, methodName, context.getLogUUId(),
							" Before calling beanConverter.convertInstallHistoryToInstallResponseDTO method ", logger);
					InstallationSummaryResponseDTO vf = beanConverter.convertInstallHistoryToInstallResponseDTO(ins,
							filterValues, userId);
					if (vf.isFilter()) {
						assetResponseDTOList.add(vf);
						size = assetResponseDTOList.size();
					}
				} else {
					Logutils.log(className, methodName, context.getLogUUId(),
							" Before calling beanConverter.convertInstallHistoryToInstallResponseDTO method ", logger);
					InstallationSummaryResponseDTO vf = beanConverter.convertInstallHistoryToInstallResponseDTO(ins,
							filterValues, userId);
					assetResponseDTOList.add(vf);
					size = (int) installHistory.getTotalElements();
				}
			} catch (JsonProcessingException e) {
				logger.error("Exception while converting installHistory to InstallationSummaryResponseDTO", e);
				e.printStackTrace();
			}
		}

		String[] headerColumns = { "Asset ID", "Product", "Device ID", "Status", "User Name", "User Company", "Started",
				"Finished", "Last Updated", "InstallAssist Version", "Battery", "Battery V", "Blue/ABS", "Blue/ABS V",
				"Marker/Brown", "Marker/Brown V", "Cargo Sensor", "Cargo Camera Sensor", "Cargo Camera Mac",
				"Door Sensor", "Door MAC", "Door Type", "LampCheck ATIS", "LampCheck ATIS MAC", "ABS", "ATIS",
				"LiteSentry", "Receiver", "TPMS LOF-1", "TPMS LOF-1 ID", "TPMS LOF-1 Temp", "TPMS LOF-1 Pressure",
				"TPMS LIF-2", "TPMS LIF-2 ID", "TPMS LIF-2 Temp", "TPMS LIF-2 Pressure", "TPMS RIF-3", "TPMS RIF-3 ID",
				"TPMS RIF-3 Temp", "TPMS RIF-3 Pressure", "TPMS ROF-4", "TPMS ROF-4 ID", "TPMS ROF-4 Temp",
				"TPMS ROF-4 Pressure", "TPMS LOR-5", "TPMS LOR-5 ID", "TPMS LOR-5 Temp", "TPMS LOR-5 Pressure",
				"TPMS LIR-6", "TPMS LIR-6 ID", "TPMS LIR-6 Temp", "TPMS LIR-6 Pressure", "TPMS RIR-7", "TPMS RIR-7 ID",
				"TPMS RIR-7 Temp", "TPMS RIR-7 Pressure", "TPMS ROR-8", "TPMS ROR-8 ID", "TPMS ROR-8 Temp",
				"TPMS ROR-8 Pressure", "Air Tank", "Air Tank ID", "Air Tank Temp", "Air Tank Pressure", "Regulator",
				"Regulator ID", "Regulator Temp", "Regulator Pressure", "Wheel End", "Maxon MaxLink",
				"Maxon MaxLink Mac", "Tank Saver", "Tank Saver Mac" };
		workbook = createExcelForInstallationSummary("Installation Summary Report", headerColumns,
				assetResponseDTOList);
		Logutils.log(className, methodName, context.getLogUUId(), " Exiting From InstallerService ", logger);
		return workbook;
	}

	public Workbook createExcelForInstallationSummary(String sheetName, String[] headerColumns,
			List<InstallationSummaryResponseDTO> assetResponseDTOList) {
		String methodName = "createExcelForInstallationSummary";
		Logutils.log(className, methodName, " Inside createExcelForInstallationSummary Method From InstallerService ",
				logger);
		// Create a Workbook
		Workbook workbook = new XSSFWorkbook(); // new HSSFWorkbook() for generating `.xls` file

		// Create a Sheet
		Sheet sheet = workbook.createSheet(sheetName);

		// Create a Font for styling header cells
		Font headerFontForClientDetail = workbook.createFont();
		headerFontForClientDetail.setBold(true);
		headerFontForClientDetail.setFontName("Calibri");
		headerFontForClientDetail.setFontHeightInPoints((short) 11);
		headerFontForClientDetail.setColor(IndexedColors.BLACK.getIndex());

		// Create a CellStyle with the font
		CellStyle headerCellStyleForClientDetails = workbook.createCellStyle();
		headerCellStyleForClientDetails.setFont(headerFontForClientDetail);

		// Create a Font for styling header cells
		Font headerFont = workbook.createFont();
		headerFont.setBold(true);
		headerFont.setFontName("Calibri");
		headerFont.setFontHeightInPoints((short) 10);
		headerFont.setColor(IndexedColors.BLACK.getIndex());

		// Create a CellStyle with the font
		CellStyle headerCellStyle = workbook.createCellStyle();
		headerCellStyle.setFont(headerFont);

		/* create Header for Asset Details */
		Row headerForContactPerson = sheet.createRow(0);
		// Create cells
		for (int i = 0; i < headerColumns.length; i++) {
			Cell cell = headerForContactPerson.createCell(i);
			cell.setCellValue(headerColumns[i]);
			cell.setCellStyle(headerCellStyle);
		}
		int rowNum = 1;

		if (utilities.checkInputValue(assetResponseDTOList)) {
			for (InstallationSummaryResponseDTO installationSummaryDTO : assetResponseDTOList) {
				Row row = sheet.createRow(rowNum++);
				int i = 0;
				createCellAndSetValue(row, i++, installationSummaryDTO.getAssetId());
				createCellAndSetValue(row, i++, installationSummaryDTO.getProductName() != null
						? installationSummaryDTO.getProductName() + " (" + installationSummaryDTO.getProductCode() + ")"
						: "");
				createCellAndSetValue(row, i++, installationSummaryDTO.getDeviceId());
				createCellAndSetValue(row, i++, installationSummaryDTO.getStatus());
				createCellAndSetValue(row, i++, installationSummaryDTO.getInstallerName());
				createCellAndSetValue(row, i++, installationSummaryDTO.getInstallerCompany());
				createCellAndSetValue(row, i++,
						installationSummaryDTO.getDateStarted() != null
								? installationSummaryDTO.getDateStarted().toString()
								: "");
				createCellAndSetValue(row, i++, installationSummaryDTO.getInstalled());
				createCellAndSetValue(row, i++,
						installationSummaryDTO.getUpdatedAt() != null ? installationSummaryDTO.getUpdatedAt().toString()
								: "");
				createCellAndSetValue(row, i++,
						installationSummaryDTO.getAppVersion() != null ? installationSummaryDTO.getAppVersion() : "");
				createCellAndSetValue(row, i++, installationSummaryDTO.getBatteryStatus());
				createCellAndSetValue(row, i++, installationSummaryDTO.getBatteryVoltage()); // 10

				createCellAndSetValue(row, i++, installationSummaryDTO.getPrimaryStatus());
				createCellAndSetValue(row, i++, installationSummaryDTO.getPrimaryVoltage());
				createCellAndSetValue(row, i++, installationSummaryDTO.getSecondaryStatus());
				createCellAndSetValue(row, i++, installationSummaryDTO.getSecondaryVoltage());
				createCellAndSetValue(row, i++, installationSummaryDTO.getCargoSensor());
				createCellAndSetValue(row, i++, installationSummaryDTO.getCargoCameraSensor());

				String cargoCameraMacAddress = null;
				if (installationSummaryDTO.getCargoCameraMac() != null) {
					cargoCameraMacAddress = installationSummaryDTO.getCargoCameraMac();
					cargoCameraMacAddress = cargoCameraMacAddress.replaceAll(":", "");
				}
				createCellAndSetValue(row, i++, cargoCameraMacAddress);

				createCellAndSetValue(row, i++, installationSummaryDTO.getDoorSensor());
				String doorMacAddress = null;
				if (installationSummaryDTO.getDoorMacAddress() != null) {
					doorMacAddress = installationSummaryDTO.getDoorMacAddress();
					doorMacAddress = doorMacAddress.replaceAll(":", "");
				}
				createCellAndSetValue(row, i++, doorMacAddress);
				createCellAndSetValue(row, i++, installationSummaryDTO.getDoorType());
				createCellAndSetValue(row, i++, installationSummaryDTO.getLampCheckAtis()); // 20

				String atisMacAddress = null;
				if (installationSummaryDTO.getLampCheckAtisMac() != null) {
					atisMacAddress = installationSummaryDTO.getLampCheckAtisMac();
					atisMacAddress = atisMacAddress.replaceAll(":", "");
				}
				createCellAndSetValue(row, i++, atisMacAddress);
				createCellAndSetValue(row, i++, installationSummaryDTO.getABSSensor());
				createCellAndSetValue(row, i++, installationSummaryDTO.getAtisSensor());
				createCellAndSetValue(row, i++, installationSummaryDTO.getLightSentry());
				createCellAndSetValue(row, i++, installationSummaryDTO.getReciever());

				createCellAndSetValue(row, i++, installationSummaryDTO.getTpmsLofStatus());
				createCellAndSetValue(row, i++, installationSummaryDTO.getTpmsLof());
				createCellAndSetValue(row, i++, installationSummaryDTO.getTpmsLofTemperature());
				createCellAndSetValue(row, i++, installationSummaryDTO.getTpmsLofPressure());

				createCellAndSetValue(row, i++, installationSummaryDTO.getTpmsLifStatus());
				createCellAndSetValue(row, i++, installationSummaryDTO.getTpmsLif());
				createCellAndSetValue(row, i++, installationSummaryDTO.getTpmsLifTemperature());
				createCellAndSetValue(row, i++, installationSummaryDTO.getTpmsLifPressure());

				createCellAndSetValue(row, i++, installationSummaryDTO.getTpmsRifStatus());
				createCellAndSetValue(row, i++, installationSummaryDTO.getTpmsRif());
				createCellAndSetValue(row, i++, installationSummaryDTO.getTpmsRifTemperature());
				createCellAndSetValue(row, i++, installationSummaryDTO.getTpmsRifPressure());

				createCellAndSetValue(row, i++, installationSummaryDTO.getTpmsRofStatus());
				createCellAndSetValue(row, i++, installationSummaryDTO.getTpmsRof());
				createCellAndSetValue(row, i++, installationSummaryDTO.getTpmsRofTemperature());
				createCellAndSetValue(row, i++, installationSummaryDTO.getTpmsRofPressure());

				createCellAndSetValue(row, i++, installationSummaryDTO.getTpmsLorStatus()); // 30
				createCellAndSetValue(row, i++, installationSummaryDTO.getTpmsLor());
				createCellAndSetValue(row, i++, installationSummaryDTO.getTpmsLorTemperature());
				createCellAndSetValue(row, i++, installationSummaryDTO.getTpmsLorPressure());

				createCellAndSetValue(row, i++, installationSummaryDTO.getTpmsLirStatus());
				createCellAndSetValue(row, i++, installationSummaryDTO.getTpmsLir());
				createCellAndSetValue(row, i++, installationSummaryDTO.getTpmsLirTemperature());
				createCellAndSetValue(row, i++, installationSummaryDTO.getTpmsLirPressure());

				createCellAndSetValue(row, i++, installationSummaryDTO.getTpmsRirStatus());
				createCellAndSetValue(row, i++, installationSummaryDTO.getTpmsRir());
				createCellAndSetValue(row, i++, installationSummaryDTO.getTpmsRirTemperature());
				createCellAndSetValue(row, i++, installationSummaryDTO.getTpmsRirPressure());

				createCellAndSetValue(row, i++, installationSummaryDTO.getTpmsRorStatus());
				createCellAndSetValue(row, i++, installationSummaryDTO.getTpmsRor());
				createCellAndSetValue(row, i++, installationSummaryDTO.getTpmsRorTemperature());
				createCellAndSetValue(row, i++, installationSummaryDTO.getTpmsRorPressure());

				createCellAndSetValue(row, i++, installationSummaryDTO.getAirTank());
				createCellAndSetValue(row, i++, installationSummaryDTO.getAirTankId());
				createCellAndSetValue(row, i++, installationSummaryDTO.getAirTankTemperature());
				createCellAndSetValue(row, i++, installationSummaryDTO.getAirTankPressure());

				createCellAndSetValue(row, i++, installationSummaryDTO.getRegulator());
				createCellAndSetValue(row, i++, installationSummaryDTO.getRegulatorId());
				createCellAndSetValue(row, i++, installationSummaryDTO.getRegulatorTemperature());
				createCellAndSetValue(row, i++, installationSummaryDTO.getRegulatorPressure());

				createCellAndSetValue(row, i++, installationSummaryDTO.getWheelEnd()); // 38

				createCellAndSetValue(row, i++, installationSummaryDTO.getMaxonMaxLink());
				String maxonMaxLinkMacAddress = null;
				if (installationSummaryDTO.getMaxonMaxLinkMac() != null) {
					maxonMaxLinkMacAddress = installationSummaryDTO.getMaxonMaxLinkMac();
					maxonMaxLinkMacAddress = maxonMaxLinkMacAddress.replaceAll(":", "");
				}
				createCellAndSetValue(row, i++, maxonMaxLinkMacAddress);

				createCellAndSetValue(row, i++, installationSummaryDTO.getTankSaver());
				String tankSaverMacAddress = null;
				if (installationSummaryDTO.getTankSaverMac() != null) {
					tankSaverMacAddress = installationSummaryDTO.getTankSaverMac();
					tankSaverMacAddress = tankSaverMacAddress.replaceAll(":", "");
				}
				createCellAndSetValue(row, i++, tankSaverMacAddress); // 53

			}
		}

		getAutoSizeColumeForAssigneeReport(sheet, 73);

		Logutils.log(className, methodName, " Exiting From InstallerService ", logger);
		return workbook;

	}

	public void createCellAndSetValue(Row row, int i, String value) {
		row.createCell(i).setCellValue(value != null ? value : "N/A");
	}

	private void getAutoSizeColumeForAssigneeReport(Sheet sheet, int count) {
		for (int i = 0; i < count; i++) {
			sheet.autoSizeColumn(i);
		}
	}

	/**
	 * @param sort
	 * @return
	 */
	Pageable getPageable(int page, int size, String sort, String _order) {

		if (null == sort)
			return PageRequest.of(page, size);

		Pattern pattern = Pattern.compile(Constants.SORT_PATTERN);
		Matcher matcher = pattern.matcher(sort + Constants.COMMA);
		List<Sort.Order> orderList = new ArrayList<Sort.Order>();
		while (matcher.find()) {
			orderList.add(new Sort.Order(getEnum(matcher.group(3)), matcher.group(1)));
			logger.debug("shorting order: " + matcher.group(1) + ": " + matcher.group(3));
		}
		if ("ASC".equalsIgnoreCase(_order)) {

			return PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, sort));
		} else {
			return PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, sort));
		}
	}

	/**
	 * @param direction
	 * @return
	 */
	Direction getEnum(String direction) {

		if (direction.equalsIgnoreCase("desc"))
			return Direction.DESC;
		else
			return Direction.ASC;
	}

	@Override
	public Page<GatewayDetailsBean> getGatewayDetailsWithPaginationNew(String installCode, String can, Long userId,
			Context context, Integer page, Integer pageSize, String sort, String order, String timeOfLastDownload) {
		long totalElements = 0;
		Pageable pageable = getPageable(page, pageSize, sort, order);
		String methodName = "getGatewayDetailsWithPaginationNew";
		Logutils.log(className, methodName, context.getLogUUId(),
				" Inside getGatewayDetailsWithPaginationNew Method From InstallerService ", logger);
		List<GatewayDetailsBean> gatewatDetailList = new ArrayList<>();
		List<GatewayDetailsBean> gatewayList = new ArrayList<>();
		String userIdLog = "UserId: " + userId;
		Logutils.log(className, methodName, context.getLogUUId(),
				" Before calling restUtils.getUserFromAuthService method ", logger, userIdLog);
		User user = restUtils.getUserFromAuthService("", userId);
		if (installCode == null && can == null) {
			/*
			 * Logutils.log(className, methodName, context.getLogUUId(),
			 * "value of install code and can is null and role of user is ", logger,
			 * user.getOrganisation().getType().getValue());
			 */
			if (user.getOrganisation().getOrganisationRole().contains(OrganisationRole.INSTALLER)) {
				Organisation companie = restUtils
						.getOrganisationByUuidFromCompanyService(user.getOrganisation().getUuid());
				List<Organisation> companies = companie.getAccessList();
				if (companies.size() > 0) {
					List<String> canList = new ArrayList<String>();

					for (Organisation com : companies) {
						canList.add(com.getAccountNumber());

					}
					Logutils.log(className, methodName, context.getLogUUId(),
							" Before calling restUtils.getGatewaysByCANAndStatusFromDeviceServiceWithPaginationV2 method ",
							logger);

					ResponseEntity<Page<GatewayDetailsBean>> result = restUtils
							.getGatewaysByCANAndStatusFromDeviceServiceWithPaginationV2(null, page, pageSize, sort,
									order, canList, timeOfLastDownload);
					List<GatewayDetailsBean> list = result.getBody().getContent();
					totalElements = result.getBody().getTotalElements();
					pageable = result.getBody().getPageable();
					gatewayList.addAll(list);
				}

			}
			if (user.getOrganisation().getOrganisationRole().contains(OrganisationRole.END_CUSTOMER)) {
				Organisation companies = user.getOrganisation();
				if (companies != null) {
					Logutils.log(className, methodName, context.getLogUUId(),
							" Before calling restUtils.getGatewaysByCANAndStatusFromDeviceServiceWithPaginationV2 method ",
							logger);
					ResponseEntity<Page<GatewayDetailsBean>> result = restUtils
							.getGatewaysByCANAndStatusFromDeviceServiceWithPaginationV2(companies.getAccountNumber(),
									page, pageSize, sort, order, null, timeOfLastDownload);

					List<GatewayDetailsBean> list = result.getBody().getContent();
					totalElements = result.getBody().getTotalElements();
					pageable = result.getBody().getPageable();

					gatewayList.addAll(list);
				}
			}
			if (user.getOrganisation().getOrganisationRole().contains(OrganisationRole.MANUFACTURER)) {

				Logutils.log(className, methodName, context.getLogUUId(),
						" Before calling restUtils.getCompanyByCustomer method ", logger);
				List<Organisation> list = restUtils.getCompanyByCustomer();
				if (list.size() > 0) {
					List<String> canList = new ArrayList<String>();

					for (Organisation com : list) {
						canList.add(com.getAccountNumber());
					}
					Logutils.log(className, methodName, context.getLogUUId(),
							" Before calling restUtils.getGatewaysByCANAndStatusFromDeviceServiceWithPaginationV2 method ",
							logger);
					ResponseEntity<Page<GatewayDetailsBean>> result = restUtils
							.getGatewaysByCANAndStatusFromDeviceServiceWithPaginationV2(null, page, pageSize, sort,
									order, canList, timeOfLastDownload);
					List<GatewayDetailsBean> list1 = result.getBody().getContent();
					totalElements = result.getBody().getTotalElements();
					pageable = result.getBody().getPageable();

					gatewayList.addAll(list1);
				}

			}
		}
		String installCodeLog = "InstallCode: " + installCode;

		if (utilities.checkInputValue(installCode) && can == null) {
			Logutils.log(className, methodName, context.getLogUUId(),
					" Before calling installHistoryRepository.findByInstallCode method ", logger, installCodeLog);

			InstallHistory installHistory1 = installHistoryRepository.findByInstallCode(installCode);
			gatewayList.add(deviceToGatewayDetailsBean(installHistory1.getDevice()));
		}
		if (installCode == null && utilities.checkInputValue(can)) {
			String canLog = "Can: " + can;
			Logutils.log(className, methodName, context.getLogUUId(),
					" Before calling restUtils.getGatewaysByCANAndStatusFromDeviceService method ", logger, canLog);

			ResponseEntity<Page<GatewayDetailsBean>> result = restUtils
					.getGatewaysByCANAndStatusFromDeviceServiceWithPaginationV2(can, page, pageSize, sort, order, null,
							timeOfLastDownload);

			gatewayList = result.getBody().getContent();
			totalElements = result.getBody().getTotalElements();
			pageable = result.getBody().getPageable();

		}

		if (utilities.checkInputValue(installCode) && utilities.checkInputValue(can)) {
			Logutils.log(className, methodName, context.getLogUUId(),
					" Before calling installHistoryRepository.findByInstallCode method ", logger, installCodeLog);

			InstallHistory installHistory = installHistoryRepository.findByInstallCode(installCode);
			if (installHistory.getOrganisation().getAccountNumber().equalsIgnoreCase(can)) {

				gatewayList.add(deviceToGatewayDetailsBean(installHistory.getDevice()));
			} else {
				throw new InstallerException("provided Install Code and Account Number is not matching");
			}
		}

		Page<GatewayDetailsBean> pageOfGatewayDetailsBean = new PageImpl<>(gatewayList, pageable, totalElements);
		Logutils.log(className, methodName, context.getLogUUId(), " Exiting From InstallerService ", logger);
		return pageOfGatewayDetailsBean;

	}

	@Override
	public Page<GatewayDetailsBean> getGatewayDetailsWithPaginationNewTesting(String installCode, String can,
			Long userId, Context context, Integer page, Integer pageSize, String sort, String order,
			String timeOfLastDownload) {
		long totalElements = 0;
		Pageable pageable = getPageable(page, pageSize, sort, order);
		String methodName = "getGatewayDetailsWithPaginationNewTesting";
		Logutils.log(className, methodName, context.getLogUUId(),
				" Inside getGatewayDetailsWithPaginationNewTesting Method From InstallerService ", logger);
		List<GatewayDetailsBean> gatewatDetailList = new ArrayList<>();
		List<GatewayDetailsBean> gatewayList = new ArrayList<>();
		String userIdLog = "UserId: " + userId;
		Logutils.log(className, methodName, context.getLogUUId(),
				" Before calling restUtils.getUserFromAuthService method ", logger, userIdLog);
		User user = restUtils.getUserFromAuthService("", userId);
		if (installCode == null && can == null) {
			/*
			 * Logutils.log(className, methodName, context.getLogUUId(),
			 * "value of install code and can is null and role of user is ", logger,
			 * user.getOrganisation().getType().getValue());
			 */
			if (user.getOrganisation().getOrganisationRole().contains(OrganisationRole.INSTALLER)) {
				Organisation companie = restUtils
						.getOrganisationByUuidFromCompanyService(user.getOrganisation().getUuid());
				List<Organisation> companies = companie.getAccessList();
				if (companies.size() > 0) {
					List<String> canList = new ArrayList<String>();

					for (Organisation com : companies) {
						canList.add(com.getAccountNumber());

					}
					String canNumber = null;
					String status = null;

					Page<GatewayDetailsBean> result = getGatewaysByCANAndStatusFromDeviceServiceWithPaginationV2Testing(
							canNumber, status, getPageable(page - 1, pageSize, sort, order), canList,
							timeOfLastDownload);
					List<GatewayDetailsBean> list = result.getContent();
					totalElements = result.getTotalElements();
					pageable = result.getPageable();
					gatewayList.addAll(list);
				}

			}
			if (user.getOrganisation().getOrganisationRole().contains(OrganisationRole.END_CUSTOMER)) {
				Organisation companies = user.getOrganisation();
				if (companies != null) {
					String status = null;

					Page<GatewayDetailsBean> result = getGatewaysByCANAndStatusFromDeviceServiceWithPaginationV2Testing(
							companies.getAccountNumber(), status, getPageable(page - 1, pageSize, sort, order), null,
							timeOfLastDownload);

					List<GatewayDetailsBean> list = result.getContent();
					totalElements = result.getTotalElements();
					pageable = result.getPageable();

					gatewayList.addAll(list);
				}
			}
			if (user.getOrganisation().getOrganisationRole().contains(OrganisationRole.MANUFACTURER)) {
				Logutils.log(className, methodName, context.getLogUUId(),
						" Before calling restUtils.getCompanyByCustomer method ", logger);

				List<Organisation> list = restUtils.getCompanyByCustomer();
				if (list.size() > 0) {
					List<String> canList = new ArrayList<String>();

					for (Organisation com : list) {
						canList.add(com.getAccountNumber());
					}

					String canNumber = null;
					String status = null;

					Page<GatewayDetailsBean> result = getGatewaysByCANAndStatusFromDeviceServiceWithPaginationV2Testing(
							canNumber, status, getPageable(page - 1, pageSize, sort, order), canList,
							timeOfLastDownload);
					List<GatewayDetailsBean> list1 = result.getContent();
					totalElements = result.getTotalElements();
					pageable = result.getPageable();

					gatewayList.addAll(list1);
				}

			}
		}
		String installCodeLog = "InstallCode: " + installCode;

		if (utilities.checkInputValue(installCode) && can == null) {
			Logutils.log(className, methodName, context.getLogUUId(),
					" Before calling installHistoryRepository.findByInstallCode method ", logger, installCodeLog);

			InstallHistory installHistory1 = installHistoryRepository.findByInstallCode(installCode);
			gatewayList.add(deviceToGatewayDetailsBean(installHistory1.getDevice()));
		}
		if (installCode == null && utilities.checkInputValue(can)) {
			String canLog = "Can: " + can;

			String status = null;

			Page<GatewayDetailsBean> result = getGatewaysByCANAndStatusFromDeviceServiceWithPaginationV2Testing(can,
					status, getPageable(page - 1, pageSize, sort, order), null, timeOfLastDownload);

			gatewayList = result.getContent();
			totalElements = result.getTotalElements();
			pageable = result.getPageable();

		}

		if (utilities.checkInputValue(installCode) && utilities.checkInputValue(can)) {
			Logutils.log(className, methodName, context.getLogUUId(),
					" Before calling installHistoryRepository.findByInstallCode method ", logger, installCodeLog);

			InstallHistory installHistory = installHistoryRepository.findByInstallCode(installCode);
			if (installHistory.getOrganisation().getAccountNumber().equalsIgnoreCase(can)) {

				gatewayList.add(deviceToGatewayDetailsBean(installHistory.getDevice()));
			} else {
				throw new InstallerException("provided Install Code and Account Number is not matching");
			}
		}

		Page<GatewayDetailsBean> pageOfGatewayDetailsBean = new PageImpl<>(gatewayList, pageable, totalElements);

		Logutils.log(className, methodName, context.getLogUUId(), " Exiting From InstallerService ", logger);
		return pageOfGatewayDetailsBean;

	}

	private Page<GatewayDetailsBean> getGatewaysByCANAndStatusFromDeviceServiceWithPaginationV2Testing(
			String accountNumber, String status, Pageable pageable, List<String> canList,
			String lastDownloadeTimeString) {

		String methodName = "getGatewaysByCANAndStatusFromDeviceServiceWithPaginationV2Testing";
		Page<DeviceDto> gatewayList = null;
		List<GatewayDetailsBean> gatewatDetailList = new ArrayList<>();
		Instant lastDownloadeTime = null;
		if (lastDownloadeTimeString != null && !lastDownloadeTimeString.isEmpty() && lastDownloadeTimeString != ""
				&& !lastDownloadeTimeString.equals("0")) {
			lastDownloadeTime = Instant.ofEpochMilli(Long.parseLong(lastDownloadeTimeString));
			logger.info("Last Downloaded Time: " + lastDownloadeTime + " Account Number: " + accountNumber);
		}

		Page<GatewayDetailsBean> pageOfGatewayDetailsBean = null;
		if (lastDownloadeTime != null && accountNumber != null && !accountNumber.isEmpty()) {
			Logutils.log(className, methodName,
					" Before calling deviceRepository.findByAccountNumberWithPagination method ", logger);
			gatewayList = deviceRepository.findByAccountNumberWithPagination(accountNumber, lastDownloadeTime,
					pageable);
		} else if (accountNumber != null && !accountNumber.isEmpty()) {
			gatewayList = deviceRepository.findByAccountNumberWithPagination(accountNumber, pageable);
		} else if (lastDownloadeTime != null) {
			gatewayList = deviceRepository.findByListOfAccountNumberWithPagination(canList, lastDownloadeTime,
					pageable);
		} else {
			gatewayList = deviceRepository.findByListOfAccountNumberWithPagination(canList, pageable);
		}

		if (gatewayList.getContent() != null && gatewayList.getContent().size() > 0) {

			List<Attribute> attributesList = getAttributes();
			List<ProductMasterDto> productMastersList = getProductMasters();
			List<SensorInstallInstruction> sensorInstallInstructionsList = getAllSensorInstallInstruction();

			for (DeviceDto gat : gatewayList) {
				String productNameLog = "ProductName: " + gat.getProductName();
//					Logutils.log(className, methodName, context.getLogUUId(),
//							" Inside iterating Gateway LIst to gate product Name ", logger, productNameLog);
				String uuidLog = "Uuid: " + gat.getUuid();
				List<InstallInstructionBean> sensorInstallInstructions = new ArrayList<>();
				Map<String, List<ReasonCodeBean>> reasonCodeBeanMap = new HashMap<>();
				List<AttributeValueResposneDTO> attributeList = new ArrayList<>();
//					Logutils.log(className, methodName, context.getLogUUId(),
//							" Before calling restUtils.getAttributeListByProductName method ", logger, productNameLog,
//							uuidLog);

//					List<Attribute> attributes = getProductByCode(gat.getProductCode());
				List<Attribute> attributes = null;
				if (gat.getProductCode() != null && !gat.getProductCode().isEmpty() && gat.getProductCode() != "") {
					List<ProductMasterDto> filterListProductMaster = productMastersList.stream()
							.filter(p -> p.getProductCode().equals(gat.getProductCode())).collect(Collectors.toList());

					if (filterListProductMaster != null && !filterListProductMaster.isEmpty()
							&& filterListProductMaster.size() > 0) {
						attributes = attributesList.stream().filter(
								p -> p.getProductMaster().getUuid().equals(filterListProductMaster.get(0).getUuid()))
								.collect(Collectors.toList());

					}

				}
//					Logutils.log(className, methodName, context.getLogUUId(),
//							" after calling restUtils.getAttributeListByProductName method ", logger, productNameLog,
//							uuidLog);
				GatewayDetailsBean gatewatDetails = new GatewayDetailsBean();
				gatewatDetails.setGatewayUuid(gat.getUuid());
				gatewatDetails.setGatewayProductName(gat.getProductName());
				gatewatDetails.setGatewayProductCode(gat.getProductCode());
				// gatewatDetails.setEligibleAssetType(ins.getAsset().getGatewayEligibility());
				if (attributes != null && attributes.size() > 0) {
					gatewatDetails.set_blocker(attributes.get(0).getProductMaster().isBlocker());
					for (Attribute att : attributes) {
						AttributeValueResposneDTO attRes = new AttributeValueResposneDTO();
						attRes.setApplicable(att.isApplicable());
						attRes.setAttribute_uuid(att.getUuid());
						attRes.setAttributeName(att.getAttributeName());
						attRes.setThresholdValue(att.getAttributeValue());
						attributeList.add(attRes);
					}
				}

				gatewatDetails.setGatewayAttribute(attributeList);
//					Logutils.log(className, methodName, context.getLogUUId(),
//							" Before calling sensorInstallInstructionRepository.findBySensorProductName method ", logger,
//							productNameLog);
//					List<SensorInstallInstruction> sensorInstallInstructionList =
//							 getSensorInstallInstructionByProductCode(gat.getProductCode());
				List<SensorInstallInstruction> sensorInstallInstructionList = null;

				if (gat.getProductCode() != null && !gat.getProductCode().isEmpty() && gat.getProductCode() != "") {
					sensorInstallInstructionList = sensorInstallInstructionsList.stream()
							.filter(p -> p.getSensorProductCode().equals(gat.getProductCode()))
							.collect(Collectors.toList());
					sensorInstallInstructionList.forEach(sensorInstallInstruction -> {
						InstallInstructionBean installInstructionBean = new InstallInstructionBean();
						installInstructionBean
								.setInstruction(sensorInstallInstruction.getInstallInstruction().getInstruction());
						installInstructionBean
								.setSequence(sensorInstallInstruction.getInstallInstruction().getStepSequence());
						sensorInstallInstructions.add(installInstructionBean);
					});
					InstallInstructionComparator comparator = new InstallInstructionComparator();
					Collections.sort(sensorInstallInstructions, comparator);
					gatewatDetails.setGatewayInstallInstructions(sensorInstallInstructions);
				}

//					Logutils.log(className, methodName, context.getLogUUId(),
//							" Before calling sensorReasonCodeRepository.findBySensorProductName method ", logger,
//							productNameLog);

				if (gat.getProductCode() != null && !gat.getProductCode().isEmpty() && gat.getProductCode() != "") {
					List<SensorReasonCode> sensorReasonCodeList = getSensorReasonCodeByProductCode(
							gat.getProductCode());
					sensorReasonCodeList.forEach(sensorReasonCode -> {
						ReasonCodeBean reasonCodeBean = new ReasonCodeBean();
						reasonCodeBean.setReasonCode(sensorReasonCode.getReasonCode().getCode());
						reasonCodeBean.setDisplayName(sensorReasonCode.getReasonCode().getValue());
						if (reasonCodeBeanMap.get(sensorReasonCode.getReasonCode().getIssueType()) != null) {
							reasonCodeBeanMap.get(sensorReasonCode.getReasonCode().getIssueType()).add(reasonCodeBean);
						} else {
							List<ReasonCodeBean> reasonCodeBeanList = new ArrayList<>();
							reasonCodeBeanList.add(reasonCodeBean);
							reasonCodeBeanMap.put(sensorReasonCode.getReasonCode().getIssueType(), reasonCodeBeanList);
						}
					});
					gatewatDetails.setGatewayReasonCodes(reasonCodeBeanMap);
				}

				gatewatDetailList.add(gatewatDetails);
			}
			pageOfGatewayDetailsBean = new PageImpl<>(gatewatDetailList, pageable, gatewayList.getTotalElements());

		} else {
			throw new InstallerException("No Gateway for Given Input");
		}

		Logutils.log(className, methodName, " Exiting From InstallerService ", logger);
		return pageOfGatewayDetailsBean;

	}

//	@Override
//	public Page<GatewayDetailsBean> getGatewaysByAccountNumberAndStatusWithPaginationV2Testing(String accountNumber, String status,
//			Pageable pageable, List<String> cans, Instant lastDownloadeTime) {
//		logger.info(" accountNumber  " + accountNumber + " status: " + status + "cans: "+cans);
//		Page<Device> gatewayList = null;
//		List<GatewayDetailsBean> gatewatDetailList = new ArrayList<>();
//		Page<GatewayDetailsBean> pageOfGatewayDetailsBean= null;
//			if (lastDownloadeTime != null && accountNumber != null && !accountNumber.isEmpty()) {
//				gatewayList = deviceRepository.findByAccountNumberWithPagination(accountNumber, lastDownloadeTime, pageable);
//			} else if (accountNumber != null && !accountNumber.isEmpty()) {
//				gatewayList = deviceRepository.findByAccountNumberWithPagination(accountNumber, pageable);
//			} else if(lastDownloadeTime != null) {
//				gatewayList = deviceRepository.findByListOfAccountNumberWithPagination(cans, lastDownloadeTime, pageable);
//			} else {
//				gatewayList = deviceRepository.findByListOfAccountNumberWithPagination(cans, pageable);
//			}
//			
//			if (gatewayList.getContent() != null && gatewayList.getContent().size() > 0) {
//				for (Device gat : gatewayList) {
//					String productNameLog = "ProductName: " + gat.getProductName();
////					Logutils.log(className, methodName, context.getLogUUId(),
////							" Inside iterating Gateway LIst to gate product Name ", logger, productNameLog);
//					String uuidLog = "Uuid: " + gat.getUuid();
//					List<InstallInstructionBean> sensorInstallInstructions = new ArrayList<>();
//					Map<String, List<ReasonCodeBean>> reasonCodeBeanMap = new HashMap<>();
//					List<AttributeValueResposneDTO> attributeList = new ArrayList<>();
////					Logutils.log(className, methodName, context.getLogUUId(),
////							" Before calling restUtils.getAttributeListByProductName method ", logger, productNameLog,
////							uuidLog);
//
//					List<Attribute> attributes = getProductByName(gat.getProductName());
////					Logutils.log(className, methodName, context.getLogUUId(),
////							" after calling restUtils.getAttributeListByProductName method ", logger, productNameLog,
////							uuidLog);
//					GatewayDetailsBean gatewatDetails = new GatewayDetailsBean();
//					gatewatDetails.setGatewayUuid(gat.getUuid());
//					gatewatDetails.setGatewayProductName(gat.getProductName());
//					gatewatDetails.setGatewayProductCode(gat.getProductCode());
//					// gatewatDetails.setEligibleAssetType(ins.getAsset().getGatewayEligibility());
//					if (attributes.size() > 0) {
//						gatewatDetails.set_blocker(attributes.get(0).getProductMaster().isBlocker());
//						for (Attribute att : attributes) {
//							AttributeValueResposneDTO attRes = new AttributeValueResposneDTO();
//							attRes.setApplicable(att.isApplicable());
//							attRes.setAttribute_uuid(att.getUuid());
//							attRes.setAttributeName(att.getAttributeName());
//							attRes.setThresholdValue(att.getAttributeValue());
//							attributeList.add(attRes);
//						}
//					}
//					gatewatDetails.setGatewayAttribute(attributeList);
////					Logutils.log(className, methodName, context.getLogUUId(),
////							" Before calling sensorInstallInstructionRepository.findBySensorProductName method ", logger,
////							productNameLog);
//
//					List<SensorInstallInstruction> sensorInstallInstructionList =
//							getSensorInstallInstruction(gat.getProductName());
//					sensorInstallInstructionList.forEach(sensorInstallInstruction -> {
//						InstallInstructionBean installInstructionBean = new InstallInstructionBean();
//						installInstructionBean
//								.setInstruction(sensorInstallInstruction.getInstallInstruction().getInstruction());
//						installInstructionBean
//								.setSequence(sensorInstallInstruction.getInstallInstruction().getStepSequence());
//						sensorInstallInstructions.add(installInstructionBean);
//					});
//					InstallInstructionComparator comparator = new InstallInstructionComparator();
//					Collections.sort(sensorInstallInstructions, comparator);
//					gatewatDetails.setGatewayInstallInstructions(sensorInstallInstructions);
////					Logutils.log(className, methodName, context.getLogUUId(),
////							" Before calling sensorReasonCodeRepository.findBySensorProductName method ", logger,
////							productNameLog);
//
//					List<SensorReasonCode> sensorReasonCodeList = getSensorReasonCode(gat.getProductName());
//					sensorReasonCodeList.forEach(sensorReasonCode -> {
//						ReasonCodeBean reasonCodeBean = new ReasonCodeBean();
//						reasonCodeBean.setReasonCode(sensorReasonCode.getReasonCode().getCode());
//						reasonCodeBean.setDisplayName(sensorReasonCode.getReasonCode().getValue());
//						if (reasonCodeBeanMap.get(sensorReasonCode.getReasonCode().getIssueType()) != null) {
//							reasonCodeBeanMap.get(sensorReasonCode.getReasonCode().getIssueType()).add(reasonCodeBean);
//						} else {
//							List<ReasonCodeBean> reasonCodeBeanList = new ArrayList<>();
//							reasonCodeBeanList.add(reasonCodeBean);
//							reasonCodeBeanMap.put(sensorReasonCode.getReasonCode().getIssueType(), reasonCodeBeanList);
//						}
//					});
//					gatewatDetails.setGatewayReasonCodes(reasonCodeBeanMap);
//					gatewatDetailList.add(gatewatDetails);
//				}
//			 pageOfGatewayDetailsBean = new PageImpl<>(gatewatDetailList, pageable,gatewayList.getTotalElements());
//
//				
//			} else {
//				throw new InstallerException("No Gateway for Given Input");
//			}				
//			
//		return pageOfGatewayDetailsBean;
//	}

	public List<SensorInstallInstructionDto> getSensorInstallInstruction(String productName) {

		List<SensorInstallInstructionDto> findBySensorProductNameByDto = sensorInstallInstructionRepository
				.findBySensorProductNameByDto(productName);
		return findBySensorProductNameByDto;

	}

	public List<SensorInstallInstruction> getSensorInstallInstructionByProductCode(String productCode) {

		List<SensorInstallInstruction> findBySensorProductName = sensorInstallInstructionRepository
				.findBySensorProductCode(productCode);
		return findBySensorProductName;

	}

	public List<SensorInstallInstruction> getAllSensorInstallInstruction() {

		List<SensorInstallInstruction> sensorInstallInstructionList = sensorInstallInstructionRepository.findAll();
		return sensorInstallInstructionList;

	}

	@Override
	public Boolean createGatewaySensorAssociation(CreateGatewaySensorAssociation createGatewaySensorAssociation,
			Long userId, String logUuid) {
		String methodName = "createGatewaySensorAssociation";
		Logutils.log(className, methodName, logUuid,
				" Inside createGatewaySensorAssociation Method From InstallerService "
						+ " CreateGatewaySensorAssociation " + createGatewaySensorAssociation,
				logger);
		Boolean flag = false;
		if (utilities.checkInputValue(createGatewaySensorAssociation)
				&& utilities.checkInputValue(createGatewaySensorAssociation.getProductCode())

				&& utilities.checkInputValue(createGatewaySensorAssociation.getProductName())

				&& utilities.checkInputValue(createGatewaySensorAssociation.getInstallUuid())

				&& utilities.checkInputValue(createGatewaySensorAssociation.getGatewayUuid())

				&& utilities.checkInputValue(createGatewaySensorAssociation.getMacAddress()) && userId != null) {
			String deviceIdLog = "DeviceId: " + createGatewaySensorAssociation.getGatewayUuid();
			String installUuidLog = " InstallUuid: " + createGatewaySensorAssociation.getInstallUuid();
			String macAddressLog = " Mac Address: " + createGatewaySensorAssociation.getMacAddress();
			String sensorProductCode = " Product Code: " + createGatewaySensorAssociation.getProductCode();
			String sensorProductName = " Product Name: " + createGatewaySensorAssociation.getProductName();
			String sensorStatus = " Sensor status: " + createGatewaySensorAssociation.getSensorStatus();
			String userIdLog = "UserId: " + userId;
			Logutils.log(logUuid, " Before calling restUtils.getUserFromAuthService method", logger, userIdLog);

			User user = restUtils.getUserFromAuthService("", userId);
			if (user == null) {
				userIdLog = "";
			}
			Logutils.log(logUuid, " After calling getUserFromAuthService method", logger, userIdLog);

			Logutils.log(logUuid, " Device Id  ", logger, deviceIdLog);
			Logutils.log(logUuid, " Install Uuid Log  ", logger, installUuidLog);
			Logutils.log(logUuid, " Mac Address Log ", logger, macAddressLog);
			Logutils.log(logUuid, " Sensor Product Code ", logger, sensorProductCode);
			Logutils.log(logUuid, " sensor Product Name ", logger, sensorProductName);
			Logutils.log(logUuid, " sensor Status ", logger, sensorStatus);
			Logutils.log(logUuid, " user Id Log ", logger, userIdLog);

			Logutils.log(className, methodName, logUuid,
					" Before calling installHistoryRepository.findByInstallCode method ", logger, installUuidLog);
			InstallHistory installHistory = installHistoryRepository
					.findByInstallCode(createGatewaySensorAssociation.getInstallUuid());
			String installHistoryIdLog = "";
			if (installHistory != null) {
				installHistoryIdLog = "InstallHistoryId: " + installHistory.getId();
			}
			Logutils.log(logUuid, " After calling findByInstallCode", logger, installHistoryIdLog);
			if (installHistory != null && (installHistory.getStatus().equals(InstallHistoryStatus.FINISHED)
					|| installHistory.getStatus().equals(InstallHistoryStatus.PROBLEM))) {
				Device gateway = installHistory.getDevice() != null ? installHistory.getDevice() : null;
				if (gateway != null
						&& gateway.getUuid().equalsIgnoreCase(createGatewaySensorAssociation.getGatewayUuid())) {
					Logutils.log(logUuid, " Gateway is not null and same the request gateway uuid", logger);
					Device cameraSensor = null;

					List<Device_Device_xref> deviceSensorxreffList = deviceDeviceXrefRepository
							.findByDeviceid(installHistory.getDevice().getId());
//					deviceSensorxreffList.forEach(sensor -> {
//						Device d = sensor.getSensorUuid();
//						d.setStatus(DeviceStatus.PENDING);
//						deviceRepository.save(d);
//					});

//					for (Sensor sensor : gateway.getSensors()) {
					for (Device_Device_xref sensor : deviceSensorxreffList) {
						if (sensor.getSensorUuid().getProductCode()
								.equalsIgnoreCase(createGatewaySensorAssociation.getProductCode())) {
							cameraSensor = sensor.getSensorUuid();
							Logutils.log(logUuid, " Sensor is already associate with gateway so now we can update it",
									logger);
							break;
						}
					}

					try {
						cameraSensor = createSensorInfo(cameraSensor, user, createGatewaySensorAssociation, gateway,
								installHistory, logUuid);
						if (cameraSensor != null && cameraSensor.getId() != null) {
							flag = true;
						}
						Logutils.log(logUuid, " Sensor is associatated successfull", logger);
					} catch (Exception e) {
						e.printStackTrace();
						logger.error("Exception occurred while  creating Gateway Sensor Association", e.getMessage());
						throw new InstallerException("Gateway is different compare to request gateway uuid");
					}
				} else {
					logger.error("Exception occurred while  creating Gateway Sensor Association",
							"Gateway is different compare to request gateway uuid");
					throw new InstallerException("No InstallHistory found for Install UUID");
				}
			} else {
				logger.error("Exception occurred while  creating Gateway Sensor Association",
						"No InstallHistory found for Install UUID");
				throw new InstallerException("No InstallHistory found for Install UUID");
			}

		} else {
			logger.error("Exception occurred while creating Gateway Sensor Association",
					"Apart from sensor status all parameter should not be null");
			throw new InstallerException("Apart from sensor status all parameter should not be null");
		}
		Logutils.log(className, logUuid, methodName, " Exiting From InstallerService ", logger);
		return flag;
	}

	private Device createSensorInfo(Device sensor, User user,
			CreateGatewaySensorAssociation createGatewaySensorAssociation, Device gateway,
			InstallHistory installHistory, String logUuid) throws Exception {
		String methodName = "createSensorInfo";
		Logutils.log(logUuid, methodName,
				" Inside the  method " + "CreateGatewaySensorAssociation " + createGatewaySensorAssociation, logger);
		Boolean isAdd = false;
		if (sensor == null) {
			sensor = new Device();
			isAdd = true;
			sensor.setCreatedBy(user);
			sensor.setCreatedOn(Instant.now());
			boolean isSensorUuidUnique = false;
			String sensorUuid = "";
			Device sensorByUuid = null;
			while (!isSensorUuidUnique) {
				sensorUuid = UUID.randomUUID().toString();
				try {
					Logutils.log(logUuid, " Before calling restUtils.getSensorBySensorUuid method ", logger);
					sensorByUuid = (restUtils.getSensorBySensorUuid(logUuid, sensorUuid)).get(0);
					Logutils.log(logUuid, " After calling restUtils.getSensorBySensorUuid method ", logger);
				} catch (Exception exception) {
					System.out.println("======================sensorByUuid is null===============================");
				}

				if (sensorByUuid == null) {
					isSensorUuidUnique = true;
				}
			}
			sensor.setUuid(sensorUuid);
			sensor.setIotType(IOTType.SENSOR);
			// sensor.setGateway(gateway);
			sensor.setProductCode(createGatewaySensorAssociation.getProductCode());
			sensor.setProductName(createGatewaySensorAssociation.getProductName());
		}

		sensor.setMacAddress(createGatewaySensorAssociation.getMacAddress());
		if (createGatewaySensorAssociation.getSensorStatus() != null
				&& createGatewaySensorAssociation.getSensorStatus() != "") {
			sensor.setStatus(DeviceStatus.getGatewayStatusInSearch(createGatewaySensorAssociation.getSensorStatus()));
		} else {
			sensor.setStatus(DeviceStatus.INSTALLED);
		}
		sensor.setUpdatedOn(Instant.now());
		sensor.setUpdatedBy(user);
		Logutils.log(logUuid, " Before calling restUtils.updateSensor method ", logger);
		sensor = restUtils.updateSensor(sensor);
		Logutils.log(logUuid, " After calling restUtils.updateSensor method ", logger);

		if (sensor != null) {
			if (isAdd) {
				createGatewaySensorXref(sensor, gateway, logUuid);
			}
			createInstallLog(sensor, installHistory, logUuid);
			try {
				createSensorHistoryForInstallation(installHistory, user, sensor, isAdd);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		Logutils.log(logUuid, " Exiting from the createSensorInfo", logger);
		return sensor;
	}

	private boolean createGatewaySensorXref(Device sensor, Device gateway, String logUuid) throws Exception {
		String methodName = "createGatewaySensorXref";
		boolean isSaved = false;
		Logutils.log(className, methodName, logUuid,
				" Inside createGatewaySensorXref Method From InstallerService " + "Device " + sensor, logger);
//		List<Device_Device_xref> gatewaySensorXrefLst = new ArrayList();
		List<DeviceSensorxrefPayload> dsxrefPayloadLst = new ArrayList();

//		Device_Device_xref gatewaySensorXref = new Device_Device_xref();
//		gatewaySensorXref.setDateCreated(Instant.now());
//		gatewaySensorXref.setActive(Boolean.TRUE);
//		gatewaySensorXref.setDeviceUuid(gateway);
//		gatewaySensorXref.setSensorUuid(sensor);
//		gatewaySensorXrefLst.add(gatewaySensorXref);

		DeviceSensorxrefPayload dsxrefPayload = new DeviceSensorxrefPayload();
		dsxrefPayload.setDateCreated(Instant.now());
		dsxrefPayload.setActive(Boolean.TRUE);
		dsxrefPayload.setDeviceUuid(gateway);
		dsxrefPayload.setSensorUuid(sensor);
		dsxrefPayloadLst.add(dsxrefPayload);
		Logutils.log(className, methodName, logUuid, " Before calling restUtils.saveGatewaySensorXref method ", logger);
		boolean isSave = restUtils.saveGatewaySensorXref("", dsxrefPayloadLst);
		isSaved = true;
		Logutils.log(logUuid, " Exiting from the createGatewaySensorXref", logger);
		return isSaved;
	}

	private void createSensorHistoryForInstallation(InstallHistory installHistory, User user, Device sensor,
			Boolean isAdd) {
		String methodName = "createSensorHistoryForInstallation";
		Logutils.log(className, methodName, " Inside createSensorHistoryForInstallation Method From InstallerService ",
				"InstallHistory " + installHistory, logger);
		SensorHistoryForInstallation sensorHistoryForInstallation = new SensorHistoryForInstallation();

		if (isAdd) {
			sensorHistoryForInstallation.setCreatedOn(Instant.now());
			sensorHistoryForInstallation.setAction("Added");
		} else {
			if (sensor != null) {
				sensorHistoryForInstallation.setCreatedOn(sensor.getCreatedOn());
				sensorHistoryForInstallation.setUpdatedOn(sensor.getUpdatedOn());
			}
			sensorHistoryForInstallation.setAction("Updated");
		}

		if (user != null) {
			sensorHistoryForInstallation.setUserUuid(user.getUuid());
			sensorHistoryForInstallation.setUpdatedUserUuid(user.getUuid());
			sensorHistoryForInstallation.setUserName(user.getFirstName() + " " + user.getLastName());
			sensorHistoryForInstallation.setUpdatedUserName(user.getFirstName() + " " + user.getLastName());
		}

		if (installHistory != null) {
			if (installHistory.getAsset() != null) {
				sensorHistoryForInstallation.setAssetUuid(installHistory.getAsset().getUuid());
			}

			if (installHistory.getDevice() != null) {
				sensorHistoryForInstallation.setGatewayUuid(installHistory.getDevice().getUuid());
			}

			sensorHistoryForInstallation.setInstallCode(installHistory.getInstallCode());
		}

		if (sensor != null) {
			sensorHistoryForInstallation.setSensorUuid(sensor.getUuid());
			sensorHistoryForInstallation.setProductCode(sensor.getProductCode());
			sensorHistoryForInstallation.setProductName(sensor.getProductName());
		}
		Logutils.log(className, methodName, " Before calling sensorHistoryForInstallationRepository.save method ",
				logger);

		sensorHistoryForInstallationRepository.save(sensorHistoryForInstallation);

	}

	private InstallLog createInstallLog(Device sensor, InstallHistory installHistory, String logUuid) throws Exception {
		String methodName = "createInstallLog";
		Logutils.log(className, methodName, logUuid, " Inside createInstallLog Method From InstallerService "
				+ "InstallHistory " + installHistory.getInstallCode(), logger);
		InstallLog installLog = new InstallLog();
		if (sensor.getStatus().equals(SensorStatus.INSTALLED)) {
			installLog.setEventType(EventType.SENSOR_INSTALLATION_COMPLETE);
		} else {
			installLog.setEventType(EventType.ISSUE);
		}
		installLog.setTimestamp(Instant.now());
		installLog.setSensor(sensor);
		installLog.setInstallHistory(installHistory);
		Logutils.log(className, methodName, logUuid, " Before calling installLogRepository.save method ", logger);
		installLog = installLogRepository.save(installLog);
		Logutils.log(logUuid, " Exiting from the createInstallLog", logger);
		return installLog;
	}

	@Override
	public Page<InProgressInstall> getFinishedInstallationsWithPagination(Pageable pageable, String accountNumber) {

		Context context = new Context();
		Page<InProgressInstall> page = null;
		Page<InstallHistory> installations = null;

		String methodName = "getFinishedInstallationsWithPagination";
		Logutils.log(className, methodName, context.getLogUUId(),
				" Inside the getFinishedInstallationsWithPagination method " + "accountNumber " + accountNumber,
				logger);
		if (utilities.checkInputValue(accountNumber)) {
			Logutils.log(className, methodName, context.getLogUUId(),
					" Before calling installHistoryRepository.findAllByCanWithPagination method ", logger);
			installations = installHistoryRepository.findAllByCanWithPagination(accountNumber, pageable);
		} else {
			Logutils.log(className, methodName, context.getLogUUId(),
					" Before calling installHistoryRepository.findAllWithPagination method ", logger);
			installations = installHistoryRepository.findAllWithPagination(pageable);
		}

		List<InProgressInstall> finishedInstallations = new ArrayList<>();
		if (utilities.checkInputValue(installations) && utilities.checkInputValue(installations.getContent())) {
			finishedInstallations = beanConverter.createInProgressInstallHistoryList(installations.getContent());
			page = new PageImpl<>(finishedInstallations, pageable, installations.getTotalElements());
		}
		Logutils.log(className, methodName, context.getLogUUId(), " Exiting from the InstallerService", logger);
		return page;
	}

	@Override
	public InProgressInstall getInstallationByCanAndImei(String accountNumber, String imei) {

		String methodName = "getInstallationByCanAndImei";
		InProgressInstall finishedInstallation = null;
		String accountNumberLog = "AccountNumber: " + accountNumber + " IMEI : " + imei;
//        Logutils.log(className,methodName,context.getLogUUId()," calling getInstallationByCanAndImei method ",logger, accountNumberLog);
		Logutils.log(className, methodName, " Inside getInstallationByCanAndImei Method From InstallerService ",
				accountNumberLog, logger);
		if (accountNumber != null && imei != null) {

			Logutils.log(className, methodName,
					" Before calling installHistoryRepository.findByInstallationByCanAndImei method ", logger);
			InstallHistory installHistory = installHistoryRepository.findByInstallationByCanAndImei(accountNumber,
					imei);
//             Logutils.log(className,methodName,context.getLogUUId()," After calling installHistoryRepository.findByCompanyUuidAndStatus method ",logger);
			if (installHistory != null) {
				finishedInstallation = beanConverter.createInProgressInstallHistory(installHistory);
			} else {
				throw new InstallerException("No installation found for given can and imei");
			}

		} else {
			throw new InstallerException("CAN and IMEI must be required");
		}
		Logutils.log(className, methodName, " Exiting From InstallerService ", logger);
		return finishedInstallation;

	}

	@Override
	public List<InstalledHistroyResponse> getInstaledHistoryDeviceImei(List<String> deviceImeiList, Context context) {
		String methodName = "getInstaledHistoryDeviceImei";
		Logutils.log(className, context.getLogUUId(), methodName,
				" Inside getInstaledHistoryDeviceImei Method From InstallerService " + "List<String> " + deviceImeiList,
				logger);
		Logutils.log(className, context.getLogUUId(), methodName,
				" Before calling installHistoryRepository.findByDeviceImeis method ", logger);
		List<InstallHistory> installedHistoryList = installHistoryRepository.findByDeviceImeis(deviceImeiList);
		Logutils.log(className, context.getLogUUId(), methodName,
				" Before calling beanConverter.convertInstalledHistoryModelToDTO method ", logger);
		Logutils.log(className, methodName, " Exiting From InstallerService ", logger);
		return beanConverter.convertInstalledHistoryModelToDTO(installedHistoryList);
	}

	public List<SensorReasonCode> getSensorReasonCode(String productName) {
		String methodName = "getSensorReasonCode";
		Logutils.log(className, methodName, " Inside getSensorReasonCode Method From InstallerService ",
				"productName " + productName, logger);
		Logutils.log(className, methodName,
				" Before calling sensorReasonCodeRepository.findBySensorProductName method ", logger);
		List<SensorReasonCode> findBySensorProductName = sensorReasonCodeRepository
				.findBySensorProductName(productName);

		Logutils.log(className, methodName, " Exiting From InstallerService ", logger);
		return findBySensorProductName;
	}

	public List<SensorReasonCode> getSensorReasonCodeByProductCode(String productCode) {
		String methodName = "getSensorReasonCodeByProductCode";
		Logutils.log(className, methodName,
				" " + " Inside getSensorReasonCodeByProductCode Method From InstallerService ",
				"productCode " + productCode, logger);

		Logutils.log(className, methodName,
				" Before calling sensorReasonCodeRepository.findBySensorProductName method ", logger);

		List<SensorReasonCode> findBySensorProductName = sensorReasonCodeRepository
				.findBySensorProductName(productCode);

		Logutils.log(className, methodName, " Exiting From InstallerService ", logger);
		return findBySensorProductName;
	}

	public List<SensorReasonCodeDto> getSensorReasonCodeByDto(String productName) {
		String methodName = "getSensorReasonCodeByDto";
		Logutils.log(className, methodName, " Inside getSensorReasonCodeByDto Method From InstallerService ",
				"productName " + productName, logger);

		Logutils.log(className, methodName,
				" Before calling sensorReasonCodeRepository.findBySensorReasonProductCodeByDto method ", logger);
		List<SensorReasonCodeDto> findBySensorProductName = sensorReasonCodeRepository
				.findBySensorReasonProductCodeByDto(productName);

		Logutils.log(className, methodName, " Exiting From InstallerService ", logger);
		return findBySensorProductName;
	}

	private GatewayDetailsBean deviceToGatewayDetailsBean(Device gat) {
		String methodName = "deviceToGatewayDetailsBean";
		String productNameLog = "ProductName: " + gat.getProductName();
		Logutils.log(className, methodName, " Inside deviceToGatewayDetailsBean Method From InstallerService ",
				"Device " + gat, logger);
//				Logutils.log(className, methodName, context.getLogUUId(),
//						" Inside iterating Gateway LIst to gate product Name ", logger, productNameLog);
		String uuidLog = "Uuid: " + gat.getUuid();
		List<InstallInstructionBean> sensorInstallInstructions = new ArrayList<>();
		Map<String, List<ReasonCodeBean>> reasonCodeBeanMap = new HashMap<>();
		List<AttributeValueResposneDTO> attributeList = new ArrayList<>();
		Logutils.log(className, methodName, " Before calling restUtils.getAttributeListByProductName method ", logger,
				productNameLog, uuidLog);

		List<Attribute> attributes = restUtils.getAttributeListByProductName("", gat.getProductName(), gat.getUuid());
		Logutils.log(className, methodName, " after calling restUtils.getAttributeListByProductName method ", logger,
				productNameLog, uuidLog);
		GatewayDetailsBean gatewatDetails = new GatewayDetailsBean();
		gatewatDetails.setGatewayUuid(gat.getUuid());
		gatewatDetails.setGatewayProductName(gat.getProductName());
		gatewatDetails.setGatewayProductCode(gat.getProductCode());
		// gatewatDetails.setEligibleAssetType(ins.getAsset().getGatewayEligibility());
		if (attributes.size() > 0) {
			gatewatDetails.set_blocker(attributes.get(0).getProductMaster().isBlocker());
			for (Attribute att : attributes) {
				AttributeValueResposneDTO attRes = new AttributeValueResposneDTO();
				attRes.setApplicable(att.isApplicable());
				attRes.setAttribute_uuid(att.getUuid());
				attRes.setAttributeName(att.getAttributeName());
				attRes.setThresholdValue(att.getAttributeValue());
				attributeList.add(attRes);
			}
		}
		gatewatDetails.setGatewayAttribute(attributeList);
		Logutils.log(className, methodName,
				" Before calling sensorInstallInstructionRepository.findBySensorProductName method ", logger,
				productNameLog);

		List<SensorInstallInstruction> sensorInstallInstructionList = sensorInstallInstructionRepository
				.findBySensorProductName(gat.getProductName());
		sensorInstallInstructionList.forEach(sensorInstallInstruction -> {
			InstallInstructionBean installInstructionBean = new InstallInstructionBean();
			installInstructionBean.setInstruction(sensorInstallInstruction.getInstallInstruction().getInstruction());
			installInstructionBean.setSequence(sensorInstallInstruction.getInstallInstruction().getStepSequence());
			sensorInstallInstructions.add(installInstructionBean);
		});
		InstallInstructionComparator comparator = new InstallInstructionComparator();
		Collections.sort(sensorInstallInstructions, comparator);
		gatewatDetails.setGatewayInstallInstructions(sensorInstallInstructions);
		Logutils.log(className, methodName,
				" Before calling sensorReasonCodeRepository.findBySensorProductName method ", logger, productNameLog);

		List<SensorReasonCode> sensorReasonCodeList = sensorReasonCodeRepository
				.findBySensorProductName(gat.getProductName());
		sensorReasonCodeList.forEach(sensorReasonCode -> {
			ReasonCodeBean reasonCodeBean = new ReasonCodeBean();
			reasonCodeBean.setReasonCode(sensorReasonCode.getReasonCode().getCode());
			reasonCodeBean.setDisplayName(sensorReasonCode.getReasonCode().getValue());
			if (reasonCodeBeanMap.get(sensorReasonCode.getReasonCode().getIssueType()) != null) {
				reasonCodeBeanMap.get(sensorReasonCode.getReasonCode().getIssueType()).add(reasonCodeBean);
			} else {
				List<ReasonCodeBean> reasonCodeBeanList = new ArrayList<>();
				reasonCodeBeanList.add(reasonCodeBean);
				reasonCodeBeanMap.put(sensorReasonCode.getReasonCode().getIssueType(), reasonCodeBeanList);
			}
		});
		gatewatDetails.setGatewayReasonCodes(reasonCodeBeanMap);
		Logutils.log(className, methodName, " Exiting From InstallerService ", logger);
		return gatewatDetails;

	}

	private List<Attribute> getProductByName(String name) {
		String methodName = "getProductByName";
		Logutils.log(className, methodName, " Inside getProductByName Method From InstallerService ", " name " + name,
				logger);
		List<Attribute> at = new ArrayList<>();
		Logutils.log(className, methodName, " Before calling productMasterRepository.findByProductName() method ",
				logger);
		ProductMaster pd = productMasterRepository.findByProductName(name);
		if (pd != null && pd.getUuid() != null) {
			Logutils.log(className, methodName, " Before calling atttributeRespo.findByProductMasterUuid() method ",
					logger);
			at = atttributeRespo.findByProductMasterUuid(pd.getUuid());
		}
		Logutils.log(className, methodName, " Exiting From InstallerService ", logger);
		return at;
	}

	private List<Attribute> getProductByCode(String code) {
		String methodName = "getProductByCode";
		Logutils.log(className, methodName, " Inside getProductByCode Method From InstallerService ", " code " + code,
				logger);
		List<Attribute> at = new ArrayList<>();
		Logutils.log(className, methodName, " Before calling productMasterRepository.findByProductCode() method ",
				logger);
		ProductMaster pd = productMasterRepository.findByProductCode(code);
		if (pd != null && pd.getUuid() != null) {
			Logutils.log(className, methodName, " Before calling atttributeRespo.findByProductMasterUuid() method ",
					logger);
			at = atttributeRespo.findByProductMasterUuid(pd.getUuid());
		}
		Logutils.log(className, methodName, " Exiting From InstallerService ", logger);
		return at;
	}

	private List<ProductMasterDto> getProductMasters() {
		String methodName = "getProductMasters";
		Logutils.log(className, methodName, " Inside getProductMasters Method From InstallerService ", logger);
		Logutils.log(className, methodName, " Before calling productMasterRepository.getAllProductMaster() method ",
				logger);
		List<ProductMasterDto> at = productMasterRepository.getAllProductMaster();
		Logutils.log(className, methodName, "Exiting From InstallerService ", logger);
		return at;
	}

	private List<Attribute> getAttributes() {
		String methodName = "getAttributes";
		Logutils.log(className, methodName, " Inside getAttributes Method From InstallerService ", logger);
		Logutils.log(className, methodName, " Before calling atttributeRespo.findAll() method ", logger);
		List<Attribute> at = atttributeRespo.findAll();
		Logutils.log(className, methodName, " Exiting From InstallerService ", logger);
		return at;
	}

	public String getInstallationDate(String assetUuid) {

//			Instant installationDate = installHistoryRepository.getInstallationDate(assetUuid);

		return null;

	}

	@Override
	public Page<GatewayDetailsBean> getGatewaysByAccountNumberAndStatusWithPaginationV2Testing(String accountNumber,
			String status, Pageable pageable, List<String> cans, Instant lastDownloadeTime) {
		// TODO Auto-generated method stub
		return null;
	}

}
