package com.pct.common.model;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "role", catalog = "pct_auth")
@SQLDelete(sql = "UPDATE role SET deleted = true WHERE role_id=?")
//@Where(clause = "deleted=false OR deleted is null")
@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "roleId", scope = Long.class)
@JsonIgnoreProperties(ignoreUnknown=true)
public class Role extends BaseEntity implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "role_id", unique = true, nullable = false, length = 10)
	private Long roleId;
	
	@Column(name = "role_name")
    private String roleName;
	
	@Column(name = "name")
	private String name;

	@Column(name = "description")
	private String description;

	@JsonIgnoreProperties("permissions")
	@ManyToMany
	@JoinTable(name = "role_permission", catalog = "pct_auth", joinColumns = @JoinColumn(name = "role_id"), inverseJoinColumns = @JoinColumn(name = "permission_id"))
	@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
//	@NotAudited
	private List<PermissionEntity> permissions;

////	@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, 
////            property  = "id", 
////            scope     = Long.class)
//	@JsonIgnoreProperties("user")
//	@OneToMany(mappedBy = "role")
//	@NotAudited
//	private List<User> user;

	@Column(name = "deleted",columnDefinition = "boolean default false")
	private Boolean deleted;
	
}