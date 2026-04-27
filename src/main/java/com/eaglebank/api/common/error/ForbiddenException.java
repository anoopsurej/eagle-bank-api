package com.eaglebank.api.common.error;

public class ForbiddenException extends ApiException {

    public ForbiddenException(String message) {
        super(message, org.springframework.http.HttpStatus.FORBIDDEN);
    }
}
