package com.pct.organisation.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.pct.common.model.OrganisationSettings;

public interface IOrganisationSettingsRepository extends JpaRepository<OrganisationSettings, Long>, JpaSpecificationExecutor<OrganisationSettings>{

	OrganisationSettings findOrganisationSettingsByUuid(String uuid);
	
	List<OrganisationSettings> findOrganisationSettingsByOrganisationUuid(String uuid);
	
	@Query("FROM OrganisationSettings cs WHERE cs.organisationSectionUuid = :companySectionUuid and cs.organisationUuid = :companyUuid")
    List<OrganisationSettings> findBySectionUuidAndCompanyUuid(@Param("companySectionUuid") String companySectionUuid, @Param("companyUuid") String companyUuid);
}
