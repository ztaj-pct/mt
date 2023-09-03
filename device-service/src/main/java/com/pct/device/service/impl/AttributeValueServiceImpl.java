package com.pct.device.service.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.pct.common.constant.GatewayType;
import com.pct.common.model.Attribute;
import com.pct.common.model.AttributeValue;
import com.pct.common.model.Device;
//import com.pct.common.model.Gateway;
import com.pct.common.payload.AttributeValueRequest;
import com.pct.device.dto.AttributeResponseDTO;
import com.pct.device.dto.AttributeValueResponseDTO;
import com.pct.device.exception.DeviceException;
import com.pct.device.repository.IAttributeRepository;
import com.pct.device.repository.IAttributeValueRepository;
import com.pct.device.repository.IDeviceRepository;
//import com.pct.device.repository.IGatewayRepository;
import com.pct.device.service.IAttributeValueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AttributeValueServiceImpl implements IAttributeValueService {



    @Autowired
    private IAttributeValueRepository attributeValueRepository;
    
    @Autowired
    private IDeviceRepository gatewayRepository;

    @Autowired
    private IAttributeRepository attributeRepository;
    
    @Override
    public Boolean addAttributeValue(AttributeValueRequest attributeValueRequest) throws InstantiationException {

        if (attributeValueRequest != null) {
                	 if(attributeValueRequest.getAttributeUUID()!=null && !attributeValueRequest.getAttributeUUID().isEmpty() &&
                			 attributeValueRequest.getDeviceId()!=null && !attributeValueRequest.getDeviceId().isEmpty() && 
                			 attributeValueRequest.getStatus()!=null && !attributeValueRequest.getDeviceId().isEmpty() && 
                			 attributeValueRequest.getValue()!=null && !attributeValueRequest.getDeviceId().isEmpty() &&
                			 attributeValueRequest.getType().getValue()!=null && !attributeValueRequest.getDeviceId().isEmpty())
                	 {
                     AttributeValue attributeValue=new AttributeValue();
                     Device gateway = new Device();
                     if(attributeValueRequest.getType()!=null && attributeValueRequest.getType().getValue().equalsIgnoreCase(GatewayType.GATEWAY.getValue())) {
                    	 gateway  = gatewayRepository.findByImei(attributeValueRequest.getDeviceId());
                     if(gateway==null ||gateway.getUuid()==null) {
                    	 throw new DeviceException("Gateway does not found for given IMEI number");
                     }
                     }
                     if(attributeValueRequest.getType()!=null && attributeValueRequest.getType().getValue().equalsIgnoreCase(GatewayType.BEACON.getValue())) {
                     	 gateway  = gatewayRepository.findByMac_address(attributeValueRequest.getDeviceId());
                         if(gateway==null ||gateway.getUuid()==null) {
                        	 throw new DeviceException("Gateway does not found for given IMEI number");
                         }
                        } 
                     attributeValue.setDevice(gateway);
                    Attribute att =attributeRepository.findByUuid(attributeValueRequest.getAttributeUUID());
                    if(att==null ||att.getUuid()==null) {
                   	 throw new DeviceException("Attribute does not found for given Attribute Uuid number");
                    }
                    AttributeValue attr = attributeValueRepository.findByDeviceImeiAndAttribute(gateway.getImei(), att.getUuid());
                    if(attr != null && attr.getUuid() != null) {
                    	attr.setStatus(attributeValueRequest.getStatus());
                    	attr.setValue(attributeValueRequest.getValue());
                    	attr.setUpdatedOn(Instant.now());
                        attributeValueRepository.save(attr);
                    }else {
                    attributeValue.setAttribute(att);
                    attributeValue.setStatus(attributeValueRequest.getStatus());
                    attributeValue.setType(attributeValueRequest.getType().getValue());
                    attributeValue.setValue(attributeValueRequest.getValue());
                    attributeValue.setCreatedOn(Instant.now());
                    boolean isAttributeValueUuidUnique = false;
                    String attributeValueUuid = "";
                    while (!isAttributeValueUuidUnique) {
                        attributeValueUuid = UUID.randomUUID().toString();
                        AttributeValue byUuid = attributeValueRepository.findByUuid(attributeValueUuid);
                        if (byUuid == null) {
                            isAttributeValueUuidUnique = true;
                        }
                    }
                     attributeValue.setUuid(attributeValueUuid);
                     attributeValueRepository.save(attributeValue);
                    }
                }else {
                	 throw new DeviceException("Attribute values should not null");
                	
                }
            }
        else {
            throw new InstantiationException("AttributeValueRequest JSON Can't be NULL");
        }
        return  Boolean.TRUE;
    }
/*
    @Override
    public AttributeResponse getAttibuteByUuid(final String id) {
        AttributeResponse attributeResponse=new AttributeResponse();
        final Attribute attribute = attributeRepository.findByUuid(id);
        attributeResponse.setProductUuid(attribute.getProduct_uuid());
        attributeResponse.setUuid(attribute.getUuid());
        attributeResponse.setId(attribute.getId());
        return attributeResponse;
    }

    @Override
    public AttributeResponse update(final AttributeRequest attributeRequest) throws InstantiationException {
        if(attributeRequest.getId() == null){
            throw new InstantiationException("Id Can't be NULL");
         }
         Attribute attribute = attributeRepository.findByUuid(attributeRequest.getUuid());

        if(attributeRequest.getProductUuid() !=null){
            attribute.setProduct_uuid(attributeRequest.getProductUuid());
        }
        if(attributeRequest.getAttributeName() !=null){
            attribute.setAttributeName(attributeRequest.getAttributeName());
        }
        if(attributeRequest.getAttributeValue() !=null){
            attribute.setAttributeValue(attributeRequest.getAttributeValue());
        }
        attributeRepository.save(attribute);

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
        response.setUuid(attribute.getUuid());
        response.setProductUuid(attribute.getProduct_uuid());
        response.setAttributeName(attribute.getAttributeName());
        response.setAttributeValue(attribute.getAttributeValue());
        response.setId(attribute.getId());
        return response;
    }*/
    
	@Override
	public List<AttributeValueResponseDTO> getAttributeByGatewayDeviceId(String deviceId) {
		List<AttributeValue> list = new ArrayList<AttributeValue>();
		List<AttributeValueResponseDTO> resposneList = new ArrayList<>();
		if(deviceId != null) {
			 list = attributeValueRepository.findByDeviceImei(deviceId);
			 if(list.size()==0) {
				 list = attributeValueRepository.findByDeviceImei(deviceId); 
				 if(list.size()==0) {
						 throw new DeviceException("Attribute Value is not present for given Device Id");	
				 }
			 }
              for(AttributeValue att : list) {
            	  AttributeValueResponseDTO avr = new AttributeValueResponseDTO();
            	  avr.setDevice_id(att.getDevice().getImei());
            	  avr.setPower_source_name(att.getAttribute().getAttributeName());
            	  avr.setThreshold_value(att.getAttribute().getAttributeValue());
            	  avr.setActual_value(att.getValue());
            	  avr.setStatus(att.getStatus());
            	  resposneList.add(avr);
              }
			 
		}else {
			 throw new DeviceException("Device Id can not be null");	
		}
		return resposneList;
	}
	
	@Override
	public List<AttributeResponseDTO> getAttributeValueByGatewayDeviceId(String deviceId) {
		List<AttributeValue> lists = new ArrayList<AttributeValue>();
		List<AttributeResponseDTO> attributeDetails = new ArrayList<AttributeResponseDTO>();
		if (deviceId != null) {
			lists = attributeValueRepository.findByDeviceImei(deviceId);
			for (AttributeValue list : lists) {
				AttributeResponseDTO aResponse = new AttributeResponseDTO();
				aResponse.setAttributeName(list.getAttribute().getAttributeName());// Due to deserialization issue only
																					// attribute is returned.
				aResponse.setStatus(list.getStatus());
				aResponse.setAttributeValue(list.getValue());
				attributeDetails.add(aResponse);
			}
			if (lists.size() == 0) {
				lists = attributeValueRepository.findByDeviceMacAddress(deviceId);
				for (AttributeValue list : lists) {
					AttributeResponseDTO aResponse = new AttributeResponseDTO();
					aResponse.setAttributeName(list.getAttribute().getAttributeName());// Due to deserialization issue
																						// only attribute is returned.
					aResponse.setStatus(list.getStatus());
					aResponse.setAttributeValue(list.getValue());
					attributeDetails.add(aResponse);
				}
			}
		} else {
			throw new DeviceException("Device Id can not be null");
		}
		return attributeDetails;
	}
}

