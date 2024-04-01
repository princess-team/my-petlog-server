package com.ppp.api.diary.controller;

import com.ppp.api.diary.dto.request.DiaryDraftCreateRequest;
import com.ppp.api.diary.dto.response.DiaryDraftCheckResponse;
import com.ppp.api.diary.dto.response.DiaryDraftResponse;
import com.ppp.api.diary.service.DiaryDraftService;
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
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "Diary Draft", description = "Diary Draft APIs")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/pets/{petId}/diaries/drafts")
public class DiaryDraftController {
    private final DiaryDraftService diaryDraftService;

    @Operation(summary = "임시 저장 일기 생성")
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = {@Content(schema = @Schema())}),
            @ApiResponse(responseCode = "400", description = "요청 필드 에러", content = {@Content(schema = @Schema(implementation = ExceptionResponse.class))})
    })
    @RequestBody(content = @Content(encoding = @Encoding(name = "request", contentType = "application/json")))
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    private ResponseEntity<Void> createDiaryDraft(@PathVariable Long petId,
                                                  @Valid @RequestPart DiaryDraftCreateRequest request,
                                                  @Valid @RequestPart(required = false)
                                                  @Size(max = 10, message = "이미지는 10개 이하로 첨부해주세요.") List<MultipartFile> images,
                                                  @AuthenticationPrincipal PrincipalDetails principalDetails) {
        diaryDraftService.createDiaryDraft(petId, request, images, principalDetails.getUser());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "임시 저장 일기 존재 여부 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = {@Content(array = @ArraySchema(schema = @Schema(implementation = DiaryDraftCheckResponse.class)))})
    })
    @GetMapping(value = "/check")
    private ResponseEntity<DiaryDraftCheckResponse> checkHasDiaryDraft(@PathVariable Long petId,
                                                                       @AuthenticationPrincipal PrincipalDetails principalDetails) {
        return ResponseEntity.ok(diaryDraftService.checkHasDiaryDraft(petId, principalDetails.getUser()));
    }

    @Operation(summary = "임시 저장 일기 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = {@Content(array = @ArraySchema(schema = @Schema(implementation = DiaryDraftResponse.class)))}),
            @ApiResponse(responseCode = "404", description = "임시 저장 일기 없음", content = {@Content(schema = @Schema(implementation = ExceptionResponse.class))})
    })
    @GetMapping
    private ResponseEntity<DiaryDraftResponse> retrieveDiaryDraft(@PathVariable Long petId,
                                                                  @AuthenticationPrincipal PrincipalDetails principalDetails) {
        return ResponseEntity.ok(diaryDraftService.retrieveDiaryDraft(petId, principalDetails.getUser()));
    }
}
