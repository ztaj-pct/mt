package com.pct.device.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "event", catalog = "pct_device")
public class Event {

	private static final long serialVersionUID = 1L; 
	 
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
	
	
	@Column(name = "event_id")
	private int  eventId;
	
	@Column(name = "event_type")
	private String eventType;
	
	@Column(name = "uuid")
	private String uuid;
	
	
	
}