package com.pct.organisation.util;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pct.common.constant.OrganisationRole;
import com.pct.common.dto.CustomerForwardingGroupDTO;
import com.pct.common.dto.CustomerForwardingRuleDTO;
import com.pct.common.dto.CustomerForwardingRuleUrlDTO;
import com.pct.common.dto.LocationDTO;
import com.pct.common.model.CustomerForwardingGroup;
import com.pct.common.model.CustomerForwardingRule;
import com.pct.common.model.CustomerForwardingRuleUrl;
import com.pct.common.model.Organisation;
import com.pct.common.model.OrganisationSection;
import com.pct.common.model.OrganisationSettings;
import com.pct.common.model.User;
import com.pct.common.payload.OrganisationIdPayload;
import com.pct.organisation.dto.CustomerWithLocation;
import com.pct.organisation.dto.Location;
import com.pct.organisation.dto.OrganisationDTO;
import com.pct.organisation.payload.AddOrganisationPayload;
import com.pct.organisation.payload.AddOrganisationResponse;
import com.pct.organisation.payload.CustomerForwardingRulePayload;
import com.pct.organisation.payload.CustomerLocationPayload;
import com.pct.organisation.payload.HubCustomer;
import com.pct.organisation.payload.LocationPayload;
import com.pct.organisation.payload.OrganisationListPayload;
import com.pct.organisation.payload.OrganisationPayload;
import com.pct.organisation.payload.OrganisationPayloadForCAN;
import com.pct.organisation.repository.IOrganisationRepository;
import com.pct.organisation.repository.IOrganisationSectionRepository;
import com.pct.organisation.repository.IOrganisationSettingsRepository;

@Component
public class BeanConvertor {

	private final String STATUS = "PENDING";
	private final ObjectMapper mapper = new ObjectMapper();

	@Autowired
	IOrganisationRepository organisationRepository;

	@Autowired
	private IOrganisationSectionRepository organisationSectionRepository;
	
	@Autowired
    private IOrganisationSettingsRepository companySettingsRepository;

	public OrganisationPayload hubOrganisationToOganisationPayload(HubCustomer hubCustomer) {
		OrganisationPayload organisationPayload = new OrganisationPayload();
		organisationPayload.setOrganisationName(hubCustomer.getAccountName());
		organisationPayload.setAccountNumber(hubCustomer.getSalesforceAccountId());
		return organisationPayload;
	}

	public AddOrganisationPayload OrganisationToAddOrganisationPayload(Organisation org) {
		AddOrganisationPayload payload = new AddOrganisationPayload();

		payload.setOrganisationName(org.getOrganisationName());
		payload.setShortName(org.getShortName());
		payload.setAccountNumber(org.getAccountNumber());
		payload.setStatus(org.getIsActive());

		return payload;
	}

	public AddOrganisationResponse OrganisationToAddOrganisationResponse(Organisation org) {
		AddOrganisationResponse payload = new AddOrganisationResponse();
		if (org.getId() != null) {
			payload.setId(org.getId());
		}
		payload.setOrganisationName(org.getOrganisationName());
		payload.setShortName(org.getShortName());
		payload.setAccountNumber(org.getAccountNumber());
		payload.setStatus(org.getIsActive());
		payload.setUuid(org.getUuid());
		payload.setEpicorId(org.getEpicorAccountNumber());
		payload.setPreviousRecordId(org.getPreviousRecordId());
		return payload;
	}

	public Organisation addOrganisationPayloadToOrganisation(AddOrganisationPayload payload) {
		Organisation org = new Organisation();

		org.setShortName(payload.getShortName());
		org.setOrganisationName(payload.getOrganisationName());
		org.setIsActive(payload.getStatus());

		org.setEpicorAccountNumber(payload.getEpicorAccountNumber());
		org.setAccountNumber(payload.getSalesforceAccountNumber());
		org.setOrganisationRole(payload.getOrganisationRole());
		org.setIsAssetListRequired(payload.getIsAssetListRequired());
		if (payload.getResellerList() != null && !payload.getResellerList().isEmpty()) {
			org.setResellerList(convertOrganisationCompanyIdPayloadIntoOrganisation(payload.getResellerList()));

		}
		if (payload.getInstallerList() != null && !payload.getInstallerList().isEmpty()) {
			org.setInstallerList(convertOrganisationCompanyIdPayloadIntoOrganisation(payload.getInstallerList()));
		}
		if(payload.getMaintenanceList()!=null && !payload.getMaintenanceList().isEmpty()) {					
			org.setMaintenanceList(convertOrganisationCompanyIdPayloadIntoOrganisation(payload.getMaintenanceList()));
		}
		if(payload.getAccessId()!=null) {
			String idList = payload.getAccessId().toString();
			List<Organisation> organisation = organisationRepository.getAllOrganisationByName(payload.getAccessId());
			org.setAccessList(organisation);
		}

		return org;
	}

