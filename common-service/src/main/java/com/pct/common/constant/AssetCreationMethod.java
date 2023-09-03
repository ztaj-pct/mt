package com.pct.common.constant;

/**
 * @author Abhishek on 03/03/21
 */
public enum AssetCreationMethod {

    MANUAL("Manual"), UPLOAD("Upload");

    private final String value;

    AssetCreationMethod(String value) {
        this.value = value;
    }

    public static AssetCreationMethod findByValue(int value) {
        return AssetCreationMethod.values()[value];
    }

    public static AssetCreationMethod findByValue(String value) {
        for (AssetCreationMethod enumObject : AssetCreationMethod.values()) {
            if (enumObject.toString().equalsIgnoreCase(value)) {
                return enumObject;
            }
        }
        return null;
    }

    public String getValue() {
        return this.value;
    }
}
