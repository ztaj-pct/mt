package com.pct.organisation.payload;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.pct.common.model.Organisation;

import java.util.ArrayList;
import java.util.List;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class OrganisationAccess {

	    private OrganisationPayload customer;
	    private List<Organisation> OrganisationViewList = new ArrayList();
	    private boolean status;
	    private String type;
	    private Boolean isAssetListRequired;
	    
	    
		public OrganisationPayload getCustomer() {
			return customer;
		}
		public void setCustomer(OrganisationPayload customer) {
			this.customer = customer;
		}
		public List<Organisation> getOrganisationViewList() {
			return OrganisationViewList;
		}
		public void setOrganisationViewList(List<Organisation> organisationViewList) {
			OrganisationViewList = organisationViewList;
		}
		public boolean getStatus() {
			return status;
		}
		public void setStatus(boolean status) {
			this.status = status;
		}
		public String getType() {
			return type;
		}
		public void setType(String type) {
			this.type = type;
		}
		public Boolean getIsAssetListRequired() {
			return isAssetListRequired;
		}
		public void setIsAssetListRequired(Boolean isAssetListRequired) {
			this.isAssetListRequired = isAssetListRequired;
		}
	    
	    
	    
}
