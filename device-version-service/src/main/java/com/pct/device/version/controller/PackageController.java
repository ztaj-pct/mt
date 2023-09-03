package com.pct.device.version.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pct.common.controller.IApplicationController;
import com.pct.common.dto.ResponseBodyDTO;
import com.pct.common.util.JwtUser;
import com.pct.common.util.JwtUtil;
import com.pct.device.version.dto.MessageDTO;
import com.pct.device.version.exception.BadRequestException;
import com.pct.device.version.exception.BaseMessageException;
import com.pct.device.version.exception.DeviceVersionException;
import com.pct.device.version.model.Package;
import com.pct.device.version.payload.PackagePayload;
import com.pct.device.version.payload.SavePackageRequest;
import com.pct.device.version.service.ICampaignExecutionService;
import com.pct.device.version.service.IPackageService;
import com.pct.device.version.util.Constants;

@RestController
@RequestMapping("/package")
@CrossOrigin("*")
public class PackageController implements IApplicationController<Package> {

	Logger logger = LoggerFactory.getLogger(PackageController.class);

	private static Integer DEFAULT_PAGE_SIZE = 10000;
	@Autowired
	private IPackageService packageService;
	@Autowired
	private JwtUtil jwtUtil;
	@Autowired
	private ICampaignExecutionService campaignExecution ;

