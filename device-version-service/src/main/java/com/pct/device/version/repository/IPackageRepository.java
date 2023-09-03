package com.pct.device.version.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

import com.pct.device.version.model.Package;

/**
 * @author dhruv
 *
 */
@Repository
public interface IPackageRepository extends 
JpaRepository<Package, Long>, JpaSpecificationExecutor<Package> {

	//@ROBERT 4/26/2022 Query method to find non deleted packages for CSV export
	@Query("FROM Package package WHERE package.isDeleted = false")
	List<Package> findByActiveFalse();
	
	@Query("FROM Package package WHERE package.uuid = :uuid")
	Package findByUuid(@Param("uuid") String uuid);

	@Modifying(clearAutomatically = true)
	@Query("UPDATE Package package set isDeleted = true WHERE package.uuid = :uuid")
	void deleteByUuid(@Param("uuid") String uuid);

	@Query("FROM Package package WHERE package.packageName = :packageName and package.isDeleted = false")
	Package findByPackageName(@Param("packageName") String packageName);

	@Query("select p from Package p where (lower(p.appVersion) =:appVersion AND lower(p.binVersion) =:binVersion "
			+ "AND lower(p.bleVersion) =:bleVersion AND lower(p.mcuVersion) =:mcuVersion "
			+ "AND lower(p.config1) =:config1 AND lower(p.config2) =:config2 "
			+ "AND lower(p.config3) =:config3 AND lower(p.config4) =:config4 "
			+ "AND lower(p.config1Crc) =:config1Crc AND lower(p.config2Crc) =:config2Crc "
			+ "AND lower(p.config3Crc) =:config3Crc AND lower(p.config4Crc) =:config4Crc "
			+ "AND lower(p.deviceType) =:deviceType AND lower(p.liteSentryHardware) =:liteSentryHardware "
			+ "AND lower(p.liteSentryApp) =:liteSentryApp AND lower(p.liteSentryBoot) =:liteSentryBoot "
			+ "AND lower(p.microspMcu) =:microspMcu AND lower(p.microspApp) =:microspApp "
			+ "AND lower(p.cargoMaxbotixHardware) =:cargoMaxbotixHardware AND lower(p.cargoMaxbotixFirmware) =:cargoMaxbotixFirmware "
			+ "AND lower(p.cargoRiotHardware) =:cargoRiotHardware AND lower(p.cargoRiotFirmware) =:cargoRiotFirmware AND p.isDeleted = false )")
	List<Package> findIdentical(@Param("appVersion") String appVersion, @Param("binVersion") String binVersion,
			@Param("bleVersion") String bleVersion, @Param("mcuVersion") String mcuVersion,
			@Param("config1") String config1, @Param("config2") String config2, @Param("config3") String config3,
			@Param("config4") String config4, @Param("config1Crc") String config1Crc,
			@Param("config2Crc") String config2Crc, @Param("config3Crc") String config3Crc,
			@Param("config4Crc") String config4Crc, @Param("deviceType") String deviceType,
			@Param("liteSentryHardware") String liteSentryHardware, @Param("liteSentryApp") String liteSentryApp,
			@Param("liteSentryBoot") String liteSentryBoot, @Param("microspMcu") String microspMcu,
			@Param("microspApp") String microspApp, @Param("cargoMaxbotixHardware") String cargoMaxbotixHardware,
			@Param("cargoMaxbotixFirmware") String cargoMaxbotixFirmware,
			@Param("cargoRiotHardware") String cargoRiotHardware, @Param("cargoRiotFirmware") String cargoRiotFirmware);

	@Query("select p from Package p where (lower(p.appVersion) =:appVersion AND lower(p.binVersion) =:binVersion "
			+ "AND lower(p.bleVersion) =:bleVersion AND lower(p.mcuVersion) =:mcuVersion "
			+ "AND lower(p.config1) =:config1 AND lower(p.config2) =:config2 "
			+ "AND lower(p.config3) =:config3 AND lower(p.config4) =:config4 "
			+ "AND lower(p.config1Crc) =:config1Crc AND lower(p.config2Crc) =:config2Crc "
			+ "AND lower(p.config3Crc) =:config3Crc AND lower(p.config4Crc) =:config4Crc "
			+ "AND lower(p.deviceType) =:deviceType AND lower(p.liteSentryHardware) =:liteSentryHardware "
			+ "AND lower(p.liteSentryApp) =:liteSentryApp AND lower(p.liteSentryBoot) =:liteSentryBoot "
			+ "AND lower(p.microspMcu) =:microspMcu AND lower(p.microspApp) =:microspApp "
			+ "AND lower(p.cargoMaxbotixHardware) =:cargoMaxbotixHardware AND lower(p.cargoMaxbotixFirmware) =:cargoMaxbotixFirmware "
			+ "AND lower(p.cargoRiotHardware) =:cargoRiotHardware AND lower(p.cargoRiotFirmware) =:cargoRiotFirmware AND p.isDeleted = false AND p.packageId !=:packageId )")
	List<Package> findIdentical(@Param("appVersion") String appVersion, @Param("binVersion") String binVersion,
			@Param("bleVersion") String bleVersion, @Param("mcuVersion") String mcuVersion,
			@Param("config1") String config1, @Param("config2") String config2, @Param("config3") String config3,
			@Param("config4") String config4, @Param("config1Crc") String config1Crc,
			@Param("config2Crc") String config2Crc, @Param("config3Crc") String config3Crc,
			@Param("config4Crc") String config4Crc, @Param("deviceType") String deviceType,
			@Param("liteSentryHardware") String liteSentryHardware, @Param("liteSentryApp") String liteSentryApp,
			@Param("liteSentryBoot") String liteSentryBoot, @Param("microspMcu") String microspMcu,
			@Param("microspApp") String microspApp, @Param("cargoMaxbotixHardware") String cargoMaxbotixHardware,
			@Param("cargoMaxbotixFirmware") String cargoMaxbotixFirmware,
			@Param("cargoRiotHardware") String cargoRiotHardware, @Param("cargoRiotFirmware") String cargoRiotFirmware,
			@Param("packageId") Long packageId);

	

}
