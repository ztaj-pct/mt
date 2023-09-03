package com.pct.device.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pct.common.model.Attribute;

@Repository
public interface IAttributeRepository extends JpaRepository<Attribute, Long> {

//    @Query("FROM Attribute attribute WHERE attribute.product_master_uuid = :productUuid")
//    List<Attribute> findByProductUuid(@Param("product_master_uuid") String productUuid);

    @Query("FROM Attribute attribute WHERE attribute.uuid = :uuid")
    Attribute findByUuid(@Param("uuid") String uuid);
    
    @Query("FROM Attribute attribute WHERE attribute.productMaster.uuid = :uuid AND attribute.attributeName = :attributeName")
    List<Attribute> findByProductMasterUuidAndAttributeName(@Param("attributeName") String attributeName, @Param("uuid") String uuid);
    
    @Query("FROM Attribute attribute WHERE attribute.productMaster.uuid = :uuid")
    List<Attribute> findByProductMasterUuid(@Param("uuid") String uuid);

}
