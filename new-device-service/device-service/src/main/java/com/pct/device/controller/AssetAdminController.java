package com.pct.device.controller;

import com.pct.common.controller.IApplicationController;
import com.pct.common.model.Asset;
import com.pct.common.util.JwtUtil;
import com.pct.device.dto.AssetResponseDTO;
import com.pct.device.dto.MessageDTO;
import com.pct.device.exception.DeviceException;
import com.pct.device.exception.DuplicateVinException;
import com.pct.device.exception.ManufacturerNotFoundException;
import com.pct.device.payload.AddAssetResponse;
import com.pct.device.payload.AssetCompany;
import com.pct.device.payload.AssetRecordPayload;
import com.pct.device.payload.AssetsPayload;
import com.pct.device.service.IAssetAdminService;
import com.pct.device.service.IAssetsService;
import com.pct.device.util.MessageKeys;
import com.pct.device.util.WriteCsvToResponse;
import io.micrometer.core.annotation.Timed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.FileCopyUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/asset")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AssetAdminController implements IApplicationController<Asset> {

	private static final Logger assetControllerLogger = LoggerFactory.getLogger(AssetController.class);

	@Autowired
	private IAssetAdminService assetAdminService;
	@Autowired
	private IAssetsService service;
	@Autowired
	private JwtUtil jwtutil;
	@Autowired
	private MessageSource responseMessageSource;

	@PostMapping("/bulk")
	public ResponseEntity<MessageDTO<Map<String, List<String>>>> addAssets(@Validated @RequestBody AssetCompany assetsPayloads, HttpServletRequest httpServletRequest) {
		assetControllerLogger.info("Inside addAssets");
		Map<String, List<String>> failedAssets = null;
		try {
			Long userId = jwtutil.getUserIdFromRequest(httpServletRequest);
			failedAssets = assetAdminService.addAssets(assetsPayloads, userId);
		} catch (Exception e) {
			logger.error("Exception occurred in processing uploaded file", e);
			return new ResponseEntity<MessageDTO<Map<String, List<String>>>>(new MessageDTO<Map<String, List<String>>>(e.getMessage(), failedAssets, false),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}

		return new ResponseEntity<MessageDTO<Map<String, List<String>>>>(new MessageDTO<Map<String, List<String>>>("Assets saved successfully", failedAssets, true),
				HttpStatus.CREATED);
	}

	@PostMapping()
	public ResponseEntity<MessageDTO<AddAssetResponse>> addAsset(@Validated @RequestBody AssetsPayload assetsPayloads, HttpServletRequest httpServletRequest) {
		assetControllerLogger.info("Inside addAsset");
		AddAssetResponse addAssetResponse = null;
		try {
			Long userId = jwtutil.getUserIdFromRequest(httpServletRequest);
			addAssetResponse = assetAdminService.addAsset(assetsPayloads, userId);
		} catch (ManufacturerNotFoundException e) {
			return new ResponseEntity<MessageDTO<AddAssetResponse>>(new MessageDTO<AddAssetResponse>(responseMessageSource
					.getMessage(MessageKeys.MANUFACTURER_IS_INVALID, new Object[]{}, Locale.ENGLISH), false),
					HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (DuplicateVinException e) {
			return new ResponseEntity<MessageDTO<AddAssetResponse>>(new MessageDTO<AddAssetResponse>(responseMessageSource
					.getMessage(MessageKeys.DUPLICATE_VIN, new Object[]{}, Locale.ENGLISH), false,"VIN Already in Use"),
					HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (DeviceException e) {
			return new ResponseEntity<MessageDTO<AddAssetResponse>>(new MessageDTO<AddAssetResponse>(responseMessageSource
					.getMessage(e.getMessage(), new Object[]{}, Locale.ENGLISH), false,e.getTitle()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
		catch(Exception e) {
			return new ResponseEntity<MessageDTO<AddAssetResponse>>(new MessageDTO<AddAssetResponse>(e.getMessage(), false),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
		boolean status = true;
		HttpStatus httpStatus = HttpStatus.CREATED;
		String message = "Asset added successfully";
		if(addAssetResponse.getErrors() != null && !addAssetResponse.getErrors().isEmpty()) {
			status = false;
			message = "";
		}
		return new ResponseEntity<MessageDTO<AddAssetResponse>>(new MessageDTO<AddAssetResponse>(message,
				addAssetResponse, status), httpStatus);
	}

	@PutMapping("/admin")
	public ResponseEntity<MessageDTO<AddAssetResponse>> updateAsset(@Validated @RequestBody AssetsPayload assetsPayloads, HttpServletRequest httpServletRequest) {
		assetControllerLogger.info("Inside updateAsset");
		Long userId = jwtutil.getUserIdFromRequest(httpServletRequest);
		AddAssetResponse addAssetResponse = null;
		try {
			addAssetResponse  =	assetAdminService.updateAsset(assetsPayloads, userId);
		} catch (ManufacturerNotFoundException e) {
			return new ResponseEntity<MessageDTO<AddAssetResponse>>(new MessageDTO<AddAssetResponse>(responseMessageSource
					.getMessage(MessageKeys.MANUFACTURER_IS_INVALID, new Object[]{}, Locale.ENGLISH), false),
					HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (DuplicateVinException e) {
			return new ResponseEntity<MessageDTO<AddAssetResponse>>(new MessageDTO<AddAssetResponse>(responseMessageSource
					.getMessage(MessageKeys.DUPLICATE_VIN, new Object[]{}, Locale.ENGLISH), false,"VIN Already in Use"),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
		catch (DeviceException e) {
			return new ResponseEntity<MessageDTO<AddAssetResponse>>(new MessageDTO<AddAssetResponse>(responseMessageSource
					.getMessage(e.getMessage(), new Object[]{}, Locale.ENGLISH), false,e.getTitle()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}catch(Exception e) {
			return new ResponseEntity<MessageDTO<AddAssetResponse>>(new MessageDTO<AddAssetResponse>(e.getMessage(), false),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}

		boolean status = true;
		HttpStatus httpStatus = HttpStatus.CREATED;
		String message = "Asset updated successfully";
		if(addAssetResponse.getErrors() != null && !addAssetResponse.getErrors().isEmpty()) {
			status = false;
			httpStatus = HttpStatus.BAD_REQUEST;
			message = "";
		}
		return new ResponseEntity<MessageDTO<AddAssetResponse>>(new MessageDTO<AddAssetResponse>(message,
				addAssetResponse, status), httpStatus);
	}


	@GetMapping("/overwrite/{companyId}")
	public ResponseEntity<MessageDTO<Boolean>> checkForAssetOverwrite(@PathVariable("companyId") Long companyId) {
		assetControllerLogger.info("Inside checkForAssetOverwrite");
		try {
			Boolean hasExistingAsset = assetAdminService.checkForAssetOverwrite(companyId);
			return new ResponseEntity<MessageDTO<Boolean>>(
					new MessageDTO<Boolean>("checkForAssetOverwrite", hasExistingAsset), HttpStatus.OK);
		} catch (Exception e) {
			logger.error("Exception while checking for asset overwrite", e);
			return new ResponseEntity<MessageDTO<Boolean>>(
					new MessageDTO<Boolean>("Exception while checking for asset overwrite", false), HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@PutMapping("/bulk")
	public ResponseEntity<MessageDTO<Map<String, List<String>>>> addModifiedAssets(@Validated @RequestBody AssetCompany assetsPayloads, HttpServletRequest httpServletRequest) {
		assetControllerLogger.info("Inside addModifiedAssets");
		Long userId = jwtutil.getUserIdFromRequest(httpServletRequest);
		Map<String, List<String>> failedAssets = null;
		try {
			failedAssets = assetAdminService.addModifiedAssets(assetsPayloads, userId);
		} catch (Exception e) {
			logger.error("Exception while updating assets in bulk", e);
			return new ResponseEntity<MessageDTO<Map<String, List<String>>>>(new MessageDTO<Map<String, List<String>>>(e.getMessage(), failedAssets, false),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}

		return new ResponseEntity<MessageDTO<Map<String, List<String>>>>(new MessageDTO<Map<String, List<String>>>("Assets saved successfully", failedAssets, true),
				HttpStatus.CREATED);
	}

	@GetMapping(value = "/download/{companyId}")
	public ResponseEntity<Object> downloadCSV(HttpServletResponse response, @PathVariable("companyId") Long companyId,
											  @RequestParam(value = "_page", required = false) Integer page,
											  @RequestParam(value = "_limit", required = false) Integer size,
											  @RequestParam(value = "sort", required = false) String sort,
											  @RequestParam(value = "_order", required = false) String order,
											  @RequestParam(value = "_fromDate", required = false) String fromDate,
											  @RequestParam(value = "_toDate", required = false) String toDate,
											  @RequestParam(value = "_error", required = false) String byError,
											  HttpServletRequest httpServletRequest) {

		MessageDTO<Object> messageDto = new MessageDTO<Object>("");
		try {
			// List<CommunicationLog> items = pageList.getContent();
			Long userId = jwtutil.getUserIdFromRequest(httpServletRequest);
			Pageable p = getPageable(0, 1000, null, "ASC");
			Page<AssetResponseDTO> assetResponseDTOList = assetAdminService.getAllActive(p, companyId, userId);
			if (assetResponseDTOList != null && assetResponseDTOList.getContent() != null && assetResponseDTOList.getContent().size() > 0) {
				WriteCsvToResponse.writeCommunicationsToCsvUsingStringArray(response.getWriter(), assetResponseDTOList.getContent());
			}
			return new ResponseEntity<Object>(messageDto, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Error:", e);
			return new ResponseEntity<Object>(HttpStatus.BAD_REQUEST);
		}

	}
	
	 @GetMapping("/asset-upload-template")
	    @Timed
	    @ResponseBody
	    public ResponseEntity<ByteArrayResource> getImportTemplate() throws IOException {
	        Resource resource = new ClassPathResource("templates/DF_Data_Collection_Template_draft3.xlsx");
	        byte[] bdata = FileCopyUtils.copyToByteArray(resource.getInputStream());
	        ByteArrayResource byteArrayResource = new ByteArrayResource(bdata);
	        HttpHeaders headers = new HttpHeaders();
	        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Data_Collection_Template_df");
	        return ResponseEntity.ok()
	            .headers(headers)
	            .contentLength(byteArrayResource.contentLength())
	            .contentType(MediaType.parseMediaType("application/octet-stream"))
	            .body(byteArrayResource);
	    }

	@GetMapping("/admin")
	public ResponseEntity<MessageDTO<List<AssetsPayload>>> getAssets(HttpServletRequest httpServletRequest) {
		assetControllerLogger.info("Inside getAssets");
		try {
			Long userId = jwtutil.getUserIdFromRequest(httpServletRequest);
			List<AssetsPayload> assetsPayload = service.getAssetsByCompany(userId);
			return new ResponseEntity<MessageDTO<List<AssetsPayload>>>(
					new MessageDTO<List<AssetsPayload>>("Fetched Assets", assetsPayload), HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Failed while getting Assets");
		}

	}

	@GetMapping("/{assetId}")
	public ResponseEntity<MessageDTO<AssetsPayload>> getAssetById(@PathVariable("assetId") Integer assetId) {
		assetControllerLogger.info("Inside getAssetById");
		try {
			AssetsPayload assetsPayload = service.find(assetId);
			return new ResponseEntity<MessageDTO<AssetsPayload>>(
					new MessageDTO<AssetsPayload>("Fetched AssetById", assetsPayload), HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Failed while getting Asset by id");
		}

	}

	@DeleteMapping("/{assetUuids}")
	public ResponseEntity<MessageDTO<List<String>>> batchDeleteAssets(
			@PathVariable("assetUuids") List<String> assetUuids) {
		assetControllerLogger.info("Inside batchDeleteAssets");
		try {
			List<String> failedAssets = service.deleteBatchAssets(assetUuids);
			return new ResponseEntity<MessageDTO<List<String>>>(
					new MessageDTO<List<String>>("Assets Deleted successfully", failedAssets), HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<MessageDTO<List<String>>>(
					new MessageDTO<List<String>>(e.getMessage(), null, false), HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@GetMapping("/page")
	// @Secured(AuthoritiesConstants.SUPER_ADMIN)
	public ResponseEntity<Page<AssetResponseDTO>> getAllAsset(@RequestParam(value = "_page", required = false) Integer page,
											  @RequestParam(value = "_type", required = false) String type,
											  @RequestParam(value = "_limit", required = false) Integer size,
											  @RequestParam(value = "_sort", required = false) String sort,
											  @RequestParam(value = "_order", required = false) String order,
											  @RequestParam(value = "companyId", required = false) Long companyId,
											  HttpServletRequest httpServletRequest) {

		MessageDTO<Page<AssetResponseDTO>> messageDto = new MessageDTO<>("Users Fetched Successfully", true);
		Long userId = jwtutil.getUserIdFromRequest(httpServletRequest);
		Page<AssetResponseDTO> users = assetAdminService.getAllActive(getPageable(page - 1, size, sort, order), companyId, userId);
		messageDto.setBody(users);
		messageDto.setTotalKey(users.getTotalElements());
		messageDto.setCurrentPage(users.getNumber());
		messageDto.setTotal_pages(users.getTotalPages());

		return new ResponseEntity(messageDto, HttpStatus.OK);

	}

	@GetMapping("/summary")
	public ResponseEntity<MessageDTO<Page<AssetRecordPayload>>> getCustomerAssets(@RequestParam(value = "_page", required = false) Integer page,
																				  @RequestParam(value = "_type", required = false) String type,
																				  @RequestParam(value = "_limit", required = false) Integer size,
																				  @RequestParam(value = "_sort", required = false) String sort,
																				  @RequestParam(value = "_order", required = false) String order,
																				  HttpServletRequest httpServletRequest) {

		assetControllerLogger.info("Inside getCustomerAssets");
		Long userId = jwtutil.getUserIdFromRequest(httpServletRequest);
		MessageDTO<Page<AssetRecordPayload>> messageDto = new MessageDTO<>("Customer Assets Feteched Successfully", true);
		Page<AssetRecordPayload> assets = service.getCustomerAssets(getPageable(page - 1, size, sort, order), userId);
		messageDto.setBody(assets);
		messageDto.setTotalKey(assets.getTotalElements());
		messageDto.setCurrentPage(assets.getNumber());
		messageDto.setTotal_pages(assets.getTotalPages());

		return new ResponseEntity(messageDto, HttpStatus.OK);

	}
	
	@PostMapping("/page")
	// @Secured(AuthoritiesConstants.SUPER_ADMIN)
	public ResponseEntity<Object> getAllAssetCompanyList(@RequestParam(value = "_page", required = false) Integer page,
											  @RequestParam(value = "_type", required = false) String type,
											  @RequestParam(value = "_limit", required = false) Integer size,
											  @RequestParam(value = "_sort", required = false) String sort,
											  @RequestParam(value = "_order", required = false) String order,
											  @RequestParam(value = "_yearFilter", required = false) String yearFilter,
											  @RequestParam(value = "companyId", required = false) Long companyId,
											  @RequestBody Map<String, String> filterValues,
											  HttpServletRequest httpServletRequest) {
		System.out.println(yearFilter);
		MessageDTO<Page<AssetResponseDTO>> messageDto = new MessageDTO<>("Users Fetched Successfully", true);
		Long userId = jwtutil.getUserIdFromRequest(httpServletRequest);
		Page<AssetResponseDTO> users = assetAdminService.getAllActiveCompanyList(getPageable(page - 1, size, sort, order), companyId, userId,filterValues,yearFilter);
		messageDto.setBody(users);
		messageDto.setTotalKey(users.getTotalElements());
		messageDto.setCurrentPage(users.getNumber());
		messageDto.setTotal_pages(users.getTotalPages());

		return new ResponseEntity(messageDto, HttpStatus.OK);

	}
	
	@PostMapping("/summary")
	public ResponseEntity<Object> getCustomerAssetsList(@RequestParam(value = "_page", required = false) Integer page,
																				  @RequestParam(value = "_type", required = false) String type,
																				  @RequestParam(value = "_limit", required = false) Integer size,
																				  @RequestParam(value = "_sort", required = false) String sort,
																				  @RequestParam(value = "_order", required = false) String order,
																				  @RequestParam(value = "_filterModelCountFilter", required = false) String filterModelCountFilter,
																				  @RequestBody Map<String, String> filterValues,
																				  HttpServletRequest httpServletRequest) {

		assetControllerLogger.info("Inside getCustomerAssetsList");
		Long userId = jwtutil.getUserIdFromRequest(httpServletRequest);
		MessageDTO<Page<AssetRecordPayload>> messageDto = new MessageDTO<>("Customer Assets Feteched Successfully", true);
		Page<AssetRecordPayload> assets = service.getCustomerAssetsList(getPageable(page - 1, size, sort, order), userId,filterValues,filterModelCountFilter);
		messageDto.setBody(assets);
		messageDto.setTotalKey(assets.getTotalElements());
		messageDto.setCurrentPage(assets.getNumber());
		messageDto.setTotal_pages(assets.getTotalPages());

		return new ResponseEntity(messageDto, HttpStatus.OK);

	
	}

	@PostMapping("/downloadCSV")
	public ResponseEntity<Object> getFilteredAssetCompanyList(HttpServletResponse response1,
			@RequestParam(value = "_page", required = false) Integer page,
			@RequestParam(value = "_type", required = false) String type,
			@RequestParam(value = "_limit", required = false) Integer size,
			@RequestParam(value = "_sort", required = false) String sort,
			@RequestParam(value = "_order", required = false) String order,
			@RequestParam(value = "_yearFilter", required = false) String yearFilter,
			@RequestParam(value = "companyId", required = false) Long companyId,
			@RequestBody Map<String, String> filterValues, HttpServletRequest httpServletRequest) {

		MessageDTO<Page<AssetResponseDTO>> messageDto = new MessageDTO<>("Users Fetched Successfully", true);

		try {
			Long userId = jwtutil.getUserIdFromRequest(httpServletRequest);
			Page<AssetResponseDTO> users = assetAdminService.getAllActiveCompanyList(
					getPageable(0, 10000, sort, order), companyId, userId, filterValues, yearFilter);
			if (users != null && users.getContent() != null && users.getContent().size() > 0) {
				WriteCsvToResponse.writeCommunicationsToCsvUsingStringArray(response1.getWriter(), users.getContent());
			}
			return new ResponseEntity<Object>(messageDto, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Error:", e);
			return new ResponseEntity<Object>(HttpStatus.BAD_REQUEST);
		}
	}
	
	@GetMapping("/getSuperAdmin")
	public List<String> getSuperAdminUserList() {
		assetControllerLogger.info("Inside getSuperAdminUserList");
		try {
			List<String> users = assetAdminService.getSuperAdminUser();
			return users;
		} catch (Exception e) {
			logger.error("Exception while fatching user list", e);
			 throw new RuntimeException("Exception while fatching user list");
		}

	}
}


