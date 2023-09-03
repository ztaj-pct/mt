package com.pct.common.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "organisation_settings", catalog = "pct_organisation")
public class OrganisationSettings extends UserDateAudit{
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name = "uuid")
	private String uuid;
	
	@Column(name = "data_type")
	private String dataType;
	
	@Column(name = "field_name")
	private String fieldName;
	
	@Column(name = "field_value")
	private String fieldValue;
	
	@Column(name = "organisation_section_uuid")
	private String organisationSectionUuid;
	
	@Column(name = "organisation_uuid")
	private String organisationUuid;
	
}
