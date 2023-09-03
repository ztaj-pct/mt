package com.pct.device.controller;

import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pct.common.controller.IApplicationController;
import com.pct.common.dto.ResponseBodyDTO;
import com.pct.common.dto.ResponseDTO;
import com.pct.common.model.Device;
import com.pct.device.Bean.ProductionToolBean;
import com.pct.device.Bean.ProductionToolFetchBean;
import com.pct.device.Bean.ProductionToolResponseBean;
import com.pct.device.dto.DeviceListDTO;
import com.pct.device.dto.MessageDTO;
import com.pct.device.service.IProductionToolService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@RequestMapping("/production_tool")
@Api(value = "/production_tool", tags = "Production Tool")
public class ProductionToolController implements IApplicationController<Device> {

	@Autowired
	private IProductionToolService productionToolService;

	Logger logger = LoggerFactory.getLogger(ProductionToolController.class);

	@ApiOperation(value = "Save production tool data", notes = "API to save production tool data", response = String.class, tags = {
			"Production Tool" })
	@ApiResponses(value = { @ApiResponse(code = 201, message = "Added Successfully", response = String.class),
			@ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 401, message = "Unauthorized"),
			@ApiResponse(code = 403, message = "Forbidden"),
			@ApiResponse(code = 500, message = "Internal Server Error") })
	@PostMapping("/save")
	public ResponseEntity<ResponseDTO> saveProductionToolData(
			@Valid @RequestBody ProductionToolBean productionToolBean) {
		String msgUuid = UUID.randomUUID().toString();
		try {
			StopWatch stopWatch = new StopWatch();
			stopWatch.start();

			logger.info("msgUuid : " + msgUuid
					+ "Before getting response from saveProductionToolData method from production tool controller :"
					+ stopWatch.prettyPrint());
			if ((productionToolBean.getToolName() == null)
					|| (!productionToolBean.getToolName().trim().equalsIgnoreCase("BAT-Test"))
							&& (!productionToolBean.getToolName().trim().equalsIgnoreCase("Spare-Tool"))
							&& (!productionToolBean.getToolName().trim().equalsIgnoreCase("Tire-Tool"))
							&& (!productionToolBean.getToolName().trim().equalsIgnoreCase("Lock-Tool"))
							&& (!productionToolBean.getToolName().trim().equalsIgnoreCase("Free-Tool"))) {
				return new ResponseEntity<ResponseDTO>(new ResponseDTO(false,
						"Tool name is null or not valid please try again valid tool names are Spare-Tool,BAT-Test ,Tire-Tool,Lock-Tool and Free-Tool"),
						HttpStatus.BAD_REQUEST);

			}

			if (productionToolBean.getEventContent() == null) {
				return new ResponseEntity<ResponseDTO>(new ResponseDTO(false, "Event content can not be null"),
						HttpStatus.BAD_REQUEST);
			}
			productionToolService.saveProductionToolData(productionToolBean, msgUuid);
			logger.info("msgUuid : " + msgUuid
					+ "After getting response from saveProductionToolData method from production tool controller :"
					+ stopWatch.prettyPrint());
			return new ResponseEntity<ResponseDTO>(new ResponseDTO(true, "Prodution tool data successfully added"),
					HttpStatus.OK);
		} catch (Exception exception) {
			logger.error("msgUuid : " + msgUuid + "Exception while adding prodution tool data", exception);
			return new ResponseEntity<ResponseDTO>(new ResponseDTO(false, exception.getMessage()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@ApiOperation(value = "Find production tool data", notes = "API to find production tool data", response = String.class, tags = {
			"Production Tool" })
	@ApiResponses(value = { @ApiResponse(code = 201, message = "Find Successfully", response = String.class),
			@ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 401, message = "Unauthorized"),
			@ApiResponse(code = 403, message = "Forbidden"),
			@ApiResponse(code = 500, message = "Internal Server Error") })
	@PostMapping("/find")
	public ResponseEntity<Object> getProductionToolDataPagination(
			@RequestBody ProductionToolFetchBean productionToolFetchBean) {
		String msgUuid = UUID.randomUUID().toString();
		try {
			StopWatch stopWatch = new StopWatch();
			stopWatch.start();
			logger.info("msgUuid : " + msgUuid
					+ "Before getting response from getProductionToolDataPagination method from production tool controller :"
					+ stopWatch.prettyPrint());
			MessageDTO<Page<ProductionToolResponseBean>> messageDto = new MessageDTO<>(
					"Fetched Production Tool(s) Successfully", true);
			Page<ProductionToolResponseBean> productionTool = productionToolService.getProductionToolDataPagination(
					productionToolFetchBean.getStartDate(), productionToolFetchBean.getEndDate(), productionToolFetchBean.getToolName(), getPageable(productionToolFetchBean.get_page() - 1, productionToolFetchBean.get_limit(), productionToolFetchBean.get_sort(), productionToolFetchBean.get_order()), msgUuid,
					productionToolFetchBean.getQueryParam(),productionToolFetchBean.getQueryParamValue());
			messageDto.setBody(productionTool);
			messageDto.setTotalKey(productionTool.getTotalElements());
			logger.info("msgUuid : " + msgUuid + "Total Element ==== " + productionTool.getTotalElements());
			logger.info("msgUuid : " + msgUuid + "Current Page " + productionTool.getNumber());
			logger.info("msgUuid : " + msgUuid + "Total Pages" + productionTool.getTotalPages());
			stopWatch.stop();
			logger.info("msgUuid : " + msgUuid
					+ "After getting response from getProductionToolDataPagination method from device controller :"
					+ stopWatch.prettyPrint());
			return new ResponseEntity<>(messageDto, HttpStatus.OK);
		} catch (Exception exception) {
			logger.error("msgUuid : " + msgUuid + "Exception occurred while getting Production Tool(s)", exception);
			return new ResponseEntity<Object>(new ResponseBodyDTO<DeviceListDTO>(false, exception.getMessage(), null),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

}
