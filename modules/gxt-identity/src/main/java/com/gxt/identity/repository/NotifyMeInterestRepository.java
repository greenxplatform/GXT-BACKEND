package com.gxt.identity.repository;

import com.gxt.identity.entity.NotifyMeInterest;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotifyMeInterestRepository extends JpaRepository<NotifyMeInterest, UUID> {
    Optional<NotifyMeInterest> findByEmail(String email);
}
