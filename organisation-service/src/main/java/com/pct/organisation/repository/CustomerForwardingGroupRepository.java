package com.pct.organisation.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pct.common.model.CustomerForwardingGroup;

@Repository
public interface CustomerForwardingGroupRepository
		extends JpaRepository<CustomerForwardingGroup, Long>, JpaSpecificationExecutor<CustomerForwardingGroup> {
	
	@Query("SELECT COUNT(cfg) > 0 FROM CustomerForwardingGroup cfg where cfg.uuid=:uuid")
	boolean existByUuid(@Param("uuid") String uuid);
	
	@Query("SELECT DISTINCT cfg.name FROM CustomerForwardingGroup cfg")
	List<String> findAllDistinctName();
}
