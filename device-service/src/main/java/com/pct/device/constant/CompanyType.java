package com.pct.device.constant;


public enum CompanyType {

    INSTALLER("Installer"), MANUFACTURER("Manufacturer"), CUSTOMER("Customer");

    private final String value;

    CompanyType(String value) {
        this.value = value;
    }

    public final static CompanyType getCompanyType(String type) {

        if (type.equalsIgnoreCase(INSTALLER.getValue())) {
            return INSTALLER;
        } else if (type.equalsIgnoreCase(MANUFACTURER.getValue())) {
            return MANUFACTURER;
        } else if (type.equalsIgnoreCase(CUSTOMER.getValue())) {
            return CUSTOMER;
        }
        return null;
    }

    public String getValue() {
        return this.value;
    }
}
