package com.pct.common.constant.convertor;

import com.pct.common.constant.AssetStatus;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class AssetStatusEnumConvertor implements Converter<String, AssetStatus> {

    @Override
    public AssetStatus convert(String source) {
        return AssetStatus.valueOf(source);
    }

}
