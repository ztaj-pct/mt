package com.pct.device.service.impl;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pct.device.model.Event;
import com.pct.device.repository.EventRepository;
import com.pct.device.repository.IDeviceRepository;
import com.pct.device.service.IEventService;

@Service
public class EventServiceImpl implements IEventService {

	private static final Logger LOGGER = LoggerFactory.getLogger(EventServiceImpl.class);
	@Autowired
	EventRepository eventRepository;
	
	@Autowired
	IDeviceRepository deviceRepository;

	@Override
	public List<Event> getAllEvent() throws Exception {
		LOGGER.info("Fetching event list ");
		try {
			Runnable myThread = () -> {
				eventRepository.updateEventTypinDeviceDetails();
			};
			Thread threads = new Thread(myThread);
			threads.start();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		try {
			Runnable myThread2 = () -> {
				deviceRepository.updateDeviceDetailsData();
			};
			Thread threads2 = new Thread(myThread2);
			threads2.start();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		List<Event> list = eventRepository.findAll();
		return list;
	}

	@Override
	public boolean addEvent(Event event) throws Exception {
		LOGGER.info("Inside addEvent for the event " + event);
		Event eventId = eventRepository.findByEventId(event.getEventId());
		if (eventId == null) {
			Event ev = new Event();
			boolean eventUuidUnique = false;
			String eventUuid = "";
			while (!eventUuidUnique) {
				eventUuid = UUID.randomUUID().toString();
				System.out.println("eventUuid:" + eventUuid);
				Event byUuid = eventRepository.findByUuid(eventUuid);
				if (byUuid == null) {
					eventUuidUnique = true;
				}
			}
			event.setUuid(eventUuid);
			Event save = eventRepository.save(event);
			LOGGER.info("Event Details saved successfully");
		} else {
			throw new Exception("Event already exists for the event id : " + event.getEventId());
		}
		return Boolean.TRUE;
	}
}