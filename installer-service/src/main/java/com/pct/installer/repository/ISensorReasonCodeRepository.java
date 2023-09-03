package com.pct.installer.repository;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pct.common.model.SensorReasonCode;
import com.pct.installer.dto.SensorReasonCodeDto;

/**
 * @author Abhishek on 22/06/20
 */

@Repository
public interface ISensorReasonCodeRepository extends JpaRepository<SensorReasonCode, Long>, JpaSpecificationExecutor<SensorReasonCode> {

    @Query("FROM SensorReasonCode src WHERE src.sensorProductName = :productName")
    List<SensorReasonCode> findBySensorProductName(@Param("productName") String productName);
    
    @Query("FROM SensorReasonCode src WHERE src.sensorProductCode = :productCode")
    List<SensorReasonCode> findBySensorProductCode(@Param("productCode") String productCode);
    
    @Query("SELECT new com.pct.installer.dto.SensorReasonCodeDto(src.sensorProductName as sensorProductName, src.reasonCode.code as code,src.reasonCode.value as value,src.reasonCode.issueType as issueType, src.reasonCode.uuid as reasonCodeUuid, src.sensorProductCode as sensorProductCode) FROM SensorReasonCode src WHERE src.sensorProductName = :productName")
    List<SensorReasonCodeDto> findBySensorReasonProductCodeByDto(@Param("productName") String productName);

    @Query("FROM SensorReasonCode src WHERE src.sensorProductName = :productName AND src.reasonCode.issueType = :issueType")
    List<SensorReasonCode> findBySensorProductNameAndIssueType(@Param("productName") String productName,
                                                               @Param("issueType") String issueType);
}
