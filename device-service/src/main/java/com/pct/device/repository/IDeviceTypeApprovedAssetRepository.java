package com.pct.device.repository;


import com.pct.device.model.DeviceTypeApprovedAsset;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;



public interface IDeviceTypeApprovedAssetRepository extends JpaRepository<DeviceTypeApprovedAsset, Long>, JpaSpecificationExecutor<DeviceTypeApprovedAsset> {

    @Query("FROM DeviceTypeApprovedAsset gta WHERE gta.deviceProductName = :gatewayEligibility")
    List<DeviceTypeApprovedAsset> findByGatewayProductName(@Param("gatewayEligibility") String gatewayEligibility);
}
