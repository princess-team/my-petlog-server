package com.ppp.domain.notification.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MessageCode {
    INVITATION_REQUEST("초대 요청"),
    INVITATION_ACCEPT("초대 수락"),
    INVITATION_REJECT("초대 거절"),
    INVITATION_GUARDIAN_KICK("멤버 삭제"),


    DIARY_COMMENT_CREATE("댓글을 달았습니다."),
    DIARY_RECOMMENT_CREATE("대댓글을 달았습니다."),
    DIARY_TAG("태그를 했습니다."),
    DIARY_LIKE("일기를 좋아합니다."),

    SUBSCRIBE("일기를 구독하기 시작했습니다."),
    SUBSCRIBE_CANCEL("구독을 취소했습니다."),
    ;

    private final String description;
}