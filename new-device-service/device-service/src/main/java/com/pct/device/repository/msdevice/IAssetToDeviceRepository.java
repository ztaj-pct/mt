package com.pct.device.repository.msdevice;

import com.pct.device.service.device.AssetToDevice;
import com.pct.device.service.device.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * @author Abhishek on 20/01/21
 */

@Repository
public interface IAssetToDeviceRepository extends JpaRepository<AssetToDevice, Long>, JpaSpecificationExecutor<AssetToDevice> {

    @Query(nativeQuery = true, value = "select * from connectedtracking.AssetToDevice ad where ad.ASSET_ID = :assetId")
    AssetToDevice findByAssetID(@Param("assetId") String assetId);

    @Query(nativeQuery = true, value = "select * from connectedtracking.AssetToDevice ad where ad.VIN = :vin")
    AssetToDevice findByVin(@Param("vin") String vin);

    @Query(nativeQuery = true, value = "select max(RECORD_ID) from connectedtracking.AssetToDevice")
    Long findMaximumRecordId();
}
