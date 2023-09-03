package com.pct.device.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.elasticsearch.search.SearchHit;
import org.json.CDL;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.supercsv.io.CsvBeanWriter;
import org.supercsv.io.ICsvBeanWriter;
import org.supercsv.prefs.CsvPreference;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.shared.Application;
import com.pct.common.dto.CustomerForwardingRuleDTO;
import com.pct.common.dto.CustomerForwardingRuleUrlDTO;
//import com.pct.common.constant.OrganisationType;
import com.pct.common.dto.ResponseBodyDTO;
import com.pct.common.dto.ResponseDTO;
import com.pct.common.model.Organisation;
import com.pct.common.model.SensorInstallInstruction;
import com.pct.common.model.SensorReasonCode;
import com.pct.common.model.User;
import com.pct.common.payload.DeviceATCommandReqResponse;
import com.pct.device.Bean.ShippedDevicesHubRequest;
import com.pct.device.dto.CampaignHistoryDeviceResponse;
import com.pct.device.dto.CampaignHistoryPayloadResponse;
import com.pct.device.dto.CampaignInstallDeviceResponse;
import com.pct.device.dto.CurrentCampaignResponse;
import com.pct.device.dto.GatewayCommandResponse;
import com.pct.device.dto.MessageDTO;
import com.pct.device.dto.TpmsSensorCountDTO;
import com.pct.device.exception.InterServiceRestException;
import com.pct.device.feing.client.InstallerServiceFeignClient;
import com.pct.device.model.Event;
import com.pct.device.payload.ATCommandRequestPayload;
import com.pct.device.payload.DeviceCommandRequestPayload;
import com.pct.device.payload.DeviceReportPayload;
import com.pct.device.payload.DeviceResponsePayload;
import com.pct.device.payload.GatewaySummaryPayload;
import com.pct.device.payload.InstalledHistoryResponsePayload;

@Component
public class RestUtils {

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	InstallerServiceFeignClient installerServiceFeignClient;

	@Autowired
	private EurekaClient eurekaClient;

	@Value("${service.gateway.serviceId}")
	private String deviceServiceId;

	@Value("${base.url}")
	private String baseUrl;

	@Value("${atcommand.url}")
	private String atcommandUrl;

	@Value("${hub.endpoint.shipment}")
	private String hubShipmentEndpoint;

	@Autowired
	MailUtil mailUtil;

	private static final DecimalFormat DECFOR = new DecimalFormat("0.00");

	private final static String TOKEN = "Bearer eyJhbGciOiJIUzUxMiJ9.eyJzY29wZXMiOlsiU3VwZXJhZG1pbiJdLCJzdWIiOjE5LCJjb21wYW55SWQiOjEsImlhdCI6MTY0ODIxMjQ5MiwiZXhwIjoxNjQ4Mjk4ODkyfQ.7_XYAnWvtmpOOPV149sU5ZiT7E9r_1oEplTlRzoCr8r_4tBciuJGLOt_lxTCYERrpE6-jG-QR6SXvO_bYJjiSw";

	private final ObjectMapper objectMapper = new ObjectMapper();
	Logger logger = LoggerFactory.getLogger(RestUtils.class);

