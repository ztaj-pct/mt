package com.pct.device.service;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.pct.common.constant.AssetStatus;
import com.pct.common.model.Asset;
//import com.pct.common.model.AssetGatewayXref;
import com.pct.common.model.AssetSensorXref;
import com.pct.common.model.Asset_Device_xref;
import com.pct.common.model.Device;
import com.pct.common.payload.SaveAssetGatewayXrefRequest;
import com.pct.device.dto.AssetDTO;
import com.pct.device.dto.AssetResponseDTO;
import com.pct.device.dto.AssetVinSearchDTO;
import com.pct.device.dto.MessageDTO;
import com.pct.device.dto.ResponseBodyDTO;
import com.pct.device.exception.DeviceException;
import com.pct.device.payload.AddAssetResponse;
import com.pct.device.payload.AssetAssociationPayload;
import com.pct.device.payload.AssetCompany;
import com.pct.device.payload.AssetDeviceAssociationPayLoad;
import com.pct.device.payload.AssetRecordPayload;
import com.pct.device.payload.AssetsPayload;
import com.pct.device.payload.AssetsPayloadMobile;
import com.pct.device.payload.CompanyPayload;
import com.pct.device.util.Context;



public interface IAssetService {

	List<AssetDTO> getAssetByIdOrVinNumber(String vin, String assetId, String accountNumber) throws DeviceException;

    List<AssetDTO> getAsset(String accountNumber, AssetStatus status) throws DeviceException;

    List<AssetResponseDTO> getAssetsByAssetId(String assetId, String imei) throws JsonProcessingException;

    AssetVinSearchDTO getAssetVinSearch(String vin) throws Exception;


	AddAssetResponse addAsset(AssetsPayload assetsPayloads, String userId) throws DeviceException;;
	
	void validateAndSaveAssets(List<Asset> assetsList, CompanyPayload company, Map<String, List<String>> validationFailedAssets, boolean isBulkUpload, boolean isSingleCreate, String userName, Boolean isMoblieApi,Boolean isAssociation) throws DeviceException;
	
	List<Asset> nullCheckValidation(List<Asset> assetsList, List<Asset> nullCheckValidatedList, boolean isBulkUpload, Boolean isMoblieApi);
	
	Map<String, List<String>> validationOfAssets(List<Asset> assetList, CompanyPayload company, boolean isBulkUpload, boolean isSingleCreate, String userName, Boolean isMoblieApi,Boolean isAssociation) throws DeviceException;

	AddAssetResponse updateAsset(AssetsPayload assetsPayloads, String userName);

	Page<AssetResponseDTO> getAllActiveCustomerOrganisationList(Pageable pageable, Long companyId, String userName,
			Map<String, String> filterValues, String yearFilter, String sort);

	Page<AssetRecordPayload> getCustomerAssetsList(Pageable pageable, String userName, Map<String, String> filterValues,
			String filterModelCountFilter, String sort);

	List<String> getSuperAdminUser();
	
	AssetResponseDTO getAssetsById(String uuid);
	
	Map<String, List<String>> addAssets(AssetCompany assets, String userName) throws DeviceException;
	
	Page<AssetResponseDTO> getAllActiveCompanyList(Pageable pageable, Long comapnyId, String userName, Map<String, String> filterValues,String yearFilter);
	
	List<String> deleteBatchAssets(List<String> assetIds) throws DeviceException;

	Boolean checkForAssetOverwrite(Long companyId);
	
	Map<String, List<String>> addModifiedAssets(AssetCompany assets, String userName) throws DeviceException;
	
	public MessageDTO uploadAssetAssociation(AssetAssociationPayload assetAssociationPayload);

	Asset findByUuid(String uuid);

	Asset_Device_xref saveAssetDeviceXref(SaveAssetGatewayXrefRequest saveAssetGatewayXrefRequest, String userName) throws Exception;

	Boolean updateCompanyInAsset(String accountNumber, String asset_uuid);

	Boolean isAssetHavePrePairProducts(String assetUuid);

	List<AssetSensorXref> getAllAssetSensorXrefForAssetUuid(String assetUuid);

	Asset getAssetByVinAndCan(String vin, String can);
	
	Boolean deleteAssetByAssetUuid(String assetUuid);
	
	Asset getAssetByVin(String vin);
	 
	Page<AssetResponseDTO> getAssets(String accountNumber, String vin, String assignedName, String status, String eligibleGateway,
				Context context, Pageable pageable, MessageDTO<Page<AssetResponseDTO>> messageDto) throws DeviceException, JsonProcessingException ;

}
