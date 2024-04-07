package com.ppp.api.diary.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.ppp.api.user.dto.response.UserResponse;
import com.ppp.common.util.TimeUtil;
import com.ppp.domain.diary.DiaryComment;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;
import java.util.stream.Collectors;

@Schema(description = "육아 일기 댓글")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
public record DiaryCommentResponse(
        @Schema(description = "댓글 아이디")
        Long commentId,
        @Schema(description = "내용")
        String content,
        @Schema(description = "생성 날짜")
        String createdAt,
        @Schema(description = "삭제된 댓글인지 여부")
        Boolean isDeleted,
        @Schema(description = "유저가 좋아요를 누른 댓글인지 여부")
        Boolean isCurrentUserLiked,
        @Schema(description = "댓글 좋아요 수")
        Integer likeCount,
        @Schema(description = "대댓글 수")
        Integer recommentCount,
        @Schema(description = "글쓴이 정보")
        UserResponse writer,
        @ArraySchema(schema = @Schema(description = "태깅 유저 정보"))
        List<UserResponse> taggedUsers
) {
    public static DiaryCommentResponse from(DiaryComment comment, String currentUserId, boolean isCurrentUserLiked, int likeCount, int recommentCount) {
        return DiaryCommentResponse.builder()
                .commentId(comment.getId())
                .content(comment.getContent())
                .createdAt(TimeUtil.calculateTerm(comment.getCreatedAt()))
                .writer(UserResponse.from(comment.getUser(), currentUserId))
                .isDeleted(false)
                .isCurrentUserLiked(isCurrentUserLiked)
                .likeCount(likeCount)
                .recommentCount(recommentCount)
                .taggedUsers(comment.getTaggedUsersIdNicknameMap().keySet()
                        .stream().map(id -> com.ppp.api.user.dto.response.UserResponse.of(id,
                                comment.getTaggedUsersIdNicknameMap().get(id), currentUserId))
                        .collect(Collectors.toList()))
                .build();
    }

    public static DiaryCommentResponse ofDeletedComment(Long commentId, int recommentCount) {
        return DiaryCommentResponse.builder()
                .commentId(commentId)
                .recommentCount(recommentCount)
                .isDeleted(true)
                .build();
    }

    public static DiaryCommentResponse from(DiaryComment comment, String currentUserId) {
        return DiaryCommentResponse.builder()
                .commentId(comment.getId())
                .content(comment.getContent())
                .isDeleted(false)
                .isCurrentUserLiked(false)
                .likeCount(0)
                .recommentCount(0)
                .createdAt(TimeUtil.calculateTerm(comment.getCreatedAt()))
                .writer(UserResponse.from(comment.getUser(), currentUserId))
                .taggedUsers(comment.getTaggedUsersIdNicknameMap().keySet()
                        .stream().map(id -> com.ppp.api.user.dto.response.UserResponse.of(id,
                                comment.getTaggedUsersIdNicknameMap().get(id), currentUserId))
                        .collect(Collectors.toList()))
                .build();
    }
}
