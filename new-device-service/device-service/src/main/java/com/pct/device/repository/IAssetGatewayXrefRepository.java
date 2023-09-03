package com.pct.device.repository;

import com.pct.common.model.AssetGatewayXref;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Abhishek on 08/05/20
 */

@Repository
public interface IAssetGatewayXrefRepository extends JpaRepository<AssetGatewayXref, Long>, JpaSpecificationExecutor<AssetGatewayXref> {

    @Query("FROM AssetGatewayXref agx WHERE agx.asset.id = :assetId AND agx.gateway.id = :gatewayId")
    List<AssetGatewayXref> findByAssetIdAndGatewayId(@Param("assetId") Long assetId,
                                                     @Param("gatewayId") Long gatewayId);

    @Query("FROM AssetGatewayXref agx WHERE agx.asset.id = :assetId")
    List<AssetGatewayXref> findByAssetId(@Param("assetId") Long assetId);

    @Query("FROM AssetGatewayXref agx WHERE agx.gateway.id = :gatewayId")
    List<AssetGatewayXref> findByGatewayId(@Param("gatewayId") Long assetId);
}
