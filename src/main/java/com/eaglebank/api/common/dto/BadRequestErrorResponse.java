package com.eaglebank.api.common.dto;

import java.util.List;

public record BadRequestErrorResponse(String message, List<FieldError> details) {
}
