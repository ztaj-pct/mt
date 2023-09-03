package com.pct.device.service.impl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pct.device.exception.DeviceException;
import com.pct.device.model.Lookup;
import com.pct.device.payload.LookupPayload;
import com.pct.device.repository.ILookupRepository;
import com.pct.device.service.ILookupService;
import com.pct.device.util.BeanConverter;

@Service
public class LookupServiceImpl implements ILookupService {

	@Autowired
    ILookupRepository lookupRepository;

    @Autowired
    BeanConverter beanConverter;
	
	
	
	@Override
	public Map<String, List<LookupPayload>> getAllAssetConfiguration(List<String> fields) throws DeviceException {
		
		   List<Lookup> lookups = lookupRepository.findByField(fields);
	        if (lookups.size() == 0) {
	            throw new DeviceException("No Asset Configurations Found");
	        }
	        List<LookupPayload> lookupPayloads = lookups.stream().
	                map(beanConverter::assetConfigurationToAssetConfigurationPayload).collect(Collectors.toList());
	        Map<String, List<LookupPayload>> assetConfigurationsMap = lookupPayloads.stream().collect(Collectors.groupingBy(asc -> asc.getField(), Collectors.toList()));
	        return assetConfigurationsMap;
	}

	@Override
	public void saveLookup(LookupPayload saveLookupPayload) {
		
		if (saveLookupPayload == null) {
            throw new DeviceException("No Lookup Configurations Found");
        }
		Lookup lookup = beanConverter.lookupPayloadToLookup(saveLookupPayload);
		lookupRepository.save(lookup); 	
	}

}
