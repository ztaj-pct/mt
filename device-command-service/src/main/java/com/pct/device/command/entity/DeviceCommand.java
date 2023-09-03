package com.pct.device.command.entity;

import java.io.Serializable;
import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.pct.common.model.User;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "device_command")
@Getter
@Setter
@NoArgsConstructor
public class DeviceCommand implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@CreatedDate
	@Column(name = "created_date")
	private Instant createdDate;

	@Column(name = "created_epoch")
	private Instant CreatedEpoch;

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
	private boolean is_success;

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
	
	
	private String status;
	
	@Column(name = "created_by")
	private String createdBy;

	@Column(name = "updated_by")
	private String updatedBy;
	
	@Column(name = "device_raw_response")
	private String deviceRawResponse;

}
