package com.pct.device.controller;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pct.common.controller.IApplicationController;
import com.pct.common.model.Asset;
//import com.pct.common.model.AssetGatewayXref;
import com.pct.common.model.AssetSensorXref;
import com.pct.common.model.Asset_Device_xref;
import com.pct.common.model.Device;
import com.pct.common.payload.AssetSensorXrefPayload;
import com.pct.common.payload.SaveAssetGatewayXrefRequest;
import com.pct.common.util.JwtUser;
import com.pct.device.dto.AssetResponseDTO;
import com.pct.device.dto.AssetVinSearchDTO;
import com.pct.device.dto.MessageDTO;
import com.pct.device.dto.ResponseBodyDTO;
import com.pct.device.dto.ResponseDTO;
import com.pct.device.exception.DeviceException;
import com.pct.device.exception.DuplicateVinException;
import com.pct.device.exception.ManufacturerNotFoundException;
import com.pct.device.model.Lookup;
import com.pct.device.payload.AddAssetResponse;
import com.pct.device.payload.AssetAssociationPayload;
import com.pct.device.payload.AssetCompany;
import com.pct.device.payload.AssetRecordPayload;
import com.pct.device.payload.AssetsPayload;
import com.pct.device.payload.AssetsPayloadMobile;
import com.pct.device.payload.DeviceResponsePayload;
import com.pct.device.service.IAssetService;
import com.pct.device.util.AppUtility;
import com.pct.device.util.Context;
import com.pct.device.util.Logutils;
import com.pct.device.util.MessageKeys;
import com.pct.device.util.WriteCsvToResponse;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@RequestMapping("/asset")
@Api(value = "/asset", tags = "Asset Management")
public class AssetController implements IApplicationController<Asset> {
	public static final String className = "AssetController";
	private static final Logger LOGGER = LoggerFactory.getLogger(AssetController.class);

	@Autowired
	private IAssetService assetService;

	@Autowired
	private MessageSource responseMessageSource;


