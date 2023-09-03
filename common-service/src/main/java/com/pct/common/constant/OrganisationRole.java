package com.pct.common.constant;

public enum OrganisationRole {

	INSTALLER("Installer"), END_CUSTOMER("End_Customer"), RESELLER("Reseller"), MANUFACTURER("Manufacturer"), MAINTENANCE_MODE("Maintenance_Mode"), CUSTOMER("Customer"), PCT("Pct");
	
	private final String value;

	private OrganisationRole(String value) {
		this.value = value;
	}
	
	
	public final static OrganisationRole getOrganisationRole(String type) {

		if (type.equalsIgnoreCase(INSTALLER.getValue())) {
			return INSTALLER;
		} else if (type.equalsIgnoreCase(END_CUSTOMER.getValue()) || type.equalsIgnoreCase("CUSTOMER") || type.equalsIgnoreCase("ENDCUSTOMER")) {
			return END_CUSTOMER;
		} else if (type.equalsIgnoreCase(RESELLER.getValue())) {
			return RESELLER;
		} else if (type.equalsIgnoreCase(PCT.getValue())) {
			return PCT;
		} else if(type.equalsIgnoreCase(MANUFACTURER.getValue())) {
			return MANUFACTURER;
		} else if(type.equalsIgnoreCase(MAINTENANCE_MODE.getValue())) {
			return MAINTENANCE_MODE;
		}
		
		return null;
	}

	public String getValue() {
		return this.value;
	}
	
	
}
