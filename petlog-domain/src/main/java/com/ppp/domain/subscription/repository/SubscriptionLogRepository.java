package com.ppp.domain.subscription.repository;

import com.ppp.domain.subscription.SubscriptionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubscriptionLogRepository extends JpaRepository<SubscriptionLog, Long> {
    boolean existsByPetIdAndUserId(Long id, String userId);
}
