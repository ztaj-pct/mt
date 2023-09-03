package com.pct.device.feing.client;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.pct.common.dto.ResponseBodyDTO;
import com.pct.common.model.SensorInstallInstruction;
import com.pct.common.model.SensorReasonCode;
import com.pct.device.feing.config.InstallerServiceFeignConfig;

@FeignClient(name = "installer-service", configuration = InstallerServiceFeignConfig.class)
public interface InstallerServiceFeignClient {

	 @GetMapping("/installation/get-sensor-install-instruction")
	 public ResponseEntity<ResponseBodyDTO<List<SensorInstallInstruction>>> getSensorInstallInstruction(@RequestParam(value = "product-name", required = true) String productName);
	 
	 @GetMapping("/installation/get-sensor-reason-code")
	  public ResponseEntity<ResponseBodyDTO<List<SensorReasonCode>>> getSensorReasonCode(@RequestParam(value = "product-name", required = true) String productName);
	
}
