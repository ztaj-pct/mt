package com.pct.device.service.impl;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import com.pct.common.constant.DeviceStatus;
import com.pct.common.constant.DeviceType;
import com.pct.common.constant.GatewayStatus;
import com.pct.common.constant.IOTType;
import com.pct.common.model.Asset_Device_xref;
import com.pct.common.model.AttributeValue;
import com.pct.common.model.Cellular;
import com.pct.common.model.Device;
import com.pct.common.model.Device_Device_xref;
import com.pct.common.model.Organisation;
import com.pct.common.model.SensorSection;
import com.pct.common.model.SensorSectionConfiguration;
import com.pct.common.model.User;
import com.pct.common.payload.DeviceForwardingRequest;
import com.pct.common.util.JwtUtil;
import com.pct.device.Bean.DeviceBean;
import com.pct.device.constant.KeyConstant;
import com.pct.device.dto.AttributeResponseDTO;
import com.pct.device.dto.BatteryResponseDTO;
import com.pct.device.dto.GatewayCheckDataDTO;
import com.pct.device.dto.GatewayUpdateDTO;
import com.pct.device.dto.MessageDTO;
import com.pct.device.dto.SensorDataDTO;
import com.pct.device.dto.SensorSectionDTO;
import com.pct.device.exception.DeviceException;
import com.pct.device.model.DeviceView;
import com.pct.device.model.GatewaySummary;
import com.pct.device.model.Lookup;
import com.pct.device.ms.repository.IDeviceViewMsRepository;
import com.pct.device.payload.AssetDetail;
import com.pct.device.payload.AssetsStatusPayload;
import com.pct.device.payload.BeaconDetailsRequest;
import com.pct.device.payload.BeaconPayload;
import com.pct.device.payload.GatewayBeanForMobileApp;
import com.pct.device.payload.GatewayBulkUploadRequest;
import com.pct.device.payload.GatewayBulkUploadRequestWithDeviceForwarding;
import com.pct.device.payload.GatewayDataCheckRequest;
import com.pct.device.payload.GatewayDetailPayLoad;
import com.pct.device.payload.GatewayPayload;
import com.pct.device.payload.GatewaySensorAssociation;
import com.pct.device.payload.GatewaySensorPayload;
import com.pct.device.payload.GatewaySummaryPayload;
import com.pct.device.payload.GatewayUploadRequest;
import com.pct.device.payload.SensorProduct;
import com.pct.device.payload.ShipmentDetailsRequest;
import com.pct.device.payload.UpdateGatewayRequest;
import com.pct.device.payload.UpdateMacAddressRequest;
import com.pct.device.repository.DeviceIgnoreForwardingRuleRepository;
import com.pct.device.repository.IAssetDeviceXrefRepository;
import com.pct.device.repository.IAttributeValueRepository;
import com.pct.device.repository.IDeviceDeviceXrefRepository;
import com.pct.device.repository.IDeviceRepository;
import com.pct.device.repository.IGatewaySummaryRepository;
import com.pct.device.repository.ILookupRepository;
import com.pct.device.repository.ISensorSectionConfigurationRepository;
import com.pct.device.repository.ISensorSectionRepository;
import com.pct.device.repository.RedisDeviceRepository;
import com.pct.device.repository.SensorDetailRepository;
import com.pct.device.service.DeviceForwardingService;
import com.pct.device.service.IGatewayService;
import com.pct.device.specification.DeviceViewSpecification;
import com.pct.device.specification.GatewaySpecification;
import com.pct.device.specification.SensorSpecification;
import com.pct.device.util.BeanConverter;
import com.pct.device.util.Constants;
import com.pct.device.util.RestUtils;

@Service
public class GatewayServiceImpl implements IGatewayService {

	Logger logger = LoggerFactory.getLogger(GatewayServiceImpl.class);

	private static final List<String> DEVICE_INFO_FIXED_FIELDS = new ArrayList() {
		{
			add(KeyConstant.CUSTOMER_ID);
			add(KeyConstant.DEVICE_TYPE);
		}
	};

	@Autowired
	private IDeviceRepository deviceRepository;

	@Autowired
	private IDeviceViewMsRepository deviceRepositoryViewRepository;

	@Autowired
	IAttributeValueRepository attributeValueRepository;

	@Autowired
	private BeanConverter beanConverter;

	@Autowired
	private RestUtils restUtils;

	@Autowired
	private IDeviceDeviceXrefRepository deviceDeviceXrefRespository;

	@Autowired
	private IAssetDeviceXrefRepository assetDeviceXrefRepository;

	@Autowired
	private SensorDetailRepository sensorDetailRepository;

	@Autowired
	private ISensorSectionRepository sensorSectionRepository;

	@Autowired
	private ISensorSectionConfigurationRepository sensorSectionConfigurationRepository;

	@Autowired
	private IGatewaySummaryRepository gatewaySummaryRepository;

	@Autowired
	RedisDeviceRepository redisDeviceRepository;

	@Autowired
	private JwtUtil JwtUtil;

	@Autowired
	private DeviceForwardingService deviceForwardingService;

	@Autowired
	private DeviceIgnoreForwardingRuleRepository deviceIgnoreForwardingRuleRepository;

	@Autowired
	private ILookupRepository assetConfigurationRepository;
	
	 @Autowired
     private AttributeValueServiceImpl attributeValueServiceImpl;
	
	private Stream<Device> filter;

	@Override
	public Boolean addGatewayDetail(GatewayUploadRequest gatewayUploadRequest, String userName) throws DeviceException {
		logger.info("Inside addGatewayDetail and fetching gatewayDetail and userId value",
				gatewayUploadRequest + " " + userName);
		String can = gatewayUploadRequest.getCan();
		Organisation company = restUtils.getCompanyFromCompanyService(can);
		User user = restUtils.getUserFromAuthService(userName);
		Device deviceMacAddress = deviceRepository.findByMac_address(gatewayUploadRequest.getMacAddress());
		String imei = gatewayUploadRequest.getImei();
		String macAddress = gatewayUploadRequest.getMacAddress();
		if (company != null) {
			if (imei != null && macAddress != null) {
				if (imei.length() == Constants.IMEI_LENGTH) {
					Device byImei = deviceRepository.findByImei(imei);
					if (byImei == null && deviceMacAddress == null) {
						Device device = new Device();
						device.setOrganisation(company);
						device.setProductCode(gatewayUploadRequest.getProductCode());
						device.setProductName(gatewayUploadRequest.getProductName());
						device.setSon(gatewayUploadRequest.getSon());
						device.setQuantityShipped(gatewayUploadRequest.getQuantityShipped());
						device.setStatus(DeviceStatus.PENDING);
						device.setCreatedAt(Instant.now());
						device.setEpicorOrderNumber(gatewayUploadRequest.getEpicorOrderNumber());
						device.setMacAddress(gatewayUploadRequest.getMacAddress());
						boolean isDeviceUuidUnique = false;
						String deviceUuid = "";
						while (!isDeviceUuidUnique) {
							deviceUuid = UUID.randomUUID().toString();
							Device byUuid = deviceRepository.findByUuid(deviceUuid);
							if (byUuid == null) {
								isDeviceUuidUnique = true;
							}
						}
						device.setIotType(IOTType.GATEWAY);
						device.setImei(gatewayUploadRequest.getImei());
						device.setCreatedBy(user);
						device.setCreatedAt(Instant.now());
						device.setUuid(deviceUuid);
						deviceRepository.save(device);
						logger.info("Gateway details saved successfully");

					} else {
						throw new DeviceException("IMEI/MAC Address already exist");
					}
				} else {
					throw new DeviceException("Invalid IMEI");
				}
			} else {
				throw new DeviceException("IMEI/MAC Address can not be null");
			}
		} else {
			throw new DeviceException("No company found for account number " + gatewayUploadRequest.getCan());
		}

		redisDeviceRepository.addMap(KeyConstant.DEVICE_CURRENT_VIEW_PREFIX + gatewayUploadRequest.getImei(),
				KeyConstant.CUSTOMER_ID, gatewayUploadRequest.getCan());
		if (gatewayUploadRequest.getProductName() != null) {
			redisDeviceRepository.addMap(KeyConstant.DEVICE_CURRENT_VIEW_PREFIX + gatewayUploadRequest.getImei(),
					KeyConstant.DEVICE_TYPE, gatewayUploadRequest.getProductName());
		}

		return Boolean.TRUE;
	}

	@Override
	public Page<GatewayPayload> getGatewayWithPagination(String accountNumber, String imei, String uuid,
			DeviceStatus status, Map<String, String> filterValues, String macAddress, Pageable pageable,
			String timeOfLastDownload) {
		logger.info("Inside getGatewayWithPagination service");
		Page<Device> deviceDetails = null;
		IOTType type = IOTType.getValue("Gateway");
		Instant lastDownloadeTime = null;
		if (timeOfLastDownload != null && !timeOfLastDownload.isEmpty()) {
			lastDownloadeTime = Instant.ofEpochMilli(Long.parseLong(timeOfLastDownload));
			logger.info("Last Downloaded Time: " + lastDownloadeTime + " Account Number: " + accountNumber);
		}
		Specification<Device> spc = GatewaySpecification.getGatewayListSpecification(accountNumber, imei, uuid, status,
				filterValues, type, macAddress, lastDownloadeTime);
		deviceDetails = deviceRepository.findAll(spc, pageable);
		logger.info("Fetching device details for specification");
		Page<GatewayPayload> gatewayRecordloadPage = null;
		if (deviceDetails.getSize() > 0) {
			gatewayRecordloadPage = beanConverter.convertGatewayToGatewayPayLoad(deviceDetails, pageable);
			logger.info("Fetching Gateway Details list" + gatewayRecordloadPage);
		} else {
			throw new DeviceException("No gateway details found for account number: " + accountNumber);
		}
		return gatewayRecordloadPage;
	}

