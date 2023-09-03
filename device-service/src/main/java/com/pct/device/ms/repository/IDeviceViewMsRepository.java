package com.pct.device.ms.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.pct.common.dto.DeviceDto;
import com.pct.common.model.Device;
import com.pct.device.model.DeviceView;

@Repository
public interface IDeviceViewMsRepository  extends JpaRepository<DeviceView, Long>, JpaSpecificationExecutor<DeviceView>{

}
