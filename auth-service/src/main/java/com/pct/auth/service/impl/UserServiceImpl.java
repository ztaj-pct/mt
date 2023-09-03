package com.pct.auth.service.impl;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.pct.auth.config.JwtUser;
import com.pct.auth.dto.OrganisationDetails;
import com.pct.auth.dto.ResetPasswordBean;
import com.pct.auth.dto.UserOrganizationDetails;
import com.pct.auth.repository.RoleRepository;
import com.pct.auth.repository.UserRepository;
import com.pct.auth.service.IUserService;
import com.pct.auth.specification.UserSpecification;
import com.pct.auth.util.MailUtil;
import com.pct.auth.util.RestUtils;
import com.pct.auth.util.UserConverter;
import com.pct.auth.util.Util;
import com.pct.auth.util.ValidationUtility;
import com.pct.common.constant.Constants;
import com.pct.common.constant.OrganisationRole;
import com.pct.common.dto.LocationDTO;
import com.pct.common.dto.RoleDTO;
import com.pct.common.dto.UserDTO;
import com.pct.common.dto.UserResponse;
import com.pct.common.model.Location;
import com.pct.common.model.Organisation;
import com.pct.common.model.Role;
import com.pct.common.model.User;
import com.pct.common.util.Context;
import com.pct.common.util.JwtUtil;
import com.pct.common.util.Logutils;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
@Service(value = "userService")
public class UserServiceImpl implements UserDetailsService, IUserService {

	Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

	public static final String className = "UserServiceImpl";
	@Autowired
	private PasswordEncoder passwordEncoder;
	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	UserRepository userRepository;

	@Autowired
	UserConverter userConverter;

	@Value("${twilio.verify.serviceId}")
	private String twilioServiceId;

	@Value("${twilio.accountSid}")
	private String accountSid;

	@Value("${twilio.authToken}")
	private String authToken;

	@Value("${twilio.phoneNumber}")
	private String number;
	@Autowired
	private MailUtil mailUtil;

	@Autowired
	private JwtUtil jwtUtil;
	
	@Autowired
	RestUtils restUtils;

	public JwtUser loadUserByUsername(String username) throws UsernameNotFoundException {
		Optional<User> userOptional = userRepository.findByUserName(username);
		if (!userOptional.isPresent()) {
			throw new UsernameNotFoundException("Invalid username or password.");
		}
		User user = userOptional.get();
		if (user == null) {
			throw new UsernameNotFoundException("Invalid username or password.");
		}
		JwtUser jwtUser = new JwtUser();
		jwtUser.setUsername(username);
		jwtUser.setPassword(user.getPassword());
		List<Map<String, Object>> roleList = new ArrayList<>();
		List<String> roleNameList = new ArrayList<>();
		for (Role role : user.getRole()) {
			Map<String, Object> roleObject = new HashMap<>();
			roleObject.put("roleId", role.getRoleId());
			roleObject.put("name", role.getName());
			roleNameList.add(role.getName());
			roleList.add(roleObject);
		}
		jwtUser.setRoleName(roleNameList);
		jwtUser.setRole(roleList);
		jwtUser.setUserId(user.getId());
		if(user.getOrganisation()!=null&&user.getOrganisation().getAccountNumber()!=null) {
			jwtUser.setAccountNumber(user.getOrganisation().getAccountNumber());
		}
		return jwtUser;
		// return new
		// org.springframework.security.core.userdetails.User(user.getUsername(),
		// user.getPassword(), new HashSet<>());
	}

	public User loadUserByUsernameForLogin(String username) throws UsernameNotFoundException {
		Optional<User> userOptional = userRepository.findByUserName(username);
		if (!userOptional.isPresent()) {
			throw new UsernameNotFoundException("Invalid username or password.");
		}
		User user = userOptional.get();
		if (user == null) {
			throw new UsernameNotFoundException("Invalid username or password.");
		}
		
		return user;
		// return new
		// org.springframework.security.core.userdetails.User(user.getUsername(),
		// user.getPassword(), new HashSet<>());
	}
	
