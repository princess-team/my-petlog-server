package com.ppp.api.auth.dto.request;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SocialRequest {
    @Schema(description = "이메일", example = "abc@test.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;
    @Schema(description = "로그인 타입", allowableValues = {"KAKAO", "GOOGLE"}, requiredMode = Schema.RequiredMode.REQUIRED)
    private String loginType;
}