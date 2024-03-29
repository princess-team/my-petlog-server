package com.ppp.api.subscription.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SubscriberBlockRequest {
    @Schema(description = "유저 아이디")
    @NotBlank(message = "아이디는 공백일 수 없습니다.")
    @Size(min = 1, message = "유효한 아이디를 입력해주세요.")
    private String userId;
}

