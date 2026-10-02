package com.hercufy.exceptions;

public class GoogleUnauthorizedException extends RuntimeException {
    public GoogleUnauthorizedException(String msg) {
        super(msg);
    }
}
