package com.pct.device.version.repository.msdevice;

import java.math.BigInteger;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pct.device.service.device.CampaignInstalledDevice;


@Repository
public interface ICampaignInstalledDeviceMsRepository extends JpaRepository<CampaignInstalledDevice, BigInteger> {

	//@Query(nativeQuery = true, value = "select * from connectedtracking.CampaignInstalledDevice d where d.DEVICE_ID = :imei")
	//CampaignInstalledDevice findCampaignInstalledDeviceByDeviceImei(@Param("imei") String imei);

	//@Query(nativeQuery = true, value = "select Installed_Flag from connectedtracking.CampaignInstalledDevice d where d.DEVICE_ID = :imei")
	//String getInstalledFlagByDeviceImei(@Param("imei") String imei);

}
