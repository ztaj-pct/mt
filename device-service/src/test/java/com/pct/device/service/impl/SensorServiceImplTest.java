package com.pct.device.service.impl;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import com.pct.common.constant.DeviceStatus;
import com.pct.common.constant.DeviceType;
import com.pct.common.constant.IOTType;
import com.pct.common.model.Cellular;
import com.pct.common.model.Device;
import com.pct.common.model.Organisation;
import com.pct.common.model.PermissionEntity;
import com.pct.common.model.Role;
import com.pct.common.model.SensorDetail;
import com.pct.common.model.User;
import com.pct.common.util.JwtUtil;
import com.pct.device.payload.SensorDetailPayLoad;
import com.pct.device.payload.SensorPayLoad;
import com.pct.device.payload.SensorRequestPayload;
import com.pct.device.payload.SensorSubDetailPayload;
import com.pct.device.payload.SensorSubDetailRequestPayload;
import com.pct.device.repository.IDeviceRepository;
import com.pct.device.repository.SensorDetailRepository;
import com.pct.device.specification.SensorSpecification;
import com.pct.device.util.BeanConverter;
import com.pct.device.util.RestUtils;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
public class SensorServiceImplTest {
	Logger logger = LoggerFactory.getLogger(SensorServiceImplTest.class);
	private static final DeviceStatus INSTALLED = null;

	@Mock
	Specification<Device> spc;

	@Mock
	SensorDetailRepository sensorDetailRepository;

	@InjectMocks
	SensorServiceImpl sensorServiceImpl;

	@Mock
	private Pageable pageableMock;

	@Mock
	private IDeviceRepository deviceRepository;

	@Mock
	private BeanConverter beanConverter;

	@Mock
	private RestUtils restUtils;