	public List<AddOrganisationPayload> ListOrganisationToAddOrganisationPayload(List<Organisation> organisation) {
		List<AddOrganisationPayload> addOrganisationPayload = new ArrayList<AddOrganisationPayload>();
		organisation.forEach(org -> addOrganisationPayload.add(OrganisationToAddOrganisationPayload(org)));
		return addOrganisationPayload;
	}

	public Page<AddOrganisationResponse> ListOrganisationToAddOrganisationResponse(Page<Organisation> organisation) {
		Page<AddOrganisationResponse> addOrganisationResponse = Page.empty();
		organisation.forEach(org -> addOrganisationResponse.map(it -> OrganisationToAddOrganisationResponse(org)));
		return addOrganisationResponse;
	}

	public OrganisationPayload organisationToOrganisationPayload(Organisation org) {
		OrganisationPayload organisationPayload = new OrganisationPayload();
		organisationPayload.setOrganisationName(org.getOrganisationName());
		organisationPayload.setId(org.getId());
		organisationPayload.setStatus(org.getIsActive());
		organisationPayload.setShortName(org.getShortName());
		organisationPayload.setAccountNumber(org.getAccountNumber());
		organisationPayload.setIsAssetListRequired(org.getIsAssetListRequired());
		organisationPayload.setUuid(org.getUuid());
		organisationPayload.setPriviousRecordId(org.getPreviousRecordId());
		organisationPayload.setEpicorId(org.getEpicorAccountNumber());
		List<OrganisationPayload> list = new ArrayList<>();
		for (Organisation access : org.getAccessList()) {
			OrganisationPayload accessList = new OrganisationPayload();
			accessList.setOrganisationName(access.getOrganisationName());
			accessList.setId(access.getId());
			accessList.setStatus(access.getIsActive());
			accessList.setShortName(access.getShortName());
			accessList.setAccountNumber(access.getAccountNumber());
			accessList.setIsAssetListRequired(access.getIsAssetListRequired());
			accessList.setUuid(access.getUuid());
			accessList.setPriviousRecordId(access.getPreviousRecordId());
			accessList.setEpicorId(access.getEpicorAccountNumber());
			list.add(accessList);
		}
		organisationPayload.setAccessList(list);
		organisationPayload.setOrganisationRoles(
				org.getOrganisationRole().stream().map(orgRole -> orgRole.getValue()).collect(Collectors.toSet()));
		return organisationPayload;
	}

	public OrganisationPayloadForCAN organisationToOrganisationPayloadForCAN(Organisation org) {
		OrganisationPayloadForCAN organisationPayloadForCAN = new OrganisationPayloadForCAN();
		organisationPayloadForCAN.setOrganisationName(org.getOrganisationName());
		organisationPayloadForCAN.setId(org.getId());
		organisationPayloadForCAN.setStatus(org.getIsActive());
		// comp.setShortName(com.getShortName());
		organisationPayloadForCAN.setAccountNumber(org.getAccountNumber());
		organisationPayloadForCAN.setIsAssetListRequired(org.getIsAssetListRequired());
		organisationPayloadForCAN.setUuid(org.getUuid());
		return organisationPayloadForCAN;
	}

	public CustomerLocationPayload convertOrganisationToCustomerLocationPayload(Organisation organisation) {
		CustomerLocationPayload customerLocationPayload = new CustomerLocationPayload();
		customerLocationPayload.setAccount_number(organisation.getAccountNumber());
		customerLocationPayload.setCustomer_name(organisation.getOrganisationName());
		List<LocationPayload> locations = new ArrayList<LocationPayload>();
		LocationPayload firstLocation = new LocationPayload();
		firstLocation.setLocation_id(1l);
		firstLocation.setLocation_name("Depot 1, LA");
		LocationPayload secondLocation = new LocationPayload();
		secondLocation.setLocation_id(2l);
		secondLocation.setLocation_name("Depot 2, LA");
		locations.add(firstLocation);
		locations.add(secondLocation);
		customerLocationPayload.setLocations(locations);
		return customerLocationPayload;
	}

