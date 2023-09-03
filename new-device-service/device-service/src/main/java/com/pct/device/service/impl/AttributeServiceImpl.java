package com.pct.device.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import com.pct.common.model.Attribute;
import com.pct.common.model.ProductMaster;
import com.pct.common.payload.AttributeDetails;
import com.pct.common.payload.AttributeRequest;
import com.pct.common.payload.AttributeResponse;
import com.pct.common.payload.ProductMasterResponse;
import com.pct.device.exception.DeviceException;
import com.pct.device.repository.IAttributeRepository;
import com.pct.device.repository.IProductMasterRepository;
import com.pct.device.service.IAttributeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class AttributeServiceImpl implements IAttributeService {

    @Autowired
    private IAttributeRepository attributeRepository;
    
    @Autowired
    private IProductMasterRepository productMasterRepository;

    @Override
    public Boolean addAttribute(AttributeRequest attributeRequest) throws InstantiationException {

        if (attributeRequest != null) {
            for (AttributeDetails attributeDetails : attributeRequest.getAttributeDetails()) {
                if (attributeDetails != null) {
                    Attribute attribute = new Attribute();
                    attribute.setAttributeName(attributeDetails.getAttributeName());
                    attribute.setAttributeValue(attributeDetails.getAttributeValue());
                    attribute.setApplicable(attributeDetails.isApplicable());
                   ProductMaster prod = productMasterRepository.findByUuid(attributeRequest.getProductUuid());
                   if(prod==null || prod.getUuid()==null) {
                	   throw new DeviceException("No Product Found Curresponding to given ProuductUuid");
                   }
                    attribute.setProductMaster(prod);
                    boolean isAttributeUuidUnique = false;
                    String attributeUuid = "";
                    while (!isAttributeUuidUnique) {
                        attributeUuid = UUID.randomUUID().toString();
                        Attribute byUuid = attributeRepository.findByUuid(attributeUuid);
                        if (byUuid == null) {
                            isAttributeUuidUnique = true;
                        }
                    }
                    attribute.setUuid(attributeUuid);
                    List<Attribute> att = attributeRepository.findByProductMasterUuidAndAttributeName(attribute.getAttributeName(),attribute.getProductMaster().getUuid());
                    if(att.size() > 0) {
                 	   throw new DeviceException("Attribute is already present for this name");
                    }
                    attributeRepository.save(attribute);
                }

            }

        }
        else {
            throw new InstantiationException("AttributeRequest JSON Can't be NULL");
        }
        return  Boolean.TRUE;
    }

    @Override
    public AttributeResponse getAttibuteByUuid(final String id) {
        AttributeResponse attributeResponse=new AttributeResponse();
    /*    final Attribute attribute = attributeRepository.findByUuid(id);
        attributeResponse.setProductUuid(attribute.getProduct_uuid());
        attributeResponse.setUuid(attribute.getUuid());
        attributeResponse.setId(attribute.getId());*/
        return attributeResponse;
    }

    @Override
    public AttributeResponse update(final AttributeRequest attributeRequest) throws InstantiationException {
//        if(attributeRequest.getId() == null){
//            throw new InstantiationException("Id Can't be NULL");
//         }
//         Attribute attribute = attributeRepository.findByUuid(attributeRequest.getUuid());
//
//        if(attributeRequest.getProductUuid() !=null){
//            attribute.setProduct_uuid(attributeRequest.getProductUuid());
//        }
//        if(attributeRequest.getAttributeName() !=null){
//            attribute.setAttributeName(attributeRequest.getAttributeName());
//        }
//        if(attributeRequest.getAttributeValue() !=null){
//            attribute.setAttributeValue(attributeRequest.getAttributeValue());
//        }
//        attributeRepository.save(attribute);

        return null;
    }

    @Override
    public Page<AttributeResponse> getAllAttributes( final Pageable pageable) {
        Page<Attribute> all = attributeRepository.findAll( pageable);

        List<AttributeResponse> attributeResponseList = new ArrayList<>();

        all.forEach(attribute -> {
            AttributeResponse attributeResponse = attributeToAttributeResponse(attribute);
            attributeResponseList.add(attributeResponse);
        });
        Page<AttributeResponse> pageOfAttributeResponse = new PageImpl<>(attributeResponseList, pageable, all.getTotalElements());
        return pageOfAttributeResponse;
    }

    @Override
    public List<AttributeResponse> getAllAttributeList() {
         List<Attribute> all = attributeRepository.findAll();
        List<AttributeResponse> attributeResponseList = new ArrayList<>();

        all.forEach(attribute -> {
            AttributeResponse attributeResponse = attributeToAttributeResponse(attribute);
             attributeResponseList.add(attributeResponse);

        });
        return attributeResponseList;
    }

    public AttributeResponse attributeToAttributeResponse(Attribute attribute) {
        AttributeResponse response = new AttributeResponse();
        response.setName(attribute.getAttributeName());
        response.setThreshold_Value(attribute.getAttributeValue());
        response.setAttribute_uuid(attribute.getUuid());
        ProductMaster prod =attribute.getProductMaster();
        ProductMasterResponse pmr = new ProductMasterResponse();
        pmr.setUuid(prod.getUuid());
        pmr.setProduct_name(prod.getProductName());
        pmr.setProduct_code(prod.getProductCode());
        pmr.setType(prod.getType());
        pmr.setId(prod.getId());
        response.setProduct_response(pmr);
        return response;
    }

}

