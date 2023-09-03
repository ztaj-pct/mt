package com.pct.device.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pct.common.model.SubSensor;

@Repository
public interface ISubSensorRepository extends JpaRepository<SubSensor, Long>, JpaSpecificationExecutor<SubSensor> {

	@Query("FROM SubSensor subSensor WHERE subSensor.uuid = :uuid")
	SubSensor findByUuid(@Param("uuid") String uuid);
	
	@Query("FROM SubSensor subSensor WHERE subSensor.sensor.uuid = :uuid")
	List<SubSensor> findBySensorUuid(@Param("uuid") String uuid);
}