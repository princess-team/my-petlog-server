package com.ppp.domain.pet.repository;

import com.ppp.domain.pet.Pet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PetRepository extends JpaRepository<Pet, Long> {
    Optional<Pet> findByIdAndIsDeletedFalse(Long id);

    @Query("SELECT p " +
            "FROM Pet p " +
            "WHERE p.id = :petId " +
            "AND p.user.id = :userId " +
            "AND p.isDeleted = false")
    Optional<Pet> findMyPetById(@Param("petId") Long petId, @Param("userId") String userId);

    Optional<Pet> findByInvitedCode(String inviteCode);

    @Query("SELECT p.invitedCode FROM Pet p " +
            "WHERE p.id = :petId " +
            "AND p.isDeleted = false")
    Optional<String> findPetCodeById(@Param("petId") Long petId);
}
