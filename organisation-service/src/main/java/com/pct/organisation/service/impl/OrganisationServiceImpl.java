package com.pct.organisation.service.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSON;
import com.pct.common.constant.OrganisationRole;
import com.pct.common.model.CustomerForwardingGroupMapper;
import com.pct.common.model.CustomerForwardingGroupMapperId;
import com.pct.common.model.CustomerForwardingRule;
import com.pct.common.model.CustomerForwardingRuleUrl;
import com.pct.common.model.Organisation;
//import com.pct.common.model.OrganisationRole;
import com.pct.common.model.OrganisationSection;
import com.pct.common.model.OrganisationSettings;
import com.pct.common.model.Role;
import com.pct.common.model.User;
import com.pct.common.redis.CustomerPrefix;
import com.pct.common.util.Context;
import com.pct.common.util.Logutils;
import com.pct.common.util.Message;
import com.pct.organisation.config.RedisService;
import com.pct.organisation.dto.OrganisationDTO;
import com.pct.organisation.exception.BadRequestException;
import com.pct.organisation.exception.BaseMessageException;
import com.pct.organisation.exception.OrganisationException;
import com.pct.organisation.payload.AddOrganisationPayload;
import com.pct.organisation.payload.AddOrganisationResponse;
import com.pct.organisation.payload.CreateOrganisationPayload;
import com.pct.organisation.payload.CustomerForwardingGroupMapperPayload;
import com.pct.organisation.payload.CustomerForwardingRulePayload;
import com.pct.organisation.payload.HubCustomer;
import com.pct.organisation.payload.OrganisationAccess;
import com.pct.organisation.payload.OrganisationAccessDTO;
import com.pct.organisation.payload.OrganisationAccessDTOForCAN;
import com.pct.organisation.payload.OrganisationListPayload;
import com.pct.organisation.payload.OrganisationPayload;
import com.pct.organisation.payload.OrganisationRequest;
import com.pct.organisation.repository.CustomerForwardingGroupMapperRepository;
import com.pct.organisation.repository.CustomerForwardingGroupRepository;
import com.pct.organisation.repository.CustomerForwardingRuleRepository;
import com.pct.organisation.repository.CustomerForwardingRuleUrlRepository;
import com.pct.organisation.repository.IOrganisationRepository;
import com.pct.organisation.repository.IOrganisationSectionRepository;
import com.pct.organisation.repository.IOrganisationSettingsRepository;
import com.pct.organisation.service.IOrganisationService;
import com.pct.organisation.specification.OrganisationSpecification;
import com.pct.organisation.util.AuthoritiesConstants;
import com.pct.organisation.util.BeanConvertor;
import com.pct.organisation.util.RestUtils;

@Service
public class OrganisationServiceImpl implements IOrganisationService {

	private static final String CFR_FIELD_ACCOUNT_NUMBER = "ACCOUNT_NUMBER";
	private static final String CFR_FIELD_TYPES = "TYPES";
	private static final String CFR_FIELD_URLS = "URLS";
	private static final String CFR_FIELD_KAFKA_TOPIC_NMAES = "KAFKA_TOPIC_NAMES";
	Logger logger = LoggerFactory.getLogger(OrganisationServiceImpl.class);
	public static final String className = "OrganisationServiceImpl";

	@Autowired
	private IOrganisationRepository organisationRepository;

	@Autowired
	private IOrganisationSectionRepository organisationSectionRepository;

	@Autowired
	private IOrganisationSettingsRepository organisationSettingsRepository;

	@Autowired
	private CustomerForwardingGroupMapperRepository customerForwardingGroupMapperRepository;

	@Autowired
	private CustomerForwardingRuleRepository customerForwardingRuleRepository;

	@Autowired
	private CustomerForwardingRuleUrlRepository customerForwardingRuleUrlRepository;

	@Autowired
	private CustomerForwardingGroupRepository customerForwardingGroupRepository;

	@Autowired
	private BeanConvertor beanConvertor;

	@Autowired
	private RestUtils restUtils;

	@Autowired
	private RedisService redisService;

	@Override
	public Organisation saveCustomer(CreateOrganisationPayload createOrganisationPayload, Context context) {
		logger.info("Inside saveCustomer and fetch createOrganisationPayload", createOrganisationPayload);
		String methodName = "saveCustomer";

		Logutils.log(className, methodName, context.getLogUUId(),
				" Before calling saveCustomer Method From OrganisationServiceImpl Service method in OrganisationServiceImpl",
				logger);

		Organisation organisation = new Organisation();
		organisation.setOrganisationName(createOrganisationPayload.getOrganisationName());
		organisation.setIsActive(createOrganisationPayload.getStatus());
		organisation.setAccountNumber(createOrganisationPayload.getExtId());
		organisation.setIsAssetListRequired(createOrganisationPayload.getIsAssetListRequired());

		String organisationUuid = "";
		boolean isOrganisationUuidUnique = false;
		while (!isOrganisationUuidUnique) {
			organisationUuid = UUID.randomUUID().toString();
			Organisation byUuid = organisationRepository.findByUuid(organisationUuid);
			if (byUuid == null) {
				isOrganisationUuidUnique = true;
			}
		}
		organisation.setUuid(organisationUuid);
		organisation = organisationRepository.save(organisation);
		logger.info("Exiting from saveCustomer Method From OrganisationServiceImpl");
		return organisation;
	}

