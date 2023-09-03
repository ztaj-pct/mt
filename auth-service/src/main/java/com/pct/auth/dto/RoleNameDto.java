package com.pct.auth.dto;

import javax.validation.constraints.NotBlank;

import lombok.Data;

@Data 
public class RoleNameDto {

	private String name;
	private Long roleId;
	private String description;
}