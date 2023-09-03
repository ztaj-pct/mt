package com.pct.device.command.entity;

import java.io.Serializable;
import javax.persistence.*;
import java.sql.Timestamp;


/**
 * The persistent class for the devicereport database table.
 * 
 */
@Table(name = "DeviceReport")
@Entity
public class Devicereport implements Serializable {
	private static final long serialVersionUID = 1L;

	@EmbeddedId
	private DevicereportPK id;

	@Column(name="ACCELEROMETER_INFO")
	private String accelerometerInfo;

	@Column(name="ACCELEROMETER_X_CALIB")
	private Integer accelerometerXCalib;

	@Column(name="ACCELEROMETER_X_MMS2")
	private Integer accelerometerXMms2;

	@Column(name="ACCELEROMETER_X_RAW")
	private Integer accelerometerXRaw;

	@Column(name="ACCELEROMETER_Y_CALIB")
	private Integer accelerometerYCalib;

	@Column(name="ACCELEROMETER_Y_MMS2")
	private Integer accelerometerYMms2;

	@Column(name="ACCELEROMETER_Y_RAW")
	private Integer accelerometerYRaw;

	@Column(name="ACCELEROMETER_Z_CALIB")
	private Integer accelerometerZCalib;

	@Column(name="ACCELEROMETER_Z_MMS2")
	private Integer accelerometerZMms2;

	@Column(name="ACCELEROMETER_Z_RAW")
	private Integer accelerometerZRaw;

	@Column(name="ALTITUDE_FEET")
	private Float altitudeFeet;

	@Column(name="AMBIENT_TEMPERATURE")
	private Float ambientTemperature;

	@Column(name="APP_SW_VERSION")
	private String appSwVersion;

	@Column(name="AUX_POWER")
	private Float auxPower;

	@Column(name="BACKUP_POWER")
	private Float backupPower;

	@Column(name="BASEBAND_SW_VERSION")
	private String basebandSwVersion;

	@Column(name="CARGO_INFO")
	private byte[] cargoInfo;

	@Column(name="CARGO_TIME")
	private Timestamp cargoTime;

	@Column(name="CELLULAR_BAND")
	private String cellularBand;

	@Column(name="CHARGING_POWER")
	private Float chargingPower;

	@Column(name="CONFIG_CHANGED")
	private String configChanged;

	@Column(name="CONFIG_DESC")
	private String configDesc;

	@Column(name="CONFIG_VERSION")
	private Integer configVersion;

//	@Column(name="CUSTOMER_REPORT_COUNTER")
//	private Integer customerReportCounter;

	@Column(name="DEBUG_FIELDS")
	private byte[] debugFields;

	@Column(name="DEVICE_IP")
	private String deviceIp;

	@Column(name="DEVICE_PORT")
	private Integer devicePort;

	@Column(name="DEVICE_TAG")
	private String deviceTag;

	@Column(name="DIAGNOSTIC_CODES")
	private String diagnosticCodes;

	@Column(name="DIAGNOSTIC_LIGHT")
	private Integer diagnosticLight;

	@Column(name="ENGINE_MINUTES")
	private Integer engineMinutes;

	@Column(name="EVENT_TYPE")
	private String eventType;

	@Column(name="EXTENDER_VERSION")
	private String extenderVersion;

	private Integer gpio;

	@Column(name="GPIO_DIRECTION")
	private Integer gpioDirection;

	@Column(name="GPIO_INFO")
	private byte[] gpioInfo;

	@Column(name="GPS_STATUS")
	private String gpsStatus;

	@Column(name="GPS_TIME")
	private Timestamp gpsTime;

	private Float hdop;

	private Integer heading;

	@Column(name="HW_REVISION")
	private Integer hwRevision;

	@Column(name="HW_VERSION")
	private String hwVersion;

	@Column(name="INTERNAL_TEMPERATURE")
	private Float internalTemperature;

	@Column(name="IO_VERSION")
	private Integer ioVersion;

	private Double latitude;

	private Double longitude;

	@Column(name="MAIN_POWER")
	private Float mainPower;

	@Column(name="NEIGHBORING_NETWORKS")
	private byte[] neighboringNetworks;

	@Column(name="NMEA_DATA")
	private byte[] nmeaData;