	@Test
	public void addSensorDetail() {

		Long userId = 46L;
		String can = "can";
		String Son = "son";
		String QuantityShipped = "2";
		String BinVersion = "4.1";
		String setBleVersion = "5.1";
		String setMacAddress = "01-23-45-67-89-AB";
		String setConfig1 = "config1";
		String setEpicorOrderNumber = "EpicorOrderNumber";
		String setMcuVersion = "McuVersion";
		String setProductCode = "productcode";
		String setProductName = "productname";
		String userName = "userName";
		Long roleId = 34L;
		new JwtUtil();
		SensorSubDetailRequestPayload SensorSubDetailRequestPayload = new SensorSubDetailRequestPayload();
		SensorSubDetailRequestPayload.setPosition("position");
		SensorSubDetailRequestPayload.setSensorId("123");
		SensorSubDetailRequestPayload.setType("type");
		SensorSubDetailRequestPayload.setUuid("536a3ca1-3c7c-45b0-844a-e012135c46d3");

		List<SensorSubDetailRequestPayload> list = new ArrayList<SensorSubDetailRequestPayload>();
		list.add(SensorSubDetailRequestPayload);

		SensorRequestPayload sensorRequestRequest = new SensorRequestPayload();
		sensorRequestRequest.setSon(Son);
		sensorRequestRequest.setBleVersion(setBleVersion);
		sensorRequestRequest.setCan(can);
		sensorRequestRequest.setMacAddress(setMacAddress);
		sensorRequestRequest.setEpicorOrderNumber(setEpicorOrderNumber);
		sensorRequestRequest.setProductCode(setProductCode);
		sensorRequestRequest.setProductName(setProductName);
		sensorRequestRequest.setSensorSubDetails(list);

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
		role.setRoleId(roleId);
		role.setUpdatedAt(Instant.now());
		role.setUpdatedBy("updatedBy");
		role.setPermissions(permission);
		roleList.add(role);

		Organisation company = new Organisation();
		company.setUuid("536a3ca1-3c7c-45b0-844a-e012135c46d3");
		company.setAccountNumber("accountbean");
		company.setOrganisationName("organisationName");
		company.setIsActive(true);

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
		user.setOrganisation(company);
		user.setPassword("Password");
		user.setPhone("Phone");
		user.setRole(roleList);
		user.setTimeZone("timeZone");
		user.setId(userId);

		when(restUtils.getUserFromAuthService(userName)).thenReturn(user);

		when(restUtils.getCompanyFromCompanyService(can)).thenReturn(company);
//		System.out.println("/////"+company);

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

		Device sensorUUID = new Device();
		sensorUUID.setUuid("536a3ca1-3c7c-45b0-844a-e012135c46d3");

		SensorDetail sensorDetail = new SensorDetail();
		sensorDetail.setCreatedOn(Instant.now());
		sensorDetail.setId(userId);
		sensorDetail.setPosition("Active");
		sensorDetail.setSensorId("23213");
		sensorDetail.setSensorUUID(sensorUUID);
		sensorDetail.setType("type");
		sensorDetail.setUpdatedOn(Instant.now());
		sensorDetail.setUuid("536a3ca1-3c7c-45b0-844a-e012135c46d3");

		List<SensorDetail> sensorDetailList = new ArrayList<>();
		sensorDetailList.add(sensorDetail);

		IOTType deviceType = IOTType.BEACON;

		Device device = new Device();
		device.setEpicorOrderNumber(setEpicorOrderNumber);
		device.setMacAddress(setMacAddress);
		device.setSon(Son);
		device.setCellular(cell);
		
		device.setCreatedBy(user);
		device.setCreatedAt(Instant.now());
		device.setId(roleId);
		device.setImei("imei");
		device.setOrganisation(company);
		device.setProductCode(setProductCode);
		device.setProductName(setProductName);
		device.setQaStatus("qaStatus");
		device.setQuantityShipped(QuantityShipped);
		device.setSensorDetail(sensorDetailList);
		device.setSon(Son);
		device.setStatus(INSTALLED);
		device.setIotType(deviceType);
		device.setUpdatedBy(user);
		device.setUpdatedAt(Instant.now());
		device.setUuid("536a3ca1-3c7c-45b0-844a-e012135c46d3");
		;

		when(deviceRepository.save(any(Device.class))).thenReturn(device);

		SensorDetail byUuid = new SensorDetail();
		byUuid.setCreatedOn(Instant.now());
		byUuid.setId(userId);
		byUuid.setPosition("123");
		byUuid.setSensorId("12232");
		byUuid.setType("type");
		byUuid.setSensorUUID(device);
		byUuid.setUpdatedOn(Instant.now());
		byUuid.setUuid("536a3ca1-3c7c-45b0-844a-e012135c46d3");

		Boolean addSensorDetail = sensorServiceImpl.addSensorDetail(sensorRequestRequest, userName);
		assertNotNull(addSensorDetail);
		verify(deviceRepository, atLeast(1)).findByMac_address(setMacAddress);
	}

