package com.ppp.domain.diary.repository;

import com.ppp.domain.diary.Diary;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface DiaryRepository extends JpaRepository<Diary, Long> {
    @EntityGraph(attributePaths = {"user", "pet", "diaryMedias"}, type = EntityGraph.EntityGraphType.FETCH)
    Optional<Diary> findByIdAndIsDeletedFalse(Long id);
    @EntityGraph(attributePaths = {"user"}, type = EntityGraph.EntityGraphType.FETCH)
    Slice<Diary> findByPetIdAndIsDeletedFalseAndIsPublicInOrderByDateDesc(Long petId, Set<Boolean> isPublicFilter, PageRequest pageRequest);
    boolean existsByIdAndIsDeletedFalse(Long id);
}
