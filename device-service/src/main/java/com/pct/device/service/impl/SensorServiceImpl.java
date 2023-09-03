package com.pct.device.service.impl;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.pct.common.constant.DeviceStatus;
import com.pct.common.constant.IOTType;
import com.pct.common.model.Device;
import com.pct.common.model.Device_Device_xref;
import com.pct.common.model.Organisation;
import com.pct.common.model.ProductMaster;
import com.pct.common.model.SensorDetail;
import com.pct.common.model.User;
import com.pct.common.payload.DeviceATCommandReqResponse;
import com.pct.common.util.JwtUtil;
import com.pct.device.dto.MessageDTO;
import com.pct.device.dto.SensorDataDTO;
import com.pct.device.exception.DeviceException;
import com.pct.device.payload.ATCommandRequestPayload;
import com.pct.device.payload.DeviceCommandRequestPayload;
import com.pct.device.payload.RefreshUpdatedMacAddressReqPayload;
import com.pct.device.payload.RetriveDeviceSensorRequest;
import com.pct.device.payload.SensorDetailPayLoad;
import com.pct.device.payload.SensorPayLoad;
import com.pct.device.payload.SensorRequestPayload;
import com.pct.device.payload.SensorSubDetailRequestPayload;
import com.pct.device.payload.UpdateMacAddressRequest;
import com.pct.device.repository.IDeviceDeviceXrefRepository;
import com.pct.device.repository.IDeviceRepository;
import com.pct.device.repository.IProductMasterRepository;
import com.pct.device.repository.SensorDetailRepository;
import com.pct.device.service.ISensorService;
import com.pct.device.specification.SensorSpecification;
import com.pct.device.util.AppUtility;
import com.pct.device.util.BeanConverter;
import com.pct.device.util.RestUtils;

@Service
public class SensorServiceImpl implements ISensorService {
	Logger logger = LoggerFactory.getLogger(SensorServiceImpl.class);

	private final String PSI_PRODUCT_CODE = "77-S500";
	private final String STE_PRODUCT_CODE = "77-S177";
	private final String AIR_PRODUCT_CODE = "77-S202";
	private final String ATIS_PRODUCT_CODE = "77-S203";
	private final String PSI_AIR_PRODUCT_CODE = "77-S137";
	private final String PSI_ATIS_PRODUCT_CODE = "77-S164";
	private final String RECEIVER_PRODUCT_CODE = "77-S206";
	private final String WHEEL_END_PRODUCT_CODE = "77-S119";
	private final String AIR_PRODUCT_NAME = "MicroSP Air Tank";
	private final String ATIS_PRODUCT_NAME = "MicroSP ATIS Regulator";
	private final String PSI_AIR_PRODUCT_NAME = "Air Tank Sensor";
	private final String PSI_ATIS_PRODUCT_NAME = "Regulator Sensor";
	private final String PSI_PRODUCT_NAME = "PSI TireView - 8 Tires Excluding CP Hoses";
	private final String STE_PRODUCT_NAME = "MICROSP TPMS SENSOR";
	private final String RECEIVER_PRODUCT_NAME = "MicroSP Wired Receiver";
	private final String WHEEL_END_PRODUCT_NAME = "Wheel End Sensor";

	@Autowired
	private IDeviceRepository deviceRepository;

	@Autowired
	private BeanConverter beanConverter;

	@Autowired
	private RestUtils restUtils;

	@Autowired
	private SensorDetailRepository sensorDetailRepository;

	@Autowired
	private IProductMasterRepository productMasterRepository;

	@Autowired
	private IDeviceDeviceXrefRepository deviceDeviceXrefRepository;

	@Autowired
	private JwtUtil JwtUtil;

	@Override
	public Boolean addSensorDetail(SensorRequestPayload sensorRequestRequest, String userName) throws DeviceException {
		logger.info("Inside addSensorDetail and fetching sensorDetail and userId value",
				sensorRequestRequest + " " + userName);
		String can = sensorRequestRequest.getCan();
		Organisation company = restUtils.getCompanyFromCompanyService(can);
		User user = restUtils.getUserFromAuthService(userName);
		Device deviceMacAddress = deviceRepository.findByMac_address(sensorRequestRequest.getMacAddress());
		String macAddress = sensorRequestRequest.getMacAddress();
		if (company != null) {
			if (macAddress != null) {
				if (deviceMacAddress == null) {
					Device device = new Device();
					device.setOrganisation(company);
					device.setProductCode(sensorRequestRequest.getProductCode());
					device.setProductName(sensorRequestRequest.getProductName());
					device.setSon(sensorRequestRequest.getSon());
					device.setStatus(DeviceStatus.PENDING);
					device.setCreatedAt(Instant.now());
					device.setEpicorOrderNumber(sensorRequestRequest.getEpicorOrderNumber());
					device.setMacAddress(sensorRequestRequest.getMacAddress());
					boolean isDeviceUuidUnique = false;
					String deviceUuid = "";
					while (!isDeviceUuidUnique) {
						deviceUuid = UUID.randomUUID().toString();
						Device byUuid = deviceRepository.findByUuid(deviceUuid);
						if (byUuid == null) {
							isDeviceUuidUnique = true;
						}
					}
					device.setIotType(IOTType.SENSOR);

					device.setCreatedBy(user);
					device.setCreatedAt(Instant.now());

					device.setUuid(deviceUuid);
					List<SensorDetail> sensorDetails = new ArrayList<>();
					if (sensorRequestRequest.getSensorSubDetails() != null
							&& sensorRequestRequest.getSensorSubDetails().size() > 0) {
						for (SensorSubDetailRequestPayload sensorSubDetailRequestPayload : sensorRequestRequest
								.getSensorSubDetails()) {
							SensorDetail sensorDetail = new SensorDetail();
							sensorDetail.setPosition(sensorSubDetailRequestPayload.getPosition());
							sensorDetail.setType(sensorSubDetailRequestPayload.getType());
							sensorDetail.setSensorId(sensorSubDetailRequestPayload.getSensorId());
							sensorDetail.setSensorUUID(device);
							boolean isSensotUuidUnique = false;
							String sensorUuid = "";
							while (!isSensotUuidUnique) {
								sensorUuid = UUID.randomUUID().toString();
								SensorDetail byUuid = sensorDetailRepository.findByUuid(sensorUuid);
								if (byUuid == null) {
									isSensotUuidUnique = true;
								}
							}
							sensorDetail.setUuid(sensorUuid);
							sensorDetails.add(sensorDetail);
						}
					}
					device.setSensorDetail(sensorDetails);
					deviceRepository.save(device);
					logger.info("Sensor Details saved successfully");

				} else {
					throw new DeviceException("MAC Address already exist");
				}
			} else {
				throw new DeviceException("MAC Address can not be null");
			}
		} else {
			throw new DeviceException("No Company found for account number " + sensorRequestRequest.getCan());
		}
		return Boolean.TRUE;
	}

