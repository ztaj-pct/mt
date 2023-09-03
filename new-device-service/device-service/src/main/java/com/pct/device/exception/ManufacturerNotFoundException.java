package com.pct.device.exception;

import com.pct.common.util.Message;

/**
 * @author Abhishek on 24/09/20
 */
public class ManufacturerNotFoundException extends RuntimeException {
    private final Message message;

    public ManufacturerNotFoundException(Message message) {
        this.message = message;
    }

    public ManufacturerNotFoundException(Message message, Throwable cause) {
        super(cause);
        this.message = message;
    }

    public ManufacturerNotFoundException(String message) {
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
