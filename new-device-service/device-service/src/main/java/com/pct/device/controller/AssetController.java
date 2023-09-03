package com.pct.device.controller;

import com.pct.common.controller.IApplicationController;
import com.pct.common.dto.ResponseBodyDTO;
import com.pct.common.dto.ResponseDTO;
import com.pct.common.util.Context;
import com.pct.common.payload.PrePairSensorsForAssetRequest;
import com.pct.common.payload.PrePairSensorsForAssetUpdateRequest;
import com.pct.common.util.JwtUtil;
import com.pct.common.util.Logutils;
import com.pct.device.dto.AssetResponseDTO;
import com.pct.device.dto.AssetUpdateDTO;
import com.pct.device.dto.AssetVinSearchDTO;
import com.pct.device.model.Lookup;
import com.pct.device.payload.AssetsPayloadMobile;
import com.pct.device.service.IAssetService;
import com.pct.device.service.IDeviceService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StopWatch;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import java.util.List;


@RestController
@RequestMapping("/asset")
public class AssetController implements IApplicationController<Lookup> {
	  public static final String className="AssetController";
    private static final Logger LOGGER = LoggerFactory.getLogger(AssetController.class);

    @Autowired
    private IAssetService assetService;
    
    @Autowired
	private JwtUtil jwtutil;
    
    @Autowired
    IDeviceService deviceService;

   /* @GetMapping()
    public ResponseEntity<ResponseBodyDTO<List<AssetDTO>>> getAsset(@RequestParam(value = "account-number", required = true) String accountNumber,
                                                                    @RequestParam(value = "status", required = false) AssetStatus status) {
        try {
            List<AssetDTO> assetsDtoList = assetService.getAsset(accountNumber, status);
            return new ResponseEntity<ResponseBodyDTO<List<AssetDTO>>>(
                    new ResponseBodyDTO<List<AssetDTO>>(true, "Fetched asset(s) successfully", assetsDtoList), HttpStatus.OK);
        } catch (Exception exception) {
            logger.error("Exception occurred while getting assets", exception);
            return new ResponseEntity<ResponseBodyDTO<List<AssetDTO>>>(
                    new ResponseBodyDTO<List<AssetDTO>>(false, exception.getMessage(), null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }*/

