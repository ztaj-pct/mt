package com.pct.installer.service;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.pct.common.dto.InProgressInstall;
import com.pct.common.dto.TpmsSensorCountDTO;
import com.pct.common.model.InstallHistory;
import com.pct.common.model.SensorReasonCode;
import com.pct.common.payload.GatewayDetailsBean;
import com.pct.common.payload.GetInstallHistoryByAssetUuids;
import com.pct.common.payload.InstallationStatusGatewayRequest;
import com.pct.common.payload.InstalledHistroyResponse;
//import com.pct.common.dto.InProgressInstall;
//import com.pct.common.dto.TpmsSensorCountDTO;
//import com.pct.common.model.Asset;
//import com.pct.common.model.Gateway;
//import com.pct.common.model.InstallHistory;
//import com.pct.common.payload.GetInstallHistoryByAssetUuids;
//import com.pct.common.payload.InstallationStatusGatewayRequest;
import com.pct.common.util.Context;
import com.pct.installer.dto.InstallationDetailResponseDTO;
import com.pct.installer.dto.InstallationSummaryResponseDTO;
import com.pct.installer.dto.SensorInstallInstructionDto;
import com.pct.installer.dto.SensorReasonCodeDto;
import com.pct.installer.entity.LogIssue;
import com.pct.installer.payload.CreateGatewaySensorAssociation;
import com.pct.installer.payload.DeviceDetailsResponse;
import com.pct.installer.payload.FinishInstallRequest;
import com.pct.installer.payload.InstallationStatusResponse;
import com.pct.installer.payload.LogIssueBean;
import com.pct.installer.payload.LogIssueGatewayRequest;
import com.pct.installer.payload.LogIssueRequest;
import com.pct.installer.payload.LogIssueStatusRequest;
import com.pct.installer.payload.SensorDetailsResponse;
//import com.pct.installer.dto.InstallationDetailResponseDTO;
//import com.pct.installer.dto.InstallationSummaryResponseDTO;
//import com.pct.installer.model.LogIssue;
//import com.pct.installer.payload.*;
import com.pct.installer.payload.StartInstallRequest;
import com.pct.installer.payload.UpdateSensorStatusRequest;
import com.pct.installer.payload.UpdateSensorStatusWithInstanceRequest;

//import java.util.List;
//import java.util.Map;

//import org.apache.poi.ss.usermodel.Workbook;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;

/**
 * @author Abhishek on 01/05/20
 */

public interface IInstallationService {

	Boolean startInstall(StartInstallRequest startInstallRequest, Long userId, String logUUId) throws Exception;

//    Boolean finishInstall(FinishInstallRequest finishInstallRequest,Context context,Long userId);
//
//    Boolean updateSensorStatus(UpdateSensorStatusRequest updateSensorStatusRequest);
//
   public  Boolean updateGatewayInstallStatus(InstallationStatusGatewayRequest installationStatusGatewayRequest);
//
    InstallationStatusResponse getInstallationStatus(String installUuid);
//
    Boolean resetInstallation(String logUUid, String vin, String deviceID, String can);
//
//    InstallHistory inProgressInstallForGateway(String gatewayUuid);
//
//    Boolean resetCompanyData(String companyUuid,Context context);
//
    List<InProgressInstall> getInProgressInstall(String accountNumber,String logUUid);
//
    LogIssue logIssue(LogIssueRequest logIssueRequest,Long userId,String logUUid);
    
    TpmsSensorCountDTO getSensorCount(String sensorUuid);
//
//    LogIssue logIssueForGateway(LogIssueGatewayRequest logIssueRequest,Long userId,Context context);
//    
//    SensorDetailsResponse getSensorDetails(String installCode,Context context);
//
//    List<LogIssueBean> getLoggedIssues(String installCode,Context context);
//
//    Boolean resetGatewayData(String gatewayUuid);
//    
//    Boolean resetGatewayStatus(String installUuid);
//
//	Boolean updateLogIssueStatus(LogIssueStatusRequest logIssueStatusRequest, Long userId);
//	
//	 public Boolean updateSensorStatusWithPositioning(UpdateSensorStatusWithInstanceRequest updateSensorStatusRequest,Context context);
//
	List<InstallHistory> getInstallHistoryByAssetUuids(GetInstallHistoryByAssetUuids getInstallHistoryByAssetUuids);