	@Override
	public Page<SensorPayLoad> getSensorWithPagination(String accountNumber, String uuid, DeviceStatus status,
			String mac, Map<String, String> filterValues, Pageable pageable) {
		Page<Device> deviceDetails = null;
		IOTType type = IOTType.getValue("Sensor");
		Specification<Device> spc = SensorSpecification.getSensorListSpecification(accountNumber, uuid, status, mac,
				filterValues, type);
		logger.info("After Specification " + spc);
		deviceDetails = deviceRepository.findAll(spc, pageable);
		logger.info("After DB Call " + deviceDetails);
		Page<SensorPayLoad> sensorRecordloadPage = null;
		if (deviceDetails.getNumberOfElements() > 0) {
			sensorRecordloadPage = beanConverter.convertSensorToSensorPayLoad(deviceDetails, pageable);
			logger.info("Fetching Sensor Details list" + sensorRecordloadPage);
		} else {
			throw new DeviceException("No sensor details found for account number: " + accountNumber);
		}
		return sensorRecordloadPage;
	}

	@Override
	public boolean deleteSensorDetail(String can, String uuid) {
		logger.info("Inside deleteSensorDetail");
		if (can != null && !can.isEmpty()) {
			Organisation company = restUtils.getCompanyFromCompanyService(can); // Company will be used when deleting //
																				// Install history
			IOTType type = IOTType.getValue("Sensor");
			DeviceStatus dStatus = DeviceStatus.getGatewayStatusInSearch("PENDING");
			Specification<Device> spc = SensorSpecification.getBeaconSpec(can, uuid, type);
			List<Device> deviceList = deviceRepository.findAll(spc);
			logger.info("After fetching sensor details" + deviceList.toString());
			AtomicBoolean status = new AtomicBoolean(true);
			if (deviceList.size() > 0) {
				deviceList.forEach(device -> {
					if (!device.getStatus().equals(dStatus)) {
						throw new DeviceException("Device is not in pending state.");
					} else {

						status.set(status.get() && deleteSensorData(device));
						logger.info("Sensor deleted Successfully");
					}
				});
			} else {
				throw new DeviceException("No device found for uuid/can number");
			}
			return status.get();
		} else {
			throw new DeviceException("Account number is mandatory");
		}
	}

	private Boolean deleteSensorData(Device device) {
		deviceRepository.delete(device);
		return true;

	}

	@Override
	public Boolean updateSensorDetail(SensorDetailPayLoad sensorDetailPayload, String userName) {
		logger.info("Inside updateSensorDetail and fetching sensorDetail and userId value" + sensorDetailPayload + " "
				+ userName);
		User user = restUtils.getUserFromAuthService(userName);
		Device device = deviceRepository.findByUuid(sensorDetailPayload.getUuid());
		logger.info("Fetching sensor details for uuid : " + sensorDetailPayload.getUuid());
		if (device != null) {
			device.setProductCode(sensorDetailPayload.getProductCode());
			device.setProductName(sensorDetailPayload.getProductName());
			device.setSon(sensorDetailPayload.getSon());
			device.setUpdatedAt(Instant.now());
			device.setUpdatedBy(user);
			device.setEpicorOrderNumber(sensorDetailPayload.getEpicorOrderNumber());
			device.setMacAddress(sensorDetailPayload.getMacAddress());
			deviceRepository.save(device);
			logger.info("Sensor updated for the uuid : " + sensorDetailPayload.getUuid());
		} else {
			throw new DeviceException("Sensor not found for the uuid " + sensorDetailPayload.getUuid());
		}
		return Boolean.TRUE;
	}

	@Override
	public List<SensorPayLoad> getSensorDetails(String accountNumber, String uuid) {
		logger.info("Inside getSensorDetails for account number " + accountNumber + " uuid " + uuid);
		List<SensorPayLoad> sensorDetailList = new ArrayList<>();
		List<Device> deviceList = new ArrayList<Device>();
		IOTType type = IOTType.getValue("Sensor");
		Specification<Device> spc = SensorSpecification.getBeaconSpec(accountNumber, uuid, type);
		logger.info("Fetching device details based on specification.");
		deviceList = deviceRepository.findAll(spc);
		if (deviceList.size() > 0) {
			deviceList.forEach(device -> {
				sensorDetailList.add(beanConverter.convertSensorToSensorPayLoad(device));
			});
		} else {
			throw new DeviceException("No device found for uuid/can number");
		}
		return sensorDetailList;
	}

	@Override
	public List<SensorDataDTO> getSensorList() {
		List<ProductMaster> prod = productMasterRepository.findByType("Sensor");
		List<SensorDataDTO> sensors = new ArrayList<>();
		for (ProductMaster prd : prod) {
			SensorDataDTO sendto = new SensorDataDTO();
			sendto.setProductCode(prd.getProductCode());
			sendto.setProductName(prd.getProductName());
			sendto.setSensorUuid(prd.getUuid());
			sensors.add(sendto);
		}
		return sensors;
	}

