package com.gxt.common.gxtNotifyMe;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NotifyMeRequest(
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Email String email
) {}
