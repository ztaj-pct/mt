package com.pct.installer.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pct.common.model.Device;
import com.pct.common.model.WorkOrder;

@Repository
public interface IWorkOrderRepository extends JpaRepository<WorkOrder, Long>, JpaSpecificationExecutor<WorkOrder> {

	WorkOrder findWorkOrderByUuid(String uuid);

	@Query("FROM WorkOrder work WHERE work.uuid = :uuid")
	WorkOrder findByUuid(@Param("uuid") String uuid);
	
	@Query("FROM WorkOrder work WHERE work.installCode = :installCode and endDate is not null")
	List<WorkOrder> findWorkOrderByInstallCode(@Param("installCode") String installCode);
}
