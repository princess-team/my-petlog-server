package com.ppp.api.diary.controller;

import com.ppp.api.diary.dto.response.DiaryGroupByDateResponse;
import com.ppp.api.diary.dto.response.DiaryMostUsedTermsResponse;
import com.ppp.api.diary.service.DiarySearchService;
import com.ppp.api.exception.ExceptionResponse;
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
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Diary Search", description = "Diary Search APIs")
@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/pets")
public class DiarySearchController {
    private final DiarySearchService diarySearchService;

    @Operation(summary = "일기 검색")
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = {@Content(array = @ArraySchema(schema = @Schema(implementation = DiaryGroupByDateResponse.class)))}),
            @ApiResponse(responseCode = "403", description = "기록 공간에 대한 권한 없음", content = {@Content(schema = @Schema(implementation = ExceptionResponse.class))})
    })
    @GetMapping("/{petId}/diaries/search")
    private ResponseEntity<Page<DiaryGroupByDateResponse>> searchInPetSpace(@PathVariable Long petId,
                                                                            @RequestParam String keyword,
                                                                            @RequestParam(defaultValue = "0") int page,
                                                                            @RequestParam(defaultValue = "5") int size,
                                                                            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        return ResponseEntity.ok(diarySearchService.searchInPetSpace(principalDetails.getUser(), keyword, petId, page, size));
    }

    @Operation(summary = "일기에서 자주 사용한 용어 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = {@Content(array = @ArraySchema(schema = @Schema(implementation = DiaryMostUsedTermsResponse.class)))}),
            @ApiResponse(responseCode = "403", description = "기록 공간에 대한 권한 없음", content = {@Content(schema = @Schema(implementation = ExceptionResponse.class))})
    })
    @GetMapping("/{petId}/diaries/search/terms")
    private ResponseEntity<DiaryMostUsedTermsResponse> findMostUsedTermsByPetId(@PathVariable Long petId,
                                                                                @AuthenticationPrincipal PrincipalDetails principalDetails) {
        return ResponseEntity.ok(diarySearchService.findMostUsedTermsByPetId(principalDetails.getUser(), petId));
    }

    @Operation(summary = "친구의 일기 검색")
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = {@Content(array = @ArraySchema(schema = @Schema(implementation = DiaryGroupByDateResponse.class)))}),
            @ApiResponse(responseCode = "403", description = "기록 공간에 대한 권한 없음", content = {@Content(schema = @Schema(implementation = ExceptionResponse.class))})
    })
    @GetMapping("/diaries/search")
    private ResponseEntity<Page<DiaryGroupByDateResponse>> searchInFeed(@RequestParam String keyword,
                                                                        @RequestParam(defaultValue = "0") int page,
                                                                        @RequestParam(defaultValue = "5") int size,
                                                                        @AuthenticationPrincipal PrincipalDetails principalDetails) {
        return ResponseEntity.ok(diarySearchService.searchInFeed(principalDetails.getUser(), keyword, page, size));
    }
}
