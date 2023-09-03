package com.pct.device.service.impl;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.pct.common.constant.DeviceStatus;
import com.pct.common.constant.IOTType;
import com.pct.common.model.Cellular;
import com.pct.common.model.Device;
import com.pct.common.model.DeviceCompany;
import com.pct.common.model.DeviceData;
import com.pct.common.model.DeviceDetails;
import com.pct.common.model.DeviceQa;
import com.pct.common.model.FailedDeviceQA;
import com.pct.common.model.Organisation;
import com.pct.common.model.User;
import com.pct.common.util.JwtUtil;
import com.pct.device.exception.DeviceException;
import com.pct.device.payload.DeviceQaRequest;
import com.pct.device.payload.DeviceQaResponse;
import com.pct.device.repository.DeviceQARepository;
import com.pct.device.repository.FailedDeviceQARepository;
import com.pct.device.repository.ICellularRepository;
import com.pct.device.repository.IDeviceCompanyRepository;
import com.pct.device.repository.IDeviceRepository;
import com.pct.device.repository.RedisDeviceRepository;
import com.pct.device.service.IDeviceQAService;
import com.pct.device.util.BeanConverter;
import com.pct.device.util.RestUtils;

@Service
public class DeviceQAServiceImpl implements IDeviceQAService {
	Logger logger = LoggerFactory.getLogger(DeviceQAServiceImpl.class);

	public static final String DEVICE_CURRENT_VIEW_PREFIX = "deviceData:";
	private static final List<String> DEVICE_INFO_FIXED_FIELDS = new ArrayList() {
		{
			add("customerId");
			add("deviceType");
		}
	};

	@Autowired
	private DeviceQARepository deviceQARepository;

	@Autowired
	private FailedDeviceQARepository failedDeviceQARepository;

	@Autowired
	private IDeviceRepository deviceRepository;

	@Autowired
	private IDeviceCompanyRepository companyDeviceRepo;

	@Autowired
	private BeanConverter beanConverter;

	@Autowired
	private RestUtils restUtils;

	@Autowired
	private ICellularRepository cellularRepository;

	@Autowired
	RedisDeviceRepository redisDeviceRepository;

	@Autowired
	private JwtUtil jwtUtil;

	private static final Logger LOGGER = LoggerFactory.getLogger(DeviceForwardingServiceImpl.class);

	@Override
	public Boolean addQaDeviceDetail(DeviceQaRequest deviceQaRequest) throws DeviceException {

		Device deviceId = deviceRepository.findByImei(deviceQaRequest.getImei());
		DeviceQa deviceQaRequests = new DeviceQa();
		if (deviceId != null && deviceQaRequest != null) {

			deviceQaRequests.setUuid(deviceQaRequest.getUuid());
			deviceQaRequests.setQaResult(deviceQaRequest.getQaResult());
			deviceQaRequests.setQaStatus(deviceQaRequest.getQaStatus());
			deviceQaRequests.setQaDate(deviceQaRequest.getQaDate());

			// BeanUtils.copyProperties(deviceQaRequests, deviceQaRequest);
			deviceQaRequests.setDeviceId(deviceId);
			deviceQARepository.save(deviceQaRequests);
			logger.info("Device Qa Details saved successfully");

		} else {
			throw new DeviceException("deviceId / deviceQaRequest  can not be null");
		}
		return Boolean.TRUE;
	}

