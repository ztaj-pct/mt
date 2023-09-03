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
import java.util.UUID;

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

import com.pct.common.constant.DeviceStatus;
import com.pct.common.constant.DeviceType;
import com.pct.common.constant.IOTType;
import com.pct.common.constant.OrganisationRole;
import com.pct.common.model.Device;
import com.pct.common.model.Organisation;
import com.pct.common.model.PermissionEntity;
import com.pct.common.model.Role;
import com.pct.common.model.User;
import com.pct.device.payload.BeaconDetailPayLoad;
import com.pct.device.payload.BeaconPayload;
import com.pct.device.payload.BeaconRequestPayload;
import com.pct.device.repository.IDeviceRepository;
import com.pct.device.specification.BeaconSpecification;
import com.pct.device.util.BeanConverter;
import com.pct.device.util.RestUtils;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
public class BeaconServiceImplTest {

	Logger logger = LoggerFactory.getLogger(BeaconServiceImplTest.class);

	@Mock
	private IDeviceRepository deviceRepository;

	@Mock
	private BeanConverter beanConverter;

	@Mock
	private RestUtils restUtils;

	@InjectMocks
	private BeaconServiceImpl service;

	@Mock
	Specification<Device> spc;

	@Test
	public void addBeaconDetail() {
		BeaconRequestPayload beaconRequestRequest = new BeaconRequestPayload();
		beaconRequestRequest.setProductCode("53453553");
		beaconRequestRequest.setProductName("product_name");
		beaconRequestRequest.setCan("can");
		beaconRequestRequest.setConfig1("config1");
		beaconRequestRequest.setMacAddress("macaddress");
		beaconRequestRequest.setAppVersion("app_version");
		beaconRequestRequest.setBinVersion("bin_version");
		beaconRequestRequest.setBleVersion("ble_version");
		beaconRequestRequest.setMcuVersion("mcu_version");
		beaconRequestRequest.setSon("son");
		beaconRequestRequest.setEpicorOrderNumber("254534");

		Long userId = 45L;
		Long roleId = 55L;
		String userName = "userName";

		logger.info("Inside addBeaconDetail and fetching beaconDetail and userId value",
				beaconRequestRequest + " " + userId);
		String can = beaconRequestRequest.getCan();

		OrganisationRole type = OrganisationRole.END_CUSTOMER;

		Organisation organisation = new Organisation();
		organisation.setUuid("84d6efc0-37ad-4cde-98c9-79611e2fab60");
		organisation.setAccountNumber("52454885");
		organisation.setShortName("short_name");
		organisation.setIsActive(true);
		organisation.setId(userId);
		organisation.setIsAssetListRequired(true);

		Mockito.when(restUtils.getCompanyFromCompanyService(can)).thenReturn(organisation);

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

		List<PermissionEntity> permission = new ArrayList<>();
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
		user.setIsDeleted(true);
		user.setIsPasswordChange(true);
		user.setLastName("lastName");
		user.setOrganisation(organisation);
		user.setPassword("Password");
		user.setPhone("Phone");
		user.setRole(roleList);
		user.setTimeZone("timeZone");
		user.setId(userId);

		when(restUtils.getUserFromAuthService(userName)).thenReturn(user);

		String macAddress = beaconRequestRequest.getMacAddress();

		Device deviceMacAddress = null;

		Mockito.when(deviceRepository.findByMac_address(beaconRequestRequest.getMacAddress()))
				.thenReturn(deviceMacAddress);

		boolean isDeviceUuidUnique = false;
		String deviceUuid = "";
		while (!isDeviceUuidUnique) {
			deviceUuid = UUID.randomUUID().toString();
			Device byUuid = deviceRepository.findByUuid(deviceUuid);
			if (byUuid == null) {
				isDeviceUuidUnique = true;
			}
		}

		when(deviceRepository.save(any(Device.class))).thenReturn(deviceMacAddress);

		logger.info("Beacon Details saved successfully");

		Boolean addBeaconDetail = service.addBeaconDetail(beaconRequestRequest, userName);
		assertNotNull(addBeaconDetail);

		verify(restUtils, atLeast(1)).getCompanyFromCompanyService(can);
		verify(restUtils, atLeast(1)).getUserFromAuthService(userName);
	}

	@Test
	public void getBeaconWithPagination() {
		String accountNumber = "54545224748547";
		String uuid = "30fe2e67-e8e4-49de-980b-b7aefcfb551a";
		DeviceStatus status = DeviceStatus.ACTIVE;
		String mac = "mac";
		Map<String, String> filterValues = new HashMap<String, String>();
		filterValues.put("fdgfs", "tyrst");

		List<Device> deviceList = new ArrayList<Device>();
		Device device = new Device();

		Organisation organisation = new Organisation();
		organisation.setUuid("84d6efc0-37ad-4cde-98c9-79611e2fab60");
		organisation.setAccountNumber("52454885");
		organisation.setShortName("short_name");
		organisation.setIsActive(true);

		device.setOrganisation(organisation);
		device.setProductCode("5435454");
		device.setProductName("product_name");
		device.setCreatedAt(Instant.now());
		device.setUpdatedAt(Instant.now());
		device.setMacAddress("mac_address");

		deviceList.add(device);

		PageRequest pageable = PageRequest.of(0, 20);
		Page<Device> deviceDetails = new PageImpl<Device>(deviceList);

		Specification<Device> spc = BeaconSpecification.getBeaconListSpecification(accountNumber, uuid, status, mac,
				filterValues, IOTType.BEACON);
		logger.info("After Specification " + spc);
		when(deviceRepository.findAll(Mockito.any(Specification.class), Mockito.any(Pageable.class)))
				.thenReturn(deviceDetails);
		Page<BeaconPayload> beaconWithPagination = service.getBeaconWithPagination(accountNumber, uuid, status, mac,
				filterValues, pageable);

	}

