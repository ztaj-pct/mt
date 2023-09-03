package com.pct.device.version.util;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.exception.ExceptionUtils;
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
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.UriComponentsBuilder;
import org.supercsv.io.CsvBeanWriter;
import org.supercsv.io.ICsvBeanWriter;
import org.supercsv.prefs.CsvPreference;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.shared.Application;
import com.pct.common.dto.MsDeviceRestResponse;
import com.pct.common.exception.InterServiceRestException;
import com.pct.common.model.User;
import com.pct.device.version.dto.MessageDTO;
import com.pct.device.version.model.Package;
import com.pct.device.version.payload.ExecuteCampaignRequest;
import com.pct.device.version.payload.PackagePayload;

@Component
public class RestUtils {
    Logger logger = LoggerFactory.getLogger(RestUtils.class);
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private EurekaClient eurekaClient;
    @Value("${service.gateway.serviceId}")
    private String gatewayServiceId;
    
	@Value("${service.gateway.serviceId}")
	private String deviceServiceId;
	
	@Autowired
    private KafkaTemplate<String,ExecuteCampaignRequest> kafkaTemplate;


    private final ObjectMapper objectMapper = new ObjectMapper();

    Map<String,String> NAME_REPLACE=new HashMap<>() ;
    
    
    public User getUserFromAuthService(Long userId) {
        Application application = eurekaClient.getApplication(gatewayServiceId);
        InstanceInfo instanceInfo = application.getInstances().get(0);
        String url = "http://" + instanceInfo.getIPAddr() + ":" + instanceInfo.getPort() + "/user/get/" + userId;
        
        String token = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest()
				.getHeader("Authorization");
         HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.ALL));
		headers.set("Authorization", token);
  		//HttpEntity<String> entity = new HttpEntity<String>(personList,headers);
		HttpEntity<Object> entity = new HttpEntity<Object>(headers);
		
		ResponseEntity<MessageDTO>  responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity,  MessageDTO.class);
        
        
     //   ResponseEntity<MessageDTO> userResponseEntity = restTemplate.getForEntity(url, MessageDTO.class);
        if (responseEntity.getStatusCode().equals(HttpStatus.OK)) {
            User user = objectMapper.convertValue(responseEntity.getBody().getBody(), User.class);
            return user;
        } else {
            throw new InterServiceRestException
                    ("Http call not successful", responseEntity.getStatusCode());
        }
    }
    
    // get devices by customer name
//    public List<MsDeviceRestResponse> getDevicesFromMSByCustomerName(String customerName) {
//        Application application = eurekaClient.getApplication(gatewayServiceId);
//        InstanceInfo instanceInfo = application.getInstances().get(0);
//        String token = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest()
//				.getHeader("Authorization");
//		if (customerName != null) {
//			customerName = customerName.trim();
//		}
//        String url = "http://" +instanceInfo.getIPAddr()+ ":" + instanceInfo.getPort() + "/device/ms-device/"+ customerName;
////        ResponseEntity<ArrayList> deviceResponseEntity = restTemplate.getForEntity(url, ArrayList.class);
//        HttpHeaders headers = new HttpHeaders();
//		headers.setAccept(Arrays.asList(MediaType.ALL));
//		headers.set("Authorization", token);
//		HttpEntity<Object> entity = new HttpEntity<Object>(customerName,headers);
//		 ResponseEntity<MsDeviceRestResponse[]> deviceResponseEntity = restTemplate.exchange(url, HttpMethod.GET, entity, MsDeviceRestResponse[].class);
//       // ResponseEntity<DeviceRestData[]> deviceResponseEntity = restTemplate.getForEntity(url, DeviceRestData[].class);
//        if (deviceResponseEntity.getStatusCode().equals(HttpStatus.OK)) {
//            List<MsDeviceRestResponse> deviceList = Arrays.asList(deviceResponseEntity.getBody());
//            return deviceList;
//        } else {
//            throw new InterServiceRestException
//                    ("Http call not successful", deviceResponseEntity.getStatusCode());
//        }
//    }
    
    // Get Campaign Installed Devices by imei
