package com.pct.device.controller;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
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
import com.pct.common.model.Asset;
import com.pct.common.util.JwtUser;
import com.pct.device.dto.AssetToDeviceDTO;
import com.pct.device.dto.MessageDTO;
import com.pct.device.dto.RyderApiDTO;
import com.pct.device.dto.RyderApiPayload;
import com.pct.device.exception.DeviceException;
import com.pct.device.payload.AddAssetResponse;
import com.pct.device.payload.AssestAssociationRequest;
import com.pct.device.payload.AssestDissociationRequest;
import com.pct.device.payload.AssestReassignmentRequest;
import com.pct.device.service.IReportBuilderService;

import io.swagger.annotations.Api;

@RestController
@RequestMapping("/reportbuilder")
@Api(value = "/reportbuilder", tags = "Report Builder Management")
public class ReportBuilderController implements IApplicationController<Asset> {
	public static final String className = "ReportBuilderController";
	private static final Logger LOGGER = LoggerFactory.getLogger(ReportBuilderController.class);

	@Autowired
	private IReportBuilderService iReportBuilderService;

	@Autowired
	private MessageSource responseMessageSource;

	@PostMapping(value = "/assign")
	public ResponseEntity<MessageDTO<AddAssetResponse>> assetAssociation(
			@RequestBody @Validated AssestAssociationRequest assestAssociationRequest,
			HttpServletRequest httpServletRequest) {
		AddAssetResponse addAssetResponse = null;
		String msgUuid = UUID.randomUUID().toString();
		try {
			LOGGER.info("MsgUuid: " + msgUuid + " Inside ReportBuilderController > assetAssociation method");
			JwtUser jwtUser = (JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			LOGGER.info("MsgUuid: " + msgUuid + " Username : " + jwtUser.getUsername());
			addAssetResponse = iReportBuilderService.assestAssociation(assestAssociationRequest,
					jwtUser.getAccountNumber(), msgUuid);
			;
		} catch (DeviceException exception) {
			LOGGER.error("MsgUuid: " + msgUuid + " Validation Error: " + exception.getMessage());
			return new ResponseEntity<MessageDTO<AddAssetResponse>>(
					new MessageDTO<AddAssetResponse>(exception.getMessage(), addAssetResponse, false), HttpStatus.OK);
		} catch (Exception e) {
			LOGGER.error("MsgUuid: " + msgUuid + " Error occured during assign of asset to device " + e.getMessage());
			return new ResponseEntity<MessageDTO<AddAssetResponse>>(
					new MessageDTO<AddAssetResponse>(
							responseMessageSource.getMessage(e.getMessage(), new Object[] {}, Locale.ENGLISH), false),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}

		boolean status = true;
		HttpStatus httpStatus = HttpStatus.CREATED;
		String message = "Successfully assosiated asset with device.";
		if (addAssetResponse != null && addAssetResponse.getErrors() != null
				&& !addAssetResponse.getErrors().isEmpty()) {
			status = false;
			message = "";
		}
		LOGGER.info("MsgUuid: " + msgUuid + " Successfully assosiated asset with device.");
		return new ResponseEntity<MessageDTO<AddAssetResponse>>(
				new MessageDTO<AddAssetResponse>(message, addAssetResponse, status), httpStatus);
	}

	@PostMapping(value = "/reassign")
	public ResponseEntity<MessageDTO<AddAssetResponse>> assetReassignment(
			@RequestBody @Validated AssestReassignmentRequest assetReassignmentRequest) {
		AddAssetResponse addAssetResponse = null;
		String msgUuid = UUID.randomUUID().toString();
		try {
			LOGGER.info("MsgUuid: " + msgUuid + " Inside ReportBuilderController > assetReassignment method");
			JwtUser jwtUser = (JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			logger.info("MsgUuid: " + msgUuid + " Username : " + jwtUser.getUsername());
			addAssetResponse = iReportBuilderService.assetReassignment(assetReassignmentRequest,
					jwtUser.getAccountNumber(), msgUuid);

		} catch (DeviceException exception) {
			LOGGER.error("MsgUuid: " + msgUuid + " Validation Error: " + exception.getMessage());
			return new ResponseEntity<MessageDTO<AddAssetResponse>>(
					new MessageDTO<AddAssetResponse>(exception.getMessage(), addAssetResponse, false), HttpStatus.OK);
		} catch (Exception e) {
			LOGGER.error("MsgUuid: " + msgUuid + " Error occured during assign of asset to device " + e.getMessage());
			return new ResponseEntity<MessageDTO<AddAssetResponse>>(
					new MessageDTO<AddAssetResponse>(
							responseMessageSource.getMessage(e.getMessage(), new Object[] {}, Locale.ENGLISH), false),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}

		boolean status = true;
		HttpStatus httpStatus = HttpStatus.CREATED;
		String message = "Successfully asset reassignment with device.";
		if (addAssetResponse != null && addAssetResponse.getErrors() != null
				&& !addAssetResponse.getErrors().isEmpty()) {
			status = false;
			message = "";
		}
		LOGGER.info("MsgUuid: " + msgUuid + " Successfully asset reassignment with device ");
		return new ResponseEntity<MessageDTO<AddAssetResponse>>(
				new MessageDTO<AddAssetResponse>(message, addAssetResponse, status), httpStatus);

	}

	@PostMapping(value = "/unassign")
	public ResponseEntity<MessageDTO<AddAssetResponse>> assetDissociation(
			@RequestBody @Validated AssestDissociationRequest assetDissociationRequest) {
		AddAssetResponse addAssetResponse = null;
		String msgUuid = UUID.randomUUID().toString();
		try {
			LOGGER.info("MsgUuid: " + msgUuid + " Inside ReportBuilderController > assetDissociation method");
			JwtUser jwtUser = (JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			LOGGER.info("MsgUuid: " + msgUuid + " Username : " + jwtUser.getUsername());
			addAssetResponse = iReportBuilderService.assetDissociation(assetDissociationRequest,
					jwtUser.getAccountNumber(), msgUuid);
		} catch (DeviceException exception) {
			LOGGER.error("MsgUuid: " + msgUuid + " Validation Error: " + exception.getMessage());
			return new ResponseEntity<MessageDTO<AddAssetResponse>>(
					new MessageDTO<AddAssetResponse>(exception.getMessage(), addAssetResponse, false), HttpStatus.OK);
		} catch (Exception e) {
			LOGGER.error("MsgUuid: " + msgUuid + " Error occured during assign of asset to device " + e.getMessage());
			return new ResponseEntity<MessageDTO<AddAssetResponse>>(
					new MessageDTO<AddAssetResponse>(
							responseMessageSource.getMessage(e.getMessage(), new Object[] {}, Locale.ENGLISH), false),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
		boolean status = true;
		HttpStatus httpStatus = HttpStatus.CREATED;
		String message = "Successfully asset unassigned with device.";
		if (addAssetResponse != null && addAssetResponse.getErrors() != null
				&& !addAssetResponse.getErrors().isEmpty()) {
			status = false;
			message = "";
		}
		LOGGER.info("MsgUuid: " + msgUuid + " Successfully asset unassigned with device ");
		return new ResponseEntity<MessageDTO<AddAssetResponse>>(
				new MessageDTO<AddAssetResponse>(message, addAssetResponse, status), httpStatus);

	}

	@GetMapping(value = "/byId")
	public ResponseEntity<MessageDTO<AssetToDeviceDTO>> findAssetAssociationDetails(
			@RequestParam(value = "asset_id", required = true) String assetId) {

		String msgUuid = UUID.randomUUID().toString();
		try {
			LOGGER.info("MsgUuid: " + msgUuid
					+ " Inside ReportBuilderController > findAssetAssociationDetails get method ");
			JwtUser jwtUser = (JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			LOGGER.info("MsgUuid: " + msgUuid + " Username : " + jwtUser.getUsername());
			AssetToDeviceDTO assetToDeviceDTO = iReportBuilderService.getAssetAssociationDetails(assetId,
					jwtUser.getAccountNumber(), msgUuid);

			boolean status = true;
			HttpStatus httpStatus = HttpStatus.CREATED;
			String message = "";
			if (assetToDeviceDTO.getGateway_id() == null || assetToDeviceDTO.getGateway_id().isEmpty()) {
				message = "GatewayId not found for AssetId " + assetId;

			} else {
				message = "GatewayId  found for AssetId " + assetId;
			}
			LOGGER.info("MsgUuid: " + msgUuid + " Successfully found association for asset id " + assetId);
			return new ResponseEntity<MessageDTO<AssetToDeviceDTO>>(
					new MessageDTO<AssetToDeviceDTO>(message, assetToDeviceDTO, status), httpStatus);

		} catch (Exception e) {
			LOGGER.error("MsgUuid: " + msgUuid + " Error occured during assign of asset to device " + e.getMessage());
			return new ResponseEntity<MessageDTO<AssetToDeviceDTO>>(
					new MessageDTO<AssetToDeviceDTO>(
							responseMessageSource.getMessage(e.getMessage(), new Object[] {}, Locale.ENGLISH), false),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@PostMapping(value = "/getAssetData")
	public ResponseEntity<MessageDTO<List<RyderApiDTO>>> ryderApi(
			@RequestBody @Validated RyderApiPayload ryderApiPayload) {
		String msgUuid = UUID.randomUUID().toString();
		LOGGER.info("msgUuid: " + msgUuid + " Inside ReportBuilderController > ryderApi get method");
		try {
			List<RyderApiDTO> listRyderApiDTO = iReportBuilderService.ryderApi(ryderApiPayload.getAssetName(),
					ryderApiPayload.getImei(), ryderApiPayload.getImei_last5(), ryderApiPayload.getVin(), msgUuid);
			boolean status = true;
			HttpStatus httpStatus = HttpStatus.OK;
			String message = "";
			if (listRyderApiDTO == null || listRyderApiDTO.isEmpty() || listRyderApiDTO.size() == 0) {
				message = "No asset data found";

			} else {
				message = "Asset data found successfully";
			}
			return new ResponseEntity<MessageDTO<List<RyderApiDTO>>>(
					new MessageDTO<List<RyderApiDTO>>(message, listRyderApiDTO, status), httpStatus);

		} catch (Exception e) {
			LOGGER.error("msgUuid: " + msgUuid + " Exception occurred in ryder api " + e.getMessage());
			return new ResponseEntity<MessageDTO<List<RyderApiDTO>>>(
					new MessageDTO<List<RyderApiDTO>>(
							responseMessageSource.getMessage(e.getMessage(), new Object[] {}, Locale.ENGLISH), false),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@GetMapping(value = "/mail-report")
	public ResponseEntity<MessageDTO<Map<String, Object>>> mailAssetAssociationReport() {
		String msgUuid = UUID.randomUUID().toString();
		try {
			LOGGER.info("msgUuid: " + msgUuid + "Inside ReportBuilderController > mail-report method");
			Map<String, Object> map = iReportBuilderService.mailReport();
			boolean status = true;
			HttpStatus httpStatus = HttpStatus.CREATED;
			String message = "Mail Successful";
			
			LOGGER.info("MsgUuid: " + msgUuid + "Mail data get Successful");
			return new ResponseEntity<MessageDTO<Map<String, Object>>>(
					new MessageDTO<Map<String, Object>>(message, map, status), httpStatus);
		
		} catch (Exception exception) {
			LOGGER.error("msgUuid: " + msgUuid + " Exception occurred in mail-report method " + exception.getMessage());
			return new ResponseEntity<MessageDTO<Map<String, Object>>>(
					new MessageDTO<Map<String, Object>>(
							responseMessageSource.getMessage(exception.getMessage(), new Object[] {}, Locale.ENGLISH), false),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

}