	public OrganisationDTO convertOrganisationToOrganisationDTO(Organisation organisation,
			List<OrganisationSettings> organisationSettingsList) {

		OrganisationDTO organisationDTO = new OrganisationDTO();
		organisationDTO.setId(organisation.getId());
		organisationDTO.setAccountNumber(organisation.getAccountNumber());
		//organisationDTO.setCreatedBy(organisation.getCreatedBy());
		organisationDTO.setEpicorId(organisation.getEpicorAccountNumber());
		organisationDTO.setIsActive(organisation.getIsActive());
		organisationDTO.setIsAssetListRequired(organisation.getIsAssetListRequired());
		organisationDTO.setOrganisationName(organisation.getOrganisationName());
		organisationDTO.setPreviousRecordId(organisation.getPreviousRecordId());
		organisationDTO.setShortName(organisation.getShortName());
		//organisationDTO.setUpdatedBy(organisation.getUpdatedBy());
		organisationDTO.setUuid(organisation.getUuid());
		organisationDTO.setEpicorAccountNumber(organisation.getEpicorAccountNumber());
		organisationDTO.setSalesforceAccountNumber(organisation.getAccountNumber());
		organisationDTO.setOrganisationRole(organisation.getOrganisationRole());
		List<String> accesId = new ArrayList<>();
		for (Organisation organisation2 : organisation.getAccessList()) {
			accesId.add(organisation2.getOrganisationName());
		}
		organisationDTO.setAccessId(accesId);
		organisationDTO.setInstallerList(convertOrganisationToOrganisationIdPayload(organisation.getInstallerList()));
		organisationDTO.setResellerList(convertOrganisationToOrganisationIdPayload(organisation.getResellerList()));
		organisationDTO.setMaintenanceList(convertOrganisationToOrganisationIdPayload(organisation.getMaintenanceList()));
		if (organisationSettingsList != null && organisationSettingsList.size() > 0) {
			for (OrganisationSettings organisationSettings : organisationSettingsList) {
				switch (organisationSettings.getFieldName()) {
				case "isApprovalReqForDeviceUpdate":
					organisationDTO.setIsApprovalReqForDeviceUpdate(organisationSettings.getFieldValue() != null
							? Boolean.parseBoolean(organisationSettings.getFieldValue())
							: false);
					break;
				case "isTestReqBeforeDeviceUpdate":
					organisationDTO.setIsTestReqBeforeDeviceUpdate(organisationSettings.getFieldValue() != null
							? Boolean.parseBoolean(organisationSettings.getFieldValue())
							: false);

					break;
				case "isMonthlyReleaseNotesReq":
					organisationDTO.setIsMonthlyReleaseNotesReq(organisationSettings.getFieldValue() != null
							? Boolean.parseBoolean(organisationSettings.getFieldValue())
							: false);
					break;
				case "isDigitalSignReqForFirmware":
					organisationDTO.setIsDigitalSignReqForFirmware(organisationSettings.getFieldValue() != null
							? Boolean.parseBoolean(organisationSettings.getFieldValue())
							: false);
					break;
				case "noOfDevice":
					organisationDTO.setNoOfDevice(organisationSettings.getFieldValue() != null
							? Integer.parseInt(organisationSettings.getFieldValue())
							: 0);
					break;

				default:
					break;
				}
			}
		}
		return organisationDTO;
	}

	private List<OrganisationIdPayload> convertOrganisationToOrganisationIdPayload(List<Organisation> installerList) {
		List<OrganisationIdPayload> organisationIdPayloads = new ArrayList<>();
		if (installerList != null) {
			for (Organisation organisation : installerList) {
				OrganisationIdPayload organisationIdPayload = new OrganisationIdPayload();
				organisationIdPayload.setCompanyId(organisation.getId());
				organisationIdPayloads.add(organisationIdPayload);
			}
		}
		return organisationIdPayloads;
	}
	
