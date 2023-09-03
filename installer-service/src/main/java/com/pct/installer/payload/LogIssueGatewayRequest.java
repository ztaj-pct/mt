package com.pct.installer.payload;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LogIssueGatewayRequest {
	   
	    @JsonProperty("install_code")
	    private String installCode;
	    
	    @JsonProperty("device_uuid")
	    private String deviceUuid;
	   
	    @JsonProperty("reason_code")
	    private String reasonCode;
	    
	    @JsonProperty("issue_type")
	    private String issueType;
	    
	    @JsonProperty("datetime_rt")
	    private String datetimeRT;
	
	    @JsonProperty("comment")
	    private String comment;
	    
	    private String data;
	    
	    private String status;
	    
	  

}