	@Column(name="NUM_SATELLITES")
	private Integer numSatellites;

	@Column(name="ODOMETER_MILES")
	private Float odometerMiles;

	@Column(name="PERIPHERAL_DESC")
	private String peripheralDesc;

	@Column(name="PERIPHERAL_DRIVER")
	private Integer peripheralDriver;

	@Column(name="PERIPHERAL_MODEL")
	private String peripheralModel;

	@Column(name="PERIPHERAL_PORT_NUM")
	private Integer peripheralPortNum;

	@Column(name="PERIPHERAL_PORT_TYPE")
	private String peripheralPortType;

	@Column(name="PERIPHERAL_REV")
	private String peripheralRev;

	@Column(name="RAW_REPORT")
	private String rawReport;

	@Column(name="REMOTE_START_RESPONSE")
	private String remoteStartResponse;

	@Column(name="REMOTE_START_RUN_SEC")
	private Integer remoteStartRunSec;

	@Column(name="REMOTE_START_STATUS")
	private String remoteStartStatus;

	@Column(name="REMOTE_START_YELLOW_ALARM")
	private Integer remoteStartYellowAlarm;

	private String roaming;

	private Integer rssi;

	@Column(name="RT_CLOCK")
	private Timestamp rtClock;

	@Column(name="RX_TX_EC")
	private String rxTxEc;

	@Column(name="SEQUENCE_NUM")
	private Integer sequenceNum;

	@Column(name="SERVER_IP")
	private String serverIp;

	@Column(name="SERVER_PORT")
	private Integer serverPort;

	@Column(name="SERVICE_COUNTRY")
	private String serviceCountry;

	@Column(name="SERVICE_NETWORK")
	private String serviceNetwork;

	@Column(name="SERVICE_TYPE")
	private String serviceType;

	@Column(name="SPEED_MPH")
	private Float speedMph;

	private Integer temperature;

	@Column(name="TFTP_STATUS")
	private byte[] tftpStatus;

	@Column(name="TOWER_CENTROID_LAT")
	private Double towerCentroidLat;

	@Column(name="TOWER_CENTROID_LON")
	private Double towerCentroidLon;

	@Column(name="TOWER_ID")
	private String towerId;

	private String vin;

	public Devicereport() {
	}

	public DevicereportPK getId() {
		return this.id;
	}

	public void setId(DevicereportPK id) {
		this.id = id;
	}

	public String getAccelerometerInfo() {
		return this.accelerometerInfo;
	}

	public void setAccelerometerInfo(String accelerometerInfo) {
		this.accelerometerInfo = accelerometerInfo;
	}

	public Integer getAccelerometerXCalib() {
		return this.accelerometerXCalib;
	}

	public void setAccelerometerXCalib(Integer accelerometerXCalib) {
		this.accelerometerXCalib = accelerometerXCalib;
	}

	public Integer getAccelerometerXMms2() {
		return this.accelerometerXMms2;
	}

	public void setAccelerometerXMms2(Integer accelerometerXMms2) {
		this.accelerometerXMms2 = accelerometerXMms2;
	}

	public Integer getAccelerometerXRaw() {
		return this.accelerometerXRaw;
	}

	public void setAccelerometerXRaw(Integer accelerometerXRaw) {
		this.accelerometerXRaw = accelerometerXRaw;
	}

	public Integer getAccelerometerYCalib() {
		return this.accelerometerYCalib;
	}

	public void setAccelerometerYCalib(Integer accelerometerYCalib) {
		this.accelerometerYCalib = accelerometerYCalib;
	}

	public Integer getAccelerometerYMms2() {
		return this.accelerometerYMms2;
	}

	public void setAccelerometerYMms2(Integer accelerometerYMms2) {
		this.accelerometerYMms2 = accelerometerYMms2;
	}

	public Integer getAccelerometerYRaw() {
		return this.accelerometerYRaw;
	}

	public void setAccelerometerYRaw(Integer accelerometerYRaw) {
		this.accelerometerYRaw = accelerometerYRaw;
	}

	public Integer getAccelerometerZCalib() {
		return this.accelerometerZCalib;
	}

