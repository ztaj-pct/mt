package com.pct.device.ms.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pct.common.model.MaintenanceReportHistory;

@Repository
public interface MaintenanceReportHistoryRepository extends JpaRepository<MaintenanceReportHistory, Long>, JpaSpecificationExecutor<MaintenanceReportHistory> {

	@Query("FROM MaintenanceReportHistory maintenanceReportHistory WHERE maintenanceReportHistory.uuid = :uuid")
	MaintenanceReportHistory findByUuid(@Param("uuid") String uuid);
	
	@Query("FROM MaintenanceReportHistory maintenanceReportHistory WHERE maintenanceReportHistory.uuid in :uuid order by createdDate desc")
	List<MaintenanceReportHistory> findByUuidList(@Param("uuid") List<String> uuid);
}
