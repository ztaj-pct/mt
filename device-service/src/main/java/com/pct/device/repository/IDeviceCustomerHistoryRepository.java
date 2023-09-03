package com.pct.device.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pct.common.model.DeviceCustomerHistory;

@Repository
public interface IDeviceCustomerHistoryRepository extends JpaRepository<DeviceCustomerHistory, Long> {

}
