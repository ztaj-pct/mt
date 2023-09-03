package com.pct.device.service;

import java.util.List;


import com.pct.device.payload.AttributeRequest;
import com.pct.device.payload.AttributeResponse; 
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IAttributeService {

    Boolean addAttribute(com.pct.device.payload.AttributeRequest attributeRequest) throws InstantiationException;
    com.pct.device.payload.AttributeResponse getAttibuteByUuid(String uuid);
    AttributeResponse update(AttributeRequest attributeRequest) throws InstantiationException;
    Page<AttributeResponse> getAllAttributes(Pageable pageable);
    List<AttributeResponse> getAllAttributeList();

}

