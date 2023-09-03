package com.pct.device.version.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pct.common.dto.MsDeviceRestResponse;
import com.pct.common.model.Device;


@Repository
public interface IDeviceVersionRepository extends JpaRepository<Device, Long>, JpaSpecificationExecutor<Device> {
    @Query(value="SELECT new com.pct.common.dto.MsDeviceRestResponse(device.imei ,device.organisation.id , device.organisation.organisationName ,device.installedStatusFlag ) FROM Device device WHERE device.organisation.organisationName = :customerName AND device.iotType ='GATEWAY' ")
    List<MsDeviceRestResponse> findImeisByCustomerName(@Param("customerName") String customerName);

    @Query(value="SELECT new com.pct.common.dto.MsDeviceRestResponse(device.imei ,device.organisation.id , device.organisation.organisationName ,device.installedStatusFlag ) FROM Device device WHERE device.organisation.organisationName IN (:customersName) AND device.iotType ='GATEWAY' ")
	List<MsDeviceRestResponse> findAllDeviceByCustomersName(@Param("customersName") List<String> customersName);
  
    @Query(value="SELECT new com.pct.common.dto.MsDeviceRestResponse(device.imei ,device.organisation.id , device.organisation.organisationName ,device.installedStatusFlag ) FROM Device device WHERE device.iotType ='GATEWAY' ")
    List<MsDeviceRestResponse> findAllDevices();
    
    @Query(value="SELECT new com.pct.common.dto.MsDeviceRestResponse(device.imei ,device.organisation.id , device.organisation.organisationName ,device.installedStatusFlag ) FROM Device device WHERE device.imei IN (:imei) AND device.iotType ='GATEWAY' ")
   	List<MsDeviceRestResponse> findAllDeviceByIMEIList(@Param("imei") List<String> imei);
     
    @Query(value="SELECT new com.pct.common.dto.MsDeviceRestResponse(device.imei ,device.organisation.id , device.organisation.organisationName ,device.installedStatusFlag ) FROM Device device WHERE device.imei = :imei AND device.iotType ='GATEWAY' ")
   	MsDeviceRestResponse findDeviceByIMEI(@Param("imei") String imei);
   

}
