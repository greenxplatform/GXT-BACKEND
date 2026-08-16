package com.gxt.identity.service;

import com.gxt.common.gxtIdentity.AuthResponse;
import com.gxt.common.gxtIdentity.LoginEmailRequest;
import com.gxt.common.gxtIdentity.RegisterEmailRequest;
import com.gxt.common.gxtIdentity.UserStatus;
import com.gxt.common.gxtIdentity.UserSummaryResponse;
import com.gxt.identity.config.JwtProperties;
import com.gxt.identity.dal.RevokedTokenDal;
import com.gxt.identity.dal.UserDal;
import com.gxt.identity.entity.RevokedToken;
import com.gxt.identity.entity.User;
import com.gxt.identity.exception.AccountNotActiveException;
import com.gxt.identity.exception.EmailAlreadyExistsException;
import com.gxt.identity.exception.InvalidCredentialsException;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserDal userDal;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final JwtProperties jwtProperties;
    private final RevokedTokenDal revokedTokenDal;
    private final VerificationService verificationService;

    @Transactional
    public AuthResponse registerWithEmail(RegisterEmailRequest req) {
        String email = req.email().trim().toLowerCase();
        if (userDal.existsByEmail(email)) {
            throw new EmailAlreadyExistsException(email);
        }
        User user = User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(req.password()))
                .displayName(req.name().trim())
                .isVerified(false)
                .build();
        user = userDal.save(user);
        verificationService.requestEmailVerification(user.getId());
        return buildAuthResponse(user);
    }

    public AuthResponse loginWithEmail(LoginEmailRequest req) {
        String email = req.email().trim().toLowerCase();
        User user = userDal.findByEmail(email).orElseThrow(InvalidCredentialsException::new);
        if (user.getPasswordHash() == null
                || !passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new AccountNotActiveException();
        }
        return buildAuthResponse(user);
    }

    public void logoutAndTokenBlackListing(String rawJwt) {
        if (rawJwt == null || rawJwt.isBlank()) {
            throw new InvalidCredentialsException();
        }
        String token = rawJwt.trim();
        String hash = tokenService.hashToken(token);
        if (revokedTokenDal.existsByTokenHash(hash)) {
            return;
        }
        Instant expiresAt = tokenService.getTokenExpiration(token);
        revokedTokenDal.save(
                RevokedToken.builder()
                        .tokenHash(hash)
                        .expiresAt(expiresAt)
                        .build());
    }

    AuthResponse buildAuthResponse(User user) {
        return new AuthResponse(
                tokenService.generateToken(user),
                "Bearer",
                jwtProperties.getExpirySeconds(),
                new UserSummaryResponse(
                        user.getId(), user.getEmail(), user.getDisplayName(), user.getIsVerified()));
    }
}
