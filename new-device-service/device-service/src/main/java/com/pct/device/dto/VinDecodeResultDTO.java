package com.pct.device.dto;

import com.pct.device.payload.AssetsPayload;


public class VinDecodeResultDTO {

	private String result;
	private String message;
	private NHTSAResultDTO nhtsaResultDTO;

	public VinDecodeResultDTO() {

	}

	public VinDecodeResultDTO(String result, String message, NHTSAResultDTO nhtsaResultDTO) {
		this.result = result;
		this.message = message;
		this.nhtsaResultDTO = nhtsaResultDTO;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public NHTSAResultDTO getVehicleDTO() {
		return nhtsaResultDTO;
	}

	public void setVehicleDTO(NHTSAResultDTO nhtsaResultDTO) {
		this.nhtsaResultDTO = nhtsaResultDTO;
	}

	@Override
	public String toString() {
		return "VinDecodeResponseDTO [result=" + result + ", message=" + message + ", assetsPayload=" + nhtsaResultDTO + "]";
	}
}