	@Test
	public void getSensorWithPagination() {
		Pageable pageable = PageRequest.of(0, 8);

		String can = "can";
		String Son = "goku";
		String BinVersion = "4.1";
		String setBleVersion = "5.1";
		String setMacAddress = "01-23-45-67-89-AB";
		String setConfig1 = "config1";
		String setEpicorOrderNumber = "EpicorOrderNumber";
		String setMcuVersion = "McuVersion";
		String setProductCode = "productcode";
		String setProductName = "productname";
		String accountNumber = "accountNumber";
		String uuid = "536a3ca1-3c7c-45b0-844a-e012135c46d3";
		DeviceStatus status = INSTALLED;
		String mac = "mac";

		Map<String, String> filterValues = new HashMap<String, String>();
		filterValues.put(uuid, mac);
		IOTType type = IOTType.SENSOR;

		Device device = new Device();
		device.setEpicorOrderNumber(setEpicorOrderNumber);
		device.setMacAddress(setMacAddress);
		device.setSon(Son);

		spc = SensorSpecification.getSensorListSpecification(accountNumber, uuid, status, mac, filterValues, type);
		logger.info("After Specification " + spc);

		List<Device> list = new ArrayList<Device>();
		list.add(device);

		Page<Device> deviceDetails = new PageImpl<>(list);

		when(deviceRepository.findAll(Mockito.any(Specification.class), Mockito.any(Pageable.class)))
				.thenReturn(deviceDetails);
		logger.info("After DB Call " + deviceDetails);

		SensorSubDetailPayload SensorSubDetailPayload = new SensorSubDetailPayload();
		SensorSubDetailPayload.setPosition("position");
		SensorSubDetailPayload.setSensorId("2321");
		SensorSubDetailPayload.setType("type");
		SensorSubDetailPayload.setSensorUUID("0cbe5c6d-2e8c-4623-afa4-6c7bf0b624a5");
		SensorSubDetailPayload.setUuid(uuid);

		List<SensorSubDetailPayload> sensorSubDetail = new ArrayList<>();
		sensorSubDetail.add(SensorSubDetailPayload);

		SensorPayLoad sensorPayLoad = new SensorPayLoad();
		sensorPayLoad.setBleVersion(setBleVersion);
		sensorPayLoad.setMacAddress(setMacAddress);
		sensorPayLoad.setProductCode(setProductCode);
		sensorPayLoad.setProductName(setProductName);
		sensorPayLoad.setStatus(status);
		sensorPayLoad.setType(type);
		sensorPayLoad.setUuid(uuid);
		sensorPayLoad.setCreatedBy("createdBy");
		sensorPayLoad.setMacAddress(setMacAddress);
		sensorPayLoad.setSensorSubDetail(sensorSubDetail);

		List<SensorPayLoad> list1 = new ArrayList<SensorPayLoad>();
		list1.add(sensorPayLoad);

		Page<SensorPayLoad> sensorRecordloadPage = new PageImpl<>(list1);

		when(beanConverter.convertSensorToSensorPayLoad(deviceDetails, pageable)).thenReturn(sensorRecordloadPage);

		Page<SensorPayLoad> sensorWithPagination = sensorServiceImpl.getSensorWithPagination(accountNumber, uuid,
				status, mac, filterValues, pageable);
		assertNotNull(sensorWithPagination);
	}

	@Test
	public void deleteSensorDetail() {
		String uuid = "536a3ca1-3c7c-45b0-844a-e012135c46d3";
		String can = "can";
		String Son = "goku";
		String BinVersion = "4.1";
		String setBleVersion = "5.1";
		String setMacAddress = "01-23-45-67-89-AB";
		String setConfig1 = "config1";
		String setEpicorOrderNumber = "EpicorOrderNumber";
		String setMcuVersion = "McuVersion";

		Organisation company = new Organisation();
		company.setUuid("536a3ca1-3c7c-45b0-844a-e012135c46d3");
		company.setAccountNumber("accountbean");
		company.setOrganisationName("organisationName");
		company.setIsActive(true);

		when(restUtils.getCompanyFromCompanyService(can)).thenReturn(company);

		DeviceType type = DeviceType.SENSOR;
		System.out.println("////" + type);
		// when(DeviceType.getValue("SENSOR")).thenReturn(type);
		DeviceStatus dStatus = DeviceStatus.PENDING;

		Device device = new Device();
		device.setEpicorOrderNumber(setEpicorOrderNumber);
		device.setMacAddress(setMacAddress);
		device.setSon(Son);
		device.setStatus(dStatus);

		List<Device> deviceList = new ArrayList<Device>();
		deviceList.add(device);

		when(deviceRepository.findAll(Mockito.any(Specification.class))).thenReturn(deviceList);

		// AtomicBoolean status = new AtomicBoolean(true);

		boolean deleteSensorDetail = sensorServiceImpl.deleteSensorDetail(can, uuid);
		// sensorServiceImpl.deleteSensorDetail(can, uuid);
		assertTrue(deleteSensorDetail);
	}

