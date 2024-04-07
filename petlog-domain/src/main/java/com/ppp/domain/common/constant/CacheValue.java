package com.ppp.domain.common.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CacheValue {
    PET_SPACE_AUTHORITY("petSpaceAuthority"),
    DIARY_COMMENT_COUNT("diaryCommentCount"),
    DIARY_COMMENT_RE_COMMENT_COUNT("diaryCommentReCommentCount"),
    DIARY_LIKE_COUNT("diaryLikeCount"),
    DIARY_COMMENT_LIKE_COUNT("diaryCommentLikeCount"),
    DIARY_MOST_USED_TERMS("diaryMostUsedTerms"),
    DIARY_ACCESS_AUTHORITY("diaryAccessAuthority"),
    SUBSCRIPTION_INFO("subscriptionInfo")
    ;

    private final String value;
}
