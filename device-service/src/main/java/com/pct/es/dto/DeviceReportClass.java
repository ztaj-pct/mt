package com.pct.es.dto;

import java.time.Instant;


import lombok.Data;

//@Document(indexName = "jsonobject")
@Data
public class DeviceReportClass {

	private Long id;

	private String device_id;

	private int sequence_number;

	private Instant created_date;

	private Instant created_epoch;

	private Instant updated_date;

	private String uuid;

	private String raw_report;

	public Integer event_id;

	private ESJsonDto parsed_report;

}
