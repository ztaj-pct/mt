package com.pct.device.controller;

import java.util.List;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pct.common.controller.IApplicationController;
import com.pct.common.dto.ResponseBodyDTO;
import com.pct.common.model.Attribute;
import com.pct.common.model.Company;
import com.pct.common.payload.AttributeResponse;
import com.pct.common.payload.ProductMasterRequest;
import com.pct.common.payload.ProductMasterResponse;
import com.pct.device.dto.MessageDTO;
import com.pct.device.service.IProductMasterService;

@RestController
@RequestMapping("/product")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ProductMasterController implements IApplicationController<Company> {

	@Autowired
	private IProductMasterService productMasterService;

	private static final Logger productMasterControllerLogger = LoggerFactory.getLogger(ProductMasterController.class);
	@Autowired
	private MessageSource responseMessageSource;

	@PostMapping("/add-product")
	public ResponseEntity<MessageDTO<String>> addProduct(
			@Valid @RequestBody ProductMasterRequest productMasterRequest) {
		productMasterControllerLogger.info("Inside product addProduct method");
		try {
			if (productMasterRequest != null) {
				productMasterService.addProduct(productMasterRequest);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Failed while adding product attributes ");
		}

		return new ResponseEntity<MessageDTO<String>>(new MessageDTO<String>("Product  added successfully", "", true),
				HttpStatus.CREATED);
	}

	/*
	 * @PostMapping("/update-product") public ResponseEntity<MessageDTO<String>>
	 * updateProduct(@RequestBody ProductMasterRequest productMasterRequest) {
	 * 
	 * return null;
	 * 
	 * }
	 */

	@GetMapping("/getAllProduct")
	public ResponseEntity<ResponseBodyDTO<List<ProductMasterResponse>>> getAllProduct() {
		productMasterControllerLogger.info("Inside getAllProduct Method");
		try {
			List<ProductMasterResponse> ProductMasterList = productMasterService.getAllProductList();
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

	
	
	
	@GetMapping("/attribute-list/product-code")
	public ResponseEntity<? extends Object> getAttributeListByProductCode(
			@RequestParam(value = "product-code", required = true) String productCode,
			@RequestParam(value = "gateway-uuid", required = false) String gatewayUuid) {
		try {
			List<AttributeResponse> attributeResponse = productMasterService.getAttributeListByProductCode(productCode,
					gatewayUuid);
			return new ResponseEntity<>(attributeResponse, HttpStatus.OK);
		} catch (Exception exception) {
			productMasterControllerLogger.error("Exception occurred while getting attributeResponseList", exception);
			return new ResponseEntity<>(exception.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

}
