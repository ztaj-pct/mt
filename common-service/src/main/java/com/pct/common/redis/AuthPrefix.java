package com.pct.common.redis;



public class AuthPrefix extends BasePrefix {
	
	public AuthPrefix(int expireSeconds, String prefix) {
		super(expireSeconds, prefix);
	}
	
	/**
     * Set up role cache
     */
	
	public static AuthPrefix getRole = new AuthPrefix(0, "role");
	
	public static AuthPrefix getSession = new AuthPrefix(0, "session");
	
	public static AuthPrefix getTokenType = new AuthPrefix(0, "tokenType");
}
