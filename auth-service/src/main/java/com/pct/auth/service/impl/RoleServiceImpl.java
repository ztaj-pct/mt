package com.pct.auth.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.server.ResponseStatusException;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.pct.auth.config.JwtUser;
import com.pct.auth.dto.RoleDto;
import com.pct.auth.dto.RoleNameDto;
import com.pct.auth.redis.RedisService;
import com.pct.auth.repository.PermissionRepository;
import com.pct.auth.repository.RoleRepository;
import com.pct.auth.service.IRoleService;
import com.pct.common.constant.Constants;
import com.pct.common.dto.RoleDTO;
import com.pct.common.model.PermissionEntity;
import com.pct.common.model.Role;
import com.pct.common.payload.PermissionCacheModel;
import com.pct.common.payload.RoleCacheModel;
import com.pct.common.redis.AuthPrefix;
import com.pct.common.util.Context;
import com.pct.common.util.Logutils;

//@AllArgsConstructor
@Service
public class RoleServiceImpl implements IRoleService {
	@Autowired
	private RoleRepository roleRepository;
	@Autowired
	private PermissionRepository permissionRepository;
	@Autowired
	private RedisService redisService;
	@Autowired
	private ModelMapper modelMapper;
	private static final Logger logger = LoggerFactory.getLogger(RoleServiceImpl.class);

	@Override
	public Boolean addRole(RoleDto roleDto, Context context) {
		logger.info("Inside addRole method of RoleServiceImpl");

		Logutils.log("RoleServiceImpl", "addRole", context.getLogUUId(),
				" Before calling findByName method of RoleRepository from RoleServiceImpl", logger);

		Role entity = roleRepository.findByName(roleDto.getName());

		Logutils.log("RoleServiceImpl", "addRole", context.getLogUUId(),
				" After calling findByName method of RoleRepository from RoleServiceImpl", logger);

		if (entity != null)
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role is already exist");

		Role roleEntity = new Role();
		roleEntity.setName(roleDto.getName());
		roleEntity.setDescription(roleDto.getDescription());

		List<PermissionEntity> permissionEntityList = new ArrayList<PermissionEntity>();
		Logutils.log("RoleServiceImpl", "addRole", context.getLogUUId(),
				" Before calling findByPermissionId method of PermissionRepository from RoleServiceImpl", logger);
		for (Integer permissionId : roleDto.getPermissionIdList()) {
			PermissionEntity permissionEntity = permissionRepository.findByPermissionId(permissionId);
			if (permissionEntity == null)
				throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid list of Permission Ids");
			permissionEntityList.add(permissionEntity);
		}
		Logutils.log("RoleServiceImpl", "addRole", context.getLogUUId(),
				" After calling findByPermissionId method of PermissionRepository from RoleServiceImpl", logger);

		roleEntity.setPermissions(permissionEntityList);
		Logutils.log("RoleServiceImpl", "addRole", context.getLogUUId(),
				" Before calling save method of RoleRepository from RoleServiceImpl", logger);

		roleRepository.save(roleEntity);

		Logutils.log("RoleServiceImpl", "addRole", context.getLogUUId(),
				" After calling save method of RoleRepository from RoleServiceImpl", logger);

		logger.info("Exiting from addRole Method of RoleServiceImpl");
		return true;
	}

