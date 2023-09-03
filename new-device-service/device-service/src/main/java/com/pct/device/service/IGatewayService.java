package com.pct.device.service;

import java.util.List;
import java.util.Map;

import com.pct.common.constant.GatewayStatus;
import com.pct.common.constant.GatewayType;
import com.pct.common.dto.InProgressInstall;
import com.pct.device.bean.GatewayBean;
import com.pct.device.payload.BeaconDetailsRequest;
import com.pct.device.payload.FetchGatewayImeiResponse;
import com.pct.device.payload.ShipmentDetailsRequest;
import com.pct.device.payload.UpdateMacAddressRequest;

public interface IGatewayService {

    FetchGatewayImeiResponse getGatewayImeiResponse(Long imei);

    List<GatewayBean> getGateway(String accountNumber, GatewayStatus status);

    List<GatewayBean> getGateway(String accountNumber, String imei, String gatewayUuid, GatewayStatus gatewayStatus, GatewayType type, String macAddress);

    Map<String, List<String>> saveShipmentDetails(ShipmentDetailsRequest shipmentDetailsRequest);
    
    Map<String, List<String>> saveBeaconDetails(BeaconDetailsRequest beaconDetailsRequest,Long userId);
    
    Boolean updateGatewayMacAddress(UpdateMacAddressRequest updateMacAddressRequest, Long userId);

    Boolean resetGateway(String can, String imei);
    
    Boolean resetGatewayWithMac(String can, String mac);

	String getGatewayInstallationStatus(String imei);
}
