package com.pct.device.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
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

import com.pct.common.controller.IApplicationController;
import com.pct.common.dto.ResponseDTO;
import com.pct.common.model.ProductMaster;
import com.pct.common.util.JwtUser;
import com.pct.device.dto.MessageDTO;
import com.pct.device.dto.ResponseBodyDTO;
import com.pct.device.payload.AttributeResponse;
import com.pct.device.payload.ProductMasterRequest;
import com.pct.device.payload.ProductMasterResponse;
import com.pct.device.service.IProductMasterService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@RequestMapping("/product")
@CrossOrigin(origins = "*", maxAge = 3600)
@Api(value = "/product", tags = "Product Management")
public class ProductMasterController implements IApplicationController<ProductMaster> {

	@Autowired
	private IProductMasterService productMasterService;

	private static final Logger productMasterControllerLogger = LoggerFactory.getLogger(ProductMasterController.class);

	@ApiOperation(value = "Add Product", notes = "API For add a new Product", response = String.class, tags = {
			"Product Management" })
	@ApiResponses(value = { @ApiResponse(code = 201, message = "Created", response = String.class),
			@ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 401, message = "Unauthorized"),
			@ApiResponse(code = 403, message = "Forbidden"),
			@ApiResponse(code = 500, message = "Internal Server Error") })
	@PostMapping()
	public ResponseEntity<ResponseDTO> addProduct(@Valid @RequestBody ProductMasterRequest productMasterRequest) {
		productMasterControllerLogger.info("Request received to add device detail", productMasterRequest.toString());
		try {
			if (productMasterRequest != null) {
				productMasterService.addProduct(productMasterRequest);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Failed while adding product ");
		}

		return new ResponseEntity(new ResponseDTO(true, "Product  added successfully"), HttpStatus.CREATED);
	}

	@ApiOperation(value = "Update Product", notes = "API For Update Product", response = String.class, tags = {
			"Product Management" })
	@ApiResponses(value = { @ApiResponse(code = 201, message = "Created", response = String.class),
			@ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 401, message = "Unauthorized"),
			@ApiResponse(code = 403, message = "Forbidden"),
			@ApiResponse(code = 500, message = "Internal Server Error") })
	@PutMapping()
	public ResponseEntity<ResponseBodyDTO<ProductMaster>> updateProduct(
			@Valid @RequestBody ProductMasterRequest productMasterRequest) {
		ProductMaster prod = new ProductMaster();
		productMasterControllerLogger.info("Request received to update product detail {}",
				productMasterRequest.toString());
		JwtUser jwtUser = (JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		logger.info("Username : " + jwtUser.getUsername());
		try {
			if (productMasterRequest != null) {
				prod = productMasterService.updateProduct(productMasterRequest);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Failed while updating product ");
		}

		return new ResponseEntity<ResponseBodyDTO<ProductMaster>>(
				new ResponseBodyDTO<ProductMaster>(true, "Product Updated Successfully", prod), HttpStatus.CREATED);
	}

	@ApiOperation(value = "Get all Products", notes = "API to get the list of Products", response = ProductMasterResponse.class, tags = {
			"Product Management" })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Fetched product List(s) successfully", response = ProductMasterResponse.class),
			@ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 401, message = "Unauthorized"),
			@ApiResponse(code = 403, message = "Forbidden"),
			@ApiResponse(code = 500, message = "Internal Server Error") })
	@GetMapping()
	public ResponseEntity<ResponseBodyDTO<List<ProductMasterResponse>>> getProducts(
			HttpServletRequest httpServletRequest, @RequestParam(value = "uuid", required = false) String uuid) {
		JwtUser jwtUser = (JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		logger.info("Username : " + jwtUser.getUsername());
		productMasterControllerLogger.info("Received request to fetch product list for uuid " + uuid);
		try {
			List<ProductMasterResponse> ProductMasterList = productMasterService.getProductByUuid(uuid);
			return new ResponseEntity<ResponseBodyDTO<List<ProductMasterResponse>>>(
					new ResponseBodyDTO<List<ProductMasterResponse>>(true, "Fetched product List(s) successfully",
							ProductMasterList),
					HttpStatus.OK);
		} catch (Exception exception) {
			productMasterControllerLogger.error("Exception occurred while getting product", exception);
			return new ResponseEntity<ResponseBodyDTO<List<ProductMasterResponse>>>(
					new ResponseBodyDTO<List<ProductMasterResponse>>(false, exception.getMessage(), null),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@ApiOperation(value = "Get the attribute list by ProductCode", notes = "API to get the attribute list by ProductCode", response = Object.class, tags = {
			"Product Management" })
	@ApiResponses(value = { @ApiResponse(code = 201, message = "Fetched successfully", response = Object.class),
			@ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 401, message = "Unauthorized"),
			@ApiResponse(code = 403, message = "Forbidden"),
			@ApiResponse(code = 500, message = "Internal Server Error") })
	@GetMapping("/attribute-list")
	public ResponseEntity<? extends Object> getAttributeListByProductCode(
			@RequestParam(value = "product-code", required = true) String productCode,
			@RequestParam(value = "gateway-uuid", required = false) String gatewayUuid) {
		try {
			List<AttributeResponse> attributeResponse = productMasterService.getAttributeListByProductCode(productCode,
					gatewayUuid);
			return new ResponseEntity<>(attributeResponse, HttpStatus.OK);
		} catch (Exception exception) {
			productMasterControllerLogger.error("Exception occurred while getting attributeResponseList", exception);
			return new ResponseEntity<>(exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@ApiOperation(value = "Get the product list", notes = "API to get the product list", response = Object.class, tags = {
			"Product Management" })
	@ApiResponses(value = { @ApiResponse(code = 201, message = "Fetched successfully", response = Object.class),
			@ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 401, message = "Unauthorized"),
			@ApiResponse(code = 403, message = "Forbidden"),
			@ApiResponse(code = 500, message = "Internal Server Error") })
	@GetMapping("/product-list")
	public ResponseEntity<? extends Object> getAttributeListByProductCode() {
		try {
			List<ProductMasterResponse> attributeResponse = productMasterService.getAllProductList();
			return new ResponseEntity<>(attributeResponse, HttpStatus.OK);
		} catch (Exception exception) {
			productMasterControllerLogger.error("Exception occurred while getting attributeResponseList", exception);
			return new ResponseEntity<>(exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

}
