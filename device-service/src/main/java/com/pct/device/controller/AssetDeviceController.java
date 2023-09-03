package com.pct.device.controller;

import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pct.common.controller.IApplicationController;
import com.pct.common.dto.ResponseBodyDTO;
import com.pct.common.model.Asset_Device_xref;
import com.pct.common.util.JwtUser;
import com.pct.device.payload.AssetDeviceAssociationPayLoad;
import com.pct.device.payload.AssetDeviceAssociationPayLoadForIA;
import com.pct.device.payload.InstallationResponse;
import com.pct.device.service.impl.AssetDeviceServiceImpl;

import io.swagger.annotations.Api;

@RestController
@RequestMapping("/asset-device")
@Api(value = "/asset-device", tags = "Asset Device Management")
public class AssetDeviceController implements IApplicationController<Asset_Device_xref> {
	public static final String className = "AssetDeviceController";
	private static final Logger LOGGER = LoggerFactory.getLogger(AssetDeviceController.class);

	@Autowired
	AssetDeviceServiceImpl assetDeviceServiceImpl;

	@PostMapping("/findByVinAssetDevice")
	public Asset_Device_xref addAssetDeviceAssociation(
			@Validated @RequestBody AssetDeviceAssociationPayLoad assetDeviceAssociationPayLoad,
			HttpServletRequest httpServletRequest) {

		logger.info("Before addAssetDeviceAssociation method in AssetDeviceController controller :");
		JwtUser jwtUser = (JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Asset_Device_xref assetDeviceXrf = null;
		try {
			assetDeviceXrf = assetDeviceServiceImpl.addAssetDeviceAssociation(assetDeviceAssociationPayLoad,
					jwtUser.getUsername());
			logger.info("After addAssetDeviceAssociation method in AssetDeviceController controller :");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return assetDeviceXrf;
	}

	@GetMapping("/getAssetDeviceAssociation")
	public Asset_Device_xref getAssetDeviceAssociation(
			@RequestParam(value = "device-id", required = false) String deviceId,
			HttpServletRequest httpServletRequest) {

		logger.info("Before getAssetDeviceAssociation method in AssetDeviceController controller :");
		JwtUser jwtUser = (JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Asset_Device_xref assetDeviceXrf = null;
		try {
			assetDeviceXrf = assetDeviceServiceImpl.getAssetDeviceAssociation(deviceId, jwtUser.getUsername());
			logger.info("After getAssetDeviceAssociation method in AssetDeviceController controller :");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return assetDeviceXrf;
	}

	@PostMapping("/asset-device-history")
	public List<Asset_Device_xref> AssetDeviceHistory(
			@RequestParam(value = "device-id", required = false) String deviceId,
			HttpServletRequest httpServletRequest) {

		logger.info("Before getAssetDeviceAssociation method in AssetDeviceController controller :");
		JwtUser jwtUser = (JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		List<Asset_Device_xref> assetDeviceXrf = null;
		try {
			assetDeviceXrf = assetDeviceServiceImpl.getAssetDeviceHistory(deviceId, jwtUser.getUsername());
			logger.info("After getAssetDeviceAssociation method in AssetDeviceController controller :");
		} catch (Exception e) {
			e.printStackTrace();
		}

		return assetDeviceXrf;
	}
	
	@PostMapping("/asset-device-association")
	public ResponseEntity<Object> createAssetDeviceAssociationForIA(
			@Validated @RequestBody AssetDeviceAssociationPayLoadForIA assetDeviceAssociationPayLoadForIA,
			HttpServletRequest httpServletRequest) {
		HttpStatus httpStatus = null;
		logger.info("Before createAssetDeviceAssociationForIA method in AssetDeviceController controller :");
		JwtUser jwtUser = (JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Asset_Device_xref assetDeviceXrf = null;
		try {
			assetDeviceXrf = assetDeviceServiceImpl.createAssetDeviceAssociationForIA(assetDeviceAssociationPayLoadForIA,
					jwtUser.getUsername());
			logger.info("After createAssetDeviceAssociationForIA method in AssetDeviceController controller :");
			httpStatus = HttpStatus.OK;
		} catch (Exception e) {
			httpStatus = HttpStatus.BAD_REQUEST;
			e.printStackTrace();
		}
		return new ResponseEntity<>(/*assetDeviceXrf*/ new HashMap<>(), httpStatus);
	}
	
	@GetMapping("lookup")
	public ResponseEntity<ResponseBodyDTO<List<InstallationResponse>>> getInstallation(
			@RequestParam(value = "assetId", required = false) String assetId,
			@RequestParam(value = "imei", required = false) String imei,
			HttpServletRequest httpServletRequest) {

		logger.info("Before getTraillerLookup method in AssetDeviceController controller :");
		List<InstallationResponse> intaInstallationResponses = null;
		try {
			intaInstallationResponses = assetDeviceServiceImpl.getTraillerLookup(assetId, imei);
			logger.info("After getTraillerLookup method in AssetDeviceController controller :");
		} catch (Exception e) {
			logger.error("Exception occurred at while getting asset device traler lookup data", e);
			return new ResponseEntity<ResponseBodyDTO<List<InstallationResponse>>>(new ResponseBodyDTO<List<InstallationResponse>>(false, e.getMessage()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<>(new ResponseBodyDTO<>(true, "Fetched Asset Device Trailer LookUp Data Successfully", intaInstallationResponses),
				HttpStatus.OK);
	}

}