	private List<Organisation> convertOrganisationToOrganisationId(List<Organisation> installerList) {
		List<Organisation> organisationList = new ArrayList<>();
		if (installerList != null) {
			for (Organisation organisation : installerList) {
				Organisation organisationIdPayload = new Organisation();
				organisationIdPayload.setId(organisation.getId());
				organisationIdPayload.setAccountNumber(organisation.getAccountNumber());
				organisationList.add(organisationIdPayload);
			}
		}
		return organisationList;
	}

	public List<Organisation> convertOrganisationCompanyIdPayloadIntoOrganisation(
			List<OrganisationIdPayload> listOrganisationIdPayload) {

		List<Organisation> listOfOrganisation = new ArrayList<>();
		for (OrganisationIdPayload organisationIdPayload : listOrganisationIdPayload) {

			if (organisationIdPayload.getCompanyId() != null) {

				Organisation organisation = new Organisation();
				organisation.setId(organisationIdPayload.getCompanyId());
				listOfOrganisation.add(organisation);
			}

		}
		return listOfOrganisation;
	}

	public List<Organisation> organisationsToOrganisationsConvertor(List<Organisation> list) {
		List<Organisation> organisationList = new ArrayList<>();
		for (Organisation organisation : list) {
			Organisation org = convertOrganisation(organisation);
			List<Organisation> accessList = new ArrayList<>();
			if (organisation.getAccessList() != null && organisation.getAccessList().size() > 0) {
				for (Organisation organisation2 : organisation.getAccessList()) {
					Organisation access = convertOrganisationForAccessList(organisation2);
					accessList.add(access);
				}
			}
			org.setAccessList(accessList);
			organisationList.add(org);
		}
		return organisationList;
	}
	
	public List<OrganisationListPayload> organisationsToOrganisationListPayloadConvertor(List<Organisation> list) {
		List<OrganisationListPayload> organisationList = new ArrayList<>();
		for (Organisation organisation : list) {
			OrganisationListPayload org = convertOrganisationToOrganisationListPayload(organisation);
			List<Organisation> accessList = new ArrayList<>();
			if (organisation.getAccessList() != null && organisation.getAccessList().size() > 0) {
				for (Organisation organisation2 : organisation.getAccessList()) {
					Organisation access = convertOrganisationForAccessList(organisation2);
					accessList.add(access);
				}
			}
			org.setAccessList(accessList);
			organisationList.add(org);
		}
		return organisationList;
	}
	
	public Organisation convertOrganisationForAccessList(Organisation organisation) {
		Organisation org = new Organisation();
		org.setAccountNumber(organisation.getAccountNumber());
		org.setCreatedAt(organisation.getCreatedAt());
		org.setEpicorAccountNumber(organisation.getEpicorAccountNumber());
		org.setId(organisation.getId());
		org.setIsActive(organisation.getIsActive());
		org.setIsAssetListRequired(organisation.getIsAssetListRequired());
		org.setOrganisationName(organisation.getOrganisationName());
		org.setPreviousRecordId(organisation.getPreviousRecordId());
		org.setShortName(organisation.getShortName());
		org.setUpdatedAt(organisation.getUpdatedAt());
		org.setUuid(organisation.getUuid());
		org.setCreatedBy(null);
		org.setMaintenanceMode(organisation.getMaintenanceMode());
		org.setUpdatedBy(null);
		return org;
	}

