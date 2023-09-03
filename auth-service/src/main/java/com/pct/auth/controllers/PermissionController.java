package com.pct.auth.controllers;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pct.auth.dto.PermissionDto;
import com.pct.auth.service.IPermissionService;
import com.pct.common.response.BaseResponse;
import com.pct.common.response.Status;
import com.pct.common.util.Context;
import com.pct.common.util.Logutils;

import io.swagger.annotations.ApiOperation;

//@Api(tags = "Permission controller provider", description = "This is a permission contoller to provide all the permission related APIs")
//@AllArgsConstructor
@RestController
@RequestMapping("/api")
public class PermissionController {

	Logger logger = LoggerFactory.getLogger(PermissionController.class);

	@Autowired
	private IPermissionService permissionService;

	@CrossOrigin("*")
	@ApiOperation(value = "It is to add new permission")
	@PostMapping("/addPermission")
	public ResponseEntity<BaseResponse<Boolean, Integer>> addPermission(
			@Valid @RequestBody PermissionDto permissionDto) {
		logger.info("Inside addPermission method of PermissionController");
		logger.info("permissionDto:" + permissionDto);
		Context context = new Context();
		Logutils.log("PermissionController", "addPermission", context.getLogUUId(),
				"Before calling addPermission method of PermissionController ", logger);

		permissionService.addPermission(permissionDto, context);
		Logutils.log("PermissionController", "addPermission", context.getLogUUId(),
				"After calling addPermission method of PermissionController ", logger);
		logger.info("Permission is created successfully");
		BaseResponse<Boolean, Integer> response = new BaseResponse<>();
		response.setStatus(Status.SUCCESS);
		response.setMessage("Permission is created successfully");
		logger.info("Exiting from addPermission method of PermissionController");
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@CrossOrigin("*")
	@ApiOperation(value = "It is to update existing permission")
	@PutMapping("/updatePermission/{permissionId}")
	public ResponseEntity<BaseResponse<Boolean, Integer>> updatePermission(@PathVariable Integer permissionId,
			@Valid @RequestBody PermissionDto permissionDto) {
		logger.info("Inside updatePermission method of PermissionController");
		logger.info("permissionId:" + permissionId);
		logger.info("permissionDto:" + permissionDto);
		Context context = new Context();
		Logutils.log("PermissionController", "updatePermission", context.getLogUUId(),
				"Before calling updatePermission method of PermissionController ", logger);

		permissionService.updatePermission(permissionId, permissionDto, context);

		Logutils.log("PermissionController", "updatePermission", context.getLogUUId(),
				"After calling updatePermission method of PermissionController ", logger);
		BaseResponse<Boolean, Integer> response = new BaseResponse<>();
		response.setStatus(Status.SUCCESS);
		response.setMessage("Permission with id: [" + permissionId + "] updated successfully.");
		logger.info("Exiting from updatePermission method of PermissionController");
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@CrossOrigin("*")
	@ApiOperation(value = "It is to delete existing permission")
	@DeleteMapping("/deletePermission/{permissionId}")
	public ResponseEntity<BaseResponse<Boolean, Integer>> deletePermission(@PathVariable Integer permissionId) {
		logger.info("Inside deletePermission method of PermissionController");
		logger.info("permissionId:" + permissionId);

		Context context = new Context();
		Logutils.log("PermissionController", "deletePermission", context.getLogUUId(),
				"Before calling deletePermission method of PermissionController ", logger);

		permissionService.deletePermission(permissionId, context);

		Logutils.log("PermissionController", "deletePermission", context.getLogUUId(),
				"After calling deletePermission method of PermissionController ", logger);
		BaseResponse<Boolean, Integer> response = new BaseResponse<>();
		response.setStatus(Status.SUCCESS);
		response.setMessage("Permission with id: [" + permissionId + "] deleted successfully.");
		logger.info("Exiting from deletePermission method of PermissionController");
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
}