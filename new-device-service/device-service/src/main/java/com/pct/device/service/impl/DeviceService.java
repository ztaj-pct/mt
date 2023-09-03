package com.pct.device.service.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pct.common.constant.AssetStatus;
import com.pct.common.constant.GatewayStatus;
import com.pct.common.constant.InstanceType;
import com.pct.common.constant.SensorStatus;
import com.pct.common.dto.AssetDTO;
import com.pct.common.model.Asset;
import com.pct.common.model.AssetGatewayXref;
import com.pct.common.model.AssetSensorXref;
import com.pct.common.model.AttributeValue;
import com.pct.common.model.Gateway;
import com.pct.common.model.GatewaySensorXref;
import com.pct.common.model.Sensor;
import com.pct.common.model.SensorHardwareConfig;
import com.pct.common.model.SubSensor;
import com.pct.common.model.User;
import com.pct.common.payload.InstallationStatusGatewayRequest;
import com.pct.common.payload.InventoryResponse;
import com.pct.common.payload.JobSummaryRequest;
import com.pct.common.payload.JobSummaryResponse;
import com.pct.common.payload.PrePairSensorsForAssetRequest;
import com.pct.common.payload.PrePairSensorsForAssetUpdateRequest;
import com.pct.common.payload.SaveAssetGatewayXrefRequest;
import com.pct.common.payload.SensorListForPrePair;
import com.pct.common.payload.SensorListForPrePairForUpdate;
import com.pct.common.payload.SensorUpdateRequest;
import com.pct.common.payload.SubSensorList;
import com.pct.common.payload.SubSensorListForUpdate;
import com.pct.common.payload.UpdateAssetForGatewayRequest;
import com.pct.common.payload.UpdateAssetToDeviceForInstallationRequest;
import com.pct.common.payload.UpdateGatewayAssetStatusRequest;
import com.pct.device.exception.DeviceException;
import com.pct.device.model.Lookup;
import com.pct.device.repository.IAssetGatewayXrefRepository;
import com.pct.device.repository.IAssetRepository;
import com.pct.device.repository.IAssetSensorXrefRepository;
import com.pct.device.repository.IAttributeValueRepository;
import com.pct.device.repository.IGatewayRepository;
import com.pct.device.repository.IGatewaySensorXrefRepository;
import com.pct.device.repository.ILookupRepository;
import com.pct.device.repository.ISensorHardwareConfigRepository;
import com.pct.device.repository.ISensorRepository;
import com.pct.device.repository.ISubSensorRepository;
import com.pct.device.repository.msdevice.IAssetToDeviceHistoryRepository;
import com.pct.device.repository.msdevice.IAssetToDeviceRepository;
import com.pct.device.repository.msdevice.IDeviceRepository;
import com.pct.device.repository.projections.ApprovedAssetTypeCountView;
import com.pct.device.repository.projections.GatewayTypeCountView;
import com.pct.device.repository.projections.InProgressAssetTypeAndCountView;
import com.pct.device.service.IDeviceService;
import com.pct.device.service.device.AssetToDevice;
import com.pct.device.service.device.AssetToDeviceHistory;
import com.pct.device.service.device.Device;
import com.pct.device.service.device.DeviceReport;
import com.pct.device.service.device.DeviceReportView;
import com.pct.device.util.BeanConverter;
import com.pct.device.util.RestUtils;

/**
 * @author Abhishek on 24/04/20
 */

@Service
public class DeviceService implements IDeviceService {

    Logger logger = LoggerFactory.getLogger(DeviceService.class);

    @Autowired
    private IGatewayRepository gatewayRepository;
    @Autowired
    private IAssetRepository assetRepository;
    @Autowired
    private ISensorRepository sensorRepository;
    @Autowired
    private IAssetGatewayXrefRepository assetGatewayXrefRepository;
    @Autowired
    private IGatewaySensorXrefRepository gatewaySensorXrefRepository;
    @Autowired
    private IAssetSensorXrefRepository assetSensorXrefRepository;
    @Autowired
    private ILookupRepository lookupRepository;
    @Autowired
    private BeanConverter beanConverter;
    @Autowired
    private IDeviceRepository deviceRepository;
    @Autowired
    private IAssetToDeviceRepository assetToDeviceRepository;
    @Autowired
    private IAssetToDeviceHistoryRepository assetToDeviceHistoryRepository;
    @Autowired
    private IAttributeValueRepository attributeRespository;
    @Autowired
    private ISubSensorRepository subSensorRepository;
    @Autowired
    private ISensorHardwareConfigRepository sensorHardWareRepository;
    @Autowired
	private RestUtils restUtils;
    
    
    @Override
    public JobSummaryResponse getDeviceJobSummary(JobSummaryRequest jobSummaryRequest) {
        List<GatewayTypeCountView> gatewayReadyForInstallation = gatewayRepository
                .findProductTypeAndCountByStatus(GatewayStatus.ACTIVE.getValue(), jobSummaryRequest.getAccountNumber());
        List<ApprovedAssetTypeCountView> assetsApprovedForInstallation = assetRepository.findAssetTypeAndCountByStatus("Approved",
                jobSummaryRequest.getAccountNumber());
        List<InProgressAssetTypeAndCountView> inProgressInstallation = gatewayRepository.findByAssetIdNotNullAndErrorStatus();
        JobSummaryResponse jobSummaryResponse = beanConverter.createJobSummaryResponseFromProjections
                (gatewayReadyForInstallation, assetsApprovedForInstallation, inProgressInstallation);
        return jobSummaryResponse;
    }

