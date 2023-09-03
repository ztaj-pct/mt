package com.pct.device.version.model;

import com.pct.common.model.User;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;


@Entity
@Table(name = "campaign_step")
@Getter
@Setter

public class CampaignStep implements Serializable {

	@Column(name = "step_order_number")
	Long stepOrderNumber;
	
    @ManyToOne
    @JoinColumn(name = "from_package_uuid", referencedColumnName = "uuid")
	Package fromPackage;
	
	
    @ManyToOne
    @JoinColumn(name = "to_package_uuid", referencedColumnName = "uuid")
	Package toPackage;
    
    @ManyToOne
    @JoinColumn(name = "campaign_uuid", referencedColumnName = "uuid")
	Campaign campaign;
    
    @Column(name = "uuid")
    String uuid;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "step_id")
	Long stepId;

    @ManyToOne
    @JoinColumn(name = "created_by", referencedColumnName = "uuid")
    User createdBy;

    @ManyToOne
    @JoinColumn(name = "updated_by", referencedColumnName = "uuid")
    User updatedBy;
    
	
	@Column(name = "created_at")
	Instant createdAt;
	
	@Column(name = "updated_at")
	Instant updatedAt;
	
    @Column(name = "at_command")
    String atCommand;
	
}
