package com.pct.device.version.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pct.device.version.model.Campaign;
import com.pct.device.version.model.CampaignListDisplay;

@Repository
public interface ICampaignListDisplayRepository extends 
JpaRepository<CampaignListDisplay, Long>, JpaSpecificationExecutor<CampaignListDisplay> {

	long deleteByUuid(String uuid);

	CampaignListDisplay findByUuid(String uuid);
}
