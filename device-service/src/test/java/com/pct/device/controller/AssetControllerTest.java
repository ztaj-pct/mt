package com.pct.device.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.nio.charset.Charset;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.pct.device.payload.AddAssetResponse;
import com.pct.device.payload.AssetsPayload;
import com.pct.device.payload.CompanyPayload;

@SpringBootTest
@AutoConfigureMockMvc
class AssetControllerTest {
	@Autowired
	MockMvc mockMvc;
	@Autowired
	private ObjectMapper mapper;

	final static MediaType APPLICATION_JSON_UTF8 = new MediaType(MediaType.APPLICATION_JSON.getType(),
			MediaType.APPLICATION_JSON.getSubtype(), Charset.forName("utf8"));
	final static String APPLICATION_JSON = "application/json;charset=UTF-8";

	final static String jwtToken = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhYmhpc2hla0BsZWFmbm9kZS5pbyIsInJvbGVJZCI6IjEiLCJqd3RVc2VyIjp7InJvbGVJZCI6IjEiLCJyb2xlTmFtZSI6IlN1cGVyYWRtaW4iLCJ1c2VybmFtZSI6ImFiaGlzaGVrQGxlYWZub2RlLmlvIiwidXNlcklkIjoyLCJwYXNzd29yZCI6IiQyYSQxMCRxLm9VaENKNWhqUGFlci5QNUFLLmZPQnFIUHFjVmhZMmNIOW5mT3RjL1lqYmVxY1JxbnluZSIsImVuYWJsZWQiOnRydWUsImF1dGhvcml0aWVzIjpudWxsLCJjcmVkZW50aWFsc05vbkV4cGlyZWQiOnRydWUsImFjY291bnROb25Mb2NrZWQiOnRydWUsImFjY291bnROb25FeHBpcmVkIjp0cnVlfSwicm9sZU5hbWUiOiJTdXBlcmFkbWluIiwiaWF0IjoxNjQ2MjE3OTY1LCJleHAiOjE2NDYyMzU5NjV9.xUZ5O2UbmOVjptWDhJWB3z2HiQaKEu-lXM0ERhSiaaw";

	@WithMockUser(username = "abhishek@leafnode.io", password = "passme", roles = "Phillips Connect Admin")
	@Test
	public void addAsset() throws JsonProcessingException, Exception {
		Long userId = 57L;
		Long Id = 44L;
		String manufacturer = "manufacturer";
		CompanyPayload companyPayload = new CompanyPayload();
		companyPayload.setCompanyName("name");
		companyPayload.setAccountNumber("5346544");
		companyPayload.setShortName("short_name");
		companyPayload.setStatus(true);
		companyPayload.setId(userId);
		companyPayload.setIsAssetListRequired(true);
		companyPayload.setUuid("84d6efc0-37ad-4cde-98c9-79611e2fab60");

		AssetsPayload assets = new AssetsPayload();
		assets.setAssignedName("assigned_name");
		assets.setCategory("category");
		assets.setUuid("84d6efc0-37ad-4cde-98c9-79611e2fab60");
		assets.setStatus("status");
		assets.setCompany(companyPayload);
		assets.setManufacturer(manufacturer);
		assets.setComment("comment");
		assets.setEligibleGateway("eligibleGateway");
		assets.setId(Id);
		assets.setIsVinValidated(false);
		assets.setVin("vin");
		assets.setYear("year");
		AddAssetResponse addAssetResponse = new AddAssetResponse();
		addAssetResponse.setAssetPayload(assets);

		mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		String json = mapper.writeValueAsString(assets);
		MvcResult mvcResult = this.mockMvc.perform(post("/asset").contentType(APPLICATION_JSON_UTF8).content(json)
				.header("Authorization", "Bearer " + jwtToken)).andReturn();

		System.out.println("mvcResult" + mvcResult);
		assertEquals(201, mvcResult.getResponse().getStatus());
	}
}