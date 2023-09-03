package com.pct.device.service;

import java.util.List;
import com.pct.common.payload.AttributeRequest;
import com.pct.common.payload.AttributeResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IAttributeService {

    Boolean addAttribute(AttributeRequest attributeRequest) throws InstantiationException;
    AttributeResponse getAttibuteByUuid(String uuid);
    AttributeResponse update(AttributeRequest attributeRequest) throws InstantiationException;
    Page<AttributeResponse> getAllAttributes(Pageable pageable);
    List<AttributeResponse> getAllAttributeList();

}

