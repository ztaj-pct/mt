package com.pct.auth.controllers;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.apache.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pct.auth.config.JwtUser;
import com.pct.auth.dto.MessageDTO;
import com.pct.auth.dto.ResetPasswordBean;
import com.pct.auth.dto.UserOrganizationDetails;
import com.pct.auth.service.IUserService;
import com.pct.auth.util.MessageKeys;
import com.pct.auth.util.RestUtils;
import com.pct.auth.util.ValidationUtility;
import com.pct.common.constant.Constants;
import com.pct.common.controller.IApplicationController;
import com.pct.common.dto.ResponseDTO;
import com.pct.common.dto.UserDTO;
import com.pct.common.dto.UserResponse;
import com.pct.common.model.Organisation;
import com.pct.common.model.User;
import com.pct.common.util.Context;
import com.pct.common.util.Logutils;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@RequestMapping("/user")
public class UserController implements IApplicationController<User> {

	public static final String className = "UserController";
	private static final Logger logger = LoggerFactory.getLogger(UserController.class);
	@Autowired
	private IUserService userService;
	@Autowired
	private MessageSource responseMessageSource;
	@Autowired
	private RestUtils restUtils;

	@Autowired
	UserDetailsService userDetailsService;

	@GetMapping("/username")
	public ResponseEntity<UserDTO> getUserByIDForAnotherService(@Validated @RequestParam("user_name") String userName) {
		logger.info("Inside getUserByID Method From UserController");
		Context context = new Context();
		String methodName = "getUserByID";

		Logutils.log(className, methodName, context.getLogUUId(),
				" Before calling getUserByID Method From UserController Service method in controller", logger);

		UserDTO userDto = userService.findByUserName(userName, context);

		Logutils.log(className, methodName, context.getLogUUId(), " after calling getUserByID Method of UserController",
				logger);
		logger.info("Exiting from getUserByID Method of UserController");
		return new ResponseEntity<UserDTO>(userDto, HttpStatus.OK);

	}
	
	
	@GetMapping("/user-details")
	public ResponseEntity<UserOrganizationDetails> getUserOrganizationDetails(@Validated @RequestParam("userName") String userName) {
		logger.info("Inside getUserOrganizationDetails Method From UserController");
		Context context = new Context();
		String methodName = "getUserOrganizationDetails";

		Logutils.log(className, methodName, context.getLogUUId(),
				" Before calling getUserOrganizationDetails Method From UserController Service method in controller", logger);

		UserOrganizationDetails userOrganizationDetails = userService.findByEmail(userName, context);

		Logutils.log(className, methodName, context.getLogUUId(), " after calling getUserOrganizationDetails Method of UserController",
				logger);
		logger.info("Exiting from getUserOrganizationDetails Method of UserController");
		return new ResponseEntity<UserOrganizationDetails>(userOrganizationDetails, HttpStatus.OK);

	}

//TODO
//	@GetMapping("/username")
//	public ResponseEntity<Object> getUserByIDForAnotherService(@Validated @RequestParam("user_name") String userName) {
//		logger.info("Inside getUserByIDForAnotherService Method of UserController");
//		logger.info("userName:" + userName);
//		MessageDTO<Object> messageDto;
//		Boolean isAvailable = false;
//		ResponseEntity<Object> responseEntity = null;
//		try {
//
//			Context context = new Context();
//			Logutils.log(className, "getUserByIDForAnotherService", context.getLogUUId(),
//					"Before calling findByUserName Method of IUserService From UserController", logger);
//
//			UserDTO userDto = userService.findByUserName(userName, context);
//
//			Logutils.log(className, "getUserByIDForAnotherService", context.getLogUUId(),
//					" After calling findByUserName Method of IUserService From UserController", logger);
//
//			if (userDto != null) {
//				isAvailable = true;
//				messageDto = new MessageDTO<Object>(MessageKeys.USER_NAME_FETCHED_SUCCESSFULLY, true);
//				messageDto.setBody(isAvailable);
//				responseEntity = new ResponseEntity<Object>(messageDto, HttpStatus.OK);
//			} else {
//				messageDto = new MessageDTO<Object>(MessageKeys.USER_NAME_NOT_FOUND, false);
//				messageDto.setBody(isAvailable);
//				responseEntity = new ResponseEntity<Object>(messageDto, HttpStatus.BAD_REQUEST);
//			}
//		} catch (Exception e) {
//			logger.error("Exception occurred at getUserByIDForAnotherService of UserController:" + e);
//			messageDto = new MessageDTO<Object>(MessageKeys.USER_NAME_FETCHED_ERROR, false);
//			messageDto.setBody(isAvailable);
//			responseEntity = new ResponseEntity<Object>(messageDto, HttpStatus.INTERNAL_SERVER_ERROR);
//		}
//		logger.info("Exiting from getUserByIDForAnotherService Method of UserController");
//		return responseEntity;
//	}

