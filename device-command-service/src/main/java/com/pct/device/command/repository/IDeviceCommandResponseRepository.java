package com.pct.device.command.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.pct.device.command.entity.DeviceCommandResponse;

public interface IDeviceCommandResponseRepository extends JpaRepository<DeviceCommandResponse, Long>, JpaSpecificationExecutor<DeviceCommandResponse>{

	@Query(nativeQuery = true, value = "select * from device_command_response d where d.device_id = :deviceId order by created_date desc")
	Page<DeviceCommandResponse> findGatewayCommandByDeviceId(@Param("deviceId") String deviceId, Pageable pageable);
}