	public void setAccelerometerZCalib(Integer accelerometerZCalib) {
		this.accelerometerZCalib = accelerometerZCalib;
	}

	public Integer getAccelerometerZMms2() {
		return this.accelerometerZMms2;
	}

	public void setAccelerometerZMms2(Integer accelerometerZMms2) {
		this.accelerometerZMms2 = accelerometerZMms2;
	}

	public Integer getAccelerometerZRaw() {
		return this.accelerometerZRaw;
	}

	public void setAccelerometerZRaw(Integer accelerometerZRaw) {
		this.accelerometerZRaw = accelerometerZRaw;
	}

	public Float getAltitudeFeet() {
		return this.altitudeFeet;
	}

	public void setAltitudeFeet(Float altitudeFeet) {
		this.altitudeFeet = altitudeFeet;
	}

	public Float getAmbientTemperature() {
		return this.ambientTemperature;
	}

	public void setAmbientTemperature(Float ambientTemperature) {
		this.ambientTemperature = ambientTemperature;
	}

	public String getAppSwVersion() {
		return this.appSwVersion;
	}

	public void setAppSwVersion(String appSwVersion) {
		this.appSwVersion = appSwVersion;
	}

	public Float getAuxPower() {
		return this.auxPower;
	}

	public void setAuxPower(Float auxPower) {
		this.auxPower = auxPower;
	}

	public Float getBackupPower() {
		return this.backupPower;
	}

	public void setBackupPower(Float backupPower) {
		this.backupPower = backupPower;
	}

	public String getBasebandSwVersion() {
		return this.basebandSwVersion;
	}

	public void setBasebandSwVersion(String basebandSwVersion) {
		this.basebandSwVersion = basebandSwVersion;
	}

	public byte[] getCargoInfo() {
		return this.cargoInfo;
	}

	public void setCargoInfo(byte[] cargoInfo) {
		this.cargoInfo = cargoInfo;
	}

	public Timestamp getCargoTime() {
		return this.cargoTime;
	}

	public void setCargoTime(Timestamp cargoTime) {
		this.cargoTime = cargoTime;
	}

	public String getCellularBand() {
		return this.cellularBand;
	}

	public void setCellularBand(String cellularBand) {
		this.cellularBand = cellularBand;
	}

	public Float getChargingPower() {
		return this.chargingPower;
	}

	public void setChargingPower(Float chargingPower) {
		this.chargingPower = chargingPower;
	}

	public String getConfigChanged() {
		return this.configChanged;
	}

	public void setConfigChanged(String configChanged) {
		this.configChanged = configChanged;
	}

	public String getConfigDesc() {
		return this.configDesc;
	}

	public void setConfigDesc(String configDesc) {
		this.configDesc = configDesc;
	}

	public Integer getConfigVersion() {
		return this.configVersion;
	}

	public void setConfigVersion(Integer configVersion) {
		this.configVersion = configVersion;
	}

//	public Integer getCustomerReportCounter() {
//		return this.customerReportCounter;
//	}
//
//	public void setCustomerReportCounter(Integer customerReportCounter) {
//		this.customerReportCounter = customerReportCounter;
//	}

	public byte[] getDebugFields() {
		return this.debugFields;
	}

	public void setDebugFields(byte[] debugFields) {
		this.debugFields = debugFields;
	}

	public String getDeviceIp() {
		return this.deviceIp;
	}

	public void setDeviceIp(String deviceIp) {
		this.deviceIp = deviceIp;
	}

	public Integer getDevicePort() {
		return this.devicePort;
	}

	public void setDevicePort(Integer devicePort) {
		this.devicePort = devicePort;
	}

	public String getDeviceTag() {
		return this.deviceTag;
	}

	public void setDeviceTag(String deviceTag) {
		this.deviceTag = deviceTag;
	}

	public String getDiagnosticCodes() {
		return this.diagnosticCodes;
	}

	public void setDiagnosticCodes(String diagnosticCodes) {
		this.diagnosticCodes = diagnosticCodes;
	}

	public Integer getDiagnosticLight() {
		return this.diagnosticLight;
	}

	public void setDiagnosticLight(Integer diagnosticLight) {
		this.diagnosticLight = diagnosticLight;
	}

	public Integer getEngineMinutes() {
		return this.engineMinutes;
	}

