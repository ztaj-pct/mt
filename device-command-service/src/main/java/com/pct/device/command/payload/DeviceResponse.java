package com.pct.device.command.payload;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.pct.common.constant.Constants;

import lombok.Getter;
import lombok.Setter;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Setter
@Getter
public class DeviceResponse implements Serializable {

	// fields to be saved in the database
	/////////////////////////////////////
	private long recordID;

	@JsonProperty("gateway_id")
	private String deviceID = null;
	private Timestamp commandSentTimestampPST;
	@JsonProperty("at_command")
	private String commandSent = null;
	private String responseReceived = null;
	private String rawResponse = null;

	private boolean transIsExistsInDB = false;

	public long getRecordID() {
		return recordID;
	}

	public void setRecordID(long recordID) {
		this.recordID = recordID;
	}

	public String getDeviceID() {

		return deviceID;
	}

	public void setDeviceID(String deviceID) {

		
		this.deviceID = deviceID;
	}

	public Timestamp getCommandSentTimestampPST() {
		return commandSentTimestampPST;
	}

	

	
	public String getCommandSent() {
		return commandSent;
	}

	

	public String getResponseReceived() {
		return responseReceived;
	}

	public void setResponseReceived(String responseReceived) {
		if (responseReceived != null) {
			responseReceived = responseReceived.trim().toLowerCase();
		}
		this.responseReceived = responseReceived;
	}

	public String getRawResponse() {
		return rawResponse;
	}

	public void setRawResponse(String rawResponse) {
		this.rawResponse = rawResponse;
	}

	// transient fields not saved in DB
	///////////////////////////////////////////////////////////////////////////////////////////
	public boolean isTransIsExistsInDB() {
		return transIsExistsInDB;
	}

	public void setTransIsExistsInDB(boolean isExistsInDB) {
		this.transIsExistsInDB = isExistsInDB;
	}

	/////////////////////////////////////////////////////// Utilities
	/////////////////////////////////////////////////////// //////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Retrieve all the necessary field names separated by commas into a string
	 * 
	 * @return String
	 */
	public static String GetFieldNames() {

		StringBuilder sb = new StringBuilder(",");

		Class aClass = DeviceResponse.class;
		Field[] fields = aClass.getDeclaredFields();

		String result = null;

		int i = 0, j = 0;
		if ((fields != null) && (fields.length > 0)) {

			for (i = 0; i < fields.length; i++) {

				int modifiers = fields[i].getModifiers();
				if (Modifier.isPrivate(modifiers)) {
					String name = fields[i].getName();

					if (!name.startsWith("trans")) {
						sb.append(name);
						sb.append(",");
					}
				}
			}
			result = sb.toString();
		} else {
			result = null;
		}

		return result;
	}

	/**
	 * Convert the required field values to Strings separated by commas
	 * 
	 * @return String
	 */
	public String ToString() {

		StringBuilder sb = new StringBuilder(",");

		sb.append(recordID);
		sb.append(",");
		sb.append(StringUtils.defaultString(deviceID));
		sb.append(",");
		sb.append(commandSentTimestampPST);
		sb.append(",");
		sb.append(StringUtils.defaultString(commandSent));
		sb.append(",");
		sb.append(StringUtils.defaultString(responseReceived));
		sb.append(",");
		sb.append(StringUtils.defaultString(rawResponse));

		return sb.toString();
	}

	/**
	 * De-serialization (=constructor). All setters do their own validation if
	 * required so no need for validating here at the final state of the
	 * de-serialized object.
	 * 
	 * @param aInputStream - ObjectInputStream holding the handler to the file from
	 *                     which the serialized data will be read.
	 * @throws ClassNotFoundException IOException
	 */
	private void readObject(ObjectInputStream aInputStream) throws ClassNotFoundException, IOException {
		// always perform the default de-serialization first
		aInputStream.defaultReadObject();

	}

	/**
	 * Serialization
	 * 
	 * @param aOutputStream - ObjectOutputStream
	 */
	private void writeObject(ObjectOutputStream aOutputStream) throws IOException {
		// perform the default serialization for all non-transient, non-static fields
		aOutputStream.defaultWriteObject();
	}
}
