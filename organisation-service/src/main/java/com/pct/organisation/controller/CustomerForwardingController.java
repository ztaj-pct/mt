package com.pct.organisation.controller;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.pct.common.model.CustomerForwardingRule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.pct.common.dto.CustomerForwardingGroupDTO;
import com.pct.common.dto.CustomerForwardingRuleDTO;
import com.pct.common.dto.CustomerForwardingRuleUrlDTO;
import com.pct.common.dto.ResponseBodyDTO;
import com.pct.organisation.exception.BaseMessageException;
import com.pct.organisation.payload.CustomerForwardingRuleUrlPayload;
import com.pct.organisation.service.CustomerForwardingService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@RequestMapping("/customer/forwarding")
@Api(value = "/customer/forwarding", tags = "Customer Forwarding Management")
public class CustomerForwardingController {

	@Autowired
	private CustomerForwardingService customerForwardingService;

	@ApiOperation(value = "Get All Customer Forwarding Groups", notes = "API to get all customer forwarding groups", tags = {
			"Customer Forwarding Management" })
	@ApiResponses(value = { 
			@ApiResponse(code = 200, message = "Fetch Successfully"),
			@ApiResponse(code = 400, message = "Bad Request"), 
			@ApiResponse(code = 401, message = "Unauthorized"),
			@ApiResponse(code = 403, message = "Forbidden"),
			@ApiResponse(code = 500, message = "Internal Server Error") })
	@GetMapping(value = "/groups/all")
	public ResponseEntity<List<CustomerForwardingGroupDTO>> getAllCustomerForwardingGroup() {
		return ResponseEntity.ok(customerForwardingService.getAllCustomerForwardingGroup());
	}
	
	@ApiOperation(value = "Get Customer Forwarding Groups Using Organization Uuid", notes = "API to get all customer forwarding groups using organization uuid", tags = {
	"Customer Forwarding Management" })
	@ApiResponses(value = { 
		@ApiResponse(code = 200, message = "Fetch Successfully"),
		@ApiResponse(code = 400, message = "Bad Request"), 
		@ApiResponse(code = 401, message = "Unauthorized"),
		@ApiResponse(code = 403, message = "Forbidden"),
		@ApiResponse(code = 500, message = "Internal Server Error") })
	@GetMapping(value = "/groups")
	public ResponseEntity<List<CustomerForwardingGroupDTO>> getCustomerForwardingGroupByOrganizationUuid(@RequestParam("organizationUuid") String organizationUuid) {
		return ResponseEntity.ok(customerForwardingService.getCustomerForwardingGroupByOrganizationUuid(organizationUuid));
	}
	
	@ApiOperation(value = "Get Customer Forwarding Rules Using Organization Uuids", notes = "API to get customer forwarding rules using organization uuids", tags = {
	"Customer Forwarding Management" })
	@ApiResponses(value = { 
		@ApiResponse(code = 200, message = "Fetch Successfully"),
		@ApiResponse(code = 400, message = "Bad Request"), 
		@ApiResponse(code = 401, message = "Unauthorized"),
		@ApiResponse(code = 403, message = "Forbidden"),
		@ApiResponse(code = 500, message = "Internal Server Error") })
	@GetMapping(value = "/rules")
	public ResponseEntity<List<CustomerForwardingRuleDTO>> getCustomerForwardingRulesByOrganizationUuids(
			@RequestParam("organizationUuids") Set<String> organizationUuids) {
		return ResponseEntity
				.ok(customerForwardingService.getCustomerForwardingRulesByOrganizationUuids(organizationUuids));
	}
	
	@ApiOperation(value = "Get All Customer Forwarding Rule Urls", notes = "API to get all customer forwarding rule urls", tags = {
	"Customer Forwarding Management" })
	@ApiResponses(value = { 
		@ApiResponse(code = 200, message = "Fetch Successfully"),
		@ApiResponse(code = 400, message = "Bad Request"), 
		@ApiResponse(code = 401, message = "Unauthorized"),
		@ApiResponse(code = 403, message = "Forbidden"),
		@ApiResponse(code = 500, message = "Internal Server Error") })
	@GetMapping(value = "/rule/urls")
	public ResponseEntity<List<CustomerForwardingRuleUrlDTO>> getAllCustomerForwardingRuleUrl() {
		return ResponseEntity
				.ok(customerForwardingService.getAllCustomerForwardingRuleUrl());
	}
	
