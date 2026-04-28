package com.eaglebank.api.common.error;

import org.springframework.http.HttpStatus;

public class UnprocessableContentException extends ApiException {

    public UnprocessableContentException(String message) {
        super(message, HttpStatus.UNPROCESSABLE_CONTENT);
    }
}
