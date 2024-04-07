package com.ppp.api.diary.validator;

import com.ppp.api.diary.exception.DiaryException;
import com.ppp.domain.diary.Diary;
import com.ppp.domain.diary.repository.DiaryRepository;
import com.ppp.domain.guardian.repository.GuardianRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import static com.ppp.api.diary.exception.ErrorCode.DIARY_NOT_FOUND;
import static com.ppp.api.diary.exception.ErrorCode.FORBIDDEN_PET_SPACE;

@Component
@Slf4j
@RequiredArgsConstructor
public class DiaryAccessValidator {
    private final GuardianRepository guardianRepository;
    private final DiaryRepository diaryRepository;

    @Cacheable(value = "diaryAccessAuthority", key = "{#petId, #userId, #diaryId}")
    public boolean validateAccessDiary(Long petId, String userId, Long diaryId) {
        log.error("validateAccessDiary");
        Diary diary = diaryRepository.findByIdAndPetIdAndIsDeletedFalse(diaryId, petId)
                .orElseThrow(() -> new DiaryException(DIARY_NOT_FOUND));
        if (diary.isPublic()) return true;
        if (!guardianRepository.existsByUserIdAndPetId(userId, petId))
            throw new DiaryException(FORBIDDEN_PET_SPACE);
        return true;
    }

    @Cacheable(value = "diaryAccessAuthority", key = "{#petId, #userId, #diary.id}")
    public boolean validateAccessDiary(Long petId, String userId, Diary diary) {
        log.error("validateAccessDiary");
        if (diary.isPublic()) return true;
        if (!guardianRepository.existsByUserIdAndPetId(userId, petId))
            throw new DiaryException(FORBIDDEN_PET_SPACE);
        return true;
    }
}
