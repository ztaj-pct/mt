package com.pct.device.command.util;

import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.shared.Application;
import com.pct.common.exception.InterServiceRestException;
import com.pct.common.model.User;
import com.pct.device.command.dto.MessageDTO;

@Component
public class RestUtils {

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private EurekaClient eurekaClient;

	@Value("${service.gateway.serviceId}")
	private String deviceServiceId;

	private final ObjectMapper objectMapper = new ObjectMapper();
	Logger logger = LoggerFactory.getLogger(RestUtils.class);

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
			logger.info("Url:-" + url);
			logger.info("Token:-" + token);
			HttpHeaders headers = new HttpHeaders();
			headers.setAccept(Arrays.asList(MediaType.ALL));
			headers.set("Authorization", token);
			HttpEntity<String> entity = new HttpEntity<String>(headers);
			logger.info("RestUtils to get user by username" + userName);
			ResponseEntity<String> userResponseEntity1 = restTemplate.exchange(url, HttpMethod.GET, entity,
					String.class);
			JSONObject jsonObject = new JSONObject(userResponseEntity1.getBody());
			User user2 = new User();
			user2.setUserName(jsonObject.getString("user_name"));
			logger.info("AFter user name" + user2.getUserName());
//			ResponseEntity<User> userResponseEntity = restTemplate.exchange(url, HttpMethod.GET, entity, User.class);
//			logger.info("Getting response of response entity" + userResponseEntity.toString());
//			logger.info("Getting response body" + userResponseEntity.getBody());
			if (userResponseEntity1.getStatusCode().equals(HttpStatus.OK)) {
				return user2;
			} else {
				logger.info("Error Inside RestUtils Class getUserFromAuthService method :- Http call not successful");
				logger.error("Error Inside RestUtils Class getUserFromAuthService method :- Http call not successful");
				throw new InterServiceRestException("Http call not successful", userResponseEntity1.getStatusCode());
			}
		} catch (Exception ex) {
			logger.info("Error Inside RestUtils Class getUserFromAuthService method :- Http call not successful"
					+ ex.getMessage());
			logger.error("Exception Inside RestUtils Class getUserFromAuthService method", ex);
			throw new InterServiceRestException(
					"Exception while calling the http call get user by username, Exception:-" + ex.getMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}