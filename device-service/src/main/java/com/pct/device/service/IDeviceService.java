package com.pct.device.service;

import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.pct.common.constant.DeviceStatus;
import com.pct.common.constant.IOTType;
import com.pct.common.dto.AssetDTO;
import com.pct.common.dto.DeviceResponsePayloadForAssetUpdate;
//import com.pct.common.model.AssetGatewayXref;
import com.pct.common.model.AssetSensorXref;
import com.pct.common.model.Device;
import com.pct.common.model.Device_Device_xref;
import com.pct.common.model.LatestDeviceReportCount;
import com.pct.common.model.Organisation;
import com.pct.common.model.ReportCount;
import com.pct.common.payload.AssetSensorXrefPayload;
import com.pct.common.payload.DeviceSensorxrefPayload;
import com.pct.common.payload.GatewayDetailsBean;
import com.pct.common.payload.InstallationStatusGatewayRequest;
import com.pct.common.payload.SensorUpdateRequest;
import com.pct.common.payload.UpdateAssetToDeviceForInstallationRequest;
import com.pct.common.payload.UpdateGatewayAssetStatusRequest;
import com.pct.device.dto.DeviceReportDTO;
import com.pct.device.exception.DeviceException;
import com.pct.device.model.ColumnDefs;
import com.pct.device.model.DeviceReportCount;
import com.pct.device.payload.AssetDevicePayload;
import com.pct.device.payload.BatchDeviceEditPayload;
import com.pct.device.payload.DeviceCustomerUpdatePayload;
import com.pct.device.payload.DeviceDetailPayLoad;
import com.pct.device.payload.DeviceDetailsRequest;
import com.pct.device.payload.DeviceResponsePayload;
import com.pct.device.payload.DeviceWithSensorPayload;
import com.pct.device.payload.UpdateDeviceStatusPayload;
import com.pct.es.dto.Filter;

public interface IDeviceService {

	Boolean addDeviceDetail(DeviceDetailsRequest deviceUploadRequest, String userName) throws DeviceException;

	Page<DeviceResponsePayload> getDeviceWithPagination(String messageUuid,String accountNumber, String imei, String uuid,
			DeviceStatus status, IOTType type, String mac, Map<String, Filter> filterValues,
			String filterModelCountFilter, Pageable pageable, String userName, boolean forExport, String token, String timeOfLastDownload, String sort);
	
	void exportDeviceDataIntoCSV(String messageUuid,String accountNumber, String imei, String uuid,
			DeviceStatus status, IOTType type, String mac, Map<String, Filter> filterValues,
			String filterModelCountFilter, int page, int size, String sort,String order, String userName, boolean forExport, String token,List<String> columnDef, HttpServletResponse response);
	
	
	Page<LatestDeviceReportCount> getLatestDeviceReportCountWithPagination( Map<String, Filter> filterValues,
			String filterModelCountFilter, Pageable pageable, String userName);

	Page<DeviceWithSensorPayload> getDeviceAndSensorWithPagination(String accountNumber,
			Map<String, String> filterValues, String filterModelCountFilter, Pageable pageable, String userName,String sort);
	
	Page<DeviceReportCount> getDeviceReportCountWithPagination( Map<String, Filter> filterValues,
			String filterModelCountFilter, Pageable pageable, String userName);

	boolean deleteDeviceDetail(String can, String mac, String uuid, IOTType type);

	Device updateDeviceStatus(@Valid UpdateDeviceStatusPayload deviceStatusPayload) throws Exception;

	Boolean updateDeviceDetail(DeviceDetailPayLoad devicedetailPayload, String userId);

	public String getParsedReport(String rawReport, String format, String type);

	String[] getCompanyByType(String type);

	Organisation getCompanyById(Long id);

	List<DeviceResponsePayload> getDeviceDetails(String accountNumber, String uuid, String deviceId);

	public Device getDevice(String imei);

	public SearchResponse getDeviceReportByUUid(int from, int size, Map<String, Filter> filterValues, String imei)
			throws IOException;

	List<Organisation> getListOfCompanyByType(String type,String name);

	List<ColumnDefs> getColumnDefs();

	Boolean updateAssetDeviceDetails(AssetDevicePayload assetDevicePayload, String username);
	
	Boolean batchUpdateAssetDeviceDetails(BatchDeviceEditPayload batchDeviceEditPayload, String username);
	
	Boolean updateDeviceCustomerDetails(DeviceCustomerUpdatePayload deviceCustomerUpdatePayload);

