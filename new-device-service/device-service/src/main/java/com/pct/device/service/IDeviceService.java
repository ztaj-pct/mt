package com.pct.device.service;

import java.util.List;

import javax.validation.Valid;

import com.pct.common.dto.AssetDTO;
import com.pct.common.model.Asset;
import com.pct.common.model.AssetGatewayXref;
import com.pct.common.model.AssetSensorXref;
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
import com.pct.device.service.device.Device;
import com.pct.device.service.device.DeviceReport;

/**
 * @author Abhishek on 24/04/20
 */
public interface IDeviceService {

    JobSummaryResponse getDeviceJobSummary(JobSummaryRequest jobSummaryRequest);

    Gateway getGatewayByImei(String imei);

    Gateway getGatewayByUuid(String uuid);
    
    Asset getAssetByVin(String vin);

    Long getAssetCountByCompanyId(Long companyId);
    Asset getAssetByAssetUuid(String assetUuid);

    Gateway updateAssetForGateway(UpdateAssetForGatewayRequest updateAssetForGatewayRequest) throws Exception;

    List<Sensor> getAllSensorsForGateway(String gatewayUuid);

    InventoryResponse getInventoryForCustomer(String accountNumber);

    Sensor getSensorBySensorUuid(String sensorUuid);

    Sensor updateSensor(SensorUpdateRequest sensor);

    AssetGatewayXref saveAssetGatewayXref(SaveAssetGatewayXrefRequest saveAssetGatewayXrefRequest);

    Gateway updateGatewayStatus(InstallationStatusGatewayRequest installationStatusGatewayRequest);

    List<Asset> getAssetsByAccountNumberAndStatus(String accountNumber, String status);
    
    List<AssetDTO> getAssetsByAccountNumberAndStatusUsingDTO(String accountNumber, String status);

    List<Gateway> getGatewaysByAccountNumberAndStatus(String accountNumber, String status);

    Gateway updateGatewayAssetStatus(UpdateGatewayAssetStatusRequest updateGatewayAssetStatusRequest);

    Boolean resetInstall(Long assetId, Long gatewayId);

    Boolean resetCompanyData(String companyUuid);

    Asset getAssetByVinAndCan(String vin, String can);

    Gateway getGatewayByImeiAndCan(String imei, String can);

    Gateway getGatewayByMACAndCan(String mac, String can);
    
    List<Sensor> getSensorsForCan(String can);

    String getLookupValue(String field);

	List<Device> getAllGatewayFromMS(String customerName);

	List<Device> getSelectedGatewayFromMS(@Valid List<String> imeiList);
	
	List<DeviceReport> getMaintReportsFromMS(@Valid List<String> imeiList);

	Long updateAssetToDeviceInMS(UpdateAssetToDeviceForInstallationRequest request) throws Exception;

	Boolean prePairSensorsForAsset(PrePairSensorsForAssetRequest prePairSensorsForAssetRequest, Long userId);
	
	Boolean prePairSensorsForAssetUpdate(PrePairSensorsForAssetUpdateRequest prePairSensorsForAssetRequest, Long userId);

	//Boolean prePairingInstallationAtTheTimeOfStartInstall(String assetUuid, String gatewayUuid, Long userId);

	Boolean isAssetHavePrePairProducts(String assetUuid);

	PrePairSensorsForAssetUpdateRequest getPrePairingDetailsForAssetUuid(String assetUuid);
	
	List<AssetSensorXref> getAllAssetSensorXrefForAssetUuid(String assetUuid);

	AssetSensorXref updateAssetSensorXref(AssetSensorXref assetSensorXref);

	GatewaySensorXref saveGatewaySensorXref(GatewaySensorXref gatewaySensorXref);

	List<SubSensor> findBySensorUuid(String subSensorUuid);

	Sensor updateSensorBySensorObj(Sensor sensor);

	List<PrePairSensorsForAssetUpdateRequest> getPrePairingDetailsForCompany(String can);
}
