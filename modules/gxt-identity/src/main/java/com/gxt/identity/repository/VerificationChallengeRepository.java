package com.gxt.identity.repository;

import com.gxt.common.gxtIdentity.VerificationStatus;
import com.gxt.common.gxtIdentity.VerificationType;
import com.gxt.identity.entity.VerificationChallenge;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VerificationChallengeRepository extends JpaRepository<VerificationChallenge, UUID> {

    Optional<VerificationChallenge> findTopByUserIdAndVerificationTypeAndStatusOrderByCreatedAtDesc(
            UUID userId, VerificationType type, VerificationStatus status);

    List<VerificationChallenge> findByUserIdAndVerificationTypeAndStatus(
            UUID userId, VerificationType type, VerificationStatus status);
}
