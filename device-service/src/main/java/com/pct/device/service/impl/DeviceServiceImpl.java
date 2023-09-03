package com.pct.device.service.impl;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import javax.validation.Valid;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.converters.DateConverter;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.index.reindex.UpdateByQueryRequest;
import org.elasticsearch.script.Script;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.wnameless.json.flattener.JsonFlattener;
import com.google.gson.Gson;
import com.pct.common.constant.AssetCategory;
import com.pct.common.constant.AssetStatus;
import com.pct.common.constant.DeviceStatus;
import com.pct.common.constant.IOTType;
import com.pct.common.dto.AssetDTO;
import com.pct.common.dto.AttributeValueResposneDTO;
//import com.pct.common.constant.OrganisationType;
import com.pct.common.dto.CustomerForwardingRuleDTO;
import com.pct.common.dto.CustomerForwardingRuleUrlDTO;
import com.pct.common.dto.DeviceResponsePayloadForAssetUpdate;
import com.pct.common.model.Asset;
//import com.pct.common.model.AssetGatewayXref;
import com.pct.common.model.AssetSensorXref;
import com.pct.common.model.Asset_Device_xref;
import com.pct.common.model.Attribute;
import com.pct.common.model.Cellular;
import com.pct.common.model.Device;
import com.pct.common.model.DeviceCompany;
import com.pct.common.model.DeviceCustomerHistory;
import com.pct.common.model.DeviceDetails;
import com.pct.common.model.DeviceIgnoreForwardingRule;
import com.pct.common.model.Device_Device_xref;
import com.pct.common.model.LatestDeviceReportCount;
import com.pct.common.model.Manufacturer;
import com.pct.common.model.Organisation;
import com.pct.common.model.ReportCount;
import com.pct.common.model.SensorInstallInstruction;
import com.pct.common.model.SensorReasonCode;
import com.pct.common.model.User;
import com.pct.common.payload.AssetSensorXrefPayload;
import com.pct.common.payload.DeviceSensorxrefPayload;
import com.pct.common.payload.GatewayDetailsBean;
import com.pct.common.payload.InstallInstructionBean;
import com.pct.common.payload.InstallationStatusGatewayRequest;
import com.pct.common.payload.ReasonCodeBean;
import com.pct.common.payload.SensorUpdateRequest;
import com.pct.common.payload.UpdateAssetToDeviceForInstallationRequest;
import com.pct.common.payload.UpdateGatewayAssetStatusRequest;
import com.pct.common.util.Context;
import com.pct.common.util.JwtUser;
import com.pct.device.dto.DeviceReportDTO;
import com.pct.device.exception.DeviceException;
import com.pct.device.model.ColumnDefs;
import com.pct.device.model.DeviceReportCount;
import com.pct.device.model.DeviceStatusTransient;
import com.pct.device.model.Event;
import com.pct.device.model.Lookup;
import com.pct.device.ms.repository.IAssetSensorXrefRepository;
import com.pct.device.ms.repository.IGatewaySensorXrefRepository;
import com.pct.device.payload.AddAssetResponse;
import com.pct.device.payload.AssetDevicePayload;
import com.pct.device.payload.AssetsPayload;
import com.pct.device.payload.BatchDeviceEditPayload;
import com.pct.device.payload.CellularDetailPayload;
import com.pct.device.payload.CompanyPayload;
import com.pct.device.payload.DeviceCustomerUpdatePayload;
import com.pct.device.payload.DeviceDetailPayLoad;
import com.pct.device.payload.DeviceDetailsRequest;
import com.pct.device.payload.DeviceForwardingResponse;
import com.pct.device.payload.DeviceReportPayload;
import com.pct.device.payload.DeviceResponsePayload;
import com.pct.device.payload.DeviceWithSensorPayload;
import com.pct.device.payload.InstalledHistoryResponsePayload;
import com.pct.device.payload.UpdateDeviceStatusPayload;
import com.pct.device.repository.DeviceIgnoreForwardingRuleRepository;
import com.pct.device.repository.EventRepository;
import com.pct.device.repository.IAssetDeviceXrefRepository;
import com.pct.device.repository.IAssetRepository;
import com.pct.device.repository.ICellularRepository;
import com.pct.device.repository.IColumnDefsRepository;
import com.pct.device.repository.IDeviceCompanyRepository;
import com.pct.device.repository.IDeviceCustomerHistoryRepository;
import com.pct.device.repository.IDeviceDeviceXrefRepository;
import com.pct.device.repository.IDeviceReportCountRepository;
import com.pct.device.repository.IDeviceRepository;
import com.pct.device.repository.IDeviceStatusTransientRepository;
import com.pct.device.repository.ILatestDeviceReportRepository;
import com.pct.device.repository.ILookupRepository;
import com.pct.device.repository.IManufacturerRepository;
import com.pct.device.repository.IReportCount;
import com.pct.device.repository.RedisDeviceRepository;
import com.pct.device.repository.msdevice.IAssetToDeviceHistoryRepository;
import com.pct.device.repository.msdevice.IAssetToDeviceRepository;
import com.pct.device.service.IDeviceService;
import com.pct.device.service.IProductMasterService;
import com.pct.device.service.device.AssetToDevice;
import com.pct.device.service.device.AssetToDeviceHistory;
import com.pct.device.specification.DeviceSpecification;
import com.pct.device.util.AppUtility;
import com.pct.device.util.AuthoritiesConstants;
import com.pct.device.util.BeanConverter;
import com.pct.device.util.Constants;
import com.pct.device.util.Logutils;
import com.pct.device.util.ReportRow;
import com.pct.device.util.RestUtils;
import com.pct.es.dto.Filter;

import lombok.Data;


@Service
public class DeviceServiceImpl implements IDeviceService {
	Logger logger = LoggerFactory.getLogger(DeviceServiceImpl.class);
	public static final String className = "DeviceServiceImpl";
	public static final String DEVICE_CURRENT_VIEW_PREFIX = "deviceData:";
	public static final DateFormat simpleDateTimeFormatPST = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	private static final List<String> DEVICE_INFO_FIXED_FIELDS = new ArrayList() {
		{
			add("customerId");
			add("deviceType");
		}
	};

	private static final List<String> DEVICE_INFO_FIELDS = new ArrayList() {
		{
			add("deviceId");
			add("deviceIP");
			add("devicePort");
			add("deviceMaintIP");
			add("deviceMaintPort");
			add("latestMaintReportUUID");
			add("latestMaintReportTimeStamp");
			add("latestReportUUID");
			add("latestReportTimeStamp");
			add("latestReportEventId");
			add("customerId");
			add("deviceType");

		}
	};

	@Autowired
	RedisDeviceRepository redisDeviceRepository;
	
	@Autowired
	IAssetSensorXrefRepository assetSensorXrefRepository;

	@Autowired
	private IDeviceRepository deviceRepository;

	@Autowired
	private ILatestDeviceReportRepository latestDeviceReportRepository;

	@Autowired
	private IDeviceReportCountRepository latestReportCountRepository;

	@Autowired
	private IReportCount reportCount;

	@Autowired
	private BeanConverter beanConverter;
	
	@Autowired
	private IAssetToDeviceHistoryRepository assetToDeviceHistoryRepository;

	@Autowired
	private RestUtils restUtils;

	@Autowired
	EventRepository eventRepository;

	@Autowired
	private IDeviceCompanyRepository deviceCompanyRepository;

	@Autowired
	private ICellularRepository cellularRepository;

	@Autowired
	private IColumnDefsRepository columnDefsRepo;

	@Autowired
	IDeviceStatusTransientRepository deviceStatusTransientRepo;

	@Autowired
	IAssetRepository assetRepository;

	@Autowired
	IAssetDeviceXrefRepository assetDeviceXrefRepository;

	@Autowired
	private DeviceIgnoreForwardingRuleRepository deviceIgnoreForwardingRuleRepository;

	@Autowired
	private RestHighLevelClient template;
	
	@Autowired
	private IAssetToDeviceRepository assetToDeviceRepository;

	@Autowired
	private IDeviceDeviceXrefRepository deviceDeviceXrefRepository;

	@Autowired
	private IManufacturerRepository manufacturerRepository;

	@Autowired
	AssetServiceImpl assetService;
	
	@Autowired
	private ILookupRepository lookupRepository;
	
//	@Autowired
//	private IAssetGatewayXrefRepository assetGatewayXrefRepository;
	
	@Autowired
	private IGatewaySensorXrefRepository gatewaySensorXrefRepository;

	@Autowired
	IDeviceCustomerHistoryRepository deviceCustomerHistoryRepository;
	
    @Autowired
	IProductMasterService productMasterService;

	@Value("${spring.elasticsearch.rest.uris}")
	String url;

	@Value("${spring.elasticsearch.rest.username}")
	String elasticSearchUsername;

	@Value("${spring.elasticsearch.rest.password}")
	String elasticSearchPassword;

	@Value("${device.Key}")
	String deviceKey;

	@Value("${device.operator}")
	String deviceOperator;

	@Value("${jsonobject.value}")
	String jsonObject;

	@Value("${client.configration.url}")
	String clientConfigUrl;

	@Value("${general_mask_fields.received_time_stamp}")
	String general_mask_fields_received_time_stamp;
	private List<Device_Device_xref> saveAllAndFlush;

	@Value("${file.download-dir}")
	String fileDownloadDir;

	@Value("${file.download-url}")
	String fileDownloadUrl;

	@Value("${jsonobject.value}")
	String index;

	@Value("${general_mask_fields.received_time_stamp}")
	String generalMaskFieldRecTimeStamp;
	
	@Value("${report_header.device_id}")
	String reportHeaderDevId;
	
	@Override
	public Boolean addDeviceDetail(DeviceDetailsRequest deviceUploadRequest, String userName) throws DeviceException {
//		logger.info("Inside addDeviceDetail and fetching deviceDetail and userId value",
//				deviceUploadRequest + " " + userName);
		
		String methodName="addDeviceDetail";
		Context context = new Context();
		Logutils.log(className,methodName,context.getLogUUId(),"Inside addDeviceDetail and fetching deviceDetail and userId value", logger);
		String can = deviceUploadRequest.getCan();
		
		Logutils.log(className, " Before calling restUtils.getCompanyFromCompanyService method ", logger);
		Organisation company = restUtils.getCompanyFromCompanyService(can);
		
		
		Logutils.log(className, " Before calling restUtils.getUserFromAuthService method ", logger);
		User user = restUtils.getUserFromAuthService(userName);
		
		String macAddress = deviceUploadRequest.getMacAddress();
		String imei = deviceUploadRequest.getImei();
		DeviceStatusTransient deviceStatusTransient = new DeviceStatusTransient();
		if (company != null) {
			if (imei != null && macAddress != null) {
				if (imei.length() == Constants.IMEI_LENGTH) {
					Device byImei = deviceRepository.findByImei(imei);
					if (byImei == null) {
						Device device = new Device();
						device = beanConverter.convertDeviceDetailRequestToDeviceBean(deviceUploadRequest);
						device.setOrganisation(company);
//						DeviceDetails deviceDetails = new DeviceDetails();
//						deviceDetails.setUsageStatus(deviceUploadRequest.getUsage_status());
//						deviceDetails.setImei(deviceUploadRequest.getImei());
//						device.setDeviceDetails(deviceDetails);
						device.setUsageStatus(deviceUploadRequest.getUsage_status());
						boolean isDeviceUuidUnique = false;
						String deviceUuid = "";
						while (!isDeviceUuidUnique) {
							deviceUuid = UUID.randomUUID().toString();
							Device byUuid = deviceRepository.findByUuid(deviceUuid);
							if (byUuid == null) {
								isDeviceUuidUnique = true;
							}
						}
						device.setCreatedBy(user);
						device.setCreatedAt(Instant.now());
						device.setUuid(deviceUuid);
						deviceRepository.save(device);
						logger.info("Device Details saved successfully");
						deviceStatusTransient.setDate_created(Instant.now());
						deviceStatusTransient.setDeviceId(imei);
						deviceStatusTransient.setStatus(DeviceStatus.PENDING);
						// deviceStatusTransientRepo.save(deviceStatusTransient);
						logger.info("Device Status saved successfully");

					} else {
						throw new DeviceException("IMEI/MAC Address number already exist");
					}
				} else {
					throw new DeviceException("Invalid IMEI number");
				}
			} else {
				throw new DeviceException("IMEI/MAC Address number can not be null");
			}
		} else {
			throw new DeviceException("No Company found for account number " + deviceUploadRequest.getCan());
		}

		updateCustomerAndTypeInRedis(deviceUploadRequest.getImei(), deviceUploadRequest.getDeviceType(),
				deviceUploadRequest.getCan());
		Logutils.log(className,methodName,context.getLogUUId(),"Exiting From DeviceServiceImpl ", logger);
		return Boolean.TRUE;
	}

	@Override
	public Page<DeviceResponsePayload> getDeviceWithPagination(String messageUuid, String accountNumber, String imei, String uuid,
			DeviceStatus status, IOTType type, String mac, Map<String, Filter> filterValues,
			String filterModelCountFilter, Pageable pageable, String userName, boolean forExport, String token, String timeOfLastDownload, String sort) {
		String methodName="getDeviceWithPagination";
		Context context = new Context();
		
		Logutils.log(className,methodName,context.getLogUUId(),"Inside getDeviceWithPagination ", logger);
     
		Page<Device> deviceDetails = null;
		IOTType dType = IOTType.getValue("Gateway");
		
		Logutils.log(className, " Before calling restUtils.getUserFromAuthServiceWithToken method ", logger);
		User user = restUtils.getUserFromAuthServiceWithToken(userName, token);
		Logutils.log(className, " After calling restUtils.getUserFromAuthServiceWithToken method ", logger);
		
		List<String> ls2 = null;
		String result = filterValues.entrySet().stream().filter(map -> "assetIdList".equals(map.getValue().getKey()))
				.map(map -> map.getValue().getValue()).collect(Collectors.joining());

		if (result != null && result.length() > 0) {
			ls2 = assetDeviceXrefRepository.findAllImeiByAssetId(Arrays.asList(result.split(",")));
		}
		List<CustomerForwardingRuleDTO> customerForwardingRules = new ArrayList<>();
		        //restUtils.getAllCustomerForwardingRules(token);
		List<CustomerForwardingRuleUrlDTO> customerForwardingRuleUrls = new ArrayList<>();
		        //restUtils.getAllCustomerForwardingRuleUrl(token);
		Specification<Device> spc = DeviceSpecification.getDeviceListSpecification(accountNumber, imei, uuid, status,
				dType, mac, filterValues, filterModelCountFilter, user, ls2, sort);
		logger.info("After Specification " + spc);
		logger.info("After Specification  messageUUid :" +messageUuid);
		logger.info("Time before getting data from db:- " + new Date().getTime());
		deviceDetails = deviceRepository.findAll(spc, pageable);
		logger.info("Fetching device details for specification of messageUUid :" +messageUuid);
		logger.info("Fetching device details for specification");
		logger.info("Fetching device details for specification");
//		Set<String> deviceOrgUuids = deviceDetails.getContent().stream()
//				.filter(device -> device.getOrganisation() != null && device.getOrganisation().getUuid() != null)
//				.map(device -> {
//					return device.getOrganisation().getUuid();
//				}).collect(Collectors.toSet());
//		Set<String> imeis = deviceDetails.getContent().stream().map(device -> {
//			return device.getImei();
//		}).collect(Collectors.toSet());
		Set<DeviceIgnoreForwardingRule> deviceIgnoreForwardingRules = new HashSet<>();
		        //deviceIgnoreForwardingRuleRepository
				//.findByDeviceiImeisIn(imeis);
		List<Map<String, String>> customerForwardingRulesMap = new ArrayList<>();
//		for (String deviceOrgUuid : deviceOrgUuids) {
//			customerForwardingRules.stream().forEach(customerForwardingRule -> {
//				if (deviceOrgUuid.equals(customerForwardingRule.getOrganisation().getUuid())) {
//					Optional<CustomerForwardingRuleUrlDTO> customerForwardingRuleUrlDTO = customerForwardingRuleUrls
//							.stream().filter(customerForwardingRuleUrl -> customerForwardingRuleUrl.getUuid()
//									.equals(customerForwardingRule.getForwardingRuleUrl().getUuid()))
//							.findAny();
//					if (customerForwardingRuleUrlDTO.isPresent()) {
//						Map<String, String> map = new HashMap<>();
//						map.put("ruleName", customerForwardingRuleUrlDTO.get().getRuleName());
//						map.put("sourceName", customerForwardingRule.getOrganisation().getOrganisationName());
//						map.put("uuid", customerForwardingRuleUrlDTO.get().getUuid());
//						map.put("ruleUuid", customerForwardingRule.getUuid());
//						map.put("orgUuid", deviceOrgUuid);
//						customerForwardingRulesMap.add(map);
//					}
//				}
//			});
//		}
		Logutils.log(className,methodName,context.getLogUUId(),"exiting form getDeviceWithPagination ", logger);
		return beanConverter.convertDeviceToDevicePayLoad1(messageUuid,deviceDetails, pageable, forExport,
				customerForwardingRuleUrls, customerForwardingRulesMap, deviceIgnoreForwardingRules);
	}

