package com.ppp.domain.subscription.repository;

import com.ppp.domain.pet.Pet;
import com.ppp.domain.subscription.Subscription;
import com.ppp.domain.subscription.constant.Status;
import com.ppp.domain.user.User;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    Optional<Subscription> findBySubscriberAndPet(User user, Pet pet);

    @EntityGraph(attributePaths = {"subscriber"}, type = EntityGraph.EntityGraphType.FETCH)
    List<Subscription> findByPetId(Long petId);

    @Query("select s.pet.id from Subscription s where s.subscriber.id = ?1 and s.status != 'BLOCK'")
    Set<Long> findByValidSubscribedPetId(String userId);

    List<Subscription> findBySubscriberId(String userId);

    Optional<Subscription> findBySubscriberIdAndPetId(String userId, Long petId);
}
