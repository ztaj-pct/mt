//package com.pct.auth.controller;
//
//import static org.hamcrest.CoreMatchers.is;
//import static org.mockito.Mockito.when;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
//import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//import org.junit.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.http.MediaType;
//import org.springframework.test.context.ContextConfiguration;
//import org.springframework.test.context.event.annotation.BeforeTestMethod;
//import org.springframework.test.context.junit.jupiter.SpringExtension;
//import org.springframework.test.context.junit4.SpringRunner;
//import org.springframework.test.context.web.WebAppConfiguration;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
//import org.springframework.test.web.servlet.setup.MockMvcBuilders;
//import org.springframework.web.context.WebApplicationContext;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.pct.auth.controllers.RoleController;
//import com.pct.auth.dto.RoleDto;
//import com.pct.auth.dto.Status;
//import com.pct.auth.response.BaseResponse;
//import com.pct.auth.service.IRoleService;
//
//@RunWith(SpringRunner.class)
//@ExtendWith(SpringExtension.class)
//@ContextConfiguration(classes=RoleController.class)
//@WebAppConfiguration
//@WebMvcTest
//public class RoleControllerTest {
//	
//	@Autowired
//	private WebApplicationContext wac;
//
//	private MockMvc mockMvc;
//
//	@MockBean
//	private IRoleService roleService;
//
//	@Autowired
//	private ObjectMapper mapper;
//
//	@BeforeTestMethod
//	public void setup() {
//		mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
//	} 
//
//	@Test
//	public void addRoleTest() throws Exception{
//		RoleDto request = new RoleDto();
//		request.setName("admin");
//		request.setDescription("add role");
//		String json = mapper.writeValueAsString(request);
//
//		BaseResponse<Boolean, Integer> response = new BaseResponse<>();
//		response.setMessage("Role is created successfully.");
//		response.setStatus(Status.SUCCESS);
//		
//		when(roleService.addRole(request))
//		.thenReturn(Boolean.TRUE);
//
//		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
//		this.mockMvc.perform(post("/api/addRole")
//				.contentType(MediaType.APPLICATION_JSON_VALUE)
//				.content(json))
//		.andDo(print())
//		.andExpect(status().isOk()) 
//		.andExpect(MockMvcResultMatchers.content().string(this.mapper.writeValueAsString(response)))
//		.andExpect(jsonPath("$.status", is("SUCCESS")))
//		.andReturn();
//
//	}
//	
//	@Test
//	public void updateRoleTest() throws Exception{
//		RoleDto request = new RoleDto();
//		request.setName("update");
//		request.setDescription("update role");
//		
//		String json = mapper.writeValueAsString(request);
//
//		BaseResponse<Boolean, Integer> response = new BaseResponse<>();
//		response.setMessage("Role with id: [1] updated successfully.");
//		response.setStatus(Status.SUCCESS);
//		
//		when(roleService.updateRole(1,request))
//		.thenReturn(Boolean.TRUE);
//
//		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
//		this.mockMvc.perform(put("/api/updateRole/{roleId}", 1)
//				.contentType(MediaType.APPLICATION_JSON_VALUE)
//				.content(json))
//		.andDo(print())
//		.andExpect(status().isOk()) 
//		.andExpect(MockMvcResultMatchers.content().string(this.mapper.writeValueAsString(response)))
//		.andExpect(jsonPath("$.status", is("SUCCESS")))
//		.andReturn();
//
//	}
//	
//	@Test
//	public void deleteRoleTest() throws Exception{
//
//		BaseResponse<Boolean, Integer> response = new BaseResponse<>();
//		response.setMessage("Role with id: [1] deleted successfully.");
//		response.setStatus(Status.SUCCESS);
//		
//		when(roleService.deleteRole(1))
//		.thenReturn(Boolean.TRUE);
//
//		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
//		this.mockMvc.perform(delete("/api/deleteRole/{roleId}", 1))
//		.andDo(print())
//		.andExpect(status().isOk()) 
//		.andExpect(MockMvcResultMatchers.content().string(this.mapper.writeValueAsString(response)))
//		.andExpect(jsonPath("$.status", is("SUCCESS")))
//		.andReturn();
//
//	}
//
//}
