package com.pct.es.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
public class RtcdateTimeInfo {
    public int year;
    public int month;
    public int day;
    public int hour;
    public int minutes;
    public int seconds;
}
