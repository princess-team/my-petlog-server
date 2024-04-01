package com.ppp.api.diary.dto.response;

import lombok.Builder;

@Builder
public record DiaryDraftCheckResponse(
        Boolean hasDiaryDraft
) {
    public static DiaryDraftCheckResponse of(boolean hasDiaryDraft) {
        return DiaryDraftCheckResponse.builder()
                .hasDiaryDraft(hasDiaryDraft)
                .build();
    }
}
