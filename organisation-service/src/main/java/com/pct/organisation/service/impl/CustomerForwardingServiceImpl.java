package com.pct.organisation.service.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Instant;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSON;
import com.pct.common.dto.CustomerForwardingGroupDTO;
import com.pct.common.dto.CustomerForwardingRuleDTO;
import com.pct.common.dto.CustomerForwardingRuleUrlDTO;
import com.pct.common.model.CustomerForwardingRule;
import com.pct.common.model.CustomerForwardingRuleUrl;
import com.pct.common.model.Organisation;
import com.pct.common.model.User;
import com.pct.common.redis.CustomerPrefix;
import com.pct.common.util.Message;
import com.pct.organisation.config.RedisService;
import com.pct.organisation.exception.BaseMessageException;
import com.pct.organisation.payload.CustomerForwardingRuleUrlPayload;
import com.pct.organisation.repository.CustomerForwardingGroupMapperRepository;
import com.pct.organisation.repository.CustomerForwardingGroupRepository;
import com.pct.organisation.repository.CustomerForwardingRuleRepository;
import com.pct.organisation.repository.CustomerForwardingRuleUrlRepository;
import com.pct.organisation.repository.IOrganisationRepository;
import com.pct.organisation.service.CustomerForwardingService;
import com.pct.organisation.util.BeanConvertor;
import com.pct.organisation.util.RestUtils;

@Service
public class CustomerForwardingServiceImpl implements CustomerForwardingService {

	@Autowired
	private IOrganisationRepository iOrganisationRepository;

	@Autowired
	private CustomerForwardingGroupRepository customerForwardingGroupRepository;

	@Autowired
	private CustomerForwardingGroupMapperRepository customerForwardingGroupMapperRepository;

	@Autowired
	private CustomerForwardingRuleRepository customerForwardingRuleRepository;

	@Autowired
	private CustomerForwardingRuleUrlRepository customerForwardingRuleUrlRepository;

	@Autowired
	private BeanConvertor beanConvertor;

	@Autowired
	private RestUtils restUtils;

	@Autowired
	private RedisService redisService;

	private static final Map<String, Object> DATA_FORWARDING_TYPE_MAPPER = new HashMap<>();

	static {
		DATA_FORWARDING_TYPE_MAPPER.put("Standard JSON", "json");
		DATA_FORWARDING_TYPE_MAPPER.put("Elastic (RAW)", "elastic");
		DATA_FORWARDING_TYPE_MAPPER.put("Custom (UPS)", "ups");
		DATA_FORWARDING_TYPE_MAPPER.put("Kinesis (EROAD)", "eroad");
		DATA_FORWARDING_TYPE_MAPPER.put("kafka (Amazon)", "kafka");
	};

	private static final String CFR_FIELD_ACCOUNT_NUMBER = "ACCOUNT_NUMBER";
	private static final String CFR_FIELD_TYPES = "TYPES";
	private static final String CFR_FIELD_URLS = "URLS";

	private static final Logger LOGGER = LoggerFactory.getLogger(CustomerForwardingServiceImpl.class);

	@Override
	public List<CustomerForwardingGroupDTO> getAllCustomerForwardingGroup() {
		return customerForwardingGroupRepository.findAll().parallelStream().map(beanConvertor::convert)
				.collect(Collectors.toList());
	}

	@Override
	public List<CustomerForwardingGroupDTO> getCustomerForwardingGroupByOrganizationUuid(String OrganizationUuid) {
		return customerForwardingGroupMapperRepository.findCustomerForwardingGroupByOrganisationUuid(OrganizationUuid)
				.parallelStream().map(beanConvertor::convert).collect(Collectors.toList());
	}

	@Override
	public List<CustomerForwardingRuleDTO> getCustomerForwardingRulesByOrganizationUuids(
			Set<String> OrganizationUuids) {
		return customerForwardingRuleRepository.findByOrganisationUuidsIn(OrganizationUuids).parallelStream()
				.map(beanConvertor::convert).collect(Collectors.toList());
	}

