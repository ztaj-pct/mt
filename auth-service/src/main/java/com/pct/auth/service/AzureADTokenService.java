package com.pct.auth.service;

import com.pct.auth.dto.AzureADToken;

import io.jsonwebtoken.Claims;

public interface AzureADTokenService {
	
	public AzureADToken getJWTAccessToken(String code, String state);
	
	public String getLoginUrl(String state);
	
	public Claims getClaimsFromJWTToken(String token);
	
	public String getLogoutUrl();
}
