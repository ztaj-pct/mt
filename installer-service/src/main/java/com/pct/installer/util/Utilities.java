package com.pct.installer.util;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pct.common.constant.InstallHistoryStatus;
import com.pct.common.dto.DeviceResponsePayloadForAssetUpdate;
import com.pct.common.model.Asset;
import com.pct.common.model.AssetSensorXref;
import com.pct.common.model.Device;
import com.pct.common.model.InstallHistory;
import com.pct.common.model.InstallLog;
import com.pct.common.model.Organisation;
import com.pct.common.model.SensorInstallInstruction;
import com.pct.common.model.SensorReasonCode;
import com.pct.common.model.User;
import com.pct.common.payload.AssetSensorXrefPayload;
import com.pct.common.payload.DeviceSensorxrefPayload;
import com.pct.common.payload.InstallInstructionBean;
import com.pct.common.payload.ReasonCodeBean;
import com.pct.common.payload.SaveAssetGatewayXrefRequest;
import com.pct.common.util.Logutils;
import com.pct.installer.entity.LogIssue;
import com.pct.installer.exception.InstallerException;
import com.pct.installer.payload.SensorDetailsBean;
import com.pct.installer.payload.StartInstallRequest;
import com.pct.installer.repository.IDeviceRepository;
import com.pct.installer.repository.IInstallHistoryRepository;
import com.pct.installer.repository.IInstallLogRepository;
import com.pct.installer.repository.ILogIssueRepository;
import com.pct.installer.repository.ISensorInstallInstructionRepository;
import com.pct.installer.repository.ISensorReasonCodeRepository;
import com.pct.installer.repository.SensorDetailRepository;

/**
 * @author Aamir on 05/11/22
 */

@Component
public class Utilities {

	Logger logger = LoggerFactory.getLogger(Utilities.class);

	private final ObjectMapper objectMapper = new ObjectMapper();

	boolean status = Boolean.FALSE;

	@Autowired
	private IInstallHistoryRepository installHistoryRepository;
	
	@Autowired
	private IInstallLogRepository installLogRepository;
	
	@Autowired
	private ILogIssueRepository logIssueRepository;
	
	@Autowired
	private ISensorReasonCodeRepository sensorReasonCodeRepository;
	
	@Autowired
	private ISensorInstallInstructionRepository sensorInstallInstructionRepository;

	@Autowired
	private RestUtils restUtils;
	
	@Autowired
	Utilities utilities;
	
	@Autowired
	private IDeviceRepository deviceRepository;
	
	
	@Autowired
	private SensorDetailRepository sensorDetailRepository;
	
	
	
	
	public List<SensorDetailsBean> getSensorDetailsBeanList(List<String> productNameList){
		List<SensorDetailsBean> sensorDetailsBeanList = new ArrayList<>();
		productNameList.forEach(productName -> {
			SensorDetailsBean sensorDetailsBean = new SensorDetailsBean();
			String productNameLog = "ProductName: " + productName;

			List<SensorInstallInstruction> sensorInstallInstructionList = sensorInstallInstructionRepository
					.findBySensorProductName(productName);
			sensorDetailsBean.setSensorProductName(productName);
			List<InstallInstructionBean> sensorInstallInstructions = new ArrayList<>();
			sensorInstallInstructionList.forEach(sensorInstallInstruction -> {
				sensorDetailsBean.setSensorProductCode(sensorInstallInstruction.getSensorProductCode());
				InstallInstructionBean installInstructionBean = new InstallInstructionBean();
				if(sensorInstallInstruction.getInstallInstruction() != null && sensorInstallInstruction.getInstallInstruction().getInstruction() != null) {
					installInstructionBean
					.setInstruction(sensorInstallInstruction.getInstallInstruction().getInstruction());
				}
				if(sensorInstallInstruction.getInstallInstruction() != null && sensorInstallInstruction.getInstallInstruction().getStepSequence() != 0) {
					installInstructionBean.setSequence(sensorInstallInstruction.getInstallInstruction().getStepSequence());
				}
				
				sensorInstallInstructions.add(installInstructionBean);
			});
			sensorDetailsBean.setSensorInstallInstructions(sensorInstallInstructions);

			Map<String, List<ReasonCodeBean>> reasonCodeBeanMap = new HashMap<>();

			List<SensorReasonCode> sensorReasonCodeList = sensorReasonCodeRepository
					.findBySensorProductName(productName.toString());
			sensorReasonCodeList.forEach(sensorReasonCode -> {
				ReasonCodeBean reasonCodeBean = new ReasonCodeBean();
				if(sensorReasonCode.getReasonCode() != null) {
					reasonCodeBean.setReasonCode(sensorReasonCode.getReasonCode().getCode());
					reasonCodeBean.setDisplayName(sensorReasonCode.getReasonCode().getValue());
					if (reasonCodeBeanMap.get(sensorReasonCode.getReasonCode().getIssueType()) != null) {
						reasonCodeBeanMap.get(sensorReasonCode.getReasonCode().getIssueType()).add(reasonCodeBean);
					} else {
						List<ReasonCodeBean> reasonCodeBeanList = new ArrayList<>();
						reasonCodeBeanList.add(reasonCodeBean);
						reasonCodeBeanMap.put(sensorReasonCode.getReasonCode().getIssueType(), reasonCodeBeanList);
					}
				}
				
			});
			sensorDetailsBean.setSensorReasonCodes(reasonCodeBeanMap);
			sensorDetailsBeanList.add(sensorDetailsBean);
		});
		
		return sensorDetailsBeanList;
	}
	
