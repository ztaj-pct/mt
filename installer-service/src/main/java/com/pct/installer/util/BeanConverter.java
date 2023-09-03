package com.pct.installer.util;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pct.common.constant.DeviceStatus;
import com.pct.common.constant.DeviceType;
import com.pct.common.constant.EventType;
import com.pct.common.constant.IOTType;
import com.pct.common.constant.InstallHistoryStatus;
import com.pct.common.constant.InstanceType;
import com.pct.common.dto.AssetDTO;
import com.pct.common.dto.InProgressInstall;
import com.pct.common.model.Asset;
import com.pct.common.model.Device;
import com.pct.common.model.Device_Device_xref;
import com.pct.common.model.InstallHistory;
import com.pct.common.model.InstallLog;
import com.pct.common.model.WorkOrder;
import com.pct.common.payload.InstalledHistroyResponse;
import com.pct.common.payload.UpdateAssetToDeviceForInstallationRequest;
import com.pct.common.util.Context;
import com.pct.common.util.Logutils;
//import com.pct.installer.constant.EventType;
import com.pct.installer.dto.AssetDetailResponseDTO;
import com.pct.installer.dto.AttributeResponseDTO;
import com.pct.installer.dto.BatteryResponseDTO;
import com.pct.installer.dto.FinishWorkOrderDTO;
import com.pct.installer.dto.InstallationDetailResponseDTO;
import com.pct.installer.dto.InstallationSummaryResponseDTO;
import com.pct.installer.dto.InventoryListAssetDTO;
import com.pct.installer.dto.InventoryListBeaconDTO;
import com.pct.installer.dto.InventoryListGatewayDTO;
import com.pct.installer.dto.LoggedIssueResponseDTO;
import com.pct.installer.dto.ManufactureDetailResponseDTO;
import com.pct.installer.dto.SensorResponseDTO;
//import com.pct.installer.entity.InstallLog;
import com.pct.installer.entity.LogIssue;
import com.pct.installer.payload.AssetsStatusPayload;
import com.pct.installer.payload.InstallationStatusResponse;
import com.pct.installer.payload.InstallationStatusSensor;
import com.pct.installer.payload.LogIssueBean;
import com.pct.installer.repository.IDeviceDeviceXrefRepository;
import com.pct.installer.repository.IInstallLogRepository;
//import com.pct.installer.repository.ILogIssueRepository;
import com.pct.installer.repository.ILogIssueRepository;
import com.pct.installer.repository.IWorkOrderRepository;

/**
 * @author Abhishek on 02/05/20
 */

@Component
public class BeanConverter {

	@Autowired
	private IInstallLogRepository installLogRepository;

	@Autowired
	private IDeviceDeviceXrefRepository deviceDeviceXrefRepository;

	@Autowired
	private IWorkOrderRepository workOrderRepository;

	@Autowired
	private ILogIssueRepository ilogIssueRepository;

	@Autowired
	private RestUtils restUtils;

	private final ObjectMapper objectMapper = new ObjectMapper();

	Logger logger = LoggerFactory.getLogger(BeanConverter.class);
	public static final String className = "BeanConverter";

//    public InventoryResponse createInventoryResponseFromMap(Map inputMap) {
//        Map<String, Integer> assets = (Map<String, Integer>) inputMap.get("assets");
//        Map<String, Integer> gateways = (Map<String, Integer>) inputMap.get("gateways");
//        InventoryResponse inventoryResponse = new InventoryResponse();
//        inventoryResponse.setAssets(assets);
//        inventoryResponse.setGateways(gateways);
//        return inventoryResponse;
//    }
//
//    public InstallationStatusResponse createInstallationStatusResponseFromInstallHistory(InstallHistory installHistory) {
//        InstallationStatusResponse installationStatusResponse = new InstallationStatusResponse();
//        installationStatusResponse.setInstallStatus(installHistory.getStatus().getValue());
//        installationStatusResponse.setAssetUuid(installHistory.getAsset().getUuid());
//        installationStatusResponse.setAssetStatus(installHistory.getAsset().getStatus().getValue());
//        installationStatusResponse.setGatewayUuid(installHistory.getGateway().getUuid());
//        installationStatusResponse.setGatewayStatus(installHistory.getGateway().getStatus().getValue());
//        if (installHistory.getGateway().getUpdatedOn() != null) {
//            installationStatusResponse.setGatewayDatetimeRT(installHistory.getGateway().getUpdatedOn().toString());
//        }
//        List<InstallLog> gatewayInstallLogList = installLogRepository.findByInstallHistoryIdAndEventType(installHistory.getId(), EventType.GATEWAY_INSTALLATION_COMPLETE);
//        if (gatewayInstallLogList != null && !gatewayInstallLogList.isEmpty()) {
//            installationStatusResponse.setGatewayData(gatewayInstallLogList.get(0).getData());
//        }
//        List<InstallationStatusSensor> sensorStatusList = new ArrayList<>();
//        for (Sensor sensor : installHistory.getGateway().getSensors()) {
//            InstallationStatusSensor installationStatusSensor = new InstallationStatusSensor();
//            installationStatusSensor.setSensorUuid(sensor.getUuid());
//            installationStatusSensor.setSensorStatus(sensor.getStatus().getValue());
//            if (sensor.getUpdatedOn() != null) {
//                installationStatusSensor.setSensorDatetimeRT(sensor.getUpdatedOn().toString());
//            }
//            List<InstallLog> sensorInstallLogList = installLogRepository.findByInstallHistoryIdEventTypeAndSensor(installHistory.getId(), EventType.SENSOR_INSTALLATION_COMPLETE, sensor.getId());
//            if(sensorInstallLogList != null && !sensorInstallLogList.isEmpty()) {
//                installationStatusSensor.setSensorData(sensorInstallLogList.get(0).getData());
//            }
//            sensorStatusList.add(installationStatusSensor);
//        }
//        installationStatusResponse.setSensorList(sensorStatusList);
//        return installationStatusResponse;
//    }
//
//    public List<InventoryListAssetDTO> createInventoryListAssetDTOFromAssets(List<Asset> assets) {
//        Map<String, Map<String, Integer>> categoryToStatusToCountMap = new HashMap<>();
//        for (Asset asset : assets) {
//            if (categoryToStatusToCountMap.get(asset.getCategory().getValue()) != null) {
//                if (categoryToStatusToCountMap.get(asset.getCategory().getValue()).get(asset.getStatus().getValue()) != null) {
//                    categoryToStatusToCountMap.get(asset.getCategory().getValue()).
//                            put(asset.getStatus().getValue(), categoryToStatusToCountMap.get(asset.getCategory().getValue()).get(asset.getStatus().getValue()) + 1);
//                } else {
//                    categoryToStatusToCountMap.get(asset.getCategory().getValue()).
//                            put(asset.getStatus().getValue(), 1);
//                }
//            } else {
//                HashMap<String, Integer> statusToCount = new HashMap<>();
//                statusToCount.put(asset.getStatus().getValue(), 1);
//                categoryToStatusToCountMap.put(asset.getCategory().getValue(), statusToCount);
//            }
//        }
//        List<InventoryListAssetDTO> inventoryListAssetDTOList = new ArrayList<>();
//        for (Map.Entry<String, Map<String, Integer>> categoryEntry : categoryToStatusToCountMap.entrySet()) {
//            for (Map.Entry<String, Integer> statusEntry : categoryEntry.getValue().entrySet()) {
//                InventoryListAssetDTO inventoryListAssetDTO = new InventoryListAssetDTO(categoryEntry.getKey(),
//                        statusEntry.getKey(),
//                        statusEntry.getValue());
//                inventoryListAssetDTOList.add(inventoryListAssetDTO);
//            }
//        }
//        return inventoryListAssetDTOList;
//    }
//    
	public List<InventoryListAssetDTO> createInventoryListAssetDTOFromAssetDTOs(List<AssetDTO> assets) {

		String methodName = "createInventoryListAssetDTOFromAssetDTOs";
		Context context = new Context();
		String logUUid = context.getLogUUId();
		Logutils.log(className, methodName, logUUid,
				" Inside createInventoryListAssetDTOFromAssetDTOs Method From BeanConverter " + "List<AssetDTO> "
						+ assets,
				logger);
		Map<String, Map<String, Integer>> categoryToStatusToCountMap = new HashMap<>();
		for (AssetDTO asset : assets) {
			if (categoryToStatusToCountMap.get(asset.getCategory().getValue()) != null) {
				if (categoryToStatusToCountMap.get(asset.getCategory().getValue())
						.get(asset.getStatus().getValue()) != null) {
					categoryToStatusToCountMap.get(asset.getCategory().getValue()).put(asset.getStatus().getValue(),
							categoryToStatusToCountMap.get(asset.getCategory().getValue())
									.get(asset.getStatus().getValue()) + 1);
				} else {
					categoryToStatusToCountMap.get(asset.getCategory().getValue()).put(asset.getStatus().getValue(), 1);
				}
			} else {
				HashMap<String, Integer> statusToCount = new HashMap<>();
				statusToCount.put(asset.getStatus().getValue(), 1);
				categoryToStatusToCountMap.put(asset.getCategory().getValue(), statusToCount);
			}
		}
		List<InventoryListAssetDTO> inventoryListAssetDTOList = new ArrayList<>();
		for (Map.Entry<String, Map<String, Integer>> categoryEntry : categoryToStatusToCountMap.entrySet()) {
			for (Map.Entry<String, Integer> statusEntry : categoryEntry.getValue().entrySet()) {
				InventoryListAssetDTO inventoryListAssetDTO = new InventoryListAssetDTO(categoryEntry.getKey(),
						statusEntry.getKey(), statusEntry.getValue());
				inventoryListAssetDTOList.add(inventoryListAssetDTO);
			}
		}

		Logutils.log(className, methodName, logUUid, " Exiting From createInventoryListAssetDTOFromAssetDTOs ", logger);
		return inventoryListAssetDTOList;
	}

	public List<InventoryListGatewayDTO> createInventoryListGatewayDTOFromGateways(List<Device> gateways) {
		Context context = new Context();
		String logUuId = context.getLogUUId();
		String methodName = "createInventoryListGatewayDTOFromGateways";
		Logutils.log(className, methodName, logUuId,
				" Inside createInventoryListGatewayDTOFromGateways " + "List<Device> " + gateways, logger);
		Map<String, Map<String, Integer>> productCodeToStatusToCountMap = new HashMap<>();
		for (Device gateway : gateways) {
//        	if(gateway.getDeviceType().equalsIgnoreCase(DeviceType.GATEWAY.getDeviceValue())) {
			if (gateway.getIotType().getIOTTypeValue().equalsIgnoreCase(DeviceType.GATEWAY.getDeviceValue())) {
				if (productCodeToStatusToCountMap.get(gateway.getProductName()) != null) {
					if (productCodeToStatusToCountMap.get(gateway.getProductName())
							.get(gateway.getStatus().getValue()) != null) {
						productCodeToStatusToCountMap.get(gateway.getProductName()).put(gateway.getStatus().getValue(),
								productCodeToStatusToCountMap.get(gateway.getProductName())
										.get(gateway.getStatus().getValue()) + 1);
					} else {
						productCodeToStatusToCountMap.get(gateway.getProductName()).put(gateway.getStatus().getValue(),
								1);
					}
				} else {
					HashMap<String, Integer> statusToCount = new HashMap<>();
					statusToCount.put(gateway.getStatus().getValue(), 1);
					productCodeToStatusToCountMap.put(gateway.getProductName(), statusToCount);
				}
			}
		}

		List<InventoryListGatewayDTO> inventoryListGatewayDTOList = new ArrayList<>();
		for (Map.Entry<String, Map<String, Integer>> productCodeEntry : productCodeToStatusToCountMap.entrySet()) {
			for (Map.Entry<String, Integer> statusEntry : productCodeEntry.getValue().entrySet()) {
				InventoryListGatewayDTO inventoryListGatewayDTO = null;
				if (statusEntry.getKey().equalsIgnoreCase(DeviceStatus.INSTALL_IN_PROGRESS.getValue())) {
					inventoryListGatewayDTO = new InventoryListGatewayDTO(productCodeEntry.getKey(),
							statusEntry.getKey(), statusEntry.getValue());
				} else {
					inventoryListGatewayDTO = new InventoryListGatewayDTO(productCodeEntry.getKey(),
							statusEntry.getKey(), statusEntry.getValue());
				}
				inventoryListGatewayDTOList.add(inventoryListGatewayDTO);
			}
		}
		Logutils.log(className, methodName, logUuId, " Exiting From createInventoryListGatewayDTOFromGateways ",
				logger);
		return inventoryListGatewayDTOList;
	}

