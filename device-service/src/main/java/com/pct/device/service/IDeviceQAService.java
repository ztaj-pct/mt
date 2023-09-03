package com.pct.device.service;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.springframework.aop.ThrowsAdvice;

import com.pct.device.exception.DeviceException;
import com.pct.device.payload.DeviceQaRequest;
import com.pct.device.payload.DeviceQaResponse;

public interface IDeviceQAService {

	Boolean addQaDeviceDetail(DeviceQaRequest deviceQaRequest) throws DeviceException;

	List<DeviceQaResponse>getAllDeviceQa() throws Exception;
	
	public boolean updateDeviceQa(DeviceQaRequest df) throws Exception;
	
	String addQADeviceDetail(HttpServletRequest request, String userName) throws ServletException,IOException, Exception;

	Boolean addQADeviceDetail1(HttpServletRequest request, String userName) throws ServletException,IOException, Exception;

}