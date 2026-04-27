package com.eaglebank.api.common.error;

public class ConflictException extends ApiException {

    public ConflictException(String message) {
        super(message, org.springframework.http.HttpStatus.CONFLICT);
    }
}