	@Override
	public List<DeviceQaResponse> getAllDeviceQa() throws Exception {
		List<DeviceQaResponse> list = null;
		try {
			LOGGER.info("inside getAllDeviceQa()");
			List<DeviceQa> al = deviceQARepository.findAll();
			System.out.println("data:" + al);
			if (al != null && al.isEmpty()) {
				throw new Exception("No such DeviceQa found");
			}
			list = beanConverter.convertDeviceQAToDeviceQAResponse(al);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	@Transactional
	@Override
	public boolean updateDeviceQa(DeviceQaRequest df) throws Exception {
		LOGGER.info("Inside updateDeviceQa detail");
		if (df != null) {
			DeviceQa deviceQa = beanConverter.convertDeviceQaRequestToDeviceQa(df);
			System.out.println("deviceQa.getUuid():" + deviceQa.getUuid());
			DeviceQa byUuid = deviceQARepository.findByUuid(deviceQa.getUuid());
			if (byUuid != null) {
				deviceQARepository.update(deviceQa.getQaResult(), deviceQa.getQaStatus(),
						deviceQa.getQaDate().toString(), deviceQa.getUuid());
				LOGGER.info("DeviceQa updated for the uuid:" + deviceQa.getUuid());
			} else {
				throw new Exception("DeviceQa not found");
			}
		} else {
			throw new Exception("DeviceQA JSON Can't be NULL");
		}
		return Boolean.TRUE;
	}

	@Override
	public String addQADeviceDetail(HttpServletRequest request, String userName) {
		DeviceData deviceFromRequest = null;
		try {
			GsonBuilder gsonBuilder = new GsonBuilder();
			gsonBuilder.setDateFormat("yyyy-MM-dd hh:mm:ss");
			Gson gson = gsonBuilder.create();
			deviceFromRequest = gson.fromJson(request.getReader(), DeviceData.class);
			logger.info("Inside addQADeviceDetail");
			Device device = new Device();
			logger.info("Get Device By Imei:-" + deviceFromRequest.getDeviceID());
			Device byImei = deviceRepository.findByImei(deviceFromRequest.getDeviceID());
			logger.info("Get User By username");
			User user = restUtils.getUserFromAuthService(userName);
			logger.info("Get Device Company By owner level 2:-" + deviceFromRequest.getOwnerLevel2());
			DeviceCompany deviceCompany = companyDeviceRepo
					.findDeviceCompanyByMs1OrganisationName(deviceFromRequest.getOwnerLevel2());
			if (deviceCompany == null) {
				logger.info("organisation found from the table");
				deviceCompany = new DeviceCompany();
				deviceCompany.setCan("X-");
			}
			logger.info("Get Organisation By can number:-" + deviceCompany.getCan());
			Organisation company = restUtils.getCompanyFromCompanyService(deviceCompany.getCan());
			logger.info("organisation object got");
			if (byImei == null) {
				boolean isDeviceQAUuidUnique = false;
				String deviceQaUuid = "";
				while (!isDeviceQAUuidUnique) {
					deviceQaUuid = UUID.randomUUID().toString();
					DeviceQa byUuid = deviceQARepository.findByUuid(deviceQaUuid);
					if (byUuid == null) {
						isDeviceQAUuidUnique = true;
					}
				}
				boolean isDeviceUuidUnique = false;
				String deviceUuid = "";
				while (!isDeviceUuidUnique) {
					deviceUuid = UUID.randomUUID().toString();
					Device byUuid = deviceRepository.findByUuid(deviceUuid);
					if (byUuid == null) {
						isDeviceUuidUnique = true;
					}
				}
				logger.info("Prepare Device Object");
				logger.info("Set User");
				if (user != null && user.getUserName() != null) {
					logger.info("User object" + user.getUserName());
					device.setCreatedBy(user);
				}
				logger.info("Set CreatedAt value");
				device.setCreatedAt(Instant.now());

				device.setEpicorOrderNumber(deviceFromRequest.getSalesOrderID());
				device.setProductName(deviceFromRequest.getDeviceModel());
				device.setProductCode(deviceFromRequest.getProductCode());
				logger.info("Set getWaterfallDevdef_cfgCRC_value");
				logger.info("Set DeviceStatus value");
				device.setStatus(DeviceStatus.ACTIVE);
				device.setSon(deviceFromRequest.getSalesforceOrderNumber());
				logger.info("Set Qa Status");
				device.setQaStatus(deviceFromRequest.getQAStatus());
				if (deviceFromRequest.getQATimestampPST() != null) {
					device.setQaDate(deviceFromRequest.getQATimestampPST().toInstant());
				}
				device.setUuid(deviceUuid);
				device.setImei(deviceFromRequest.getDeviceID());
				String ownerLevel2 = deviceFromRequest.getOwnerLevel2();
				device.setOwnerLevel2(ownerLevel2);
				device.setIotType(IOTType.GATEWAY);
				device.setOrganisation(company);
				device.setUsageStatus(deviceFromRequest.getDeviceUsage());
				device.setDeviceType(deviceFromRequest.getDeviceModel());
				device.setComment(deviceFromRequest.getComment());
				device.setConfigName(deviceFromRequest.getConfigName());
//				DeviceDetails deviceDetails = new DeviceDetails();
//				deviceDetails.setImei(device.getImei());
//				deviceDetails.setUsageStatus(deviceFromRequest.getDeviceUsage());
//				device.setDeviceDetails(deviceDetails);
				logger.info("Prepare Cellular Object");
				Cellular cellular = new Cellular();
				cellular.setServiceCountry(deviceFromRequest.getServiceCountry());
				cellular.setPhone(deviceFromRequest.getDevicePhoneNum());
				cellular.setServiceNetwork(deviceFromRequest.getServiceNetwork());
				cellular.setImei(deviceFromRequest.getDeviceID());
				cellular.setCellular(deviceFromRequest.getDeviceSimNum());
				boolean isCellularUuidUnique = false;
				String cellularUuid = "";
				while (!isCellularUuidUnique) {
					cellularUuid = UUID.randomUUID().toString();
					Cellular byUuid = cellularRepository.findByUuid(cellularUuid);
					if (byUuid == null) {
						isCellularUuidUnique = true;
					}
				}
				cellular.setUuid(cellularUuid);
				logger.info("Save Cellular Details");
				cellularRepository.save(cellular);
				logger.info("Cellular Details saved successfully");
				device.setCellular(cellular);
				logger.info("For inserting QADevice Details");
				deviceRepository.save(device);
				logger.info("Device Details saved successfully");
				DeviceQa deviceQa = new DeviceQa();
				deviceQa.setQaResult(deviceFromRequest.getQAResult());
				deviceQa.setQaStatus(deviceFromRequest.getQAStatus());
				Timestamp now = Timestamp.from(Instant.now());
				deviceQa.setQaDate(now);
				deviceQa.setUuid(deviceQaUuid);
				deviceQa.setDeviceId(device);
				deviceQa.setCreatedOn(Instant.now());
				logger.info("Save Device Qa Details");
				deviceQa = deviceQARepository.save(deviceQa);
				logger.info("DeviceQA Details saved successfully, ", deviceQa.getId() + "--- " + deviceQa.getUuid());

				redisDeviceRepository.addMap(DEVICE_CURRENT_VIEW_PREFIX + device.getImei(), "customerId",
						deviceCompany.getCan());

				if (device.getProductName() != null) {
					redisDeviceRepository.addMap(DEVICE_CURRENT_VIEW_PREFIX + device.getImei(), "deviceType",
							device.getProductName());
				}
				ObjectMapper objectMapper = new ObjectMapper();
				objectMapper.registerModule(new JavaTimeModule());
				SimpleDateFormat FORMATTER = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
				objectMapper.setDateFormat(FORMATTER);
				String writeValueAsString = objectMapper.writeValueAsString(deviceQa);
				return writeValueAsString;
			} else {
				logger.info("Inside update QA and device details");
				byImei.setEpicorOrderNumber(deviceFromRequest.getEpicorOrderNumber());
				byImei.setProductName(deviceFromRequest.getDeviceModel());
				byImei.setProductCode(deviceFromRequest.getProductCode());
				byImei.setImei(deviceFromRequest.getDeviceID());

				byImei.setUpdatedBy(user);
				byImei.setUpdatedAt(Instant.now());

				byImei.setSon(deviceFromRequest.getSalesforceOrderNumber());
				byImei.setUpdatedAt(Instant.now());
				byImei.setIotType(IOTType.GATEWAY);
				byImei.setStatus(DeviceStatus.ACTIVE);
				byImei.setQaDate(deviceFromRequest.getQATimestampPST().toInstant());
				byImei.setQaStatus(deviceFromRequest.getQAStatus());
				byImei.setUsageStatus(deviceFromRequest.getDeviceUsage());
				byImei.setDeviceType(deviceFromRequest.getDeviceModel());
				String ownerLevel2 = deviceFromRequest.getOwnerLevel2();
				byImei.setOwnerLevel2(ownerLevel2);
				byImei.setOrganisation(company);
//				DeviceDetails deviceDetails = new DeviceDetails();
//				deviceDetails.setImei(byImei.getImei());
//				deviceDetails.setUsageStatus(deviceFromRequest.getDeviceUsage());
//				byImei.setDeviceDetails(deviceDetails);
				Cellular cellularPayload = cellularRepository.findByUuid(byImei.getCellular().getUuid());
				cellularPayload.setServiceCountry(deviceFromRequest.getServiceCountry());
				cellularPayload.setPhone(deviceFromRequest.getDevicePhoneNum());
				cellularPayload.setServiceNetwork(deviceFromRequest.getServiceNetwork());
				cellularPayload.setImei(deviceFromRequest.getDeviceID());
				cellularPayload.setCellular(deviceFromRequest.getDeviceSimNum());
				cellularRepository.save(cellularPayload);
				logger.info("Cellular Details updated successfully");
				byImei.setCellular(cellularPayload);
				deviceRepository.save(byImei);
				logger.info("Device Details updated successfully");

				List<DeviceQa> deviceQADetails = deviceQARepository.findByDeviceId(deviceFromRequest.getDeviceID());
				DeviceQa deviceQa = null;
				if (deviceQADetails != null && deviceQADetails.size() > 0) {
					deviceQADetails.get(0).setQaResult(deviceFromRequest.getQAResult());
					deviceQADetails.get(0).setQaStatus(deviceFromRequest.getQAStatus());
					deviceQADetails.get(0).setUpdatedOn(Instant.now());
					deviceQADetails.get(0).setQaDate(deviceFromRequest.getQATimestampPST());
					deviceQa = deviceQARepository.save(deviceQADetails.get(0));
					logger.info("DeviceQA Details saved successfully, ", deviceQa.getId() + "--- " + deviceQa.getUuid());
				} else {
					logger.info("No data found in Device QA");
					// throw new Exception("No data found in Device QA");
				}

				redisDeviceRepository.addMap(DEVICE_CURRENT_VIEW_PREFIX + byImei.getImei(), "customerId",
						deviceCompany.getCan());

				if (device.getProductName() != null) {
					redisDeviceRepository.addMap(DEVICE_CURRENT_VIEW_PREFIX + byImei.getImei(), "deviceType",
							byImei.getProductName());
				}
				if (deviceQa != null) {
					ObjectMapper objectMapper = new ObjectMapper();
					objectMapper.registerModule(new JavaTimeModule());
					SimpleDateFormat FORMATTER = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
					objectMapper.setDateFormat(FORMATTER);
					String writeValueAsString = objectMapper.writeValueAsString(deviceQa);
					return writeValueAsString;
				} else {
					return "No data found in Device QA";
				}
			}
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			PrintWriter printWriter = new PrintWriter(sw);
			e.printStackTrace(printWriter);
			printWriter.close();
			logger.info("Exception occured:-" + e.getMessage());
			e.printStackTrace();
			FailedDeviceQA failedDeviceQA = new FailedDeviceQA();
			failedDeviceQA.setCreatedAt(Instant.now());
			failedDeviceQA.setValue(deviceFromRequest.toString());
			failedDeviceQA.setException(sw.toString());
			failedDeviceQARepository.save(failedDeviceQA);
			logger.info("Failed To store data");
			return "Failed To store data";
		}
	}

	@Override
	public Boolean addQADeviceDetail1(HttpServletRequest request, String userName) {
		DeviceData deviceFromRequest = null;
		try {
			deviceFromRequest = new Gson().fromJson(request.getReader(), DeviceData.class);
			logger.info("Inside addQADeviceDetail");
			Device device = new Device();
			logger.info("Get Device By Imei:-" + deviceFromRequest.getDeviceID());
			Device byImei = deviceRepository.findByImei(deviceFromRequest.getDeviceID());
			logger.info("Get User By username");
			User user = restUtils.getUserFromAuthService(userName);
			logger.info("Get Device Company By owner level 2:-" + deviceFromRequest.getOwnerLevel2());
			DeviceCompany deviceCompany = companyDeviceRepo
					.findDeviceCompanyByMs1OrganisationName(deviceFromRequest.getOwnerLevel2());
			if (deviceCompany == null) {
				logger.info("organisation found from the table");
				deviceCompany = new DeviceCompany();
				deviceCompany.setCan("X-");
			}
			logger.info("Get Organisation By can number:-" + deviceCompany.getCan());
			Organisation company = restUtils.getCompanyFromCompanyService(deviceCompany.getCan());
			logger.info("organisation object got");
			if (byImei == null) {
				boolean isDeviceQAUuidUnique = false;
				String deviceQaUuid = "";
				while (!isDeviceQAUuidUnique) {
					deviceQaUuid = UUID.randomUUID().toString();
					DeviceQa byUuid = deviceQARepository.findByUuid(deviceQaUuid);
					if (byUuid == null) {
						isDeviceQAUuidUnique = true;
					}
				}
				boolean isDeviceUuidUnique = false;
				String deviceUuid = "";
				while (!isDeviceUuidUnique) {
					deviceUuid = UUID.randomUUID().toString();
					Device byUuid = deviceRepository.findByUuid(deviceUuid);
					if (byUuid == null) {
						isDeviceUuidUnique = true;
					}
				}
				logger.info("Prepare Device Object");
				logger.info("Set User");
				if (user != null && user.getUserName() != null) {
					logger.info("User object" + user.getUserName());
					device.setCreatedBy(user);
				}
				logger.info("Set CreatedAt value");
				device.setCreatedAt(Instant.now());

				device.setEpicorOrderNumber(deviceFromRequest.getSalesOrderID());
				device.setProductName(deviceFromRequest.getDeviceModel());
				device.setProductCode(deviceFromRequest.getProductCode());
				logger.info("Set getWaterfallDevdef_cfgCRC_value");
				logger.info("Set DeviceStatus value");
				device.setStatus(DeviceStatus.ACTIVE);
				device.setSon(deviceFromRequest.getSalesforceOrderNumber());
				logger.info("Set Qa Status");
				device.setQaStatus(deviceFromRequest.getQAStatus());
				if (deviceFromRequest.getQATimestampPST() != null) {
					device.setQaDate(deviceFromRequest.getQATimestampPST().toInstant());
				}
				device.setUuid(deviceUuid);
				device.setImei(deviceFromRequest.getDeviceID());
				String ownerLevel2 = deviceFromRequest.getOwnerLevel2();
				device.setOwnerLevel2(ownerLevel2);
				device.setIotType(IOTType.GATEWAY);
				device.setOrganisation(company);
				device.setUsageStatus(deviceFromRequest.getDeviceUsage());
				device.setDeviceType(deviceFromRequest.getDeviceModel());
//				DeviceDetails deviceDetails = new DeviceDetails();
//				deviceDetails.setImei(device.getImei());
//				deviceDetails.setUsageStatus(deviceFromRequest.getDeviceUsage());
//				device.setDeviceDetails(deviceDetails);
				logger.info("Prepare Cellular Object");
				Cellular cellular = new Cellular();
				cellular.setServiceCountry(deviceFromRequest.getServiceCountry());
				cellular.setPhone(deviceFromRequest.getDevicePhoneNum());
				cellular.setServiceNetwork(deviceFromRequest.getServiceNetwork());
				cellular.setImei(deviceFromRequest.getDeviceID());
				cellular.setCellular(deviceFromRequest.getDeviceSimNum());
				boolean isCellularUuidUnique = false;
				String cellularUuid = "";
				while (!isCellularUuidUnique) {
					cellularUuid = UUID.randomUUID().toString();
					Cellular byUuid = cellularRepository.findByUuid(cellularUuid);
					if (byUuid == null) {
						isCellularUuidUnique = true;
					}
				}
				cellular.setUuid(cellularUuid);
				logger.info("Save Cellular Details");
				cellularRepository.save(cellular);
				logger.info("Cellular Details saved successfully");
				device.setCellular(cellular);
				logger.info("For inserting QADevice Details");
				deviceRepository.save(device);
				logger.info("Device Details saved successfully");
				DeviceQa deviceQa = new DeviceQa();
				deviceQa.setQaResult(deviceFromRequest.getQAResult());
				deviceQa.setQaStatus(deviceFromRequest.getQAStatus());
				Timestamp now = Timestamp.from(Instant.now());
				deviceQa.setQaDate(now);
				deviceQa.setUuid(deviceQaUuid);
				deviceQa.setDeviceId(device);
				deviceQa.setCreatedOn(Instant.now());
				logger.info("Save Device Qa Details");
				deviceQARepository.save(deviceQa);
				logger.info("DeviceQA Details saved successfully");

				redisDeviceRepository.addMap(DEVICE_CURRENT_VIEW_PREFIX + device.getImei(), "customerId",
						deviceCompany.getCan());

				if (device.getProductName() != null) {
					redisDeviceRepository.addMap(DEVICE_CURRENT_VIEW_PREFIX + device.getImei(), "deviceType",
							device.getProductName());
				}

				return Boolean.TRUE;
			} else {
				logger.info("Inside update QA and device details");
				byImei.setEpicorOrderNumber(deviceFromRequest.getEpicorOrderNumber());
				byImei.setProductName(deviceFromRequest.getDeviceModel());
				byImei.setProductCode(deviceFromRequest.getProductCode());
				byImei.setImei(deviceFromRequest.getDeviceID());

				byImei.setUpdatedBy(user);
				byImei.setUpdatedAt(Instant.now());

				byImei.setSon(deviceFromRequest.getSalesforceOrderNumber());
				byImei.setUpdatedAt(Instant.now());
				byImei.setIotType(IOTType.GATEWAY);
				byImei.setStatus(DeviceStatus.ACTIVE);
				byImei.setQaDate(deviceFromRequest.getQATimestampPST().toInstant());
				byImei.setQaStatus(deviceFromRequest.getQAStatus());
				byImei.setUsageStatus(deviceFromRequest.getDeviceUsage());
				byImei.setDeviceType(deviceFromRequest.getDeviceModel());
				String ownerLevel2 = deviceFromRequest.getOwnerLevel2();
				byImei.setOwnerLevel2(ownerLevel2);
				byImei.setOrganisation(company);
//				DeviceDetails deviceDetails = new DeviceDetails();
//				deviceDetails.setImei(byImei.getImei());
//				deviceDetails.setUsageStatus(deviceFromRequest.getDeviceUsage());
//				byImei.setDeviceDetails(deviceDetails);
				Cellular cellularPayload = cellularRepository.findByUuid(byImei.getCellular().getUuid());
				cellularPayload.setServiceCountry(deviceFromRequest.getServiceCountry());
				cellularPayload.setPhone(deviceFromRequest.getDevicePhoneNum());
				cellularPayload.setServiceNetwork(deviceFromRequest.getServiceNetwork());
				cellularPayload.setImei(deviceFromRequest.getDeviceID());
				cellularPayload.setCellular(deviceFromRequest.getDeviceSimNum());
				cellularRepository.save(cellularPayload);
				logger.info("Cellular Details updated successfully");
				byImei.setCellular(cellularPayload);
				deviceRepository.save(byImei);
				logger.info("Device Details updated successfully");

				List<DeviceQa> deviceQADetails = deviceQARepository.findByDeviceId(deviceFromRequest.getDeviceID());
				if (deviceQADetails != null && deviceQADetails.size() > 0) {
					deviceQADetails.get(0).setQaResult(deviceFromRequest.getQAResult());
					deviceQADetails.get(0).setQaStatus(deviceFromRequest.getQAStatus());
					deviceQADetails.get(0).setUpdatedOn(Instant.now());
					deviceQADetails.get(0).setQaDate(deviceFromRequest.getQATimestampPST());
					deviceQARepository.save(deviceQADetails.get(0));
					logger.info("DeviceQA Details updated successfully");
				} else {
					logger.info("No data found in Device QA");
					// throw new Exception("No data found in Device QA");
				}

				redisDeviceRepository.addMap(DEVICE_CURRENT_VIEW_PREFIX + byImei.getImei(), "customerId",
						deviceCompany.getCan());

				if (device.getProductName() != null) {
					redisDeviceRepository.addMap(DEVICE_CURRENT_VIEW_PREFIX + byImei.getImei(), "deviceType",
							byImei.getProductName());
				}
				return Boolean.TRUE;
			}
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			PrintWriter printWriter = new PrintWriter(sw);
			e.printStackTrace(printWriter);
			printWriter.close();
			logger.info("Exception occured:-" + e.getMessage());
			e.printStackTrace();
			FailedDeviceQA failedDeviceQA = new FailedDeviceQA();
			failedDeviceQA.setCreatedAt(Instant.now());
			failedDeviceQA.setValue(deviceFromRequest.toString());
			failedDeviceQA.setException(sw.toString());
			failedDeviceQARepository.save(failedDeviceQA);
			logger.info("Failed To store data");
			return true;
		}
	}
}
