package com.gxt.identity.service;

import com.gxt.common.gxtIdentity.VerificationStatus;
import com.gxt.common.gxtIdentity.VerificationStatusResponse;
import com.gxt.common.gxtIdentity.VerificationType;
import com.gxt.identity.dal.UserDal;
import com.gxt.identity.dal.VerificationChallengeDal;
import com.gxt.identity.entity.User;
import com.gxt.identity.entity.VerificationChallenge;
import com.gxt.identity.exception.AlreadyVerifiedException;
import com.gxt.identity.exception.OtpExpiredException;
import com.gxt.identity.exception.OtpInvalidException;
import com.gxt.identity.port.OtpDeliveryPort;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VerificationService {

    private static final int OTP_TTL_SECONDS = 600;

    private final UserDal userDal;
    private final VerificationChallengeDal verificationChallengeDal;
    private final OtpDeliveryPort otpDeliveryPort;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void requestEmailVerification(UUID userId) {
        User user = userDal.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found"));
        if (Boolean.TRUE.equals(user.getIsVerified())) {
            throw new AlreadyVerifiedException();
        }
        if (user.getEmail() == null) {
            throw new IllegalStateException("User has no email");
        }

        verificationChallengeDal.expirePendingChallenges(userId, VerificationType.EMAIL);

        String otp = String.format("%06d", (int) (Math.random() * 1_000_000));
        String email = user.getEmail().toLowerCase();

        verificationChallengeDal.save(
                VerificationChallenge.builder()
                        .userId(userId)
                        .verificationType(VerificationType.EMAIL)
                        .target(email)
                        .otpHash(passwordEncoder.encode(otp))
                        .expiresAt(Instant.now().plusSeconds(OTP_TTL_SECONDS))
                        .build());
        otpDeliveryPort.sendEmailOtp(email, otp);
    }

    @Transactional
    public VerificationStatusResponse confirmEmailVerification(UUID userId, String otp) {
        User user = userDal.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found"));
        if (Boolean.TRUE.equals(user.getIsVerified())) {
            throw new AlreadyVerifiedException();
        }

        VerificationChallenge challenge = verificationChallengeDal
                .findLatestPendingByUserId(userId, VerificationType.EMAIL)
                .orElseThrow(OtpInvalidException::new);

        if (challenge.getExpiresAt().isBefore(Instant.now())) {
            throw new OtpExpiredException();
        }
        if (!passwordEncoder.matches(otp, challenge.getOtpHash())) {
            throw new OtpInvalidException();
        }

        user.setIsVerified(true);
        userDal.save(user);

        challenge.setStatus(VerificationStatus.VERIFIED);
        challenge.setVerifiedAt(Instant.now());
        verificationChallengeDal.save(challenge);

        return toStatusResponse(user);
    }

    public VerificationStatusResponse getStatus(UUID userId) {
        User user = userDal.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found"));
        return toStatusResponse(user);
    }

    private VerificationStatusResponse toStatusResponse(User user) {
        return new VerificationStatusResponse(
                Boolean.TRUE.equals(user.getIsVerified()),
                user.getEmail());
    }
}
