package com.ppp.domain.diary.repository;

import com.ppp.domain.diary.Diary;
import com.ppp.domain.diary.DiaryComment;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DiaryCommentRepository extends JpaRepository<DiaryComment, Long> {
    Optional<DiaryComment> findByIdAndIsDeletedFalse(Long id);

    @EntityGraph(attributePaths = {"user"}, type = EntityGraph.EntityGraphType.FETCH)
    Slice<DiaryComment> findByDiaryAndAncestorCommentIdIsNullAndIsDeletedFalse(Diary diary, PageRequest request);

    @EntityGraph(attributePaths = {"parent", "parent.user"}, type = EntityGraph.EntityGraphType.FETCH)
    List<DiaryComment> findByAncestorCommentIdAndIsDeletedFalseOrderByIdDesc(Long ancestorCommentId);

    boolean existsByIdAndIsDeletedFalse(Long id);

    @EntityGraph(attributePaths = {"diary"}, type = EntityGraph.EntityGraphType.FETCH)
    @Query("select c from DiaryComment c inner join Diary d on d.id = c.diary.id where c.id = ?1 and d.pet.id = ?2 and c.isDeleted = false")
    Optional<DiaryComment> findByIdAndPetIdAndIsDeletedFalse(Long id, Long petId);
}
