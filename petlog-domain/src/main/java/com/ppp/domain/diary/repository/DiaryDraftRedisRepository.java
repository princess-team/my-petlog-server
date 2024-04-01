package com.ppp.domain.diary.repository;

import com.ppp.domain.diary.DiaryDraft;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DiaryDraftRedisRepository extends CrudRepository<DiaryDraft, String> {
    Optional<DiaryDraft> findByPetIdAndUserId(Long petId, String userId);
    boolean existsByPetIdAndUserId(Long petId, String userId);
}
