package com.gxt.identity.exception;

public class AlreadyVerifiedException extends RuntimeException {
    public AlreadyVerifiedException() {
        super("Email is already verified");
    }
}
