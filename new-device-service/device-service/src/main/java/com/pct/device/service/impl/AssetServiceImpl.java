package com.pct.device.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.pct.common.constant.AssetStatus;
import com.pct.common.dto.ResponseBodyDTO;
import com.pct.common.model.Asset;
import com.pct.common.model.User;
import com.pct.common.util.Context;
import com.pct.common.util.Logutils;
import com.pct.device.controller.AssetController;
import com.pct.device.dto.*;
import com.pct.device.exception.DeviceException;
import com.pct.device.model.Lookup;
import com.pct.device.payload.AssetsPayloadMobile;
import com.pct.device.repository.IAssetRepository;
import com.pct.device.repository.ILookupRepository;
import com.pct.device.service.IAssetAdminService;
import com.pct.device.service.IAssetService;
import com.pct.device.specification.AssetSpecification;
import com.pct.device.util.BeanConverter;
import com.pct.device.util.RestUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AssetServiceImpl implements IAssetService {
	 public static final String className="AssetServiceImpl";
	 private static final Logger LOGGER = LoggerFactory.getLogger(AssetServiceImpl.class);

    @Autowired
    private IAssetRepository assetRepository;

    @Autowired
    private BeanConverter beanConverter;
    
    @Autowired
	private RestUtils restUtils;
    
    @Autowired
    private IAssetAdminService assetAdminService;
    
    @Autowired
    private ILookupRepository assetConfigurationRepository;

    @Override
    public List<AssetDTO> getAssetByIdOrVinNumber(String vin, String assetId, String accountNumber) throws DeviceException {
        List<AssetDTO> assetDtoList = new ArrayList<>();
        List<Asset> assetList = new ArrayList<>();
        if (assetId != null) {
            assetList.add(assetRepository.findAssetByAssetId(assetId, accountNumber));
        } else if (vin != null) {
            assetList.add(assetRepository.findAssetByVinNumber(vin, accountNumber));
        }
        if (assetList.size() > 0)
            assetDtoList = assetList.stream().map(beanConverter::convertAssetToAssetsDto).collect(Collectors.toList());

        return assetDtoList;
    }

    @Override
    public List<AssetDTO> getAsset(String accountNumber, AssetStatus status) throws DeviceException {
        List<AssetDTO> assetDtoList = new ArrayList<>();
        List<Asset> assetList = new ArrayList<Asset>();
        if (status == null) {
            assetList = assetRepository.findByAccountNumber(accountNumber);
        } else {
            assetList = assetRepository.findByAccountNumberAndStatus(accountNumber, status);
        }
        if (assetList.size() > 0)
            assetDtoList = assetList.stream().map(beanConverter::convertAssetToAssetsDto).collect(Collectors.toList());

        return assetDtoList;
    }
    
    public boolean isArgumentNull(String value) {
    	boolean flag=false;
    	if(value!=null && !value.equalsIgnoreCase("") && !value.isEmpty()) {
    		flag=true;;
    	}
    	return flag;
    }

    @Override
    public List<AssetResponseDTO> getAssets(String accountNumber, String vin, String assignedName, String status, String eligibleGateway,Context context) throws Exception {
    	List<AssetResponseDTO> assetResponseDtoList = new ArrayList<>();
    	 String methodName = "getAssets";
    	List<Asset> assetList = new ArrayList<>();
        Specification<Asset> spc = AssetSpecification.getAssetListSpecification(accountNumber, vin, assignedName, status, eligibleGateway);
		assetList = assetRepository.findAll(spc);
		Logutils.log(className,methodName,context.getLogUUId()," list of array get from the findAll method ",LOGGER, context.getLogUUId());
        Logutils.log(className,methodName,context.getLogUUId()," After calling repository findAll method from assetRepository ",LOGGER, context.getLogUUId(),accountNumber);
            if (assetList.size() < 1) {
                throw new DeviceException("No such asset found");
            }
            for (Asset asset : assetList) {
                AssetResponseDTO assetResponseDto = beanConverter.convertAssetToAssetResponseDTO(asset, null);
                assetResponseDtoList.add(assetResponseDto);
            }
            Logutils.log(className,methodName,context.getLogUUId(),"completed coverting reponse ",LOGGER, context.getLogUUId(),accountNumber);
        
        
        return assetResponseDtoList;
    }
    
    @Override
    public List<AssetResponseDTO> getAssetsForCAN(String accountNumber, String vin, String assignedName, String status, String eligibleGateway,Context context) throws Exception {
    	List<AssetResponseDTO> assetResponseDtoList = new ArrayList<>();
        List<Asset> assetList = new ArrayList<>();
        String methodName = "getAssetsForCAN";
        //List<AssetListResponseDTO> assetListResponseDTO = new ArrayList<>();
        Specification<Asset> spc = AssetSpecification.getAssetListSpecification(accountNumber, vin, assignedName, status, eligibleGateway);
		/*if(isArgumentNull(accountNumber) && !isArgumentNull(vin) && !isArgumentNull(assignedName) && !isArgumentNull(status) && !isArgumentNull(eligibleGateway) ) {
			assetListResponseDTO = assetRepository.findAllForCustomDTO(accountNumber); 
			if (assetListResponseDTO.size() < 1) {
		        throw new DeviceException("No such asset found");
		    }
			for (AssetListResponseDTO asset : assetListResponseDTO) {
		         AssetResponseDTO assetResponseDto = beanConverter.convertAssetListResponseDTOToAssetResponseDTO(asset);
		         assetResponseDtoList.add(assetResponseDto);
		     }
			
		} else {*/
        if(accountNumber != null) {
        	StopWatch stopWatch  = new StopWatch();
      		stopWatch.start();
      		LOGGER.info(stopWatch.prettyPrint());
      		Logutils.log(className,methodName,context.getLogUUId()," Before calling repository method from assetRepository ",LOGGER, context.getLogUUId(),accountNumber,stopWatch.prettyPrint());
        	List<Object[]> records = assetRepository.getAssetDetailsByCompanyAccountNo(accountNumber);
        	stopWatch.stop();
        	Logutils.log(className,methodName,context.getLogUUId()," after getting response from assetRepository ",LOGGER,context.getLogUUId(), accountNumber,stopWatch.prettyPrint());
            if(records != null && records.size() > 0) {
            	stopWatch.start();
            	Logutils.log(className,methodName,context.getLogUUId()," time requires to changes the response in responseBody ",LOGGER,context.getLogUUId(), accountNumber,stopWatch.prettyPrint());
            	for(int i = 0; i < records.size(); i++) {
            		Object[] assetDetails = records.get(i);
            		AssetResponseDTO assetResponseDto = beanConverter.convertAssetArrayToAssetResponseDTO(assetDetails);
            		assetResponseDtoList.add(assetResponseDto);
            	}
            	stopWatch.stop();
            	Logutils.log(className,methodName,context.getLogUUId(),"after time requires to changes the response in responseBody ",LOGGER,context.getLogUUId(), accountNumber,stopWatch.prettyPrint());
            }
        } else {
        	assetList = assetRepository.findAll(spc);
        }
        	
//            if (assetList.size() < 1) {
//                throw new DeviceException("No such asset found");
//            }
//            for (Asset asset : assetList) {
//                AssetResponseDTO assetResponseDto = beanConverter.convertAssetToAssetResponseDTO(asset, null);
//                assetResponseDtoList.add(assetResponseDto);
//            }
        //}
        
        return assetResponseDtoList;
    }


    @Override
    public AssetVinSearchDTO getAssetVinSearch(String vin) throws Exception {
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
    public ResponseBodyDTO<AssetResponseDTO> addAsset(AssetsPayloadMobile assetsPayloadMobile, Long userId, Boolean isMoblieApi) throws DeviceException, JsonProcessingException {
        User user = restUtils.getUserFromAuthService(userId);
        Asset asset = beanConverter.assetsPayloadToAssets(assetsPayloadMobile, true, user);
        List<Asset> assetList = new ArrayList<>();
        assetList.add(asset);
        Map<String, List<String>> failedAssets = assetAdminService.validationOfAssets(assetList, beanConverter.companyToCompanyPayload(asset.getCompany()), false, true, userId, isMoblieApi);
        List<Asset> byVinNumber = assetRepository.findByAssignedName(assetsPayloadMobile.getAssignedName(),assetsPayloadMobile.getCan());
        AssetResponseDTO assetResponseDTO = beanConverter.convertAssetToAssetResponseDTO(byVinNumber.get(0), null);
        ResponseBodyDTO<AssetResponseDTO> response = null;
        if (failedAssets.size() > 0) {
            response = new ResponseBodyDTO<>(false, failedAssets.keySet().iterator().next(), assetResponseDTO);
        }
        response = new ResponseBodyDTO<>(true, "Asset added successfully", assetResponseDTO);
        return response;
    }
    
    
}