	@ApiOperation(value = "Add New User", notes = "API To Add New User", response = UserDTO.class, tags = {
			"User Management" })
	@ApiResponses(value = { @ApiResponse(code = 201, message = "Created", response = UserDTO.class),
			@ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 401, message = "Unauthorized"),
			@ApiResponse(code = 403, message = "Forbidden"),
			@ApiResponse(code = 500, message = "Internal Server Error") })

	@PostMapping()
	public ResponseEntity<MessageDTO<UserDTO>> addUser(@Validated @RequestBody UserDTO userDto) {
		logger.info("Inside addUser Method of UserController");
		logger.info("UserDTO:" + userDto);
		Context context = new Context();
		JwtUser jwtUser = (JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		boolean roleAvailable = false;
		for (Map<String, Object> role : jwtUser.getRole()) {
			String roleName = (String) role.get("name");
			if (roleName.equalsIgnoreCase(Constants.ROLE_SUPER_ADMIN)
					|| roleName.equalsIgnoreCase(Constants.ROLE_CUSTOMER_ADMIN) || roleName.equalsIgnoreCase(Constants.ROLE_CALL_CENTER_USER) || roleName.equalsIgnoreCase(Constants.ROLE_ORGANIZATION_USER) || roleName.equalsIgnoreCase(Constants.ROLE_MAINTENANCE)) {
				roleAvailable = true;
				break;
			}
		}
		if (jwtUser != null && roleAvailable) {
			Logutils.log(className, "addUser", context.getLogUUId(),
					" userRoles.contains(Constants.ROLE_SUPER_ADMIN) || userRoles.contains(Constants.ROLE_CUSTOMER_MANAGER)",
					logger);
			try {
				if (ValidationUtility.isEmpty(userDto.getOrganisation())
						|| ValidationUtility.isEmpty(userDto.getOrganisation().getAccountNumber())) {
					return new ResponseEntity<MessageDTO<UserDTO>>(
							new MessageDTO<UserDTO>("Account number can not be null", false), HttpStatus.CONFLICT);
				}
				Organisation company = restUtils
						.getCompanyFromCompanyService(userDto.getOrganisation().getAccountNumber());
				userDto.setOrganisation(company);
				if (userDto.getId() != null && userDto.getId() != 0) {
					UserDTO userRsponseDto = userService.updateExistingUser(userDto, context);
					return new ResponseEntity<MessageDTO<UserDTO>>(
							new MessageDTO<UserDTO>("User Updated Successfully", true), HttpStatus.CREATED);
				} else {

					logger.debug("Inside try block of addUser Method From UserController");
					if (ValidationUtility.isEmpty(userDto.getEmail())) {
						logger.info("Null email ID:" + userDto.getEmail());
						return new ResponseEntity<MessageDTO<UserDTO>>(
								new MessageDTO<UserDTO>("Email Can not be null", false), HttpStatus.CONFLICT);
					}
					if (userService.existsByEmail(userDto.getEmail(), context)) {
						logger.info("Duplicate email ID:" + userDto.getEmail());
						return new ResponseEntity<MessageDTO<UserDTO>>(new MessageDTO<UserDTO>(responseMessageSource
								.getMessage(MessageKeys.DUPLICATE_EMAIL_ID, new Object[] {}, Locale.ENGLISH), false),
								HttpStatus.CONFLICT);
					}
					if (ValidationUtility.isEmpty(userDto.getPhone()) && userDto.getPhone().length() == 10) {
						logger.info("Null Phone number:" + userDto.getEmail());
						return new ResponseEntity<MessageDTO<UserDTO>>(
								new MessageDTO<UserDTO>("Phone Number Can not be null & Valide Phone number required",
										false),
								HttpStatus.CONFLICT);
					}
					if (userService.existsByMobileNumber(userDto.getPhone(), context)) {
						logger.info("Duplicate mobile number:" + userDto.getPhone());
						return new ResponseEntity<MessageDTO<UserDTO>>(
								new MessageDTO<UserDTO>(responseMessageSource.getMessage(
										MessageKeys.DUPLICATE_MOBILE_NUMBER, new Object[] {}, Locale.ENGLISH), false),
								HttpStatus.CONFLICT);
					}

					if (ValidationUtility.isEmpty(userDto.getUserName())
							|| userService.existsByUserName(userDto.getUserName(), context)) {
						logger.info("Duplicate username:" + userDto.getUserName());
						return new ResponseEntity<MessageDTO<UserDTO>>(
								new MessageDTO<UserDTO>(
										responseMessageSource.getMessage(MessageKeys.DUPLICATE_USER_NAME,
												new Object[] { userDto.getUserName() }, Locale.ENGLISH),
										false),
								HttpStatus.CONFLICT);
					}

					if (ValidationUtility.isEmpty(userDto.getFirstName()) && userDto.getFirstName().length() <= 20) {
						logger.info("First name null or invalid:" + userDto.getEmail());
						return new ResponseEntity<MessageDTO<UserDTO>>(
								new MessageDTO<UserDTO>("Please enter correct first name/maximum lenght 20", false),
								HttpStatus.CONFLICT);
					}

					if (ValidationUtility.isEmpty(userDto.getLastName()) && userDto.getLastName().length() <= 20) {
						logger.info("Null last name:" + userDto.getEmail());
						return new ResponseEntity<MessageDTO<UserDTO>>(
								new MessageDTO<UserDTO>("Please enter correct last name/maximum lenght 20", false),
								HttpStatus.CONFLICT);
					}

					Logutils.log(className, "addUser", context.getLogUUId(),
							"Before calling createUser method of IUserService from UserController", logger);

					UserDTO userRsponseDto = userService.createUser(userDto, context);

					Logutils.log(className, "addUser", context.getLogUUId(),
							" After calling createUser method of IUserService from UserController", logger);

					logger.info("Exiting from addUser method of UserController");
					return new ResponseEntity<MessageDTO<UserDTO>>(
							new MessageDTO<UserDTO>(
									responseMessageSource.getMessage(MessageKeys.USER_ADDED_SUCCESSFULLY,
											new Object[] { userRsponseDto.getUserName() }, Locale.ENGLISH),
									true),
							HttpStatus.CREATED);
				}
			} catch (Exception e) {
				logger.info("Exception occurred at addUser method of UserController:" + e);
				return new ResponseEntity<MessageDTO<UserDTO>>(
						new MessageDTO<UserDTO>("Exception Occured:" + e.toString(), false),
						HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} else {
			logger.info("User is Unauthorized");
			return new ResponseEntity<MessageDTO<UserDTO>>(new MessageDTO<UserDTO>(
					responseMessageSource.getMessage(MessageKeys.USER_UNAUTHORIZED, new Object[] {}, Locale.ENGLISH),
					false), HttpStatus.FORBIDDEN);
		}
	}

	@ApiOperation(value = "Show All User", notes = "API To Fetch All Usres", response = UserDTO.class, tags = {
			"User Management" })
	@ApiResponses(value = { @ApiResponse(code = 201, message = "Fetch All", response = UserDTO.class),
			@ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 401, message = "Unauthorized"),
			@ApiResponse(code = 403, message = "Forbidden"),
			@ApiResponse(code = 500, message = "Internal Server Error") })

	@GetMapping("/all")
	public ResponseEntity<MessageDTO<List<UserDTO>>> getAllUsers() {
		logger.info("Inside getAllUsers Method of UserController");
		try {
			Context context = new Context();
			Logutils.log(className, "getAllUsers", context.getLogUUId(),
					"Before calling getAllUsers method of IUserService from UserController", logger);

			List<UserDTO> al = userService.getAllUsers(context);
			Logutils.log(className, "getAllUsers", context.getLogUUId(),
					"After calling getAllUsers method of IUserService from UserController", logger);
			if (!al.isEmpty()) {
				logger.info("Exiting from getAllUsers method of UserController");
				return new ResponseEntity<MessageDTO<List<UserDTO>>>(new MessageDTO<List<UserDTO>>("", al, true),
						HttpStatus.OK);
			} else {
				logger.info("Exiting from getAllUsers method of UserController");
				return new ResponseEntity<MessageDTO<List<UserDTO>>>(
						new MessageDTO<List<UserDTO>>("Data not found", false), HttpStatus.CONFLICT);
			}
		} catch (Exception e) {
			logger.info("Exception occurred at getAllUsers method of UserController:" + e);
			return new ResponseEntity<MessageDTO<List<UserDTO>>>(
					new MessageDTO<List<UserDTO>>("Exception Occred:" + e, false), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@ApiOperation(value = "Get User", notes = "API To Fetch User Based On User ID", response = UserDTO.class, tags = {
			"User Management" })
	@ApiResponses(value = { @ApiResponse(code = 201, message = "Fetched", response = UserDTO.class),
			@ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 401, message = "Unauthorized"),
			@ApiResponse(code = 403, message = "Forbidden"),
			@ApiResponse(code = 500, message = "Internal Server Error") })

	@GetMapping("/get/{id}")
	public ResponseEntity<MessageDTO<UserDTO>> getUser(@PathVariable(value = "id", required = false) Long id) {
		logger.info("Inside getUser Method of UserController");
		logger.info("Userid:" + id);
		try {
			Context context = new Context();
			Logutils.log(className, "getUser", context.getLogUUId(),
					"Before calling getUserById method of IUserService from UserController", logger);

			UserDTO userDTO = userService.getUserById(id, context);
			Logutils.log(className, "getUser", context.getLogUUId(),
					"After calling getUserById method of IUserService from UserController", logger);

			logger.info("Exiting from getUser method of UserController");
			return new ResponseEntity<MessageDTO<UserDTO>>(new MessageDTO<UserDTO>("", userDTO), HttpStatus.OK);
		} catch (Exception e) {
			logger.info("Exception occurred at getUser method of UserController:" + e);
			return new ResponseEntity<MessageDTO<UserDTO>>(
					new MessageDTO<UserDTO>("Data not found for ID:" + id, false), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/error-code/{code}")
	public ResponseEntity<HttpResponse> getUser(@PathVariable(value = "code", required = false) Integer code) {
		logger.info("Inside getUser Method of UserController");
		try {
			if (code == 503) {
				return new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
			} else if (code == 504) {
				return new ResponseEntity<>(HttpStatus.GATEWAY_TIMEOUT);
			}
		} catch (Exception e) {
			logger.info("Exception occurred at getUser method of UserController:" + e);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return null;

	}

	@ApiOperation(value = "Delete User", notes = "API To Delete User Based On User ID", response = UserDTO.class, tags = {
			"User Management" })
	@ApiResponses(value = { @ApiResponse(code = 201, message = "Deleted", response = UserDTO.class),
			@ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 401, message = "Unauthorized"),
			@ApiResponse(code = 403, message = "Forbidden"),
			@ApiResponse(code = 500, message = "Internal Server Error") })

	@DeleteMapping("/{id}")
	public ResponseEntity<MessageDTO<UserDTO>> deleteUser(@PathVariable(value = "id", required = false) Long id) {
		logger.info("Inside deleteUser Method of UserController");
		logger.info("Userid:" + id);
		try {
			Context context = new Context();
			Logutils.log(className, "deleteUser", context.getLogUUId(),
					"Before calling deleteUserById method of IUserService from UserController", logger);

			userService.deleteUserById(id, context);

			Logutils.log(className, "deleteUser", context.getLogUUId(),
					"After calling deleteUserById method of IUserService from UserController", logger);

			logger.info("Exiting from deleteUser method of UserController");

			return new ResponseEntity<MessageDTO<UserDTO>>(
					new MessageDTO<UserDTO>("User Deleted Successfully with ID:" + id, true), HttpStatus.OK);
		} catch (Exception e) {
			logger.info("Exception occurred at deleteUser method of UserController:" + e);
			return new ResponseEntity<MessageDTO<UserDTO>>(
					new MessageDTO<UserDTO>("User not found for ID:" + id, false), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@ApiOperation(value = "Update User", notes = "API To Update Usres", response = UserDTO.class, tags = {
			"User Management" })
	@ApiResponses(value = { @ApiResponse(code = 201, message = "Updated", response = UserDTO.class),
			@ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 401, message = "Unauthorized"),
			@ApiResponse(code = 403, message = "Forbidden"),
			@ApiResponse(code = 500, message = "Internal Server Error") })

	@PutMapping()
	public ResponseEntity<MessageDTO<UserDTO>> updateUser(@Valid @RequestBody UserDTO userDto) {
		logger.info("Inside updateUser Method of UserController");
		logger.info("UserDTO:" + userDto);

		try {
			Context context = new Context();
			UserDTO udto = userService.getUserById(userDto.getId(), context);
			if (udto != null) {
				Organisation company = restUtils
						.getCompanyFromCompanyService(userDto.getOrganisation().getAccountNumber());
				userDto.setOrganisation(company);
//				JwtUser jwtUser = (JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//				for (Long roleId : jwtUser.getRoleId()) {
//					
//				}
//				userDto.getRole().setRoleId(jwtUser.getRoleId());
				Logutils.log(className, "updateUser", context.getLogUUId(),
						"Before calling updateUser method of IUserService from UserController", logger);

				userDto = userService.updateUser(userDto, context);
				Logutils.log(className, "updateUser", context.getLogUUId(),
						"Before calling updateUser method of IUserService from UserController", logger);

				logger.info("Exiting from updateUser method of UserController");

				return new ResponseEntity<MessageDTO<UserDTO>>(
						new MessageDTO<UserDTO>(MessageKeys.USER_UPDATED_SUCCESSFULLY + userDto, true), HttpStatus.OK);
			} else {
				logger.info("User not found for update");
				return new ResponseEntity<MessageDTO<UserDTO>>(
						new MessageDTO<UserDTO>("Please Enter Valid Data:" + userDto, false), HttpStatus.CONFLICT);
			}
		} catch (Exception e) {
			logger.info("Exception occurred at updateUser method of UserController:" + e);
			return new ResponseEntity<MessageDTO<UserDTO>>(new MessageDTO<UserDTO>("Exception Occred:" + e, false),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@ApiResponses(value = { @ApiResponse(code = 201, message = "Rest Password", response = UserDTO.class),
			@ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 401, message = "Unauthorized"),
			@ApiResponse(code = 403, message = "Forbidden"),
			@ApiResponse(code = 500, message = "Internal Server Error") })

	@ApiOperation(value = "Reset the password through Admin", notes = "API to reset the password by Admin", response = UserDTO.class, tags = {
			"User Management" })
	@PostMapping(value = "/reset-password/admin/{email}")
	public ResponseEntity<MessageDTO<String>> resetPassword(@Validated @PathVariable("email") String email) {
		logger.info("Inside resetPassword Method of UserController");
		logger.info("Email:" + email);

		try {
			Context context = new Context();
			UserDTO userDto = null;
			Logutils.log(className, "resetPassword", context.getLogUUId(),
					" Before calling forgotPassword Method of IUserService From UserController", logger);

			userDto = userService.forgotPassword(email, context);

			Logutils.log(className, "resetPassword", context.getLogUUId(),
					" after calling forgotPassword Method of IUserService From UserController", logger);

			logger.info("Exiting from resetPassword Method of UserController");
			
			return new ResponseEntity<MessageDTO<String>>(
					new MessageDTO<String>(
							responseMessageSource.getMessage(MessageKeys.RESET_PASSWORD_BY_ADMIN,
									new Object[] { email }, Locale.ENGLISH),
							true),
					HttpStatus.OK);
		} catch (Exception e) {
			logger.info("Exception occurred at resetPassword method of UserController:" + e);
			return new ResponseEntity<MessageDTO<String>>(
					new MessageDTO<String>("Exception while resetting passowrd:- "+e.getMessage(),
							false),
					HttpStatus.OK);

		}
	}

	@PutMapping(value = "/update-password/admin")
	public ResponseEntity<MessageDTO<String>> resetPassword(@RequestBody ResetPasswordBean bean) {
		JwtUser jwtUser = (JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		boolean b = userService.resetPassword(bean, jwtUser);
		if (!b) {
			 return new ResponseEntity<>(new MessageDTO<>(
	                    responseMessageSource.getMessage(MessageKeys.INCORRECT_EXISITING_PASSWORD, new Object[]{}, Locale.ENGLISH), false),
	                    HttpStatus.BAD_REQUEST);
		} else {
			return new ResponseEntity<>(new MessageDTO<>(responseMessageSource
					.getMessage(MessageKeys.PASSWORD_RESET_SUCCESSFULLY, new Object[] {}, Locale.ENGLISH), true),
					HttpStatus.OK);
		}
	}

	@ApiOperation(value = "Check User Availability", notes = "API To User Availability", response = UserDTO.class, tags = {
			"User Management" })
	@ApiResponses(value = { @ApiResponse(code = 201, message = "Availability", response = UserDTO.class),
			@ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 401, message = "Unauthorized"),
			@ApiResponse(code = 403, message = "Forbidden"),
			@ApiResponse(code = 500, message = "Internal Server Error") })

	@PostMapping(value = "/username-availability")
	public ResponseEntity<Object> checkUserNameAvailability(@RequestParam("username") String username) {
		logger.info("Inside checkUserNameAvailability method of UserController");
		logger.info("Username:" + username);
		MessageDTO<Object> messageDto;
		try {
			Context context = new Context();
			Logutils.log(className, "resetPassword", context.getLogUUId(),
					" Before calling findByUserName Method of IUserService From UserController", logger);

			UserDTO userDto = userService.findByUserName(username, context);

			Logutils.log(className, "resetPassword", context.getLogUUId(),
					" After calling findByUserName Method of IUserService From UserController", logger);

			if (userDto != null) {
				messageDto = new MessageDTO<Object>("Email Already exist", true);
				messageDto.setBody(true);
			} else {
				messageDto = new MessageDTO<Object>("Email Not Available", false);
				messageDto.setBody(false);
			}
			logger.info("Exiting from checkUserNameAvailability Method of UserController");

			return new ResponseEntity<Object>(messageDto, HttpStatus.OK);
		} catch (Exception e) {
			logger.info("Exception occurred at checkUserNameAvailability method of UserController:" + e);
			// messageDto = new MessageDTO<Object>(MessageKeys.EMAIL_FETCHED_ERROR, false);
			// messageDto.setBody(false);
			// return new ResponseEntity<Object>(messageDto,
			// HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return null;
	}

	@ApiOperation(value = "Get All Users Based On Role", notes = "API to Fetch All Role Based Users", response = String.class, tags = {
			"User Management" })
	@ApiResponses(value = { @ApiResponse(code = 201, message = "AllUsersByRole", response = UserDTO.class),
			@ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 401, message = "Unauthorized"),
			@ApiResponse(code = 403, message = "Forbidden"),
			@ApiResponse(code = 500, message = "Internal Server Error") })

	@GetMapping(value = "/adminUser/{roleID}")
	public ResponseEntity<MessageDTO<UserDTO>> getAllUsersByRole(@PathVariable("roleID") Long roleID) {
		logger.info("Inside getAllUsersByRole Method From UserController");
		logger.info("Role ID:" + roleID);
		try {
			Context context = new Context();

			Logutils.log(className, "getAllUsersByRole", context.getLogUUId(),
					"Before calling getAllUsersByRole Method of IUserService From UserController", logger);
			List<UserDTO> useList = userService.getAllUsersByRole(roleID, context);

			Logutils.log(className, "getAllUsersByRole", context.getLogUUId(),
					" After calling getAllUsersByRole Method of IUserService From UserController", logger);

			logger.info("Exiting from getAllUsersByRole of UserController");
			if (useList != null) {
				return new ResponseEntity<MessageDTO<UserDTO>>(new MessageDTO<UserDTO>("" + useList, true),
						HttpStatus.OK);
			} else {
				return new ResponseEntity<MessageDTO<UserDTO>>(
						new MessageDTO<UserDTO>("fpgateway.error.roleId.notFound", false), HttpStatus.BAD_REQUEST);
			}
		} catch (Exception e) {
			logger.info("Exception occurred at getAllUsersByRole method of UserController:" + e);
			return new ResponseEntity<MessageDTO<UserDTO>>(new MessageDTO<UserDTO>("Exception Occred:" + e, false),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@ApiOperation(value = "Load By Username", notes = "API for Username Based User", response = String.class, tags = {
			"User Management" })
	@ApiResponses(value = { @ApiResponse(code = 201, message = "LoadUserByUsername", response = UserDTO.class),
			@ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 401, message = "Unauthorized"),
			@ApiResponse(code = 403, message = "Forbidden"),
			@ApiResponse(code = 500, message = "Internal Server Error") })

	@GetMapping(value = "/loadUser/{userName}")
	public ResponseEntity<MessageDTO<JwtUser>> loadUserByUsername(@PathVariable("userName") String userName) {
		logger.info("Inside loadUserByUsername Method From UserController");
		logger.info("UserName:" + userName);
		try {
			Context context = new Context();
			Logutils.log(className, "loadUserByUsername", context.getLogUUId(),
					"Before calling loadUserByUsername Method of IUserService UserController Service method in controller",
					logger);

			JwtUser jwtUser = (JwtUser) userDetailsService.loadUserByUsername(userName);

			Logutils.log(className, "loadUserByUsername", context.getLogUUId(),
					" after calling loadUserByUsername Method of IUserService From UserController", logger);
			logger.info("Exiting from loadUserByUsername Method of UserController");
			if (jwtUser != null) {
				return new ResponseEntity<MessageDTO<JwtUser>>(new MessageDTO<JwtUser>("" + jwtUser, true),
						HttpStatus.OK);
			} else {
				return new ResponseEntity<MessageDTO<JwtUser>>(
						new MessageDTO<JwtUser>("fpgateway.error.roleId.notFound", false), HttpStatus.BAD_REQUEST);
			}
		} catch (Exception e) {
			logger.info("Exception occurred at loadUserByUsername method of UserController:" + e);
			return new ResponseEntity<MessageDTO<JwtUser>>(new MessageDTO<JwtUser>("Exception Occred:" + e, false),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@ApiOperation(value = "Get user list with pagination", notes = "API to get user list  with pagination", response = Object.class, tags = {
			"User Management" })
	@ApiResponses(value = { @ApiResponse(code = 201, message = "get with pagination", response = Object.class),
			@ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 401, message = "Unauthorized"),
			@ApiResponse(code = 403, message = "Forbidden"),
			@ApiResponse(code = 500, message = "Internal Server Error") })

	@PostMapping("/page")
	public ResponseEntity<MessageDTO<Object>> getUserListWithPagination(
			@RequestParam(value = "_page", required = false) Integer page,
			@RequestParam(value = "_limit", required = false) Integer size,
			@RequestParam(value = "_sort", required = false) String sort,
			@RequestParam(value = "_order", required = false) String order,
			@RequestParam(value = "_filterModelCountFilter", required = false) String filterModelCountFilter,
			@RequestBody Map<String, String> filterValues, HttpServletRequest httpServletRequest) {
		logger.info("inside getUserListWithPagination method");
		logger.info("page:" + page);
		logger.info("size:" + size);
		logger.info("sort:" + sort);
		logger.info("order:" + order);
		logger.info("filterModelCountFilter:" + filterModelCountFilter);
		logger.info("filterValues:" + filterValues);
		try {
			Context context = new Context();
			JwtUser jwtUser = (JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			Logutils.log(className, "getUserListWithPagination", context.getLogUUId(),
					" Before calling getUserListWithPagination Method of IUserService ", logger);

			Page<UserResponse> userPage = userService.getUserListWithPagination(filterValues, filterModelCountFilter,
					getPageable(page - 1, size, sort, order), context,jwtUser.getUsername(), sort);

			Logutils.log(className, "getUserListWithPagination", context.getLogUUId(),
					" After calling getUserListWithPagination Method of IUserService ", logger);

			return new ResponseEntity<MessageDTO<Object>>(new MessageDTO<Object>(responseMessageSource.getMessage(
					"User list with pagination Fetched successfully", new Object[] {}, Locale.ENGLISH), userPage, true),
					HttpStatus.OK);
		} catch (PropertyReferenceException e) {
			logger.error("Exception occure while fetching User list with pagination", e);
			return new ResponseEntity<MessageDTO<Object>>(new MessageDTO<Object>(
					responseMessageSource.getMessage("Exception occure while fetching User list with pagination",
							new Object[] {}, Locale.ENGLISH),
					e.toString(), true), HttpStatus.NOT_FOUND);
		} catch (Exception e) {
			logger.error("Exception occure while fetching User list with pagination", e);
			return new ResponseEntity<MessageDTO<Object>>(new MessageDTO<Object>(
					responseMessageSource.getMessage("Exception occure while fetching User list with pagination",
							new Object[] {}, Locale.ENGLISH),
					e.toString(), true), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@GetMapping("/is-user")
	public ResponseEntity<ResponseDTO> getIsUserAssociateWithLocation(@Validated @RequestParam("locationId") Long locationId) {
		logger.info("Inside getIsUserAssociateWithLocation Method From UserController");
		Context context = new Context();
		String methodName = "getIsUserAssociateWithLocation";

		Logutils.log(className, methodName, context.getLogUUId(),
				" Before calling getIsUserAssociateWithLocation Method From UserController Service method in controller", logger);

		Boolean status = userService.isUserAssociateWithLocation(locationId);

		Logutils.log(className, methodName, context.getLogUUId(), " after calling getIsUserAssociateWithLocation Method of UserController",
				logger);
		logger.info("Exiting from getIsUserAssociateWithLocation Method of UserController");
		return new ResponseEntity<>(new ResponseDTO(status, "Success"),
				HttpStatus.OK);

	}
}