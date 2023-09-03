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
@Table(name = "customer_forwarding_rule_url", catalog = "pct_organisation")
public class CustomerForwardingRuleUrl implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "uuid", unique = true, nullable = false, length = 255)
	private String uuid;

	@Column(name = "format", length = 255)
	private String format;
	
	@Column(name = "type", length = 255)
	private String type;
	
	@Column(name = "kafka_topic_name", length = 255)
	private String kafkaTopicName;

	@Column(name = "rule_name", nullable = false, length = 2000)
	private String ruleName;

	@Column(name = "endpoint_destination", nullable = false, length = 2000)
	private String endpointDestination;

	@Column(name = "description", columnDefinition = "LONGTEXT")
	private String description;

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
