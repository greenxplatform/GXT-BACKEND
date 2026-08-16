package com.gxt.identity.port.model;

public record GoogleUserInfo(
        String googleId,
        String email,
        String displayName,
        boolean emailVerified
) {}
