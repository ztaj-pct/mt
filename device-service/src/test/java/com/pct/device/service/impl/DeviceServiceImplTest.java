package com.pct.device.service.impl;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.collections.map.HashedMap;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.RestClients;
import org.springframework.data.jpa.domain.Specification;

import com.netflix.discovery.EurekaClient;
import com.pct.common.constant.DeviceStatus;
import com.pct.common.constant.IOTType;
import com.pct.common.constant.OrganisationRole;
import com.pct.common.dto.CustomerForwardingRuleDTO;
import com.pct.common.dto.CustomerForwardingRuleUrlDTO;
import com.pct.common.dto.OrganisationDTO;
import com.pct.common.model.Cellular;
import com.pct.common.model.Device;
import com.pct.common.model.DeviceIgnoreForwardingRule;
import com.pct.common.model.Organisation;
import com.pct.common.model.PermissionEntity;
import com.pct.common.model.Role;
import com.pct.common.model.User;
import com.pct.device.model.ColumnDefs;
import com.pct.device.model.DeviceStatusTransient;
import com.pct.device.payload.CellularPayload;
import com.pct.device.payload.DeviceDetailPayLoad;
import com.pct.device.payload.DeviceDetailsRequest;
import com.pct.device.payload.DeviceResponsePayload;
import com.pct.device.payload.UpdateDeviceStatusPayload;
import com.pct.device.repository.DeviceIgnoreForwardingRuleRepository;
import com.pct.device.repository.ICellularRepository;
import com.pct.device.repository.IColumnDefsRepository;
import com.pct.device.repository.IDeviceRepository;
import com.pct.device.repository.IDeviceStatusTransientRepository;
import com.pct.device.repository.RedisDeviceRepository;
import com.pct.device.specification.DeviceSpecification;
import com.pct.device.util.BeanConverter;
import com.pct.device.util.RestUtils;
import com.pct.es.dto.Filter;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
public class DeviceServiceImplTest {

	Logger logger = LoggerFactory.getLogger(DeviceServiceImplTest.class);

	@Mock
	IDeviceStatusTransientRepository deviceStatusTransientRepo;

	@Mock
	RedisDeviceRepository redisDeviceRepository;

	@Mock
	private IDeviceRepository deviceRepository;
	
	@Mock
	private DeviceIgnoreForwardingRuleRepository deviceIgnoreForwardingRuleRepository;

	@Mock
	private EurekaClient eurekaClient;

	@Mock
	private BeanConverter beanConverter;

	@Mock
	private RestUtils restUtils;

	@Mock
	Specification<Device> spc;

	@InjectMocks
	private DeviceServiceImpl service;

	@Mock
	private ICellularRepository cellularRepository;

	@Mock
	private IColumnDefsRepository columnDefsRepo;

	private User getUser() {

		OrganisationRole type = OrganisationRole.END_CUSTOMER;

		Organisation organisation = new Organisation();
		organisation.setUuid("84d6efc0-37ad-4cde-98c9-79611e2fab60");
		organisation.setAccountNumber("52454885");
		organisation.setShortName("short_name");
		organisation.setIsActive(true);
		organisation.setIsAssetListRequired(true);

		PermissionEntity permissionEntity = new PermissionEntity();
		permissionEntity.setCreatedAt(Instant.now());
		permissionEntity.setCreatedBy("createdBy");
		permissionEntity.setDescription("description");
//		permissionEntity.setMethodType(null);
		permissionEntity.setName("name");
		permissionEntity.setPath("path");
		permissionEntity.setPermissionId(3123);
		permissionEntity.setUpdatedAt(Instant.now());
		permissionEntity.setUpdatedBy("updatedBy");

		List<PermissionEntity> permission = new ArrayList<PermissionEntity>();
		permission.add(permissionEntity);

		List<Role> roleList = new ArrayList<>();
		Role role = new Role();
		role.setDescription("description");
		role.setName("name");
		role.setCreatedAt(Instant.now());
		role.setCreatedBy("createdBy");
		role.setDeleted(false);
		role.setRoleId(123L);
		role.setUpdatedAt(Instant.now());
		role.setUpdatedBy("updatedBy");
		role.setPermissions(permission);
		roleList.add(role);

		User user = new User();
		user.setUuid("84d6efc0-37ad-4cde-98c9-79611e2fab60");
		user.setFirstName("first_name");
		user.setIsActive(true);
		user.setUserName("user_name");
		user.setNotify("notify");
		user.setCountryCode("countryCode");
		user.setEmail("@gmail");
		user.setIsDeleted(true);
		user.setIsPasswordChange(true);
		user.setLastName("lastName");
		user.setOrganisation(organisation);
		user.setPassword("Password");
		user.setPhone("Phone");
		user.setRole(roleList);
		user.setTimeZone("timeZone");
		user.setId(321L);
		return user;
	}

