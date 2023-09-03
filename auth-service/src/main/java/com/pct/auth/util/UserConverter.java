package com.pct.auth.util;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.tomcat.util.bcel.classfile.Constant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import com.pct.auth.config.JwtUser;
import com.pct.auth.dto.OrganisationDetails;
import com.pct.auth.dto.UserOrganizationDetails;
import com.pct.common.constant.Constants;
import com.pct.common.constant.OrganisationRole;
import com.pct.common.dto.LocationDTO;
import com.pct.common.dto.OrganisationDTO;
import com.pct.common.dto.RoleDTO;
import com.pct.common.dto.RoleResponse;
import com.pct.common.dto.UserDTO;
import com.pct.common.dto.UserResponse;
import com.pct.common.model.Location;
import com.pct.common.model.Organisation;
import com.pct.common.model.Role;
import com.pct.common.model.User;
import com.pct.common.util.JwtUtil;

@Component
public class UserConverter {
	
	@Autowired
	private RestUtils restUtils;
	
	public User userDtoToUser(UserDTO udto) {
		User user = new User();
		user.setId(udto.getId());
		user.setEmail(udto.getEmail());
		user.setFirstName(udto.getFirstName());
		user.setOrganisation(udto.getOrganisation());
		user.setIsActive(udto.getIsActive());
		user.setIsDeleted(false);
		user.setIsPasswordChange(false);
		user.setLastName(udto.getLastName());
		user.setNotify(udto.getNotify());
		user.setCountryCode(udto.getCountryCode());
		user.setPhone(udto.getPhone());
		user.setUserName(udto.getEmail());
		user.setTimeZone(Instant.now() + "");
		JwtUtil jwtUtil = new JwtUtil();
		user.setCreatedBy(jwtUtil.getUserFromContaxt());
		user.setCreatedAt(Instant.now());
		return user;
	}

	public UserDTO userToUserDTO(User user) {
		UserDTO dto = new UserDTO();
		dto.setId(user.getId());
		dto.setUuid(user.getUuid());
		dto.setEmail(user.getEmail());
		dto.setFirstName(user.getFirstName());
		//dto.setOrganisation(user.getOrganisation());
		dto.setCountryCode(user.getCountryCode());
		dto.setNotify(user.getNotify());
		dto.setIsActive(user.getIsActive());
		dto.setIsDeleted(user.getIsDeleted());
		dto.setLastName(user.getLastName());
		dto.setPhone(user.getPhone());
		dto.setUserName(user.getUserName());
		dto.setTimeZone(user.getTimeZone());
		dto.setId(user.getId());
		List<RoleDTO> roleDtoList = new ArrayList<>();
		for (Role roles : user.getRole()) {
			RoleDTO roleDto = new RoleDTO();
			roleDto.setId(roles.getId());
			roleDto.setName(roles.getName());
			roleDto.setRoleId(roles.getRoleId());
			roleDto.setDescription(roles.getDescription());
			roleDtoList.add(roleDto);
		}
		dto.setRole(roleDtoList);
		if (user.getOrganisation() != null) {
			dto.setOrganisation(convertOrganisation(user.getOrganisation()));
		}
		if (user.getHomeLocation() != null) {
			dto.setHomeLocation(convertLocationToLocationDTO(user.getHomeLocation()));
		}
		List<LocationDTO> addLocationDTOList = new ArrayList<>();
		for (Location location : user.getAdditionalLocations()) {
			LocationDTO locationDTO = convertLocationToLocationDTO(location);
			addLocationDTOList.add(locationDTO);
		}
		dto.setAdditionalLocations(addLocationDTOList);
		dto.setIsPasswordChange(user.getIsPasswordChange());
		dto.setLandingPage(user.getLandingPage());
		return dto;
	}

	public Organisation convertOrganisation(Organisation organisation) {
		Organisation org = new Organisation();
		org.setAccountNumber(organisation.getAccountNumber());
		org.setId(organisation.getId());
		org.setUuid(organisation.getUuid());
		org.setOrganisationName(organisation.getOrganisationName());
		org.setEpicorAccountNumber(organisation.getEpicorAccountNumber());
		org.setShortName(organisation.getShortName());
		//org.setType(organisation.getType());
		if(organisation.getOrganisationRole() != null && organisation.getOrganisationRole().size() > 0) {
			Set<OrganisationRole> organisationRoleList = new HashSet<OrganisationRole>();
			for(OrganisationRole organisationRole : organisation.getOrganisationRole()) {
				organisationRoleList.add(organisationRole);
			}
			org.setOrganisationRole(organisationRoleList);
		}
		return org;

	}
	
	public LocationDTO convertLocationToLocationDTO(Location location) {
		LocationDTO locationDTO = new LocationDTO();
		locationDTO.setId(location.getId());
		locationDTO.setLocationName(location.getLocationName());
		locationDTO.setStreetAddress(location.getStreetAddress());
		locationDTO.setCity(location.getCity());
		locationDTO.setState(location.getState());
		locationDTO.setZipCode(location.getZipCode());
		return locationDTO;

	}