	@PostMapping()
	public ResponseEntity<ResponseBodyDTO> createPackage(@RequestBody List<SavePackageRequest> savePackageRequest,
			HttpServletRequest httpServletRequest) {
		try {
			logger.info("Request received for savePackageRequest {}", savePackageRequest.toString());
			JwtUser jwtUser = (JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			packageService.savePackage(savePackageRequest, jwtUser.getUsername());
			logger.info("Package Saved Successfully.");
			String message = "Successfully saved Packages ";
			return new ResponseEntity(new ResponseBodyDTO(true, message), HttpStatus.OK);
		} catch (BaseMessageException e) {
			logger.error("Exception while saving Packages ", e);
			return new ResponseEntity(new ResponseBodyDTO(false, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (DeviceVersionException e) {
			logger.error("Exception while saving Packages ", e);
			return new ResponseEntity(new ResponseBodyDTO(false, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			logger.error("Exception while saving Packages ", e);
			return new ResponseEntity(new ResponseBodyDTO(false, "Exception occurred while saving Packages"),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/v1/exportToCsv")
	public void exportToCsv(HttpServletResponse response) throws IOException {
		logger.info("Inside exportToCsv Method");
		String csvFileName = Constants.CSVFILE;
		response.setContentType("text/csv");
		String headerKey = "Content-Disposition";
		String headerValue = String.format("attachment; filename=\"%s\"",
				csvFileName);
		response.setHeader(headerKey, headerValue);
		List<Package> allPackages=packageService.fetchALLPackagesForCsv(response);
		logger.info("The Size Of Package List Is : "+allPackages.size());
	}

	@PostMapping("/v1/exportToCsvFilter")
	public void exportToCsvFilteredRecords(@RequestParam(value = "_page", required = false) Integer page,
										   @RequestParam(value = "_limit", required = false) Integer pageSize,
										   @RequestParam(value = "_sort", required = false) String sort,
										   @RequestParam(value = "_order", required = false) String order,
										   @RequestBody Map<String, String> filterValues,HttpServletRequest httpServletRequest,
										   HttpServletResponse response) throws IOException {
		logger.info("Inside Package Controller exportToCsvFilter Method...");
		JwtUser jwtUser = (JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		//Long userId = jwtUtil.getUserIdFromRequest(httpServletRequest);
		boolean sortByInUse = false;
		if (page == null) {
			page = 1;
		} else if (page == -1 || page == 0) {
			throw new BadRequestException("invalid.page_index.in.input");
		}
		if (pageSize == null)
			pageSize = 25;
		if (pageSize > DEFAULT_PAGE_SIZE)
			pageSize = DEFAULT_PAGE_SIZE;
		if (sort.equalsIgnoreCase("isUsedInCampaign")) {
			sort = null;
			sortByInUse = true;
		}
		logger.info("Inside exportToCsvFilteredRecords Method");
		String csvFileName = Constants.CSVFILE_FILTERED;
		response.setContentType("text/csv");
		String headerKey = "Content-Disposition";
		String headerValue = String.format("attachment; filename=\"%s\"",
				csvFileName);
		response.setHeader(headerKey, headerValue);
		List<PackagePayload> payloadList = packageService.getAllPackage(filterValues,
				getPageable(page - 1, pageSize, sort, order), sortByInUse, order,jwtUser.getUsername()).getContent();
		if(payloadList.size()>0){
			packageService.fetchFilterdPackagesForCsv(payloadList,response);
			logger.info("Size of Filtered Package List Is : "+payloadList.size());
		}


	}

	@PostMapping("/getAll")
	public ResponseEntity<Object> getPackage(@RequestParam(value = "_page", required = false) Integer page,
			@RequestParam(value = "_limit", required = false) Integer pageSize,
			@RequestParam(value = "_sort", required = false) String sort,
			@RequestParam(value = "_order", required = false) String order,
			@RequestBody Map<String, String> filterValues,HttpServletRequest httpServletRequest) {
		logger.info("Inside getPackage");
		logger.info("Inside getPackage");
		JwtUser jwtUser = (JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
	//Long userId = jwtUtil.getUserIdFromRequest(httpServletRequest);
		try {
			boolean sortByInUse = false;
			if (page == null) {
				page = 1;
			} else if (page == -1 || page == 0) {
				throw new BadRequestException("invalid.page_index.in.input");
			}
			if (pageSize == null)
				pageSize = 25;
			if (pageSize > DEFAULT_PAGE_SIZE)
				pageSize = DEFAULT_PAGE_SIZE;
			if (sort.equalsIgnoreCase("isUsedInCampaign")) {
				sort = null;
				sortByInUse = true;
			}

			MessageDTO<Object> messageDto = new MessageDTO<>("Packages Fetched Successfully", true);
			Page<PackagePayload> packageResponse = packageService.getAllPackage(filterValues,
					getPageable(page - 1, pageSize, sort, order), sortByInUse, order,jwtUser.getUsername());

			messageDto.setBody(packageResponse.getContent());
			messageDto.setTotalKey(packageResponse.getTotalElements());
			messageDto.setCurrentPage(packageResponse.getNumber() + 1);
			messageDto.setTotal_pages(packageResponse.getTotalPages());

			return new ResponseEntity(messageDto, HttpStatus.OK);

		} catch (BaseMessageException e) {
			logger.error("Exception while fetching Packages ", e);
			return new ResponseEntity(new ResponseBodyDTO(false, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			logger.error("Exception while fetching Packages ", e);
			return new ResponseEntity(new ResponseBodyDTO(false, "Exception occurred while fetching Packages"),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@GetMapping("/check-packageName-exist/{packageName}")
	public ResponseEntity<MessageDTO<String>> checkPackageNameExist(@PathVariable("packageName") String packageName) {
		logger.info("Inside checkUniquePackageName");
		try {
			String message = "";
			Boolean isExist = packageService.getPackageByName(packageName);
			if (isExist) {
				message = "Package name " + packageName + " Alrady exist";
			} else {
				message = "Package name " + packageName + " is unique";
			}
			return new ResponseEntity<MessageDTO<String>>(new MessageDTO<String>(message, isExist), HttpStatus.OK);
		} catch (Exception e) {
			logger.error("Exception while checking package name ", e);
			return new ResponseEntity(new MessageDTO("Exception occurred while checking package name", false),
					HttpStatus.INTERNAL_SERVER_ERROR);

		}

	}

	@DeleteMapping(value = "/{uuid}")
	public ResponseEntity<ResponseBodyDTO> softDeletePackage(@PathVariable("uuid") String uuid) {
		logger.info("Inside Delete package");
		try {
			packageService.deleteById(uuid);
			return new ResponseEntity<ResponseBodyDTO>(new ResponseBodyDTO(true, "package Deleted successfully"),
					HttpStatus.OK);
		} catch (BaseMessageException e) {
			logger.error("Exception in delete Package ", e);
			return new ResponseEntity(new ResponseBodyDTO(false, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (DeviceVersionException e) {
			logger.error("Exception in delete Packages ", e);
			return new ResponseEntity(new ResponseBodyDTO(false, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			logger.error("Exception in delete Package ", e);
			return new ResponseEntity(new ResponseBodyDTO(false, "Exception occurred in Package delete"),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@GetMapping(value = "/{uuid}")
	public ResponseEntity<ResponseBodyDTO<PackagePayload>> getPackageByUuid(
			@Validated @PathVariable("uuid") String packageUuid) {
		logger.info("Inside getPackageByUuid");
		try {

			PackagePayload packageResponse = packageService.getByUuid(packageUuid);

			return new ResponseEntity<ResponseBodyDTO<PackagePayload>>(
					new ResponseBodyDTO<PackagePayload>("Fetched package by uuid", packageResponse), HttpStatus.OK);

		} catch (DeviceVersionException e) {
			logger.error("Exception while Fetching Packages ", e);
			return new ResponseEntity(new ResponseBodyDTO(false, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			logger.error("Exception while Fetching Packages ", e);
			return new ResponseEntity(new ResponseBodyDTO(false, "Error In Fetching package"),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PutMapping()
	public ResponseEntity<MessageDTO<String>> updatePackage(@RequestBody PackagePayload packageToUpdate,
			HttpServletRequest httpServletRequest) {
		logger.info("Inside UpdatePackage");
		try {
			//Long userId = jwtUtil.getUserIdFromRequest(httpServletRequest);
			JwtUser jwtUser = (JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			packageService.update(packageToUpdate, jwtUser.getUsername());
			return new ResponseEntity<MessageDTO<String>>(
					new MessageDTO<String>("package Updated  successfully", "", true), HttpStatus.CREATED);
		} catch (BaseMessageException e) {
			logger.error("Exception while updating the Package ", e);
			return new ResponseEntity(new ResponseBodyDTO(false, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (DeviceVersionException e) {
			logger.error("Exception while updating the Package ", e);
			return new ResponseEntity(new ResponseBodyDTO(false, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			logger.error("Exception while updating the Package ", e);
			return new ResponseEntity(new ResponseBodyDTO(false, "Error In updating the package"),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}
	
	@GetMapping("/emergency-stop")
	public ResponseEntity<MessageDTO<String>> getEmergencyStopFlagValue() {
		logger.info("Inside getEmergencyStopFlagValue");
		try {
			Boolean emergencyStopFlag = packageService.getEmergencyStopFlagValue();
			return new ResponseEntity<MessageDTO<String>>(new MessageDTO<String>("Fetched Emergency Stop Value Successfully", emergencyStopFlag), HttpStatus.OK);
		} catch (Exception e) {
			logger.error("Exception while getEmergencyStopFlagValue", e);
			return new ResponseEntity(new MessageDTO("Exception occurred while getEmergencyStopFlagValue", false),
					HttpStatus.INTERNAL_SERVER_ERROR);

		}

	}
	
	@PutMapping("/emergency-stop")
	public ResponseEntity<MessageDTO<String>> updateEmergencyStopFlagValue(
			@RequestParam(value = "isEmergencyStop", required = true) Boolean isEmergencyStop,
			HttpServletRequest httpServletRequest) {
		logger.info("Inside updateEmergencyStopFlagValue");
		try {
			Boolean flag = packageService.updateEmergencyStopFlagValue(isEmergencyStop);
			return new ResponseEntity<MessageDTO<String>>(
					new MessageDTO<String>("Emergency Stop Flag Value updated successfully", "", flag), HttpStatus.OK);
		} catch (DeviceVersionException e) {
			logger.error("Exception while updating the Emergency Stop Flag Value", e);
			return new ResponseEntity(new ResponseBodyDTO(false, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			logger.error("Exception while updating the Emergency Stop Flag Value ", e);
			return new ResponseEntity(new ResponseBodyDTO(false, "Exception while updating the Emergency Stop Flag Value"),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	@PutMapping("/manage-status")
	public ResponseEntity<MessageDTO<String>> manageCampaign(
			@RequestParam(value = "campaignUuid", required = false) String campaignUuid,
			@RequestParam(value = "campaignStatus", required = false) String campaignStatus,
			@RequestParam(value = "pauseLimit", required = false) Long pauseLimit,
			HttpServletRequest httpServletRequest) {
		    logger.info("Inside manageCampaign campaignUuid: "+campaignUuid+" campaignStatus: "+campaignStatus +" pauseLimit:"+pauseLimit);		try {
			//Long userId = jwtUtil.getUserIdFromRequest(httpServletRequest);
			JwtUser jwtUser = (JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			String uuid = campaignExecution.manageCampaignStatus(campaignUuid, jwtUser.getUsername(), campaignStatus,pauseLimit);
			return new ResponseEntity<MessageDTO<String>>(
					new MessageDTO<String>("Campaign status changed successfully", uuid, true), HttpStatus.OK);
		} catch (DeviceVersionException e) {
			logger.error("Exception while changing the Campaign status ", e);
			return new ResponseEntity(new ResponseBodyDTO(false, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			logger.error("Exception while changing the Campaign status ", e);
			return new ResponseEntity(new ResponseBodyDTO(false, "Error while changing the Campaign status "),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
