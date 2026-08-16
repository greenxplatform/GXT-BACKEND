package com.gxt.common.gxtIdentity;

import java.util.UUID;

public record UserSummaryResponse(
        UUID id,
        String email,
        String name,
        boolean verified
) {}
