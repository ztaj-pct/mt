package com.pct.device.repository;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pct.common.model.Asset_Device_xref;
import com.pct.common.model.Device;


@Repository
public interface IAssetDeviceXrefRepository extends JpaRepository<Asset_Device_xref, Long> {
	@Query("FROM Asset_Device_xref a WHERE a.device.imei = :deviceId")
	public Asset_Device_xref findByDevice(@Param("deviceId") String deviceId);
	
	@Query("FROM Asset_Device_xref a WHERE a.device.imei = :deviceId")
	List<Asset_Device_xref> findAllByDevice(@Param("deviceId") String deviceId);

	@Query("DELETE FROM Asset_Device_xref a WHERE a.device.imei = :deviceId")
	public Asset_Device_xref deleteByDevice(@Param("deviceId") String deviceId);
	
	@Query("FROM Asset_Device_xref a WHERE a.asset.uuid = :uuid")
	List<Asset_Device_xref> findAllByAssetId(@Param("uuid") String uuid);
	
	@Query("Select a.device.imei FROM Asset_Device_xref a WHERE a.asset.assignedName in :name")
	List<String> findAllImeiByAssetId(@Param("name") List<String> name);

	@Query("FROM Asset_Device_xref a WHERE a.asset.id = :assetId AND a.device.id =:gatewayId")
	public List<Asset_Device_xref> findByAssetIdAndGatewayId(@Param("assetId") Long assetId, @Param("gatewayId") Long gatewayId);

	@Query("FROM Asset_Device_xref a WHERE a.asset.id = :assetId")
	public List<Asset_Device_xref> findByAssetId(@Param("assetId") Long assetId);

	@Query("FROM Asset_Device_xref a WHERE a.device.id = :gatewayId")
	public Asset_Device_xref findByGatewayId(@Param("gatewayId") Long gatewayId);
	
	@Query("FROM Asset_Device_xref a WHERE a.asset.uuid = :assetUuid or a.device.uuid = :deviceUuid")
	List<Asset_Device_xref> findAssetDeviceByAssetUuidAndDeviceUuid(@Param("assetUuid") String assetUuid,@Param("deviceUuid") String deviceUuid);
	
	@Query("FROM Asset_Device_xref a WHERE a.asset.uuid IN :uuid")
    List<Asset_Device_xref> findAllByAssetUuid(@Param("uuid") Set<String> uuid);
    
    @Query("Select a FROM Asset_Device_xref a WHERE a.asset.assignedName LIKE %:name%")
	List<Asset_Device_xref> findByAssetId(@Param("name") String name);
    
    @Query("Select a FROM Asset_Device_xref a WHERE a.device.imei LIKE %:imei%")
	List<Asset_Device_xref> findAllByDeviceId(@Param("imei") String imei);

	@Query("Select a FROM Asset_Device_xref a WHERE a.device.imei = :imei")
	Asset_Device_xref findByDeviceID(@Param("imei") String imei);

	@Query("select a from Asset_Device_xref a where a.asset.assignedName = :assetId and a.asset.organisation.accountNumber = :can and a.device.imei !=''" )
	public List<Asset_Device_xref> findByAssetID(@Param("assetId") String assetId ,@Param("can") String can);

	@Query("select a from Asset_Device_xref a where a.asset.organisation.accountNumber =:can and a.comment like '%Updated from Report Builder API%'" )
	public List<Asset_Device_xref> getAssetAssociationDetails(@Param("can") String can);

    @Query("select a from Asset_Device_xref a where a.device.organisation.accountNumber =:can and "
   		+ "a.device.installedStatusFlag = 'Y' and a.device.deviceDetails.latestReport < :previousDayDate" )
	List<Asset_Device_xref> findNonReportingAssets(@Param("can") String can, @Param("previousDayDate") Date previousDayDate);
	
	@Query("select a.device.imei from Asset_Device_xref a where a.device.organisation.accountNumber =:can and "
		   		+ "a.device.installedStatusFlag = 'Y' and a.asset = null" )
	List<String> processNonAssociatedReportingDevices(@Param("can") String can);
}