	String updateLatestReport(String imei, String latestReport);

	public void getDeviceDetailsForCsv(List<Page<DeviceResponsePayload>> payloadList, HttpServletResponse response, String username, String token, int totalSize,List<String> columnDef)
			throws IOException;

	public void getCSVData(HttpServletResponse response, int from, int size, String order, String sort, String column,
			Map<String, Filter> filterValues,String deviceId,String userName, String token,List<String> columnDef);

	Boolean addDeviceDetailInBatch(List<DeviceDetailsRequest> deviceUploadRequest) throws DeviceException;
	
	Page<ReportCount> getReportCountWithPagination( Map<String, Filter> filterValues,
			String filterModelCountFilter, Pageable pageable, String userName);
	
	public int getDeviceCountByOrganizationId(List<String> ids);
	
//	Device updateDeviceAssetStatus(UpdateDeviceAssetStatusRequest updateDeviceAssetStatusRequest);
    
	Long updateAssetToDeviceInMS(UpdateAssetToDeviceForInstallationRequest request) throws Exception;

	Device getGatewayByMACAndCan(String mac, String can);

	Boolean updateCompanyInAsset(String accountNumber, String asset_uuid);

//	AssetGatewayXref saveAssetDeviceXref(SaveAssetGatewayXrefRequest saveAssetGatewayXrefRequest);

	Device updateAssetForGateway(String gatewayUuid, String assetUuid) throws Exception;

	List<AssetSensorXref> getAllAssetSensorXrefForAssetUuid(String assetUuid);

	Device getGatewayByImeiAndCan(String imei, String can);

//	Boolean resetInstall(Long assetId, Long gatewayId);
	Boolean resetInstall(String logUUid, Long assetId, Long gatewayId);

//	AssetSensorXref updateAssetSensorXref(AssetSensorXref assetSensorXref);

	Device updateSensor(SensorUpdateRequest sensorUpdateRequest);

//	Device_Sensor_xref saveGatewaySensorXref(Device_Sensor_xref gatewaySensorXref);

//	Device_Sensor_xref saveGatewaySensorXref(DeviceSensorxrefPayload gatewaySensorXref);

	List<Device_Device_xref> saveGatewaySensorXref(List<DeviceSensorxrefPayload> gatewaySensorXref);

//	AssetSensorXref updateAssetSensorXref(AssetSensorXrefPayload assetSensorXrefPayload);

	Resource downloadImage(String imageName);
	List<AssetSensorXref> updateAssetSensorXref(List<AssetSensorXrefPayload> assetSensorXrefPayload);

	Device updateDeviceAssetStatus(@Valid UpdateGatewayAssetStatusRequest updateDeviceAssetStatusRequest);

	DeviceResponsePayload updateGatewayStatus(InstallationStatusGatewayRequest installationStatusGatewayRequest, String userName);

//Aamir
	Boolean deleteAssetByAssetUuid(String assetUuid);

	List<AssetDTO> getAssetsByAccountNumberAndStatusUsingDTO(String accountNumber, String status);
	
	Page<Device> getGatewaysByAccountNumberAndStatusWithPagination(String accountNumber, String status, Pageable pageable, List<String> cans);

	 String getLookupValue(String field);
	//----------------------------------------Aamir 1 Start ---------------------------------//
	
	List<Device> getSensorsForCan(String can);
	
	public Device getGatewayByUuid(String uuid);
	
	public List<Device> getGatewaysByAccountNumberAndStatus(String accountNumber, String status);

	Device updateGatewayAssetStatus(UpdateGatewayAssetStatusRequest updateGatewayAssetStatusRequest);

	Device updateSensorBySensorObj(Device sensor);

	Device resetGateway(InstallationStatusGatewayRequest installationStatusGatewayRequest);
	
	Page<Device> getGatewaysByAccountNumberAndStatusWithPaginationNew(String accountNumber, String status, Pageable pageable,
			List<String> cans, Instant lastDownloadeTime);

	DeviceResponsePayloadForAssetUpdate updateAssetForGatewayNew(String gatewayUuid, String assetUuid) throws Exception;

	Page<GatewayDetailsBean> getGatewaysByAccountNumberAndStatusWithPaginationV2(String accountNumber, String status,
			Pageable pageable, List<String> cans, Instant lastDownloadeTime);
	
	DeviceReportDTO getLatestReportFromElastic(String deviceId) throws IOException;


}
