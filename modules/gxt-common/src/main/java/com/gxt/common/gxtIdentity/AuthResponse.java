package com.gxt.common.gxtIdentity;

public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        UserSummaryResponse user
) {}
