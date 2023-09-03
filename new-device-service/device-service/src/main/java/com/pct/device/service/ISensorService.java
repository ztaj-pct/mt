package com.pct.device.service;

import com.pct.common.constant.SensorStatus;
import com.pct.device.bean.SensorBean;
import com.pct.device.payload.UpdateMacAddressRequest;

import java.util.List;

public interface ISensorService {

    List<SensorBean> getSensor(String accountNumber, String sensorUuid, SensorStatus sensorStatus);

    Boolean updateSensorMacAddress(UpdateMacAddressRequest updateMacAddressRequest, Long userId);
}
