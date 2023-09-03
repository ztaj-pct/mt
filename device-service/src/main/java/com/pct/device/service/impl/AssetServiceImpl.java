package com.pct.device.service.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.pct.common.constant.AssetCategory;
import com.pct.common.constant.AssetCreationMethod;
import com.pct.common.constant.AssetStatus;
import com.pct.device.constant.AssetVinComment;
import com.pct.common.constant.DeviceStatus;
import com.pct.device.dto.AssetDTO;
import com.pct.device.dto.AssetResponseDTO;
import com.pct.device.dto.AssetVinSearchDTO;
import com.pct.device.dto.MessageDTO;
import com.pct.device.dto.NHTSAResponseDTO;
import com.pct.device.dto.NHTSAResultDTO;
import com.pct.device.dto.ResponseBodyDTO;
import com.pct.device.dto.VinDecodeResultDTO;
import com.pct.device.exception.DeviceException;
import com.pct.device.exception.DuplicateVinException;
import com.pct.device.exception.ManufacturerNotFoundException;
import com.pct.device.model.AssetRecord;
import com.pct.device.model.DeviceShippingDetails;
import com.pct.device.model.DeviceTypeApprovedAsset;
import com.pct.device.model.Event;
import com.pct.device.model.Lookup;
import com.pct.device.ms.repository.IAssetSensorXrefRepository;
import com.pct.common.model.Asset;
//import com.pct.common.model.AssetGatewayXref;
import com.pct.common.model.AssetSensorXref;
import com.pct.common.model.Asset_Device_xref;
import com.pct.common.model.Device;
import com.pct.common.model.Manufacturer;
import com.pct.common.model.ManufacturerDetails;
import com.pct.common.model.Organisation;
import com.pct.common.model.Role;
import com.pct.common.model.User;
import com.pct.common.payload.SaveAssetGatewayXrefRequest;
import com.pct.common.util.JwtUser;
import com.pct.common.util.Logutils;
import com.pct.device.payload.AddAssetResponse;
import com.pct.device.payload.AssetAssociationPayload;
import com.pct.device.payload.AssetCompany;
import com.pct.device.payload.AssetRecordPayload;
import com.pct.device.payload.AssetsPayload;
import com.pct.device.payload.AssetsPayloadMobile;
import com.pct.device.payload.AssociationPayload;
import com.pct.device.payload.CompanyPayload;
import com.pct.device.payload.InstalledHistoryResponsePayload;
import com.pct.device.repository.IAssetDeviceXref;
import com.pct.device.repository.IAssetDeviceXrefRepository;
import com.pct.device.repository.IAssetRecordRepository;
import com.pct.device.repository.IAssetRepository;
import com.pct.device.repository.IDeviceRepository;
import com.pct.device.repository.IDeviceTypeApprovedAssetRepository;
import com.pct.device.repository.ILookupRepository;
import com.pct.device.repository.IManufacturerDetailsRepository;
import com.pct.device.repository.IManufacturerRepository;
import com.pct.device.repository.RedisDeviceRepository;
import com.pct.device.service.IAssetService;
import com.pct.device.service.VinDecoderService;
import com.pct.device.specification.AssetSpecification;
import com.pct.device.specification.CustomAssetSpecification;
import com.pct.device.util.AppUtility;
import com.pct.device.util.AuthoritiesConstants;
import com.pct.device.util.BeanConverter;
import com.pct.device.util.Constants;
import com.pct.device.util.Context;
import com.pct.device.util.MailUtil;
import com.pct.device.util.RestUtils;
import com.pct.device.util.StringUtils;

@Service
public class AssetServiceImpl implements IAssetService {

	Logger logger = LoggerFactory.getLogger(AssetServiceImpl.class);
	private static final Logger LOGGER = LoggerFactory.getLogger(AssetServiceImpl.class);
	public static final String DEVICE_CURRENT_VIEW_PREFIX = "deviceData:";
	public static final String[] ASSET_ERRORS = { "All",
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
			"The VIN provided for the following record(s) could not be validated because the service is unavailable. These assets have been added with a notation of the VIN error. ", };

	public static final String className = "AssetServiceImpl";

	@Autowired
	private IAssetRepository assetRepository;

	@Autowired
	private BeanConverter beanConverter;

	@Autowired
	private RestUtils restUtils;

	@Autowired
	private ILookupRepository assetPropertiesRepository;

	@Autowired
	private VinDecoderService vinDecoderService;

	@Autowired
	private IManufacturerRepository manufacturerRepository;

	@Autowired
	private IManufacturerDetailsRepository manufacturerDetailsRepository;

	@Autowired
	private IDeviceTypeApprovedAssetRepository gatewayTypeApprovedAssetRepository;

	@Autowired
	private IAssetRecordRepository assetRecordRepository;

	@Autowired
	private IAssetDeviceXrefRepository assetDeviceXrefRepository;

	@Autowired
	private IDeviceRepository deviceRepository;

	@Autowired
	private IAssetDeviceXref assetDeviceXref;

	@Autowired
	IAssetSensorXrefRepository iAssetSensorXrefRepository;

	@Autowired
	IAssetSensorXrefRepository assetSensorXrefRepository;

	@Autowired
	MailUtil mailUtil;

	@Autowired
	RedisDeviceRepository redisDeviceRepository;

	@Override
	public AddAssetResponse addAsset(AssetsPayload assets, String userName) throws DeviceException {
		LOGGER.info("Inside addAsset and fetch asset Details", assets);
		if (assets.getVin() != null && assets.getVin() != "" && assets.getVin().isEmpty()) {
			List<Asset> assetAvailable = assetRepository.findByVinNumber(assets.getVin());
			if (assetAvailable != null && assetAvailable.size() > 0) {
				throw new DeviceException(
						"The VIN provided is already in use for another asset. Please check it and try again.");
			}
		}
		User user = restUtils.getUserFromAuthService(userName);
		CompanyPayload company = assets.getCompany();
		if (company.getAccountNumber() != null) {
			Organisation com = restUtils.getCompanyFromCompanyService(company.getAccountNumber());
			LOGGER.info("after rest call from get company", com);
			if (com != null) {
				company.setAccountNumber(com.getAccountNumber());
				company.setCompanyName(com.getOrganisationName());
				company.setId(com.getId());
				company.setIsAssetListRequired(com.getIsAssetListRequired());
				company.setShortName(com.getShortName());
				company.setStatus(com.getIsActive());
				company.setUuid(com.getUuid());
			}
		}
		Asset asset = beanConverter.assetsPayloadToAssets(assets, company, true, user, null);
		if ((assets.getManufacturer() != null && !assets.getManufacturer().isEmpty())
				&& asset.getManufacturer() == null) {
			throw new ManufacturerNotFoundException(ASSET_ERRORS[12]);
		}
		List<Asset> assetList = new ArrayList<>();
		assetList.add(asset);
		Map<String, List<String>> failedAssets = validationOfAssets(assetList, assets.getCompany(), false, true,
				userName, Boolean.FALSE, Boolean.FALSE);
		AddAssetResponse addAssetResponse = new AddAssetResponse();
		addAssetResponse.setErrors(failedAssets);
		addAssetResponse.setAssetPayload(beanConverter.assetsassetsToPayload(asset));
		LOGGER.info("After saving asset details");
		return addAssetResponse;
	}

	@Override
	public Asset getAssetByVinAndCan(String vin, String can) {
		Asset assetByVinNumber = assetRepository.findByVinAndAccountNumber(vin);// can
		return assetByVinNumber;
	}

