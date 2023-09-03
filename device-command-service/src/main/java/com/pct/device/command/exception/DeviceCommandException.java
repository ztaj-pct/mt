package com.pct.device.command.exception;

import com.pct.common.util.Message;

public class DeviceCommandException extends RuntimeException {
    private final Message message;
	private String title;
 

    public DeviceCommandException(Message message) {
        this.message = message;
    }
    public DeviceCommandException(String message, String title ) {
    	  this.message = new Message(message);
        this.setTitle(title);
    }

    public DeviceCommandException(Message message, Throwable cause) {
        super(cause);
        this.message = message;
    }

    public DeviceCommandException(String message) {
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
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
}
