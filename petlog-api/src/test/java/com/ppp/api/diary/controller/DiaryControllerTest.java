package com.ppp.api.diary.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ppp.api.diary.dto.request.DiaryCreateRequest;
import com.ppp.api.diary.dto.request.DiaryUpdateRequest;
import com.ppp.api.diary.service.DiaryService;
import com.ppp.api.test.WithMockCustomUser;
import com.ppp.common.security.UserDetailsServiceImpl;
import com.ppp.common.security.jwt.JwtAuthenticationFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = DiaryController.class)
@AutoConfigureMockMvc(addFilters = false)
class DiaryControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private DiaryService diaryService;

    private static final String TOKEN = "Bearer token";

    @Test
    @WithMockCustomUser
    @DisplayName("일기 생성 성공")
    void createDiary_success() throws Exception {
        //given
        DiaryCreateRequest request = DiaryCreateRequest.builder()
                .title("우리 강아지")
                .content("너무 귀엽당")
                .uploadedVideoIds(List.of("c8e8f796-8e29-4067-86c4-0eae419a054e"))
                .date(LocalDate.now().toString())
                .build();
        //when
        mockMvc.perform(multipart("/api/v1/pets/{petId}/diaries", 1L)
                        .file(new MockMultipartFile("request", "json",
                                MediaType.APPLICATION_JSON_VALUE,
                                objectMapper.writeValueAsString(request).getBytes(StandardCharsets.UTF_8)))
                        .file(new MockMultipartFile("images", "image.jpg",
                                MediaType.IMAGE_JPEG_VALUE, "abcde" .getBytes()))
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                ).andDo(print())
                .andExpect(status().isOk());
        //then
    }

    @Test
    @WithMockCustomUser
    @DisplayName("일기 생성 성공-file not required")
    void createDiary_success_FILE_NOT_REQUIRED() throws Exception {
        //given
        DiaryCreateRequest request = DiaryCreateRequest.builder()
                .title("우리 강아지")
                .content("너무 귀엽당")
                .uploadedVideoIds(List.of("c8e8f796-8e29-4067-86c4-0eae419a054e"))
                .date(LocalDate.now().toString())
                .build();
        //when
        mockMvc.perform(multipart("/api/v1/pets/{petId}/diaries", 1L, 1L)
                        .file(new MockMultipartFile("request", "json",
                                MediaType.APPLICATION_JSON_VALUE,
                                objectMapper.writeValueAsString(request).getBytes(StandardCharsets.UTF_8)))
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                ).andDo(print())
                .andExpect(status().isOk());
        //then
    }

    @Test
    @WithMockCustomUser
    @DisplayName("일기 생성 실패-잘못된 datetime")
    void createDiary_fail_WhenDatetimeIsWrong() throws Exception {
        //given
        DiaryCreateRequest request = DiaryCreateRequest.builder()
                .title("우리 강아지")
                .content("너무 귀엽당")
                .uploadedVideoIds(List.of("c8e8f796-8e29-4067-86c4-0eae419a054e"))
                .date("2024-03-2")
                .build();
        //when
        mockMvc.perform(multipart("/api/v1/pets/{petId}/diaries", 1L, 1L)
                        .file(new MockMultipartFile("request", "json",
                                MediaType.APPLICATION_JSON_VALUE,
                                objectMapper.writeValueAsString(request).getBytes(StandardCharsets.UTF_8)))
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                ).andDo(print())
                .andExpect(status().isBadRequest());
        //then
    }

    @Test
    @WithMockCustomUser
    @DisplayName("일기 수정 성공")
    void updateDiary_success() throws Exception {
        //given
        DiaryUpdateRequest request = DiaryUpdateRequest.builder()
                .title("우리 강아지")
                .content("너무 귀엽당")
                .date(LocalDate.now().toString())
                .deletedVideoIds(Set.of(1L))
                .uploadedVideoIds(List.of("c8e8f796-8e29-4067-86c4-0eae419a054e"))
                .build();
        //when
        mockMvc.perform(multipart("/api/v1/pets/{petId}/diaries/{diaryId}", 1L, 1L)
                        .file(new MockMultipartFile("request", "json",
                                MediaType.APPLICATION_JSON_VALUE,
                                objectMapper.writeValueAsString(request).getBytes(StandardCharsets.UTF_8)))
                        .file(new MockMultipartFile("images", "image.jpg",
                                MediaType.IMAGE_JPEG_VALUE, "abcde" .getBytes()))
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .with(httpServletRequest -> {
                            httpServletRequest.setMethod("PUT");
                            return httpServletRequest;
                        })
                ).andDo(print())
                .andExpect(status().isOk());
        //then
    }

    @Test
    @WithMockCustomUser
    @DisplayName("일기 수정 성공-file not required")
    void updateDiary_success_FILE_NOT_REQUIRED() throws Exception {
        //given
        DiaryUpdateRequest request = DiaryUpdateRequest.builder()
                .title("우리 강아지")
                .content("너무 귀엽당")
                .date(LocalDate.now().toString())
                .deletedVideoIds(Set.of(1L))
                .uploadedVideoIds(List.of("c8e8f796-8e29-4067-86c4-0eae419a054e"))
                .build();
        //when
        mockMvc.perform(multipart("/api/v1/pets/{petId}/diaries/{diaryId}", 1L, 1L)
                        .file(new MockMultipartFile("request", "json",
                                MediaType.APPLICATION_JSON_VALUE,
                                objectMapper.writeValueAsString(request).getBytes(StandardCharsets.UTF_8)))
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .with(httpServletRequest -> {
                            httpServletRequest.setMethod("PUT");
                            return httpServletRequest;
                        })
                ).andDo(print())
                .andExpect(status().isOk());
        //then
    }

    @Test
    @WithMockCustomUser
    @DisplayName("일기 삭제 성공")
    void deleteDiary_success() throws Exception {
        mockMvc.perform(delete("/api/v1/pets/{petId}/diaries/{diaryId}", 1L, 1L)
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                ).andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @WithMockCustomUser
    @DisplayName("일기 상세 조회 성공")
    void displayDiary_success() throws Exception {
        //given
        //when
        mockMvc.perform(get("/api/v1/pets/{petId}/diaries/{diaryId}", 1L, 1L)
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                ).andDo(print())
                .andExpect(status().isOk());
        //then
    }

    @Test
    @WithMockCustomUser
    @DisplayName("일기 리스트 조회 성공")
    void displayDiaries_success() throws Exception {
        //given
        //when
        mockMvc.perform(get("/api/v1/pets/{petId}/diaries", 1L)
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                ).andDo(print())
                .andExpect(status().isOk());
        //then
    }

    @Test
    @WithMockCustomUser
    @DisplayName("일기 상세 조회 성공")
    void displayFeedDiary_success() throws Exception {
        //given
        //when
        mockMvc.perform(get("/api/v1/pets/{petId}/diaries/{diaryId}/feed", 1L, 1L)
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                ).andDo(print())
                .andExpect(status().isOk());
        //then
    }
}