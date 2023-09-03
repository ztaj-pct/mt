package com.pct.device.version.repository;

import java.util.List;

import javax.persistence.Tuple;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pct.device.version.constant.CampaignStepDeviceStatus;
import com.pct.device.version.model.CampaignStepDeviceDetail;
import com.pct.device.version.repository.projections.DeviceCampaignStepStatus;
import com.pct.device.version.repository.projections.StepStatusForDeviceView;

/**
 * @author dhruv
 *
 */
@Repository
public interface ICampaignStepDeviceDetailRepository extends 
JpaRepository<CampaignStepDeviceDetail, Long>, JpaSpecificationExecutor<CampaignStepDeviceDetail> {

	@Query(nativeQuery = true, value = "SELECT * FROM campaign_step_device_detail deviceDetail WHERE "
			+ "deviceDetail.device_id = :deviceId and step_uuid = :stepUuid  and deviceDetail.status != 'FAILED'")
	CampaignStepDeviceDetail findStatusByCampaignUuidAndStepUuid(@Param("deviceId") String deviceId, @Param("stepUuid") String stepUuid);


	@Query(nativeQuery = true, value = "SELECT deviceDetail.status FROM campaign_step_device_detail deviceDetail , campaign_step step  "
			+ "WHERE deviceDetail.step_uuid = step.uuid and  deviceDetail.device_id = :deviceId and step.campaign_uuid = :campUuid "
			+ " and deviceDetail.status != 'FAILED' and step.step_order_number = :stepNum ")
	String findStatusByCampaignUuidAndStepOrderNumber(@Param("campUuid") String campUuid, @Param("deviceId") String deviceId, @Param("stepNum") Long stepNum);
	
	@Query("FROM CampaignStepDeviceDetail deviceDetail WHERE deviceDetail.uuid = :uuid")
	CampaignStepDeviceDetail findByUuid(@Param("uuid") String uuid);

	@Query("FROM CampaignStepDeviceDetail deviceDetail WHERE deviceDetail.campaign.uuid = :uuid order by deviceDetail.startExecutionTime desc")
	List<CampaignStepDeviceDetail> findByUuidMax(@Param("uuid") String uuid);
	
	@Query(nativeQuery = true, value = "SELECT DISTINCT(device_id) FROM campaign_step_device_detail deviceDetail where deviceDetail.campaign_uuid = :campUuid ")
	List<Object> findCountOfExecutedGateways(@Param("campUuid") String campUuid);

	@Query("FROM CampaignStepDeviceDetail deviceDetail WHERE deviceDetail.campaignStep.uuid = :stepUuid AND deviceDetail.status = :status")
	List<CampaignStepDeviceDetail> findByCampaignStepAndStatus(@Param("stepUuid") String stepUuid,
															   @Param("status") CampaignStepDeviceStatus success);
	
	@Query("FROM CampaignStepDeviceDetail deviceDetail WHERE deviceDetail.campaign.uuid = :campUuid and deviceDetail.status != :status ORDER BY deviceDetail.deviceId , deviceDetail.campaignStep.stepOrderNumber")
	List<CampaignStepDeviceDetail> findByCampaignUuid(@Param("campUuid") String campUuid, @Param("status") CampaignStepDeviceStatus failed);
	
	
	@Query("SELECT COUNT(*) FROM CampaignStepDeviceDetail deviceDetail WHERE deviceDetail.campaign.uuid = :campUuid AND deviceDetail.campaignStep.stepOrderNumber = :lastStepNum AND deviceDetail.status = :status")
	Long getCountOfLastCampaignStepStatusAsSuccess(@Param("campUuid") String campUuid, @Param("lastStepNum") Long lastStepNum , 
															   @Param("status") CampaignStepDeviceStatus success);

	@Query("FROM CampaignStepDeviceDetail deviceDetail WHERE deviceDetail.campaign.uuid = :campUuid AND deviceDetail.status = :status")
	List<CampaignStepDeviceDetail> findByCampaignAndStatus(@Param("campUuid") String campUuid,
															   @Param("status") CampaignStepDeviceStatus success);

	// @Query("SELECT deviceDetail.campaignStep.stepOrderNumber FROM
	// CampaignStepDeviceDetail deviceDetail WHERE deviceDetail.campaign.uuid =
	// :campUuid AND deviceDetail.deviceId = :deviceId ORDER BY
	// deviceDetail.campaignStep.stepOrderNumber DESC LIMIT 1 ")
	@Query(nativeQuery = true, value = "SELECT step.step_order_number FROM campaign_step_device_detail deviceDetail , campaign_step step  "
			+ " WHERE deviceDetail.step_uuid = step.uuid and  deviceDetail.device_id = :deviceId and step.campaign_uuid = :campUuid "
			+ " ORDER BY step.step_order_number DESC LIMIT 1 ")
	Integer getLastExecutedStep(@Param("campUuid") String campUuid, @Param("deviceId") String deviceId);
	
	@Query(nativeQuery = true, value = "SELECT count(*) FROM campaign_step_device_detail deviceDetail WHERE "
			+ " deviceDetail.campaign_uuid = :campUuid AND deviceDetail.status = 'PENDING' AND "
			+ " device_id not in (SELECT device_id FROM multiple_campaign_device ct where "
			+ " ct.campaign_uuid = :campUuid )")
	Long getInProgressButNotHoldGatewayCount(@Param("campUuid") String campUuid);


	@Query(nativeQuery = true, value = "SELECT count(*) FROM campaign_step_device_detail deviceDetail WHERE "
			+ "deviceDetail.device_id = :deviceId and step_uuid = :stepUuid and deviceDetail.status = 'FAILED'")
	Long findByCampaignUuidAndStepUuidAndFailedStatus(@Param("deviceId") String deviceId, @Param("stepUuid") String stepUuid);
	
	@Query(nativeQuery = true, value = "SELECT deviceDetail.step_uuid FROM campaign_step_device_detail deviceDetail "
			+ " WHERE deviceDetail.device_id = :deviceId and deviceDetail.campaign_uuid = :campUuid "
			+ " and deviceDetail.status = 'FAILED'")
	List<String> getFailedExecutedStep(@Param("campUuid") String campUuid, @Param("deviceId") String deviceId);
	
	
	
	@Query(nativeQuery = true, value = "SELECT deviceDetail.step_uuid FROM campaign_step_device_detail deviceDetail "
			+ " WHERE deviceDetail.device_id = :deviceId and deviceDetail.campaign_uuid = :campUuid "
			+ " and deviceDetail.status = 'FAILED'")
	List<String> getFailedExecutedStep(@Param("campUuid") String campUuid, @Param("deviceId") List<String> deviceId);
	
	@Query(nativeQuery = true, value = "SELECT deviceDetail.status FROM campaign_step_device_detail deviceDetail "
			+ " WHERE deviceDetail.device_id = :deviceId and deviceDetail.step_uuid = :stepUuid "
			+ " and deviceDetail.status = 'PENDING'")
	String getStatusOfStep(@Param("stepUuid") String stepUuid, @Param("deviceId") String deviceId);

	@Query(nativeQuery = true, value = "SELECT deviceDetail.status FROM campaign_step_device_detail deviceDetail "
			+ " WHERE deviceDetail.device_id = :deviceId and deviceDetail.step_uuid = :stepUuid ")
	String getStepSatusByStepUuid(@Param("stepUuid") String stepUuid, @Param("deviceId") String deviceId);
	
	@Query(nativeQuery = true, value = "SELECT deviceDetail.device_id , deviceDetail.status  FROM campaign_step_device_detail deviceDetail , campaign_step step  "
			+ "WHERE deviceDetail.step_uuid = step.uuid and  deviceDetail.device_id in :deviceId and step.campaign_uuid = :campUuid "
			+ " and deviceDetail.status != 'FAILED' and step.step_order_number = :stepNum ")
	List<StepStatusForDeviceView> findAllStatusByCampaignUuidAndStepOrderNumber(@Param("campUuid") String campUuid, @Param("deviceId") List<String> deviceId, @Param("stepNum") Long stepNum);
	
	@Query(nativeQuery = true, value = "SELECT * FROM campaign_step_device_detail deviceDetail  "
			+ "WHERE deviceDetail.device_id = :deviceId and deviceDetail.campaign_uuid = :campUuid "
			+ " ORDER BY deviceDetail.campaign_step_run_id DESC LIMIT 1 ")
	CampaignStepDeviceDetail getDeviceCampaignLastExecuteStep(@Param("campUuid") String campUuid, @Param("deviceId") String deviceId);
	
	@Query(nativeQuery = true, value = "SELECT * FROM campaign_step_device_detail deviceDetail  "
			+ "WHERE deviceDetail.device_id = :deviceId and deviceDetail.campaign_uuid = :campUuid "
			+ " ORDER BY deviceDetail.campaign_step_run_id DESC LIMIT 1 ")
	CampaignStepDeviceDetail getDeviceCampaignLastExecuteStep(@Param("campUuid") String campUuid, @Param("deviceId") List<String> deviceId);

	@Query(nativeQuery = true, value = "SELECT t1.* FROM campaign_step_device_detail t1 INNER JOIN (    SELECT `device_id`, MAX(campaign_step_run_id) AS max_age FROM campaign_step_device_detail  where campaign_uuid = :campUuid GROUP BY `device_id` ) t2     ON t1.`device_id` = t2.`device_id` AND t1.campaign_step_run_id = t2.max_age")
	List<CampaignStepDeviceDetail> getDeviceCampaignLastExecuteStep(@Param("campUuid") String campUuid);
	
	@Query("FROM CampaignStepDeviceDetail deviceDetail WHERE deviceDetail.deviceId = :imei AND  deviceDetail.campaign.uuid = :campaignUuid AND deviceDetail.campaignStep.uuid = :stepUuid order by campaignStepRunId desc")
	List<CampaignStepDeviceDetail> findByDeviceIdAndCampaignUuidAndCampaignStepUuid(@Param("imei") String imei,@Param("campaignUuid") String campaignUuid,@Param("stepUuid") String stepUuid);

	/*
	This query returns the number of rows which have a true problem_email value for a specific device and step in a campaign.
	This checks whether san email has already been sent in the problem status scenario.
	 */

	@Query(nativeQuery = true, value = "SELECT count(*) FROM campaign_step_device_detail deviceDetail WHERE deviceDetail.device_id = :imei "
			+ "AND deviceDetail.campaign_uuid = :campaignUuid AND deviceDetail.step_uuid = :stepUuid "
			+ "AND deviceDetail.problem_email = true")
	Long getProblemEmailCountForStep (@Param("imei") String imei,@Param("campaignUuid") String campaignUuid,@Param("stepUuid") String stepUuid);
	
	
	@Query(nativeQuery = true, value = "SELECT * FROM campaign_step_device_detail deviceDetail  "
			+ " WHERE  deviceDetail.device_id = :deviceId and deviceDetail.campaign_uuid = :campUuid and deviceDetail.status = 'REMOVED' "
			+ " ORDER BY deviceDetail.campaign_step_run_id DESC LIMIT 1 ")
	List<CampaignStepDeviceDetail> getRemovedIMEI(@Param("campUuid") String campUuid, @Param("deviceId") String deviceId);

//	@Query(nativeQuery = true, value = "SELECT  package.package_name , count(*),`step_uuid` , status FROM `campaign_step_device_detail` , package , campaign_step WHERE campaign_step_device_detail.campaign_uuid = '65b29ff0-3834-4286-966a-e1fbafa1be98' and step_uuid = campaign_step.uuid and campaign_step.to_package_uuid = package.uuid group by `step_uuid` , status;")
	@Query( nativeQuery = true, value = "SELECT package.package_name , count(*) , step_uuid , status  FROM campaign_step_device_detail , package , campaign_step WHERE campaign_step_device_detail.campaign_uuid = :campaignUuid and step_uuid = campaign_step.uuid and campaign_step.from_package_uuid = package.uuid and status != 'FAILED' group by step_uuid , status ")
	List<Tuple> findByStepDtoByUuid(@Param("campaignUuid") String campaignUuid);
	
	
	@Query(nativeQuery = true, value = "SELECT * FROM campaign_step_device_detail deviceDetail  "
			+ "WHERE deviceDetail.device_id = :deviceId and deviceDetail.campaign_uuid = :campUuid "
			+ " ")
	List<CampaignStepDeviceDetail> getDeviceCampaign(@Param("campUuid") String campUuid, @Param("deviceId") List<String> deviceId);
	
	
	@Query(nativeQuery = true, value = "SELECT deviceDetail.device_id , deviceDetail.status, step.step_order_number FROM campaign_step_device_detail deviceDetail , campaign_step step  "
			+ "WHERE deviceDetail.step_uuid = step.uuid and  deviceDetail.device_id = :deviceId and step.campaign_uuid = :campUuid and deviceDetail.status != 'FAILED'")
	List<DeviceCampaignStepStatus> getStepsByDeviceCampaign(@Param("campUuid") String campUuid, @Param("deviceId") String deviceId);
}
