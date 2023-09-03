package com.pct.auth.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.pct.auth.repository.UserRepository;
import com.pct.common.dto.RoleDTO;
import com.pct.common.dto.UserDTO;
import com.pct.common.model.Organisation;
import com.pct.common.model.Role;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	ObjectMapper mapper;

	@Mock
	UserRepository userRepository;

	@Mock
	PasswordEncoder passwordEncoder;

	final static MediaType APPLICATION_JSON_UTF8 = new MediaType(MediaType.APPLICATION_JSON.getType(),
			MediaType.APPLICATION_JSON.getSubtype(), Charset.forName("utf8"));
	final static String APPLICATION_JSON = "application/json;charset=UTF-8";

	final static String jwtToken = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhYmhpc2hla0BsZWFmbm9kZS5pbyIsInJvbGVJZCI6IjEiLCJqd3RVc2VyIjp7InJvbGVJZCI6IjEiLCJyb2xlTmFtZSI6IlN1cGVyYWRtaW4iLCJ1c2VybmFtZSI6ImFiaGlzaGVrQGxlYWZub2RlLmlvIiwidXNlcklkIjoyLCJwYXNzd29yZCI6IiQyYSQxMCRxLm9VaENKNWhqUGFlci5QNUFLLmZPQnFIUHFjVmhZMmNIOW5mT3RjL1lqYmVxY1JxbnluZSIsImVuYWJsZWQiOnRydWUsImNyZWRlbnRpYWxzTm9uRXhwaXJlZCI6dHJ1ZSwiYWNjb3VudE5vbkV4cGlyZWQiOnRydWUsImFjY291bnROb25Mb2NrZWQiOnRydWUsImF1dGhvcml0aWVzIjpudWxsfSwicm9sZU5hbWUiOiJTdXBlcmFkbWluIiwiaWF0IjoxNjQ1NjYzMDU2LCJleHAiOjE2NDU2ODEwNTZ9.l2MEMWJDx47nnO2zeZcyChLBm3U1fiTLYdmS4_Bs_ak";

	@WithMockUser(username = "abhishek@leafnode.io", password = "passme", roles = "Org Admin")
	@Order(1)
	@Test
	public void test_saveUser1() throws Exception {
		UserDTO dto = getUser();
		dto.setEmail("soniya12@gmail.com");
		ObjectMapper Obj = new ObjectMapper();
		Obj.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		String json = Obj.writeValueAsString(dto);
		MvcResult result = this.mockMvc.perform(post("/user").contentType(APPLICATION_JSON_UTF8).content(json)
				.header("Authorization", "Bearer " + jwtToken)).andReturn();
		assertEquals(409, result.getResponse().getStatus());
	}

	@WithMockUser(username = "abhishek@leafnode.io", password = "passme", roles = "Org Admin")
	@Order(3)
	@Test

	public void test_saveUser3() throws Exception {
		UserDTO dto = getUser();
		dto.setEmail("soniya123@gmail.com");
		dto.setUserName("soniya12@gmail.com");
		dto.setPhone("00000000000000");
		ObjectMapper Obj = new ObjectMapper();
		Obj.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		String json = Obj.writeValueAsString(dto);
		MvcResult result = this.mockMvc.perform(post("/user").contentType(APPLICATION_JSON_UTF8).content(json)
				.header("Authorization", "Bearer " + jwtToken)).andReturn();
		assertEquals(409, result.getResponse().getStatus());
	}

	@WithMockUser(username = "abhishek@leafnode.io", password = "passme", roles = "Org Admin")
	@Order(2)
	@Test

	public void test_saveUser2() throws Exception {
		UserDTO dto = getUser();
		dto.setEmail("soniya123@gmail.com");
		dto.setUserName("soniya123@gmail.com");
		dto.setPhone("3111111111");
		ObjectMapper Obj = new ObjectMapper();
		Obj.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		String json = Obj.writeValueAsString(dto);
		MvcResult result = this.mockMvc.perform(post("/user").contentType(APPLICATION_JSON_UTF8).content(json)
				.header("Authorization", "Bearer " + jwtToken)).andReturn();
		assertEquals(409, result.getResponse().getStatus());
	}

	@WithMockUser(username = "abhishek@leafnode.io", password = "passme", roles = "Org Admin")
	@Order(4)
	@Test

	public void test_saveUser4() throws Exception {
		UserDTO dto = getUser();
		dto.setOrganisation(null);
		ObjectMapper Obj = new ObjectMapper();
		Obj.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		String json = Obj.writeValueAsString(dto);
		MvcResult result = this.mockMvc.perform(post("/user").contentType(APPLICATION_JSON_UTF8).content(json)
				.header("Authorization", "Bearer " + jwtToken)).andReturn();
		assertEquals(500, result.getResponse().getStatus());
	}

	@WithMockUser(username = "abhishek@leafnode.io", password = "passme", roles = "Org Admin")
	@Order(5)
	@Test

	public void test_saveUser5() throws Exception {
		UserDTO dto = getUser();
		ObjectMapper Obj = new ObjectMapper();
		Obj.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		String json = Obj.writeValueAsString(dto);
		MvcResult result = this.mockMvc.perform(post("/user").contentType(APPLICATION_JSON_UTF8).content(json)
				.header("Authorization", "Bearer " + jwtToken)).andReturn();
		assertEquals(201, result.getResponse().getStatus());
	}

	@WithMockUser(username = "abhishek@leafnode.io", password = "passme")
	@Order(6)
	@Test

	public void test_getAllUsers() throws Exception {
		MvcResult result = mockMvc
				.perform(get("/user/all").accept(APPLICATION_JSON).header("Authorization", "Bearer " + jwtToken))
				.andReturn();
		assertEquals(200, result.getResponse().getStatus());
	}

	@WithMockUser(username = "abhishek@leafnode.io", password = "passme")
	@Order(7)
	@Test

	public void test_getUser1() throws Exception {
		MvcResult result = mockMvc
				.perform(get("/user/get/21").accept(APPLICATION_JSON).header("Authorization", "Bearer " + jwtToken))
				.andReturn();
		assertEquals(200, result.getResponse().getStatus());
	}

	@WithMockUser(username = "abhishek@leafnode.io", password = "passme")
	@Order(8)
	@Test
	public void test_getUser2() throws Exception {
		MvcResult result = mockMvc
				.perform(get("/user/get/77").accept(APPLICATION_JSON).header("Authorization", "Bearer " + jwtToken))
				.andReturn();
		assertEquals(400, result.getResponse().getStatus());
	}

	@WithMockUser(username = "abhishek@leafnode.io", password = "passme")
	@Order(9)
	@Test

	public void test_deleteUser1() throws Exception {
		MvcResult result = mockMvc
				.perform(delete("/user/18").accept(APPLICATION_JSON).header("Authorization", "Bearer " + jwtToken))
				.andReturn();
		assertEquals(200, result.getResponse().getStatus());
	}

	@WithMockUser(username = "abhishek@leafnode.io", password = "passme")
	@Order(10)
	@Test

	public void test_deleteUser2() throws Exception {
		MvcResult result = mockMvc
				.perform(delete("/user/50").accept(APPLICATION_JSON).header("Authorization", "Bearer " + jwtToken))
				.andReturn();
		assertEquals(500, result.getResponse().getStatus());
	}

	@WithMockUser(username = "abhishek@leafnode.io", password = "passme")
	@Order(11)
	@Test

	public void test_allUsersByRole1() throws Exception {
		MvcResult result = mockMvc
				.perform(
						get("/user/adminUser/1").accept(APPLICATION_JSON).header("Authorization", "Bearer " + jwtToken))
				.andReturn();
		assertEquals(200, result.getResponse().getStatus());
	}

	@WithMockUser(username = "abhishek@leafnode.io", password = "passme")
	@Order(12)
	@Test

	public void test_allUsersByRole2() throws Exception {
		MvcResult result = mockMvc.perform(
				get("/user/adminUser/108").accept(APPLICATION_JSON).header("Authorization", "Bearer " + jwtToken))
				.andReturn();
		assertEquals(400, result.getResponse().getStatus());
	}

	@WithMockUser(username = "abhishek@leafnode.io", password = "passme")
	@Order(13)
	@Test

	public void test_getUserByIDForAnotherService1() throws Exception {
		MvcResult result = mockMvc.perform(get("/user/username?user_name=soniya12@gmail.com").accept(APPLICATION_JSON)
				.header("Authorization", "Bearer " + jwtToken)).andReturn();
		assertEquals(200, result.getResponse().getStatus());
	}

	@WithMockUser(username = "abhishek@leafnode.io", password = "passme")
	@Order(14)
	@Test

	public void test_getUserByIDForAnotherService2() throws Exception {
		MvcResult result = mockMvc.perform(get("/user/username?user_name=xyz@leafnode.io").accept(APPLICATION_JSON)
				.header("Authorization", "Bearer " + jwtToken)).andReturn();
		assertEquals(500, result.getResponse().getStatus());
	}

	@WithMockUser(username = "abhishek@leafnode.io", password = "passme")
	@Order(15)
	@Test

	public void test_checkUserNameAvailability1() throws Exception {
		MvcResult result = mockMvc.perform(post("/user/username-availability/soniya12@gmail.com")
				.accept(APPLICATION_JSON).header("Authorization", "Bearer " + jwtToken)).andReturn();
		assertEquals(200, result.getResponse().getStatus());
	}

	@WithMockUser(username = "abhishek@leafnode.io", password = "passme")
	@Order(16)
	@Test

	public void test_checkUserNameAvailability2() throws Exception {
		MvcResult result = mockMvc.perform(post("/user/username-availability/ritu@leafnode.io").accept(APPLICATION_JSON)
				.header("Authorization", "Bearer " + jwtToken)).andReturn();
		assertEquals(500, result.getResponse().getStatus());
	}

	@WithMockUser(username = "abhishek@leafnode.io", password = "passme")
	@Order(17)
	@Test
	public void test_resetPassword1() throws Exception {
		MvcResult result = mockMvc.perform(post("/user/reset-password/admin/soniya12@gmail.com")
				.accept(APPLICATION_JSON).header("Authorization", "Bearer " + jwtToken)).andReturn();
		assertEquals(200, result.getResponse().getStatus());
	}

	@WithMockUser(username = "abhishek@leafnode.io", password = "passme")
	@Order(18)
	@Test
	public void test_resetPassword2() throws Exception {
		MvcResult result = mockMvc.perform(post("/user/reset-password/admin/soniya128@gmail.com")
				.accept(APPLICATION_JSON).header("Authorization", "Bearer " + jwtToken)).andReturn();
		assertEquals(500, result.getResponse().getStatus());
	}

	@WithMockUser(username = "abhishek@leafnode.io", password = "passme")
	@Order(19)
	@Test

	public void test_updateUser1() throws Exception {
		UserDTO dto = getUser();
		dto.setId(21L);
		ObjectMapper Obj = new ObjectMapper();
		Obj.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		String json = Obj.writeValueAsString(dto);
		MvcResult result = this.mockMvc.perform(put("/user").contentType(APPLICATION_JSON_UTF8).content(json)
				.header("Authorization", "Bearer " + jwtToken)).andReturn();
		assertEquals(200, result.getResponse().getStatus());
	}

	@WithMockUser(username = "abhishek@leafnode.io", password = "passme")
	@Order(20)
	@Test
	public void test_updateUser2() throws Exception {
		UserDTO dto = getUser();
		ObjectMapper Obj = new ObjectMapper();
		Obj.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		String json = Obj.writeValueAsString(dto);
		MvcResult result = this.mockMvc.perform(put("/user").contentType(APPLICATION_JSON_UTF8).content(json)
				.header("Authorization", "Bearer " + jwtToken)).andReturn();
		assertEquals(500, result.getResponse().getStatus());
	}

	@WithMockUser(username = "abhishek@leafnode.io", password = "passme")
	@Order(21)
	@Test
	public void test_loadUserByUsername1() throws Exception {
		UserDTO dto = getUser();
		ObjectMapper Obj = new ObjectMapper();
		Obj.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		String json = Obj.writeValueAsString(dto);
		MvcResult result = this.mockMvc.perform(get("/user/loadUser/akshit123@leafnode.io").accept(APPLICATION_JSON)
				.header("Authorization", "Bearer " + jwtToken)).andReturn();
		assertEquals(200, result.getResponse().getStatus());
	}

	@WithMockUser(username = "abhishek@leafnode.io", password = "passme")
	@Order(22)
	@Test
	public void test_loadUserByUsername2() throws Exception {
		UserDTO dto = getUser();
		ObjectMapper Obj = new ObjectMapper();
		Obj.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		String json = Obj.writeValueAsString(dto);
		MvcResult result = this.mockMvc.perform(get("/user/loadUser/soniya1223@gmail.com").accept(APPLICATION_JSON)
				.header("Authorization", "Bearer " + jwtToken)).andReturn();
		assertEquals(400, result.getResponse().getStatus());
	}

	@WithMockUser(username = "abhishek@leafnode.io", password = "passme")
	@Order(23)
	@Test
	public void test_loadUserByUsername3() throws Exception {
		UserDTO dto = getUser();
		ObjectMapper Obj = new ObjectMapper();
		Obj.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		String json = Obj.writeValueAsString(dto);
		MvcResult result = this.mockMvc
				.perform(get("/user/loadUser").accept(APPLICATION_JSON).header("Authorization", "Bearer " + jwtToken))
				.andReturn();
		assertEquals(500, result.getResponse().getStatus());
	}

	public UserDTO getUser() {
		UserDTO u1 = new UserDTO();
		// u1.setId(15L);
		u1.setCountryCode("+91");
		u1.setEmail("khushi@gmail.com");
		u1.setFirstName("khushi");
		u1.setLastName("Rai");
		u1.setIsActive(true);
		u1.setIsDeleted(true);
		u1.setIsPasswordChange(true);
		u1.setNotify("email");
		Organisation org = new Organisation();
		org.setAccountNumber("123456");
		org.setId(1L);
		u1.setPassword("khushi@123");
		u1.setPhone("8111111111");
		u1.setOrganisation(org);
		RoleDTO r = new RoleDTO();
		r.setRoleId(1L);
		List<RoleDTO> roleList= new ArrayList<>();
		roleList.add(r);
		u1.setRole(roleList);
		u1.setTimeZone("abc");
		u1.setUserName("khushi@gmail.com");
		return u1;
	}
}