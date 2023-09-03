package com.pct.device.util;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ReportRow {

    public static final String VAlUE_DATE_POSTFIX = "Dt";
    public static final String VALUE_TAG_POSTFIX = "Tag";
    public static final String VALUE_ERROR_POSTFIX = "Error";
    public static final String VALUE_WARNING_POSTFIX = "Warning";
    public static final String TRANSIENT_VALUE_PREFIX = "trans";

    public int id;  // row counter. required.
    // Device data
    //////////////////////
    public String   deviceID;
    public String   serialNum;
    public String   sim;
    public String   phoneNum;
    public String   deviceName;
    public String   deviceModel;
    public String   deviceParentCustomer;
    public String   customer;         // mandatory if registered
    public String   deviceSalesOrder;
    public String   deviceQAStatus;
    public Date     deviceQADate;     // MM/dd/yyyy HH:mm:ss
    public String   forwardURLCSV;
    public String   forwardMethodCSV;

    // Asset data
    /////////////
    public String   assetID;
    public String   assetType;
    public Date     assetDate;

    // general report data
    //////////////////////
    public String   eventTypeStr;
    public Date     maintenanceReportDate;
    public Date     startTripDate;
    public Date     endTripDate;
    public Date     dateReceived;  // MM/dd/yyyy HH:mm:ss
    public Integer  sequenceNumInt;  // integer
    public Date     dateRT;  // MM/dd/yyyy HH:mm:ss
    public Date     deviceStatusDt;
    public String   deviceStatusStr;

    // GPS related data
    ///////////////////
    public Date     latLonDt;
    public Date     dateGPS;  // MM/dd/yyyy HH:mm:ss
    public Double   latitudeDouble;  // decimal
    public Double   longitudeDouble;  // decimal
    public Date     altitudeDt;
    public Float    altitudeFloat;  // decimal
    public Date     speedHeadingDt;
    public Float    speedMPHFloat;  // decimal
    public Integer  headingInt;  // decimal (angle)
    public Date     gpsStatusDt;
    public String   gpsStatusStr;

    // Power related data
    /////////////////////
    public Date     primaryExternalPowerDt;
    public Float    primaryExternalPowerVFloat;    // decimal
    public Date     batteryPowerDt;
    public Float    batteryPowerVFloat;            // decimal
    public Date     secondaryExternalPowerDt;
    public Float    secondaryExternalPowerVFloat;  // decimal
    public Date     solarPowerDt;
    public Float    solarPowerVFloat;              // decimal
    public String   batteryPercentageStr;          // decimal
    public Date     batteryPercentageDt;
    public String   powerSourceStr;

    // Satellite Data
    /////////////////
    public Date     hdopDt;
    public Integer  numSatsInt;   // integer
    public Float    hdopFloat;    // decimal
    // horizontal dilution of precision -
    // specify the additional multiplicative effect of navigation satellite geometry
    // on positional measurement precision

    // Engine related Data
    //////////////////////
    public Date     engineHDt;
    public Double   engineHoursDouble;          // decimal hours and minutes
    public Date     odometerDt;
    public Float    odometerMilesFloat;         // decimal
    public Date     fuelLevelDt;
    public Float    fuelLevelPercentFloat;      // decimal
    public Float    odtSinceMILMilesFloat;      // decimal
    public Float    odtSinceDTCClearMilesFloat; // decimal
    public Date     vinDt;
    public String   vinStr;                   // alphanumeric
    public Date     engLightDt;
    public String   engLightStr;              // on/off
    public String   engCodesStr;              // comma separated error codes (hex)
    public Date     internalTempDt;
    public Float    internalTemperatureFloat;   // decimal degrees celsius
    public Float    ambientTemperatureFloat;    // decimal degrees celsius
    public String 	vin;

    // Acceleration and Orientation data
    ////////////////////////////////////
    public Date     accelerometerDt;
    public String   accelerometerInfoStr;
    public Integer  accelerometerXmm_s2Int;   // integer
    public Integer  accelerometerYmm_s2Int;   // integer
    public Integer  accelerometerZmm_s2Int;   // integer
    public Integer  accelerometerXCalibInt;   // integer
    public Integer  accelerometerYCalibInt;   // integer
    public Integer  accelerometerZCalibInt;   // integer
    public Date     orientDt;
    public String   orientInfoStr;
    public Integer  orientXInt;   // integer
    public Integer  orientYInt;   // integer
    public Integer  orientZInt;   // integer

    // Network related data
    ///////////////////////
    public Date     RSSIDt;
    public Integer  RSSIInt;          // integer between 0 and 99 ( the higher the worse the signal to nosie ratio
    public Date     networkDt;
    public String   serviceTypeStr;
    public String   roamingStr;
    public String   countryStr;
    public String   networkStr;
    public String   towerIDStr;
    public Double   centroidLatDouble;   // decimal
    public Double   centroidLonDouble;   // decimal
    public String   bandStr;
    public String   RxTxEcStr;

    // Versions data
    ////////////////
    public Date     appVersionDt;
    public String   osVersionStr;
    public String   appVersionStr;
    public String   hwVersionStr;
    public String   hwRevisionStr;
    public String   ioVersionStr;
    public String   extenderVersionStr;
    public String   bleVersionStr;
    public Date     configDt;
    public String   deviceConfigChangedStr;
    public String   deviceConfigStr;
    public String   configurationDescStr;

    public Date     TFTPDt;
    public String   TFTPStatusStr;

    // Peripherals data
    ///////////////////
    public Date     peripheralDt;
    public String   peripheralPortTypeStr;
    public Integer  peripheralPortNumInt; // integer
    public String   peripheralDriverStr;
    public String   peripheralDescStr;
    public String   peripheralModelStr;
    public String   peripheralRevStr;
    public String   GPIOInfoStr;

    // Sensors data
    ///////////////
    public String   cargoSensorInfoStr;  // deprecated
    public Date     cargoDate;  // deprecated

    public Date     lightDt;
    public String   lightSentryStr;
    public Date     ABSDt;
    public String   ABSStr;
    public Date     ABSOdoDt;
    public Float    ABSOdometerFloat;
    public Date     brakeDt;
    public String   brakeStrokeStr;
    public Date     doorDt;
    public String   doorStr;
    public Date     chassisDt;
    public String   chassisMatingStr;
    public Date     TPMSDt;     // tires pressure measurements system
    public String   TPMSStr;
    public Date     ATISfDt;
    public String   ATISStr;    // tires inflater
    public Date     tractorPairingDt;
    public String   tractorPairingStr;
    public Date     gladhandDt;
    public String   gladhandStr;
    public String betaABSStr;
    public Date betaABSDt;
    public String waterfallStr;
    public Date waterfallDt;
    public String psiWheelEndStr;
    public Date psiWheelEndDt;
    public String psiAirSupplyStr;
    public Date psiAirSupplyDt;
    public String bleTemperatureStr;
    public Date bleTemperatureDt;
    public String peripheralVerStr;
    public Date peripheralVerDt;
    

    public String minewTemperatureStr;
    public Date minewTemperatureDt;
    public String minewTemperatureError;
    
    public String betaTPMSStr;
    public Date betaTPMSDt;
    public String absIdStr;
    public Date absIdDt;
    public String sfkWheelEndStr;
    public Date sfkWheelEndDt;
    public String reeferStr;
    public Date reeferDt;
    public String bleDoorSensorStr;
    
    public String waterfallDevdef_cfgName;
    public String waterfallDevdef_cfgCRC_value;
    public String waterfallDevdef2_cfgName;
    public String waterfallDevdef2_cfgCRC_value;
    public String waterfallDevdef3_cfgName;
    public String waterfallDevdef3_cfgCRC_value;
    public String waterfallDevdef4_cfgName;
    public String waterfallDevdef4_cfgCRC_valuee;
    public String waterfallDevusr_cfg_Name;
    public String waterfallDevusr_cfg_CRC_value;
    
    
    //Smart Pair Beacon
    public String beaconInfoStr;
    public Date beaconInfoDt;
    public String beaconInfoJSONStr;
    
    // Gateway Advertisment Sensor(URI) TLV 1583
    public String gatewayAdvertismentSensorStr;
	public Date gatewayAdvertismentSensorDt;
    public String gatewayAdvertismentSensorError;
    
    //Tank Saver Beta 
    public String tankSaverBetaStr;
	public Date tankSaverBetaDt;
    public String tankSaverBetaError;
    
    // Advertisment Maxlink
    public String advertismentMaxlinkStr;
   	public Date advertismentMaxlinkDt;
    public String advertismentMaxlinkError;
    
    // Connectable Maxlink
    public String connectableMaxlinkStr;
   	public Date connectableMaxlinkDt;
    public String connectableMaxlinkError;
    
   
	// Communication Info
    /////////////////////
    public Integer  ACKRetriesNumInt;    // integer normally should be 0
    public String   rcvGpsTimeDiffStr;
    public String   rcvRtcTimeDiffStr;
    public String   deviceIPStr;         // xxx.xxx.xxx.xxx
    public String   serverInfoStr;
    public String   rawReportHexStr;

    // Post processing Info
    ////////////////////////
    public Float    odoMilesDiffFloat;  // miles driven since the previous report
    public Integer  hoursAtLocationInt;  // time spent at same location

    // Rendering tips
    /////////////////
    public String infoTag;
    public String seqError;
    public String ackError;
    public String latitudeError;
    public String longitudeError;
    public String altitudeError;
    public String gpsUnlockedWarning;
    public String RSSIError;
    public String headingError;
    public String centroidLatError;
    public String centroidLonError;
    public String batteryError;
    public String lightSentryError;
    public String ABSError;
    public String brakeError;
    public String doorError;
    public String chassisMatingError;
    public String TPMSError;
    public String ATISError;
    public String tractorPairingError;
    public String gladhandError;
    public String betaABSError;
    public String waterfallError;
    public String psiWheelEndError;
    public String psiAirSupplyError;
    public String bleTemperatureError;
    public String peripheralVerError;
    public String betaTPMSError;
    public String absIdError;
    public String sfkWheelEndError;
    public String reeferError;
    public String beaconInfoError;
    public String chassisTMCStr;
    public String bleDoorStr;
    public Date bleDoorDt;
    public String bleDoorError;
    
    public String 					priorDistanceStr;
    public String 					originalLatStr;
    public String 					originalLonStr;
    public String 					originalRawReportStr;
    public String					gpsDriftAppliedStr;

    // Transient (temporary) variables
    //////////////////////////////////
    public int      transRadius;
    public double   transCentroidLat;
    public double   transCentroidLon;
    public boolean  transSingleDeviceBool;
    
    public String thumbnail;
    public String originalImage;
    public String cargoCameraSensorStr;
	public Integer cameraVoltage; 


    public String getWaterfallDevdef_cfgName() {
		return waterfallDevdef_cfgName;
	}

	public void setWaterfallDevdef_cfgName(String waterfallDevdef_cfgName) {
		this.waterfallDevdef_cfgName = waterfallDevdef_cfgName;
	}

	public String getWaterfallDevdef_cfgCRC_value() {
		return waterfallDevdef_cfgCRC_value;
	}

	public void setWaterfallDevdef_cfgCRC_value(String waterfallDevdef_cfgCRC_value) {
		this.waterfallDevdef_cfgCRC_value = waterfallDevdef_cfgCRC_value;
	}

	public String getWaterfallDevdef2_cfgName() {
		return waterfallDevdef2_cfgName;
	}

	public void setWaterfallDevdef2_cfgName(String waterfallDevdef2_cfgName) {
		this.waterfallDevdef2_cfgName = waterfallDevdef2_cfgName;
	}

	public String getWaterfallDevdef2_cfgCRC_value() {
		return waterfallDevdef2_cfgCRC_value;
	}

	public void setWaterfallDevdef2_cfgCRC_value(String waterfallDevdef2_cfgCRC_value) {
		this.waterfallDevdef2_cfgCRC_value = waterfallDevdef2_cfgCRC_value;
	}

	public String getWaterfallDevdef3_cfgName() {
		return waterfallDevdef3_cfgName;
	}

	public void setWaterfallDevdef3_cfgName(String waterfallDevdef3_cfgName) {
		this.waterfallDevdef3_cfgName = waterfallDevdef3_cfgName;
	}

	public String getWaterfallDevdef3_cfgCRC_value() {
		return waterfallDevdef3_cfgCRC_value;
	}

	public void setWaterfallDevdef3_cfgCRC_value(String waterfallDevdef3_cfgCRC_value) {
		this.waterfallDevdef3_cfgCRC_value = waterfallDevdef3_cfgCRC_value;
	}

	public String getWaterfallDevdef4_cfgName() {
		return waterfallDevdef4_cfgName;
	}

	public void setWaterfallDevdef4_cfgName(String waterfallDevdef4_cfgName) {
		this.waterfallDevdef4_cfgName = waterfallDevdef4_cfgName;
	}

	public String getWaterfallDevdef4_cfgCRC_valuee() {
		return waterfallDevdef4_cfgCRC_valuee;
	}

	public void setWaterfallDevdef4_cfgCRC_valuee(String waterfallDevdef4_cfgCRC_valuee) {
		this.waterfallDevdef4_cfgCRC_valuee = waterfallDevdef4_cfgCRC_valuee;
	}

	public String getWaterfallDevusr_cfg_Name() {
		return waterfallDevusr_cfg_Name;
	}

	public void setWaterfallDevusr_cfg_Name(String waterfallDevusr_cfg_Name) {
		this.waterfallDevusr_cfg_Name = waterfallDevusr_cfg_Name;
	}

	public String getWaterfallDevusr_cfg_CRC_value() {
		return waterfallDevusr_cfg_CRC_value;
	}

	public void setWaterfallDevusr_cfg_CRC_value(String waterfallDevusr_cfg_CRC_value) {
		this.waterfallDevusr_cfg_CRC_value = waterfallDevusr_cfg_CRC_value;
	}

	//////////////////////////////////////////////////// Getter and Setters ////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSerialNum() {
        return serialNum;
    }

    public void setSerialNum(String serialNum) {
        this.serialNum = serialNum;
    }

    public String getSim() {
        return sim;
    }

    public void setSim(String sim) {
        this.sim = sim;
    }

    public String getPhoneNum() {
        return phoneNum;
    }

    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceModel() {
        return deviceModel;
    }

    public void setDeviceModel(String deviceModel) {
        this.deviceModel = deviceModel;
    }

    public String getDeviceParentCustomer() {
        return deviceParentCustomer;
    }

    public void setDeviceParentCustomer(String deviceParentCustomer) {
        this.deviceParentCustomer = deviceParentCustomer;
    }

    public String getCustomer() {
        return customer;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }

    public String getDeviceSalesOrder() {
        return deviceSalesOrder;
    }

    public void setDeviceSalesOrder(String deviceSalesOrder) {
        this.deviceSalesOrder = deviceSalesOrder;
    }

    public String getDeviceQAStatus() {
        return deviceQAStatus;
    }

    public void setDeviceQAStatus(String deviceQAStatus) {
        this.deviceQAStatus = deviceQAStatus;
    }

    public Date getDeviceQADate() {
        return deviceQADate;
    }

    public void setDeviceQADate(Date deviceQADate) {
        this.deviceQADate = deviceQADate;
    }

    public String getForwardURLCSV() {
        return forwardURLCSV;
    }

    public void setForwardURLCSV(String forwardIP) {
        this.forwardURLCSV = forwardURLCSV;
    }

    public String getForwardMethodCSV() {
        return forwardMethodCSV;
    }

    public void setForwardMethodCSV(String forwardMethodCSV) {
        this.forwardMethodCSV = forwardMethodCSV;
    }

    public String getAssetID() {
        return assetID;
    }

    public void setAssetID(String assetID) {
        this.assetID = assetID;
    }

    public String getAssetType() {
        return assetType;
    }

    public void setAssetType(String assetType) {
        this.assetType = assetType;
    }

    public Date getAssetDate() {
        return assetDate;
    }

    public void setAssetDate(Date assetDate) {
        this.assetDate = assetDate;
    }

    public String getEventTypeStr() {
        return eventTypeStr;
    }

    public void setEventTypeStr(String eventTypeStr) {
        this.eventTypeStr = eventTypeStr;
    }

    public Date getMaintenanceReportDate() {
        return maintenanceReportDate;
    }

    public void setMaintenanceReportDate(Date maintenanceReportDate) {
        this.maintenanceReportDate = maintenanceReportDate;
    }

    public Date getStartTripDate() {
        return startTripDate;
    }

    public void setStartTripDate(Date startTripDate) {
        this.startTripDate = startTripDate;
    }

    public Date getEndTripDate() {
        return endTripDate;
    }

    public void setEndTripDate(Date endTripDate) {
        this.endTripDate = endTripDate;
    }

    public String getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }


    public Date getDateReceived() {
        return dateReceived;
    }

    public void setDateReceived(Date dateReceived) {
        this.dateReceived = dateReceived;
    }

