package com.pct.device.service.impl;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import com.pct.common.dto.CustomerForwardingRuleDTO;
import com.pct.common.dto.CustomerForwardingRuleUrlDTO;
import com.pct.common.dto.ForwardRuleResponseDTO;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.pct.common.model.Device;
import com.pct.common.model.DeviceForwarding;
import com.pct.common.model.DeviceIgnoreForwardingRule;
import com.pct.common.model.Organisation;
import com.pct.common.model.User;
import com.pct.common.payload.DeviceForwardingRequest;
import com.pct.common.payload.Forwarding;
import com.pct.common.util.JwtUser;
import com.pct.device.constant.KeyConstant;
import com.pct.device.dto.DeviceIgnoreForwardingRuleDTO;
import com.pct.device.dto.ImportForwardingDTO;
import com.pct.device.dto.ImportForwardingResponseDTO;
import com.pct.device.exception.DeviceException;
import com.pct.device.payload.DeviceDataForwardingBulkUploadRequest;
import com.pct.device.payload.DeviceForwardingResponse;
import com.pct.device.repository.DeviceForwardingRepository;
import com.pct.device.repository.DeviceIgnoreForwardingRuleRepository;
import com.pct.device.repository.IDeviceRepository;
import com.pct.device.repository.RedisDeviceRepository;
import com.pct.device.service.DeviceForwardingService;
import com.pct.device.service.IDeviceService;
import com.pct.device.util.BeanConverter;
import com.pct.device.util.RestUtils;

@Service
public class DeviceForwardingServiceImpl implements DeviceForwardingService {

	private static final Logger LOGGER = LoggerFactory.getLogger(DeviceForwardingServiceImpl.class);

	@Autowired
	DeviceForwardingRepository deviceForwardingRepository;

	@Autowired
	private BeanConverter beanConverter;

	@Autowired
	private IDeviceRepository deviceRepository;

	@Autowired
	private IDeviceService deviceService;

	@Autowired
	private RestUtils restUtils;
	
	@Autowired
	RedisDeviceRepository redisDeviceRepository;
	
	@Autowired
	private DeviceIgnoreForwardingRuleRepository deviceIgnoreForwardingRuleRepository;

	@Override
	public boolean addDeviceForwarding(DeviceForwardingRequest df, String userName) throws Exception {
		if (df != null) {
			User user = restUtils.getUserFromAuthService(userName);
			String imei = df.getDeviceId();
			List<DeviceForwarding> deviceDetails = deviceForwardingRepository.findByImei(imei);
			// if(deviceDetails.size()==4) {
			deleteForwarding(df, deviceDetails);
			df.getForwardingList().forEach(deviceForward -> {
				String type = deviceForward.getType();
				if (type != null && type != "") {
					DeviceForwarding deviceForwarding = new DeviceForwarding();
					if (deviceForward.getUuId() == null) {
						boolean isDeviceForwardUuidUnique = false;
						String deviceForwardUuid = "";
						while (!isDeviceForwardUuidUnique) {
							deviceForwardUuid = UUID.randomUUID().toString();
							System.out.println("deviceForwardUuid:" + deviceForwardUuid);
							DeviceForwarding byUuid = deviceForwardingRepository.findByUuid(deviceForwardUuid);
							if (byUuid == null) {
								isDeviceForwardUuidUnique = true;
							}
						}
						deviceForwarding.setUuid(deviceForwardUuid);
						deviceForwarding.setCreatedOn(Instant.now());
						deviceForwarding.setCreatedBy(user);
					} else {
						deviceForwarding.setUuid(deviceForward.getUuId());
						deviceForwarding.setId(deviceForward.getId());
					}
					Device device = deviceRepository.findByImei(imei);
					deviceForwarding.setType(type);
					deviceForwarding.setUrl(deviceForward.getUrl());
					deviceForwarding.setForwardingRuleUrlUuid(deviceForward.getForwardingRuleUrlUuid());
					deviceForwarding.setDevice(device);
					deviceForwardingRepository.save(deviceForwarding);
				}
			});
			updateDeviceForwardingOnRedis(imei);
			LOGGER.info("DeviceForwarding Details saved successfully");
//			}else
//			{
//				throw new Exception("The ");
//			}
		} else {
			throw new Exception("DeviceForwardingRequest JSON Can't be NULL");
		}
		return Boolean.TRUE;
	}

	private void deleteForwarding(DeviceForwardingRequest df, List<DeviceForwarding> deviceDetails) {
		List<DeviceForwarding> deviceForwardings = new ArrayList<>();
		if(df.getForwardingList().isEmpty() && !deviceDetails.isEmpty()) {
			deviceForwardings.addAll(deviceDetails);
		} else {
			deviceForwardings.addAll(deviceDetails.stream().filter(dr-> 
			df.getForwardingList().stream().noneMatch(
					f-> dr.getUuid().equals(f.getUuId())
					)
			).collect(Collectors.toList()));
			
		}
		if(!deviceForwardings.isEmpty()) {
			deviceForwardingRepository.deleteAll(deviceForwardings);
		}
	}
	
	private void  updateDeviceForwardingOnRedis(String imei){
		LOGGER.info("Inside the update device forwarding on redis");
		
		Map<String, String> kafkatopicNameMap = getKafkaTopicMap();
		
		String key = KeyConstant.DEVICE_CURRENT_VIEW_PREFIX + imei;
		
		List<DeviceForwarding> deviceForwardings = deviceForwardingRepository.findByImei(imei);
		
		if(!deviceForwardings.isEmpty()) {
			LOGGER.info("Updating device forwarding");
			List<String> urls = new ArrayList<>();
			List<String> types = new ArrayList<>();
			List<String> kafkaTopicNames = new ArrayList<>();
			
			for (DeviceForwarding deviceForwarding : deviceForwardings) {
				urls.add(deviceForwarding.getUrl());
				types.add(deviceForwarding.getType());
				kafkaTopicNames.add(kafkatopicNameMap.get(deviceForwarding.getForwardingRuleUrlUuid()));
			}
			LOGGER.info("Device forwarding url count {}", urls.size());
			LOGGER.info("Device forwarding type count {}", types.size());
			
			redisDeviceRepository.addMap(key, KeyConstant.FROWARDING_URLS, JSON.toJSONString(urls));
			redisDeviceRepository.addMap(key, KeyConstant.FROWARDING_TYPES, JSON.toJSONString(types));
			redisDeviceRepository.addMap(key, KeyConstant.FROWARDING_KAFKA_TOPIC_NAMES, JSON.toJSONString(kafkaTopicNames));
			LOGGER.info("Updated device forwarding");
		} else {
			LOGGER.info("Deleting device forwarding");
			redisDeviceRepository.removeMap(key, KeyConstant.FROWARDING_URLS);
			redisDeviceRepository.removeMap(key, KeyConstant.FROWARDING_TYPES);
			redisDeviceRepository.removeMap(key, KeyConstant.FROWARDING_KAFKA_TOPIC_NAMES);
			LOGGER.info("Deleted device forwarding");
		}
		
		LOGGER.info("Successfully finished device forwarding in redis");
	}

