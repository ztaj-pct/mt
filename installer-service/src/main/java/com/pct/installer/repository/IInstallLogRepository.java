package com.pct.installer.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pct.common.constant.EventType;
import com.pct.common.model.InstallLog;
//import com.pct.installer.constant.EventType;
//import com.pct.installer.entity.InstallLog;



/**
 * @author Abhishek on 07/05/20
 */

@Repository
public interface IInstallLogRepository extends JpaRepository<InstallLog, Long>, JpaSpecificationExecutor<InstallLog> {

    @Query("FROM InstallLog installLog WHERE installLog.installHistory.id = :installHistoryId")
    List<InstallLog> findByInstallHistory(@Param("installHistoryId") Long installHistoryId);
//
@Query("FROM InstallLog installLog WHERE installLog.installHistory.id = :installHistoryId AND installLog.eventType = :eventType ORDER BY installLog.timestamp desc")
    List<InstallLog> findByInstallHistoryIdAndEventType(@Param("installHistoryId") Long installHistoryId, @Param("eventType") EventType eventType);
//    @Query("FROM InstallLog installLog WHERE installLog.installHistory.id = :installHistoryId AND installLog.eventType = :eventType ORDER BY installLog.timestamp desc")
//    List<InstallLog> findByInstallHistoryIdAndEventType(@Param("installHistoryId") Long installHistoryId, @Param("eventType") EventType eventType);
//
//    @Query("FROM InstallLog installLog WHERE installLog.installHistory.id = :installHistoryId AND installLog.eventType = :eventType AND installLog.sensor.id = :sensorId ORDER BY installLog.timestamp desc")
//    List<InstallLog> findByInstallHistoryIdEventTypeAndSensor(@Param("installHistoryId") Long installHistoryId, @Param("eventType") EventType eventType, @Param("sensorId") Long sensorId);
//
//    @Query("FROM InstallLog il WHERE il.sensor.uuid = :uuid")
//    List<InstallLog> findBySensorUuid(@Param("uuid") String uuid);
    
//    @Query("SELECT DISTINCT installLog.instanceType FROM InstallLog installLog WHERE installLog.sensor.uuid = :uuid and installLog.instanceType IS NOT NULL")
//    List<String> findDistinctByInstanceType(@Param("uuid") String uuid);
//
//    @Query("FROM InstallLog il WHERE il.sensor.uuid = :uuid order by il.timestamp desc")
//    List<InstallLog> findBySensorUuidOrderByTimeStampDesc(@Param("uuid") String uuid);
    @Query("SELECT DISTINCT installLog.instanceType FROM InstallLog installLog WHERE installLog.sensorId = :uuid and installLog.instanceType IS NOT NULL")
    List<String> findDistinctByInstanceType(@Param("uuid") String uuid);

    @Query("FROM InstallLog il WHERE il.sensor.uuid = :uuid order by il.timestamp desc")
    List<InstallLog> findBySensorUuidOrderByTimeStampDesc(@Param("uuid") String uuid);
    
    @Query("FROM InstallLog installLog WHERE installLog.installHistory.id = :installHistoryId AND installLog.eventType = :eventType AND installLog.sensorId = :sensorId")
    List<InstallLog> findByInstallHistoryIdEventTypeAndSensor(@Param("installHistoryId") Long installHistoryId, @Param("eventType") EventType eventType, @Param("sensorId") String sensorId);
//
   @Query("FROM InstallLog il WHERE il.installHistory.installCode = :installCode order by il.timestamp desc")
    List<InstallLog> findByInstallUuidOrderByTimeStampDesc(@Param("installCode") String installCode);

    // -------------------------------Amir 1 start--------------------------------------------//
   
   @Query("FROM InstallLog il WHERE il.sensorId = :uuid")
   List<InstallLog> findBySensorUuid(@Param("uuid") String uuid);
   
   
   
   // -------------------------------Amir 1 start--------------------------------------------//
   




}
