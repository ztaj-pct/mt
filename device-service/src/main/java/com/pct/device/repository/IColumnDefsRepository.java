package com.pct.device.repository;


import com.pct.common.model.Cellular;
import com.pct.device.model.ColumnDefs;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;


@Repository
public interface IColumnDefsRepository extends JpaRepository<ColumnDefs, Long>, JpaSpecificationExecutor<Cellular> {
}
