package com.pct.device.util;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;
import java.util.stream.Collectors;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.TopHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.pct.common.constant.AssetCategory;
import com.pct.common.constant.AssetCreationMethod;
import com.pct.common.constant.AssetStatus;
import com.pct.common.constant.DeviceStatus;
import com.pct.common.constant.GatewayStatus;
import com.pct.common.constant.GatewayType;
import com.pct.common.constant.IOTType;
import com.pct.common.constant.InstallHistoryStatus;
import com.pct.common.dto.CustomerForwardingRuleUrlDTO;
import com.pct.common.dto.Device_Sensor_Xref_Dto;
import com.pct.common.model.Asset;
import com.pct.common.model.Asset_Device_xref;
import com.pct.common.model.Cellular;
import com.pct.common.model.Device;
import com.pct.common.model.DeviceForwarding;
import com.pct.common.model.DeviceIgnoreForwardingRule;
import com.pct.common.model.DeviceQa;
//import com.pct.common.model.Device_Device_xref;
import com.pct.common.model.Device_Device_xref;
import com.pct.common.model.InstallHistory;
import com.pct.common.model.MaintenanceReportHistory;
import com.pct.common.model.Manufacturer;
import com.pct.common.model.ManufacturerDetails;
import com.pct.common.model.Organisation;
import com.pct.common.model.ProductMaster;
import com.pct.common.model.Role;
import com.pct.common.model.SensorDetail;
import com.pct.common.model.User;
import com.pct.device.Bean.DeviceBean;
import com.pct.device.Bean.DeviceCommandBean;
import com.pct.device.Bean.SensorBean;
//import com.pct.device.bean.SensorBean;
import com.pct.device.dto.AssetDTO;
import com.pct.device.dto.AssetListResponseDTO;
import com.pct.device.dto.AssetResponseDTO;
import com.pct.device.dto.CustomerDTO;
import com.pct.device.dto.ManufacturerDetailsDTO;
import com.pct.device.model.AssetRecord;
import com.pct.device.model.DeviceShippingDetails;
import com.pct.device.model.DeviceView;
import com.pct.device.model.Event;
import com.pct.device.model.GatewaySummary;
import com.pct.device.model.Lookup;
import com.pct.device.payload.AssetDeviceAssociationPayLoadForIA;
import com.pct.device.payload.AssetRecordPayload;
import com.pct.device.payload.AssetsDetailPayload;
import com.pct.device.payload.AssetsPayload;
import com.pct.device.payload.AssetsPayloadMobile;
import com.pct.device.payload.AssetsStatusPayload;
import com.pct.device.payload.BeaconPayload;
import com.pct.device.payload.CellularDetailPayload;
import com.pct.device.payload.CellularPayload;
import com.pct.device.payload.CompanyPayload;
import com.pct.device.payload.CreateCompanyPayload;
import com.pct.device.payload.DeviceDetailsRequest;
import com.pct.device.payload.DeviceForwardingResponse;
import com.pct.device.payload.DeviceQaRequest;
import com.pct.device.payload.DeviceQaResponse;
import com.pct.device.payload.DeviceResponsePayload;
import com.pct.device.payload.DeviceWithSensorPayload;
import com.pct.device.payload.GatewayBeanForMobileApp;
import com.pct.device.payload.GatewayBulkUploadRequest;
import com.pct.device.payload.GatewayBulkUploadRequestWithDeviceForwarding;
import com.pct.device.payload.GatewayPayload;
import com.pct.device.payload.GatewaySensorPayload;
import com.pct.device.payload.GatewaySummaryPayload;
import com.pct.device.payload.InstallationResponse;
import com.pct.device.payload.InstalledHistoryResponsePayload;
import com.pct.device.payload.InventoryResponse;
import com.pct.device.payload.JobSummaryResponse;
import com.pct.device.payload.LookupPayload;
import com.pct.device.payload.MaintenanceReportHistoryPayload;
import com.pct.device.payload.ProductMasterRequest;
import com.pct.device.payload.ProductMasterResponse;
import com.pct.device.payload.SensorPayLoad;
import com.pct.device.payload.SensorSubDetailPayload;
import com.pct.device.repository.DeviceForwardingRepository;
import com.pct.device.repository.EventRepository;
import com.pct.device.repository.IAssetDeviceXrefRepository;
import com.pct.device.repository.IAssetRepository;
import com.pct.device.repository.ICellularRepository;
import com.pct.device.repository.IDeviceDeviceXrefRepository;
import com.pct.device.repository.IDeviceRepository;
import com.pct.device.repository.ILookupRepository;
import com.pct.device.repository.IManufacturerDetailsRepository;
import com.pct.device.repository.IManufacturerRepository;
import com.pct.device.repository.RedisDeviceRepository;
import com.pct.device.repository.projections.ApprovedAssetTypeCountView;
import com.pct.device.repository.projections.AssetGatewayView;
import com.pct.device.repository.projections.DeviceTypeCountView;
import com.pct.device.repository.projections.InProgressAssetTypeAndCountView;
import com.pct.device.service.device.DeviceCommand;
import com.pct.device.service.impl.DeviceServiceImpl;

@Component
public class BeanConverter {

	@Autowired
	private IManufacturerRepository manufacturerRepository;
	@Autowired
	private IManufacturerDetailsRepository manufacturerDetailsRepository;
	@Autowired
	private ILookupRepository assetConfigurationRepository;
	@Autowired
	private ICellularRepository cellularRepository;

	@Autowired
	private RedisDeviceRepository deviceRedisRepo;

	@Autowired
	private RestUtils restUtils;

	@Autowired
	private ILookupRepository lookupRepository;

	@Autowired
	private IDeviceRepository deviceRepository;

	@Autowired
	private IDeviceDeviceXrefRepository deviceDeviceXrefRepository;

	@Autowired
	DeviceForwardingRepository deviceForwardingRepository;

	@Autowired
	DeviceServiceImpl deviceServiceImpl;

	@Autowired
	IAssetRepository assetRepository;

	@Autowired
	EventRepository eventRepository;

	@Autowired
	IAssetDeviceXrefRepository assetDeviceXrefRepository;

	@Value("${jsonobject.value}")
	String jsonObject;

	@Autowired
	private RestHighLevelClient template;

	private static final List<String> DEVICE_INFO_FIELDS = new ArrayList() {
		{
			add("deviceId");
			add("latestMaintReportUUID");
			add("latestMaintReportTimeStamp");
			add("latestReportUUID");
			add("latestReportTimeStamp");
			add("latestReportEventId");
			add("latestRawReport");

		}
	};

	private static final Logger LOGGER = LoggerFactory.getLogger(BeanConverter.class);

	public CompanyPayload companyToCompanyPayload(Organisation company) {
		LOGGER.info("Inside companyToCompanyPayload and fetch company " + company);
		CompanyPayload comp = new CompanyPayload();
		comp.setCompanyName(company.getOrganisationName());
		comp.setId(company.getId());
		comp.setStatus(company.getIsActive());
		comp.setAccountNumber(company.getAccountNumber());
		comp.setIsAssetListRequired(company.getIsAssetListRequired());
		comp.setUuid(company.getUuid());
		comp.setShortName(company.getShortName());
		return comp;
	}

	public Organisation companyPayloadToCompanies(CompanyPayload com) {
		LOGGER.info("Inside companyPayloadToCompanies and fetch companypayload " + com);
		Organisation comp = new Organisation();
		comp.setOrganisationName(com.getCompanyName());
		comp.setId(com.getId());
		comp.setIsActive(com.getStatus());
		comp.setAccountNumber(com.getAccountNumber());
		comp.setIsAssetListRequired(com.getIsAssetListRequired());
		comp.setShortName(com.getShortName());
		comp.setUuid(com.getUuid());
		// comp.setType(OrganisationRole.getOrganisationRole(com.getType()));
		return comp;
	}

	public Organisation createCompanyPayloadToCompany(CreateCompanyPayload createCompanyPayload) {
		LOGGER.info("Inside createCompanyPayloadToCompany and fetch companypayload " + createCompanyPayload);
		Organisation company = new Organisation();
		company.setOrganisationName(createCompanyPayload.getCompanyName());
		return company;
	}

	public LookupPayload assetConfigurationToAssetConfigurationPayload(Lookup lookup) {
		LOGGER.info("Inside assetConfigurationToAssetConfigurationPayload and fetch lookup " + lookup);
		LookupPayload lookupPayload = new LookupPayload();
		lookupPayload.setId(lookup.getId());
		lookupPayload.setField(lookup.getField());
		lookupPayload.setValue(lookup.getValue());
		lookupPayload.setDisplayLabel(lookup.getDisplayLabel());
		return lookupPayload;
	}

	public Lookup assetConfigurationPayloadToAssetConfiguration(LookupPayload lookupPayload) {
		LOGGER.info("Inside assetConfigurationPayloadToAssetConfiguration and fetch lookup " + lookupPayload);
		Lookup lookup = new Lookup();
		lookup.setId(lookupPayload.getId());
		lookup.setField(lookupPayload.getField());
		return lookup;
	}

	public Lookup lookupPayloadToLookup(LookupPayload lookupPayload) {
		LOGGER.info("Inside lookupPayloadToLookup and fetch lookupPayload " + lookupPayload);
		Lookup lookup = new Lookup();
		lookup.setField(lookupPayload.getField());
		lookup.setValue(lookupPayload.getValue());
		lookup.setDisplayLabel(lookupPayload.getDisplayLabel());
		return lookup;
	}

	public AssetDTO convertAssetToAssetsDto(Asset asset) {

		LOGGER.info("Inside convertAssetToAssetsDto method and fetch lookupPayload " + asset);
		AssetDTO assetDto = new AssetDTO();
		assetDto.setId(asset.getId());
		assetDto.setAssignedName(asset.getAssignedName());
		assetDto.setDeviceEligibility(asset.getGatewayEligibility());
		assetDto.setVin(asset.getVin());
		if (asset.getYear() != null) {
			assetDto.setYear(asset.getYear());
		}
		assetDto.setManufacturer(asset.getManufacturer().getName());
		assetDto.setAccountNumber(asset.getOrganisation().getAccountNumber());
		assetDto.setCategory(asset.getCategory().getValue());
		assetDto.setStatus(asset.getStatus().getValue());
		assetDto.setCreatedBy(asset.getCreatedBy().getEmail());
		assetDto.setUpdatedBy(asset.getUpdatedBy());
		assetDto.setDateCreated(asset.getCreatedAt());
		assetDto.setDateUpdated(asset.getUpdatedAt());
		assetDto.setIsVinValidated(asset.getIsVinValidated());
		assetDto.setComment(asset.getComment());
		return assetDto;
	}

	public JobSummaryResponse createJobSummaryResponseFromProjections(
			List<DeviceTypeCountView> deviceReadyForInstallation,
			List<ApprovedAssetTypeCountView> assetsApprovedForInstallation,
			List<InProgressAssetTypeAndCountView> inProgressInstallation) {

		LOGGER.info("Inside createJobSummaryResponseFromProjections method ");

		Map<String, Integer> gatewayTypeToCount = deviceReadyForInstallation.stream()
				.collect(Collectors.toMap(DeviceTypeCountView::getProduct_code, DeviceTypeCountView::getCount));
		Map<String, Integer> assetTypeToCount = assetsApprovedForInstallation.stream().collect(
				Collectors.toMap(ApprovedAssetTypeCountView::getAsset_Type1, ApprovedAssetTypeCountView::getCount));
		Map<String, Integer> inProgressAssetTypeToCount = inProgressInstallation.stream().collect(Collectors
				.toMap(InProgressAssetTypeAndCountView::getAsset_Type1, InProgressAssetTypeAndCountView::getCount));
		JobSummaryResponse jobSummaryResponse = new JobSummaryResponse(gatewayTypeToCount, assetTypeToCount,
				inProgressAssetTypeToCount);
		return jobSummaryResponse;
	}

	public InventoryResponse createInventoryResponseFromProjections(
			List<DeviceTypeCountView> gatewayReadyForInstallation,
			List<ApprovedAssetTypeCountView> assetsApprovedForInstallation) {

		LOGGER.info("Inside createInventoryResponseFromProjections method ");

		Map<String, Integer> gatewayTypeToCount = gatewayReadyForInstallation.stream()
				.collect(Collectors.toMap(DeviceTypeCountView::getProduct_code, DeviceTypeCountView::getCount));
		Map<String, Integer> assetTypeToCount = assetsApprovedForInstallation.stream().collect(
				Collectors.toMap(ApprovedAssetTypeCountView::getAsset_Type1, ApprovedAssetTypeCountView::getCount));
		InventoryResponse inventoryResponse = new InventoryResponse();
		inventoryResponse.setAssets(assetTypeToCount);
		inventoryResponse.setGateways(gatewayTypeToCount);
		return inventoryResponse;
	}

	public CustomerDTO convertCompanyToCustomerDTO(Organisation company, List<DeviceShippingDetails> shipmentDetails) {

		LOGGER.info("Inside convertCompanyToCustomerDTO method and fetch shipmentDetails" + shipmentDetails);

		CustomerDTO customerDto = new CustomerDTO();
		customerDto.setAccountNumber(company.getAccountNumber());
		customerDto.setName(company.getOrganisationName());
		List<String> shipmentAddresses = shipmentDetails.stream().map(DeviceShippingDetails::getAddressShipped)
				.collect(Collectors.toList());
		customerDto.setShipToLocations(shipmentAddresses);
		return customerDto;
	}

	public AssetResponseDTO convertAssetListResponseDTOToAssetResponseDTO(AssetListResponseDTO asset)
			throws JsonProcessingException {

		LOGGER.info("Inside convertAssetListResponseDTOToAssetResponseDTO method and AssetListResponseDto " + asset);
		AssetResponseDTO assetResponseDto = new AssetResponseDTO();
		assetResponseDto.setAssetUuid(asset.getAssetUuid());
		assetResponseDto.setAssignedName(asset.getAssignedName());
		assetResponseDto.setStatus(asset.getStatus().getValue());
		assetResponseDto.setCategory(asset.getCategory().getValue());
		assetResponseDto.setVin(asset.getVin());
		ManufacturerDetailsDTO manufacturerDetailsDTO = new ManufacturerDetailsDTO();
		if (asset.getManufacturerDetails() != null) {
			manufacturerDetailsDTO.setMake(asset.getManufacturerDetails().getManufacturer().getName());
		}
		if (asset.getManufacturerDetails() != null) {
			manufacturerDetailsDTO.setModel(asset.getManufacturerDetails().getModel());
		}
		if (asset.getYear() != null) {
			manufacturerDetailsDTO.setYear(asset.getYear());
		}
		assetResponseDto.setManufacturerDetails(manufacturerDetailsDTO);
		assetResponseDto.setEligibleGateway(asset.getEligibleDevice());
		assetResponseDto.setCan(asset.getCan());
		List<Lookup> lookups = assetConfigurationRepository.findByField(asset.getCategory().getValue());
		assetResponseDto.setDisplayName(lookups.get(0).getValue());
		if (asset.getDatetimeCreated() != null)
			assetResponseDto.setDatetimeCreated(asset.getDatetimeCreated());
		if (asset.getDatetimeUpdated() != null)
			assetResponseDto.setDatetimeUpdated(asset.getDatetimeUpdated());
		assetResponseDto.setCompanyName(asset.getCompanyName());
		assetResponseDto.setIsVinValidated(asset.getIsVinValidated());
		assetResponseDto.setComment(asset.getComment());
		return assetResponseDto;
	}

	public ProductMaster convertProductMasterRequestToProductMasterBean(ProductMasterRequest productMasterRequest) {

		LOGGER.info("Inside convertProductMasterRequestToProductMasterBean method and fetch ProductMasteRequest "
				+ productMasterRequest);
		ProductMaster productMaster = new ProductMaster();
		productMaster.setProductCode(productMasterRequest.getProductCode());
		productMaster.setProductName(productMasterRequest.getProductName());
		productMaster.setSubtype(productMasterRequest.getSubtype());
		productMaster.setType(productMasterRequest.getType());
		productMaster.setBlocker(productMasterRequest.isBlocker());
		return productMaster;
	}

	public List<ProductMasterResponse> convertProductMasterToProductMasteResponse(
			List<ProductMaster> productMasterList) {
		LOGGER.info("Inside convertProductMasterToProductMasteResponse method and fetch ProductMasteList "
				+ productMasterList);
		List<ProductMasterResponse> productMasterResponseList = new ArrayList<ProductMasterResponse>();
		productMasterList.forEach(productMaster -> {
			ProductMasterResponse productMasterResponse = new ProductMasterResponse();
			productMasterResponse.setId(productMaster.getId());
			productMasterResponse.setUuid(productMaster.getUuid());
			productMasterResponse.setProduct_code(productMaster.getProductCode());
			productMasterResponse.setProduct_name(productMaster.getProductName());
			productMasterResponse.setSubtype(productMaster.getSubtype());
			productMasterResponse.setType(productMaster.getType());
			productMasterResponseList.add(productMasterResponse);
		});
		return productMasterResponseList;
	}

	public Asset assetsPayloadToAssets(AssetsPayloadMobile assetsPayloadMobile, boolean isCreated, User user) {

		LOGGER.info("Inside assetsPayloadToAssets method and fetch assetsPayloadMobile " + assetsPayloadMobile);
		Asset asset = new Asset();
		asset.setAssignedName(assetsPayloadMobile.getAssignedName());
		asset.setVin(assetsPayloadMobile.getVin());
		asset.setStatus(AssetStatus.PENDING);
		asset.setCategory(AssetCategory.getAssetCategory(assetsPayloadMobile.getAssetType().toUpperCase()));
		Organisation company = restUtils.getCompanyFromCompanyService(assetsPayloadMobile.getCan());
		asset.setOrganisation(company);

		if (null != assetsPayloadMobile.getAssetUuid() && !assetsPayloadMobile.getAssetUuid().isEmpty()) {
			asset.setUuid(assetsPayloadMobile.getAssetUuid());
		} else {
			asset.setUuid(UUID.randomUUID().toString());
		}

		if (isCreated) {
			asset.setCreatedBy(user);
		}
		asset.setUpdatedBy(user);
		return asset;
	}

	// isAdd is true, if operation is record insertion.false if operation is record
	// updation

	public Asset assetsPayloadToAssets(AssetsPayload assetsPayload, CompanyPayload companyPayload, Boolean isAdd,
			User user, Map<String, List<String>> validationFailedAssets) {

		LOGGER.info("Inside assetsPayloadToAssets method and fetch AssetsPayload " + assetsPayload);

		Asset asset = new Asset();
		asset.setAssignedName(assetsPayload.getAssignedName());
		asset.setCategory(AssetCategory.getAssetCategory(assetsPayload.getCategory().toUpperCase()));
		asset.setGatewayEligibility(assetsPayload.getEligibleGateway());
		if (assetsPayload.getManufacturer() != null && !assetsPayload.getManufacturer().isEmpty()) {
			Manufacturer manufacturer = manufacturerRepository.findByName(assetsPayload.getManufacturer());
			if (manufacturer != null) {
				asset.setManufacturer(manufacturer);
			} else {
				if (validationFailedAssets != null) {
					// if (validationFailedAssets.get(AssetAdminServiceImpl.ASSET_ERRORS[12]) !=
					// null) {
					// validationFailedAssets.get(AssetAdminServiceImpl.ASSET_ERRORS[12]).add(asset.getAssignedName());
					// } else {
					// List<String> failedAssetAssignedName = new ArrayList<>();
					// failedAssetAssignedName.add(asset.getAssignedName());
					// validationFailedAssets.put(AssetAdminServiceImpl.ASSET_ERRORS[12],
					// failedAssetAssignedName);
					// }
				}
			}
		}
		asset.setYear(assetsPayload.getYear());
		asset.setVin(assetsPayload.getVin());
		asset.setId(assetsPayload.getId());
		asset.setComment(assetsPayload.getComment());
		asset.setNoOfTires(assetsPayload.getNoOfTires());
		asset.setNoOfAxles(assetsPayload.getNoOfAxel());
		asset.setAssetNickName(assetsPayload.getAssetNickName());
		asset.setExternalLength(assetsPayload.getExternalLength());
		asset.setDoorType(assetsPayload.getDoorType());
		asset.setTag(assetsPayload.getTag());
		if (assetsPayload.getStatus() == null) {
			asset.setStatus(AssetStatus.PENDING);
		} else {
			asset.setStatus(AssetStatus.getAssetStatus(assetsPayload.getStatus()));
		}
		asset.setOrganisation(companyPayloadToCompanies(companyPayload));
		asset.setUuid(assetsPayload.getUuid());
		if (isAdd) {
			asset.setCreatedBy(user);
			asset.setCreatedAt(Instant.now());
		} else if (!isAdd) {

			asset.setUpdatedBy(user);
			asset.setUpdatedAt(Instant.now());
		}
		// asset.setUpdatedBy(user);
		return asset;
	}