//    public CampaignInstalledDevice CampaignInstalledDeviceFromMS(String imei) {
//    	
//    	Application application = eurekaClient	.getApplication(gatewayServiceId);
//        InstanceInfo instanceInfo = application.getInstances().get(0);
//    	String token = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest()
//				.getHeader("Authorization");
//        String url = "http://" + instanceInfo.getIPAddr()  + ":" + instanceInfo.getPort() + "/device/ms-device/getCampaignInstalledDevice/"+imei;
//        HttpHeaders headers = new HttpHeaders();
//		headers.setAccept(Arrays.asList(MediaType.ALL));
//		headers.set("Authorization", token);
//		HttpEntity<Object> entity = new HttpEntity<Object>(imei,headers);
//
// //         ResponseEntity<ArrayList> deviceResponseEntity = restTemplate.getForEntity(url, ArrayList.class);
//        ResponseEntity<CampaignInstalledDevice> deviceResponseEntity = restTemplate.exchange(url, HttpMethod.GET, entity, CampaignInstalledDevice.class);
//		//ResponseEntity<DeviceRestData[]> responseEntity = restTemplate.exchange(url, HttpMethod.POST, entity,  DeviceRestData[].class);
//
//        if (deviceResponseEntity.getStatusCode().equals(HttpStatus.OK)) {
//        	CampaignInstalledDevice campaignInstalledDevice = objectMapper.convertValue(deviceResponseEntity.getBody(), CampaignInstalledDevice.class);
//            return campaignInstalledDevice;
//        } else {
//            throw new InterServiceRestException
//                    ("Http call not successful", deviceResponseEntity.getStatusCode());
//        }
//    }

    
//    public List<DeviceRestData>  getSelectedDeviceDataFromMS(List<String> imeiList) {
//        Application application = eurekaClient.getApplication(gatewayServiceId);
//        InstanceInfo instanceInfo = application.getInstances().get(0);
//        String url = "http://" + instanceInfo.getIPAddr() + ":" + instanceInfo.getPort() + "/device/core/ms-device/getSelectedDevices";
//        ResponseEntity<DeviceRestData[]> responseEntity = restTemplate.postForEntity(url, imeiList, DeviceRestData[].class);
//        if (responseEntity.getStatusCode().equals(HttpStatus.OK)) {
//        	 List<DeviceRestData> deviceList= Arrays.asList(responseEntity.getBody());
//            return deviceList;
//        } else {
//            throw new InterServiceRestException
//                    ("Http call not successful", responseEntity.getStatusCode());
//        }
//    }
    
