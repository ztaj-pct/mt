//package com.pct.organisation;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.verify;
//
//import java.time.Instant;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//import java.util.Optional;
//import java.util.Set;
//import java.util.stream.Collectors;
//
//import org.junit.jupiter.api.Order;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.Mockito;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.mockito.junit.jupiter.MockitoSettings;
//import org.mockito.quality.Strictness;
//
//import com.pct.common.constant.OrganisationType;
//import com.pct.common.model.Organisation;
//import com.pct.common.model.Role;
//import com.pct.common.model.User;
//import com.pct.common.util.Context;
//import com.pct.organisation.payload.AddOrganisationPayload;
//import com.pct.organisation.payload.CreateOrganisationPayload;
//import com.pct.organisation.payload.HubCustomer;
//import com.pct.organisation.payload.OrganisationAccess;
//import com.pct.organisation.payload.OrganisationAccessDTOForCAN;
//import com.pct.organisation.payload.OrganisationPayload;
//import com.pct.organisation.payload.OrganisationPayloadForCAN;
//import com.pct.organisation.repository.IOrganisationRepository;
//import com.pct.organisation.service.impl.OrganisationServiceImpl;
//import com.pct.organisation.util.BeanConvertor;
//import com.pct.organisation.util.RestUtils;
//
//@ExtendWith(MockitoExtension.class)
//@MockitoSettings(strictness = Strictness.LENIENT)
//class OrganisationServiceApplicationTests {
//	@Mock
//	private IOrganisationRepository organisationRepository;
//
//	@InjectMocks
//	private OrganisationServiceImpl service;
//	@Mock
//	BeanConvertor beanConvertor;
//
//	@Mock
//	private RestUtils restUtils;
//	Context context = new Context();
//
//	@Test
//	@Order(1)
//	public void test_saveCustomer() {
//		CreateOrganisationPayload createOrganisationPayload = new CreateOrganisationPayload();
//		createOrganisationPayload.setExtId("311234");
//		createOrganisationPayload.setIsApprovalReqForDeviceUpdate(true);
//		createOrganisationPayload.setIsAssetListRequired(true);
//		createOrganisationPayload.setIsAutoResetInstallation(true);
//		createOrganisationPayload.setIsMonthlyReleaseNotesReq(true);
//		createOrganisationPayload.setIsTestReqBeforeDeviceUpdate(true);
//		createOrganisationPayload.setIsDigitalSignReqForFirmware(true);
//		createOrganisationPayload.setNoOfDevice(123L);
//		createOrganisationPayload.setOrganisationName("Exatip");
//		createOrganisationPayload.setStatus(true);
//
//		Organisation organisation = new Organisation();
//		organisation.setOrganisationName(createOrganisationPayload.getOrganisationName());
//		organisation.setType(OrganisationType.CUSTOMER);
//		organisation.setIsActive(createOrganisationPayload.getStatus());
//		organisation.setAccountNumber(createOrganisationPayload.getExtId());
//		organisation.setIsAssetListRequired(createOrganisationPayload.getIsAssetListRequired());
//		organisation.setUuid("f7250a6c-74b7-47fd-91d3-98cd75ddf551");
//		organisation.setId(101L);
//		organisation.setShortName("Indore");
//		Mockito.when(organisationRepository.save(organisation)).thenReturn(organisation);
//		Organisation org = service.saveCustomer(createOrganisationPayload, new Context());
//		assertEquals(organisation.getOrganisationName(), org.getOrganisationName());
//	}
//
//	@Test
//	@Order(2)
//	public void test_save() {
//		OrganisationPayload or1 = new OrganisationPayload();
//		or1.setAccountNumber("301234");
//		or1.setId(101L);
//		or1.setIsAssetListRequired(true);
//		or1.setOrganisationName("Exatip");
//		or1.setShortName("Indore");
//		or1.setStatus(true);
//		or1.setType("Installer");
//		or1.setUuid("f7250a6c-74b7-47fd-91d3-98cd75ddf551");
//		OrganisationAccess organisationAccess = new OrganisationAccess();
//		organisationAccess.setOrganisationViewList(getListOfOrganisations());
//		organisationAccess.setCustomer(or1);
//		organisationAccess.setIsAssetListRequired(true);
//		organisationAccess.setStatus(true);
//		organisationAccess.setType("Installer");
//		Organisation organisation = new Organisation();
//		organisation.setOrganisationName(organisationAccess.getCustomer().getOrganisationName());
//		organisation.setType(OrganisationType.INSTALLER);
//		organisation.setIsActive(organisationAccess.getStatus());
//		organisation.setUuid(or1.getUuid());
//		Mockito.when(organisationRepository.save(organisation)).thenReturn(organisation);
//		Organisation org = service.save(organisationAccess, new Context());
//		assertEquals(organisation.getAccountNumber(), org.getAccountNumber());
//	}
//
//	@Test
//	@Order(3)
//	public void test_getCustomerOrganisationsFromHub() {
//		List<HubCustomer> customersFromHub = getHubCustomers();
//		Mockito.when(restUtils.getCustomersFromHub()).thenReturn(customersFromHub);
//		List<Organisation> customersFromDB = getListOfOrganisations();
//		Mockito.when(organisationRepository.findAllCustomer()).thenReturn(customersFromDB);
//		Set<String> accountNumbersFromDB = customersFromDB.stream()
//				.map(customerFromDB -> customerFromDB.getAccountNumber()).collect(Collectors.toSet());
//		List<HubCustomer> filteredHubCustomers = customersFromHub.stream()
//				.filter(customerFromHub -> !accountNumbersFromDB.contains(customerFromHub.getSalesforceAccountId()))
//				.collect(Collectors.toList());
//		List<OrganisationPayload> organisations = filteredHubCustomers.stream()
//				.map(beanConvertor::hubOrganisationToOganisationPayload).collect(Collectors.toList());
//
//		List<OrganisationPayload> organisations1 = service.getCustomerOrganisationsFromHub(context);
//		assertEquals(organisations.size(), organisations1.size());
//	}
//
//	@Test
//	@Order(4)
//	void test_getOrganisation() {
//		List<String> types = Arrays.asList("ALL", "Manufacturer", "Installer");
//		List<OrganisationType> organisationTypeList = new ArrayList<>();
//		if (types.get(0).equalsIgnoreCase("ALL")) {
//			organisationTypeList.add(OrganisationType.MANUFACTURER);
//			organisationTypeList.add(OrganisationType.CUSTOMER);
//			organisationTypeList.add(OrganisationType.INSTALLER);
//		} else {
//			types.forEach(type -> {
//				organisationTypeList.add(OrganisationType.getOrganisationType(type));
//			});
//		}
//		Boolean active = false;
//		List<Organisation> list = getListOfOrganisations();
//		Mockito.when(organisationRepository.getOrganisation(organisationTypeList, active)).thenReturn(list);
//		List<OrganisationPayload> abc1 = list.stream().map(beanConvertor::organisationToOrganisationPayload)
//				.collect(Collectors.toList());
//		List<OrganisationPayload> abc12 = service.getOrganisation(types, active, context);
//		assertEquals(abc1.size(), abc12.size());
//	}
//
//	@Test
//	@Order(6)
//	void test_getByCan() {
//		String accountNumber = "301234";
//		Organisation organisation = getListOfOrganisations().get(0);
//		OrganisationPayloadForCAN forCAN = m1(organisation);
//		Mockito.when(organisationRepository.findByAccountNumber(accountNumber)).thenReturn(organisation);
//		Mockito.when(beanConvertor.organisationToOrganisationPayloadForCAN(organisation)).thenReturn(forCAN);
//		OrganisationAccessDTOForCAN result = service.getByCan(accountNumber, context);
//		assertEquals(organisation.getOrganisationName(), result.getCustomer().getOrganisationName());
//	}
//
//	@Test
//	@Order(7)
//	void test_deleteById() {
//		Context context = new Context();
//		Organisation organisation = getListOfOrganisations().get(0);
//		Mockito.when(organisationRepository.findById(organisation.getId())).thenReturn(Optional.of(organisation));
//		Mockito.doNothing().when(organisationRepository).delete(any());
//		service.deleteById(organisation.getId(), context);
//		verify(organisationRepository).delete(any());
//	}
//
//	@Test
//	@Order(8)
//	void test_findDistinctOrganisationNames() {
//		List<String> organisations1 = Arrays.asList("Exatip", "Microsoft");
//		Mockito.when(organisationRepository.findDistinctOrganisationNames()).thenReturn(organisations1);
//		List<String> organisations2 = service.findDistinctOrganisationNames(context);
//		assertEquals(organisations1.size(), organisations2.size());
//	}
//
//	@Test
//	@Order(9)
//	void test_findAllCustomer() {
//		List<Organisation> organisations1 = getListOfOrganisations();
//		Mockito.when(organisationRepository.findAllCustomer()).thenReturn(organisations1);
//		List<Organisation> organisations2 = service.findAllCustomer();
//		assertEquals(organisations1.size(), organisations2.size());
//	}
//
//	@Test
//	@Order(10)
//	void test_getOrganisationByAccountNumber() {
//		Organisation organisation1 = getListOfOrganisations().get(0);
//		Mockito.when(organisationRepository.findByAccountNumber(organisation1.getAccountNumber()))
//				.thenReturn(organisation1);
//		Organisation organisation2 = service.getOrganisationByAccountNumber(organisation1.getAccountNumber(), context);
//		assertEquals(organisation1.getAccountNumber(), organisation2.getAccountNumber());
//	}
//
//	@Test
//	@Order(11)
//	void test_getOrganisationByType() {
//		List<Organisation> al = getListOfOrganisations();
//		OrganisationType type = al.get(0).getType();
//		System.out.println("Type:" + OrganisationType.getOrganisationType(type.getValue()));
//		Context context = new Context();
//		Mockito.when(organisationRepository.findByType(type)).thenReturn(al);
//		List<Organisation> result = service.getOrganisationByType(type.getValue(), context);
//		assertEquals(al.size(), result.size());
//	}
//
//	@Test
//	@Order(12)
//	public void test_SaveOrganisation() throws InstantiationException {
//		AddOrganisationPayload addOrganisationPayload = new AddOrganisationPayload();
//		addOrganisationPayload.setAccountNumber("301234");
//		addOrganisationPayload.setOrganisationName("Exatip");
//		addOrganisationPayload.setShortName("Indore");
//		addOrganisationPayload.setStatus(true);
//		addOrganisationPayload.setType(OrganisationType.CUSTOMER);
//		Organisation org = new Organisation();
//		org.setOrganisationName(addOrganisationPayload.getOrganisationName());
//		org.setShortName(addOrganisationPayload.getShortName());
//		org.setAccountNumber(addOrganisationPayload.getAccountNumber());
//		org.setIsActive(addOrganisationPayload.getStatus());
//		org.setType(addOrganisationPayload.getType());
//		org.setUuid("cee205a7-63f4-45ff-966a-5c5343b98fb2");
//		Mockito.when(organisationRepository.save(org)).thenReturn(org);
//		Mockito.when(beanConvertor.AddOrganisationPayloadToOrganisation(addOrganisationPayload)).thenReturn(org);
//		Boolean flag = service.SaveOrganisation(addOrganisationPayload, new Context());
//		assertEquals(true, flag);
//	}
//
//	@Test
//	@Order(13)
//	void test_update() {
//		AddOrganisationPayload addOrganisationPayload = new AddOrganisationPayload();
//		addOrganisationPayload.setAccountNumber("30412858902");
//		addOrganisationPayload.setEpicorId("1234");
//		addOrganisationPayload.setIsAssetListRequired(true);
//		addOrganisationPayload.setOrganisationName("Exatip");
//		addOrganisationPayload.setRecordId("4321");
//		addOrganisationPayload.setShortName("Exatip");
//		addOrganisationPayload.setStatus(true);
//		addOrganisationPayload.setType(OrganisationType.CUSTOMER);
//		Organisation organisation1 = getListOfOrganisations().get(0);
//		Mockito.when(organisationRepository.findByAccountNumber(addOrganisationPayload.getAccountNumber()))
//				.thenReturn(organisation1);
//		organisation1.setOrganisationName(addOrganisationPayload.getOrganisationName());
//		organisation1.setShortName(addOrganisationPayload.getShortName());
//		organisation1.setIsActive(addOrganisationPayload.getStatus());
//		organisation1.setType(addOrganisationPayload.getType());
//		Mockito.when(organisationRepository.save(organisation1)).thenReturn(organisation1);
//		Mockito.when(beanConvertor.OrganisationToAddOrganisationPayload(organisation1))
//				.thenReturn(addOrganisationPayload);
//		AddOrganisationPayload addOrganisationPayload2 = service.update(addOrganisationPayload, context);
//		assertEquals(addOrganisationPayload2.getAccountNumber(), addOrganisationPayload.getAccountNumber());
//	}
//
//	void test_deleteByUuid() {
//
//	}
//
//	private OrganisationPayloadForCAN m1(Organisation org) {
//		OrganisationPayloadForCAN organisationPayloadForCAN = new OrganisationPayloadForCAN();
//		organisationPayloadForCAN.setOrganisationName(org.getOrganisationName());
//		organisationPayloadForCAN.setId(org.getId());
//		organisationPayloadForCAN.setStatus(org.getIsActive());
//		organisationPayloadForCAN.setType(org.getType().getValue());
//		organisationPayloadForCAN.setAccountNumber(org.getAccountNumber());
//		organisationPayloadForCAN.setIsAssetListRequired(org.getIsAssetListRequired());
//		organisationPayloadForCAN.setUuid(org.getUuid());
//		return organisationPayloadForCAN;
//	}
//
//	private List<OrganisationPayload> getListOfOrganisationPayload() {
//		OrganisationPayload payload1 = new OrganisationPayload();
//		payload1.setAccountNumber("3012345");
//		payload1.setId(1001L);
//		payload1.setIsAssetListRequired(true);
//		payload1.setOrganisationName("Exatip");
//		payload1.setShortName("Exatip");
//		payload1.setStatus(true);
//		payload1.setType("Customer");
//		payload1.setUuid("f7250a6c-74b7-47fd-91d3-98cd75ddf551");
//		OrganisationPayload payload2 = new OrganisationPayload();
//		payload2.setAccountNumber("4012345");
//		payload2.setId(1001L);
//		payload2.setIsAssetListRequired(true);
//		payload2.setOrganisationName("Wipro");
//		payload2.setShortName("Wipro");
//		payload2.setStatus(true);
//		payload2.setType("Customer");
//		payload2.setUuid("f7250a6c-74b7-47fd-91d3-98cd75ddf0000");
//		return Arrays.asList(payload1, payload2);
//	}
//
//	private List<Organisation> getListOfOrganisations() {
//
//		Organisation or1 = new Organisation();
//		or1.setAccountNumber("301234");
//		or1.setId(101L);
//		or1.setIsActive(true);
//		or1.setIsAssetListRequired(true);
//		or1.setOrganisationName("Exatip");
//		or1.setShortName("Indore");
//		or1.setType(OrganisationType.CUSTOMER);
//
//		Organisation or2 = new Organisation();
//		or2.setAccountNumber("501234");
//		or2.setId(101L);
//		or2.setIsActive(true);
//		or2.setIsAssetListRequired(true);
//		or2.setOrganisationName("Wipro");
//		or2.setShortName("Pune");
//		or2.setType(OrganisationType.CUSTOMER);
//		return Arrays.asList(or1, or2);
//	}
//
//	private List<HubCustomer> getHubCustomers() {
//
//		HubCustomer hc1 = new HubCustomer();
//		hc1.setAccountName("301234");
//		hc1.setEpicorAccountId("54321");
//		hc1.setSalesforceAccountId("EX123");
//
//		HubCustomer hc2 = new HubCustomer();
//		hc2.setAccountName("40321");
//		hc2.setEpicorAccountId("12345");
//		hc2.setSalesforceAccountId("EX321");
//		return Arrays.asList(hc1, hc2);
//	}
//
//	private User getUser() {
//		User u1 = new User();
//		u1.setId(1L);
//		u1.setCountryCode("+91");
//		u1.setCreatedAt(Instant.now());
//		u1.setCreatedBy("Created By");
//		u1.setDeleted(true);
//		u1.setEmail("akshit@gmail.com");
//		u1.setFirstName("Akshit");
//		u1.setLastName("Rai");
//		u1.setIsActive(true);
//		u1.setIsDeleted(true);
//		u1.setIsPasswordChange(true);
//		u1.setNotify("email");
//
//		Organisation org = new Organisation();
//		org.setAccountNumber("123456");
//		org.setId(1L);
//		u1.setOrganisation(org);
//		u1.setPassword("akshit@123");
//		u1.setPhone("9999999999");
//		Role r = new Role();
//		r.setRoleId(1L);
//		List<Role> roleList= new ArrayList<>();
//		roleList.add(r);
//		u1.setRole(roleList);
//		// u1.setTimeZone("2022-02-24T03:19:05.720Z");
//		u1.setUpdatedAt(Instant.now());
//		u1.setUpdatedBy("user");
//		u1.setUserName("akshit@gmail.com");
//		u1.setUuid("0d2f1041-b9f7-4869-8bf5-107781fb23da");
//		return u1;
//	}
//
//}