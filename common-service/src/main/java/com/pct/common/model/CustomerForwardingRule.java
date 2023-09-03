package com.pct.common.model;

import java.io.Serializable;
import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.envers.Audited;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Audited
@Table(name = "customer_forwarding_rule", catalog = "pct_organisation")
public class CustomerForwardingRule implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "uuid", unique = true, nullable = false, length = 255)
	private String uuid;

	@Column(name = "type", nullable = false, length = 255)
	private String type;

	@Column(name = "url", nullable = false, length = 2000)
	private String url;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "organisation_uuid", referencedColumnName = "uuid")
	private Organisation organisation;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "forwarding_rule_url_uuid", referencedColumnName = "uuid")
	private CustomerForwardingRuleUrl forwardingRuleUrl;

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
