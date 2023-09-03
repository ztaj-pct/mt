package com.pct.es.dto;

import java.time.Instant;

import lombok.Data;

@Data
public class DeviceReport {

	private Long id;

	private String device_id;

	private String sequence_number;

	private Instant created_date;

	private Instant created_epoch;

	private Instant updated_date;

	private String uuid;

	private String raw_report;

	public Integer event_id;

	private ESJsonDto parsed_report;

}
