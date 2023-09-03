package com.pct.es.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data

public class Peripheral {
	public int id;
	public Object status;
	public String receivedTimeStamp;
	public int selector;
	public String name;
	public String displayName;
	public int portNum;
	public int driver;
	public int portType;
	public String descript;
	public String model;
	public String rev;
}