	@Override
	public boolean deleteGatewayDetail(String can, String imei, String uuid) {
		logger.info("Inside deleteGatewayDetail");
		if (can != null && !can.isEmpty()) {
			Organisation company = restUtils.getCompanyFromCompanyService(can); // Company will be used when deleting
																				// Install history
			IOTType type = IOTType.getValue("Gateway");
			DeviceStatus dStatus = DeviceStatus.getGatewayStatusInSearch("PENDING");
			Specification<Device> spc = GatewaySpecification.getGatewaySpec(can, imei, uuid, type);

			List<Device> deviceList = deviceRepository.findAll(spc);
			logger.info("After fetching gateway details" + deviceList.toString());
			AtomicBoolean status = new AtomicBoolean(true);
			if (deviceList.size() > 0) {
				deviceList.forEach(device -> {
					if (!device.getStatus().equals(dStatus)) {
						throw new DeviceException("Device is not in pending state.");
					} else {
						status.set(status.get() && deleteGatewayData(device));
						logger.info("Gateway deleted successfully");
					}
				});
			} else {
				throw new DeviceException("No device found for imei/uuid/can number");
			}
			return status.get();
		} else {
			throw new DeviceException("Account number is mandatory");
		}
	}

	private Boolean heardDeleteGatewayData(Device device) {
		deviceRepository.delete(device);
		return true;

	}

	@Override
	public Boolean updateGatewayDetail(GatewayDetailPayLoad gatewayDetailPayload, String userName) {
		logger.info("Inside updateGatewayDetail and fetching gatewayDetail and userId value" + gatewayDetailPayload
				+ " " + userName);
		User user = restUtils.getUserFromAuthService(userName);
		Device device = deviceRepository.findByUuid(gatewayDetailPayload.getUuid());
		logger.info("Fetching gateway details for uuid : " + gatewayDetailPayload.getUuid());
		if (device != null) {
			device.setImei(gatewayDetailPayload.getImei());
			if (device.getDeviceDetails() != null) {
				device.getDeviceDetails().setAppVersion(gatewayDetailPayload.getAppVersion());
				device.getDeviceDetails().setBinVersion(gatewayDetailPayload.getBinVersion());
				device.getDeviceDetails().setBleVersion(gatewayDetailPayload.getBleVersion());
				device.getDeviceDetails().setConfig1Name(gatewayDetailPayload.getConfig1());
				device.getDeviceDetails().setConfig2Name(gatewayDetailPayload.getConfig2());
				device.getDeviceDetails().setConfig3Name(gatewayDetailPayload.getConfig3());
				device.getDeviceDetails().setConfig4Name(gatewayDetailPayload.getConfig4());
				device.getDeviceDetails().setMcuVersion(gatewayDetailPayload.getMcuVersion());
//				if (device.getDeviceMaintenanceDetails() != null) {
//				device.getDeviceMaintenanceDetails().setAppVersion(gatewayDetailPayload.getAppVersion());
//				device.getDeviceMaintenanceDetails().setBinVersion(gatewayDetailPayload.getBinVersion());
//				device.getDeviceMaintenanceDetails().setBleVersion(gatewayDetailPayload.getBleVersion());
//				device.getDeviceMaintenanceDetails().setConfig1Name(gatewayDetailPayload.getConfig1());
//				device.getDeviceMaintenanceDetails().setConfig2Name(gatewayDetailPayload.getConfig2());
//				device.getDeviceMaintenanceDetails().setConfig3Name(gatewayDetailPayload.getConfig3());
//				device.getDeviceMaintenanceDetails().setConfig4Name(gatewayDetailPayload.getConfig4());
//				device.getDeviceMaintenanceDetails().setMcuVersion(gatewayDetailPayload.getMcuVersion());
			}
			device.setProductCode(gatewayDetailPayload.getProductCode());
			device.setProductName(gatewayDetailPayload.getProductName());
			device.setSon(gatewayDetailPayload.getSon());
			device.setUpdatedAt(Instant.now());
			device.setUpdatedBy(user);
			device.setQuantityShipped(gatewayDetailPayload.getQuantityShipped());
			device.setEpicorOrderNumber(gatewayDetailPayload.getEpicorOrderNumber());
			device.setMacAddress(gatewayDetailPayload.getMacAddress());
			deviceRepository.save(device);
			logger.info("Gateway updated for the uuid : " + gatewayDetailPayload.getUuid());
		} else {
			throw new DeviceException("Gateway not found for the uuid " + gatewayDetailPayload.getUuid());
		}
		return Boolean.TRUE;
	}

	@Override
	public List<GatewayPayload> getGatewayDetails(String accountNumber, String uuid, String gatewayId) {
		logger.info("Inside getGatewayDetails service for account number " + accountNumber + " uuid " + uuid
				+ "gateway Id " + gatewayId);
		List<GatewayPayload> gatewayDetailList = new ArrayList<>();
		List<Device> deviceList = new ArrayList<Device>();
		IOTType type = IOTType.getValue("Gateway");
		Specification<Device> spc = SensorSpecification.getSensorSpec(accountNumber, uuid, type, gatewayId);
		logger.info("Fetching device details based on specification.");
		deviceList = deviceRepository.findAll(spc);
		if (deviceList.size() > 0) {
			deviceList.forEach(device -> {
				gatewayDetailList.add(beanConverter.convertGatewayPayloadToGatewayBean(device));
				;
			});
		} else {
			throw new DeviceException("No device found for uuid/can number");
		}
		return gatewayDetailList;
	}

	@Override
	@Transactional
	public Boolean associateGatewaySensor(GatewaySensorAssociation gatewaysensorRequest, String userName) {
		logger.info("Inside associateGatewaySensor for device uuid " + gatewaysensorRequest.getDeviceUuid());
		Device gateway = deviceRepository.findByUuid(gatewaysensorRequest.getDeviceUuid());
		if (gateway != null) {
			List<String> sensorList = gatewaysensorRequest.getSensorUuid();
			sensorList.forEach(sensorUuid -> {
				Device_Device_xref deviceXref = new Device_Device_xref();
				logger.info("Fetching device details for sensor uuid " + sensorUuid);
				Device sDevice = deviceRepository.findByUuid(sensorUuid);
				List<Device_Device_xref> sensorDevice = deviceDeviceXrefRespository.findBySensorUuid(sDevice);
				if (sensorDevice.size() == 0) {
					if (sDevice != null) {
						deviceXref.setDeviceUuid(gateway);
						deviceXref.setDateCreated(Instant.now());
						deviceXref.setActive(true);
						deviceXref.setSensorUuid(sDevice);
						deviceDeviceXrefRespository.save(deviceXref);
						logger.info("Sensor details mapped successfully for gateway: "
								+ gatewaysensorRequest.getDeviceUuid());
					}
				} else {
					throw new DeviceException("Sensor details are already mapped to a gateway.");
				}
			});
		} else {
			throw new DeviceException("Device not found");
		}
		return Boolean.TRUE;
	}

	@Override
	public List<GatewaySensorPayload> getGatewaySensorDetails(String imei, String deviceUuid) {
		logger.info("Inside getGatewaySensorDetails service for imei " + imei + " uuid " + deviceUuid);
		List<GatewaySensorPayload> gatewayDetailList = new ArrayList<GatewaySensorPayload>();
		GatewaySensorPayload gatewaySensorList = new GatewaySensorPayload();
		IOTType type = IOTType.getValue("Gateway");
		Specification<Device> spc = GatewaySpecification.getGatewaySensorSpec(imei, deviceUuid, type);
		logger.info("Fetching device details based on specification.");
		List<Device> deviceList = deviceRepository.findAll(spc);
		if (deviceList.size() > 0) {
			deviceList.forEach(device -> {
				Device dDevice = deviceRepository.findByUuid(device.getUuid());
				List<Device_Device_xref> deviceSensorXrefsList = deviceDeviceXrefRespository.findByDeviceUuid(dDevice);
				gatewayDetailList
						.add(beanConverter.convertGatewaySensorToGatewaySensorBean(device, deviceSensorXrefsList));
			});
		} else {
			throw new DeviceException("No device found for imei/uuid number");
		}
		return gatewayDetailList;
	}

