package com.pct.common.model;

import java.io.Serializable;
import java.time.Instant;

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

import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.pct.common.constant.AssetCategory;
import com.pct.common.constant.AssetCreationMethod;
import com.pct.common.constant.AssetStatus;
import com.pct.common.serde.InstantDeserializer;
import com.pct.common.serde.InstantSerializer;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "asset", catalog = "pct_device")
@Getter
@Setter
@Audited
@NoArgsConstructor
public class Asset extends DateAudit implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

   	@Column(name="created_at")
   	private Instant createdAt;
     	
   	@Column(name="updated_at")
   	private Instant updatedAt;
   	
    @Column(name = "assigned_name")
    private String assignedName;

    @Column(name = "gateway_eligibility")
    private String gatewayEligibility;
    
    @JsonIgnoreProperties("assets")
    @ManyToOne
    @JoinColumn(name = "account_number", referencedColumnName = "account_number",nullable = false)
    private Organisation organisation;

    @Column(name = "vin")
    private String vin;

    @Column(name = "uuid")
    private String uuid;

    @Column(name = "year")
    private String year;

    @NotAudited
    @ManyToOne
    @JoinColumn(name = "manufacturer_uuid", referencedColumnName = "uuid")
    private Manufacturer manufacturer;

    @NotAudited
    @Column(name = "category")
    @Enumerated(EnumType.STRING)
    private AssetCategory category;

    @JsonIgnore
    @NotAudited
    @Column(name = "status")
    @Enumerated(value = EnumType.STRING)
    private AssetStatus status;

    @NotAudited
    @ManyToOne
    @JoinColumn(name = "manufacturer_details_uuid", referencedColumnName = "uuid")
    private ManufacturerDetails manufacturerDetails;

    @JsonIgnoreProperties("role")
    @ManyToOne
    @JoinColumn(name = "created_by", referencedColumnName = "uuid")
    private User createdBy;

    @JsonIgnoreProperties("role")
    @ManyToOne
    @JoinColumn(name = "updated_by", referencedColumnName = "uuid")
    private User updatedBy;

    @Column(name = "comment")
    private String comment;

    @Column(name= "is_vin_validated")
    private Boolean isVinValidated;

    @NotAudited
    @Column(name = "creation_method")
    @Enumerated(value = EnumType.STRING)
    private AssetCreationMethod creationMethod;
    
    @Column(name = "is_applicable_for_pre_pair", nullable = false)
    private Boolean isApplicableForPrePair = true;
    
    @JsonSerialize(using = InstantSerializer.class)
	@JsonDeserialize(using = InstantDeserializer.class)
	@Column(name = "installation_date")
	private Instant installationDate;
    @Column(name = "asset_nick_name")
    private String assetNickName;
    
    @Column(name = "no_of_tires")
    private String noOfTires;
    
    @Column(name = "no_of_axles")
    private String noOfAxles;
    
    @Column(name = "external_length")
    private String externalLength;
    
    @Column(name = "door_type")
    private String doorType;
    
    @Column(name = "tag")
    private String tag;
    
}
