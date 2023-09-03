package com.pct.device.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pct.device.model.DeviceStatusTransient;

@Repository
public interface IDeviceStatusTransientRepository extends JpaRepository<DeviceStatusTransient, Long> {
		

}
