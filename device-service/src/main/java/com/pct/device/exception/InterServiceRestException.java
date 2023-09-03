package com.pct.device.exception;

import org.springframework.http.HttpStatus;

/**
 * @author Abhishek on 29/04/20
 */

public class InterServiceRestException extends RuntimeException {

    private final HttpStatus httpStatus;

    public InterServiceRestException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public HttpStatus getHttpStatus() {
        return this.httpStatus;
    }
}