	@ApiOperation(value = "Import Customer Forwarding Rules", notes = "API to import customer forwarding rules", tags = {
	"Customer Forwarding Management" })
	@ApiResponses(value = { 
		@ApiResponse(code = 200, message = "Import Successfully"),
		@ApiResponse(code = 400, message = "Bad Request"), 
		@ApiResponse(code = 401, message = "Unauthorized"),
		@ApiResponse(code = 403, message = "Forbidden"),
		@ApiResponse(code = 500, message = "Internal Server Error") })
	@PostMapping(value = "/rules/import")
	public ResponseEntity<ResponseBodyDTO<Map<String, Object>>> importCustomerForwardingRules(@RequestPart("file") MultipartFile file) {
		try {
		return ResponseEntity
				.ok(new ResponseBodyDTO<>(true,"Import Successfully" ,customerForwardingService.importCustomerForwardingRules(file)));
		} catch (BaseMessageException e) {
			return new ResponseEntity<>(new ResponseBodyDTO<>(false, e.getMessage()), HttpStatus.BAD_REQUEST);
		}
	}
	
	@ApiOperation(value = "Create Customer Forwarding Rule Valid Url", notes = "API to create customer forwarding rule valid url", tags = {
	"Customer Forwarding Management" })
	@ApiResponses(value = { 
		@ApiResponse(code = 201, message = "Create Successfully"),
		@ApiResponse(code = 400, message = "Bad Request"), 
		@ApiResponse(code = 401, message = "Unauthorized"),
		@ApiResponse(code = 403, message = "Forbidden"),
		@ApiResponse(code = 500, message = "Internal Server Error") })
	@PostMapping(value = "/rule/url")
	public ResponseEntity<ResponseBodyDTO<CustomerForwardingRuleUrlDTO>> createCustomerForwardingRuleUrl(@RequestBody @Validated CustomerForwardingRuleUrlPayload payload) {
		try {
			return new ResponseEntity<>(new ResponseBodyDTO<>(true,"Create Successfully" ,customerForwardingService.createCustomerForwardingRuleUrl(payload)), HttpStatus.CREATED);
		} catch (BaseMessageException e) {
			return new ResponseEntity<>(new ResponseBodyDTO<>(false, e.getMessage()), HttpStatus.BAD_REQUEST);
		}
	}
	
	@ApiOperation(value = "Update Customer Forwarding Rule Valid Url", notes = "API to update customer forwarding rule valid url", tags = {
	"Customer Forwarding Management" })
	@ApiResponses(value = { 
		@ApiResponse(code = 200, message = "Updated Successfully"),
		@ApiResponse(code = 400, message = "Bad Request"), 
		@ApiResponse(code = 401, message = "Unauthorized"),
		@ApiResponse(code = 403, message = "Forbidden"),
		@ApiResponse(code = 500, message = "Internal Server Error") })
	@PutMapping(value = "/rule/url")
	public ResponseEntity<ResponseBodyDTO<CustomerForwardingRuleUrlDTO>> modifyCustomerForwardingRuleUrl(@RequestParam("uuid") String uuid, @RequestBody @Validated CustomerForwardingRuleUrlPayload payload) {
		try {
			return new ResponseEntity<>(new ResponseBodyDTO<>(true,"Update Successfully" ,customerForwardingService.modifyCustomerForwardingRuleUrl(uuid, payload)), HttpStatus.OK);
		} catch (BaseMessageException e) {
			return new ResponseEntity<>(new ResponseBodyDTO<>(false, e.getMessage()), HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(value = "Get Customer Forwarding Rules", notes = "API to get customer forwarding rules", tags = {
			"Customer Forwarding Management" })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Fetch Successfully"),
			@ApiResponse(code = 400, message = "Bad Request"),
			@ApiResponse(code = 401, message = "Unauthorized"),
			@ApiResponse(code = 403, message = "Forbidden"),
			@ApiResponse(code = 500, message = "Internal Server Error") })
	@GetMapping(value = "/rules/customer-forwarding")
	public ResponseEntity<List<CustomerForwardingRuleDTO>>  getCustomerForwardingRules() {
		return ResponseEntity
				.ok().body(customerForwardingService.getAllCustomerForwardingRules());
	}
	
	@ApiOperation(value = "Get All Customer Forwarding Group names", notes = "API to get all customer forwarding group names", tags = {
	"Customer Forwarding Management" })
	@ApiResponses(value = { 
		@ApiResponse(code = 200, message = "Fetch Successfully"),
		@ApiResponse(code = 400, message = "Bad Request"), 
		@ApiResponse(code = 401, message = "Unauthorized"),
		@ApiResponse(code = 403, message = "Forbidden"),
		@ApiResponse(code = 500, message = "Internal Server Error") })
	@GetMapping(value = "/groups/name/all")
	public ResponseEntity<List<String>> getAllCustomerForwardingGroupName() {
	return ResponseEntity.ok(customerForwardingService.getAllCustomerForwardingGroupName());
	}
	
}