	public List<InventoryListBeaconDTO> createInventoryListBeaconDTOFromGateways(List<Device> gateways) {
		Context context = new Context();
		String logUuId = context.getLogUUId();
		String methodName = "createInventoryListBeaconDTOFromGateways";
		Logutils.log(className, methodName, logUuId,
				" Inside createInventoryListBeaconDTOFromGateways " + "List<Device " + gateways, logger);
		Map<String, Map<String, Integer>> productCodeToStatusToCountMap = new HashMap<>();
		for (Device gateway : gateways) {
//        	if(gateway.getIotType().equals(DeviceType.BEACON)) {
			if (gateway.getIotType().getIOTTypeValue().equalsIgnoreCase(DeviceType.BEACON.getDeviceValue())) {
				if (productCodeToStatusToCountMap.get(gateway.getProductName()) != null) {
					if (productCodeToStatusToCountMap.get(gateway.getProductName())
							.get(gateway.getStatus().getValue()) != null) {
						productCodeToStatusToCountMap.get(gateway.getProductName()).put(gateway.getStatus().getValue(),
								productCodeToStatusToCountMap.get(gateway.getProductName())
										.get(gateway.getStatus().getValue()) + 1);
					} else {
						productCodeToStatusToCountMap.get(gateway.getProductName()).put(gateway.getStatus().getValue(),
								1);
					}
				} else {
					HashMap<String, Integer> statusToCount = new HashMap<>();
					statusToCount.put(gateway.getStatus().getValue(), 1);
					productCodeToStatusToCountMap.put(gateway.getProductName(), statusToCount);
				}
			}
		}
		List<InventoryListBeaconDTO> inventoryListBeaconDTOList = new ArrayList<>();
		for (Map.Entry<String, Map<String, Integer>> productCodeEntry : productCodeToStatusToCountMap.entrySet()) {
			for (Map.Entry<String, Integer> statusEntry : productCodeEntry.getValue().entrySet()) {
				InventoryListBeaconDTO inventoryListBeaconDTO = null;
				if (statusEntry.getKey().equals(DeviceStatus.INSTALL_IN_PROGRESS.getValue())) {
					inventoryListBeaconDTO = new InventoryListBeaconDTO(productCodeEntry.getKey(), statusEntry.getKey(),
							statusEntry.getValue());
				} else {
					inventoryListBeaconDTO = new InventoryListBeaconDTO(productCodeEntry.getKey(), statusEntry.getKey(),
							statusEntry.getValue());
				}
				inventoryListBeaconDTOList.add(inventoryListBeaconDTO);
			}
		}
		Logutils.log(className, methodName, logUuId, " Exiting From createInventoryListBeaconDTOFromGateways ", logger);
		return inventoryListBeaconDTOList;
	}

//
//    public List<Asset> createListOfAssetsFromMap(Map inputMap) {
//        List<Map<String, Integer>> assetsMapList = (List<Map<String, Integer>>) inputMap.get("body");
//        List<Asset> assetList = new ArrayList<>();
//        assetsMapList.forEach(assetMap -> {
//            assetList.add(objectMapper.convertValue(assetMap, Asset.class));
//        });
//        return assetList;
//    }
//    
	public List<AssetDTO> createListOfAssetDTOsFromMap(Map inputMap) {
		String methodName = "createListOfAssetDTOsFromMap";
		Context context = new Context();
		String logUUid = context.getLogUUId();
		Logutils.log(className, methodName, logUUid,
				" Inside createListOfAssetDTOsFromMap Method From BeanConverter " + "Map " + inputMap, logger);
		List<Map<String, Integer>> assetsMapList = (List<Map<String, Integer>>) inputMap.get("body");
		List<AssetDTO> assetList = new ArrayList<>();
		assetsMapList.forEach(assetMap -> {
			assetList.add(objectMapper.convertValue(assetMap, AssetDTO.class));
		});
		Logutils.log(className, methodName, logUUid, " Exiting From createListOfAssetDTOsFromMap ", logger);
		return assetList;
	}

//
//
//    public List<Gateway> createListOfGatewaysFromMap(Map inputMap) {
//        List<Map<String, Integer>> gatewaysMapList = (List<Map<String, Integer>>) inputMap.get("body");
//        List<Gateway> gatewayList = new ArrayList<>();
//        gatewaysMapList.forEach(gatewayMap -> {
//            gatewayList.add(objectMapper.convertValue(gatewayMap, Gateway.class));
//        });
//        return gatewayList;
//    }
//
//    public List<Sensor> createListOfSensorsFromMap(List<Map> inputMap) {
//        List<Sensor> sensorList = new ArrayList<>();
//        inputMap.forEach(sensorMap -> {
//            sensorList.add(objectMapper.convertValue(sensorMap, Sensor.class));
//        });
//        return sensorList;
//    }
//
//    public List<InProgressInstall> createInProgressInstallHistoryList(List<InstallHistory> installHistoryList) {
//        List<InProgressInstall> inProgressInstallList = new ArrayList<InProgressInstall>();
//        if (installHistoryList != null && installHistoryList.size() > 0) {
//            for (InstallHistory installHistory : installHistoryList) {
//                InProgressInstall inProgressInstall = new InProgressInstall();
//                inProgressInstall.setAssetUuid(installHistory.getAsset().getUuid());
//                inProgressInstall.setGatewayUuid(installHistory.getGateway().getUuid());
//                inProgressInstall.setVin(installHistory.getAsset().getVin());
//                inProgressInstall.setAssignedName(installHistory.getAsset().getAssignedName());
//                inProgressInstall.setImei(installHistory.getGateway().getImei());
//                inProgressInstall.setInstallCode(installHistory.getInstallCode());
//                inProgressInstallList.add(inProgressInstall);
//                inProgressInstall.setStatus(installHistory.getStatus().getValue());
//                inProgressInstall.setMacAddress(installHistory.getGateway().getMacAddress());
//            }
//        }
//        return inProgressInstallList;
//    }
//
//    public List<LogIssueBean> convertLogIssueToLogIssueBean(List<LogIssue> logIssueList) {
//        List<LogIssueBean> logIssueBeanList = new ArrayList<>();
//        for (LogIssue logIssue : logIssueList) {
//        	if(logIssue.getType()==null || logIssue.getType().equalsIgnoreCase("SENSOR")) {
//            LogIssueBean logIssueBean = new LogIssueBean();
//            logIssueBean.setInstallCode(logIssue.getInstallHistory().getInstallCode());
//            logIssueBean.setComment(logIssue.getComment());
//            logIssueBean.setCreatedOn(logIssue.getCreatedOn().toString());
//            logIssueBean.setIssueType(logIssue.getIssueType());
//            if(logIssue.getReasonCode() != null) {
//                logIssueBean.setReasonCode(logIssue.getReasonCode().getCode());
//                logIssueBean.setReasonCodeDisplayName(logIssue.getReasonCode().getValue());
//            }
//            logIssueBean.setSensorUuid(logIssue.getSensor().getUuid());
//            logIssueBean.setSensorProductCode(logIssue.getSensor().getProductCode());
//            String sensorDisplayName = restUtils.getLookupValueFromDeviceService(logIssue.getSensor().getProductCode());
//            if (sensorDisplayName != null && !sensorDisplayName.isEmpty()) {
//                logIssueBean.setSensorProductName(sensorDisplayName);
//            } else {
//                logIssueBean.setSensorProductName(logIssue.getSensor().getProductName());
//            }
//            logIssueBean.setData(logIssue.getData());
//            logIssueBean.setStatus(logIssue.getStatus().getValue());
//            logIssueBean.setIssueUuid(logIssue.getUuid());
//            logIssueBeanList.add(logIssueBean);
//        }
//        }
//        return logIssueBeanList;
//    }
//
	public UpdateAssetToDeviceForInstallationRequest convertInstallHistoryToUpdateAssetToDeviceForInstallationRequest(
			InstallHistory installHistory) {
		String methodName = "convertInstallHistoryToUpdateAssetToDeviceForInstallationRequest";
		Context context = new Context();
		String logUUid = context.getLogUUId();
		Logutils.log(className, methodName, logUUid,
				" Inside convertInstallHistoryToUpdateAssetToDeviceForInstallationRequest Method From BeanConverter "
						+ "InstallHistory " + installHistory.getInstallCode(),
				logger);
		UpdateAssetToDeviceForInstallationRequest request = new UpdateAssetToDeviceForInstallationRequest();
		request.setAssignedName(installHistory.getAsset().getAssignedName());
		request.setVin(installHistory.getAsset().getVin());
		request.setImei(installHistory.getDevice().getImei());
		request.setInstallTimestamp(Timestamp.from(installHistory.getDateEnded()));
		if (installHistory.getAsset().getOrganisation() != null) {
			request.setCustomerName(installHistory.getAsset().getOrganisation().getOrganisationName());
		} else {
			request.setCustomerName(installHistory.getDevice().getOrganisation().getOrganisationName());
		}
		request.setAssetType(installHistory.getAsset().getCategory().getValue());
		request.setMake(installHistory.getAsset().getManufacturer().getName());
		if (installHistory.getAsset().getManufacturerDetails() != null) {
			request.setModel(installHistory.getAsset().getManufacturerDetails().getModel());
		}
		request.setYear(installHistory.getAsset().getYear());
		Logutils.log(className, methodName, logUUid,
				" Exiting From convertInstallHistoryToUpdateAssetToDeviceForInstallationRequest ", logger);
		return request;
	}

//    
//    public InstallationSummaryResponseDTO convertInstallHistoryToInstallResponseDTO(InstallHistory ins,Map<String, String> filterValues) throws JsonProcessingException {
//    	InstallationSummaryResponseDTO installResponseDto = new InstallationSummaryResponseDTO();
//    	installResponseDto.setAssetId(ins.getAsset().getAssignedName());
//    	installResponseDto.setInstallCode(ins.getInstallCode());
//    	installResponseDto.setProductName(ins.getGateway().getProductName());
//    	installResponseDto.setProductCode(ins.getGateway().getProductCode());
//    	installResponseDto.setCreatedAt(ins.getCreatedOn());
//    	installResponseDto.setUpdatedAt(ins.getUpdatedOn());
//    	if(ins.getDateStarted() != null) {
//    		installResponseDto.setDateStarted(ins.getDateStarted());
//    	}
//    	if(ins.getUpdatedBy()!=null) {
//    		installResponseDto.setInstallerName(ins.getUpdatedBy().getLastName()+", "+ins.getUpdatedBy().getFirstName()+",");
//    		installResponseDto.setUserId(ins.getUpdatedBy().getId());
//    		if(ins.getUpdatedBy().getCompany()!=null)
//    		installResponseDto.setInstallerCompany(ins.getUpdatedBy().getCompany().getCompanyName());
//    	}
//    	if(ins.getGateway().getType().getValue().equalsIgnoreCase(GatewayType.GATEWAY.getValue())) {
//    	installResponseDto.setDeviceId(ins.getGateway().getImei());
//    	}else if(ins.getGateway().getType().getValue().equalsIgnoreCase(GatewayType.BEACON.getValue())) {
//    		installResponseDto.setDeviceId(ins.getGateway().getMacAddress());	
//    	}
//    	
//    	if(ins != null && ins.getAppVersion() != null) {
//    		installResponseDto.setAppVersion(ins.getAppVersion());
//    	} else {
//    		installResponseDto.setAppVersion("N/A");
//    	}
//    	installResponseDto.setStatus(ins.getStatus().getValue());
//    	if(ins.getStatus().equals(InstallHistoryStatus.FINISHED) || ins.getStatus().equals(InstallHistoryStatus.PROBLEM)) {
//    		installResponseDto.setInstalled(ins.getDateEnded().toString());
//        }
//    	 List<AttributeValue> atrvalue = restUtils.getAttributeValueDeviceId(ins.getGateway().getImei());
//    	 if(atrvalue.size()>0) {
//    	 for(AttributeValue att : atrvalue) {
//    		 if(att.getAttribute()!=null) {
//    		 if(att.getAttribute().getAttributeName().equalsIgnoreCase("Battery")) {
//    			 installResponseDto.setBatteryVoltage(att.getValue());
//    			 installResponseDto.setBatteryStatus(att.getStatus());
//    		 }if(att.getAttribute().getAttributeName().equalsIgnoreCase("Primary")) {
//    			 installResponseDto.setPrimaryVoltage(att.getValue());
//    			 installResponseDto.setPrimaryStatus(att.getStatus());
//    		 }if(att.getAttribute().getAttributeName().equalsIgnoreCase("Secondary")) {
//    			 installResponseDto.setSecondaryVoltage(att.getValue());
//    			 installResponseDto.setSecondaryStatus(att.getStatus());
//    		 }
//    		} 
//    	 }
//    	 }
//    	if(ins.getGateway()!=null && ins.getGateway().getSensors().size()>0) {
//    		List<Sensor> sensors = ins.getGateway().getSensors();
//    		for(Sensor sen : sensors) {
//    			String resp="N/A";
//    		List<InstallLog> inslog = installLogRepository.findBySensorUuidOrderByTimeStampDesc(sen.getUuid());	
//    		List<LogIssue> issueLogs = ilogIssueRepository.findBySensorUuidOrderByCreatedOnDesc(sen.getUuid());
//    		if(inslog.size()>0) {
//    			if(inslog.get(0).getEventType().getValue().equalsIgnoreCase("Sensor Installation Complete") && (issueLogs.size()==0 || issueLogs.get(0).getStatus().getValue().equalsIgnoreCase("Resolved"))) {
//    				resp="PASS";
//    			}
//    			if((inslog.get(0).getEventType().getValue().equalsIgnoreCase("Installation") || inslog.get(0).getEventType().getValue().equalsIgnoreCase("Verification") || inslog.get(0).getEventType().getValue().equalsIgnoreCase("Issue")) && issueLogs.size() > 0 && issueLogs.get(0).getStatus().getValue().equalsIgnoreCase("Open")) {
//        		    resp="FAIL";
//        			}
//    		}else {
//    			resp = "PENDING";
//    		}
//    		
//    		if(filterValues.containsKey("cargo_sensor") &&  (sen.getProductCode().equalsIgnoreCase("77-S102") || sen.getProductCode().equalsIgnoreCase("77-S180"))) {
//				String filter = filterValues.get("cargo_sensor");
//				String[] allStatus = filter.split(",");
//				for(String s : allStatus) {
//				if(s.equalsIgnoreCase(resp)) {
//					installResponseDto.setFilter(true);			
//					}
//				}
//    		}
//    		
//				/*if(filterValues.containsKey("micro_sp_receiver") &&  sen.getProductCode().equalsIgnoreCase("77-S206")) {
//					String filter = filterValues.get("micro_sp_receiver");
//					String[] allStatus = filter.split(",");
//					for(String s : allStatus) {
//					if(s.equalsIgnoreCase(resp)) {
//						installResponseDto.setFilter(true);			
//						}
//					}
//				}*/
//    		
//				/*if(filterValues.containsKey("pct_cargo_sensor") &&  sen.getProductCode().equalsIgnoreCase("77-S180")) {
//					String filter = filterValues.get("pct_cargo_sensor");
//					String[] allStatus = filter.split(",");
//					for(String s : allStatus) {
//					if(s.equalsIgnoreCase(resp)) {
//						installResponseDto.setFilter(true);			
//						}
//					}
//				}*/
//    		if(filterValues.containsKey("cargo_camera_sensor") &&  sen.getProductCode().equalsIgnoreCase("77-S111")) {
//				String filter = filterValues.get("cargo_camera_sensor");
//				String[] allStatus = filter.split(",");
//				for(String s : allStatus) {
//				if(s.equalsIgnoreCase(resp)) {
//					installResponseDto.setFilter(true);			
//					}
//				}
//    		}
//    		if(filterValues.containsKey("abssensor") &&  sen.getProductCode().equalsIgnoreCase("77-H101")) {
//				String filter = filterValues.get("abssensor");
//				String[] allStatus = filter.split(",");
//				for(String s : allStatus) {
//					if(s.equalsIgnoreCase(resp)) {
//						installResponseDto.setFilter(true);			
//						}
//					}
//					
//    		}	
//    		if(filterValues.containsKey("door_sensor") &&  sen.getProductCode().equalsIgnoreCase("77-S108")) {
//				String filter = filterValues.get("door_sensor");
//				String[] allStatus = filter.split(",");
//				for(String s : allStatus) {
//					if(s.equalsIgnoreCase(resp)) {
//						installResponseDto.setFilter(true);			
//						}
//					}
//					
//    		}	
//    		if(filterValues.containsKey("atis_sensor") &&  sen.getProductCode().equalsIgnoreCase("77-S120")) {
//				String filter = filterValues.get("atis_sensor");
//				String[] allStatus = filter.split(",");
//				for(String s : allStatus) {
//					if(s.equalsIgnoreCase(resp)) {
//						installResponseDto.setFilter(true);			
//						}
//					}
//					
//    		}	
//    		if(filterValues.containsKey("light_sentry") &&  sen.getProductCode().equalsIgnoreCase("77-S107")) {
//				String filter = filterValues.get("light_sentry");
//				String[] allStatus = filter.split(",");
//				for(String s : allStatus) {
//					if(s.equalsIgnoreCase(resp)) {
//						installResponseDto.setFilter(true);			
//						}
//					}
//					
//    		}	
//    		if(filterValues.containsKey("tpms") &&  (sen.getProductCode().equalsIgnoreCase("77-S500") || sen.getProductCode().equalsIgnoreCase("77-S177"))) {
//				String filter = filterValues.get("tpms");
//				String[] allStatus = filter.split(",");
//				for(String s : allStatus) {
//					if(s.equalsIgnoreCase(resp)) {
//						installResponseDto.setFilter(true);			
//						}
//					}
//					
//    		}	
//    		if(filterValues.containsKey("wheel_end") &&  sen.getProductCode().equalsIgnoreCase("77-S119")) {
//				String filter = filterValues.get("wheel_end");
//				String[] allStatus = filter.split(",");
//				for(String s : allStatus) {
//					if(s.equalsIgnoreCase(resp)) {
//						installResponseDto.setFilter(true);			
//						}
//					}
//					
//    		}	
//    		if(filterValues.containsKey("air_tank") && (sen.getProductCode().equalsIgnoreCase("77-S137") || sen.getProductCode().equalsIgnoreCase("77-S202"))) {
//				String filter = filterValues.get("air_tank");
//				String[] allStatus = filter.split(",");
//				for(String s : allStatus) {
//					if(s.equalsIgnoreCase(resp)) {
//						installResponseDto.setFilter(true);			
//						}
//					}
//					
//    		}
//    		if(filterValues.containsKey("regulator") && (sen.getProductCode().equalsIgnoreCase("77-S164") || sen.getProductCode().equalsIgnoreCase("77-S203"))) {
//				String filter = filterValues.get("regulator");
//				String[] allStatus = filter.split(",");
//				for(String s : allStatus) {
//					if(s.equalsIgnoreCase(resp)) {
//						installResponseDto.setFilter(true);			
//						}
//					}
//					
//    		}
//    		if(filterValues.containsKey("reciever") && (sen.getProductCode().equalsIgnoreCase("77-S196") || sen.getProductCode().equalsIgnoreCase("77-S206"))) {
//				String filter = filterValues.get("reciever");
//				String[] allStatus = filter.split(",");
//				for(String s : allStatus) {
//					if(s.equalsIgnoreCase(resp)) {
//						installResponseDto.setFilter(true);			
//						}
//					}
//					
//    		}
//    		
//				/*if(filterValues.containsKey("micro_sp_air_tank") &&  sen.getProductCode().equalsIgnoreCase("77-S202")) {
//					String filter = filterValues.get("micro_sp_air_tank");
//					String[] allStatus = filter.split(",");
//					for(String s : allStatus) {
//						if(s.equalsIgnoreCase(resp)) {
//							installResponseDto.setFilter(true);			
//							}
//						}
//						
//				}*/
//    		
//				/*if(filterValues.containsKey("micro_sp_regulator") &&  sen.getProductCode().equalsIgnoreCase("77-S203")) {
//					String filter = filterValues.get("micro_sp_regulator");
//					String[] allStatus = filter.split(",");
//					for(String s : allStatus) {
//						if(s.equalsIgnoreCase(resp)) {
//							installResponseDto.setFilter(true);			
//							}
//						}
//						
//				}*/
//			
//    		if(sen.getProductCode().equalsIgnoreCase("77-H101")) {
//    			installResponseDto.setABSSensor(resp);
//    		}else if(sen.getProductCode().equalsIgnoreCase("77-S119")) {
//    			installResponseDto.setWheelEnd(resp);
//    		}else if(sen.getProductCode().equalsIgnoreCase("77-S102") || sen.getProductCode().equalsIgnoreCase("77-S180")) {
//    			installResponseDto.setCargoSensor(resp);
//    		} else if(sen.getProductCode().equalsIgnoreCase("77-S111")) {
//    			installResponseDto.setCargoCameraSensor(resp);
//    		} else if(sen.getProductCode().equalsIgnoreCase("77-S137") || sen.getProductCode().equalsIgnoreCase("77-S202")) {
//    			installResponseDto.setAirTank(resp);
//    			if(inslog.size()>0 && inslog.get(0).getSensorId()!=null) {
//    				String sensorId = inslog.get(0).getSensorId();
//					if (sensorId != null && sensorId.startsWith("0x")) {
//						sensorId = sensorId.replace("0x", "");
//					}
//        			installResponseDto.setAirTankId(sensorId);
//        			installResponseDto.setAirTankPressure(inslog.get(0).getSensorPressure());
//        			installResponseDto.setAirTankTemperature(inslog.get(0).getSensorTemperature());
//        	}
//    		}else if(sen.getProductCode().equalsIgnoreCase("77-S500") || sen.getProductCode().equalsIgnoreCase("77-S177")) {
//					if (inslog.size() > 0) {
//						for (InstallLog inlog : inslog) {
//							if (inlog.getInstanceType() != null
//									&& !inlog.getInstanceType().equalsIgnoreCase(InstanceType.DEFAULT.toString())) {
//								String respTpms = "PENDING";
//								if (inlog.getEventType().getValue().equalsIgnoreCase("Sensor Installation Complete")
//										&& (issueLogs.size() == 0
//												|| issueLogs.get(0).getStatus().getValue().equalsIgnoreCase("Resolved"))) {
//									respTpms = "PASS";
//								}
//								if ((inlog.getEventType().getValue().equalsIgnoreCase("Installation")
//										|| inlog.getEventType().getValue().equalsIgnoreCase("Verification")
//										|| inlog.getEventType().getValue().equalsIgnoreCase("Issue")) && issueLogs.size() > 0
//										&& issueLogs.get(0).getStatus().getValue().equalsIgnoreCase("Open")) {
//									respTpms = "FAIL";
//								}
//								if (inlog.getEventType().getValue().equalsIgnoreCase("Installation") && inlog.getInstanceType() != null
//										&& !inlog.getInstanceType().equalsIgnoreCase("Default")) {
//									respTpms = "PENDING";
//								}
//								
//								if (inlog.getInstanceType().equalsIgnoreCase("0x22")) {
//									if (inlog.getSensorId() != null) {
//										String sensorId = inlog.getSensorId();
//										if (inlog.getSensorId().startsWith("0x")) {
//											sensorId = sensorId.replace("0x", "");
//										}
//										installResponseDto.setTpmsLif(sensorId);
//										installResponseDto.setTpmsLifStatus(respTpms);
//										installResponseDto.setTpmsLifPressure(inslog.get(0).getSensorPressure());
//					        			installResponseDto.setTpmsLifTemperature(inslog.get(0).getSensorTemperature());
//									}
//
//								}
//								if (inlog.getInstanceType().equalsIgnoreCase("0x26")) {
//									if (inlog.getSensorId() != null) {
//										String sensorId = inlog.getSensorId();
//										if (inlog.getSensorId().startsWith("0x")) {
//											sensorId = sensorId.replace("0x", "");
//										}
//										installResponseDto.setTpmsLir(sensorId);
//										installResponseDto.setTpmsLirStatus(respTpms);
//										installResponseDto.setTpmsLirPressure(inslog.get(0).getSensorPressure());
//					        			installResponseDto.setTpmsLirTemperature(inslog.get(0).getSensorTemperature());
//									}
//								}
//								if (inlog.getInstanceType().equalsIgnoreCase("0x21")) {
//									if (inlog.getSensorId() != null) {
//										String sensorId = inlog.getSensorId();
//										if (inlog.getSensorId().startsWith("0x")) {
//											sensorId = sensorId.replace("0x", "");
//										}
//										installResponseDto.setTpmsLof(sensorId);
//										installResponseDto.setTpmsLofStatus(respTpms);
//										installResponseDto.setTpmsLofPressure(inslog.get(0).getSensorPressure());  
//										installResponseDto.setTpmsLofTemperature(inslog.get(0).getSensorTemperature()); 
//									}   
//								}
//								if (inlog.getInstanceType().equalsIgnoreCase("0x25")) {
//									if (inlog.getSensorId() != null) {
//										String sensorId = inlog.getSensorId();
//										if (inlog.getSensorId().startsWith("0x")) {
//											sensorId = sensorId.replace("0x", "");
//										}
//										installResponseDto.setTpmsLor(sensorId);
//										installResponseDto.setTpmsLorStatus(respTpms);
//										installResponseDto.setTpmsLorPressure(inslog.get(0).getSensorPressure());      
//										installResponseDto.setTpmsLorTemperature(inslog.get(0).getSensorTemperature());
//										
//									}
//								}
//								if (inlog.getInstanceType().equalsIgnoreCase("0x23")) {
//									if (inlog.getSensorId() != null) {
//										String sensorId = inlog.getSensorId();
//										if (inlog.getSensorId().startsWith("0x")) {
//											sensorId = sensorId.replace("0x", "");
//										}
//										installResponseDto.setTpmsRif(sensorId);
//										installResponseDto.setTpmsRifStatus(respTpms);
//										installResponseDto.setTpmsRifPressure(inslog.get(0).getSensorPressure());      
//										installResponseDto.setTpmsRifTemperature(inslog.get(0).getSensorTemperature());
//									}
//								}
//								if (inlog.getInstanceType().equalsIgnoreCase("0x27")) {
//									if (inlog.getSensorId() != null) {
//										String sensorId = inlog.getSensorId();
//										if (inlog.getSensorId().startsWith("0x")) {
//											sensorId = sensorId.replace("0x", "");
//										}
//										installResponseDto.setTpmsRir(sensorId);
//										installResponseDto.setTpmsRirStatus(respTpms);
//										installResponseDto.setTpmsRirPressure(inslog.get(0).getSensorPressure());      
//										installResponseDto.setTpmsRirTemperature(inslog.get(0).getSensorTemperature());
//									}
//								}
//								if (inlog.getInstanceType().equalsIgnoreCase("0x24")) {
//									if (inlog.getSensorId() != null) {
//										String sensorId = inlog.getSensorId();
//										if (inlog.getSensorId().startsWith("0x")) {
//											sensorId = sensorId.replace("0x", "");
//										}
//										installResponseDto.setTpmsRof(sensorId);
//										installResponseDto.setTpmsRofStatus(respTpms);
//										installResponseDto.setTpmsRofPressure(inslog.get(0).getSensorPressure());      
//										installResponseDto.setTpmsRofTemperature(inslog.get(0).getSensorTemperature());
//									}
//								}
//								if (inlog.getInstanceType().equalsIgnoreCase("0x28")) {
//									if (inlog.getSensorId() != null) {
//										String sensorId = inlog.getSensorId();
//										if (inlog.getSensorId().startsWith("0x")) {
//											sensorId = sensorId.replace("0x", "");
//										}
//										installResponseDto.setTpmsRor(sensorId);
//										installResponseDto.setTpmsRorStatus(respTpms);
//										installResponseDto.setTpmsRorPressure(inslog.get(0).getSensorPressure());      
//										installResponseDto.setTpmsRorTemperature(inslog.get(0).getSensorTemperature());
//									}
//								}
//							}
//						}
//					}
//    			installResponseDto.setTpms(resp);
//    		}else if(sen.getProductCode().equalsIgnoreCase("77-S164") || sen.getProductCode().equalsIgnoreCase("77-S203")) {
//    			installResponseDto.setRegulator(resp);
//    			if(inslog.size()>0 && inslog.get(0).getSensorId()!=null) {
//    				String sensorId = inslog.get(0).getSensorId();
//					if (sensorId != null && sensorId.startsWith("0x")) {
//						sensorId = sensorId.replace("0x", "");
//					}
//        			installResponseDto.setRegulatorId(sensorId);
//        			installResponseDto.setRegulatorPressure(inslog.get(0).getSensorPressure());
//        			installResponseDto.setRegulatorTemperature(inslog.get(0).getSensorTemperature());
//        			}
//    		}else if(sen.getProductCode().equalsIgnoreCase("77-S196") || sen.getProductCode().equalsIgnoreCase("77-S206")) {
//    			installResponseDto.setReciever(resp);
//    		}else if(sen.getProductCode().equalsIgnoreCase("77-S108")) {
//    			if(inslog != null && inslog.size() > 0) {
//    				for(InstallLog inLg : inslog) {
//    					if(inLg.getType()!=null) {
//    						installResponseDto.setDoorType(inLg.getType());
//        				}
//    				}
//    			}
//    			if(sen.getMacAddress()!=null) {
//    				String doorMacAdd = sen.getMacAddress();
//    				if(doorMacAdd.contains("-")) {
//    					doorMacAdd = doorMacAdd.replaceAll("-", ":");
//    				}
//    				installResponseDto.setDoorMacAddress(doorMacAdd);
//    			}
//    			installResponseDto.setDoorSensor(resp);
//    		}else if(sen.getProductCode().equalsIgnoreCase("77-S107")) {
//    			installResponseDto.setLightSentry(resp);
//    		}else if(sen.getProductCode().equalsIgnoreCase("77-S120")) {
//    			installResponseDto.setAtisSensor(resp);
//    		}else if(sen.getProductCode().equalsIgnoreCase("77-S188")) {
//    			installResponseDto.setLampCheckAtis(resp);
//    			if(sen.getMacAddress()!=null) {
//    				String atisMacAdd = sen.getMacAddress();
//    				if(atisMacAdd.contains("-")) {
//    					atisMacAdd = atisMacAdd.replaceAll("-", ":");
//    				}
//    				installResponseDto.setLampCheckAtisMac(atisMacAdd);
//    			}
//    		}
//    		
//    		
//    		
//    		}
//    	}
//    	return installResponseDto;
//
//    
//    }
//    public InstallationDetailResponseDTO convertInstallationDetail(InstallHistory ins) throws JsonProcessingException{
//    	Map<String, String> positionMap = new HashMap<>();
//    	positionMap.put("0x21", "LOF-1");
//    	positionMap.put("0x22", "LIF-2");
//    	positionMap.put("0x23", "RIF-3");
//    	positionMap.put("0x24", "ROF-4");
//    	positionMap.put("0x25", "LOR-5");
//    	positionMap.put("0x26", "LIR-6");
//    	positionMap.put("0x27", "RIR-7");
//    	positionMap.put("0x28", "ROR-8");
//    	InstallationDetailResponseDTO installdto = new InstallationDetailResponseDTO();
//    	if(ins.getInstallCode()!=null) {
//    	installdto.setInstallCode(ins.getInstallCode());
//    	}
//    	
//    	if(ins.getDateStarted() != null) {
//    		installdto.setDateStarted(ins.getDateStarted().toString());
//    	}
//    	
//    	if(ins.getDateEnded() != null) {
//    	installdto.setInstalledDate(ins.getDateEnded().toString());
//    	}
//    	
//    	if(ins.getUpdatedOn() != null) {
//        	installdto.setLastUpdated(ins.getUpdatedOn().toString());
//        }
//    	
//    	if(ins.getCreatedBy()!=null) {
//    		if(ins.getUpdatedBy()!=null) {
//    	installdto.setInstallerName(ins.getUpdatedBy().getFirstName()+" "+ins.getUpdatedBy().getLastName());
//    	installdto.setInstallerPhone(ins.getUpdatedBy().getPhone());
//    	}else {
//    		installdto.setInstallerName(ins.getCreatedBy().getFirstName()+" "+ins.getCreatedBy().getLastName());
//        	installdto.setInstallerPhone(ins.getCreatedBy().getPhone());
//    	}
//    	}
//    	
//    	if(ins != null && ins.getAppVersion() != null && !ins.getAppVersion().isEmpty()) {
//    		installdto.setAppVersion(ins.getAppVersion());
//    	} else {
//    		installdto.setAppVersion("N/A");
//    	}
//    	if(ins.getAsset().getUuid()!=null) {
//    	Asset asset = ins.getAsset();
//    	installdto.setAssetStatus(ins.getStatus().getValue());
//    	installdto.setAssetDetails(ins.getCompany().getCompanyName()+" "+ins.getAsset().getCategory().getValue()+" : "+ins.getAsset().getAssignedName());
//    	installdto.setAssetUuid(ins.getAsset().getUuid());
//        AssetDetailResponseDTO assetResponseDto = new AssetDetailResponseDTO();
//        assetResponseDto.setAssetUuid(asset.getUuid());
//        assetResponseDto.setAssignedName(asset.getAssignedName());
//        assetResponseDto.setStatus(asset.getStatus().getValue());
//        assetResponseDto.setCategory(asset.getCategory().getValue());
//        assetResponseDto.setVin(asset.getVin());
//        ManufactureDetailResponseDTO manufacturerDetailsDTO = new ManufactureDetailResponseDTO();
//        if (asset.getManufacturer() != null) {
//            manufacturerDetailsDTO.setMake(asset.getManufacturer().getName());
//        }
//        if (asset.getManufacturerDetails() != null) {
//            manufacturerDetailsDTO.setModel(asset.getManufacturerDetails().getModel());
//        }
//        manufacturerDetailsDTO.setYear(asset.getYear());
//        assetResponseDto.setManufacturerDetails(manufacturerDetailsDTO);
//        assetResponseDto.setEligibleGateway(asset.getGatewayEligibility());
//        assetResponseDto.setCan(asset.getCompany().getAccountNumber());
//        assetResponseDto.setDisplayName(asset.getAssignedName());
//        if (asset.getCreatedOn() != null)
//            assetResponseDto.setDatetimeCreated(asset.getCreatedOn().toString());
//        if (asset.getUpdatedOn() != null)
//            assetResponseDto.setDatetimeUpdated(asset.getUpdatedOn().toString());
//        assetResponseDto.setCompanyName(asset.getCompany().getCompanyName());
//        assetResponseDto.setIsVinValidated(asset.getIsVinValidated());
//        assetResponseDto.setComment(asset.getComment());
//        installdto.setAssetDetailsResposne(assetResponseDto);
//    
//    	}
//    	if(ins.getGateway().getUuid()!=null) {
//    		installdto.setGatewayDetails(ins.getGateway().getProductName()+" "+ins.getGateway().getProductCode());
//    		installdto.setDeviceId(ins.getGateway().getImei());
//    	}
//    	List<Sensor> sensors = ins.getGateway().getSensors();
//    	Set<SensorResponseDTO> sensorResponse = new HashSet<>();
//		for (Sensor sen : sensors) {
//			String resp = "PENDING";
//			List<InstallLog> inslog = installLogRepository.findBySensorUuidOrderByTimeStampDesc(sen.getUuid());
//			List<LogIssue> issueLogs = ilogIssueRepository.findBySensorUuidOrderByCreatedOnDesc(sen.getUuid());
//			if (inslog.size() > 0) {
//				for (InstallLog insl : inslog) {
//					if (insl.getEventType().getValue().equalsIgnoreCase("Sensor Installation Complete")
//							&& (issueLogs.size() == 0 || issueLogs.get(0).getStatus().getValue().equalsIgnoreCase("Resolved"))) {
//						resp = "PASS";
//					}
//					if ((insl.getEventType().getValue().equalsIgnoreCase("Installation")
//							|| insl.getEventType().getValue().equalsIgnoreCase("Verification")
//							|| insl.getEventType().getValue().equalsIgnoreCase("Issue")) && issueLogs.size() > 0
//							&& issueLogs.get(0).getStatus().getValue().equalsIgnoreCase("Open")) {
//						resp = "FAIL";
//					}
//					if (insl.getEventType().getValue().equalsIgnoreCase("Installation") && insl.getInstanceType() != null
//							&& !insl.getInstanceType().equalsIgnoreCase("Default")) {
//						resp = "PENDING";
//					}
//					SensorResponseDTO s = new SensorResponseDTO();
//					s.setSensorName(sen.getProductName());
//					s.setProductCode(sen.getProductCode());
//					s.setSensorUuid(sen.getUuid());
//					s.setMacAddress(sen.getMacAddress());
//					if (insl.getSensorId() != null) {
//						String sensorId = insl.getSensorId();
//						if (insl.getSensorId().startsWith("0x")) {
//							sensorId = sensorId.replace("0x", "");
//						}
//						s.setSensorId(sensorId);
//						s.setSensorPressure(insl.getSensorPressure());
//	        			s.setSensorTemperature(insl.getSensorTemperature());
//					}
//					
//					if (insl.getInstanceType() != null && insl.getInstanceType() != null
//							&& !insl.getInstanceType().equalsIgnoreCase("Default")) {
//						String pos = positionMap.get(insl.getInstanceType());
//						if (pos != null) {
//							s.setPosition(pos);
//							s.setSensorPressure(insl.getSensorPressure());
//		        			s.setSensorTemperature(insl.getSensorTemperature());
//						}
//					} else {
//						s.setPosition("--");
//						s.setSensorPressure("--");
//	        			s.setSensorTemperature("--");
//					}
//					
//					s.setStatus(resp);
//					if (insl.getType() != null) {
//						s.setType(insl.getType());
//					} else {
//						s.setType("--");
//					}
//					
//					sensorResponse.add(s);
//				}
//			} else {
//				SensorResponseDTO s = new SensorResponseDTO();
//				s.setSensorName(sen.getProductName());
//				s.setProductCode(sen.getProductCode());
//				s.setSensorUuid(sen.getUuid());
//				s.setMacAddress(sen.getMacAddress());
//				s.setType("--");
//				s.setPosition("--");
//				s.setSensorPressure("--");
//    			s.setSensorTemperature("--");
//				s.setStatus("PENDING");
//				sensorResponse.add(s);
//
//			}
//
//		}
//
//			 SensorResponseDTO sensor = null;
//			 int count = 0;
//			 if(sensorResponse != null && sensorResponse.size() > 0) {
//				for (SensorResponseDTO sen : sensorResponse) {
//					if (sen.getProductCode() != null && sen.getProductCode().equalsIgnoreCase("77-S108")) {
//						if (sen.getType() != null) {
//							sensor = sen;
//						}
//						count++;
//					}
//				}
//				if (count > 1) {
//					sensorResponse.removeIf(filter -> filter.getProductCode() != null && filter.getProductCode().equalsIgnoreCase("77-S108"));
//					if(sensor != null) {
//						sensorResponse.add(sensor);
//					}
//				}
//			}
//    	 installdto.setSensorDetails(sensorResponse);
//    	 List<LogIssue> logissue = ilogIssueRepository.findByInstallCode(ins.getInstallCode());
//    	 List<LoggedIssueResponseDTO> logResposne = new ArrayList<>();
//    	 for(LogIssue issue:logissue) {
//    	if(issue.getType()==null || issue.getType().equalsIgnoreCase("SENSOR")){
//    		 LoggedIssueResponseDTO li = new LoggedIssueResponseDTO();
//    		 li.setComment(issue.getComment());
//    		 li.setDate(issue.getCreatedOn().toString());
//    		 if(issue.getReasonCode()!=null) {
//    		 li.setReasonCode(issue.getReasonCode().getValue());
//    		 }
//    		 li.setStatus(issue.getStatus().getValue());
//    		 li.setSensor(issue.getSensor().getProductName());
//    		 if(issue.getCreatedBy()!=null) {
//    			 if(issue.getUpdatedBy()!=null) {
//    		    li.setUser(issue.getUpdatedBy().getFirstName()+" "+issue.getUpdatedBy().getLastName());
//    			 }else {
//    				 li.setUser(issue.getCreatedBy().getFirstName()+" "+issue.getCreatedBy().getLastName());
//    			 }
//    		 }
//    		 logResposne.add(li);
//    	 } else if(issue.getType()!=null && issue.getType().equalsIgnoreCase("GATEWAY")) {
//    		 LoggedIssueResponseDTO li = new LoggedIssueResponseDTO();
//    		 li.setComment(issue.getComment());
//    		 li.setDate(issue.getCreatedOn() != null ? issue.getCreatedOn().toString() : null);
//    		 if(issue.getReasonCode()!=null) {
//    		 li.setReasonCode(issue.getReasonCode().getValue());
//    		 }
//    		 
//    		 if(issue.getStatus()!=null) {
//    			 li.setStatus(issue.getStatus().getValue());
//    		 }
//    		 
//    		 if(issue.getSensor()!=null) {
//    			 li.setSensor(issue.getSensor().getProductName());
//    		 } else if(issue.getGateway() != null){
//    			 li.setSensor(issue.getGateway().getProductName());
//    		 }
//    		
//    		 if(issue.getCreatedBy()!=null) {
//    			 if(issue.getUpdatedBy()!=null) {
//    		    li.setUser(issue.getUpdatedBy().getFirstName()+" "+issue.getUpdatedBy().getLastName());
//    			 }else {
//    				 li.setUser(issue.getCreatedBy().getFirstName()+" "+issue.getCreatedBy().getLastName());
//    			 }
//    		 }
//    		 logResposne.add(li);
//    	 }
//    	}
//    	 installdto.setLoggedIssues(logResposne);
//    	 List<AttributeValue> atrvalue = restUtils.getAttributeValueDeviceId(ins.getGateway().getImei());
//    	 List<BatteryResponseDTO> batteryresposne = new ArrayList<>();
//    	 for(AttributeValue att : atrvalue) {
//    	 BatteryResponseDTO battery = new BatteryResponseDTO();
//    	 battery.setPowerSource(att.getAttribute().getAttributeName());
//    	 battery.setStatus(att.getStatus());
//    	 battery.setVoltage(att.getValue());
//    	 batteryresposne.add(battery);
//    	 }
//    	 installdto.setBatteryData(batteryresposne);
//		return installdto;
//    	
//    }

