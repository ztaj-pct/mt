package com.pct.device.redis;

import java.io.Serializable;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PermissionCacheModel implements Serializable {
	
	private Integer permissionId;
	private String name;
	private String description;
    private MethodType methodType;
	private String path;

}
