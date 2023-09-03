package com.pct.device.ms.repository;

import java.util.List;

import javax.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pct.device.service.device.DeviceCommand;


@Repository
public interface IDeviceCommandMsRepository extends JpaRepository<DeviceCommand, Long>, JpaSpecificationExecutor<DeviceCommand> {
	
    @Query(nativeQuery = true, value = "select * from connectedtracking.device_command d where d.device_id = :deviceId order by created_date desc")
   	Page<DeviceCommand> findGatewayCommandByDeviceId(@Param("deviceId")  String deviceId,Pageable pageable);
    
//    @Query("From pct_receiver.device_command d where d.device_id = :deviceId")
//   	Page<DeviceCommand> findGatewayCommandByDeviceId(@Param("deviceId")  String deviceId,Pageable pageable);
    
//    @Query(nativeQuery = true, value = "SELECT * FROM pct_campaign.campaign c " +
//			"WHERE c.is_deleted = false AND c.campaign_name like %:campaignName% " +
//			"AND c.description like %:desc% ")
//	Page<Campaign>  findByCampaignNameAndDesc(@Param("campaignName") String campaignName , @Param("desc") String desc , Pageable pageable);
	
	
}
