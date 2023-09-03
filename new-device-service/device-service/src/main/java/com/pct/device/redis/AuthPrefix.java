package com.pct.device.redis;

public class AuthPrefix extends BasePrefix {
	
	public AuthPrefix(int expireSeconds, String prefix) {
		super(expireSeconds, prefix);
	}
	
	/**
     * Set up role cache
     */
	
	public static AuthPrefix getRole = new AuthPrefix(0, "role");

}
