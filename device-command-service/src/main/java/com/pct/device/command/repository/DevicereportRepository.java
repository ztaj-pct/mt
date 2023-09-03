package com.pct.device.command.repository;

import java.sql.Timestamp;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pct.device.command.entity.Devicereport;
import com.pct.device.command.entity.DevicereportPK;

@Repository
public interface DevicereportRepository extends JpaRepository<Devicereport, DevicereportPK> {

	@Query(nativeQuery = true, value = "select * from connectedtracking.DeviceReport d "
			+ "where d.DEVICE_ID = :deviceId and d.EVENT_TYPE = 'Maintenance' and d.TIMESTAMP_RECEIVED > :earliestTimestamp "
			+ "order by d.TIMESTAMP_RECEIVED desc limit 1")
	Optional<Devicereport> findDeviceReportByEventType(@Param("deviceId") String deviceId,
			@Param("earliestTimestamp") Timestamp earliestTimestamp);

	@Query(nativeQuery = true, value = "select * from connectedtracking.DeviceReport d "
			+ "where d.DEVICE_ID = :deviceId and d.TIMESTAMP_RECEIVED > :earliestTimestamp "
			+ "order by d.TIMESTAMP_RECEIVED desc limit 1")
	Optional<Devicereport> findDeviceReport(@Param("deviceId") String deviceId,
			@Param("earliestTimestamp") Timestamp earliestTimestamp);

}
