package com.pct.device.version.exception;

import java.util.ArrayList;
import java.util.List;

import com.pct.common.util.Message;

public class DeviceVersionBatchNotificationException extends RuntimeException {
    private final Message message;
    private final List<String> object;
    
    
    public DeviceVersionBatchNotificationException(String message, List<String> objectList) {
        this.message = new Message(message);
        this.object = objectList;
    }

    public Message asMessage() {
        return message;
    }

    /**
     * Overrides RuntimeException's public String getMessage().
     *
     * @return a localized string description
     */
    @Override
    public String getMessage() {
        return this.message.toString();
    }
    
    public List<String>  getObject() {
        return this.object;
    }
}
