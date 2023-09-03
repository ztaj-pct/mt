package com.pct.device.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.pct.common.model.SensorDetail;

public interface SensorDetailRepository extends JpaRepository<SensorDetail, Long>, JpaSpecificationExecutor<SensorDetail> {

	 @Query("FROM SensorDetail sensorDetail WHERE sensorDetail.uuid = :uuid")
	 SensorDetail findByUuid(@Param("uuid") String uuid);
	 
	 @Query("FROM SensorDetail sensorDetail WHERE sensorDetail.sensorUUID.uuid = :sensorUuid")
	 List<SensorDetail> findListBySensorUuid(@Param("sensorUuid") String sensorUuid);
}