	public InstallationStatusResponse createInstallationStatusResponseFromInstallHistory(
			InstallHistory installHistory) {
		Context context = new Context();
		String logUuId = context.getLogUUId();
		String methodName = "createInstallationStatusResponseFromInstallHistory";
		Logutils.log(className, methodName, logUuId, " Inside createInstallationStatusResponseFromInstallHistory "
				+ "InstallHistory " + installHistory.getInstallCode(), logger);

		InstallationStatusResponse installationStatusResponse = new InstallationStatusResponse();
		installationStatusResponse.setInstallStatus(installHistory.getStatus().getValue());
		installationStatusResponse.setAssetUuid(installHistory.getAsset().getUuid());
		installationStatusResponse.setAssetStatus(installHistory.getAsset().getStatus().getValue());
		installationStatusResponse.setGatewayUuid(installHistory.getDevice().getUuid());
		installationStatusResponse.setGatewayStatus(installHistory.getDevice().getStatus().getValue());
		if (installHistory.getDevice().getUpdatedBy() != null) {
			installationStatusResponse.setGatewayDatetimeRT(installHistory.getDevice().getUpdatedBy().toString());
		}
		Logutils.log(className, methodName,
				" Before calling installLogRepository.findByInstallHistoryIdAndEventType method ", logger);
		List<InstallLog> gatewayInstallLogList = installLogRepository
				.findByInstallHistoryIdAndEventType(installHistory.getId(), EventType.GATEWAY_INSTALLATION_COMPLETE);
		if (gatewayInstallLogList != null && !gatewayInstallLogList.isEmpty()) {
			installationStatusResponse.setGatewayData(gatewayInstallLogList.get(0).getData());
		}
		Logutils.log(className, methodName, " Before calling deviceDeviceXrefRepository.findByDeviceUuid method ",
				logger);
		List<Device_Device_xref> sensorList = deviceDeviceXrefRepository
				.findByDeviceUuid(installHistory.getDevice().getUuid());
		List<InstallationStatusSensor> sensorStatusList = new ArrayList<>();
		for (Device_Device_xref sensor : sensorList) {
			InstallationStatusSensor installationStatusSensor = new InstallationStatusSensor();
			installationStatusSensor.setSensorUuid(sensor.getSensorUuid().getUuid());
			installationStatusSensor.setSensorStatus(sensor.getSensorUuid().getStatus().getValue());
			if (sensor.getUpdatedOn() != null) {
				installationStatusSensor.setSensorDatetimeRT(sensor.getUpdatedOn().toString());
			}
			String str = String.valueOf(sensor.getId());
			Logutils.log(className, methodName,
					" Before calling installLogRepository.findByInstallHistoryIdEventTypeAndSensor method ", logger);
			List<InstallLog> sensorInstallLogList = installLogRepository.findByInstallHistoryIdEventTypeAndSensor(
					installHistory.getId(), EventType.SENSOR_INSTALLATION_COMPLETE, str);
			if (sensorInstallLogList != null && !sensorInstallLogList.isEmpty()) {
				installationStatusSensor.setSensorData(sensorInstallLogList.get(0).getData());
			}
			sensorStatusList.add(installationStatusSensor);
		}
		installationStatusResponse.setSensorList(sensorStatusList);
		Logutils.log(className, methodName, logUuId,
				" Exiting From createInstallationStatusResponseFromInstallHistory ", logger);
		return installationStatusResponse;
	}

