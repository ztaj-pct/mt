package com.pct.device.repository;

import com.pct.common.model.ManufacturerDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * @author Abhishek on 05/06/20
 */

@Repository
public interface IManufacturerDetailsRepository extends JpaRepository<ManufacturerDetails, Long>, JpaSpecificationExecutor<ManufacturerDetails> {

    @Query("FROM ManufacturerDetails md WHERE md.model = :model AND md.manufacturer.id = :manufacturerId")
    public ManufacturerDetails findByModelAndManufacturer(@Param("model") String model,
                                                          @Param("manufacturerId") Long manufacturerId);

    @Query("FROM ManufacturerDetails md WHERE md.uuid = :uuid")
    public ManufacturerDetails findByUuid(@Param("uuid") String uuid);
}
