package com.pct.common.azure.jwt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pct.common.exception.AuthenticationException;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.IncorrectClaimException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MissingClaimException;
import io.jsonwebtoken.SignatureException;

public class JwtValidater {

	private static final Logger LOGGER = LoggerFactory.getLogger(JwtValidater.class);

	private final static String AUTHORITY = "https://login.microsoftonline.com/common/v2.0";

	public static boolean isValid(String accessToken) {
		Claims claims = paraseToken(accessToken);
		return claims != null ? true : false;
	}

	public static Claims getClaims(String accessToken) {
		return paraseToken(accessToken);
	}

	private static Claims paraseToken(String accessToken) {
		Claims claims = null;
		Jws<Claims> jwsClaims = null;
		try {
			SigningKeyResolver signingKeyResolver = new SigningKeyResolver(AUTHORITY);

			// @formatter:off

        	 jwsClaims = Jwts.parser()
                    .setSigningKeyResolver(signingKeyResolver)
                    .parseClaimsJws(accessToken);
        	
        	// @formatter:on
			claims = jwsClaims.getBody();
		} catch (SignatureException ex) {
			LOGGER.error("Jwt validation failed: invalid signature", ex);
			throw new AuthenticationException("Invalid signature");
		} catch (ExpiredJwtException ex) {
			LOGGER.error("Jwt validation failed: access token us expired", ex);
			throw new AuthenticationException("access token us expired");
		} catch (MissingClaimException ex) {
			LOGGER.error("Jwt validation failed: missing required claim", ex);
			throw new AuthenticationException("Missing required claim");
		} catch (IncorrectClaimException ex) {
			LOGGER.error("Jwt validation failed: required claim has incorrect value", ex);
			throw new AuthenticationException("Required claim has incorrect value");
		} catch (JwtValidationException ex) {
			LOGGER.error("wt validation failed: invalid token", ex);
			throw new AuthenticationException("Invalid token");
		} catch (Exception ex) {
			LOGGER.error("Execption Occured", ex);
			throw new AuthenticationException("Internal server error");
		}

		return claims;
	}
}
