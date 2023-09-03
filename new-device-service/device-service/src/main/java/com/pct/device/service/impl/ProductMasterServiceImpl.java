package com.pct.device.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pct.common.model.Attribute;
import com.pct.common.model.AttributeValue;
import com.pct.common.model.ProductMaster;
import com.pct.common.payload.AttributeResponse;
import com.pct.common.payload.ProductMasterRequest;
import com.pct.common.payload.ProductMasterResponse;
import com.pct.device.exception.DeviceException;
import com.pct.device.repository.IAttributeRepository;
import com.pct.device.repository.IAttributeValueRepository;
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
		LOGGER.info("******Inside addProduct**********");

		if (productMasterRequest != null && productMasterRequest.getProductName()!=null) {
			ProductMaster pd = productMasterRepository.findByProductName(productMasterRequest.getProductName());
			if(pd !=null && pd.getUuid()!=null) {
				throw new DeviceException("Product Name is already registered"+pd.getProductName());
			}
			ProductMaster productMaster = beanConverter
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
			productMasterRepository.save(productMaster);
		} else {
			throw new InstantiationException("Product Master JSON Can't be NULL");
		}
		return Boolean.TRUE;

	}

	@Override
	public ProductMasterResponse getProductByUuid(String uuid) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ProductMasterResponse update(ProductMasterRequest productMasterRequest) throws InstantiationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ProductMasterResponse> getAllProductList() {

		List<ProductMaster> productMasterList = new ArrayList<>();
		productMasterList = productMasterRepository.findAll();
		if (productMasterList.size() < 1) {
			throw new DeviceException("No such Product found");
		}
		List<ProductMasterResponse> productMasterResponseList = beanConverter
				.convertProductMasterToProductMasteResponse(productMasterList);

		return productMasterResponseList;

	}

	@Override
	public List<AttributeResponse> getAttributeListByProductCode(String productCode, String gatewayUuid) {

		List<AttributeResponse> attributeResponseList = new ArrayList<>();
		if (productCode != null && !productCode.isEmpty()) {
			ProductMaster productMaster = productMasterRepository.findByUuid(productCode);
			List<Attribute> attributeList =new ArrayList<>();
			//attributeRepository.findByProductUuid(productMaster.getUuid());
			attributeList.forEach(attribute -> {
				AttributeValue attributeValue = new AttributeValue();
						//attributeValueRepository.findByAttributeUuidAndGatewayUuid(attribute.getUuid(), gatewayUuid);
				AttributeResponse attributeResponse = new AttributeResponse();
				attributeResponse.setName(attribute.getAttributeName());
				attributeResponse.setAttribute_uuid(attribute.getUuid());
				attributeResponse.setThreshold_Value(attributeValue.getValue());
				//attributeResponse.setValue(attribute.getAttributeValue());
				attributeResponseList.add(attributeResponse);
			});

		}
		return attributeResponseList;
	}

	@Override
	public List<Attribute> getProductByName(String name) {
		List<Attribute> at = new ArrayList<>();
	
		ProductMaster pd = productMasterRepository.findByProductName(name);
		if(pd!=null && pd.getUuid()!=null) {
		at = atttributeRespo.findByProductMasterUuid(pd.getUuid());
		}
		return at;
	}

}