	public AssetsPayload assetsassetsToPayload(Asset asset) {

		LOGGER.info("Inside assetsassetsToPayload method and fetch Assets" + asset);

		AssetsPayload assetsPayload = new AssetsPayload();
		assetsPayload.setAssignedName(asset.getAssignedName());
		assetsPayload.setCategory(asset.getCategory().getValue());
		assetsPayload.setEligibleGateway(asset.getGatewayEligibility());
		if (asset.getManufacturer() != null)
			assetsPayload.setManufacturer(asset.getManufacturer().getName());
		if (asset.getYear() != null) {
			assetsPayload.setYear(asset.getYear());
		} else {
			assetsPayload.setYear("Not Available");
		}
		assetsPayload.setVin(asset.getVin());
		assetsPayload.setId(asset.getId());
		assetsPayload.setCompany(companyToCompanyPayload(asset.getOrganisation()));
		assetsPayload.setUuid(asset.getUuid());
		assetsPayload.setStatus(asset.getStatus().getValue());
		// System.out.println(asset.getSensor().iterator().next().getId());
		// Set<SensorPayload>sensorSet=asset.getSensor().stream().map(this ::
		// SensorToSensorPayload).collect(Collectors.toSet());
		// asset.setSensor(sensorSet);
		assetsPayload.setIsVinValidated(asset.getIsVinValidated());
		assetsPayload.setComment(asset.getComment());
		assetsPayload.setNoOfTires(asset.getNoOfTires());
		assetsPayload.setNoOfAxel(asset.getNoOfAxles());
		assetsPayload.setAssetNickName(asset.getAssetNickName());
		assetsPayload.setExternalLength(asset.getExternalLength());
		assetsPayload.setDoorType(asset.getDoorType());
		assetsPayload.setTag(asset.getTag());
		return assetsPayload;
	}

	public AssetResponseDTO convertAssetArrayToAssetResponseDTO(Object[] assetDetails) throws JsonProcessingException {
		AssetResponseDTO assetResponseDto = new AssetResponseDTO();
		assetResponseDto.setAssetUuid(String.valueOf(assetDetails[15]));
		assetResponseDto.setAssignedName(String.valueOf(assetDetails[3]));
		return assetResponseDto;
	}

	public AssetResponseDTO convertAssetGatewayViewToAssetResponse(AssetGatewayView assetGatewayView) {

		LOGGER.info(
				"Inside convertAssetGatewayViewToAssetResponse method and fetch AssetGatewayView" + assetGatewayView);

		AssetResponseDTO assetResponseDto = new AssetResponseDTO();
		assetResponseDto.setAssetUuid(assetGatewayView.getUuid());
		assetResponseDto.setAssignedName(assetGatewayView.getAssigned_name());
		assetResponseDto.setStatus(assetGatewayView.getStatus().getValue());
		assetResponseDto.setCategory(assetGatewayView.getCategory().getValue());
		assetResponseDto.setVin(assetGatewayView.getVin());
		ManufacturerDetailsDTO manufacturerDetailsDTO = new ManufacturerDetailsDTO();
		if (assetGatewayView.getManufacturer_uuid() != null) {
			Manufacturer manufacturer = manufacturerRepository.findByUuid(assetGatewayView.getManufacturer_uuid());
			manufacturerDetailsDTO.setMake(manufacturer.getName());
		}
		if (assetGatewayView.getManufacturer_details_uuid() != null) {
			ManufacturerDetails manufacturerDetails = manufacturerDetailsRepository
					.findByUuid(assetGatewayView.getManufacturer_details_uuid());
			manufacturerDetailsDTO.setModel(manufacturerDetails.getModel());
		}
		manufacturerDetailsDTO.setYear(assetGatewayView.getYear());
		assetResponseDto.setManufacturerDetails(manufacturerDetailsDTO);
		assetResponseDto.setEligibleGateway(assetGatewayView.getGateway_eligibility());
		assetResponseDto.setCan(assetGatewayView.getAccount_number());
		List<Lookup> lookups = assetConfigurationRepository.findByField(assetGatewayView.getCategory().getValue());
		assetResponseDto.setDisplayName(lookups.get(0).getValue());
		if (assetGatewayView.getCreated_on() != null)
			assetResponseDto.setDatetimeCreated(assetGatewayView.getCreated_on().toString());
		if (assetGatewayView.getUpdated_on() != null)
			assetResponseDto.setDatetimeUpdated(assetGatewayView.getUpdated_on().toString());
		Organisation company = restUtils.getCompanyFromCompanyService(assetGatewayView.getAccount_number());
		assetResponseDto.setCompanyName(company.getOrganisationName());
		assetResponseDto.setIsVinValidated(assetGatewayView.getIs_vin_validated());
		assetResponseDto.setComment(assetGatewayView.getComment());
		return assetResponseDto;
	}

	// commented
	// public Map<String, InstallHistory>
	// createMapFromListOfInstallHistory(List<InstallHistory> installHistories) {
	// Map<String, InstallHistory> map = new HashMap<>();
	// for(InstallHistory ih : installHistories) {
	// map.put(ih.getAsset().getUuid(), ih);
	// }
	// return map;
	// }

	// commented
	public AssetResponseDTO convertAssetToAssetResponseDTO(Asset asset) throws JsonProcessingException {

		// LOGGER.info("Inside convertAssetToAssetResponseDTO method and fetch Asset" +
		// asset);
		AssetResponseDTO assetResponseDto = new AssetResponseDTO();
		assetResponseDto.setAssetUuid(asset.getUuid());
		assetResponseDto.setId(asset.getId());
		assetResponseDto.setAssignedName(asset.getAssignedName());
		if (asset.getStatus().equals(AssetStatus.ACTIVE) && asset.getInstallationDate() != null) {
			assetResponseDto.setInstallationDate(asset.getInstallationDate());
		}
		if (asset.getStatus() != null) {
			assetResponseDto.setStatus(asset.getStatus().getValue());
		}
		if (asset.getCategory() != null) {
			assetResponseDto.setCategory(asset.getCategory().getValue());
		}
		assetResponseDto.setVin(asset.getVin());
		ManufacturerDetailsDTO manufacturerDetailsDTO = new ManufacturerDetailsDTO();
		if (asset.getManufacturer() != null && asset.getManufacturer().getName() != null) {
			manufacturerDetailsDTO.setMake(asset.getManufacturer().getName());
		}
		if (asset.getManufacturerDetails() != null) {
			manufacturerDetailsDTO.setModel(asset.getManufacturerDetails().getModel());
		}
		if (asset.getYear() != null) {
			manufacturerDetailsDTO.setYear(asset.getYear());
		}
//		if(asset.getStatus().equals(InstallHistoryStatus.FINISHED) || asset.getStatus().equals(InstallHistoryStatus.PROBLEM)) {
//			
//			assetResponseDto.setInstalled(asset.getDateEnded().toString());
//	    }

		// else {
		// manufacturerDetailsDTO.setYear("Not Available");
		// }
		assetResponseDto.setManufacturerDetails(manufacturerDetailsDTO);
		assetResponseDto.setEligibleGateway(asset.getGatewayEligibility());
		if (asset.getOrganisation() != null) {
			assetResponseDto.setCan(asset.getOrganisation().getAccountNumber());
		}
		List<Lookup> lookups = null;
		if (asset.getCategory() != null) {
			lookups = assetConfigurationRepository.findByField(asset.getCategory().getValue());
		}
		if (lookups != null && lookups.size() > 0) {
			assetResponseDto.setDisplayName(lookups.get(0).getValue());
		}
		if (asset.getCreatedAt() != null)
			assetResponseDto.setDatetimeCreated(asset.getCreatedAt().toString());
		if (asset.getUpdatedAt() != null)
			assetResponseDto.setDatetimeUpdated(asset.getUpdatedAt().toString());
		assetResponseDto.setCompanyName(asset.getOrganisation().getOrganisationName());
		assetResponseDto.setIsVinValidated(asset.getIsVinValidated());
		assetResponseDto.setComment(asset.getComment());
		assetResponseDto.setNoOfTires(asset.getNoOfTires());
		assetResponseDto.setNoOfAxel(asset.getNoOfAxles());
		assetResponseDto.setAssetNickName(asset.getAssetNickName());
		assetResponseDto.setExternalLength(asset.getExternalLength());
		assetResponseDto.setDoorType(asset.getDoorType());
		assetResponseDto.setTag(asset.getTag());
//	        if(installHistory != null) {
//	            assetResponseDto.setImei(installHistory.getGateway().getImei());
//	            if(installHistory.getStatus().equals(InstallHistoryStatus.FINISHED)) {
//	                assetResponseDto.setInstalled(installHistory.getDateEnded().toString());
//	            }
//	        }
		return assetResponseDto;
	}

	public Page<AssetRecordPayload> convertAssetRecordToAssetRecordPayload(Page<AssetRecord> customerAssets,
			Pageable pageable) {
		List<AssetRecordPayload> assetRecordPayloadList = new ArrayList<>();
		customerAssets.forEach(customerAsset -> {
			AssetRecordPayload assetRecordPayload = new AssetRecordPayload();
			assetRecordPayload.setOrganisationId(customerAsset.getOrganisationId());
			assetRecordPayload.setOrganisationName(customerAsset.getOrganisationName());
			assetRecordPayload.setCount(customerAsset.getCount());
			assetRecordPayload.setCreatedAt(customerAsset.getCreatedAt());
			assetRecordPayload.setUpdatedAt(customerAsset.getUpdatedAt());
			assetRecordPayload.setCreatedFirstName(customerAsset.getCreatedFirstName());
			assetRecordPayload.setCreatedLastName(customerAsset.getCreatedLastName());
			assetRecordPayload.setUpdatedFirstName(customerAsset.getUpdatedFirstName());
			assetRecordPayload.setUpdatedLastName(customerAsset.getUpdatedLastName());
			assetRecordPayload.setForwardingGroup(customerAsset.getForwardingGroup());
			assetRecordPayload.setParentGroup(customerAsset.getParentGroup());
			assetRecordPayloadList.add(assetRecordPayload);
		});
		Page<AssetRecordPayload> page = new PageImpl<>(assetRecordPayloadList, pageable, assetRecordPayloadList.size());
		return page;
	}

	// return assetResponseDto;
	// }

	public DeviceResponsePayload convertDeviceDetailPayloadToDeviceBean(Device deviceDetails) {
		LOGGER.info("Inside convertDeviceDetailPayloadToDeviceBean method and fetch Device Request " + deviceDetails);
		DeviceResponsePayload device = new DeviceResponsePayload();
		device.setImei(deviceDetails.getImei());
//		device.setAsset_Device_xref(deviceDetails.getAssetDeviceXref());
		device.setId(deviceDetails.getId());
		if (deviceDetails.getDeviceDetails() != null) {
			device.setAppVersion(deviceDetails.getDeviceDetails().getAppVersion());
			device.setBinVersion(deviceDetails.getDeviceDetails().getBinVersion());
			device.setBleVersion(deviceDetails.getDeviceDetails().getBleVersion());
			device.setConfig1(deviceDetails.getDeviceDetails().getConfig1Name());
			device.setConfig2(deviceDetails.getDeviceDetails().getConfig2Name());
			device.setConfig3(deviceDetails.getDeviceDetails().getConfig3Name());
			device.setConfig4(deviceDetails.getDeviceDetails().getConfig4Name());
			device.setMcuVersion(deviceDetails.getDeviceDetails().getMcuVersion());
//			if (deviceDetails.getDeviceMaintenanceDetails() != null) {
//			device.setAppVersion(deviceDetails.getDeviceMaintenanceDetails().getAppVersion());
//			device.setBinVersion(deviceDetails.getDeviceMaintenanceDetails().getBinVersion());
//			device.setBleVersion(deviceDetails.getDeviceMaintenanceDetails().getBleVersion());
//			device.setConfig1(deviceDetails.getDeviceMaintenanceDetails().getConfig1Name());
//			device.setConfig2(deviceDetails.getDeviceMaintenanceDetails().getConfig2Name());
//			device.setConfig3(deviceDetails.getDeviceMaintenanceDetails().getConfig3Name());
//			device.setConfig4(deviceDetails.getDeviceMaintenanceDetails().getConfig4Name());
//			device.setMcuVersion(deviceDetails.getDeviceMaintenanceDetails().getMcuVersion());
		}
		// device.setOther1Version(deviceDetails.getOther1Version());
		// device.setOther2Version(deviceDetails.getOther2Version());
		device.setProductCode(deviceDetails.getProductCode());
		device.setProductName(deviceDetails.getProductName());
		device.setCan(deviceDetails.getOrganisation().getAccountNumber());
		device.setSon(deviceDetails.getSon());
		device.setType(deviceDetails.getIotType());
		device.setQuantityShipped(deviceDetails.getQuantityShipped());
		device.setCreatedBy(deviceDetails.getOrganisation().getOrganisationName());
		device.setStatus(deviceDetails.getStatus());
		device.setEpicorOrderNumber(deviceDetails.getEpicorOrderNumber());
		device.setMacAddress(deviceDetails.getMacAddress());
		device.setUuid(deviceDetails.getUuid());
		device.setUpdatedby(deviceDetails.getOrganisation().getOrganisationName());
		if (deviceDetails.getPurchaseBy() != null) {
			device.setPurchaseBy(deviceDetails.getPurchaseBy().getAccountNumber());
		}
		return device;
	}

	public GatewayPayload convertGatewayPayloadToGatewayBean(Device deviceDetails) {
		LOGGER.info("Inside convertGatewayDetailPayloadToGatewayBean method and fetch Device Request " + deviceDetails);
		GatewayPayload device = new GatewayPayload();
		device.setImei(deviceDetails.getImei());
		if (deviceDetails.getDeviceDetails() != null) {
			device.setAppVersion(deviceDetails.getDeviceDetails().getAppVersion());
			device.setBinVersion(deviceDetails.getDeviceDetails().getBinVersion());
			device.setBleVersion(deviceDetails.getDeviceDetails().getBleVersion());
			device.setConfig1(deviceDetails.getDeviceDetails().getConfig1Name());
			device.setConfig2(deviceDetails.getDeviceDetails().getConfig2Name());
			device.setConfig3(deviceDetails.getDeviceDetails().getConfig3Name());
			device.setConfig4(deviceDetails.getDeviceDetails().getConfig4Name());
			device.setMcuVersion(deviceDetails.getDeviceDetails().getMcuVersion());
//			if (deviceDetails.getDeviceMaintenanceDetails() != null) {
//			device.setAppVersion(deviceDetails.getDeviceMaintenanceDetails().getAppVersion());
//			device.setBinVersion(deviceDetails.getDeviceMaintenanceDetails().getBinVersion());
//			device.setBleVersion(deviceDetails.getDeviceMaintenanceDetails().getBleVersion());
//			device.setConfig1(deviceDetails.getDeviceMaintenanceDetails().getConfig1Name());
//			device.setConfig2(deviceDetails.getDeviceMaintenanceDetails().getConfig2Name());
//			device.setConfig3(deviceDetails.getDeviceMaintenanceDetails().getConfig3Name());
//			device.setConfig4(deviceDetails.getDeviceMaintenanceDetails().getConfig4Name());
//			device.setMcuVersion(deviceDetails.getDeviceMaintenanceDetails().getMcuVersion());
		}
		device.setProductCode(deviceDetails.getProductCode());
		device.setProductName(deviceDetails.getProductName());
		device.setSon(deviceDetails.getSon());
		device.setType(deviceDetails.getIotType());

		device.setCan(deviceDetails.getOrganisation().getAccountNumber());
		device.setQuantityShipped(deviceDetails.getQuantityShipped());
		device.setCreatedBy(deviceDetails.getOrganisation().getOrganisationName());
		device.setStatus(deviceDetails.getStatus());
		device.setEpicorOrderNumber(deviceDetails.getEpicorOrderNumber());
		device.setMacAddress(deviceDetails.getMacAddress());
		device.setUuid(deviceDetails.getUuid());
		device.setUpdatedby(deviceDetails.getOrganisation().getOrganisationName());
		return device;
	}

	public SensorPayLoad convertSensorToSensorPayLoad(Device deviceDetails) {
		SensorPayLoad devicepayload = new SensorPayLoad();
		devicepayload.setProductCode(deviceDetails.getProductCode());
		devicepayload.setProductName(deviceDetails.getProductName());
		// devicepayload.setSon(deviceDetails.getSon());
		// devicepayload.setEpicorOrderNumber(deviceDetails.getEpicorOrderNumber());
		// devicepayload.setCan(deviceDetails.getOrganisation().getAccountNumber());
		devicepayload.setMacAddress(deviceDetails.getMacAddress());
		devicepayload.setCreatedBy(deviceDetails.getOrganisation().getOrganisationName());
		devicepayload.setUpdatedBy(deviceDetails.getOrganisation().getOrganisationName());
		devicepayload.setUuid(deviceDetails.getUuid());
		devicepayload.setType(deviceDetails.getIotType());
		devicepayload.setStatus(deviceDetails.getStatus());
		List<SensorSubDetailPayload> sensorSubDetailPayloadList = new ArrayList<>();
		for (SensorDetail sensorDetail : deviceDetails.getSensorDetail()) {
			SensorSubDetailPayload sensorSubDetailPayload = new SensorSubDetailPayload();
			sensorSubDetailPayload.setPosition(sensorDetail.getPosition());
			sensorSubDetailPayload.setType(sensorDetail.getType());
			sensorSubDetailPayload.setSensorId(sensorDetail.getSensorId());
			sensorSubDetailPayload.setUuid(sensorDetail.getUuid());
			sensorSubDetailPayload.setSensorUUID(sensorDetail.getSensorUUID().getUuid());
			sensorSubDetailPayloadList.add(sensorSubDetailPayload);
		}
		devicepayload.setSensorSubDetail(sensorSubDetailPayloadList);
		return devicepayload;
	}

	public BeaconPayload convertBeaconToBeaconPayLoad(Device deviceDetails) {
		BeaconPayload devicepayload = new BeaconPayload();
		if (deviceDetails.getDeviceDetails() != null) {
			devicepayload.setAppVersion(deviceDetails.getDeviceDetails().getAppVersion());
			devicepayload.setBinVersion(deviceDetails.getDeviceDetails().getBinVersion());
			devicepayload.setBleVersion(deviceDetails.getDeviceDetails().getBleVersion());
			devicepayload.setConfig1(deviceDetails.getDeviceDetails().getConfig1Name());
			devicepayload.setMcuVersion(deviceDetails.getDeviceDetails().getMcuVersion());
//			if (deviceDetails.getDeviceMaintenanceDetails() != null) {
//			devicepayload.setAppVersion(deviceDetails.getDeviceMaintenanceDetails().getAppVersion());
//			devicepayload.setBinVersion(deviceDetails.getDeviceMaintenanceDetails().getBinVersion());
//			devicepayload.setBleVersion(deviceDetails.getDeviceMaintenanceDetails().getBleVersion());
//			devicepayload.setConfig1(deviceDetails.getDeviceMaintenanceDetails().getConfig1Name());
//			devicepayload.setMcuVersion(deviceDetails.getDeviceMaintenanceDetails().getMcuVersion());
		}
		devicepayload.setProductCode(deviceDetails.getProductCode());
		devicepayload.setProductName(deviceDetails.getProductName());
		devicepayload.setSon(deviceDetails.getSon());
		devicepayload.setEpicorOrderNumber(deviceDetails.getEpicorOrderNumber());
		devicepayload.setCan(deviceDetails.getOrganisation().getAccountNumber());
		devicepayload.setMacAddress(deviceDetails.getMacAddress());
		devicepayload.setCreatedBy(deviceDetails.getOrganisation().getOrganisationName());
		devicepayload.setUpdatedBy(deviceDetails.getOrganisation().getOrganisationName());
		devicepayload.setUuid(deviceDetails.getUuid());
		devicepayload.setStatus(deviceDetails.getStatus());
		devicepayload.setType(deviceDetails.getIotType());
		return devicepayload;
	}