	//----------------------------------------------------------------Aamir-------------------------------------------------------------//
	 @GetMapping("/vin/{vin}")
	    public  ResponseEntity<ResponseBodyDTO<Asset>> getAssetByVin(@PathVariable("vin") String vin) {
		 String methodName="getAssetByVin";
		 Context context=new Context();
		 String logUUid=context.getLogUUId();
		Logutils.log(className,methodName,logUUid,"Inside startInstall Method From InstallerService " ,logger);
	        try {
	            Asset asset = assetService.getAssetByVin(vin);
	            if(asset!=null)
	            {
	            	Logutils.log(className,methodName,logUUid,"asset  Uuid : " + asset.getUuid(),logger);
	            }
//	            return new ResponseEntity<>(asset, HttpStatus.OK);
	            return new ResponseEntity<ResponseBodyDTO<Asset>>(
	            		new ResponseBodyDTO<Asset>(true, "Fetched Asset(s) Successfully",
	            				asset),
	            		HttpStatus.OK);
	        } catch (Exception e) {
	            logger.error("Exception while getting asset for vin {}", vin, e);
//	            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
	            return new ResponseEntity(
	                    new ResponseDTO(false, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
	        }
	    }
	 
	@GetMapping("/asset-sensor-xref/{assetUuid}")
	public ResponseEntity<ResponseBodyDTO<List<AssetSensorXref>>> getAllAssetSensorXrefForAssetUuid(@RequestParam(value = "logUUId", required = false) String logUUId, 
																									@PathVariable("assetUuid") String assetUuid) {
		try {
			StopWatch stopWatch = new StopWatch();
			stopWatch.start();
			 Logutils.log(logUUId," Before calling getAllAssetSensorXrefForAssetUuid method from assetService", logger, "asset_uuid: "+assetUuid," "+ stopWatch.prettyPrint());
	            
			 List<AssetSensorXref> allAssetSensorXrefForAssetUuid = assetService.getAllAssetSensorXrefForAssetUuid(assetUuid);
	        	
	         Logutils.log(logUUId," after calling getAllAssetSensorXrefForAssetUuid method from assetService", logger," "+ stopWatch.prettyPrint()); 
    		
			
			List<AssetSensorXrefPayload> assetSensorXrefPayload=new ArrayList<AssetSensorXrefPayload>();
			BeanUtils.copyProperties(allAssetSensorXrefForAssetUuid, assetSensorXrefPayload);
			return new ResponseEntity<ResponseBodyDTO<List<AssetSensorXref>>>(
						new ResponseBodyDTO<List<AssetSensorXref>>(true, "Fetched Device(s) Successfully",
								allAssetSensorXrefForAssetUuid),HttpStatus.OK);
			
//			return new ResponseEntity<>(allAssetSensorXrefForAssetUuid, HttpStatus.OK);
		} catch (Exception e) {
			logger.error("Exception while getting Asset Sensor Xref for asset id {}", assetUuid, e);
			return new ResponseEntity(
                    new ResponseDTO(false, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@GetMapping("/uuid/{uuid}")
	    public ResponseEntity<ResponseBodyDTO<List<AssetsPayload>>> getAssetByUUID(@RequestParam(value = "logUUId", required = false) String logUUId,
	    																		@PathVariable(value = "uuid", required = true) String uuid) {
		
		String methodName="getAssetByUUID";
		Logutils.log(className,methodName,logUUId,"Inside getAssetByUUID Method From InstallerService " ,logger);
	    	try {
	    		StopWatch stopWatch = new StopWatch();
				stopWatch.start();
	    		 Logutils.log(logUUId," Before calling findByUuid method from assetService", logger, "asset_uuid: "+uuid," "+ stopWatch.prettyPrint());
		            
	    		 Asset asset =  assetService.findByUuid(uuid);
	    		 
	    		 if(asset!=null)
	    		 {
	    			 Logutils.log(className,methodName,logUUId,"asset  Uuid : " + asset.getUuid(),logger);
	    		 }
		        	
		         Logutils.log(logUUId," after calling findByUuid method from assetService", logger, " "+stopWatch.prettyPrint()); 
	    		
	    		 AssetsPayload assetsPayload =new AssetsPayload();
	    		 BeanUtils.copyProperties(asset, assetsPayload);
	    		 List<AssetsPayload> assetLst=new ArrayList<>();
	    		 assetLst.add(assetsPayload);
	    		 return new ResponseEntity<ResponseBodyDTO<List<AssetsPayload>>>(
							new ResponseBodyDTO<List<AssetsPayload>>(true, "Fetched Device(s) Successfully",
									assetLst),HttpStatus.OK);
	        } catch (Exception exception) {
	            logger.error("Exception while resetting company data", exception);
	            return null;
	        }
	    }
	 	 
	 @PostMapping("/update-asset-company")
	    public ResponseEntity<ResponseDTO> updateCompanyDataForAsset(@RequestParam(value = "logUUId", required = false) String logUUId, 
														    		 @RequestParam(value = "account_number", required = true) String accountNumber,
														    		 @RequestParam(value = "asset_uuid", required = true) String asset_uuid) {
	    	logger.info("updating the value of asset company in case asset company is null  "+accountNumber + " "+asset_uuid);
	    	try {
	    		StopWatch stopWatch = new StopWatch();
				stopWatch.start();
	    		 Logutils.log(logUUId," Before calling updateCompanyInAsset method from assetService", logger, "account_number: "+accountNumber, "asset_uuid: "+asset_uuid," "+ stopWatch.prettyPrint());
	            
	    		 Boolean status =  assetService.updateCompanyInAsset(accountNumber, asset_uuid);
		        	
		         Logutils.log(logUUId," after calling updateCompanyInAsset method from assetService", logger," "+ stopWatch.prettyPrint()); 
	    		
	    		 logger.info("asset company has updated successfully");
	             return new ResponseEntity<ResponseDTO>(new ResponseDTO(status, "update asset company successful"), HttpStatus.OK);
	        } catch (Exception exception) {
	            logger.error("Exception while resetting company data", exception);
	            return new ResponseEntity<ResponseDTO>(
	                    new ResponseDTO(false, exception.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
	        }
	    }
	 
		@GetMapping("/is-asset-applicable-for-pre-pair/{assetUuid}")
		public ResponseEntity<ResponseDTO> isAssetApplicableForPrePair(@RequestParam(value = "logUUId", required = false) String logUUId, @PathVariable("assetUuid") String assetUuid) {
		     try {
		    	 StopWatch stopWatch = new StopWatch();
				 stopWatch.start();
		    	 Logutils.log(logUUId," Before calling isAssetHavePrePairProducts method from assetService", logger, "assetUUid: "+assetUuid," "+ stopWatch.prettyPrint());
		            
		    	 Boolean status = assetService.isAssetHavePrePairProducts(assetUuid);
		        	
		         Logutils.log(logUUId," after calling isAssetHavePrePairProducts method from assetService", logger," "+ stopWatch.prettyPrint());   	
		         return new ResponseEntity<ResponseDTO>(new ResponseDTO(status, "Reset successful"), HttpStatus.OK);
		     } catch (Exception exception) {
		         logger.error("Exception while checking Asset is Applicable For Pre Pair", exception);
		         return new ResponseEntity<ResponseDTO>(
		                 new ResponseDTO(false, exception.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		     }
		 }
		@PostMapping("/asset-device-xref")
		public ResponseEntity<ResponseBodyDTO<List<AssetsPayload>>> saveAssetDeviceXref(
				@RequestBody SaveAssetGatewayXrefRequest saveAssetGatewayXrefRequest) {
			try {
				StopWatch stopWatch = new StopWatch();
				stopWatch.start();
				Logutils.log(saveAssetGatewayXrefRequest.getLogUUId()," Before calling saveAssetDeviceXref method from assetService", logger, "AssetUuid: "+saveAssetGatewayXrefRequest.getAssetUuid()," Imei: "+saveAssetGatewayXrefRequest.getImei()," "+ stopWatch.prettyPrint());
				JwtUser jwtUser = (JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
				
				Asset_Device_xref assetGatewayXref = assetService.saveAssetDeviceXref(saveAssetGatewayXrefRequest,jwtUser.getUsername());
	        	
	        	Logutils.log(saveAssetGatewayXrefRequest.getLogUUId()," after calling saveAssetDeviceXref method from assetService", logger," "+ stopWatch.prettyPrint());
	            return new ResponseEntity<ResponseBodyDTO<List<AssetsPayload>>>(
						new ResponseBodyDTO<List<AssetsPayload>>(true, "Successfully saved AssetGatewayXref"),
						HttpStatus.OK);
			} catch (Exception exception) {
				logger.error("Exception while saving AssetGatewayXref", exception);
				return new ResponseEntity(new ResponseDTO(false, "Device "+saveAssetGatewayXrefRequest.getImei()+" already associated with Asset "+ saveAssetGatewayXrefRequest.getAssetUuid()), HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}

		@GetMapping("/can-vin")
		public ResponseEntity<? extends Object> getAssetByVinAndCan(
				@RequestParam(value = "logUUId", required = false) String logUUId,
				@RequestParam(value = "vin", required = true) String vin,
				@RequestParam(value = "can", required = true) String can) {
			try {

				String methodName = "getAssetByVinAndCan";
				Logutils.log(className, methodName, logUUId, " started updating sensor status", logger);
				StopWatch stopWatch = new StopWatch();
				stopWatch.start();
				Logutils.log(logUUId, " Before calling getAssetByVinAndCan method from assetService", logger,
						"vin: " + vin, " can: " + can, " " + stopWatch.prettyPrint());

				Asset asset = assetService.getAssetByVinAndCan(vin, can);
				if(asset!=null)
				{
					 Logutils.log(className,methodName,logUUId,"asset  Uuid : " + asset.getUuid(),logger);
				}

				Logutils.log(logUUId, " after calling getAssetByVinAndCan method from assetService", logger,
						"assetUUid: " + asset.getUuid(), " " + stopWatch.prettyPrint());

//		        	AssetsPayload assetResponseDTO = new AssetsPayload();
//		            BeanUtils.copyProperties(asset, assetResponseDTO);
//		            List<AssetsPayload> assetResponseDtoList =new ArrayList();
//		            assetResponseDtoList.add(assetResponseDTO);
//		            return new ResponseEntity<ResponseBodyDTO<List<AssetsPayload>>>(
//							new ResponseBodyDTO<List<AssetsPayload>>(true, "Fetched asset(s) successfully",
//									assetResponseDtoList),HttpStatus.OK);
				return new ResponseEntity<>(asset, HttpStatus.OK);
			} catch (Exception e) {
				logger.error("Exception while getting asset for vin {}", vin, e);
				return null;
			}
		}
		  
		  @GetMapping("/uuid/delete/{assetUuid}")
		    public ResponseEntity<? extends Object> deleteAssetByAssetUuid(@PathVariable("assetUuid") String assetUuid) {
		        try {
		            Boolean isDeleted = assetService.deleteAssetByAssetUuid(assetUuid);
		            return new ResponseEntity<>(isDeleted, HttpStatus.OK);
		        } catch (Exception e) {
		            logger.error("Exception while getting asset for uuid {}", assetUuid, e);
		            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		        }
		    }
	//------------------------------------------------------------Aamir-----------------------------------------------------------------//
	
	@PostMapping()
	public ResponseEntity<MessageDTO<AddAssetResponse>> addAsset(@Validated @RequestBody AssetsPayload assetsPayloads,
			HttpServletRequest httpServletRequest) {
		AddAssetResponse addAssetResponse = null;
		try {
			String revisionType = "Create";
			LOGGER.info("Inside addAsset method from Asset Controller");
			System.out.println("Hello");
			JwtUser jwtUser = (JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			logger.info("Username : " + jwtUser.getUsername());
			addAssetResponse = assetService.addAsset(assetsPayloads, jwtUser.getUsername());
		} catch (ManufacturerNotFoundException e) {
			return new ResponseEntity<MessageDTO<AddAssetResponse>>(
					new MessageDTO<AddAssetResponse>(responseMessageSource
							.getMessage(MessageKeys.MANUFACTURER_IS_INVALID, new Object[] {}, Locale.ENGLISH), false),
					HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (DuplicateVinException e) {
			return new ResponseEntity<MessageDTO<AddAssetResponse>>(
					new MessageDTO<AddAssetResponse>(responseMessageSource.getMessage(MessageKeys.DUPLICATE_VIN,
							new Object[] {}, Locale.ENGLISH), false, "VIN Already in Use"),
					HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (DeviceException e) {
			return new ResponseEntity<MessageDTO<AddAssetResponse>>(new MessageDTO<AddAssetResponse>(
					responseMessageSource.getMessage(e.getMessage(), new Object[] {}, Locale.ENGLISH), false,
					e.getTitle()), HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			return new ResponseEntity<MessageDTO<AddAssetResponse>>(
					new MessageDTO<AddAssetResponse>(e.getMessage(), false), HttpStatus.INTERNAL_SERVER_ERROR);
		}
		boolean status = true;
		HttpStatus httpStatus = HttpStatus.CREATED;
		String message = "Asset added successfully";
		if (addAssetResponse.getErrors() != null && !addAssetResponse.getErrors().isEmpty()) {
			status = false;
			message = "";
		}
		return new ResponseEntity<MessageDTO<AddAssetResponse>>(
				new MessageDTO<AddAssetResponse>(message, addAssetResponse, status), httpStatus);
	}

	@ApiOperation(value = "Get the assets", notes = "API to get the assets", response = AssetVinSearchDTO.class, tags = {
			"Asset Management" })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Fetched asset(s) successfully", response = AssetVinSearchDTO.class),
			@ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 401, message = "Unauthorized"),
			@ApiResponse(code = 403, message = "Forbidden"),
			@ApiResponse(code = 500, message = "Internal Server Error") })
	@GetMapping()
	public ResponseEntity<ResponseBodyDTO<List<AssetResponseDTO>>> getAssets(
			@RequestParam(value = "asset-id", required = false) String assetId,@RequestParam(value = "imei", required = false) String imei) {
		Context context = new Context();
		String methodName = "getAssets";
		List<AssetResponseDTO> assetResponseDtoList = new ArrayList<>();
		try {
			Logutils.log(className, methodName, context.getLogUUId(),
					" Before calling getAssets method from assetService", logger, context.getLogUUId());

			assetResponseDtoList = assetService.getAssetsByAssetId(assetId,imei);

			Logutils.log(className, methodName, context.getLogUUId(),
					" after calling getAssets method from assetService", logger, context.getLogUUId());
			return new ResponseEntity<ResponseBodyDTO<List<AssetResponseDTO>>>(
					new ResponseBodyDTO<List<AssetResponseDTO>>(true, "Fetched asset(s) successfully",
							assetResponseDtoList),
					HttpStatus.OK);
		} catch (Exception exception) {
			logger.error("Exception occurred while getting asset", exception);
			return new ResponseEntity<ResponseBodyDTO<List<AssetResponseDTO>>>(
					new ResponseBodyDTO<List<AssetResponseDTO>>(false, exception.getMessage(), null),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@ApiOperation(value = "Update the assets", notes = "API for update the assets", response = ResponseDTO.class, tags = {
			"Asset Management" })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Asset updated successfully", response = ResponseDTO.class),
			@ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 401, message = "Unauthorized"),
			@ApiResponse(code = 403, message = "Forbidden"),
			@ApiResponse(code = 500, message = "Internal Server Error") })
	@PutMapping()
	public ResponseEntity<MessageDTO<AddAssetResponse>> updateAsset(
			@Validated @RequestBody AssetsPayload assetsPayloads, HttpServletRequest httpServletRequest) {
		logger.info("Received request to update asset detail : " + assetsPayloads.toString());
		JwtUser jwtUser = (JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		logger.info("Username : " + jwtUser.getUsername());
		AddAssetResponse addAssetResponse = null;
		try {

			addAssetResponse = assetService.updateAsset(assetsPayloads, jwtUser.getUsername());
		} catch (ManufacturerNotFoundException e) {
			return new ResponseEntity<MessageDTO<AddAssetResponse>>(
					new MessageDTO<AddAssetResponse>(responseMessageSource
							.getMessage(MessageKeys.MANUFACTURER_IS_INVALID, new Object[] {}, Locale.ENGLISH), false),
					HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (DuplicateVinException e) {
			return new ResponseEntity<MessageDTO<AddAssetResponse>>(
					new MessageDTO<AddAssetResponse>(responseMessageSource.getMessage(MessageKeys.DUPLICATE_VIN,
							new Object[] {}, Locale.ENGLISH), false, "VIN Already in Use"),
					HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (DeviceException e) {
			return new ResponseEntity<MessageDTO<AddAssetResponse>>(new MessageDTO<AddAssetResponse>(
					responseMessageSource.getMessage(e.getMessage(), new Object[] {}, Locale.ENGLISH), false,
					e.getTitle()), HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<MessageDTO<AddAssetResponse>>(
					new MessageDTO<AddAssetResponse>(e.getMessage(), false), HttpStatus.INTERNAL_SERVER_ERROR);
		}

		boolean status = true;
		HttpStatus httpStatus = HttpStatus.CREATED;
		String message = "Asset updated successfully";
		if (addAssetResponse.getErrors() != null && !addAssetResponse.getErrors().isEmpty()) {
			status = false;
			httpStatus = HttpStatus.BAD_REQUEST;
			message = "";
		}
		return new ResponseEntity<MessageDTO<AddAssetResponse>>(
				new MessageDTO<AddAssetResponse>(message, addAssetResponse, status), httpStatus);
	}

	@PostMapping("/summary")
	public ResponseEntity<Object> getCustomerAssetsList(@RequestParam(value = "_page", required = false) Integer page,
			@RequestParam(value = "_type", required = false) String type,
			@RequestParam(value = "_limit", required = false) Integer size,
			@RequestParam(value = "_sort", required = false) String sort,
			@RequestParam(value = "_order", required = false) String order,
			@RequestParam(value = "_filterModelCountFilter", required = false) String filterModelCountFilter,
			@RequestBody Map<String, String> filterValues, HttpServletRequest httpServletRequest) {

		LOGGER.info("Inside getCustomerAssetsList");
		JwtUser jwtUser = (JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		logger.info("Username : " + jwtUser.getUsername());
		MessageDTO<Page<AssetRecordPayload>> messageDto = new MessageDTO<>("Customer Assets Feteched Successfully",
				true);
		Page<AssetRecordPayload> assets = assetService.getCustomerAssetsList(getPageable(page - 1, size, sort, order),
				jwtUser.getUsername(), filterValues, filterModelCountFilter, sort);
		messageDto.setBody(assets);
		messageDto.setTotalKey(assets.getTotalElements());
		messageDto.setCurrentPage(assets.getNumber());
		messageDto.setTotal_pages(assets.getTotalPages());

		return new ResponseEntity(messageDto, HttpStatus.OK);

	}

	@PostMapping("/page")
	public ResponseEntity<Object> getAllAssetCompanyList(@RequestParam(value = "_page", required = false) Integer page,
			@RequestParam(value = "_type", required = false) String type,
			@RequestParam(value = "_limit", required = false) Integer size,
			@RequestParam(value = "_sort", required = false) String sort,
			@RequestParam(value = "_order", required = false) String order,
			@RequestParam(value = "_yearFilter", required = false) String yearFilter,
			@RequestParam(value = "companyId", required = false) Long companyId,
			@RequestBody Map<String, String> filterValues, HttpServletRequest httpServletRequest) {
		System.out.println(yearFilter);
		LOGGER.info("Received request for fetching list based on filter values");
		MessageDTO<Page<AssetResponseDTO>> messageDto = new MessageDTO<>("Users Fetched Successfully", true);
		JwtUser jwtUser = (JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		logger.info("Username : " + jwtUser.getUsername());
		Page<AssetResponseDTO> users = assetService.getAllActiveCustomerOrganisationList(
				getPageable(page - 1, size, sort, order), companyId, jwtUser.getUsername(), filterValues, yearFilter, sort);
		messageDto.setBody(users);
		messageDto.setTotalKey(users.getTotalElements());
		messageDto.setCurrentPage(users.getNumber());
		messageDto.setTotal_pages(users.getTotalPages());
		return new ResponseEntity<>(messageDto, HttpStatus.OK);

	}

	@GetMapping("/getById")
	public ResponseEntity<AssetResponseDTO> getAssetCompanyListById(
			@RequestParam(value = "id", required = true) String uuid, HttpServletRequest httpServletRequest) {
		JwtUser jwtUser = (JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		logger.info("Username : " + jwtUser.getUsername());
		AssetResponseDTO users = assetService.getAssetsById(uuid);
		return new ResponseEntity<>(users, HttpStatus.OK);
	}


	@GetMapping("/getSuperAdmin")
	public List<String> getSuperAdminUserList() {
		LOGGER.info("Inside getSuperAdminUserList");
		try {
			return assetService.getSuperAdminUser();
		} catch (Exception e) {
			logger.error("Exception while fatching user list", e);
			throw new RuntimeException("Exception while fatching user list");
		}

	}

	@PostMapping("/bulk")
	public ResponseEntity<MessageDTO<Map<String, List<String>>>> addAssets(
			@Validated @RequestBody AssetCompany assetsPayloads, HttpServletRequest httpServletRequest) {
//		assetControllerLogger.info("Inside addAssets");
		Map<String, List<String>> failedAssets = null;
		try {
			JwtUser jwtUser = (JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			logger.info("Username : " + jwtUser.getUsername());
			failedAssets = assetService.addAssets(assetsPayloads, jwtUser.getUsername());
		} catch (Exception e) {
			logger.error("Exception occurred in processing uploaded file", e);
			return new ResponseEntity<MessageDTO<Map<String, List<String>>>>(
					new MessageDTO<Map<String, List<String>>>(e.getMessage(), failedAssets, false),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}

		return new ResponseEntity<MessageDTO<Map<String, List<String>>>>(
				new MessageDTO<Map<String, List<String>>>("Assets saved successfully", failedAssets, true),
				HttpStatus.CREATED);
	}

	@PutMapping("/bulk")
	public ResponseEntity<MessageDTO<Map<String, List<String>>>> addModifiedAssets(
			@Validated @RequestBody AssetCompany assetsPayloads, HttpServletRequest httpServletRequest) {
		logger.info("Inside addModifiedAssets");
		JwtUser jwtUser = (JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		logger.info("Username : " + jwtUser.getUsername());
		Map<String, List<String>> failedAssets = null;
		try {
			failedAssets = assetService.addModifiedAssets(assetsPayloads, jwtUser.getUsername());
		} catch (Exception e) {
			logger.error("Exception while updating assets in bulk", e);
			return new ResponseEntity<MessageDTO<Map<String, List<String>>>>(
					new MessageDTO<Map<String, List<String>>>(e.getMessage(), failedAssets, false),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}

		return new ResponseEntity<MessageDTO<Map<String, List<String>>>>(
				new MessageDTO<Map<String, List<String>>>("Assets saved successfully", failedAssets, true),
				HttpStatus.CREATED);
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

		MessageDTO<Page<AssetResponseDTO>> messageDto = new MessageDTO<>("Asset Fetched Successfully", true);

		try {
			JwtUser jwtUser = (JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			logger.info("Username : " + jwtUser.getUsername());
			Page<AssetResponseDTO> users = assetService.getAllActiveCompanyList(getPageable(page, size, sort, order),
					companyId, jwtUser.getUsername(), filterValues, yearFilter);
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

	@PostMapping("/delete")
	public ResponseEntity<MessageDTO<List<String>>> batchDeleteAssets(@RequestBody List<String> assetUuids) {
		logger.info("Inside batchDeleteAssets");
		try {
			List<String> failedAssets = assetService.deleteBatchAssets(assetUuids);
			return new ResponseEntity<MessageDTO<List<String>>>(
					new MessageDTO<List<String>>("Assets Deleted successfully", failedAssets), HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<MessageDTO<List<String>>>(
					new MessageDTO<List<String>>(e.getMessage(), null, false), HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@GetMapping("/overwrite/{companyId}")
	public ResponseEntity<MessageDTO<Boolean>> checkForAssetOverwrite(@PathVariable("companyId") Long companyId) {
		logger.info("Inside checkForAssetOverwrite");
		try {
			Boolean hasExistingAsset = assetService.checkForAssetOverwrite(companyId);
			return new ResponseEntity<MessageDTO<Boolean>>(
					new MessageDTO<Boolean>("checkForAssetOverwrite", hasExistingAsset), HttpStatus.OK);
		} catch (Exception e) {
			logger.error("Exception while checking for asset overwrite", e);
			return new ResponseEntity<MessageDTO<Boolean>>(
					new MessageDTO<Boolean>("Exception while checking for asset overwrite", false),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@PostMapping("uploadAsset")
	public ResponseEntity<MessageDTO<AddAssetResponse>> uploadAsset(
			@Validated @RequestBody List<AssetsPayload> assetsPayloads, HttpServletRequest httpServletRequest) {
		AddAssetResponse addAssetResponse = null;
		try {
			LOGGER.info("Inside addAsset method from Asset Controller");
			System.out.println("Hello");
			JwtUser jwtUser = (JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			logger.info("Username : " + jwtUser.getUsername());
			for (AssetsPayload assetsPayload : assetsPayloads) {
				addAssetResponse = assetService.addAsset(assetsPayload, jwtUser.getUsername());
			}

		} catch (ManufacturerNotFoundException e) {
			return new ResponseEntity<MessageDTO<AddAssetResponse>>(
					new MessageDTO<AddAssetResponse>(responseMessageSource
							.getMessage(MessageKeys.MANUFACTURER_IS_INVALID, new Object[] {}, Locale.ENGLISH), false),
					HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (DuplicateVinException e) {
			return new ResponseEntity<MessageDTO<AddAssetResponse>>(
					new MessageDTO<AddAssetResponse>(responseMessageSource.getMessage(MessageKeys.DUPLICATE_VIN,
							new Object[] {}, Locale.ENGLISH), false, "VIN Already in Use"),
					HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (DeviceException e) {
			return new ResponseEntity<MessageDTO<AddAssetResponse>>(new MessageDTO<AddAssetResponse>(
					responseMessageSource.getMessage(e.getMessage(), new Object[] {}, Locale.ENGLISH), false,
					e.getTitle()), HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			return new ResponseEntity<MessageDTO<AddAssetResponse>>(
					new MessageDTO<AddAssetResponse>(e.getMessage(), false), HttpStatus.INTERNAL_SERVER_ERROR);
		}
		boolean status = true;
		HttpStatus httpStatus = HttpStatus.CREATED;
		String message = "Asset added successfully";
		if (addAssetResponse.getErrors() != null && !addAssetResponse.getErrors().isEmpty()) {
			status = false;
			message = "";
		}
		return new ResponseEntity<MessageDTO<AddAssetResponse>>(
				new MessageDTO<AddAssetResponse>(message, addAssetResponse, status), httpStatus);
	}

	@PostMapping("asset_association")
	public ResponseEntity<MessageDTO> assetAssociation(
			@Validated @RequestBody AssetAssociationPayload assetAssociationPayload,
			HttpServletRequest httpServletRequest) {
		try {
			LOGGER.info("Inside assetAssociation method from Asset Controller");
			JwtUser jwtUser = (JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			logger.info("Username : " + jwtUser.getUsername());
			MessageDTO message = assetService.uploadAssetAssociation(assetAssociationPayload);
			return new ResponseEntity<MessageDTO>(message, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<MessageDTO>(new MessageDTO(e.getMessage(), false),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	
	 @GetMapping("/pagination")
	    public ResponseEntity<Object> getAssetsWithPagination(@RequestParam(value = "can", required = false) String accountNumber,
	                                                                             @RequestParam(value = "vin", required = false) String vin,
	                                                                             @RequestParam(value = "assigned-name", required = false) String assignedName,
	                                                                             @RequestParam(value = "status", required = false) String status,
	                                                                             @RequestParam(value = "eligible-gateway", required = false) String eligibleGateway,
	                                                                             @RequestParam(value = "_page", required = true) Integer page,
	                                                                 			 @RequestParam(value = "_limit", required = true) Integer size,
	                                                                 			 @RequestParam(value = "_sort", required = false) String sort, 
	                                                                 			 @RequestParam(value = "_order", required = false) String order) {
	    	  Context context = new Context();
	    	   String methodName = "getAssets";
	    	try {
	    		Logutils.log(className,methodName,context.getLogUUId()," Before calling getAssets method from assetService",logger, context.getLogUUId(), accountNumber);
	    		MessageDTO<Page<AssetResponseDTO>> messageDto = new MessageDTO<>("Fetched asset(s) successfully", true);
	    		assetService.getAssets(accountNumber, vin, assignedName, status, eligibleGateway,context, getPageable(page - 1, size, sort, order), messageDto);
	            Logutils.log(className,methodName,context.getLogUUId()," after calling getAssets method from assetService",logger, context.getLogUUId(), accountNumber);
	            return new ResponseEntity(messageDto, HttpStatus.OK);
	        } catch (Exception exception) {
	            logger.error("Exception occurred while getting asset", exception);
	            return new ResponseEntity<Object>(
						new ResponseBodyDTO<AssetResponseDTO>(false, exception.getMessage(), null), HttpStatus.INTERNAL_SERVER_ERROR);
	        }
	    }
	
}