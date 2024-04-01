package com.ppp.api.diary.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ppp.api.pet.dto.response.PetResponse;
import com.ppp.api.user.dto.response.UserResponse;
import com.ppp.domain.diary.Diary;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Schema(description = "육아 일기")
@Builder
public record DiaryDetailResponse(
        @Schema(description = "일기 아이디")
        Long diaryId,
        @Schema(description = "일기 제목")
        String title,
        @Schema(description = "일기 내용")
        String content,
        @Schema(description = "일기 쓴 날짜", example = "2024.02.11")
        @JsonFormat(pattern = "yyyy.MM.dd")
        LocalDate date,
        @ArraySchema(schema = @Schema(implementation = DiaryMediaResponse.class))
        List<DiaryMediaResponse> images,
        @ArraySchema(schema = @Schema(implementation = DiaryMediaResponse.class))
        List<DiaryMediaResponse> videos,
        @Schema(description = "전체 공개 일기인지 여부")
        boolean isPublic,
        @Schema(description = "유저가 좋아요를 누른 글인지 여부")
        boolean isCurrentUserLiked,
        @Schema(description = "글쓴이에 대한 정보")
        UserResponse writer,
        @Schema(description = "댓글 개수")
        int commentCount,
        @Schema(description = "좋아요 개수")
        int likeCount,
        @Schema(description = "반려 동물 정보")
        PetResponse pet
) {
    public static DiaryDetailResponse from(Diary diary, String currentUserId, int commentCount, boolean isCurrentUserLiked, int likeCount) {
        return DiaryDetailResponse.builder()
                .diaryId(diary.getId())
                .title(diary.getTitle())
                .content(diary.getContent())
                .commentCount(commentCount)
                .isPublic(diary.isPublic())
                .images(diary.getImageMedias().stream()
                        .map(DiaryMediaResponse::from)
                        .collect(Collectors.toList()))
                .videos(diary.getVideoMedias().stream()
                        .map(DiaryMediaResponse::from)
                        .collect(Collectors.toList()))
                .date(diary.getDate())
                .isCurrentUserLiked(isCurrentUserLiked)
                .likeCount(likeCount)
                .writer(UserResponse.from(diary.getUser(), currentUserId))
                .pet(PetResponse.from(diary.getPet()))
                .build();
    }
}
