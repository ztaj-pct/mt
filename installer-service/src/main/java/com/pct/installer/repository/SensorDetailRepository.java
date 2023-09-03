package com.pct.installer.repository;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pct.common.model.SensorDetail;
import com.pct.common.model.SensorReasonCode;

@Repository
public interface SensorDetailRepository extends JpaRepository<SensorDetail, Long> , JpaSpecificationExecutor<SensorDetail>{

	 @Query("FROM SensorDetail sensorDetail WHERE sensorDetail.uuid = :uuid")
	 SensorDetail findByUuid(@Param("uuid") String uuid);
	 
	 @Query("FROM SensorDetail sensorDetail WHERE sensorDetail.sensorUUID.uuid = :sensorUuid")
	 List<SensorDetail> findListBySensorUuid(@Param("sensorUuid") String sensorUuid);
	 
	 @Query("select count(*) from SensorDetail s where s.sensorUUID.uuid = :uuid")
	 Integer getSensorCountBySensorUuid(@Param("uuid") String uuid);
	 
	 @Query("FROM SensorDetail sensorDetail WHERE sensorDetail.sensorUUID.uuid = :sensorUuid AND sensorDetail.position = :position ")
	 SensorDetail findByIdAndPosition(@Param("sensorUuid") String sensorUuid, @Param("position") String position);
	 
	 @Query("FROM SensorDetail sensorDetail WHERE sensorDetail.sensorUUID.uuid = :sensorUuid AND sensorDetail.position = :position ")
	 SensorDetail findBySensp(@Param("sensorUuid") String sensorUuid, @Param("position") String position);
    
}