//    public List<DeviceReport>  getLastMaintReportFromMS(List<String> imeiList) {
//        Application application = eurekaClient.getApplication(gatewayServiceId);
//        InstanceInfo instanceInfo = application.getInstances().get(0);
//        String url = "http://" + instanceInfo.getIPAddr() + ":" + instanceInfo.getPort() + "/device/core/ms-device/getLastMaintReports";
//        ResponseEntity<DeviceReport[]> responseEntity = restTemplate.postForEntity(url, imeiList, DeviceReport[].class);
//        if (responseEntity.getStatusCode().equals(HttpStatus.OK)) {
//        	 List<DeviceReport> deviceReports= Arrays.asList(responseEntity.getBody());
//            return deviceReports;
//        } else {
//            throw new InterServiceRestException
//                    ("Http call not successful", responseEntity.getStatusCode());
//        }
//    }

    public void writeCSVFile(String csvFileName, List<Package> packageList, HttpServletResponse response) throws IOException {
        logger.info("Inside writeCSVFile in RestUtils.");
        ICsvBeanWriter csvWriter = new CsvBeanWriter(response.getWriter(),
                CsvPreference.STANDARD_PREFERENCE);
        Field[] declaredFields = Package.class.getDeclaredFields();
        List<String> attributesList=new ArrayList<>();
        for(Field field:declaredFields){
            attributesList.add(field.getName().toUpperCase());
        }
        attributesList.removeAll(Constants.INTERSECTED_LIST);
         
        logger.info("Size Of Difference attributesList List Is "+attributesList.size());
         String[] header=new String[setHeader(attributesList).size()];
        header = attributesList.toArray(header);

        csvWriter.writeHeader(header);
        for (Package aPackage : packageList) {
            csvWriter.write(aPackage, header);
        }
        csvWriter.close();
        logger.info("CSV Has been written Successfully.");
    }
    public void writeFilterCSVFile(String csvFileName, List<PackagePayload> packageList, HttpServletResponse response) throws IOException {
        logger.info("Inside writeFilterCSVFile in RestUtils.");
        ICsvBeanWriter csvWriter = new CsvBeanWriter(response.getWriter(),
                CsvPreference.STANDARD_PREFERENCE);
        Field[] declaredFields = Package.class.getDeclaredFields();
        List<String> attributesList=new ArrayList<>();
        for(Field field:declaredFields){
            attributesList.add(field.getName().toUpperCase());
        }
        List<String> differences = new ArrayList<>
                (Sets.difference(Sets.newHashSet(attributesList), Sets.newHashSet(Constants.INTERSECTED_LIST)));
        logger.info("Size Of Difference List Is "+differences.size());
        //List<String> columnForGUI=setHeader(differences);
        String[] header=new String[setHeader(differences).size()];
        header=differences.toArray(header);

        csvWriter.writeHeader(header);
        for (PackagePayload aPackage : packageList) {
            csvWriter.write(aPackage, header);
        }
        csvWriter.close();
        logger.info("Filtered CSV Has been written Successfully.");
    }
    public List<String> setHeader(List<String> header){
        header.set(0,"PACKAGENAME");
        header.set(1,"CREATEDAT");
        header.set(2,"BINVERSION");
        header.set(3,"APPVERSION");
        header.set(4,"MCUVERSION");
        header.set(5,"BLEVERSION");
        header.set(6,"CONFIG1");
        header.set(7,"CONFIG1CRC");
        header.set(8,"CONFIG2");
        header.set(9,"CONFIG2CRC");
        header.set(10,"CONFIG3");
        header.set(11,"CONFIG3CRC");
        header.set(12,"CONFIG4");
        header.set(13,"CONFIG4CRC");
        return header;
    }
    public String newValue(String oldValue){
        NAME_REPLACE.put("PACKAGENAME","Name");
        NAME_REPLACE.put("CREATEDAT","Created Time UTC");
        NAME_REPLACE.put("BINVERSION","BIN");
        NAME_REPLACE.put("APPVERSION","App");
        NAME_REPLACE.put("MCUVERSION","MCU");
        NAME_REPLACE.put("CONFIG1","Config1");
        NAME_REPLACE.put("CONFIG2","Config2");
        NAME_REPLACE.put("CONFIG3","Config3");
        NAME_REPLACE.put("CONFIG4","Config4");
        NAME_REPLACE.put("CONFIG1CRC","Config1_crc");
        NAME_REPLACE.put("CONFIG2CRC","Config2_crc");
        NAME_REPLACE.put("CONFIG3CRC","Config3_crc");
        NAME_REPLACE.put("CONFIG4CRC","Config4_crc");
        NAME_REPLACE.put("BLEVERSION","BLE");
        return NAME_REPLACE.get(oldValue);
    }

    
//    public String getDeviceInstallationStatus(String imei) {
//        Application application = eurekaClient.getApplication(gatewayServiceId);
//        InstanceInfo instanceInfo = application.getInstances().get(0);
//        String url = "http://" + instanceInfo.getIPAddr() + ":" + instanceInfo.getPort() + "/device/core/status/" + imei;
//        ResponseEntity<ResponseDTO> responseEntity = restTemplate.getForEntity(url, ResponseDTO.class);
//        if (responseEntity.getStatusCode().equals(HttpStatus.OK)) {
//            return responseEntity.getBody().getMessage();
//        } else {
//            throw new InterServiceRestException
//                    ("Http call not successful", responseEntity.getStatusCode());
//        }
//    }


    
	// Get User by username 
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


	// get devices by imeis