	private Device getDeviceData() {
		Device device = new Device();
		device.setProductCode("5435454");
		device.setProductName("product_name");
		device.setCreatedAt(Instant.now());
		device.setUpdatedAt(Instant.now());
		device.setMacAddress("mac_address");
		device.setEpicorOrderNumber("354565");
		device.setImei("354892034904997");
		Organisation organisation = new Organisation();
		organisation.setUuid("84d6efc0-37ad-4cde-98c9-79611e2fab60");
		device.setOrganisation(organisation);
		return device;
	}

	@Test
	@Order(1)
	public void test_addDeviceDetail() {

		User user = getUser();
		Device device = getDeviceData();
		device.setOrganisation(user.getOrganisation());

		DeviceDetailsRequest deviceUploadRequest = new DeviceDetailsRequest();
		deviceUploadRequest.setProductName("product_name");
		deviceUploadRequest.setProductCode("product_code");
		deviceUploadRequest.setQuantityShipped("quantity_shipped");
		deviceUploadRequest.setCan("can");
		deviceUploadRequest.setMacAddress("mac_address1");
		deviceUploadRequest.setImei("354892034904997");

		Organisation company = user.getOrganisation();
		when(restUtils.getCompanyFromCompanyService(deviceUploadRequest.getCan())).thenReturn(company);
		when(restUtils.getUserFromAuthService(user.getUserName())).thenReturn(user);
		when(deviceRepository.findByMac_address(deviceUploadRequest.getMacAddress())).thenReturn(device);
		when(deviceRepository.findByImei(deviceUploadRequest.getImei())).thenReturn(null);
		when(beanConverter.convertDeviceDetailRequestToDeviceBean(deviceUploadRequest)).thenReturn(device);

		boolean isDeviceUuidUnique = false;
		String deviceUuid = "";
		while (!isDeviceUuidUnique) {
			deviceUuid = UUID.randomUUID().toString();
			Device byUuid = deviceRepository.findByUuid(deviceUuid);
			if (byUuid == null) {
				isDeviceUuidUnique = true;
			}
		}
		device.setCreatedBy(user);
		device.setUuid(deviceUuid);
		when(deviceRepository.save(any(Device.class))).thenReturn(device);
		logger.info("Device Details saved successfully");
		doNothing().when(redisDeviceRepository).addMap(anyString(), anyString());
		deviceUploadRequest.setDeviceType("mnmn");
		Boolean addDeviceDetail = service.addDeviceDetail(deviceUploadRequest, user.getUserName());
		assertNotNull(addDeviceDetail);
		verify(deviceRepository, atLeast(1)).save(device);
	}

