package com.pct.installer.feign.clients;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.pct.common.dto.OrganisationDtoForInventory;
import com.pct.common.dto.ResponseBodyDTO;
import com.pct.common.model.Organisation;
import com.pct.installer.feign.config.DeviceServiceFeignConfig;

@FeignClient(name = "organisation-service", configuration = DeviceServiceFeignConfig.class)
public interface OrganisationServiceFeignClient {


	@GetMapping("/customer/core")
	public ResponseEntity<Organisation> getCompanyForAccountNumber(@RequestParam(value = "logUUid", required = false) String logUUid, @RequestParam("accountNumber") String accountNumber);

	 @GetMapping("/customer/core/customer/company")
	 public ResponseEntity<List<Organisation>> getAllCustomerCompany(@RequestParam(value = "logUUid", required = false) String logUUid);   
	 
	 @GetMapping("/customer/core/customer/company/dto")
	 public ResponseEntity<List<OrganisationDtoForInventory>> getAllCustomerCompanyDto(@RequestParam(value = "logUUid", required = false) String logUUid);   
		
	// -------------------------------------Aamir 1 start ---------------------------------//
	
	@GetMapping("/customer/core/uuid/{uuid}")
	public ResponseEntity<Organisation> getCompanyForUuid(@PathVariable("uuid") String uuid);

	@GetMapping("/customer/organisation")
	public ResponseEntity<ResponseBodyDTO<List<Organisation>>> getAllCustomerCompany();
	
	// -------------------------------------Aamir 1 end ---------------------------------//
	@GetMapping("/customer/core/type/{type}")
	public ResponseEntity<List<Organisation>>  getListOfOrganisationForType(@RequestParam(value = "logUUid", required = false) String logUUid, @RequestParam(value = "type", required = false) String type);
}
