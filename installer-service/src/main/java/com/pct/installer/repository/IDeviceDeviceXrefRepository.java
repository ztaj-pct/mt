package com.pct.installer.repository;


import com.pct.common.model.Device;
import com.pct.common.model.Device_Device_xref;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface IDeviceDeviceXrefRepository extends JpaRepository<Device_Device_xref, Long>, JpaSpecificationExecutor<Device_Device_xref> {
	
	 @Query("FROM Device_Device_xref d WHERE d.deviceUuid = :deviceUuid")
	 List<Device_Device_xref> findByDeviceUuid(@Param("deviceUuid") Device deviceUuid);

	  @Query("FROM Device_Device_xref d WHERE d.sensorUuid = :sensorUuid")
	  List<Device_Device_xref> findBySensorUuid(@Param("sensorUuid") Device sensorUuid);
	  
	  @Query("FROM Device_Device_xref d WHERE d.sensorUuid = :sensorUuid")
	  List<Device_Device_xref> findBySensorUuid(@Param("sensorUuid") String sensorUuid);
	 
	  
	  @Query("FROM Device_Device_xref d WHERE d.deviceUuid.uuid = :deviceUuid and d.sensorUuid.productCode=:productCode")
	  Device_Device_xref findSensorUuidByProductCodeAndDeviceUuid(@Param("deviceUuid") String deviceUuid,@Param("productCode") String productCode);

	  @Query(value = "SELECT * FROM pct_device.device_device_xref where device_uuid=:sensorUuid", nativeQuery = true)
	  List<Device_Device_xref> findByDeviceUUID(@Param("sensorUuid") String sensorUuid);

	  @Query(value = "SELECT * FROM pct_device.device_device_xref  WHERE device_uuid =:Uuid", nativeQuery = true)
	  List<Device_Device_xref> findByDeviceUuid(@Param("Uuid") String uuid);
	  
	  @Query("FROM Device_Device_xref d WHERE d.deviceUuid.id = :deviceUuid")
	  List<Device_Device_xref> findByDeviceid(@Param("deviceUuid") Long deviceUuid);
	  
	  //---------------------------Aamir 1 Start----------------------------------------------//
	 
	  @Query("FROM Device_Device_xref d WHERE d.deviceUuid.uuid = :deviceUUid")
	  List<Device_Device_xref> findByGateway(@Param("deviceUUid") String deviceUUid);
	  
	  @Query("FROM Device_Device_xref d WHERE  d.sensorUuid = :sensorUuid")
	  Device_Device_xref findSensorBySensorUuid(@Param("sensorUuid") Device sensorUuid);
	  
	  
	  //---------------------------Aamir 1 End ----------------------------------------------// 
}
