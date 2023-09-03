package com.pct.device.service.impl;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.collections.map.HashedMap;
import org.junit.Assert;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import com.pct.common.constant.AssetCreationMethod;
import com.pct.common.constant.AssetStatus;
import com.pct.common.constant.DeviceStatus;
import com.pct.common.constant.IOTType;
import com.pct.common.constant.OrganisationRole;
import com.pct.common.model.Asset;
import com.pct.common.model.Asset_Device_xref;
import com.pct.common.model.Device;
//import com.pct.common.model.Device_Device_xref;
import com.pct.common.model.Device_Device_xref;
import com.pct.common.model.Manufacturer;
import com.pct.common.model.ManufacturerDetails;
import com.pct.common.model.Organisation;
import com.pct.common.model.PermissionEntity;
import com.pct.common.model.Role;
import com.pct.common.model.User;
import com.pct.common.util.JwtUtil;
import com.pct.device.payload.AssetsStatusPayload;
import com.pct.device.payload.GatewayDetailPayLoad;
import com.pct.device.payload.GatewayPayload;
import com.pct.device.payload.GatewaySensorAssociation;
import com.pct.device.payload.GatewaySensorPayload;
import com.pct.device.payload.GatewayUploadRequest;
import com.pct.device.repository.IAssetDeviceXrefRepository;
import com.pct.device.repository.IDeviceDeviceXrefRepository;
import com.pct.device.repository.IDeviceRepository;
import com.pct.device.specification.GatewaySpecification;
import com.pct.device.util.BeanConverter;
import com.pct.device.util.RestUtils;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
public class GatewayServiceImplTest {

	@InjectMocks
	private GatewayServiceImpl service;

	@Mock
	private IDeviceRepository deviceRepository;

	@Mock
	private BeanConverter beanConverter;

	@Mock
	private RestUtils restUtils;

	@Mock
	private Pageable pageableMock;

	@Mock
	private IAssetDeviceXrefRepository assetDeviceXrefRepository;

	@Mock
	private IDeviceDeviceXrefRepository deviceDeviceXrefRespository;
	Specification<Device> spc;

	private User getUser() {
		User user = new User();
		user.setId(1L);
		user.setCountryCode("+91");
		user.setCreatedAt(Instant.now());
		user.setCreatedBy("Created By");
		user.setEmail("akshit@gmail.com");
		user.setFirstName("Akshit");
		user.setLastName("Rai");
		user.setIsActive(true);
		user.setIsDeleted(true);
		user.setIsPasswordChange(true);
		user.setNotify("email");
		Organisation org = new Organisation();
		org.setAccountNumber("123456");
		org.setId(1L);
		user.setOrganisation(org);
		user.setPassword("akshit@123");
		user.setPhone("9999999999");
		Role r = new Role();
		r.setRoleId(1L);
		List<Role> roleList = new ArrayList<>();
		roleList.add(r);
		user.setRole(roleList);
		user.setTimeZone("2022-02-24T03:19:05.720Z");
		user.setUpdatedAt(Instant.now());
		user.setUpdatedBy("Updated BY");
		user.setUserName("akshit@gmail.com");
		user.setUuid("0d2f1041-b9f7-4869-8bf5-107781fb23da");
		return user;
	}

	private GatewayUploadRequest getGatewayUploadRequest() {
		GatewayUploadRequest gatewayUploadRequest = new GatewayUploadRequest();
		gatewayUploadRequest.setProductName("product_name");
		gatewayUploadRequest.setProductCode("product_code");
		gatewayUploadRequest.setQuantityShipped("quantity_shipped");
		gatewayUploadRequest.setConfig1("config1");
		gatewayUploadRequest.setMacAddress("mac_address");
		gatewayUploadRequest.setImei("354892034904997");
		gatewayUploadRequest.setCan("can");
		gatewayUploadRequest.setConfig2("config2");
		gatewayUploadRequest.setConfig3("config3");
		gatewayUploadRequest.setConfig4("config4");
		gatewayUploadRequest.setEpicorOrderNumber("epicorOrderNumber");
		gatewayUploadRequest.setAppVersion("4.1.4");
		gatewayUploadRequest.setBinVersion("5.1.2");
		gatewayUploadRequest.setBleVersion("5.1.4");
		gatewayUploadRequest.setMcuVersion("6.1.4");
		gatewayUploadRequest.setSon("son");
		return gatewayUploadRequest;
	}

