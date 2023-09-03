package com.pct.auth.controllers;

import java.io.IOException;
import java.util.Map;

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

import com.pct.auth.config.TokenProvider;
import com.pct.auth.redis.RedisService;
import com.pct.auth.service.AzureADTokenService;
import com.pct.common.redis.AuthPrefix;
import com.pct.common.util.Context;
import com.pct.common.util.Logutils;

import io.jsonwebtoken.Claims;

@Controller
@RequestMapping("/app")
public class AppController {

	@Autowired
	private AzureADTokenService azureADTokenService;

	@Value("${endpoint.url}")
	private String endpointUrl;

	@Autowired
	private RedisService redisService;

	@Autowired
	private TokenProvider tokenProvider;

	public static final String className = "AppController";

	private static final Logger logger = LoggerFactory.getLogger(AppController.class);

	@GetMapping("/login")
	public void login(@RequestParam Map<String, Object> param, HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		Context context = new Context();

		String redirectUrl = endpointUrl + "#/login";

		logger.info("Params: {}", param);

		String query = (String) param.get("q");

		if (query != null && query.equalsIgnoreCase("AD")) {

			String state = param != null && param.containsKey("state") ? (String) param.get("state") : null;

			Logutils.log(className, "getLoginUrl", context.getLogUUId(),
					"Before calling getLoginUrl method of AzureADTokenService from AppController", logger);

			redirectUrl = azureADTokenService.getLoginUrl(state);

			Logutils.log(className, "getLoginUrl", context.getLogUUId(),
					" after calling getLoginUrl Method of AppController", logger);
		}

		response.sendRedirect(redirectUrl);
	}

	@GetMapping("/logout")
	public void logout(HttpServletRequest request, HttpServletResponse response) throws IOException {

		boolean isAzureADToken = false;

		String redirectUrl = endpointUrl + "#/login";

		String token = request.getParameter("Authorization");
		try {
			if (token != null && !token.isEmpty() && token.startsWith("Bearer ")) {
				token = token.substring(7);

				isAzureADToken = tokenProvider.isAzureAdToken(token);
				if (isAzureADToken) {
					Claims claims = azureADTokenService.getClaimsFromJWTToken(token);

					String userName = claims.get("unique_name", String.class);

					String sessionString = redisService.hget(AuthPrefix.getSession, userName);

					if (sessionString != null && !sessionString.isEmpty()) {
						redisService.hdel(AuthPrefix.getSession, "", userName);
					}
				
					redirectUrl = azureADTokenService.getLogoutUrl();
				}
				
				redisService.hdel(AuthPrefix.getTokenType , "" , token);
			}
		} catch (Exception e) {
			logger.error("Execption Occured", e);
		}

		response.sendRedirect(redirectUrl);
	}

}
