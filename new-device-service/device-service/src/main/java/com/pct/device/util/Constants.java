package com.pct.device.util;

import java.time.format.DateTimeFormatter;

/**
 *
 */

public class Constants {

    public static final String SEARCH_PATTERN = "(\\w+?)(:|<|>|=)(\\w+?),";
    public static final String SORT_PATTERN = "(\\w+?)(:)(\\w+?),";
    public static final String COMMA = ",";

    //this is used for Security
    public static final long ACCESS_TOKEN_VALIDITY_SECONDS = (60 * 24) * 60 * 60;
    public static final String SIGNING_KEY = "devglan123r";
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String HEADER_STRING = "Authorization";
    public static final String AUTHORITIES_KEY = "scopes";


    public static final DateTimeFormatter DATEFORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    //2019-03-02T16:28:48
    public static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    //2019-05-07T15:00:00Z
    public static final DateTimeFormatter TIMESTAMP_WITH_ZONE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssz");


    public static final String NORMAL = "Normal"; // Provisined
    public static final String REGISTERED_ONLY = "Registered Only"; // Not provisioned
    public static final String REPORTING = "Reporting"; // Active
    public static final String DELETED = "Deleted"; //
    public static final String ERROR = "Other error";
    public static final String NOT_REGISTERED = "Error unregistered";

    public static final String TERMINATED = "Terminated";
    public static final String PROVISIONED = "Provisioned";
    public static final String PENDING = "Pending";
    public static final String PARTIAL = "Partial";
    public static final String INSTALLED = "Installed";

    // Asset Upload List Asset Properties
    public static final String ASSET_FIELD_APPROVED_PRODUCT = "APPROVED_PRODUCT";
    public static final String ASSET_FIELD_ASSET_TYPE_1 = "ASSET_TYPE_1";

    // Company Type
    public static final String MANUFACTURER = "Manufacturer";
    public static final String INSTALLER = "3rd Party Installer";
    public static final String ASSET_STATUS_APPROVED = "APPROVED";

    public static final String SAVED_MAP_KEY = "saved";
    public static final String REJECTED_MAP_KEY = "rejected";
}
