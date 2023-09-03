package com.pct.device.service.impl;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pct.common.model.Attribute;
import com.pct.common.model.AttributeValue;
import com.pct.common.model.ProductMaster;
import com.pct.device.payload.AttributeResponse;
import com.pct.device.payload.ProductMasterRequest;
import com.pct.device.payload.ProductMasterResponse;
import com.pct.device.repository.IAttributeRepository;
import com.pct.device.repository.IProductMasterRepository;
import com.pct.device.util.BeanConverter;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
public class ProductMasterServiceImplTest {

	@InjectMocks
	ProductMasterServiceImpl productMasterServiceImpl;

	@Mock
	private IProductMasterRepository productMasterRepository;

	@Mock
	private IAttributeRepository atttributeRespo;

	@Mock
	private BeanConverter beanConverter;

	private static final Logger LOGGER = LoggerFactory.getLogger(ProductMasterServiceImplTest.class);

	@Test
	public void addProduct() throws InstantiationException {

		String uuid = "536a3ca1-3c7c-45b0-844a-e012135c46d3";
		String productCode = "23244";
		String productName = "Product";
		String type = "type";
		String subtype = "sub_type";
		boolean isBlocker = true;

		ProductMasterRequest productMasterRequest = new ProductMasterRequest();
		productMasterRequest.setProductCode(productCode);
		productMasterRequest.setProductName(productName);
		productMasterRequest.setType(type);
		productMasterRequest.setSubtype(subtype);
		productMasterRequest.setBlocker(isBlocker);

		ProductMaster productMaster = new ProductMaster();
		productMaster.setUuid(uuid);
		productMaster.setProductName(productName);
		productMaster.setProductCode(productCode);
		productMaster.setType(type);
		productMaster.setSubtype(subtype);

		when(beanConverter.convertProductMasterRequestToProductMasterBean(productMasterRequest))
				.thenReturn(productMaster);

		boolean isProductUuidUnique = false;
		String productUuid = "";

		while (!isProductUuidUnique) {
			productUuid = UUID.randomUUID().toString();
			ProductMaster byUuid = productMasterRepository.findByUuid(productUuid);
			if (byUuid == null) {
				isProductUuidUnique = true;
			}
		}
		when(productMasterRepository.save(any(ProductMaster.class))).thenReturn(productMaster);
		Boolean addProduct = productMasterServiceImpl.addProduct(productMasterRequest);
		assertNotNull(addProduct);
		verify(productMasterRepository, atLeast(1)).findByUuid(productUuid);
	}

	@Test
	public void addProduct1() throws InstantiationException {

		ProductMasterRequest productMasterRequest = null;

		try {
			productMasterServiceImpl.addProduct(productMasterRequest);
		} catch (Exception e) {
			// TODO: handle exception
		}

	}

	@Test
	public void addProduct2() throws InstantiationException {

		String uuid = "536a3ca1-3c7c-45b0-844a-e012135c46d3";
		String productCode = "23244";
		String productName = "Product";
		String type = "type";
		String subtype = "sub_type";
		boolean isBlocker = true;

		ProductMasterRequest productMasterRequest = new ProductMasterRequest();
		productMasterRequest.setProductCode(productCode);
		productMasterRequest.setProductName(productName);
		productMasterRequest.setType(type);
		productMasterRequest.setSubtype(subtype);
		productMasterRequest.setBlocker(isBlocker);

		ProductMaster productMaster = new ProductMaster();
		productMaster.setUuid(uuid);
		productMaster.setProductName(productName);
		productMaster.setProductCode(productCode);
		productMaster.setType(type);
		productMaster.setSubtype(subtype);

		when(productMasterRepository.findByProductName(productMasterRequest.getProductName()))
				.thenReturn(productMaster);

		try {
			productMasterServiceImpl.addProduct(productMasterRequest);
		} catch (Exception e) {
			// TODO: handle exception
		}

	}

	@Test
	public void getProductByUuid() throws InstantiationException {
		String uuid = "536a3ca1-3c7c-45b0-844a-e012135c46d3";
		String productCode = "23244";
		String productName = "Product";
		String subtype = "sub_type";

		List<ProductMaster> product = new ArrayList<>();
		ProductMaster prod = new ProductMaster();
		prod.setUuid(uuid);
		prod.setProductCode(productCode);
		prod.setProductName(productName);
		prod.setType(subtype);
		prod.setSubtype(subtype);

		product.add(prod);

		when(productMasterRepository.findByUuid(uuid)).thenReturn(prod);

		List<ProductMasterResponse> productByUuid = productMasterServiceImpl.getProductByUuid(uuid);
		assertNotNull(productByUuid);
		verify(productMasterRepository, atLeast(1)).findByUuid(uuid);
	}