	public void setEngineMinutes(Integer engineMinutes) {
		this.engineMinutes = engineMinutes;
	}

	public String getEventType() {
		return this.eventType;
	}

	public void setEventType(String eventType) {
		this.eventType = eventType;
	}

	public String getExtenderVersion() {
		return this.extenderVersion;
	}

	public void setExtenderVersion(String extenderVersion) {
		this.extenderVersion = extenderVersion;
	}

	public Integer getGpio() {
		return this.gpio;
	}

	public void setGpio(Integer gpio) {
		this.gpio = gpio;
	}

	public Integer getGpioDirection() {
		return this.gpioDirection;
	}

	public void setGpioDirection(Integer gpioDirection) {
		this.gpioDirection = gpioDirection;
	}

	public byte[] getGpioInfo() {
		return this.gpioInfo;
	}

	public void setGpioInfo(byte[] gpioInfo) {
		this.gpioInfo = gpioInfo;
	}

	public String getGpsStatus() {
		return this.gpsStatus;
	}

	public void setGpsStatus(String gpsStatus) {
		this.gpsStatus = gpsStatus;
	}

	public Timestamp getGpsTime() {
		return this.gpsTime;
	}

	public void setGpsTime(Timestamp gpsTime) {
		this.gpsTime = gpsTime;
	}

	public Float getHdop() {
		return this.hdop;
	}

	public void setHdop(Float hdop) {
		this.hdop = hdop;
	}

	public Integer getHeading() {
		return this.heading;
	}

	public void setHeading(Integer heading) {
		this.heading = heading;
	}

	public Integer getHwRevision() {
		return this.hwRevision;
	}

	public void setHwRevision(Integer hwRevision) {
		this.hwRevision = hwRevision;
	}

	public String getHwVersion() {
		return this.hwVersion;
	}

	public void setHwVersion(String hwVersion) {
		this.hwVersion = hwVersion;
	}

	public Float getInternalTemperature() {
		return this.internalTemperature;
	}

	public void setInternalTemperature(Float internalTemperature) {
		this.internalTemperature = internalTemperature;
	}

	public Integer getIoVersion() {
		return this.ioVersion;
	}

	public void setIoVersion(Integer ioVersion) {
		this.ioVersion = ioVersion;
	}

