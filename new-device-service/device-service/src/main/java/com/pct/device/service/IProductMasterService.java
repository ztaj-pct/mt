package com.pct.device.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.pct.common.model.Attribute;
import com.pct.common.model.ProductMaster;
import com.pct.common.payload.AttributeRequest;
import com.pct.common.payload.AttributeResponse;
import com.pct.common.payload.ProductMasterRequest;
import com.pct.common.payload.ProductMasterResponse;

public interface IProductMasterService {

    Boolean addProduct(ProductMasterRequest productMasterRequest) throws InstantiationException;
    ProductMasterResponse getProductByUuid(String uuid);
    ProductMasterResponse update(ProductMasterRequest productMasterRequest) throws InstantiationException;
    List<ProductMasterResponse> getAllProductList();
    List<AttributeResponse>getAttributeListByProductCode(String productCode,String gatewayUuid);
    public List<Attribute> getProductByName(String name);
    

}