	@Override
	public Boolean updateRole(Long roleId, RoleDto roleDto, Context context) {
		logger.info("Inside updateRole method of RoleServiceImpl");

		Logutils.log("RoleServiceImpl", "updateRole", context.getLogUUId(),
				" Before calling findByRoleId method of RoleRepository from RoleServiceImpl", logger);

		Role roleEntity = roleRepository.findByRoleId(roleId);
		Logutils.log("RoleServiceImpl", "updateRole", context.getLogUUId(),
				" After calling findByRoleId method of RoleRepository from RoleServiceImpl", logger);

		if (roleEntity == null)
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid Role Id");

		if (!roleDto.getName().equalsIgnoreCase(roleEntity.getName())) {
			Logutils.log("RoleServiceImpl", "updateRole", context.getLogUUId(),
					" Before calling findByName method of RoleRepository from RoleServiceImpl", logger);

			Role entity = roleRepository.findByName(roleDto.getName());
			Logutils.log("RoleServiceImpl", "updateRole", context.getLogUUId(),
					" After calling findByName method of RoleRepository from RoleServiceImpl", logger);

			if (entity != null)
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role is already exist");
		}
		List<PermissionEntity> permissionEntityList = new ArrayList<PermissionEntity>();
		Logutils.log("RoleServiceImpl", "updateRole", context.getLogUUId(),
				" Before calling findByPermissionId method of PermissionRepository from RoleServiceImpl", logger);

		for (Integer permissionId : roleDto.getPermissionIdList()) {
			PermissionEntity permissionEntity = permissionRepository.findByPermissionId(permissionId);
			if (permissionEntity == null)
				throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid list of Permission Ids");
			permissionEntityList.add(permissionEntity);
		}
		Logutils.log("RoleServiceImpl", "updateRole", context.getLogUUId(),
				" After calling findByPermissionId method of PermissionRepository from RoleServiceImpl", logger);

		roleEntity.setName(roleDto.getName());
		roleEntity.setDescription(roleDto.getDescription());
		roleEntity.setPermissions(permissionEntityList);
		Logutils.log("RoleServiceImpl", "addRole", context.getLogUUId(),
				" Before calling save method of RoleRepository from RoleServiceImpl", logger);

		roleRepository.save(roleEntity);
		Logutils.log("RoleServiceImpl", "addRole", context.getLogUUId(),
				" After calling save method of RoleRepository from RoleServiceImpl", logger);

		RoleCacheModel role = modelMapper.map(roleEntity, RoleCacheModel.class);

		Logutils.log("RoleServiceImpl", "addRole", context.getLogUUId(),
				" Before calling hset method of RedisService from RoleServiceImpl", logger);

		redisService.hset(AuthPrefix.getRole, roleEntity.getRoleId().toString(), JSON.toJSON(role).toString());

		Logutils.log("RoleServiceImpl", "addRole", context.getLogUUId(),
				" After calling hset method of RedisService from RoleServiceImpl", logger);

		logger.info("Exiting from updateRole Method of RoleServiceImpl");
		return true;
	}

	@Override
	public Boolean deleteRole(Long roleId, Context context) {
		logger.info("Inside deleteRole method of RoleServiceImpl");

		Logutils.log("RoleServiceImpl", "deleteRole", context.getLogUUId(),
				" Before calling findByRoleId method of RoleRepository from RoleServiceImpl", logger);

		Role roleEntity = roleRepository.findByRoleId(roleId);

		Logutils.log("RoleServiceImpl", "deleteRole", context.getLogUUId(),
				" After calling findByRoleId method of RoleRepository from RoleServiceImpl", logger);

		if (roleEntity == null)
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid Role Id");

		Logutils.log("RoleServiceImpl", "deleteRole", context.getLogUUId(),
				" Before calling delete method of RoleRepository from RoleServiceImpl", logger);

		roleRepository.delete(roleEntity);

		Logutils.log("RoleServiceImpl", "deleteRole", context.getLogUUId(),
				" After calling delete method of RoleRepository from RoleServiceImpl", logger);

		logger.info("Exiting from deleteRole Method of RoleServiceImpl");
		return true;
	}

	@Override
	public Boolean addAllRolesInRedis(Context context) {
		logger.info("Inside addAllRolesInRedis method of RoleServiceImpl");

		Logutils.log("RoleServiceImpl", "deleteRole", context.getLogUUId(),
				" Before calling findAll method of RoleRepository from RoleServiceImpl", logger);

		List<Role> roleEntityList = (List<Role>) roleRepository.findAll();

		Logutils.log("RoleServiceImpl", "deleteRole", context.getLogUUId(),
				" After calling findAll method of RoleRepository from RoleServiceImpl", logger);

		Logutils.log("RoleServiceImpl", "addRole", context.getLogUUId(),
				" Before calling hset method of RedisService from RoleServiceImpl", logger);

		if (!CollectionUtils.isEmpty(roleEntityList)) {
			for (Role roleEntity : roleEntityList) {
				List<PermissionCacheModel> permissionCacheModelList = null;
				if (!CollectionUtils.isEmpty(roleEntity.getPermissions())) {
					permissionCacheModelList = roleEntity.getPermissions().stream().map(permission -> {
						PermissionCacheModel permissionCacheModel = modelMapper.map(permission,
								PermissionCacheModel.class);
						return permissionCacheModel;
					}).collect(Collectors.toList());
				}
				RoleCacheModel role = modelMapper.map(roleEntity, RoleCacheModel.class);
				role.setPermissions(permissionCacheModelList);

				redisService.hset(AuthPrefix.getRole, roleEntity.getRoleId().toString(), JSON.toJSON(role).toString());
			}
		}

		Logutils.log("RoleServiceImpl", "addRole", context.getLogUUId(),
				" After calling hset method of RedisService from RoleServiceImpl", logger);

		logger.info("Exiting from addAllRolesInRedis Method of RoleServiceImpl");
		return true;
	}