    @Override
    public Gateway getGatewayByImei(String imei) {
        Gateway byImei = gatewayRepository.findByImei(imei);
        return byImei;
    }
    
    private void createHardwareConfigForAsset(String assetUuid, PrePairSensorsForAssetRequest prePairSensorsForAssetRequest) {
    	SensorHardwareConfig conf = sensorHardWareRepository.findByAssetUuid(assetUuid);
		if (conf != null) {
			conf.setIsDoor(prePairSensorsForAssetRequest.getIsDoor());
			conf.setIsMircoSp(prePairSensorsForAssetRequest.getIsMicrosp());
			conf.setIsWired(prePairSensorsForAssetRequest.getIsWireless());
			sensorHardWareRepository.save(conf);
		} else {
			conf = new SensorHardwareConfig();
			conf.setAssetUuid(assetUuid);
			conf.setIsDoor(prePairSensorsForAssetRequest.getIsDoor());
			conf.setIsMircoSp(prePairSensorsForAssetRequest.getIsMicrosp());
			conf.setIsWired(prePairSensorsForAssetRequest.getIsWireless());
			sensorHardWareRepository.save(conf);
		}
    }
    
    private boolean convertAndCreateSensorForPrePairing(PrePairSensorsForAssetRequest prePairSensorsForAssetRequest, Asset asset, Long userId) {
		Boolean status = false; 
		User user = restUtils.getUserFromAuthService(userId);
		for (SensorListForPrePair sensorForPrePair : prePairSensorsForAssetRequest.getSensorList()) { 
			if(sensorForPrePair.getProductCode() == null || "".equalsIgnoreCase(sensorForPrePair.getProductCode()) || sensorForPrePair.getProductName() == null || "".equalsIgnoreCase(sensorForPrePair.getProductName())) {
				throw new DeviceException("Product code or Product name should not be null or blank");
			}
		}
		
		Map<String, Long> duplicatesValueBasedOnProduct = prePairSensorsForAssetRequest.getSensorList().stream()
			       .collect(Collectors.groupingBy(SensorListForPrePair::getProductCode, Collectors.counting()));
		for (Map.Entry<String,Long> entry : duplicatesValueBasedOnProduct.entrySet()) {
			if(entry.getValue() > 1) {
				throw new DeviceException("Found duplicate sensor in sensor list. Please remove it and try again.");
			}
		}
		
		for (SensorListForPrePair sensorForPrePair : prePairSensorsForAssetRequest.getSensorList()) {
			Sensor sensor = new Sensor();
			sensor.setStatus(SensorStatus.PENDING);
			//sensor.setGateway(gateway);
			sensor.setProductCode(sensorForPrePair.getProductCode());
			sensor.setProductName(sensorForPrePair.getProductName());
			sensor.setMacAddress(sensorForPrePair.getMacAddress());
			sensor.setCreatedOn(Instant.now());
			sensor.setUpdatedOn(Instant.now());
			sensor.setCreatedBy(user);
			sensor.setUpdatedBy(user);
			boolean isSensorUuidUnique = false;
			String sensorUuid = "";
			while (!isSensorUuidUnique) {
				sensorUuid = UUID.randomUUID().toString();
				Sensor byUuid = sensorRepository.findByUuid(sensorUuid);
				if (byUuid == null) {
					isSensorUuidUnique = true;
				}
			}
			sensor.setUuid(sensorUuid);
			sensor = sensorRepository.save(sensor);
			logger.info("Ceated the Sensor For the prePairSensorsForAsset with id " + sensor.getUuid());
			AssetSensorXref assetSensorXref = new AssetSensorXref();
			assetSensorXref.setDateCreated(Instant.now());
			assetSensorXref.setIsActive(true);
			assetSensorXref.setAsset(asset);
			assetSensorXref.setSensor(sensor);
			assetSensorXref.setIsGatewayAttached(Boolean.FALSE);
			assetSensorXref = assetSensorXrefRepository.save(assetSensorXref);
			logger.info("Generating relationship between the asset : " + asset.getUuid() + " and sensor : " + sensor.getUuid());
			status = true;
			
			if (sensorForPrePair.getSubSensorList() != null && !sensorForPrePair.getSubSensorList().isEmpty()) {
				for (SubSensorList subSensorList : sensorForPrePair.getSubSensorList()) {
					SubSensor subSensor = new SubSensor();
					if(subSensorList.getInstanceType() != null) {
						subSensor.setInstanceType(subSensorList.getInstanceType());
					} else {
						subSensor.setInstanceType(InstanceType.DEFAULT.toString());
					}
					subSensor.setSubSensorId(subSensorList.getSubSensorId());
					subSensor.setType(subSensorList.getType());
					subSensor.setSensor(sensor);
					subSensor.setTimestamp(Instant.now());
					boolean isSubSensorUuidUnique = false;
					String subSensorUuid = "";
					while (!isSubSensorUuidUnique) {
						subSensorUuid = UUID.randomUUID().toString();
						SubSensor byUuid = subSensorRepository.findByUuid(sensorUuid);
						if (byUuid == null) {
							isSubSensorUuidUnique = true;
						}
					}
					subSensor.setUuid(subSensorUuid);
					subSensor = subSensorRepository.save(subSensor);
					logger.info("Generating relationship between the sub sensor : " + subSensor.getUuid() + " and sensor : "
							+ sensor.getUuid());
				}
			}
		}
		return status;
	}
    
