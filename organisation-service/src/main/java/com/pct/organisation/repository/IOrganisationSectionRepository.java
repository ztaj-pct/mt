package com.pct.organisation.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.pct.common.model.OrganisationSection;

public interface IOrganisationSectionRepository extends JpaRepository<OrganisationSection, Long>, JpaSpecificationExecutor<OrganisationSection>{

	OrganisationSection findOrganisationSectionByDisplayName(String name);
	
	@Query("FROM OrganisationSection cs WHERE cs.displayName = :displayName")
    List<OrganisationSection> findByDisplayName(@Param("displayName") String displayName);
	
	
}