	public Device convertDeviceDetailRequestToDeviceBean(DeviceDetailsRequest deviceUploadRequest) {
		LOGGER.info(
				"Inside convertDeviceDetailRequestToDeviceBean method and fetch Device Request " + deviceUploadRequest);
		Device device = new Device();
		device.setImei(deviceUploadRequest.getImei());
		device.setProductCode(deviceUploadRequest.getProductCode());
		device.setProductName(deviceUploadRequest.getProductName());
		device.setSon(deviceUploadRequest.getSon());
		device.setIotType(deviceUploadRequest.getType());
		device.setQuantityShipped(deviceUploadRequest.getQuantityShipped());
		device.setStatus(DeviceStatus.PENDING);
		device.setCreatedAt(Instant.now());
		device.setEpicorOrderNumber(deviceUploadRequest.getEpicorOrderNumber());
		device.setMacAddress(deviceUploadRequest.getMacAddress());
		device.setQaStatus(deviceUploadRequest.getQaStatus());
		device.setUsageStatus(deviceUploadRequest.getUsage_status());
		device.setDeviceType(deviceUploadRequest.getDeviceType());
		device.setOwnerLevel2(deviceUploadRequest.getOwnerLevel2());
		device.setQaDate(Instant.now());
		Cellular cellularPayload = new Cellular();
		if (deviceUploadRequest.getCellularPayload() != null) {
			cellularPayload.setCarrierId(deviceUploadRequest.getCellularPayload().getCarrierId());
			cellularPayload.setCellular(deviceUploadRequest.getCellularPayload().getCellular());
			cellularPayload.setCountryCode(deviceUploadRequest.getCellularPayload().getCountryCode());
			cellularPayload.setIccid(deviceUploadRequest.getCellularPayload().getIccid());
			cellularPayload.setImsi(deviceUploadRequest.getCellularPayload().getImsi());
			cellularPayload.setPhone(deviceUploadRequest.getCellularPayload().getPhone());
			cellularPayload.setServiceCountry(deviceUploadRequest.getCellularPayload().getServiceCountry());
			cellularPayload.setServiceNetwork(deviceUploadRequest.getCellularPayload().getServiceNetwork());
			cellularPayload.setImei(deviceUploadRequest.getCellularPayload().getImei());

			boolean isCellularUuidUnique = false;
			String cellularUuid = "";
			while (!isCellularUuidUnique) {
				cellularUuid = UUID.randomUUID().toString();
				Cellular byUuid = cellularRepository.findByUuid(cellularUuid);
				if (byUuid == null) {
					isCellularUuidUnique = true;
				}
			}
			cellularPayload.setUuid(cellularUuid);
			// cellularRepository.save(cellularPayload);
		}
		device.setCellular(cellularPayload);
		return device;
	}

	// public Page<DeviceResponsePayload> convertDeviceToDevicePayLoad(Page<Device>
	// deviceDetails, Pageable pageable,
	// boolean forExport) {
	// List<String> imei = deviceDetails.stream().map(e ->
	// e.getImei()).collect(Collectors.toList());
	// List<Event> eventList = eventRepository.findAll();
	// List<SearchHit[]> hitsForMaintainence = null;
	// List<SearchHit[]> hitsForLatestReport = null;
	// try {
	// hitsForMaintainence = getDataFromElasticforDeviceIds(imei, true);
	// } catch (IOException e1) {
	// e1.printStackTrace();
	// }
	// try {
	// hitsForLatestReport = getDataFromElasticforDeviceIds(imei, false);
	// } catch (IOException e1) {
	// e1.printStackTrace();
	// }
	// final List<SearchHit[]> hits = hitsForMaintainence;
	// final List<SearchHit[]> hits2 = hitsForLatestReport;
	// List<DeviceResponsePayload> devicePayLoadList = new ArrayList<>();
	// deviceDetails.forEach(deviceDetail -> {
	// DeviceResponsePayload devicepayload = new DeviceResponsePayload();
	// try {
	// if (hits != null) {
	// devicepayload = getDataFromElastic(hits, devicepayload,
	// deviceDetail.getImei());
	// }
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// devicepayload.setImei(deviceDetail.getImei());
	// devicepayload.setDeviceId(deviceDetail.getImei());
	// devicepayload.setProductCode(deviceDetail.getProductCode());
	// devicepayload.setProductName(deviceDetail.getProductName());
	// devicepayload.setModel(deviceDetail.getProductName());
	// devicepayload.setSon(deviceDetail.getSon());
	// devicepayload.setOrder(deviceDetail.getSon());
	// devicepayload.setType(deviceDetail.getIotType());
	// devicepayload.setEpicorOrderNumber(deviceDetail.getEpicorOrderNumber());
	// devicepayload.setMacAddress(deviceDetail.getMacAddress());
	// devicepayload.setQuantityShipped(deviceDetail.getQuantityShipped());
	//
	// if (deviceDetail.getOrganisation() != null) {
	// devicepayload.setCan(deviceDetail.getOrganisation().getAccountNumber());
	// devicepayload.setCreatedBy(deviceDetail.getOrganisation().getOrganisationName());
	// devicepayload.setCustomer(deviceDetail.getOrganisation().getOrganisationName());
	// devicepayload.setUpdatedby(deviceDetail.getOrganisation().getOrganisationName());
	// }
	// devicepayload.setUuid(deviceDetail.getUuid());
	// devicepayload.setType(deviceDetail.getIotType());
	// devicepayload.setStatus(deviceDetail.getStatus());
	// devicepayload.setQaStatus(deviceDetail.getQaStatus());
	// devicepayload.setUsageStatus(deviceDetail.getUsageStatus());
	// devicepayload.setOwnerLevel2(deviceDetail.getOwnerLevel2());
	//
	//// LOGGER.info("Before format:- "+deviceDetail.getQaDate().toString());
	//
	// DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
	// .withZone(ZoneId.of("America/Los_Angeles"));
	// if (deviceDetail.getQaDate() != null) {
	// String now = dtf.format(deviceDetail.getQaDate());
	// devicepayload.setQaDates(now);
	// }
	//// if(deviceDetail.getLatestReport()!=null) {
	//// String dateFormate = "yyyy-MM-dd HH:mm:ss";
	//// SimpleDateFormat dateFormat = new SimpleDateFormat(dateFormate);
	//// devicepayload.setLastReportDate(
	// dateFormat.format(deviceDetail.getLatestReport()));
	//// }
	// try {
	// if (hits2 != null) {
	// devicepayload = getLatestReportFromElastic(hits2, deviceDetail.getImei(),
	// devicepayload, eventList);
	// }
	// } catch (Exception e) {
	// e.printStackTrace();
	// // TODO: handle exception
	// }
	//
	//// if (deviceDetail.getDeviceDetails() != null) {
	//// devicepayload.setAppVersion(deviceDetail.getDeviceDetails().getAppVersion());
	//// devicepayload.setBinVersion(deviceDetail.getDeviceDetails().getBinVersion());
	//// devicepayload.setBleVersion(deviceDetail.getDeviceDetails().getBleVersion());
	//// devicepayload.setMcuVersion(deviceDetail.getDeviceDetails().getMcuVersion());
	////
	//// devicepayload.setConfig1Name(deviceDetail.getDeviceDetails().getConfig1Name());
	//// devicepayload.setConfig1CRC(deviceDetail.getDeviceDetails().getConfig1CRC());
	//// devicepayload.setConfig2Name(deviceDetail.getDeviceDetails().getConfig2Name());
	//// devicepayload.setConfig2CRC(deviceDetail.getDeviceDetails().getConfig2CRC());
	//// devicepayload.setConfig3Name(deviceDetail.getDeviceDetails().getConfig3Name());
	//// devicepayload.setConfig3CRC(deviceDetail.getDeviceDetails().getConfig3CRC());
	//// devicepayload.setConfig4Name(deviceDetail.getDeviceDetails().getConfig4Name());
	//// devicepayload.setConfig4CRC(deviceDetail.getDeviceDetails().getConfig4CRC());
	//// devicepayload.setDevuserCfgName(deviceDetail.getDeviceDetails().getDevuserCfgName());
	//// devicepayload.setDevuserCfgValue(deviceDetail.getDeviceDetails().getDevuserCfgValue());
	////
	//// devicepayload.setLastReportDateTime(deviceDetail.getDeviceDetails().getLatestReport());
	//// devicepayload.setEventId(deviceDetail.getDeviceDetails().getEventId());
	//// devicepayload.setEventType(deviceDetail.getDeviceDetails().getEventType());
	//// devicepayload.setBattery(deviceDetail.getDeviceDetails().getBattery());
	//// devicepayload.setLat(deviceDetail.getDeviceDetails().getLat());
	//// devicepayload.setLongitude(deviceDetail.getDeviceDetails().getLongitude());
	////
	////
	//// }
	//
	// CellularDetailPayload cellularPayload = new CellularDetailPayload();
	// if (deviceDetail.getCellular() != null) {
	// cellularPayload.setCarrierId(deviceDetail.getCellular().getCarrierId());
	// cellularPayload.setCountryCode(deviceDetail.getCellular().getCountryCode());
	// cellularPayload.setIccid(deviceDetail.getCellular().getIccid());
	// cellularPayload.setImsi(deviceDetail.getCellular().getImsi());
	// cellularPayload.setImei(deviceDetail.getCellular().getImei());
	// cellularPayload.setPhone(deviceDetail.getCellular().getPhone());
	// cellularPayload.setServiceCountry(deviceDetail.getCellular().getServiceCountry());
	// cellularPayload.setServiceNetwork(deviceDetail.getCellular().getServiceNetwork());
	// cellularPayload.setCellular(deviceDetail.getCellular().getCellular());
	//
	// devicepayload.setCarrierId(deviceDetail.getCellular().getCarrierId());
	// devicepayload.setCountryCode(deviceDetail.getCellular().getCountryCode());
	// devicepayload.setPhone(deviceDetail.getCellular().getPhone());
	// devicepayload.setSim(deviceDetail.getCellular().getCellular());
	// devicepayload.setServiceCountry(deviceDetail.getCellular().getServiceCountry());
	// devicepayload.setServiceNetwork(deviceDetail.getCellular().getServiceNetwork());
	// }
	//
	// devicepayload.setCellularPayload(cellularPayload);
	//// List<DeviceForwarding> al =
	// deviceForwardingRepository.findByImei(deviceDetail.getImei());
	// List<DeviceForwardingResponse> list = new
	// ArrayList<DeviceForwardingResponse>();
	// int k = 0;
	// if (deviceDetail.getDeviceForwarding() != null &&
	// deviceDetail.getDeviceForwarding().size() > 0) {
	// for (DeviceForwarding pm : deviceDetail.getDeviceForwarding()) {
	// DeviceForwardingResponse response = new DeviceForwardingResponse();
	// response.setId(pm.getId());
	// response.setUuid(pm.getUuid());
	// response.setType(pm.getType());
	// response.setUrl(pm.getUrl());
	// Device device = new Device();
	// if (pm.getDevice() != null) {
	// device.setUuid(pm.getDevice().getUuid());
	// device.setImei(pm.getDevice().getImei());
	// }
	// response.setDevice(device);
	// if (k == 0) {
	// devicepayload.setType1(pm.getType());
	// devicepayload.setUrl1(pm.getUrl());
	// } else if (k == 1) {
	// devicepayload.setType2(pm.getType());
	// devicepayload.setUrl2(pm.getUrl());
	// } else if (k == 2) {
	// devicepayload.setType3(pm.getType());
	// devicepayload.setUrl3(pm.getUrl());
	// } else if (k == 3) {
	// devicepayload.setType4(pm.getType());
	// devicepayload.setUrl4(pm.getUrl());
	// }
	// list.add(response);
	// }
	// }
	// devicepayload.setDeviceForwardingPayload(list);
	// try {
	//// Asset_Device_xref asset_Device_xref =
	// assetDeviceXrefRepository.findByDevice(deviceDetail.getImei());
	//
	// Asset_Device_xref asset_Device_xref = deviceDetail.getAssetDeviceXref();
	// if (asset_Device_xref != null) {
	// if (asset_Device_xref.getAsset() != null) {
	// devicepayload.setAssetName(asset_Device_xref.getAsset().getAssignedName());
	// devicepayload.setAssetType(asset_Device_xref.getAsset().getCategory().toString());
	// devicepayload.setVin(asset_Device_xref.getAsset().getVin());
	// if (asset_Device_xref.getAsset().getManufacturer() != null) {
	// devicepayload.setManufacturer(asset_Device_xref.getAsset().getManufacturer().getName());
	// }
	// }
	// }
	//
	// DeviceSignature deviceSignature = deviceDetail.getDeviceSignature();
	// if (!AppUtility.isEmpty(deviceSignature)) {
	// devicepayload.setImeiHashed(deviceSignature.getImeiHashed());
	// devicepayload.setCreatedTime(deviceSignature.getCreatedTime());
	// devicepayload.setRevokedTime(deviceSignature.getRevokedTime());
	// if (!AppUtility.isEmpty(deviceSignature.getImeiHashed())) {
	// if (deviceSignature.getImeiHashed().equals("REVOKED")) {
	// devicepayload.setSecStatus("Unlocked");
	// devicepayload.setSecDate(deviceSignature.getRevokedTime());
	// } else {
	// devicepayload.setSecStatus("Locked");
	// devicepayload.setSecDate(deviceSignature.getCreatedTime());
	// }
	// } else {
	// devicepayload.setSecStatus("N/A");
	// }
	//
	// } else {
	// devicepayload.setSecStatus("N/A");
	// }
	//
	// } catch (Exception ex) {
	// ex.printStackTrace();
	// }
	//
	// devicePayLoadList.add(devicepayload);
	// });
	// Page<DeviceResponsePayload> page = new PageImpl<>(devicePayLoadList,
	// pageable,
	// deviceDetails.getTotalElements());
	// return page;
	// }

	public JSONObject getLatestReportforDeviceId(String deviceId, boolean maintainenceReport) throws IOException {
		SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
		BoolQueryBuilder queryBuilder;
		if (maintainenceReport) {
			queryBuilder = QueryBuilders.boolQuery().must(QueryBuilders.matchQuery("report_header.device_id", deviceId))
					.must(QueryBuilders.matchQuery("report_header.event_id", 34));
		} else {
			queryBuilder = QueryBuilders.boolQuery()
					.must(QueryBuilders.matchQuery("report_header.device_id", deviceId));
		}
		sourceBuilder.query(queryBuilder);
		sourceBuilder.size(1).sort("general_mask_fields.received_time_stamp.keyword", SortOrder.DESC);
		SearchRequest searchRequest = new SearchRequest();
		searchRequest.indices(jsonObject);
		searchRequest.source(sourceBuilder);
		SearchResponse searchResponse = template.search(searchRequest, RequestOptions.DEFAULT);
		SearchHit[] hits = searchResponse.getHits().getHits();
		if (hits.length > 0) {
			SearchHit searchHit = hits[0];
			return new JSONObject(searchHit.getSourceAsString());
		}
		return null;
	}

	public List<SearchHit[]> getDataFromElasticforDeviceIds(List<String> deviceIds, boolean maintainenceReport)
			throws IOException {
		SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
		BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
		if (maintainenceReport) {
			sourceBuilder.query(queryBuilder.must(QueryBuilders.termsQuery("report_header.device_id", deviceIds))
					.must(QueryBuilders.matchQuery("report_header.event_id", 34)));
		} else {
			sourceBuilder.query(queryBuilder.must(QueryBuilders.termsQuery("report_header.device_id", deviceIds)));
		}
		// List<SortBuilder<?>> sortBuilders = new ArrayList<>();
		sourceBuilder.size(0);
		AggregationBuilder aggregation = AggregationBuilders.terms("unique_id").field("report_header.device_id.keyword")
				.subAggregation(AggregationBuilders.topHits("top_result").size(1).sort(SortBuilders
						.fieldSort("general_mask_fields.received_time_stamp.keyword").order(SortOrder.DESC)));

		sourceBuilder.aggregation(aggregation);
		// sortBuilders
		// .add(SortBuilders.fieldSort("general_mask_fields.received_time_stamp.keyword").order(SortOrder.DESC));
		// sourceBuilder.collapse(new CollapseBuilder("report_header.device_id.keyword")
		// .setInnerHits(new
		// InnerHitBuilder("most_recent").setSize(1).setSorts(sortBuilders)));
		SearchRequest searchRequest = new SearchRequest();
		searchRequest.indices(jsonObject);
		searchRequest.source(sourceBuilder);
		List<SearchHit[]> serchHitList = new ArrayList<>();
		SearchResponse searchResponse = template.search(searchRequest, RequestOptions.DEFAULT);
		Aggregations aggregations = searchResponse.getAggregations();
		Terms byRecommenderId = aggregations.get("unique_id");
		for (Terms.Bucket bucket : byRecommenderId.getBuckets()) {
			Aggregations aggregations2 = bucket.getAggregations();
			TopHits topResult = aggregations2.get("top_result");
			serchHitList.add(topResult.getHits().getHits());
		}
		// System.out.println("total documents returned are " + hits.length);
		return serchHitList;
	}

	public DeviceResponsePayload getDataFromElastic(List<SearchHit[]> listHits, DeviceResponsePayload devicepayload,
			String imei) {
		for (SearchHit[] hits : listHits) {
			for (SearchHit searchHit : hits) {
				JSONObject jsonString = new JSONObject(searchHit.getSourceAsString());
				JSONObject reportHeader = AppUtility.checkJSONObjectNullKey(jsonString, "report_header");
				String deviceId = reportHeader != null ? AppUtility.checkNullKey(reportHeader, "device_id") : null;
				if (deviceId != null && imei.equals(deviceId)) {
					JSONObject softwareVersion = AppUtility.checkJSONObjectNullKey(jsonString, "software_version");
					devicepayload.setAppVersion(
							softwareVersion != null ? AppUtility.checkNullKey(softwareVersion, "app_version") : null);
					devicepayload.setBinVersion(
							softwareVersion != null ? AppUtility.checkNullKey(softwareVersion, "os_version") : null);
					devicepayload.setBleVersion(
							softwareVersion != null ? AppUtility.checkNullKey(softwareVersion, "ble_version") : null);
					devicepayload.setMcuVersion(
							softwareVersion != null ? AppUtility.checkNullKey(softwareVersion, "extender_version")
									: null);

					JSONObject waterfall = AppUtility.checkJSONObjectNullKey(jsonString, "waterfall");
					JSONArray waterFallInfoArray = (waterfall != null
							? AppUtility.checkJSONArrayNullKey(waterfall, "waterfall_info")
							: null);
					if (waterFallInfoArray != null) {
						for (int i = 0; i < waterFallInfoArray.length(); i++) {
							JSONObject waterfallInfo = waterFallInfoArray.getJSONObject(i);
							int id = (waterfallInfo != null ? AppUtility.checkIntNullKey(waterfallInfo, "config_id")
									: 0);
							if (id == 1) {
								devicepayload.setConfig1Name(waterfallInfo != null
										? AppUtility.checkNullKey(waterfallInfo, "config_identification_version")
										: null);
								devicepayload.setConfig1CRC(
										waterfallInfo != null ? AppUtility.checkNullKey(waterfallInfo, "config_crc")
												: null);
							}
							if (id == 2) {
								devicepayload.setConfig2Name(waterfallInfo != null
										? AppUtility.checkNullKey(waterfallInfo, "config_identification_version")
										: null);
								devicepayload.setConfig2CRC(
										waterfallInfo != null ? AppUtility.checkNullKey(waterfallInfo, "config_crc")
												: null);
							}
							if (id == 3) {
								devicepayload.setConfig3Name(waterfallInfo != null
										? AppUtility.checkNullKey(waterfallInfo, "config_identification_version")
										: null);
								devicepayload.setConfig3CRC(
										waterfallInfo != null ? AppUtility.checkNullKey(waterfallInfo, "config_crc")
												: null);
							}
							if (id == 4) {
								devicepayload.setConfig4Name(waterfallInfo != null
										? AppUtility.checkNullKey(waterfallInfo, "config_identification_version")
										: null);
								devicepayload.setConfig4CRC(
										waterfallInfo != null ? AppUtility.checkNullKey(waterfallInfo, "config_crc")
												: null);
							}

							if (id == 5) {
								devicepayload.setDevuserCfgName(waterfallInfo != null
										? AppUtility.checkNullKey(waterfallInfo, "config_identification_version")
										: null);
								devicepayload.setDevuserCfgValue(
										waterfallInfo != null ? AppUtility.checkNullKey(waterfallInfo, "config_crc")
												: null);
							}

						}
					}
					break;
				}
			}
		}
		return devicepayload;
	}

