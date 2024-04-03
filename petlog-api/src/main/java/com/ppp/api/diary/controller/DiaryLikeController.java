package com.ppp.api.diary.controller;

import com.ppp.api.diary.service.DiaryLikeService;
import com.ppp.api.exception.ExceptionResponse;
import com.ppp.api.user.dto.response.UserResponse;
import com.ppp.common.security.PrincipalDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Diary Like", description = "Diary Like APIs")
@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/pets")
public class DiaryLikeController {
    private final DiaryLikeService diaryLikeService;


    @Operation(summary = "일기 좋아요")
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = {@Content(schema = @Schema())}),
            @ApiResponse(responseCode = "403", description = "기록 공간에 대한 권한 없음", content = {@Content(schema = @Schema(implementation = ExceptionResponse.class))}),
            @ApiResponse(responseCode = "404", description = "일치하는 일기 없음", content = {@Content(schema = @Schema(implementation = ExceptionResponse.class))})
    })
    @PostMapping(value = "/{petId}/diaries/{diaryId}/like")
    private ResponseEntity<Void> likeDiary(@PathVariable Long petId,
                                           @PathVariable Long diaryId,
                                           @AuthenticationPrincipal PrincipalDetails principalDetails) {
        diaryLikeService.likeDiary(principalDetails.getUser(), petId, diaryId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "일기 좋아요 리스트 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = {@Content(schema = @Schema())})
    })
    @GetMapping(value = "/diaries/{diaryId}/like")
    private ResponseEntity<List<UserResponse>> retrieveDiaryLikeUsers(
            @PathVariable Long diaryId,
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        return ResponseEntity.ok(diaryLikeService.retrieveDiaryLikeUsers(principalDetails.getUser(), diaryId));
    }
}