	@Override
	public void exportDeviceDataIntoCSV(String messageUuid, String accountNumber, String imei, String uuid, DeviceStatus status,
			IOTType type, String mac, Map<String, Filter> filterValues, String filterModelCountFilter, int page,
			int size, String sort, String order, String userName, boolean forExport, String token,
			List<String> columnDef, HttpServletResponse response) {
		logger.info("inside exportDeviceDataIntoCSV messageUUid :-" +messageUuid);
		String methodName="exportDeviceDataIntoCSV";
		Context context = new Context();
		Logutils.log(className,methodName,context.getLogUUId(),"Inside exportDeviceDataIntoCSV ", logger);

		List<Page<DeviceResponsePayload>> allData = new ArrayList<>();
		try {
			IOTType dType = IOTType.getValue("Gateway");
			
			Logutils.log(className, " Before calling restUtils.getUserFromAuthServiceWithToken method ", logger);
			User user = restUtils.getUserFromAuthServiceWithToken(userName, token);
			Logutils.log(className, " After calling restUtils.getUserFromAuthServiceWithToken method ", logger);
			
			
			
			List<CustomerForwardingRuleDTO> customerForwardingRules = restUtils.getAllCustomerForwardingRules(token);
			Logutils.log(className, " After calling restUtils.getAllCustomerForwardingRules method ", logger);
			List<CustomerForwardingRuleUrlDTO> customerForwardingRuleUrls = restUtils
					.getAllCustomerForwardingRuleUrl(token);
			List<String> ls2 = null;
			String result = filterValues.entrySet().stream()
					.filter(map -> "assetIdList".equals(map.getValue().getKey())).map(map -> map.getValue().getValue())
					.collect(Collectors.joining());

			if (result != null && result.length() > 0) {
				ls2 = assetDeviceXrefRepository.findAllImeiByAssetId(Arrays.asList(result.split(",")));
			}
			Specification<Device> spc = DeviceSpecification.getDeviceListSpecification(accountNumber, imei, uuid,
					status, dType, mac, filterValues, filterModelCountFilter, user, ls2, null);
			size = (int) deviceRepository.count(spc);
			int last = 0;
			int totalSize = size;
			Thread[] threads = null;
			if (size <= 5000) {
				threads = new Thread[1];
			} else {
				int len = size / 5000;
				threads = new Thread[len + 1];
				last = size % 5000;
			}
			logger.info("Number of thread will create :-" + threads.length);
			for (int i = 0; i < threads.length; i++) {
				if (threads.length > 1) {
					size = 5000;
				}
				if (last > 0 && i == threads.length - 1) {
					size = last;
				}
				final int newSize = size;
				final int start = i;
				final List<String> ls = ls2;
				logger.info("Thread Number" + i + " and data size" + newSize);
				Runnable myThread = () -> {
					try {
						Pageable pageable = getPageable(start, newSize, sort, order);

						Page<Device> deviceDetails = getDeviceWithPaginationForExportingData(accountNumber, imei, uuid,
								status, type, mac, filterValues, filterModelCountFilter, pageable, dType, user, ls);

						Set<String> deviceOrgUuids = deviceDetails.getContent().stream()
								.filter(device -> device.getOrganisation() != null
										&& device.getOrganisation().getUuid() != null)
								.map(device -> {
									return device.getOrganisation().getUuid();
								}).collect(Collectors.toSet());
						Set<String> imeis = deviceDetails.getContent().stream().map(device -> {
							return device.getImei();
						}).collect(Collectors.toSet());
						Set<DeviceIgnoreForwardingRule> deviceIgnoreForwardingRules = deviceIgnoreForwardingRuleRepository
								.findByDeviceiImeisIn(imeis);
						List<Map<String, String>> customerForwardingRulesMap = new ArrayList<>();
						for (String deviceOrgUuid : deviceOrgUuids) {
							customerForwardingRules.stream().forEach(customerForwardingRule -> {
								if (deviceOrgUuid.equals(customerForwardingRule.getOrganisation().getUuid())) {
									Optional<CustomerForwardingRuleUrlDTO> customerForwardingRuleUrlDTO = customerForwardingRuleUrls
											.stream()
											.filter(customerForwardingRuleUrl -> customerForwardingRuleUrl.getUuid()
													.equals(customerForwardingRule.getForwardingRuleUrl().getUuid()))
											.findAny();
									if (customerForwardingRuleUrlDTO.isPresent()) {
										Map<String, String> map = new HashMap<>();
										map.put("ruleName", customerForwardingRuleUrlDTO.get().getRuleName());
										map.put("sourceName",
												customerForwardingRule.getOrganisation().getOrganisationName());
										map.put("uuid", customerForwardingRuleUrlDTO.get().getUuid());
										map.put("ruleUuid", customerForwardingRule.getUuid());
										map.put("orgUuid", deviceOrgUuid);
										customerForwardingRulesMap.add(map);
									}
								}
							});
						}

						logger.info("Get Installed Date for Installed History table:- "+ new Date().getTime());
//						List<InstalledHistoryResponsePayload> installedHistoryResponse =  restUtils.getInstalledHistoryResponseByDeviceImei(imeis);
						logger.info("Time Before going to beanconverter:- "+ new Date().getTime());
//						allData.add(beanConverter.convertDeviceToDevicePayLoad(deviceDetails, pageable, forExport));
						allData.add(beanConverter.convertDeviceToDevicePayLoad1(messageUuid,deviceDetails, pageable, forExport,
								customerForwardingRuleUrls, customerForwardingRulesMap, deviceIgnoreForwardingRules));
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				};
				threads[i] = new Thread(myThread);
			}
			for (int i = 0; i < threads.length; i++) {
				logger.info("Thread " + i + "Started");
				threads[i].start();
				threads[i].join();
			}
			logger.info("Data got from db successfully");
			getDeviceDetailsForCsv(allData, response, userName, token, totalSize, columnDef);
			logger.info("Exiting exportDeviceDataIntoCSV messageUUid :-" +messageUuid);

		} catch (Exception e) {
			logger.info("Exception while exporting device list");
			logger.info("Exception while exporting device list, Messagwe" + e.getMessage());
			// TODO: handle exception
			e.printStackTrace();
		}

	}

	public Page<Device> getDeviceWithPaginationForExportingData(String accountNumber, String imei, String uuid,
			DeviceStatus status, IOTType type, String mac, Map<String, Filter> filterValues,
			String filterModelCountFilter, Pageable pageable, IOTType dType, User user, List<String> ls2) {

		Specification<Device> spc = DeviceSpecification.getDeviceListSpecification(accountNumber, imei, uuid, status,
				dType, mac, filterValues, filterModelCountFilter, user, ls2, null);
		logger.info("After Specification " + spc);
		return deviceRepository.findAll(spc, pageable);
	}

	@Override
	public Page<DeviceWithSensorPayload> getDeviceAndSensorWithPagination(String accountNumber,
			Map<String, String> filterValues, String filterModelCountFilter, Pageable pageable, String userName,String sort) {
		String methodName="getDeviceAndSensorWithPagination";
		Context context = new Context();
		Logutils.log(className,methodName,context.getLogUUId(),"Inside getDeviceAndSensorWithPagination ", logger);
		Page<Device> deviceDetails = null;
		
		Logutils.log(className, " Before calling restUtils.getUserFromAuthService method ", logger);
		User user = restUtils.getUserFromAuthService(userName);
		Logutils.log(className, " After calling restUtils.getUserFromAuthService method ", logger);

		Specification<Device> spc = DeviceSpecification.getDeviceWithSensorListSpecification(accountNumber,
				filterValues, filterModelCountFilter, user,sort);
		logger.info("After Specification " + spc);
		deviceDetails = deviceRepository.findAll(spc, pageable);
		logger.info("Fetching device details for specification");
		Page<DeviceWithSensorPayload> deviceRecordloadPage = null;
		
		Logutils.log(className,methodName,context.getLogUUId(),"before convertDeviceToDeviceSensorPayLoad ", logger);
		deviceRecordloadPage = beanConverter.convertDeviceToDeviceSensorPayLoad(deviceDetails, pageable);
		
		Logutils.log(className,methodName,context.getLogUUId(),"Exiting from getDeviceAndSensorWithPagination ", logger);
		return deviceRecordloadPage;
	}

	@Override
	public boolean deleteDeviceDetail(String can, String imei, String uuid, IOTType type) {
		//logger.info("Inside deleteDeviceDetail service for device" + imei);
		
		String methodName="deleteDeviceDetail";
		Context context = new Context();
		Logutils.log(className,methodName,context.getLogUUId(),"Inside deleteDeviceDetail ", logger);
		
		if (can != null && !can.isEmpty()) {
			Specification<Device> spc = DeviceSpecification.getDeviceSpec(can, imei, uuid, type);
			List<Device> deviceList = deviceRepository.findAll(spc);
			logger.info("Fetching device detail(s)" + deviceList.toString());
			AtomicBoolean status = new AtomicBoolean(true);
			if (deviceList.size() > 0) {
				deviceList.forEach(device -> {
					Cellular cell = cellularRepository.findByUuid(device.getCellular().getUuid());
					if (cell != null) {
						status.set(status.get() && deleteDeviceData(device, cell));
						logger.info("Device deleted successfully");

					} else {
						throw new DeviceException("Cellular information is not found");
					}
				});
			}
			Logutils.log(className,methodName,context.getLogUUId(),"Exiting deleteDeviceDetail ", logger);
			return status.get();

		} else {
			throw new DeviceException("Account number is mandatory");
		}
	}

	private Boolean deleteDeviceData(Device device, Cellular cell) {
		deviceRepository.delete(device);
		cellularRepository.delete(cell);
		return true;
	}

	@Override
	public Device updateDeviceStatus(@Valid UpdateDeviceStatusPayload deviceStatusPayload) throws Exception {
//		logger.info("Inside updateDeviceStatus service for uuid " + deviceStatusPayload.getDeviceUuid());
		
		String methodName="updateDeviceStatus";
		Context context = new Context();
		Logutils.log(className,methodName,context.getLogUUId(),"Inside updateDeviceStatus service for uuid" + deviceStatusPayload.getDeviceUuid(), logger);
		
		Device device = deviceRepository.findByUuid(deviceStatusPayload.getDeviceUuid());
		logger.info("Fetching device details for updation");
		DeviceStatus dStatus = DeviceStatus.getGatewayStatusInSearch("PENDING");
		DeviceStatusTransient deviceStatusTransient = new DeviceStatusTransient();
		if (device != null) {
			if (device.getStatus().equals(dStatus)) {
				device.setStatus(DeviceStatus.INSTALL_IN_PROGRESS);
				Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
				String currentPrincipalName = authentication.getName();
				
				Logutils.log(className, " Before calling restUtils.getUserFromAuthService method ", logger);
				User user = restUtils.getUserFromAuthService(currentPrincipalName);
				Logutils.log(className, " After calling restUtils.getUserFromAuthService method ", logger);
				
				device.setUpdatedBy(user);
				device.setUpdatedAt(Instant.now());
				deviceRepository.save(device);
				deviceStatusTransient.setDate_created(Instant.now());
				deviceStatusTransient.setDeviceId(device.getImei());
				deviceStatusTransient.setStatus(DeviceStatus.INSTALL_IN_PROGRESS);
				deviceStatusTransientRepo.save(deviceStatusTransient);
				logger.info("Device Updated successfully for uuid " + deviceStatusPayload.getDeviceUuid());
			} else {
				throw new Exception(
						"Device status is not in pending state for Uuid = " + deviceStatusPayload.getDeviceUuid());
			}
		} else {
			throw new Exception("Device not found for Uuid = " + deviceStatusPayload.getDeviceUuid());
		}
		Logutils.log(className,methodName,context.getLogUUId(),"Exiting updateDeviceStatus service for uuid",logger);
		return device;
	}

	@Override
	public Boolean updateDeviceDetail(DeviceDetailPayLoad devicedetailPayload, String userName) {
//		logger.info("Inside updateDeviceDetail and fetching deviceDetail and userId value" + devicedetailPayload + " "
//				+ userName);
		
		String methodName="updateDeviceDetail";
		Context context = new Context();
		Logutils.log(className,methodName,context.getLogUUId(),"Inside updateDeviceDetail and fetching deviceDetail and userId value" + devicedetailPayload + " "
				+ userName, logger);
		
		Logutils.log(className, " Before calling restUtils.getUserFromAuthService method ", logger);
		User user = restUtils.getUserFromAuthService(userName);
		
		Device device = deviceRepository.findByUuid(devicedetailPayload.getUuid());
		Logutils.log(className, " After calling deviceRepository.findByUuid method " + device.getUuid(), logger);
		
		Cellular cellularPayload = cellularRepository.findByUuid(devicedetailPayload.getCellularPayload().getUuid());
		Logutils.log(className, " After calling cellularRepository.findByUuid method " + cellularPayload.getUuid(), logger);
		if (cellularPayload == null) {
			cellularPayload = new Cellular();
		}
		logger.info("Fetching device details for uuid : " + devicedetailPayload.getUuid()
				+ " and Cellular details for uuid " + devicedetailPayload.getCellularPayload().getUuid());
		if (device != null) {
			device.setImei(devicedetailPayload.getImei());
			device.setUsageStatus(devicedetailPayload.getUsage_status());
//			DeviceDetails deviceDetails = device.getDeviceDetails();
//			deviceDetails.setUsageStatus(devicedetailPayload.getUsage_status());
//			deviceDetails.setImei(devicedetailPayload.getImei());
//			device.setDeviceDetails(deviceDetails);;
//			device.setAppVersion(devicedetailPayload.getAppVersion());
//			device.setBinVersion(devicedetailPayload.getBinVersion());
//			device.setBleVersion(devicedetailPayload.getBleVersion());
//			device.setConfig1(devicedetailPayload.getConfig1());
//			device.setConfig2(devicedetailPayload.getConfig2());
//			device.setConfig3(devicedetailPayload.getConfig3());
//			device.setConfig4(devicedetailPayload.getConfig4());
//			device.setMcuVersion(devicedetailPayload.getMcuVersion());
//			device.setOther1Version(devicedetailPayload.getOther1Version());
//			device.setOther2Version(devicedetailPayload.getOther2Version());
			device.setProductCode(devicedetailPayload.getProductCode());
			device.setProductName(devicedetailPayload.getProductName());
//			device.setSon(devicedetailPayload.getSon());
//			device.setIotType(devicedetailPayload.getIotType());
//			device.setDeviceType(devicedetailPayload.getDeviceType());
			device.setUpdatedAt(Instant.now());
			device.setUpdatedBy(user);
//			device.setEpicorOrderNumber(devicedetailPayload.getEpicorOrderNumber());
//			device.setMacAddress(devicedetailPayload.getMacAddress());
//			device.setQuantityShipped(devicedetailPayload.getQuantityShipped());
//			cellularPayload.setCarrierId(devicedetailPayload.getCellularPayload().getCarrierId());
//			cellularPayload.setCellular(devicedetailPayload.getCellularPayload().getCellular());
			cellularPayload.setCountryCode(devicedetailPayload.getCellularPayload().getCountryCode());
			cellularPayload.setIccid(devicedetailPayload.getCellularPayload().getIccid());
			cellularPayload.setImsi(devicedetailPayload.getCellularPayload().getImsi());
			cellularPayload.setPhone(devicedetailPayload.getCellularPayload().getPhone());
//			cellularPayload.setServiceCountry(devicedetailPayload.getCellularPayload().getServiceCountry());
			cellularPayload.setServiceNetwork(devicedetailPayload.getCellularPayload().getServiceNetwork());
			cellularPayload.setImei(devicedetailPayload.getCellularPayload().getImei());
			device.setCellular(cellularPayload);
			deviceRepository.save(device);
			logger.info("Device updated for the uuid : " + devicedetailPayload.getUuid());
		} else {
			throw new DeviceException("Device not found for the uuid " + devicedetailPayload.getUuid());
		}
		Logutils.log(className,methodName,context.getLogUUId(),"Exiting updateDeviceDetail and fetching deviceDetail and userId value", logger);
		return Boolean.TRUE;
	}

	@Override
	public List<DeviceResponsePayload> getDeviceDetails(String accountNumber, String uuid, String deviceId) {
//		logger.info("Inside getDeviceDetails method for account number " + accountNumber + " uuid " + uuid
//				+ "device Id " + deviceId);
		
		String methodName="getDeviceDetails";
		Context context = new Context();
		Logutils.log(className,methodName,context.getLogUUId(),"Inside getDeviceDetails method for account number " + accountNumber + " uuid " + uuid
				+ "device Id " + deviceId, logger);
		List<DeviceResponsePayload> deviceDetailList = new ArrayList<>();
		List<Device> deviceList = new ArrayList<Device>();
		Specification<Device> spc = DeviceSpecification.getDeviceListSpec(accountNumber, uuid, deviceId);
		deviceList = deviceRepository.findAll(spc);
		System.out.println("--deviceList------------- "+deviceList);
		logger.info("Fetching device details based on specification");
		if (deviceList.size() > 0) {
			deviceList.forEach(device -> {
				deviceDetailList.add(beanConverter.convertDeviceDetailPayloadToDeviceBean(device));
				
			});
		} else {
			throw new DeviceException("No device found for uuid/can number");
		}
		Logutils.log(className,methodName,context.getLogUUId(),"Exiting getDeviceDetails method for account number " ,logger);
		return deviceDetailList;
	}

	@Override
	public String getParsedReport(String rawReport, String format, String type) {
		logger.info("Inside getParsedReport for raw report " + rawReport + " format " + format + "type " + type);
		String parsedReport = restUtils.getParsedReport(rawReport, format, type);
		return parsedReport;
	}

	@Override
	public void getCSVData(HttpServletResponse response, int from, int size, String order, String sort, String column,
			Map<String, Filter> filterValues, String deviceId, String userName, String token, List<String> columnDef) {
//		logger.info("Inside getParsedReport for raw report ");
		
		String methodName="getCSVData";
		Context context = new Context();
		Logutils.log(className,methodName,context.getLogUUId(),"Inside getParsedReport for raw report",logger);
		try {
			if (deviceId != null && deviceId.length() > 1) {
				Filter filter = new Filter();
				filter.setKey(deviceKey);
				filter.setOperator(deviceOperator);
				filter.setValue(deviceId);
				filterValues.put("filter", filter);
			}
			logger.info("Get All organisation list");
			List<Organisation> organisationList = restUtils.findAllOrganisation(token);
			logger.info("Organisation list got successfully");
			logger.info("Get All event list");
			List<Event> eventList = eventRepository.findAll();
			logger.info("Event list got successfully");
			List<List<DeviceReportPayload>> allData = new ArrayList<>();
			int last = 0;
			int totalSize = size;
			Thread[] threads = null;
			if (size <= 5000) {
				threads = new Thread[1];
			} else {
				int len = size / 5000;
				last = size % 5000;
				if (last > 0) {
					threads = new Thread[len + 1];
				} else {
					threads = new Thread[len];
				}
			}
			logger.info("Number of thread will create :-" + threads.length);
			for (int i = 0; i < threads.length; i++) {
				if (threads.length > 1) {
					size = 5000;
				}
				int startFrom = i;
				if (i != 0) {
					startFrom = size * i;
				}
				if (last > 0 && i == threads.length - 1) {
					size = last;
				}
				final int newSize = size;
				final int start = startFrom;
				logger.info("Thread Number" + i + " and data size" + newSize);
				Runnable myThread = () -> {
					try {
						logger.info("Start number" + start + " and data size" + newSize);
						SearchResponse searchResponse = getDeviceReportFromElastic(start, newSize, filterValues, sort,

								order);
						SearchHit[] hits = searchResponse.getHits().getHits();
						allData.add(restUtils.exportCsv(hits, token, organisationList, eventList));
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				};
				threads[i] = new Thread(myThread);
			}
			for (int i = 0; i < threads.length; i++) {
				logger.info("Thread " + i + "Started");
				threads[i].start();
				threads[i].join();
			}
			User user = null;
			try {
				user = restUtils.getUserFromAuthServiceWithToken(userName, token);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			logger.info("After getting data from elstic going to creating a csv file");
			restUtils.writeFilterCSVFileFromElsatic("deviceData:", allData, response, user, totalSize, columnDef);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new DeviceException("Exception while downloading data:- " + e.getMessage());
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new DeviceException("Exception while downloading data:- " + e.getMessage());
		}
	}

	@Override
	public String[] getCompanyByType(String type) {
		String[] companies = restUtils.findByType(type);
		logger.info("Fetching organisation details for the type CUSTOMER");
		return companies;
	}

	@Override
	public Organisation getCompanyById(Long id) {
		Organisation companies = restUtils.findById(id);
		logger.info("Fetching organisation details for the type CUSTOMER");
		return companies;
	}

	@Override
	public List<Organisation> getListOfCompanyByType(String type, String name) {
//		logger.info("Fetching User details");
		
		String methodName="getListOfCompanyByType";
		Context context = new Context();
		Logutils.log(className,methodName,context.getLogUUId(),"Fetching User details",logger);
		
		JwtUser jwtUser = (JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		logger.info("Username : " + jwtUser.getUsername());
		List<String> roleName = jwtUser.getRoleName();
		boolean roleAvailable = false;
		for (String rName : roleName) {
			if (rName.contains(AuthoritiesConstants.ROLE_CUSTOMER_ADMIN)
					|| rName.contains(AuthoritiesConstants.ROLE_ORGANIZATION_USER)) {
				roleAvailable = true;
				break;
			}
		}
		List<Organisation> companies = new ArrayList<>();
		if (roleAvailable && jwtUser.getAccountNumber() != null) {
			name = jwtUser.getAccountNumber();
			Organisation org = restUtils.getCompanyFromCompanyService(jwtUser.getAccountNumber());
			companies.add(org);
		} else {
			companies = restUtils.findOrganisationByType(type, name);
		}
		logger.info("Fetching organisation details for the type CUSTOMER");
		return companies;
	}

	public SearchResponse getDeviceFromElastic(int from, int size, Map<String, Filter> filterValues, String imei,
			String sorting, String order) throws IOException {
//		logger.info("Inside getDeviceFromElastic service for device " + imei);
		
		String methodName="getDeviceFromElastic";
		Context context = new Context();
		Logutils.log(className,methodName,context.getLogUUId(),"Inside getDeviceFromElastic service for device " + imei,logger);
		
		MatchQueryBuilder matchQueryBuilder = null;
		String operator = null;
		SortOrder orderBy = null;
		Filter f = null;
		if (sorting.equalsIgnoreCase("report_header.device_id")
				|| sorting.equalsIgnoreCase("software_version.app_version")
				|| sorting.equalsIgnoreCase("software_version.os_version")
				|| sorting.equalsIgnoreCase("software_version.extender_version")
				|| sorting.equalsIgnoreCase("software_version.ble_version")
				|| sorting.equalsIgnoreCase("config_version.device_config_changed")
				|| sorting.equalsIgnoreCase("tftp_status.tftp_status")
				|| sorting.equalsIgnoreCase("network_field.tower_id")
				|| sorting.equalsIgnoreCase("network_field.cellular_band")
				|| sorting.equalsIgnoreCase("network_field.rx_tx_ec")
				|| sorting.equalsIgnoreCase("alpha_atis.condition")
				|| sorting.equalsIgnoreCase("skf_wheel_end.comm_status")
				|| sorting.equalsIgnoreCase("general.rawreport")) {
			sorting = sorting + ".keyword";
		}
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
		if (imei != null) {
			Filter filter = new Filter();
			filter.setKey(deviceKey);
			filter.setOperator(deviceOperator);
			filter.setValue(imei);
			filterValues.put("filter", filter);
		}
		for (Map.Entry<String, Filter> fitlers : filterValues.entrySet()) {
			f = fitlers.getValue();
			operator = f.getOperator();
			matchQueryBuilder = QueryBuilders.matchQuery(f.getKey(), f.getValue());
			if (operator.equals(Constants.LIKE))
				boolQueryBuilder.must(QueryBuilders.wildcardQuery(f.getKey(), "*" + f.getValue().toLowerCase() + "*"))
						.boost(2);
			else if (operator.equals(Constants.EQUAL))
				boolQueryBuilder.must(matchQueryBuilder);
			else if (operator.equals(Constants.NOTEQUAL))
				boolQueryBuilder.mustNot(matchQueryBuilder);
			else if (operator.equals(Constants.GT))
				boolQueryBuilder.must(QueryBuilders.rangeQuery(f.getKey()).gt(f.getValue() + ".000000000"));
			else if (operator.equals(Constants.LT))
				boolQueryBuilder.must(QueryBuilders.rangeQuery(f.getKey()).lt(f.getValue() + ".000000000"));
			else if (operator.equals(Constants.GTE))
				boolQueryBuilder.must(QueryBuilders.rangeQuery(f.getKey()).gte(f.getValue() + ".000000000"));
			else if (operator.equals(Constants.LTE))
				boolQueryBuilder.must(QueryBuilders.rangeQuery(f.getKey()).lte(f.getValue() + ".000000000"));
			else if (operator.equals(Constants.DATE)) {
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSSSSS");
				Calendar cal = Calendar.getInstance();
				try {
					cal.setTime(dateFormat.parse(f.getValue() + ".000000000"));
				} catch (java.text.ParseException e) {
					e.printStackTrace();
				}
				cal.add(Calendar.DATE, 1);
				String convertedDate = dateFormat.format(cal.getTime());
				boolQueryBuilder
						.must(QueryBuilders.rangeQuery(f.getKey()).gt(f.getValue() + ".000000000").lt(convertedDate));
//				searchSourceBuilder.sort(new FieldSortBuilder(f.getKey()).order(SortOrder.DESC));
			} else if (operator.equals(Constants.EXIST)) {

				boolQueryBuilder.must(QueryBuilders.existsQuery(f.getValue()));
			} else if (operator.equals(Constants.TERM)) {
				Set<String> listOfValues = new HashSet<String>();
				String[] allValues = f.getValue().split(",");
				for (String values : allValues) {
					listOfValues.add(values);
				}
				boolQueryBuilder.must(QueryBuilders.termsQuery(f.getKey(), listOfValues));
			}
		}
		if (order.contentEquals("asc")) {
			orderBy = SortOrder.ASC;
		} else {
			orderBy = SortOrder.DESC;
		}
		SearchRequest searchRequest = new SearchRequest(jsonObject);
		searchSourceBuilder.from(from);
		searchSourceBuilder.size(size);
		searchSourceBuilder.query(boolQueryBuilder);
		searchSourceBuilder.sort(new FieldSortBuilder(sorting).order(orderBy));
		searchRequest.source(searchSourceBuilder);
		SearchResponse searchResponse = template.search(searchRequest, RequestOptions.DEFAULT);
		logger.info("Fetched report details");
		logger.info("Total hits: " + searchResponse.getHits().getTotalHits());
		return searchResponse;
	}

	@Override
	public List<ColumnDefs> getColumnDefs() {
		List<ColumnDefs> columnDefs = columnDefsRepo.findAll();
		return columnDefs;
	}

	@Override
	public Device getDevice(String imei) {
		Device device = null;
		
		String methodName="getDevice";
		Context context = new Context();
		Logutils.log(className,methodName,context.getLogUUId(),"Inside getDevice",logger);
		try {
			device = deviceRepository.findByImei(imei);
		} catch (Exception e) {
			e.printStackTrace();
		}
		Logutils.log(className,methodName,context.getLogUUId(),"exiting getDevice",logger);
		return device;
	}

	public SearchResponse getDeviceReportByUUid(int from, int size, Map<String, Filter> filterValues, String imei)
			throws IOException {
//		logger.info("Inside getDeviceFromElastic service for device" + imei);
		
		String methodName="getDeviceReportByUUid";
		Context context = new Context();
		Logutils.log(className,methodName,context.getLogUUId(),"Inside getDeviceFromElastic service for device" + imei,logger);
		
		
		MatchQueryBuilder matchQueryBuilder = null;
		String operator = null;
		Filter f = null;
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
		if (imei != null) {
			Filter filter = new Filter();
			filter.setKey(deviceKey);
			filter.setOperator(deviceOperator);
			filter.setValue(imei);
			filterValues.put("filter", filter);
		}
		for (Map.Entry<String, Filter> fitlers : filterValues.entrySet()) {
			f = fitlers.getValue();
			operator = f.getOperator();
			matchQueryBuilder = QueryBuilders.matchQuery(f.getKey(), f.getValue());

			if (operator.equals(Constants.EQUAL))
				boolQueryBuilder.must(QueryBuilders.termsQuery(f.getKey(), f.getValue()));

		}
		SearchRequest searchRequest = new SearchRequest(jsonObject);
		searchSourceBuilder.from(from);
		searchSourceBuilder.size(size);
		searchSourceBuilder.query(boolQueryBuilder);
		searchSourceBuilder.sort(new FieldSortBuilder(general_mask_fields_received_time_stamp).order(SortOrder.DESC));
		searchRequest.source(searchSourceBuilder);
		SearchResponse searchResponse = template.search(searchRequest, RequestOptions.DEFAULT);
		logger.info("Fetched report details");
		logger.info("Total hits: " + searchResponse.getHits().getTotalHits());
		return searchResponse;
	}

	public SearchResponse getDeviceReportFromElastic(int from, int size, Map<String, Filter> filterValues,
			String sorting, String order) throws IOException {

//		logger.info("Inside getDeviceFromElastic service for device ");
		
		String methodName="getDeviceReportFromElastic";
		Context context = new Context();
		Logutils.log(className,methodName,context.getLogUUId()," Inside getDeviceFromElastic service for device ",logger);
		
		MatchQueryBuilder matchQueryBuilder = null;
		String operator = null;
		Filter f = null;
		SortOrder orderBy = null;
//		sorting = sorting + ".keyword";
//		if(!sorting.contains(".keyword")) {
//			sorting = sorting + ".keyword";
//		}
//		if (sorting.equalsIgnoreCase("report_header.device_id")
//				|| sorting.equalsIgnoreCase("software_version.app_version")
//				|| sorting.equalsIgnoreCase("software_version.os_version")
//				|| sorting.equalsIgnoreCase("software_version.extender_version")
//				|| sorting.equalsIgnoreCase("software_version.ble_version")
//				|| sorting.equalsIgnoreCase("config_version.device_config_changed")
//				|| sorting.equalsIgnoreCase("tftp_status.tftp_status")
//				|| sorting.equalsIgnoreCase("network_field.tower_id")
//				|| sorting.equalsIgnoreCase("network_field.cellular_band")
//				|| sorting.equalsIgnoreCase("network_field.rx_tx_ec")
//				|| sorting.equalsIgnoreCase("alpha_atis.condition")
//				|| sorting.equalsIgnoreCase("skf_wheel_end.comm_status")
//				|| sorting.equalsIgnoreCase("general.company_id")
//				|| sorting.equalsIgnoreCase("general.rawreport")) {
//			sorting = sorting + ".keyword";
//		}
		
//		if (sorting.equalsIgnoreCase("report_header.ack_n")
//				|| sorting.equalsIgnoreCase("general_mask_fields.received_time_stamp")
//				|| sorting.equalsIgnoreCase("report_header.rtcdate_time_info_str")
//				|| sorting.equalsIgnoreCase("general_mask_fields.external_power_volts")
//				|| sorting.equalsIgnoreCase("voltage.aux_power")
//				|| sorting.equalsIgnoreCase("general_mask_fields.internal_power_volts")
//				|| sorting.equalsIgnoreCase("voltage.charge_power")
//				|| sorting.equalsIgnoreCase("al_mask_fields.altitude_feet")
//				|| sorting.equalsIgnoreCase("general_mask_fields.speed_miles")
//				|| sorting.equalsIgnoreCase("general_mask_fields.speed_kms")
//				|| sorting.equalsIgnoreCase("general_mask_fields.heading")
//				|| sorting.equalsIgnoreCase("general_mask_fields.odometer_kms")
//				|| sorting.equalsIgnoreCase("field_vehicle_ecu.fuel_level_percentage")
//				|| sorting.equalsIgnoreCase("field_vehicle_ecu.odtsince_milmiles")
//				|| sorting.equalsIgnoreCase("field_vehicle_ecu.odtsince_dtcclear_miles")
//				|| sorting.equalsIgnoreCase("accelerometer_fields.accelerometer_xmm_s2")
//				|| sorting.equalsIgnoreCase("orientation_fields.orientation_zaxis_milli_gs")
//				|| sorting.equalsIgnoreCase("orientation_fields.orientation_yaxis_milli_gs")
//				|| sorting.equalsIgnoreCase("orientation_fields.orientation_xaxis_milli_gs")
//				|| sorting.equalsIgnoreCase("accelerometer_fields.accelerometer_zmm_s2")
//				|| sorting.equalsIgnoreCase("accelerometer_fields.accelerometer_ymm_s2")
//				|| sorting.equalsIgnoreCase("temperature.internal_temperature")
//				|| sorting.equalsIgnoreCase("temperature.ambient_temperature")
//				|| sorting.equalsIgnoreCase("general_mask_fields.rssi")
//				|| sorting.equalsIgnoreCase("general_mask_fields.hdop")
//				|| sorting.equalsIgnoreCase("general_mask_fields.num_satellites")
//				|| sorting.equalsIgnoreCase("network_field.mobile_country_code")
//				|| sorting.equalsIgnoreCase("network_field.mobile_network_code")
//				|| sorting.equalsIgnoreCase("network_field.tower_centroid_latitude")
//				|| sorting.equalsIgnoreCase("network_field.tower_centroid_longitude")
//				|| sorting.equalsIgnoreCase("cargo_camera_sensor.uri")
//				|| sorting.equalsIgnoreCase("reefer.status")
//				|| sorting.equalsIgnoreCase("reefer.state")
//				|| sorting.equalsIgnoreCase("config_version.device_config_changed")
//				|| sorting.equalsIgnoreCase("config_version.configuration_desc")
//				|| sorting.equalsIgnoreCase("general_mask_fields.gps_time")
//				|| sorting.equalsIgnoreCase("abs_odometer.odometer")
//				|| sorting.equalsIgnoreCase("reefer.fan_state")) {
//			sorting = sorting + ".keyword";
//		}

		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

		for (Map.Entry<String, Filter> fitlers : filterValues.entrySet()) {
			f = fitlers.getValue();
			operator = f.getOperator();
			matchQueryBuilder = QueryBuilders.matchQuery(f.getKey(), f.getValue());
			if (operator.equals(Constants.LIKE))
				boolQueryBuilder.must(QueryBuilders.wildcardQuery(f.getKey(), "*" + f.getValue().toLowerCase() + "*"))
						.boost(2);
			else if (operator.equals(Constants.EQUAL) && !f.getKey().equalsIgnoreCase("general.company_id"))
				boolQueryBuilder.must(matchQueryBuilder);
			if (operator.equals(Constants.EQUAL) && f.getKey().equalsIgnoreCase("general.company_id")) {
				Set<String> mySet = new HashSet<>(Arrays.asList(f.getValue().split(",")));
				boolQueryBuilder.must(QueryBuilders.termsQuery(f.getKey(), mySet));
//				boolQueryBuilder.should(matchQueryBuilder);
			} else if (operator.equals(Constants.NOTEQUAL))
				boolQueryBuilder.mustNot(matchQueryBuilder);
			else if (operator.equals(Constants.GT))
				boolQueryBuilder.must(QueryBuilders.rangeQuery(f.getKey()).gt(f.getValue() + ".000000000"));
			else if (operator.equals(Constants.LT))
				boolQueryBuilder.must(QueryBuilders.rangeQuery(f.getKey()).lt(f.getValue() + ".000000000"));
			else if (operator.equals(Constants.DATE)) {
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSSSSS");
				Calendar cal = Calendar.getInstance();
				try {
					cal.setTime(dateFormat.parse(f.getValue() + ".000000000"));
				} catch (java.text.ParseException e) {
					e.printStackTrace();
				}
				cal.add(Calendar.MINUTE, 23 * 60 + 59);
				String convertedDate = dateFormat.format(cal.getTime());
				boolQueryBuilder
						.must(QueryBuilders.rangeQuery(f.getKey()).gt(f.getValue() + ".000000000").lt(convertedDate));
//				searchSourceBuilder.sort(new FieldSortBuilder(f.getKey()).order(SortOrder.DESC));
			} else if (operator.equals(Constants.EXIST)) {

				boolQueryBuilder.must(QueryBuilders.existsQuery(f.getValue()));
			} else if (operator.equals(Constants.TERM)) {
				Set<String> imeiValue = new HashSet<String>();
				String[] imei = f.getValue().split(",");
				if (f.getKey().equalsIgnoreCase("report_header.assets_id")) {
					for (String values : imei) {
						Asset asset = assetRepository.findByAssignedName(values);
						if (asset != null) {
							List<Asset_Device_xref> assetDeviceXrefList = assetDeviceXrefRepository
									.findAllByAssetId(asset.getUuid());
							for (Iterator<Asset_Device_xref> iterator = assetDeviceXrefList.iterator(); iterator
									.hasNext();) {
								Asset_Device_xref assetDeviceXref = (Asset_Device_xref) iterator.next();
								imeiValue.add(assetDeviceXref.getDevice().getImei());
							}
						}
					}
					f.setKey("report_header.device_id");

				} else {
					for (String values : imei) {
						imeiValue.add(values);
					}
				}
				boolQueryBuilder.must(QueryBuilders.termsQuery(f.getKey(), imeiValue));
			} else if (operator.equals(Constants.NOTEQUAL_TERM)) {
				Set<String> dataValue = new HashSet<String>();
				String[] dataValueList = f.getValue().split(",");
				for (String values : dataValueList) {
					dataValue.add(values);
				}
				boolQueryBuilder.mustNot(QueryBuilders.termsQuery(f.getKey(), dataValue));
			}
		}
		if (order.contentEquals("asc")) {
			orderBy = SortOrder.ASC;
		} else {
			orderBy = SortOrder.DESC;
		}
		SearchRequest searchRequest = new SearchRequest(jsonObject);
		searchSourceBuilder.from(from);
		searchSourceBuilder.size(size);
		searchSourceBuilder.query(boolQueryBuilder);
		searchSourceBuilder.sort(new FieldSortBuilder(sorting).order(orderBy));
		searchRequest.source(searchSourceBuilder);
		logger.info("Before Fetching report details");
		SearchResponse searchResponse = template.search(searchRequest, RequestOptions.DEFAULT);
		logger.info("Fetched report details");
		logger.info("Total hits: " + searchResponse.getHits().getTotalHits());
		return searchResponse;
	}

	public List<Map<String, Object>> getDeviceReport(Integer from, Integer size, String imei, String companyId,
			String startTime, String endTime) throws IOException {

//		logger.info("Inside getDeviceFromElastic service for device ");
		
		String methodName="getDeviceReport";
		Context context = new Context();
		Logutils.log(className,methodName,context.getLogUUId()," Inside getDeviceFromElastic service for device ",logger);
		
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

		if (imei != null) {
			boolQueryBuilder.must(QueryBuilders.matchQuery("report_header.device_id", imei));
		}
		if (companyId != null && companyId.length() > 2) {
			companyId = companyId.substring(2);
			boolQueryBuilder.must(QueryBuilders.matchQuery("general.company_id", companyId));
		}

		if (startTime != null && endTime != null) {
			boolQueryBuilder.must(
					QueryBuilders.rangeQuery("general_mask_fields.received_time_stamp").gt(startTime + ".000000000"));
			boolQueryBuilder.must(
					QueryBuilders.rangeQuery("general_mask_fields.received_time_stamp").lt(endTime + ".000000000"));
		} else if (startTime != null) {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSSSSS");
			Calendar cal = Calendar.getInstance();
			try {
				cal.setTime(dateFormat.parse(startTime + ".000000000"));
			} catch (java.text.ParseException e) {
				e.printStackTrace();
			}
			cal.add(Calendar.MINUTE, 23 * 60 + 59);
			String convertedDate = dateFormat.format(cal.getTime());
			boolQueryBuilder.must(QueryBuilders.rangeQuery("general_mask_fields.received_time_stamp")
					.gt(startTime + ".000000000").lt(convertedDate));
		} else if (endTime != null) {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSSSSS");
			Calendar cal = Calendar.getInstance();
			try {
				cal.setTime(dateFormat.parse(endTime + ".000000000"));
			} catch (java.text.ParseException e) {
				e.printStackTrace();
			}
			cal.add(Calendar.MINUTE, 23 * 60 + 59);
			String convertedDate = dateFormat.format(cal.getTime());
			boolQueryBuilder.must(QueryBuilders.rangeQuery("general_mask_fields.received_time_stamp")
					.gt(endTime + ".000000000").lt(convertedDate));
		}

		SearchRequest searchRequest = new SearchRequest(jsonObject);
		if (from == null) {
			from = 0;
		}
		if (size == null) {
			size = 50;
		}
		searchSourceBuilder.from(from);
		searchSourceBuilder.size(size);
		searchSourceBuilder.query(boolQueryBuilder);
		searchRequest.source(searchSourceBuilder);
		searchSourceBuilder.sort(new FieldSortBuilder("general_mask_fields.received_time_stamp").order(SortOrder.DESC));
		SearchResponse searchResponse = template.search(searchRequest, RequestOptions.DEFAULT);
		logger.info("Fetched report details");
		logger.info("Total hits: " + searchResponse.getHits().getTotalHits());
		SearchHits searchHits = searchResponse.getHits();
		SearchHit[] search = searchHits.getHits();
		List<Map<String, Object>> array = new ArrayList<>();
		for (int i = 0; i < search.length; i++) {
			SearchHit searchHit = search[i];
			array.add(searchHit.getSourceAsMap());
		}
		Logutils.log(className,methodName,context.getLogUUId()," Exiting getDeviceFromElastic service for device ",logger);
		return array;
	}

	@Override
	public Boolean updateAssetDeviceDetails(AssetDevicePayload assetDevicePayload, String username) {
		
		String methodName="updateAssetDeviceDetails";
		Context context = new Context();
		Logutils.log(className,methodName,context.getLogUUId()," Inside updateAssetDeviceDetails" + assetDevicePayload
				+ " " + username,logger);
		try {
		
			Logutils.log(className,methodName,context.getLogUUId(), " Before calling restUtils.getAssetByVinAndCan method ", logger);
			User user = restUtils.getUserFromAuthService(username);
			if (assetDevicePayload.getUuid() == null) {
				throw new DeviceException("Device UUID can not be null");
			}
			String accountNumber = null;
			Device device = deviceRepository.findByUuid(assetDevicePayload.getUuid());
			Logutils.log(className,methodName,context.getLogUUId(), " Before calling deviceRepository.findByUuid method "+device.getUuid(),logger);

			if (device == null) {
				throw new DeviceException("Device Not found");
			}
			Cellular cellularPayload = cellularRepository.findByUuid(assetDevicePayload.getCellularPayload().getUuid());
			if (cellularPayload == null) {
				cellularPayload = new Cellular();
				
				cellularPayload.setCountryCode(assetDevicePayload.getCellularPayload().getCountryCode());
				cellularPayload.setIccid(assetDevicePayload.getCellularPayload().getIccid());
				cellularPayload.setCellular(assetDevicePayload.getCellularPayload().getCellular());
				cellularPayload.setImsi(assetDevicePayload.getCellularPayload().getImsi());
				cellularPayload.setPhone(assetDevicePayload.getCellularPayload().getPhone());
				cellularPayload.setServiceNetwork(assetDevicePayload.getCellularPayload().getServiceNetwork());
				cellularPayload.setImei(assetDevicePayload.getCellularPayload().getImei());
				boolean isCellularUuidUnique = false;
				String cellularUuid = "";
				while (!isCellularUuidUnique) {
					cellularUuid = UUID.randomUUID().toString();
					Cellular byUuid = cellularRepository.findByUuid(cellularUuid);
					if (byUuid == null) {
						isCellularUuidUnique = true;
					}
				}
				cellularPayload.setUuid(cellularUuid);
				cellularRepository.save(cellularPayload);
			}
			Asset_Device_xref assetDeviceDetail = assetDeviceXrefRepository.findByDevice(assetDevicePayload.getImei());
			Asset assetPayload = null;
			AddAssetResponse response = null;
			Organisation organisation = null;
			Organisation purchaseBy = null;
			Organisation installedBy = null;
			if (assetDevicePayload.getCan() != null && !assetDevicePayload.getCan().isEmpty()) {
				organisation = restUtils.getCompanyFromCompanyService(assetDevicePayload.getCan());
				organisation.setCreatedBy(null);
				organisation.setUpdatedBy(null);
			}
			if (assetDevicePayload.getInstalledBy() != null && !assetDevicePayload.getInstalledBy().isEmpty()) {
				installedBy = restUtils.getCompanyFromCompanyService(assetDevicePayload.getInstalledBy());
				installedBy.setCreatedBy(null);
				installedBy.setUpdatedBy(null);
			}
			if (assetDevicePayload.getPurchaseBy() != null && !assetDevicePayload.getPurchaseBy().isEmpty()) {
				purchaseBy = restUtils.getCompanyFromCompanyService(assetDevicePayload.getPurchaseBy());
				purchaseBy.setCreatedBy(null);
				purchaseBy.setUpdatedBy(null);
			}
			if (assetDeviceDetail == null || assetDeviceDetail.getAsset() == null
					|| assetDeviceDetail.getAsset().getUuid() == null) {
				// throw new DeviceException("Assets Details not found/no assets Assign to
				// device");
//				&& assetDevicePayload.getAssetPayload().getManufacturerName() != null
//						&& !assetDevicePayload.getAssetPayload().getManufacturerName().isEmpty()
				if (assetDevicePayload.getAssetPayload() != null
						&& assetDevicePayload.getAssetPayload().getAssetName() != null
						&& !assetDevicePayload.getAssetPayload().getAssetName().isEmpty()) {
					AssetsPayload assetPayload2 = new AssetsPayload();
					assetPayload2.setIsVinValidated(true);
					assetPayload2.setManufacturer(assetDevicePayload.getAssetPayload().getManufacturerName());
					assetPayload2.setAssignedName(assetDevicePayload.getAssetPayload().getAssetName());
					assetPayload2.setYear(assetDevicePayload.getAssetPayload().getYear());
					assetPayload2.setVin(assetDevicePayload.getAssetPayload().getVin());
//					if (assetDevicePayload.getProductName() != null) {
//						assetPayload2.setEligibleGateway(assetDevicePayload.getProductName());
//					} else {
//						assetPayload2.setEligibleGateway("Smart7");
//					}2

					if (organisation != null) {
						CompanyPayload companyPayload = new CompanyPayload();
						companyPayload.setId(organisation.getId());
						companyPayload.setUuid(organisation.getUuid());
						companyPayload.setAccountNumber(organisation.getAccountNumber());
						companyPayload.setCompanyName(organisation.getOrganisationName());
						assetPayload2.setCompany(companyPayload);
					} else {
						throw new DeviceException("Please Select valid organisation");
					}
					;
					if (assetDevicePayload.getAssetPayload().getAssetType() != null
							&& assetDevicePayload.getAssetPayload().getAssetType() != "") {
						assetPayload2.setCategory(
								AssetCategory.valueOf(assetDevicePayload.getAssetPayload().getAssetType()).toString());
						if (assetDevicePayload.getAssetPayload().getAssetType().equalsIgnoreCase("trailer")) {
							assetPayload2.setEligibleGateway("Smart7");
						}
						if (assetDevicePayload.getAssetPayload().getAssetType().equalsIgnoreCase("tractor")) {
							assetPayload2.setEligibleGateway("SmartPair");
						}
						if (assetDevicePayload.getAssetPayload().getAssetType().equalsIgnoreCase("container")) {
							assetPayload2.setEligibleGateway("ContainerNet");
						}
						if (assetDevicePayload.getAssetPayload().getAssetType().equalsIgnoreCase("chassis")) {
							assetPayload2.setEligibleGateway("ChassisNet");
						}
					} else {
						throw new DeviceException("Please Add Valide Asset type ");
					}
					try {
						response = assetService.addAsset(assetPayload2, username);
					} catch (Exception e) {
						throw new DeviceException("Error while Adding new Asset,  " + e.getMessage());
					}
				}
			} else {
				assetPayload = assetRepository.findByUuid(assetDeviceDetail.getAsset().getUuid());
				if (assetPayload == null) {
					// throw new DeviceException("Assets Details not found/no assets Assign to
					// device");
				}
			}
			logger.info("Fetching device details for uuid : " + assetDevicePayload.getUuid()
					+ " and Cellular details for uuid " + assetDevicePayload.getCellularPayload().getUuid());
			logger.info("Asset Payload " + assetDeviceDetail);
			if (device != null && cellularPayload != null) {
				accountNumber = device.getOrganisation().getAccountNumber();
				device.setImei(assetDevicePayload.getImei());
				device.setUsageStatus(assetDevicePayload.getUsage_status());
				device.setProductCode(assetDevicePayload.getProductCode());
				device.setProductName(assetDevicePayload.getProductName());
				device.setUpdatedAt(Instant.now());
				device.setUpdatedBy(user);
				device.setIsActive(assetDevicePayload.getIsActive());
				if (organisation != null) {
					device.setOrganisation(organisation);
				}
				if (purchaseBy != null) {
					device.setPurchaseBy(purchaseBy);
				}
				if (installedBy != null) {
					device.setInstalledBy(installedBy);
				}
				cellularPayload.setCountryCode(assetDevicePayload.getCellularPayload().getCountryCode());
				cellularPayload.setIccid(assetDevicePayload.getCellularPayload().getIccid());
				cellularPayload.setCellular(assetDevicePayload.getCellularPayload().getCellular());
				cellularPayload.setImsi(assetDevicePayload.getCellularPayload().getImsi());
				cellularPayload.setPhone(assetDevicePayload.getCellularPayload().getPhone());
				cellularPayload.setServiceNetwork(assetDevicePayload.getCellularPayload().getServiceNetwork());
				cellularPayload.setImei(assetDevicePayload.getCellularPayload().getImei());
				device.setCellular(cellularPayload);

				DeviceDetails deviceDetails = new DeviceDetails();
				if (device.getDeviceDetails() != null) {
					deviceDetails = device.getDeviceDetails();
				}
				deviceDetails.setImei(device.getImei());
//				deviceDetails.setUsageStatus(assetDevicePayload.getUsage_status());
				if (assetDevicePayload.getHardwareId() != null || assetDevicePayload.getHardwareType() != null) {
					deviceDetails.setHardwareId(assetDevicePayload.getHardwareId());
					deviceDetails.setHardwareType(assetDevicePayload.getHardwareType());
				}
				device.setDeviceDetails(deviceDetails);
				logger.info("Before saving device asset details");
				deviceRepository.save(device);
				logger.info("device asset details saved successfully");

				try {
					if (!accountNumber.equalsIgnoreCase(organisation.getAccountNumber())) {
						updateDeviceReportCustomerOnElastic(device.getImei(), organisation.getAccountNumber());
						logger.info("Saving Device customer update History");
						updateDeviceCustomerHistory(accountNumber, device);
					}
				} catch (Exception ex) {
					logger.info("Exception while updating device customer history");
					ex.printStackTrace();
				}
				logger.info("Update Customer Id in redis");

				updateCustomerAndTypeInRedis(device.getImei(), device.getDeviceType() , device.getOrganisation().getAccountNumber());
				logger.info("Device update Successfully");
				if (response != null && response.getAssetPayload() != null) {
					Asset_Device_xref xref = new Asset_Device_xref();
					Asset asset = new Asset();
					logger.info("Going to mapping asset to device");
					asset.setUuid(response.getAssetPayload().getUuid());
					asset.setId(response.getAssetPayload().getId());
					xref.setAsset(asset);
					xref.setDevice(device);
					xref.setActive(false);
					assetDeviceXrefRepository.save(xref);
					logger.info("Asset mapped to device Successfully");
				}
				if (assetPayload != null) {
					AssetsPayload assetsPayload = new AssetsPayload();
					if (assetDevicePayload.getAssetPayload().getManufacturerName() != null
							&& !assetDevicePayload.getAssetPayload().getManufacturerName().isEmpty()) {
						assetsPayload.setManufacturer(assetDevicePayload.getAssetPayload().getManufacturerName());
					}
					assetsPayload.setAssignedName(assetDevicePayload.getAssetPayload().getAssetName());
					assetsPayload.setYear(assetDevicePayload.getAssetPayload().getYear());
					assetsPayload.setEligibleGateway(assetPayload.getGatewayEligibility());
					assetsPayload.setId(assetPayload.getId());
					assetsPayload.setUuid(assetPayload.getUuid());
					assetsPayload.setVin(assetDevicePayload.getAssetPayload().getVin());
					if (organisation != null) {
						CompanyPayload companyPayload = new CompanyPayload();
						companyPayload.setId(organisation.getId());
						companyPayload.setUuid(organisation.getUuid());
						companyPayload.setAccountNumber(organisation.getAccountNumber());
						companyPayload.setCompanyName(organisation.getOrganisationName());
						assetsPayload.setCompany(companyPayload);
					}
					;
					if (assetDevicePayload.getAssetPayload().getAssetType() != null
							&& assetDevicePayload.getAssetPayload().getAssetType() != "") {
						assetsPayload.setCategory(
								AssetCategory.valueOf(assetDevicePayload.getAssetPayload().getAssetType()).toString());
					} else {
						throw new DeviceException("Please Add Valide Asset type ");
					}

					try {
						assetService.updateAsset(assetsPayload, username);
						logger.info("Asset updated for the uuid : " + assetDevicePayload.getUuid());
						try {
							logger.info("Update Asset Id and Asset Type in redis");
							redisDeviceRepository.addMap(DEVICE_CURRENT_VIEW_PREFIX + device.getImei(), "assetId",
									assetsPayload.getAssignedName());
							redisDeviceRepository.addMap(DEVICE_CURRENT_VIEW_PREFIX + device.getImei(), "assetType",
									assetsPayload.getCategory().toUpperCase());
						} catch (Exception ex) {
							logger.info("Exception while updating asset data on Redis");
							ex.printStackTrace();
						}
						deviceRepository.updateDeviceAssetData();
					} catch (Exception e) {
						// TODO: handle exception
						throw new DeviceException("Error while updating Asset:- " + e.getMessage());
					}

				}
			} else {
				throw new DeviceException("Device not found for the uuid " + assetDevicePayload.getUuid());
			}
			return Boolean.TRUE;
		} catch (Exception e) {
			e.printStackTrace();
			;
			throw new DeviceException("Device details not Updated :-" + e.getMessage());
		}
	}

	@Override
	public void getDeviceDetailsForCsv(List<Page<DeviceResponsePayload>> payloadList, HttpServletResponse response,
			String userName, String token, int totalSize, List<String> columnDef) throws IOException {
		// TODO Auto-generated method stub
		String methodName = "getDeviceDetailsForCsv";
		Context context = new Context();
		Logutils.log(className, methodName, context.getLogUUId(), "Inside getDeviceDetailsForCsv ", logger);

		Logutils.log(className, " Before calling restUtils.getUserFromAuthServiceWithToken method ", logger);
		User user = restUtils.getUserFromAuthServiceWithToken(userName, token);
		logger.info("Exporting DB data into csv file");
		restUtils.writeFilterCSVFile(DEVICE_CURRENT_VIEW_PREFIX, payloadList, response, user, totalSize, columnDef);
	}

	public String updateLatestReport(String imei, String latestReport) {
//		logger.info("Inside updateLatestReport and fetching imei and latestReport value" + imei + " " + latestReport);
		
		String methodName="updateLatestReport";
		Context context = new Context();
		Logutils.log(className,methodName,context.getLogUUId(),"Inside updateLatestReport and fetching imei and latestReport value" + imei + " " + latestReport,logger);
		boolean status = false;
		String updatelatestReport = null;
		try {

			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date date1 = dateFormat.parse(latestReport);
			Integer updateLatestReport = deviceRepository.updateLatestReport(imei, date1);
			if (updateLatestReport > 0) {
				status = true;
				updatelatestReport = latestReport;
			}

		} catch (ParseException ex) {
			ex.printStackTrace();
		}
		Logutils.log(className,methodName,context.getLogUUId(),"Exiting updateLatestReport and fetching imei and latestReport value",logger);
		return updatelatestReport;
	}

	@Override
	public Boolean addDeviceDetailInBatch(List<DeviceDetailsRequest> listDeviceUploadRequest) throws DeviceException {
//		logger.info("Inside addDeviceDetail and fetching deviceDetail and userId value",
//				deviceUploadRequest + " " + userName);
		
		String methodName="addDeviceDetailInBatch";
		Context context = new Context();
		Logutils.log(className,methodName,context.getLogUUId()," Inside addDeviceDetailInBatch",logger);

		List<Device> deviceList = new ArrayList<>();
		for (DeviceDetailsRequest deviceUploadRequest : listDeviceUploadRequest) {
			String can = deviceUploadRequest.getCan();
			if (can == null) {
				throw new DeviceException("Company can not be null, Company name :-" + can);
			}
			Logutils.log(className,methodName,context.getLogUUId(), " Before calling deviceCompanyRepository.findByMs1OrganisationName method ", logger);
			List<DeviceCompany> deviceCompanies = deviceCompanyRepository.findByMs1OrganisationName(can);

			if (deviceCompanies == null || deviceCompanies.size() <= 0) {
				throw new DeviceException("Company not exist in ms2 systyem, Company name :-" + can);
			}
			Organisation company = deviceCompanies.get(0).getOrganisation();
			String imei = deviceUploadRequest.getImei();
			if (company != null) {
				if (imei != null) {
					if (imei.length() == Constants.IMEI_LENGTH) {
						Device byImei = deviceRepository.findByImei(imei);
						if (byImei == null) {
							Device device = new Device();
							device = beanConverter.convertDeviceDetailRequestToDeviceBean(deviceUploadRequest);
							device.setOrganisation(company);
							boolean isDeviceUuidUnique = false;
							String deviceUuid = "";
							while (!isDeviceUuidUnique) {
								deviceUuid = UUID.randomUUID().toString();
								Device byUuid = deviceRepository.findByUuid(deviceUuid);
								if (byUuid == null) {
									isDeviceUuidUnique = true;
								}
							}
							device.setCreatedBy(null);
							device.setUuid(deviceUuid);
							deviceList.add(device);
							deviceRepository.save(device);
//							logger.info("Device Details saved successfully");
//							deviceStatusTransient.setDate_created(Instant.now());
//							deviceStatusTransient.setDeviceId(imei);
//							deviceStatusTransient.setStatus(DeviceStatus.PENDING);
							// deviceStatusTransientRepo.save(deviceStatusTransient);
							logger.info("Device Status saved successfully");

						} else {
							throw new DeviceException("IMEI/MAC Address number already exist :-" + byImei);
						}
					} else {
						throw new DeviceException("Invalid IMEI number :- " + imei);
					}
				} else {
					throw new DeviceException("IMEI/MAC Address number can not be null");
				}
			} else {
				throw new DeviceException("No Company found for account number " + deviceUploadRequest.getCan());
			}

			updateCustomerAndTypeInRedis(deviceUploadRequest.getImei(), deviceUploadRequest.getDeviceType(),
					deviceUploadRequest.getCan());
		}
		Logutils.log(className,methodName,context.getLogUUId()," Exiting addDeviceDetailInBatch",logger);
		return Boolean.TRUE;
	}

	@Override
	public Page<LatestDeviceReportCount> getLatestDeviceReportCountWithPagination(Map<String, Filter> filterValues,
			String filterModelCountFilter, Pageable pageable, String userName) {
		
		String methodName="getLatestDeviceReportCountWithPagination";
		Context context = new Context();
		Logutils.log(className,methodName,context.getLogUUId()," Inside getLatestDeviceReportCountWithPagination",logger);
		
		
		Page<LatestDeviceReportCount> deviceDetails = null;
		
		Logutils.log(className,methodName,context.getLogUUId(), " Before calling restUtils.getUserFromAuthService method ", logger);
		User user = restUtils.getUserFromAuthService(userName);
		Logutils.log(className,methodName,context.getLogUUId(), " After calling restUtils.getUserFromAuthService method "+ user.getUserName(), logger);
		
		Specification<LatestDeviceReportCount> spc = DeviceSpecification
				.getLatestDeviceReportCountListSpecification(filterValues, filterModelCountFilter, user);
		logger.info("After Specification " + spc);
		deviceDetails = latestDeviceReportRepository.findAll(spc, pageable);
		logger.info("Fetching latest device report count for specification");
		return deviceDetails;
	}

	@Override
	public Page<ReportCount> getReportCountWithPagination(Map<String, Filter> filterValues,
			String filterModelCountFilter, Pageable pageable, String userName) {
		
		String methodName="getReportCountWithPagination";
		Context context = new Context();
		Logutils.log(className,methodName,context.getLogUUId()," Inside getReportCountWithPagination",logger);
		
		Page<ReportCount> deviceDetails = null;
		
		Logutils.log(className, " Before calling restUtils.getUserFromAuthService method ", logger);
		User user = restUtils.getUserFromAuthService(userName);
		Logutils.log(className, " After calling restUtils.getUserFromAuthService method "+ user.getUserName(), logger);
		
		Specification<ReportCount> spc = DeviceSpecification.getReportCountListSpecification(filterValues,
				filterModelCountFilter, user);
		logger.info("After Specification " + spc);
		deviceDetails = reportCount.findAll(spc, pageable);
		logger.info("Fetching latest device report count for specification");
		return deviceDetails;
	}

	@Override
	public Page<DeviceReportCount> getDeviceReportCountWithPagination(Map<String, Filter> filterValues,
			String filterModelCountFilter, Pageable pageable, String userName) {

		String methodName = "getDeviceReportCountWithPagination";
		Context context = new Context();
		Logutils.log(className, methodName, context.getLogUUId(), " Inside getDeviceReportCountWithPagination", logger);

		Page<DeviceReportCount> deviceDetails = null;
		
		Logutils.log(className, " Before calling restUtils.getUserFromAuthService method ", logger);
		User user = restUtils.getUserFromAuthService(userName);
		Logutils.log(className, " After calling restUtils.getUserFromAuthService method "+ user.getUserName(), logger);
		
		Specification<DeviceReportCount> spc = DeviceSpecification.getDeviceReportCountListSpecification(filterValues,
				filterModelCountFilter, user);
		logger.info("After Specification " + spc);
		deviceDetails = latestReportCountRepository.findAll(spc, pageable);
		logger.info("Fetching latest device report count for specification");
		return deviceDetails;
	}

	@Override
	public int getDeviceCountByOrganizationId(List<String> ids) {
		return deviceRepository.getCountByOrganizationId(ids);
	}
	
	@Override
	public Device updateDeviceAssetStatus(@Valid UpdateGatewayAssetStatusRequest updateDeviceAssetStatusRequest) {
//		logger.info("service method for updating asset gateway status for finsih install");
		
		String methodName = "updateDeviceAssetStatus";
		Context context = new Context();
		Logutils.log(className, methodName, context.getLogUUId(), " service method for updating asset gateway status for finsih install", logger);
		
		
		Device device = deviceRepository.findByUuid(updateDeviceAssetStatusRequest.getGatewayUuid());
		Logutils.log(className,methodName,context.getLogUUId(), " After calling  deviceRepository.findByUuid method "+device.getUuid(), logger);
		if (device != null) {
			device.setStatus(updateDeviceAssetStatusRequest.getGatewayStatus());
			device.setLastPerformAction(Constants.GATEWAYS_ACTION_UPDATED);
			device.setTimeOfLastDownload(Instant.now());
			  device = deviceRepository.save(device);
			logger.info("gateway status update for finish install " + device.getStatus());
			Asset asset = device.getAssetDeviceXref().getAsset();
			asset.setStatus(updateDeviceAssetStatusRequest.getAssetStatus());
			assetRepository.save(asset);
			logger.info("asset status update for finish install " + asset.getStatus());
			return device;
		}
		Logutils.log(className, methodName, context.getLogUUId(), " Exiting service method for updating asset gateway status for finsih install", logger);
		return null;
	}
	
	@Override
	public Long updateAssetToDeviceInMS(UpdateAssetToDeviceForInstallationRequest request) throws Exception {

		String methodName = "updateAssetToDeviceInMS";
		Context context = new Context();
		Logutils.log(className, methodName, context.getLogUUId(), " Inside updateAssetToDeviceInMS", logger);
		
		
		Logutils.log(className,methodName,context.getLogUUId(), " Before calling assetToDeviceRepository.findByAssetID method ", logger);
		AssetToDevice assetToDevice = assetToDeviceRepository.findByAssetID(request.getAssignedName());
		
		if (assetToDevice == null) {
			assetToDevice = assetToDeviceRepository.findByVin(request.getVin());
			if (assetToDevice == null) {
				logger.info("No record for Asset ID {} and VIN {} in AssetToDevice table. New record will be created",
						request.getAssignedName(), request.getVin());
				assetToDevice = new AssetToDevice();
				long recordId = 0l;
				try {
					recordId = assetToDeviceRepository.findMaximumRecordId();
				} catch (Exception e) {
					logger.error("Error occurred while getting  AssetToDevice record in MS - ", e);
				}
				recordId++;
				assetToDevice.setRECORD_ID(recordId);
				assetToDevice.setASSET_ID(request.getAssignedName());
				assetToDevice.setVIN(request.getVin());
				assetToDevice.setCUSTOMER(request.getCustomerName());
			}
		}
		if (assetToDevice.getASSET_TYPE() == null || assetToDevice.getASSET_TYPE().isEmpty()) {
			assetToDevice.setASSET_TYPE(request.getAssetType());
		}
		if (assetToDevice.getMAKE() == null || assetToDevice.getMAKE().isEmpty()) {
			assetToDevice.setMAKE(request.getMake());
		}
		if (assetToDevice.getMODEL() == null || assetToDevice.getMODEL().isEmpty()) {
			assetToDevice.setMODEL(request.getModel());
		}
		if (assetToDevice.getYEAR() == null || assetToDevice.getYEAR().isEmpty()) {
			if (null != request.getYear()) {
				if (request.getYear().equalsIgnoreCase("Not Available")) {
					assetToDevice.setYEAR(null);
				} else {
					assetToDevice.setYEAR(request.getYear());
				}
			} else {
				assetToDevice.setYEAR(null);
			}

		}
		assetToDevice.setDEVICE_ID(request.getImei());
		assetToDevice.setINSTALL_TIMESTAMP(request.getInstallTimestamp());
		assetToDevice.setCOMMENT("Updated from InstallAssist");
		assetToDevice = assetToDeviceRepository.save(assetToDevice);
		Long maximumRecordId = assetToDeviceHistoryRepository.findMaximumRecordId();
		if (maximumRecordId == null) {
			maximumRecordId = 1l;
		}
		AssetToDeviceHistory assetToDeviceHistory = new AssetToDeviceHistory();
		assetToDeviceHistory.setRECORD_ID(maximumRecordId + 1);
		assetToDeviceHistory.setASSET_ID(request.getAssignedName());
		assetToDeviceHistory.setDEVICE_ID(request.getImei());
		assetToDeviceHistory.setINSTALL_TIMESTAMP(request.getInstallTimestamp());
		assetToDeviceHistory.setCUSTOMER(request.getCustomerName());
		assetToDeviceHistory = assetToDeviceHistoryRepository.save(assetToDeviceHistory);
		Logutils.log(className, methodName, context.getLogUUId(), " exiting updateAssetToDeviceInMS", logger);
		return assetToDevice.getRECORD_ID();
	}

	
	
	
	
	


	public Set<String> getDeviceImeisByCustomerAccountNumber(String accountNumber) {
		logger.info("Inside getDeviceImeisByCustomerAccountNumber method for account number " + accountNumber);
		return deviceRepository.findImeiByOrganisationAccountNumber(accountNumber);
	}

	/**
	 * @param sort
	 * @return
	 */
	Pageable getPageable(int page, int size, String sort, String _order) {

		if (null == sort)
			return PageRequest.of(page, size);

		Pattern pattern = Pattern.compile(Constants.SORT_PATTERN);
		Matcher matcher = pattern.matcher(sort + Constants.COMMA);
		List<Sort.Order> orderList = new ArrayList<Sort.Order>();
		while (matcher.find()) {
			orderList.add(new Sort.Order(getEnum(matcher.group(3)), matcher.group(1)));
			logger.debug("shorting order: " + matcher.group(1) + ": " + matcher.group(3));
		}
		if ("ASC".equalsIgnoreCase(_order)) {

			return PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, sort));
		} else {
			return PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, sort));
		}
	}
	
	
	
	
	
	
	

