package com.pct.device.version.util;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.pct.device.version.service.impl.CampaignServiceImpl;

/**
 *
 */

@Component
public class Schedular {
	
	Logger logger = LoggerFactory.getLogger(Schedular.class);
	Logger analysisLog = LoggerFactory.getLogger("analytics");

	@Autowired
	CampaignServiceImpl campaignService;
	
	//@Scheduled(fixedDelay = 30000, initialDelay = 7000)
	public void scheduleFixedDelayTask() {
		String msgUuid = UUID.randomUUID().toString();

		try {
		analysisLog.info("schedular Processing Start  ");
	    campaignService.findALLCampaignListDisplay(msgUuid);
	    analysisLog.info("schedular Processing Done  ");
		} catch (Exception e) {
			analysisLog.error("Exception in schedular Processing ", e);			
		}
		
	}
	
}       
