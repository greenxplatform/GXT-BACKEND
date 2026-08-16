package com.gxt.identity.exception;

public class OtpInvalidException extends RuntimeException {
    public OtpInvalidException() {
        super("Invalid verification code");
    }
}
