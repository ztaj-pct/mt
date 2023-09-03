package com.pct.device.service.impl;

import com.pct.common.constant.AssetStatus;
import com.pct.common.model.Asset;
import com.pct.common.model.Role;
import com.pct.common.model.User;
import com.pct.device.specification.CustomAssetSpecification;
import com.pct.device.exception.DeviceException;
import com.pct.device.model.AssetRecord;
import org.springframework.data.jpa.domain.Specification;
import com.pct.device.payload.AssetRecordPayload;
import com.pct.device.payload.AssetsPayload;
import com.pct.device.repository.ILookupRepository;
import com.pct.device.repository.IAssetRecordRepository;
import com.pct.device.repository.IAssetsRepository;
import com.pct.device.service.IAssetsService;
import com.pct.device.service.VinDecoderService;
import com.pct.device.util.AuthoritiesConstants;
import com.pct.device.util.BeanConverter;
import com.pct.device.util.RestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
public class AssetsService implements IAssetsService {
	private static final Logger LOGGER = LoggerFactory.getLogger(AssetsService.class);
	private final String TRAILER = "Trailer";
	private final String CHASSIS = "Chassis";

	@Autowired
	private IAssetsRepository repository;
	@Autowired
	private BeanConverter beanConverter;
	@Autowired
	private ILookupRepository assetPropertiesRepository;
	@Autowired
	private VinDecoderService vinDecoderService;
	@Autowired
	private RestUtils restUtils;
	@Autowired
	private IAssetRecordRepository assetRecordRepository;

	@Override
	public AssetsPayload find(Integer id) {
		Asset asset = repository.findById(id).get();
		return beanConverter.assetsassetsToPayload(asset);
	}

//	@Override
//	public Asset update(AssetsPayload assets) {
//		Asset asset =repository.findById(assets.getId()).get();
//		asset = beanConvertor.assetsToAsset(assets,asset);
//		asset = repository.saveAndFlush(asset);
//		return asset;
//	}

	@Override
	public List<AssetsPayload> getAll() {
		List<Asset> assetsList = repository.findAll();
		List<AssetsPayload> assetsPayloadList = assetsList.stream().map(beanConverter::assetsassetsToPayload)
				.collect(Collectors.toList());
		return assetsPayloadList;
	}

//	@Override
//	public List<String> addAssets(AssetCompany assets) throws InstallerException {
//		LOGGER.info("Inside addAssets", assets);
//			
//			List<Asset> assetsList = assets.getAssetList().stream().map(item -> {
//					return beanConvertor.assetsPayloadToAssets(item, true);
//				}).collect(Collectors.toList());
//			
//			List<String> validationFailedAssets = validationOfAssets(assetsList, assets.getCompany());
//			
//			LOGGER.info("Existing  addAssets", assets);
//			return validationFailedAssets;
//	}

//	@Override
//	@Transactional
//	public void validateAndSaveAssets(List<Asset> assetsList, Company company, List<String> validationFailedAssets) throws InstallerException {
//		
//		List<AssetConfiguration> approvedProducts = assetPropertiesRepository.findByField(Constants.ASSET_FIELD_APPROVED_PRODUCT);
//		List<AssetConfiguration> assetType1Products = assetPropertiesRepository.findByField(Constants.ASSET_FIELD_ASSET_TYPE_1);
//		
//		// Try to decode
//		List<String> vins = new ArrayList<>();
//		vins.addAll(assetsList.stream().filter(asset -> asset.getVin() != null && !asset.getVin().isEmpty()).map(Asset::getVin).collect(Collectors.toList()));
//		Map<String, VinDecodeResultDTO> vinDecodeResult = vinDecoderService.decodeVins(vins);
//		
//		
//		List<Asset> assetsListToSave = new ArrayList<Asset>();
//		for (Asset a : assetsList) {
//			
//			//Asset ID validations
//			String modifiedAssetId = StringUtils.replaceBlankSpaceWithUnderscore(a.getAssetId());
//			validationFailedAssets.remove(a.getAssetId());
//			a.setAssetId(modifiedAssetId);
//			validationFailedAssets.add(a.getAssetId());
//			Asset checkForUniqueAsset = repository.findAssetByAssetIdAndCompany(modifiedAssetId, company.getId());
//			if(checkForUniqueAsset != null) {
//				a.setId(checkForUniqueAsset.getId());
//				a.setCreatedAt(checkForUniqueAsset.getCreatedAt());
//			}
//			
//			//Asset Type Validations
//			if(!assetType1Products.stream().anyMatch(str -> str.getDataType().trim().equals(a.getAssetType1()))) {
//				continue;
//			}
//							
//			//VIN Validations
//			if((a.getAssetType1().equalsIgnoreCase(TRAILER) || a.getAssetType1().equalsIgnoreCase(CHASSIS)) && !(a.getVin() == null || a.getVin().isEmpty())) {
//				// Try to decode
//				if (!vinDecodeResult.isEmpty()) {
//					VinDecodeResultDTO vinDecodingResult = vinDecodeResult.get(a.getVin());
//					if (!vinDecodingResult.getResult().equals("success")) {
//						continue;
//					}
//				}
//			}
//			
//			//Approved Products Validations
//			if(!approvedProducts.stream().anyMatch(str -> str.getDataType().trim().equals(a.getIsProductIsApprovedForAsset()))) {
//				continue;
//			}
//			
//			//Model Year Validations
//			if(!a.getModelYear().matches("^\\d{4}")) {
//				continue;
//			}
//			
//			setCompanyToAssets(a, company);
//			validationFailedAssets.remove(a.getAssetId());
//			assetsListToSave.add(a);
//			
//		}
//		if (assetsListToSave.size() > 0) {
//			repository.saveAll(assetsListToSave);
//		}
//
//	}

//	@Override
//	public void addAsset(AssetsPayload assets) {
//		try {
//			Asset asset = beanConvertor.assetsPayloadToAssets(assets,true);
//			List<Asset> assetList = new ArrayList<Asset>();
//			assetList.add(asset);
//			List<String> failedAssets = validationOfAssets(assetList, assets.getCompany());
//			if(failedAssets.size() > 0) {
//				throw new Exception("The Asset failed to validate.");
//			}
//		} catch (Exception e) {
//
//			e.printStackTrace();
//		}
//	}

