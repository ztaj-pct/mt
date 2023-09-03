package com.pct.common.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
//import com.pct.common.model.Company;
import com.pct.common.model.Role;
import com.pct.common.model.User;
import com.pct.common.model.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class UserDTO {

    public String timeZone;
    private String uuid;
    private Long id;
    private String userName;
    private String firstName;
    private String lastName;
    private String password;
    private String email;
    private String notify;
    private String countryCode;
    private String phone;
    private Boolean isActive;
    private List<RoleDTO> role;
    private Boolean isDeleted;
    private Organisation organisation;
    private Boolean isPasswordChange;
    private String landingPage;
    private LocationDTO homeLocation;
    private List<LocationDTO> additionalLocations;

    public UserDTO(Long id, String userName, String firstName, String lastName, String password, String email,
                   String countryCode, String phone, Boolean isActive, List<RoleDTO> role, Boolean isDeleted,
                   Organisation fleet, String timeZone,String uuid,String landingPage,List<LocationDTO> additionalLocations) {
        super();
        this.id = id;
        this.userName = userName;
        this.firstName = firstName;
        this.lastName = lastName;
        this.password = password;
        this.email = email;
        this.countryCode = countryCode;
        this.phone = phone;
        this.isActive = isActive;
        this.role = role;
        this.notify = notify;
        this.isDeleted = isDeleted;
        this.organisation = fleet;
        this.timeZone = timeZone;
        this.uuid = uuid;
        this.landingPage = landingPage;
        this.additionalLocations= additionalLocations;
    }

    public UserDTO(User user) {
        this.id = id;
        this.userName = user.getUserName();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.password = user.getPassword();
        this.notify = user.getNotify();
        this.email = user.getEmail();
        this.countryCode = user.getCountryCode();
        this.phone = user.getPhone();
        this.isActive = user.getIsActive();
        List<RoleDTO> roleDtoList = new ArrayList<>();
        for (Role roles : user.getRole()) {
			RoleDTO roleDto = new RoleDTO();
			roleDto.setId(roles.getId());
			roleDto.setName(roles.getName());
			roleDto.setRoleId(roles.getRoleId());
			roleDto.setDescription(roles.getDescription());
			roleDtoList.add(roleDto);
		}
        this.role = roleDtoList;
        List<LocationDTO> locationDTOList = new ArrayList<>();
        for(Location location : user.getAdditionalLocations()) {
        	LocationDTO locationDTO = new LocationDTO();
        	locationDTO.setLocationName(location.getLocationName());
        	locationDTO.setStreetAddress(location.getStreetAddress());
        	locationDTO.setCity(location.getCity());
        	locationDTO.setState(location.getState());
        	locationDTO.setZipCode(location.getZipCode());
        	locationDTOList.add(locationDTO);
        }
        this.additionalLocations = locationDTOList;
        this.isDeleted = user.getIsDeleted();
        this.organisation = user.getOrganisation();
        this.timeZone = user.getTimeZone();
        this.uuid = user.getUuid();
        this.landingPage = user.getLandingPage();
    }

    public UserDTO() {
        super();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public List<RoleDTO> getRole() {
		return role;
	}

	public void setRole(List<RoleDTO> role) {
		this.role = role;
	}

	public Boolean getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    public Organisation getOrganisation() {
        return organisation;
    }

    public void setOrganisation(Organisation fleet) {
        this.organisation = fleet;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public String getNotify() {
        return notify;
    }

    public void setNotify(String notify) {
        this.notify = notify;
    }


    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

	public Boolean getIsPasswordChange() {
		return isPasswordChange;
	}

	public void setIsPasswordChange(Boolean isPasswordChange) {
		this.isPasswordChange = isPasswordChange;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getLandingPage() {
		return landingPage;
	}

	public void setLandingPage(String landingPage) {
		this.landingPage = landingPage;
	}

	public List<LocationDTO> getAdditionalLocations() {
		return additionalLocations;
	}

	public void setAdditionalLocations(List<LocationDTO> additionalLocations) {
		this.additionalLocations = additionalLocations;
	}

	public LocationDTO getHomeLocation() {
		return homeLocation;
	}

	public void setHomeLocation(LocationDTO homeLocation) {
		this.homeLocation = homeLocation;
	}

}
