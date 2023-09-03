package com.pct.common.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class MSDeviceDTO implements Serializable {

	
	private long                recordID;

    private String              factoryPO ;
    private int                 factoryBoxNum;
    private String              deviceSerialNum ;
    private String              deviceModel ;
    private String              deviceID ;
    private String              deviceSimNum ;
    private String              devicePhoneNum;
    private String              serviceCountry ;
    private String              serviceNetwork ;

    private java.sql.Timestamp  QATimestampPST ;
    private String              QAStatus ;
    private String              QAResult ;

    private String              salesOrderID;
    private int                 salesBoxNum;

    private String              deviceHWID ;
    private String              deviceHWIDVer;

    private String              swVersionBaseband ;
    private String              swVersionApplication ;
    private String              deviceConfigChanged;
    private int                 deviceConfig = 0;
    private String              configurationDesc;
    private String              deviceName ;
    private String              ownerLevel1 ;
    private String              ownerLevel2 ;
    private String              ownerLevel3 ;
    private String              ownerLevel4 ;
    private String              vertical ;
    private String              deviceUsage ;

    private String              comment ;
    private int                 updateID;

    private int                 hwVersionIO = 0;

    // transient fields not to be saved in the database
    ///////////////////////////////////////////////////
    private boolean             transIsExistsInDB = false;
    private long                transIMSI; /* International Mobile Subscriber Identity. Format MCC-MNC-MSIN.
                                         MCC = 2 bytes - Mobile Country Code (e.g. 310 for USA);
                                         MNC = 2 bytes - Mobile Network Code (e.g. 410 for AT&T),
                                         MSIN = 4 bytes - sequential serial number. */
    private long                transQATimeMilliSec;  // Epoch timeliseconds
    private int                 transMCC;
    private int                 transMNC;

    private String              transDeviceBinVer ;
    private String              transDeviceAppVer ;
    private String              transDeviceCfgVer ;
    

    public String 				salesforceOrderNumber ;
    public String 				epicorOrderNumber ;


    public MSDeviceDTO() {
    }


}
