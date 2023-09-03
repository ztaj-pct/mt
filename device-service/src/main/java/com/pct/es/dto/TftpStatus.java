package com.pct.es.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data

public class TftpStatus {
    public int id;
    public Object status;
    public String receivedTimeStamp;
    public int selector;
    public String name;
    public String displayName;
    public String tftpStatus;
}
