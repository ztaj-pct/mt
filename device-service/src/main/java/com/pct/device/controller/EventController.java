package com.pct.device.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pct.common.dto.ResponseDTO;
import com.pct.device.model.Event;
import com.pct.common.util.JwtUser;
import com.pct.device.dto.ResponseBodyDTO;
import com.pct.device.service.IEventService;

@RestController
@RequestMapping("/event")
@CrossOrigin("*")
public class EventController {

	@Autowired
	private IEventService eventService;

	private static final Logger logger = LoggerFactory.getLogger(EventController.class);

	@GetMapping()
	public ResponseEntity<ResponseBodyDTO<List<Event>>> getEventList(HttpServletRequest httpServletRequest) {
		logger.info("Before getting response from getEventList method from event controller");
		try {
			List<Event> eventMasterList = eventService.getAllEvent();
			return new ResponseEntity<ResponseBodyDTO<List<Event>>>(
					new ResponseBodyDTO<List<Event>>(true, "Fetched Event  List(s) successfully", eventMasterList),
					HttpStatus.OK);
		} catch (Exception exception) {
			logger.error("Exception occurred while getting event", exception);
			return new ResponseEntity<ResponseBodyDTO<List<Event>>>(
					new ResponseBodyDTO<List<Event>>(false, exception.getMessage(), null),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PostMapping()
	public ResponseEntity<ResponseDTO> addEvent(@Valid @RequestBody Event event) throws Exception {
		Boolean status = false;
		try {
			logger.info("Before getting response from addEvent method from event controller");
			status = eventService.addEvent(event);
		} catch (Exception e) {
			logger.error("Exception occurred in adding of event detail", e);
			return new ResponseEntity<ResponseDTO>(new ResponseDTO(status, e.getMessage()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<ResponseDTO>(new ResponseDTO(status, "Event details saved successfully"),
				HttpStatus.CREATED);
	}

}