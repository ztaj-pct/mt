package com.pct.device.command.entity;

import java.io.Serializable;
import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Immutable;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

@Entity
@Immutable
@Table(name = "device_command_response")
public class DeviceCommandResponse implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@CreatedDate
	@Column(name = "created_date")
	private Instant createdDate;

	@Column(name = "created_epoch")
	private Instant createdEpoch;

	@LastModifiedDate
	@Column(name = "updated_date")
	private Instant updatedDate;

	@Column(name = "uuid")
	private String uuid;

	@Column(name = "device_id")
	private String deviceId;

	@Column(name = "at_command")
	private String atCommand;

	@Column(name = "source")
	private String source;

	@Column(name = "priority")
	private String priority;

	@Column(name = "sent_timestamp")
	private Instant sentTimestamp;
	
	@Column(name = "response_timestamp")
	private Instant responseTimestamp;

	@Column(name = "success")
	private boolean isSuccess;

	@Column(name = "device_ip")
	private String deviceIp;

	@Column(name = "device_port")
	private int devicePort;

	@Column(name = "device_response")
	private String deviceResponse;

	@Column(name = "retry_count")
	private int retryCount;

	@Column(name = "raw_report")
	private String rawReport;

	@Column(name = "server_ip")
	private String serverIp;

	@Column(name = "server_port")
	private int serverPort;

	@Column(name = "response_server_ip")
	private String responseServerIp;

	@Column(name = "response_server_port")
	private int responseServerPort;
	
	@Column(name = "status")
	private String status;
	
	@Column(name = "created_by")
	private String createdBy;

	@Column(name = "updated_by")
	private String updatedBy;
	
	@Column(name = "device_raw_response")
	private String deviceRawResponse;
	
	@Column(name = "response_from")
	private String responseFrom;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Instant getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Instant createdDate) {
		this.createdDate = createdDate;
	}

	public Instant getCreatedEpoch() {
		return createdEpoch;
	}

	public void setCreatedEpoch(Instant createdEpoch) {
		this.createdEpoch = createdEpoch;
	}

	public Instant getUpdatedDate() {
		return updatedDate;
	}

	public void setUpdatedDate(Instant updatedDate) {
		this.updatedDate = updatedDate;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public String getAtCommand() {
		return atCommand;
	}

	public void setAtCommand(String atCommand) {
		this.atCommand = atCommand;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getPriority() {
		return priority;
	}

	public void setPriority(String priority) {
		this.priority = priority;
	}

	public Instant getSentTimestamp() {
		return sentTimestamp;
	}

	public void setSentTimestamp(Instant sentTimestamp) {
		this.sentTimestamp = sentTimestamp;
	}

	public Instant getResponseTimestamp() {
		return responseTimestamp;
	}

	public void setResponseTimestamp(Instant responseTimestamp) {
		this.responseTimestamp = responseTimestamp;
	}

	public boolean isSuccess() {
		return isSuccess;
	}

	public void setSuccess(boolean isSuccess) {
		this.isSuccess = isSuccess;
	}

	public String getDeviceIp() {
		return deviceIp;
	}

	public void setDeviceIp(String deviceIp) {
		this.deviceIp = deviceIp;
	}

	public int getDevicePort() {
		return devicePort;
	}

	public void setDevicePort(int devicePort) {
		this.devicePort = devicePort;
	}

	public String getDeviceResponse() {
		return deviceResponse;
	}

	public void setDeviceResponse(String deviceResponse) {
		this.deviceResponse = deviceResponse;
	}

	public int getRetryCount() {
		return retryCount;
	}

	public void setRetryCount(int retryCount) {
		this.retryCount = retryCount;
	}

	public String getRawReport() {
		return rawReport;
	}

	public void setRawReport(String rawReport) {
		this.rawReport = rawReport;
	}

	public String getServerIp() {
		return serverIp;
	}

	public void setServerIp(String serverIp) {
		this.serverIp = serverIp;
	}

	public int getServerPort() {
		return serverPort;
	}

	public void setServerPort(int serverPort) {
		this.serverPort = serverPort;
	}

	public String getResponseServerIp() {
		return responseServerIp;
	}

	public void setResponseServerIp(String responseServerIp) {
		this.responseServerIp = responseServerIp;
	}

	public int getResponseServerPort() {
		return responseServerPort;
	}

	public void setResponseServerPort(int responseServerPort) {
		this.responseServerPort = responseServerPort;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public String getUpdatedBy() {
		return updatedBy;
	}

	public void setUpdatedBy(String updatedBy) {
		this.updatedBy = updatedBy;
	}

	public String getDeviceRawResponse() {
		return deviceRawResponse;
	}

	public void setDeviceRawResponse(String deviceRawResponse) {
		this.deviceRawResponse = deviceRawResponse;
	}

	public String getResponseFrom() {
		return responseFrom;
	}

	public void setResponseFrom(String responseFrom) {
		this.responseFrom = responseFrom;
	}

	
}