	@Test
	public void getProductByUuid1() throws InstantiationException {
		String uuid = null;
		String productCode = "23244";
		String productName = "Product";
		String subtype = "sub_type";

		List<ProductMaster> product = new ArrayList<>();
		ProductMaster prod = new ProductMaster();
		prod.setProductCode(productCode);
		prod.setProductName(productName);
		prod.setType(subtype);
		prod.setSubtype(subtype);

		product.add(prod);

		when(productMasterRepository.findAll()).thenReturn(product);
		List<ProductMasterResponse> productByUuid = productMasterServiceImpl.getProductByUuid(uuid);
		assertNotNull(productByUuid);
		verify(productMasterRepository, atLeast(1)).findAll();
	}

	@Test
	public void getProductByUuid2() throws InstantiationException {
		String uuid = null;
		String productCode = "23244";
		String productName = "Product";
		String subtype = "sub_type";

		List<ProductMaster> product = new ArrayList<>();
		ProductMaster prod = null;
		product.add(prod);
		try {
			productMasterServiceImpl.getProductByUuid(uuid);
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	@Test
	public void updateProduct() throws InstantiationException {

		String uuid = "536a3ca1-3c7c-45b0-844a-e012135c46d3";
		String productCode = "23244";
		String productName = "Product";
		String type = "type";
		String subtype = "sub_type";
		boolean isBlocker = true;

		ProductMasterRequest productMasterRequest = new ProductMasterRequest();
		productMasterRequest.setUuid(uuid);
		productMasterRequest.setProductCode(productCode);
		productMasterRequest.setProductName(productName);
		productMasterRequest.setType(type);
		productMasterRequest.setSubtype(subtype);
		productMasterRequest.setBlocker(isBlocker);

		ProductMaster pd = new ProductMaster();
		pd.setUuid(uuid);
		pd.setProductCode(productCode);
		pd.setProductName(productName);
		pd.setType(type);
		pd.setSubtype(subtype);
		pd.setBlocker(isBlocker);

		when(productMasterRepository.findByUuid(productMasterRequest.getUuid())).thenReturn(pd);
		when(beanConverter.convertProductMasterRequestToProductMasterBean(productMasterRequest)).thenReturn(pd);
		when(productMasterRepository.save(pd)).thenReturn(pd);
		ProductMaster updateProduct = productMasterServiceImpl.updateProduct(productMasterRequest);
		assertNotNull(updateProduct);
		verify(productMasterRepository, atLeast(1)).findByUuid(productMasterRequest.getUuid());
		verify(productMasterRepository, atLeast(1)).save(pd);
	}

	@Test
	public void updateProduct1() throws InstantiationException {

		String uuid = "536a3ca1-3c7c-45b0-844a-e012135c46d3";
		String productCode = "23244";
		String productName = "Product";
		String type = "type";
		String subtype = "sub_type";
		boolean isBlocker = true;

		ProductMasterRequest productMasterRequest = new ProductMasterRequest();
		productMasterRequest.setUuid(uuid);
		productMasterRequest.setProductCode(productCode);
		productMasterRequest.setProductName(productName);
		productMasterRequest.setType(type);
		productMasterRequest.setSubtype(subtype);
		productMasterRequest.setBlocker(isBlocker);

		ProductMaster pd = null;
		try {
			ProductMaster updateProduct = productMasterServiceImpl.updateProduct(productMasterRequest);
		} catch (Exception e) {
			// TODO: handle exception
		}

	}

	@Test
	public void updateProduct2() throws InstantiationException {

		ProductMasterRequest productMasterRequest = null;

		try {
			ProductMaster updateProduct = productMasterServiceImpl.updateProduct(productMasterRequest);
		} catch (Exception e) {
			// TODO: handle exception
		}

	}

	@Test
	public void getAllProductList() {

		String uuid = "536a3ca1-3c7c-45b0-844a-e012135c46d3";
		String productCode = "23244";
		String productName = "Product";
		String type = "type";
		String subtype = "sub_type";
		boolean isBlocker = true;

		ProductMaster pd = new ProductMaster();
		pd.setUuid(uuid);
		pd.setProductCode(productCode);
		pd.setProductName(productName);
		pd.setType(type);
		pd.setSubtype(subtype);
		pd.setBlocker(isBlocker);

		ProductMasterResponse productMasterResponse = new ProductMasterResponse();
		productMasterResponse.setProduct_name(productName);
		productMasterResponse.setProduct_code(productName);
		productMasterResponse.setType(type);
		productMasterResponse.setSubtype(subtype);
		productMasterResponse.setUuid(uuid);

		List<ProductMaster> findAll = new ArrayList<>();
		findAll.add(pd);

		List<ProductMasterResponse> productMasterResList = new ArrayList<>();
		productMasterResList.add(productMasterResponse);

		when(productMasterRepository.findAll()).thenReturn(findAll);
//			when(beanConverter.convertProductMasterToProductMasteResponse(findAll)).then(productMasterResList);

		List<ProductMasterResponse> allProductList = productMasterServiceImpl.getAllProductList();
		assertNotNull(allProductList);
	}

	@Test
	public void getAllProductList1() {
		List<ProductMaster> findAll = null;
		try {
			List<ProductMasterResponse> allProductList = productMasterServiceImpl.getAllProductList();
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	@Test
	public void getAttributeListByProductCode() {
		String productCode = "3546545";
		String gatewayUuid = "536a3ca1-3c7c-45b0-844a-e012135c46d3";

		LOGGER.info("getAttributeListByProductCode() and fetch productCode and gatewayUUid" + productCode + ""
				+ gatewayUuid);

		List<AttributeResponse> attributeResponseList = new ArrayList<>();

		ProductMaster productMaster = new ProductMaster();
		productMaster.setUuid(gatewayUuid);
		productMaster.setProductCode(productCode);
		productMaster.setProductName("productName");
		productMaster.setType("type");
		productMaster.setSubtype("subtype");
		productMaster.setBlocker(true);

		when(productMasterRepository.findByUuid(productCode)).thenReturn(productMaster);

		Attribute attribute = new Attribute();
		attribute.setAttributeName("attribute_name");
		attribute.setUuid("536a3ca1-3c7c-45b0-844a-e012135c46d3");
		attribute.setAttributeValue("value");

		List<Attribute> attributeList = new ArrayList<>();
		attributeList.add(attribute);

		AttributeValue attributeValue = new AttributeValue();
		attributeValue.setValue("value");

		attributeList.forEach(attribute1 -> {
			AttributeResponse attributeResponse = new AttributeResponse();
			attributeResponse.setName(attribute.getAttributeName());
			attributeResponse.setAttribute_uuid(attribute.getUuid());
			attributeResponse.setThreshold_Value(attributeValue.getValue());
			// attributeResponse.setValue(attribute.getAttributeValue());
			attributeResponseList.add(attributeResponse);
		});

		List<AttributeResponse> attributeListByProductCode = productMasterServiceImpl
				.getAttributeListByProductCode(productCode, gatewayUuid);
		assertNotNull(attributeListByProductCode);
	}

	@Test
	public void getProductByName() {

		String name = "name";
		List<Attribute> at = new ArrayList<>();

		Attribute attribute = new Attribute();
		attribute.setAttributeName("attribute_name");
		attribute.setUuid("536a3ca1-3c7c-45b0-844a-e012135c46d3");

		ProductMaster pd = new ProductMaster();
		pd.setUuid("536a3ca1-3c7c-45b0-844a-e012135c46d3");
		pd.setProductCode("product");
		pd.setProductName(name);
		pd.setSubtype("subtype");
		pd.setType("type");
		pd.setBlocker(true);

		when(productMasterRepository.findByProductName(name)).thenReturn(pd);
		when(atttributeRespo.findByProductMasterUuid(pd.getUuid())).thenReturn(at);
		List<Attribute> productByName = productMasterServiceImpl.getProductByName(name);
		assertNotNull(productByName);
		verify(productMasterRepository, atLeast(1)).findByProductName(name);
		// verify(productMasterServiceImpl,atLeastOnce()).getProductByName(name);
	}

	@Test
	public void getProductByName1() {

		String name = null;

		ProductMaster pd = null;

		List<Attribute> productByName = productMasterServiceImpl.getProductByName(name);
	}
}