package com.pct.device.command.util;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.slf4j.Logger;

public class Logutils {

	public static void log(String className, String methodName, String messageUUID, String message, Logger logger,
			String... params) {
		StringBuilder sb = new StringBuilder();
		sb.append("Class: " + className + " Method: " + methodName + " MessageUUID: " + messageUUID);
		if (params != null && params.length > 0) {
			for (String param : params) {
				sb.append(" " + param);
			}
		}
		sb.append(message);
		logger.info(sb.toString());
	}

	public static void log(String className, String methodName, String messageUUID, String message, Logger logger,
			int... params) {
		StringBuilder sb = new StringBuilder();
		sb.append("Class: " + className + " Method: " + methodName + " MessageUUID: " + messageUUID);
		if (params != null && params.length > 0) {
			for (int param : params) {
				sb.append(" " + param);
			}
		}
		sb.append(message);
		logger.info(sb.toString());
	}

	public static void log(String className, String methodName, String messageUUID, String message, Logger logger) {
		StringBuilder sb = new StringBuilder();
		sb.append("Class: " + className + " Method: " + methodName + " MessageUUID: " + messageUUID);
		sb.append(message);
		logger.info(sb.toString());
	}

	public static StringWriter exception(Exception e) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		return sw;
	}
}
