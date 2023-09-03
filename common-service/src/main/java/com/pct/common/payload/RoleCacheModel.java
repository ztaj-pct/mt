package com.pct.common.payload;

import java.io.Serializable;
import java.util.List;


import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RoleCacheModel implements Serializable {
	
	private Integer roleId;
    private String name;
	private String description;
	private List<PermissionCacheModel> permissions;

}