	public void deleteRepoForResetInstallation2(String logUUid, String imei,String mac){

		// logger.debug("Only IMEI is present");
		Device gateway = null;
		if (imei != null && imei != "") {
//			gateway = restUtils.getGatewayByImei(logUUid, imei);
			gateway = deviceRepository.findByImei(imei);
			if (gateway == null) {
				throw new InstallerException("This IMEI could not be found. Please check and try again.");
			}
		}
		if (mac != null && mac != "") {
//			gateway = restUtils.getGatewayByMACAndCan(logUUid, mac, null);
			gateway = deviceRepository.findByMac_address(mac);

			if (gateway == null) {
				throw new InstallerException(
						"This MAC address could not be found. Please check and try again...");
			}
		}
		// Gateway gateway = restUtils.getGatewayByImei(imei);

		List<InstallHistory> installHistories = installHistoryRepository.findByGatewayId(gateway.getId());
		if (installHistories != null && !installHistories.isEmpty()) {
			for (InstallHistory installHistory : installHistories) {
				List<InstallLog> installLogList = installLogRepository
						.findByInstallHistory(installHistory.getId());
				if (installLogList != null && !installLogList.isEmpty()) {
					installLogList.forEach(installLog -> {
						if(installLog.getSensorDetail() != null) {
							sensorDetailRepository.delete(installLog.getSensorDetail());
							installLog.setSensorDetail(null);
						}
						installLogRepository.delete(installLog);
					});
				}
				List<LogIssue> logIssueList = logIssueRepository
						.findByInstallCode(installHistory.getInstallCode());
				logIssueList.forEach(logIssue -> {
					logIssueRepository.delete(logIssue);
				});
				status = restUtils.resetInstallInDeviceService(logUUid, null, gateway.getId());
				installHistoryRepository.delete(installHistory);
				deleteAssetByUUID(logUUid, installHistory.getAsset());
				Logutils.log(logUUid, "after deleting install history from the installation service ", logger,
						installHistory.getInstallCode());
			}
		} else {
			throw new InstallerException(
					"This [IMEI]/[MAC address]/[VIN] is not part of an in-progress installation. Please check and try again.");
		}
	
	}
	public void deleteRepoForResetInstallation1(String logUUid, String vin){
		logger.debug("Only VIN is present");
		Logutils.log(logUUid, " Before restUtil calling getAssetByVin  method ", logger, vin);
		Asset asset = restUtils.getAssetByVin(logUUid, vin);
		if (asset == null) {
			throw new InstallerException("This VIN could not be found. Please check and try again.");
		}
		Logutils.log(logUUid, " after restUtil calling getAssetByVin  method ", logger, vin);
		Logutils.log(logUUid, " before calling findByAssetId  method ", logger, asset.getUuid());
		List<InstallHistory> installHistories = installHistoryRepository.findByAssetId(asset.getId());
		Logutils.log(logUUid, " after calling findByAssetId method getting list of install history", logger);
		if (utilities.checkInputValue(installHistories)) {
			for (InstallHistory installHistory : installHistories) {
				List<InstallLog> installLogList = installLogRepository
						.findByInstallHistory(installHistory.getId());
				if (installLogList != null && !installLogList.isEmpty()) {
					installLogList.forEach(installLog -> {
						if(installLog.getSensorDetail() != null) {
							sensorDetailRepository.delete(installLog.getSensorDetail());
							installLog.setSensorDetail(null);
						}
						
						installLogRepository.delete(installLog);
					});
				}
				List<LogIssue> logIssueList = logIssueRepository
						.findByInstallCode(installHistory.getInstallCode());
				logIssueList.forEach(logIssue -> {
					logIssueRepository.delete(logIssue);
				});
				status = restUtils.resetInstallInDeviceService(logUUid, asset.getId(), null);
				installHistoryRepository.delete(installHistory);
				deleteAssetByUUID(logUUid, asset);
				Logutils.log(logUUid, "after deleting install history from the installation service ", logger,
						installHistory.getInstallCode());
			}

		} else {
			throw new InstallerException(
					"This [Device-ID]/[VIN] is not part of an in-progress installation. Please check and try again.");
		}
	}
	
