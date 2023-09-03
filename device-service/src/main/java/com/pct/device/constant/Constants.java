package com.pct.device.constant;

import java.util.Arrays;
import java.util.List;

public class Constants {

    public static final String SEARCH_PATTERN = "(\\w+?)(:|<|>|=)(\\w+?),";
    public static final String SORT_PATTERN = "(\\w+?)(:)(\\w+?),";
    public static final String COMMA = ",";
    
 // User roles 
    public static final String ROLE_SUPER_ADMIN = "Phillips Connect Admin";
    public static final String ROLE_CUSTOMER_ADMIN = "Org Admin";
    public static final String ROLE_ORGANIZATION_USER = "Org User";
    public static final String ROLE_INSTALLER = "Installer";
    
    public static final String SAVED_MAP_KEY = "saved";
    public static final String REJECTED_MAP_KEY = "rejected";

    public static final String GATEWAYS_CSVFILE_FILTERED = "DeviceFiltered.csv";
    public static final List<String> INTERSECTED_LIST= Arrays.asList("Uuid","ProductName","AccountNumber","GatewayType");
}
