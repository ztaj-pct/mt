package com.pct.device.repository;


import com.pct.common.dto.Device_Sensor_Xref_Dto;
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
	  
	  @Query("FROM Device_Device_xref d WHERE d.deviceUuid.uuid = :deviceUuid and d.sensorUuid.productCode=:productCode")
	  Device_Device_xref findSensorUuidByProductCodeAndDeviceUuid(@Param("deviceUuid") String deviceUuid,@Param("productCode") String productCode);

	@Query("FROM Device_Device_xref d WHERE d.deviceUuid.id = :gatewayId")
	List<Device_Device_xref> findSensorByGatewayId(@Param("gatewayId") Long gatewayId);
	
	@Query("SELECT new com.pct.common.dto.Device_Sensor_Xref_Dto(c.sensorUuid.uuid, c.sensorUuid.status, c.sensorUuid.productCode, c.sensorUuid.macAddress, c.createdOn,  c.updatedOn, c.sensorUuid.productName) FROM Device_Device_xref c WHERE c.deviceUuid.id = :gatewayId")
	List<Device_Sensor_Xref_Dto> findSensorByDtoGatewayId(@Param("gatewayId") Long gatewayId);
	
	@Query("FROM Device_Device_xref d WHERE  d.sensorUuid = :sensorUuid")
	Device_Device_xref findSensorBySensorUuid(@Param("sensorUuid") Device sensorUuid);

}
