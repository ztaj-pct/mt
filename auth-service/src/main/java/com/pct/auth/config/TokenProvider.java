package com.pct.auth.config;

import static com.pct.auth.config.JwtConstants.ACCESS_TOKEN_VALIDITY_SECONDS;
import static com.pct.auth.config.JwtConstants.JWT_USER_KEY;
import static com.pct.auth.config.JwtConstants.ROLE_ID;
import static com.pct.auth.config.JwtConstants.SIGNING_KEY;
import static com.pct.auth.config.JwtConstants.TOKEN_TYPE_AZURE_AD;
import static com.pct.common.util.JwtConstants.ROLE_NAME;
import static com.pct.common.util.JwtConstants.ACCOUNT_NUMBER;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pct.auth.redis.RedisService;
import com.pct.common.azure.jwt.JwtValidater;
import com.pct.common.payload.DeviceATCommandReqResponse;
import com.pct.common.redis.AuthPrefix;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Component
public class TokenProvider implements Serializable {

	@Autowired
	private RedisService redisService;

	public String getUsernameFromToken(String token) {
		if (isAzureAdToken(token)) {
			return JwtValidater.getClaims(token).get("unique_name", String.class);
		} else {
			return getClaimFromToken(token, Claims::getSubject);
		}
	}

	public Date getExpirationDateFromToken(String token) {
		return getClaimFromToken(token, Claims::getExpiration);
	}

	public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
		final Claims claims = getAllClaimsFromToken(token);
		return claimsResolver.apply(claims);
	}

	private Claims getAllClaimsFromToken(String token) {
		if (isAzureAdToken(token)) {
			return JwtValidater.getClaims(token);
		} else {
			return Jwts.parser().setSigningKey(SIGNING_KEY).parseClaimsJws(token).getBody();
		}
	}

	public Boolean isTokenExpired(String token) {
		final Date expiration = getExpirationDateFromToken(token);
		if (expiration == null) {
			return false;
		}
		return expiration.before(new Date());
	}

	public String generateToken(JwtUser jwtUser) {

		if (jwtUser.getUsername().equalsIgnoreCase("pcwh-computer@phillips-connect.com")) {
			return Jwts.builder().setSubject(jwtUser.getUsername()).claim(ROLE_ID, jwtUser.getRole())
					.claim(JWT_USER_KEY, jwtUser).signWith(SignatureAlgorithm.HS256, SIGNING_KEY)
					.setIssuedAt(new Date(System.currentTimeMillis())).compact();
		} else {
			return Jwts.builder().setSubject(jwtUser.getUsername()).claim(ROLE_ID, jwtUser.getRole())
					.claim(JWT_USER_KEY, jwtUser).signWith(SignatureAlgorithm.HS256, SIGNING_KEY)
					.setIssuedAt(new Date(System.currentTimeMillis()))
					.setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_VALIDITY_SECONDS * 1000))
					.compact();
		}

	}

	public Boolean validateToken(String token, UserDetails userDetails) {
		final String username = getUsernameFromToken(token);
		return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
	}

	public JwtUser getJwtUser(final String token) {
		JwtUser jwtUser = null;
		if (isAzureAdToken(token)) {
			final Claims claims = JwtValidater.getClaims(token);
			jwtUser = new JwtUser();
			String userName = claims.get("unique_name", String.class);
			jwtUser.setUsername(userName);
			String sessionString = redisService.hget(AuthPrefix.getSession, userName);
			Map<String, Object> sessionMap = JSON.toJavaObject(JSONObject.parseObject(sessionString), Map.class);
			jwtUser.setRole((List<Map<String, Object>>) sessionMap.get("roles"));
		} else {
			final JwtParser jwtParser = Jwts.parser().setSigningKey(SIGNING_KEY);
			final Jws<Claims> claimsJws = jwtParser.parseClaimsJws(token);
			final Claims claims = claimsJws.getBody();
			jwtUser = new JwtUser();
			jwtUser.setUsername(claims.getSubject());
			jwtUser.setRole((List<Map<String, Object>>) claims.get(ROLE_ID));
			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			JwtUser jw = mapper.convertValue(claims.get(JWT_USER_KEY), JwtUser.class);
			if(jw!=null) {
				jwtUser.setRoleName(jw.getRoleName());	
				jwtUser.setAccountNumber(jw.getAccountNumber());
			}
		}

		return jwtUser;
	}

	public boolean isAzureAdToken(String token) {
		String tokenType = redisService.hget(AuthPrefix.getTokenType, token);
		return tokenType != null && tokenType.equalsIgnoreCase(TOKEN_TYPE_AZURE_AD) ? true : false;
	}
}
