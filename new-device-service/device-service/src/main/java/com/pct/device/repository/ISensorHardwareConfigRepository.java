package com.pct.device.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pct.common.model.AssetSensorXref;
import com.pct.common.model.SensorHardwareConfig;

@Repository
public interface ISensorHardwareConfigRepository extends JpaRepository<SensorHardwareConfig, Long>, JpaSpecificationExecutor<SensorHardwareConfig> {


	 @Query("FROM SensorHardwareConfig asx WHERE assetUuid = :assetUuid")
	 SensorHardwareConfig findByAssetUuid(@Param("assetUuid") String assetUuid);
}
