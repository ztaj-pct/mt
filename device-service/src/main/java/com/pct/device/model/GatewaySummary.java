package com.pct.device.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Immutable;

import lombok.Data;

@Entity
@Immutable
@Table(name = "gateway_record")
@Data
public class GatewaySummary {

	@Column(name = "organisation_name")
	String organisationName;

	@Id
	@Column(name = "organisation_id")
	Long organisationId;

	@Column(name = "smart_seven")
	Long smartSeven;

	@Column(name = "trailler_net")
	Long traillerNet;

	@Column(name = "smart_pair")
	Long smartPair;

	@Column(name = "sabre")
	Long sabre;

	@Column(name = "stealth_net")
	Long stealthNet;

	@Column(name = "freight_la")
	Long freightLa;

	@Column(name = "arrow_l")
	Long arrowL;

	@Column(name = "freight_l")
	Long freightL;

	@Column(name = "cutlass_l")
	Long cutlassL;

	@Column(name = "dagger67_lg")
	Long dagger67Lg;

	@Column(name = "Smart7")
	Long smart7;

	@Column(name = "katana_h")
	Long katanaH;

}
