package com.pct.device.ms.repository;

import java.util.List;

import javax.validation.Valid;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pct.device.service.device.DeviceDetail;
import com.pct.device.service.device.DeviceReport;
import com.pct.device.service.device.DeviceReportView;

/**
 * @author Abhishek on 16/04/20
 */
@Repository
public interface IDeviceMsRepository extends JpaRepository<DeviceReport, Long>, JpaSpecificationExecutor<DeviceReport> {
	
	

//    @Query(nativeQuery = true, value = "select * from connectedtracking.Device d where d.OWNER_LEVEL_2 = :customerNumber ")
//    List<DeviceDetail> findDeviceByCustomerNumber( @Param("customerNumber") String customerNumber);

//    @Query(nativeQuery = true, value = "select * from pct_receiver.device_report d where d.device_id IN (:imeiList) ")
//	List<DeviceReport> findDeviceReportByImei(@Param("imeiList") @Valid List<String> imeiList);
    
    @Query(nativeQuery = true, value = "select * from pct_receiver.device_report d where d.device_id IN (:imeiList) ")
	List<DeviceReport> findDeviceReportByImei(@Param("imeiList") @Valid String imeiList);
    
    @Query(nativeQuery = true, value = "select * from pct_receiver.device_latest_report d where d.device_id = :imei")
   	DeviceReport findLatestDeviceReportByImei(@Param("imei")  String imei);


/*    @Query(nativeQuery = true, value = "Select  e.* "
    		+ " from (SELECT  dr.DEVICE_ID, dr.BASEBAND_SW_VERSION, dr.APP_SW_VERSION, dr.EXTENDER_VERSION  FROM connectedtracking.DeviceReport as dr where dr.device_id "
    		+ "in (:imeiList) order by  dr.TIMESTAMP_RECEIVED DESC ) as e group by e.device_id ")
   	List<DeviceReportView> findMaintReportsByDeviceImei(@Param("imeiList")  List<String> imeiList);*/
    
//    @Query(nativeQuery = true, value = "Select dr.DEVICE_ID, dr.BASEBAND_SW_VERSION, dr.APP_SW_VERSION, dr.EXTENDER_VERSION from "
//    		+ "connectedtracking.DeviceReport as dr where dr.REPORT_ID = ( select max(REPORT_ID) from connectedtracking.DeviceReport as dr1 where dr1.event_Type = 'maintenance' and DEVICE_ID = :imei ) ")
//   	DeviceReportView findMaintReportsByDeviceImei(@Param("imei")  String imei);
}
