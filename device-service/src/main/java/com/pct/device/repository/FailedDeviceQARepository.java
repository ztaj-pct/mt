package com.pct.device.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pct.common.model.FailedDeviceQA;

@Repository
public interface FailedDeviceQARepository extends JpaRepository<FailedDeviceQA, Long>{

	
}
