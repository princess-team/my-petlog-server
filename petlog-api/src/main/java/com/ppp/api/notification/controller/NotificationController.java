package com.ppp.api.notification.controller;

import com.ppp.api.notification.dto.response.NotificationResponse;
import com.ppp.api.notification.service.NotificationService;
import com.ppp.common.security.PrincipalDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "알림", description = "알림 APIs")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "알림목록조회", description = "알림 목록을 페이지네이션으로 조회합니다. 순서: 알림생성일자 내림차순, 읽지 않은 알림")
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = {@Content(schema = @Schema())}),
    })
    @GetMapping("/v1/notifications")
    public Page<NotificationResponse> displayNotifications(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            Pageable pageable) {
        return notificationService.displayNotifications(principalDetails.getUser(), pageable);
    }

    @Operation(summary = "모든 알림 읽음 처리")
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = {@Content(schema = @Schema())}),
    })
    @PostMapping("/v1/notifications/read-all")
    public ResponseEntity<Void> readNotifications(@AuthenticationPrincipal PrincipalDetails principalDetails) {
        notificationService.readNotifications(principalDetails.getUser());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "모든 알림 삭제")
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = {@Content(schema = @Schema())}),
    })
    @DeleteMapping("/v1/notifications")
    public ResponseEntity<Void> deleteNotifications(@AuthenticationPrincipal PrincipalDetails principalDetail) {
        notificationService.deleteNotifications(principalDetail.getUser());
        return ResponseEntity.ok().build();
    }

}