package com.pct.device.version.model;

import java.io.Serializable;

import javax.persistence.*;

@Entity
@Table(name = "grouping", catalog = "pct_campaign")
public class Grouping  implements Serializable {

	@Column(name = "target_value", columnDefinition = "MEDIUMTEXT")
	String targetValue;
	
	@Column(name = "not_eligible_value", columnDefinition = "text")
	String notEligibleValue;
	
	@Column(name = "grouping_type")
	String groupingType;
	
	@Column(name = "grouping_name")
	String groupingName;
	
	
	@Column(name = "excluded_imei", columnDefinition = "MEDIUMTEXT")
	String excludedImei;

	
	@Column(name = "removed_imei", columnDefinition = "MEDIUMTEXT")
	String removedImei;

   
	@Column(name = "uuid")
    String uuid;
    
/*    @Column(name = "not_eligible_count")
    Long notEligibleCount;*/
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "grouping_id")
	Long groupingId;
 
	public String getTargetValue() {
		return targetValue;
	}

	public void setTargetValue(String targetValue) {
		this.targetValue = targetValue;
	}

	public String getGroupingName() {
		return groupingName;
	}

	public void setGroupingName(String groupingName) {
		this.groupingName = groupingName;
	}

	public String getGroupingType() {
		return groupingType;
	}

	public void setGroupingType(String groupingType) {
		this.groupingType = groupingType;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public Long getGroupingId() {
		return groupingId;
	}

	public void setGroupingId(Long groupingId) {
		this.groupingId = groupingId;
	}

	public String getNotEligibleValue() {
		return notEligibleValue;
	}

	public void setNotEligibleValue(String notEligibleValue) {
		this.notEligibleValue = notEligibleValue;
	}
	 public String getExcludedImei() {
			return excludedImei;
		}

		public void setExcludedImei(String excludedImei) {
			this.excludedImei = excludedImei;
		}

		public String getRemovedImei() {
			return removedImei;
		}

		public void setRemovedImei(String removedImei) {
			this.removedImei = removedImei;
		}

	
}
