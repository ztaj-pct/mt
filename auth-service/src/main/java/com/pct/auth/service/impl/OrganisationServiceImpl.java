package com.pct.auth.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.pct.auth.service.OrganisationService;
import com.pct.common.model.Organisation;
import com.pct.common.util.Context;
import com.pct.common.util.Logutils;

@Service
public class OrganisationServiceImpl implements OrganisationService {

	private static final Logger logger = LoggerFactory.getLogger(OrganisationServiceImpl.class);

	private static final String ORGANISATION_SERVICE_ID = "organisation-service";

	public static final String className = "OrganisationServiceImpl";

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private LoadBalancerClient loadBalancerClient;

	@Override
	public Organisation getByname(String name, String token, Context context) {

		String methodName = "getByname";

		String url = loadBalancerClient.choose(ORGANISATION_SERVICE_ID).getUri().toString()
				+ "/organisation/getOrganisationByName";

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setBearerAuth(token);

		url = url + "?name=" + name + "&logUuid=" + context.getLogUUId();

		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(null,
				headers);

		Logutils.log(className, methodName, context.getLogUUId(),
				" Before calling restTemplate.exchange method from OrganisationServiceImpl in auth service", logger);

		ResponseEntity<Organisation> responseEntity = restTemplate.exchange(url, HttpMethod.GET, request,
				Organisation.class);

		Logutils.log(className, methodName, context.getLogUUId(),
				" After calling restTemplate.exchange method from OrganisationServiceImpl  in auth service", logger);

		return responseEntity.getBody();
	}

}
