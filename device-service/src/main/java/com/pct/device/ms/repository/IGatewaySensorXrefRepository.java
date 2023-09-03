package com.pct.device.ms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.pct.common.model.Device_Device_xref;

public interface IGatewaySensorXrefRepository extends JpaRepository<Device_Device_xref, Long>, JpaSpecificationExecutor<Device_Device_xref>{

}
