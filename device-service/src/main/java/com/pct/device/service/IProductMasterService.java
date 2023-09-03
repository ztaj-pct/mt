package com.pct.device.service;

import java.util.List;

import com.pct.common.model.Attribute;
import com.pct.common.model.ProductMaster;
import com.pct.device.payload.AttributeResponse;
import com.pct.device.payload.ProductMasterRequest;
import com.pct.device.payload.ProductMasterResponse;

public interface IProductMasterService {

	Boolean addProduct(ProductMasterRequest productMasterRequest) throws InstantiationException;

	List<ProductMasterResponse> getProductByUuid(String uuid) throws InstantiationException;

	ProductMaster updateProduct(ProductMasterRequest productMasterRequest) throws InstantiationException;

	List<ProductMasterResponse> getAllProductList();

	List<AttributeResponse> getAttributeListByProductCode(String productCode, String gatewayUuid);

	public List<Attribute> getProductByName(String name);

}
