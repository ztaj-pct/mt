package com.pct.installer.controller;

import com.pct.common.dto.ResponseBodyDTO;
import com.pct.common.util.Context;
import com.pct.installer.dto.CustomerInventoryDTO;
import com.pct.installer.exception.InstallerException;
import com.pct.installer.service.IInstallerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Abhishek on 02/06/20
 */

@RestController
@RequestMapping("/installer")
public class InstallerController {

    Logger logger = LoggerFactory.getLogger(InstallerController.class);

    @Autowired
    private IInstallerService installerService;

    @GetMapping("/inventory")
    public ResponseEntity<ResponseBodyDTO<CustomerInventoryDTO>> getInventoryForCustomer(@RequestParam(value = "can", required = false) String accountNumber) {
        try {
            logger.info("Inside getInventoryForCustomer Method");
            StopWatch stopWatch  = new StopWatch();
    		stopWatch.start("findBy ModuleId and plantCode");
    		logger.info(stopWatch.prettyPrint());
    		Context context = new Context();
			String logUUId = context.getLogUUId();
    		logger.info("===================================== TIme Start ===============================");
            CustomerInventoryDTO customerInventoryDTO = installerService.inventoryForCustomer(logUUId, accountNumber);
            stopWatch.stop();
            logger.info("===================================== TIme End ===============================");
            logger.info(stopWatch.prettyPrint());
            String message = "Successfully fetched Inventory for Customer";
            if (customerInventoryDTO.getInventoryList() != null && !customerInventoryDTO.getInventoryList().isEmpty()) {
                if ((customerInventoryDTO.getInventoryList().get(0).getAssets() == null ||
                        customerInventoryDTO.getInventoryList().get(0).getAssets().isEmpty()) &&
                        (customerInventoryDTO.getInventoryList().get(0).getGateways() == null ||
                                customerInventoryDTO.getInventoryList().get(0).getGateways().isEmpty())) {
                    message = "No assets and devices available for this customer.";
                } else if (customerInventoryDTO.getInventoryList().get(0).getAssets() == null ||
                        customerInventoryDTO.getInventoryList().get(0).getAssets().isEmpty()) {
                    message = "No assets available for this customer.";
                } else if (customerInventoryDTO.getInventoryList().get(0).getGateways() == null ||
                        customerInventoryDTO.getInventoryList().get(0).getGateways().isEmpty()) {
                    message = "No devices available for this customer.";
                }
            }
            return new ResponseEntity<>(
                    new ResponseBodyDTO<CustomerInventoryDTO>(true, message, customerInventoryDTO),
                    HttpStatus.OK);
        } catch (InstallerException exception) {
            logger.error("Exception occurred while getting inventory for Customer", exception);
            return new ResponseEntity<ResponseBodyDTO<CustomerInventoryDTO>>(
                    new ResponseBodyDTO<CustomerInventoryDTO>(false, exception.getMessage(), null), HttpStatus.INTERNAL_SERVER_ERROR);
         } catch (Exception exception) {
            logger.error("Exception occurred while getting inventory for Customer", exception);
            return new ResponseEntity<ResponseBodyDTO<CustomerInventoryDTO>>(
                    new ResponseBodyDTO<CustomerInventoryDTO>(false, exception.getMessage(), null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @GetMapping("/inventory/new")
    public ResponseEntity<ResponseBodyDTO<CustomerInventoryDTO>> getInventoryForCustomerWithImprovement(@RequestParam(value = "can", required = false) String accountNumber) {
        try {
        	Context ctx = new Context();
        	String logUUid=ctx.getLogUUId();
            logger.info("Inside getInventoryForCustomerWithImprovement Method");
            StopWatch stopWatch  = new StopWatch();
    		stopWatch.start("findBy ModuleId and plantCode");
    		logger.info(stopWatch.prettyPrint());
    		logger.info("===================================== TIme Start ===============================");
            CustomerInventoryDTO customerInventoryDTO = installerService.getInventoryForCustomerWithImprovement(logUUid, accountNumber);
            stopWatch.stop();
            logger.info("===================================== TIme End ===============================");
            logger.info(stopWatch.prettyPrint());
            String message = "Successfully fetched Inventory for Customer";
            if (customerInventoryDTO.getInventoryList() != null && !customerInventoryDTO.getInventoryList().isEmpty()) {
                if ((customerInventoryDTO.getInventoryList().get(0).getAssets() == null ||
                        customerInventoryDTO.getInventoryList().get(0).getAssets().isEmpty()) &&
                        (customerInventoryDTO.getInventoryList().get(0).getGateways() == null ||
                                customerInventoryDTO.getInventoryList().get(0).getGateways().isEmpty())) {
                    message = "No assets and devices available for this customer.";
                } else if (customerInventoryDTO.getInventoryList().get(0).getAssets() == null ||
                        customerInventoryDTO.getInventoryList().get(0).getAssets().isEmpty()) {
                    message = "No assets available for this customer.";
                } else if (customerInventoryDTO.getInventoryList().get(0).getGateways() == null ||
                        customerInventoryDTO.getInventoryList().get(0).getGateways().isEmpty()) {
                    message = "No devices available for this customer.";
                }
            }
            return new ResponseEntity<>(
                    new ResponseBodyDTO<CustomerInventoryDTO>(true, message, customerInventoryDTO),
                    HttpStatus.OK);
        } catch (InstallerException exception) {
            logger.error("Exception occurred while getting inventory for Customer", exception);
            return new ResponseEntity<ResponseBodyDTO<CustomerInventoryDTO>>(
                    new ResponseBodyDTO<CustomerInventoryDTO>(false, exception.getMessage(), null), HttpStatus.INTERNAL_SERVER_ERROR);
         } catch (Exception exception) {
            logger.error("Exception occurred while getting inventory for Customer", exception);
            return new ResponseEntity<ResponseBodyDTO<CustomerInventoryDTO>>(
                    new ResponseBodyDTO<CustomerInventoryDTO>(false, exception.getMessage(), null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
