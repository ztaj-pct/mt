package com.pct.device.version.exception;

import com.pct.common.util.Message;

/**
 * @author Abhishek on 02/11/20
 */
public class DeviceInMultipleCampaignsException extends Exception {

    private final Message message;

    public DeviceInMultipleCampaignsException(Message message) {
        this.message = message;
    }

    public DeviceInMultipleCampaignsException(Message message, Throwable cause) {
        super(cause);
        this.message = message;
    }

    public DeviceInMultipleCampaignsException(String message) {
        this.message = new Message(message);
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
}