	InstallationDetailResponseDTO getInstallHistoryByDeviceUUid(String logUUid, String assetUUid);

//    
    Page<InstallationSummaryResponseDTO> getAllInstallationSummary(Pageable pageable, String comapnyUuid, Long userId, Map<String, String> filterValues, Long days);
//
    SensorDetailsResponse getOfflineData(Context context);
//
//    GatewayDetailsResponse getGatewayDetails(String installCode,String can,Long userId,Context context);
//
//	Boolean prePairingInstallationAtTheTimeOfStartInstall(Asset asset, Gateway gateway, Long userId, InstallHistory installHistory);
//
//	TpmsSensorCountDTO getSensorCount(String sensorUuid);
//
//	String checkIsAutoResetInstallationIsApplicableForCurrentInstallation(FinishInstallRequest finishInstallRequest,Context context);
//
//	Workbook exportExcelForInstallationSummary(Pageable pageable, String comapnyUuid, Long userId, Map<String, String> filterValues,
//			Long days);
//
	Boolean markInstallationToInProgress(String installCode, Long userId, Context context);
//	
//    public Page<GatewayDetailsBean>  getGatewayDetailsWithPagination(String installCode,String can,Long userId,Context context,Integer page, Integer pageSize, String sort, String order);

	   // -------------------- Aamir 1 Start -----------------------------------------------//  
	
    Boolean resetCompanyData(String companyUuid,Context context);
    
    Boolean resetGatewayData(String deviceUuid);
    
    LogIssue logIssueForGateway(LogIssueGatewayRequest logIssueRequest,Long userId,Context context);
    
    DeviceDetailsResponse getGatewayDetails(String installCode,String can,Long userId,String logUUid);
    
	SensorDetailsResponse getSensorDetails(String installCode, Context context);
		
    // -------------------- Aamir 1 End -----------------------------------------------//  
	Page<GatewayDetailsBean> getGatewayDetailsWithPagination(String logUUid, String installCode, String can,
			Long userId, Context context, Integer page, Integer pageSize, String sort, String order);


	List<LogIssueBean> getLoggedIssues(String installCode, Context context);

	Boolean updateLogIssueStatus(LogIssueStatusRequest logIssueStatusRequest, Long userId);

	Workbook exportExcelForInstallationSummary(Pageable pageable, String companyUuid, Long userId,
			Map<String, String> filterValues, Long valueOf);

	Boolean finishInstall(FinishInstallRequest finishInstallRequest, Long userId);

	Boolean updateSensorStatus(UpdateSensorStatusRequest updateSensorStatusRequest);
	
	Boolean updateSensorStatusWithPositioning(UpdateSensorStatusWithInstanceRequest updateSensorStatusRequest, String logUUid);
	
	Page<InProgressInstall> getFinishedInstallationsWithPagination(Pageable pageable, String accountNumber);

	Page<GatewayDetailsBean> getGatewayDetailsWithPaginationNew(String installCode, String can, Long userId, Context context, Integer page,
			Integer pageSize, String sort, String order, String timeOfLastDownload);

	Boolean createGatewaySensorAssociation(CreateGatewaySensorAssociation createGatewaySensorAssociation, Long userId,
			String logUuid);

	InProgressInstall getInstallationByCanAndImei(String accountNumber, String imei);
	
	List<InstalledHistroyResponse> getInstaledHistoryDeviceImei(List<String> deviceImeiList,Context context);

	List<SensorInstallInstructionDto> getSensorInstallInstruction(String productName);
	
	public List<SensorReasonCode> getSensorReasonCode(String productName);
	

	Page<GatewayDetailsBean> getGatewayDetailsWithPaginationNewTesting(String installCode, String can, Long userId,
			Context context, Integer page, Integer pageSize, String sort, String order, String timeOfLastDownload);

	Page<GatewayDetailsBean> getGatewaysByAccountNumberAndStatusWithPaginationV2Testing(String accountNumber,
			String status, Pageable pageable, List<String> cans, Instant lastDownloadeTime);

	List<SensorReasonCodeDto> getSensorReasonCodeByDto(String productName);

	Page<InProgressInstall> getInProgressInstallV2(String accountNumber, String logUUid, Pageable page);

	String getInstallationDate(String assetUuid);



	


}
