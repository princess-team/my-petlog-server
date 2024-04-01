package com.ppp.api.diary.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    FORBIDDEN_PET_SPACE(HttpStatus.FORBIDDEN, "DIARY-0001","해당 기록 공간에 대한 권한이 없습니다."),
    DIARY_NOT_FOUND(HttpStatus.NOT_FOUND, "DIARY-0002","일치하는 일기가 없습니다."),
    NOT_DIARY_OWNER(HttpStatus.BAD_REQUEST, "DIARY-0003", "일기 작성자가 아닙니다."),
    DIARY_COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "DIARY-0004","일치하는 댓글이 없습니다."),
    NOT_DIARY_COMMENT_OWNER(HttpStatus.BAD_REQUEST, "DIARY-0005", "댓글 작성자가 아닙니다."),
    MEDIA_UPLOAD_LIMIT_OVER(HttpStatus.BAD_REQUEST, "DIARY-0006", "허용되는 미디어 수 초과입니다."),
    DIARY_DRAFT_NOT_FOUND(HttpStatus.NOT_FOUND, "DIARY-0007","임시 저장한 일기가 없습니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}

