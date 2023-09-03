package com.pct.device.repository;


import com.pct.common.model.Cellular;
import com.pct.common.model.Device;
import com.pct.device.model.DeviceTypeApprovedAsset;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface ICellularRepository extends JpaRepository<Cellular, Long>, JpaSpecificationExecutor<Cellular> {
    @Query("FROM Cellular cellular WHERE cellular.uuid = :uuid")
    Cellular findByUuid(@Param("uuid") String uuid);
}
