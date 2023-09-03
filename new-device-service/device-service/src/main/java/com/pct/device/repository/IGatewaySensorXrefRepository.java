package com.pct.device.repository;

import com.pct.common.model.GatewaySensorXref;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Abhishek on 25/05/20
 */

@Repository
public interface IGatewaySensorXrefRepository extends JpaRepository<GatewaySensorXref, Long>, JpaSpecificationExecutor<GatewaySensorXref> {

    @Query("FROM GatewaySensorXref gsx WHERE gsx.gateway.uuid = :gatewayUuid")
    List<GatewaySensorXref> findByGatewayUuid(@Param("gatewayUuid") String gatewayUuid);
}
