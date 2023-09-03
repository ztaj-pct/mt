package com.pct.device.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.pct.device.bean.*;
import com.pct.device.payload.*;
import com.pct.device.service.device.AssetToDevice;
import com.pct.common.constant.InstallHistoryStatus;
import com.pct.common.constant.InstanceType;
import com.pct.common.model.*;
import com.pct.device.repository.projections.AssetGatewayView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pct.common.constant.AssetCategory;
import com.pct.common.constant.AssetStatus;
import com.pct.common.constant.InstallHistoryStatus;
import com.pct.common.payload.InventoryResponse;
import com.pct.common.payload.JobSummaryResponse;
import com.pct.common.payload.PrePairSensorsForAssetRequest;
import com.pct.common.payload.PrePairSensorsForAssetUpdateRequest;
import com.pct.common.payload.ProductMasterRequest;
import com.pct.common.payload.ProductMasterResponse;
import com.pct.common.payload.SensorListForPrePair;
import com.pct.common.payload.SensorListForPrePairForUpdate;
import com.pct.common.payload.SubSensorList;
import com.pct.common.payload.SubSensorListForUpdate;
import com.pct.device.bean.GatewayBean;
import com.pct.device.bean.HubAssetDetail;
import com.pct.device.bean.HubSensor;
import com.pct.device.bean.SensorBean;
import com.pct.device.bean.ShippedDevicesHubRequest;
import com.pct.device.dto.AssetDTO;
import com.pct.device.dto.AssetListResponseDTO;
import com.pct.device.dto.AssetResponseDTO;
import com.pct.device.dto.CustomerDTO;
import com.pct.device.dto.ManufacturerDetailsDTO;
import com.pct.device.model.AssetRecord;
import com.pct.device.model.GatewayShippingDetails;
import com.pct.device.model.Lookup;
import com.pct.device.payload.AssetRecordPayload;
import com.pct.device.payload.AssetsPayload;
import com.pct.device.payload.AssetsPayloadMobile;
import com.pct.device.payload.CompanyPayload;
import com.pct.device.payload.CreateCompanyPayload;
import com.pct.device.payload.LookupPayload;
import com.pct.device.payload.Message;
import com.pct.device.payload.ShippedDevice;
import com.pct.device.payload.ShippedDevicesRequest;
import com.pct.device.repository.ILookupRepository;
import com.pct.device.repository.IManufacturerDetailsRepository;
import com.pct.device.repository.IManufacturerRepository;
import com.pct.device.repository.ISubSensorRepository;
import com.pct.device.repository.projections.ApprovedAssetTypeCountView;
import com.pct.device.repository.projections.AssetGatewayView;
import com.pct.device.repository.projections.GatewayTypeCountView;
import com.pct.device.repository.projections.InProgressAssetTypeAndCountView;
import com.pct.device.service.VinDecoderService;
import com.pct.device.service.impl.AssetAdminServiceImpl;

@Component
public class BeanConverter {

    private final String STATUS = "PENDING";

    @Autowired
    private IManufacturerRepository manufacturerRepository;
    @Autowired
    private IManufacturerDetailsRepository manufacturerDetailsRepository;
    @Autowired
    private ILookupRepository assetConfigurationRepository;
    @Autowired
    private VinDecoderService vinDecoderService;
    @Autowired
    private RestUtils restUtils;
    @Autowired
    private ISubSensorRepository isubsensorRepo;