	@Test
	public void updateSensorDetail() {

		String can = "can";
		String Son = "son";
		String BinVersion = "4.1";
		String setBleVersion = "5.1";
		String setMacAddress = "01-23-45-67-89-AB";
		String setConfig1 = "config1";
		String setEpicorOrderNumber = "EpicorOrderNumber";
		String setMcuVersion = "McuVersion";
		String setProductCode = "productcode";
		String setProductName = "productname";
		String uuid = "536a3ca1-3c7c-45b0-844a-e012135c46d3";
		Long userId = 56L;
		Long roleId = 45L;
		String userName = "userName";

		SensorDetailPayLoad sensorDetailPayload = new SensorDetailPayLoad();
		sensorDetailPayload.setBleVersion(setBleVersion);
		sensorDetailPayload.setCan(can);
		sensorDetailPayload.setEpicorOrderNumber(setEpicorOrderNumber);
		sensorDetailPayload.setMacAddress(setMacAddress);
		sensorDetailPayload.setProductCode(setProductCode);
		sensorDetailPayload.setProductName(setProductName);
		sensorDetailPayload.setSon(Son);
		sensorDetailPayload.setUuid(uuid);

		Organisation organisation = new Organisation();
		organisation.setUuid("84d6efc0-37ad-4cde-98c9-79611e2fab60");
		organisation.setAccountNumber("52454885");
		organisation.setShortName("short_name");
		organisation.setIsActive(true);
		organisation.setId(userId);
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
		role.setRoleId(roleId);
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
		user.setId(userId);
		user.setIsDeleted(true);
		user.setIsPasswordChange(true);
		user.setLastName("lastName");
		user.setOrganisation(organisation);
		user.setPassword("Password");
		user.setPhone("Phone");
		user.setRole(roleList);
		user.setTimeZone("timeZone");

		List<Role> role1 = new ArrayList<>();
		role1.add(role);

		when(restUtils.getUserFromAuthService(userName)).thenReturn(user);

		Device device = new Device();
		device.setEpicorOrderNumber(setEpicorOrderNumber);
		device.setMacAddress(setMacAddress);
		device.setSon(Son);

		when(deviceRepository.findByUuid(sensorDetailPayload.getUuid())).thenReturn(device);

		when(deviceRepository.save(any(Device.class))).thenReturn(device);
		Boolean updateSensorDetail = sensorServiceImpl.updateSensorDetail(sensorDetailPayload, userName);
		assertTrue(updateSensorDetail);
	}

	@Test
	public void updateSensorDetail1() {

		String can = "can";
		String Son = "son";
		String setBleVersion = "5.1";
		String setMacAddress = "01-23-45-67-89-AB";
		String setEpicorOrderNumber = "EpicorOrderNumber";
		String setProductCode = "productcode";
		String setProductName = "productname";
		String uuid = "536a3ca1-3c7c-45b0-844a-e012135c46d3";
		Long userId = 56L;
		Long roleId = 45L;
		String userName = "userName";

		SensorDetailPayLoad sensorDetailPayload = new SensorDetailPayLoad();
		sensorDetailPayload.setBleVersion(setBleVersion);
		sensorDetailPayload.setCan(can);
		sensorDetailPayload.setEpicorOrderNumber(setEpicorOrderNumber);
		sensorDetailPayload.setMacAddress(setMacAddress);
		sensorDetailPayload.setProductCode(setProductCode);
		sensorDetailPayload.setProductName(setProductName);
		sensorDetailPayload.setSon(Son);
		sensorDetailPayload.setUuid(uuid);

		Organisation organisation = new Organisation();
		organisation.setUuid("84d6efc0-37ad-4cde-98c9-79611e2fab60");
		organisation.setAccountNumber("52454885");
		organisation.setShortName("short_name");
		organisation.setIsActive(true);
		organisation.setId(userId);
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
		role.setRoleId(roleId);
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
		user.setId(userId);
		user.setIsDeleted(true);
		user.setIsPasswordChange(true);
		user.setLastName("lastName");
		user.setOrganisation(organisation);
		user.setPassword("Password");
		user.setPhone("Phone");
		user.setRole(roleList);
		user.setTimeZone("timeZone");

		List<Role> role1 = new ArrayList<>();
		role1.add(role);

		when(restUtils.getUserFromAuthService(userName)).thenReturn(user);

		Device device = null;

		when(deviceRepository.findByUuid(sensorDetailPayload.getUuid())).thenReturn(device);

		when(deviceRepository.save(any(Device.class))).thenReturn(device);
		Boolean updateSensorDetail = sensorServiceImpl.updateSensorDetail(sensorDetailPayload, userName);
		assertTrue(updateSensorDetail);
	}

