package com.pct.common.model;

import java.time.Instant;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract  class UserDateAudit {

	public static final String CREATED_BY = "created_by";
	public static final String CREATED_DATE = "created_on";
	public static final String LAST_MODIFIED_BY = "updated_by";
	public static final String LAST_MODIFIED_DATE = "updated_on";

	@CreatedBy
	@JsonIgnoreProperties("role")
	@ManyToOne
	@JoinColumn(name = CREATED_BY, referencedColumnName = "uuid")
	private User createdBy;

	@CreatedDate
	@Column(name = CREATED_DATE)
	private Instant createdOn;

	@LastModifiedBy
	@JsonIgnoreProperties("role")
	@ManyToOne
	@JoinColumn(name = LAST_MODIFIED_BY, referencedColumnName = "uuid")
	private User updatedBy;

	@LastModifiedDate
	@Column(name = LAST_MODIFIED_DATE)
	private Instant updatedOn;
}
