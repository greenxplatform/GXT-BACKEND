package com.gxt.common.gxtIdentity;

import jakarta.validation.constraints.NotBlank;

public record GoogleOAuthRequest(
        @NotBlank String idToken,
        String password
) {}