	public Organisation convertOrganisation(Organisation organisation) {
		Organisation org = new Organisation();
		org.setAccountNumber(organisation.getAccountNumber());
		org.setCreatedAt(organisation.getCreatedAt());
		org.setEpicorAccountNumber(organisation.getEpicorAccountNumber());
		org.setId(organisation.getId());
		org.setIsActive(organisation.getIsActive());
		org.setIsAssetListRequired(organisation.getIsAssetListRequired());
		org.setOrganisationName(organisation.getOrganisationName());
		org.setPreviousRecordId(organisation.getPreviousRecordId());
		org.setShortName(organisation.getShortName());
		org.setUpdatedAt(organisation.getUpdatedAt());
		org.setUuid(organisation.getUuid());
		if(organisation.getOrganisationRole()!=null&&organisation.getOrganisationRole().size()>0) {
			Set<OrganisationRole> roleList = new HashSet();
			for (OrganisationRole roles : organisation.getOrganisationRole()) {
				roleList.add(roles);
			}
			org.setOrganisationRole(roleList);
		}
		
		org.setCreatedBy(null);
		org.setUpdatedBy(null);
		org.setMaintenanceMode(organisation.getMaintenanceMode());
		org.setInstallerList(convertOrganisationToOrganisationId(organisation.getInstallerList()));
		org.setResellerList(convertOrganisationToOrganisationId(organisation.getResellerList()));
		return org;
	}

	
	public OrganisationListPayload convertOrganisationToOrganisationListPayload(Organisation organisation) {
		OrganisationListPayload org = new OrganisationListPayload();
		org.setAccountNumber(organisation.getAccountNumber());
		org.setCreatedAt(organisation.getCreatedAt());
		org.setEpicorAccountNumber(organisation.getEpicorAccountNumber());
		org.setId(organisation.getId());
		org.setIsActive(organisation.getIsActive());
		org.setIsAssetListRequired(organisation.getIsAssetListRequired());
		org.setOrganisationName(organisation.getOrganisationName());
		org.setPreviousRecordId(organisation.getPreviousRecordId());
		org.setShortName(organisation.getShortName());
		org.setUpdatedAt(organisation.getUpdatedAt());
		org.setUuid(organisation.getUuid());
		if(organisation.getOrganisationRole()!=null&&organisation.getOrganisationRole().size()>0) {
			Set<OrganisationRole> roleList = new HashSet<>();
			for (OrganisationRole roles : organisation.getOrganisationRole()) {
				roleList.add(roles);
			}
			org.setOrganisationRole(roleList);
		}
		
		org.setCreatedBy(null);
		org.setUpdatedBy(null);
		org.setMaintenanceMode(organisation.getMaintenanceMode());
		org.setInstallerList(convertOrganisationToOrganisationId(organisation.getInstallerList()));
		org.setResellerList(convertOrganisationToOrganisationId(organisation.getResellerList()));
		return org;
	}

	public CustomerForwardingGroupDTO convert(CustomerForwardingGroup entity) {
		CustomerForwardingGroupDTO dto = new CustomerForwardingGroupDTO();
		dto.setId(entity.getId());
		dto.setName(entity.getName());
		dto.setDescription(entity.getDescription());
		dto.setUuid(entity.getUuid());
		dto.setCreatedOn(entity.getCreatedOn());
		dto.setUpdatedOn(entity.getUpdatedOn());
		dto.setCreatedBy(getUserUuid(entity.getCreatedBy()));
		dto.setUpdatedBy(getUserUuid(entity.getUpdatedBy()));
		return dto;
	}

	public CustomerForwardingRuleDTO convert(CustomerForwardingRule entity) {
		CustomerForwardingRuleDTO dto = new CustomerForwardingRuleDTO();
		dto.setId(entity.getId());
		dto.setType(entity.getType());
		dto.setUrl(entity.getUrl());
		dto.setUuid(entity.getUuid());
		dto.setCreatedOn(entity.getCreatedOn());
		dto.setUpdatedOn(entity.getUpdatedOn());
		dto.setCreatedBy(getUserUuid(entity.getCreatedBy()));
		dto.setUpdatedBy(getUserUuid(entity.getUpdatedBy()));

		if (entity.getOrganisation() != null) {
			dto.setOrganisationUuid(entity.getOrganisation().getUuid());
			dto.setOrganisation(convert(entity.getOrganisation()));
		}

		if (entity.getForwardingRuleUrl() != null) {
			dto.setForwardingRuleUrlUuid(entity.getForwardingRuleUrl().getUuid());
			dto.setForwardingRuleUrl(convert(entity.getForwardingRuleUrl()));
		}

		return dto;
	}

