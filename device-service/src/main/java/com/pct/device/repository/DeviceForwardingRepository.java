package com.pct.device.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pct.common.model.Device;
import com.pct.common.model.DeviceForwarding;

@Repository
public interface DeviceForwardingRepository extends JpaRepository<DeviceForwarding, Long> {

	@Query(value = "FROM DeviceForwarding df WHERE df.device.imei = :devices")
	List<DeviceForwarding> findByDeviceId(@Param("devices") Device devices);
	
	@Query(value = "FROM DeviceForwarding df WHERE df.uuid = :uuid")
	DeviceForwarding findByUuid(@Param("uuid") String uuid);

	
	@Query(value = "FROM DeviceForwarding df WHERE df.device.imei = :imei")
	List<DeviceForwarding> findByImei(@Param("imei") String imei);

	@Modifying
	@Query(value = "update DeviceForwarding set updated_on=:updatedOn,type=:type,url=:url,device_id=:device_id WHERE uuid = :uuid")
	void update(@Param("updatedOn") Instant updatedOn, @Param("type") String type, @Param("url") String url,
			@Param("device_id") String device_id, @Param("uuid") String uuid);

	
//	@Query(value = "FROM DeviceForwarding df WHERE df.type = :type")
//	DeviceForwarding findByType(@Param("type") String type);

	@Modifying
	@Query(value = "delete from DeviceForwarding df where df.device.imei = :device_id")
	void deleteByDeviceId(@Param("device_id") String deviceId);
	
	@Query(value = "FROM DeviceForwarding df WHERE df.device.imei IN (:imeis)")
	List<DeviceForwarding> findByImeisIn(@Param("imeis") Set<String> imeis);
	
	@Query(value = "SELECT COUNT(df) > 0 FROM DeviceForwarding df WHERE df.uuid = :uuid")
	boolean existByUuid(@Param("uuid") String uuid);
}
