package com.pct.device.version.util;

import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

/**
 *
 */

public class Constants {

	public static final String ON_PATH = "ON_PATH";
	public static final String OFF_PATH = "OFF_PATH";
	public static final String PROBLEM = "PROBLEM";
	public static final String NOPPROBLEM = "NO_PROBLEM";
	public static final String ONHOLD = "ON_HOLD";
	public static final String OFFHOLD = "OFF_HOLD";
	public static final String LAST_SUCCESS_STARTED_STEP = "NOTSTARTED";
    public static final String USAGE_ENGINEERING = "engineering";
    public static final String USAGE_RMA = "rma";
    public static final String USAGE_EOL = "EOL";
    public static final String CUSTOMER_ENGINEERING = "engineering";
    public static final String ANY = "Any";
    public static final String MISSING = "Missing";
    public static final String ZERO = "0";
    public static final String EMPTY_STRING = "";
    public static final int UPGRADE_ELIGIBLE_TIME_BUFFER = 48;
    
    public static final String COMPLETION_PATTERN = "% Complete";
    public static final long MAX_FAILED_ATTEMPT_FOR_UPDATE = 3;
    public static final String NOT_REPORTED_RECENTLY = "Gateway has not reported recently";
    public static final String NOT_ELIGIBLE_FOR_BASELINE = "Configuration does not match baseline";
    public static final String GATEWAY_NOT_INSTALLED = "Gateway is not installed";
    public static final String CONFLICT_ELIGIBLE = "Gateway also part of ";
    public static final String CONFLICT_NOT_ELIGIBLE = "Currently in-progress in ";
    public static final String GATEWAY_PROBLEM_STATUS = " could not be executed after multiple attempts ";
    
    public static final String SEARCH_PATTERN = "(\\w+?)(:|<|>|=)(\\w+?),";
    public static final String SORT_PATTERN = "(\\w+?)(:)(\\w+?),";
    public static final String COMMA = ",";
    public static final long IMEI_LENGTH= 15;
    public static final String ASCENDING_ORDER = "asc";
    
    public static final String ERROR_TITLE_INVALID_IMEI = "Invalid IMEI";
    public static final String ERROR_TITLE_NOT_FOUND_ON_MS = "Not found on MS";
    public static final String ERROR_TITLE_IN_MULTIPLE_CAMPAIGNS = "In multiple Campaigns";
    
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


    public static final String ALREADY_UPDATED = "Already Updated Via AT Command"; // Provisined
    public static final String ALREADY_EXECUTED_IN_24H= "Already Executed The AT Command In Last 24 Hours";
    public static final String NO_RESPONSE_OF_ATCMD_SINCE_LAST_24H= "No Responce Of Last AT Command Since Last 24 Hours";
    public static final String PREVIOUS_STEP_SUCCESSFULLY_DONE = "Previous Step Has Completed Successfully ";
    public static final String UPDATE_NOT_ALLOWED = "Update Not Allowed";
	public static final List<String> INTERSECTED_LIST= Arrays.asList("UUID","PACKAGEID","ISDELETED","CREATEDBY","UPDATEDBY","UPDATEDAT");


    public static final String CSVFILE = "packages.csv";
    public static final String CSVFILE_FILTERED = "packagesFiltered.csv";
    public static final String FEIGN_CLIENT_NAME ="CampaignFeignClient";
    public static final String FEIGN_CLIENT_POST_GET_ALL="/package/getAll";
    public static final String BASE_PACKAGE="com.pct.device.version";
    public static final String DEVICE_VERSION_NAME="Device Version Service";
    
    public static final float LOW_BATTERY_POWER = 3.2f;
    
    public static final String ACTIVE_GATEWAY_STATUS = "Active";
}