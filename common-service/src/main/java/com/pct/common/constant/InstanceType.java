package com.pct.common.constant;

public enum InstanceType {
	
	DEFAULT("Default"), LEFT_INNER_FRONT("Left Inner Front"), LEFT_OUTER_FRONT("Left Outer Front"), LEFT_INNER_REAR("Left Inner Rear"),LEFT_OUTER_REAR("Left Outer Rear"),RIGHT_INNER_FRONT("Right Inner Front"),RIGHT_OUTER_FRONT("Right Outer Front"),RIGHT_OUTER_REAR("Right Outer Rear"),RIGHT_INNER_REAR("Right Inner Rear");

    private final String value;

    InstanceType(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return this.value;
    }

    public final static InstanceType getInstanceStatus(String value) {
        if (value.equalsIgnoreCase(InstanceType.DEFAULT.getValue())) {
            return InstanceType.DEFAULT;
        } else if (value.equalsIgnoreCase(InstanceType.LEFT_INNER_FRONT.getValue())) {
            return InstanceType.LEFT_INNER_FRONT;
        } else if (value.equalsIgnoreCase(InstanceType.LEFT_OUTER_FRONT.getValue())) {
            return InstanceType.LEFT_OUTER_FRONT;
        } else if (value.equalsIgnoreCase(InstanceType.LEFT_INNER_REAR.getValue())) {
            return InstanceType.LEFT_INNER_REAR;
        }else if (value.equalsIgnoreCase(InstanceType.LEFT_OUTER_REAR.getValue())) {
            return InstanceType.LEFT_OUTER_REAR;
        }else if (value.equalsIgnoreCase(InstanceType.RIGHT_INNER_FRONT.getValue())) {
            return InstanceType.RIGHT_INNER_FRONT;
        }else if (value.equalsIgnoreCase(InstanceType.RIGHT_OUTER_FRONT.getValue())) {
            return InstanceType.RIGHT_OUTER_FRONT;
        }else if (value.equalsIgnoreCase(InstanceType.RIGHT_INNER_REAR.getValue())) {
            return InstanceType.RIGHT_INNER_REAR;
        }else if (value.equalsIgnoreCase(InstanceType.RIGHT_OUTER_REAR.getValue())) {
            return InstanceType.RIGHT_OUTER_REAR;
        }
        return null;
    }
}
