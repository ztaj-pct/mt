package com.pct.common.azure.jwt;

public class JwtValidationException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	JwtValidationException(String message, Throwable ex){
        super(message, ex);
    }

    JwtValidationException(String message){
        super(message);
    }

}
