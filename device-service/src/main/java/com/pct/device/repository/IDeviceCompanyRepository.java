package com.pct.device.repository;

import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.pct.common.model.DeviceCompany;

public interface IDeviceCompanyRepository extends JpaRepository<DeviceCompany, Long> {

	@Query("FROM DeviceCompany d where d.ms1OrganisationName=:ms1OrganisationName or d.ms2OrganisationName=:ms1OrganisationName")
	List<DeviceCompany> findByMs1OrganisationName(@Param("ms1OrganisationName") String ms1OrganisationName);

	@Query("FROM DeviceCompany d WHERE d.ms1OrganisationName = :ms1OrganisationName")
	DeviceCompany findDeviceCompanyByMs1OrganisationName(@Param("ms1OrganisationName") String ms1OrganisationName);

	@Query("Select d.ms1OrganisationName as ms1OrgName, d.ms2OrganisationName as ms2OrgName from DeviceCompany d")
	List<Map<String, Object>> findMs1OrganisationNameMs2OrganisationNameAll();

}