//    public Long getDateReceived() {
//
//        return dateReceived;
//    }
//    public void setDateReceived(Long dateReceived) {
//
//        this.dateReceived = dateReceived;
//    }

    public Integer getSequenceNumInt() {
        return sequenceNumInt;
    }

    public void setSequenceNumInt(Integer sequenceNumInt) {
        this.sequenceNumInt = sequenceNumInt;
    }

    public Date getDateRT() {
        return dateRT;
    }

    public void setDateRT(Date dateRT) {
        this.dateRT = dateRT;
    }

    public Date getDateGPS() {
        return dateGPS;
    }

    public void setDateGPS(Date dateGPS) {
        this.dateGPS = dateGPS;
    }

    public Double getLatitudeDouble() {
        return latitudeDouble;
    }

    public void setLatitudeDouble(Double latitudeDouble) {
        this.latitudeDouble = latitudeDouble;
    }

    public Double getLongitudeDouble() {
        return longitudeDouble;
    }

    public void setLongitudeDouble(Double longitudeDouble) {
        this.longitudeDouble = longitudeDouble;
    }

    public Float getAltitudeFloat() {
        return altitudeFloat;
    }

    public void setAltitudeFloat(Float altitudeFloat) {
        this.altitudeFloat = altitudeFloat;
    }

    public Float getPrimaryExternalPowerVFloat() {
        return primaryExternalPowerVFloat;
    }

    public void setPrimaryExternalPowerVFloat(Float primaryExternalPowerVFloat) {
        this.primaryExternalPowerVFloat = primaryExternalPowerVFloat;
    }

    public Float getSecondaryExternalPowerVFloat() {
        return secondaryExternalPowerVFloat;
    }

    public void setSecondaryExternalPowerVFloat(Float secondaryExternalPowerVFloat) {
        this.secondaryExternalPowerVFloat = secondaryExternalPowerVFloat;
    }

    public Float getBatteryPowerVFloat() {
        return batteryPowerVFloat;
    }

    public void setBatteryPowerVFloat(Float batteryPowerVFloat) {
        this.batteryPowerVFloat = batteryPowerVFloat;
    }

    public Float getSolarPowerVFloat() {
        return solarPowerVFloat;
    }

    public void setSolarPowerVFloat(Float solarPowerVFloat) {
        this.solarPowerVFloat = solarPowerVFloat;
    }

    public Date getBatteryPowerDt() {
        return batteryPowerDt;
    }

    public void setBatteryPowerDt(Date batteryPowerDt) {
        this.batteryPowerDt = batteryPowerDt;
    }

    public Date getSolarPowerDt() {
        return solarPowerDt;
    }

    public void setSolarPowerDt(Date solarPowerDt) {
        this.solarPowerDt = solarPowerDt;
    }

    public String getBatteryPercentageStr() {
        return batteryPercentageStr;
    }

    public void setBatteryPercentageStr(String batteryPercentageStr) {
        this.batteryPercentageStr = batteryPercentageStr;
    }

    public Date getBatteryPercentageDt() {
        return batteryPercentageDt;
    }

    public void setBatteryPercentageDt(Date batteryPercentageDt) {
        this.batteryPercentageDt = batteryPercentageDt;
    }

    public String getPowerSourceStr() {
        return powerSourceStr;
    }

    public void setPowerSourceStr(String powerSourceStr) {
        this.powerSourceStr = powerSourceStr;
    }

    public Float getSpeedMPHFloat() {
        return speedMPHFloat;
    }

    public void setSpeedMPHFloat(Float speedMPHFloat) {
        this.speedMPHFloat = speedMPHFloat;
    }

    public Integer getHeadingInt() {
        return headingInt;
    }

    public void setHeadingInt(Integer headingInt) {
        this.headingInt = headingInt;
    }

    public String getGpsStatusStr() {
        return gpsStatusStr;
    }

    public void setGpsStatusStr(String gpsStatusStr) {
        this.gpsStatusStr = gpsStatusStr;
    }

    public Integer getNumSatsInt() {
        return numSatsInt;
    }

    public void setNumSatsInt(Integer numSatsInt) {
        this.numSatsInt = numSatsInt;
    }

    public Float getHdopFloat() {
        return hdopFloat;
    }

    public void setHdopFloat(Float hdopFloat) {
        this.hdopFloat = hdopFloat;
    }

    public String getDeviceStatusStr() {
        return deviceStatusStr;
    }

    public void setDeviceStatusStr(String deviceStatusStr) {
        this.deviceStatusStr = deviceStatusStr;
    }

    public Double getEngineHoursDouble() {
        return engineHoursDouble;
    }

    public void setEngineHoursDouble(Double engineHoursDouble) {
        this.engineHoursDouble = engineHoursDouble;
    }

    public Float getOdometerMilesFloat() {
        return odometerMilesFloat;
    }

    public void setOdometerMilesFloat(Float odometerMilesFloat) {
        this.odometerMilesFloat = odometerMilesFloat;
    }

    public Float getFuelLevelPercentFloat() {
        return fuelLevelPercentFloat;
    }

    public void setFuelLevelPercentFloat(Float fuelLevelPercentFloat) {
        this.fuelLevelPercentFloat = fuelLevelPercentFloat;
    }

    public Float getOdtSinceMILMilesFloat() {
        return odtSinceMILMilesFloat;
    }

    public void setOdtSinceMILMilesFloat(Float odtSinceMILMilesInt) {
        this.odtSinceMILMilesFloat = odtSinceMILMilesInt;
    }

    public Float getOdtSinceDTCClearMilesFloat() {
        return odtSinceDTCClearMilesFloat;
    }

    public void setOdtSinceDTCClearMilesFloat(Float odtSinceDTCClearMilesFloat) {
        this.odtSinceDTCClearMilesFloat = odtSinceDTCClearMilesFloat;
    }

    public String getAccelerometerInfoStr() {
        return accelerometerInfoStr;
    }

    public void setAccelerometerInfoStr(String accelerometerInfoStr) {
        this.accelerometerInfoStr = accelerometerInfoStr;
    }

    public Integer getAccelerometerXmm_s2Int() {
        return accelerometerXmm_s2Int;
    }

    public void setAccelerometerXmm_s2Int(Integer accelerometerXmm_s2Int) {
        this.accelerometerXmm_s2Int = accelerometerXmm_s2Int;
    }

    public Integer getAccelerometerYmm_s2Int() {
        return accelerometerYmm_s2Int;
    }

    public void setAccelerometerYmm_s2Int(Integer accelerometerYmm_s2Int) {
        this.accelerometerYmm_s2Int = accelerometerYmm_s2Int;
    }

    public Integer getAccelerometerZmm_s2Int() {
        return accelerometerZmm_s2Int;
    }

    public void setAccelerometerZmm_s2Int(Integer accelerometerZmm_s2Int) {
        this.accelerometerZmm_s2Int = accelerometerZmm_s2Int;
    }

    public Integer getAccelerometerXCalibInt() {
        return accelerometerXCalibInt;
    }

    public void setAccelerometerXCalibInt(Integer accelerometerXCalibInt) {
        this.accelerometerXCalibInt = accelerometerXCalibInt;
    }

    public Integer getAccelerometerYCalibInt() {
        return accelerometerYCalibInt;
    }

    public void setAccelerometerYCalibInt(Integer accelerometerYCalibInt) {
        this.accelerometerYCalibInt = accelerometerYCalibInt;
    }

    public Integer getAccelerometerZCalibInt() {
        return accelerometerZCalibInt;
    }

    public void setAccelerometerZCalibInt(Integer accelerometerZCalibInt) {
        this.accelerometerZCalibInt = accelerometerZCalibInt;
    }

    public String getOrientInfoStr() {
        return orientInfoStr;
    }

    public void setOrientInfoStr(String orientInfoStr) {
        this.orientInfoStr = orientInfoStr;
    }

    public Integer getOrientXInt() {
        return orientXInt;
    }

    public void setOrientXInt(Integer orientXInt) {
        this.orientXInt = orientXInt;
    }

    public Integer getOrientYInt() {
        return orientYInt;
    }

    public void setOrientYInt(Integer orientYInt) {
        this.orientYInt = orientYInt;
    }

    public Integer getOrientZInt() {
        return orientZInt;
    }

    public void setOrientZInt(Integer orientZInt) {
        this.orientZInt = orientZInt;
    }

    public Float getInternalTemperatureFloat() {
        return internalTemperatureFloat;
    }

    public void setInternalTemperatureFloat(Float internalTemperatureFloat) {
        this.internalTemperatureFloat = internalTemperatureFloat;
    }

    public Float getAmbientTemperatureFloat() {
        return ambientTemperatureFloat;

    }

    public void setAmbientTemperatureFloat(Float ambientTemperatureFloat) {
        this.ambientTemperatureFloat = ambientTemperatureFloat;

    }

    public Integer getRSSIInt() {
        return RSSIInt;

    }

    public void setRSSIInt(Integer RSSIInt) {
        this.RSSIInt = RSSIInt;

    }

    public String getServiceTypeStr() {
        return serviceTypeStr;

    }

    public void setServiceTypeStr(String serviceTypeStr) {
        this.serviceTypeStr = serviceTypeStr;

    }

    public String getRoamingStr() {
        return roamingStr;

    }

    public void setRoamingStr(String roamingStr) {
        this.roamingStr = roamingStr;
    }

    public String getCountryStr() {
        return countryStr;

    }

    public void setCountryStr(String countryStr) {
        this.countryStr = countryStr;

    }

    public String getNetworkStr() {
        return networkStr;

    }

    public void setNetworkStr(String networkStr) {
        this.networkStr = networkStr;

    }

    public String getTowerIDStr() {
        return towerIDStr;

    }

    public void setTowerIDStr(String towerIDStr) {
        this.towerIDStr = towerIDStr;

    }

    public Double getCentroidLatDouble() {
        return centroidLatDouble;

    }

    public void setCentroidLatDouble(Double centroidLatDouble) {
        this.centroidLatDouble = centroidLatDouble;

    }

    public Double getCentroidLonDouble() {
        return centroidLonDouble;

    }

    public void setCentroidLonDouble(Double centroidLonDouble) {
        this.centroidLonDouble = centroidLonDouble;

    }

    public String getBandStr() {
        return bandStr;

    }

    public void setBandStr(String bandStr) {
        this.bandStr = bandStr;

    }

    public String getRxTxEcStr() {
        return RxTxEcStr;

    }

    public void setRxTxEcStr(String rxTxEcStr) {
        RxTxEcStr = rxTxEcStr;

    }

    public String getVinStr() {
        return vinStr;

    }

    public void setVinStr(String vinStr) {
        this.vinStr = vinStr;

    }

    public String getEngLightStr() {
        return engLightStr;

    }

    public void setEngLightStr(String engLightStr) {
        this.engLightStr = engLightStr;

    }

    public String getEngCodesStr() {
        return engCodesStr;

    }

    public void setEngCodesStr(String engCodesStr) {
        this.engCodesStr = engCodesStr;

    }

    public String getOsVersionStr() {
        return osVersionStr;

    }

    public void setOsVersionStr(String osVersionStr) {
        this.osVersionStr = osVersionStr;

    }

    public String getAppVersionStr() {
        return appVersionStr;

    }

    public void setAppVersionStr(String appVersionStr) {
        this.appVersionStr = appVersionStr;

    }

    public String getHwVersionStr() {
        return hwVersionStr;

    }

    public void setHwVersionStr(String hwVersionStr) {
        this.hwVersionStr = hwVersionStr;

    }

    public String getHwRevisionStr() {
        return hwRevisionStr;

    }

    public void setHwRevisionStr(String hwRevisionStr) {
        this.hwRevisionStr = hwRevisionStr;

    }

    public String getIoVersionStr() {
        return ioVersionStr;
    }

    public void setIoVersionStr(String ioVersionStr) {
        this.ioVersionStr = ioVersionStr;

    }
    
    public String getBleVersionStr() {
		return bleVersionStr;
	}

	public void setBleVersionStr(String bleVersionStr) {
		this.bleVersionStr = bleVersionStr;
	}

    public String getExtenderVersionStr() {
        return extenderVersionStr;
    }

    public void setExtenderVersionStr(String extenderVersionStr) {
        this.extenderVersionStr = extenderVersionStr;
    }

    public String getDeviceConfigChangedStr() {
        return deviceConfigChangedStr;
    }

    public void setDeviceConfigChangedStr(String deviceConfigChangedStr) {
        this.deviceConfigChangedStr = deviceConfigChangedStr;
    }

    public String getDeviceConfigStr() {
        return deviceConfigStr;
    }

    public void setDeviceConfigStr(String deviceConfigStr) {
        this.deviceConfigStr = deviceConfigStr;
    }

    public String getConfigurationDescStr() {
        return configurationDescStr;
    }

    public void setConfigurationDescStr(String configurationDescStr) {
        this.configurationDescStr = configurationDescStr;
    }

    public Date getCargoDate() {
        return cargoDate;
    }

    public void setCargoDate(Date cargoDate) {
        this.cargoDate = cargoDate;
    }

    public String getCargoSensorInfoStr() {
        return cargoSensorInfoStr;
    }

    public void setCargoSensorInfoStr(String cargoSensorInfoStr) {
        this.cargoSensorInfoStr = cargoSensorInfoStr;
    }

    public String getPeripheralPortTypeStr() {
        return peripheralPortTypeStr;
    }

    public void setPeripheralPortTypeStr(String peripheralPortTypeStr) {
        this.peripheralPortTypeStr = peripheralPortTypeStr;
    }

    public Integer getPeripheralPortNumInt() {
        return peripheralPortNumInt;
    }

    public void setPeripheralPortNumInt(Integer peripheralPortNumInt) {
        this.peripheralPortNumInt = peripheralPortNumInt;

    }

    public String getPeripheralDriverStr() {
        return peripheralDriverStr;

    }

    public void setPeripheralDriverStr(String peripheralDriverStr) {
        this.peripheralDriverStr = peripheralDriverStr;
    }

    public String getPeripheralDescStr() {
        return peripheralDescStr;
    }

    public void setPeripheralDescStr(String peripheralDescStr) {
        this.peripheralDescStr = peripheralDescStr;

    }

    public String getPeripheralModelStr() {
        return peripheralModelStr;
    }

    public void setPeripheralModelStr(String peripheralModelStr) {
        this.peripheralModelStr = peripheralModelStr;
    }

    public String getPeripheralRevStr() {
        return peripheralRevStr;
    }

    public void setPeripheralRevStr(String peripheralRevStr) {
        this.peripheralRevStr = peripheralRevStr;
    }

    public String getGPIOInfoStr() {
        return GPIOInfoStr;
    }

    public void setGPIOInfoStr(String GPIOInfoStr) {
        this.GPIOInfoStr = GPIOInfoStr;
    }

    public String getTFTPStatusStr() {
        return TFTPStatusStr;
    }

    public void setTFTPStatusStr(String TFTPStatusStr) {
        this.TFTPStatusStr = TFTPStatusStr;
    }

    public String getLightSentryStr() {
        return lightSentryStr;
    }

    public void setLightSentryStr(String lightSentryStr) {
        this.lightSentryStr = lightSentryStr;
    }

    public String getABSStr() {
        return ABSStr;
    }

    public void setABSStr(String ABSStr) {
        this.ABSStr = ABSStr;
    }

    public Float getABSOdometerFloat() {
        return ABSOdometerFloat;
    }

    public void setABSOdometerFloat(Float ABSOdometerFloat) {
        this.ABSOdometerFloat = ABSOdometerFloat;
    }

    public String getBrakeStrokeStr() {
        return brakeStrokeStr;
    }

    public void setBrakeStrokeStr(String brakeStrokeStr) {
        this.brakeStrokeStr = brakeStrokeStr;
    }

    public String getDoorStr() {
        return doorStr;
    }

    public void setDoorStr(String doorStr) {
        this.doorStr = doorStr;
    }

    public String getChassisMatingStr() {
        return chassisMatingStr;
    }

    public void setChassisMatingStr(String chassisMatingStr) {
        this.chassisMatingStr = chassisMatingStr;
    }

    public String getTPMSStr() {
        return TPMSStr;
    }

    public void setTPMSStr(String TPMSStr) {
        this.TPMSStr = TPMSStr;
    }

    public String getATISStr() {
        return ATISStr;
    }

    public void setATISStr(String ATISStr) {
        this.ATISStr = ATISStr;
    }

    public Date getTractorPairingDt() {
        return tractorPairingDt;
    }

    public void setTractorPairingDt(Date tractorPairingDt) {
        this.tractorPairingDt = tractorPairingDt;
    }

    public String getTractorPairingStr() {
        return tractorPairingStr;
    }

    public void setTractorPairingStr(String tractorPairingStr) {
        this.tractorPairingStr = tractorPairingStr;
    }

    public Date getGladhandDt() {
        return gladhandDt;
    }

    public void setGladhandDt(Date gladhandDt) {
        this.gladhandDt = gladhandDt;
    }

    public String getGladhandStr() {
        return gladhandStr;
    }

    public void setGladhandStr(String gladhandStr) {
        this.gladhandStr = gladhandStr;
    }

    public String getTractorPairingError() {
        return tractorPairingError;
    }

    public void setTractorPairingError(String tractorPairingError) {
        this.tractorPairingError = tractorPairingError;
    }

    public String getGladhandError() {
        return gladhandError;
    }

    public void setGladhandError(String gladhandError) {
        this.gladhandError = gladhandError;
    }

    public Integer getACKRetriesNumInt() {
        return ACKRetriesNumInt;
    }

    public void setACKRetriesNumInt(Integer ACKRetriesNumInt) {
        this.ACKRetriesNumInt = ACKRetriesNumInt;
    }

    public String getRcvGpsTimeDiffStr() {
        return rcvGpsTimeDiffStr;
    }

    public void setRcvGpsTimeDiffStr(String rcvGpsTimeDiffStr) {
        this.rcvGpsTimeDiffStr = rcvGpsTimeDiffStr;
    }

    public String getRcvRtcTimeDiffStr() {
        return rcvRtcTimeDiffStr;
    }

    public void setRcvRtcTimeDiffStr(String rcvRtcTimeDiffStr) {
        this.rcvRtcTimeDiffStr = rcvRtcTimeDiffStr;
    }

    public String getDeviceIPStr() {
        return deviceIPStr;
    }

    public void setDeviceIPStr(String deviceIPStr) {
        this.deviceIPStr = deviceIPStr;
    }

    public String getRawReportHexStr() {
        return rawReportHexStr;
    }

    public void setRawReportHexStr(String rawReportHexStr) {
        this.rawReportHexStr = rawReportHexStr;
    }

    public String getServerInfoStr() {
        return serverInfoStr;
    }

    public void setServerInfoStr(String serverInfoStr) {
        this.serverInfoStr = serverInfoStr;
    }

    public Float getOdoMilesDiffFloat() {
        return odoMilesDiffFloat;
    }

    public void setOdoMilesDiffFloat(Float odoMilesDiffFloat) {
        this.odoMilesDiffFloat = odoMilesDiffFloat;
    }

    public Integer getHoursAtLocationInt() {
        return hoursAtLocationInt;
    }

    public void setHoursAtLocationInt(Integer hoursAtLocationInt) {
        this.hoursAtLocationInt = hoursAtLocationInt;
    }

    public String getInfoTag() {
        return infoTag;
    }

    public void setInfoTag(String infoTag) {
        this.infoTag = infoTag;
    }

    public String getSeqError() {
        return seqError;
    }

    public void setSeqError(String seqError) {
        this.seqError = seqError;
    }

    public String getAckError() {
        return ackError;
    }

    public void setAckError(String ackError) {
        this.ackError = ackError;
    }

    public String getLatitudeError() {
        return latitudeError;
    }

    public void setLatitudeError(String latitudeError) {
        this.latitudeError = latitudeError;
    }

    public String getLongitudeError() {
        return longitudeError;
    }

    public void setLongitudeError(String longitudeError) {
        this.longitudeError = longitudeError;
    }

    public String getAltitudeError() {
        return altitudeError;
    }

    public void setAltitudeError(String altitudeError) {
        this.altitudeError = altitudeError;
    }

    public String getGpsUnlockedWarning() {
        return gpsUnlockedWarning;
    }

    public void setGpsUnlockedWarning(String gpsUnlockedWarning) {
        this.gpsUnlockedWarning = gpsUnlockedWarning;
    }

    public String getRSSIError() {
        return RSSIError;
    }

    public void setRSSIError(String RSSIError) {
        this.RSSIError = RSSIError;
    }

    public String getHeadingError() {
        return headingError;
    }

    public void setHeadingError(String headingError) {
        this.headingError = headingError;
    }

    public String getCentroidLatError() {
        return centroidLatError;
    }

    public void setCentroidLatError(String centroidLatError) {
        this.centroidLatError = centroidLatError;
    }

    public String getCentroidLonError() {
        return centroidLonError;
    }

    public String getBatteryError() {
        return batteryError;
    }

    public void setBatteryError(String batteryError) {
        this.batteryError = batteryError;
    }

    public String getLightSentryError() {
        return lightSentryError;
    }

    public void setLightSentryError(String lightSentryError) {
        this.lightSentryError = lightSentryError;
    }

    public String getABSError() {
        return ABSError;
    }

    public void setABSError(String ABSError) {
        this.ABSError = ABSError;
    }

    public String getBrakeError() {
        return brakeError;
    }

    public void setBrakeError(String brakeError) {
        this.brakeError = brakeError;
    }

    public String getDoorError() {
        return doorError;
    }

    public void setDoorError(String doorError) {
        this.doorError = doorError;
    }

    public String getChassisMatingError() {
        return chassisMatingError;
    }

    public void setChassisMatingError(String chassisMatingError) {
        this.chassisMatingError = chassisMatingError;
    }

    public String getTPMSError() {
        return TPMSError;
    }

    public void setTPMSError(String TPMSError) {
        this.TPMSError = TPMSError;
    }

    public String getATISError() {
        return ATISError;
    }

    public void setATISError(String ATISError) {
        this.ATISError = ATISError;
    }

    public void setCentroidLonError(String centroidLonError) {
        this.centroidLonError = centroidLonError;
    }

    public int getTransRadius() {
        return transRadius;
    }

    public void setTransRadius(int transRadius) {
        this.transRadius = transRadius;
    }

    public double getTransCentroidLat() {
        return transCentroidLat;
    }

    public void setTransCentroidLat(double transCentroidLat) {
        this.transCentroidLat = transCentroidLat;
    }

    public double getTransCentroidLon() {
        return transCentroidLon;
    }

    public void setTransCentroidLon(double transCentroidLon) {
        this.transCentroidLon = transCentroidLon;
    }

    public boolean isTransSingleDeviceBool() {
        return transSingleDeviceBool;
    }

    public void setTransSingleDeviceBool(boolean transSingleDeviceBool) {
        this.transSingleDeviceBool = transSingleDeviceBool;
    }

    public Date getDeviceStatusDt() {
        return deviceStatusDt;
    }

    public void setDeviceStatusDt(Date deviceStatusDt) {
        this.deviceStatusDt = deviceStatusDt;
    }

    public Date getLatLonDt() {
        return latLonDt;
    }

    public void setLatLonDt(Date latLonDt) {
        this.latLonDt = latLonDt;
    }

    public Date getAltitudeDt() {
        return altitudeDt;
    }

    public void setAltitudeDt(Date altitudeDt) {
        this.altitudeDt = altitudeDt;
    }

    public Date getSpeedHeadingDt() {
        return speedHeadingDt;
    }

    public void setSpeedHeadingDt(Date speedHeadingDt) {
        this.speedHeadingDt = speedHeadingDt;
    }

    public Date getGpsStatusDt() {
        return gpsStatusDt;
    }

    public void setGpsStatusDt(Date gpsStatusDt) {
        this.gpsStatusDt = gpsStatusDt;
    }

    public Date getPrimaryExternalPowerDt() {
        return primaryExternalPowerDt;
    }

    public void setPrimaryExternalPowerDt(Date primaryExternalPowerDt) {
        this.primaryExternalPowerDt = primaryExternalPowerDt;
    }

    public Date getSecondaryExternalPowerDt() {
        return secondaryExternalPowerDt;
    }

    public void setSecondaryExternalPowerDt(Date secondaryExternalPowerDt) {
        this.secondaryExternalPowerDt = secondaryExternalPowerDt;
    }

    public Date getHdopDt() {
        return hdopDt;
    }

    public void setHdopDt(Date hdopDt) {
        this.hdopDt = hdopDt;
    }

    public Date getEngineHDt() {
        return engineHDt;
    }

    public void setEngineHDt(Date engineHDt) {
        this.engineHDt = engineHDt;
    }

    public Date getOdometerDt() {
        return odometerDt;
    }

    public void setOdometerDt(Date odometerDt) {
        this.odometerDt = odometerDt;
    }

    public Date getFuelLevelDt() {
        return fuelLevelDt;
    }

    public void setFuelLevelDt(Date fuelLevelDt) {
        this.fuelLevelDt = fuelLevelDt;
    }

    public Date getVinDt() {
        return vinDt;
    }

    public void setVinDt(Date vinDt) {
        this.vinDt = vinDt;
    }

    public Date getEngLightDt() {
        return engLightDt;
    }

    public void setEngLightDt(Date engLightDt) {
        this.engLightDt = engLightDt;
    }

    public Date getInternalTempDt() {
        return internalTempDt;
    }

    public void setInternalTempDt(Date internalTempDt) {
        this.internalTempDt = internalTempDt;
    }

    public Date getAccelerometerDt() {
        return accelerometerDt;
    }

    public void setAccelerometerDt(Date accelerometerDt) {
        this.accelerometerDt = accelerometerDt;
    }

    public Date getOrientDt() {
        return orientDt;
    }

    public void setOrientDt(Date orientDt) {
        this.orientDt = orientDt;
    }

    public Date getRSSIDt() {
        return RSSIDt;
    }

    public void setRSSIDt(Date RSSIDt) {
        this.RSSIDt = RSSIDt;
    }

    public Date getNetworkDt() {
        return networkDt;
    }

    public void setNetworkDt(Date networkDt) {
        this.networkDt = networkDt;
    }

    public Date getAppVersionDt() {
        return appVersionDt;
    }

    public void setAppVersionDt(Date appVersionDt) {
        this.appVersionDt = appVersionDt;
    }

    public Date getConfigDt() {
        return configDt;
    }

    public void setConfigDt(Date configDt) {
        this.configDt = configDt;
    }

    public Date getTFTPDt() {
        return TFTPDt;
    }

    public void setTFTPDt(Date TFTPDt) {
        this.TFTPDt = TFTPDt;
    }

    public Date getPeripheralDt() {
        return peripheralDt;
    }

    public void setPeripheralDt(Date peripheralDt) {
        this.peripheralDt = peripheralDt;
    }

    public Date getLightDt() {
        return lightDt;
    }

    public void setLightDt(Date lightDt) {
        this.lightDt = lightDt;
    }

    public Date getABSDt() {
        return ABSDt;
    }

    public void setABSDt(Date ABSDt) {
        this.ABSDt = ABSDt;
    }

    public Date getABSOdoDt() {
        return ABSOdoDt;
    }

    public void setABSOdoDt(Date ABSOdoDt) {
        this.ABSOdoDt = ABSOdoDt;
    }

    public Date getBrakeDt() {
        return brakeDt;
    }

    public void setBrakeDt(Date brakeDt) {
        this.brakeDt = brakeDt;
    }

    public Date getDoorDt() {
        return doorDt;
    }

    public void setDoorDt(Date doorDt) {
        this.doorDt = doorDt;
    }

    public Date getChassisDt() {
        return chassisDt;
    }

    public void setChassisDt(Date chassisDt) {
        this.chassisDt = chassisDt;
    }

    public Date getTPMSDt() {
        return TPMSDt;
    }

    public void setTPMSDt(Date TPMSDt) {
        this.TPMSDt = TPMSDt;
    }

    public Date getATISfDt() {
        return ATISfDt;
    }

    public void setATISfDt(Date ATISfDt) {
        this.ATISfDt = ATISfDt;
    }

    public String getBetaABSStr() {
        return betaABSStr;
    }

    public void setBetaABSStr(String betaABSStr) {
        this.betaABSStr = betaABSStr;
    }

    public Date getBetaABSDt() {
        return betaABSDt;
    }

    public void setBetaABSDt(Date betaABSDt) {
        this.betaABSDt = betaABSDt;
    }

    public String getBetaABSError() {
        return betaABSError;
    }

    public void setBetaABSError(String betaABSError) {
        this.betaABSError = betaABSError;
    }

    public String getWaterfallStr() {
        return waterfallStr;
    }
    
    public void setWaterfallStr(String waterfallStr) {
		this.waterfallStr = waterfallStr;
	}

	public void setWaterfallDt(Date waterfallDt) {
		this.waterfallDt = waterfallDt;
	}

	public void setWaterfallError(String waterfallError) {
		this.waterfallError = waterfallError;
	}

	public Date getWaterfallDt() {
        return waterfallDt;
    }

    public String getWaterfallError() {
        return waterfallError;
    }

    public String getPSIWheelEndStr() {
        return psiWheelEndStr;
    }

    public Date getPSIWheelEndDt() {
        return psiWheelEndDt;
    }

    public String getPSIWheelEndError() {
        return psiWheelEndError;
    }

    public void setPSIWheelEndStr(String psiWheelEndStr) {
        this.psiWheelEndStr = psiWheelEndStr;
    }

    public void setPSIWheelEndDt(Date psiWheelEndDt) {
        this.psiWheelEndDt = psiWheelEndDt;
    }

    public void setPSIWheelEndError(String psiWheelEndError) {
        this.psiWheelEndError = psiWheelEndError;
    }

    public String getPsiAirSupplyStr() {
        return psiAirSupplyStr;
    }

    public void setPsiAirSupplyStr(String psiAirSupplyStr) {
        this.psiAirSupplyStr = psiAirSupplyStr;
    }

    public Date getPsiAirSupplyDt() {
        return psiAirSupplyDt;
    }

    public void setPsiAirSupplyDt(Date psiAirSupplyDt) {
        this.psiAirSupplyDt = psiAirSupplyDt;
    }

    public String getPsiAirSupplyError() {
        return psiAirSupplyError;
    }

    public void setPsiAirSupplyError(String psiAirSupplyError) {
        this.psiAirSupplyError = psiAirSupplyError;
    }

    public String getBleTemperatureStr() {
        return bleTemperatureStr;
    }

    public void setBleTemperatureStr(String bleTemperatureStr) {
        this.bleTemperatureStr = bleTemperatureStr;
    }

    public Date getBleTemperatureDt() {
        return bleTemperatureDt;
    }

    public void setBleTemperatureDt(Date bleTemperatureDt) {
        this.bleTemperatureDt = bleTemperatureDt;
    }

    public String getBleTemperatureError() {
        return bleTemperatureError;
    }

    public void setBleTemperatureError(String bleTemperatureError) {
        this.bleTemperatureError = bleTemperatureError;
    }

    public String getPeripheralVerStr() {
		return peripheralVerStr;
	}

	public void setPeripheralVerStr(String peripheralVerStr) {
		this.peripheralVerStr = peripheralVerStr;
	}

	public Date getPeripheralVerDt() {
		return peripheralVerDt;
	}

	public void setPeripheralVerDt(Date peripheralVerDt) {
		this.peripheralVerDt = peripheralVerDt;
	}

	public String getPeripheralVerError() {
		return peripheralVerError;
	}

	public void setPeripheralVerError(String peripheralVerError) {
		this.peripheralVerError = peripheralVerError;
	}

    public String getReeferStr() {
        return reeferStr;
    }

    public void setReeferStr(String reeferStr) {
        this.reeferStr = reeferStr;
    }

    public Date getReeferDt() {
        return reeferDt;
    }

    public void setReeferDt(Date reeferDt) {
        this.reeferDt = reeferDt;
    }

    public String getSfkWheelEndError() {
        return sfkWheelEndError;
    }

    public void setSfkWheelEndError(String sfkWheelEndError) {
        this.sfkWheelEndError = sfkWheelEndError;
    }

    public String getReeferError() {
        return reeferError;
    }

    public void setReeferError(String reeferError) {
        this.reeferError = reeferError;
    }
    
    public String getBeaconInfoStr() {
		return beaconInfoStr;
	}

	public void setBeaconInfoStr(String beaconInfoStr) {
		this.beaconInfoStr = beaconInfoStr;
	}

	public Date getBeaconInfoDt() {
		return beaconInfoDt;
	}

	public void setBeaconInfoDt(Date beaconInfoDt) {
		this.beaconInfoDt = beaconInfoDt;
	}

	public String getBeaconInfoError() {
		return beaconInfoError;
	}

	public void setBeaconInfoError(String beaconInfoError) {
		this.beaconInfoError = beaconInfoError;
	}
	
	public String getBeaconInfoJSONStr() {
		return beaconInfoJSONStr;
	}

  public String getVin() {
		return vin;
	}

	public void setVin(String vin) {
		this.vin = vin;
	}

	public void setBeaconInfoJSONStr(String beaconInfoJSONStr) {
		this.beaconInfoJSONStr = beaconInfoJSONStr;
	}
	
	public String getChassisTMCStr() {
		return chassisTMCStr;
	}

	public void setChassisTMCStr(String chassisTMCStr) {
		this.chassisTMCStr = chassisTMCStr;
	}
	
	public String getBleDoorStr() {
		return bleDoorStr;
	}

	public void setBleDoorStr(String bleDoorStr) {
		this.bleDoorStr = bleDoorStr;
	}

	public Date getBleDoorDt() {
		return bleDoorDt;
	}

	public void setBleDoorDt(Date bleDoorDt) {
		this.bleDoorDt = bleDoorDt;
	}

	public String getBleDoorError() {
		return bleDoorError;
	}

	public void setBleDoorError(String bleDoorError) {
		this.bleDoorError = bleDoorError;
	}
  
	public String getMinewTemperatureStr() {
		return minewTemperatureStr;
	}

	public void setMinewTemperatureStr(String minewTemperatureStr) {
		this.minewTemperatureStr = minewTemperatureStr;
	}

	public Date getMinewTemperatureDt() {
		return minewTemperatureDt;
	}

	public void setMinewTemperatureDt(Date minewTemperatureDt) {
		this.minewTemperatureDt = minewTemperatureDt;
	}

	public String getMinewTemperatureError() {
		return minewTemperatureError;
	}

	public void setMinewTemperatureError(String minewTemperatureError) {
		this.minewTemperatureError = minewTemperatureError;
	}
	
	 public String getGatewayAdvertismentSensorStr() {
			return gatewayAdvertismentSensorStr;
		}

		public void setGatewayAdvertismentSensorStr(String gatewayAdvertismentSensorStr) {
			this.gatewayAdvertismentSensorStr = gatewayAdvertismentSensorStr;
		}

		public Date getGatewayAdvertismentSensorDt() {
			return gatewayAdvertismentSensorDt;
		}

		public void setGatewayAdvertismentSensorDt(Date gatewayAdvertismentSensorDt) {
			this.gatewayAdvertismentSensorDt = gatewayAdvertismentSensorDt;
		}

		public String getGatewayAdvertismentSensorError() {
			return gatewayAdvertismentSensorError;
		}

		public void setGatewayAdvertismentSensorError(String gatewayAdvertismentSensorError) {
			this.gatewayAdvertismentSensorError = gatewayAdvertismentSensorError;
		}

		public String getTankSaverBetaStr() {
			return tankSaverBetaStr;
		}

		public void setTankSaverBetaStr(String tankSaverBetaStr) {
			this.tankSaverBetaStr = tankSaverBetaStr;
		}

		public Date getTankSaverBetaDt() {
			return tankSaverBetaDt;
		}

		public void setTankSaverBetaDt(Date tankSaverBetaDt) {
			this.tankSaverBetaDt = tankSaverBetaDt;
		}

		public String getTankSaverBetaError() {
			return tankSaverBetaError;
		}

		public void setTankSaverBetaError(String tankSaverBetaError) {
			this.tankSaverBetaError = tankSaverBetaError;
		}

		public String getAdvertismentMaxlinkStr() {
			return advertismentMaxlinkStr;
		}

		public void setAdvertismentMaxlinkStr(String advertismentMaxlinkStr) {
			this.advertismentMaxlinkStr = advertismentMaxlinkStr;
		}

		public Date getAdvertismentMaxlinkDt() {
			return advertismentMaxlinkDt;
		}

		public void setAdvertismentMaxlinkDt(Date advertismentMaxlinkDt) {
			this.advertismentMaxlinkDt = advertismentMaxlinkDt;
		}

		public String getAdvertismentMaxlinkError() {
			return advertismentMaxlinkError;
		}

		public void setAdvertismentMaxlinkError(String advertismentMaxlinkError) {
			this.advertismentMaxlinkError = advertismentMaxlinkError;
		}
		
		public String getConnectableMaxlinkStr() {
			return connectableMaxlinkStr;
		}

		public void setConnectableMaxlinkStr(String connectableMaxlinkStr) {
			this.connectableMaxlinkStr = connectableMaxlinkStr;
		}

		public Date getConnectableMaxlinkDt() {
			return connectableMaxlinkDt;
		}

		public void setConnectableMaxlinkDt(Date connectableMaxlinkDt) {
			this.connectableMaxlinkDt = connectableMaxlinkDt;
		}

		public String getConnectableMaxlinkError() {
			return connectableMaxlinkError;
		}

		public void setConnectableMaxlinkError(String connectableMaxlinkError) {
			this.connectableMaxlinkError = connectableMaxlinkError;
		}


    ////////////////////////////////////////////////// Helper Methods //////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public void SetSensorErrorFlags() {

        if (!gpsStatusStr.equalsIgnoreCase("Locked")) {
            gpsUnlockedWarning = "#ffad33";
        } else {
            gpsUnlockedWarning = null;
        }
        if (ACKRetriesNumInt > 0) {
            ackError = "#ffad33";
        } else {
            ackError = null;
        }
        if (RSSIInt >=  99) {
            RSSIError = "#ffad33";
        } else {
            RSSIError = null;
        }
    }

    /*
        Write the column values for a device/report row. Must ne same order as the actual values
        written in the method after this.
        @see WriteRowTitlesCSV
        @param fileWriter - the file object
        @param withDeviceInfo - boolean. Flag indicating if the deice info is available as well for writing
     */
    public void WriteRowCSV(FileWriter fileWriter, boolean withDeviceInfo, boolean withDeviceName, boolean withAsset) throws IOException {

        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm");
        if (fileWriter != null) {
            if (withDeviceName) {
                fileWriter.write(deviceName);
                fileWriter.write(",");
                fileWriter.write(deviceModel);
                fileWriter.write(",");
            }
            if (withAsset) {
                fileWriter.write(assetType);
                fileWriter.write(",");
                if (assetDate != null) {
                    fileWriter.write(simpleDateFormat.format(assetDate));     // MM/dd/yyyy HH:mm:ss
                } else {
                    fileWriter.write("");
                }
                fileWriter.write(",");
            }
            if (withDeviceInfo) {
                fileWriter.write(deviceParentCustomer);
                fileWriter.write(",");
                fileWriter.write(customer);
                fileWriter.write(",");
                fileWriter.write(deviceSalesOrder);
                fileWriter.write(",");
                fileWriter.write("\"" + deviceQAStatus + "\"");
                fileWriter.write(",");
                if (deviceQADate != null) {
                    fileWriter.write(simpleDateFormat.format(deviceQADate));     // MM/dd/yyyy HH:mm:ss
                } else {
                    fileWriter.write("");
                }
                fileWriter.write(",");
                fileWriter.write(forwardURLCSV);
                fileWriter.write(",");
                fileWriter.write(forwardMethodCSV);
                fileWriter.write(",");
            }
            fileWriter.write(eventTypeStr);
            fileWriter.write(",");
            if (maintenanceReportDate != null) {
                fileWriter.write(simpleDateFormat.format(maintenanceReportDate));
            } else {
                fileWriter.write("");
            }
            fileWriter.write(",");

            fileWriter.write(deviceID);
            fileWriter.write(",");
            fileWriter.write(simpleDateFormat.format(dateReceived));  // MM/dd/yyyy HH:mm:ss
            fileWriter.write(",");
            fileWriter.write(String.valueOf(sequenceNumInt));  // integer
            fileWriter.write(",");
            fileWriter.write(simpleDateFormat.format(dateRT));  // MM/dd/yyyy HH:mm:ss
            fileWriter.write(",");
            fileWriter.write("\"" + deviceStatusStr + "\"");  // to handle commas in a field must put in quotations
            fileWriter.write(",");

            // GPS related data
            ///////////////////
            fileWriter.write(simpleDateFormat.format(dateGPS));  // MM/dd/yyyy HH:mm:ss
            fileWriter.write(",");
            fileWriter.write(String.valueOf(latitudeDouble));  // decimal
            fileWriter.write(",");
            fileWriter.write(String.valueOf(longitudeDouble));  // decimal
            fileWriter.write(",");
            fileWriter.write(String.valueOf(altitudeFloat));  // decimal
            fileWriter.write(",");
            fileWriter.write(String.valueOf(speedMPHFloat));  // decimal
            fileWriter.write(",");
            fileWriter.write(String.valueOf(headingInt));  // decimal (angle)
            fileWriter.write(",");
            fileWriter.write(gpsStatusStr);
            fileWriter.write(",");

            // Power related data
            /////////////////////
            fileWriter.write(String.valueOf(primaryExternalPowerVFloat));        // decimal
            fileWriter.write(",");
            fileWriter.write(String.valueOf(secondaryExternalPowerVFloat));         // decimal
            fileWriter.write(",");
            fileWriter.write(String.valueOf(batteryPowerVFloat));      // decimal
            fileWriter.write(",");
            fileWriter.write(String.valueOf(solarPowerVFloat));    // decimal
            fileWriter.write(",");
            fileWriter.write(batteryPercentageStr);    // decimal
            fileWriter.write(",");

            // Satellite Data
            /////////////////
            fileWriter.write(String.valueOf(numSatsInt));   // integer
            fileWriter.write(",");
            fileWriter.write(String.valueOf(hdopFloat));      // decimal
            // horizontal dilution of precision -
            // specify the additional multiplicative effect of navigation satellite geometry
            // on positional measurement precision
            fileWriter.write(",");

            // Engine related Data
            //////////////////////
            fileWriter.write(String.valueOf(engineHoursDouble));          // decimal hours and minutes
            fileWriter.write(",");
            fileWriter.write(String.valueOf(odometerMilesFloat));         // decimal
            fileWriter.write(",");
            fileWriter.write(String.valueOf(fuelLevelPercentFloat));      // decimal
            fileWriter.write(",");
            fileWriter.write(String.valueOf(odtSinceMILMilesFloat));      // decimal
            fileWriter.write(",");
            fileWriter.write(String.valueOf(odtSinceDTCClearMilesFloat)); // decimal
            fileWriter.write(",");
            fileWriter.write(vinStr);                   // alphanumeric
            fileWriter.write(",");
            fileWriter.write(engLightStr);              // on/off
            fileWriter.write(",");
            fileWriter.write("\"" + engCodesStr + "\"");              // comma separated error codes (hex)
            fileWriter.write(",");
            fileWriter.write(String.valueOf(internalTemperatureFloat));   // decimal degrees celius
            fileWriter.write(",");
            fileWriter.write(String.valueOf(ambientTemperatureFloat));    // decimal degrees celius
            fileWriter.write(",");

            // Acceleration and Orientation data
            ////////////////////////////////////
            fileWriter.write(accelerometerInfoStr);
            fileWriter.write(String.valueOf(accelerometerXmm_s2Int));   // integer
            fileWriter.write(",");
            fileWriter.write(String.valueOf(accelerometerYmm_s2Int));   // integer
            fileWriter.write(",");
            fileWriter.write(String.valueOf(accelerometerZmm_s2Int));   // integer
            fileWriter.write(",");
            fileWriter.write(String.valueOf(accelerometerXCalibInt));   // integer
            fileWriter.write(",");
            fileWriter.write(String.valueOf(accelerometerYCalibInt));   // integer
            fileWriter.write(",");
            fileWriter.write(String.valueOf(accelerometerZCalibInt));   // integer
            fileWriter.write(",");
            fileWriter.write(orientInfoStr);
            fileWriter.write(",");
            fileWriter.write(String.valueOf(orientXInt));   // integer
            fileWriter.write(",");
            fileWriter.write(String.valueOf(orientYInt));   // integer
            fileWriter.write(",");
            fileWriter.write(String.valueOf(orientZInt));   // integer
            fileWriter.write(",");

            // Network related data
            ///////////////////////
            fileWriter.write(String.valueOf(RSSIInt));          // integer between 0 and 99 ( the higher the worse the signal to nosie ratio
            fileWriter.write(",");
            fileWriter.write(serviceTypeStr);
            fileWriter.write(",");
            fileWriter.write(roamingStr);
            fileWriter.write(",");
            fileWriter.write(countryStr);
            fileWriter.write(",");
            fileWriter.write(networkStr);
            fileWriter.write(",");
            fileWriter.write(towerIDStr);
            fileWriter.write(",");
            fileWriter.write(String.valueOf(centroidLatDouble));   // decimal
            fileWriter.write(",");
            fileWriter.write(String.valueOf(centroidLonDouble));   // decimal
            fileWriter.write(",");
            fileWriter.write(bandStr);
            fileWriter.write(",");
            fileWriter.write(RxTxEcStr);
            fileWriter.write(",");

            // Versions data
            ////////////////
            fileWriter.write(osVersionStr);
            fileWriter.write(",");
            fileWriter.write(appVersionStr);
            fileWriter.write(",");
            fileWriter.write(hwVersionStr);
            fileWriter.write(",");
            fileWriter.write(hwRevisionStr);
            fileWriter.write(",");
            fileWriter.write(ioVersionStr);
            fileWriter.write(",");
            fileWriter.write(extenderVersionStr);
            fileWriter.write(",");
            fileWriter.write(bleVersionStr);
            fileWriter.write(",");
            fileWriter.write(deviceConfigChangedStr);
            fileWriter.write(",");
            fileWriter.write(deviceConfigStr);
            fileWriter.write(",");
            fileWriter.write("\"" + configurationDescStr + "\"");
            fileWriter.write(",");
            fileWriter.write(TFTPStatusStr);
            fileWriter.write(",");

            // Peripherals data
            ///////////////////
            fileWriter.write(peripheralPortTypeStr);
            fileWriter.write(",");
            fileWriter.write(String.valueOf(peripheralPortNumInt)); // integer
            fileWriter.write(",");
            fileWriter.write(peripheralDriverStr);
            fileWriter.write(",");
            fileWriter.write("\"" + peripheralDescStr + "\"");
            fileWriter.write(",");
            fileWriter.write(peripheralModelStr);
            fileWriter.write(",");
            fileWriter.write(peripheralRevStr);
            fileWriter.write(",");
            fileWriter.write(GPIOInfoStr);
            fileWriter.write(",");

            // Sensors data
            ///////////////
            fileWriter.write("\"" + cargoSensorInfoStr + "\"");
            fileWriter.write(",");
            if (cargoDate != null) {
                fileWriter.write(simpleDateFormat.format(cargoDate));
            } else {
                fileWriter.write("NA");
            }
            fileWriter.write(",");
            fileWriter.write("\"" + lightSentryStr + "\"");
            fileWriter.write(",");
            fileWriter.write("\"" + ABSStr + "\"");
            fileWriter.write(",");
            fileWriter.write(String.valueOf(ABSOdometerFloat));
            fileWriter.write(",");
            fileWriter.write("\"" + brakeStrokeStr + "\"");
            fileWriter.write(",");
            fileWriter.write("\"" + doorStr + "\"");
            fileWriter.write(",");
            fileWriter.write("\"" + chassisMatingStr + "\"");
            fileWriter.write(",");
            fileWriter.write("\"" + TPMSStr + "\"");
            fileWriter.write(",");
            fileWriter.write("\"" + ATISStr + "\"");
            fileWriter.write(",");
            fileWriter.write("\"" + tractorPairingStr + "\"");
            fileWriter.write(",");
            fileWriter.write("\"" + gladhandStr + "\"");
            fileWriter.write(",");

            // Communication Info
            /////////////////////
            fileWriter.write(String.valueOf(ACKRetriesNumInt));    // integer normally should be 0
            fileWriter.write(",");
            fileWriter.write(rcvGpsTimeDiffStr);
            fileWriter.write(",");
            fileWriter.write(rcvRtcTimeDiffStr);
            fileWriter.write(",");
            fileWriter.write(deviceIPStr);         // xxx.xxx.xxx.xxx
            fileWriter.write(",");
            fileWriter.write("\"" + serverInfoStr + "\"");
            fileWriter.write(",");
            fileWriter.write(rawReportHexStr);
            fileWriter.write(",");

            // Post processing Info
            ///////////////////////
            fileWriter.write(String.valueOf(odoMilesDiffFloat));
            fileWriter.write(",");
            fileWriter.write(String.valueOf(hoursAtLocationInt));

            fileWriter.write("\r\n");
        }
    }


    /*
        Write the column titles for a device/report row. Must ne same order as the actual values
        written in the method above.
        @see WriteRowCSV
        @param fileWriter - the file object
        @param withDeviceInfo - boolean. Flag indicating if the deice info is available as well for writing
     */
    public static void WriteRowTitlesCSV(FileWriter fileWriter, boolean withDeviceInfo, boolean withDeviceName, boolean withAsset) throws IOException {

        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm");
        if (fileWriter != null) {
            if (withDeviceName) {
                fileWriter.write("Name/Asset");
                fileWriter.write(",");
                fileWriter.write("Model");
                fileWriter.write(",");
            }
            if (withAsset) {
                fileWriter.write("Asset Type");
                fileWriter.write(",");
                fileWriter.write("Asset Install Date");
                fileWriter.write(",");
            }
            if (withDeviceInfo) {
                fileWriter.write("Parent Customer");
                fileWriter.write(",");
                fileWriter.write("Customer");
                fileWriter.write(",");
                fileWriter.write("Sales Order");
                fileWriter.write(",");
                fileWriter.write("QA Status");
                fileWriter.write(",");
                fileWriter.write("QA Date");     // MM/dd/yyyy HH:mm:ss
                fileWriter.write(",");
                fileWriter.write("Forward IP");
                fileWriter.write(",");
                fileWriter.write("Forward Port");
                fileWriter.write(",");
                fileWriter.write("Forward Method");
                fileWriter.write(",");
            }
            fileWriter.write("Event Type");
            fileWriter.write(",");
            fileWriter.write("Maint. Report Date");
            fileWriter.write(",");
            fileWriter.write("Device ID");
            fileWriter.write(",");
            fileWriter.write("Received Date");  // MM/dd/yyyy HH:mm:ss
            fileWriter.write(",");
            fileWriter.write("Seq #");  // integer
            fileWriter.write(",");
            fileWriter.write("RT Date");  // MM/dd/yyyy HH:mm:ss
            fileWriter.write(",");
            fileWriter.write("Status");
            fileWriter.write(",");

            // GPS related data
            ///////////////////
            fileWriter.write("GPS Date");  // MM/dd/yyyy HH:mm:ss
            fileWriter.write(",");
            fileWriter.write("Latitude");  // decimal
            fileWriter.write(",");
            fileWriter.write("Longitude");  // decimal
            fileWriter.write(",");
            fileWriter.write("Altitude");  // decimal
            fileWriter.write(",");
            fileWriter.write("Speed MPH");  // decimal
            fileWriter.write(",");
            fileWriter.write("Heading Deg.");  // decimal (angle)
            fileWriter.write(",");
            fileWriter.write("GPS Status");
            fileWriter.write(",");

            // Power related data
            /////////////////////
            fileWriter.write("Primary Ext Power V");        // decimal
            fileWriter.write(",");
            fileWriter.write("Secondary Ext Power V");         // decimal
            fileWriter.write(",");
            fileWriter.write("Battery Power V");      // decimal
            fileWriter.write(",");
            fileWriter.write("Solar Power V");    // decimal
            fileWriter.write(",");
            fileWriter.write("Battery Percentage");    // decimal
            fileWriter.write(",");

            // Satellite Data
            /////////////////
            fileWriter.write("# Sats");   // integer
            fileWriter.write(",");
            fileWriter.write("HDOP");      // decimal
            // horizontal dilution of precision -
            // specify the additional multiplicative effect of navigation satellite geometry
            // on positional measurement precision
            fileWriter.write(",");

            // Engine related Data
            //////////////////////
            fileWriter.write("Engine Hours");          // decimal hours and minutes
            fileWriter.write(",");
            fileWriter.write("Odometer Miles");         // decimal
            fileWriter.write(",");
            fileWriter.write("Fuel Level %");      // decimal
            fileWriter.write(",");
            fileWriter.write("ODT Since MIL Miles");      // decimal
            fileWriter.write(",");
            fileWriter.write("ODT Since DTC Clear Miles"); // decimal
            fileWriter.write(",");
            fileWriter.write("VIN");                   // alphanumeric
            fileWriter.write(",");
            fileWriter.write("Engine Light");              // on/off
            fileWriter.write(",");
            fileWriter.write("Engine Codes");              // comma separated error codes (hex)
            fileWriter.write(",");
            fileWriter.write("Int. Temp C");   // decimal degrees celius
            fileWriter.write(",");
            fileWriter.write("Amb. Temp C");    // decimal degrees celius
            fileWriter.write(",");

            // Acceleration and Orientation data
            ////////////////////////////////////
            fileWriter.write("Accl. Info");
            fileWriter.write(",");
            fileWriter.write("Accl X mm/s^2");   // integer
            fileWriter.write(",");
            fileWriter.write("Accl Y mm/s^2");   // integer
            fileWriter.write(",");
            fileWriter.write("Accl Z mm/s^2");   // integer
            fileWriter.write(",");
            fileWriter.write("Accl X Calib.");   // integer
            fileWriter.write(",");
            fileWriter.write("Accl. Y Calib.");   // integer
            fileWriter.write(",");
            fileWriter.write("Accl. Z Calib.");   // integer
            fileWriter.write(",");
            fileWriter.write("Orient. Info");
            fileWriter.write(",");
            fileWriter.write("Orient. X");   // integer
            fileWriter.write(",");
            fileWriter.write("Orient. Y");   // integer
            fileWriter.write(",");
            fileWriter.write("Orient. Z");   // integer
            fileWriter.write(",");

            // Network related data
            ///////////////////////
            fileWriter.write("RSSI");          // integer between 0 and 99 ( the higher the worse the signal to nosie ratio
            fileWriter.write(",");
            fileWriter.write("Service");
            fileWriter.write(",");
            fileWriter.write("Roaming");
            fileWriter.write(",");
            fileWriter.write("Country");
            fileWriter.write(",");
            fileWriter.write("Network");
            fileWriter.write(",");
            fileWriter.write("Tower ID");
            fileWriter.write(",");
            fileWriter.write("Centroid Lat.");   // decimal
            fileWriter.write(",");
            fileWriter.write("Centroid Lon.");   // decimal
            fileWriter.write(",");
            fileWriter.write("Band");
            fileWriter.write(",");
            fileWriter.write("RxTxEc");
            fileWriter.write(",");

            // Versions data
            ////////////////
            fileWriter.write("OS Version");
            fileWriter.write(",");
            fileWriter.write("App Version");
            fileWriter.write(",");
            fileWriter.write("HW Version");
            fileWriter.write(",");
            fileWriter.write("HW Revision");
            fileWriter.write(",");
            fileWriter.write("IO Version");
            fileWriter.write(",");
            fileWriter.write("Extender Ver.");
            fileWriter.write(",");
            fileWriter.write("Config Changed?");
            fileWriter.write(",");
            fileWriter.write("Config ID");
            fileWriter.write(",");
            fileWriter.write("Config Desc.");
            fileWriter.write(",");
            fileWriter.write("TFTP Status");
            fileWriter.write(",");

            // Peripherals data
            ///////////////////
            fileWriter.write("Periph Port Type");
            fileWriter.write(",");
            fileWriter.write("Periph Port #"); // integer
            fileWriter.write(",");
            fileWriter.write("Periph Driver");
            fileWriter.write(",");
            fileWriter.write("Periph. Desc");
            fileWriter.write(",");
            fileWriter.write("Periph. Model");
            fileWriter.write(",");
            fileWriter.write("Periph. Rev");
            fileWriter.write(",");
            fileWriter.write("GPIO");
            fileWriter.write(",");

            // Sensors data
            ///////////////
            fileWriter.write("Cargo Sensor");
            fileWriter.write(",");
            fileWriter.write("Cargo Date");
            fileWriter.write(",");
            fileWriter.write("Lite Sentry");
            fileWriter.write(",");
            fileWriter.write("ABS");
            fileWriter.write(",");
            fileWriter.write("ABS Odometer");
            fileWriter.write(",");
            fileWriter.write("Brake Stroke");
            fileWriter.write(",");
            fileWriter.write("Door");
            fileWriter.write(",");
            fileWriter.write("Chassis Mating");
            fileWriter.write(",");
            fileWriter.write("Tires");
            fileWriter.write(",");
            fileWriter.write("Tires Inflator");
            fileWriter.write(",");

            // Communication Info
            /////////////////////
            fileWriter.write("ACK Retries #");    // integer normally should be 0
            fileWriter.write(",");
            fileWriter.write("rcvGpsTimeDiff");
            fileWriter.write(",");
            fileWriter.write("cvRtcTimeDiff");
            fileWriter.write(",");
            fileWriter.write("Device IP");         // xxx.xxx.xxx.xxx
            fileWriter.write(",");
            fileWriter.write("Server Info");
            fileWriter.write(",");
            fileWriter.write("Raw Report Hex");
            fileWriter.write(",");

            // Post processing Info
            ///////////////////////
            fileWriter.write("Miles Since Last Report");
            fileWriter.write(",");
            fileWriter.write("Time at Location");

            fileWriter.write("\r\n");
        }
    }


    /**
     * Retrieve all the necessary field names separated by commas into a string
     *
     * @return String
     */
    public static String[] GetFieldNames() {

        ArrayList<String> names = new ArrayList<String>();

        Class aClass = ReportRow.class;
        Field[] fields = aClass.getDeclaredFields();
        ArrayList<String> transFieldNames = new ArrayList<String>();

        int i = 0, j = 0;
        if ((fields != null) && (fields.length > 0)) {

            for (i = 0; i < fields.length; i++) {

                int modifiers = fields[i].getModifiers();
                if (Modifier.isPrivate(modifiers)) {
                    String name = fields[i].getName();
                    if (!name.startsWith("trans")) {
                        transFieldNames.add(j, name);
                        j++;
                        names.add(name);
                    }
                }
            }
        } else {
            transFieldNames = null;
        }

        if (names.isEmpty()) {
            return null;
        }
        return names.toArray(new String[0]);
    }

}
