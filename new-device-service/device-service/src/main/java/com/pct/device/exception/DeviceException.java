package com.pct.device.exception;

import com.pct.common.util.Message;

public class DeviceException extends RuntimeException {
    private final Message message;
	private String title;
 

    public DeviceException(Message message) {
        this.message = message;
    }
    public DeviceException(String message, String title ) {
    	  this.message = new Message(message);
        this.setTitle(title);
    }

    public DeviceException(Message message, Throwable cause) {
        super(cause);
        this.message = message;
    }

    public DeviceException(String message) {
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
