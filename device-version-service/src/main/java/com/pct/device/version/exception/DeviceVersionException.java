package com.pct.device.version.exception;

import com.pct.common.util.Message;

public class DeviceVersionException extends RuntimeException {
    private final Message message;

    public DeviceVersionException(Message message) {
        this.message = message;
    }

    public DeviceVersionException(Message message, Throwable cause) {
        super(cause);
        this.message = message;
    }

    public DeviceVersionException(String message) {
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