	/**
	 * @param direction
	 * @return
	 */
	Direction getEnum(String direction) {

		if (direction.equalsIgnoreCase("desc"))
			return Direction.DESC;
		else
			return Direction.ASC;
	}

	public Map<String, String> getAllDeviceCompanyMapping() {
		Map<String, String> orgMappingMap = new HashMap<>();

		List<Map<String, Object>> deviceCompanies = deviceCompanyRepository
				.findMs1OrganisationNameMs2OrganisationNameAll();

		deviceCompanies.forEach(map -> {
			orgMappingMap.put((String) map.get("ms1OrgName"), (String) map.get("ms2OrgName"));
		});

		return orgMappingMap;
	}

	@Override
	@Transactional
	public Boolean updateDeviceCustomerDetails(DeviceCustomerUpdatePayload deviceCustomerUpdatePayload) {
//		logger.info("Inside updateDeviceCustomerDetails method ");
		
		String methodName = "updateDeviceCustomerDetails";
		Context context = new Context();
		Logutils.log(className, methodName, context.getLogUUId(), " Inside updateDeviceCustomerDetails method", logger);
		
		if (deviceCustomerUpdatePayload.getCan() == null || deviceCustomerUpdatePayload.getCan().isEmpty()) {
			throw new DeviceException("Can number can not be null");
		}
		if (deviceCustomerUpdatePayload.getDeviceIds() == null
				|| deviceCustomerUpdatePayload.getDeviceIds().size() <= 0) {
			throw new DeviceException("Device List can not be null");
		}
//		logger.info("Before Getting device details");
//		Device device = deviceRepository.findByImei(deviceCustomerUpdatePayload.getDeviceId());
//		if (device == null) {
//			throw new DeviceException("Device Not found");
//		}
		logger.info("Before Getting organisation details");
		Organisation organisation = restUtils.getCompanyFromCompanyService(deviceCustomerUpdatePayload.getCan());
		if (organisation == null) {
			throw new DeviceException(
					"Organisation Not found for this Can number " + deviceCustomerUpdatePayload.getCan());
		}
		for (String deviceId : deviceCustomerUpdatePayload.getDeviceIds()) {
			try {
				logger.info("Before Getting device details");
				Device device = deviceRepository.findByImei(deviceId);
				if (device == null) {
					throw new DeviceException("Device Not found For this Device Id:- " + deviceId);
				}
				String accountNumber = "";
				if (device.getOrganisation() != null) {
					accountNumber = device.getOrganisation().getAccountNumber();
				}
				logger.info("Before Save device details");
				deviceRepository.updateDeviceCustomer(deviceId, deviceCustomerUpdatePayload.getCan());

				try {
					if (!accountNumber.equalsIgnoreCase(deviceCustomerUpdatePayload.getCan())) {
						updateDeviceReportCustomerOnElastic(deviceId, deviceCustomerUpdatePayload.getCan());
						logger.info("Saving Device customer update History");
						updateDeviceCustomerHistory(accountNumber, device);
					}
				} catch (Exception ex) {
					logger.info("Exception while updating device customer history");
					ex.printStackTrace();
				}
				redisDeviceRepository.addMap(DEVICE_CURRENT_VIEW_PREFIX + deviceId, "customerId",
						deviceCustomerUpdatePayload.getCan());
			} catch (Exception ex) {
				logger.info(ex.getMessage());
				ex.printStackTrace();
				throw new DeviceException(ex.getMessage());
			}
		}
		Logutils.log(className, methodName, context.getLogUUId(), " Exiting updateDeviceCustomerDetails method", logger);
		return true;
	}

