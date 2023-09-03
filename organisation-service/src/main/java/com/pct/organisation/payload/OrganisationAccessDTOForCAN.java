package com.pct.organisation.payload;

import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.pct.common.model.Organisation;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class OrganisationAccessDTOForCAN {

	private OrganisationPayloadForCAN customer;
	private List<Organisation> organisationViewList;
	private boolean status;
	private String type;
	
	public OrganisationPayloadForCAN getCustomer() {
		return customer;
	}
	public void setCustomer(OrganisationPayloadForCAN customer) {
		this.customer = customer;
	}
	public List<Organisation> getOrganisationViewList() {
		return organisationViewList;
	}
	public void setOrganisationViewList(List<Organisation> organisationViewList) {
		this.organisationViewList = organisationViewList;
	}
	public boolean isStatus() {
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
	
	
}
