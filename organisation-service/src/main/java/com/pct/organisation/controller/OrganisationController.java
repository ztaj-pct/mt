package com.pct.organisation.controller;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StopWatch;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pct.common.dto.ResponseBodyDTO;
import com.pct.common.model.Organisation;
import com.pct.common.model.User;
import com.pct.common.util.Context;
import com.pct.common.util.JwtUser;
import com.pct.common.util.JwtUtil;
import com.pct.common.util.Logutils;
import com.pct.organisation.dto.CustomerWithLocation;
import com.pct.organisation.dto.MessageDTO;
import com.pct.organisation.dto.OrganisationDTO;
import com.pct.organisation.exception.BadRequestException;
import com.pct.organisation.exception.BaseMessageException;
import com.pct.organisation.payload.AddOrganisationPayload;
import com.pct.organisation.payload.OrganisationAccessDTO;
import com.pct.organisation.payload.OrganisationListPayload;
import com.pct.organisation.payload.OrganisationPayload;
import com.pct.organisation.payload.OrganisationRequest;
import com.pct.organisation.service.ICustomerService;
import com.pct.organisation.service.IOrganisationService;
import com.pct.organisation.service.impl.OrganisationServiceImpl;
import com.pct.organisation.util.RestUtils;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@RequestMapping("/organisation")
@Api(value = "/organisation", tags = "Organisation Management")
public class OrganisationController implements IApplicationController<Organisation> {

	Logger logger = LoggerFactory.getLogger(OrganisationController.class);
	public static final String className = "OrganisationController";

	private static Integer DEFAULT_PAGE_SIZE = 10000;
	@Autowired
	private IOrganisationService organisationService;

	@Autowired
	private ICustomerService customerService;

	@Autowired
	private MessageSource responseMessageSource;

	@Autowired
	private RestUtils restUtils;

	@Autowired
	private JwtUtil jwtutil;

