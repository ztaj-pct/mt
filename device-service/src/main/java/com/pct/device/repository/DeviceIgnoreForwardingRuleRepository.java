package com.pct.device.repository;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pct.common.model.DeviceIgnoreForwardingRule;

@Repository
public interface DeviceIgnoreForwardingRuleRepository extends JpaRepository<DeviceIgnoreForwardingRule, Long> {

	@Query("Select difr FROM DeviceIgnoreForwardingRule difr where difr.device.imei IN (:imeis)")
	public Set<DeviceIgnoreForwardingRule> findByDeviceiImeisIn(@Param("imeis") Set<String> imeis);
	
	@Query("Select difr, difr.device FROM DeviceIgnoreForwardingRule difr where difr.device.organisation.accountNumber=:customerAccountNumber")
	public Set<DeviceIgnoreForwardingRule> findByCustomerAccountNumber(@Param("customerAccountNumber") String customerAccountNumber);

	@Modifying
	@Query(value = "delete from DeviceIgnoreForwardingRule df where df.device.imei = :device_id")
	int deleteByDeviceId(@Param("device_id") String deviceId);

	@Query("Select count(difr) > 0 FROM DeviceIgnoreForwardingRule difr where difr.customerForwardingRuleUuid=:uuid and difr.device.imei=:deviceId")
	public Boolean isExistCustomerForwardingRuleUuidAndDeviceId(@Param("uuid") String uuid, @Param("deviceId") String deviceId);

	@Query("Select difr FROM DeviceIgnoreForwardingRule difr where difr.customerForwardingRuleUuid IN (:customerForwardingRuleUuids) and difr.device.imei =:imei")
	public List<DeviceIgnoreForwardingRule> findByCustomerForwardingRuleUuidIn(
			@Param("customerForwardingRuleUuids") List<String> customerForwardingRuleUuids, @Param("imei") String deviceId);
	
	@Query("Select difr FROM DeviceIgnoreForwardingRule difr where difr.device.imei = :imei")
	public Set<DeviceIgnoreForwardingRule> findByDeviceiImei(@Param("imei") String imei);
}
