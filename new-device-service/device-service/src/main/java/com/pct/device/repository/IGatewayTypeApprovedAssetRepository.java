package com.pct.device.repository;

import com.pct.device.model.GatewayTypeApprovedAsset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Abhishek on 02/07/20
 */

@Repository
public interface IGatewayTypeApprovedAssetRepository extends JpaRepository<GatewayTypeApprovedAsset, Long>, JpaSpecificationExecutor<GatewayTypeApprovedAsset> {

    @Query("FROM GatewayTypeApprovedAsset gta WHERE gta.gatewayProductName = :gatewayEligibility")
    List<GatewayTypeApprovedAsset> findByGatewayProductName(@Param("gatewayEligibility") String gatewayEligibility);
}
