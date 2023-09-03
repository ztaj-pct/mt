package com.pct.auth.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.pct.auth.config.JwtUser;
import com.pct.auth.dto.PermissionDto;
import com.pct.auth.repository.PermissionRepository;
import com.pct.auth.service.IPermissionService;
import com.pct.common.model.PermissionEntity;
import com.pct.common.util.Context;
import com.pct.common.util.Logutils;

//@AllArgsConstructor
@Service
public class PermissionServiceImpl implements IPermissionService {
	Logger logger = LoggerFactory.getLogger(PermissionServiceImpl.class);
	@Autowired
	private PermissionRepository permissionRepository;

	@Override
	public Boolean addPermission(PermissionDto dto, Context context) {
		logger.info("Inside addPermission method of PermissionServiceImpl");
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (null != auth || auth.isAuthenticated()) {
			System.out.println("----------------------------------------------------------");
			System.out.println("username : " + ((JwtUser) auth.getPrincipal()).getUsername());
			System.out.println("----------------------------------------------------------");
		}
		PermissionEntity permissionEntity = permissionRepository.findByName(dto.getName());
		if (permissionEntity != null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Permission is already exist");
		}
		PermissionEntity entity = new PermissionEntity();
		entity.setName(dto.getName());
		entity.setDescription(dto.getDescription());
		entity.setMethodType(dto.getMethodType());
		entity.setPath(dto.getPath());

		Logutils.log("PermissionServiceImpl", "addPermission", context.getLogUUId(),
				" Before calling save method of PermissionRepository from PermissionServiceImpl", logger);

		permissionEntity = permissionRepository.save(entity);

		Logutils.log("PermissionServiceImpl", "addPermission", context.getLogUUId(),
				" After calling save method of PermissionRepository from PermissionServiceImpl", logger);

		logger.info("Exiting from updatePermission Method of PermissionServiceImpl");
		return true;
	}

	@Override
	public Boolean updatePermission(Integer permissionId, PermissionDto dto, Context context) {
		logger.info("Inside updatePermission method of PermissionServiceImpl");
		PermissionEntity entity = permissionRepository.findByPermissionId(permissionId);
		if (entity == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid Permission Id");
		}
		if (!dto.getName().equalsIgnoreCase(entity.getName())) {
			PermissionEntity permissionEntity = permissionRepository.findByName(dto.getName());
			if (permissionEntity != null) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Permission is already exist");
			}
		}
		entity.setName(dto.getName());
		entity.setDescription(dto.getDescription());
		entity.setMethodType(dto.getMethodType());
		entity.setPath(dto.getPath());
		Logutils.log("PermissionServiceImpl", "updatePermission", context.getLogUUId(),
				" Before calling save method of PermissionRepository from PermissionServiceImpl ", logger);

		entity = permissionRepository.save(entity);

		Logutils.log("PermissionServiceImpl", "updatePermission", context.getLogUUId(),
				" After calling save method of PermissionRepository from PermissionServiceImpl", logger);

		logger.info("Exiting from updatePermission Method of PermissionServiceImpl");
		return true;
	}

	@Override
	public Boolean deletePermission(Integer permissionId, Context context) {
		logger.info("Inside deletePermission method of PermissionServiceImpl");
		PermissionEntity entity = permissionRepository.findByPermissionId(permissionId);
		if (entity == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid Permission Id");
		}

		Logutils.log("PermissionServiceImpl", "deletePermission", context.getLogUUId(),
				" Before calling delete method of PermissionRepository from PermissionServiceImpl", logger);

		permissionRepository.delete(entity);

		Logutils.log("PermissionServiceImpl", "deletePermission", context.getLogUUId(),
				" After calling delete method of PermissionRepository from PermissionServiceImpl ", logger);

		logger.info("Exiting from deletePermission Method of PermissionServiceImpl");
		return true;
	}
}