package com.pct.device.util;

import org.slf4j.Logger;

public class Logutils {

	public static void log(String className, String methodName, String messageUUID  ,String message ,Logger logger , String... params)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("Class: "+ className +" Method: "+ methodName+" MessageUUID: "+messageUUID);
		if(params != null && params.length > 0) {
			for (String param : params) {
				sb.append(" " + param);
			}
		}
		sb.append(message);
 		logger.info(sb.toString());
 	}
	
	public static void log(String messageUUID  ,String message ,Logger logger , String... params)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(" MessageUUID: "+messageUUID);
		if(params != null && params.length > 0) {
			for (String param : params) {
				sb.append(" " + param);
			}
		}
		sb.append(message);
 		logger.info(sb.toString());
 	}
}
