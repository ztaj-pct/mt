package com.pct.device.service.device;

import java.io.Serializable;
import java.time.Instant;


import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;



import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@NoArgsConstructor
@ToString
public class DeviceCommand implements Serializable {

	private static final long serialVersionUID = 1L;

    @Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;


	private String created_date;


	private Instant created_epoch;


	private Instant updated_date;


	private String uuid;


	private String device_id;

	private String at_command;

	private String source;


	private String priority;

	private Instant sent_timestamp;

	private boolean success;

	private String device_ip;

	private int device_port;

	private String device_response;

	private String status;
	
	private int retry_count;

	private String raw_report;

	private String server_ip;

	private int server_port;

	private String response_server_ip;

	private int response_server_port;


}