	@Transactional
	@Override
	public MessageDTO updateSensorDetailsMacAddress(Map<String, Object> sensorDetails) {
		// TODO Auto-generated method stub
		if (sensorDetails == null) {
			throw new DeviceException("Please send valid data to update");
		}
		String uuid = (String) sensorDetails.get("uuid");
		if (uuid == null) {
			throw new DeviceException("Please send valid sensor uuid");
		}
		String macAddress = (String) sensorDetails.get("macAddress");
		String position = (String) sensorDetails.get("position");
		String deviceId = (String) sensorDetails.get("deviceId");
		if (macAddress == null) {
			throw new DeviceException("Please enter valid mac address");
		}
		SensorDetail details = sensorDetailRepository.findByUuid(uuid);
		if (details == null) {
			throw new DeviceException("No sensor found for uuid");
		}
		try {
			ATCommandRequestPayload payload = new ATCommandRequestPayload();
			payload.setGatewayId(deviceId);
			payload.setPriority(-1);
			payload.setAtCommand("AT+XTPMSAD=" + position + "," + macAddress);
			DeviceATCommandReqResponse response = restUtils.getATCResponse(payload);
			if (!AppUtility.isEmpty(response) && !AppUtility.isEmpty(response.getUuid())) {
				DeviceCommandRequestPayload deviceCommandRequestPayload = new DeviceCommandRequestPayload();
				deviceCommandRequestPayload.setUuid(response.getUuid());
//				deviceCommandRequestPayload.setDeviceId(deviceId);
				Map<String, Object> resp = new HashMap<>();
				resp.put("at_command_uuid", response.getUuid());
				DeviceATCommandReqResponse deviceCommandResponse = new DeviceATCommandReqResponse();
				TimeUnit.SECONDS.sleep(10);
				deviceCommandResponse = restUtils.getDeviceATCommandRequestResponse(deviceCommandRequestPayload);
				if (!AppUtility.isEmpty(deviceCommandResponse.getStatus())
						&& !deviceCommandResponse.getStatus().equals("COMPLETED")) {
					TimeUnit.SECONDS.sleep(10);
					deviceCommandResponse = restUtils.getDeviceATCommandRequestResponse(deviceCommandRequestPayload);
				}
				if (!AppUtility.isEmpty(deviceCommandResponse.getStatus())
						&& !deviceCommandResponse.getStatus().equals("COMPLETED")) {
					TimeUnit.SECONDS.sleep(10);
					deviceCommandResponse = restUtils.getDeviceATCommandRequestResponse(deviceCommandRequestPayload);
				}
				if (!AppUtility.isEmpty(deviceCommandResponse.getStatus())
						&& !deviceCommandResponse.getStatus().equals("COMPLETED")) {
					TimeUnit.SECONDS.sleep(10);
					deviceCommandResponse = restUtils.getDeviceATCommandRequestResponse(deviceCommandRequestPayload);
				}
				if (!AppUtility.isEmpty(deviceCommandResponse.getStatus())
						&& !deviceCommandResponse.getStatus().equals("COMPLETED")) {
					TimeUnit.SECONDS.sleep(10);
					deviceCommandResponse = restUtils.getDeviceATCommandRequestResponse(deviceCommandRequestPayload);
				}
				if (!AppUtility.isEmpty(deviceCommandResponse.getStatus())
						&& deviceCommandResponse.getStatus().equals("COMPLETED")) {
					details.setSensorId(macAddress);
					details.setAtCommandStatus(deviceCommandResponse.getStatus());
					sensorDetailRepository.save(details);
					return new MessageDTO<>("STATUSCOMPLETED", resp, true);
				} else if (!AppUtility.isEmpty(deviceCommandResponse.getStatus())
						&& deviceCommandResponse.getStatus().equals("ERROR")) {
					details.setAtCommandStatus("ERROR");
					details.setAtCommandUuid(response.getUuid());
					details.setNewSensorId(macAddress);
					sensorDetailRepository.save(details);
					return new MessageDTO<>("STATUSERROR", resp, false);
				} else {
					details.setAtCommandStatus("PENDING");
					details.setAtCommandUuid(response.getUuid());
					details.setNewSensorId(macAddress);
					sensorDetailRepository.save(details);
					return new MessageDTO<>("STATUSPENDING", resp, false);
				}
			} else {
				throw new DeviceException("The AT command could not be sent to the gateway successfully");
			}

		} catch (Exception ex) {
			ex.printStackTrace();
			logger.info("Error while sending AT command");
			throw new DeviceException("The ID could not be sent to the gateway successfully :-- " + ex.getMessage());
		}
	}