	@Override
	public List<AssetsStatusPayload> getAssetsStatusDetails(String imei, String deviceUuid, String userName) {
		logger.info("Inside getGatewaySensorDetails service for imei " + imei + " uuid " + deviceUuid);
		User user = restUtils.getUserFromAuthService(userName);
		List<AssetsStatusPayload> assetsStatusList = new ArrayList<AssetsStatusPayload>();
		AssetsStatusPayload gatewaySensorList = new AssetsStatusPayload();
		IOTType type = IOTType.getValue("Gateway");
		Specification<Device> spc = GatewaySpecification.getGatewaySensorSpec(imei, deviceUuid, type);
		logger.info("Fetching device details based on specification.");
		List<Device> deviceList = deviceRepository.findAll(spc);
		if (deviceList.size() > 0) {
			deviceList.forEach(device -> {
				Device dDevice = deviceRepository.findByUuid(device.getUuid());
				Asset_Device_xref asset_Device_xref = assetDeviceXrefRepository.findByDevice(device.getImei());
				List<Device_Device_xref> deviceSensorXrefsList = deviceDeviceXrefRespository.findByDeviceUuid(dDevice);
				assetsStatusList.add(beanConverter.convertSensorDetailToAssetsStatusBean(device, deviceSensorXrefsList,
						asset_Device_xref, user));
			});
			
			 List<AttributeResponseDTO> atrvalue = attributeValueServiceImpl.getAttributeValueByGatewayDeviceId(imei);
			 List<BatteryResponseDTO> batteryresponse = new ArrayList<>();
			 for(AttributeResponseDTO att : atrvalue) {
			 BatteryResponseDTO battery = new BatteryResponseDTO();
			 battery.setPowerSource(att.getAttributeName());
			 battery.setVoltage(att.getAttributeValue());
			 batteryresponse.add(battery);	
			 }
			 gatewaySensorList.setBatteryData(batteryresponse);
			 if(assetsStatusList != null && assetsStatusList.size() > 0) {
				 (assetsStatusList.get(0)).setBatteryData(batteryresponse);
			 }			 
		} else {
			throw new DeviceException("No device found for imei/uuid number");
		}
		return assetsStatusList;
	}

	@Override
	public GatewayCheckDataDTO checkGatewayListData(GatewayDataCheckRequest gatewayDataCheckRequest) {
		GatewayCheckDataDTO gatewayCheckDataDTO = new GatewayCheckDataDTO();
		gatewayCheckDataDTO.setIsAllImei(false);
		gatewayCheckDataDTO.setIsAllMacAddress(false);
		gatewayCheckDataDTO.setStatus(false);
		String macReg = "^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$";
		String imeiReg = "^([0-9]{15})$";
		Pattern macPattern = Pattern.compile(macReg);
		Pattern imeiPattern = Pattern.compile(imeiReg);
		List<String> listOfImei = new ArrayList<String>();
		List<String> listOfMacAddress = new ArrayList<String>();
		List<String> gatewayListWhichPresentInDb = new ArrayList<String>();
		List<String> invalidIdsList = new ArrayList<String>();
		if (gatewayDataCheckRequest != null && gatewayDataCheckRequest.getIsProductIsApprovedForAsset() != null
				&& !"".equals(gatewayDataCheckRequest.getIsProductIsApprovedForAsset())
				&& gatewayDataCheckRequest.getDeviceList() != null
				&& gatewayDataCheckRequest.getDeviceList().size() > 0) {
			Boolean isAllImei = false;
			Boolean isAllMacAddress = false;
			Boolean isInvalidIds = false;
			for (String device : gatewayDataCheckRequest.getDeviceList()) {
				if (device != null && !"".equals(device)) {
					Matcher macMatcher = macPattern.matcher(device.trim());
					Matcher imeiMatcher = imeiPattern.matcher(device.trim());

					if (device.length() == 14) {
						device = "0" + device;
						imeiMatcher = imeiPattern.matcher(device.trim());
						if (checkImeiByLuhnAlgo(device)) {
							if (imeiMatcher.matches()) {
								listOfImei.add(device);
								logger.info("Check Value of : imeiMatcher and device id " + device);
							} else {
								isInvalidIds = true;
								invalidIdsList.add(device);
								logger.info("Check Value of : isInvalidIds and device id " + device);
							}
						} else {
							isInvalidIds = true;
							invalidIdsList.add(device);
							logger.info("Check Value of : isInvalidIds and device id " + device);
						}

					} else if (macMatcher.matches()) {
						listOfMacAddress.add(device);
						logger.info("Check Value of : macMatcher and device id " + device);
					} else if (imeiMatcher.matches()) {
						listOfImei.add(device);
						logger.info("Check Value of : imeiMatcher and device id " + device);
					} else {
						isInvalidIds = true;
						invalidIdsList.add(device);
						logger.info("Check Value of : isInvalidIds and device id " + device);
					}
				}
			}

			if (listOfMacAddress.size() == gatewayDataCheckRequest.getDeviceList().size()) {
				isAllMacAddress = true;
			} else if (listOfImei.size() == gatewayDataCheckRequest.getDeviceList().size()) {
				isAllImei = true;
			}
			gatewayCheckDataDTO.setIsAllImei(isAllImei);
			gatewayCheckDataDTO.setIsAllMacAddress(isAllMacAddress);

			if (isAllMacAddress) {
				if (gatewayDataCheckRequest.getIsProductIsApprovedForAsset().equals("SmartPair")) {
					gatewayCheckDataDTO.setStatus(true);
					gatewayListWhichPresentInDb = checkGatewayPresentInDbOrNot(gatewayDataCheckRequest.getDeviceList(),
							false, isAllMacAddress);
					gatewayCheckDataDTO.setGatewayList(gatewayDataCheckRequest.getDeviceList());
				} else {
					gatewayCheckDataDTO.setGatewayList(listOfMacAddress);
				}
			} else if (isAllImei) {
				if (!gatewayDataCheckRequest.getIsProductIsApprovedForAsset().equals("SmartPair")) {
					gatewayCheckDataDTO.setStatus(true);
					gatewayListWhichPresentInDb = checkGatewayPresentInDbOrNot(listOfImei, isAllImei, false);
					gatewayCheckDataDTO.setGatewayList(listOfImei);
				} else {
					gatewayCheckDataDTO.setGatewayList(listOfImei);
				}
			} else {
				if (gatewayDataCheckRequest.getIsProductIsApprovedForAsset().equals("SmartPair")) {
					gatewayCheckDataDTO.setGatewayList(listOfImei);
					gatewayCheckDataDTO.setInvalidIdsList(listOfImei);
				} else {
					gatewayCheckDataDTO.setGatewayList(listOfMacAddress);
					gatewayCheckDataDTO.setInvalidIdsList(listOfMacAddress);
				}

			}
//			if(deviceRepository.existBySimno((String)device.get("imei"), sim)) {
//				 dto.setStatus(true);
//				 dto.setMessage("The following records have existing SIM numbers. They will be updated with the details in the file.");
//			 }
//			 
//			 
//			 if(deviceRepository.existBySerialNumber((String)device.get("imei"), serial)) {
//				 dto.setStatus(true);
//				 dto.setMessage("The following records have existing serial numbers. They will be updated with the details in the file.");
//			 }


			if (isInvalidIds) {
				gatewayCheckDataDTO.setInvalidIdsList(invalidIdsList);
			}

			gatewayCheckDataDTO.setGatewayListWhichPresentInDb(gatewayListWhichPresentInDb);
			gatewayCheckDataDTO.setIsInvalidIds(isInvalidIds);
			
			if(gatewayDataCheckRequest.getDeviceDetails() != null && !gatewayDataCheckRequest.getDeviceDetails().isEmpty()) {
				List<String>  simNoAlreadyExist = new ArrayList<>();
				List<String> serialNoAlreadyExist = new ArrayList<>();
				for (Map<String, Object> device : gatewayDataCheckRequest.getDeviceDetails()) {
	
					String deviceid = (String) device.get("imei");
	
					
					String serial = (String) device.get("serial");
					String sim = (String) device.get("sim");
					
					if(deviceRepository.existBySimno((String)device.get("imei"), sim)) {
						simNoAlreadyExist.add(deviceid);
					 }
					 
					 
					 if(deviceRepository.existBySerialNumber((String)device.get("imei"), serial)) {
						 serialNoAlreadyExist.add(deviceid);
					 }
					 
				}
				gatewayCheckDataDTO.setSerialNoAlreadyExist(serialNoAlreadyExist);
				gatewayCheckDataDTO.setSimNoAlreadyExist(simNoAlreadyExist);
			}
		}
		return gatewayCheckDataDTO;
	}

	// Returns check digit for 14 digit IMEI prefix
	public int getCheckDigit(String imeiPrefix) {
		int sum = 0;
		for (int i = 13; i >= 0; i = i - 1) {
			String sDigit = imeiPrefix.substring(i, i + 1);
			int digit = Integer.valueOf(sDigit);
			if (i % 2 == 0) {
				sum = sum + digit;
			} else {
				sum = sum + sumOfDigits(digit * 2);
			}
		}
		sum = sum * 9;
		return sum % 10; // Return check digit
	}

	// Calculate sum of digits for a number
	public int sumOfDigits(int number) {
		int sum = 0;
		while (number > 0) {
			sum += number % 10;
			number = number / 10;
		}
		return sum;
	}

