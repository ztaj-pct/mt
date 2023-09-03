package com.pct.auth.controllers;

import java.util.List;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pct.auth.dto.RoleDto;
import com.pct.auth.dto.RoleNameDto;
import com.pct.common.payload.RoleCacheModel;
import com.pct.common.response.BaseResponse;
import com.pct.common.response.Status;
import com.pct.common.util.Context;
import com.pct.common.util.Logutils;
import com.pct.auth.service.IRoleService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;

@Api(tags = "Role controller provider", description = "This is a role contoller to provide all the role related APIs")
//@AllArgsConstructor
@RestController
@RequestMapping("/api")
public class RoleController {
	@Autowired
	private IRoleService roleService;
	Logger logger = LoggerFactory.getLogger(RoleController.class);

	@CrossOrigin("*")
	@ApiOperation(value = "It is to add new role")
	@PostMapping("/addRole")
	public ResponseEntity<BaseResponse<Boolean, Integer>> addRole(@Valid @RequestBody RoleDto roleDto) {
		logger.info("Inside addRole method of RoleController");
		logger.info("RoleDto:" + roleDto);
		Context context = new Context();
		Logutils.log("RoleController", "addRole", context.getLogUUId(),
				"Before calling addRole method of IRoleService from RoleController", logger);

		roleService.addRole(roleDto, context);
		Logutils.log("RoleController", "addRole", context.getLogUUId(),
				"After calling addRole method of IRoleService from RoleController", logger);

		BaseResponse<Boolean, Integer> response = new BaseResponse<>();
		response.setStatus(Status.SUCCESS);
		response.setMessage("Role is created successfully.");
		logger.info("Exiting from addRole method of RoleController");
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@CrossOrigin("*")
	@ApiOperation(value = "It is to update existing role")
	@PutMapping("/updateRole")
	public ResponseEntity<BaseResponse<Boolean, Integer>> updateRole(@RequestParam(name = "roleId") Long roleId,
			@Valid @RequestBody RoleDto roleDto) {
		logger.info("Inside updateRole method of RoleController");
		logger.info("RoleDto:" + roleDto);
		logger.info("Role ID:" + roleId);
		Context context = new Context();
		Logutils.log("RoleController", "updateRole", context.getLogUUId(),
				"Before calling updateRole method of IRoleService from RoleController", logger);

		roleService.updateRole(roleId, roleDto, context);
		Logutils.log("RoleController", "updateRole", context.getLogUUId(),
				"After calling updateRole method of IRoleService from RoleController", logger);

		BaseResponse<Boolean, Integer> response = new BaseResponse<>();
		response.setStatus(Status.SUCCESS);
		response.setMessage("Role with id: [" + roleId + "] updated successfully.");
		logger.info("Exiting from updateRole method of RoleController");
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@CrossOrigin("*")
	@ApiOperation(value = "It is to delete existing role")
	@DeleteMapping("/deleteRole/{roleId}")
	public ResponseEntity<BaseResponse<Boolean, Integer>> deleteRole(@PathVariable(name = "roleId") Long roleId) {
		logger.info("Inside deleteRole method of RoleController");
		logger.info("Role ID:" + roleId);
		Context context = new Context();
		Logutils.log("RoleController", "deleteRole", context.getLogUUId(),
				"Before calling deleteRole method of IRoleService from RoleController", logger);

		roleService.deleteRole(roleId, context);
		Logutils.log("RoleController", "deleteRole", context.getLogUUId(),
				"After calling deleteRole method of IRoleService from RoleController", logger);

		BaseResponse<Boolean, Integer> response = new BaseResponse<>();
		response.setStatus(Status.SUCCESS);
		response.setMessage("Role with id: [" + roleId + "] deleted successfully.");
		logger.info("Exiting from deleteRole method of RoleController");
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@CrossOrigin("*")
	@ApiOperation(value = "It is to add list of roles in redis cache")
	@PostMapping("/resetRoleCache")
	public ResponseEntity<BaseResponse<Boolean, Integer>> addAllRolesInRedis() {
		logger.info("Inside addAllRolesInRedis method of RoleController");
		Context context = new Context();
		Logutils.log("RoleController", "addAllRolesInRedis", context.getLogUUId(),
				"Before calling addAllRolesInRedis method of IRoleService from RoleController", logger);
		roleService.addAllRolesInRedis(context);
		Logutils.log("RoleController", "addAllRolesInRedis", context.getLogUUId(),
				"After calling addAllRolesInRedis method of IRoleService from RoleController", logger);

		BaseResponse<Boolean, Integer> response = new BaseResponse<>();
		response.setStatus(Status.SUCCESS);
		response.setMessage("All Roles in redis is created successfully.");
		logger.info("Exiting from addAllRolesInRedis method of RoleController");
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@CrossOrigin("*")
	@ApiOperation(value = "It is to get Role from redis cache")
	@GetMapping("/getRole/{roleId}")
	public ResponseEntity<BaseResponse<RoleCacheModel, Integer>> getRole(@PathVariable(name = "roleId") Long roleId) {
		logger.info("Inside getRole method of RoleController");
		logger.info("Role ID:" + roleId);
		BaseResponse<RoleCacheModel, Integer> response = new BaseResponse<>();
		Context context = new Context();
		Logutils.log("RoleController", "getRole", context.getLogUUId(),
				"Before calling getRole method of IRoleService from RoleController", logger);

		response.setData(roleService.getRole(roleId, context));
		Logutils.log("RoleController", "getRole", context.getLogUUId(),
				"After calling getRole method of IRoleService from RoleController", logger);

		response.setStatus(Status.SUCCESS);
		response.setMessage("Role from redis fetched successfully.");
		logger.info("Exiting from getRole method of RoleController");
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@CrossOrigin("*")
	@ApiOperation(value = "It is to get list of Roles from redis cache")
	@GetMapping("/getAllRoles")
	public ResponseEntity<BaseResponse<List<RoleCacheModel>, Integer>> getAllRoles() {
		logger.info("Inside getAllRoles method of RoleController");
		BaseResponse<List<RoleCacheModel>, Integer> response = new BaseResponse<>();
		Context context = new Context();
		Logutils.log("RoleController", "getAllRoles", context.getLogUUId(),
				"Before calling getAllRoles method of IRoleService from RoleController", logger);

		response.setData(roleService.getAllRoles(context));
		Logutils.log("RoleController", "getAllRoles", context.getLogUUId(),
				"After calling getAllRoles method of IRoleService from RoleController", logger);

		response.setStatus(Status.SUCCESS);
		response.setMessage("Roles list from redis fetched successfully.");
		logger.info("Exiting from getAllRoles method of RoleController");
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	@CrossOrigin("*")
	@ApiOperation(value = "It is to get list of Roles from redis cache")
	@GetMapping("/getAllRoleName")
	public ResponseEntity<BaseResponse<List<RoleNameDto>, Integer>> getAllRoleName() {
		BaseResponse<List<RoleNameDto>, Integer> response = new BaseResponse<>();
		response.setData(roleService.getAllRoleName());
		response.setStatus(Status.SUCCESS);
		response.setMessage("Roles list from redis fetched successfully.");
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
}