	@Override
	public RoleCacheModel getRole(Long roleId, Context context) {
		logger.info("Inside getRole method of RoleServiceImpl");

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		System.out.println("username : " + ((JwtUser) auth.getPrincipal()).getUsername());
		RoleCacheModel role = null;
		Logutils.log("RoleServiceImpl", "addRole", context.getLogUUId(),
				" Before calling hset method of RedisService from RoleServiceImpl", logger);

		String json = redisService.hget(AuthPrefix.getRole, roleId.toString());
		Logutils.log("RoleServiceImpl", "addRole", context.getLogUUId(),
				" After calling hset method of RedisService from RoleServiceImpl", logger);
		if (Objects.nonNull(json)) {
			role = JSON.toJavaObject(JSONObject.parseObject(json), RoleCacheModel.class);
		}
		logger.info("Exiting from getRole Method of RoleServiceImpl");
		return role;
	}

	@Override
	public List<RoleCacheModel> getAllRoles(Context context) {
		logger.info("Inside getAllRoles method of RoleServiceImpl");

		List<RoleCacheModel> jsonList = Collections.emptyList();
		Logutils.log("RoleServiceImpl", "addRole", context.getLogUUId(),
				" Before calling hgetall method of RedisService from RoleServiceImpl", logger);

		jsonList = redisService.hgetall(AuthPrefix.getRole, RoleCacheModel.class);
		Logutils.log("RoleServiceImpl", "addRole", context.getLogUUId(),
				" Before calling hgetall method of RedisService from RoleServiceImpl", logger);

		logger.info("Exiting from getAllRoles Method of RoleServiceImpl");
		return jsonList;
	}

	@Override
	public List<RoleNameDto> getAllRoleName() {
		logger.info("Fetching User details");
		JwtUser jwtUser = (JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		logger.info("Username : " + jwtUser.getUsername());
		List<String> roleName = jwtUser.getRoleName();
		boolean roleAvailable = false;
		boolean roleAvailable2 = false;
		for (String rName : roleName) {
			if (rName.contains(Constants.ROLE_CUSTOMER_ADMIN)) {
				roleAvailable = true;
				break;
			}
			if (rName.contains(Constants.ROLE_ORGANIZATION_USER)) {
				roleAvailable2 = true;
				break;
			}
		}
		List<Role> roleList = new ArrayList<>();
		if (roleAvailable) {
			Role role = roleRepository.findByName(Constants.ROLE_CUSTOMER_ADMIN);
			roleList.add(role);
		}else if (roleAvailable2) {
			Role role = roleRepository.findByName(Constants.ROLE_ORGANIZATION_USER);
			roleList.add(role);
		}else {
			roleList = roleRepository.findAll();
		}
		List<RoleNameDto> roleNameDtoList = new ArrayList<RoleNameDto>();
		roleList.forEach(role -> {
			RoleNameDto roleNameDto = new RoleNameDto();
			roleNameDto.setRoleId(role.getRoleId());
			roleNameDto.setName(role.getName());
			roleNameDto.setDescription(role.getDescription());
			roleNameDtoList.add(roleNameDto);
		});

		return roleNameDtoList;
	}

	@Override
	public RoleDTO getByName(String name, Context context) {
		logger.info("Inside getByName method of RoleServiceImpl");

		Logutils.log("RoleServiceImpl", "findByName", context.getLogUUId(),
				" Before calling findByName method of roleRepository from RoleServiceImpl", logger);

		Role role = roleRepository.findByName(name);

		Logutils.log("RoleServiceImpl", "findByName", context.getLogUUId(),
				" Before calling findByName method of roleRepository from RoleServiceImpl", logger);

		RoleDTO roleDTO = modelMapper.map(role, RoleDTO.class);

		logger.info("Exiting from getByName Method of RoleServiceImpl");

		return roleDTO;
	}

}