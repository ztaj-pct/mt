package com.pct.device.version.repository;

import com.pct.device.version.model.Campaign;
import com.pct.device.version.model.MultipleCampaignDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Abhishek on 02/11/20
 */

@Repository
public interface IMultipleCampaignDeviceRepository extends
        JpaRepository<MultipleCampaignDevice, Long>, JpaSpecificationExecutor<MultipleCampaignDevice> {

    @Query("FROM MultipleCampaignDevice m WHERE m.deviceId = :deviceId")
    List<MultipleCampaignDevice> findByDeviceId(@Param("deviceId") String deviceId);

    @Query("FROM MultipleCampaignDevice m WHERE m.deviceId = :deviceId AND m.campaign.uuid = :campaignUuid")
    MultipleCampaignDevice findByDeviceIdAndCampaignUuid(@Param("deviceId") String deviceId,
                                                               @Param("campaignUuid") String campaignUuid);
    
    @Query("FROM MultipleCampaignDevice m WHERE m.campaign.uuid  = :campaignUuid")
    List<MultipleCampaignDevice> findByCampaignUuid(@Param("campaignUuid") String campaignUuid);
}