	private PermissionEntity getPermissionEntity() {
		PermissionEntity permissionEntity = new PermissionEntity();
		permissionEntity.setCreatedAt(Instant.now());
		permissionEntity.setCreatedBy("");
		permissionEntity.setDescription("description");
		permissionEntity.setName("name");
		permissionEntity.setPath("path");
		permissionEntity.setPermissionId(3123);
		permissionEntity.setUpdatedAt(Instant.now());
		permissionEntity.setUpdatedBy("");
		return permissionEntity;
	}

	private Organisation getOrganisation() {
		Organisation organisation = new Organisation();
		organisation.setUuid("84d6efc0-37ad-4cde-98c9-79611e2fab60");
		organisation.setAccountNumber("52454885");
		organisation.setShortName("short_name");
		organisation.setIsActive(true);
		organisation.setIsAssetListRequired(true);
		return organisation;
	}

	private Device getDevice() {
		Device device = new Device();
		device.setProductCode("5435454");
		device.setProductName("product_name");
		device.setCreatedAt(Instant.now());
		device.setUpdatedAt(Instant.now());
		device.setMacAddress("mac_address");
		device.setEpicorOrderNumber("354565");
		device.setImei("354892034904997");
		device.setUuid("84d6efc0-37ad-4cde-98c9-79611e2fab60");
		return device;
	}

	@Test
	@Order(1)
	public void test_addGatewayDetail() {

		GatewayUploadRequest gatewayUploadRequest = getGatewayUploadRequest();

		Organisation organisation = getOrganisation();
		Mockito.when(restUtils.getCompanyFromCompanyService(gatewayUploadRequest.getCan())).thenReturn(organisation);

		User user = getUser();
		Mockito.when(restUtils.getUserFromAuthService(user.getUserName())).thenReturn(user);

		Device deviceMacAddress = null;
		Mockito.when(deviceRepository.findByMac_address(gatewayUploadRequest.getMacAddress()))
				.thenReturn(deviceMacAddress);

		Device byImei = null;
		when(deviceRepository.findByImei(gatewayUploadRequest.getImei())).thenReturn(byImei);

		Device device = new Device();
		device.setOrganisation(organisation);
		device.setProductCode(gatewayUploadRequest.getProductCode());
		device.setProductName(gatewayUploadRequest.getProductName());
		device.setQuantityShipped(gatewayUploadRequest.getQuantityShipped());
		device.setImei(gatewayUploadRequest.getImei());
		device.setMacAddress(gatewayUploadRequest.getMacAddress());
		device.setStatus(DeviceStatus.PENDING);
		device.setCreatedAt(Instant.now());
		boolean isDeviceUuidUnique = false;
		String deviceUuid = "";
		while (!isDeviceUuidUnique) {
			deviceUuid = UUID.randomUUID().toString();
			Device byUuid = deviceRepository.findByUuid(deviceUuid);
			if (byUuid == null) {
				isDeviceUuidUnique = true;
			}
		}
		JwtUtil jwtUtil = new JwtUtil();
		device.setCreatedBy(user);
		device.setUuid(deviceUuid);
		when(deviceRepository.save(any(Device.class))).thenReturn(device);
		Boolean addGatewayDetail = service.addGatewayDetail(gatewayUploadRequest, user.getUserName());
		assertNotNull(addGatewayDetail);
	}