    @Override
	public Boolean prePairSensorsForAsset(PrePairSensorsForAssetRequest prePairSensorsForAssetRequest, Long userId) {
		logger.info("Inside the Device Service from the prePairSensorsForAsset method");
		Boolean status = false;
		if (prePairSensorsForAssetRequest != null && prePairSensorsForAssetRequest.getAssetUuid() != null) {
			Asset asset = assetRepository.findByUuid(prePairSensorsForAssetRequest.getAssetUuid());
			if (asset != null) {
				List<AssetSensorXref> listOfAssetSensorXref = assetSensorXrefRepository.findByAssetUuid(asset.getUuid());
				if (listOfAssetSensorXref != null && listOfAssetSensorXref.size() > 0) {
					throw new DeviceException("Asset is Already Prepaired");
				}

				if (!asset.getStatus().equals(AssetStatus.PENDING)) {
					throw new DeviceException("Asset is Already Installed");
				}

				createHardwareConfigForAsset(asset.getUuid(), prePairSensorsForAssetRequest);
				if (prePairSensorsForAssetRequest.getSensorList() != null && !prePairSensorsForAssetRequest.getSensorList().isEmpty() && prePairSensorsForAssetRequest.getSensorList().size() > 0) {
					status = convertAndCreateSensorForPrePairing(prePairSensorsForAssetRequest, asset, userId);
				} else {
					throw new DeviceException("Please provide sensor list");
				}

			} else {
				throw new DeviceException("Asset uuid not found");
			}
		}
		logger.info("Exiting from the Device Service from the prePairSensorsForAsset method");
		return status;
	}
    
    private void createHardwareConfigForAsset(String assetUuid, PrePairSensorsForAssetUpdateRequest prePairSensorsForAssetRequest) { 
    	SensorHardwareConfig conf = sensorHardWareRepository.findByAssetUuid(assetUuid);
		if (conf != null) {
			conf.setIsDoor(prePairSensorsForAssetRequest.getIsDoor());
			conf.setIsMircoSp(prePairSensorsForAssetRequest.getIsMicrosp());
			conf.setIsWired(prePairSensorsForAssetRequest.getIsWireless());
			sensorHardWareRepository.save(conf);
		} else {
			conf = new SensorHardwareConfig();
			conf.setAssetUuid(assetUuid);
			conf.setIsDoor(prePairSensorsForAssetRequest.getIsDoor());
			conf.setIsMircoSp(prePairSensorsForAssetRequest.getIsMicrosp());
			conf.setIsWired(prePairSensorsForAssetRequest.getIsWireless());
			sensorHardWareRepository.save(conf);
		}
    }
    
