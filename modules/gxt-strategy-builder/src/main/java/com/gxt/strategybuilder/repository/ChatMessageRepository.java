package com.gxt.strategybuilder.repository;

import com.gxt.strategybuilder.domain.ChatMessageEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMessageRepository extends JpaRepository<ChatMessageEntity, String> {
    List<ChatMessageEntity> findByStrategyIdOrderByCreatedAtAsc(String strategyId);
}
