package com.pct.organisation.payload;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class OrganisationPayloadForCAN {

	    private Long id;
	    private String OrganisationName;
	    private String shortName;
	    private String type;
	    private Boolean status;
	    private String accountNumber;
	    private Boolean isAssetListRequired;
	    private String uuid;
	    
	    
		public Long getId() {
			return id;
		}
		public void setId(Long id) {
			this.id = id;
		}
		public String getOrganisationName() {
			return OrganisationName;
		}
		public void setOrganisationName(String organisationName) {
			OrganisationName = organisationName;
		}
		public String getShortName() {
			return shortName;
		}
		public void setShortName(String shortName) {
			this.shortName = shortName;
		}
		public String getType() {
			return type;
		}
		public void setType(String type) {
			this.type = type;
		}
		public Boolean getStatus() {
			return status;
		}
		public void setStatus(Boolean status) {
			this.status = status;
		}
		public String getAccountNumber() {
			return accountNumber;
		}
		public void setAccountNumber(String accountNumber) {
			this.accountNumber = accountNumber;
		}
		public Boolean getIsAssetListRequired() {
			return isAssetListRequired;
		}
		public void setIsAssetListRequired(Boolean isAssetListRequired) {
			this.isAssetListRequired = isAssetListRequired;
		}
		public String getUuid() {
			return uuid;
		}
		public void setUuid(String uuid) {
			this.uuid = uuid;
		}
	    
	    
}
