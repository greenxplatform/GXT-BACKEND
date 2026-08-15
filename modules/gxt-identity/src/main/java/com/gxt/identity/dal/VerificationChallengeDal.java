package com.gxt.identity.dal;

import com.gxt.common.gxtIdentity.VerificationType;
import com.gxt.identity.entity.VerificationChallenge;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VerificationChallengeDal {
    VerificationChallenge save(VerificationChallenge challenge);

    Optional<VerificationChallenge> findLatestPendingByUserId(UUID userId, VerificationType type);

    List<VerificationChallenge> findPendingByUserId(UUID userId, VerificationType type);

    void expirePendingChallenges(UUID userId, VerificationType type);
}
