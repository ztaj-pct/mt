package com.pct.device.version.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pct.device.version.model.Grouping;

/**
 * @author dhruv
 *
 */
@Repository
public interface IGroupingRepository extends 
JpaRepository<Grouping, Long>, JpaSpecificationExecutor<Grouping> {

	@Query("FROM Grouping g WHERE g.uuid = :uuid")
	Grouping findByUuid(@Param("uuid") String uuid);
}
