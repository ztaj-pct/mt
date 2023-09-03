package com.pct.device.version.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pct.device.version.model.LatestDeviceMaintenanceReport;

@Repository
public interface ILatestDeviceMaintenanceReportRepository extends JpaRepository<LatestDeviceMaintenanceReport, Long>,
        JpaSpecificationExecutor<LatestDeviceMaintenanceReport> {
	
    LatestDeviceMaintenanceReport findByDeviceId(String deviceId);
    

    @Query(nativeQuery = true, value = "select * FROM latest_device_maintenance_report where device_id = :deviceId")
    LatestDeviceMaintenanceReport findLatestDeviceMaintenanceReportByDeviceId(@Param("deviceId") String deviceId);
    
    @Query(nativeQuery = true, value = "select * FROM latest_device_maintenance_report where device_id IN (:deviceId)")
    List<LatestDeviceMaintenanceReport> findLatestDeviceMaintenanceReportByDeviceId(@Param("deviceId") List<String> deviceId);
}