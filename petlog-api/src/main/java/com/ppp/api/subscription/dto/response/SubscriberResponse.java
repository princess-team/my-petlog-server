package com.ppp.api.subscription.dto.response;

import com.ppp.domain.subscription.Subscription;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(description = "구독자 유저")
@Builder
public record SubscriberResponse(
        @Schema(description = "구독자 유저 아이디")
        String id,
        @Schema(description = "구독자 유저 닉네임")
        String nickname,
        @Schema(description = "구독자 유저 프로필")
        String profilePath,
        boolean isBlocked
) {
    public static SubscriberResponse from(Subscription subscription) {
        return SubscriberResponse.builder()
                .isBlocked(subscription.isBlocked())
                .id(subscription.getSubscriber().getId())
                .nickname(subscription.getSubscriber().getNickname())
                .profilePath(subscription.getSubscriber().getProfilePath())
                .build();
    }
}
