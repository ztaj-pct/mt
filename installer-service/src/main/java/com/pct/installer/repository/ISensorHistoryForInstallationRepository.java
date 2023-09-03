package com.pct.installer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pct.common.model.SensorHistoryForInstallation;

@Repository
public interface ISensorHistoryForInstallationRepository extends JpaRepository<SensorHistoryForInstallation, Long> {

}
