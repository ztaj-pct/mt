package com.pct.es.dto;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import lombok.Data;
@Data
public class DeviceInfo {

	public Map<String, String> deviceInfoMap;

	public DeviceInfo(String deviceId, String deviceIP, int port, String rawReport, Integer eventId,
			Integer sequenceNumber, Timestamp createdTime, String parsedReport) {
		deviceInfoMap = new HashMap();
		deviceInfoMap.put("deviceId", deviceId);
		// if this is maintenace report
		if (eventId == 34) {
			deviceInfoMap.put("deviceMaintIP", port + "");
			deviceInfoMap.put("deviceMaintPort", port + "");
		} else {
			deviceInfoMap.put("deviceIP", deviceIP);
			deviceInfoMap.put("devicePort", port + "");
		}

		this.deviceId = deviceId;
		this.deviceIP = deviceIP;
		this.devicePort = port;
		this.createdTime = createdTime;
		this.eventId = eventId;
		this.rawReport = rawReport;
		this.sequenceNumber = sequenceNumber;
		this.parsedReport = parsedReport;

	}
	
	

	public DeviceInfo() {
		super();
		// TODO Auto-generated constructor stub
	}



	public String deviceId;
	public String deviceIP;
	public int devicePort;
	public int sequenceNumber;
	public int eventId;
	public Timestamp createdTime;
	public String rawReport;
	public String parsedReport;

}
