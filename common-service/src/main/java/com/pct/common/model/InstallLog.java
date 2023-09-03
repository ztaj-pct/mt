package com.pct.common.model;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.hibernate.envers.RelationTargetAuditMode;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.pct.common.constant.EventType;
import com.pct.common.constant.InstallLogStatus;
import com.pct.common.model.DateAudit;
import com.pct.common.model.Device;
import com.pct.common.model.InstallHistory;
import com.pct.common.model.ReasonCode;
import com.pct.common.model.SensorDetail;
//import com.pct.common.model.Sensor;
import com.pct.common.model.User;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Audited
@Data
@Setter
@Getter
@NoArgsConstructor
@Table(name = "install_log", catalog = "pct_installer_ms")
public class InstallLog extends DateAudit implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	
	@ManyToOne
	@JoinColumn(name = "install_code", referencedColumnName = "install_code")
	private InstallHistory installHistory;
	
	@NotAudited
	@ManyToOne
	@JoinColumn(name = "sensor_detail_uuid", referencedColumnName = "uuid")
	private SensorDetail sensorDetail;


	@Column(name = "status", columnDefinition = "VARCHAR(255)")
	@Enumerated(value = EnumType.STRING)
	private InstallLogStatus status;

	@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
	@ManyToOne
	@JoinColumn(name = "reason_code_id", referencedColumnName = "id")
	private ReasonCode reasonCode;

	@Column(name = "installer_comments", columnDefinition = "VARCHAR(255)")
	private String installerComments;

	@Column(name = "timestamp")
	private Instant timestamp;

    @JsonIgnoreProperties("role")
    @ManyToOne
    @JoinColumn(name = "created_by", referencedColumnName = "uuid")
    private User createdBy;

    @JsonIgnoreProperties("role")
    @ManyToOne
    @JoinColumn(name = "updated_by", referencedColumnName = "uuid")
    private User updatedBy;
    
	@ManyToOne
	@JoinColumn(name = "sensor_uuid", referencedColumnName = "uuid")
	private Device sensor;

	@Column(name = "data")
	private String data;

	@ManyToOne
	@JoinColumn(name = "installer_uuid", referencedColumnName = "uuid")
	private User user;

	@Enumerated(EnumType.STRING)
	@Column(name = "event_type")
	private EventType eventType;

	@Column(name = "instance_type")
	private String instanceType;

	@Column(name = "sensor_id")
	private String sensorId;

	@Column(name = "door_type")
	private String type;

	@Column(name = "sensor_temperature")
	private String sensorTemperature;

	@Column(name = "sensor_pressure")
	private String sensorPressure;

//	public Long getId() {
//		return id;
//	}
//
//	public void setId(Long id) {
//		this.id = id;
//	}
//
//	public InstallHistory getInstallHistory() {
//		return installHistory;
//	}
//
//	public void setInstallHistory(InstallHistory installHistory) {
//		this.installHistory = installHistory;
//	}
//
//	public InstallLogStatus getStatus() {
//		return status;
//	}
//
//	public void setStatus(InstallLogStatus status) {
//		this.status = status;
//	}
//
//	public ReasonCode getReasonCode() {
//		return reasonCode;
//	}
//
//	public void setReasonCode(ReasonCode reasonCode) {
//		this.reasonCode = reasonCode;
//	}
//
//	public String getInstallerComments() {
//		return installerComments;
//	}
//
//	public void setInstallerComments(String installerComments) {
//		this.installerComments = installerComments;
//	}
//
//	public Instant getTimestamp() {
//		return timestamp;
//	}
//
//	public void setTimestamp(Instant timestamp) {
//		this.timestamp = timestamp;
//	}
//
////	public Sensor getSensor() {
////		return sensor;
////	}
////
////	public void setSensor(Sensor sensor) {
////		this.sensor = sensor;
////	}
//
//	public String getData() {
//		return data;
//	}
//
//	public void setData(String data) {
//		this.data = data;
//	}
//
//	public User getUser() {
//		return user;
//	}
//
//	public void setUser(User user) {
//		this.user = user;
//	}
//
//	public EventType getEventType() {
//		return eventType;
//	}
//
//	public void setEventType(EventType eventType) {
//		this.eventType = eventType;
//	}
//
//	public String getInstanceType() {
//		return instanceType;
//	}
//
//	public void setInstanceType(String instanceType) {
//		this.instanceType = instanceType;
//	}
//
//	public String getSensorId() {
//		return sensorId;
//	}
//
//	public void setSensorId(String sensorId) {
//		this.sensorId = sensorId;
//	}
//
//	public String getType() {
//		return type;
//	}
//
//	public void setType(String type) {
//		this.type = type;
//	}
//
//	public String getSensorTemperature() {
//		return sensorTemperature;
//	}
//
//	public void setSensorTemperature(String sensorTemperature) {
//		this.sensorTemperature = sensorTemperature;
//	}
//
//	public String getSensorPressure() {
//		return sensorPressure;
//	}
//
//	public void setSensorPressure(String sensorPressure) {
//		this.sensorPressure = sensorPressure;
//	}
//
//	public SensorDetail getSensorDetail() {
//		return sensorDetail;
//	}
//
//	public void setSensorDetail(SensorDetail sensorDetail) {
//		this.sensorDetail = sensorDetail;
//	}
//
//	public User getCreatedBy() {
//		return createdBy;
//	}
//
//	public void setCreatedBy(User createdBy) {
//		this.createdBy = createdBy;
//	}
//
//	public User getUpdatedBy() {
//		return updatedBy;
//	}
//
//	public void setUpdatedBy(User updatedBy) {
//		this.updatedBy = updatedBy;
//	}
//
//	public Device getSensor() {
//		return sensor;
//	}
//
//	public void setSensor(Device sensor) {
//		this.sensor = sensor;
//	}



}