	@ApiOperation(value = "Get Customer Organisation", notes = "API to get customer organisation", response = Object.class, tags = {
			"Organisation Management" })
	@ApiResponses(value = { @ApiResponse(code = 201, message = "Fetch Successfully", response = Object.class),
			@ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 401, message = "Unauthorized"),
			@ApiResponse(code = 403, message = "Forbidden"),
			@ApiResponse(code = 500, message = "Internal Server Error") })
	@PostMapping("/page")
	public ResponseEntity<Object> getOrganisationListWithPagination(
			@RequestParam(value = "_page", required = false) Integer page,
			@RequestParam(value = "_limit", required = false) Integer pageSize,
			@RequestParam(value = "_type", required = true) String type,
			@RequestParam(value = "_sort", required = false) String sort,
			@RequestParam(value = "_order", required = false) String order,
			@RequestBody Map<String, String> filterValues, HttpServletRequest httpServletRequest) {
		Context context = new Context();
		String methodName = "getOrganisation";
		try {
			logger.info("Inside try block of getOrganisation Method From OrganisationController ");
			Logutils.log(className, methodName, context.getLogUUId(),
					" Before calling getOrganisation Method From OrganisationController Service method in controller",
					logger);

			JwtUser jwtUser = (JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			logger.info("Username : " + jwtUser.getUsername());

			MessageDTO<Object> messageDto = new MessageDTO<>("Organisation Fetched Successfully", true);
			if ("organisationRole".equalsIgnoreCase(sort)) {
				sort = null;
			}

			Page<OrganisationPayload> packageResponse = organisationService.getAllAOrganisation(filterValues,
					getPageable(page - 1, pageSize, sort, order), type, jwtUser.getUsername(), context, sort);

			messageDto.setBody(packageResponse.getContent());
			messageDto.setTotalKey(packageResponse.getTotalElements());
			messageDto.setCurrentPage(packageResponse.getNumber());
			messageDto.setTotal_pages(packageResponse.getTotalPages());
			Logutils.log(className, methodName, context.getLogUUId(),
					" after calling getOrganisation Method From OrganisationController", logger);
			logger.info("After getting response from getOrganisationListWithPagination from organisation controller :");
			return new ResponseEntity(messageDto, HttpStatus.OK);

		} catch (BaseMessageException e) {
			logger.error("Exception while fetching organisations ", e);
			return new ResponseEntity(new ResponseBodyDTO(false, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity(new ResponseBodyDTO(false, "Exception occurred while fetching organisations"),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@ApiOperation(value = "get list of getCustomerOrganisationsHub", notes = "API to get list of getCustomer Organisations Hub", response = OrganisationPayload.class, tags = {
			"Organisation Management" })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Customer Organisation Hub", response = OrganisationPayload.class),
			@ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 401, message = "Unauthorized"),
			@ApiResponse(code = 403, message = "Forbidden"),
			@ApiResponse(code = 500, message = "Internal Server Error") })
	@GetMapping("/customer/hub")
	public ResponseEntity<MessageDTO<List<OrganisationPayload>>> getCustomerOrganisations() {
		logger.info("Inside getCustomerOrganisations Method From OrganisationController");
		Context context = new Context();
		String methodName = "getCustomerOrganisations";
		try {
			logger.info("Inside try block of getCustomerOrganisations Method From OrganisationController ");
			logger.info("Before getting response from getCustomerOrganisations from organisation controller :");
			List<OrganisationPayload> organisations = organisationService.getCustomerOrganisationsFromHub(context);
			Logutils.log(className, methodName, context.getLogUUId(),
					" after calling getCustomerOrganisations Method From OrganisationController", logger);
			logger.info("Exiting from getCustomerOrganisations Method of OrganisationController");
			logger.info("After getting response from getCustomerOrganisations from organisation controller :");
			return new ResponseEntity<MessageDTO<List<OrganisationPayload>>>(
					new MessageDTO<List<OrganisationPayload>>("Fetched all active organisations", organisations),
					HttpStatus.OK);
		} catch (Exception e) {
			logger.error("Exception while getting all active Organisations", e);
			throw new RuntimeException("Failed while getting all active Organisations");
		}
	}

	@ApiOperation(value = "To inactivate organisation using id", notes = "API to inactivate the organisation", response = String.class, tags = {
			"Organisation Management" })
	@ApiResponses(value = { @ApiResponse(code = 201, message = "Deleted", response = String.class),
			@ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 401, message = "Unauthorized"),
			@ApiResponse(code = 403, message = "Forbidden"),
			@ApiResponse(code = 500, message = "Internal Server Error") })
	@DeleteMapping()
	public ResponseEntity<MessageDTO<String>> deleteOrganisation(
			@RequestParam(value = "uuid", required = false) String organisationUuid) {
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		logger.info("Request received to delete organisation detail {} in organisation controller"
				+ stopWatch.prettyPrint());
		Context context = new Context();
		String methodName = "softDeleteOrganisation1";
		try {
			logger.info("Inside try block of softDeleteOrganisation1 Method From OrganisationController ");
			Logutils.log(className, methodName, context.getLogUUId(),
					" Before calling softDeleteOrganisation1 Method From OrganisationController Service method in controller",
					logger);
			organisationService.deleteByUuid(organisationUuid, context);
			logger.info("Exiting from softDeleteOrganisation1 Method of OrganisationController");
			return new ResponseEntity<MessageDTO<String>>(
					new MessageDTO<String>("Organisation deleted successfully", "", true), HttpStatus.OK);

		} catch (Exception e) {
			logger.error("Error : ", e.getMessage());
			e.printStackTrace();
			return new ResponseEntity<MessageDTO<String>>(new MessageDTO<String>(e.getMessage(), false),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@ApiOperation(value = "Add new organisation", notes = "API to create a new organisation", response = String.class, tags = {
			"Organisation Management" })
	@ApiResponses(value = { @ApiResponse(code = 201, message = "Created", response = String.class),
			@ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 401, message = "Unauthorized"),
			@ApiResponse(code = 403, message = "Forbidden"),
			@ApiResponse(code = 500, message = "Internal Server Error") })
	@PostMapping()
	public ResponseEntity<ResponseBodyDTO> addOrganisation(
			@Validated @RequestBody AddOrganisationPayload addOrganisationPayload) {
		logger.info("Request received to add organisation detail {}");
		Context context = new Context();
		String methodName = "addOrganisation";
		try {
			logger.debug("Inside try block of addOrganisation Method From OrganisationController ");
			Logutils.log(className, methodName, context.getLogUUId(),
					" Before calling addOrganisation Method From OrganisationController Service method in controller",
					logger);
			if (addOrganisationPayload == null) {
				return new ResponseEntity<ResponseBodyDTO>(new ResponseBodyDTO(false, "Payload must not null"),
						HttpStatus.BAD_REQUEST);
			}
			Organisation org= organisationService.saveOrganisation(addOrganisationPayload, context);
			Logutils.log(className, methodName, context.getLogUUId(),
					" after calling addOrganisation Method From OrganisationController", logger);
			logger.info("Exiting from addOrganisation Method of OrganisationController");
			return new ResponseEntity<ResponseBodyDTO>(new ResponseBodyDTO(true, "Organisation added successfully",org.getId()),
					HttpStatus.CREATED);
		} catch (BaseMessageException e) {
			logger.info("validation failed while creating addOrganisation");
			logger.info("Validation failed message : {}", e.getMessage());
			return new ResponseEntity<>(new ResponseBodyDTO(false, e.getMessage()), HttpStatus.BAD_REQUEST);
		} catch (Exception e) {
			logger.error("Exception occurred while creating addOrganisation", e);
			e.printStackTrace();
			return new ResponseEntity<ResponseBodyDTO>(new ResponseBodyDTO(false, e.getMessage()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@ApiOperation(value = "Get All Organisation", notes = "API to get All organisation", response = Object.class, tags = {
			"Organisation Management" })
	@ApiResponses(value = { @ApiResponse(code = 201, message = "Fetch Successfully", response = Object.class),
			@ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 401, message = "Unauthorized"),
			@ApiResponse(code = 403, message = "Forbidden"),
			@ApiResponse(code = 500, message = "Internal Server Error") })
	@GetMapping("/getAllOrganisation")
	public ResponseEntity<List<Organisation>> getAllOrganisation(HttpServletRequest httpServletRequest) {
		Context context = new Context();
		String methodName = "getOrganisation";
		try {
			logger.info("Inside try block of getAllOrganisation Method From OrganisationController ");
			Logutils.log(className, methodName, context.getLogUUId(),
					" Before calling getOrganisation Method From OrganisationController Service method in controller",
					logger);

			JwtUser jwtUser = (JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			logger.info("Username : " + jwtUser.getUsername());

			MessageDTO<Object> messageDto = new MessageDTO<>("Organisation Fetched Successfully", true);
			List<Organisation> org = organisationService.getAllOrganisation(context);

			messageDto.setBody(org);

			Logutils.log(className, methodName, context.getLogUUId(),
					" after calling getOrganisation Method From OrganisationController", logger);
			logger.info("After getting response from getOrganisationListWithPagination from organisation controller :");
			return new ResponseEntity(messageDto, HttpStatus.OK);

		} catch (BaseMessageException e) {
			logger.error("Exception while fetching organisations ", e);
			return new ResponseEntity(new ResponseBodyDTO(false, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity(new ResponseBodyDTO(false, "Exception occurred while fetching organisations"),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/id")
	public ResponseEntity<OrganisationDTO> getCompanyForId(@RequestParam(value = "id", required = true) Long id) {
		try {
			logger.info("Request to get organisation details by id");
			OrganisationDTO organisation = organisationService.getOrganisationById(id);
			return new ResponseEntity<>(organisation, HttpStatus.OK);
		} catch (Exception exception) {
			logger.error("Exception occurred while getting Company", exception);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/byAccount")
	public ResponseEntity<OrganisationDTO> getCompanyForAccountNumber(
			@RequestParam(value = "id", required = true) String id) {
		try {
			logger.info("Request to get organisation details by id");
			OrganisationDTO organisation = organisationService.getOrganisationByAccountNumber(id);
			return new ResponseEntity<>(organisation, HttpStatus.OK);
		} catch (Exception exception) {
			logger.error("Exception occurred while getting Company", exception);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/getAll")
	public ResponseEntity<List<Organisation>> getListOfCompany() {
		try {
			JwtUser jwtUser = (JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			logger.info("Username : " + jwtUser.getUsername());
			logger.info("Request to get organisation details by type");
			List<Organisation> companies = organisationService.getListOfCompany(jwtUser.getUsername());
			return new ResponseEntity<>(companies, HttpStatus.OK);
		} catch (Exception exception) {
			logger.error("Exception occurred while getting Company", exception);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}


	@GetMapping("/getAllOrg")
	public ResponseEntity<List<OrganisationListPayload>> getAllCompany() {
		try {
			JwtUser jwtUser = (JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			logger.info("Username : " + jwtUser.getUsername());
			logger.info("Request to get organisation details by type");
			List<OrganisationListPayload> companies = organisationService.getAllCompany(jwtUser.getUsername());
			return new ResponseEntity<>(companies, HttpStatus.OK);
		} catch (Exception exception) {
			logger.error("Exception occurred while getting Company", exception);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@ApiOperation(value = "Get Organisation By Name", notes = "API to get organisation by name", response = Object.class, tags = {
			"Organisation Management" })
	@ApiResponses(value = { @ApiResponse(code = 201, message = "Fetch Successfully", response = Object.class),
			@ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 401, message = "Unauthorized"),
			@ApiResponse(code = 403, message = "Forbidden"),
			@ApiResponse(code = 500, message = "Internal Server Error") })
	@GetMapping("/getOrganisationByName")
	public ResponseEntity<OrganisationDTO> getOrganisationByName(@RequestParam(value = "name") String organisationName,
			@RequestParam(value = "logUuid", required = false) String logUuid) {
		Context context = new Context();

		if (logUuid != null && !logUuid.isEmpty()) {
			context.setLogUUId(logUuid);
		}

		String methodName = "getOrganisationByName";

		try {
			logger.info("Request to get organisation details by name");

			Logutils.log(className, methodName, context.getLogUUId(),
					" Before calling getOrganisationByName Method From OrganisationController Service method in controller",
					logger);

			OrganisationDTO organisation = organisationService.getOrganisationByName(organisationName, context);

			Logutils.log(className, methodName, context.getLogUUId(),
					" After calling getOrganisationByName Method From OrganisationController Service method in controller",
					logger);

			return new ResponseEntity<>(organisation, HttpStatus.OK);
		} catch (Exception exception) {
			logger.error("Exception occurred while getting Company", exception);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/getAllCompanyName")
	public ResponseEntity<List<String>> getListOfCompanyName() {
		try {
			logger.info("Request to get organisation details by type");
			List<String> companies = organisationService.getAllCompanyName();
			return new ResponseEntity<>(companies, HttpStatus.OK);
		} catch (Exception exception) {
			logger.error("Exception occurred while getting Company", exception);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@ApiOperation(value = "Get All Active Organisation By organisation role", notes = "API to get active All organisation By organisation role", response = MessageDTO.class, tags = {
			"Organisation Management" })
	@ApiResponses(value = { @ApiResponse(code = 400, message = "Bad Request"),
			@ApiResponse(code = 401, message = "Unauthorized"), @ApiResponse(code = 403, message = "Forbidden"),
			@ApiResponse(code = 500, message = "Internal Server Error") })
	@GetMapping("/getAllActiveByOrganisationRoles")
	public ResponseEntity<?> getAllActiveByOrganisationRoles(
			@RequestParam("organisationRoles") List<String> organisationRoles) {

		Context context = new Context();
		String methodName = "getAllActiveByOrganisationRole";

		try {
			Logutils.log(className, methodName, context.getLogUUId(),
					" Before calling getAllActiveByOrganisationRole Method From OrganisationController Service method in controller",
					logger);

			MessageDTO<List<OrganisationDTO>> messageDto = new MessageDTO<>("Organisation Fetched By Role Successfully",
					true);

			List<OrganisationDTO> organisationDTOs = organisationService
					.getAllActiveByOrganisationRoles(organisationRoles, context);

			messageDto.setBody(organisationDTOs);

			Logutils.log(className, methodName, context.getLogUUId(),
					" after calling getAllActiveByOrganisationRole Method From OrganisationController", logger);

			return new ResponseEntity<>(messageDto, HttpStatus.OK);

		} catch (BadRequestException e) {
			return new ResponseEntity<>(new ResponseBodyDTO<>(false, e.getMessage()), HttpStatus.BAD_REQUEST);
		} catch (Exception e) {
			logger.error("Exception while fetching all active organisation by role ", e);
			return new ResponseEntity<>(
					new ResponseBodyDTO<>(false, "Exception occurred while fetching all active organisation by role"),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@ApiOperation(value = "Organisation migrate role", notes = "API to organisation migrate role", response = MessageDTO.class, tags = {
			"Organisation Management" })
	@ApiResponses(value = { @ApiResponse(code = 400, message = "Bad Request"),
			@ApiResponse(code = 401, message = "Unauthorized"), @ApiResponse(code = 403, message = "Forbidden"),
			@ApiResponse(code = 500, message = "Internal Server Error") })
	@PostMapping("/migrate/role")
	public ResponseEntity<?> migrateOrganisationRole() {

		Context context = new Context();
		String methodName = "migrateOrganisationRole";

		try {
			Logutils.log(className, methodName, context.getLogUUId(),
					" Before calling migrateOrganisationRole Method From OrganisationController Service method in controller",
					logger);

			MessageDTO<Map<String, Integer>> messageDto = new MessageDTO<>("Organisation role migrate Successfully",
					true);

			Map<String, Integer> resultMap = organisationService.migrateOrganisationRole(context);

			messageDto.setBody(resultMap);

			Logutils.log(className, methodName, context.getLogUUId(),
					" after calling migrateOrganisationRole Method From OrganisationController", logger);

			return new ResponseEntity<>(messageDto, HttpStatus.OK);

		} catch (Exception e) {
			logger.error("Exception while organisation role migrate ", e);
			return new ResponseEntity<>(
					new ResponseBodyDTO<>(false, "Exception occurred while organisation role migrate"),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	// -------------------------------------Aamir 1 Start
	// -----------------------------------//

	@GetMapping("/uuid/{uuid}")
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

	@GetMapping()
	public ResponseEntity<MessageDTO<Page<OrganisationAccessDTO>>> getCustomerCompanies(
			@RequestParam(value = "_page", required = false) Integer page,
			@RequestParam(value = "_limit", required = false) Integer size,
			@RequestParam(value = "_type", required = true) String type,
			@RequestParam(value = "_sort", required = false) String sort,
			@RequestParam(value = "_order", required = false) String order,
			@RequestParam(value = "_status", required = false) Boolean status, HttpServletRequest httpServletRequest) {

		Context context = new Context();
		MessageDTO<Page<OrganisationAccessDTO>> messageDto = new MessageDTO<>("Companies Fetched Successfully", true);
		JwtUser jwtUser = (JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		User user = restUtils.getUserFromAuthService(jwtUser.getUsername());
		if (page == null && size == null && sort == null && order == null) {
			page = 1;
			size = 50;
			sort = "organisationName";
			order = "asc";
		}
		Page<OrganisationAccessDTO> organisation = organisationService.getAllActiveOrganisation(
				getPageable(page - 1, size, sort, order), type, status, user.getId(), context);
		messageDto.setBody(organisation);
		messageDto.setTotalKey(organisation.getTotalElements());
		messageDto.setCurrentPage(organisation.getNumber());
		messageDto.setTotal_pages(organisation.getTotalPages());
		return new ResponseEntity(messageDto, HttpStatus.OK);
	}

	@ApiOperation(value = "Get All Organisation", notes = "API to get All organisation", response = Object.class, tags = {
			"Organisation Management" })
	@ApiResponses(value = { @ApiResponse(code = 201, message = "Fetch Successfully", response = Object.class),
			@ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 401, message = "Unauthorized"),
			@ApiResponse(code = 403, message = "Forbidden"),
			@ApiResponse(code = 500, message = "Internal Server Error") })
	@GetMapping("/getAllOrganisationFilter")
	public ResponseEntity<Map<String, String>> getAllOrganisationFilter(
			@RequestParam(value = "id", required = false) String type, HttpServletRequest httpServletRequest) {
		Context context = new Context();
		String methodName = "getOrganisation";
		try {
			logger.info("Inside try block of getAllOrganisationOnTheBasisOfFilter Method From OrganisationController ");
			Logutils.log(className, methodName, context.getLogUUId(),
					" Before calling getOrganisation Method From OrganisationController Service method in controller",
					logger);

			JwtUser jwtUser = (JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			logger.info("Username : " + jwtUser.getUsername());

			MessageDTO<Object> messageDto = new MessageDTO<>("Organisation Fetched Successfully", true);
			List<OrganisationRequest> allOrganisationFilter = organisationService.getAllOrganisationFilter(context);
			messageDto.setBody(allOrganisationFilter);

			Logutils.log(className, methodName, context.getLogUUId(),
					" after calling getOrganisation Method From OrganisationController", logger);
			logger.info("After getting response from getOrganisationListWithPagination from organisation controller :");
			return new ResponseEntity(messageDto, HttpStatus.OK);

		} catch (BaseMessageException e) {
			logger.error("Exception while fetching organisations ", e);
			return new ResponseEntity(new ResponseBodyDTO(false, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity(new ResponseBodyDTO(false, "Exception occurred while fetching organisations"),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping(value = "/location/{userName}")
    public ResponseEntity<ResponseBodyDTO<CustomerWithLocation>> getCustomerAssetList(
            @Validated @PathVariable("userName") String userUuid) {
        logger.info("Inside getCustomerAndLocationByUser Method");
        try {
            CustomerWithLocation customerList = customerService
                    .getCustomerWithLocationByUser(userUuid);
            logger.info("Exiting from getCustomerAndLocationByUser Method");
            return new ResponseEntity<ResponseBodyDTO<CustomerWithLocation>>(
                    new ResponseBodyDTO<CustomerWithLocation>(true,
                            "Fetched customers successfully", customerList),
                    HttpStatus.OK);
        } catch (Exception exception) {
            logger.error("Getting Exception in getCustomerAndLocationByUser", exception.getMessage());
            return new ResponseEntity<ResponseBodyDTO<CustomerWithLocation>>(
                    new ResponseBodyDTO<CustomerWithLocation>(false, exception.getMessage()),
                    HttpStatus.BAD_REQUEST);
        }
    }
    

}