    public static String mapToJson(Object object) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(object);

    }
    
   

    public CompanyPayload companyToCompanyPayload(Company com) {
        CompanyPayload comp = new CompanyPayload();
        comp.setCompanyName(com.getCompanyName());
        comp.setId(com.getId());
        comp.setStatus(com.getIsActive());
        comp.setAccountNumber(com.getAccountNumber());
        comp.setIsAssetListRequired(com.getIsAssetListRequired());
        comp.setUuid(com.getUuid());
        return comp;
    }

    public Company companyPayloadToCompanies(CompanyPayload com) {
        Company comp = new Company();
        comp.setCompanyName(com.getCompanyName());
        comp.setId(com.getId());
        comp.setIsActive(com.getStatus());
        comp.setAccountNumber(com.getAccountNumber());
        comp.setIsAssetListRequired(com.getIsAssetListRequired());
        comp.setUuid(com.getUuid());
        return comp;
    }
    
    // 

    public Company createCompanyPayloadToCompany(CreateCompanyPayload createCompanyPayload) {
        Company company = new Company();
        company.setCompanyName(createCompanyPayload.getCompanyName());
        return company;
    }

    public LookupPayload assetConfigurationToAssetConfigurationPayload(Lookup lookup) {
        LookupPayload lookupPayload = new LookupPayload();
        lookupPayload.setId(lookup.getId());
        lookupPayload.setField(lookup.getField());
        lookupPayload.setValue(lookup.getValue());
        lookupPayload.setDisplayLabel(lookup.getDisplayLabel());
        return lookupPayload;
    }

    public Lookup assetConfigurationPayloadToAssetConfiguration(LookupPayload lookupPayload) {
        Lookup lookup = new Lookup();
        lookup.setId(lookupPayload.getId());
        lookup.setField(lookupPayload.getField());
        return lookup;
    }
    
    public Lookup lookupPayloadToLookup(LookupPayload lookupPayload) {
        Lookup lookup = new Lookup();
        lookup.setField(lookupPayload.getField());
        lookup.setValue(lookupPayload.getValue());
        lookup.setDisplayLabel(lookupPayload.getDisplayLabel());
        return lookup;
    }
    

    public AssetDTO convertAssetToAssetsDto(Asset asset) {
        AssetDTO assetDto = new AssetDTO();
        assetDto.setId(asset.getId());
        assetDto.setAssignedName(asset.getAssignedName());
        assetDto.setGatewayEligibility(asset.getGatewayEligibility());
        assetDto.setVin(asset.getVin());
        assetDto.setYear(asset.getYear());
        assetDto.setManufacturer(asset.getManufacturer().getName());
        assetDto.setAccountNumber(asset.getCompany().getAccountNumber());
        assetDto.setCategory(asset.getCategory().getValue());
        assetDto.setStatus(asset.getStatus().getValue());
        assetDto.setCreatedBy(asset.getCreatedBy().getEmail());
        assetDto.setUpdatedBy(asset.getUpdatedBy().getEmail());
        assetDto.setDateCreated(asset.getCreatedOn());
        assetDto.setDateUpdated(asset.getUpdatedOn());
        assetDto.setIsVinValidated(asset.getIsVinValidated());
        assetDto.setComment(asset.getComment());
        return assetDto;
    }

    public JobSummaryResponse createJobSummaryResponseFromProjections
            (List<GatewayTypeCountView> gatewayReadyForInstallation,
             List<ApprovedAssetTypeCountView> assetsApprovedForInstallation,
             List<InProgressAssetTypeAndCountView> inProgressInstallation) {

        Map<String, Integer> gatewayTypeToCount = gatewayReadyForInstallation.stream()
                .collect(Collectors.toMap(GatewayTypeCountView::getProduct_code, GatewayTypeCountView::getCount));
        Map<String, Integer> assetTypeToCount = assetsApprovedForInstallation.stream()
                .collect(Collectors.toMap(ApprovedAssetTypeCountView::getAsset_Type1, ApprovedAssetTypeCountView::getCount));
        Map<String, Integer> inProgressAssetTypeToCount = inProgressInstallation.stream()
                .collect(Collectors.toMap(InProgressAssetTypeAndCountView::getAsset_Type1, InProgressAssetTypeAndCountView::getCount));
        JobSummaryResponse jobSummaryResponse = new JobSummaryResponse(gatewayTypeToCount, assetTypeToCount, inProgressAssetTypeToCount);
        return jobSummaryResponse;
    }

    public InventoryResponse createInventoryResponseFromProjections
            (List<GatewayTypeCountView> gatewayReadyForInstallation,
             List<ApprovedAssetTypeCountView> assetsApprovedForInstallation) {
        Map<String, Integer> gatewayTypeToCount = gatewayReadyForInstallation.stream()
                .collect(Collectors.toMap(GatewayTypeCountView::getProduct_code, GatewayTypeCountView::getCount));
        Map<String, Integer> assetTypeToCount = assetsApprovedForInstallation.stream()
                .collect(Collectors.toMap(ApprovedAssetTypeCountView::getAsset_Type1, ApprovedAssetTypeCountView::getCount));
        InventoryResponse inventoryResponse = new InventoryResponse();
        inventoryResponse.setAssets(assetTypeToCount);
        inventoryResponse.setGateways(gatewayTypeToCount);
        return inventoryResponse;
    }

    public CustomerDTO convertCompanyToCustomerDTO(Company company, List<GatewayShippingDetails> shipmentDetails) {
        CustomerDTO customerDto = new CustomerDTO();
        customerDto.setAccountNumber(company.getAccountNumber());
        customerDto.setName(company.getCompanyName());
        List<String> shipmentAddresses = shipmentDetails.stream().map(GatewayShippingDetails::getAddressShipped).collect(Collectors.toList());
        customerDto.setShipToLocations(shipmentAddresses);
        return customerDto;
    }
    
    public AssetResponseDTO convertAssetListResponseDTOToAssetResponseDTO(AssetListResponseDTO asset) throws JsonProcessingException {
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
        manufacturerDetailsDTO.setYear(asset.getYear());
        assetResponseDto.setManufacturerDetails(manufacturerDetailsDTO);
        assetResponseDto.setEligibleGateway(asset.getEligibleGateway());
        assetResponseDto.setCan(asset.getCan());
        List<Lookup> lookups = assetConfigurationRepository.findByField(asset.getCategory().getValue());
        assetResponseDto.setDisplayName(lookups.get(0).getValue());
        if (asset.getDatetimeCreated() != null)
            assetResponseDto.setDatetimeCreated(asset.getDatetimeCreated().toString());
        if (asset.getDatetimeUpdated() != null)
            assetResponseDto.setDatetimeUpdated(asset.getDatetimeUpdated().toString());
        assetResponseDto.setCompanyName(asset.getCompanyName());
        assetResponseDto.setIsVinValidated(asset.getIsVinValidated());
        assetResponseDto.setComment(asset.getComment());
        return assetResponseDto;
    }

    
    
   
    
    public AssetResponseDTO convertAssetToAssetResponseDTO(Asset asset, InstallHistory installHistory) throws JsonProcessingException {
        AssetResponseDTO assetResponseDto = new AssetResponseDTO();
        assetResponseDto.setAssetUuid(asset.getUuid());
        assetResponseDto.setAssignedName(asset.getAssignedName());
        assetResponseDto.setStatus(asset.getStatus().getValue());
        assetResponseDto.setCategory(asset.getCategory().getValue());
        assetResponseDto.setVin(asset.getVin());
        ManufacturerDetailsDTO manufacturerDetailsDTO = new ManufacturerDetailsDTO();
        if (asset.getManufacturer() != null && asset.getManufacturer().getName() != null) {
            manufacturerDetailsDTO.setMake(asset.getManufacturer().getName());
        }
        if (asset.getManufacturerDetails() != null) {
            manufacturerDetailsDTO.setModel(asset.getManufacturerDetails().getModel());
        }
        manufacturerDetailsDTO.setYear(asset.getYear());
        assetResponseDto.setManufacturerDetails(manufacturerDetailsDTO);
        assetResponseDto.setEligibleGateway(asset.getGatewayEligibility());
        assetResponseDto.setCan(asset.getCompany().getAccountNumber());
        List<Lookup> lookups = assetConfigurationRepository.findByField(asset.getCategory().getValue());
        if(lookups != null && lookups.size() > 0) {
        	assetResponseDto.setDisplayName(lookups.get(0).getValue());
        }
        if (asset.getCreatedOn() != null)
            assetResponseDto.setDatetimeCreated(asset.getCreatedOn().toString());
        if (asset.getUpdatedOn() != null)
            assetResponseDto.setDatetimeUpdated(asset.getUpdatedOn().toString());
        assetResponseDto.setCompanyName(asset.getCompany().getCompanyName());
        assetResponseDto.setIsVinValidated(asset.getIsVinValidated());
        assetResponseDto.setComment(asset.getComment());
        if(installHistory != null) {
            assetResponseDto.setImei(installHistory.getGateway().getImei());
            if(installHistory.getStatus().equals(InstallHistoryStatus.FINISHED)) {
                assetResponseDto.setInstalled(installHistory.getDateEnded().toString());
            }
        }
        return assetResponseDto;
    }
    
    public GatewayBean convertGatewayToGatewayBean(Gateway gateway, List<GatewaySensorXref> gatewaySensorXrefList) {
        GatewayBean gatewayBean = new GatewayBean();
        gatewayBean.setGatewayUuid(gateway.getUuid());
        gatewayBean.setDateCreated(gateway.getCreatedOn());
        gatewayBean.setDateUpdated(gateway.getUpdatedOn());
        gatewayBean.setImei(gateway.getImei());
        gatewayBean.setMacAddress(gateway.getMacAddress());
        gatewayBean.setProductCode(gateway.getProductCode());
        gatewayBean.setProductName(gateway.getProductName());
        gatewayBean.setStatus(gateway.getStatus().getValue());
        gatewayBean.setCan(gateway.getCompany().getAccountNumber());
        gatewayBean.setType(gateway.getType().getValue());
        List<SensorBean> sensorBeanList = new ArrayList<>();
        gatewaySensorXrefList.forEach(gatewaySensorXref -> {
            Sensor sensor = gatewaySensorXref.getSensor();
            SensorBean sensorBean = new SensorBean();
            sensorBean.setCan(sensor.getGateway().getCompany().getAccountNumber());
            sensorBean.setSensorUuid(sensor.getUuid());
            sensorBean.setProductCode(sensor.getProductCode());
            sensorBean.setStatus(sensor.getStatus().getValue());
            sensorBean.setDatetimeCreated(sensor.getCreatedOn());
            sensorBean.setDatetimeUpdated(sensor.getUpdatedOn());
            sensorBean.setGatewayUuid(gateway.getUuid());
            sensorBean.setMacAddress(sensor.getMacAddress());
            List<Lookup> lookups = assetConfigurationRepository.findByField(sensor.getProductCode());
            if(lookups.size() > 0) {
                sensorBean.setDisplayName(lookups.get(0).getValue());
            }
            sensorBeanList.add(sensorBean);
        });
        gatewayBean.setSensorList(sensorBeanList);
        return gatewayBean;
    }

    public SensorBean convertSensorToSensorBean(Sensor sensor) {
        SensorBean sensorBean = new SensorBean();
        sensorBean.setDatetimeCreated(sensor.getCreatedOn());
        sensorBean.setDatetimeUpdated(sensor.getUpdatedOn());
        if (sensor.getGateway() != null)
            sensorBean.setGatewayUuid(sensor.getGateway().getUuid());
        sensorBean.setMacAddress(sensor.getMacAddress());
        sensorBean.setProductCode(sensor.getProductCode());
        sensorBean.setSensorUuid(sensor.getUuid());
        sensorBean.setStatus(sensor.getStatus().getValue());
        sensorBean.setCan(sensor.getGateway().getCompany().getAccountNumber());
        List<Lookup> lookups = assetConfigurationRepository.findByField(sensor.getProductCode());
        sensorBean.setDisplayName(lookups.get(0).getValue());
        return sensorBean;
    }
    

    // migration methods

    public Asset assetsPayloadToAssets(AssetsPayload assetsPayload, CompanyPayload companyPayload, Boolean isCreated, User user, Map<String, List<String>> validationFailedAssets) {
        Asset asset = new Asset();
        asset.setAssignedName(assetsPayload.getAssignedName());
        asset.setCategory(AssetCategory.findByValue(assetsPayload.getCategory().toUpperCase()));
        asset.setGatewayEligibility(assetsPayload.getEligibleGateway());
        if(assetsPayload.getManufacturer() != null && !assetsPayload.getManufacturer().isEmpty()) {
            Manufacturer manufacturer = manufacturerRepository.findByName(assetsPayload.getManufacturer());
            if (manufacturer != null) {
                asset.setManufacturer(manufacturer);
            } else {
                if (validationFailedAssets != null) {
                    if (validationFailedAssets.get(AssetAdminServiceImpl.ASSET_ERRORS[12]) != null) {
                        validationFailedAssets.get(AssetAdminServiceImpl.ASSET_ERRORS[12]).add(asset.getAssignedName());
                    } else {
                        List<String> failedAssetAssignedName = new ArrayList<>();
                        failedAssetAssignedName.add(asset.getAssignedName());
                        validationFailedAssets.put(AssetAdminServiceImpl.ASSET_ERRORS[12], failedAssetAssignedName);
                    }
                }
            }
        }
        asset.setYear(assetsPayload.getYear());
        asset.setVin(assetsPayload.getVin());
        asset.setId(assetsPayload.getId());
        if(assetsPayload.getStatus() == null){
        	asset.setStatus(AssetStatus.PENDING);
        } else {
        	asset.setStatus(AssetStatus.getAssetStatus(assetsPayload.getStatus()));
        }
        asset.setCompany(companyPayloadToCompanies(companyPayload));
        asset.setUuid(assetsPayload.getUuid());
        if (isCreated) {
            asset.setCreatedBy(user);
        }
        asset.setUpdatedBy(user);
        return asset; 
    }

    public AssetsPayload assetsassetsToPayload(Asset asset) {

        AssetsPayload assetsPayload = new AssetsPayload();
        assetsPayload.setAssignedName(asset.getAssignedName());
        assetsPayload.setCategory(asset.getCategory().getValue());
        assetsPayload.setEligibleGateway(asset.getGatewayEligibility());
        if(asset.getManufacturer() != null)
        assetsPayload.setManufacturer(asset.getManufacturer().getName());
        assetsPayload.setYear(asset.getYear());
        assetsPayload.setVin(asset.getVin());
        assetsPayload.setId(asset.getId());
        assetsPayload.setCompany(companyToCompanyPayload(asset.getCompany()));
        assetsPayload.setUuid(asset.getUuid());
        assetsPayload.setStatus(asset.getStatus().getValue());
        // System.out.println(asset.getSensor().iterator().next().getId());
        // Set<SensorPayload>sensorSet=asset.getSensor().stream().map(this ::
        // SensorToSensorPayload).collect(Collectors.toSet());
        // asset.setSensor(sensorSet);
        assetsPayload.setVinValidated(asset.getIsVinValidated());
        assetsPayload.setComment(asset.getComment());
        return assetsPayload;
    }

    public Asset assetsPayloadToAssets(AssetsPayloadMobile assetsPayloadMobile, boolean isCreated, User user) {
        Asset asset = new Asset();
        asset.setAssignedName(assetsPayloadMobile.getAssignedName());
        asset.setVin(assetsPayloadMobile.getVin());
        asset.setStatus(AssetStatus.PENDING);
        asset.setCategory(AssetCategory.findByValue(assetsPayloadMobile.getAssetType().toUpperCase()));
        Company company = restUtils.getCompanyFromCompanyService(assetsPayloadMobile.getCan());
        asset.setCompany(company);
        asset.setUuid(UUID.randomUUID().toString());

        if (isCreated) {
            asset.setCreatedBy(user);
        }
        asset.setUpdatedBy(user);
        return asset;
    }

    public Page<AssetRecordPayload> convertAssetRecordToAssetRecordPayload(Page<AssetRecord> customerAssets, Pageable pageable) {
        List<AssetRecordPayload> assetRecordPayloadList = new ArrayList<>();
        customerAssets.forEach(customerAsset -> {
            AssetRecordPayload assetRecordPayload = new AssetRecordPayload();
            assetRecordPayload.setCompanyId(customerAsset.getCompanyId());
            assetRecordPayload.setCompanyName(customerAsset.getCompanyName());
            assetRecordPayload.setCount(customerAsset.getCount());
            assetRecordPayload.setCreatedAt(customerAsset.getCreatedAt());
            assetRecordPayload.setUpdatedAt(customerAsset.getUpdatedAt());
            assetRecordPayload.setCreatedFirstName(customerAsset.getCreatedFirstName());
            assetRecordPayload.setCreatedLastName(customerAsset.getCreatedLastName());
            assetRecordPayload.setUpdatedFirstName(customerAsset.getUpdatedFirstName());
            assetRecordPayload.setUpdatedLastName(customerAsset.getUpdatedLastName());
            assetRecordPayloadList.add(assetRecordPayload);
        });
        Page<AssetRecordPayload> page = new PageImpl<>(assetRecordPayloadList, pageable, assetRecordPayloadList.size());
        return page;
    }

    public ShippedDevicesHubRequest convertShippedDevicesToShippedDevicesHubRequest(ShippedDevicesRequest shippedDevicesRequest) {
        ShippedDevicesHubRequest shippedDevicesHubRequest = new ShippedDevicesHubRequest();
        shippedDevicesHubRequest.setEpicorOrderNumber(shippedDevicesRequest.getShippedDevices().getShippedDeviceList().get(0).getEpicorOrderId());
        shippedDevicesHubRequest.setSalesforceAccountId(shippedDevicesRequest.getShippedDevices().getShippedDeviceList().get(0).getSalesforceAccountId());
        shippedDevicesHubRequest.setSalesforceOrderNumber(shippedDevicesRequest.getShippedDevices().getShippedDeviceList().get(0).getSalesforceOrderNumber());
        shippedDevicesHubRequest.setPackingSlipNumber(shippedDevicesRequest.getShippedDevices().getShippedDeviceList().get(0).getPackingSlipNumber());
        List<HubAssetDetail> hubAssetDetailList = new ArrayList<>();
        shippedDevicesRequest.getShippedDevices().getShippedDeviceList().forEach(shippedDevice ->{
            HubAssetDetail hubAssetDetail = new HubAssetDetail();
            hubAssetDetail.setImeiList(shippedDevice.getImei());
            hubAssetDetail.setProductCode(shippedDevice.getProductCode());
            hubAssetDetail.setQuantityShipped(String.valueOf(shippedDevice.getQuantityShipped()));
            hubAssetDetail.setOrderItemNumber(shippedDevice.getOrderItemNumber());
            hubAssetDetailList.add(hubAssetDetail);
        });
        shippedDevicesHubRequest.setHubAssetDetails(hubAssetDetailList);
        return shippedDevicesHubRequest;
    }

    public AssetResponseDTO convertAssetGatewayViewToAssetResponse(AssetGatewayView assetGatewayView) {
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
            ManufacturerDetails manufacturerDetails = manufacturerDetailsRepository.findByUuid(assetGatewayView.getManufacturer_details_uuid());
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
        Company company = restUtils.getCompanyFromCompanyService(assetGatewayView.getAccount_number());
        assetResponseDto.setCompanyName(company.getCompanyName());
        assetResponseDto.setIsVinValidated(assetGatewayView.getIs_vin_validated());
        assetResponseDto.setComment(assetGatewayView.getComment());
        return assetResponseDto;
    }

    public Map<String, InstallHistory> createMapFromListOfInstallHistory(List<InstallHistory> installHistories) {
        Map<String, InstallHistory> map = new HashMap<>();
        for(InstallHistory ih : installHistories) {
            map.put(ih.getAsset().getUuid(), ih);
        }
        return map;
    }
    
    public ProductMaster convertProductMasterRequestToProductMasterBean(ProductMasterRequest productMasterRequest) {
    	ProductMaster productMaster = new ProductMaster();
    	productMaster.setProductCode(productMasterRequest.getProductCode());
    	productMaster.setProductName(productMasterRequest.getProductName());
    	productMaster.setSubtype(productMasterRequest.getSubtype());
    	productMaster.setType(productMasterRequest.getType());
    	productMaster.setBlocker(productMasterRequest.isBlocker());
    	return productMaster;
    }
    
    public List<ProductMasterResponse> convertProductMasterToProductMasteResponse(List<ProductMaster> productMasterList) {
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
    
    public AssetResponseDTO convertAssetArrayToAssetResponseDTO(Object[] assetDetails) throws JsonProcessingException {
        AssetResponseDTO assetResponseDto = new AssetResponseDTO();
        assetResponseDto.setAssetUuid(String.valueOf(assetDetails[15]));
        assetResponseDto.setAssignedName(String.valueOf(assetDetails[3]));
        return assetResponseDto;
    }
	public PrePairSensorsForAssetUpdateRequest convertPreParingResposne(List<AssetSensorXref> listOfAssetSensorXref,
			SensorHardwareConfig conf, String assetUuid) {
		PrePairSensorsForAssetUpdateRequest prep = new PrePairSensorsForAssetUpdateRequest();
		prep.setAssetUuid(assetUuid);
		List<SensorListForPrePairForUpdate> sensorList = new ArrayList<>();
		prep.setAssetUuid(conf.getAssetUuid());
		prep.setIsDoor(conf.getIsDoor());
		prep.setIsMicrosp(conf.getIsMircoSp());
		prep.setIsWireless(conf.getIsWired());
		if(listOfAssetSensorXref != null && listOfAssetSensorXref.size() > 0) {
			for (AssetSensorXref asx : listOfAssetSensorXref) {
				SensorListForPrePairForUpdate sen = new SensorListForPrePairForUpdate();
				sen.setProductCode(asx.getSensor().getProductCode());
				sen.setProductName(asx.getSensor().getProductName());
				sen.setUuid(asx.getSensor().getUuid());
				if (asx.getSensor().getMacAddress() != null) {
					sen.setMacAddress(asx.getSensor().getMacAddress());
				}
				List<SubSensor> subsens = isubsensorRepo.findBySensorUuid(asx.getSensor().getUuid());
				if (subsens != null && subsens.size() > 0) {
					List<SubSensorListForUpdate> subSensorList = new ArrayList<>();
					for (SubSensor ss : subsens) {
						SubSensorListForUpdate sebs = new SubSensorListForUpdate();
						sebs.setInstanceType(ss.getInstanceType());
						sebs.setSubSensorId(ss.getSubSensorId());
						sebs.setType(ss.getType());
						sebs.setUuid(ss.getUuid());
						subSensorList.add(sebs);
					}
					sen.setSubSensorList(subSensorList);
				}
				sensorList.add(sen);
			}
			prep.setSensorList(sensorList);
		}
		return prep;

	}
}
