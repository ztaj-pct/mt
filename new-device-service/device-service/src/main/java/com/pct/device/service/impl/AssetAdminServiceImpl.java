package com.pct.device.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.pct.common.constant.AssetCategory;
import com.pct.common.constant.AssetCreationMethod;
import com.pct.common.constant.AssetStatus;
import com.pct.common.model.*;
import com.pct.device.constant.AssetVinComment;
import com.pct.device.dto.AssetResponseDTO;
import com.pct.device.dto.VinDecodeResultDTO;
import com.pct.device.exception.DeviceException;
import com.pct.device.exception.DuplicateVinException;
import com.pct.device.exception.ManufacturerNotFoundException;
import com.pct.device.model.GatewayTypeApprovedAsset;
import com.pct.device.model.Lookup;
import com.pct.device.payload.AddAssetResponse;
import com.pct.device.payload.AssetCompany;
import com.pct.device.payload.AssetsPayload;
import com.pct.device.payload.CompanyPayload;
import com.pct.device.repository.*;
import com.pct.device.repository.projections.AssetGatewayView;
import com.pct.device.service.IAssetAdminService;
import com.pct.device.service.VinDecoderService;
import com.pct.device.specification.AssetSpecification;
import com.pct.device.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AssetAdminServiceImpl implements IAssetAdminService {

	Logger logger = LoggerFactory.getLogger(AssetAdminServiceImpl.class);

	@Autowired
	private IAssetRepository assetRepository;
	@Autowired
	private BeanConverter beanConverter;
	@Autowired
	private ILookupRepository assetPropertiesRepository;
	@Autowired
	private IManufacturerRepository manufacturerRepository;
	@Autowired
	private IManufacturerDetailsRepository manufacturerDetailsRepository;
	@Autowired
	private IGatewayTypeApprovedAssetRepository gatewayTypeApprovedAssetRepository;
	@Autowired
	private VinDecoderService vinDecoderService;
	@Autowired
	private RestUtils restUtils;

	private static final Logger LOGGER = LoggerFactory.getLogger(AssetServiceImpl.class);

	public static final String[] ASSET_ERRORS = {
			"All",
			"Invalid VIN (does not pass length check): The VIN provided is the incorrect length in the following record(s):",
			"VIN in use for another asset: The VIN provided is already in use for another asset in the following record(s):",
			"Asset ID in use for another asset: The Asset ID provided is already in use for another asset for the following record(s):",
			"Missing Approved Product: The Approved Product is missing for the following record(s):",
			"Missing Manufacturer: The Manufacturer is missing for the following record(s):",
			"Missing Model Year: The Model Year is missing for the following record(s):",
			"Approved Product: The Approved Product for the following record(s) is not in the list of valid products:",
			"VIN: The VIN provided for the following record(s) is not the correct length:",
			"Asset Type: The Asset Type for the following record(s) is not is not in the list of valid products:",
			"Model Year: The Model Year for the following record(s) must be four digits:",
			"Asset Type and Approved Product Mismatch: The asset type does not match the selected approved product:",
			"Invalid Manufacturer: Manufacturer name is not valid.",
			"The VINs provided for the following record(s) are invalid",
			"The VIN provided for the following record(s) were decoded successfully, but contained errors. These assets have been added with a notation of the error.",
			"The VIN provided for the following record(s) could not be validated because the service is unavailable. These assets have been added with a notation of the VIN error. "
	};

	@Override
	public Map<String, List<String>> addAssets(AssetCompany assets, Long userId) throws DeviceException {
		LOGGER.info("Inside addAssets", assets);
		User user = restUtils.getUserFromAuthService(userId);
		Map<String, List<String>> validationFailedAssets = new HashMap<>();
		List<Asset> assetsList = new ArrayList<>();
		for(AssetsPayload assetPayload : assets.getAssetList()) {
			assetsList.add(beanConverter.assetsPayloadToAssets(assetPayload, assets.getCompany(), true, user, validationFailedAssets));
		}
		validationFailedAssets.putAll(validationOfAssets(assetsList, assets.getCompany(), true, false, userId, Boolean.FALSE));

		LOGGER.info("Existing  addAssets", assets);
		validationFailedAssets.remove(ASSET_ERRORS[0]);
		return validationFailedAssets;
	}

	@Override
	public void validateAndSaveAssets(List<Asset> assetsList, CompanyPayload company, Map<String, List<String>> validationFailedAssets, boolean isBulkUpload, boolean isSingleCreate, Long userId, Boolean isMoblieApi)
            throws DeviceException {

		List<Lookup> approvedProducts = assetPropertiesRepository.findByField(Constants.ASSET_FIELD_APPROVED_PRODUCT);
		List<Lookup> assetType1Products = assetPropertiesRepository.findByField(Constants.ASSET_FIELD_ASSET_TYPE_1);

		// Try to decode
		List<String> vins = new ArrayList<>();
		vins.addAll(assetsList.stream().filter(asset -> asset.getVin() != null && !asset.getVin().isEmpty()).map(Asset::getVin).collect(Collectors.toList()));
		// commenting as the 3rd party API is down
		Map<String, VinDecodeResultDTO> vinDecodeResult = vinDecoderService.decodeVins(vins);

		List<Asset> assetsListToSave = new ArrayList<>();
		for (Asset a : assetsList) {

			//Asset ID validations
			String modifiedAssetId = StringUtils.replaceBlankSpaceWithUnderscore(a.getAssignedName());
			validationFailedAssets.get(ASSET_ERRORS[0]).remove(a.getAssignedName());
			a.setAssignedName(modifiedAssetId);
			validationFailedAssets.get(ASSET_ERRORS[0]).add(a.getAssignedName());
			Asset checkForUniqueAsset = null;
			if(a.getUuid() != null && !a.getUuid().isEmpty()) {
				checkForUniqueAsset = assetRepository.findByUuid(a.getUuid());
			}
			if (checkForUniqueAsset != null) {
				if (!isSingleCreate) {
					a.setId(checkForUniqueAsset.getId());
					a.setCreatedOn(checkForUniqueAsset.getCreatedOn());
				} else {
					throw new DeviceException("Asset already exists with assigned name " + modifiedAssetId);
				}
			}
			//Asset Type Validations
			if (!assetType1Products.stream().anyMatch(str -> str.getValue().trim().equalsIgnoreCase(a.getCategory().getValue()))) {
				if (isBulkUpload) {
					if(validationFailedAssets.get(ASSET_ERRORS[9]) != null) {
						validationFailedAssets.get(ASSET_ERRORS[9]).add(a.getAssignedName());
					} else {
						List<String> failedAssetAssignedName = new ArrayList<>();
						failedAssetAssignedName.add(a.getAssignedName());
						validationFailedAssets.put(ASSET_ERRORS[9], failedAssetAssignedName);
					}
					validationFailedAssets.get(ASSET_ERRORS[0]).remove(a.getAssignedName());
					continue;
				} else {
					throw new DeviceException("Asset type is invalid");
				}
			}
			
			
			//Assigned name  Validations
			List<Asset> checkForUniqueAssignedName = null;
			if(a.getAssignedName() != null && !a.getAssignedName().isEmpty() && a.getId() == null) {
				checkForUniqueAssignedName = assetRepository.findByAssignedName(a.getAssignedName(),a.getCompany().getAccountNumber());
			}else if(a.getAssignedName() != null && !a.getAssignedName().isEmpty() && a.getId() != null){
				checkForUniqueAssignedName = assetRepository.findByAssignedNameAndNotInId(a.getAssignedName(),a.getId(),a.getCompany().getAccountNumber());

			}
			if (checkForUniqueAssignedName!=null && checkForUniqueAssignedName.size() > 0) {
				if (isBulkUpload) {
					if(validationFailedAssets.get(ASSET_ERRORS[3]) != null) {
						validationFailedAssets.get(ASSET_ERRORS[3]).add(a.getAssignedName());
					} else {
						List<String> failedAssetAssignedName = new ArrayList<>();
						failedAssetAssignedName.add(a.getAssignedName());
						validationFailedAssets.put(ASSET_ERRORS[3], failedAssetAssignedName);
					}
					validationFailedAssets.get(ASSET_ERRORS[0]).remove(a.getAssignedName());
					continue;
				} else {
					throw new DeviceException("The Asset ID provided is already in use for another asset. Please check it and try again.", "Asset ID Already in Use");
				}
			}

			//VIN Validations
			if (a.getVin() != null && !a.getVin().isEmpty()) {
				if ((a.getCategory().equals(AssetCategory.TRAILER) || a.getCategory().equals(AssetCategory.CHASSIS))) {
					if (a.getVin().length() != 17 && !isMoblieApi) {
						a.setComment(AssetVinComment.INVALID.getValue());
						validationFailedAssets.get(ASSET_ERRORS[0]).remove(a.getAssignedName());
						if (validationFailedAssets.get(ASSET_ERRORS[13]) != null) {
							validationFailedAssets.get(ASSET_ERRORS[13]).add(a.getAssignedName());
						} else {
							List<String> failedAssetAssignedName = new ArrayList<>();
							failedAssetAssignedName.add(a.getAssignedName());
							validationFailedAssets.put(ASSET_ERRORS[13], failedAssetAssignedName);
						}
						continue;
					}

					if (!vinDecodeResult.isEmpty()) {
						VinDecodeResultDTO vinDecodingResult = vinDecodeResult.get(a.getVin());
						if (vinDecodingResult == null || !vinDecodingResult.getResult().equals("success")) {
							a.setIsVinValidated(false);
							if (vinDecodingResult != null && vinDecodingResult.getResult().equals("failure")) {
								a.setComment(AssetVinComment.DECODE_ERROR.getValue());
								validationFailedAssets.get(ASSET_ERRORS[0]).remove(a.getAssignedName());
								if (validationFailedAssets.get(ASSET_ERRORS[14]) != null) {
									validationFailedAssets.get(ASSET_ERRORS[14]).add(a.getAssignedName());
								} else {
									List<String> failedAssetAssignedName = new ArrayList<>();
									failedAssetAssignedName.add(a.getAssignedName());
									validationFailedAssets.put(ASSET_ERRORS[14], failedAssetAssignedName);
								}
						/*if(isBulkUpload) {
							continue;
						} else {
							throw new DeviceException("Failed to validate VIN Number");
						}*/
							} else if (vinDecodingResult != null && vinDecodingResult.getResult().equals("decodeFailure")) {
								a.setComment(AssetVinComment.NOT_VALIDATED.getValue());
								validationFailedAssets.get(ASSET_ERRORS[0]).remove(a.getAssignedName());
								if (validationFailedAssets.get(ASSET_ERRORS[15]) != null) {
									validationFailedAssets.get(ASSET_ERRORS[15]).add(a.getAssignedName());
								} else {
									List<String> failedAssetAssignedName = new ArrayList<>();
									failedAssetAssignedName.add(a.getAssignedName());
									validationFailedAssets.put(ASSET_ERRORS[15], failedAssetAssignedName);
								}
							}
						} else if (vinDecodingResult != null && vinDecodingResult.getResult().equals("success")) {
							a.setIsVinValidated(true);
						}
					}

					// check if this VIN already exists
					List<Asset> assetList = assetRepository.findByVinNumber(a.getVin());
					if (assetList != null && !assetList.isEmpty()) {
						if (assetList.size() > 1) {
							if (isBulkUpload) {
								if (validationFailedAssets.get(ASSET_ERRORS[2]) != null) {
									validationFailedAssets.get(ASSET_ERRORS[2]).add(a.getAssignedName());
								} else {
									List<String> failedAssetAssignedName = new ArrayList<>();
									failedAssetAssignedName.add(a.getAssignedName());
									validationFailedAssets.put(ASSET_ERRORS[2], failedAssetAssignedName);
								}
								validationFailedAssets.get(ASSET_ERRORS[0]).remove(a.getAssignedName());
								continue;
							} else {
								throw new DuplicateVinException("The VIN provided is already in use for another asset. Please check it and try again.");
							}
						} else {
							if (assetList.get(0) != null && a.getId() == null) {
								if (isBulkUpload) {
									if (validationFailedAssets.get(ASSET_ERRORS[2]) != null) {
										validationFailedAssets.get(ASSET_ERRORS[2]).add(a.getAssignedName());
									} else {
										List<String> failedAssetAssignedName = new ArrayList<>();
										failedAssetAssignedName.add(a.getAssignedName());
										validationFailedAssets.put(ASSET_ERRORS[2], failedAssetAssignedName);
									}
									validationFailedAssets.get(ASSET_ERRORS[0]).remove(a.getAssignedName());
									continue;
								} else {
									throw new DuplicateVinException("The VIN provided is already in use for another asset. Please check it and try again.");
								}
							} else {
								Asset asset = assetRepository.findByVinAndNotInId(a.getVin(), a.getId());
								if (asset != null) {
									throw new DuplicateVinException("The VIN provided is already in use for another asset. Please check it and try again.");
								}
							}
						}
					}
				}
			}
			
			//Approved Products Validations
			if(!isMoblieApi && !approvedProducts.stream().anyMatch(str -> str.getValue().trim().equals(a.getGatewayEligibility()))) {
				if(isBulkUpload) {
					if(validationFailedAssets.get(ASSET_ERRORS[7]) != null) {
						validationFailedAssets.get(ASSET_ERRORS[7]).add(a.getAssignedName());
					} else {
						List<String> failedAssetAssignedName = new ArrayList<>();
						failedAssetAssignedName.add(a.getAssignedName());
						validationFailedAssets.put(ASSET_ERRORS[7], failedAssetAssignedName);
					}
					validationFailedAssets.get(ASSET_ERRORS[0]).remove(a.getAssignedName());
					continue;
				}else {
					throw new DeviceException("Approved product is invalid");
				}
			}
			
			//Model Year Validations
			if(a.getYear() != null && !a.getYear().isEmpty()) {
				if(a.getIsVinValidated()==true||a.getIsVinValidated()==null) {
				if (!a.getYear().matches("^\\d{4}")) {
					if (isBulkUpload) {
						if (validationFailedAssets.get(ASSET_ERRORS[10]) != null) {
							validationFailedAssets.get(ASSET_ERRORS[10]).add(a.getAssignedName());
						} else {
							List<String> failedAssetAssignedName = new ArrayList<>();
							failedAssetAssignedName.add(a.getAssignedName());
							validationFailedAssets.put(ASSET_ERRORS[10], failedAssetAssignedName);
						}
						validationFailedAssets.get(ASSET_ERRORS[0]).remove(a.getAssignedName());
						continue;
					} else {
						throw new DeviceException("Model year is invalid");
					}
				}
				}
			}

			//UUID Validations
			if(a.getUuid() == null || a.getUuid().isEmpty()) {
				String assetUuid = UUID.randomUUID().toString();
				boolean assetUuidIsNew = false;
				while (!assetUuidIsNew) {
					Asset byUuid = assetRepository.findByUuid(assetUuid);
					if (byUuid != null) {
						assetUuid = UUID.randomUUID().toString();
						continue;
					} else {
						assetUuidIsNew = true;
						break;
					}
				}
				a.setUuid(assetUuid);
			}

			if (a.getCreatedOn() == null) {
				a.setCreatedOn(Instant.now());
			}

			//validate gateway type to asset type
			List<GatewayTypeApprovedAsset> gatewayTypeApprovedAssetList = gatewayTypeApprovedAssetRepository.
					findByGatewayProductName(a.getGatewayEligibility());
			Set<String> assetTypes = gatewayTypeApprovedAssetList.stream().map(gatewayTypeApprovedAsset -> gatewayTypeApprovedAsset.getAssetType())
					.collect(Collectors.toSet());
			if (!isMoblieApi && !assetTypes.contains(a.getCategory().getValue().toUpperCase())) {
				if (isBulkUpload) {
					if(validationFailedAssets.get(ASSET_ERRORS[11]) != null) {
						validationFailedAssets.get(ASSET_ERRORS[11]).add(a.getAssignedName());
					} else {
						List<String> failedAssetAssignedName = new ArrayList<>();
						failedAssetAssignedName.add(a.getAssignedName());
						validationFailedAssets.put(ASSET_ERRORS[11], failedAssetAssignedName);
					}
					validationFailedAssets.get(ASSET_ERRORS[0]).remove(a.getAssignedName());
					continue;
				} else {
					throw new DeviceException("Approved product is not compatible with asset type");
				}
			}

			setCompanyToAssets(a, company, userId);
			validationFailedAssets.get(ASSET_ERRORS[0]).remove(a.getAssignedName());
			a.setUpdatedOn(Instant.now());

			if ((a.getCategory().equals(AssetCategory.TRAILER) || a.getCategory().equals(AssetCategory.CHASSIS))
					&& vinDecodeResult != null && !vinDecodeResult.isEmpty() &&
					vinDecodeResult.get(a.getVin()) != null &&
					vinDecodeResult.get(a.getVin()).getResult() != null
					&& vinDecodeResult.get(a.getVin()).getResult().equals("success")) {
				String make = vinDecodeResult.get(a.getVin()).getVehicleDTO().getMake();
				String model = vinDecodeResult.get(a.getVin()).getVehicleDTO().getModel();
				a.setYear(vinDecodeResult.get(a.getVin()).getVehicleDTO().getModelYear());
				Manufacturer manufacturer = manufacturerRepository.findByName(make);
				if (manufacturer == null) {
					manufacturer = new Manufacturer();
					manufacturer.setName(make);
					String manufacturerUuid = UUID.randomUUID().toString();
					boolean manufacturerUuidIsNew = false;
					while (!manufacturerUuidIsNew) {
						Manufacturer manufacturerFromDB = manufacturerRepository.findByUuid(manufacturerUuid);
						if (manufacturerFromDB != null) {
							manufacturerUuid = UUID.randomUUID().toString();
							continue;
						} else {
							manufacturerUuidIsNew = true;
							break;
						}
					}
					manufacturer.setUuid(manufacturerUuid);
					manufacturer = manufacturerRepository.save(manufacturer);
				}
				a.setManufacturer(manufacturer);
				ManufacturerDetails manufacturerDetails = manufacturerDetailsRepository.findByModelAndManufacturer(model, manufacturer.getId());
				if (manufacturerDetails == null) {
					manufacturerDetails = new ManufacturerDetails();
					manufacturerDetails.setManufacturer(manufacturer);
					manufacturerDetails.setModel(model);
					boolean manufacturerDetailsUuidIsNew = false;
					String manufacturerDetailsUuid = UUID.randomUUID().toString();
					while (!manufacturerDetailsUuidIsNew) {
						ManufacturerDetails manufacturerDetailsFromDB = manufacturerDetailsRepository.findByUuid(manufacturerDetailsUuid);
						if (manufacturerDetailsFromDB != null) {
							manufacturerDetailsUuid = UUID.randomUUID().toString();
							continue;
						} else {
							manufacturerDetailsUuidIsNew = true;
							break;
						}
					}
					manufacturerDetails.setUuid(manufacturerDetailsUuid);
					manufacturerDetails = manufacturerDetailsRepository.save(manufacturerDetails);
				}
				a.setManufacturerDetails(manufacturerDetails);
			} else {
				if(a.getManufacturer() == null && a.getManufacturerDetails() == null) {
					Manufacturer notAvailableManufacturer = manufacturerRepository.findByName("Not Available");
					a.setManufacturer(notAvailableManufacturer);
					ManufacturerDetails notAvailableManufacturerDetails = manufacturerDetailsRepository.findByModelAndManufacturer("Not Available", notAvailableManufacturer.getId());
					a.setManufacturerDetails(notAvailableManufacturerDetails);
				}
				if(a.getYear() == null || a.getYear().isEmpty()) {
					a.setYear("Not Available");
				}
			}
			if(isBulkUpload) {
				a.setCreationMethod(AssetCreationMethod.UPLOAD);
			} else {
				a.setCreationMethod(AssetCreationMethod.MANUAL);
			}

			assetsListToSave.add(a);
		}
		if (assetsListToSave.size() > 0) {
			assetRepository.saveAll(assetsListToSave);
		}
		validationFailedAssets.remove(ASSET_ERRORS[0]);
	}

	@Override
	public AddAssetResponse addAsset(AssetsPayload assets, Long userId) throws DeviceException {
		User user = restUtils.getUserFromAuthService(userId);
		Asset asset = beanConverter.assetsPayloadToAssets(assets, assets.getCompany(), true, user, null);
		if((assets.getManufacturer() != null && !assets.getManufacturer().isEmpty()) &&
			asset.getManufacturer() == null) {
			throw new ManufacturerNotFoundException(ASSET_ERRORS[12]);
		}
		List<Asset> assetList = new ArrayList<>();
		assetList.add(asset);
		Map<String, List<String>> failedAssets = validationOfAssets(assetList, assets.getCompany(), false, true, userId, Boolean.FALSE);
		AddAssetResponse addAssetResponse = new AddAssetResponse();
		addAssetResponse.setErrors(failedAssets);
		addAssetResponse.setAssetPayload(beanConverter.assetsassetsToPayload(asset));
		return addAssetResponse;
	}
	
	@Override
	public AddAssetResponse updateAsset(AssetsPayload assets, Long userId) throws Exception {
		User user = restUtils.getUserFromAuthService(userId);
		Asset asset = beanConverter.assetsPayloadToAssets(assets, assets.getCompany(), true, user, null);
		String modifiedAssetId = StringUtils.replaceBlankSpaceWithUnderscore(asset.getAssignedName());
		Asset existingAsset = assetRepository.findAssetByAssetIdAndCompany(modifiedAssetId,
				assets.getCompany().getId());
		if(existingAsset == null) {
			existingAsset = assetRepository.findByUuid(asset.getUuid());
			if(existingAsset == null) {
				throw new DeviceException("The Asset with Asset ID " + modifiedAssetId + " does not exist");
			}
		}
		if (existingAsset.getStatus().equals(AssetStatus.PARTIAL)
				|| existingAsset.getStatus().equals(AssetStatus.ACTIVE)) {
			throw new DeviceException("The record with the Asset ID " + asset.getAssignedName()
					+ " already has hardware installed, and so it cannot be Updated");
		}
		List<Asset> assetList = new ArrayList<Asset>();
		assetList.add(asset);
		Map<String, List<String>> failedAssets = validationOfAssets(assetList, assets.getCompany(), false, false, userId, Boolean.FALSE);
		AddAssetResponse addAssetResponse = new AddAssetResponse();
		addAssetResponse.setErrors(failedAssets);
		addAssetResponse.setAssetPayload(beanConverter.assetsassetsToPayload(asset));
		return addAssetResponse;
	}

	@Override
	public Boolean checkForAssetOverwrite(Long companyId) {
		Boolean hasExistingAsset = assetRepository.getAssetCountByCompanyId(companyId) > 0;
		return hasExistingAsset;
	}

	@Override
	public Map<String, List<String>> addModifiedAssets(AssetCompany assets, Long userId) throws DeviceException {
		Map<String, List<String>> failedAssets = null;
		List<AssetsPayload> assetsToBeIgnored = new ArrayList<AssetsPayload>();
		for(AssetsPayload asset: assets.getAssetList()) {
			String modifiedAssetId = StringUtils.replaceBlankSpaceWithUnderscore(asset.getAssignedName());
			Asset existingAsset = assetRepository.findAssetByAssetIdAndCompany(modifiedAssetId, assets.getCompany().getId());
			if(existingAsset != null) {
				if(existingAsset.getStatus().equals(AssetStatus.PARTIAL) || existingAsset.getStatus().equals(AssetStatus.ACTIVE)) {
					assetsToBeIgnored.add(asset);
				}
			}
		}
		assets.getAssetList().removeAll(assetsToBeIgnored);
		failedAssets = addAssets(assets, userId);
		return failedAssets;
	}

	@Override
	public List<Asset> nullCheckValidation(List<Asset> assetsList, List<Asset> nullCheckValidatedList, boolean isBulkUpload, Boolean isMoblieApi) {
		List<String> duplicateAssetIdCheckList = new ArrayList<>();
		for (Asset a : assetsList) {
			
			//Asset ID Null check
			if(a.getAssignedName() == null || a.getAssignedName().isEmpty() || duplicateAssetIdCheckList.contains(a.getAssignedName())) {
				if(isBulkUpload) {
					continue;
				}else {
					throw new DeviceException("Asset ID is null");
				}
			}
			
			//Asset Type 1 Null check
			if(a.getCategory() == null || a.getCategory().getValue().isEmpty()) {
				if(isBulkUpload) {
					continue;
				}else {
					throw new DeviceException("Asset type is null");
				}
			}
			
			//VIN Null Check
			// commenting this logic for CLD-285
			/*if((a.getCategory().equals(AssetCategory.TRAILER) || a.getCategory().equals(AssetCategory.CHASSIS)) && (a.getVin() == null || a.getVin().isEmpty())) {
				if(isBulkUpload) {
					continue;
				}else {
					throw new DeviceException("VIN number is null");
				}
			}*/
			
			//Approved Product Null Check
			//Ignore Null Check For Mobile Incoming Request
			if((a.getGatewayEligibility() == null || a.getGatewayEligibility().isEmpty()) && !isMoblieApi) {
				if(isBulkUpload) {
					continue;
				}else {
					throw new DeviceException("Approved product is null");
				}
			}
			
			//Model Year Null Check
			// commenting this logic for CLD-285
			/*if((a.getYear() == null || a.getYear().isEmpty()) && !isMoblieApi) {
				if(isBulkUpload) {
					continue;
				}else {
					throw new DeviceException("Model year is null");
				}
			}*/
			
			//Manufacturer Null Check
			// commenting this logic for CLD-285
			/*if((a.getManufacturer() == null || a.getManufacturer().getName().isEmpty()) && !isMoblieApi) {
				if(isBulkUpload) {
					continue;
				}else {
					throw new DeviceException("Manufacturer is null");
				}
			}*/
			nullCheckValidatedList.add(a);
			duplicateAssetIdCheckList.add(a.getAssignedName());
		}
		return nullCheckValidatedList;
	}

	@Override
	public Map<String, List<String>> validationOfAssets(List<Asset> assetList, CompanyPayload company, boolean isBulkUpload, boolean isSingleCreate, Long userId, Boolean isMoblieApi) throws DeviceException {
        List<Asset> nullCheckValidatedList = new ArrayList<>();
		List<String> allAssetList = assetList.stream().map(item -> item.getAssignedName()).collect(Collectors.toList());
		Map<String, List<String>> validationFailedAssets = new HashMap<>();
		validationFailedAssets.put(ASSET_ERRORS[0], allAssetList);
		nullCheckValidatedList = nullCheckValidation(assetList, nullCheckValidatedList, isBulkUpload, isMoblieApi);

		validateAndSaveAssets(nullCheckValidatedList, company, validationFailedAssets, isBulkUpload, isSingleCreate, userId, isMoblieApi);

		return validationFailedAssets;
	}
	
	public Asset setCompanyToAssets(Asset asset, CompanyPayload company, Long userId) {
		User user = restUtils.getUserFromAuthService(userId);
		List<String> role = user.getRole().stream().map(Role::getRoleName).collect(Collectors.toList());
		if (role.contains(AuthoritiesConstants.SUPER_ADMIN) || role.contains(AuthoritiesConstants.CUSTOMER_ADMIN)) {
			asset.setCompany(beanConverter.companyPayloadToCompanies(company));
		} else {
			asset.setCompany(user.getCompany());
		}
		return asset;
	}
	
	@Override
	public Page<AssetResponseDTO> getAllActive(Pageable pageable, Long companyId, Long userId) {
		User user = restUtils.getUserFromAuthService(userId);
		Company company = restUtils.getCompanyFromCompanyServiceById(companyId);
		List<String> role = user.getRole().stream().map(Role::getRoleName).collect(Collectors.toList());
		Page<AssetGatewayView> allAssetByCompany = null;
		if ((role.contains(AuthoritiesConstants.SUPER_ADMIN) || role.contains(AuthoritiesConstants.CUSTOMER_ADMIN)) && companyId != null) {
			allAssetByCompany = this.getAllAssetByCompany(pageable, company.getAccountNumber());
		} else {
			allAssetByCompany = this.getAllAssetByCompany(pageable, company.getAccountNumber());
		}
		List<AssetResponseDTO> assetResponseDTOList = new ArrayList<>();
		allAssetByCompany.forEach(asset -> {
			assetResponseDTOList.add(beanConverter.convertAssetGatewayViewToAssetResponse(asset));
		});
		Page<AssetResponseDTO> page = new PageImpl<>(assetResponseDTOList, pageable, allAssetByCompany.getTotalElements());
		return page;
	}
	@Override
	public Page<AssetResponseDTO> getAllActiveCompanyList(Pageable pageable, Long companyId, Long userId, Map<String, String> filterValues, String yearFilter) {
		
		System.out.println("year type filter: "+yearFilter);
		User user = restUtils.getUserFromAuthService(userId);
		Page<Asset> allAssetByCompany = null;
		Specification<Asset> spc = AssetSpecification.getaAssetSpecification(filterValues, user, companyId,yearFilter);
		allAssetByCompany = assetRepository.findAll(spc, pageable);
		List<String> assetUuids = allAssetByCompany.stream().map(asset -> asset.getUuid()).collect(Collectors.toList());
		Map<String, InstallHistory> mapFromListOfInstallHistory = new HashMap<>();
		List<InstallHistory> installHistories = new ArrayList<InstallHistory>();
		if(assetUuids != null && !assetUuids.isEmpty()) {
			if(filterValues.containsKey("imei") || filterValues.containsKey("installed")) {
				installHistories = restUtils.getInstallHistoryListForAssetUuids(assetUuids, filterValues);
			mapFromListOfInstallHistory = beanConverter.createMapFromListOfInstallHistory(installHistories);
			}else {
			 installHistories = restUtils.getInstallHistoryListForAssetUuids(assetUuids, null);
				mapFromListOfInstallHistory = beanConverter.createMapFromListOfInstallHistory(installHistories);
		}
			
		}
		List<AssetResponseDTO> assetResponseDTOList = new ArrayList<>();
		if(filterValues.containsKey("imei") ||filterValues.containsKey("installed")) {
			for (Asset asset : allAssetByCompany) {
				InstallHistory ih = mapFromListOfInstallHistory.get(asset.getUuid());
					if(ih!=null && ih.getAsset().getUuid().equalsIgnoreCase(asset.getUuid())) {
				try {
					assetResponseDTOList.add(beanConverter.convertAssetToAssetResponseDTO(asset, mapFromListOfInstallHistory.get(asset.getUuid())));
					
				} catch (JsonProcessingException e) {
					logger.error("Exception while converting asset to AssetResponseDTO", e);
				}
				}
			}
			Page<AssetResponseDTO> page = new PageImpl<>(assetResponseDTOList, pageable, assetResponseDTOList.size());
			return page;
		}else {
		for (Asset asset : allAssetByCompany) {
			try {
				assetResponseDTOList.add(beanConverter.convertAssetToAssetResponseDTO(asset, mapFromListOfInstallHistory.get(asset.getUuid())));
			} catch (JsonProcessingException e) {
				logger.error("Exception while converting asset to AssetResponseDTO", e);
			}
		}
		Page<AssetResponseDTO> page = new PageImpl<>(assetResponseDTOList, pageable, allAssetByCompany.getTotalElements());
		return page;
		}
	}
	
	@Override
	public Page<AssetGatewayView> getAllAssetByCompany(Pageable pageable, String accountNumber) {
		Page<AssetGatewayView> assetsList = assetRepository.getAllAssetsByCompany(pageable, accountNumber);
		return assetsList;
	}

	@Override
	public List<String> getSuperAdminUser() throws Exception {
		List<String> list = restUtils.getSuperAdminUser();
		if(list.size()>0) {
		return list;
		}else {
			throw new DeviceException("User list is null");
		}
	}
}
