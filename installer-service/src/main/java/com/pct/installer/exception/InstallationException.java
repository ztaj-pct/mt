package com.pct.installer.exception;

import com.pct.common.util.Message;

public class InstallationException extends RuntimeException {

	 private final Message message;
		private String title;
	 

	    public InstallationException(Message message) {
	        this.message = message;
	    }
	    public InstallationException(String message, String title ) {
	    	  this.message = new Message(message);
	        this.setTitle(title);
	    }

	    public InstallationException(Message message, Throwable cause) {
	        super(cause);
	        this.message = message;
	    }

	    public InstallationException(String message) {
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
