package com.pct.device.version.repository;

import java.util.List;

import javax.transaction.Transactional;

import com.pct.device.version.constant.CampaignStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pct.device.version.model.Campaign;
import com.pct.device.version.model.Package;
import com.pct.device.version.repository.projections.CampaignIdAndImeiView;
import com.pct.device.version.repository.projections.CampaignPauseView;

/**
 * @author dhruv
 *
 */
@Repository
public interface ICampaignRepository extends 
JpaRepository<Campaign, Long>, JpaSpecificationExecutor<Campaign> {
	
	@Query("FROM Campaign camp WHERE camp.uuid = :uuid")
	Campaign findByUuid(@Param("uuid") String uuid);
	
	@Modifying(clearAutomatically = true)
	@Query("UPDATE Campaign camp  set isDeleted = true WHERE camp.uuid = :uuid")
	void deleteByUuid(@Param("uuid") String uuid);

    @Query(nativeQuery = true, value = "SELECT camp.* FROM campaign camp, pct_campaign.grouping g where (g.uuid = camp.grouping_uuid AND camp.grouping_uuid is NOT null and camp.is_active = true) and (g.grouping_name = :can or g.target_value = 'ALL')")
	List<Campaign> findActiveCampaignByDynamicCustomer(@Param("can") String can);
    
	@Query(nativeQuery = true, value = "SELECT pause_limit , pause_execution FROM campaign camp WHERE camp.uuid = :uuid")
	CampaignPauseView findCampaignStepLimit(@Param("uuid") String uuid);

	@Query(nativeQuery = true, value = "SELECT c.* FROM pct_campaign.campaign c, pct_campaign.grouping g " +
			"WHERE c.is_active = true AND c.is_deleted = false AND g.target_value like %:imei% " +
			"AND c.grouping_uuid = g.uuid")
	List<Campaign> findByIMEIForStaticGrouping(@Param("imei") String imei);

	@Query("FROM Campaign camp WHERE camp.campaignStatus = :status and camp.isDeleted = false")
	List<Campaign> findByStatus(@Param("status") CampaignStatus status);

	@Query("FROM Campaign camp WHERE camp.campaignName = :campaignName and camp.isDeleted = false")
	Campaign findByCampaignName(@Param("campaignName") String campaignName);
	

	@Query(nativeQuery = true, value = "SELECT * FROM pct_campaign.campaign c " +
			"WHERE c.is_deleted = false AND c.campaign_name like %:campaignName% " +
			"AND c.description like %:desc% ")
	Page<Campaign>  findByCampaignNameAndDesc(@Param("campaignName") String campaignName , @Param("desc") String desc , Pageable pageable);
	
	@Query("FROM Campaign camp WHERE camp.isDeleted = false")
	List<Campaign> findAllNonDeleted();

	@Modifying
	@Transactional
	@Query("DELETE FROM CampaignStepDeviceDetail csdd WHERE csdd.deviceId IN (:selectedDeviceIdForResetStatus) AND csdd.status ='FAILED'")
	void deleteFailedStepFromCampaignStepDeviceDetail(@Param("selectedDeviceIdForResetStatus") List<String> selectedDeviceIdForResetStatus);

	@Query(nativeQuery = true, value ="SELECT * FROM pct_campaign.campaign where uuid in (:listOfCompaignUuid) order by created_at asc limit 1")
	Campaign getEligibleCompaignByUuids(@Param("listOfCompaignUuid") List<String> listOfCompaignUuid);

	
}
