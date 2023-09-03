package com.pct.auth.service.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.Assert;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.pct.auth.repository.RoleRepository;
import com.pct.auth.repository.UserRepository;
import com.pct.auth.util.MailUtil;
import com.pct.auth.util.UserConverter;
import com.pct.common.dto.RoleDTO;
import com.pct.common.dto.UserDTO;
import com.pct.common.model.Organisation;
import com.pct.common.model.Role;
import com.pct.common.model.User;
import com.pct.common.util.Context;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private UserServiceImpl service;

	@Mock
	private RoleRepository roleRepository;

	@Mock
	UserConverter userConverter;

	@Mock
	private PasswordEncoder passwordEncoder;

	Context context = new Context();
	@Mock
	private MailUtil mailUtil;

	@Test
	@Order(1)
	public void test_createUser() {
		User user = getAllUsersList().get(0);
		Role role = user.getRole().get(0);
		UserDTO dto = userToUserDTO(user);
		dto.setPassword("abcdc");
		Mockito.when(userConverter.userDtoToUser(dto)).thenReturn(user);
		Mockito.when(passwordEncoder.encode(dto.getPassword())).thenReturn(dto.getPassword());
		Mockito.when(roleRepository.findByRoleId(role.getRoleId())).thenReturn(role);
		Mockito.when(userRepository.save(user)).thenReturn(user);
		Mockito.when(userConverter.userToUserDTO(user)).thenReturn(dto);
		dto = service.createUser(dto, context);
		Assert.assertEquals(user.getFirstName(), dto.getFirstName());
	}

	@Test
	@Order(2)
	public void test_updateUser() {
		User user = getAllUsersList().get(0);
		Mockito.when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
		Mockito.when(userRepository.save(user)).thenReturn(user);
		Role role = user.getRole().get(0);
		Mockito.when(roleRepository.findByRoleId(user.getId())).thenReturn(role);
		Mockito.when(userRepository.save(user)).thenReturn(user);
		UserDTO dto = userToUserDTO(user);
		Mockito.when(userConverter.userToUserDTO(user)).thenReturn(dto);
		dto = service.updateUser(userToUserDTO(user), context);
		Assert.assertEquals(user.getFirstName(), dto.getFirstName());
	}

	@Test
	@Order(3)
	public void test_forgotPassword() {
		User user = getAllUsersList().get(0);
		Mockito.when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
		Mockito.when(userRepository.saveAndFlush(user)).thenReturn(user);
		UserDTO dto = userToUserDTO(user);
		Mockito.when(userConverter.userToUserDTO(user)).thenReturn(dto);
		dto = service.forgotPassword(user.getEmail(), context);
		Assert.assertEquals(user.getFirstName(), dto.getFirstName());
	}

	List<UserDTO> list = new ArrayList<UserDTO>();

	@Test
	@Order(4)
	public void test_getAllUsers() {
		List<User> al = getAllUsersList();
		Mockito.when(userRepository.findAll()).thenReturn(al);
//		UserDTO dto = userToUserDTO(al.get(0));
		Mockito.when(userConverter.userToUserDTO(al.get(0))).thenReturn(userToUserDTO(al.get(0)));

		al.forEach(u -> {
			UserDTO dto = userToUserDTO(u);
			Mockito.when(userConverter.userToUserDTO(u)).thenReturn(dto);
			if (u.getOrganisation() != null) {
				dto.setOrganisation(u.getOrganisation());
				list.add(dto);
			}
		});
		list = service.getAllUsers(context);
		Assert.assertEquals(list.size(), al.size());
	}

	@Test
	@Order(5)
	public void test_getUserById() {
		User user = getAllUsersList().get(0);
		Mockito.when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
		UserDTO dto = userToUserDTO(user);
		Mockito.when(userConverter.userToUserDTO(user)).thenReturn(dto);
		dto = service.getUserById(user.getId(), context);
		Assert.assertEquals(user.getFirstName(), dto.getFirstName());
	}

	@Test
	@Order(6)
	public void test_findByUserName() {
		User user = getAllUsersList().get(0);
		Mockito.when(userRepository.findByUserName("akshit@gmail.com")).thenReturn(Optional.of(user));
		UserDTO dto = userToUserDTO(user);
		Mockito.when(userConverter.userToUserDTO(user)).thenReturn(dto);
		dto = service.findByUserName(user.getUserName(), context);
		Assert.assertEquals(user.getFirstName(), dto.getFirstName());
	}

	@Test
	@Order(7)
	public void test_existsByEmail() {
		User user = getAllUsersList().get(0);
		Mockito.when(userRepository.existsByEmail(user.getEmail())).thenReturn(true);
		Assert.assertEquals(true, service.existsByEmail(user.getEmail(), context));
	}

	@Test
	@Order(8)
	public void test_existsByMobileNumber() {
		User user = getAllUsersList().get(0);
		Mockito.when(userRepository.existsByPhone(user.getPhone())).thenReturn(true);
		Assert.assertEquals(true, service.existsByMobileNumber(user.getPhone(), context));
	}

	@Test
	@Order(9)
	public void test_existsByUserName() {
		User user = getAllUsersList().get(0);
		Mockito.when(userRepository.existsByUserName(user.getUserName())).thenReturn(true);
		Assert.assertEquals(true, service.existsByUserName(user.getUserName(), context));
	}

	@Test
	@Order(10)
	public void test_getAllUsersByRole() {
		List<User> al = getAllUsersList();
		Role role = al.get(0).getRole().get(0);
		Mockito.when(roleRepository.findByRoleId(role.getRoleId())).thenReturn(role);
		Mockito.when(userRepository.findByRole(role)).thenReturn(al);
		List<UserDTO> list = service.getAllUsersByRole(al.get(0).getRole().get(0).getRoleId(), context);
		Assert.assertEquals(list.size(), al.size());
	}

	@Test
	@Order(11)
	public void test_deleteUserById() {
		User user = getAllUsersList().get(0);
		Mockito.when(userRepository.getById(user.getId())).thenReturn(user);
		Mockito.doNothing().when(userRepository).deleteById(any());
		service.deleteUserById(user.getId(), context);
		verify(userRepository).deleteById(any());
	}

	@Test
	@Order(12)
	public void test_loadUserByUsername() {
		User user = getAllUsersList().get(0);
		Mockito.when(userRepository.findByUserName(user.getUserName())).thenReturn(Optional.of(user));
		service.loadUserByUsername(user.getUserName());
		verify(userRepository).findByUserName(user.getUserName());
	}

	private UserDTO userToUserDTO(User user) {
		UserDTO dto = new UserDTO();
		dto.setId(user.getId());
		dto.setEmail(user.getEmail());
		dto.setFirstName(user.getFirstName());
		dto.setOrganisation(user.getOrganisation());
		dto.setCountryCode(user.getCountryCode());
		dto.setNotify(user.getNotify());
		dto.setIsActive(user.getIsActive());
		dto.setIsDeleted(user.getIsDeleted());
		dto.setLastName(user.getLastName());
		dto.setPhone(user.getPhone());
		dto.setUserName(user.getUserName());
		dto.setTimeZone(user.getTimeZone());
		dto.setId(user.getId());
		List<RoleDTO> roleDtoList = new ArrayList<>();
		for (Role roles : user.getRole()) {
			RoleDTO roleDto = new RoleDTO();
			roleDto.setId(roles.getId());
			roleDto.setName(roles.getName());
			roleDto.setRoleId(roles.getRoleId());
			roleDto.setDescription(roles.getDescription());
			roleDtoList.add(roleDto);
		}
		dto.setRole(roleDtoList);
		dto.setPassword(user.getPassword());
		dto.setIsPasswordChange(user.getIsPasswordChange());
		return dto;
	}

	private List<User> getAllUsersList() {

		User u1 = new User();
		u1.setId(1L);
		u1.setCountryCode("+91");
		u1.setCreatedAt(Instant.now());
		u1.setCreatedBy("Raj");
		u1.setEmail("akshit@gmail.com");
		u1.setFirstName("Akshit");
		u1.setLastName("Rai");
		u1.setIsActive(true);
		u1.setIsDeleted(true);
		u1.setIsPasswordChange(true);
		u1.setNotify("email");

		Organisation org = new Organisation();
		org.setAccountNumber("123456");
		org.setId(1L);
		u1.setOrganisation(org);
		u1.setPassword("akshit@123");
		u1.setPhone("9999999999");
		Role r = new Role();
		r.setRoleId(1L);
		List<Role> roleList = new ArrayList<>();
		roleList.add(r);
		u1.setRole(roleList);
		// u1.setTimeZone("2022-02-24T03:19:05.720Z");
		u1.setUpdatedAt(Instant.now());
		u1.setUpdatedBy("Abc");
		u1.setUserName("akshit@gmail.com");
		u1.setUuid("0d2f1041-b9f7-4869-8bf5-107781fb23da");
		User u2 = new User();
		u2.setId(2L);
		u2.setCountryCode("+91");
		u2.setCreatedAt(Instant.now());
		u2.setCreatedBy("Raj");
		u2.setEmail("aditi@gmail.com");
		u2.setFirstName("Aditi");
		u2.setLastName("Rai");
		u2.setIsActive(true);
		u2.setIsDeleted(true);
		u2.setIsPasswordChange(true);
		u2.setNotify("email");
		org = new Organisation();
		org.setAccountNumber("223456");
		org.setId(1L);
		u2.setOrganisation(org);
		u2.setPassword("aditi@123");
		u2.setPhone("888888888");
		r = new Role();
		r.setRoleId(1L);
		List<Role> roleList2 = new ArrayList<>();
		roleList2.add(r);
		u2.setRole(roleList2);
		u2.setTimeZone("abc");
		u2.setUpdatedAt(Instant.now());
		u2.setUpdatedBy("Abc");
		u2.setUserName("aditi@gmail.com");
		u2.setUuid("0d2f1041-b9f7-4869-8bf5-107781fb23da");
		List<User> al = new ArrayList<User>();
		al.add(u1);
		al.add(u2);
		return al;
	}
}