	@Test
	@Order(2)
	public void getGatewayDetails() {
		String accountNumber = "5468548545";
		String uuid = "e930d55b-c4f4-4a96-918e-dcf0b979649f";
		String gatewayId = "123321";
		List<GatewayPayload> gatewayDetailList = new ArrayList<>();
		List<Device> deviceList = new ArrayList<Device>();
		Device device = getDevice();
		device.setUuid(uuid);
		deviceList.add(device);
		IOTType type = IOTType.getValue("Gateway");
		// Specification<Device> spc = SensorSpecification.getSensorSpec(accountNumber,
		// uuid, type, gatewayId);
		when(deviceRepository.findAll(Mockito.any(Specification.class))).thenReturn(deviceList);
		deviceList.forEach(device1 -> {
			gatewayDetailList.add(beanConverter.convertGatewayPayloadToGatewayBean(device));
		});

		List<GatewayPayload> gatewayDetails = service.getGatewayDetails(accountNumber, uuid, gatewayId);
		assertNotNull(gatewayDetails);
		Assert.assertEquals(gatewayDetailList.size(), gatewayDetails.size());
	}

	@Test
	@Order(3)
	public void test_associateGatewaySensor() {
		GatewaySensorAssociation gatewaysensorRequest = new GatewaySensorAssociation();
		gatewaysensorRequest.setDeviceUuid("deviceUUid");
		Long userId = 56L;
		String sensorUuid = "84d6efc0-37ad-4cde-98c9-79611e2fab60";
		String userName = "userName";
		Device gateway = getDevice();
		when(deviceRepository.findByUuid(gatewaysensorRequest.getDeviceUuid())).thenReturn(gateway);
		List<String> sensorList = new ArrayList<>();
		sensorList.add(sensorUuid);
		gatewaysensorRequest.setSensorUuid(sensorList);
		Device sDevice = getDevice();
		when(deviceRepository.findByUuid(sensorUuid)).thenReturn(sDevice);
		Device_Device_xref deviceXref = new Device_Device_xref();
		deviceXref.setActive(true);
		deviceXref.setDateCreated(Instant.now());
		deviceXref.setDateDeleted(Instant.now());
		deviceXref.setId(userId);
		deviceXref.setDeviceUuid(sDevice);
		Mockito.when(deviceDeviceXrefRespository.save(Mockito.any(Device_Device_xref.class))).thenReturn(deviceXref);
		Boolean result = service.associateGatewaySensor(gatewaysensorRequest, userName);
		assertTrue(result);
		Assert.assertEquals(true, result);
	}

	@Test
	@Order(4)
	public void test_deleteGatewayDetail() {
		String can = "can";
		String imei = "910519706385661";
		String uuid = "63d5f16c-ec54-4b4b-a057-0dceeb50d6a8 ";
		Device device = getDevice();
		Organisation organisation = getOrganisation();
		when(restUtils.getCompanyFromCompanyService(can)).thenReturn(organisation);
		IOTType type = IOTType.getValue("Gateway");
		Specification<Device> spc = GatewaySpecification.getGatewaySpec(can, imei, uuid, type);
		List<Device> deviceList = Arrays.asList(device);
		Mockito.when(deviceRepository.findAll(Mockito.any(Specification.class))).thenReturn(deviceList);

		service.deleteGatewayDetail(can, imei, uuid);
	}

