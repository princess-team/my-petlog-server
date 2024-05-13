package com.ppp.api.auth.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    NOT_FOUND_TOKEN(HttpStatus.UNAUTHORIZED, "TOKEN-0001", "토큰을 찾을 수 없습니다."),
    REFRESHTOKEN_EXPIRATION(HttpStatus.UNAUTHORIZED, "TOKEN-0002", "리프레시 토큰 만료, 로그인이 필요합니다."),
    INVALID_SIGNATURE(HttpStatus.UNAUTHORIZED, "TOKEN-0003","JWT의 서명이 올바르지 않습니다."),
    MALFORMED_TOKEN(HttpStatus.UNAUTHORIZED, "TOKEN-0004","토큰 형식이 잘못되었습니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "TOKEN-0005","토큰이 만료되었습니다."),
    UNSUPPORTED_TOKEN(HttpStatus.UNAUTHORIZED, "TOKEN-0006","지원하지 않는 토큰 형식입니다."),
    ILLEGALARGUMENT_TOKEN(HttpStatus.UNAUTHORIZED, "TOKEN-0007","JWT 클레임 문자열이 비어 있습니다."),

    EXISTS_EMAIL(HttpStatus.CONFLICT, "AUTH-0001", "이미 사용 중인 이메일 주소입니다."),
    NOTMATCH_PASSWORD(HttpStatus.BAD_REQUEST, "AUTH-0002", "현재 비밀번호와 다릅니다."),
    UNABLE_TO_SEND_EMAIL(HttpStatus.BAD_REQUEST, "AUTH-0003", "3분이 지나지 않았습니다. 잠시 후 다시 시도해주세요."),
    CODE_EXPIRATION(HttpStatus.BAD_REQUEST, "AUTH-0004", "인증번호의 유효기간이 만료되었습니다. 인증번호를 재발송해주세요."),
    SEND_EMAIL_FAILURE(HttpStatus.INTERNAL_SERVER_ERROR, "AUTH-0005", "이메일 전송에 실패하였습니다. 인증번호를 재발송해주세요."),
    VERIFICATION_CODE_NOT_MATCHED(HttpStatus.NOT_FOUND, "AUTH-0006", "인증번호가 일치하지 않습니다. 다시확인해주세요.")
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;

}
