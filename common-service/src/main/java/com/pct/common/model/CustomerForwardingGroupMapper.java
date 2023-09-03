package com.pct.common.model;

import java.io.Serializable;
import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.envers.Audited;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Audited
@Entity
@Table(name = "customer_forwarding_group_mapper", catalog = "pct_organisation")
public class CustomerForwardingGroupMapper implements Serializable {

	private static final long serialVersionUID = 1L;

	@EmbeddedId
	private CustomerForwardingGroupMapperId id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "customer_forwarding_group_uuid", referencedColumnName = "uuid", insertable = false, updatable = false)
	private CustomerForwardingGroup customerForwardingGroup;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "organisation_uuid", referencedColumnName = "uuid", insertable = false, updatable = false)
	private Organisation organisation;
	
	@JsonIgnoreProperties("role")
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "created_by", referencedColumnName = "uuid")
	private User createdBy;

	@Column(name = "created_on")
	private Instant createdOn;

	@JsonIgnoreProperties("role")
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "updated_by", referencedColumnName = "uuid")
	private User updatedBy;

	@Column(name = "updated_on")
	private Instant updatedOn;

}