	public JwtUser convertUserToJwtUser(User user) {
		JwtUser jwtUser = new JwtUser();
		jwtUser.setUsername(user.getUserName());
		jwtUser.setPassword(user.getPassword());
		List<Map<String, Object>> roleList = new ArrayList<>();
		List<String> roleNameList = new ArrayList<>();
		for (Role role : user.getRole()) {
			Map<String, Object> roleObject = new HashMap<>();
			roleObject.put("roleId", role.getRoleId());
			roleObject.put("name", role.getName());
			roleNameList.add(role.getName());
			roleList.add(roleObject);
		}
		jwtUser.setRoleName(roleNameList);
		jwtUser.setRole(roleList);
		jwtUser.setUserId(user.getId());
		if(user.getOrganisation()!=null&&user.getOrganisation().getAccountNumber()!=null) {
			jwtUser.setAccountNumber(user.getOrganisation().getAccountNumber());
		}
		return jwtUser;
	}
	
//	public List<PermissionDto> getAllPermissionsByUsername(String username) {
//		User user = userRepository.findByUserName(username).get();
//		List<PermissionDto> permissionDtoList = new ArrayList<PermissionDto>();
//		for (PermissionEntity permission : user.getRole().getPermissions()) {
//			PermissionDto permissionDto = modelMapper.map(permission, PermissionDto.class);
//			permissionDtoList.add(permissionDto);
//		}
//		return permissionDtoList;
//	}