	public com.pct.common.dto.OrganisationDTO convert(Organisation entity) {
		com.pct.common.dto.OrganisationDTO dto = new com.pct.common.dto.OrganisationDTO();
		dto.setId(entity.getId());
		dto.setOrganisationName(entity.getOrganisationName());
		;
		dto.setUuid(entity.getUuid());
		dto.setAccountNumber(entity.getAccountNumber());
		dto.setCreatedAt(entity.getCreatedAt());
		dto.setEpicorAccountNumber(entity.getEpicorAccountNumber());
		dto.setIsActive(entity.getIsActive());
		dto.setIsAssetListRequired(entity.getIsAssetListRequired());
		dto.setShortName(entity.getShortName());
		dto.setPreviousRecordId(entity.getPreviousRecordId());
		dto.setUpdatedAt(entity.getUpdatedAt());
		dto.setCreatedByUuid(getUserUuid(entity.getCreatedBy()));
		dto.setUpdatedByUuid(getUserUuid(entity.getUpdatedBy()));
		dto.setOrganisationRole(entity.getOrganisationRole());
		return dto;
	}

	private String getUserUuid(User user) {
		return user != null ? user.getUuid() : null;
	}

	public CustomerForwardingRule convert(CustomerForwardingRule entity,
			Map<String, CustomerForwardingRuleUrl> customerForwardingRuleUrlMap, CustomerForwardingRulePayload payload,
			Organisation organisation, User user) {
		if (entity == null) {
			entity = new CustomerForwardingRule();
			entity.setUuid(UUID.randomUUID().toString());
			entity.setCreatedOn(Instant.now());
			entity.setCreatedBy(user);
		} else {
			entity.setUpdatedOn(Instant.now());
			entity.setUpdatedBy(user);
		}
		entity.setType(payload.getType());
		// entity.setUrl(payload.getUrl());
		entity.setOrganisation(organisation);
		CustomerForwardingRuleUrl customerForwardingRuleUrl = customerForwardingRuleUrlMap
				.get(payload.getForwardingRuleUrlUuid());
		entity.setForwardingRuleUrl(customerForwardingRuleUrl);
		entity.setUrl(customerForwardingRuleUrl.getEndpointDestination());
		return entity;
	}

	public CustomerForwardingRuleUrlDTO convert(CustomerForwardingRuleUrl entity) {
		CustomerForwardingRuleUrlDTO dto = new CustomerForwardingRuleUrlDTO();

		dto.setId(entity.getId());
		dto.setFormat(entity.getFormat());
		dto.setEndpointDestination(entity.getEndpointDestination());
		dto.setRuleName(entity.getRuleName());
		dto.setDescription(entity.getDescription());
		dto.setUuid(entity.getUuid());
		dto.setKafkaTopicName(entity.getKafkaTopicName());
		dto.setType(entity.getType());

		dto.setCreatedOn(entity.getCreatedOn());
		dto.setUpdatedOn(entity.getUpdatedOn());
		dto.setCreatedBy(getUserUuid(entity.getCreatedBy()));
		dto.setUpdatedBy(getUserUuid(entity.getUpdatedBy()));
		return dto;
	}

