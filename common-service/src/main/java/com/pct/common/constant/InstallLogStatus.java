package com.pct.common.constant;

public enum InstallLogStatus {

	STARTED("Started"), ERROR("Error"), PENDING("Pending"), FINISHED("Finished") , REJECTED("Rejected Installation"), PROBLEM("Problem");
    private final String value;

    InstallLogStatus(String value) {
        this.value = value;
    }

    public final static InstallLogStatus getInstallLogStatus(String value) {
        if (value.equalsIgnoreCase(InstallLogStatus.STARTED.getValue())) {
            return InstallLogStatus.STARTED;
        } else if (value.equalsIgnoreCase(InstallLogStatus.ERROR.getValue())) {
            return InstallLogStatus.ERROR;
        } else if (value.equalsIgnoreCase(InstallLogStatus.PENDING.getValue())) {
            return InstallLogStatus.PENDING;
        } else if (value.equalsIgnoreCase(InstallLogStatus.FINISHED.getValue())) {
            return InstallLogStatus.FINISHED;
        }else if (value.equalsIgnoreCase(InstallLogStatus.REJECTED.getValue())) {
            return InstallLogStatus.REJECTED;
        } else if (value.equalsIgnoreCase(InstallLogStatus.PROBLEM.getValue())) {
            return InstallLogStatus.PROBLEM;
        }
        return null;
    }

    public String getValue() {
        return this.value;
    }
}
