package com.pct.device.i18n;


import org.springframework.context.support.ReloadableResourceBundleMessageSource;

import java.util.*;

public class ExposedMessageSourceImpl extends ReloadableResourceBundleMessageSource implements
        ExposedMessageSource {

    protected Properties getAllProperties(Locale locale) {
        clearCacheIncludingAncestors();
        PropertiesHolder propertiesHolder = getMergedProperties(locale);
        return propertiesHolder.getProperties();
    }

    /**
     * Get all messages for given locale.
     *
     * @param locale Locale.
     * @return Map with messages.
     */
    public Map<String, String> getAllMessages(Locale locale) {
        Properties props = getAllProperties(locale);
        Enumeration<String> keys = (Enumeration<String>) props.propertyNames();
        Map<String, String> asMap = new HashMap<>();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            asMap.put(key, props.getProperty(key));
        }
        return asMap;
    }
}
