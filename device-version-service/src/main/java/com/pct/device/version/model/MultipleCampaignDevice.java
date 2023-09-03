package com.pct.device.version.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;

/**
 * @author Abhishek on 02/11/20
 */

@Entity
@Table(name = "multiple_campaign_device", catalog = "pct_campaign")
@Getter
@Setter
@NoArgsConstructor
public class MultipleCampaignDevice implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    Long id;

    @Column(name = "created_at")
    Instant createdAt;

    @ManyToOne
    @JoinColumn(name = "campaign_uuid", referencedColumnName = "uuid")
    Campaign campaign;

    @Column(name = "device_id")
    String deviceId;
    
    @ManyToOne
    @JoinColumn(name = "step_uuid", referencedColumnName = "uuid")
    CampaignStep campaignStep;
}
