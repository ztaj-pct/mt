package com.pct.device.version.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pct.device.version.model.Campaign;
import com.pct.device.version.model.CampaignStep;
import com.pct.device.version.model.Package;
import com.pct.device.version.repository.projections.CampaignIdAndNameView;
import com.pct.device.version.repository.projections.CampaignStepView;

/**
 * @author dhruv
 *
 */
@Repository
public interface ICampaignStepRepository extends 
JpaRepository<CampaignStep, Long>, JpaSpecificationExecutor<CampaignStep> {
	
	@Query("FROM CampaignStep step WHERE step.uuid = :uuid")
 	CampaignStep findByUuid(@Param("uuid") String uuid);
	
	@Query(nativeQuery = true, value = "SELECT * FROM campaign_step step WHERE step.campaign_uuid = :campUuid")
	List<CampaignStep> findByCampaignUuid(@Param("campUuid") String campUuid);

	@Query("SELECT COUNT(*) FROM CampaignStep step WHERE step.campaign.isDeleted = false AND (step.fromPackage.uuid = :packageUuid or step.toPackage.uuid = :packageUuid) ")
	Long findPackageUsedInCampaign(@Param("packageUuid") String packageUuid);
 
 
 @Query(nativeQuery = true , value = "SELECT MAX(step.step_order_number) FROM campaign_step step WHERE step.campaign_uuid = :campaignUuid" )
	Long findLastStepInCampaign(@Param("campaignUuid") String campaignUuid);

/*
	@Query(nativeQuery = true, value = "SELECT * FROM campaign_step step WHERE step.campaign_uuid = :campUuid and step.from_package_uuid = ("
			+ " select uuid from package p where p.app_version = :appVersion and p.bin_version = :binVersion and p.config1 = :config1 "
			+ " and p.mcu_version = :mcuVersion )")
	CampaignStep findFrom_PackageAndStepOrderNumber(@Param("appVersion") String appVersion, @Param("binVersion") String binVersion, 
			@Param("config1") String config1, @Param("mcuVersion")
			String mcuVersion, @Param("campUuid") String campUuid);
*/
/*	@Query(nativeQuery = true, value = "SELECT * FROM campaign_step step WHERE step.campaign_uuid = :campUuid and step.to_package_uuid = ("
			+ " select uuid from package p where p.app_version = :appVersion and p.bin_version = :binVersion and p.config1 = :config1 "
			+ " and p.mcu_version = :mcuVersion )")
	CampaignStep findTo_PackageConfig(@Param("appVersion") String appVersion, @Param("binVersion") String binVersion, 
			@Param("config1") String config1, @Param("mcuVersion")
			String mcuVersion, @Param("campUuid") String campUuid);*/

	@Query(nativeQuery = true, value = "SELECT * FROM campaign_step step WHERE step.campaign_uuid = :campaignUuid ORDER BY step.step_order_number DESC LIMIT 1")
	CampaignStep findLastStepByCampaignUuid(@Param("campaignUuid") String campaignUuid);

	@Query(nativeQuery = true, value = "SELECT * FROM campaign_step step WHERE step.campaign_uuid = :campaignUuid ORDER BY step.step_order_number")
	List<CampaignStep> getAllStepsOfCampaign(@Param("campaignUuid") String campaignUuid);
	
	@Query("SELECT fromPackage FROM CampaignStep step WHERE step.campaign.uuid = :campaignUuid AND step.stepOrderNumber = 1")
	Package findBaseLinePackageByCampaignUuid(@Param("campaignUuid") String campaignUuid);

	@Query(nativeQuery = true, value = "SELECT camp.uuid, camp.campaign_name FROM campaign_step step , campaign camp WHERE "
			+ " camp.uuid = step.campaign_uuid and step.from_package_uuid = :packageUuid and step.step_order_number = 1 "
			+ " and camp.is_deleted = false and  camp.campaign_status != 'FINISHED' ")
	List<CampaignIdAndNameView> getBaselinePackageFromExistingCampaign(@Param("packageUuid")  String packageUuid);

	
	@Query(" FROM CampaignStep step WHERE step.campaign.uuid = :campaignUuid AND step.stepOrderNumber = :stepOrderNumber")
	CampaignStep findByCampaignUuidAndStepOrderNumber(@Param("campaignUuid")  String campaignUuid,@Param("stepOrderNumber") Long stepOrderNumber);

	@Query(nativeQuery = true, value = "SELECT camp.uuid, camp.campaign_name FROM campaign_step step , campaign camp WHERE "
			+ " camp.uuid = step.campaign_uuid and step.from_package_uuid IN (:uuids) and step.step_order_number = 1 "
			+ " and camp.is_deleted = false and  camp.campaign_status != 'FINISHED' ")
	List<CampaignIdAndNameView> getBaselinePackageFromExistingCampaign(@Param("uuids") List<String> uuids);
}
