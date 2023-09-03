package com.pct.device.command.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.pct.device.command.entity.DeviceCommand;
import com.pct.device.command.repository.IDeviceCommandRepository;
import com.pct.device.command.util.MailUtil;

@Component
public class SchedulerService {

	private static final Logger logger = LoggerFactory.getLogger(SchedulerService.class);

	@Autowired
	IDeviceCommandRepository deviceCommandRepository;

	@Autowired
	MailUtil mailUtil;

	@Scheduled(cron = "0 0/30 * * * ?")
	public void fixedRateSch() {
		try {
			logger.info("Inside fixedRateSch....");
			List<DeviceCommand> deviceCommandList = deviceCommandRepository.findDeviceCommandBySentAndReceivedTime();
			logger.info("received list of device commands");
			for (DeviceCommand deviceCommand : deviceCommandList) {
				try {
					if (deviceCommand.getDeviceResponse() != null) {
						logger.info("sending response received mail");
						mailUtil.sendMail(deviceCommand);
					} else {
						logger.info("sending response not received mail");
						mailUtil.responseNotReceivedMail(deviceCommand);
					}
					deviceCommandRepository.updateCommandStatus(deviceCommand.getId());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