	@Test
	@Order(2)
	public void test_updateDeviceStatus() throws Throwable {
		UpdateDeviceStatusPayload deviceStatusPayload = new UpdateDeviceStatusPayload();
		deviceStatusPayload.setDeviceUuid("84d6efc0-37ad-4cde-98c9-79611e2fab60");
		deviceStatusPayload.setStatus("status");

		Device device = getDeviceData();
		device.setStatus(DeviceStatus.PENDING);

		when(deviceRepository.findByUuid(deviceStatusPayload.getDeviceUuid())).thenReturn(device);
		device.setUpdatedAt(Instant.now());

		when(deviceRepository.save(device)).thenReturn(device);
		DeviceStatusTransient deviceStatusTransient = new DeviceStatusTransient();
		deviceStatusTransient.setDate_created(Instant.now());
		deviceStatusTransient.setDeviceId(device.getImei());
		deviceStatusTransient.setStatus(DeviceStatus.INSTALL_IN_PROGRESS);
		when(deviceStatusTransientRepo.save(deviceStatusTransient)).thenReturn(deviceStatusTransient);

		logger.info("Device Updated successfully for uuid " + deviceStatusPayload.getDeviceUuid());

		Device updateDeviceStatus = service.updateDeviceStatus(deviceStatusPayload);
		assertNotNull(updateDeviceStatus);

		verify(deviceRepository, atLeast(1)).save(device);
	}

	private DeviceDetailPayLoad getDeviceDetailPayLoad() {
		CellularPayload cellularPayload = new CellularPayload();
		cellularPayload.setCarrierId("CarrierId");
		cellularPayload.setCellular("cellular");
		cellularPayload.setCountryCode("countrycode");
		cellularPayload.setIccid("iccid");
		cellularPayload.setImei("354892034904997");
		cellularPayload.setImsi("imesi");
		cellularPayload.setPhone("phone");
		cellularPayload.setServiceCountry("country");
		cellularPayload.setUuid("84d6efc0-37ad-4cde-98c9-79611e2fab60");

		DeviceDetailPayLoad deviceDetailPayLoad = new DeviceDetailPayLoad();
		deviceDetailPayLoad.setProductName("productname");
		deviceDetailPayLoad.setProductCode("product_code");
		deviceDetailPayLoad.setCan("can");
		deviceDetailPayLoad.setConfig1("config1");
		deviceDetailPayLoad.setQuantityShipped("quantity_shipped");
		deviceDetailPayLoad.setAppVersion("app_version");
		deviceDetailPayLoad.setBinVersion("bin_version");
		deviceDetailPayLoad.setBleVersion("ble_version");
		deviceDetailPayLoad.setConfig2("config2");
		deviceDetailPayLoad.setConfig3("config3");
		deviceDetailPayLoad.setConfig4("config4");
		deviceDetailPayLoad.setEpicorOrderNumber("35454");
		deviceDetailPayLoad.setImei("354892034904997");
		deviceDetailPayLoad.setMacAddress("mac_address");
		deviceDetailPayLoad.setMcuVersion("mcu_version");
		deviceDetailPayLoad.setOther1Version("other1version");
		deviceDetailPayLoad.setOther2Version("other2version");
		deviceDetailPayLoad.setSon("son");
		deviceDetailPayLoad.setIotType(IOTType.SENSOR);
		deviceDetailPayLoad.setUuid("84d6efc0-37ad-4cde-98c9-79611e2fab60");
		deviceDetailPayLoad.setCellularPayload(cellularPayload);
		return deviceDetailPayLoad;
	}

