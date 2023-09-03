package com.pct.es.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties
public class GeneralMaskFields {
	public int id;
    public Object status;
    public String received_time_stamp;
    public Object selector;
    public String name;
    public String display_name;
    public int gpsstatus_index;
    public int num_satellites;
    public double hdop;
    public double external_power_volts;
    public double internal_power_volts;
    public double odometer_kms;
    public double latitude;
    public double longitude;
    public double altitude_feet;
    public double speed_kms;
    public int heading;
    public int rssi;
    public String gps_time;
    public String power;
    public String trip;
    public String ignition;
    public String motion;
    public String shipping_mode;
    public String vib_trip;
    public String charger;
    public String tamper;
    public String odometer_field;
    public String status_field;
    public String ext_int_power;
    public String gps_field;
    public String altitude_field;
    public String speedheading_field;
}
