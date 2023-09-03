package com.pct.device.version.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "campaign_config_problem")
@Getter
@Setter
public class CampaignConfigProblem {
	
	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    Long id;
	
	@Column(name = "imei")
	private String imei;
	
	@Column(name = "product_name")
	private String productName;
	
	@Column(name = "customer_name")
	private String customerName;
	
	@Column(name = "device_status_for_campaign")
	private String deviceStatusForCampaign;
	
	@Column(name = "comments")
	private String comments;
	
	@Column(name = "campaign_name")
	private String campaignName;
	
	@Column(name = "campaign")
	Campaign campaign;
	
	@Column(name = "campaign_id")
	Long campaignId;

}
