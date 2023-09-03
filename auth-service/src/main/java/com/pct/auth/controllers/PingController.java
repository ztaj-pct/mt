package com.pct.auth.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class PingController {

	@CrossOrigin(origins = "*")
	@GetMapping("/ping")
	public ResponseEntity<String> ping() throws Exception {
		return ResponseEntity.ok("Ping Successfull, version- 1.0.1");
	}
}