	private Map<String, String> getKafkaTopicMap() {
		Map<String, String> kafkatopicNameMap = new HashMap<>();
		
		List<Map<String, Object>> forwardingvalidUrlMaps = restUtils.getAllCustomerForwardingRuleUrl();

		forwardingvalidUrlMaps.forEach(map -> {
			kafkatopicNameMap.put((String) map.get("uuid"), (String) map.get("kafkaTopicName"));
		});
		return kafkatopicNameMap;
	}


	@Transactional
	@Override
	public boolean updateDeviceForwarding(DeviceForwarding df) throws Exception {
		LOGGER.info("Inside updateDeviceForwarding");

		if (df != null) {
			DeviceForwarding deviceForwarding = null;
		//	beanConverter.convertDeviceForwardingRequestToDeviceForwarding(df);
			System.out.println("deviceForwarding.getUuid():" + deviceForwarding.getUuid());
			DeviceForwarding byUuid = deviceForwardingRepository.findByUuid(deviceForwarding.getUuid());
			if (byUuid != null) {
				deviceForwarding.setUpdatedOn(Instant.now());
				Device device = deviceService.getDevice(deviceForwarding.getDevice().getImei());
				deviceForwarding.setDevice(device);
				deviceForwardingRepository.update(deviceForwarding.getUpdatedOn(), deviceForwarding.getType(),
						deviceForwarding.getUrl(), deviceForwarding.getDevice().getImei(), deviceForwarding.getUuid());

				LOGGER.info("DeviceForwarding updated for the uuid:" + deviceForwarding.getUuid());
			} else {
				throw new Exception("DeviceForwarding not found");
			}
		} else {
			throw new Exception("DeviceForwardingRequest JSON Can't be NULL");
		}
		return Boolean.TRUE;
	}

	@Override
	public boolean deleteDeviceForwarding(String uuid) throws Exception {
		LOGGER.info("Inside deleteDeviceForwarding for uuid " + uuid);
		if (uuid != null) {
			LOGGER.info("Fetching DeviceForwarding detail for uuid " + uuid);
			DeviceForwarding deviceForwarding = deviceForwardingRepository.findByUuid(uuid);
			if (deviceForwarding != null) {
				deviceForwardingRepository.delete(deviceForwarding);
			} else {
				throw new Exception("No such DeviceForwarding found for uuid:" + uuid);
			}
		} else {
			throw new Exception("Uuid Can't be NULL");
		}
		return Boolean.TRUE;
	}

	@Override
	public List<DeviceForwardingResponse> getDeviceForwardingById(Long id) throws Exception {
		List<DeviceForwarding> al = new ArrayList<DeviceForwarding>();
		LOGGER.info("Inside getDeviceForwardingById for uuid " + id);
		if (id != null) {
			LOGGER.info("Fetching DeviceForwarding detail for uuid " + id);
			DeviceForwarding deviceForwarding = deviceForwardingRepository.findById(id).get();
			al.add(deviceForwarding);
		} else {
			LOGGER.info("Fetching all DeviceForwarding details as uuid is null");
			al = deviceForwardingRepository.findAll();
		}
		if (al.isEmpty()) {
			throw new Exception("No such DeviceForwarding found for id:" + id);
		}
		return beanConverter.convertDeviceForwardingToDeviceForwardingResponse(al);
	}

	@Override
	public List<DeviceForwardingResponse> getDeviceForwardingByUuid(String uuid) throws Exception {
		List<DeviceForwarding> al = new ArrayList<DeviceForwarding>();
		LOGGER.info("Inside getDeviceForwardingByUuid for uuid " + uuid);
		if (uuid != null) {
			LOGGER.info("Fetching DeviceForwarding detail for uuid " + uuid);
			DeviceForwarding deviceForwarding = deviceForwardingRepository.findByUuid(uuid);
			if (deviceForwarding != null) {
				al.add(deviceForwarding);
			}
			LOGGER.info("al=" + al);
		} else {
			LOGGER.info("Fetching all DeviceForwarding details as uuid is null");
			al = deviceForwardingRepository.findAll();
		}
		if (al.isEmpty()) {
			throw new Exception("No such DeviceForwarding found for uuid:" + uuid);
		}
		return beanConverter.convertDeviceForwardingToDeviceForwardingResponse(al);
	}

	@Override
	public List<DeviceForwardingResponse> getAllDeviceForwarding() throws Exception {
		List<DeviceForwardingResponse> list = null;
		try {
			LOGGER.info("inside getAllDeviceForwarding()");
			List<DeviceForwarding> al = deviceForwardingRepository.findAll();
			list = beanConverter.convertDeviceForwardingToDeviceForwardingResponse(al);
		} catch (Exception e) {
			throw new Exception("Exception while all getting device forwading",e);
		}
		return list;
	}

	@Override
	public List<DeviceForwardingResponse> getAllDeviceForwardingByImei(String imei) throws Exception {
		List<DeviceForwardingResponse> list = null;
		try {
			LOGGER.info("inside getAllDeviceForwarding()");
			List<DeviceForwarding> al = deviceForwardingRepository.findByImei(imei);
			list = beanConverter.convertDeviceForwardingToDeviceForwardingResponse(al);
		} catch (Exception e) {
			throw new Exception("Exception while getting device forwading by imei id",e);
		}
		return list;
	}
	
