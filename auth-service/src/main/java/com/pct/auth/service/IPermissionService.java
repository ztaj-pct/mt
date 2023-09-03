package com.pct.auth.service;

import com.pct.auth.dto.PermissionDto;
import com.pct.common.util.Context;

public interface IPermissionService {

	public Boolean addPermission(PermissionDto permissionDto, Context context);

	public Boolean updatePermission(Integer permissionId, PermissionDto permissionDto, Context context);

	public Boolean deletePermission(Integer permissionId, Context context);
}