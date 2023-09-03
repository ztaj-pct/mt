package com.pct.device.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pct.device.payload.ProductMasterResponse;
import com.pct.common.model.Attribute;
import com.pct.common.model.AttributeValue;
import com.pct.common.model.ProductMaster;
import com.pct.device.exception.DeviceException;
import com.pct.device.payload.AttributeResponse;
import com.pct.device.payload.ProductMasterRequest;

import com.pct.device.repository.IAttributeRepository;
import com.pct.device.repository.IProductMasterRepository;
import com.pct.device.service.IProductMasterService;
import com.pct.device.util.BeanConverter;

@Service
public class ProductMasterServiceImpl implements IProductMasterService {

	@Autowired
	private IProductMasterRepository productMasterRepository;

	@Autowired
	private IAttributeRepository atttributeRespo;

	@Autowired
	private BeanConverter beanConverter;

	private static final Logger LOGGER = LoggerFactory.getLogger(ProductMasterServiceImpl.class);

	@Override
	public Boolean addProduct(ProductMasterRequest productMasterRequest) throws InstantiationException {
		LOGGER.info("Inside addProduct and fetching productDetail", productMasterRequest);
		if (productMasterRequest != null && productMasterRequest.getProductName() != null) {
			ProductMaster pd = productMasterRepository.findByProductName(productMasterRequest.getProductName());
			if (pd != null && pd.getUuid() != null) {
				throw new DeviceException("Product Name is already registered" + pd.getProductName());
			}
			ProductMaster productMaster = (ProductMaster) beanConverter
					.convertProductMasterRequestToProductMasterBean(productMasterRequest);
			boolean isProductUuidUnique = false;
			String productUuid = "";

			while (!isProductUuidUnique) {
				productUuid = UUID.randomUUID().toString();
				ProductMaster byUuid = productMasterRepository.findByUuid(productUuid);
				if (byUuid == null) {
					isProductUuidUnique = true;
				}
			}
			productMaster.setUuid(productUuid);

			productMaster.setUuid(productUuid);
			productMasterRepository.save(productMaster);
			LOGGER.info("Product Details saved successfully");
		} else {
			throw new InstantiationException("Product Master JSON Can't be NULL");
		}
		return Boolean.TRUE;
	}

	@Override
	public List<ProductMasterResponse> getProductByUuid(String uuid) throws InstantiationException {
		LOGGER.info("Inside getProductByUuid for uuid " + uuid);
		List<ProductMaster> product = new ArrayList<>();
		if (uuid != null) {
			LOGGER.info("Fetching product list detail for uuid " + uuid);
			ProductMaster prod = productMasterRepository.findByUuid(uuid);
			product.add(prod);
		} else {
			LOGGER.info("Fetching all product details as uuid is null");
			product = productMasterRepository.findAll();
		}
		if (product.size() < 1) {
			throw new DeviceException("No such Product found");
		}
		List<ProductMasterResponse> productMasterResList = beanConverter
				.convertProductMasterToProductMasteResponse(product);
		return productMasterResList;

	}

	@Override
	public ProductMaster updateProduct(ProductMasterRequest productMasterRequest) throws InstantiationException {
		LOGGER.info("Inside updateProduct and fetching productDetail" + productMasterRequest);
		if (productMasterRequest != null && productMasterRequest.getProductName() != null
				&& productMasterRequest.getUuid() != null) {
			LOGGER.info("Fetching product details for uuid" + productMasterRequest.getUuid());
			ProductMaster pd = productMasterRepository.findByUuid(productMasterRequest.getUuid());
			if (pd != null) {
				ProductMaster productMaster = (ProductMaster) beanConverter
						.convertProductMasterRequestToProductMasterBean(productMasterRequest);
				ProductMaster prod = productMasterRepository.save(productMaster);
				LOGGER.info("Product details updated for the uuid" + productMasterRequest.getUuid());
				return prod;
			} else {
				throw new InstantiationException("Product not found");
			}
		} else {
			throw new InstantiationException("Product Master JSON Can't be NULL");
		}
	}

	@Override
	public List<ProductMasterResponse> getAllProductList() {

		LOGGER.info("getAllProductList()");
		List<ProductMaster> findAll = productMasterRepository.findAll();
		if (findAll.size() < 1) {
			throw new DeviceException("No such Product found");
		}
		List<ProductMasterResponse> productMasterResList = beanConverter
				.convertProductMasterToProductMasteResponse(findAll);
		return productMasterResList;

	}

	@Override
	public List<AttributeResponse> getAttributeListByProductCode(String productCode, String gatewayUuid) {

		LOGGER.info("getAttributeListByProductCode() and fetch productCode and gatewayUUid" + productCode + ""
				+ gatewayUuid);

		List<AttributeResponse> attributeResponseList = new ArrayList<>();
		if (productCode != null && !productCode.isEmpty()) {
			ProductMaster productMaster = productMasterRepository.findByUuid(productCode);
			List<Attribute> attributeList = new ArrayList<>();
			// attributeRepository.findByProductUuid(productMaster.getUuid());
			attributeList.forEach(attribute -> {
				AttributeValue attributeValue = new AttributeValue();
				// attributeValueRepository.findByAttributeUuidAndGatewayUuid(attribute.getUuid(),
				// gatewayUuid);
				AttributeResponse attributeResponse = new AttributeResponse();
				attributeResponse.setName(attribute.getAttributeName());
				attributeResponse.setAttribute_uuid(attribute.getUuid());
				attributeResponse.setThreshold_Value(attributeValue.getValue());
				// attributeResponse.setValue(attribute.getAttributeValue());
				attributeResponseList.add(attributeResponse);
			});

		}
		return attributeResponseList;

	}
	
	
	// -------------------------Aamir 1 Start --------------------------------------//

	@Override
	public List<Attribute> getProductByName(String name) {

		LOGGER.info("getProductByName and fetch name " + name);

		List<Attribute> at = new ArrayList<>();

		ProductMaster pd = productMasterRepository.findByProductName(name);
		if (pd != null && pd.getUuid() != null) {
			at = atttributeRespo.findByProductMasterUuid(pd.getUuid());
		}
		return at;
	}

}