	public Boolean checkImeiByLuhnAlgo(String imei) {
		Boolean isImei = false;

		if (imei != null && imei != "" && imei.length() == 15) {
			int computedCheckDigit = getCheckDigit(imei.substring(0, 14));
			int checkDigitInSource = Integer.valueOf(imei.substring(14));

			if (computedCheckDigit == checkDigitInSource) {
				isImei = true;
				System.out.println(imei + " is a valid IMEI number!");
			} else {
				System.out.println(imei + " is NOT a valid IMEI number!");
				System.out.println("Check digit computed: " + computedCheckDigit);
			}
		}
		return isImei;
	}

	private List<String> checkGatewayPresentInDbOrNot(List<String> gatewayList, Boolean isAllImei,
			Boolean isAllMacAddress) {
		List<String> gatewayListWhichPresentInDb = new ArrayList<String>();

		for (String device : gatewayList) {
			Device gateway = null;
			if (isAllImei) {
				gateway = deviceRepository.findByImei(device);
				if (gateway != null) {
					gatewayListWhichPresentInDb.add(device);
				}
			} else if (isAllMacAddress) {
				gateway = deviceRepository.findByMac_address(device);
				if (gateway != null) {
					gatewayListWhichPresentInDb.add(device);
				}
			}

		}
		return gatewayListWhichPresentInDb;

	}

	@Transactional
	@Override
	public Boolean uploadBulkGateway(GatewayBulkUploadRequest gatewayBulkUploadRequest, String userName) {
		Boolean flag = false;
		logger.info("Inside uploadBulkGateway", gatewayBulkUploadRequest);
		User user = restUtils.getUserFromAuthService(userName);
		Organisation company = restUtils
				.getCompanyFromCompanyService(gatewayBulkUploadRequest.getCompany().getAccountNumber());
		List<Device> deviceList = new ArrayList<>();
		for (Map<String, Object> device : gatewayBulkUploadRequest.getGatewayList()) {
			Device gatwayDevice = null;
			if (gatewayBulkUploadRequest.getIsAllImei()) {
				gatwayDevice = deviceRepository.findByImei(String.valueOf(device.get("imei")));
				if (gatwayDevice == null) {
					deviceList.add(
							beanConverter.gatewayPayloadTogateway(device, gatewayBulkUploadRequest, user, company));
				}
			} else if (gatewayBulkUploadRequest.getIsAllMacAddress()) {
				gatwayDevice = deviceRepository.findByMac_address(String.valueOf(device.get("imei")));
				if (gatwayDevice == null) {
					deviceList.add(
							beanConverter.gatewayPayloadTogateway(device, gatewayBulkUploadRequest, user, company));
				}
			}

		}
		if (deviceList != null && deviceList.size() > 0) {
			deviceRepository.saveAll(deviceList);
			for (Device device : deviceList) {
				redisDeviceRepository.addMap(KeyConstant.DEVICE_CURRENT_VIEW_PREFIX + device.getImei(),
						KeyConstant.CUSTOMER_ID, device.getOrganisation().getAccountNumber());
				if (device.getIotType() != null) {
					redisDeviceRepository.addMap(KeyConstant.DEVICE_CURRENT_VIEW_PREFIX + device.getImei(),
							KeyConstant.DEVICE_TYPE, device.getProductName());
				}
			}
			flag = true;
		} else {
			throw new DeviceException("All devices(s) present in database");
		}
		return flag;
	}

	@Override
	@Transactional
	public Boolean bulkDeleteForGateways(List<String> listOfUuid) {
		Boolean flag = false;
		if (listOfUuid != null && listOfUuid.size() > 0) {
			try {
				for (String uuid : listOfUuid) {
					Device gateway = deviceRepository.findByUuid(uuid);
					if (gateway != null && gateway.getStatus().name().equals(GatewayStatus.PENDING.name())) {
						List<Device_Device_xref> gatewaySensorXrefList = null;
						if (gateway.getIotType() != null
								&& IOTType.GATEWAY.equals(IOTType.getValue(gateway.getIotType().toString()))) {
							gatewaySensorXrefList = deviceDeviceXrefRespository.findByDeviceUuid(gateway);
						} else {
							gatewaySensorXrefList = deviceDeviceXrefRespository.findBySensorUuid(gateway);
						}

						if (gatewaySensorXrefList != null && !gatewaySensorXrefList.isEmpty()) {
							deviceDeviceXrefRespository.deleteAll(gatewaySensorXrefList);
						}

						if (gateway.getSensorDetail() != null && !gateway.getSensorDetail().isEmpty()) {
							sensorDetailRepository.deleteAll(gateway.getSensorDetail());
						}

						List<Asset_Device_xref> asseDeviceXrefsList = assetDeviceXrefRepository
								.findAllByDevice(gateway.getUuid());
						if (asseDeviceXrefsList != null && !asseDeviceXrefsList.isEmpty()) {
							assetDeviceXrefRepository.deleteAll(asseDeviceXrefsList);
						}

						// if(gateway.getImei() != null) {
						// List<AttributeValue> attributvalue =
						// attributeValueRepository.findByDeviceImei(gateway.getImei());
						// if(attributvalue != null && attributvalue.size() > 0) {
						// attributvalue.forEach(attribteval -> {
						// attributeValueRepository.delete(attribteval);
						// });
						// }
						// }
						gateway.setIsDeleted(Boolean.TRUE);
						gateway.setLastPerformAction(Constants.GATEWAYS_ACTION_DELETED);
						gateway.setTimeOfLastDownload(Instant.now());
						deviceRepository.save(gateway);
						// gatewayRepository.delete(gateway);
						flag = true;
					}

				}
			} catch (Exception e) {
				flag = false;
				logger.error(e.getMessage());
			}
		}
		return flag;
	}

	@Override
	public Boolean updateGateway(UpdateGatewayRequest updateGatewayRequest, String userName) {
		Boolean status = false;
		User user = restUtils.getUserFromAuthService(userName);
		if (updateGatewayRequest != null && updateGatewayRequest.getImeiList() != null
				&& !updateGatewayRequest.getImeiList().isEmpty() && updateGatewayRequest.isStandardConfiguration()
				&& updateGatewayRequest.getSectionId() != null && updateGatewayRequest.getSectionId() != "") {
			List<SensorSectionConfiguration> listOfSensorSectionConfiguration = sensorSectionConfigurationRepository
					.findBySection(updateGatewayRequest.getSectionId());
			status = replaceExistingConfiguration(updateGatewayRequest, listOfSensorSectionConfiguration, user);
		} else if (updateGatewayRequest != null && updateGatewayRequest.getImeiList() != null
				&& !updateGatewayRequest.getImeiList().isEmpty() && !updateGatewayRequest.isStandardConfiguration()
				&& updateGatewayRequest.getSensorList() != null && !updateGatewayRequest.getSensorList().isEmpty()) {
			if (updateGatewayRequest.isReplaceExistingConfiguration()) {
				status = replaceExistingConfiguration(updateGatewayRequest, user);
			} else {
				status = addToExistingConfiguration(updateGatewayRequest, user);
			}
		} else {
			throw new DeviceException("Not valid Request");
		}

		return status;
	}

	private Boolean addToExistingConfiguration(UpdateGatewayRequest updateGatewayRequest, User user) {
		Boolean status = false;
		for (String imei : updateGatewayRequest.getImeiList()) {
			if (imei != null && !imei.equals("")) {
				try {
					Device gateway = deviceRepository.findByImei(imei);
					List<Device_Device_xref> deviceDeviceXrefsList = deviceDeviceXrefRespository
							.findByDeviceUuid(gateway);
					if (gateway != null && gateway.getStatus().name().equals(GatewayStatus.PENDING.name())) {
						if (updateGatewayRequest.getSensorList() != null
								&& !updateGatewayRequest.getSensorList().isEmpty()) {
							for (SensorDataDTO sensorProduct : updateGatewayRequest.getSensorList()) {
								Boolean isAlreadyAttachedWithDevice = false;
								if (deviceDeviceXrefsList != null && !deviceDeviceXrefsList.isEmpty()) {
									for (Device_Device_xref deviceDeviceXref : deviceDeviceXrefsList) {
										 if (sensorProduct.getProductCode()
										 .equalsIgnoreCase(deviceDeviceXref.getDeviceUuid().getProductCode())) {
										if (sensorProduct.getProductCode().equalsIgnoreCase("77-S177")) {
											gateway.setUpdatedOn(Instant.now());
											gateway.setUpdatedBy(user);
											gateway.setProductName(sensorProduct.getProductName());
											gateway = deviceRepository.save(gateway);
											gateway.setProductName(sensorProduct.getProductName());

										}
										isAlreadyAttachedWithDevice = true;
										break;
									  }
									}
								}
								if (!isAlreadyAttachedWithDevice) {
									Device sensor = new Device();
									sensor.setStatus(DeviceStatus.PENDING);
									sensor.setProductCode(sensorProduct.getProductCode());
									sensor.setProductName(sensorProduct.getProductName());
									sensor.setCreatedAt(Instant.now());
									sensor.setSon(gateway.getSon());
									sensor.setOrganisation(gateway.getOrganisation());
									sensor.setIotType(IOTType.SENSOR);

									sensor.setCreatedBy(user);
									sensor.setCreatedAt(Instant.now());

									boolean isSensorUuidUnique = false;
									String sensorUuid = "";
									while (!isSensorUuidUnique) {
										sensorUuid = UUID.randomUUID().toString();
										Device byUuid = deviceRepository.findByUuid(sensorUuid);
										if (byUuid == null) {
											isSensorUuidUnique = true;
										}
									}
									sensor.setUuid(sensorUuid);
									sensor = deviceRepository.save(sensor);
									Device_Device_xref gatewaySensorXref = new Device_Device_xref();
									gatewaySensorXref.setActive(true);
									gatewaySensorXref.setDateCreated(Instant.now());
									gatewaySensorXref.setDeviceUuid(gateway);
									gatewaySensorXref.setSensorUuid(sensor);
									gatewaySensorXref = deviceDeviceXrefRespository.save(gatewaySensorXref);
								}

							}
							status = true;
						}

						gateway.setUpdatedBy(user);
						gateway.setUpdatedAt(Instant.now());
						gateway.setLastPerformAction(Constants.GATEWAYS_ACTION_UPDATED);
						gateway.setTimeOfLastDownload(Instant.now());
						deviceRepository.save(gateway);
					}
				} catch (Exception e) {
					status = false;
					throw new DeviceException("Getting Error while updating device(s)");
				}
			}

		}
		return status;

	}

