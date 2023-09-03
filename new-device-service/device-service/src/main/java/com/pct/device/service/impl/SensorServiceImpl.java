package com.pct.device.service.impl;

import com.pct.common.constant.SensorStatus;
import com.pct.common.model.Sensor;
import com.pct.common.model.User;
import com.pct.device.bean.SensorBean;
import com.pct.device.payload.UpdateMacAddressRequest;
import com.pct.device.repository.ISensorRepository;
import com.pct.device.service.ISensorService;
import com.pct.device.specification.SensorSpecification;
import com.pct.device.util.BeanConverter;
import com.pct.device.util.RestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SensorServiceImpl implements ISensorService {

    @Autowired
    private ISensorRepository sensorRepository;
    @Autowired
    private RestUtils restUtils;
    @Autowired
    private BeanConverter beanConverter;

    @Override
    public List<SensorBean> getSensor(String accountNumber, String sensorUuid, SensorStatus sensorStatus) {
        List<SensorBean> sensorBeanList = new ArrayList<>();
        List<Sensor> sensorList = new ArrayList<Sensor>();
        Specification<Sensor> specification = SensorSpecification.getSensorListSpecification(accountNumber, sensorUuid, sensorStatus);
        sensorList = sensorRepository.findAll(specification);

        if (sensorList.size() > 0)
            sensorBeanList = sensorList.stream().map(beanConverter::convertSensorToSensorBean)
                    .collect(Collectors.toList());

        return sensorBeanList;
    }

    @Override
    public Boolean updateSensorMacAddress(UpdateMacAddressRequest updateMacAddressRequest, Long userId) {
        User user = restUtils.getUserFromAuthService(userId);
        Sensor sensor = sensorRepository.findByUuid(updateMacAddressRequest.getUuid());
        sensor.setMacAddress(updateMacAddressRequest.getMacAddress());
        sensor.setUpdatedOn(Instant.ofEpochMilli(Long.parseLong(updateMacAddressRequest.getDatetimeRT())));
        sensor.setUpdatedBy(user);
        sensor = sensorRepository.save(sensor);
        return true;
    }
}
