package com.pct.device.service.impl;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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

import com.pct.common.model.Device;
import com.pct.common.model.DeviceForwarding;
import com.pct.common.model.Organisation;
import com.pct.common.model.Role;
import com.pct.common.model.User;
import com.pct.common.payload.DeviceForwardingRequest;
import com.pct.common.payload.Forwarding;
import com.pct.device.payload.DeviceForwardingResponse;
import com.pct.device.repository.DeviceForwardingRepository;
import com.pct.device.repository.IDeviceRepository;
import com.pct.device.service.IDeviceService;
import com.pct.device.util.BeanConverter;
import com.pct.device.util.RestUtils;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
public class DeviceForwardingServiceImplTest {

	@Mock
	DeviceForwardingRepository deviceForwardingRepository;

	@Mock
	private BeanConverter beanConverter;

	@Mock
	private IDeviceService deviceService;

	@Mock
	private RestUtils restUtils;

	@Mock
	private IDeviceRepository deviceRepository;

	@InjectMocks
	private DeviceForwardingServiceImpl deviceForwardingServiceImpl;

	private List<DeviceForwardingResponse> getListOfDeviceForwardingResponses(List<DeviceForwarding> al) {
		List<DeviceForwardingResponse> list = new ArrayList<DeviceForwardingResponse>();
		if (al != null && !al.isEmpty()) {
			al.forEach(pm -> {
				DeviceForwardingResponse response = new DeviceForwardingResponse();
				response.setId(pm.getId());
				response.setUuid(pm.getUuid());
				response.setType(pm.getType());
				response.setUrl(pm.getUrl());
				Device device = new Device();
				if (pm.getDevice() != null) {
					device.setUuid(pm.getDevice().getUuid());
					device.setImei(pm.getDevice().getImei());
				}
				response.setDevice(device);
				list.add(response);
			});
		}
		return list;
	}

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
		user.setUpdatedBy("UpdatedBy");
		user.setUserName("akshit@gmail.com");
		user.setUuid("0d2f1041-b9f7-4869-8bf5-107781fb23da");
		return user;
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
		return device;
	}

	private DeviceForwarding getDeviceForwarding() {
		DeviceForwarding deviceForwarding = new DeviceForwarding();
		deviceForwarding.setId(12345L);
		deviceForwarding.setUuid("0d2f1041-b9f7-4869-8bf5-107781fb23RR");
		deviceForwarding.setCreatedOn(Instant.now());
		deviceForwarding.setType("Electronic");
		deviceForwarding.setUrl("www.exatip.com");
		return deviceForwarding;
	}

	@Test
	@Order(1)
	public void test_addDeviceForwarding_1() throws Exception {
		User user = getUser();
		Device device = getDevice();
		DeviceForwarding deviceForwarding = getDeviceForwarding();
		deviceForwarding.setCreatedBy(user);
		deviceForwarding.setDevice(device);
		Mockito.when(restUtils.getUserFromAuthService(user.getUserName())).thenReturn(user);
		Mockito.when(deviceRepository.findByImei(device.getImei())).thenReturn(device);
		Mockito.when(deviceForwardingRepository.save(deviceForwarding)).thenReturn(deviceForwarding);
		Forwarding deviceForward = new Forwarding();
		deviceForward.setId(101L);
		deviceForward.setType("Electronic");
		deviceForward.setUrl("www.exatip.com");
		deviceForward.setUuId("0d2f1041-b9f7-4869-8bf5-107781fb23db");
		DeviceForwardingRequest df = new DeviceForwardingRequest();
		df.setDeviceId(device.getImei());
		df.setForwardingList(Arrays.asList(deviceForward));
		Boolean actual = deviceForwardingServiceImpl.addDeviceForwarding(df, user.getUserName());
		Boolean expected = true;
		Assert.assertEquals(expected, actual);
	}

	@Test
	@Order(11)
	public void test_addDeviceForwarding_2() throws Exception {
		User user = getUser();
		Device device = getDevice();
		DeviceForwarding deviceForwarding = getDeviceForwarding();
		deviceForwarding.setCreatedBy(user);
		deviceForwarding.setDevice(device);
		Mockito.when(restUtils.getUserFromAuthService(user.getUserName())).thenReturn(user);
		Mockito.when(deviceRepository.findByImei(device.getImei())).thenReturn(device);
		Mockito.when(deviceForwardingRepository.save(deviceForwarding)).thenReturn(deviceForwarding);
		Forwarding deviceForward = new Forwarding();
		deviceForward.setId(101L);
		deviceForward.setType("Electronic");
		deviceForward.setUrl("www.exatip.com");
		deviceForward.setUuId(null);
		DeviceForwardingRequest df = new DeviceForwardingRequest();
		df.setDeviceId(device.getImei());
		df.setForwardingList(Arrays.asList(deviceForward));
		Boolean actual = deviceForwardingServiceImpl.addDeviceForwarding(df, user.getUserName());
		Boolean expected = true;
		Assert.assertEquals(expected, actual);
	}

	@Test
	@Order(2)
	public void test_updateDeviceForwarding_1() throws Exception {
		Device device = getDevice();
		DeviceForwarding deviceForwarding = getDeviceForwarding();
		deviceForwarding.setDevice(device);
		deviceForwarding.setUpdatedOn(Instant.now());
		Mockito.when(deviceForwardingRepository.findByUuid(deviceForwarding.getUuid())).thenReturn(deviceForwarding);
		Mockito.when(deviceService.getDevice(deviceForwarding.getDevice().getImei())).thenReturn(device);
		Boolean actual = deviceForwardingServiceImpl.updateDeviceForwarding(deviceForwarding);
		Boolean expected = true;
		Assert.assertEquals(expected, actual);
	}

	@Test
	@Order(3)
	public void test_deleteDeviceForwarding_1() throws Exception {
		Device device = getDevice();
		DeviceForwarding deviceForwarding = getDeviceForwarding();
		deviceForwarding.setDevice(device);
		Mockito.when(deviceForwardingRepository.findByUuid(device.getUuid())).thenReturn(deviceForwarding);
		boolean deleteDeviceForwarding = deviceForwardingServiceImpl.deleteDeviceForwarding(device.getUuid());
		assertTrue(deleteDeviceForwarding);
	}

	@Test
	@Order(4)
	public void test_deleteDeviceForwarding_2() throws Exception {
		Device device = getDevice();
		device.setUuid("0d2f1041-b9f7-4869-8bf5-107781fb23db");
		DeviceForwarding deviceForwarding = getDeviceForwarding();
		deviceForwarding.setDevice(device);
		Mockito.when(deviceForwardingRepository.findByUuid(device.getUuid())).thenReturn(deviceForwarding);
		boolean deleteDeviceForwarding = deviceForwardingServiceImpl.deleteDeviceForwarding(device.getUuid());
		assertTrue(deleteDeviceForwarding);
	}

	@Test
	@Order(5)
	public void test_getDeviceForwardingById_1() throws Exception {
		DeviceForwarding deviceForwarding = getDeviceForwarding();
		List<DeviceForwarding> al = new ArrayList<DeviceForwarding>();
		al.add(deviceForwarding);
		List<DeviceForwardingResponse> actual = getListOfDeviceForwardingResponses(al);
		Mockito.when(deviceForwardingRepository.findById(deviceForwarding.getId()))
				.thenReturn(Optional.of(deviceForwarding));
		Mockito.when(beanConverter.convertDeviceForwardingToDeviceForwardingResponse(al)).thenReturn(actual);
		List<DeviceForwardingResponse> expected = deviceForwardingServiceImpl
				.getDeviceForwardingById(deviceForwarding.getId());
		assertNotNull(expected);
		Assert.assertEquals(expected.size(), actual.size());
	}

	@Test
	@Order(6)
	public void test_getDeviceForwardingById_2() throws Exception {
		DeviceForwarding deviceForwarding = getDeviceForwarding();
		List<DeviceForwarding> al = new ArrayList<DeviceForwarding>();
		al.add(deviceForwarding);
		List<DeviceForwardingResponse> actual = getListOfDeviceForwardingResponses(al);
		Mockito.when(deviceForwardingRepository.findAll()).thenReturn(al);
		Mockito.when(beanConverter.convertDeviceForwardingToDeviceForwardingResponse(al)).thenReturn(actual);
		List<DeviceForwardingResponse> expected = deviceForwardingServiceImpl.getDeviceForwardingById(null);
		assertNotNull(expected);
		Assert.assertEquals(expected.size(), actual.size());
	}

	@Test
	@Order(7)
	public void test_getDeviceForwardingByUuid_1() throws Exception {
		DeviceForwarding deviceForwarding = getDeviceForwarding();
		List<DeviceForwarding> al = new ArrayList<DeviceForwarding>();
		al.add(deviceForwarding);
		List<DeviceForwardingResponse> actual = getListOfDeviceForwardingResponses(al);
		Mockito.when(deviceForwardingRepository.findByUuid(deviceForwarding.getUuid())).thenReturn(deviceForwarding);
		Mockito.when(beanConverter.convertDeviceForwardingToDeviceForwardingResponse(al)).thenReturn(actual);
		List<DeviceForwardingResponse> expected = deviceForwardingServiceImpl
				.getDeviceForwardingByUuid(deviceForwarding.getUuid());
		assertNotNull(actual);
		Assert.assertEquals(expected.size(), actual.size());

	}

	@Test
	@Order(8)
	public void test_getDeviceForwardingByUuid_2() throws Exception {
		DeviceForwarding deviceForwarding = getDeviceForwarding();
		List<DeviceForwarding> al = new ArrayList<DeviceForwarding>();
		al.add(deviceForwarding);
		List<DeviceForwardingResponse> actual = getListOfDeviceForwardingResponses(al);
		Mockito.when(deviceForwardingRepository.findAll()).thenReturn(al);
		Mockito.when(beanConverter.convertDeviceForwardingToDeviceForwardingResponse(al)).thenReturn(actual);
		List<DeviceForwardingResponse> expected = deviceForwardingServiceImpl.getDeviceForwardingByUuid(null);
		assertNotNull(expected);
		Assert.assertEquals(expected.size(), actual.size());
	}

	@Test
	@Order(9)
	public void test_getAllDeviceForwarding() throws Exception {
		DeviceForwarding deviceForwarding = getDeviceForwarding();
		List<DeviceForwarding> al = new ArrayList<DeviceForwarding>();
		al.add(deviceForwarding);
		List<DeviceForwardingResponse> actual = getListOfDeviceForwardingResponses(al);
		Mockito.when(deviceForwardingRepository.findAll()).thenReturn(al);
		Mockito.when(beanConverter.convertDeviceForwardingToDeviceForwardingResponse(al)).thenReturn(actual);
		List<DeviceForwardingResponse> expected = deviceForwardingServiceImpl.getAllDeviceForwarding();
		assertNotNull(expected);
		Assert.assertEquals(expected.size(), actual.size());
	}

	@Test
	@Order(10)
	public void test_getAllDeviceForwardingByImei() throws Exception {
		String imei = getDevice().getImei();
		DeviceForwarding deviceForwarding = getDeviceForwarding();
		List<DeviceForwarding> al = new ArrayList<DeviceForwarding>();
		al.add(deviceForwarding);
		List<DeviceForwardingResponse> actual = getListOfDeviceForwardingResponses(al);
		Mockito.when(deviceForwardingRepository.findByImei(imei)).thenReturn(al);
		Mockito.when(beanConverter.convertDeviceForwardingToDeviceForwardingResponse(al)).thenReturn(actual);
		List<DeviceForwardingResponse> expected = deviceForwardingServiceImpl.getAllDeviceForwardingByImei(imei);
		assertNotNull(expected);
		Assert.assertEquals(expected.size(), actual.size());
	}
}