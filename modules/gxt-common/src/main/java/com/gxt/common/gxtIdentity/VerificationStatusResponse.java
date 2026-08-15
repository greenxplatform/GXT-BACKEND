package com.gxt.common.gxtIdentity;

public record VerificationStatusResponse(
        boolean verified,
        String email
) {}