	@Override
	public void deleteAsset(Integer id) throws DeviceException {
		Optional<Asset> optionalAsset = repository.findById(id);
		if (optionalAsset.isPresent()) {
			Asset asset = optionalAsset.get();
			if (asset.getStatus().equals(AssetStatus.PARTIAL) || asset.getStatus().equals(AssetStatus.ACTIVE)) {
				throw new DeviceException("The record with the Asset ID " + asset.getAssignedName() + " already has hardware installed, and so it cannot be deleted.");
			}
			repository.deleteById(id);
		} else {
			throw new DeviceException("Asset not found");
		}
	}

	@Override
	public List<AssetsPayload> getAssetsByCompany(Long userId) {
		User user = restUtils.getUserFromAuthService(userId);
		List<Asset> assetsList = repository.findByCompany(user.getCompany());
		List<AssetsPayload> assetsPayloadList = assetsList.stream().map(beanConverter::assetsassetsToPayload)
				.collect(Collectors.toList());
		return assetsPayloadList;
	}

//	@Override
//	public Page<Asset> getAllAssetByCompany(Pageable pageable, Long CompanyId) {
//		Page<Asset> assetsList = repository.getAllAssetsByCompany(pageable, CompanyId);
//		return assetsList;
//	}

//	@Override
//	public Page<Asset> getAllActive(Pageable pageable,Long comapnyId) {
//		User user = beanConvertor.getLogedInUser();
//		List<String> role = user.getRole().stream().map(Role::getRoleName).collect(Collectors.toList());
//		if (role.contains(AuthoritiesConstants.SUPER_ADMIN) && comapnyId != null) {
//			return this.getAllAssetByCompany(pageable,comapnyId);
//		} else {
//			return this.getAllAssetByCompany(pageable, user.getCompany().getId());
//		}
//	}

	@Override
	public Page<Asset> getAllAsset(Pageable pageable) {
		Page<Asset> users = null;
		Page<AssetsPayload> users_page = null;
		try {
			users = repository.getAllAsset(pageable);
			List<AssetsPayload> listData = users.getContent().stream().map(beanConverter::assetsassetsToPayload)
					.collect(Collectors.toList());
			users_page = new PageImpl<>(listData);
			return users;
		} catch (Exception e) {
			// TODO Auto-generated catch block

			e.printStackTrace();
			return users;
		}
	}

//	public Asset setCompanyToAssets(Asset asset, Company company) {
//		User user = beanConvertor.getLogedInUser();
//		List<String> role = user.getRole().stream().map(Role::getRoleName).collect(Collectors.toList());
//		if (role.contains(ROLE_SUPERADMIN)) {
//			asset.setCompany(company);
//		} else {
//			asset.setCompany(user.getCompany());
//		}
//		return asset;
//	}

	@Override
	public Page<AssetRecordPayload> getCustomerAssets(Pageable pageable, Long userId) {
		Page<AssetRecord> customerAssets = null;
		User user = restUtils.getUserFromAuthService(userId);
		List<String> role = user.getRole().stream().map(Role::getRoleName).collect(Collectors.toList());
		if (role.contains(AuthoritiesConstants.SUPER_ADMIN)
				|| (role.contains(AuthoritiesConstants.CUSTOMER_ADMIN) && user.getCompany().getType().equals("Manufacturer"))) {
			customerAssets = assetRecordRepository.findAll(pageable);
		} else {
			Long companyId = user.getCompany().getId();
			customerAssets = assetRecordRepository.getAllAssetRecordsByCompany(pageable, companyId);
		}
		Page<AssetRecordPayload> assetRecordPayloadPage = beanConverter.convertAssetRecordToAssetRecordPayload(customerAssets, pageable);
		return assetRecordPayloadPage;
	}