    private boolean convertAndCreateSensorForPrePairing(PrePairSensorsForAssetUpdateRequest prePairSensorsForAssetRequest, Asset asset, Long userId) {
		Boolean status = false;
		User user = restUtils.getUserFromAuthService(userId);
		List<AssetSensorXref> listOfAssetSensorXref = assetSensorXrefRepository.findByAssetUuid(asset.getUuid());
		for (SensorListForPrePairForUpdate sensorForPrePair : prePairSensorsForAssetRequest.getSensorList()) { 
			if(sensorForPrePair.getProductCode() == null || "".equalsIgnoreCase(sensorForPrePair.getProductCode()) || sensorForPrePair.getProductName() == null || "".equalsIgnoreCase(sensorForPrePair.getProductName())) {
				throw new DeviceException("Product code or Product name should not be null or blank");
			}
		}
		
		Map<String, Long> duplicatesValueBasedOnProduct = prePairSensorsForAssetRequest.getSensorList().stream()
			       .collect(Collectors.groupingBy(SensorListForPrePairForUpdate::getProductCode, Collectors.counting()));
		for (Map.Entry<String,Long> entry : duplicatesValueBasedOnProduct.entrySet()) {
			if(entry.getValue() > 1) {
				throw new DeviceException("Found duplicate sensor in sensor list. Please remove it and try again.");
			}
		}
		
		if(listOfAssetSensorXref != null && listOfAssetSensorXref.size() > 0) {
			if(listOfAssetSensorXref.get(0).getIsGatewayAttached()) {
				throw new DeviceException("The sensors of this asset is already prepaired with the gateway so you can not modify this asset if you want to modify it you can reset the asset");
			}
			for(AssetSensorXref asx: listOfAssetSensorXref) {
				List<SubSensor> listOfSubSensor = subSensorRepository.findBySensorUuid(asx.getSensor().getUuid());
				if(listOfSubSensor != null && listOfSubSensor.size() > 0) {
					subSensorRepository.deleteInBatch(listOfSubSensor);
				}
			}
			assetSensorXrefRepository.deleteInBatch(listOfAssetSensorXref);
		}
		
		for (SensorListForPrePairForUpdate sensorForPrePair : prePairSensorsForAssetRequest.getSensorList()) {
			Sensor sensor = new Sensor();
			sensor.setStatus(SensorStatus.PENDING);
			sensor.setProductCode(sensorForPrePair.getProductCode());
			sensor.setProductName(sensorForPrePair.getProductName());
			sensor.setMacAddress(sensorForPrePair.getMacAddress());
			sensor.setCreatedOn(Instant.now());
			sensor.setUpdatedOn(Instant.now());
			sensor.setCreatedBy(user);
			sensor.setUpdatedBy(user);
			boolean isSensorUuidUnique = false;
			String sensorUuid = "";
			while (!isSensorUuidUnique) {
				sensorUuid = UUID.randomUUID().toString();
				Sensor byUuid = sensorRepository.findByUuid(sensorUuid);
				if (byUuid == null) {
					isSensorUuidUnique = true;
				}
			}
			sensor.setUuid(sensorUuid);
			sensor = sensorRepository.save(sensor);
			logger.info("Ceated the Sensor For the prePairSensorsForAsset with id " + sensor.getUuid());
			AssetSensorXref assetSensorXref = new AssetSensorXref();
			assetSensorXref.setDateCreated(Instant.now());
			assetSensorXref.setIsActive(true);
			assetSensorXref.setAsset(asset);
			assetSensorXref.setSensor(sensor);
			assetSensorXref.setIsGatewayAttached(Boolean.FALSE);
			assetSensorXref = assetSensorXrefRepository.save(assetSensorXref);
			logger.info("Generating relationship between the asset : " + asset.getUuid() + " and sensor : " + sensor.getUuid());
			status = true;
			
			if (sensorForPrePair.getSubSensorList() != null && !sensorForPrePair.getSubSensorList().isEmpty()) {
				for (SubSensorListForUpdate subSensorList : sensorForPrePair.getSubSensorList()) {
					SubSensor subSensor = new SubSensor();
					if(subSensorList.getInstanceType() != null) {
						subSensor.setInstanceType(subSensorList.getInstanceType());
					} else {
						subSensor.setInstanceType(InstanceType.DEFAULT.toString());
					}
					subSensor.setSubSensorId(subSensorList.getSubSensorId());
					subSensor.setType(subSensorList.getType());
					subSensor.setSensor(sensor);
					subSensor.setTimestamp(Instant.now());
					boolean isSubSensorUuidUnique = false;
					String subSensorUuid = "";
					while (!isSubSensorUuidUnique) {
						subSensorUuid = UUID.randomUUID().toString();
						SubSensor byUuid = subSensorRepository.findByUuid(sensorUuid);
						if (byUuid == null) {
							isSubSensorUuidUnique = true;
						}
					}
					subSensor.setUuid(subSensorUuid);
					subSensor = subSensorRepository.save(subSensor);
					logger.info("Generating relationship between the sub sensor : " + subSensor.getUuid() + " and sensor : "
							+ sensor.getUuid());
				}
			}
		}
    	
    	return status;
    }
    
    @Override
	public Boolean prePairSensorsForAssetUpdate(PrePairSensorsForAssetUpdateRequest prePairSensorsForAssetRequest, Long userId) {
		logger.info("Inside the Device Service from the prePairSensorsForAssetUpdate method");
		Boolean status = false;
		if (prePairSensorsForAssetRequest != null && prePairSensorsForAssetRequest.getAssetUuid() != null) {
			Asset asset = assetRepository.findByUuid(prePairSensorsForAssetRequest.getAssetUuid());
			if (asset != null) {
				createHardwareConfigForAsset(asset.getUuid(), prePairSensorsForAssetRequest);
				if (prePairSensorsForAssetRequest.getSensorList() != null && !prePairSensorsForAssetRequest.getSensorList().isEmpty() && prePairSensorsForAssetRequest.getSensorList().size() > 0) {
					status = convertAndCreateSensorForPrePairing(prePairSensorsForAssetRequest, asset, userId);
				} else {
					throw new DeviceException("Please provide sensor list");
				}
			} else {
				throw new DeviceException("Asset uuid not found");
			}
		}
		logger.info("Exiting from the Device Service from the prePairSensorsForAssetUpdate method");
		return status;
	}
    
    @Override
	public Boolean isAssetHavePrePairProducts(String assetUuid) {
		logger.info("Inside the Device Service from the isAssetHavePrePairProducts method");
		Boolean status = false;
		Asset asset = assetRepository.findByUuid(assetUuid);
		if (asset != null) {
			List<AssetSensorXref> listOfAssetSensorXref = assetSensorXrefRepository.findByAssetUuid(assetUuid);
			if (listOfAssetSensorXref != null && listOfAssetSensorXref.size() > 0) {
				logger.info("Size of AssetSensorXref : " + listOfAssetSensorXref.size());
				status = true;
				logger.info("Value of isAssetHavePrePairProducts : " + status);
			}
		} else {
			throw new DeviceException("Asset uuid not found");
		}
		logger.info("Exiting from the Device Service from the isAssetHavePrePairProducts method");
		return status;
	}	
    
	