	@Override
	public MessageDTO refreshUpdatedSensor(RefreshUpdatedMacAddressReqPayload sensorDetails) {
		if (sensorDetails == null) {
			throw new DeviceException("Please send valid data to refresh");
		}
		if (sensorDetails.getSensorUuid() == null) {
			throw new DeviceException("Please send valid sensor uuid");
		}
		if (sensorDetails.getDeviceId() == null) {
			throw new DeviceException("Please send valid device Id");
		}
		if (sensorDetails.getNewSensorId() == null) {
			throw new DeviceException("Please send valid new sensor mac address");
		}
		if (sensorDetails.getAtCommandUuid() == null) {
			throw new DeviceException("Please send valid AT Command uuid");
		}
		SensorDetail details = sensorDetailRepository.findByUuid(sensorDetails.getSensorUuid());
		if (details == null) {
			throw new DeviceException("No sensor found for uuid");
		}
		DeviceCommandRequestPayload deviceCommandRequestPayload = new DeviceCommandRequestPayload();
		deviceCommandRequestPayload.setUuid(sensorDetails.getAtCommandUuid());
//		deviceCommandRequestPayload.setDeviceId(sensorDetails.getDeviceId());
		DeviceATCommandReqResponse deviceCommandResponse = restUtils
				.getDeviceATCommandRequestResponse(deviceCommandRequestPayload);
		String message = "STATUSPENDING";
		boolean status = false;
		if (deviceCommandResponse.getStatus().equals("DELETED")) {
			return new MessageDTO<>("STATUSDELETED", false);
		}
		if (!AppUtility.isEmpty(deviceCommandResponse.getStatus())
				&& deviceCommandResponse.getStatus().equals("ERROR")) {
			details.setAtCommandStatus(deviceCommandResponse.getStatus());
			sensorDetailRepository.save(details);
			message = "STATUSERROR";
		}
		if (!deviceCommandResponse.getStatus().equals("COMPLETED")) {
			try {
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
						.withZone(ZoneId.systemDefault());
				LocalDateTime pastDate = LocalDateTime.parse(deviceCommandResponse.getCreatedOn(), formatter);
				LocalDateTime today = LocalDateTime.now();
				long minutes = ChronoUnit.MINUTES.between(pastDate, today);
				if (minutes >= 3) {
					ATCommandRequestPayload atCommand = new ATCommandRequestPayload();
					atCommand.setUuid(deviceCommandResponse.getUuid());
					atCommand.setAtCommand(sensorDetails.getNewSensorId());
					atCommand.setGatewayId(sensorDetails.getDeviceId());
					atCommand.setPriority(-1);
					restUtils.deleteATCResponse(atCommand);
					details.setAtCommandStatus("DELETED");
					sensorDetailRepository.save(details);
					return new MessageDTO<>("STATUSDELETED", false);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		if (!AppUtility.isEmpty(deviceCommandResponse.getStatus())
				&& deviceCommandResponse.getStatus().equals("PENDING")) {
			details.setAtCommandStatus(deviceCommandResponse.getStatus());
			sensorDetailRepository.save(details);
			message = "STATUSPENDING";
		}
		if (!AppUtility.isEmpty(deviceCommandResponse.getStatus())
				&& deviceCommandResponse.getStatus().equals("COMPLETED")) {
			if (!AppUtility.isEmpty(deviceCommandResponse.getDeviceResponse())
					&& deviceCommandResponse.getDeviceResponse().equalsIgnoreCase("error")) {
				details.setAtCommandStatus(deviceCommandResponse.getStatus());
				message = "STATUSCOMPLETEDWITHERROR";
			} else {
				details.setSensorId(sensorDetails.getNewSensorId());
				details.setAtCommandStatus(deviceCommandResponse.getStatus());
				message = "STATUSCOMPLETED";
				status = true;
			}
			sensorDetailRepository.save(details);
		}
		return new MessageDTO<>(message, deviceCommandResponse.getUuid(), status);
	}

	@Override
	public MessageDTO getAndAddLatestSensorDetails(RetriveDeviceSensorRequest retriveDeviceSensorRequest) {
		// TODO Auto-generated method stub
		logger.info("Inside getAndAddSensorDetails method");
		if (retriveDeviceSensorRequest == null) {
			throw new DeviceException("Please send valid data to get Device Response");
		}
		String deviceId = retriveDeviceSensorRequest.getDeviceId();
		if (deviceId == null) {
			throw new DeviceException("Please enter valid Device ID");
		}
		try {
			MessageDTO message = getSensorResponse("", retriveDeviceSensorRequest, true);
			Device device = deviceRepository.findByImei(deviceId);
			device.setRetriveStatus(message.getMessage());
			deviceRepository.save(device);
			return message;

		} catch (Exception ex) {
			ex.printStackTrace();
			logger.info("Error while sending AT command");
			throw new DeviceException(
					"The AT command could not be sent to the gateway successfully :- " + ex.getMessage());
		}
	}

	/**
	 * This method is used to retrieve sensor data using sending AT command and
	 * parse the sensor data and store in DB
	 * 
	 * @param retriveDeviceSensorRequest
	 */
	@Override
	public MessageDTO getAndAddSensorDetails(RetriveDeviceSensorRequest retriveDeviceSensorRequest) {
		// TODO Auto-generated method stub
		logger.info("Inside getAndAddSensorDetails method");
		if (retriveDeviceSensorRequest == null) {
			throw new DeviceException("Please send valid data to send AT Command");
		}
		String atCommand = retriveDeviceSensorRequest.getAtCommand();
		String deviceId = retriveDeviceSensorRequest.getDeviceId();
		if (atCommand == null) {
			throw new DeviceException("Please enter valid AT Command");
		}
		try {
			ATCommandRequestPayload payload = new ATCommandRequestPayload();
			payload.setGatewayId(deviceId);
			payload.setPriority(-1);
			payload.setAtCommand(atCommand);
			Device device = deviceRepository.findByImei(deviceId);
			logger.info("Send At Command to retrive sensor data");
			DeviceATCommandReqResponse response = restUtils.getATCResponse(payload);
			if (!AppUtility.isEmpty(response) && !AppUtility.isEmpty(response.getUuid())) {
				logger.info("Wait for 6 second to get at command device response data");
				TimeUnit.SECONDS.sleep(6);
				MessageDTO message = getSensorResponse(response.getUuid(), retriveDeviceSensorRequest, false);
				if (!message.getStatus()) {
					logger.info("Re run getSensorResponse method after not getting status completed");
					TimeUnit.SECONDS.sleep(5);
					message = getSensorResponse(response.getUuid(), retriveDeviceSensorRequest, false);
					if (!message.getStatus()) {
						TimeUnit.SECONDS.sleep(15);
						message = getSensorResponse(response.getUuid(), retriveDeviceSensorRequest, false);
					}
				}
				device.setRetriveStatus(message.getMessage());
				deviceRepository.save(device);
				return message;

			} else {
				throw new DeviceException("The AT command could not be sent to the gateway successfully");
			}

		} catch (Exception ex) {
			ex.printStackTrace();
			logger.info("Error while sending AT command");
			throw new DeviceException(ex.getMessage());
		}
	}

	/**
	 * This method is used to retrieve sensor data using sending AT command and
	 * parse the sensor data and store in DB
	 * 
	 * @param atCommandUuid
	 * @param retriveDeviceSensorRequest
	 * @param refresh
	 * @return
	 */
	public MessageDTO getSensorResponse(String atCommandUuid, RetriveDeviceSensorRequest retriveDeviceSensorRequest,
			boolean refresh) {
		DeviceATCommandReqResponse deviceCommandResponse = null;
		logger.info("Inside getSensorResponse method atCommandUuid= " + atCommandUuid);
		DeviceCommandRequestPayload deviceCommandRequestPayload = new DeviceCommandRequestPayload();
		deviceCommandRequestPayload.setUuid(atCommandUuid);
//		deviceCommandRequestPayload.setDeviceId(retriveDeviceSensorRequest.getDeviceId());
		if (refresh) {
			// Get AT command Response where response not come in 30 second. to refresh to
			// get data
			deviceCommandResponse = restUtils.getDeviceATCommandLatestRequestResponse(deviceCommandRequestPayload);
		} else {
			logger.info("going to fetch at command sensor response");
			deviceCommandResponse = restUtils.getDeviceATCommandRequestResponse(deviceCommandRequestPayload);
		}
		if (AppUtility.isEmpty(deviceCommandResponse)) {
			return new MessageDTO<>("DataNotUpdated", false);
		}
		String deviceResponse = deviceCommandResponse.getDeviceResponse();
		logger.info("At Command device response " + deviceResponse);
		if (deviceCommandResponse.getStatus().equals("DELETED")) {
			return new MessageDTO<>("StatusDeleted", false);
		}

		// If we do not get response of AT command with in 3 min then we will delete the
		// AT command from Redis Queue
		if (!deviceCommandResponse.getStatus().equals("COMPLETED")) {
			try {
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
						.withZone(ZoneId.systemDefault()); // Specify locale to determine human language and cultural
															// norms used in translating that input string.
				LocalDateTime pastDate = LocalDateTime.parse(deviceCommandResponse.getCreatedOn(), formatter);
				LocalDateTime today = LocalDateTime.now();
				long minutes = ChronoUnit.MINUTES.between(pastDate, today);
				// Comparing the AT command send time, if AT command send time greater then 3
				// minute then delete AT Command form redis
				if (minutes >= 3) {
					ATCommandRequestPayload atCommand = new ATCommandRequestPayload();
					atCommand.setUuid(deviceCommandResponse.getUuid());
					atCommand.setAtCommand(retriveDeviceSensorRequest.getAtCommand());
					atCommand.setGatewayId(retriveDeviceSensorRequest.getDeviceId());
					atCommand.setPriority(-1);
					// Delete AT Command from Redis Queue
					restUtils.deleteATCResponse(atCommand);
					return new MessageDTO<>("StatusDeleted", false);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			return new MessageDTO<>("StatusNotCompleted", false);
		}
		if (AppUtility.isEmpty(deviceResponse)) {
			return new MessageDTO<>("SensorNotOnline", false);
		}
		String status = null;
		String[] arr = deviceResponse.split("OFFLINE");
		String[] arr2 = deviceResponse.split("NOTINSTALLED");
		String[] array1 = deviceResponse.split("Loc:");
		if (arr != null && arr.length > 1) {
			if (array1 != null && array1.length > 1) {
				status = "OFFLINE";
			} else {
				return new MessageDTO<>("SensorNotOnline", false);
			}
		}
		if (arr2 != null && arr2.length > 1) {
			status = "NOTINSTALLED";
			return new MessageDTO<>(status, false);
		} else {
			status = "ONLINE";
		}

		if (deviceCommandResponse.getStatus().equals("COMPLETED")) {
			Device device = null;
			Device deviceAir = null;
			Device deviceAtis = null;
			Device deviceReceiver = null;
			Device deviceWheelEnd = null;

			String vendors = null;
			// Getting vendor name from AT command device response
			if (deviceResponse != null) {
				String[] arrs = deviceResponse.split("Vendor:");
				if (arrs != null && arrs.length > 1) {
					String[] arr3 = arrs[1].split("[\\r\\n]+");
					if (arr3 != null && arr3.length > 0) {
						vendors = arr3[0].trim();
					}
				}
			}
			// If Vendor is PSI then no need to Add Receiver Sensor in Table
			if (vendors != null && !vendors.equalsIgnoreCase("psi")) {
				// Find Device Sensor Mapping for Receiver Sensor
				Device_Device_xref device_Device_xref_reciever = deviceDeviceXrefRepository
						.findSensorUuidByProductCodeAndDeviceUuid(retriveDeviceSensorRequest.getDeviceUuid(),
								RECEIVER_PRODUCT_CODE);
				if (AppUtility.isEmpty(device_Device_xref_reciever)) {
					logger.info("TPSM receiver sensor not availble, Adding sensor");
					deviceReceiver = addSensor(RECEIVER_PRODUCT_CODE, RECEIVER_PRODUCT_NAME,
							retriveDeviceSensorRequest);
					logger.info("TPSM receiver sensor not availble, Adding Device sensor Mapping Receiver");
					addDeviceSensorMapping(retriveDeviceSensorRequest, deviceReceiver);
				} else {
					deviceReceiver = device_Device_xref_reciever.getSensorUuid();
				}
			}

			Device_Device_xref device_Device_xref = null;
			// Find Device Sensor Mapping for STE Sensor
			if (vendors != null && vendors.equalsIgnoreCase("psi")) {
				device_Device_xref = deviceDeviceXrefRepository.findSensorUuidByProductCodeAndDeviceUuid(
						retriveDeviceSensorRequest.getDeviceUuid(), PSI_PRODUCT_CODE);
			} else {
				device_Device_xref = deviceDeviceXrefRepository.findSensorUuidByProductCodeAndDeviceUuid(
						retriveDeviceSensorRequest.getDeviceUuid(), STE_PRODUCT_CODE);
			}
			if (AppUtility.isEmpty(device_Device_xref)) {
				if (vendors != null && vendors.equalsIgnoreCase("psi")) {
					logger.info("TPSM PSI vendor sensor not availble, Adding sensor");
					device = addSensor(PSI_PRODUCT_CODE, PSI_PRODUCT_NAME, retriveDeviceSensorRequest);
					logger.info("TPSM PSI vendor sensor not availble, Adding Device sensor Mapping STE");
				} else {
					logger.info("TPSM STE vendor sensor not availble, Adding sensor");
					device = addSensor(STE_PRODUCT_CODE, STE_PRODUCT_NAME, retriveDeviceSensorRequest);
					logger.info("TPSM STE vendor sensor not availble, Adding Device sensor Mapping STE");
				}
				addDeviceSensorMapping(retriveDeviceSensorRequest, device);
			} else {
				device = device_Device_xref.getSensorUuid();
			}
			if (!(array1 != null && array1.length > 1)) {
				// If the Device response code with Sensor value 0 then we manually add sensor
				addSenorDataForZeroSensor(device, status, deviceResponse, retriveDeviceSensorRequest.getDeviceId(),
						STE_PRODUCT_CODE, deviceReceiver, vendors);
				return new MessageDTO<>("DataSavedSuccessfully", true);
			}
			logger.info("Fetch STE sensor Mappinng using device Uuid :" + retriveDeviceSensorRequest.getDeviceUuid()
					+ " and Product Code");
			// Find Device Sensor Mapping for AIR Sensor
			Device_Device_xref device_Device_xref_air = null;

			// Find Device Sensor Mapping for PSI Sensor
			if (vendors != null && vendors.equalsIgnoreCase("psi")) {
				device_Device_xref_air = deviceDeviceXrefRepository.findSensorUuidByProductCodeAndDeviceUuid(
						retriveDeviceSensorRequest.getDeviceUuid(), PSI_AIR_PRODUCT_CODE);
			} else {
				device_Device_xref_air = deviceDeviceXrefRepository.findSensorUuidByProductCodeAndDeviceUuid(
						retriveDeviceSensorRequest.getDeviceUuid(), AIR_PRODUCT_CODE);
			}

			// Find Device Sensor Mapping for ATIS Sensor
			Device_Device_xref device_Device_xref_atis = null;

			// Find Device Sensor Mapping for PSI Sensor
			if (vendors != null && vendors.equalsIgnoreCase("psi")) {
				device_Device_xref_atis = deviceDeviceXrefRepository.findSensorUuidByProductCodeAndDeviceUuid(
						retriveDeviceSensorRequest.getDeviceUuid(), PSI_ATIS_PRODUCT_CODE);
			} else {
				device_Device_xref_atis = deviceDeviceXrefRepository.findSensorUuidByProductCodeAndDeviceUuid(
						retriveDeviceSensorRequest.getDeviceUuid(), ATIS_PRODUCT_CODE);
			}

			// Find Device Sensor Mapping for WheelEdd Sensor
			if (deviceResponse.contains("0x41")) {
				Device_Device_xref device_Device_xref_wheel_end = deviceDeviceXrefRepository
						.findSensorUuidByProductCodeAndDeviceUuid(retriveDeviceSensorRequest.getDeviceUuid(),
								WHEEL_END_PRODUCT_CODE);
				if (AppUtility.isEmpty(device_Device_xref_wheel_end)) {
					logger.info("Wheel ENd sensor not availble, Adding sensor");
					deviceWheelEnd = addSensor(WHEEL_END_PRODUCT_CODE, WHEEL_END_PRODUCT_NAME,
							retriveDeviceSensorRequest);
					logger.info("Wheel end sensor not avvailble, Adding Device sensor Mapping Wheel End");
					addDeviceSensorMapping(retriveDeviceSensorRequest, deviceWheelEnd);
				} else {
					deviceWheelEnd = device_Device_xref_wheel_end.getSensorUuid();
				}
			}

			if (AppUtility.isEmpty(device_Device_xref_air)) {
				if (vendors != null && vendors.equalsIgnoreCase("psi")) {
					logger.info("PSI Air Tank sensor not availble, Adding sensor");
					deviceAir = addSensor(PSI_AIR_PRODUCT_CODE, PSI_AIR_PRODUCT_NAME, retriveDeviceSensorRequest);
					logger.info("PSI Air Tank sensor not avvailble, Adding Device sensor Mapping  PSI Air Tank");
				} else {
					logger.info("Air Tank sensor not availble, Adding sensor");
					deviceAir = addSensor(AIR_PRODUCT_CODE, AIR_PRODUCT_NAME, retriveDeviceSensorRequest);
					logger.info("Air Tank sensor not avvailble, Adding Device sensor Mapping Air Tank");
				}
				addDeviceSensorMapping(retriveDeviceSensorRequest, deviceAir);
			} else {
				deviceAir = device_Device_xref_air.getSensorUuid();
			}

			if (AppUtility.isEmpty(device_Device_xref_atis)) {
				if (vendors != null && vendors.equalsIgnoreCase("psi")) {
					logger.info("ATIS Regulator sensor not avvailble, Adding sensor");
					deviceAtis = addSensor(PSI_ATIS_PRODUCT_CODE, ATIS_PRODUCT_NAME, retriveDeviceSensorRequest);
					logger.info("ATIS Regulator sensor not avvailble, Adding Device sensor Mapping ATIS Regulator");
				} else {
					logger.info("ATIS Regulator sensor not avvailble, Adding sensor");
					deviceAtis = addSensor(ATIS_PRODUCT_CODE, ATIS_PRODUCT_NAME, retriveDeviceSensorRequest);
					logger.info("ATIS Regulator sensor not avvailble, Adding Device sensor Mapping ATIS Regulator");
				}
				addDeviceSensorMapping(retriveDeviceSensorRequest, deviceAtis);
			} else {
				deviceAtis = device_Device_xref_atis.getSensorUuid();
			}

//			}
			// Here we are parsing the device response and adding to DB
			if (!AppUtility.isEmpty(device) && !AppUtility.isEmpty(deviceAir) && !AppUtility.isEmpty(deviceAtis)) {
				logger.info("Going to parse the device response sensor data");
				parseSensorResponseAndStoreInDB(deviceResponse, retriveDeviceSensorRequest.getDeviceId(),
						device.getProductCode(), device, deviceAir, deviceAtis, deviceReceiver, deviceWheelEnd, status,
						vendors);
			}
		}
		return new MessageDTO<>("DataSavedSuccessfully", true);
	}

	/**
	 * This method is used for adding sensor manually when sensor online but 0
	 * sensor value
	 * 
	 * @param device
	 * @param status
	 * @param deviceResponse
	 * @param deviceId
	 * @param productCode
	 * @param deviceReceiver
	 * @param vendor
	 */
	public void addSenorDataForZeroSensor(Device device, String status, String deviceResponse, String deviceId,
			String productCode, Device deviceReceiver, String vendor) {
		List<SensorDetail> sensorDetailList = sensorDetailRepository.findListBySensorUuid(device.getUuid());
		List<SensorDetail> sensorDetailListReceiver = null;

		// If Vendor is PSI then no need to Add Receiver Sensor in Table
		if (vendor != null && !vendor.equalsIgnoreCase("psi")) {
			if (deviceReceiver != null) {
				sensorDetailListReceiver = sensorDetailRepository.findListBySensorUuid(deviceReceiver.getUuid());
			}
			SensorDetail sensorDetailForReceiver = new SensorDetail();
			if (AppUtility.isEmpty(sensorDetailListReceiver)) {
				logger.info("Sensor not availble for Receiver, creating new object");
				sensorDetailListReceiver = new ArrayList<>();
			} else if (sensorDetailListReceiver.size() > 0) {
				sensorDetailForReceiver = sensorDetailListReceiver.get(0);
			}
			sensorDetailForReceiver.setVendor(vendor);
			sensorDetailForReceiver.setStatus(status);
			sensorDetailForReceiver.setDeviceId(deviceId);
			sensorDetailForReceiver.setProductCode(productCode);
			if (AppUtility.isEmpty(sensorDetailForReceiver.getUuid())) {
				sensorDetailForReceiver.setSensorUUID(deviceReceiver);
				boolean isDeviceUuidUnique = false;
				String sensorUuid = "";
				while (!isDeviceUuidUnique) {
					sensorUuid = UUID.randomUUID().toString();
					SensorDetail byUuid = sensorDetailRepository.findByUuid(sensorUuid);
					if (byUuid == null) {
						isDeviceUuidUnique = true;
					}
				}
				sensorDetailForReceiver.setUuid(sensorUuid);
			}
			sensorDetailListReceiver.add(sensorDetailForReceiver);
		}

		boolean sensorNotAvailable = false;
		if (AppUtility.isEmpty(sensorDetailList)) {
			logger.info("Sensor not availble for tpms, creating new object");
			sensorDetailList = new ArrayList<>();
			sensorNotAvailable = true;
		}
		// Here we are adding sensor for all 8 location manually
		for (int i = 0; i < 8; i++) {
			String location = "";
			switch (i) {
			case 0:
				location = "0x21";
				break;
			case 1:
				location = "0x22";
				break;
			case 2:
				location = "0x23";
				break;
			case 3:
				location = "0x24";
				break;
			case 4:
				location = "0x25";
				break;
			case 5:
				location = "0x26";
				break;
			case 6:
				location = "0x27";
				break;
			case 7:
				location = "0x28";
				break;

			default:
				break;
			}

			final String locations = location;
			SensorDetail sensorDetail = new SensorDetail();
			if (sensorDetailList != null && sensorDetailList.size() > 0 && !sensorNotAvailable) {
				logger.info("Find Sensor by location");
				List<SensorDetail> details = sensorDetailList.stream().filter(e -> e.getPosition().equals(locations))
						.collect(Collectors.toList());
				if (!AppUtility.isEmpty(details) && details.size() > 0) {
					sensorDetail = details.get(0);
				}
			}
			sensorDetail.setVendor(vendor);
			sensorDetail.setPosition(location);
//				sensorDetail.setStatus(status);
			sensorDetail.setDeviceId(deviceId);
			sensorDetail.setProductCode(productCode);
			if (AppUtility.isEmpty(sensorDetail.getUuid())) {
				sensorDetail.setSensorUUID(device);
				boolean isDeviceUuidUnique = false;
				String sensorUuid = "";
				while (!isDeviceUuidUnique) {
					sensorUuid = UUID.randomUUID().toString();
					SensorDetail byUuid = sensorDetailRepository.findByUuid(sensorUuid);
					if (byUuid == null) {
						isDeviceUuidUnique = true;
					}
				}
				sensorDetail.setUuid(sensorUuid);
			}
			sensorDetailList.add(sensorDetail);
		}
		// If Vendor is PSI then no need to Add Receiver Sensor in Table
		if (!AppUtility.isEmpty(sensorDetailListReceiver) && !vendor.equalsIgnoreCase("psi")) {
			sensorDetailRepository.saveAll(sensorDetailListReceiver);
		}
		if (!AppUtility.isEmpty(sensorDetailList)) {
			sensorDetailRepository.saveAll(sensorDetailList);
		}
	}

	/**
	 * This method is used for Adding the Device sensor Mapping
	 * 
	 * @param retriveDeviceSensorRequest
	 * @param device
	 */
	public void addDeviceSensorMapping(RetriveDeviceSensorRequest retriveDeviceSensorRequest, Device device) {
		logger.info("Inside addDeviceSensorMapping method");
		Device_Device_xref device_Device_xref = new Device_Device_xref();
		Device deviceDevice = deviceRepository.findByUuid(retriveDeviceSensorRequest.getDeviceUuid());
		device_Device_xref.setDeviceUuid(deviceDevice);
		device_Device_xref.setSensorUuid(device);
		device_Device_xref.setActive(true);
		device_Device_xref = deviceDeviceXrefRepository.save(device_Device_xref);
	}

	/**
	 * This method is used for adding the Sensor Details
	 * 
	 * @param productCode
	 * @param productName
	 * @param retriveDeviceSensorRequest
	 * @return
	 */
	public Device addSensor(String productCode, String productName,
			RetriveDeviceSensorRequest retriveDeviceSensorRequest) {
		logger.info("Inside addSensor method ");
		Device device = new Device();
		device.setProductCode(productCode);
		device.setProductName(productName);
		device.setStatus(DeviceStatus.ACTIVE);
		device.setOrganisation(null);
		boolean isDeviceUuidUnique = false;
		String deviceUuid = "";
		while (!isDeviceUuidUnique) {
			deviceUuid = UUID.randomUUID().toString();
			Device byUuid = deviceRepository.findByUuid(deviceUuid);
			if (byUuid == null) {
				isDeviceUuidUnique = true;
			}
		}
		device.setUuid(deviceUuid);
		device = deviceRepository.save(device);
		return device;
	}

	/**
	 * This method is used for Parsing the AT command Device response and save into
	 * sensor_details table
	 * 
	 * @param deviceResponse
	 * @param deviceId
	 * @param productCode
	 * @param device
	 * @param deviceAir
	 * @param deviceAtis
	 * @param deviceReceiver
	 * @param deviceWheelEnd
	 * @param status
	 * @param vendor
	 * @return
	 */
	public String parseSensorResponseAndStoreInDB(String deviceResponse, String deviceId, String productCode,
			Device device, Device deviceAir, Device deviceAtis, Device deviceReceiver, Device deviceWheelEnd,
			String status, String vendor) {
		logger.info("Inside parseSensorResponseAndStoreInDB method ");
		List<SensorDetail> sensorDetailList = sensorDetailRepository.findListBySensorUuid(device.getUuid());
		List<SensorDetail> sensorDetailListAir = sensorDetailRepository.findListBySensorUuid(deviceAir.getUuid());
		List<SensorDetail> sensorDetailListAtis = sensorDetailRepository.findListBySensorUuid(deviceAtis.getUuid());
		List<SensorDetail> sensorDetailListReceiver = null;
		List<SensorDetail> sensorDetailListWheelEnd = null;

		if (deviceWheelEnd != null) {
			sensorDetailListWheelEnd = sensorDetailRepository.findListBySensorUuid(deviceAtis.getUuid());
			if (AppUtility.isEmpty(sensorDetailListWheelEnd)) {
				logger.info("Sensor not availble for wheel end, creating new object");
				sensorDetailListWheelEnd = new ArrayList<>();
			}
		}

		if (AppUtility.isEmpty(sensorDetailList)) {
			logger.info("Sensor not availble for tpms, creating new object");
			sensorDetailList = new ArrayList<>();
		}
		if (AppUtility.isEmpty(sensorDetailListAir)) {
			logger.info("Sensor not availble for Air tank, creating new object");
			sensorDetailListAir = new ArrayList<>();
		}
		if (AppUtility.isEmpty(sensorDetailListAtis)) {
			logger.info("Sensor not availble ATIS Regulator, creating new object");
			sensorDetailListAtis = new ArrayList<>();
		}

		if (deviceResponse != null) {
			if (vendor != null && !vendor.equalsIgnoreCase("psi")) {
				sensorDetailListReceiver = sensorDetailRepository.findListBySensorUuid(deviceReceiver.getUuid());
				SensorDetail sensorDetailForReceiver = new SensorDetail();
				if (AppUtility.isEmpty(sensorDetailListReceiver)) {
					logger.info("Sensor not availble for Receiver, creating new object");
					sensorDetailListReceiver = new ArrayList<>();
				} else if (sensorDetailListReceiver.size() > 0) {
					sensorDetailForReceiver = sensorDetailListReceiver.get(0);
				}
				sensorDetailForReceiver.setVendor(vendor);
				sensorDetailForReceiver.setStatus(status);
				sensorDetailForReceiver.setDeviceId(deviceId);
				sensorDetailForReceiver.setProductCode(productCode);
				if (AppUtility.isEmpty(sensorDetailForReceiver.getUuid())) {
					sensorDetailForReceiver.setSensorUUID(deviceReceiver);
					boolean isDeviceUuidUnique = false;
					String sensorUuid = "";
					while (!isDeviceUuidUnique) {
						sensorUuid = UUID.randomUUID().toString();
						SensorDetail byUuid = sensorDetailRepository.findByUuid(sensorUuid);
						if (byUuid == null) {
							isDeviceUuidUnique = true;
						}
					}
					sensorDetailForReceiver.setUuid(sensorUuid);
				}
				sensorDetailListReceiver.add(sensorDetailForReceiver);
			}

			String[] array1 = deviceResponse.split("Loc:");
			if (array1 != null && array1.length > 1) {
				logger.info("Successfully Splited by Location");
				String s1 = array1[1];
				String[] array2 = s1.split("[\\r\\n]+");
				for (int j = 1; j < array2.length; j++) {
					String[] array3 = array2[j].split(":");
					if (array3.length > 1) {
						String location = array3[0].trim();
						String[] array4 = array3[1].split(",");
						SensorDetail sensorDetail = new SensorDetail();
						sensorDetail.setProductCode(productCode);
						if (location.equals("0x49")) {
							sensorDetail.setProductCode(deviceAir.getProductCode());
							if (sensorDetailListAir != null && sensorDetailListAir.size() > 0) {
								logger.info("Find Sensor by location");
								List<SensorDetail> details = sensorDetailListAir.stream()
										.filter(e -> e.getPosition().equals(location)).collect(Collectors.toList());
								if (!AppUtility.isEmpty(details) && details.size() > 0) {
									sensorDetail = details.get(0);
								}
							}
						} else if (location.equals("0x4A")) {
							sensorDetail.setProductCode(deviceAtis.getProductCode());
							if (sensorDetailListAtis != null && sensorDetailListAtis.size() > 0) {
								logger.info("Find Sensor by location");
								List<SensorDetail> details = sensorDetailListAtis.stream()
										.filter(e -> e.getPosition().equals(location)).collect(Collectors.toList());
								if (!AppUtility.isEmpty(details) && details.size() > 0) {
									sensorDetail = details.get(0);
								}
							}
						} else if (deviceWheelEnd != null && location.equals("0x41") || location.equals("0x42")
								|| location.equals("0x43") || location.equals("0x44")) {
							sensorDetail.setProductCode(deviceWheelEnd.getProductCode());
							if (sensorDetailListWheelEnd != null && sensorDetailListWheelEnd.size() > 0) {
								logger.info("Find Sensor by location");
								List<SensorDetail> details = sensorDetailListWheelEnd.stream()
										.filter(e -> (e.getPosition() != null && e.getPosition().equals(location)))
										.collect(Collectors.toList());
								if (!AppUtility.isEmpty(details) && details.size() > 0) {
									sensorDetail = details.get(0);
								}
							}
						} else {
							if (sensorDetailList != null && sensorDetailList.size() > 0) {
								logger.info("Find Sensor by location");
								List<SensorDetail> details = sensorDetailList.stream()
										.filter(e -> e.getPosition().equals(location)).collect(Collectors.toList());
								if (!AppUtility.isEmpty(details) && details.size() > 0) {
									sensorDetail = details.get(0);
								}
							}
						}
						sensorDetail.setDeviceId(deviceId);
//						sensorDetail.setStatus(status);
						sensorDetail.setVendor(vendor);
						if (AppUtility.isEmpty(sensorDetail.getUuid())) {
							sensorDetail.setPosition(location);
							if (location.equals("0x49")) {
								sensorDetail.setSensorUUID(deviceAir);
							} else if (location.equals("0x4A")) {
								sensorDetail.setSensorUUID(deviceAtis);
							} else if (deviceWheelEnd != null && location.equals("0x41") || location.equals("0x42")
									|| location.equals("0x43") || location.equals("0x44")) {
								sensorDetail.setSensorUUID(deviceWheelEnd);
							} else {
								sensorDetail.setSensorUUID(device);
							}
							boolean isDeviceUuidUnique = false;
							String sensorUuid = "";
							while (!isDeviceUuidUnique) {
								sensorUuid = UUID.randomUUID().toString();
								SensorDetail byUuid = sensorDetailRepository.findByUuid(sensorUuid);
								if (byUuid == null) {
									isDeviceUuidUnique = true;
								}
							}
							sensorDetail.setUuid(sensorUuid);
						}
						for (int k = 0; k < array4.length; k++) {
							switch (k) {
							case 0:
								sensorDetail.setSensorId(array4[0].trim());
								break;
							case 1:
								sensorDetail.setStatus(array4[1].trim());
								break;
							case 2:
								if (!AppUtility.isEmpty(array4[2])) {
									String[] values = array4[2].split("m");
									if (values != null && values.length > 0) {
										try {
											int mbar = Integer.parseInt(values[0].trim());
											sensorDetail.setSensorPressure(
													String.valueOf(Math.round(mbar / 68.948)) + "PSI");
										} catch (Exception e) {
											// TODO: handle exception
										}
									}
								}
								break;
							case 3:
								sensorDetail.setSensorTemperature(array4[3]);
								if (!AppUtility.isEmpty(array4[3])) {
									String[] values = array4[3].split("C");
									if (values != null && values.length > 0) {
										try {
											double celsius = (Integer.parseInt(values[0].trim()) * 1.8) + 32;
											sensorDetail
													.setSensorTemperature(String.valueOf(Math.round(celsius)) + "F");
										} catch (Exception e) {
											// TODO: handle exception
										}
									}
								}
								break;
//							case 4:
//								sensorObj.put("battery", array4[4]);
//								break;
							default:
								break;
							}
						}
						if (location.equals("0x49")) {
							sensorDetailListAir.add(sensorDetail);
						} else if (location.equals("0x4A")) {
							sensorDetailListAtis.add(sensorDetail);
						} else if (deviceWheelEnd != null && location.equals("0x41") || location.equals("0x42")
								|| location.equals("0x43") || location.equals("0x44")) {
							sensorDetailListWheelEnd.add(sensorDetail);
						} else {
							sensorDetailList.add(sensorDetail);
						}

					}
				}
				if (!AppUtility.isEmpty(sensorDetailListReceiver)) {
					sensorDetailRepository.saveAll(sensorDetailListReceiver);
				}
				if (!AppUtility.isEmpty(sensorDetailList)) {
					sensorDetailRepository.saveAll(sensorDetailList);
				}
				if (!AppUtility.isEmpty(sensorDetailListAir)) {
					sensorDetailRepository.saveAll(sensorDetailListAir);
				}
				if (!AppUtility.isEmpty(sensorDetailListAtis)) {
					sensorDetailRepository.saveAll(sensorDetailListAtis);
				}
				if (!AppUtility.isEmpty(sensorDetailListWheelEnd) && deviceWheelEnd != null) {
					sensorDetailRepository.saveAll(sensorDetailListWheelEnd);
				}
			} else {
				return "No Sensor Data Found";
			}

		} else {
			return "No Sensor Data Found";
		}
		return deviceResponse;
	}
	
	
	@Override
    public Boolean updateSensorMacAddress(UpdateMacAddressRequest updateMacAddressRequest, String userName) {
		User user = restUtils.getUserFromAuthService(userName);
        Device sensor = deviceRepository.findByUuid(updateMacAddressRequest.getUuid());
        sensor.setMacAddress(updateMacAddressRequest.getMacAddress());
        sensor.setUpdatedOn(Instant.ofEpochMilli(Long.parseLong(updateMacAddressRequest.getDatetimeRT())));
        sensor.setUpdatedBy(user);
        sensor = deviceRepository.save(sensor);
        return true;
    }
	
}
