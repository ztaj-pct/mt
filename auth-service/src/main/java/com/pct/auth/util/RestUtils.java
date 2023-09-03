package com.pct.auth.util;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
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
import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.shared.Application;
import com.pct.common.dto.OrganisationDTO;
import com.pct.common.exception.InterServiceRestException;
import com.pct.common.model.Location;
import com.pct.common.model.Organisation;

@Component
public class RestUtils {
	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private EurekaClient eurekaClient;

	@Value("${service.gateway.serviceId}")
	private String deviceServiceId;

	private static final Logger logger = LoggerFactory.getLogger(RestUtils.class);

	public Organisation getCompanyFromCompanyService(String accountNumber) throws Exception {
		Organisation company = null;
		try {
			Application application = eurekaClient.getApplication(deviceServiceId);
			InstanceInfo instanceInfo = application.getInstances().get(0);
			String token = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest()
					.getHeader("Authorization");
//			String url = "http://" + instanceInfo.getIPAddr() + ":" + instanceInfo.getPort()
//					+ "/customer/core?accountNumber=" + accountNumber;
//			
			String url = "http://localhost:8014/customer/core?accountNumber=" + accountNumber;

			logger.info("url:" + url);
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.set("Authorization", token);
			HttpEntity<String> entity = new HttpEntity<String>(headers);
			ResponseEntity<Organisation> companyResponseEntity = restTemplate.exchange(url, HttpMethod.GET, entity,
					Organisation.class);
			if (companyResponseEntity.getStatusCode().equals(HttpStatus.OK)) {
				company = companyResponseEntity.getBody();
				return company;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return company;
	}
	
	public List<Organisation> findOrganisationByType(String organisationType) {
		try {
			logger.info(
					"Inside RestUtils Class findByType method and find organization by type and organisationType value",
					organisationType.toString());
			Application application = eurekaClient.getApplication(deviceServiceId);
			InstanceInfo instanceInfo = application.getInstances().get(0);
			String token = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest()
					.getHeader("Authorization");
			String url = "http://" + instanceInfo.getIPAddr() + ":" + instanceInfo.getPort() + "/customer/type/"
					+ organisationType;
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
	
	
	public OrganisationDTO findOrganisationById(Long id) {
		try {
			logger.info(
					"Inside RestUtils Class findOrganisationById method and find organization by id ({})",
					id);
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
			ResponseEntity<OrganisationDTO> companyResponseEntity = restTemplate.exchange(url, HttpMethod.GET,
					entity,OrganisationDTO.class);
			if (companyResponseEntity.getStatusCode().equals(HttpStatus.OK)) {
				OrganisationDTO company = companyResponseEntity.getBody();
				return company;
			} else {
				logger.error("Error Inside RestUtils Class findOrganisationById method :- Http call not successful");
				throw new InterServiceRestException("Http call not successful", companyResponseEntity.getStatusCode());
			}
		} catch (Exception ex) {
			logger.error("Exception Inside RestUtils Class findOrganisationById method", ex);
			throw new InterServiceRestException(
					"Exception while calling the http call Get /organisation/id Exception:-" + ex.getMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}
	
	public Location getLocationFromOrganisationService(Long locationId) throws Exception {
		Location location = null;
		try {
			Application application = eurekaClient.getApplication(deviceServiceId);
			InstanceInfo instanceInfo = application.getInstances().get(0);
			String token = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest()
					.getHeader("Authorization");
			String url = "http://localhost:8014/location/byId?locationId=" + locationId;

			logger.info("url:" + url);
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.set("Authorization", token);
			HttpEntity<String> entity = new HttpEntity<String>(headers);
			ResponseEntity<Location> companyResponseEntity = restTemplate.exchange(url, HttpMethod.GET, entity,
					Location.class);
			if (companyResponseEntity.getStatusCode().equals(HttpStatus.OK)) {
				location = companyResponseEntity.getBody();
				return location;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return location;
	}

}