	@Test
	@Order(3)
	public void test_updateDeviceDetail() {
		DeviceDetailPayLoad deviceDetailPayLoad = getDeviceDetailPayLoad();
		User user = getUser();
		Device device = getDeviceData();

		Cellular cellularPayload1 = new Cellular();
		cellularPayload1.setCarrierId("CarrierId");
		cellularPayload1.setCellular("cellular");
		cellularPayload1.setCountryCode("countrycode");
		cellularPayload1.setIccid("iccid");
		cellularPayload1.setImei("354892034904997");
		cellularPayload1.setImsi("imesi");
		cellularPayload1.setPhone("phone");
		cellularPayload1.setServiceCountry("country");
		cellularPayload1.setUuid("84d6efc0-37ad-4cde-98c9-79611e2fab60");

		when(restUtils.getUserFromAuthService(user.getUserName())).thenReturn(user);

		when(deviceRepository.findByUuid(deviceDetailPayLoad.getUuid())).thenReturn(device);

		logger.info("Fetching device details for uuid : " + deviceDetailPayLoad.getUuid());

		device.setImei(deviceDetailPayLoad.getImei());
		device.setProductCode(deviceDetailPayLoad.getProductCode());
		device.setProductName(deviceDetailPayLoad.getProductName());
		device.setSon(deviceDetailPayLoad.getSon());
		device.setIotType(deviceDetailPayLoad.getIotType());
		device.setUpdatedAt(Instant.now());
		device.setUpdatedBy(user);
		
		device.setEpicorOrderNumber(deviceDetailPayLoad.getEpicorOrderNumber());
		device.setMacAddress(deviceDetailPayLoad.getMacAddress());
		device.setQuantityShipped(deviceDetailPayLoad.getQuantityShipped());
		when(deviceRepository.save(any(Device.class))).thenReturn(device);
		when(cellularRepository.findByUuid(deviceDetailPayLoad.getCellularPayload().getUuid()))
				.thenReturn(cellularPayload1);
		Boolean updateDeviceDetail = service.updateDeviceDetail(deviceDetailPayLoad, user.getUserName());
		assertNotNull(updateDeviceDetail);
		verify(deviceRepository, atLeast(1)).save(device);
	}

	@Test
	@Order(4)
	public void test_getDeviceDetails() {
		String accountNumber = "5100000000404390";
		String uuid = "e930d55b-c4f4-4a96-918e-dcf0b979649f";
		String deviceId = "123";

		logger.info("Inside getDeviceDetails for account number " + accountNumber + " uuid " + uuid);

		Device device = new Device();
		device.setProductCode("5435454");
		device.setProductName("product_name");
		device.setCreatedAt(Instant.now());
		device.setUpdatedAt(Instant.now());
		device.setMacAddress("mac_address");
		device.setEpicorOrderNumber("354565");
		device.setImei("354892034904997");
		device.setUuid("84d6efc0-37ad-4cde-98c9-79611e2fab60");

		List<Device> deviceList = new ArrayList<Device>();
		deviceList.add(device);

//		Specification<Device> spc = DeviceSpecification.getDeviceListSpec(accountNumber,uuid);
		logger.info("Fetching device details based on specification.");

		when(deviceRepository.findAll(Mockito.any(Specification.class))).thenReturn(deviceList);

		List<DeviceResponsePayload> deviceDetails = service.getDeviceDetails(accountNumber, uuid, deviceId);
		assertNotNull(deviceDetails);

	}

	@Test
	@Order(5)
	public void test_deleteDeviceDetail() {
		String can = "can";
		String imei = "354892034904997";
		String uuid = "84d6efc0-37ad-4cde-98c9-79611e2fab60";
		String accountNo = "account-no";
		Long ID = 49L;
		String OrganisationName = "company";
		IOTType type = IOTType.getValue("Gateway");

		logger.info("Inside Device Delete");

		Organisation company = new Organisation();
		company.setAccountNumber(accountNo);
		company.setId(ID);
		company.setIsActive(true);
		company.setIsAssetListRequired(true);
		company.setOrganisationName(OrganisationName);
		company.setUuid(uuid);

		Cellular cell = new Cellular();
		cell.setCarrierId("CarrierId");
		cell.setCellular("cellular");
		cell.setCountryCode("countrycode");
		cell.setIccid("iccid");
		cell.setImei("354892034904997");
		cell.setImsi("imesi");
		cell.setPhone("phone");
		cell.setServiceCountry("country");
		cell.setUuid("84d6efc0-37ad-4cde-98c9-79611e2fab60");

		when(restUtils.getCompanyFromCompanyService(can)).thenReturn(company);

		Device device = new Device();
		device.setProductCode("5435454");
		device.setProductName("product_name");
		device.setCreatedAt(Instant.now());
		device.setUpdatedAt(Instant.now());
		device.setMacAddress("mac_address");
		device.setEpicorOrderNumber("354565");
		device.setImei("354892034904997");
		device.setUuid("84d6efc0-37ad-4cde-98c9-79611e2fab60");
		device.setCellular(cell);

		List<Device> deviceList = new ArrayList<>();
		deviceList.add(device);

		@SuppressWarnings("unused")
		Specification<Device> spc = DeviceSpecification.getDeviceSpec(can, imei, uuid, type);
		when(deviceRepository.findAll(Mockito.any(Specification.class))).thenReturn(deviceList);
		logger.info("After fetching device details" + deviceList.toString());
		when(cellularRepository.findByUuid(device.getCellular().getUuid())).thenReturn(cell);
		boolean deleteDeviceDetail = service.deleteDeviceDetail(can, imei, uuid, type);
		assertTrue(deleteDeviceDetail);
	}

