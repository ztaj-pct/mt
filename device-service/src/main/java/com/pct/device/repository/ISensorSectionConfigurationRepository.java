package com.pct.device.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.pct.common.model.SensorSectionConfiguration;

public interface ISensorSectionConfigurationRepository extends JpaRepository<SensorSectionConfiguration, Long> {
	
	@Query("FROM SensorSectionConfiguration s WHERE s.section = :section")
	 List<SensorSectionConfiguration> findBySection(@Param("section") String section);

}
