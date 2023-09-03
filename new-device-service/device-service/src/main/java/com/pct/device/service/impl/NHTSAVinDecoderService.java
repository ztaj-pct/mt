package com.pct.device.service.impl;

import com.google.common.collect.Maps;
import com.pct.device.dto.NHTSAResponseDTO;
import com.pct.device.dto.NHTSAResultDTO;
import com.pct.device.dto.VinDecodeResultDTO;
import com.pct.device.exception.DeviceException;
import com.pct.device.payload.AssetsPayload;
import com.pct.device.service.VinDecoderService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class NHTSAVinDecoderService implements VinDecoderService {

	private static final Logger logger = LoggerFactory.getLogger(NHTSAVinDecoderService.class);

	@Autowired
	private RestTemplate restClient;

	private static final int NHTSA_MAX_VIN_SIZE = 50;

	@Override
	public Map<String, VinDecodeResultDTO> decodeVins(List<String> vins) {
		if (vins.isEmpty()) {
			return Maps.newHashMap();
		}
		List<List<String>> vinBatches = new ArrayList<>();
		if(vins.size() > NHTSA_MAX_VIN_SIZE) {
			int start = 0;
			int end = NHTSA_MAX_VIN_SIZE;
			while(end < vins.size()) {
				vinBatches.add(vins.subList(start, end));
				start = end;
				end = end + NHTSA_MAX_VIN_SIZE;
			}
			if(vins.size() % NHTSA_MAX_VIN_SIZE > 0) {
				start = (vins.size() / NHTSA_MAX_VIN_SIZE) * NHTSA_MAX_VIN_SIZE;
				end = vins.size();
				vinBatches.add(vins.subList(start, end));
			}
		} else {
			vinBatches.add(vins);
		}
		Map<String, VinDecodeResultDTO> resultMap = new HashMap<>();
		for(List<String> vinBatch : vinBatches) {
			String vinsToDecode = StringUtils.join(vinBatch, ";");
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

			MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
			map.add("DATA", vinsToDecode);
			map.add("format", "JSON");
			HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(map, headers);

			try {
				//TODO move URL to config
				NHTSAResponseDTO nhtsaResponse = this.restClient.postForObject(
						"https://vpic.nhtsa.dot.gov/api/vehicles/DecodeVINValuesBatch/", request, NHTSAResponseDTO.class);
				String successfulDecodingMessage = "0 - VIN decoded clean. Check Digit (9th position) is correct";

				for (NHTSAResultDTO nhtsaResult : nhtsaResponse.getResults()) {

					if (nhtsaResult.getErrorText() != null && nhtsaResult.getErrorText().equals(successfulDecodingMessage)) {
						AssetsPayload assetsPayload = new AssetsPayload();
						assetsPayload.setVin(nhtsaResult.getVIN());
						assetsPayload.setYear(nhtsaResult.getModelYear());
						assetsPayload.setManufacturer(nhtsaResult.getMake());
						// TODO check with Jean if we want to store duty class in the Additional properties field or it could be removed entirely
				/* vehicleDTO.setEngineMake(nhtsaResult.getEngineManufacturer());
				vehicleDTO.setEngineModel(nhtsaResult.getEngineModel());
				vehicleDTO.setEngineDisplacementLitres(nhtsaResult.getDisplacementL());
				vehicleDTO.setFuelType(nhtsaResult.getFuelTypePrimary());
	
				// Based on https://en.wikipedia.org/wiki/Truck_classification#Table_of_US_GVWR_classifications
				String weightClassString = nhtsaResult.getGVWR();
				if (weightClassString.startsWith("Class 9")) {
					vehicleDTO.setDutyClass("SUPER_HEAVY");
				} else if (weightClassString.startsWith("Class 7") || weightClassString.startsWith("Class 8")) {
					vehicleDTO.setDutyClass("HEAVY");
				} else if (weightClassString.startsWith("Class 4") || weightClassString.startsWith("Class 5")
						|| weightClassString.startsWith("Class 6")) {
					vehicleDTO.setDutyClass("MEDIUM");
				} else if (weightClassString.startsWith("Class 1") || weightClassString.startsWith("Class 2")
						|| weightClassString.startsWith("Class 3")) {
					vehicleDTO.setDutyClass("LIGHT");
				} */

						// See https://vpic.nhtsa.dot.gov/api/vehicles/getvehiclevariablevalueslist/Body%20Class?format=json
						// For possible values of Body Type field in NHTSA API.
						if (StringUtils.isNotBlank(nhtsaResult.getBodyClass())) {
							String bodyClass = nhtsaResult.getBodyClass();
							if (bodyClass.startsWith("Truck")) {
								bodyClass = bodyClass.substring(0, 5);
							}
//					assetsPayload.setAssetType(bodyClass);
						} else {
							// Required field so we need some value in case there is no body class returned.
//					assetsPayload.setAssetType("Unknown");
						}

						resultMap.put(nhtsaResult.getVIN(), new VinDecodeResultDTO("success", nhtsaResult.getErrorCode(), nhtsaResult));
					} else {
						resultMap.put(nhtsaResult.getVIN(), new VinDecodeResultDTO("failure", nhtsaResult.getErrorCode(), null));
					}
				}
			} catch (Exception e) {
				logger.error("Exception while decoding VIN", e);
				for(String vin : vinBatch) {
					resultMap.put(vin, new VinDecodeResultDTO("decodeFailure", null, null));
				}
			}
		}
		return resultMap;
	}
}
