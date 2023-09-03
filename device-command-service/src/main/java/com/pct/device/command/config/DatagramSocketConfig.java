package com.pct.device.command.config;

import java.net.DatagramSocket;
import java.net.InetAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class DatagramSocketConfig {

	@Value("${receiver.udp.ip}")

	private String receiverIp;

	@Value("${receiver.udp.port}")

	private int recieiverPort;

	Logger logger = LoggerFactory.getLogger(DatagramSocketConfig.class);

	@Bean
	public DatagramSocket getSocket() throws Exception {
		logger.info("Starting server on IP " + receiverIp + " Port : " + recieiverPort);
		return new DatagramSocket();
	}
}