	@Override
	public Map<String, List<String>> validationOfAssets(List<Asset> assetList, CompanyPayload company,
			boolean isBulkUpload, boolean isSingleCreate, String userName, Boolean isMoblieApi, Boolean isAssociation)
			throws DeviceException {
		LOGGER.info("Validating Asset Details");
		List<Asset> nullCheckValidatedList = new ArrayList<>();
		List<String> allAssetList = assetList.stream().map(item -> item.getAssignedName()).collect(Collectors.toList());
		Map<String, List<String>> validationFailedAssets = new HashMap<>();
		validationFailedAssets.put(ASSET_ERRORS[0], allAssetList);
		nullCheckValidatedList = nullCheckValidation(assetList, nullCheckValidatedList, isBulkUpload, isMoblieApi);

		validateAndSaveAssets(nullCheckValidatedList, company, validationFailedAssets, isBulkUpload, isSingleCreate,
				userName, isMoblieApi, isAssociation);

		return validationFailedAssets;
	}

	@Override
	public List<Asset> nullCheckValidation(List<Asset> assetsList, List<Asset> nullCheckValidatedList,
			boolean isBulkUpload, Boolean isMoblieApi) {
		LOGGER.info("Checking nullcheckValidation");
		List<String> duplicateAssetIdCheckList = new ArrayList<>();
		for (Asset a : assetsList) {

			// Asset ID Null check
			if (a.getAssignedName() == null || a.getAssignedName().isEmpty()
					|| duplicateAssetIdCheckList.contains(a.getAssignedName())) {
				if (isBulkUpload) {
					continue;
				} else {
					throw new DeviceException("Asset ID is null");
				}
			}

			// Asset Type 1 Null check
			if (a.getCategory() == null || a.getCategory().getValue().isEmpty()) {
				if (isBulkUpload) {
					LOGGER.info("Category not found");
					continue;
				} else {
					throw new DeviceException("Asset type is null");
				}
			}

			// VIN Null Check
			// commenting this logic for CLD-285
			/*
			 * if((a.getCategory().equals(AssetCategory.TRAILER) ||
			 * a.getCategory().equals(AssetCategory.CHASSIS)) && (a.getVin() == null ||
			 * a.getVin().isEmpty())) { if(isBulkUpload) { continue; }else { throw new
			 * DeviceException("VIN number is null"); } }
			 */

			// Approved Product Null Check
			// Ignore Null Check For Mobile Incoming Request
			if ((a.getGatewayEligibility() == null || a.getGatewayEligibility().isEmpty()) && !isMoblieApi) {
				if (isBulkUpload) {
					LOGGER.info("GatewayEligibility not found");
					continue;
				} else {
					throw new DeviceException("Approved product is null");
				}
			}

			// Model Year Null Check
			// commenting this logic for CLD-285
			/*
			 * if((a.getYear() == null || a.getYear().isEmpty()) && !isMoblieApi) {
			 * if(isBulkUpload) { continue; }else { throw new
			 * DeviceException("Model year is null"); } }
			 */

			// Manufacturer Null Check
			// commenting this logic for CLD-285
			/*
			 * if((a.getManufacturer() == null || a.getManufacturer().getName().isEmpty())
			 * && !isMoblieApi) { if(isBulkUpload) { continue; }else { throw new
			 * DeviceException("Manufacturer is null"); } }
			 */
			nullCheckValidatedList.add(a);
			duplicateAssetIdCheckList.add(a.getAssignedName());
		}
		return nullCheckValidatedList;
	}

