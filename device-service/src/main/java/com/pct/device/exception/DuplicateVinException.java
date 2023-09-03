package com.pct.device.exception;

import com.pct.common.util.Message;

public class DuplicateVinException extends RuntimeException {
    private final Message message;

    public DuplicateVinException(Message message) {
        this.message = message;
    }

    public DuplicateVinException(Message message, Throwable cause) {
        super(cause);
        this.message = message;
    }

    public DuplicateVinException(String message) {
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