	public DeviceResponsePayload getLatestReportFromElastic(List<SearchHit[]> listHits, String imei,
			DeviceResponsePayload devicepayload, List<Event> eventList) {
		String latestReport = null;
		int eventId = 0;
		String eventType = null;
		Float battery = null;
		Float latitude = null;
		Float longitude = null;
		for (SearchHit[] hits : listHits) {
			for (SearchHit searchHit : hits) {
				JSONObject jsonString = new JSONObject(searchHit.getSourceAsString());
				JSONObject reportHeader = AppUtility.checkJSONObjectNullKey(jsonString, "report_header");
				JSONObject voltage = AppUtility.checkJSONObjectNullKey(jsonString, "voltage");
				String deviceId = reportHeader != null ? AppUtility.checkNullKey(reportHeader, "device_id") : null;
				if (deviceId != null && imei.equals(deviceId)) {
					JSONObject generalMaskFields = AppUtility.checkJSONObjectNullKey(jsonString, "general_mask_fields");
					latestReport = (generalMaskFields != null
							? AppUtility.checkNullKey(generalMaskFields, "received_time_stamp")
							: null);
					eventId = (reportHeader != null ? AppUtility.checkIntNullKey(reportHeader, "event_id") : 0);
					eventType = getEventByEventId(eventList, eventId);
					battery = (voltage != null ? AppUtility.checkFlotNullKey(voltage, "battery_power") : null);
					latitude = (generalMaskFields != null ? AppUtility.checkFlotNullKey(generalMaskFields, "latitude")
							: null);
					longitude = (generalMaskFields != null ? AppUtility.checkFlotNullKey(generalMaskFields, "longitude")
							: null);
					break;
				}
			}
		}
		devicepayload.setLastReportDate(latestReport);
		devicepayload.setEventId(eventId);
		devicepayload.setEventType(eventType);
		devicepayload.setBattery(battery);
		devicepayload.setLat(latitude);
		devicepayload.setLongitude(longitude);
		return devicepayload;
	}

	public String getEventByEventId(List<Event> eventList, int id) {
		if (id > 0) {
			Event event = eventList.stream().filter(e -> e.getEventId() == id).findAny().orElse(null);
			if (event != null) {
				return event.getEventType();
			}
		}
		return null;
	}

	public Page<DeviceWithSensorPayload> convertDeviceToDeviceSensorPayLoad(Page<Device> deviceDetails,
			Pageable pageable) {
		List<DeviceWithSensorPayload> devicePayLoadList = new ArrayList<>();

		deviceDetails.forEach(deviceDetail -> {
			DeviceWithSensorPayload devicepayload = new DeviceWithSensorPayload();
			devicepayload.setUuid(deviceDetail.getUuid());
			devicepayload.setProductName(deviceDetail.getProductName());
			devicepayload.setProductCode(deviceDetail.getProductCode());
			if (deviceDetail.getOrganisation() != null) {
				devicepayload.setCompanyName(deviceDetail.getOrganisation().getOrganisationName());
				devicepayload.setAccountNumber(deviceDetail.getOrganisation().getAccountNumber());
//				devicepayload.setCreatedBy(deviceDetail.getOrganisation().getOrganisationName());
//				devicepayload.setUpdatedBy(deviceDetail.getOrganisation().getOrganisationName());
			}
			if (!AppUtility.isEmpty(deviceDetail.getCreatedBy())) {
				devicepayload.setCreatedBy(deviceDetail.getCreatedBy().getFirstName());
			}
			if (!AppUtility.isEmpty(deviceDetail.getUpdatedBy())) {
				devicepayload.setUpdatedBy(deviceDetail.getUpdatedBy().getFirstName());
			}
			
			if (!AppUtility.isEmpty(deviceDetail.getStatus())) {
				devicepayload.setStatus(deviceDetail.getStatus().toString());
			}
			devicepayload.setOrderNo(deviceDetail.getSon());
			if (deviceDetail.getIotType() != null) {
				devicepayload.setGatewayType(deviceDetail.getIotType().toString());
			}
			devicepayload.setCreatedAt(deviceDetail.getCreatedAt());
			devicepayload.setUpdatedAt(deviceDetail.getUpdatedAt());

			if (deviceDetail.getIotType() != null
					&& IOTType.BEACON.equals(IOTType.getValue(deviceDetail.getIotType().toString()))) {
				devicepayload.setImei(deviceDetail.getMacAddress());
				devicepayload.setAbsSensor("N/A");
				devicepayload.setWheelEnd("N/A");
				devicepayload.setCargoSensor("N/A");
				devicepayload.setAirTank("N/A");
				devicepayload.setTpms("N/A");
				devicepayload.setRegulator("N/A");
				devicepayload.setMicroSpTransceiver("N/A");
				devicepayload.setDoorSensor("N/A");
				devicepayload.setLightSentry("N/A");
				devicepayload.setAtisSensor("N/A");
				devicepayload.setLampCheckAtis("N/A");
				devicepayload.setLampCheckABS("N/A");
				devicepayload.setPctCargoSensor("N/A");
				devicepayload.setMicroSpAirTank("N/A");
				devicepayload.setMicroSpATISRegulator("N/A");
				devicepayload.setMicroSpWiredReceiver("N/A");
				devicepayload.setMicroSpTPMS("N/A");
				devicepayload.setMicroSpTPMSOuter("N/A");
				devicepayload.setMicroSpTPMSInner("N/A");
				devicepayload.setPctCargoCameraG1("N/A");
				devicepayload.setPctCargoCameraG2("N/A");
				devicepayload.setPctCargoCameraG3("N/A");
			} else if (IOTType.GATEWAY.equals(IOTType.getValue(deviceDetail.getIotType().toString()))) {
				devicepayload.setImei(deviceDetail.getImei());
				List<Device_Device_xref> sensorList = deviceDeviceXrefRepository.findByDeviceUuid(deviceDetail);
				if (sensorList != null && !sensorList.isEmpty())
					for (Device_Device_xref sensor : sensorList) {
						if (!AppUtility.isEmpty(sensor.getSensorUuid())) {
							if (sensor.getSensorUuid().getProductCode().equalsIgnoreCase("77-H101")) {
								devicepayload.setAbsSensor("Y");
							} else if (sensor.getSensorUuid().getProductCode().equalsIgnoreCase("77-S119")) {
								if (!AppUtility.isEmpty(sensor.getSensorUuid().getStatus())
										&& !sensor.getSensorUuid().getStatus().equals(GatewayStatus.INSTALL_IN_PROGRESS)
										&& !sensor.getSensorUuid().getStatus().equals(GatewayStatus.INSTALLED)) {
									devicepayload.setWheelEnd("Y");
								} else {
									try {
										Integer count = restUtils.getSensorCount(sensor.getDeviceUuid().getUuid());
										devicepayload.setWheelEnd(count != null ? count.toString() : "0");
									} catch (Exception ex) {
										ex.printStackTrace();
									}
								}

							} else if (sensor.getSensorUuid().getProductCode().equalsIgnoreCase("77-S102")) {
								devicepayload.setCargoSensor("Y");
							} else if (sensor.getSensorUuid().getProductCode().equalsIgnoreCase("77-S137")) {
								devicepayload.setAirTank("Y");
							} else if (sensor.getSensorUuid().getProductCode().equalsIgnoreCase("77-S500")) {
								if (!sensor.getSensorUuid().getStatus().equals(GatewayStatus.INSTALL_IN_PROGRESS)
										&& !sensor.getSensorUuid().getStatus().equals(GatewayStatus.INSTALLED)) {
									devicepayload.setTpms("Y");
								} else {
									Integer count = restUtils.getSensorCount(sensor.getDeviceUuid().getUuid());
									devicepayload.setTpms(count != null ? count.toString() : "0");
								}

							} else if (sensor.getSensorUuid().getProductCode().equalsIgnoreCase("77-S164")) {
								devicepayload.setRegulator("Y");
							} else if (sensor.getSensorUuid().getProductCode().equalsIgnoreCase("77-S196")) {
								devicepayload.setMicroSpTransceiver("Y");
							} else if (sensor.getSensorUuid().getProductCode().equalsIgnoreCase("77-S108")) {
								devicepayload.setDoorSensor("Y");
							} else if (sensor.getSensorUuid().getProductCode().equalsIgnoreCase("77-S107")) {
								devicepayload.setLightSentry("Y");
							} else if (sensor.getSensorUuid().getProductCode().equalsIgnoreCase("77-S120")) {
								devicepayload.setAtisSensor("Y");
							} else if (sensor.getSensorUuid().getProductCode().equalsIgnoreCase("77-S188")) {
								devicepayload.setLampCheckAtis("Y");
							} else if (sensor.getSensorUuid().getProductCode().equalsIgnoreCase("77-S191")) {
								devicepayload.setLampCheckABS("Y");
							} else if (sensor.getSensorUuid().getProductCode().equalsIgnoreCase("77-S180")) {
								devicepayload.setPctCargoSensor("Y");
							} else if (sensor.getSensorUuid().getProductCode().equalsIgnoreCase("77-S202")) {
								devicepayload.setMicroSpAirTank("Y");
							} else if (sensor.getSensorUuid().getProductCode().equalsIgnoreCase("77-S203")) {
								devicepayload.setMicroSpATISRegulator("Y");
							} else if (sensor.getSensorUuid().getProductCode().equalsIgnoreCase("77-S206")) {
								devicepayload.setMicroSpWiredReceiver("Y");
							} else if (sensor.getSensorUuid().getProductCode().equalsIgnoreCase("77-S177")) {
								if (sensor.getSensorUuid().getProductName().equals("2x2 – MicroSP TPMS Sensors")) {
									devicepayload.setMicroSpTPMS2X2("Y");
								}
								if (sensor.getSensorUuid().getProductName().equals("4x1 – MicroSP TPMS Sensors")) {
									devicepayload.setMicroSpTPMS4X1("Y");
								}
								if (sensor.getSensorUuid().getProductName().equals("4x2 – MicroSP TPMS Sensors")) {
									devicepayload.setMicroSpTPMS4X2("Y");
								}
								if (!AppUtility.isEmpty(deviceDetail.getStatus())
										&& !deviceDetail.getStatus().equals(GatewayStatus.INSTALL_IN_PROGRESS)
										&& !deviceDetail.getStatus().equals(GatewayStatus.INSTALLED)) {
									devicepayload.setMicroSpTPMS("Y");
								} else {
									try {
										Integer count = restUtils.getSensorCount(sensor.getSensorUuid().getUuid());
										devicepayload.setMicroSpTPMS(count != null ? count.toString() : "0");
									} catch (Exception ex) {
										ex.printStackTrace();
									}
								}
							} else if (sensor.getSensorUuid().getProductCode().equalsIgnoreCase("77-S199")) {
								if (!AppUtility.isEmpty(deviceDetail.getStatus())
										&& !deviceDetail.getStatus().equals(GatewayStatus.INSTALL_IN_PROGRESS)
										&& !deviceDetail.getStatus().equals(GatewayStatus.INSTALLED)) {
									devicepayload.setMicroSpTPMSOuter("Y");
								} else {
									Integer count = restUtils.getSensorCount(sensor.getSensorUuid().getUuid());
									devicepayload.setMicroSpTPMSOuter(count != null ? count.toString() : "0");
								}
							} else if (sensor.getSensorUuid().getProductCode().equalsIgnoreCase("77-S198")) {
								if (!AppUtility.isEmpty(deviceDetail.getStatus())
										&& !deviceDetail.getStatus().equals(GatewayStatus.INSTALL_IN_PROGRESS)
										&& !deviceDetail.getStatus().equals(GatewayStatus.INSTALLED)) {
									devicepayload.setMicroSpTPMSInner("Y");
								} else {
									Integer count = restUtils.getSensorCount(sensor.getSensorUuid().getUuid());
									devicepayload.setMicroSpTPMSInner(count != null ? count.toString() : "0");
								}
							} else if (sensor.getSensorUuid().getProductCode().equalsIgnoreCase("77-S111")) {
								devicepayload.setPctCargoCameraG1("Y");
							} else if (sensor.getSensorUuid().getProductCode().equalsIgnoreCase("77-S225")) {
								devicepayload.setPctCargoCameraG2("Y");
							} else if (sensor.getSensorUuid().getProductCode().equalsIgnoreCase("77-S226")) {
								devicepayload.setPctCargoCameraG3("Y");
							}

						}
					}
			}
			devicePayLoadList.add(devicepayload);
		});
		Page<DeviceWithSensorPayload> page = new PageImpl<>(devicePayLoadList, pageable,
				deviceDetails.getTotalElements());
		return page;
	}

	public Page<GatewayPayload> convertGatewayToGatewayPayLoad(Page<Device> deviceDetails, Pageable pageable) {
		List<GatewayPayload> devicePayLoadList = new ArrayList<>();
		deviceDetails.forEach(deviceDetail -> {
			GatewayPayload devicepayload = new GatewayPayload();
			// devicepayload.setImei(deviceDetail.getImei());
			if (deviceDetail.getIotType().getIOTTypeValue().equals(GatewayType.BEACON)) {
				devicepayload.setImei(deviceDetail.getMacAddress());
			} else {
				devicepayload.setImei(deviceDetail.getImei());
			}
			/*
			 * devicepayload.setAppVersion(deviceDetail.getAppVersion());
			 * devicepayload.setBinVersion(deviceDetail.getBinVersion());
			 * devicepayload.setBleVersion(deviceDetail.getBleVersion());
			 */
			devicepayload.setQuantityShipped(deviceDetail.getQuantityShipped());
			if (deviceDetail.getDeviceMaintenanceDetails() != null) {
//				devicepayload.setAppVersion(deviceDetail.getDeviceDetails().getAppVersion());
//				devicepayload.setBinVersion(deviceDetail.getDeviceDetails().getBinVersion());
//				devicepayload.setBleVersion(deviceDetail.getDeviceDetails().getBleVersion());
//				devicepayload.setConfig1(deviceDetail.getDeviceDetails().getConfig1Name());
//				devicepayload.setConfig2(deviceDetail.getDeviceDetails().getConfig2Name());
//				devicepayload.setConfig3(deviceDetail.getDeviceDetails().getConfig3Name());
//				devicepayload.setConfig4(deviceDetail.getDeviceDetails().getConfig4Name());
//				devicepayload.setMcuVersion(deviceDetail.getDeviceDetails().getMcuVersion());
				devicepayload.setAppVersion(deviceDetail.getDeviceMaintenanceDetails().getAppVersion());
				devicepayload.setBinVersion(deviceDetail.getDeviceMaintenanceDetails().getBinVersion());
				devicepayload.setBleVersion(deviceDetail.getDeviceMaintenanceDetails().getBleVersion());
				devicepayload.setConfig1(deviceDetail.getDeviceMaintenanceDetails().getConfig1Name());
				devicepayload.setConfig2(deviceDetail.getDeviceMaintenanceDetails().getConfig2Name());
				devicepayload.setConfig3(deviceDetail.getDeviceMaintenanceDetails().getConfig3Name());
				devicepayload.setConfig4(deviceDetail.getDeviceMaintenanceDetails().getConfig4Name());
				devicepayload.setMcuVersion(deviceDetail.getDeviceMaintenanceDetails().getMcuVersion());
			}
			devicepayload.setProductCode(deviceDetail.getProductCode());
			devicepayload.setProductName(deviceDetail.getProductName());
			devicepayload.setSon(deviceDetail.getSon());
			devicepayload.setEpicorOrderNumber(deviceDetail.getEpicorOrderNumber());
			devicepayload.setCan(deviceDetail.getOrganisation().getAccountNumber());
			devicepayload.setMacAddress(deviceDetail.getMacAddress());
			devicepayload.setCreatedBy(deviceDetail.getOrganisation().getOrganisationName());
			devicepayload.setUpdatedby(deviceDetail.getOrganisation().getOrganisationName());
			devicepayload.setUuid(deviceDetail.getUuid());
			devicepayload.setStatus(deviceDetail.getStatus());
			devicepayload.setType(deviceDetail.getIotType());
			devicePayLoadList.add(devicepayload);
		});
		Page<GatewayPayload> page = new PageImpl<>(devicePayLoadList, pageable, deviceDetails.getTotalElements());
		return page;
	}

	public Page<SensorPayLoad> convertSensorToSensorPayLoad(Page<Device> deviceDetails, Pageable pageable) {
		List<SensorPayLoad> devicePayLoadList = new ArrayList<>();
		deviceDetails.forEach(deviceDetail -> {
			SensorPayLoad devicepayload = new SensorPayLoad();
			devicepayload.setProductCode(deviceDetail.getProductCode());
			devicepayload.setProductName(deviceDetail.getProductName());
			// devicepayload.setSon(deviceDetail.getSon());
			// devicepayload.setEpicorOrderNumber(deviceDetail.getEpicorOrderNumber());
			// devicepayload.setCan(deviceDetail.getOrganisation().getAccountNumber());
			devicepayload.setMacAddress(deviceDetail.getMacAddress());
			devicepayload.setCreatedBy(deviceDetail.getOrganisation().getOrganisationName());
			devicepayload.setUpdatedBy(deviceDetail.getOrganisation().getOrganisationName());
			devicepayload.setUuid(deviceDetail.getUuid());
			devicepayload.setStatus(deviceDetail.getStatus());
			devicepayload.setType(deviceDetail.getIotType());
			devicePayLoadList.add(devicepayload);
		});
		Page<SensorPayLoad> page = new PageImpl<>(devicePayLoadList, pageable, deviceDetails.getTotalElements());
		return page;
	}

	public Page<BeaconPayload> convertBeaconToBeaconPayLoad(Page<Device> deviceDetails, Pageable pageable) {
		List<BeaconPayload> devicePayLoadList = new ArrayList<>();
		deviceDetails.forEach(deviceDetail -> {
			BeaconPayload devicepayload = new BeaconPayload();
			if (deviceDetail.getDeviceDetails() != null) {
				devicepayload.setAppVersion(deviceDetail.getDeviceDetails().getAppVersion());
				devicepayload.setBinVersion(deviceDetail.getDeviceDetails().getBinVersion());
				devicepayload.setBleVersion(deviceDetail.getDeviceDetails().getBleVersion());
				devicepayload.setConfig1(deviceDetail.getDeviceDetails().getConfig1Name());
				devicepayload.setMcuVersion(deviceDetail.getDeviceDetails().getMcuVersion());
//				if (deviceDetail.getDeviceMaintenanceDetails() != null) {
//				devicepayload.setAppVersion(deviceDetail.getDeviceMaintenanceDetails().getAppVersion());
//				devicepayload.setBinVersion(deviceDetail.getDeviceMaintenanceDetails().getBinVersion());
//				devicepayload.setBleVersion(deviceDetail.getDeviceMaintenanceDetails().getBleVersion());
//				devicepayload.setConfig1(deviceDetail.getDeviceMaintenanceDetails().getConfig1Name());
//				devicepayload.setMcuVersion(deviceDetail.getDeviceMaintenanceDetails().getMcuVersion());
			}
			devicepayload.setProductCode(deviceDetail.getProductCode());
			devicepayload.setProductName(deviceDetail.getProductName());
			devicepayload.setSon(deviceDetail.getSon());
			devicepayload.setEpicorOrderNumber(deviceDetail.getEpicorOrderNumber());
			devicepayload.setCan(deviceDetail.getOrganisation().getAccountNumber());
			devicepayload.setMacAddress(deviceDetail.getMacAddress());
			devicepayload.setCreatedBy(deviceDetail.getOrganisation().getOrganisationName());
			devicepayload.setUpdatedBy(deviceDetail.getOrganisation().getOrganisationName());
			devicepayload.setUuid(deviceDetail.getUuid());
			devicepayload.setStatus(deviceDetail.getStatus());
			devicepayload.setType(deviceDetail.getIotType());
			devicePayLoadList.add(devicepayload);
		});
		Page<BeaconPayload> page = new PageImpl<>(devicePayLoadList, pageable, deviceDetails.getTotalElements());
		return page;
	}

