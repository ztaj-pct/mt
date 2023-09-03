package com.pct.device.version.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pct.device.service.device.DeviceReport;
import com.pct.device.version.constant.WaterfallConverterConstants;
import com.pct.device.version.model.LatestDeviceMaintenanceReport;
import com.pct.device.version.repository.ILatestDeviceMaintenanceReportRepository;
import com.pct.device.version.repository.RedisCampaignRepository;
import com.pct.device.version.repository.RedisDeviceRepository;

/**
 * @author Abhishek on 19/03/21
 */

@Component
public class CampaignDeviceHelper {
	Logger analysisLog = LoggerFactory.getLogger("analytics");
	Logger logger = LoggerFactory.getLogger(CampaignDeviceHelper.class);
//    @Autowired
//    private RedisDeviceRepository redisDeviceRepository;
	@Autowired
	private RestUtils restUtils;
	@Autowired
	private BeanConverter beanConverter;
	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private RedisCampaignRepository redisCampaignRepository;

	@Autowired
	private ILatestDeviceMaintenanceReportRepository iLatestDeviceMaintenanceReportRepository;

	Set<String> noReportsIdsRef = DeviceReportNotAvailableInMS.getNoReportsIds();
	DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");

	private static final List<String> FIELDS = new ArrayList() {
		{
			add("basebandSWVersionCampaign");
			add("appSWVersionCampaign");
			add("extenderVersionCampaign");
			add("customer");
			add("waterfallStr");
			add("serverInfoStr");
			add("isDeviceInstalledForCampaign");
			add("deviceInstalledForCampaignDT");
			add("isDeviceUpgradeEligibleForCampaign");
			add("deviceUpgradeEligibleForCampaignDT");

			add("liteSentryStatusCampaign");
			add("liteSentryHwCampaign");
			add("liteSentryAppCampaign");
			add("liteSentryBootCampaign");

			add("maxbotixStatusCampaign");
			add("maxbotixFirmwareCampaign");
			add("maxbotixhardwareCampaign");

			add("steStatusCampaign");
			add("steMCUCampaign");
			add("steAppCampaign");

			add("riotStatusCampaign");
			add("riotFirmwareCampaign");
			add("riothardwareCampaign");

			add("deviceTypeCampaign");

			add("bleVersionCampaign");
		}
	};

    private static final List<String> UTILVALUES = new ArrayList() {{
        // add("dateReceived");
		add("maintenanceReportDate");
    }};
	private static final String DEVICE_ID_PREFIX = "deviceID:";

