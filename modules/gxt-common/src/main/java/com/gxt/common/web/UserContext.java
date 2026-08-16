package com.gxt.common.web;

import com.gxt.common.config.GxtProperties;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component
public class UserContext {
    private static final String USER_HEADER = "X-User-Id";

    private final GxtProperties properties;

    public UserContext(GxtProperties properties) {
        this.properties = properties;
    }

    public String resolveUserId(HttpServletRequest request) {
        String header = request.getHeader(USER_HEADER);
        if (header != null && !header.isBlank()) {
            return header.trim();
        }
        return properties.getStubUserId();
    }
}
