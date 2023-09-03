package com.pct.common.constant.convertor;

import com.pct.common.constant.SensorStatus;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class SensorStatusConvertor implements Converter<String, SensorStatus> {

    @Override
    public SensorStatus convert(String source) {
        return SensorStatus.valueOf(source);
    }

}
