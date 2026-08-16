package com.gxt.identity.controller;

import com.gxt.common.gxtIdentity.AuthResponse;
import com.gxt.common.gxtIdentity.GoogleOAuthRequest;
import com.gxt.common.gxtIdentity.LoginEmailRequest;
import com.gxt.common.gxtIdentity.RegisterEmailRequest;
import com.gxt.identity.exception.InvalidCredentialsException;
import com.gxt.identity.service.AuthService;
import com.gxt.identity.service.GoogleOAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final GoogleOAuthService googleOAuthService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterEmailRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registerWithEmail(req));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginEmailRequest req) {
        return ResponseEntity.ok(authService.loginWithEmail(req));
    }

    @PostMapping("/oauth/google")
    public ResponseEntity<AuthResponse> googleOAuth(@Valid @RequestBody GoogleOAuthRequest req) {
        return ResponseEntity.ok(googleOAuthService.authenticateWithGoogle(req));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION) String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new InvalidCredentialsException();
        }
        authService.logoutAndTokenBlackListing(authorization.substring(7));
        return ResponseEntity.noContent().build();
    }
}