	private Boolean deleteAssetByUUID(String logUUid, Asset asset) {
		Logutils.log(logUUid, " Inside the method ", logger);
		Boolean isDeleted = false;
		try {
			if (asset != null && asset.getOrganisation() != null
					&& asset.getOrganisation().getIsAssetListRequired() != null
					&& !asset.getOrganisation().getIsAssetListRequired()) {
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
	
	
	public void createInstallationHisteory(String logUUid, Asset asset,Device device){
		List<InstallHistory> installHistories = installHistoryRepository.findByAssetAndGateway(asset.getId(),
				device.getId());
		Logutils.log(logUUid, " After calling installHistoryRepository.findByAssetAndGateway method ", logger);

		if (utilities.checkInputValue(installHistories)) {
			for (InstallHistory installHistory : installHistories) {
				String installHistoryId = "InstallHistoryId: " + installHistory.getId();
				Logutils.log(logUUid, " Before calling installLogRepository.findByInstallHistory method ",
						logger, installHistoryId);

				List<InstallLog> installLogList = installLogRepository
						.findByInstallHistory(installHistory.getId());
				Logutils.log(logUUid, " After calling installLogRepository.findByInstallHistory method ",
						logger);

				if (utilities.checkInputValue(installLogList)) {
					installLogList.forEach(installLog -> {
						String installLogIdLog = "InstallLogId: " + installLog.getId();
						Logutils.log(logUUid, " Before calling installLogRepository.delete method ", logger,
								installLogIdLog);
						installLogRepository.delete(installLog);
						Logutils.log(logUUid, " After calling installLogRepository.delete method ", logger);

					});
				}
				String installCodeLog = "InstallCode: " + installHistory.getInstallCode();
				Logutils.log(logUUid, " Before calling logIssueRepository.findByInstallCode method ", logger,
						installCodeLog);

				List<LogIssue> logIssueList = logIssueRepository
						.findByInstallCode(installHistory.getInstallCode());
				Logutils.log(logUUid, " After calling logIssueRepository.findByInstallCode method ", logger);

				logIssueList.forEach(logIssue -> {
					Logutils.log(logUUid, " Before calling  logIssueRepository.delete method ", logger,
							installCodeLog);
					logIssueRepository.delete(logIssue);
				});
				Logutils.log(logUUid, " After deleting log issue ", logger);
				status = restUtils.resetInstallInDeviceService(logUUid, asset.getId(), device.getId());
				installHistoryRepository.delete(installHistory);
				deleteAssetByUUID(logUUid, asset);
				Logutils.log(logUUid, "after deleting install history from the installation service ", logger,
						installCodeLog);

			}
		} else {
			List<InstallHistory> installHistoriesByAsset = installHistoryRepository
					.findByAssetId(asset.getId());
			List<InstallHistory> installHistoriesByGateway = installHistoryRepository
					.findByGatewayId(device.getId());
			if (installHistoriesByAsset != null && !installHistoriesByAsset.isEmpty()
					&& installHistoriesByGateway != null && !installHistoriesByGateway.isEmpty()) {
				throw new InstallerException(
						"This [Device ID] and [VIN] are not part of same in-progress installation. Please check and try again.");

			} else
				throw new InstallerException(
						"This [DeviceID]/[VIN] is not part of an in-progress installation. Please check and try again.");
		}
	}
	
	
	public Device getDeviceForRest(String logUUid, String imei,String mac,String can){
		Device device = null;	
		if (utilities.checkInputValue(imei)) {
			String imeiLog = "Imei: " + imei;
			logger.debug("VIN and IMEI both are present");
//			Logutils.log(logUUid, " Before calling restUtils.getGatewayByImeiAndCan method ", logger, imeiLog,
//					canLog);

//			device = restUtils.getGatewayByImeiAndCan(logUUid, imei, can);
			device = deviceRepository.findByImei(imei);
//			if (device != null) {
//				gatewayIdLog = gatewayIdLog + device.getId();
//			}
//			Logutils.log(logUUid, " After calling restUtils.getGatewayByImeiAndCan method ", logger,
//					gatewayIdLog);

			if (device == null) {
				throw new InstallerException("This IMEI could not be found. Please check and try again.");
			}
		} else if (utilities.checkInputValue(mac)) {
//			String macLog = "Mac: " + mac;
//			logger.debug("VIN and MAC address both are present");
//			Logutils.log(logUUid, " Before calling restUtils.getGatewayByMACAndCan method ", logger, macLog,
//					canLog);

//			device = restUtils.getGatewayByMACAndCan(logUUid, mac, can);
			device = deviceRepository.findByMac_address(mac);
//			if (device != null) {
//				gatewayIdLog = gatewayIdLog + device.getId();
//			}
//			Logutils.log(logUUid, " After calling restUtils.getGatewayByMACAndCan method ", logger,
//					gatewayIdLog);

			if (device == null) {
				throw new InstallerException(
						"This MAC address could not be found. Please check and try again.");
			}
		}
		return device;
	}

	public Device getDevice(String logUUid, StartInstallRequest startInstallRequest) {
		Logutils.log("getDevice", logUUid, " Calling getDevice from Utilities", logger);
		String macReg = "^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$";
		String imeiReg = "^([0-9]{15})$";
		Pattern macPattern = Pattern.compile(macReg);
		Pattern imeiPattern = Pattern.compile(imeiReg);
		Matcher macMatcher = macPattern.matcher(startInstallRequest.getDeviceID().trim());
		Matcher imeiMatcher = imeiPattern.matcher(startInstallRequest.getDeviceID().trim());
		Device device = null;

		Logutils.log(logUUid, " Installation Service method ", logger, startInstallRequest.toString());

		if (macMatcher.matches()) {
			//device = restUtils.getGatewayByMACAndCan(logUUid, startInstallRequest.getDeviceID(), null);
			device = deviceRepository.findByMac_address(startInstallRequest.getDeviceID());

		} else if (imeiMatcher.matches()) {
			//device = restUtils.getGatewayByImei(logUUid, startInstallRequest.getDeviceID());
			device = deviceRepository.findByImei(startInstallRequest.getDeviceID());

		} else {
			throw new InstallerException(
					"Installation start for " + startInstallRequest.getDeviceID() + " has been unsuccessful due to incorrect format of Device ID.");
		}
		return device;
	}

	public boolean checkFieldsNullablity(String logUUid, StartInstallRequest startInstallRequest) {
		Logutils.log("checkFieldsNullablity", logUUid, " Calling checkFieldsNullablity from Utilities", logger);
		return startInstallRequest != null 
				&& utilities.checkInputValue(startInstallRequest.getDeviceID()) 
				&&utilities.checkInputValue(startInstallRequest.getAssetUuid() )
				&& utilities.checkInputValue(startInstallRequest.getInstallUuid() )
				&& utilities.checkInputValue(startInstallRequest.getDatetimeRT() );
	}

	public boolean createAssetDeviceyXrefRequest(String logUUid, StartInstallRequest startInstallRequest) {
		Logutils.log("createAssetDeviceyXrefRequest", logUUid, " Calling createAssetDeviceyXrefRequest from Utilities", logger);
		SaveAssetGatewayXrefRequest saveAssetGatewayXrefRequest = new SaveAssetGatewayXrefRequest();
		saveAssetGatewayXrefRequest.setAssetUuid(startInstallRequest.getAssetUuid());
		saveAssetGatewayXrefRequest.setImei(startInstallRequest.getDeviceID());
		saveAssetGatewayXrefRequest.setDatetimeRT(startInstallRequest.getDatetimeRT());
		saveAssetGatewayXrefRequest.setLogUUId(logUUid);
		saveAssetGatewayXrefRequest.setIsActive(true);
		Logutils.log(logUUid, " Before calling restUtils.saveAssetGatewayXref.save method", logger);

		boolean issaveAssetGatewayXref = restUtils.saveAssetGatewayXref(saveAssetGatewayXrefRequest);

		Logutils.log(logUUid, " After calling restUtils.saveAssetGatewayXref.save method", logger);
		status = true;
		return status;
	}

	public InstallHistory createInstallHistory(String logUUid, Asset asset, Device device, Long userId, StartInstallRequest startInstallRequest) {
		Logutils.log("createInstallHistory", logUUid, " Calling createInstallHistory from Utilities", logger);
		InstallHistory installHistory = null;
		User user = restUtils.getUserFromAuthService(logUUid, userId);
		if (device != null && asset != null) {
			if (asset.getGatewayEligibility() != null && !asset.getGatewayEligibility().equalsIgnoreCase(device.getProductName())) {
				Logutils.log(logUUid, " eligibility gateway and gateway product name is not matched", logger, asset.getGatewayEligibility(),
						device.getProductName());
				throw new InstallerException("This device type does not match the selected asset");
			}
			Logutils.log(logUUid, " Before calling findByAssetAndGateway method", logger);
			List<InstallHistory> installHistoryList = installHistoryRepository.findByAssetAndGateway(asset.getId(), device.getId());
			if (installHistoryList != null && !installHistoryList.isEmpty() && installHistoryList.size() > 0) {
				logger.error("Exception occurred while creating job", "InstallHistory is already created for this device");
				Logutils.log(logUUid, "Install history found by findByAssetAndGateway method", logger);
				throw new InstallerException("InstallHistory is already created for this device and asset");
			} else {
				String installHistryStatusLog = "InstallHistoryStatus: " + InstallHistoryStatus.STARTED;

				Logutils.log(logUUid, " Before calling findByAssetUuidAndStatus method", logger, installHistryStatusLog);
				List<InstallHistory> installHistoryByAsset = installHistoryRepository.findByAssetUuidAndStatus(asset.getUuid(),
						InstallHistoryStatus.STARTED);
				Logutils.log(logUUid, " After calling findByAssetUuidAndStatus method", logger);

				String gatewayUuidLog = "GatewayUuid: " + device.getUuid();
				Logutils.log(logUUid, " Before calling findByGatewayUuidAndStatus method", logger, gatewayUuidLog, installHistryStatusLog);

				List<InstallHistory> installHistoryByGateway = installHistoryRepository.findByGatewayUuidAndStatus(device.getUuid(),
						InstallHistoryStatus.STARTED);
				Logutils.log(logUUid, " After calling findByGatewayUuidAndStatus method", logger);

				if ((installHistoryByAsset == null || installHistoryByAsset.isEmpty())
						&& (installHistoryByGateway == null || installHistoryByGateway.isEmpty())) {
					DeviceResponsePayloadForAssetUpdate deviceResponsePayloadForAssetUpdate = restUtils.updateAssetForGatewayV1(logUUid, device.getUuid(), asset.getUuid());

					installHistory = new InstallHistory();
					installHistory.setAsset(asset);
					installHistory.setDevice(device);
					installHistory.setDateStarted(Instant.ofEpochMilli(Long.parseLong(startInstallRequest.getDatetimeRT())));
					installHistory.setCreatedOn(Instant.now());
					installHistory.setCreatedBy(user);
					installHistory.setInstallCode(startInstallRequest.getInstallUuid());
					installHistory.setStatus(InstallHistoryStatus.STARTED);
					if (asset.getOrganisation() != null) {
						installHistory.setOrganisation(asset.getOrganisation());
					} else {
						installHistory.setOrganisation(device.getOrganisation());
						if(deviceResponsePayloadForAssetUpdate != null && deviceResponsePayloadForAssetUpdate.getOrganisationAccNo() != null) {
							Organisation org = new Organisation();
							org.setId(deviceResponsePayloadForAssetUpdate.getOrganisationId());
							org.setUuid(deviceResponsePayloadForAssetUpdate.getOrganisationUuid());
							org.setAccountNumber(deviceResponsePayloadForAssetUpdate.getOrganisationAccNo());
							installHistory.setOrganisation(org);
						restUtils.updateAssetCompany(logUUid, deviceResponsePayloadForAssetUpdate.getOrganisationAccNo(), startInstallRequest.getAssetUuid());
						}
						else {
							restUtils.updateAssetCompany(logUUid, device.getOrganisation().getAccountNumber(), startInstallRequest.getAssetUuid());
						}
						}
					
				
					installHistory.setCreatedBy(user);
					if (startInstallRequest.getAppVersion() != null && !startInstallRequest.getAppVersion().isEmpty()) {
						installHistory.setAppVersion(startInstallRequest.getAppVersion());
						Logutils.log(logUUid, " App Version Of IA : " + startInstallRequest.getAppVersion(), logger);
					} else {
						Logutils.log(logUUid, " App Version is null", logger);
					}

					Logutils.log(logUUid, " Before calling installHistoryRepository.save method", logger);
					UUID uuid = UUID.randomUUID();
					installHistory.setUuid(uuid.toString());
					installHistory = installHistoryRepository.save(installHistory);
					Logutils.log(logUUid, " After calling installHistoryRepository.save method", logger);
					Logutils.log(logUUid, " After calling restUtils.saveAssetGatewayXref.save method", logger);

					/*
					 * Logutils.log(className,methodName,context.getLogUUId()
					 * ," Call method : callSendUDPCommandAPI of restUtils ",logger); Boolean flag =
					 * restUtils.callSendUDPCommandAPI(gateway.getImei());
					 * Logutils.log(className,methodName,context.getLogUUId()
					 * ,"After Calling method : callSendUDPCommandAPI of restUtils and flag is : " +
					 * flag,logger);
					 */

				} else if (installHistoryByAsset != null && !installHistoryByAsset.isEmpty()) {
					logger.error("Exception occurred while creating install history, In progress Install history already exists for Asset", "In progress Install history already exists for Asset");
					throw new InstallerException("Asset Not Available/This asset already has a device installed. Please choose another asset.");
				} else if (installHistoryByGateway != null && !installHistoryByGateway.isEmpty()) {
					logger.error("Exception occurred while creating install history, In progress Install history already exists for Gateway ", "In progress Install history already exists for Gateway");
					throw new InstallerException("Device is Already Installed");
				}
			}

		}
		status = true;
		return installHistory;
	}

	public Boolean prePairingInstallationAtTheTimeOfStartInstall(String logUUId, Asset asset, Device device, Long userId, InstallHistory installHistory) {
		Logutils.log("prePairingInstallationAtTheTimeOfStartInstall", logUUId, " Calling prePairingInstallationAtTheTimeOfStartInstall from Utilities",
				logger);
		Boolean status = false;
		List<DeviceSensorxrefPayload> deviceSensorxrefPayloadList = new ArrayList();
		List<AssetSensorXrefPayload> assetSensorXrefPayloadList = new ArrayList();

		User user = restUtils.getUserFromAuthService(logUUId, userId);
		if (asset != null) {
			List<AssetSensorXref> listOfAssetSensorXref = restUtils.getAllAssetSensorXrefForAssetUuid(logUUId, asset.getUuid());
			if (utilities.checkInputValue(listOfAssetSensorXref)&& asset.getIsApplicableForPrePair()) {
				if (listOfAssetSensorXref.get(0).getIsGatewayAttached()) {
					logger.info("Pre pairing is already done for asset : " + asset.getUuid());
				} else {
					for (AssetSensorXref assetSensorXref : listOfAssetSensorXref) {
						if (device.getIotType() != null && device.getIotType() == device.getIotType().SENSOR) {
							Boolean isSensorRepeated = false;//
							device = assetSensorXref.getDevice();
							if (device.getProductCode().trim().equalsIgnoreCase(assetSensorXref.getDevice().getProductCode().trim())) {
								isSensorRepeated = true;
								break;
							}
							if (isSensorRepeated) {
								continue;
							}
						}
						DeviceSensorxrefPayload deviceSensorxrefPayload = new DeviceSensorxrefPayload();
						deviceSensorxrefPayload.setDateCreated(Instant.now());
						deviceSensorxrefPayload.setActive(false);
						deviceSensorxrefPayload.setDeviceUuid(device);
						deviceSensorxrefPayload.setCreatedBy(user);
						deviceSensorxrefPayload.setDateCreated(Instant.now());
						deviceSensorxrefPayload.setLogUUId(logUUId);
						Device sensor = assetSensorXref.getDevice();
						deviceSensorxrefPayload.setSensorUuid(sensor);

						AssetSensorXrefPayload assetSensorXrefPayload = new AssetSensorXrefPayload();
						assetSensorXrefPayload.setIsGatewayAttached(Boolean.TRUE);
						assetSensorXrefPayload.setAsset(assetSensorXref.getAsset());
						assetSensorXrefPayload.setDevice(assetSensorXref.getDevice());
						assetSensorXrefPayload.setIsActive(assetSensorXref.getIsActive());
						assetSensorXrefPayload.setLogUUId(logUUId);
						assetSensorXrefPayload.setId(assetSensorXref.getId());
						assetSensorXrefPayload.setLogUUId(logUUId);

						assetSensorXrefPayloadList.add(assetSensorXrefPayload);
						deviceSensorxrefPayloadList.add(deviceSensorxrefPayload);

						// Aamir note
						// TODO: need clarification
//						List<SubSensor> listOfSubSensor = restUtils.findBySensorUuid(sensor.getUuid());
//						if (listOfSubSensor != null && listOfSubSensor.size() > 0) {
//							for (SubSensor subSensor : listOfSubSensor) {
//								InstallLog installLog = new InstallLog();
//								installLog.setEventType(EventType.INSTALLATION);
//								installLog.setStatus(InstallLogStatus.STARTED);
//								installLog.setInstallHistory(installHistory);
//								if (subSensor.getInstanceType() != null) {
//									installLog.setInstanceType(subSensor.getInstanceType());
//								} else {
//									installLog.setInstanceType(InstanceType.DEFAULT.toString());
//								}
//								if (subSensor.getSubSensorId() != null) {
//									installLog.setSensorId(subSensor.getSubSensorId());
//								}
//								if(subSensor.getType() != null) {
//									installLog.setType(subSensor.getType());
//								}
//								installLog.setTimestamp(Instant.now());
//								installLog.setSensor(subSensor.getSensor());
//								installLog = installLogRepository.save(installLog);
//							}
//							status = true;
//						}
					}
					restUtils.updateAssetSensorXref(logUUId, assetSensorXrefPayloadList);
					restUtils.saveGatewaySensorXref(logUUId, deviceSensorxrefPayloadList);
				}
			}
			status = true;
		} else {
			throw new InstallerException("Asset uuid not found");
		}
		
		return status;
	}
	
public boolean checkInputValue(Object value){
	
	if(value instanceof Integer){
		return ((Integer) value).intValue()>0 && value!=null;
	}
	if(value instanceof Long){
		return ((Long) value).longValue()>0.0 && value!=null;
	}
	if(value instanceof String){
		return !((String) value).isEmpty() && value!=null && !((String) value).equalsIgnoreCase("");
	}
	if(value instanceof Collection){
		return !((Collection) value).isEmpty() && value!=null;
	}	
	if(value instanceof Object){
		return  value!=null;
	}	
	
	return false;	
	}
}
