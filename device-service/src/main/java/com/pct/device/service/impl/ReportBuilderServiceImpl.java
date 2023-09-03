package com.pct.device.service.impl;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pct.common.model.Asset;
import com.pct.common.model.Asset_Device_xref;
import com.pct.common.model.Device;
import com.pct.common.model.DeviceDetails;
import com.pct.common.model.DeviceQa;
import com.pct.device.dto.AssetToDeviceDTO;
import com.pct.device.dto.DeviceHealthDTO;
import com.pct.device.dto.RyderApiDTO;
import com.pct.device.exception.DeviceException;
import com.pct.device.payload.AddAssetResponse;
import com.pct.device.payload.AssestAssociationRequest;
import com.pct.device.payload.AssestDissociationRequest;
import com.pct.device.payload.AssestReassignmentRequest;
import com.pct.device.repository.DeviceQARepository;
import com.pct.device.repository.IAssetDeviceXrefRepository;
import com.pct.device.repository.IAssetRepository;
import com.pct.device.repository.IDeviceRepository;
import com.pct.device.repository.RedisDeviceRepository;
import com.pct.device.service.IReportBuilderService;
import com.pct.device.util.StringUtils;

@Service
public class ReportBuilderServiceImpl implements IReportBuilderService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ReportBuilderServiceImpl.class);

	private static final String DEVICE_ID_PREFIX = "deviceID:";

	@Autowired
	private IAssetRepository assetRepository;

	@Autowired
	private IAssetDeviceXrefRepository assetDeviceXrefRepository;

	@Autowired
	private IDeviceRepository deviceRepository;

	@Autowired
	RedisDeviceRepository redisDeviceRepository;
	
	@Autowired
	DeviceQARepository deviceQARepository;

	@PersistenceContext
	EntityManager entityManager;

	String can = "A-00097";

	@Override
	public AddAssetResponse assestAssociation(AssestAssociationRequest assestAssociationRequest, String can,
			String msgUuid) {
		LOGGER.info(
				"MsgUuid: " + msgUuid + " Assest assignment Request Parameters " + assestAssociationRequest.toString());
		if (assestAssociationRequest.getGatewayId() == null || assestAssociationRequest.getGatewayId().equals("")) {
			throw new DeviceException("Gateway id can not be empty");
		}
		if (assestAssociationRequest.getAssetId() == null || assestAssociationRequest.getAssetId().equals("")) {
			throw new DeviceException("Asset id can not be empty");
		}

		Device device = deviceRepository.getByDeviceId(assestAssociationRequest.getGatewayId());
		if (device == null) {
			throw new DeviceException("Gateway not found for gateway id " + assestAssociationRequest.getGatewayId());
		}
		Asset asset = assetRepository.findByAssetId(assestAssociationRequest.getAssetId());
		if (asset == null) {
			throw new DeviceException("Asset not found for Asset id " + assestAssociationRequest.getAssetId());
		}
		Device validateCustomerDevice = deviceRepository.getByDeviceIdAndCan(assestAssociationRequest.getGatewayId(),
				can);
		if (validateCustomerDevice == null) {
			throw new DeviceException(
					"Gateway id " + assestAssociationRequest.getGatewayId() + " is not belongs to " + can);
		}
		validateDeviceIdAlreadyAssignedWithAnotherAssest(assestAssociationRequest.getGatewayId(),
				assestAssociationRequest.getAssetId(), can, assestAssociationRequest, msgUuid);
		if (assestAssociationRequest.getOverWriteIfExists() != null
				&& assestAssociationRequest.getOverWriteIfExists() == true) {

			List<Asset_Device_xref> assetToDeviceByDeviceList = assetDeviceXrefRepository
					.findByAssetID(assestAssociationRequest.getAssetId(), can);
			for (Asset_Device_xref asset_Device_xref : assetToDeviceByDeviceList) {
				assetDeviceXrefRepository.delete(asset_Device_xref);
				Map<String, String> assetMap = new HashMap<String, String>();
				redisDeviceRepository.add(DEVICE_ID_PREFIX + asset_Device_xref.getDevice().getImei(), assetMap);

			}

		}
		Asset_Device_xref assetToDevice = new Asset_Device_xref();
		assetToDevice.setDevice(device);
		assetToDevice.setAsset(asset);
		assetToDevice.setActive(true);
		assetToDevice.setComment("Updated from Report Builder API");
		if (assestAssociationRequest.getEventDateTime() != null) {
			final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
					.withZone(ZoneId.systemDefault());
			Instant result = Instant.from(formatter.parse(assestAssociationRequest.getEventDateTime()));
			assetToDevice.setDateCreated(result);
		}
		assetDeviceXrefRepository.save(assetToDevice);
		LOGGER.info("MsgUuid: " + msgUuid + " assosiation of asset with device save successfully.");
		Map<String, String> assetMap = prepareAssetMapForRedis(assetToDevice);
		redisDeviceRepository.add(DEVICE_ID_PREFIX + assestAssociationRequest.getGatewayId(), assetMap);

		LOGGER.info("MsgUuid: " + msgUuid + " redis updated");
		return null;
	}

	@Override
	public AddAssetResponse assetReassignment(AssestReassignmentRequest assetReassignmentRequest, String can,
			String msgUuid) {
		if (deviceAssetMappingAlreadyExists(assetReassignmentRequest, can)) {
			LOGGER.info("MsgUuid: " + msgUuid + " Assest Reassignment Request Parameters "
					+ assetReassignmentRequest.toString());
			if (assetReassignmentRequest.getGatewayId() == null || assetReassignmentRequest.getGatewayId().equals("")) {
				throw new DeviceException("Gateway id can not be empty");
			}

			if (assetReassignmentRequest.getOldAssetId() == null
					|| assetReassignmentRequest.getOldAssetId().equals("")) {
				throw new DeviceException("Old asset id can not be empty");
			}

			Device device = deviceRepository.getByDeviceId(assetReassignmentRequest.getGatewayId());
			if (device == null) {
				throw new DeviceException(
						"Gateway not found for gateway id " + assetReassignmentRequest.getGatewayId());
			}

			Asset asset = assetRepository.findByAssetId(assetReassignmentRequest.getNewAssetId());
			if (asset == null) {
				throw new DeviceException("Asset not found for Asset id " + assetReassignmentRequest.getNewAssetId());
			}

			Device validateCustomerDevice = deviceRepository
					.getByDeviceIdAndCan(assetReassignmentRequest.getGatewayId(), can);
			if (validateCustomerDevice == null) {
				throw new DeviceException(
						"Gateway id " + assetReassignmentRequest.getGatewayId() + " is not belongs to " + can);
			}

			unRegisterAssetId(assetReassignmentRequest.getNewAssetId(), can);
			unRegisterAssetId(assetReassignmentRequest.getOldAssetId(), can);

			Asset_Device_xref assetToDevice_xref = assetDeviceXrefRepository
					.findByDeviceID(assetReassignmentRequest.getGatewayId());
			if (null != assetToDevice_xref) {
				unRegisterAssetId(assetToDevice_xref.getAsset().getAssignedName(), can);
			}

			Asset_Device_xref assetToDevice = new Asset_Device_xref();
			assetToDevice.setDevice(device);
			assetToDevice.setAsset(asset);
			assetToDevice.setActive(true);
			assetToDevice.setComment("Updated from Report Builder API");
			final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
					.withZone(ZoneId.systemDefault());
			Instant result = Instant.from(formatter.parse(assetReassignmentRequest.getEventDateTime()));
			assetToDevice.setDateCreated(result);
			assetDeviceXrefRepository.save(assetToDevice);
			LOGGER.info("MsgUuid: " + msgUuid + " reassosiation of asset with device save successfully.");
			Map<String, String> assetMap = prepareAssetMapForRedis(assetToDevice);
			redisDeviceRepository.add(DEVICE_ID_PREFIX + assetReassignmentRequest.getGatewayId(), assetMap);
			LOGGER.info("MsgUuid: " + msgUuid + " redis updated");

		}
		return null;
	}

	@Override
	public AddAssetResponse assetDissociation(AssestDissociationRequest assetDissociationRequest, String can,
			String msgUuid) {
		LOGGER.info(
				"MsgUuid: " + msgUuid + " Assest assignment Request Parameters " + assetDissociationRequest.toString());
		if (assetDissociationRequest.getGatewayId() == null || assetDissociationRequest.getGatewayId().equals("")) {
			throw new DeviceException("Gateway id can not be empty");
		}
		if (assetDissociationRequest.getAssetId() == null || assetDissociationRequest.getAssetId().equals("")) {
			throw new DeviceException("Asset id can not be empty");
		}
		Device validateCustomerDevice = deviceRepository.getByDeviceIdAndCan(assetDissociationRequest.getGatewayId(),
				can);
		if (validateCustomerDevice == null) {
			throw new DeviceException(
					"Gateway id " + assetDissociationRequest.getGatewayId() + " is not belongs to " + can);
		}

		List<Asset_Device_xref> assetToDeviceByDeviceList = assetDeviceXrefRepository
				.findByAssetID(assetDissociationRequest.getAssetId(), can);

		for (Asset_Device_xref asset_Device_xref : assetToDeviceByDeviceList) {
			assetDeviceXrefRepository.delete(asset_Device_xref);
			Map<String, String> assetMap = new HashMap<String, String>();
			redisDeviceRepository.add(DEVICE_ID_PREFIX + asset_Device_xref.getDevice().getImei(), assetMap);
		}
		LOGGER.info("MsgUuid: " + msgUuid + " redis updated");
		return null;
	}

	@Override
	public AssetToDeviceDTO getAssetAssociationDetails(String assetId, String can, String msgUuid) {
		AssetToDeviceDTO assetToDeviceDTO = new AssetToDeviceDTO();
		LOGGER.info("MsgUuid: " + msgUuid + " getAssetAssociationDetails Request Parameters " + assetId);
		Asset_Device_xref assetToDevice = null;
		if (null != assetId && !assetId.isEmpty()) {
			List<Asset_Device_xref> assetToDeviceByDeviceList = assetDeviceXrefRepository.findByAssetID(assetId, can);
			if (assetToDeviceByDeviceList != null && assetToDeviceByDeviceList.size() > 0) {
				assetToDevice = new Asset_Device_xref();
				assetToDevice = assetToDeviceByDeviceList.get(0);
			}
			if (assetToDevice == null) {
				assetToDeviceDTO.setAsset_id(null);
				assetToDeviceDTO.setGateway_id(null);
				assetToDeviceDTO.setInstall_timestamp(null);
			} else if (assetToDevice != null) {
				assetToDeviceDTO.setAsset_id(assetToDevice.getAsset().getAssignedName());
				assetToDeviceDTO.setGateway_id(assetToDevice.getDevice().getImei());
				assetToDeviceDTO.setInstall_timestamp(Timestamp.from(assetToDevice.getDateCreated()));
			}
		}
		return assetToDeviceDTO;
	}

	private void validateDeviceIdAlreadyAssignedWithAnotherAssest(String deviceId, String assetId, String can,
			AssestAssociationRequest assestAssociationRequest, String msgUuid) {
		Asset_Device_xref assetToDevice = assetDeviceXrefRepository.findByDeviceID(deviceId);
		if (assetToDevice != null) {
			if (String.valueOf(assetToDevice.getAsset().getAssignedName()).equalsIgnoreCase(assetId)) {
				LOGGER.error("MsgUuid: " + msgUuid + "Association of device " + deviceId + " already exists with asset "
						+ assetId);
				throw new DeviceException(
						"Association of device " + deviceId + " already exists with asset " + assetId);
			} else {
				LOGGER.error("MsgUuid: " + msgUuid + " Device already assigned to another asset Id DeviceId: :"
						+ deviceId + " assetId : " + assetId + " customer : " + can);
				throw new DeviceException(assetToDevice.getDevice().getImei()
						+ " is already associated with a different asset " + assetToDevice.getAsset().getId());
			}
		}

	}

	private Map<String, String> prepareAssetMapForRedis(Asset_Device_xref assetToDevice) {

		Map<String, String> assetMap = new HashMap<>();
		String assetType = null;
		String assetID = null;
		String deviceID = null;

		LOGGER.info("tostring " + assetToDevice.toString());
		if (assetToDevice.getDevice() != null) {
			LOGGER.info(" prepareAssetMapForRedis1 " + assetToDevice.getDevice().getId() + "getInstall_timestamp   "
					+ assetToDevice.getDateCreated());
		} else {
			LOGGER.info(" prepareAssetMapForRedis1  null getInstall_timestamp   " + assetToDevice.getDateCreated());
		}

		assetID = String.valueOf(assetToDevice.getAsset().getId());
		assetID = assetID.trim().toLowerCase();

		if (!assetID.isEmpty()) {

			assetMap.put("deviceName", assetID);

			if (assetToDevice.getDateCreated() != null) {
				assetMap.put("assetDate", assetToDevice.getDateCreated().toString());
			} else {
				assetMap.put("assetDate", "");
			}
			assetType = assetToDevice.getAsset().getAssignedName();
			if (assetType != null) {
				assetType = assetType.trim().toLowerCase();
				if (assetType.isEmpty()) {
					assetType = null;
				}
			}
			if (assetType != null) {
				assetMap.put("assetType", assetType);
			}
			if (assetToDevice.getDevice() != null) {
				deviceID = assetToDevice.getDevice().getImei();
			}
			if (deviceID != null) {
				deviceID = deviceID.trim().toLowerCase();
				if (deviceID.isEmpty()) {
					deviceID = null;
				}
			}
			if (deviceID != null) {
				assetMap.put("deviceID", deviceID);
			} else {
				assetMap.put("deviceID", "");
			}
		}
		return assetMap;
	}

	private boolean deviceAssetMappingAlreadyExists(AssestReassignmentRequest assetReassignmentRequest, String can) {

		if (assetReassignmentRequest.getNewAssetId() == null || assetReassignmentRequest.getNewAssetId().equals("")) {
			throw new DeviceException("New asset id can not be empty");
		}
		List<Asset_Device_xref> assetToDeviceByDeviceList = assetDeviceXrefRepository
				.findByAssetID(assetReassignmentRequest.getNewAssetId(), can);
		Asset_Device_xref assetToDevice = null;
		if (assetToDeviceByDeviceList != null && assetToDeviceByDeviceList.size() > 0) {
			assetToDevice = assetToDeviceByDeviceList.get(0);
		}
		if (assetToDevice != null && assetReassignmentRequest.getNewAssetId() != null) {
			if (assetToDevice.getAsset().getAssignedName().equals(assetReassignmentRequest.getNewAssetId())
					&& assetToDevice.getDevice().getImei().equals(assetReassignmentRequest.getGatewayId()))
				return false;
		}

		return true;
	}

	private void unRegisterAssetId(String assetId, String can) {
		List<Asset_Device_xref> assetToDeviceByDeviceList = assetDeviceXrefRepository.findByAssetID(assetId, can);
		for (Asset_Device_xref asset_Device_xref : assetToDeviceByDeviceList) {
			assetDeviceXrefRepository.delete(asset_Device_xref);
		}
	}

	@Override
	public List<RyderApiDTO> ryderApi(String assetName, String imei, String imei_last5, String vin, String msgUuid) {
		LOGGER.info("msgUuid : " + msgUuid + " Inside ryderApi Method : ");
		LOGGER.info("msgUuid : " + msgUuid + " assetName : " + assetName + " imei : " + imei + " imei_last5 : "
				+ imei_last5 + " vin : " + vin);
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Asset_Device_xref> cq = cb.createQuery(Asset_Device_xref.class);
		Root<Asset_Device_xref> root = cq.from(Asset_Device_xref.class);
		cq.where(getRyderApiSpecification(assetName, imei, imei_last5, vin, root, cb));
		List<Asset_Device_xref> asset_Device_xrefs = entityManager.createQuery(cq).getResultList();
		LOGGER.info("msgUuid : " + msgUuid + " Successfully get data from database ");

		List<RyderApiDTO> resultList = new ArrayList<RyderApiDTO>();
		for (Asset_Device_xref assetToDevice : asset_Device_xrefs) {
			RyderApiDTO ryderApiDTO = new RyderApiDTO();
			if (assetToDevice.getAsset() != null) {
				if (assetToDevice.getAsset().getCategory() != null) {
					ryderApiDTO.setAssetType(assetToDevice.getAsset().getCategory().getValue());
				}
				if (assetToDevice.getAsset().getOrganisation() != null) {
					ryderApiDTO.setCustomerName(assetToDevice.getAsset().getOrganisation().getOrganisationName());
					ryderApiDTO.setCan(assetToDevice.getAsset().getOrganisation().getAccountNumber());
				}
				if (assetToDevice.getAsset().getManufacturerDetails() != null) {
					ryderApiDTO.setModel(assetToDevice.getAsset().getManufacturerDetails().getModel());
				}
				if (assetToDevice.getAsset().getManufacturer() != null) {
					ryderApiDTO.setMake(assetToDevice.getAsset().getManufacturer().getName());
				}
				ryderApiDTO.setAssetName(assetToDevice.getAsset().getAssignedName());
				ryderApiDTO.setYear(assetToDevice.getAsset().getYear());
				ryderApiDTO.setVin(assetToDevice.getAsset().getVin());
				ryderApiDTO.setAssetUuid(assetToDevice.getAsset().getUuid());

			}
			if (assetToDevice.getDateCreated() != null) {
				ryderApiDTO.setDateAdded(assetToDevice.getDateCreated().toString());
			}
			if (assetToDevice.getDevice() != null) {
				ryderApiDTO.setImei(assetToDevice.getDevice().getImei());
			}
			resultList.add(ryderApiDTO);
		}
		LOGGER.info("msgUuid : " + msgUuid + "Data return successfully");
		return resultList;
	}

	private Predicate getRyderApiSpecification(String assetName, String imei, String imei_last5, String vin,
			Root<Asset_Device_xref> root, CriteriaBuilder cb) {

		List<Predicate> predicates = new ArrayList<>();

		if (assetName != null && !assetName.isEmpty()) {
			predicates.add(cb.equal(cb.lower(root.get("asset").get("assignedName")), assetName.trim().toLowerCase()));
		}

		if (imei != null && !imei.isEmpty()) {
			predicates.add(cb.equal(cb.lower(root.get("device").get("imei")), imei.trim().toLowerCase()));
		}

		if (imei_last5 != null && !imei_last5.isEmpty()) {
			predicates.add(cb.equal(cb.substring(root.get("device").get("imei"), 11), imei_last5.trim().toLowerCase()));
		}

		if (vin != null && !vin.isEmpty()) {
			predicates.add(cb.equal(cb.lower(root.get("asset").get("vin")), vin.trim().toLowerCase()));
		}
		return cb.and(predicates.toArray(new Predicate[0]));

	}

	@Override
	public Map<String, Object> mailReport() {
		Map<String, Object> map = new HashMap<String, Object>();
		Instant instant = Instant.now();
		Instant previousDayInstant = instant.minus(Duration.ofDays(1)).truncatedTo(ChronoUnit.DAYS);
		//assetAssocationMailList
		List<Asset_Device_xref> assetToDeviceByDeviceList = assetDeviceXrefRepository.getAssetAssociationDetails(can);
	    map.put("assetAssocationMailList", assetToDeviceByDeviceList);
	    //processNonReportingAssets
	    Date previousDayDate = Date.from(previousDayInstant.atZone(ZoneId.systemDefault()).toInstant());
	    List<Asset_Device_xref> processNonReportingAssetsList = assetDeviceXrefRepository.findNonReportingAssets(can,previousDayDate);
	    map.put("processNonReportingAssets", processNonReportingAssetsList);
	    //processNonAssociatedReportingDevices
	    List<String> processNonAssociatedReportingDevices = assetDeviceXrefRepository.processNonAssociatedReportingDevices(can);
	    map.put("processNonAssociatedReportingDevices", processNonAssociatedReportingDevices);
	    //processDeviceHealthReport
	    List<DeviceHealthDTO> processDeviceHealthReport = getDevicesHealth(assetToDeviceByDeviceList);
	    map.put("processDeviceHealthReport", processDeviceHealthReport);	
	    return map;
	}
	
	public List<DeviceHealthDTO> getDevicesHealth(List<Asset_Device_xref> assetToDeviceByDeviceList) {
		LOGGER.info("service get-devices-health customer " + can);
		List<DeviceHealthDTO> deviceHealthList = new ArrayList<>();
		
		int totalAssetCount = assetToDeviceByDeviceList.size();
		
		LOGGER.info("service get-devices-health customer " + can + " assets found in DB " + totalAssetCount);
		List<String> redisValues;
		
		final int DEVICE_NAME_INDEX = 0;
		final int DEVICE_CONFIG_INDEX = 1;
		final int LAST_EVENT_INDEX = 2;
		final int LAST_EVENT_TIME_INDEX = 3;
		final int CURRENT_GPS_STATUS_INDEX = 4;
		final int CURRENT_BATTERY_POWER_INDEX = 5;
		final int CURRENT_PRIMARY_EXTERNAL_POWER_INDEX = 6;
		final int CURRENT_SECONDARY_EXTERNAL_POWER_INDEX = 7;
		final int MAINTENANCE_REPORT_TIME_INDEX = 8;
		final int BLUE_POWER_STATUS_REPORT_NUM = 10;
		final int BROWN_POWER_STATUS_REPORT_NUM = 12;
		final int GPS_LOCKED_DURING_INSTALLATION_STATUS_REPORT_NUM = 14;
		
		String[] redisKeysToFetch = new String[] {"deviceName", "configurationDescStr", "eventTypeStr", "dateRT", 
				"gpsStatusStr", "batteryPowerVFloat", "primaryExternalPowerVFloat", 
				"secondaryExternalPowerVFloat", "maintenanceReportDate", "bluePowerStatus", "bluePowerStatusReportNum",
				"brownPowerStatus", "brownPowerStatusReportNum", "gpsLockedDuringInstallationStatus", "gpsLockedDuringInstallationStatusReportNum"};
		List<String> redisKeysList = Arrays.asList(redisKeysToFetch);
		
		int count = 1;
		
		for (Asset_Device_xref asset : assetToDeviceByDeviceList) {

			try {
				if (asset.getDevice() != null) {

					String deviceId = asset.getDevice().getImei();
					DeviceHealthDTO deviceHealth = new DeviceHealthDTO();
					// Populating all the current values from Redis
					redisValues = redisDeviceRepository.findValuesForDevice(DEVICE_ID_PREFIX + deviceId, redisKeysList);

					if (redisValues != null && redisValues.size() > 0) {
						deviceHealth.setAssetId(redisValues.get(DEVICE_NAME_INDEX));
						deviceHealth.setDeviceConfig(redisValues.get(DEVICE_CONFIG_INDEX));
						deviceHealth.setLastEvent(redisValues.get(LAST_EVENT_INDEX));
						deviceHealth.setCurrentGpsStatus(redisValues.get(CURRENT_GPS_STATUS_INDEX));
						deviceHealth.setCurrentBatteryPowerV(redisValues.get(CURRENT_BATTERY_POWER_INDEX));
						deviceHealth.setCurrentMainPowerV(redisValues.get(CURRENT_PRIMARY_EXTERNAL_POWER_INDEX));
						deviceHealth.setCurrentAltPowerV(redisValues.get(CURRENT_SECONDARY_EXTERNAL_POWER_INDEX));
						deviceHealth.setLastEventRtc(redisValues.get(LAST_EVENT_TIME_INDEX));
						deviceHealth.setDateInstalled(asset.getDateCreated().toString());
						deviceHealth.setGatewayId(deviceId);

						// Checking if Maintenance Report for this device came after install time
						String maintenanceReportString = redisValues.get(MAINTENANCE_REPORT_TIME_INDEX);
						if (!StringUtils.isEmpty(maintenanceReportString)
								&& !StringUtils.isEmpty(asset.getDateCreated().toString())) {
							DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss");
							LocalDateTime maintenanceReportDateTime = LocalDateTime.parse(maintenanceReportString,
									formatter);
							LocalDateTime installationDateTime = LocalDateTime.parse(asset.getDateCreated().toString(),
									formatter);
							if (maintenanceReportDateTime.isAfter(installationDateTime)) {
								deviceHealth.setMaintenanceEventSeen("Y");
							} else {
								deviceHealth.setMaintenanceEventSeen("N");
							}
						} else {
							deviceHealth.setMaintenanceEventSeen("N");
						}

					}

					// Check if the values are already there for this asset in Redis
					if (!StringUtils.isEmpty(redisValues.get(BLUE_POWER_STATUS_REPORT_NUM))
							&& !StringUtils.isEmpty(redisValues.get(BROWN_POWER_STATUS_REPORT_NUM)) && !StringUtils
									.isEmpty(redisValues.get(GPS_LOCKED_DURING_INSTALLATION_STATUS_REPORT_NUM))) {
						deviceHealth.setBluePowerStatus("Y");
						deviceHealth.setBrownPowerStatus("Y");
						deviceHealth.setGpsLockStatus("Y");
						deviceHealth.setOverallStatus("Y");
					} else {

						LOGGER.info(
								"service get-devices-health Processing installation fields as not found in Redis for device: "
										+ deviceId);
						// Process and save in Redis so that it doesn't have to be calculated again

						// Get Latest Device QA Report for the device
						List<DeviceQa> latestDeviceQARecordList = deviceQARepository.findLatestDeviceQAByDeviceID(deviceId);

						// If QA Record Not found, we won't be able to get the fields which are
						// dependent on device QA time
						DeviceQa latestDeviceQARecord = null;
						if(latestDeviceQARecordList != null && latestDeviceQARecordList.size() > 0)
						{
							latestDeviceQARecord = latestDeviceQARecordList.get(0);
						}
						if (latestDeviceQARecord != null && latestDeviceQARecord.getQaDate() != null) {

							LOGGER.info("service get-devices-health DeviceQA Record found for device: " + deviceId);

							Timestamp deviceQATimestamp = latestDeviceQARecord.getQaDate();

							Map<String, String> redisStoreMap = new HashMap<>();

							// Get First Device Report having Main Power (Blue/ABS Power) > 11 after Device
							// QA
							if (StringUtils.isEmpty(redisValues.get(BLUE_POWER_STATUS_REPORT_NUM))) {
								DeviceDetails firstMainPowerReport = deviceRepository
										.findMainPowerReportAfterInstallation(deviceId, deviceQATimestamp);
								if (firstMainPowerReport != null) {
									deviceHealth.setBluePowerStatus("Y");
									redisStoreMap.put("bluePowerStatus", "true");
									redisStoreMap.put("bluePowerStatusReportNum",
											firstMainPowerReport.getReportId().toString());
								} else {
									deviceHealth.setBluePowerStatus("N");
								}
							} else {
								deviceHealth.setBluePowerStatus("Y");
							}

							// Get First Device Report having Alt Power (Bworn/Marker Power) > 11 after
							// Device QA
							if (StringUtils.isEmpty(redisValues.get(BROWN_POWER_STATUS_REPORT_NUM))) {
								DeviceDetails firstAltPowerReport = deviceRepository
										.findAltPowerReportAfterInstallation(deviceId, deviceQATimestamp);
								if (firstAltPowerReport != null) {
									deviceHealth.setBrownPowerStatus("Y");
									redisStoreMap.put("brownPowerStatus", "true");
									redisStoreMap.put("brownPowerStatusReportNum",
											firstAltPowerReport.getReportId().toString());
								} else {
									deviceHealth.setBrownPowerStatus("N");
								}
							} else {
								deviceHealth.setBrownPowerStatus("Y");
							}

							// Get First Device Report having GPS_Status = Locked after Device QA
							if (StringUtils
									.isEmpty(redisValues.get(GPS_LOCKED_DURING_INSTALLATION_STATUS_REPORT_NUM))) {
								DeviceDetails firstGPSLockedReport = deviceRepository
										.findGPSLockedReportAfterInstallation(deviceId, deviceQATimestamp);
								if (firstGPSLockedReport != null) {
									deviceHealth.setGpsLockStatus("Y");
									redisStoreMap.put("gpsLockedDuringInstallationStatus", "true");
									redisStoreMap.put("gpsLockedDuringInstallationStatusReportNum",
											firstGPSLockedReport.getReportId().toString());
								} else {
									deviceHealth.setGpsLockStatus("N");
								}
							} else {
								deviceHealth.setGpsLockStatus("Y");
							}

							// Checking for Overall Status (If Blue Power, Brown Power and GPS Locked
							// columns are Y, then overall, Y. Else, N
							if (!StringUtils.isEmpty(deviceHealth.getBluePowerStatus())
									&& deviceHealth.getBluePowerStatus().equalsIgnoreCase("Y")
									&& !StringUtils.isEmpty(deviceHealth.getBrownPowerStatus())
									&& deviceHealth.getBrownPowerStatus().equalsIgnoreCase("Y")
									&& !StringUtils.isEmpty(deviceHealth.getGpsLockStatus())
									&& deviceHealth.getGpsLockStatus().equalsIgnoreCase("Y")) {
								deviceHealth.setOverallStatus("Y");
							} else {
								deviceHealth.setOverallStatus("N");
							}

							if (!redisStoreMap.isEmpty()) {
								LOGGER.info("service get-devices-health Storing processed fields in Redis for device : "
										+ deviceId);
								redisDeviceRepository.add(DEVICE_ID_PREFIX + deviceId, redisStoreMap);
							}
						}
					}

					deviceHealthList.add(deviceHealth);
				}
				count++;
			} catch (Exception e) {
				if(asset.getDevice() != null)
				LOGGER.error("service get-devices-health Exception in processing device ID " + asset.getDevice().getImei()
						+ " error " + e.getMessage());
			}
		}
		
		return deviceHealthList;
	}
}

