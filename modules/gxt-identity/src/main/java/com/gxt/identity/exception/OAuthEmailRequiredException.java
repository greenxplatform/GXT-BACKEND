package com.gxt.identity.exception;

public class OAuthEmailRequiredException extends RuntimeException {
    public OAuthEmailRequiredException() {
        super("Google account did not provide an email");
    }
}