	public OrganisationPayload organisationToorganisationPayload(Organisation org) {
		OrganisationPayload comp = new OrganisationPayload();
		comp.setOrganisationName(org.getOrganisationName());
		comp.setId(org.getId());
		comp.setStatus(org.getIsActive());
		// comp.setShortName(com.getShortName());
		//comp.setType(org.getType().getValue());
		comp.setAccountNumber(org.getAccountNumber());
		comp.setIsAssetListRequired(org.getIsAssetListRequired());
		comp.setUuid(org.getUuid());

		// For Firmware Updates
		List<OrganisationSection> listOfFirmwareUpdates = organisationSectionRepository
				.findByDisplayName(Constants.ORGANISATION_SECTION_FIRMWARE_UPDATES);
		if (listOfFirmwareUpdates != null && listOfFirmwareUpdates.size() > 0) {
			List<OrganisationSettings> listOfOrganisationSettings = companySettingsRepository
					.findBySectionUuidAndCompanyUuid(listOfFirmwareUpdates.get(0).getUuid(), org.getUuid());
			for (OrganisationSettings organisationSettings : listOfOrganisationSettings) {
				if (organisationSettings.getDataType().equalsIgnoreCase("Boolean")) {
					if (organisationSettings.getFieldName().equalsIgnoreCase("isApprovalReqForDeviceUpdate")) {
						comp.setIsApprovalReqForDeviceUpdate(Boolean.parseBoolean(organisationSettings.getFieldValue()));
					} else if (organisationSettings.getFieldName().equalsIgnoreCase("isTestReqBeforeDeviceUpdate")) {
						comp.setIsTestReqBeforeDeviceUpdate(Boolean.parseBoolean(organisationSettings.getFieldValue()));
					} else if (organisationSettings.getFieldName().equalsIgnoreCase("isMonthlyReleaseNotesReq")) {
						comp.setIsMonthlyReleaseNotesReq(Boolean.parseBoolean(organisationSettings.getFieldValue()));
					} else if (organisationSettings.getFieldName().equalsIgnoreCase("isDigitalSignReqForFirmware")) {
						comp.setIsDigitalSignReqForFirmware(Boolean.parseBoolean(organisationSettings.getFieldValue()));
					} else if (organisationSettings.getFieldName().equalsIgnoreCase("isAutoResetInstallation")) {
						comp.setIsAutoResetInstallation(Boolean.parseBoolean(organisationSettings.getFieldValue()));
					}
				} else if (organisationSettings.getDataType().equalsIgnoreCase("Number")) {
					if (organisationSettings.getFieldName().equalsIgnoreCase("noOfDevice")) {
						comp.setNoOfDevice(Long.parseLong(organisationSettings.getFieldValue()));
					}
				}
			}
		}
		// END - For Firmware Updates
		return comp;
	}

	
	public OrganisationPayload convertOrganisationBasicInfoToOrganisationPayload(Organisation org) {
		OrganisationPayload organisationPayload = new OrganisationPayload();
		organisationPayload.setOrganisationName(org.getOrganisationName());
		organisationPayload.setId(org.getId());
		organisationPayload.setStatus(org.getIsActive());
		organisationPayload.setShortName(org.getShortName());
		organisationPayload.setAccountNumber(org.getAccountNumber());
		organisationPayload.setIsAssetListRequired(org.getIsAssetListRequired());
		organisationPayload.setUuid(org.getUuid());
		organisationPayload.setPriviousRecordId(org.getPreviousRecordId());
		organisationPayload.setEpicorId(org.getEpicorAccountNumber());
		organisationPayload.setOrganisationRoles(org.getOrganisationRole().stream().map(orgRole -> orgRole.getValue()).collect(Collectors.toSet()));
		return organisationPayload;
	}
	
	 public CustomerLocationPayload convertCompanyToCustomerLocationPayload(Organisation company) {
	        CustomerLocationPayload customerLocationPayload = new CustomerLocationPayload();
	        customerLocationPayload.setAccount_number(company.getAccountNumber());
	        customerLocationPayload.setCustomer_name(company.getOrganisationName());
	        List<LocationPayload> locations = new ArrayList<LocationPayload>();
	        LocationPayload firstLocation = new LocationPayload();
	        firstLocation.setLocation_id(1l);
	        firstLocation.setLocation_name("Depot 1, LA");
	        LocationPayload secondLocation = new LocationPayload();
	        secondLocation.setLocation_id(2l);
	        secondLocation.setLocation_name("Depot 2, LA");
	        locations.add(firstLocation);
	        locations.add(secondLocation);
	        customerLocationPayload.setLocations(locations);
	        return customerLocationPayload;
	    }
	public CustomerWithLocation convertUserOrgToCustomerWithLocation(List<Organisation> organisationList) {
		CustomerWithLocation customerWithLocations = new CustomerWithLocation();
		customerWithLocations.setOrgLocation(prepareLocation());
		if (organisationList != null && organisationList.size()>0) {
			for (Organisation org : organisationList) {
				CustomerWithLocation customerWithLocation = new CustomerWithLocation();
				//customerWithLocation.setCustomerName(org.getOrganisationName());
				//customerWithLocation.setCustomerUuid(org.getUuid());
				
			}
		}
		return customerWithLocations;
	}
	
