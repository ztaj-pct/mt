package com.pct.device.version.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pct.device.version.model.LatestDeviceMaintenanceReport;
import com.pct.device.version.payload.ExecuteCampaignRequest;
import com.pct.device.version.repository.ILatestDeviceMaintenanceReportRepository;
import com.pct.device.version.service.ILatestDeviceMaintenanceReportService;
import com.pct.device.version.util.BeanConverter;

@Service
public class ILatestDeviceMaintenanceReportServiceImpl implements ILatestDeviceMaintenanceReportService {
    Logger logger = LoggerFactory.getLogger(ILatestDeviceMaintenanceReportServiceImpl.class);

    @Autowired
    private ILatestDeviceMaintenanceReportRepository iLatestDeviceMaintenanceReportRepository;

    @Autowired
    private BeanConverter beanConverter;

    @Override
    public void updateLatestDeviceMaintenanceReport(ExecuteCampaignRequest executeCampaignRequest, String deviceId, String msgUuid) {
        
    	logger.info("Inside ILatestDeviceMaintenanceReportServiceImpl updateLatestDeviceMaintenanceReport");
        logger.debug("Inside ILatestDeviceMaintenanceReportServiceImpl updateLatestDeviceMaintenanceReport Message UUID (1): " + msgUuid);

        LatestDeviceMaintenanceReport latestDeviceMaintenanceReport = beanConverter
                .getLatestDeviceMaintenanceReport(executeCampaignRequest, deviceId, msgUuid);
        iLatestDeviceMaintenanceReportRepository.save(latestDeviceMaintenanceReport);

        logger.info("Exit ILatestDeviceMaintenanceReportServiceImpl updateLatestDeviceMaintenanceReport Message UUID (2): " + msgUuid); 
    }
}