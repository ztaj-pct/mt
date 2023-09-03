package com.pct.auth.dto;

import java.util.List;

import javax.validation.constraints.NotBlank;

import lombok.Data;

@Data
public class RoleDto {

	@NotBlank(message = "Name must not be null or blank")
	private String name;
	
	@NotBlank(message = "Description must not be null or blank")
	private String description;
	
	private List<Integer> permissionIdList;
}
