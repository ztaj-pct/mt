package com.pct.device.version.payload;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author dhruv
 *
 */
@Data
@NoArgsConstructor
@ToString
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class ExecuteCampaignRequest {

    public static final String DEFAULT_OWNER = "engineering";

    public static final String USAGE_PRODUCTION = "production";
    public static final String USAGE_PILOT = "pilot";
    public static final String USAGE_DEMO = "demo";
    public static final String USAGE_ENGINEERING = "engineering";
    public static final String[] DEVICE_USAGES = new String[]{USAGE_ENGINEERING, USAGE_DEMO, USAGE_PILOT, USAGE_PRODUCTION};
    public static final String  NO_STRING_VALUE = "NA";
    public static final int     MIN_INDEX_VALUE = -1;
    
    private long                    reportId;
    private String                  deviceTag;
    private String                  deviceId;
    private java.sql.Timestamp      timestampReceivedPST;
    private String                  rawReport;
    private String                  uuid;
    private String                  eventType;
    private String                  deviceIPAddress;
    private int                     devicePort;
    private int                     serverPort;
    private String                  serverIPAddress;
    private int                     sequenceNumber = MIN_INDEX_VALUE;
    private java.sql.Timestamp      GPSTimestampPST ;//= TimeUtilities.GetNullTimestamp();
    private java.sql.Timestamp      RTClockTimestampPST;// = TimeUtilities.GetNullTimestamp();
    
    private String                  configurationDesc;
    private String                  extenderVersion = NO_STRING_VALUE;
    
    private String                  swVersionApplication = NO_STRING_VALUE;
    private String                  swVersionBaseband = NO_STRING_VALUE;
  //CLD-898
    private String                  bleVersion = NO_STRING_VALUE;
    
    //for CLD-960
    private String 					device_type;
    
    // for CLD-958
    private String 					liteSentryStatus;
    private String 					liteSentryHw; 
    private String 					liteSentryApp; 
    private String 					liteSentryBoot
    ;    
    private String 					maxbotixStatus; 
    private String 					maxbotixFirmware;

    private String 					maxbotixHardware;
    
    private String 					steStatus; 
    private String 					steMcu;

    private String 					steApp;
    
    private String 					riotStatus; 
    private String 					riotFirmware;

    private String 					riotHardware;

    
    private transient boolean isFiltered;
    private String customerName;
    private String latestATCommand;
    private String                  cellularBand;
    private String              ownerLevel1;
    private String              ownerLevel2;
    private String              ownerLevel3;
    private String              ownerLevel4;
    private String              vertical;
    private String              deviceUsage;
    private String 				salesforceOrderNumber;
    private String 				epicorOrderNumber;
    public WaterfallInfo waterfallData;
    private float batteryPowerV;
    
    public String config1CRC;
    public String config1CIV;
    
    public String config2CRC;
    public String config2CIV;
    
    public String config3CRC;
    public String config3CIV;
    
    public String config4CRC;
    public String config4CIV;
    
    public String config5CRC;
    public String config5CIV;
}