	@Override
	public Map<String, Object> getDeviceForwardingDetailsFromRedis(String imei) {

		LOGGER.info("Inside getDeviceForwardingDetailsFromRedis for imei " + imei);

		Map<String, Object> deviceForwardingMap = new HashMap<>();
		deviceForwardingMap.put("imei", imei);
		deviceForwardingMap.put("deviceForwardingRules", new ArrayList<>());
		deviceForwardingMap.put("customerForwardingRules", new ArrayList<>());
		deviceForwardingMap.put("customerIgnoreForwardingRules", new ArrayList<>());
		deviceForwardingMap.put("deviceForwardingApplingRules", new ArrayList<>());
		deviceForwardingMap.put("purchasedByForwardingRules", new ArrayList<>());
		
		String key = KeyConstant.DEVICE_CURRENT_VIEW_PREFIX + imei;

		String data = redisDeviceRepository.getMap(key, KeyConstant.FROWARDING_URLS);
		if (data != null && !data.isEmpty()) {
			deviceForwardingMap.put("deviceFrowardingUrls", JSON.parse(data));
		}

		data = redisDeviceRepository.getMap(key, KeyConstant.FROWARDING_TYPES);
		if (data != null && !data.isEmpty()) {
			deviceForwardingMap.put("deviceFrowardingTypes", JSON.parse(data));
		}

		if (deviceForwardingMap.containsKey("deviceFrowardingUrls")
				&& deviceForwardingMap.containsKey("deviceFrowardingTypes")) {
			deviceForwardingMap.put("deviceForwardingRules",
					getRules((JSONArray) deviceForwardingMap.get("deviceFrowardingUrls"),
							(JSONArray) deviceForwardingMap.get("deviceFrowardingTypes")));
		}

		data = redisDeviceRepository.getMap(key, KeyConstant.CUSTOMER_ID);
		if (data != null && !data.isEmpty()) {
			deviceForwardingMap.put("deviceCustomerId", data);
		}

		data = redisDeviceRepository.getMap(key, KeyConstant.EXCLUDED_CUSTOMER_FROWARDING_TYPES);
		if (data != null && !data.isEmpty()) {
			deviceForwardingMap.put("deviceExcludedCustomerFrowardingTypes", JSON.parse(data));
		}

		data = redisDeviceRepository.getMap(key, KeyConstant.EXCLUDED_CUSTOMER_FROWARDING_URLS);
		if (data != null && !data.isEmpty()) {
			deviceForwardingMap.put("deviceExcludedCustomerFrowardingUrls", JSON.parse(data));
		}

		if (deviceForwardingMap.containsKey("deviceExcludedCustomerFrowardingUrls")
				&& deviceForwardingMap.containsKey("deviceExcludedCustomerFrowardingTypes")) {
			deviceForwardingMap.put("customerIgnoreForwardingRules",
					getRules((JSONArray) deviceForwardingMap.get("deviceExcludedCustomerFrowardingUrls"),
							(JSONArray) deviceForwardingMap.get("deviceExcludedCustomerFrowardingTypes")));
		}

		if (deviceForwardingMap.containsKey("deviceCustomerId")) {
			LOGGER.info("Get customer forwarding rules");

			String deviceCustomerId = (String) deviceForwardingMap.get("deviceCustomerId");

			String customerForwardingKey = "CustomerPrefix:forwardingRule" + deviceCustomerId;

			data = redisDeviceRepository.getMap(customerForwardingKey, "URLS");
			if (data != null && !data.isEmpty()) {
				deviceForwardingMap.put("customerFrowardingUrls", JSON.parse(data));
			}

			data = redisDeviceRepository.getMap(customerForwardingKey, "TYPES");
			if (data != null && !data.isEmpty()) {
				deviceForwardingMap.put("customerFrowardingTypes", JSON.parse(data));
			}

			if (deviceForwardingMap.containsKey("customerFrowardingUrls")
					&& deviceForwardingMap.containsKey("customerFrowardingTypes")) {
				deviceForwardingMap.put("customerForwardingRules",
						getRules((JSONArray) deviceForwardingMap.get("customerFrowardingUrls"),
								(JSONArray) deviceForwardingMap.get("customerFrowardingTypes")));
			}

			LOGGER.info("Get customer forwarding rules fetch successfully done");
		}
		
		data = redisDeviceRepository.getMap(key, KeyConstant.PURCHASED_BY);
		if (data != null && !data.isEmpty()) {
			deviceForwardingMap.put("devicePurchasedById", data);
		}
		
		if (deviceForwardingMap.containsKey("devicePurchasedById") && !deviceForwardingMap.get("devicePurchasedById")
				.equals(deviceForwardingMap.get("deviceCustomerId"))) {
			LOGGER.info("Get device purchased by forwarding rules");

			String devicePurchasedById = (String) deviceForwardingMap.get("devicePurchasedById");

			String purchasedByForwardingKey = "CustomerPrefix:forwardingRule" + devicePurchasedById;

			data = redisDeviceRepository.getMap(purchasedByForwardingKey, "URLS");
			if (data != null && !data.isEmpty()) {
				deviceForwardingMap.put("purchasedByFrowardingUrls", JSON.parse(data));
			}

			data = redisDeviceRepository.getMap(purchasedByForwardingKey, "TYPES");
			if (data != null && !data.isEmpty()) {
				deviceForwardingMap.put("purchasedByFrowardingTypes", JSON.parse(data));
			}

			if (deviceForwardingMap.containsKey("purchasedByFrowardingUrls")
					&& deviceForwardingMap.containsKey("purchasedByFrowardingTypes")) {
				deviceForwardingMap.put("purchasedByForwardingRules",
						getRules((JSONArray) deviceForwardingMap.get("purchasedByFrowardingUrls"),
								(JSONArray) deviceForwardingMap.get("purchasedByFrowardingTypes")));
			}
		}

		deviceForwardingMap.put("deviceForwardingApplingRules",
				getApplingRules((List<Map<String, Object>>) deviceForwardingMap.get("deviceForwardingRules"),
						(List<Map<String, Object>>) deviceForwardingMap.get("customerForwardingRules"),
						(List<Map<String, Object>>) deviceForwardingMap.get("purchasedByForwardingRules"),
						(List<Map<String, Object>>) deviceForwardingMap.get("customerIgnoreForwardingRules")));
		
		if (deviceForwardingMap.containsKey("devicePurchasedById") && deviceForwardingMap.get("devicePurchasedById")
				.equals(deviceForwardingMap.get("deviceCustomerId"))) {
			deviceForwardingMap.put("purchasedByForwardingRules", deviceForwardingMap.get("customerForwardingRules"));
		}
		
		LOGGER.info("Get device forwarding details fetch successfullt done...");

		return deviceForwardingMap;
	}

	private List<Map<String, Object>> getApplingRules(List<Map<String, Object>> deviceForwardingRules,
			List<Map<String, Object>> customerForwardingRules, List<Map<String, Object>> purchasedByForwardingRules,
			List<Map<String, Object>> customerIgnoreForwardingRules) {

		List<Map<String, Object>> applingRules = new ArrayList<>();

		if (deviceForwardingRules != null) {
			applingRules.addAll(deviceForwardingRules.stream().map(map -> {
				map.put("from", "Device");
				return map;
			}).collect(Collectors.toList()));
		}

		if (customerForwardingRules != null && customerIgnoreForwardingRules != null) {
			applingRules
					.addAll(customerForwardingRules
							.stream().filter(
									cfr -> customerIgnoreForwardingRules.stream()
											.noneMatch(cifr -> cifr.getOrDefault("url", "N/A").equals(cfr.get("url"))
													&& cifr.getOrDefault("type", "N/A").equals(cfr.get("type"))))
							.map(map -> {
								map.put("from", "Customer");
								return map;
							}).collect(Collectors.toList()));
		}

		if (purchasedByForwardingRules != null && purchasedByForwardingRules != null) {
			applingRules
					.addAll(purchasedByForwardingRules
							.stream().filter(
									cfr -> customerIgnoreForwardingRules.stream()
											.noneMatch(cifr -> cifr.getOrDefault("url", "N/A").equals(cfr.get("url"))
													&& cifr.getOrDefault("type", "N/A").equals(cfr.get("type"))))
							.map(map -> {
								map.put("from", "Purchase By");
								return map;
							}).collect(Collectors.toList()));
		}

		return applingRules;
	}

	private List<Map<String, Object>> getRules(JSONArray urls, JSONArray types) {
		List<Map<String, Object>> rules = new ArrayList<>();

		if (types != null && urls != null && types.size() == urls.size()) {
			for (int i = 0; i < types.size(); i++) {
				Map<String, Object> rule = new HashMap<>();
				rule.put("type", types.getString(i));
				rule.put("url", urls.getString(i));
				rules.add(rule);
			}
		}

		return rules;
	}
	
	@Override
	public boolean deleteDeviceForwardingByImei(String imei) throws Exception {
		LOGGER.info("Inside deleteDeviceForwardingByImei for imei " + imei);
		if (imei != null) {
			LOGGER.info("Fetching DeviceForwarding detail for imei " + imei);
			List<DeviceForwarding> deviceForwardings = deviceForwardingRepository.findByImei(imei);
			if (!deviceForwardings.isEmpty()) {
				deviceForwardingRepository.deleteAll(deviceForwardings);
			}
		} else {
			throw new Exception("Imei Can't be NULL");
		}
		return Boolean.TRUE;
	}
	
