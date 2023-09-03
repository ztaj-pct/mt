package com.pct.device.service;

import java.util.List;

import com.pct.device.model.Event;


public interface IEventService {

	List<Event> getAllEvent() throws Exception;
	
	
	boolean addEvent(Event event) throws Exception;
}