	public Page<UserResponse> convertUserToUserDto(Page<User> userDetails, Pageable pageable) {
		List<UserResponse> userPayLoadList = new ArrayList<>();
		userDetails.forEach(user -> {
			UserResponse dto = new UserResponse();
			dto.setId(user.getId());
			dto.setEmail(user.getEmail());
			dto.setFirstName(user.getFirstName());
			dto.setCountryCode(user.getCountryCode());
			dto.setNotify(user.getNotify());
			dto.setIsActive(user.getIsActive());
			dto.setIsDeleted(user.getIsDeleted());
			dto.setLastName(user.getLastName());
			dto.setPhone(user.getPhone());
			dto.setUserName(user.getUserName());
			dto.setTimeZone(user.getTimeZone());
			dto.setId(user.getId());
			if (user.getOrganisation() != null)
				dto.setOrganisationName(user.getOrganisation().getOrganisationName());
			RoleResponse role = new RoleResponse();
			List<Role> r = user.getRole();
			if (r != null && r.size() > 0) {
				role.setName(r.get(0).getName());
				role.setDescription(r.get(0).getDescription());
			}
			dto.setRole(role);
			dto.setIsPasswordChange(user.getIsPasswordChange());
			userPayLoadList.add(dto);
		});
		Page<UserResponse> page = new PageImpl<>(userPayLoadList, pageable, userDetails.getTotalElements());
		return page;
	}

	public List<Organisation> convertOrganisationToOrganisation(List<Organisation> organisations) {
		List<Organisation> organisationsList = new ArrayList<>();
		for (Organisation organisationOld : organisations) {
			Organisation organisation = new Organisation();
			organisation.setId(organisationOld.getId());
			organisation.setIsActive(organisationOld.getIsActive());
			organisation.setShortName(organisationOld.getShortName());
			organisation.setAccountNumber(organisationOld.getAccountNumber());
			organisation.setIsAssetListRequired(organisationOld.getIsAssetListRequired());
			organisation.setUuid(organisationOld.getUuid());
			organisation.setPreviousRecordId(organisationOld.getPreviousRecordId());
			organisation.setEpicorAccountNumber(organisationOld.getEpicorAccountNumber());
			organisation.setOrganisationRole(organisationOld.getOrganisationRole());
			organisationsList.add(organisation);
		}
		return organisationsList;
	}

	public List<OrganisationDetails> updateAccessList(List<Organisation> accessList) {
		List<OrganisationDetails> accessList2 = new ArrayList<>();
		for (Organisation access : accessList) {
			OrganisationDetails orgDetails = new OrganisationDetails();
//			orgDetails.setAccountNumber(access.getAccountNumber());
			orgDetails.setOrganizationName(access.getOrganisationName());
//			orgDetails.setActive(access.getIsActive());
//			orgDetails.setId(access.getId());
			orgDetails.setUuid(access.getUuid());
//			orgDetails.setOrganisationRole(access.getOrganisationRole());
			accessList2.add(orgDetails);
		}
		return accessList2;
	}

	public UserOrganizationDetails userToUserOrganizationDetails(User user, List<Organisation> orgAccessList) {

		UserOrganizationDetails userOrganizationDetails = new UserOrganizationDetails();
		List<OrganisationDetails> orgAccessList2 = new ArrayList<>();
		userOrganizationDetails.setUuid(user.getUuid());
		userOrganizationDetails.setId(user.getId());
		if (user.getOrganisation() != null) {
			OrganisationDetails orgDetails = new OrganisationDetails();
			orgDetails.setAccountNumber(user.getOrganisation().getAccountNumber());
			orgDetails.setOrganizationName(user.getOrganisation().getOrganisationName());
			orgDetails.setActive(user.getOrganisation().getIsActive());
			orgDetails.setId(user.getOrganisation().getId());
			orgDetails.setUuid(user.getOrganisation().getUuid());
			OrganisationDTO organisationDTO  = restUtils.findOrganisationById(user.getOrganisation().getId());
			orgDetails.setOrganisationRole(organisationDTO.getOrganisationRole());
			userOrganizationDetails.setOrganization(orgDetails);
		}
		if(orgAccessList != null) {
			for(Organisation org :orgAccessList) {
				OrganisationDetails orgDetails = new OrganisationDetails();
				orgDetails.setAccountNumber(org.getAccountNumber());
				orgDetails.setOrganizationName(org.getOrganisationName());
				orgDetails.setActive(org.getIsActive());
				orgDetails.setUuid(org.getUuid());
				orgAccessList2.add(orgDetails);	
			}
		}
		
		userOrganizationDetails.setOrgAccessList(orgAccessList2);
		List<String> roleNameList = new ArrayList<>();
		for (Role roles : user.getRole()) {
			roleNameList.add(roles.getName());
		}
		userOrganizationDetails.setDefaultLocationUuid("ae586b82-27e0-4204-997c-c8f03239f4ce");

		userOrganizationDetails.setAssignedRoles(roleNameList);
		return userOrganizationDetails;
	}
}