	public GatewaySensorPayload convertGatewaySensorToGatewaySensorBean(Device device,
			List<Device_Device_xref> deviceSensorXrefsList) {
		HashMap<String, String> map = new HashMap<String, String>();// Creating HashMap
		map.put("4015R00", "Arrow-L P1.1.4 with Freight P1.0.0");
		map.put("4015R01", "Arrow-L P1.1.4 with Freight P2.0.0");
		map.put("4015R02", "Arrow-L P1.1.4 with Freight P2.0.2");
		map.put("4015R03", "Arrow-L P1.1.4 with Freight P2.0.3/2.0.5");
		map.put("4115R10", "Arrow-LA P2.0.1 with Freight P5.1.0");

		GatewaySensorPayload gatewaySensorBean = new GatewaySensorPayload();
		gatewaySensorBean.setImei(device.getImei());
		gatewaySensorBean.setQuantityShipped(device.getQuantityShipped());

		if (device.getDeviceDetails() != null) {
			gatewaySensorBean.setAppVersion(device.getDeviceDetails().getAppVersion());
			gatewaySensorBean.setBinVersion(device.getDeviceDetails().getBinVersion());
			gatewaySensorBean.setBleVersion(device.getDeviceDetails().getBleVersion());
			gatewaySensorBean.setConfig1(device.getDeviceDetails().getConfig1Name());
			gatewaySensorBean.setConfig2(device.getDeviceDetails().getConfig2Name());
			gatewaySensorBean.setConfig3(device.getDeviceDetails().getConfig3Name());
			gatewaySensorBean.setConfig4(device.getDeviceDetails().getConfig4Name());
			gatewaySensorBean.setMcuVersion(device.getDeviceDetails().getMcuVersion());
//			if (device.getDeviceMaintenanceDetails() != null) {
//			gatewaySensorBean.setAppVersion(device.getDeviceMaintenanceDetails().getAppVersion());
//			gatewaySensorBean.setBinVersion(device.getDeviceMaintenanceDetails().getBinVersion());
//			gatewaySensorBean.setBleVersion(device.getDeviceMaintenanceDetails().getBleVersion());
//			gatewaySensorBean.setConfig1(device.getDeviceMaintenanceDetails().getConfig1Name());
//			gatewaySensorBean.setConfig2(device.getDeviceMaintenanceDetails().getConfig2Name());
//			gatewaySensorBean.setConfig3(device.getDeviceMaintenanceDetails().getConfig3Name());
//			gatewaySensorBean.setConfig4(device.getDeviceMaintenanceDetails().getConfig4Name());
//			gatewaySensorBean.setMcuVersion(device.getDeviceMaintenanceDetails().getMcuVersion());
//			gatewaySensorBean.setUsage(device.getDeviceDetails().getUsageStatus());
		}
		gatewaySensorBean.setProductCode(device.getProductCode());
		gatewaySensorBean.setProductName(device.getProductName());
		gatewaySensorBean.setSon(device.getSon());
		gatewaySensorBean.setEpicorOrderNumber(device.getEpicorOrderNumber());
		gatewaySensorBean.setCan(device.getOrganisation().getAccountNumber());
		gatewaySensorBean.setMacAddress(device.getMacAddress());
		gatewaySensorBean.setCreatedBy(device.getOrganisation().getOrganisationName());
		gatewaySensorBean.setUpdatedby(device.getOrganisation().getOrganisationName());
		gatewaySensorBean.setUuid(device.getUuid());
		gatewaySensorBean.setStatus(device.getStatus());
		gatewaySensorBean.setType(device.getIotType());
		gatewaySensorBean.setUsage(device.getUsageStatus());
		String map1 = deviceRedisRepo.getMap(Constants.DEVICE_360 + device.getImei());
		if (map1 != null) {
			gatewaySensorBean.setRedisData(map1);
			String hardwareTypeKey = "4015R00";
			String hardwareType = map.get(hardwareTypeKey);
			gatewaySensorBean.setHardwareId(hardwareTypeKey);
			gatewaySensorBean.setHardwareType(hardwareType);
		}
		List<String> values = deviceRedisRepo
				.findValuesForDevice(Constants.DEVICE_CURRENT_VIEW_PREFIX + device.getImei(), DEVICE_INFO_FIELDS);
		if (values != null && !values.isEmpty()) {
			Lookup lookup = lookupRepository.findByFieldId(values.get(5));
			gatewaySensorBean.setLastReportDate(values.get(4));
			if (lookup != null && lookup.getValue() != null) {
				gatewaySensorBean.setLastEvent(lookup.getValue());
			}
		}
		CellularPayload cellularPayload = new CellularPayload();
		if (device.getCellular() != null) {
			cellularPayload.setCarrierId(device.getCellular().getCarrierId());
			cellularPayload.setCountryCode(device.getCellular().getCountryCode());
			cellularPayload.setIccid(device.getCellular().getIccid());
			cellularPayload.setImsi(device.getCellular().getImsi());
			cellularPayload.setImei(device.getCellular().getImei());
			cellularPayload.setPhone(device.getCellular().getPhone());
			cellularPayload.setServiceCountry(device.getCellular().getServiceCountry());
			cellularPayload.setServiceNetwork(device.getCellular().getServiceNetwork());
			cellularPayload.setCellular(device.getCellular().getCellular());
			cellularPayload.setUuid(device.getCellular().getUuid());
		}
		gatewaySensorBean.setCellularPayload(cellularPayload);

		List<SensorPayLoad> sensorBeanList = new ArrayList<>();
		deviceSensorXrefsList.forEach(deviceSensorXref -> {
			Device sensor = deviceSensorXref.getSensorUuid();
			SensorPayLoad sensorPayload = new SensorPayLoad();
			sensorPayload.setProductCode(sensor.getProductCode());
			sensorPayload.setProductName(sensor.getProductName());
			// sensorPayload.setSon(sensor.getSon());
			// sensorPayload.setEpicorOrderNumber(sensor.getEpicorOrderNumber());
			// sensorPayload.setCan(sensor.getOrganisation().getAccountNumber());
			sensorPayload.setMacAddress(sensor.getMacAddress());
			sensorPayload.setCreatedBy(sensor.getOrganisation().getOrganisationName());
			sensorPayload.setUpdatedBy(sensor.getOrganisation().getOrganisationName());
			sensorPayload.setUuid(sensor.getUuid());
			sensorPayload.setStatus(sensor.getStatus());
			sensorPayload.setType(sensor.getIotType());
			List<SensorSubDetailPayload> sensorSubDetailPayloadList = new ArrayList<SensorSubDetailPayload>();
			for (SensorDetail sensorDetail : sensor.getSensorDetail()) {
				SensorSubDetailPayload sensorSubDetailPayload = new SensorSubDetailPayload();
				sensorSubDetailPayload.setPosition(sensorDetail.getPosition());
				sensorSubDetailPayload.setType(sensorDetail.getType());
				sensorSubDetailPayload.setSensorId(sensorDetail.getSensorId());
				sensorSubDetailPayload.setUuid(sensorDetail.getUuid());
				sensorSubDetailPayload.setSensorUUID(sensorDetail.getSensorUUID().getUuid());
				sensorSubDetailPayloadList.add(sensorSubDetailPayload);
			}
			sensorPayload.setSensorSubDetail(sensorSubDetailPayloadList);
			sensorBeanList.add(sensorPayload);
		});
		gatewaySensorBean.setSensorPayload(sensorBeanList);
		return gatewaySensorBean;
	}

	public DeviceCommandBean convertDeviceCommandToDeviceCommand(DeviceCommand device) {
		DeviceCommandBean deviceCommandBean = new DeviceCommandBean();
		try {
			deviceCommandBean.setAtCommand(device.getAt_command());
			deviceCommandBean.setCreatedDate(device.getCreated_date().toString());
			deviceCommandBean.setDeviceId(device.getDevice_id());
			deviceCommandBean.setSource(device.getSource());
			deviceCommandBean.setSuccess(device.isSuccess());
			deviceCommandBean.setSource(device.getSource());
			deviceCommandBean.setUuid(device.getUuid());
			deviceCommandBean.setPriority(Integer.parseInt(device.getPriority()));
			if (device.getDevice_response() != null)
				deviceCommandBean.setDeviceResponse(device.getDevice_response());
			if (device.getStatus() != null)
				deviceCommandBean.setStatus(device.getStatus());
			deviceCommandBean.setRetryCount(device.getRetry_count());
		} catch (Exception e) {
			e.getMessage();
		}
		return deviceCommandBean;
	}

	// public DeviceForwarding
	// convertDeviceForwardingRequestToDeviceForwarding(DeviceForwardingRequest
	// request) {
	// DeviceForwarding response = new DeviceForwarding();
	// response.setUuid(request.getUuid());
	// response.setDevice(request.getDevice());
	// response.setType(request.getType());
	// response.setUrl(request.getUrl());
	// return response;
	// }

	public List<DeviceForwardingResponse> convertDeviceForwardingToDeviceForwardingResponse(List<DeviceForwarding> al) {
		List<DeviceForwardingResponse> list = new ArrayList<DeviceForwardingResponse>();
		if (al != null && !al.isEmpty()) {
			al.forEach(pm -> {
				DeviceForwardingResponse response = new DeviceForwardingResponse();
				response.setId(pm.getId());
				response.setUuid(pm.getUuid());
				response.setType(pm.getType());
				response.setUrl(pm.getUrl());
				response.setForwardingRuleUrlUuid(pm.getForwardingRuleUrlUuid());
				Device device = new Device();
				if (pm.getDevice() != null) {
					device.setUuid(pm.getDevice().getUuid());
					device.setImei(pm.getDevice().getImei());
				}
				response.setDevice(device);
				list.add(response);
			});
		}
		return list;
	}

	public List<DeviceQaResponse> convertDeviceQAToDeviceQAResponse(List<DeviceQa> al) {
		List<DeviceQaResponse> list = new ArrayList<DeviceQaResponse>();
		if (al != null && !al.isEmpty()) {
			al.forEach(pm -> {
				DeviceQaResponse response = new DeviceQaResponse();
				response.setQaDate(pm.getQaDate());
				response.setQaResult(pm.getQaResult());
				response.setQaStatus(pm.getQaStatus());
				if (pm.getDeviceId().getImei() != null) {
					response.setDeviceId(pm.getDeviceId().getImei());
				}
				list.add(response);
			});
		}
		return list;
	}

	public DeviceQa convertDeviceQaRequestToDeviceQa(DeviceQaRequest request) {
		DeviceQa response = new DeviceQa();

		response.setQaResult(request.getQaResult());
		response.setQaStatus(request.getQaStatus());
		response.setUuid(request.getUuid());
		response.setQaDate(request.getQaDate());
		return response;
	}