	@Override
	public Map<String, Set<String>> getIgnoreRuleImeisByCustomerAccountNumber(String customerAccountNumber) {
		
		Map<String, Set<String>> resultMap = new HashMap<>();
		
		Set<DeviceIgnoreForwardingRule> deviceIgnoreForwardingRules = deviceIgnoreForwardingRuleRepository.findByCustomerAccountNumber(customerAccountNumber);
		
		deviceIgnoreForwardingRules.forEach( iRule -> {
			if(resultMap.containsKey(iRule.getCustomerForwardingRuleUuid())) {
				Set<String> imeis = resultMap.get(iRule.getCustomerForwardingRuleUuid());
				imeis.add(iRule.getDevice().getImei());
			} else {
				Set<String> imeis = new HashSet<>();
				imeis.add(iRule.getDevice().getImei());
				resultMap.put(iRule.getCustomerForwardingRuleUuid(), imeis);
			}
		});
		
		return resultMap;
	}
	
	@Override
	public List<DeviceIgnoreForwardingRuleDTO> getIgnoreRulesUsingDeviceImei(String imei) {
		Set<String> imeis = new HashSet<>();
		imeis.add(imei);
		Set<DeviceIgnoreForwardingRule> deviceIgnoreForwardingRules = deviceIgnoreForwardingRuleRepository.findByDeviceiImeisIn(imeis);
		return deviceIgnoreForwardingRules.parallelStream().map(r->{
			DeviceIgnoreForwardingRuleDTO dto = new DeviceIgnoreForwardingRuleDTO();
			if(r.getCreatedBy() != null) {
				dto.setCreatedBy(r.getCreatedBy().getUuid());
			}
			dto.setCreatedOn(r.getCreatedOn());
			dto.setCustomerForwardingRuleUuid(r.getCustomerForwardingRuleUuid());
			
			if(r.getDevice() != null) {
				dto.setDeviceImei(r.getDevice().getImei());
			}
			dto.setId(r.getId());
			dto.setType(r.getType());
			if(r.getUpdatedBy() != null) {
				dto.setUpdatedBy(r.getUpdatedBy().getUuid());
			}
			dto.setUpdatedOn(r.getUpdatedOn());
			dto.setUrl(r.getUrl());
			dto.setUuid(r.getUrl());
			return dto;
		}).collect(Collectors.toList());
	}
	
