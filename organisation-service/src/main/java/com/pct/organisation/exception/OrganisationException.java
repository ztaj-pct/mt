package com.pct.organisation.exception;

import com.pct.common.util.Message;

public class OrganisationException extends RuntimeException {

	 private final Message message;

	    public OrganisationException(Message message) {
	        this.message = message;
	    }

	    public OrganisationException(Message message, Throwable cause) {
	        super(cause);
	        this.message = message;
	    }

	    public OrganisationException(String message) {
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
