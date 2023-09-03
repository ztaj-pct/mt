package com.pct.es.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Temperature {
    public int id;
    public Object status;
    public String received_time_stamp;
    public Object selector;
    public String name;
    public String display_name;
    public double ambient_temperature;
    public double internal_temperature;
}