	@Test
	public void deleteBeaconDetail() {
		String can = "can";
		String uuid = "84d6efc0-37ad-4cde-98c9-79611e2fab60";

		logger.info("Inside deleteBeaconDetail");

		Organisation organisation = new Organisation();
		organisation.setUuid("84d6efc0-37ad-4cde-98c9-79611e2fab60");
		organisation.setAccountNumber("52454885");
		organisation.setShortName("short_name");
		organisation.setIsActive(true);

		when(restUtils.getCompanyFromCompanyService(can)).thenReturn(organisation);

		DeviceType type = DeviceType.getValue("Beacon");
		DeviceStatus dStatus = DeviceStatus.getGatewayStatusInSearch("PENDING");

//		Specification<Device> spc = SensorSpecification.getSensorSpec(can, uuid,type);

		Device device = new Device();

		device.setOrganisation(organisation);
		device.setProductCode("5435454");
		device.setProductName("product_name");
		device.setCreatedAt(Instant.now());
		device.setUpdatedAt(Instant.now());
		device.setMacAddress("mac_address");
		device.setStatus(dStatus);

		List<Device> deviceList = new ArrayList<Device>();
		deviceList.add(device);

		when(deviceRepository.findAll(Mockito.any(Specification.class))).thenReturn(deviceList);
		System.out.println("++++++//////" + deviceList);
		logger.info("After fetching beacon details" + deviceList.toString());

		boolean deleteBeaconDetail = service.deleteBeaconDetail(can, uuid);
		assertTrue(deleteBeaconDetail);
	}

	@Test
	public void updateBeaconDetail() {
		BeaconDetailPayLoad beaconDetailPayload = new BeaconDetailPayLoad();
		beaconDetailPayload.setUuid("84d6efc0-37ad-4cde-98c9-79611e2fab60");
		beaconDetailPayload.setProductName("product_name");
		beaconDetailPayload.setProductCode("53455");
		beaconDetailPayload.setConfig1("config1");
		beaconDetailPayload.setCan("can");
		beaconDetailPayload.setMacAddress("mac_address");

		Long userId = 5345544L;
		String userName = "user";

		OrganisationRole type = OrganisationRole.END_CUSTOMER;

		Organisation organisation = new Organisation();
		organisation.setUuid("84d6efc0-37ad-4cde-98c9-79611e2fab60");
		organisation.setAccountNumber("52454885");
		organisation.setShortName("short_name");
		organisation.setIsActive(true);
		organisation.setId(userId);
		organisation.setIsAssetListRequired(true);

		Role role = new Role();
		role.setDescription("description");
//		role.setDisplayName("displayName");
//		role.setId(userId);
//		role.setRoleName("roleName");
//		role.setUuid("84d6efc0-37ad-4cde-98c9-79611e2fab60");

		List<Role> role1 = new ArrayList<>();
		role1.add(role);

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
//		user.setRole(role1);
		user.setTimeZone("timeZone");
		user.setId(userId);

		// User user = restUtils.getUserFromAuthService(userId);
		when(restUtils.getUserFromAuthService(userName)).thenReturn(user);

		Device device = new Device();

		device.setProductCode("5435454");
		device.setProductName("product_name");
		device.setCreatedAt(Instant.now());
		device.setUpdatedAt(Instant.now());
		device.setMacAddress("mac_address");

		when(deviceRepository.findByUuid(beaconDetailPayload.getUuid())).thenReturn(device);

		logger.info("Fetching beacon details for uuid : " + beaconDetailPayload.getUuid());

		when(deviceRepository.save(any(Device.class))).thenReturn(device);

		Boolean updateBeaconDetail = service.updateBeaconDetail(beaconDetailPayload, userName);
		assertNotNull(updateBeaconDetail);

		verify(deviceRepository, atLeast(1)).save(device);
	}

	@Test
	public void getBeaconDetails() {
		String can = "can";
		String uuid = "84d6efc0-37ad-4cde-98c9-79611e2fab60";

		logger.info("Inside getBeaconDetails for account number " + can + " uuid " + uuid);
		List<BeaconPayload> beaconDetailList = new ArrayList<>();
		List<Device> deviceList = new ArrayList<Device>();
		Device device = new Device();

		Organisation organisation = new Organisation();
		organisation.setUuid("84d6efc0-37ad-4cde-98c9-79611e2fab60");
		organisation.setAccountNumber("52454885");
		organisation.setShortName("short_name");
		organisation.setIsActive(true);

		device.setOrganisation(organisation);
		device.setProductCode("5435454");
		device.setProductName("product_name");
		device.setCreatedAt(Instant.now());
		device.setUpdatedAt(Instant.now());
		device.setMacAddress("mac_address");

		deviceList.add(device);
		DeviceType type = DeviceType.getValue("Beacon");

//		spc = SensorSpecification.getSensorSpec(can, uuid, type);

		BeaconPayload devicepayload = new BeaconPayload();
		devicepayload.setProductCode(device.getProductCode());
		devicepayload.setProductName(device.getProductName());
		devicepayload.setMacAddress(device.getMacAddress());

		when(deviceRepository.findAll(Mockito.any(Specification.class))).thenReturn(deviceList);
		when(beanConverter.convertBeaconToBeaconPayLoad(device)).thenReturn(devicepayload);

		deviceList.forEach(device1 -> {
			beaconDetailList.add(devicepayload);
		});

		List<BeaconPayload> beaconDetails = service.getBeaconDetails(can, uuid);
		assertNotNull(beaconDetails);

		verify(deviceRepository, atLeast(1)).findAll(Mockito.any(Specification.class));
	}
}