	@Test
	@Order(6)
	public void test_getDeviceWithPagination() {
		String accountNumber = "5100000000404390";
		String imei = "354892034904997";
		String uuid = "84d6efc0-37ad-4cde-98c9-79611e2fab60";
		DeviceStatus status = DeviceStatus.ACTIVE;
		String mac = "mac";
		Filter filter = new Filter();
		filter.setKey("1");
		filter.setOperator("ed");
		filter.setValue(imei);
		Map<String, Filter> filterValues = new HashedMap();
		filterValues.put(uuid, filter);
		String filterModelCountFilter = "filterModelCounterFilter";
		IOTType dType = IOTType.getValue("Gateway");
		Pageable pageable = PageRequest.of(0, 8);
		String can = "can";
		String Son = "goku";
		String BinVersion = "4.1";
		String setBleVersion = "5.1";
		String setMacAddress = "01-23-45-67-89-AB";
		String setConfig1 = "config1";
		String setEpicorOrderNumber = "EpicorOrderNumber";
		String setMcuVersion = "McuVersion";
		String userName = "user";
		String token = "eyJhbGciOiJIUzI1NiJ9";

		Device device = getDeviceData();
		List<Device> list = new ArrayList<Device>();
		list.add(device);

		Page<Device> deviceDetails = new PageImpl<>(list);
//		spc = DeviceSpecification.getDeviceListSpecification(accountNumber,imei,uuid,status,dType,mac,filterValues,filterModelCountFilter);
		logger.info("After Specification " + spc);

		when(deviceRepository.findAll(Mockito.any(Specification.class), Mockito.any(Pageable.class)))
				.thenReturn(deviceDetails);
		Set<String> imeis = new HashSet<>();
		imeis.add(imei);
		Set<DeviceIgnoreForwardingRule> deviceIgnoreForwardingRules = new HashSet<>();
		DeviceIgnoreForwardingRule deviceIgnoreForwardingRule = new DeviceIgnoreForwardingRule();
		deviceIgnoreForwardingRules.add(deviceIgnoreForwardingRule);
		when(deviceIgnoreForwardingRuleRepository.findByDeviceiImeisIn(imeis)).thenReturn(deviceIgnoreForwardingRules);
		
		logger.info("After DB Call " + deviceDetails);

		DeviceResponsePayload deviceResponsePayload = new DeviceResponsePayload();
		deviceResponsePayload.setAppVersion(setMcuVersion);
		deviceResponsePayload.setBinVersion(BinVersion);
		deviceResponsePayload.setBleVersion(setBleVersion);
		deviceResponsePayload.setCan(can);
		deviceResponsePayload.setImei(imei);
		deviceResponsePayload.setMacAddress(setMacAddress);
		deviceResponsePayload.setMcuVersion(setMcuVersion);
		deviceResponsePayload.setConfig1(setConfig1);
		deviceResponsePayload.setEpicorOrderNumber(setEpicorOrderNumber);
		deviceResponsePayload.setSon(Son);
		deviceResponsePayload.setUuid(uuid);
		deviceResponsePayload.setStatus(status);

		List<DeviceResponsePayload> list1 = new ArrayList<>();
		list1.add(deviceResponsePayload);
		Page<DeviceResponsePayload> deviceRecordloadPage = new PageImpl<>(list1);

		User user = new User();
		Mockito.when(restUtils.getUserFromAuthServiceWithToken(userName, token)).thenReturn(user);
		
		List<CustomerForwardingRuleUrlDTO> customerForwardingRuleUrls = new ArrayList<>();
		CustomerForwardingRuleUrlDTO customerForwardingRuleUrlDTO = new CustomerForwardingRuleUrlDTO();
		customerForwardingRuleUrlDTO.setUuid(uuid);
		customerForwardingRuleUrls.add(customerForwardingRuleUrlDTO);
		Mockito.when(restUtils.getAllCustomerForwardingRuleUrl(token)).thenReturn(customerForwardingRuleUrls);
		
		List<CustomerForwardingRuleDTO> customerForwardingRules = new ArrayList<>();
		CustomerForwardingRuleDTO customerForwardingRuleDTO = new CustomerForwardingRuleDTO();
		customerForwardingRuleDTO.setForwardingRuleUrl(customerForwardingRuleUrlDTO);
		customerForwardingRuleDTO.setUuid(uuid);
		OrganisationDTO organisationDTO = new OrganisationDTO();
		organisationDTO.setUuid(uuid);
		organisationDTO.setOrganisationName("org");;
		customerForwardingRuleDTO.setOrganisation(organisationDTO);;
		customerForwardingRules.add(customerForwardingRuleDTO);
		Mockito.when(restUtils.getAllCustomerForwardingRules(token)).thenReturn(customerForwardingRules);
		
		List<Map<String, String>> customerForwardingRulesMap = new ArrayList<>();
		Map<String, String> map = new HashMap<>();
		map.put("ruleName", null);
		map.put("sourceName", customerForwardingRuleDTO.getOrganisation().getOrganisationName());
		map.put("uuid", customerForwardingRuleUrlDTO.getUuid());
		map.put("ruleUuid", customerForwardingRuleDTO.getUuid());
		map.put("orgUuid", uuid);
		customerForwardingRulesMap.add(map);
		
		when(beanConverter.convertDeviceToDevicePayLoad1("",deviceDetails, pageable, false, customerForwardingRuleUrls,
				customerForwardingRulesMap, deviceIgnoreForwardingRules)).thenReturn(deviceRecordloadPage);
		Page<DeviceResponsePayload> deviceWithPagination = service.getDeviceWithPagination("",accountNumber, imei, uuid,
				status, dType, setMacAddress, filterValues, filterModelCountFilter, pageable, userName, false, token, "",null);
		assertNotNull(deviceWithPagination);
	}

