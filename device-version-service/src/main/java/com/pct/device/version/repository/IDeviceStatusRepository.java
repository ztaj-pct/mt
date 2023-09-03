package com.pct.device.version.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pct.device.version.model.DeviceStatus;

@Repository
public interface IDeviceStatusRepository extends 
JpaRepository<DeviceStatus, Long>, JpaSpecificationExecutor<DeviceStatus> {
	@Query("FROM DeviceStatus c WHERE c.deviceId = :deviceId and c.runningStatus = :runningStatus")
	public List<DeviceStatus> getByDeviceAndStatus(@Param("deviceId") String deviceId , @Param("runningStatus") String runningStatus);
	
	
	@Query("FROM DeviceStatus c WHERE c.deviceId = :deviceId and c.runningStatus = :runningStatus and c.campaign.uuid = :campaignUUID")
	public List<DeviceStatus> getByDeviceAndStatus(@Param("deviceId") String deviceId , @Param("runningStatus") String runningStatus,@Param("campaignUUID") String campaignUUID);
	
	@Query("FROM DeviceStatus c WHERE c.deviceId = :deviceId ")
	public List<DeviceStatus> getByDevice(@Param("deviceId") String deviceId);

	 
}
