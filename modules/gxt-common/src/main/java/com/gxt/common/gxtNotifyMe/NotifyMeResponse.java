package com.gxt.common.gxtNotifyMe;

import java.util.UUID;

public record NotifyMeResponse(
        UUID id,
        String name,
        String email,
        NotifyMeStatus status,
        boolean alreadyRegistered,
        String message
) {}
