package com.pct.es.dto;



import lombok.Data;

@Data

public class Voltage {
    public int id;
    public Object status;
    public String received_time_stamp;
    public int selector;
    public String name;
    public String display_name;
    public double main_power;
    public double aux_power;
    public double charge_power;
    public double battery_power;
}
