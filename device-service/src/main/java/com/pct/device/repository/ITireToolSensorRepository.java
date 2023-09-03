package com.pct.device.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.pct.device.model.TireToolSensor;


@Repository
public interface ITireToolSensorRepository extends JpaRepository<TireToolSensor, Long>, JpaSpecificationExecutor<TireToolSensor> {
	
}
