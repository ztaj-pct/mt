package com.pct.device.repository;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Set;

import com.pct.common.constant.DeviceStatus;
import com.pct.common.constant.IOTType;
import com.pct.common.constant.GatewayType;
import com.pct.common.model.Device;
import com.pct.common.model.DeviceDetails;
import com.pct.device.repository.projections.DeviceIdAndImeiView;
import com.pct.device.repository.projections.DeviceTypeCountView;
import com.pct.device.repository.projections.InProgressAssetTypeAndCountView;
import javax.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.transaction.Transactional;
import com.pct.common.constant.DeviceStatus;
import com.pct.common.dto.DeviceDto;
import com.pct.common.model.Device;


@Repository
public interface IDeviceRepository extends JpaRepository<Device, Long>, JpaSpecificationExecutor<Device> {

//   @Query(nativeQuery = true, value = "select product_code, count(*) as count from device where status = :status and account_number = :accountNumber group by product_code")
//    List<DeviceTypeCountView> findProductTypeAndCountByStatus(@Param("status") String status, @Param("accountNumber") String accountNumber);
//
//    @Query(nativeQuery = true, value = "SELECT a.category, count(*) as count FROM gateway g, asset a where g.asset_uuid = a.uuid AND g.asset_uuid is NOT null and g.status ='Error' group by a.category")
//    List<InProgressAssetTypeAndCountView> findByAssetIdNotNullAndErrorStatus();
//
//    @Query(nativeQuery = true, value = "select id as deviceId, imei from device where imei like %:imei and asset_uuid is null")
//    List<DeviceIdAndImeiView> findIdAndImei(@Param("imei") Long imei);
//
//    @Query("UPDATE device g set g.status = 'PENDING' WHERE g.uuid = :uuid")
//    Device resetByUuid(@Param("uuid") String uuid);
//    
//    
    //Gateway findByMac_address(String mac_address);
    @Query("FROM Device device WHERE device.macAddress = :macAddress AND device.isDeleted = false")
    Device findByMac_address(@Param("macAddress") String macAddress);
    
    
//    @Query("FROM Device device WHERE device.type = 'Sensor' and device.uuid := uuid")
//    List<Device> findBySensorUuid(@Param("uuid") String uuid);
//
//   // @Query("FROM device g WHERE g.imei = :imei AND g.company.accountNumber = :accountNumber")
//    Device findByImeiAndAccountNumber(@Param("imei") String imei,
//                                             @Param("accountNumber") String accountNumber);
//    
//    
//   // @Query("FROM Device device WHERE device.company.accountNumber = :accountNumber AND device.type = :type")
//    List<Device> findByAccountNumberAndType(@Param("accountNumber") String accountNumber, @Param("type") Device type);
//    
    @Query("FROM Device g WHERE g.macAddress = :mac AND g.organisation.accountNumber = :accountNumber AND g.isDeleted = false")
    Device findByMACAndAccountNumber(@Param("mac") String mac, @Param("accountNumber") String accountNumber);

   
//    @Query("FROM Device device WHERE device.asset is null")
//    List<Device> findByAssetIsNull();
//
//    @Query("FROM Device device WHERE device.company.accountNumber = :accountNumber AND device.uuid = :uuid")
//    List<Device> findByAccountNumberAndUuid(@Param("accountNumber") String accountNumber, @Param("uuid") String uuid);
//
//    @Query("FROM Device device WHERE device.company.accountNumber = :accountNumber")
//    List<Device> findByAccountNumber(@Param("accountNumber") String accountNumber);

    @Query("FROM Device device WHERE device.uuid = :uuid AND device.isDeleted = false")
    Device findByUuid(@Param("uuid") String uuid);
    
//    @Query("FROM DeviceCompany deviceCompany WHERE deviceCompany.ms1_comapny_name = :ownerLevel2")
    

//    
//    @Query(nativeQuery = true, value = "SELECT status FROM device where  imei = :imei ")
//    String findStatusByImei(@Param("imei") String imei);
    
//    @Query(nativeQuery = true, value = "SELECT * FROM device where  imei = :imei , uuid = : deviceUuid and type = 'GATEWAY'")
//	Device findByUuidAndImei(@Param("imei") String imei, @Param("") String deviceUuid);
    