	private Boolean replaceExistingConfiguration(UpdateGatewayRequest updateGatewayRequest, User user) {
		Boolean status = false;
		for (String imei : updateGatewayRequest.getImeiList()) {
			if (imei != null && !imei.equals("")) {
				try {
					Device gateway = deviceRepository.findByImei(imei);
					if (gateway != null && gateway.getStatus().name().equals(GatewayStatus.PENDING.name())) {
						Boolean isDeleted = deleteGatewaySensor(gateway.getUuid());
						if (isDeleted) {
							if (updateGatewayRequest.getSensorList() != null
									&& !updateGatewayRequest.getSensorList().isEmpty()) {
								for (SensorDataDTO sensorProduct : updateGatewayRequest.getSensorList()) {
									Device sensor = new Device();
									sensor.setStatus(DeviceStatus.PENDING);
									sensor.setProductCode(sensorProduct.getProductCode());
									sensor.setProductName(sensorProduct.getProductName());
									sensor.setCreatedAt(Instant.now());
									sensor.setSon(gateway.getSon());
									sensor.setOrganisation(gateway.getOrganisation());
									sensor.setIotType(IOTType.SENSOR);

									sensor.setCreatedBy(user);
									sensor.setCreatedAt(Instant.now());

									boolean isSensorUuidUnique = false;
									String sensorUuid = "";
									while (!isSensorUuidUnique) {
										sensorUuid = UUID.randomUUID().toString();
										Device byUuid = deviceRepository.findByUuid(sensorUuid);
										if (byUuid == null) {
											isSensorUuidUnique = true;
										}
									}
									sensor.setUuid(sensorUuid);
									sensor = deviceRepository.save(sensor);
									Device_Device_xref gatewaySensorXref = new Device_Device_xref();
									gatewaySensorXref.setDateCreated(Instant.now());
									gatewaySensorXref.setActive(true);
									gatewaySensorXref.setDateCreated(Instant.now());
									gatewaySensorXref.setDeviceUuid(gateway);
									gatewaySensorXref.setSensorUuid(sensor);
									gatewaySensorXref = deviceDeviceXrefRespository.save(gatewaySensorXref);
								}
								status = true;
							}
						}
						gateway.setUpdatedBy(user);
						gateway.setUpdatedAt(Instant.now());
						gateway.setLastPerformAction(Constants.GATEWAYS_ACTION_UPDATED);
						gateway.setTimeOfLastDownload(Instant.now());
						deviceRepository.save(gateway);
					}
				} catch (Exception e) {
					status = false;
					throw new DeviceException("Getting Error while updating device(s)");
				}
			}

		}
		return status;
	}

	private Boolean deleteGatewaySensor(String uuid) {
		Boolean flag = false;
		if (uuid != null) {
			try {
				Device gateway = deviceRepository.findByUuid(uuid);
				if (gateway != null && gateway.getStatus().name().equals(GatewayStatus.PENDING.name())) {
					List<Device_Device_xref> gatewaySensorXrefList = null;
					if (gateway.getIotType() != null
							&& IOTType.GATEWAY.equals(IOTType.getValue(gateway.getIotType().toString()))) {
						gatewaySensorXrefList = deviceDeviceXrefRespository.findByDeviceUuid(gateway);
					} else {
						gatewaySensorXrefList = deviceDeviceXrefRespository.findBySensorUuid(gateway);
					}

					if (gatewaySensorXrefList != null && !gatewaySensorXrefList.isEmpty()) {
						deviceDeviceXrefRespository.deleteAll(gatewaySensorXrefList);
					}

					if (gateway.getSensorDetail() != null && !gateway.getSensorDetail().isEmpty()) {
						sensorDetailRepository.deleteAll(gateway.getSensorDetail());
					}

					List<Asset_Device_xref> asseDeviceXrefsList = assetDeviceXrefRepository
							.findAllByDevice(gateway.getUuid());
					if (asseDeviceXrefsList != null && !asseDeviceXrefsList.isEmpty()) {
						assetDeviceXrefRepository.deleteAll(asseDeviceXrefsList);
					}

					// deviceRepository.delete(gateway);
				}
				flag = true;
			} catch (Exception e) {
				flag = false;
				logger.error(e.getMessage());
			}
		}
		return flag;
	}

	private Boolean replaceExistingConfiguration(UpdateGatewayRequest updateGatewayRequest,
			List<SensorSectionConfiguration> listOfCompanySensorConfiguration, User user) {
		Boolean status = false;
		for (String imei : updateGatewayRequest.getImeiList()) {
			if (imei != null && !imei.equals("")) {
				try {
					Device gateway = deviceRepository.findByImei(imei);
					if (gateway != null && gateway.getStatus().name().equals(GatewayStatus.PENDING.name())) {
						Boolean isDeleted = deleteGatewaySensor(gateway.getUuid());
						gateway = deviceRepository.findByImei(imei);
						if (isDeleted) {
							if (listOfCompanySensorConfiguration != null
									&& !listOfCompanySensorConfiguration.isEmpty()) {
								for (SensorSectionConfiguration sensorSectionConfiguration : listOfCompanySensorConfiguration) {
									Device sensor = new Device();
									sensor.setProductCode(sensorSectionConfiguration.getProductCode());
									sensor.setProductName(sensorSectionConfiguration.getProductName());
									sensor.setCreatedAt(Instant.now());
									sensor.setStatus(DeviceStatus.PENDING);
									sensor.setCreatedAt(Instant.now());
									sensor.setSon(gateway.getSon());
									sensor.setIotType(IOTType.SENSOR);

									sensor.setCreatedBy(user);
									sensor.setCreatedAt(Instant.now());

									sensor.setOrganisation(gateway.getOrganisation());
									boolean isSensorUuidUnique = false;
									String sensorUuid = "";
									while (!isSensorUuidUnique) {
										sensorUuid = UUID.randomUUID().toString();
										Device byUuid = deviceRepository.findByUuid(sensorUuid);
										if (byUuid == null) {
											isSensorUuidUnique = true;
										}
									}
									sensor.setUuid(sensorUuid);
									sensor = deviceRepository.save(sensor);
									Device_Device_xref gatewaySensorXref = new Device_Device_xref();
									gatewaySensorXref.setDateCreated(Instant.now());
									gatewaySensorXref.setActive(true);
									gatewaySensorXref.setDateCreated(Instant.now());
									gatewaySensorXref.setDeviceUuid(gateway);
									gatewaySensorXref.setSensorUuid(sensor);
									gatewaySensorXref = deviceDeviceXrefRespository.save(gatewaySensorXref);
								}
								status = true;
							}
						}
						gateway.setUpdatedBy(user);
						gateway.setUpdatedAt(Instant.now());
						gateway.setLastPerformAction(Constants.GATEWAYS_ACTION_UPDATED);
						gateway.setTimeOfLastDownload(Instant.now());
						deviceRepository.save(gateway);
					}
				} catch (Exception e) {
					status = false;
					throw new DeviceException("Getting Error while updating device(s)");
				}
			}

		}
		return status;
	}

	@Override
	public List<SensorSectionDTO> getSensorSectionList() {
		List<SensorSection> listOfSensorSection = sensorSectionRepository.findAll();
		List<SensorSectionDTO> listOfSensorSectionDTO = new ArrayList<>();
		for (SensorSection sensorSection : listOfSensorSection) {
			SensorSectionDTO sensorSectionDTO = new SensorSectionDTO();
			sensorSectionDTO.setSectionName(sensorSection.getSectionName());
			sensorSectionDTO.setSectionId(sensorSection.getUuid());
			listOfSensorSectionDTO.add(sensorSectionDTO);
		}
		return listOfSensorSectionDTO;
	}

