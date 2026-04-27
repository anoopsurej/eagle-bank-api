package com.eaglebank.api.common.error;

public class NotFoundException extends ApiException {

    public NotFoundException(String message) {
        super(message, org.springframework.http.HttpStatus.NOT_FOUND);
    }
}