	private void updateDeviceReportCustomerOnElastic(String imei, String can) {
//		logger.info("Update Device report customer in elastic");
		
		String methodName = "updateDeviceReportCustomerOnElastic";
		Context context = new Context();
		Logutils.log(className, methodName, context.getLogUUId(), " Update Device report customer in elastic", logger);
		
		UpdateByQueryRequest request = new UpdateByQueryRequest(jsonObject);
		request.setQuery(new TermQueryBuilder("report_header.device_id", imei));
		Script script = new Script("ctx._source.general.company_id = \"" + can + "\";");
		request.setScript(script);
		try {
			template.updateByQuery(request, RequestOptions.DEFAULT);
			logger.info("Update Device report customer in elastic Successfully");
		} catch (IOException e) {
			logger.info("Error while updating device report customer in elastic");
			e.printStackTrace();
		}
	}

	@Override
	public Resource downloadImage(String imageName) {
		URL url;
		byte[] response;
		try {
			logger.info("started downloading image for " + imageName);
			url = new URL(fileDownloadUrl.concat(imageName));
			logger.info("url " + url.toString());
			InputStream in = new BufferedInputStream(url.openStream());
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			byte[] buf = new byte[1024];
			int n;
			while (-1 != (n = in.read(buf))) {
				out.write(buf, 0, n);
			}
			out.close();
			in.close();
			response = out.toByteArray();
			File file = new File(fileDownloadDir);
			Path path = Paths.get(file.getAbsolutePath()).resolve(imageName).normalize();
			logger.info("image download path " + path.toString());
			Resource resource = new UrlResource(path.toUri());
			if (resource.exists()) {
				logger.info("image existed");
				Files.delete(path);
			}
			FileOutputStream fos = new FileOutputStream(fileDownloadDir + imageName);
			fos.write(response);
			fos.close();
			logger.info("image downloaded at " + path.toString() + " path");
			logger.info("completed downloading image for " + imageName);
			return resource;
		} catch (IOException e) {
			throw new DeviceException(e.getMessage());
		} catch (Exception e) {
			throw new DeviceException(e.getMessage());
		}
	}

