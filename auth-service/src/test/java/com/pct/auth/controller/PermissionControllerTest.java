//package com.pct.auth.controller;
//
//import static org.mockito.Mockito.when;
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
//import static org.hamcrest.CoreMatchers.is;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
//import org.springframework.test.web.servlet.setup.MockMvcBuilders;
//import org.springframework.web.context.WebApplicationContext;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.pct.auth.controllers.PermissionController;
//import com.pct.auth.dto.MethodType;
//import com.pct.auth.dto.PermissionDto;
//import com.pct.auth.dto.Status;
//import com.pct.auth.response.BaseResponse;
//import com.pct.auth.service.IPermissionService;
//
//@RunWith(SpringRunner.class)
//@ExtendWith(SpringExtension.class)
//@ContextConfiguration(classes=PermissionController.class)
//@WebAppConfiguration
//@WebMvcTest
//public class PermissionControllerTest {
//
//	@Autowired
//	private WebApplicationContext wac;
//
//	private MockMvc mockMvc;
//
//	@MockBean
//	private IPermissionService permissionService;
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
//	public void addPermissionTest() throws Exception{
//		PermissionDto request = new PermissionDto();
//		request.setName("create");
//		request.setDescription("It is to add permission");
//		request.setMethodType(MethodType.POST);
//		request.setPath("/addPermission");
//		String json = mapper.writeValueAsString(request);
//
//		BaseResponse<Boolean, Integer> response = new BaseResponse<>();
//		response.setMessage("Permission is created successfully.");
//		response.setStatus(Status.SUCCESS);
//		
//		when(permissionService.addPermission(request))
//		.thenReturn(Boolean.TRUE);
//
//		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
//		this.mockMvc.perform(post("/api/addPermission")
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
//	public void updatePermissionTest() throws Exception{
//		PermissionDto request = new PermissionDto();
//		request.setName("update");
//		request.setDescription("It is to update permission");
//		request.setMethodType(MethodType.PUT);
//		request.setPath("/updatePermission");
//		String json = mapper.writeValueAsString(request);
//
//		BaseResponse<Boolean, Integer> response = new BaseResponse<>();
//		response.setMessage("Permission with id: [1] updated successfully.");
//		response.setStatus(Status.SUCCESS);
//		
//		when(permissionService.updatePermission(1,request))
//		.thenReturn(Boolean.TRUE);
//
//		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
//		this.mockMvc.perform(put("/api/updatePermission/{permissionId}", 1)
//				.contentType(MediaType.APPLICATION_JSON_VALUE)
//				.content(json))
//		.andDo(print())
//		.andExpect(status().isOk()) 
//		.andExpect(MockMvcResultMatchers.content().string(this.mapper.writeValueAsString(response)))
//		.andExpect(jsonPath("$.status", is("SUCCESS")))
//		.andReturn();
//
//	}
//	@Test
//	public void deletePermissionTest() throws Exception{
//
//		BaseResponse<Boolean, Integer> response = new BaseResponse<>();
//		response.setMessage("Permission with id: [1] deleted successfully.");
//		response.setStatus(Status.SUCCESS);
//		
//		when(permissionService.deletePermission(1))
//		.thenReturn(Boolean.TRUE);
//
//		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
//		this.mockMvc.perform(delete("/api/deletePermission/{permissionId}", 1))
//		.andDo(print())
//		.andExpect(status().isOk()) 
//		.andExpect(MockMvcResultMatchers.content().string(this.mapper.writeValueAsString(response)))
//		.andExpect(jsonPath("$.status", is("SUCCESS")))
//		.andReturn();
//
//	}
//	
//}