	public AssetsStatusPayload convertSensorDetailToAssetsStatusBean(Device device,
			List<Device_Device_xref> deviceSensorXrefsList, Asset_Device_xref assetsDeviceXref, User user) {
		AssetsStatusPayload assetsStatusBean = new AssetsStatusPayload();
		assetsStatusBean.setImei(device.getImei());
		// assetsStatusBean.setQuantityShipped(device.getQuantityShipped());
		if (device.getDeviceDetails() != null) {
			// assetsStatusBean.setAppVersion(device.getDeviceDetails().getAppVersion());
			// assetsStatusBean.setBinVersion(device.getDeviceDetails().getBinVersion());
			// assetsStatusBean.setBleVersion(device.getDeviceDetails().getBleVersion());
			// assetsStatusBean.setConfig1(device.getDeviceDetails().getConfig1Name());
			// assetsStatusBean.setConfig2(device.getDeviceDetails().getConfig2Name());
			// assetsStatusBean.setConfig3(device.getDeviceDetails().getConfig3Name());
			// assetsStatusBean.setConfig4(device.getDeviceDetails().getConfig4Name());
			// assetsStatusBean.setMcuVersion(device.getDeviceDetails().getMcuVersion());
			assetsStatusBean.setInstalledStatusFlag(device.getDeviceDetails().getInstalledStatusFlag());
			assetsStatusBean.setHwIdVersion(device.getDeviceDetails().getHwIdVersion());
			assetsStatusBean.setHwVersionRevision(device.getDeviceDetails().getHwVersionRevision());
			assetsStatusBean.setUsage(device.getUsageStatus());
		}
		assetsStatusBean.setProductCode(device.getProductCode());
		assetsStatusBean.setProductName(device.getProductName());
		// assetsStatusBean.setSon(device.getSon());
		assetsStatusBean.setEpicorOrderNumber(device.getEpicorOrderNumber());
		// assetsStatusBean.setMacAddress(device.getMacAddress());
		if (device.getOrganisation() != null) {
			assetsStatusBean.setCan(device.getOrganisation().getAccountNumber());
			assetsStatusBean.setCreatedBy(device.getOrganisation().getOrganisationName());
			assetsStatusBean.setUpdatedby(device.getOrganisation().getOrganisationName());
		}
		if (device.getPurchaseBy() != null) {
			assetsStatusBean.setPurchaseBy(device.getPurchaseBy().getAccountNumber());
		}
		if (device.getInstalledBy() != null) {
			assetsStatusBean.setInstalledBy(device.getInstalledBy().getAccountNumber());
		}
		if (device.getDeviceDetails() != null) {
			assetsStatusBean.setHardwareId(device.getDeviceDetails().getHardwareId());
			assetsStatusBean.setHardwareType(device.getDeviceDetails().getHardwareType());
		}

		assetsStatusBean.setUuid(device.getUuid());
		assetsStatusBean.setStatus(device.getStatus());
		assetsStatusBean.setType(device.getIotType());
		assetsStatusBean.setUsage(device.getUsageStatus());
		assetsStatusBean.setRetriveStatus(device.getRetriveStatus());
		assetsStatusBean.setIsActive(device.getIsActive());
		assetsStatusBean.setDeviceType(device.getDeviceType());

		try {
			JSONObject jsonObject = getLatestReportforDeviceId(device.getImei(), false);
			if (jsonObject != null) {
				JSONObject reportHeader = AppUtility.checkJSONObjectNullKey(jsonObject, "report_header");
				JSONObject general = AppUtility.checkJSONObjectNullKey(jsonObject, "general");
				JSONObject generalMaskFields = AppUtility.checkJSONObjectNullKey(jsonObject, "general_mask_fields");
				assetsStatusBean.setLastReportDate(
						generalMaskFields != null ? AppUtility.checkNullKey(generalMaskFields, "received_time_stamp")
								: null);

				assetsStatusBean
						.setLastRawReport(general != null ? AppUtility.checkNullKey(general, "rawreport") : null);
				int eventId = (reportHeader != null ? AppUtility.checkIntNullKey(reportHeader, "event_id") : 0);
				if (eventId > 0) {
					Event event = eventRepository.findByEventId(eventId);
					assetsStatusBean.setLastEvent(event.getEventType());
				}
				JSONObject jsonObject2 = getLatestReportforDeviceId(device.getImei(), true);
				if (jsonObject2 != null) {
					JSONObject generalMaskFields2 = AppUtility.checkJSONObjectNullKey(jsonObject2,
							"general_mask_fields");
					assetsStatusBean.setLastMaintenanceReportDate(generalMaskFields2 != null
							? AppUtility.checkNullKey(generalMaskFields2, "received_time_stamp")
							: null);
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//
		// JSONObject reportHeader = AppUtility.checkJSONObjectNullKey(jsonObject,
		// "report_header");
		// JSONObject general = AppUtility.checkJSONObjectNullKey(jsonObject,
		// "general");
		// List<String> values = deviceRedisRepo
		// .findValuesForDevice(Constants.DEVICE_CURRENT_VIEW_PREFIX + device.getImei(),
		// DEVICE_INFO_FIELDS);
		// if (values != null && !values.isEmpty()) {
		// LOGGER.info("values are printing for device " + values.toString());
		// List<Lookup> lookup = lookupRepository.findByField(values.get(5));
		// assetsStatusBean.setLastReportDate(values.get(4));
		// assetsStatusBean.setLastMaintenanceReportDate(values.get(2));
		// assetsStatusBean.setLastRawReport(values.get(6));
		// String uuid = values.get(3);
		//
		// if (lookup.size() > 0) {
		// assetsStatusBean.setLastEvent(lookup.get(0).getValue());
		// }
		// }

		CellularPayload cellularPayload = new CellularPayload();
		if (device.getCellular() != null) {
			cellularPayload.setCarrierId(device.getCellular().getCarrierId());
			cellularPayload.setCountryCode(device.getCellular().getCountryCode());
			cellularPayload.setIccid(device.getCellular().getIccid());
			cellularPayload.setImsi(device.getCellular().getImsi());
			cellularPayload.setImei(device.getCellular().getImei());
			cellularPayload.setPhone(device.getCellular().getPhone());
			cellularPayload.setServiceCountry(device.getCellular().getServiceCountry());
			cellularPayload.setServiceNetwork(device.getCellular().getServiceNetwork());
			cellularPayload.setCellular(device.getCellular().getCellular());
			cellularPayload.setUuid(device.getCellular().getUuid());
		}
		assetsStatusBean.setCellularPayload(cellularPayload);
		boolean callCenterRoleAvailable = false;
		for (Role roles : user.getRole()) {
			if (roles.getName().equalsIgnoreCase(AuthoritiesConstants.ROLE_CALL_CENTER)) {
				callCenterRoleAvailable = true;
				break;
			}
		}
		final boolean callCenterRoleAvailable2 = callCenterRoleAvailable;
		List<SensorPayLoad> sensorBeanList = new ArrayList<>();
		List<SensorPayLoad> sensorBeanListOfTPMS = new ArrayList<>();
		deviceSensorXrefsList.forEach(deviceSensorXref -> {
			Device sensor = deviceSensorXref.getSensorUuid();
			SensorPayLoad sensorPayload = new SensorPayLoad();
			sensorPayload.setProductCode(sensor.getProductCode());
			sensorPayload.setProductName(sensor.getProductName());
			// sensorPayload.setSon(sensor.getSon());
			// sensorPayload.setEpicorOrderNumber(sensor.getEpicorOrderNumber());
			sensorPayload.setMacAddress(sensor.getMacAddress());
			if (sensor.getOrganisation() != null) {
				// sensorPayload.setCan(sensor.getOrganisation().getAccountNumber());
				sensorPayload.setCreatedBy(sensor.getOrganisation().getOrganisationName());
				sensorPayload.setUpdatedBy(sensor.getOrganisation().getOrganisationName());
			}
			sensorPayload.setUuid(sensor.getUuid());
			sensorPayload.setStatus(sensor.getStatus());
			sensorPayload.setType(sensor.getIotType());
			Map<String, String> colorMapForLiteSentry = new HashMap<>();
			colorMapForLiteSentry.put("1", "RED");
			colorMapForLiteSentry.put("2", "GREEN");
			colorMapForLiteSentry.put("3", "YELLOW");
			colorMapForLiteSentry.put("4", "BROWN");
			colorMapForLiteSentry.put("5", "BLACK");
			List<SensorSubDetailPayload> sensorSubDetailPayloadList = new ArrayList<SensorSubDetailPayload>();

			for (SensorDetail sensorDetail : sensor.getSensorDetail()) {
				SensorSubDetailPayload sensorSubDetailPayload = new SensorSubDetailPayload();
				if (sensorDetail.getPosition() != null && !sensorDetail.getPosition().equalsIgnoreCase("Default")
						&& sensor.getProductCode() != null && sensor.getProductCode().equalsIgnoreCase("77-S107")) {
					String colorName = colorMapForLiteSentry.get(sensorDetail.getPosition());
					if (colorName != null) {
						sensorSubDetailPayload.setPosition(colorName);
						sensorSubDetailPayload.setSensorPressure(sensorDetail.getSensorPressure());
						sensorSubDetailPayload.setSensorTemperature(sensorDetail.getSensorTemperature());
					} else {
						sensorSubDetailPayload.setPosition("--");
						sensorSubDetailPayload.setSensorPressure("--");
						sensorSubDetailPayload.setSensorTemperature("--");
					}
				} else {
					sensorSubDetailPayload.setPosition(sensorDetail.getPosition());
					sensorSubDetailPayload.setSensorPressure(sensorDetail.getSensorPressure());
					sensorSubDetailPayload.setSensorTemperature(sensorDetail.getSensorTemperature());
				}

				sensorSubDetailPayload.setType(sensorDetail.getType());
				sensorSubDetailPayload.setSensorId(sensorDetail.getSensorId());
				sensorSubDetailPayload.setUuid(sensorDetail.getUuid());
				sensorSubDetailPayload.setSensorUUID(sensorDetail.getSensorUUID().getUuid());
				sensorSubDetailPayload.setStatus(sensorDetail.getStatus());
				sensorSubDetailPayload.setVendor(sensorDetail.getVendor());
				sensorSubDetailPayload.setAtCommandStatus(sensorDetail.getAtCommandStatus());
				sensorSubDetailPayload.setAtCommandUuid(sensorDetail.getAtCommandUuid());
				sensorSubDetailPayload.setNewSensorId(sensorDetail.getNewSensorId());
				sensorSubDetailPayload.setUpdatedOn(sensorDetail.getUpdatedOn());
				sensorSubDetailPayload.setCreatedOn(sensorDetail.getCreatedOn());
				sensorSubDetailPayloadList.add(sensorSubDetailPayload);

			}
			if (sensor.getProductCode() != null && sensor.getProductCode().equalsIgnoreCase("77-S107")) {
				if (sensorSubDetailPayloadList != null && sensorSubDetailPayloadList.size() > 0) {
					List<SensorSubDetailPayload> subDetailPayloads = createNewDummyRecordForLiteSentry(
							sensorSubDetailPayloadList);
					sensorPayload.setSensorSubDetail(subDetailPayloads);
				}
			} else {
				sensorPayload.setSensorSubDetail(sensorSubDetailPayloadList);
			}
			if (callCenterRoleAvailable2 && (sensor.getProductCode().equalsIgnoreCase("77-S177")
					|| sensor.getProductCode().equalsIgnoreCase("77-S500"))) {
				if (sensor.getProductCode().equalsIgnoreCase("77-S177")) {
					sensorBeanListOfTPMS.add(sensorPayload);
				} else {
					sensorBeanList.add(sensorPayload);
				}

			} else if (!callCenterRoleAvailable2) {
				if (sensor.getProductCode().equalsIgnoreCase("77-S177")) {
					sensorBeanListOfTPMS.add(sensorPayload);
				} else {
					sensorBeanList.add(sensorPayload);
				}
			}
		});
		AssetsDetailPayload assetsDetailPayload = new AssetsDetailPayload();

		if (device.getOrganisation() != null) {
			assetsDetailPayload.setCompanyName(device.getOrganisation().getOrganisationName());

		}
		if (assetsDeviceXref != null && assetsDeviceXref.getAsset() != null) {
			assetsDetailPayload.setAssetUniqId(assetsDeviceXref.getAsset().getId());
			assetsDetailPayload.setAssetID(assetsDeviceXref.getAsset().getAssignedName());
			if (assetsDeviceXref.getAsset().getCategory() != null) {
				assetsDetailPayload.setAssetType(assetsDeviceXref.getAsset().getCategory().toString());
			}
			assetsDetailPayload.setVin(assetsDeviceXref.getAsset().getVin());
			assetsDetailPayload.setYear(assetsDeviceXref.getAsset().getYear());
			assetsDetailPayload.setAssetName(assetsDeviceXref.getAsset().getAssignedName());
			assetsDetailPayload.setInstalledDate(assetsDeviceXref.getDateCreated());
			if (assetsDeviceXref.getAsset().getManufacturerDetails() != null) {
				assetsDetailPayload.setModel(assetsDeviceXref.getAsset().getManufacturerDetails().getModel());
			}
			if (assetsDeviceXref.getAsset().getManufacturer() != null) {
				assetsDetailPayload.setManufacturerName(assetsDeviceXref.getAsset().getManufacturer().getName());
			}
			assetsDetailPayload.setNoOfTires(assetsDeviceXref.getAsset().getNoOfTires());
			assetsDetailPayload.setNoOfAxel(assetsDeviceXref.getAsset().getNoOfAxles());
			assetsDetailPayload.setAssetNickName(assetsDeviceXref.getAsset().getAssetNickName());
			assetsDetailPayload.setExternalLength(assetsDeviceXref.getAsset().getExternalLength());
			assetsDetailPayload.setDoorType(assetsDeviceXref.getAsset().getDoorType());
			assetsDetailPayload.setTag(assetsDeviceXref.getAsset().getTag());
		}
		String map1 = deviceRedisRepo.getMap(Constants.DEVICE_360 + device.getImei());
		if (map1 != null) {
			assetsStatusBean.setRedisData(map1);
		}
		assetsStatusBean.setAssetsDetailPayload(assetsDetailPayload);
		if (sensorBeanListOfTPMS != null && sensorBeanListOfTPMS.size() > 0) {
			SensorPayLoad sensorPayLoad = createNewDummyRecordForTPMSSensor(sensorBeanListOfTPMS);
			sensorBeanList.add(sensorPayLoad);
		}
		assetsStatusBean.setSensorPayload(sensorBeanList);
		return assetsStatusBean;
	}

	private List<SensorSubDetailPayload> createNewDummyRecordForLiteSentry(
			List<SensorSubDetailPayload> sensorSubDetailPayloadList) {
		List<SensorSubDetailPayload> subDetailPayloads = new ArrayList<SensorSubDetailPayload>();
		for (int i = 0; i < 5; i++) {
			SensorSubDetailPayload subDetailPayload = new SensorSubDetailPayload();
			BeanUtils.copyProperties(sensorSubDetailPayloadList.get(0), subDetailPayload);
			if (i == 0) {
				subDetailPayload.setPosition("RED");
			} else if (i == 1) {
				subDetailPayload.setPosition("GREEN");
			} else if (i == 2) {
				subDetailPayload.setPosition("YELLOW");
			} else if (i == 3) {
				subDetailPayload.setPosition("BROWN");
			} else if (i == 4) {
				subDetailPayload.setPosition("BLACK");
			}
			subDetailPayload.setSensorPressure("--");
			subDetailPayload.setSensorTemperature("--");
			subDetailPayloads.add(subDetailPayload);

		}

		for (SensorSubDetailPayload sensorSubDetailPayload : sensorSubDetailPayloadList) {
			subDetailPayloads
					.removeIf(filter -> filter.getPosition().equalsIgnoreCase(sensorSubDetailPayload.getPosition()));
			if (sensorSubDetailPayload.getPosition() != null) {
				subDetailPayloads.add(sensorSubDetailPayload);
			}
		}

		return subDetailPayloads;
	}

	private SensorPayLoad createNewDummyRecordForTPMSSensor(List<SensorPayLoad> sensorBeanListOfTPMS) {
		Map<String, String> positionMap = new HashMap<>();
		positionMap.put("0x21", "LOF-1");
		positionMap.put("0x22", "LIF-2");
		positionMap.put("0x23", "RIF-3");
		positionMap.put("0x24", "ROF-4");
		positionMap.put("0x25", "LOR-5");
		positionMap.put("0x26", "LIR-6");
		positionMap.put("0x27", "RIR-7");
		positionMap.put("0x28", "ROR-8");
		SensorPayLoad sensorPayLoad = new SensorPayLoad();
		if (sensorBeanListOfTPMS != null && sensorBeanListOfTPMS.size() > 0) {
			List<SensorSubDetailPayload> subDetailPayloads = new ArrayList<SensorSubDetailPayload>();

			for (int i = 0; i < 8; i++) {

				SensorSubDetailPayload sensorSubDetailPayload = new SensorSubDetailPayload();

				sensorPayLoad.setProductName(sensorBeanListOfTPMS.get(0).getProductName());
				sensorPayLoad.setProductCode(sensorBeanListOfTPMS.get(0).getProductCode());
				sensorPayLoad.setUuid(sensorBeanListOfTPMS.get(0).getUuid());
				sensorSubDetailPayload.setSensorUUID("--");
				sensorSubDetailPayload.setType("--");
				if (i == 0) {
					sensorSubDetailPayload.setPosition("LOF-1");
				} else if (i == 1) {
					sensorSubDetailPayload.setPosition("LIF-2");
				} else if (i == 2) {
					sensorSubDetailPayload.setPosition("RIF-3");
				} else if (i == 3) {
					sensorSubDetailPayload.setPosition("ROF-4");
				} else if (i == 4) {
					sensorSubDetailPayload.setPosition("LOR-5");
				} else if (i == 5) {
					sensorSubDetailPayload.setPosition("LIR-6");
				} else if (i == 6) {
					sensorSubDetailPayload.setPosition("RIR-7");
				} else if (i == 7) {
					sensorSubDetailPayload.setPosition("ROR-8");
				}
				sensorSubDetailPayload.setSensorPressure("--");
				sensorSubDetailPayload.setSensorTemperature("--");
				sensorSubDetailPayload.setStatus("PENDING");
				subDetailPayloads.add(sensorSubDetailPayload);

			}
			for (SensorPayLoad sensorPayLoad1 : sensorBeanListOfTPMS) {
				if (sensorPayLoad1.getSensorSubDetail() != null && sensorPayLoad1.getSensorSubDetail().size() > 0) {
					for (SensorSubDetailPayload sensorSubDetailPayload : sensorPayLoad1.getSensorSubDetail()) {
						String position = positionMap.get(sensorSubDetailPayload.getPosition());
						sensorSubDetailPayload.setPosition(position);
						subDetailPayloads.removeIf(
								filter -> filter.getPosition().equalsIgnoreCase(sensorSubDetailPayload.getPosition()));
					}

				}

			}
			for (SensorPayLoad sensorPayLoad1 : sensorBeanListOfTPMS) {
				if (sensorPayLoad1.getSensorSubDetail() != null && sensorPayLoad1.getSensorSubDetail().size() > 0) {
					subDetailPayloads.addAll(sensorPayLoad1.getSensorSubDetail());

				}

			}
			sensorPayLoad.setSensorSubDetail(subDetailPayloads);
		}
		return sensorPayLoad;
	}

	public Device gatewayPayloadTogateway(Map<String, Object> gateway,
			GatewayBulkUploadRequest gatewayBulkUploadRequest, User user, Organisation company) {
		Device device = new Device();
		device.setLastPerformAction(Constants.GATEWAYS_ACTION_ADDED);
		device.setTimeOfLastDownload(Instant.now());
		device.setOrganisation(company);
		// device.setProductCode(gatewayBulkUploadRequest.getProductMasterResponse().getProduct_code());
		device.setProductCode(gatewayBulkUploadRequest.getProductCode());
		device.setProductName(gatewayBulkUploadRequest.getProductMasterResponse().getProduct_name());
		device.setSon(gatewayBulkUploadRequest.getSalesforceOrderId());
		// device.setUsageStatus(gatewayBulkUploadRequest.getUsageStatus());
		device.setStatus(DeviceStatus.PENDING);
		device.setCreatedAt(Instant.now());
		boolean isDeviceUuidUnique = false;
		String deviceUuid = "";
		while (!isDeviceUuidUnique) {
			deviceUuid = UUID.randomUUID().toString();
			Device byUuid = deviceRepository.findByUuid(deviceUuid);
			if (byUuid == null) {
				isDeviceUuidUnique = true;
			}
		}

		device.setCreatedBy(user);
		device.setUuid(deviceUuid);
		Cellular cellular = addCellular(gateway, gatewayBulkUploadRequest);
		device.setCellular(cellular);
		if (gatewayBulkUploadRequest.getIsAllMacAddress()) {
			device.setMacAddress(String.valueOf(gateway.get("imei")));
			device.setIotType(IOTType.BEACON);
		} else if (gatewayBulkUploadRequest.getIsAllImei()) {
			String imei = String.valueOf(gateway.get("imei")).length() == 14 ? "0" + String.valueOf(gateway.get("imei"))
					: String.valueOf(gateway.get("imei"));
			device.setImei(imei);
			device.setIotType(IOTType.GATEWAY);
		}
		return device;
	}

	public Cellular addCellular(Map<String, Object> device, GatewayBulkUploadRequest gatewayBulkUploadRequest) {
		Cellular cellular = new Cellular();
		cellular.setCellular(String.valueOf(device.get("sim")));
		cellular.setImei(String.valueOf(device.get("imei")));
		cellular.setPhone(String.valueOf(device.get("phone")));
		cellular.setServiceNetwork(gatewayBulkUploadRequest.getTelecomProvider());
		boolean isCellularUuidUnique = false;
		String cellularUuid = "";
		while (!isCellularUuidUnique) {
			cellularUuid = UUID.randomUUID().toString();
			Cellular byUuid = cellularRepository.findByUuid(cellularUuid);
			if (byUuid == null) {
				isCellularUuidUnique = true;
			}
		}
		cellular.setUuid(cellularUuid);
		return cellularRepository.save(cellular);
	}

	public Page<GatewaySummaryPayload> convertGatewaySummaryToGatewaySummaryPayload(
			Page<GatewaySummary> customerGatewaySummary, Pageable pageable) {
		List<GatewaySummaryPayload> gatewaySummaryPayloadList = new ArrayList<>();

		customerGatewaySummary.forEach(customerGatewaySummarys -> {
			GatewaySummaryPayload gatewaySummaryPayload = new GatewaySummaryPayload();
			gatewaySummaryPayload.setOrganisationId(customerGatewaySummarys.getOrganisationId());
			gatewaySummaryPayload.setOrganisationName(customerGatewaySummarys.getOrganisationName());
			gatewaySummaryPayload.setTraillerNet(customerGatewaySummarys.getTraillerNet());
			gatewaySummaryPayload.setSmartPair(customerGatewaySummarys.getSmartPair());
			gatewaySummaryPayload.setSmartSeven(customerGatewaySummarys.getSmartSeven());
			gatewaySummaryPayload.setSabre(customerGatewaySummarys.getSabre());
			gatewaySummaryPayload.setStealthNet(customerGatewaySummarys.getStealthNet());
			gatewaySummaryPayload.setFreightLa(customerGatewaySummarys.getFreightLa());
			gatewaySummaryPayload.setFreightL(customerGatewaySummarys.getFreightL());
			gatewaySummaryPayload.setArrowL(customerGatewaySummarys.getArrowL());
			gatewaySummaryPayload.setCutlassL(customerGatewaySummarys.getCutlassL());
			gatewaySummaryPayload.setDagger67Lg(customerGatewaySummarys.getDagger67Lg());
			gatewaySummaryPayload.setSmart7(customerGatewaySummarys.getSmart7());
			gatewaySummaryPayload.setKatanaH(customerGatewaySummarys.getKatanaH());
			gatewaySummaryPayloadList.add(gatewaySummaryPayload);
		});
		Page<GatewaySummaryPayload> page = new PageImpl<>(gatewaySummaryPayloadList, pageable,
				customerGatewaySummary.getTotalElements());

		return page;
	}

	public Asset assetsPayloadToAssets(AssetDeviceAssociationPayLoadForIA assetsPayload, Organisation company,
			Boolean isCreated, User user, Map<String, List<String>> validationFailedAssets) {

		LOGGER.info("Inside assetsPayloadToAssets method and fetch AssetsPayload " + assetsPayload);

		Asset asset = new Asset();
		asset.setAssignedName(assetsPayload.getAssetId());
		asset.setCategory(AssetCategory.getAssetCategory(assetsPayload.getCategory().toUpperCase()));
		asset.setGatewayEligibility(assetsPayload.getEligibleGateway());
		if (assetsPayload.getManufacturer() != null && !assetsPayload.getManufacturer().isEmpty()) {
			Manufacturer manufacturer = manufacturerRepository.findByName(assetsPayload.getManufacturer());
			if (manufacturer != null) {
				asset.setManufacturer(manufacturer);
			} else {
				if (validationFailedAssets != null) {
				}
			}
		}
		asset.setYear(assetsPayload.getYear());
		asset.setVin(assetsPayload.getVin());
		asset.setCreationMethod(AssetCreationMethod.MANUAL);
		if (assetsPayload.getStatus() == null) {
			asset.setStatus(AssetStatus.PENDING);
		} else {
			asset.setStatus(AssetStatus.getAssetStatus(assetsPayload.getStatus()));
		}
		asset.setOrganisation(company);

		boolean isAssetUuidUnique = false;
		String assetUuid = "";
		while (!isAssetUuidUnique) {
			assetUuid = UUID.randomUUID().toString();
			Asset byUuid = assetRepository.findByUuid(assetUuid);
			if (byUuid == null) {
				isAssetUuidUnique = true;
			}
		}
		asset.setUuid(assetUuid);
		if (isCreated) {
			asset.setCreatedBy(user);
		}
		asset.setUpdatedBy(user);
		return asset;
	}

	public Device gatewayPayloadTogateway(Map<String, Object> gateway,
			GatewayBulkUploadRequestWithDeviceForwarding gatewayBulkUploadRequestWithDeviceForwarding, User user,
			Organisation company) {
		Device device = new Device();
		device.setLastPerformAction(Constants.GATEWAYS_ACTION_ADDED);
		device.setTimeOfLastDownload(Instant.now());
		device.setOrganisation(company);
		// device.setProductCode(gatewayBulkUploadRequest.getProductMasterResponse().getProduct_code());
		device.setProductCode(gatewayBulkUploadRequestWithDeviceForwarding.getProductCode());
		device.setProductName(
				gatewayBulkUploadRequestWithDeviceForwarding.getProductMasterResponse().getProduct_name());
		device.setSon(gatewayBulkUploadRequestWithDeviceForwarding.getSalesforceOrderId());
		// device.setUsageStatus(gatewayBulkUploadRequestWithDeviceForwarding.getUsageStatus());
		device.setStatus(DeviceStatus.PENDING);
		device.setCreatedAt(Instant.now());
		boolean isDeviceUuidUnique = false;
		String deviceUuid = "";
		while (!isDeviceUuidUnique) {
			deviceUuid = UUID.randomUUID().toString();
			Device byUuid = deviceRepository.findByUuid(deviceUuid);
			if (byUuid == null) {
				isDeviceUuidUnique = true;
			}
		}

		device.setCreatedBy(user);
		device.setUuid(deviceUuid);
		Cellular cellular = addCellular(gateway, gatewayBulkUploadRequestWithDeviceForwarding);
		device.setCellular(cellular);
		if (gatewayBulkUploadRequestWithDeviceForwarding.getIsAllMacAddress()) {
			device.setMacAddress(String.valueOf(gateway.get("imei")));
			device.setIotType(IOTType.BEACON);
		} else if (gatewayBulkUploadRequestWithDeviceForwarding.getIsAllImei()) {
			String imei = String.valueOf(gateway.get("imei")).length() == 14 ? "0" + String.valueOf(gateway.get("imei"))
					: String.valueOf(gateway.get("imei"));
			device.setImei(imei);
			device.setIotType(IOTType.GATEWAY);
		}
		return device;
	}

	public Cellular addCellular(Map<String, Object> device,
			GatewayBulkUploadRequestWithDeviceForwarding gatewayBulkUploadRequestWithDeviceForwarding) {
		Cellular cellular = new Cellular();
		cellular.setCellular(String.valueOf(device.get("sim")));
		cellular.setImei(String.valueOf(device.get("imei")));
		cellular.setPhone(String.valueOf(device.get("phone")));
		cellular.setServiceNetwork(gatewayBulkUploadRequestWithDeviceForwarding.getTelecomProvider());
		boolean isCellularUuidUnique = false;
		String cellularUuid = "";
		while (!isCellularUuidUnique) {
			cellularUuid = UUID.randomUUID().toString();
			Cellular byUuid = cellularRepository.findByUuid(cellularUuid);
			if (byUuid == null) {
				isCellularUuidUnique = true;
			}
		}
		cellular.setUuid(cellularUuid);
		return cellularRepository.save(cellular);
	}

	public Page<DeviceResponsePayload> convertDeviceToDevicePayLoad1(String messageUuid, Page<Device> deviceDetails,
			Pageable pageable, boolean forExport, List<CustomerForwardingRuleUrlDTO> customerForwardingRuleUrls,
			List<Map<String, String>> customerForwardingRulesMap,
			Set<DeviceIgnoreForwardingRule> deviceIgnoreForwardingRules) {
		LOGGER.info("Inside convertDeviceToDevicePayLoad1 of messageUuid " + messageUuid);
		// List<String> imei = deviceDetails.stream().map(e ->
		// e.getImei()).collect(Collectors.toList());
		List<Event> eventList = eventRepository.findAll();
		// List<SearchHit[]> hitsForMaintainence = null;
		// List<SearchHit[]> hitsForLatestReport = null;
		// LOGGER.info("Time Before before getting data from elastic:- " + new
		// Date().getTime());
		// try {
		// hitsForMaintainence = getDataFromElasticforDeviceIds(imei, true);
		// } catch (IOException e1) {
		// e1.printStackTrace();
		// }
		// try {
		// hitsForLatestReport = getDataFromElasticforDeviceIds(imei, false);
		// } catch (IOException e1) {
		// e1.printStackTrace();
		// }
		// LOGGER.info("Time after getting data from elastic:- " + new
		// Date().getTime());
		// final List<SearchHit[]> hits = hitsForMaintainence;
		// final List<SearchHit[]> hits2 = hitsForLatestReport;
		List<DeviceResponsePayload> devicePayLoadList = new ArrayList<>();
		deviceDetails.forEach(deviceDetail -> {
			DeviceResponsePayload devicepayload = new DeviceResponsePayload();
			// try {
			// if (hits != null) {
			// devicepayload = getDataFromElastic(hits, devicepayload,
			// deviceDetail.getImei());
			// }
			// } catch (Exception e) {
			// e.printStackTrace();
			// }

			devicepayload.setImei(deviceDetail.getImei());
			devicepayload.setDeviceId(deviceDetail.getImei());
			if (deviceDetail.getDeviceDetails() != null) {
				devicepayload.setAppVersion(deviceDetail.getDeviceDetails().getAppVersion());
				devicepayload.setBinVersion(deviceDetail.getDeviceDetails().getBinVersion());
				devicepayload.setBleVersion(deviceDetail.getDeviceDetails().getBleVersion());
				devicepayload.setConfig1(deviceDetail.getDeviceDetails().getConfig1Name());
				devicepayload.setConfig2(deviceDetail.getDeviceDetails().getConfig2Name());
				devicepayload.setConfig3(deviceDetail.getDeviceDetails().getConfig3Name());
				devicepayload.setConfig4(deviceDetail.getDeviceDetails().getConfig4Name());
				devicepayload.setMcuVersion(deviceDetail.getDeviceDetails().getMcuVersion());
//				if (deviceDetail.getDeviceMaintenanceDetails() != null) {
//				devicepayload.setAppVersion(deviceDetail.getDeviceMaintenanceDetails().getAppVersion());
//				devicepayload.setBinVersion(deviceDetail.getDeviceMaintenanceDetails().getBinVersion());
//				devicepayload.setBleVersion(deviceDetail.getDeviceMaintenanceDetails().getBleVersion());
//				devicepayload.setConfig1(deviceDetail.getDeviceMaintenanceDetails().getConfig1Name());
//				devicepayload.setConfig2(deviceDetail.getDeviceMaintenanceDetails().getConfig2Name());
//				devicepayload.setConfig3(deviceDetail.getDeviceMaintenanceDetails().getConfig3Name());
//				devicepayload.setConfig4(deviceDetail.getDeviceMaintenanceDetails().getConfig4Name());
//				devicepayload.setMcuVersion(deviceDetail.getDeviceMaintenanceDetails().getMcuVersion());
			}
			if (deviceDetail.getStatus() != null && deviceDetail.getStatus().equals(DeviceStatus.ACTIVE)) {
				devicepayload.setInstallationDate(deviceDetail.getInstallationDate());
			} else {
				devicepayload.setInstallationDate(null);
			}

			devicepayload.setProductCode(deviceDetail.getProductCode());
			devicepayload.setProductName(deviceDetail.getProductName());
			devicepayload.setModel(deviceDetail.getProductName());
			devicepayload.setSon(deviceDetail.getSon());
			devicepayload.setOrder(deviceDetail.getSon());
			devicepayload.setType(deviceDetail.getIotType());
			devicepayload.setEpicorOrderNumber(deviceDetail.getEpicorOrderNumber());
			devicepayload.setMacAddress(deviceDetail.getMacAddress());
			devicepayload.setQuantityShipped(deviceDetail.getQuantityShipped());
			devicepayload.setDeviceType(deviceDetail.getDeviceType());
			

			if (deviceDetail.getOrganisation() != null) {
				devicepayload.setCan(deviceDetail.getOrganisation().getAccountNumber());
				devicepayload.setCreatedBy(deviceDetail.getOrganisation().getOrganisationName());
				devicepayload.setCustomer(deviceDetail.getOrganisation().getOrganisationName());
				devicepayload.setUpdatedby(deviceDetail.getOrganisation().getOrganisationName());
				try {
					devicepayload.setForwardingGroup(deviceDetail.getOrganisation().getForwardingGroupMappers().stream()
							.map(x -> x.getCustomerForwardingGroup().getName()).findFirst().orElse(""));
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			if (deviceDetail.getPurchaseBy() != null) {
				devicepayload.setPurchaseBy(deviceDetail.getPurchaseBy().getAccountNumber());
				devicepayload.setPurchaseByName(deviceDetail.getPurchaseBy().getOrganisationName());
			}
			if (deviceDetail.getInstalledBy() != null) {
				devicepayload.setInstalledBy(deviceDetail.getInstalledBy().getAccountNumber());
			}
			devicepayload.setUuid(deviceDetail.getUuid());
			devicepayload.setType(deviceDetail.getIotType());
			devicepayload.setStatus(deviceDetail.getStatus());
			devicepayload.setQaStatus(deviceDetail.getQaStatus());
			devicepayload.setUsageStatus(deviceDetail.getUsageStatus());
			devicepayload.setOwnerLevel2(deviceDetail.getOwnerLevel2());

			// LOGGER.info("Before format:- "+deviceDetail.getQaDate().toString());

			Asset_Device_xref assetGatewayXref = assetDeviceXrefRepository.findByGatewayId(deviceDetail.getId());
			if (assetGatewayXref != null && assetGatewayXref.getAsset() != null) {
				Asset asset = assetGatewayXref.getAsset();
				devicepayload.setAssetName(asset.getAssignedName());
				devicepayload.setVin(asset.getVin());
				devicepayload
						.setManufacturer(asset.getManufacturer() != null ? asset.getManufacturer().getName() : null);
				devicepayload.setAssetType(asset.getCategory() != null ? asset.getCategory().getValue() : null);

			}
			DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
					.withZone(ZoneId.of("America/Los_Angeles"));
			if (deviceDetail.getQaDate() != null) {
				String now = dtf.format(deviceDetail.getQaDate());
				devicepayload.setQaDates(now);
			}
			// if(deviceDetail.getLatestReport()!=null) {
			// String dateFormate = "yyyy-MM-dd HH:mm:ss";
			// SimpleDateFormat dateFormat = new SimpleDateFormat(dateFormate);
			// devicepayload.setLastReportDate(
			// dateFormat.format(deviceDetail.getLatestReport()));
			// }
			// try {
			// if (hits2 != null) {
			// devicepayload = getLatestReportFromElastic(hits2, deviceDetail.getImei(),
			// devicepayload, eventList);
			// }
			// } catch (Exception e) {
			// e.printStackTrace();
			// // TODO: handle exception
			// }

//			if(deviceDetail.getDeviceMaintenanceDetails() != null) {
//				devicepayload.setAppVersion(deviceDetail.getDeviceMaintenanceDetails().getAppVersion());
//				devicepayload.setBinVersion(deviceDetail.getDeviceMaintenanceDetails().getBinVersion());
//				devicepayload.setBleVersion(deviceDetail.getDeviceMaintenanceDetails().getBleVersion());
//				devicepayload.setMcuVersion(deviceDetail.getDeviceMaintenanceDetails().getMcuVersion());
//
//				devicepayload.setConfig1Name(deviceDetail.getDeviceMaintenanceDetails().getConfig1Name());
//				devicepayload.setConfig1CRC(deviceDetail.getDeviceMaintenanceDetails().getConfig1CRC());
//				devicepayload.setConfig2Name(deviceDetail.getDeviceMaintenanceDetails().getConfig2Name());
//				devicepayload.setConfig2CRC(deviceDetail.getDeviceMaintenanceDetails().getConfig2CRC());
//				devicepayload.setConfig3Name(deviceDetail.getDeviceMaintenanceDetails().getConfig3Name());
//				devicepayload.setConfig3CRC(deviceDetail.getDeviceMaintenanceDetails().getConfig3CRC());
//				devicepayload.setConfig4Name(deviceDetail.getDeviceMaintenanceDetails().getConfig4Name());
//				devicepayload.setConfig4CRC(deviceDetail.getDeviceMaintenanceDetails().getConfig4CRC());
//				devicepayload.setDevuserCfgName(deviceDetail.getDeviceMaintenanceDetails().getDevuserCfgName());
//				devicepayload.setDevuserCfgValue(deviceDetail.getDeviceMaintenanceDetails().getDevuserCfgValue());
//			}
			if (deviceDetail.getDeviceDetails() != null) {
				devicepayload.setAppVersion(deviceDetail.getDeviceDetails().getAppVersion());
				devicepayload.setBinVersion(deviceDetail.getDeviceDetails().getBinVersion());
				devicepayload.setBleVersion(deviceDetail.getDeviceDetails().getBleVersion());
				devicepayload.setMcuVersion(deviceDetail.getDeviceDetails().getMcuVersion());

				devicepayload.setConfig1Name(deviceDetail.getDeviceDetails().getConfig1Name());
				devicepayload.setConfig1CRC(deviceDetail.getDeviceDetails().getConfig1CRC());
				devicepayload.setConfig2Name(deviceDetail.getDeviceDetails().getConfig2Name());
				devicepayload.setConfig2CRC(deviceDetail.getDeviceDetails().getConfig2CRC());
				devicepayload.setConfig3Name(deviceDetail.getDeviceDetails().getConfig3Name());
				devicepayload.setConfig3CRC(deviceDetail.getDeviceDetails().getConfig3CRC());
				devicepayload.setConfig4Name(deviceDetail.getDeviceDetails().getConfig4Name());
				devicepayload.setConfig4CRC(deviceDetail.getDeviceDetails().getConfig4CRC());
				devicepayload.setDevuserCfgName(deviceDetail.getDeviceDetails().getDevuserCfgName());
				devicepayload.setDevuserCfgValue(deviceDetail.getDeviceDetails().getDevuserCfgValue());

				devicepayload.setLastReportDateTime(deviceDetail.getDeviceDetails().getLatestReport());
				devicepayload.setEventId(deviceDetail.getDeviceDetails().getEventId());

				devicepayload.setEventType(getEventByEventId(eventList, deviceDetail.getDeviceDetails().getEventId()));
				devicepayload.setBattery(deviceDetail.getDeviceDetails().getBattery());
				devicepayload.setLat(deviceDetail.getDeviceDetails().getLat());
				devicepayload.setLongitude(deviceDetail.getDeviceDetails().getLongitude());
				devicepayload.setHardwareIdVersion(deviceDetail.getDeviceDetails().getHwIdVersion());
				devicepayload.setHardwareversionRevision(deviceDetail.getDeviceDetails().getHwVersionRevision());
				devicepayload.setInstalledStatusFlag(deviceDetail.getDeviceDetails().getInstalledStatusFlag());
				devicepayload.setUsageStatus(deviceDetail.getUsageStatus());

			}

			CellularDetailPayload cellularPayload = new CellularDetailPayload();
			if (deviceDetail.getCellular() != null) {
				cellularPayload.setCarrierId(deviceDetail.getCellular().getCarrierId());
				cellularPayload.setCountryCode(deviceDetail.getCellular().getCountryCode());
				cellularPayload.setIccid(deviceDetail.getCellular().getIccid());
				cellularPayload.setImsi(deviceDetail.getCellular().getImsi());
				cellularPayload.setImei(deviceDetail.getCellular().getImei());
				cellularPayload.setPhone(deviceDetail.getCellular().getPhone());
				cellularPayload.setServiceCountry(deviceDetail.getCellular().getServiceCountry());
				cellularPayload.setServiceNetwork(deviceDetail.getCellular().getServiceNetwork());
				cellularPayload.setCellular(deviceDetail.getCellular().getCellular());

				devicepayload.setCarrierId(deviceDetail.getCellular().getCarrierId());
				devicepayload.setCountryCode(deviceDetail.getCellular().getCountryCode());
				devicepayload.setPhone(deviceDetail.getCellular().getPhone());
				devicepayload.setSim(deviceDetail.getCellular().getCellular());
				devicepayload.setServiceCountry(deviceDetail.getCellular().getServiceCountry());
				devicepayload.setServiceNetwork(deviceDetail.getCellular().getServiceNetwork());
			}

			devicepayload.setCellularPayload(cellularPayload);

			if (deviceDetail.getConfigName() != null) {
				devicepayload.setConfigName(deviceDetail.getConfigName());
			} else {
				devicepayload.setConfigName("N/A");
			}
			// List<DeviceForwarding> al =
			// deviceForwardingRepository.findByImei(deviceDetail.getImei());
			// List<DeviceForwardingResponse> list = new
			// ArrayList<DeviceForwardingResponse>();
			// int k = 0;
			// if (deviceDetail.getDeviceForwarding() != null &&
			// deviceDetail.getDeviceForwarding().size() > 0) {
			// for (DeviceForwarding pm : deviceDetail.getDeviceForwarding()) {
			// DeviceForwardingResponse response = new DeviceForwardingResponse();
			// response.setId(pm.getId());
			// response.setUuid(pm.getUuid());
			// response.setType(pm.getType());
			// response.setUrl(pm.getUrl());
			// Device device = new Device();
			// if (pm.getDevice() != null) {
			// device.setUuid(pm.getDevice().getUuid());
			// device.setImei(pm.getDevice().getImei());
			// }
			// response.setDevice(device);
			// final String[] ruleName = new String[1];
			// boolean match = customerForwardingRuleUrls.stream()
			// .filter(customerForwardingRuleUrl -> customerForwardingRuleUrl.getUuid() !=
			// null)
			// .anyMatch(customerForwardingRuleUrl -> {
			// if
			// (customerForwardingRuleUrl.getUuid().equals(pm.getForwardingRuleUrlUuid())) {
			// ruleName[0] = customerForwardingRuleUrl.getRuleName();
			// return true;
			// }
			// return false;
			// });
			// if (k == 0) {
			// devicepayload.setType1(pm.getType());
			// devicepayload.setUrl1(pm.getUrl());
			// if (match) {
			// devicepayload.setRule1(ruleName[0]);
			// devicepayload.setRule1Source("Device Specific");
			// }
			// } else if (k == 1) {
			// devicepayload.setType2(pm.getType());
			// devicepayload.setUrl2(pm.getUrl());
			// if (match) {
			// devicepayload.setRule2(ruleName[0]);
			// devicepayload.setRule2Source("Device Specific");
			// }
			// } else if (k == 2) {
			// devicepayload.setType3(pm.getType());
			// devicepayload.setUrl3(pm.getUrl());
			// if (match) {
			// devicepayload.setRule3(ruleName[0]);
			// devicepayload.setRule3Source("Device Specific");
			// }
			// } else if (k == 3) {
			// devicepayload.setType4(pm.getType());
			// devicepayload.setUrl4(pm.getUrl());
			// if (match) {
			// devicepayload.setRule4(ruleName[0]);
			// devicepayload.setRule4Source("Device Specific");
			// }
			// } else if (k == 4) {
			// devicepayload.setType5(pm.getType());
			// devicepayload.setUrl5(pm.getUrl());
			// if (match) {
			// devicepayload.setRule5(ruleName[0]);
			// devicepayload.setRule5Source("Device Specific");
			// }
			// }
			// k++;
			// list.add(response);
			// }
			// }
			// if (k < 5) {
			// for (Map<String, String> map : customerForwardingRulesMap) {
			// String ruleUuid = map.get("ruleUuid");
			// String orgUuid = map.get("orgUuid");
			// if ((deviceDetail.getOrganisation().getUuid().equals(orgUuid)) &&
			// deviceIgnoreForwardingRules.stream()
			// .filter(deviceIgnoreForwardingRule -> deviceIgnoreForwardingRule
			// .getCustomerForwardingRuleUuid() != null)
			// .noneMatch(deviceIgnoreForwardingRule -> deviceIgnoreForwardingRule
			// .getCustomerForwardingRuleUuid().equals(ruleUuid))) {
			// DeviceForwardingResponse response = new DeviceForwardingResponse();
			// if (k == 0) {
			// devicepayload.setRule1(map.get("ruleName"));
			// devicepayload.setRule1Source(map.get("sourceName"));
			// } else if (k == 1) {
			// devicepayload.setRule2(map.get("ruleName"));
			// devicepayload.setRule2Source(map.get("sourceName"));
			// } else if (k == 2) {
			// devicepayload.setRule3(map.get("ruleName"));
			// devicepayload.setRule3Source(map.get("sourceName"));
			// } else if (k == 3) {
			// devicepayload.setRule4(map.get("ruleName"));
			// devicepayload.setRule4Source(map.get("sourceName"));
			// } else if (k == 4) {
			// devicepayload.setRule5(map.get("ruleName"));
			// devicepayload.setRule5Source(map.get("sourceName"));
			// }
			// k++;
			// list.add(response);
			// if (k >= 5) {
			// break;
			// }
			// }
			// }
			// }
			// devicepayload.setDeviceForwardingPayload(list);
			try {
				// Asset_Device_xref asset_Device_xref =
				// assetDeviceXrefRepository.findByDevice(deviceDetail.getImei());

				Asset_Device_xref asset_Device_xref = deviceDetail.getAssetDeviceXref();
				if (asset_Device_xref != null) {
					if (asset_Device_xref.getAsset() != null) {
						devicepayload.setAssetName(asset_Device_xref.getAsset().getAssignedName());
						devicepayload.setAssetType(asset_Device_xref.getAsset().getCategory().toString());
						devicepayload.setVin(asset_Device_xref.getAsset().getVin());
						if (asset_Device_xref.getAsset().getManufacturer() != null) {
							devicepayload.setManufacturer(asset_Device_xref.getAsset().getManufacturer().getName());
						}
					}
				}

				if (deviceDetail.getDeviceSignature() != null) {
					devicepayload.setCreatedTime(deviceDetail.getDeviceSignature().getCreatedTime());
					devicepayload.setRevokedTime(deviceDetail.getDeviceSignature().getRevokedTime());
					if (!AppUtility.isEmpty(deviceDetail.getDeviceSignature().getImeiHashed())) {
						devicepayload.setImeiHashed(deviceDetail.getDeviceSignature().getImeiHashed());
						if (deviceDetail.getDeviceSignature().getImeiHashed().equals("REVOKED")) {
							devicepayload.setSecStatus("Unsecured");
							devicepayload.setSecDate(deviceDetail.getDeviceSignature().getRevokedTime());
						} else {
							devicepayload.setSecStatus("Secured");
							devicepayload.setSecDate(deviceDetail.getDeviceSignature().getCreatedTime());
						}
					} else {
						devicepayload.setSecStatus("N/A");
					}

				} else {
					devicepayload.setSecStatus("N/A");
				}

			} catch (Exception ex) {
				ex.printStackTrace();
			}

			devicePayLoadList.add(devicepayload);
		});
		LOGGER.info("Time After preparing the deviceresponse payload object in beanconverter for device list API:- "
				+ new Date().getTime());
		LOGGER.info("exiting convertDeviceToDevicePayLoad1 of messageUuid " + messageUuid);
		Page<DeviceResponsePayload> page = new PageImpl<>(devicePayLoadList, pageable,
				deviceDetails.getTotalElements());
		return page;
	}

	public DeviceBean convertGatewayToGatewayBean(Device gateway, HashMap<String, String> lookupMap) {
		DeviceBean gatewayBean = new DeviceBean();
		gatewayBean.setDeviceUuid(gateway.getUuid());
		gatewayBean.setDateCreated(gateway.getCreatedOn());
		gatewayBean.setDateUpdated(gateway.getUpdatedOn());
		gatewayBean.setImei(gateway.getImei());
		gatewayBean.setMacAddress(gateway.getMacAddress());
		gatewayBean.setProductCode(gateway.getProductCode());
		gatewayBean.setProductName(gateway.getProductName());
		gatewayBean.setStatus(gateway.getStatus().getValue());
		gatewayBean.setCan(gateway.getOrganisation().getAccountNumber());
		gatewayBean.setType(gateway.getDeviceType());
		if (gateway.getLastPerformAction() != null) {
			gatewayBean.setAction(gateway.getLastPerformAction());
		} else {
			if (gateway.getIsDeleted()) {
				gatewayBean.setAction(Constants.GATEWAYS_ACTION_DELETED);
			} else {
				gatewayBean.setAction(Constants.GATEWAYS_ACTION_UPDATED);
			}
		}
		List<Device> sensorBeanList = new ArrayList<>();
		List<Device_Device_xref> deviceSensorxreffList = deviceDeviceXrefRepository
				.findSensorByGatewayId(gateway.getId());
		deviceSensorxreffList.forEach(sensor -> {
			Device d = sensor.getSensorUuid();
			d.setStatus(DeviceStatus.PENDING);
			deviceRepository.save(d);
		});
		deviceSensorxreffList.forEach(gatewaySensor -> {
			Device sensor = gatewaySensor.getSensorUuid();
			// SensorBean sensorBean = new SensorBean();
			sensor.setOrganisation(sensor.getOrganisation());

			// sensor.setSensorUuid(sensor.getUuid());
			sensor.setProductCode(sensor.getProductCode());
			sensor.setStatus(sensor.getStatus());
			sensor.setCreatedOn(sensor.getCreatedOn());
			sensor.setUpdatedOn(sensor.getUpdatedOn());
			// sensor.setGatewayUuid(gateway.getUuid());
			sensor.setMacAddress(sensor.getMacAddress());
			if (sensor.getProductCode() != null && sensor.getProductCode().equalsIgnoreCase("77-S177")) {
				gatewayBean.setDisplayName(sensor.getProductName());
			} else if (lookupMap.size() > 0) {
				sensor.setProductCode(lookupMap.get(sensor.getProductCode()));
			}
			sensorBeanList.add(sensor);
		});
		gatewayBean.setDeviceList(sensorBeanList);
		return gatewayBean;
	}

	public AssetResponseDTO convertAssetToAssetResponseDTO(Asset asset, InstallHistory installHistory)
			throws JsonProcessingException {
		AssetResponseDTO assetResponseDto = new AssetResponseDTO();
		assetResponseDto.setAssetUuid(asset.getUuid());
		assetResponseDto.setAssignedName(asset.getAssignedName());
		assetResponseDto.setStatus(asset.getStatus().getValue());
		if (asset.getCategory() != null && asset.getCategory().getValue() != "") {
			assetResponseDto.setCategory(asset.getCategory().getValue());
		}
		assetResponseDto.setVin(asset.getVin());
		ManufacturerDetailsDTO manufacturerDetailsDTO = new ManufacturerDetailsDTO();
		if (asset.getManufacturer() != null && asset.getManufacturer().getName() != null) {
			manufacturerDetailsDTO.setMake(asset.getManufacturer().getName());
		}
		if (asset.getManufacturerDetails() != null) {
			manufacturerDetailsDTO.setModel(asset.getManufacturerDetails().getModel());
		}
		if (asset.getYear() != null) {
			manufacturerDetailsDTO.setYear(asset.getYear());
		} else {
			manufacturerDetailsDTO.setYear("Not Available");
		}
		assetResponseDto.setManufacturerDetails(manufacturerDetailsDTO);
		assetResponseDto.setEligibleGateway(asset.getGatewayEligibility());
		assetResponseDto.setCan(asset.getOrganisation().getAccountNumber());
		List<Lookup> lookups = null;
		if (asset.getCategory() != null && asset.getCategory().getValue() != "") {
			lookups = assetConfigurationRepository.findByField(asset.getCategory().getValue());
		}
		if (lookups != null && lookups.size() > 0) {
			assetResponseDto.setDisplayName(lookups.get(0).getValue());
		}
		if (asset.getCreatedOn() != null)
			assetResponseDto.setDatetimeCreated(asset.getCreatedOn().toString());
		if (asset.getUpdatedOn() != null)
			assetResponseDto.setDatetimeUpdated(asset.getUpdatedOn().toString());
		assetResponseDto.setCompanyName(asset.getOrganisation().getOrganisationName());
		assetResponseDto.setIsVinValidated(asset.getIsVinValidated());
		assetResponseDto.setComment(asset.getComment());
		if (installHistory != null) {
			assetResponseDto.setImei(installHistory.getDevice().getImei());
			if (installHistory.getStatus().equals(InstallHistoryStatus.FINISHED)) {
				assetResponseDto.setInstalled(installHistory.getDateEnded().toString());
			}
		}
		return assetResponseDto;
	}

	public List<InstallationResponse> convertAssetDeviceXrefToInstalledResponse(List<Asset_Device_xref> device_xrefs) {

		List<InstallationResponse> installationResponses = new ArrayList<>();

		for (Asset_Device_xref asset_Device_xref : device_xrefs) {
			InstallationResponse installationResponse = new InstallationResponse();
			installationResponse.setAssetId(asset_Device_xref.getAsset().getAssignedName());
			installationResponse.setAssetType(asset_Device_xref.getAsset().getCategory().getValue());
			installationResponse.setMakeYear(asset_Device_xref.getAsset().getYear());
			if (asset_Device_xref.getDevice() != null) {
				installationResponse.setImei(asset_Device_xref.getDevice().getImei());
			}
			if (asset_Device_xref.getAsset().getManufacturer() != null) {
				installationResponse.setManufacturer(asset_Device_xref.getAsset().getManufacturer().getName());
			}
			if (asset_Device_xref.getAsset().getOrganisation() != null) {
				installationResponse
						.setCustomerName(asset_Device_xref.getAsset().getOrganisation().getOrganisationName());
			} else if (asset_Device_xref.getDevice().getOrganisation() != null) {
				installationResponse
						.setCustomerName(asset_Device_xref.getDevice().getOrganisation().getOrganisationName());
			}
			installationResponses.add(installationResponse);
		}
		return installationResponses;

	}

	public GatewayBeanForMobileApp convertGatewayToGatewayBeanForMobileApp(DeviceView gateway,
			HashMap<String, String> lookupMap) {
		GatewayBeanForMobileApp gatewayBean = new GatewayBeanForMobileApp();
		gatewayBean.setGatewayUuid(gateway.getUuid());
		gatewayBean.setDateCreated(gateway.getCreatedOn());
		gatewayBean.setDateUpdated(gateway.getUpdatedOn());
		gatewayBean.setImei(gateway.getImei());
		gatewayBean.setMacAddress(gateway.getMacAddress());
		gatewayBean.setProductCode(gateway.getProductCode());
		gatewayBean.setProductName(gateway.getProductName());
		if (gateway.getStatus() != null) {
			// gatewayBean.setStatus(gateway.getStatus());
			gatewayBean.setStatus(DeviceStatus.valueOf(gateway.getStatus()).getValue());
		}
		gatewayBean.setCan(gateway.getCan());
		gatewayBean.setType(gateway.getIotType());
		if (gateway.getLastPerformAction() != null) {
			gatewayBean.setAction(gateway.getLastPerformAction());
		} else {
			if (gateway.getIsDeleted()) {
				gatewayBean.setAction(Constants.GATEWAYS_ACTION_DELETED);
			} else {
				gatewayBean.setAction(Constants.GATEWAYS_ACTION_UPDATED);
			}
		}
		List<SensorBean> sensorBeanList = new ArrayList<>();
		if (gateway != null && gateway.getId() != null) {
			List<Device_Sensor_Xref_Dto> sensorList = deviceDeviceXrefRepository
					.findSensorByDtoGatewayId(gateway.getId());
			sensorList.forEach(gatewaySensor -> {
				SensorBean sensorBean = new SensorBean();
				sensorBean.setCan(gateway.getCan());
				sensorBean.setSensorUuid(gatewaySensor.getSensorUuid());
				sensorBean.setProductCode(gatewaySensor.getProductCode());
				if (gatewaySensor.getStatus() != null && gatewaySensor.getStatus().getValue() != null) {
					sensorBean.setStatus(gatewaySensor.getStatus().getValue());
				}
				sensorBean.setDatetimeCreated(gatewaySensor.getCreatedOn());
				sensorBean.setDatetimeUpdated(gatewaySensor.getUpdatedOn());
				sensorBean.setGatewayUuid(gateway.getUuid());
				sensorBean.setMacAddress(gatewaySensor.getMacAddress());
				if (gatewaySensor.getProductCode() != null
						&& gatewaySensor.getProductCode().equalsIgnoreCase("77-S177")) {
					sensorBean.setDisplayName(gatewaySensor.getDisplayName());
				} else {
					sensorBean.setDisplayName(gatewaySensor.getDisplayName());
				}
				// else if (lookupMap.size() > 0) {
				// sensorBean.setDisplayName(lookupMap.get(gatewaySensor.getProductCode()));
				// }

				sensorBeanList.add(sensorBean);
			});
			gatewayBean.setSensorList(sensorBeanList);
		}
		return gatewayBean;
	}

	// commented
	public List<AssetResponseDTO> convertAssetToAssetResponseDTO(List<Asset_Device_xref> assetList)
			throws JsonProcessingException {
		List<AssetResponseDTO> list = new ArrayList<>();
		Set<String> imeiList = assetList.stream().map(e -> e.getDevice().getImei()).collect(Collectors.toSet());
		List<InstalledHistoryResponsePayload> installedHistoryList = new ArrayList<>();
		try {
			LOGGER.info("before calling installed history API for getting installed code");
			installedHistoryList = restUtils.getInstalledHistoryResponseByDeviceImei(imeiList);
		} catch (Exception e) {
			LOGGER.info("Failed to fetch installed history data", e.getMessage());
			e.printStackTrace();
		}
		for (Asset_Device_xref assetDevice : assetList) {
			Asset asset = assetDevice.getAsset();
			Device device = assetDevice.getDevice();
			AssetResponseDTO assetResponseDto = new AssetResponseDTO();
			if (!AppUtility.isEmpty(installedHistoryList)) {
				InstalledHistoryResponsePayload installedHistoryResponsePayload = installedHistoryList.stream()
						.filter(e -> e.getImei().equals(device.getImei())).findAny().orElse(null);
				if (!AppUtility.isEmpty(installedHistoryResponsePayload)) {
					assetResponseDto.setInstallationCode(installedHistoryResponsePayload.getInstallCode());
					assetResponseDto.setInstallationStatus(installedHistoryResponsePayload.getStatus());
				}
			}
			assetResponseDto.setAssetUuid(asset.getUuid());
			assetResponseDto.setId(asset.getId());
			assetResponseDto.setAssignedName(asset.getAssignedName());
			if (asset.getStatus() != null) {
				assetResponseDto.setStatus(asset.getStatus().getValue());
			}
			if (asset.getCategory() != null) {
				assetResponseDto.setCategory(asset.getCategory().getValue());
			}
			assetResponseDto.setVin(asset.getVin());
			ManufacturerDetailsDTO manufacturerDetailsDTO = new ManufacturerDetailsDTO();
			if (asset.getManufacturer() != null && asset.getManufacturer().getName() != null) {
				manufacturerDetailsDTO.setMake(asset.getManufacturer().getName());
			}
			if (asset.getManufacturerDetails() != null) {
				manufacturerDetailsDTO.setModel(asset.getManufacturerDetails().getModel());
			}
			if (asset.getYear() != null) {
				manufacturerDetailsDTO.setYear(asset.getYear());
			}
			assetResponseDto.setManufacturerDetails(manufacturerDetailsDTO);
			assetResponseDto.setEligibleGateway(asset.getGatewayEligibility());
			if (asset.getOrganisation() != null) {
				assetResponseDto.setCan(asset.getOrganisation().getAccountNumber());
			}
			List<Lookup> lookups = null;
			if (asset.getCategory() != null) {
				lookups = assetConfigurationRepository.findByField(asset.getCategory().getValue());
			}
			if (lookups != null && lookups.size() > 0) {
				assetResponseDto.setDisplayName(lookups.get(0).getValue());
			}
			if (asset.getCreatedAt() != null)
				assetResponseDto.setDatetimeCreated(asset.getCreatedAt().toString());
			if (asset.getUpdatedAt() != null)
				assetResponseDto.setDatetimeUpdated(asset.getUpdatedAt().toString());
			assetResponseDto.setCompanyName(asset.getOrganisation().getOrganisationName());
			assetResponseDto.setIsVinValidated(asset.getIsVinValidated());
			assetResponseDto.setComment(asset.getComment());
			assetResponseDto.setImei(device.getImei());
			if (device.getInstalledStatusFlag() != null)
				assetResponseDto.setInstalled(device.getInstalledStatusFlag());
			list.add(assetResponseDto);
		}
		return list;
	}

	public Page<MaintenanceReportHistoryPayload> convertMaintenanceReportHistoryToMaintenanceReportHistoryPayload(
			Page<MaintenanceReportHistory> maitenanceReportPage, Pageable pageable, boolean isUuid) {
		List<MaintenanceReportHistoryPayload> maintenanceReportHistoryPayloads = new ArrayList<>();
		Set<String> sensorUuidList = maitenanceReportPage.stream()
				.filter(d -> (d.getSensorUuid() != null && d.getSensorUuid().trim().length() > 0))
				.map(e -> e.getSensorUuid()).collect(Collectors.toSet());
		List<Device> deviceList = new ArrayList<>();
		if (isUuid) {
			deviceList = deviceRepository.getListOfDeviceByUuidList(sensorUuidList);
		}
		final List<Device> sensorList = deviceList;
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		formatter.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));

		maitenanceReportPage.forEach(maitenanceReport -> {
			MaintenanceReportHistoryPayload maintenanceReportHistoryPayload = new MaintenanceReportHistoryPayload();
			maintenanceReportHistoryPayload.setId(maitenanceReport.getId());
			maintenanceReportHistoryPayload.setNewSensorId(maitenanceReport.getNewSensorId());
			maintenanceReportHistoryPayload.setOldSensorId(maitenanceReport.getOldSensorId());
			maintenanceReportHistoryPayload.setResolutionType(maitenanceReport.getResolutionType());
			maintenanceReportHistoryPayload.setSensorType(maitenanceReport.getSensorType());
			maintenanceReportHistoryPayload.setWorkOrder(maitenanceReport.getWorkOrder());
			maintenanceReportHistoryPayload.setMaintenanceLocation(maitenanceReport.getMaintenanceLocation());
			try {
				if (maitenanceReport.getServiceDateTime() != null) {
					Date dateStarted = Date.from(maitenanceReport.getServiceDateTime());
					String formattedDate = formatter.format(dateStarted);
					maintenanceReportHistoryPayload.setPstServiceTime(formattedDate + " PST");
				}
			} catch (Exception e) {
				// TODO: handle exception
			}

			maintenanceReportHistoryPayload.setServiceTime(maitenanceReport.getServiceDateTime());
			maintenanceReportHistoryPayload.setValidationTime(maitenanceReport.getValidationTime());
			maintenanceReportHistoryPayload.setCreatedDate(maitenanceReport.getCreatedDate());
			maintenanceReportHistoryPayload.setPosition(maitenanceReport.getPosition());
			if (maitenanceReport.getAsset() != null && maitenanceReport.getAsset().getAssignedName() != null) {
				maintenanceReportHistoryPayload.setAssetId(maitenanceReport.getAsset().getAssignedName());
				maintenanceReportHistoryPayload.setAssetUuId(maitenanceReport.getAsset().getUuid());
			}
			if (maitenanceReport.getDevice() != null && maitenanceReport.getDevice().getImei() != null) {
				maintenanceReportHistoryPayload.setDeviceId(maitenanceReport.getDevice().getImei());
			}
			if (maitenanceReport.getOrganisation() != null
					&& maitenanceReport.getOrganisation().getOrganisationName() != null) {
				maintenanceReportHistoryPayload
						.setServiceVendorName(maitenanceReport.getOrganisation().getOrganisationName());
			}
			if (maitenanceReport.getUser() != null && maitenanceReport.getUser().getFirstName() != null) {
				String technician = maitenanceReport.getUser().getFirstName();
				if (maitenanceReport.getUser().getLastName() != null) {
					technician = technician + " " + maitenanceReport.getUser().getLastName();
				}
				maintenanceReportHistoryPayload.setTechnician(technician);
			}
			if (!AppUtility.isEmpty(maitenanceReport.getSensorUuid())) {
				Device sensor = sensorList.stream().filter(e -> e.getUuid().equals(maitenanceReport.getSensorUuid()))
						.findAny().orElse(null);
				if (sensor != null) {
					maintenanceReportHistoryPayload.setSensorName(sensor.getProductName());
					if (!AppUtility.isEmpty(sensor.getStatus())) {
						maintenanceReportHistoryPayload.setStatus(sensor.getStatus().getValue());
					}
				}
			}
			maintenanceReportHistoryPayload.setMacAddress(maitenanceReport.getMacAddress());
			maintenanceReportHistoryPayload.setOldMacAddress(maitenanceReport.getOldMacAddress());
			maintenanceReportHistoryPayloads.add(maintenanceReportHistoryPayload);
		});
		Page<MaintenanceReportHistoryPayload> page = new PageImpl<>(maintenanceReportHistoryPayloads, pageable,
				maintenanceReportHistoryPayloads.size());
		return page;
	}

	public List<MaintenanceReportHistoryPayload> convertMaintenanceReportHistoryToMaintenanceReportHistoryPayload(
			List<MaintenanceReportHistory> maitenanceReportList) {
		List<MaintenanceReportHistoryPayload> maintenanceReportHistoryPayloads = new ArrayList<>();
		Set<String> sensorUuidList = maitenanceReportList.stream()
				.filter(d -> (d.getSensorUuid() != null && d.getSensorUuid().trim().length() > 0))
				.map(e -> e.getSensorUuid()).collect(Collectors.toSet());
		List<Device> sensorList = deviceRepository.getListOfDeviceByUuidList(sensorUuidList);
		maitenanceReportList.forEach(maintenanceReport -> {
			MaintenanceReportHistoryPayload maintenanceReportHistoryPayload = new MaintenanceReportHistoryPayload();
			maintenanceReportHistoryPayload.setId(maintenanceReport.getId());
			maintenanceReportHistoryPayload.setNewSensorId(maintenanceReport.getNewSensorId());
			maintenanceReportHistoryPayload.setOldSensorId(maintenanceReport.getOldSensorId());
			maintenanceReportHistoryPayload.setResolutionType(maintenanceReport.getResolutionType());
			maintenanceReportHistoryPayload.setSensorType(maintenanceReport.getSensorType());
			maintenanceReportHistoryPayload.setWorkOrder(maintenanceReport.getWorkOrder());
			maintenanceReportHistoryPayload.setMaintenanceLocation(maintenanceReport.getMaintenanceLocation());
			maintenanceReportHistoryPayload.setServiceTime(maintenanceReport.getServiceDateTime());
			maintenanceReportHistoryPayload.setValidationTime(maintenanceReport.getValidationTime());
			maintenanceReportHistoryPayload.setCreatedDate(maintenanceReport.getCreatedDate());
			if (maintenanceReport.getAsset() != null && maintenanceReport.getAsset().getAssignedName() != null) {
				maintenanceReportHistoryPayload.setAssetId(maintenanceReport.getAsset().getAssignedName());
				maintenanceReportHistoryPayload.setAssetUuId(maintenanceReport.getAsset().getUuid());
			}
			if (maintenanceReport.getDevice() != null && maintenanceReport.getDevice().getImei() != null) {
				maintenanceReportHistoryPayload.setDeviceId(maintenanceReport.getDevice().getImei());
			}
			if (maintenanceReport.getOrganisation() != null
					&& maintenanceReport.getOrganisation().getOrganisationName() != null) {
				maintenanceReportHistoryPayload
						.setServiceVendorName(maintenanceReport.getOrganisation().getOrganisationName());
			}
			if (maintenanceReport.getUser() != null && maintenanceReport.getUser().getFirstName() != null) {
				String technician = maintenanceReport.getUser().getFirstName();
				if (maintenanceReport.getUser().getLastName() != null) {
					technician = technician + " " + maintenanceReport.getUser().getLastName();
				}
				maintenanceReportHistoryPayload.setTechnician(technician);
			}
			if (!AppUtility.isEmpty(maintenanceReport.getSensorUuid())) {
				Device sensor = sensorList.stream().filter(e -> e.getUuid().equals(maintenanceReport.getSensorUuid()))
						.findAny().orElse(null);
				if (sensor != null) {
					maintenanceReportHistoryPayload.setSensorName(sensor.getProductName());
					if (!AppUtility.isEmpty(sensor.getStatus())) {
						maintenanceReportHistoryPayload.setStatus(sensor.getStatus().getValue());
					}
				}
			}
			maintenanceReportHistoryPayloads.add(maintenanceReportHistoryPayload);
		});

		return maintenanceReportHistoryPayloads;
	}

	public DeviceResponsePayload deviceToDeviceResponsePayload(Device device) {
		DeviceResponsePayload deviceResponsePayload = new DeviceResponsePayload();
		BeanUtils.copyProperties(device, deviceResponsePayload);
		Organisation organisation = deviceResponsePayload.getOrganisation();
		organisation.setCreatedBy(null);
		organisation.setUpdatedBy(null);
		deviceResponsePayload.setOrganisation(organisation);
		return deviceResponsePayload;
	}
}