	@Override
	public Page<GatewaySummaryPayload> getCustomerGatewaySummary(Pageable pageable, String userName,
			Map<String, String> filterValues) {

		Page<GatewaySummary> customerGatewaySummary = null;
		User user = restUtils.getUserFromAuthService(userName);

		Specification<GatewaySummary> spc = GatewaySpecification.getSpecificationForGatewaySummary(filterValues, user);
		customerGatewaySummary = gatewaySummaryRepository.findAll(spc, pageable);

		Page<GatewaySummaryPayload> gatewaySummaryPayloadPage = beanConverter
				.convertGatewaySummaryToGatewaySummaryPayload(customerGatewaySummary, pageable);

		return gatewaySummaryPayloadPage;

	}

	@Override
	public void getDeviceDetailsForCsv(Page<GatewaySummaryPayload> gatewaySummaryPayloadPage,
			HttpServletResponse response) throws IOException {
		// TODO Auto-generated method stub
		restUtils.writeFilterCSVFileFromDeviceSummary("gatewaySummary", gatewaySummaryPayloadPage, response);
	}

	@Transactional
	@Override
	public Boolean uploadBulkGatewayWithDeviceForwarding(
			GatewayBulkUploadRequestWithDeviceForwarding gatewayBulkUploadRequestWithDeviceForwarding,
			String userName) {
		Boolean flag = false;
		logger.info("Inside uploadBulkGatewayWithDeviceForwarding", gatewayBulkUploadRequestWithDeviceForwarding);
		User user = restUtils.getUserFromAuthService(userName);
		Organisation company = restUtils.getCompanyFromCompanyService(
				gatewayBulkUploadRequestWithDeviceForwarding.getCompany().getAccountNumber());
		Organisation purchasedBy = restUtils
				.getCompanyById(gatewayBulkUploadRequestWithDeviceForwarding.getPurchasedBy().getId());
		List<Device> deviceList = new ArrayList<>();
		for (Map<String, Object> device : gatewayBulkUploadRequestWithDeviceForwarding.getGatewayList()) {
			Device gatwayDevice = null;
			if (gatewayBulkUploadRequestWithDeviceForwarding.getIsAllImei()) {
				gatwayDevice = deviceRepository.findByImei(String.valueOf(device.get("imei")));
				if (gatwayDevice == null) {
					deviceList.add(beanConverter.gatewayPayloadTogateway(device,
							gatewayBulkUploadRequestWithDeviceForwarding, user, company));
				}
			} else if (gatewayBulkUploadRequestWithDeviceForwarding.getIsAllMacAddress()) {
				gatwayDevice = deviceRepository.findByMac_address(String.valueOf(device.get("imei")));
				if (gatwayDevice == null) {
					deviceList.add(beanConverter.gatewayPayloadTogateway(device,
							gatewayBulkUploadRequestWithDeviceForwarding, user, company));
				}
			}

		}
		if (deviceList != null && deviceList.size() > 0) {
			deviceRepository.saveAll(deviceList);
			for (Device device : deviceList) {
				redisDeviceRepository.addMap(KeyConstant.DEVICE_CURRENT_VIEW_PREFIX + device.getImei(),
						KeyConstant.CUSTOMER_ID, device.getOrganisation().getAccountNumber());

				if (purchasedBy != null) {
					redisDeviceRepository.addMap(KeyConstant.DEVICE_CURRENT_VIEW_PREFIX + device.getImei(),
							KeyConstant.PURCHASED_BY, purchasedBy.getAccountNumber());
				}

				if (device.getIotType() != null) {
					redisDeviceRepository.addMap(KeyConstant.DEVICE_CURRENT_VIEW_PREFIX + device.getImei(),
							KeyConstant.DEVICE_TYPE, device.getProductName());
				}

				if (gatewayBulkUploadRequestWithDeviceForwarding.getIsAllImei()) {
					DeviceForwardingRequest deviceForwardingRequest = new DeviceForwardingRequest();
					deviceForwardingRequest
							.setForwardingList(gatewayBulkUploadRequestWithDeviceForwarding.getForwardingList());
					deviceForwardingRequest.setDeviceId(device.getImei());
					try {
						Boolean isSaved = deviceForwardingService.addDeviceForwarding(deviceForwardingRequest,
								userName);
						logger.info("Device forwarding saved successfully for device id : " + device.getImei()
								+ "status : " + isSaved);
					} catch (Exception e) {
						logger.error(
								"Exception occured while saving device forwarding for device id : " + device.getImei());
						logger.error("Exception is : " + e.getMessage());
					}
				}
			}
			deviceForwardingService.saveExcludedCustomerForwardingRules(
					gatewayBulkUploadRequestWithDeviceForwarding.getIgnoreForwardingRules(), deviceList, company,
					purchasedBy, user);
			deviceForwardingService.updateExcludeCustomerFrowardingRulesOnRedis(
					gatewayBulkUploadRequestWithDeviceForwarding.getIgnoreForwardingRules(), deviceList);
			flag = true;
		} else {
			throw new DeviceException("All devices(s) present in database");
		}
		return flag;
	}

	@Transactional
	@Override
	public List<GatewayUpdateDTO> uploadBulkGateway(
			GatewayBulkUploadRequestWithDeviceForwarding gatewayBulkUploadRequestWithDeviceForwarding,
			String userName) {
		logger.info("Inside uploadBulkGatewayWithDeviceForwarding", gatewayBulkUploadRequestWithDeviceForwarding);

		User user = restUtils.getUserFromAuthService(userName);

		Organisation company = restUtils.getCompanyFromCompanyService(
				gatewayBulkUploadRequestWithDeviceForwarding.getCompany().getAccountNumber());

		Organisation purchasedBy = restUtils
				.getCompanyById(gatewayBulkUploadRequestWithDeviceForwarding.getPurchasedBy().getId());

		List<Device> devices = new ArrayList<>();

		List<GatewayUpdateDTO> gatewayUpdateDTOs = new ArrayList<>();

		for (Map<String, Object> device : gatewayBulkUploadRequestWithDeviceForwarding.getGatewayList()) {

			GatewayUpdateDTO dto = new GatewayUpdateDTO((String) device.get("imei"));

			Device gatwayDevice = null;

			if (gatewayBulkUploadRequestWithDeviceForwarding.getIsAllImei()) {
				gatwayDevice = deviceRepository.findByImei(String.valueOf(device.get("imei")));
			} else if (gatewayBulkUploadRequestWithDeviceForwarding.getIsAllMacAddress()) {
				gatwayDevice = deviceRepository.findByMac_address(String.valueOf(device.get("imei")));
			}

			if (gatwayDevice == null) {
				dto.setStatus(false);
				dto.setMessage("Device is not exist in db");
			}

//			else if(gatwayDevice.getOrganisation() != null) {
//				if(!gatwayDevice.getOrganisation().getAccountNumber().equals(company.getAccountNumber())) {
//					dto.setStatus(false);
//					dto.setMessage("Device end customer is not valid");
//				}
//			}
			String serial = (String)device.get("serial");
			String sim = (String)device.get("sim");
			
			Pattern pattern = Pattern.compile("^[0-9]+$");
			
			
			if (sim == null || sim.isEmpty() || sim.equalsIgnoreCase("null")) {
				dto.setStatus(false);
				dto.setMessage("The following records are missing SIM numbers. Please check the file and try again.");
			}
			
			if (serial == null || serial.isEmpty() || serial.equalsIgnoreCase("null")) {
				dto.setStatus(false);
				dto.setMessage(
						"The following records are missing serial numbers. Please check the file and try again.");
			}
			
			if (sim.length() < 18 || sim.length() > 22 || !pattern.matcher(sim).matches()) {
				dto.setStatus(false);
				dto.setMessage(
						"The following records have invalid SIM numbers. Please check that these values are numeric and between 18 and 22 digits long and try again.");
			}
			
			boolean match = devices.stream().anyMatch((data) -> {
					return data.getCellular().getCellular() == sim;
			});
			 
			 if(match) {
				 dto.setStatus(false);
				 dto.setMessage("The following records have existing SIM numbers. They will be updated with the details in the file.");
			 }
			 
			 boolean Serialmatch = devices.stream().anyMatch((data) -> {
					return data.getCellular().getCellular() == serial;
			 });
			
			 if(Serialmatch) {
				 dto.setStatus(false);
				 dto.setMessage("The following records have existing serial numbers. They will be updated with the details in the file. ");
			 }
			 
			 
			 
			if(deviceRepository.existBySimno((String)device.get("imei"), sim)) {
				 dto.setStatus(true);
				 dto.setMessage("The following records have existing SIM numbers. They will be updated with the details in the file.");
			 }
			 
			 
			 if(deviceRepository.existBySerialNumber((String)device.get("imei"), serial)) {
				 dto.setStatus(true);
				 dto.setMessage("The following records have existing serial numbers. They will be updated with the details in the file.");
			 }



			if (dto.getStatus()) {
				gatwayDevice.setProductCode(gatewayBulkUploadRequestWithDeviceForwarding.getProductCode());
				gatwayDevice.setProductName(
						gatewayBulkUploadRequestWithDeviceForwarding.getProductMasterResponse().getProduct_name());
				gatwayDevice.setSon(gatewayBulkUploadRequestWithDeviceForwarding.getSalesforceOrderId());

				gatwayDevice.setUsageStatus(gatewayBulkUploadRequestWithDeviceForwarding.getUsageStatus());
//				DeviceDetails detail = new DeviceDetails();
//				detail.setImei(gatwayDevice.getImei());
//				detail.setUsageStatus(gatewayBulkUploadRequestWithDeviceForwarding.getUsageStatus());
//				gatwayDevice.setDeviceDetails(detail);;
				gatwayDevice.setPurchaseBy(purchasedBy);
				gatwayDevice.setOrganisation(company);

//				String serial = String.valueOf(device.get("serial"));
//				String sim = String.valueOf(device.get("sim"));
		//		if(sim != null && !sim.isEmpty() && !sim.equalsIgnoreCase("null") && serial != null && !serial.isEmpty() && !serial.equalsIgnoreCase("null")) {
				Cellular cellular = gatwayDevice.getCellular();
				if (cellular == null) {
					cellular = new Cellular();
					cellular.setUuid(UUID.randomUUID().toString());
					cellular.setImei(gatwayDevice.getImei());
					 }

					cellular.setCellular(sim);
					cellular.setImsi(serial);

					gatwayDevice.setCellular(cellular);
			//	}

					
					 
					 devices.add(gatwayDevice);
			}
			gatewayUpdateDTOs.add(dto);
		}
		
		
		
		if (!devices.isEmpty()) {
			deviceRepository.saveAll(devices);
		}
		for (Device device : devices) {
			redisDeviceRepository.addMap(KeyConstant.DEVICE_CURRENT_VIEW_PREFIX + device.getImei(),
					KeyConstant.CUSTOMER_ID, device.getOrganisation().getAccountNumber());

			if (purchasedBy != null) {
				redisDeviceRepository.addMap(KeyConstant.DEVICE_CURRENT_VIEW_PREFIX + device.getImei(),
						KeyConstant.PURCHASED_BY, purchasedBy.getAccountNumber());
			}

			if (device.getIotType() != null) {
				redisDeviceRepository.addMap(KeyConstant.DEVICE_CURRENT_VIEW_PREFIX + device.getImei(),
						KeyConstant.DEVICE_TYPE, device.getProductName());
			}

			if (gatewayBulkUploadRequestWithDeviceForwarding.getIsAllImei()) {
				DeviceForwardingRequest deviceForwardingRequest = new DeviceForwardingRequest();
				deviceForwardingRequest
						.setForwardingList(gatewayBulkUploadRequestWithDeviceForwarding.getForwardingList());
				deviceForwardingRequest.setDeviceId(device.getImei());
				try {
					deviceForwardingService.deleteDeviceForwardingByImei(device.getImei());
					Boolean isSaved = deviceForwardingService.addDeviceForwarding(deviceForwardingRequest, userName);
					logger.info("Device forwarding saved successfully for device id : " + device.getImei() + "status : "
							+ isSaved);
				} catch (Exception e) {
					logger.error(
							"Exception occured while saving device forwarding for device id : " + device.getImei());
					logger.error("Exception is : " + e.getMessage());
				}
			}
		}
		deviceForwardingService.saveExcludedCustomerForwardingRules(
				gatewayBulkUploadRequestWithDeviceForwarding.getIgnoreForwardingRules(), devices, company, purchasedBy,
				user);
		deviceForwardingService.updateExcludeCustomerFrowardingRulesOnRedis(
				gatewayBulkUploadRequestWithDeviceForwarding.getIgnoreForwardingRules(), devices);

		return gatewayUpdateDTOs;
	}

