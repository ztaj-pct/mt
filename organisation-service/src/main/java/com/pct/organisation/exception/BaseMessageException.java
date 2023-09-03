package com.pct.organisation.exception;

import com.pct.common.util.Message;

/**
 * Base class for exceptions using Message.
 */
public class BaseMessageException extends RuntimeException{
	
	private final Message message;

    public BaseMessageException(Message message) {
        this.message = message;
    }

    public BaseMessageException(Throwable cause, Message message) {
        super(cause);
        this.message = message;
    }

    public BaseMessageException(String messageKey) {
        this.message = new Message(messageKey);
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