	public List<DeviceReport> getDeviceReports(List<String> deviceIds) {
		List<DeviceReport> list = new ArrayList<>();
		List<String> cacheMissIds = new ArrayList<>();
		for (String deviceId : deviceIds) {
			logger.info("getDeviceReports DEVICE_ID_PREFIX:  " + DEVICE_ID_PREFIX + " deviceId: " + deviceId
					+ " FIELDS: " + FIELDS);

			/* Part of invalid bulk exception */ List<String> valuesForDevice = redisCampaignRepository
					.findValuesForDevice(DEVICE_ID_PREFIX + deviceId, FIELDS);
			logger.info("Redis invoked **** **** **** **** deviceId: " + deviceId);
			logger.info("Redis VALUES  ##### ##### ##### #####:: " + valuesForDevice.toString());

			if (valuesForDevice != null && !valuesForDevice.isEmpty()
					&& !(valuesForDevice.get(0) == null && valuesForDevice.get(1) == null
							&& valuesForDevice.get(2) == null && valuesForDevice.get(4) == null)) {
				DeviceReport deviceReport = new DeviceReport();
				deviceReport.setBASEBAND_SW_VERSION(valuesForDevice.get(0));
				deviceReport.setAPP_SW_VERSION(valuesForDevice.get(1));
				deviceReport.setEXTENDER_VERSION(valuesForDevice.get(2));
				deviceReport.setCustomer(valuesForDevice.get(3));
				deviceReport.setDEVICE_ID(deviceId);
				deviceReport.setSERVER_INFO_STR(getServerIP(valuesForDevice.get(5)));
				deviceReport.setIsDeviceInstalledForCampaign(valuesForDevice.get(6));
				deviceReport.setDeviceInstalledForCampaignDT(valuesForDevice.get(7));
				deviceReport.setIsDeviceUpgradeEligibleForCampaign(valuesForDevice.get(8));
				deviceReport.setDeviceUpgradeEligibleForCampaignDT(valuesForDevice.get(9));

				deviceReport.setLiteSentryHw(valuesForDevice.get(11));
				deviceReport.setLiteSentryApp(valuesForDevice.get(12));
				deviceReport.setLiteSentryBoot(valuesForDevice.get(13));
				deviceReport.setMaxbotixFirmware(valuesForDevice.get(15));
				deviceReport.setMaxbotixhardware(valuesForDevice.get(16));
				deviceReport.setSteMCU(valuesForDevice.get(18));
				deviceReport.setSteApp(valuesForDevice.get(19));

				deviceReport.setRiotFirmware(valuesForDevice.get(21));
				deviceReport.setRiothardware(valuesForDevice.get(22));
				deviceReport.setDevice_type(valuesForDevice.get(23));

				deviceReport.setBLE_VERSION(valuesForDevice.get(24));
				try {
					logger.info("Device's firmware versions found in redis {}",
							deviceId + " , BASEBAND_SW_VERSION: " + valuesForDevice.get(0) + " , APP_SW_VERSION: "
									+ valuesForDevice.get(1) + " , EXTENDER_VERSION: " + valuesForDevice.get(2));
				} catch (Exception e) {
					logger.info("Exception while fetching Device's firmware versions {}", deviceId);
				}
				fillWaterfallToDeviceReport(valuesForDevice, deviceReport);
				list.add(deviceReport);
			} else {
				logger.info("Device's firmware versions are not in redis {}", deviceId);
				cacheMissIds.add(deviceId);
			}
		}
		if (cacheMissIds != null && !cacheMissIds.isEmpty()) {
			ArrayList<String> appTypeList = new ArrayList<>(APPLICATION_TYPE.values());
			List<DeviceReport> lastMaintReportFromMS = new ArrayList<>();
			List<DeviceReport> lastMaintReportFromMS1 = new ArrayList<>();
			List<String> primaryData = new ArrayList<>();
			logger.info("noReportsIdsRef size " + noReportsIdsRef.size());
			cacheMissIds.removeAll(noReportsIdsRef);
			logger.info("calling primary table for reports for size " + cacheMissIds.size());
			// for latest reports
			for (String deviceId : cacheMissIds) {
				try {
					logger.info("calling primary table for reports device Id " + deviceId);
					LatestDeviceMaintenanceReport latestDeviceMaintenanceReport = iLatestDeviceMaintenanceReportRepository
							.findLatestDeviceMaintenanceReportByDeviceId(deviceId);
					if (latestDeviceMaintenanceReport != null) {
						DeviceReport deviceReports = new DeviceReport();
						deviceReports.setAPP_SW_VERSION(latestDeviceMaintenanceReport.getSwVersionApplication());
						deviceReports.setBASEBAND_SW_VERSION(latestDeviceMaintenanceReport.getSwVersionBaseband());
						deviceReports.setBLE_VERSION(latestDeviceMaintenanceReport.getBleVersion());
						deviceReports.setDEVICE_ID(latestDeviceMaintenanceReport.getDeviceId());
						deviceReports.setEXTENDER_VERSION(latestDeviceMaintenanceReport.getExtenderVersion());
						deviceReports.setDEVICE_ID(deviceId);
						deviceReports.setLiteSentryHw(latestDeviceMaintenanceReport.getLiteSentryApp());
						deviceReports.setLiteSentryBoot(latestDeviceMaintenanceReport.getLiteSentryBoot());
						deviceReports.setMaxbotixFirmware(latestDeviceMaintenanceReport.getCargoMaxbotixFirmware());
						deviceReports.setMaxbotixhardware(latestDeviceMaintenanceReport.getCargoMaxbotixHardware());
						//deviceReport.setSteMCU(latestDeviceMaintenanceReport.get);
					//	deviceReport.setSteApp(latestDeviceMaintenanceReport.ge`);
						deviceReports.setRiotFirmware(latestDeviceMaintenanceReport.getCargoRiotFirmware());
						deviceReports.setRiothardware(latestDeviceMaintenanceReport.getCargoRiotHardware());
						deviceReports.setBLE_VERSION(latestDeviceMaintenanceReport.getBleVersion());
						deviceReports.setUpdated_date(latestDeviceMaintenanceReport.getTimestampReceivedPST().toInstant());
						
						logger.info("data found in primary table for device id  " + deviceId);
						try {
							String appVers = "";
							String appVerFromDB = latestDeviceMaintenanceReport.getSwVersionApplication();
							if (appVerFromDB != null) {
								for (String appType : appTypeList) {

									appVers = appVerFromDB.contains(appType) ? appVerFromDB.replace(appType, "")
											: appVerFromDB;
									break;
								}
							}
							Map<String, String> map = new HashMap<>();
							map.put("basebandSWVersionCampaign", latestDeviceMaintenanceReport.getSwVersionBaseband());
							map.put("appSWVersionCampaign", appVers);
							map.put("extenderVersionCampaign", latestDeviceMaintenanceReport.getExtenderVersion());
							map.put("bleVersionCampaign", latestDeviceMaintenanceReport.getBleVersion());

							redisCampaignRepository.add(DEVICE_ID_PREFIX + latestDeviceMaintenanceReport.getDeviceId(),
									map);
							logger.info("Device's firmware versions are insterted in redis via primary table call: {}",
									latestDeviceMaintenanceReport.getDeviceId());
						} catch (Exception e) {
							logger.error("Exception occured while setting redis data from primary data source "
									+ e.getMessage());
						}
						lastMaintReportFromMS.add(deviceReports);
						primaryData.add(deviceId);
					}
				} catch (Exception e) {
					logger.error("Exception occured while adding data from primary data source " + e.getMessage());
				}

			}
			cacheMissIds.removeAll(primaryData);

			logger.info("calling MS for report table for size " + cacheMissIds.size() + " ids :  " + cacheMissIds);
//			if(cacheMissIds.size()>0)
//            lastMaintReportFromMS1 = restUtils.getLastMaintReportFromMS(cacheMissIds);

			// lastMaintReportFromMS.addAll(lastMaintReportFromMS1);
			List<String> allImei = lastMaintReportFromMS.stream().map(DeviceReport::getDEVICE_ID)
					.collect(Collectors.toList());
			cacheMissIds.removeAll(allImei);
			noReportsIdsRef.addAll(cacheMissIds);

//            for(DeviceReport report : lastMaintReportFromMS) {
//            	String appVer="";
//            	for (String appType : appTypeList) {
//            		appVer = report.getAPP_SW_VERSION().contains(appType)?report.getAPP_SW_VERSION().replace(appType, ""): report.getAPP_SW_VERSION();
//            		break;
//            	}
//                Map<String, String> map = new HashMap<>();
//                map.put("basebandSWVersionCampaign", report.getBASEBAND_SW_VERSION());
//                //map.put("appSWVersionCampaign", report.getAPP_SW_VERSION());
//                map.put("appSWVersionCampaign", appVer);
//                map.put("extenderVersionCampaign", report.getEXTENDER_VERSION());
//              //CLD-898
//                map.put("bleVersionCampaign", report.getBLE_VERSION());
//                redisDeviceRepository.add(DEVICE_ID_PREFIX + report.getDEVICE_ID(), map);
//                logger.info("Device's firmware versions are insterted in redis {}", report.getDEVICE_ID());
//            }
			list.addAll(lastMaintReportFromMS);
		}
		return list;
	}

	
	public List<DeviceReport> getDeviceReportsForScheduler(List<String> deviceIds) {
		List<DeviceReport> list = new ArrayList<>();
		List<String> cacheMissIds = new ArrayList<>();
		for (String deviceId : deviceIds) {
			analysisLog.info("getDeviceReports DEVICE_ID_PREFIX:  " + DEVICE_ID_PREFIX + " deviceId: " + deviceId
					+ " FIELDS: " + FIELDS);

			/* Part of invalid bulk exception */ List<String> valuesForDevice = redisCampaignRepository
					.findValuesForDeviceScheduler(DEVICE_ID_PREFIX + deviceId, FIELDS);
			analysisLog.info("Redis invoked **** **** **** **** deviceId: " + deviceId);
			analysisLog.info("Redis VALUES  ##### ##### ##### #####:: " + valuesForDevice.toString());

			if (valuesForDevice != null && !valuesForDevice.isEmpty()
					&& !(valuesForDevice.get(0) == null && valuesForDevice.get(1) == null
							&& valuesForDevice.get(2) == null && valuesForDevice.get(4) == null)) {
				DeviceReport deviceReport = new DeviceReport();
				deviceReport.setBASEBAND_SW_VERSION(valuesForDevice.get(0));
				deviceReport.setAPP_SW_VERSION(valuesForDevice.get(1));
				deviceReport.setEXTENDER_VERSION(valuesForDevice.get(2));
				deviceReport.setCustomer(valuesForDevice.get(3));
				deviceReport.setDEVICE_ID(deviceId);
				deviceReport.setSERVER_INFO_STR(getServerIP(valuesForDevice.get(5)));
				deviceReport.setIsDeviceInstalledForCampaign(valuesForDevice.get(6));
				deviceReport.setDeviceInstalledForCampaignDT(valuesForDevice.get(7));
				deviceReport.setIsDeviceUpgradeEligibleForCampaign(valuesForDevice.get(8));
				deviceReport.setDeviceUpgradeEligibleForCampaignDT(valuesForDevice.get(9));

				deviceReport.setLiteSentryHw(valuesForDevice.get(11));
				deviceReport.setLiteSentryApp(valuesForDevice.get(12));
				deviceReport.setLiteSentryBoot(valuesForDevice.get(13));
				deviceReport.setMaxbotixFirmware(valuesForDevice.get(15));
				deviceReport.setMaxbotixhardware(valuesForDevice.get(16));
				deviceReport.setSteMCU(valuesForDevice.get(18));
				deviceReport.setSteApp(valuesForDevice.get(19));

				deviceReport.setRiotFirmware(valuesForDevice.get(21));
				deviceReport.setRiothardware(valuesForDevice.get(22));
				deviceReport.setDevice_type(valuesForDevice.get(23));

				deviceReport.setBLE_VERSION(valuesForDevice.get(24));
				try {
					analysisLog.info("Device's firmware versions found in redis {}",
							deviceId + " , BASEBAND_SW_VERSION: " + valuesForDevice.get(0) + " , APP_SW_VERSION: "
									+ valuesForDevice.get(1) + " , EXTENDER_VERSION: " + valuesForDevice.get(2));
				} catch (Exception e) {
					analysisLog.info("Exception while fetching Device's firmware versions {}", deviceId);
				}
				fillWaterfallToDeviceReport(valuesForDevice, deviceReport);
				list.add(deviceReport);
			} else {
				analysisLog.info("Device's firmware versions are not in redis {}", deviceId);
				cacheMissIds.add(deviceId);
			}
		}
		if (cacheMissIds != null && !cacheMissIds.isEmpty()) {
			ArrayList<String> appTypeList = new ArrayList<>(APPLICATION_TYPE.values());
			List<DeviceReport> lastMaintReportFromMS = new ArrayList<>();
			List<DeviceReport> lastMaintReportFromMS1 = new ArrayList<>();
			List<String> primaryData = new ArrayList<>();
			analysisLog.info("noReportsIdsRef size " + noReportsIdsRef.size());
			cacheMissIds.removeAll(noReportsIdsRef);
			analysisLog.info("calling primary table for reports for size " + cacheMissIds.size());
			// for latest reports
			for (String deviceId : cacheMissIds) {
				try {
					analysisLog.info("calling primary table for reports device Id " + deviceId);
					LatestDeviceMaintenanceReport latestDeviceMaintenanceReport = iLatestDeviceMaintenanceReportRepository
							.findLatestDeviceMaintenanceReportByDeviceId(deviceId);
					if (latestDeviceMaintenanceReport != null) {
						DeviceReport deviceReports = new DeviceReport();
						deviceReports.setAPP_SW_VERSION(latestDeviceMaintenanceReport.getSwVersionApplication());
						deviceReports.setBASEBAND_SW_VERSION(latestDeviceMaintenanceReport.getSwVersionBaseband());
						deviceReports.setBLE_VERSION(latestDeviceMaintenanceReport.getBleVersion());
						deviceReports.setDEVICE_ID(latestDeviceMaintenanceReport.getDeviceId());
						deviceReports.setEXTENDER_VERSION(latestDeviceMaintenanceReport.getExtenderVersion());
						deviceReports.setDEVICE_ID(deviceId);
						deviceReports.setLiteSentryHw(latestDeviceMaintenanceReport.getLiteSentryApp());
						deviceReports.setLiteSentryBoot(latestDeviceMaintenanceReport.getLiteSentryBoot());
						deviceReports.setMaxbotixFirmware(latestDeviceMaintenanceReport.getCargoMaxbotixFirmware());
						deviceReports.setMaxbotixhardware(latestDeviceMaintenanceReport.getCargoMaxbotixHardware());
						//deviceReport.setSteMCU(latestDeviceMaintenanceReport.get);
					//	deviceReport.setSteApp(latestDeviceMaintenanceReport.ge`);
						deviceReports.setRiotFirmware(latestDeviceMaintenanceReport.getCargoRiotFirmware());
						deviceReports.setRiothardware(latestDeviceMaintenanceReport.getCargoRiotHardware());
						deviceReports.setBLE_VERSION(latestDeviceMaintenanceReport.getBleVersion());
						deviceReports.setUpdated_date(latestDeviceMaintenanceReport.getTimestampReceivedPST().toInstant());
						
						analysisLog.info("data found in primary table for device id  " + deviceId);
						try {
							String appVers = "";
							String appVerFromDB = latestDeviceMaintenanceReport.getSwVersionApplication();
							if (appVerFromDB != null) {
								for (String appType : appTypeList) {

									appVers = appVerFromDB.contains(appType) ? appVerFromDB.replace(appType, "")
											: appVerFromDB;
									break;
								}
							}
							Map<String, String> map = new HashMap<>();
							map.put("basebandSWVersionCampaign", latestDeviceMaintenanceReport.getSwVersionBaseband());
							map.put("appSWVersionCampaign", appVers);
							map.put("extenderVersionCampaign", latestDeviceMaintenanceReport.getExtenderVersion());
							map.put("bleVersionCampaign", latestDeviceMaintenanceReport.getBleVersion());

							redisCampaignRepository.add(DEVICE_ID_PREFIX + latestDeviceMaintenanceReport.getDeviceId(),
									map);
							analysisLog.info("Device's firmware versions are insterted in redis via primary table call: {}",
									latestDeviceMaintenanceReport.getDeviceId());
						} catch (Exception e) {
							analysisLog.error("Exception occured while setting redis data from primary data source "
									+ e.getMessage());
						}
						lastMaintReportFromMS.add(deviceReports);
						primaryData.add(deviceId);
					}
				} catch (Exception e) {
					analysisLog.error("Exception occured while adding data from primary data source " + e.getMessage());
				}

			}
			cacheMissIds.removeAll(primaryData);

			analysisLog.info("calling MS for report table for size " + cacheMissIds.size() + " ids :  " + cacheMissIds);
//			if(cacheMissIds.size()>0)
//            lastMaintReportFromMS1 = restUtils.getLastMaintReportFromMS(cacheMissIds);

			// lastMaintReportFromMS.addAll(lastMaintReportFromMS1);
			List<String> allImei = lastMaintReportFromMS.stream().map(DeviceReport::getDEVICE_ID)
					.collect(Collectors.toList());
			cacheMissIds.removeAll(allImei);
			noReportsIdsRef.addAll(cacheMissIds);

//            for(DeviceReport report : lastMaintReportFromMS) {
//            	String appVer="";
//            	for (String appType : appTypeList) {
//            		appVer = report.getAPP_SW_VERSION().contains(appType)?report.getAPP_SW_VERSION().replace(appType, ""): report.getAPP_SW_VERSION();
//            		break;
//            	}
//                Map<String, String> map = new HashMap<>();
//                map.put("basebandSWVersionCampaign", report.getBASEBAND_SW_VERSION());
//                //map.put("appSWVersionCampaign", report.getAPP_SW_VERSION());
//                map.put("appSWVersionCampaign", appVer);
//                map.put("extenderVersionCampaign", report.getEXTENDER_VERSION());
//              //CLD-898
//                map.put("bleVersionCampaign", report.getBLE_VERSION());
//                redisDeviceRepository.add(DEVICE_ID_PREFIX + report.getDEVICE_ID(), map);
//                analysisLog.info("Device's firmware versions are insterted in redis {}", report.getDEVICE_ID());
//            }
			list.addAll(lastMaintReportFromMS);
		}
		return list;
	}
	public Map<String,List<DeviceReport>> getDeviceReports1(List<String> deviceIds) {
		List<DeviceReport> list = new ArrayList<>();
		// List<String> cacheMissIds = new ArrayList<>();

		ArrayList<String> appTypeList = new ArrayList<>(APPLICATION_TYPE.values());
		List<DeviceReport> lastMaintReportFromMS = new ArrayList<>();
		List<DeviceReport> lastMaintReportFromMS1 = new ArrayList<>();
		List<String> primaryData = new ArrayList<>();
		logger.info("noReportsIdsRef size " + noReportsIdsRef.size());
		Map<String ,List<DeviceReport>> deviceReportMap =  new HashMap<String,List<DeviceReport>>();

		try {

			List<LatestDeviceMaintenanceReport> latestDeviceMaintenanceReportList = iLatestDeviceMaintenanceReportRepository
					.findLatestDeviceMaintenanceReportByDeviceId(deviceIds);
			if (latestDeviceMaintenanceReportList != null && latestDeviceMaintenanceReportList.size() > 0) {
				for (LatestDeviceMaintenanceReport latestDeviceMaintenanceReport : latestDeviceMaintenanceReportList) {
					List<DeviceReport> deviceReportList = new ArrayList<DeviceReport>();
					DeviceReport deviceReports = new DeviceReport();
					deviceReports.setAPP_SW_VERSION(latestDeviceMaintenanceReport.getSwVersionApplication());
					deviceReports.setBASEBAND_SW_VERSION(latestDeviceMaintenanceReport.getSwVersionBaseband());
					deviceReports.setBLE_VERSION(latestDeviceMaintenanceReport.getBleVersion());
					deviceReports.setDEVICE_ID(latestDeviceMaintenanceReport.getDeviceId());
					deviceReports.setEXTENDER_VERSION(latestDeviceMaintenanceReport.getExtenderVersion());
					
					deviceReports.setLiteSentryApp(latestDeviceMaintenanceReport.getLiteSentryApp());
					deviceReports.setLiteSentryHw(latestDeviceMaintenanceReport.getLiteSentryHardware());
					deviceReports.setLiteSentryBoot(latestDeviceMaintenanceReport.getLiteSentryBoot());
					deviceReports.setSteMCU(latestDeviceMaintenanceReport.getMicrospMcu());
					deviceReports.setSteApp(latestDeviceMaintenanceReport.getMicrospApp());
					deviceReports.setMaxbotixFirmware(latestDeviceMaintenanceReport.getCargoMaxbotixFirmware());
					deviceReports.setMaxbotixhardware(latestDeviceMaintenanceReport.getCargoMaxbotixHardware());
					//deviceReport.setSteMCU(latestDeviceMaintenanceReport.get);
				//	deviceReport.setSteApp(latestDeviceMaintenanceReport.ge`);
					deviceReports.setRiotFirmware(latestDeviceMaintenanceReport.getCargoRiotFirmware());
					deviceReports.setRiothardware(latestDeviceMaintenanceReport.getCargoRiotHardware());
					deviceReports.setBLE_VERSION(latestDeviceMaintenanceReport.getBleVersion());
					deviceReports.setUpdated_date(latestDeviceMaintenanceReport.getTimestampReceivedPST().toInstant());
					deviceReports.setConfig1CIV(latestDeviceMaintenanceReport.getConfig1());
					deviceReports.setConfig2CIV(latestDeviceMaintenanceReport.getConfig2());
					deviceReports.setConfig3CIV(latestDeviceMaintenanceReport.getConfig3());
					deviceReports.setConfig4CIV(latestDeviceMaintenanceReport.getConfig4());
					deviceReports.setConfig5CIV(latestDeviceMaintenanceReport.getConfig5());
					
					deviceReports.setConfig1CRC(latestDeviceMaintenanceReport.getConfig1Crc());
					deviceReports.setConfig2CRC(latestDeviceMaintenanceReport.getConfig2Crc());
					deviceReports.setConfig3CRC(latestDeviceMaintenanceReport.getConfig3Crc());
					deviceReports.setConfig4CRC(latestDeviceMaintenanceReport.getConfig4Crc());
					deviceReports.setConfig5CRC(latestDeviceMaintenanceReport.getConfig5Crc());
					
					
					
					
					
					logger.info("data found in primary table for device id  " + latestDeviceMaintenanceReport.getDeviceId());
					try {
						String appVers = "";
						String appVerFromDB = latestDeviceMaintenanceReport.getSwVersionApplication();
						if (appVerFromDB != null) {
							for (String appType : appTypeList) {

								appVers = appVerFromDB.contains(appType) ? appVerFromDB.replace(appType, "")
										: appVerFromDB;
								break;
							}
						}
					} catch (Exception e) {
						logger.error("Exception occured while setting redis data from primary data source "
								+ e.getMessage());
					}
					deviceReportList.add(deviceReports);
					deviceReportMap.put(latestDeviceMaintenanceReport.getDeviceId(),deviceReportList);
					
				}
			}
		} catch (Exception e) {
			logger.error("Exception occured while adding data from primary data source " + e.getMessage());
		}

//         

		return deviceReportMap;
	}

