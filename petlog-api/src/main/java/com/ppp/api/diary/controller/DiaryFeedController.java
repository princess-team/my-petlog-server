package com.ppp.api.diary.controller;

import com.ppp.api.diary.dto.response.DiaryFeedResponse;
import com.ppp.api.diary.service.DiaryFeedService;
import com.ppp.common.security.PrincipalDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
import java.util.Set;

@Tag(name = "Diary Feed", description = "Diary Feed APIs")
@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/diaries/feed")
public class DiaryFeedController {
    private final DiaryFeedService diaryFeedService;


    @Operation(summary = "피드 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = {@Content(array = @ArraySchema(schema = @Schema(implementation = DiaryFeedResponse.class)))})
    })
    @GetMapping
    private ResponseEntity<Set<DiaryFeedResponse>> retrieveDiaryFeed(@RequestParam(defaultValue = "0") int page,
                                                                     @RequestParam(defaultValue = "5") int size,
                                                                     @AuthenticationPrincipal PrincipalDetails principalDetails) {
        return ResponseEntity.ok(diaryFeedService.retrieveDiaryFeed(Optional.ofNullable(principalDetails), page, size));
    }
}
