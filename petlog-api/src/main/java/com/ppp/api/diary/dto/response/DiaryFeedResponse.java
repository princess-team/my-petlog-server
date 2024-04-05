package com.ppp.api.diary.dto.response;

import com.ppp.common.util.TimeUtil;
import com.ppp.domain.diary.dto.PetDiaryDto;
import lombok.Builder;

import java.util.List;
import java.util.stream.Collectors;

@Builder
public record DiaryFeedResponse(
        Pet pet,
        Long diaryId,
        String title,
        String content,
        String createdAt,
        List<DiaryMediaResponse> medias,
        int commentCount,
        int likeCount,
        boolean isCurrentUserLiked

) {
    public static DiaryFeedResponse from(PetDiaryDto dto, int commentCount, boolean isCurrentUserLiked, int likeCount, boolean isSubscribed) {
        return DiaryFeedResponse.builder()
                .diaryId(dto.getDiaryId())
                .title(dto.getTitle())
                .content(dto.getContent())
                .createdAt(TimeUtil.calculateTerm(dto.getCreatedAt()))
                .medias(dto.getDiaryMedias().stream().map(DiaryMediaResponse::from)
                        .collect(Collectors.toList()))
                .commentCount(commentCount)
                .likeCount(likeCount)
                .isCurrentUserLiked(isCurrentUserLiked)
                .pet(Pet.of(dto.getPetId(), dto.getPetName(), dto.getPetProfilePath(), isSubscribed))
                .build();
    }

    public static DiaryFeedResponse from(PetDiaryDto dto, int commentCount, boolean isCurrentUserLiked, int likeCount) {
        return DiaryFeedResponse.builder()
                .diaryId(dto.getDiaryId())
                .title(dto.getTitle())
                .content(dto.getContent())
                .createdAt(TimeUtil.calculateTerm(dto.getCreatedAt()))
                .medias(dto.getDiaryMedias().stream().map(DiaryMediaResponse::from)
                        .collect(Collectors.toList()))
                .commentCount(commentCount)
                .likeCount(likeCount)
                .isCurrentUserLiked(isCurrentUserLiked)
                .pet(Pet.of(dto.getPetId(), dto.getPetName(), dto.getPetProfilePath(), true))
                .build();
    }

    public static DiaryFeedResponse from(PetDiaryDto dto, int commentCount, int likeCount) {
        return DiaryFeedResponse.builder()
                .diaryId(dto.getDiaryId())
                .title(dto.getTitle())
                .content(dto.getContent())
                .createdAt(TimeUtil.calculateTerm(dto.getCreatedAt()))
                .medias(dto.getDiaryMedias().stream().map(DiaryMediaResponse::from)
                        .collect(Collectors.toList()))
                .commentCount(commentCount)
                .likeCount(likeCount)
                .pet(Pet.of(dto.getPetId(), dto.getPetName(), dto.getPetProfilePath(), true))
                .build();
    }

    @Builder
    public record Pet(
            String name,
            Long id,
            String profilePath,
            boolean isSubscribed
    ) {
        public static Pet of(Long id, String name, String profilePath, boolean isSubscribed) {
            return Pet.builder()
                    .id(id)
                    .name(name)
                    .profilePath(profilePath)
                    .isSubscribed(isSubscribed)
                    .build();
        }
    }
}
