package com.pct.auth.controllers;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.pct.auth.config.JwtConstants;
import com.pct.auth.config.JwtUser;
import com.pct.auth.config.TokenProvider;
import com.pct.auth.dto.AuthenticationRequest;
import com.pct.auth.redis.RedisService;
import com.pct.auth.service.impl.UserServiceImpl;
import com.pct.common.model.User;
import com.pct.common.redis.AuthPrefix;
import com.pct.common.response.GenericResponse;
import com.pct.common.response.Status;
import com.pct.common.util.Context;
import com.pct.common.util.Logutils;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = "Authentication Controller Provider")
@CrossOrigin("*")
@RestController
@RequestMapping("/user/token")
public class AuthenticateController {

	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private TokenProvider jwtTokenUtil;

	@Resource(name = "userService")
	private UserServiceImpl userService;

	@Autowired
	private RedisService redisService;

	Logger logger = LoggerFactory.getLogger(AuthenticateController.class);

	@ApiOperation(value = "It is to generate new authorization token")
	@PostMapping("/login")
	public ResponseEntity createAuthToken(@RequestBody AuthenticationRequest authenticationRequest, HttpServletResponse res) throws IOException {
		logger.info("Inside createAuthToken method of AuthenticateController");
		logger.info("AuthenticationRequest:" + authenticationRequest);
		try {
			Context context = new Context();

			Logutils.log("AuthenticateController", "createAuthToken", context.getLogUUId(),
					"Before calling authenticate method of AuthenticationManager from AuthenticateController", logger);
			// for production tool api
			if (authenticationRequest.getUsername().equalsIgnoreCase("pcwh-computer@phillips-connect.com")) {
				authenticationManager.authenticate(
						new UsernamePasswordAuthenticationToken(authenticationRequest.getUsername(), "passme"));
			} else {
				authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
						authenticationRequest.getUsername(), authenticationRequest.getPassword()));
			}

			Logutils.log("AuthenticateController", "createAuthToken", context.getLogUUId(),
					"After calling authenticate method of AuthenticationManager from AuthenticateController", logger);

			Logutils.log("AuthenticateController", "createAuthToken", context.getLogUUId(),
					"Before calling loadUserByUsername method of UserServiceImpl from AuthenticateController", logger);

			User user = userService.loadUserByUsernameForLogin(authenticationRequest.getUsername());
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
			Date lastLogintime = null;
			long noOfDays=0;
			try {
				lastLogintime = simpleDateFormat.parse(user.getLastLoginTime());
				Date currentDateTime = new Date(System.currentTimeMillis());
				long differenceInTime = currentDateTime.getTime() - lastLogintime.getTime();
				noOfDays = (differenceInTime / (1000 * 60 * 60 * 24)) % 365;
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (noOfDays > 15 && !user.getUserName().contains("phillips-connect.com")) {
				userService.updateActiveStatus(user.getUserName());
				GenericResponse response = new GenericResponse();
				response.setMessage("Your account has been disabled due to inactivity over the past 15 days. Please contact your InstallAssist administrator");
				response.setStatus(Status.FAILURE);				
				response.setError("Your account has been disabled due to inactivity over the past 15 days. Please contact your InstallAssist administrator");
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Your account has been disabled due to inactivity over the past 15 days. Please contact your InstallAssist administrator");
//				throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
//						"Your account has been disabled due to inactivity over the past 15 days. Please contact your InstallAssist administrator");
			}
			JwtUser jwtUser = userService.convertUserToJwtUser(user);
			try {
				if (authenticationRequest.getLanding() != null && authenticationRequest.getLanding().length() > 1) {
					userService.updateLandingPage(authenticationRequest.getUsername(),
							authenticationRequest.getLanding());
				}
			} catch (Exception ex) {
				ex.printStackTrace();
				Logutils.log("AuthenticateController", "createAuthToken", context.getLogUUId(),
						"Not Able to update the landing page", logger);
			}
			Logutils.log("AuthenticateController", "createAuthToken", context.getLogUUId(),
					"After calling loadUserByUsername method of UserServiceImpl from AuthenticateController", logger);

			final String token = jwtTokenUtil.generateToken(jwtUser);
			GenericResponse response = new GenericResponse();
			response.setMessage("New authorization token has been generated.");
			response.setStatus(Status.SUCCESS);
			response.setToken(token);
			response.setUserName(jwtUser.getUsername());

			redisService.hset(AuthPrefix.getTokenType, token, JwtConstants.TOKEN_TYPE_SELF);
			LocalDateTime dateTime = LocalDateTime.now(); // Gets the current date and time
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
			userService.insertLastLoginDate(jwtUser.getUsername(), dateTime.format(formatter));

			logger.info("Exiting from createAuthToken method of AuthenticateController");
			return ResponseEntity.ok(response);

		} catch (BadCredentialsException e) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The username & password entered does not match a valid user account. Please check and try again.");
		} catch (DisabledException e) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "This user is blocked");
		}
	}
}