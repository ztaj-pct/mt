package com.pct.organisation.repository;

import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pct.common.model.CustomerForwardingRuleUrl;

@Repository
public interface CustomerForwardingRuleUrlRepository
		extends JpaRepository<CustomerForwardingRuleUrl, Long>, JpaSpecificationExecutor<CustomerForwardingRuleUrl> {
	
	@Query("SELECT cfru FROM CustomerForwardingRuleUrl cfru where cfru.uuid IN (:uuids)")
	Set<CustomerForwardingRuleUrl> findByUuidsIn(@Param("uuids") Set<String> uuids);
	
	@Query("SELECT cfru FROM CustomerForwardingRuleUrl cfru where cfru.format=:format AND cfru.endpointDestination=:endpointDestination")
	CustomerForwardingRuleUrl findByFormatAndEndpointDestination(@Param("format") String format, @Param("endpointDestination") String endpointDestination);
	
	@Query("SELECT cfru FROM CustomerForwardingRuleUrl cfru where cfru.uuid=:uuid")
	CustomerForwardingRuleUrl findByUuid(@Param("uuid") String uuid);
	
}
