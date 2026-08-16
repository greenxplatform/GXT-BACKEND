package com.gxt.identity.exception;

public class OAuthAccountConflictException extends RuntimeException {
    public OAuthAccountConflictException() {
        super("This Google account is linked to a different user");
    }
}
