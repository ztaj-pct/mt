package com.pct.device.service;

import java.util.List;

import com.pct.common.model.AttributeValue;
import com.pct.common.payload.AttributeValueRequest;
import com.pct.device.dto.AttributeValueResponseDTO;


public interface IAttributeValueService {

   Boolean addAttributeValue(AttributeValueRequest attributeValueRequest) throws InstantiationException;
   public List<AttributeValueResponseDTO> getAttributeByGatewayDeviceId(String deviceId);
   public List<AttributeValue> getAttributeValueByGatewayDeviceId(String deviceId);

}

