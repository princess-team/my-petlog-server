package com.ppp.api.diary.controller;

import com.ppp.api.diary.dto.request.DiaryCreateRequest;
import com.ppp.api.diary.dto.request.DiaryUpdateRequest;
import com.ppp.api.diary.dto.response.DiaryDetailResponse;
import com.ppp.api.diary.dto.response.DiaryGroupByDateResponse;
import com.ppp.api.diary.service.DiaryService;
import com.ppp.api.exception.ExceptionResponse;
import com.ppp.common.security.PrincipalDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Slice;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "Diary", description = "Diary APIs")
@RestController
@Slf4j
@RequiredArgsConstructor
@MultipartConfig(maxFileSize = 1024 * 1024 * 15)
@RequestMapping("/api/v1/pets/{petId}/diaries")
public class DiaryController {
    private final DiaryService diaryService;

    @Operation(summary = "일기 생성")
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = {@Content(schema = @Schema())}),
            @ApiResponse(responseCode = "400", description = "요청 필드 에러", content = {@Content(schema = @Schema(implementation = ExceptionResponse.class))}),
            @ApiResponse(responseCode = "403", description = "기록 공간에 대한 권한 없음", content = {@Content(schema = @Schema(implementation = ExceptionResponse.class))}),
            @ApiResponse(responseCode = "404", description = "일치하는 반려동물 없음", content = {@Content(schema = @Schema(implementation = ExceptionResponse.class))})
    })
    @RequestBody(content = @Content(encoding = @Encoding(name = "request", contentType = "application/json")))
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    private ResponseEntity<Void> createDiary(@PathVariable Long petId,
                                             @Valid @RequestPart DiaryCreateRequest request,
                                             @Valid @RequestPart(required = false)
                                             @Size(max = 10, message = "이미지는 10개 이하로 첨부해주세요.") List<MultipartFile> images,
                                             @AuthenticationPrincipal PrincipalDetails principalDetails) {
        diaryService.createDiary(principalDetails.getUser(), petId, request, images);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "일기 수정")
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = {@Content(schema = @Schema())}),
            @ApiResponse(responseCode = "400", description = "요청 필드 에러", content = {@Content(schema = @Schema(implementation = ExceptionResponse.class))}),
            @ApiResponse(responseCode = "400", description = "게시물 수정 권한 없음", content = {@Content(schema = @Schema(implementation = ExceptionResponse.class))}),
            @ApiResponse(responseCode = "403", description = "기록 공간에 대한 권한 없음", content = {@Content(schema = @Schema(implementation = ExceptionResponse.class))}),
            @ApiResponse(responseCode = "404", description = "일치하는 반려동물 없음", content = {@Content(schema = @Schema(implementation = ExceptionResponse.class))})
    })
    @RequestBody(content = @Content(encoding = @Encoding(name = "request", contentType = "application/json")))
    @PutMapping(value = "/{diaryId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    private ResponseEntity<Void> updateDiary(@PathVariable Long petId,
                                             @PathVariable Long diaryId,
                                             @Valid @RequestPart DiaryUpdateRequest request,
                                             @Valid @RequestPart(required = false)
                                             @Size(max = 10, message = "이미지는 10개 이하로 첨부해주세요.") List<MultipartFile> images,
                                             @AuthenticationPrincipal PrincipalDetails principalDetails) {
        diaryService.updateDiary(principalDetails.getUser(), petId, diaryId, request, images);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "일기 삭제")
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = {@Content(schema = @Schema())}),
            @ApiResponse(responseCode = "400", description = "게시물 삭제 권한 없음", content = {@Content(schema = @Schema(implementation = ExceptionResponse.class))}),
            @ApiResponse(responseCode = "403", description = "기록 공간에 대한 권한 없음", content = {@Content(schema = @Schema(implementation = ExceptionResponse.class))}),
            @ApiResponse(responseCode = "404", description = "일치하는 반려동물 없음", content = {@Content(schema = @Schema(implementation = ExceptionResponse.class))})
    })
    @DeleteMapping(value = "/{diaryId}")
    private ResponseEntity<Void> deleteDiary(@PathVariable Long petId,
                                             @PathVariable Long diaryId,
                                             @AuthenticationPrincipal PrincipalDetails principalDetails) {
        diaryService.deleteDiary(principalDetails.getUser(), petId, diaryId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "일기 상세 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = {@Content(schema = @Schema(implementation = DiaryDetailResponse.class))}),
            @ApiResponse(responseCode = "403", description = "기록 공간에 대한 권한 없음", content = {@Content(schema = @Schema(implementation = ExceptionResponse.class))}),
            @ApiResponse(responseCode = "404", description = "일치하는 일기 없음", content = {@Content(schema = @Schema(implementation = ExceptionResponse.class))})
    })
    @GetMapping(value = "/{diaryId}")
    private ResponseEntity<DiaryDetailResponse> displayDiary(@PathVariable Long petId,
                                                             @PathVariable Long diaryId,
                                                             @AuthenticationPrincipal PrincipalDetails principalDetails) {
        return ResponseEntity.ok(diaryService.displayDiary(principalDetails.getUser(), petId, diaryId));
    }

    @Operation(summary = "일기 리스트 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = {@Content(array = @ArraySchema(schema = @Schema(implementation = DiaryGroupByDateResponse.class)))}),
            @ApiResponse(responseCode = "403", description = "기록 공간에 대한 권한 없음", content = {@Content(schema = @Schema(implementation = ExceptionResponse.class))})
    })
    @GetMapping
    private ResponseEntity<Slice<DiaryGroupByDateResponse>> displayDiaries(@PathVariable Long petId,
                                                                           @RequestParam(defaultValue = "0") int page,
                                                                           @RequestParam(defaultValue = "5") int size,
                                                                           @AuthenticationPrincipal PrincipalDetails principalDetails) {
        return ResponseEntity.ok(diaryService.displayDiaries(principalDetails.getUser(), petId, page, size));
    }

    @Operation(summary = "피드 검색 전용 일기 상세 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = {@Content(schema = @Schema(implementation = DiaryDetailResponse.class))}),
            @ApiResponse(responseCode = "404", description = "일치하는 일기 없음", content = {@Content(schema = @Schema(implementation = ExceptionResponse.class))})
    })
    @GetMapping(value = "/{diaryId}/feed")
    private ResponseEntity<DiaryDetailResponse> displayFeedDiary(@PathVariable Long diaryId,
                                                                 @AuthenticationPrincipal PrincipalDetails principalDetails) {
        return ResponseEntity.ok(diaryService.displayFeedDiary(principalDetails.getUser(), diaryId));
    }
}