    @Override
    public Asset getAssetByVin(String vin) {
        List<Asset> byVinNumber = assetRepository.findByVinNumber(vin);
        return byVinNumber.get(0);
    }

    @Override
    public Asset getAssetByAssetUuid(String assetUuid) {
        Asset asset = assetRepository.findByUuid(assetUuid);
        return asset;
    }

    @Override
    public Gateway updateAssetForGateway(UpdateAssetForGatewayRequest updateAssetForGatewayRequest) throws Exception {
        Gateway gateway = gatewayRepository.findByUuid(updateAssetForGatewayRequest.getGatewayUuid());
        if (gateway != null) {
            Asset asset = assetRepository.findByUuid(updateAssetForGatewayRequest.getAssetUuid());
            if (asset != null) {
                gateway.setAsset(asset);
                gateway.setStatus(GatewayStatus.INSTALL_IN_PROGRESS);
                gateway = gatewayRepository.save(gateway);
                asset.setStatus(AssetStatus.INSTALL_IN_PROGRESS);
                assetRepository.save(asset);
            } else {
                throw new Exception("Asset found null for Id = " + updateAssetForGatewayRequest.getAssetUuid());
            }
        } else {
            throw new Exception("Gateway found null for Id = " + updateAssetForGatewayRequest.getGatewayUuid());
        }
        return gateway;
    }

    @Override
    public List<Sensor> getAllSensorsForGateway(String gatewayUuid) {
        List<Sensor> byGatewayId = sensorRepository.findByGatewayUuid(gatewayUuid);
        return byGatewayId;
    }

    @Override
    public InventoryResponse getInventoryForCustomer(String accountNumber) {
        List<ApprovedAssetTypeCountView> assetTypeAndCountByStatus = assetRepository.
                findAssetTypeAndCountByStatus(AssetStatus.PENDING.getValue(), accountNumber);
        List<GatewayTypeCountView> gatewayReadyForInstallation = gatewayRepository
                .findProductTypeAndCountByStatus(GatewayStatus.ACTIVE.getValue(), accountNumber);
        InventoryResponse inventoryResponseFromProjections = beanConverter.
                createInventoryResponseFromProjections(gatewayReadyForInstallation, assetTypeAndCountByStatus);
        return inventoryResponseFromProjections;
    }

    @Override
    public Sensor getSensorBySensorUuid(String sensorUuid) {
        Sensor sensor = sensorRepository.findByUuid(sensorUuid);
        return sensor;
    }

    @Override
    public Sensor updateSensor(SensorUpdateRequest sensorUpdateRequest) {
        Sensor sensor = sensorRepository.findByUuid(sensorUpdateRequest.getSensorUuid());
        sensor.setUpdatedOn(Instant.ofEpochMilli(Long.parseLong(sensorUpdateRequest.getUpdatedOn())));
        sensor.setStatus(SensorStatus.getSensorStatus(sensorUpdateRequest.getStatus()));
        sensor = sensorRepository.save(sensor);
        return sensor;
    }
    
    @Override
    public Sensor updateSensorBySensorObj(Sensor sensor) {
        Sensor sensorObj = sensorRepository.save(sensor);
        return sensorObj;
    }

    @Override
    public AssetGatewayXref saveAssetGatewayXref(SaveAssetGatewayXrefRequest saveAssetGatewayXrefRequest) {
        AssetGatewayXref assetGatewayXref = new AssetGatewayXref();
        Asset asset = assetRepository.findByUuid(saveAssetGatewayXrefRequest.getAssetUuid());
        assetGatewayXref.setAsset(asset);
        Gateway gateway = gatewayRepository.findByImei(saveAssetGatewayXrefRequest.getImei());
        if(gateway==null) {
        	gateway = gatewayRepository.findByMac_address(saveAssetGatewayXrefRequest.getImei());
        }
        assetGatewayXref.setGateway(gateway);
        assetGatewayXref.setDateCreated(Instant.ofEpochMilli(Long.parseLong(saveAssetGatewayXrefRequest.getDatetimeRT())));
        assetGatewayXref.setIsActive(saveAssetGatewayXrefRequest.getIsActive());
        AssetGatewayXref save = assetGatewayXrefRepository.save(assetGatewayXref);
        return save;
    }

    @Override
    public Gateway updateGatewayStatus(InstallationStatusGatewayRequest installationStatusGatewayRequest) {
        Gateway gateway = gatewayRepository.findByUuid(installationStatusGatewayRequest.getGatewayUuid());
        gateway.setStatus(GatewayStatus.valueOf(installationStatusGatewayRequest.getStatus()));
        gateway.setUpdatedOn(Instant.ofEpochMilli(Long.parseLong(installationStatusGatewayRequest.getDatetimeRt())));
        gateway = gatewayRepository.save(gateway);
        return gateway;
    }

    @Override
    public List<Asset> getAssetsByAccountNumberAndStatus(String accountNumber, String status) {
        List<Asset> assets = null;
        if (status != null && !status.isEmpty()) {
            assets = assetRepository.findByAccountNumberAndStatus(accountNumber, AssetStatus.getAssetStatus(status));
        } else {
            assets = assetRepository.findByAccountNumber(accountNumber);
        }
        return assets;
    }
    