	@Override
	public Organisation save(OrganisationAccess organisationPayload, Context context) {
		logger.info("Inside save Method and fetch organisationPayload", organisationPayload);
		String methodName = "save";

		Organisation organisation = new Organisation();
		try {
			logger.debug("Inside try block of save Method From OrganisationServiceImpl ");
			if (organisationPayload.getType().equalsIgnoreCase(OrganisationRole.INSTALLER.getValue())) {
				organisation.setOrganisationName(organisationPayload.getCustomer().getOrganisationName());
				organisation.setIsActive(organisationPayload.getStatus());
				String organisationUuid = "";
				boolean isOrganisationUuidUnique = false;
				while (!isOrganisationUuidUnique) {
					organisationUuid = UUID.randomUUID().toString();
					Organisation byUuid = organisationRepository.findByUuid(organisationUuid);
					if (byUuid == null) {
						isOrganisationUuidUnique = true;
					}
				}
				organisation.setUuid(organisationUuid);
				Logutils.log(className, methodName, context.getLogUUId(), " Fetch uuid value", logger,
						organisationUuid);
			}
//			if (!CollectionUtils.isEmpty(organisationPayload.getOrganisationViewList())) {
//				organisation.setAccessList(organisationPayload.getOrganisationViewList());
//			}
			organisationRepository.save(organisation);
			logger.info("Exiting from save Method of OrganisationServiceImpl");
			return organisation;
		} catch (Exception e) {
			logger.error("Exception occurred while save organisation", e);
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public List<OrganisationPayload> getCustomerOrganisationsFromHub(Context context) {
		logger.info("Inside getCustomerOrganisationsFromHub service ");
		String methodName = "getCustomerOrganisationsFromHub";
//		Logutils.log(className, methodName, context.getLogUUId(),
//				" Before calling getCustomerOrganisationsFromHub method from OrganisationServiceImpl ", logger);
		List<HubCustomer> customersFromHub = restUtils.getCustomersFromHub();
		List<Organisation> customersFromDB = organisationRepository.findAllCustomer();
		logger.info("Fectching all customer details");
		Set<String> accountNumbersFromDB = customersFromDB.stream()
				.map(customerFromDB -> customerFromDB.getAccountNumber()).collect(Collectors.toSet());
		List<HubCustomer> filteredHubCustomers = customersFromHub.stream()
				.filter(customerFromHub -> !accountNumbersFromDB.contains(customerFromHub.getSalesforceAccountId()))
				.collect(Collectors.toList());
		List<OrganisationPayload> organisations = filteredHubCustomers.stream()
				.map(beanConvertor::hubOrganisationToOganisationPayload).collect(Collectors.toList());

//		Logutils.log(className, methodName, context.getLogUUId(),
//				" After calling getCustomerOrganisationsFromHub method from OrganisationServiceImpl ", logger);
//		logger.info("Exiting from getCustomerOrganisationsFromHub Method of OrganisationServiceImpl");
		return organisations;
	}

	@Override
	public List<OrganisationPayload> getOrganisation(List<String> types, Boolean active, Context context) {
		logger.info("Inside getOrganisation Method ");
		String methodName = "getOrganisation";
		Logutils.log(className, methodName, context.getLogUUId(),
				" Before calling getOrganisation method from OrganisationServiceImpl ", logger);

		if (active == null) {
			active = true;
		}
		List<OrganisationRole> organisationTypeList = new ArrayList<>();
		if (types.get(0).equalsIgnoreCase("ALL")) {
			organisationTypeList.add(OrganisationRole.MANUFACTURER);
			organisationTypeList.add(OrganisationRole.END_CUSTOMER);
			organisationTypeList.add(OrganisationRole.INSTALLER);
		} else {
			types.forEach(type -> {
				organisationTypeList.add(OrganisationRole.getOrganisationRole(type));
			});
		}

		List<OrganisationPayload> organisations = organisationRepository.getOrganisation(organisationTypeList, active)
				.stream().map(beanConvertor::organisationToOrganisationPayload).collect(Collectors.toList());

		Logutils.log(className, methodName, context.getLogUUId(),
				" After calling getOrganisation method from OrganisationServiceImpl ", logger);
		logger.info("Exiting from getOrganisation Method of OrganisationServiceImpl");
		return organisations;
	}

	@Override
	public Page<OrganisationPayload> getAllAOrganisation(Map<String, String> filterValues, Pageable pageable,
			String type, String userName, Context context, String sort) {
		logger.info("Inside getAllAOrganisation Method and fetch userId : ", userName);
		String methodName = "getAllAOrganisation";
		Logutils.log(className, methodName, context.getLogUUId(),
				" Before calling getAllAOrganisation method from OrganisationServiceImpl ", logger);

		User user = restUtils.getUserFromAuthService(userName);

		Specification<Organisation> spc = OrganisationSpecification.getOrganisationSpecification(filterValues, user, sort);

		logger.info("Fetching organisation detail(s) based on specification");
		Page<Organisation> all = organisationRepository.findAll(spc, pageable);

		List<OrganisationPayload> organisationPayloadList = new ArrayList<>();
		all.forEach(organisation -> {
			OrganisationPayload organisationPayload = beanConvertor.organisationToOrganisationPayload(organisation);
			organisationPayload.setResellers(organisation.getResellerList().stream()
					.map(r -> beanConvertor.convertOrganisationBasicInfoToOrganisationPayload(r))
					.collect(Collectors.toList()));
			organisationPayload.setForwardingGroup(organisation.getForwardingGroupMappers().stream()
					.map(g -> g.getCustomerForwardingGroup().getName()).findAny().orElse(""));
			organisationPayloadList.add(organisationPayload);
		});
		Page<OrganisationPayload> pageOfOrganisationPayload = new PageImpl<>(organisationPayloadList, pageable,
				all.getTotalElements());

		Logutils.log(className, methodName, context.getLogUUId(),
				" After calling getAllAOrganisation method from OrganisationServiceImpl ", logger);
		logger.info("Exiting from getAllAOrganisation Method of OrganisationServiceImpl");

		return pageOfOrganisationPayload;
	}

	@Override
	public OrganisationAccessDTO getById(Long id, Context context) {
		logger.info("Inside getById Method From OrganisationServiceImpl");
		String methodName = "getById";
		Logutils.log(className, methodName, context.getLogUUId(),
				" Before calling getById method from OrganisationServiceImpl ", logger);

		Organisation organisation = null;
		organisation = organisationRepository.findById(id).get();
		OrganisationAccessDTO oa = new OrganisationAccessDTO();
		List<Organisation> accessibleOrganistions = new ArrayList<Organisation>();
		// oa.setCustomer(beanConvertor.organisationToOrganisationPayload(organisation));
		return oa;

	}

	@Override
	public OrganisationAccessDTOForCAN getByCan(String accountNumber, Context context) {
		Organisation organisation = null;
		logger.info("Inside getByCan Method");
		String methodName = "getByCan";
		Logutils.log(className, methodName, context.getLogUUId(),
				" Before calling getByCan method from OrganisationServiceImpl ", logger);

		organisation = organisationRepository.findBySalesForceAccountNumber(accountNumber);
		OrganisationAccessDTOForCAN oa = new OrganisationAccessDTOForCAN();
		List<Organisation> accessibleOrganisations = new ArrayList<>();
		oa.setCustomer(beanConvertor.organisationToOrganisationPayloadForCAN(organisation));
		return oa;
	}

	@Override
	public void deleteById(Long id, Context context) {
		logger.info("Inside deleteById Method");
		String methodName = "deleteById";
		Logutils.log(className, methodName, context.getLogUUId(),
				" Before calling getByCan method from OrganisationServiceImpl ", logger);

		Organisation organisation = organisationRepository.findById(id).get();
		if (organisation == null) {
			throw new BadRequestException("organisation not found by given id");
		}
		organisationRepository.delete(organisation);

	}

	@Override
	public List<String> findDistinctOrganisationNames(Context context) {
		logger.info("Inside findDistinctOrganisationNames Method");
		String methodName = "findDistinctOrganisationNames";
		Logutils.log(className, methodName, context.getLogUUId(),
				" Before calling findDistinctOrganisationNames method from OrganisationServiceImpl ", logger);

		List<String> organisations = organisationRepository.findDistinctOrganisationNames();
		return organisations;
	}

	@Override
	public List<Organisation> findAllCustomer() {
		return organisationRepository.findAllCustomer();
	}

	@Override
	public Organisation getOrganisationByAccountNumber(String accountNumber, Context context) {
		logger.info("Inside getOrganisationByAccountNumber Method and fetch Account number", accountNumber);
		String methodName = "getOrganisationByAccountNumber";
		Logutils.log(className, methodName, context.getLogUUId(),
				" Before calling getOrganisationByAccountNumber method from CustomerService ", logger);

		Organisation organisation = organisationRepository.findBySalesForceAccountNumber(accountNumber);
		return organisation;
	}

	@Override
	public List<Organisation> getOrganisationByType(String type, Context context) {
		logger.info("Inside getOrganisationByType Method");
		String methodName = "getOrganisationByType";
		Logutils.log(className, methodName, context.getLogUUId(),
				" Before calling getOrganisationByType method from CustomerService ", logger);

		List<Organisation> organisations = organisationRepository
				.findByType(OrganisationRole.getOrganisationRole(type));

		logger.info("Exiting from getOrganisationByType Method of CustomerController");
		return organisations;
	}

	@Override
	@Transactional
	public Organisation saveOrganisation(AddOrganisationPayload addOrganisationPayload, Context context)
			throws InstantiationException {
		Organisation orgs= new Organisation();
		logger.info("Inside saveOrganisation service for OrganisationPayload", addOrganisationPayload);
		String methodName = "saveOrganisation";
		logger.info("Inside try block of saveOrganisation Method From OrganisationServiceImpl");
		Logutils.log(className, methodName, context.getLogUUId(),
				" Before calling saveOrganisation method from OrganisationServiceImpl ", logger);

		validateOrganizationRole(addOrganisationPayload);

		if (addOrganisationPayload.getId() != null && addOrganisationPayload.getId() != 0) {
			Organisation organisation = organisationRepository.findById(addOrganisationPayload.getId()).get();
			if (organisation == null) {
				throw new InstantiationException("Organisation Not Available");
			}
			organisation.setOrganisationName(addOrganisationPayload.getOrganisationName());
			organisation.setIsActive(addOrganisationPayload.getStatus());
			organisation.setEpicorAccountNumber(addOrganisationPayload.getEpicorAccountNumber());
			validateSalesforceAccountNumber(organisation, addOrganisationPayload.getSalesforceAccountNumber());
			organisation.setAccountNumber(addOrganisationPayload.getSalesforceAccountNumber());
			organisation.setOrganisationRole(addOrganisationPayload.getOrganisationRole());
			organisation.setIsAssetListRequired(addOrganisationPayload.getIsAssetListRequired());
			organisation.setAccessList(new ArrayList<>());
			organisation.setResellerList(new ArrayList<>());
			organisation.setInstallerList(new ArrayList<>());
			organisation.setMaintenanceList(new ArrayList<>());

			if (addOrganisationPayload.getOrganisationRole().contains(OrganisationRole.INSTALLER)) {

				List<Organisation> organisationList = organisationRepository
						.getAllOrganisationByName(addOrganisationPayload.getAccessId());
				organisation.setAccessList(organisationList);
			}

			if (addOrganisationPayload.getOrganisationRole().contains(OrganisationRole.END_CUSTOMER)) {
				if (addOrganisationPayload.getResellerList() != null
						&& !addOrganisationPayload.getResellerList().isEmpty()) {
					organisation.setResellerList(beanConvertor.convertOrganisationCompanyIdPayloadIntoOrganisation(
							addOrganisationPayload.getResellerList()));
				}

				if (addOrganisationPayload.getInstallerList() != null
						&& !addOrganisationPayload.getInstallerList().isEmpty()) {
					organisation.setInstallerList(beanConvertor.convertOrganisationCompanyIdPayloadIntoOrganisation(
							addOrganisationPayload.getInstallerList()));
				}
				
				if (addOrganisationPayload.getMaintenanceList() != null
						&& !addOrganisationPayload.getMaintenanceList().isEmpty()) {
					organisation.setMaintenanceList(beanConvertor.convertOrganisationCompanyIdPayloadIntoOrganisation(
							addOrganisationPayload.getMaintenanceList()));
				}

			}

			UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication()
					.getPrincipal();

			User user = restUtils.getUserFromAuthService(userDetails.getUsername());
			organisation.setCreatedBy(user);
			organisation.setCreatedAt(Instant.now());

			organisation = organisationRepository.save(organisation);
			orgs= organisation;

			if (addOrganisationPayload.getOrganisationRole().contains(OrganisationRole.END_CUSTOMER)) {
				List<OrganisationSettings> organisationSettingsList = organisationSettingsRepository
						.findOrganisationSettingsByOrganisationUuid(organisation.getUuid());
				if (organisationSettingsList != null && organisationSettingsList.size() > 0) {
					List<OrganisationSettings> organisationSettingsNewList = new ArrayList<>();
					for (OrganisationSettings organisationSettings : organisationSettingsList) {
						switch (organisationSettings.getFieldName()) {
						case "isApprovalReqForDeviceUpdate":
							organisationSettings.setFieldValue(
									Boolean.toString(addOrganisationPayload.getIsApprovalReqForDeviceUpdate()));
							break;
						case "isTestReqBeforeDeviceUpdate":
							organisationSettings.setFieldValue(
									Boolean.toString(addOrganisationPayload.getIsTestReqBeforeDeviceUpdate()));
							break;
						case "isMonthlyReleaseNotesReq":
							organisationSettings.setFieldValue(
									Boolean.toString(addOrganisationPayload.getIsMonthlyReleaseNotesReq()));
							break;
						case "isDigitalSignReqForFirmware":
							organisationSettings.setFieldValue(
									Boolean.toString(addOrganisationPayload.getIsDigitalSignReqForFirmware()));
							break;
						case "noOfDevice":
							organisationSettings
									.setFieldValue(Integer.toString(addOrganisationPayload.getNoOfDevice()));
							break;
						default:
							break;
						}
						organisationSettingsNewList.add(organisationSettings);
					}

					if (organisationSettingsNewList != null && organisationSettingsNewList.size() > 0) {
						organisationSettingsRepository.saveAll(organisationSettingsNewList);
					}
				} else {
					addOrganisationSettings(organisation.getUuid(), addOrganisationPayload);
				}

			}

			saveCustomerForwardingRules(addOrganisationPayload.getCustomerForwardingRules(), organisation, user,
					context);
			saveCustomerForwardingGroup(addOrganisationPayload.getCustomerForwardingGroups(), organisation, user,
					context);

		} else {
			if (addOrganisationPayload != null
					&& (addOrganisationPayload.getOrganisationRole().contains(OrganisationRole.END_CUSTOMER)
							|| addOrganisationPayload.getSalesforceAccountNumber() != null)) {
				if (!"".equals(addOrganisationPayload.getSalesforceAccountNumber())
						&& addOrganisationPayload.getSalesforceAccountNumber() != null
						&& addOrganisationPayload.getOrganisationRole().contains(OrganisationRole.END_CUSTOMER)) {
					Organisation org = organisationRepository
							.findBySalesForceAccountNumber(addOrganisationPayload.getSalesforceAccountNumber());
					if (org != null && org.getUuid() != null) {
						throw new OrganisationException(
								"Account number is already registered with given Account Number :"
										+ org.getAccountNumber());
					}
				}

				List<Organisation> org2 = organisationRepository
						.findByOrganisationByOrganisationName(addOrganisationPayload.getOrganisationName());
				if (org2 != null && org2.size() > 0) {
					throw new OrganisationException("Organsation is already registered with given Organisation Name :"
							+ addOrganisationPayload.getOrganisationName());
				}
				Organisation addOrganisationPayloadToOrganisation = beanConvertor
						.addOrganisationPayloadToOrganisation(addOrganisationPayload);
				String organisationUuid = "";
				boolean isOrganisationUuidUnique = false;

				while (!isOrganisationUuidUnique) {
					organisationUuid = UUID.randomUUID().toString();
					Organisation byUuid = organisationRepository.findByUuid(organisationUuid);
					if (byUuid == null) {
						isOrganisationUuidUnique = true;
					}
				}
				UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication()
						.getPrincipal();
				User user = restUtils.getUserFromAuthService(userDetails.getUsername());
				addOrganisationPayloadToOrganisation.setCreatedBy(user);
				addOrganisationPayloadToOrganisation.setCreatedAt(Instant.now());

				addOrganisationPayloadToOrganisation.setUuid(organisationUuid);
				orgs = organisationRepository.save(addOrganisationPayloadToOrganisation);
				if (addOrganisationPayload.getOrganisationRole().contains(OrganisationRole.END_CUSTOMER)) {
					addOrganisationSettings(organisationUuid, addOrganisationPayload);
				}

				saveCustomerForwardingRules(addOrganisationPayload.getCustomerForwardingRules(),
						addOrganisationPayloadToOrganisation, user, context);
				saveCustomerForwardingGroup(addOrganisationPayload.getCustomerForwardingGroups(),
						addOrganisationPayloadToOrganisation, user, context);

				logger.info("Organisation details saved successfully.");

			} else {
				throw new InstantiationException("Organisation payload/account number can't be null");
			}
		}
		return orgs;
	}

	private void validateSalesforceAccountNumber(Organisation organisation, String salesforceAccountNumber) {
		logger.info("Inside the validate sales force account number");
		if (!organisation.getAccountNumber().equals(salesforceAccountNumber)) {
			Set<String> deviceImeis = restUtils.getImeisByAccountNumber(organisation.getAccountNumber());
			if (!deviceImeis.isEmpty()) {
				logger.info("Organisation account number associated device");
				throw new BaseMessageException("Account Number can not be updated");
			}
		}
		logger.info("Successfully validate sales force account number");
	}

	private void validateOrganizationRole(AddOrganisationPayload addOrganisationPayload) {
		if (addOrganisationPayload.getOrganisationRole() == null
				|| addOrganisationPayload.getOrganisationRole().isEmpty()) {
			throw new BaseMessageException("Atleast one organization role is required");
		}

		if (addOrganisationPayload.getOrganisationRole().stream().noneMatch(r -> r.equals(OrganisationRole.END_CUSTOMER)
				|| r.equals(OrganisationRole.INSTALLER) || r.equals(OrganisationRole.RESELLER) || r.equals(OrganisationRole.MAINTENANCE_MODE))) {
			throw new BaseMessageException("Invalid organization role type");
		}
	}

	private void saveCustomerForwardingGroup(List<CustomerForwardingGroupMapperPayload> customerForwardingGroupPayloads,
			Organisation organisation, User user, Context context) {

		Logutils.log(className, "saveCustomerForwardingGroup", context.getLogUUId(),
				" Inside the saveCustomerForwardingGroup method from OrganisationServiceImpl ", logger);

		List<CustomerForwardingGroupMapper> customerForwardingGroupMappers = customerForwardingGroupMapperRepository
				.findByOrganisationUuid(organisation.getUuid());

		if (organisation.getOrganisationRole().contains(OrganisationRole.END_CUSTOMER)
				&& customerForwardingGroupPayloads != null && !customerForwardingGroupPayloads.isEmpty()) {

			validateCustomerForwardingGroupPayloads(customerForwardingGroupPayloads, context);

			logger.info("Adding new customer forwarding group {} record(s)", customerForwardingGroupPayloads.size());

			Set<CustomerForwardingGroupMapper> groupMappers = customerForwardingGroupPayloads.stream()
					.filter(payload -> customerForwardingGroupMappers.stream().noneMatch(cfgm -> {
						return cfgm.getId().getCustomerForwardingGroupUuid()
								.equals(payload.getCustomerForwardingGroupUuid())
								&& cfgm.getId().getOrganisationUuid().equals(organisation.getUuid());
					})).map(payload -> {
						CustomerForwardingGroupMapper mapper = new CustomerForwardingGroupMapper();
						mapper.setId(new CustomerForwardingGroupMapperId(payload.getCustomerForwardingGroupUuid(),
								organisation.getUuid()));
						mapper.setCreatedOn(Instant.now());
						mapper.setCreatedBy(user);
						return mapper;
					}).collect(Collectors.toSet());

			if (!customerForwardingGroupMappers.isEmpty() && !groupMappers.isEmpty()) {
				customerForwardingGroupMapperRepository.deleteAll(customerForwardingGroupMappers);
			}

			if (!groupMappers.isEmpty()) {
				customerForwardingGroupMapperRepository.saveAll(groupMappers);
			}

			logger.info("Added successully new customer forwarding groups");
		} else {
			if (!customerForwardingGroupMappers.isEmpty()) {
				logger.info("Deleting existing customer forwarding group {} record(s)",
						customerForwardingGroupMappers.size());
				customerForwardingGroupMapperRepository.deleteAll(customerForwardingGroupMappers);
			}
		}
		Logutils.log(className, "saveCustomerForwardingGroup", context.getLogUUId(),
				" Successfully save customer forwarding groups ", logger);
	}

	private void validateCustomerForwardingGroupPayloads(
			List<CustomerForwardingGroupMapperPayload> customerForwardingGroupPayloads, Context context) {

		Logutils.log(className, "validateCustomerForwardingGroupPayloads", context.getLogUUId(),
				" Inside the validateCustomerForwardingGroupPayloads method from OrganisationServiceImpl ", logger);

		if (customerForwardingGroupPayloads != null && !customerForwardingGroupPayloads.isEmpty()) {

			customerForwardingGroupPayloads.forEach(payload -> {
				if (payload.getCustomerForwardingGroupUuid() == null
						|| payload.getCustomerForwardingGroupUuid().isEmpty()) {
					logger.info("Customer forwarding group uuid {} is null or empty string",
							payload.getCustomerForwardingGroupUuid());
					throw new BaseMessageException(new Message("Customer forwarding group uuid is required"));
				}
				if (!customerForwardingGroupRepository.existByUuid(payload.getCustomerForwardingGroupUuid())) {
					logger.info("Customer forwarding group uuid {} is not exist",
							payload.getCustomerForwardingGroupUuid());
					throw new BaseMessageException(new Message(String.format(
							"Customer forwarding group (%s) is not exist", payload.getCustomerForwardingGroupUuid())));
				}
			});
		}

		Logutils.log(className, "validateCustomerForwardingGroupPayloads", context.getLogUUId(),
				" Successfully validated customer forwarding group payloads ", logger);
	}

	private void saveCustomerForwardingRules(List<CustomerForwardingRulePayload> customerForwardingRulePayloads,
			Organisation organisation, User user, Context context) {

		Logutils.log(className, "saveCustomerForwardingRules", context.getLogUUId(),
				" Inside the saveCustomerForwardingRules method from OrganisationServiceImpl ", logger);

		Map<String, CustomerForwardingRuleUrl> customerForwardingRuleUrlMap = getCustomerForwardingRuleUrlMap(
				customerForwardingRulePayloads);

		if (organisation.getOrganisationRole().contains(OrganisationRole.END_CUSTOMER)
				&& customerForwardingRulePayloads != null && !customerForwardingRulePayloads.isEmpty()) {

			logger.info("Inside add, edit and delete customer forwarding rules block");

			validateCustomerForwardingRulePayloads(customerForwardingRulePayloads, context);

			Set<CustomerForwardingRule> newCustomerForwardingRules = new HashSet<>();
			Set<CustomerForwardingRule> deletedCustomerForwardingRules = new HashSet<>();

			customerForwardingRuleRepository.findByOrganisationUuid(organisation.getUuid()).forEach(rule -> {
				Optional<CustomerForwardingRulePayload> optional = getCustomerForwardingRulePayloadByUuid(
						customerForwardingRulePayloads, rule);
				if (optional.isPresent()) {
					newCustomerForwardingRules.add(beanConvertor.convert(rule, customerForwardingRuleUrlMap,
							optional.get(), organisation, user));
				} else {
					deletedCustomerForwardingRules.add(rule);
				}
			});

			Set<String> ids = newCustomerForwardingRules.stream().map(r -> r.getUuid()).collect(Collectors.toSet());
			customerForwardingRulePayloads.removeIf(payload -> ids.contains(payload.getUuid()));

			newCustomerForwardingRules.addAll(convetCustomerForwardingRulePayloadsToEntities(
					customerForwardingRulePayloads, customerForwardingRuleUrlMap, organisation, user));

			logger.info("Deleting customer forwarding rules count is {}", deletedCustomerForwardingRules.size());
			logger.info("Adding new customer forwarding rules count is {}", newCustomerForwardingRules.size());

			deleteCustomerForwardingRules(deletedCustomerForwardingRules);
			saveCustomerForwardingRules(newCustomerForwardingRules);

			saveCustomerForwardingRulesOnRedis(newCustomerForwardingRules, organisation, context);

			logger.info("Successfully update customer forwarding rules");

		} else {
			logger.info("Deleteing all customer forwarding rules");
			List<CustomerForwardingRule> customerForwardingRules = customerForwardingRuleRepository
					.findByOrganisationUuid(organisation.getUuid());
			deleteCustomerForwardingRules(new HashSet<>(customerForwardingRules));
			saveCustomerForwardingRulesOnRedis(new HashSet<>(), organisation, context);
			logger.info("Deleted all customer forwarding rules");
		}

		Logutils.log(className, "saveCustomerForwardingRules", context.getLogUUId(),
				" Successfully save customer forwarding rules ", logger);
	}

	private Map<String, CustomerForwardingRuleUrl> getCustomerForwardingRuleUrlMap(
			List<CustomerForwardingRulePayload> customerForwardingRulePayloads) {
		Map<String, CustomerForwardingRuleUrl> customerForwardingRuleUrlMap = new HashMap<>();

		Set<String> forwardingRuleUrlUuids = customerForwardingRulePayloads.parallelStream()
				.map(p -> p.getForwardingRuleUrlUuid()).collect(Collectors.toSet());

		if (!forwardingRuleUrlUuids.isEmpty()) {
			Set<CustomerForwardingRuleUrl> customerForwardingRuleUrls = customerForwardingRuleUrlRepository
					.findByUuidsIn(forwardingRuleUrlUuids);
			if (customerForwardingRuleUrls.size() != forwardingRuleUrlUuids.size()) {
				throw new BaseMessageException(new Message("Customer forwarding valid url is not exist"));
			}
			customerForwardingRuleUrls.forEach(customerForwardingRuleUrl -> {
				customerForwardingRuleUrlMap.put(customerForwardingRuleUrl.getUuid(), customerForwardingRuleUrl);
			});
		}
		return customerForwardingRuleUrlMap;
	}

	private void saveCustomerForwardingRulesOnRedis(Set<CustomerForwardingRule> newCustomerForwardingRules,
			Organisation organisation, Context context) {
		Logutils.log(className, "saveCustomerForwardingRulesOnRedis", context.getLogUUId(),
				" Inside the saveCustomerForwardingRulesOnRedis method from OrganisationServiceImpl ", logger);

		List<String> urls = new ArrayList<>();
		List<String> types = new ArrayList<>();
		List<String> kafkaTopicNames = new ArrayList<>();

		for (CustomerForwardingRule customerForwardingRule : newCustomerForwardingRules) {
			types.add(customerForwardingRule.getType());
			urls.add(customerForwardingRule.getUrl());
			kafkaTopicNames.add(customerForwardingRule.getForwardingRuleUrl().getKafkaTopicName());
		}

		redisService.hdel(CustomerPrefix.getForwardingRule, organisation.getAccountNumber(), CFR_FIELD_URLS);
		redisService.hdel(CustomerPrefix.getForwardingRule, organisation.getAccountNumber(), CFR_FIELD_TYPES);
		redisService.hdel(CustomerPrefix.getForwardingRule, organisation.getAccountNumber(),
				CFR_FIELD_KAFKA_TOPIC_NMAES);

		if (!urls.isEmpty()) {
			redisService.hset(CustomerPrefix.getForwardingRule, organisation.getAccountNumber(), CFR_FIELD_URLS,
					JSON.toJSONString(urls));
		}

		if (!types.isEmpty()) {
			redisService.hset(CustomerPrefix.getForwardingRule, organisation.getAccountNumber(), CFR_FIELD_TYPES,
					JSON.toJSONString(types));
		}

		if (!kafkaTopicNames.isEmpty()) {
			redisService.hset(CustomerPrefix.getForwardingRule, organisation.getAccountNumber(),
					CFR_FIELD_KAFKA_TOPIC_NMAES, JSON.toJSONString(kafkaTopicNames));
		}

		putDeviceidsWithAccountNumberInRedis(organisation, context);

		logger.info("Customer forwarding rule urls {} record(s) save successfully in redis db", urls.size());
		logger.info("Customer forwarding rule types {} record(s) save successfully in redis db", types.size());

		Logutils.log(className, "saveCustomerForwardingRulesOnRedis", context.getLogUUId(),
				" Successfully save customer forwarding rules on redis ", logger);

	}

	private void putDeviceidsWithAccountNumberInRedis(Organisation organisation, Context context) {
		Logutils.log(className, "putDeviceidsWithAccountNumberInRedis", context.getLogUUId(),
				" Inside the putDeviceidsWithAccountNumberInRedis method from OrganisationServiceImpl ", logger);

		Set<String> deviceIds = restUtils.getImeisByAccountNumber(organisation.getAccountNumber());

		deviceIds.removeIf(id -> id == null);

		if (!deviceIds.isEmpty()) {
			Set<String> deviceKeys = redisService.getKeys(CustomerPrefix.getDevice.getPrefix() + "*");

			if (deviceKeys != null) {
				Optional<String> idOptional = deviceIds.parallelStream()
						.filter(id -> id != null && deviceKeys.contains(CustomerPrefix.getDevice.getPrefix() + id))
						.findAny();
				boolean isEditedAccountNumber = false;
				if (idOptional.isPresent()) {
					String accountNumber = redisService.hget(CustomerPrefix.getDevice, idOptional.get(),
							CFR_FIELD_ACCOUNT_NUMBER);
					if (accountNumber != null && !accountNumber.equals(organisation.getAccountNumber())) {
						isEditedAccountNumber = true;
					}
				}

				if (!isEditedAccountNumber) {
					deviceIds.removeIf(id -> deviceKeys.contains(CustomerPrefix.getDevice.getPrefix() + id));
				}
			}
		}

		for (String deviceId : deviceIds) {
			redisService.hset(CustomerPrefix.getDevice, deviceId, CFR_FIELD_ACCOUNT_NUMBER,
					organisation.getAccountNumber());
		}
		Logutils.log(className, "putDeviceidsWithAccountNumberInRedis", context.getLogUUId(),
				" Successfully put device ids with account number in redis ", logger);
	}

	private Optional<CustomerForwardingRulePayload> getCustomerForwardingRulePayloadByUuid(
			List<CustomerForwardingRulePayload> customerForwardingRulePayloads, CustomerForwardingRule rule) {
		return customerForwardingRulePayloads.parallelStream()
				.filter(payload -> rule.getUuid().equals(payload.getUuid())).findFirst();
	}

	private void saveCustomerForwardingRules(Set<CustomerForwardingRule> newCustomerForwardingRules) {
		if (!newCustomerForwardingRules.isEmpty()) {
			customerForwardingRuleRepository.saveAll(newCustomerForwardingRules);
		}
		logger.info("Saved customer forwarding rules count is {}", newCustomerForwardingRules.size());
	}

	private Set<CustomerForwardingRule> convetCustomerForwardingRulePayloadsToEntities(
			List<CustomerForwardingRulePayload> customerForwardingRulePayloads,
			Map<String, CustomerForwardingRuleUrl> customerForwardingRuleUrlMap, Organisation organisation, User user) {
		Set<CustomerForwardingRule> newCustomerForwardingRules = new HashSet<>();
		customerForwardingRulePayloads.forEach(payload -> {
			newCustomerForwardingRules
					.add(beanConvertor.convert(null, customerForwardingRuleUrlMap, payload, organisation, user));
		});
		return newCustomerForwardingRules;
	}

	private void deleteCustomerForwardingRules(Set<CustomerForwardingRule> customerForwardingRules) {
		if (!customerForwardingRules.isEmpty()) {
			customerForwardingRuleRepository.deleteAll(customerForwardingRules);
		}
		logger.info("Deleted customer forwarding rules count is {}", customerForwardingRules.size());
	}

	private void validateCustomerForwardingRulePayloads(
			List<CustomerForwardingRulePayload> customerForwardingRulePayloads, Context context) {

		Logutils.log(className, "validateCustomerForwardingRulePayloads", context.getLogUUId(),
				" Inside the validateCustomerForwardingRulePayloads method from OrganisationServiceImpl ", logger);

		if (customerForwardingRulePayloads != null) {

			if (customerForwardingRulePayloads.size() > 5) {
				logger.info("Max five customer forwarding rules is allowed");
				throw new BaseMessageException(new Message("Max five customer forwarding rules is allowed"));
			}

			customerForwardingRulePayloads.forEach(payload -> {
				if (payload.getType() == null || payload.getType().isEmpty()) {
					logger.info("Customer forwarding rule type is required");
					throw new BaseMessageException(new Message("Customer forwarding rule type is required"));
				}
//				if (payload.getUrl() == null || payload.getUrl().isEmpty()) {
//					logger.info("Customer forwarding rule url is required");
//					throw new BaseMessageException(new Message("Customer forwarding rule url is required"));
//				}
				if (payload.getForwardingRuleUrlUuid() == null || payload.getForwardingRuleUrlUuid().isEmpty()) {
					logger.info("Customer forwarding rule url is required");
					throw new BaseMessageException(new Message("Customer forwarding rule url is required"));
				}
			});
		}

		Logutils.log(className, "validateCustomerForwardingRulePayloads", context.getLogUUId(),
				" Successfully validated customer forwarding rules payloads ", logger);
	}

	public void addOrganisationSettings(String organisationUuid, AddOrganisationPayload addOrganisationPayload)
			throws InstantiationException {
		OrganisationSection organisationSection = organisationSectionRepository
				.findOrganisationSectionByDisplayName("Firmware Updates");
		if (organisationSection == null) {
			throw new InstantiationException("Organisation Section not available");
		}
		List<OrganisationSettings> organisationSettingsList = new ArrayList<>();
		organisationSettingsList.add(prepareOrganisationSection(organisationSection.getUuid(), organisationUuid,
				"isApprovalReqForDeviceUpdate",
				String.valueOf(addOrganisationPayload.getIsApprovalReqForDeviceUpdate()), -1));
		organisationSettingsList.add(prepareOrganisationSection(organisationSection.getUuid(), organisationUuid,
				"isTestReqBeforeDeviceUpdate", String.valueOf(addOrganisationPayload.getIsTestReqBeforeDeviceUpdate()),
				-1));
		organisationSettingsList.add(prepareOrganisationSection(organisationSection.getUuid(), organisationUuid,
				"isMonthlyReleaseNotesReq", String.valueOf(addOrganisationPayload.getIsMonthlyReleaseNotesReq()), -1));
		organisationSettingsList.add(prepareOrganisationSection(organisationSection.getUuid(), organisationUuid,
				"isDigitalSignReqForFirmware", String.valueOf(addOrganisationPayload.getIsDigitalSignReqForFirmware()),
				-1));
		organisationSettingsList.add(prepareOrganisationSection(organisationSection.getUuid(), organisationUuid,
				"noOfDevice", "", addOrganisationPayload.getNoOfDevice()));
		organisationSettingsRepository.saveAll(organisationSettingsList);
	}

	public OrganisationSettings prepareOrganisationSection(String sectionUuid, String organisationUuid,
			String fieldName, String fieldValues, int deviceValue) {
		OrganisationSettings organisationSettings = new OrganisationSettings();
		String organisationSettingUuid = "";
		boolean isOrganisationUuidUnique = false;
		while (!isOrganisationUuidUnique) {
			organisationSettingUuid = UUID.randomUUID().toString();
			OrganisationSettings byUuid = organisationSettingsRepository
					.findOrganisationSettingsByUuid(organisationSettingUuid);
			if (byUuid == null) {
				isOrganisationUuidUnique = true;
			}
		}
		organisationSettings.setUuid(organisationSettingUuid);
		organisationSettings.setOrganisationSectionUuid(sectionUuid);
		organisationSettings.setOrganisationUuid(organisationUuid);
		if (deviceValue == -1) {
			organisationSettings.setDataType("Boolean");
			organisationSettings.setFieldName(fieldName);
			organisationSettings.setFieldValue(fieldValues);
		} else {
			organisationSettings.setDataType("Number");
			organisationSettings.setFieldName(fieldName);
			organisationSettings.setFieldValue(String.valueOf(deviceValue));
		}
		return organisationSettings;

	}

	@Override
	public AddOrganisationPayload update(AddOrganisationPayload addOrganisationPayload, Context context)
			throws OrganisationException {
		logger.info("Inside update Method and fetch addOrganisationPayload", addOrganisationPayload);
		String methodName = "update";

		logger.debug("Inside try block of update Method From OrganisationServiceImpl ");
		Logutils.log(className, methodName, context.getLogUUId(),
				" Before calling update method from OrganisationServiceImpl ", logger);

		if (addOrganisationPayload.getAccountNumber() == null) {
			throw new BadRequestException(new Message("Invalid Request"));
		}
		logger.info("Fetching organisation details based on accountnumber");
		Organisation organisation = organisationRepository
				.findBySalesForceAccountNumber(addOrganisationPayload.getAccountNumber());
		if (organisation == null) {
			throw new OrganisationException("No Data Found to Update");
		}
//			Organisation addOrganisationPayloadToOrganisation = beanConvertor
//					.AddOrganisationPayloadToOrganisation(addOrganisationPayload);
		organisation.setOrganisationName(addOrganisationPayload.getOrganisationName());
		organisation.setShortName(addOrganisationPayload.getShortName());
		organisation.setIsActive(addOrganisationPayload.getStatus());
		Organisation save = organisationRepository.save(organisation);
		AddOrganisationPayload organisationToAddOrganisationPayload = beanConvertor
				.OrganisationToAddOrganisationPayload(save);
		logger.info("Exiting from update Method of OrganisationServiceImpl");
		return organisationToAddOrganisationPayload;

	}

	@Override
	@Transactional
	public void deleteByUuid(String uuid, Context context) throws OrganisationException {
		logger.info("Inside deleteByUuid service for organisation uuid " + uuid);
		Organisation organisation = organisationRepository.findByUuid(uuid);
		if (organisation == null) {
			throw new OrganisationException("No Organisation found for the uuid.");
		}
		organisation.setIsActive(false);

		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		String username = userDetails.getUsername();
		User user = restUtils.getUserFromAuthService(username);
		organisation.setUpdatedBy(user);
		organisation.setUpdatedAt(Instant.now());
		organisationRepository.save(organisation);
		logger.info("Organisation is deleted successfully");
	}

	@Override
	public List<Organisation> getAllOrganisation(Context context) {

		logger.info("Inside getAllOrganisation Method");
		String methodName = "getAllOrganisation";
		Logutils.log(className, methodName, context.getLogUUId(),
				" Before calling getOrganisationByType method from CustomerService ", logger);

		List<Organisation> organisations = organisationRepository.getAllOrganisation();
//		
//		List<Organisation> filteredOrgList = organisations.stream()
//				.filter(m -> (m.getType() != null && m.getType().toString().equalsIgnoreCase("CUSTOMER")))
//				.collect(Collectors.toList());

		return organisations;
	}

	
	@Override
	public Page<AddOrganisationResponse> findAllOrganisatons(Pageable pageable, OrganisationRole type,
			String accountNumber, String uuid, Context context) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OrganisationDTO getOrganisationById(Long id) {
		logger.info("Inside getCompanyByAccountNumber service for account id: " + id);
		Organisation organisation = organisationRepository.findById(id).get();
		List<OrganisationSettings> organisationSettingsList = null;
		if (organisation.getOrganisationRole().contains(OrganisationRole.END_CUSTOMER)) {
			organisationSettingsList = organisationSettingsRepository
					.findOrganisationSettingsByOrganisationUuid(organisation.getUuid());
		}
		OrganisationDTO organisationDTO = beanConvertor.convertOrganisationToOrganisationDTO(organisation,
				organisationSettingsList);
		// logger.info("Fetching organisation details ", organisation);
		return organisationDTO;
	}

	@Override
	public List<Organisation> getListOfCompany(String userName) {
		logger.info("Inside getListOfCompany service ");
		User user = restUtils.getUserFromAuthService(userName);
		boolean roleAvailable = false;
		if (user != null) {
			for (Role roles : user.getRole()) {
				if (roles.getName().contains(AuthoritiesConstants.ROLE_CUSTOMER_ADMIN)
						|| roles.getName().contains(AuthoritiesConstants.ROLE_ORGANIZATION_USER)) {
					roleAvailable = true;
					break;
				}
			}
		}
		List<Organisation> companies = new ArrayList<>();
		;
		if (roleAvailable && user.getOrganisation() != null) {
			companies = organisationRepository
					.findByOrganisationByOrganisationName(user.getOrganisation().getOrganisationName());
		} else if (!roleAvailable) {
			companies = organisationRepository.findAll();
		}

		logger.info("Fetching organisation details successfully done");
		return beanConvertor.organisationsToOrganisationsConvertor(companies);
	}
	
	
	@Override
	public OrganisationDTO getOrganisationByName(String name, Context context) {
		String methodName = "getOrganisationByName";

		logger.info("Inside getOrganisationByName service with name: {}", name);

		Logutils.log(className, methodName, context.getLogUUId(),
				" Before calling getAllOrganisationByName method from OrganisationServiceImpl ", logger);

		List<Organisation> organisations = organisationRepository
				.getAllOrganisationByName(Collections.singletonList(name));

		Logutils.log(className, methodName, context.getLogUUId(),
				" After calling getAllOrganisationByName method from OrganisationServiceImpl ", logger);

		OrganisationDTO organisationDTO = null;

		if (!organisations.isEmpty()) {
			organisationDTO = beanConvertor.convertOrganisationToOrganisationDTO(organisations.get(0), null);
		}

		logger.info("Fetching organisation details {}", organisationDTO);
		return organisationDTO;
	}

	public List<String> getAllCompanyName() {
		logger.info("Inside getListOfCompany service ");
		List<Organisation> companies = organisationRepository.findAll();
		List<String> organisationList = companies.stream().map(e -> e.getOrganisationName())
				.collect(Collectors.toList());
		logger.info("Fetching organisation details " + companies);
		return organisationList;
	}

	@Override
	public OrganisationDTO getOrganisationByAccountNumber(String id) {
		logger.info("Inside getCompanyByAccountNumber service for account id: " + id);
		Organisation organisation = organisationRepository.findBySalesForceAccountNumber(id);
		List<OrganisationSettings> organisationSettingsList = null;
		if (organisation.getOrganisationRole().contains("END_CUSTOMER")) {
			organisationSettingsList = organisationSettingsRepository
					.findOrganisationSettingsByOrganisationUuid(organisation.getUuid());
		}
		OrganisationDTO organisationDTO = beanConvertor.convertOrganisationToOrganisationDTO(organisation,
				organisationSettingsList);
		logger.info("Fetching organisation details ", organisation);
		return organisationDTO;
	}

//	@Override
//	public Boolean saveOrganisationDetail(AddOrganisationPayload addOrganisationPayload, Context context) throws InstantiationException {
//	
//		logger.info("Inside SaveOrganisationDetail and fetch createOrganisationPayload", addOrganisationPayload);
//		String methodName = "SaveOrganisationDetail";
//
//		Logutils.log(className, methodName, context.getLogUUId(),
//				" Before calling SaveOrganisationDetail Method From OrganisationServiceImpl Service method in OrganisationServiceImpl",
//				logger);
//		boolean check = false;
//		Organisation organisation = new Organisation();
//		
//		if (addOrganisationPayload.getOrganisationRole().contains(OrganisationRole.INSTALLER))
//			{
//				organisation.setOrganisationName(addOrganisationPayload.getOrganisationName());
//				organisation.setIsActive(addOrganisationPayload.getStatus());
//				organisation.setEpicorAccountNumber(addOrganisationPayload.getEpicorAccountNumber());
//				organisation.setSalesforceAccountNumber(addOrganisationPayload.getSalesforceAccountNumber());
//				organisation.setOrganisationRole(addOrganisationPayload.getOrganisationRole());
//				organisation.setIsAssetListRequired(addOrganisationPayload.getIsAssetListRequired());
//			}
//			else if (addOrganisationPayload.getOrganisationRole().contains(OrganisationRole.END_CUSTOMER)) 
//				{
//					organisation.setOrganisationName(addOrganisationPayload.getOrganisationName());
//					organisation.setIsActive(addOrganisationPayload.getStatus());
//					organisation.setEpicorAccountNumber(addOrganisationPayload.getEpicorAccountNumber());
//					organisation.setSalesforceAccountNumber(addOrganisationPayload.getSalesforceAccountNumber());
//					organisation.setOrganisationRole(addOrganisationPayload.getOrganisationRole());
//					organisation.setIsAssetListRequired(addOrganisationPayload.getIsAssetListRequired());
//					if(addOrganisationPayload.getResellerList()!=null && !addOrganisationPayload.getResellerList().isEmpty()) {					
//						organisation.setResellerList(beanConvertor.convertOrganisationCompanyIdPayloadIntoOrganisatioln(addOrganisationPayload.getResellerList()));
//					
//					}
//					if(addOrganisationPayload.getInstallerList()!=null && !addOrganisationPayload.getInstallerList().isEmpty()) {					
//						organisation.setInstallerList(beanConvertor.convertOrganisationCompanyIdPayloadIntoOrganisatioln(addOrganisationPayload.getInstallerList()));
//					}
//					
//				}
//				
//				else if (organisation.getOrganisationRole().contains(OrganisationRole.RESELLER)) 
//					{
//						organisation.setOrganisationName(addOrganisationPayload.getOrganisationName());
//						organisation.setIsActive(addOrganisationPayload.getStatus());
//						organisation.setEpicorAccountNumber(addOrganisationPayload.getEpicorAccountNumber());
//						organisation.setSalesforceAccountNumber(addOrganisationPayload.getSalesforceAccountNumber());
//						organisation.setOrganisationRole(addOrganisationPayload.getOrganisationRole());
//						organisation.setIsAssetListRequired(addOrganisationPayload.getIsAssetListRequired());
//										
//					}
//					else {
//						throw new InstantiationException("Organisation payload/account number can't be null");
//					}
//		
//		
//		
//		if (organisation != null ) {
//			 Organisation save = organisationRepository.save(organisation);
//			 check=true;
//		}
//		       		  
//		      return Boolean.TRUE;
//			}      	
//	
	@Override
	public List<OrganisationDTO> getAllActiveByOrganisationRoles(List<String> organisationRoles, Context context) {
		String methodName = "getAllActiveByOrganisationRole";

		Logutils.log(className, methodName, context.getLogUUId(),
				" Inside the getAllActiveByOrganisationRole method from OrganisationServiceImpl ", logger);

		Set<OrganisationRole> organisationRoleEnums = new HashSet<>();

		for (String orgRole : organisationRoles) {
			OrganisationRole organisationRoleEnum = OrganisationRole.getOrganisationRole(orgRole);
			if (organisationRoleEnum == null) {
				throw new BadRequestException("Invalid organisation role");
			}
			organisationRoleEnums.add(organisationRoleEnum);
		}
		if (organisationRoleEnums.isEmpty()) {
			throw new BadRequestException("Atleast one organization role required");
		}
		Logutils.log(className, methodName, context.getLogUUId(),
				" Before calling getAllActiveByOrganisationRole method from OrganisationRepository ", logger);

		List<Organisation> organisations = organisationRepository
				.getAllActiveByOrganisationRolesIn(organisationRoleEnums);

		Logutils.log(className, methodName, context.getLogUUId(),
				" After calling getAllActiveByOrganisationRole method from OrganisationRepository ", logger);

		List<OrganisationDTO> organisationDTOs = new ArrayList<>();

		for (Organisation organisation : organisations) {
			organisationDTOs.add(beanConvertor.convertOrganisationToOrganisationDTO(organisation, null));
		}

		Logutils.log(className, methodName, context.getLogUUId(),
				" Exiting getAllActiveByOrganisationRole method from OrganisationServiceImpl ", logger);

		return organisationDTOs;
	}

	@Override
	public Map<String, Integer> migrateOrganisationRole(Context context) {

		String methodName = "migrateOrganisationRole";

		Logutils.log(className, methodName, context.getLogUUId(),
				" Inside the migrateOrganisationRole method from OrganisationServiceImpl ", logger);

		List<Organisation> organisations = organisationRepository.findAll();

		logger.info("Total organisation record : {}", organisations.size());

		int invalidRoleMigratedCount = 0;
		int roleMigratedSuccessCount = 0;
		int epicorAccountNumberSuccessCount = 0;
		int totalAccountNumberSuccessRecordCount = 0;

		List<Organisation> updateOrganisations = new ArrayList<>();

		for (Organisation organisation : organisations) {

			logger.info("Organisation id: {}, Type : {} ", organisation.getId());

			boolean isRequiredUpdate = true;

			if (!organisation.getOrganisationRole().isEmpty()) {
				invalidRoleMigratedCount++;
				isRequiredUpdate = false;
			}

			if (isRequiredUpdate) {
				Set<OrganisationRole> organisationRoles = getOrganisationRoles(organisation.getOrganisationRole());

				logger.info("Organisation id: {}, Type : {}, Role: {} ", organisation.getId(), organisationRoles);

				organisation.setOrganisationRole(organisationRoles);

				roleMigratedSuccessCount++;
			}

			boolean isAccountNumberUpdate = false;

			if (StringUtils.hasText(organisation.getEpicorAccountNumber())) {
				organisation.setEpicorAccountNumber(organisation.getEpicorAccountNumber());
				isRequiredUpdate = true;
				isAccountNumberUpdate = true;
				epicorAccountNumberSuccessCount++;
			}

			if (isAccountNumberUpdate) {
				totalAccountNumberSuccessRecordCount++;
			}

			if (isRequiredUpdate) {
				updateOrganisations.add(organisation);
			}
		}

		if (!updateOrganisations.isEmpty()) {
			organisationRepository.saveAll(updateOrganisations);
		}

		Map<String, Integer> resultMap = getMigrateResultMap(organisations.size(), invalidRoleMigratedCount,
				roleMigratedSuccessCount, epicorAccountNumberSuccessCount, totalAccountNumberSuccessRecordCount,
				updateOrganisations);

		logger.info("Migrate Result : {}", resultMap);

		Logutils.log(className, methodName, context.getLogUUId(),
				" Ended the migrateOrganisationRole method from OrganisationServiceImpl ", logger);

		return resultMap;
	}

	private Map<String, Integer> getMigrateResultMap(int totalRecord, int invalidRoleMigratedCount,
			int roleMigratedSuccessCount, int epicorAccountNumberSuccessCount, int totalAccountNumberSuccessRecordCount,
			List<Organisation> updateOrganisations) {
		Map<String, Integer> resultMap = new HashMap<>();
		resultMap.put("totalRecord", totalRecord);
		resultMap.put("invalidRoleMigratedRecord", invalidRoleMigratedCount);
		resultMap.put("roleMigratedSuccessRecord", roleMigratedSuccessCount);
		resultMap.put("totalSuccessRecord", updateOrganisations.size());
		resultMap.put("totalAccountNumberSuccessRecord", totalAccountNumberSuccessRecordCount);
		resultMap.put("epicorAccountNumberSuccessRecord", epicorAccountNumberSuccessCount);
		return resultMap;
	}

	private Set<OrganisationRole> getOrganisationRoles(Set<OrganisationRole> organisationTypeValue) {
		OrganisationRole organisationRole = OrganisationRole.MANUFACTURER;
		for (OrganisationRole organisationRoleValue : organisationTypeValue) {
			String organisationType = organisationRoleValue.getValue();
			if (organisationType.equals(OrganisationRole.END_CUSTOMER)) {
				organisationRole = OrganisationRole.END_CUSTOMER;
			}

			if (organisationType.equals(OrganisationRole.INSTALLER)) {
				organisationRole = OrganisationRole.INSTALLER;
			}

			if (organisationType.equals(OrganisationRole.RESELLER)) {
				organisationRole = OrganisationRole.RESELLER;
			}
		}

		Set<OrganisationRole> organisationRoles = new HashSet<>();
		organisationRoles.add(organisationRole);

		return organisationRoles;
	}

	public Page<OrganisationAccessDTO> getAllActiveOrganisation(Pageable pageable, String type, Boolean status,
			Long userId, Context context) {
		Page<Organisation> organisation = null;
		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		User user = restUtils.getUserFromAuthService(userDetails.getUsername());
		// User user = restUtils.getUserFromAuthService(userId);
		List<String> role = user.getRole().stream().map(Role::getRoleName).collect(Collectors.toList());
		List<OrganisationRole> organisationTypeList = new ArrayList<>();
		if (type.equalsIgnoreCase("ALL")) {
			organisationTypeList.add(OrganisationRole.MANUFACTURER);
			organisationTypeList.add(OrganisationRole.END_CUSTOMER);
			organisationTypeList.add(OrganisationRole.INSTALLER);
		} else {
			organisationTypeList.add(OrganisationRole.getOrganisationRole(type));
		}
		if (role.contains(AuthoritiesConstants.SUPER_ADMIN)) {
			organisation = organisationRepository.findAllCompanies(pageable, organisationTypeList);
		} else if (role.contains(AuthoritiesConstants.ROLE_INSTALLER)
				|| (role.contains(AuthoritiesConstants.ROLE_CUSTOMER_ADMIN)
						&& user.getOrganisation().getOrganisationRole().contains(OrganisationRole.INSTALLER))) {
			List<Long> companyIds = new ArrayList<>();
			if (user.getOrganisation() != null && user.getOrganisation().getAccessList() != null
					&& user.getOrganisation().getAccessList().size() > 0) {
				for (int i = 0; i < user.getOrganisation().getAccessList().size(); i++) {
					companyIds.add(user.getOrganisation().getAccessList().get(i).getId());
				}
			}
			organisation = organisationRepository.findNonCustomerCompanyByIds(pageable, organisationTypeList,
					companyIds);
		} else {
			organisation = organisationRepository.findNonCustomerCompanyById(pageable, organisationTypeList,
					user.getOrganisation().getId());
		}
		List<OrganisationAccessDTO> organisationPayloadList = new ArrayList<>();
		organisation.forEach(company -> {
			OrganisationPayload organisationPayload = beanConvertor.organisationToorganisationPayload(company);
			OrganisationAccessDTO organisationAccessDTO = new OrganisationAccessDTO();
			organisationAccessDTO.setCustomer(organisationPayload);
			//organisationAccessDTO.setType(company.getType().getValue());
			organisationAccessDTO.setStatus(company.getIsActive());
			organisationAccessDTO.setUuid(company.getUuid());
			List<OrganisationPayload> accessOrganisationPayloadList = new ArrayList<>();
			company.getAccessList().forEach(accessCompany -> {
				accessOrganisationPayloadList.add(beanConvertor.organisationToorganisationPayload(accessCompany));
			});
			organisationAccessDTO.setOrganisationViewList(accessOrganisationPayloadList);
			organisationPayloadList.add(organisationAccessDTO);
		});
		Page<OrganisationAccessDTO> pageOfCompanyPayload = new PageImpl<>(organisationPayloadList, pageable,
				organisation.getTotalElements());
		return pageOfCompanyPayload;
	}

	@Override
	public List<OrganisationRequest> getAllOrganisationFilter(Context context) {

		logger.info("Inside getAllOrganisation Method");
		String methodName = "getAllOrganisation";
		Logutils.log(className, methodName, context.getLogUUId(),
				" Before calling getOrganisationByType method from CustomerService ", logger);
		List<OrganisationRequest> allOrganisationFilter = organisationRepository.getAllOrganisationFilter();
		return allOrganisationFilter;
	}

	@Override
	public OrganisationAccess update(OrganisationAccess organisations, Context context) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<OrganisationListPayload> getAllCompany(String userName) {
		logger.info("Inside getListOfCompany service ");
		User user = restUtils.getUserFromAuthService(userName);
		boolean roleAvailable = false;
		if (user != null) {
			for (Role roles : user.getRole()) {
				if (roles.getName().contains(AuthoritiesConstants.ROLE_CUSTOMER_ADMIN)
						|| roles.getName().contains(AuthoritiesConstants.ROLE_ORGANIZATION_USER)) {
					roleAvailable = true;
					break;
				}
			}
		}
		List<Organisation> companies = new ArrayList<>();
		;
		if (roleAvailable && user.getOrganisation() != null) {
			companies = organisationRepository
					.findByOrganisationByOrganisationName(user.getOrganisation().getOrganisationName());
		} else if (!roleAvailable) {
			companies = organisationRepository.findAll();
		}

		logger.info("Fetching organisation details successfully done");
		return beanConvertor.organisationsToOrganisationListPayloadConvertor(companies);
	}

}
