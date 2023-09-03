package com.pct.organisation.controller;

import com.pct.common.dto.OrganisationDtoForInventory;
import com.pct.common.dto.ResponseBodyDTO;
import com.pct.common.dto.ResponseDTO;
import com.pct.common.model.Organisation;
import com.pct.organisation.dto.CustomerWithLocation;
import com.pct.organisation.payload.CustomerLocationPayload;
import com.pct.organisation.service.ICustomerService;
import com.pct.organisation.service.IOrganisationService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StopWatch;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/customer")
public class CustomerController {

	Logger logger = LoggerFactory.getLogger(CustomerController.class);

	@Autowired
	private ICustomerService customerService;
	
	@Autowired
	private IOrganisationService organisationService;

    @GetMapping("/core")
    public ResponseEntity<Organisation> getCompanyForAccountNumber(@RequestParam(value = "logUuid", required = false) String logUuid, @RequestParam("accountNumber") String accountNumber) {
        try {
        	Organisation company = customerService.getCompanyByAccountNumber(accountNumber);
            return new ResponseEntity<>(company, HttpStatus.OK);
        } catch (Exception exception) {
            logger.error("Exception occurred while getting Company", exception);
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

	@GetMapping("/core/id/{id}")
	public ResponseEntity<Organisation> getCompanyForId(@PathVariable("id") Long id) {
		try {
			logger.info("Request to get organisation details by id");
			Organisation company = customerService.getCompanyById(id);
			return new ResponseEntity<>(company, HttpStatus.OK);
		} catch (Exception exception) {
			logger.error("Exception occurred while getting Company", exception);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/core/uuid/{uuid}")
	public ResponseEntity<Organisation> getCompanyForUuid(@PathVariable("uuid") String uuid) {
		try {
			logger.info("Request to get organisation details by uuid");
			Organisation company = customerService.getCompanyByUuid(uuid);
			return new ResponseEntity<>(company, HttpStatus.OK);
		} catch (Exception exception) {
			logger.error("Exception occurred while getting Company", exception);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/core/type/{type}")
	public ResponseEntity<List<String>> getCompanyForType(@RequestParam(value = "logUUid", required = false) String logUUid, @PathVariable("type") String type) {
		try {
			logger.info("Request to get organisation details by type");
			List<String> companies = customerService.getCompanyByType(type);
			return new ResponseEntity<>(companies, HttpStatus.OK);
		} catch (Exception exception) {
			logger.error("Exception occurred while getting Company", exception);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@GetMapping("/type")
	public ResponseEntity<List<Organisation>> getListOfOrganisationForType(@RequestParam(value = "logUuid", required = false) String logUuid, @RequestParam("type") String type) {
		try {
			logger.info("Request to get organisation details by type");
			List<Organisation> companies = customerService.getListOfAllOrganisationByType(type);
			return new ResponseEntity<>(companies, HttpStatus.OK);
		} catch (Exception exception) {
			logger.error("Exception occurred while getting Company", exception);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@GetMapping("/type/{type}")
	public ResponseEntity<List<Organisation>> getListOfCompanyForType(@RequestParam(value = "logUuid", required = false) String logUuid, @PathVariable("type") String type,@RequestParam(value = "name",required=false) String name) {
		try {
			logger.info("Request to get organisation details by type");
			List<Organisation> companies = customerService.getListOfCompanyByType(type,name);
			return new ResponseEntity<>(companies, HttpStatus.OK);
		} catch (Exception exception) {
			logger.error("Exception occurred while getting Company", exception);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}


	@GetMapping(value = "/locations/{userUuid}")
	public ResponseEntity<ResponseBodyDTO<Map<String, List<CustomerLocationPayload>>>> getCustomerAndLocationByUser(
			@Validated @PathVariable("userUuid") String userUuid) {
		logger.info("Inside getCustomerAndLocationByUser Method");
		try {
			Map<String, List<CustomerLocationPayload>> customerList = customerService
					.getCustomerAndLocationByUser(userUuid);
			logger.info("Exiting from getCustomerAndLocationByUser Method");
			return new ResponseEntity<ResponseBodyDTO<Map<String, List<CustomerLocationPayload>>>>(
					new ResponseBodyDTO<Map<String, List<CustomerLocationPayload>>>(true,
							"Fetched customers successfully", customerList),
					HttpStatus.OK);
		} catch (Exception exception) {
			logger.error("Getting Exception in getCustomerAndLocationByUser", exception.getMessage());
			return new ResponseEntity<ResponseBodyDTO<Map<String, List<CustomerLocationPayload>>>>(
					new ResponseBodyDTO<Map<String, List<CustomerLocationPayload>>>(false, exception.getMessage()),
					HttpStatus.BAD_REQUEST);
		}
	}
	
	@GetMapping(value = "/access/{userName}")
	public ResponseEntity<ResponseBodyDTO<Map<String, List<CustomerLocationPayload>>>> getAccessListByUserName(
			@Validated @PathVariable("userName") String userName) {
		logger.info("Inside getAccessListByUserName Method");
		try {
			Map<String, List<CustomerLocationPayload>> customerList = customerService
					.getAccessListByUserName(userName);
			logger.info("Exiting from getAccessListByUserName Method");
			return new ResponseEntity<ResponseBodyDTO<Map<String, List<CustomerLocationPayload>>>>(
					new ResponseBodyDTO<Map<String, List<CustomerLocationPayload>>>(true,
							"Fetched customers successfully", customerList),
					HttpStatus.OK);
		} catch (Exception exception) {
			logger.error("Getting Exception in getAccessListByUserName", exception.getMessage());
			return new ResponseEntity<ResponseBodyDTO<Map<String, List<CustomerLocationPayload>>>>(
					new ResponseBodyDTO<Map<String, List<CustomerLocationPayload>>>(false, exception.getMessage()),
					HttpStatus.BAD_REQUEST);
		}
	}

	@PostMapping("/reset")
	public ResponseEntity<ResponseDTO> resetCustomerData(@RequestParam(name = "customer_uuid") String customerUuid) {
		logger.info("Inside resetCustomerData Method");
		try {
			Boolean status = customerService.resetCompanyData(customerUuid);
			logger.info("Exiting from resetCustomerData Method");
			return new ResponseEntity<ResponseDTO>(new ResponseDTO(status, "Successfully deleted customer data"),
					HttpStatus.OK);
		} catch (Exception exception) {
			logger.error("Getting Exception in resetCustomerData", exception);
			return new ResponseEntity<ResponseDTO>(new ResponseDTO(false, exception.getMessage()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	
	// ------------------------------Aamir 1 Start ---------------------------------//
	
	@GetMapping("/organisation")
	public ResponseEntity<ResponseBodyDTO<List<Organisation>>> getAllCustomerCompany() {
		try {
			List<Organisation> organisation = organisationService.findAllCustomer();

			return new ResponseEntity<ResponseBodyDTO<List<Organisation>>>(
					new ResponseBodyDTO<List<Organisation>>(true, "Fetched Organisation(s) Successfully", organisation),
					HttpStatus.OK);
		}

		catch (Exception exception) {
			logger.error("Exception occurred while getting companyList", exception);
			return new ResponseEntity(exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	// ------------------------------Aamir 1 End ---------------------------------//

	  @GetMapping("/core/customer/company")
		public ResponseEntity<List<Organisation>> getCustomerCompany(@RequestParam(value = "logUUid", required = false) String logUUid) {
			try {
				List<Organisation> customerCompanyList = customerService.getCustomerCompany();
				return new ResponseEntity<>(customerCompanyList, HttpStatus.OK);
			}
			
			catch (Exception exception) {
				logger.error("Exception occurred while getting companyList", exception);
				 return new ResponseEntity(exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
	  
	  
	  @GetMapping("/core/customer/company/dto")
		public ResponseEntity<List<OrganisationDtoForInventory>> getCustomerCompanyDto(@RequestParam(value = "logUUid", required = false) String logUUid) {
			try {
				List<OrganisationDtoForInventory> customerCompanyList = customerService.getCustomerCompanyDto();
				return new ResponseEntity<>(customerCompanyList, HttpStatus.OK);
			}
			
			catch (Exception exception) {
				logger.error("Exception occurred while getting companyList", exception);
				 return new ResponseEntity(exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
	  
}
