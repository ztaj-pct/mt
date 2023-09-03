package com.pct.installer.repository;

import java.time.Instant;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pct.common.constant.InstallHistoryStatus;
import com.pct.common.dto.InProgressInstall;
import com.pct.common.model.InstallHistory;

@Repository
public interface IInstallHistoryRepository extends JpaRepository<InstallHistory, Long> , JpaSpecificationExecutor<InstallHistory>{
//Aamir
    @Query("FROM InstallHistory ih WHERE ih.asset.id= :id AND ih.device.id= :deviceId")
    List<InstallHistory> findByAssetAndGateway(@Param("id") Long id, @Param("deviceId") Long deviceId);

    @Query("FROM InstallHistory ih WHERE ih.asset.id = :assetId")
    List<InstallHistory> findByAssetId(@Param("assetId") Long assetId);
//    
    @Query("FROM InstallHistory ih WHERE ih.device.id = :gatewayId")
    List<InstallHistory> findByGatewayId(@Param("gatewayId") Long gatewayId);
//
//    @Query("FROM InstallHistory ih WHERE ih.gateway.uuid = :gatewayUuid")
//    List<InstallHistory> findByGatewayUuid(@Param("gatewayUuid") String gatewayUuid);
//Aamir
    @Query("FROM InstallHistory ih WHERE ih.asset.uuid = :assetUuid AND ih.status = :status")
    List<InstallHistory> findByAssetUuidAndStatus(@Param("assetUuid") String assetUuid,
                                                  @Param("status") InstallHistoryStatus installHistoryStatus);
  //Aamir
    @Query("FROM InstallHistory ih WHERE ih.device.uuid = :deviceUuid AND ih.status = :status")
    List<InstallHistory> findByGatewayUuidAndStatus(@Param("deviceUuid") String gatewayUuid,
                                                    @Param("status") InstallHistoryStatus installHistoryStatus);

    @Query("FROM InstallHistory ih WHERE ih.installCode = :installCode")
    InstallHistory findByInstallCode(@Param("installCode") String installCode);
//
//    @Query("FROM InstallHistory ih WHERE ih.company.uuid = :companyUuid")
//    List<InstallHistory> findByCompanyUuid(@Param("companyUuid") String companyUuid);
//
//    @Query("FROM InstallHistory ih WHERE ih.company.uuid = :companyUuid AND ih.status = :status")
//    List<InstallHistory> findByCompanyUuidAndStatus(@Param("companyUuid") String companyUuid,
//                                                    @Param("status") InstallHistoryStatus installHistoryStatus);
//
//    @Query("FROM InstallHistory ih WHERE ih.asset.uuid IN :assetUuids AND ih.gateway.imei like %:imei%")
//    List<InstallHistory> findByAssetUuidswithImei(@Param("assetUuids") List<String> assetUuids, @Param("imei") String imei);
//    
//    @Query("FROM InstallHistory ih WHERE ih.asset.uuid IN :assetUuids AND ih.status = 'FINISHED' AND ih.dateEnded between :installed and :installeda")
//    List<InstallHistory> findByAssetUuidswithInstalledDate(@Param("assetUuids") List<String> assetUuids, @Param("installed") Instant installed,@Param("installeda") Instant installed1);
//    
//    @Query("FROM InstallHistory ih WHERE ih.asset.uuid IN :assetUuids AND ih.gateway.imei like %:imei% AND ih.status = 'FINISHED' AND ih.dateEnded between :installed and :installeda")
//    List<InstallHistory> findByAssetUuidswithImeiAndInstalledDate(@Param("assetUuids") List<String> assetUuids, @Param("imei") String imei, @Param("installed") Instant installed,@Param("installeda") Instant installed1);
//    
//    @Query("FROM InstallHistory ih WHERE ih.asset.uuid IN :assetUuids")
//    List<InstallHistory> findByAssetUuids(@Param("assetUuids") List<String> assetUuids);
//    
    
    @Query("FROM InstallHistory ih WHERE ih.organisation.uuid = :companyUuid AND ih.status = :status")
    List<InstallHistory> findByCompanyUuidAndStatus(@Param("companyUuid") String companyUuid,
                                                    @Param("status") InstallHistoryStatus installHistoryStatus);
    
    @Query(" SELECT new com.pct.common.dto.InProgressInstall(ih.device.uuid   ,ih.asset.uuid , ih.device.imei ,ih.device.macAddress , ih.asset.vin ,ih.asset.assignedName , ih.installCode ,ih.status ) FROM InstallHistory ih WHERE ih.organisation.uuid = :companyUuid AND ih.status = :status")
    List<InProgressInstall> findByCompanyUuidAndStatusByDto(@Param("companyUuid") String companyUuid,
                                                    @Param("status") InstallHistoryStatus installHistoryStatus);
    
