package com.pct.organisation.exception;

import com.pct.common.util.Message;

/**
 * @author Abhishek on 12/05/20
 */
public class CustomerServiceException extends RuntimeException {

	 private final Message message;

	    public CustomerServiceException(Message message) {
	        this.message = message;
	    }

	    public CustomerServiceException(Message message, Throwable cause) {
	        super(cause);
	        this.message = message;
	    }

	    public CustomerServiceException(String message) {
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