    @GetMapping("/vin-search")
    public ResponseEntity<ResponseBodyDTO<AssetVinSearchDTO>> getAssetVinSearch(@RequestParam(value = "vin", required = true) String vin) {
        try {
            AssetVinSearchDTO assetVinSearchDto = assetService.getAssetVinSearch(vin);

            return new ResponseEntity<ResponseBodyDTO<AssetVinSearchDTO>>(
                    new ResponseBodyDTO<AssetVinSearchDTO>(true, "Fetched asset successfully", assetVinSearchDto), HttpStatus.OK);
        } catch (Exception exception) {
            logger.error("Exception occurred while getting asset", exception);
            return new ResponseEntity<ResponseBodyDTO<AssetVinSearchDTO>>(
                    new ResponseBodyDTO<AssetVinSearchDTO>(false, exception.getMessage(), null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping()
    public ResponseEntity<ResponseBodyDTO<List<AssetResponseDTO>>> getAssets(@RequestParam(value = "can", required = false) String accountNumber,
                                                                             @RequestParam(value = "vin", required = false) String vin,
                                                                             @RequestParam(value = "assigned-name", required = false) String assignedName,
                                                                             @RequestParam(value = "status", required = false) String status,
                                                                             @RequestParam(value = "eligible-gateway", required = false) String eligibleGateway) {
    	  Context context = new Context();
    	   String methodName = "getAssets";
    	try {
    		Logutils.log(className,methodName,context.getLogUUId()," Before calling getAssets method from assetService",logger, context.getLogUUId(), accountNumber);
            List<AssetResponseDTO> assetResponseDtoList = assetService.getAssets(accountNumber, vin, assignedName, status, eligibleGateway,context);
            Logutils.log(className,methodName,context.getLogUUId()," after calling getAssets method from assetService",logger, context.getLogUUId(), accountNumber);
            return new ResponseEntity<ResponseBodyDTO<List<AssetResponseDTO>>>(
                    new ResponseBodyDTO<List<AssetResponseDTO>>(true, "Fetched asset(s) successfully", assetResponseDtoList), HttpStatus.OK);
        } catch (Exception exception) {
            logger.error("Exception occurred while getting asset", exception);
            return new ResponseEntity<ResponseBodyDTO<List<AssetResponseDTO>>>(
                    new ResponseBodyDTO<List<AssetResponseDTO>>(false, exception.getMessage(), null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping()
    public ResponseEntity<ResponseDTO> updateAsset(@Validated @RequestBody AssetUpdateDTO assetUpdateDto) {
        try {
            Boolean status = Boolean.TRUE;
            if (status) {
                return new ResponseEntity<ResponseDTO>(new ResponseDTO(status, "Asset updated successfully"),
                        HttpStatus.OK);
            } else {
                return new ResponseEntity<ResponseDTO>(new ResponseDTO(status, "Getting error while updating asset"),
                        HttpStatus.BAD_GATEWAY);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<ResponseDTO>(new ResponseDTO(Boolean.FALSE, e.getMessage()),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @PostMapping("/quick-add")
    public ResponseEntity<ResponseBodyDTO<AssetResponseDTO>> quickAdd(@Validated @RequestBody AssetsPayloadMobile assetsPayloads, HttpServletRequest httpServletRequest) {
        LOGGER.info("Inside addAsset");
        try {
            Long userId = jwtutil.getUserIdFromRequest(httpServletRequest);
            ResponseBodyDTO<AssetResponseDTO> response = assetService.addAsset(assetsPayloads, userId, Boolean.TRUE);
            return new ResponseEntity<ResponseBodyDTO<AssetResponseDTO>>(response,
                    HttpStatus.CREATED);
        } catch (Exception e) {
        	e.printStackTrace();
            return new ResponseEntity<ResponseBodyDTO<AssetResponseDTO>>(new ResponseBodyDTO<AssetResponseDTO>(Boolean.FALSE, e.getMessage()),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    
    @GetMapping("/for-can")
    public ResponseEntity<ResponseBodyDTO<List<AssetResponseDTO>>> getAssetsByCompany(@RequestParam(value = "can", required = false) String accountNumber,
                                                                             @RequestParam(value = "vin", required = false) String vin,
                                                                             @RequestParam(value = "assigned-name", required = false) String assignedName,
                                                                             @RequestParam(value = "status", required = false) String status,
                                                                             @RequestParam(value = "eligible-gateway", required = false) String eligibleGateway) {
    	Context context = new Context();
   	   String methodName = "getAssetsByCompany";
    	try {
    		StopWatch stopWatch  = new StopWatch();
      		stopWatch.start();
      		logger.info(stopWatch.prettyPrint());
    		 Logutils.log(className,methodName,context.getLogUUId()," Before calling getAssetsForCAN method from assetService method ",logger, context.getLogUUId(), accountNumber,stopWatch.prettyPrint());
            List<AssetResponseDTO> assetResponseDtoList = assetService.getAssetsForCAN(accountNumber, vin, assignedName, status, eligibleGateway,context);
            stopWatch.stop();
            Logutils.log(className,methodName,context.getLogUUId()," after getting response from getAssetsForCAN method from assetService method ",logger, context.getLogUUId(),accountNumber,stopWatch.prettyPrint());
            return new ResponseEntity<ResponseBodyDTO<List<AssetResponseDTO>>>(
                    new ResponseBodyDTO<List<AssetResponseDTO>>(true, "Fetched asset(s) successfully", assetResponseDtoList), HttpStatus.OK);
        } catch (Exception exception) {
            logger.error("Exception occurred while getting asset", exception);
            return new ResponseEntity<ResponseBodyDTO<List<AssetResponseDTO>>>(
                    new ResponseBodyDTO<List<AssetResponseDTO>>(false, exception.getMessage(), null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @PostMapping("/pre-pair/asset-sensors-association")
    public ResponseEntity<? extends Object> prePairSensorsForAsset(@Valid @RequestBody PrePairSensorsForAssetRequest prePairSensorsForAssetRequest,HttpServletRequest httpServletRequest) {
        try {
        	logger.info("Inside the Device Controller from the prePairSensorsForAsset method");
        	Long userId = jwtutil.getUserIdFromRequest(httpServletRequest);
            Boolean status = deviceService.prePairSensorsForAsset(prePairSensorsForAssetRequest, userId);
            logger.info("Exiting from the Device Controller from the prePairSensorsForAsset method");
            return new ResponseEntity(new ResponseBodyDTO("Successfully saved Sensors for Asset : " + prePairSensorsForAssetRequest.getAssetUuid(), status),
                    HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("Exception while association with asset for sensors", e);
            return new ResponseEntity(new ResponseBodyDTO(e.getMessage(), Boolean.FALSE),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @PutMapping("/pre-pair/asset-sensors-association")
    public ResponseEntity<? extends Object> prePairSensorsForAssetUpdate(@Valid @RequestBody PrePairSensorsForAssetUpdateRequest prePairSensorsForAssetRequest,HttpServletRequest httpServletRequest) {
        try {
        	logger.info("Inside the Device Controller from the prePairSensorsForAsset method");
        	Long userId = jwtutil.getUserIdFromRequest(httpServletRequest);
            Boolean status = deviceService.prePairSensorsForAssetUpdate(prePairSensorsForAssetRequest, userId);
            logger.info("Exiting from the Device Controller from the prePairSensorsForAsset method");
            return new ResponseEntity(new ResponseBodyDTO("Successfully updated Sensors for Asset : " + prePairSensorsForAssetRequest.getAssetUuid(), status),
                    HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Exception while association with asset for sensors", e);
            return new ResponseEntity(new ResponseBodyDTO(e.getMessage(), Boolean.FALSE),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @GetMapping("/pre-pair/asset-sensors-association/{assetUuid}")
	public ResponseEntity<? extends Object> getPrePairingDetailsForAssetUuid(@PathVariable("assetUuid") String assetUuid) {
		try {
			PrePairSensorsForAssetUpdateRequest prerequest  = deviceService.getPrePairingDetailsForAssetUuid(assetUuid);
			return new ResponseEntity<>(prerequest, HttpStatus.OK);
		} catch (Exception e) {
			logger.error("Exception while getting Asset Sensor Xref for asset id {}", assetUuid, e);
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
    
    @GetMapping("/pre-pair/asset-sensors-association-by-can/{can}")
	public ResponseEntity<? extends Object> getPrePairingDetailsForCompany(@PathVariable("can") String can) {
		try {
			List<PrePairSensorsForAssetUpdateRequest> listOfPrepairingDatw  = deviceService.getPrePairingDetailsForCompany(can);
			return new ResponseEntity<>(listOfPrepairingDatw, HttpStatus.OK);
		} catch (Exception e) {
			logger.error("Exception while getting Asset Sensor Xref for can  {}", can, e);
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

}
