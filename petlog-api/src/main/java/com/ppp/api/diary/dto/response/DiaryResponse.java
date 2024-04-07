package com.ppp.api.diary.dto.response;

import com.ppp.api.user.dto.response.UserResponse;
import com.ppp.domain.diary.Diary;
import com.ppp.domain.diary.DiaryDocument;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(description = "육아 일기")
@Builder
public record DiaryResponse(
        @Schema(description = "일기 아이디")
        Long diaryId,
        @Schema(description = "일기 제목")
        String title,
        @Schema(description = "일기 내용")
        String content,
        @Schema(description = "일기 썸네일")
        String thumbnailPath,
        @Schema(description = "글쓴이에 대한 정보")
        UserResponse writer,
        @Schema(description = "댓글 개수")
        int commentCount,
        boolean isPublic
) {
    public static DiaryResponse from(Diary diary, String currentUserId, int commentCount) {
        return DiaryResponse.builder()
                .diaryId(diary.getId())
                .title(diary.getTitle())
                .content(diary.getContent())
                .thumbnailPath(diary.getThumbnailPath())
                .writer(UserResponse.of(diary.getUser().getId(), diary.getUser().getNickname(), currentUserId))
                .commentCount(commentCount)
                .isPublic(diary.isPublic())
                .build();
    }

    public static DiaryResponse from(DiaryDocument diaryDocument, String currentUserId, int commentCount) {
        return DiaryResponse.builder()
                .diaryId(Long.parseLong(diaryDocument.getId()))
                .title(diaryDocument.getTitle())
                .isPublic(diaryDocument.isPublic())
                .content(diaryDocument.getContent())
                .thumbnailPath(diaryDocument.getThumbnailPath())
                .writer(UserResponse.from(diaryDocument.getUser(), currentUserId))
                .commentCount(commentCount)
                .build();
    }
}