	//-----------------------------------------------Aamir-----------------------------------------------------------------//
	@Override
	public Device getGatewayByMACAndCan(String mac, String can) {
		String methodName = "getGatewayByMACAndCan";
		Context context = new Context();
		Logutils.log(className, methodName, context.getLogUUId(), " Inside getGatewayByMACAndCan method", logger);
		Device device = null;
		if (can != null) {
			device = deviceRepository.findByMACAndAccountNumber(mac, can);
		} else {
			device = deviceRepository.findByMac_address(mac);
		}
		
		Logutils.log(className, methodName, context.getLogUUId(), " Exiting getGatewayByMACAndCan method", logger);
		return device;
	}
	
	public Device getDeviceByImei(String imei) {
		Device byImei = getDevice(imei);
		return byImei;
	}
	
	

	@Override
	public Boolean updateCompanyInAsset(String accountNumber, String asset_uuid) {
//		logger.info("Asset compnay is null now we are updating the asset company " + accountNumber);
		
		String methodName = "updateCompanyInAsset";
		Context context = new Context();
		Logutils.log(className, methodName, context.getLogUUId(), " Asset compnay is null now we are updating the asset company " + accountNumber, logger);
		
		
		
		Asset asset = assetRepository.findByUuid(asset_uuid);
		Logutils.log(className,methodName,context.getLogUUId(), " After calling assetRepository.findByUuid method "+asset.getUuid(), logger);
		try {
			Organisation com = restUtils.getCompanyFromCompanyService(accountNumber);
			asset.setOrganisation(com);
			assetRepository.save(asset);
			return true;
		} catch (Exception e) {
			return false;
		}

	}
	
