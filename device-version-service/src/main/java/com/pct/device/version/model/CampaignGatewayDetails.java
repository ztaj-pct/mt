package com.pct.device.version.model;

import com.pct.device.version.payload.DeviceStepStatus;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@ToString
@Getter
@Setter
public class CampaignGatewayDetails {
    private String imei;
    private String customerName;
    private String deviceStatusForCampaign;
    private String comments;
    private List<DeviceStepStatus> deviceStepStatus;
}