	@Test
	@Order(7)
	public void test_getParsedReport() {

		String rawReport = "7d010015115004700885112b2a373d101a500117bc2a373d1007146a5e04de7cdf396c694653d000026246e2100901374d00110000105af00104007a007ae6100100e6210104e614028000e6200104e6270104e62a0104e6120400000000e629010c";
		String format = "format";
		String type = "tlv";

		String parsedReport = "parsedReport";
		Mockito.when(restUtils.getParsedReport(rawReport, format, type)).thenReturn(parsedReport);
		String parsedReport2 = service.getParsedReport(rawReport, format, type);
		assertNotNull(parsedReport2);
	}

	@Test
	@Order(8)
	public void test_getCompanyByType() {

		String type = OrganisationRole.END_CUSTOMER.toString();
		String[] companies = { "Customer" };
		Mockito.when(restUtils.findByType(type)).thenReturn(companies);
		String[] companyByType = service.getCompanyByType(type);
		assertNotNull(companyByType);
	}

	@Test
	@Order(9)
	public void test_getDeviceReportByUuid() throws IOException, URISyntaxException {

		int from = 0;
		int size = 10;

		Filter filter = new Filter();
		filter.setKey("report_header.device_id");
		filter.setValue("015115004700885");
		Map<String, Filter> filterValues = new HashedMap();
		filterValues.put("first", filter);
		String imei = "354892034904997";
		String url = "https://search-ms2-qa-pv6bfhiebk55ximohkcozyeb3a.us-east-1.es.amazonaws.com:443";
		// URI uri=new URI(url);
		// when(URI.create(url)).thenReturn(uri);

		MatchQueryBuilder matchQueryBuilder = QueryBuilders.matchQuery(filter.getKey(), filter.getValue());
		String operator = "eq";
		String clientConfigUrl = "search-ms2-qa-pv6bfhiebk55ximohkcozyeb3a.us-east-1.es.amazonaws.com:443";
		final ClientConfiguration.MaybeSecureClientConfigurationBuilder builder = ClientConfiguration.builder()
				.connectedTo(clientConfigUrl);
		boolean isSsl = true;
//		Mockito.when("https".equals(uri.getScheme())).thenReturn(isSsl);
		String elasticSearchUsername = "admin";
		String elasticSearchPassword = "Qweasdzxc@123";
//		Mockito.when(builder.withBasicAuth(elasticSearchUsername, elasticSearchPassword )).thenReturn(builder);
		RestHighLevelClient rs = RestClients.create(builder.build()).rest();
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
		SearchRequest searchRequest = new SearchRequest();
		searchSourceBuilder.from(from);
		searchSourceBuilder.size(size);
		searchSourceBuilder.query(boolQueryBuilder);
		searchSourceBuilder.sort(new FieldSortBuilder("general_mask_fields_received_time_stamp").order(SortOrder.DESC));
		searchRequest.source(searchSourceBuilder);
//		SearchResponse searchResponse = rs.search(searchRequest, RequestOptions.DEFAULT);

		SearchResponse deviceReportByUUid = service.getDeviceReportByUUid(from, size, filterValues, imei);
		assertNotNull(deviceReportByUUid);

	}

