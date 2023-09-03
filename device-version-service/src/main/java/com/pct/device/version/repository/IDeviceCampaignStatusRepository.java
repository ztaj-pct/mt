package com.pct.device.version.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pct.device.version.model.DeviceCampaignStatus;


@Repository
public interface IDeviceCampaignStatusRepository extends 
JpaRepository<DeviceCampaignStatus, Long>, JpaSpecificationExecutor<DeviceCampaignStatus> {
	
	@Query("FROM DeviceCampaignStatus deviceCampaignStatus where deviceCampaignStatus.deviceId = :deviceId and deviceCampaignStatus.campaign.uuid = :campaign_uuid ")
	List<DeviceCampaignStatus> findByDeviceAndCampaignUUID(@Param("deviceId") String deviceId, @Param("campaign_uuid") String campaign_uuid);
	
	@Query("FROM DeviceCampaignStatus deviceCampaignStatus where deviceCampaignStatus.campaign.uuid = :campaign_uuid ")
	List<DeviceCampaignStatus> findByCampaignUUID( @Param("campaign_uuid") String campaign_uuid);


	@Query("FROM DeviceCampaignStatus deviceCampaignStatus where deviceCampaignStatus.deviceId = :deviceId and deviceCampaignStatus.campaign.uuid = :campaign_uuid ")
	List<DeviceCampaignStatus> findStatusByCampaignUuidAndCampaignUUID(@Param("deviceId") String deviceId, @Param("campaign_uuid") String campaign_uuid);
	
	
	@Query("FROM DeviceCampaignStatus deviceCampaignStatus where deviceCampaignStatus.deviceId = :deviceId and deviceCampaignStatus.runningStatus = :runningStatus ")
	List<DeviceCampaignStatus> findByDeviceIdAndStatus( @Param("deviceId") String deviceId, @Param("runningStatus") String runningStatus);
	
	
	@Query("FROM DeviceCampaignStatus deviceCampaignStatus where deviceCampaignStatus.deviceId in :deviceId and deviceCampaignStatus.campaign.uuid = :campaign_uuid ")
	List<DeviceCampaignStatus> findByDeviceListAndCampaignUUID(@Param("deviceId") List<String> deviceId, @Param("campaign_uuid") String campaign_uuid);
	
	

}
