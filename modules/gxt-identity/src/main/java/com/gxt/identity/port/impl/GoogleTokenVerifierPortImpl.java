package com.gxt.identity.port.impl;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.gxt.identity.config.GoogleOAuthProperties;
import com.gxt.identity.exception.InvalidGoogleTokenException;
import com.gxt.identity.port.GoogleTokenVerifierPort;
import com.gxt.identity.port.model.GoogleUserInfo;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class GoogleTokenVerifierPortImpl implements GoogleTokenVerifierPort {

    private final GoogleOAuthProperties googleOAuthProperties;

    @Override
    public GoogleUserInfo verifyIdToken(String idToken) {
        if (!StringUtils.hasText(googleOAuthProperties.getClientId())) {
            throw new IllegalStateException("gxt.oauth.google.client-id is not configured");
        }
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(), GsonFactory.getDefaultInstance())
                    .setAudience(Collections.singletonList(googleOAuthProperties.getClientId()))
                    .build();

            GoogleIdToken token = verifier.verify(idToken);
            if (token == null) {
                throw new InvalidGoogleTokenException();
            }

            GoogleIdToken.Payload payload = token.getPayload();
            String email = payload.getEmail();
            String name = payload.get("name") != null ? payload.get("name").toString() : null;
            if (!StringUtils.hasText(name) && StringUtils.hasText(email)) {
                name = email.substring(0, email.indexOf('@'));
            }

            return new GoogleUserInfo(
                    payload.getSubject(),
                    email != null ? email.toLowerCase() : null,
                    name,
                    Boolean.TRUE.equals(payload.getEmailVerified()));
        } catch (InvalidGoogleTokenException e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidGoogleTokenException();
        }
    }
}