	@Override
	public List<CustomerForwardingRuleUrlDTO> getAllCustomerForwardingRuleUrl() {
		return customerForwardingRuleUrlRepository.findAll().parallelStream().map(beanConvertor::convert)
				.collect(Collectors.toList());
	}

	@Transactional
	@Override
	public Map<String, Object> importCustomerForwardingRules(MultipartFile file) {

		LOGGER.info(" Inside the importCustomerForwardingRules method from CustomerForwardingServiceImpl ");

		validateImportFile(file);

		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		User user = restUtils.getUserFromAuthService(userDetails.getUsername());

		Map<String, String> ms1OrgNameMap = restUtils.getAllMS1And2OrgNameMap();

		Map<String, Object> resultMap = new HashMap<>();
		Map<String, Object> failureRecordDetails = new HashMap<>();

		List<String> records = getRecordsFromFile(file);
		records.remove(0);

		resultMap.put("totalRecord", records.size());

		Map<String, String> orgUuidAccountNumberNumberMap = new HashMap<>();

		int failureRecordCount = 0;
		int successRecordCount = 0;
		int alreadyExistForwardingUrlCount = 0;
		int alreadyExistCustomerForwardingCount = 0;
		for (String record : records) {
			boolean isValidRecord = true;
			String message = "";
			
			String[] recordCols = record.split(Pattern.quote(","));
			
			if(recordCols.length > 3 && record.startsWith("\"")) {
				List<String> cols = new ArrayList<>();
				String firstStr = record.substring(0, record.lastIndexOf("\"") + 1);
				firstStr = firstStr.replaceAll("\"", "");
				cols.add(firstStr);
				String secondStr = record.substring(record.lastIndexOf("\"") + 2);
				String []splitSecondStr = secondStr.split(Pattern.quote(","));
				
				cols.addAll(Arrays.asList(splitSecondStr));
				
				recordCols = cols.toArray(new String[cols.size()]);
			}
			
			if (recordCols.length != 3) {
				isValidRecord = false;
				message = "Required three column value customer_name, forward_url, format";
			} else {
				if (recordCols[0].isEmpty()) {
					isValidRecord = false;
					message = "Customer name is required";
				}
				if (recordCols[1].isEmpty()) {
					isValidRecord = false;
					message = "Forward url is required";
				}
				if (recordCols[2].isEmpty()) {
					isValidRecord = false;
					message = "Format is required";
				}

				if (!DATA_FORWARDING_TYPE_MAPPER.containsKey(recordCols[2])) {
					isValidRecord = false;
					message = "Invalid format type";
				}

				if (!iOrganisationRepository.existByOrganisationName(recordCols[0])) {
					String ms2OrgName = ms1OrgNameMap.get(recordCols[0]);
					if (ms2OrgName == null || !iOrganisationRepository.existByOrganisationName(ms2OrgName)) {
						isValidRecord = false;
						message = "Customer is not exist in db";
					} else {
						recordCols[0] = ms2OrgName;
					}
				}

			}

			if (isValidRecord) {

				CustomerForwardingRuleUrl customerForwardingRuleUrl = customerForwardingRuleUrlRepository
						.findByFormatAndEndpointDestination(recordCols[2], recordCols[1]);

				if (customerForwardingRuleUrl == null) {
					customerForwardingRuleUrl = new CustomerForwardingRuleUrl();
					customerForwardingRuleUrl.setCreatedBy(user);
					customerForwardingRuleUrl.setCreatedOn(Instant.now());
					customerForwardingRuleUrl.setEndpointDestination(recordCols[1]);
					customerForwardingRuleUrl.setUuid(UUID.randomUUID().toString());
					customerForwardingRuleUrl.setFormat(recordCols[2]);
					customerForwardingRuleUrl.setRuleName(recordCols[0]);
					customerForwardingRuleUrl
							.setDescription(String.format("%s endpoint (%s format)", recordCols[0], recordCols[2]));
					customerForwardingRuleUrl = customerForwardingRuleUrlRepository.save(customerForwardingRuleUrl);
				} else {
					alreadyExistForwardingUrlCount++;
				}

				Organisation organisation = iOrganisationRepository.findByOrganisationByOrganisationName(recordCols[0])
						.get(0);

				CustomerForwardingRule customerForwardingRule = customerForwardingRuleRepository
						.findByOrganisationUuidAndForwardingRuleUrlUuid(organisation.getUuid(),
								customerForwardingRuleUrl.getUuid());

				if (customerForwardingRule == null) {
					customerForwardingRule = new CustomerForwardingRule();
					customerForwardingRule.setCreatedBy(user);
					customerForwardingRule.setCreatedOn(Instant.now());
					customerForwardingRule.setForwardingRuleUrl(customerForwardingRuleUrl);
					customerForwardingRule.setOrganisation(organisation);
					customerForwardingRule.setType((String) DATA_FORWARDING_TYPE_MAPPER.get(recordCols[2]));
					customerForwardingRule.setUrl(customerForwardingRuleUrl.getEndpointDestination());
					customerForwardingRule.setUuid(UUID.randomUUID().toString());
					customerForwardingRuleRepository.save(customerForwardingRule);
					orgUuidAccountNumberNumberMap.put(organisation.getUuid(), organisation.getAccountNumber());
				} else {
					alreadyExistCustomerForwardingCount++;
				}
				successRecordCount++;
			} else {
				failureRecordCount++;
				failureRecordDetails.put(record, message);
			}

		}

		updateCustomerForwardingOnRedis(orgUuidAccountNumberNumberMap);

		resultMap.put("failureRecordCount", failureRecordCount);
		resultMap.put("successRecordCount", successRecordCount);
		resultMap.put("alreadyExistForwardingUrlCount", alreadyExistForwardingUrlCount);
		resultMap.put("alreadyExistCustomerForwardingCount", alreadyExistCustomerForwardingCount);
		resultMap.put("failureRecordDetails", failureRecordDetails);

		LOGGER.info("Result Of import file is {}", resultMap);

		LOGGER.info("file import successfully done...");

		return resultMap;
	}