	public final Map<Integer, String> APPLICATION_TYPE = new HashMap<Integer, String>() {
		{
			put(0x00, "arrow-g");
			put(0x01, "katana-g");
			put(0x02, "dagger-g");
			put(0x03, "cutlass-g");

			put(0x10, "arrow-c");
			put(0x11, "katana-c");
			put(0x12, "dagger-c");
			put(0x13, "cutlass-c");

			put(0x20, "arrow-h");
			put(0x21, "katana-h");
			put(0x22, "dagger-h");
			put(0x23, "cutlass-h");

			put(0x30, "arrow-l");
			put(0x31, "katana-l");
			put(0x32, "dagger-l");
			put(0x33, "cutlass-l");

			put(0x40, "arrow-ma");
			put(0x41, "katana-ma");
			put(0x42, "dagger-ma");
			put(0x43, "cutlass-ma");
			put(0x44, "dagger67-ma");

			put(0x50, "arrow-mg");
			put(0x51, "katana-mg");
			put(0x52, "dagger-mg");
			put(0x53, "cutlass-mg");

			put(0x60, "arrow-lg");
			put(0x61, "katana-lg");
			put(0x62, "dagger-lg");
			put(0x63, "cutlass-lg");

			put(0x64, "dagger67-lg");
			put(0x65, "arrow-lgc");

			put(0x70, "arrow-la");
			put(0x71, "katana-la");
			put(0x72, "dagger-la");
			put(0x73, "cutlass-la");

			put(0x74, "dagger67-la");
			put(0x75, "arrow-lac");

			put(0x78, "cutlass-la");
			put(0x84, "dagger67-qa");
			put(0x85, "arrow-qa");
			put(0x95, "arrow-qg");

		}
	};