	@Override
	public Boolean bulkUpdateDeviceForwarding(DeviceDataForwardingBulkUploadRequest request) {
		validateBulkUpdateDeviceForwardingRequest(request);

		JwtUser jwtUser = (JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		User user = restUtils.getUserFromAuthService(jwtUser.getUsername());

		Organisation company = restUtils.getCompanyFromCompanyService(request.getEndCustomerAccountNumber());

		Organisation purchasedBy = null;
		if (request.getPurchasedByAccountNumber() != null) {
			purchasedBy = restUtils.getCompanyFromCompanyService(request.getPurchasedByAccountNumber());
		}

		List<Device> devices = new ArrayList<>();

		for (String imei : request.getImeis()) {
			Device device = deviceRepository.findByImei(imei);

			if (device == null) {
				throw new DeviceException(String.format("Device is not present in db for imei %s", imei));
			}

			devices.add(device);

			List<DeviceForwarding> existingDeviceForwardingRules = deviceForwardingRepository.findByImei(imei);

			List<DeviceForwarding> deleteDeviceForwardingRules = new ArrayList<>();

			if (request.getForwardingRules().isEmpty() && !existingDeviceForwardingRules.isEmpty()) {
				deleteDeviceForwardingRules.addAll(existingDeviceForwardingRules);
			} else {
				deleteDeviceForwardingRules.addAll(existingDeviceForwardingRules.stream().filter(
						dr -> request.getForwardingRules().stream().noneMatch(f -> dr.getUuid().equals(f.getUuId())))
						.collect(Collectors.toList()));
			}

			List<DeviceForwarding> deviceForwardingUpdateList = getUpdatedDeviceForwardingList(request, user, device,
					existingDeviceForwardingRules);
			
			if (!deleteDeviceForwardingRules.isEmpty()) {
				deviceForwardingRepository.deleteAllInBatch(deleteDeviceForwardingRules);
			}
			
			if (!deviceForwardingUpdateList.isEmpty()) {
				deviceForwardingRepository.saveAll(deviceForwardingUpdateList);
			}

			updateDeviceForwardingOnRedis(imei);
		}

		saveExcludedCustomerForwardingRules(request.getIgnoreForwardingRules(), devices, company, purchasedBy, user);
		updateExcludeCustomerFrowardingRulesOnRedis(request.getIgnoreForwardingRules(), devices);

		return Boolean.TRUE;
	}

	private void validateBulkUpdateDeviceForwardingRequest(DeviceDataForwardingBulkUploadRequest request) {
		if (request == null) {
			throw new DeviceException("Request payload is required");
		}

		if (request.getImeis() == null || request.getImeis().isEmpty()) {
			throw new DeviceException("Atleast one imei is required");
		}

		if (request.getForwardingRules() == null || request.getForwardingRules().size() > 5) {
			throw new DeviceException("Either device forwarding rules is null or gether than 5 rules");
		}
	}

	private List<DeviceForwarding> getUpdatedDeviceForwardingList(DeviceDataForwardingBulkUploadRequest request,
			User user, Device device, List<DeviceForwarding> existingDeviceForwardingRules) {
		List<DeviceForwarding> deviceForwardingUpdateList = new ArrayList<>();

		request.getForwardingRules().forEach(deviceForward -> {
			DeviceForwarding deviceForwarding = new DeviceForwarding();
			if (deviceForward.getUuId() == null) {
				do {
					deviceForwarding.setUuid(UUID.randomUUID().toString());
				} while (existingDeviceForwardingRules.stream().anyMatch(x -> x.getUuid().equals(deviceForwarding.getUuid()))
						|| deviceForwardingUpdateList.stream()
								.anyMatch(x -> x.getUuid().equals(deviceForwarding.getUuid())));

				deviceForwarding.setCreatedOn(Instant.now());
				deviceForwarding.setCreatedBy(user);
			} else {
				deviceForwarding.setUuid(deviceForward.getUuId());
				deviceForwarding.setId(deviceForward.getId());
			}
			deviceForwarding.setUpdatedBy(user);
			deviceForwarding.setUpdatedOn(Instant.now());
			deviceForwarding.setType(deviceForward.getType());
			deviceForwarding.setUrl(deviceForward.getUrl());
			deviceForwarding.setForwardingRuleUrlUuid(deviceForward.getForwardingRuleUrlUuid());
			deviceForwarding.setDevice(device);
			deviceForwardingUpdateList.add(deviceForwarding);
		});
		return deviceForwardingUpdateList;
	}

	@Override
	public void saveExcludedCustomerForwardingRules(List<Forwarding> ignoreForwardingRules, List<Device> devices,
			Organisation endCustomer, Organisation purchasedBy, User user) {

		LOGGER.info("Inside the save excluded customer for device forwarding rules in db");

		List<Map<String, Object>> customerForwardingRuleDTOs = getForwardingRulesFromCustomerService(endCustomer,
				purchasedBy);

		validateIgnoreForwardingRules(ignoreForwardingRules, customerForwardingRuleDTOs);

		Set<DeviceIgnoreForwardingRule> newDeviceIgnoreForwardingRules = new HashSet<>();
		Set<String> imeis = new HashSet<>();
		devices.forEach(device -> {
			imeis.add(device.getImei());
			ignoreForwardingRules.forEach(ignoreRule -> {
				DeviceIgnoreForwardingRule rule = new DeviceIgnoreForwardingRule();
				rule.setCreatedBy(user);
				rule.setCreatedOn(Instant.now());
				rule.setCustomerForwardingRuleUuid(ignoreRule.getUuId());
				rule.setDevice(device);
				rule.setType(ignoreRule.getType());
				rule.setUrl(ignoreRule.getUrl());
				rule.setUuid(UUID.randomUUID().toString());
				newDeviceIgnoreForwardingRules.add(rule);
			});
		});

		if (!imeis.isEmpty()) {
			Set<DeviceIgnoreForwardingRule> oldDeviceIgnoreForwardingRules = deviceIgnoreForwardingRuleRepository
					.findByDeviceiImeisIn(imeis);
			if (!oldDeviceIgnoreForwardingRules.isEmpty()) {
				LOGGER.info("Deleteing old excluded customer forwarding rules count: {}",
						oldDeviceIgnoreForwardingRules.size());
				deviceIgnoreForwardingRuleRepository.deleteAll(oldDeviceIgnoreForwardingRules);
			}
		}

		if (!newDeviceIgnoreForwardingRules.isEmpty()) {
			LOGGER.info("Saving excluded customer forwarding rules count: {}", newDeviceIgnoreForwardingRules.size());
			deviceIgnoreForwardingRuleRepository.saveAll(newDeviceIgnoreForwardingRules);
		}

		LOGGER.info("Successfully finshed excluded customer forwarding rules save in db");
	}

	private List<Map<String, Object>> getForwardingRulesFromCustomerService(Organisation endCustomer,
			Organisation purchasedBy) {
		Set<String> uuids = new HashSet<>();
		uuids.add(endCustomer.getUuid());

		if (purchasedBy != null) {
			uuids.add(purchasedBy.getUuid());
		}

		return restUtils.getCustomerForwardingRulesByOrganizationUuids(uuids);
	}

	private void validateIgnoreForwardingRules(List<Forwarding> ignoreForwardingRules,
			List<Map<String, Object>> customerForwardingRuleDTOs) {

		ignoreForwardingRules.forEach(ignoreRule -> {
			if (ignoreRule.getUuId() == null || ignoreRule.getUuId().isEmpty()) {
				throw new DeviceException("Device ignore forwarding rule uuid is required");
			}
			if (ignoreRule.getUrl() == null || ignoreRule.getUrl().isEmpty()) {
				throw new DeviceException("Device ignore forwarding rule url is required");
			}
			if (ignoreRule.getType() == null || ignoreRule.getType().isEmpty()) {
				throw new DeviceException("Device ignore forwarding rule type is required");
			}
			Optional<Map<String, Object>> optional = customerForwardingRuleDTOs.stream()
					.filter(forward -> ignoreRule.getUuId().equals(forward.get("uuid"))).findFirst();
			if (!optional.isPresent()) {
				throw new DeviceException("Device ignore forwarding rules is not present in customer forwarding rules");
			}

			Map<String, Object> customerForwardingRuleMap = optional.get();
			if (!ignoreRule.getUrl().equals(customerForwardingRuleMap.get("url"))) {
				throw new DeviceException(
						"Device ignore forwarding rule url is not match in customer forwarding rule url");
			}
			if (!ignoreRule.getType().equals(customerForwardingRuleMap.get("type"))) {
				throw new DeviceException(
						"Device ignore forwarding rule type is not match in customer forwarding rule type");
			}
		});

	}

	@Override
	public void updateExcludeCustomerFrowardingRulesOnRedis(List<Forwarding> ignoreForwardingRules,
			List<Device> devices) {
		LOGGER.info("Inside the excluded customer forwarding rules updating in redis db");

		List<String> urls = ignoreForwardingRules.stream().map(r -> r.getUrl()).collect(Collectors.toList());
		List<String> types = ignoreForwardingRules.stream().map(r -> r.getType()).collect(Collectors.toList());

		boolean isExisteExcludeCustomerFrowarding = !ignoreForwardingRules.isEmpty();

		for (Device device : devices) {

			String key = KeyConstant.DEVICE_CURRENT_VIEW_PREFIX + device.getImei();

			redisDeviceRepository.removeMap(key, KeyConstant.EXCLUDED_CUSTOMER_FROWARDING_URLS);
			redisDeviceRepository.removeMap(key, KeyConstant.EXCLUDED_CUSTOMER_FROWARDING_TYPES);

			if (isExisteExcludeCustomerFrowarding) {
				redisDeviceRepository.addMap(key, KeyConstant.EXCLUDED_CUSTOMER_FROWARDING_URLS,
						JSON.toJSONString(urls));
				redisDeviceRepository.addMap(key, KeyConstant.EXCLUDED_CUSTOMER_FROWARDING_TYPES,
						JSON.toJSONString(types));
			}
		}

		LOGGER.info("Successfully finshed excluded customer forwarding rules updated in redis db");
	}

	@Override
	@Transactional
	public Map<String, List<ForwardRuleResponseDTO.DeviceFwdRulResp>> processDeviceForwardingRuleForDevice(
			DeviceDataForwardingBulkUploadRequest request, String token) {
		if (request == null) {
			throw new DeviceException("Request payload is required");
		}
		if (request.getImeis() == null || request.getImeis().isEmpty()) {
			throw new DeviceException("Atleast one imei is required");
		}
		JwtUser jwtUser = (JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		User user = restUtils.getUserFromAuthService(jwtUser.getUsername());
		List<CustomerForwardingRuleDTO> customerForwardingRules = restUtils.getAllCustomerForwardingRules(token);
		List<Organisation> organizations = restUtils.findAllOrganisation(token);
		List<Device> devicesToAddinRedis = new ArrayList<>();
		Map<String, List<ForwardRuleResponseDTO.DeviceFwdRulResp>> deviceMessageMap = new HashMap<>();
		List<String> imeis = request.getImeis();
		List<Device> devices = deviceRepository.findByImei(imeis);
		Map<String, Device> deviceMap = new HashMap<>();
		devices.stream().forEach(d -> {
			deviceMap.put(d.getImei(), d);
		});
		String action = "";
		Set<String> imeisKey = deviceMap.keySet();
		for (String imei : imeisKey) {
			Device device = deviceMap.get(imei);
			if (device == null) {
				throw new DeviceException(String.format("Device is not present in db for imei %s", imei));
			}
			devicesToAddinRedis.add(device);
			DeviceForwardingRequest deviceForwardingRequest = new DeviceForwardingRequest();
			deviceForwardingRequest.setForwardingList(request.getForwardingRules());
			deviceForwardingRequest.setDeviceId(device.getImei());
			action = request.getAction();
			try {
				deviceMessageMap = processDeviceForwardingRuleForDevice(deviceForwardingRequest, device, user,
						deviceMessageMap, action, organizations, customerForwardingRules);
				LOGGER.info("Device forwarding saved successfully for device id : {} status : {} ", device.getImei(),
						deviceMessageMap);
			} catch (Exception e) {
				LOGGER.error("Exception occured while saving device forwarding for device id : {}", device.getImei());
				LOGGER.error("Exception is : ", e);
				throw new RuntimeException(e);
			}
		}
		if (StringUtils.equalsIgnoreCase(action, "restore")) {
			updateExcludeCustomerFrowardingRulesOnRedis(Collections.EMPTY_LIST, devicesToAddinRedis);
		}
		return deviceMessageMap;
	}

	private Map<String, List<ForwardRuleResponseDTO.DeviceFwdRulResp>> processDeviceForwardingRuleForDevice(
			DeviceForwardingRequest deviceForwardingRequest, Device device, User user,
			Map<String, List<ForwardRuleResponseDTO.DeviceFwdRulResp>> deviceMessageMap, String action,
			List<Organisation> organizations, List<CustomerForwardingRuleDTO> customerForwardingRules)
			throws Exception {
		if (deviceForwardingRequest != null) {
			String imei = deviceForwardingRequest.getDeviceId();
			List<DeviceForwarding> deviceForwardings = deviceForwardingRepository.findByImei(imei);
			if (StringUtils.equalsIgnoreCase(action, "add")) {
				final int[] size = { deviceForwardings.size() };
				List<ForwardRuleResponseDTO.DeviceFwdRulResp> deviceFwdRulResps = new ArrayList<>();
				filterForwardingRuleURLUuidNotToAdd(deviceForwardingRequest, deviceForwardings, deviceFwdRulResps);
				filterForwardingRuleURLUuidToAdd(deviceForwardingRequest, deviceForwardings);
				addForwardingRules(deviceForwardingRequest, device, user, deviceMessageMap, imei, size,
						deviceFwdRulResps);
			} else if (StringUtils.equalsIgnoreCase(action, "replace")) {
				if (CollectionUtils.isNotEmpty(deviceForwardings)) {
					deleteExistingForwardingRule(imei);
					final int[] size = { 0 };
					List<ForwardRuleResponseDTO.DeviceFwdRulResp> deviceFwdRulResps = new ArrayList<>();
					addForwardingRules(deviceForwardingRequest, device, user, deviceMessageMap, imei, size,
							deviceFwdRulResps);
				}
			} else if (StringUtils.equalsIgnoreCase(action, "remove")) {
				if (CollectionUtils.isNotEmpty(deviceForwardings)) {
					deleteExistingForwardingRule(imei);
					updateDeviceForwardingOnRedis(imei);
				}
				Optional<Organisation> organisation = organizations.stream().filter(organization -> organization
						.getAccountNumber().equals(device.getOrganisation().getAccountNumber())).findAny();
				LOGGER.info("organisation.isPresent() " + organisation.isPresent());
				LOGGER.info(
						"device.getOrganisation().getAccountNumber() " + device.getOrganisation().getAccountNumber());
				if (organisation.isPresent()) {
					String orgUuid = organisation.get().getUuid();
					LOGGER.info("organisation.uuid " + orgUuid);
					List<CustomerForwardingRuleDTO> customerForwardingRuleDTOs = customerForwardingRules.stream()
							.filter(customerForwardingRule -> customerForwardingRule.getOrganisationUuid()
									.equals(orgUuid))
							.collect(Collectors.toList());
					LOGGER.info("customerForwardingRuleDTOs size " + customerForwardingRuleDTOs.size());
					Map<String, CustomerForwardingRuleDTO> customerForwardingRuleDTOMap = new HashMap<>();
					customerForwardingRuleDTOs.stream().forEach(customerForwardingRuleDTO -> {
						customerForwardingRuleDTOMap.put(customerForwardingRuleDTO.getUuid(),
								customerForwardingRuleDTO);
					});
					LOGGER.info("customerForwardingRuleDTOMap size " + customerForwardingRuleDTOMap.size());
					List<String> uuids = customerForwardingRuleDTOs.stream()
							.map(customerForwardingRuleDTO -> customerForwardingRuleDTO.getUuid())
							.collect(Collectors.toList());
					LOGGER.info("customer_forwarding_rule uuids " + uuids);
					List<DeviceIgnoreForwardingRule> deviceIgnoreForwardingRules = deviceIgnoreForwardingRuleRepository
							.findByCustomerForwardingRuleUuidIn(uuids, device.getImei());
					LOGGER.info("deviceIgnoreForwardingRules size " + deviceIgnoreForwardingRules.size());
					List<String> customerForwardingRuleUuids = deviceIgnoreForwardingRules.stream().map(
							deviceIgnoreForwardingRule -> deviceIgnoreForwardingRule.getCustomerForwardingRuleUuid())
							.collect(Collectors.toList());
					LOGGER.info("customerForwardingRuleUuids " + customerForwardingRuleUuids);
					List<String> unmatchForwardingRuleUrlUuids = uuids.stream()
							.filter(uuid -> customerForwardingRuleUuids.stream()
									.noneMatch(customerForwardingRuleUuid -> customerForwardingRuleUuid.equals(uuid)))
							.collect(Collectors.toList());
					LOGGER.info("unmatchForwardingRuleUrlUuids " + unmatchForwardingRuleUrlUuids);
					if (!unmatchForwardingRuleUrlUuids.isEmpty()) {
						Set<DeviceIgnoreForwardingRule> newDeviceIgnoreForwardingRules = new HashSet<>();
						unmatchForwardingRuleUrlUuids.forEach(unmatchForwardingRuleUrlUuid -> {
							LOGGER.info("deviceIgnoreForwardingRule entry for ForwardingRuleUrlUuid "
									+ unmatchForwardingRuleUrlUuid);
							DeviceIgnoreForwardingRule deviceIgnoreForwardingRule = new DeviceIgnoreForwardingRule();
							deviceIgnoreForwardingRule.setCreatedOn(Instant.now());
							deviceIgnoreForwardingRule.setCustomerForwardingRuleUuid(unmatchForwardingRuleUrlUuid);
							deviceIgnoreForwardingRule
									.setType(customerForwardingRuleDTOMap.get(unmatchForwardingRuleUrlUuid).getType());
							deviceIgnoreForwardingRule
									.setUrl(customerForwardingRuleDTOMap.get(unmatchForwardingRuleUrlUuid).getUrl());
							deviceIgnoreForwardingRule.setUuid(UUID.randomUUID().toString());
							deviceIgnoreForwardingRule.setCreatedBy(user);
							deviceIgnoreForwardingRule.setDevice(device);
							newDeviceIgnoreForwardingRules.add(deviceIgnoreForwardingRule);
						});
						LOGGER.info("total newDeviceIgnoreForwardingRules size to be inserted "
								+ newDeviceIgnoreForwardingRules.size());
						deviceIgnoreForwardingRuleRepository.saveAll(newDeviceIgnoreForwardingRules);
						LOGGER.info("saved deviceIgnoreForwardingRules");
						updateExcludeCustomerFrowardingRulesOnRedis(imei);
					}
				}

			} else if (StringUtils.equalsIgnoreCase(action, "restore")) {
				deleteExistingForwardingRule(imei);
				deleteExistingIgnoreForwardingRule(imei);
				updateDeviceForwardingOnRedis(imei);
			}
			LOGGER.info("DeviceForwarding Details saved successfully");
		} else {
			throw new Exception("DeviceForwardingRequest JSON Can't be NULL");
		}
		return deviceMessageMap;
	}

	private void deleteExistingForwardingRule(String imei) {
		deviceForwardingRepository.deleteByDeviceId(imei);
	}

	private void deleteExistingIgnoreForwardingRule(String imei) {
		LOGGER.info("deleting records for  imei " + imei);
		int deleteCount = deviceIgnoreForwardingRuleRepository.deleteByDeviceId(imei);
		LOGGER.info("deleted " + deleteCount + " records for  imei " + imei);
	}

	private void addForwardingRules(DeviceForwardingRequest deviceForwardingRequest, Device device, User user,
									Map<String, List<ForwardRuleResponseDTO.DeviceFwdRulResp>> deviceMessageMap, String imei, final int[] size,
									List<ForwardRuleResponseDTO.DeviceFwdRulResp> deviceFwdRulResps) {
		deviceForwardingRequest.getForwardingList().forEach(deviceForwardReq -> {
			if (size[0] != 5) {
				String type = deviceForwardReq.getType();
				String url = deviceForwardReq.getUrl();
				if ((type != null && !type.equals("")) && (url != null && !url.equals(""))) {
					DeviceForwarding deviceForwarding = new DeviceForwarding();
					boolean isDeviceForwardUuidUnique = false;
					String deviceForwardUuid = "";
					deviceForwardUuid = generateAndCheckGeneratedUUIDExist(isDeviceForwardUuidUnique,
							deviceForwardUuid);
					deviceForwarding.setUuid(deviceForwardUuid);
					deviceForwarding.setCreatedOn(Instant.now());
					deviceForwarding.setCreatedBy(user);
					deviceForwarding.setUrl(deviceForwardReq.getUrl());
					deviceForwarding.setForwardingRuleUrlUuid(deviceForwardReq.getForwardingRuleUrlUuid());
					deviceForwarding.setType(type);
					deviceForwarding.setDevice(device);
					deviceForwardingRepository.save(deviceForwarding);
					ForwardRuleResponseDTO.DeviceFwdRulResp deviceFwdRulResp = new ForwardRuleResponseDTO().new DeviceFwdRulResp();
					deviceFwdRulResp.setForwardingRuleUrlUuid(deviceForwardReq.getForwardingRuleUrlUuid());
					deviceFwdRulResp.setMessage("added");
					deviceFwdRulResp.setStatus(true);
					deviceFwdRulResps.add(deviceFwdRulResp);
					size[0]++;
				}
			} else {
				ForwardRuleResponseDTO.DeviceFwdRulResp deviceFwdRulResp = new ForwardRuleResponseDTO().new DeviceFwdRulResp();
				deviceFwdRulResp.setForwardingRuleUrlUuid(deviceForwardReq.getForwardingRuleUrlUuid());
				deviceFwdRulResp.setMessage("cannot add more than 5 rule");
				deviceFwdRulResp.setStatus(false);
				deviceFwdRulResps.add(deviceFwdRulResp);
			}
		});
		deviceMessageMap.put(imei, deviceFwdRulResps);
		updateDeviceForwardingOnRedis(imei);
	}

	private void filterForwardingRuleURLUuidNotToAdd(DeviceForwardingRequest deviceForwardingRequest,
													 List<DeviceForwarding> deviceForwardings, List<ForwardRuleResponseDTO.DeviceFwdRulResp> deviceFwdRulResps) {
		List<Forwarding> forwardingRuleUrlUuidsNotToAdd = deviceForwardingRequest.getForwardingList().stream()
				.filter(o1 -> deviceForwardings.stream()
						.anyMatch(o2 -> o2.getForwardingRuleUrlUuid().equals(o1.getForwardingRuleUrlUuid())))
				.collect(Collectors.toList());
		forwardingRuleUrlUuidsNotToAdd.forEach(forwardingRuleUrlUuid -> {
			ForwardRuleResponseDTO.DeviceFwdRulResp deviceFwdRulResp = new ForwardRuleResponseDTO().new DeviceFwdRulResp();
			deviceFwdRulResp.setForwardingRuleUrlUuid(forwardingRuleUrlUuid.getForwardingRuleUrlUuid());
			deviceFwdRulResp.setMessage("existed");
			deviceFwdRulResp.setStatus(false);
			deviceFwdRulResps.add(deviceFwdRulResp);
		});
	}

	private void filterForwardingRuleURLUuidToAdd(DeviceForwardingRequest deviceForwardingRequest,
												  List<DeviceForwarding> deviceForwardings) {
		List<Forwarding> forwardingRuleUrlUuidsToAdd = deviceForwardingRequest.getForwardingList().stream()
				.filter(o1 -> deviceForwardings.stream()
						.noneMatch(o2 -> o2.getForwardingRuleUrlUuid().equals(o1.getForwardingRuleUrlUuid())))
				.collect(Collectors.toList());
		deviceForwardingRequest.setForwardingList(forwardingRuleUrlUuidsToAdd);
	}

	private String generateAndCheckGeneratedUUIDExist(boolean isDeviceForwardUuidUnique, String deviceForwardUuid) {
		while (!isDeviceForwardUuidUnique) {
			deviceForwardUuid = UUID.randomUUID().toString();
			DeviceForwarding byUuid = deviceForwardingRepository.findByUuid(deviceForwardUuid);
			if (byUuid == null) {
				isDeviceForwardUuidUnique = true;
			}
		}
		return deviceForwardUuid;
	}
	
	@Override
	public void updateExcludeCustomerFrowardingRulesOnRedis(String imei) {
		LOGGER.info("Inside the excluded customer forwarding rules updating in redis db");
		Set<DeviceIgnoreForwardingRule> deviceIgnoreForwardingRules = deviceIgnoreForwardingRuleRepository
				.findByDeviceiImei(imei);

		List<String> urls = deviceIgnoreForwardingRules.stream().map(r -> r.getUrl()).collect(Collectors.toList());
		List<String> types = deviceIgnoreForwardingRules.stream().map(r -> r.getType()).collect(Collectors.toList());

		String key = KeyConstant.DEVICE_CURRENT_VIEW_PREFIX + imei;

		redisDeviceRepository.removeMap(key, KeyConstant.EXCLUDED_CUSTOMER_FROWARDING_URLS);
		redisDeviceRepository.removeMap(key, KeyConstant.EXCLUDED_CUSTOMER_FROWARDING_TYPES);

		redisDeviceRepository.addMap(key, KeyConstant.EXCLUDED_CUSTOMER_FROWARDING_URLS, JSON.toJSONString(urls));
		redisDeviceRepository.addMap(key, KeyConstant.EXCLUDED_CUSTOMER_FROWARDING_TYPES, JSON.toJSONString(types));

		LOGGER.info("Successfully finshed excluded customer forwarding rules updated in redis db");
	}
	
	@Override
	public List<ImportForwardingResponseDTO> importDeviceForwardingRules(List<ImportForwardingDTO> dtos, String type) {
		LOGGER.info("Inside the import device forwarding rules");
		
		List<ImportForwardingResponseDTO> importForwardingResponseDTOs = new ArrayList<>();
		
		if(dtos == null || dtos.isEmpty()) {
			return importForwardingResponseDTOs;
		}
		
		List<ImportForwardingDTO> importForwardingDTOs = null;
		
		if(type != null && !type.isEmpty()) {
		importForwardingDTOs = dtos.stream().filter(x -> x.getType().equals(type))
				.collect(Collectors.toList());
		} else {
			importForwardingDTOs = new ArrayList<>(dtos);
		}
		
		LOGGER.info("mport device forwarding rules count: {}", importForwardingDTOs.size());
		
		Set<String> deviceIds = importForwardingDTOs.stream()
				.filter(x -> x != null && x.getDeviceId() != null && !x.getDeviceId().isEmpty())
				.map(x -> x.getDeviceId()).collect(Collectors.toSet());
		
		List<Device> devices = new ArrayList<>();
		
		if (!deviceIds.isEmpty()) {
			devices = deviceRepository.getListOfDeviceByImeiList(deviceIds);
		}

		List<DeviceForwarding> deviceForwardings = new ArrayList<>();

		if (!deviceIds.isEmpty()) {
			deviceForwardings = deviceForwardingRepository.findByImeisIn(deviceIds);
		}
		
		Set<DeviceIgnoreForwardingRule> deviceIgnoreForwardingRules = new HashSet<>();

		if (!deviceIds.isEmpty()) {
			deviceIgnoreForwardingRules = deviceIgnoreForwardingRuleRepository.findByDeviceiImeisIn(deviceIds);
		}
		
		String token = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest()
				.getHeader("Authorization");

		List<CustomerForwardingRuleUrlDTO> customerForwardingRuleUrlDTOs = restUtils
				.getAllCustomerForwardingRuleUrl(token);
		
		List<CustomerForwardingRuleDTO> customerForwardingRuleDTOs = restUtils.getAllCustomerForwardingRules(token);
		
		JwtUser jwtUser = (JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		
		User user = restUtils.getUserFromAuthService(jwtUser.getUsername());
		
		List<DeviceIgnoreForwardingRule> deletingDeviceIgnoreForwardingRules = new ArrayList<>();
		
		List<DeviceForwarding> addingDeviceForwardings = new ArrayList<>();
		
		for (ImportForwardingDTO importForwardingDTO : importForwardingDTOs) {
			if (importForwardingDTO != null 
					&& importForwardingDTO.getDeviceId() != null
					&& !importForwardingDTO.getDeviceId().isEmpty()
					&& importForwardingDTO.getUrl() != null
					&& !importForwardingDTO.getUrl().isEmpty()
					&& importForwardingDTO.getType() != null
					&& !importForwardingDTO.getType().isEmpty()
					) {
				
				Optional<Device> deviceOptional = devices.stream()
						.filter(d -> d.getImei().equals(importForwardingDTO.getDeviceId())).findFirst();

				if (deviceOptional.isPresent()) {
					Optional<CustomerForwardingRuleUrlDTO> customerForwardingRuleUrlDTOptional = customerForwardingRuleUrlDTOs
							.stream().filter(x -> x.getType().equals(importForwardingDTO.getType())
									&& x.getEndpointDestination().equals(importForwardingDTO.getUrl()))
							.findFirst();

					if (customerForwardingRuleUrlDTOptional.isPresent()) {

						Optional<DeviceForwarding> deviceForwardingOptional = deviceForwardings.stream()
								.filter(x -> x.getType().equals(importForwardingDTO.getType())
										&& x.getUrl().equals(importForwardingDTO.getUrl())
										&& x.getDevice().getImei().equals(importForwardingDTO.getDeviceId()))
								.findFirst();

						if (!deviceForwardingOptional.isPresent()) {

							Optional<CustomerForwardingRuleDTO> customerForwardingRuleDTOptional = customerForwardingRuleDTOs
									.stream()
									.filter(c -> c.getOrganisation().getAccountNumber()
											.equals(deviceOptional.get().getOrganisation().getAccountNumber())
											&& c.getType().equals(importForwardingDTO.getType())
											&& c.getUrl().equals(importForwardingDTO.getUrl()))
									.findFirst();

							if (!customerForwardingRuleDTOptional.isPresent()) {
								DeviceForwarding deviceForwarding = new DeviceForwarding();
								
								do {
									deviceForwarding.setUuid(UUID.randomUUID().toString());
								} while (addingDeviceForwardings.stream()
										.anyMatch(x -> x.getUuid().equals(deviceForwarding.getUuid()))
										|| deviceForwardingRepository.existByUuid(deviceForwarding.getUuid()));
								
								deviceForwarding.setCreatedOn(Instant.now());
								deviceForwarding.setCreatedBy(user);

								deviceForwarding.setUpdatedBy(user);
								deviceForwarding.setUpdatedOn(Instant.now());
								deviceForwarding.setType(importForwardingDTO.getType());
								deviceForwarding.setUrl(importForwardingDTO.getUrl());
								deviceForwarding
										.setForwardingRuleUrlUuid(customerForwardingRuleUrlDTOptional.get().getUuid());
								deviceForwarding.setDevice(deviceOptional.get());
								addingDeviceForwardings.add(deviceForwarding);
								importForwardingResponseDTOs.add(new ImportForwardingResponseDTO(importForwardingDTO,
										true, "Successfully added"));
							} else {
								List<DeviceIgnoreForwardingRule> dIgnoreForwardingRules = deviceIgnoreForwardingRules
										.stream()
										.filter(c -> c.getDevice().getImei().equals(importForwardingDTO.getDeviceId())
												&& c.getType().equals(importForwardingDTO.getType())
												&& c.getUrl().equals(importForwardingDTO.getUrl()))
										.collect(Collectors.toList());

								deletingDeviceIgnoreForwardingRules.addAll(dIgnoreForwardingRules);
								if (dIgnoreForwardingRules.isEmpty()) {
									importForwardingResponseDTOs.add(new ImportForwardingResponseDTO(
											importForwardingDTO, true, "Already rule exist in customer level"));
								} else {
									importForwardingResponseDTOs
											.add(new ImportForwardingResponseDTO(importForwardingDTO, true,
													"Successfully remove ignore customer level rule"));
								}
							}

						} else {
							importForwardingResponseDTOs.add(new ImportForwardingResponseDTO(importForwardingDTO, false,
									"Device forwarding is already exist"));
						}

					} else {
						importForwardingResponseDTOs.add(new ImportForwardingResponseDTO(importForwardingDTO, false,
								"Customer forwarding rule url is not exist in database"));
					}
				} else {
					importForwardingResponseDTOs.add(new ImportForwardingResponseDTO(importForwardingDTO, false,
							"Device is not exist in database"));
				}
			} else {
				importForwardingResponseDTOs.add(new ImportForwardingResponseDTO(importForwardingDTO, false,
						"Invalid input"));
			}
		}
		
		if(!deletingDeviceIgnoreForwardingRules.isEmpty()) {
			deviceIgnoreForwardingRuleRepository.deleteAllInBatch(deletingDeviceIgnoreForwardingRules);
		}
		
		if(!addingDeviceForwardings.isEmpty()) {
			deviceForwardingRepository.saveAll(addingDeviceForwardings);
		}
		
		for (String deviceImei : addingDeviceForwardings.stream().map(x->x.getDevice().getImei()).collect(Collectors.toSet())) {
			updateDeviceForwardingOnRedis(deviceImei);
			updateExcludeCustomerFrowardingRulesOnRedis(deviceImei);
		}
		
		LOGGER.info("Successfully the import device forwarding rules");

		return importForwardingResponseDTOs;
	}
}
