package com.pct.common.payload;

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
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.pct.common.model.Device;
import com.pct.common.model.User;
import com.pct.common.model.UserDateAudit;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Data
@Getter
@Setter
@NoArgsConstructor
public class DeviceSensorxrefPayload {

	private static final long serialVersionUID = 1L;	
	Instant dateCreated;
	
	Instant dateDeleted;
	
	private boolean active;

	private Device deviceUuid;
	
	private Device sensorUuid;
	
	@JsonIgnoreProperties("role")	
	private User createdBy;

	@JsonIgnoreProperties("role")
	private User updatedBy;

	@LastModifiedDate
	private Instant updatedOn;
	
	private String logUUId;

}
