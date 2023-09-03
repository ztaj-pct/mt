package com.pct.installer.feign.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.pct.common.dto.UserDTO;
import com.pct.common.model.User;
import com.pct.common.util.JwtUser;
import com.pct.installer.dto.MessageDTO;
import com.pct.installer.feign.config.DeviceServiceFeignConfig;

@FeignClient(name = "auth-service", configuration = DeviceServiceFeignConfig.class)
public interface AuthServiceClient {

	@GetMapping("/user/get/{id}")
	public ResponseEntity<MessageDTO<UserDTO>> getUserFromAuthService(@PathVariable("id") Long userId);
	
	@GetMapping(value = "/user/username")
	public ResponseEntity<UserDTO> getUserFromAuthServiceByName(@RequestParam("user_name") String userName); 

}
