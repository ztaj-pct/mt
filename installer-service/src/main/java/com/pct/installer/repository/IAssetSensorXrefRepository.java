package com.pct.installer.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pct.common.model.AssetSensorXref;

/**
 * @author Ashish on 18/06/21
 */

@Repository
public interface IAssetSensorXrefRepository extends JpaRepository<AssetSensorXref, Long>, JpaSpecificationExecutor<AssetSensorXref> {
	 
	 @Query("FROM AssetSensorXref asx WHERE asx.asset.uuid = :assetUuid and asx.isGatewayAttached = false")
	 List<AssetSensorXref> findByAssetUuid(@Param("assetUuid") String assetUuid);

}