	@Test
	@Order(5)
	public void test_updateGatewayDetail() {
		User user = getUser();
		GatewayDetailPayLoad gatewayDetailPayLoad = new GatewayDetailPayLoad();
		gatewayDetailPayLoad.setProductName("productname");
		gatewayDetailPayLoad.setProductCode("product_code");
		gatewayDetailPayLoad.setCan("can");
		gatewayDetailPayLoad.setConfig1("config1");
		gatewayDetailPayLoad.setQuantityShipped("quantity_shipped");
		gatewayDetailPayLoad.setAppVersion("app_version");
		gatewayDetailPayLoad.setBinVersion("bin_version");
		gatewayDetailPayLoad.setBleVersion("ble_version");
		gatewayDetailPayLoad.setConfig2("config2");
		gatewayDetailPayLoad.setConfig3("config3");
		gatewayDetailPayLoad.setConfig4("config4");
		gatewayDetailPayLoad.setEpicorOrderNumber("35454");
		gatewayDetailPayLoad.setImei("354892034904997");
		gatewayDetailPayLoad.setMacAddress("mac_address");
		gatewayDetailPayLoad.setMcuVersion("mcu_version");
		gatewayDetailPayLoad.setOther1Version("other1version");
		gatewayDetailPayLoad.setOther2Version("other2version");
		gatewayDetailPayLoad.setSon("son");
		gatewayDetailPayLoad.setUpdatedby("updated");
		gatewayDetailPayLoad.setUuid("84d6efc0-37ad-4cde-98c9-79611e2fab60");

		OrganisationRole type = OrganisationRole.END_CUSTOMER;

		Organisation organisation = new Organisation();
		organisation.setUuid("84d6efc0-37ad-4cde-98c9-79611e2fab60");
		organisation.setAccountNumber("52454885");
		organisation.setShortName("short_name");
		organisation.setIsActive(true);
		organisation.setId(user.getId());
		organisation.setIsAssetListRequired(true);

		PermissionEntity permissionEntity = getPermissionEntity();

		List<PermissionEntity> permission = new ArrayList<PermissionEntity>();
		permission.add(permissionEntity);
		Mockito.when(restUtils.getUserFromAuthService(user.getUserName())).thenReturn(user);

		Device device = getDevice();

		Mockito.when(deviceRepository.findByUuid(gatewayDetailPayLoad.getUuid())).thenReturn(device);

		device.setImei(gatewayDetailPayLoad.getImei());
		device.setProductCode(gatewayDetailPayLoad.getProductCode());
		device.setProductName(gatewayDetailPayLoad.getProductName());
		device.setSon(gatewayDetailPayLoad.getSon());
		device.setUpdatedAt(Instant.now());
				
		JwtUtil jwtUtil = new JwtUtil();
		device.setUpdatedBy(user);
		device.setQuantityShipped(gatewayDetailPayLoad.getQuantityShipped());
		device.setEpicorOrderNumber(gatewayDetailPayLoad.getEpicorOrderNumber());
		device.setMacAddress(gatewayDetailPayLoad.getMacAddress());
		when(deviceRepository.save(any(Device.class))).thenReturn(device);
		Boolean updateGatewayDetail = service.updateGatewayDetail(gatewayDetailPayLoad, user.getUserName());
		assertNotNull(updateGatewayDetail);
		verify(deviceRepository, atLeast(1)).save(device);
	}

	@Test
	@Order(6)
	public void test_getGatewayWithPagination() {
		String accountNumber = "accountNumber";
		String imei = "imei";
		String uuid = "e930d55b-c4f4-4a96-918e-dcf0b979649f";
		DeviceStatus status = DeviceStatus.ACTIVE;
		Map<String, String> filterValues = new HashedMap();
		filterValues.put("sdfdsf", "ddsv");
		Pageable pageable = PageRequest.of(0, 8);

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

		Page<Device> deviceDetails = new PageImpl<Device>(deviceList);
		IOTType type = IOTType.getValue("Gateway");
		spc = GatewaySpecification.getGatewayListSpecification(accountNumber, imei, uuid, status, filterValues, type, "", null);

		when(deviceRepository.findAll(Mockito.any(Specification.class), Mockito.any(Pageable.class)))
				.thenReturn(deviceDetails);

		GatewayPayload gatewayPayLoad = new GatewayPayload();
		gatewayPayLoad.setProductCode("product_code");
		gatewayPayLoad.setCan("can");
		gatewayPayLoad.setConfig1("config1");
		gatewayPayLoad.setQuantityShipped("quantity_shipped");
		gatewayPayLoad.setAppVersion("app_version");
		gatewayPayLoad.setBinVersion("bin_version");
		gatewayPayLoad.setBleVersion("ble_version");
		gatewayPayLoad.setConfig2("config2");
		gatewayPayLoad.setConfig3("config3");
		gatewayPayLoad.setConfig4("config4");
		gatewayPayLoad.setEpicorOrderNumber("35454");
		gatewayPayLoad.setImei("354892034904997");
		gatewayPayLoad.setMacAddress("mac_address");
		gatewayPayLoad.setMcuVersion("mcu_version");
		gatewayPayLoad.setOther1Version("other1version");
		gatewayPayLoad.setOther2Version("other2version");
		gatewayPayLoad.setSon("son");
		gatewayPayLoad.setUpdatedby("updated");
		gatewayPayLoad.setUuid("84d6efc0-37ad-4cde-98c9-79611e2fab60");

		List<GatewayPayload> list = new ArrayList<>();
		list.add(gatewayPayLoad);

		Page<GatewayPayload> gatewayRecordloadPage = new PageImpl<>(list);
		when(beanConverter.convertGatewayToGatewayPayLoad(deviceDetails, pageable)).thenReturn(gatewayRecordloadPage);
		Page<GatewayPayload> gatewayWithPagination = service.getGatewayWithPagination(accountNumber, imei, uuid, status,
				filterValues, uuid, pageable, uuid);
		assertNotNull(gatewayWithPagination);
	}

