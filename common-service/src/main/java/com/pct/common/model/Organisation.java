package com.pct.common.model;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.Set;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.pct.common.constant.OrganisationRole;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Audited
@Table(name = "organisation", catalog = "pct_organisation")
@JsonIgnoreProperties({ "organisationRole"})	
public class Organisation implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name="created_at")
	private Instant createdAt;
	
	@Column(name="updated_at")
	private Instant updatedAt;
	
	@Column(name = "organisation_name")
	private String organisationName;

	@Column(name = "is_active")
	private Boolean isActive;
	
	@Column(name = "maintenance_mode")
	private Boolean maintenanceMode = false;

	@Column(name = "account_number")
	private String accountNumber;

	@Column(name = "short_name")
	private String shortName;

	@Column(name = "uuid")
	private String uuid;

	@Column(name = "is_asset_list_required")
	private Boolean isAssetListRequired;
	

	@JsonIgnoreProperties("role")
	@ManyToOne
	@JoinColumn(name = "created_by", referencedColumnName = "uuid")
	private User createdBy;

	@JsonIgnoreProperties("role")
	@ManyToOne
	@JoinColumn(name = "updated_by", referencedColumnName = "uuid")
	private User updatedBy;



	@Column(name = "previous_record_id", columnDefinition = "VARCHAR(255)")
	private String previousRecordId;
	
	@NotAudited
	@JsonIgnoreProperties({ "accessList", "users" ,"organisationRole"})
	@JoinTable(name = "organisation_access", catalog = "pct_organisation", joinColumns = {
			@JoinColumn(name = "organisation_id", referencedColumnName = "id") }, inverseJoinColumns = {
					@JoinColumn(name = "organisationaccess_id", referencedColumnName = "id") })
	@ManyToMany
	private List<Organisation> accessList;
	
	
	@Column(name="epicor_account_number")
	private String epicorAccountNumber;
	
 
	
	@ElementCollection(targetClass = OrganisationRole.class) 
	@CollectionTable(name = "organisation_role_mapping",catalog = "pct_organisation", joinColumns = @JoinColumn(name = "organisation_id")) 
	@Enumerated(EnumType.STRING) 
	@Column(name="oraganisation_role")
	private Set<OrganisationRole> organisationRole;
	
	

	@JsonIgnoreProperties({ "resellerList", "users" ,"organisationRole"})
	@JoinTable(name = "organisation_reseller_mapping", catalog = "pct_organisation", joinColumns = {
			@JoinColumn(name = "organisation_id", referencedColumnName = "id") }, inverseJoinColumns = {
					@JoinColumn(name = "reseller_id", referencedColumnName = "id") })
	@ManyToMany
	private List<Organisation> resellerList;
	
	
	@JsonIgnoreProperties({ "installerList", "users" ,"organisationRole"})
	@JoinTable(name = "organisation_installer_mapping", catalog = "pct_organisation", joinColumns = {
			@JoinColumn(name = "organisation_id", referencedColumnName = "id") }, inverseJoinColumns = {
					@JoinColumn(name = "installer_id", referencedColumnName = "id") })
	@ManyToMany
	private List<Organisation> installerList;

	
	@JsonIgnoreProperties({ "maintenanceList", "users" })
	@JoinTable(name = "organisation_maintenance_mapping", catalog = "pct_organisation", joinColumns = {
			@JoinColumn(name = "organisation_id", referencedColumnName = "id") }, inverseJoinColumns = {
					@JoinColumn(name = "maintenance_id", referencedColumnName = "id") })
	@ManyToMany
	private List<Organisation> maintenanceList;
	
	@OneToMany(mappedBy = "organisation", fetch = FetchType.LAZY)
	private List<CustomerForwardingGroupMapper> forwardingGroupMappers;
	
	@OneToMany(mappedBy = "organisation", fetch = FetchType.LAZY)
	private List<Location> locations;

}
