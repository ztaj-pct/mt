package com.pct.device.service;

import com.pct.common.model.Asset;
import com.pct.device.dto.AssetResponseDTO;
import com.pct.device.exception.DeviceException;
import com.pct.device.payload.AddAssetResponse;
import com.pct.device.payload.AssetCompany;
import com.pct.device.payload.AssetsPayload;
import com.pct.device.payload.CompanyPayload;

import com.pct.device.repository.projections.AssetGatewayView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface IAssetAdminService {

	Map<String, List<String>> addAssets(AssetCompany assets, Long userId) throws DeviceException;

	void validateAndSaveAssets(List<Asset> assetsList, CompanyPayload company, Map<String, List<String>> validationFailedAssets, boolean isBulkUpload, boolean isSingleCreate, Long userId, Boolean isMoblieApi) throws DeviceException;

	AddAssetResponse addAsset(AssetsPayload assets, Long userId) throws DeviceException;

	Boolean checkForAssetOverwrite(Long companyId);

	Map<String, List<String>> addModifiedAssets(AssetCompany assets, Long userId) throws DeviceException;

	List<Asset> nullCheckValidation(List<Asset> assetsList, List<Asset> nullCheckValidatedList, boolean isBulkUpload, Boolean isMoblieApi);

	Map<String, List<String>> validationOfAssets(List<Asset> assetList, CompanyPayload company, boolean isBulkUpload, boolean isSingleCreate, Long userId, Boolean isMoblieApi) throws DeviceException;
	Page<AssetResponseDTO> getAllActive(Pageable pageable, Long comapnyId, Long userId);
	Page<AssetResponseDTO> getAllActiveCompanyList(Pageable pageable, Long comapnyId, Long userId, Map<String, String> filterValues,String yearFilter);
	Page<AssetGatewayView> getAllAssetByCompany(Pageable pageable, String accountNumber);

	AddAssetResponse updateAsset(AssetsPayload assets, Long userId) throws Exception;
	
	List<String> getSuperAdminUser() throws Exception;

}
