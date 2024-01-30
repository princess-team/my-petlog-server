package com.ppp.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    FILE_UPLOAD_FAILED(HttpStatus.BAD_REQUEST,"GLOBAL-0002", "피일 업로드에 실패했습니다."),
    NOT_VALID_EXTENSION(HttpStatus.BAD_REQUEST,"GLOBAL-0003", "적합한 확장자가 아닙니다.")
    NOT_FOUND_TOKEN(HttpStatus.UNAUTHORIZED, "TOKEN-0001", "토큰을 찾을 수 없습니다."),
    REFRESHTOKEN_EXPIRATION(HttpStatus.UNAUTHORIZED, "TOKEN-0002", "리프레시 토큰 만료, 로그인 필요"),
    INVALID_SIGNATURE(HttpStatus.UNAUTHORIZED, "TOKEN-0003","JWT의 서명이 올바르지 않음"),
    MALFORMED_TOKEN(HttpStatus.UNAUTHORIZED, "TOKEN-0004","토큰 형식이 잘못됨"),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "TOKEN-0005","토큰 만료"),
    UNSUPPORTED_TOKEN(HttpStatus.UNAUTHORIZED, "TOKEN-0006","지원하지 않는 토큰 형식"),
    ILLEGALARGUMENT_TOKEN(HttpStatus.UNAUTHORIZED, "TOKEN-0007","JWT 클레임 문자열이 비어 있음"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "TOKEN-0008", "인증실패");

    private final HttpStatus status;
    private final String code;
    private final String message;

}
