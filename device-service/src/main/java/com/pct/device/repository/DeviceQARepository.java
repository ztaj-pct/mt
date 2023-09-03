package com.pct.device.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pct.common.model.DeviceQa;

@Repository
public interface DeviceQARepository extends JpaRepository<DeviceQa, Long> {
	
	@Query(value = "FROM DeviceQa df WHERE df.uuid = :uuid")
	DeviceQa findByUuid(@Param("uuid") String uuid);
	
	@Modifying
	@Query(value = "update DeviceQa set qa_result=:qaResult,qa_status=:qaStatus,qa_date=:qaDate WHERE uuid = :uuid")
	void update(@Param("qaResult") String qaResult, 
			@Param("qaStatus") String qaStatus, @Param("qaDate") String  qaDate, @Param("uuid") String uuid);

	@Query(value = "From DeviceQa where deviceId.imei =:deviceId ORDER BY createdOn desc")
	List<DeviceQa> findByDeviceId(@Param("deviceId") String deviceId);

	@Query(value = "From DeviceQa where deviceId.imei =:deviceId ORDER BY qaDate desc")
	List<DeviceQa> findLatestDeviceQAByDeviceID(@Param("deviceId")  String deviceId);

	
}