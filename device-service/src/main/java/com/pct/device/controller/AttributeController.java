package com.pct.device.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pct.common.model.Attribute;
import com.pct.device.dto.MessageDTO;
import com.pct.device.dto.ResponseBodyDTO;
import com.pct.device.payload.AttributeRequest;
import com.pct.device.payload.AttributeResponse;
import com.pct.device.service.IAttributeService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@RequestMapping("/attribute")
@CrossOrigin(origins = "*", maxAge = 3600)
@Api(value = "/attribute", tags = "Attribute Management")
public class AttributeController implements IApplicationController<Attribute> {

	private static final Logger attributeControllerLogger = LoggerFactory.getLogger(AttributeController.class);

	@Autowired
	private IAttributeService attributeService;

	@ApiOperation(value = "Add Product attribute", notes = "Api to add a new product attribute", response = String.class, tags = {
			"Attribute Management" })
	@ApiResponses(value = { @ApiResponse(code = 201, message = "Added", response = String.class),
			@ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 401, message = "Unauthorized"),
			@ApiResponse(code = 403, message = "Forbidden"),
			@ApiResponse(code = 500, message = "Internal Server Error") })
	@PostMapping("/add-attribute")
	public ResponseEntity<MessageDTO<String>> addAttribute(@RequestBody AttributeRequest attributeRequest) {
		attributeControllerLogger.info("Inside product addAttribute method");
		try {
			if (attributeRequest != null) {
				attributeService.addAttribute(attributeRequest);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<MessageDTO<String>>(new MessageDTO<String>(e.getMessage()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}

		return new ResponseEntity<MessageDTO<String>>(
				new MessageDTO<String>("Product attribute added successfully", "", true), HttpStatus.CREATED);
	}

	@ApiOperation(value = "Get Product attribute thriugh Uuid", notes = "Api to get the Product attribute by Uuid", response = AttributeResponse.class, tags = {
			"Attribute Management" })
	@ApiResponses(value = { @ApiResponse(code = 201, message = "Success", response = AttributeResponse.class),
			@ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 401, message = "Unauthorized"),
			@ApiResponse(code = 403, message = "Forbidden"),
			@ApiResponse(code = 500, message = "Internal Server Error") })
	@GetMapping(value = "/{uuid}")
	public ResponseEntity<AttributeResponse> getAttibuteByUuid(@Validated @PathVariable("uuid") String attributId) {
		attributeControllerLogger.info("Inside product getAttibuteByUuid method");
		try {
			com.pct.device.payload.AttributeResponse attributeResponse = attributeService.getAttibuteByUuid(attributId);
			return new ResponseEntity<>(attributeResponse, HttpStatus.OK);
		} catch (Exception e) {
			attributeControllerLogger.error("Exception while getting Attribute for uuid {}", attributId, e);
			return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@ApiOperation(value = "Product attribute Update", notes = "Api to Update the Product Attribute", response = String.class, tags = {
			"Attribute Management" })
	@ApiResponses(value = { @ApiResponse(code = 201, message = "Updated", response = String.class),
			@ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 401, message = "Unauthorized"),
			@ApiResponse(code = 403, message = "Forbidden"),
			@ApiResponse(code = 500, message = "Internal Server Error") })
	@PutMapping()
	public ResponseEntity<MessageDTO<String>> updateAttribute(
			@Validated @RequestBody AttributeRequest attributeRequest) {
		attributeControllerLogger.info("Inside updateAttribute method");
		try {
			attributeService.update(attributeRequest);
		} catch (Exception e) {
			throw new RuntimeException("Failed while updating Attribute");
		}
		return new ResponseEntity<MessageDTO<String>>(
				new MessageDTO<String>("Attribute Updated successfully", "", true), HttpStatus.OK);

	}

	// @PostMapping("/getAll")
	public ResponseEntity<Object> getAllAttributes(@RequestParam(value = "_page", required = false) Integer page,
			@RequestParam(value = "_limit", required = false) Integer pageSize,
			@RequestParam(value = "_sort", required = false) String sort,
			@RequestParam(value = "_order", required = false) String order) {
		attributeControllerLogger.info("Inside getAllAttributes");
		try {

			MessageDTO<Object> messageDto = new MessageDTO<>("Attributes Fetched Successfully", true);
			Page<AttributeResponse> packageResponse = attributeService
					.getAllAttributes(getPageable(page - 1, pageSize, sort, order));

			messageDto.setBody(packageResponse.getContent());
			messageDto.setTotalKey(packageResponse.getTotalElements());
			messageDto.setCurrentPage(packageResponse.getNumber());
			messageDto.setTotal_pages(packageResponse.getTotalPages());

			return new ResponseEntity(messageDto, HttpStatus.OK);

		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity(new ResponseBodyDTO(false, "Exception occurred while fetching attributes"),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@ApiOperation(value = "Get list of Product attribute", notes = "Api to get the list of Product attribute", response = AttributeResponse.class, tags = {
			"Attribute Management" })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Fetched all active organisations", response = AttributeResponse.class),
			@ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 401, message = "Unauthorized"),
			@ApiResponse(code = 403, message = "Forbidden"),
			@ApiResponse(code = 500, message = "Internal Server Error") })
	@GetMapping("/get-attributes")
	public ResponseEntity<MessageDTO<List<AttributeResponse>>> getAttributes() {
		logger.info("Inside get attributes Method");
		try {
			List<AttributeResponse> attributeResponseList = attributeService.getAllAttributeList();
			return new ResponseEntity<MessageDTO<List<AttributeResponse>>>(
					new MessageDTO<List<AttributeResponse>>("Fetched all atribute values", attributeResponseList),
					HttpStatus.OK);
		} catch (Exception e) {
			throw new RuntimeException("Failed while getting All atribute values");
		}

	}

}