	@Override
	public void validateAndSaveAssets(List<Asset> assetsList, CompanyPayload company,
			Map<String, List<String>> validationFailedAssets, boolean isBulkUpload, boolean isSingleCreate,
			String userName, Boolean isMoblieApi, Boolean isAssociation) throws DeviceException {
		LOGGER.info("Inside validating and saving asset details");
		List<Lookup> approvedProducts = assetPropertiesRepository.findByField(Constants.ASSET_FIELD_APPROVED_PRODUCT);
		List<Lookup> assetType1Products = assetPropertiesRepository.findByField(Constants.ASSET_FIELD_ASSET_TYPE_1);

		// Try to decode
		List<String> vins = new ArrayList<>();
		vins.addAll(assetsList.stream().filter(asset -> asset.getVin() != null && !asset.getVin().isEmpty())
				.map(Asset::getVin).collect(Collectors.toList()));
		// commenting as the 3rd party API is down
		Map<String, VinDecodeResultDTO> vinDecodeResult = vinDecoderService.decodeVins(vins);
		String currentTimestampInStringFormat = addTimestampInStringFormat();
		List<Asset> assetsListToSave = new ArrayList<>();
		for (Asset a : assetsList) {

			// Asset ID validations
			LOGGER.info("Validating Asset Id");
			String modifiedAssetId = StringUtils.replaceBlankSpaceWithUnderscore(a.getAssignedName());
			validationFailedAssets.get(ASSET_ERRORS[0]).remove(a.getAssignedName());
			a.setAssignedName(modifiedAssetId);
			validationFailedAssets.get(ASSET_ERRORS[0]).add(a.getAssignedName());
			Asset checkForUniqueAsset = null;
			if (a.getUuid() != null && !a.getUuid().isEmpty()) {
				checkForUniqueAsset = assetRepository.findByUuid(a.getUuid());
			}
			if (checkForUniqueAsset == null) {
				checkForUniqueAsset = assetRepository.findByAssignedName(modifiedAssetId);
			}
			if (checkForUniqueAsset != null) {
				if (!isSingleCreate) {
					a.setId(checkForUniqueAsset.getId());
					a.setUuid(checkForUniqueAsset.getUuid());
					a.setCreatedAt(checkForUniqueAsset.getCreatedAt());
				} else {
					throw new DeviceException("Asset already exists with assigned name " + modifiedAssetId);
				}
			}

			// Asset Type Validations
			if (!assetType1Products.stream()
					.anyMatch(str -> str.getValue().trim().equalsIgnoreCase(a.getCategory().getValue()))) {
				if (isBulkUpload) {
					if (validationFailedAssets.get(ASSET_ERRORS[9]) != null) {
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

			// Assigned name Validations
			List<Asset> checkForUniqueAssignedName = null;
			if (a.getAssignedName() != null && !a.getAssignedName().isEmpty() && a.getId() == null) {
				checkForUniqueAssignedName = assetRepository.findByAssignedName(a.getAssignedName(),
						a.getOrganisation().getAccountNumber());
			} else if (a.getAssignedName() != null && !a.getAssignedName().isEmpty() && a.getId() != null) {
				checkForUniqueAssignedName = assetRepository.findByAssignedNameAndNotInId(a.getAssignedName(),
						a.getId(), a.getOrganisation().getAccountNumber());

			}
			if (checkForUniqueAssignedName != null && checkForUniqueAssignedName.size() > 0) {
				if (isBulkUpload) {
					if (validationFailedAssets.get(ASSET_ERRORS[3]) != null) {
						validationFailedAssets.get(ASSET_ERRORS[3]).add(a.getAssignedName());
					} else {
						List<String> failedAssetAssignedName = new ArrayList<>();
						failedAssetAssignedName.add(a.getAssignedName());
						validationFailedAssets.put(ASSET_ERRORS[3], failedAssetAssignedName);
					}
					validationFailedAssets.get(ASSET_ERRORS[0]).remove(a.getAssignedName());
					continue;
				} else {
					throw new DeviceException(
							"The Asset ID provided is already in use for another asset. Please check it and try again.",
							"Asset ID Already in Use");
				}
			}

			LOGGER.info("Validating VIN Number");
			// VIN Validations
			a.setComment("");
			// VIN Validations
			if (a.getVin() != null && !a.getVin().isEmpty()) {
//				if ((a.getCategory().equals(AssetCategory.TRAILER) || a.getCategory().equals(AssetCategory.CHASSIS))) {
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
							/*
							 * if(isBulkUpload) { continue; } else { throw new
							 * DeviceException("Failed to validate VIN Number"); }
							 */
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
					if (isMoblieApi != null && isMoblieApi && a.getUuid() != null) {
						LOGGER.info("Inside the condition of vin no check for moblie request");
						Boolean isVinNoUniqe = true;
						for (Asset assetForVIN : assetList) {
							if (assetForVIN.getUuid().equals(a.getUuid())) {
								isVinNoUniqe = false;
								break;
							}
						}
						LOGGER.info(
								"Inside the condition of vin no check for moblie request and is uniqe vin no value is : "
										+ isVinNoUniqe);
						if (isVinNoUniqe) {
							a.setVin(a.getVin() + currentTimestampInStringFormat);
//								a.setIsReviewRequired(true);
//								if(assetListFromRequest != null && assetListFromRequest.size() > 0) {
//									assetListFromRequest.get(0).setVin(a.getVin());
//								}
							LOGGER.info(
									"Inside the condition of vin no check for moblie request and vin no value is  : "
											+ a.getVin());
						}

					} else {
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
								throw new DuplicateVinException(
										"The VIN provided is already in use for another asset. Please check it and try again.");
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
									throw new DuplicateVinException(
											"The VIN provided is already in use for another asset. Please check it and try again.");
								}
							} else {
								Asset asset = assetRepository.findByVinAndNotInId(a.getVin(), a.getId());
								if (asset != null) {
									throw new DuplicateVinException(
											"The VIN provided is already in use for another asset. Please check it and try again.");
								}
							}
						}
					}
				}
//				}
			}

//			if (a.getVin() != null && !a.getVin().isEmpty()) {
//				a.setIsVinValidated(true);
//			}

			// Approved Products Validations

			// Model Year Validations
			if (a.getYear() != null && !a.getYear().isEmpty()) {
				if (a.getIsVinValidated() == null || a.getIsVinValidated() == true) {
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

			// UUID Validations
			if (a.getUuid() == null || a.getUuid().isEmpty()) {
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

//			if (a.getCreatedAt() == null) {
//				a.setCreatedAt(Instant.now());
//			}

			// validate gateway type to asset type
			List<DeviceTypeApprovedAsset> gatewayTypeApprovedAssetList = gatewayTypeApprovedAssetRepository
					.findByGatewayProductName(a.getGatewayEligibility());
			Set<String> assetTypes = gatewayTypeApprovedAssetList.stream()
					.map(gatewayTypeApprovedAsset -> gatewayTypeApprovedAsset.getAssetType())
					.collect(Collectors.toSet());
			if (!isMoblieApi && !assetTypes.contains(a.getCategory().getValue().toUpperCase())) {
				if (isBulkUpload) {
					if (validationFailedAssets.get(ASSET_ERRORS[11]) != null) {
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
			setCompanyToAssets(a, company, userName, isMoblieApi, isAssociation);
			validationFailedAssets.get(ASSET_ERRORS[0]).remove(a.getAssignedName());

//			a.setUpdatedAt(Instant.now());

			if (vinDecodeResult != null && !vinDecodeResult.isEmpty() && vinDecodeResult.get(a.getVin()) != null
					&& vinDecodeResult.get(a.getVin()).getResult() != null
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
				ManufacturerDetails manufacturerDetails = manufacturerDetailsRepository
						.findByModelAndManufacturer(model, manufacturer.getId());
				if (manufacturerDetails == null) {
					manufacturerDetails = new ManufacturerDetails();
					manufacturerDetails.setManufacturer(manufacturer);
					manufacturerDetails.setModel(model);
					boolean manufacturerDetailsUuidIsNew = false;
					String manufacturerDetailsUuid = UUID.randomUUID().toString();
					while (!manufacturerDetailsUuidIsNew) {
						ManufacturerDetails manufacturerDetailsFromDB = manufacturerDetailsRepository
								.findByUuid(manufacturerDetailsUuid);
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
				if (a.getManufacturer() == null && a.getManufacturerDetails() == null) {

					Manufacturer notAvailableManufacturer = manufacturerRepository.findByName("");
					a.setManufacturer(notAvailableManufacturer);
					ManufacturerDetails notAvailableManufacturerDetails = manufacturerDetailsRepository
							.findByModelAndManufacturer("Not Available", notAvailableManufacturer.getId());
					a.setManufacturerDetails(notAvailableManufacturerDetails);
				}
//				if(a.getYear() == null || a.getYear().isEmpty()) {
//					a.setYear("Not Available");
//				}
			}
			if (isBulkUpload) {
				a.setCreationMethod(AssetCreationMethod.UPLOAD);
			} else {
				a.setCreationMethod(AssetCreationMethod.MANUAL);
			}

//			//CreatedBy and CreatedAt set from here
//			User user = restUtils.getUserFromAuthService(userName);		
//			a.setCreatedBy(user);
//			a.setCreatedAt(Instant.now());
			assetsListToSave.add(a);
			System.out.println("size of assetlist  : " + assetsListToSave.size());

		}

		if (assetsListToSave.size() > 0) {
			LOGGER.info("call for saving Asset into database");
			assetRepository.saveAll(assetsListToSave);
		}
		validationFailedAssets.remove(ASSET_ERRORS[0]);
	}

	public Asset setCompanyToAssets(Asset asset, CompanyPayload company, String userName, Boolean isMoblieApi,
			Boolean isAssociation) {
		if (!AppUtility.isEmpty(asset.getOrganisation()) && isAssociation) {
			return asset;
		}

		User user = restUtils.getUserFromAuthService(userName);
		if (isMoblieApi != null && isMoblieApi) {
			LOGGER.info("the request for adding asset is from mobile app side or quick add asset api ");
			boolean roleAvailable = false;
			for (Role roles : user.getRole()) {
				LOGGER.info("Inside Set company for asset call the value of user role " + roles.getName());
				if (roles.getName().contains(AuthoritiesConstants.SUPER_ADMIN)
						|| roles.getName().contains(AuthoritiesConstants.ROLE_CUSTOMER_ADMIN)
						|| roles.getName().contains(AuthoritiesConstants.ROLE_INSTALLER)) {
					roleAvailable = true;
					break;
				}
			}
			if (roleAvailable) {
				asset.setOrganisation(beanConverter.companyPayloadToCompanies(company));
			} else {
				asset.setOrganisation(user.getOrganisation());
			}
		} else {
			boolean roleAvailable = false;
			for (Role roles : user.getRole()) {
				LOGGER.info("Inside Set company for asset call the value of user role " + roles.getName());
				if (roles.getName().contains(AuthoritiesConstants.SUPER_ADMIN)
						|| roles.getName().contains(AuthoritiesConstants.ROLE_CUSTOMER_ADMIN)) {
					roleAvailable = true;
					break;
				}
			}
			if (roleAvailable) {
				asset.setOrganisation(beanConverter.companyPayloadToCompanies(company));
			} else {
				asset.setOrganisation(user.getOrganisation());
			}
		}

		if (asset.getOrganisation() != null && asset.getOrganisation().getAccountNumber() != null) {
			LOGGER.info("after adding company into asset object for saving asset "
					+ asset.getOrganisation().getAccountNumber());
		}
		return asset;
	}

	@Override
	public List<AssetDTO> getAssetByIdOrVinNumber(String vin, String assetId, String accountNumber)
			throws DeviceException {

		LOGGER.info("Inside getAssetByIdOrVinNumber and fetch assetId and accountNumber",
				assetId + " " + accountNumber);

		List<AssetDTO> assetDtoList = new ArrayList<>();
		List<Asset> assetList = new ArrayList<>();
		if (assetId != null) {
			// assetList.add(assetRepository.findAssetByAssetId(assetId, accountNumber));
		} else if (vin != null) {
			// assetList.add(assetRepository.findAssetByVinNumber(vin, accountNumber));
		}
		if (assetList.size() > 0)
			assetDtoList = assetList.stream().map(beanConverter::convertAssetToAssetsDto).collect(Collectors.toList());

		return assetDtoList;
	}

	@Override
	public List<AssetDTO> getAsset(String accountNumber, AssetStatus status) throws DeviceException {

		LOGGER.info("Inside getAsset and fetch accountNumber and status", accountNumber + " " + status);
		List<AssetDTO> assetDtoList = new ArrayList<>();
		List<Asset> assetList = new ArrayList<Asset>();
		if (status == null) {
			// assetList = assetRepository.findByAccountNumber(accountNumber);
		} else {
			// assetList = assetRepository.findByAccountNumberAndStatus(accountNumber,
			// status);
		}
		if (assetList.size() > 0)
			assetDtoList = assetList.stream().map(beanConverter::convertAssetToAssetsDto).collect(Collectors.toList());

		return assetDtoList;

	}

	@Override
	public AssetVinSearchDTO getAssetVinSearch(String vin) throws Exception {

		LOGGER.info("Inside getAssetVinSearch and fetch vin" + vin);

		final String uri = "https://vpic.nhtsa.dot.gov/api/vehicles/decodevinvalues/" + vin + "*BA?format=json";
		AssetVinSearchDTO assetVinSearchDto = new AssetVinSearchDTO();
		RestTemplate restTemplate = new RestTemplate();
		NHTSAResponseDTO result = restTemplate.getForObject(uri, NHTSAResponseDTO.class);
		NHTSAResultDTO decodedVin = result.getResults().get(0);
		String errorCodeString = decodedVin.getErrorCode();
		String[] errorCodes = errorCodeString.split(",");
		if (errorCodes.length > 1) {
			for (String errorCode : errorCodes) {
				if (Integer.parseInt(errorCode) != 0) {
					throw new Exception("Error decoding VIN. " + decodedVin.getErrorText());
				}
			}
		} else {
			if (Integer.parseInt(errorCodeString) == 0) {
				assetVinSearchDto.setMake(decodedVin.getMake());
				assetVinSearchDto.setModel(decodedVin.getModel());
				assetVinSearchDto.setManufacturer(decodedVin.getManufacturer());
				assetVinSearchDto.setModelYear(decodedVin.getModelYear());
				assetVinSearchDto.setVin(decodedVin.getVIN());
				assetVinSearchDto.setBodyType(decodedVin.getBodyClass());
				assetVinSearchDto.setLength(decodedVin.getTrailerLength());
				assetVinSearchDto.setNumberOfAxles(Integer.parseInt(decodedVin.getAxles()));
				assetVinSearchDto.setCategory(decodedVin.getVehicleType());
			} else {
				throw new Exception("Error decoding VIN. " + decodedVin.getErrorText());
			}
		}

		return assetVinSearchDto;

	}

	@Override
	public AddAssetResponse updateAsset(AssetsPayload assetsPayloads, String userName) {
		LOGGER.info("Inside updateAsset and fetching assetDetails and userId value" + assetsPayloads + " " + userName);
		User user = restUtils.getUserFromAuthService(userName);
		Asset asset = beanConverter.assetsPayloadToAssets(assetsPayloads, assetsPayloads.getCompany(), false, user,
				null);
		String modifiedAssetId = StringUtils.replaceBlankSpaceWithUnderscore(asset.getAssignedName());
		Asset existingAsset = assetRepository.findAssetByAssetIdAndOrganisation(modifiedAssetId,
				assetsPayloads.getCompany().getId());
		if (existingAsset == null) {
			existingAsset = assetRepository.findByUuid(asset.getUuid());
			if (existingAsset == null) {
				throw new DeviceException("The Asset with Asset ID " + modifiedAssetId + " does not exist");
			}
		}
//		if (existingAsset.getStatus().equals(AssetStatus.PARTIAL)
//				|| existingAsset.getStatus().equals(AssetStatus.ACTIVE)) {
//			throw new DeviceException("The record with the Asset ID " + asset.getAssignedName()
//					+ " already has hardware installed, and so it cannot be Updated");
//		}

		asset.setCreatedAt(existingAsset.getCreatedAt());
		asset.setCreatedBy(existingAsset.getCreatedBy());

		List<Asset> assetList = new ArrayList<Asset>();
		assetList.add(asset);
		Map<String, List<String>> failedAssets = validationOfAssets(assetList, assetsPayloads.getCompany(), false,
				false, userName, Boolean.FALSE, Boolean.FALSE);
		AddAssetResponse addAssetResponse = new AddAssetResponse();
		addAssetResponse.setErrors(failedAssets);
		addAssetResponse.setAssetPayload(beanConverter.assetsassetsToPayload(asset));
		return addAssetResponse;
	}

	@Override
	public Page<AssetResponseDTO> getAllActiveCustomerOrganisationList(Pageable pageable, Long companyId,
			String userName, Map<String, String> filterValues, String yearFilter, String sort) {
		LOGGER.info("Inside get getAllActiveCustomerOrganisationList method");
		System.out.println("year type filter: " + yearFilter);
		User user = restUtils.getUserFromAuthService(userName);
		Page<Asset> allAssetByCompany = null;
		Specification<Asset> spc = AssetSpecification.getaAssetSpecification(filterValues, user, companyId, yearFilter,
				sort);
		LOGGER.info("Fetching values based on specifications");
		allAssetByCompany = assetRepository.findAll(spc, pageable);
		List<AssetResponseDTO> assetResponseDTOList = new ArrayList<>();
		for (Asset asset : allAssetByCompany) {
			try {
				assetResponseDTOList.add(beanConverter.convertAssetToAssetResponseDTO(asset));
			} catch (JsonProcessingException e) {
				LOGGER.error("Exception while converting asset to AssetResponseDTO", e);
			}
		}
		Page<AssetResponseDTO> page = new PageImpl<>(assetResponseDTOList, pageable,
				allAssetByCompany.getTotalElements());

		return page;
	}

	@Override
	public Page<AssetRecordPayload> getCustomerAssetsList(Pageable pageable, String userName,
			Map<String, String> filterValues, String filterModelCountFilter, String sort) {
		Page<AssetRecord> customerAssets = null;
		User user = restUtils.getUserFromAuthService(userName);
		Specification<AssetRecord> spc = CustomAssetSpecification.getCustomerAsetSpecification(filterValues, user,
				filterModelCountFilter, sort);
		customerAssets = assetRecordRepository.findAll(spc, pageable);
		Page<AssetRecordPayload> assetRecordPayloadPage = beanConverter
				.convertAssetRecordToAssetRecordPayload(customerAssets, pageable);
		return assetRecordPayloadPage;
	}

	@Override
	public List<String> getSuperAdminUser() {
		return restUtils.getSuperAdminUser();
	}

	@Override
	public AssetResponseDTO getAssetsById(String uuid) {
		LOGGER.info("Inside get getAssetsById method");
		Asset asset = assetRepository.findByUuid(uuid);
		AssetResponseDTO assetResponseDTO = new AssetResponseDTO();
		if (asset != null) {
			try {
				assetResponseDTO = beanConverter.convertAssetToAssetResponseDTO(asset);
			} catch (JsonProcessingException e) {
				throw new DeviceException("Error in converting the Bean to dto: error = " + e.getMessage());
			}
		}
		return assetResponseDTO;
	}

	@Override
	public Map<String, List<String>> addAssets(AssetCompany assets, String userName) throws DeviceException {
		LOGGER.info("Inside addAssets", assets);
		User user = restUtils.getUserFromAuthService(userName);
		Map<String, List<String>> validationFailedAssets = new HashMap<>();
		List<Asset> assetsList = new ArrayList<>();
		for (AssetsPayload assetPayload : assets.getAssetList()) {
			assetsList.add(beanConverter.assetsPayloadToAssets(assetPayload, assets.getCompany(), true, user,
					validationFailedAssets));
		}
		validationFailedAssets.putAll(validationOfAssets(assetsList, assets.getCompany(), true, false, userName,
				Boolean.FALSE, Boolean.FALSE));

		LOGGER.info("Existing  addAssets", assets);
		validationFailedAssets.remove(ASSET_ERRORS[0]);
		return validationFailedAssets;
	}

	@Override
	public Page<AssetResponseDTO> getAllActiveCompanyList(Pageable pageable, Long companyId, String userName,
			Map<String, String> filterValues, String yearFilter) {
		User user = restUtils.getUserFromAuthService(userName);
		Page<Asset> allAssetByCompany = null;
		Specification<Asset> spc = AssetSpecification.getaAssetSpecification(filterValues, user, companyId, yearFilter,
				null);
		allAssetByCompany = assetRepository.findAll(spc, pageable);
		List<AssetResponseDTO> assetResponseDTOList = new ArrayList<>();
		LOGGER.info("Go to inside convertAssetToAssetResponseDTO");
		for (Asset asset : allAssetByCompany) {
			try {
				assetResponseDTOList.add(beanConverter.convertAssetToAssetResponseDTO(asset));
			} catch (JsonProcessingException e) {
				LOGGER.error("Exception while converting asset to AssetResponseDTO", e);
			}
		}
		Page<AssetResponseDTO> page = new PageImpl<>(assetResponseDTOList, pageable,
				allAssetByCompany.getTotalElements());
		return page;
//		List<String> assetUuids = allAssetByCompany.stream().map(asset -> asset.getUuid()).collect(Collectors.toList());
//		Map<String, InstallHistory> mapFromListOfInstallHistory = new HashMap<>();
//		List<InstallHistory> installHistories = new ArrayList<InstallHistory>();
//		if (assetUuids != null && !assetUuids.isEmpty()) {
//			if (filterValues.containsKey("imei") || filterValues.containsKey("installed")) {
//				installHistories = restUtils.getInstallHistoryListForAssetUuids(assetUuids, filterValues);
//				mapFromListOfInstallHistory = beanConverter.createMapFromListOfInstallHistory(installHistories);
//			} else {
//				installHistories = restUtils.getInstallHistoryListForAssetUuids(assetUuids, null);
//				mapFromListOfInstallHistory = beanConverter.createMapFromListOfInstallHistory(installHistories);
//			}
//
//		}
//		List<AssetResponseDTO> assetResponseDTOList = new ArrayList<>();
//		if (filterValues.containsKey("imei") || filterValues.containsKey("installed")) {
//			for (Asset asset : allAssetByCompany) {
//				InstallHistory ih = mapFromListOfInstallHistory.get(asset.getUuid());
//				if (ih != null && ih.getAsset().getUuid().equalsIgnoreCase(asset.getUuid())) {
//					try {
//						assetResponseDTOList.add(beanConverter.convertAssetToAssetResponseDTO(asset,
//								mapFromListOfInstallHistory.get(asset.getUuid())));
//
//					} catch (JsonProcessingException e) {
//						LOGGER.error("Exception while converting asset to AssetResponseDTO", e);
//					}
//				}
//			}
//			Page<AssetResponseDTO> page = new PageImpl<>(assetResponseDTOList, pageable, assetResponseDTOList.size());
//			return page;
//		} else {
//			for (Asset asset : allAssetByCompany) {
//				try {
//					assetResponseDTOList.add(beanConverter.convertAssetToAssetResponseDTO(asset,
//							mapFromListOfInstallHistory.get(asset.getUuid())));
//				} catch (JsonProcessingException e) {
//					LOGGER.error("Exception while converting asset to AssetResponseDTO", e);
//				}
//			}
//			Page<AssetResponseDTO> page = new PageImpl<>(assetResponseDTOList, pageable,
//					allAssetByCompany.getTotalElements());
//			return page;
//		}
	}

	@Override
	@Transactional
	public List<String> deleteBatchAssets(List<String> assetUuids) throws DeviceException {
		List<String> failedAssets = null;
		List<Asset> assets = assetRepository.getAssetsByUuids(assetUuids);
		List<Asset> assetsToBeDeleted = new ArrayList<Asset>();
		List<Asset_Device_xref> assetsDeviceToBeDeleted = new ArrayList<Asset_Device_xref>();
		Set<Long> assetXrefIds = new HashSet<>();
		if (assets.size() > 0) {
			failedAssets = new ArrayList<String>();
			for (Asset asset : assets) {
				if (asset.getStatus() != null && (asset.getStatus().equals(AssetStatus.PARTIAL)
						|| asset.getStatus().equals(AssetStatus.ACTIVE)
						|| asset.getStatus().equals(AssetStatus.INSTALL_IN_PROGRESS))) {
					failedAssets.add(asset.getAssignedName());
				} else {
					assetsToBeDeleted.add(asset);
					List<Asset_Device_xref> assetDeviceList = assetDeviceXrefRepository
							.findAllByAssetId(asset.getUuid());
					if (assetDeviceList != null && assetDeviceList.size() > 0) {
						assetsDeviceToBeDeleted.addAll(assetDeviceList);
						assetXrefIds = assetDeviceList.stream().map(Asset_Device_xref::getId)
								.collect(Collectors.toSet());
					}
				}
			}
			if (assetXrefIds.size() > 0) {
				for (Long ids : assetXrefIds) {
					deviceRepository.updateDeviceAssetMapping(ids);
				}
			}
			assetDeviceXrefRepository.deleteAll(assetsDeviceToBeDeleted);
			assetRepository.deleteAll(assetsToBeDeleted);
		} else {
			throw new DeviceException("No assets found.");
		}
		return failedAssets;
	}

	@Override
	public Boolean checkForAssetOverwrite(Long companyId) {
		Boolean hasExistingAsset = assetRepository.getAssetCountByCompanyId(companyId) > 0;
		return hasExistingAsset;
	}

	@Override
	public Map<String, List<String>> addModifiedAssets(AssetCompany assets, String userName) throws DeviceException {
		Map<String, List<String>> failedAssets = null;
		List<AssetsPayload> assetsToBeIgnored = new ArrayList<AssetsPayload>();
		for (AssetsPayload asset : assets.getAssetList()) {
			String modifiedAssetId = StringUtils.replaceBlankSpaceWithUnderscore(asset.getAssignedName());
			Asset existingAsset = assetRepository.findAssetByAssetIdAndCompany(modifiedAssetId,
					assets.getCompany().getId());
			if (existingAsset != null) {
				if (existingAsset.getStatus().equals(AssetStatus.PARTIAL)
						|| existingAsset.getStatus().equals(AssetStatus.ACTIVE)) {
					assetsToBeIgnored.add(asset);
				}
			}
		}
		assets.getAssetList().removeAll(assetsToBeIgnored);
		failedAssets = addAssets(assets, userName);
		return failedAssets;
	}

	@Override
	public Asset findByUuid(String uuid) {
		String methodName = "findByUuid";

		Logutils.log(className, methodName, uuid, "Inside findByUuid Method From InstallerService ", logger);
		return assetRepository.findByUuid(uuid);
	}

	// -----------------------------------------------Aamir---------------------------------------------------------------------//

	@Override
	public Asset_Device_xref saveAssetDeviceXref(SaveAssetGatewayXrefRequest saveAssetGatewayXrefRequest,
			String userName) throws Exception {
		// TODO Auto-generated method stub
		Asset_Device_xref assetDeviceXref = new Asset_Device_xref();
		Asset asset = assetRepository.findByUuid(saveAssetGatewayXrefRequest.getAssetUuid());
		assetDeviceXref.setAsset(asset);
		Device device = deviceRepository.findByImei(saveAssetGatewayXrefRequest.getImei());
		if (device == null) {
			device = deviceRepository.findByMac_address(saveAssetGatewayXrefRequest.getImei());
		}
		assetDeviceXref.setDevice(device);
		assetDeviceXref
				.setDateCreated(Instant.ofEpochMilli(Long.parseLong(saveAssetGatewayXrefRequest.getDatetimeRT())));

		User user = restUtils.getUserFromAuthService(userName);
		assetDeviceXref.setCreatedBy(user);
		assetDeviceXref.setActive(saveAssetGatewayXrefRequest.getIsActive());
		Asset_Device_xref save = assetDeviceXrefRepository.save(assetDeviceXref);
		return save;
	}

	@Override
	public Boolean updateCompanyInAsset(String accountNumber, String asset_uuid) {
//		logger.info("Asset compnay is null now we are updating the asset company " + accountNumber);
		Asset asset = assetRepository.findByUuid(asset_uuid);
		try {
			Organisation com = restUtils.getCompanyFromCompanyService(accountNumber);
			asset.setOrganisation(com);
			assetRepository.save(asset);
			return true;
		} catch (Exception e) {
			return false;
		}

	}

	@Override
	public Boolean isAssetHavePrePairProducts(String assetUuid) {
//		logger.info("Inside the Device Service from the isAssetHavePrePairProducts method");
		Boolean status = false;
		Asset asset = assetRepository.findByUuid(assetUuid);
		if (asset != null) {
			List<AssetSensorXref> listOfAssetSensorXref = iAssetSensorXrefRepository.findByAssetUuid(assetUuid);
			if (listOfAssetSensorXref != null && listOfAssetSensorXref.size() > 0) {
//				logger.info("Size of AssetSensorXref : " + listOfAssetSensorXref.size());
				status = true;
//				logger.info("Value of isAssetHavePrePairProducts : " + status);
			}
		} else {
			throw new DeviceException("Asset uuid not found");
		}
//		logger.info("Exiting from the Device Service from the isAssetHavePrePairProducts method");
		return status;
	}

	@Override
	public List<AssetSensorXref> getAllAssetSensorXrefForAssetUuid(String assetUuid) {
		List<AssetSensorXref> listOfAssetSensorXref = null;
		if (assetUuid != null) {

			listOfAssetSensorXref = assetSensorXrefRepository.findByAssetUuid(assetUuid);
		}
		return listOfAssetSensorXref;
	}

	@Override
	public Boolean deleteAssetByAssetUuid(String assetUuid) {
//		logger.info("Inside the method deleteAssetByAssetUuid and UUID : " + assetUuid);
		Boolean isDeleted = false;
		if (assetUuid != null && !assetUuid.isEmpty()) {
			Asset asset = assetRepository.findByUuid(assetUuid);
			if (asset != null) {
				assetRepository.delete(asset);
				isDeleted = true;
//				logger.info("Inside the method deleteAssetByAssetUuid and isDeleted : " + isDeleted);
			}
		}
//		logger.info("Exiting from the method deleteAssetByAssetUuid and isDeleted : " + isDeleted);
		return isDeleted;
	}

	@Override
	public Asset getAssetByVin(String vin) {
		String methodName = "getAssetByVin";
		Context context = new Context();
		String logUuid = context.getLogUUId();
		List<Asset> byVinNumber = assetRepository.findByVinNumber(vin);
		if (byVinNumber != null && byVinNumber.size() > 0) {

			Logutils.log(className, methodName, logUuid, " list of byVinNumber : " + byVinNumber, logger);
		}
		return byVinNumber.get(0);
	}

	@Override
	public MessageDTO uploadAssetAssociation(AssetAssociationPayload assetAssociationPayload) {
		JwtUser jwtUser = (JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		LOGGER.info("Username : " + jwtUser.getUsername());
//		if (assetAssociationPayload.getAccountNumber() == null
//				|| assetAssociationPayload.getAccountNumber().length() <= 0) {
//			return new MessageDTO<>("Account Number Can Not Null", null, false);
//		}
		Organisation com = restUtils.getCompanyFromCompanyService(assetAssociationPayload.getAccountNumber());
		LOGGER.info("after rest call from get company", com);
		if (AppUtility.isEmpty(com)) {
			return new MessageDTO<>("Company Not Found", null, false);
		}
		LOGGER.info("Check Assocaition payload not null");
		if (AppUtility.isEmpty(assetAssociationPayload.getAssetPayload())) {
			return new MessageDTO<>("AssociationDataNull", null, false);
		}

		Set<String> deviceIdList = assetAssociationPayload.getAssetPayload().stream().map(item -> item.getDeviceId())
				.collect(Collectors.toSet());
		Set<String> deviceIdListWithoutCompanyObj = assetAssociationPayload.getAssetPayload().stream()
				.filter(e -> (AppUtility.isEmpty(e.getCompanyPayload())
						|| AppUtility.isEmpty(e.getCompanyPayload().getAccountNumber())))
				.map(item -> item.getDeviceId()).collect(Collectors.toSet());
		Set<AssociationPayload> deviceIdListWithCompanyObj = assetAssociationPayload.getAssetPayload().stream()
				.filter(e -> (!AppUtility.isEmpty(e.getCompanyPayload())
						&& !AppUtility.isEmpty(e.getCompanyPayload().getAccountNumber())))
				.collect(Collectors.toSet());
		LOGGER.info("Get Device by Device List for checking device available on DB");
		List<Device> deviceList = deviceRepository.getListOfDeviceByImeiList(deviceIdList);

		if (AppUtility.isEmpty(deviceList)) {
			LOGGER.info("All Device not found in DB");
			return new MessageDTO<>("DeviceNotFound", null, false);
		}

		if (deviceIdList.size() != deviceList.size()) {
			LOGGER.info("Some device not available in DB");
			return new MessageDTO<>("DeviceNotFound", null, false);
		}

		LOGGER.info("Checking same customer associated to device ids");

		Set<String> deviceIdList2 = new HashSet<>();
		for (String deviceId : deviceIdListWithoutCompanyObj) {
			Set<String> deviceIdList3 = deviceList.stream().filter(e -> e.getImei().equals(deviceId)
					&& !e.getOrganisation().getAccountNumber().equals(assetAssociationPayload.getAccountNumber()))
					.map(item -> item.getImei()).collect(Collectors.toSet());
			if (!AppUtility.isEmpty(deviceIdList3)) {
				return new MessageDTO<>("DiffrentEndCustomer", deviceId, false);
			}
		}
		for (AssociationPayload associationPayload : deviceIdListWithCompanyObj) {
			Set<String> deviceIdList3 = deviceList.stream()
					.filter(e -> e.getImei().equals(associationPayload.getDeviceId()) && !e.getOrganisation()
							.getAccountNumber().equals(associationPayload.getCompanyPayload().getAccountNumber()))
					.map(item -> item.getImei()).collect(Collectors.toSet());
			if (!AppUtility.isEmpty(deviceIdList3)) {
				return new MessageDTO<>("DiffrentEndCustomer", associationPayload.getDeviceId(), false);
			}
		}

		LOGGER.info("Preparing the Asset id list from Asset Association Payload");
		Set<String> assetIdList = assetAssociationPayload.getAssetPayload().stream().map(item -> item.getAssetId())
				.collect(Collectors.toSet());
		LOGGER.info("Find asset using list of asset ID ");
		List<Asset> assetList = assetRepository.getAssetsByAssignedName(assetIdList);
		Map<String, List<String>> validationFailedAssets = new HashMap<>();
		CompanyPayload companyPayload = new CompanyPayload();
		companyPayload.setId(com.getId());
		companyPayload.setCompanyName(com.getOrganisationName());
		companyPayload.setShortName(com.getShortName());
		companyPayload.setAccountNumber(com.getAccountNumber());
		companyPayload.setIsAssetListRequired(com.getIsAssetListRequired());
		companyPayload.setUuid(com.getUuid());
//		if (AppUtility.isEmpty(assetList)) {
		LOGGER.info("Some Asset not added");
//			Set<String> assetIdList2 = assetList.stream().map(item -> item.getAssignedName())
//					.collect(Collectors.toSet());
//			assetIdList.removeAll(assetIdList2);
		List<Asset> assetList2 = new ArrayList<>();
		for (AssociationPayload payload : assetAssociationPayload.getAssetPayload()) {
			Asset asset = assetList.stream().filter(e -> e.getAssignedName().equalsIgnoreCase(payload.getAssetId()))
					.findAny().orElse(null);
			if (asset != null) {
				if (asset.getOrganisation() != null && !(payload.getCompanyPayload() != null && asset.getOrganisation()
						.getAccountNumber().equalsIgnoreCase(payload.getCompanyPayload().getAccountNumber()))) {
					if (payload.getCompanyPayload() != null) {
						asset.setOrganisation(beanConverter.companyPayloadToCompanies(payload.getCompanyPayload()));
					} else if (!(asset.getOrganisation().getAccountNumber().equalsIgnoreCase(com.getAccountNumber()))) {
						asset.setOrganisation(com);
					}
					assetList2.add(asset);
				}
			} else {
				assetList2.add(assetsAssociationPayloadToAssets(payload, com));
			}
//				List<AssociationPayload> associationPayload = assetAssociationPayload.getAssetPayload().stream()
//						.filter(e -> e.getAssetId().equals(assetId)).collect(Collectors.toList());
//				if (!AppUtility.isEmpty(associationPayload) && associationPayload.size() > 0) {
//					AssociationPayload payload = associationPayload.get(0);

//				}
//			}

			LOGGER.info("Validating and Saving asset");
			validationFailedAssets.putAll(validationOfAssets(assetList2, companyPayload, true, false,
					jwtUser.getUsername(), Boolean.FALSE, Boolean.TRUE));
			validationFailedAssets.remove(ASSET_ERRORS[0]);
		}

		assetIdList = assetAssociationPayload.getAssetPayload().stream().map(item -> item.getAssetId())
				.collect(Collectors.toSet());
		LOGGER.info("After adding asset find the asset list by Asset Id");
		assetList = assetRepository.getAssetsByAssignedName(assetIdList);
		List<Asset_Device_xref> assetDeviceXrefList = new ArrayList<>();
		List<String> errorAsset = new ArrayList<>();
		List<Asset> updateAssetList = new ArrayList<>();
		for (Device device : deviceList) {
			LOGGER.info("Checking the Asset and Device Association");
			List<AssociationPayload> associationPayload = assetAssociationPayload.getAssetPayload().stream()
					.filter(e -> e.getDeviceId().equals(device.getImei())).collect(Collectors.toList());
			if (!AppUtility.isEmpty(associationPayload)) {
				AssociationPayload assetPayload = associationPayload.get(0);
				List<Asset> payload = assetList.stream()
						.filter(e -> e.getAssignedName().equals(assetPayload.getAssetId()))
						.collect(Collectors.toList());
				if (!AppUtility.isEmpty(payload)) {
					Asset assets = payload.get(0);
					LOGGER.info("Finding the Asset Device Association Using AssetUuid and Device Uuid");
					List<Asset_Device_xref> assetDeviceXref = assetDeviceXrefRepository
							.findAssetDeviceByAssetUuidAndDeviceUuid(assets.getUuid(), device.getUuid());
					if (AppUtility.isEmpty(assetDeviceXref)) {
						LOGGER.info("Preparing the asset association Payload for saving into db");
						Asset_Device_xref assetDeviceXrefObj = prepareAssetDeviceXrefObj(assets, device);
						assetDeviceXrefList.add(assetDeviceXrefObj);
					} else {
						LOGGER.info("Checking Asset Association Data");
						Asset_Device_xref asset_Device_xref = assetDeviceXref.get(0);
						if (asset_Device_xref.getDevice().getUuid().equals(device.getUuid())
								&& asset_Device_xref.getAsset().getUuid().equals(assets.getUuid())) {
							// Update asset if different data.
							Asset updateAsset = updateAssetDataIfSomethingNew(assetPayload,
									asset_Device_xref.getAsset());
							if (!AppUtility.isEmpty(updateAsset)) {
								updateAssetList.add(updateAsset);
							}
						} else {
							deviceRepository.updateDeviceAsset(asset_Device_xref.getDevice().getImei());
							assetDeviceXrefRepository.delete(asset_Device_xref);
							// return wrong asset data
							errorAsset.add(assets.getAssignedName());
							Asset_Device_xref assetDeviceXrefObj = prepareAssetDeviceXrefObj(assets, device);
							assetDeviceXrefList.add(assetDeviceXrefObj);
						}
					}
				}
			}

		}
		if (!AppUtility.isEmpty(updateAssetList)) {
			try {
				LOGGER.info("Go for Update Asset details Vin, Category, Manufacturer");
				validationFailedAssets.putAll(validationOfAssets(updateAssetList, companyPayload, true, false,
						jwtUser.getUsername(), Boolean.FALSE, Boolean.TRUE));
				LOGGER.info("Successfully updated asset");
			} catch (Exception ex) {
				LOGGER.info("Exception while Updating Asset details Vin, Category, Manufacturer");
				ex.printStackTrace();
			}
		}
		if (!AppUtility.isEmpty(assetDeviceXrefList)) {
			LOGGER.info("Saving Asset association into DB");
			assetDeviceXrefRepository.saveAll(assetDeviceXrefList);
			try {
				deviceRepository.updateDeviceAssetData();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		Set<String> finalAssetIdList = assetAssociationPayload.getAssetPayload().stream().map(item -> item.getAssetId())
				.collect(Collectors.toSet());
		LOGGER.info("Find the Final List for asset insertd into DB");
		List<Asset> finalAssetList = assetRepository.getAssetsByAssignedName(finalAssetIdList);
		Set<String> assetIdList2 = finalAssetList.stream().map(item -> item.getAssignedName())
				.collect(Collectors.toSet());
		Set<String> assetUuidList = finalAssetList.stream().map(item -> item.getUuid()).collect(Collectors.toSet());
		if (AppUtility.isEmpty(finalAssetIdList) && finalAssetIdList.size() != finalAssetList.size()) {
			LOGGER.info("Find the final Asset association into DB");
			List<Asset_Device_xref> assetDeviceXref = assetDeviceXrefRepository.findAllByAssetUuid(assetUuidList);
			List<String> assetXrefIdList = assetDeviceXref.stream().map(item -> item.getAsset().getAssignedName())
					.collect(Collectors.toList());
			finalAssetIdList.removeAll(assetIdList2);
			assetIdList2.removeAll(assetXrefIdList);
			finalAssetIdList.addAll(deviceIdList2);
			LOGGER.info("FInal Asset list the are not associated: " + finalAssetIdList);
		} else {
			LOGGER.info("Find the final Asset association into DB in equal condition");
			List<Asset_Device_xref> assetDeviceXref = assetDeviceXrefRepository.findAllByAssetUuid(assetUuidList);
			List<String> assetXrefIdList = assetDeviceXref.stream().map(item -> item.getAsset().getAssignedName())
					.collect(Collectors.toList());
			finalAssetIdList.removeAll(assetXrefIdList);
			LOGGER.info("FInal Device list the are not associated: " + finalAssetIdList);
		}
		List<AssociationPayload> finalUnAssociationPayload = new ArrayList<>();
		if (!AppUtility.isEmpty(finalAssetIdList)) {
			LOGGER.info("Getting final Asset Device Details for Sending mail");
			// send mail for non creation and non association asset;
			for (String assetIds : finalAssetIdList) {
				List<AssociationPayload> unAssociationPayload = assetAssociationPayload.getAssetPayload().stream()
						.filter(e -> e.getAssetId().equals(assetIds)).collect(Collectors.toList());
				if (!AppUtility.isEmpty(unAssociationPayload)) {
					AssociationPayload unAssociatAssetPayload = unAssociationPayload.get(0);
					finalUnAssociationPayload.add(unAssociatAssetPayload);
				}
			}
		}
		if (!AppUtility.isEmpty(finalUnAssociationPayload)) {
			LOGGER.info("Sending the mail");
			mailUtil.sendMailForAssetAssociation(finalUnAssociationPayload, jwtUser.getUsername());
		}

		if (!AppUtility.isEmpty(validationFailedAssets)) {
			LOGGER.info("Checking for invalid VIN number");
			for (Map.Entry<String, List<String>> entry : validationFailedAssets.entrySet()) {
				LOGGER.info(entry.getKey());
				LOGGER.info(entry.getValue() + "");
				if (entry.getKey().contains("The VIN provided for the following record")
						|| entry.getKey().contains("The VINs provided for the following record")) {
					return new MessageDTO<>("WrongVIN", validationFailedAssets, false);
				} else if (entry.getKey().contains("VIN in use for another asset")) {
					LOGGER.info("Another asset");
				}
			}
		}

		return new MessageDTO<>("Associated", validationFailedAssets, true);
	}

	public Asset_Device_xref prepareAssetDeviceXrefObj(Asset assets, Device device) {
		Asset_Device_xref assetDeviceXrefObj = new Asset_Device_xref();
		assetDeviceXrefObj.setDevice(device);
		assetDeviceXrefObj.setAsset(assets);
		assetDeviceXrefObj.setActive(true);
		assetDeviceXrefObj.setCreatedOn(Instant.now());
		assetDeviceXrefObj.setUpdatedOn(Instant.now());
		try {
			redisDeviceRepository.addMap(DEVICE_CURRENT_VIEW_PREFIX + device.getImei(), "assetId",
					assets.getAssignedName());
			redisDeviceRepository.addMap(DEVICE_CURRENT_VIEW_PREFIX + device.getImei(), "assetType",
					assets.getCategory().getValue());
		} catch (Exception ex) {
			LOGGER.info("Exception while updating asset data on Redis");
			ex.printStackTrace();
		}
		return assetDeviceXrefObj;
	}

	public Asset updateAssetDataIfSomethingNew(AssociationPayload associationPayload, Asset xrefAsset) {
		boolean noChanges = true;
		try {
			if (associationPayload != null) {
				if (!AppUtility.isEmpty(associationPayload.getAssetType())
						&& !AssetCategory.getAssetCategory(associationPayload.getAssetType().toUpperCase())
								.equals(xrefAsset.getCategory())) {
					xrefAsset.setCategory(
							AssetCategory.getAssetCategory(associationPayload.getAssetType().toUpperCase()));
					noChanges = false;
				}
				if (!AppUtility.isEmpty(associationPayload.getVin())
						&& !associationPayload.getVin().equals(xrefAsset.getVin())) {
					xrefAsset.setVin(associationPayload.getVin());
					noChanges = false;
				}
//				if (!AppUtility.isEmpty(associationPayload.getMake())) {
//					if (AppUtility.isEmpty(xrefAsset.getManufacturer())) {
//						Manufacturer manufacturer = manufacturerRepository.findByName(associationPayload.getMake());
//						xrefAsset.setManufacturer(manufacturer);
//						noChanges = false;
//					} else {
//						if (!associationPayload.getMake().equalsIgnoreCase(xrefAsset.getManufacturer().getName())) {
//							Manufacturer manufacturer = manufacturerRepository.findByName(associationPayload.getMake());
//							xrefAsset.setManufacturer(manufacturer);
//							noChanges = false;
//						}
//					}
//				}

			}
		} catch (Exception ex) {
			LOGGER.info("Exception while Preparing update object for asset" + ex.getMessage());
			ex.printStackTrace();
			noChanges = true;
		}

		if (noChanges) {
			return null;
		} else {
			return xrefAsset;
		}
	}

	public Asset assetsAssociationPayloadToAssets(AssociationPayload associationPayload, Organisation organisations) {
		LOGGER.info("Inside assetsAssociationPayloadToAssets method and fetch AssetsPayload " + associationPayload);
		Asset asset = new Asset();
		asset.setAssignedName(associationPayload.getAssetId());
		asset.setCategory(AssetCategory.getAssetCategory(associationPayload.getAssetType().toUpperCase()));
		asset.setGatewayEligibility(associationPayload.getGatewayEligibility());
		if (!AppUtility.isEmpty(associationPayload.getMake())) {
			LOGGER.info("Finding the Manufacturer by Name" + associationPayload.getMake());
			Manufacturer manufacturer = manufacturerRepository.findByName(associationPayload.getMake());
			if (manufacturer != null) {
				asset.setManufacturer(manufacturer);
			} else {
				LOGGER.info("Manufacturer not found" + associationPayload.getMake());
			}
		}
		asset.setVin(associationPayload.getVin());
		asset.setStatus(AssetStatus.PENDING);
		asset.setOrganisation(organisations);
		return asset;
	}

	private String addTimestampInStringFormat() {
		String fld = "";
		try {
			Instant instant = Instant.now();
			long timeStampMillis = instant.toEpochMilli();
			fld = "-" + timeStampMillis;
		} catch (Exception e) {
			LOGGER.error("Getting error while generating timestam" + e.getMessage());
		}
		return fld;
	}

	@Override
	public Page<AssetResponseDTO> getAssets(String accountNumber, String vin, String assignedName, String status,
			String eligibleGateway, Context context, Pageable pageable, MessageDTO<Page<AssetResponseDTO>> messageDto)
			throws DeviceException, JsonProcessingException {
		Page<AssetResponseDTO> assetResponseDtoList = null;
		List<AssetResponseDTO> assetDtoList = new ArrayList<>();
		String methodName = "getAssets";
		Logutils.log(className, methodName, context.getLogUUId(), " assignedName ", LOGGER, context.getLogUUId(),
				assignedName != null ? assignedName : "");
		Page<Asset> assetList = null;
		Specification<Asset> spc = AssetSpecification.getAssetListSpecification(accountNumber, vin, assignedName,
				status, eligibleGateway);
		assetList = assetRepository.findAll(spc, pageable);
		Logutils.log(className, methodName, context.getLogUUId(), " list of array get from the findAll method ", LOGGER,
				context.getLogUUId());
		Logutils.log(className, methodName, context.getLogUUId(),
				" After calling repository findAll method from assetRepository ", LOGGER, context.getLogUUId(),
				accountNumber);
		if (assetList == null || assetList.getContent() == null || assetList.getContent().size() < 1) {
			throw new DeviceException("No such asset found");
		}
		for (Asset asset : assetList.getContent()) {
			AssetResponseDTO assetResponseDto = beanConverter.convertAssetToAssetResponseDTO(asset, null);
			assetDtoList.add(assetResponseDto);
		}

		if (assetDtoList != null && assetDtoList.size() > 0) {
			assetResponseDtoList = new PageImpl<>(assetDtoList);
			messageDto.setBody(assetResponseDtoList);
			messageDto.setTotalKey(assetList.getTotalElements());
			messageDto.setCurrentPage(assetList.getNumber());
			messageDto.setTotal_pages(assetList.getTotalPages());
		}
		Logutils.log(className, methodName, context.getLogUUId(), "completed coverting reponse ", LOGGER,
				context.getLogUUId(), accountNumber);

		return assetResponseDtoList;
	}

	// -----------------------------------------------Aamir---------------------------------------------------------------------//
	@Override
	public List<AssetResponseDTO> getAssetsByAssetId(String assetId, String imei) throws JsonProcessingException {
		List<Asset_Device_xref> assetList = new ArrayList<>();
		if (!AppUtility.isEmpty(assetId)) {
			assetList = assetDeviceXrefRepository.findByAssetId(assetId);
		}
		if (!AppUtility.isEmpty(imei)) {
			assetList = assetDeviceXrefRepository.findAllByDeviceId(imei);
		}
		return beanConverter.convertAssetToAssetResponseDTO(assetList);
	}

}