	public Map<String, String> getDeviceLastReportDate(List<String> deviceIds) {
		Map<String, String> map = new HashMap<>();
		for (String deviceId : deviceIds) {
			List<String> valuesForDevice = redisCampaignRepository.findValuesForDevice(DEVICE_ID_PREFIX + deviceId,
					UTILVALUES);

			if (valuesForDevice != null && !valuesForDevice.isEmpty() && valuesForDevice.get(0) != null) {
				map.put(deviceId, df.format(Date.parse(valuesForDevice.get(0))));
			} else {
				logger.info("Device's util values are not in redis {}", deviceId);
			}
		}
		return map;
	}

	private String getServerIP(String serverInfoStr) {
		if (serverInfoStr == null || serverInfoStr.isEmpty()) {
			return "N/A";
		}
		// Sample record in redis serverInfoStr : Device Port: 63495 Server IP:
		// 127.0.0.1 Server Port: 15020
		String serverIP = StringUtils.substringBetween(serverInfoStr, "Server IP:", "Server Port:");
		if (serverIP == null || serverIP.isEmpty()) {
			return "N/A";
		}
		return serverIP;
	}

	private void fillWaterfallToDeviceReport(List<String> valuesForDevice, DeviceReport deviceReport) {

		if (valuesForDevice.get(4) == null) {
			return;
		}

		String[] split = valuesForDevice.get(4).split("\n");
		for (String string : split) {

			String[] configKeyValue = string.split(":");
			if (configKeyValue == null || configKeyValue.length < 2) {
				continue;
			}
			String configValue = null;
			if (configKeyValue[1] != null) {
				configValue = configKeyValue[1].replaceAll(";", "");
			}

			switch (configKeyValue[0]) {
			case WaterfallConverterConstants.CIV_1:
				deviceReport.setConfig1CIV(configValue);
				break;

			case WaterfallConverterConstants.CIV_2:
				deviceReport.setConfig2CIV(configValue);
				break;

			case WaterfallConverterConstants.CIV_3:
				deviceReport.setConfig3CIV(configValue);
				break;

			case WaterfallConverterConstants.CIV_4:
				deviceReport.setConfig4CIV(configValue);
				break;

			case WaterfallConverterConstants.CIV_5:
				deviceReport.setConfig5CIV(configValue);
				break;

			case WaterfallConverterConstants.CRC_1:
				deviceReport.setConfig1CRC(configValue);
				break;

			case WaterfallConverterConstants.CRC_2:
				deviceReport.setConfig2CRC(configValue);
				break;

			case WaterfallConverterConstants.CRC_3:
				deviceReport.setConfig3CRC(configValue);
				break;

			case WaterfallConverterConstants.CRC_4:
				deviceReport.setConfig4CRC(configValue);
				break;

			case WaterfallConverterConstants.CRC_5:
				deviceReport.setConfig5CRC(configValue);
				break;

			default:
				break;
			}

		}

	}

	public void updateDeviceReports(DeviceReport deviceReport, Map<String, String> map) {

		if (null != deviceReport && null != deviceReport.getDEVICE_ID() && null != map && !map.isEmpty()) {
			redisCampaignRepository.add(DEVICE_ID_PREFIX + deviceReport.getDEVICE_ID(), map);
		}

	}

}
