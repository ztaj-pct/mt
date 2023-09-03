package com.pct.device.service.impl;

import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.pct.device.dto.NHTSAResponseDTO;
import com.pct.device.dto.NHTSAResultDTO;
import com.pct.device.dto.VinDecodeResultDTO;
import com.pct.device.payload.AssetsPayload;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
public class NHTSAVinDecoderServiceTest {

	private static final Logger logger = LoggerFactory.getLogger(NHTSAVinDecoderServiceTest.class);

	@InjectMocks
	private NHTSAVinDecoderService service;

	@Mock
	private RestTemplate restClient;

	private static final int NHTSA_MAX_VIN_SIZE = 50;

	@Test
	public void decodeVins() {
		String[] strArray = { "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r",
				"s", "t", "u", "v", "w", "x", "y", "z", "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m",
				"n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z" };

		List<String> vins = new ArrayList<>(Arrays.asList(strArray));
//		vins.add("4T1BE30K25U942463");
//		vins.add("1GBJG31U431165751");
//		vins.add("2FMDK39C29BA44891");

		List<List<String>> vinBatches = new ArrayList<>();

		int start = 0;
		int end = NHTSA_MAX_VIN_SIZE;

		vinBatches.add(vins.subList(start, end));
		start = end;
		end = end + NHTSA_MAX_VIN_SIZE;

		start = (vins.size() / NHTSA_MAX_VIN_SIZE) * NHTSA_MAX_VIN_SIZE;
		end = vins.size();
		vinBatches.add(vins.subList(start, end));

		Map<String, VinDecodeResultDTO> resultMap = new HashMap<>();

		for (List<String> vinBatch : vinBatches) {
			String vinsToDecode = StringUtils.join(vinBatch, ";");
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

			MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
			map.add("DATA", vinsToDecode);
			map.add("format", "JSON");
			HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(map,
					headers);

			try {
				String successfulDecodingMessage = "0 - VIN decoded clean. Check Digit (9th position) is correct";

				NHTSAResponseDTO nhtsaResponse = new NHTSAResponseDTO();
				nhtsaResponse.setCount(5);
				nhtsaResponse.setMessage("message");
				nhtsaResponse.setSearchCriteria("criteria");

				NHTSAResultDTO nhtsaResultDTO = new NHTSAResultDTO();
				nhtsaResultDTO.setABS("abs");
				nhtsaResultDTO.setAdaptiveCruiseControl("cruise");
				nhtsaResultDTO.setAdaptiveHeadlights("headlights");
				nhtsaResultDTO.setAEB("aeb");
				nhtsaResultDTO.setAirBagLocCurtain("airbagloccurtain");
				nhtsaResultDTO.setErrorText("0 - VIN decoded clean. Check Digit (9th position) is correct");
				nhtsaResultDTO.setVIN("vin");
				nhtsaResultDTO.setModelYear("1993");
				nhtsaResultDTO.setMake("make");
				nhtsaResultDTO.setBodyClass("Truck");

				List<NHTSAResultDTO> results = new ArrayList<NHTSAResultDTO>();
				results.add(nhtsaResultDTO);

				nhtsaResponse.setResults(results);

				when(this.restClient.postForObject("https://vpic.nhtsa.dot.gov/api/vehicles/DecodeVINValuesBatch/",
						request, NHTSAResponseDTO.class)).thenReturn(nhtsaResponse);

				for (NHTSAResultDTO nhtsaResult : nhtsaResponse.getResults()) {

					AssetsPayload assetsPayload = new AssetsPayload();
					assetsPayload.setVin(nhtsaResult.getVIN());
					assetsPayload.setYear(nhtsaResult.getModelYear());
					assetsPayload.setManufacturer(nhtsaResult.getMake());

					String bodyClass = nhtsaResult.getBodyClass();
					bodyClass = bodyClass.substring(0, 5);

					resultMap.put(nhtsaResult.getVIN(),
							new VinDecodeResultDTO("success", nhtsaResult.getErrorCode(), nhtsaResult));
				}
			} catch (Exception e) {
				System.out.println("Error:" + e);
			}
		}

		service.decodeVins(vins);
	}

