package com.pct.auth.controllers;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.alibaba.fastjson.JSON;
import com.pct.auth.config.JwtConstants;
import com.pct.auth.dto.AzureADToken;
import com.pct.auth.redis.RedisService;
import com.pct.auth.service.AzureADTokenService;
import com.pct.auth.service.IRoleService;
import com.pct.auth.service.IUserService;
import com.pct.auth.service.OrganisationService;
import com.pct.common.dto.RoleDTO;
import com.pct.common.dto.UserDTO;
import com.pct.common.model.Organisation;
import com.pct.common.redis.AuthPrefix;
import com.pct.common.util.Context;
import com.pct.common.util.Logutils;

import io.jsonwebtoken.Claims;

@Controller
@RequestMapping("/azure")
public class AzureADLoginController {

	private static final String PCT_USER = "Phillips Connect  User";

	private static final String ORGANISATION_PHILLIPS_INDUSTRIES = "Phillips Connect";

	private static final String STATE_DEFAULT = "default";

	public static final String className = "AzureADLoginController";

	private static final Logger logger = LoggerFactory.getLogger(AzureADLoginController.class);

	@Value("${endpoint.url}")
	private String endpointUrl;

	@Autowired
	private AzureADTokenService azureADTokenService;

	@Autowired
	private IUserService iUserService;

	@Autowired
	private IRoleService iRoleService;

	@Autowired
	private OrganisationService organisationService;

	@Autowired
	private RedisService redisService;

	@GetMapping("/login")
	public void login(@RequestParam Map<String, Object> param, HttpServletRequest request, HttpServletResponse response)
			throws IOException {

		logger.info("Params: {}", param);

		Context context = new Context();

		if (param != null && param.containsKey("code")) {
			String state = (String) param.get("state");
			String code = (String) param.get("code");

			Logutils.log(className, "getJWTAccessToken", context.getLogUUId(),
					"Before calling getJWTAccessToken method of AzureADTokenService from AzureADLoginController",
					logger);

			AzureADToken azureADToken = azureADTokenService.getJWTAccessToken(code, state);

			Logutils.log(className, "getJWTAccessToken", context.getLogUUId(),
					" after calling getJWTAccessToken Method of AzureADLoginController", logger);

			Claims claims = azureADTokenService.getClaimsFromJWTToken(azureADToken.getAccessToken());

			initNewUser(claims, azureADToken, context);

			initSession(false, claims, azureADToken, context);

			response.sendRedirect(getRedirectUrl(state, azureADToken, context));

		} else {
			String state = param != null && param.containsKey("state") ? (String) param.get("state") : null;
			response.sendRedirect(azureADTokenService.getLoginUrl(state));
		}
	}

	private void initSession(boolean isNewUser, Claims claims, AzureADToken azureADToken, Context context) {

		String userName = claims.get("unique_name", String.class);

		String sessionString = redisService.hget(AuthPrefix.getSession, userName);
		
		if (sessionString != null && !sessionString.isEmpty()) {
			redisService.hdel(AuthPrefix.getSession, "", userName);
			redisService.hdel(AuthPrefix.getTokenType, "", azureADToken.getAccessToken());
		}
		Map<String, Object> sessionMap = new HashMap<>();

		if (isNewUser) {
			sessionMap.put("roles", Collections.singletonList(iRoleService.getByName(PCT_USER, context)));
		} else {
			sessionMap.put("roles", iUserService.findByUserName(userName, context).getRole());
		}
		redisService.hset(AuthPrefix.getSession, userName, JSON.toJSON(sessionMap).toString());
		redisService.hset(AuthPrefix.getTokenType, azureADToken.getAccessToken(), JwtConstants.TOKEN_TYPE_AZURE_AD);
	}

	private String getRedirectUrl(String state, AzureADToken azureADToken, Context context) {
		String redirectUrl = "";
		switch (state) {
		case STATE_DEFAULT:
			redirectUrl = redirectUrl + endpointUrl + "#/login" + getQueryParams(azureADToken, context);
			break;

		default:
			redirectUrl = redirectUrl + endpointUrl;
			break;
		}
		return redirectUrl;
	}

	private void initNewUser(Claims claims, AzureADToken azureADToken, Context context) {

		if (claims != null && !iUserService.existsByUserName(claims.get("unique_name", String.class), context)) {
			initSession(true, claims, azureADToken, context);
			
			Organisation organisation = getDefaultOrganisation(azureADToken.getAccessToken(), context);
			
			iUserService.createUser(getUserDTOObject(claims, organisation, context), context);
		}
	}

	private UserDTO getUserDTOObject(Claims claims, Organisation organisation, Context context) {
		UserDTO userDTO = new UserDTO();
		userDTO.setOrganisation(organisation);

		// userDTO.setCountryCode();
		// userDTO.setPhone();
		userDTO.setTimeZone("UTC");

		userDTO.setEmail(claims.get("unique_name", String.class));
		userDTO.setFirstName(claims.get("given_name", String.class));
		userDTO.setIsActive(true);
		userDTO.setLastName(claims.get("family_name", String.class));
		userDTO.setNotify("email");
		userDTO.setPassword("");

		userDTO.setRole(getRoles(context));

		//userDTO.setTimeZone(null);
		userDTO.setUserName(claims.get("unique_name", String.class));
		userDTO.setUuid(UUID.randomUUID().toString());
		return userDTO;
	}

	private Organisation getDefaultOrganisation(String token, Context context) {
		return organisationService.getByname(ORGANISATION_PHILLIPS_INDUSTRIES, token, context);
	}

	private List<RoleDTO> getRoles(Context context) {
		return Collections.singletonList(iRoleService.getByName(PCT_USER, context));
	}

	private String getQueryParams(AzureADToken azureADToken, Context context) {
		String param = "?token=" + azureADToken.getAccessToken();

		Claims claims = azureADTokenService.getClaimsFromJWTToken(azureADToken.getAccessToken());
		if (claims != null) {
			UserDTO user = iUserService.findByUserName(claims.get("unique_name", String.class), context);
			if (user != null) {
				// @formatter:off
 
				param = param 
						+ "&userId=" 
						+ user.getId() 
						+ "&timeZone="
						+ (user.getTimeZone() != null ? user.getTimeZone() : "NA") + "&userUuid=" 
						+ user.getUuid()
						+ "&userName=" 
						+ user.getUserName();
				
				// @formatter:on
			}
		}
		return param;
	}

	@GetMapping("/logout")
	public void logout(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String token = request.getParameter("Authorization");
		try {
			if (token != null && !token.isEmpty() && token.startsWith("Bearer ")) {
				token = token.substring(7);
				
				Claims claims = azureADTokenService.getClaimsFromJWTToken(token);
	
				String userName = claims.get("unique_name", String.class);
				
				String sessionString = redisService.hget(AuthPrefix.getSession, userName);
				
				if (sessionString != null && !sessionString.isEmpty()) {
					redisService.hdel(AuthPrefix.getSession, "", userName);
				}
				
				redisService.hdel(AuthPrefix.getTokenType, "", token);
			}
		}catch (Exception e) {
			logger.error("Execption Occured", e);
		}
		response.sendRedirect(azureADTokenService.getLogoutUrl());
	}

}
