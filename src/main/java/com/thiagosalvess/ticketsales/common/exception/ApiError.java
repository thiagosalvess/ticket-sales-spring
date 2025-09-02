package com.thiagosalvess.ticketsales.common.exception;

import java.time.Instant;
import java.util.List;

public record ApiError(Instant timestamp,
                       int status,
                       String error,
                       String message,
                       String path,
                       List<FieldErrorItem> errors) {

    public static ApiError of(int status, String error, String message, String path, List<FieldErrorItem> errors) {
        return new ApiError(Instant.now(), status, error, message, path, errors);
    }

    public record FieldErrorItem(String field, String message) {}
}