	@Test
	@Order(7)
	public void test_getGatewaySensorDetails() {
		String imei = "imei";
		String deviceUuid = "deviceUuid";
		Long id = 56L;

		GatewaySensorPayload gatewaySensorList = new GatewaySensorPayload();
		gatewaySensorList.setConfig1("config1");
		gatewaySensorList.setProductCode("5435454");
		gatewaySensorList.setProductName("product_name");
		gatewaySensorList.setMacAddress("mac_address");
		gatewaySensorList.setAppVersion("app_version");
		gatewaySensorList.setBinVersion("bin_version");
		gatewaySensorList.setBleVersion("ble_version");
		gatewaySensorList.setConfig2("config2");
		gatewaySensorList.setConfig3("config3");
		gatewaySensorList.setConfig4("config4");
		gatewaySensorList.setEpicorOrderNumber("354565");
		gatewaySensorList.setImei("354892034904997");
		gatewaySensorList.setUuid("84d6efc0-37ad-4cde-98c9-79611e2fab60");
		gatewaySensorList.setCan("can");
		gatewaySensorList.setCreatedBy("createdby");
		gatewaySensorList.setImei(imei);
		gatewaySensorList.setSon("son");

		List<GatewaySensorPayload> gatewayDetailList = new ArrayList<>();
		gatewayDetailList.add(gatewaySensorList);
		IOTType type = IOTType.GATEWAY;
		spc = GatewaySpecification.getGatewaySensorSpec(imei, deviceUuid, type);

		Device device = new Device();
		device.setProductCode("5435454");
		device.setProductName("product_name");
		device.setCreatedAt(Instant.now());
		device.setUpdatedAt(Instant.now());
		device.setMacAddress("mac_address");
		device.setEpicorOrderNumber("354565");
		device.setImei("354892034904997");
		device.setUuid("84d6efc0-37ad-4cde-98c9-79611e2fab60");

		List<Device> deviceDetails = new ArrayList<Device>();
		deviceDetails.add(device);
		when(deviceRepository.findAll(Mockito.any(Specification.class))).thenReturn(deviceDetails);

		when(deviceRepository.findByUuid(device.getUuid())).thenReturn(device);

		Device_Device_xref device_Device_xref = new Device_Device_xref();
		device_Device_xref.setActive(true);
		device_Device_xref.setDateCreated(Instant.now());
		device_Device_xref.setDateDeleted(Instant.now());
		device_Device_xref.setId(id);
		device_Device_xref.setDeviceUuid(device);
		List<Device_Device_xref> deviceSensorXrefsList = new ArrayList<Device_Device_xref>();
		deviceSensorXrefsList.add(device_Device_xref);
		when(deviceDeviceXrefRespository.findByDeviceUuid(device)).thenReturn(deviceSensorXrefsList);
		List<com.pct.device.payload.GatewaySensorPayload> gatewaySensorDetails = service.getGatewaySensorDetails(imei,
				deviceUuid);
		assertNotNull(gatewaySensorDetails);
	}

