package com.pct.device.command.repository;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pct.common.dto.DeviceCommandResponseDTO;
import com.pct.device.command.entity.DeviceCommand;

@Repository
public interface IDeviceCommandRepository
		extends JpaRepository<DeviceCommand, Long>, JpaSpecificationExecutor<DeviceCommand> {

	@Query("FROM DeviceCommand deviceCommandManager WHERE deviceCommandManager.uuid = :uuid")
	DeviceCommand findByUuid(@Param("uuid") String uuid);

	@Query("FROM DeviceCommand deviceCommandManager WHERE deviceCommandManager.deviceId = :deviceId and deviceCommandManager.atCommand = :atCommand and deviceCommandManager.is_success is false")
	List<DeviceCommand> findByDeviceIdAndAtCommand(@Param("deviceId") String deviceId,
			@Param("atCommand") String atCommand);

	@Query("FROM DeviceCommand deviceCommandManager WHERE deviceCommandManager.deviceId = :deviceId and deviceCommandManager.uuid = :uuid and deviceCommandManager.is_success is false")
	List<DeviceCommand> findByDeviceIdAndUUID(@Param("deviceId") String deviceId, @Param("uuid") String uuid);

	@Query(nativeQuery = true, value = "select * from device_command d where d.device_id = :deviceId order by created_date desc")
	Page<DeviceCommand> findGatewayCommandByDeviceId(@Param("deviceId") String deviceId, Pageable pageable);
	
	@Query(nativeQuery = true, value = "select * from device_command d where d.uuid = :uuid")
	DeviceCommand findGatewayCommandByDeviceIdAndUuid(@Param("uuid") String uuid);
	    
   
    

	@Query(value = "select new com.pct.common.dto.DeviceCommandResponseDTO(d.uuid, d.deviceId, d.atCommand, d.deviceResponse, d.status) from DeviceCommand d where d.uuid = :uuid")
	DeviceCommandResponseDTO findGatewayCommandByDeviceUuid(@Param("uuid") String uuid);
	
	@Query(nativeQuery = true, value = "select * from device_command d where d.at_command = :atCommand and d.device_id = :deviceId order by sent_timestamp desc limit 1")
	DeviceCommand findGatewayCommandByDeviceIdAndLatestTimeStampt(@Param("atCommand") String atCommand, @Param("deviceId") String deviceId);
	
	
	@Query(nativeQuery = true, value = "SELECT * FROM device_command where success = false and DATE_SUB(response_timestamp, INTERVAL 12 hour)>=sent_timestamp;")
	List<DeviceCommand> findDeviceCommandBySentAndReceivedTime();

	@Transactional
    @Modifying
    @Query( nativeQuery = true, value = "update device_command set success = true where id= :id")
    Integer updateCommandStatus(@Param("id") Long id) ;
	
//	@Query(value = "select new com.pct.common.dto.DeviceCommandResponseDTO(d.uuid, d.deviceId, d.atCommand, d.deviceResponse, d.status) from DeviceCommand d where d.uuid = :uuid")
//	DeviceCommandResponseDTO findGatewayCommandByDeviceUuid(@Param("uuid") String uuid);
}
