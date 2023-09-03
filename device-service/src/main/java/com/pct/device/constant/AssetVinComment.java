package com.pct.device.constant;


public enum AssetVinComment {

    INVALID("VIN is invalid"), DECODE_ERROR("VIN decoded with errors"),
    NOT_VALIDATED("VIN not validated");

    private final String value;

    AssetVinComment(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    public static final AssetVinComment getAssetVinComment(String value) {
        if (value.equalsIgnoreCase(INVALID.getValue())) {
            return AssetVinComment.INVALID;
        } else if (value.equalsIgnoreCase(DECODE_ERROR.getValue())) {
            return AssetVinComment.DECODE_ERROR;
        } else if (value.equalsIgnoreCase(NOT_VALIDATED.getValue())) {
            return AssetVinComment.NOT_VALIDATED;
        }
        return null;
    }
}