	public Double getLatitude() {
		return this.latitude;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	public Double getLongitude() {
		return this.longitude;
	}

	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

	public Float getMainPower() {
		return this.mainPower;
	}

	public void setMainPower(Float mainPower) {
		this.mainPower = mainPower;
	}

	public byte[] getNeighboringNetworks() {
		return this.neighboringNetworks;
	}

	public void setNeighboringNetworks(byte[] neighboringNetworks) {
		this.neighboringNetworks = neighboringNetworks;
	}

	public byte[] getNmeaData() {
		return this.nmeaData;
	}

	public void setNmeaData(byte[] nmeaData) {
		this.nmeaData = nmeaData;
	}

	public Integer getNumSatellites() {
		return this.numSatellites;
	}

	public void setNumSatellites(Integer numSatellites) {
		this.numSatellites = numSatellites;
	}

	public Float getOdometerMiles() {
		return this.odometerMiles;
	}

	public void setOdometerMiles(Float odometerMiles) {
		this.odometerMiles = odometerMiles;
	}

	public String getPeripheralDesc() {
		return this.peripheralDesc;
	}

	public void setPeripheralDesc(String peripheralDesc) {
		this.peripheralDesc = peripheralDesc;
	}

	public Integer getPeripheralDriver() {
		return this.peripheralDriver;
	}

	public void setPeripheralDriver(Integer peripheralDriver) {
		this.peripheralDriver = peripheralDriver;
	}

	public String getPeripheralModel() {
		return this.peripheralModel;
	}

	public void setPeripheralModel(String peripheralModel) {
		this.peripheralModel = peripheralModel;
	}

	public Integer getPeripheralPortNum() {
		return this.peripheralPortNum;
	}

	public void setPeripheralPortNum(Integer peripheralPortNum) {
		this.peripheralPortNum = peripheralPortNum;
	}

	public String getPeripheralPortType() {
		return this.peripheralPortType;
	}

	public void setPeripheralPortType(String peripheralPortType) {
		this.peripheralPortType = peripheralPortType;
	}

	public String getPeripheralRev() {
		return this.peripheralRev;
	}

	public void setPeripheralRev(String peripheralRev) {
		this.peripheralRev = peripheralRev;
	}

	public String getRawReport() {
		return this.rawReport;
	}

	public void setRawReport(String rawReport) {
		this.rawReport = rawReport;
	}

	public String getRemoteStartResponse() {
		return this.remoteStartResponse;
	}

	public void setRemoteStartResponse(String remoteStartResponse) {
		this.remoteStartResponse = remoteStartResponse;
	}

	public Integer getRemoteStartRunSec() {
		return this.remoteStartRunSec;
	}

	public void setRemoteStartRunSec(Integer remoteStartRunSec) {
		this.remoteStartRunSec = remoteStartRunSec;
	}

	public String getRemoteStartStatus() {
		return this.remoteStartStatus;
	}

	public void setRemoteStartStatus(String remoteStartStatus) {
		this.remoteStartStatus = remoteStartStatus;
	}

	public Integer getRemoteStartYellowAlarm() {
		return this.remoteStartYellowAlarm;
	}

	public void setRemoteStartYellowAlarm(Integer remoteStartYellowAlarm) {
		this.remoteStartYellowAlarm = remoteStartYellowAlarm;
	}

	public String getRoaming() {
		return this.roaming;
	}

	public void setRoaming(String roaming) {
		this.roaming = roaming;
	}

	public Integer getRssi() {
		return this.rssi;
	}

	public void setRssi(Integer rssi) {
		this.rssi = rssi;
	}

	public Timestamp getRtClock() {
		return this.rtClock;
	}

	public void setRtClock(Timestamp rtClock) {
		this.rtClock = rtClock;
	}

	public String getRxTxEc() {
		return this.rxTxEc;
	}

	public void setRxTxEc(String rxTxEc) {
		this.rxTxEc = rxTxEc;
	}

	public Integer getSequenceNum() {
		return this.sequenceNum;
	}

	public void setSequenceNum(Integer sequenceNum) {
		this.sequenceNum = sequenceNum;
	}

	public String getServerIp() {
		return this.serverIp;
	}

	public void setServerIp(String serverIp) {
		this.serverIp = serverIp;
	}

	public Integer getServerPort() {
		return this.serverPort;
	}

	public void setServerPort(Integer serverPort) {
		this.serverPort = serverPort;
	}

	public String getServiceCountry() {
		return this.serviceCountry;
	}

	public void setServiceCountry(String serviceCountry) {
		this.serviceCountry = serviceCountry;
	}

	public String getServiceNetwork() {
		return this.serviceNetwork;
	}

	public void setServiceNetwork(String serviceNetwork) {
		this.serviceNetwork = serviceNetwork;
	}

	public String getServiceType() {
		return this.serviceType;
	}

	public void setServiceType(String serviceType) {
		this.serviceType = serviceType;
	}

	public Float getSpeedMph() {
		return this.speedMph;
	}

	public void setSpeedMph(Float speedMph) {
		this.speedMph = speedMph;
	}

	public Integer getTemperature() {
		return this.temperature;
	}

	public void setTemperature(Integer temperature) {
		this.temperature = temperature;
	}

	public byte[] getTftpStatus() {
		return this.tftpStatus;
	}

	public void setTftpStatus(byte[] tftpStatus) {
		this.tftpStatus = tftpStatus;
	}

	public Double getTowerCentroidLat() {
		return this.towerCentroidLat;
	}

	public void setTowerCentroidLat(Double towerCentroidLat) {
		this.towerCentroidLat = towerCentroidLat;
	}

	public Double getTowerCentroidLon() {
		return this.towerCentroidLon;
	}

	public void setTowerCentroidLon(Double towerCentroidLon) {
		this.towerCentroidLon = towerCentroidLon;
	}

	public String getTowerId() {
		return this.towerId;
	}

	public void setTowerId(String towerId) {
		this.towerId = towerId;
	}

	public String getVin() {
		return this.vin;
	}

	public void setVin(String vin) {
		this.vin = vin;
	}

}