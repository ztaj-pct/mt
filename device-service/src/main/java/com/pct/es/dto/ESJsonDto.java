package com.pct.es.dto;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

//@Document(indexName = "jsonobject")
@Data
@JsonIgnoreProperties
public class ESJsonDto {
	public int id;
	//@JsonIgnore
	public Peripheral peripheral;
	//@JsonIgnore
	public OrientationFields orientationFields;
	//@JsonIgnore
	public ReportHeader    report_header;
	//@JsonIgnore
	public TftpStatus tftpStatus;
	//@JsonIgnore
	public Waterfall waterfall;
	//@JsonIgnore
	public Temperature temperature;
	//@JsonIgnore
	public GeneralMaskFields general_mask_fields;
	//@JsonIgnore
	public SoftwareVersion softwareVersion;
	//@JsonIgnore
	public BetaAbsId betaAbsId;
	//@JsonIgnore
	public ConfigVersion configVersion;
	//@JsonIgnore
	public Voltage voltage;
}
