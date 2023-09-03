package com.pct.device.version.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.pct.device.version.model.EmergencyStop;

@Repository
public interface IEmergencyStopRepository extends 
JpaRepository<EmergencyStop, Long>, JpaSpecificationExecutor<EmergencyStop> {

}
