package com.pct.device.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pct.common.model.Manufacturer;


public interface IManufacturerRepository extends JpaRepository<Manufacturer, Long>, JpaSpecificationExecutor<Manufacturer> {

    @Query("FROM Manufacturer m WHERE m.name = :name")
    public Manufacturer findByName(@Param("name") String name);

    @Query("FROM Manufacturer m WHERE m.uuid = :uuid")
    public Manufacturer findByUuid(@Param("uuid") String uuid);
}