	@Query("FROM Device device  WHERE device.imei = :imei AND device.isDeleted = false")
	Page<Device> findAllBySpecAndPagination(Pageable pageable,@Param("imei") String imei);
	
	
//	@Query("FROM Device device  WHERE device.organisation.organisationName = :can, device.macAddress = :mac,device.uuid = :uuid AND device.type = : type")
//	List<Device> findByParam(@Param("can") String can,@Param("mac") String mac,@Param("uuid") String uuid,@Param("type") DeviceType type);
	
//    @Query("FROM Gateway g  WHERE g.company.company = :companyName")
//    Page<Gateway> findAllByCompanyAndPagination(Pageable page, @Param("companyName") String companyName);
    
//	@Query("FROM Device device  WHERE device.organisation.organisationName = :can, device.imei = :imei,device.uuid = :uuid AND device.type = :type")
//	List<Device> findByCanAndImeiAndUuidAndType(String can,String imei,String uuid,DeviceType type);
//
    @Transactional
    @Modifying
    @Query( nativeQuery = true, value = "Update pct_device.device  set  latest_report = :latestReport  where device.imei = :imei AND device.isDeleted = false")
    Integer updateLatestReport(@Param("imei") String imei, @Param("latestReport") Date latestReport  ) ;
    
    
    @Query("SELECT d.imei FROM Device d WHERE d.organisation.accountNumber=:can AND d.isDeleted = false")
	Set<String> findImeiByOrganisationAccountNumber(@Param("can") String can);
    @Transactional
    @Modifying
    @Query( nativeQuery = true, value = "update pct_device.device set asset_device =null where asset_device= :assetXrefId AND is_deleted = false")
    Integer updateDeviceAssetMapping(@Param("assetXrefId") Long assetXrefId) ;
	
    //@Query("Select count(*) from Device device where can in :idList")
    //int getCountByOrganizationId(@Param("idList") List<String> idList);
    
    @Transactional
    @Modifying
    @Query( nativeQuery = true, value = "update pct_device.device set can =:can where imei= :imei")
    Integer updateDeviceCustomer(@Param("imei") String imei,@Param("can") String can) ;
    
    @Query("Select device from Device device where device.imei in :imeiList")
    List<Device> getListOfDeviceByImeiList(@Param("imeiList") Set<String> imeiList);
    
    @Query("Select device from Device device where device.uuid in :uuidList")
    List<Device> getListOfDeviceByUuidList(@Param("uuidList") Set<String> uuidList);

    @Query("SELECT d FROM Device d WHERE d.imei IN :imeis")
    List<Device> findByImei(@Param("imeis") List<String> imeis);
    
    @Transactional
    @Modifying
    @Query( nativeQuery = true, value = "update pct_device.device set asset_device = null where imei= :imei")
    Integer updateDeviceAsset(@Param("imei") String imei) ;
    
    @Transactional
    @Modifying
    @Query( nativeQuery = true, value = "UPDATE pct_device.device de,pct_device.asset_device_xref xref SET de.asset_device = xref.id where de.uuid = xref.device_uuid;")
    Integer updateDeviceAssetData() ;
    @Query("Select count(*) from Device  where can in :idList AND isDeleted = false")
    int getCountByOrganizationId(@Param("idList") List<String> idList);  

   @Query("FROM Device device  WHERE device.imei = :imei AND device.isDeleted = false")
	Device findByImei(@Param("imei") String imei);

   @Query("FROM Device g WHERE g.imei = :imei AND g.organisation.accountNumber = :accountNumber AND g.isDeleted = false")
   Device findByImeiAndAccountNumber(@Param("imei") String imei,
                                            @Param("accountNumber") String accountNumber);
   
   
   // ------------------------------Aamir 1 start ---------------------------------------//
   
   @Query("FROM Device d WHERE d.iotType = 'SENSOR' AND d.organisation.accountNumber = :can AND d.isDeleted = false")
   List<Device> findByGatewayCan(@Param("can") String can);
   
   @Query("FROM Device d WHERE d.organisation.accountNumber = :accountNumber AND d.status = :status and d.isDeleted = false")
   List<Device> findByAccountNumberAndStatus(@Param("accountNumber") String accountNumber, @Param("status") DeviceStatus status);
   
   @Query("FROM Device d WHERE d.organisation.accountNumber = :accountNumber AND d.isDeleted = false")
   List<Device> findByAccountNumber(@Param("accountNumber") String accountNumber);
   
    @Query("FROM Device gateway WHERE gateway.organisation.accountNumber = :accountNumber AND gateway.isDeleted = false")
   Page<Device> findByAccountNumberWithPagination(@Param("accountNumber") String accountNumber, Pageable page);
   
