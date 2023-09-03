package com.pct.device.service;

import java.util.List;

import com.pct.common.payload.AttributeValueRequest;
import com.pct.device.dto.AttributeResponseDTO;
import com.pct.device.dto.AttributeValueResponseDTO;


public interface IAttributeValueService {

   Boolean addAttributeValue(AttributeValueRequest attributeValueRequest) throws InstantiationException;
   public List<AttributeValueResponseDTO> getAttributeByGatewayDeviceId(String deviceId);
   public List<AttributeResponseDTO> getAttributeValueByGatewayDeviceId(String deviceId);

}

