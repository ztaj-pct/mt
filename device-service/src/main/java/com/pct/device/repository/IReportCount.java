package com.pct.device.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.pct.common.model.ReportCount;

@Repository
public interface IReportCount extends JpaRepository<ReportCount, String>, JpaSpecificationExecutor<ReportCount>  {

}
