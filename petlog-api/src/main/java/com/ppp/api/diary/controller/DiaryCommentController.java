package com.ppp.api.diary.controller;

import com.ppp.api.diary.dto.request.DiaryCommentRequest;
import com.ppp.api.diary.dto.response.DiaryCommentResponse;
import com.ppp.api.diary.dto.response.DiaryReCommentResponse;
import com.ppp.api.diary.service.DiaryCommentService;
import com.ppp.api.exception.ExceptionResponse;
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
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Diary Comment", description = "Diary Comment APIs")
@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/pets/{petId}/diaries")
public class DiaryCommentController {
    private final DiaryCommentService diaryCommentService;

    @Operation(summary = "댓글 생성")
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = {@Content(schema = @Schema())}),
            @ApiResponse(responseCode = "400", description = "요청 필드 에러", content = {@Content(schema = @Schema(implementation = ExceptionResponse.class))}),
            @ApiResponse(responseCode = "403", description = "기록 공간에 대한 권한 없음", content = {@Content(schema = @Schema(implementation = ExceptionResponse.class))}),
            @ApiResponse(responseCode = "404", description = "일치하는 일기 없음", content = {@Content(schema = @Schema(implementation = ExceptionResponse.class))})
    })
    @PostMapping(value = "/{diaryId}/comments")
    private ResponseEntity<DiaryCommentResponse> createComment(@PathVariable Long petId,
                                                               @PathVariable Long diaryId,
                                                               @Valid @RequestBody DiaryCommentRequest request,
                                                               @AuthenticationPrincipal PrincipalDetails principalDetails) {
        return ResponseEntity.ok(diaryCommentService.createComment(principalDetails.getUser(), petId, diaryId, request));
    }

    @Operation(summary = "댓글 수정")
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = {@Content(schema = @Schema())}),
            @ApiResponse(responseCode = "400", description = "요청 필드 에러, 댓글 수정 권한 없음", content = {@Content(schema = @Schema(implementation = ExceptionResponse.class))}),
            @ApiResponse(responseCode = "403", description = "기록 공간에 대한 권한 없음", content = {@Content(schema = @Schema(implementation = ExceptionResponse.class))}),
            @ApiResponse(responseCode = "404", description = "일치하는 댓글 없음", content = {@Content(schema = @Schema(implementation = ExceptionResponse.class))})
    })
    @PutMapping(value = "/comments/{commentId}")
    private ResponseEntity<Void> updateComment(@PathVariable Long petId,
                                               @PathVariable Long commentId,
                                               @Valid @RequestBody DiaryCommentRequest request,
                                               @AuthenticationPrincipal PrincipalDetails principalDetails) {
        diaryCommentService.updateComment(principalDetails.getUser(), petId, commentId, request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "댓글 삭제")
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = {@Content(schema = @Schema())}),
            @ApiResponse(responseCode = "400", description = "댓글 수정 권한 없음", content = {@Content(schema = @Schema(implementation = ExceptionResponse.class))}),
            @ApiResponse(responseCode = "403", description = "기록 공간에 대한 권한 없음", content = {@Content(schema = @Schema(implementation = ExceptionResponse.class))}),
            @ApiResponse(responseCode = "404", description = "일치하는 댓글 없음", content = {@Content(schema = @Schema(implementation = ExceptionResponse.class))})
    })
    @DeleteMapping(value = "/comments/{commentId}")
    private ResponseEntity<Void> deleteComment(@PathVariable Long petId,
                                               @PathVariable Long commentId,
                                               @AuthenticationPrincipal PrincipalDetails principalDetails) {
        diaryCommentService.deleteComment(principalDetails.getUser(), petId, commentId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "댓글 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = {@Content(array = @ArraySchema(schema = @Schema(implementation = DiaryCommentResponse.class)))}),
            @ApiResponse(responseCode = "403", description = "기록 공간에 대한 권한 없음", content = {@Content(schema = @Schema(implementation = ExceptionResponse.class))})
    })
    @GetMapping(value = "/{diaryId}/comments")
    private ResponseEntity<Slice<DiaryCommentResponse>> displayComments(@PathVariable Long petId,
                                                                        @PathVariable Long diaryId,
                                                                        @RequestParam(defaultValue = "0") int page,
                                                                        @RequestParam(defaultValue = "5") int size,
                                                                        @AuthenticationPrincipal PrincipalDetails principalDetails) {
        return ResponseEntity
                .ok(diaryCommentService.displayComments(principalDetails.getUser(), petId, diaryId, page, size));
    }

    @Operation(summary = "댓글 좋아요")
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = {@Content(schema = @Schema())}),
            @ApiResponse(responseCode = "403", description = "기록 공간에 대한 권한 없음", content = {@Content(schema = @Schema(implementation = ExceptionResponse.class))}),
            @ApiResponse(responseCode = "404", description = "일치하는 댓글 없음", content = {@Content(schema = @Schema(implementation = ExceptionResponse.class))})
    })
    @PostMapping(value = "/comments/{commentId}/like")
    private ResponseEntity<Void> likeComment(@PathVariable Long petId,
                                             @PathVariable Long commentId,
                                             @AuthenticationPrincipal PrincipalDetails principalDetails) {
        diaryCommentService.likeComment(principalDetails.getUser(), petId, commentId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "대댓글 생성")
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = {@Content(schema = @Schema())}),
            @ApiResponse(responseCode = "400", description = "요청 필드 에러", content = {@Content(schema = @Schema(implementation = ExceptionResponse.class))}),
            @ApiResponse(responseCode = "403", description = "기록 공간에 대한 권한 없음", content = {@Content(schema = @Schema(implementation = ExceptionResponse.class))}),
            @ApiResponse(responseCode = "404", description = "일치하는 댓글 없음", content = {@Content(schema = @Schema(implementation = ExceptionResponse.class))})
    })
    @PostMapping(value = "/comments/{commentId}/recomment")
    private ResponseEntity<DiaryReCommentResponse> createReComment(@PathVariable Long petId,
                                                                   @PathVariable Long commentId,
                                                                   @Valid @RequestBody DiaryCommentRequest request,
                                                                   @AuthenticationPrincipal PrincipalDetails principalDetails) {
        return ResponseEntity.ok(diaryCommentService.createReComment(principalDetails.getUser(), petId, commentId, request));
    }

    @Operation(summary = "대댓글 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = {@Content(schema = @Schema())}),
            @ApiResponse(responseCode = "400", description = "요청 필드 에러", content = {@Content(schema = @Schema(implementation = ExceptionResponse.class))}),
            @ApiResponse(responseCode = "403", description = "기록 공간에 대한 권한 없음", content = {@Content(schema = @Schema(implementation = ExceptionResponse.class))})
    })
    @GetMapping(value = "/{diaryId}/comments/{ancestorId}/recomment")
    private ResponseEntity<List<DiaryReCommentResponse>> displayReComments(@PathVariable Long petId,
                                                                           @PathVariable Long diaryId,
                                                                           @PathVariable Long ancestorId,
                                                                           @AuthenticationPrincipal PrincipalDetails principalDetails) {
        return ResponseEntity.ok(diaryCommentService.displayReComments(principalDetails.getUser(), petId, diaryId, ancestorId));
    }
}
