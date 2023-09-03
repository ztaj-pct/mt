package com.pct.device.repository;

import com.pct.common.model.ProductMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface IProductMasterRepository extends JpaRepository<ProductMaster, Long> {

    @Query("FROM ProductMaster p WHERE p.productName = :productName")
    ProductMaster findByProductName(@Param("productName") String productName);

    @Query("FROM ProductMaster p WHERE p.uuid = :uuid")
    ProductMaster findByUuid(@Param("uuid") String uuid);

}
