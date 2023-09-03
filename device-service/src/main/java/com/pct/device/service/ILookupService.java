package com.pct.device.service;

import com.pct.device.exception.DeviceException;
import com.pct.device.payload.LookupPayload;

import java.util.List;
import java.util.Map;

public interface ILookupService {

    Map<String, List<LookupPayload>> getAllAssetConfiguration(List<String> type) throws DeviceException;

	void saveLookup(LookupPayload saveLookupPayload);

}
