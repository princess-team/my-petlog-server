package com.ppp.domain.user.repository;

import com.ppp.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

    Optional<User> findByIdAndIsDeletedFalse(String id);

    @Query("select u from User u left join Guardian g on u.id = g.user.id where g.pet.id = ?1 and u.id = ?2 and u.isDeleted = false")
    Optional<User> findByGuardianUserByPetIdAndUserId(Long petId, String userId);

    @Query("select u from User u left join Guardian g on u.id = g.user.id where g.pet.id = ?1 and u.id in ?2 and u.isDeleted = false")
    List<User> findByGuardianUsersByPetIdAndUserIdsContaining(Long petId, List<String> userIds);

    List<User> findByIdIn(Set<String> userIds);
}
