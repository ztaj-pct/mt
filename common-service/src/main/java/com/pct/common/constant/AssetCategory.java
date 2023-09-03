package com.pct.common.constant;

/**
 * @author Abhishek on 16/04/20
 */
public enum AssetCategory {

    TRAILER("Trailer"), CHASSIS("Chassis"), CONTAINER("Container"), VEHICLE("Vehicle"), TRACTOR("Tractor");

    private final String value;

    AssetCategory(String value) {
        this.value = value;
    }
    
    public final static AssetCategory getAssetCategory(String type) {

		if (type.equalsIgnoreCase(TRAILER.getValue())) {
			return TRAILER;
		} else if (type.equalsIgnoreCase(CHASSIS.getValue())) {
			return CHASSIS;
		} else if (type.equalsIgnoreCase(CONTAINER.getValue())) {
			return CONTAINER;
		} else if (type.equalsIgnoreCase(VEHICLE.getValue())) {
			return VEHICLE;
		} else if (type.equalsIgnoreCase(TRACTOR.getValue())) {
			return TRACTOR;
		}
		return null;
	}

//    public static AssetCategory findByValue(int value) {
//        return AssetCategory.values()[value];
//    }
//
//    public static AssetCategory findByValue(String value) {
//        for (AssetCategory enumObject : AssetCategory.values()) {
//            if (enumObject.toString().equalsIgnoreCase(value)) {
//                return enumObject;
//            }
//        }
//        return null;
//    }

    public String getValue() {
        return this.value;
    }
}
