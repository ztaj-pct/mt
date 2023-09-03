package com.pct.device.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.pct.common.constant.AssetStatus;
import com.pct.common.dto.ResponseBodyDTO;
import com.pct.common.util.Context;
import com.pct.device.dto.AssetDTO;
import com.pct.device.dto.AssetResponseDTO;
import com.pct.device.dto.AssetVinSearchDTO;
import com.pct.device.exception.DeviceException;
import com.pct.device.payload.AssetsPayloadMobile;

import java.util.List;

public interface IAssetService {

    List<AssetDTO> getAssetByIdOrVinNumber(String vin, String assetId, String accountNumber) throws DeviceException;

    List<AssetDTO> getAsset(String accountNumber, AssetStatus status) throws DeviceException;

    List<AssetResponseDTO> getAssets(String accountNumber, String vin, String assignedName, String status, String eligibleGateway,Context context) throws Exception;

    AssetVinSearchDTO getAssetVinSearch(String vin) throws Exception;

    ResponseBodyDTO<AssetResponseDTO> addAsset(AssetsPayloadMobile assets, Long userId, Boolean isMoblieApi) throws DeviceException, JsonProcessingException;

    public List<AssetResponseDTO> getAssetsForCAN(String accountNumber, String vin, String assignedName, String status, String eligibleGateway,Context context) throws Exception;
}
