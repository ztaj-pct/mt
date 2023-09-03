package com.pct.installer.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pct.common.model.SensorInstallInstruction;
import com.pct.installer.dto.SensorInstallInstructionDto;

/**
 * @author Abhishek on 22/06/20
 */

@Repository
public interface ISensorInstallInstructionRepository extends JpaRepository<SensorInstallInstruction, Long>, JpaSpecificationExecutor<SensorInstallInstruction> {

    @Query("FROM SensorInstallInstruction src WHERE src.sensorProductName = :productName")
    List<SensorInstallInstruction> findBySensorProductName(@Param("productName") String productName);
    
    @Query("SELECT new com.pct.installer.dto.SensorInstallInstructionDto(src.sensorProductName as sensorProductName, src.installInstruction.stepSequence as stepSequence, src.installInstruction.uuid as installInstructionUUID, src.sensorProductCode as sensorProductCode) FROM SensorInstallInstruction src WHERE src.sensorProductName = :productName")
    List<SensorInstallInstructionDto> findBySensorProductNameByDto(@Param("productName") String productName);
    
    @Query("FROM SensorInstallInstruction src WHERE src.sensorProductCode = :productCode")
    List<SensorInstallInstruction> findBySensorProductCode(@Param("productCode") String productCode);

    @Query(" select DISTINCT(s.sensorProductName) from SensorInstallInstruction s")
    List<String> findAllUniqueSensorInstallInstruction();
}
