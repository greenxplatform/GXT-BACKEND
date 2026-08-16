package com.gxt.identity.dal.impl;

import com.gxt.common.gxtIdentity.VerificationStatus;
import com.gxt.common.gxtIdentity.VerificationType;
import com.gxt.identity.dal.VerificationChallengeDal;
import com.gxt.identity.entity.VerificationChallenge;
import com.gxt.identity.repository.VerificationChallengeRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VerificationChallengeDalImpl implements VerificationChallengeDal {

    private final VerificationChallengeRepository verificationChallengeRepository;

    @Override
    public VerificationChallenge save(VerificationChallenge challenge) {
        return verificationChallengeRepository.save(challenge);
    }

    @Override
    public Optional<VerificationChallenge> findLatestPendingByUserId(UUID userId, VerificationType type) {
        return verificationChallengeRepository.findTopByUserIdAndVerificationTypeAndStatusOrderByCreatedAtDesc(
                userId, type, VerificationStatus.PENDING);
    }

    @Override
    public List<VerificationChallenge> findPendingByUserId(UUID userId, VerificationType type) {
        return verificationChallengeRepository.findByUserIdAndVerificationTypeAndStatus(
                userId, type, VerificationStatus.PENDING);
    }

    @Override
    public void expirePendingChallenges(UUID userId, VerificationType type) {
        List<VerificationChallenge> pending = findPendingByUserId(userId, type);
        if (pending.isEmpty()) {
            return;
        }
        pending.forEach(challenge -> challenge.setStatus(VerificationStatus.EXPIRED));
        verificationChallengeRepository.saveAll(pending);
    }
}