   @Query("FROM Device gateway WHERE gateway.organisation.accountNumber IN (:cans) AND gateway.isDeleted = false")
   Page<Device> findByListOfAccountNumberWithPagination(@Param("cans") List<String> cans, Pageable page);
   
   @Query("UPDATE Device g set g.status = 'PENDING' WHERE g.uuid = :uuid")
   Device resetByUuid(@Param("uuid") String uuid);
   
   @Query("FROM Device gateway WHERE gateway.organisation.accountNumber = :accountNumber and gateway.timeOfLastDownload >= :lastDownloadeTime and gateway.isDeleted = false")
   Page<Device> findByAccountNumberWithPagination(@Param("accountNumber") String accountNumber,@Param("lastDownloadeTime") Instant lastDownloadeTime, Pageable page);
   
   @Query("FROM Device gateway WHERE gateway.organisation.accountNumber IN (:cans) and gateway.timeOfLastDownload >= :lastDownloadeTime and gateway.isDeleted = false")
   Page<Device> findByListOfAccountNumberWithPagination(@Param("cans") List<String> cans, @Param("lastDownloadeTime") Instant lastDownloadeTime, Pageable page);
   
   @Query("FROM Device gateway WHERE gateway.organisation.accountNumber IN (:cans) and gateway.timeOfLastDownload >= :lastDownloadeTime and gateway.isDeleted = false")
   Page<Device> findDeviceByCan(@Param("cans") List<String> cans, @Param("lastDownloadeTime") Instant lastDownloadeTime, Pageable page);
   
    
    @Transactional
    @Modifying
    @Query( nativeQuery = true, value = "UPDATE device de,device_details details SET de.device_details_id = details.imei where de.imei = details.imei;")
    Integer updateDeviceDetailsData() ;
    
    @Query("SELECT d FROM Device d WHERE d.imei=:imei and d.organisation.accountNumber =:can")
	Device getByDeviceIdAndCan(@Param("imei") String imei,@Param("can") String can);
    
    @Query("SELECT d FROM Device d WHERE d.imei=:imei")
	Device getByDeviceId(@Param("imei") String imei);
    
    @Query("SELECT dd FROM DeviceDetails dd where dd.imei=:deviceId and dd.mainPower > 11 and dd.latestReport > :deviceQATimestamp")
	DeviceDetails findMainPowerReportAfterInstallation(@Param("deviceId") String deviceId, @Param("deviceQATimestamp") Timestamp deviceQATimestamp);
    
    @Query("SELECT dd FROM DeviceDetails dd where dd.imei=:deviceId and dd.auxPower > 11 and dd.latestReport > :deviceQATimestamp")
   	DeviceDetails findAltPowerReportAfterInstallation(@Param("deviceId") String deviceId,@Param("deviceQATimestamp") Timestamp deviceQATimestamp);
    
    @Query("SELECT dd FROM DeviceDetails dd where dd.imei=:deviceId and dd.gpsStatus = 'Locked' and dd.latestReport > :deviceQATimestamp")
   	DeviceDetails findGPSLockedReportAfterInstallation(@Param("deviceId") String deviceId, @Param("deviceQATimestamp") Timestamp deviceQATimestamp);
    
    
   
    //--------------Testing API --------------------------------------------//
    
    @Query(value = "SELECT new com.pct.common.dto.DeviceDto(c.uuid,c.imei,c.macAddress) FROM Device c WHERE c.imei = :imei AND c.isDeleted = false")
    DeviceDto findByTestMac_address(@Param("imei") String imei);
    
    
    @Query(value = "SELECT new com.pct.common.dto.DeviceDto(c.uuid,c.imei,c.macAddress) FROM Device c WHERE c.isDeleted = false")
    Page<DeviceDto> getAllDevice(Pageable pageable);
    
//    @Query(value = "select new com.pct.es.dto.DeviceDto(c.imei,c.uuid,c.imei,c.macAddress) FROM Device c WHERE c.macAddress = :mac AND c.isDeleted = false")
//    DeviceDto findByTestMACAndAccountNumber(@Param("mac") String mac);
    @Query("SELECT COUNT(d) > 0 FROM Device d WHERE d.imei !=:imei AND d.cellular.cellular=:simno")
    boolean existBySimno(@Param("imei") String imeis, @Param("simno") String simno);
    
    
    @Query("SELECT COUNT(d) > 0 FROM Device d WHERE d.imei !=:imei AND d.cellular.imsi=:serialNo")
    boolean existBySerialNumber(@Param("imei") String imeis, @Param("serialNo") String serialNo);
}
