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


@Entity
@Getter
@Setter
@NoArgsConstructor
public class DeviceDetail implements Serializable {

	private static final long serialVersionUID = 1L;
	
    @Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
    
    private String deviceId;

    private Integer sequenceNumber;
    
	private Instant createdDate;
	
	private Instant CreatedEpoch;
	
	private Instant updatedDate;
	
	private String uuid;

	private String  rawReport;
	
	public Integer eventId;
	
	private String  parsedReport;
	
	public String deviceIP;
	
	public Integer devicePort;
	


}