	@Override
	public Page<AssetRecordPayload> getCustomerAssetsList(Pageable pageable, Long userId,Map<String, String> filterValues, String filterModelCountFilter) {
		Page<AssetRecord> customerAssets = null;
		User user = restUtils.getUserFromAuthService(userId);
		Specification<AssetRecord> spc = CustomAssetSpecification.getCampaignSpecification(filterValues, user, filterModelCountFilter);
		customerAssets = assetRecordRepository.findAll(spc, pageable);
		Page<AssetRecordPayload> assetRecordPayloadPage = beanConverter.convertAssetRecordToAssetRecordPayload(customerAssets, pageable);
		return assetRecordPayloadPage;
	}

//	@Override
//	public Boolean checkForAssetOverwrite(Long companyId) {
//		Boolean hasExistingAsset = repository.getAssetCountByCompanyId(companyId) > 0 ? true : false;
//		return hasExistingAsset;
//	}

//	@Override
//	public List<String> addModifiedAssets(AssetCompany assets) throws InstallerException {
//		List<String> failedAssets = null;
//		List<AssetsPayload> assetsToBeIgnored = new ArrayList<AssetsPayload>();
//		for(AssetsPayload asset: assets.getAssetList()) {
//			String modifiedAssetId = StringUtils.replaceBlankSpaceWithUnderscore(asset.getAssetId());
//			Asset existingAsset = repository.findAssetByAssetIdAndCompany(modifiedAssetId, assets.getCompany().getId());
//			if(existingAsset != null) {
//				if(existingAsset.getStatus().equalsIgnoreCase(Constants.PARTIAL) || existingAsset.getStatus().equalsIgnoreCase(Constants.INSTALLED)) {
//					assetsToBeIgnored.add(asset);
//				}
//			}
//		}
//		assets.getAssetList().removeAll(assetsToBeIgnored);
//		failedAssets = addAssets(assets);
//		return failedAssets;
//	}

//	public List<Asset> nullCheckValidation(List<Asset> assetsList, List<Asset> nullCheckValidatedList) {
//		List<String> duplicateAssetIdCheckList = new ArrayList<>();
//		for (Asset a : assetsList) {
//			
//			//Asset ID Null check
//			if(a.getAssetId() == null || a.getAssetId().isEmpty() || duplicateAssetIdCheckList.contains(a.getAssetId())) {
//				continue;
//			}
//			
//			//Asset Type 1 Null check
//			if(a.getAssetType1() == null || a.getAssetType1().isEmpty()) {
//				continue;
//			}
//			
//			//VIN Null Check
//			if((a.getAssetType1().equalsIgnoreCase(TRAILER) || a.getAssetType1().equalsIgnoreCase(CHASSIS)) && (a.getVin() == null || a.getVin().isEmpty())) {
//				continue;
//			}
//			
//			//Approved Product Null Check
//			if(a.getIsProductIsApprovedForAsset() == null || a.getIsProductIsApprovedForAsset().isEmpty()) {
//				continue;
//			}
//			
//			//Model Year Null Check
//			if(a.getModelYear() == null || a.getModelYear().isEmpty()) {
//				continue;
//			}
//			
//			//Manufacturer Null Check
//			if(a.getManufacturer() == null || a.getManufacturer().isEmpty()) {
//				continue;
//			}
//			nullCheckValidatedList.add(a);
//			duplicateAssetIdCheckList.add(a.getAssetId());
//		}
//		return nullCheckValidatedList;
//	}

	@Override
	@Transactional
	public List<String> deleteBatchAssets(List<String> assetUuids) throws DeviceException {
		List<String> failedAssets = null;
		List<Asset> assets = repository.getAssetsByUuids(assetUuids);
		List<Asset> assetsToBeDeleted = new ArrayList<Asset>();
		if (assets.size() > 0) {
			failedAssets = new ArrayList<String>();
			for (Asset asset : assets) {
				if (asset.getStatus().equals(AssetStatus.PARTIAL) || asset.getStatus().equals(AssetStatus.ACTIVE) || asset.getStatus().equals(AssetStatus.INSTALL_IN_PROGRESS)) {
					failedAssets.add(asset.getAssignedName());
				} else {
					assetsToBeDeleted.add(asset);
				}
			}
			repository.deleteInBatch(assetsToBeDeleted);
		} else {
			throw new DeviceException("No assets found.");
		}
		return failedAssets;
	}

//	public List<String> validationOfAssets(List<Asset> assetList, Company company) {
//		List<Asset> nullCheckValidatedList = new ArrayList<Asset>();
//		List<String> validationFailedAssets = assetList.stream().map(item -> item.getAssetId()).collect(Collectors.toList());
//		
//		nullCheckValidatedList = nullCheckValidation(assetList, nullCheckValidatedList);
//			
//		validateAndSaveAssets(nullCheckValidatedList, company, validationFailedAssets);
//		
//		return validationFailedAssets;
//	}

}
