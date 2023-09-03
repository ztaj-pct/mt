package com.pct.organisation.payload;

import java.util.List;

public class CustomerLocationPayload {

	private String account_number;
    private String customer_name;
    private List<LocationPayload> locations;
    
	public String getAccount_number() {
		return account_number;
	}
	public void setAccount_number(String account_number) {
		this.account_number = account_number;
	}
	public String getCustomer_name() {
		return customer_name;
	}
	public void setCustomer_name(String customer_name) {
		this.customer_name = customer_name;
	}
	public List<LocationPayload> getLocations() {
		return locations;
	}
	public void setLocations(List<LocationPayload> locations) {
		this.locations = locations;
	}
    
    
}
