package com.pct.auth.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.pct.auth.dto.AzureADToken;
import com.pct.auth.service.AzureADTokenService;
import com.pct.common.azure.jwt.JwtValidater;
import com.pct.common.exception.AuthenticationException;

import io.jsonwebtoken.Claims;

@Service
public class AzureADTokenServiceImpl implements AzureADTokenService {

	private static final Logger LOGGER = LoggerFactory.getLogger(AzureADTokenServiceImpl.class);

	private static final String URI_OAUTH2_V2_TOKEN = "https://login.microsoftonline.com/%s/oauth2/v2.0/token";
	
	// @formatter:off
	
	private static final String URI_OAUTH2_V2_AUTHORIZE = "https://login.microsoftonline.com/%s/oauth2/v2.0/authorize?"
			+ "client_id=%s" 
			+ "&response_type=%s" 
			+ "&redirect_uri=%s" 
			+ "&scope=%s" 
			+ "&state=%s" 
			+ "&nonce=%s"
			;
	
	private static final String LOGOUT_URL = "https://login.microsoftonline.com/%s/oauth2/logout?"
			+ "client_id=%s";
	
	// @formatter:on
	
	private static final String GRANT_TYPE_AUTHORIZATION_CODE = "authorization_code";
	private static final String RESPONSE_TYPE_CODE = "code";

	@Autowired
	private RestTemplate restTemplate;

	@Value("${azure.activedirectory.tenant-id}")
	private String tenantId;

	@Value("${azure.activedirectory.client-id}")
	private String clientId;

	@Value("${azure.activedirectory.client-secret}")
	private String clientSecret;

	@Value("${azure.activedirectory.redirect-uri-template}")
	private String redirectUrl;

	@Value("${azure.activedirectory.nonce:none}")
	private String nonce;
	
	@Value("${azure.activedirectory.scopes}")
	private String scopes;
	
	@Value("${azure.activedirectory.state}")
	private String defaultState;

	@Override
	public AzureADToken getJWTAccessToken(String code, String state) {

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
		map.add("client_id", clientId);
		map.add("client_secret", clientSecret);
		map.add("grant_type", GRANT_TYPE_AUTHORIZATION_CODE);
		map.add("scope", scopes);
		map.add("code", code);
		map.add("redirect_uri", redirectUrl);
		map.add("state", state);
		map.add("nonce", nonce);

		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(map, headers);
		AzureADToken azureADToken = null;
		try {
			ResponseEntity<AzureADToken> response = restTemplate.exchange(String.format(URI_OAUTH2_V2_TOKEN, tenantId),
					HttpMethod.POST, request, AzureADToken.class);
			azureADToken = response.getBody();
			LOGGER.info("Token Info : {}", azureADToken);
		} catch (Exception e) {
			LOGGER.error("Exception Occured", e);
			throw new AuthenticationException(e.getMessage());
		}
		return azureADToken;
	}

	@Override
	public String getLoginUrl(String state) {
		// @formatter:off
 
		return String.format(
				URI_OAUTH2_V2_AUTHORIZE, 
				this.tenantId, 
				this.clientId, 
				RESPONSE_TYPE_CODE,
				this.redirectUrl, 
				scopes, 
				getState(state), 
				nonce
				);
		
		// @formatter:on
	}

	private Object getState(String state) {
		String result = this.defaultState;
		if (state != null && !state.isEmpty()) {
			result = state;
		}
		return result;
	}

	@Override
	public Claims getClaimsFromJWTToken(String token) {
		return getClaims(token);
	}

	private Claims getClaims(String accessToken) {
		return JwtValidater.getClaims(accessToken);
	}
	
	@Override
	public String getLogoutUrl() {
		return String.format(LOGOUT_URL, 
				this.tenantId, 
				this.clientId);
	}
	
}
