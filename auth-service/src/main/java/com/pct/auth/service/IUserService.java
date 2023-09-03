package com.pct.auth.service;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.pct.auth.config.JwtUser;
import com.pct.auth.dto.ResetPasswordBean;
import com.pct.auth.dto.UserOrganizationDetails;
import com.pct.common.dto.UserDTO;
import com.pct.common.dto.UserResponse;
import com.pct.common.util.Context;

public interface IUserService {

	public UserDTO createUser(UserDTO userDto, Context context);

	public Boolean deleteUserById(Long id, Context context);

	public UserDTO updateUser(UserDTO userDto, Context context);

	public UserDTO updateExistingUser(UserDTO userDto, Context context);

	public UserDTO forgotPassword(String email, Context context);

	public boolean resetPassword(ResetPasswordBean resetPasswordBean, JwtUser jwtUser);

	public List<UserDTO> getAllUsers(Context context);

	public UserDTO getUserById(Long id, Context context);

	public UserDTO findByUserName(String userName, Context context);
	
	public UserOrganizationDetails findByEmail(String email, Context context);

	public Boolean existsByEmail(String email, Context context);

	public Boolean existsByMobileNumber(String mobileNumber, Context context);

	public Boolean existsByUserName(String userName, Context context);

	public List<UserDTO> getAllUsersByRole(Long roleId, Context context);

	public Page<UserResponse> getUserListWithPagination(Map<String, String> filterValues, String filterModelCountFilter,
			Pageable pageable, Context context, String userName, String sort);	

	public void insertLastLoginDate(String username,String dt);
	
	public void updateLandingPage(String username,String landingPage);
	
	public Boolean isUserAssociateWithLocation(Long locationId);

}