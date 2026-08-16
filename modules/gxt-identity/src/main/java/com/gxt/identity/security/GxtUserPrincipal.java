package com.gxt.identity.security;

import java.util.UUID;

public record GxtUserPrincipal(
        UUID userId,
        String email,
        String role,
        boolean verified) {}
