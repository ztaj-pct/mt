package com.pct.device.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.pct.device.model.TireTool;


@Repository
public interface ITireToolRepository extends JpaRepository<TireTool, Long>, JpaSpecificationExecutor<TireTool> {
	
}
