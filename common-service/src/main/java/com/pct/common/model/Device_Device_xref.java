package com.pct.common.model;

import java.time.Instant;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.envers.Audited;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "device_device_xref", catalog = "pct_device")
@NoArgsConstructor
public class Device_Device_xref extends UserDateAudit {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "date_created")
	Instant dateCreated;

	@Column(name = "date_deleted")
	Instant dateDeleted;

	@Column(name = "is_active")
	private boolean isActive;

	@ManyToOne
	@JoinColumn(name = "device_uuid", referencedColumnName = "uuid")
	private Device deviceUuid;

	@ManyToOne
	@JoinColumn(name = "sensor_uuid", referencedColumnName = "uuid")
	private Device sensorUuid;

}
