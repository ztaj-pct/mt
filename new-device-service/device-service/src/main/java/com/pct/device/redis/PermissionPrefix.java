package com.pct.device.redis;

public class PermissionPrefix extends BasePrefix {
	
	public PermissionPrefix(int expireSeconds, String prefix) {
		super(expireSeconds, prefix);
	}
	
	/**
     * Set up permission cache
     */
	
	public static PermissionPrefix getPermission = new PermissionPrefix(0, "permission");

}
