package com.ppp.domain.diary.repository;

import com.ppp.domain.diary.dto.DiaryMediaDto;
import com.ppp.domain.diary.dto.PetDiaryDto;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import static com.ppp.domain.diary.QDiary.diary;
import static com.ppp.domain.diary.QDiaryMedia.diaryMedia;
import static com.ppp.domain.pet.QPetImage.petImage;
import static com.querydsl.core.group.GroupBy.groupBy;
import static com.querydsl.core.group.GroupBy.list;
import static com.querydsl.core.types.Projections.constructor;

@RequiredArgsConstructor
@Repository
public class DiaryQuerydslRepository {
    private final JPAQueryFactory jpaQueryFactory;
    private final DiaryRepository diaryRepository;

    private Long getRandomSelectedPetsDiaryOffset() {
        int totalDiaryCount = diaryRepository.countByIsPublicTrueAndIsDeletedFalse();
        return ThreadLocalRandom.current().nextLong(-1, totalDiaryCount) + 1;
    }

    public List<PetDiaryDto> findRandomPetsDiaries(Pageable pageable) {
        return jpaQueryFactory.from(diary)
                .leftJoin(diary.diaryMedias, diaryMedia)
                .leftJoin(petImage).on(petImage.pet.id.eq(diary.pet.id))
                .where(diary.isPublic.eq(true), diary.isDeleted.eq(false))
                .offset(getRandomSelectedPetsDiaryOffset())
                .limit(pageable.getPageSize())
                .fetchJoin()
                .transform(
                        groupBy(diary.id)
                                .list(constructor(PetDiaryDto.class,
                                        diary.id, diary.pet.id, diary.pet.name,
                                        list(constructor(DiaryMediaDto.class, diaryMedia.id, diaryMedia.type, diaryMedia.path)),
                                        petImage.url, diary.content, diary.title, diary.createdAt))
                );
    }

    public List<PetDiaryDto> findRandomPetsDiaries(Set<Long> blockedPetIds, Pageable pageable) {
        return jpaQueryFactory.from(diary)
                .leftJoin(diary.diaryMedias, diaryMedia)
                .leftJoin(petImage).on(petImage.pet.id.eq(diary.pet.id))
                .where(diary.pet.id.notIn(blockedPetIds), diary.isPublic.eq(true), diary.isDeleted.eq(false))
                .offset((long) Math.ceil(getRandomSelectedPetsDiaryOffset() * 0.75))
                .limit(pageable.getPageSize())
                .fetchJoin()
                .transform(
                        groupBy(diary.id)
                                .list(constructor(PetDiaryDto.class,
                                        diary.id, diary.pet.id, diary.pet.name,
                                        list(constructor(DiaryMediaDto.class, diaryMedia.id, diaryMedia.type, diaryMedia.path)),
                                        petImage.url, diary.content, diary.title, diary.createdAt))
                );
    }

    public List<PetDiaryDto> findSubscribedPetsDiariesBySubscription(Set<Long> subscribedPetIds, Pageable pageable) {
        return jpaQueryFactory
                .from(diary)
                .leftJoin(diary.diaryMedias, diaryMedia)
                .leftJoin(petImage).on(petImage.pet.id.eq(diary.pet.id))
                .where(diary.pet.id.in(subscribedPetIds),
                        diary.isPublic.eq(true),
                        diary.isDeleted.eq(false))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(diary.createdAt.desc())
                .fetchJoin()
                .transform(
                        groupBy(diary.id)
                                .list(constructor(PetDiaryDto.class,
                                        diary.id, diary.pet.id, diary.pet.name,
                                        list(constructor(DiaryMediaDto.class, diaryMedia.id, diaryMedia.type, diaryMedia.path)),
                                        petImage.url, diary.content, diary.title, diary.createdAt))
                );
    }

    public boolean hasNext(List<PetDiaryDto> contents, int pageSize) {
        if (contents.size() > pageSize) {
            contents.remove(pageSize);
            return true;
        }
        return false;
    }
}