	//Aamir
	
		public Device getDeviceByUUID(String uuid) {
			Device device = null;
			try {
				device = deviceRepository.findByUuid(uuid);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return device;
		}

	@Override
	public Device updateAssetForGateway(String gatewayUuid, String assetUuid) throws Exception {
		
		String methodName = "updateAssetForGateway";
		Context context = new Context();
		Logutils.log(className, methodName, context.getLogUUId(), " Inside updateAssetForGateway " , logger);
		
		Device device = deviceRepository.findByUuid(gatewayUuid);
		Logutils.log(className,methodName,context.getLogUUId(), " After calling  deviceRepository.findByUuid method "+ device.getUuid(), logger);
		if (device != null) {
			Asset asset = assetRepository.findByUuid(assetUuid);
			if (asset != null) {
				asset.setStatus(AssetStatus.INSTALL_IN_PROGRESS);
				asset = assetRepository.save(asset);
				device.setStatus(DeviceStatus.INSTALL_IN_PROGRESS);
				device.setLastPerformAction(Constants.GATEWAYS_ACTION_UPDATED);
				device.setTimeOfLastDownload(Instant.now());
				device = deviceRepository.save(device);
			} else {
				throw new Exception("Asset found null for Id = " + assetUuid);
			}
		} else {
			throw new Exception("Gateway found null for Id = " + gatewayUuid);
		}
		Logutils.log(className, methodName, context.getLogUUId(), " Exiting updateAssetForGateway " , logger);
		return device;
	}

	@Override
	public List<AssetSensorXref> getAllAssetSensorXrefForAssetUuid(String assetUuid) {
		List<AssetSensorXref> listOfAssetSensorXref = null;
		if (assetUuid != null) {
			listOfAssetSensorXref = assetSensorXrefRepository.findByAssetUuid(assetUuid);
		}
		return listOfAssetSensorXref;
	}

	@Override
	public Device getGatewayByImeiAndCan(String imei, String can) {
		Device device = deviceRepository.findByImeiAndAccountNumber(imei, can);
		return device;
	}
//	public AssetGatewayXref saveAssetDeviceXref(SaveAssetGatewayXrefRequest saveAssetGatewayXrefRequest) {
//		// TODO Auto-generated method stub
//		return null;
//	}
	 @Override
	    public DeviceResponsePayload updateGatewayStatus(InstallationStatusGatewayRequest installationStatusGatewayRequest, String userName) {
//	    	logger.info("Inside UpdateGateway Status after finishing install");
	    	
	    	String methodName = "updateGatewayStatus";
			Context context = new Context();
			Logutils.log(className, methodName, context.getLogUUId(), " Inside UpdateGateway Status after finishing install" , logger);
	    	
			
			Logutils.log(className, " Before calling restUtils.getUserFromAuthService method ", logger);
	    	User user = restUtils.getUserFromAuthService(userName);
			Logutils.log(className, " After calling restUtils.getUserFromAuthService method "+ user.getUserName(), logger);
			
	    	Device devices = new Device();
			
			Logutils.log(className,methodName,context.getLogUUId(), " Befter calling deviceRepository.findByUuid method ", logger);
	        Device device = deviceRepository.findByUuid(installationStatusGatewayRequest.getGatewayUuid());
	        Logutils.log(className,methodName,context.getLogUUId(), " After calling deviceRepository.findByUuid method "+ device.getUuid() , logger);
	        
	       // Logutils.log(className,methodName,context.getLogUUId()," find gateway Object using findByUuid method ",LOGGER, context.getLogUUId());
	        device.setStatus(DeviceStatus.valueOf(installationStatusGatewayRequest.getStatus()));
	        if(installationStatusGatewayRequest.getDatetimeRt() != null) {
	        	logger.info("DatetimeRt value :"+installationStatusGatewayRequest.getDatetimeRt());
	        device.setUpdatedAt(Instant.ofEpochMilli(Long.parseLong(installationStatusGatewayRequest.getDatetimeRt())));
	        }else {
	        	device.setUpdatedAt(Instant.now());	
	        }
	        device.setLastPerformAction(Constants.GATEWAYS_ACTION_UPDATED);
	        device.setTimeOfLastDownload(Instant.now());
	        device = deviceRepository.save(device);
	       // Logutils.log(className,methodName,context.getLogUUId(),"After Gateway Data Saved ",LOGGER, context.getLogUUId());
	    	logger.info("Gateway Status marked pending while updating gateway status");
	    	 DeviceResponsePayload deviceResponsePayload=new DeviceResponsePayload();
		        BeanUtils.copyProperties(device, deviceResponsePayload);
		    	Logutils.log(className, methodName, context.getLogUUId(), " exiting UpdateGateway Status after finishing install" , logger);
	        return deviceResponsePayload;
	    }
	@Override
	public List<AssetSensorXref> updateAssetSensorXref(List<AssetSensorXrefPayload> assetSensorXrefPayload) {
		
		List<AssetSensorXref> assetSensorXrefList =new ArrayList();		
		for (AssetSensorXrefPayload element : assetSensorXrefPayload) {
			AssetSensorXref asset_Sensor_xref =new AssetSensorXref();
			BeanUtils.copyProperties(element, asset_Sensor_xref);
			assetSensorXrefList.add(asset_Sensor_xref);
			}	
		List<AssetSensorXref> saveAllAndFlush2 = assetSensorXrefRepository.saveAllAndFlush(assetSensorXrefList);
		return saveAllAndFlush2;
	}
	@Override
	public Device updateSensor(SensorUpdateRequest sensorUpdateRequest) {

    	String methodName = "updateSensor";
		Context context = new Context();
		Logutils.log(className, methodName, context.getLogUUId(), " Inside updateSensor " , logger);
		
		Device sensor = deviceRepository.findByUuid(sensorUpdateRequest.getSensorUuid());
		Logutils.log(className, " After calling deviceRepository.findByUuid method "+ sensor.getUuid(), logger);
		sensor.setUpdatedOn(Instant.ofEpochMilli(Long.parseLong(sensorUpdateRequest.getUpdatedOn())));
		sensor.setStatus(DeviceStatus.getGatewayStatusInSearch(sensorUpdateRequest.getStatus()));
		sensor = deviceRepository.save(sensor);
		
		Logutils.log(className, " Before calling deviceDeviceXrefRepository.findSensorBySensorUuid method ", logger);
		Device_Device_xref deviceSensorxref = deviceDeviceXrefRepository.findSensorBySensorUuid(sensor);
		
		 if(deviceSensorxref.getDeviceUuid() != null) {
			 Device gateway = deviceSensorxref.getDeviceUuid();
	        	gateway.setLastPerformAction(Constants.GATEWAYS_ACTION_UPDATED);
	        	gateway.setTimeOfLastDownload(Instant.now());
	        	deviceRepository.save(gateway);
	        }
		return sensor;
	}
	
	@Override
    public Device updateSensorBySensorObj(Device sensor) {
		String methodName = "updateSensorBySensorObj";
		Context context = new Context();
		Logutils.log(className, methodName, context.getLogUUId(), " Inside updateSensorBySensorObj " , logger);
		
		Device sensorObj = deviceRepository.save(sensor);
		Device_Device_xref deviceSensorxref = deviceDeviceXrefRepository.findSensorBySensorUuid(sensor);
		Logutils.log(className, " After calling deviceDeviceXrefRepository.findSensorBySensorUuid method "+deviceSensorxref.getSensorUuid(), logger);
		
		 if(deviceSensorxref.getDeviceUuid() != null) {
			 Device gateway = deviceSensorxref.getDeviceUuid();
	        	gateway.setLastPerformAction(Constants.GATEWAYS_ACTION_UPDATED);
	        	gateway.setTimeOfLastDownload(Instant.now());
	        	deviceRepository.save(gateway);
	        }
		 Logutils.log(className, methodName, context.getLogUUId(), " Exiting updateSensorBySensorObj " , logger);
        return sensorObj;
    }
	@Override
	public List<Device_Device_xref> saveGatewaySensorXref(List<DeviceSensorxrefPayload>  gatewaySensorXref) {
		String methodName = "saveGatewaySensorXref";
		Context context = new Context();
		Logutils.log(className, methodName, context.getLogUUId(), " Inside saveGatewaySensorXref " , logger);
		
		List<Device_Device_xref> device_Sensor_xref_lst =new ArrayList();
		for (DeviceSensorxrefPayload element : gatewaySensorXref) {
			Device_Device_xref device_Sensor_xref =new Device_Device_xref();
			BeanUtils.copyProperties(element, device_Sensor_xref);
			device_Sensor_xref_lst.add(device_Sensor_xref);
			}
		saveAllAndFlush = gatewaySensorXrefRepository.saveAllAndFlush(device_Sensor_xref_lst);
		Logutils.log(className, methodName, context.getLogUUId(), " Exiting saveGatewaySensorXref " , logger);
		
		return saveAllAndFlush;
	}
	//-----------------------------------------------Aamir-----------------------------------------------------------------//

	@Override
	@Transactional
	public Boolean resetInstall(String logUUid, Long assetId, Long gatewayId) {
	
//		Logutils.log(className, methodName, context.getLogUUId(), " Reset installation method call in DeviceService",
//				logger, (gatewayId != null ? gatewayId.toString() : ""), (assetId != null ? assetId.toString() : ""));
		String methodName = "resetInstall";
		Context context = new Context();
		Logutils.log(className, methodName, context.getLogUUId(), " Inside Reset installation method call in DeviceService" , logger);
		
		if (assetId != null && gatewayId != null) {
			List<Asset_Device_xref> assetGatewayXrefList = assetDeviceXrefRepository.findByAssetIdAndGatewayId(assetId, gatewayId);
			assetGatewayXrefList.forEach(assetGatewayXref -> {
				Logutils.log(logUUid, " List of AssetGatewayXRef found in method call in DeviceService", logger, assetGatewayXref.getDevice().getImei().toString(),
						assetGatewayXref.getAsset().getVin().toString());
				Device gateway = assetGatewayXref.getDevice();
				gateway.setStatus(DeviceStatus.PENDING);
				Logutils.log(logUUid, " gateway status marked as pending", logger, gateway.getStatus().toString());
//				gateway.setAsset(null);

				Logutils.log(className,methodName,context.getLogUUId(), " Before calling deviceDeviceXrefRepository.findSensorByGatewayId method ", logger);
				List<Device_Device_xref> deviceSensorxreffList = deviceDeviceXrefRepository.findSensorByGatewayId(gatewayId);
				deviceSensorxreffList.forEach(sensor -> {
					Device d = sensor.getSensorUuid();
					d.setStatus(DeviceStatus.PENDING);
					deviceRepository.save(d);
				});
				gateway.setLastPerformAction(Constants.GATEWAYS_ACTION_UPDATED);
                gateway.setTimeOfLastDownload(Instant.now());
				Device gat = deviceRepository.save(gateway);
				Logutils.log(logUUid, " gateway status marked as pending after saving gateway", logger, gat.getStatus().toString());
				Asset asset = assetRepository.findById(assetId).get();
				asset.setStatus(AssetStatus.PENDING);
				logger.info("Asset has set as Pending");
				asset = assetRepository.save(asset);
				assetDeviceXrefRepository.delete(assetGatewayXref);
				deviceDeviceXrefRepository.deleteAll(deviceSensorxreffList);

			});

			Logutils.log(logUUid, " gateway and asset status marked as pending ", logger, gatewayId.toString(), assetId.toString());
		} else if (assetId != null && gatewayId == null) {
			List<Asset_Device_xref> assetGatewayXrefList = assetDeviceXrefRepository.findByAssetId(assetId);
			assetGatewayXrefList.forEach(assetGatewayXref -> {
				Device gateway = assetGatewayXref.getDevice();
				if(gateway != null) {
					gateway.setAssetDeviceXref(null);
					gateway.setStatus(DeviceStatus.PENDING);
					logger.info("gateway status marked pending  " + gateway.getStatus());
//					gateway.setAsset(null);
//					
//					
//					gateway.getSensors().forEach(sensor -> {
//						sensor.setStatus(SensorStatus.PENDING);
//						sensorRepository.save(sensor);
//					});
					Logutils.log(className,methodName,context.getLogUUId(), " Before calling  deviceDeviceXrefRepository.findSensorByGatewayId method ", logger);
					List<Device_Device_xref> deviceSensorxreffList = deviceDeviceXrefRepository.findSensorByGatewayId(gateway.getId());
					deviceSensorxreffList.forEach(sensor -> {
						Device d = sensor.getSensorUuid();
						d.setStatus(DeviceStatus.PENDING);
						deviceRepository.save(d);
					});
					gateway.setLastPerformAction(Constants.GATEWAYS_ACTION_UPDATED);
	                gateway.setTimeOfLastDownload(Instant.now());
					deviceRepository.save(gateway);
					logger.info("gateway object status marked pending after saving " + gateway.getStatus());
//					if(deviceSensorxreffList != null) {
//						deviceDeviceXrefRepository.deleteAll(deviceSensorxreffList);
//					}
				}
				assetDeviceXrefRepository.delete(assetGatewayXref);
				
			});
			Asset asset = assetRepository.findById(assetId).get();
			asset.setStatus(AssetStatus.PENDING);
			logger.info("Asset has set as Pending");
			assetRepository.save(asset);
		} else if (assetId == null && gatewayId != null) {
			Asset_Device_xref assetGatewayXref = assetDeviceXrefRepository.findByGatewayId(gatewayId);
			if(assetGatewayXref != null ) {
				Asset asset = assetGatewayXref.getAsset();
				asset.setStatus(AssetStatus.PENDING);
				assetRepository.save(asset);
				Device gateway = assetGatewayXref.getDevice();
				gateway.setAssetDeviceXref(null);
				gateway.setStatus(DeviceStatus.PENDING);
				logger.info("gateway status marked pending  " + gateway.getStatus());
				
				Logutils.log(className,methodName,context.getLogUUId(), " Before calling  deviceDeviceXrefRepository.findSensorByGatewayId method ", logger);
				List<Device_Device_xref> deviceSensorxreffList = deviceDeviceXrefRepository.findSensorByGatewayId(gatewayId);
				deviceSensorxreffList.forEach(sensor -> {
					Device d = sensor.getSensorUuid();
					d.setStatus(DeviceStatus.PENDING);
					deviceRepository.save(d);
				});
				
				gateway.setLastPerformAction(Constants.GATEWAYS_ACTION_UPDATED);
	            gateway.setTimeOfLastDownload(Instant.now());
				deviceRepository.save(gateway);
				if(assetGatewayXref != null) {
					assetDeviceXrefRepository.delete(assetGatewayXref);
				}
				logger.info("gateway object status marked pending after saving " + gateway.getStatus());
			}
			
			
			
		} else {
			throw new DeviceException("Both gateway ID and asset ID can't be null");
		}
		return true;
	}

	@Override
	public Boolean deleteAssetByAssetUuid(String assetUuid) {
//		logger.info("Inside the method deleteAssetByAssetUuid and UUID : " + assetUuid);
		
		String methodName = "deleteAssetByAssetUuid";
		Context context = new Context();
		Logutils.log(className, methodName, context.getLogUUId(), " Inside the method deleteAssetByAssetUuid and UUID :" + assetUuid , logger);
		
		Boolean isDeleted = false;
		if (assetUuid != null && !assetUuid.isEmpty()) {
			Asset asset = assetRepository.findByUuid(assetUuid);
			if (asset != null) {
				assetRepository.delete(asset);
				isDeleted = true;
				logger.info("Inside the method deleteAssetByAssetUuid and isDeleted : " + isDeleted);
			}
		}
		logger.info("Exiting from the method deleteAssetByAssetUuid and isDeleted : " + isDeleted);
		return isDeleted;
	}

	@Override
	public List<AssetDTO> getAssetsByAccountNumberAndStatusUsingDTO(String accountNumber, String status) {
		
		String methodName = "getAssetsByAccountNumberAndStatusUsingDTO";
		Context context = new Context();
		Logutils.log(className, methodName, context.getLogUUId(), " Inside getAssetsByAccountNumberAndStatusUsingDTO :" , logger);
		
		List<AssetDTO> assets = null;
		if (status != null && !status.isEmpty()) {
			assets = assetRepository.findByAccountNumberAndStatusUsingDTO(accountNumber, AssetStatus.getAssetStatus(status));
		} else {
			assets = assetRepository.findByAccountNumberUsingDTO(accountNumber);
		}
		Logutils.log(className, methodName, context.getLogUUId(), " Exiting getAssetsByAccountNumberAndStatusUsingDTO :" , logger);
		return assets;
	}
	
		@Override
	public Page<Device> getGatewaysByAccountNumberAndStatusWithPagination(String accountNumber, String status,
			Pageable pageable, List<String> cans) {
			
			String methodName = "getGatewaysByAccountNumberAndStatusWithPagination";
			Context context = new Context();
			Logutils.log(className, methodName, context.getLogUUId(), " Inside getGatewaysByAccountNumberAndStatusWithPagination :" , logger);
			
		logger.info(" accountNumber  " + accountNumber + " status: " + status + "cans: "+cans);
		Page<Device> gatewayList = null;
			if (accountNumber != null && !accountNumber.isEmpty()) {
				gatewayList = deviceRepository.findByAccountNumberWithPagination(accountNumber, pageable);
			} else {
				gatewayList = deviceRepository.findByListOfAccountNumberWithPagination(cans, pageable);

			}
			Logutils.log(className, methodName, context.getLogUUId(), " Exiting getGatewaysByAccountNumberAndStatusWithPagination :" , logger);
		return gatewayList;
	}
	
	@Override
	public String getLookupValue(String field) {
		
		String methodName = "getLookupValue";
		Context context = new Context();
		Logutils.log(className, methodName, context.getLogUUId(), " Inside getLookupValue :" , logger);
		
		List<Lookup> byField = lookupRepository.findByField(field);
		if (byField != null && byField.size() > 0) {
			return byField.get(0).getValue();
		}
		Logutils.log(className, methodName, context.getLogUUId(), " Exiting getLookupValue :" , logger);
		return "";
	}
	
	// --------------------------------Aamir 1 Start ---------------------------------------//
	
	@Override
	public List<Device> getSensorsForCan(String can) {
		
		List<Device> deviceList = deviceRepository.findByGatewayCan(can);
		return deviceList;
	}
	

	@Override
	public Device getGatewayByUuid(String uuid) {
		Device device = deviceRepository.findByUuid(uuid);
		return device;
	}
	
	
	@Override
	public List<Device> getGatewaysByAccountNumberAndStatus(String accountNumber, String status) {
		List<Device> device = null;
		if (status != null && !status.isEmpty()) {
			device= deviceRepository.findByAccountNumberAndStatus(accountNumber, DeviceStatus.valueOf(status));
		} else {
			device = deviceRepository.findByAccountNumber(accountNumber);
		}
		return device;
	}
	
	
	// --------------------------------Aamir 1 End ---------------------------------------//
	
	@Override
	public Device updateGatewayAssetStatus(UpdateGatewayAssetStatusRequest updateGatewayAssetStatusRequest) {
//		logger.info("service method for updating asset gateway status for finsih install");

		String methodName = "updateGatewayAssetStatus";
		Context context = new Context();
		Logutils.log(className, methodName, context.getLogUUId(),
				" service method for updating asset gateway status for finsih install :", logger);

		Device gateway = deviceRepository.findByUuid(updateGatewayAssetStatusRequest.getGatewayUuid());
		Logutils.log(className, " After calling deviceRepository.findByUuid method " + gateway.getUuid(), logger);

		if (gateway != null) {
			gateway.setStatus(updateGatewayAssetStatusRequest.getGatewayStatus());
			gateway = deviceRepository.save(gateway);
			logger.info("gateway status update for finish install " + gateway.getStatus());

			Asset asset = assetRepository.findByUuid(updateGatewayAssetStatusRequest.getAssetUuid());
			Logutils.log(className, " After calling assetRepository.findByUuid method " + asset.getUuid(), logger);

			if (asset != null) {
				asset.setStatus(updateGatewayAssetStatusRequest.getAssetStatus());
				assetRepository.save(asset);
			}
			logger.info("asset status update for finish install " + asset.getStatus());
			return gateway;
		}
		Logutils.log(className, methodName, context.getLogUUId(),
				"  Exiting service method for updating asset gateway status for finsih install :", logger);
		return null;
	}
	
	@Override
	public Device resetGateway(InstallationStatusGatewayRequest installationStatusGatewayRequest) {
//	    	logger.info("Inside UpdateGateway Status after finishing install");
		String methodName = "resetGateway";
		Context context = new Context();
		Logutils.log(className, methodName, context.getLogUUId(),
				" Inside UpdateGateway Status after finishing install :", logger);
		Device gateway = deviceRepository.resetByUuid(installationStatusGatewayRequest.getGatewayUuid());
		
		Logutils.log(className, methodName, context.getLogUUId(),
				" After calling deviceRepository.resetByUuid method " + gateway.getUuid(), logger);
		gateway.setLastPerformAction(Constants.GATEWAYS_ACTION_UPDATED);
		gateway.setTimeOfLastDownload(Instant.now());
		gateway = deviceRepository.save(gateway);
		Logutils.log(className, methodName, context.getLogUUId(),
				" After calling deviceRepository.save method " + gateway.getUuid(), logger);

		Logutils.log(className, methodName, context.getLogUUId(),
				" Exiting UpdateGateway Status after finishing install :", logger);
		return gateway;
	}
	  
	  public Boolean resetGateway(Device gateway) {
	    	gateway.setAssetDeviceXref(null);
	    	gateway.setLastPerformAction(Constants.GATEWAYS_ACTION_UPDATED);
	        gateway.setTimeOfLastDownload(Instant.now());
	    	gateway.setStatus(DeviceStatus.PENDING);
	    	   deviceRepository.save(gateway);
			   return true;
	    	
	    }
	  

		@Override
		public Page<Device> getGatewaysByAccountNumberAndStatusWithPaginationNew(String accountNumber, String status,
				Pageable pageable, List<String> cans, Instant lastDownloadeTime) {
			logger.info(" accountNumber  " + accountNumber + " status: " + status + "cans: " + cans);

			Page<Device> gatewayList = null;
			if (lastDownloadeTime != null && accountNumber != null && !accountNumber.isEmpty()) {
				gatewayList = deviceRepository.findByAccountNumberWithPagination(accountNumber, lastDownloadeTime,
						pageable);
			} else if (accountNumber != null && !accountNumber.isEmpty()) {
				gatewayList = deviceRepository.findByAccountNumberWithPagination(accountNumber, pageable);
			} else if (lastDownloadeTime != null) {
				gatewayList = deviceRepository.findByListOfAccountNumberWithPagination(cans, lastDownloadeTime,
						pageable);
			} else {
				gatewayList = deviceRepository.findByListOfAccountNumberWithPagination(cans, pageable);
			}

			return gatewayList;
		}
		
		@Override
		public Page<GatewayDetailsBean> getGatewaysByAccountNumberAndStatusWithPaginationV2(String accountNumber, String status,
				Pageable pageable, List<String> cans, Instant lastDownloadeTime) {
			logger.info(" accountNumber  " + accountNumber + " status: " + status + "cans: "+cans);
			Page<Device> gatewayList = null;
			List<GatewayDetailsBean> gatewatDetailList = new ArrayList<>();
			Page<GatewayDetailsBean> pageOfGatewayDetailsBean= null;
				if (lastDownloadeTime != null && accountNumber != null && !accountNumber.isEmpty()) {
					gatewayList = deviceRepository.findByAccountNumberWithPagination(accountNumber, lastDownloadeTime, pageable);
				} else if (accountNumber != null && !accountNumber.isEmpty()) {
					gatewayList = deviceRepository.findByAccountNumberWithPagination(accountNumber, pageable);
				} else if(lastDownloadeTime != null) {
					gatewayList = deviceRepository.findByListOfAccountNumberWithPagination(cans, lastDownloadeTime, pageable);
				} else {
					gatewayList = deviceRepository.findByListOfAccountNumberWithPagination(cans, pageable);
				}
				
				if (gatewayList.getContent() != null && gatewayList.getContent().size() > 0) {
					for (Device gat : gatewayList) {
						String productNameLog = "ProductName: " + gat.getProductName();
//						Logutils.log(className, methodName, context.getLogUUId(),
//								" Inside iterating Gateway LIst to gate product Name ", logger, productNameLog);
						String uuidLog = "Uuid: " + gat.getUuid();
						List<InstallInstructionBean> sensorInstallInstructions = new ArrayList<>();
						Map<String, List<ReasonCodeBean>> reasonCodeBeanMap = new HashMap<>();
						List<AttributeValueResposneDTO> attributeList = new ArrayList<>();
//						Logutils.log(className, methodName, context.getLogUUId(),
//								" Before calling restUtils.getAttributeListByProductName method ", logger, productNameLog,
//								uuidLog);
						Logutils.log(className, " Before calling getProductByName method ", logger);
						List<Attribute> attributes = productMasterService.getProductByName(gat.getProductName());
//						Logutils.log(className, methodName, context.getLogUUId(),
//								" after calling restUtils.getAttributeListByProductName method ", logger, productNameLog,
//								uuidLog);
						GatewayDetailsBean gatewatDetails = new GatewayDetailsBean();
						gatewatDetails.setGatewayUuid(gat.getUuid());
						gatewatDetails.setGatewayProductName(gat.getProductName());
						gatewatDetails.setGatewayProductCode(gat.getProductCode());
						// gatewatDetails.setEligibleAssetType(ins.getAsset().getGatewayEligibility());
						if (attributes.size() > 0) {
							gatewatDetails.set_blocker(attributes.get(0).getProductMaster().isBlocker());
							for (Attribute att : attributes) {
								AttributeValueResposneDTO attRes = new AttributeValueResposneDTO();
								attRes.setApplicable(att.isApplicable());
								attRes.setAttribute_uuid(att.getUuid());
								attRes.setAttributeName(att.getAttributeName());
								attRes.setThresholdValue(att.getAttributeValue());
								attributeList.add(attRes);
							}
						}
						gatewatDetails.setGatewayAttribute(attributeList);
//						Logutils.log(className, methodName, context.getLogUUId(),
//								" Before calling sensorInstallInstructionRepository.findBySensorProductName method ", logger,
//								productNameLog);

						Logutils.log(className, " Before calling restUtils.getSensorInstallInstruction method ", logger);
						List<SensorInstallInstruction> sensorInstallInstructionList = restUtils
								.getSensorInstallInstruction(gat.getProductName());
						Logutils.log(className, " After calling restUtils.getSensorInstallInstruction method ", logger);
						sensorInstallInstructionList.forEach(sensorInstallInstruction -> {
							InstallInstructionBean installInstructionBean = new InstallInstructionBean();
							installInstructionBean
									.setInstruction(sensorInstallInstruction.getInstallInstruction().getInstruction());
							installInstructionBean
									.setSequence(sensorInstallInstruction.getInstallInstruction().getStepSequence());
							sensorInstallInstructions.add(installInstructionBean);
						});
						InstallInstructionComparator comparator = new InstallInstructionComparator();
						Collections.sort(sensorInstallInstructions, comparator);
						gatewatDetails.setGatewayInstallInstructions(sensorInstallInstructions);
//						Logutils.log(className, methodName, context.getLogUUId(),
//								" Before calling sensorReasonCodeRepository.findBySensorProductName method ", logger,
//								productNameLog);

						Logutils.log(className, " Before calling restUtils.getSensorReasonCode method ", logger);
						List<SensorReasonCode> sensorReasonCodeList = restUtils
								.getSensorReasonCode(gat.getProductName());
						sensorReasonCodeList.forEach(sensorReasonCode -> {
							ReasonCodeBean reasonCodeBean = new ReasonCodeBean();
							reasonCodeBean.setReasonCode(sensorReasonCode.getReasonCode().getCode());
							reasonCodeBean.setDisplayName(sensorReasonCode.getReasonCode().getValue());
							if (reasonCodeBeanMap.get(sensorReasonCode.getReasonCode().getIssueType()) != null) {
								reasonCodeBeanMap.get(sensorReasonCode.getReasonCode().getIssueType()).add(reasonCodeBean);
							} else {
								List<ReasonCodeBean> reasonCodeBeanList = new ArrayList<>();
								reasonCodeBeanList.add(reasonCodeBean);
								reasonCodeBeanMap.put(sensorReasonCode.getReasonCode().getIssueType(), reasonCodeBeanList);
							}
						});
						gatewatDetails.setGatewayReasonCodes(reasonCodeBeanMap);
						gatewatDetailList.add(gatewatDetails);
					}
				 pageOfGatewayDetailsBean = new PageImpl<>(gatewatDetailList, pageable,gatewayList.getTotalElements());

					
				} else {
					throw new DeviceException("No Gateway for Given Input");
				}				
				
			return pageOfGatewayDetailsBean;
		}
		
	@Override
	public Boolean batchUpdateAssetDeviceDetails(BatchDeviceEditPayload batchDeviceEditPayload, String username) {
		try {
//			logger.info("Inside updateDeviceDetail and fetching assetDeviceDetail and userId value"
//					+ batchDeviceEditPayload + " " + username);
//			
			String methodName = "batchUpdateAssetDeviceDetails";
			Context context = new Context();
			Logutils.log(className, methodName, context.getLogUUId(), " Inside batchUpdateAssetDeviceDetails :"+ batchDeviceEditPayload + " " + username , logger);
			
			Logutils.log(className, " Before calling restUtils.getUserFromAuthService method :", logger);
			User user = restUtils.getUserFromAuthService(username);
			Logutils.log(className, " After calling restUtils.getUserFromAuthService method "+ user.getUserName(), logger);
			
			Organisation organisation = null;
			Organisation purchaseBy = null;
			Manufacturer manufacturer = null;
			Organisation installedBy = null;
			if (batchDeviceEditPayload.getCan() != null && !batchDeviceEditPayload.getCan().isEmpty()) {
				organisation = restUtils.getCompanyFromCompanyService(batchDeviceEditPayload.getCan());
				organisation.setCreatedBy(null);
				organisation.setUpdatedBy(null);
			}
			if (batchDeviceEditPayload.getInstalledBy() != null && !batchDeviceEditPayload.getInstalledBy().isEmpty()) {
				installedBy = restUtils.getCompanyFromCompanyService(batchDeviceEditPayload.getInstalledBy());
				installedBy.setCreatedBy(null);
				installedBy.setUpdatedBy(null);
			}
			if (batchDeviceEditPayload.getPurchaseBy() != null && !batchDeviceEditPayload.getPurchaseBy().isEmpty()) {
				purchaseBy = restUtils.getCompanyFromCompanyService(batchDeviceEditPayload.getPurchaseBy());
				purchaseBy.setCreatedBy(null);
				purchaseBy.setUpdatedBy(null);
			}
			if (batchDeviceEditPayload.getManufacturerName() != null
					&& !batchDeviceEditPayload.getManufacturerName().isEmpty()) {
				manufacturer = manufacturerRepository.findByName(batchDeviceEditPayload.getManufacturerName());
			}

			for (String imei : batchDeviceEditPayload.getImei()) {
				Device device = deviceRepository.findByImei(imei);
				if (device == null) {
					continue;
				}
				Cellular cellularPayload = device.getCellular();
				updateBatchDeviceAsset(device, batchDeviceEditPayload, organisation, username, manufacturer);
				if (device != null) {
					updateBatchDevice(device, batchDeviceEditPayload, organisation, user, purchaseBy, installedBy,
							cellularPayload);
				}

			}
			return Boolean.TRUE;
		} catch (Exception e) {
			e.printStackTrace();
			throw new DeviceException("Device details not Updated :-" + e.getMessage());
		}
	}

	private void updateBatchDevice(Device device, BatchDeviceEditPayload batchDeviceEditPayload,
			Organisation organisation, User user, Organisation purchaseBy, Organisation installedBy,
			Cellular cellularPayload) {
		String accountNumber = device.getOrganisation().getAccountNumber();
		device.setUsageStatus(batchDeviceEditPayload.getUsage_status());
//		DeviceDetails deviceDetails = new DeviceDetails();
//		deviceDetails.setUsageStatus(batchDeviceEditPayload.getUsage_status());
//		device.setDeviceDetails(deviceDetails);
		device.setProductCode(batchDeviceEditPayload.getProductCode());
		device.setProductName(batchDeviceEditPayload.getProductName());
		device.setUpdatedAt(Instant.now());
		device.setIsActive(batchDeviceEditPayload.getIsActive());
		device.setUpdatedBy(user);
		if (organisation != null) {
			device.setOrganisation(organisation);
		}
		if (purchaseBy != null) {
			device.setPurchaseBy(purchaseBy);
		}
		if (installedBy != null) {
			device.setInstalledBy(installedBy);
		}
		if (cellularPayload != null) {
			cellularPayload.setCountryCode(batchDeviceEditPayload.getCountryCode());
			cellularPayload.setServiceNetwork(batchDeviceEditPayload.getServiceNetwork());
			device.setCellular(cellularPayload);
		}
		logger.info("Before saving device asset details");
		deviceRepository.save(device);
		logger.info("device asset details saved successfully");

		try {
			if (!accountNumber.equalsIgnoreCase(organisation.getAccountNumber())) {
				updateDeviceReportCustomerOnElastic(device.getImei(), organisation.getAccountNumber());
				logger.info("Saving Device customer update History");
				updateDeviceCustomerHistory(accountNumber, device);
			}
		} catch (Exception ex) {
			logger.info("Exception while updating device customer history");
			ex.printStackTrace();
		}
		logger.info("Update Customer Id in redis");
		updateCustomerAndTypeInRedis(device.getImei(), device.getDeviceType(),
				device.getOrganisation().getAccountNumber());
		logger.info("Device update Successfully");
	}

	private void updateBatchDeviceAsset(Device device, BatchDeviceEditPayload batchDeviceEditPayload,
			Organisation organisation, String username, Manufacturer manufacturer) {
		Asset_Device_xref assetDeviceDetail = device.getAssetDeviceXref();
		if (assetDeviceDetail != null && assetDeviceDetail.getAsset() != null
				&& assetDeviceDetail.getAsset().getUuid() != null) {
			if (batchDeviceEditPayload.getManufacturerName() != null
					&& !batchDeviceEditPayload.getManufacturerName().isEmpty()
					|| batchDeviceEditPayload.getAssetType() != null
							&& !batchDeviceEditPayload.getAssetType().isEmpty()) {
//				AssetsPayload assetPayload2 = new AssetsPayload();
				Asset asset = assetDeviceDetail.getAsset();
				if (batchDeviceEditPayload.getAssetType() != null && !batchDeviceEditPayload.getAssetType().isEmpty()) {
//					assetPayload2.setCategory(AssetCategory.valueOf(batchDeviceEditPayload.getAssetType()).toString());
					asset.setCategory(AssetCategory.valueOf(batchDeviceEditPayload.getAssetType()));
				}
				asset.setYear(username);
				if (manufacturer != null) {
					asset.setManufacturer(manufacturer);
				}
//				assetPayload2.setManufacturer(batchDeviceEditPayload.getManufacturerName());
//				assetPayload2.setYear(batchDeviceEditPayload.getModelYear());
//				assetPayload2.setUuid(assetDeviceDetail.getAsset().getUuid());
//				assetPayload2.setId(assetDeviceDetail.getAsset().getId());
				if (organisation != null) {
					CompanyPayload companyPayload = new CompanyPayload();
					companyPayload.setId(organisation.getId());
					companyPayload.setUuid(organisation.getUuid());
					companyPayload.setAccountNumber(organisation.getAccountNumber());
					companyPayload.setCompanyName(organisation.getOrganisationName());
//					assetPayload2.setCompany(companyPayload);
					asset.setOrganisation(organisation);
				}

				try {
					assetRepository.save(asset);
//					assetService.updateAsset(assetPayload2, username);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

	}

	private void updateCustomerAndTypeInRedis(String imei, String deviceType, String accountNumber) {
		logger.info("Inside update device customer and type in redis method");
		redisDeviceRepository.addMap(DEVICE_CURRENT_VIEW_PREFIX + imei, "customerId", accountNumber);

		if (deviceType != null) {
			redisDeviceRepository.addMap(DEVICE_CURRENT_VIEW_PREFIX + imei, "deviceType", deviceType);
		}
	}

	private void updateDeviceCustomerHistory(String accountNumber, Device device) {
		try {
			logger.info("Inside update device customer history method");
			DeviceCustomerHistory deviceCustomerHistory = new DeviceCustomerHistory();
			deviceCustomerHistory.setAccountNumber(accountNumber);
			deviceCustomerHistory.setImei(device.getImei());
			deviceCustomerHistoryRepository.save(deviceCustomerHistory);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public DeviceResponsePayload getDeviceWithForwardingAndParentGroup(String imei) {
		logger.info("Inside the get device with forwarding and parent group method");

		
		Device device = deviceRepository.findByImei(imei);
		Logutils.log(className,
				" After calling deviceRepository.findByImei method " + device.getImei(), logger);
		
		if (AppUtility.isEmpty(device)) {
			throw new DeviceException("Device is not present");
		}

		DeviceResponsePayload deviceResponsePayload = beanConverter.convertDeviceDetailPayloadToDeviceBean(device);

		if (device.getPurchaseBy() != null) {
			deviceResponsePayload.setPurchaseByName(device.getPurchaseBy().getOrganisationName());
		}

		if (device.getOrganisation() != null && device.getOrganisation().getForwardingGroupMappers()!=null) {
			deviceResponsePayload.setForwardingGroup(device.getOrganisation().getForwardingGroupMappers().stream()
					.map(mapper -> mapper.getCustomerForwardingGroup().getName()).findFirst().orElse(null));
		}

		logger.info("successfully the get device with forwarding and parent group");

		return deviceResponsePayload;
	}

	@Override
	public DeviceResponsePayloadForAssetUpdate updateAssetForGatewayNew(String gatewayUuid, String assetUuid) throws Exception {
		
		String methodName = "updateAssetForGatewayNew";
		Context context = new Context();
		Logutils.log(className, methodName, context.getLogUUId(),
				" Inside updateAssetForGatewayNew:", logger);
		
		DeviceResponsePayloadForAssetUpdate deviceResponsePayloadForAssetUpdate = null;
		Device device = deviceRepository.findByUuid(gatewayUuid);
		Logutils.log(className,
				" After calling deviceRepository.findByUuid method " + device.getUuid(), logger);
		
		if (device != null) {
			Asset asset = assetRepository.findByUuid(assetUuid);
			if (asset != null) {
				asset.setStatus(AssetStatus.INSTALL_IN_PROGRESS);
				asset = assetRepository.save(asset);
				device.setStatus(DeviceStatus.INSTALL_IN_PROGRESS);
				device.setLastPerformAction(Constants.GATEWAYS_ACTION_UPDATED);
				device.setTimeOfLastDownload(Instant.now());
				device = deviceRepository.save(device);
				
				deviceResponsePayloadForAssetUpdate = new DeviceResponsePayloadForAssetUpdate();
				deviceResponsePayloadForAssetUpdate.setDeviceId(device.getId());
				deviceResponsePayloadForAssetUpdate.setDeviceUuid(device.getUuid());
				deviceResponsePayloadForAssetUpdate.setImei(device.getImei());
				deviceResponsePayloadForAssetUpdate.setOrganisationId(device.getOrganisation() != null ? device.getOrganisation().getId() : null);
				deviceResponsePayloadForAssetUpdate.setOrganisationUuid(device.getOrganisation() != null ? device.getOrganisation().getUuid() : null);
				deviceResponsePayloadForAssetUpdate.setOrganisationAccNo(device.getOrganisation() != null ? device.getOrganisation().getAccountNumber() : null);
			} else {
				throw new Exception("Asset found null for Id = " + assetUuid);
			}
		} else {
			throw new Exception("Gateway found null for Id = " + gatewayUuid);
		}
		
		return deviceResponsePayloadForAssetUpdate;
	}
	
	class InstallInstructionComparator implements Comparator<InstallInstructionBean> {
		@Override
		public int compare(InstallInstructionBean x, InstallInstructionBean y) {
			int seqComparison = compare(x.getSequence(), y.getSequence());
			return seqComparison;
		}

		private int compare(int a, int b) {
			return a < b ? -1 : a > b ? 1 : 0;
		}
	}
	
	public DeviceReportDTO getLatestReportFromElastic(String deviceId) throws IOException {
		String methodName = "getLatestReportFromElastic";
		Context context = new Context();
		String messageUUID = context.getLogUUId();
		Logutils.log(className, methodName, messageUUID, " Inside getLatestReportFromElastic:", logger);
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		SearchRequest searchRequest = new SearchRequest(index);
		searchSourceBuilder.query(QueryBuilders.matchQuery("report_header.device_id", deviceId)).size(1)
				.sort(generalMaskFieldRecTimeStamp, SortOrder.DESC);
		searchRequest.source(searchSourceBuilder);
		logger.info("MessageUUID : " + messageUUID + " starting fetching details from ES  ");
		SearchResponse searchResponse = template.search(searchRequest, RequestOptions.DEFAULT);
		logger.info("MessageUUID : " + messageUUID + " completed fetching details from ES  ");
		SearchHit[] hits = searchResponse.getHits().getHits();
		DeviceReportDTO deviceReportDTO = new DeviceReportDTO();
		logger.info("MessageUUID : " + messageUUID + " size is " + hits.length);
		if (hits.length != 0) {
			SearchHit searchHit = Arrays.asList(hits).get(0);
			deviceReportDTO = createDeviceReport(searchHit, messageUUID);
		}
		return deviceReportDTO;

	}

	private DeviceReportDTO createDeviceReport(SearchHit e, String messageUUID) {
		String string = e.getSourceAsString();
		JSONObject json = new JSONObject(string);
		JSONObject getGeneral = json.getJSONObject("general");
		String rawReport = (String) getGeneral.get("rawreport");
		JSONObject generalMaskFields = json.getJSONObject("general_mask_fields");
		String receivedTimeStamp = (String) generalMaskFields.get("received_time_stamp");
		java.util.Date date;
		DeviceReportDTO deviceReport = new DeviceReportDTO();
		deviceReport.setRawReport(rawReport);
		try {
//			date = simpleDateTimeFormatPST.parse(receivedTimeStamp);
//			DateFormat simpleDateFormatPST = new SimpleDateFormat("MM/dd/yyyy");
//			deviceReport.setDateReceived(simpleDateFormatPST.format(date));
//			DateFormat simpleTimeFormatPST = new SimpleDateFormat("HH:mm:ss");
//			deviceReport.setTimeReceived(simpleTimeFormatPST.format(date));
			SimpleDateFormat formatDate = new SimpleDateFormat("yyyy-MM-dd"); 
			Date getReceivedDate = formatDate.parse(receivedTimeStamp);
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy");
			String receivedDate = simpleDateFormat.format(getReceivedDate);
			deviceReport.setDateReceived(receivedDate);
			
			SimpleDateFormat formatHours  = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
			Date getHourMinSec = formatHours.parse(receivedTimeStamp);
			SimpleDateFormat simpleDateFormatHr = new SimpleDateFormat("HH:mm:ss"); 
			String receivedHourMinSec = simpleDateFormatHr.format(getHourMinSec); 
			deviceReport.setTimeReceived(receivedHourMinSec);
		} catch (ParseException e1) {
			logger.info("MessageUUID : " + messageUUID + " exception due to " + e1.getMessage());
			e1.printStackTrace();
		}
		return deviceReport;
	}

	public List<DeviceReportDTO> getReportsByDateRange(String deviceId, Integer from, Integer size, String fromDate, String toDate)
			throws IOException {
		String methodName = "getReportsByDateRange";
		Context context = new Context();
		String messageUUID = context.getLogUUId();
		Logutils.log(className, methodName, messageUUID, " Inside getReportsByDateRange:", logger);
		
		int limit = 3000;
		
		if (from == null) {
			from = 0;
		}
		if (size != null) {
			limit = size;
		}
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
		boolQueryBuilder.must(QueryBuilders.matchQuery(reportHeaderDevId, deviceId))
				.must(QueryBuilders.rangeQuery(generalMaskFieldRecTimeStamp).gt(fromDate + ".000000000")
						.lt(toDate + ".000000000").includeUpper(true));
		SearchRequest searchRequest = new SearchRequest(index);
		searchSourceBuilder.from(from);
		searchSourceBuilder.size(limit);
		searchSourceBuilder.query(boolQueryBuilder);
		searchSourceBuilder.sort(generalMaskFieldRecTimeStamp, SortOrder.DESC);
		searchRequest.source(searchSourceBuilder);
		logger.info("MessageUUID : " + messageUUID + " starting fetching details from ES  ");
		SearchResponse searchResponse = template.search(searchRequest, RequestOptions.DEFAULT);
		logger.info("MessageUUID : " + messageUUID + " completed fetching details from ES  ");
		SearchHit[] searchHits = searchResponse.getHits().getHits();
		logger.info("MessageUUID : " + messageUUID + " total records returned are " + searchHits.length);
		List<DeviceReportDTO> deviceReports = new ArrayList<>();
		deviceReports = createDeviceReports(searchHits, messageUUID);
		return deviceReports;
	}
	
	private List<DeviceReportDTO> createDeviceReports(SearchHit[] searchHits, String messageUUID) {
		List<DeviceReportDTO> deviceReports = new ArrayList<>();
		for (SearchHit searchHit : searchHits) {
			String string = searchHit.getSourceAsString();
			JSONObject json = new JSONObject(string);
			JSONObject getGeneral = json.getJSONObject("general");
			String rawReport = (String) getGeneral.get("rawreport");
			JSONObject generalMaskFields = json.getJSONObject("general_mask_fields");
			String receivedTimeStamp = (String) generalMaskFields.get("received_time_stamp");
			java.util.Date date;
			DeviceReportDTO deviceReport = new DeviceReportDTO();
			deviceReport.setRawReport(rawReport);
			try {
//				date = simpleDateTimeFormatPST.parse(receivedTimeStamp);
//				DateFormat simpleDateFormatPST = new SimpleDateFormat("MM/dd/yyyy");
//				deviceReport.setDateReceived(simpleDateFormatPST.format(date));
//				DateFormat simpleTimeFormatPST = new SimpleDateFormat("HH:mm:ss");
//				deviceReport.setTimeReceived(simpleTimeFormatPST.format(date));
//				deviceReports.add(deviceReport);
				SimpleDateFormat formatDate = new SimpleDateFormat("yyyy-MM-dd"); 
				Date getReceivedDate = formatDate.parse(receivedTimeStamp);
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy");
				String receivedDate = simpleDateFormat.format(getReceivedDate);
				deviceReport.setDateReceived(receivedDate);
				
				SimpleDateFormat formatHours  = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
				Date getHourMinSec = formatHours.parse(receivedTimeStamp);
				SimpleDateFormat simpleDateFormatHr = new SimpleDateFormat("HH:mm:ss"); 
				String receivedHourMinSec = simpleDateFormatHr.format(getHourMinSec); 
				deviceReport.setTimeReceived(receivedHourMinSec);
				deviceReports.add(deviceReport);
			} catch (ParseException e1) {
				logger.info("MessageUUID : " + messageUUID + " exception due to " + e1.getMessage());
				e1.printStackTrace();
			}
		}
		return deviceReports;
	}
	
	
	public String getDeviceStatusFromDB(String deviceId, String messageUUID) throws Exception {
		try {
			logger.info("MessageUUID : " + messageUUID + " started fetching details from Redis for deviceId  " + deviceId);
			Map<String, String> reportRowMap = redisDeviceRepository.getValue("device360:" + deviceId);
			StringBuffer sb = new StringBuffer();
			reportRowMap.forEach((k, v) -> sb.append(k + " - " + v + "\r"));
			if(!reportRowMap.isEmpty()) {
				logger.info("MessageUUID : " + messageUUID + " reportRowMap size is  " + reportRowMap.size());
				Gson gson = new Gson();
				logger.info("MessageUUID : " + messageUUID + " getting value for key device360:" + deviceId);
				String reportRowMapValue = reportRowMap.get("device360:" + deviceId);
				logger.info("MessageUUID : " + messageUUID + " value is " + reportRowMapValue);
				Map<String, Object> flattenAsMap = new JsonFlattener(reportRowMapValue).flattenAsMap();
				logger.info("MessageUUID : " + messageUUID + " map is " + flattenAsMap);
				String deviceStatus = gson.toJson(flattenAsMap);
				logger.info("MessageUUID : " + messageUUID + " deviceStatus value is  " + deviceStatus);
				logger.info("MessageUUID : " + messageUUID + " completed fetching details from Redis for deviceId  " + deviceId);
				return deviceStatus;
			} else {
				return "ERROR: couldn't fine set for key: " + deviceId;
			}
		}catch(Exception e) {
			logger.error("MessageUUID : " + messageUUID + " exception due to " + e.getMessage());
			return "Error in fetching data";
		}
	}
}
