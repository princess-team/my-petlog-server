package com.ppp.api.subscription.controller;

import com.ppp.api.exception.ExceptionResponse;
import com.ppp.api.subscription.dto.request.SubscriberBlockRequest;
import com.ppp.api.subscription.dto.response.SubscribedPetResponse;
import com.ppp.api.subscription.dto.response.SubscriberResponse;
import com.ppp.api.subscription.service.SubscriptionService;
import com.ppp.common.security.PrincipalDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Subscription", description = "Subscription APIs")
@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/pets")
public class SubscriptionController {
    private final SubscriptionService subscriptionService;

    @Operation(summary = "구독 및 구독 취소")
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = {@Content(schema = @Schema())}),
            @ApiResponse(responseCode = "404", description = "일치하는 반려 동물 없음", content = {@Content(schema = @Schema(implementation = ExceptionResponse.class))})
    })
    @PostMapping(value = "/{petId}/subscriptions")
    private ResponseEntity<Void> subscribe(@PathVariable Long petId,
                                           @AuthenticationPrincipal PrincipalDetails principalDetails) {
        subscriptionService.subscribeOrUnsubscribe(petId, principalDetails.getUser());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "구독중인 펫 계정 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = {@Content(array = @ArraySchema(schema = @Schema(implementation = SubscribedPetResponse.class)))}),
            @ApiResponse(responseCode = "403", description = "기록 공간에 대한 권한 없음", content = {@Content(schema = @Schema(implementation = ExceptionResponse.class))})
    })
    @GetMapping(value = "/subscriptions")
    private ResponseEntity<List<SubscribedPetResponse>> displayMySubscribedPets(@AuthenticationPrincipal PrincipalDetails principalDetails) {
        return ResponseEntity.ok(subscriptionService.displayMySubscribedPets(principalDetails.getUser()));
    }

    @Operation(summary = "구독자 리스트 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = {@Content(array = @ArraySchema(schema = @Schema(implementation = SubscriberResponse.class)))}),
            @ApiResponse(responseCode = "403", description = "구독자 관리 권한 없음", content = {@Content(schema = @Schema(implementation = ExceptionResponse.class))})
    })
    @GetMapping(value = "/{petId}/subscriptions")
    private ResponseEntity<List<SubscriberResponse>> displayMyPetsSubscribers(@PathVariable Long petId,
                                                                              @AuthenticationPrincipal PrincipalDetails principalDetails) {
        return ResponseEntity.ok(subscriptionService.displayMyPetsSubscribers(petId, principalDetails.getUser()));
    }

    @Operation(summary = "구독자 차단")
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = {@Content(schema = @Schema())}),
            @ApiResponse(responseCode = "403", description = "구독자 관리 권한 없음", content = {@Content(schema = @Schema(implementation = ExceptionResponse.class))}),
            @ApiResponse(responseCode = "404", description = "일치하는 구독자 없음", content = {@Content(schema = @Schema(implementation = ExceptionResponse.class))})
    })
    @PutMapping(value = "/{petId}/subscriptions")
    private ResponseEntity<Void> blockOrUnblockSubscriber(@PathVariable Long petId,
                                                 @Valid @RequestBody SubscriberBlockRequest request,
                                                 @AuthenticationPrincipal PrincipalDetails principalDetails) {
        subscriptionService.blockOrUnblockSubscriber(petId, request.getUserId(), principalDetails.getUser());
        return ResponseEntity.ok().build();
    }
}
