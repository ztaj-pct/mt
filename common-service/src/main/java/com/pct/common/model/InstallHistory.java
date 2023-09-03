package com.pct.common.model;

import java.io.Serializable;
import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.pct.common.constant.InstallHistoryStatus;
import com.pct.common.serde.InstantDeserializer;
import com.pct.common.serde.InstantSerializer;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "install_history", catalog = "pct_installer_ms")
@Data
@Setter
@Getter
@NoArgsConstructor
@Audited
public class InstallHistory extends DateAudit implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	 @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
	@Column(name = "status", columnDefinition = "VARCHAR(255)")
	@Enumerated(value = EnumType.STRING)
	private InstallHistoryStatus status;

	@JsonSerialize(using = InstantSerializer.class)
	@JsonDeserialize(using = InstantDeserializer.class)
	@Column(name = "date_started")
	private Instant dateStarted;

	@JsonIgnoreProperties("role")
	@ManyToOne
	@JoinColumn(name = "created_by", referencedColumnName = "uuid")
	private User createdBy;

	@JsonIgnoreProperties("role")
	@ManyToOne
	@JoinColumn(name = "updated_by", referencedColumnName = "uuid")
	private User updatedBy;

	@JsonSerialize(using = InstantSerializer.class)
	@JsonDeserialize(using = InstantDeserializer.class)
	@Column(name = "date_ended")
	private Instant dateEnded;

	@ManyToOne
	@JoinColumn(name = "asset_uuid", referencedColumnName = "uuid")
	private Asset asset;

	@ManyToOne
	@JoinColumn(name = "device_uuid", referencedColumnName = "uuid")
	private Device device;

	@ManyToOne
	@JoinColumn(name = "organisation_uuid", referencedColumnName = "uuid")
	private Organisation organisation;

	@Column(name = "install_code")
	private String installCode;

	@Column(name = "uuid")
	private String uuid;

	@Column(name = "app_version")
	private String appVersion;
}
