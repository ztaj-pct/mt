package com.pct.device.service;

import com.pct.common.model.Asset;
import com.pct.device.exception.DeviceException;
import com.pct.device.model.AssetRecord;
import com.pct.device.payload.AssetRecordPayload;
import com.pct.device.payload.AssetsPayload;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface IAssetsService {
	/**
	 * Method to save communication
	 *
	 * @param communication
	 * @return
	 */
//    public List<String> addAssets(AssetCompany assets) throws InstallerException; 

	/**
	 * Method to get communication by id
	 *
	 * @param id
	 * @return Communication
	 */
    AssetsPayload find(Integer id);

//    /**
//     * Method to update communication
//     * 
//     * @param communication
//     * @return Communication
//     */
//    public Asset update(AssetsPayload assets) ;

	/**
	 * Method to get All Communications
	 *
	 * @return List<Communication>
	 */
    List<AssetsPayload> getAll();

	List<AssetsPayload> getAssetsByCompany(Long userId);

	//	public Page<Asset> getAllActive(Pageable pageable,Long comapnyId);
//	public void addAsset(AssetsPayload assets); 
    void deleteAsset(Integer id);

	Page<Asset> getAllAsset(Pageable pageable);

	//	public Page<Asset> getAllAssetByCompany(Pageable pageable,Long CompanyId);
    Page<AssetRecordPayload> getCustomerAssets(Pageable pageable, Long userId);
    Page<AssetRecordPayload> getCustomerAssetsList(Pageable pageable, Long userId, Map<String, String> filterValues, String filterModelCountFilter);

	//	public void validateAndSaveAssets(List<Asset> assetsList, Company company, List<String> validationFailedAssets) throws InstallerException;
//	public Boolean checkForAssetOverwrite(Long companyId);
//	public List<String> addModifiedAssets(AssetCompany assets) throws InstallerException;
    List<String> deleteBatchAssets(List<String> assetIds) throws DeviceException;
}