	@Override
	public UserDTO createUser(UserDTO udto, Context context) {
		logger.info("Inside createUser method of UserServiceImpl");
		UserDTO userDto = null;
		User user = userConverter.userDtoToUser(udto);
		user.setPassword(passwordEncoder.encode(udto.getPassword()));
		if (udto.getRole() != null) {
			List<Role> roles = new ArrayList<Role>();
			for (RoleDTO role : udto.getRole()) {
				Role dbRole = roleRepository.findByRoleId(role.getRoleId());
				roles.add(dbRole);
			}
			user.setRole(roles);
		}
		
		if(udto.getHomeLocation() != null) {
			try {
				Location location = restUtils.getLocationFromOrganisationService(udto.getHomeLocation().getId());
				user.setHomeLocation(location);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		if(udto.getAdditionalLocations() != null) {
			List<Location> addtionalLocations = new ArrayList<>();
			for(LocationDTO locationDTO: udto.getAdditionalLocations()) {
				try {
					Location location = restUtils.getLocationFromOrganisationService(locationDTO.getId());
					addtionalLocations.add(location);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			user.setAdditionalLocations(addtionalLocations);
		}
//	user.setRole(roleRepository.findByRoleId(udto.getId()));
		String uuid = UUID.randomUUID().toString();
		while (userRepository.existsByUuid(uuid)) {
			uuid = UUID.randomUUID().toString();
		}
		user.setUuid(uuid);
		Logutils.log("UserServiceImpl", "createUser", context.getLogUUId(),
				" Before calling save method of UserRepository from UserServiceImpl", logger);

		user = userRepository.save(user);
		Logutils.log("UserServiceImpl", "createUser", context.getLogUUId(),
				"After calling save method of UserRepository from UserServiceImpl", logger);
//		if (user != null && udto.getRole() != null) {
//			//user.setRole(udto.getRole());
//		//	user = userRepository.save(user);
//			if (user != null) {
//				userDto = userConverter.userToUserDTO(user);
//			}
//		} else if (user != null) {
		userDto = userConverter.userToUserDTO(user);
//		}
		logger.info("Exiting from createUser Method of UserServiceImpl");
		return userDto;
	}

	@Override
	public List<UserDTO> getAllUsers(Context context) {
		logger.info("Inside getAllUsers method of UserServiceImpl");

		Logutils.log("UserServiceImpl", "getAllUsers", context.getLogUUId(),
				" Before calling findAll method of UserRepository from UserServiceImpl", logger);

		List<User> al = userRepository.findAll();

		Logutils.log("UserServiceImpl", "getAllUsers", context.getLogUUId(),
				" After calling findAll method of UserRepository from UserServiceImpl", logger);

		final List<UserDTO> list = new ArrayList<UserDTO>();
		al.forEach(u -> {
			UserDTO dto = userConverter.userToUserDTO(u);
			if (u.getOrganisation() != null) {
				dto.setOrganisation(dto.getOrganisation());
				list.add(dto);
			}
		});
		if (list.isEmpty()) {
			return null;
		}
		logger.info("Exiting from getAllUsers Method of UserServiceImpl");
		return list;
	}

	@Override
	public UserDTO getUserById(Long id, Context context) {
		logger.info("Inside getUserById method of UserServiceImpl");

		Logutils.log("UserServiceImpl", "getUserById", context.getLogUUId(),
				" Before calling findById method of UserRepository from UserServiceImpl", logger);
		User u = userRepository.findById(id).get();

		Logutils.log("UserServiceImpl", "getUserById", context.getLogUUId(),
				" After calling findById method of UserRepository from UserServiceImpl", logger);

		UserDTO dto = u != null ? userConverter.userToUserDTO(u) : null;

		logger.info("Exiting from getAllUsers Method of UserServiceImpl");
		return dto;
	}

	@Override
	public Boolean deleteUserById(final Long id, Context context) {
		logger.info("Inside deleteUserById method of UserServiceImpl");
		Boolean flag = false;
		Logutils.log("UserServiceImpl", "deleteUserById", context.getLogUUId(),
				" Before calling getById method of UserRepository from UserServiceImpl", logger);

		User user = userRepository.getById(id);

		Logutils.log("UserServiceImpl", "deleteUserById", context.getLogUUId(),
				" After calling getById method of UserRepository from UserServiceImpl", logger);

		if (user != null) {
			Logutils.log("UserServiceImpl", "deleteUserById", context.getLogUUId(),
					" Before calling deleteById method of UserRepository from UserServiceImpl", logger);
//			userRepository.deleteById(id);
			user.setIsActive(false);

			user.setUpdatedAt(Instant.now());
			UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication()
					.getPrincipal();
			String username = userDetails.getUsername();
			user.setUpdatedBy(username);
			userRepository.save(user);
			Logutils.log("UserServiceImpl", "deleteUserById", context.getLogUUId(),
					" After calling deleteById method of UserRepository from UserServiceImpl", logger);
			flag = true;
		}
		logger.info("Exiting from deleteUserById Method of UserServiceImpl");
		return flag;
	}

	@Override
	public UserDTO updateUser(UserDTO udto, Context context) {
		logger.info("Inside updateUser method of UserServiceImpl");
		UserDTO dto = null;
		User user = userRepository.findById(udto.getId()).get();
		user.setEmail(udto.getEmail());
		user.setFirstName(udto.getFirstName());
		user.setOrganisation(udto.getOrganisation());
		user.setIsActive(udto.getIsActive());
		user.setLastName(udto.getLastName());
		user.setNotify(udto.getNotify());
		user.setCountryCode(udto.getCountryCode());
		user.setPhone(udto.getPhone());
		user.setUserName(udto.getEmail());
		user.setUpdatedBy(jwtUtil.getUserFromContaxt());
		user.setUpdatedAt(Instant.now());
		List<Role> roles = new ArrayList<>();
		for (RoleDTO role : udto.getRole()) {
			Role r = roleRepository.findByRoleId(role.getRoleId());
			roles.add(r);
		}
		user.setRole(roles);
//		user.setRole(roleRepository.findByRoleId(udto.getId()));

		Logutils.log("UserServiceImpl", "updateUser", context.getLogUUId(),
				" Before calling save method of UserRepository from UserServiceImpl", logger);
		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		String username = userDetails.getUsername();
		user.setUpdatedAt(Instant.now());
		user.setUpdatedBy(username);
		user = userRepository.save(user);

		Logutils.log("UserServiceImpl", "updateUser", context.getLogUUId(),
				" After calling save method of UserRepository from UserServiceImpl", logger);

		dto = userConverter.userToUserDTO(user);
		logger.info("Exiting from updateUser Method of UserServiceImpl");
		return dto;
	}

	@Override
	public UserDTO forgotPassword(String email, Context context) {
		logger.info("Inside forgotPassword method of UserServiceImpl");
		UserDTO userDto = null;
		User user = null;
		try {
			user = userRepository.findByEmail(email).get();
		} catch (Exception e) {
			// TODO: handle exception
			throw new UsernameNotFoundException("User Not exist by this email:- " + email);
		}
		if (user != null) {
			String pwd = Util.generatePassword(16);
			user.setPassword(passwordEncoder.encode(pwd));
			user.setIsPasswordChange(true);
			Logutils.log(className, "forgotPassword", context.getLogUUId(),
					" Before calling saveAndFlush method of UserRepository From UserServiceImpl ", logger);
			user = userRepository.saveAndFlush(user);
			Logutils.log(className, "forgotPassword", context.getLogUUId(),
					" After calling saveAndFlush method of UserRepository From UserServiceImpl ", logger);
			userDto = user != null ? userConverter.userToUserDTO(user) : null;
			mailUtil.sendMail(user, pwd);
		} else {
			throw new UsernameNotFoundException("User Not exist by this email:- " + email);
		}
		logger.info("Exiting of forgotPassword Method of UserServiceImpl");
		return userDto;
	}

	@Override
	public UserDTO findByUserName(String userName, Context context) {
		logger.info("Inside findByUserName method of UserServiceImpl");
		UserDTO userDto = null;
		Logutils.log(className, "findByUserName", context.getLogUUId(),
				" Before calling findByUserName method of UserRepository From UserServiceImpl ", logger);

		User user = userRepository.findUserByUserName(userName);

		Logutils.log(className, "findByUserName", context.getLogUUId(),
				" After calling findByUserName method of UserRepository From UserServiceImpl ", logger);
		userDto = user != null ? userConverter.userToUserDTO(user) : null;
		logger.info("Exiting of findByUserName method of UserServiceImpl");
		return userDto;
	}

	@Override
	public Boolean existsByEmail(String email, Context context) {
		logger.info("Inside existsByEmail Method of UserServiceImpl");
		Logutils.log(className, "existsByEmail", context.getLogUUId(),
				" Before calling existsByEmail method of UserRepository From UserServiceImpl ", logger);

		boolean flag = userRepository.existsByEmail(email);
		Logutils.log(className, "existsByEmail", context.getLogUUId(),
				" After calling existsByEmail method of UserRepository From UserServiceImpl ", logger);
		logger.info("Exiting of existsByEmail method of UserServiceImpl");
		return flag;
	}

	@Override
	public Boolean existsByMobileNumber(String mobileNumber, Context context) {
		logger.info("Inside existsByMobileNumber Method of UserServiceImpl ");
		Logutils.log(className, "existsByMobileNumber", context.getLogUUId(),
				" Before calling existsByPhone method of UserRepository From UserServiceImpl ", logger);
		Boolean flag = userRepository.existsByPhone(mobileNumber);
		Logutils.log(className, "existsByMobileNumber", context.getLogUUId(),
				" After calling existsByPhone method of UserRepository From UserServiceImpl ", logger);
		logger.info("Exiting of existsByMobileNumber method of UserServiceImpl");
		return flag;
	}

	@Override
	public Boolean existsByUserName(String userName, Context context) {
		logger.info("Inside existsByUserName Method of UserServiceImpl");
		Logutils.log(className, "existsByUserName", context.getLogUUId(),
				" Before calling existsByUserName method of UserRepository From UserServiceImpl ", logger);

		Boolean flag = userRepository.existsByUserName(userName);
		Logutils.log(className, "existsByUserName", context.getLogUUId(),
				" After calling existsByUserName method of  UserRepository From UserServiceImpl ", logger);
		logger.info("Exiting of existsByUserName method of UserServiceImpl");
		return flag;
	}

	@Override
	public List<UserDTO> getAllUsersByRole(Long roleId, Context context) {
		logger.info("Inside getAllUsersByRole Method of UserServiceImpl:" + roleId);
		List<UserDTO> list = new ArrayList<UserDTO>();
		Logutils.log(className, "getAllUsersByRole", context.getLogUUId(),
				" Before calling findByRoleId method of RoleRepository from UserServiceImpl ", logger);

		Role role = roleRepository.findByRoleId(roleId);
		Logutils.log(className, "getAllUsersByRole", context.getLogUUId(),
				" After calling findByRoleId method of RoleRepository from UserServiceImpl ", logger);

		if (role != null) {
			Logutils.log(className, "existsByUserName", context.getLogUUId(),
					" Before calling findByRole method of UserRepository From UserServiceImpl ", logger);

			List<User> users = userRepository.findByRole(role);
			Logutils.log(className, "existsByUserName", context.getLogUUId(),
					" After calling findByRole method of UserRepository From UserServiceImpl ", logger);

			for (User user : users) {
				list.add(userConverter.userToUserDTO(user));
			}
		}
		if (list.isEmpty()) {
			list = null;
		}
		logger.info("Exiting of getAllUsersByRole method of UserServiceImpl");

		return list;
	}

	@Override
	public Page<UserResponse> getUserListWithPagination(Map<String, String> filterValues, String filterModelCountFilter,
			Pageable pageable, Context context, String userName, String sort) {
		logger.info("Inside getUserListWithPagination method");
		User user = userRepository.findUserByUserName(userName);
		Specification<User> spc = UserSpecification.getDeviceListSpecification(filterValues, filterModelCountFilter,
				user, sort);
		Page<User> userDetails = null;
		Logutils.log(className, "getUserListWithPagination", context.getLogUUId(),
				" Before calling findAll method of UserRepository ", logger);
		userDetails = userRepository.findAll(spc, pageable);
		Logutils.log(className, "getUserListWithPagination", context.getLogUUId(),
				" After calling findAll method of UserRepository ", logger);
		Page<UserResponse> userRecordloadPage = userConverter.convertUserToUserDto(userDetails, pageable);
		logger.info("Exit from getUserListWithPagination method");
		return userRecordloadPage;
	}

	@Override
	public UserDTO updateExistingUser(UserDTO udto, Context context) {
		logger.info("Inside update user method of UserServiceImpl");
		UserDTO userDto = null;
		User user = userRepository.findById(udto.getId()).get();
		user.setOrganisation(udto.getOrganisation());
		if (!ValidationUtility.isEmpty(udto.getFirstName())) {
			user.setFirstName(udto.getFirstName());
		}
		if (!ValidationUtility.isEmpty(udto.getLastName())) {
			user.setLastName(udto.getLastName());
		}
		if (!ValidationUtility.isEmpty(udto.getNotify())) {
			user.setNotify(udto.getNotify());
		}
		LocalDateTime dateTime = LocalDateTime.now(); // Gets the current date and time
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
		user.setLastLoginTime(dateTime.format(formatter));

		if (!ValidationUtility.isEmpty(udto.getIsActive())) {
			if(!user.getIsActive()) {
				LocalDateTime dateTimes = LocalDateTime.now();
				DateTimeFormatter formatters = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
				user.setLastLoginTime(dateTimes.format(formatters));
			}
			user.setIsActive(udto.getIsActive());
		}
		if (!user.getEmail().equals(udto.getEmail()) && existsByEmail(udto.getEmail(), context)) {
			throw new UsernameNotFoundException("Email Already Exist");
		}
		user.setEmail(udto.getEmail());
		if (user.getPhone() != null) {
			if (!user.getPhone().equals(udto.getPhone()) && existsByMobileNumber(udto.getPhone(), context)) {
				throw new UsernameNotFoundException("Phone number Already Exist");
			}
		}
		user.setPhone(udto.getPhone());
		user.setCountryCode(udto.getCountryCode());
		if (!user.getUserName().equals(udto.getUserName()) && existsByUserName(udto.getUserName(), context)) {
			throw new UsernameNotFoundException("UserName Already Exist");
		}
		user.setUserName(udto.getUserName());
		user.getRole();
		if (udto.getRole() != null) {
			List<Role> roles = new ArrayList<Role>();
			for (RoleDTO role : udto.getRole()) {
				Role dbRole = roleRepository.findByRoleId(role.getRoleId());
				roles.add(dbRole);
			}
			user.setRole(roles);
		}
		
		if(udto.getHomeLocation() != null) {
			try {
				Location location = restUtils.getLocationFromOrganisationService(udto.getHomeLocation().getId());
				user.setHomeLocation(location);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		if(udto.getAdditionalLocations() != null) {
			List<Location> addtionalLocations = new ArrayList<>();
			for(LocationDTO locationDTO: udto.getAdditionalLocations()) {
				try {
					Location location = restUtils.getLocationFromOrganisationService(locationDTO.getId());
					addtionalLocations.add(location);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			user.setAdditionalLocations(addtionalLocations);
		}

		Logutils.log("UserServiceImpl", "createUser", context.getLogUUId(),
				" Before calling save method of UserRepository from UserServiceImpl", logger);

		try {
			UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication()
					.getPrincipal();
			String username = userDetails.getUsername();
			user.setUpdatedAt(Instant.now());
			user.setUpdatedBy(username);
			user = userRepository.save(user);
		} catch (Exception e) {
			e.printStackTrace();
			throw new UsernameNotFoundException(e.getMessage());
		}
		Logutils.log("UserServiceImpl", "updateUser", context.getLogUUId(),
				"After calling save method of UserRepository from UserServiceImpl", logger);
		userDto = userConverter.userToUserDTO(user);
//		}
		logger.info("Exiting from updateUser Method of UserServiceImpl");
		return userDto;
	}

	@Override
	public boolean resetPassword(ResetPasswordBean resetPasswordBean, JwtUser jwtUser) {
		User user = userRepository.findUserByUserName(jwtUser.getUsername());
		if (user != null && passwordEncoder.matches(resetPasswordBean.getOldpassword(), user.getPassword())) {
			try {
				userRepository.updatePassword(passwordEncoder.encode(resetPasswordBean.getNewpassword()),
						user.getEmail());
			} catch (Exception e) {
				throw new UsernameNotFoundException("Exception while updating password:-" + e.getMessage());
			}

			return true;
		} else {
			return false;
		}
	}

	public void insertLastLoginDate(String username, String dt) {
		userRepository.update(username, dt);

	}

	@Override
	public void updateLandingPage(String username, String landingPage) {
		userRepository.updateLandingPage(username, landingPage);
	}
	
	public void updateActiveStatus(String username) {
		userRepository.updateActiveStatus(username,false);
	}

	@Override
	public UserOrganizationDetails findByEmail(String email, Context context) {
		
		logger.info("Inside findByUserEmail method of UserServiceImpl");
		UserOrganizationDetails userOrganizationDetails = null;
		List<Organisation> orgAccessList = new ArrayList<>();
		Logutils.log(className, "findByUserEmail", context.getLogUUId(),
				" Before calling findByEmail method of UserRepository From UserServiceImpl ", logger);

		Optional<User> user = userRepository.findByEmail(email);
		if(!user.isPresent()) {
			throw new UsernameNotFoundException("User Not Found with provided email");
		}
		if(user.get().getOrganisation()!=null && user.get().getOrganisation().getAccessList().size()>0) {
			orgAccessList =  user.get().getOrganisation().getAccessList();
			
		}
		
		JwtUser jwtUser = (JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		logger.info("Logged In User : " + jwtUser.getUsername());
		
		Logutils.log(className, "findByUserEmail", context.getLogUUId(),
				" After calling findByUserEmail method of UserRepository From UserServiceImpl ", logger);
	
		userOrganizationDetails = userConverter.userToUserOrganizationDetails(user.get(), orgAccessList);
		
		logger.info("Exiting of findByUserEmail method of UserServiceImpl");
		return userOrganizationDetails;
	}

	@Override
	public Boolean isUserAssociateWithLocation(Long locationId) {

		List<User> userByLocationId = userRepository.getUserByLocationId(locationId);
		if(userByLocationId.isEmpty())
			return false;
		return true;
	}

}