package com.pct.auth.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AzureADToken implements Serializable {
	private static final long serialVersionUID = 1L;

	@JsonProperty("token_type")
	private String tokenType;

	private String scope;

	@JsonProperty("expires_in")
	private Long expiresIn;

	@JsonProperty("ext_expires_in")
	private Long extExpiresIn;

	@JsonProperty("access_token")
	private String accessToken;
	
	@JsonProperty("refresh_token")
	private String refreshToken;

	public String getTokenType() {
		return tokenType;
	}

	public void setTokenType(String tokenType) {
		this.tokenType = tokenType;
	}

	public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}

	public Long getExpiresIn() {
		return expiresIn;
	}

	public void setExpiresIn(Long expiresIn) {
		this.expiresIn = expiresIn;
	}

	public Long getExtExpiresIn() {
		return extExpiresIn;
	}

	public void setExtExpiresIn(Long extExpiresIn) {
		this.extExpiresIn = extExpiresIn;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public String getRefreshToken() {
		return refreshToken;
	}

	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}

	@Override
	public String toString() {
		return "AzureADToken [tokenType=" + tokenType + ", scope=" + scope + ", expiresIn=" + expiresIn
				+ ", extExpiresIn=" + extExpiresIn + ", accessToken=" + accessToken + ", refreshToken=" + refreshToken
				+ "]";
	}

}