    @Override
    public List<AssetDTO> getAssetsByAccountNumberAndStatusUsingDTO(String accountNumber, String status) {
        List<AssetDTO> assets = null;
        if (status != null && !status.isEmpty()) {
            assets = assetRepository.findByAccountNumberAndStatusUsingDTO(accountNumber, AssetStatus.getAssetStatus(status));
        } else {
            assets = assetRepository.findByAccountNumberUsingDTO(accountNumber);
        }
        return assets;
    }

    @Override
    public List<Gateway> getGatewaysByAccountNumberAndStatus(String accountNumber, String status) {
        List<Gateway> gateways = null;
        if (status != null && !status.isEmpty()) {
            gateways = gatewayRepository.findByAccountNumberAndStatus(accountNumber, GatewayStatus.valueOf(status));
        } else {
            gateways = gatewayRepository.findByAccountNumber(accountNumber);
        }
        return gateways;
    }

    @Override
    public Gateway updateGatewayAssetStatus(UpdateGatewayAssetStatusRequest updateGatewayAssetStatusRequest) {
        Gateway gateway = gatewayRepository.findByUuid(updateGatewayAssetStatusRequest.getGatewayUuid());
        if (gateway != null) {
            gateway.setStatus(updateGatewayAssetStatusRequest.getGatewayStatus());
            gateway = gatewayRepository.save(gateway);
            Asset asset = gateway.getAsset();
            asset.setStatus(updateGatewayAssetStatusRequest.getAssetStatus());
            assetRepository.save(asset);
            return gateway;
        }
        return null;
    }

    @Override
    public Boolean resetInstall(Long assetId, Long gatewayId) {
        if (assetId != null && gatewayId != null) {
            List<AssetGatewayXref> assetGatewayXrefList = assetGatewayXrefRepository.findByAssetIdAndGatewayId(assetId, gatewayId);
            assetGatewayXrefList.forEach(assetGatewayXref -> {
                assetGatewayXrefRepository.delete(assetGatewayXref);
            });
            Gateway gateway = gatewayRepository.findById(gatewayId).get();
            Asset asset = assetRepository.findById(assetId).get();
            gateway.setAsset(null);
            gateway.setStatus(GatewayStatus.PENDING);
            gateway.getSensors().forEach(sensor -> {
                sensor.setStatus(SensorStatus.PENDING);
                sensorRepository.save(sensor);
            });
            gateway = gatewayRepository.save(gateway);
            asset.setStatus(AssetStatus.PENDING);
            asset = assetRepository.save(asset);
        } else if (assetId != null && gatewayId == null) {
            List<AssetGatewayXref> assetGatewayXrefList = assetGatewayXrefRepository.findByAssetId(assetId);
            assetGatewayXrefList.forEach(assetGatewayXref -> {
                Gateway gateway = assetGatewayXref.getGateway();
                gateway.setStatus(GatewayStatus.PENDING);
                gateway.setAsset(null);
                gateway.getSensors().forEach(sensor -> {
                    sensor.setStatus(SensorStatus.PENDING);
                    sensorRepository.save(sensor);
                });
                gatewayRepository.save(gateway);
                assetGatewayXrefRepository.delete(assetGatewayXref);
            });
            Asset asset = assetRepository.findById(assetId).get();
            asset.setStatus(AssetStatus.PENDING);
            assetRepository.save(asset);
        } else if (assetId == null && gatewayId != null) {
            List<AssetGatewayXref> assetGatewayXrefList = assetGatewayXrefRepository.findByGatewayId(gatewayId);
            assetGatewayXrefList.forEach(assetGatewayXref -> {
                Asset asset = assetGatewayXref.getAsset();
                asset.setStatus(AssetStatus.PENDING);
                assetRepository.save(asset);
                assetGatewayXrefRepository.delete(assetGatewayXref);
            });
            Gateway gateway = gatewayRepository.findById(gatewayId).get();
            gateway.setStatus(GatewayStatus.PENDING);
            gateway.setAsset(null);
            gateway.getSensors().forEach(sensor -> {
                sensor.setStatus(SensorStatus.PENDING);
                sensorRepository.save(sensor);
            });
            gatewayRepository.save(gateway);
        } else {
            throw new DeviceException("Both gateway ID and asset ID can't be null");
        }
        return true;
    }