	@Test
	@Order(10)
	public void test_getDevice() {

		String imei = "354892034904997";

		Device device = new Device();
		device.setProductCode("5435454");
		device.setProductName("product_name");
		device.setCreatedAt(Instant.now());
		device.setUpdatedAt(Instant.now());
		device.setMacAddress("mac_address");
		device.setEpicorOrderNumber("354565");
		device.setImei(imei);
		device.setUuid("84d6efc0-37ad-4cde-98c9-79611e2fab60");

		Mockito.when(deviceRepository.findByImei(imei)).thenReturn(device);
		Device device2 = service.getDevice(imei);
		assertNotNull(device2);
	}

	@Test
	@Order(11)
	public void test_getColumnDefs1() {
		ColumnDefs columnDefs = new ColumnDefs();
		columnDefs.setId(2L);
		columnDefs.setReportColumnDefs("reportColumnDefs");
		Mockito.when(columnDefsRepo.findById(2L)).thenReturn(Optional.of(columnDefs));
		List<ColumnDefs> columnDefs2 = service.getColumnDefs();
		assertNotNull(columnDefs2);
	}

	@Test
	@Order(12)
	public void test_getColumnDefs2() {
		ColumnDefs columnDefs = new ColumnDefs();
		columnDefs.setId(2L);
		columnDefs.setReportColumnDefs("reportColumnDefs");
		Mockito.when(columnDefsRepo.findById(1L)).thenReturn(Optional.of(columnDefs));
		List<ColumnDefs> columnDefs2 = service.getColumnDefs();
		assertNotNull(columnDefs2);
	}

	@Test
	@Order(13)
	public void test_getListOfCompanyByType() {
		Organisation organisation = getUser().getOrganisation();
		List<Organisation> companies = Arrays.asList(organisation);
		if(!organisation.getOrganisationRole().isEmpty()) {
			List<OrganisationRole> list = new ArrayList<OrganisationRole>(organisation.getOrganisationRole());
			Mockito.when(restUtils.findOrganisationByType(list.get(0).getValue(), "")).thenReturn(companies);
			List<Organisation> al = service.getListOfCompanyByType("Customer", "");
			assertNotNull(al);
		}
		
	}
	
}