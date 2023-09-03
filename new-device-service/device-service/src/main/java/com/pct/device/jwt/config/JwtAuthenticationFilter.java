package com.pct.device.jwt.config;


import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.CollectionUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pct.device.constant.Status;
import com.pct.device.dto.GenericResponse;
import com.pct.device.redis.AuthPrefix;
import com.pct.device.redis.PermissionCacheModel;
import com.pct.device.redis.RedisService;
import com.pct.device.redis.RoleCacheModel;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	@Autowired
	private TokenProvider jwtTokenUtil;

	@Autowired
	private ObjectMapper objectMapper;
	
	private List<String> excludedUrls;
	
	private String tokenHeader="Authorization";
	
	@Autowired
	private RedisService redisService;

	@Override
	protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws IOException, ServletException {
		String username = null;

		excludedUrls = Arrays.asList("springfox-swagger-ui", "swagger-resources", "swagger-ui.html", "/v2/api-docs", "/api/auth", "/api/ping");
		
		if (excludedUrls.stream().noneMatch(url-> req.getRequestURL().toString().toLowerCase().contains(url.toLowerCase())) && !req.getMethod().equals("OPTIONS")) {
			String authToken = req.getHeader(this.tokenHeader);
			
			if(null == authToken) {
				logger.error("Token can not be left blank.");
				prepareErrorResponse(res, "Token can not be left blank.");
				return;
			}
			
			if(authToken.startsWith("Bearer ")) {
				authToken = authToken.substring(7);
				
				if (jwtTokenUtil.isTokenExpired(authToken)) { 
					logger.info("Token is expired.");
					prepareErrorResponse(res, "Token is expired.");
					return;
				}
				username = jwtTokenUtil.getUsernameFromToken(authToken);
			}

			if(null == username) {
				logger.info("Token is not valid.");
				prepareErrorResponse(res, "Token is not valid.");
				return;
			}
			
			try {
				if (username != null) {

					JwtUser jwtUser = jwtTokenUtil.getJwtUser(authToken);

					if (!jwtTokenUtil.isTokenExpired(authToken)) {
						UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(jwtUser, null, Arrays.asList(new SimpleGrantedAuthority(jwtUser.getRoleName())));
						authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
						logger.info("authenticated user " + username + ", setting security context");
						SecurityContextHolder.getContext().setAuthentication(authentication);
						
						
						List<PermissionCacheModel> permissionList = null;
						RoleCacheModel role = null;
						String json = redisService.hget(AuthPrefix.getRole, jwtUser.getRoleId());
						if (Objects.nonNull(json)) {
							role = JSON.toJavaObject(JSONObject.parseObject(json), RoleCacheModel.class);
						}
						if(!CollectionUtils.isEmpty(role.getPermissions())) {
							permissionList = role.getPermissions();
						}			
						if (permissionList.stream().noneMatch(permission -> req.getRequestURI().contains(permission.getPath()) 
								&& permission.getMethodType().name().equalsIgnoreCase(req.getMethod()))) {
							prepareErrorResponse(res, "you don't have permission to access the requested resource");
							return;
						}
					}
				}

			} catch (Exception e) {
				prepareErrorResponse(res, "Invalid user");
				e.printStackTrace();
				return;
			}
		}

		chain.doFilter(req, res);
	}

	private void prepareErrorResponse(HttpServletResponse response, String errorMessage) throws IOException {
		response.setStatus(HttpStatus.UNAUTHORIZED.value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		GenericResponse body = new GenericResponse();
		body.setStatus(Status.FAILURE );
		body.setError(errorMessage);
		response.getOutputStream().println(objectMapper.writeValueAsString(body));
	}
}