package com.gxt.identity.service;

import com.gxt.common.gxtIdentity.AuthResponse;
import com.gxt.common.gxtIdentity.GoogleOAuthRequest;
import com.gxt.common.gxtIdentity.UserStatus;
import com.gxt.common.gxtIdentity.UserSummaryResponse;
import com.gxt.identity.config.JwtProperties;
import com.gxt.identity.dal.UserDal;
import com.gxt.identity.entity.User;
import com.gxt.identity.exception.AccountNotActiveException;
import com.gxt.identity.exception.OAuthAccountConflictException;
import com.gxt.identity.exception.OAuthEmailRequiredException;
import com.gxt.identity.port.GoogleTokenVerifierPort;
import com.gxt.identity.port.model.GoogleUserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class GoogleOAuthService {

    private final GoogleTokenVerifierPort googleTokenVerifierPort;
    private final UserDal userDal;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final JwtProperties jwtProperties;

    @Transactional
    public AuthResponse authenticateWithGoogle(GoogleOAuthRequest req) {
        GoogleUserInfo googleUser = googleTokenVerifierPort.verifyIdToken(req.idToken());
        return userDal.findByGoogleId(googleUser.googleId())
                .map(this::loginExistingUser)
                .orElseGet(() -> loginOrRegisterByEmail(googleUser, req.password()));
    }

    private AuthResponse loginOrRegisterByEmail(GoogleUserInfo googleUser, String password) {
        if (!StringUtils.hasText(googleUser.email())) {
            throw new OAuthEmailRequiredException();
        }
        return userDal.findByEmail(googleUser.email())
                .map(user -> linkGoogleAndLogin(user, googleUser))
                .orElseGet(() -> registerNewGoogleUser(googleUser, password));
    }

    private AuthResponse linkGoogleAndLogin(User user, GoogleUserInfo googleUser) {
        if (user.getGoogleId() != null && !user.getGoogleId().equals(googleUser.googleId())) {
            throw new OAuthAccountConflictException();
        }
        if (user.getGoogleId() == null) {
            user.setGoogleId(googleUser.googleId());
        }
        if (googleUser.emailVerified()) {
            user.setIsVerified(true);
        }
        userDal.save(user);
        return loginExistingUser(user);
    }

    private AuthResponse registerNewGoogleUser(GoogleUserInfo googleUser, String password) {
        String displayName = StringUtils.hasText(googleUser.displayName())
                ? googleUser.displayName()
                : googleUser.email().substring(0, googleUser.email().indexOf('@'));

        User.UserBuilder builder = User.builder()
                .googleId(googleUser.googleId())
                .email(googleUser.email())
                .displayName(displayName)
                .isVerified(googleUser.emailVerified());
        if (StringUtils.hasText(password) && password.length() >= 6) {
            builder.passwordHash(passwordEncoder.encode(password));
        }
        return loginExistingUser(userDal.save(builder.build()));
    }

    private AuthResponse loginExistingUser(User user) {
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new AccountNotActiveException();
        }
        return new AuthResponse(
                tokenService.generateToken(user),
                "Bearer",
                jwtProperties.getExpirySeconds(),
                new UserSummaryResponse(
                        user.getId(), user.getEmail(), user.getDisplayName(), user.getIsVerified()));
    }
}