	@Test
	public void decodeVins1() {

		List<String> vins = new ArrayList<>();
		service.decodeVins(vins);
	}

	@Test
	public void decodeVins2() {
		String[] strArray = { "a", "b", "c" };

		List<String> vins = new ArrayList<>(Arrays.asList(strArray));

		List<List<String>> vinBatches = new ArrayList<>();
		service.decodeVins(vins);
	}

	@Test
	public void decodeVins3() {
		String[] strArray = { "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r",
				"s", "t", "u", "v", "w", "x", "y", "z", "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m",
				"n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x" };

		List<String> vins = new ArrayList<>(Arrays.asList(strArray));
		System.out.println("///////" + vins.size());
		List<List<String>> vinBatches = new ArrayList<>();

		NHTSAResponseDTO nhtsaResponse = null;
		String successfulDecodingMessage = "successfulDecodingMessage";
		NHTSAResultDTO nhtsaResult = null;

		service.decodeVins(vins);
	}

	@Test
	public void decodeVins4() {
		String[] strArray = { "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r",
				"s", "t", "u", "v", "w", "x", "y", "z", "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m",
				"n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z" };

		List<String> vins = new ArrayList<>(Arrays.asList(strArray));

		List<List<String>> vinBatches = new ArrayList<>();

		int start = 0;
		int end = NHTSA_MAX_VIN_SIZE;

		vinBatches.add(vins.subList(start, end));
		start = end;
		end = end + NHTSA_MAX_VIN_SIZE;

		start = (vins.size() / NHTSA_MAX_VIN_SIZE) * NHTSA_MAX_VIN_SIZE;
		end = vins.size();
		vinBatches.add(vins.subList(start, end));

		Map<String, VinDecodeResultDTO> resultMap = new HashMap<>();

		for (List<String> vinBatch : vinBatches) {
			String vinsToDecode = StringUtils.join(vinBatch, ";");
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

			MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
			map.add("DATA", vinsToDecode);
			map.add("format", "JSON");
			HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(map,
					headers);

			try {
				String successfulDecodingMessage = "1 - VIN decoded clean. Check Digit (9th position) is incorrect";

				NHTSAResponseDTO nhtsaResponse = new NHTSAResponseDTO();
				nhtsaResponse.setCount(5);
				nhtsaResponse.setMessage("message");
				nhtsaResponse.setSearchCriteria("criteria");

				NHTSAResultDTO nhtsaResultDTO = new NHTSAResultDTO();
				nhtsaResultDTO.setABS("abs");
				nhtsaResultDTO.setAdaptiveCruiseControl("cruise");
				nhtsaResultDTO.setAdaptiveHeadlights("headlights");
				nhtsaResultDTO.setAEB("aeb");
				nhtsaResultDTO.setAirBagLocCurtain("airbagloccurtain");
				nhtsaResultDTO.setErrorText(null);
				nhtsaResultDTO.setVIN("vin");
				nhtsaResultDTO.setModelYear("1993");
				nhtsaResultDTO.setMake("make");
				nhtsaResultDTO.setBodyClass("Truck");

				List<NHTSAResultDTO> results = new ArrayList<NHTSAResultDTO>();
				results.add(nhtsaResultDTO);

				nhtsaResponse.setResults(results);

				when(this.restClient.postForObject("https://vpic.nhtsa.dot.gov/api/vehicles/DecodeVINValuesBatch/",
						request, NHTSAResponseDTO.class)).thenReturn(nhtsaResponse);

				for (NHTSAResultDTO nhtsaResult : nhtsaResponse.getResults()) {

					AssetsPayload assetsPayload = new AssetsPayload();
					assetsPayload.setVin(nhtsaResult.getVIN());
					assetsPayload.setYear(nhtsaResult.getModelYear());
					assetsPayload.setManufacturer(nhtsaResult.getMake());

					String bodyClass = nhtsaResult.getBodyClass();
					bodyClass = bodyClass.substring(0, 5);

					resultMap.put(nhtsaResult.getVIN(),
							new VinDecodeResultDTO("success", nhtsaResult.getErrorCode(), nhtsaResult));
				}
			} catch (Exception e) {
				System.out.println("Error:" + e);
			}
		}

		service.decodeVins(vins);
	}
}