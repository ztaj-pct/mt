package com.pct.auth.service;

import java.util.List;

import com.pct.auth.dto.RoleDto;
import com.pct.auth.dto.RoleNameDto;
import com.pct.common.dto.RoleDTO;
import com.pct.common.payload.RoleCacheModel;
import com.pct.common.util.Context;

public interface IRoleService {

	public Boolean addRole(RoleDto roleDto,Context context);

	public Boolean updateRole(Long roleId, RoleDto roleDto, Context context);

	public Boolean deleteRole(Long roleId, Context context);

	public Boolean addAllRolesInRedis(Context context);

	public RoleCacheModel getRole(Long roleId, Context context);

	public List<RoleCacheModel> getAllRoles(Context context);
	
	public List<RoleNameDto> getAllRoleName();
	
	public RoleDTO getByName(String name, Context context);
}