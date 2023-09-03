package com.pct.device.constant;

public enum AssetCategory {

    TRAILER("Trailer"), CHASSIS("Chassis"), CONTAINER("Container"), VEHICLE("Vehicle"), TRACTOR("Tractor");

    private final String value;

    AssetCategory(String value) {
        this.value = value;
    }

    public static AssetCategory findByValue(int value) {
        return AssetCategory.values()[value];
    }

    public static AssetCategory findByValue(String value) {
        for (AssetCategory enumObject : AssetCategory.values()) {
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
