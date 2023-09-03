package com.pct.installer.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pct.installer.entity.LogIssue;

import java.util.List;

/**
 * @author Abhishek on 11/06/20
 */
@Repository
public interface ILogIssueRepository extends JpaRepository<LogIssue, Long>, JpaSpecificationExecutor<LogIssue> {

    @Query("FROM LogIssue li WHERE li.installHistory.installCode = :installCode")
    List<LogIssue> findByInstallCode(@Param("installCode") String installCode);

    @Query("FROM LogIssue li WHERE li.device.uuid = :uuid")
    List<LogIssue> findBySensorUuid(@Param("uuid") String uuid);
    
    @Query("FROM LogIssue li WHERE li.device.uuid = :uuid order by li.createdOn desc")
    List<LogIssue> findBySensorUuidOrderByCreatedOnDesc(@Param("uuid") String uuid);

    @Query("FROM LogIssue li WHERE li.uuid = :uuid")
	LogIssue findByLogIssueUuid(@Param("uuid") String uuid);
    
    
    
}

