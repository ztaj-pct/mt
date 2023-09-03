package com.pct.organisation.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.shared.Application;
import com.pct.common.dto.ResponseBodyDTO;
import com.pct.common.dto.ResponseDTO;
import com.pct.common.exception.InterServiceRestException;
import com.pct.common.model.User;
import com.pct.organisation.dto.MessageDTO;
import com.pct.organisation.payload.HubCustomer;
import com.pct.organisation.payload.HubCustomerResponse;

@Component
public class RestUtils {

	@Autowired
    private RestTemplate restTemplate;
    @Autowired
    private EurekaClient eurekaClient;
    @Autowired
    private BeanConvertor beanConvertor;

    @Value("${service.gateway.serviceId}")
    private String gatewayServiceId;
    
    @Value("${hub.endpoint.customer}")
    private String hubCustomerEndpoint;

    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public User getUserFromAuthService(Long userId) {
        Application application = eurekaClient.getApplication(gatewayServiceId);
        InstanceInfo instanceInfo = application.getInstances().get(0);
        String url = "http://" + instanceInfo.getIPAddr() + ":" + instanceInfo.getPort() + "/user/core/" + userId;
        ResponseEntity<MessageDTO> userResponseEntity = restTemplate.getForEntity(url, MessageDTO.class);
        if (userResponseEntity.getStatusCode().equals(HttpStatus.OK)) {
            User user = objectMapper.convertValue(userResponseEntity.getBody().getBody(), User.class);
            return user ;
        } else {
            throw new InterServiceRestException
                    ("Http call not successful", userResponseEntity.getStatusCode());
        }
    }
    
