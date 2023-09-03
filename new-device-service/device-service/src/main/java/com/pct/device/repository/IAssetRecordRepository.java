package com.pct.device.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pct.device.model.AssetRecord;

@Repository
public interface IAssetRecordRepository extends JpaRepository<AssetRecord, Integer>, JpaSpecificationExecutor<AssetRecord> {
	@Query("FROM AssetRecord a WHERE a.companyId = :companyId")
    Page<AssetRecord> getAllAssetRecordsByCompany(Pageable page, @Param("companyId") Long companyId);
	
	@Query("FROM AssetRecord a WHERE a.companyId in :companyList")
    Page<AssetRecord> getAllAssetRecordsByCompanies(Pageable page, @Param("companyList") List<Long> companyList);
}
