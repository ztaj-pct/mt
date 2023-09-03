package com.pct.common.model;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

//import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Audited
@Table(name = "user", catalog = "pct_auth")
@SQLDelete(sql = "UPDATE user SET deleted = true WHERE id=?")
@Where(clause = "deleted=false OR deleted is null")
@JsonIgnoreProperties(ignoreUnknown=true)
public class User implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@JsonIgnore
	@Column(name = "created_at")
	private Instant createdAt;

	@JsonIgnore
	@Column(name = "created_by")
	private String createdBy;

	@Column(name = "updated_at")
	private Instant updatedAt;

	@JsonIgnore
	@Column(name = "updated_by")
	private String updatedBy;

	@Column(name = "time_zone")
	public String timeZone;

	@Column(name = "notify")
	public String notify;

	@Column(name = "user_name")
	private String userName;

	@Column(name = "first_name")
	private String firstName;

	@Column(name = "last_name")
	private String lastName;

	@Column(name = "password")
	@NotNull
	private String password;

	@Column(name = "email", unique = true)
	private String email;

	@Column(name = "country_code")
	private String countryCode = "us";

	@Column(name = "phone")
	private String phone;

	@Column(name = "is_active")
	private Boolean isActive;

	@Column(name = "is_deleted")
	private Boolean isDeleted;

	@Column(name = "is_password_change", nullable = false)
	private Boolean isPasswordChange = false;

	@JsonIgnoreProperties(value = { "users" }, allowSetters = true)
	@ManyToOne
	@JoinColumn(name = "organisation_uuid", referencedColumnName = "uuid")
	@NotAudited
	private Organisation organisation;

	@Column(name = "uuid")
	private String uuid;

	@Column(name = "last_login_time")
	private String lastLoginTime;
	
	@Column(name = "landing_page")
	private String landingPage;

	@NotAudited
	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name = "user_role", catalog = "pct_auth", joinColumns = {
			@JoinColumn(name = "user_uuid", referencedColumnName = "uuid") }, inverseJoinColumns = {
					@JoinColumn(name = "role_id", referencedColumnName = "role_id") })
	@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
	private List<Role> role;
	
	@ManyToOne
	@JoinColumn(name = "location_id", referencedColumnName = "id")
	@NotAudited
	private Location homeLocation;
	
	@NotAudited
	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "user_additional_locations", catalog = "pct_auth", joinColumns = {
			@JoinColumn(name = "user_uuid", referencedColumnName = "uuid") }, inverseJoinColumns = {
					@JoinColumn(name = "location_id", referencedColumnName = "id") })
	@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
	private List<Location> additionalLocations;

	@Column(name = "deleted")
	private Boolean deleted = Boolean.FALSE;
	
	@Override
	public String toString() {
		return "";
	}
}
