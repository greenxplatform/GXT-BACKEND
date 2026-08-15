package com.gxt.common.gxtIdentity;

import jakarta.validation.constraints.NotBlank;

public record EmailVerifyConfirmRequest(
        @NotBlank String code
) {}
