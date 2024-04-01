package com.ppp.api.diary.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ppp.domain.diary.DiaryDraft;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "육아 일기 임시 저장")
@Builder
public record DiaryDraftResponse(
        @Schema(description = "일기 제목")
        String title,
        @Schema(description = "일기 내용")
        String content,
        @Schema(description = "일기 쓴 날짜", example = "2024.02.11")
        @JsonFormat(pattern = "yyyy.MM.dd")
        LocalDate date,
        @Schema(description = "전체 공개 일기인지 여부")
        boolean isPublic,
        @ArraySchema
        List<String> images,
        @ArraySchema
        List<String> videos
) {
    public static DiaryDraftResponse from(DiaryDraft diaryDraft) {
        return DiaryDraftResponse.builder()
                .title(diaryDraft.getTitle())
                .content(diaryDraft.getContent())
                .date(diaryDraft.getDate())
                .isPublic(diaryDraft.isPublic())
                .images(diaryDraft.getImages())
                .videos(diaryDraft.getVideos())
                .build();
    }
}
