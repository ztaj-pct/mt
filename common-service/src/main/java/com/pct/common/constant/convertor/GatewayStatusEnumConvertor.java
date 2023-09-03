package com.pct.common.constant.convertor;

import com.pct.common.constant.DeviceStatus;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class GatewayStatusEnumConvertor implements Converter<String, DeviceStatus> {

    @Override
    public DeviceStatus convert(String source) {
        return DeviceStatus.valueOf(source);
    }
}
