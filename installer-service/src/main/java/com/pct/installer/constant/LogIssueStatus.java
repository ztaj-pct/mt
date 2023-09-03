package com.pct.installer.constant;

public enum LogIssueStatus {

	OPEN("Open"), RESOLVED("Resolved"), CANCELLED("Cancelled");
    private final String value;

    
    public final static LogIssueStatus getLogIssueStatus(String value) {
        if (value.equalsIgnoreCase(LogIssueStatus.OPEN.getValue())) {
            return LogIssueStatus.OPEN;
        } else if (value.equalsIgnoreCase(LogIssueStatus.RESOLVED.getValue())) {
            return LogIssueStatus.RESOLVED;
        } else if (value.equalsIgnoreCase(LogIssueStatus.CANCELLED.getValue())) {
            return LogIssueStatus.CANCELLED;
        } 
        return null;
    }
    
    LogIssueStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

}
