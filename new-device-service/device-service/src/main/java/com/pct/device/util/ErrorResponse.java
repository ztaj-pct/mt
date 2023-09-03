package com.pct.device.util;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

public class ErrorResponse implements Serializable {

    @Getter
    @Setter
    private String message;

    @Getter
    @Setter
    private String description;

    public ErrorResponse(String message, String description) {
        super();
        this.message = message;
        this.description = description;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}