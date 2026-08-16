package com.gxt.strategybuilder.dto;

import java.time.Instant;

public record ChatMessageResponse(String id, String role, String content, Instant createdAt) {}
