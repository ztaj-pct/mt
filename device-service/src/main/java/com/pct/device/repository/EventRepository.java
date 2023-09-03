package com.pct.device.repository;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.pct.device.model.Event;

@Repository
public interface EventRepository  extends JpaRepository<Event, Long> {

	Event findByUuid(String eventUuid);

	
	Event findByEventId(int eventId);
	
	@Transactional
	@Modifying
	@Query( nativeQuery = true, value = "UPDATE pct_device.device_details t1,pct_device.event t2 SET t1.event_type =  t2.event_type where t1.event_id = t2.event_id;")
	void updateEventTypinDeviceDetails();

}