    public User getUserFromAuthService(String userName) {
        Application application = eurekaClient.getApplication(gatewayServiceId);
        InstanceInfo instanceInfo = application.getInstances().get(0);
        String token = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest().getHeader("Authorization");
        String url = "http://" + instanceInfo.getIPAddr() + ":" + instanceInfo.getPort() + "/user/username?user_name=" + userName;
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.ALL));
        headers.set("Authorization", token);
        HttpEntity<String> entity = new HttpEntity<String>(headers);
        User user = new User();
        try {
//        	 List<HttpMessageConverter<?>> messageConverters = new ArrayList<>();
//        	   MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
//        	   converter.setSupportedMediaTypes(Collections.singletonList(MediaType.ALL));
//        	   messageConverters.add(converter);
//        	   restTemplate.setMessageConverters(messageConverters);
			ResponseEntity<User> userResponseEntity =  restTemplate.exchange(url, HttpMethod.GET,entity, User.class);
			if (userResponseEntity.getStatusCode().equals(HttpStatus.OK)) {
			  user = userResponseEntity.getBody();
			 return user;
			} else {
			    throw new InterServiceRestException
			            ("Http call not successful", userResponseEntity.getStatusCode());
			}
		} catch (RestClientException e) {
			e.printStackTrace();
		}
        return user;
    }
    
    public User getUserByUuidFromAuthService(String userUuid) {
        Application application = eurekaClient.getApplication(gatewayServiceId);
        InstanceInfo instanceInfo = application.getInstances().get(0);
        String url = "http://" + instanceInfo.getIPAddr() + ":" + instanceInfo.getPort() + "/user/core/uuid/" + userUuid;
        ResponseEntity<MessageDTO> userResponseEntity = restTemplate.getForEntity(url, MessageDTO.class);
        if (userResponseEntity.getStatusCode().equals(HttpStatus.OK)) {
            User user = objectMapper.convertValue(userResponseEntity.getBody().getBody(), User.class);
            return user;
        } else {
            throw new InterServiceRestException
                    ("Http call not successful", userResponseEntity.getStatusCode());
        }
    }
    
    public Boolean deleteInstallDataForOrganisation(String organisationUuid) {
        Application application = eurekaClient.getApplication(gatewayServiceId);
        InstanceInfo instanceInfo = application.getInstances().get(0);
        String url = "http://" + instanceInfo.getIPAddr() + ":" + instanceInfo.getPort() + "/installation/core/reset-company?company_uuid=" + organisationUuid;
        ResponseEntity<ResponseDTO> responseEntity = restTemplate.postForEntity(url, null, ResponseDTO.class);
        if (responseEntity.getStatusCode().equals(HttpStatus.OK)) {
            Boolean status = responseEntity.getBody().getStatus();
            return status;
        } else {
            throw new InterServiceRestException
                    ("Http call not successful", responseEntity.getStatusCode());
        }
    }
    
    public Boolean deleteInstallDataForDevice(String accountNumber) {
        Application application = eurekaClient.getApplication(gatewayServiceId);
        InstanceInfo instanceInfo = application.getInstances().get(0);
        String url = "http://" + instanceInfo.getIPAddr() + ":" + instanceInfo.getPort() + "/device/core/reset-company?account_number=" + accountNumber;
        ResponseEntity<ResponseDTO> responseEntity = restTemplate.postForEntity(url, null, ResponseDTO.class);
        if (responseEntity.getStatusCode().equals(HttpStatus.OK)) {
            Boolean status = responseEntity.getBody().getStatus();
            return status;
        } else {
            throw new InterServiceRestException
                    ("Http call not successful", responseEntity.getStatusCode());
        }
    }
    
    public Long getCountOfAssetForOrganisation(Long organisationId) {
   	 Application application = eurekaClient.getApplication(gatewayServiceId);
        InstanceInfo instanceInfo = application.getInstances().get(0);
        String url = "http://" + instanceInfo.getIPAddr() + ":" + instanceInfo.getPort() + "/device/core/count/asset?companyId=" + organisationId;
        ResponseEntity<Long> responseEntity = restTemplate.postForEntity(url, null, Long.class);
        if (responseEntity.getStatusCode().equals(HttpStatus.OK)) {
       	 System.out.print(responseEntity.getBody());
       	 Long value = responseEntity.getBody();
            return value;
        } else {
            throw new InterServiceRestException
                    ("Http call not successful", responseEntity.getStatusCode());
        } 
   	
   }
    
    public List<HubCustomer> getCustomersFromHub() {
        ResponseEntity<HubCustomerResponse> responseEntity = restTemplate.getForEntity(hubCustomerEndpoint, HubCustomerResponse.class);
        if (responseEntity.getStatusCode().equals(HttpStatus.OK)) {
            return responseEntity.getBody().getMessage().getCustomerList();
        } else {
        	System.out.println("@@@@@@==== "+responseEntity.getHeaders());
        	System.out.println("@@@@@@==== "+responseEntity.getHeaders());
        	 return responseEntity.getBody().getMessage().getCustomerList();
        }
    }
    
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Set<String> getImeisByAccountNumber(String accountNumber) {
		Application application = eurekaClient.getApplication(gatewayServiceId);
		InstanceInfo instanceInfo = application.getInstances().get(0);
		
		String url = "http://" + instanceInfo.getIPAddr() + ":" + instanceInfo.getPort() + "/device/imei?can="
				+ accountNumber;

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		
		String token = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest().getHeader("Authorization");
		
		headers.set("Authorization", token);
		
		HttpEntity<String> httpEntity = new HttpEntity<String>(headers);
		
		ResponseEntity<ResponseBodyDTO> responseEntity = restTemplate.exchange(url, HttpMethod.GET, httpEntity, ResponseBodyDTO.class);

		if (responseEntity.getStatusCode().equals(HttpStatus.OK)) {
			ResponseBodyDTO<Set<String>> messageDTO = responseEntity.getBody();
			return new HashSet<>(messageDTO.getBody());
		} else {
			throw new InterServiceRestException("Http call not successful", responseEntity.getStatusCode());
		}

	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Map<String, String> getAllMS1And2OrgNameMap() {
		Application application = eurekaClient.getApplication(gatewayServiceId);
		InstanceInfo instanceInfo = application.getInstances().get(0);
		
		String url = "http://" + instanceInfo.getIPAddr() + ":" + instanceInfo.getPort() + "/device/company/mappings";

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		
		String token = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest().getHeader("Authorization");
		
		headers.set("Authorization", token);
		
		HttpEntity<String> httpEntity = new HttpEntity<String>(headers);
		
		ResponseEntity<ResponseBodyDTO> responseEntity = restTemplate.exchange(url, HttpMethod.GET, httpEntity, ResponseBodyDTO.class);

		if (responseEntity.getStatusCode().equals(HttpStatus.OK)) {
			ResponseBodyDTO<Map<String, String>> messageDTO = responseEntity.getBody();
			return messageDTO.getBody();
		} else {
			throw new InterServiceRestException("Http call not successful", responseEntity.getStatusCode());
		}

	}
	
	public ResponseDTO getIsUserAssoicatedWithLocationFromAuthService(Long locationID) {
        Application application = eurekaClient.getApplication(gatewayServiceId);
        InstanceInfo instanceInfo = application.getInstances().get(0);
        String token = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest().getHeader("Authorization");
        String url = "http://" + instanceInfo.getIPAddr() + ":" + instanceInfo.getPort() + "/user/is-user?locationId=" + locationID;
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.ALL));
        headers.set("Authorization", token);
        HttpEntity<String> entity = new HttpEntity<String>(headers);
//        User user = new User();
        ResponseDTO responseDTO = new ResponseDTO();
        try {
			ResponseEntity<ResponseDTO> responseEntity =  restTemplate.exchange(url, HttpMethod.GET,entity, ResponseDTO.class);
			if (responseEntity.getStatusCode().equals(HttpStatus.OK)) {
				responseDTO = responseEntity.getBody();
			 return responseDTO;
			} else {
			    throw new InterServiceRestException
			            ("Http call not successful", responseEntity.getStatusCode());
			}
		} catch (RestClientException e) {
			e.printStackTrace();
		}
        return responseDTO;
    }
}
