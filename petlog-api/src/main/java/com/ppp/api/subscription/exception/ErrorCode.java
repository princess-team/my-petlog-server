package com.ppp.api.subscription.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    FORBIDDEN_PET_SPACE(HttpStatus.FORBIDDEN, "SUBSCRIPTION-0001", "해당 공간에 대한 권한이 없습니다."),
    SUBSCRIBER_NOT_FOUND(HttpStatus.BAD_REQUEST, "SUBSCRIPTION-0002", "해당 구독자를 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}