	public List<Location> prepareLocation() {
		List<Location> locationList = new ArrayList<>();
		Location location1 = new Location();
		Location location2 = new Location();
		Location location3 = new Location();
		Location location4 = new Location();
		Location location5 = new Location();
		Location location6 = new Location();
		Location location7 = new Location();
		Location location8 = new Location();
		Location location9 = new Location();

		location1.setName("TA Ashland");
		location1.setStreet1("100 North Carter Rd");
		location1.setStreet2("");
		location1.setCity("Ashland");
		location1.setState("Virginia");
		location1.setCounty("USA");
		location1.setLatitude(37.7598F);
		location1.setLongitude(-77.4631F);
		location1.setLocationCode(null);
		location1.setUuid("ae586b82-27e0-4204-997c-c8f03239f4ce");
		

		location2.setName("TA Greensboro");
		location2.setStreet1("1101 NC Highway 61");
		location2.setStreet2("");
		location2.setCity("Whitsett");
		location2.setState("North Carolina");
		location2.setCounty("USA");
		location2.setLatitude(36.0635F);
		location2.setLongitude(-79.5636F);
		location2.setLocationCode(null);
		location2.setUuid(UUID.randomUUID().toString());

		location3.setName("TA Brookville");
		location3.setStreet1("245 Allegheny Blvd.");
		location3.setStreet2("");
		location3.setCity("Brookville");
		location3.setState("Pennsylvania");
		location3.setCounty("USA");
		location3.setLatitude(41.1729F);
		location3.setLongitude(-79.0992F);
		location3.setLocationCode(null);
		location3.setUuid(UUID.randomUUID().toString());

		location4.setName("TA Columbia");
		location4.setStreet1("2 Simpson Road");
		location4.setStreet2("");
		location4.setCity("Columbia");
		location4.setState("New Jersey");
		location4.setCounty("USA");
		location4.setLatitude(40.9317F);
		location4.setLongitude(-75.0969F);
		location4.setLocationCode(null);
		location4.setUuid(UUID.randomUUID().toString());

		location5.setName("TA Eloy");
		location5.setStreet1("2949 North Toltec Road");
		location5.setStreet2("");
		location5.setCity("Eloy");
		location5.setState("Arizona");
		location5.setCounty("USA");
		location5.setLatitude(32.7737f);
		location5.setLongitude(-111.6175F);
		location5.setLocationCode(null);
		location5.setUuid(UUID.randomUUID().toString());


		location6.setName("-111.6175");
		location6.setStreet1("3404 W Historical Highway 66");
		location6.setStreet2("");
		location6.setCity("Gallup");
		location6.setState("New Mexico");
		location6.setCounty("USA");
		location6.setLatitude(35.5059F);
		location6.setLongitude(-108.8359F);
		location6.setLocationCode(null);
		location6.setUuid(UUID.randomUUID().toString());

		location7.setName("TA Gary");
		location7.setStreet1("2510 Burr St.");
		location7.setStreet2("");
		location7.setCity("Gary");
		location7.setState("Indiana");
		location7.setCounty("USA");
		location7.setLatitude(41.573F);
		location7.setLongitude(-87.4045F);
		location7.setLocationCode(null);
		location7.setUuid(UUID.randomUUID().toString());

		location8.setName("TA Dayton");
		location8.setStreet1("6762 US Rte 127");
		location8.setStreet2("");
		location8.setCity("Eaton");
		location8.setState("Ohio");
		location8.setCounty("USA");
		location8.setLatitude(39.84F);
		location8.setLongitude(-84.6282F);
		location8.setLocationCode(null);
		location8.setUuid(UUID.randomUUID().toString());

		location9.setName("TA Harrisburg");
		location9.setStreet1("7848 Linglestown Road");
		location9.setStreet2("");
		location9.setCity("Harrisburg");
		location9.setState("Pennsylvania");
		location9.setCounty("USA");
		location9.setLatitude(40.355F);
		location9.setLongitude(-76.7241f);
		location9.setLocationCode(null);
		location9.setUuid(UUID.randomUUID().toString());

		locationList.add(location1);
		locationList.add(location2);
		locationList.add(location3);
		locationList.add(location4);
		locationList.add(location5);
		locationList.add(location6);
		locationList.add(location7);
		locationList.add(location8);
		locationList.add(location9);

		return locationList;
	}
	
	public List<LocationDTO> convertLocationEntityTOLocationDto(List<com.pct.common.model.Location> locationList){
		List<LocationDTO> locationDtoList = new ArrayList<>();
		for (com.pct.common.model.Location location : locationList) {
			LocationDTO loc=new LocationDTO();
			loc.setId(location.getId());
			loc.setCity(location.getCity());
			loc.setLocationName(location.getLocationName());
			loc.setState(location.getState());
			loc.setStreetAddress(location.getStreetAddress());
			loc.setZipCode(location.getZipCode());
			locationDtoList.add(loc);
		}
		return locationDtoList;
	}
}