	@Test
	@Order(8)
	public void test_getAssetsStatusDetails() {
		String imei = "imei";
		String deviceUuid = "deviceUuid";
		Long id = 56L;
		IOTType type = IOTType.getValue("Gateway");
		Device device = getDevice();

		List<Device> deviceList = Arrays.asList(device);
		Specification<Device> spc = GatewaySpecification.getGatewaySensorSpec(imei, deviceUuid, type);
		Mockito.when(deviceRepository.findAll(Mockito.any(Specification.class))).thenReturn(deviceList);
		Mockito.when(deviceRepository.findByUuid(device.getUuid())).thenReturn(device);
		Asset_Device_xref xref = getAssetDeviceXref();
		xref.setDevice(device);
		xref.setAsset(getAsset());
		List<Device_Device_xref> deviceSensorXrefsList = Arrays.asList(getDeviceDeviceXref());
		Mockito.when(assetDeviceXrefRepository.findByDevice(device.getImei())).thenReturn(xref);
		Mockito.when(deviceDeviceXrefRespository.findByDeviceUuid(device)).thenReturn(deviceSensorXrefsList);
		User user = getUser();
		List<AssetsStatusPayload> list = Arrays
				.asList(beanConverter.convertSensorDetailToAssetsStatusBean(device, deviceSensorXrefsList, xref,user));
		assertNotNull(service.getAssetsStatusDetails(imei, deviceUuid,user.getUserName()));
	}

	private Device_Device_xref getDeviceDeviceXref() {
		Device_Device_xref device_xref = new Device_Device_xref();
		Device device = getDevice();
		device_xref.setActive(false);
		device_xref.setDateCreated(Instant.now());
		device_xref.setDateDeleted(Instant.now());
		device_xref.setDeviceUuid(device);
		device_xref.setId(12301L);
		device_xref.setSensorUuid(device);
		return device_xref;
	}

	private Asset_Device_xref getAssetDeviceXref() {
		User user = getUser();
		Asset_Device_xref xref = new Asset_Device_xref();
		xref.setActive(true);
		xref.setDateCreated(Instant.now());
		xref.setDateDeleted(Instant.now());
		xref.setId(1234L);
		xref.setCreatedBy(user);
		xref.setUpdatedBy(user);
		return xref;
	}

	private Asset getAsset() {
		User user = getUser();
		Manufacturer manufacturer = new Manufacturer();
		manufacturer.setId(121L);
		manufacturer.setName("Exatip");
		manufacturer.setUuid("rd2f1041-b9f7-4869-8bf5-107781fb23da");
		ManufacturerDetails details = new ManufacturerDetails();
		details.setConfig("Indore");
		details.setId(1231L);
		details.setManufacturer(manufacturer);
		details.setModel("Online");
		details.setUuid("ld2f1041-b9f7-4869-8bf5-107781fb23da");

		Asset asset = new Asset();
		asset.setAssignedName("Abc");
		asset.setCategory(null);
		asset.setComment("Done");
		asset.setCreatedBy(user);
		asset.setCreatedAt(Instant.now());
		asset.setGatewayEligibility("Xyz");
		asset.setId(1234L);
		asset.setIsApplicableForPrePair(true);
		asset.setIsVinValidated(true);
		asset.setManufacturer(manufacturer);
		asset.setManufacturerDetails(details);
		asset.setStatus(AssetStatus.ACTIVE);
		asset.setCreationMethod(AssetCreationMethod.MANUAL);
		asset.setOrganisation(getOrganisation());
		asset.setUpdatedBy(user);
		asset.setUuid("0d2f1041-b9f7-4869-8bf5-107781fb23da");
		asset.setVin("12qwe3rt");
		asset.setYear("2022");
		return asset;
	}
}