	public User getUserFromAuthService(Long userId) {
		try {
			logger.info(
					"Inside RestUtils Class getUserFromAuthService method and fetching user details and userId value",
					userId);
			Application application = eurekaClient.getApplication(deviceServiceId);
			InstanceInfo instanceInfo = application.getInstances().get(0);
			String url = "http://" + instanceInfo.getIPAddr() + ":" + instanceInfo.getPort() + "/user/core/" + userId;
			ResponseEntity<MessageDTO> userResponseEntity = restTemplate.getForEntity(url, MessageDTO.class);
			if (userResponseEntity.getStatusCode().equals(HttpStatus.OK)) {
				User user = objectMapper.convertValue(userResponseEntity.getBody().getBody(), User.class);
				return user;
			} else {
				logger.error("Error Inside RestUtils Class getUserFromAuthService method :- Http call not successful");
				throw new InterServiceRestException("Http call not successful", userResponseEntity.getStatusCode());
			}
		} catch (Exception ex) {
			logger.error("Exception Inside RestUtils Class getUserFromAuthService method", ex);
			throw new InterServiceRestException(
					"Exception while calling the http call Get User From Auth Servic, Exception:-" + ex.getMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	public User getUserFromAuthService(String userName) {
		try {
			logger.info(
					"Inside RestUtils Class getUserFromAuthService method and fetching user details and userName value",
					userName);
			Application application = eurekaClient.getApplication(deviceServiceId);
			InstanceInfo instanceInfo = application.getInstances().get(0);
			String token = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest()
					.getHeader("Authorization");
			String url = "http://" + instanceInfo.getIPAddr() + ":" + instanceInfo.getPort()
					+ "/user/username?user_name=" + userName;
			HttpHeaders headers = new HttpHeaders();
			headers.setAccept(Arrays.asList(MediaType.ALL));
			headers.set("Authorization", token);
			HttpEntity<String> entity = new HttpEntity<String>(headers);
			ResponseEntity<User> userResponseEntity = restTemplate.exchange(url, HttpMethod.GET, entity, User.class);
			if (userResponseEntity.getStatusCode().equals(HttpStatus.OK)) {
				return userResponseEntity.getBody();
			} else {
				logger.error("Error Inside RestUtils Class getUserFromAuthService method :- Http call not successful");
				throw new InterServiceRestException("Http call not successful", userResponseEntity.getStatusCode());
			}
		} catch (Exception ex) {
			logger.error("Exception Inside RestUtils Class getUserFromAuthService method", ex);
			throw new InterServiceRestException(
					"Exception while calling the http call get user by username, Exception:-" + ex.getMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	public User getUserFromAuthServiceWithToken(String userName, String token) {
		try {
			logger.info(
					"Inside RestUtils Class getUserFromAuthService method and fetching user details and userName value",
					userName);
			Application application = eurekaClient.getApplication(deviceServiceId);
			InstanceInfo instanceInfo = application.getInstances().get(0);
//			String token = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest()
//					.getHeader("Authorization");
			String url = "http://" + instanceInfo.getIPAddr() + ":" + instanceInfo.getPort()
					+ "/user/username?user_name=" + userName;
			HttpHeaders headers = new HttpHeaders();
			headers.setAccept(Arrays.asList(MediaType.ALL));
			headers.set("Authorization", token);
			HttpEntity<String> entity = new HttpEntity<String>(headers);
			ResponseEntity<User> userResponseEntity = restTemplate.exchange(url, HttpMethod.GET, entity, User.class);
			if (userResponseEntity.getStatusCode().equals(HttpStatus.OK)) {
				return userResponseEntity.getBody();
			} else {
				logger.error("Error Inside RestUtils Class getUserFromAuthService method :- Http call not successful");
				throw new InterServiceRestException("Http call not successful", userResponseEntity.getStatusCode());
			}
		} catch (Exception ex) {
			logger.error("Exception Inside RestUtils Class getUserFromAuthService method", ex);
			throw new InterServiceRestException(
					"Exception while calling the http call get user by username, Exception:-" + ex.getMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	public Organisation getCompanyFromCompanyService(String accountNumber) {
		try {
			logger.info(
					"Inside RestUtils Class getCompanyFromCompanyService method and fetching Company Details details and accountNumber value",
					accountNumber);
			Application application = eurekaClient.getApplication(deviceServiceId);
			InstanceInfo instanceInfo = application.getInstances().get(0);
			String token = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest()
					.getHeader("Authorization");
			String url = "http://" + instanceInfo.getIPAddr() + ":" + instanceInfo.getPort()
					+ "/customer/core?accountNumber=" + accountNumber;
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.set("Authorization", token);
			HttpEntity<String> entity = new HttpEntity<String>(headers);
			ResponseEntity<Organisation> companyResponseEntity = restTemplate.exchange(url, HttpMethod.GET, entity,
					Organisation.class);
			if (companyResponseEntity.getStatusCode().equals(HttpStatus.OK)) {
				Organisation company = companyResponseEntity.getBody();
				return company;
			} else {
				logger.error(
						"Error Inside RestUtils Class getCompanyFromCompanyService method :- Http call not successful");
				throw new InterServiceRestException("Http call not successful", companyResponseEntity.getStatusCode());
			}
		} catch (Exception ex) {
			logger.error("Exception Inside RestUtils Class getCompanyFromCompanyService method", ex);
			throw new InterServiceRestException(
					"Exception while calling the http call get company by account number, Exception:-"
							+ ex.getMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	public List<String> getSuperAdminUser() {
		try {
			logger.info("Inside RestUtils Class getSuperAdminUser method and fetching user details");
			Application application = eurekaClient.getApplication(deviceServiceId);
			InstanceInfo instanceInfo = application.getInstances().get(0);
			String token = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest()
					.getHeader("Authorization");
			String url = "http://" + instanceInfo.getIPAddr() + ":" + instanceInfo.getPort() + "/user/core/adminUser";
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.set("Authorization", token);
			HttpEntity<String> entity = new HttpEntity<String>(headers);
			ResponseEntity<MessageDTO> userResponseEntity = restTemplate.exchange(url, HttpMethod.GET, entity,
					MessageDTO.class);
			if (userResponseEntity.getStatusCode().equals(HttpStatus.OK)) {
				List<String> user = (List<String>) objectMapper.convertValue(userResponseEntity.getBody().getBody(),
						List.class);
				return user;
			} else {
				logger.error("Error Inside RestUtils Class getSuperAdminUser method :- Http call not successful");
				throw new InterServiceRestException("Http call not successful", userResponseEntity.getStatusCode());
			}
		} catch (Exception ex) {
			logger.error("Exception Inside RestUtils Class getSuperAdminUser method", ex);
			throw new InterServiceRestException(
					"Exception while calling the http call get user/core/adminUser, Exception:-" + ex.getMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	public Organisation getCompanyFromCompanyServiceById(Long id) {
		try {
			logger.info(
					"Inside RestUtils Class getCompanyFromCompanyServiceById method and fetching company details and id value",
					id);
			Application application = eurekaClient.getApplication(deviceServiceId);
			InstanceInfo instanceInfo = application.getInstances().get(0);
			String token = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest()
					.getHeader("Authorization");
			String url = "http://" + instanceInfo.getIPAddr() + ":" + instanceInfo.getPort() + "/customer/core/id/"
					+ id;
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.set("Authorization", "Bearer " + token);
			HttpEntity<String> entity = new HttpEntity<String>(headers);
			ResponseEntity<Organisation> companyResponseEntity = restTemplate.exchange(url, HttpMethod.GET, entity,
					Organisation.class);
			if (companyResponseEntity.getStatusCode().equals(HttpStatus.OK)) {
				Organisation company = companyResponseEntity.getBody();
				return company;
			} else {
				logger.error(
						"Error Inside RestUtils Class getCompanyFromCompanyServiceById method :- Http call not successful");
				throw new InterServiceRestException("Http call not successful", companyResponseEntity.getStatusCode());
			}
		} catch (Exception ex) {
			logger.error("Exception Inside RestUtils Class getCompanyFromCompanyServiceById method", ex);
			throw new InterServiceRestException(
					"Exception while calling the http call get company by id, Exception:-" + ex.getMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	public Boolean deleteInstallDataForCompany(String companyUuid) {
		try {
			logger.info(
					"Inside RestUtils Class deleteInstallDataForCompany method and delete installed data fro company and company uuid value",
					companyUuid);
			Application application = eurekaClient.getApplication(deviceServiceId);
			InstanceInfo instanceInfo = application.getInstances().get(0);
			String url = "http://" + instanceInfo.getIPAddr() + ":" + instanceInfo.getPort()
					+ "/installation/core/reset-company?company_uuid=" + companyUuid;
			ResponseEntity<ResponseDTO> responseEntity = restTemplate.postForEntity(url, null, ResponseDTO.class);
			if (responseEntity.getStatusCode().equals(HttpStatus.OK)) {
				Boolean status = responseEntity.getBody().getStatus();
				return status;
			} else {
				logger.error(
						"Error Inside RestUtils Class deleteInstallDataForCompany method :- Http call not successful");

				throw new InterServiceRestException("Http call not successful", responseEntity.getStatusCode());
			}
		} catch (Exception ex) {
			logger.error("Exception Inside RestUtils Class deleteInstallDataForCompany method", ex);
			throw new InterServiceRestException(
					"Exception while calling the http call Delete install data for company, Exception:-"
							+ ex.getMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	public Boolean deleteInstallDataForGateway(String gatewayUuid) {
		try {
			logger.info(
					"Inside RestUtils Class deleteInstallDataForGateway method and delete installed data for gateway and company gatewayUuid value",
					gatewayUuid);

			Application application = eurekaClient.getApplication(deviceServiceId);
			InstanceInfo instanceInfo = application.getInstances().get(0);
			String url = "http://" + instanceInfo.getIPAddr() + ":" + instanceInfo.getPort()
					+ "/installation/core/reset-gateway?gateway_uuid=" + gatewayUuid;
			ResponseEntity<ResponseDTO> responseEntity = restTemplate.postForEntity(url, null, ResponseDTO.class);
			if (responseEntity.getStatusCode().equals(HttpStatus.OK)) {
				Boolean status = responseEntity.getBody().getStatus();
				return status;
			} else {
				logger.error(
						"Error Inside RestUtils Class deleteInstallDataForGateway method :- Http call not successful");
				throw new InterServiceRestException("Http call not successful", responseEntity.getStatusCode());
			}
		} catch (Exception ex) {
			logger.error("Exception Inside RestUtils Class deleteInstallDataForGateway method", ex);
			throw new InterServiceRestException(
					"Exception while calling the http call delete Install Data for gateway, Exception:-"
							+ ex.getMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	public Boolean sendShippedDevicesToHub(ShippedDevicesHubRequest shippedDevicesHubRequest) {
		ResponseEntity<Object> responseEntity = restTemplate.postForEntity(hubShipmentEndpoint,
				shippedDevicesHubRequest, Object.class);
		if (responseEntity.getStatusCode().equals(HttpStatus.OK)) {
			return true;
		}
		return false;
	}

//    public List<InstallHistory> getInstallHistoryListForAssetUuids(List<String> assetUuids,Map<String, String> filterValues) {
//        Application application = eurekaClient.getApplication(deviceServiceId);
//        InstanceInfo instanceInfo = application.getInstances().get(0);
//        GetInstallHistoryByAssetUuids request = new GetInstallHistoryByAssetUuids();
//        request.setAssetUuids(assetUuids);
//        request.setFilterValues(filterValues);
//        String url = "http://" + instanceInfo.getIPAddr() + ":" + instanceInfo.getPort() + "/installation/core/asset";
//        ResponseEntity responseEntity = restTemplate.postForEntity(url, request, List.class);
//        if (responseEntity.getStatusCode().equals(HttpStatus.OK)) {
//            return (List<InstallHistory>) ((List) responseEntity.getBody()).stream().map(ih ->
//                    objectMapper.convertValue(ih, InstallHistory.class)).collect(Collectors.toList());
//        } else {
//            throw new InterServiceRestException
//                    ("Http call not successful", responseEntity.getStatusCode());
//        }
//    }

	public Integer getSensorCount(String uuid) {
		try {
			logger.info("Inside RestUtils Class getSensorCount method and get sensor count and company uuid value",
					uuid);

			Application application = eurekaClient.getApplication(deviceServiceId);
			InstanceInfo instanceInfo = application.getInstances().get(0);
			String url = "http://" + instanceInfo.getIPAddr() + ":" + instanceInfo.getPort()
					+ "/installation/core/sensor-count?sensor_uuid=" + uuid;
			ResponseEntity<TpmsSensorCountDTO> count = restTemplate.getForEntity(url, TpmsSensorCountDTO.class);
			if (count.getStatusCode().equals(HttpStatus.OK)) {
				TpmsSensorCountDTO counts = count.getBody();
				return counts.getCount();
			} else {
				logger.error("Error Inside RestUtils Class getSensorCount method :- Http call not successful");
				throw new InterServiceRestException("Http call not successful", count.getStatusCode());
			}
		} catch (Exception ex) {
			logger.error("Exception Inside RestUtils Class getSensorCount method", ex);
			throw new InterServiceRestException(
					"Exception while calling the http call get /installation/core/sensor-count?sensor_uuid=" + uuid
							+ ", Exception:-" + ex.getMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	public void writeFilterCSVFile(String csvFileName, List<Page<DeviceResponsePayload>> gatewayDetailPayLoadList,
			HttpServletResponse response, User user, int totalSize, List<String> columnDef) throws IOException {
		response.reset();
		Random rnd = new Random();
		int number = rnd.nextInt(999999);
		File theDir = new File("/files");
		if (!theDir.exists()) {
			theDir.mkdirs();
		}

//		String[] header = new String[] { "DeviceId", "Customer", "AssetName", "AssetType", "InstalledDate", "Vin", "Manufacturer",
//				"Status", "Order", "ProductName", "Battery",  "LastReportDate",
//				"EventId", "EventType","Lat","Longitude", "QaStatus", "QaDates", "UsageStatus", "ServiceNetwork", "Cellular",
//				"ServiceCountry", "Phone", "Type1", "Url1", "Type2", "Url2", "Type3", "Url3", "Type4", "Url4",
//				"BinVersion", "AppVersion", "McuVersion", "BleVersion", "Config1Name", "Config1CRC", "Config2Name",
//				"Config2CRC", "Config3Name", "Config3CRC", "Config4Name", "Config4CRC", "DevuserCfgName",
//				"DevuserCfgValue", "ProductCode"  };
		String[] header = columnDef.toArray(new String[0]);
		if (totalSize > 10000) {
			ICsvBeanWriter csvWriter = new CsvBeanWriter(new FileWriter("files/alldeviceList" + number + ".csv"),
					CsvPreference.STANDARD_PREFERENCE);
			if (user != null) {
				csvWriter.writeHeader(header);
				logger.info("Start writing a data into csv file");
				for (Iterator<Page<DeviceResponsePayload>> iterator = gatewayDetailPayLoadList.iterator(); iterator
						.hasNext();) {
					Page<DeviceResponsePayload> page = (Page<DeviceResponsePayload>) iterator.next();
					for (DeviceResponsePayload gatewayDetailPayLoad : page) {
						csvWriter.write(gatewayDetailPayLoad, header);
					}
				}
				csvWriter.close();
			}

			logger.info("alldeviceList" + number + ".csv" + " file created successfully");
			if (user != null) {
				mailUtil.sendMail(user, "files/alldeviceList" + number + ".csv", true,
						"alldeviceList" + number + ".csv");
			}
		} else {
			ICsvBeanWriter csvWriter2 = new CsvBeanWriter(response.getWriter(), CsvPreference.STANDARD_PREFERENCE);
			csvWriter2.writeHeader(header);
			for (Iterator<Page<DeviceResponsePayload>> iterator = gatewayDetailPayLoadList.iterator(); iterator
					.hasNext();) {
				Page<DeviceResponsePayload> page = (Page<DeviceResponsePayload>) iterator.next();
				for (DeviceResponsePayload gatewayDetailPayLoad : page) {
					csvWriter2.write(gatewayDetailPayLoad, header);
				}
			}
			csvWriter2.close();
		}

	}

	public List<String> setHeader(List<String> header) {

		header.set(0, "OrderNo");
		header.set(1, "CompanyName");
		header.set(2, "Imei");
		header.set(3, "ProductCode");
		header.set(4, "Status");
		header.set(5, "PctCargoSensor");
		header.set(6, "MicroSpTransceiver");
		header.set(7, "MicroSpTPMS");
		header.set(8, "MicroSpTPMSInner");
		header.set(9, "MicroSpTPMSOuter");
		header.set(10, "MicroSpAirTank");
		header.set(11, "MicroSpWiredReceiver");
		header.set(12, "MicroSpATISRegulator");
		header.set(13, "AirTank");
		header.set(14, "AbsSensor");
		header.set(15, "AtisSensor");
		header.set(16, "CargoSensor");
		header.set(17, "DoorSensor");
		header.set(18, "LampCheckABS");
		header.set(19, "LampCheckAtis");
		header.set(20, "LightSentry");
		header.set(21, "Regulator");
		header.set(22, "Tpms");
		header.set(23, "WheelEnd");
		header.set(24, "PctCargoCameraG1");
		header.set(25, "PctCargoCameraG2");
		header.set(26, "PctCargoCameraG3");
		header.set(27, "CreatedBy");
		header.set(28, "UpdatedBy");

		return header;
	}

	public String getParsedReport(String rawReport, String format, String type) {
		try {
			logger.info(
					"Inside RestUtils Class getParsedReport method and get parsed report and company rawReport, formate,type value",
					rawReport + "," + format + "," + type);

			String url = null;
			if (type.contains("report")) {
				url = "http://100.24.214.108:9998/gateway/parse-report?raw_report=" + rawReport + "&format=" + format;
			} else if (type.contains("tlv")) {
				url = "http://100.24.214.108:9998/gateway/parse-tlv?tlv=" + rawReport + "&format=" + format;
			}
			ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
			if (response.getStatusCode().equals(HttpStatus.OK)) {
				return response.getBody();
			} else {
				logger.error("Error Inside RestUtils Class getParsedReport method :- Http call not successful");
				throw new InterServiceRestException("Http call not successful", response.getStatusCode());
			}
		} catch (Exception ex) {
			logger.error("Exception Inside RestUtils Class getParsedReport method", ex);
			throw new InterServiceRestException(
					"Exception while calling the http call Get Parsed report, Exception:-" + ex.getMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	public List<GatewayCommandResponse> getATCQueueRequest(String deviceId) {
		try {
			logger.info(
					"Inside RestUtils Class getATCQueueRequest method and get at command request and company deviceId value",
					deviceId);
			Application application = eurekaClient.getApplication(deviceServiceId);
			InstanceInfo instanceInfo = application.getInstances().get(0);
			String url = "http://" + instanceInfo.getIPAddr() + ":" + instanceInfo.getPort()
					+ "/gateway-command/queued-atc-request?gateway_Id=" + deviceId;
			String token = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest()
					.getHeader("Authorization");
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.set("Authorization", token);
			HttpEntity<String> entity = new HttpEntity<String>(headers);
			ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
			List<GatewayCommandResponse> gatewaycommands = new ArrayList<GatewayCommandResponse>();
			if (response.getStatusCode().equals(HttpStatus.OK)) {
				String res = response.getBody();
				if (res != null) {
					JSONObject json = new JSONObject(res);
					if (!json.get("body").equals(null)) {
						System.out.println("priting " + json.get("body"));
						JSONObject que = (JSONObject) json.get("body");
						if (que != null && que.get("gateway_command") != null) {
							JSONArray array = (JSONArray) que.get("gateway_command");
							for (int i = 0; i < array.length(); i++) {
								GatewayCommandResponse gatewaycommand = new GatewayCommandResponse();
								JSONObject object = array.getJSONObject(i);
								gatewaycommand.setAt_command(object.getString("at_command"));
								gatewaycommand.setPriority(object.getInt("priority"));
								gatewaycommand.setCreated_epoch(Instant
										.ofEpochMilli(Long.parseLong(object.getString("created_epoch"))).toString());
								gatewaycommand.setUuid(object.getString("uuid"));
								gatewaycommands.add(gatewaycommand);
							}
						}
					}
				}
				return gatewaycommands;
			} else {
				logger.error("Error Inside RestUtils Class getATCQueueRequest method :- Http call not successful");
				throw new InterServiceRestException("Http call not successful", response.getStatusCode());
			}
		} catch (RestClientException e) {
			logger.error("Exception Inside RestUtils Class getATCQueueRequest method", e);
			throw new InterServiceRestException(
					"Exception while calling the http call Get /gateway/queued-atc-request?gateway_Id=" + deviceId
							+ ", Exception:-" + e.getMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Exception ex) {
			logger.error("Exception Inside RestUtils Class getATCQueueRequest method", ex);
			throw new InterServiceRestException(
					"Exception while calling the http call Get queued-atc-request, Exception:-" + ex.getMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	public DeviceATCommandReqResponse getATCResponse(ATCommandRequestPayload atcRequestPayload) {
		try {
			logger.info(
					"Inside RestUtils Class getATCResponse method and get at command request and atcRequestPayload value",
					atcRequestPayload.toString());
			Application application = eurekaClient.getApplication(deviceServiceId);
			InstanceInfo instanceInfo = application.getInstances().get(0);
			String url = "http://" + instanceInfo.getIPAddr() + ":" + instanceInfo.getPort()
					+ "/gateway-command/atc-request";
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			String token = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest()
					.getHeader("Authorization");
			headers.set("Authorization", token);
			HttpEntity<ATCommandRequestPayload> entity = new HttpEntity<ATCommandRequestPayload>(atcRequestPayload,
					headers);
			ResponseEntity<ResponseBodyDTO> response = restTemplate.postForEntity(url, entity, ResponseBodyDTO.class);
			if (response.getStatusCode().equals(HttpStatus.OK)) {
				logger.info("ATC Response +++" + response.getBody());
				ObjectMapper mapper = new ObjectMapper();
				mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
				DeviceATCommandReqResponse deviceCommandResponse = mapper.convertValue(response.getBody().getBody(),
						DeviceATCommandReqResponse.class);
				return deviceCommandResponse;
			} else {
				logger.error("Error Inside RestUtils Class getATCResponse method :- Http call not successful");
				throw new InterServiceRestException("Http call not successful", response.getStatusCode());
			}
		} catch (Exception ex) {

			logger.error("Exception Inside RestUtils Class getATCResponse method", ex);
			throw new InterServiceRestException(
					"Exception while calling the http call Get gateway/atc-request, Exception:-" + ex.getMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	public DeviceATCommandReqResponse getDeviceATCommandRequestResponse(
			DeviceCommandRequestPayload deviceCommandRequestPayload) {
		try {
			logger.info(
					"Inside RestUtils Class getDeviceATCommandRequestResponse method and get at command request and atcRequestPayload value",
					deviceCommandRequestPayload.toString());
			Application application = eurekaClient.getApplication(deviceServiceId);
			InstanceInfo instanceInfo = application.getInstances().get(0);
			String url = "http://" + instanceInfo.getIPAddr() + ":" + instanceInfo.getPort()
					+ "/gateway-command/get-request";
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			String token = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest()
					.getHeader("Authorization");
			headers.set("Authorization", token);
			HttpEntity<DeviceCommandRequestPayload> entity = new HttpEntity<DeviceCommandRequestPayload>(
					deviceCommandRequestPayload, headers);
			ResponseEntity<ResponseBodyDTO> response = restTemplate.postForEntity(url, entity, ResponseBodyDTO.class);
			if (response.getStatusCode().equals(HttpStatus.OK)) {
				logger.info("ATC Response +++" + response.getBody().toString());
//				return response.getBody().toString();
				ObjectMapper mapper = new ObjectMapper();
				mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
				DeviceATCommandReqResponse deviceCommandResponse = mapper.convertValue(response.getBody().getBody(),
						DeviceATCommandReqResponse.class);
//				User user = objectMapper.convertValue(userResponseEntity.getBody().getBody(), User.class);
				return deviceCommandResponse;
			} else {
				logger.error("Error Inside RestUtils Class getATCResponse method :- Http call not successful");
				throw new InterServiceRestException("Http call not successful", response.getStatusCode());
			}
		} catch (Exception ex) {
			logger.error("Exception Inside RestUtils Class getATCResponse method", ex);
			throw new InterServiceRestException(
					"Exception while calling the http call Get gateway/atc-request, Exception:-" + ex.getMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	public DeviceATCommandReqResponse getDeviceATCommandLatestRequestResponse(
			DeviceCommandRequestPayload deviceCommandRequestPayload) {
		try {
			logger.info(
					"Inside RestUtils Class getDeviceATCommandRequestResponse method and get at command request and atcRequestPayload value",
					deviceCommandRequestPayload.toString());
			Application application = eurekaClient.getApplication(deviceServiceId);
			InstanceInfo instanceInfo = application.getInstances().get(0);
			String url = "http://" + instanceInfo.getIPAddr() + ":" + instanceInfo.getPort()
					+ "/gateway-command/get-latest-request";
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			String token = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest()
					.getHeader("Authorization");
			headers.set("Authorization", token);
			HttpEntity<DeviceCommandRequestPayload> entity = new HttpEntity<DeviceCommandRequestPayload>(
					deviceCommandRequestPayload, headers);
			ResponseEntity<ResponseBodyDTO> response = restTemplate.postForEntity(url, entity, ResponseBodyDTO.class);
			if (response.getStatusCode().equals(HttpStatus.OK)) {
				logger.info("ATC Response +++" + response.getBody().toString());
//				return response.getBody().toString();
				ObjectMapper mapper = new ObjectMapper();
				mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
				DeviceATCommandReqResponse deviceCommandResponse = null;
				if (response.getBody().getBody() != null && response.getBody().getBody().toString().length() > 1) {
					deviceCommandResponse = mapper.convertValue(response.getBody().getBody(),
							DeviceATCommandReqResponse.class);
				}
//				User user = objectMapper.convertValue(userResponseEntity.getBody().getBody(), User.class);
				return deviceCommandResponse;
			} else {
				logger.error("Error Inside RestUtils Class getATCResponse method :- Http call not successful");
				throw new InterServiceRestException("Http call not successful", response.getStatusCode());
			}
		} catch (Exception ex) {
			logger.error("Exception Inside RestUtils Class getATCResponse method", ex);
			throw new InterServiceRestException("Data Not Found for this device :- " + ex.getMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	public String deleteATCResponse(ATCommandRequestPayload atcRequestPayload) {
		try {
			logger.info(
					"Inside RestUtils Class deleteATCResponse method and delete at command request and atcRequestPayload value",
					atcRequestPayload.toString());
			Application application = eurekaClient.getApplication(deviceServiceId);
			InstanceInfo instanceInfo = application.getInstances().get(0);
			String url = "http://" + instanceInfo.getIPAddr() + ":" + instanceInfo.getPort()
					+ "/gateway-command/atc-delete";
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			String token = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest()
					.getHeader("Authorization");
			headers.set("Authorization", token);
			HttpEntity<ATCommandRequestPayload> entity = new HttpEntity<ATCommandRequestPayload>(atcRequestPayload,
					headers);
			ResponseEntity<Object> response = restTemplate.postForEntity(url, entity, Object.class);
			if (response.getStatusCode().equals(HttpStatus.OK)) {
				logger.info("ATC Response +++" + response.getBody().toString());
				return response.getBody().toString();
			} else {
				logger.error("Error Inside RestUtils Class deleteATCResponse method :- Http call not successful");
				throw new InterServiceRestException("Http call not successful", response.getStatusCode());
			}
		} catch (Exception ex) {
			logger.error("Exception Inside RestUtils Class deleteATCResponse method", ex);
			throw new InterServiceRestException(
					"Exception while calling the http call Delete /gateway/atc-delete, Exception:-" + ex.getMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	public String[] findByType(String organisationType) {
		try {
			logger.info(
					"Inside RestUtils Class findByType method and find organization by type and organisationType value",
					organisationType.toString());
			Application application = eurekaClient.getApplication(deviceServiceId);
			InstanceInfo instanceInfo = application.getInstances().get(0);
			String token = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest()
					.getHeader("Authorization");
			String url = "http://" + instanceInfo.getIPAddr() + ":" + instanceInfo.getPort() + "/customer/core/type/"
					+ organisationType;
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.set("Authorization", token);
			HttpEntity<String> entity = new HttpEntity<String>(headers);
			ResponseEntity<String[]> companyResponseEntity = restTemplate.exchange(url, HttpMethod.GET, entity,
					String[].class);
			if (companyResponseEntity.getStatusCode().equals(HttpStatus.OK)) {
				String[] company = companyResponseEntity.getBody();
				return company;
			} else {
				logger.error("Error Inside RestUtils Class findByType method :- Http call not successful");
				throw new InterServiceRestException("Http call not successful", companyResponseEntity.getStatusCode());
			}
		} catch (Exception ex) {
			logger.error("Exception Inside RestUtils Class findByType method", ex);
			throw new InterServiceRestException(
					"Exception while calling the http call Get /customer/core/type Exception:-" + ex.getMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	public Organisation getCompanyFromCompanyServiceByOrganisationName(String organisationName) {
		try {
			logger.info("Inside RestUtils Class findById method and find organization by id", organisationName);
			Application application = eurekaClient.getApplication(deviceServiceId);
			InstanceInfo instanceInfo = application.getInstances().get(0);
			String token = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest()
					.getHeader("Authorization");
			String url = "http://" + instanceInfo.getIPAddr() + ":" + instanceInfo.getPort()
					+ "/organisation/getOrganisationByName?name=" + organisationName;
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.set("Authorization", token);
			HttpEntity<String> entity = new HttpEntity<String>(headers);
			ResponseEntity<Organisation> companyResponseEntity = restTemplate.exchange(url, HttpMethod.GET, entity,
					Organisation.class);
			if (companyResponseEntity.getStatusCode().equals(HttpStatus.OK)) {
				Organisation company = companyResponseEntity.getBody();
				return company;
			} else {
				logger.error("Error Inside RestUtils Class findById method :- Http call not successful");
				throw new InterServiceRestException("Http call not successful", companyResponseEntity.getStatusCode());
			}
		} catch (Exception ex) {
			logger.error("Exception Inside RestUtils Class findById method", ex);
			throw new InterServiceRestException(
					"Exception while calling the http call Get /customer/core/type Exception:-" + ex.getMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	public Organisation findById(Long id) {
		try {
			logger.info("Inside RestUtils Class findById method and find organization by id", id);
			Application application = eurekaClient.getApplication(deviceServiceId);
			InstanceInfo instanceInfo = application.getInstances().get(0);
			String token = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest()
					.getHeader("Authorization");
			String url = "http://" + instanceInfo.getIPAddr() + ":" + instanceInfo.getPort() + "/customer/core/id/"
					+ id;
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.set("Authorization", token);
			HttpEntity<String> entity = new HttpEntity<String>(headers);
			ResponseEntity<Organisation> companyResponseEntity = restTemplate.exchange(url, HttpMethod.GET, entity,
					Organisation.class);
			if (companyResponseEntity.getStatusCode().equals(HttpStatus.OK)) {
				Organisation company = companyResponseEntity.getBody();
				return company;
			} else {
				logger.error("Error Inside RestUtils Class findById method :- Http call not successful");
				throw new InterServiceRestException("Http call not successful", companyResponseEntity.getStatusCode());
			}
		} catch (Exception ex) {
			logger.error("Exception Inside RestUtils Class findById method", ex);
			throw new InterServiceRestException(
					"Exception while calling the http call Get /customer/core/type Exception:-" + ex.getMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	public List<Organisation> findAllOrganisation(String token) {
		try {
			logger.info("Inside RestUtils Class findAllOrganisation method");
			Application application = eurekaClient.getApplication(deviceServiceId);
			InstanceInfo instanceInfo = application.getInstances().get(0);
//			String token = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest()
//					.getHeader("Authorization");
			String url = "http://" + instanceInfo.getIPAddr() + ":" + instanceInfo.getPort() + "/organisation/getAll";
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.set("Authorization", token);
			HttpEntity<String> entity = new HttpEntity<String>(headers);
			ResponseEntity<List<Organisation>> companyResponseEntity = restTemplate.exchange(url, HttpMethod.GET,
					entity, new ParameterizedTypeReference<List<Organisation>>() {
					});
			if (companyResponseEntity.getStatusCode().equals(HttpStatus.OK)) {
				List<Organisation> company = companyResponseEntity.getBody();
				return company;
			} else {
				logger.error("Error Inside RestUtils Class findAllOrganisation method :- Http call not successful");
				throw new InterServiceRestException("Http call not successful", companyResponseEntity.getStatusCode());
			}
		} catch (Exception ex) {
			logger.error("Exception Inside RestUtils Class findAllOrganisation method", ex);
			throw new InterServiceRestException(
					"Exception while calling the http call Get /organisation/getAll Exception:-" + ex.getMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	public List<Organisation> findOrganisationByType(String organisationType, String name) {
		try {
			logger.info(
					"Inside RestUtils Class findByType method and find organization by type and organisationType value",
					organisationType.toString());
			Application application = eurekaClient.getApplication(deviceServiceId);
			InstanceInfo instanceInfo = application.getInstances().get(0);
			String token = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest()
					.getHeader("Authorization");
			String url = "http://" + instanceInfo.getIPAddr() + ":" + instanceInfo.getPort() + "/customer/type/"
					+ organisationType + "?name=" + name;
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.set("Authorization", token);
			HttpEntity<String> entity = new HttpEntity<String>(headers);
			ResponseEntity<List<Organisation>> companyResponseEntity = restTemplate.exchange(url, HttpMethod.GET,
					entity, new ParameterizedTypeReference<List<Organisation>>() {
					});
			if (companyResponseEntity.getStatusCode().equals(HttpStatus.OK)) {
				List<Organisation> company = companyResponseEntity.getBody();
				return company;
			} else {
				logger.error("Error Inside RestUtils Class findByType method :- Http call not successful");
				throw new InterServiceRestException("Http call not successful", companyResponseEntity.getStatusCode());
			}
		} catch (Exception ex) {
			logger.error("Exception Inside RestUtils Class findByType method", ex);
			throw new InterServiceRestException(
					"Exception while calling the http call Get /customer/core/type Exception:-" + ex.getMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	public List<CampaignHistoryDeviceResponse> getDeviceCampaignHistoryByImei(String imei) {
		try {
			logger.info(
					"Inside RestUtils Class getDeviceCampaignHistoryByImei method and get Device Campaign History By Imei and imei id value",
					imei);
			String url = baseUrl + "/campaign/getDeviceCampaignHistoryByImei/" + imei;
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.set("Authorization", TOKEN);
			HttpEntity<String> entity = new HttpEntity<String>(headers);
			ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
			ArrayList<CampaignHistoryDeviceResponse> allCampaignHistory = new ArrayList<CampaignHistoryDeviceResponse>();
			if (response.getStatusCode().equals(HttpStatus.OK)) {
				String res = response.getBody();
				if (res != null) {
					System.out.println(res);
					JSONArray json = new JSONArray(res);
					for (int i = 0; i < json.length(); i++) {
						CampaignHistoryDeviceResponse campaignHistory = new CampaignHistoryDeviceResponse();
						JSONObject object = json.getJSONObject(i);
						ObjectMapper mapper = new ObjectMapper();
						mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
						try {
							campaignHistory = mapper.readValue(object.toString().getBytes(),
									CampaignHistoryDeviceResponse.class);
						} catch (JsonParseException e) {
							throw new InterServiceRestException("JSON Parsing exception" + e.getMessage(),
									HttpStatus.INTERNAL_SERVER_ERROR);
						} catch (JsonMappingException e) {
							throw new InterServiceRestException("JSON mapping exception" + e.getMessage(),
									HttpStatus.INTERNAL_SERVER_ERROR);
						} catch (IOException e) {
							throw new InterServiceRestException("JSON I/O exception" + e.getMessage(),
									HttpStatus.INTERNAL_SERVER_ERROR);
						}
						allCampaignHistory.add(campaignHistory);
					}
				}
				return allCampaignHistory;
			} else {
				logger.error(
						"Error Inside RestUtils Class getDeviceCampaignHistoryByImei method :- Http call not successful");
				throw new InterServiceRestException("Http call not successful", response.getStatusCode());
			}
		} catch (RestClientException e) {
			logger.error("RestClientException Exception Inside RestUtils Class getDeviceCampaignHistoryByImei method",
					e);
			throw new InterServiceRestException(
					"Exception while calling the http call Get /campaign/getDeviceCampaignHistoryByImei, Exception:-"
							+ e.getMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Exception ex) {
			logger.error("Exception Inside RestUtils Class getDeviceCampaignHistoryByImei method", ex);
			throw new InterServiceRestException(
					"Exception while calling the http call Get Device Campaign History By Imei Exception:-"
							+ ex.getMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	public List<CampaignInstallDeviceResponse> getCampaignInstalledDevice(String imei) {
		try {
			logger.info(
					"Inside RestUtils Class getCampaignInstalledDevice method and get Campaign Installed Device By Imei and imei id value",
					imei);
			String url = baseUrl + "/campaign/ms-device/getCampaignInstalledDevice/" + imei;
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.set("Authorization", TOKEN);
			HttpEntity<String> entity = new HttpEntity<String>(headers);
			ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
			List<CampaignInstallDeviceResponse> allCampaignHistory = new ArrayList<CampaignInstallDeviceResponse>();
			CampaignInstallDeviceResponse campaignHistory = new CampaignInstallDeviceResponse();
			if (response.getStatusCode().equals(HttpStatus.OK)) {
				if (response.getBody() != null) {
					JSONObject object = new JSONObject(response.getBody());
					ObjectMapper mapper = new ObjectMapper();
					mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
					try {
						campaignHistory = mapper.readValue(object.toString().getBytes(),
								CampaignInstallDeviceResponse.class);
					} catch (JsonParseException e) {
						logger.error(
								"JsonParseException Exception Inside RestUtils Class getCampaignInstalledDevice method",
								e);
						throw new InterServiceRestException("JSON Parsing exception" + e.getMessage(),
								HttpStatus.INTERNAL_SERVER_ERROR);
					} catch (JsonMappingException e) {
						logger.error(
								"JsonMappingException Exception Inside RestUtils Class getCampaignInstalledDevice method",
								e);
						throw new InterServiceRestException("JSON mapping exception" + e.getMessage(),
								HttpStatus.INTERNAL_SERVER_ERROR);
					} catch (IOException e) {
						logger.error("IOException Exception Inside RestUtils Class getCampaignInstalledDevice method",
								e);
						throw new InterServiceRestException("JSON I/O exception" + e.getMessage(),
								HttpStatus.INTERNAL_SERVER_ERROR);
					}
				}
			}
			allCampaignHistory.add(campaignHistory);
			return allCampaignHistory;
		} catch (RestClientException e) {
			logger.error("RestClientException Exception Inside RestUtils Class getCampaignInstalledDevice method", e);
			throw new InterServiceRestException(
					"Exception while calling the http call Get campaign/ms-device/getCampaignInstalledDevice, Exception:-"
							+ e.getMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Exception ex) {
			logger.error("Exception Inside RestUtils Class getCampaignInstalledDevice method", ex);
			throw new InterServiceRestException(
					"Exception while calling the http call Get Campaign Installed Device By Imei Exception:-"
							+ ex.getMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	public List<CampaignHistoryPayloadResponse> getCampaignHistory(String imei) {
		try {
			logger.info(
					"Inside RestUtils Class getCampaignHistory method and get Campaign History By Imei and imei id value",
					imei);
			String url = baseUrl + "/campaign/campaign-history/" + imei;
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.set("Authorization", TOKEN);
			HttpEntity<String> entity = new HttpEntity<String>(headers);
			ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
			List<CampaignHistoryPayloadResponse> allCampaignHistory = new ArrayList<CampaignHistoryPayloadResponse>();
			CampaignHistoryPayloadResponse campaignHistory = new CampaignHistoryPayloadResponse();
			if (response.getStatusCode().equals(HttpStatus.OK)) {
				String res = response.getBody();
				if (res != null) {
					System.out.println(res);
					ObjectMapper mapper = new ObjectMapper();
					JSONObject object = new JSONObject(res);
					mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
					try {
						campaignHistory = mapper.readValue(object.toString().getBytes(),
								CampaignHistoryPayloadResponse.class);
					} catch (JsonParseException e) {
						logger.error("JsonParseException Exception Inside RestUtils Class getCampaignHistory method",
								e);
						throw new InterServiceRestException("JSON Parsing exception" + e.getMessage(),
								HttpStatus.INTERNAL_SERVER_ERROR);
					} catch (JsonMappingException e) {
						logger.error("JsonMappingException Exception Inside RestUtils Class getCampaignHistory method",
								e);
						throw new InterServiceRestException("JSON mapping exception" + e.getMessage(),
								HttpStatus.INTERNAL_SERVER_ERROR);
					} catch (IOException e) {
						logger.error("IOException Exception Inside RestUtils Class getCampaignHistory method", e);
						throw new InterServiceRestException("JSON I/O exception" + e.getMessage(),
								HttpStatus.INTERNAL_SERVER_ERROR);
					}
				}

			}

			allCampaignHistory.add(campaignHistory);
			return allCampaignHistory;
		} catch (RestClientException ex) {
			logger.error("RestClientException Exception Inside RestUtils Class getCampaignHistory method", ex);
			return new ArrayList<CampaignHistoryPayloadResponse>();

//			throw new InterServiceRestException(
//					"Device Not found",
//					HttpStatus.NOT_FOUND);
		} catch (Exception ex) {
			logger.error("Exception Inside RestUtils Class getCampaignHistory method", ex);
			throw new InterServiceRestException(
					"Exception while calling the http call Get campaign/campaign-history/ By Imei Exception:-"
							+ ex.getMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	public CurrentCampaignResponse getCurrentCampaign(String imei) {
		try {
			logger.info(
					"Inside RestUtils Class getCampaignInstalledDevice method and get Campaign Installed Device By Imei and imei id value",
					imei);
			String url = baseUrl + "/campaign/current-campaign/" + imei;
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.set("Authorization", TOKEN);
			HttpEntity<String> entity = new HttpEntity<String>(headers);
			ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
			CurrentCampaignResponse currentCampaign = new CurrentCampaignResponse();
			if (response.getStatusCode().equals(HttpStatus.OK)) {
				if (response.getBody() != null) {
					JSONObject object = new JSONObject(response.getBody());
					ObjectMapper mapper = new ObjectMapper();
					mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
					try {
						if (!object.get("body").equals(null)) {
							currentCampaign = mapper.readValue(object.get("body").toString().getBytes(),
									CurrentCampaignResponse.class);
						}
					} catch (JsonParseException e) {
						logger.error(
								"JsonParseException Exception Inside RestUtils Class getCampaignInstalledDevice method",
								e);
						throw new InterServiceRestException("JSON Parsing exception" + e.getMessage(),
								HttpStatus.INTERNAL_SERVER_ERROR);
					} catch (JsonMappingException e) {
						logger.error(
								"JsonMappingException Exception Inside RestUtils Class getCampaignInstalledDevice method",
								e);
						throw new InterServiceRestException("JSON mapping exception" + e.getMessage(),
								HttpStatus.INTERNAL_SERVER_ERROR);
					} catch (IOException e) {
						logger.error("IOException Exception Inside RestUtils Class getCampaignInstalledDevice method",
								e);
						throw new InterServiceRestException("JSON I/O exception" + e.getMessage(),
								HttpStatus.INTERNAL_SERVER_ERROR);
					}
				}
			}
			return currentCampaign;
		} catch (RestClientException e) {
			logger.error("RestClientException Exception Inside RestUtils Class getCampaignInstalledDevice method", e);
			return new CurrentCampaignResponse();
//			throw new InterServiceRestException(
//					"Exception while calling the http call Get campaign/ms-device/getCampaignInstalledDevice, Exception:-"
//							+ e.getMessage(),
//					HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Exception ex) {
			logger.error("Exception Inside RestUtils Class getCampaignInstalledDevice method", ex);
			throw new InterServiceRestException(
					"Exception while calling the http call Get Campaign Installed Device By Imei Exception:-"
							+ ex.getMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	public List<DeviceReportPayload> exportCsv(SearchHit[] hits, String token, List<Organisation> list,
			List<Event> eventList) {
		try {
			List<DeviceReportPayload> dataList = new ArrayList<>();
			for (int i = 0; i < hits.length; i++) {
				SearchHit searchHit = hits[i];
				try {
					JSONObject explrObject = new JSONObject(searchHit.getSourceAsString());
					dataList.add(prepareMap(explrObject, list, eventList));
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}
			}
			return dataList;

		} catch (Exception ex) {
			logger.error("Exception Inside RestUtils Class getUserFromAuthService method", ex);
			throw new InterServiceRestException(
					"Exception while calling the http call get user by username, Exception:-" + ex.getMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	public DeviceReportPayload prepareMap(JSONObject jsonObject, List<Organisation> organisationList,
			List<Event> eventList) {
		try {
//			Map<String, Object> map = new HashMap<>();
			JSONObject reportHeader = AppUtility.checkJSONObjectNullKey(jsonObject, "report_header");
			JSONObject general = AppUtility.checkJSONObjectNullKey(jsonObject, "general");
			JSONObject psiAirSupply = AppUtility.checkJSONObjectNullKey(jsonObject, "psi_air_supply");
			JSONObject temperature = AppUtility.checkJSONObjectNullKey(jsonObject, "temperature");
			JSONObject generalMaskFields = AppUtility.checkJSONObjectNullKey(jsonObject, "general_mask_fields");
			JSONObject tpmsBeta = AppUtility.checkJSONObjectNullKey(jsonObject, "tpms_beta");
			JSONObject voltage = AppUtility.checkJSONObjectNullKey(jsonObject, "voltage");
			JSONObject fieldVehicleVin = AppUtility.checkJSONObjectNullKey(jsonObject, "field_vehicle_vin");
			JSONObject softwareVersion = AppUtility.checkJSONObjectNullKey(jsonObject, "software_version");
			JSONObject fieldVehicleEcu = AppUtility.checkJSONObjectNullKey(jsonObject, "field_vehicle_ec");
			JSONObject accelerometer_fields = AppUtility.checkJSONObjectNullKey(jsonObject, "accelerometer_fields");
			JSONObject orientationFields = AppUtility.checkJSONObjectNullKey(jsonObject, "orientation_fields");
			JSONObject networkField = AppUtility.checkJSONObjectNullKey(jsonObject, "network_field");
			JSONObject configVersion = AppUtility.checkJSONObjectNullKey(jsonObject, "config_version");
			JSONObject waterfall = AppUtility.checkJSONObjectNullKey(jsonObject, "waterfall");
			JSONObject gpio = AppUtility.checkJSONObjectNullKey(jsonObject, "gpio");
			JSONObject tftpStatus = AppUtility.checkJSONObjectNullKey(jsonObject, "tftp_status");
			JSONObject abs = AppUtility.checkJSONObjectNullKey(jsonObject, "abs");
			JSONObject absOdometer = AppUtility.checkJSONObjectNullKey(jsonObject, "abs_odometer");
			JSONObject betaAbsId = AppUtility.checkJSONObjectNullKey(jsonObject, "beta_abs_id");
			JSONObject alphaAtis = AppUtility.checkJSONObjectNullKey(jsonObject, "alpha_atis");
			JSONObject psiWheelEnd = AppUtility.checkJSONObjectNullKey(jsonObject, "psi_wheel_end");
			JSONObject skfWheelEnd = AppUtility.checkJSONObjectNullKey(jsonObject, "skf_wheel_end");
			JSONObject tpmsAlpha = AppUtility.checkJSONObjectNullKey(jsonObject, "tpms_alpha");
			JSONObject beacon = AppUtility.checkJSONObjectNullKey(jsonObject, "beacon");
			JSONObject brakestroke = AppUtility.checkJSONObjectNullKey(jsonObject, "brakestroke");
			JSONObject chassis = AppUtility.checkJSONObjectNullKey(jsonObject, "chassis");
			JSONObject cargoCameraSensor = AppUtility.checkJSONObjectNullKey(jsonObject, "cargo_camera_sensor");
			JSONObject doorSensor = AppUtility.checkJSONObjectNullKey(jsonObject, "door_sensor");
			JSONObject bleDoorSensor = AppUtility.checkJSONObjectNullKey(jsonObject, "ble_door_sensor");
			JSONObject liteSentry = AppUtility.checkJSONObjectNullKey(jsonObject, "lite_sentry");
			JSONObject reefer = AppUtility.checkJSONObjectNullKey(jsonObject, "reefer");
			JSONObject pepsiTemperature = AppUtility.checkJSONObjectNullKey(jsonObject, "pepsi_temperature");
			JSONObject bleTemperature = AppUtility.checkJSONObjectNullKey(jsonObject, "ble_temperature");
			JSONObject peripheralVersion = AppUtility.checkJSONObjectNullKey(jsonObject, "peripheral_version");
			JSONObject advertisementMaxlink = AppUtility.checkJSONObjectNullKey(jsonObject, "advertisement_maxlink");
			JSONObject connectableMaxlink = AppUtility.checkJSONObjectNullKey(jsonObject, "connectable_maxlink");
			JSONObject tankSaverBeta = AppUtility.checkJSONObjectNullKey(jsonObject, "tank_saver_beta");

			DeviceReportPayload devieReportPayload = new DeviceReportPayload();
			devieReportPayload
					.setDeviceID(reportHeader != null ? AppUtility.checkNullKey(reportHeader, "device_id") : null);
			devieReportPayload.setCustomer(getOrganisationByCanNumber(organisationList, general));
			devieReportPayload.setVIN(fieldVehicleVin != null ? AppUtility.checkNullKey(fieldVehicleVin, "vin") : null);
			devieReportPayload
					.setAssetID(softwareVersion != null ? AppUtility.checkNullKey(softwareVersion, "io") : null);
			devieReportPayload.setEvent(getEventByEventId(eventList, reportHeader));
			int id = reportHeader != null ? AppUtility.checkIntNullKey(reportHeader, "event_id") : 0;
			if (id > 0) {
				devieReportPayload.setEventId(id);
			}
			devieReportPayload.setReceivedTime(generalMaskFields != null
					? " " + AppUtility.checkNullKey(generalMaskFields, "received_time_stamp") + " "
					: null);
			devieReportPayload
					.setSeq(reportHeader != null ? AppUtility.checkIntNullKey(reportHeader, "sequence") : null);
			devieReportPayload.setRTCTime(
					reportHeader != null ? "'" + AppUtility.checkNullKey(reportHeader, "rtcdate_time_info_str") + "'"
							: null);
			devieReportPayload.setGPSTime(
					generalMaskFields != null ? "'" + AppUtility.checkNullKey(generalMaskFields, "gps_time") + "'"
							: null);
			int gpsIndex = (generalMaskFields != null ? AppUtility.checkIntNullKey(generalMaskFields, "gpsstatus_index")
					: -1);
			if (gpsIndex == 0) {
				devieReportPayload.setGPS("Unlocked");
			} else {
				devieReportPayload.setGPS("Locked");
			}
			devieReportPayload.setLat(
					generalMaskFields != null ? AppUtility.checkDoubleNullKey(generalMaskFields, "latitude") : null);
			devieReportPayload.setLong(
					generalMaskFields != null ? AppUtility.checkDoubleNullKey(generalMaskFields, "longitude") : null);
			devieReportPayload.setLatLong(
					((generalMaskFields != null ? AppUtility.checkDoubleNullKey(generalMaskFields, "latitude") : 0)
							+ "/"
							+ (generalMaskFields != null ? AppUtility.checkDoubleNullKey(generalMaskFields, "longitude")
									: 0)));

//			devieReportPayload.setMainV(voltage != null ? AppUtility.checkFlotNullKey(voltage, "main_power") : null);
//			devieReportPayload.setAltV(voltage != null ? AppUtility.checkFlotNullKey(voltage, "aux_power") : null);
//			devieReportPayload.setBatrV(voltage != null ? AppUtility.checkFlotNullKey(voltage, "battery_power") : null);
//			devieReportPayload
//					.setChargingV(voltage != null ? AppUtility.checkFlotNullKey(voltage, "charge_power") : null);
			devieReportPayload = prepareVoltage(generalMaskFields, voltage, devieReportPayload);

			devieReportPayload = preprareDeviceStatus(generalMaskFields, voltage, devieReportPayload);
			int altitude = (generalMaskFields != null ? AppUtility.checkIntNullKey(generalMaskFields, "altitude_feet")
					: 0);
			if (altitude == 1000000000) {
				devieReportPayload.setAltitude("NA");
			} else {
				devieReportPayload.setAltitude(String.valueOf(altitude * 3.28084));
			}
			devieReportPayload.setMPH(
					generalMaskFields != null ? AppUtility.checkFlotNullKey(generalMaskFields, "speed_miles") : 0f);
			devieReportPayload.setKPH(
					generalMaskFields != null ? AppUtility.checkFlotNullKey(generalMaskFields, "speed_kms") : 0f);
			devieReportPayload.setHeading(
					generalMaskFields != null ? AppUtility.checkIntNullKey(generalMaskFields, "heading") : 0);
			double odomerter = generalMaskFields != null
					? AppUtility.checkDoubleNullKey(generalMaskFields, "odometer_kms")
					: 0d;
			if (odomerter == 1000000000 || odomerter == 0) {
				odomerter = 0d;
			} else if (odomerter > 0) {
				odomerter = odomerter * 0.621371;
			}
			devieReportPayload.setOdometer(odomerter);
			devieReportPayload.setFuel(
					fieldVehicleEcu != null ? AppUtility.checkIntNullKey(fieldVehicleEcu, "fuel_level_percentage") : 0);
			devieReportPayload.setODTMILMiles(
					fieldVehicleEcu != null ? AppUtility.checkNullKey(fieldVehicleEcu, "odtsince_milmiles") : null);
			devieReportPayload.setODTDTCClearMiles(
					fieldVehicleEcu != null ? AppUtility.checkNullKey(fieldVehicleEcu, "odtsince_dtcclear_miles")
							: null);

			devieReportPayload.setAccInfo(accelerometer_fields != null
					? AppUtility.checkFlotNullKey(accelerometer_fields, "accelerometer_xmm_s2")
					: null);
			devieReportPayload.setAccX(accelerometer_fields != null
					? AppUtility.checkFlotNullKey(accelerometer_fields, "accelerometer_xmm_s2")
					: null);
			devieReportPayload.setAccY(accelerometer_fields != null
					? AppUtility.checkFlotNullKey(accelerometer_fields, "accelerometer_ymm_s2")
					: null);
			devieReportPayload.setAccZ(accelerometer_fields != null
					? AppUtility.checkFlotNullKey(accelerometer_fields, "accelerometer_zmm_s2")
					: null);

			devieReportPayload.setAccCalibX(accelerometer_fields == null ? -13824.00f
					: (accelerometer_fields != null
							? AppUtility.checkFlotNullKey(accelerometer_fields, "accelerometer_xcalib")
							: -13824.00f));
			devieReportPayload.setAccCalibY(accelerometer_fields == null ? -13824.00f
					: (accelerometer_fields != null
							? AppUtility.checkFlotNullKey(accelerometer_fields, "accelerometer_ycalib")
							: -13824.00f));
			devieReportPayload.setAccCalibZ(accelerometer_fields == null ? -13824.00f
					: (accelerometer_fields != null
							? AppUtility.checkFlotNullKey(accelerometer_fields, "accelerometer_zcalib")
							: -13824.00f));
			devieReportPayload.setOrientation(
					orientationFields != null ? AppUtility.checkNullKey(orientationFields, "orientation_status")
							: null);
			devieReportPayload.setOrientX(orientationFields != null
					? AppUtility.checkFlotNullKey(orientationFields, "orientation_xaxis_milli_gs")
					: null);
			devieReportPayload.setOrientY(orientationFields != null
					? AppUtility.checkFlotNullKey(orientationFields, "orientation_yaxis_milli_gs")
					: null);
			devieReportPayload.setOrientZ(orientationFields != null
					? AppUtility.checkFlotNullKey(orientationFields, "orientation_zaxis_milli_gs")
					: null);

			devieReportPayload.setIntTemp(
					temperature != null ? AppUtility.checkFlotNullKey(temperature, "internal_temperature") : 0f);
			devieReportPayload.setAmbientTemp(
					temperature != null ? AppUtility.checkFlotNullKey(temperature, "ambient_temperature") : 0f);
			devieReportPayload
					.setRSSI(generalMaskFields != null ? AppUtility.checkIntNullKey(generalMaskFields, "rssi") : 0);

			int hdop = (generalMaskFields != null ? AppUtility.checkIntNullKey(generalMaskFields, "hdop") : 0);
			if (hdop == 1000000000) {
				devieReportPayload.setAltitude("NA");
			} else {
				devieReportPayload.setAltitude(String.valueOf(AppUtility.checkIntNullKey(generalMaskFields, "hdop")));
			}
			int numSatellites = (generalMaskFields != null
					? AppUtility.checkIntNullKey(generalMaskFields, "num_satellites")
					: 0);
			if (numSatellites == 1000000000) {
				devieReportPayload.setSats("NA");
			} else {
				devieReportPayload
						.setSats(String.valueOf(AppUtility.checkIntNullKey(generalMaskFields, "num_satellites")));
			}
			devieReportPayload.setService(
					networkField != null ? AppUtility.checkNullKey(networkField, "service_type_index") : null);
			devieReportPayload
					.setRoaming(networkField != null ? AppUtility.checkNullKey(networkField, "roaming_index") : null);
			devieReportPayload.setCountry(
					networkField != null ? AppUtility.checkNullKey(networkField, "mobile_country_code") : null);
			devieReportPayload.setTowerID(
					networkField != null ? AppUtility.checkNullKey(networkField, "mobile_network_code") : null);
			devieReportPayload
					.setNetwork(networkField != null ? AppUtility.checkNullKey(networkField, "tower_id") : null);
			devieReportPayload.setCentroidLat(
					networkField != null ? AppUtility.checkNullKey(networkField, "tower_centroid_latitude") : null);
			devieReportPayload.setCentroidLong(
					networkField != null ? AppUtility.checkNullKey(networkField, "tower_centroid_longitude") : null);
			devieReportPayload
					.setBand(networkField != null ? AppUtility.checkNullKey(networkField, "cellular_band") : null);
			devieReportPayload
					.setRxTxEc(networkField != null ? AppUtility.checkNullKey(networkField, "rx_tx_ec") : null);

			devieReportPayload.setAPPVer(
					softwareVersion != null ? AppUtility.checkNullKey(softwareVersion, "app_version") : null);
			devieReportPayload
					.setOSVer(softwareVersion != null ? AppUtility.checkNullKey(softwareVersion, "os_version") : null);
			devieReportPayload.setHWRev(
					softwareVersion != null ? AppUtility.checkNullKey(softwareVersion, "hw_id_version") : null);
			devieReportPayload.setHWVer(
					softwareVersion != null ? AppUtility.checkNullKey(softwareVersion, "hw_version_revision") : null);
			devieReportPayload.setIOVer(
					softwareVersion != null ? AppUtility.checkNullKey(softwareVersion, "hw_version_revision") : null);
			devieReportPayload.setExtenderVer(
					softwareVersion != null ? AppUtility.checkNullKey(softwareVersion, "extender_version") : null);
			devieReportPayload.setBLEVer(
					softwareVersion != null ? AppUtility.checkNullKey(softwareVersion, "ble_version") : null);

			devieReportPayload.setConfigChg(
					configVersion != null ? AppUtility.checkNullKey(configVersion, "device_config_changed") : null);
			devieReportPayload
					.setConfig(configVersion != null ? AppUtility.checkNullKey(configVersion, "device_config") : null);
			devieReportPayload.setConfigDesc(
					configVersion != null ? AppUtility.checkNullKey(configVersion, "configuration_desc") : null);

			JSONArray waterFallInfoArray = (waterfall != null
					? AppUtility.checkJSONArrayNullKey(waterfall, "waterfall_info")
					: null);
			if (waterFallInfoArray != null && waterFallInfoArray.length() > 0) {
				for (int i = 0; i < waterFallInfoArray.length(); i++) {
					JSONObject waterfallInfo = waterFallInfoArray.getJSONObject(i);
					int configId = (waterfallInfo != null ? AppUtility.checkIntNullKey(waterfallInfo, "config_id") : 0);
					if (configId == 1) {
						devieReportPayload.setDevdefcfg(waterfallInfo != null
								? AppUtility.checkNullKey(waterfallInfo, "config_identification_version")
								: null);
						devieReportPayload.setDevdefcfgCRC(
								waterfallInfo != null ? AppUtility.checkNullKey(waterfallInfo, "config_crc") : null);
					}
					if (configId == 2) {
						devieReportPayload.setDevdef2cfg(waterfallInfo != null
								? AppUtility.checkNullKey(waterfallInfo, "config_identification_version")
								: null);
						devieReportPayload.setDevdef2cfgCRC(
								waterfallInfo != null ? AppUtility.checkNullKey(waterfallInfo, "config_crc") : null);
					}
					if (configId == 3) {
						devieReportPayload.setDevdef3cfg(waterfallInfo != null
								? AppUtility.checkNullKey(waterfallInfo, "config_identification_version")
								: null);
						devieReportPayload.setDevdef3cfgCRC(
								waterfallInfo != null ? AppUtility.checkNullKey(waterfallInfo, "config_crc") : null);
					}
					if (configId == 4) {
						devieReportPayload.setDevdef4cfg(waterfallInfo != null
								? AppUtility.checkNullKey(waterfallInfo, "config_identification_version")
								: null);
						devieReportPayload.setDevdef4cfgCRC(
								waterfallInfo != null ? AppUtility.checkNullKey(waterfallInfo, "config_crc") : null);
					}

					if (configId == 5) {
						devieReportPayload.setDevusrcfg(waterfallInfo != null
								? AppUtility.checkNullKey(waterfallInfo, "config_identification_version")
								: null);
						devieReportPayload.setDevusrcfgCRC(
								waterfallInfo != null ? AppUtility.checkNullKey(waterfallInfo, "config_crc") : null);
					}

				}
			}
			devieReportPayload.setGPIO(gpio != null ? AppUtility.checkNullKey(gpio, "gpio_status") : null);
			devieReportPayload
					.setTFTPStatus(tftpStatus != null ? AppUtility.checkNullKey(tftpStatus, "tftp_status") : null);
			devieReportPayload.setPeripheralVersion(preprarePeripheralVersion(peripheralVersion));
			devieReportPayload.setABS(abs != null ? AppUtility.checkNullKey(abs, "status") : null);

			Float absOdometers = absOdometer != null ? AppUtility.checkFlotNullKey(absOdometer, "odometer") : null;
			if (absOdometers != null && absOdometers > 0) {
				DECFOR.setRoundingMode(RoundingMode.UP);
				String absOdo = DECFOR.format(absOdometers * 0.0621371);
				devieReportPayload.setABSOdometer(absOdo);
			}
//			devieReportPayload
//					.setABSOdometer(absOdometer != null ? AppUtility.checkNullKey(absOdometer, "odometer") : null);
			devieReportPayload.setABSInfo(betaAbsId != null ? AppUtility.checkNullKey(betaAbsId, "status") : null);

			devieReportPayload.setAirSupply(prepareAirSupply(psiAirSupply));
//			devieReportPayload.setAirSupply(
//					psiAirSupply != null ? AppUtility.checkNullKey(psiAirSupply, "psi_air_supply_measure") : null);
			devieReportPayload.setATIS(alphaAtis != null ? AppUtility.checkNullKey(alphaAtis, "condition") : null);

			devieReportPayload.setWheelEnd(prepareWheelEnd(psiWheelEnd));
//			devieReportPayload.setWheelEnd(
//					psiWheelEnd != null ? AppUtility.checkNullKey(psiWheelEnd, "psi_wheel_end_measure") : null);
			devieReportPayload
					.setSFKWheelEnd(skfWheelEnd != null ? AppUtility.checkNullKey(skfWheelEnd, "comm_status") : null);
			devieReportPayload = prepareAlphaTpms(tpmsAlpha, devieReportPayload);
			devieReportPayload = prepareBetaTpms(tpmsBeta, devieReportPayload);
			devieReportPayload
					.setBeaconSmartPair(beacon != null ? AppUtility.checkNullKey(beacon, "comm_status") : null);
			devieReportPayload
					.setBrakeStroke(brakestroke != null ? AppUtility.checkNullKey(brakestroke, "brakestroke") : null);

			devieReportPayload.setChassisCargo(prepareChassisCargo(chassis));
			devieReportPayload
					.setThumbnail(cargoCameraSensor != null ? AppUtility.checkNullKey(cargoCameraSensor, "uri") : null);
			devieReportPayload
					.setState(cargoCameraSensor != null ? AppUtility.checkNullKey(cargoCameraSensor, "state") : null);
			devieReportPayload.setLoaded(
					cargoCameraSensor != null ? AppUtility.checkNullKey(cargoCameraSensor, "prediction_value") : null);
			devieReportPayload.setConfidenceRating(
					cargoCameraSensor != null ? AppUtility.checkNullKey(cargoCameraSensor, "confidence_rating") : null);
			devieReportPayload.setDoor(doorSensor != null ? AppUtility.checkNullKey(doorSensor, "status") : null);
			devieReportPayload.setDoorWireless(prepareeBleDoorWirelessSensor(bleDoorSensor));

			devieReportPayload.setLiftgateVoltage(
					advertisementMaxlink != null ? AppUtility.checkNullKey(advertisementMaxlink, "lift_gate_voltage")
							: null);
			devieReportPayload.setStateOfCharge(
					advertisementMaxlink != null ? AppUtility.checkNullKey(advertisementMaxlink, "state_of_charge")
							: null);
			devieReportPayload.setLiftGateCycles(
					connectableMaxlink != null ? AppUtility.checkNullKey(connectableMaxlink, "lift_gate_cycles")
							: null);
			devieReportPayload.setMotor1Runtime(
					connectableMaxlink != null ? AppUtility.checkNullKey(connectableMaxlink, "motor1_runtime") : null);
			devieReportPayload.setMotor2Runtime(
					connectableMaxlink != null ? AppUtility.checkNullKey(connectableMaxlink, "motor2_runtime") : null);
			devieReportPayload.setStatusFlags(
					connectableMaxlink != null ? AppUtility.checkNullKey(connectableMaxlink, "status_flags") : null);

			devieReportPayload.setLightOut(prepareLiteOut(liteSentry));

			devieReportPayload.setStatus(reefer != null ? AppUtility.checkNullKey(reefer, "status") : null);
			devieReportPayload.setTempControl(reefer != null ? AppUtility.checkNullKey(reefer, "state") : null);
			devieReportPayload.setAirFlow(reefer != null ? AppUtility.checkNullKey(reefer, "fan_state") : null);
			String reefers = devieReportPayload.getStatus() != null ? devieReportPayload.getStatus() + ";" : "";
			String reefers1 = devieReportPayload.getTempControl() != null ? devieReportPayload.getTempControl() + "; "
					: "";
			String reefers2 = devieReportPayload.getAirFlow() != null ? devieReportPayload.getAirFlow() : "";
			devieReportPayload.setReefer(reefers + reefers1 + reefers2);
			devieReportPayload.setTankSaverBeta(prerareTankSaverObj(tankSaverBeta));
			devieReportPayload = prepareTempMinue(pepsiTemperature, devieReportPayload);
			devieReportPayload = prepareTempWireLess(bleTemperature, devieReportPayload);
			devieReportPayload.setACKn(reportHeader != null ? AppUtility.checkNullKey(reportHeader, "ack_n") : null);
			devieReportPayload.setDeviceIP(general != null ? AppUtility.checkNullKey(general, "device_ip") : null);
			devieReportPayload.setRawReport(general != null ? AppUtility.checkNullKey(general, "rawreport") : null);

			return devieReportPayload;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			throw new InterServiceRestException("Exception while Converting data into JSON Object:-" + e.getMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	public String prepareWheelEnd(JSONObject psiWheelEnd) {
		String psiWheel = "";
		if (psiWheelEnd != null) {
			JSONArray psiWheelEndMeasure = psiWheelEnd != null
					? AppUtility.checkJSONArrayNullKey(psiWheelEnd, "psi_wheel_end_measure")
					: null;
			String commonStatus = psiWheelEnd != null ? AppUtility.checkNullKey(psiWheelEnd, "comm_status") : "";
			if (psiWheelEndMeasure != null && psiWheelEndMeasure.length() > 0) {
				JSONObject obj = psiWheelEndMeasure.getJSONObject(0);
				String leftStatus = obj != null ? AppUtility.checkNullKey(obj, "left_status") : "";
				String leftBattery = obj != null ? AppUtility.checkNullKey(obj, "left_battery") : "";
				String rightStatus = obj != null ? AppUtility.checkNullKey(obj, "right_status") : "";
				String rightBattery = obj != null ? AppUtility.checkNullKey(obj, "right_battery") : "";
				String leftTemperature = obj != null ? AppUtility.checkNullKey(obj, "left_temperature") : "";
				String rightTemperature = obj != null ? AppUtility.checkNullKey(obj, "right_temperature") : "";
				String leftStatus2 = "";
				String leftBattery2 = "";
				String rightStatus2 = "";
				String rightBattery2 = "";
				String leftTemperature2 = "";
				String rightTemperature2 = "";
				if (psiWheelEndMeasure.length() > 1) {
					JSONObject obj2 = psiWheelEndMeasure.getJSONObject(1);
					leftStatus2 = obj2 != null ? AppUtility.checkNullKey(obj2, "left_status") : "";
					leftBattery2 = obj2 != null ? AppUtility.checkNullKey(obj2, "left_battery") : "";
					rightStatus2 = obj2 != null ? AppUtility.checkNullKey(obj2, "right_status") : "";
					rightBattery2 = obj2 != null ? AppUtility.checkNullKey(obj2, "right_battery") : "";
					leftTemperature2 = obj2 != null ? AppUtility.checkNullKey(obj2, "left_temperature") : "";
					rightTemperature2 = obj2 != null ? AppUtility.checkNullKey(obj2, "right_temperature") : "";
				}
				psiWheel = commonStatus + "; L Status_1:" + leftStatus + "; L Bat_1:" + leftBattery + ";  R Status_1:"
						+ rightStatus + " R Bat_1:" + rightBattery + "; L Temp_1: " + leftTemperature + "; R Temp_1: "
						+ rightTemperature + "; L Status_2:" + leftStatus2 + "; L Bat_2:" + leftBattery2
						+ ";  R Status_2:" + rightStatus2 + " R Bat_2:" + rightBattery2 + "; L Temp_2: "
						+ leftTemperature2 + "; R Temp_2: " + rightTemperature2;

			} else {
				psiWheel = commonStatus;
			}

		}
		return psiWheel;

	}

	public String prepareAirSupply(JSONObject psiAirSupply) {
		String airSupply = "";
		if (psiAirSupply != null) {
			JSONArray psiAirSupplyMeasure = psiAirSupply != null
					? AppUtility.checkJSONArrayNullKey(psiAirSupply, "psi_air_supply_measure")
					: null;
			String commonStatus = psiAirSupply != null ? AppUtility.checkNullKey(psiAirSupply, "comm_status") : "";
			if (psiAirSupplyMeasure != null && psiAirSupplyMeasure.length() > 0) {
				for (int i = 0; i < psiAirSupplyMeasure.length(); i++) {
					int j = i + 1;
					JSONObject obj = psiAirSupplyMeasure.getJSONObject(i);
					String tankStatus = obj != null ? AppUtility.checkNullKey(obj, "tank_status") : "";
					String tankBattery = obj != null ? AppUtility.checkNullKey(obj, "tank_battery") : "";
					String supplyStatus = obj != null ? AppUtility.checkNullKey(obj, "supply_status") : "";
					String supplyBattery = obj != null ? AppUtility.checkNullKey(obj, "supply_battery") : "";
					String tankPressure = obj != null ? AppUtility.checkNullKey(obj, "tank_pressure") : "";
					String supplyPressure = obj != null ? AppUtility.checkNullKey(obj, "supply_pressure") : "";
					airSupply = airSupply + "ASMS " + j + ":  T Status_ " + j + ": " + tankStatus + "; T Bat_" + j
							+ ": " + tankBattery + "; S Status_" + j + ": " + supplyStatus + "; S Bat_" + j + ": "
							+ supplyBattery + "; Tank Pressure_" + j + ": " + tankPressure + "; Supply Pressure_" + j
							+ ": " + supplyPressure;
				}
				airSupply = commonStatus + airSupply;
			} else {
				airSupply = commonStatus;
			}

		}
		return airSupply;

	}

	public DeviceReportPayload prepareVoltage(JSONObject generalMaskFields, JSONObject voltage,
			DeviceReportPayload deviceReportPayload) {
		String mainV = null;
		String altV = null;
		String batV = null;
		String chargingV = null;
		if (voltage == null) {

			Integer internalPowerVolts = generalMaskFields != null
					? AppUtility.checkIntNullKey(generalMaskFields, "internal_power_volts")
					: null;
			if (internalPowerVolts != null && internalPowerVolts == 1000000000) {
				batV = "NA";
			} else {
				batV = internalPowerVolts + "";
			}

			Integer externalPowerVolts = generalMaskFields != null
					? AppUtility.checkIntNullKey(generalMaskFields, "external_power_volts")
					: null;
			if (externalPowerVolts != null && externalPowerVolts == 1000000000) {
				mainV = "NA";
			} else {
				mainV = externalPowerVolts + "";
			}
		} else {
			DECFOR.setRoundingMode(RoundingMode.UP);
			Float mainPower = voltage != null ? AppUtility.checkFlotNullKey(voltage, "main_power") : null;
			if (mainPower != null && mainPower > 0) {
				mainV = DECFOR.format(mainPower);
			}
			Float batteryPower = voltage != null ? AppUtility.checkFlotNullKey(voltage, "battery_power") : null;
			if (batteryPower != null && batteryPower > 0) {
				batV = DECFOR.format(batteryPower);
			}
		}

		if (voltage != null) {
			DECFOR.setRoundingMode(RoundingMode.UP);
			Integer batteryPower = voltage != null ? AppUtility.checkIntNullKey(voltage, "aux_power") : null;
			if (batteryPower != null && batteryPower == 1000000000) {
				altV = "NA";
			} else {
				Float batteryPowerF = voltage != null ? AppUtility.checkFlotNullKey(voltage, "aux_power") : null;
				if (batteryPowerF != null) {
					altV = DECFOR.format(batteryPowerF);
				}
			}
			Integer chargePower = voltage != null ? AppUtility.checkIntNullKey(voltage, "charge_power") : null;
			if (chargePower != null && chargePower == 1000000000) {
				chargingV = "NA";
			} else {
				Float chargePowerF = voltage != null ? AppUtility.checkFlotNullKey(voltage, "charge_power") : null;
				if (chargePowerF != null) {
					chargingV = DECFOR.format(chargePower);
				}
			}
		}

		deviceReportPayload.setMainV(mainV);
		deviceReportPayload.setAltV(altV);
		deviceReportPayload.setBatrV(batV);
		deviceReportPayload.setChargingV(chargingV);
		return deviceReportPayload;
	}

	public String prepareeBleDoorWirelessSensor(JSONObject bleDoorSensor) {
		String doorSensor = "";
		String commStatus = bleDoorSensor != null ? AppUtility.checkNullKey(bleDoorSensor, "comm_status") : null;
		String sensorStatus = bleDoorSensor != null ? AppUtility.checkNullKey(bleDoorSensor, "sensor_status") : null;
		String doorState = bleDoorSensor != null ? AppUtility.checkNullKey(bleDoorSensor, "door_state") : null;
		String doorType = bleDoorSensor != null ? AppUtility.checkNullKey(bleDoorSensor, "door_type") : null;
		String doorSequence = bleDoorSensor != null ? AppUtility.checkNullKey(bleDoorSensor, "door_sequence") : null;
		String battery = bleDoorSensor != null ? AppUtility.checkNullKey(bleDoorSensor, "battery") : null;
		String temperatureC = bleDoorSensor != null ? AppUtility.checkNullKey(bleDoorSensor, "temperature_c") : null;

		doorSensor = doorSensor + commStatus;
		if (sensorStatus != null) {
			doorSensor = doorSensor + "; sStatus : " + sensorStatus;
		}
		if (doorState != null) {
			doorSensor = doorSensor + ": " + doorState;
		}
		if (doorType != null) {
			doorSensor = doorSensor + "; " + doorType;
		}
		if (doorSequence != null) {
			doorSensor = doorSensor + "; " + doorSequence;
		}
		if (battery != null) {
			doorSensor = doorSensor + "; " + battery;
		}
		if (temperatureC != null) {
			doorSensor = doorSensor + "; " + temperatureC;
		}
		return doorSensor;
	}

	public String prepareChassisCargo(JSONObject chassis) {
		String chassisCargo = "";
		if (chassis != null) {
			String condition = chassis != null ? AppUtility.checkNullKey(chassis, "condition") : null;
			String code1 = chassis != null ? AppUtility.checkNullKey(chassis, "code1") : null;
			String code2 = chassis != null ? AppUtility.checkNullKey(chassis, "code2") : null;
			String cargo_state = chassis != null ? AppUtility.checkNullKey(chassis, "cargo_state") : null;
			String dist_mm = chassis != null ? AppUtility.checkNullKey(chassis, "dist_mm") : null;
			if (condition != null && condition.equals("Healthy")) {
				chassisCargo = condition + ", Codes: " + code1 + ", " + code2 + ", " + cargo_state + ", " + dist_mm
						+ "mm";
			} else {
				chassisCargo = condition + ", Codes: " + code1 + ", " + code2;
			}
		}
		return chassisCargo;
	}

	public String prerareTankSaverObj(JSONObject tankSaverBeta) {
		String tankSaver = "";
		if (tankSaverBeta != null) {
			tankSaver = tankSaverBeta != null ? AppUtility.checkNullKey(tankSaverBeta, "status") : null;
			JSONArray tankSaverBetaMeasure = tankSaverBeta != null
					? AppUtility.checkJSONArrayNullKey(tankSaverBeta, "tank_saver_beta_measure")
					: null;
			if (tankSaverBetaMeasure != null && tankSaverBetaMeasure.length() > 0) {
				for (int i = 0; i < tankSaverBetaMeasure.length(); i++) {
					JSONObject saverBeta = tankSaverBetaMeasure.getJSONObject(i);
					String sStatus = saverBeta != null ? AppUtility.checkNullKey(saverBeta, "s_status") : null;
					String sesstionTime = saverBeta != null ? AppUtility.checkNullKey(saverBeta, "session_time") : null;
					String advAge = saverBeta != null ? AppUtility.checkNullKey(saverBeta, "advertisement_age") : null;
					String sessionAgeCode = saverBeta != null ? AppUtility.checkNullKey(saverBeta, "session_age_code")
							: null;
					String advAgeCode = saverBeta != null ? AppUtility.checkNullKey(saverBeta, "advertisement_age_code")
							: null;
					tankSaver = tankSaver + "S Status: " + sStatus + ", ";
					if (sesstionTime != null) {
						tankSaver = tankSaver + "Session Time : " + sesstionTime + ", ";
					}
					if (advAge != null) {
						tankSaver = tankSaver + "Advertisemnet Age : " + advAge + ", ";
					}
					if (sessionAgeCode != null) {
						tankSaver = tankSaver + "Session Age Code : " + sessionAgeCode + ", ";
					}
					if (advAgeCode != null) {
						tankSaver = tankSaver + "Advertisment Age Code : " + advAgeCode + ", ";
					}
				}

			}
		}
		return tankSaver;
	}

	public DeviceReportPayload prepareTempWireLess(JSONObject bleTemperature, DeviceReportPayload deviceReportPayload) {

		deviceReportPayload.setTempWirelessStatus(
				bleTemperature != null ? AppUtility.checkNullKey(bleTemperature, "status") : null);
		JSONArray jsonArray = (bleTemperature != null
				? AppUtility.checkJSONArrayNullKey(bleTemperature, "ble_temperature_sensormeasure")
				: null);
		String reading = "";
		if (jsonArray != null && jsonArray.length() > 0) {
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonObj = jsonArray.getJSONObject(i);
				JSONArray dataReadingArray = (jsonObj != null
						? AppUtility.checkJSONArrayNullKey(jsonObj, "data_reading")
						: null);
				if (dataReadingArray != null && dataReadingArray.length() > 0) {
					for (int j = 0; j < dataReadingArray.length(); j++) {
						JSONObject dataReadingObj = dataReadingArray.getJSONObject(j);
						JSONArray readingArray = (dataReadingObj != null
								? AppUtility.checkJSONArrayNullKey(dataReadingObj, "temperature_readings")
								: null);
						reading = reading + CDL.rowToString(readingArray);
					}
				}
			}
		}
		deviceReportPayload.setTempWirelessReadings(reading);
		return deviceReportPayload;
	}

	public DeviceReportPayload prepareTempMinue(JSONObject pepsiTemperature, DeviceReportPayload deviceReportPayload) {

		deviceReportPayload.setCommStatus(
				pepsiTemperature != null ? AppUtility.checkNullKey(pepsiTemperature, "comm_status") : null);

		String sensorId = "";
		String ageOfReading = "";
		String sStatus = "";
		String humidity = "";
		String temperature = "";
		String batteryLevel = "";
		String pepsiMinew = "";
		JSONArray pepsiTempArray = (pepsiTemperature != null
				? AppUtility.checkJSONArrayNullKey(pepsiTemperature, "pepsi_temperaturemeasure")
				: null);

		int elements = (pepsiTemperature != null ? AppUtility.checkIntNullKey(pepsiTemperature, "temperature_elements")
				: 0);

		if (elements > 0 && pepsiTempArray != null) {
			pepsiMinew = (pepsiTemperature != null ? AppUtility.checkNullKey(pepsiTemperature, "comm_status") : null)
					+ "; ";
			for (int i = 0; i < pepsiTempArray.length(); i++) {
				JSONObject pepsiTemp = pepsiTempArray.getJSONObject(i);
				sensorId = sensorId + (pepsiTemp != null ? AppUtility.checkNullKey(pepsiTemp, "sensor_id") : "")
						+ ((elements > 1) ? "," : "");
				ageOfReading = ageOfReading
						+ (pepsiTemp != null ? AppUtility.checkNullKey(pepsiTemp, "age_of_reading") : "")
						+ ((elements > 1) ? "," : "");
				sStatus = sStatus + (pepsiTemp != null ? AppUtility.checkNullKey(pepsiTemp, "s_status") : "")
						+ ((elements > 1) ? "," : "");
				humidity = humidity + (pepsiTemp != null ? AppUtility.checkNullKey(pepsiTemp, "humidity") : "")
						+ ((elements > 1) ? "," : "");
				temperature = temperature + (pepsiTemp != null ? AppUtility.checkNullKey(pepsiTemp, "temperature") : "")
						+ ((elements > 1) ? "C," : "C");
				batteryLevel = batteryLevel
						+ (pepsiTemp != null ? AppUtility.checkNullKey(pepsiTemp, "battery_level") : "")
						+ ((elements > 1) ? "%," : "%");
				String macAddress = (pepsiTemp != null ? AppUtility.checkNullKey(pepsiTemp, "mac_address") : null)
						+ "; ";
				pepsiMinew = pepsiMinew + "Mac Address : " + macAddress + "; Battery Level : " + batteryLevel
						+ "; Temperature : " + temperature + " ; ";
			}
		}
		deviceReportPayload.setSensorId(sensorId);
		deviceReportPayload.setAgeOfReading(ageOfReading);
		deviceReportPayload.setSensorStatus(sStatus);
		deviceReportPayload.setHumidity(humidity);
		deviceReportPayload.setTemperature(temperature);
		deviceReportPayload.setBatteryLevel(batteryLevel);
		deviceReportPayload.setTempMinew(pepsiMinew + " Total Sensors : " + elements + "; ");
		return deviceReportPayload;
	}

	public DeviceReportPayload prepareBetaTpms(JSONObject tpms_beta, DeviceReportPayload deviceReportPayload) {
		String tpmsBeta = "";
		JSONArray tpms_beta_measure = (tpms_beta != null
				? AppUtility.checkJSONArrayNullKey(tpms_beta, "tpms_beta_measure")
				: null);
		if (tpms_beta != null && tpms_beta_measure != null) {
			tpmsBeta = (tpms_beta != null ? AppUtility.checkNullKey(tpms_beta, "status") : null);
			deviceReportPayload.setReceivers(tpmsBeta);
			if (tpms_beta_measure.length() > 0) {
				JSONObject obj1 = tpms_beta_measure.getJSONObject(0);
				String primary_curb_status = (obj1 != null ? AppUtility.checkNullKey(obj1, "primary_curb_status")
						: null);
				String inner_curb_status = (obj1 != null ? AppUtility.checkNullKey(obj1, "inner_curb_status") : null);
				String inner_roadside_status = (obj1 != null ? AppUtility.checkNullKey(obj1, "inner_roadside_status")
						: null);
				String primary_roadside_status = (obj1 != null
						? AppUtility.checkNullKey(obj1, "primary_roadside_status")
						: null);
				if (primary_curb_status != null && primary_curb_status.equalsIgnoreCase("online")) {
					String primary_curbside_temperature_f = (obj1 != null
							? AppUtility.checkNullKey(obj1, "primary_curbside_temperature_f")
							: null);
					String primary_curbside_pressure_psi = (obj1 != null
							? AppUtility.checkNullKey(obj1, "primary_curbside_pressure_psi")
							: null);
					deviceReportPayload.setLOF(primary_curb_status);
					deviceReportPayload.setLOF1TempBeta(primary_curbside_temperature_f + "F");
					deviceReportPayload.setLOF1PsiBeta(primary_curbside_pressure_psi + "psi");
					tpmsBeta = tpmsBeta + "; FLOS: " + primary_curb_status + "; FLO: " + primary_curbside_pressure_psi
							+ "psi, " + primary_curbside_temperature_f + "F ";
				} else {
					deviceReportPayload.setLOF(primary_curb_status);
					tpmsBeta = tpmsBeta + "; FLOS: " + primary_curb_status;
				}
				if (inner_curb_status != null && inner_curb_status.equalsIgnoreCase("online")) {
					String inner_curbside_temperature_f = (obj1 != null
							? AppUtility.checkNullKey(obj1, "inner_curbside_temperature_f")
							: null);
					String inner_curbisde_pressure_psi = (obj1 != null
							? AppUtility.checkNullKey(obj1, "inner_curbisde_pressure_psi")
							: null);
					deviceReportPayload.setLIF(inner_curb_status);
					deviceReportPayload.setLIF1Psi(inner_curbisde_pressure_psi + "psi");
					deviceReportPayload.setLIF1Temp(inner_curbside_temperature_f + "F");
					tpmsBeta = tpmsBeta + "; FLIS: " + inner_curb_status + "; FLI: " + inner_curbisde_pressure_psi
							+ "psi, " + inner_curbisde_pressure_psi + "F ";
				} else {
					deviceReportPayload.setLIF(inner_curb_status);
					tpmsBeta = tpmsBeta + "; FLIS: " + inner_curb_status;
				}
				if (inner_roadside_status != null && inner_roadside_status.equalsIgnoreCase("online")) {
					String inner_roadside_temperature_f = (obj1 != null
							? AppUtility.checkNullKey(obj1, "inner_roadside_temperature_f")
							: null);
					String inner_roadside_pressure_psi = (obj1 != null
							? AppUtility.checkNullKey(obj1, "inner_roadside_pressure_psi")
							: null);
					deviceReportPayload.setRIF(inner_roadside_status);
					deviceReportPayload.setRIF1Psi(inner_roadside_pressure_psi + "psi");
					deviceReportPayload.setRIF1Temp(inner_roadside_temperature_f + "F");
					tpmsBeta = tpmsBeta + "; FRIS: " + inner_roadside_status + "; FRI: " + inner_roadside_pressure_psi
							+ "psi, " + inner_roadside_temperature_f + "F ";
				} else {
					deviceReportPayload.setRIF(inner_roadside_status);
					tpmsBeta = tpmsBeta + "; FRIS:" + inner_roadside_status;
				}
				if (primary_roadside_status != null && primary_roadside_status.equalsIgnoreCase("online")) {
					String primary_roadside_temperature_f = (obj1 != null
							? AppUtility.checkNullKey(obj1, "primary_roadside_temperature_f")
							: null);
					String primary_roadside_pressure_psi = (obj1 != null
							? AppUtility.checkNullKey(obj1, "primary_roadside_pressure_psi")
							: null);
					deviceReportPayload.setROF(primary_roadside_status);
					deviceReportPayload.setROF1Psi(primary_roadside_pressure_psi + "F");
					deviceReportPayload.setROF1Temp(primary_roadside_temperature_f + "psi");
					tpmsBeta = tpmsBeta + "; FROS: " + primary_roadside_status + "; FRO: "
							+ primary_roadside_pressure_psi + "psi, " + primary_roadside_temperature_f + "F ";
				} else {
					tpmsBeta = tpmsBeta + "; FROS: " + primary_roadside_status;
					deviceReportPayload.setROF(primary_roadside_status);
				}
				if (tpms_beta_measure.length() >= 2) {
					JSONObject obj2 = tpms_beta_measure.getJSONObject(1);
					String primary_curb_status1 = (obj2 != null ? AppUtility.checkNullKey(obj2, "primary_curb_status")
							: null);
					String inner_curb_status1 = (obj2 != null ? AppUtility.checkNullKey(obj2, "inner_curb_status")
							: null);
					String inner_roadside_status1 = (obj2 != null
							? AppUtility.checkNullKey(obj2, "inner_roadside_status")
							: null);
					String primary_roadside_status1 = (obj2 != null
							? AppUtility.checkNullKey(obj2, "primary_roadside_status")
							: null);
					if (primary_curb_status1 != null && primary_curb_status1.equalsIgnoreCase("online")) {
						String primary_curbside_temperature_f = (obj2 != null
								? AppUtility.checkNullKey(obj2, "primary_curbside_temperature_f")
								: null);
						String primary_curbside_pressure_psi = (obj2 != null
								? AppUtility.checkNullKey(obj2, "primary_curbside_pressure_psi")
								: null);
						deviceReportPayload.setLOR(primary_curb_status1);
						deviceReportPayload.setLOR1Temp(primary_curbside_temperature_f);
						deviceReportPayload.setLOR1Psi(primary_curbside_pressure_psi);
						tpmsBeta = tpmsBeta + "; RLOS: " + primary_curb_status1 + "; RLOS: "
								+ primary_curbside_pressure_psi + "psi, " + primary_curbside_temperature_f + "F ";
					} else {
						deviceReportPayload.setLOR(primary_curb_status1);
						tpmsBeta = tpmsBeta + "; RLOS: " + primary_curb_status1;
					}
					if (inner_curb_status1 != null && inner_curb_status1.equalsIgnoreCase("online")) {
						String inner_curbside_temperature_f = (obj2 != null
								? AppUtility.checkNullKey(obj2, "inner_curbside_temperature_f")
								: null);
						String inner_curbisde_pressure_psi = (obj2 != null
								? AppUtility.checkNullKey(obj2, "inner_curbisde_pressure_psi")
								: null);
						deviceReportPayload.setLIR(inner_curb_status1);
						deviceReportPayload.setLIR1Psi(inner_curbisde_pressure_psi + "psi");
						deviceReportPayload.setLIR1Temp(inner_curbside_temperature_f + "F");
						tpmsBeta = tpmsBeta + "; RLIS: " + inner_curb_status1 + "; RLIS: " + inner_curbisde_pressure_psi
								+ "psi, " + inner_curbisde_pressure_psi + "F ";
					} else {
						deviceReportPayload.setLIR(inner_curb_status1);
						tpmsBeta = tpmsBeta + "; RLIS: " + inner_curb_status1;
					}

					if (inner_roadside_status1 != null && inner_roadside_status1.equalsIgnoreCase("online")) {
						String inner_roadside_temperature_f = (obj2 != null
								? AppUtility.checkNullKey(obj2, "inner_roadside_temperature_f")
								: null);
						String inner_roadside_pressure_psi = (obj2 != null
								? AppUtility.checkNullKey(obj2, "inner_roadside_pressure_psi")
								: null);
						deviceReportPayload.setRIR(inner_roadside_status1);
						deviceReportPayload.setRIR1Psi(inner_roadside_pressure_psi + "psi");
						deviceReportPayload.setRIR1Temp(inner_roadside_temperature_f + "F");
						tpmsBeta = tpmsBeta + "; RRIS: " + inner_roadside_status1 + "; RRIS: "
								+ inner_roadside_pressure_psi + "psi, " + inner_roadside_temperature_f + "F ";
					} else {
						deviceReportPayload.setRIR(inner_roadside_status1);
						tpmsBeta = tpmsBeta + "; RRIS:" + inner_roadside_status1;
					}

					if (primary_roadside_status1 != null && primary_roadside_status1.equalsIgnoreCase("online")) {
						String primary_roadside_temperature_f = (obj2 != null
								? AppUtility.checkNullKey(obj2, "primary_roadside_temperature_f")
								: null);
						String primary_roadside_pressure_psi = (obj2 != null
								? AppUtility.checkNullKey(obj2, "primary_roadside_pressure_psi")
								: null);
						deviceReportPayload.setROR(primary_roadside_status1);
						deviceReportPayload.setROR1Psi(primary_roadside_pressure_psi + "F");
						deviceReportPayload.setROR1Temp(primary_roadside_temperature_f + "psi");
						tpmsBeta = tpmsBeta + "; RROS: " + primary_roadside_status1 + "; RROS: "
								+ primary_roadside_pressure_psi + "psi, " + primary_roadside_temperature_f + "F ";
					} else {
						tpmsBeta = tpmsBeta + "; RROS: " + primary_roadside_status1;
						deviceReportPayload.setROR(primary_roadside_status1);
					}
				}
			}
			deviceReportPayload.setBetaTPMS(tpmsBeta);
		}
		return deviceReportPayload;
	}

	public DeviceReportPayload prepareAlphaTpms(JSONObject tpms_alpha, DeviceReportPayload deviceReportPayload) {
		if (tpms_alpha != null) {
			deviceReportPayload.setReceiver(tpms_alpha != null ? AppUtility.checkNullKey(tpms_alpha, "status") : null);
			JSONArray tempMeasure = (tpms_alpha != null ? AppUtility.checkJSONArrayNullKey(tpms_alpha, "tpms_measures")
					: null);
			int numSensor = (tpms_alpha != null ? AppUtility.checkIntNullKey(tpms_alpha, "num_sensors") : 0);
			String tpmsAlpha = deviceReportPayload.getReceiver()+ " ";
			if (numSensor > 0) {
				if (tempMeasure != null && tempMeasure.length() > 0) {
					for (int i = 0; i < tempMeasure.length(); i++) {
						JSONObject alphaTpms = tempMeasure.getJSONObject(i);
						int tirePressurePsi = (alphaTpms != null
								? AppUtility.checkIntNullKey(alphaTpms, "tire_pressure_psi")
								: null);
						int tirePressureC = (alphaTpms != null
								? AppUtility.checkIntNullKey(alphaTpms, "tire_temperature_c")
								: null);
						int tirePressureF = (alphaTpms != null
								? AppUtility.checkIntNullKey(alphaTpms, "tire_temperature_f")
								: null);
						String temperature = "";
						String pressure = "";
						if (tirePressurePsi <= 0) {
							pressure = "--";
							tpmsAlpha = tpmsAlpha + "-- , ";
						} else {
							pressure = tirePressurePsi + "psi";
							  tpmsAlpha = tpmsAlpha + tirePressurePsi + "psi, ";
						}
						if (tirePressureC <= -50) {
							temperature = "--";
							tpmsAlpha = tpmsAlpha + "-- , ";
						} else {
							temperature = tirePressureF + "F";
							tpmsAlpha = tpmsAlpha + tirePressureF + "F; ";
						}
						String tireLocation = (alphaTpms != null
								? AppUtility.checkNullKey(alphaTpms, "tire_location_str")
								: null);
						if (tireLocation.equals("FLO") || tireLocation.equals("LFO") || tireLocation.equals("LOF")
								|| tireLocation.equals("FOL") || tireLocation.equals("OFL")
								|| tireLocation.equals("OLF")) {
							deviceReportPayload.setLOF1(tireLocation);
							deviceReportPayload.setLOF1Psi(pressure);
							deviceReportPayload.setLOF1Temp(temperature);
						}
						if (tireLocation.equals("LIF") || tireLocation.equals("LFI") || tireLocation.equals("ILF")
								|| tireLocation.equals("IFL") || tireLocation.equals("FLI")
								|| tireLocation.equals("FIL")) {
							deviceReportPayload.setLIF2(tireLocation);
							deviceReportPayload.setLIF2Psi(pressure);
							deviceReportPayload.setLIF2Temp(temperature);
						}
						if (tireLocation.equals("RIF") || tireLocation.equals("RFI") || tireLocation.equals("IRF")
								|| tireLocation.equals("IFR") || tireLocation.equals("FRI")
								|| tireLocation.equals("FIR")) {
							deviceReportPayload.setRIF3(tireLocation);
							deviceReportPayload.setRIF3Psi(pressure);
							deviceReportPayload.setRIF3Temp(temperature);
						}
						if (tireLocation.equals("ROF") || tireLocation.equals("RFO") || tireLocation.equals("ORF")
								|| tireLocation.equals("OFR") || tireLocation.equals("FRO")
								|| tireLocation.equals("FOR")) {
							deviceReportPayload.setROF4(tireLocation);
							deviceReportPayload.setROF4Psi(pressure);
							deviceReportPayload.setROF4Temp(temperature);
						}
						if (tireLocation.equals("LOR") || tireLocation.equals("LRO") || tireLocation.equals("OLR")
								|| tireLocation.equals("ORL") || tireLocation.equals("RLO")
								|| tireLocation.equals("ROL")) {
							deviceReportPayload.setLOR5(tireLocation);
							deviceReportPayload.setLOR5Psi(pressure);
							deviceReportPayload.setLOR5Temp(temperature);
						}
						if (tireLocation.equals("LIR") || tireLocation.equals("LRI") || tireLocation.equals("ILR")
								|| tireLocation.equals("IRL") || tireLocation.equals("RLI")
								|| tireLocation.equals("RIL")) {
							deviceReportPayload.setLIR6(tireLocation);
							deviceReportPayload.setLIR6Psi(pressure);
							deviceReportPayload.setLIR6Temp(temperature);
						}
						if (tireLocation.equals("RIR") || tireLocation.equals("IRR") || tireLocation.equals("RRI")) {
							deviceReportPayload.setRIR7(tireLocation);
							deviceReportPayload.setRIR7Psi(pressure);
							deviceReportPayload.setRIR7Temp(temperature);
						}
						if (tireLocation.equals("ROR") || tireLocation.equals("ORR") || tireLocation.equals("RRO")) {
							deviceReportPayload.setROR8(tireLocation);
							deviceReportPayload.setROR8Psi(pressure);
							deviceReportPayload.setROR8Temp(temperature);
						}
					}
				}
			}
			deviceReportPayload.setAlphaTpms(tpmsAlpha);
		}
		return deviceReportPayload;
	}

	public String prepareLiteOut(JSONObject liteSentry) {
		String lightOut = null;

		if (liteSentry != null) {
			Boolean communicating = liteSentry.has("communicating")
					? AppUtility.checkBooleanNullKey(liteSentry, "communicating")
					: false;
			Boolean genericFault = liteSentry.has("generic_fault")
					? AppUtility.checkBooleanNullKey(liteSentry, "generic_fault")
					: false;
			Boolean markerFault = liteSentry.has("marker_fault")
					? AppUtility.checkBooleanNullKey(liteSentry, "marker_fault")
					: false;
			Boolean deadRightMarkerBulb = liteSentry.has("dead_right_marker_bulb")
					? AppUtility.checkBooleanNullKey(liteSentry, "dead_right_marker_bulb")
					: false;
			Boolean deadStopBulb = liteSentry.has("dead_stop_bulb")
					? AppUtility.checkBooleanNullKey(liteSentry, "dead_stop_bulb")
					: false;
			Boolean deadLeftMarkerBulb = liteSentry.has("dead_left_marker_bulb")
					? AppUtility.checkBooleanNullKey(liteSentry, "dead_left_marker_bulb")
					: false;
			Boolean deadLicenseBulb = liteSentry.has("dead_license_bulb")
					? AppUtility.checkBooleanNullKey(liteSentry, "dead_license_bulb")
					: false;

			if (communicating) {
				if (!genericFault) {
					if (!markerFault && !deadRightMarkerBulb && !deadStopBulb && !deadLeftMarkerBulb
							&& !deadLicenseBulb) {
						lightOut = "LFT RGT MRK LIC STP";
					} else {
						if (markerFault) {
							lightOut = "Marker Fault;";
						}
						if (deadLicenseBulb) {
							lightOut = "License Fault;";
						} else if (deadRightMarkerBulb) {
							lightOut = "Right Fault;";
						}
						if (deadLeftMarkerBulb) {
							lightOut = "Left Fault;";
						}
						if (deadStopBulb) {
							lightOut = "Stop Fault;";
						}
					}
				} else {
					lightOut = "Unresponsive";
				}
			} else {
				lightOut = "Unresponsive";
			}
		}

		return lightOut;

	}

	public DeviceReportPayload preprareDeviceStatus(JSONObject jsonObject, JSONObject voltateObject,
			DeviceReportPayload deviceReportPayload) {
		String deviceStatus = "";
		String deviceStatusAll = "";

		if (voltateObject != null) {
			if (AppUtility.checkIntNullKey(voltateObject, "selector") == 1) {
				if ((AppUtility.checkIntNullKey(voltateObject, "main_power") < 1000000000)
						&& (AppUtility.checkIntNullKey(voltateObject, "main_power") > 7)
						&& (AppUtility.checkIntNullKey(voltateObject, "aux_power") < 1000000000)
						&& ((AppUtility.checkIntNullKey(voltateObject, "main_power") + 0.3) > AppUtility
								.checkIntNullKey(voltateObject, "aux_power"))) {
					deviceStatus = "Primary External";
					deviceStatusAll = deviceStatusAll + "Primary External,";
				} else if ((AppUtility.checkIntNullKey(voltateObject, "aux_power") < 1000000000)
						&& (AppUtility.checkIntNullKey(voltateObject, "aux_power") > 7)) {
					deviceStatus = "Secondary External";
					deviceStatusAll = deviceStatusAll + "Secondary External, ";
				} else {
					deviceStatus = "Battery";
					deviceStatusAll = deviceStatusAll + "Battery, ";
				}
			} else {
				if ((AppUtility.checkIntNullKey(voltateObject, "main_power") > 7)
						&& (AppUtility.checkIntNullKey(voltateObject, "main_power") < 1000000000)) {
					deviceStatus = "Primary External";
					deviceStatusAll = deviceStatusAll + "Primary External, ";
				} else {
					deviceStatus = "Battery";
					deviceStatusAll = deviceStatusAll + "Battery, ";
				}
			}
		} else {
			if ((AppUtility.checkIntNullKey(jsonObject, "external_power_volts") < 1000000000)
					&& (AppUtility.checkIntNullKey(jsonObject, "external_power_volts") > 7)) {
				deviceStatus = "External";
				deviceStatusAll = deviceStatusAll + "External, ";
			} else {
				deviceStatus = "Battery";
				deviceStatusAll = deviceStatusAll + "Battery, ";
			}
		}

		deviceReportPayload.setPower(deviceStatus);

		if (jsonObject != null) {
			if ((AppUtility.checkNullKey(jsonObject, "trip") != null ? AppUtility.checkNullKey(jsonObject, "trip") : "")
					.equals("On")) {
				deviceReportPayload.setInTrip("In Trip");
				deviceStatusAll = deviceStatusAll + "In Trip, ";
			} else {
				deviceReportPayload.setInTrip("Not In Trip");
				deviceStatusAll = deviceStatusAll + "Not In Trip, ";
			}
			if ((AppUtility.checkNullKey(jsonObject, "ignition") != null ? AppUtility.checkNullKey(jsonObject, "trip")
					: "").equals("On")) {
				deviceReportPayload.setIgnition("Ignition On");
				deviceStatusAll = deviceStatusAll + "Ignition On, ";
			} else {
				deviceReportPayload.setIgnition("Ignition Off");
				deviceStatusAll = deviceStatusAll + "Ignition Off, ";
			}
			if ((AppUtility.checkNullKey(jsonObject, "motion") != null ? AppUtility.checkNullKey(jsonObject, "trip")
					: "").equals("On")) {
				deviceReportPayload.setInMotion("In Motion");
				deviceStatusAll = deviceStatusAll + "In Motion, ";
			} else {
				deviceReportPayload.setInMotion("Not In Motion");
				deviceStatusAll = deviceStatusAll + "Not In Motion, ";
			}
			if ((AppUtility.checkNullKey(jsonObject, "tamper") != null ? AppUtility.checkNullKey(jsonObject, "trip")
					: "").equals("On")) {
				deviceReportPayload.setTamper("Tampered");
				deviceStatusAll = deviceStatusAll + "Tampered";
			} else {
				deviceReportPayload.setTamper("Not Tampered");
				deviceStatusAll = deviceStatusAll + "Not Tampered";
			}
		}
		deviceReportPayload.setDeviceStatus(deviceStatusAll);

		return deviceReportPayload;
	}

	public String getOrganisationByCanNumber(List<Organisation> organisationList, JSONObject general) {
		String id = general != null ? AppUtility.checkNullKey(general, "company_id") : null;
		if (id != null && id.length() > 0) {
			Organisation org = organisationList.stream().filter(e -> e.getAccountNumber().equals(id)).findAny()
					.orElse(null);
			if (org != null) {
				return org.getOrganisationName();
			}
		}
		return null;
	}

	public String getEventByEventId(List<Event> eventList, JSONObject reportHeader) {
		int id = reportHeader != null ? AppUtility.checkIntNullKey(reportHeader, "event_id") : 0;
		if (id > 0) {
			Event event = eventList.stream().filter(e -> e.getEventId() == id).findAny().orElse(null);
			if (event != null) {
				return event.getEventType();
			}
		}
		return null;
	}

	public void writeFilterCSVFileFromElsatic(String csvFileName,
			List<List<DeviceReportPayload>> gatewayDetailPayLoadList, HttpServletResponse response, User user,
			int totalSize, List<String> columnDef) throws IOException {
		response.reset();
		Random rnd = new Random();
		int number = rnd.nextInt(999999);
		File theDir = new File("/files");
		if (!theDir.exists()) {
			theDir.mkdirs();
		}

//		String[] header = new String[] { "Customer", "DeviceID", "VIN", "AssetID", "Event", "EventId", "ReceivedTime",
//				"Seq", "RTCTime", "GPSTime", "GPS", "Lat", "Long", "LatLong", "MainV", "AltV", "BatrV", "ChargingV",
//				"DeviceStatus", "Power", "InTrip", "Ignition", "InMotion", "Tamper", "Altitude", "MPH", "KPH",
//				"Heading", "Odometer", "Fuel", "ODTMILMiles", "ODTDTCClearMiles", "AccInfo", "AccX", "AccY", "AccZ",
//				"AccCalibX", "AccCalibY", "AccCalibZ", "Orientation", "OrientX", "OrientY", "OrientZ", "IntTemp",
//				"AmbientTemp", "RSSI", "HDOP", "Sats", "Service", "Roaming", "Country", "Network", "TowerID",
//				"CentroidLat", "CentroidLong", "Band", "RxTxEc", "APPVer", "OSVer", "HWVer", "HWRev", "IOVer",
//				"ExtenderVer", "BLEVer", "ConfigChg", "Config", "ConfigDesc", "Devdefcfg", "DevdefcfgCRC", "Devdef2cfg",
//				"Devdef2cfgCRC", "Devdef3cfg", "Devdef3cfgCRC", "Devdef4cfg", "Devdef4cfgCRC", "Devusrcfg",
//				"DevusrcfgCRC", "GPIO", "TFTPStatus", "PeripheralVersion", "ABS", "ABSOdometer", "ABSInfo", "AirSupply",
//				"ATIS", "WheelEnd", "SFKWheelEnd", "Receiver", "LOF1", "LOF1Temp", "LOF1Psi", "LIF2", "LIF2Temp",
//				"LIF2Psi", "RIF3", "RIF3Temp", "RIF3Psi", "ROF4", "ROF4Temp", "ROF4Psi", "LOR5", "LOR5Temp", "LOR5Psi",
//				"LIR6", "LIR6Temp", "LIR6Psi", "RIR7", "RIR7Temp", "RIR7Psi", "ROR8", "ROR8Temp", "ROR8Psi", "BetaTPMS",
//				"BeaconSmartPair", "BrakeStroke", "ChassisCargo", "Thumbnail", "State", "Loaded",
//				"ConfidenceRating", "Door", "DoorWireless", "LightOut", "Reefer", "CommStatus", "SensorId",
//				"AgeOfReading", "SensorStatus", "Humidity", "Temperature", "BatteryLevel", "BleSensorStatus",
//				"BleTemperatureReadings", "ACKn", "DeviceIP", "RawReport" };
		String[] header = columnDef.toArray(new String[0]);
		if (totalSize > 10000) {
			if (user != null) {
				ICsvBeanWriter csvWriter = new CsvBeanWriter(new FileWriter("files/alldeviceReport" + number + ".csv"),
						CsvPreference.STANDARD_PREFERENCE);
				csvWriter.writeHeader(header);
				logger.info("Writing data into csv file, size of file:- " + gatewayDetailPayLoadList.size());
				for (Iterator<List<DeviceReportPayload>> iterator = gatewayDetailPayLoadList.iterator(); iterator
						.hasNext();) {
					List<DeviceReportPayload> list = (List<DeviceReportPayload>) iterator.next();
					logger.info("Size of inner list" + list.size());
					for (DeviceReportPayload gatewayDetailPayLoad : list) {
						csvWriter.write(gatewayDetailPayLoad, header);
					}
				}
				csvWriter.close();
				if (user != null) {
					logger.info("Send the donwload url on the mail");
					mailUtil.sendMail(user, "files/alldeviceReport" + number + ".csv", false,
							"alldeviceReport" + number + ".csv");
				}
				logger.info("alldeviceReport" + number + ".csv" + " file created success fully");
			}
		} else {
			ICsvBeanWriter csvWriter2 = new CsvBeanWriter(response.getWriter(), CsvPreference.STANDARD_PREFERENCE);
			csvWriter2.writeHeader(header);
			for (Iterator<List<DeviceReportPayload>> iterator = gatewayDetailPayLoadList.iterator(); iterator
					.hasNext();) {
				List<DeviceReportPayload> list = (List<DeviceReportPayload>) iterator.next();
				for (DeviceReportPayload gatewayDetailPayLoad : list) {
					csvWriter2.write(gatewayDetailPayLoad, header);
				}
			}
			csvWriter2.close();
		}
	}

	public String preprarePeripheralVersion(JSONObject peripheralVersionJsonObject) {
		String peripheralVersion = "";
		if (peripheralVersionJsonObject != null) {
			JSONObject liteSentry = AppUtility.checkJSONObjectNullKey(peripheralVersionJsonObject, "lite_sentry");
			if (liteSentry != null) {
				String sensorStatus = AppUtility.checkNullKey(liteSentry, "sensor_status");
				String dataSize = AppUtility.checkNullKey(liteSentry, "data_size");
				String hardwareId = AppUtility.checkNullKey(liteSentry, "hardware_id");
				String hardwareVersion = AppUtility.checkNullKey(liteSentry, "hardware_version");
				String appVersion = AppUtility.checkNullKey(liteSentry, "app_version");
				String bootVersion = AppUtility.checkNullKey(liteSentry, "boot_version");
				if (sensorStatus != null && sensorStatus.equalsIgnoreCase("Online")) {
					peripheralVersion = "Sensor Type : Lite Sentry; Sensor Status: " + sensorStatus + "; Size :"
							+ dataSize + ";Hardware id :" + hardwareId + ";Hardware Version :" + hardwareVersion
							+ "; App Version :" + appVersion + ";Boot Version :" + bootVersion + ";";
				} else {
					peripheralVersion = "Sensor Type : Lite Sentry; Sensor Status: " + sensorStatus + ";";
				}
			}
			JSONObject maxbotixCargoSensor = AppUtility.checkJSONObjectNullKey(peripheralVersionJsonObject,
					"maxbotix_cargo_sensor");
			if (maxbotixCargoSensor != null) {
				String sensorStatus = AppUtility.checkNullKey(maxbotixCargoSensor, "sensor_status");
				String dataSize = AppUtility.checkNullKey(maxbotixCargoSensor, "data_size");
				String firmwareVersion = AppUtility.checkNullKey(maxbotixCargoSensor, "firmware_version");
				String hardwareVersion = AppUtility.checkNullKey(maxbotixCargoSensor, "hardware_version");
				if (sensorStatus != null && sensorStatus.equalsIgnoreCase("Online")) {
					peripheralVersion = peripheralVersion + "Sensor Type : Maxbotix Cargo Sensor; Sensor Status: "
							+ sensorStatus + "; Size :" + dataSize + "; Hardware Version :" + hardwareVersion
							+ "; Firmware Version :" + firmwareVersion + ";";
				} else {
					peripheralVersion = "Sensor Type : Maxbotix Cargo Sensor; Sensor Status: " + sensorStatus + ";";
				}
			}
			JSONObject steReceiver = AppUtility.checkJSONObjectNullKey(peripheralVersionJsonObject, "ste_receiver");
			if (steReceiver != null) {
				String sensorStatus = AppUtility.checkNullKey(steReceiver, "sensor_status");
				String dataSize = AppUtility.checkNullKey(steReceiver, "data_size");
				String mcuApp = AppUtility.checkNullKey(steReceiver, "mcu_app");
				String zeekiApp = AppUtility.checkNullKey(steReceiver, "zeeki_app");
				if (sensorStatus != null && sensorStatus.equalsIgnoreCase("Online")) {
					peripheralVersion = peripheralVersion + "Sensor Type : STE TPMS Receiver; Sensor Status: "
							+ sensorStatus + "; Size :" + dataSize + ";MCU App :" + mcuApp + ";Zeeki App :" + zeekiApp
							+ ";";
				} else {
					peripheralVersion = "Sensor Type : STE TPMS Receiver; Sensor Status: " + sensorStatus + ";";
				}
			}
			JSONObject riotCargoChassisSensor = AppUtility.checkJSONObjectNullKey(peripheralVersionJsonObject,
					"riot_cargo_or_chassis_sensor");
			if (riotCargoChassisSensor != null) {
				String sensorStatus = AppUtility.checkNullKey(riotCargoChassisSensor, "sensor_status");
				String dataSize = AppUtility.checkNullKey(riotCargoChassisSensor, "data_size");
				String hardwareVersion = AppUtility.checkNullKey(riotCargoChassisSensor, "hardware_version");
				String firmwareVersion = AppUtility.checkNullKey(riotCargoChassisSensor, "firmware_version");
				if (sensorStatus != null && sensorStatus.equalsIgnoreCase("Online")) {
					peripheralVersion = peripheralVersion + "Sensor Type : Riot Cargo/Chassis Sensor; Sensor Status: "
							+ sensorStatus + "; Size :" + dataSize + ";Hardware Version :" + hardwareVersion
							+ ";Firmware Version :" + firmwareVersion + ";";
				} else {
					peripheralVersion = peripheralVersion + "Sensor Type : Riot Cargo/Chassis Sensor; Sensor Status: "
							+ sensorStatus + ";";
				}
			}
			JSONObject hegemonAbsreader = AppUtility.checkJSONObjectNullKey(peripheralVersionJsonObject,
					"hegemon_absreader");
			if (hegemonAbsreader != null) {
				String sensorStatus = AppUtility.checkNullKey(hegemonAbsreader, "sensor_status");
				String dataSize = AppUtility.checkNullKey(hegemonAbsreader, "data_size");
				String version = AppUtility.checkNullKey(hegemonAbsreader, "version");
				if (sensorStatus != null && sensorStatus.equalsIgnoreCase("Online")) {
					peripheralVersion = peripheralVersion + "Sensor Type : Hegemon PLC Reader; Sensor Status: "
							+ sensorStatus + ";Size :" + dataSize + ";Version:" + version + ";";
				} else {
					peripheralVersion = peripheralVersion + "Sensor Type : Hegemon PLC Reader; Sensor Status: "
							+ sensorStatus + ";";
				}
			}
			JSONObject absReader = AppUtility.checkJSONObjectNullKey(peripheralVersionJsonObject, "abs_reader");
			if (absReader != null) {
				String sensorStatus = AppUtility.checkNullKey(absReader, "sensor_status");
				String dataSize = AppUtility.checkNullKey(absReader, "data_size");
				if (sensorStatus != null && sensorStatus.equalsIgnoreCase("Online")) {
					peripheralVersion = peripheralVersion + "Sensor Type : ABS Reader; Sensor Status: " + sensorStatus
							+ "; Size :" + dataSize + ";";
				} else {
					peripheralVersion = peripheralVersion + "Sensor Type : ABS Reader; Sensor Status: " + sensorStatus
							+ ";";
				}
			}
			JSONObject temperatureSensor = AppUtility.checkJSONObjectNullKey(peripheralVersionJsonObject,
					"temperature_sensor");
			if (temperatureSensor != null) {
				String sensorStatus = AppUtility.checkNullKey(temperatureSensor, "sensor_status");
				String dataSize = AppUtility.checkNullKey(temperatureSensor, "data_size");
				if (sensorStatus != null && sensorStatus.equalsIgnoreCase("Online")) {
					peripheralVersion = peripheralVersion + "Sensor Type : BLE PCT Temp Sensor; Sensor Status: "
							+ sensorStatus + "; Size :" + dataSize + ";";
				} else {
					peripheralVersion = peripheralVersion + "Sensor Type : BLE PCT Temp Sensor; Sensor Status: "
							+ sensorStatus + ";";
				}
			}
			JSONObject lampCheckAtis = AppUtility.checkJSONObjectNullKey(peripheralVersionJsonObject,
					"lamp_check_atis");
			if (lampCheckAtis != null) {
				String sensorStatus = AppUtility.checkNullKey(lampCheckAtis, "sensor_status");
				String dataSize = AppUtility.checkNullKey(lampCheckAtis, "data_size");
				if (sensorStatus != null && sensorStatus.equalsIgnoreCase("Online")) {
					peripheralVersion = peripheralVersion + "Sensor Type : Lamp Check ATIS; Sensor Status: "
							+ sensorStatus + "; Size :" + dataSize + ";";
				} else {
					peripheralVersion = "Sensor Type : Lamp Check ATIS; Sensor Status: " + sensorStatus + ";";
				}
			}
			JSONObject doorSensor = AppUtility.checkJSONObjectNullKey(peripheralVersionJsonObject, "door_sensor");
			if (doorSensor != null) {
				String sensorStatus = AppUtility.checkNullKey(doorSensor, "sensor_status");
				String dataSize = AppUtility.checkNullKey(doorSensor, "data_size");
				if (sensorStatus != null && sensorStatus.equalsIgnoreCase("Online")) {
					peripheralVersion = peripheralVersion + "Sensor Type : BLE PCT Door Sensor; Sensor Status: "
							+ sensorStatus + "; Size :" + dataSize + ";";
				} else {
					peripheralVersion = peripheralVersion + "Sensor Type : BLE PCT Door Sensor; Sensor Status: "
							+ sensorStatus + ";";
				}
			}
		}
		return peripheralVersion;
	}

	public void writeFilterCSVFileFromDeviceSummary(String csvFileName,
			Page<GatewaySummaryPayload> gatewaySummaryPayload, HttpServletResponse response) throws IOException {
		response.reset();
		ICsvBeanWriter csvWriter = new CsvBeanWriter(response.getWriter(), CsvPreference.STANDARD_PREFERENCE);

		String[] header = new String[] { "OrganisationName", "Smart7", "TraillerNet", "SmartPair", "StealthNet",
				"Sabre", "FreightLa", "ArrowL", "FreightL", "CutlassL", "Dagger67Lg", "SmartSeven", "KatanaH" };
		csvWriter.writeHeader(header);
		for (GatewaySummaryPayload gatewayDetailPayLoad : gatewaySummaryPayload) {
			csvWriter.write(gatewayDetailPayLoad, header);
		}
		csvWriter.close();
	}

	public List<Map<String, Object>> getCustomerForwardingRulesByOrganizationUuids(Set<String> organizationUuids) {
		try {
			logger.info(
					"Inside RestUtils Class getCustomerForwardingRulesByOrganizationUuids method and fetching customer forwarding rules by organization uuids",
					organizationUuids);
			Application application = eurekaClient.getApplication(deviceServiceId);
			InstanceInfo instanceInfo = application.getInstances().get(0);
			String token = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest()
					.getHeader("Authorization");
			String url = "http://" + instanceInfo.getIPAddr() + ":" + instanceInfo.getPort()
					+ "/customer/forwarding/rules?organizationUuids="
					+ organizationUuids.stream().collect(Collectors.joining(","));
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.set("Authorization", token);
			HttpEntity<String> entity = new HttpEntity<String>(headers);
			ResponseEntity<List> customerForwardingRulesResponseEntity = restTemplate.exchange(url, HttpMethod.GET,
					entity, List.class);
			if (customerForwardingRulesResponseEntity.getStatusCode().equals(HttpStatus.OK)) {
				List<Map<String, Object>> customerForwardingRules = customerForwardingRulesResponseEntity.getBody();
				return customerForwardingRules;
			} else {
				logger.error(
						"Error Inside RestUtils Class getCustomerForwardingRulesByOrganizationUuids method :- Http call not successful");
				throw new InterServiceRestException("Http call not successful",
						customerForwardingRulesResponseEntity.getStatusCode());
			}
		} catch (Exception ex) {
			logger.error("Exception Inside RestUtils Class getCustomerForwardingRulesByOrganizationUuids method", ex);
			throw new InterServiceRestException(
					"Exception while calling the http call get customer forwarding rules by organization uuids, Exception:-"
							+ ex.getMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	public Organisation getCompanyById(Long id) {
		try {
			logger.info("Inside RestUtils Class getCompanyById method and fetching Company Details and id value", id);
			Application application = eurekaClient.getApplication(deviceServiceId);
			InstanceInfo instanceInfo = application.getInstances().get(0);
			String token = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest()
					.getHeader("Authorization");
			String url = "http://" + instanceInfo.getIPAddr() + ":" + instanceInfo.getPort() + "/organisation/id?id="
					+ id;
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.set("Authorization", token);
			HttpEntity<String> entity = new HttpEntity<String>(headers);
			ResponseEntity<Organisation> companyResponseEntity = restTemplate.exchange(url, HttpMethod.GET, entity,
					Organisation.class);
			if (companyResponseEntity.getStatusCode().equals(HttpStatus.OK)) {
				Organisation company = companyResponseEntity.getBody();
				return company;
			} else {
				logger.error("Error Inside RestUtils Class getCompanyById method :- Http call not successful");
				throw new InterServiceRestException("Http call not successful", companyResponseEntity.getStatusCode());
			}
		} catch (Exception ex) {
			logger.error("Exception Inside RestUtils Class getCompanyById method", ex);
			throw new InterServiceRestException(
					"Exception while calling the http call get company by id, Exception:-" + ex.getMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	public List<Map<String, Object>> getAllCustomerForwardingRuleUrl() {
		try {
			logger.info(
					"Inside RestUtils Class getAllCustomerForwardingRuleUrl method and fetching all customer forwarding rule url");
			Application application = eurekaClient.getApplication(deviceServiceId);
			InstanceInfo instanceInfo = application.getInstances().get(0);
			String token = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest()
					.getHeader("Authorization");
			String url = "http://" + instanceInfo.getIPAddr() + ":" + instanceInfo.getPort()
					+ "/customer/forwarding/rule/urls";
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.set("Authorization", token);
			HttpEntity<String> entity = new HttpEntity<String>(headers);
			ResponseEntity<List> customerForwardingRulesResponseEntity = restTemplate.exchange(url, HttpMethod.GET,
					entity, List.class);
			if (customerForwardingRulesResponseEntity.getStatusCode().equals(HttpStatus.OK)) {
				List<Map<String, Object>> customerForwardingRules = customerForwardingRulesResponseEntity.getBody();
				return customerForwardingRules;
			} else {
				logger.error(
						"Error Inside RestUtils Class getAllCustomerForwardingRuleUrl method :- Http call not successful");
				throw new InterServiceRestException("Http call not successful",
						customerForwardingRulesResponseEntity.getStatusCode());
			}
		} catch (Exception ex) {
			logger.error("Exception Inside RestUtils Class getAllCustomerForwardingRuleUrl method", ex);
			throw new InterServiceRestException(
					"Exception while calling the http call get customer forwarding rule all url Exception:-"
							+ ex.getMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	public List<CustomerForwardingRuleUrlDTO> getAllCustomerForwardingRuleUrl(String token) {
		try {
			logger.info("Inside RestUtils Class getAllCustomerForwardingRuleUrl method");
			Application application = eurekaClient.getApplication(deviceServiceId);
			InstanceInfo instanceInfo = application.getInstances().get(0);
			String url = "http://" + instanceInfo.getIPAddr() + ":" + instanceInfo.getPort()
					+ "/customer/forwarding/rule/urls";
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.set("Authorization", token);
			HttpEntity<String> entity = new HttpEntity<String>(headers);
			ResponseEntity<List<CustomerForwardingRuleUrlDTO>> customerFwdRuleUrls = restTemplate.exchange(url,
					HttpMethod.GET, entity, new ParameterizedTypeReference<List<CustomerForwardingRuleUrlDTO>>() {
					});
			if (customerFwdRuleUrls.getStatusCode().equals(HttpStatus.OK)) {
				List<CustomerForwardingRuleUrlDTO> CustomerForwardingRuleUrlDTO = customerFwdRuleUrls.getBody();
				return CustomerForwardingRuleUrlDTO;
			} else {
				logger.error(
						"Error Inside RestUtils Class getAllCustomerForwardingRuleUrl method :- Http call not successful");
				throw new InterServiceRestException("Http call not successful", customerFwdRuleUrls.getStatusCode());
			}
		} catch (Exception ex) {
			logger.error("Exception Inside RestUtils Class getAllCustomerForwardingRuleUrl method", ex);
			throw new InterServiceRestException(
					"Exception while calling the http call Get /customer/forwarding/rule/urls Exception:-"
							+ ex.getMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	public List<CustomerForwardingRuleDTO> getAllCustomerForwardingRules(String token) {
		try {
			logger.info("Inside RestUtils Class getAllCustomerForwardingRules method");
			Application application = eurekaClient.getApplication(deviceServiceId);
			InstanceInfo instanceInfo = application.getInstances().get(0);
			String url = "http://" + instanceInfo.getIPAddr() + ":" + instanceInfo.getPort()
					+ "/customer/forwarding/rules/customer-forwarding";
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.set("Authorization", token);
			HttpEntity<String> entity = new HttpEntity<String>(headers);
			ResponseEntity<List<CustomerForwardingRuleDTO>> customerFwdRules = restTemplate.exchange(url,
					HttpMethod.GET, entity, new ParameterizedTypeReference<List<CustomerForwardingRuleDTO>>() {
					});
			if (customerFwdRules.getStatusCode().equals(HttpStatus.OK)) {
				List<CustomerForwardingRuleDTO> customerForwardingRules = customerFwdRules.getBody();
				return customerForwardingRules;
			} else {
				logger.error(
						"Error Inside RestUtils Class getAllCustomerForwardingRules method :- Http call not successful");
				throw new InterServiceRestException("Http call not successful", customerFwdRules.getStatusCode());
			}
		} catch (Exception ex) {
			logger.error("Exception Inside RestUtils Class getAllCustomerForwardingRules method", ex);
			throw new InterServiceRestException(
					"Exception while calling the http call Get /customer/forwarding/rules/customer-forwarding Exception:-"
							+ ex.getMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	public List<InstalledHistoryResponsePayload> getInstalledHistoryResponseByDeviceImei(Set<String> deviceImeiList) {
		try {
			logger.info("Inside RestUtils Class getInstalledHistoryResponseByDeviceImei method",
					deviceImeiList.toString());
			Application application = eurekaClient.getApplication(deviceServiceId);
			InstanceInfo instanceInfo = application.getInstances().get(0);
			String url = "http://" + instanceInfo.getIPAddr() + ":" + instanceInfo.getPort()
					+ "/installation/get-installed-history";
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			String token = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest()
					.getHeader("Authorization");
			headers.set("Authorization", token);
			HttpEntity<Set<String>> entity = new HttpEntity<Set<String>>(deviceImeiList, headers);
			ResponseEntity<ResponseBodyDTO> response = restTemplate.postForEntity(url, entity, ResponseBodyDTO.class);
			if (response.getStatusCode().equals(HttpStatus.OK)) {
				logger.info("Installed History response" + response.getBody().toString());
				ObjectMapper mapper = new ObjectMapper();
				mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
				mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
//				mapper.registerModule(new JavaTimeModule());

				List<InstalledHistoryResponsePayload> deviceCommandResponse = mapper.convertValue(
						response.getBody().getBody(), new TypeReference<List<InstalledHistoryResponsePayload>>() {
						});
				return deviceCommandResponse;
			} else {
				logger.error(
						"Error Inside RestUtils Class getInstalledHistoryResponseByDeviceImei method :- Http call not successful");
				throw new InterServiceRestException("Http call not successful", response.getStatusCode());
			}
		} catch (Exception ex) {
			logger.error("Exception Inside RestUtils Class getInstalledHistoryResponseByDeviceImei method", ex);
			throw new InterServiceRestException(
					"Exception while calling the http call Post /installation/get-installed-history Exception:-"
							+ ex.getMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	public List<SensorInstallInstruction> getSensorInstallInstruction(String productName) {

		ResponseEntity<ResponseBodyDTO<List<SensorInstallInstruction>>> sensorInstallInstructionResponseEntity = installerServiceFeignClient
				.getSensorInstallInstruction(productName);

		if (sensorInstallInstructionResponseEntity.getStatusCode().equals(HttpStatus.OK)) {
			List<SensorInstallInstruction> sensorInstallInstruction = sensorInstallInstructionResponseEntity.getBody()
					.getBody();
			return sensorInstallInstruction;
		} else {
			throw new InterServiceRestException("Http call not successful",
					sensorInstallInstructionResponseEntity.getStatusCode());
		}
	}

	public List<SensorReasonCode> getSensorReasonCode(String productName) {

		ResponseEntity<ResponseBodyDTO<List<SensorReasonCode>>> sensorReasonCodeResponseEntity = installerServiceFeignClient
				.getSensorReasonCode(productName);

		if (sensorReasonCodeResponseEntity.getStatusCode().equals(HttpStatus.OK)) {
			List<SensorReasonCode> sensorReasonCode = sensorReasonCodeResponseEntity.getBody().getBody();
			return sensorReasonCode;
		} else {
			throw new InterServiceRestException("Http call not successful",
					sensorReasonCodeResponseEntity.getStatusCode());
		}
	}

}
