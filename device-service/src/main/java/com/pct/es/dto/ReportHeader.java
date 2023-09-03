package com.pct.es.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties
public class ReportHeader {
	
    public String device_id;
    public int sequence;
    public boolean acknowledge;
    public int event_id;
    public RtcdateTimeInfo rtcdate_time_info;
}
