package com.pct.common.model;

import java.io.Serializable;
import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.envers.Audited;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "asset_device_xref", catalog = "pct_device")
@NoArgsConstructor
//@Audited
public class Asset_Device_xref extends DateAudit implements Serializable  {

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
	
	@Column(name = "comment")
	private String comment;

	@OneToOne
	@JoinColumn(name = "device_uuid", referencedColumnName = "uuid")
	private Device device;

	@OneToOne
	@JoinColumn(name = "asset_uuid", referencedColumnName = "uuid")
	private Asset asset;
	
	@CreatedBy
	@JsonIgnoreProperties("role")
	@ManyToOne
	@JoinColumn(name = "created_by", referencedColumnName = "uuid")
	private User createdBy;

	@LastModifiedBy
	@JsonIgnoreProperties("role")
	@ManyToOne
	@JoinColumn(name = "updated_by", referencedColumnName = "uuid")
	private User updatedBy;
	

}
