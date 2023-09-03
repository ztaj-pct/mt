package com.pct.common.constant;

/**
 * @author Abhishek on 22/04/20
 */
public enum OrganisationType {

    INSTALLER("Installer"), MANUFACTURER("Manufacturer"), END_CUSTOMER("END_CUSTOMER"), PCT("Pct"), RESELLER("Reseller");

	private final String value;

	OrganisationType(String value) {
		this.value = value;
	}

	public final static OrganisationType getOrganisationType(String type) {

		if (type.equalsIgnoreCase(INSTALLER.getValue())) {
			return INSTALLER;
		} else if (type.equalsIgnoreCase(MANUFACTURER.getValue())) {
			return MANUFACTURER;
		} else if (type.equalsIgnoreCase(END_CUSTOMER.getValue())) {
			return END_CUSTOMER;
		} else if (type.equalsIgnoreCase(PCT.getValue())) {
			return PCT;
		} else if (type.equalsIgnoreCase(RESELLER.getValue())) {
			return RESELLER;
		}
		return null;
	}

	public String getValue() {
		return this.value;
	}
}