//	  public List<MsDeviceRestResponse>  getSelectedDevicesFromMSByImeis(List<String> imeiList) {
//	        Application application = eurekaClient.getApplication(gatewayServiceId);
//	        InstanceInfo instanceInfo = application.getInstances().get(0);
//	    	String token = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest()
//					.getHeader("Authorization");
//	        String url = "http://" + instanceInfo.getIPAddr() + ":" + instanceInfo.getPort() + "/device/ms-device/getSelectedDevices";
//	        HttpHeaders headers = new HttpHeaders();
//			headers.setAccept(Arrays.asList(MediaType.ALL));
//			headers.set("Authorization", token);
//			List<String> imeis= new ArrayList<String>();
//			imeis.addAll(imeiList);
//			//HttpEntity<String> entity = new HttpEntity<String>(personList,headers);
//			HttpEntity<Object> entity = new HttpEntity<Object>(imeis,headers);
//			ResponseEntity<MsDeviceRestResponse[]> responseEntity = restTemplate.exchange(url, HttpMethod.POST, entity,  MsDeviceRestResponse[].class);
//	        if (responseEntity.getStatusCode().equals(HttpStatus.OK)) {
//	        	 List<MsDeviceRestResponse> allMsDeviceRestResponse= Arrays.asList(responseEntity.getBody());
//	            return allMsDeviceRestResponse;
//	        } else {
//	            throw new InterServiceRestException
//	                    ("Http call not successful", responseEntity.getStatusCode());
//	        }
//	    }
	  // Fetch Campaign Installed Flag 
