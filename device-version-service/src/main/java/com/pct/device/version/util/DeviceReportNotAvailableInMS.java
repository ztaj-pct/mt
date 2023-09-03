package com.pct.device.version.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.pct.device.version.service.impl.CampaignServiceImpl;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 *
 */

@Setter
@Getter
@NoArgsConstructor
public class DeviceReportNotAvailableInMS {
	
	Logger logger = LoggerFactory.getLogger(DeviceReportNotAvailableInMS.class);
	
		public static Set<String> noReportsIds= null; 
		 
		 public static Set<String> getNoReportsIds() 
		    { 
		        if (noReportsIds == null) 
		        {	
		        	noReportsIds = new HashSet<String>();
		        }
		        return noReportsIds; 
		    }

	
}       