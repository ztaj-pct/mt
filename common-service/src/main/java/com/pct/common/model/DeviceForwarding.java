package com.pct.common.model;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
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

@Entity
@Table(name = "forwarding_device", catalog = "pct_device")
@Getter
@Setter
public class DeviceForwarding extends UserDateAudit implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "uuid")
	private String uuid;

	@ManyToOne
	@JoinColumn(name = "device_id", referencedColumnName = "imei")
	private Device device;

	@Column(name = "type")
	private String type;

	@Column(name = "url")
	private String url;
	
//	@JsonIgnoreProperties("role")
//	@ManyToOne
//	@JoinColumn(name = "created_by", referencedColumnName = "uuid")
//	private User createdBy;
//
//	@JsonIgnoreProperties("role")
//	@ManyToOne
//	@JoinColumn(name = "updated_by", referencedColumnName = "uuid")
//	private User updatedBy;

	@Column(name = "forwarding_rule_url_uuid", length = 255)
	private String forwardingRuleUrlUuid;

	@Override
	public String toString() {
		return "DeviceForwarding [id=" + id + ", uuid=" + uuid + ", device=" + device + ", type=" + type + ", url="
				+ url + "]";
	}
}