	@Override
	@Transactional
	public Map<String, List<String>> saveShipmentDetails(ShipmentDetailsRequest shipmentDetailsRequest) {
		Organisation company = restUtils.getCompanyFromCompanyService(shipmentDetailsRequest.getSalesforceAccountId());
		Map<String, List<String>> responseMap = new HashMap<>();
		if (company != null) {
			List<Device> gatewayList = new ArrayList<>();
			for (AssetDetail assetDetail : shipmentDetailsRequest.getAssetDetails()) {
				for (String imei : assetDetail.getImeiList()) {
					if (imei != null) {
						Device byImei = deviceRepository.findByImei(imei);
						if (byImei == null) {
							Device gateway = new Device();
							gateway.setStatus(DeviceStatus.PENDING);
							gateway.setOrganisation(company);
							gateway.setDeviceType(IOTType.GATEWAY.getIOTTypeValue());
							gateway.setCreatedOn(Instant.now());
							gateway.setImei(imei);
							gateway.setProductCode(assetDetail.getProductCode());
							gateway.setProductName(assetDetail.getProductShortName());
							boolean isGatewayUuidUnique = false;
							String gatewayUuid = "";
							while (!isGatewayUuidUnique) {
								gatewayUuid = UUID.randomUUID().toString();
								Device byUuid = deviceRepository.findByUuid(gatewayUuid);
								if (byUuid == null) {
									isGatewayUuidUnique = true;
								}
							}
							gateway.setUuid(gatewayUuid);
							gateway.setSalesforceOrderId(shipmentDetailsRequest.getSalesforceOrderNumber());
							gateway.setLastPerformAction(Constants.GATEWAYS_ACTION_ADDED);
							gateway.setTimeOfLastDownload(Instant.now());
							gateway = deviceRepository.save(gateway);
							if (assetDetail.getSensorList() != null && !assetDetail.getSensorList().isEmpty()) {
								for (SensorProduct sensorProduct : assetDetail.getSensorList()) {
									Device sensor = new Device();
									sensor.setStatus(DeviceStatus.PENDING);
									// sensor.setGateway(gateway);
									sensor.setProductCode(sensorProduct.getProductCode());
									sensor.setProductName(sensorProduct.getProductName());
									sensor.setCreatedOn(Instant.now());
									boolean isSensorUuidUnique = false;
									String sensorUuid = "";
									while (!isSensorUuidUnique) {
										sensorUuid = UUID.randomUUID().toString();
										Device byUuid = deviceRepository.findByUuid(sensorUuid);
										if (byUuid == null) {
											isSensorUuidUnique = true;
										}
									}
									sensor.setUuid(sensorUuid);
									sensor = deviceRepository.save(sensor);
									Device_Device_xref gatewaySensorXref = new Device_Device_xref();
									gatewaySensorXref.setDateCreated(Instant.now());
									gatewaySensorXref.setActive(true);
									gatewaySensorXref.setDeviceUuid(gateway);
									gatewaySensorXref.setSensorUuid(sensor);
									gatewaySensorXref = deviceDeviceXrefRespository.save(gatewaySensorXref);
								}
							}
							if (responseMap.get(Constants.SAVED_MAP_KEY) != null) {
								responseMap.get(Constants.SAVED_MAP_KEY).add(imei);
							} else {
								List<String> imeiList = new ArrayList<>();
								imeiList.add(imei);
								responseMap.put(Constants.SAVED_MAP_KEY, imeiList);
							}
						} else {
							if (responseMap.get(Constants.REJECTED_MAP_KEY) != null) {
								responseMap.get(Constants.REJECTED_MAP_KEY).add(imei);
							} else {
								List<String> imeiList = new ArrayList<>();
								imeiList.add(imei);
								responseMap.put(Constants.REJECTED_MAP_KEY, imeiList);
							}
						}
					} else {
						throw new DeviceException("IMEI number can not be null");
					}
				}
			}
		} else {
			throw new DeviceException(
					"No Company found for account number " + shipmentDetailsRequest.getSalesforceAccountName());
		}
		return responseMap;
	}

	@Override
	@Transactional
	public Map<String, List<String>> saveBeaconDetails(BeaconDetailsRequest beaconDetailsRequest, Long userId) {
		Organisation company = restUtils.getCompanyFromCompanyService(beaconDetailsRequest.getSalesforceAccountId());
		Map<String, List<String>> responseMap = new HashMap<>();
		if (company != null) {
			for (BeaconPayload beaconPayload : beaconDetailsRequest.getBeaconDetails()) {
				if (beaconPayload.getMacAddress() != null) {
					Device gate = deviceRepository.findByMac_address(beaconPayload.getMacAddress());
					User user = restUtils.getUserFromAuthService(userId);
					if (gate == null) {
						Device gateway = new Device();
						gateway.setStatus(DeviceStatus.PENDING);
						gateway.setOrganisation(company);
						gateway.setDeviceType(DeviceType.BEACON.getDeviceValue());
						gateway.setImei("");
						gateway.setCreatedOn(Instant.now());
						gateway.setCreatedBy(user);
						gateway.setProductCode(beaconPayload.getProductCode());
						gateway.setProductName(beaconPayload.getProductName());
						gateway.setMacAddress(beaconPayload.getMacAddress());
						boolean isGatewayUuidUnique = false;
						String gatewayUuid = "";
						while (!isGatewayUuidUnique) {
							gatewayUuid = UUID.randomUUID().toString();
							Device byUuid = deviceRepository.findByUuid(gatewayUuid);
							if (byUuid == null) {
								isGatewayUuidUnique = true;
							}
						}
						gateway.setUuid(gatewayUuid);
						gateway.setSalesforceOrderId(beaconDetailsRequest.getSalesforceOrderNumber());
						gateway.setLastPerformAction(Constants.GATEWAYS_ACTION_ADDED);
						gateway.setTimeOfLastDownload(Instant.now());
						gateway = deviceRepository.save(gateway);
					} else {
						throw new DeviceException("Mac Address is already associated with another device");
					}
				} else {
					throw new DeviceException("Mac Address can not be null");
				}
			}

		} else {
			throw new DeviceException(
					"No Company found for account number " + beaconDetailsRequest.getSalesforceAccountName());
		}
		return responseMap;

	}

