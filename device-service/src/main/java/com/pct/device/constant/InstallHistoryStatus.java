package com.pct.device.constant;

/**
 * @author Abhishek on 21/04/20
 */
public enum InstallHistoryStatus {

    STARTED("Started"), FINISHED("Finished"), ERROR("Error"), PROBLEM("Problem"), ACTIVE_WITH_MINOR_ISSUE("Active with Minor Issue") ;

    private final String value;

    InstallHistoryStatus(String value) {
        this.value = value;
    }

    public static final InstallHistoryStatus getInstallHistoryStatus(String value) {
        if (value.equalsIgnoreCase(STARTED.getValue())) {
            return InstallHistoryStatus.STARTED;
        } else if (value.equalsIgnoreCase(FINISHED.getValue())) {
            return InstallHistoryStatus.FINISHED;
        } else if (value.equalsIgnoreCase(ERROR.getValue())) {
            return InstallHistoryStatus.ERROR;
        } else if (value.equalsIgnoreCase(PROBLEM.getValue())) {
            return InstallHistoryStatus.PROBLEM;
        }else if (value.equalsIgnoreCase(ACTIVE_WITH_MINOR_ISSUE.getValue())) {
            return InstallHistoryStatus.ACTIVE_WITH_MINOR_ISSUE;
        }
        
        return null;
    }

    public String getValue() {
        return this.value;
    }
}
