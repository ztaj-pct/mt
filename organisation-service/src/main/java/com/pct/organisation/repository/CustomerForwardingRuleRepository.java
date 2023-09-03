package com.pct.organisation.repository;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pct.common.model.CustomerForwardingRule;

@Repository
public interface CustomerForwardingRuleRepository
		extends JpaRepository<CustomerForwardingRule, Long>, JpaSpecificationExecutor<CustomerForwardingRule> {

	@Query("SELECT cfr FROM CustomerForwardingRule cfr WHERE cfr.organisation.uuid=:organisationUuid")
	List<CustomerForwardingRule> findByOrganisationUuid(@Param("organisationUuid") String organisationUuid);
	
	@Query("SELECT cfr, cfr.organisation, cfr.forwardingRuleUrl FROM CustomerForwardingRule cfr WHERE cfr.organisation.uuid IN (:organisationUuids)")
	List<CustomerForwardingRule> findByOrganisationUuidsIn(@Param("organisationUuids") Set<String> organisationUuids);
	
	@Query("SELECT cfr FROM CustomerForwardingRule cfr WHERE cfr.organisation.uuid=:organisationUuid AND cfr.forwardingRuleUrl.uuid=:forwardingRuleUrlUuid")
	CustomerForwardingRule findByOrganisationUuidAndForwardingRuleUrlUuid(@Param("organisationUuid") String organisationUuid, @Param("forwardingRuleUrlUuid") String forwardingRuleUrlUuid);
}
