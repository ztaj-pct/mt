package com.pct.device.repository;


import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pct.device.model.GatewaySummary;

@Repository
public interface IGatewaySummaryRepository extends JpaRepository<GatewaySummary, Integer>, JpaSpecificationExecutor<GatewaySummary> {

	@Query("FROM GatewaySummary a WHERE a.organisationId = :companyId")
    Page<GatewaySummary> getAllGatewaySummaryByCompany(Pageable page, @Param("companyId") Long companyId);
	
	@Query("FROM GatewaySummary a WHERE a.organisationId in :companyList")
    Page<GatewaySummary> getAllGatewaySummaryByCompany(Pageable page, @Param("companyList") List<Long> companyList);
}
