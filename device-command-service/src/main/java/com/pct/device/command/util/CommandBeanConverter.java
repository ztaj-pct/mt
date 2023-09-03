package com.pct.device.command.util;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Component;

import com.pct.common.payload.DeviceATCommandReqResponse;
import com.pct.device.command.dto.DeviceCommandBean;
import com.pct.device.command.entity.DeviceCommand;
import com.pct.device.command.entity.DeviceCommandResponse;

@Component
public class CommandBeanConverter {
	

	public DeviceCommandBean convertDeviceCommandToDeviceCommand(DeviceCommand device) {
		DeviceCommandBean deviceCommandBean = new DeviceCommandBean();
		try {
			deviceCommandBean.setAtCommand(device.getAtCommand());
			deviceCommandBean.setCreatedDate(device.getCreatedDate().toString());
			deviceCommandBean.setDeviceId(device.getDeviceId());
			deviceCommandBean.setSource(device.getSource());
			deviceCommandBean.setSuccess(device.is_success());
			deviceCommandBean.setSource(device.getSource());
			deviceCommandBean.setRetryCount(device.getRetryCount());
		} catch (Exception e) {
			e.getMessage();
		}
		return deviceCommandBean;
	}

	public com.pct.device.command.payload.DeviceCommandBean convertDeviceCommandToDeviceCommandNew(
			DeviceCommand device) {
		com.pct.device.command.payload.DeviceCommandBean deviceCommandBean = new com.pct.device.command.payload.DeviceCommandBean();
		try {
			deviceCommandBean.setAtCommand(device.getAtCommand());
			DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
					.withZone(ZoneId.of("America/Los_Angeles"));
			try {
			if (device.getCreatedDate() != null) {
				String now = dtf.format(device.getCreatedDate());
				deviceCommandBean.setCreatedDate(now);
			}
			}catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
//			deviceCommandBean.setCreatedDate(device.getCreatedDate().toString());
			deviceCommandBean.setDeviceId(device.getDeviceId());
			deviceCommandBean.setSource(device.getSource());
			deviceCommandBean.setSuccess(device.is_success());
			deviceCommandBean.setSource(device.getSource());
			deviceCommandBean.setUuid(device.getUuid());
			deviceCommandBean.setPriority(Integer.parseInt(device.getPriority()));
			deviceCommandBean.setCreatedBy(device.getCreatedBy());
			if (device.getDeviceResponse() != null)
				deviceCommandBean.setDeviceResponse(device.getDeviceResponse());
			if (device.getStatus() != null)
				deviceCommandBean.setStatus(device.getStatus());
			deviceCommandBean.setRetryCount(device.getRetryCount());
		} catch (Exception e) {
			e.getMessage();
		}
		return deviceCommandBean;
	}
	
	public com.pct.device.command.payload.DeviceCommandBean convertDeviceCommandResponseToDeviceCommand(
			DeviceCommandResponse device) {
		com.pct.device.command.payload.DeviceCommandBean deviceCommandBean = new com.pct.device.command.payload.DeviceCommandBean();
		try {
			if(device.getResponseFrom().equalsIgnoreCase("deviceresponse")) {
				try {
//				parseNewResponse(device.getDeviceRawResponse());
					String output = StringUtilities.hexToAscii(device.getDeviceRawResponse());
					deviceCommandBean.setDeviceResponse(output);
				}catch(Exception ex) {
					ex.printStackTrace();
				}
			}
			deviceCommandBean.setAtCommand(device.getAtCommand());
			DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
					.withZone(ZoneId.of("America/Los_Angeles"));
			try {
			if (device.getCreatedDate() != null) {
				String now = dtf.format(device.getCreatedDate());
				deviceCommandBean.setCreatedDate(now);
			}
			}catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
//			deviceCommandBean.setCreatedDate(device.getCreatedDate().toString());
			deviceCommandBean.setDeviceId(device.getDeviceId());
			deviceCommandBean.setSource(device.getSource());
			deviceCommandBean.setSuccess(device.isSuccess());
			deviceCommandBean.setSource(device.getSource());
			deviceCommandBean.setUuid(device.getUuid());
			deviceCommandBean.setPriority(Integer.parseInt(device.getPriority()));
			deviceCommandBean.setCreatedBy(device.getCreatedBy());
			if (device.getDeviceResponse() != null&&!device.getResponseFrom().equalsIgnoreCase("deviceresponse"))
				deviceCommandBean.setDeviceResponse(device.getDeviceResponse());
			if (device.getStatus() != null)
				deviceCommandBean.setStatus(device.getStatus());
			deviceCommandBean.setRetryCount(device.getRetryCount());
		} catch (Exception e) {
			e.getMessage();
		}
		return deviceCommandBean;
	}

	public DeviceATCommandReqResponse deviceCommanBeanToDtoConverter(DeviceCommand deviceCommand) {

		DeviceATCommandReqResponse deviceAtCommand = new DeviceATCommandReqResponse();
		if (deviceCommand != null) {
			deviceAtCommand.setUuid(deviceCommand.getUuid());
			deviceAtCommand.setId(deviceCommand.getId());

			try {
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
						.withZone(ZoneId.systemDefault());
				if (deviceCommand.getCreatedDate() != null) {
					String strDate = formatter.format(deviceCommand.getCreatedDate());
					deviceAtCommand.setCreatedOn(strDate);
				}
				if (deviceCommand.getUpdatedDate() != null) {
					String strUpdatedDate = formatter.format(deviceCommand.getUpdatedDate());
					deviceAtCommand.setUpdatedOn(strUpdatedDate);
				}

			} catch (Exception ex) {
				ex.printStackTrace();
			}

//		    deviceAtCommand.setCreatedDate(deviceCommand.getCreatedDate());
//		deviceAtCommand.setCreatedEpoch(deviceCommand.getCreatedEpoch());
//		deviceAtCommand.setUpdatedDate(deviceCommand.getUpdatedDate());
			deviceAtCommand.setDeviceId(deviceCommand.getDeviceId());
			deviceAtCommand.setAtCommand(deviceCommand.getAtCommand());
			deviceAtCommand.setSource(deviceCommand.getSource());
			deviceAtCommand.setPriority(deviceCommand.getPriority());
//		deviceAtCommand.setSentTimestamp(deviceCommand.getSentTimestamp());
			deviceAtCommand.setDeviceResponse(deviceCommand.getDeviceResponse());
			deviceAtCommand.setStatus(deviceCommand.getStatus());
			return deviceAtCommand;
		}
		return null;

	}
}
