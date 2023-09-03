package com.pct.device.version.service;

import com.pct.device.version.payload.ExecuteCampaignRequest;

public interface ILatestDeviceMaintenanceReportService {
    void updateLatestDeviceMaintenanceReport(ExecuteCampaignRequest executeCampaignRequest, String deviceId, String msgUuid);
}