    @Override
    public Boolean resetCompanyData(String accountNumber) {
        List<Gateway> gatewayList = gatewayRepository.findByAccountNumber(accountNumber);
        List<Asset> assetList = assetRepository.findByAccountNumber(accountNumber);
        assetList.forEach(asset -> {
        List<AssetSensorXref> assetSensorXrefs = assetSensorXrefRepository.findByAssetUuid(asset.getUuid());
         for(AssetSensorXref assetSensorXref : assetSensorXrefs) {
            assetSensorXrefRepository.delete(assetSensorXref);
        }
        });
        gatewayList.forEach(gateway -> {
            List<GatewaySensorXref> gatewaySensorXrefs = gatewaySensorXrefRepository.findByGatewayUuid(gateway.getUuid());
            gatewaySensorXrefs.forEach(gatewaySensorXref -> {
                gatewaySensorXrefRepository.delete(gatewaySensorXref);
            });
            List<Sensor> sensorList = sensorRepository.findByGatewayUuid(gateway.getUuid());
            
            sensorList.forEach(sensor -> {
            	List<SubSensor> subsensors = subSensorRepository.findBySensorUuid(sensor.getUuid());
            	subsensors.forEach(subsen ->{
            		subSensorRepository.delete(subsen);
            	});
                sensorRepository.delete(sensor);
            });
            List<AssetGatewayXref> assetGatewayXrefs = assetGatewayXrefRepository.findByGatewayId(gateway.getId());
            assetGatewayXrefs.forEach(assetGatewayXref -> {
                assetGatewayXrefRepository.delete(assetGatewayXref);
            });
            List<AttributeValue> attrubuteValue = attributeRespository.findByDeviceImei(gateway.getImei());
            if(attrubuteValue.size()>0) {
            	attrubuteValue.forEach(att -> {
            		attributeRespository.delete(att);
            });
            }
            gatewayRepository.delete(gateway);
        });
        assetList.forEach(asset -> {
            assetRepository.delete(asset);
        });
        return true;
    }

    @Override
    public Asset getAssetByVinAndCan(String vin, String can) {
        Asset assetByVinNumber = assetRepository.findByVinAndAccountNumber(vin, can);
        return assetByVinNumber;
    }

    @Override
    public Gateway getGatewayByImeiAndCan(String imei, String can) {
        Gateway gateway = gatewayRepository.findByImeiAndAccountNumber(imei, can);
        return gateway;
    }
    
    @Override
    public Gateway getGatewayByMACAndCan(String mac, String can) {
    	Gateway gateway=null;
    	if(can!=null) {
    		 gateway = gatewayRepository.findByMACAndAccountNumber(mac, can);
    	}else {
    		gateway = gatewayRepository.findByMac_address(mac);
    	}
 
        return gateway;
    }
    
    @Override
    public List<Sensor> getSensorsForCan(String can) {
        List<Sensor> sensorList = sensorRepository.findByGatewayCan(can);
        return sensorList;
    }

    @Override
    public String getLookupValue(String field) {
        List<Lookup> byField = lookupRepository.findByField(field);
        if(byField != null && byField.size() > 0) {
            return byField.get(0).getValue();
        }
        return "";
    }

	@Override
	public List<Device> getAllGatewayFromMS(String customerName) {

		 List<Device> deviceList = deviceRepository.findDeviceByCustomerNumber(customerName);
		 
	        return deviceList;
	}

	@Override
	public List<Device> getSelectedGatewayFromMS(@Valid List<String> imeiList) {

		 List<Device> deviceList = deviceRepository.findDeviceByCustomerNumber(imeiList);
	        return deviceList;
	}
	
	@Override
	public List<DeviceReport> getMaintReportsFromMS(@Valid List<String> imeiList) {

		 List<DeviceReportView> reportView = new ArrayList<DeviceReportView>();
		 DeviceReportView view = null;
		 for (String imei : imeiList) {
			 view = deviceRepository.findMaintReportsByDeviceImei(imei);
			 if (view != null) {
				 reportView.add(view);
			}
		}
		 List<DeviceReport> reportList = new ArrayList<DeviceReport>();
		 for (DeviceReportView deviceReportView : reportView) {
			 DeviceReport deviceReport =  new DeviceReport();
			 deviceReport.setAPP_SW_VERSION(deviceReportView.getAPP_SW_VERSION());
			 deviceReport.setBASEBAND_SW_VERSION(deviceReportView.getBASEBAND_SW_VERSION());
			 deviceReport.setDEVICE_ID(deviceReportView.getDEVICE_ID());
			 deviceReport.setEXTENDER_VERSION(deviceReportView.getEXTENDER_VERSION());
			 reportList.add(deviceReport);
		}
	        return reportList;
	}

