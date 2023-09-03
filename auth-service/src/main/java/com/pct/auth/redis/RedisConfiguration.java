package com.pct.auth.redis;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "spring.redis") // Read redis related parameters in the configuration file
public class RedisConfiguration {
	private String host;
	private int port;
	private String password;
	private int timeout;
	private int poolMaxTotal;
	private int poolMaxIdle;
	private int poolMaxWait;

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public int getPoolMaxTotal() {
		return poolMaxTotal;
	}

	public void setPoolMaxTotal(int poolMaxTotal) {
		this.poolMaxTotal = poolMaxTotal;
	}

	public int getPoolMaxIdle() {
		return poolMaxIdle;
	}

	public void setPoolMaxIdle(int poolMaxIdle) {
		this.poolMaxIdle = poolMaxIdle;
	}

	public int getPoolMaxWait() {
		return poolMaxWait;
	}

	public void setPoolMaxWait(int poolMaxWait) {
		this.poolMaxWait = poolMaxWait;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public String toString() {
		return "RedisConfiguration{" + "host='" + host + '\'' + ", port=" + port + ", password='" + password + '\''
				+ ", timeout=" + timeout + ", poolMaxTotal=" + poolMaxTotal + ", poolMaxIdle=" + poolMaxIdle
				+ ", poolMaxWait=" + poolMaxWait + '}';
	}
}