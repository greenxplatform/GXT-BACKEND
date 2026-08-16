package com.gxt.identity.exception;

public class OtpExpiredException extends RuntimeException {
    public OtpExpiredException() {
        super("Verification code expired");
    }
}