	public List<InProgressInstall> createInProgressInstallHistoryList(List<InstallHistory> installHistoryList) {
		Context context = new Context();
		String logUuId = context.getLogUUId();
		String methodName = "createInProgressInstallHistoryList";
		Logutils.log(className, methodName, logUuId,
				" Inside createInProgressInstallHistoryList " + "List<InstallHistory> " + installHistoryList, logger);
		List<InProgressInstall> inProgressInstallList = new ArrayList<InProgressInstall>();
		if (installHistoryList != null && installHistoryList.size() > 0) {
			for (InstallHistory installHistory : installHistoryList) {
				InProgressInstall inProgressInstall = new InProgressInstall();
				inProgressInstall.setAssetUuid(installHistory.getAsset().getUuid());
				inProgressInstall.setGatewayUuid(installHistory.getDevice().getUuid());
				inProgressInstall.setVin(installHistory.getAsset().getVin());
				inProgressInstall.setAssignedName(installHistory.getAsset().getAssignedName());
				inProgressInstall.setImei(installHistory.getDevice().getImei());
				inProgressInstall.setInstallCode(installHistory.getInstallCode());
				inProgressInstallList.add(inProgressInstall);
				inProgressInstall.setStatus(installHistory.getStatus().getValue());
				inProgressInstall.setMacAddress(installHistory.getDevice().getMacAddress());
			}
		}
		Logutils.log(className, methodName, logUuId, " Exiting From createInProgressInstallHistoryList ", logger);
		return inProgressInstallList;
	}

