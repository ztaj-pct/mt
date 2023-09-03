package com.pct.device.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pct.common.dto.AssetDTO;
import com.pct.common.dto.ResponseBodyDTO;
import com.pct.common.dto.ResponseDTO;
import com.pct.common.model.Asset;
import com.pct.common.model.AssetGatewayXref;
import com.pct.common.model.AssetSensorXref;
import com.pct.common.model.Attribute;
import com.pct.common.model.AttributeValue;
import com.pct.common.model.Gateway;
import com.pct.common.model.GatewaySensorXref;
import com.pct.common.model.Sensor;
import com.pct.common.model.SubSensor;
import com.pct.common.payload.InstallationStatusGatewayRequest;
import com.pct.common.payload.InventoryResponse;
import com.pct.common.payload.JobSummaryRequest;
import com.pct.common.payload.JobSummaryResponse;
import com.pct.common.payload.PrePairSensorsForAssetRequest;
import com.pct.common.payload.PrePairSensorsForAssetUpdateRequest;
import com.pct.common.payload.SaveAssetGatewayXrefRequest;
import com.pct.common.payload.SensorUpdateRequest;
import com.pct.common.payload.UpdateAssetForGatewayRequest;
import com.pct.common.payload.UpdateAssetToDeviceForInstallationRequest;
import com.pct.common.payload.UpdateGatewayAssetStatusRequest;
import com.pct.common.util.JwtUtil;
import com.pct.device.dto.AttributeValueResponseDTO;
import com.pct.device.dto.MessageDTO;
import com.pct.device.exception.DeviceException;
import com.pct.device.service.IAttributeValueService;
import com.pct.device.service.IDeviceService;
import com.pct.device.service.IGatewayService;
import com.pct.device.service.IProductMasterService;
import com.pct.device.service.device.Device;
import com.pct.device.service.device.DeviceReport;

/**
 * @author Abhishek on 24/04/20
 */

@RestController
@RequestMapping("/device/core")
public class DeviceController {

    Logger logger = LoggerFactory.getLogger(DeviceController.class);

    @Autowired
    IDeviceService deviceService;
    
    @Autowired
    IProductMasterService productMasterService;
    
    @Autowired
    IAttributeValueService attributeValueService;

    @Autowired
    private IGatewayService gatewayService;
    
    @Autowired
   	private JwtUtil jwtutil;
  
