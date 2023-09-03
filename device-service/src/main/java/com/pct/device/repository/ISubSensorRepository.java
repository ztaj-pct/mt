package com.pct.device.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pct.common.model.Device;
//import com.pct.common.model.SubSensor;

@Repository
public interface ISubSensorRepository extends JpaRepository<Device, Long>, JpaSpecificationExecutor<Device> {

	@Query("FROM Device device WHERE device.uuid = :uuid")
	Device findByUuid(@Param("uuid") String uuid);
	
//	@Query("FROM Device subSensor WHERE subSensor.sensor.uuid = :uuid")
//	List<Device> findBySensorUuid(@Param("uuid") String uuid);
}