	private List<String> getRecordsFromFile(MultipartFile file) {

		LOGGER.info(" Inside the getRecordsFromFile method from CustomerForwardingServiceImpl ");

		List<String> records = new ArrayList<>();
		try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
			String line = null;
			while ((line = br.readLine()) != null) {
				records.add(line);
			}
		} catch (IOException e) {
			throw new BaseMessageException(new Message("Invalid file: error in read file"));
		}

		LOGGER.info("Read file data successfully");
		return records;
	}

	private void validateImportFile(MultipartFile file) {
		LOGGER.info(" Inside the validateImportFile method from CustomerForwardingServiceImpl ");
		if (file == null) {
			throw new BaseMessageException(new Message("File is null"));
		}

		if (file.isEmpty()) {
			throw new BaseMessageException(new Message("File is empty"));
		}

		if (file.getName() == null || file.getName().isEmpty()) {
			throw new BaseMessageException(new Message("File name is null or empty"));
		}

		int index = file.getOriginalFilename().lastIndexOf('.');
		if (index == -1 || !file.getOriginalFilename().substring(index + 1).equalsIgnoreCase("csv")) {
			throw new BaseMessageException(new Message("Require csv file"));
		}
		LOGGER.info("Validated file");
	}

	private void updateCustomerForwardingOnRedis(Map<String, String> orgUuidAccountNumberNumberMap) {
		LOGGER.info(" Inside the updateCustomerForwardingOnRedis method from CustomerForwardingServiceImpl ");
		orgUuidAccountNumberNumberMap.entrySet().forEach(entry -> {
			List<CustomerForwardingRule> customerForwardingRules = customerForwardingRuleRepository
					.findByOrganisationUuid(entry.getKey());
			saveCustomerForwardingRulesOnRedis(customerForwardingRules, entry.getValue());
		});
	}

	private void saveCustomerForwardingRulesOnRedis(List<CustomerForwardingRule> newCustomerForwardingRules,
			String accountNumber) {
		LOGGER.info(" Inside the saveCustomerForwardingRulesOnRedis method from CustomerForwardingServiceImpl ");

		List<String> urls = new ArrayList<>();
		List<String> types = new ArrayList<>();

		for (CustomerForwardingRule customerForwardingRule : newCustomerForwardingRules) {
			types.add(customerForwardingRule.getType());
			urls.add(customerForwardingRule.getUrl());
		}

		redisService.hdel(CustomerPrefix.getForwardingRule, accountNumber, CFR_FIELD_URLS);
		redisService.hdel(CustomerPrefix.getForwardingRule, accountNumber, CFR_FIELD_TYPES);

		if (!urls.isEmpty()) {
			redisService.hset(CustomerPrefix.getForwardingRule, accountNumber, CFR_FIELD_URLS, JSON.toJSONString(urls));
		}

		if (!types.isEmpty()) {
			redisService.hset(CustomerPrefix.getForwardingRule, accountNumber, CFR_FIELD_TYPES,
					JSON.toJSONString(types));
		}

		putDeviceidsWithAccountNumberInRedis(accountNumber);

		LOGGER.info("Customer forwarding rule urls {} record(s) save successfully in redis db", urls.size());
		LOGGER.info("Customer forwarding rule types {} record(s) save successfully in redis db", types.size());

		LOGGER.info(" Successfully save customer forwarding rules on redis ");
	}

	private void putDeviceidsWithAccountNumberInRedis(String accountNumber) {
		LOGGER.info(" Inside the putDeviceidsWithAccountNumberInRedis method from CustomerForwardingServiceImpl ");

		Set<String> deviceIds = restUtils.getImeisByAccountNumber(accountNumber);
		deviceIds.remove(null);

		if (!deviceIds.isEmpty()) {
			Set<String> deviceKeys = redisService.getKeys(CustomerPrefix.getDevice.getPrefix() + "*");

			Optional<String> idOptional = deviceIds.parallelStream()
					.filter(id -> deviceKeys.contains(CustomerPrefix.getDevice.getPrefix() + id)).findAny();
			boolean isEditedAccountNumber = false;
			if (idOptional.isPresent()) {
				String accountNumberRedis = redisService.hget(CustomerPrefix.getDevice, idOptional.get(),
						CFR_FIELD_ACCOUNT_NUMBER);
				if (accountNumberRedis != null && !accountNumber.equals(accountNumberRedis)) {
					isEditedAccountNumber = true;
				}
			}

			if (!isEditedAccountNumber) {
				deviceIds.removeIf(id -> deviceKeys.contains(CustomerPrefix.getDevice.getPrefix() + id));
			}
		}

		for (String deviceId : deviceIds) {
			redisService.hset(CustomerPrefix.getDevice, deviceId, CFR_FIELD_ACCOUNT_NUMBER, accountNumber);
		}
		LOGGER.info(" Successfully put device ids with account number in redis ");
	}

	@Transactional
	@Override
	public CustomerForwardingRuleUrlDTO createCustomerForwardingRuleUrl(CustomerForwardingRuleUrlPayload payload) {

		LOGGER.info(
				" Inside the createCustomerForwardingRuleUrl method from CustomerForwardingServiceImpl with payload {}",
				payload);

		validateCustomerForwardingRuleUrlPayload(payload);

		CustomerForwardingRuleUrl customerForwardingRuleUrl = new CustomerForwardingRuleUrl();

		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		User user = restUtils.getUserFromAuthService(userDetails.getUsername());

		customerForwardingRuleUrl.setCreatedBy(user);
		customerForwardingRuleUrl.setCreatedOn(Instant.now());
		customerForwardingRuleUrl.setDescription(payload.getDescription());
		customerForwardingRuleUrl.setEndpointDestination(payload.getEndpointDestination());
		customerForwardingRuleUrl.setFormat(payload.getFormat());
		customerForwardingRuleUrl.setKafkaTopicName(payload.getKafkaTopicName());
		customerForwardingRuleUrl.setRuleName(payload.getRuleName());
		customerForwardingRuleUrl.setType(payload.getType());
		customerForwardingRuleUrl.setUuid(getCustomerForwardingRuleUrlUniqueUuid());
		
		customerForwardingRuleUrl = customerForwardingRuleUrlRepository.save(customerForwardingRuleUrl);

		LOGGER.info(" Successfully customer forwarding rule url created ");

		return beanConvertor.convert(customerForwardingRuleUrl);
	}
	
	private String getCustomerForwardingRuleUrlUniqueUuid() {
		boolean isUuidUnique = false;
		String uuid = "";
		while (!isUuidUnique) {
			uuid = UUID.randomUUID().toString();
			CustomerForwardingRuleUrl customerForwardingRuleUrl = customerForwardingRuleUrlRepository.findByUuid(uuid);
			if (customerForwardingRuleUrl == null) {
				isUuidUnique = true;
			}
		}
		return uuid;
	}

	@Transactional
	@Override
	public CustomerForwardingRuleUrlDTO modifyCustomerForwardingRuleUrl(String uuid,
			CustomerForwardingRuleUrlPayload payload) {

		LOGGER.info(
				" Inside the modifyCustomerForwardingRuleUrl method from CustomerForwardingServiceImpl with payload {}",
				payload);

		validateCustomerForwardingRuleUrlPayload(payload);

		CustomerForwardingRuleUrl customerForwardingRuleUrl = customerForwardingRuleUrlRepository.findByUuid(uuid);
		
		if(customerForwardingRuleUrl == null) {
			throw new BaseMessageException("Customer forwarding rule url is not exist");
		}
		
		customerForwardingRuleUrl.setDescription(payload.getDescription());
		customerForwardingRuleUrl.setEndpointDestination(payload.getEndpointDestination());
		customerForwardingRuleUrl.setFormat(payload.getFormat());
		customerForwardingRuleUrl.setKafkaTopicName(payload.getKafkaTopicName());
		customerForwardingRuleUrl.setRuleName(payload.getRuleName());
		customerForwardingRuleUrl.setType(payload.getType());

		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		User user = restUtils.getUserFromAuthService(userDetails.getUsername());

		customerForwardingRuleUrl.setUpdatedBy(user);
		customerForwardingRuleUrl.setUpdatedOn(Instant.now());

		customerForwardingRuleUrl = customerForwardingRuleUrlRepository.save(customerForwardingRuleUrl);

		LOGGER.info(" Successfully customer forwarding rule url updated ");

		return beanConvertor.convert(customerForwardingRuleUrl);
	}

	private void validateCustomerForwardingRuleUrlPayload(CustomerForwardingRuleUrlPayload payload) {
		if (payload == null) {
			throw new BaseMessageException("Request payload is not present");
		}
		
		if(payload.getEndpointDestination() == null || payload.getEndpointDestination().isEmpty()) {
			throw new BaseMessageException("Endpoint destination is not present");
		}
		
		if(payload.getRuleName() == null || payload.getRuleName().isEmpty()) {
			throw new BaseMessageException("Rule name is not present");
		}

		if(payload.getKafkaTopicName() == null || payload.getKafkaTopicName().isEmpty()) {
			throw new BaseMessageException("Kafka topic name is not present");
		}
		
		if(payload.getFormat() == null || payload.getFormat().isEmpty()) {
			throw new BaseMessageException("Format is not present");
		}
		
		if(payload.getType() == null || payload.getType().isEmpty()) {
			throw new BaseMessageException("Type is not present");
		}
		
		if(!DATA_FORWARDING_TYPE_MAPPER.containsKey(payload.getFormat())) {
			throw new BaseMessageException("Invalid format");
		}
		
		String type = (String) DATA_FORWARDING_TYPE_MAPPER.get(payload.getFormat());
		
		if(!payload.getType().equals(type)) {
			throw new BaseMessageException(String.format("Invalid Type. %s format only allow with %s type", payload.getFormat(), type));
		}
	}

	@Override
	public List<CustomerForwardingRuleDTO> getAllCustomerForwardingRules() {
		return customerForwardingRuleRepository.findAll().parallelStream().map(beanConvertor::convert).collect(Collectors.toList());
	}
	
	@Override
	public List<String> getAllCustomerForwardingGroupName() {
		return customerForwardingGroupRepository.findAllDistinctName();
	}
}