	public InstallationDetailResponseDTO convertInstallationDetail(String logUUid, InstallHistory ins)
			throws JsonProcessingException {
		String methodName = "convertInstallationDetail";

		Logutils.log(className, methodName, logUUid, " Inside convertInstallationDetail Method From BeanConverter "
				+ "InstallHistory " + ins.getInstallCode(), logger);
		Map<String, String> positionMap = new HashMap<>();
		positionMap.put("0x21", "LOF-1");
		positionMap.put("0x22", "LIF-2");
		positionMap.put("0x23", "RIF-3");
		positionMap.put("0x24", "ROF-4");
		positionMap.put("0x25", "LOR-5");
		positionMap.put("0x26", "LIR-6");
		positionMap.put("0x27", "RIR-7");
		positionMap.put("0x28", "ROR-8");

		// color code for lite sentry
		Map<String, String> colorMapForLiteSentry = new HashMap<>();
		colorMapForLiteSentry.put("1", "RED");
		colorMapForLiteSentry.put("2", "GREEN");
		colorMapForLiteSentry.put("3", "YELLOW");
		colorMapForLiteSentry.put("4", "BROWN");
		colorMapForLiteSentry.put("5", "BLACK");

		InstallationDetailResponseDTO installdto = new InstallationDetailResponseDTO();
		if (ins.getInstallCode() != null) {
			installdto.setInstallCode(ins.getInstallCode());
		}
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		formatter.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
		try {
//			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
//			formatter.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
			if (ins.getDateStarted() != null) {
				Date dateStarted = Date.from(ins.getDateStarted());
				String formattedDate = formatter.format(dateStarted);
				installdto.setDateStarted(formattedDate + " PST");
			}

			if (ins.getDateEnded() != null) {
				Date dateEnded = Date.from(ins.getDateEnded());
				String formattedDate = formatter.format(dateEnded);
				installdto.setInstalledDate(formattedDate + " PST");
			}

			if (ins.getUpdatedOn() != null) {
				Date updatedOn = Date.from(ins.getUpdatedOn());
				String formattedDate = formatter.format(updatedOn);
				installdto.setLastUpdated(formattedDate + " PST");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

			if (ins.getDateStarted() != null) {
				installdto.setDateStarted(ins.getDateStarted().toString());
			}

			if (ins.getDateEnded() != null) {
				installdto.setInstalledDate(ins.getDateEnded().toString());
			}

			if (ins.getUpdatedOn() != null) {
				installdto.setLastUpdated(ins.getUpdatedOn().toString());
			}
		}

		if (ins.getCreatedBy() != null) {
			if (ins.getUpdatedBy() != null) {
				installdto.setInstallerName(ins.getUpdatedBy().getFirstName() + " " + ins.getUpdatedBy().getLastName());
				installdto.setInstallerPhone(ins.getUpdatedBy().getPhone());
			} else {
				installdto.setInstallerName(ins.getCreatedBy().getFirstName() + " " + ins.getCreatedBy().getLastName());
				installdto.setInstallerPhone(ins.getCreatedBy().getPhone());
			}
		}

		if (ins.getDevice() != null) {
			try {
				if (ins.getDevice().getQaDate() != null) {
//					SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
//					formatter.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
					Date qaDate = Date.from(ins.getDevice().getQaDate());
					String formattedDate = formatter.format(qaDate);
					installdto.setQaDateAndTime(formattedDate + " PST");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			installdto.setConfigName(ins.getDevice().getConfigName());
			installdto.setQaStatus(ins.getDevice().getQaStatus());
		}

		if (ins != null && ins.getAppVersion() != null && !ins.getAppVersion().isEmpty()) {
			installdto.setAppVersion(ins.getAppVersion());
		} else {
			installdto.setAppVersion("N/A");
		}
		if (ins.getAsset().getUuid() != null) {
			Asset asset = ins.getAsset();
			installdto.setAssetStatus(ins.getStatus().getValue());
			installdto.setAssetDetails(ins.getOrganisation().getOrganisationName() + " "
					+ ins.getAsset().getCategory().getValue() + " : " + ins.getAsset().getAssignedName());
			installdto.setAssetUuid(ins.getAsset().getUuid());
			AssetDetailResponseDTO assetResponseDto = new AssetDetailResponseDTO();
			assetResponseDto.setAssetUuid(asset.getUuid());
			assetResponseDto.setAssignedName(asset.getAssignedName());
			assetResponseDto.setStatus(asset.getStatus().getValue());
			assetResponseDto.setCategory(asset.getCategory().getValue());
			assetResponseDto.setVin(asset.getVin());
			ManufactureDetailResponseDTO manufacturerDetailsDTO = new ManufactureDetailResponseDTO();
			if (asset.getManufacturer() != null) {
				manufacturerDetailsDTO.setMake(asset.getManufacturer().getName());
			}
			if (asset.getManufacturerDetails() != null) {
				manufacturerDetailsDTO.setModel(asset.getManufacturerDetails().getModel());
			}
			manufacturerDetailsDTO.setYear(asset.getYear());
			assetResponseDto.setManufacturerDetails(manufacturerDetailsDTO);
			assetResponseDto.setEligibleGateway(asset.getGatewayEligibility());
			assetResponseDto.setCan(asset.getOrganisation().getAccountNumber());
			assetResponseDto.setDisplayName(asset.getAssignedName());
			if (asset.getCreatedOn() != null)
				assetResponseDto.setDatetimeCreated(asset.getCreatedOn().toString());
			if (asset.getUpdatedOn() != null)
				assetResponseDto.setDatetimeUpdated(asset.getUpdatedOn().toString());
			assetResponseDto.setCompanyName(asset.getOrganisation().getOrganisationName());
			assetResponseDto.setIsVinValidated(asset.getIsVinValidated());
			assetResponseDto.setComment(asset.getComment());
			installdto.setAssetDetailsResposne(assetResponseDto);

		}
		if (ins.getDevice().getUuid() != null) {
			installdto.setGatewayDetails(ins.getDevice().getProductName() + " " + ins.getDevice().getProductCode());
			installdto.setDeviceId(ins.getDevice().getImei());
		}
		Long gatewayId = ins.getDevice().getId();
		Logutils.log(className, methodName, logUUid,
				" Before calling deviceDeviceXrefRepository.findByDeviceid method ", logger);

		List<Device_Device_xref> deviceSensorxreffList = deviceDeviceXrefRepository.findByDeviceid(gatewayId);
		Logutils.log(className, methodName, logUUid, " After calling deviceDeviceXrefRepository.findByDeviceid method ",
				logger);
		// Sensor
		List<Device> sensors = new ArrayList<>();
		deviceSensorxreffList.forEach(sensor -> {
			Device d = sensor.getSensorUuid();
			sensors.add(d);
		});

		Set<SensorResponseDTO> sensorResponse = new HashSet<>();
		for (Device sen : sensors) {
			String resp = "PENDING";
			List<InstallLog> inslog = installLogRepository.findBySensorUuidOrderByTimeStampDesc(sen.getUuid());
			List<LogIssue> issueLogs = ilogIssueRepository.findBySensorUuidOrderByCreatedOnDesc(sen.getUuid());
			if (inslog.size() > 0) {
				if ((sen.getProductCode().equalsIgnoreCase("77-S203")
						|| sen.getProductCode().equalsIgnoreCase("77-S202")
						|| sen.getProductCode().equalsIgnoreCase("77-S202")) && inslog.size() > 1) {
					inslog.removeIf(filter -> filter.getSensorId() == null);
				}
				for (InstallLog insl : inslog) {
					if (insl.getEventType() != null) {
						if (insl.getEventType().getValue().equalsIgnoreCase("Sensor Installation Complete")
								&& (issueLogs.size() == 0
										|| issueLogs.get(0).getStatus().getValue().equalsIgnoreCase("Resolved"))) {
							resp = "PASS";
						}
						if ((insl.getEventType().getValue().equalsIgnoreCase("Installation")
								|| insl.getEventType().getValue().equalsIgnoreCase("Verification")
								|| insl.getEventType().getValue().equalsIgnoreCase("Issue")) && issueLogs.size() > 0
								&& issueLogs.get(0).getStatus().getValue().equalsIgnoreCase("Open")) {
							resp = "FAIL";
						}
						if (insl.getEventType().getValue().equalsIgnoreCase("Installation")
								&& insl.getInstanceType() != null
								&& !insl.getInstanceType().equalsIgnoreCase("Default")) {
							resp = "PENDING";
						}
					}

					if (sen.getProductCode() != null && sen.getProductCode().equalsIgnoreCase("77-S107")
							&& insl.getInstanceType() != null && !insl.getInstanceType().equalsIgnoreCase("Default")
							&& insl.getStatus() != null && insl.getStatus().getValue().equalsIgnoreCase("Error")) {
						resp = "PENDING";
					}

					if (sen.getProductCode() != null && sen.getProductCode().equalsIgnoreCase("77-S107")
							&& insl.getInstanceType() != null && !insl.getInstanceType().equalsIgnoreCase("Default")
							&& insl.getStatus() != null && insl.getStatus().getValue().equalsIgnoreCase("Problem")) {
						resp = "FAIL";
					}

					logger.info("Product code : " + sen.getProductCode() + " Instance Type : " + insl.getInstanceType()
							+ " Sensor Type :  " + insl.getSensorId() + " Presure:  " + insl.getSensorPressure()
							+ " Tempature : " + insl.getSensorTemperature());

					SensorResponseDTO s = new SensorResponseDTO();
					s.setSensorName(sen.getProductName());
					s.setProductCode(sen.getProductCode());
					s.setSensorUuid(sen.getUuid());

					if (sen.getOldMacAddress() != null) {
						s.setMacAddress(sen.getOldMacAddress());
					} else {
						s.setMacAddress(sen.getMacAddress());
					}

					if (insl.getSensorId() != null) {
						String sensorId = insl.getSensorId();
//						if (insl.getSensorId().startsWith("0x")) {
//							sensorId = sensorId.replace("0x", "");
//						}
						s.setSensorId(sensorId);
						s.setSensorPressure(insl.getSensorPressure());
						s.setSensorTemperature(insl.getSensorTemperature());
					}

					if (insl.getInstanceType() != null && !insl.getInstanceType().equalsIgnoreCase("Default")
							&& sen.getProductCode() != null && sen.getProductCode().equalsIgnoreCase("77-S107")) {
						String colorName = colorMapForLiteSentry.get(insl.getInstanceType());
						if (colorName != null) {
							s.setPosition(colorName);
							s.setSensorPressure(insl.getSensorPressure());
							s.setSensorTemperature(insl.getSensorTemperature());
						} else {
							s.setPosition("--");
							s.setSensorPressure("--");
							s.setSensorTemperature("--");
						}
					} else if (insl.getInstanceType() != null && !insl.getInstanceType().equalsIgnoreCase("Default")) {
						String pos = positionMap.get(insl.getInstanceType());
						if (pos != null) { // && resp.equals("PASS")
							s.setPosition(pos);
						}
						s.setSensorPressure((insl.getSensorPressure() != null && insl.getSensorPressure() != "")
								? insl.getSensorPressure()
								: "--");
						s.setSensorTemperature(
								(insl.getSensorTemperature() != null && insl.getSensorTemperature() != "")
										? insl.getSensorTemperature()
										: "--");
						/*
						 * else if(pos == null && resp.equals("PASS")) { s.setPosition("--");
						 * s.setSensorPressure(insl.getSensorPressure());
						 * s.setSensorTemperature(insl.getSensorTemperature()); } else {
						 * s.setPosition("--"); s.setSensorPressure("--"); s.setSensorTemperature("--");
						 * }
						 */
					} else {
						s.setPosition("--");
						s.setSensorPressure("--");
						s.setSensorTemperature("--");
					}

					if (sen.getProductCode() != null && sen.getProductCode().equalsIgnoreCase("77-S107")) {
						if (resp == "PASS") {
							s.setStatus("VERIFIED");
						} else if (resp == "FAIL") {
							s.setStatus("NOT VERIFIED");
						} else if (resp == "PENDING") {
							s.setStatus("PENDING");
						}
					} else {
						s.setStatus(resp);
					}

					if (insl.getType() != null) {
						s.setType(insl.getType());
					} else {
						s.setType("--");
					}

					sensorResponse.add(s);
				}
			} else {
				SensorResponseDTO s = new SensorResponseDTO();
				s.setSensorName(sen.getProductName());
				s.setProductCode(sen.getProductCode());
				s.setSensorUuid(sen.getUuid());
				if (sen.getOldMacAddress() != null) {
					s.setMacAddress(sen.getOldMacAddress());
				} else {
					s.setMacAddress(sen.getMacAddress());
				}
				s.setType("--");
				s.setPosition("--");
				s.setSensorPressure("--");
				s.setSensorTemperature("--");
				s.setStatus("PENDING");
				sensorResponse.add(s);

			}

		}

		// Last Maintenance Rport Date

		Logutils.log(className, methodName, logUUid, " Before calling restUtils.getAssetsDetails method ", logger);
		AssetsStatusPayload details = restUtils.getAssetsDetails(installdto.getDeviceId());
		Logutils.log(className, methodName, logUUid, " After calling restUtils.getAssetsDetails method ", logger);
		installdto.setLastMaintenanceReportDate(details.getLastMaintenanceReportDate());
		if (ins.getInstallCode() != null && ins.getInstallCode().trim().length() > 0) {
			Logutils.log(className, methodName, logUUid,
					" Before calling workOrderRepository.findWorkOrderByInstallCode method ", logger);
			List<WorkOrder> workOrderList = workOrderRepository.findWorkOrderByInstallCode(ins.getInstallCode());
			Logutils.log(className, methodName, logUUid,
					" After calling workOrderRepository.findWorkOrderByInstallCode method ", logger);
			List<FinishWorkOrderDTO> finishWorkOrderList = new ArrayList<>();

			for (WorkOrder workOrder : workOrderList) {
				FinishWorkOrderDTO finishWorkOrder = new FinishWorkOrderDTO();
				finishWorkOrder.setId(workOrder.getId());
				finishWorkOrder.setUuid(workOrder.getUuid());
				finishWorkOrder.setWorkOrder(workOrder.getWorkOrder());
				finishWorkOrder.setLocationUuid(workOrder.getLocationUuid());
				finishWorkOrder.setInstallCode(workOrder.getInstallCode());
				finishWorkOrder.setMaintenanceUuid(workOrder.getMaintenanceUuid());
				finishWorkOrder.setStatus(workOrder.getStatus());
				finishWorkOrder.setResolutionType(workOrder.getResolutionType());
				if (workOrder.getValidationTime() != null) {
					finishWorkOrder.setValidationTime(workOrder.getValidationTime() + "PST");
				}
				if (workOrder.getUser() != null) {
					finishWorkOrder.setTechName(workOrder.getUser().getUserName());
				}

				if (workOrder.getOrganisation() != null) {
					finishWorkOrder.setTechName(workOrder.getOrganisation().getOrganisationName());
				}

				try {
					Date dateStarted = Date.from(workOrder.getServiceDateTime());
					String formattedDate = formatter.format(dateStarted);
					finishWorkOrder.setServiceDateTime(formattedDate + " PST");
				} catch (Exception ex) {

				}

				try {
					Date dateStarted = Date.from(workOrder.getStartDate());
					String formattedDate = formatter.format(dateStarted);
					finishWorkOrder.setStartDate(formattedDate + " PST");
				} catch (Exception ex) {

				}
				try {
					Date dateEnd = Date.from(workOrder.getEndDate());
					String formattedDate = formatter.format(dateEnd);
					finishWorkOrder.setEndDate(formattedDate + " PST");
				} catch (Exception ex) {

				}
				finishWorkOrderList.add(finishWorkOrder);
			}

			installdto.setFinishWorkOrder(finishWorkOrderList);
		}
		SensorResponseDTO sensor = null;
		List<SensorResponseDTO> sensorCopyForTPMS = new ArrayList<>();

		int count = 0;
		int count_for_tpms = 0;

		Boolean isPassStatus = false;
		Boolean isPendingStatus = false;
		Boolean isFailStatus = false;

		int count_for_air_tank = 0;
		Boolean isPassStatusForAirTank = false;
		Boolean isPendingStatusForAirTank = false;

		int count_for_atis_regulator = 0;
		Boolean isPassStatusForAtisRegulator = false;
		Boolean isPendingStatusForAtisRegulator = false;

		// this code is used for lite sentry sensor
		int count_for_lite_sentry = 0;
		Boolean isPassStatusForLiteSentry = false;
		Boolean isPendingStatusForLiteSentry = false;
		List<SensorResponseDTO> sensorCopyForLiteSentry = new ArrayList<>();

		if (sensorResponse != null && sensorResponse.size() > 0) {

			for (SensorResponseDTO sen : sensorResponse) {
				logger.info("---------------sen.getProductCode()---------------" + sen.getProductCode());
			}

			for (SensorResponseDTO sen : sensorResponse) {
				if (sen.getProductCode() != null && sen.getProductCode().equalsIgnoreCase("77-S108")) {
					if (sen.getType() != null) {
						sensor = sen;
					}
					count++;
				} else if (sen.getProductCode() != null && (sen.getProductCode().equalsIgnoreCase("77-S198")
						|| sen.getProductCode().equalsIgnoreCase("77-S199")
						|| sen.getProductCode().equalsIgnoreCase("77-S177")
						|| sen.getProductCode().equalsIgnoreCase("77-S109"))) {
					count_for_tpms++;

					if (!isPassStatus && sen.getStatus().equalsIgnoreCase("PASS")) {
						isPassStatus = true;
					} else if (!isPendingStatus && sen.getStatus().equalsIgnoreCase("PENDING")) {
						isPendingStatus = true;
					} else if (!isFailStatus && sen.getStatus().equalsIgnoreCase("FAIL")) {
						isFailStatus = true;
					}

					sensorCopyForTPMS.add(sen);

				} else if (sen.getProductCode() != null && (sen.getProductCode().equalsIgnoreCase("77-S137")
						|| sen.getProductCode().equalsIgnoreCase("77-S202"))) {
					count_for_air_tank++;

					if (!isPassStatusForAirTank && sen.getStatus().equalsIgnoreCase("PASS")) {
						isPassStatusForAirTank = true;
					} else if (!isPendingStatusForAirTank && sen.getStatus().equalsIgnoreCase("PENDING")) {
						isPendingStatusForAirTank = true;
					}

				} else if (sen.getProductCode() != null && sen.getProductCode().equalsIgnoreCase("77-S203")) {
					count_for_atis_regulator++;

					if (!isPassStatusForAtisRegulator && sen.getStatus().equalsIgnoreCase("PASS")) {
						isPassStatusForAtisRegulator = true;
					} else if (!isPendingStatusForAtisRegulator && sen.getStatus().equalsIgnoreCase("PENDING")) {
						isPendingStatusForAtisRegulator = true;
					}

				} else if (sen.getProductCode() != null && sen.getProductCode().equalsIgnoreCase("77-S107")) {
					count_for_lite_sentry++;

					if (!isPassStatusForLiteSentry && sen.getStatus().equalsIgnoreCase("VERIFIED")) {
						isPassStatusForLiteSentry = true;
					} else if (!isPendingStatusForLiteSentry && sen.getStatus().equalsIgnoreCase("PENDING")) {
						isPendingStatusForLiteSentry = true;
					}

					sensorCopyForLiteSentry.add(sen);
				}
			}
			if (count > 1) {
				sensorResponse.removeIf(filter -> filter.getProductCode() != null
						&& filter.getProductCode().equalsIgnoreCase("77-S108"));
				if (sensor != null) {
					sensorResponse.add(sensor);
				}
			}

			logger.info("Count of TPMS : " + count_for_tpms);
			if (count_for_tpms > 8) {
				if (isPassStatus && isPendingStatus) {
					sensorResponse.removeIf(filter -> filter.getProductCode() != null
							&& (filter.getProductCode().equalsIgnoreCase("77-S198")
									|| filter.getProductCode().equalsIgnoreCase("77-S199")
									|| filter.getProductCode().equalsIgnoreCase("77-S177")
									|| filter.getProductCode().equalsIgnoreCase("77-S109"))
							&& filter.getStatus().equalsIgnoreCase("PENDING"));
				}
			} else if (count_for_tpms < 8) {
				sensorResponse.removeIf(filter -> filter.getProductCode() != null
						&& (filter.getProductCode().equalsIgnoreCase("77-S198")
								|| filter.getProductCode().equalsIgnoreCase("77-S199")
								|| filter.getProductCode().equalsIgnoreCase("77-S177")
								|| filter.getProductCode().equalsIgnoreCase("77-S109"))
						&& filter.getStatus().equalsIgnoreCase("PENDING"));
				for (int i = 0; i < 8; i++) {
					SensorResponseDTO sensorResponseDTO = createNewDummyRecordForTPMSSensor(sensorCopyForTPMS, i);
					if (sensorResponseDTO != null) {
						sensorResponse.add(sensorResponseDTO);
					}

				}

			}

			if (count_for_air_tank > 1) {
				if (isPassStatusForAirTank && isPendingStatusForAirTank) {
					sensorResponse.removeIf(filter -> filter.getProductCode() != null
							&& (filter.getProductCode().equalsIgnoreCase("77-S137")
									|| filter.getProductCode().equalsIgnoreCase("77-S202"))
							&& filter.getStatus().equalsIgnoreCase("PENDING"));
				}
			}

			if (count_for_atis_regulator > 1) {
				if (isPassStatusForAtisRegulator && isPendingStatusForAtisRegulator) {
					sensorResponse.removeIf(filter -> filter.getProductCode() != null
							&& filter.getProductCode().equalsIgnoreCase("77-S203")
							&& filter.getStatus().equalsIgnoreCase("PENDING"));
				}
			}

			if (count_for_lite_sentry > 5) {
				if (isPassStatusForLiteSentry && isPendingStatusForLiteSentry) {
					sensorResponse.removeIf(filter -> filter.getProductCode() != null
							&& ((filter.getProductCode().equalsIgnoreCase("77-S107")
									&& filter.getStatus().equalsIgnoreCase("PENDING"))
									|| (filter.getProductCode().equalsIgnoreCase("77-S107")
											&& ((filter.getStatus().equalsIgnoreCase("VERIFIED")
													|| filter.getStatus().equalsIgnoreCase("PENDING"))
													&& filter.getPosition().equalsIgnoreCase("--")))));
				} else {
					sensorResponse.removeIf(filter -> filter.getProductCode() != null
							&& ((filter.getProductCode().equalsIgnoreCase("77-S107")
									&& filter.getStatus().equalsIgnoreCase("PENDING"))
									|| (filter.getProductCode().equalsIgnoreCase("77-S107")
											&& ((filter.getStatus().equalsIgnoreCase("VERIFIED")
													|| filter.getStatus().equalsIgnoreCase("PENDING"))
													&& filter.getPosition().equalsIgnoreCase("--")))));
				}
			} else if (count_for_lite_sentry < 5) {
				sensorResponse.removeIf(filter -> filter.getProductCode() != null
						&& ((filter.getProductCode().equalsIgnoreCase("77-S107")
								&& filter.getStatus().equalsIgnoreCase("PENDING"))
								|| (filter.getProductCode().equalsIgnoreCase("77-S107")
										&& ((filter.getStatus().equalsIgnoreCase("VERIFIED")
												|| filter.getStatus().equalsIgnoreCase("PENDING"))
												&& filter.getPosition().equalsIgnoreCase("--")))));
				for (int i = 0; i < 5; i++) {
					SensorResponseDTO sensorResponseDTO = createNewDummyRecordForLiteSentry(sensorCopyForLiteSentry, i);
					if (sensorResponseDTO != null)
						sensorResponse.add(sensorResponseDTO);
				}

			}

//	if(sensorResponse != null && sensorResponse.size() > 0 && count_for_lite_sentry > 5) {
//		Boolean isRedAvl = false;
//		Boolean isGreenAvl = false;
//		Boolean isYellowAvl = false;
//		Boolean isBrownAvl = false;
//		Boolean isBlackAvl = false;
//		for(SensorResponseDTO sensorResp : sensorResponse) {
//			if(sensorResp.getProductCode() != null && sensorResp.getProductCode().equalsIgnoreCase("77-S107")) {
//			
//			if(sensorResp.getPosition() != null && !sensorResp.getPosition().equalsIgnoreCase("--")) {
//				if(sensorResp.getPosition().equalsIgnoreCase("RED")) {
//					isRedAvl = true;
//				} else if(sensorResp.getPosition().equalsIgnoreCase("GREEN")) {
//					isGreenAvl = true;
//				} else if(sensorResp.getPosition().equalsIgnoreCase("YELLOW")) {
//					isYellowAvl = true;
//				} else if(sensorResp.getPosition().equalsIgnoreCase("BROWN")) {
//					isBrownAvl = true;
//				} else if(sensorResp.getPosition().equalsIgnoreCase("BLACK")) {
//					isBlackAvl = true;
//				}
//			}
//			}
//		}
//		
//		if(!isRedAvl) {
//			sensorResponse.add(createNewDummyRecordForLiteSentryWithPendingStatus(sensorCopyForLiteSentry, 0));
//		}
//		
//		if(!isGreenAvl) {
//			sensorResponse.add(createNewDummyRecordForLiteSentryWithPendingStatus(sensorCopyForLiteSentry, 1));
//		}
//
//		if(!isYellowAvl) {
//			sensorResponse.add(createNewDummyRecordForLiteSentryWithPendingStatus(sensorCopyForLiteSentry, 2));
//		}
//
//		if(!isBrownAvl) {
//			sensorResponse.add(createNewDummyRecordForLiteSentryWithPendingStatus(sensorCopyForLiteSentry, 3));
//		}
//
//		if(!isBlackAvl) {
//			sensorResponse.add(createNewDummyRecordForLiteSentryWithPendingStatus(sensorCopyForLiteSentry, 4));
//		}
//	}

		}

		installdto.setSensorDetails(sensorResponse);
		Logutils.log(className, methodName, logUUid, " Before calling ilogIssueRepository.findByInstallCode method ",
				logger);
		List<LogIssue> logissue = ilogIssueRepository.findByInstallCode(ins.getInstallCode());
		Logutils.log(className, methodName, logUUid, " After calling ilogIssueRepository.findByInstallCode method ",
				logger);
		List<LoggedIssueResponseDTO> logResposne = new ArrayList<>();
		for (LogIssue issue : logissue) {
			if (issue.getType() == null || issue.getType().equalsIgnoreCase("SENSOR")) {
				LoggedIssueResponseDTO li = new LoggedIssueResponseDTO();
				li.setComment(issue.getComment());
				li.setDate(issue.getCreatedOn().toString());
				if (issue.getReasonCode() != null) {
					li.setReasonCode(issue.getReasonCode().getValue());
				}
				li.setStatus(issue.getStatus().getValue());
				li.setSensor(issue.getDevice().getProductName());
				if (issue.getCreatedBy() != null) {
					if (issue.getUpdatedBy() != null) {
						li.setUser(issue.getUpdatedBy().getFirstName() + " " + issue.getUpdatedBy().getLastName());
					} else {
						li.setUser(issue.getCreatedBy().getFirstName() + " " + issue.getCreatedBy().getLastName());
					}
				}
				logResposne.add(li);
			} else if (issue.getType() != null && issue.getType().equalsIgnoreCase("GATEWAY")) {
				LoggedIssueResponseDTO li = new LoggedIssueResponseDTO();
				li.setComment(issue.getComment());
				li.setDate(issue.getCreatedOn() != null ? issue.getCreatedOn().toString() : null);
				if (issue.getReasonCode() != null) {
					li.setReasonCode(issue.getReasonCode().getValue());
				}

				if (issue.getStatus() != null) {
					li.setStatus(issue.getStatus().getValue());
				}

				if (issue.getDevice() != null) {
					li.setSensor(issue.getDevice().getProductName());
				} else if (issue.getGateway() != null) {
					li.setSensor(issue.getGateway().getProductName());
				}

				if (issue.getCreatedBy() != null) {
					if (issue.getUpdatedBy() != null) {
						li.setUser(issue.getUpdatedBy().getFirstName() + " " + issue.getUpdatedBy().getLastName());
					} else {
						li.setUser(issue.getCreatedBy().getFirstName() + " " + issue.getCreatedBy().getLastName());
					}
				}
				logResposne.add(li);
			}
		}
		installdto.setLoggedIssues(logResposne);
		Logutils.log(className, methodName, logUUid, " Before calling restUtils.getAttributeValueDeviceId method ",
				logger);
		List<AttributeResponseDTO> atrvalue = restUtils.getAttributeValueDeviceId(null, ins.getDevice().getImei());
		Logutils.log(className, methodName, logUUid, " After calling restUtils.getAttributeValueDeviceId method ",
				logger);
		List<BatteryResponseDTO> batteryresposne = new ArrayList<>();
		for (AttributeResponseDTO att : atrvalue) {
			BatteryResponseDTO battery = new BatteryResponseDTO();
			battery.setPowerSource(att.getAttributeName());
			battery.setStatus(att.getStatus());
			battery.setVoltage(att.getAttributeValue());
			batteryresposne.add(battery);
		}
		installdto.setBatteryData(batteryresposne);
		Logutils.log(className, methodName, logUUid, " Exiting From convertInstallationDetail ", logger);
		return installdto;
	}

	private SensorResponseDTO createNewDummyRecordForLiteSentry(List<SensorResponseDTO> sensorCopyForLiteSentry,
			int i) {
		String methodName = "createNewDummyRecordForLiteSentry";
		Context context = new Context();
		String logUUid = context.getLogUUId();
		Logutils.log(className, methodName, logUUid,
				" Inside createNewDummyRecordForLiteSentry Method From BeanConverter " + "List<SensorResponseDTO> "
						+ sensorCopyForLiteSentry,
				logger);
		SensorResponseDTO sensorResponseDTO = new SensorResponseDTO();
		if (sensorCopyForLiteSentry != null && sensorCopyForLiteSentry.size() > 0) {
			sensorResponseDTO.setSensorName(sensorCopyForLiteSentry.get(0).getSensorName());
			sensorResponseDTO.setProductCode(sensorCopyForLiteSentry.get(0).getProductCode());
			sensorResponseDTO.setStatus("PENDING");
		}
		sensorResponseDTO.setSensorUuid("--");
		sensorResponseDTO.setType("--");
		if (i == 0) {
			sensorResponseDTO.setPosition("RED");
		} else if (i == 1) {
			sensorResponseDTO.setPosition("GREEN");
		} else if (i == 2) {
			sensorResponseDTO.setPosition("YELLOW");
		} else if (i == 3) {
			sensorResponseDTO.setPosition("BROWN");
		} else if (i == 4) {
			sensorResponseDTO.setPosition("BLACK");
		}
		sensorResponseDTO.setSensorPressure("--");
		sensorResponseDTO.setSensorTemperature("--");
		List<SensorResponseDTO> list = sensorCopyForLiteSentry.stream()
				.filter(e -> e.getPosition().equalsIgnoreCase(sensorResponseDTO.getPosition()))
				.collect(Collectors.toList());

		if (list != null && list.size() > 0) {
			return null;
		}
//	if(sensorResponseDTO.getPosition().equalsIgnoreCase(sensorCopyForLiteSentry.getPosition()))
//	return null;
		Logutils.log(className, methodName, logUUid, " Exiting From createNewDummyRecordForLiteSentry ", logger);
		return sensorResponseDTO;

	}

	private SensorResponseDTO createNewDummyRecordForLiteSentryWithPendingStatus(
			SensorResponseDTO sensorCopyForLiteSentry, int i) {
		String methodName = "createNewDummyRecordForLiteSentryWithPendingStatus";
		Context context = new Context();
		String logUUid = context.getLogUUId();
		Logutils.log(className, methodName, logUUid,
				" Inside createNewDummyRecordForLiteSentryWithPendingStatus Method From BeanConverter "
						+ " SensorResponseDTO " + sensorCopyForLiteSentry,
				logger);
		SensorResponseDTO sensorResponseDTO = new SensorResponseDTO();
		sensorResponseDTO.setSensorName(sensorCopyForLiteSentry.getSensorName());
		sensorResponseDTO.setProductCode(sensorCopyForLiteSentry.getProductCode());
		sensorResponseDTO.setSensorUuid("--");
		sensorResponseDTO.setType("--");
		if (i == 0) {
			sensorResponseDTO.setPosition("RED");
		} else if (i == 1) {
			sensorResponseDTO.setPosition("GREEN");
		} else if (i == 2) {
			sensorResponseDTO.setPosition("YELLOW");
		} else if (i == 3) {
			sensorResponseDTO.setPosition("BROWN");
		} else if (i == 4) {
			sensorResponseDTO.setPosition("BLACK");
		}
		sensorResponseDTO.setSensorPressure("--");
		sensorResponseDTO.setSensorTemperature("--");
		sensorResponseDTO.setStatus("PENDING");
		Logutils.log(className, methodName, logUUid,
				" Exiting From createNewDummyRecordForLiteSentryWithPendingStatus ", logger);
		return sensorResponseDTO;

	}

	private SensorResponseDTO createNewDummyRecordForTPMSSensor(List<SensorResponseDTO> sensorCopyForTPMS, int i) {
		String methodName = "createNewDummyRecordForTPMSSensor";
		Context context = new Context();
		String logUUid = context.getLogUUId();
		Logutils.log(className, methodName, logUUid,
				" Inside createNewDummyRecordForTPMSSensor Method From BeanConverter " + "List<SensorResponseDTO> "
						+ sensorCopyForTPMS,
				logger);
		SensorResponseDTO sensorResponseDTO = new SensorResponseDTO();
		if (sensorCopyForTPMS != null && sensorCopyForTPMS.size() > 0) {
			sensorResponseDTO.setSensorName(sensorCopyForTPMS.get(0).getSensorName());
			sensorResponseDTO.setProductCode(sensorCopyForTPMS.get(0).getProductCode());
		}

		sensorResponseDTO.setSensorUuid("--");
		sensorResponseDTO.setType("--");
		if (i == 0) {
			sensorResponseDTO.setPosition("LOF-1");
		} else if (i == 1) {
			sensorResponseDTO.setPosition("LIF-2");
		} else if (i == 2) {
			sensorResponseDTO.setPosition("RIF-3");
		} else if (i == 3) {
			sensorResponseDTO.setPosition("ROF-4");
		} else if (i == 4) {
			sensorResponseDTO.setPosition("LOR-5");
		} else if (i == 5) {
			sensorResponseDTO.setPosition("LIR-6");
		} else if (i == 6) {
			sensorResponseDTO.setPosition("RIR-7");
		} else if (i == 7) {
			sensorResponseDTO.setPosition("ROR-8");
		}
		sensorResponseDTO.setSensorPressure("--");
		sensorResponseDTO.setSensorTemperature("--");
		sensorResponseDTO.setStatus("PENDING");
		List<SensorResponseDTO> list = sensorCopyForTPMS.stream()
				.filter(e -> e.getPosition().equalsIgnoreCase(sensorResponseDTO.getPosition()))
				.collect(Collectors.toList());
//	if(sensorResponseDTO.getPosition().equalsIgnoreCase(sensorCopyForTPMS.getPosition()))
//	return null;
		if (list != null && list.size() > 0) {
			return null;
		}
		Logutils.log(className, methodName, logUUid, " Exiting From createNewDummyRecordForTPMSSensor ", logger);
		return sensorResponseDTO;

	}

	public List<LogIssueBean> convertLogIssueToLogIssueBean(List<LogIssue> logIssueList) {
		String methodName = "convertLogIssueToLogIssueBean";
		Context context = new Context();
		String logUUid = context.getLogUUId();
		Logutils.log(className, methodName, logUUid,
				" Inside convertLogIssueToLogIssueBean Method From BeanConverter " + " List<LogIssue> " + logIssueList,
				logger);
		List<LogIssueBean> logIssueBeanList = new ArrayList<>();
		for (LogIssue logIssue : logIssueList) {
			if (logIssue.getType() == null || logIssue.getType().equalsIgnoreCase("SENSOR")) {
				LogIssueBean logIssueBean = new LogIssueBean();
				logIssueBean.setInstallCode(logIssue.getInstallHistory().getInstallCode());
				logIssueBean.setComment(logIssue.getComment());
				logIssueBean.setCreatedOn(logIssue.getCreatedOn().toString());
				logIssueBean.setIssueType(logIssue.getIssueType());
				if (logIssue.getReasonCode() != null) {
					logIssueBean.setReasonCode(logIssue.getReasonCode().getCode());
					logIssueBean.setReasonCodeDisplayName(logIssue.getReasonCode().getValue());
				}
				logIssueBean.setSensorUuid(logIssue.getDevice().getUuid());
				logIssueBean.setSensorProductCode(logIssue.getDevice().getProductCode());
				String sensorDisplayName = restUtils
						.getLookupValueFromDeviceService(logIssue.getDevice().getProductCode());
				if (sensorDisplayName != null && !sensorDisplayName.isEmpty()) {
					logIssueBean.setSensorProductName(sensorDisplayName);
				} else {
					logIssueBean.setSensorProductName(logIssue.getDevice().getProductName());
				}
				logIssueBean.setData(logIssue.getData());
				logIssueBean.setStatus(logIssue.getStatus().getValue());
				logIssueBean.setIssueUuid(logIssue.getUuid());
				logIssueBeanList.add(logIssueBean);
			}
		}
		Logutils.log(className, methodName, logUUid, " Exiting From convertLogIssueToLogIssueBean ", logger);
		return logIssueBeanList;
	}

	public InstallationSummaryResponseDTO convertInstallHistoryToInstallResponseDTO(InstallHistory ins,
			Map<String, String> filterValues, Long userId) throws JsonProcessingException {
		String methodName = "convertInstallHistoryToInstallResponseDTO";
		Context context = new Context();
		String logUUid = context.getLogUUId();
		Logutils.log(className, methodName, logUUid,
				" Inside convertInstallHistoryToInstallResponseDTO Method From BeanConverter " + " InstallHistory "
						+ ins.getInstallCode(),
				logger);
		InstallationSummaryResponseDTO installResponseDto = new InstallationSummaryResponseDTO();
		if (ins.getAsset() != null) {
			installResponseDto.setAssetId(ins.getAsset().getAssignedName());
			installResponseDto.setAssetUuid(ins.getAsset().getUuid());
		}
		if (ins.getDevice() != null) {
			installResponseDto.setProductName(ins.getDevice().getProductName());
			installResponseDto.setProductCode(ins.getDevice().getProductCode());
			installResponseDto.setDeviceUuid(ins.getDevice().getUuid());
		}
		installResponseDto.setInstallCode(ins.getInstallCode());

		installResponseDto.setCreatedAt(ins.getCreatedOn());
		installResponseDto.setUpdatedAt(ins.getUpdatedOn());

		if (ins.getOrganisation() != null && ins.getOrganisation().getOrganisationName() != null) {
			String orgName = ins.getOrganisation().getOrganisationName();
			installResponseDto.setOrganisationName(orgName);
			Long id = ins.getOrganisation().getId();
			installResponseDto.setOrgId(id);
		}
		if (ins.getDateStarted() != null) {
			installResponseDto.setDateStarted(ins.getDateStarted());
		}
		if (ins.getStatus() != null && ins.getStatus().equals(InstallHistoryStatus.STARTED)) {
			if (ins.getUpdatedBy() != null) {
				installResponseDto.setUserName(ins.getUpdatedBy().getUserName());
				installResponseDto.setInstallerName(
						ins.getUpdatedBy().getLastName() + ", " + ins.getUpdatedBy().getFirstName() + ",");
				installResponseDto.setUserId(ins.getUpdatedBy().getId());
				if (ins.getUpdatedBy().getOrganisation() != null)
					installResponseDto.setInstallerCompany(ins.getUpdatedBy().getOrganisation().getOrganisationName());
			}
		} else {
			if (ins.getUpdatedBy() != null) {
				installResponseDto.setUserName(ins.getUpdatedBy().getUserName());
				installResponseDto.setInstallerName(
						ins.getUpdatedBy().getLastName() + ", " + ins.getUpdatedBy().getFirstName() + ",");
				installResponseDto.setUserId(ins.getUpdatedBy().getId());
				if (ins.getUpdatedBy().getOrganisation() != null)
					installResponseDto.setInstallerCompany(ins.getUpdatedBy().getOrganisation().getOrganisationName());
			}
		}

		if (ins.getDevice() != null
				&& ins.getDevice().getIotType().getIOTTypeValue().equalsIgnoreCase(IOTType.GATEWAY.getIOTTypeValue())) {
			installResponseDto.setDeviceId(ins.getDevice().getImei());
		} else if (ins.getDevice() != null
				&& ins.getDevice().getIotType().getIOTTypeValue().equalsIgnoreCase(IOTType.BEACON.getIOTTypeValue())) {
			installResponseDto.setDeviceId(ins.getDevice().getMacAddress());
		}

		if (ins != null && ins.getAppVersion() != null) {
			installResponseDto.setAppVersion(ins.getAppVersion());
		} else {
			installResponseDto.setAppVersion("N/A");
		}
		if (ins.getStatus() != null) {
			installResponseDto.setStatus(ins.getStatus().getValue());
			if (ins.getStatus().equals(InstallHistoryStatus.FINISHED)
					|| ins.getStatus().equals(InstallHistoryStatus.PROBLEM)) {
				installResponseDto.setInstalled(ins.getDateEnded().toString());
			}
		}

		Logutils.log(className, methodName, context.getLogUUId(),
				" Before calling restUtils.getAttributeValueDeviceId method ", logger);
		List<AttributeResponseDTO> atrvalue = restUtils.getAttributeValueDeviceId("", ins.getDevice().getImei());
		Logutils.log(className, methodName, context.getLogUUId(),
				" After calling restUtils.getAttributeValueDeviceId method ", logger);
		if (atrvalue.size() > 0) {
			for (AttributeResponseDTO att : atrvalue) {
				if (att != null) {
					if (att.getAttributeName().equalsIgnoreCase("Battery")) {
						installResponseDto.setBatteryVoltage(att.getAttributeValue());
						installResponseDto.setBatteryStatus(att.getStatus());
					}
					if (att.getAttributeName().equalsIgnoreCase("Primary")) {
						installResponseDto.setPrimaryVoltage(att.getAttributeValue());
						installResponseDto.setPrimaryStatus(att.getStatus());
					}
					if (att.getAttributeName().equalsIgnoreCase("Secondary")) {
						installResponseDto.setSecondaryVoltage(att.getAttributeValue());
						installResponseDto.setSecondaryStatus(att.getStatus());
					}
				}
			}
		}
		String deviceId = String.valueOf(ins.getDevice().getId());
		Logutils.log(className, methodName, context.getLogUUId(),
				" Before calling deviceDeviceXrefRepository.findByGateway method ", logger);
		List<Device_Device_xref> deviceSensorxreffList = deviceDeviceXrefRepository
				.findByGateway(ins.getDevice().getUuid());
		Logutils.log(className, methodName, context.getLogUUId(),
				" After calling deviceDeviceXrefRepository.findByGateway method ", logger);
//		deviceSensorxreffList.forEach(sensor -> {
//			Device d = sensor.getSensorUuid();
//			d.setStatus(DeviceStatus.PENDING);
//			deviceRepository.save(d);
//		});
		if (ins.getDevice() != null && deviceSensorxreffList.size() > 0) {

//		List<Sensor> sensors = ins.getGateway().getSensors();
			for (Device_Device_xref sen : deviceSensorxreffList) {
				String resp = "N/A";
				List<InstallLog> inslog = installLogRepository
						.findBySensorUuidOrderByTimeStampDesc(sen.getSensorUuid().getUuid());
				List<LogIssue> issueLogs = ilogIssueRepository
						.findBySensorUuidOrderByCreatedOnDesc(sen.getSensorUuid().getUuid());
				if (inslog.size() > 0) {
					if (inslog.get(0).getEventType().getValue().equalsIgnoreCase("Sensor Installation Complete")
							&& (issueLogs.size() == 0
									|| issueLogs.get(0).getStatus().getValue().equalsIgnoreCase("Resolved"))) {
						resp = "PASS";
					}
					if ((inslog.get(0).getEventType().getValue().equalsIgnoreCase("Installation")
							|| inslog.get(0).getEventType().getValue().equalsIgnoreCase("Verification")
							|| inslog.get(0).getEventType().getValue().equalsIgnoreCase("Issue"))
							&& issueLogs.size() > 0
							&& issueLogs.get(0).getStatus().getValue().equalsIgnoreCase("Open")) {
						resp = "FAIL";
					}
				} else {
					resp = "PENDING";
				}
//		getProductCode()
				if (filterValues.containsKey("cargo_sensor")
						&& (sen.getSensorUuid().getProductCode().equalsIgnoreCase("77-S102")
								|| sen.getSensorUuid().getProductCode().equalsIgnoreCase("77-S180"))) {
					String filter = filterValues.get("cargo_sensor");
					String[] allStatus = filter.split(",");
					for (String s : allStatus) {
						if (s.equalsIgnoreCase(resp)) {
							installResponseDto.setFilter(true);
						}
					}
				}

				/*
				 * if(filterValues.containsKey("micro_sp_receiver") &&
				 * sen.getProductCode().equalsIgnoreCase("77-S206")) { String filter =
				 * filterValues.get("micro_sp_receiver"); String[] allStatus =
				 * filter.split(","); for(String s : allStatus) { if(s.equalsIgnoreCase(resp)) {
				 * installResponseDto.setFilter(true); } } }
				 */

				/*
				 * if(filterValues.containsKey("pct_cargo_sensor") &&
				 * sen.getProductCode().equalsIgnoreCase("77-S180")) { String filter =
				 * filterValues.get("pct_cargo_sensor"); String[] allStatus = filter.split(",");
				 * for(String s : allStatus) { if(s.equalsIgnoreCase(resp)) {
				 * installResponseDto.setFilter(true); } } }
				 */
				if (filterValues.containsKey("cargo_camera_sensor")
						&& sen.getSensorUuid().getProductCode().equalsIgnoreCase("77-S111")) {
					String filter = filterValues.get("cargo_camera_sensor");
					String[] allStatus = filter.split(",");
					for (String s : allStatus) {
						if (s.equalsIgnoreCase(resp)) {
							installResponseDto.setFilter(true);
						}
					}
				}
				if (filterValues.containsKey("abssensor")
						&& sen.getSensorUuid().getProductCode().equalsIgnoreCase("77-H101")) {
					String filter = filterValues.get("abssensor");
					String[] allStatus = filter.split(",");
					for (String s : allStatus) {
						if (s.equalsIgnoreCase(resp)) {
							installResponseDto.setFilter(true);
						}
					}

				}
				if (filterValues.containsKey("door_sensor")
						&& sen.getSensorUuid().getProductCode().equalsIgnoreCase("77-S108")) {
					String filter = filterValues.get("door_sensor");
					String[] allStatus = filter.split(",");
					for (String s : allStatus) {
						if (s.equalsIgnoreCase(resp)) {
							installResponseDto.setFilter(true);
						}
					}

				}
				if (filterValues.containsKey("atis_sensor")
						&& sen.getSensorUuid().getProductCode().equalsIgnoreCase("77-S120")) {
					String filter = filterValues.get("atis_sensor");
					String[] allStatus = filter.split(",");
					for (String s : allStatus) {
						if (s.equalsIgnoreCase(resp)) {
							installResponseDto.setFilter(true);
						}
					}

				}
				if (filterValues.containsKey("light_sentry")
						&& sen.getSensorUuid().getProductCode().equalsIgnoreCase("77-S107")) {
					String filter = filterValues.get("light_sentry");
					String[] allStatus = filter.split(",");
					for (String s : allStatus) {
						if (s.equalsIgnoreCase(resp)) {
							installResponseDto.setFilter(true);
						}
					}

				}
				if (filterValues.containsKey("tpms")
						&& (sen.getSensorUuid().getProductCode().equalsIgnoreCase("77-S500")
								|| sen.getSensorUuid().getProductCode().equalsIgnoreCase("77-S177"))) {
					String filter = filterValues.get("tpms");
					String[] allStatus = filter.split(",");
					for (String s : allStatus) {
						if (s.equalsIgnoreCase(resp)) {
							installResponseDto.setFilter(true);
						}
					}

				}
				if (filterValues.containsKey("wheel_end")
						&& sen.getSensorUuid().getProductCode().equalsIgnoreCase("77-S119")) {
					String filter = filterValues.get("wheel_end");
					String[] allStatus = filter.split(",");
					for (String s : allStatus) {
						if (s.equalsIgnoreCase(resp)) {
							installResponseDto.setFilter(true);
						}
					}

				}
				if (filterValues.containsKey("air_tank")
						&& (sen.getSensorUuid().getProductCode().equalsIgnoreCase("77-S137")
								|| sen.getSensorUuid().getProductCode().equalsIgnoreCase("77-S202"))) {
					String filter = filterValues.get("air_tank");
					String[] allStatus = filter.split(",");
					for (String s : allStatus) {
						if (s.equalsIgnoreCase(resp)) {
							installResponseDto.setFilter(true);
						}
					}

				}
				if (filterValues.containsKey("regulator")
						&& (sen.getSensorUuid().getProductCode().equalsIgnoreCase("77-S164")
								|| sen.getSensorUuid().getProductCode().equalsIgnoreCase("77-S203"))) {
					String filter = filterValues.get("regulator");
					String[] allStatus = filter.split(",");
					for (String s : allStatus) {
						if (s.equalsIgnoreCase(resp)) {
							installResponseDto.setFilter(true);
						}
					}

				}
				if (filterValues.containsKey("reciever")
						&& (sen.getSensorUuid().getProductCode().equalsIgnoreCase("77-S196")
								|| sen.getSensorUuid().getProductCode().equalsIgnoreCase("77-S206"))) {
					String filter = filterValues.get("reciever");
					String[] allStatus = filter.split(",");
					for (String s : allStatus) {
						if (s.equalsIgnoreCase(resp)) {
							installResponseDto.setFilter(true);
						}
					}

				}

				/*
				 * if(filterValues.containsKey("micro_sp_air_tank") &&
				 * sen.getProductCode().equalsIgnoreCase("77-S202")) { String filter =
				 * filterValues.get("micro_sp_air_tank"); String[] allStatus =
				 * filter.split(","); for(String s : allStatus) { if(s.equalsIgnoreCase(resp)) {
				 * installResponseDto.setFilter(true); } }
				 * 
				 * }
				 */

				/*
				 * if(filterValues.containsKey("micro_sp_regulator") &&
				 * sen.getProductCode().equalsIgnoreCase("77-S203")) { String filter =
				 * filterValues.get("micro_sp_regulator"); String[] allStatus =
				 * filter.split(","); for(String s : allStatus) { if(s.equalsIgnoreCase(resp)) {
				 * installResponseDto.setFilter(true); } }
				 * 
				 * }
				 */

				if (sen.getSensorUuid().getProductCode().equalsIgnoreCase("77-H101")) {
					installResponseDto.setABSSensor(resp);
				} else if (sen.getSensorUuid().getProductCode().equalsIgnoreCase("77-S119")) {
					installResponseDto.setWheelEnd(resp);
				} else if (sen.getSensorUuid().getProductCode().equalsIgnoreCase("77-S102")
						|| sen.getSensorUuid().getProductCode().equalsIgnoreCase("77-S180")) {
					installResponseDto.setCargoSensor(resp);
				} else if (sen.getSensorUuid().getProductCode().equalsIgnoreCase("77-S111")) {
					installResponseDto.setCargoCameraSensor(resp);
				} else if (sen.getSensorUuid().getProductCode().equalsIgnoreCase("77-S137")
						|| sen.getSensorUuid().getProductCode().equalsIgnoreCase("77-S202")) {
					installResponseDto.setAirTank(resp);
					if (inslog.size() > 0 && inslog.get(0).getSensorId() != null) {
						String sensorId = inslog.get(0).getSensorId();
//						if (sensorId != null && sensorId.startsWith("0x")) {
//							sensorId = sensorId.replace("0x", "");
//						}
						installResponseDto.setAirTankId(sensorId);
						installResponseDto.setAirTankPressure(inslog.get(0).getSensorPressure());
						installResponseDto.setAirTankTemperature(inslog.get(0).getSensorTemperature());
					}
				} else if (sen.getSensorUuid().getProductCode().equalsIgnoreCase("77-S500")
						|| sen.getSensorUuid().getProductCode().equalsIgnoreCase("77-S177")) {
					if (inslog.size() > 0) {
						for (InstallLog inlog : inslog) {
							if (inlog.getInstanceType() != null
									&& !inlog.getInstanceType().equalsIgnoreCase(InstanceType.DEFAULT.toString())) {
								String respTpms = "PENDING";
								if (inlog.getEventType().getValue().equalsIgnoreCase("Sensor Installation Complete")
										&& (issueLogs.size() == 0 || issueLogs.get(0).getStatus().getValue()
												.equalsIgnoreCase("Resolved"))) {
									respTpms = "PASS";
								}
								if ((inlog.getEventType().getValue().equalsIgnoreCase("Installation")
										|| inlog.getEventType().getValue().equalsIgnoreCase("Verification")
										|| inlog.getEventType().getValue().equalsIgnoreCase("Issue"))
										&& issueLogs.size() > 0
										&& issueLogs.get(0).getStatus().getValue().equalsIgnoreCase("Open")) {
									respTpms = "FAIL";
								}
								if (inlog.getEventType().getValue().equalsIgnoreCase("Installation")
										&& inlog.getInstanceType() != null
										&& !inlog.getInstanceType().equalsIgnoreCase("Default")) {
									respTpms = "PENDING";
								}

								if (inlog.getInstanceType().equalsIgnoreCase("0x22")) {
									if (inlog.getSensorId() != null) {
										String sensorId = inlog.getSensorId();
//										if (inlog.getSensorId().startsWith("0x")) {
//											sensorId = sensorId.replace("0x", "");
//										}
										installResponseDto.setTpmsLif(sensorId);
										installResponseDto.setTpmsLifStatus(respTpms);
										installResponseDto.setTpmsLifPressure(inslog.get(0).getSensorPressure());
										installResponseDto.setTpmsLifTemperature(inslog.get(0).getSensorTemperature());
									}

								}
								if (inlog.getInstanceType().equalsIgnoreCase("0x26")) {
									if (inlog.getSensorId() != null) {
										String sensorId = inlog.getSensorId();
//										if (inlog.getSensorId().startsWith("0x")) {
//											sensorId = sensorId.replace("0x", "");
//										}
										installResponseDto.setTpmsLir(sensorId);
										installResponseDto.setTpmsLirStatus(respTpms);
										installResponseDto.setTpmsLirPressure(inslog.get(0).getSensorPressure());
										installResponseDto.setTpmsLirTemperature(inslog.get(0).getSensorTemperature());
									}
								}
								if (inlog.getInstanceType().equalsIgnoreCase("0x21")) {
									if (inlog.getSensorId() != null) {
										String sensorId = inlog.getSensorId();
//										if (inlog.getSensorId().startsWith("0x")) {
//											sensorId = sensorId.replace("0x", "");
//										}
										installResponseDto.setTpmsLof(sensorId);
										installResponseDto.setTpmsLofStatus(respTpms);
										installResponseDto.setTpmsLofPressure(inslog.get(0).getSensorPressure());
										installResponseDto.setTpmsLofTemperature(inslog.get(0).getSensorTemperature());
									}
								}
								if (inlog.getInstanceType().equalsIgnoreCase("0x25")) {
									if (inlog.getSensorId() != null) {
										String sensorId = inlog.getSensorId();
//										if (inlog.getSensorId().startsWith("0x")) {
//											sensorId = sensorId.replace("0x", "");
//										}
										installResponseDto.setTpmsLor(sensorId);
										installResponseDto.setTpmsLorStatus(respTpms);
										installResponseDto.setTpmsLorPressure(inslog.get(0).getSensorPressure());
										installResponseDto.setTpmsLorTemperature(inslog.get(0).getSensorTemperature());

									}
								}
								if (inlog.getInstanceType().equalsIgnoreCase("0x23")) {
									if (inlog.getSensorId() != null) {
										String sensorId = inlog.getSensorId();
//										if (inlog.getSensorId().startsWith("0x")) {
//											sensorId = sensorId.replace("0x", "");
//										}
										installResponseDto.setTpmsRif(sensorId);
										installResponseDto.setTpmsRifStatus(respTpms);
										installResponseDto.setTpmsRifPressure(inslog.get(0).getSensorPressure());
										installResponseDto.setTpmsRifTemperature(inslog.get(0).getSensorTemperature());
									}
								}
								if (inlog.getInstanceType().equalsIgnoreCase("0x27")) {
									if (inlog.getSensorId() != null) {
										String sensorId = inlog.getSensorId();
//										if (inlog.getSensorId().startsWith("0x")) {
//											sensorId = sensorId.replace("0x", "");
//										}
										installResponseDto.setTpmsRir(sensorId);
										installResponseDto.setTpmsRirStatus(respTpms);
										installResponseDto.setTpmsRirPressure(inslog.get(0).getSensorPressure());
										installResponseDto.setTpmsRirTemperature(inslog.get(0).getSensorTemperature());
									}
								}
								if (inlog.getInstanceType().equalsIgnoreCase("0x24")) {
									if (inlog.getSensorId() != null) {
										String sensorId = inlog.getSensorId();
//										if (inlog.getSensorId().startsWith("0x")) {
//											sensorId = sensorId.replace("0x", "");
//										}
										installResponseDto.setTpmsRof(sensorId);
										installResponseDto.setTpmsRofStatus(respTpms);
										installResponseDto.setTpmsRofPressure(inslog.get(0).getSensorPressure());
										installResponseDto.setTpmsRofTemperature(inslog.get(0).getSensorTemperature());
									}
								}
								if (inlog.getInstanceType().equalsIgnoreCase("0x28")) {
									if (inlog.getSensorId() != null) {
										String sensorId = inlog.getSensorId();
//										if (inlog.getSensorId().startsWith("0x")) {
//											sensorId = sensorId.replace("0x", "");
//										}
										installResponseDto.setTpmsRor(sensorId);
										installResponseDto.setTpmsRorStatus(respTpms);
										installResponseDto.setTpmsRorPressure(inslog.get(0).getSensorPressure());
										installResponseDto.setTpmsRorTemperature(inslog.get(0).getSensorTemperature());
									}
								}
							}
						}
					}
					installResponseDto.setTpms(resp);
				} else if (sen.getSensorUuid().getProductCode().equalsIgnoreCase("77-S164")
						|| sen.getSensorUuid().getProductCode().equalsIgnoreCase("77-S203")) {
					installResponseDto.setRegulator(resp);
					if (inslog.size() > 0 && inslog.get(0).getSensorId() != null) {
						String sensorId = inslog.get(0).getSensorId();
//						if (sensorId != null && sensorId.startsWith("0x")) {
//							sensorId = sensorId.replace("0x", "");
//						}
						installResponseDto.setRegulatorId(sensorId);
						installResponseDto.setRegulatorPressure(inslog.get(0).getSensorPressure());
						installResponseDto.setRegulatorTemperature(inslog.get(0).getSensorTemperature());
					}
				} else if (sen.getSensorUuid().getProductCode().equalsIgnoreCase("77-S196")
						|| sen.getSensorUuid().getProductCode().equalsIgnoreCase("77-S206")) {
					installResponseDto.setReciever(resp);
				} else if (sen.getSensorUuid().getProductCode().equalsIgnoreCase("77-S108")) {
					if (inslog != null && inslog.size() > 0) {
						for (InstallLog inLg : inslog) {
							if (inLg.getType() != null) {
								installResponseDto.setDoorType(inLg.getType());
							}
						}
					}
					if (sen.getSensorUuid().getMacAddress() != null) {
						String doorMacAdd = sen.getSensorUuid().getMacAddress();
						if (doorMacAdd.contains("-")) {
							doorMacAdd = doorMacAdd.replaceAll("-", ":");
						}
						installResponseDto.setDoorMacAddress(doorMacAdd);
					}
					installResponseDto.setDoorSensor(resp);
				} else if (sen.getSensorUuid().getProductCode().equalsIgnoreCase("77-S107")) {
					installResponseDto.setLightSentry(resp);
				} else if (sen.getSensorUuid().getProductCode().equalsIgnoreCase("77-S120")) {
					installResponseDto.setAtisSensor(resp);
				} else if (sen.getSensorUuid().getProductCode().equalsIgnoreCase("77-S188")) {
					installResponseDto.setLampCheckAtis(resp);
					if (sen.getSensorUuid().getMacAddress() != null) {
						String atisMacAdd = sen.getSensorUuid().getMacAddress();
						if (atisMacAdd.contains("-")) {
							atisMacAdd = atisMacAdd.replaceAll("-", ":");
						}
						installResponseDto.setLampCheckAtisMac(atisMacAdd);
					}
				}

			}
		}
		Logutils.log(className, methodName, logUUid, " Exiting From convertInstallHistoryToInstallResponseDTO ",
				logger);
		return installResponseDto;

	}

	public InProgressInstall createInProgressInstallHistory(InstallHistory installHistory) {
		String methodName = "createInProgressInstallHistory";
		Context context = new Context();
		String logUUid = context.getLogUUId();
		Logutils.log(className, methodName, logUUid, " Inside createInProgressInstallHistory Method From BeanConverter "
				+ "InstallHistory " + installHistory.getInstallCode(), logger);
		InProgressInstall finsinedInstallation = null;
		if (installHistory != null) {

			finsinedInstallation = new InProgressInstall();
			if (installHistory.getAsset() != null) {
				finsinedInstallation.setAssetUuid(installHistory.getAsset().getUuid());
				finsinedInstallation.setAssignedName(installHistory.getAsset().getAssignedName());
				finsinedInstallation.setVin(installHistory.getAsset().getVin());
			}
			if (installHistory.getDevice() != null) {
				finsinedInstallation.setGatewayUuid(installHistory.getDevice().getUuid());
				finsinedInstallation.setImei(installHistory.getDevice().getImei());
				finsinedInstallation.setMacAddress(installHistory.getDevice().getMacAddress());
			}

			finsinedInstallation.setInstallCode(installHistory.getInstallCode());
			finsinedInstallation.setStatus(installHistory.getStatus().getValue());

		}
		Logutils.log(className, methodName, logUUid, " Exiting From createInProgressInstallHistory ", logger);
		return finsinedInstallation;
	}

	public List<InstalledHistroyResponse> convertInstalledHistoryModelToDTO(List<InstallHistory> installedHistroyList) {
		String methodName = "convertInstalledHistoryModelToDTO";
		Context context = new Context();
		String logUUid = context.getLogUUId();
		Logutils.log(className, methodName, logUUid,
				" Inside convertInstalledHistoryModelToDTO Method From BeanConverter " + "List<InstallHistory> "
						+ installedHistroyList,
				logger);
		List<InstalledHistroyResponse> installedHistroyResponses = new ArrayList<>();
		if (installedHistroyList != null && installedHistroyList.size() > 0) {
			for (InstallHistory installedHistroy : installedHistroyList) {
				InstalledHistroyResponse response = new InstalledHistroyResponse();
				response.setId(installedHistroy.getId());
				response.setDateEnded(installedHistroy.getDateEnded());
				response.setDateStarted(installedHistroy.getDateStarted());
				response.setInstallCode(installedHistroy.getInstallCode());
				response.setUuid(installedHistroy.getUuid());
				response.setAppVersion(installedHistroy.getAppVersion());
				if (installedHistroy.getStatus() != null) {
					response.setStatus(installedHistroy.getStatus().getValue());
				}
				if (installedHistroy.getAsset() != null) {
					response.setAssinedName(installedHistroy.getAsset().getAssignedName());
				}
				if (installedHistroy.getDevice() != null) {
					response.setImei(installedHistroy.getDevice().getImei());
				}
				if (installedHistroy.getOrganisation() != null) {
					response.setAccountNumber(installedHistroy.getOrganisation().getAccountNumber());
				}
				installedHistroyResponses.add(response);
			}
		}
		Logutils.log(className, methodName, logUUid, " Exiting From convertInstalledHistoryModelToDTO ", logger);
		return installedHistroyResponses;
	}
}
