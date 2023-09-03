package com.pct.device.repository;

import com.pct.common.model.Sensor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ISensorRepository extends JpaRepository<Sensor, Long>, JpaSpecificationExecutor<Sensor> {

    @Query("FROM Sensor sensor WHERE sensor.gateway.uuid = :gatewayUuid")
    List<Sensor> findByGatewayUuid(@Param("gatewayUuid") String gatewayUuid);

    @Query("FROM Sensor sensor WHERE sensor.uuid = :sensorUuid")
    Sensor findByUuid(@Param("sensorUuid") String sensorUuid);

    @Query("FROM Sensor s WHERE s.gateway.company.accountNumber = :can")
    List<Sensor> findByGatewayCan(@Param("can") String can);
}
