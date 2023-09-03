package com.pct.organisation.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pct.common.model.CustomerForwardingGroup;
import com.pct.common.model.CustomerForwardingGroupMapper;
import com.pct.common.model.CustomerForwardingGroupMapperId;

@Repository
public interface CustomerForwardingGroupMapperRepository
		extends JpaRepository<CustomerForwardingGroupMapper, CustomerForwardingGroupMapperId>,
		JpaSpecificationExecutor<CustomerForwardingGroupMapper> {

	@Query("SELECT cfgm.customerForwardingGroup FROM CustomerForwardingGroupMapper cfgm WHERE cfgm.id.organisationUuid=:organisationUuid")
	List<CustomerForwardingGroup> findCustomerForwardingGroupByOrganisationUuid(
			@Param("organisationUuid") String organisationUuid);
	
	@Query("SELECT cfgm FROM CustomerForwardingGroupMapper cfgm WHERE cfgm.id.organisationUuid=:organisationUuid")
	List<CustomerForwardingGroupMapper> findByOrganisationUuid(
			@Param("organisationUuid") String organisationUuid);
}
