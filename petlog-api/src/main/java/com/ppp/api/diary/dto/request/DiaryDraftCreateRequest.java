package com.ppp.api.diary.dto.request;

import com.ppp.common.validator.Date;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DiaryDraftCreateRequest {
    @Schema(description = "제목", example = "오늘은 공원에서 산책을 했어요")
    @Size(min = 1, max = 255, message = "1자 이상 255자 이하의 제목을 입력해주세요.")
    private String title;

    @Schema(description = "내용", example = "날씨가 좋아서 산책을 했답니다")
    @Size(min = 1, max = 5000, message = "1자 이상 5000자 이하의 내용을 입력해주세요.")
    private String content;

    @Schema(description = "날짜", example = "2024-01-31")
    @Date(message = "적합한 날짜를 입력해주세요.")
    @Getter(value = AccessLevel.NONE)
    private String date;

    @Schema(description = "공개 여부")
    private Boolean isPublic;

    @ArraySchema(arraySchema = @Schema(description = "임시 동영상 아이디", example = "[\"c8e8f796-8e29-4067-86c4-0eae419a054e\"]"))
    @Size(max = 1)
    private List<String> uploadedVideoIds = new ArrayList<>();

    public LocalDate getDate() {
        return LocalDate.parse(date);
    }
}