	@Test
	public void getSensorDetails() {
		String accountNumber = "accountNumber";
		String uuid = "536a3ca1-3c7c-45b0-844a-e012135c46d3";
		String can = "can";
		String Son = "goku";
		String BinVersion = "4.1";
		String setBleVersion = "5.1";
		String setMacAddress = "01-23-45-67-89-AB";
		String setConfig1 = "config1";
		String setEpicorOrderNumber = "EpicorOrderNumber";
		String setMcuVersion = "McuVersion";
		String setProductCode = "productcode";
		String setProductName = "productname";
		SensorPayLoad sensorPayLoad = new SensorPayLoad();
		sensorPayLoad.setBleVersion(setBleVersion);
		sensorPayLoad.setMacAddress(setMacAddress);
		sensorPayLoad.setProductCode(setProductCode);
		sensorPayLoad.setProductName(setProductName);
		sensorPayLoad.setStatus(INSTALLED);
		sensorPayLoad.setUuid(uuid);

		List<SensorPayLoad> sensorDetailList = new ArrayList<>();
		sensorDetailList.add(sensorPayLoad);

		Device device = new Device();
		device.setEpicorOrderNumber(setEpicorOrderNumber);
		device.setMacAddress(setMacAddress);
		device.setSon(Son);

		List<Device> deviceList = new ArrayList<Device>();
		deviceList.add(device);

		DeviceType type = DeviceType.getValue("Sensor");
		System.out.println("////" + type);

		when(deviceRepository.findAll(Mockito.any(Specification.class))).thenReturn(deviceList);
		List<SensorPayLoad> sensorDetails = sensorServiceImpl.getSensorDetails(accountNumber, uuid);
		assertNotNull(sensorDetails);
	}

	@Test
	public void getSensorDetails1() {
		String accountNumber = "accountNumber";
		String uuid = "536a3ca1-3c7c-45b0-844a-e012135c46d3";
		String can = "can";
		String Son = "goku";
		String setBleVersion = "5.1";
		String setMacAddress = "01-23-45-67-89-AB";
		String setEpicorOrderNumber = "EpicorOrderNumber";
		String setProductCode = "productcode";
		String setProductName = "productname";
		SensorPayLoad sensorPayLoad = new SensorPayLoad();
		sensorPayLoad.setBleVersion(setBleVersion);
		sensorPayLoad.setMacAddress(setMacAddress);
		sensorPayLoad.setProductCode(setProductCode);
		sensorPayLoad.setProductName(setProductName);
		sensorPayLoad.setStatus(INSTALLED);
		sensorPayLoad.setUuid(uuid);

		List<SensorPayLoad> sensorDetailList = new ArrayList<>();
		sensorDetailList.add(sensorPayLoad);

		Device device = new Device();

		List<Device> deviceList = new ArrayList<Device>();
		deviceList.add(device);

		DeviceType type = DeviceType.getValue("Sensor");
		System.out.println("////" + type);

//		when(deviceRepository.findAll(Mockito.any(Specification.class))).thenReturn(deviceList);
		List<SensorPayLoad> sensorDetails = sensorServiceImpl.getSensorDetails(accountNumber, uuid);
		assertNotNull(sensorDetails);
	}
}