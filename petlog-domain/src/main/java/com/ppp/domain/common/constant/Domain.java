package com.ppp.domain.common.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Domain {
    USER(false),
    DIARY(true),
    DIARY_LIKE(false),
    DIARY_COMMENT(false),
    DIARY_RE_COMMENT(false),
    DIARY_COMMENT_LIKE(false),
    DIARY_DRAFT(true),
    PET(false);
    private final boolean hasVideo;
}
