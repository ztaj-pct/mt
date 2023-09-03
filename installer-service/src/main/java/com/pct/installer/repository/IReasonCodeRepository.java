package com.pct.installer.repository;

import com.pct.common.model.ReasonCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface IReasonCodeRepository extends JpaRepository<ReasonCode, Long> {

    @Query("FROM ReasonCode rc WHERE rc.code = :code AND rc.issueType = :issueType")
    ReasonCode findByCodeAndIssueType(@Param("code") String code,
                                      @Param("issueType") String issueType);
}
