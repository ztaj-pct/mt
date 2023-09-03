package com.pct.organisation.payload;

import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.pct.common.model.Organisation;

import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class OrganisationPayload {

	private Long id;
    private String OrganisationName;
    private String shortName;
    private String type;
    private Boolean status;
    private String accountNumber;
    private Boolean isAssetListRequired;
    private String uuid;
    private String priviousRecordId;
    private String epicorId;
    private List<OrganisationPayload> accessList;
    private Set<String> organisationRoles;
    private Boolean isApprovalReqForDeviceUpdate = false;
    private Boolean isTestReqBeforeDeviceUpdate = false;
	private Boolean isMonthlyReleaseNotesReq = false;
	private Boolean isDigitalSignReqForFirmware = false;
	private Boolean isAutoResetInstallation = false;
	private Long noOfDevice;

    
    private String forwardingGroup;
    private List<OrganisationPayload> resellers;
    
}
