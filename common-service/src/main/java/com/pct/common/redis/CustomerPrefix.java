package com.pct.common.redis;

public class CustomerPrefix extends BasePrefix {
	
	public CustomerPrefix(int expireSeconds, String prefix) {
		super(expireSeconds, prefix);
	}
	
	public static CustomerPrefix getForwardingRule = new CustomerPrefix(0, "forwardingRule");
	
	public static CustomerPrefix getDevice = new CustomerPrefix(0, "device");
}
