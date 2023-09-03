package com.pct.device.service;

import com.pct.device.dto.VinDecodeResultDTO;

import java.util.List;
import java.util.Map;

public interface VinDecoderService {
	
	/**
	 * Decode a set of VINs using the Vin decoder service.
	 * @param vins list of VINs to decode.
	 *
	 * @return map keyed by each VIN passed and the corresponding result of decoding in {@link VinDecodeResultDTO}
	 */
	Map<String, VinDecodeResultDTO> decodeVins(List<String> vins);
}
