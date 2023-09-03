package com.pct.common.util;

import java.util.UUID;

public class Context {
	
	private String logUUId ;
	
	public Context()
	{
		logUUId = UUID.randomUUID().toString();
	}

	public String getLogUUId() {
		return logUUId;
	}

	public void setLogUUId(String logUUId) {
		this.logUUId = logUUId;
	}
	


}