    //    @PostMapping("/job-summary")
    public ResponseEntity<? extends Object> getJobSummary(HttpServletRequest httpServletRequest, @Valid @RequestBody JobSummaryRequest jobSummaryRequest) {
        try {
            JobSummaryResponse jobSummaryResponse = deviceService.getDeviceJobSummary(jobSummaryRequest);
            return new ResponseEntity<>(jobSummaryResponse, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Exception while getting job summary", e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/gateway/{imei}")
    public ResponseEntity<? extends Object> getGatewayByImei(@PathVariable("imei") String imei) {
        try {
            Gateway gateway = deviceService.getGatewayByImei(imei);
            return new ResponseEntity<>(gateway, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Exception while getting gateway for imei {}", imei, e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @GetMapping("/gateway/uuid/{uuid}")
    public ResponseEntity<? extends Object> getGatewayByUuid(@PathVariable("uuid") String uuid) {
        try {
            Gateway gateway = deviceService.getGatewayByUuid(uuid);
            return new ResponseEntity<>(gateway, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Exception while getting gateway for imei {}", uuid, e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
 
    
    @GetMapping("/asset/uuid/{assetUuid}")
    public ResponseEntity<? extends Object> getAssetByAssetUuid(@PathVariable("assetUuid") String assetUuid) {
        try {
            Asset asset = deviceService.getAssetByAssetUuid(assetUuid);
            return new ResponseEntity<>(asset, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Exception while getting asset for uuid {}", assetUuid, e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/asset/{vin}")
    public ResponseEntity<? extends Object> getAssetByVin(@PathVariable("vin") String vin) {
        try {
            Asset asset = deviceService.getAssetByVin(vin);
            return new ResponseEntity<>(asset, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Exception while getting asset for vin {}", vin, e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/gateway/update-asset")
    public ResponseEntity<? extends Object> updateAssetForGateway(@Valid @RequestBody UpdateAssetForGatewayRequest updateAssetForGatewayRequest) {
        try {
            Gateway gateway = deviceService.updateAssetForGateway(updateAssetForGatewayRequest);
            return new ResponseEntity<>(gateway, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Exception while updating asset id {} for gateway id {}",
                    updateAssetForGatewayRequest.getAssetUuid(), updateAssetForGatewayRequest.getGatewayUuid(), e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    //    @GetMapping("/sensor/gateway/{gatewayUuid}")
    public ResponseEntity<? extends Object> getAllSensorsForGateway(@PathVariable("gatewayUuid") String gatewayUuid) {
        try {
            List<Sensor> allSensorsForGateway = deviceService.getAllSensorsForGateway(gatewayUuid);
            return new ResponseEntity<>(allSensorsForGateway, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Exception while getting sensors for gateway id {}", gatewayUuid, e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
	@GetMapping("/asset-sensor-xref/{assetUuid}")
	public ResponseEntity<List<AssetSensorXref>> getAllAssetSensorXrefForAssetUuid(@PathVariable("assetUuid") String assetUuid) {
		try {
			List<AssetSensorXref> allAssetSensorXrefForAssetUuid = deviceService.getAllAssetSensorXrefForAssetUuid(assetUuid);
			return new ResponseEntity<>(allAssetSensorXrefForAssetUuid, HttpStatus.OK);
		} catch (Exception e) {
			logger.error("Exception while getting Asset Sensor Xref for asset id {}", assetUuid, e);
			return new ResponseEntity(
                    new ResponseDTO(false, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@GetMapping("/sub-sensor/{subSensorUuid}")
	public ResponseEntity<List<SubSensor>> findBySensorUuid(@PathVariable("subSensorUuid") String subSensorUuid) {
		try {
			List<SubSensor> allSubSensor = deviceService.findBySensorUuid(subSensorUuid);
			return new ResponseEntity<>(allSubSensor, HttpStatus.OK);
		} catch (Exception e) {
			logger.error("Exception while getting Sensor for sub sensor id {}", subSensorUuid, e);
			return new ResponseEntity(
                    new ResponseDTO(false, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

    //    @GetMapping("/inventory/{accountNumber}")
    public ResponseEntity<? extends Object> getInventoryForCustomer(@PathVariable("accountNumber") String accountNumber) {
        try {
            InventoryResponse inventoryResponse = deviceService.getInventoryForCustomer(accountNumber);
            return new ResponseEntity<>(
                    new ResponseBodyDTO<InventoryResponse>(true, "Successfully fetched Inventory for Customer",
                            inventoryResponse), HttpStatus.OK);
        } catch (Exception exception) {
            logger.error("Exception while getting inventory for Customer", exception);
            return new ResponseEntity(
                    new ResponseDTO(false, exception.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/sensor/uuid/{sensorUuid}")
    public ResponseEntity<? extends Object> getSensorBySensorUuid(@PathVariable("sensorUuid") String sensorUuid) {
        try {
            Sensor sensor = deviceService.getSensorBySensorUuid(sensorUuid);
            return new ResponseEntity<>(
                    new ResponseBodyDTO<Sensor>(true, "Successfully fetched Sensor for Uuid",
                            sensor), HttpStatus.OK);
        } catch (Exception exception) {
            logger.error("Exception while getting Sensor for Uuid", exception);
            return new ResponseEntity(
                    new ResponseDTO(false, exception.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/sensor")
    public ResponseEntity<? extends Object> updateSensor(@Valid @RequestBody SensorUpdateRequest sensorUpdateRequest) {
        try {
            Sensor sensor = deviceService.updateSensor(sensorUpdateRequest);
            return new ResponseEntity<>(
                    new ResponseBodyDTO<Sensor>(true, "Successfully updated Sensor",
                            sensor), HttpStatus.OK);
        } catch (Exception exception) {
            logger.error("Exception while updating Sensor", exception);
            return new ResponseEntity(
                    new ResponseDTO(false, exception.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @PutMapping("/update-sensor")
    public ResponseEntity<? extends Object> updateSensorBySensorObj(@Valid @RequestBody Sensor sensor) {
        try {
            Sensor sensorObj = deviceService.updateSensorBySensorObj(sensor);
            return new ResponseEntity<>(
                    new ResponseBodyDTO<Sensor>(true, "Successfully updated Sensor",
                    		sensorObj), HttpStatus.OK);
        } catch (Exception exception) {
            logger.error("Exception while updating Sensor", exception);
            return new ResponseEntity(
                    new ResponseDTO(false, exception.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @PutMapping("/asset-sensor-xref")
    public ResponseEntity<? extends Object> updateAssetSensorXref(@Valid @RequestBody AssetSensorXref assetSensorXref) {
        try {
        	AssetSensorXref assetSensorXrefObj = deviceService.updateAssetSensorXref(assetSensorXref);
            return new ResponseEntity<>(
                    new ResponseBodyDTO<AssetSensorXref>(true, "Successfully updated AssetSensorXref",
                    		assetSensorXrefObj), HttpStatus.OK);
        } catch (Exception exception) {
            logger.error("Exception while updating AssetSensorXref", exception);
            return new ResponseEntity(
                    new ResponseDTO(false, exception.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @PutMapping("/gateway-sensor-xref")
    public ResponseEntity<? extends Object> saveGatewaySensorXref(@Valid @RequestBody GatewaySensorXref gatewaySensorXref) {
        try {
        	GatewaySensorXref gatewaySensorXrefObj = deviceService.saveGatewaySensorXref(gatewaySensorXref);
            return new ResponseEntity<>(
                    new ResponseBodyDTO<GatewaySensorXref>(true, "Successfully updated GatewaySensorXref",
                    		gatewaySensorXrefObj), HttpStatus.OK);
        } catch (Exception exception) {
            logger.error("Exception while updating AssetSensorXref", exception);
            return new ResponseEntity(
                    new ResponseDTO(false, exception.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/asset-gateway-xref")
    public ResponseEntity<? extends Object> saveAssetGatewayXref(@Valid @RequestBody SaveAssetGatewayXrefRequest saveAssetGatewayXrefRequest) {
        try {
            AssetGatewayXref assetGatewayXref = deviceService.saveAssetGatewayXref(saveAssetGatewayXrefRequest);
            return new ResponseEntity<>(
                    new ResponseBodyDTO<AssetGatewayXref>(true, "Successfully saved AssetGatewayXref",
                            assetGatewayXref), HttpStatus.OK);
        } catch (Exception exception) {
            logger.error("Exception while saving AssetGatewayXref", exception);
            return new ResponseEntity(
                    new ResponseDTO(false, exception.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/gateway/status")
    public ResponseEntity<? extends Object> updateGatewayStatus(@Valid @RequestBody InstallationStatusGatewayRequest installationStatusGatewayRequest) {
        try {
            Gateway gateway = deviceService.updateGatewayStatus(installationStatusGatewayRequest);
            return new ResponseEntity<>(
                    new ResponseBodyDTO<Gateway>(true, "Successfully updating Gateway",
                            gateway), HttpStatus.OK);
        } catch (Exception exception) {
            logger.error("Exception while updating Gateway", exception);
            return new ResponseEntity(
                    new ResponseDTO(false, exception.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

//    @GetMapping("/asset")
//    public ResponseEntity<? extends Object> getAssetsByAccountNumberAndStatus(@RequestParam(value = "can") String accountNumber, @RequestParam(value = "status", required = false) String status) {
//        try {
//            List<Asset> assets = deviceService.getAssetsByAccountNumberAndStatus(accountNumber, status);
//            return new ResponseEntity<>(
//                    new ResponseBodyDTO<List>(true, "Successfully fetched Assets",
//                            assets), HttpStatus.OK);
//        } catch (Exception exception) {
//            logger.error("Exception while fetching Assets", exception);
//            return new ResponseEntity(
//                    new ResponseDTO(false, exception.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }
    
    @GetMapping("/asset")
    public ResponseEntity<? extends Object> getAssetsByAccountNumberAndStatusUsingDTO(@RequestParam(value = "can") String accountNumber, @RequestParam(value = "status", required = false) String status) {
        try {
            List<AssetDTO> assets = deviceService.getAssetsByAccountNumberAndStatusUsingDTO(accountNumber, status);
            return new ResponseEntity<>(
                    new ResponseBodyDTO<List>(true, "Successfully fetched Assets",
                            assets), HttpStatus.OK);
        } catch (Exception exception) {
            logger.error("Exception while fetching Assets", exception);
            return new ResponseEntity(
                    new ResponseDTO(false, exception.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/gateway")
    public ResponseEntity<? extends Object> getGatewaysByAccountNumberAndStatus(@RequestParam(value = "can") String accountNumber, @RequestParam(value = "status", required = false) String status) {
        try {
            List<Gateway> gateways = deviceService.getGatewaysByAccountNumberAndStatus(accountNumber, status);
            return new ResponseEntity<>(
                    new ResponseBodyDTO<List>(true, "Successfully fetched Gateways",
                            gateways), HttpStatus.OK);
        } catch (Exception exception) {
            logger.error("Exception while fetching Gateways", exception);
            return new ResponseEntity(
                    new ResponseDTO(false, exception.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/status/gateway-asset")
    public ResponseEntity<? extends Object> updateGatewayAssetStatus(@Valid @RequestBody UpdateGatewayAssetStatusRequest updateGatewayAssetStatusRequest) {
        try {
            Gateway gateway = deviceService.updateGatewayAssetStatus(updateGatewayAssetStatusRequest);
            if (gateway != null) {
                return new ResponseEntity<>(
                        new ResponseBodyDTO<Gateway>(true, "Successfully updated Gateway and Asset status",
                                gateway), HttpStatus.OK);
            } else {
                logger.error("Exception while updating Gateway and Asset status");
                return new ResponseEntity(
                        new ResponseDTO(false, "Gateway not found for given gateway Id"), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (Exception exception) {
            logger.error("Exception while updating Gateway and Asset status", exception);
            return new ResponseEntity(
                    new ResponseDTO(false, exception.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/reset-install")
    public ResponseEntity<ResponseDTO> resetInstall(@RequestParam(name = "assetId", required = false) Long assetId,
                                                    @RequestParam(name = "gatewayId", required = false) Long gatewayId) {
        try {
            Boolean status = deviceService.resetInstall(assetId, gatewayId);
            return new ResponseEntity<ResponseDTO>(new ResponseDTO(status, "Reset successful"), HttpStatus.OK);
        } catch (Exception exception) {
            logger.error("Exception while resetting Gateway and Asset status", exception);
            return new ResponseEntity<ResponseDTO>(
                    new ResponseDTO(false, exception.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @GetMapping("/is-asset-applicable-for-pre-pair/{assetUuid}")
    public ResponseEntity<ResponseDTO> isAssetApplicableForPrePair(@PathVariable("assetUuid") String assetUuid) {
        try {
            Boolean status = deviceService.isAssetHavePrePairProducts(assetUuid);
            return new ResponseEntity<ResponseDTO>(new ResponseDTO(status, "Reset successful"), HttpStatus.OK);
        } catch (Exception exception) {
            logger.error("Exception while checking Asset is Applicable For Pre Pair", exception);
            return new ResponseEntity<ResponseDTO>(
                    new ResponseDTO(false, exception.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/reset-company")
    public ResponseEntity<ResponseDTO> resetCompanyData(@RequestParam("account_number") String accountNumber) {
        try {
            Boolean status = deviceService.resetCompanyData(accountNumber);
            return new ResponseEntity<ResponseDTO>(new ResponseDTO(status, "Reset successful"), HttpStatus.OK);
        } catch (Exception exception) {
            logger.error("Exception while resetting company data", exception);
            return new ResponseEntity<ResponseDTO>(
                    new ResponseDTO(false, exception.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/asset/can-vin")
    public ResponseEntity<? extends Object> getAssetByVinAndCan(@RequestParam(value = "vin", required = true) String vin,
                                                                @RequestParam(value = "can", required = true) String can) {
        try {
            Asset asset = deviceService.getAssetByVinAndCan(vin, can);
            return new ResponseEntity<>(asset, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Exception while getting asset for vin {}", vin, e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/gateway/can-imei")
    public ResponseEntity<? extends Object> getGatewayByImeiAndCan(@RequestParam(value = "imei", required = true) String imei,
                                                                   @RequestParam(value = "can", required = true) String can) {
        try {
            Gateway gateway = deviceService.getGatewayByImeiAndCan(imei, can);
            return new ResponseEntity<>(gateway, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Exception while getting asset for vin {}", imei, e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @GetMapping("/gateway/can-mac")
    public ResponseEntity<? extends Object> getGatewayByMACAndCan(@RequestParam(value = "mac", required = true) String mac,
                                                                   @RequestParam(value = "can", required = false) String can) {
        try {
        	Gateway gateway =null;
            if(can==null || can=="") {
            	gateway = deviceService.getGatewayByMACAndCan(mac, null);
            }else {
            	 gateway = deviceService.getGatewayByMACAndCan(mac, can);
            }
            return new ResponseEntity<>(gateway, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Exception while getting asset for vin {}", mac, e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/sensor/can")
    public ResponseEntity<List<Sensor>> getSensorForCan(@RequestParam(name = "can") String can) {
        try {
            List<Sensor> sensorList = deviceService.getSensorsForCan(can);
            return new ResponseEntity<List<Sensor>>(sensorList, HttpStatus.OK);
        } catch(Exception e) {
            logger.error("Exception while getting sensors for can {}", can, e);
            return new ResponseEntity(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/count/asset")
    public ResponseEntity<Long> getAssetCountForCompany(@RequestParam(name = "companyId") Long companyId) {
        try {
            Long  assetList = deviceService.getAssetCountByCompanyId(companyId);
            return new ResponseEntity<Long>(assetList, HttpStatus.OK);
        } catch(Exception e) {
            logger.error("Exception while getting count for asset {}", companyId, e);
            return new ResponseEntity(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/lookup")
    public ResponseEntity<String> getLookupValue(@RequestParam(name = "field") String field) {
        try {
            String value = deviceService.getLookupValue(field);
            return new ResponseEntity<String>(value, HttpStatus.OK);
        } catch(Exception e) {
            logger.error("Exception while getting lookup value for field {}", field, e);
            return new ResponseEntity(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @GetMapping("/ms-device/{customerName}")
    public   ResponseEntity<List<Device>> getAllGatewayFromMS(@PathVariable("customerName") String customerName) {
        try {
            List<Device> allDevices = deviceService.getAllGatewayFromMS(customerName);
            return new ResponseEntity<>(allDevices, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Exception while getting devices from MS database {}",  e);
            return new ResponseEntity(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @PostMapping("/ms-device/getSelectedDevices")
    public   ResponseEntity<List<Device>> getSelectedGatewayFromMS(@RequestBody List<String> imeiList) {
        try {
            List<Device> allDevices = deviceService.getSelectedGatewayFromMS(imeiList);
            return new ResponseEntity<>(allDevices, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Exception while getting devices from MS database {}",  e);
            return new ResponseEntity(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/ms-device/getLastMaintReports")
    public ResponseEntity<List<DeviceReport>> getMaintReportsFromMS(@RequestBody List<String> imeiList) {
        try {
            List<DeviceReport> maintReports = deviceService.getMaintReportsFromMS(imeiList);
            return new ResponseEntity<>(maintReports, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Exception while getting devices from MS database {}", e);
            return new ResponseEntity(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping("/status/{imei}")
    public ResponseEntity<ResponseDTO> getGatewayInstallationStatus(@PathVariable("imei") String imei) {
        try {
            String status = gatewayService.getGatewayInstallationStatus(imei);
            return new ResponseEntity<ResponseDTO>(new ResponseDTO(true,status), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Exception while getting device installation status {}",  e);
            return new ResponseEntity<ResponseDTO>(
                    new ResponseDTO(false, e.getMessage()),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/ms-device/assetToDevice")
    public ResponseEntity<ResponseBodyDTO<Long>> updateAssetToDeviceInMS(@RequestBody UpdateAssetToDeviceForInstallationRequest request) {
        try {
            Long recordId = deviceService.updateAssetToDeviceInMS(request);
            return new ResponseEntity<>(new ResponseBodyDTO(false, "Successfully updated AssetToDevice in MS", recordId),
                    HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Exception while getting devices from MS database {}", e);
            return new ResponseEntity(new ResponseBodyDTO(false, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @GetMapping("/product-name/{productName}")
	public ResponseEntity<List<Attribute>> getAttributeListByProductName(@Validated
			@PathVariable("productName") String producName) {
		try {
			List<Attribute> attributeResponse = productMasterService.getProductByName(producName);
			return new ResponseEntity<>(attributeResponse, HttpStatus.OK);
		}
		catch (DeviceException exception) {
			logger.error("Exception occurred while getting attributeResponseList", exception);
			  return new ResponseEntity(exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
		catch (Exception exception) {
			logger.error("Exception occurred while getting attributeResponseList", exception);
			  return new ResponseEntity(exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
    
    @GetMapping("/get-attribute-value")
   	public ResponseEntity<List<AttributeValue>> getAllInstallatioDetails(@RequestParam(value = "deviceId", required = true) String deviceId,
   											  HttpServletRequest httpServletRequest) {
     try {
    	 List<AttributeValue> details = attributeValueService.getAttributeValueByGatewayDeviceId(deviceId);
    	 return new ResponseEntity<>(details, HttpStatus.OK);
     } catch (Exception e) {
         logger.error("Exception occurred while getting attribute values", e);
         return new ResponseEntity(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
     }
   	}
}
