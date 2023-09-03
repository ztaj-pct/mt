package com.pct.auth.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import com.pct.common.util.MethodType;
import lombok.Data;

@Data
public class PermissionDto {

	@NotBlank(message = "Name must not be blank")
	private String name;
	
	@NotBlank(message = "Description must not be blank")
	private String description;
	
	@NotNull(message = "Method type is not valid")
	private MethodType methodType;
	
	@NotBlank(message = "Path must not be blank")
	private String path;
}
