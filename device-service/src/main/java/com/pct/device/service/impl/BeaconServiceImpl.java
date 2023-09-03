package com.pct.device.service.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

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
import com.pct.common.model.Organisation;
import com.pct.common.model.User;
import com.pct.common.util.JwtUtil;
import com.pct.device.exception.DeviceException;
import com.pct.device.payload.BeaconDetailPayLoad;
import com.pct.device.payload.BeaconPayload;
import com.pct.device.payload.BeaconRequestPayload;
import com.pct.device.payload.SensorPayLoad;
import com.pct.device.repository.IDeviceRepository;
import com.pct.device.service.IBeaconService;
import com.pct.device.specification.BeaconSpecification;
import com.pct.device.specification.DeviceSpecification;
import com.pct.device.specification.SensorSpecification;
import com.pct.device.util.BeanConverter;
import com.pct.device.util.RestUtils;

@Service
public class BeaconServiceImpl implements IBeaconService {
	Logger logger = LoggerFactory.getLogger(BeaconServiceImpl.class);

	@Autowired
	private IDeviceRepository deviceRepository;

	@Autowired
	private BeanConverter beanConverter;

	@Autowired
	private RestUtils restUtils;
	@Autowired
	private JwtUtil jwtUtil;

	@Override
	public Boolean addBeaconDetail(BeaconRequestPayload beaconRequestRequest, String userName) throws DeviceException {
		logger.info("Inside addBeaconDetail and fetching beaconDetail and userId value",
				beaconRequestRequest + " " + userName);
		String can = beaconRequestRequest.getCan();
		Organisation company = restUtils.getCompanyFromCompanyService(can);
		User user = restUtils.getUserFromAuthService(userName);
		Device deviceMacAddress = deviceRepository.findByMac_address(beaconRequestRequest.getMacAddress());
		String macAddress = beaconRequestRequest.getMacAddress();
		if (company != null) {
			if (macAddress != null) {
				if (deviceMacAddress == null) {
					Device device = new Device();
					device.setOrganisation(company);
					device.setProductCode(beaconRequestRequest.getProductCode());
					device.setProductName(beaconRequestRequest.getProductName());
					device.setSon(beaconRequestRequest.getSon());
					device.setStatus(DeviceStatus.PENDING);
					device.setCreatedAt(Instant.now());
					device.setEpicorOrderNumber(beaconRequestRequest.getEpicorOrderNumber());
					device.setMacAddress(beaconRequestRequest.getMacAddress());
					boolean isDeviceUuidUnique = false;
					String deviceUuid = "";
					while (!isDeviceUuidUnique) {
						deviceUuid = UUID.randomUUID().toString();
						Device byUuid = deviceRepository.findByUuid(deviceUuid);
						if (byUuid == null) {
							isDeviceUuidUnique = true;
						}
					}
					device.setIotType(IOTType.BEACON);
					device.setCreatedBy(user);
					device.setCreatedAt(Instant.now());					
					device.setUuid(deviceUuid);
					deviceRepository.save(device);
					logger.info("Beacon details saved successfully");

				} else {
					throw new DeviceException("MAC Address already exist");
				}
			} else {
				throw new DeviceException("MAC Address can not be null");
			}
		} else {
			throw new DeviceException("No Company found for account number " + beaconRequestRequest.getCan());
		}
		return Boolean.TRUE;
	}

	@Override
	public Page<BeaconPayload> getBeaconWithPagination(String accountNumber, String uuid, DeviceStatus status,
			String mac, Map<String, String> filterValues, Pageable pageable) {
		Page<Device> deviceDetails = null;
		IOTType type = IOTType.getValue("Beacon");
		Specification<Device> spc = BeaconSpecification.getBeaconListSpecification(accountNumber, uuid, status, mac,
				filterValues, type);
		deviceDetails = deviceRepository.findAll(spc, pageable);
		logger.info("Fetching beacon details based on specification");
		Page<BeaconPayload> beaconRecordloadPage = null;
		if (deviceDetails.getNumberOfElements() > 0) {
			beaconRecordloadPage = beanConverter.convertBeaconToBeaconPayLoad(deviceDetails, pageable);
			logger.info("Fetching sensor details list" + beaconRecordloadPage);
		} else {
			throw new DeviceException("No beacon details found for account number: " + accountNumber);
		}

		return beaconRecordloadPage;
	}

	@Override
	public boolean deleteBeaconDetail(String can, String uuid) {
		logger.info("Inside deleteBeaconDetail for can: " + can + " uuid: " + uuid);
		if (can != null && !can.isEmpty()) {
			Organisation company = restUtils.getCompanyFromCompanyService(can); // Company will be used when deleting
																				// Install history
			IOTType type = IOTType.getValue("Beacon");
			DeviceStatus dStatus = DeviceStatus.getGatewayStatusInSearch("PENDING");
			Specification<Device> spc = SensorSpecification.getBeaconSpec(can, uuid, type);
			List<Device> deviceList = deviceRepository.findAll(spc);
			logger.info("After fetching beacon details" + deviceList.toString());
			AtomicBoolean status = new AtomicBoolean(true);
			if (deviceList.size() > 0) {
				deviceList.forEach(device -> {
					if (!device.getStatus().equals(dStatus)) {
						throw new DeviceException("Device is not in pending state.");
					} else {
						status.set(status.get() && deleteBeaconData(device));
						logger.info("Beacon deleted Successfully");
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

	private Boolean deleteBeaconData(Device device) {
		deviceRepository.delete(device);
		return true;

	}

	@Override
	public Boolean updateBeaconDetail(BeaconDetailPayLoad beaconDetailPayload, String userName) {
		logger.info("Inside updateBeaconDetail and fetching beaconDetail and userId value" + beaconDetailPayload + " "
				+ userName);
		User user = restUtils.getUserFromAuthService(userName);
		Device device = deviceRepository.findByUuid(beaconDetailPayload.getUuid());
		logger.info("Fetching beacon details for uuid : " + beaconDetailPayload.getUuid());
		if (device != null) {
			device.setProductCode(beaconDetailPayload.getProductCode());
			device.setProductName(beaconDetailPayload.getProductName());
			device.setSon(beaconDetailPayload.getSon());
			
			device.setUpdatedAt(Instant.now());
			device.setUpdatedBy(user);
			
			device.setEpicorOrderNumber(beaconDetailPayload.getEpicorOrderNumber());
			device.setMacAddress(beaconDetailPayload.getMacAddress());
			device.setQuantityShipped(beaconDetailPayload.getQuantityShipped());
			deviceRepository.save(device);
			logger.info("Beacon updated for the uuid : " + beaconDetailPayload.getUuid());
		} else {
			throw new DeviceException("Beacon not found for the uuid " + beaconDetailPayload.getUuid());
		}
		return Boolean.TRUE;
	}

	@Override
	public List<BeaconPayload> getBeaconDetails(String can, String uuid) {
		logger.info("Inside getBeaconDetails for account number " + can + " uuid " + uuid);
		List<BeaconPayload> beaconDetailList = new ArrayList<>();
		List<Device> deviceList = new ArrayList<Device>();
		IOTType type = IOTType.getValue("Beacon");
		Specification<Device> spc = SensorSpecification.getBeaconSpec(can, uuid, type);
		logger.info("Fetching device details based on specification.");
		deviceList = deviceRepository.findAll(spc);
		if (deviceList.size() > 0) {
			deviceList.forEach(device -> {
				beaconDetailList.add(beanConverter.convertBeaconToBeaconPayLoad(device));
			});
		} else {
			throw new DeviceException("No device found for uuid/can number");
		}
		return beaconDetailList;
	}
}
