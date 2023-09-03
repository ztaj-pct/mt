package com.pct.device.repository.msdevice;

import com.pct.device.service.device.AssetToDeviceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * @author Abhishek on 21/01/21
 */

@Repository
public interface IAssetToDeviceHistoryRepository extends JpaRepository<AssetToDeviceHistory, Long>, JpaSpecificationExecutor<AssetToDeviceHistory> {

    @Query(nativeQuery = true, value = "select max(RECORD_ID) from connectedtracking.AssetToDeviceHistory")
    Long findMaximumRecordId();
}
