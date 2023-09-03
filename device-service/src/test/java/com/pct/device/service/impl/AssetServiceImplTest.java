package com.pct.device.service.impl;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.pct.common.constant.AssetCategory;
import com.pct.common.constant.AssetCreationMethod;
import com.pct.common.constant.AssetStatus;
import com.pct.common.constant.OrganisationRole;
//import com.pct.common.constant.OrganisationType;
import com.pct.common.model.Asset;
import com.pct.common.model.Manufacturer;
import com.pct.common.model.ManufacturerDetails;
import com.pct.common.model.Organisation;
import com.pct.common.model.PermissionEntity;
import com.pct.common.model.Role;
import com.pct.common.model.User;
import com.pct.device.constant.AssetVinComment;
import com.pct.device.dto.AssetDTO;
import com.pct.device.dto.AssetResponseDTO;
import com.pct.device.dto.AssetVinSearchDTO;
import com.pct.device.dto.NHTSAResponseDTO;
import com.pct.device.dto.NHTSAResultDTO;
import com.pct.device.dto.VinDecodeResultDTO;
import com.pct.device.exception.DeviceException;
import com.pct.device.exception.DuplicateVinException;
import com.pct.device.model.AssetRecord;
import com.pct.device.model.Lookup;
import com.pct.device.payload.AddAssetResponse;
import com.pct.device.payload.AssetRecordPayload;
import com.pct.device.payload.AssetsPayload;
import com.pct.device.payload.CompanyPayload;
import com.pct.device.repository.IAssetRecordRepository;
import com.pct.device.repository.IAssetRepository;
import com.pct.device.repository.IDeviceTypeApprovedAssetRepository;
import com.pct.device.repository.ILookupRepository;
import com.pct.device.repository.IManufacturerDetailsRepository;
import com.pct.device.repository.IManufacturerRepository;
import com.pct.device.service.VinDecoderService;
import com.pct.device.specification.AssetSpecification;
import com.pct.device.specification.CustomAssetSpecification;
import com.pct.device.util.AuthoritiesConstants;
import com.pct.device.util.BeanConverter;
import com.pct.device.util.RestUtils;
import com.pct.device.util.StringUtils;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
public class AssetServiceImplTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(AssetServiceImplTest.class);
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
			"The VIN provided for the following record(s) could not be validated because the service is unavailable. These assets have been added with a notation of the VIN error. " };

	public static final String className = "AssetServiceImplTest";

	@InjectMocks
	private AssetServiceImpl service;

	@Mock
	private IAssetRepository assetRepository;

	@Mock
	private BeanConverter beanConverter;

	@Mock
	private RestUtils restUtils;

	@Mock
	private ILookupRepository assetPropertiesRepository;

	@Mock
	private VinDecoderService vinDecoderService;

	@Mock
	private IManufacturerRepository manufacturerRepository;

	@Mock
	private IManufacturerDetailsRepository manufacturerDetailsRepository;

	@Mock
	private IDeviceTypeApprovedAssetRepository gatewayTypeApprovedAssetRepository;

	@Mock
	private IAssetRecordRepository assetRecordRepository;

	@Mock
	Specification<Asset> spc;

	@Mock
	Specification<AssetRecord> spc1;

	@Mock
	private Pageable pageableMock;

	private Asset getAsset() {
		Long userId = 57L;
		Manufacturer manufacturer1 = new Manufacturer();
		manufacturer1.setName("name");
		manufacturer1.setUuid("84d6efc0-37ad-4cde-98c9-79611e2fa");

		AssetCategory category = AssetCategory.CHASSIS;

		User createdBy = new User();
		createdBy.setCountryCode("CountryCode");
		createdBy.setEmail("@gmail");
		createdBy.setFirstName("firstName");
		createdBy.setIsActive(true);
		createdBy.setIsDeleted(true);
		createdBy.setIsPasswordChange(true);
		createdBy.setLastName("lastName");

		AssetCreationMethod assetCreationMethod = AssetCreationMethod.UPLOAD;

		ManufacturerDetails manufacturerDetails = new ManufacturerDetails();
		manufacturerDetails.setId(userId);
		manufacturerDetails.setConfig("config");
		manufacturerDetails.setManufacturer(manufacturer1);
		manufacturerDetails.setModel("model");
		manufacturerDetails.setUuid("84d6efc0-37ad-4cde-98c9-79611e2fa");

		Asset asset = new Asset();
		asset.setAssignedName("assigned_name");
		asset.setComment("comment");
		asset.setUuid("84d6efc0-37ad-4cde-98c9-79611e2fa");
		asset.setCategory(category);
		asset.setCreatedBy(createdBy);
		asset.setCreatedAt(Instant.now());
		asset.setGatewayEligibility("gateway");
		asset.setIsApplicableForPrePair(true);
		asset.setIsVinValidated(true);
		asset.setUpdatedBy(createdBy);
		asset.setUpdatedAt(Instant.now());
		asset.setUuid("84d6efc0-37ad-4cde-98c9-79611e2fa");
		asset.setVin("Vin");
		asset.setYear("2021");
		asset.setId(userId);
		asset.setManufacturer(manufacturer1);
		asset.setCreationMethod(assetCreationMethod);
		asset.setManufacturerDetails(manufacturerDetails);
		return asset;
	}

	private User getUser() {
		Long userId = 57L;
		Long roleId = 47L;
		PermissionEntity permissionEntity = new PermissionEntity();
		permissionEntity.setCreatedAt(Instant.now());
		permissionEntity.setCreatedBy("createdBy");
		permissionEntity.setDescription("description");
//		permissionEntity.setMethodType(null);
		permissionEntity.setName("name");
		permissionEntity.setPath("path");
		permissionEntity.setPermissionId(3123);
		permissionEntity.setUpdatedAt(Instant.now());
		permissionEntity.setUpdatedBy("updatedBy");

		List<PermissionEntity> permission = new ArrayList<PermissionEntity>();
		permission.add(permissionEntity);

		Organisation organisation = new Organisation();
		organisation.setUuid("84d6efc0-37ad-4cde-98c9-79611e2fab60");
		organisation.setAccountNumber("5346544");
		organisation.setShortName("short_name");
		organisation.setIsActive(true);
		organisation.setId(userId);
		organisation.setIsAssetListRequired(true);
		List<Role> roleList = new ArrayList<>();
		Role role = new Role();
		role.setDescription("description");
		role.setName("name");
		role.setCreatedAt(Instant.now());
		role.setCreatedBy("createdBy");
		role.setDeleted(false);
		role.setRoleId(roleId);
		role.setUpdatedAt(Instant.now());
		role.setUpdatedBy("updatedBy");
		role.setPermissions(permission);
		roleList.add(role);
		User user = new User();
		user.setUuid("84d6efc0-37ad-4cde-98c9-79611e2fab60");
		user.setFirstName("first_name");
		user.setIsActive(true);
		user.setUserName("user_name");
		user.setNotify("notify");
		user.setCountryCode("countryCode");
		user.setEmail("@gmail");
		user.setIsDeleted(true);
		user.setIsPasswordChange(true);
		user.setLastName("lastName");
		user.setOrganisation(organisation);
		user.setPassword("Password");
		user.setPhone("Phone");
		user.setRole(roleList);
		user.setTimeZone("timeZone");
		user.setId(userId);
		return user;
	}

	@Test
	@Order(1)
	public void test_addAsset() {

		Long userId = 57L;
		Long Id = 44L;

		String manufacturer = "manufacturer";

		CompanyPayload companyPayload = new CompanyPayload();
		companyPayload.setCompanyName("name");
		companyPayload.setAccountNumber("5346544");
		companyPayload.setShortName("short_name");
		companyPayload.setStatus(true);
		companyPayload.setId(userId);
		companyPayload.setIsAssetListRequired(true);
		companyPayload.setUuid("84d6efc0-37ad-4cde-98c9-79611e2fab60");

		AssetsPayload assets = new AssetsPayload();
		assets.setAssignedName("assigned_name");
		assets.setCategory("category");
		assets.setUuid("84d6efc0-37ad-4cde-98c9-79611e2fab60");
		assets.setStatus("status");
		assets.setCompany(companyPayload);
		assets.setManufacturer(manufacturer);
		assets.setComment("comment");
		assets.setEligibleGateway("eligibleGateway");
		assets.setId(Id);
		assets.setIsVinValidated(false);
		assets.setVin("vin");
		assets.setYear("year");

		Asset asset = getAsset();
		User user = getUser();
		CompanyPayload company = assets.getCompany();
		Mockito.when(restUtils.getUserFromAuthService(user.getUserName())).thenReturn(user);
		Organisation com = user.getOrganisation();
		Mockito.when(restUtils.getCompanyFromCompanyService(company.getAccountNumber())).thenReturn(com);
		Mockito.when(beanConverter.assetsPayloadToAssets(assets, company, true, user, null)).thenReturn(asset);

		List<String> list = new ArrayList<String>();
		list.add("hgshgjh");
		list.add("kjhskdj");
		Map<String, List<String>> failedAssets = new HashMap<String, List<String>>();
		failedAssets.put("hjukdhf", list);
		Mockito.when(beanConverter.assetsPayloadToAssets(assets, company, true, user, null)).thenReturn(asset);
		AddAssetResponse addAsset = service.addAsset(assets, user.getUserName());
		assertNotNull(addAsset);

	}

	@Test
	@Order(2)
	public void validationOfAssets() {
		Long userId = 34L;

		AssetCategory category = AssetCategory.CHASSIS;
		User createdBy = new User();
		createdBy.setCountryCode("CountryCode");
		createdBy.setEmail("@gmail");
		createdBy.setFirstName("firstName");
		createdBy.setIsActive(true);
		createdBy.setIsDeleted(true);
		createdBy.setIsPasswordChange(true);
		createdBy.setLastName("lastName");

		Manufacturer manufacturer = new Manufacturer();
		manufacturer.setId(userId);
		manufacturer.setName("name");
		manufacturer.setUuid("84d6efc0-37ad-4cde-98c9-79611e2fa");

		AssetCreationMethod assetCreationMethod = AssetCreationMethod.UPLOAD;

		ManufacturerDetails manufacturerDetails = new ManufacturerDetails();
		manufacturerDetails.setId(userId);
		manufacturerDetails.setConfig("config");
		manufacturerDetails.setManufacturer(manufacturer);
		manufacturerDetails.setModel("model");
		manufacturerDetails.setUuid("84d6efc0-37ad-4cde-98c9-79611e2fa");

		Asset asset = new Asset();
		asset.setAssignedName("assigned_name");
		asset.setComment("comment");
		asset.setUuid("84d6efc0-37ad-4cde-98c9-79611e2fa");
		asset.setCategory(category);
		asset.setCreatedBy(createdBy);
		asset.setCreatedAt(Instant.now());
		asset.setGatewayEligibility("gateway");
		asset.setIsApplicableForPrePair(true);
		asset.setIsVinValidated(true);
		asset.setUpdatedBy(createdBy);
		asset.setUpdatedAt(Instant.now());
		asset.setUuid("84d6efc0-37ad-4cde-98c9-79611e2fa");
		asset.setVin("Vin");
		asset.setYear("2021");
		asset.setId(userId);
		asset.setManufacturer(manufacturer);
		asset.setCreationMethod(assetCreationMethod);
		asset.setManufacturerDetails(manufacturerDetails);

		List<Asset> assetList = new ArrayList<Asset>();
		assetList.add(asset);

		CompanyPayload company = new CompanyPayload();
		company.setCompanyName("name");
		company.setAccountNumber("5346544");
		company.setShortName("short_name");
		company.setStatus(true);

		boolean isBulkUpload = false;
		Boolean isMoblieApi = false;
		LOGGER.info("Validating Asset Details");

		List<Asset> nullCheckValidatedList = new ArrayList<>();
		nullCheckValidatedList.add(asset);
		List<Asset> nullCheckValidation = service.nullCheckValidation(assetList, nullCheckValidatedList, isBulkUpload,
				isMoblieApi);
		assertNotNull(nullCheckValidation);
	}

	@Test
	@Order(3)
	public void nullCheckValidation() {
		LOGGER.info("Checking nullcheckValidation");

		Asset asset = new Asset();
//		asset.setId(56L);

		List<Asset> assetsList = new ArrayList<>();
		assetsList.add(asset);
		List<Asset> nullCheckValidatedList = new ArrayList<>();
		boolean isBulkUpload = true;
		Boolean isMoblieApi = true;
		List<String> duplicateAssetIdCheckList = new ArrayList<>();
		for (Asset a : assetsList) {
			if (a.getAssignedName() == null || a.getAssignedName().isEmpty()
					|| duplicateAssetIdCheckList.contains(a.getAssignedName())) {
				if (isBulkUpload) {
					continue;
				} else {
					throw new DeviceException("Asset ID is null");
				}
			}
			if (a.getCategory() == null || a.getCategory().getValue().isEmpty()) {
				if (isBulkUpload) {
					continue;
				} else {
					throw new DeviceException("Asset type is null");
				}
			}
			if ((a.getGatewayEligibility() == null || a.getGatewayEligibility().isEmpty()) && !isMoblieApi) {
				if (isBulkUpload) {
					continue;
				} else {
					throw new DeviceException("Approved product is null");
				}
			}
			nullCheckValidatedList.add(a);
			duplicateAssetIdCheckList.add(a.getAssignedName());
		}
	}

	public void validateAndSaveAssets() throws DeviceException {
		Long userId = 34L;

		AssetCategory category = AssetCategory.CHASSIS;
		User createdBy = new User();
		createdBy.setCountryCode("CountryCode");
		createdBy.setEmail("@gmail");
		createdBy.setFirstName("firstName");
		createdBy.setIsActive(true);
		createdBy.setIsDeleted(true);
		createdBy.setIsPasswordChange(true);
		createdBy.setLastName("lastName");

		Manufacturer manufacturer = new Manufacturer();
		manufacturer.setId(userId);
		manufacturer.setName("name");
		manufacturer.setUuid("84d6efc0-37ad-4cde-98c9-79611e2fa");

		AssetCreationMethod assetCreationMethod = AssetCreationMethod.UPLOAD;

		ManufacturerDetails manufacturerDetails = new ManufacturerDetails();
		manufacturerDetails.setId(userId);
		manufacturerDetails.setConfig("config");
		manufacturerDetails.setManufacturer(manufacturer);
		manufacturerDetails.setModel("model");
		manufacturerDetails.setUuid("84d6efc0-37ad-4cde-98c9-79611e2fa");

		Asset asset = new Asset();
		asset.setAssignedName("assigned_name");
		asset.setComment("comment");
		asset.setUuid("84d6efc0-37ad-4cde-98c9-79611e2fa");
		asset.setCategory(category);
		asset.setCreatedBy(createdBy);
		asset.setCreatedAt(Instant.now());
		asset.setGatewayEligibility("gateway");
		asset.setIsApplicableForPrePair(true);
		asset.setIsVinValidated(true);
		asset.setUpdatedBy(createdBy);
		asset.setUpdatedAt(Instant.now());
		asset.setUuid("84d6efc0-37ad-4cde-98c9-79611e2fa");
		asset.setVin("Vin");
		asset.setYear("2021");
		asset.setId(userId);
		asset.setManufacturer(manufacturer);
		asset.setCreationMethod(assetCreationMethod);
		asset.setManufacturerDetails(manufacturerDetails);

		List<Asset> assetsList = new ArrayList<>();
		assetsList.add(asset);

		CompanyPayload company = new CompanyPayload();
		company.setId(56L);
		company.setAccountNumber("accountNo");
		company.setCompanyName("companyName");

		List<String> list = new ArrayList<String>();
		list.add("hgshgjh");
		list.add("kjhskdj");
		Map<String, List<String>> validationFailedAssets = new HashMap<>();
		validationFailedAssets.put("fdfjh", list);

		boolean isBulkUpload = false;
		boolean isSingleCreate = true;
		Boolean isMoblieApi = true;

		Lookup lookup = new Lookup();
		lookup.setId(56L);
		lookup.setDisplayLabel("DisplayLabel");
		lookup.setField("Feild");
		lookup.setValue("value");
		List<Lookup> approvedProducts = new ArrayList<>();
		approvedProducts.add(lookup);
		List<Lookup> assetType1Products = new ArrayList<>();
		assetType1Products.add(lookup);

		when(assetPropertiesRepository.findByField(Mockito.any(String.class))).thenReturn(approvedProducts);
		when(assetPropertiesRepository.findByField(Mockito.any(String.class))).thenReturn(assetType1Products);
		List<String> vins = new ArrayList<>();
		vins.addAll(assetsList.stream().filter(asset1 -> asset.getVin() != null && !asset.getVin().isEmpty())
				.map(Asset::getVin).collect(Collectors.toList()));

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

		VinDecodeResultDTO vinDecodeResultDTO = new VinDecodeResultDTO();
		vinDecodeResultDTO.setVehicleDTO(nhtsaResultDTO);
		Map<String, VinDecodeResultDTO> vinDecodeResult = new HashMap<>();
		vinDecodeResult.put("dummy", vinDecodeResultDTO);

//		
		List<Asset> assetsListToSave = new ArrayList<>();
		for (Asset a : assetsList) {

			// Asset ID validations
			LOGGER.info("Validating Asset Id");
			String modifiedAssetId = "assigned_name";
			StringUtils.replaceBlankSpaceWithUnderscore(a.getAssignedName());
			validationFailedAssets.get(ASSET_ERRORS[0]).remove(a.getAssignedName());
			a.setAssignedName(modifiedAssetId);
			validationFailedAssets.get(ASSET_ERRORS[0]).add(a.getAssignedName());
			Asset checkForUniqueAsset = null;
			if (a.getUuid() != null && !a.getUuid().isEmpty()) {
				checkForUniqueAsset = assetRepository.findByUuid(a.getUuid());
			}
			if (checkForUniqueAsset != null) {
				if (!isSingleCreate) {
					a.setId(checkForUniqueAsset.getId());
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
								/*
								 * if(isBulkUpload) { continue; } else { throw new
								 * DeviceException("Failed to validate VIN Number"); }
								 */
							} else if (vinDecodingResult != null
									&& vinDecodingResult.getResult().equals("decodeFailure")) {
								asset.setComment(AssetVinComment.NOT_VALIDATED.getValue());
								validationFailedAssets.get(ASSET_ERRORS[0]).remove(asset.getAssignedName());
								if (validationFailedAssets.get(ASSET_ERRORS[15]) != null) {
									validationFailedAssets.get(ASSET_ERRORS[15]).add(asset.getAssignedName());
								} else {
									List<String> failedAssetAssignedName = new ArrayList<>();
									failedAssetAssignedName.add(asset.getAssignedName());
									validationFailedAssets.put(ASSET_ERRORS[15], failedAssetAssignedName);
								}
							}
						} else if (vinDecodingResult != null && vinDecodingResult.getResult().equals("success")) {
							asset.setIsVinValidated(true);
						}
					}

					// check if this VIN already exists
					List<Asset> assetList = assetRepository.findByVinNumber(asset.getVin());
					if (assetList != null && !assetList.isEmpty()) {
						if (assetList.size() > 1) {
							if (isBulkUpload) {
								if (validationFailedAssets.get(ASSET_ERRORS[2]) != null) {
									validationFailedAssets.get(ASSET_ERRORS[2]).add(asset.getAssignedName());
								} else {
									List<String> failedAssetAssignedName = new ArrayList<>();
									failedAssetAssignedName.add(asset.getAssignedName());
									validationFailedAssets.put(ASSET_ERRORS[2], failedAssetAssignedName);
								}
								validationFailedAssets.get(ASSET_ERRORS[0]).remove(asset.getAssignedName());
								continue;
							} else {
								throw new DuplicateVinException(
										"The VIN provided is already in use for another asset. Please check it and try again.");
							}
						} else {
							if (assetList.get(0) != null && asset.getId() == null) {
								if (isBulkUpload) {
									if (validationFailedAssets.get(ASSET_ERRORS[2]) != null) {
										validationFailedAssets.get(ASSET_ERRORS[2]).add(asset.getAssignedName());
									} else {
										List<String> failedAssetAssignedName = new ArrayList<>();
										failedAssetAssignedName.add(asset.getAssignedName());
										validationFailedAssets.put(ASSET_ERRORS[2], failedAssetAssignedName);
									}
									validationFailedAssets.get(ASSET_ERRORS[0]).remove(asset.getAssignedName());
									continue;
								} else {
									throw new DuplicateVinException(
											"The VIN provided is already in use for another asset. Please check it and try again.");
								}
							} else {
								assetRepository.findByVinAndNotInId(asset.getVin(), asset.getId());
								if (asset != null) {
									throw new DuplicateVinException(
											"The VIN provided is already in use for another asset. Please check it and try again.");
								}
							}
						}
					}
				}
			}
		}
		service.validateAndSaveAssets(assetsListToSave, company, validationFailedAssets, isBulkUpload, isSingleCreate,
				"username", isMoblieApi,false);
	}

	public void setCompanyToAssets() {

		Long userId = 34L;
		Long roleId = 43L;
		String userName = "userName";

		Organisation organisation = new Organisation();
		organisation.setUuid("84d6efc0-37ad-4cde-98c9-79611e2fab60");
		organisation.setAccountNumber("52454885");
		organisation.setShortName("short_name");
		organisation.setIsActive(true);
		organisation.setId(userId);
		organisation.setIsAssetListRequired(true);

		PermissionEntity permissionEntity = new PermissionEntity();
		permissionEntity.setCreatedAt(Instant.now());
		permissionEntity.setCreatedBy("createdBy");
		permissionEntity.setDescription("description");
//		permissionEntity.setMethodType(null);
		permissionEntity.setName("name");
		permissionEntity.setPath("path");
		permissionEntity.setPermissionId(3123);
		permissionEntity.setUpdatedAt(Instant.now());
		permissionEntity.setUpdatedBy("updatedBy");

		List<PermissionEntity> permission = new ArrayList<>();
		permission.add(permissionEntity);

		String superAdmin = AuthoritiesConstants.SUPER_ADMIN;
		List<Role> roleList = new ArrayList<>();
		Role role = new Role();
		role.setDescription("description");
		role.setName(superAdmin);
		role.setCreatedAt(Instant.now());
		role.setCreatedBy("createdBy");
		role.setDeleted(false);
		role.setRoleId(roleId);
		role.setUpdatedAt(Instant.now());
		role.setUpdatedBy("updatedBy");
		role.setPermissions(permission);
		roleList.add(role);
		User user = new User();
		user.setUuid("84d6efc0-37ad-4cde-98c9-79611e2fab60");
		user.setFirstName("first_name");
		user.setIsActive(true);
		user.setUserName("user_name");
		user.setNotify("notify");
		user.setCountryCode("countryCode");
		user.setEmail("@gmail");
		user.setIsDeleted(true);
		user.setIsPasswordChange(true);
		user.setLastName("lastName");
		user.setOrganisation(organisation);
		user.setPassword("Password");
		user.setPhone("Phone");
		user.setRole(roleList);
		user.setTimeZone("timeZone");
		user.setId(userId);

		Mockito.when(restUtils.getUserFromAuthService(userName)).thenReturn(user);

//		String role1 = user.getRole().getName();

		AssetCategory category = AssetCategory.CHASSIS;

		Manufacturer manufacturer = new Manufacturer();
		manufacturer.setId(userId);
		manufacturer.setName("name");
		manufacturer.setUuid("84d6efc0-37ad-4cde-98c9-79611e2fa");

		AssetCreationMethod assetCreationMethod = AssetCreationMethod.UPLOAD;

		ManufacturerDetails manufacturerDetails = new ManufacturerDetails();
		manufacturerDetails.setId(userId);
		manufacturerDetails.setConfig("config");
		manufacturerDetails.setManufacturer(manufacturer);
		manufacturerDetails.setModel("model");
		manufacturerDetails.setUuid("84d6efc0-37ad-4cde-98c9-79611e2fa");

		Boolean isMoblieApi = true;

		CompanyPayload company = new CompanyPayload();
		company.setCompanyName("name");
		company.setAccountNumber("5346544");
		company.setShortName("short_name");
		company.setStatus(true);
		company.setId(userId);
		company.setIsAssetListRequired(true);
		company.setUuid("84d6efc0-37ad-4cde-98c9-79611e2fa");

		Asset asset = new Asset();
		asset.setAssignedName("assigned_name");
		asset.setComment("comment");
		asset.setUuid("84d6efc0-37ad-4cde-98c9-79611e2fa");
		asset.setCategory(category);
		asset.setCreatedBy(user);
		asset.setCreatedAt(Instant.now());
		asset.setGatewayEligibility("gateway");
		asset.setIsApplicableForPrePair(true);
		asset.setIsVinValidated(true);
		asset.setUpdatedBy(user);
		asset.setUpdatedAt(Instant.now());
		asset.setUuid("84d6efc0-37ad-4cde-98c9-79611e2fa");
		asset.setVin("Vin");
		asset.setYear("2021");
		asset.setId(userId);
		asset.setManufacturer(manufacturer);
		asset.setCreationMethod(assetCreationMethod);
		asset.setManufacturerDetails(manufacturerDetails);
		asset.setOrganisation(beanConverter.companyPayloadToCompanies(company));

		Asset setCompanyToAssets = service.setCompanyToAssets(asset, company, userName, isMoblieApi,Boolean.FALSE);
		assertNotNull(setCompanyToAssets);
	}

	public void setCompanyToAssets1() {

		Long userId = 34L;
		Long roleId = 43L;
		String userName = "userName";

		Organisation organisation = new Organisation();
		organisation.setUuid("84d6efc0-37ad-4cde-98c9-79611e2fab60");
		organisation.setAccountNumber("52454885");
		organisation.setShortName("short_name");
		organisation.setIsActive(true);
		organisation.setId(userId);
		organisation.setIsAssetListRequired(true);

		PermissionEntity permissionEntity = new PermissionEntity();
		permissionEntity.setCreatedAt(Instant.now());
		permissionEntity.setCreatedBy("createdBy");
		permissionEntity.setDescription("description");
//		permissionEntity.setMethodType(null);
		permissionEntity.setName("name");
		permissionEntity.setPath("path");
		permissionEntity.setPermissionId(3123);
		permissionEntity.setUpdatedAt(Instant.now());
		permissionEntity.setUpdatedBy("updatedBy");

		List<PermissionEntity> permission = new ArrayList<PermissionEntity>();
		permission.add(permissionEntity);

		String customerAdmin = AuthoritiesConstants.ROLE_CUSTOMER_ADMIN;
		List<Role> roleList = new ArrayList<>();
		Role role = new Role();
		role.setDescription("description");
		role.setName(customerAdmin);
		role.setCreatedAt(Instant.now());
		role.setCreatedBy("createdBy");
		role.setDeleted(false);
		role.setRoleId(roleId);
		role.setUpdatedAt(Instant.now());
		role.setUpdatedBy("updatedBy");
		role.setPermissions(permission);
		roleList.add(role);

		User user = new User();
		user.setUuid("84d6efc0-37ad-4cde-98c9-79611e2fab60");
		user.setFirstName("first_name");
		user.setIsActive(true);
		user.setUserName("user_name");
		user.setNotify("notify");
		user.setCountryCode("countryCode");
		user.setEmail("@gmail");
		user.setIsDeleted(true);
		user.setIsPasswordChange(true);
		user.setLastName("lastName");
		user.setOrganisation(organisation);
		user.setPassword("Password");
		user.setPhone("Phone");
		user.setRole(roleList);
		user.setTimeZone("timeZone");
		user.setId(userId);

		Mockito.when(restUtils.getUserFromAuthService(userName)).thenReturn(user);

//		String role1 = user.getRole().getName();

		AssetCategory category = AssetCategory.CHASSIS;

		Manufacturer manufacturer = new Manufacturer();
		manufacturer.setId(userId);
		manufacturer.setName("name");
		manufacturer.setUuid("84d6efc0-37ad-4cde-98c9-79611e2fa");

		AssetCreationMethod assetCreationMethod = AssetCreationMethod.UPLOAD;

		ManufacturerDetails manufacturerDetails = new ManufacturerDetails();
		manufacturerDetails.setId(userId);
		manufacturerDetails.setConfig("config");
		manufacturerDetails.setManufacturer(manufacturer);
		manufacturerDetails.setModel("model");
		manufacturerDetails.setUuid("84d6efc0-37ad-4cde-98c9-79611e2fa");

		Boolean isMoblieApi = null;

		CompanyPayload company = new CompanyPayload();
		company.setCompanyName("name");
		company.setAccountNumber("5346544");
		company.setShortName("short_name");
		company.setStatus(true);
		company.setId(userId);
		company.setIsAssetListRequired(true);
		company.setUuid("84d6efc0-37ad-4cde-98c9-79611e2fa");

		Asset asset = new Asset();
		asset.setAssignedName("assigned_name");
		asset.setComment("comment");
		asset.setUuid("84d6efc0-37ad-4cde-98c9-79611e2fa");
		asset.setCategory(category);
		asset.setCreatedBy(user);
		asset.setCreatedAt(Instant.now());
		asset.setGatewayEligibility("gateway");
		asset.setIsApplicableForPrePair(true);
		asset.setIsVinValidated(true);
		asset.setUpdatedBy(user);
		asset.setUpdatedAt(Instant.now());
		asset.setUuid("84d6efc0-37ad-4cde-98c9-79611e2fa");
		asset.setVin("Vin");
		asset.setYear("2021");
		asset.setId(userId);
		asset.setManufacturer(manufacturer);
		asset.setCreationMethod(assetCreationMethod);
		asset.setManufacturerDetails(manufacturerDetails);
		asset.setOrganisation(beanConverter.companyPayloadToCompanies(company));

		Asset setCompanyToAssets = service.setCompanyToAssets(asset, company, userName, isMoblieApi,Boolean.FALSE);
		assertNotNull(setCompanyToAssets);
	}

	public void getAssetByIdOrVinNumber() {
		String vin = "vin";
		String assetId = "assetId";
		String accountNumber = "accountNumber";

		Long userId = 34L;
		Long roleId = 43L;
		Organisation organisation = new Organisation();
		organisation.setUuid("84d6efc0-37ad-4cde-98c9-79611e2fab60");
		organisation.setAccountNumber("52454885");
		organisation.setShortName("short_name");
		organisation.setIsActive(true);
		organisation.setId(userId);
		organisation.setIsAssetListRequired(true);

		PermissionEntity permissionEntity = new PermissionEntity();
		permissionEntity.setCreatedAt(Instant.now());
		permissionEntity.setCreatedBy("createdBy");
		permissionEntity.setDescription("description");
//		permissionEntity.setMethodType(null);
		permissionEntity.setName("name");
		permissionEntity.setPath("path");
		permissionEntity.setPermissionId(3123);
		permissionEntity.setUpdatedAt(Instant.now());
		permissionEntity.setUpdatedBy("updatedBy");

		List<PermissionEntity> permission = new ArrayList<PermissionEntity>();
		permission.add(permissionEntity);

		String superAdmin = AuthoritiesConstants.SUPER_ADMIN;

		List<Role> roleList = new ArrayList<>();
		Role role = new Role();
		role.setDescription("description");
		role.setName(superAdmin);
		role.setCreatedAt(Instant.now());
		role.setCreatedBy("createdBy");
		role.setDeleted(false);
		role.setRoleId(roleId);
		role.setUpdatedAt(Instant.now());
		role.setUpdatedBy("updatedBy");
		role.setPermissions(permission);
		roleList.add(role);

		User user = new User();
		user.setUuid("84d6efc0-37ad-4cde-98c9-79611e2fab60");
		user.setFirstName("first_name");
		user.setIsActive(true);
		user.setUserName("user_name");
		user.setNotify("notify");
		user.setCountryCode("countryCode");
		user.setEmail("@gmail");
		user.setIsDeleted(true);
		user.setIsPasswordChange(true);
		user.setLastName("lastName");
		user.setOrganisation(organisation);
		user.setPassword("Password");
		user.setPhone("Phone");
		user.setRole(roleList);
		user.setTimeZone("timeZone");
		user.setId(userId);

		Manufacturer manufacturer = new Manufacturer();
		manufacturer.setId(userId);
		manufacturer.setName("name");
		manufacturer.setUuid("84d6efc0-37ad-4cde-98c9-79611e2fa");

		AssetCreationMethod assetCreationMethod = AssetCreationMethod.UPLOAD;

		ManufacturerDetails manufacturerDetails = new ManufacturerDetails();
		manufacturerDetails.setId(userId);
		manufacturerDetails.setConfig("config");
		manufacturerDetails.setManufacturer(manufacturer);
		manufacturerDetails.setModel("model");
		manufacturerDetails.setUuid("84d6efc0-37ad-4cde-98c9-79611e2fa");

		AssetCategory category = AssetCategory.CHASSIS;

		Asset asset = new Asset();
		asset.setAssignedName("assigned_name");
		asset.setComment("comment");
		asset.setUuid("84d6efc0-37ad-4cde-98c9-79611e2fa");
		asset.setCategory(category);
		asset.setCreatedBy(user);
		asset.setCreatedAt(Instant.now());
		asset.setGatewayEligibility("gateway");
		asset.setIsApplicableForPrePair(true);
		asset.setIsVinValidated(true);
		asset.setUpdatedBy(user);
		asset.setUpdatedAt(Instant.now());
		asset.setUuid("84d6efc0-37ad-4cde-98c9-79611e2fa");
		asset.setVin("Vin");
		asset.setYear("2021");
		asset.setId(userId);
		asset.setManufacturer(manufacturer);
		asset.setCreationMethod(assetCreationMethod);
		asset.setManufacturerDetails(manufacturerDetails);
		asset.setOrganisation(organisation);

		List<Asset> assetList = new ArrayList<>();
		assetList.add(asset);

		AssetDTO assetDTO = new AssetDTO();
		assetDTO.setAccountNumber(accountNumber);
		assetDTO.setAssignedName("assignedName");
		assetDTO.setCategory("chasis");
		assetDTO.setComment("comment");
		assetDTO.setConfig("config");
		assetDTO.setCreatedBy("createdBy");
		assetDTO.setDateCreated(Instant.now());
		assetDTO.setDateUpdated(Instant.now());
		assetDTO.setDeviceEligibility("deviceEligibility");
		assetDTO.setId(roleId);
		assetDTO.setIsVinValidated(true);
		assetDTO.setManufacturer("manufacturer");
		assetDTO.setStatus("Active");
		assetDTO.setUpdatedBy(user);
		assetDTO.setVin(vin);
		assetDTO.setYear("2010");

//		for(AssetDTO convertAssetToAssetsDto1 : assetList) {

		AssetDTO convertAssetToAssetsDto = beanConverter.convertAssetToAssetsDto(asset);

		List<AssetDTO> assetDtoList = new ArrayList<>();
		assetDtoList.add(assetDTO);
		assetDtoList.add(convertAssetToAssetsDto);

		List<AssetDTO> assetByIdOrVinNumber = service.getAssetByIdOrVinNumber(vin, assetId, accountNumber);
		assertNotNull(assetByIdOrVinNumber);

	}

	public void getAssetVinSearch() throws Exception {

		String vin = "19XFA1F95BE007653";

		final String uri = "https://vpic.nhtsa.dot.gov/api/vehicles/decodevinvalues/" + vin + "*BA?format=json";

		AssetVinSearchDTO assetVinSearchDto = new AssetVinSearchDTO();
		RestTemplate restTemplate = new RestTemplate();
		NHTSAResponseDTO result = restTemplate.getForObject(uri, NHTSAResponseDTO.class);
		NHTSAResultDTO decodedVin = result.getResults().get(0);
		decodedVin.setMake("make");
		decodedVin.setModel("Audi_S8");
		decodedVin.setManufacturer("EicherMotors");
		decodedVin.setModelYear("2008");
		decodedVin.setVIN(vin);
		decodedVin.setBodyClass("MetallicBlack");
		decodedVin.setTrailerLength("48");
		decodedVin.setAxles("3");
		decodedVin.setVehicleType("truck");

		String errorCodeString = decodedVin.getErrorCode();
		String[] errorCodes = errorCodeString.split(",");
		if (errorCodes.length > 1) {
			for (String errorCode : errorCodes) {
				if (Integer.parseInt(errorCode) != 0) {
					decodedVin.getErrorText();
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
			}

			AssetVinSearchDTO assetVinSearch = service.getAssetVinSearch(vin);
			assertNotNull(assetVinSearch);
		}
	}

	public void updateAsset() {

		Long userId = 56L;
		Long roleId = 45L;
		String userName = "user";

		CompanyPayload company = new CompanyPayload();
		company.setAccountNumber("accountNo");
		company.setCompanyName("companyName");
		company.setId(userId);
		company.setIsAssetListRequired(true);
		company.setShortName("ShortName");
		company.setStatus(true);
		company.setUuid("e81c87a8-7771-458b-a764-b45ca707dd79");

		AssetsPayload assetsPayloads = new AssetsPayload();
		assetsPayloads.setId(userId);
		assetsPayloads.setAssignedName("assigedName");
		assetsPayloads.setCategory("category");
		assetsPayloads.setComment("comment");
		assetsPayloads.setEligibleGateway("gateway");
		assetsPayloads.setIsVinValidated(true);
		assetsPayloads.setManufacturer("manufacturer");
		assetsPayloads.setStatus("status");
		assetsPayloads.setUuid("e81c87a8-7771-458b-a764-b45ca707dd79");
		assetsPayloads.setVin("vin");
		assetsPayloads.setYear("year");
		assetsPayloads.setCompany(company);

		Organisation organisation = new Organisation();
		organisation.setUuid("84d6efc0-37ad-4cde-98c9-79611e2fab60");
		organisation.setAccountNumber("52454885");
		organisation.setShortName("short_name");
		organisation.setIsActive(true);
		organisation.setId(userId);
		organisation.setIsAssetListRequired(true);

		PermissionEntity permissionEntity = new PermissionEntity();
		permissionEntity.setCreatedAt(Instant.now());
		permissionEntity.setCreatedBy("createdBy");
		permissionEntity.setDescription("description");
//		permissionEntity.setMethodType(null);
		permissionEntity.setName("name");
		permissionEntity.setPath("path");
		permissionEntity.setPermissionId(3123);
		permissionEntity.setUpdatedAt(Instant.now());
		permissionEntity.setUpdatedBy("updatedBy");

		List<PermissionEntity> permission = new ArrayList<PermissionEntity>();
		permission.add(permissionEntity);

		String superAdmin = AuthoritiesConstants.SUPER_ADMIN;

		List<Role> roleList = new ArrayList<>();
		Role role = new Role();
		role.setDescription("description");
		role.setName(superAdmin);
		role.setCreatedAt(Instant.now());
		role.setCreatedBy("createdBy");
		role.setDeleted(false);
		role.setRoleId(roleId);
		role.setUpdatedAt(Instant.now());
		role.setUpdatedBy("updatedBy");
		role.setPermissions(permission);
		roleList.add(role);

		User user = new User();
		user.setUuid("84d6efc0-37ad-4cde-98c9-79611e2fab60");
		user.setFirstName("first_name");
		user.setIsActive(true);
		user.setUserName("user_name");
		user.setNotify("notify");
		user.setCountryCode("countryCode");
		user.setEmail("@gmail");
		user.setIsDeleted(true);
		user.setIsPasswordChange(true);
		user.setLastName("lastName");
		user.setOrganisation(organisation);
		user.setPassword("Password");
		user.setPhone("Phone");
		user.setRole(roleList);
		user.setTimeZone("timeZone");
		user.setId(userId);
		when(restUtils.getUserFromAuthService(userId)).thenReturn(user);

		Manufacturer manufacturer = new Manufacturer();
		manufacturer.setId(userId);
		manufacturer.setName("name");
		manufacturer.setUuid("84d6efc0-37ad-4cde-98c9-79611e2fa");

		AssetCreationMethod assetCreationMethod = AssetCreationMethod.UPLOAD;
		AssetCategory category = AssetCategory.CHASSIS;

		ManufacturerDetails manufacturerDetails = new ManufacturerDetails();
		manufacturerDetails.setId(userId);
		manufacturerDetails.setConfig("config");
		manufacturerDetails.setManufacturer(manufacturer);
		manufacturerDetails.setModel("model");
		manufacturerDetails.setUuid("84d6efc0-37ad-4cde-98c9-79611e2fa");

		AssetStatus status = AssetStatus.ACTIVE;

		Asset asset = new Asset();
		asset.setAssignedName("assigned_name");
		asset.setComment("comment");
		asset.setUuid("84d6efc0-37ad-4cde-98c9-79611e2fa");
		asset.setCategory(category);
		asset.setCreatedBy(user);
		asset.setCreatedAt(Instant.now());
		asset.setGatewayEligibility("gateway");
		asset.setIsApplicableForPrePair(true);
		asset.setIsVinValidated(true);
		asset.setUpdatedBy(user);
		asset.setUpdatedAt(Instant.now());
		asset.setUuid("84d6efc0-37ad-4cde-98c9-79611e2fa");
		asset.setVin("Vin");
		asset.setYear("2021");
		asset.setId(userId);
		asset.setManufacturer(manufacturer);
		asset.setCreationMethod(assetCreationMethod);
		asset.setManufacturerDetails(manufacturerDetails);
		asset.setOrganisation(organisation);
		asset.setStatus(status);

		when(beanConverter.assetsPayloadToAssets(assetsPayloads, assetsPayloads.getCompany(), true, user, null))
				.thenReturn(asset);
		String modifiedAssetId = StringUtils.replaceBlankSpaceWithUnderscore(asset.getAssignedName());

		when(assetRepository.findAssetByAssetIdAndOrganisation(modifiedAssetId, assetsPayloads.getCompany().getId()))
				.thenReturn(asset);

		List<Asset> assetList = new ArrayList<Asset>();
		assetList.add(asset);

		List<String> string = new ArrayList<>();
		string.add("dasdad");
		string.add("dfdsf");
		string.add("dfsfgs");

		Map<String, List<String>> failedAssets = new HashMap<>();
		failedAssets.put("adssdf", string);

		Mockito.when(service.validationOfAssets(assetList, assetsPayloads.getCompany(), true, false, userName,
				Boolean.FALSE,Boolean.FALSE)).thenReturn(failedAssets);

		AddAssetResponse addAssetResponse = new AddAssetResponse();
		addAssetResponse.setErrors(failedAssets);
		addAssetResponse.setAssetPayload(beanConverter.assetsassetsToPayload(asset));

		AddAssetResponse updateAsset = service.updateAsset(assetsPayloads, userName);
		assertNotNull(updateAsset);
	}

	public void getAllActiveCustomerOrganisationList() {

		Pageable pageable = PageRequest.of(1, 10);
		Long companyId = 45L;
		Long userId = 46L;
		Long roleId = 56L;
		Map<String, String> filterValues = new HashMap<String, String>();
		filterValues.put("dsfda", "dsadas");
		String yearFilter = "yearFilter";
		String userName = "userName";

		Organisation organisation = new Organisation();
		organisation.setUuid("84d6efc0-37ad-4cde-98c9-79611e2fab60");
		organisation.setAccountNumber("52454885");
		organisation.setShortName("short_name");
		organisation.setIsActive(true);
		organisation.setId(userId);
		organisation.setIsAssetListRequired(true);

		PermissionEntity permissionEntity = new PermissionEntity();
		permissionEntity.setCreatedAt(Instant.now());
		permissionEntity.setCreatedBy("createdBy");
		permissionEntity.setDescription("description");
//		permissionEntity.setMethodType(null);
		permissionEntity.setName("name");
		permissionEntity.setPath("path");
		permissionEntity.setPermissionId(3123);
		permissionEntity.setUpdatedAt(Instant.now());
		permissionEntity.setUpdatedBy("updatedBy");

		List<PermissionEntity> permission = new ArrayList<PermissionEntity>();
		permission.add(permissionEntity);

		String superAdmin = AuthoritiesConstants.SUPER_ADMIN;

		List<Role> roleList = new ArrayList<>();
		Role role = new Role();
		role.setDescription("description");
		role.setName(superAdmin);
		role.setCreatedAt(Instant.now());
		role.setCreatedBy("createdBy");
		role.setDeleted(false);
		role.setRoleId(roleId);
		role.setUpdatedAt(Instant.now());
		role.setUpdatedBy("updatedBy");
		role.setPermissions(permission);
		roleList.add(role);

		User user = new User();
		user.setUuid("84d6efc0-37ad-4cde-98c9-79611e2fab60");
		user.setFirstName("first_name");
		user.setIsActive(true);
		user.setUserName("user_name");
		user.setNotify("notify");
		user.setCountryCode("countryCode");
		user.setEmail("@gmail");
		user.setIsDeleted(true);
		user.setIsPasswordChange(true);
		user.setLastName("lastName");
		user.setOrganisation(organisation);
		user.setPassword("Password");
		user.setPhone("Phone");
		user.setRole(roleList);
		user.setTimeZone("timeZone");
		user.setId(userId);

		when(restUtils.getUserFromAuthService(userName)).thenReturn(user);

		Manufacturer manufacturer = new Manufacturer();
		manufacturer.setId(userId);
		manufacturer.setName("name");
		manufacturer.setUuid("84d6efc0-37ad-4cde-98c9-79611e2fa");

		AssetCreationMethod assetCreationMethod = AssetCreationMethod.UPLOAD;

		ManufacturerDetails manufacturerDetails = new ManufacturerDetails();
		manufacturerDetails.setId(userId);
		manufacturerDetails.setConfig("config");
		manufacturerDetails.setManufacturer(manufacturer);
		manufacturerDetails.setModel("model");
		manufacturerDetails.setUuid("84d6efc0-37ad-4cde-98c9-79611e2fa");

		AssetStatus status = AssetStatus.ACTIVE;
		AssetCategory category = AssetCategory.CHASSIS;

		Asset asset = new Asset();
		asset.setAssignedName("assigned_name");
		asset.setComment("comment");
		asset.setUuid("84d6efc0-37ad-4cde-98c9-79611e2fa");
		asset.setCategory(category);
		asset.setCreatedBy(user);
		asset.setCreatedAt(Instant.now());
		asset.setGatewayEligibility("gateway");
		asset.setIsApplicableForPrePair(true);
		asset.setIsVinValidated(true);
		asset.setUpdatedBy(user);
		asset.setUpdatedAt(Instant.now());
		asset.setUuid("84d6efc0-37ad-4cde-98c9-79611e2fa");
		asset.setVin("Vin");
		asset.setYear("2021");
		asset.setId(userId);
		asset.setManufacturer(manufacturer);
		asset.setCreationMethod(assetCreationMethod);
		asset.setManufacturerDetails(manufacturerDetails);
		asset.setOrganisation(organisation);
		asset.setStatus(status);

		List<Asset> list = new ArrayList<>();
		list.add(asset);

		Page<Asset> allAssetByCompany = new PageImpl<>(list);

		spc = AssetSpecification.getaAssetSpecification(filterValues, user, companyId, yearFilter, null);
		when(assetRepository.findAll(Mockito.any(Specification.class), Mockito.any(Pageable.class)))
				.thenReturn(allAssetByCompany);
		List<AssetResponseDTO> assetResponseDTOList = new ArrayList<>();
		for (Asset asset1 : allAssetByCompany) {
			try {
				assetResponseDTOList.add(beanConverter.convertAssetToAssetResponseDTO(asset));
			} catch (JsonProcessingException e) {
				LOGGER.error("Exception while converting asset to AssetResponseDTO", e);
			}
		}
		Page<AssetResponseDTO> page = new PageImpl<>(assetResponseDTOList, pageable,
				allAssetByCompany.getTotalElements());
		Page<AssetResponseDTO> allActiveCustomerOrganisationList = service
				.getAllActiveCustomerOrganisationList(pageable, companyId, userName, filterValues, yearFilter, null);
		assertNotNull(allActiveCustomerOrganisationList);
		assertNotNull(page);
	}

	public void getCustomerAssetsList() {

		Long userId = 44L;
		Long organisationId = 44L;
		Long roleId = 46L;
		String userName = "userName";

		Map<String, String> filterValues = new HashMap<>();
		filterValues.put("sdsf", "sfsfd");
		filterValues.put("dfsf", "sdcsd");
		filterValues.put("asda", "dadas");
		filterValues.put("asda", "adass");

		String filterModelCountFilter = "filterModelCountFilter";

		Organisation organisation = new Organisation();
		organisation.setUuid("84d6efc0-37ad-4cde-98c9-79611e2fab60");
		organisation.setAccountNumber("52454885");
		organisation.setShortName("short_name");
		organisation.setIsActive(true);
		organisation.setId(userId);
		organisation.setIsAssetListRequired(true);

		PermissionEntity permissionEntity = new PermissionEntity();
		permissionEntity.setCreatedAt(Instant.now());
		permissionEntity.setCreatedBy("createdBy");
		permissionEntity.setDescription("description");
//		permissionEntity.setMethodType(null);
		permissionEntity.setName("name");
		permissionEntity.setPath("path");
		permissionEntity.setPermissionId(3123);
		permissionEntity.setUpdatedAt(Instant.now());
		permissionEntity.setUpdatedBy("updatedBy");

		List<PermissionEntity> permission = new ArrayList<PermissionEntity>();
		permission.add(permissionEntity);

		String superAdmin = AuthoritiesConstants.SUPER_ADMIN;

		List<Role> roleList = new ArrayList<>();
		Role role = new Role();
		role.setDescription("description");
		role.setName(superAdmin);
		role.setCreatedAt(Instant.now());
		role.setCreatedBy("createdBy");
		role.setDeleted(false);
		role.setRoleId(roleId);
		role.setUpdatedAt(Instant.now());
		role.setUpdatedBy("updatedBy");
		role.setPermissions(permission);
		roleList.add(role);

		User user = new User();
		user.setUuid("84d6efc0-37ad-4cde-98c9-79611e2fab60");
		user.setFirstName("first_name");
		user.setIsActive(true);
		user.setUserName("user_name");
		user.setNotify("notify");
		user.setCountryCode("countryCode");
		user.setEmail("@gmail");
		user.setIsDeleted(true);
		user.setIsPasswordChange(true);
		user.setLastName("lastName");
		user.setOrganisation(organisation);
		user.setPassword("Password");
		user.setPhone("Phone");
		user.setRole(roleList);
		user.setTimeZone("timeZone");
		user.setId(userId);

		AssetRecord assetRecord = new AssetRecord();
		assetRecord.setUpdatedFirstName("updatedFirstName");
		assetRecord.setUpdatedLastName("updatedLastName");
		assetRecord.setCreatedAt(Instant.now());
		assetRecord.setOrganisationId(organisationId);
		assetRecord.setOrganisationName("organisationName");
		assetRecord.setUpdatedAt(Instant.now());
		assetRecord.setCreatedFirstName("createdFirstName");
		assetRecord.setCreatedLastName("createdLastName");

		List<AssetRecord> list = new ArrayList<>();
		list.add(assetRecord);

		Page<AssetRecord> customerAssets = new PageImpl<>(list);

		AssetRecordPayload assetRecordPayload = new AssetRecordPayload();
		assetRecordPayload.setUpdatedFirstName("updatedFirstName");
		assetRecordPayload.setUpdatedLastName("updatedLastName");
		assetRecordPayload.setCreatedAt(Instant.now());
		assetRecordPayload.setOrganisationId(organisationId);
		assetRecordPayload.setOrganisationName("organisationName");
		assetRecordPayload.setUpdatedAt(Instant.now());
		assetRecordPayload.setCreatedFirstName("createdFirstName");
		assetRecordPayload.setCreatedLastName("createdLastName");

		List<AssetRecordPayload> list1 = new ArrayList<>();
		list1.add(assetRecordPayload);
		new PageImpl<>(list1);

		Pageable pageable = PageRequest.of(0, 10);

		Mockito.when(restUtils.getUserFromAuthService(userName)).thenReturn(user);
		spc1 = CustomAssetSpecification.getCustomerAsetSpecification(filterValues, user, filterModelCountFilter, null);
		when(assetRepository.findAll(Mockito.any(Specification.class), Mockito.any(Pageable.class)))
				.thenReturn(customerAssets);
		service.getCustomerAssetsList(pageable, userName, filterValues,
				filterModelCountFilter, null);
	}

	public void getSuperAdminUser() {

		List<String> list = new ArrayList<String>();
		list.add("dwqqeedq");
		list.add("dqwsd");
		Mockito.when(restUtils.getSuperAdminUser()).thenReturn(list);
		List<String> superAdminUser = service.getSuperAdminUser();
		assertNotNull(superAdminUser);
	}

	public void getSuperAdminUser1() {

		List<String> list = new ArrayList<String>();
		Mockito.when(restUtils.getSuperAdminUser()).thenReturn(list);
		service.getSuperAdminUser();
	}
}