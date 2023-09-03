package com.pct.common.model;

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

import com.pct.common.constant.OrganisationRole;
//import com.pct.common.constant.OrganisationType;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "device_company", catalog = "pct_device")
@Getter
@Setter
public class DeviceCompany {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "ms2_organisation_name")
	String ms2OrganisationName;
	
	@Column(name = "can")
	String can;
	
	@Column(name = "ms1_organisation_name")
	String ms1OrganisationName;
	
	@Column(name = "type", columnDefinition = "VARCHAR(15)")
	@Enumerated(value = EnumType.STRING)
	private OrganisationRole type;
	
	@ManyToOne
	@JoinColumn(name = "organisation_id", referencedColumnName = "uuid")
	private Organisation organisation;
}
