package com.pct.device.repository;

import com.pct.common.constant.GatewayStatus;
import com.pct.common.constant.GatewayType;
import com.pct.common.model.Gateway;
import com.pct.device.repository.projections.GatewayIdAndImeiView;
import com.pct.device.repository.projections.GatewayTypeCountView;
import com.pct.device.repository.projections.InProgressAssetTypeAndCountView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Abhishek on 16/04/20
 */
@Repository
public interface IGatewayRepository extends JpaRepository<Gateway, Long>, JpaSpecificationExecutor<Gateway> {

    @Query(nativeQuery = true, value = "select product_code, count(*) as count from gateway where status = :status and account_number = :accountNumber group by product_code")
    List<GatewayTypeCountView> findProductTypeAndCountByStatus(@Param("status") String status, @Param("accountNumber") String accountNumber);

    @Query(nativeQuery = true, value = "SELECT a.category, count(*) as count FROM gateway g, asset a where g.asset_uuid = a.uuid AND g.asset_uuid is NOT null and g.status ='Error' group by a.category")
    List<InProgressAssetTypeAndCountView> findByAssetIdNotNullAndErrorStatus();

    @Query(nativeQuery = true, value = "select id as gatewayId, imei from gateway where imei like %:imei and asset_uuid is null")
    List<GatewayIdAndImeiView> findIdAndImei(@Param("imei") Long imei);

    Gateway findByImei(String imei);
    
    //Gateway findByMac_address(String mac_address);
    @Query("FROM Gateway g WHERE g.macAddress = :mac")
    Gateway findByMac_address(@Param("mac") String mac);

    @Query("FROM Gateway g WHERE g.imei = :imei AND g.company.accountNumber = :accountNumber")
    Gateway findByImeiAndAccountNumber(@Param("imei") String imei,
                                             @Param("accountNumber") String accountNumber);
    
    
    @Query("FROM Gateway gateway WHERE gateway.company.accountNumber = :accountNumber AND gateway.type = :type")
    List<Gateway> findByAccountNumberAndType(@Param("accountNumber") String accountNumber, @Param("type") GatewayType type);
    
    @Query("FROM Gateway g WHERE g.macAddress = :mac AND g.company.accountNumber = :accountNumber")
    Gateway findByMACAndAccountNumber(@Param("mac") String mac,
                                             @Param("accountNumber") String accountNumber);

    @Query("FROM Gateway gateway WHERE gateway.asset is null")
    List<Gateway> findByAssetIsNull();

    @Query("FROM Gateway gateway WHERE gateway.company.accountNumber = :accountNumber AND gateway.status = :status")
    List<Gateway> findByAccountNumberAndStatus(@Param("accountNumber") String accountNumber, @Param("status") GatewayStatus status);

    @Query("FROM Gateway gateway WHERE gateway.company.accountNumber = :accountNumber")
    List<Gateway> findByAccountNumber(@Param("accountNumber") String accountNumber);

    @Query("FROM Gateway gateway WHERE gateway.uuid = :uuid")
    Gateway findByUuid(@Param("uuid") String uuid);
    
    @Query(nativeQuery = true, value = "SELECT status FROM gateway where  imei = :imei ")
    String findStatusByImei(@Param("imei") String imei);
}
