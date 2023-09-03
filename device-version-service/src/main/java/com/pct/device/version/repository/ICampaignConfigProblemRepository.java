package com.pct.device.version.repository;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pct.device.version.model.CampaignConfigProblem;


@Repository
public interface ICampaignConfigProblemRepository extends 
JpaRepository<CampaignConfigProblem, Long>, JpaSpecificationExecutor<CampaignConfigProblem> {
	
	  @Query("FROM CampaignConfigProblem c WHERE c.imei = :imei")
	    List<CampaignConfigProblem> findByDeviceId(@Param("imei") String deviceId);

	  @Modifying
	  @Transactional
	  @Query("Delete FROM CampaignConfigProblem c WHERE c.imei = :imei and c.campaignName = :campaignName")
	  void deleteByDeviceIdAndCampaignName(@Param("imei") String deviceId, @Param("campaignName") String campaignName);

	  @Query("FROM CampaignConfigProblem c WHERE c.imei = :imei and c.campaignName = :campaignName")
	  List<CampaignConfigProblem> findByDeviceIdAndCampaignName(@Param("imei") String deviceId, @Param("campaignName") String campaignName);

	  @Modifying
	  @Transactional
	  @Query("Delete FROM CampaignConfigProblem c WHERE c.imei = :imei and c.campaignId = :campaignId")
	  void deleteByDeviceIdAndCampaignId(@Param("imei") String deviceId, @Param("campaignId") Long campaignId);
	  
	  @Query("FROM CampaignConfigProblem c WHERE c.imei = :imei and c.campaignId = :campaignId")
	  List<CampaignConfigProblem> findByDeviceIdAndCampaignId(@Param("imei") String deviceId, @Param("campaignId") Long campaignId);
	  
	  
	  @Query("FROM CampaignConfigProblem c WHERE c.campaignId = :campaignId")
	  List<CampaignConfigProblem> findByCampaignId(@Param("campaignId") Long campaignId);
	  
}