	@Override
	public Boolean updateGatewayMacAddress(UpdateMacAddressRequest updateMacAddressRequest, Long userId) {
		User user = restUtils.getUserFromAuthService(userId);
		Device gateway = deviceRepository.findByUuid(updateMacAddressRequest.getUuid());
		gateway.setMacAddress(updateMacAddressRequest.getMacAddress());
		gateway.setUpdatedOn(Instant.ofEpochMilli(Long.parseLong(updateMacAddressRequest.getDatetimeRT())));
		gateway.setUpdatedBy(user);
		gateway.setLastPerformAction(Constants.GATEWAYS_ACTION_UPDATED);
		gateway.setTimeOfLastDownload(Instant.now());
		gateway = deviceRepository.save(gateway);
		return true;
	}

	private Boolean deleteGatewayData(Device gateway) {
		List<Device_Device_xref> gatewaySensorXrefList = deviceDeviceXrefRespository.findByDeviceUuid(gateway);
		gatewaySensorXrefList.forEach(gatewaySensorXref -> {
			deviceDeviceXrefRespository.delete(gatewaySensorXref);
		});
		// gateway.getSensors().forEach(sensor -> {
		// sensorRepository.delete(sensor);
		// });
		Asset_Device_xref assetGatewayXref = assetDeviceXrefRepository.findByGatewayId(gateway.getId());
		// assetGatewayXrefList.forEach(assetGatewayXref -> {
		assetDeviceXrefRepository.delete(assetGatewayXref);
		// });
		if (gateway.getImei() != null) {
			List<AttributeValue> attributvalue = attributeValueRepository.findByDeviceImei(gateway.getImei());
			if (attributvalue != null && attributvalue.size() > 0) {
				attributvalue.forEach(attribteval -> {
					attributeValueRepository.delete(attribteval);
				});
			}
		}

		gateway.setIsDeleted(Boolean.TRUE);
		gateway.setLastPerformAction(Constants.GATEWAYS_ACTION_DELETED);
		gateway.setTimeOfLastDownload(Instant.now());
		deviceRepository.save(gateway);
		// gatewayRepository.delete(gateway);
		return true;
	}

	@Override
	public Boolean markInstalledToDevices(List<String> listOfUuid, Long userId) {
		Boolean flag = false;
		User user = restUtils.getUserFromAuthService(userId);
		if (listOfUuid != null && listOfUuid.size() > 0 && user != null) {
			try {
				for (String uuid : listOfUuid) {
					Device gateway = deviceRepository.findByUuid(uuid);
					if (gateway != null && gateway.getStatus().equals(GatewayStatus.PENDING)) {
						gateway.setStatus(DeviceStatus.ACTIVE_NOT_IA);
						gateway.setUpdatedBy(user);
						gateway.setUpdatedOn(Instant.now());
						gateway.setLastPerformAction(Constants.GATEWAYS_ACTION_UPDATED);
						gateway.setTimeOfLastDownload(Instant.now());
						deviceRepository.save(gateway);
						flag = true;
					}
				}
			} catch (Exception e) {
				flag = false;
				logger.error(e.getMessage());
			}
		}
		return flag;
	}

	@Override
	public Boolean resetIndividualDevice(String gatewayUuid, Long userId) {
		Boolean flag = false;
		User user = restUtils.getUserFromAuthService(userId);
		if (gatewayUuid != null && user != null) {
			try {
				Device gateway = deviceRepository.findByUuid(gatewayUuid);
				if (gateway != null && gateway.getStatus().equals(DeviceStatus.ACTIVE_NOT_IA)) {
					gateway.setStatus(DeviceStatus.PENDING);
					gateway.setUpdatedBy(user);
					gateway.setUpdatedOn(Instant.now());
					gateway.setLastPerformAction(Constants.GATEWAYS_ACTION_UPDATED);
					gateway.setTimeOfLastDownload(Instant.now());
					deviceRepository.save(gateway);
					flag = true;
				}
			} catch (Exception e) {
				flag = false;
				logger.error(e.getMessage());
			}
		}
		return flag;
	}

	@Override
	public List<DeviceBean> getGateway(String accountNumber, String imei, String gatewayUuid,
			DeviceStatus gatewayStatus, IOTType type, String macAddress) {
		List<DeviceBean> gatewayBeanList = new ArrayList<>();
		List<Device> gatewayList = new ArrayList<Device>();
		HashMap<String, String> lookupMap = null;
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		logger.info("before getting response from getGateway method from gateway service find All method :"
				+ stopWatch.prettyPrint());
		Specification<Device> spc = GatewaySpecification.getGatewayListSpecification(accountNumber, imei, gatewayUuid,
				gatewayStatus, null, type, macAddress, null);
		gatewayList = deviceRepository.findAll(spc);
		stopWatch.stop();
		logger.info("after getting response from getGateway method from gateway service find All method :"
				+ stopWatch.prettyPrint());

		StopWatch stopWatchBean = new StopWatch();
		stopWatchBean.start();
		logger.info(
				"before getting response from getGateway method from gateway service find All bean converter method :"
						+ stopWatchBean.prettyPrint());
		List<Lookup> lookups = assetConfigurationRepository.findAll();
		if (lookups != null && lookups.size() > 0) {
			lookupMap = new HashMap<String, String>();
			for (Lookup lookup : lookups) {
				lookupMap.put(lookup.getField(), lookup.getValue());
			}
		}

		if (gatewayList.size() > 0) {
			for (Device gateway : gatewayList) {
				gatewayBeanList.add(beanConverter.convertGatewayToGatewayBean(gateway, lookupMap));
			}
			;
		}
		stopWatchBean.stop();
		logger.info(
				"after getting response from getGateway method from gateway service find All bean converter method :"
						+ stopWatchBean.prettyPrint());
		return gatewayBeanList;
	}

	@Override
	public Page<GatewayBeanForMobileApp> getGatewayWithPagination(String accountNumber, String imei, String gatewayUuid,
			DeviceStatus gatewayStatus, IOTType type, String macAddress, Pageable pageable,
			MessageDTO<Page<GatewayBeanForMobileApp>> messageDto, String timeOfLastDownload) {
		Page<GatewayBeanForMobileApp> gatewayBeanDetailList = null;
		List<GatewayBeanForMobileApp> gatewayBeanList = new ArrayList<>();
		Page<DeviceView> gatewayList = null;
		HashMap<String, String> lookupMap = null;
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		Instant lastDownloadeTime = null;
		if (timeOfLastDownload != null && !timeOfLastDownload.isEmpty()) {
			lastDownloadeTime = Instant.ofEpochMilli(Long.parseLong(timeOfLastDownload));
			logger.info("Last Downloaded Time: " + lastDownloadeTime + " Account Number: " + accountNumber);
		}
		logger.info("before getting response from getGateway method from gateway service find All method :"
				+ stopWatch.prettyPrint());
		Specification<DeviceView> spc = DeviceViewSpecification.getGatewayListSpecification(accountNumber, imei,
				gatewayUuid, gatewayStatus, type, macAddress, lastDownloadeTime);
		gatewayList = deviceRepositoryViewRepository.findAll(spc, pageable);
		stopWatch.stop();
		logger.info("after getting response from getGateway method from gateway service find All method :"
				+ stopWatch.prettyPrint());

		StopWatch stopWatchBean = new StopWatch();
		stopWatchBean.start();
		logger.info(
				"before getting response from getGateway method from gateway service find All bean converter method :"
						+ stopWatchBean.prettyPrint());
		List<Lookup> lookups = assetConfigurationRepository.findAll();
		if (lookups != null && lookups.size() > 0) {
			lookupMap = new HashMap<String, String>();
			for (Lookup lookup : lookups) {
				lookupMap.put(lookup.getField(), lookup.getValue());
			}
		}

		if (gatewayList != null && gatewayList.getContent() != null && gatewayList.getContent().size() > 0) {
			for (DeviceView gateway : gatewayList.getContent()) {
				gatewayBeanList.add(beanConverter.convertGatewayToGatewayBeanForMobileApp(gateway, lookupMap));
			}
			
		}

		if (gatewayBeanList != null && gatewayBeanList.size() > 0) {
			gatewayBeanDetailList = new PageImpl<>(gatewayBeanList);
			messageDto.setBody(gatewayBeanDetailList);
			logger.info("Inside getGatewayList Post Size ==== " + gatewayList.getTotalElements());
			messageDto.setTotalKey(gatewayList.getTotalElements());
			logger.info("Total Elements" + gatewayList.getTotalElements());

			messageDto.setCurrentPage(gatewayList.getNumber());
			logger.info("Current Page " + gatewayList.getNumber());

			messageDto.setTotal_pages(gatewayList.getTotalPages());
			logger.info("Total pages" + gatewayList.getTotalPages());
		}
		stopWatchBean.stop();
		logger.info(
				"after getting response from getGateway method from gateway service find All bean converter method :"
						+ stopWatchBean.prettyPrint());
		return gatewayBeanDetailList;
	}

}
