package com.pct.organisation.dto;

import java.util.List;
import java.util.Set;

import com.pct.common.constant.OrganisationRole;
import com.pct.common.model.User;
import com.pct.common.payload.OrganisationIdPayload;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Data
public class OrganisationDTO {

	private Long id;

	private String organisationName;

	private Boolean isActive;

	private String accountNumber;

	private String shortName;

	private String uuid;

	private Boolean isAssetListRequired;

	private User createdBy;

	private User updatedBy;

	private String epicorId;

	private String previousRecordId;
	
	private Boolean isApprovalReqForDeviceUpdate;

	private Boolean isTestReqBeforeDeviceUpdate;

	private Boolean isMonthlyReleaseNotesReq;

	private Boolean isDigitalSignReqForFirmware;
	
	private Boolean isAutoResetInstallation;

	private int noOfDevice;
	
	private List<String> accessId;
	
	private Set<OrganisationRole> organisationRole;
	
	private String salesforceAccountNumber;
	
	private String epicorAccountNumber;
	
	private List<OrganisationIdPayload> resellerList;
	
	private List<OrganisationIdPayload> installerList;
	
	private List<OrganisationIdPayload> maintenanceList;

}
