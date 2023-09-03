package com.pct.common.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Data;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Data
public class UserResponse {
	public String timeZone;
	private Long id;
	private String userName;
	private String firstName;
	private String lastName;
	private String email;
	private String organisationName;
	private String notify;
	private String countryCode;
	private String phone;
	private Boolean isActive;
	private RoleResponse role;
	private Boolean isDeleted;
	private Boolean isPasswordChange;
}