//		public String getCampaignInstalledFlag(String msgUuid, String imei) {
//			logger.info("Before calling rest template msgUuid: " + msgUuid + " imei: " + imei);
//
//			Application application = eurekaClient.getApplication(gatewayServiceId);
//			InstanceInfo instanceInfo = application.getInstances().get(0);
//			String token = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest()
//					.getHeader("Authorization");
//			String url = "http://" + instanceInfo.getIPAddr() + ":" +instanceInfo.getPort() + "/device/campaign-install-device/" + imei;
//			 HttpHeaders headers = new HttpHeaders();
//			headers.setAccept(Arrays.asList(MediaType.ALL));
//			headers.set("Authorization", token);
////			List<String> imeis= new ArrayList<String>();
////			imeis.addAll(imeiList);
//			HttpEntity<Object> entity = new HttpEntity<Object>(imei,headers);
//
//			ResponseEntity<ResponseDTO> responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity,  ResponseDTO.class);
//
//			if (responseEntity.getStatusCode().equals(HttpStatus.OK)) {
//				logger.info("getCampaignInstalledFlag DONE  msgUuid: " + msgUuid + " imei: " + imei);
//
//				return responseEntity.getBody().getMessage();
//			} else {
//				throw new InterServiceRestException("Http call not successful", responseEntity.getStatusCode());
//			}
// 
//		}
		
		public String isApprovalReqForDeviceUpdate(String companyName) {
			logger.info("Before calling rest template isApprovalReqForDeviceUpdate method ");

	        Application application = eurekaClient.getApplication(gatewayServiceId);
	        InstanceInfo instanceInfo = application.getInstances().get(0);
	        String token = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest()
					.getHeader("Authorization");
	        String url = "http://" + instanceInfo.getIPAddr() + ":" + instanceInfo.getPort() + "/organisation/organisation-name/" + companyName;
	        HttpHeaders headers = new HttpHeaders();
			headers.setAccept(Arrays.asList(MediaType.ALL));
			headers.set("Authorization", token);
			HttpEntity<Object> entity = new HttpEntity<Object>(headers);

	        //ResponseEntity<MessageDTO> entity = restTemplate.getForEntity(url, MessageDTO.class);
			  ResponseEntity<MessageDTO> responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity,  MessageDTO.class);

	        
	        MessageDTO body = responseEntity.getBody();
	        return body.getMessage();
	    }

	    public Page<MsDeviceRestResponse> getDeviceDataFromMS(String customerName, Integer page, Integer pageSize, String sort,
	            String order) {
	        Application application = eurekaClient.getApplication(gatewayServiceId);
	        InstanceInfo instanceInfo = application.getInstances().get(0);
	        String token = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest()
					.getHeader("Authorization");
	        HttpHeaders headers = new HttpHeaders();
			headers.setAccept(Arrays.asList(MediaType.ALL));
			headers.set("Authorization", token);
			HttpEntity<Object> entity = new HttpEntity<Object>(headers);

	        String url = "http://" + instanceInfo.getIPAddr() + ":" + instanceInfo.getPort() + "/device/page/ms-device/{customerName}";
 		 
	        Map<String, String> urlParams = new HashMap<>();
	        urlParams.put("customerName", customerName);

	        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(url)
	                .queryParam("_page", page)
	                .queryParam("_limit", pageSize)
	                .queryParam("_sort", sort)
	                .queryParam("_order", order);

	        URI uri = uriBuilder.buildAndExpand(urlParams).toUri();

	        ResponseEntity<RestResponsePage<MsDeviceRestResponse>> deviceResponseEntity = restTemplate.exchange(uri, HttpMethod.GET, entity,
	                new ParameterizedTypeReference<RestResponsePage<MsDeviceRestResponse>>() {});
	        if (deviceResponseEntity.getStatusCode().equals(HttpStatus.OK)) {
	            Page<MsDeviceRestResponse> body = deviceResponseEntity.getBody();
	            return body;
	        } else {
	            throw new InterServiceRestException("Http call not successful", deviceResponseEntity.getStatusCode());
	        }
  	    }
		/*
		 * public List<String> getOrganisationNameByOrganisationService() {
		 * 
		 * Application application = eurekaClient.getApplication(deviceServiceId);
		 * InstanceInfo instanceInfo = application.getInstances().get(0); String token =
		 * ((ServletRequestAttributes)
		 * RequestContextHolder.getRequestAttributes()).getRequest()
		 * .getHeader("Authorization"); String url = "http://" +
		 * instanceInfo.getIPAddr() + ":" + instanceInfo.getPort() +
		 * "/organisation/firmware-flag"; HttpHeaders headers = new HttpHeaders();
		 * headers.setAccept(Arrays.asList(MediaType.ALL)); headers.set("Authorization",
		 * token); HttpEntity<Object> entity = new HttpEntity<Object>(headers);
		 * ResponseEntity<String[]> responseEntity = restTemplate.exchange(url,
		 * HttpMethod.GET, entity, String[].class);
		 * 
		 * if (responseEntity.getStatusCode().equals(HttpStatus.OK)) { List<String>
		 * deviceList = Arrays.asList(responseEntity.getBody()); return deviceList; }
		 * else { throw new InterServiceRestException("Http call not successful",
		 * responseEntity.getStatusCode()); }
		 * 
		 * }
		 */
      
	   @Async
		public void callIA(ExecuteCampaignRequest executeCampaignRequest, String uuid) {
			try {
				logger.info("Calling Kafka IA ***** " + uuid);
 				kafkaTemplate.send("ms2iaqueue", executeCampaignRequest);
				logger.info("called Kafka IA completed***** " + uuid);

			} catch (Exception ex) {
				logger.info("Exception Calling IA ABC ***** " + uuid + "   " + ex.getMessage());
      			 logger.info("Exception XYZ: " + ExceptionUtils.getStackTrace(ex));
                
			}
		}
	  
	  
}