    @Query(" SELECT new com.pct.common.dto.InProgressInstall(ih.device.uuid   ,ih.asset.uuid , ih.device.imei ,ih.device.macAddress , ih.asset.vin ,ih.asset.assignedName , ih.installCode ,ih.status ) FROM InstallHistory ih WHERE ih.organisation.uuid = :companyUuid AND ih.status = :status")
    Page<InProgressInstall> findByCompanyUuidAndStatusByDtoV2(@Param("companyUuid") String companyUuid,
                                                    @Param("status") InstallHistoryStatus installHistoryStatus,Pageable pageable);
    
    //----------------------------------Aamir 1 Start-------------------------------------/
   
    @Query("FROM InstallHistory ih WHERE ih.organisation.uuid = :companyUuid")
	List<InstallHistory> findByCompanyUuid(@Param("companyUuid") String companyUuid); 
    
    @Query("FROM InstallHistory ih WHERE ih.device.uuid = :deviceUuid")
	List<InstallHistory> findByGatewayUuid(@Param("deviceUuid") String deviceUuid);
    
	@Query("FROM InstallHistory ih WHERE ih.asset.uuid IN :assetUuids AND ih.device.imei like %:imei% AND ih.status = 'FINISHED' AND ih.dateEnded between :installed and :installeda")
	List<InstallHistory> findByAssetUuidswithImeiAndInstalledDate(@Param("assetUuids") List<String> assetUuids,
			@Param("imei") String imei, @Param("installed") Instant installed, @Param("installeda") Instant installeda);

//    @Query("FROM InstallHistory ih WHERE ih.asset.uuid IN :assetUuids AND ih.status = 'FINISHED' AND ih.dateEnded between :installed and :installed1")
//    List<InstallHistory> findByAssetUuidswithInstalledDate(@Param("assetUuids") List<String> assetUuids, @Param("installed") Instant installed, @Param("installed1") Instant installed1);

	@Query("FROM InstallHistory ih WHERE ih.asset.uuid IN :assetUuids AND ih.status ='FINISHED' AND ih.dateEnded between :installed AND :installeda")
    List<InstallHistory> findByAssetUuidswithInstalledDate(@Param("assetUuids") List<String> assetUuids, @Param("installed") Instant installed, @Param("installeda") Instant installeda);

	@Query("FROM InstallHistory ih WHERE ih.asset.uuid IN :assetUuids AND ih.device.imei like %:imei%")
	List<InstallHistory> findByAssetUuidswithImei(@Param("assetUuids") List<String> assetUuids,
			@Param("imei") String imei);

	@Query("FROM InstallHistory ih WHERE ih.asset.uuid IN :assetUuids")
	List<InstallHistory> findByAssetUuids(@Param("assetUuids") List<String> assetUuids);
	
	@Query("FROM InstallHistory ih WHERE ih.device.uuid IN :deviceUUid")
	InstallHistory findByDeviceUuid(@Param("deviceUUid") String deviceUUid);
	
	@Query("FROM InstallHistory ih WHERE ih.organisation.accountNumber = :companyAccountNo AND (ih.status = 'FINISHED' OR ih.status = 'PROBLEM')")
	Page<InstallHistory> findAllByCanWithPagination(@Param("companyAccountNo") String companyAccountNo,
			Pageable pageable);

	@Query("FROM InstallHistory ih WHERE (ih.status = 'FINISHED' OR ih.status = 'PROBLEM')")
	Page<InstallHistory> findAllWithPagination(Pageable pageable);
    
    //----------------------------------Aamir 1 End-------------------------------------/
    
    @Query("FROM InstallHistory ih WHERE ih.organisation.accountNumber = :companyAccountNo AND ih.device.imei = :imei AND (ih.status = 'FINISHED' OR ih.status = 'PROBLEM')")
    InstallHistory findByInstallationByCanAndImei(@Param("companyAccountNo") String companyAccountNo, @Param("imei") String imei);

    @Query("FROM InstallHistory ih WHERE ih.device.imei IN :deviceimeis")
	List<InstallHistory> findByDeviceImeis(@Param("deviceimeis") List<String> deviceimeis);
    
//    @Query("SELECT new com.pct.common.dto.InProgressInstall(ih.dateEnded) FROM InstallHistory ih WHERE ih.asset.uuid = :assetUuid AND ih.status = 'FINISHED' ")
//   	Instant getInstallationDate(@Param("assetUuid") String assetUuid);
}
