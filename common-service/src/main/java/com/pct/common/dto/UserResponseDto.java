package com.pct.common.dto;

import lombok.Data;

@Data
public class UserResponseDto {
	public String timeZone;
	private Long id;
	private String userName;
	private String firstName;
	private String lastName;
	private String email;
	private String notify;
	private String countryCode;
	private String phone;
	private Boolean isActive;
	private RoleResponseDto role;
	private Boolean isDeleted;
	private Boolean isPasswordChange;
}