    @Override
    public Long updateAssetToDeviceInMS(UpdateAssetToDeviceForInstallationRequest request) throws Exception {

        AssetToDevice assetToDevice = assetToDeviceRepository.findByAssetID(request.getAssignedName());
        if(assetToDevice == null) {
            assetToDevice = assetToDeviceRepository.findByVin(request.getVin());
            if(assetToDevice == null) {
                logger.info("No record for Asset ID {} and VIN {} in AssetToDevice table. New record will be created", request.getAssignedName(), request.getVin());
                assetToDevice = new AssetToDevice();
                long recordId =0l;
                try {
                    recordId= assetToDeviceRepository.findMaximumRecordId();
                }catch (Exception e){
                    logger.error("Error occurred while getting  AssetToDevice record in MS - ", e);
                }
                recordId++;
                assetToDevice.setRECORD_ID(recordId);
                assetToDevice.setASSET_ID(request.getAssignedName());
                assetToDevice.setVIN(request.getVin());
                assetToDevice.setCUSTOMER(request.getCustomerName());
            }
        }
        if(assetToDevice.getASSET_TYPE() == null || assetToDevice.getASSET_TYPE().isEmpty()) {
            assetToDevice.setASSET_TYPE(request.getAssetType());
        }
        if(assetToDevice.getMAKE() == null || assetToDevice.getMAKE().isEmpty()) {
            assetToDevice.setMAKE(request.getMake());
        }
        if(assetToDevice.getMODEL() == null || assetToDevice.getMODEL().isEmpty()) {
            assetToDevice.setMODEL(request.getModel());
        }
        if(assetToDevice.getYEAR() == null || assetToDevice.getYEAR().isEmpty()) {
        	if(request.getYear().equalsIgnoreCase("Not Available")) {
        		assetToDevice.setYEAR(null);
        	}else {
        		assetToDevice.setYEAR(request.getYear());
        	}
        }
        assetToDevice.setDEVICE_ID(request.getImei());
        assetToDevice.setINSTALL_TIMESTAMP(request.getInstallTimestamp());
        assetToDevice.setCOMMENT("Updated from InstallAssist");
        assetToDevice = assetToDeviceRepository.save(assetToDevice);
        Long maximumRecordId = assetToDeviceHistoryRepository.findMaximumRecordId();
        if(maximumRecordId == null) {
            maximumRecordId = 1l;
        }
        AssetToDeviceHistory assetToDeviceHistory = new AssetToDeviceHistory();
        assetToDeviceHistory.setRECORD_ID(maximumRecordId + 1);
        assetToDeviceHistory.setASSET_ID(request.getAssignedName());
        assetToDeviceHistory.setDEVICE_ID(request.getImei());
        assetToDeviceHistory.setINSTALL_TIMESTAMP(request.getInstallTimestamp());
        assetToDeviceHistory.setCUSTOMER(request.getCustomerName());
        assetToDeviceHistory = assetToDeviceHistoryRepository.save(assetToDeviceHistory);
        return assetToDevice.getRECORD_ID();
    }

	@Override
	public Long getAssetCountByCompanyId(Long companyId) {
	Long assetCount = 0l;
	if(companyId != null) {
		assetCount = assetRepository.getAssetCountByCompanyId(companyId);
		return assetCount;
	}
	  return assetCount;

	}

	@Override
	public Gateway getGatewayByUuid(String uuid) {
		  Gateway gateway = gatewayRepository.findByUuid(uuid);
	        return gateway;
	}

	@Override
	public PrePairSensorsForAssetUpdateRequest getPrePairingDetailsForAssetUuid(String assetUuid) {
		List<AssetSensorXref> listOfAssetSensorXref = null;
		SensorHardwareConfig conf = new SensorHardwareConfig();
		if(assetUuid != null) {
			conf = sensorHardWareRepository.findByAssetUuid(assetUuid);
			listOfAssetSensorXref = assetSensorXrefRepository.findByAssetUuid(assetUuid);
		}
		PrePairSensorsForAssetUpdateRequest prepair = beanConverter.convertPreParingResposne(listOfAssetSensorXref, conf, assetUuid);
		return prepair;
	}
	
	@Override
	public List<AssetSensorXref> getAllAssetSensorXrefForAssetUuid(String assetUuid) {
		List<AssetSensorXref> listOfAssetSensorXref = null;
		if(assetUuid != null) {
			listOfAssetSensorXref = assetSensorXrefRepository.findByAssetUuid(assetUuid);
		}
		return listOfAssetSensorXref;
	}

	@Override
	public AssetSensorXref updateAssetSensorXref(AssetSensorXref assetSensorXref) {
		AssetSensorXref assetSensorXrefObj = assetSensorXrefRepository.save(assetSensorXref);
		return assetSensorXrefObj;
	}

	@Override
	public GatewaySensorXref saveGatewaySensorXref(GatewaySensorXref gatewaySensorXref) {
		gatewaySensorXref = gatewaySensorXrefRepository.save(gatewaySensorXref);
		return gatewaySensorXref;
	}

	@Override
	public List<SubSensor> findBySensorUuid(String subSensorUuid) {
		List<SubSensor> listOfSubSensor = null;
		if(subSensorUuid != null) {
			listOfSubSensor = subSensorRepository.findBySensorUuid(subSensorUuid);
		}
		return listOfSubSensor;
	}

	@Override
	public List<PrePairSensorsForAssetUpdateRequest> getPrePairingDetailsForCompany(String can) {
		List<PrePairSensorsForAssetUpdateRequest> listOfPrePairData = null;
		SensorHardwareConfig conf = null;
		if(can != null) {
			List<String> listOfAssetUuid =  assetRepository.findByCompanyAccountNo(can);
			if(listOfAssetUuid != null && listOfAssetUuid.size() > 0) {
				listOfPrePairData = new ArrayList<PrePairSensorsForAssetUpdateRequest>();
				for(String assetUuid : listOfAssetUuid) {
					List<AssetSensorXref> listOfAssetSensorXref = null;
					conf = new SensorHardwareConfig();
					conf = sensorHardWareRepository.findByAssetUuid(assetUuid);
					listOfAssetSensorXref = assetSensorXrefRepository.findByAssetUuid(assetUuid);
					if(listOfAssetSensorXref != null && listOfAssetSensorXref.size() > 0 && !listOfAssetSensorXref.get(0).getIsGatewayAttached()) {
						PrePairSensorsForAssetUpdateRequest prepair = beanConverter.convertPreParingResposne(listOfAssetSensorXref, conf, assetUuid);
						listOfPrePairData.add(prepair);
					}
				}
			}
			
		}
		return listOfPrePairData;
	}

	
}
