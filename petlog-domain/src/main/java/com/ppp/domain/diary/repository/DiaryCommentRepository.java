package com.ppp.domain.diary.repository;

import com.ppp.domain.diary.Diary;
import com.ppp.domain.diary.DiaryComment;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DiaryCommentRepository extends JpaRepository<DiaryComment, Long> {
    Optional<DiaryComment> findByIdAndIsDeletedFalse(Long id);

    @EntityGraph(attributePaths = {"user"}, type = EntityGraph.EntityGraphType.FETCH)
    @Query("select c1 from DiaryComment c1 left join DiaryComment c2 on c1.id = c2.ancestorCommentId " +
            "where (c1.isDeleted = false or c2.id != null) " +
            "and c1.diary.id = ?1 and c1.ancestorCommentId = null ")
    Slice<DiaryComment> findAncestorCommentByDiaryId(Long diaryId, PageRequest request);

    @EntityGraph(attributePaths = {"user"}, type = EntityGraph.EntityGraphType.FETCH)
    Slice<DiaryComment> findByDiaryAndAncestorCommentIdIsNullAndIsDeletedFalse(Diary diary, PageRequest request);

    @EntityGraph(attributePaths = {"parent", "parent.user"}, type = EntityGraph.EntityGraphType.FETCH)
    List<DiaryComment> findByAncestorCommentIdAndIsDeletedFalseOrderByIdDesc(Long ancestorCommentId);

    boolean existsByIdAndIsDeletedFalse(Long id);

    @EntityGraph(attributePaths = {"diary"}, type = EntityGraph.EntityGraphType.FETCH)
    @Query("select c from DiaryComment c inner join Diary d on d.id = c.diary.id where c.id = ?1 and d.pet.id = ?2 and c.isDeleted = false")
    Optional<DiaryComment> findByIdAndPetIdAndIsDeletedFalse(Long id, Long petId);
}
