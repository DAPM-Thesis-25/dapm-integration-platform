package com.dapm.security_service.exceptions;

public class DuplicateFieldException extends RuntimeException {
    private final String field;

    public DuplicateFieldException(String field, String message) {
        super(message);
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
